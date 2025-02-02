# -------------------------------------------------------------
#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
# -------------------------------------------------------------

# Autogenerated By   : src/main/python/generator/generator.py
# Autogenerated From : scripts/builtin/auc.dml

from typing import Dict, Iterable

from systemds.operator import OperationNode, Matrix, Frame, List, MultiReturn, Scalar
from systemds.script_building.dag import OutputType
from systemds.utils.consts import VALID_INPUT_TYPES


def auc(Y: Matrix,
        P: Matrix):
    """
     This builting function computes the area under the ROC curve (AUC)
     for binary classifiers.
    
    
    
    :param Y: Binary response vector (shape: n x 1), in -1/+1 or 0/1 encoding
    :param P: Prediction scores (predictor such as estimated probabilities)
        for true class (shape: n x 1), assumed in [0,1]
    :return: Area under the ROC curve (AUC)
    """

    params_dict = {'Y': Y, 'P': P}
    return Matrix(Y.sds_context,
        'auc',
        named_input_nodes=params_dict)
