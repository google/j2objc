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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.SocketException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ConnectionPendingException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.NoConnectionPendingException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;
import junit.framework.TestCase;

/**
 * Tests for SocketChannel and its default implementation.
 */
public class SocketChannelTest extends TestCase {

    private static final int CAPACITY_NORMAL = 200;

    private InetSocketAddress localAddr1;
    private InetSocketAddress localAddr2;

    private SocketChannel channel1;

    private SocketChannel channel2;

    private ServerSocket server1;

    private ServerSocket server2;

    private final static int TIMEOUT = 60000;

    private final static int EOF = -1;

    protected void setUp() throws Exception {
        super.setUp();
        this.channel1 = SocketChannel.open();
        this.channel2 = SocketChannel.open();
        this.server1 = new ServerSocket(0);
        this.localAddr1 = new InetSocketAddress("127.0.0.1", server1.getLocalPort());

        this.server2 = new ServerSocket(0);
        this.localAddr2 = new InetSocketAddress("127.0.0.1", server2.getLocalPort());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        if (null != this.channel1) {
            try {
                this.channel1.close();
            } catch (Exception e) {
                //ignore
            }
        }
        if (null != this.channel2) {
            try {
                this.channel2.close();
            } catch (Exception e) {
                //ignore
            }
        }
        if (null != this.server1) {
            try {
                this.server1.close();
            } catch (Exception e) {
                //ignore
            }
        }
        if (null != this.server2) {
            try {
                this.server2.close();
            } catch (Exception e) {
                //ignore
            }
        }
    }

    // -------------------------------------------------------------------
    // Test for methods in abstract class.
    // -------------------------------------------------------------------
    /*
     * Test method for 'java.nio.channels.SocketChannel.validOps()'
     */
    public void testValidOps() {
        MockSocketChannel testMSChannel = new MockSocketChannel(null);
        assertEquals(13, this.channel1.validOps());
        assertEquals(13, testMSChannel.validOps());
    }

    /*
     * Test method for 'java.nio.channels.SocketChannel.open()'
     */
    public void testOpen() throws IOException {
        java.nio.ByteBuffer[] buf = new java.nio.ByteBuffer[1];
        buf[0] = java.nio.ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        MockSocketChannel testMSChannel = new MockSocketChannel(null);
        MockSocketChannel testMSChannelnotnull = new MockSocketChannel(
                SelectorProvider.provider());
        assertNull(testMSChannel.provider());
        assertNotNull(testMSChannelnotnull.provider());
        assertNotNull(this.channel1);
        assertEquals(this.channel1.provider(), testMSChannelnotnull.provider());
        try {
            this.channel1.write(buf);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
            // correct
        }
    }

    /*
     * Test method for 'java.nio.channels.SocketChannel.open(SocketAddress)'
     */
    public void testOpenSocketAddress_Null() throws IOException {
        try {
            SocketChannel.open(null);
            fail("Should throw an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // correct
        }
    }

    public void testBind_Null() throws Exception {
        assertNull(channel1.socket().getLocalSocketAddress());

        channel1.socket().bind(null);

        InetSocketAddress localAddress = (InetSocketAddress) channel1.socket().getLocalSocketAddress();
        assertTrue(localAddress.getAddress().isAnyLocalAddress());
        assertTrue(localAddress.getPort() > 0);
    }

    public void testBind_Failure() throws Exception {
        assertNull(channel1.socket().getLocalSocketAddress());

        try {
            // Bind to a local address that is in use
            channel1.socket().bind(localAddr1);
            fail();
        } catch (IOException expected) {
        }
    }

    public void testBind_Closed() throws Exception {
        channel1.close();

        try {
            channel1.socket().bind(null);
            fail();
        } catch (IOException expected) {
        }
    }

    public void testBind_explicitPort() throws Exception {
        ServerSocketChannel portPickingChannel = ServerSocketChannel.open();
        // Have the OS find a free port.
        portPickingChannel.socket().bind(null);
        InetSocketAddress address = (InetSocketAddress) portPickingChannel.socket().getLocalSocketAddress();
        assertTrue(address.getPort() > 0);
        portPickingChannel.close();

        // There is a risk of flakiness here if the port is allocated to something else between
        // close() and bind().
        InetSocketAddress bindAddress = new InetSocketAddress("localhost", address.getPort());
        // Allow the socket to bind to a port we know is already in use.
        channel1.socket().setReuseAddress(true);
        channel1.socket().bind(bindAddress);

        InetSocketAddress boundAddress = (InetSocketAddress) channel1.socket().getLocalSocketAddress();
        assertEquals(bindAddress.getHostName(), boundAddress.getHostName());
        assertEquals(bindAddress.getPort(), boundAddress.getPort());
    }

    public void test_getLocalSocketAddress_afterClose() throws IOException {
        SocketChannel sc = SocketChannel.open();
        assertNull(sc.socket().getLocalSocketAddress());

        InetSocketAddress bindAddr = new InetSocketAddress("localhost", 0);
        sc.socket().bind(bindAddr);

        assertNotNull(sc.socket().getLocalSocketAddress());

        sc.close();

        assertFalse(sc.isOpen());

        sc.socket().getLocalSocketAddress();
    }

    /*
     * Test method for 'java.nio.channels.SocketChannel.read(ByteBuffer[])'
     */
    public void testReadByteBufferArray() throws IOException {
        java.nio.ByteBuffer[] byteBuf = null;
        MockSocketChannel testMSChannelnull = new MockSocketChannel(null);
        MockSocketChannel testMSChannel = new MockSocketChannel(
                SelectorProvider.provider());
        ServerSocket testServer = new ServerSocket(0);
        try {
            try {
                this.channel1.read(byteBuf);
                fail("Should throw NPE");
            } catch (NullPointerException e) {
                // correct
            }
            byteBuf = new java.nio.ByteBuffer[CAPACITY_NORMAL];
            try {
                this.channel1.read(byteBuf);
                fail("Should throw NotYetConnectedException");
            } catch (NotYetConnectedException e) {
                // correct
            }
            long readNum = testMSChannel.read(byteBuf);
            assertEquals(0, readNum);
            readNum = CAPACITY_NORMAL;
            readNum = testMSChannelnull.read(byteBuf);
            assertEquals(0, readNum);
        } finally {
            testServer.close();
        }
    }

    /*
     * Test method for 'java.nio.channels.SocketChannel.read(ByteBuffer[])'
     */
    public void testReadByteBufferArray_BufNull() throws IOException {
        java.nio.ByteBuffer[] byteBuf = null;
        MockSocketChannel testMSChannelnull = new MockSocketChannel(null);
        MockSocketChannel testMSChannel = new MockSocketChannel(
                SelectorProvider.provider());
        try {
            this.channel1.read(byteBuf);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // correct
        }
        try {
            testMSChannel.read(byteBuf);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // correct
        }
        try {
            testMSChannelnull.read(byteBuf);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // correct
        }
    }

    /*
     * Test method for 'java.nio.channels.SocketChannel.write(ByteBuffer[])'
     */
    public void testWriteByteBufferArray() throws IOException {
        java.nio.ByteBuffer[] byteBuf = null;
        MockSocketChannel testMSChannelnull = new MockSocketChannel(null);
        MockSocketChannel testMSChannel = new MockSocketChannel(
                SelectorProvider.provider());
        try {
            this.channel1.write(byteBuf);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // correct
        }
        byteBuf = new java.nio.ByteBuffer[CAPACITY_NORMAL];
        try {
            this.channel1.write(byteBuf);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
            // correct
        }
        testMSChannel.write(byteBuf);
        testMSChannelnull.write(byteBuf);
    }

    /*
     * Test method for 'java.nio.channels.SocketChannel.write(ByteBuffer[])'
     */
    public void testWriteByteBufferArray_BufNull() throws IOException {
        java.nio.ByteBuffer[] byteBuf = null;
        MockSocketChannel testMSChannelnull = new MockSocketChannel(null);
        MockSocketChannel testMSChannel = new MockSocketChannel(
                SelectorProvider.provider());
        try {
            this.channel1.write(byteBuf);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // correct
        }
        try {
            testMSChannel.write(byteBuf);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // correct
        }
        try {
            testMSChannelnull.write(byteBuf);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // correct
        }
    }

    public void testSocket_BasicStatusBeforeConnect() throws IOException {
        assertFalse(this.channel1.isConnected());// not connected
        Socket s1 = this.channel1.socket();
        assertSocketBeforeBind(s1);
        assertSocketBeforeConnect(s1);
        Socket s2 = this.channel1.socket();
        // same
        assertSame(s1, s2);
    }

    public void testSocket_Block_BasicStatusAfterConnect() throws IOException {
        assertFalse(this.channel1.isConnected());// not connected
        assertTrue(this.channel1.connect(localAddr1));

        assertTrue(this.channel1.isConnected());
        Socket s1 = this.channel1.socket();

        assertSocketAfterConnect(s1, localAddr1);
        Socket s2 = this.channel1.socket();
        // same
        assertSame(s1, s2);
    }

    public void testSocket_NonBlock_BasicStatusAfterConnect() throws Exception {
        assertFalse(this.channel1.isConnected());// not connected
        this.channel1.configureBlocking(false);
        boolean connected = channel1.connect(localAddr1);
        Socket s1;
        Socket s2;
        if (!connected) {
            assertFalse(this.channel1.isConnected());
            assertTrue(this.channel1.isConnectionPending());
            s1 = this.channel1.socket();
            // A connect() causes an implicit bind()
            assertSocketAfterImplicitBind(s1);

            // status of not connected
            assertSocketBeforeConnect(s1);
            s2 = this.channel1.socket();
            // same
            assertSame(s1, s2);
        }

        if (tryFinish()) {
            assertTrue(this.channel1.isConnected());
            s1 = this.channel1.socket();
            assertSocketAfterConnect(s1, localAddr1);
            s2 = this.channel1.socket();
            // same
            assertSame(s1, s2);
        }
    }

    public void testSocket_Block_ActionsBeforeConnect() throws IOException {
        assertFalse(this.channel1.isConnected());// not connected
        Socket s = this.channel1.socket();
        assertSocketAction_Block_BeforeConnect(s);
    }

    public void testSocket_Block_ActionsAfterConnect() throws IOException {
        assertFalse(this.channel1.isConnected());// not connected
        assertTrue(this.channel1.connect(localAddr1));
        assertTrue(this.channel1.isConnected());
        Socket s = this.channel1.socket();
        assertSocketAction_Block_AfterConnect(s);

    }

    public void testSocket_NonBlock_ActionsAfterConnectBeforeFinish()
            throws IOException {
        assertFalse(this.channel1.isConnected());// not connected
        this.channel1.configureBlocking(false);
        boolean connected = channel1.connect(localAddr1);
        if (!connected) {
            assertFalse(this.channel1.isConnected());
            assertTrue(this.channel1.isConnectionPending());
            Socket s1 = this.channel1.socket();
            // Action of not connected
            assertSocketAction_NonBlock_BeforeConnect(s1);
            Socket s2 = this.channel1.socket();
            // same
            assertSame(s1, s2);
        }
    }

    public void testSocket_NonBlock_ActionsAfterConnectAfterFinish()
            throws Exception {
        assertFalse(this.channel1.isConnected());// not connected
        this.channel1.configureBlocking(false);
        channel1.connect(localAddr1);
        if (tryFinish()) {
            Socket s1 = this.channel1.socket();
            assertSocketAction_NonBlock_AfterConnect(s1);
            Socket s2 = this.channel1.socket();
            // same
            assertSame(s1, s2);
        }
    }

    public void testSocket_getInetAddress() throws Exception {
        Socket socket = channel1.socket();
        assertNull(socket.getInetAddress());

        channel1.connect(localAddr1);

        assertNotNull(socket.getInetAddress());
        assertEquals(localAddr1.getAddress(), socket.getInetAddress());
    }

    public void testSocket_getRemoteSocketAddress() throws Exception {
        Socket socket = channel1.socket();
        assertNull(socket.getRemoteSocketAddress());

        channel1.connect(localAddr1);

        assertNotNull(socket.getRemoteSocketAddress());
        assertEquals(localAddr1, socket.getRemoteSocketAddress());
    }

    public void testSocket_getPort() throws Exception {
        Socket socket = channel1.socket();
        assertEquals(0, socket.getPort());

        channel1.connect(localAddr1);

        assertEquals(localAddr1.getPort(), socket.getPort());
    }

    public void testSocket_getLocalAddress() throws Exception {
        Socket socket = channel1.socket();

        channel1.connect(localAddr1);

        assertNotNull(socket.getLocalSocketAddress());
    }

    public void testSocket_getLocalSocketAddress() throws Exception {
        Socket socket = channel1.socket();
        assertNull(socket.getLocalSocketAddress());

        channel1.connect(localAddr1);

        assertNotNull(socket.getLocalSocketAddress());
    }

    public void testSocket_getLocalPort() throws Exception {
        Socket socket = channel1.socket();
        assertEquals(-1, socket.getLocalPort());

        channel1.connect(localAddr1);

        assertTrue(-1 != socket.getLocalPort());
        assertTrue(0 != socket.getLocalPort());
    }

    public void testSocket_bind() throws Exception {
        Socket socket = channel1.socket();
        socket.bind(new InetSocketAddress("127.0.0.1", 0));
        assertEquals("127.0.0.1", socket.getLocalAddress().getHostAddress());
        assertTrue(socket.getLocalPort() != -1);
    }

    private void assertSocketBeforeBind(Socket s) {
        assertFalse(s.isBound());
        assertTrue(s.getLocalAddress().isAnyLocalAddress());
        // RI fails here. RI returns 0 while spec says unbound socket should
        // return -1.
        assertEquals(-1, s.getLocalPort());
        assertNull(s.getLocalSocketAddress());
    }

    private void assertSocketAfterImplicitBind(Socket s) throws IOException {
        assertTrue(s.isBound());
        assertTrue(s.getLocalAddress().isLoopbackAddress());
        assertTrue(s.getLocalPort() > 0);

        InetSocketAddress localSocketAddress = (InetSocketAddress) s.getLocalSocketAddress();
        assertTrue(localSocketAddress.getAddress().isLoopbackAddress());
        assertEquals(s.getLocalPort(), localSocketAddress.getPort());
    }

    private void assertSocketBeforeConnect(Socket s) throws IOException {
        assertFalse(s.isClosed());
        assertFalse(s.isConnected());
        assertFalse(s.getKeepAlive());
        try {
            s.getInputStream();
            fail("Should throw SocketException.");
        } catch (SocketException e) {
            // OK.
        }
        assertFalse(s.getOOBInline());
        try {
            s.getOutputStream();
            fail("Should throw SocketException.");
        } catch (SocketException e) {
            // OK.
        }
        assertEquals(-1, s.getSoLinger());
        assertFalse(s.getTcpNoDelay());

        assertFalse(s.isInputShutdown());
        assertFalse(s.isOutputShutdown());

        assertNull(s.getInetAddress());
        assertFalse(s.getReuseAddress());

        // not connected
        assertEquals(0, s.getPort());
        assertTrue(s.getReceiveBufferSize() >= 8192);
        assertNull(s.getRemoteSocketAddress());
        assertTrue(s.getSendBufferSize() >= 8192);
        assertEquals(0, s.getSoTimeout());
        assertEquals(0, s.getTrafficClass());

    }

    private void assertSocketAfterConnect(Socket s, InetSocketAddress address)
            throws IOException {
        assertTrue(s.isBound());
        assertFalse(s.isClosed());
        assertTrue(s.isConnected());
        assertFalse(s.getKeepAlive());

        assertNotNull(s.getInputStream());
        assertNotNull(s.getOutputStream());

        assertFalse(s.getOOBInline());
        assertEquals(-1, s.getSoLinger());
        assertFalse(s.getTcpNoDelay());

        assertFalse(s.isInputShutdown());
        assertFalse(s.isOutputShutdown());

        assertSame(s.getInetAddress(), address.getAddress());

        assertEquals(s.getLocalAddress(), this.localAddr1.getAddress());
        assertEquals(s.getPort(), address.getPort());
        assertNotNull(s.getLocalSocketAddress());
        assertTrue(s.getReceiveBufferSize() >= 8192);
        assertEquals(s.getRemoteSocketAddress(), (SocketAddress) address);
        // assertFalse(s.getReuseAddress());
        assertTrue(s.getSendBufferSize() >= 8192);
        assertEquals(0, s.getSoTimeout());
        assertEquals(0, s.getTrafficClass());
    }

    private void assertSocketAction_Block_BeforeConnect(Socket s)
            throws IOException {
        assertFalse(this.channel1.isConnected());
        s.connect(localAddr2);
        assertTrue(this.channel1.isConnected());
        assertTrue(s.isConnected());

        assertSocketAfterConnect(s, localAddr2);

        try {
            s.bind(localAddr2);
            fail("Should throw SocketException");
        } catch (SocketException e) {
            // OK.
        }

        s.close();
        assertTrue(s.isClosed());
        assertFalse(this.channel1.isOpen());
    }

    private void assertSocketAction_NonBlock_BeforeConnect(Socket s)
            throws IOException {
        assertFalse(this.channel1.isConnected());
        try {
            s.connect(localAddr2);
            fail("Should throw IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e1) {
            // OK.
        }

        if (this.channel1.isConnectionPending()) {
            try {
                s.bind(localAddr2);
                fail("Should throw ConnectionPendingException");
            } catch (ConnectionPendingException e1) {
                // OK.
            }
        } else {
            try {
                s.bind(localAddr2);
                fail("Should throw BindException");
            } catch (BindException e1) {
                // OK.
            }
        }

        assertFalse(this.channel1.isConnected());
        assertFalse(s.isConnected());

        s.close();
        assertTrue(s.isClosed());
        assertFalse(this.channel1.isOpen());
    }

    private void assertSocketAction_Block_AfterConnect(Socket s)
            throws IOException {
        assertEquals(s.getPort(), localAddr1.getPort());
        assertTrue(this.channel1.isConnected());
        assertTrue(s.isConnected());
        try {
            s.connect(localAddr2);
            fail("Should throw SocketException");
        } catch (SocketException e) {
            // OK.
        }

        try {
            s.bind(localAddr2);
            fail("Should throw SocketException");
        } catch (SocketException e) {
            // OK.
        }

        s.close();
        assertTrue(s.isClosed());
        assertFalse(this.channel1.isOpen());
    }

    private void assertSocketAction_NonBlock_AfterConnect(Socket s)
            throws IOException {
        assertEquals(s.getPort(), localAddr1.getPort());
        assertTrue(this.channel1.isConnected());
        assertTrue(s.isConnected());

        if (this.channel1.isConnectionPending()) {
            try {
                s.connect(localAddr2);
                fail("Should throw SocketException");
            } catch (SocketException e) {
                // OK.
            }
        } else {
            try {
                s.connect(localAddr2);
                fail("Should throw IllegalBlockingModeException");
            } catch (IllegalBlockingModeException e) {
                // OK.
            }
        }

        try {
            s.bind(localAddr2);
            fail("Should throw SocketException");
        } catch (SocketException e) {
            // OK.
        }

        s.close();
        assertTrue(s.isClosed());
        assertFalse(this.channel1.isOpen());
    }

    // -------------------------------------------------------------------
    // Tests for connect(), finishConnect(),isConnected(),isConnectionPending()
    // These methods are very close, so we test them together, call them "CFII".
    // -------------------------------------------------------------------
    /**
     * connect-->finish-->close
     */
    public void testCFII_Norml_NoServer_Block() throws Exception {
        // ensure
        ensureServerClosed();
        assertTrue(this.channel1.isBlocking());
        statusNotConnected_NotPending();
        // connect
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ConnectException here.");
        } catch (ConnectException e) {
            // OK.
        }
        statusChannelClosed();
        try {
            this.channel1.finishConnect();
            fail("Should throw a ClosedChannelException here.");
        } catch (ClosedChannelException e) {
            // OK.
        }
    }

    /**
     * connect-->finish-->close
     */
    public void testCFII_Norml_NoServer_NonBlock() throws Exception {
        connectNoServerNonBlock();

        this.channel1.close();
        statusChannelClosed();
    }

    /**
     * connect-->finish-->close
     */
    public void testCFII_Norml_Server_Block() throws Exception {
        connectServerBlock();

        this.channel1.close();
        statusChannelClosed();

    }

    /**
     * connect-->finish-->close
     */
    public void testCFII_Norml_Server_NonBlock() throws Exception {
        connectServerNonBlock();

        this.channel1.close();
        statusChannelClosed();
    }

    /**
     * connect-->server closed-->finish-->close
     */
    public void testCFII_ServerClosed_Block() throws Exception {
        // ensure
        ensureServerOpen();
        assertTrue(this.channel1.isBlocking());
        statusNotConnected_NotPending();
        // connect
        assertTrue(this.channel1.connect(localAddr1));
        statusConnected_NotPending();

        ensureServerClosed();

        tryFinish();

        this.channel1.close();
        statusChannelClosed();

    }

    /**
     * connect-->server closed-->finish-->close
     */
    public void testCFII_ServerClosed_NonBlock() throws Exception {
        // ensure
        ensureServerOpen();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        // connect
        boolean connected = channel1.connect(localAddr1);
        if (!connected) {
            statusNotConnected_Pending();
        }
        ensureServerClosed();

        tryFinish();

        this.channel1.close();
        statusChannelClosed();
    }

    /**
     * connect-->finish-->server closed-->close
     */
    public void testCFII_ServerClosedAfterFinish_Block() throws Exception {
        connectServerBlock();

        ensureServerClosed();
        assertTrue(this.channel1.isOpen());
        this.channel1.close();
        statusChannelClosed();

    }

    /**
     * connect-->finish-->server closed-->close
     */
    public void testCFII_ServerClosedAfterFinish_NonBlock() throws Exception {
        connectServerNonBlock();

        ensureServerClosed();
        assertTrue(this.channel1.isOpen());
        this.channel1.close();
        statusChannelClosed();
    }

    /**
     * no server-->connect-->server open-->finish-->close
     */
    public void testCFII_ServerStartLater_Block() throws Exception {
        // ensure
        ensureServerClosed();
        assertTrue(this.channel1.isBlocking());
        statusNotConnected_NotPending();
        // connect
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ConnectException here.");
        } catch (ConnectException e) {
            // OK.
        }
        statusChannelClosed();
        ensureServerOpen();
        try {
            this.channel1.finishConnect();
            fail("Should throw a ClosedChannelException here.");
        } catch (ClosedChannelException e) {
            // OK.
        }
    }

    /**
     * no server-->connect-->server open-->finish-->close
     */
    public void testCFII_ServerStartLater_NonBlock() throws Exception {
        // ensure
        ensureServerClosed();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        // connect
        assertFalse(this.channel1.connect(localAddr1));
        statusNotConnected_Pending();

        ensureServerOpen();

        try {
            assertFalse(this.channel1.finishConnect());
            statusNotConnected_Pending();
            this.channel1.close();
        } catch (ConnectException e) {
            // FIXME: assertEquals(e.getMessage(), "Connection refused");
        }
    }

    /**
     * connect-->finish-->finish-->close
     */
    public void testCFII_FinishTwice_NoServer_NonBlock() throws Exception {
        // ensure
        ensureServerClosed();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        // connect
        assertFalse(this.channel1.connect(localAddr1));
        statusNotConnected_Pending();
        try {
            assertFalse(this.channel1.finishConnect());
            statusNotConnected_Pending();
            assertFalse(this.channel1.finishConnect());
            statusNotConnected_Pending();
            this.channel1.close();
        } catch (ConnectException e) {
          // FIXME: assertEquals(e.getMessage(), "Connection refused");
        }
        statusChannelClosed();
    }

    /**
     * connect-->finish-->finish-->close
     */
    public void testCFII_FinishTwice_Server_Block() throws Exception {
        connectServerBlock();
        tryFinish();
        this.channel1.close();
        statusChannelClosed();

    }

    /**
     * connect-->finish-->finish-->close
     */
    public void testCFII_FinishTwice_Server_NonBlock() throws Exception {
        connectServerNonBlock();
        tryFinish();
        this.channel1.close();
        statusChannelClosed();
    }

    /**
     * connect-->finish-->connect-->close
     */
    public void testCFII_ConnectAfterFinish_NoServer_Block() throws Exception {
        // ensure
        ensureServerClosed();
        assertTrue(this.channel1.isBlocking());
        statusNotConnected_NotPending();
        // connect
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ConnectException here.");
        } catch (ConnectException e) {
            // OK.
        }
        statusChannelClosed();
        try {
            this.channel1.finishConnect();
            fail("Should throw a ClosedChannelException here.");
        } catch (ClosedChannelException e) {
            // OK.
        }
        statusChannelClosed();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ClosedChannelException here.");
        } catch (ClosedChannelException e) {
            // OK.
        }
        statusChannelClosed();
    }

    /**
     * connect-->finish-->connect-->close
     */
    public void testCFII_ConnectAfterFinish_NoServer_NonBlock()
            throws Exception {
        // ensure
        ensureServerClosed();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        // connect
        assertFalse(this.channel1.connect(localAddr1));
        statusNotConnected_Pending();
        try {
            assertFalse(this.channel1.finishConnect());
            statusNotConnected_Pending();
        } catch (ConnectException e) {
            // FIXME: assertEquals(e.getMessage(), "Connection refused");
        }

        if (this.channel1.isOpen()) {

            try {
                this.channel1.connect(localAddr1);
                fail("Should throw a ConnectionPendingException here.");
            } catch (ConnectionPendingException e) {
                // OK.
            }
            statusNotConnected_Pending();

            // connect another addr
            try {
                this.channel1.connect(localAddr2);
                fail("Should throw a ConnectionPendingException here.");
            } catch (ConnectionPendingException e) {
                // OK.
            }
            statusNotConnected_Pending();

            // connect if server closed
            ensureServerClosed();

            try {
                this.channel1.connect(localAddr1);
                fail("Should throw a ConnectionPendingException here.");
            } catch (ConnectionPendingException e) {
                // OK.
            }
            statusNotConnected_Pending();

            this.channel1.close();
        }
        statusChannelClosed();
    }

    /**
     * connect-->finish-->connect-->close
     */
    public void testCFII_ConnectAfterFinish_Server_Block() throws Exception {
        connectServerBlock();

        if (!this.channel1.isConnected()) {
            System.err
                    .println("Connection fail, testCFII_ConnectAfterFinish_Server_Block is not finished.");
            return;
        }

        try {
            this.channel1.connect(localAddr1);
            fail("Should throw an AlreadyConnectedException here.");
        } catch (AlreadyConnectedException e) {
            // OK.
        }
        statusConnected_NotPending();

        // connect another addr
        try {
            this.channel1.connect(localAddr2);
            fail("Should throw an AlreadyConnectedException here.");
        } catch (AlreadyConnectedException e) {
            // OK.
        }
        statusConnected_NotPending();

        // connect if server closed
        ensureServerClosed();

        try {
            this.channel1.connect(localAddr1);
            fail("Should throw an AlreadyConnectedException here.");
        } catch (AlreadyConnectedException e) {
            // OK.
        }
        statusConnected_NotPending();

        this.channel1.close();
        statusChannelClosed();

    }

    /* TODO(user): fix and enable.
     * connect-->finish-->connect-->close
    public void testCFII_ConnectAfterFinish_Server_NonBlock() throws Exception {
        connectServerNonBlock();

        if (!this.channel1.isConnected()) {
            System.err
                    .println("Connection fail, testCFII_ConnectAfterFinish_Server_Block is not finished.");
            return;
        }
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw an AlreadyConnectedException or a ConnectionPendingException here.");
        } catch (AlreadyConnectedException e) {
            // OK.
        }

        statusConnected_NotPending();

        // connect another addr
        try {
            this.channel1.connect(localAddr2);
            fail("Should throw an AlreadyConnectedException here.");
        } catch (AlreadyConnectedException e) {
            // OK.
        }
        statusConnected_NotPending();

        // connect if server closed
        ensureServerClosed();

        try {
            this.channel1.connect(localAddr1);
            fail("Should throw an AlreadyConnectedException here.");
        } catch (AlreadyConnectedException e) {
            // OK.
        }
        statusConnected_NotPending();

        this.channel1.close();
        statusChannelClosed();
    }
    */

    /**
     * connect-->connect-->finish-->close
     */
    public void testCFII_ConnectTwice_NoServer_NonBlock() throws Exception {
        // ensure
        ensureServerClosed();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        // connect
        assertFalse(this.channel1.connect(localAddr1));
        statusNotConnected_Pending();

        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ConnectionPendingException here.");
        } catch (ConnectionPendingException e) {
            // OK.
        }
        statusNotConnected_Pending();

        // connect another addr
        try {
            this.channel1.connect(localAddr2);
            fail("Should throw a ConnectionPendingException here.");
        } catch (ConnectionPendingException e) {
            // OK.
        }
        statusNotConnected_Pending();

        // connect if server closed
        ensureServerClosed();

        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ConnectionPendingException here.");
        } catch (ConnectionPendingException e) {
            // OK.
        }
        statusNotConnected_Pending();

        try {
            assertFalse(this.channel1.finishConnect());
            statusNotConnected_Pending();
            this.channel1.close();
        } catch (ConnectException e) {
            // FIXME: assertEquals(e.getMessage(), "Connection refused");
        }

        statusChannelClosed();
    }

    /**
     * connect-->connect-->finish-->close
     */
    public void testCFII_ConnectTwice_Server_Block() throws Exception {
        // ensure
        ensureServerOpen();
        assertTrue(this.channel1.isBlocking());
        statusNotConnected_NotPending();
        // connect
        assertTrue(this.channel1.connect(localAddr1));
        statusConnected_NotPending();

        try {
            this.channel1.connect(localAddr1);
            fail("Should throw an AlreadyConnectedException here.");
        } catch (AlreadyConnectedException e) {
            // OK.
        }
        statusConnected_NotPending();

        // connect another addr
        try {
            this.channel1.connect(localAddr2);
            fail("Should throw an AlreadyConnectedException here.");
        } catch (AlreadyConnectedException e) {
            // OK.
        }
        statusConnected_NotPending();

        // connect if server closed
        ensureServerClosed();

        try {
            this.channel1.connect(localAddr1);
            fail("Should throw an AlreadyConnectedException here.");
        } catch (AlreadyConnectedException e) {
            // OK.
        }
        statusConnected_NotPending();

        tryFinish();

        this.channel1.close();
        statusChannelClosed();

    }

    /**
     * connect-->connect-->finish-->close
     */
    public void testCFII_ConnectTwice_Server_NonBlock() throws Exception {
        // ensure
        ensureServerOpen();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        // connect
        boolean connected = channel1.connect(localAddr1);
        if (!connected) {
            statusNotConnected_Pending();

            try {
                this.channel1.connect(localAddr1);
                fail("Should throw a ConnectionPendingException here.");
            } catch (ConnectionPendingException e) {
                // OK.
            }
            statusNotConnected_Pending();

            // connect another addr
            try {
                this.channel1.connect(localAddr2);
                fail("Should throw a ConnectionPendingException here.");
            } catch (ConnectionPendingException e) {
                // OK.
            }
            statusNotConnected_Pending();

            // connect if server closed
            ensureServerClosed();

            try {
                this.channel1.connect(localAddr1);
                fail("Should throw a ConnectionPendingException here.");
            } catch (ConnectionPendingException e) {
                // OK.
            }
            statusNotConnected_Pending();
        }
        tryFinish();

        this.channel1.close();
        statusChannelClosed();
    }

    /**
     * finish-->connect-->finish-->close
     */
    public void testCFII_FinishFirst_NoServer_Block() throws Exception {
        // ensure
        ensureServerClosed();
        assertTrue(this.channel1.isBlocking());
        statusNotConnected_NotPending();
        // finish
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
            // OK.
        }
        statusNotConnected_NotPending();
        // connect
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw a ConnectException here.");
        } catch (ConnectException e) {
            // OK.
        }
        statusChannelClosed();
        try {
            this.channel1.finishConnect();
            fail("Should throw a ClosedChannelException here.");
        } catch (ClosedChannelException e) {
            // OK.
        }
        statusChannelClosed();
    }

    /**
     * finish-->connect-->finish-->close
     */
    public void testCFII_FinishFirst_NoServer_NonBlock() throws Exception {
        // ensure
        ensureServerClosed();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        // finish
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
            // OK.
        }
        statusNotConnected_NotPending();
        // connect
        assertFalse(this.channel1.connect(localAddr1));
        statusNotConnected_Pending();

        try {
            assertFalse(this.channel1.finishConnect());
            statusNotConnected_Pending();
            this.channel1.close();
        } catch (ConnectException e) {
            // FIXME: assertEquals(e.getMessage(), "Connection refused");
        }

        statusChannelClosed();
    }

    /**
     * finish-->connect-->finish-->close
     */
    public void testCFII_FinishFirst_Server_Block() throws Exception {
        // ensure
        ensureServerOpen();
        assertTrue(this.channel1.isBlocking());
        statusNotConnected_NotPending();
        // finish
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
            // OK.
        }
        statusNotConnected_NotPending();
        // connect
        assertTrue(this.channel1.connect(localAddr1));
        statusConnected_NotPending();

        tryFinish();

        this.channel1.close();
        statusChannelClosed();

    }

    /**
     * finish-->connect-->finish-->close
     */
    public void testCFII_FinishFirst_Server_NonBlock() throws Exception {
        // ensure
        ensureServerOpen();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        // finish
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
            // OK.
        }
        statusNotConnected_NotPending();
        // connect
        boolean connected = channel1.connect(localAddr1);
        if (!connected) {
            statusNotConnected_Pending();
        }
        tryFinish();

        this.channel1.close();
        statusChannelClosed();
    }

    public void testCFII_Null() throws Exception {
        statusNotConnected_NotPending();
        try {
            this.channel1.connect(null);
            fail("Should throw an IllegalArgumentException here.");
        } catch (IllegalArgumentException e) {
            // OK.
        }
    }

    public void testCFII_UnsupportedType() throws Exception {
        class SubSocketAddress extends SocketAddress {
            private static final long serialVersionUID = 1L;

            //empty
            public SubSocketAddress() {
                super();
            }
        }
        statusNotConnected_NotPending();
        SocketAddress newTypeAddress = new SubSocketAddress();
        try {
            this.channel1.connect(newTypeAddress);
            fail("Should throw an UnsupportedAddressTypeException here.");
        } catch (UnsupportedAddressTypeException e) {
            // OK.
        }
    }

    public void testCFII_Unresolved() throws IOException {
        statusNotConnected_NotPending();
        InetSocketAddress unresolved = new InetSocketAddress(
                "unresolved address", 1080);
        try {
            this.channel1.connect(unresolved);
            fail("Should throw an UnresolvedAddressException here.");
        } catch (UnresolvedAddressException e) {
            // OK.
        }
    }

    public void testCFII_EmptyHost() throws Exception {
        statusNotConnected_NotPending();
        ServerSocket server = new ServerSocket(0);
        int port = server.getLocalPort();
        server.close();
        try {
            this.channel1.connect(new InetSocketAddress("", port));
            fail("Should throw ConnectException");
        } catch (ConnectException e) {
            // correct
        }
    }

    public void testCFII_CloseFirst() throws Exception {
        this.channel1.close();
        statusChannelClosed();
        ensureServerOpen();
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
            // OK.
        }
        statusChannelClosed();
        try {
            this.channel1.finishConnect();
            fail("Should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
            // OK.
        }
        statusChannelClosed();
        try {
            this.channel1.configureBlocking(false);
            fail("Should throw ClosedChannelException.");
        } catch (ClosedChannelException e) {
            // OK.
        }
        statusChannelClosed();
    }

    public void testCFII_StatusAfterFinish() throws Exception {
        // 1. close server, finish must return false, check the status
        ensureServerClosed();

        // 1.1 block mode
        assertTrue(this.channel1.isBlocking());
        try {
            channel1.connect(localAddr1);
            fail("Should throw ConnectException");
        } catch (ConnectException e) {
            // OK.
        }
        assertFalse(this.channel1.isOpen());

        assertFalse(this.channel1.isOpen());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnectionPending());

        // 1.2 non block mode
        this.channel1 = SocketChannel.open();
        this.channel1.configureBlocking(false);
        assertFalse(this.channel1.connect(localAddr1));
        try {
            assertFalse(this.channel1.finishConnect());
            statusNotConnected_Pending();
            this.channel1.close();
        } catch (ConnectException e) {
            System.out.println(e.getMessage());
        }

        // 2. start server, finish usually return true, check the status
        ensureServerOpen();

        // 2.1 block mode
        this.channel1 = SocketChannel.open();
        assertTrue(this.channel1.isBlocking());
        assertTrue(this.channel1.connect(localAddr1));
        assertTrue(this.channel1.finishConnect());
        statusConnected_NotPending();
        this.channel1.close();

        // 2.2 non block mode
        this.channel1 = SocketChannel.open();
        this.channel1.configureBlocking(false);
        assertFalse(this.channel1.connect(localAddr1));
        tryFinish();
        this.channel1.close();
    }

    private void ensureServerClosed() throws IOException {
        if (null != this.server1) {
            this.server1.close();
            assertTrue(this.server1.isClosed());
        }
        if (null != this.server2) {
            this.server2.close();
            assertTrue(this.server2.isClosed());
        }
    }

    private void ensureServerOpen() throws IOException {
        ensureServerClosed();
        this.server1 = new ServerSocket(0);
        this.localAddr1 = new InetSocketAddress("127.0.0.1", server1.getLocalPort());
        this.server2 = new ServerSocket(0);
        this.localAddr2 = new InetSocketAddress("127.0.0.1", server2.getLocalPort());
        assertTrue(this.server1.isBound());
        assertTrue(this.server2.isBound());
    }

    private void connectNoServerNonBlock() throws Exception {
        // ensure
        ensureServerClosed();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        // connect
        assertFalse(this.channel1.connect(localAddr1));
        statusNotConnected_Pending();
        try {
            assertFalse(this.channel1.finishConnect());
            statusNotConnected_Pending();
        } catch (ConnectException e) {
            // FIXME: assertEquals(e.getMessage(), "Connection refused");
        }
    }

    private void connectServerNonBlock() throws Exception {
        // ensure
        ensureServerOpen();
        this.channel1.configureBlocking(false);
        statusNotConnected_NotPending();
        // connect
        boolean connected = channel1.connect(localAddr1);
        if (!connected) {
            statusNotConnected_Pending();
        }
        tryFinish();
    }

    private void connectServerBlock() throws Exception {
        // ensure
        ensureServerOpen();
        assertTrue(this.channel1.isBlocking());
        statusNotConnected_NotPending();
        // connect
        assertTrue(this.channel1.connect(localAddr1));
        statusConnected_NotPending();
        tryFinish();
    }

    private void statusChannelClosed() {
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertFalse(this.channel1.isOpen());
    }

    private void statusNotConnected_NotPending() {
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
    }

    private void statusNotConnected_Pending() {
        assertFalse(this.channel1.isConnected());
        assertTrue(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
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

    // -------------------------------------------------------------------
    // Original tests. Test method for CFII with real data.
    // -------------------------------------------------------------------

    /**
     *
     * 'SocketChannelImpl.connect(SocketAddress)'
     */
    public void testCFII_Data_ConnectWithServer() throws Exception {
        ensureServerOpen();
        java.nio.ByteBuffer writeBuf = java.nio.ByteBuffer
                .allocate(CAPACITY_NORMAL);
        java.nio.ByteBuffer[] writeBufArr = new java.nio.ByteBuffer[1];
        writeBufArr[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());

        this.channel1.connect(localAddr1);

        assertTrue(this.channel1.isBlocking());
        assertTrue(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf));
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBufArr, 0, 1));

        this.channel1.configureBlocking(false);
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw AlreadyConnectedException");
        } catch (AlreadyConnectedException e) {
            // correct
        }

        assertFalse(this.channel1.isRegistered());
        tryFinish();
    }

    /*
     * Test method for 'SocketChannelImpl.connect(SocketAddress)'
     */
    public void testCFII_Data_ConnectWithServer_nonBlocking() throws Exception {
        ensureServerOpen();
        java.nio.ByteBuffer writeBuf = java.nio.ByteBuffer
                .allocate(CAPACITY_NORMAL);
        java.nio.ByteBuffer[] writeBufArr = new java.nio.ByteBuffer[1];
        writeBufArr[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        this.channel1.configureBlocking(false);
        this.channel1.connect(localAddr1);

        assertFalse(this.channel1.isBlocking());
        boolean connected = channel1.isConnected();
        if (!connected) {
            assertTrue(this.channel1.isConnectionPending());
            assertTrue(this.channel1.isOpen());
        }
        if (tryFinish()) {
            assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf));
            assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBufArr, 0, 1));

            this.channel1.configureBlocking(false);
            try {
                this.channel1.connect(localAddr1);
                fail("Should throw AlreadyConnectedException");
            } catch (AlreadyConnectedException e) {
                // correct
            }
        }

        assertFalse(this.channel1.isRegistered());
        tryFinish();
    }

    /*
     * Test method for 'SocketChannelImpl.finishConnect()'
     */
    public void testCFII_Data_FinishConnect_nonBlocking() throws IOException {
        ensureServerOpen();

        java.nio.ByteBuffer writeBuf = java.nio.ByteBuffer
                .allocate(CAPACITY_NORMAL);
        java.nio.ByteBuffer[] writeBufArr = new java.nio.ByteBuffer[1];
        writeBufArr[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);

        this.channel1.configureBlocking(false);
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
            // correct
        }
        boolean connected = channel1.connect(localAddr1);
        if (!connected) {
            assertFalse(this.channel1.isBlocking());
            assertFalse(this.channel1.isConnected());
            assertTrue(this.channel1.isConnectionPending());
            assertTrue(this.channel1.isOpen());
        }
        this.server1.accept();
        if (tryFinish()) {
            assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf));
            assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBufArr, 0, 1));
            try {
                this.channel1.connect(localAddr1);
                fail("Should throw AlreadyConnectedException");
            } catch (AlreadyConnectedException e) {
                // correct
            }
        }
        assertFalse(this.channel1.isRegistered());
        tryFinish();
    }

    public void testCFII_Data_FinishConnect_AddrSetServerStartLater()
            throws IOException, InterruptedException {
        ensureServerClosed();
        this.channel1.configureBlocking(false);
        try {
            SocketChannel.open(localAddr1);
            fail("Should throw ConnectException");
        } catch (ConnectException e) {
            // correct
        }
        assertTrue(this.channel1.isOpen());
        assertFalse(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnectionPending());
        this.channel1.configureBlocking(true);
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
            // correct
        }
        try {
            this.channel1.connect(localAddr2);
            fail("Should throw ConnectException");
        } catch (ConnectException e) {
            // correct
        }

        assertTrue(this.channel1.isBlocking());
        try {
            this.channel1.finishConnect();
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // correct
        }
        assertFalse(this.channel1.isConnected());
        // finish after finish OK
        assertFalse(this.channel1.isConnectionPending());
        this.channel1 = SocketChannel.open();
        this.channel1.configureBlocking(false);
        this.channel1.connect(localAddr1);
        assertFalse(this.channel1.isConnected());
        ensureServerOpen();
        // cannot connect?
        try {
            assertFalse(this.channel1.finishConnect());
            assertFalse(this.channel1.isBlocking());
            assertFalse(this.channel1.isConnected());
            assertTrue(this.channel1.isConnectionPending());
            assertTrue(this.channel1.isOpen());
            try {
                this.channel1.connect(localAddr1);
                fail("Should throw ConnectionPendingException");
            } catch (ConnectionPendingException e) {
                // correct
            }
            this.channel1.configureBlocking(true);
            try {
                this.channel1.connect(localAddr1);
                fail("Should throw ConnectionPendingException");
            } catch (ConnectionPendingException e) {
                // correct
            }
            tryFinish();
        } catch (ConnectException e) {
            // FIXME: assertEquals(e.getMessage(), "Connection refused");
        }
    }

    public void testCFII_Data_FinishConnect_ServerStartLater()
            throws IOException {
        ensureServerClosed();
        this.channel1.configureBlocking(true);
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
            // correct
        }
        try {
            this.channel1.connect(localAddr1);
            fail("Should throw ConnectException");
        } catch (ConnectException e) {
            // correct
        }

        try {
            this.channel1.finishConnect();
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // correct
        }
        assertFalse(this.channel1.isConnected());
        // finish after finish OK
        assertFalse(this.channel1.isConnectionPending());
        this.channel1 = SocketChannel.open();
        this.channel1.configureBlocking(false);
        this.channel1.connect(localAddr1);
        assertFalse(this.channel1.isConnected());
        ensureServerOpen();
        // cannot connect?
        try {
            assertFalse(this.channel1.finishConnect());
            assertFalse(this.channel1.isBlocking());
            assertFalse(this.channel1.isConnected());
            assertTrue(this.channel1.isConnectionPending());
            assertTrue(this.channel1.isOpen());
            try {
                this.channel1.connect(localAddr1);
                fail("Should throw ConnectionPendingException");
            } catch (ConnectionPendingException e) {
                // correct
            }
            this.channel1.configureBlocking(true);
            try {
                this.channel1.connect(localAddr1);
                fail("Should throw ConnectionPendingException");
            } catch (ConnectionPendingException e) {
                // correct
            }
            tryFinish();
        } catch (ConnectException e) {
            // FIXME: assertEquals(e.getMessage(), "Connection refused");
        }
    }

    public void testCFII_Data_FinishConnect_Blocking() throws IOException {
        ensureServerOpen();
        java.nio.ByteBuffer writeBuf = java.nio.ByteBuffer
                .allocate(CAPACITY_NORMAL);
        java.nio.ByteBuffer[] writeBufArr = new java.nio.ByteBuffer[1];
        writeBufArr[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        this.channel1.configureBlocking(true);
        try {
            this.channel1.finishConnect();
            fail("Should throw NoConnectionPendingException");
        } catch (NoConnectionPendingException e) {
            // correct
        }

        this.channel1.connect(localAddr1);

        assertTrue(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        if (tryFinish()) {
            assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf));
            assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBufArr, 0, 1));

            try {
                this.channel1.connect(localAddr1);
                fail("Should throw AlreadyConnectedException");
            } catch (AlreadyConnectedException e) {
                // correct
            }
        }
        assertFalse(this.channel1.isRegistered());
        tryFinish();
    }

    /**
     * Regression test for Harmony-1947.
     */
    public void test_finishConnect() throws Exception {
        SocketAddress address = new InetSocketAddress("localhost", 0);

        ServerSocketChannel theServerChannel = ServerSocketChannel.open();
        ServerSocket serversocket = theServerChannel.socket();
        serversocket.setReuseAddress(true);
        // Bind the socket
        theServerChannel.socket().bind(address);

        boolean doneNonBlockingConnect = false;
        // Loop so that we make sure we're definitely testing finishConnect()
        while (!doneNonBlockingConnect) {
            channel1 = SocketChannel.open();

            // Set the SocketChannel to non-blocking so that connect(..) does
            // not block
            channel1.configureBlocking(false);
            boolean connected = channel1.connect(new InetSocketAddress("localhost",serversocket.getLocalPort()));
            if (!connected) {
                // Now set the SocketChannel back to blocking so that
                // finishConnect() blocks.
                channel1.configureBlocking(true);
                doneNonBlockingConnect = channel1.finishConnect();
            }
            if (doneNonBlockingConnect) {
                tryFinish();
            }
            channel1.close();
        }
        if (!serversocket.isClosed()) {
            serversocket.close();
        }
    }

    // -------------------------------------------------------------------
    // End of original tests. Test method for CFII with real data.
    // -------------------------------------------------------------------

    /**
     * @tests java.nio.channels.SocketChannel#read(ByteBuffer)
     */
    public void test_readLjava_nio_ByteBuffer_Blocking() throws IOException {
        // initialize write content
        byte[] writeContent = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < writeContent.length; i++) {
            writeContent[i] = (byte) i;
        }
        // establish connection
        channel1.connect(localAddr1);
        Socket acceptedSocket = server1.accept();

        // use OutputStream.write to send CAPACITY_NORMAL bytes data
        OutputStream out = acceptedSocket.getOutputStream();
        out.write(writeContent);
        // use close to guarantee all data is sent
        acceptedSocket.close();

        ByteBuffer readContent = ByteBuffer.allocate(CAPACITY_NORMAL + 1);
        int totalCount = 0;
        int count;
        long startTime = System.currentTimeMillis();
        // use SocketChannel.read to read data
        while (totalCount <= CAPACITY_NORMAL) {
            count = channel1.read(readContent);
            if (EOF == count) {
                break;
            }
            totalCount += count;
            // if the channel could not finish reading in TIMEOUT ms, the
            // test fails. It is used to guarantee the test never hangs even
            // if there are bugs of SocketChannel implementation. For
            // blocking read, it possibly returns 0 in some cases.
            assertTimeout(startTime, TIMEOUT);
        }
        assertEquals(CAPACITY_NORMAL, totalCount);
        readContent.flip();
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            assertEquals(writeContent[i], readContent.get());
        }
    }

    /**
     * @tests java.nio.channels.SocketChannel#read(ByteBuffer)
     */
    public void test_readLjava_nio_ByteBuffer_Nonblocking() throws IOException {
        // initialize write content
        byte[] writeContent = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < writeContent.length; i++) {
            writeContent[i] = (byte) i;
        }

        // establish connection
        channel1.connect(localAddr1);
        Socket acceptedSocket = server1.accept();
        // use OutputStream.write to write CAPACITY_NORMAL bytes data.
        OutputStream out = acceptedSocket.getOutputStream();
        out.write(writeContent);
        // use close to guarantee all data is sent
        acceptedSocket.close();

        channel1.configureBlocking(false);
        ByteBuffer readContent = ByteBuffer.allocate(CAPACITY_NORMAL + 1);
        int totalCount = 0;
        int count;
        long startTime = System.currentTimeMillis();
        // use SocketChannel.read to read data
        while (totalCount <= CAPACITY_NORMAL) {
            count = channel1.read(readContent);
            if (EOF == count) {
                break;
            }
            totalCount += count;
            // if the channel could not finish reading in TIMEOUT ms, the
            // test fails. It is used to guarantee the test never hangs even
            // if there are bugs of SocketChannel implementation.
            assertTimeout(startTime, TIMEOUT);
        }

        // assert read content
        assertEquals(CAPACITY_NORMAL, totalCount);
        assertEquals(CAPACITY_NORMAL, readContent.position());
        readContent.flip();
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            assertEquals(writeContent[i], readContent.get());
        }
    }

    /**
     * @tests java.nio.channels.SocketChannel#write(ByteBuffer)
     */
    public void test_writeLjava_nio_ByteBuffer_Blocking() throws IOException {
        // initialize write content
        ByteBuffer writeContent = ByteBuffer.allocate(CAPACITY_NORMAL);
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            writeContent.put((byte) i);
        }
        writeContent.flip();
        // establish connection
        channel1.connect(localAddr1);
        Socket acceptedSocket = server1.accept();

        // use SocketChannel.write(ByteBuffer) to write CAPACITY_NORMAL bytes
        // data
        int writtenCount = channel1.write(writeContent);
        // assert written count and ByteBuffer position
        assertEquals(CAPACITY_NORMAL, writtenCount);
        assertEquals(CAPACITY_NORMAL, writeContent.position());
        // use close to guarantee all data is sent
        channel1.close();

        InputStream in = acceptedSocket.getInputStream();
        int totalCount = 0;
        int count = 0;
        byte[] readContent = new byte[CAPACITY_NORMAL + 1];
        // if the channel could not finish reading in TIMEOUT ms, the test
        // fails. It is used to guarantee the test never hangs even if there
        // are bugs of SocketChannel implementation.
        acceptedSocket.setSoTimeout(TIMEOUT);

        // use InputStream.read to read data.
        while (totalCount <= CAPACITY_NORMAL) {
            count = in.read(readContent, totalCount, readContent.length
                    - totalCount);
            if (EOF == count) {
                break;
            }
            totalCount += count;
        }

        // assert read content
        assertEquals(CAPACITY_NORMAL, totalCount);
        writeContent.flip();
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            assertEquals(writeContent.get(), readContent[i]);
        }
    }

    /**
     * @tests java.nio.channels.SocketChannel#write(ByteBuffer)
     */
    public void test_writeLjava_nio_ByteBuffer_NonBlocking() throws Exception {
        // initialize write content
        ByteBuffer writeContent = ByteBuffer.allocate(CAPACITY_NORMAL);
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            writeContent.put((byte) i);
        }
        writeContent.flip();

        // establish connection
        channel1.connect(localAddr1);
        Socket acceptedSocket = server1.accept();

        channel1.configureBlocking(false);
        int writtenTotalCount = 0;
        int writtenCount = 0;
        long startTime = System.currentTimeMillis();
        // use SocketChannel.write(ByteBuffer) to write CAPACITY_NORMAL bytes
        while (writtenTotalCount < CAPACITY_NORMAL) {
            writtenCount = channel1.write(writeContent);
            writtenTotalCount += writtenCount;
            // if the channel could not finish writing in TIMEOUT ms, the
            // test fails. It is used to guarantee the test never hangs even
            // if there are bugs of SocketChannel implementation.
            assertTimeout(startTime, TIMEOUT);
        }
        // assert written count and ByteBuffer position
        assertEquals(CAPACITY_NORMAL, writtenTotalCount);
        assertEquals(CAPACITY_NORMAL, writeContent.position());
        // use close to guarantee all data is sent
        channel1.close();

        InputStream in = acceptedSocket.getInputStream();
        byte[] readContent = new byte[CAPACITY_NORMAL + 1];
        int totalCount = 0;
        int count = 0;
        // if the channel could not finish reading in TIMEOUT ms, the test
        // fails. It is used to guarantee the test never hangs even if there
        // are bugs of SocketChannel implementation.
        acceptedSocket.setSoTimeout(TIMEOUT);
        // use InputStream.read to read data.
        while (totalCount <= CAPACITY_NORMAL) {
            count = in.read(readContent, totalCount, readContent.length
                    - totalCount);
            if (EOF == count) {
                break;
            }
            totalCount += count;
        }
        // assert read content
        assertEquals(CAPACITY_NORMAL, totalCount);
        writeContent.flip();
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            assertEquals(writeContent.get(), readContent[i]);
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

    // -------------------------------------------------
    // Test for read/write but no real data expressed
    // -------------------------------------------------

    public void testReadByteBuffer() throws Exception {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer readBuf = java.nio.ByteBuffer
                .allocate(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        // note: blocking-mode will make the read process endless!
        this.channel1.configureBlocking(false);
        try {
            channel1.read(readBuf);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
            // correct
        }
        boolean connected = this.channel1.connect(localAddr1);
        if (!connected) {
            assertFalse(this.channel1.isBlocking());
            assertTrue(this.channel1.isConnectionPending());
            assertFalse(this.channel1.isConnected());
        }
        if (tryFinish()) {
            assertEquals(0, this.channel1.read(readBuf));
        }

        this.channel1.close();
        try {
            channel1.read(readBuf);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // correct
        }
    }

    public void testReadByteBuffer_Direct() throws Exception {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer readBuf = java.nio.ByteBuffer
                .allocateDirect(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        // note: blocking-mode will make the read process endless!
        this.channel1.configureBlocking(false);
        try {
            channel1.read(readBuf);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
            // correct
        }
        boolean connected = this.channel1.connect(localAddr1);
        if (!connected) {
            assertFalse(this.channel1.isBlocking());
            assertTrue(this.channel1.isConnectionPending());
            assertFalse(this.channel1.isConnected());
        }
        if (tryFinish()) {
            assertEquals(0, this.channel1.read(readBuf));
        }

        this.channel1.close();
        try {
            channel1.read(readBuf);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // correct
        }
    }

    public void testReadByteBuffer_Direct2() throws IOException {
        byte[] request = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        ByteBuffer buffer = ByteBuffer.allocateDirect(128);

        ServerSocketChannel server = ServerSocketChannel.open();
        try {
          server.socket().bind(new InetSocketAddress(InetAddress.getLocalHost(), 0), 5);
        } catch (BindException e) {
          // Continuous build environment doesn't support localhost sockets.
          return;
        }
        Socket client = new Socket(InetAddress.getLocalHost(), server.socket()
                .getLocalPort());
        client.setTcpNoDelay(false);
        Socket worker = server.socket().accept();
        SocketChannel workerChannel = worker.getChannel();

        OutputStream out = client.getOutputStream();
        out.write(request);
        out.close();

        buffer.limit(5);
        int bytesRead = workerChannel.read(buffer);
        assertEquals(5, bytesRead);
        assertEquals(5, buffer.position());

        buffer.limit(request.length);
        bytesRead = workerChannel.read(buffer);
        assertEquals(6, bytesRead);

        buffer.flip();
        assertEquals(request.length, buffer.limit());

        assertEquals(ByteBuffer.wrap(request), buffer);

        client.close();
        worker.close();
        server.close();
    }

    public void testReadByteBuffer_BufNull() throws Exception {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer readBuf = java.nio.ByteBuffer.allocate(0);
        // note: blocking-mode will make the read process endless!
        this.channel1.configureBlocking(false);
        try {
            channel1.read((java.nio.ByteBuffer) null);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // correct
        }
        this.channel1.connect(localAddr1);
        if (tryFinish()) {
            try {
                this.channel1.read((java.nio.ByteBuffer) null);
                fail("Should throw NPE");
            } catch (NullPointerException e) {
                // correct
            }
            assertEquals(0, this.channel1.read(readBuf));
        }
        this.server1.close();
        try {
            channel1.read((java.nio.ByteBuffer) null);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // correct
        }
    }

    /*
     * SocketChannelImpl.read(ByteBuffer[], int, int)'
     */
    public void testReadByteBufferArrayIntInt() throws Exception {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer[] readBuf = new java.nio.ByteBuffer[2];
        readBuf[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        readBuf[1] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        // note: blocking-mode will make the read process endless!
        this.channel1.configureBlocking(false);
        try {
            channel1.read(readBuf, 0, 1);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
            // correct
        }
        boolean connected = this.channel1.connect(localAddr1);
        if (!connected) {
            assertFalse(this.channel1.isBlocking());
            assertTrue(this.channel1.isConnectionPending());
            assertFalse(this.channel1.isConnected());
        }
        if (tryFinish()) {
            assertEquals(0, this.channel1.read(readBuf, 0, 1));
            assertEquals(0, this.channel1.read(readBuf, 0, 2));
        }

        this.channel1.close();
        try {
            channel1.read(readBuf, 0, 1);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // correct
        }
    }

    /*
     * SocketChannelImpl.read(ByteBuffer[], int, int)'
     */
    public void testReadByteBufferArrayIntInt_Direct() throws Exception {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer[] readBuf = new java.nio.ByteBuffer[2];
        readBuf[0] = java.nio.ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        readBuf[1] = java.nio.ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        // note: blocking-mode will make the read process endless!
        this.channel1.configureBlocking(false);
        try {
            channel1.read(readBuf, 0, 1);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
            // correct
        }
        boolean connected = this.channel1.connect(localAddr1);
        if (!connected) {
            assertFalse(this.channel1.isBlocking());
            assertTrue(this.channel1.isConnectionPending());
            assertFalse(this.channel1.isConnected());
        }
        if (tryFinish()) {
            assertEquals(0, this.channel1.read(readBuf, 0, 1));
            assertEquals(0, this.channel1.read(readBuf, 0, 2));
        }

        this.channel1.close();
        try {
            channel1.read(readBuf, 0, 1);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // correct
        }
    }

    public void testReadByteBufferArrayIntInt_BufNull() throws Exception {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer[] readBuf = new java.nio.ByteBuffer[2];
        readBuf[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        // note: blocking-mode will make the read process endless!
        this.channel1.configureBlocking(false);
        try {
            channel1.read(null, 0, 1);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // correct
        }
        this.channel1.connect(localAddr1);
        if (tryFinish()) {

            try {
                channel1.read(null, 0, 1);
                fail("Should throw NPE");
            } catch (NullPointerException e) {
                // correct
            }
            try {
                channel1.read(readBuf, 0, 2);
                fail("Should throw NPE");
            } catch (NullPointerException e) {
                // correct
            }

            assertEquals(0, this.channel1.read(readBuf, 0, 1));
        }
        this.channel1.close();
        try {
            channel1.read(null, 0, 1);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // correct
        }
    }

    public void testWriteByteBuffer() throws IOException {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer writeBuf = java.nio.ByteBuffer
                .allocate(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        try {
            channel1.write(writeBuf);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
            // correct
        }
        this.channel1.connect(localAddr1);
        assertTrue(this.channel1.isBlocking());
        assertTrue(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf));

        this.channel1.close();
        try {
            channel1.write(writeBuf);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // correct
        }
    }

    public void testWriteByteBuffer_Direct() throws IOException {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer writeBuf = java.nio.ByteBuffer
                .allocateDirect(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        try {
            channel1.write(writeBuf);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
            // correct
        }
        this.channel1.connect(localAddr1);
        assertTrue(this.channel1.isBlocking());
        assertTrue(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf));

        this.channel1.close();
        try {
            channel1.write(writeBuf);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // correct
        }
    }

    public void testWriteByteBuffer_BufNull() throws IOException {
        assertTrue(this.server1.isBound());
        java.nio.ByteBuffer writeBuf = java.nio.ByteBuffer.allocate(0);
        this.channel1.connect(localAddr1);
        assertEquals(this.channel1.write(writeBuf), 0);
        try {
            this.channel1.write((java.nio.ByteBuffer) null);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // correct
        }
    }

    /*
     * SocketChannelImpl.write(ByteBuffer[], int, int)'
     */
    public void testWriteByteBufferArrayIntInt() throws IOException {
        java.nio.ByteBuffer[] writeBuf = new java.nio.ByteBuffer[2];
        writeBuf[0] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        writeBuf[1] = java.nio.ByteBuffer.allocate(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        try {
            channel1.write(writeBuf, 0, 1);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
            // correct
        }
        this.channel1.connect(localAddr1);
        assertTrue(this.channel1.isBlocking());
        assertTrue(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf, 0, 1));
        // still writes the same size as above
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf, 0, 2));
        writeBuf[0].flip();
        writeBuf[1].flip();
        assertEquals(CAPACITY_NORMAL * 2, this.channel1.write(writeBuf, 0, 2));
        this.channel1.close();
        try {
            channel1.write(writeBuf);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // correct
        }
    }

    /*
     * SocketChannelImpl.write(ByteBuffer[], int, int)'
     */
    public void testWriteByteBufferArrayIntInt_Direct() throws IOException {
        java.nio.ByteBuffer[] writeBuf = new java.nio.ByteBuffer[2];
        writeBuf[0] = java.nio.ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        writeBuf[1] = java.nio.ByteBuffer.allocateDirect(CAPACITY_NORMAL);
        assertFalse(this.channel1.isRegistered());
        assertTrue(this.channel1.isBlocking());
        assertFalse(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        try {
            channel1.write(writeBuf, 0, 1);
            fail("Should throw NotYetConnectedException");
        } catch (NotYetConnectedException e) {
            // correct
        }
        this.channel1.connect(localAddr1);
        assertTrue(this.channel1.isBlocking());
        assertTrue(this.channel1.isConnected());
        assertFalse(this.channel1.isConnectionPending());
        assertTrue(this.channel1.isOpen());
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf, 0, 1));
        // still writes the same size as above
        assertEquals(CAPACITY_NORMAL, this.channel1.write(writeBuf, 0, 2));
        writeBuf[0].flip();
        writeBuf[1].flip();
        assertEquals(CAPACITY_NORMAL * 2, this.channel1.write(writeBuf, 0, 2));
        this.channel1.close();
        try {
            channel1.write(writeBuf);
            fail("Should throw ClosedChannelException");
        } catch (ClosedChannelException e) {
            // correct
        }
    }

    public void testWriteByteBufferArrayIntInt_BufNull() throws IOException {
        java.nio.ByteBuffer[] writeBuf = new java.nio.ByteBuffer[0];

        this.channel1.connect(localAddr1);
        try {
            this.channel1.write(null, 0, 1);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // correct
        }
        assertEquals(0, this.channel1.write(writeBuf, 0, 0));
        try {
            this.channel1.write(writeBuf, 0, 1);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // correct
        }
        writeBuf = new java.nio.ByteBuffer[1];
        try {
            this.channel1.write(writeBuf, 0, 1);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // correct
        }
        try {
            this.channel1.write(writeBuf, 0, 2);
            fail("Should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // correct
        }
        this.server1.close();
        try {
            channel1.read(null, 0, 1);
            fail("Should throw NPE");
        } catch (NullPointerException e) {
            // correct
        }
    }

    public void testWriteByteBufferArrayIntInt_SizeError() throws IOException {
        java.nio.ByteBuffer[] buf = new java.nio.ByteBuffer[1];
        this.channel1.connect(localAddr1);
        assertEquals(0, this.channel1.write(buf, 0, 0));
        try {
            this.channel1.write(buf, -1, 1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            this.channel1.write(buf, 0, -1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            this.channel1.write(buf, 0, 2);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            this.channel1.write(buf, 2, 0);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            this.channel1.write(null, 0, 0);
            fail();
        } catch (NullPointerException expected) {
        }
        this.server1.close();
    }

    public void testReadByteBufferArrayIntInt_SizeError() throws IOException {
        java.nio.ByteBuffer[] buf = new java.nio.ByteBuffer[1];
        this.channel1.connect(localAddr1);
        assertEquals(0, this.channel1.read(buf, 0, 0));
        try {
            this.channel1.read(buf, -1, 1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            this.channel1.read(buf, 0, -1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            this.channel1.read(buf, 0, 2);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            this.channel1.read(buf, 2, 0);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            this.channel1.read(null, 0, 0);
            fail();
        } catch (NullPointerException expected) {
        }
        this.server1.close();
    }

    /*
     * ==========================================================================
     * Tests for read/write real data
     * ==========================================================================
     */


    /**
     * @tests java.nio.channels.SocketChannel#read(ByteBuffer[])
     */
    public void test_read$LByteBuffer() throws IOException {
        MockSocketChannel sc = new MockSocketChannel(null);
        ByteBuffer [] byteBufferArray = { ByteBuffer.allocate(1), ByteBuffer.allocate(1)};
        // Verify that calling read(ByteBuffer[]) leads to the method
        // read(ByteBuffer[], int, int) being called with a 0 for the
        // second parameter and targets.length as the third parameter.
        sc.read(byteBufferArray);
        assertTrue(sc.isReadCalled);
    }
    /**
     * @tests java.nio.channels.SocketChannel#read(ByteBuffer[],int,int)
     */
    public void test_read$LByteBufferII_blocking() throws Exception {
        assert_read$LByteBuffer(true);
    }

    /**
     * @tests java.nio.channels.SocketChannel#read(ByteBuffer[],int,int)
     */
    public void test_read$LByteBufferII_nonblocking() throws Exception {
        assert_read$LByteBuffer(false);
    }

    private void assert_read$LByteBuffer(boolean isBlocking) throws IOException {
        // initialize write content
        byte[] writeContent = new byte[CAPACITY_NORMAL * 2];
        for (int i = 0; i < CAPACITY_NORMAL * 2; i++) {
            writeContent[i] = (byte) i;
        }
        ByteBuffer[] readContents = new ByteBuffer[2];
        readContents[0] = ByteBuffer.allocate(CAPACITY_NORMAL);
        readContents[1] = ByteBuffer.allocate(CAPACITY_NORMAL + 1);
        // establish connection
        channel1.connect(localAddr1);
        Socket acceptedSocket = server1.accept();
        // use OutputStream.write to send CAPACITY_NORMAL * 2 bytes data
        OutputStream out = acceptedSocket.getOutputStream();
        out.write(writeContent);
        // use close to guarantee all data is sent
        acceptedSocket.close();
        // configure block/nonblock mode
        channel1.configureBlocking(isBlocking);
        long startTime = System.currentTimeMillis();
        long totalRead = 0;
        long countRead;

        while (totalRead <= CAPACITY_NORMAL * 2) {
            countRead = channel1.read(readContents, 0, 2);
            if (0 == countRead && !readContents[1].hasRemaining()) {
                // read returns 0 because readContents is full
                break;
            }
            if (EOF == countRead) {
                break;
            }
            totalRead += countRead;
            // if the channel could not finish reading in TIMEOUT ms, the
            // test fails. It is used to guarantee the test never hangs even
            // if there are bugs of SocketChannel implementation. For
            // blocking read, it possibly returns 0 in some cases.
            assertTimeout(startTime, TIMEOUT);
        }

        // assert total bytes read and the position of ByteBuffers
        assertEquals(CAPACITY_NORMAL * 2, totalRead);
        assertEquals(CAPACITY_NORMAL, readContents[0].position());
        assertEquals(CAPACITY_NORMAL, readContents[1].position());
        // assert read content
        readContents[0].flip();
        readContents[1].flip();
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            assertEquals(writeContent[i], readContents[0].get());
        }
        for (int i = CAPACITY_NORMAL; i < CAPACITY_NORMAL * 2; i++) {
            assertEquals(writeContent[i], readContents[1].get());
        }
    }

    /**
     * @tests java.nio.channels.SocketChannel#write(ByteBuffer[],int,int)
     */
    public void test_write$LByteBufferII_blocking() throws Exception {
        assert_write$LByteBuffer(true);
    }

    /**
     * @tests java.nio.channels.SocketChannel#write(ByteBuffer[],int,int)
     */
    public void test_write$LByteBufferII_nonblocking()
            throws Exception {
        assert_write$LByteBuffer(false);
    }

    private void assert_write$LByteBuffer(boolean isBlocking)
            throws IOException {
        // initialize write contents
        ByteBuffer writeContents[] = new ByteBuffer[2];
        writeContents[0] = ByteBuffer.allocate(CAPACITY_NORMAL);
        writeContents[1] = ByteBuffer.allocate(CAPACITY_NORMAL);
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            writeContents[0].put((byte) i);
        }
        for (int i = CAPACITY_NORMAL; i < CAPACITY_NORMAL * 2; i++) {
            writeContents[1].put((byte) i);
        }
        writeContents[0].flip();
        writeContents[1].flip();
        // establish connection
        channel1.connect(localAddr1);
        Socket acceptedSocket = server1.accept();
        // set blocking/nonblocking mode
        channel1.configureBlocking(isBlocking);

        assertEquals(CAPACITY_NORMAL, channel1.write(writeContents, 0, 1));
        assertEquals(CAPACITY_NORMAL, channel1.write(writeContents, 1, 1));

        // assert written count and ByteBuffer position
        assertEquals(CAPACITY_NORMAL, writeContents[0].position());
        assertEquals(CAPACITY_NORMAL, writeContents[1].position());
        // use close to guarantee all data is sent
        channel1.close();
        InputStream in = acceptedSocket.getInputStream();
        byte[] readContent = new byte[CAPACITY_NORMAL * 2 + 1];
        int totalCount = 0;
        int count;
        // if the channel could not finish reading in TIMEOUT ms, the test
        // fails. It is used to guarantee the test never hangs even if there
        // are bugs of SocketChannel implementation.
        acceptedSocket.setSoTimeout(TIMEOUT);
        // use InputStream.read to read data.
        while (totalCount <= CAPACITY_NORMAL) {
            count = in.read(readContent, totalCount, readContent.length
                    - totalCount);
            if (EOF == count) {
                break;
            }
            totalCount += count;
        }
        // assert read content
        assertEquals(CAPACITY_NORMAL * 2, totalCount);
        writeContents[0].flip();
        writeContents[1].flip();
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            assertEquals(writeContents[0].get(), readContent[i]);
        }
        for (int i = CAPACITY_NORMAL; i < CAPACITY_NORMAL * 2; i++) {
            assertEquals(writeContents[1].get(), readContent[i]);
        }
    }

    /**
     * @tests java.nio.channels.SocketChannel#write(ByteBuffer[])
     */
    public void test_write$LByteBuffer() throws IOException {
        MockSocketChannel sc = new MockSocketChannel(null);
        ByteBuffer [] byteBufferArray = { ByteBuffer.allocate(1), ByteBuffer.allocate(1)};
        // Verify that calling write(ByteBuffer[]) leads to the method
        // write(ByteBuffer[], int, int) being called with a 0 for the
        // second parameter and sources.length as the third parameter.
        sc.write(byteBufferArray);
        assertTrue(sc.isWriteCalled);
    }

    /**
     * @tests java.nio.channels.SocketChannel#write(ByteBuffer[])
     */
    public void test_writev() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(null);
        SocketChannel sc = SocketChannel.open();
        sc.connect(ssc.socket().getLocalSocketAddress());
        SocketChannel sock = ssc.accept();
        ByteBuffer[] buf = { ByteBuffer.allocate(10), ByteBuffer.allocateDirect(20) };

        while (buf[0].remaining() != 0 && buf[1].remaining() !=0) {
            assertTrue(sc.write(buf, 0, 2) >= 0);
        }

        ByteBuffer target = ByteBuffer.allocate(30);

        while (target.remaining() != 0) {
            assertTrue(sock.read(target) >=0);
        }

        ssc.close();
        sc.close();
        sock.close();
    }

    /**
     * @tests java.nio.channels.SocketChannel#write(ByteBuffer[])
     */
    /* J2ObjC: not supported on iOS.
    public void test_writev2() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.socket().bind(null);
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        boolean connected = sc.connect(ssc.socket().getLocalSocketAddress());
        SocketChannel sock = ssc.accept();
        if (!connected) {
            sc.finishConnect();
        }

        ByteBuffer buf1 = ByteBuffer.allocate(10);
        sc.socket().setSendBufferSize(512);
        int bufSize = sc.socket().getSendBufferSize();
        ByteBuffer buf2 = ByteBuffer.allocate(bufSize * 10);

        ByteBuffer[] sent = new ByteBuffer[2];
        sent[0] = buf1;
        sent[1] = buf2;

        long whole = buf1.remaining() + buf2.remaining();

        long write = sc.write(sent);
        ssc.close();
        sc.close();
        sock.close();

        assertTrue(whole == (write + buf1.remaining() + buf2.remaining()));
    }
    */

    /**
     * @tests java.nio.channels.SocketChannel#write(ByteBuffer[])
     *
     * In non-blocking mode, the native system call will return EAGAIN/EWOULDBLOCK error
     * code on Linux/Unix and return WSATRY_AGAIN/WSAEWOULDBLOCK error code on Windows.
     * These error code means try again but not fatal error, so we should not throw exception.
     */
    /* J2ObjC: not supported on iOS.
    public void test_write$NonBlockingException() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.socket().bind(null);
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        boolean connected = sc.connect(ssc.socket().getLocalSocketAddress());
        SocketChannel sock = ssc.accept();
        if (!connected) {
            sc.finishConnect();
        }

        try {
            for (int i = 0; i < 100; i++) {
                ByteBuffer buf1 = ByteBuffer.allocate(10);
                sc.socket().setSendBufferSize(512);
                int bufSize = sc.socket().getSendBufferSize();
                ByteBuffer buf2 = ByteBuffer.allocate(bufSize * 10);

                ByteBuffer[] sent = new ByteBuffer[2];
                sent[0] = buf1;
                sent[1] = buf2;

                sc.write(sent);
            }
        } finally {
            ssc.close();
            sc.close();
            sock.close();
        }

    }
    */

    /**
     * @tests java.nio.channels.SocketChannel#write(ByteBuffer[])
     */
    public void test_write$LByteBuffer2() throws IOException {
        // Set-up
        ServerSocketChannel server = ServerSocketChannel.open();
        server.socket().bind(null);
        SocketChannel client = SocketChannel.open();
        client.connect(server.socket().getLocalSocketAddress());
        SocketChannel worker = server.accept();

        // Test overlapping buffers
        byte[] data = "Hello world!".getBytes("UTF-8");
        ByteBuffer[] buffers = new ByteBuffer[3];
        buffers[0] = ByteBuffer.wrap(data, 0, 6);
        buffers[1] = ByteBuffer.wrap(data, 6, data.length - 6);
        buffers[2] = ByteBuffer.wrap(data);

        // Write them out, read what we wrote and check it
        client.write(buffers);
        client.close();
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        while (EOF != worker.read(readBuffer)) {}
        readBuffer.flip();
        Buffer expected = ByteBuffer.allocate(1024).put(data).put(data).flip();
        assertEquals(expected, readBuffer);

        // Tidy-up
        worker.close();
        server.close();
    }

    /**
     * @tests java.nio.channels.SocketChannel#write(ByteBuffer[])
     */
    public void test_write$LByteBuffer_buffers() throws IOException {
        // Set-up
        ServerSocketChannel server = ServerSocketChannel.open();
        server.socket().bind(null);
        SocketChannel client = SocketChannel.open();
        client.connect(server.socket().getLocalSocketAddress());
        SocketChannel worker = server.accept();

        // A variety of buffer types to write
        byte[] data = "Hello world!".getBytes("UTF-8");
        ByteBuffer[] buffers = new ByteBuffer[3];
        buffers[0] = ByteBuffer.wrap(data, 0, 2);
        assertFalse(buffers[0].isDirect());
        assertTrue(buffers[0].hasArray());

        buffers[1] = ByteBuffer.wrap(data, 2, 4).asReadOnlyBuffer();
        assertFalse(buffers[1].isDirect());
        assertFalse(buffers[1].hasArray());

        buffers[2] = ByteBuffer.allocateDirect(42);
        buffers[2].put(data, 6, data.length - 6);
        buffers[2].flip();
        assertTrue(buffers[2].isDirect());

        // Write them out, read what we wrote and check it
        client.write(buffers);
        client.close();
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        while (EOF != worker.read(readBuffer)) {}
        readBuffer.flip();
        assertEquals(ByteBuffer.wrap(data), readBuffer);

        // Tidy-up
        worker.close();
        server.close();
    }

    /**
     * @tests java.nio.channels.SocketChannel#write(ByteBuffer[])
     */
    public void test_write$LByteBuffer_writes() throws IOException {
        // Set-up
        ServerSocketChannel server = ServerSocketChannel.open();
        server.socket().bind(null);
        SocketChannel client = SocketChannel.open();
        client.connect(server.socket().getLocalSocketAddress());
        SocketChannel worker = server.accept();

        // Data to write
        byte[] data = "Hello world!".getBytes("UTF-8");
        ByteBuffer[] buffers = new ByteBuffer[3];
        buffers[0] = ByteBuffer.wrap(data, 0, 6);
        buffers[1] = ByteBuffer.wrap("world!".getBytes("UTF-8"));
        buffers[2] = buffers[0];
        assertTrue(buffers[0].hasArray());

        // Test a sequence of write calls
        client.write(buffers, 0, 0); // write nothing
        client.write(buffers, 1, 0); // write nothing
        client.write(buffers, 0, 1); // write "Hello "
        assertEquals("Failed to drain buffer 0", 0, buffers[0].remaining());
        assertEquals("Shouldn't touch buffer 1", buffers[1].limit(), buffers[1]
                .remaining());
        client.write(buffers, 0, 2); // writes "world!"
        assertEquals("Failed to drain buffer 1", 0, buffers[1].remaining());
        client.write(buffers, 0, 3); // write nothing
        client.close();

        // Read what we wrote and check it
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        while (EOF != worker.read(readBuffer)) {}
        readBuffer.flip();
        assertEquals(ByteBuffer.wrap(data), readBuffer);

        // Tidy-up
        worker.close();
        server.close();
    }

    /**
     * @tests java.nio.channels.SocketChannel#write(ByteBuffer[])
     */
    public void test_write$LByteBuffer_invalid() throws IOException {
        // Set-up
        ServerSocketChannel server = ServerSocketChannel.open();
        server.socket().bind(null);

        SocketChannel client = SocketChannel.open();
        client.connect(server.socket().getLocalSocketAddress());

        SocketChannel worker = server.accept();

        // Do some stuff
        try {
            client.write((ByteBuffer[]) null);
            fail("Should throw a NPE");
        } catch (NullPointerException expected) {
        }
        try {
            client.write((ByteBuffer[]) null, 0, 0);
            fail("Should throw a NPE");
        } catch (NullPointerException expected) {
        }
        try {
            client.write((ByteBuffer[]) null, 1, 0);
            fail("Should throw a NPE");
        } catch (NullPointerException expected) {
        }
        try {
            client.write((ByteBuffer[]) null, 0, 1);
            fail("Should throw a NPE");
        } catch (NullPointerException expected) {
        }
        try {
            client.write((ByteBuffer[]) null, 1, 1);
            fail("Should throw a NPE");
        } catch (NullPointerException expected) {
        }

        ByteBuffer[] buffers = new ByteBuffer[1];
        buffers[0] = ByteBuffer.wrap("Hello ".getBytes("UTF-8"));

        try {
            client.write(buffers, -1, 1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            client.write(buffers, 0, -1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            client.write(buffers, 0, 2);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            client.write(buffers, 2, 0);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            client.write(null, 0, 0);
            fail();
        } catch (NullPointerException expected) {
        }

        // Tidy-up
        worker.close();
        client.close();
        server.close();
    }

    public void testSocket_configureblocking() throws IOException {
        byte[] serverWBuf = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < serverWBuf.length; i++) {
            serverWBuf[i] = (byte) i;
        }
        java.nio.ByteBuffer buf = java.nio.ByteBuffer
                .allocate(CAPACITY_NORMAL + 1);
        channel1.connect(localAddr1);
        server1.accept();
        Socket sock = this.channel1.socket();
        channel1.configureBlocking(false);
        assertFalse(channel1.isBlocking());
        OutputStream channelSocketOut = sock.getOutputStream();
        try {
            // write operation is not allowed in non-blocking mode
            channelSocketOut.write(buf.array());
            fail("Non-Blocking mode should cause IllegalBlockingModeException");
        } catch (IllegalBlockingModeException e) {
            // correct
        }
        channel1.configureBlocking(true);
        assertTrue(channel1.isBlocking());
        // write operation is allowed in blocking mode
        channelSocketOut.write(buf.array());
    }

    /**
     * @tests SocketChannel#read(ByteBuffer[], int, int) when remote server
     *        closed
     */
    public void test_socketChannel_read_ByteBufferII_remoteClosed()
            throws Exception {
        // regression 1 for HARMONY-549
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(null);
        SocketChannel sc = SocketChannel.open();
        sc.connect(ssc.socket().getLocalSocketAddress());
        ssc.accept().close();
        ByteBuffer[] buf = { ByteBuffer.allocate(10) };
        assertEquals(-1, sc.read(buf, 0, 1));
        ssc.close();
        sc.close();
    }

    /**
     * @tests SocketChannel#write(ByteBuffer[], int, int)
     */
    public void test_socketChannel_write_ByteBufferII() throws Exception {
        // regression 2 for HARMONY-549
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(null);
        SocketChannel sc = SocketChannel.open();
        sc.connect(ssc.socket().getLocalSocketAddress());
        SocketChannel sock = ssc.accept();
        ByteBuffer[] buf = { ByteBuffer.allocate(10), null };
        try {
            sc.write(buf, 0, 2);
            fail("should throw NPE");
        } catch (NullPointerException expected) {
        }
        ssc.close();
        sc.close();
        ByteBuffer target = ByteBuffer.allocate(10);
        assertEquals(-1, sock.read(target));
    }

    /**
     * @tests SocketChannel#read(ByteBuffer[], int, int) with a null ByteBuffer
     */
    public void test_socketChannel_read_ByteBufferII_bufNULL() throws Exception {
        // regression 3 for HARMONY-549
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(null);
        SocketChannel sc = SocketChannel.open();
        sc.connect(ssc.socket().getLocalSocketAddress());
        ssc.accept();
        ByteBuffer[] buf = new ByteBuffer[2];
        buf[0] = ByteBuffer.allocate(1);
        // let buf[1] be null
        try {
            sc.read(buf, 0, 2);
            fail("should throw NullPointerException");
        } catch (NullPointerException expected) {
        }
        ssc.close();
        sc.close();
    }

    /**
     * @tests SocketChannel#write(ByteBuffer) after close
     */
    public void test_socketChannel_write_close() throws Exception {
        // regression 4 for HARMONY-549
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(null);
        SocketChannel sc = SocketChannel.open();
        sc.connect(ssc.socket().getLocalSocketAddress());
        SocketChannel sock = ssc.accept();
        ByteBuffer buf = null;
        ssc.close();
        sc.close();
        try {
            sc.write(buf);
            fail("should throw NPE");
        } catch (NullPointerException expected) {
        }
        sock.close();
    }

    /**
     * @tests SocketChannel#write(ByteBuffer) if position is not zero
     */
    public void test_socketChannel_write_ByteBuffer_posNotZero()
            throws Exception {
        // regression 5 for HARMONY-549
        final String testStr = "Hello World";
        ByteBuffer readBuf = ByteBuffer.allocate(11);
        ByteBuffer buf = ByteBuffer.wrap(testStr.getBytes());
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(null);
        SocketChannel sc = SocketChannel.open();
        sc.connect(ssc.socket().getLocalSocketAddress());
        buf.position(2);
        ssc.accept().write(buf);
        assertEquals(9, sc.read(readBuf));
        buf.flip();
        readBuf.flip();
        byte[] read = new byte[9];
        byte[] write = new byte[11];
        buf.get(write);
        readBuf.get(read);
        for (int i = 0; i < 9; i++) {
            assertEquals(read[i], write[i + 2]);
        }
    }

    /**
     * @tests SocketChannelImpl#read(ByteBuffer[])
     */
    public void test_read_$ByteBuffer_Blocking() throws IOException {
        // regression test for Harmony-728
        byte[] data = new byte[CAPACITY_NORMAL];
        for (int i = 0; i < CAPACITY_NORMAL; i++) {
            data[i] = (byte) i;
        }
        ByteBuffer[] buf = new ByteBuffer[2];
        buf[0] = ByteBuffer.allocate(CAPACITY_NORMAL);
        buf[1] = ByteBuffer.allocate(CAPACITY_NORMAL);
        channel1.connect(localAddr1);
        Socket socket = null;
        try {
            socket = server1.accept();
            OutputStream out = socket.getOutputStream();
            out.write(data);
            // should not block here
            channel1.read(buf);
        } finally {
            if (null != socket) {
                socket.close();
            }
        }
    }

    public void test_socket_getOutputStream_nonBlocking_read_Exception() throws IOException {
        byte[] buf = new byte[1];
        channel1.connect(this.localAddr1);
        InputStream is = channel1.socket().getInputStream();
        channel1.configureBlocking(false);
        try {
            is.read();
            fail();
        } catch (IllegalBlockingModeException expected) {
        }

        try {
            is.read(null);
            fail();
        } catch (NullPointerException expected) {
            // Any of these exceptions are possible.
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        }

        try {
            is.read(buf, -1, 1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
            // Any of these exceptions are possible.
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        }

        try {
            is.read(buf, 0, -1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
            // Any of these exceptions are possible.
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        }

        try {
            is.read(buf, 0, 2);
            fail();
        } catch (IndexOutOfBoundsException expected) {
            // Any of these exceptions are possible.
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        }

        try {
            is.read(buf, 2, 0);
            fail();
        } catch (IndexOutOfBoundsException expected) {
            // Any of these exceptions are possible.
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        }

        try {
            is.read(null, 0, 0);
            fail();
        } catch (NullPointerException expected) {
            // Any of these exceptions are possible.
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        }

        is.close();

        try {
            is.read();
            fail();
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (IOException expected) {
            // Any of these exceptions are possible.
        }

        try {
            is.read(null);
            fail();
        } catch (NullPointerException expected) {
            // Any of these exceptions are possible.
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (IOException expected) {
            // Any of these exceptions are possible.
        }

        try {
            is.read(buf, -1, 1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
            // Any of these exceptions are possible.
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (IOException expected) {
            // Any of these exceptions are possible.
        }

        try {
            is.read(buf, 0, -1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
            // Any of these exceptions are possible.
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (IOException expected) {
            // Any of these exceptions are possible.
        }

        try {
            is.read(buf, 0, 2);
            fail();
        } catch (IndexOutOfBoundsException expected) {
            // Any of these exceptions are possible.
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (IOException expected) {
            // Any of these exceptions are possible.
        }

        try {
            is.read(buf, 2, 0);
            fail();
        } catch (IndexOutOfBoundsException expected) {
            // Any of these exceptions are possible.
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (IOException expected) {
            // Any of these exceptions are possible.
        }

        try {
            is.read(null, 0, 0);
            fail();
        } catch (NullPointerException expected) {
            // Any of these exceptions are possible.
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (IOException expected) {
            // Any of these exceptions are possible.
        }
    }

    public void test_socket_getOutputStream_blocking_read_Exception() throws IOException {
        byte[] buf = new byte[1];
        channel1.connect(this.localAddr1);
        InputStream is = channel1.socket().getInputStream();
        try {
            is.read(null);
            fail();
        } catch (NullPointerException expected) {
        }
        try {
            is.read(buf, -1, 1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            is.read(buf, 0, -1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            is.read(buf, 0, 2);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            is.read(buf, 2, 1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            is.read(null, 0, 1);
            fail();
        } catch (NullPointerException expected) {
        }

        is.close();

        try {
            is.read(null);
            fail();
        } catch (NullPointerException expected) {
        }
        try {
            is.read(buf, -1, 1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            is.read(buf, 0, -1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            is.read(buf, 0, 2);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            is.read(buf, 2, 1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            is.read(null, 0, 1);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void test_socket_getOutputStream_nonBlocking_write_Exception() throws IOException {
        byte[] buf = new byte[1];
        channel1.connect(this.localAddr1);
        OutputStream os = channel1.socket().getOutputStream();
        channel1.configureBlocking(false);

        try {
            os.write(1);
            fail();
        } catch (IllegalBlockingModeException expected) {
        }
        try {
            os.write(null);
            fail();
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (NullPointerException expected) {
            // Any of these exceptions are possible.
        }

        try {
            os.write(buf, -1, 1);
            fail();
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (IndexOutOfBoundsException expected) {
            // Any of these exceptions are possible.
        }

        try {
            os.write(buf, 0, -1);
            fail();
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (IndexOutOfBoundsException expected) {
            // Any of these exceptions are possible.
        }

        try {
            os.write(buf, 0, 2);
            fail();
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (IndexOutOfBoundsException expected) {
            // Any of these exceptions are possible.
        }

        try {
            os.write(buf, 2, 1);
            fail();
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (IndexOutOfBoundsException expected) {
            // Any of these exceptions are possible.
        }

        try {
            os.write(null, 0, 1);
            fail();
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (NullPointerException expected) {
            // Any of these exceptions are possible.
        }

        os.close();

        try {
            os.write(1);
            fail();
        } catch (IllegalBlockingModeException expected) {
        }

        try {
            os.write(null);
            fail();
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (NullPointerException expected) {
            // Any of these exceptions are possible.
        }

        try {
            os.write(buf, -1, 1);
            fail();
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (IndexOutOfBoundsException expected) {
            // Any of these exceptions are possible.
        }

        try {
            os.write(buf, 0, -1);
            fail();
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (IndexOutOfBoundsException expected) {
            // Any of these exceptions are possible.
        }

        try {
            os.write(buf, 0, 2);
            fail();
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (IndexOutOfBoundsException expected) {
            // Any of these exceptions are possible.
        }

        try {
            os.write(buf, 2, 0);
            fail();
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (IndexOutOfBoundsException expected) {
            // Any of these exceptions are possible.
        }

        try {
            os.write(null, 0, 0);
            fail();
        } catch (IllegalBlockingModeException expected) {
            // Any of these exceptions are possible.
        } catch (NullPointerException expected) {
            // Any of these exceptions are possible.
        }
    }

    public void test_socket_getOutputStream_blocking_write_Exception() throws IOException {
        byte[] buf = new byte[1];
        channel1.connect(this.localAddr1);
        OutputStream os = channel1.socket().getOutputStream();
        try {
            os.write(null);
            fail();
        } catch (NullPointerException expected) {
        }
        try {
            os.write(buf, -1, 1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            os.write(buf, 0, -1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            os.write(buf, 0, 2);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            os.write(buf, 2, 0);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            os.write(null, 0, 0);
            fail();
        } catch (NullPointerException expected) {
        }

        os.close();

        try {
            os.write(null);
            fail();
        } catch (NullPointerException expected) {
        }
        try {
            os.write(buf, -1, 1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            os.write(buf, 0, -1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            os.write(buf, 0, 2);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            os.write(buf, 2, 0);
            fail();
        } catch (IndexOutOfBoundsException expected) {
        }
        try {
            os.write(null, 0, 0);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * @tests SocketChannelImpl#socket().getOutputStream().write(int)
     */
    public void test_socket_getOutputStream_write_oneByte()
            throws IOException {

        // Regression test for Harmony-3475

        int MAGIC = 123;

        channel1.connect(this.localAddr1);

        OutputStream os = channel1.socket().getOutputStream();

        Socket acceptedSocket = server1.accept();

        InputStream in = acceptedSocket.getInputStream();

        os.write(MAGIC);
        channel1.close();

        int lastByte =  in.read();
        if (lastByte == -1) {
            fail("Server received nothing. Expected 1 byte.");
        } else if (lastByte != MAGIC) {
            fail("Server received wrong single byte: " + lastByte +
                 ", expected: " + MAGIC);
        }

        lastByte = in.read();
        if (lastByte != -1) {
            fail("Server received too long sequence. Expected 1 byte.");
        }
    }

    public void testSocket_setOptions() throws IOException {
        channel1.connect(localAddr1);
        Socket socket = channel1.socket();

        ByteBuffer buffer = ByteBuffer.wrap(new byte[] {1, 2, 3});
        socket.setKeepAlive(true);
        channel1.write(buffer);

        socket.setOOBInline(true);
        channel1.write(buffer);

        socket.setReceiveBufferSize(100);
        channel1.write(buffer);

        socket.setReuseAddress(true);
        channel1.write(buffer);

        socket.setSendBufferSize(100);
        channel1.write(buffer);

        socket.setSoLinger(true, 100);
        channel1.write(buffer);

        socket.setSoTimeout(1000);
        channel1.write(buffer);

        socket.setTcpNoDelay(true);
        channel1.write(buffer);

        socket.setTrafficClass(10);
        channel1.write(buffer);
    }

    class MockSocketChannel extends SocketChannel {

        private boolean isWriteCalled = false;

        private boolean isReadCalled = false;

        public MockSocketChannel(SelectorProvider provider) {
            super(provider);
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
        public long read(ByteBuffer[] targets, int offset, int length) throws IOException {
            // Verify that calling read(ByteBuffer[]) leads to the method
            // read(ByteBuffer[], int, int) being called with a 0 for the
            // second parameter and targets.length as the third parameter.
            if(0 == offset && length == targets.length){
                isReadCalled = true;
            }
            return 0;
        }

        @Override
        public int write(ByteBuffer source) throws IOException {
            return 0;
        }

        @Override
        public long write(ByteBuffer[] sources, int offset, int length) throws IOException {
            // Verify that calling write(ByteBuffer[]) leads to the method
            // write(ByteBuffer[], int, int) being called with a 0 for the
            // second parameter and sources.length as the third parameter.
            if(0 == offset && length == sources.length){
                isWriteCalled = true;
            }
            return 0;
        }

        @Override
        protected void implCloseSelectableChannel() throws IOException {
            // empty
        }

        @Override
        protected void implConfigureBlocking(boolean blockingMode) throws IOException {
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
}
