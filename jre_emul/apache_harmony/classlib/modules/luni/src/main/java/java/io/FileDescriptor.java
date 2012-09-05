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

/**
 * The lowest-level representation of a file, device, or
 * socket. If is often used for wrapping an operating system "handle". Some
 * I/O classes can be queried for the FileDescriptor they are operating on, and
 * this descriptor can subsequently be used during the instantiation of another
 * I/O class, so that the new object will reuse it.
 * <p>
 * The FileDescriptor class also contains static fields representing the
 * system's standard input, output and error streams. These can be used directly
 * if desired, but it is recommended to go through System.in, System.out, and
 * System.err streams respectively.
 * <p>
 * Applications should not create new FileDescriptors.
 * 
 * @see FileInputStream#getFD()
 * @see FileOutputStream#getFD()
 * @see RandomAccessFile#getFD()
 */
public final class FileDescriptor {

    /**
     * The FileDescriptor representing standard input.
     */
    public static final FileDescriptor in = new FileDescriptor();

    /**
     * FileDescriptor representing standard out.
     */
    public static final FileDescriptor out = new FileDescriptor();

    /**
     * FileDescriptor representing standard error.
     */
    public static final FileDescriptor err = new FileDescriptor();

    /**
     * Represents a link to any underlying OS resources for this FileDescriptor.
     * A value of -1 indicates that this FileDescriptor is invalid.
     */
    long descriptor = -1;
    
    boolean readOnly = false; 

    private static native long getStdInDescriptor() /*-{
      return (long) fcntl(STDIN_FILENO, F_DUPFD, 0);
    }-*/;
    
    private static native long getStdOutDescriptor() /*-{
      return (long) fcntl(STDOUT_FILENO, F_DUPFD, 0);
    }-*/;
    
    private static native long getStdErrDescriptor() /*-{
      return (long) fcntl(STDERR_FILENO, F_DUPFD, 0);
    }-*/;

    static {
        in.descriptor = getStdInDescriptor();
        out.descriptor = getStdOutDescriptor();
        err.descriptor = getStdErrDescriptor();
    }

    /**
     * Constructs a new FileDescriptor containing an invalid handle. The
     * contained handle is usually modified by native code at a later point.
     */
    public FileDescriptor() {
        super();
    }

    /**
     * Ensures that data which is buffered within the underlying implementation
     * is written out to the appropriate device before returning.
     * 
     * @throws SyncFailedException
     *             when the operation fails.
     */
    public void sync() throws SyncFailedException {
        // if the descriptor is a read-only one, do nothing
        if (!readOnly) {
            syncImpl();
        }
    }
    
    private native void syncImpl() throws SyncFailedException /*-{
      fsync((int) descriptor_);
    }-*/;

    /**
     * Indicates whether this FileDescriptor is valid.
     * 
     * @return {@code true} if this FileDescriptor is valid, {@code false}
     *         otherwise.
     */
    public boolean valid() {
        return descriptor != -1;
    }
}
