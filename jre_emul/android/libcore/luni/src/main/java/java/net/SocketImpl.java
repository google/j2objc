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

package java.net;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class is the base of all streaming socket implementation classes.
 * Streaming sockets are wrapped by two classes, {@code ServerSocket} and
 * {@code Socket} at the server and client end of a connection. At the server,
 * there are two types of sockets engaged in communication, the {@code
 * ServerSocket} on a well known port (referred to as listener) used to
 * establish a connection and the resulting {@code Socket} (referred to as
 * host).
 */
public abstract class SocketImpl implements SocketOptions {

    /**
     * The remote address this socket is connected to.
     */
    protected InetAddress address;

    /**
     * The remote port this socket is connected to.
     */
    protected int port;

    /**
     * The file descriptor of this socket.
     */
    protected FileDescriptor fd;

    /**
     * The local port this socket is connected to.
     */
    protected int localport;

    /**
     * Waits for an incoming request and blocks until the connection is opened
     * on the given socket.
     *
     * @param newSocket
     *            the socket to accept connections on.
     * @throws IOException
     *             if an error occurs while accepting a new connection.
     */
    protected abstract void accept(SocketImpl newSocket) throws IOException;

    /**
     * Returns the available number of bytes which are readable from this socket
     * without blocking.
     *
     * @return the number of bytes that may be read without blocking.
     * @throws IOException
     *             if an error occurs while reading the number of bytes.
     */
    protected abstract int available() throws IOException;

    /**
     * Binds this socket to the specified local host address and port number.
     *
     * @param address
     *            the local machine address to bind this socket to.
     * @param port
     *            the port on the local machine to bind this socket to.
     * @throws IOException
     *             if an error occurs while binding this socket.
     */
    protected abstract void bind(InetAddress address, int port) throws IOException;

    /**
     * Closes this socket. This makes later access invalid.
     *
     * @throws IOException
     *             if an error occurs while closing this socket.
     */
    protected abstract void close() throws IOException;

    /**
     * Connects this socket to the specified remote host and port number.
     *
     * @param host
     *            the remote host this socket has to be connected to.
     * @param port
     *            the remote port on which this socket has to be connected.
     * @throws IOException
     *             if an error occurs while connecting to the remote host.
     */
    protected abstract void connect(String host, int port) throws IOException;

    /**
     * Connects this socket to the specified remote host address and port
     * number.
     *
     * @param address
     *            the remote host address this socket has to be connected to.
     * @param port
     *            the remote port on which this socket has to be connected.
     * @throws IOException
     *             if an error occurs while connecting to the remote host.
     */
    protected abstract void connect(InetAddress address, int port)
            throws IOException;

    /**
     * Creates a new unconnected socket. The argument {@code isStreaming}
     * defines whether the new socket is a streaming or a datagram socket.
     *
     * @param isStreaming
     *            defines whether the type of the new socket is streaming or
     *            datagram.
     * @throws IOException
     *             if an error occurs while creating the socket.
     */
    protected abstract void create(boolean isStreaming) throws IOException;

    /**
     * Gets the file descriptor of this socket.
     *
     * @return the file descriptor of this socket.
     */
    protected FileDescriptor getFileDescriptor() {
        return fd;
    }

    /**
     * @hide used by java.nio
     */
    public FileDescriptor getFD$() {
        return fd;
    }

    /**
     * Gets the remote address this socket is connected to.
     *
     * @return the remote address of this socket.
     */
    protected InetAddress getInetAddress() {
        return address;
    }

    /**
     * Gets the input stream of this socket.
     *
     * @return the input stream of this socket.
     * @throws IOException
     *             if an error occurs while accessing the input stream.
     */
    protected abstract InputStream getInputStream() throws IOException;

    /**
     * Gets the local port number of this socket. The field is initialized to
     * {@code -1} and upon demand will go to the IP stack to get the bound
     * value. See the class comment for the context of the local port.
     *
     * @return the local port number this socket is bound to.
     */
    protected int getLocalPort() {
        return localport;
    }

    /**
     * Gets the output stream of this socket.
     *
     * @return the output stream of this socket.
     * @throws IOException
     *             if an error occurs while accessing the output stream.
     */
    protected abstract OutputStream getOutputStream() throws IOException;

    /**
     * Gets the remote port number of this socket. This value is not meaningful
     * when this instance is wrapped by a {@code ServerSocket}.
     *
     * @return the remote port this socket is connected to.
     */
    protected int getPort() {
        return port;
    }

    /**
     * Listens for connection requests on this streaming socket. Incoming
     * connection requests are queued up to the limit specified by {@code
     * backlog}. Additional requests are rejected. This method
     * may only be invoked on stream sockets.
     *
     * @param backlog
     *            the maximum number of outstanding connection requests.
     * @throws IOException
     *             if an error occurs while listening.
     */
    protected abstract void listen(int backlog) throws IOException;

    /**
     * Returns a string containing a concise, human-readable description of the
     * socket.
     *
     * @return the textual representation of this socket.
     */
    @Override
    public String toString() {
        return "Socket[address=" + getInetAddress() +
                ",port=" + port + ",localPort=" + getLocalPort() + "]";
    }

    /**
     * Closes the input channel of this socket.
     *
     * <p>This default implementation always throws an {@link IOException} to
     * indicate that the subclass should have overridden this method.
     *
     * @throws IOException
     *             always because this method should be overridden.
     */
    protected void shutdownInput() throws IOException {
        throw new IOException("Method has not been implemented");
    }

    /**
     * Closes the output channel of this socket.
     *
     * <p>This default implementation always throws an {@link IOException} to
     * indicate that the subclass should have overridden this method.
     *
     * @throws IOException
     *             always because this method should be overridden.
     */
    protected void shutdownOutput() throws IOException {
        throw new IOException("Method has not been implemented");
    }

    /**
     * Connects this socket to the remote host address and port number specified
     * by the {@code SocketAddress} object with the given timeout. This method
     * will block indefinitely if the timeout is set to zero.
     *
     * @param remoteAddr
     *            the remote host address and port number to connect to.
     * @param timeout
     *            the timeout value in milliseconds.
     * @throws IOException
     *             if an error occurs while connecting.
     */
    protected abstract void connect(SocketAddress remoteAddr, int timeout) throws IOException;

    /**
     * Returns whether the socket supports urgent data or not. Subclasses should
     * override this method.
     *
     * @return {@code false} because subclasses must override this method.
     */
    protected boolean supportsUrgentData() {
        return false;
    }

    /**
     * Sends the single byte of urgent data on the socket.
     *
     * @param value
     *            the byte of urgent data.
     * @throws IOException
     *             if an error occurs sending urgent data.
     */
    protected abstract void sendUrgentData(int value) throws IOException;

    /**
     * Sets performance preference for connection time, latency and bandwidth.
     * Does nothing by default.
     *
     * @param connectionTime
     *            the importance of connect time.
     * @param latency
     *            the importance of latency.
     * @param bandwidth
     *            the importance of bandwidth.
     */
    protected void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
    }

    /**
     * Initialize the bind() state.
     * @hide used in java.nio.
     */
    public void onBind(InetAddress localAddress, int localPort) {
        // Do not add any code to these methods. They are concrete only to preserve API
        // compatibility.
    }

    /**
     * Initialize the connect() state.
     * @hide used in java.nio.
     */
    public void onConnect(InetAddress remoteAddress, int remotePort) {
        // Do not add any code to these methods. They are concrete only to preserve API
        // compatibility.
    }

    /**
     * Initialize the close() state.
     * @hide used in java.nio.
     */
    public void onClose() {
        // Do not add any code to these methods. They are concrete only to preserve API
        // compatibility.
    }
}
