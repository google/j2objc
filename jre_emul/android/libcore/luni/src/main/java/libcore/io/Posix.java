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

import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.NioUtils;

import libcore.util.MutableInt;
import libcore.util.MutableLong;

/*-[
#include "BufferUtils.h"
#include "Portability.h"
#include "TempFailureRetry.h"
#include "java/io/File.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/System.h"
#include "java/net/Inet6Address.h"
#include "java/net/InetAddress.h"
#include "java/net/InetUnixAddress.h"
#include "libcore/io/AsynchronousCloseMonitor.h"
#include "libcore/io/StructLinger.h"
#include "libcore/io/StructPollfd.h"
#include "libcore/io/StructStatVfs.h"
#include "libcore/io/StructTimeval.h"

#include <fcntl.h>
#include <poll.h>
#include <arpa/inet.h>
#include <net/if.h>
#include <netdb.h>
#include <ifaddrs.h>
#include <sys/ioctl.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/uio.h>
#include <sys/un.h>
#include <sys/utsname.h>
#include <termios.h>
#include <unistd.h>

static inline BOOL throwIfClosed(JavaIoFileDescriptor *fd) {
  if ([fd getInt$] == -1) {
    @throw AUTORELEASE([[JavaNetSocketException alloc] initWithNSString:@"Socket closed"]);
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

public final class Posix implements Os {
  Posix() { }

  /*-[
  JavaNetInetAddress *sockaddrToInetAddress(const struct sockaddr_storage *ss, int *port);

  static JavaNetInetSocketAddress *makeSocketAddress(const struct sockaddr_storage *ss) {
    int port;
    JavaNetInetAddress *inetAddress = sockaddrToInetAddress(ss, &port);
    if (!inetAddress) {
      return nil;
    }
    return AUTORELEASE([[JavaNetInetSocketAddress alloc]
                        initWithJavaNetInetAddress:inetAddress withInt:port]);
  }

  static LibcoreIoStructStat *makeStructStat(const struct stat *sb) {
      return AUTORELEASE([[LibcoreIoStructStat alloc]
                          initWithLong:sb->st_dev
                              withLong:sb->st_ino
                               withInt:sb->st_mode
                              withLong:sb->st_nlink
                               withInt:sb->st_uid
                               withInt:sb->st_gid
                              withLong:sb->st_rdev
                              withLong:sb->st_size
                              withLong:sb->st_atime
                              withLong:sb->st_mtime
                              withLong:sb->st_ctime
                              withLong:sb->st_blksize
                              withLong:sb->st_blocks]);
  }

  static LibcoreIoStructStatVfs *makeStructStatVfs(const struct statvfs *sb) {
    return AUTORELEASE([[LibcoreIoStructStatVfs alloc]
                        initWithLong:(long long)sb->f_bsize
                            withLong:(long long)sb->f_frsize
                            withLong:(long long)sb->f_blocks
                            withLong:(long long)sb->f_bfree
                            withLong:(long long)sb->f_bavail
                            withLong:(long long)sb->f_files
                            withLong:(long long)sb->f_ffree
                            withLong:(long long)sb->f_favail
                            withLong:(long long)sb->f_fsid
                            withLong:(long long)sb->f_flag
                            withLong:255LL]);  // __DARWIN_MAXNAMLEN
  }

  static LibcoreIoStructUtsname *makeStructUtsname(const struct utsname *buf) {
    NSString *sysname = [NSString stringWithUTF8String:buf->sysname];
    NSString *nodename = [NSString stringWithUTF8String:buf->nodename];
    NSString *release = [NSString stringWithUTF8String:buf->release];
    NSString *version = [NSString stringWithUTF8String:buf->version];
    NSString *machine = [NSString stringWithUTF8String:buf->machine];
    return AUTORELEASE([[LibcoreIoStructUtsname alloc] initWithNSString:sysname
                                                           withNSString:nodename
                                                           withNSString:release
                                                           withNSString:version
                                                           withNSString:machine]);
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
    LibcoreIoPosix_updateInetSocketAddressWithJavaNetInetSocketAddress_withJavaNetInetAddress_withInt_(
        srcAddress, RETAIN_(sender), port);
    return YES;
 }

  static id doStat(NSString *path, BOOL isLstat) {
    if (!path) {
      return nil;
    }
    const char* cpath = absolutePath(path);
    struct stat sb;
    int rc = isLstat ? TEMP_FAILURE_RETRY(lstat(cpath, &sb))
                     : TEMP_FAILURE_RETRY(stat(cpath, &sb));
    if (rc == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(
          (isLstat ? @"lstat" : @"stat"), errno);
    }
    return makeStructStat(&sb);
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
      @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] initWithNSString:errMsg]);
    }
    if (port != NULL) {
      *port = sin_port;
    }

    IOSByteArray *byteArray =
        [IOSByteArray arrayWithBytes:(jbyte *)rawAddress count:(jint)addressLength];

    if (ss->ss_family == AF_UNIX) {
        // Note that we get here for AF_UNIX sockets on accept(2). The unix(7) man page claims
        // that the peer's sun_path will contain the path, but in practice it doesn't, and the
        // peer length is returned as 2 (meaning only the sun_family field was set).
        return AUTORELEASE([[JavaNetInetUnixAddress alloc] initWithByteArray:byteArray]);
    }
    return JavaNetInetAddress_getByAddressWithNSString_withByteArray_withInt_(
        nil, byteArray, scope_id);
  }

  static BOOL inetAddressToSockaddrImpl(JavaNetInetAddress *inetAddress, int port,
      struct sockaddr_storage *ss, socklen_t *sa_len, BOOL map) {
    memset(ss, 0, sizeof(struct sockaddr_storage));
    *sa_len = 0;
    nil_chk(inetAddress);

    // Get the address family.
    ss->ss_family = [inetAddress getFamily];
    if (ss->ss_family == AF_UNSPEC) {
      *sa_len = sizeof(ss->ss_family);
      return YES; // Job done!
    }

    // Check this is an address family we support.
    if (ss->ss_family != AF_INET && ss->ss_family != AF_INET6 && ss->ss_family != AF_UNIX) {
      NSString *errMsg =
          [NSString stringWithFormat:@"inetAddressToSockaddr bad family: %i", ss->ss_family];
      @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] initWithNSString:errMsg]);
    }

    // Handle the AF_UNIX special case.
    if (ss->ss_family == AF_UNIX) {
      struct sockaddr_un *sun = (struct sockaddr_un *)ss;

      jint path_length = inetAddress->ipaddress_->size_;
      if ((size_t)path_length >= sizeof(sun->sun_path)) {
        NSString *errMsg =
            [NSString stringWithFormat:@"inetAddressToSockaddr path too long for AF_UNIX: %d",
                path_length];
        @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] initWithNSString:errMsg]);
      }

      // Copy the bytes...
      jbyte* dst = (jbyte *)sun->sun_path;
      memset(dst, 0, sizeof(sun->sun_path));
      [inetAddress->ipaddress_ getBytes:dst length:path_length];
      *sa_len = sizeof(sun->sun_path);
      return YES;
    }

    // We use AF_INET6 sockets, so we want an IPv6 address (which may be a IPv4-mapped address).
    struct sockaddr_in6 *sin6 = (struct sockaddr_in6 *) ss;
    sin6->sin6_port = htons(port);
    if (ss->ss_family == AF_INET6) {
      // IPv6 address. Copy the bytes...
      jbyte *dst = (jbyte *)sin6->sin6_addr.s6_addr;
      [inetAddress->ipaddress_ getBytes:dst length:16];
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
      // Copy the bytes...
      jbyte *dst = (jbyte *)&sin6->sin6_addr.s6_addr[12];
      [inetAddress->ipaddress_ getBytes:dst length:4];
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
      jbyte *dst = (jbyte *)&sin->sin_addr.s_addr;
      [inetAddress->ipaddress_ getBytes:dst length:4];
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

  BOOL setBlocking(int fd, bool blocking) {
    int flags = fcntl(fd, F_GETFL);
    if (flags == -1) {
      return NO;
    }

    if (!blocking) {
        flags |= O_NONBLOCK;
    } else {
        flags &= ~O_NONBLOCK;
    }

    int rc = fcntl(fd, F_SETFL, flags);
    return (rc != -1);
  }

  const char *absolutePath(NSString *path) {
    if ([path length] == 0) {
      return "";
    }
    if ([path characterAtIndex:0] != '/') {
      JavaIoFile *f = [[JavaIoFile alloc] initWithNSString:path];
      path = [f getAbsolutePath];
      RELEASE_(f);
    }
    return [path fileSystemRepresentation];
  }
  ]-*/

  private static void throwErrnoException(String message, int errorCode) throws ErrnoException {
    throw new ErrnoException(message, errorCode);
  }

  private static native int throwIfMinusOne(String name, int resultCode) throws ErrnoException /*-[
    if (resultCode == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(name, errno);
    }
    return resultCode;
  ]-*/;

  public native FileDescriptor accept(FileDescriptor fd, InetSocketAddress peerAddress)
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
    JavaIoFileDescriptor *newFd = AUTORELEASE([[JavaIoFileDescriptor alloc] init]);
    [newFd setInt$WithInt:clientFd];
    return newFd;
  ]-*/;

  public native boolean access(String path, int mode) throws ErrnoException /*-[
    if (!path) {
      return NO;
    }
    const char* cpath = absolutePath(path);
    int rc = TEMP_FAILURE_RETRY(access(cpath, mode));
    if (rc == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"access", errno);
    }
    return (rc == 0);
  ]-*/;

  public native void bind(FileDescriptor fd, InetAddress address, int port)
      throws ErrnoException, SocketException /*-[
    struct sockaddr_storage ss;
    socklen_t sa_len;
    if (!inetAddressToSockaddr(address, port, &ss, &sa_len)) {
      return;
    }
    const struct sockaddr* sa = (const struct sockaddr *) &ss;
    (void) NET_FAILURE_RETRY(int, bind, fd, sa, sa_len);
  ]-*/;

  /**
   * Like access(), but returns false on error instead of throwing an exception.
   */
  public native boolean canAccess(String path, int mode) /*-[
    if (!path) {
      return NO;
    }
    const char* cpath = absolutePath(path);
    return (TEMP_FAILURE_RETRY(access(cpath, mode)) == 0);
  ]-*/;

  public native void chmod(String path, int mode) throws ErrnoException /*-[
    if (path) {
      const char* cpath = absolutePath(path);
      int rc = TEMP_FAILURE_RETRY(chmod(cpath, mode));
      LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"chmod", rc);
    }
  ]-*/;

  public native void chown(String path, int uid, int gid) throws ErrnoException /*-[
    if (path) {
      const char* cpath = absolutePath(path);
      int rc = TEMP_FAILURE_RETRY(chown(cpath, uid, gid));
      LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"chown", rc);
    }
  ]-*/;

  public native void close(FileDescriptor javaFd) throws ErrnoException /*-[
    // Get the FileDescriptor's 'fd' field and clear it.
    // We need to do this before we can throw an IOException.
    int fd = [javaFd getInt$];
    [javaFd setInt$WithInt:-1];
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"close", close(fd));
  ]-*/;

  public native void connect(FileDescriptor fd, InetAddress address, int port)
      throws ErrnoException, SocketException /*-[
    struct sockaddr_storage ss;
    socklen_t sa_len;
    if (!inetAddressToSockaddr(address, port, &ss, &sa_len)) {
      return;
    }
    const struct sockaddr* sa = (const struct sockaddr *) &ss;
    (void) NET_FAILURE_RETRY(int, connect, fd, sa, sa_len);
  ]-*/;

  public native FileDescriptor dup(FileDescriptor oldFd) throws ErrnoException /*-[
    int nativeFd = TEMP_FAILURE_RETRY(dup([oldFd getInt$]));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"dup", nativeFd);
    JavaIoFileDescriptor *newFd = AUTORELEASE([[JavaIoFileDescriptor alloc] init]);
    [newFd setInt$WithInt:nativeFd];
    return newFd;
  ]-*/;

  public native FileDescriptor dup2(FileDescriptor oldFd, int newNativeFd)
      throws ErrnoException /*-[
    int nativeFd = TEMP_FAILURE_RETRY(dup2([oldFd getInt$], newNativeFd));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"dup2", nativeFd);
    JavaIoFileDescriptor *newFd = AUTORELEASE([[JavaIoFileDescriptor alloc] init]);
    [newFd setInt$WithInt:nativeFd];
    return newFd;
  ]-*/;

  public native void fchmod(FileDescriptor fd, int mode) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(fchmod([fd getInt$], mode));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"fchmod", rc);
  ]-*/;

  public native void fchown(FileDescriptor fd, int uid, int gid) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(fchown([fd getInt$], uid, gid));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"fchown", rc);
  ]-*/;

  public native int fcntlFlock(FileDescriptor fd, int cmd, StructFlock arg)
      throws ErrnoException /*-[
    struct flock64 lock;
    memset(&lock, 0, sizeof(lock));
    lock.l_type = arg->l_type_;
    lock.l_whence = arg->l_whence_;
    lock.l_start = arg->l_start_;
    lock.l_len = arg->l_len_;
    lock.l_pid = arg->l_pid_;

    int rc = TEMP_FAILURE_RETRY(fcntl((int) [fd getInt$], cmd, &lock));
    if (rc == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"fcntl", errno);
    }
    arg->l_type_ = lock.l_type;
    arg->l_whence_ = lock.l_whence;
    arg->l_start_ = lock.l_start;
    arg->l_len_ = lock.l_len;
    arg->l_pid_ = lock.l_pid;
    return rc;
  ]-*/;

  public native int fcntlLong(FileDescriptor fd, int cmd, long arg) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(fcntl([fd getInt$], cmd, arg));
    return LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"fcntl", rc);
  ]-*/;

  public native int fcntlVoid(FileDescriptor fd, int cmd) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(fcntl([fd getInt$], cmd));
    return LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"fcntl", rc);
  ]-*/;

  public native void fdatasync(FileDescriptor fd) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(fdatasync([fd getInt$]));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"fdatasync", rc);
  ]-*/;

  public native StructStat fstat(FileDescriptor fd) throws ErrnoException /*-[
    struct stat sb;
    int rc = TEMP_FAILURE_RETRY(fstat([fd getInt$], &sb));
    if (rc == -1) {
        LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"fstat", rc);
    }
    return makeStructStat(&sb);
  ]-*/;

  public native void fsync(FileDescriptor fd) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(fsync([fd getInt$]));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"fsync", rc);
  ]-*/;

  public native void ftruncate(FileDescriptor fd, long length) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(ftruncate([fd getInt$], (off_t) length));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"ftruncate", rc);
  ]-*/;

  public native String gai_strerror(int error) /*-[
    return [NSString stringWithUTF8String:gai_strerror(error)];
  ]-*/;

  public native InetAddress[] getaddrinfo(String node, StructAddrinfo javaHints)
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
      @throw AUTORELEASE([[LibcoreIoGaiException alloc]
                          initWithNSString:@"getaddrinfo" withInt:rc]);
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
      struct sockaddr_storage address = *(struct sockaddr_storage *) ai->ai_addr;
      JavaNetInetAddress *inetAddress = sockaddrToInetAddress(&address, NULL);
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

  public native String getnameinfo(InetAddress address, int flags) throws GaiException /*-[
    struct sockaddr_storage ss;
    socklen_t sa_len;
    if (!inetAddressToSockaddrVerbatim(address, 0, &ss, &sa_len)) {
      return nil;
    }
    char buf[NI_MAXHOST]; // NI_MAXHOST is longer than INET6_ADDRSTRLEN.
    errno = 0;
    int rc = getnameinfo((struct sockaddr *) &ss, sa_len, buf, sizeof(buf), NULL, 0, flags);
    if (rc != 0) {
      @throw AUTORELEASE([[LibcoreIoGaiException alloc]
                          initWithNSString:@"getnameinfo" withInt:rc]);
    }
    return [NSString stringWithUTF8String:buf];
  ]-*/;

  public native SocketAddress getsockname(FileDescriptor fd) throws ErrnoException /*-[
    return doGetSockName([fd getInt$], YES);
  ]-*/;

  public native int getsockoptByte(FileDescriptor fd, int level, int option)
      throws ErrnoException /*-[
    u_char result = 0;
    socklen_t size = sizeof(result);
    int rc = TEMP_FAILURE_RETRY(getsockopt([fd getInt$], level, option, &result, &size));
    if (rc == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"getsockopt", rc);
    }
    return result;
  ]-*/;

  public native InetAddress getsockoptInAddr(FileDescriptor fd, int level, int option)
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

  public native int getsockoptInt(FileDescriptor fd, int level, int option)
      throws ErrnoException /*-[
    int result = 0;
    socklen_t size = sizeof(result);
    int rc = TEMP_FAILURE_RETRY(getsockopt([fd getInt$], level, option, &result, &size));
    if (rc == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"getsockopt", rc);
    }
    return result;
  ]-*/;

  public native StructLinger getsockoptLinger(FileDescriptor fd, int level, int option)
      throws ErrnoException /*-[
    struct linger l;
    socklen_t size = sizeof(l);
    memset(&l, 0, size);
    int rc = TEMP_FAILURE_RETRY(getsockopt([fd getInt$], level, option, &l, &size));
    if (rc == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"getsockopt", rc);
    }
    return AUTORELEASE([[LibcoreIoStructLinger alloc] initWithInt:l.l_onoff withInt:l.l_linger]);
  ]-*/;

  public native StructTimeval getsockoptTimeval(FileDescriptor fd, int level,
      int option) throws ErrnoException /*-[
    struct timeval tv;
    socklen_t size = sizeof(tv);
    memset(&tv, 0, size);
    int rc = TEMP_FAILURE_RETRY(getsockopt([fd getInt$], level, option, &tv, &size));
    if (rc == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"getsockopt", rc);
    }
    return AUTORELEASE([[LibcoreIoStructTimeval alloc] initWithLong:tv.tv_sec withLong:tv.tv_usec]);
  ]-*/;

  public native String if_indextoname(int index) /*-[
    char buf[IF_NAMESIZE];
    char *name = if_indextoname(index, buf);
    // if_indextoname(3) returns NULL on failure. There's no useful information in errno,
    // so we don't bother throwing. Callers can null-check.
    return name ? [NSString stringWithUTF8String:name] : nil;
]-*/;

  public native InetAddress inet_pton(int family, String address) /*-[
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

  public native InetAddress ioctlInetAddress(FileDescriptor fd, int cmd,
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
    freeifaddrs(interfaces);
    if (!sinAddress) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(msg, originalError);
    }
    IOSByteArray *byteArray = [IOSByteArray arrayWithBytes:(jbyte *)sinAddress count:4];
    return JavaNetInetAddress_getByAddressWithNSString_withByteArray_withInt_(nil, byteArray, 0);
  ]-*/;

  public native int ioctlInt(FileDescriptor fd, int cmd, MutableInt javaArg)
      throws ErrnoException /*-[
    int arg = javaArg->value_;
    int rc = TEMP_FAILURE_RETRY(ioctl([fd getInt$], cmd, &arg));
    if (rc == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"ioctl", rc);
    }
    javaArg->value_ = arg;
    return rc;
  ]-*/;

  public native boolean isatty(FileDescriptor fd) /*-[
    return TEMP_FAILURE_RETRY(isatty([fd getInt$])) == 1;
  ]-*/;

  public native long lseek(FileDescriptor fd, long offset, int whence) throws ErrnoException /*-[
    off_t rc = TEMP_FAILURE_RETRY(lseek([fd getInt$], offset, whence));
    if (rc == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"lseek", errno);
    }
    return rc;
  ]-*/;

  public native StructStat lstat(String path) throws ErrnoException /*-[
    return doStat(path, YES);
  ]-*/;

  public native void listen(FileDescriptor fd, int backlog) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(listen([fd getInt$], backlog));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"listen", rc);
  ]-*/;

  public native void mincore(long address, long byteCount, byte[] vector) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(mincore(
        (caddr_t) address, (size_t) byteCount, (char *)vector->buffer_));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"mincore", rc);
  ]-*/;

  public native void mkdir(String path, int mode) throws ErrnoException /*-[
    if (path) {
      const char* cpath = absolutePath(path);
      int rc = TEMP_FAILURE_RETRY(mkdir(cpath, mode));
      LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"mkdir", rc);
    }
  ]-*/;

  public native void mlock(long address, long byteCount) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(mlock((void *) address, (size_t) byteCount));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"mlock", rc);
  ]-*/;

  public native long mmap(long address, long byteCount, int prot, int flags,
      FileDescriptor fd, long offset) throws ErrnoException /*-[
    void* ptr = mmap((void *) address, (size_t) byteCount, prot, flags, [fd getInt$], offset);
    if (ptr == MAP_FAILED) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"mmap", errno);
    }
    return (long long) ptr;
  ]-*/;

  public native void msync(long address, long byteCount, int flags) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(msync((void *) address, (size_t) byteCount, flags));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"msync", rc);
  ]-*/;

  public native void munlock(long address, long byteCount) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(munlock((void *) address, (size_t) byteCount));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"munlock", rc);
  ]-*/;

  public native void munmap(long address, long byteCount) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(munmap((void *) address, (size_t) byteCount));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"munmap", rc);
  ]-*/;

  public native FileDescriptor open(String path, int flags, int mode) throws ErrnoException /*-[
    if (!path) {
      return nil;
    }
    const char* cpath = absolutePath(path);
    int nativeFd = TEMP_FAILURE_RETRY(open(cpath, flags, mode));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"open", nativeFd);
    JavaIoFileDescriptor *newFd = AUTORELEASE([[JavaIoFileDescriptor alloc] init]);
    [newFd setInt$WithInt:nativeFd];
    return newFd;
  ]-*/;

  public native FileDescriptor[] pipe() throws ErrnoException /*-[
    int fds[2];
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"pipe", TEMP_FAILURE_RETRY(pipe(&fds[0])));
    IOSObjectArray *result = [IOSObjectArray arrayWithLength:2 type:JavaIoFileDescriptor_class_()];
    for (int i = 0; i < 2; ++i) {
      JavaIoFileDescriptor *fd = AUTORELEASE([[JavaIoFileDescriptor alloc] init]);
      [fd setInt$WithInt:fds[i]];
      [result replaceObjectAtIndex:i withObject:fd];
    }
    return result;
  ]-*/;

  public native int poll(StructPollfd[] fds, int timeoutMs) throws ErrnoException /*-[
    jint count = fds->size_;
    struct pollfd *pollFds = (struct pollfd *)calloc(count, sizeof(struct pollfd));
    for (jint i = 0; i < count; i++) {
      LibcoreIoStructPollfd *javaPollFd = [fds objectAtIndex:i];
      if (javaPollFd) {
        pollFds[i].fd = [javaPollFd->fd_ getInt$];
        pollFds[i].events = javaPollFd->events_;
      }
    }
    int rc = poll(pollFds, count, timeoutMs);
    if (rc == -1) {
      free(pollFds);
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"poll", rc);
    }
    for (jint i = 0; i < count; i++) {
      LibcoreIoStructPollfd *javaPollFd = [fds objectAtIndex:i];
      if (javaPollFd) {
        javaPollFd->revents_ = pollFds[i].revents;
      }
    }
    free(pollFds);
    return rc;
  ]-*/;

  public int pread(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException {
    if (buffer.isDirect()) {
      return preadBytes(fd, buffer, buffer.position(), buffer.remaining(), offset);
    } else {
      return preadBytes(fd, NioUtils.unsafeArray(buffer),
          NioUtils.unsafeArrayOffset(buffer) + buffer.position(), buffer.remaining(), offset);
    }
  }

  public int pread(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset)
      throws ErrnoException {
    return preadBytes(fd, bytes, byteOffset, byteCount, offset);
  }

  private native int preadBytes(FileDescriptor fd, Object buffer, int bufferOffset, int byteCount,
      long offset) throws ErrnoException /*-[
    char *bytes = BytesRW(buffer);
    if (!bytes) {
        return -1;
    }
    int rc =
      TEMP_FAILURE_RETRY(pread64([fd getInt$], bytes + bufferOffset, byteCount, offset));
    return LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"pread", rc);
  ]-*/;

  public int pwrite(FileDescriptor fd, ByteBuffer buffer, long offset)
      throws ErrnoException {
    if (buffer.isDirect()) {
      return pwriteBytes(fd, buffer, buffer.position(), buffer.remaining(),
          offset);
    } else {
      return pwriteBytes(fd, NioUtils.unsafeArray(buffer),
          NioUtils.unsafeArrayOffset(buffer) + buffer.position(),
          buffer.remaining(), offset);
    }
  }

  public int pwrite(FileDescriptor fd, byte[] bytes, int byteOffset,
      int byteCount, long offset) throws ErrnoException {
    return pwriteBytes(fd, bytes, byteOffset, byteCount, offset);
  }

  private native int pwriteBytes(FileDescriptor fd, Object buffer,
      int bufferOffset, int byteCount, long offset) throws ErrnoException /*-[
    const char *bytes = BytesRO(buffer);
    if (!bytes) {
        return -1;
    }
    if (byteCount + offset < 0) {  // If buffer overflow.
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"pwrite", ERANGE);
    }
    int rc =
      TEMP_FAILURE_RETRY(pwrite64([fd getInt$], bytes + bufferOffset, byteCount, offset));
    return LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"pwrite", rc);
  ]-*/;

  public int read(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException {
    if (buffer.isDirect()) {
      return readBytes(fd, buffer, buffer.position(), buffer.remaining());
    } else {
      return readBytes(fd, NioUtils.unsafeArray(buffer),
          NioUtils.unsafeArrayOffset(buffer) + buffer.position(), buffer.remaining());
    }
  }

  public int read(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount)
      throws ErrnoException {
    return readBytes(fd, bytes, byteOffset, byteCount);
  }

  private native int readBytes(FileDescriptor fd, Object buffer, int offset, int byteCount)
      throws ErrnoException /*-[
    char *bytes = BytesRW(buffer);
    if (!bytes) {
      return -1;
    }
    int rc = TEMP_FAILURE_RETRY(read([fd getInt$], bytes + offset, byteCount));
    return LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"read", rc);
  ]-*/;

  public native int readv(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts)
      throws ErrnoException /*-[
    int nIoVecs = buffers->size_;
    struct iovec *ioVecs = (struct iovec *)malloc(nIoVecs * sizeof (struct iovec));
    for (int i = 0; i < nIoVecs; i++) {
      char *bytes = BytesRW([buffers objectAtIndex:i]);
      if (!bytes) {
        free(ioVecs);
        return -1;
      }
      ioVecs[i].iov_base = ((char *) bytes) + IOSIntArray_Get(offsets, i);
      ioVecs[i].iov_len = IOSIntArray_Get(byteCounts, i);
    }
    int rc = TEMP_FAILURE_RETRY(readv([fd getInt$], ioVecs, nIoVecs));
    free(ioVecs);
    return LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"readv", rc);
  ]-*/;

  public native String realpath(String path) /*-[
    if (!path) {
      return nil;
    }
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSString *standardizedPath = [path stringByStandardizingPath];
    if (![fileManager fileExistsAtPath:standardizedPath]) {
      return standardizedPath;
    }
    const char* cpath = [path UTF8String];
    char *realPath = realpath(cpath, NULL);
    if (!realPath) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"realpath", errno);
    }
    NSString *result =
        [[NSFileManager defaultManager] stringWithFileSystemRepresentation:realPath
                                                                    length:strlen(realPath)];
    free(realPath);
    return result;
  ]-*/;

  public int recvfrom(FileDescriptor fd, ByteBuffer buffer, int flags,
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

  public int recvfrom(FileDescriptor fd, byte[] bytes, int byteOffset,
      int byteCount, int flags, InetSocketAddress srcAddress)
      throws ErrnoException, SocketException {
    // This indirection isn't strictly necessary, but ensures that our public
    // interface is type safe.
    return recvfromBytes(fd, bytes, byteOffset, byteCount, flags, srcAddress);
  }

  private native int recvfromBytes(FileDescriptor fd, Object buffer, int byteOffset, int byteCount,
      int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException /*-[
    char *bytes = BytesRW(buffer);
    if (!bytes) {
      return -1;
    }
    struct sockaddr_storage ss;
    socklen_t sl = sizeof(ss);
    memset(&ss, 0, sizeof(ss));
    struct sockaddr* from = (srcAddress) ? (struct sockaddr *) &ss : NULL;
    socklen_t* fromLength = (srcAddress) ? &sl : 0;
    int recvCount = (int) NET_FAILURE_RETRY(ssize_t, recvfrom, fd, bytes + byteOffset, byteCount,
        flags, from, fromLength);
    fillInetSocketAddress(recvCount, srcAddress, &ss);
    return recvCount;
  ]-*/;

  public native void remove(String path) throws ErrnoException /*-[
    if (path) {
      const char* cpath = absolutePath(path);
      int rc = TEMP_FAILURE_RETRY(remove(cpath));
      LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"remove", rc);
    }
  ]-*/;

  public native void rename(String oldPath, String newPath) throws ErrnoException /*-[
    if (oldPath && newPath) {
      const char* cOldPath = absolutePath(oldPath);
      const char* cNewPath = absolutePath(newPath);
      int rc = TEMP_FAILURE_RETRY(rename(cOldPath, cNewPath));
      LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"rename", rc);
    }
  ]-*/;

  public native long sendfile(FileDescriptor outFd, FileDescriptor inFd, MutableLong inOffset,
      long byteCount) throws ErrnoException /*-[
    off_t offset = 0;
    off_t* offsetPtr = NULL;
    if (inOffset != NULL) {
      offset = inOffset->value_;
      offsetPtr = &offset;
    }
    int rc = TEMP_FAILURE_RETRY(sendfile_([outFd getInt$], [inFd getInt$],
        offsetPtr, (size_t) byteCount));
    if (inOffset != NULL) {
      inOffset->value_ = offset;
    }
    return LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"sendfile", rc);
  ]-*/;

  public int sendto(FileDescriptor fd, ByteBuffer buffer, int flags, InetAddress inetAddress,
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

  public int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags,
      InetAddress inetAddress, int port) throws ErrnoException, SocketException {
    // This indirection isn't strictly necessary, but ensures that our public
    // interface is type safe.
    return sendtoBytes(fd, bytes, byteOffset, byteCount, flags, inetAddress, port);
  }

  private native int sendtoBytes(FileDescriptor fd, Object buffer, int byteOffset, int byteCount,
      int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException /*-[
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

  public native void setsockoptByte(FileDescriptor fd, int level, int option, int value)
      throws ErrnoException /*-[
    u_char byte = value;
    int rc = TEMP_FAILURE_RETRY(setsockopt([fd getInt$], level, option, &byte, sizeof(byte)));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"setsockopt", rc);
  ]-*/;

  public native void setsockoptGroupReq(FileDescriptor fd, int level, int option,
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

  public native void setsockoptGroupSourceReq(FileDescriptor fd, int level, int option,
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

  public native void setsockoptIfreq(FileDescriptor fd, int level, int option,
      String interfaceName) throws ErrnoException /*-[
    struct ifreq req;
    if (!fillIfreq(interfaceName, &req)) {
      return;
    }
    int rc = TEMP_FAILURE_RETRY(setsockopt([fd getInt$], level, option, &req, sizeof(req)));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"setsockopt", rc);
  ]-*/;

  public native void setsockoptInt(FileDescriptor fd, int level, int option, int value)
      throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(setsockopt([fd getInt$], level, option, &value, sizeof(value)));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"setsockopt", rc);
  ]-*/;

  public native void setsockoptIpMreqn(FileDescriptor fd, int level, int option,
      int value) throws ErrnoException /*-[
    struct ip_mreqn req;
    memset(&req, 0, sizeof(req));
    req.imr_ifindex = value;
    int rc = TEMP_FAILURE_RETRY(setsockopt([fd getInt$], level, option, &req, sizeof(req)));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"setsockopt", rc);
  ]-*/;

  public native void setsockoptLinger(FileDescriptor fd, int level, int option,
      StructLinger structLinger) throws ErrnoException /*-[
    struct linger value;
    value.l_onoff = structLinger->l_onoff_;
    value.l_linger = structLinger->l_linger_;
    int rc = TEMP_FAILURE_RETRY(setsockopt([fd getInt$], level, option, &value, sizeof(value)));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"setsockopt", rc);
  ]-*/;

  public native void setsockoptTimeval(FileDescriptor fd, int level, int option,
      StructTimeval structTimeval) throws ErrnoException /*-[
    struct timeval value;
    value.tv_sec = (long) structTimeval->tv_sec_;
    value.tv_usec = (int) structTimeval->tv_usec_;
    int rc = TEMP_FAILURE_RETRY(setsockopt([fd getInt$], level, option, &value, sizeof(value)));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"setsockopt", rc);
  ]-*/;

  public native void shutdown(FileDescriptor fd, int how) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(shutdown([fd getInt$], how));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"shutdown", rc);
  ]-*/;

  public native FileDescriptor socket(int domain, int type, int protocol) throws ErrnoException /*-[
    int nativeFd = TEMP_FAILURE_RETRY(socket(domain, type, protocol));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"socket", nativeFd);
    JavaIoFileDescriptor *newFd = AUTORELEASE([[JavaIoFileDescriptor alloc] init]);
    [newFd setInt$WithInt:nativeFd];
    return newFd;
  ]-*/;

  public native void socketpair(int domain, int type, int protocol, FileDescriptor fd1,
      FileDescriptor fd2) throws ErrnoException /*-[
    int fds[2];
    int rc = TEMP_FAILURE_RETRY(socketpair(domain, type, protocol, fds));
    if (rc != -1) {
      [fd1 setInt$WithInt:fds[0]];
      [fd2 setInt$WithInt:fds[1]];
    }
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"socketpair", rc);
  ]-*/;

  public native StructStat stat(String path) throws ErrnoException /*-[
    return doStat(path, NO);
  ]-*/;

  public native StructStatVfs statvfs(String path) throws ErrnoException /*-[
    if (!path) {
      return nil;
    }
    const char* cpath = absolutePath(path);
    struct statvfs sb;
    int rc = TEMP_FAILURE_RETRY(statvfs(cpath, &sb));
    if (rc == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"statvfs", errno);
    }
    return makeStructStatVfs(&sb);
  ]-*/;

  public native String strerror(int errno) /*-[
    char buffer[BUFSIZ];
    int ret = strerror_r(errno_, buffer, BUFSIZ);
    if (ret != 0) {  // If not successful...
      snprintf(buffer, BUFSIZ, "errno %d", errno_);
    }
    return [NSString stringWithUTF8String:buffer];
  ]-*/;

  public native long sysconf(int name) /*-[
    // Since -1 is a valid result from sysconf(3), detecting failure is a little more awkward.
    errno = 0;
    long result = sysconf(name);
    if (result == -1L && errno == EINVAL) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(@"sysconf", errno);
    }
    return result;
  ]-*/;

  public native void symlink(String oldPath, String newPath) throws ErrnoException /*-[
    if (oldPath && newPath) {
      const char* cOldPath = [oldPath UTF8String];
      const char* cNewPath = [newPath UTF8String];
      int rc = TEMP_FAILURE_RETRY(symlink(cOldPath, cNewPath));
      LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"symlink", rc);
    }
  ]-*/;

  public native void tcdrain(FileDescriptor fd) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(tcdrain([fd getInt$]));
    LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"fcntl", rc);
  ]-*/;

  public native StructUtsname uname() /*-[
    struct utsname buf;
    if (TEMP_FAILURE_RETRY(uname(&buf)) == -1) {
      return nil;
    }
    return makeStructUtsname(&buf);
  ]-*/;

  public int write(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException {
    if (buffer.isDirect()) {
      return writeBytes(fd, buffer, buffer.position(), buffer.remaining());
    } else {
      return writeBytes(fd, NioUtils.unsafeArray(buffer),
          NioUtils.unsafeArrayOffset(buffer) + buffer.position(), buffer.remaining());
    }
  }

  public native int write(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount)
      throws ErrnoException /*-[
    if (!bytes) {
      return -1;
    }
    IOSArray_checkRange(bytes->size_, byteOffset, byteCount);
    int rc =
        TEMP_FAILURE_RETRY(write([fd getInt$], bytes->buffer_ + byteOffset, byteCount));
    return LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"write", rc);
  ]-*/;

  private native int writeBytes(FileDescriptor fd, Object buffer, int offset, int byteCount)
      throws ErrnoException /*-[
    const char *bytes = BytesRO(buffer);
    if (!bytes) {
      return -1;
    }
    int rc = TEMP_FAILURE_RETRY(write([fd getInt$], bytes + offset, byteCount));
    return LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"write", rc);
  ]-*/;

  public native int writev(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts)
      throws ErrnoException /*-[
    int nIoVecs = buffers->size_;
    struct iovec *ioVecs = (struct iovec *)malloc(nIoVecs * sizeof (struct iovec));
    for (int i = 0; i < nIoVecs; i++) {
      const char *bytes = BytesRO([buffers objectAtIndex:i]);
      if (!bytes) {
        free(ioVecs);
        return -1;
      }
      ioVecs[i].iov_base = ((char *) bytes) + IOSIntArray_Get(offsets, i);
      ioVecs[i].iov_len = IOSIntArray_Get(byteCounts, i);
    }
    int rc = TEMP_FAILURE_RETRY(writev([fd getInt$], ioVecs, nIoVecs));
    free(ioVecs);
    return LibcoreIoPosix_throwIfMinusOneWithNSString_withInt_(@"writev", rc);
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

  @Override
  public native int getpid() /*-[
    return getpid();
  ]-*/;

  @Override
  public native int getppid() /*-[
    return getppid();
  ]-*/;

// Uncomment and implement as Os interface grows.
//  public native String[] environ();

//  public native StructStatFs fstatfs(FileDescriptor fd) throws ErrnoException;
//  public native int getegid();
//  public native int geteuid();
//  public native int getgid();
//  public native String getenv(String name);
//  public native StructPasswd getpwnam(String name) throws ErrnoException;
//  public native StructPasswd getpwuid(int uid) throws ErrnoException;
//  public native int getuid();
//  public native void kill(int pid, int signal) throws ErrnoException;
//  public native void lchown(String path, int uid, int gid) throws ErrnoException;
//  public native void listen(FileDescriptor fd, int backlog) throws ErrnoException;
//  public native void setegid(int egid) throws ErrnoException;
//  public native void setenv(String name, String value, boolean overwrite) throws ErrnoException;
//  public native void seteuid(int euid) throws ErrnoException;
//  public native void setgid(int gid) throws ErrnoException;
//  public native int setsid() throws ErrnoException;
//  public native void setuid(int uid) throws ErrnoException;
//  public native FileDescriptor socket(int domain, int type, int protocol) throws ErrnoException;
//  public native StructStatFs statfs(String path) throws ErrnoException;
//  public native void tcsendbreak(FileDescriptor fd, int duration) throws ErrnoException;
//  public int umask(int mask) {
//    if ((mask & 0777) != mask) {
//      throw new IllegalArgumentException("Invalid umask: " + mask);
//    }
//    return umaskImpl(mask);
//  }
//  private native int umaskImpl(int mask);
//  public native void unsetenv(String name) throws ErrnoException;
//  public native int waitpid(int pid, MutableInt status, int options) throws ErrnoException;
}
