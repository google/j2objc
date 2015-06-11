// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//
//  jni.m
//  JreEmulation
//
//  JNI runtime support for J2ObjC applications. This is not a full JNI
//  implementation, since J2ObjC doesn't have anything similar to a JVM.
//
//  Created by Tom Ball on 4/22/15.
//

#include "jni.h"

#include "IOSArray.h"
#include "IOSClass.h"
#include "IOSObjectArray.h"
#include "IOSPrimitiveArray.h"
#include "java/lang/Throwable.h"
#include "java/nio/Buffer.h"
#include "java/nio/DirectByteBuffer.h"
#include "java/nio/NIOAccess.h"

__attribute__ ((unused)) static inline id null_chk(void *p) {
#if !defined(J2OBJC_DISABLE_NIL_CHECKS)
  if (__builtin_expect(!p, 0)) {
    JreThrowNullPointerException();
  }
#endif
  return p;
}

static void *GetPrimitiveArrayCritical(JNIEnv *, jarray, jboolean *);

static jclass FindClass(JNIEnv *env, const char *name) {
  return [IOSClass forName:[NSString stringWithUTF8String:name]];
}

static jsize GetArrayLength(JNIEnv *env, jarray array) {
  nil_chk(array);
  return ((IOSArray *)array)->size_;
}

static jboolean *GetBooleanArrayElements(JNIEnv *env, jbooleanArray array, jboolean *isCopy) {
  return GetPrimitiveArrayCritical(env, array, isCopy);
}

static void GetBooleanArrayRegion(JNIEnv *env, jbooleanArray array, jsize offset, jsize length,
    jboolean *buffer) {
  nil_chk(array);
  null_chk((void*)buffer);
  IOSBooleanArray_GetRange(buffer, (IOSBooleanArray *)array, offset, length);
}

static jbyte *GetByteArrayElements(JNIEnv *env, jbyteArray array, jboolean *isCopy) {
  return GetPrimitiveArrayCritical(env, array, isCopy);
}

static void GetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize offset, jsize length,
    jbyte *buffer) {
  nil_chk(array);
  null_chk((void*)buffer);
  IOSByteArray_GetRange(buffer, (IOSByteArray *)array, offset, length);
}

static jchar *GetCharArrayElements(JNIEnv *env, jcharArray array, jboolean *isCopy) {
  return GetPrimitiveArrayCritical(env, array, isCopy);
}

static void GetCharArrayRegion(JNIEnv *env, jcharArray array, jsize offset, jsize length,
    jchar *buffer) {
  nil_chk(array);
  null_chk((void*)buffer);
  IOSCharArray_GetRange(buffer, (IOSCharArray *)array, offset, length);
}

static void *GetDirectBufferAddress(JNIEnv *env, jobject buf) {
  nil_chk(buf);
  return (void *) ((JavaNioBuffer *) buf)->effectiveDirectAddress_;
}

static jlong GetDirectBufferCapacity(JNIEnv *env, jobject buf) {
  nil_chk(buf);
  return (jlong) ((JavaNioBuffer *) buf)->capacity_;
}

static jdouble *GetDoubleArrayElements(JNIEnv *env, jdoubleArray array, jboolean *isCopy) {
  return GetPrimitiveArrayCritical(env, array, isCopy);
}

static void GetDoubleArrayRegion(JNIEnv *env, jdoubleArray array, jsize offset, jsize length,
    jdouble *buffer) {
  nil_chk(array);
  null_chk((void*)buffer);
  IOSDoubleArray_GetRange(buffer, (IOSDoubleArray *)array, offset, length);
}

static jfloat *GetFloatArrayElements(JNIEnv *env, jfloatArray array, jboolean *isCopy) {
  return GetPrimitiveArrayCritical(env, array, isCopy);
}

static void GetFloatArrayRegion(JNIEnv *env, jfloatArray array, jsize offset, jsize length,
    jfloat *buffer) {
  nil_chk(array);
  null_chk((void*)buffer);
  IOSFloatArray_GetRange(buffer, (IOSFloatArray *)array, offset, length);
}

static jint *GetIntArrayElements(JNIEnv *env, jintArray array, jboolean *isCopy) {
  return GetPrimitiveArrayCritical(env, array, isCopy);
}

static void GetIntArrayRegion(JNIEnv *env, jintArray array, jsize offset, jsize length,
    jint *buffer) {
  nil_chk(array);
  null_chk((void*)buffer);
  IOSIntArray_GetRange(buffer, (IOSIntArray *)array, offset, length);
}

static jlong *GetLongArrayElements(JNIEnv *env, jlongArray array, jboolean *isCopy) {
  return GetPrimitiveArrayCritical(env, array, isCopy);
}

static void GetLongArrayRegion(JNIEnv *env, jlongArray array, jsize offset, jsize length,
    jlong *buffer) {
  nil_chk(array);
  null_chk((void*)buffer);
  IOSLongArray_GetRange(buffer, (IOSLongArray *)array, offset, length);
}

static jobject GetObjectArrayElement(JNIEnv *env, jobjectArray array, jsize index) {
  return IOSObjectArray_Get((IOSObjectArray *) array, index);
}

static jclass GetObjectClass(JNIEnv *env, jobject obj) {
  nil_chk(obj);
  return [(id<JavaObject>) obj getClass];
}

static void *GetPrimitiveArrayCritical(JNIEnv *env, jarray array, jboolean *isCopy) {
  nil_chk(array);
  if (isCopy) {
    *isCopy = NO;
  }
  // All primitive array types have buffer_ at same offset.
  return (void *) ((IOSByteArray *) array)->buffer_;
}

static jshort *GetShortArrayElements(JNIEnv *env, jshortArray array, jboolean *isCopy) {
  return GetPrimitiveArrayCritical(env, array, isCopy);
}

static void GetShortArrayRegion(JNIEnv *env, jshortArray array, jsize offset, jsize length,
    jshort *buffer) {
  nil_chk(array);
  null_chk((void*)buffer);
  IOSShortArray_GetRange(buffer, (IOSShortArray *)array, offset, length);
}

static const jchar *GetStringChars(JNIEnv *env, jstring s, jboolean *isCopy) {
  nil_chk(s);
  if (isCopy) {
    *isCopy = YES;
  }
  return [IOSCharArray arrayWithNSString:(NSString *) s]->buffer_;
}

static const jchar *GetStringCritical(JNIEnv *env, jstring s, jboolean *isCopy) {
  nil_chk(s);
  if (isCopy) {
    *isCopy = YES;
  }
  return [IOSCharArray arrayWithNSString:(NSString *) s]->buffer_;
}

static jsize GetStringLength(JNIEnv *env, jstring s) {
  nil_chk(s);
  return (jsize) [(NSString *) s length];
}

static void GetStringRegion(JNIEnv *env, jstring s, jsize offset, jsize length, jchar *buffer) {
  nil_chk(s);
  NSRange range = NSMakeRange(offset, length);
  [(NSString *) s getCharacters:(unichar *)buffer range:range];
}

static const char *GetStringUTFChars(JNIEnv *env, jstring s, jboolean *isCopy) {
  nil_chk(s);
  if (isCopy) {
    *isCopy = NO;
  }
  return ((NSString *) s).UTF8String;
}

static jsize GetStringUTFLength(JNIEnv *env, jstring s) {
  nil_chk(s);
  return (jsize) strlen(((NSString *) s).UTF8String);
}

static void GetStringUTFRegion(JNIEnv *env, jstring s, jsize offset, jsize length, char *buffer) {
  nil_chk(s);
  null_chk((void*)buffer);
  const char *utf = ((NSString *) s).UTF8String;
  memcpy(buffer, utf + offset, length);
}

static jclass GetSuperclass(JNIEnv *env, jclass clazz) {
  return [(IOSClass *) clazz getSuperclass];
}

static jint GetVersion(JNIEnv *env) {
  return JNI_VERSION_1_6;
}

static jboolean IsAssignableFrom(JNIEnv *env, jclass clazz1, jclass clazz2) {
  return [(IOSClass *) clazz2 isAssignableFrom:clazz1];
}

static jboolean IsInstanceOf(JNIEnv *env, jobject obj, jclass clazz) {
  return [(IOSClass *) clazz isInstance:obj];
}

static jobject NewGlobalRef(JNIEnv *env, jobject obj) {
  return obj;
}

static jobject NewLocalRef(JNIEnv *env, jobject obj) {
  return obj;
}

static void DeleteGlobalRef(JNIEnv *env, jobject globalRef) {
  // no-op
}

static void DeleteLocalRef(JNIEnv *env, jobject localRef) {
  // no-op
}

static jboolean IsSameObject(JNIEnv *env, jobject obj1, jobject obj2) {
  return obj1 == obj2;
}

static jbooleanArray NewBooleanArray(JNIEnv *env, jsize length) {
  return [IOSBooleanArray arrayWithLength:length];
}

static jbyteArray NewByteArray(JNIEnv *env, jsize length) {
  return [IOSByteArray arrayWithLength:length];
}

static jcharArray NewCharArray(JNIEnv *env, jsize length) {
  return [IOSCharArray arrayWithLength:length];
}

static jobject NewDirectByteBuffer(JNIEnv *env, void *address, jlong capacity) {
  return
      AUTORELEASE([[JavaNioDirectByteBuffer alloc] initWithLong:(jlong)address
                                                        withInt:(jint)capacity]);
}

static jdoubleArray NewDoubleArray(JNIEnv *env, jsize length) {
  return [IOSDoubleArray arrayWithLength:length];
}

static jfloatArray NewFloatArray(JNIEnv *env, jsize length) {
  return [IOSFloatArray arrayWithLength:length];
}

static jintArray NewIntArray(JNIEnv *env, jsize length) {
  return [IOSIntArray arrayWithLength:length];
}

static jlongArray NewLongArray(JNIEnv *env, jsize length) {
  return [IOSLongArray arrayWithLength:length];
}

static jobjectArray NewObjectArray(JNIEnv *env, jsize length, jclass clazz,
    jobject initialElement) {
  nil_chk(clazz);
  IOSObjectArray *result = [IOSObjectArray arrayWithLength:length type:(IOSClass *) clazz];
  if (initialElement) {
    for (jsize i = 0; i < length; i++) {
      IOSObjectArray_Set(result, i, initialElement);
    }
  }
  return result;
}

static jshortArray NewShortArray(JNIEnv *env, jsize length) {
  return [IOSShortArray arrayWithLength:length];
}

static jstring NewString(JNIEnv *env, const jchar *unicodeChars, jsize len) {
  null_chk((void*)unicodeChars);
  return [NSString stringWithCharacters:unicodeChars length:len];
}

static jstring NewStringUTF(JNIEnv *env, const char *bytes) {
  null_chk((void*)bytes);
  return [NSString stringWithUTF8String:bytes];
}

static void ReleaseBooleanArrayElements(
    JNIEnv *env, jbooleanArray array, jboolean *elems, jint mode) {
  // no-op
}

static void ReleaseByteArrayElements(JNIEnv *env, jbyteArray array, jbyte *elems, jint mode) {
  // no-op
}

static void ReleaseCharArrayElements(JNIEnv *env, jcharArray array, jchar *elems, jint mode) {
  // no-op
}

static void ReleaseDoubleArrayElements(JNIEnv *env, jdoubleArray array, jdouble *elems, jint mode) {
  // no-op
}

static void ReleaseFloatArrayElements(JNIEnv *env, jfloatArray array, jfloat *elems, jint mode) {
  // no-op
}

static void ReleaseIntArrayElements(JNIEnv *env, jintArray array, jint *elems, jint mode) {
  // no-op
}

static void ReleaseLongArrayElements(JNIEnv *env, jlongArray array, jlong *elems, jint mode) {
  // no-op
}

static void ReleasePrimitiveArrayCritical(JNIEnv *env, jarray array, void *carray, jint mode) {
  // no-op
}

static void ReleaseShortArrayElements(JNIEnv *env, jshortArray array, jshort *elems, jint mode) {
  // no-op
}

static void ReleaseStringChars(JNIEnv *env, jstring string, const jchar *chars) {
  // no-op
}

static void ReleaseStringCritical(JNIEnv *env, jstring s, const jchar *buffer) {
  // no-op
}

static void ReleaseStringUTFChars(JNIEnv *env, jstring string, const char *utf) {
  // no-op
}

static void SetBooleanArrayRegion(JNIEnv *env, jbooleanArray array, jsize offset, jsize length,
    const jboolean *buffer) {
  nil_chk(array);
  null_chk((void*)buffer);
  IOSBooleanArray_SetRange((IOSBooleanArray *)array, buffer, offset, length);
}

static void SetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize offset, jsize length,
    const jbyte *buffer) {
  nil_chk(array);
  null_chk((void*)buffer);
  IOSByteArray_SetRange((IOSByteArray *)array, buffer, offset, length);
}

static void SetCharArrayRegion(JNIEnv *env, jcharArray array, jsize offset, jsize length,
    const jchar *buffer) {
  nil_chk(array);
  null_chk((void*)buffer);
  IOSCharArray_SetRange((IOSCharArray *)array, buffer, offset, length);
}

static void SetDoubleArrayRegion(JNIEnv *env, jdoubleArray array, jsize offset, jsize length,
    const jdouble *buffer) {
  nil_chk(array);
  null_chk((void*)buffer);
  IOSDoubleArray_SetRange((IOSDoubleArray *)array, buffer, offset, length);
}

static void SetFloatArrayRegion(JNIEnv *env, jfloatArray array, jsize offset, jsize length,
    const jfloat *buffer) {
  nil_chk(array);
  null_chk((void*)buffer);
  IOSFloatArray_SetRange((IOSFloatArray *)array, buffer, offset, length);
}

static void SetIntArrayRegion(JNIEnv *env, jintArray array, jsize offset, jsize length,
    const jint *buffer) {
  nil_chk(array);
  null_chk((void*)buffer);
  IOSIntArray_SetRange((IOSIntArray *)array, buffer, offset, length);
}

static void SetLongArrayRegion(JNIEnv *env, jlongArray array, jsize offset, jsize length,
    const jlong *buffer) {
  nil_chk(array);
  null_chk((void*)buffer);
  IOSLongArray_SetRange((IOSLongArray *)array, buffer, offset, length);
}

static void SetObjectArrayElement(JNIEnv *env, jobjectArray array, jsize index, jobject value) {
  IOSObjectArray_Set((IOSObjectArray *) array, index, value);
}

static void SetShortArrayRegion(JNIEnv *env, jshortArray array, jsize offset, jsize length,
    const jshort *buffer) {
  nil_chk(array);
  null_chk((void*)(void*)buffer);
  IOSShortArray_SetRange((IOSShortArray *)array, buffer, offset, length);
}

static jint Throw(JNIEnv *env, jthrowable obj) {
  nil_chk(obj);
  @throw obj;
  return 0;
}

static jint ThrowNew(JNIEnv *env, jclass clazz, const char *message) {
  nil_chk(clazz);
  NSString *msg = [NSString stringWithUTF8String:message];
  id exc = [(JavaLangThrowable *) [((IOSClass *) clazz).objcClass alloc] initWithNSString:msg];
  @throw AUTORELEASE(exc);
  return 0;
}


static struct JNINativeInterface JNI_JNIEnvTable = {
  &GetVersion,
  &FindClass,
  &GetSuperclass,
  &IsAssignableFrom,
  &Throw,
  &ThrowNew,
  &NewGlobalRef,
  &NewLocalRef,
  &DeleteGlobalRef,
  &DeleteLocalRef,
  &IsSameObject,
  &GetObjectClass,
  &IsInstanceOf,
  &NewString,
  &GetStringLength,
  &GetStringChars,
  &ReleaseStringChars,
  &NewStringUTF,
  &GetStringUTFLength,
  &GetStringUTFChars,
  &ReleaseStringUTFChars,
  &GetArrayLength,
  &NewObjectArray,
  &GetObjectArrayElement,
  &SetObjectArrayElement,
  &NewBooleanArray,
  &NewByteArray,
  &NewCharArray,
  &NewShortArray,
  &NewIntArray,
  &NewLongArray,
  &NewFloatArray,
  &NewDoubleArray,
  &GetBooleanArrayElements,
  &GetByteArrayElements,
  &GetCharArrayElements,
  &GetShortArrayElements,
  &GetIntArrayElements,
  GetLongArrayElements,
  &GetFloatArrayElements,
  &GetDoubleArrayElements,
  &ReleaseBooleanArrayElements,
  &ReleaseByteArrayElements,
  &ReleaseCharArrayElements,
  &ReleaseShortArrayElements,
  &ReleaseIntArrayElements,
  &ReleaseLongArrayElements,
  &ReleaseFloatArrayElements,
  &ReleaseDoubleArrayElements,
  &GetBooleanArrayRegion,
  &GetByteArrayRegion,
  &GetCharArrayRegion,
  &GetShortArrayRegion,
  &GetIntArrayRegion,
  &GetLongArrayRegion,
  &GetFloatArrayRegion,
  &GetDoubleArrayRegion,
  &SetBooleanArrayRegion,
  &SetByteArrayRegion,
  &SetCharArrayRegion,
  &SetShortArrayRegion,
  &SetIntArrayRegion,
  &SetLongArrayRegion,
  &SetFloatArrayRegion,
  &SetDoubleArrayRegion,
  &GetStringRegion,
  &GetStringUTFRegion,
  &GetPrimitiveArrayCritical,
  &ReleasePrimitiveArrayCritical,
  &GetStringCritical,
  &ReleaseStringCritical,
  &NewDirectByteBuffer,
  &GetDirectBufferAddress,
  &GetDirectBufferCapacity,
};

C_JNIEnv J2ObjC_JNIEnv = &JNI_JNIEnvTable;
