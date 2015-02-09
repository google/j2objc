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

import libcore.util.MutableInt;
import libcore.util.MutableLong;

public interface Os {

    public FileDescriptor accept(FileDescriptor fd, InetSocketAddress peerAddress)
        throws ErrnoException, SocketException;
    public boolean access(String path, int mode) throws ErrnoException;
    public boolean canAccess(String path, int mode);
    public void bind(FileDescriptor fd, InetAddress address, int port)
        throws ErrnoException, SocketException;
    public void chmod(String path, int mode) throws ErrnoException;
    public void chown(String path, int uid, int gid) throws ErrnoException;
    public void close(FileDescriptor fd) throws ErrnoException;
    public void connect(FileDescriptor fd, InetAddress address, int port)
        throws ErrnoException, SocketException;
    public FileDescriptor dup(FileDescriptor oldFd) throws ErrnoException;
    public FileDescriptor dup2(FileDescriptor oldFd, int newFd) throws ErrnoException;
    public void fchmod(FileDescriptor fd, int mode) throws ErrnoException;
    public void fchown(FileDescriptor fd, int uid, int gid) throws ErrnoException;
    public int fcntlFlock(FileDescriptor fd, int cmd, StructFlock arg) throws ErrnoException;
    public int fcntlLong(FileDescriptor fd, int cmd, long arg) throws ErrnoException;
    public int fcntlVoid(FileDescriptor fd, int cmd) throws ErrnoException;
    public void fdatasync(FileDescriptor fd) throws ErrnoException;
    public StructStat fstat(FileDescriptor fd) throws ErrnoException;
    public void fsync(FileDescriptor fd) throws ErrnoException;
    public void ftruncate(FileDescriptor fd, long length) throws ErrnoException;
    public String gai_strerror(int error);
    public InetAddress[] getaddrinfo(String node, StructAddrinfo hints) throws GaiException;
    public String getnameinfo(InetAddress address, int flags) throws GaiException;
    public SocketAddress getsockname(FileDescriptor fd) throws ErrnoException;
    public int getsockoptByte(FileDescriptor fd, int level, int option) throws ErrnoException;
    public InetAddress getsockoptInAddr(FileDescriptor fd, int level, int option)
        throws ErrnoException;
    public int getsockoptInt(FileDescriptor fd, int level, int option) throws ErrnoException;
    public StructLinger getsockoptLinger(FileDescriptor fd, int level, int option)
        throws ErrnoException;
    public StructTimeval getsockoptTimeval(FileDescriptor fd, int level, int option)
        throws ErrnoException;
    public String if_indextoname(int index);
    public InetAddress inet_pton(int family, String address);
    public InetAddress ioctlInetAddress(FileDescriptor fd, int cmd, String interfaceName)
        throws ErrnoException;
    public int ioctlInt(FileDescriptor fd, int cmd, MutableInt arg) throws ErrnoException;
    public boolean isatty(FileDescriptor fd);
    public void listen(FileDescriptor fd, int backlog) throws ErrnoException;
    public long lseek(FileDescriptor fd, long offset, int whence) throws ErrnoException;
    public void mkdir(String path, int mode) throws ErrnoException;
    public void mincore(long address, long byteCount, byte[] vector) throws ErrnoException;
    public void mlock(long address, long byteCount) throws ErrnoException;
    public long mmap(long address, long byteCount, int prot, int flags, FileDescriptor fd,
        long offset) throws ErrnoException;
    public void msync(long address, long byteCount, int flags) throws ErrnoException;
    public void munlock(long address, long byteCount) throws ErrnoException;
    public void munmap(long address, long byteCount) throws ErrnoException;
    public FileDescriptor open(String path, int flags, int mode) throws ErrnoException;
    public FileDescriptor[] pipe() throws ErrnoException;
    public int getpid();
    public int poll(StructPollfd[] fds, int timeoutMs) throws ErrnoException;
    public int getppid();
    public int pread(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException;
    public int pwrite(FileDescriptor fd, ByteBuffer buffer, long offset) throws ErrnoException;
    public int read(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException;
    public int read(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount)
        throws ErrnoException;
    public int recvfrom(FileDescriptor fd, ByteBuffer buffer, int flags,
        InetSocketAddress srcAddress) throws ErrnoException, SocketException;
    public int recvfrom(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount,
        int flags, InetSocketAddress srcAddress) throws ErrnoException, SocketException;
    public int readv(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts)
        throws ErrnoException;
    public String realpath(String path);
    public void remove(String path) throws ErrnoException;
    public void rename(String oldPath, String newPath) throws ErrnoException;
    public long sendfile(FileDescriptor outFd, FileDescriptor inFd, MutableLong inOffset,
        long byteCount) throws ErrnoException;
    public int sendto(FileDescriptor fd, ByteBuffer buffer, int flags, InetAddress inetAddress,
        int port) throws ErrnoException, SocketException;
    public int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags,
        InetAddress inetAddress, int port) throws ErrnoException, SocketException;
    public void setsockoptByte(FileDescriptor fd, int level, int option, int value)
        throws ErrnoException;
    public void setsockoptGroupReq(FileDescriptor fd, int level, int option,
        StructGroupReq value) throws ErrnoException;
    public void setsockoptGroupSourceReq(FileDescriptor fd, int level, int option,
        StructGroupSourceReq value) throws ErrnoException;
    public void setsockoptIfreq(FileDescriptor fd, int level, int option, String value)
        throws ErrnoException;
    public void setsockoptInt(FileDescriptor fd, int level, int option, int value)
        throws ErrnoException;
    public void setsockoptIpMreqn(FileDescriptor fd, int level, int option, int value)
        throws ErrnoException;
    public void setsockoptLinger(FileDescriptor fd, int level, int option, StructLinger value)
        throws ErrnoException;
    public void setsockoptTimeval(FileDescriptor fd, int level, int option, StructTimeval value)
        throws ErrnoException;
    public void shutdown(FileDescriptor fd, int how) throws ErrnoException;
    public FileDescriptor socket(int domain, int type, int protocol) throws ErrnoException;
    public void socketpair(int domain, int type, int protocol, FileDescriptor fd1,
        FileDescriptor fd2) throws ErrnoException;
    public StructStat stat(String path) throws ErrnoException;
    public StructStatVfs statvfs(String path) throws ErrnoException;
    public String strerror(int errno);
    public long sysconf(int name);
    public void symlink(String oldPath, String newPath) throws ErrnoException;
    public void tcdrain(FileDescriptor fd) throws ErrnoException;
    public StructUtsname uname();
    public int write(FileDescriptor fd, ByteBuffer buffer) throws ErrnoException;
    public int write(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount)
        throws ErrnoException;
    public int writev(FileDescriptor fd, Object[] buffers, int[] offsets, int[] byteCounts)
        throws ErrnoException;

    // TODO(tball): enable as needed.
//    public String[] environ();
//    public StructStatFs fstatfs(FileDescriptor fd) throws ErrnoException;
//    public int getegid();
//    public int geteuid();
//    public int getgid();
//    public String getenv(String name);
//    public StructPasswd getpwnam(String name) throws ErrnoException;
//    public StructPasswd getpwuid(int uid) throws ErrnoException;
//    public int getuid();
//    public void kill(int pid, int signal) throws ErrnoException;
//    public void lchown(String path, int uid, int gid) throws ErrnoException;
//    public StructStat lstat(String path) throws ErrnoException;
//    public int pread(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset)
//        throws ErrnoException;
//    public int pwrite(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, long offset)
//        throws ErrnoException;
//    public void setegid(int egid) throws ErrnoException;
//    public void setenv(String name, String value, boolean overwrite) throws ErrnoException;
//    public void seteuid(int euid) throws ErrnoException;
//    public void setgid(int gid) throws ErrnoException;
//    public int setsid() throws ErrnoException;
//    public void setuid(int uid) throws ErrnoException;
//    public void tcsendbreak(FileDescriptor fd, int duration) throws ErrnoException;
//    public int umask(int mask);
//    public void unsetenv(String name) throws ErrnoException;
//    public int waitpid(int pid, MutableInt status, int options) throws ErrnoException;
}
