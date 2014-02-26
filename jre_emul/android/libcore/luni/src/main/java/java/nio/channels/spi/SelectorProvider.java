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

package java.nio.channels.spi;

import java.io.IOException;
import java.nio.SelectorProviderImpl;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ServiceLoader;

/**
 * {@code SelectorProvider} is an abstract base class that declares methods for
 * providing instances of {@link DatagramChannel}, {@link Pipe},
 * {@link java.nio.channels.Selector} , {@link ServerSocketChannel}, and
 * {@link SocketChannel}. All the methods of this class are thread-safe.
 *
 * <p>A provider instance can be retrieved through a system property or the
 * configuration file in a jar file; if no provider is available that way then
 * the system default provider is returned.
 */
public abstract class SelectorProvider {

    private static SelectorProvider provider = null;

    /**
     * Constructs a new {@code SelectorProvider}.
     */
    protected SelectorProvider() {
    }

    /**
     * Gets a provider instance by executing the following steps when called for
     * the first time:
     * <ul>
     * <li> if the system property "java.nio.channels.spi.SelectorProvider" is
     * set, the value of this property is the class name of the provider
     * returned; </li>
     * <li>if there is a provider-configuration file named
     * "java.nio.channels.spi.SelectorProvider" in META-INF/services of a jar
     * file valid in the system class loader, the first class name is the
     * provider's class name; </li>
     * <li> otherwise, a system default provider will be returned.</li>
     * </ul>
     *
     * @return the provider.
     */
    synchronized public static SelectorProvider provider() {
        if (provider == null) {
            provider = ServiceLoader.loadFromSystemProperty(SelectorProvider.class);
            if (provider == null) {
                provider = loadProviderByJar();
            }
            if (provider == null) {
                provider = new SelectorProviderImpl();
            }
        }
        return provider;
    }

    private static SelectorProvider loadProviderByJar() {
        for (SelectorProvider provider : ServiceLoader.load(SelectorProvider.class)) {
            return provider;
        }
        return null;
    }

    /**
     * Creates a new open {@code DatagramChannel}.
     *
     * @return the new channel.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public abstract DatagramChannel openDatagramChannel() throws IOException;

    /**
     * Creates a new {@code Pipe}.
     *
     * @return the new pipe.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public abstract Pipe openPipe() throws IOException;

    /**
     * Creates a new selector.
     *
     * @return the new selector.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public abstract AbstractSelector openSelector() throws IOException;

    /**
     * Creates a new open {@code ServerSocketChannel}.
     *
     * @return the new channel.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public abstract ServerSocketChannel openServerSocketChannel()
            throws IOException;

    /**
     * Create a new open {@code SocketChannel}.
     *
     * @return the new channel.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public abstract SocketChannel openSocketChannel() throws IOException;

    /**
     * Returns the channel inherited from the process that created this VM.
     * On Android, this method always returns null because stdin and stdout are
     * never connected to a socket.
     *
     * @return the channel.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public Channel inheritedChannel() throws IOException {
        // Android never has stdin/stdout connected to a socket.
        return null;
    }
}
