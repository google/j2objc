/*
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
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

package sun.nio.fs;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Unix system and library calls.
 */

class UnixNativeDispatcher {
    protected UnixNativeDispatcher() { }

    /**
     * char *getcwd(char *buf, size_t size);
     */
    static native byte[] getcwd();

    /**
     * int dup(int filedes)
     */
    static native int dup(int filedes) throws UnixException;

    private static native int open0(long pathAddress, int flags, int mode)
        throws UnixException;

    /**
     * int openat(int dfd, const char* path, int oflag, mode_t mode)
     */
    static int openat(int dfd, byte[] path, int flags, int mode) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(path);
        try {
            return openat0(dfd, buffer.address(), flags, mode);
        } finally {
            buffer.release();
        }
    }
    private static native int openat0(int dfd, long pathAddress, int flags, int mode)
        throws UnixException;

    /**
     * close(int filedes)
     */
    static native void close(int fd);

    private static native long fopen0(long pathAddress, long modeAddress)
        throws UnixException;

    /**
     * fclose(FILE* stream)
     */
    static native void fclose(long stream) throws UnixException;

    private static native void link0(long existingAddress, long newAddress)
        throws UnixException;

    private static native void unlink0(long pathAddress) throws UnixException;

    /**
     * unlinkat(int dfd, const char* path, int flag)
     */
    static void unlinkat(int dfd, byte[] path, int flag) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(path);
        try {
            unlinkat0(dfd, buffer.address(), flag);
        } finally {
            buffer.release();
        }
    }
    private static native void unlinkat0(int dfd, long pathAddress, int flag)
        throws UnixException;

    private static native void mknod0(long pathAddress, int mode, long dev)
        throws UnixException;

    private static native void rename0(long fromAddress, long toAddress)
        throws UnixException;

    /**
     *  renameat(int fromfd, const char* old, int tofd, const char* new)
     */
    static void renameat(int fromfd, byte[] from, int tofd, byte[] to) throws UnixException {
        NativeBuffer fromBuffer = NativeBuffers.asNativeBuffer(from);
        NativeBuffer toBuffer = NativeBuffers.asNativeBuffer(to);
        try {
            renameat0(fromfd, fromBuffer.address(), tofd, toBuffer.address());
        } finally {
            toBuffer.release();
            fromBuffer.release();
        }
    }
    private static native void renameat0(int fromfd, long fromAddress, int tofd, long toAddress)
        throws UnixException;

    private static native void mkdir0(long pathAddress, int mode) throws UnixException;

    private static native void rmdir0(long pathAddress) throws UnixException;

    private static native byte[] readlink0(long pathAddress) throws UnixException;

    private static native byte[] realpath0(long pathAddress) throws UnixException;

    private static native void symlink0(long name1, long name2)
        throws UnixException;

    private static native void chown0(long pathAddress, int uid, int gid)
        throws UnixException;

    private static native void lchown0(long pathAddress, int uid, int gid)
        throws UnixException;

    /**
     * fchown(int filedes, uid_t owner, gid_t group)
     */
    static native void fchown(int fd, int uid, int gid) throws UnixException;

    private static native void chmod0(long pathAddress, int mode)
        throws UnixException;

    /**
     * fchmod(int fildes, mode_t mode)
     */
    static native void fchmod(int fd, int mode) throws UnixException;

    private static native void utimes0(long pathAddress, long times0, long times1)
        throws UnixException;

    /**
     * futimes(int fildes,, const struct timeval times[2])
     */
    static native void futimes(int fd, long times0, long times1) throws UnixException;

    private static native long opendir0(long pathAddress) throws UnixException;

    /**
     * DIR* fdopendir(int filedes)
     */
    static native long fdopendir(int dfd) throws UnixException;


    /**
     * closedir(DIR* dirp)
     */
    static native void closedir(long dir) throws UnixException;

    /**
     * struct dirent* readdir(DIR *dirp)
     *
     * @return  dirent->d_name
     */
    static native byte[] readdir(long dir) throws UnixException;

    /**
     * size_t read(int fildes, void* buf, size_t nbyte)
     */
    static native int read(int fildes, long buf, int nbyte) throws UnixException;

    /**
     * size_t writeint fildes, void* buf, size_t nbyte)
     */
    static native int write(int fildes, long buf, int nbyte) throws UnixException;

    private static native void access0(long pathAddress, int amode) throws UnixException;

    /**
     * struct passwd *getpwuid(uid_t uid);
     *
     * @return  passwd->pw_name
     */
    static native byte[] getpwuid(int uid) throws UnixException;

    /**
     * struct group *getgrgid(gid_t gid);
     *
     * @return  group->gr_name
     */
    static native byte[] getgrgid(int gid) throws UnixException;

    /**
     * struct passwd *getpwnam(const char *name);
     *
     * @return  passwd->pw_uid
     */
    static int getpwnam(String name) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(Util.toBytes(name));
        try {
            return getpwnam0(buffer.address());
        } finally {
            buffer.release();
        }
    }
    private static native int getpwnam0(long nameAddress) throws UnixException;

    /**
     * struct group *getgrnam(const char *name);
     *
     * @return  group->gr_name
     */
    static int getgrnam(String name) throws UnixException {
        NativeBuffer buffer = NativeBuffers.asNativeBuffer(Util.toBytes(name));
        try {
            return getgrnam0(buffer.address());
        } finally {
            buffer.release();
        }
    }
    private static native int getgrnam0(long nameAddress) throws UnixException;

    private static native long pathconf0(long pathAddress, int name)
        throws UnixException;

    /**
     * long fpathconf(int fildes, int name);
     */
    static native long fpathconf(int filedes, int name) throws UnixException;

    /**
     * char* strerror(int errnum)
     */
    static native byte[] strerror(int errnum);

    /**
     * Capabilities
     */
    private static final int SUPPORTS_OPENAT        = 1 << 1;    // syscalls
    private static final int SUPPORTS_FUTIMES       = 1 << 2;
    private static final int SUPPORTS_BIRTHTIME     = 1 << 16;   // other features
    private static final int capabilities;

    /**
     * Supports openat and other *at calls.
     */
    static boolean openatSupported() {
        return (capabilities & SUPPORTS_OPENAT) != 0;
    }

    /**
     * Supports futimes or futimesat
     */
    static boolean futimesSupported() {
        return (capabilities & SUPPORTS_FUTIMES) != 0;
    }

    /**
     * Supports file birth (creation) time attribute
     */
    static boolean birthtimeSupported() {
        return (capabilities & SUPPORTS_BIRTHTIME) != 0;
    }

    private static native int init();
    static {
        // Android-removed: Code to load native libraries, doesn't make sense on Android.
        /*
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                System.loadLibrary("nio");
                return null;
        }});
        */
        capabilities = init();
    }
}
