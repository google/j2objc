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

package org.apache.harmony.tests.java.nio.channels;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

class MockSocketChannel extends SocketChannel {

    protected MockSocketChannel(SelectorProvider arg0) {
        super(arg0);
    }

    @Override
    public Socket socket() {
        return null;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public boolean isConnectionPending() {
        return false;
    }

    @Override
    public boolean connect(SocketAddress arg0) throws IOException {
        return false;
    }

    @Override
    public boolean finishConnect() throws IOException {
        return false;
    }

    @Override
    public int read(ByteBuffer arg0) throws IOException {
        return 0;
    }

    @Override
    public long read(ByteBuffer[] arg0, int arg1, int arg2) throws IOException {
        return 0;
    }

    @Override
    public int write(ByteBuffer arg0) throws IOException {
        return 0;
    }

    @Override
    public long write(ByteBuffer[] arg0, int arg1, int arg2) throws IOException {
        return 0;
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
    }

    @Override
    protected void implConfigureBlocking(boolean arg0) throws IOException {
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        return null;
    }

    @Override
    public SocketChannel shutdownOutput() throws IOException {
        return null;
    }

    @Override
    public SocketChannel shutdownInput() throws IOException {
        return null;
    }

    @Override
    public <T> SocketChannel setOption(SocketOption<T> name, T value)
        throws IOException {
        return null;
    }

    @Override
    public SocketChannel bind(SocketAddress local) throws IOException {
        return null;
    }

    @Override
    public Set<SocketOption<?>> supportedOptions() {
        return null;
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return null;
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        return null;
    }

}
