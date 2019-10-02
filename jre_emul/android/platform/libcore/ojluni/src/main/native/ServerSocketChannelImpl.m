/*
 * Copyright (c) 2000, 2008, Oracle and/or its affiliates. All rights reserved.
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

#include <stdlib.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>

#if __linux__
#include <netinet/in.h>
#endif

#if defined(__solaris__) && !defined(_SOCKLEN_T)
typedef size_t socklen_t;       /* New in SunOS 5.7, so need this for 5.6 */
#endif

#include "jni.h"
#include "jni_util.h"
#include "net_util.h"
#include "jvm.h"
#include "jlong.h"
#include "sun_nio_ch_ServerSocketChannelImpl.h"
#include "nio.h"
#include "nio_util.h"

// Objective C defs.
#include "java/io/FileDescriptor.h"
#include "java/net/InetSocketAddress.h"

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }

JNIEXPORT void JNICALL
Java_sun_nio_ch_ServerSocketChannelImpl_initIDs(JNIEnv *env, jclass c) {}

JNIEXPORT jint JNICALL
Java_sun_nio_ch_ServerSocketChannelImpl_accept0(JNIEnv *env, jobject this,
                                jobject ssfdo, jobject newfdo,
                                jobjectArray isaa)
{
    jint ssfd = ((JavaIoFileDescriptor *) ssfdo)->descriptor_;
    jint newfd;
    struct sockaddr *sa;
    int alloc_len;
    jobject remote_ia = 0;
    jobject isa;
    jint remote_port;

    NET_AllocSockaddr(&sa, &alloc_len);

    /*
     * accept connection but ignore ECONNABORTED indicating that
     * a connection was eagerly accepted but was reset before
     * accept() was called.
     */
    for (;;) {
        socklen_t sa_len = alloc_len;
        newfd = accept(ssfd, sa, &sa_len);
        if (newfd >= 0) {
            break;
        }
        if (errno != ECONNABORTED) {
            break;
        }
        /* ECONNABORTED => restart accept */
    }

    if (newfd < 0) {
        free((void *)sa);
        if (errno == EAGAIN)
            return IOS_UNAVAILABLE;
        if (errno == EINTR)
            return IOS_INTERRUPTED;
        JNU_ThrowIOExceptionWithLastError(env, "Accept failed");
        return IOS_THROWN;
    }

    ((JavaIoFileDescriptor *) newfdo)->descriptor_ = newfd;
    remote_ia = NET_SockaddrToInetAddress(env, sa, (int *)&remote_port);
    free((void *)sa);
    isa = create_JavaNetInetSocketAddress_initWithJavaNetInetAddress_withInt_(
        (JavaNetInetAddress *)remote_ia, remote_port);
    (*env)->SetObjectArrayElement(env, isaa, 0, isa);
    return 1;
}

/* J2ObjC: unused.
static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(ServerSocketChannelImpl, initIDs, "()V"),
  NATIVE_METHOD(ServerSocketChannelImpl, accept0,
                "(Ljava/io/FileDescriptor;Ljava/io/FileDescriptor;[Ljava/net/InetSocketAddress;)I"),
};

void register_sun_nio_ch_ServerSocketChannelImpl(JNIEnv* env) {
  jniRegisterNativeMethods(env, "sun/nio/ch/ServerSocketChannelImpl", gMethods, NELEM(gMethods));
}
*/
