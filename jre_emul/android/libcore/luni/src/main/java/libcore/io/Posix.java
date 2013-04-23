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

/*-[
#import "TempFailureRetry.h"
#import <sys/stat.h>
]-*/

public final class Posix implements Os {
  Posix() { }

  private static int defaultEncoding;

  static {
    nativeInit();
  }

  private static native void nativeInit() /*-[
    LibcoreIoPosix_defaultEncoding_ = [NSString defaultCStringEncoding];
  ]-*/;

  private static void throwErrnoException(String message, int error) throws ErrnoException {
    throw new ErrnoException(message, error);
  }

  private static int throwIfMinusOne(String name, int errorCode) throws ErrnoException {
    if (errorCode == -1) {
      throwErrnoException(name, errorCode);
    }
    return errorCode;
  }

  public native boolean access(String path, int mode) throws ErrnoException /*-[
    if (!path) {
      return NO;
    }
    const char* cpath = [path cStringUsingEncoding:LibcoreIoPosix_defaultEncoding_];
    int rc = TEMP_FAILURE_RETRY(access(cpath, mode));
    if (rc == -1) {
      [LibcoreIoPosix throwErrnoExceptionWithNSString:@"access" withInt:errno];
    }
    return (rc == 0);
  ]-*/;

  public native void chmod(String path, int mode) throws ErrnoException /*-[
    if (path) {
      const char* cpath = [path cStringUsingEncoding:LibcoreIoPosix_defaultEncoding_];
      int rc = TEMP_FAILURE_RETRY(chmod(cpath, mode));
      [LibcoreIoPosix throwIfMinusOneWithNSString:@"chmod" withInt:rc];
    }
  ]-*/;

  public native void chown(String path, int uid, int gid) throws ErrnoException /*-[
    if (path) {
      const char* cpath = [path cStringUsingEncoding:LibcoreIoPosix_defaultEncoding_];
      int rc = TEMP_FAILURE_RETRY(chown(cpath, uid, gid));
      [LibcoreIoPosix throwIfMinusOneWithNSString:@"chown" withInt:rc];
    }
  ]-*/;

  public native void close(FileDescriptor javaFd) throws ErrnoException /*-[
    // Get the FileDescriptor's 'fd' field and clear it.
    // We need to do this before we can throw an IOException.
    int fd = (int) javaFd->descriptor_;
    javaFd->descriptor_ = -1L;
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"close" withInt:close(fd)];
  ]-*/;

  public native FileDescriptor dup(FileDescriptor oldFd) throws ErrnoException /*-[
    int nativeFd = TEMP_FAILURE_RETRY(dup((int) oldFd->descriptor_));
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"dup" withInt:nativeFd];
    JavaIoFileDescriptor *newFd = AUTORELEASE([[JavaIoFileDescriptor alloc] init]);
    newFd->descriptor_ = nativeFd;
    return newFd;
  ]-*/;

  public native FileDescriptor dup2(FileDescriptor oldFd, int newNativeFd)
      throws ErrnoException /*-[
    int nativeFd = TEMP_FAILURE_RETRY(dup2((int) oldFd->descriptor_, newNativeFd));
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"dup2" withInt:nativeFd];
    JavaIoFileDescriptor *newFd = AUTORELEASE([[JavaIoFileDescriptor alloc] init]);
    newFd->descriptor_ = nativeFd;
    return newFd;
  ]-*/;

  public native int fcntlVoid(FileDescriptor fd, int cmd) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(fcntl((int) fd->descriptor_, cmd));
    return [LibcoreIoPosix throwIfMinusOneWithNSString:@"fcntl" withInt:rc];
  ]-*/;

  public native int fcntlLong(FileDescriptor fd, int cmd, long arg) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(fcntl((int) fd->descriptor_, cmd, arg));
    return [LibcoreIoPosix throwIfMinusOneWithNSString:@"fcntl" withInt:rc];
  ]-*/;

  public native FileDescriptor open(String path, int flags, int mode) throws ErrnoException /*-[
    if (!path) {
      return nil;
    }
    const char* cpath = [path cStringUsingEncoding:LibcoreIoPosix_defaultEncoding_];
    int nativeFd = TEMP_FAILURE_RETRY(open(cpath, flags, mode));
    if (nativeFd == -1) {
      return nil;
    }
    JavaIoFileDescriptor *newFd = AUTORELEASE([[JavaIoFileDescriptor alloc] init]);
    newFd->descriptor_ = nativeFd;
    return newFd;
  ]-*/;

  public native String strerror(int errno) /*-[
    char buffer[BUFSIZ];
    int ret = strerror_r(errno_, buffer, BUFSIZ);
    if (ret != 0) {  // If not successful...
      snprintf(buffer, BUFSIZ, "errno %d", errno_);
    }
    return [NSString stringWithCString:buffer encoding:LibcoreIoPosix_defaultEncoding_];
  ]-*/;

// Uncomment and implement as Os interface grows.
//  public native String[] environ();
//  public native void fchmod(FileDescriptor fd, int mode) throws ErrnoException;
//  public native void fchown(FileDescriptor fd, int uid, int gid) throws ErrnoException;
//  public native int fcntlFlock(FileDescriptor fd, int cmd, StructFlock arg)
//      throws ErrnoException;
//  public native void fdatasync(FileDescriptor fd) throws ErrnoException;
//  public native StructStat fstat(FileDescriptor fd) throws ErrnoException;
//  public native StructStatFs fstatfs(FileDescriptor fd) throws ErrnoException;
//  public native void fsync(FileDescriptor fd) throws ErrnoException;
//  public native void ftruncate(FileDescriptor fd, long length) throws ErrnoException;
//  public native String gai_strerror(int error);
//  public native int getegid();
//  public native int geteuid();
//  public native int getgid();
//  public native String getenv(String name);
//  public native int getpid();
//  public native int getppid();
//  public native StructPasswd getpwnam(String name) throws ErrnoException;
//  public native StructPasswd getpwuid(int uid) throws ErrnoException;
//  public native int getuid();
//  public native String if_indextoname(int index);
//  public native int ioctlInt(FileDescriptor fd, int cmd, MutableInt arg)
//      throws ErrnoException;
//  public native boolean isatty(FileDescriptor fd);
//  public native void kill(int pid, int signal) throws ErrnoException;
//  public native void lchown(String path, int uid, int gid) throws ErrnoException;
//  public native void listen(FileDescriptor fd, int backlog) throws ErrnoException;
//  public native long lseek(FileDescriptor fd, long offset, int whence) throws ErrnoException;
//  public native StructStat lstat(String path) throws ErrnoException;
//  public native void mincore(long address, long byteCount, byte[] vector)
//      throws ErrnoException;
//  public native void mkdir(String path, int mode) throws ErrnoException;
//  public native void mlock(long address, long byteCount) throws ErrnoException;
//  public native long mmap(long address, long byteCount, int prot, int flags,
//      FileDescriptor fd, long offset) throws ErrnoException;
//  public native void msync(long address, long byteCount, int flags) throws ErrnoException;
//  public native void munlock(long address, long byteCount) throws ErrnoException;
//  public native void munmap(long address, long byteCount) throws ErrnoException;
//  public native FileDescriptor[] pipe() throws ErrnoException;
//  public native int poll(StructPollfd[] fds, int timeoutMs) throws ErrnoException;
//  public int pread(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException {
//    if (buffer.isDirect()) {
//      return preadBytes(fd, buffer, buffer.position(), buffer.remaining(), offset);
//    } else {
//      return preadBytes(fd, NioUtils.unsafeArray(buffer),
//          NioUtils.unsafeArrayOffset(buffer) + buffer.position(), buffer.remaining(), offset);
//    }
//  }
//  public int pread(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset)
//      throws ErrnoException {
//    return preadBytes(fd, bytes, byteOffset, byteCount, offset);
//  }
//  private native int preadBytes(FileDescriptor fd, Object buffer, int bufferOffset, int byteCount,
//      long offset) throws ErrnoException;
//  public int pwrite(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException {
//    if (buffer.isDirect()) {
//      return pwriteBytes(fd, buffer, buffer.position(), buffer.remaining(), offset);
//    } else {
//      return pwriteBytes(fd, NioUtils.unsafeArray(buffer),
//          NioUtils.unsafeArrayOffset(buffer) + buffer.position(), buffer.remaining(), offset);
//    }
//  }
//  public int pwrite(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset)
//      throws ErrnoException {
//    return pwriteBytes(fd, bytes, byteOffset, byteCount, offset);
//  }
//  private native int pwriteBytes(FileDescriptor fd, Object buffer, int bufferOffset,
//      int byteCount, long offset) throws ErrnoException;
//  public int read(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException {
//    if (buffer.isDirect()) {
//      return readBytes(fd, buffer, buffer.position(), buffer.remaining());
//    } else {
//      return readBytes(fd, NioUtils.unsafeArray(buffer),
//          NioUtils.unsafeArrayOffset(buffer) + buffer.position(), buffer.remaining());
//    }
//  }
//  public int read(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount)
//      throws ErrnoException {
//    return readBytes(fd, bytes, byteOffset, byteCount);
//  }
//  private native int readBytes(FileDescriptor fd, Object buffer, int offset, int byteCount)
//      throws ErrnoException;
//  public native int readv(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts)
//      throws ErrnoException;
//  public native void remove(String path) throws ErrnoException;
//  public native void rename(String oldPath, String newPath) throws ErrnoException;
//  public native long sendfile(FileDescriptor outFd, FileDescriptor inFd, MutableLong inOffset,
//      long byteCount) throws ErrnoException;
//  public native void setegid(int egid) throws ErrnoException;
//  public native void setenv(String name, String value, boolean overwrite) throws ErrnoException;
//  public native void seteuid(int euid) throws ErrnoException;
//  public native void setgid(int gid) throws ErrnoException;
//  public native int setsid() throws ErrnoException;
//  public native void setuid(int uid) throws ErrnoException;
//  public native void shutdown(FileDescriptor fd, int how) throws ErrnoException;
//  public native FileDescriptor socket(int domain, int type, int protocol) throws ErrnoException;
//  public native void socketpair(int domain, int type, int protocol, FileDescriptor fd1,
//      FileDescriptor fd2) throws ErrnoException;
//  public native StructStat stat(String path) throws ErrnoException;
//  public native StructStatFs statfs(String path) throws ErrnoException;
//  public native void symlink(String oldPath, String newPath) throws ErrnoException;
//  public native long sysconf(int name);
//  public native void tcdrain(FileDescriptor fd) throws ErrnoException;
//  public native void tcsendbreak(FileDescriptor fd, int duration) throws ErrnoException;
//  public int umask(int mask) {
//    if ((mask & 0777) != mask) {
//      throw new IllegalArgumentException("Invalid umask: " + mask);
//    }
//    return umaskImpl(mask);
//  }
//  private native int umaskImpl(int mask);
//  public native StructUtsname uname();
//  public native void unsetenv(String name) throws ErrnoException;
//  public native int waitpid(int pid, MutableInt status, int options) throws ErrnoException;
//  public int write(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException {
//    if (buffer.isDirect()) {
//      return writeBytes(fd, buffer, buffer.position(), buffer.remaining());
//    } else {
//      return writeBytes(fd, NioUtils.unsafeArray(buffer),
//          NioUtils.unsafeArrayOffset(buffer) + buffer.position(), buffer.remaining());
//    }
//  }
//  public int write(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount)
//      throws ErrnoException {
//    return writeBytes(fd, bytes, byteOffset, byteCount);
//  }
//  private native int writeBytes(FileDescriptor fd, Object buffer, int offset, int byteCount)
//      throws ErrnoException;
//  public native int writev(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts)
//      throws ErrnoException;

// TODO(user): implement these commented methods when java.net is ported.
//  public native FileDescriptor accept(FileDescriptor fd, InetSocketAddress peerAddress) throws ErrnoException, SocketException;
//  public native void bind(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException;
//  public native void connect(FileDescriptor fd, InetAddress address, int port) throws ErrnoException, SocketException;
//  public native InetAddress[] getaddrinfo(String node, StructAddrinfo hints) throws GaiException;
//  public native String getnameinfo(InetAddress address, int flags) throws GaiException;
//  public native SocketAddress getsockname(FileDescriptor fd) throws ErrnoException;
//  public native InetAddress getsockoptInAddr(FileDescriptor fd, int level, int option) throws ErrnoException;
//  public native int getsockoptByte(FileDescriptor fd, int level, int option) throws ErrnoException;
//  public native int getsockoptInt(FileDescriptor fd, int level, int option) throws ErrnoException;
//  public native StructLinger getsockoptLinger(FileDescriptor fd, int level, int option) throws ErrnoException;
//  public native StructTimeval getsockoptTimeval(FileDescriptor fd, int level, int option) throws ErrnoException;
//  public native InetAddress inet_pton(int family, String address);
//  public native InetAddress ioctlInetAddress(FileDescriptor fd, int cmd, String interfaceName) throws ErrnoException;
//  public int recvfrom(FileDescriptor fd, ByteBuffer buffer, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException {
//    if (buffer.isDirect()) {
//      return recvfromBytes(fd, buffer, buffer.position(), buffer.remaining(), flags, srcAddress);
//    } else {
//      return recvfromBytes(fd, NioUtils.unsafeArray(buffer), NioUtils.unsafeArrayOffset(buffer) + buffer.position(), buffer.remaining(), flags, srcAddress);
//    }
//  }
//  public int recvfrom(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException {
//    // This indirection isn't strictly necessary, but ensures that our public interface is type safe.
//    return recvfromBytes(fd, bytes, byteOffset, byteCount, flags, srcAddress);
//  }
//  private native int recvfromBytes(FileDescriptor fd, Object buffer, int byteOffset, int byteCount, int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException;
//  public int sendto(FileDescriptor fd, ByteBuffer buffer, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException {
//    if (buffer.isDirect()) {
//      return sendtoBytes(fd, buffer, buffer.position(), buffer.remaining(), flags, inetAddress, port);
//    } else {
//      return sendtoBytes(fd, NioUtils.unsafeArray(buffer), NioUtils.unsafeArrayOffset(buffer) + buffer.position(), buffer.remaining(), flags, inetAddress, port);
//    }
//  }
//  public int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException {
//    // This indirection isn't strictly necessary, but ensures that our public interface is type safe.
//    return sendtoBytes(fd, bytes, byteOffset, byteCount, flags, inetAddress, port);
//  }
//  private native int sendtoBytes(FileDescriptor fd, Object buffer, int byteOffset, int byteCount, int flags, InetAddress inetAddress, int port) throws ErrnoException, SocketException;
//  public native void setsockoptByte(FileDescriptor fd, int level, int option, int value) throws ErrnoException;
//  public native void setsockoptIfreq(FileDescriptor fd, int level, int option, String value) throws ErrnoException;
//  public native void setsockoptInt(FileDescriptor fd, int level, int option, int value) throws ErrnoException;
//  public native void setsockoptIpMreqn(FileDescriptor fd, int level, int option, int value) throws ErrnoException;
//  public native void setsockoptGroupReq(FileDescriptor fd, int level, int option, StructGroupReq value) throws ErrnoException;
//  public native void setsockoptLinger(FileDescriptor fd, int level, int option, StructLinger value) throws ErrnoException;
//  public native void setsockoptTimeval(FileDescriptor fd, int level, int option, StructTimeval value) throws ErrnoException;
}
