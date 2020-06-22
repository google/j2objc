/*
 * Copyright (C) 2010 The Android Open Source Project
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

package libcore.java.nio.channels;

import junit.framework.TestCase;
import org.junit.Rule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.BindException;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketImpl;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
/* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
import libcore.junit.junit3.TestCaseWithRules;
import libcore.junit.util.ResourceLeakageDetector;
import libcore.junit.util.ResourceLeakageDetector.LeakageDetectorRule;
 */

public class SocketChannelTest extends TestCase {

    /* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
    @Rule
    public LeakageDetectorRule guardRule = ResourceLeakageDetector.getRule();
     */

    public void test_read_intoReadOnlyByteArrays() throws Exception {
        try (ServerSocket ss = new ServerSocket(0);
             SocketChannel sc = SocketChannel.open(ss.getLocalSocketAddress())) {
            ByteBuffer readOnly = ByteBuffer.allocate(1).asReadOnlyBuffer();
            ss.setReuseAddress(true);
            try {
                sc.read(readOnly);
                fail();
            } catch (IllegalArgumentException expected) {
            }
            try {
                sc.read(new ByteBuffer[] { readOnly });
                fail();
            } catch (IllegalArgumentException expected) {
            }
            try {
                sc.read(new ByteBuffer[] { readOnly }, 0, 1);
                fail();
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    // https://code.google.com/p/android/issues/detail?id=56684
    public void test_56684() throws Exception {
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);

        Selector selector = Selector.open();
        SelectionKey selectionKey = sc.register(selector, SelectionKey.OP_CONNECT);

        try {
            // This test originally mocked the connect syscall to return ENETUNREACH.
            // This is not easily doable in openJdk libcore, but a connect to broadcast
            // address (255.255.255.255) for a TCP connection produces ENETUNREACH
            // Kernel code that does it is at
            // http://lxr.free-electrons.com/source/net/ipv4/tcp_ipv4.c?v=3.18#L182
            sc.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[] {
                    (byte) 255, (byte) 255, (byte) 255, (byte) 255 }), 0));
            fail();
        } catch (ConnectException ex) {
        } catch (BindException ex) {
        }

        try {
            sc.finishConnect();
            fail();
        } catch (ClosedChannelException expected) {
        }
    }

    /** Checks that closing a Socket's output stream also closes the Socket and SocketChannel. */
    public void test_channelSocketOutputStreamClosureState() throws Exception {
        ServerSocket ss = new ServerSocket(0);

        SocketChannel sc = SocketChannel.open(ss.getLocalSocketAddress());
        sc.configureBlocking(true);

        Socket scSocket = sc.socket();
        OutputStream os = scSocket.getOutputStream();

        assertTrue(sc.isOpen());
        assertFalse(scSocket.isClosed());

        os.close();

        assertFalse(sc.isOpen());
        assertTrue(scSocket.isClosed());

        ss.close();
    }

    /** Checks that closing a Socket's input stream also closes the Socket and SocketChannel. */
    public void test_channelSocketInputStreamClosureState() throws Exception {
        ServerSocket ss = new ServerSocket(0);

        SocketChannel sc = SocketChannel.open(ss.getLocalSocketAddress());
        sc.configureBlocking(true);

        Socket scSocket = sc.socket();
        InputStream is = scSocket.getInputStream();

        assertTrue(sc.isOpen());
        assertFalse(scSocket.isClosed());

        is.close();

        assertFalse(sc.isOpen());
        assertTrue(scSocket.isClosed());

        ss.close();
    }

    /** Checks the state of the SocketChannel and associated Socket after open() */
    public void test_open_initialState() throws Exception {
        SocketChannel sc = SocketChannel.open();
        try {
            assertNull(sc.socket().getLocalSocketAddress());

            Socket socket = sc.socket();
            assertFalse(socket.isBound());
            assertFalse(socket.isClosed());
            assertFalse(socket.isConnected());
            assertEquals(-1, socket.getLocalPort());
            assertTrue(socket.getLocalAddress().isAnyLocalAddress());
            assertNull(socket.getLocalSocketAddress());
            assertNull(socket.getInetAddress());
            assertEquals(0, socket.getPort());
            assertNull(socket.getRemoteSocketAddress());
            assertFalse(socket.getReuseAddress());

            assertSame(sc, socket.getChannel());
        } finally {
            sc.close();
        }
    }

    public void test_bind_unresolvedAddress() throws IOException {
        SocketChannel sc = SocketChannel.open();
        try {
            sc.socket().bind(new InetSocketAddress("unresolvedname", 31415));
            fail();
        } catch (IOException expected) {
        }

        assertNull(sc.socket().getLocalSocketAddress());
        assertTrue(sc.isOpen());
        assertFalse(sc.isConnected());

        sc.close();
    }

    /** Checks that the SocketChannel and associated Socket agree on the socket state. */
    public void test_bind_socketStateSync() throws IOException {
        SocketChannel sc = SocketChannel.open();
        assertNull(sc.socket().getLocalSocketAddress());

        Socket socket = sc.socket();
        assertNull(socket.getLocalSocketAddress());
        assertFalse(socket.isBound());

        InetSocketAddress bindAddr = new InetSocketAddress("localhost", 0);
        sc.socket().bind(bindAddr);

        InetSocketAddress actualAddr = (InetSocketAddress) sc.socket().getLocalSocketAddress();
        assertEquals(actualAddr, socket.getLocalSocketAddress());
        assertEquals(bindAddr.getHostName(), actualAddr.getHostName());
        assertTrue(socket.isBound());
        assertFalse(socket.isConnected());
        assertFalse(socket.isClosed());

        sc.close();

        assertFalse(sc.isOpen());
        assertTrue(socket.isClosed());
    }

    /**
     * Checks that the SocketChannel and associated Socket agree on the socket state, even if
     * the Socket object is requested/created after bind().
     */
    public void test_bind_socketObjectCreationAfterBind() throws IOException {
        SocketChannel sc = SocketChannel.open();
        assertNull(sc.socket().getLocalSocketAddress());

        InetSocketAddress bindAddr = new InetSocketAddress("localhost", 0);
        sc.socket().bind(bindAddr);

        // Socket object creation after bind().
        Socket socket = sc.socket();
        InetSocketAddress actualAddr = (InetSocketAddress) sc.socket().getLocalSocketAddress();
        assertEquals(actualAddr, socket.getLocalSocketAddress());
        assertEquals(bindAddr.getHostName(), actualAddr.getHostName());
        assertTrue(socket.isBound());
        assertFalse(socket.isConnected());
        assertFalse(socket.isClosed());

        sc.close();

        assertFalse(sc.isOpen());
        assertTrue(socket.isClosed());
    }

    /**
     * Tests connect() and object state for a blocking SocketChannel. Blocking mode is the default.
     */
    public void test_connect_blocking() throws Exception {
        ServerSocket ss = new ServerSocket(0);

        SocketChannel sc = SocketChannel.open();
        assertTrue(sc.isBlocking());

        assertTrue(sc.connect(ss.getLocalSocketAddress()));

        assertTrue(sc.socket().isBound());
        assertTrue(sc.isConnected());
        assertTrue(sc.socket().isConnected());
        assertFalse(sc.socket().isClosed());
        assertTrue(sc.isBlocking());

        ss.close();
        sc.close();
    }

    /** Tests connect() and object state for a non-blocking SocketChannel. */
    public void test_connect_nonBlocking() throws Exception {
        ServerSocket ss = new ServerSocket(0);

        SocketChannel sc = SocketChannel.open();
        assertTrue(sc.isBlocking());
        sc.configureBlocking(false);
        assertFalse(sc.isBlocking());

        if (!sc.connect(ss.getLocalSocketAddress())) {
            do {
                assertTrue(sc.socket().isBound());
                assertFalse(sc.isConnected());
                assertFalse(sc.socket().isConnected());
                assertFalse(sc.socket().isClosed());
            } while (!sc.finishConnect());
        }
        assertTrue(sc.socket().isBound());
        assertTrue(sc.isConnected());
        assertTrue(sc.socket().isConnected());
        assertFalse(sc.socket().isClosed());
        assertFalse(sc.isBlocking());

        ss.close();
        sc.close();
    }

    public void test_Socket_impl_notNull() throws Exception {
        try (SocketChannel sc = SocketChannel.open();
             Socket socket = sc.socket()) {
            Field f_impl = Socket.class.getDeclaredField("impl");
            f_impl.setAccessible(true);
            Object implFieldValue = f_impl.get(socket);
            assertNotNull(implFieldValue);
            assertTrue(implFieldValue instanceof SocketImpl);
        }
    }

    public void test_setOption() throws Exception {
        SocketChannel sc = SocketChannel.open();
        sc.setOption(StandardSocketOptions.SO_LINGER, 1000);

        // Assert that we can read back the option from the channel...
        assertEquals(1000, (int) sc.<Integer>getOption(StandardSocketOptions.SO_LINGER));
        // ... and its socket adaptor.
        assertEquals(1000, sc.socket().getSoLinger());

        sc.close();
        try {
            sc.setOption(StandardSocketOptions.SO_LINGER, 2000);
            fail();
        } catch (ClosedChannelException expected) {
        }
    }

    public void test_bind() throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(Inet4Address.LOOPBACK, 0);
        SocketChannel sc = SocketChannel.open();
        sc.bind(socketAddress);
        assertEquals(socketAddress.getAddress(),
                ((InetSocketAddress) (sc.getLocalAddress())).getAddress());
        assertTrue(((InetSocketAddress) (sc.getLocalAddress())).getPort() > 0);

        try {
            sc.bind(socketAddress);
            fail();
        } catch (AlreadyBoundException expected) {
        }

        socketAddress = new InetSocketAddress(Inet4Address.LOOPBACK,
                ((InetSocketAddress) (sc.getLocalAddress())).getPort());
        try (SocketChannel sc1 = SocketChannel.open()){
            sc1.bind(socketAddress);
            fail();
        } catch (BindException expected) {
        }

        sc.close();
        socketAddress = new InetSocketAddress(Inet4Address.LOOPBACK, 0);
        try {
            sc.bind(socketAddress);
            fail();
        } catch (ClosedChannelException expected) {
        }
    }

    public void test_getRemoteAddress() throws IOException {
        try (SocketChannel sc = SocketChannel.open();
             ServerSocket ss = new ServerSocket(0)) {
            assertNull(sc.getRemoteAddress());

            sc.connect(ss.getLocalSocketAddress());
            assertEquals(sc.getRemoteAddress(), ss.getLocalSocketAddress());
        }
    }

    public void test_shutdownInput() throws IOException {
        try (SocketChannel channel1 = SocketChannel.open();
            ServerSocket server1 = new ServerSocket(0)) {
            InetSocketAddress localAddr1 = new InetSocketAddress("127.0.0.1",
                    server1.getLocalPort());

            // initialize write content
            byte[] writeContent = new byte[10];
            for (int i = 0; i < writeContent.length; i++) {
                writeContent[i] = (byte) i;
            }

            // establish connection
            channel1.connect(localAddr1);
            Socket acceptedSocket = server1.accept();
            // use OutputStream.write to write bytes data.
            OutputStream out = acceptedSocket.getOutputStream();
            out.write(writeContent);
            // use close to guarantee all data is sent
            acceptedSocket.close();

            channel1.configureBlocking(false);
            ByteBuffer readContent = ByteBuffer.allocate(10 + 1);
            channel1.shutdownInput();
            assertEquals(-1, channel1.read(readContent));
        }
    }

    public void test_shutdownOutput() throws IOException {
        try (SocketChannel channel1 = SocketChannel.open();
            ServerSocket server1 = new ServerSocket(0)) {
            InetSocketAddress localAddr1 = new InetSocketAddress(
                "127.0.0.1", server1.getLocalPort());

            // initialize write content
            ByteBuffer writeContent = ByteBuffer.allocate(10);
            for (int i = 0; i < 10; i++) {
                writeContent.put((byte) i);
            }
            writeContent.flip();

            try {
                channel1.shutdownOutput();
                fail();
            } catch (NotYetConnectedException expected) {
            }

            // establish connection
            channel1.connect(localAddr1);
            channel1.shutdownOutput();

            try {
                channel1.write(writeContent);
                fail();
            } catch (ClosedChannelException expected) {
            }

            // Closing the channel early to verify that is CloseChannelException thrown by
            // #shutdownOutput.
            channel1.close();

            try {
                channel1.shutdownOutput();
                fail();
            } catch (ClosedChannelException expected) {
            }
        }
    }
}
