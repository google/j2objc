/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.nio.channels;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.util.Set;

/**
 * A common interface for channels that are backed by network sockets.
 *
 * @since 1.7
 * @hide Until ready for a public API change
 */
public interface NetworkChannel extends AutoCloseable, Channel, Closeable {

  /**
   * Binds this channel to the given local socket address. If the {@code localAddr} is set
   * to {@code null} the socket will be bound to an available local address on any free port of
   * the system.
   *
   * @param local
   *     the local machine address and port to bind on.
   * @return this channel.
   * @throws UnsupportedAddressTypeException
   *     if the {@code SocketAddress} is not supported.
   * @throws ClosedChannelException
   *     if the channel is closed.
   * @throws AlreadyBoundException
   *     if the channel is already bound.
   * @throws IOException
   *     if another I/O error occurs.
   * @hide Until ready for a public API change
   */
  NetworkChannel bind(SocketAddress local) throws IOException;

  /**
   * Returns the local socket address the channel is bound to. The socket may be bound explicitly
   * via {@link #bind(java.net.SocketAddress)} or similar methods, or as a side-effect when other
   * methods are called, depending on the implementation. If the channel is not bound {@code null}
   * is returned.
   *
   * <p>If IP is being used, the returned object will be a subclass of
   * {@link java.net.InetSocketAddress}
   *
   * @return the local socket address, or {@code null} if the socket is not bound
   * @throws ClosedChannelException
   *     if the channel is closed.
   * @throws IOException
   *     if another I/O error occurs.
   * @hide Until ready for a public API change
   */
  SocketAddress getLocalAddress() throws IOException;

  /**
   * Returns the value for the socket option.
   *
   * @throws UnsupportedOperationException
   *     if the option is not supported by the socket.
   * @throws java.nio.channels.ClosedChannelException
   *     if the socket is closed
   * @throws IOException
   *     if the value cannot be read.
   * @hide Until ready for a public API change
   * @see java.net.StandardSocketOptions
   */
  <T> T getOption(SocketOption<T> option) throws IOException;

  /**
   * Sets the value for the socket option.
   *
   * @return this NetworkChannel
   * @throws UnsupportedOperationException
   *     if the option is not supported by the socket.
   * @throws IllegalArgumentException
   *     if the value is not valid for the option.
   * @throws java.nio.channels.ClosedChannelException
   *     if the socket is closed
   * @throws IOException
   *     if the value cannot be written.
   * @hide Until ready for a public API change
   * @see java.net.StandardSocketOptions
   */
  <T> NetworkChannel setOption(SocketOption<T> option, T value) throws IOException;

  /**
   * Returns the set of socket options supported by this channel.
   *
   * @hide Until ready for a public API change
   */
  Set<SocketOption<?>> supportedOptions();
}
