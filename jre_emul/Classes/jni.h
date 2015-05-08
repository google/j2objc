/*
 * Copyright (C) 2006 The Android Open Source Project
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

/*
 * JNI specification, as defined by Sun:
 * http://java.sun.com/javase/6/docs/technotes/guides/jni/spec/jniTOC.html
 *
 * Everything here is expected to be VM-neutral.
 */

#ifndef JNI_H_
#define JNI_H_

#include "J2ObjC_common.h"

/* "cardinal indices and sizes" */
typedef jint            jsize;

#ifdef __cplusplus
/*
 * Reference types, in C++
 */
class _jobject {};
class _jclass : public _jobject {};
class _jstring : public _jobject {};
class _jarray : public _jobject {};
class _jobjectArray : public _jarray {};
class _jbooleanArray : public _jarray {};
class _jbyteArray : public _jarray {};
class _jcharArray : public _jarray {};
class _jshortArray : public _jarray {};
class _jintArray : public _jarray {};
class _jlongArray : public _jarray {};
class _jfloatArray : public _jarray {};
class _jdoubleArray : public _jarray {};
class _jthrowable : public _jobject {};

typedef _jobject*       jobject;
typedef _jclass*        jclass;
typedef _jstring*       jstring;
typedef _jarray*        jarray;
typedef _jobjectArray*  jobjectArray;
typedef _jbooleanArray* jbooleanArray;
typedef _jbyteArray*    jbyteArray;
typedef _jcharArray*    jcharArray;
typedef _jshortArray*   jshortArray;
typedef _jintArray*     jintArray;
typedef _jlongArray*    jlongArray;
typedef _jfloatArray*   jfloatArray;
typedef _jdoubleArray*  jdoubleArray;
typedef _jthrowable*    jthrowable;
typedef _jobject*       jweak;


#else /* not __cplusplus */

/*
 * Reference types, in C.
 */
typedef void*           jobject;
typedef jobject         jclass;
typedef jobject         jstring;
typedef jobject         jarray;
typedef jarray          jobjectArray;
typedef jarray          jbooleanArray;
typedef jarray          jbyteArray;
typedef jarray          jcharArray;
typedef jarray          jshortArray;
typedef jarray          jintArray;
typedef jarray          jlongArray;
typedef jarray          jfloatArray;
typedef jarray          jdoubleArray;
typedef jobject         jthrowable;
typedef jobject         jweak;

#endif /* not __cplusplus */

typedef struct {
    const char* name;
    const char* signature;
    void*       fnPtr;
} JNINativeMethod;

struct _JNIEnv;
typedef const struct JNINativeInterface* C_JNIEnv;
extern C_JNIEnv J2ObjC_JNIEnv;

#if defined(__cplusplus)
typedef _JNIEnv JNIEnv;
#else
typedef const struct JNINativeInterface* JNIEnv;
#endif

/*
 * Table of interface function pointers.
 */
struct JNINativeInterface {
  jint          (*GetVersion)(JNIEnv *);

  jclass        (*FindClass)(JNIEnv*, const char*);
  jclass        (*GetSuperclass)(JNIEnv*, jclass);
  jboolean      (*IsAssignableFrom)(JNIEnv*, jclass, jclass);
  jint          (*Throw)(JNIEnv*, jthrowable);
  jint          (*ThrowNew)(JNIEnv *, jclass, const char *);
  jboolean      (*IsSameObject)(JNIEnv*, jobject, jobject);
  jclass        (*GetObjectClass)(JNIEnv*, jobject);
  jboolean      (*IsInstanceOf)(JNIEnv*, jobject, jclass);

  jstring       (*NewString)(JNIEnv*, const jchar*, jsize);
  jsize         (*GetStringLength)(JNIEnv*, jstring);
  const jchar*  (*GetStringChars)(JNIEnv*, jstring, jboolean*);
  void          (*ReleaseStringChars)(JNIEnv*, jstring, const jchar*);
  jstring       (*NewStringUTF)(JNIEnv*, const char*);
  jsize         (*GetStringUTFLength)(JNIEnv*, jstring);
  const char*   (*GetStringUTFChars)(JNIEnv*, jstring, jboolean*);
  void          (*ReleaseStringUTFChars)(JNIEnv*, jstring, const char*);

  jsize         (*GetArrayLength)(JNIEnv*, jarray);
  jobjectArray  (*NewObjectArray)(JNIEnv*, jsize, jclass, jobject);
  jobject       (*GetObjectArrayElement)(JNIEnv*, jobjectArray, jsize);
  void          (*SetObjectArrayElement)(JNIEnv*, jobjectArray, jsize, jobject);

  jbooleanArray (*NewBooleanArray)(JNIEnv*, jsize);
  jbyteArray    (*NewByteArray)(JNIEnv*, jsize);
  jcharArray    (*NewCharArray)(JNIEnv*, jsize);
  jshortArray   (*NewShortArray)(JNIEnv*, jsize);
  jintArray     (*NewIntArray)(JNIEnv*, jsize);
  jlongArray    (*NewLongArray)(JNIEnv*, jsize);
  jfloatArray   (*NewFloatArray)(JNIEnv*, jsize);
  jdoubleArray  (*NewDoubleArray)(JNIEnv*, jsize);

  jboolean*     (*GetBooleanArrayElements)(JNIEnv*, jbooleanArray, jboolean*);
  jbyte*        (*GetByteArrayElements)(JNIEnv*, jbyteArray, jboolean*);
  jchar*        (*GetCharArrayElements)(JNIEnv*, jcharArray, jboolean*);
  jshort*       (*GetShortArrayElements)(JNIEnv*, jshortArray, jboolean*);
  jint*         (*GetIntArrayElements)(JNIEnv*, jintArray, jboolean*);
  jlong*        (*GetLongArrayElements)(JNIEnv*, jlongArray, jboolean*);
  jfloat*       (*GetFloatArrayElements)(JNIEnv*, jfloatArray, jboolean*);
  jdouble*      (*GetDoubleArrayElements)(JNIEnv*, jdoubleArray, jboolean*);

  void          (*ReleaseBooleanArrayElements)(JNIEnv*, jbooleanArray, jboolean*, jint);
  void          (*ReleaseByteArrayElements)(JNIEnv*, jbyteArray, jbyte*, jint);
  void          (*ReleaseCharArrayElements)(JNIEnv*, jcharArray, jchar*, jint);
  void          (*ReleaseShortArrayElements)(JNIEnv*, jshortArray, jshort*, jint);
  void          (*ReleaseIntArrayElements)(JNIEnv*, jintArray, jint*, jint);
  void          (*ReleaseLongArrayElements)(JNIEnv*, jlongArray, jlong*, jint);
  void          (*ReleaseFloatArrayElements)(JNIEnv*, jfloatArray, jfloat*, jint);
  void          (*ReleaseDoubleArrayElements)(JNIEnv*, jdoubleArray, jdouble*, jint);

  void          (*GetBooleanArrayRegion)(JNIEnv*, jbooleanArray, jsize, jsize, jboolean*);
  void          (*GetByteArrayRegion)(JNIEnv*, jbyteArray, jsize, jsize, jbyte*);
  void          (*GetCharArrayRegion)(JNIEnv*, jcharArray, jsize, jsize, jchar*);
  void          (*GetShortArrayRegion)(JNIEnv*, jshortArray, jsize, jsize, jshort*);
  void          (*GetIntArrayRegion)(JNIEnv*, jintArray, jsize, jsize, jint*);
  void          (*GetLongArrayRegion)(JNIEnv*, jlongArray, jsize, jsize, jlong*);
  void          (*GetFloatArrayRegion)(JNIEnv*, jfloatArray, jsize, jsize, jfloat*);
  void          (*GetDoubleArrayRegion)(JNIEnv*, jdoubleArray, jsize, jsize, jdouble*);

  void          (*SetBooleanArrayRegion)(JNIEnv*, jbooleanArray, jsize, jsize, const jboolean*);
  void          (*SetByteArrayRegion)(JNIEnv*, jbyteArray, jsize, jsize, const jbyte*);
  void          (*SetCharArrayRegion)(JNIEnv*, jcharArray, jsize, jsize, const jchar*);
  void          (*SetShortArrayRegion)(JNIEnv*, jshortArray, jsize, jsize, const jshort*);
  void          (*SetIntArrayRegion)(JNIEnv*, jintArray, jsize, jsize, const jint*);
  void          (*SetLongArrayRegion)(JNIEnv*, jlongArray, jsize, jsize, const jlong*);
  void          (*SetFloatArrayRegion)(JNIEnv*, jfloatArray, jsize, jsize, const jfloat*);
  void          (*SetDoubleArrayRegion)(JNIEnv*, jdoubleArray, jsize, jsize, const jdouble*);

  void          (*GetStringRegion)(JNIEnv*, jstring, jsize, jsize, jchar*);
  void          (*GetStringUTFRegion)(JNIEnv*, jstring, jsize, jsize, char*);
  void*         (*GetPrimitiveArrayCritical)(JNIEnv*, jarray, jboolean*);
  void          (*ReleasePrimitiveArrayCritical)(JNIEnv*, jarray, void*, jint);
  const jchar*  (*GetStringCritical)(JNIEnv*, jstring, jboolean*);
  void          (*ReleaseStringCritical)(JNIEnv*, jstring, const jchar*);

  jobject       (*NewDirectByteBuffer)(JNIEnv*, void*, jlong);
  void*         (*GetDirectBufferAddress)(JNIEnv*, jobject);
  jlong         (*GetDirectBufferCapacity)(JNIEnv*, jobject);
};

/*
 * C++ object wrapper.
 *
 * This is usually overlaid on a C struct whose first element is a
 * JNINativeInterface*.  We rely somewhat on compiler behavior.
 */
struct _JNIEnv {
    /* do not rename this; it does not seem to be entirely opaque */
    const struct JNINativeInterface* functions;

#if defined(__cplusplus)

    jint GetVersion()
    { return functions->GetVersion(this); }

    jclass FindClass(const char* name)
    { return functions->FindClass(this, name); }

    jclass GetSuperclass(jclass clazz)
    { return functions->GetSuperclass(this, clazz); }

    jboolean IsAssignableFrom(jclass clazz1, jclass clazz2)
    { return functions->IsAssignableFrom(this, clazz1, clazz2); }

    jint Throw(jthrowable obj)
    { return functions->Throw(this, obj); }

    jint ThrowNew(jclass clazz, const char* message)
    { return functions->ThrowNew(this, clazz, message); }

    jboolean IsSameObject(jobject ref1, jobject ref2)
    { return functions->IsSameObject(this, ref1, ref2); }

    jclass GetObjectClass(jobject obj)
    { return functions->GetObjectClass(this, obj); }

    jboolean IsInstanceOf(jobject obj, jclass clazz)
    { return functions->IsInstanceOf(this, obj, clazz); }

    jstring NewString(const jchar* unicodeChars, jsize len)
    { return functions->NewString(this, unicodeChars, len); }

    jsize GetStringLength(jstring string)
    { return functions->GetStringLength(this, string); }

    const jchar* GetStringChars(jstring string, jboolean* isCopy)
    { return functions->GetStringChars(this, string, isCopy); }

    void ReleaseStringChars(jstring string, const jchar* chars)
    { functions->ReleaseStringChars(this, string, chars); }

    jstring NewStringUTF(const char* bytes)
    { return functions->NewStringUTF(this, bytes); }

    jsize GetStringUTFLength(jstring string)
    { return functions->GetStringUTFLength(this, string); }

    const char* GetStringUTFChars(jstring string, jboolean* isCopy)
    { return functions->GetStringUTFChars(this, string, isCopy); }

    void ReleaseStringUTFChars(jstring string, const char* utf)
    { functions->ReleaseStringUTFChars(this, string, utf); }

    jsize GetArrayLength(jarray array)
    { return functions->GetArrayLength(this, array); }

    jobjectArray NewObjectArray(jsize length, jclass elementClass,
        jobject initialElement)
    { return functions->NewObjectArray(this, length, elementClass,
        initialElement); }

    jobject GetObjectArrayElement(jobjectArray array, jsize index)
    { return functions->GetObjectArrayElement(this, array, index); }

    void SetObjectArrayElement(jobjectArray array, jsize index, jobject value)
    { functions->SetObjectArrayElement(this, array, index, value); }

    jbooleanArray NewBooleanArray(jsize length)
    { return functions->NewBooleanArray(this, length); }
    jbyteArray NewByteArray(jsize length)
    { return functions->NewByteArray(this, length); }
    jcharArray NewCharArray(jsize length)
    { return functions->NewCharArray(this, length); }
    jshortArray NewShortArray(jsize length)
    { return functions->NewShortArray(this, length); }
    jintArray NewIntArray(jsize length)
    { return functions->NewIntArray(this, length); }
    jlongArray NewLongArray(jsize length)
    { return functions->NewLongArray(this, length); }
    jfloatArray NewFloatArray(jsize length)
    { return functions->NewFloatArray(this, length); }
    jdoubleArray NewDoubleArray(jsize length)
    { return functions->NewDoubleArray(this, length); }

    jboolean* GetBooleanArrayElements(jbooleanArray array, jboolean* isCopy)
    { return functions->GetBooleanArrayElements(this, array, isCopy); }
    jbyte* GetByteArrayElements(jbyteArray array, jboolean* isCopy)
    { return functions->GetByteArrayElements(this, array, isCopy); }
    jchar* GetCharArrayElements(jcharArray array, jboolean* isCopy)
    { return functions->GetCharArrayElements(this, array, isCopy); }
    jshort* GetShortArrayElements(jshortArray array, jboolean* isCopy)
    { return functions->GetShortArrayElements(this, array, isCopy); }
    jint* GetIntArrayElements(jintArray array, jboolean* isCopy)
    { return functions->GetIntArrayElements(this, array, isCopy); }
    jlong* GetLongArrayElements(jlongArray array, jboolean* isCopy)
    { return functions->GetLongArrayElements(this, array, isCopy); }
    jfloat* GetFloatArrayElements(jfloatArray array, jboolean* isCopy)
    { return functions->GetFloatArrayElements(this, array, isCopy); }
    jdouble* GetDoubleArrayElements(jdoubleArray array, jboolean* isCopy)
    { return functions->GetDoubleArrayElements(this, array, isCopy); }

    void ReleaseBooleanArrayElements(jbooleanArray array, jboolean* elems,
        jint mode)
    { functions->ReleaseBooleanArrayElements(this, array, elems, mode); }
    void ReleaseByteArrayElements(jbyteArray array, jbyte* elems,
        jint mode)
    { functions->ReleaseByteArrayElements(this, array, elems, mode); }
    void ReleaseCharArrayElements(jcharArray array, jchar* elems,
        jint mode)
    { functions->ReleaseCharArrayElements(this, array, elems, mode); }
    void ReleaseShortArrayElements(jshortArray array, jshort* elems,
        jint mode)
    { functions->ReleaseShortArrayElements(this, array, elems, mode); }
    void ReleaseIntArrayElements(jintArray array, jint* elems,
        jint mode)
    { functions->ReleaseIntArrayElements(this, array, elems, mode); }
    void ReleaseLongArrayElements(jlongArray array, jlong* elems,
        jint mode)
    { functions->ReleaseLongArrayElements(this, array, elems, mode); }
    void ReleaseFloatArrayElements(jfloatArray array, jfloat* elems,
        jint mode)
    { functions->ReleaseFloatArrayElements(this, array, elems, mode); }
    void ReleaseDoubleArrayElements(jdoubleArray array, jdouble* elems,
        jint mode)
    { functions->ReleaseDoubleArrayElements(this, array, elems, mode); }

    void GetBooleanArrayRegion(jbooleanArray array, jsize start, jsize len,
        jboolean* buf)
    { functions->GetBooleanArrayRegion(this, array, start, len, buf); }
    void GetByteArrayRegion(jbyteArray array, jsize start, jsize len,
        jbyte* buf)
    { functions->GetByteArrayRegion(this, array, start, len, buf); }
    void GetCharArrayRegion(jcharArray array, jsize start, jsize len,
        jchar* buf)
    { functions->GetCharArrayRegion(this, array, start, len, buf); }
    void GetShortArrayRegion(jshortArray array, jsize start, jsize len,
        jshort* buf)
    { functions->GetShortArrayRegion(this, array, start, len, buf); }
    void GetIntArrayRegion(jintArray array, jsize start, jsize len,
        jint* buf)
    { functions->GetIntArrayRegion(this, array, start, len, buf); }
    void GetLongArrayRegion(jlongArray array, jsize start, jsize len,
        jlong* buf)
    { functions->GetLongArrayRegion(this, array, start, len, buf); }
    void GetFloatArrayRegion(jfloatArray array, jsize start, jsize len,
        jfloat* buf)
    { functions->GetFloatArrayRegion(this, array, start, len, buf); }
    void GetDoubleArrayRegion(jdoubleArray array, jsize start, jsize len,
        jdouble* buf)
    { functions->GetDoubleArrayRegion(this, array, start, len, buf); }

    void SetBooleanArrayRegion(jbooleanArray array, jsize start, jsize len,
        const jboolean* buf)
    { functions->SetBooleanArrayRegion(this, array, start, len, buf); }
    void SetByteArrayRegion(jbyteArray array, jsize start, jsize len,
        const jbyte* buf)
    { functions->SetByteArrayRegion(this, array, start, len, buf); }
    void SetCharArrayRegion(jcharArray array, jsize start, jsize len,
        const jchar* buf)
    { functions->SetCharArrayRegion(this, array, start, len, buf); }
    void SetShortArrayRegion(jshortArray array, jsize start, jsize len,
        const jshort* buf)
    { functions->SetShortArrayRegion(this, array, start, len, buf); }
    void SetIntArrayRegion(jintArray array, jsize start, jsize len,
        const jint* buf)
    { functions->SetIntArrayRegion(this, array, start, len, buf); }
    void SetLongArrayRegion(jlongArray array, jsize start, jsize len,
        const jlong* buf)
    { functions->SetLongArrayRegion(this, array, start, len, buf); }
    void SetFloatArrayRegion(jfloatArray array, jsize start, jsize len,
        const jfloat* buf)
    { functions->SetFloatArrayRegion(this, array, start, len, buf); }
    void SetDoubleArrayRegion(jdoubleArray array, jsize start, jsize len,
        const jdouble* buf)
    { functions->SetDoubleArrayRegion(this, array, start, len, buf); }

    void GetStringRegion(jstring str, jsize start, jsize len, jchar* buf)
    { functions->GetStringRegion(this, str, start, len, buf); }

    void GetStringUTFRegion(jstring str, jsize start, jsize len, char* buf)
    { return functions->GetStringUTFRegion(this, str, start, len, buf); }

    void* GetPrimitiveArrayCritical(jarray array, jboolean* isCopy)
    { return functions->GetPrimitiveArrayCritical(this, array, isCopy); }

    void ReleasePrimitiveArrayCritical(jarray array, void* carray, jint mode)
    { functions->ReleasePrimitiveArrayCritical(this, array, carray, mode); }

    const jchar* GetStringCritical(jstring string, jboolean* isCopy)
    { return functions->GetStringCritical(this, string, isCopy); }

    void ReleaseStringCritical(jstring string, const jchar* carray)
    { functions->ReleaseStringCritical(this, string, carray); }

    jobject NewDirectByteBuffer(void* address, jlong capacity)
    { return functions->NewDirectByteBuffer(this, address, capacity); }

    void* GetDirectBufferAddress(jobject buf)
    { return functions->GetDirectBufferAddress(this, buf); }

    jlong GetDirectBufferCapacity(jobject buf)
    { return functions->GetDirectBufferCapacity(this, buf); }
#endif /*__cplusplus*/
};

#ifdef __cplusplus
extern "C" {
#endif

#define JNIIMPORT
#define JNIEXPORT  __attribute__ ((visibility ("default")))
#define JNICALL

#ifdef __cplusplus
}
#endif


/*
 * Manifest constants.
 */
#define JNI_FALSE   NO
#define JNI_TRUE    YES

#define JNI_VERSION_1_1 0x00010001
#define JNI_VERSION_1_2 0x00010002
#define JNI_VERSION_1_4 0x00010004
#define JNI_VERSION_1_6 0x00010006

#define JNI_OK          (0)         /* no error */
#define JNI_ERR         (-1)        /* generic error */
#define JNI_EDETACHED   (-2)        /* thread detached from the VM */
#define JNI_EVERSION    (-3)        /* JNI version error */

#define JNI_COMMIT      1           /* copy content, do not free buffer */
#define JNI_ABORT       2           /* free buffer w/o copying back */

#endif  /* JNI_H_ */
