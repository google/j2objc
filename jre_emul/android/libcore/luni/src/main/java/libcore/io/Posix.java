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
#include "libcore/io/StructStatVfs.h"
#include "libcore/io/StructPollfd.h"

#include <fcntl.h>
#include <poll.h>
#include <sys/ioctl.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/uio.h>
#include <termios.h>
#include <unistd.h>
]-*/

public final class Posix implements Os {
  Posix() { }

  /*-[
  static id makeStructStat(const struct stat *sb) {
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

  static id makeStructStatVfs(const struct statvfs *sb) {
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

  static id doStat(NSString *path, BOOL isLstat) {
    if (!path) {
      return NO;
    }
    const char* cpath = [path UTF8String];
    struct stat sb;
    int rc = isLstat ? TEMP_FAILURE_RETRY(lstat(cpath, &sb))
                     : TEMP_FAILURE_RETRY(stat(cpath, &sb));
    if (rc == -1) {
      [LibcoreIoPosix throwErrnoExceptionWithNSString:(isLstat ? @"lstat" : @"stat") withInt:errno];
    }
    return makeStructStat(&sb);
  }
  ]-*/

  private static void throwErrnoException(String message, int errorCode) throws ErrnoException {
    throw new ErrnoException(message, errorCode);
  }

  private static native int throwIfMinusOne(String name, int resultCode) throws ErrnoException /*-[
    if (resultCode == -1) {
      [LibcoreIoPosix throwErrnoExceptionWithNSString:name withInt:errno];
    }
    return resultCode;
  ]-*/;

  public native boolean access(String path, int mode) throws ErrnoException /*-[
    if (!path) {
      return NO;
    }
    const char* cpath = [path UTF8String];
    int rc = TEMP_FAILURE_RETRY(access(cpath, mode));
    if (rc == -1) {
      [LibcoreIoPosix throwErrnoExceptionWithNSString:@"access" withInt:errno];
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
    const char* cpath = [path UTF8String];
    return (TEMP_FAILURE_RETRY(access(cpath, mode)) == 0);
  ]-*/;

  public native void chmod(String path, int mode) throws ErrnoException /*-[
    if (path) {
      const char* cpath = [path UTF8String];
      int rc = TEMP_FAILURE_RETRY(chmod(cpath, mode));
      [LibcoreIoPosix throwIfMinusOneWithNSString:@"chmod" withInt:rc];
    }
  ]-*/;

  public native void chown(String path, int uid, int gid) throws ErrnoException /*-[
    if (path) {
      const char* cpath = [path UTF8String];
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

  public native void fchmod(FileDescriptor fd, int mode) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(fchmod((int) fd->descriptor_, mode));
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"fchmod" withInt:rc];
  ]-*/;

  public native void fchown(FileDescriptor fd, int uid, int gid) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(fchown((int) fd->descriptor_, uid, gid));
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"fchown" withInt:rc];
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

    int rc = TEMP_FAILURE_RETRY(fcntl((int) fd->descriptor_, cmd, &lock));
    if (rc == -1) {
      [LibcoreIoPosix throwErrnoExceptionWithNSString:@"fcntl" withInt:errno];
    }
    arg->l_type_ = lock.l_type;
    arg->l_whence_ = lock.l_whence;
    arg->l_start_ = lock.l_start;
    arg->l_len_ = lock.l_len;
    arg->l_pid_ = lock.l_pid;
    return rc;
  ]-*/;

  public native int fcntlLong(FileDescriptor fd, int cmd, long arg) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(fcntl((int) fd->descriptor_, cmd, arg));
    return [LibcoreIoPosix throwIfMinusOneWithNSString:@"fcntl" withInt:rc];
  ]-*/;

  public native int fcntlVoid(FileDescriptor fd, int cmd) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(fcntl((int) fd->descriptor_, cmd));
    return [LibcoreIoPosix throwIfMinusOneWithNSString:@"fcntl" withInt:rc];
  ]-*/;

  public native void fdatasync(FileDescriptor fd) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(fdatasync((int) fd->descriptor_));
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"fdatasync" withInt:rc];
  ]-*/;

  public native StructStat fstat(FileDescriptor fd) throws ErrnoException /*-[
    struct stat sb;
    int rc = TEMP_FAILURE_RETRY(fstat(fd->descriptor_, &sb));
    if (rc == -1) {
        [LibcoreIoPosix throwErrnoExceptionWithNSString:@"fstat" withInt:rc];
    }
    return makeStructStat(&sb);
  ]-*/;

  public native void fsync(FileDescriptor fd) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(fsync((int) fd->descriptor_));
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"fsync" withInt:rc];
  ]-*/;

  public native void ftruncate(FileDescriptor fd, long length) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(ftruncate((int) fd->descriptor_, (off_t) length));
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"ftruncate" withInt:rc];
  ]-*/;

  public native int ioctlInt(FileDescriptor fd, int cmd, MutableInt javaArg)
      throws ErrnoException /*-[
    int arg = javaArg->value_;
    int rc = TEMP_FAILURE_RETRY(ioctl((int) fd->descriptor_, cmd, &arg));
    if (rc == -1) {
      [LibcoreIoPosix throwErrnoExceptionWithNSString:@"ioctl" withInt:rc];
    }
    javaArg->value_ = arg;
    return rc;
  ]-*/;

  public native boolean isatty(FileDescriptor fd) /*-[
    return TEMP_FAILURE_RETRY(isatty((int) fd->descriptor_)) == 1;
  ]-*/;

  public native long lseek(FileDescriptor fd, long offset, int whence) throws ErrnoException /*-[
    off_t rc = TEMP_FAILURE_RETRY(lseek((int) fd->descriptor_, (off_t) offset, whence));
    return [LibcoreIoPosix throwIfMinusOneWithNSString:@"lseek" withInt:rc];
  ]-*/;

  public native StructStat lstat(String path) throws ErrnoException /*-[
    return doStat(path, YES);
  ]-*/;

  public native void mincore(long address, long byteCount, byte[] vector) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(mincore((caddr_t) address, (size_t) byteCount, vector->buffer_));
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"mincore" withInt:rc];
  ]-*/;

  public native void mkdir(String path, int mode) throws ErrnoException /*-[
    if (path) {
      const char* cpath = [path UTF8String];
      int rc = TEMP_FAILURE_RETRY(mkdir(cpath, mode));
      [LibcoreIoPosix throwIfMinusOneWithNSString:@"mkdir" withInt:rc];
    }
  ]-*/;

  public native void mlock(long address, long byteCount) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(mlock((void *) address, byteCount));
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"mlock" withInt:rc];
  ]-*/;

  public native long mmap(long address, long byteCount, int prot, int flags,
      FileDescriptor fd, long offset) throws ErrnoException /*-[
    void* ptr = mmap((void *) address, byteCount, prot, flags, fd->descriptor_, offset);
    if (ptr == MAP_FAILED) {
      [LibcoreIoPosix throwErrnoExceptionWithNSString:@"mmap" withInt:errno];
    }
    return (long long) ptr;
  ]-*/;

  public native void msync(long address, long byteCount, int flags) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(msync((void *) address, byteCount, flags));
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"msync" withInt:rc];
  ]-*/;

  public native void munlock(long address, long byteCount) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(munlock((void *) address, byteCount));
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"munlock" withInt:rc];
  ]-*/;

  public native void munmap(long address, long byteCount) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(munmap((void *) address, byteCount));
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"munmap" withInt:rc];
  ]-*/;

  public native FileDescriptor open(String path, int flags, int mode) throws ErrnoException /*-[
    if (!path) {
      return nil;
    }
    const char* cpath = [path UTF8String];
    int nativeFd = TEMP_FAILURE_RETRY(open(cpath, flags, mode));
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"open" withInt:nativeFd];
    JavaIoFileDescriptor *newFd = AUTORELEASE([[JavaIoFileDescriptor alloc] init]);
    newFd->descriptor_ = nativeFd;
    return newFd;
  ]-*/;

  public native FileDescriptor[] pipe() throws ErrnoException /*-[
    int fds[2];
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"pipe" withInt:TEMP_FAILURE_RETRY(pipe(&fds[0]))];
    IOSObjectArray *result = [IOSObjectArray arrayWithLength:2 type:[JavaIoFileDescriptor getClass]];
    for (int i = 0; i < 2; ++i) {
      JavaIoFileDescriptor *fd = AUTORELEASE([[JavaIoFileDescriptor alloc] init]);
      fd->descriptor_ = fds[i];
      [result replaceObjectAtIndex:i withObject:fd];
    }
    return result;
  ]-*/;

  public native int poll(StructPollfd[] fds, int timeoutMs) throws ErrnoException /*-[
    size_t count = [fds count];
    struct pollfd *pollFds = calloc(count, sizeof(struct pollfd));
    for (int i = 0; i < count; i++) {
      LibcoreIoStructPollfd *javaPollFd = [fds objectAtIndex:i];
      pollFds[i].fd = javaPollFd->fd_->descriptor_;
      pollFds[i].events = javaPollFd->events_;
    }
    int rc = poll(pollFds, count, timeoutMs);
    if (rc == -1) {
      free(pollFds);
      [LibcoreIoPosix throwErrnoExceptionWithNSString:@"poll" withInt:rc];
    }
    for (int i = 0; i < count; i++) {
      LibcoreIoStructPollfd *javaPollFd = [fds objectAtIndex:i];
      javaPollFd->revents_ = pollFds[i].revents;
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
      TEMP_FAILURE_RETRY(pwrite64(fd->descriptor_, bytes + bufferOffset, byteCount, offset));
    return [LibcoreIoPosix throwIfMinusOneWithNSString:@"pread" withInt:rc];
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
      [LibcoreIoPosix throwErrnoExceptionWithNSString:@"pwrite" withInt:ERANGE];
    }
    int rc =
      TEMP_FAILURE_RETRY(pwrite64(fd->descriptor_, bytes + bufferOffset, byteCount, offset));
    return [LibcoreIoPosix throwIfMinusOneWithNSString:@"pwrite" withInt:rc];
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
    IOSArray_checkRange([buffer count], NSMakeRange(offset, byteCount));
    char *bytes = BytesRW(buffer);
    if (!bytes) {
      return -1;
    }
    int rc = TEMP_FAILURE_RETRY(read(fd->descriptor_, bytes + offset, byteCount));
    return [LibcoreIoPosix throwIfMinusOneWithNSString:@"read" withInt:rc];
  ]-*/;

  public native int readv(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts)
      throws ErrnoException /*-[
    int nIoVecs = [buffers count];
    struct iovec *ioVecs = malloc(nIoVecs * sizeof (struct iovec));
    for (int i = 0; i < nIoVecs; i++) {
      char *bytes = BytesRW([buffers objectAtIndex:i]);
      if (!bytes) {
        free(ioVecs);
        return -1;
      }
      ioVecs[i].iov_base = ((void *) bytes) + IOSIntArray_Get(offsets, i);
      ioVecs[i].iov_len = IOSIntArray_Get(byteCounts, i);
    }
    int rc = TEMP_FAILURE_RETRY(readv(fd->descriptor_, ioVecs, nIoVecs));
    free(ioVecs);
    return [LibcoreIoPosix throwIfMinusOneWithNSString:@"readv" withInt:rc];
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
      [LibcoreIoPosix throwErrnoExceptionWithNSString:@"realpath" withInt:errno];
    }
    NSString *result =
        [[NSFileManager defaultManager] stringWithFileSystemRepresentation:realPath
                                                                    length:strlen(realPath)];
    free(realPath);
    return result;
  ]-*/;

  public native void remove(String path) throws ErrnoException /*-[
    if (path) {
      const char* cpath = [path UTF8String];
      int rc = TEMP_FAILURE_RETRY(remove(cpath));
      [LibcoreIoPosix throwIfMinusOneWithNSString:@"remove" withInt:rc];
    }
  ]-*/;

  public native void rename(String oldPath, String newPath) throws ErrnoException /*-[
    if (oldPath && newPath) {
      const char* cOldPath = [oldPath UTF8String];
      const char* cNewPath = [newPath UTF8String];
      int rc = TEMP_FAILURE_RETRY(rename(cOldPath, cNewPath));
      [LibcoreIoPosix throwIfMinusOneWithNSString:@"rename" withInt:rc];
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
    int rc =
        TEMP_FAILURE_RETRY(sendfile_(outFd->descriptor_, inFd->descriptor_, offsetPtr, byteCount));
    if (inOffset != NULL) {
      inOffset->value_ = offset;
    }
    return [LibcoreIoPosix throwIfMinusOneWithNSString:@"sendfile" withInt:rc];
  ]-*/;

  public native void socketpair(int domain, int type, int protocol, FileDescriptor fd1,
      FileDescriptor fd2) throws ErrnoException /*-[
    int fds[2];
    int rc = TEMP_FAILURE_RETRY(socketpair(domain, type, protocol, fds));
    if (rc != -1) {
      fd1->descriptor_ = fds[0];
      fd2->descriptor_ = fds[1];
    }
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"socketpair" withInt:rc];
  ]-*/;

  public native StructStat stat(String path) throws ErrnoException /*-[
    return doStat(path, NO);
  ]-*/;

  public native StructStatVfs statvfs(String path) throws ErrnoException /*-[
    if (!path) {
      return NO;
    }
    const char* cpath = [path UTF8String];
    struct statvfs sb;
    int rc = TEMP_FAILURE_RETRY(statvfs(cpath, &sb));
    if (rc == -1) {
      [LibcoreIoPosix throwErrnoExceptionWithNSString:@"statvfs" withInt:errno];
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
      [LibcoreIoPosix throwErrnoExceptionWithNSString:@"sysconf" withInt:errno];
    }
    return result;
  ]-*/;

  public native void symlink(String oldPath, String newPath) throws ErrnoException /*-[
    if (oldPath && newPath) {
      const char* cOldPath = [oldPath UTF8String];
      const char* cNewPath = [newPath UTF8String];
      int rc = TEMP_FAILURE_RETRY(symlink(cOldPath, cNewPath));
      [LibcoreIoPosix throwIfMinusOneWithNSString:@"symlink" withInt:rc];
    }
  ]-*/;

  public native void tcdrain(FileDescriptor fd) throws ErrnoException /*-[
    int rc = TEMP_FAILURE_RETRY(tcdrain((int) fd->descriptor_));
    [LibcoreIoPosix throwIfMinusOneWithNSString:@"fcntl" withInt:rc];
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
    IOSArray_checkRange([bytes count], NSMakeRange(byteOffset, byteCount));
    int rc =
        TEMP_FAILURE_RETRY(write((int) fd->descriptor_, bytes->buffer_ + byteOffset, byteCount));
    return [LibcoreIoPosix throwIfMinusOneWithNSString:@"write" withInt:rc];
  ]-*/;

  private native int writeBytes(FileDescriptor fd, Object buffer, int offset, int byteCount)
      throws ErrnoException /*-[
    const char *bytes = BytesRO(buffer);
    if (!bytes) {
      return -1;
    }
    int rc = TEMP_FAILURE_RETRY(write(fd->descriptor_, bytes + offset, byteCount));
    return [LibcoreIoPosix throwIfMinusOneWithNSString:@"write" withInt:rc];
  ]-*/;

  public native int writev(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts)
      throws ErrnoException /*-[
    int nIoVecs = [buffers count];
    struct iovec *ioVecs = malloc(nIoVecs * sizeof (struct iovec));
    for (int i = 0; i < nIoVecs; i++) {
      const char *bytes = BytesRO([buffers objectAtIndex:i]);
      if (!bytes) {
        free(ioVecs);
        return -1;
      }
      ioVecs[i].iov_base = ((void *) bytes) + IOSIntArray_Get(offsets, i);
      ioVecs[i].iov_len = IOSIntArray_Get(byteCounts, i);
    }
    int rc = TEMP_FAILURE_RETRY(writev(fd->descriptor_, ioVecs, nIoVecs));
    free(ioVecs);
    return [LibcoreIoPosix throwIfMinusOneWithNSString:@"writev" withInt:rc];
  ]-*/;

  @Override
  public void bind(FileDescriptor fd, InetAddress address, int port)
      throws ErrnoException, SocketException {
    // TODO Auto-generated method stub

  }

  @Override
  public void connect(FileDescriptor fd, InetAddress address, int port)
      throws ErrnoException, SocketException {
    // TODO Auto-generated method stub

  }

  @Override
  public String gai_strerror(int error) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InetAddress[] getaddrinfo(String node, StructAddrinfo hints)
      throws GaiException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getnameinfo(InetAddress address, int flags) throws GaiException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public SocketAddress getsockname(FileDescriptor fd) throws ErrnoException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getsockoptInt(FileDescriptor fd, int level, int option)
      throws ErrnoException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public InetAddress inet_pton(int family, String address) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int recvfrom(FileDescriptor fd, ByteBuffer buffer, int flags,
      InetSocketAddress srcAddress) throws ErrnoException, SocketException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int recvfrom(FileDescriptor fd, byte[] bytes, int byteOffset,
      int byteCount, int flags, InetSocketAddress srcAddress)
      throws ErrnoException, SocketException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int sendto(FileDescriptor fd, ByteBuffer buffer, int flags,
      InetAddress inetAddress, int port) throws ErrnoException, SocketException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int sendto(FileDescriptor fd, byte[] bytes, int byteOffset,
      int byteCount, int flags, InetAddress inetAddress, int port)
      throws ErrnoException, SocketException {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void setsockoptInt(FileDescriptor fd, int level, int option, int value)
      throws ErrnoException {
    // TODO Auto-generated method stub

  }

  @Override
  public FileDescriptor socket(int domain, int type, int protocol)
      throws ErrnoException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public StructUtsname uname() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String if_indextoname(int index) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InetAddress ioctlInetAddress(FileDescriptor fd, int cmd,
      String interfaceName) throws ErrnoException {
    // TODO Auto-generated method stub
    return null;
  }

// Uncomment and implement as Os interface grows.
//  public native String[] environ();

//  public native StructStatFs fstatfs(FileDescriptor fd) throws ErrnoException;
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
//  public native void kill(int pid, int signal) throws ErrnoException;
//  public native void lchown(String path, int uid, int gid) throws ErrnoException;
//  public native void listen(FileDescriptor fd, int backlog) throws ErrnoException;
//  public native void mkdir(String path, int mode) throws ErrnoException;
//  public native int readv(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts)
//      throws ErrnoException;
//  public native void remove(String path) throws ErrnoException;
//  public native void rename(String oldPath, String newPath) throws ErrnoException;
//  public native void setegid(int egid) throws ErrnoException;
//  public native void setenv(String name, String value, boolean overwrite) throws ErrnoException;
//  public native void seteuid(int euid) throws ErrnoException;
//  public native void setgid(int gid) throws ErrnoException;
//  public native int setsid() throws ErrnoException;
//  public native void setuid(int uid) throws ErrnoException;
//  public native void shutdown(FileDescriptor fd, int how) throws ErrnoException;
//  public native FileDescriptor socket(int domain, int type, int protocol) throws ErrnoException;
//  public native StructStat stat(String path) throws ErrnoException;
//  public native StructStatFs statfs(String path) throws ErrnoException;
//  public native void symlink(String oldPath, String newPath) throws ErrnoException;
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

// TODO(tball): implement these commented methods when java.net is ported.
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
