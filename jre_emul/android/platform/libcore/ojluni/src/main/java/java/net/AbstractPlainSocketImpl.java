/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.net;

import com.google.j2objc.annotations.RetainedWith;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileDescriptor;

/* J2ObjC removed
import dalvik.annotation.optimization.ReachabilitySensitive;
*/
import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import dalvik.system.SocketTagger;
import sun.net.ConnectionResetException;
import sun.net.NetHooks;
import sun.net.ResourceManager;

/**
 * Default Socket Implementation. This implementation does
 * not implement any security checks.
 * Note this class should <b>NOT</b> be public.
 *
 * @author  Steven B. Byrne
 */
abstract class AbstractPlainSocketImpl extends SocketImpl
{
    /* instance variable for SO_TIMEOUT */
    int timeout;   // timeout in millisec
    // Android-removed: traffic class is set through socket.
    // private int trafficClass;

    private boolean shut_rd = false;
    private boolean shut_wr = false;

    @RetainedWith
    private SocketInputStream socketInputStream = null;
    @RetainedWith
    private SocketOutputStream socketOutputStream = null;

    /* number of threads using the FileDescriptor */
    protected int fdUseCount = 0;

    /* lock when increment/decrementing fdUseCount */
    // Android-added: @ReachabilitySensitive.
    // Marked mostly because it's used where fd is, and fd isn't declared here.
    // This adds reachabilityFences where we would if fd were annotated.
    /* J2ObjC removed. @ReachabilitySensitive */
    protected final Object fdLock = new Object();

    /* indicates a close is pending on the file descriptor */
    protected boolean closePending = false;

    /* indicates connection reset state */
    private int CONNECTION_NOT_RESET = 0;
    private int CONNECTION_RESET_PENDING = 1;
    private int CONNECTION_RESET = 2;
    private int resetState;
    private final Object resetLock = new Object();

   /* whether this Socket is a stream (TCP) socket or not (UDP)
    */
    protected boolean stream;

    // BEGIN Android-removed: Android doesn't need to load native net library.
    /*
    /**
     * Load net library into runtime.
     *
    static {
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Void>() {
                public Void run() {
                    System.loadLibrary("net");
                    return null;
                }
            });
    }
    */
    // END Android-removed: Android doesn't need to load native net library.

    // Android-added: logs a warning if socket is not closed.
    /* J2ObjC removed. @ReachabilitySensitive */
    private final CloseGuard guard = CloseGuard.get();

    /**
     * Creates a socket with a boolean that specifies whether this
     * is a stream socket (true) or an unconnected UDP socket (false).
     */
    protected synchronized void create(boolean stream) throws IOException {
        this.stream = stream;
        if (!stream) {
            ResourceManager.beforeUdpCreate();
            // Android-removed: socketCreate should set fd if it succeeds.
            // fd = new FileDescriptor();
            try {
                socketCreate(false);
            } catch (IOException ioe) {
                ResourceManager.afterUdpClose();
                // Android-changed: Closed sockets use an invalid fd, not null. b/26470377
                // fd = null;
                throw ioe;
            }
        } else {
            // Android-removed: socketCreate should set fd if it succeeds.
            // fd = new FileDescriptor();
            socketCreate(true);
        }
        if (socket != null)
            socket.setCreated();
        if (serverSocket != null)
            serverSocket.setCreated();

        // Android-added: CloseGuard.
        if (fd != null && fd.valid()) {
            guard.open("close");
        }
    }

    /**
     * Creates a socket and connects it to the specified port on
     * the specified host.
     * @param host the specified host
     * @param port the specified port
     */
    protected void connect(String host, int port)
        throws UnknownHostException, IOException
    {
        boolean connected = false;
        try {
            InetAddress address = InetAddress.getByName(host);
            this.port = port;
            this.address = address;

            connectToAddress(address, port, timeout);
            connected = true;
        } finally {
            if (!connected) {
                try {
                    close();
                } catch (IOException ioe) {
                    /* Do nothing. If connect threw an exception then
                       it will be passed up the call stack */
                }
            }
        }
    }

    /**
     * Creates a socket and connects it to the specified address on
     * the specified port.
     * @param address the address
     * @param port the specified port
     */
    protected void connect(InetAddress address, int port) throws IOException {
        this.port = port;
        this.address = address;

        try {
            connectToAddress(address, port, timeout);
            return;
        } catch (IOException e) {
            // everything failed
            close();
            throw e;
        }
    }

    /**
     * Creates a socket and connects it to the specified address on
     * the specified port.
     * @param address the address
     * @param timeout the timeout value in milliseconds, or zero for no timeout.
     * @throws IOException if connection fails
     * @throws  IllegalArgumentException if address is null or is a
     *          SocketAddress subclass not supported by this socket
     * @since 1.4
     */
    protected void connect(SocketAddress address, int timeout)
            throws IOException {
        boolean connected = false;
        try {
            if (address == null || !(address instanceof InetSocketAddress))
                throw new IllegalArgumentException("unsupported address type");
            InetSocketAddress addr = (InetSocketAddress) address;
            if (addr.isUnresolved())
                throw new UnknownHostException(addr.getHostName());
            this.port = addr.getPort();
            this.address = addr.getAddress();

            connectToAddress(this.address, port, timeout);
            connected = true;
        } finally {
            if (!connected) {
                try {
                    close();
                } catch (IOException ioe) {
                    /* Do nothing. If connect threw an exception then
                       it will be passed up the call stack */
                }
            }
        }
    }

    private void connectToAddress(InetAddress address, int port, int timeout) throws IOException {
        if (address.isAnyLocalAddress()) {
            doConnect(InetAddress.getLocalHost(), port, timeout);
        } else {
            doConnect(address, port, timeout);
        }
    }

    public void setOption(int opt, Object val) throws SocketException {
        if (isClosedOrPending()) {
            throw new SocketException("Socket Closed");
        }
        // BEGIN Android-removed: Logic dealing with value type moved to socketSetOption.
        /*
        boolean on = true;
        switch (opt) {
            /* check type safety b4 going native.  These should never
             * fail, since only java.Socket* has access to
             * PlainSocketImpl.setOption().
             *
        case SO_LINGER:
            if (val == null || (!(val instanceof Integer) && !(val instanceof Boolean)))
                throw new SocketException("Bad parameter for option");
            if (val instanceof Boolean) {
                /* true only if disabling - enabling should be Integer *
                on = false;
            }
            break;
        case SO_TIMEOUT:
            if (val == null || (!(val instanceof Integer)))
                throw new SocketException("Bad parameter for SO_TIMEOUT");
            int tmp = ((Integer) val).intValue();
            if (tmp < 0)
                throw new IllegalArgumentException("timeout < 0");
            timeout = tmp;
            break;
        case IP_TOS:
             if (val == null || !(val instanceof Integer)) {
                 throw new SocketException("bad argument for IP_TOS");
             }
             trafficClass = ((Integer)val).intValue();
             break;
        case SO_BINDADDR:
            throw new SocketException("Cannot re-bind socket");
        case TCP_NODELAY:
            if (val == null || !(val instanceof Boolean))
                throw new SocketException("bad parameter for TCP_NODELAY");
            on = ((Boolean)val).booleanValue();
            break;
        case SO_SNDBUF:
        case SO_RCVBUF:
            if (val == null || !(val instanceof Integer) ||
                !(((Integer)val).intValue() > 0)) {
                throw new SocketException("bad parameter for SO_SNDBUF " +
                                          "or SO_RCVBUF");
            }
            break;
        case SO_KEEPALIVE:
            if (val == null || !(val instanceof Boolean))
                throw new SocketException("bad parameter for SO_KEEPALIVE");
            on = ((Boolean)val).booleanValue();
            break;
        case SO_OOBINLINE:
            if (val == null || !(val instanceof Boolean))
                throw new SocketException("bad parameter for SO_OOBINLINE");
            on = ((Boolean)val).booleanValue();
            break;
        case SO_REUSEADDR:
            if (val == null || !(val instanceof Boolean))
                throw new SocketException("bad parameter for SO_REUSEADDR");
            on = ((Boolean)val).booleanValue();
            break;
        default:
            throw new SocketException("unrecognized TCP option: " + opt);
        }
        socketSetOption(opt, on, val);
        */
        // END Android-removed: Logic dealing with value type moved to socketSetOption.
        // Android-added: Keep track of timeout value not handled by socketSetOption.
        if (opt == SO_TIMEOUT) {
            timeout = (Integer) val;
        }
        socketSetOption(opt, val);
    }
    public Object getOption(int opt) throws SocketException {
        if (isClosedOrPending()) {
            throw new SocketException("Socket Closed");
        }
        if (opt == SO_TIMEOUT) {
            return new Integer(timeout);
        }
        // BEGIN Android-changed: Logic dealing with value type moved to socketGetOption.
        /*
        int ret = 0;
        /*
         * The native socketGetOption() knows about 3 options.
         * The 32 bit value it returns will be interpreted according
         * to what we're asking.  A return of -1 means it understands
         * the option but its turned off.  It will raise a SocketException
         * if "opt" isn't one it understands.
         *

        switch (opt) {
        case TCP_NODELAY:
            ret = socketGetOption(opt, null);
            return Boolean.valueOf(ret != -1);
        case SO_OOBINLINE:
            ret = socketGetOption(opt, null);
            return Boolean.valueOf(ret != -1);
        case SO_LINGER:
            ret = socketGetOption(opt, null);
            return (ret == -1) ? Boolean.FALSE: (Object)(new Integer(ret));
        case SO_REUSEADDR:
            ret = socketGetOption(opt, null);
            return Boolean.valueOf(ret != -1);
        case SO_BINDADDR:
            InetAddressContainer in = new InetAddressContainer();
            ret = socketGetOption(opt, in);
            return in.addr;
        case SO_SNDBUF:
        case SO_RCVBUF:
            ret = socketGetOption(opt, null);
            return new Integer(ret);
        case IP_TOS:
            try {
                ret = socketGetOption(opt, null);
                if (ret == -1) { // ipv6 tos
                    return trafficClass;
                } else {
                    return ret;
                }
            } catch (SocketException se) {
                // TODO - should make better effort to read TOS or TCLASS
                return trafficClass; // ipv6 tos
            }
        case SO_KEEPALIVE:
            ret = socketGetOption(opt, null);
            return Boolean.valueOf(ret != -1);
        // should never get here
        default:
            return null;
        }
        */
        return socketGetOption(opt);
        // END Android-changed: Logic dealing with value type moved to socketGetOption.
    }

    /**
     * The workhorse of the connection operation.  Tries several times to
     * establish a connection to the given <host, port>.  If unsuccessful,
     * throws an IOException indicating what went wrong.
     */

    synchronized void doConnect(InetAddress address, int port, int timeout) throws IOException {
        synchronized (fdLock) {
            if (!closePending && (socket == null || !socket.isBound())) {
                NetHooks.beforeTcpConnect(fd, address, port);
            }
        }
        try {
            acquireFD();
            try {
                // Android-added: BlockGuard.
                BlockGuard.getThreadPolicy().onNetwork();
                socketConnect(address, port, timeout);
                /* socket may have been closed during poll/select */
                synchronized (fdLock) {
                    if (closePending) {
                        throw new SocketException ("Socket closed");
                    }
                }
                // If we have a ref. to the Socket, then sets the flags
                // created, bound & connected to true.
                // This is normally done in Socket.connect() but some
                // subclasses of Socket may call impl.connect() directly!
                if (socket != null) {
                    socket.setBound();
                    socket.setConnected();
                }
            } finally {
                releaseFD();
            }
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    /**
     * Binds the socket to the specified address of the specified local port.
     * @param address the address
     * @param lport the port
     */
    protected synchronized void bind(InetAddress address, int lport)
        throws IOException
    {
       synchronized (fdLock) {
            if (!closePending && (socket == null || !socket.isBound())) {
                NetHooks.beforeTcpBind(fd, address, lport);
            }
        }
        socketBind(address, lport);
        if (socket != null)
            socket.setBound();
        if (serverSocket != null)
            serverSocket.setBound();
    }

    /**
     * Listens, for a specified amount of time, for connections.
     * @param count the amount of time to listen for connections
     */
    protected synchronized void listen(int count) throws IOException {
        socketListen(count);
    }

    /**
     * Accepts connections.
     * @param s the connection
     */
    protected void accept(SocketImpl s) throws IOException {
        acquireFD();
        try {
            // Android-added: BlockGuard.
            BlockGuard.getThreadPolicy().onNetwork();
            socketAccept(s);
        } finally {
            releaseFD();
        }
    }

    /**
     * Gets an InputStream for this socket.
     */
    protected synchronized InputStream getInputStream() throws IOException {
        synchronized (fdLock) {
            if (isClosedOrPending())
                throw new IOException("Socket Closed");
            if (shut_rd)
                throw new IOException("Socket input is shutdown");
            if (socketInputStream == null)
                socketInputStream = new SocketInputStream(this);
        }
        return socketInputStream;
    }

    void setInputStream(SocketInputStream in) {
        socketInputStream = in;
    }

    /**
     * Gets an OutputStream for this socket.
     */
    protected synchronized OutputStream getOutputStream() throws IOException {
        synchronized (fdLock) {
            if (isClosedOrPending())
                throw new IOException("Socket Closed");
            if (shut_wr)
                throw new IOException("Socket output is shutdown");
            if (socketOutputStream == null)
                socketOutputStream = new SocketOutputStream(this);
        }
        return socketOutputStream;
    }

    // Android-removed: this.fd is maintained by the concrete implementation.
    /*
    void setFileDescriptor(FileDescriptor fd) {
        this.fd = fd;
    }
    */

    void setAddress(InetAddress address) {
        this.address = address;
    }

    void setPort(int port) {
        this.port = port;
    }

    void setLocalPort(int localport) {
        this.localport = localport;
    }

    /**
     * Returns the number of bytes that can be read without blocking.
     */
    protected synchronized int available() throws IOException {
        if (isClosedOrPending()) {
            throw new IOException("Stream closed.");
        }

        /*
         * If connection has been reset or shut down for input, then return 0
         * to indicate there are no buffered bytes.
         */
        if (isConnectionReset() || shut_rd) {
            return 0;
        }

        /*
         * If no bytes available and we were previously notified
         * of a connection reset then we move to the reset state.
         *
         * If are notified of a connection reset then check
         * again if there are bytes buffered on the socket.
         */
        int n = 0;
        try {
            n = socketAvailable();
            if (n == 0 && isConnectionResetPending()) {
                setConnectionReset();
            }
        } catch (ConnectionResetException exc1) {
            setConnectionResetPending();
            try {
                n = socketAvailable();
                if (n == 0) {
                    setConnectionReset();
                }
            } catch (ConnectionResetException exc2) {
            }
        }
        return n;
    }

    /**
     * Closes the socket.
     */
    protected void close() throws IOException {
        synchronized(fdLock) {
            if (fd != null && fd.valid()) {
                if (!stream) {
                    ResourceManager.afterUdpClose();
                }
                // Android-changed: Socket should be untagged before the preclose.
                // After preclose, socket will dup2-ed to marker_fd, therefore, it won't describe
                // the same file.  If closingPending is true, then the socket has been preclosed.
                //
                // Also, close the CloseGuard when the #close is called.
                if (!closePending) {
                    closePending = true;
                    guard.close();

                    if (fdUseCount == 0) {
                        /*
                         * We close the FileDescriptor in two-steps - first the
                         * "pre-close" which closes the socket but doesn't
                         * release the underlying file descriptor. This operation
                         * may be lengthy due to untransmitted data and a long
                         * linger interval. Once the pre-close is done we do the
                         * actual socket to release the fd.
                         */
                        try {
                            socketPreClose();
                        } finally {
                            socketClose();
                        }
                        // Android-changed: Closed sockets use an invalid fd, not null. b/26470377
                        // socketClose invalidates the fd by closing the fd.
                        // fd = null;
                        return;
                    } else {
                        /*
                         * If a thread has acquired the fd and a close
                         * isn't pending then use a deferred close.
                         * Also decrement fdUseCount to signal the last
                         * thread that releases the fd to close it.
                         */
                        fdUseCount--;
                        socketPreClose();
                    }
                }
            }
        }
    }

    void reset() throws IOException {
        if (fd != null && fd.valid()) {
            socketClose();
            // Android-changed: Notified the CloseGuard object as the fd has been released.
            guard.close();
        }
        // Android-changed: Closed sockets use an invalid fd, not null. b/26470377
        // fd = null;
        super.reset();
    }


    /**
     * Shutdown read-half of the socket connection;
     */
    protected void shutdownInput() throws IOException {
      // Android-changed: Closed sockets use an invalid fd, not null. b/26470377
      if (fd != null && fd.valid()) {
          socketShutdown(SHUT_RD);
          if (socketInputStream != null) {
              socketInputStream.setEOF(true);
          }
          shut_rd = true;
      }
    }

    /**
     * Shutdown write-half of the socket connection;
     */
    protected void shutdownOutput() throws IOException {
      // Android-changed: Closed sockets use an invalid fd, not null. b/26470377
      if (fd != null && fd.valid()) {
          socketShutdown(SHUT_WR);
          shut_wr = true;
      }
    }

    protected boolean supportsUrgentData () {
        return true;
    }

    protected void sendUrgentData (int data) throws IOException {
        // Android-changed: Closed sockets use an invalid fd, not null. b/26470377
        if (fd == null || !fd.valid()) {
            throw new IOException("Socket Closed");
        }
        socketSendUrgentData (data);
    }

    /**
     * Cleans up if the user forgets to close it.
     */
    protected void finalize() throws IOException {
        // Android-added: CloseGuard.
        if (guard != null) {
            guard.warnIfOpen();
        }

        close();
    }

    /*
     * "Acquires" and returns the FileDescriptor for this impl
     *
     * A corresponding releaseFD is required to "release" the
     * FileDescriptor.
     */
    FileDescriptor acquireFD() {
        synchronized (fdLock) {
            fdUseCount++;
            return fd;
        }
    }

    /*
     * "Release" the FileDescriptor for this impl.
     *
     * If the use count goes to -1 then the socket is closed.
     */
    void releaseFD() {
        synchronized (fdLock) {
            fdUseCount--;
            if (fdUseCount == -1) {
                if (fd != null) {
                    try {
                        socketClose();
                    } catch (IOException e) {
                        // Android-changed: Closed sockets use an invalid fd, not null. b/26470377
                        // socketClose invalidates the fd by closing the fd.
                        // } finally {
                        //     fd = null;
                    }
                }
            }
        }
    }

    public boolean isConnectionReset() {
        synchronized (resetLock) {
            return (resetState == CONNECTION_RESET);
        }
    }

    public boolean isConnectionResetPending() {
        synchronized (resetLock) {
            return (resetState == CONNECTION_RESET_PENDING);
        }
    }

    public void setConnectionReset() {
        synchronized (resetLock) {
            resetState = CONNECTION_RESET;
        }
    }

    public void setConnectionResetPending() {
        synchronized (resetLock) {
            if (resetState == CONNECTION_NOT_RESET) {
                resetState = CONNECTION_RESET_PENDING;
            }
        }

    }

    /*
     * Return true if already closed or close is pending
     */
    public boolean isClosedOrPending() {
        /*
         * Lock on fdLock to ensure that we wait if a
         * close is in progress.
         */
        synchronized (fdLock) {
            // Android-changed: Closed sockets use an invalid fd, not null. b/26470377
            if (closePending || (fd == null) || !fd.valid()) {
                return true;
            } else {
                return false;
            }
        }
    }

    /*
     * Return the current value of SO_TIMEOUT
     */
    public int getTimeout() {
        return timeout;
    }

    /*
     * "Pre-close" a socket by dup'ing the file descriptor - this enables
     * the socket to be closed without releasing the file descriptor.
     */
    private void socketPreClose() throws IOException {
        socketClose0(true);
    }

    /*
     * Close the socket (and release the file descriptor).
     */
    protected void socketClose() throws IOException {
        socketClose0(false);
    }

    abstract void socketCreate(boolean isServer) throws IOException;
    abstract void socketConnect(InetAddress address, int port, int timeout)
        throws IOException;
    abstract void socketBind(InetAddress address, int port)
        throws IOException;
    abstract void socketListen(int count)
        throws IOException;
    abstract void socketAccept(SocketImpl s)
        throws IOException;
    abstract int socketAvailable()
        throws IOException;
    abstract void socketClose0(boolean useDeferredClose)
        throws IOException;
    abstract void socketShutdown(int howto)
        throws IOException;

    // Android-changed: Method signature changes.
    // socket{Get,Set}Option work directly with Object values.
    abstract void socketSetOption(int cmd, Object value) throws SocketException;
    abstract Object socketGetOption(int opt) throws SocketException;

    abstract void socketSendUrgentData(int data)
        throws IOException;

    public final static int SHUT_RD = 0;
    public final static int SHUT_WR = 1;
}
