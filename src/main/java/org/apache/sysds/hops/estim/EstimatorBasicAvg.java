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

package org.apache.sysds.hops.estim;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.sysds.hops.OptimizerUtils;
import org.apache.sysds.runtime.matrix.data.MatrixBlock;
import org.apache.sysds.runtime.meta.DataCharacteristics;
import org.apache.sysds.runtime.meta.MatrixCharacteristics;

/**
 * Basic average case estimator for matrix sparsity:
 * sp = 1 - Math.pow(1-sp1*sp2, k)
 */
public class EstimatorBasicAvg extends SparsityEstimator
{
	@Override
	public DataCharacteristics estim(MMNode root) {
		DataCharacteristics dc1 = !root.getLeft().isLeaf() ?
			estim(root.getLeft()) : root.getLeft().getDataCharacteristics();
		DataCharacteristics dc2 = root.getRight()==null ? null :
			!root.getRight().isLeaf() ? estim(root.getRight()) : 
			root.getRight().getDataCharacteristics();
		return root.setDataCharacteristics(
				estimIntern(dc1, dc2, root.getOp()));
	}

	@Override
	public double estim(MatrixBlock m1, MatrixBlock m2) {
		return estim(m1, m2, OpCode.MM);
	}

	@Override
	public double estim(MatrixBlock m1, MatrixBlock m2, OpCode op) {
		return estimIntern(m1.getDataCharacteristics(), m2.getDataCharacteristics(), op).getSparsity();
	}

	@Override
	public double estim(MatrixBlock m, OpCode op) {
		return estimIntern(m.getDataCharacteristics(), null, op).getSparsity();
	}

	private DataCharacteristics estimIntern(DataCharacteristics dc1, DataCharacteristics dc2, OpCode op) {
		switch (op) {
			case MM:
				return new MatrixCharacteristics(dc1.getRows(), dc2.getCols(),
					OptimizerUtils.getMatMultNnz(dc1.getSparsity(), dc2.getSparsity(),
					dc1.getRows(), dc1.getCols(), dc2.getCols(), false));
			case MULT:
				return new MatrixCharacteristics(dc1.getRows(), dc1.getCols(),
					OptimizerUtils.getNnz(dc1.getRows(), dc1.getCols(),
						dc1.getSparsity() * dc2.getSparsity()));
			case PLUS:
				return new MatrixCharacteristics(dc1.getRows(), dc1.getCols(),
					OptimizerUtils.getNnz(dc1.getRows(), dc1.getCols(),
						dc1.getSparsity() + dc2.getSparsity() - dc1.getSparsity() * dc2.getSparsity()));
			case EQZERO:
			case DIAG:
			case CBIND:
			case RBIND:
			case NEQZERO:
			case TRANS:
			case RESHAPE:
				return estimExactMetaData(dc1, dc2, op);
			default:
				throw new NotImplementedException();
		}
	}
}
