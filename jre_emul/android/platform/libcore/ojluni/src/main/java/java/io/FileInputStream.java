/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

/* J2ObjC removed
import dalvik.annotation.optimization.ReachabilitySensitive;
import dalvik.system.BlockGuard;
import libcore.io.IoTracker;
import libcore.io.IoUtils;
import sun.nio.ch.FileChannelImpl;
*/

import static libcore.io.OsConstants.ESPIPE;
import static libcore.io.OsConstants.O_RDONLY;
import static libcore.io.OsConstants.SEEK_CUR;

import android.system.ErrnoException;
import dalvik.system.CloseGuard;
import java.nio.channels.FileChannel;
import libcore.io.IoBridge;
import libcore.io.Libcore;


/**
 * A <code>FileInputStream</code> obtains input bytes
 * from a file in a file system. What files
 * are  available depends on the host environment.
 *
 * <p><code>FileInputStream</code> is meant for reading streams of raw bytes
 * such as image data. For reading streams of characters, consider using
 * <code>FileReader</code>.
 *
 * @author  Arthur van Hoff
 * @see     java.io.File
 * @see     java.io.FileDescriptor
 * @see     java.io.FileOutputStream
 * @see     java.nio.file.Files#newInputStream
 * @since   JDK1.0
 */
public
class FileInputStream extends InputStream
{
    /* File Descriptor - handle to the open file */
    // Android-added: @ReachabilitySensitive
    // @ReachabilitySensitive
    private final FileDescriptor fd;

    /**
     * The path of the referenced file
     * (null if the stream is created with a file descriptor)
     */
    private final String path;

    private FileChannel channel = null;

    private final Object closeLock = new Object();
    private volatile boolean closed = false;

    // Android-added: Field for tracking whether the stream owns the underlying FileDescriptor.
    private final boolean isFdOwner;

    // Android-added: CloseGuard support.
    // @ReachabilitySensitive
    private final CloseGuard guard = CloseGuard.get();

    // Android-added: Tracking of unbuffered I/O.
    // private final IoTracker tracker = new IoTracker();

    /**
     * Creates a <code>FileInputStream</code> by
     * opening a connection to an actual file,
     * the file named by the path name <code>name</code>
     * in the file system.  A new <code>FileDescriptor</code>
     * object is created to represent this file
     * connection.
     * <p>
     * First, if there is a security
     * manager, its <code>checkRead</code> method
     * is called with the <code>name</code> argument
     * as its argument.
     * <p>
     * If the named file does not exist, is a directory rather than a regular
     * file, or for some other reason cannot be opened for reading then a
     * <code>FileNotFoundException</code> is thrown.
     *
     * @param      name   the system-dependent file name.
     * @exception  FileNotFoundException  if the file does not exist,
     *                   is a directory rather than a regular file,
     *                   or for some other reason cannot be opened for
     *                   reading.
     * @exception  SecurityException      if a security manager exists and its
     *               <code>checkRead</code> method denies read access
     *               to the file.
     * @see        java.lang.SecurityManager#checkRead(java.lang.String)
     */
    public FileInputStream(String name) throws FileNotFoundException {
        this(name != null ? new File(name) : null);
    }

    /**
     * Creates a <code>FileInputStream</code> by
     * opening a connection to an actual file,
     * the file named by the <code>File</code>
     * object <code>file</code> in the file system.
     * A new <code>FileDescriptor</code> object
     * is created to represent this file connection.
     * <p>
     * First, if there is a security manager,
     * its <code>checkRead</code> method  is called
     * with the path represented by the <code>file</code>
     * argument as its argument.
     * <p>
     * If the named file does not exist, is a directory rather than a regular
     * file, or for some other reason cannot be opened for reading then a
     * <code>FileNotFoundException</code> is thrown.
     *
     * @param      file   the file to be opened for reading.
     * @exception  FileNotFoundException  if the file does not exist,
     *                   is a directory rather than a regular file,
     *                   or for some other reason cannot be opened for
     *                   reading.
     * @exception  SecurityException      if a security manager exists and its
     *               <code>checkRead</code> method denies read access to the file.
     * @see        java.io.File#getPath()
     * @see        java.lang.SecurityManager#checkRead(java.lang.String)
     */
    public FileInputStream(File file) throws FileNotFoundException {
        String name = (file != null ? file.getPath() : null);
        // SecurityManager security = System.getSecurityManager();
        // if (security != null) {
        //     security.checkRead(name);
        // }
        if (name == null) {
            throw new NullPointerException();
        }
        if (file.isInvalid()) {
            throw new FileNotFoundException("Invalid file path");
        }
        // BEGIN Android-changed: Open files using IoBridge to share BlockGuard & StrictMode logic.
        // http://b/112107427
        // fd = new FileDescriptor();
        fd = IoBridge.open(name, O_RDONLY);
        // END Android-changed: Open files using IoBridge to share BlockGuard & StrictMode logic.

        // Android-changed: Tracking mechanism for FileDescriptor sharing.
        // fd.attach(this);
        isFdOwner = true;

        path = name;

        // Android-removed: Open files using IoBridge to share BlockGuard & StrictMode logic.
        // open(name);

        // Android-added: File descriptor ownership tracking.
        /* J2ObjC removed.
        IoUtils.setFdOwner(this.fd, this);
         */

        // Android-added: CloseGuard support.
        guard.open("close");
    }

    // Android-removed: Documentation around SecurityException. Not thrown on Android.
    // Android-changed: Added doc for the Android-specific file descriptor ownership.
    /**
     * Creates a <code>FileInputStream</code> by using the file descriptor
     * <code>fdObj</code>, which represents an existing connection to an
     * actual file in the file system.
     * <p>
     * If there is a security manager, its <code>checkRead</code> method is
     * called with the file descriptor <code>fdObj</code> as its argument to
     * see if it's ok to read the file descriptor. If read access is denied
     * to the file descriptor a <code>SecurityException</code> is thrown.
     * <p>
     * If <code>fdObj</code> is null then a <code>NullPointerException</code>
     * is thrown.
     * <p>
     * This constructor does not throw an exception if <code>fdObj</code>
     * is {@link java.io.FileDescriptor#valid() invalid}.
     * However, if the methods are invoked on the resulting stream to attempt
     * I/O on the stream, an <code>IOException</code> is thrown.
     * <p>
     * Android-specific warning: {@link #close()} method doesn't close the {@code fdObj} provided,
     * because this object doesn't own the file descriptor, but the caller does. The caller can
     * call {@link android.system.Os#close(FileDescriptor)} to close the fd.
     *
     * @param      fdObj   the file descriptor to be opened for reading.
     */
    public FileInputStream(FileDescriptor fdObj) {
        // Android-changed: Delegate to added hidden constructor.
        this(fdObj, false /* isFdOwner */);
    }

    // Android-added: Internal/hidden constructor for specifying FileDescriptor ownership.
    // Android-removed: SecurityManager calls.
    /** @hide */
    public FileInputStream(FileDescriptor fdObj, boolean isFdOwner) {
        if (fdObj == null) {
            // Android-changed: Improved NullPointerException message.
            throw new NullPointerException("fdObj == null");
        }
        fd = fdObj;
        path = null;

        // Android-changed: FileDescriptor ownership tracking mechanism.
        /*
        /*
         * FileDescriptor is being shared by streams.
         * Register this stream with FileDescriptor tracker.
         *
        fd.attach(this);
        */
        this.isFdOwner = isFdOwner;
        /* J2ObjC removed.
        if (isFdOwner) {
            IoUtils.setFdOwner(this.fd, this);
        }
         */
    }

    // BEGIN Android-changed: Open files using IoBridge to share BlockGuard & StrictMode logic.
    // http://b/112107427
    /*
    /**
     * Opens the specified file for reading.
     * @param name the name of the file
     *
    private native void open0(String name) throws FileNotFoundException;

    // wrap native call to allow instrumentation
    /**
     * Opens the specified file for reading.
     * @param name the name of the file
     *
    private void open(String name) throws FileNotFoundException {
        open0(name);
    }
    */
    // END Android-changed: Open files using IoBridge to share BlockGuard & StrictMode logic.

    /**
     * Reads a byte of data from this input stream. This method blocks
     * if no input is yet available.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             file is reached.
     * @exception  IOException  if an I/O error occurs.
     */
    public int read() throws IOException {
        // Android-changed: Read methods delegate to read(byte[], int, int) to share Android logic.
        byte[] b = new byte[1];
        return (read(b, 0, 1) != -1) ? b[0] & 0xff : -1;
    }

    // Android-removed: Read methods delegate to read(byte[], int, int) to share Android logic.
    // private native int read0() throws IOException;

    // Android-removed: Read methods delegate to read(byte[], int, int) to share Android logic.
    /*
    /**
     * Reads a subarray as a sequence of bytes.
     * @param b the data to be written
     * @param off the start offset in the data
     * @param len the number of bytes that are written
     * @exception IOException If an I/O error has occurred.
     *
    private native int readBytes(byte b[], int off, int len) throws IOException;
    */

    /**
     * Reads up to <code>b.length</code> bytes of data from this input
     * stream into an array of bytes. This method blocks until some input
     * is available.
     *
     * @param      b   the buffer into which the data is read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the file has been reached.
     * @exception  IOException  if an I/O error occurs.
     */
    public int read(byte b[]) throws IOException {
        // Android-changed: Read methods delegate to read(byte[], int, int) to share Android logic.
        return read(b, 0, b.length);
    }

    /**
     * Reads up to <code>len</code> bytes of data from this input stream
     * into an array of bytes. If <code>len</code> is not zero, the method
     * blocks until some input is available; otherwise, no
     * bytes are read and <code>0</code> is returned.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in the destination array <code>b</code>
     * @param      len   the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the file has been reached.
     * @exception  NullPointerException If <code>b</code> is <code>null</code>.
     * @exception  IndexOutOfBoundsException If <code>off</code> is negative,
     * <code>len</code> is negative, or <code>len</code> is greater than
     * <code>b.length - off</code>
     * @exception  IOException  if an I/O error occurs.
     */
    public int read(byte b[], int off, int len) throws IOException {
        // Android-added: close() check before I/O.
        if (closed && len > 0) {
            throw new IOException("Stream Closed");
        }

        // Android-added: Tracking of unbuffered I/O.
        /* J2ObjC removed.
        tracker.trackIo(len);
         */

        // Android-changed: Use IoBridge instead of calling native method.
        return IoBridge.read(fd, b, off, len);
    }

    /**
     * Skips over and discards <code>n</code> bytes of data from the
     * input stream.
     *
     * <p>The <code>skip</code> method may, for a variety of
     * reasons, end up skipping over some smaller number of bytes,
     * possibly <code>0</code>. If <code>n</code> is negative, the method
     * will try to skip backwards. In case the backing file does not support
     * backward skip at its current position, an <code>IOException</code> is
     * thrown. The actual number of bytes skipped is returned. If it skips
     * forwards, it returns a positive value. If it skips backwards, it
     * returns a negative value.
     *
     * <p>This method may skip more bytes than what are remaining in the
     * backing file. This produces no exception and the number of bytes skipped
     * may include some number of bytes that were beyond the EOF of the
     * backing file. Attempting to read from the stream after skipping past
     * the end will result in -1 indicating the end of the file.
     *
     * @param      byteCount   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if n is negative, if the stream does not
     *             support seek, or if an I/O error occurs.
     */
    // BEGIN Android-changed: skip(long) implementation changed from bare native.
    public long skip(long byteCount) throws IOException {
        // Android-added: close() check before I/O.
        if (closed) {
            throw new IOException("Stream Closed");
        }

//        try {
//            // Android-added: BlockGuard support.
//            BlockGuard.getThreadPolicy().onReadFromDisk();
//            return skip0(n);
//        } catch(UseManualSkipException e) {
//            return super.skip(n);
//        }
        // J2ObjC: use Libcore.os.lseek.
        if (byteCount < 0) {
            throw new IOException("byteCount < 0: " + byteCount);
        }
        try {
            // Try lseek(2). That returns the new offset, but we'll throw an
            // exception if it couldn't perform exactly the seek we asked for.
            Libcore.os.lseek(fd, byteCount, SEEK_CUR);
            return byteCount;
        } catch (ErrnoException errnoException) {
            if (errnoException.errno == ESPIPE) {
                // You can't seek on a pipe, so fall back to the superclass' implementation.
                return super.skip(byteCount);
            }
            throw errnoException.rethrowAsIOException();
        }
    }

    // private native long skip0(long n) throws IOException, UseManualSkipException;

    /*
     * Used to force manual skip when FileInputStream operates on pipe
     */
    private static class UseManualSkipException extends Exception {
    }
    // END Android-changed: skip(long) implementation changed from bare native.

    /**
     * Returns an estimate of the number of remaining bytes that can be read (or
     * skipped over) from this input stream without blocking by the next
     * invocation of a method for this input stream. Returns 0 when the file
     * position is beyond EOF. The next invocation might be the same thread
     * or another thread. A single read or skip of this many bytes will not
     * block, but may read or skip fewer bytes.
     *
     * <p> In some cases, a non-blocking read (or skip) may appear to be
     * blocked when it is merely slow, for example when reading large
     * files over slow networks.
     *
     * @return     an estimate of the number of remaining bytes that can be read
     *             (or skipped over) from this input stream without blocking.
     * @exception  IOException  if this file input stream has been closed by calling
     *             {@code close} or an I/O error occurs.
     */
    // BEGIN Android-changed: available() implementation changed from bare native.
    public int available() throws IOException {
        // Android-added: close() check before I/O.
        if (closed) {
            throw new IOException("Stream Closed");
        }

        /* J2ObjC: use IoBridge.
        return available0();
         */
        return IoBridge.available(fd);
    }

    // private native int available0() throws IOException;
    // END Android-changed: available() implementation changed from bare native.

    /**
     * Closes this file input stream and releases any system resources
     * associated with the stream.
     *
     * <p> If this stream has an associated channel then the channel is closed
     * as well.
     *
     * @exception  IOException  if an I/O error occurs.
     *
     * @revised 1.4
     * @spec JSR-51
     */
    public void close() throws IOException {
        synchronized (closeLock) {
            if (closed) {
                return;
            }
            closed = true;
        }

        // Android-added: CloseGuard support.
        guard.close();

        if (channel != null) {
           channel.close();
        }

        // BEGIN Android-changed: Close handling / notification of blocked threads.
        if (isFdOwner) {
            IoBridge.closeAndSignalBlockedThreads(fd);
        }
        // END Android-changed: Close handling / notification of blocked threads.
    }

    /**
     * Returns the <code>FileDescriptor</code>
     * object  that represents the connection to
     * the actual file in the file system being
     * used by this <code>FileInputStream</code>.
     *
     * @return     the file descriptor object associated with this stream.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FileDescriptor
     */
    public final FileDescriptor getFD() throws IOException {
        if (fd != null) {
            return fd;
        }
        throw new IOException();
    }

    /**
     * Returns the unique {@link java.nio.channels.FileChannel FileChannel}
     * object associated with this file input stream.
     *
     * <p> The initial {@link java.nio.channels.FileChannel#position()
     * position} of the returned channel will be equal to the
     * number of bytes read from the file so far.  Reading bytes from this
     * stream will increment the channel's position.  Changing the channel's
     * position, either explicitly or by reading, will change this stream's
     * file position.
     *
     * @return  the file channel associated with this file input stream
     *
     * @since 1.4
     * @spec JSR-51
     */
    public FileChannel getChannel() {
        synchronized (this) {
            if (channel == null) {
                channel = FileChannelOpener.open(fd, path, true, false, this);
            }
            return channel;
        }
    }

    // BEGIN Android-removed: Unused code.
    /*
    private static native void initIDs();

    private native void close0() throws IOException;

    static {
        initIDs();
    }
    */
    // END Android-removed: Unused code.

    /**
     * Ensures that the <code>close</code> method of this file input stream is
     * called when there are no more references to it.
     *
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FileInputStream#close()
     */
    protected void finalize() throws IOException {
        // Android-added: CloseGuard support.
        if (guard != null) {
            guard.warnIfOpen();
        }

        if ((fd != null) &&  (fd != FileDescriptor.in)) {
            // Android-removed: Obsoleted comment about shared FileDescriptor handling.
            close();
        }
    }
}
