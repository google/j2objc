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

package javax.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * This abstract class defines methods to create sockets. It can be subclassed
 * to create specific socket types with additional socket-level functionality.
 */
public abstract class SocketFactory {

    private static SocketFactory defaultFactory;

    /**
     * Gets the default socket factory of the system which can be used to create
     * new sockets without creating a subclass of this factory.
     *
     * @return the system default socket factory.
     */
    public static synchronized SocketFactory getDefault() {
        if (defaultFactory == null) {
            defaultFactory = new DefaultSocketFactory();
        }
        return defaultFactory;
    }

    /**
     * Creates a new {@code SocketFactory} instance.
     */
    protected SocketFactory() {
    }

    /**
     * Creates a new socket which is not connected to any remote host. This
     * method has to be overridden by a subclass otherwise a {@code
     * SocketException} is thrown.
     *
     * @return the created unconnected socket.
     * @throws IOException
     *             if an error occurs while creating a new socket.
     */
    public Socket createSocket() throws IOException {
        // follow RI's behavior
        throw new SocketException("Unconnected sockets not implemented");
    }

    /**
     * Creates a new socket which is connected to the remote host specified by
     * the parameters {@code host} and {@code port}. The socket is bound to any
     * available local address and port.
     *
     * @param host
     *            the remote host address the socket has to be connected to.
     * @param port
     *            the port number of the remote host at which the socket is
     *            connected.
     * @return the created connected socket.
     * @throws IOException
     *             if an error occurs while creating a new socket.
     * @throws UnknownHostException
     *             if the specified host is unknown or the IP address could not
     *             be resolved.
     */
    public abstract Socket createSocket(String host, int port) throws IOException,
            UnknownHostException;

    /**
     * Creates a new socket which is connected to the remote host specified by
     * the parameters {@code host} and {@code port}. The socket is bound to the
     * local network interface specified by the InetAddress {@code localHost} on
     * port {@code localPort}.
     *
     * @param host
     *            the remote host address the socket has to be connected to.
     * @param port
     *            the port number of the remote host at which the socket is
     *            connected.
     * @param localHost
     *            the local host address the socket is bound to.
     * @param localPort
     *            the port number of the local host at which the socket is
     *            bound.
     * @return the created connected socket.
     * @throws IOException
     *             if an error occurs while creating a new socket.
     * @throws UnknownHostException
     *             if the specified host is unknown or the IP address could not
     *             be resolved.
     */
    public abstract Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException;

    /**
     * Creates a new socket which is connected to the remote host specified by
     * the InetAddress {@code host}. The socket is bound to any available local
     * address and port.
     *
     * @param host
     *            the host address the socket has to be connected to.
     * @param port
     *            the port number of the remote host at which the socket is
     *            connected.
     * @return the created connected socket.
     * @throws IOException
     *             if an error occurs while creating a new socket.
     */
    public abstract Socket createSocket(InetAddress host, int port) throws IOException;


    /**
     * Creates a new socket which is connected to the remote host specified by
     * the InetAddress {@code address}. The socket is bound to the local network
     * interface specified by the InetAddress {@code localHost} on port {@code
     * localPort}.
     *
     * @param address
     *            the remote host address the socket has to be connected to.
     * @param port
     *            the port number of the remote host at which the socket is
     *            connected.
     * @param localAddress
     *            the local host address the socket is bound to.
     * @param localPort
     *            the port number of the local host at which the socket is
     *            bound.
     * @return the created connected socket.
     * @throws IOException
     *             if an error occurs while creating a new socket.
     */
    public abstract Socket createSocket(InetAddress address, int port, InetAddress localAddress,
            int localPort) throws IOException;
}
