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
import java.net.ServerSocket;
import java.net.SocketException;

/**
 * This abstract class defines methods to create server sockets. It can be
 * subclassed to create specific server socket types.
 */
public abstract class ServerSocketFactory {
    private static ServerSocketFactory defaultFactory;

    /**
     * Gets the default server socket factory of the system which can be used to
     * create new server sockets without creating a subclass of this factory.
     *
     * @return the system default server socket factory.
     */
    public static synchronized ServerSocketFactory getDefault() {
        if (defaultFactory == null) {
            defaultFactory = new DefaultServerSocketFactory();
        }
        return defaultFactory;
    }

    /**
     * Creates a new {@code ServerSocketFactory} instance.
     */
    protected ServerSocketFactory() {
    }

    /**
     * Creates a new server socket which is not bound to any local address. This
     * method has to be overridden by a subclass otherwise a {@code
     * SocketException} is thrown.
     *
     * @return the created unbound server socket.
     * @throws IOException
     *             if an error occurs while creating a new server socket.
     */
    public ServerSocket createServerSocket() throws IOException {
        // follow RI's behavior
        throw new SocketException("Unbound server sockets not implemented");
    }

    /**
     * Creates a new server socket which is bound to the given port with a
     * maximum backlog of 50 unaccepted connections.
     *
     * @param port the port on which the created socket has to listen.
     * @return the created bound server socket.
     * @throws IOException
     *             if an error occurs while creating a new server socket.
     */
    public abstract ServerSocket createServerSocket(int port) throws IOException;

    /**
     * Creates a new server socket which is bound to the given port and
     * configures its maximum of queued connections.
     *
     * @param port the port on which the created socket has to listen.
     * @param backlog the maximum number of unaccepted connections. Passing 0 or
     *     a negative value yields the default backlog of 50.
     * @return the created bound server socket.
     * @throws IOException if an error occurs while creating a new server socket.
     */
    public abstract ServerSocket createServerSocket(int port, int backlog) throws IOException;

    /**
     * Creates a new server socket which is bound to the given address on the
     * specified port and configures its maximum of queued connections.
     *
     * @param port the port on which the created socket has to listen.
     * @param backlog the maximum number of unaccepted connections. Passing 0 or
     *     a negative value yields the default backlog of 50.
     * @param iAddress the address of the network interface which is used by the
     *     created socket.
     * @return the created bound server socket.
     * @throws IOException if an error occurs while creating a new server socket.
     */
    public abstract ServerSocket createServerSocket(int port, int backlog, InetAddress iAddress)
            throws IOException;

}
