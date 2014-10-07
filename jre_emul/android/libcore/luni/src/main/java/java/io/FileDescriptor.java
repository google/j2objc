/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.io;

import libcore.io.ErrnoException;
import libcore.io.Libcore;
import static libcore.io.OsConstants.*;

/**
 * Wraps a Unix file descriptor. It's possible to get the file descriptor used by some
 * classes (such as {@link FileInputStream}, {@link FileOutputStream},
 * and {@link RandomAccessFile}), and then create new streams that point to the same
 * file descriptor.
 */
public final class FileDescriptor {

    /**
     * Corresponds to {@code stdin}.
     */
    public static final FileDescriptor in = new FileDescriptor();

    /**
     * Corresponds to {@code stdout}.
     */
    public static final FileDescriptor out = new FileDescriptor();

    /**
     * Corresponds to {@code stderr}.
     */
    public static final FileDescriptor err = new FileDescriptor();

    /**
     * The Unix file descriptor backing this FileDescriptor.
     * A value of -1 indicates that this FileDescriptor is invalid.
     */
    private int descriptor = -1;

    static {
        in.descriptor = STDIN_FILENO;
        out.descriptor = STDOUT_FILENO;
        err.descriptor = STDERR_FILENO;
    }

    /**
     * Constructs a new invalid FileDescriptor.
     */
    public FileDescriptor() {
    }

    /**
     * Ensures that data which is buffered within the underlying implementation
     * is written out to the appropriate device before returning.
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

    /**
     * Tests whether this {@code FileDescriptor} is valid.
     */
    public boolean valid() {
        return descriptor != -1;
    }

    /**
     * Returns the int fd. It's highly unlikely you should be calling this. Please discuss
     * your needs with a libcore maintainer before using this method.
     * @hide internal use only
     */
    public final int getInt$() {
        return descriptor;
    }

    /**
     * Sets the int fd. It's highly unlikely you should be calling this. Please discuss
     * your needs with a libcore maintainer before using this method.
     * @hide internal use only
     */
    public final void setInt$(int fd) {
        this.descriptor = fd;
    }

    @Override public String toString() {
        return "FileDescriptor[" + descriptor + "]";
    }
}
