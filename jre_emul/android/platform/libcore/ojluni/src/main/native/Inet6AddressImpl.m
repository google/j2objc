/*
 * Copyright (c) 2000, 2012, Oracle and/or its affiliates. All rights reserved.
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
#include <sys/time.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <string.h>
#include <strings.h>
#include <stdlib.h>
#include <ctype.h>
#ifdef _ALLBSD_SOURCE
#include <unistd.h> /* gethostname */
#endif

#include "jvm.h"
#include "jni_util.h"
#include "net_util.h"
#ifndef IPV6_DEFS_H
#include <netinet/icmp6.h>
#endif

#include "java_net_Inet4AddressImpl.h"
#include "java/net/UnknownHostException.h"

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }

/* the initial size of our hostent buffers */
#ifndef NI_MAXHOST
#define NI_MAXHOST 1025
#endif


/************************************************************************
 * Inet6AddressImpl
 */

static jclass ni_iacls;
static jclass ni_ia4cls;
static jclass ni_ia6cls;
static jmethodID ni_ia4ctrID;
static jmethodID ni_ia6ctrID;
static jfieldID ni_ia6ipaddressID;
static int initialized = 0;

/*
 * Class:     java_net_Inet6AddressImpl
 * Method:    getHostByAddr
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_java_net_Inet6AddressImpl_getHostByAddr0(JNIEnv *env, jobject this,
                                             jbyteArray addrArray) {

    jstring ret = NULL;

#ifdef AF_INET6
    char host[NI_MAXHOST+1];
    int error = 0;
    int len = 0;
    jbyte caddr[16];

    if (NET_addrtransAvailable()) {
        struct sockaddr_in him4;
        struct sockaddr_in6 him6;
        struct sockaddr *sa;

        /*
         * For IPv4 addresses construct a sockaddr_in structure.
         */
        if ((*env)->GetArrayLength(env, (jarray)addrArray) == 4) {
            jint addr;
            (*env)->GetByteArrayRegion(env, addrArray, 0, 4, caddr);
            addr = ((caddr[0]<<24) & 0xff000000);
            addr |= ((caddr[1] <<16) & 0xff0000);
            addr |= ((caddr[2] <<8) & 0xff00);
            addr |= (caddr[3] & 0xff);
            memset((void *) &him4, 0, sizeof(him4));
            him4.sin_addr.s_addr = (uint32_t) htonl(addr);
            him4.sin_family = AF_INET;
            sa = (struct sockaddr *) &him4;
            len = sizeof(him4);
        } else {
            /*
             * For IPv6 address construct a sockaddr_in6 structure.
             */
            (*env)->GetByteArrayRegion(env, addrArray, 0, 16, caddr);
            memset((void *) &him6, 0, sizeof(him6));
            memcpy((void *)&(him6.sin6_addr), caddr, sizeof(struct in6_addr) );
            him6.sin6_family = AF_INET6;
            sa = (struct sockaddr *) &him6 ;
            len = sizeof(him6) ;
        }

        error = getnameinfo(sa, len, host, NI_MAXHOST, NULL, 0, NI_NAMEREQD);

        if (!error) {
            ret = (*env)->NewStringUTF(env, host);
        }
    }
#endif /* AF_INET6 */

    if (ret == NULL) {
        J2ObjCThrowByName(JavaNetUnknownHostException, nil);
    }

    return ret;
}

#define SET_NONBLOCKING(fd) {           \
        int flags = fcntl(fd, F_GETFL); \
        flags |= O_NONBLOCK;            \
        fcntl(fd, F_SETFL, flags);      \
}

#ifdef AF_INET6
static jboolean
ping6(JNIEnv *env, jint fd, struct sockaddr_in6* him, jint timeout,
      struct sockaddr_in6* netif, jint ttl) {
    jint size;
    jint n;
    socklen_t len;
    char sendbuf[1500];
    unsigned char recvbuf[1500];
    struct icmp6_hdr *icmp6;
    struct sockaddr_in6 sa_recv;
    jbyte *caddr, *recv_caddr;
    jchar pid;
    jint tmout2, seq = 1;
    struct timeval tv;
    size_t plen;

#ifdef __linux__
    {
    int csum_offset;
    /**
     * For some strange reason, the linux kernel won't calculate the
     * checksum of ICMPv6 packets unless you set this socket option
     */
    csum_offset = 2;
    setsockopt(fd, SOL_RAW, IPV6_CHECKSUM, &csum_offset, sizeof(int));
    }
#endif

    caddr = (jbyte *)&(him->sin6_addr);

    /* icmp_id is a 16 bit data type, therefore down cast the pid */
    pid = (jchar)getpid();
    size = 60*1024;
    setsockopt(fd, SOL_SOCKET, SO_RCVBUF, &size, sizeof(size));
    if (ttl > 0) {
      setsockopt(fd, IPPROTO_IPV6, IPV6_UNICAST_HOPS, &ttl, sizeof(ttl));
    }
    if (netif != NULL) {
      if (bind(fd, (struct sockaddr*)netif, sizeof(struct sockaddr_in6)) <0) {
        NET_ThrowNew(env, errno, "Can't bind socket");
        untagSocket(env, fd);
        close(fd);
        return JNI_FALSE;
      }
    }
    SET_NONBLOCKING(fd);

    do {
      icmp6 = (struct icmp6_hdr *) sendbuf;
      icmp6->icmp6_type = ICMP6_ECHO_REQUEST;
      icmp6->icmp6_code = 0;
      /* let's tag the ECHO packet with our pid so we can identify it */
      icmp6->icmp6_id = htons(pid);
      icmp6->icmp6_seq = htons(seq);
      seq++;
      icmp6->icmp6_cksum = 0;
      gettimeofday(&tv, NULL);
      memcpy(sendbuf + sizeof(struct icmp6_hdr), &tv, sizeof(tv));
      plen = sizeof(struct icmp6_hdr) + sizeof(tv);
      n = (jint)sendto(fd, sendbuf, plen, 0, (struct sockaddr*) him, sizeof(struct sockaddr_in6));
      if (n < 0 && errno != EINPROGRESS) {
#ifdef __linux__
        if (errno != EINVAL && errno != EHOSTUNREACH)
          /*
           * On some Linuxes, when bound to the loopback interface, sendto
           * will fail and errno will be set to EINVAL or EHOSTUNREACH.
           * When that happens, don't throw an exception, just return false.
           */
#endif /*__linux__ */
        NET_ThrowNew(env, errno, "Can't send ICMP packet");
        untagSocket(env, fd);
        close(fd);
        return JNI_FALSE;
      }

      tmout2 = timeout > 1000 ? 1000 : timeout;
      do {
        tmout2 = NET_Wait(env, fd, NET_WAIT_READ, tmout2);

        if (tmout2 >= 0) {
          len = sizeof(sa_recv);
          n = (jint)recvfrom(fd, recvbuf, sizeof(recvbuf), 0, (struct sockaddr*) &sa_recv, &len);
          icmp6 = (struct icmp6_hdr *) (recvbuf);
          recv_caddr = (jbyte *)&(sa_recv.sin6_addr);
          /*
           * We did receive something, but is it what we were expecting?
           * I.E.: An ICMP6_ECHO_REPLY packet with the proper PID and
           *       from the host that we are trying to determine is reachable.
           */
          if (n >= 8 && icmp6->icmp6_type == ICMP6_ECHO_REPLY &&
              (ntohs(icmp6->icmp6_id) == pid)) {
            if (NET_IsEqual(caddr, recv_caddr)) {
              untagSocket(env, fd);
              close(fd);
              return JNI_TRUE;
            }
            if (NET_IsZeroAddr(caddr)) {
              untagSocket(env, fd);
              close(fd);
              return JNI_TRUE;
            }
          }
        }
      } while (tmout2 > 0);
      timeout -= 1000;
    } while (timeout > 0);
    untagSocket(env, fd);
    close(fd);
    return JNI_FALSE;
}
#endif /* AF_INET6 */

/*
 * Class:     java_net_Inet6AddressImpl
 * Method:    isReachable0
 * Signature: ([bII[bI)Z
 */
JNIEXPORT jboolean JNICALL
Java_java_net_Inet6AddressImpl_isReachable0(JNIEnv *env, jobject this,
                                           jbyteArray addrArray,
                                           jint scope,
                                           jint timeout,
                                           jbyteArray ifArray,
                                           jint ttl, jint if_scope) {
#ifdef AF_INET6
    jbyte caddr[16];
    jint fd, sz;
    struct sockaddr_in6 him6;
    struct sockaddr_in6 inf6;
    struct sockaddr_in6* netif = NULL;
    int len = 0;
    int connect_rv = -1;

    /*
     * If IPv6 is not enable, then we can't reach an IPv6 address, can we?
     */
    if (!ipv6_available()) {
      return JNI_FALSE;
    }
    /*
     * If it's an IPv4 address, ICMP won't work with IPv4 mapped address,
     * therefore, let's delegate to the Inet4Address method.
     */
    sz = (*env)->GetArrayLength(env, (jarray)addrArray);
    if (sz == 4) {
      return Inet4AddressImpl_isReachable0(env, this,
                                                         addrArray,
                                                         timeout,
                                                         ifArray, ttl);
    }

    memset((void *) caddr, 0, 16);
    memset((void *) &him6, 0, sizeof(him6));
    (*env)->GetByteArrayRegion(env, addrArray, 0, 16, caddr);
    memcpy((void *)&(him6.sin6_addr), caddr, sizeof(struct in6_addr) );
    him6.sin6_family = AF_INET6;

    // Android-change: Don't try and figure out a default scope ID if one isn't
    // set. It's only useful for link local addresses anyway, and callers are
    // expected to call isReachable with a specific NetworkInterface if they
    // want to query the reachability of an address that's local to that IF.
    if (scope > 0)
      him6.sin6_scope_id = scope;
    len = sizeof(struct sockaddr_in6);
    /*
     * If a network interface was specified, let's create the address
     * for it.
     */
    if (!(IS_NULL(ifArray))) {
      memset((void *) caddr, 0, 16);
      memset((void *) &inf6, 0, sizeof(inf6));
      (*env)->GetByteArrayRegion(env, ifArray, 0, 16, caddr);
      memcpy((void *)&(inf6.sin6_addr), caddr, sizeof(struct in6_addr) );
      inf6.sin6_family = AF_INET6;
      inf6.sin6_scope_id = if_scope;
      netif = &inf6;
    }
    /*
     * If we can create a RAW socket, then when can use the ICMP ECHO_REQUEST
     * otherwise we'll try a tcp socket to the Echo port (7).
     * Note that this is empiric, and not connecting could mean it's blocked
     * or the echo servioe has been disabled.
     */

    fd = socket(AF_INET6, SOCK_RAW, IPPROTO_ICMPV6);

    if (fd != -1) { /* Good to go, let's do a ping */
        tagSocket(env, fd);
        return ping6(env, fd, &him6, timeout, netif, ttl);
    }

    /* No good, let's fall back on TCP */
    fd = socket(AF_INET6, SOCK_STREAM, 0);
    if (fd == JVM_IO_ERR) {
        /* note: if you run out of fds, you may not be able to load
         * the exception class, and get a NoClassDefFoundError
         * instead.
         */
        NET_ThrowNew(env, errno, "Can't create socket");
        return JNI_FALSE;
    }
    tagSocket(env, fd);

    if (ttl > 0) {
      setsockopt(fd, IPPROTO_IPV6, IPV6_UNICAST_HOPS, &ttl, sizeof(ttl));
    }

    /*
     * A network interface was specified, so let's bind to it.
     */
    if (netif != NULL) {
      if (bind(fd, (struct sockaddr*)netif, sizeof(struct sockaddr_in6)) <0) {
        NET_ThrowNew(env, errno, "Can't bind socket");
        untagSocket(env, fd);
        close(fd);
        return JNI_FALSE;
      }
    }
    SET_NONBLOCKING(fd);

    /* no need to use NET_Connect as non-blocking */
    him6.sin6_port = htons((short) 7); /* Echo port */
    connect_rv = connect(fd, (struct sockaddr *)&him6, len);

    /**
     * connection established or refused immediately, either way it means
     * we were able to reach the host!
     */
    if (connect_rv == 0 || errno == ECONNREFUSED) {
        untagSocket(env, fd);
        close(fd);
        return JNI_TRUE;
    } else {
        int optlen;

        switch (errno) {
        case ENETUNREACH: /* Network Unreachable */
        case EAFNOSUPPORT: /* Address Family not supported */
        case EADDRNOTAVAIL: /* address is not available on  the  remote machine */
#ifdef __linux__
        case EINVAL:
        case EHOSTUNREACH:
          /*
           * On some Linuxes, when bound to the loopback interface, connect
           * will fail and errno will be set to EINVAL or EHOSTUNREACH.
           * When that happens, don't throw an exception, just return false.
           */
#endif /* __linux__ */
          untagSocket(env, fd);
          close(fd);
          return JNI_FALSE;
        }

        if (errno != EINPROGRESS) {
            NET_ThrowByNameWithLastError(env, JNU_JAVANETPKG "ConnectException",
                                         "connect failed");
            untagSocket(env, fd);
            close(fd);
            return JNI_FALSE;
        }

        timeout = NET_Wait(env, fd, NET_WAIT_CONNECT, timeout);

        if (timeout >= 0) {
          /* has connection been established */
          optlen = sizeof(connect_rv);
          if (getsockopt(fd, SOL_SOCKET, SO_ERROR, (void*)&connect_rv,
                             (socklen_t *)&optlen) <0) {
            connect_rv = errno;
          }
          if (connect_rv == 0 || connect_rv == ECONNREFUSED) {
            untagSocket(env, fd);
            close(fd);
            return JNI_TRUE;
          }
        }
        untagSocket(env, fd);
        close(fd);
        return JNI_FALSE;
    }
#else /* AF_INET6 */
    return JNI_FALSE;
#endif /* AF_INET6 */
}

/* J2ObjC: unused.
static JNINativeMethod gMethods[] = {
  NATIVE_METHOD(Inet6AddressImpl, isReachable0, "([BII[BII)Z"),
  NATIVE_METHOD(Inet6AddressImpl, getHostByAddr0, "([B)Ljava/lang/String;"),
};

void register_java_net_Inet6AddressImpl(JNIEnv* env) {
  jniRegisterNativeMethods(env, "java/net/Inet6AddressImpl", gMethods, NELEM(gMethods));
}
*/
