/*
 * Copyright 2016 The Android Open Source Project
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  The Android Open Source
 * Project designates this particular file as subject to the "Classpath"
 * exception as provided by The Android Open Source Project in the LICENSE
 * file that accompanied this code.
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
 */

package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;

/**
 * This class is only used for {@link SocketAdaptor} to be backward-compatible
 * with older Android versions which always set the {@code impl} field of
 * {@link Socket}. As such none of the methods in this class are implemented
 * since {@link SocketAdaptor} should override everything in {@link Socket}
 * which may access the {@code impl} field.
 */
class FileDescriptorHolderSocketImpl extends SocketImpl {
    public FileDescriptorHolderSocketImpl(FileDescriptor fd) {
        this.fd = fd;
    }

    @Override
    public void setOption(int optID, Object value) throws SocketException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getOption(int optID) throws SocketException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void create(boolean stream) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void connect(String host, int port) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void connect(InetAddress address, int port) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void connect(SocketAddress address, int timeout) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void bind(InetAddress host, int port) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void listen(int backlog) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void accept(SocketImpl s) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected int available() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void close() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void sendUrgentData(int data) throws IOException {
        throw new UnsupportedOperationException();
    }
}
