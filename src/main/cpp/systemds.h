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

/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_apache_sysds_utils_NativeHelper */

#ifndef _Included_org_apache_sysds_utils_NativeHelper
#define _Included_org_apache_sysds_utils_NativeHelper
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_apache_sysds_utils_NativeHelper
 * Method:    dmmdd
 * Signature: ([D[D[DIIII)I
 */
JNIEXPORT jlong JNICALL Java_org_apache_sysds_utils_NativeHelper_dmmdd
  (JNIEnv *, jclass, jdoubleArray, jdoubleArray, jdoubleArray, jint, jint, jint, jint);

/*
 * Class:     org_apache_sysds_utils_NativeHelper
 * Method:    smmdd
 * Signature: (Ljava/nio/FloatBuffer;Ljava/nio/FloatBuffer;Ljava/nio/FloatBuffer;IIII)I
 */
JNIEXPORT jlong JNICALL Java_org_apache_sysds_utils_NativeHelper_smmdd
  (JNIEnv *, jclass, jobject, jobject, jobject, jint, jint, jint, jint);

/*
 * Class:     org_apache_sysds_utils_NativeHelper
 * Method:    tsmm
 * Signature: ([D[DIIZI)I
 */
JNIEXPORT jlong JNICALL Java_org_apache_sysds_utils_NativeHelper_tsmm
  (JNIEnv *, jclass, jdoubleArray, jdoubleArray, jint, jint, jboolean, jint);

/*
 * Class:     org_apache_sysds_utils_NativeHelper
 * Method:    conv2dDense
 * Signature: ([D[D[DIIIIIIIIIIIIII)I
 */
JNIEXPORT jlong JNICALL Java_org_apache_sysds_utils_NativeHelper_conv2dDense
  (JNIEnv *, jclass, jdoubleArray, jdoubleArray, jdoubleArray, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint);

/*
 * Class:     org_apache_sysds_utils_NativeHelper
 * Method:    dconv2dBiasAddDense
 * Signature: ([D[D[D[DIIIIIIIIIIIIII)I
 */
JNIEXPORT jlong JNICALL Java_org_apache_sysds_utils_NativeHelper_dconv2dBiasAddDense
  (JNIEnv *, jclass, jdoubleArray, jdoubleArray, jdoubleArray, jdoubleArray, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint);

/*
 * Class:     org_apache_sysds_utils_NativeHelper
 * Method:    sconv2dBiasAddDense
 * Signature: (Ljava/nio/FloatBuffer;Ljava/nio/FloatBuffer;Ljava/nio/FloatBuffer;Ljava/nio/FloatBuffer;IIIIIIIIIIIIII)I
 */
JNIEXPORT jlong JNICALL Java_org_apache_sysds_utils_NativeHelper_sconv2dBiasAddDense
  (JNIEnv *, jclass, jobject, jobject, jobject, jobject, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint);

/*
 * Class:     org_apache_sysds_utils_NativeHelper
 * Method:    conv2dBackwardFilterDense
 * Signature: ([D[D[DIIIIIIIIIIIIII)I
 */
JNIEXPORT jlong JNICALL Java_org_apache_sysds_utils_NativeHelper_conv2dBackwardFilterDense
  (JNIEnv *, jclass, jdoubleArray, jdoubleArray, jdoubleArray, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint);

/*
 * Class:     org_apache_sysds_utils_NativeHelper
 * Method:    conv2dBackwardDataDense
 * Signature: ([D[D[DIIIIIIIIIIIIII)I
 */
JNIEXPORT jlong JNICALL Java_org_apache_sysds_utils_NativeHelper_conv2dBackwardDataDense
  (JNIEnv *, jclass, jdoubleArray, jdoubleArray, jdoubleArray, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint);

/*
 * Class:     org_apache_sysds_utils_NativeHelper
 * Method:    conv2dBackwardFilterSparseDense
 * Signature: (II[I[D[D[DIIIIIIIIIIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_org_apache_sysds_utils_NativeHelper_conv2dBackwardFilterSparseDense
  (JNIEnv *, jclass, jint, jint, jintArray, jdoubleArray, jdoubleArray, jdoubleArray, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint);

/*
 * Class:     org_apache_sysds_utils_NativeHelper
 * Method:    conv2dSparse
 * Signature: (II[I[D[D[DIIIIIIIIIIIIII)Z
 */
JNIEXPORT jboolean JNICALL Java_org_apache_sysds_utils_NativeHelper_conv2dSparse
  (JNIEnv *, jclass, jint, jint, jintArray, jdoubleArray, jdoubleArray, jdoubleArray, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint, jint);

/*
 * Class:     org_apache_sysds_utils_NativeHelper
 * Method:    setMaxNumThreads
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_org_apache_sysds_utils_NativeHelper_setMaxNumThreads
  (JNIEnv *, jclass, jint);

 /*
    * Class:     org_apache_sysds_utils_NativeHelper
    * Method:    testNativeBindingWithDgemm
    * Signature: (CCIIID[DI[DID[DI)V
   */

JNIEXPORT void JNICALL Java_org_apache_sysds_utils_NativeHelper_testNativeBindingWithDgemm
   (JNIEnv *, jclass, jchar, jchar, jint, jint, jint, jdouble, jdoubleArray, jint, jdoubleArray, jint, jdouble, jdoubleArray, jint);

/*
 * Class:     org_apache_sysds_utils_NativeHelper
 * Method:    imageRotate
 * Signature: ([DIIIDD[D)V
 */
JNIEXPORT void JNICALL Java_org_apache_sysds_utils_NativeHelper_imageRotate
    (JNIEnv *, jclass, jdoubleArray, jint, jint, jdouble, jdouble, jdoubleArray);

/*
 * Class:     org_apache_sysds_utils_NativeHelper
 * Method:    imageCutout
 * Signature: ([DIIIIIDD)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_org_apache_sysds_utils_NativeHelper_imageCutout
  (JNIEnv *, jclass, jdoubleArray, jint, jint, jint, jint, jint, jint, jdouble);

/*
 * Class:     org_apache_sysds_utils_NativeHelper
 * Method:    cropImage
 * Signature: ([DIIIIII)[D
 */
JNIEXPORT jdoubleArray JNICALL Java_org_apache_sysds_utils_NativeHelper_cropImage(JNIEnv *, jclass,
    jdoubleArray, jint, jint, jint, jint, jint, jint);
#ifdef __cplusplus
}
#endif
#endif
