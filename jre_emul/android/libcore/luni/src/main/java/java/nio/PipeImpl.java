/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.nio;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.Pipe;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import libcore.io.ErrnoException;
import libcore.io.IoUtils;
import libcore.io.Libcore;
import static libcore.io.OsConstants.*;

/*
 * Implements {@link java.nio.channels.Pipe}.
 */
final class PipeImpl extends Pipe {
    private final PipeSinkChannel sink;
    private final PipeSourceChannel source;

    public PipeImpl(SelectorProvider selectorProvider) throws IOException {
        try {
            FileDescriptor fd1 = new FileDescriptor();
            FileDescriptor fd2 = new FileDescriptor();
            Libcore.os.socketpair(AF_UNIX, SOCK_STREAM, 0, fd1, fd2);

            // It doesn't matter which file descriptor we use for which end;
            // they're guaranteed to be indistinguishable.
            this.sink = new PipeSinkChannel(selectorProvider, fd1);
            this.source = new PipeSourceChannel(selectorProvider, fd2);
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsIOException();
        }
    }

    @Override public SinkChannel sink() {
        return sink;
    }

    @Override public SourceChannel source() {
        return source;
    }

    private class PipeSourceChannel extends Pipe.SourceChannel implements FileDescriptorChannel {
        private final FileDescriptor fd;
        private final SocketChannel channel;

        private PipeSourceChannel(SelectorProvider selectorProvider, FileDescriptor fd) throws IOException {
            super(selectorProvider);
            this.fd = fd;
            this.channel = new SocketChannelImpl(selectorProvider, fd);
        }

        @Override protected void implCloseSelectableChannel() throws IOException {
            channel.close();
        }

        @Override protected void implConfigureBlocking(boolean blocking) throws IOException {
            IoUtils.setBlocking(getFD(), blocking);
        }

        public int read(ByteBuffer buffer) throws IOException {
            return channel.read(buffer);
        }

        public long read(ByteBuffer[] buffers) throws IOException {
            return channel.read(buffers);
        }

        public long read(ByteBuffer[] buffers, int offset, int length) throws IOException {
            return channel.read(buffers, offset, length);
        }

        public FileDescriptor getFD() {
            return fd;
        }
    }

    private class PipeSinkChannel extends Pipe.SinkChannel implements FileDescriptorChannel {
        private final FileDescriptor fd;
        private final SocketChannel channel;

        private PipeSinkChannel(SelectorProvider selectorProvider, FileDescriptor fd) throws IOException {
            super(selectorProvider);
            this.fd = fd;
            this.channel = new SocketChannelImpl(selectorProvider, fd);
        }

        @Override protected void implCloseSelectableChannel() throws IOException {
            channel.close();
        }

        @Override protected void implConfigureBlocking(boolean blocking) throws IOException {
            IoUtils.setBlocking(getFD(), blocking);
        }

        public int write(ByteBuffer buffer) throws IOException {
            return channel.write(buffer);
        }

        public long write(ByteBuffer[] buffers) throws IOException {
            return channel.write(buffers);
        }

        public long write(ByteBuffer[] buffers, int offset, int length) throws IOException {
            return channel.write(buffers, offset, length);
        }

        public FileDescriptor getFD() {
            return fd;
        }
    }
}
