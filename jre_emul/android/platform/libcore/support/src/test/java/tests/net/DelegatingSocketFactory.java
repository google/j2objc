/*
 * Copyright (C) 2015 The Android Open Source Project
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

package tests.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.SocketFactory;

/**
 * {@link SocketFactory} which delegates all invocations to the provided delegate
 * {@code SocketFactory}.
 */
public class DelegatingSocketFactory extends SocketFactory {

  private final SocketFactory mDelegate;

  public DelegatingSocketFactory(SocketFactory delegate) {
    this.mDelegate = delegate;
  }

  /**
   * Invoked after obtaining a socket from the delegate and before returning it to the caller.
   *
   * <p>The default implementation does nothing.
   */
  protected Socket configureSocket(Socket socket) throws IOException {
    return socket;
  }

  @Override
  public Socket createSocket() throws IOException {
    Socket socket = mDelegate.createSocket();
    return configureSocket(socket);
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    Socket socket = mDelegate.createSocket(host, port);
    return configureSocket(socket);
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
      throws IOException, UnknownHostException {
    Socket socket = mDelegate.createSocket(host, port, localHost, localPort);
    return configureSocket(socket);
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException {
    Socket socket = mDelegate.createSocket(host, port);
    return configureSocket(socket);
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
      int localPort) throws IOException {
    Socket socket = mDelegate.createSocket(address, port, localAddress, localPort);
    return configureSocket(socket);
  }
}
