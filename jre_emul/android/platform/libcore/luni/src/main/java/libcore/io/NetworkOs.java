/*
 * Copyright (C) 2011 The Android Open Source Project
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

package libcore.io;

import android.system.ErrnoException;
import android.system.GaiException;
import android.system.StructAddrinfo;
import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URLImpl;
import java.nio.ByteBuffer;
import java.nio.NioUtils;

/*-[
#include "android/system/GaiException.h"
#include "BufferUtils.h"
#include "TempFailureRetry.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/System.h"
#include "java/net/Inet6Address.h"
#include "java/net/InetAddress.h"
#include "java/net/InetSocketAddress.h"
#include "java/net/SocketException.h"
#include "libcore/io/AsynchronousCloseMonitor.h"
#include "libcore/io/Posix.h"

#include <arpa/inet.h>
#include <ifaddrs.h>
#include <net/if.h>
#include <netdb.h>
#include <sys/ioctl.h>
#include <sys/un.h>

static inline BOOL throwIfClosed(JavaIoFileDescriptor *fd) {
  if ([fd getInt$] == -1) {
    @throw create_JavaNetSocketException_initWithNSString_(@"Socket closed");
  }
  return YES;
}

#define NET_FAILURE_RETRY(return_type, syscall_name, java_fd, ...) ({ \
  return_type _rc = -1; \
  do { \
    int _fd = [java_fd getInt$]; \
    id _monitor = \
        LibcoreIoAsynchronousCloseMonitor_newAsynchronousSocketCloseMonitorWithInt_(_fd); \
    _rc = syscall_name(_fd, __VA_ARGS__); \
    _monitor = nil; \
    if (_rc == -1) { \
      throwIfClosed(fd); \
      if (errno != EINTR) { \
        LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_( \
            [NSString stringWithFormat:@"%s", # syscall_name], errno); \
      } \
    } \
  } while (_rc == -1); \
  _rc; })
]-*/

public final class NetworkOs {
  private NetworkOs() {}

  /*-[
  JavaNetInetAddress *sockaddrToInetAddress(const struct sockaddr_storage *ss, int *port);

  static JavaNetInetSocketAddress *makeSocketAddress(const struct sockaddr_storage *ss) {
    int port;
    JavaNetInetAddress *inetAddress = sockaddrToInetAddress(ss, &port);
    if (!inetAddress) {
      return nil;
    }
    return create_JavaNetInetSocketAddress_initWithJavaNetInetAddress_withInt_(inetAddress, port);
  }

  static BOOL fillIfreq(NSString *interfaceName, struct ifreq *req) {
    if (!interfaceName) {
      return NO;
    }
    memset(req, 0, sizeof(struct ifreq));
    strncpy(req->ifr_name, [interfaceName UTF8String], sizeof(req->ifr_name));
    req->ifr_name[sizeof(req->ifr_name) - 1] = '\0';
    return YES;
  }

  static BOOL fillInetSocketAddress(int rc, JavaNetInetSocketAddress *srcAddress,
      const struct sockaddr_storage *ss) {
    if (rc == -1 || !srcAddress) {
      return YES;
    }
    // Fill out the passed-in InetSocketAddress with the sender's IP address and port number.
    int port;
    JavaNetInetAddress *sender = sockaddrToInetAddress(ss, &port);
    if (!sender) {
      return NO;
    }
    LibcoreIoNetworkOs_updateInetSocketAddressWithJavaNetInetSocketAddress_withJavaNetInetAddress_withInt_(
        srcAddress, RETAIN_(sender), port);
    return YES;
  }

  static JavaNetSocketAddress *doGetSockName(int fd, BOOL is_sockname) {
    struct sockaddr_storage ss;
    struct sockaddr *sa = (struct sockaddr *) &ss;
    socklen_t byteCount = sizeof(ss);
    memset(&ss, 0, byteCount);
    int rc = is_sockname ? TEMP_FAILURE_RETRY(getsockname(fd, sa, &byteCount))
        : TEMP_FAILURE_RETRY(getpeername(fd, sa, &byteCount));
    if (rc == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(
          (is_sockname ? @"getsockname" : @"getpeername"), rc);
    }
    return makeSocketAddress(&ss);
  }

  static void copyAddress(int addr, jbyte *dst) {
    dst[0] = (jbyte)((addr >> 24) & 0xff);
    dst[1] = (jbyte)((addr >> 16) & 0xff);
    dst[2] = (jbyte)((addr >> 8) & 0xff);
    dst[3] = (jbyte)(addr & 0xff);
  }

  JavaNetInetAddress *sockaddrToInetAddress(const struct sockaddr_storage *ss, int *port) {
    // Convert IPv4-mapped IPv6 addresses to IPv4 addresses.
    // The RI states "Java will never return an IPv4-mapped address".
    const struct sockaddr_in6 *sin6 = (const struct sockaddr_in6 *) ss;
    if (ss->ss_family == AF_INET6 && IN6_IS_ADDR_V4MAPPED(&sin6->sin6_addr)) {
      // Copy the IPv6 address into the temporary sockaddr_storage.
      struct sockaddr_storage tmp;
      memset(&tmp, 0, sizeof(tmp));
      memcpy(&tmp, ss, sizeof(struct sockaddr_in6));
      // Unmap it into an IPv4 address.
      struct sockaddr_in *sin = (struct sockaddr_in *) &tmp;
      sin->sin_family = AF_INET;
      sin->sin_port = sin6->sin6_port;
      memcpy(&sin->sin_addr.s_addr, &sin6->sin6_addr.s6_addr[12], 4);
      // Do the regular conversion using the unmapped address.
      return sockaddrToInetAddress(&tmp, port);
    }

    const void *rawAddress;
    size_t addressLength;
    int sin_port = 0;
    int scope_id = 0;
    if (ss->ss_family == AF_INET) {
      const struct sockaddr_in *sin = (const struct sockaddr_in *) ss;
      rawAddress = &sin->sin_addr.s_addr;
      addressLength = 4;
      sin_port = ntohs(sin->sin_port);
    } else if (ss->ss_family == AF_INET6) {
      const struct sockaddr_in6 *sin6 = (const struct sockaddr_in6 *) ss;
      rawAddress = &sin6->sin6_addr.s6_addr;
      addressLength = 16;
      sin_port = ntohs(sin6->sin6_port);
      scope_id = sin6->sin6_scope_id;
    } else if (ss->ss_family == AF_UNIX) {
      const struct sockaddr_un *sun = (const struct sockaddr_un *) ss;
      rawAddress = &sun->sun_path;
      addressLength = strlen(sun->sun_path);
    } else {
      // We can't throw SocketException. We aren't meant to see bad addresses, so seeing one
      // really does imply an internal error.
      NSString *errMsg =
          [NSString stringWithFormat:@"sockaddrToInetAddress unsupported ss_family: %i",
              ss->ss_family];
      @throw create_JavaLangIllegalArgumentException_initWithNSString_(errMsg);
    }
    if (port != NULL) {
      *port = sin_port;
    }

    IOSByteArray *byteArray =
        [IOSByteArray arrayWithBytes:(jbyte *)rawAddress count:(jint)addressLength];

    return JavaNetInetAddress_getByAddressWithNSString_withByteArray_withInt_(
        nil, byteArray, scope_id);
  }

  static BOOL inetAddressToSockaddrImpl(JavaNetInetAddress *inetAddress, int port,
      struct sockaddr_storage *ss, socklen_t *sa_len, BOOL map) {
    memset(ss, 0, sizeof(struct sockaddr_storage));
    *sa_len = 0;
    (void)nil_chk(inetAddress);

    // Get the address family.
    ss->ss_family = [inetAddress->holder_ getFamily];
    if (ss->ss_family == AF_UNSPEC) {
      *sa_len = sizeof(ss->ss_family);
      return YES; // Job done!
    }

    // Check this is an address family we support.
    if (ss->ss_family != AF_INET && ss->ss_family != AF_INET6 && ss->ss_family != AF_UNIX) {
      NSString *errMsg =
          [NSString stringWithFormat:@"inetAddressToSockaddr bad family: %i", ss->ss_family];
      @throw create_JavaLangIllegalArgumentException_initWithNSString_(errMsg);
    }

    // Handle the AF_UNIX special case.
    if (ss->ss_family == AF_UNIX) {
      struct sockaddr_un *sun = (struct sockaddr_un *)ss;

      // Copy the bytes...
      jbyte* dst = (jbyte *)sun->sun_path;
      memset(dst, 0, sizeof(sun->sun_path));
      copyAddress(inetAddress->holder_->address_, dst);
      *sa_len = sizeof(sun->sun_path);
      return YES;
    }

    // We use AF_INET6 sockets, so we want an IPv6 address (which may be a IPv4-mapped address).
    struct sockaddr_in6 *sin6 = (struct sockaddr_in6 *) ss;
    sin6->sin6_port = htons(port);
    if (ss->ss_family == AF_INET6) {
      // IPv6 address. Copy the bytes...
      jbyte *dst = (jbyte *)sin6->sin6_addr.s6_addr;
      [((JavaNetInet6Address *) inetAddress)->ipaddress_ getBytes:dst length:16];
      // ...and set the scope id...
      sin6->sin6_scope_id = [(JavaNetInet6Address *) inetAddress getScopeId];
      *sa_len = sizeof(struct sockaddr_in6);
      return true;
    }

    // Deal with Inet4Address instances.
    if (map) {
      // We should represent this Inet4Address as an IPv4-mapped IPv6 sockaddr_in6.
      // Change the family...
      sin6->sin6_family = AF_INET6;
      // Copy the address
      jbyte *dst = (jbyte *)&sin6->sin6_addr.s6_addr[12];
      copyAddress(inetAddress->holder_->address_, dst);
      // INADDR_ANY and in6addr_any are both all-zeros...
      if (!IN6_IS_ADDR_UNSPECIFIED(&sin6->sin6_addr)) {
          // ...but all other IPv4-mapped addresses are ::ffff:a.b.c.d, so insert the ffff...
          memset(&(sin6->sin6_addr.s6_addr[10]), 0xff, 2);
      }
      *sa_len = sizeof(struct sockaddr_in6);
    } else {
      // We should represent this Inet4Address as an IPv4 sockaddr_in.
      struct sockaddr_in *sin = (struct sockaddr_in *) ss;
      sin->sin_port = htons(port);
      sin->sin_addr.s_addr = inetAddress->holder_->address_;
      *sa_len = sizeof(struct sockaddr_in);
    }
    return YES;
  }

  BOOL inetAddressToSockaddrVerbatim(JavaNetInetAddress *inetAddress, int port,
      struct sockaddr_storage *ss, socklen_t *sa_len) {
    return inetAddressToSockaddrImpl(inetAddress, port, ss, sa_len, NO);
  }

  BOOL inetAddressToSockaddr(JavaNetInetAddress *inetAddress, int port,
      struct sockaddr_storage *ss, socklen_t *sa_len) {
    return inetAddressToSockaddrImpl(inetAddress, port, ss, sa_len, YES);
  }
  ]-*/

  public static native FileDescriptor accept(FileDescriptor fd, InetSocketAddress peerAddress)
      throws ErrnoException, SocketException /*-[
    struct sockaddr_storage ss;
    socklen_t sl = sizeof(ss);
    memset(&ss, 0, sizeof(ss));
    struct sockaddr *peer = peerAddress ? (struct sockaddr *) &ss : NULL;
    socklen_t *peerLength = peerAddress ? &sl : 0;
    int clientFd = NET_FAILURE_RETRY(int, accept, fd, peer, peerLength);
    if (clientFd == -1 || !fillInetSocketAddress([fd getInt$], peerAddress, &ss)) {
      close(clientFd);
      return nil;
    }
    if (clientFd == -1) {
      return nil;
    }
    JavaIoFileDescriptor *newFd = create_JavaIoFileDescriptor_init();
    [newFd setInt$WithInt:clientFd];
    return newFd;
  ]-*/;

  public static native void bind(FileDescriptor fd, InetAddress address, int port)
      throws ErrnoException, SocketException /*-[
    struct sockaddr_storage ss;
    socklen_t sa_len;
    if (!inetAddressToSockaddr(address, port, &ss, &sa_len)) {
      return;
    }
    const struct sockaddr* sa = (const struct sockaddr *) &ss;
    (void) NET_FAILURE_RETRY(int, bind, fd, sa, sa_len);
  ]-*/;

  public static native void connect(FileDescriptor fd, InetAddress address, int port)
      throws ErrnoException, SocketException /*-[
    struct sockaddr_storage ss;
    socklen_t sa_len;
    if (!inetAddressToSockaddr(address, port, &ss, &sa_len)) {
      return;
    }
    bool disconnect = false;
    if (ss.ss_family == AF_UNSPEC) {
      // Closing a datagram socket by connecting to AF_UNSPEC doesn't work,
      // docs say to use an invalid inet address instead.
      disconnect = true;
      ss.ss_family = AF_INET6;
      sa_len = sizeof(const struct sockaddr_in6);
    }
    const struct sockaddr* sa = (const struct sockaddr *) &ss;
    int rc = TEMP_FAILURE_RETRY(connect([fd getInt$], sa, sa_len));
    if (rc == -1 && disconnect && errno == EADDRNOTAVAIL) {
      // It's a valid disconnect from invalid inet address above, so reset errno.
      errno = 0;
    } else {
      LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"connect", rc);
    }
  ]-*/;

  public static native String gai_strerror(int error) /*-[
    return [NSString stringWithUTF8String:gai_strerror(error)];
  ]-*/;

  public static native InetAddress[] getaddrinfo(String node, StructAddrinfo javaHints)
      throws GaiException /*-[
    if (!node) {
      return nil;
    }

    struct addrinfo hints;
    memset(&hints, 0, sizeof(hints));
    hints.ai_flags = javaHints->ai_flags_;
    hints.ai_family = javaHints->ai_family_;
    hints.ai_socktype = javaHints->ai_socktype_;
    hints.ai_protocol = javaHints->ai_protocol_;

    struct addrinfo* addressList = NULL;
    errno = 0;
    int rc = getaddrinfo([node UTF8String], NULL, &hints, &addressList);
    if (rc != 0) {
      @throw create_AndroidSystemGaiException_initWithNSString_withInt_(@"getaddrinfo", rc);
    }

    // Count results so we know how to size the output array.
    int addressCount = 0;
    for (struct addrinfo* ai = addressList; ai != NULL; ai = ai->ai_next) {
      if (ai->ai_family == AF_INET || ai->ai_family == AF_INET6) {
        ++addressCount;
      } else {
        NSString *errMsg =
            [NSString stringWithFormat:@"getaddrinfo unexpected ai_family %i", ai->ai_family];
        JavaLangSystem_logEWithNSString_(errMsg);
      }
    }
    if (addressCount == 0) {
      freeaddrinfo(addressList);
      return nil;
    }

    // Prepare output array.
    IOSObjectArray *result =
        [IOSObjectArray arrayWithLength:addressCount type:JavaNetInetAddress_class_()];

    // Examine returned addresses one by one, save them in the output array.
    int index = 0;
    for (struct addrinfo* ai = addressList; ai != NULL; ai = ai->ai_next) {
      if (ai->ai_family != AF_INET && ai->ai_family != AF_INET6) {
        // Unknown address family. Skip this address.
        NSString *errMsg =
            [NSString stringWithFormat:@"getaddrinfo unexpected ai_family %i", ai->ai_family];
        JavaLangSystem_logEWithNSString_(errMsg);
        continue;
      }

      // Convert each IP address into a Java byte array.
      struct sockaddr_storage *address = (struct sockaddr_storage *) ai->ai_addr;
      JavaNetInetAddress *inetAddress = sockaddrToInetAddress(address, NULL);
      if (!inetAddress) {
        freeaddrinfo(addressList);
        return nil;
      }
      [result replaceObjectAtIndex:index withObject:inetAddress];
      ++index;
    }
    freeaddrinfo(addressList);
    return result;
  ]-*/;

  public static native String getnameinfo(InetAddress address, int flags) throws GaiException /*-[
    struct sockaddr_storage ss;
    socklen_t sa_len;
    if (!inetAddressToSockaddrVerbatim(address, 0, &ss, &sa_len)) {
      return nil;
    }
    char buf[NI_MAXHOST]; // NI_MAXHOST is longer than INET6_ADDRSTRLEN.
    errno = 0;
    int rc = getnameinfo((struct sockaddr *) &ss, sa_len, buf, sizeof(buf), NULL, 0, flags);
    if (rc != 0) {
      @throw create_AndroidSystemGaiException_initWithNSString_withInt_(@"getnameinfo", rc);
    }
    return [NSString stringWithUTF8String:buf];
  ]-*/;

  public static native SocketAddress getsockname(FileDescriptor fd) throws ErrnoException /*-[
    return doGetSockName([fd getInt$], YES);
  ]-*/;

  public static native int getsockoptByte(FileDescriptor fd, int level, int option)
      throws ErrnoException /*-[
    u_char result = 0;
    socklen_t size = sizeof(result);
    int rc = TEMP_FAILURE_RETRY(getsockopt([fd getInt$], level, option, &result, &size));
    if (rc == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"getsockopt", rc);
    }
    return result;
  ]-*/;

  public static native InetAddress getsockoptInAddr(FileDescriptor fd, int level, int option)
      throws ErrnoException /*-[
    struct sockaddr_storage ss;
    memset(&ss, 0, sizeof(ss));
    ss.ss_family = AF_INET; // This is only for the IPv4-only IP_MULTICAST_IF.
    struct sockaddr_in* sa = (struct sockaddr_in *) &ss;
    socklen_t size = sizeof(sa->sin_addr);
    int rc = TEMP_FAILURE_RETRY(getsockopt([fd getInt$], level, option, &sa->sin_addr, &size));
    if (rc == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"getsockopt", rc);
    }
    return sockaddrToInetAddress(&ss, NULL);
  ]-*/;

  public static native int getsockoptInt(FileDescriptor fd, int level, int option)
      throws ErrnoException /*-[
    int result = 0;
    socklen_t size = sizeof(result);
    int rc = TEMP_FAILURE_RETRY(getsockopt([fd getInt$], level, option, &result, &size));
    if (rc == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"getsockopt", rc);
    }
    return result;
  ]-*/;

  public static native StructLinger getsockoptLinger(FileDescriptor fd, int level, int option)
      throws ErrnoException /*-[
    struct linger l;
    socklen_t size = sizeof(l);
    memset(&l, 0, size);
    int rc = TEMP_FAILURE_RETRY(getsockopt([fd getInt$], level, option, &l, &size));
    if (rc == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"getsockopt", rc);
    }
    return create_LibcoreIoStructLinger_initWithInt_withInt_(l.l_onoff, l.l_linger);
  ]-*/;

  public static native StructTimeval getsockoptTimeval(FileDescriptor fd, int level,
      int option) throws ErrnoException /*-[
    struct timeval tv;
    socklen_t size = sizeof(tv);
    memset(&tv, 0, size);
    int rc = TEMP_FAILURE_RETRY(getsockopt([fd getInt$], level, option, &tv, &size));
    if (rc == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"getsockopt", rc);
    }
    return create_LibcoreIoStructTimeval_initWithLong_withLong_(tv.tv_sec, tv.tv_usec);
  ]-*/;

  public static native InetAddress inet_pton(int family, String address) /*-[
    if (!address) {
      return nil;
    }
    struct sockaddr_storage ss;
    memset(&ss, 0, sizeof(ss));
    // sockaddr_in and sockaddr_in6 are at the same address, so we can use either here.
    void *dst = &((struct sockaddr_in *) &ss)->sin_addr;
    if (inet_pton(family, [address UTF8String], dst) != 1) {
      return nil;
    }
    ss.ss_family = family;
    return sockaddrToInetAddress(&ss, NULL);
  ]-*/;

  public static native InetAddress ioctlInetAddress(FileDescriptor fd, int cmd,
      String interfaceName) throws ErrnoException /*-[
    struct ifreq req;
    if (!fillIfreq(interfaceName, &req)) {
      return nil;
    }
    int rc = TEMP_FAILURE_RETRY(ioctl([fd getInt$], cmd, &req));
    if (rc == 0) {
      return sockaddrToInetAddress((struct sockaddr_storage *) &req.ifr_addr, NULL);
    }
    int originalError = errno;
    NSString *msg = [NSString stringWithFormat:@"ioctl (%d, %@)", cmd, interfaceName];
    if (originalError != ENOTSUP && originalError != EOPNOTSUPP) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(msg, originalError);
    }

    // Interface doesn't support SIOCGIFADDR, try looking up using ifaddrs
    struct ifaddrs *interfaces = NULL;
    void *sinAddress = NULL;
    if (getifaddrs(&interfaces) == 0) {
      const char *cname = [interfaceName UTF8String];
      struct ifaddrs *addr = interfaces;
      while (addr) {
        if (addr->ifa_addr->sa_family == AF_INET && strcmp(addr->ifa_name, cname) == 0) {
          sinAddress = &((struct sockaddr_in *) addr->ifa_addr)->sin_addr.s_addr;
        }
        addr = addr->ifa_next;
      }
    }
    if (!sinAddress) {
      freeifaddrs(interfaces);
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(msg, originalError);
    }
    IOSByteArray *byteArray = [IOSByteArray arrayWithBytes:(jbyte *)sinAddress count:4];
    freeifaddrs(interfaces);
    return JavaNetInetAddress_getByAddressWithNSString_withByteArray_withInt_(nil, byteArray, 0);
  ]-*/;

  public static int recvfrom(FileDescriptor fd, ByteBuffer buffer, int flags,
      InetSocketAddress srcAddress) throws ErrnoException, SocketException {
    if (buffer.isDirect()) {
      return recvfromBytes(fd, buffer, buffer.position(), buffer.remaining(),
          flags, srcAddress);
    } else {
      return recvfromBytes(fd, NioUtils.unsafeArray(buffer),
          NioUtils.unsafeArrayOffset(buffer) + buffer.position(),
          buffer.remaining(), flags, srcAddress);
    }
  }

  public static int recvfrom(FileDescriptor fd, byte[] bytes, int byteOffset,
      int byteCount, int flags, InetSocketAddress srcAddress)
      throws ErrnoException, SocketException {
    // This indirection isn't strictly necessary, but ensures that our public
    // interface is type safe.
    return recvfromBytes(fd, bytes, byteOffset, byteCount, flags, srcAddress);
  }

  private static native int recvfromBytes(FileDescriptor fd, Object buffer, int byteOffset,
      int byteCount, int flags, InetSocketAddress srcAddress)
      throws ErrnoException, SocketException /*-[
    char *bytes = BytesRW(buffer);
    if (!bytes) {
      return -1;
    }
    struct sockaddr_storage ss;
    socklen_t sl = sizeof(ss);
    memset(&ss, 0, sizeof(ss));
    struct sockaddr* from = (srcAddress) ? (struct sockaddr *) &ss : NULL;
    socklen_t* fromLength = (srcAddress) ? &sl : 0;
    if (byteCount == 0) {
      // iOS doesn't read empty datagram packages, so read one byte and discard it.
      // That works because if the client is reading an empty packet, any bytes in
      // that packet are discarded anyway.
      int _fd = [fd getInt$];
      if (_fd != -1) {
        int type = 0;
        socklen_t size = sizeof(type);
        int rc = LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(
            @"getsockopt", TEMP_FAILURE_RETRY(getsockopt(_fd, SOL_SOCKET, SO_TYPE, &type, &size)));
        if (rc == -1) {
          return rc;
        }
        if (type == SOCK_DGRAM) {
          char b;
          jint recvCount =
              (jint)NET_FAILURE_RETRY(ssize_t, recvfrom, fd, &b, 1, flags, from, fromLength);
          fillInetSocketAddress(recvCount, srcAddress, &ss);
          return recvCount >= 0 ? 0 : recvCount;
        }
      }
    }
    jint recvCount = (jint)NET_FAILURE_RETRY(ssize_t, recvfrom, fd, bytes + byteOffset, byteCount,
        flags, from, fromLength);
    fillInetSocketAddress(recvCount, srcAddress, &ss);
    return recvCount;
  ]-*/;

  public static int sendto(FileDescriptor fd, ByteBuffer buffer, int flags, InetAddress inetAddress,
      int port) throws ErrnoException, SocketException {
    if (buffer.isDirect()) {
      return sendtoBytes(fd, buffer, buffer.position(), buffer.remaining(),
          flags, inetAddress, port);
    } else {
      return sendtoBytes(fd, NioUtils.unsafeArray(buffer),
          NioUtils.unsafeArrayOffset(buffer) + buffer.position(),
          buffer.remaining(), flags, inetAddress, port);
    }
  }

  public static int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount,
      int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException {
    // This indirection isn't strictly necessary, but ensures that our public
    // interface is type safe.
    return sendtoBytes(fd, bytes, byteOffset, byteCount, flags, inetAddress, port);
  }

  private static native int sendtoBytes(FileDescriptor fd, Object buffer, int byteOffset,
      int byteCount, int flags, InetAddress inetAddress, int port)
      throws ErrnoException, SocketException /*-[
    const char *bytes = BytesRO(buffer);
    if (!bytes) {
      return -1;
    }
    struct sockaddr_storage ss;
    socklen_t sa_len = 0;
    if (inetAddress && !inetAddressToSockaddr(inetAddress, port, &ss, &sa_len)) {
      return -1;
    }
    const struct sockaddr *to = inetAddress ? (const struct sockaddr *) &ss : NULL;
    return (int) NET_FAILURE_RETRY(
        ssize_t, sendto, fd, bytes + byteOffset, byteCount, flags, to, sa_len);
  ]-*/;

  public static native void setsockoptByte(FileDescriptor fd, int level, int option, int value)
      throws ErrnoException /*-[
    u_char byte = value;
    int rc = TEMP_FAILURE_RETRY(setsockopt([fd getInt$], level, option, &byte, sizeof(byte)));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"setsockopt", rc);
  ]-*/;

  public static native void setsockoptGroupReq(FileDescriptor fd, int level, int option,
      StructGroupReq structGroupReq) throws ErrnoException /*-[
    struct group_req req;
    memset(&req, 0, sizeof(req));
    req.gr_interface = structGroupReq->gr_interface_;
    // Get the IPv4 or IPv6 multicast address to join or leave.
    JavaNetInetAddress *group = structGroupReq->gr_group_;
    socklen_t sa_len;
    if (!inetAddressToSockaddrVerbatim(group, 0, &req.gr_group, &sa_len)) {
      return;
    }

    int rc = TEMP_FAILURE_RETRY(setsockopt([fd getInt$], level, option, &req, sizeof(req)));
    if (rc == -1 && errno == EINVAL) {
        // Maybe we're a 32-bit binary talking to a 64-bit kernel?
        // glibc doesn't automatically handle this.
        struct group_req64 {
            uint32_t gr_interface;
            uint32_t my_padding;
            struct sockaddr_storage gr_group;
        };
        struct group_req64 req64;
        req64.gr_interface = req.gr_interface;
        memcpy(&req64.gr_group, &req.gr_group, sizeof(req.gr_group));
        rc = TEMP_FAILURE_RETRY(setsockopt([fd getInt$], level, option, &req64, sizeof(req64)));
    }
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"setsockopt", rc);
  ]-*/;

  public static native void setsockoptGroupSourceReq(FileDescriptor fd, int level, int option,
      StructGroupSourceReq structGroupSourceReq) throws ErrnoException /*-[
    socklen_t sa_len;
    struct group_source_req req;
    memset(&req, 0, sizeof(req));
    req.gsr_interface = structGroupSourceReq->gsr_interface_;

    // Get the IPv4 or IPv6 multicast address to join or leave.
    JavaNetInetAddress *group = structGroupSourceReq->gsr_group_;
    if (!inetAddressToSockaddrVerbatim(group, 0, &req.gsr_group, &sa_len)) {
      return;
    }

    // Get the IPv4 or IPv6 multicast address to add to the filter.
    JavaNetInetAddress *source = structGroupSourceReq->gsr_source_;
    if (!inetAddressToSockaddrVerbatim(source, 0, &req.gsr_source, &sa_len)) {
      return;
    }

    int rc = TEMP_FAILURE_RETRY(setsockopt([fd getInt$], level, option, &req, sizeof(req)));
    if (rc == -1 && errno == EINVAL) {
        // Maybe we're a 32-bit binary talking to a 64-bit kernel?
        // glibc doesn't automatically handle this.
        // http://sourceware.org/bugzilla/show_bug.cgi?id=12080
        struct group_source_req64 {
            uint32_t gsr_interface;
            uint32_t my_padding;
            struct sockaddr_storage gsr_group;
            struct sockaddr_storage gsr_source;
        };
        struct group_source_req64 req64;
        req64.gsr_interface = req.gsr_interface;
        memcpy(&req64.gsr_group, &req.gsr_group, sizeof(req.gsr_group));
        memcpy(&req64.gsr_source, &req.gsr_source, sizeof(req.gsr_source));
        rc = TEMP_FAILURE_RETRY(setsockopt([fd getInt$], level, option, &req64, sizeof(req64)));
    }
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"setsockopt", rc);
  ]-*/;

  public static native void setsockoptIfreq(FileDescriptor fd, int level, int option,
      String interfaceName) throws ErrnoException /*-[
    struct ifreq req;
    if (!fillIfreq(interfaceName, &req)) {
      return;
    }
    int rc = TEMP_FAILURE_RETRY(setsockopt([fd getInt$], level, option, &req, sizeof(req)));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"setsockopt", rc);
  ]-*/;

  public static native void setsockoptInt(FileDescriptor fd, int level, int option, int value)
      throws ErrnoException /*-[
    if (level == IPPROTO_IP && option == IP_TOS) {
      return; // Already set on iOS, and setting it fails.
    }
    int _fd = [fd getInt$];
    if (level == SOL_SOCKET && option == SO_REUSEADDR) {
      int type = 0;
      socklen_t size = sizeof(type);
      LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"getsockopt",
          TEMP_FAILURE_RETRY(getsockopt(_fd, SOL_SOCKET, SO_TYPE, &type, &size)));
      if (type == SOCK_DGRAM) {
        LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"setsockopt",
            TEMP_FAILURE_RETRY(setsockopt(_fd, SOL_SOCKET, SO_REUSEPORT, &value, sizeof(value))));
      }
    }
    int rc = TEMP_FAILURE_RETRY(setsockopt(_fd, level, option, &value, sizeof(value)));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"setsockopt", rc);
  ]-*/;

  public static native void setsockoptIpMreqn(FileDescriptor fd, int level, int option,
      int value) throws ErrnoException /*-[
    struct ip_mreqn req;
    memset(&req, 0, sizeof(req));
    req.imr_ifindex = value;
    int rc = TEMP_FAILURE_RETRY(setsockopt([fd getInt$], level, option, &req, sizeof(req)));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"setsockopt", rc);
  ]-*/;

  public static native void setsockoptLinger(FileDescriptor fd, int level, int option,
      StructLinger structLinger) throws ErrnoException /*-[
    struct linger value;
    value.l_onoff = structLinger->l_onoff_;
    value.l_linger = structLinger->l_linger_;
    int rc = TEMP_FAILURE_RETRY(setsockopt([fd getInt$], level, option, &value, sizeof(value)));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"setsockopt", rc);
  ]-*/;

  public static native void setsockoptTimeval(FileDescriptor fd, int level, int option,
      StructTimeval structTimeval) throws ErrnoException /*-[
    struct timeval value;
    value.tv_sec = (long) structTimeval->tv_sec_;
    value.tv_usec = (int) structTimeval->tv_usec_;
    int rc = TEMP_FAILURE_RETRY(setsockopt([fd getInt$], level, option, &value, sizeof(value)));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"setsockopt", rc);
  ]-*/;

  private static void updateInetSocketAddress(InetSocketAddress socketAddr,
      InetAddress addr, int port) {
    // Fill in socket values using reflection, rather than change an immutable API.
    try {
      Field addrField = InetSocketAddress.class.getDeclaredField("addr");
      addrField.setAccessible(true);
      addrField.set(socketAddr, addr);
      Field portField = InetSocketAddress.class.getDeclaredField("port");
      portField.setAccessible(true);
      portField.set(socketAddr, port);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  // Create a compile-time link to URLImpl to pull it into binaries that already link other
  // java.net classes. Most of java.net depends on NetworkOs so this declaration should be
  // sufficient to ensure that URLImpl is loaded.
  private static final Class<?> unused = URLImpl.class;
}
