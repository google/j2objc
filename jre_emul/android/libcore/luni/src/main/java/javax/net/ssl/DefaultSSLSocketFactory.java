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

package javax.net.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import libcore.util.EmptyArray;

/**
 * Default inoperative implementation of javax.net.ssl.SSLSocketFactory
 *
 */
class DefaultSSLSocketFactory extends SSLSocketFactory {

    private final String errMessage;

    DefaultSSLSocketFactory(String mes) {
        errMessage = mes;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return EmptyArray.STRING;
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return EmptyArray.STRING;
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose)
            throws IOException {
        throw new SocketException(errMessage);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        throw new SocketException(errMessage);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        throw new SocketException(errMessage);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        throw new SocketException(errMessage);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
            int localPort) throws IOException {
        throw new SocketException(errMessage);
    }

}
