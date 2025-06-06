/*
 * Copyright (c) 2000, 2011, Oracle and/or its affiliates. All rights reserved.
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
#include <netinet/in_systm.h>
#include <netinet/in.h>
#include <netinet/ip.h>
#include <netinet/ip_icmp.h>
#include <netdb.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>

#ifdef _ALLBSD_SOURCE
#include <unistd.h>
#include <sys/param.h>
#endif

#include "jvm.h"
#include "jni_util.h"
#include "net_util.h"

#define NATIVE_METHOD(className, functionName, signature) \
{ #functionName, signature, (void*)(className ## _ ## functionName) }

//#if defined(__GLIBC__) || (defined(__FreeBSD__) && (__FreeBSD_version >= 601104))
#define HAS_GLIBC_GETHOSTBY_R   1
//#endif

#define SET_NONBLOCKING(fd) {           \
        int flags = fcntl(fd, F_GETFL); \
        flags |= O_NONBLOCK;            \
        fcntl(fd, F_SETFL, flags);      \
}

/**
 * ping implementation.
 * Send a ICMP_ECHO_REQUEST packet every second until either the timeout
 * expires or a answer is received.
 * Returns true is an ECHO_REPLY is received, otherwise, false.
 */
static bool ping4(JNIEnv *env, jint fd, struct sockaddr_in *him, jint timeout,
                  struct sockaddr_in *netif, jint ttl) {
  jint size;
  jint n, hlen1, icmplen;
  socklen_t len;
  char sendbuf[1500];
  char recvbuf[1500];
  struct icmp *icmp;
  struct ip *ip;
  struct sockaddr_in sa_recv;
  jchar pid;
  jint tmout2, seq = 1;
  struct timeval tv;
  size_t plen;

  /* icmp_id is a 16 bit data type, therefore down cast the pid */
  pid = (jchar)getpid();
  size = 60 * 1024;
  setsockopt(fd, SOL_SOCKET, SO_RCVBUF, &size, sizeof(size));
  /*
   * sets the ttl (max number of hops)
   */
  if (ttl > 0) {
    setsockopt(fd, IPPROTO_IP, IP_TTL, &ttl, sizeof(ttl));
  }
  /*
   * a specific interface was specified, so let's bind the socket
   * to that interface to ensure the requests are sent only through it.
   */
  if (netif != NULL) {
    if (bind(fd, (struct sockaddr *)netif, sizeof(struct sockaddr_in)) < 0) {
      NET_ThrowNew(env, errno, "Can't bind socket");
      untagSocket(env, fd);
      close(fd);
      return JNI_FALSE;
    }
  }
  /*
   * Make the socket non blocking so we can use select
   */
  SET_NONBLOCKING(fd);
  do {
    /*
     * create the ICMP request
     */
    icmp = (struct icmp *)sendbuf;
    icmp->icmp_type = ICMP_ECHO;
    icmp->icmp_code = 0;
    icmp->icmp_id = htons(pid);
    icmp->icmp_seq = htons(seq);
    seq++;
    gettimeofday(&tv, NULL);
    memcpy(icmp->icmp_data, &tv, sizeof(tv));
    plen = ICMP_ADVLENMIN + sizeof(tv);
    icmp->icmp_cksum = 0;
    icmp->icmp_cksum = in_cksum((u_short *)icmp, (int)plen);
    /*
     * send it
     */
    n = (jint)sendto(fd, sendbuf, plen, 0, (struct sockaddr *)him, sizeof(struct sockaddr));
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
          n = (jint)recvfrom(fd, recvbuf, sizeof(recvbuf), 0, (struct sockaddr *)&sa_recv, &len);
          ip = (struct ip*) recvbuf;
          hlen1 = (ip->ip_hl) << 2;
          icmp = (struct icmp *) (recvbuf + hlen1);
          icmplen = n - hlen1;
          /*
           * We did receive something, but is it what we were expecting?
           * I.E.: A ICMP_ECHOREPLY packet with the proper PID.
           */
          if (icmplen >= 8 && icmp->icmp_type == ICMP_ECHOREPLY
               && (ntohs(icmp->icmp_id) == pid)) {
            if (him->sin_addr.s_addr == sa_recv.sin_addr.s_addr) {
              untagSocket(env, fd);
              close(fd);
              return JNI_TRUE;
            }

            if (him->sin_addr.s_addr == 0) {
              untagSocket(env, fd);
              close(fd);
              return JNI_TRUE;
            }
         }

        }
      } while (tmout2 > 0);
      timeout -= 1000;
    } while (timeout >0);
    untagSocket(env, fd);
    close(fd);
    return JNI_FALSE;
}

/*
 * Class:     java_net_Inet4AddressImpl
 * Method:    isReachable0
 * Signature: ([bI[bI)Z
 */
JNIEXPORT bool JNICALL Inet4AddressImpl_isReachable0(JNIEnv *env, jobject this,
                                                     jbyteArray addrArray, jint timeout,
                                                     jbyteArray ifArray, jint ttl) {
  jint addr;
  jbyte caddr[4];
  jint fd;
  struct sockaddr_in him;
  struct sockaddr_in *netif = NULL;
  struct sockaddr_in inf;
  int len = 0;
  int connect_rv = -1;
  int sz;

  memset((char *)caddr, 0, sizeof(caddr));
  memset((char *)&him, 0, sizeof(him));
  memset((char *)&inf, 0, sizeof(inf));
  sz = (*env)->GetArrayLength(env, (jarray)addrArray);
  if (sz != 4) {
    return JNI_FALSE;
  }
  (*env)->GetByteArrayRegion(env, addrArray, 0, 4, caddr);
  addr = ((caddr[0] << 24) & 0xff000000);
  addr |= ((caddr[1] << 16) & 0xff0000);
  addr |= ((caddr[2] << 8) & 0xff00);
  addr |= (caddr[3] & 0xff);
  addr = htonl(addr);
  him.sin_addr.s_addr = addr;
  him.sin_family = AF_INET;
  len = sizeof(him);
  /*
   * If a network interface was specified, let's create the address
   * for it.
   */
  if (!(IS_NULL(ifArray))) {
    memset((char *)caddr, 0, sizeof(caddr));
    (*env)->GetByteArrayRegion(env, ifArray, 0, 4, caddr);
    addr = ((caddr[0] << 24) & 0xff000000);
    addr |= ((caddr[1] << 16) & 0xff0000);
    addr |= ((caddr[2] << 8) & 0xff00);
    addr |= (caddr[3] & 0xff);
    addr = htonl(addr);
    inf.sin_addr.s_addr = addr;
    inf.sin_family = AF_INET;
    inf.sin_port = 0;
    netif = &inf;
  }

  /*
   * Let's try to create a RAW socket to send ICMP packets
   * This usually requires "root" privileges, so it's likely to fail.
   */
  fd = socket(AF_INET, SOCK_RAW, IPPROTO_ICMP);
  if (fd != -1) {
    /*
     * It didn't fail, so we can use ICMP_ECHO requests.
     */
    tagSocket(env, fd);
    return ping4(env, fd, &him, timeout, netif, ttl);
  }

  /*
   * Can't create a raw socket, so let's try a TCP socket
   */
  fd = socket(AF_INET, SOCK_STREAM, 0);
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
    setsockopt(fd, IPPROTO_IP, IP_TTL, &ttl, sizeof(ttl));
  }

  /*
   * A network interface was specified, so let's bind to it.
   */
  if (netif != NULL) {
    if (bind(fd, (struct sockaddr *)netif, sizeof(struct sockaddr_in)) < 0) {
      NET_ThrowNew(env, errno, "Can't bind socket");
      untagSocket(env, fd);
      close(fd);
      return JNI_FALSE;
    }
  }

  /*
   * Make the socket non blocking so we can use select/poll.
   */
  SET_NONBLOCKING(fd);

  /* no need to use NET_Connect as non-blocking */
  him.sin_port = htons(7); /* Echo */
  connect_rv = connect(fd, (struct sockaddr *)&him, len);

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
      case ENETUNREACH:   /* Network Unreachable */
      case EAFNOSUPPORT:  /* Address Family not supported */
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
          JNU_ThrowByName(env, JNU_JAVANETPKG "ConnectException",
                                       "connect failed");
          untagSocket(env, fd);
          close(fd);
          return JNI_FALSE;
        }

        timeout = NET_Wait(env, fd, NET_WAIT_CONNECT, timeout);
        if (timeout >= 0) {
          /* has connection been established? */
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
}
