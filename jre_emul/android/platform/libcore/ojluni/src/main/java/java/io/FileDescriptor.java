/*
 * Copyright (c) 1995, 2011, Oracle and/or its affiliates. All rights reserved.
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

import android.system.ErrnoException;
import libcore.io.Libcore;
import static libcore.io.OsConstants.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Instances of the file descriptor class serve as an opaque handle
 * to the underlying machine-specific structure representing an open
 * file, an open socket, or another source or sink of bytes. The
 * main practical use for a file descriptor is to create a
 * <code>FileInputStream</code> or <code>FileOutputStream</code> to
 * contain it.
 * <p>
 * Applications should not create their own file descriptors.
 *
 * @author  Pavani Diwanji
 * @see     java.io.FileInputStream
 * @see     java.io.FileOutputStream
 * @since   JDK1.0
 */
public final class FileDescriptor {
    // Android-changed: Removed parent reference counting.
    // The creator is responsible for closing the file descriptor.

    // Android-changed: Renamed fd to descriptor.
    // Renaming is to avoid issues with JNI/reflection fetching the descriptor value.
    int descriptor;

    // Android-added: Track fd owner to guard against accidental closure. http://b/110100358
    // The owner on the libc side is an pointer-sized value that can be set to an arbitrary
    // value (with 0 meaning 'unowned'). libcore chooses to use System.identityHashCode.
    private long ownerId = NO_OWNER;

    // Android-added: value of ownerId indicating that a FileDescriptor is unowned.
    /** @hide */
    public static final long NO_OWNER = 0L;

    /**
     * Constructs an (invalid) FileDescriptor
     * object.
     */
    public /**/ FileDescriptor() {
        descriptor = -1;
    }

    private /* */ FileDescriptor(int descriptor) {
        this.descriptor = descriptor;
    }

    /**
     * A handle to the standard input stream. Usually, this file
     * descriptor is not used directly, but rather via the input stream
     * known as <code>System.in</code>.
     *
     * @see     java.lang.System#in
     */
    public static final FileDescriptor in = new FileDescriptor(0);

    /**
     * A handle to the standard output stream. Usually, this file
     * descriptor is not used directly, but rather via the output stream
     * known as <code>System.out</code>.
     * @see     java.lang.System#out
     */
    public static final FileDescriptor out = new FileDescriptor(1);

    /**
     * A handle to the standard error stream. Usually, this file
     * descriptor is not used directly, but rather via the output stream
     * known as <code>System.err</code>.
     *
     * @see     java.lang.System#err
     */
    public static final FileDescriptor err = new FileDescriptor(2);

    /**
     * Tests if this file descriptor object is valid.
     *
     * @return  <code>true</code> if the file descriptor object represents a
     *          valid, open file, socket, or other active I/O connection;
     *          <code>false</code> otherwise.
     */
    public boolean valid() {
        return descriptor != -1;
    }

    /**
     * Force all system buffers to synchronize with the underlying
     * device.  This method returns after all modified data and
     * attributes of this FileDescriptor have been written to the
     * relevant device(s).  In particular, if this FileDescriptor
     * refers to a physical storage medium, such as a file in a file
     * system, sync will not return until all in-memory modified copies
     * of buffers associated with this FileDescriptor have been
     * written to the physical medium.
     *
     * sync is meant to be used by code that requires physical
     * storage (such as a file) to be in a known state  For
     * example, a class that provided a simple transaction facility
     * might use sync to ensure that all changes to a file caused
     * by a given transaction were recorded on a storage medium.
     *
     * sync only affects buffers downstream of this FileDescriptor.  If
     * any in-memory buffering is being done by the application (for
     * example, by a BufferedOutputStream object), those buffers must
     * be flushed into the FileDescriptor (for example, by invoking
     * OutputStream.flush) before that data will be affected by sync.
     *
     * @exception SyncFailedException
     *        Thrown when the buffers cannot be flushed,
     *        or because the system cannot guarantee that all the
     *        buffers have been synchronized with physical media.
     * @since     JDK1.1
     */
    public void sync() throws SyncFailedException {
        try {
            if (Libcore.os.isatty(this)) {
                Libcore.os.tcdrain(this);
            } else {
                Libcore.os.fsync(this);
            }
        } catch (ErrnoException errnoException) {
            SyncFailedException sfe = new SyncFailedException(errnoException.getMessage());
            sfe.initCause(errnoException);
            throw sfe;
        }
    }

    // Android-removed: initIDs not used to allow compile-time initialization.
    /* This routine initializes JNI field offsets for the class */
    //private static native void initIDs();

    // Android-added: Needed for framework to access descriptor value.
    /**
     * Returns the int descriptor. It's highly unlikely you should be calling this. Please discuss
     * your needs with a libcore maintainer before using this method.
     * @hide internal use only
     */
    public final int getInt$() {
        return descriptor;
    }

    // Android-added: Needed for framework to access descriptor value.
    /**
     * Sets the int descriptor. It's highly unlikely you should be calling this. Please discuss
     * your needs with a libcore maintainer before using this method.
     * @hide internal use only
     */
    public final void setInt$(int fd) {
        this.descriptor = fd;
    }

    @Override public String toString() {
        return "FileDescriptor[" + descriptor + "]";
    }

    // BEGIN Android-added: Method to clone standard file descriptors.
    // Required as a consequence of RuntimeInit#redirectLogStreams. Cloning is used in
    // ZygoteHooks.onEndPreload().
    /**
     * Clones the current native file descriptor and uses this for this FileDescriptor instance.
     *
     * This method does not close the current native file descriptor.
     *
     * @hide internal use only
     */
    /*
    public void cloneForFork() {
        try {
            int newDescriptor = Os.fcntlInt(this, F_DUPFD_CLOEXEC, 0);
            this.descriptor = newDescriptor;
        } catch (ErrnoException e) {
            throw new RuntimeException(e);
        }
    }
     */
    // END Android-added: Method to clone standard file descriptors.

    // BEGIN Android-added: Methods to enable ownership enforcement of Unix file descriptors.
    /**
     * Returns the owner ID of this FileDescriptor. It's highly unlikely you should be calling this.
     * Please discuss your needs with a libcore maintainer before using this method.
     * @hide internal use only
     */
    public long getOwnerId$() {
        return this.ownerId;
    }

    /**
     * Sets the owner ID of this FileDescriptor. The owner ID does not need to be unique, but it is
     * assumed that clashes are rare. See bionic/include/android/fdsan.h for more details.
     *
     * It's highly unlikely you should be calling this.
     * Please discuss your needs with a libcore maintainer before using this method.
     * @param owner the owner ID of the Object that is responsible for closing this FileDescriptor
     * @hide internal use only
     */
    public void setOwnerId$(long newOwnerId) {
        this.ownerId = newOwnerId;
    }

    /**
     * Returns a copy of this FileDescriptor, and sets this to an invalid state.
     * @hide internal use only
     */
    public FileDescriptor release$() {
      FileDescriptor result = new FileDescriptor();
      result.descriptor = this.descriptor;
      result.ownerId = this.ownerId;
      this.descriptor = -1;
      this.ownerId = FileDescriptor.NO_OWNER;
      return result;
    }
    // END Android-added: Methods to enable ownership enforcement of Unix file descriptors.

    // Android-added: Needed for framework to test if it's a socket.
    /**
     * @hide internal use only
     */
    public boolean isSocket$() {
        return isSocket(descriptor);
    }

    private static native boolean isSocket(int descriptor);
}
