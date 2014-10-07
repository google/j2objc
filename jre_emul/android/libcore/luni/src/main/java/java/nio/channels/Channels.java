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

package java.nio.channels;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.spi.AbstractInterruptibleChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import libcore.io.Streams;

/**
 * This class provides several utilities to get I/O streams from channels.
 */
public final class Channels {

    private Channels() {}

    /**
     * Returns an input stream on the given channel. The resulting stream has
     * the following properties:
     * <ul>
     * <li>If the stream is closed, then the underlying channel is closed as
     * well.</li>
     * <li>It is thread safe.</li>
     * <li>It throws an {@link IllegalBlockingModeException} if the channel is
     * in non-blocking mode and {@code read} is called.</li>
     * <li>Neither {@code mark} nor {@code reset} is supported.</li>
     * <li>It is not buffered.</li>
     * </ul>
     *
     * @param channel
     *            the channel to be wrapped by an InputStream.
     * @return an InputStream that takes bytes from the given byte channel.
     */
    public static InputStream newInputStream(ReadableByteChannel channel) {
        return new ChannelInputStream(channel);
    }

    /**
     * Returns an output stream on the given channel. The resulting stream has
     * the following properties:
     * <ul>
     * <li>If the stream is closed, then the underlying channel is closed as
     * well.</li>
     * <li>It is thread safe.</li>
     * <li>It throws an {@link IllegalBlockingModeException} if the channel is
     * in non-blocking mode and {@code write} is called.</li>
     * <li>It is not buffered.</li>
     * </ul>
     *
     * @param channel
     *            the channel to be wrapped by an OutputStream.
     * @return an OutputStream that puts bytes onto the given byte channel.
     */
    public static OutputStream newOutputStream(WritableByteChannel channel) {
        return new ChannelOutputStream(channel);
    }

    /**
     * Returns a readable channel on the given input stream. The resulting
     * channel has the following properties:
     * <ul>
     * <li>If the channel is closed, then the underlying stream is closed as
     * well.</li>
     * <li>It is not buffered.</li>
     * </ul>
     *
     * @param inputStream
     *            the stream to be wrapped by a byte channel.
     * @return a byte channel that reads bytes from the input stream.
     */
    public static ReadableByteChannel newChannel(InputStream inputStream) {
        return new InputStreamChannel(inputStream);
    }

    /**
     * Returns a writable channel on the given output stream.
     *
     * The resulting channel has following properties:
     * <ul>
     * <li>If the channel is closed, then the underlying stream is closed as
     * well.</li>
     * <li>It is not buffered.</li>
     * </ul>
     *
     * @param outputStream
     *            the stream to be wrapped by a byte channel.
     * @return a byte channel that writes bytes to the output stream.
     */
    public static WritableByteChannel newChannel(OutputStream outputStream) {
        return new OutputStreamChannel(outputStream);
    }

    /**
     * Returns a reader that decodes bytes from a channel.
     *
     * @param channel
     *            the Channel to be read.
     * @param decoder
     *            the Charset decoder to be used.
     * @param minBufferCapacity
     *            The minimum size of the byte buffer, -1 means to use the
     *            default size.
     * @return the reader.
     */
    public static Reader newReader(ReadableByteChannel channel,
            CharsetDecoder decoder, int minBufferCapacity) {
        /*
         * This method doesn't honor minBufferCapacity. Ignoring that parameter
         * saves us from having to add a hidden constructor to InputStreamReader.
         */
        return new InputStreamReader(new ChannelInputStream(channel), decoder);
    }

    /**
     * Returns a reader that decodes bytes from a channel. This method creates a
     * reader with a buffer of default size.
     *
     * @param channel
     *            the Channel to be read.
     * @param charsetName
     *            the name of the charset.
     * @return the reader.
     * @throws java.nio.charset.UnsupportedCharsetException
     *             if the given charset name is not supported.
     */
    public static Reader newReader(ReadableByteChannel channel,
            String charsetName) {
        if (charsetName == null) {
            throw new NullPointerException("charsetName == null");
        }
        return newReader(channel, Charset.forName(charsetName).newDecoder(), -1);
    }

    /**
     * Returns a writer that encodes characters with the specified
     * {@code encoder} and sends the bytes to the specified channel.
     *
     * @param channel
     *            the Channel to write to.
     * @param encoder
     *            the CharsetEncoder to be used.
     * @param minBufferCapacity
     *            the minimum size of the byte buffer, -1 means to use the
     *            default size.
     * @return the writer.
     */
    public static Writer newWriter(WritableByteChannel channel,
            CharsetEncoder encoder, int minBufferCapacity) {
        /*
         * This method doesn't honor minBufferCapacity. Ignoring that parameter
         * saves us from having to add a hidden constructor to OutputStreamWriter.
         */
        return new OutputStreamWriter(new ChannelOutputStream(channel), encoder);
    }

    /**
     * Returns a writer that encodes characters with the specified
     * {@code encoder} and sends the bytes to the specified channel. This method
     * creates a writer with a buffer of default size.
     *
     * @param channel
     *            the Channel to be written to.
     * @param charsetName
     *            the name of the charset.
     * @return the writer.
     * @throws java.nio.charset.UnsupportedCharsetException
     *             if the given charset name is not supported.
     */
    public static Writer newWriter(WritableByteChannel channel,
            String charsetName) {
        if (charsetName == null) {
            throw new NullPointerException("charsetName == null");
        }
        return newWriter(channel, Charset.forName(charsetName).newEncoder(), -1);
    }

    /**
     * An input stream that delegates to a readable channel.
     */
    private static class ChannelInputStream extends InputStream {

        private final ReadableByteChannel channel;

        ChannelInputStream(ReadableByteChannel channel) {
            if (channel == null) {
                throw new NullPointerException("channel == null");
            }
            this.channel = channel;
        }

        @Override public synchronized int read() throws IOException {
            return Streams.readSingleByte(this);
        }

        @Override public synchronized int read(byte[] target, int byteOffset, int byteCount) throws IOException {
            ByteBuffer buffer = ByteBuffer.wrap(target, byteOffset, byteCount);
            checkBlocking(channel);
            return channel.read(buffer);
        }

        @Override public int available() throws IOException {
            if (channel instanceof FileChannel) {
                FileChannel fileChannel = (FileChannel) channel;
                long result = fileChannel.size() - fileChannel.position();
                return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;
            } else {
                return super.available();
            }
        }

        @Override
        public synchronized void close() throws IOException {
            channel.close();
        }
    }

    /**
     * An output stream that delegates to a writable channel.
     */
    private static class ChannelOutputStream extends OutputStream {

        private final WritableByteChannel channel;

        ChannelOutputStream(WritableByteChannel channel) {
            if (channel == null) {
                throw new NullPointerException("channel == null");
            }
            this.channel = channel;
        }

        @Override
        public synchronized void write(int oneByte) throws IOException {
            byte[] wrappedByte = { (byte) oneByte };
            write(wrappedByte);
        }

        @Override
        public synchronized void write(byte[] source, int offset, int length) throws IOException {
            ByteBuffer buffer = ByteBuffer.wrap(source, offset, length);
            checkBlocking(channel);
            int total = 0;
            while (total < length) {
                total += channel.write(buffer);
            }
        }

        @Override
        public synchronized void close() throws IOException {
            channel.close();
        }
    }

    static void checkBlocking(Channel channel) {
        if (channel instanceof SelectableChannel && !((SelectableChannel) channel).isBlocking()) {
            throw new IllegalBlockingModeException();
        }
    }

    /**
     * A readable channel that delegates to an input stream.
     */
    private static class InputStreamChannel extends AbstractInterruptibleChannel
            implements ReadableByteChannel {
        private final InputStream inputStream;

        InputStreamChannel(InputStream inputStream) {
            if (inputStream == null) {
                throw new NullPointerException("inputStream == null");
            }
            this.inputStream = inputStream;
        }

        public synchronized int read(ByteBuffer target) throws IOException {
            if (!isOpen()) {
                throw new ClosedChannelException();
            }
            int bytesRemain = target.remaining();
            byte[] bytes = new byte[bytesRemain];
            int readCount = 0;
            try {
                begin();
                readCount = inputStream.read(bytes);
            } finally {
                end(readCount >= 0);
            }
            if (readCount > 0) {
                target.put(bytes, 0, readCount);
            }
            return readCount;
        }

        @Override
        protected void implCloseChannel() throws IOException {
            inputStream.close();
        }
    }

    /**
     * A writable channel that delegates to an output stream.
     */
    private static class OutputStreamChannel extends AbstractInterruptibleChannel
            implements WritableByteChannel {
        private final OutputStream outputStream;

        OutputStreamChannel(OutputStream outputStream) {
            if (outputStream == null) {
                throw new NullPointerException("outputStream == null");
            }
            this.outputStream = outputStream;
        }

        public synchronized int write(ByteBuffer source) throws IOException {
            if (!isOpen()) {
                throw new ClosedChannelException();
            }
            int bytesRemain = source.remaining();
            if (bytesRemain == 0) {
                return 0;
            }
            byte[] buf = new byte[bytesRemain];
            source.get(buf);
            try {
                begin();
                outputStream.write(buf, 0, bytesRemain);
            } finally {
                end(bytesRemain >= 0);
            }
            return bytesRemain;
        }

        @Override
        protected void implCloseChannel() throws IOException {
            outputStream.close();
        }
    }
}
