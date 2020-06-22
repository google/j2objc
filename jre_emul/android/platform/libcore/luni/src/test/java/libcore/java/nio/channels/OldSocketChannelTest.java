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

package libcore.java.nio.channels;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.NoConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;
import junit.framework.TestCase;

public class OldSocketChannelTest extends TestCase {

    private static final int CAPACITY_NORMAL = 200;

    private static final int CAPACITY_HUGE = 512 * 1024;

    private InetSocketAddress localAddr1;

    private SocketChannel channel1;

    private SocketChannel channel2;

    private ServerSocket server1;

    private final static int TIMEOUT = 60000;

    protected void setUp() throws Exception {
        super.setUp();
        this.channel1 = SocketChannel.open();
        this.channel2 = SocketChannel.open();
        this.server1 = new ServerSocket(0);
        this.localAddr1 = (InetSocketAddress) server1.getLocalSocketAddress();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        if (null != this.channel1) {
            try {
                this.channel1.close();
            } catch (Exception e) {
                // ignore
            }
        }
        if (null != this.channel2) {
            try {
                this.channel2.close();
            } catch (Exception e) {
                // ignore
            }
        }
        if (null != this.server1) {
            try {
                this.server1.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    // -------------------------------------------------------------------
    // Test for methods in abstract class.
    // -------------------------------------------------------------------
    public void testConstructor() throws IOException {
        SocketChannel channel =
                SelectorProvider.provider().openSocketChannel();
        assertNotNull(channel);
        assertSame(SelectorProvider.provider(), channel.provider());
        channel = SocketChannel.open();
        assertNotNull(channel);
        assertSame(SelectorProvider.provider(), channel.provider());
        MockSocketChannel chan = new MockSocketChannel(
                SelectorProvider.provider());
        assertTrue(chan.isConstructorCalled);
    }

    public void testValidOps() {
        MockSocketChannel testMSChannel = new MockSocketChannel(null);
        assertEquals(13, this.channel1.validOps());
        assertEquals(13, testMSChannel.validOps());
    }

    public void testOpen() throws IOException {
        java.nio.ByteBuffer[] buf = new java.nio.ByteBuffer[1];
        buf[0] = java.nio.ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        MockSocketChannel testMSChannel = new MockSocketChannel(null);
        MockSocketChannel testMSChannelnotnull = new MockSocketChannel(
                SelectorProvider.provider());
        SocketChannel testSChannel = MockSocketChannel.open();
        assertTrue(testSChannel.isOpen());
        assertNull(testMSChannel.provider());
        assertNotNull(testSChannel.provider());
        assertEquals(SelectorProvider.provider(), testSChannel.provider());
        assertNotNull(testMSChannelnotnull.provider());
        assertEquals(this.channel1.provider(), testMSChannelnotnull.provider());
        try {
            this.channel1.write(buf);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
            // correct
        }
    }

    public void testIsOpen() throws Exception {
        assertTrue(this.channel1.isOpen());
        this.channel1.close();
        assertFalse(this.channel1.isOpen());
    }

    public void testIsConnected() throws Exception {
        assertFalse(this.channel1.isConnected());// not connected
        this.channel1.configureBlocking(false);
        assertFalse(this.channel1.connect(localAddr1));
        assertFalse(this.channel1.isConnected());
        assertTrue(this.channel1.isConnectionPending());
        assertTrue(tryFinish());
        assertTrue(this.channel1.isConnected());
        this.channel1.close();
        assertFalse(this.channel1.isConnected());
    }

    public void testIsConnectionPending() throws Exception {
        // ensure
        ensureServerClosed();
        this.channel1.configureBlocking(false);
        assertFalse(this.channel1.isConnectionPending());
        // finish
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
            // OK.
        }
        assertFalse(this.channel1.isConnectionPending());
        // connect
        assertFalse(this.channel1.connect(localAddr1));
        assertTrue(this.channel1.isConnectionPending());
        this.channel1.close();

        assertFalse(this.channel1.isConnectionPending());
    }

    public void testChannelBasicStatus() {
        Socket gotSocket = this.channel1.socket();
        assertFalse(gotSocket.isClosed());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isRegistered());
        assertEquals((SelectionKey.OP_CONNECT | SelectionKey.OP_READ |
                SelectionKey.OP_WRITE), this.channel1.validOps());
        assertEquals(SelectorProvider.provider(), this.channel1.provider());
    }

    public void testOpenSocketAddress() throws IOException {
        this.channel1 = SocketChannel.open(localAddr1);
        assertTrue(this.channel1.isConnected());

        SocketAddress newTypeAddress = new SubSocketAddress();
        try {
            this.channel1 = SocketChannel.open(newTypeAddress);
            fail("Should throw UnexpectedAddressTypeException");
        } catch (UnsupportedAddressTypeException e) {
            // expected
        }

        SocketAddress unresolvedAddress =
                InetSocketAddress.createUnresolved("127.0.0.1", 8080);
        try {
            this.channel1 = SocketChannel.open(unresolvedAddress);
            fail("Should throw UnresolvedAddressException");
        } catch (UnresolvedAddressException e) {
            // expected
        }

        try {
            SocketChannel.open(null);
            fail("Should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // correct
        }
    }

    private void ensureServerClosed() throws IOException {
        if (null != this.server1) {
            this.server1.close();
            assertTrue(this.server1.isClosed());
        }
    }

    private void statusConnected_NotPending() {
        assertTrue(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
    }

    private boolean tryFinish() throws IOException {
        /*
         * the result of finish will be asserted in multi-thread tests.
         */
        boolean connected = false;
        assertTrue(this.channel1.isOpen());
        try {
            connected = this.channel1.finishConnect();
        } catch (SocketException e) {
            // Finish connection failed, probably due to reset by peer error.
        }
        if (connected) {
            statusConnected_NotPending();
        }
        return connected;
    }

    // Broken Test: Occasionally fail in CTS, but works in CoreTestRunner
    public void test_writeLjava_nio_ByteBuffer_Nonblocking_HugeData() throws IOException {
        // initialize write content
        ByteBuffer writeContent = ByteBuffer.allocate(CAPACITY_HUGE);
        for (int i = 0; i < CAPACITY_HUGE; i++) {
            writeContent.put((byte) i);
        }
        writeContent.flip();

        // establish connection
        channel1.connect(localAddr1);
        Socket acceptedSocket = server1.accept();

        channel1.configureBlocking(false);
        int writtenTotalCount = 0;
        int writtenCount = 1;
        long startTime = System.currentTimeMillis();
        // use SocketChannel.write(ByteBuffer) to try to write CAPACITY_HUGE bytes
        while (writtenTotalCount < CAPACITY_HUGE && writtenCount > 0) {
            writtenCount = channel1.write(writeContent);
            if (writtenCount == 0 && writtenTotalCount < CAPACITY_HUGE) {
                assertEquals(0, channel1.write(writeContent));
                break;
            }
            writtenTotalCount += writtenCount;
            // if the channel could not finish writing in TIMEOUT ms, the
            // test fails. It is used to guarantee the test never hangs even
            // if there are bugs of SocketChannel implementation.
            assertTimeout(startTime, TIMEOUT);
        }
    }

    /*
     * Fails if the difference between current time and start time is greater
     * than timeout.
     */
    private void assertTimeout(long startTime, long timeout) {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - startTime) > timeout) {
            fail("Timeout");
        }
    }

    public void test_socketChannel_read_DirectByteBuffer() throws InterruptedException, IOException {
        final ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(null, 0);

        Thread server = new Thread() {
            @Override public void run() {
                try {
                    for (int i = 0; i < 2; ++i) {
                        ByteBuffer buf = ByteBuffer.allocate(10);
                        buf.put(data);
                        buf.rewind();
                        ssc.accept().write(buf);
                    }
                } catch (Exception ignored) {
                }
            }
        };
        server.start();

        // First test with array based byte buffer
        SocketChannel sc = SocketChannel.open();
        sc.connect(ssc.socket().getLocalSocketAddress());

        ByteBuffer buf = ByteBuffer.allocate(data.length);
        buf.limit(data.length / 2);
        sc.read(buf);

        buf.limit(buf.capacity());
        sc.read(buf);
        sc.close();

        // Make sure the buffer is filled correctly
        buf.rewind();
        assertSameContent(data, buf);

        // Now test with direct byte buffer
        sc = SocketChannel.open();
        sc.connect(ssc.socket().getLocalSocketAddress());

        buf = ByteBuffer.allocateDirect(data.length);
        buf.limit(data.length / 2);
        sc.read(buf);

        buf.limit(buf.capacity());
        sc.read(buf);
        sc.close();

        // Make sure the buffer is filled correctly
        buf.rewind();
        assertSameContent(data, buf);
    }

    private void assertSameContent(byte[] data, ByteBuffer buf) {
        for (byte b : data) {
            if (b != buf.get()) {
                int pos = buf.position() - 1;
                fail("Content not equal. Buffer position: " +
                        (pos) + " expected: " + b + " was: " + buf.get(pos));
            }
        }
    }

    public static boolean done = false;
    public static byte[] data = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    class MockSocketChannel extends SocketChannel {

        private boolean isConstructorCalled = false;

        public MockSocketChannel(SelectorProvider provider) {
            super(provider);
            isConstructorCalled = true;
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
        public boolean connect(SocketAddress address) throws IOException {
            return false;
        }

        @Override
        public boolean finishConnect() throws IOException {
            return false;
        }

        @Override
        public int read(ByteBuffer target) throws IOException {
            return 0;
        }

        @Override
        public long read(ByteBuffer[] targets, int offset, int length)
                throws IOException {
            return 0;
        }

        @Override
        public int write(ByteBuffer source) throws IOException {
            return 0;
        }

        @Override
        public long write(ByteBuffer[] sources, int offset, int length)
                throws IOException {
            return 0;
        }

        @Override
        protected void implCloseSelectableChannel() throws IOException {
            // empty
        }

        @Override
        protected void implConfigureBlocking(boolean blockingMode)
                throws IOException {
            // empty
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
        public <T> SocketChannel setOption(SocketOption<T> name, T value) {
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

    class SubSocketAddress extends SocketAddress {
        private static final long serialVersionUID = 1L;

        // empty
        public SubSocketAddress() {
            super();
        }
    }
}
