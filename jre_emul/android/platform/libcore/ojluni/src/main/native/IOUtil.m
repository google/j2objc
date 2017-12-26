/*
 * Copyright (c) 2000, 2010, Oracle and/or its affiliates. All rights reserved.
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

#include <sys/types.h>
#include <string.h>
#include <sys/resource.h>

#include "java/io/FileDescriptor.h"
#include "java/lang/UnsupportedOperationException.h"
#include "jni.h"
#include "jni_util.h"
#include "jvm.h"
#include "jlong.h"
#include "java_lang_Integer.h"
#include "nio.h"
#include "nio_util.h"

/* J2ObjC: unused.
#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }

static jfieldID fd_fdID;        // for jint 'fd' in java.io.FileDescriptor

static void IOUtil_initIDs(JNIEnv *env)
{
    jclass clazz = (*env)->FindClass(env, "java/io/FileDescriptor");
    fd_fdID = (*env)->GetFieldID(env, clazz, "descriptor", "I");
}
*/

JNIEXPORT jboolean JNICALL
Java_sun_nio_ch_IOUtil_randomBytes(JNIEnv *env, jclass clazz,
                                  jbyteArray randArray)
{
    J2ObjCThrowByName(JavaLangUnsupportedOperationException, nil);
    return JNI_FALSE;
}

JNIEXPORT jint JNICALL
Java_sun_nio_ch_IOUtil_fdVal(JNIEnv *env, jclass clazz, jobject fdo)
{
    return ((JavaIoFileDescriptor *)fdo)->descriptor_;
}

JNIEXPORT void JNICALL
Java_sun_nio_ch_IOUtil_setfdVal(JNIEnv *env, jclass clazz, jobject fdo, jint val)
{
    ((JavaIoFileDescriptor *)fdo)->descriptor_ = val;
}

static int
configureBlocking(int fd, jboolean blocking)
{
    int flags = fcntl(fd, F_GETFL);
    int newflags = blocking ? (flags & ~O_NONBLOCK) : (flags | O_NONBLOCK);

    return (flags == newflags) ? 0 : fcntl(fd, F_SETFL, newflags);
}

JNIEXPORT void JNICALL
Java_sun_nio_ch_IOUtil_configureBlocking(JNIEnv *env, jclass clazz,
                                         jobject fdo, jboolean blocking)
{
    if (configureBlocking(fdval(env, fdo), blocking) < 0)
        JNU_ThrowIOExceptionWithLastError(env, "Configure blocking failed");
}

JNIEXPORT jlong JNICALL
Java_sun_nio_ch_IOUtil_makePipe(JNIEnv *env, jobject this, jboolean blocking)
{
    int fd[2];

    if (pipe(fd) < 0) {
        JNU_ThrowIOExceptionWithLastError(env, "Pipe failed");
        return 0;
    }
    if (blocking == JNI_FALSE) {
        if ((configureBlocking(fd[0], JNI_FALSE) < 0)
            || (configureBlocking(fd[1], JNI_FALSE) < 0)) {
            JNU_ThrowIOExceptionWithLastError(env, "Configure blocking failed");
            close(fd[0]);
            close(fd[1]);
            return 0;
        }
    }
    return ((jlong) fd[0] << 32) | (jlong) fd[1];
}

JNIEXPORT jboolean JNICALL
Java_sun_nio_ch_IOUtil_drain(JNIEnv *env, jclass cl, jint fd)
{
    char buf[128];
    int tn = 0;

    for (;;) {
        int n = (int)read(fd, buf, sizeof(buf));
        tn += n;
        if ((n < 0) && (errno != EAGAIN))
            JNU_ThrowIOExceptionWithLastError(env, "Drain");
        if (n == (int)sizeof(buf))
            continue;
        return (tn > 0) ? JNI_TRUE : JNI_FALSE;
    }
}

JNIEXPORT jint JNICALL
Java_sun_nio_ch_IOUtil_fdLimit(JNIEnv *env, jclass this)
{
    struct rlimit rlp;
    if (getrlimit(RLIMIT_NOFILE, &rlp) < 0) {
        JNU_ThrowIOExceptionWithLastError(env, "getrlimit failed");
        return -1;
    }
    // Android-changed: Remove the rlimit < 0 check.
    if (rlp.rlim_max > java_lang_Integer_MAX_VALUE) {
        return java_lang_Integer_MAX_VALUE;
    } else {
        return (jint)rlp.rlim_max;
    }
}

/* Declared in nio_util.h for use elsewhere in NIO */

jint
convertReturnVal(JNIEnv *env, jint n, jboolean reading)
{
    if (n > 0) /* Number of bytes written */
        return n;
    else if (n == 0) {
        if (reading) {
            return IOS_EOF; /* EOF is -1 in javaland */
        } else {
            return 0;
        }
    }
    else if (errno == EAGAIN)
        return IOS_UNAVAILABLE;
    else if (errno == EINTR)
        return IOS_INTERRUPTED;
    else {
        const char *msg = reading ? "Read failed" : "Write failed";
        JNU_ThrowIOExceptionWithLastError(env, msg);
        return IOS_THROWN;
    }
}

JNIEXPORT jint JNICALL
Java_sun_nio_ch_IOUtil_iovMax(JNIEnv *env, jclass this)
{
    jlong iov_max = sysconf(_SC_IOV_MAX);
    if (iov_max == -1)
        iov_max = 16;
    return (jint)iov_max;
}


/* Declared in nio_util.h for use elsewhere in NIO */

jlong
convertLongReturnVal(JNIEnv *env, jlong n, jboolean reading)
{
    if (n > 0) /* Number of bytes written */
        return n;
    else if (n == 0) {
        if (reading) {
            return IOS_EOF; /* EOF is -1 in javaland */
        } else {
            return 0;
        }
    }
    else if (errno == EAGAIN)
        return IOS_UNAVAILABLE;
    else if (errno == EINTR)
        return IOS_INTERRUPTED;
    else {
        const char *msg = reading ? "Read failed" : "Write failed";
        JNU_ThrowIOExceptionWithLastError(env, msg);
        return IOS_THROWN;
    }
}

jint
fdval(JNIEnv *env, jobject fdo)
{
    return ((JavaIoFileDescriptor *)fdo)->descriptor_;
}

/* J2ObjC unused.
static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(IOUtil, iovMax, "()I"),
  NATIVE_METHOD(IOUtil, fdLimit, "()I"),
  NATIVE_METHOD(IOUtil, drain, "(I)Z"),
  NATIVE_METHOD(IOUtil, makePipe, "(Z)J"),
  NATIVE_METHOD(IOUtil, configureBlocking, "(Ljava/io/FileDescriptor;Z)V"),
  NATIVE_METHOD(IOUtil, setfdVal, "(Ljava/io/FileDescriptor;I)V"),
  NATIVE_METHOD(IOUtil, fdVal, "(Ljava/io/FileDescriptor;)I"),
  NATIVE_METHOD(IOUtil, randomBytes, "([B)Z"),
};

void register_sun_nio_ch_IOUtil(JNIEnv* env) {
  jniRegisterNativeMethods(env, "sun/nio/ch/IOUtil", gMethods, NELEM(gMethods));

  IOUtil_initIDs(env);
}
*/
