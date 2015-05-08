/*
 * Copyright (C) 2011 The Android Open Source Project
 *
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

#include "jni.h"
#include <stdlib.h> // for abort

extern "C" jobject Java_dalvik_system_JniTest_returnThis(JNIEnv*, jobject obj) {
  return obj;
}

extern "C" jclass Java_dalvik_system_JniTest_returnClass(JNIEnv*, jclass klass) {
  return klass;
}

extern "C" jobject Java_dalvik_system_JniTest_returnObjectArgFrom16(
    JNIEnv*, jobject, int arg_no,
    jobject o1,  jobject o2,  jobject o3,  jobject o4,  jobject o5,
    jobject o6,  jobject o7,  jobject o8,  jobject o9,  jobject o10,
    jobject o11, jobject o12, jobject o13, jobject o14, jobject o15,
    jobject o16) {
  switch(arg_no){
  case 0:  return o1;
  case 1:  return o2;
  case 2:  return o3;
  case 3:  return o4;
  case 4:  return o5;
  case 5:  return o6;
  case 6:  return o7;
  case 7:  return o8;
  case 8:  return o9;
  case 9:  return o10;
  case 10: return o11;
  case 11: return o12;
  case 12: return o13;
  case 13: return o14;
  case 14: return o15;
  case 15: return o16;
  default: abort();
  }
}

extern "C" jboolean Java_dalvik_system_JniTest_returnBooleanArgFrom16(
    JNIEnv*, jobject, int arg_no,
    jboolean o1,  jboolean o2,  jboolean o3,  jboolean o4,  jboolean o5,
    jboolean o6,  jboolean o7,  jboolean o8,  jboolean o9,  jboolean o10,
    jboolean o11, jboolean o12, jboolean o13, jboolean o14, jboolean o15,
    jboolean o16) {
  switch(arg_no){
  case 0:  return o1;
  case 1:  return o2;
  case 2:  return o3;
  case 3:  return o4;
  case 4:  return o5;
  case 5:  return o6;
  case 6:  return o7;
  case 7:  return o8;
  case 8:  return o9;
  case 9:  return o10;
  case 10: return o11;
  case 11: return o12;
  case 12: return o13;
  case 13: return o14;
  case 14: return o15;
  case 15: return o16;
  default: abort();
  }
}

extern "C" jchar Java_dalvik_system_JniTest_returnCharArgFrom16(
    JNIEnv*, jobject, int arg_no,
    jchar o1,  jchar o2,  jchar o3,  jchar o4,  jchar o5,
    jchar o6,  jchar o7,  jchar o8,  jchar o9,  jchar o10,
    jchar o11, jchar o12, jchar o13, jchar o14, jchar o15,
    jchar o16) {
  switch(arg_no){
  case 0:  return o1;
  case 1:  return o2;
  case 2:  return o3;
  case 3:  return o4;
  case 4:  return o5;
  case 5:  return o6;
  case 6:  return o7;
  case 7:  return o8;
  case 8:  return o9;
  case 9:  return o10;
  case 10: return o11;
  case 11: return o12;
  case 12: return o13;
  case 13: return o14;
  case 14: return o15;
  case 15: return o16;
  default: abort();
  }
}

extern "C" jbyte Java_dalvik_system_JniTest_returnByteArgFrom16(
    JNIEnv*, jobject, int arg_no,
    jbyte o1,  jbyte o2,  jbyte o3,  jbyte o4,  jbyte o5,
    jbyte o6,  jbyte o7,  jbyte o8,  jbyte o9,  jbyte o10,
    jbyte o11, jbyte o12, jbyte o13, jbyte o14, jbyte o15,
    jbyte o16) {
  switch(arg_no){
  case 0:  return o1;
  case 1:  return o2;
  case 2:  return o3;
  case 3:  return o4;
  case 4:  return o5;
  case 5:  return o6;
  case 6:  return o7;
  case 7:  return o8;
  case 8:  return o9;
  case 9:  return o10;
  case 10: return o11;
  case 11: return o12;
  case 12: return o13;
  case 13: return o14;
  case 14: return o15;
  case 15: return o16;
  default: abort();
  }
}

extern "C" jshort Java_dalvik_system_JniTest_returnShortArgFrom16(
    JNIEnv*, jobject, int arg_no,
    jshort o1,  jshort o2,  jshort o3,  jshort o4,  jshort o5,
    jshort o6,  jshort o7,  jshort o8,  jshort o9,  jshort o10,
    jshort o11, jshort o12, jshort o13, jshort o14, jshort o15,
    jshort o16) {
  switch(arg_no){
  case 0:  return o1;
  case 1:  return o2;
  case 2:  return o3;
  case 3:  return o4;
  case 4:  return o5;
  case 5:  return o6;
  case 6:  return o7;
  case 7:  return o8;
  case 8:  return o9;
  case 9:  return o10;
  case 10: return o11;
  case 11: return o12;
  case 12: return o13;
  case 13: return o14;
  case 14: return o15;
  case 15: return o16;
  default: abort();
  }
}

extern "C" jint Java_dalvik_system_JniTest_returnIntArgFrom16(
    JNIEnv*, jobject, int arg_no,
    jint o1,  jint o2,  jint o3,  jint o4,  jint o5,
    jint o6,  jint o7,  jint o8,  jint o9,  jint o10,
    jint o11, jint o12, jint o13, jint o14, jint o15,
    jint o16) {
  switch(arg_no){
  case 0:  return o1;
  case 1:  return o2;
  case 2:  return o3;
  case 3:  return o4;
  case 4:  return o5;
  case 5:  return o6;
  case 6:  return o7;
  case 7:  return o8;
  case 8:  return o9;
  case 9:  return o10;
  case 10: return o11;
  case 11: return o12;
  case 12: return o13;
  case 13: return o14;
  case 14: return o15;
  case 15: return o16;
  default: abort();
  }
}

extern "C" jlong Java_dalvik_system_JniTest_returnLongArgFrom16(
    JNIEnv*, jobject, int arg_no,
    jlong o1,  jlong o2,  jlong o3,  jlong o4,  jlong o5,
    jlong o6,  jlong o7,  jlong o8,  jlong o9,  jlong o10,
    jlong o11, jlong o12, jlong o13, jlong o14, jlong o15,
    jlong o16) {
  switch(arg_no){
  case 0:  return o1;
  case 1:  return o2;
  case 2:  return o3;
  case 3:  return o4;
  case 4:  return o5;
  case 5:  return o6;
  case 6:  return o7;
  case 7:  return o8;
  case 8:  return o9;
  case 9:  return o10;
  case 10: return o11;
  case 11: return o12;
  case 12: return o13;
  case 13: return o14;
  case 14: return o15;
  case 15: return o16;
  default: abort();
  }
}

extern "C" jfloat Java_dalvik_system_JniTest_returnFloatArgFrom16(
    JNIEnv*, jobject, int arg_no,
    jfloat o1,  jfloat o2,  jfloat o3,  jfloat o4,  jfloat o5,
    jfloat o6,  jfloat o7,  jfloat o8,  jfloat o9,  jfloat o10,
    jfloat o11, jfloat o12, jfloat o13, jfloat o14, jfloat o15,
    jfloat o16) {
  switch(arg_no){
  case 0:  return o1;
  case 1:  return o2;
  case 2:  return o3;
  case 3:  return o4;
  case 4:  return o5;
  case 5:  return o6;
  case 6:  return o7;
  case 7:  return o8;
  case 8:  return o9;
  case 9:  return o10;
  case 10: return o11;
  case 11: return o12;
  case 12: return o13;
  case 13: return o14;
  case 14: return o15;
  case 15: return o16;
  default: abort();
  }
}

extern "C" jdouble Java_dalvik_system_JniTest_returnDoubleArgFrom16(
    JNIEnv*, jobject, int arg_no,
    jdouble o1,  jdouble o2,  jdouble o3,  jdouble o4,  jdouble o5,
    jdouble o6,  jdouble o7,  jdouble o8,  jdouble o9,  jdouble o10,
    jdouble o11, jdouble o12, jdouble o13, jdouble o14, jdouble o15,
    jdouble o16) {
  switch(arg_no){
  case 0:  return o1;
  case 1:  return o2;
  case 2:  return o3;
  case 3:  return o4;
  case 4:  return o5;
  case 5:  return o6;
  case 6:  return o7;
  case 7:  return o8;
  case 8:  return o9;
  case 9:  return o10;
  case 10: return o11;
  case 11: return o12;
  case 12: return o13;
  case 13: return o14;
  case 14: return o15;
  case 15: return o16;
  default: abort();
  }
}

extern "C" jclass Java_dalvik_system_JniTest_envGetSuperclass(
    JNIEnv* env, jobject, jclass clazz) {
  return env->GetSuperclass(clazz);
}
