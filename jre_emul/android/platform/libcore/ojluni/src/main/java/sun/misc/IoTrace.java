/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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

package sun.misc;

import java.net.InetAddress;

/**
 * Utility class used to identify trace points for I/O calls.
 * <p>
 * To use this class, a diagnostic tool must redefine this class with a version
 * that contains calls to the the diagnostic tool. This implementation will then
 * receive callbacks when file and socket operations are performed. The reason
 * for requiring a redefine of the class is to avoid any overhead caused by the
 * instrumentation.
 * <p>
 * The xxBegin() methods return a "context". This can be any Object. This
 * context will be passed to the corresponding xxEnd() method. This way, an
 * implementation can correlate the beginning of an operation with the end.
 * <p>
 * It is possible for a xxEnd() method to be called with a null handle. This
 * happens if tracing was started between the call to xxBegin() and xxEnd(), in
 * which case xxBegin() would not have been called. It is the implementation's
 * responsibility to not throw an exception in this case.
 * <p>
 * Only blocking I/O operations are identified with this facility.
 * <p>
 * <b>Warning</b>
 * <p>
 * These methods are called from sensitive points in the I/O subsystem. Great
 * care must be taken to not interfere with ongoing operations or cause
 * deadlocks. In particular:
 * <ul>
 * <li>Implementations must not throw exceptions since this will cause
 * disruptions to the I/O operations.
 * <li>Implementations must not do I/O operations since this will lead to an
 * endless loop.
 * <li>Since the hooks may be called while holding low-level locks in the I/O
 * subsystem, implementations must be careful with synchronization or
 * interaction with other threads to avoid deadlocks in the VM.
 * </ul>
 */
public final class IoTrace {
    private IoTrace() {
    }

    /**
     * Called before data is read from a socket.
     *
     * @return a context object
     */
    public static Object socketReadBegin() {
        return null;
    }

    /**
     * Called after data is read from the socket.
     *
     * @param context
     *            the context returned by the previous call to socketReadBegin()
     * @param address
     *            the remote address the socket is bound to
     * @param port
     *            the remote port the socket is bound to
     * @param timeout
     *            the SO_TIMEOUT value of the socket (in milliseconds) or 0 if
     *            there is no timeout set
     * @param bytesRead
     *            the number of bytes read from the socket, 0 if there was an
     *            error reading from the socket
     */
    public static void socketReadEnd(Object context, InetAddress address, int port,
                                     int timeout, long bytesRead) {
    }

    /**
     * Called before data is written to a socket.
     *
     * @return a context object
     */
    public static Object socketWriteBegin() {
        return null;
    }

    /**
     * Called after data is written to a socket.
     *
     * @param context
     *            the context returned by the previous call to
     *            socketWriteBegin()
     * @param address
     *            the remote address the socket is bound to
     * @param port
     *            the remote port the socket is bound to
     * @param bytesWritten
     *            the number of bytes written to the socket, 0 if there was an
     *            error writing to the socket
     */
    public static void socketWriteEnd(Object context, InetAddress address, int port,
                                      long bytesWritten) {
    }

    /**
     * Called before data is read from a file.
     *
     * @param path
     *            the path of the file
     * @return a context object
     */
    public static Object fileReadBegin(String path) {
        return null;
    }

    /**
     * Called after data is read from a file.
     *
     * @param context
     *            the context returned by the previous call to fileReadBegin()
     * @param bytesRead
     *            the number of bytes written to the file, 0 if there was an
     *            error writing to the file
     */
    public static void fileReadEnd(Object context, long bytesRead) {
    }

    /**
     * Called before data is written to a file.
     *
     * @param path
     *            the path of the file
     * @return a context object
     */
    public static Object fileWriteBegin(String path) {
        return null;
    }

    /**
     * Called after data is written to a file.
     *
     * @param context
     *            the context returned by the previous call to fileReadBegin()
     * @param bytesWritten
     *            the number of bytes written to the file, 0 if there was an
     *            error writing to the file
     */
    public static void fileWriteEnd(Object context, long bytesWritten) {
    }
}
