/*
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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

#include <errno.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>

#ifdef __solaris__
#include <fcntl.h>
#endif
#ifdef __linux__
#include <unistd.h>
//#include <sys/sysctl.h>
#include <sys/utsname.h>
#include <netinet/ip.h>

#define IPV6_MULTICAST_IF 17
#ifndef SO_BSDCOMPAT
#define SO_BSDCOMPAT  14
#endif
#endif

#ifndef IPTOS_TOS_MASK
#define IPTOS_TOS_MASK 0x1e
#endif
#ifndef IPTOS_PREC_MASK
#define IPTOS_PREC_MASK 0xe0
#endif

#include "jvm.h"
#include "jni_util.h"
#include "net_util.h"

#include "java_net_SocketOptions.h"
#include "java_net_PlainDatagramSocketImpl.h"
#include "java/lang/Boolean.h"
#include "java/lang/Integer.h"
#include "java/net/Inet6Address.h"

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }
/************************************************************************
 * PlainDatagramSocketImpl
 */

static jfieldID IO_fd_fdID;

static jfieldID pdsi_fdID;
static jfieldID pdsi_timeoutID;
static jfieldID pdsi_trafficClassID;
static jfieldID pdsi_localPortID;
static jfieldID pdsi_connected;
static jfieldID pdsi_connectedAddress;
static jfieldID pdsi_connectedPort;

#if defined(__linux__) && defined(AF_INET6)
static jfieldID pdsi_multicastInterfaceID;
static jfieldID pdsi_loopbackID;
static jfieldID pdsi_ttlID;
#endif

extern void setDefaultScopeID(JNIEnv *env, struct sockaddr *him);
extern int getDefaultScopeID(JNIEnv *env);

/*
 * Returns a java.lang.Integer based on 'i'
 */
static jobject createInteger(JNIEnv *env, int i) {
    return create_JavaLangInteger_initWithInt_(i);
}

/*
 * Returns a java.lang.Boolean based on 'b'
 */
static jobject createBoolean(JNIEnv *env, int b) {
    return JavaLangBoolean_valueOfWithBoolean_(b != 0);
}


/*
 * Returns the fd for a PlainDatagramSocketImpl or -1
 * if closed.
 */
static int getFD(JNIEnv *env, jobject this) {
    jobject fdObj = (*env)->GetObjectField(env, this, pdsi_fdID);
    if (fdObj == NULL) {
        return -1;
    }
    return (*env)->GetIntField(env, fdObj, IO_fd_fdID);
}


/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_init(JNIEnv *env, jclass cls) {

#ifdef __linux__
    struct utsname sysinfo;
#endif
    cls = (*env)->FindClass(env, "java/net/PlainDatagramSocketImpl");
    pdsi_fdID = (*env)->GetFieldID(env, cls, "fd",
                                   "Ljava/io/FileDescriptor;");
    CHECK_NULL(pdsi_fdID);
    [(id) pdsi_fdID retain];
    pdsi_timeoutID = (*env)->GetFieldID(env, cls, "timeout", "I");
    CHECK_NULL(pdsi_timeoutID);
    [(id) pdsi_timeoutID retain];
    pdsi_trafficClassID = (*env)->GetFieldID(env, cls, "trafficClass", "I");
    CHECK_NULL(pdsi_trafficClassID);
    [(id) pdsi_trafficClassID retain];
    pdsi_localPortID = (*env)->GetFieldID(env, cls, "localPort", "I");
    CHECK_NULL(pdsi_localPortID);
    [(id) pdsi_localPortID retain];
    pdsi_connected = (*env)->GetFieldID(env, cls, "connected", "Z");
    CHECK_NULL(pdsi_connected);
    [(id) pdsi_connected retain];
    pdsi_connectedAddress = (*env)->GetFieldID(env, cls, "connectedAddress",
                                               "Ljava/net/InetAddress;");
    CHECK_NULL(pdsi_connectedAddress);
    [(id) pdsi_connectedAddress retain];
    pdsi_connectedPort = (*env)->GetFieldID(env, cls, "connectedPort", "I");
    CHECK_NULL(pdsi_connectedPort);
    [(id) pdsi_connectedPort retain];
    IO_fd_fdID = NET_GetFileDescriptorID(env);
    CHECK_NULL(IO_fd_fdID);
    [(id) IO_fd_fdID retain];

#ifdef __linux__
#ifdef AF_INET6
    pdsi_multicastInterfaceID = (*env)->GetFieldID(env, cls, "multicastInterface", "I");
    CHECK_NULL(pdsi_multicastInterfaceID);
    pdsi_loopbackID = (*env)->GetFieldID(env, cls, "loopbackMode", "Z");
    CHECK_NULL(pdsi_loopbackID);
    pdsi_ttlID = (*env)->GetFieldID(env, cls, "ttl", "I");
    CHECK_NULL(pdsi_ttlID);
#endif

#endif

}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    bind
 * Signature: (ILjava/net/InetAddress;)V
 */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_bind0(JNIEnv *env, jobject this,
                                           jint localport, jobject iaObj) {
    /* fdObj is the FileDescriptor field on this */
    jobject fdObj = (*env)->GetObjectField(env, this, pdsi_fdID);
    /* fd is an int field on fdObj */
    int fd;
    int len = 0;
    SOCKADDR him;

    if (IS_NULL(fdObj)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "Socket closed");
        return;
    } else {
        fd = (*env)->GetIntField(env, fdObj, IO_fd_fdID);
    }

    if (IS_NULL(iaObj)) {
        JNU_ThrowNullPointerException(env, "iaObj is null.");
        return;
    }

    /* bind */
    if (NET_InetAddressToSockaddr(env, iaObj, localport, (struct sockaddr *)&him, &len, JNI_TRUE) != 0) {
      return;
    }
    setDefaultScopeID(env, (struct sockaddr *)&him);

    if (NET_Bind(fd, (struct sockaddr *)&him, len) < 0)  {
        if (errno == EADDRINUSE || errno == EADDRNOTAVAIL ||
            errno == EPERM || errno == EACCES) {
            JNU_ThrowByName(env, JNU_JAVANETPKG "BindException",
                            "Bind failed");
        } else {
            JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                            "Bind failed");
        }
        return;
    }

    /* intialize the local port */
    if (localport == 0) {
        /* Now that we're a connected socket, let's extract the port number
         * that the system chose for us and store it in the Socket object.
         */
        if (getsockname(fd, (struct sockaddr *)&him, (socklen_t *)&len) == -1) {
            JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                            "Error getting socket name");
            return;
        }

        localport = NET_GetPortFromSockaddr((struct sockaddr *)&him);

        (*env)->SetIntField(env, this, pdsi_localPortID, localport);
    } else {
        (*env)->SetIntField(env, this, pdsi_localPortID, localport);
    }
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    connect0
 * Signature: (Ljava/net/InetAddress;I)V
 */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_connect0(JNIEnv *env, jobject this,
                                               jobject address, jint port) {
    /* The object's field */
    jobject fdObj = (*env)->GetObjectField(env, this, pdsi_fdID);
    /* The fdObj'fd */
    jint fd;
    /* The packetAddress address, family and port */
    SOCKADDR rmtaddr;
    int len = 0;

    if (IS_NULL(fdObj)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "Socket closed");
        return;
    }
    fd = (*env)->GetIntField(env, fdObj, IO_fd_fdID);

    if (IS_NULL(address)) {
        JNU_ThrowNullPointerException(env, "address");
        return;
    }

    if (NET_InetAddressToSockaddr(env, address, port, (struct sockaddr *)&rmtaddr, &len, JNI_TRUE) != 0) {
      return;
    }

    setDefaultScopeID(env, (struct sockaddr *)&rmtaddr);
    {
        if (connect(fd, (struct sockaddr *)&rmtaddr, len) == -1) {
            JNU_ThrowByName(env, JNU_JAVANETPKG "ConnectException",
                            "Connect failed");
            return;
        }
    }
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    disconnect0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_disconnect0(JNIEnv *env, jobject this, jint family) {
    /* The object's field */
    jobject fdObj = (*env)->GetObjectField(env, this, pdsi_fdID);
    /* The fdObj'fd */
    jint fd;

#if defined(__linux__) || defined(_ALLBSD_SOURCE)
    SOCKADDR addr;
    int len;
#endif

    if (IS_NULL(fdObj)) {
        return;
    }
    fd = (*env)->GetIntField(env, fdObj, IO_fd_fdID);

#if defined(__linux__) || defined(_ALLBSD_SOURCE)
        memset(&addr, 0, sizeof(addr));
#ifdef AF_INET6
        if (ipv6_available()) {
            struct sockaddr_in6 *him6 = (struct sockaddr_in6 *)&addr;
            him6->sin6_family = AF_UNSPEC;
            len = sizeof(struct sockaddr_in6);
        } else
#endif
        {
            struct sockaddr_in *him4 = (struct sockaddr_in*)&addr;
            him4->sin_family = AF_UNSPEC;
            len = sizeof(struct sockaddr_in);
        }
        connect(fd, (struct sockaddr *)&addr, len);

#ifdef __linux__
        // After disconnecting a UDP socket, Linux kernel will set
        // local port to zero if the port number comes from implicit
        // bind. Successive send/recv on the same socket will fail.
        // So bind again with former port number here.
        int localPort = 0;
        if (getsockname(fd, (struct sockaddr *)&addr, (socklen_t *)&len) == -1) {
            return;
        }
        localPort = NET_GetPortFromSockaddr((struct sockaddr *)&addr);
        if (localPort == 0) {
            localPort = (*env)->GetIntField(env, this, pdsi_localPortID);
#ifdef AF_INET6
            if (((struct sockaddr*)&addr)->sa_family == AF_INET6) {
                ((struct sockaddr_in6*)&addr)->sin6_port = htons(localPort);
            } else
#endif /* AF_INET6 */
            {
                ((struct sockaddr_in*)&addr)->sin_port = htons(localPort);
            }
            NET_Bind(fd, (struct sockaddr *)&addr, len);
        }
#endif
#else
    connect(fd, 0, 0);
#endif
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    send
 * Signature: (Ljava/net/DatagramPacket;)V
 */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_send(JNIEnv *env, jobject this,
                                           jobject packet) {

    char BUF[MAX_BUFFER_LEN];
    char *fullPacket = NULL;
    int ret, mallocedPacket = JNI_FALSE;
    /* The object's field */
    jobject fdObj = (*env)->GetObjectField(env, this, pdsi_fdID);
    jint trafficClass = (*env)->GetIntField(env, this, pdsi_trafficClassID);

    jbyteArray packetBuffer;
    jobject packetAddress;
    jint packetBufferOffset, packetBufferLen, packetPort;
    jboolean connected;

    /* The fdObj'fd */
    jint fd;

    SOCKADDR rmtaddr, *rmtaddrP=&rmtaddr;
    int len;

    if (IS_NULL(fdObj)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "Socket closed");
        return;
    }
    fd = (*env)->GetIntField(env, fdObj, IO_fd_fdID);

    if (IS_NULL(packet)) {
        JNU_ThrowNullPointerException(env, "packet");
        return;
    }

    connected = (*env)->GetBooleanField(env, this, pdsi_connected);

    packetBuffer = (*env)->GetObjectField(env, packet, dp_bufID);
    packetAddress = (*env)->GetObjectField(env, packet, dp_addressID);
    if (IS_NULL(packetBuffer) || IS_NULL(packetAddress)) {
        JNU_ThrowNullPointerException(env, "null buffer || null address");
        return;
    }

    packetBufferOffset = (*env)->GetIntField(env, packet, dp_offsetID);
    packetBufferLen = (*env)->GetIntField(env, packet, dp_lengthID);

    if (connected) {
        /* arg to NET_Sendto () null in this case */
        len = 0;
        rmtaddrP = 0;
    } else {
        packetPort = (*env)->GetIntField(env, packet, dp_portID);
        if (NET_InetAddressToSockaddr(env, packetAddress, packetPort, (struct sockaddr *)&rmtaddr, &len, JNI_TRUE) != 0) {
          return;
        }
    }
    setDefaultScopeID(env, (struct sockaddr *)&rmtaddr);

    if (packetBufferLen > MAX_BUFFER_LEN) {
        /* When JNI-ifying the JDK's IO routines, we turned
         * read's and write's of byte arrays of size greater
         * than 2048 bytes into several operations of size 2048.
         * This saves a malloc()/memcpy()/free() for big
         * buffers.  This is OK for file IO and TCP, but that
         * strategy violates the semantics of a datagram protocol.
         * (one big send) != (several smaller sends).  So here
         * we *must* alloc the buffer.  Note it needn't be bigger
         * than 65,536 (0xFFFF) the max size of an IP packet.
         * Anything bigger should be truncated anyway.
         *
         * We may want to use a smarter allocation scheme at some
         * point.
         */
        if (packetBufferLen > MAX_PACKET_LEN) {
            packetBufferLen = MAX_PACKET_LEN;
        }
        fullPacket = (char *)malloc(packetBufferLen);

        if (!fullPacket) {
            JNU_ThrowOutOfMemoryError(env, "Send buffer native heap allocation failed");
            return;
        } else {
            mallocedPacket = JNI_TRUE;
        }
    } else {
        fullPacket = &(BUF[0]);
    }

    (*env)->GetByteArrayRegion(env, packetBuffer, packetBufferOffset, packetBufferLen,
                               (jbyte *)fullPacket);
#ifdef AF_INET6
    if (trafficClass != 0 && ipv6_available()) {
        NET_SetTrafficClass((struct sockaddr *)&rmtaddr, trafficClass);
    }
#endif /* AF_INET6 */


    /*
     * Send the datagram.
     *
     * If we are connected it's possible that sendto will return
     * ECONNREFUSED indicating that an ICMP port unreachable has
     * received.
     */
    ret = (int) NET_SendTo(fd, fullPacket, packetBufferLen, 0,
                     (struct sockaddr *)rmtaddrP, len);

    if (ret < 0) {
        switch (ret) {
            case JVM_IO_ERR :
                if (errno == ECONNREFUSED) {
                    JNU_ThrowByName(env, JNU_JAVANETPKG "PortUnreachableException",
                            "ICMP Port Unreachable");
                } else {
                    JNU_ThrowByName(env, "java/io/IOException", "sendto failed");
                }
                break;

            case JVM_IO_INTR:
                JNU_ThrowByName(env, "java/io/InterruptedIOException",
                                "operation interrupted");
                break;
        }
    }

    if (mallocedPacket) {
        free(fullPacket);
    }
    return;
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    peek
 * Signature: (Ljava/net/InetAddress;)I
 */
JNIEXPORT jint JNICALL Java_java_net_PlainDatagramSocketImpl_peek(JNIEnv *env, jobject this,
                                           jobject addressObj) {

    jobject fdObj = (*env)->GetObjectField(env, this, pdsi_fdID);
    jint timeout = (*env)->GetIntField(env, this, pdsi_timeoutID);
    jint fd;
    ssize_t n;
    SOCKADDR remote_addr;
    int len;
    char buf[1];
    jint family;
    jobject iaObj;
    int port;
    if (IS_NULL(fdObj)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Socket closed");
        return -1;
    } else {
        fd = (*env)->GetIntField(env, fdObj, IO_fd_fdID);
    }
    if (IS_NULL(addressObj)) {
        JNU_ThrowNullPointerException(env, "Null address in peek()");
    }
    if (timeout) {
        int ret = NET_Timeout(fd, timeout);
        if (ret == 0) {
            JNU_ThrowByName(env, JNU_JAVANETPKG "SocketTimeoutException",
                            "Peek timed out");
            return ret;
        } else if (ret == JVM_IO_ERR) {
            if (errno == EBADF) {
                 JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Socket closed");
            } else {
                 JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Peek failed");
            }
            return ret;
        } else if (ret == JVM_IO_INTR) {
            JNU_ThrowByName(env, JNU_JAVAIOPKG "InterruptedIOException",
                            "operation interrupted");
            return ret; /* WARNING: SHOULD WE REALLY RETURN -2??? */
        }
    }

    len = SOCKADDR_LEN;
    n = (int) NET_RecvFrom(fd, buf, 1, MSG_PEEK,
                     (struct sockaddr *)&remote_addr, (socklen_t *)&len);

    if (n == JVM_IO_ERR) {

#ifdef __solaris__
        if (errno == ECONNREFUSED) {
            int orig_errno = errno;
            (void) recv(fd, buf, 1, 0);
            errno = orig_errno;
        }
#endif
        if (errno == ECONNREFUSED) {
            JNU_ThrowByName(env, JNU_JAVANETPKG "PortUnreachableException",
                            "ICMP Port Unreachable");
        } else {
            if (errno == EBADF) {
                 JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Socket closed");
            } else {
                 JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Peek failed");
            }
        }
        return 0;
    } else if (n == JVM_IO_INTR) {
        JNU_ThrowByName(env, "java/io/InterruptedIOException", 0);
        return 0;
    }

    iaObj = NET_SockaddrToInetAddress(env, (struct sockaddr *)&remote_addr, &port);
#ifdef AF_INET6
    family = getInetAddress_family(env, iaObj) == IPv4? AF_INET : AF_INET6;
#else
    family = AF_INET;
#endif
    if (family == AF_INET) { /* this api can't handle IPV6 addresses */
        int address = getInetAddress_addr(env, iaObj);
        setInetAddress_addr(env, addressObj, address);
    }
    return port;
}

JNIEXPORT jint JNICALL Java_java_net_PlainDatagramSocketImpl_peekData(JNIEnv *env, jobject this,
                                           jobject packet) {

    char BUF[MAX_BUFFER_LEN];
    char *fullPacket = NULL;
    int mallocedPacket = JNI_FALSE;
    jobject fdObj = (*env)->GetObjectField(env, this, pdsi_fdID);
    jint timeout = (*env)->GetIntField(env, this, pdsi_timeoutID);

    jbyteArray packetBuffer;
    jint packetBufferOffset, packetBufferLen;

    int fd;

    int n;
    SOCKADDR remote_addr;
    int len;
    int port = 0;

    if (IS_NULL(fdObj)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "Socket closed");
        return -1;
    }

    fd = (*env)->GetIntField(env, fdObj, IO_fd_fdID);

    if (IS_NULL(packet)) {
        JNU_ThrowNullPointerException(env, "packet");
        return -1;
    }

    packetBuffer = (*env)->GetObjectField(env, packet, dp_bufID);
    if (IS_NULL(packetBuffer)) {
        JNU_ThrowNullPointerException(env, "packet buffer");
        return -1;
    }
    packetBufferOffset = (*env)->GetIntField(env, packet, dp_offsetID);
    packetBufferLen = (*env)->GetIntField(env, packet, dp_bufLengthID);
    if (timeout) {
        int ret = NET_Timeout(fd, timeout);
        if (ret == 0) {
            JNU_ThrowByName(env, JNU_JAVANETPKG "SocketTimeoutException",
                            "Receive timed out");
            return -1;
        } else if (ret == JVM_IO_ERR) {
#ifdef __linux__
            if (errno == EBADF) {
                JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Socket closed");
            } else {
                JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Receive failed");
            }
#else
            JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Socket closed");
#endif
            return -1;
        } else if (ret == JVM_IO_INTR) {
            JNU_ThrowByName(env, JNU_JAVAIOPKG "InterruptedIOException",
                            "operation interrupted");
            return -1;
        }
    }

    if (packetBufferLen > MAX_BUFFER_LEN) {

        /* When JNI-ifying the JDK's IO routines, we turned
         * read's and write's of byte arrays of size greater
         * than 2048 bytes into several operations of size 2048.
         * This saves a malloc()/memcpy()/free() for big
         * buffers.  This is OK for file IO and TCP, but that
         * strategy violates the semantics of a datagram protocol.
         * (one big send) != (several smaller sends).  So here
         * we *must* alloc the buffer.  Note it needn't be bigger
         * than 65,536 (0xFFFF) the max size of an IP packet.
         * anything bigger is truncated anyway.
         *
         * We may want to use a smarter allocation scheme at some
         * point.
         */
        if (packetBufferLen > MAX_PACKET_LEN) {
            packetBufferLen = MAX_PACKET_LEN;
        }
        fullPacket = (char *)malloc(packetBufferLen);

        if (!fullPacket) {
            JNU_ThrowOutOfMemoryError(env, "Peek buffer native heap allocation failed");
            return -1;
        } else {
            mallocedPacket = JNI_TRUE;
        }
    } else {
        fullPacket = &(BUF[0]);
    }

    len = SOCKADDR_LEN;
    n = (int) NET_RecvFrom(fd, fullPacket, packetBufferLen, MSG_PEEK,
                     (struct sockaddr *)&remote_addr, (socklen_t *)&len);
    /* truncate the data if the packet's length is too small */
    if (n > packetBufferLen) {
        n = packetBufferLen;
    }
    if (n == JVM_IO_ERR) {

#ifdef __solaris__
        if (errno == ECONNREFUSED) {
            int orig_errno = errno;
            (void) recv(fd, fullPacket, 1, 0);
            errno = orig_errno;
        }
#endif
        (*env)->SetIntField(env, packet, dp_offsetID, 0);
        (*env)->SetIntField(env, packet, dp_lengthID, 0);
        if (errno == ECONNREFUSED) {
            JNU_ThrowByName(env, JNU_JAVANETPKG "PortUnreachableException",
                            "ICMP Port Unreachable");
        } else {
            if (errno == EBADF) {
                 JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Socket closed");
            } else {
                 JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Receive failed");
            }
        }
    } else if (n == JVM_IO_INTR) {
        (*env)->SetIntField(env, packet, dp_offsetID, 0);
        (*env)->SetIntField(env, packet, dp_lengthID, 0);
        JNU_ThrowByName(env, JNU_JAVAIOPKG "InterruptedIOException",
                        "operation interrupted");
    } else {
        /*
         * success - fill in received address...
         *
         * REMIND: Fill in an int on the packet, and create inetadd
         * object in Java, as a performance improvement. Also
         * construct the inetadd object lazily.
         */

        jobject packetAddress;

        /*
         * Check if there is an InetAddress already associated with this
         * packet. If so we check if it is the same source address. We
         * can't update any existing InetAddress because it is immutable
         */
        packetAddress = (*env)->GetObjectField(env, packet, dp_addressID);
        if (packetAddress != NULL) {
            if (!NET_SockaddrEqualsInetAddress(env, (struct sockaddr *)&remote_addr, packetAddress)) {
                /* force a new InetAddress to be created */
                packetAddress = NULL;
            }
        }
        if (packetAddress == NULL) {
            packetAddress = NET_SockaddrToInetAddress(env, (struct sockaddr *)&remote_addr, &port);
            /* stuff the new Inetaddress in the packet */
            (*env)->SetObjectField(env, packet, dp_addressID, packetAddress);
        } else {
            /* only get the new port number */
            port = NET_GetPortFromSockaddr((struct sockaddr *)&remote_addr);
        }
        /* and fill in the data, remote address/port and such */
        (*env)->SetByteArrayRegion(env, packetBuffer, packetBufferOffset, n,
                                   (jbyte *)fullPacket);
        (*env)->SetIntField(env, packet, dp_portID, port);
        (*env)->SetIntField(env, packet, dp_lengthID, n);
    }

    if (mallocedPacket) {
        free(fullPacket);
    }
    return port;
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    receive
 * Signature: (Ljava/net/DatagramPacket;)V
 */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_receive0(JNIEnv *env, jobject this,
                                              jobject packet) {

    char BUF[MAX_BUFFER_LEN];
    char *fullPacket = NULL;
    int mallocedPacket = JNI_FALSE;
    jobject fdObj = (*env)->GetObjectField(env, this, pdsi_fdID);
    jint timeout = (*env)->GetIntField(env, this, pdsi_timeoutID);

    jbyteArray packetBuffer;
    jint packetBufferOffset, packetBufferLen;

    int fd;

    int n;
    SOCKADDR remote_addr;
    int len;
    jboolean retry;
#ifdef __linux__
    jboolean connected = JNI_FALSE;
    jobject connectedAddress = NULL;
    jint connectedPort = 0;
    jlong prevTime = 0;
#endif

    if (IS_NULL(fdObj)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "Socket closed");
        return;
    }

    fd = (*env)->GetIntField(env, fdObj, IO_fd_fdID);

    if (IS_NULL(packet)) {
        JNU_ThrowNullPointerException(env, "packet");
        return;
    }

    packetBuffer = (*env)->GetObjectField(env, packet, dp_bufID);
    if (IS_NULL(packetBuffer)) {
        JNU_ThrowNullPointerException(env, "packet buffer");
        return;
    }
    packetBufferOffset = (*env)->GetIntField(env, packet, dp_offsetID);
    packetBufferLen = (*env)->GetIntField(env, packet, dp_bufLengthID);

    if (packetBufferLen > MAX_BUFFER_LEN) {

        /* When JNI-ifying the JDK's IO routines, we turned
         * read's and write's of byte arrays of size greater
         * than 2048 bytes into several operations of size 2048.
         * This saves a malloc()/memcpy()/free() for big
         * buffers.  This is OK for file IO and TCP, but that
         * strategy violates the semantics of a datagram protocol.
         * (one big send) != (several smaller sends).  So here
         * we *must* alloc the buffer.  Note it needn't be bigger
         * than 65,536 (0xFFFF) the max size of an IP packet.
         * anything bigger is truncated anyway.
         *
         * We may want to use a smarter allocation scheme at some
         * point.
         */
        if (packetBufferLen > MAX_PACKET_LEN) {
            packetBufferLen = MAX_PACKET_LEN;
        }
        fullPacket = (char *)malloc(packetBufferLen);

        if (!fullPacket) {
            JNU_ThrowOutOfMemoryError(env, "Receive buffer native heap allocation failed");
            return;
        } else {
            mallocedPacket = JNI_TRUE;
        }
    } else {
        fullPacket = &(BUF[0]);
    }


    do {
        retry = JNI_FALSE;

        if (timeout) {
            int ret = NET_Timeout(fd, timeout);
            if (ret <= 0) {
                if (ret == 0) {
                    JNU_ThrowByName(env, JNU_JAVANETPKG "SocketTimeoutException",
                                    "Receive timed out");
                } else if (ret == JVM_IO_ERR) {
#ifdef __linux__
                    if (errno == EBADF) {
                         JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Socket closed");
                     } else {
                         JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Receive failed");
                     }
#else
                     JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Socket closed");
#endif
                } else if (ret == JVM_IO_INTR) {
                    JNU_ThrowByName(env, JNU_JAVAIOPKG "InterruptedIOException",
                                    "operation interrupted");
                }

                if (mallocedPacket) {
                    free(fullPacket);
                }

                return;
            }
        }

        /*
         * Security Note: For Linux 2.2 with connected datagrams ensure that
         * you receive into the stack/heap allocated buffer - do not attempt
         * to receive directly into DatagramPacket's byte array.
         * (ie: if the virtual machine support pinning don't use
         * GetByteArrayElements or a JNI critical section and receive
         * directly into the byte array)
         */
        len = SOCKADDR_LEN;
        n = (int) NET_RecvFrom(fd, fullPacket, packetBufferLen, 0,
                         (struct sockaddr *)&remote_addr, (socklen_t *)&len);
        /* truncate the data if the packet's length is too small */
        if (n > packetBufferLen) {
            n = packetBufferLen;
        }
        if (n == JVM_IO_ERR) {
            (*env)->SetIntField(env, packet, dp_offsetID, 0);
            (*env)->SetIntField(env, packet, dp_lengthID, 0);
            if (errno == ECONNREFUSED) {
                JNU_ThrowByName(env, JNU_JAVANETPKG "PortUnreachableException",
                                "ICMP Port Unreachable");
            } else {
                if (errno == EBADF) {
                     JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Socket closed");
                 } else {
                     JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Receive failed");
                 }
            }
        } else if (n == JVM_IO_INTR) {
            (*env)->SetIntField(env, packet, dp_offsetID, 0);
            (*env)->SetIntField(env, packet, dp_lengthID, 0);
            JNU_ThrowByName(env, JNU_JAVAIOPKG "InterruptedIOException",
                            "operation interrupted");
        } else {
            int port;
            jobject packetAddress;

            /*
             * If we are connected then we know that the datagram that we have
             * received is from the address that we are connected too. However
             * on Linux with 2.2 kernel we have to simulate this behaviour by
             * discarding any datagrams that aren't from the connected address.
             */

            /*
             * success - fill in received address...
             *
             * REMIND: Fill in an int on the packet, and create inetadd
             * object in Java, as a performance improvement. Also
             * construct the inetadd object lazily.
             */

            /*
             * Check if there is an InetAddress already associated with this
             * packet. If so we check if it is the same source address. We
             * can't update any existing InetAddress because it is immutable
             */
            packetAddress = (*env)->GetObjectField(env, packet, dp_addressID);
            if (packetAddress != NULL) {
                if (!NET_SockaddrEqualsInetAddress(env, (struct sockaddr *)&remote_addr, packetAddress)) {
                    /* force a new InetAddress to be created */
                    packetAddress = NULL;
                }
            }
            if (packetAddress == NULL) {
                packetAddress = NET_SockaddrToInetAddress(env, (struct sockaddr *)&remote_addr, &port);
                /* stuff the new Inetaddress in the packet */
                (*env)->SetObjectField(env, packet, dp_addressID, packetAddress);
            } else {
                /* only get the new port number */
                port = NET_GetPortFromSockaddr((struct sockaddr *)&remote_addr);
            }
            /* and fill in the data, remote address/port and such */
            (*env)->SetByteArrayRegion(env, packetBuffer, packetBufferOffset, n,
                                       (jbyte *)fullPacket);
            (*env)->SetIntField(env, packet, dp_portID, port);
            (*env)->SetIntField(env, packet, dp_lengthID, n);
        }

    } while (retry);

    if (mallocedPacket) {
        free(fullPacket);
    }
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    datagramSocketCreate
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_datagramSocketCreate(JNIEnv *env,
                                                           jobject this) {
    jobject fdObj = (*env)->GetObjectField(env, this, pdsi_fdID);
    int arg, fd, t = 1;
#ifdef AF_INET6
    int domain = ipv6_available() ? AF_INET6 : AF_INET;
#else
    int domain = AF_INET;
#endif

    if (IS_NULL(fdObj)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "Socket closed");
        return;
    }

    if ((fd = socket(domain, SOCK_DGRAM, 0)) == JVM_IO_ERR) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                       "Error creating socket");
        return;
    }
    tagSocket(env, fd);

#ifdef AF_INET6
    /* Disable IPV6_V6ONLY to ensure dual-socket support */
    if (domain == AF_INET6) {
        arg = 0;
        if (setsockopt(fd, IPPROTO_IPV6, IPV6_V6ONLY, (char*)&arg,
                       sizeof(int)) < 0) {
            NET_ThrowNew(env, errno, "cannot set IPPROTO_IPV6");
            untagSocket(env, fd);
            close(fd);
            return;
        }
    }
#endif /* AF_INET6 */

#ifdef __APPLE__
    arg = 65507;
    if (setsockopt(fd, SOL_SOCKET, SO_SNDBUF,
                       (char *)&arg, sizeof(arg)) < 0) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        strerror(errno));
        return;
    }
    if (setsockopt(fd, SOL_SOCKET, SO_RCVBUF,
                       (char *)&arg, sizeof(arg)) < 0) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        strerror(errno));
        return;
    }

    // Disable SIGPIPE signals, as JRE reports socket errors using exceptions.
    int set = 1;
    setsockopt(fd, SOL_SOCKET, SO_NOSIGPIPE, (void *)&set, sizeof(int));
#endif /* __APPLE__ */

     setsockopt(fd, SOL_SOCKET, SO_BROADCAST, (char*) &t, sizeof(int));

#ifdef __linux__
#ifdef AF_INET6
    /*
     * On Linux for IPv6 sockets we must set the hop limit
     * to 1 to be compatible with default ttl of 1 for IPv4 sockets.
     */
    if (domain == AF_INET6) {
        int ttl = 1;
        setsockopt(fd, IPPROTO_IPV6, IPV6_MULTICAST_HOPS, (char *)&ttl,
                   sizeof(ttl));
    }
#endif

#endif /* __linux__ */

    (*env)->SetIntField(env, fdObj, IO_fd_fdID, fd);
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    datagramSocketClose
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_datagramSocketClose(JNIEnv *env,
                                                          jobject this) {
    /*
     * REMIND: PUT A LOCK AROUND THIS CODE
     */
    jobject fdObj = (*env)->GetObjectField(env, this, pdsi_fdID);
    int fd;

    if (IS_NULL(fdObj)) {
        return;
    }
    fd = (*env)->GetIntField(env, fdObj, IO_fd_fdID);
    if (fd == -1) {
        return;
    }
    (*env)->SetIntField(env, fdObj, IO_fd_fdID, -1);
    untagSocket(env, fd);
    NET_SocketClose(fd);
}


/*
 * Set outgoing multicast interface designated by a NetworkInterface index.
 * Throw exception if failed.
 *
 * Android changed: return 0 on success, negative on failure.
 * Android changed: Interface index (not NetworkInterface) as the parameter
 */
static int mcast_set_if_by_if_v4(JNIEnv *env, jobject this, int fd, jint ifindex) {
    struct ip_mreqn req;
    memset(&req, 0, sizeof(req));
    req.imr_ifindex = ifindex;

    if (setsockopt(fd, IPPROTO_IP, IP_MULTICAST_IF,
                       (const char*)&req, sizeof(req)) < 0) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                       "Error setting socket option");
        return -1;
    }

    return 0;
}

/*
 * Set outgoing multicast interface designated by a NetworkInterface.
 * Throw exception if failed.
 * Android changed: Interface index (not NetworkInterface) as the parameter
 */
static void mcast_set_if_by_if_v6(JNIEnv *env, jobject this, int fd, jint ifindex) {
    if (setsockopt(fd, IPPROTO_IPV6, IPV6_MULTICAST_IF,
                       (const char*)&ifindex, sizeof(ifindex)) < 0) {
        if (errno == EINVAL && ifindex > 0) {
            JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                "IPV6_MULTICAST_IF failed (interface has IPv4 "
                "address only?)");
        } else {
            JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                           "Error setting socket option");
        }
        return;
    }
}

/*
 * Set outgoing multicast interface designated by an InetAddress.
 * Throw exception if failed.
 *
 * Android-changed : Return type, return 0 on success, negative on failure.
 */
static int mcast_set_if_by_addr_v4(JNIEnv *env, jobject this, int fd, jobject value) {
    struct in_addr in;

    in.s_addr = htonl( getInetAddress_addr(env, value) );

    if (setsockopt(fd, IPPROTO_IP, IP_MULTICAST_IF,
                       (const char*)&in, sizeof(in)) < 0) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                         "Error setting socket option");
        return -1;
    }

    return 0;
}

/*
 * Set outgoing multicast interface designated by an InetAddress.
 * Throw exception if failed.
 */
static void mcast_set_if_by_addr_v6(JNIEnv *env, jobject this, int fd, jobject value) {
    static jclass ni_class;
    static jmethodID ni_getByInetAddress;
    static jmethodID ni_getIndex;
    if (ni_class == NULL) {
        jclass c = (*env)->FindClass(env, "java/net/NetworkInterface");
        CHECK_NULL(c);
        ni_class = (*env)->NewGlobalRef(env, c);
        CHECK_NULL(ni_class);
        ni_getByInetAddress = (*env)->GetStaticMethodID(
            env, ni_class, "getByInetAddress", "(Ljava/net/InetAddress;)Ljava/net/NetworkInterface;");
        CHECK_NULL(ni_getByInetAddress);
        ni_getIndex = (*env)->GetMethodID(
            env, ni_class, "getIndex", "()I");
        CHECK_NULL(ni_getIndex);
    }

    /*
     * Get the NetworkInterface by inetAddress
     */
    jobject ni_value = (*env)->CallStaticObjectMethod(
        env, ni_class, ni_getByInetAddress, value);
    if (ni_value == NULL) {
        // TODO(zgao): fix ExceptionOccurred
        /*
        if (!(*env)->ExceptionOccurred(env)) {
            J2ObjCThrowByName(JavaNetSocketException,
                 @"bad argument for IP_MULTICAST_IF : address not bound to any interface");
        }
        */
        return;
    }

    /*
     * Get the NetworkInterface index
     */
    jint ifindex = (*env)->CallIntMethod(env, ni_value, ni_getIndex);
    // TODO(zgao): fix ExceptionOccurred
    /*
    if ((*env)->ExceptionOccurred(env)) {
        return;
    }
    */

    mcast_set_if_by_if_v6(env, this, fd, ifindex);
}

/*
 * Sets the multicast interface.
 *
 * SocketOptions.IP_MULTICAST_IF :-
 *      value is a InetAddress
 *      IPv4:   set outgoing multicast interface using
 *              IPPROTO_IP/IP_MULTICAST_IF
 *      IPv6:   Get the index of the interface to which the
 *              InetAddress is bound
 *              Set outgoing multicast interface using
 *              IPPROTO_IPV6/IPV6_MULTICAST_IF
 *              On Linux 2.2 record interface index as can't
 *              query the multicast interface.
 *
 * SockOptions.IF_MULTICAST_IF2 :-
 *      value is a NetworkInterface
 *      IPv4:   Obtain IP address bound to network interface
 *              (NetworkInterface.addres[0])
 *              set outgoing multicast interface using
 *              IPPROTO_IP/IP_MULTICAST_IF
 *      IPv6:   Obtain NetworkInterface.index
 *              Set outgoing multicast interface using
 *              IPPROTO_IPV6/IPV6_MULTICAST_IF
 *              On Linux 2.2 record interface index as can't
 *              query the multicast interface.
 *
 */
static void setMulticastInterface(JNIEnv *env, jobject this, int fd,
                                  jint opt, jobject value)
{
    if (opt == java_net_SocketOptions_IP_MULTICAST_IF) {
        /*
         * value is an InetAddress.
         */
        // Android-changed: Return early if mcast_set_if_by_addr_v4 threw.
        // We don't want to call into the IPV6 code with a pending exception.
        if (mcast_set_if_by_addr_v4(env, this, fd, value)) {
            return;
        }
        if (ipv6_available()) {
            mcast_set_if_by_addr_v6(env, this, fd, value);
        }
    }

    if (opt == java_net_SocketOptions_IP_MULTICAST_IF2) {
      /*
         * value is a Integer (Android-changed, openJdk uses NetworkInterface)
         */
        int index = [((JavaLangInteger *)value) intValue];

        // Android-changed: Return early if mcast_set_if_by_addr_v4 threw.
        // We don't want to call into the IPV6 code with a pending exception.
        if (mcast_set_if_by_if_v4(env, this, fd, index)) {
            return;
        }
        if (ipv6_available()) {
            mcast_set_if_by_if_v6(env, this, fd, index);
        }
    }
}

/*
 * Enable/disable local loopback of multicast datagrams.
 */
static void mcast_set_loop_v4(JNIEnv *env, jobject this, int fd, jobject value) {
    jboolean on;
    char loopback;

    on = [((JavaLangBoolean *)value) booleanValue];
    loopback = (!on ? 1 : 0);

    if (NET_SetSockOpt(fd, IPPROTO_IP, IP_MULTICAST_LOOP, (const void *)&loopback, sizeof(char)) < 0) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Error setting socket option");
        return;
    }
}

/*
 * Enable/disable local loopback of multicast datagrams.
 */
#ifdef AF_INET6
static void mcast_set_loop_v6(JNIEnv *env, jobject this, int fd, jobject value) {
    jboolean on;
    int loopback;

    on = [((JavaLangBoolean *)value) booleanValue];
    loopback = (!on ? 1 : 0);

    if (NET_SetSockOpt(fd, IPPROTO_IPV6, IPV6_MULTICAST_LOOP, (const void *)&loopback, sizeof(int)) < 0) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Error setting socket option");
        return;
    }

}
#endif  /* AF_INET6 */

/*
 * Sets the multicast loopback mode.
 */
static void setMulticastLoopbackMode(JNIEnv *env, jobject this, int fd,
                                  jint opt, jobject value) {
#ifdef AF_INET6
#ifdef __linux__
    mcast_set_loop_v4(env, this, fd, value);
    if (ipv6_available()) {
        mcast_set_loop_v6(env, this, fd, value);
    }
#else  /* __linux__ not defined */
    if (ipv6_available()) {
        mcast_set_loop_v6(env, this, fd, value);
    } else {
        mcast_set_loop_v4(env, this, fd, value);
    }
#endif  /* __linux__ */
#else
    mcast_set_loop_v4(env, this, fd, value);
#endif  /* AF_INET6 */
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    socketSetOption
 * Signature: (ILjava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_socketSetOption(JNIEnv *env,
                                                      jobject this,
                                                      jint opt,
                                                      jobject value) {
    int fd;
    int level, optname, optlen = 0;
    union {
        int i;
        char c;
    } optval;

    /*
     * Check that socket hasn't been closed
     */
    fd = getFD(env, this);
    if (fd < 0) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "Socket closed");
        return;
    }

    /*
     * Check argument has been provided
     */
    if (IS_NULL(value)) {
        JNU_ThrowNullPointerException(env, "value argument");
        return;
    }

    /*
     * Setting the multicast interface handled seperately
     */
    if (opt == java_net_SocketOptions_IP_MULTICAST_IF ||
        opt == java_net_SocketOptions_IP_MULTICAST_IF2) {

        setMulticastInterface(env, this, fd, opt, value);
        return;
    }

    /*
     * Setting the multicast loopback mode handled separately
     */
    if (opt == java_net_SocketOptions_IP_MULTICAST_LOOP) {
        setMulticastLoopbackMode(env, this, fd, opt, value);
        return;
    }

    /*
     * Map the Java level socket option to the platform specific
     * level and option name.
     */
    if (NET_MapSocketOption(opt, &level, &optname)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Invalid option");
        return;
    }

    switch (opt) {
        case java_net_SocketOptions_SO_SNDBUF :
        case java_net_SocketOptions_SO_RCVBUF :
        case java_net_SocketOptions_IP_TOS :
            {
                optval.i = [((JavaLangInteger *)value) intValue];
                optlen = sizeof(optval.i);
                break;
            }

        case java_net_SocketOptions_SO_REUSEADDR:
        case java_net_SocketOptions_SO_BROADCAST:
            {
                jboolean on;
                on = [((JavaLangBoolean *)value) booleanValue];

                /* SO_REUSEADDR or SO_BROADCAST */
                optval.i = (on ? 1 : 0);
                optlen = sizeof(optval.i);

                break;
            }

        default :
            JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                "Socket option not supported by PlainDatagramSocketImp");
            break;

    }

    if (NET_SetSockOpt(fd, level, optname, (const void *)&optval, optlen) < 0) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Error setting socket option");
        return;
    }
}


/*
 * Return the multicast interface:
 *
 * SocketOptions.IP_MULTICAST_IF
 *      IPv4:   Query IPPROTO_IP/IP_MULTICAST_IF
 *              Create InetAddress
 *              IP_MULTICAST_IF returns struct ip_mreqn on 2.2
 *              kernel but struct in_addr on 2.4 kernel
 *      IPv6:   Query IPPROTO_IPV6 / IPV6_MULTICAST_IF or
 *              obtain from impl is Linux 2.2 kernel
 *              If index == 0 return InetAddress representing
 *              anyLocalAddress.
 *              If index > 0 query NetworkInterface by index
 *              and returns addrs[0]
 *
 * SocketOptions.IP_MULTICAST_IF2
 *      IPv4:   Query IPPROTO_IP/IP_MULTICAST_IF
 *              Query NetworkInterface by IP address and
 *              return the NetworkInterface that the address
 *              is bound too.
 *      IPv6:   Query IPPROTO_IPV6 / IPV6_MULTICAST_IF
 *              (except Linux .2 kernel)
 *              Query NetworkInterface by index and
 *              return NetworkInterface.
 */
jobject getMulticastInterface(JNIEnv *env, jobject this, int fd, jint opt) {
    if ((opt == java_net_SocketOptions_IP_MULTICAST_IF2) ||
        (opt == java_net_SocketOptions_IP_MULTICAST_IF)) {
        int index;
        int len = sizeof(index);

        {
            if (getsockopt(fd, IPPROTO_IPV6, IPV6_MULTICAST_IF,
                               (char*)&index, (socklen_t *)&len) < 0) {
                JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                               "Error getting socket option");
                return NULL;
            }
        }

        jobject ifindex = createInteger(env, index);
        CHECK_NULL_RETURN(ifindex, NULL);
        return ifindex;
    }
    return NULL;
}



/*
 * Returns relevant info as a jint.
 *
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    socketGetOption
 * Signature: (I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_net_PlainDatagramSocketImpl_socketGetOption(JNIEnv *env, jobject this,
                                                      jint opt) {
    int fd;
    int level, optname, optlen;
    union {
        int i;
        char c;
    } optval;

    fd = getFD(env, this);
    if (fd < 0) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "socket closed");
        return NULL;
    }

    /*
     * Handle IP_MULTICAST_IF seperately
     */
    if (opt == java_net_SocketOptions_IP_MULTICAST_IF ||
        opt == java_net_SocketOptions_IP_MULTICAST_IF2) {
        return getMulticastInterface(env, this, fd, opt);

    }

    /*
     * SO_BINDADDR implemented using getsockname
     */
    if (opt == java_net_SocketOptions_SO_BINDADDR) {
        /* find out local IP address */
        SOCKADDR him;
        socklen_t len = 0;
        int port;
        jobject iaObj;

        len = SOCKADDR_LEN;

        if (getsockname(fd, (struct sockaddr *)&him, (socklen_t *)&len) == -1) {
            JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                           "Error getting socket name");
            return NULL;
        }
        iaObj = NET_SockaddrToInetAddress(env, (struct sockaddr *)&him, &port);

        return iaObj;
    }

    /*
     * Map the Java level socket option to the platform specific
     * level and option name.
     */
    if (NET_MapSocketOption(opt, &level, &optname)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException", "Invalid option");
        return NULL;
    }

    if (opt == java_net_SocketOptions_IP_MULTICAST_LOOP &&
        level == IPPROTO_IP) {
        optlen = sizeof(optval.c);
    } else {
        optlen = sizeof(optval.i);
    }

    if (NET_GetSockOpt(fd, level, optname, (void *)&optval, &optlen) < 0) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                         "Error getting socket option");
        return NULL;
    }

    switch (opt) {
        case java_net_SocketOptions_IP_MULTICAST_LOOP:
            /* getLoopbackMode() returns true if IP_MULTICAST_LOOP disabled */
            if (level == IPPROTO_IP) {
                return createBoolean(env, (int)!optval.c);
            } else {
                return createBoolean(env, !optval.i);
            }

        case java_net_SocketOptions_SO_BROADCAST:
        case java_net_SocketOptions_SO_REUSEADDR:
            return createBoolean(env, optval.i);

        case java_net_SocketOptions_SO_SNDBUF:
        case java_net_SocketOptions_SO_RCVBUF:
        case java_net_SocketOptions_IP_TOS:
            return createInteger(env, optval.i);

    }

    /* should never rearch here */
    return NULL;
}



/*
 * Set TTL for a socket. Throw exception if failed.
 */
static void setTTL(JNIEnv *env, int fd, jint ttl) {
    char ittl = (char)ttl;
    if (setsockopt(fd, IPPROTO_IP, IP_MULTICAST_TTL, (char*)&ittl,
                       sizeof(ittl)) < 0) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                       "Error setting socket option");
    }
}

/*
 * Set hops limit for a socket. Throw exception if failed.
 */
#ifdef AF_INET6
static void setHopLimit(JNIEnv *env, int fd, jint ttl) {
    int ittl = (int)ttl;
    if (setsockopt(fd, IPPROTO_IPV6, IPV6_MULTICAST_HOPS,
                       (char*)&ittl, sizeof(ittl)) < 0) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                       "Error setting socket option");
    }
}
#endif

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    setTTL
 * Signature: (B)V
 */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_setTimeToLive(JNIEnv *env, jobject this,
                                                    jint ttl) {

    jobject fdObj = (*env)->GetObjectField(env, this, pdsi_fdID);
    int fd;
    /* it is important to cast this to a char, otherwise setsockopt gets confused */

    if (IS_NULL(fdObj)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "Socket closed");
        return;
    } else {
        fd = (*env)->GetIntField(env, fdObj, IO_fd_fdID);
    }
    /* setsockopt to be correct ttl */
#ifdef AF_INET6
#ifdef __linux__
    setTTL(env, fd, ttl);
    if (ipv6_available()) {
        setHopLimit(env, fd, ttl);
    }
#else  /*  __linux__ not defined */
    if (ipv6_available()) {
        setHopLimit(env, fd, ttl);
    } else {
        setTTL(env, fd, ttl);
    }
#endif  /* __linux__ */
#else
    setTTL(env, fd, ttl);
#endif  /* AF_INET6 */
}

/*
 * Multicast-related calls
 */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_setTTL(JNIEnv *env, jobject this,
                                             jbyte ttl) {
    jint ittl = ttl;
    if (ittl < 0) {
        ittl += 0x100;
    }
    Java_java_net_PlainDatagramSocketImpl_setTimeToLive(env, this, ittl);
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    getTTL
 * Signature: ()B
 */
JNIEXPORT jint JNICALL Java_java_net_PlainDatagramSocketImpl_getTimeToLive(JNIEnv *env, jobject this) {

    jobject fdObj = (*env)->GetObjectField(env, this, pdsi_fdID);
    jint fd = -1;

    if (IS_NULL(fdObj)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "Socket closed");
        return -1;
    } else {
        fd = (*env)->GetIntField(env, fdObj, IO_fd_fdID);
    }
    /* getsockopt of ttl */
#ifdef AF_INET6
    if (ipv6_available()) {
        int ttl = 0;
        int len = sizeof(ttl);

        if (getsockopt(fd, IPPROTO_IPV6, IPV6_MULTICAST_HOPS,
                               (char*)&ttl, (socklen_t *)&len) < 0) {
                JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                               "Error getting socket option");
                return -1;
            }
        return (jint)ttl;
    } else
#endif /* AF_INET6 */
        {
            u_char ttl = 0;
            int len = sizeof(ttl);
            if (getsockopt(fd, IPPROTO_IP, IP_MULTICAST_TTL,
                               (char*)&ttl, (socklen_t *)&len) < 0) {
                JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                               "Error getting socket option");
                return -1;
            }
            return (jint)ttl;
        }
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    getTTL
 * Signature: ()B
 */
JNIEXPORT jbyte JNICALL Java_java_net_PlainDatagramSocketImpl_getTTL(JNIEnv *env, jobject this) {
    return (jbyte)Java_java_net_PlainDatagramSocketImpl_getTimeToLive(env, this);
}

/*
 * mcast_join_leave: Join or leave a multicast group.
 *
 * For IPv4 sockets use IP_ADD_MEMBERSHIP/IP_DROP_MEMBERSHIP socket option
 * to join/leave multicast group.
 *
 * For IPv6 sockets use IPV6_ADD_MEMBERSHIP/IPV6_DROP_MEMBERSHIP socket option
 * to join/leave multicast group. If multicast group is an IPv4 address then
 * an IPv4-mapped address is used.
 *
 * On Linux with IPv6 if we wish to join/leave an IPv4 multicast group then
 * we must use the IPv4 socket options. This is because the IPv6 socket options
 * don't support IPv4-mapped addresses. This is true as per 2.2.19 and 2.4.7
 * kernel releases. In the future it's possible that IP_ADD_MEMBERSHIP
 * will be updated to return ENOPROTOOPT if uses with an IPv6 socket (Solaris
 * already does this). Thus to cater for this we first try with the IPv4
 * socket options and if they fail we use the IPv6 socket options. This
 * seems a reasonable failsafe solution.
 */
static void mcast_join_leave(JNIEnv *env, jobject this,
                             jobject iaObj, jobject niObj,
                             jboolean join) {

    jobject fdObj = (*env)->GetObjectField(env, this, pdsi_fdID);
    jint fd;
    jint ipv6_join_leave;

    if (IS_NULL(fdObj)) {
        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "Socket closed");
        return;
    } else {
        fd = (*env)->GetIntField(env, fdObj, IO_fd_fdID);
    }
    if (IS_NULL(iaObj)) {
        JNU_ThrowNullPointerException(env, "iaObj");
        return;
    }

    /*
     * Get java/net/NetworkInterface#index field.
     */
    static jfieldID ni_indexID;

    if (ni_indexID == NULL) {
      jclass c = (*env)->FindClass(env, "java/net/NetworkInterface");
      CHECK_NULL(c);
      ni_indexID = (*env)->GetFieldID(env, c, "index", "I");
      CHECK_NULL(ni_indexID);
    }

    /*
     * Determine if this is an IPv4 or IPv6 join/leave.
     */
    ipv6_join_leave = ipv6_available();

    if (getInetAddress_family(env, iaObj) == IPv4) {
        ipv6_join_leave = JNI_FALSE;
    }

    /*
     * For IPv4 join use IP_ADD_MEMBERSHIP/IP_DROP_MEMBERSHIP socket option
     *
     * On Linux if IPv4 or IPv6 use IP_ADD_MEMBERSHIP/IP_DROP_MEMBERSHIP
     */
    if (!ipv6_join_leave) {
        struct ip_mreqn mname;
        int mname_len;

        /*
         * joinGroup(InetAddress, NetworkInterface) implementation :-
         *
         * Linux/IPv6:  use ip_mreqn structure populated with multicast
         *              address and interface index.
         *
         */
        if (niObj != NULL) {
            mname.imr_multiaddr.s_addr = htonl(getInetAddress_addr(env, iaObj));
            mname.imr_address.s_addr = 0;
            mname.imr_ifindex =  (*env)->GetIntField(env, niObj, ni_indexID);
            mname_len = sizeof(struct ip_mreqn);
        }


        /*
         * joinGroup(InetAddress) implementation :-
         *
         * Linux/IPv6:  use ip_mreqn structure populated with multicast
         *              address and interface index. index obtained
         *              from cached value or IPV6_MULTICAST_IF.
         *
         * IPv4:        use ip_mreq structure populated with multicast
         *              address and local address obtained from
         *              IP_MULTICAST_IF. On Linux IP_MULTICAST_IF
         *              returns different structure depending on
         *              kernel.
         */

        if (niObj == NULL) {
            int index;
            int len = sizeof(index);

            if (getsockopt(fd, IPPROTO_IPV6, IPV6_MULTICAST_IF,
                               (char*)&index, (socklen_t *)&len) < 0) {
                NET_ThrowCurrent(env, "getsockopt IPV6_MULTICAST_IF failed");
                return;
            }

            mname.imr_multiaddr.s_addr = htonl(getInetAddress_addr(env, iaObj));
            mname.imr_address.s_addr = 0 ;
            mname.imr_ifindex = index;
            mname_len = sizeof(struct ip_mreqn);
        }


        /*
         * Join the multicast group.
         */
        if (setsockopt(fd, IPPROTO_IP, (join ? IP_ADD_MEMBERSHIP:IP_DROP_MEMBERSHIP),
                           (char *) &mname, mname_len) < 0) {

            /*
             * If IP_ADD_MEMBERSHIP returns ENOPROTOOPT on Linux and we've got
             * IPv6 enabled then it's possible that the kernel has been fixed
             * so we switch to IPV6_ADD_MEMBERSHIP socket option.
             * As of 2.4.7 kernel IPV6_ADD_MEMERSHIP can't handle IPv4-mapped
             * addresses so we have to use IP_ADD_MEMERSHIP for IPv4 multicast
             * groups. However if the socket is an IPv6 socket then then setsockopt
             * should reurn ENOPROTOOPT. We assume this will be fixed in Linux
             * at some stage.
             */
            if (errno == ENOPROTOOPT) {
                if (ipv6_available()) {
                    ipv6_join_leave = JNI_TRUE;
                    errno = 0;
                } else  {
                    errno = ENOPROTOOPT;    /* errno can be changed by ipv6_available */
                }
            }
            if (errno) {
                if (join) {
                    NET_ThrowCurrent(env, "setsockopt IP_ADD_MEMBERSHIP failed");
                } else {
                    if (errno == ENOENT)
                        JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                            "Not a member of the multicast group");
                    else
                        NET_ThrowCurrent(env, "setsockopt IP_DROP_MEMBERSHIP failed");
                }
            }
        }

        /*
         * If we haven't switched to IPv6 socket option then we're done.
         */
        if (!ipv6_join_leave) {
            return;
        }
    }


    /*
     * IPv6 join. If it's an IPv4 multicast group then we use an IPv4-mapped
     * address.
     */
    {
        struct ipv6_mreq mname6;

        jbyteArray ipaddress;
        jbyte caddr[16];
        jint family;
        jint address;
        family = getInetAddress_family(env, iaObj) == IPv4? AF_INET : AF_INET6;
        if (family == AF_INET) { /* will convert to IPv4-mapped address */
            memset((char *) caddr, 0, 16);
            address = getInetAddress_addr(env, iaObj);

            caddr[10] = 0xff;
            caddr[11] = 0xff;

            caddr[12] = ((address >> 24) & 0xff);
            caddr[13] = ((address >> 16) & 0xff);
            caddr[14] = ((address >> 8) & 0xff);
            caddr[15] = (address & 0xff);
        } else {
            ipaddress = ((JavaNetInet6Address *)iaObj)->holder6_->ipaddress_;
            (*env)->GetByteArrayRegion(env, ipaddress, 0, 16, caddr);
        }

        memcpy((void *)&(mname6.ipv6mr_multiaddr), caddr, sizeof(struct in6_addr));
        if (IS_NULL(niObj)) {
            int index;
            int len = sizeof(index);

            {
                if (getsockopt(fd, IPPROTO_IPV6, IPV6_MULTICAST_IF,
                                 (char*)&index, (socklen_t *)&len) < 0) {
                    NET_ThrowCurrent(env, "getsockopt IPV6_MULTICAST_IF failed");
                    return;
                }
            }

            mname6.ipv6mr_interface = index;
        } else {
            jint idx = (*env)->GetIntField(env, niObj, ni_indexID);
            mname6.ipv6mr_interface = idx;
        }

#define IPV6_ADD_MEMBERSHIP     IPV6_JOIN_GROUP
#define IPV6_DROP_MEMBERSHIP    IPV6_LEAVE_GROUP
#define ADD_MEMBERSHIP          IPV6_ADD_MEMBERSHIP
#define DRP_MEMBERSHIP          IPV6_DROP_MEMBERSHIP
#define S_ADD_MEMBERSHIP        "IPV6_ADD_MEMBERSHIP"
#define S_DRP_MEMBERSHIP        "IPV6_DROP_MEMBERSHIP"

        /* Join the multicast group */
        if (setsockopt(fd, IPPROTO_IPV6, (join ? ADD_MEMBERSHIP : DRP_MEMBERSHIP),
                           (char *) &mname6, sizeof (mname6)) < 0) {

            if (join) {
                NET_ThrowCurrent(env, "setsockopt " S_ADD_MEMBERSHIP " failed");
            } else {
                if (errno == ENOENT) {
                   JNU_ThrowByName(env, JNU_JAVANETPKG "SocketException",
                        "Not a member of the multicast group");
                } else {
                    NET_ThrowCurrent(env, "setsockopt " S_DRP_MEMBERSHIP " failed");
                }
            }
        }
    }
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    join
 * Signature: (Ljava/net/InetAddress;)V
 */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_join(JNIEnv *env, jobject this,
                                           jobject iaObj, jobject niObj)
{
    mcast_join_leave(env, this, iaObj, niObj, JNI_TRUE);
}

/*
 * Class:     java_net_PlainDatagramSocketImpl
 * Method:    leave
 * Signature: (Ljava/net/InetAddress;)V
 */
JNIEXPORT void JNICALL Java_java_net_PlainDatagramSocketImpl_leave(JNIEnv *env, jobject this,
                                            jobject iaObj, jobject niObj)
{
    mcast_join_leave(env, this, iaObj, niObj, JNI_FALSE);
}

/* J2ObjC: unused.
static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(PlainDatagramSocketImpl, leave, "(Ljava/net/InetAddress;Ljava/net/NetworkInterface;)V"),
  NATIVE_METHOD(PlainDatagramSocketImpl, join, "(Ljava/net/InetAddress;Ljava/net/NetworkInterface;)V"),
  NATIVE_METHOD(PlainDatagramSocketImpl, getTimeToLive, "()I"),
  NATIVE_METHOD(PlainDatagramSocketImpl, getTTL, "()B"),
  NATIVE_METHOD(PlainDatagramSocketImpl, setTimeToLive, "(I)V"),
  NATIVE_METHOD(PlainDatagramSocketImpl, setTTL, "(B)V"),
  NATIVE_METHOD(PlainDatagramSocketImpl, socketGetOption, "(I)Ljava/lang/Object;"),
  NATIVE_METHOD(PlainDatagramSocketImpl, socketSetOption, "(ILjava/lang/Object;)V"),
  NATIVE_METHOD(PlainDatagramSocketImpl, datagramSocketClose, "()V"),
  NATIVE_METHOD(PlainDatagramSocketImpl, datagramSocketCreate, "()V"),
  NATIVE_METHOD(PlainDatagramSocketImpl, receive0, "(Ljava/net/DatagramPacket;)V"),
  NATIVE_METHOD(PlainDatagramSocketImpl, peekData, "(Ljava/net/DatagramPacket;)I"),
  NATIVE_METHOD(PlainDatagramSocketImpl, peek, "(Ljava/net/InetAddress;)I"),
  NATIVE_METHOD(PlainDatagramSocketImpl, send, "(Ljava/net/DatagramPacket;)V"),
  NATIVE_METHOD(PlainDatagramSocketImpl, disconnect0, "(I)V"),
  NATIVE_METHOD(PlainDatagramSocketImpl, connect0, "(Ljava/net/InetAddress;I)V"),
  NATIVE_METHOD(PlainDatagramSocketImpl, bind0, "(ILjava/net/InetAddress;)V"),
  NATIVE_METHOD(PlainDatagramSocketImpl, init, "()V"),
};

void register_java_net_PlainDatagramSocketImpl(JNIEnv* env) {
  jniRegisterNativeMethods(env, "java/net/PlainDatagramSocketImpl", gMethods, NELEM(gMethods));
}
*/
