/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysml.hops.estim;

import java.util.BitSet;
import java.util.stream.IntStream;

import org.apache.commons.lang.NotImplementedException;
import org.apache.sysml.hops.OptimizerUtils;
import org.apache.sysml.runtime.controlprogram.parfor.stat.InfrastructureAnalyzer;
import org.apache.sysml.runtime.matrix.MatrixCharacteristics;
import org.apache.sysml.runtime.matrix.data.DenseBlock;
import org.apache.sysml.runtime.matrix.data.MatrixBlock;
import org.apache.sysml.runtime.matrix.data.SparseBlock;

/**
 * This estimator implements a naive but rather common approach of boolean matrix
 * multiplies which allows to infer the exact non-zero structure and thus is
 * also useful for sparse result preallocation.
 * 
 * For example, the following paper indicates that this approach is used for sparse
 * spGEMM in NVIDIA cuSPARSE and Intel MKL:
 * Weifeng Liu and Brian Vinter. An Efficient GPU General Sparse Matrix-Matrix
 * Multiplication for Irregular Data. In IPDPS, pages 370–381, 2014.
 * 
 */
public class EstimatorBitsetMM extends SparsityEstimator {
	@Override
	public MatrixCharacteristics estim(MMNode root) {
		// recursive density map computation of non-leaf nodes
		if (!root.getLeft().isLeaf())
			estim(root.getLeft()); // obtain synopsis
		if (!root.getRight().isLeaf())
			estim(root.getLeft()); // obtain synopsis
		BitsetMatrix m1Map = !root.getLeft().isLeaf() ? (BitsetMatrix) root.getLeft().getSynopsis() :
			new BitsetMatrix1(root.getLeft().getData());
		BitsetMatrix m2Map = !root.getRight().isLeaf() ? (BitsetMatrix) root.getRight().getSynopsis() :
			new BitsetMatrix1(root.getRight().getData());

		// estimate output density map and sparsity via boolean matrix mult
		BitsetMatrix outMap = m1Map.matMult(m2Map);
		root.setSynopsis(outMap); // memoize boolean matrix
		return root.setMatrixCharacteristics(new MatrixCharacteristics(
			outMap.getNumRows(), outMap.getNumColumns(), outMap.getNonZeros()));
	}

	@Override
	public double estim(MatrixBlock m1, MatrixBlock m2) {
		BitsetMatrix m1Map = new BitsetMatrix1(m1);
		BitsetMatrix m2Map = (m1 == m2) ? //self product
			m1Map : new BitsetMatrix1(m2);
		BitsetMatrix outMap = m1Map.matMult(m2Map);
		return OptimizerUtils.getSparsity( // aggregate output histogram
				outMap.getNumRows(), outMap.getNumColumns(), outMap.getNonZeros());
	}
	
	@Override
	public double estim(MatrixBlock m1, MatrixBlock m2, OpCode op) {
		throw new NotImplementedException();
	}
	
	@Override
	public double estim(MatrixBlock m, OpCode op) {
		throw new NotImplementedException();
	}

	private abstract static class BitsetMatrix {
		protected final int _rlen;
		protected final int _clen;
		protected long _nonZeros;
	
		public BitsetMatrix(int rlen, int clen) {
			_rlen = rlen;
			_clen = clen;
			_nonZeros = 0;
		}
		
		public int getNumRows() {
			return _rlen;
		}

		public int getNumColumns() {
			return _clen;
		}

		public long getNonZeros() {
			return _nonZeros;
		}
		
		protected void init(MatrixBlock in) {
			if (in.isEmptyBlock(false))
				return;
			if( MULTI_THREADED_BUILD && in.getNonZeros() > MIN_PAR_THRESHOLD ) {
				int k = 4 * InfrastructureAnalyzer.getLocalParallelism();
				int blklen = (int)Math.ceil((double)_rlen/k);
				IntStream.range(0, k).parallel().forEach(i ->
					buildIntern(in, i*blklen, Math.min((i+1)*blklen, _rlen)));
			}
			else {
				//single-threaded bitset construction
				buildIntern(in, 0, in.getNumRows());
			}
			_nonZeros = in.getNonZeros();
		}

		public BitsetMatrix matMult(BitsetMatrix m2) {
			BitsetMatrix out = createBitSetMatrix(_rlen, m2._clen);
			if( this.getNonZeros() == 0 || m2.getNonZeros() == 0 )
				return out;
			long size = (long)_rlen*_clen+(long)m2._rlen*m2._clen;
			if( MULTI_THREADED_ESTIM && size > MIN_PAR_THRESHOLD ) {
				int k = 4 * InfrastructureAnalyzer.getLocalParallelism();
				int blklen = (int)Math.ceil((double)_rlen/k);
				out._nonZeros = IntStream.range(0, k).parallel().mapToLong(i ->
					matMultIntern(m2, out, i*blklen, Math.min((i+1)*blklen, _rlen))).sum();
			}
			else {
				//single-threaded boolean matrix mult
				out._nonZeros = matMultIntern(m2, out, 0, _rlen);
			}
			return out;
		}
		
		protected abstract BitsetMatrix createBitSetMatrix(int rlen, int clen);
		
		protected abstract void buildIntern(MatrixBlock in, int rl, int ru);
		
		protected abstract long matMultIntern(BitsetMatrix bsb, BitsetMatrix bsc, int rl, int ru);
	}
	
	/**
	 * This class represents a boolean matrix and provides key operations.
	 * In the interest of a cache-conscious matrix multiplication and reduced
	 * memory overhead, we use a linearized and padded array of longs instead
	 * of Java's BitSet per row (which causes memory overheads per row and does
	 * not allow for range ORs). However, this implies a maximum size of 16GB.
	 * 
	 */
	private static class BitsetMatrix1 extends BitsetMatrix {
		//linearized and padded data array in row-major order, where each long
		//represents 64 boolean values, all rows are aligned at 64 for simple access
		private final int _rowLen;
		private final long[] _data;

		public BitsetMatrix1(int rlen, int clen) {
			super(rlen, clen);
			_rowLen = (int) Math.ceil((double)clen / 64);
			_data = new long[rlen * _rowLen];
		}
		
		public BitsetMatrix1(MatrixBlock in) {
			this(in.getNumRows(), in.getNumColumns());
			init(in);
		}
		
		@Override
		protected BitsetMatrix createBitSetMatrix(int rlen, int clen) {
			return new BitsetMatrix1(rlen, clen);
		}
		
		@Override
		protected void buildIntern(MatrixBlock in, int rl, int ru) {
			if (in.isInSparseFormat()) {
				SparseBlock sblock = in.getSparseBlock();
				for (int i = rl; i < ru; i++) {
					if (sblock.isEmpty(i))
						continue;
					int alen = sblock.size(i);
					int apos = sblock.pos(i);
					int[] aix = sblock.indexes(i);
					for (int k = apos; k < apos + alen; k++)
						set(i, aix[k]);
				}
			} else {
				DenseBlock dblock = in.getDenseBlock();
				for (int i = rl; i < ru; i++) {
					double[] avals = dblock.values(i);
					int aix = dblock.pos(i);
					for (int j = 0; j < in.getNumColumns(); j++)
						if (avals[aix + j] != 0)
							set(i, j);
				}
			}
		}
		
		@Override
		protected long matMultIntern(BitsetMatrix bsb2, BitsetMatrix bsc2, int rl, int ru) {
			BitsetMatrix1 bsb = (BitsetMatrix1) bsb2;
			BitsetMatrix1 bsc = (BitsetMatrix1) bsc2;
			final long[] b = bsb._data;
			final long[] c = bsc._data;
			final int cd = _clen;
			final int n = bsb._clen;
			final int n64 = bsb._rowLen;
			
			final int blocksizeI = 32;
			final int blocksizeK = 24;
			final int blocksizeJ = 1024 * 64;
			
			long lnnz = 0;
			
			//blocked execution (cache-conscious)
			for( int bi = rl; bi < ru; bi+=blocksizeI ) {
				int bimin = Math.min(ru, bi+blocksizeI);
				for( int bk = 0; bk < cd; bk+=blocksizeK ) {
					int bkmin = Math.min(cd, bk+blocksizeK);
					for( int bj = 0; bj < n; bj+=blocksizeJ ) {
						//core sub block matrix multiplication
						int bjlen64 = (int)Math.ceil((double)(Math.min(n, bj+blocksizeJ)-bj)/64);
						int bj64 = bj/64;
						for( int i = bi, off=i*_rowLen; i < bimin; i++, off+=_rowLen) {
							for( int k = bk; k < bkmin; k++ ) {
								if( getCol(off, k) ) //implicit and
									or(b, c, k*n64+bj64, i*n64+bj64, bjlen64);
							}
						}
					}
				}
				// maintain nnz for entire output row block
				lnnz += card(c, bi*n64, (bimin-bi)*n64);
			}
			
			return lnnz;
		}
		
		private void set(int r, int c) {
			int off = r * _rowLen;
			int wordIndex = wordIndex(c); //see BitSet.java
			_data[off + wordIndex] |= (1L << c); //see BitSet.java
		}
		
		@SuppressWarnings("unused")
		private boolean get(int r, int c) {
			int off = r * _rowLen;
			int wordIndex = wordIndex(c); //see Bitset.java
			return (_data[off + wordIndex] & (1L << c)) != 0; //see BitSet.java
		}
		
		private boolean getCol(int off, int c) {
			int wordIndex = wordIndex(c); //see Bitset.java
			return (_data[off + wordIndex] & (1L << c)) != 0; //see BitSet.java
		}
		
		private static int wordIndex(int bitIndex) {
			return bitIndex >> 6; //see BitSet.java
		}
		
		private static int card(long[] c, int ci, int len) {
			int sum = 0;
			for( int i = ci; i < ci+len; i++ )
				sum += Long.bitCount(c[i]);
			return sum;
		}
		
		private static void or(long[] b, long[] c, int bi, int ci, int len) {
			final int bn = len%8;
			//compute rest
			for( int i = 0; i < bn; i++, bi++, ci++ )
				c[ci] |= b[bi];
			//unrolled 8-block (for better instruction-level parallelism)
			for( int i = bn; i < len; i+=8, bi+=8, ci+=8 ) {
				c[ci+0] |= b[bi+0]; c[ci+1] |= b[bi+1];
				c[ci+2] |= b[bi+2]; c[ci+3] |= b[bi+3];
				c[ci+4] |= b[bi+4]; c[ci+5] |= b[bi+5];
				c[ci+6] |= b[bi+6]; c[ci+7] |= b[bi+7];
			}
		}
	}
	
	@SuppressWarnings("unused")
	private static class BitsetMatrix2 extends BitsetMatrix {
		private BitSet[] _data;

		public BitsetMatrix2(int rlen, int clen) {
			super(rlen, clen);
			_data = new BitSet[_rlen];
		}

		public BitsetMatrix2(MatrixBlock in) {
			this(in.getNumRows(), in.getNumColumns());
			init(in);
		}
		
		@Override
		protected BitsetMatrix createBitSetMatrix(int rlen, int clen) {
			return new BitsetMatrix2(rlen, clen);
		}
		
		@Override
		protected void buildIntern(MatrixBlock in, int rl, int ru) {
			final int clen = in.getNumColumns();
			if (in.isInSparseFormat()) {
				SparseBlock sblock = in.getSparseBlock();
				for (int i = rl; i < ru; i++) {
					if (sblock.isEmpty(i))
						continue;
					BitSet lbs = _data[i] = new BitSet(clen);
					int alen = sblock.size(i);
					int apos = sblock.pos(i);
					int[] aix = sblock.indexes(i);
					for (int k = apos; k < apos + alen; k++)
						lbs.set(aix[k]);
				}
			} else {
				DenseBlock dblock = in.getDenseBlock();
				for (int i = rl; i < ru; i++) {
					BitSet lbs = _data[i] = new BitSet(clen);
					double[] avals = dblock.values(i);
					int aix = dblock.pos(i);
					for (int j = 0; j < in.getNumColumns(); j++)
						if (avals[aix + j] != 0)
							lbs.set(j);
				}
			}
		}
		
		@Override
		protected long matMultIntern(BitsetMatrix bsb2, BitsetMatrix bsc2, int rl, int ru) {
			BitsetMatrix2 bsb = (BitsetMatrix2) bsb2;
			BitsetMatrix2 bsc = (BitsetMatrix2) bsc2;
			final int cd = _clen;
			final int n = bsb._clen;
			// matrix multiply with IKJ schedule and pure OR ops in inner loop
			long lnnz = 0;
			for (int i = rl; i < ru; i++) {
				BitSet a = _data[i];
				if( a != null ) {
					BitSet c = bsc._data[i] = new BitSet(n);
					for (int k = 0; k < cd; k++) {
						BitSet b = bsb._data[k];
						if (a.get(k) && b != null)
							c.or(b);
					}
					// maintain nnz
					lnnz += c.cardinality();
				}
			}
			return lnnz;
		}
	}
}
