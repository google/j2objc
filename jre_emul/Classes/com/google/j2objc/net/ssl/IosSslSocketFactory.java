/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.j2objc.net.ssl;

import com.google.j2objc.security.IosSecurityProvider.SslProtocol;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;

/** Creates SSL sockets that use Apple's SecureTransport API. */
public class IosSslSocketFactory extends SSLSocketFactory {

  private final String[] enabledProtocols;

  public IosSslSocketFactory(SslProtocol protocol) {
    enabledProtocols = new String[] {protocol.toString()};
  }

  @Override
  public String[] getDefaultCipherSuites() {
    return new String[0];
  }

  @Override
  public String[] getSupportedCipherSuites() {
    return new String[0];
  }

  @Override
  public Socket createSocket() throws IOException {
    IosSslSocket socket = new IosSslSocket();
    socket.setEnabledProtocols(enabledProtocols);
    return socket;
  }

  @Override
  public Socket createSocket(Socket s, String host, int port, boolean autoClose)
      throws IOException {
    IosSslSocket socket = new IosSslSocket(s, host, port, autoClose);
    socket.setEnabledProtocols(enabledProtocols);
    return socket;
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    IosSslSocket socket = new IosSslSocket(host, port);
    socket.setEnabledProtocols(enabledProtocols);
    return socket;
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
      throws IOException, UnknownHostException {
    IosSslSocket socket = new IosSslSocket(host, port, localHost, localPort);
    socket.setEnabledProtocols(enabledProtocols);
    return socket;
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException {
    IosSslSocket socket = new IosSslSocket(host, port);
    socket.setEnabledProtocols(enabledProtocols);
    return socket;
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException {
    IosSslSocket socket = new IosSslSocket(address, port, localAddress, localPort);
    socket.setEnabledProtocols(enabledProtocols);
    return socket;
  }
}
