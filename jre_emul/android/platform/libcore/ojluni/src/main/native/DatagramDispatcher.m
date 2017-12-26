/*
 * Copyright (c) 2002, Oracle and/or its affiliates. All rights reserved.
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
 */

#include "java/net/PortUnreachableException.h"
#include "jni.h"
#include "jni_util.h"
#include "jvm.h"
#include "jlong.h"
// Android removed.
//
#include <unistd.h>
#include <sys/types.h>
#include <sys/uio.h>
#include <sys/socket.h>
#include <string.h>

#include "nio_util.h"
#include <limits.h>

#include "nio.h"

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(Java_sun_nio_ch_ ## className ## _ ## functionName) }

JNIEXPORT jint JNICALL
Java_sun_nio_ch_DatagramDispatcher_read0(JNIEnv *env, jclass clazz,
                         jobject fdo, jlong address, jint len)
{
    jint fd = fdval(env, fdo);
    void *buf = (void *)jlong_to_ptr(address);
    int result = (int)recv(fd, buf, len, 0);
    if (result < 0 && errno == ECONNREFUSED) {
        J2ObjCThrowByName(JavaNetPortUnreachableException, nil);
        return -2;
    }
    return convertReturnVal(env, result, JNI_TRUE);
}

// Android-changed : Use sysconf for IOV_MAX.
static int iov_max = -1;

JNIEXPORT jlong JNICALL
Java_sun_nio_ch_DatagramDispatcher_readv0(JNIEnv *env, jclass clazz,
                              jobject fdo, jlong address, jint len)
{
    jint fd = fdval(env, fdo);
    ssize_t result = 0;
    struct iovec *iov = (struct iovec *)jlong_to_ptr(address);
    struct msghdr m;

    // Android-changed : Use sysconf for IOV_MAX.
    if (iov_max == -1) {
        iov_max = (int)sysconf(_SC_IOV_MAX);
    }
    if (len > iov_max) {
        len = iov_max;
    }

    // initialize the message
    memset(&m, 0, sizeof(m));
    m.msg_iov = iov;
    m.msg_iovlen = len;

    result = recvmsg(fd, &m, 0);
    if (result < 0 && errno == ECONNREFUSED) {
        J2ObjCThrowByName(JavaNetPortUnreachableException, nil);
        return -2;
    }
    return convertLongReturnVal(env, (jlong)result, JNI_TRUE);
}

JNIEXPORT jint JNICALL
Java_sun_nio_ch_DatagramDispatcher_write0(JNIEnv *env, jclass clazz,
                              jobject fdo, jlong address, jint len)
{
    jint fd = fdval(env, fdo);
    void *buf = (void *)jlong_to_ptr(address);
    int result = (int)send(fd, buf, len, 0);
    if (result < 0 && errno == ECONNREFUSED) {
        J2ObjCThrowByName(JavaNetPortUnreachableException, nil);
        return -2;
    }
    return convertReturnVal(env, result, JNI_FALSE);
}

JNIEXPORT jlong JNICALL
Java_sun_nio_ch_DatagramDispatcher_writev0(JNIEnv *env, jclass clazz,
                                       jobject fdo, jlong address, jint len)
{
    jint fd = fdval(env, fdo);
    struct iovec *iov = (struct iovec *)jlong_to_ptr(address);
    struct msghdr m;
    ssize_t result = 0;

    // Android-changed : Use sysconf for IOV_MAX.
    if (iov_max == -1) {
        iov_max = (int)sysconf(_SC_IOV_MAX);
    }

    if (len > iov_max) {
        len = iov_max;
    }

    // initialize the message
    memset(&m, 0, sizeof(m));
    m.msg_iov = iov;
    m.msg_iovlen = len;

    result = sendmsg(fd, &m, 0);
    if (result < 0 && errno == ECONNREFUSED) {
        J2ObjCThrowByName(JavaNetPortUnreachableException, nil);
        return -2;
    }
    return convertLongReturnVal(env, (jlong)result, JNI_FALSE);
}

/* J2ObjC: unused.
static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(DatagramDispatcher, read0, "(Ljava/io/FileDescriptor;JI)I"),
  NATIVE_METHOD(DatagramDispatcher, readv0, "(Ljava/io/FileDescriptor;JI)J"),
  NATIVE_METHOD(DatagramDispatcher, write0, "(Ljava/io/FileDescriptor;JI)I"),
  NATIVE_METHOD(DatagramDispatcher, writev0, "(Ljava/io/FileDescriptor;JI)J"),
};

void register_sun_nio_ch_DatagramDispatcher(JNIEnv* env) {
  jniRegisterNativeMethods(env, "sun/nio/ch/DatagramDispatcher", gMethods, NELEM(gMethods));
}
*/
