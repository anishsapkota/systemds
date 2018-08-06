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

import org.apache.commons.lang.NotImplementedException;
import org.apache.sysml.hops.OptimizerUtils;
import org.apache.sysml.runtime.matrix.MatrixCharacteristics;
import org.apache.sysml.runtime.matrix.data.MatrixBlock;

/**
 * Basic average case estimator for matrix sparsity:
 * sp = Math.min(1, sp1 * k) * Math.min(1, sp2 * k).
 * 
 * Note: for outer-products (i.e., k=1) this worst-case
 * estimate is equivalent to the average case estimate and
 * the exact output sparsity.
 */
public class EstimatorBasicWorst extends SparsityEstimator
{
	@Override
	public MatrixCharacteristics estim(MMNode root) {
		MatrixCharacteristics mc1 = !root.getLeft().isLeaf() ?
			estim(root.getLeft()) : root.getLeft().getMatrixCharacteristics();
		MatrixCharacteristics mc2 = !root.getRight().isLeaf() ?
			estim(root.getRight()) : root.getRight().getMatrixCharacteristics();
		return root.setMatrixCharacteristics(
			estimIntern(mc1, mc2, root.getOp()));
	}

	@Override
	public double estim(MatrixBlock m1, MatrixBlock m2) {
		return estim(m1, m2, OpCode.MM);
	}

	@Override
	public double estim(MatrixBlock m1, MatrixBlock m2, OpCode op) {
		return estimIntern(m1.getMatrixCharacteristics(), m2.getMatrixCharacteristics(), op).getSparsity();
	}

	@Override
	public double estim(MatrixBlock m, OpCode op) {
		return estimIntern(m.getMatrixCharacteristics(), null, op).getSparsity();
	}

	private MatrixCharacteristics estimIntern(MatrixCharacteristics mc1, MatrixCharacteristics mc2, OpCode op) {
		switch (op) {
			case MM:
				return new MatrixCharacteristics(mc1.getRows(), mc2.getCols(),
					OptimizerUtils.getMatMultNnz(mc1.getSparsity(), mc2.getSparsity(),
					mc1.getRows(), mc1.getCols(), mc2.getCols(), true));
			case MULT:
				return new MatrixCharacteristics(mc1.getRows(), mc1.getCols(), 
					OptimizerUtils.getNnz(mc1.getRows(), mc1.getCols(),
						Math.min(mc1.getSparsity(), mc2.getSparsity())));
			case PLUS:
				return new MatrixCharacteristics(mc1.getRows(), mc1.getCols(), 
					OptimizerUtils.getNnz(mc1.getRows(), mc1.getCols(), 
						Math.min(mc1.getSparsity() + mc2.getSparsity(), 1)));
			case EQZERO:
				return new MatrixCharacteristics(mc1.getRows(), mc1.getCols(),
					(long) mc1.getRows() * mc1.getCols() - mc1.getNonZeros());
			case DIAG:
				return (mc1.getCols() == 1) ?
					new MatrixCharacteristics(mc1.getRows(), mc1.getRows(), mc1.getNonZeros()) :
					new MatrixCharacteristics(mc1.getRows(), 1, Math.min(mc1.getRows(), mc1.getNonZeros()));
			// binary operations that preserve sparsity exactly
			case CBIND:
				return new MatrixCharacteristics(mc1.getRows(), 
					mc1.getCols() + mc2.getCols(), mc1.getNonZeros() + mc2.getNonZeros());
			case RBIND:
				return new MatrixCharacteristics(mc1.getRows() + mc2.getRows(), 
					mc1.getCols(), mc1.getNonZeros() + mc2.getNonZeros());
			// unary operation that preserve sparsity exactly
			case NEQZERO:
			case TRANS:
			case RESHAPE:
				return mc1;
			default:
				throw new NotImplementedException();
		}
	}
}
