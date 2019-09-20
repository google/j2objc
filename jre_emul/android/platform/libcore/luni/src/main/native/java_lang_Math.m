/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// JNI functions for java.lang.Math.

#include "java/lang/Double.h"
#include "jni.h"

#include <math.h>

JNIEXPORT jdouble Java_java_lang_Math_acos(JNIEnv *env, jclass cls, jdouble d) {
  return acos(d);
}

JNIEXPORT jdouble Java_java_lang_Math_asin(JNIEnv *env, jclass cls, jdouble d) {
  return asin(d);
}

JNIEXPORT jdouble Java_java_lang_Math_atan(JNIEnv *env, jclass cls, jdouble d) {
  return atan(d);
}

JNIEXPORT jdouble Java_java_lang_Math_atan2(JNIEnv *env, jclass cls, jdouble x, jdouble y) {
  return atan2(x, y);
}

JNIEXPORT jdouble Java_java_lang_Math_cbrt(JNIEnv *env, jclass cls, jdouble d) {
  return cbrt(d);
}

JNIEXPORT jdouble Java_java_lang_Math_ceil(JNIEnv *env, jclass cls, jdouble d) {
  return ceil(d);
}

JNIEXPORT jdouble Java_java_lang_Math_cos(JNIEnv *env, jclass cls, jdouble d) {
  return cos(d);
}

JNIEXPORT jdouble Java_java_lang_Math_cosh(JNIEnv *env, jclass cls, jdouble d) {
  return cosh(d);
}

JNIEXPORT jdouble Java_java_lang_Math_exp(JNIEnv *env, jclass cls, jdouble d) {
  return exp(d);
}

JNIEXPORT jdouble Java_java_lang_Math_expm1(JNIEnv *env, jclass cls, jdouble d) {
  return expm1(d);
}

JNIEXPORT jdouble Java_java_lang_Math_floor(JNIEnv *env, jclass cls, jdouble d) {
  return floor(d);
}

JNIEXPORT jdouble Java_java_lang_Math_hypot(JNIEnv *env, jclass cls, jdouble x, jdouble y) {
  // ARM processors return hypot(x, NaN) as x, so test separately.
  if (isnan(x) || isnan(y)) {
    return (isinf(x) || isinf(y)) ? JavaLangDouble_POSITIVE_INFINITY : JavaLangDouble_NaN;
  }
  return hypot(x, y);
}

JNIEXPORT jdouble Java_java_lang_Math_IEEEremainder(JNIEnv *env, jclass cls, jdouble x, jdouble y) {
  // According to the Mac OS X math.h online man page, their routines are
  // all IEEE Standard 754 compliant.
  return remainder(x, y);
}

JNIEXPORT jdouble Java_java_lang_Math_log(JNIEnv *env, jclass cls, jdouble d) {
  return log(d);
}

JNIEXPORT jdouble Java_java_lang_Math_log10(JNIEnv *env, jclass cls, jdouble d) {
  return log10(d);
}

JNIEXPORT jdouble Java_java_lang_Math_log1p(JNIEnv *env, jclass cls, jdouble d) {
  return log1p(d);
}

JNIEXPORT jdouble Java_java_lang_Math_pow(JNIEnv *env, jclass cls, jdouble x, jdouble y) {
  return pow(x, y);
}

JNIEXPORT jdouble Java_java_lang_Math_rint(JNIEnv *env, jclass cls, jdouble d) {
  return rint(d);
}

JNIEXPORT jdouble Java_java_lang_Math_sin(JNIEnv *env, jclass cls, jdouble d) {
  return sin(d);
}

JNIEXPORT jdouble Java_java_lang_Math_sinh(JNIEnv *env, jclass cls, jdouble d) {
  return sinh(d);
}

JNIEXPORT jdouble Java_java_lang_Math_sqrt(JNIEnv *env, jclass cls, jdouble d) {
  return sqrt(d);
}

JNIEXPORT jdouble Java_java_lang_Math_tan(JNIEnv *env, jclass cls, jdouble d) {
  return tan(d);
}

JNIEXPORT jdouble Java_java_lang_Math_tanh(JNIEnv *env, jclass cls, jdouble d) {
  return tanh(d);
}
