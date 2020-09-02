/*
 * Copyright (c) 1997, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * J2ObjC: The contents of this file have been fully replaced with J2ObjC
 * specific implementations.
 */

#include "jvm.h"
#include "jni.h"

#include "IOSArray_PackagePrivate.h"
#include "java/io/IOException.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/InternalError.h"
#include "java/lang/OutOfMemoryError.h"
#include "JreEmulation.h"

static NSString *toNSString(const char *msg) {
  return msg ? [NSString stringWithUTF8String:msg] : nil;
}

/**
 * Throw a Java exception by name. Similar to SignalError.
 */
JNIEXPORT void JNICALL
JNU_ThrowByName(JNIEnv *env, const char *name, const char *msg)
{
    jclass cls = (*env)->FindClass(env, name);

    if (cls != 0) /* Otherwise an exception has already been thrown */
        (*env)->ThrowNew(env, cls, msg);
}

/* JNU_Throw common exceptions */

JNIEXPORT void JNICALL
JNU_ThrowNullPointerException(JNIEnv *env, const char *msg)
{
    JNU_ThrowByName(env, "java/lang/NullPointerException", msg);
}

JNIEXPORT void JNICALL
JNU_ThrowArrayIndexOutOfBoundsException(JNIEnv *env, const char *msg)
{
    JNU_ThrowByName(env, "java/lang/ArrayIndexOutOfBoundsException", msg);
}

JNIEXPORT void JNICALL
JNU_ThrowOutOfMemoryError(JNIEnv *env, const char *msg) {
  @throw create_JavaLangOutOfMemoryError_initWithNSString_(toNSString(msg));
}

JNIEXPORT void JNICALL
JNU_ThrowIllegalArgumentException(JNIEnv *env, const char *msg) {
  @throw create_JavaLangIllegalArgumentException_initWithNSString_(toNSString(msg));
}

JNIEXPORT void JNICALL
JNU_ThrowInternalError(JNIEnv *env, const char *msg) {
  @throw create_JavaLangInternalError_initWithNSString_(toNSString(msg));
}

static NSString *lastErrorString(const char *defaultDetail) {
  char buf[265];
  int n = JVM_GetLastErrorString(buf, sizeof(buf));
  return toNSString(n > 0 ? buf : defaultDetail);
}

/* Throw an IOException, using the last-error string for the detail
 * string.  If the last-error string is NULL, use the given default
 * detail string.
 */
JNIEXPORT void JNICALL
JNU_ThrowIOExceptionWithLastError(JNIEnv *env, const char *defaultDetail)
{
  @throw create_JavaIoIOException_initWithNSString_(lastErrorString(defaultDetail));
}

JNIEXPORT jobject JNICALL
JNU_NewObjectByName(JNIEnv *env, const char *class_name,
                    const char *constructor_sig, ...)
{
    jobject obj = NULL;

    jclass cls = 0;
    jmethodID cls_initMID;
    va_list args;

    if ((*env)->EnsureLocalCapacity(env, 2) < 0)
    goto done;

    cls = (*env)->FindClass(env, class_name);
    if (cls == 0) {
        goto done;
    }
    cls_initMID  = (*env)->GetMethodID(env, cls,
                                       "<init>", constructor_sig);
    if (cls_initMID == NULL) {
        goto done;
    }
    va_start(args, constructor_sig);
    obj = (*env)->NewObjectV(env, cls, cls_initMID, args);
    va_end(args);

done:
    (*env)->DeleteLocalRef(env, cls);
    return obj;
}

JNIEXPORT jstring
JNU_NewStringPlatform(JNIEnv *env, const char *str) {
  return [NSString stringWithUTF8String:str];
}

JNIEXPORT const char *
JNU_GetStringPlatformChars(JNIEnv *env, jstring jstr, jboolean *isCopy) {
  (void)nil_chk(jstr);
  if (isCopy) {
    *isCopy = false;
  }
  return [jstr UTF8String];
}

JNIEXPORT void JNICALL
JNU_ReleaseStringPlatformChars(JNIEnv *env, jstring jstr, const char *str) {
  // no-op
}

JNIEXPORT jclass JNICALL
JNU_ClassString(JNIEnv *env)
{
    return NSString_class_();
}

JNIEXPORT jint JNICALL
JNU_CopyObjectArray(JNIEnv *env, jobjectArray dst, jobjectArray src, jint count) {
  [(IOSArray *)src arraycopy:0
                 destination:(IOSArray *)dst
                   dstOffset:0
                      length:count];
  return 0;
}
