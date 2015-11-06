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
import java.nio.ByteBuffer;
import java.nio.NioUtils;

import libcore.util.MutableInt;
import libcore.util.MutableLong;

/*-[
#include "BufferUtils.h"
#include "Portability.h"
#include "TempFailureRetry.h"
#include "java/io/File.h"
#include "libcore/io/StructLinger.h"
#include "libcore/io/StructPollfd.h"
#include "libcore/io/StructStatVfs.h"
#include "libcore/io/StructTimeval.h"

#include <fcntl.h>
#include <poll.h>
#include <net/if.h>
#include <sys/ioctl.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/uio.h>
#include <sys/utsname.h>
#include <termios.h>
#include <unistd.h>
]-*/

public final class Posix implements Os {
  Posix() { }

  /*-[
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

  static void throwErrnoException(String message, int errorCode) throws ErrnoException {
    throw new ErrnoException(message, errorCode);
  }

  static native int throwIfMinusOne(String name, int resultCode) throws ErrnoException /*-[
    if (resultCode == -1) {
      LibcoreIoPosix_throwErrnoExceptionWithNSString_withInt_(name, errno);
    }
    return resultCode;
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

  public native String if_indextoname(int index) /*-[
    char buf[IF_NAMESIZE];
    char *name = if_indextoname(index, buf);
    // if_indextoname(3) returns NULL on failure. There's no useful information in errno,
    // so we don't bother throwing. Callers can null-check.
    return name ? [NSString stringWithUTF8String:name] : nil;
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
