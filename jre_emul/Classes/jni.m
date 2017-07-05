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
#include "java/lang/ClassNotFoundException.h"
#include "java/lang/InstantiationException.h"
#include "java/lang/Throwable.h"
#include "java/lang/reflect/Constructor.h"
#include "java/lang/reflect/Field.h"
#include "java/lang/reflect/Method.h"
#include "java/lang/reflect/Modifier.h"
#include "java/nio/Buffer.h"
#include "java/nio/DirectByteBuffer.h"

static IOSClass *IOSClass_forName(const char *name) {
  NSString *nameString = [NSString stringWithUTF8String:name];
  nameString = [nameString stringByReplacingOccurrencesOfString:@"/" withString:@"."];
  return [IOSClass forName:nameString];
}

typedef struct _JNIMethodSignature {
  IOSClass *returnType;
  IOSObjectArray *paramTypes;
} JNIMethodSignature;

static IOSClass *JNIParseTypeSignature(const char *sig, const char **next) {
  IOSClass *result = nil;
  *next = sig + 1;
  char c = *sig;
  int array_dim = 0;
  while (c == '[') {
    array_dim++;
    sig++;
    c = *sig;
  }
  if (array_dim) {
    IOSClass *componentType = JNIParseTypeSignature(sig, next);
    result = IOSClass_arrayType(componentType, array_dim);
  } else {
    if (c == 'L') {
      const char *end = strchr(sig + 1, ';');
      if (end) {
        const char *begin = sig + 1;
        size_t length = end - begin;
        char *buffer = malloc(length + 1);
        strncpy(buffer, begin, length);
        buffer[length] = 0;
        result = IOSClass_forName(buffer);
        free(buffer);
        *next = end + 1;
      }
    } else {
      result = [IOSClass primitiveClassForChar:c];
    }
  }
  if (!result) {
    @throw AUTORELEASE([[JavaLangClassNotFoundException alloc]
        initWithNSString:[NSString stringWithUTF8String:sig]]);
  }
  return result;
}

JNIMethodSignature JNIParseMethodSignature(const char *sig) {
  JNIMethodSignature result;
  result.returnType = nil;
  result.paramTypes = nil;
  const char *p = sig;
  if (*p != '(') {
    return result;
  }
  p++;
  NSMutableArray *paramTypes = [NSMutableArray array];
  while (*p != ')') {
    id paramType = JNIParseTypeSignature(p, &p);
    [paramTypes addObject:paramType];
  }
  result.paramTypes = [IOSObjectArray arrayWithLength:paramTypes.count type:IOSClass_class_()];
  for (NSUInteger i = 0; i < paramTypes.count; i++) {
    [result.paramTypes replaceObjectAtIndex:i withObject:paramTypes[i]];
  }
  p++;
  result.returnType = JNIParseTypeSignature(p, &p);
  return result;
}

NSString *JNIFormatMethodSignature(JNIMethodSignature sig) {
  NSString *result = [sig.returnType getName];
  result = [result stringByAppendingString:@"("];
  for (jint i = 0; i < sig.paramTypes.length; i++) {
    IOSClass *paramType = [sig.paramTypes objectAtIndex:i];
    NSString *paramTypeString = [paramType getName];
    result = [result stringByAppendingString:paramTypeString];
    if (i < sig.paramTypes.length - 1) {
      result = [result stringByAppendingString:@", "];
    }
  }
  result = [result stringByAppendingString:@")"];
  return result;
}

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
  return IOSClass_forName(name);
}

static jsize GetArrayLength(JNIEnv *env, jarray array) {
  nil_chk(array);
  return ((IOSArray *)array)->size_;
}

static jboolean *GetBooleanArrayElements(JNIEnv *env, jbooleanArray array, jboolean *isCopy) {
  return GetPrimitiveArrayCritical(env, array, isCopy);
}

static jbyte *GetByteArrayElements(JNIEnv *env, jbyteArray array, jboolean *isCopy) {
  return GetPrimitiveArrayCritical(env, array, isCopy);
}

static jchar *GetCharArrayElements(JNIEnv *env, jcharArray array, jboolean *isCopy) {
  return GetPrimitiveArrayCritical(env, array, isCopy);
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

static jfloat *GetFloatArrayElements(JNIEnv *env, jfloatArray array, jboolean *isCopy) {
  return GetPrimitiveArrayCritical(env, array, isCopy);
}

static jint *GetIntArrayElements(JNIEnv *env, jintArray array, jboolean *isCopy) {
  return GetPrimitiveArrayCritical(env, array, isCopy);
}

static jlong *GetLongArrayElements(JNIEnv *env, jlongArray array, jboolean *isCopy) {
  return GetPrimitiveArrayCritical(env, array, isCopy);
}

static jobject GetObjectArrayElement(JNIEnv *env, jobjectArray array, jsize index) {
  return IOSObjectArray_Get((IOSObjectArray *) array, index);
}

static jclass GetObjectClass(JNIEnv *env, jobject obj) {
  nil_chk(obj);
  return [(id<JavaObject>) obj java_getClass];
}

static void *GetPrimitiveArrayCritical(JNIEnv *env, jarray array, jboolean *isCopy) {
  nil_chk(array);
  if (isCopy) {
    *isCopy = false;
  }
  // All primitive array types have buffer_ at same offset.
  return (void *) ((IOSByteArray *) array)->buffer_;
}

static jshort *GetShortArrayElements(JNIEnv *env, jshortArray array, jboolean *isCopy) {
  return GetPrimitiveArrayCritical(env, array, isCopy);
}

static const jchar *GetStringChars(JNIEnv *env, jstring s, jboolean *isCopy) {
  nil_chk(s);
  if (isCopy) {
    *isCopy = true;
  }
  return [IOSCharArray arrayWithNSString:(NSString *) s]->buffer_;
}

static const jchar *GetStringCritical(JNIEnv *env, jstring s, jboolean *isCopy) {
  nil_chk(s);
  if (isCopy) {
    *isCopy = true;
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
    *isCopy = false;
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
  return [(__bridge id)obj retain];
}

static jobject NewLocalRef(JNIEnv *env, jobject obj) {
  return obj;  // no-op
}

static void DeleteGlobalRef(JNIEnv *env, jobject globalRef) {
  [(__bridge id)globalRef autorelease];
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

static void SetObjectArrayElement(JNIEnv *env, jobjectArray array, jsize index, jobject value) {
  IOSObjectArray_Set((IOSObjectArray *) array, index, value);
}

#define GET_ARRAY_REGION_IMPL(TYPE_NAME, JNI_TYPE) \
  static void Get##TYPE_NAME##ArrayRegion( \
      JNIEnv *env, JNI_TYPE##Array array, jsize offset, jsize length, JNI_TYPE *buffer) { \
    nil_chk(array); \
    null_chk((void*)buffer); \
    IOSArray_checkRange(array->size_, offset, length); \
    memcpy(buffer, array->buffer_ + offset, length * sizeof(JNI_TYPE)); \
  }

GET_ARRAY_REGION_IMPL(Boolean, jboolean)
GET_ARRAY_REGION_IMPL(Byte, jbyte)
GET_ARRAY_REGION_IMPL(Char, jchar)
GET_ARRAY_REGION_IMPL(Short, jshort)
GET_ARRAY_REGION_IMPL(Int, jint)
GET_ARRAY_REGION_IMPL(Long, jlong)
GET_ARRAY_REGION_IMPL(Float, jfloat)
GET_ARRAY_REGION_IMPL(Double, jdouble)

#undef GET_ARRAY_REGION_IMPL

#define SET_ARRAY_REGION_IMPL(TYPE_NAME, JNI_TYPE) \
  static void Set##TYPE_NAME##ArrayRegion( \
      JNIEnv *env, JNI_TYPE##Array array, jsize offset, jsize length, const JNI_TYPE *buffer) { \
    nil_chk(array); \
    null_chk((void*)buffer); \
    IOSArray_checkRange(array->size_, offset, length); \
    memcpy(array->buffer_ + offset, buffer, length * sizeof(JNI_TYPE)); \
  }

SET_ARRAY_REGION_IMPL(Boolean, jboolean)
SET_ARRAY_REGION_IMPL(Byte, jbyte)
SET_ARRAY_REGION_IMPL(Char, jchar)
SET_ARRAY_REGION_IMPL(Short, jshort)
SET_ARRAY_REGION_IMPL(Int, jint)
SET_ARRAY_REGION_IMPL(Long, jlong)
SET_ARRAY_REGION_IMPL(Float, jfloat)
SET_ARRAY_REGION_IMPL(Double, jdouble)

#undef SET_ARRAY_REGION_IMPL

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

static void ExceptionClear(JNIEnv *env) {
  // no-op
}

static jfieldID GetFieldID(JNIEnv *env, jclass clazz, const char *name, const char *sig) {
  IOSClass *iosClass = (IOSClass *) clazz;
  JavaLangReflectField *field = [iosClass getDeclaredField:[NSString stringWithUTF8String:name]];
  return (jfieldID) field;
}

static jfieldID GetStaticFieldID(JNIEnv *env, jclass clazz, const char *name, const char *sig) {
  return GetFieldID(env, clazz, name, sig);
}

static jmethodID GetMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig) {
  IOSClass *iosClass = (IOSClass *) clazz;
  JNIMethodSignature methodSig = JNIParseMethodSignature(sig);
  JavaLangReflectExecutable *result = nil;
  if (strcmp(name, "<init>") == 0) {
    result = [iosClass getConstructor:methodSig.paramTypes];
  } else {
    result = [iosClass getDeclaredMethod:[NSString stringWithUTF8String:name]
                          parameterTypes:methodSig.paramTypes];
  }
  return (jmethodID) result;
}

static jmethodID GetStaticMethodID(JNIEnv *env, jclass clazz, const char *name, const char *sig) {
  return GetMethodID(env, clazz, name, sig);
}

#define ALLOC_JARGS(JARGS, NUM_ARGS)           \
  const size_t _max_stack_args = 16;           \
  jvalue _stack_args[_max_stack_args];         \
  jvalue *JARGS;                               \
  jboolean _free_jargs = false;                \
  if (NUM_ARGS <= _max_stack_args) {           \
    JARGS = _stack_args;                       \
  } else {                                     \
    JARGS = malloc(NUM_ARGS * sizeof(jvalue)); \
    _free_jargs = true;                        \
  }

#define DEALLOC_JARGS(JARGS) \
  if (_free_jargs) {         \
    free(JARGS);             \
  }

#define FORWARD_VARGS(RESULT_TYPE, METHOD_CALL) \
  RESULT_TYPE result;                           \
  va_list args;                                 \
  va_start(args, methodID);                     \
  result = METHOD_CALL;                         \
  va_end(args);                                 \
  return result

static void ToArgsArray(IOSObjectArray *paramTypes, jvalue *jargs, va_list args) {
  jvalue *value = jargs;
  for (IOSClass *param in paramTypes) {
    unichar p = [param isPrimitive] ? [[param binaryName] characterAtIndex:0] : 'L';
    switch (p) {
      // On 32 bit architectures, each var arg size is promoted to at least
      // sizeof(int) for integral types, or sizeof(double) for float types.
      // TODO: verify this works for 64 bit architectures.
      case 'B': value->b = (jbyte) va_arg(args, int); break;
      case 'C': value->c = (jchar) va_arg(args, unsigned int); break;
      case 'S': value->s = (jshort) va_arg(args, int); break;
      case 'I': value->i = (jint) va_arg(args, int); break;
      case 'J': value->j = (jlong) va_arg(args, jlong); break;
      case 'F': value->f = (jfloat) va_arg(args, double); break;
      case 'D': value->d = (jdouble) va_arg(args, double); break;
      case 'Z': value->z = (jboolean) va_arg(args, int); break;
      default: value->l = (jobject) va_arg(args, jobject); break;
    }
    value++;
  }
}

static jobject AllocObject(JNIEnv *env, jclass clazz) {
  nil_chk(clazz);
  jint modifiers = [clazz getModifiers];
  if ((modifiers & (JavaLangReflectModifier_ABSTRACT | JavaLangReflectModifier_INTERFACE)) > 0
      || [clazz isArray] || [clazz isEnum]) {
    @throw create_JavaLangInstantiationException_initWithNSString_([clazz getName]);
  }
  return [[clazz.objcClass alloc] autorelease];
}

static jobject NewObjectA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
  return (jobject) [(JavaLangReflectConstructor *)methodID
      jniNewInstance:(const J2ObjcRawValue *)args];
}

static jobject NewObjectV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
  IOSObjectArray *paramTypes = [(JavaLangReflectConstructor *)methodID getParameterTypesInternal];
  size_t numArgs = paramTypes->size_;

  ALLOC_JARGS(jargs, numArgs);
  ToArgsArray(paramTypes, jargs, args);
  jobject result = NewObjectA(env, clazz, methodID, jargs);
  DEALLOC_JARGS(jargs);

  return result;
}

static jobject NewObject(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
  FORWARD_VARGS(jobject, NewObjectV(env, clazz, methodID, args));
}

static void CallMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args, jvalue *result) {
  [(JavaLangReflectMethod *)methodID
      jniInvokeWithId:obj args:(const J2ObjcRawValue *)args result:(J2ObjcRawValue *)result];
}

static void CallMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args, jvalue *result) {
  IOSObjectArray *paramTypes = [(JavaLangReflectMethod *)methodID getParameterTypesInternal];
  size_t numArgs = paramTypes->size_;

  ALLOC_JARGS(jargs, numArgs);
  ToArgsArray(paramTypes, jargs, args);
  CallMethodA(env, obj, methodID, jargs, result);
  DEALLOC_JARGS(jargs);
}

#define DEFINE_CALL_METHOD_VARIANTS(RESULT_NAME, RESULT_TYPE, RESULT_CODE) \
  RESULT_TYPE Call##RESULT_NAME##MethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) { \
    jvalue result; \
    CallMethodV(env, obj, methodID, args, &result); \
    return result.RESULT_CODE; \
  } \
  RESULT_TYPE Call##RESULT_NAME##MethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args) { \
    jvalue result; \
    CallMethodA(env, obj, methodID, args, &result); \
    return result.RESULT_CODE; \
  } \
  RESULT_TYPE Call##RESULT_NAME##Method(JNIEnv *env, jobject obj, jmethodID methodID, ...) { \
    FORWARD_VARGS(RESULT_TYPE, Call##RESULT_NAME##MethodV(env, obj, methodID, args)); \
  }

DEFINE_CALL_METHOD_VARIANTS(Object, jobject, l)
DEFINE_CALL_METHOD_VARIANTS(Boolean, jboolean, z)
DEFINE_CALL_METHOD_VARIANTS(Byte, jbyte, b)
DEFINE_CALL_METHOD_VARIANTS(Char, jchar, c)
DEFINE_CALL_METHOD_VARIANTS(Short, jshort, s)
DEFINE_CALL_METHOD_VARIANTS(Int, jint, i)
DEFINE_CALL_METHOD_VARIANTS(Long, jlong, j)
DEFINE_CALL_METHOD_VARIANTS(Float, jfloat, f)
DEFINE_CALL_METHOD_VARIANTS(Double, jdouble, d)

void CallVoidMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {
  CallMethodV(env, obj, methodID, args, NULL);
}

void CallVoidMethodA(JNIEnv *env, jobject obj, jmethodID methodID, const jvalue *args) {
  CallMethodA(env, obj, methodID, args, NULL);
}

void CallVoidMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {
  va_list args;
  va_start(args, methodID);
  CallVoidMethodV(env, obj, methodID, args);
  va_end(args);
}

jobject GetObjectField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getWithId:obj];
}

jboolean GetBooleanField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getBooleanWithId:obj];
}

jbyte GetByteField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getByteWithId:obj];
}

jchar GetCharField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getCharWithId:obj];
}

jshort GetShortField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getShortWithId:obj];
}

jint GetIntField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getIntWithId:obj];
}

jlong GetLongField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getLongWithId:obj];
}

jfloat GetFloatField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getFloatWithId:obj];
}

jdouble GetDoubleField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getDoubleWithId:obj];
}

void SetObjectField(JNIEnv *env, jobject obj, jfieldID fieldID, jobject value) {
  [(JavaLangReflectField *)fieldID setWithId:obj withId:value];
}

void SetBooleanField(JNIEnv *env, jobject obj, jfieldID fieldID, jboolean value) {
  [(JavaLangReflectField *)fieldID setBooleanWithId:obj withBoolean:value];
}

void SetByteField(JNIEnv *env, jobject obj, jfieldID fieldID, jbyte value) {
  [(JavaLangReflectField *)fieldID setByteWithId:obj withByte:value];
}

void SetCharField(JNIEnv *env, jobject obj, jfieldID fieldID, jchar value) {
  [(JavaLangReflectField *)fieldID setCharWithId:obj withChar:value];
}

void SetShortField(JNIEnv *env, jobject obj, jfieldID fieldID, jshort value) {
  [(JavaLangReflectField *)fieldID setShortWithId:obj withShort:value];
}

void SetIntField(JNIEnv *env, jobject obj, jfieldID fieldID, jint value) {
  [(JavaLangReflectField *)fieldID setIntWithId:obj withInt:value];
}

void SetLongField(JNIEnv *env, jobject obj, jfieldID fieldID, jlong value) {
  [(JavaLangReflectField *)fieldID setLongWithId:obj withLong:value];
}

void SetFloatField(JNIEnv *env, jobject obj, jfieldID fieldID, jfloat value) {
  [(JavaLangReflectField *)fieldID setFloatWithId:obj withFloat:value];
}

void SetDoubleField(JNIEnv *env, jobject obj, jfieldID fieldID, jdouble value) {
  [(JavaLangReflectField *)fieldID setDoubleWithId:obj withDouble:value];
}

#define DEFINE_CALL_STATIC_METHOD_VARIANTS(RESULT_NAME, RESULT_TYPE, RESULT_CODE) \
  RESULT_TYPE CallStatic##RESULT_NAME##MethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) { \
    jvalue result; \
    CallMethodV(env, nil, methodID, args, &result); \
    return result.RESULT_CODE; \
  } \
  RESULT_TYPE CallStatic##RESULT_NAME##MethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) { \
    jvalue result; \
    CallMethodA(env, nil, methodID, args, &result); \
    return result.RESULT_CODE; \
  } \
  RESULT_TYPE CallStatic##RESULT_NAME##Method(JNIEnv *env, jclass clazz, jmethodID methodID, ...) { \
    FORWARD_VARGS(RESULT_TYPE, Call##RESULT_NAME##MethodV(env, nil, methodID, args)); \
  }

DEFINE_CALL_STATIC_METHOD_VARIANTS(Object, jobject, l)
DEFINE_CALL_STATIC_METHOD_VARIANTS(Boolean, jboolean, z)
DEFINE_CALL_STATIC_METHOD_VARIANTS(Byte, jbyte, b)
DEFINE_CALL_STATIC_METHOD_VARIANTS(Char, jchar, c)
DEFINE_CALL_STATIC_METHOD_VARIANTS(Short, jshort, s)
DEFINE_CALL_STATIC_METHOD_VARIANTS(Int, jint, i)
DEFINE_CALL_STATIC_METHOD_VARIANTS(Long, jlong, j)
DEFINE_CALL_STATIC_METHOD_VARIANTS(Float, jfloat, f)
DEFINE_CALL_STATIC_METHOD_VARIANTS(Double, jdouble, d)

void CallStaticVoidMethodV(JNIEnv *env, jclass clazz, jmethodID methodID, va_list args) {
  CallMethodV(env, nil, methodID, args, NULL);
}

void CallStaticVoidMethodA(JNIEnv *env, jclass clazz, jmethodID methodID, const jvalue *args) {
  CallMethodA(env, nil, methodID, args, NULL);
}

void CallStaticVoidMethod(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
  va_list args;
  va_start(args, methodID);
  CallStaticVoidMethodV(env, nil, methodID, args);
  va_end(args);
}

jobject GetStaticObjectField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getWithId:nil];
}

jboolean GetStaticBooleanField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getBooleanWithId:nil];
}

jbyte GetStaticByteField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getByteWithId:nil];
}

jchar GetStaticCharField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getCharWithId:nil];
}

jshort GetStaticShortField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getShortWithId:nil];
}

jint GetStaticIntField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getIntWithId:nil];
}

jlong GetStaticLongField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getLongWithId:nil];
}

jfloat GetStaticFloatField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getFloatWithId:nil];
}

jdouble GetStaticDoubleField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
  return [(JavaLangReflectField *)fieldID getDoubleWithId:nil];
}

void SetStaticObjectField(JNIEnv *env, jclass clazz, jfieldID fieldID, jobject value) {
  [(JavaLangReflectField *)fieldID setWithId:nil withId:value];
}

void SetStaticBooleanField(JNIEnv *env, jclass clazz, jfieldID fieldID, jboolean value) {
  [(JavaLangReflectField *)fieldID setBooleanWithId:nil withBoolean:value];
}

void SetStaticByteField(JNIEnv *env, jclass clazz, jfieldID fieldID, jbyte value) {
  [(JavaLangReflectField *)fieldID setByteWithId:nil withByte:value];
}

void SetStaticCharField(JNIEnv *env, jclass clazz, jfieldID fieldID, jchar value) {
  [(JavaLangReflectField *)fieldID setCharWithId:nil withChar:value];
}

void SetStaticShortField(JNIEnv *env, jclass clazz, jfieldID fieldID, jshort value) {
  [(JavaLangReflectField *)fieldID setShortWithId:nil withShort:value];
}

void SetStaticIntField(JNIEnv *env, jclass clazz, jfieldID fieldID, jint value) {
  [(JavaLangReflectField *)fieldID setIntWithId:nil withInt:value];
}

void SetStaticLongField(JNIEnv *env, jclass clazz, jfieldID fieldID, jlong value) {
  [(JavaLangReflectField *)fieldID setLongWithId:nil withLong:value];
}

void SetStaticFloatField(JNIEnv *env, jclass clazz, jfieldID fieldID, jfloat value) {
  [(JavaLangReflectField *)fieldID setFloatWithId:nil withFloat:value];
}

void SetStaticDoubleField(JNIEnv *env, jclass clazz, jfieldID fieldID, jdouble value) {
  [(JavaLangReflectField *)fieldID setDoubleWithId:nil withDouble:value];
}

static jint GetJavaVM(JNIEnv *env, JavaVM **vm);

static struct JNINativeInterface JNI_JNIEnvTable = {
  &GetVersion,
  &FindClass,
  &GetSuperclass,
  &IsAssignableFrom,
  &Throw,
  &ThrowNew,
  &ExceptionClear,
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
  &GetFieldID,
  &GetStaticFieldID,
  &GetMethodID,
  &GetStaticMethodID,
  &AllocObject,
  &NewObject,
  &NewObjectV,
  &NewObjectA,
  &CallObjectMethod,
  &CallObjectMethodV,
  &CallObjectMethodA,
  &CallBooleanMethod,
  &CallBooleanMethodV,
  &CallBooleanMethodA,
  &CallByteMethod,
  &CallByteMethodV,
  &CallByteMethodA,
  &CallCharMethod,
  &CallCharMethodV,
  &CallCharMethodA,
  &CallShortMethod,
  &CallShortMethodV,
  &CallShortMethodA,
  &CallIntMethod,
  &CallIntMethodV,
  &CallIntMethodA,
  &CallLongMethod,
  &CallLongMethodV,
  &CallLongMethodA,
  &CallFloatMethod,
  &CallFloatMethodV,
  &CallFloatMethodA,
  &CallDoubleMethod,
  &CallDoubleMethodV,
  &CallDoubleMethodA,
  &CallVoidMethod,
  &CallVoidMethodV,
  &CallVoidMethodA,
  &GetObjectField,
  &GetBooleanField,
  &GetByteField,
  &GetCharField,
  &GetShortField,
  &GetIntField,
  &GetLongField,
  &GetFloatField,
  &GetDoubleField,
  &SetObjectField,
  &SetBooleanField,
  &SetByteField,
  &SetCharField,
  &SetShortField,
  &SetIntField,
  &SetLongField,
  &SetFloatField,
  &SetDoubleField,
  &CallStaticObjectMethod,
  &CallStaticObjectMethodV,
  &CallStaticObjectMethodA,
  &CallStaticBooleanMethod,
  &CallStaticBooleanMethodV,
  &CallStaticBooleanMethodA,
  &CallStaticByteMethod,
  &CallStaticByteMethodV,
  &CallStaticByteMethodA,
  &CallStaticCharMethod,
  &CallStaticCharMethodV,
  &CallStaticCharMethodA,
  &CallStaticShortMethod,
  &CallStaticShortMethodV,
  &CallStaticShortMethodA,
  &CallStaticIntMethod,
  &CallStaticIntMethodV,
  &CallStaticIntMethodA,
  &CallStaticLongMethod,
  &CallStaticLongMethodV,
  &CallStaticLongMethodA,
  &CallStaticFloatMethod,
  &CallStaticFloatMethodV,
  &CallStaticFloatMethodA,
  &CallStaticDoubleMethod,
  &CallStaticDoubleMethodV,
  &CallStaticDoubleMethodA,
  &CallStaticVoidMethod,
  &CallStaticVoidMethodV,
  &CallStaticVoidMethodA,
  &GetStaticObjectField,
  &GetStaticBooleanField,
  &GetStaticByteField,
  &GetStaticCharField,
  &GetStaticShortField,
  &GetStaticIntField,
  &GetStaticLongField,
  &GetStaticFloatField,
  &GetStaticDoubleField,
  &SetStaticObjectField,
  &SetStaticBooleanField,
  &SetStaticByteField,
  &SetStaticCharField,
  &SetStaticShortField,
  &SetStaticIntField,
  &SetStaticLongField,
  &SetStaticFloatField,
  &SetStaticDoubleField,
  &GetJavaVM,
};

C_JNIEnv J2ObjC_JNIEnv = &JNI_JNIEnvTable;

static jint GetEnv(JavaVM *vm, void **penv, jint version);

static jint DestroyJavaVM(JavaVM *vm) {
  return JNI_OK;
}

static jint AttachCurrentThread(JavaVM *vm, void **penv, void *args) {
  GetEnv(vm, penv, 0);
  return JNI_OK;
}

static jint DetachCurrentThread(JavaVM *vm) {
  return JNI_OK;
}

static jint GetEnv(JavaVM *vm, void **penv, jint version) {
  static JNIEnv *env_ = NULL;
  if (!env_) {
    env_ = (JNIEnv *) malloc(sizeof(JNIEnv));
    *env_ = J2ObjC_JNIEnv;
  }
  JNIEnv **result = (JNIEnv **) penv;
  *result = env_;
  return JNI_OK;
}

static jint AttachCurrentThreadAsDaemon(JavaVM *vm, void **penv, void *args) {
  GetEnv(vm, penv, 0);
  return JNI_OK;
}

static struct JNIInvokeInterface JNI_JavaVMTable = {
  &DestroyJavaVM,
  &AttachCurrentThread,
  &DetachCurrentThread,
  &GetEnv,
  &AttachCurrentThreadAsDaemon,
};

C_JavaVM J2ObjC_JavaVM = &JNI_JavaVMTable;

static jint GetJavaVM(JNIEnv *env, JavaVM **vm) {
  static JavaVM *jvm_ = NULL;
  if (!jvm_) {
    jvm_ = (JavaVM *) malloc(sizeof(JavaVM));
    *jvm_ = J2ObjC_JavaVM;
  }
  *vm = jvm_;
  return JNI_OK;
}
