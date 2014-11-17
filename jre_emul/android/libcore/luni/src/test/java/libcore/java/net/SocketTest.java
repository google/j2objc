/*
 * Copyright (C) 2009 The Android Open Source Project
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

package libcore.java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketImpl;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class SocketTest extends junit.framework.TestCase {
    public void test_close() throws Exception {
        Socket s = new Socket();
        s.close();
        // Closing a closed socket does nothing.
        s.close();
    }

    /**
     * Our getLocalAddress and getLocalPort currently use getsockname(3).
     * This means they give incorrect results on closed sockets (as well
     * as requiring an unnecessary call into native code).
     */
    public void test_getLocalAddress_after_close() throws Exception {
        Socket s = new Socket();
        try {
            // Bind to an ephemeral local port.
            s.bind(new InetSocketAddress("localhost", 0));
            assertTrue(s.getLocalAddress().toString(), s.getLocalAddress().isLoopbackAddress());
            // What local port did we get?
            int localPort = s.getLocalPort();
            assertTrue(localPort > 0);
            // Now close the socket...
            s.close();
            // The RI returns the ANY address but the original local port after close.
            assertTrue(s.getLocalAddress().isAnyLocalAddress());
            assertEquals(localPort, s.getLocalPort());
        } finally {
            s.close();
        }
    }

    // http://code.google.com/p/android/issues/detail?id=7935
    public void test_newSocket_connection_refused() throws Exception {
        try {
            new Socket("localhost", 80);
            fail("connection should have been refused");
        } catch (ConnectException expected) {
        }
    }

    // http://code.google.com/p/android/issues/detail?id=3123
    // http://code.google.com/p/android/issues/detail?id=1933
    /* Fails on machines with IPv6 connections, both on JRE and j2objc.
    public void test_socketLocalAndRemoteAddresses() throws Exception {
        checkSocketLocalAndRemoteAddresses(false);
        checkSocketLocalAndRemoteAddresses(true);
    }
    */

    public void checkSocketLocalAndRemoteAddresses(boolean setOptions) throws Exception {
        InetAddress host = InetAddress.getLocalHost();

        // Open a local server port.
        ServerSocketChannel ssc = ServerSocketChannel.open();
        InetSocketAddress listenAddr = new InetSocketAddress(host, 0);
        ssc.bind(listenAddr, 0);
        ServerSocket ss = ssc.socket();

        // Open a socket to the local port.
        SocketChannel out = SocketChannel.open();
        out.configureBlocking(false);
        if (setOptions) {
            out.socket().setTcpNoDelay(false);
        }
        InetSocketAddress addr = new InetSocketAddress(host, ssc.socket().getLocalPort());
        out.connect(addr);
        while (!out.finishConnect()) {
            Thread.sleep(1);
        }

        SocketChannel in = ssc.accept();
        if (setOptions) {
            in.socket().setTcpNoDelay(false);
        }

        InetSocketAddress listenAddress = (InetSocketAddress) in.getLocalAddress();
        InetSocketAddress outRemoteAddress = (InetSocketAddress) out.socket().getRemoteSocketAddress();
        InetSocketAddress outLocalAddress = (InetSocketAddress) out.socket().getLocalSocketAddress();
        InetSocketAddress inLocalAddress = (InetSocketAddress) in.socket().getLocalSocketAddress();
        InetSocketAddress inRemoteAddress = (InetSocketAddress) in.socket().getRemoteSocketAddress();
//        System.err.println("listenAddress: " + listenAddr);
//        System.err.println("inLocalAddress: " + inLocalAddress);
//        System.err.println("inRemoteAddress: " + inRemoteAddress);
//        System.err.println("outLocalAddress: " + outLocalAddress);
//        System.err.println("outRemoteAddress: " + outRemoteAddress);

        assertFalse(ssc.socket().isClosed());
        assertTrue(ssc.socket().isBound());
        assertTrue(in.isConnected());
        assertTrue(in.socket().isConnected());
        assertTrue(out.socket().isConnected());
        assertTrue(out.isConnected());

        in.close();
        out.close();
        ssc.close();

        assertTrue(ssc.socket().isClosed());
        assertTrue(ssc.socket().isBound());
        assertFalse(in.isConnected());
        assertFalse(in.socket().isConnected());
        assertFalse(out.socket().isConnected());
        assertFalse(out.isConnected());

        assertNull(in.socket().getRemoteSocketAddress());
        assertNull(out.socket().getRemoteSocketAddress());

        // As per docs and RI - server socket local address methods continue to return the bind()
        // addresses even after close().
        assertEquals(listenAddress, ssc.socket().getLocalSocketAddress());

        // As per docs and RI - socket local address methods return the wildcard address before
        // bind() and after close(), but the port will be the same as it was before close().
        InetSocketAddress inLocalAddressAfterClose =
                (InetSocketAddress) in.socket().getLocalSocketAddress();
        assertTrue(inLocalAddressAfterClose.getAddress().isAnyLocalAddress());
        assertEquals(inLocalAddress.getPort(), inLocalAddressAfterClose.getPort());

        InetSocketAddress outLocalAddressAfterClose =
                (InetSocketAddress) out.socket().getLocalSocketAddress();
        assertTrue(outLocalAddressAfterClose.getAddress().isAnyLocalAddress());
        assertEquals(outLocalAddress.getPort(), outLocalAddressAfterClose.getPort());
    }

    // SocketOptions.setOption has weird behavior for setSoLinger/SO_LINGER.
    // This test ensures we do what the RI does.
    public void test_SocketOptions_setOption() throws Exception {
        class MySocketImpl extends SocketImpl {
            public int option;
            public Object value;

            public boolean createCalled;
            public boolean createStream;

            public MySocketImpl() { super(); }
            @Override protected void accept(SocketImpl arg0) throws IOException { }
            @Override protected int available() throws IOException { return 0; }
            @Override protected void bind(InetAddress arg0, int arg1) throws IOException { }
            @Override protected void close() throws IOException { }
            @Override protected void connect(String arg0, int arg1) throws IOException { }
            @Override protected void connect(InetAddress arg0, int arg1) throws IOException { }
            @Override protected void connect(SocketAddress arg0, int arg1) throws IOException { }
            @Override protected InputStream getInputStream() throws IOException { return null; }
            @Override protected OutputStream getOutputStream() throws IOException { return null; }
            @Override protected void listen(int arg0) throws IOException { }
            @Override protected void sendUrgentData(int arg0) throws IOException { }
            public Object getOption(int arg0) throws SocketException { return null; }

            @Override protected void create(boolean isStream) throws IOException {
                this.createCalled = true;
                this.createStream = isStream;
            }

            public void setOption(int option, Object value) throws SocketException {
                this.option = option;
                this.value = value;
            }
        }

        class MySocket extends Socket {
            public MySocket(SocketImpl impl) throws SocketException {
                super(impl);
            }
        }

        MySocketImpl impl = new MySocketImpl();
        Socket s = new MySocket(impl);

        // Check that, as per the SocketOptions.setOption documentation, we pass false rather
        // than -1 to the SocketImpl when setSoLinger is called with the first argument false.
        s.setSoLinger(false, -1);
        assertEquals(Boolean.FALSE, (Boolean) impl.value);
        // We also check that SocketImpl.create was called. SocketChannelImpl.SocketAdapter
        // subclasses Socket, and whether or not to call SocketImpl.create is the main behavioral
        // difference.
        assertEquals(true, impl.createCalled);
        s.setSoLinger(false, 0);
        assertEquals(Boolean.FALSE, (Boolean) impl.value);
        s.setSoLinger(false, 1);
        assertEquals(Boolean.FALSE, (Boolean) impl.value);

        // Check that otherwise, we pass down an Integer.
        s.setSoLinger(true, 0);
        assertEquals(Integer.valueOf(0), (Integer) impl.value);
        s.setSoLinger(true, 1);
        assertEquals(Integer.valueOf(1), (Integer) impl.value);
    }

    public void testReadAfterClose() throws Exception {
        MockServer server = new MockServer();
        server.enqueue(new byte[]{5, 3}, 0);
        Socket socket = new Socket("localhost", server.port);
        InputStream in = socket.getInputStream();
        assertEquals(5, in.read());
        assertEquals(3, in.read());
        assertEquals(-1, in.read());
        assertEquals(-1, in.read());
        socket.close();
        in.close();

        /*
         * Rather astonishingly, read() doesn't throw even though the stream is
         * closed. This is consistent with the RI's behavior.
         */
        assertEquals(-1, in.read());
        assertEquals(-1, in.read());

        server.shutdown();
    }

    public void testWriteAfterClose() throws Exception {
        MockServer server = new MockServer();
        server.enqueue(new byte[0], 3);
        Socket socket = new Socket("localhost", server.port);
        OutputStream out = socket.getOutputStream();
        out.write(5);
        out.write(3);
        socket.close();
        out.close();

        try {
            out.write(9);
            fail();
        } catch (IOException expected) {
        }

        server.shutdown();
    }

    public void testAvailable() throws Exception {
        for (int i = 0; i < 100; i++) {
            assertAvailableReturnsZeroAfterSocketReadsAllData();
            Logger.getAnonymousLogger().finest("Success on rep " + i);
        }
    }

    private void assertAvailableReturnsZeroAfterSocketReadsAllData() throws Exception {
        final byte[] data = "foo".getBytes();
        final ServerSocket serverSocket = new ServerSocket(0);

        new Thread() {
            @Override public void run() {
                try {
                    Socket socket = serverSocket.accept();
                    socket.getOutputStream().write(data);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        Socket socket = new Socket("localhost", serverSocket.getLocalPort());
        byte[] readBuffer = new byte[128];
        InputStream in = socket.getInputStream();
        int total = 0;
        // to prevent available() from cheating after EOF, stop reading before -1 is returned
        while (total < data.length) {
            total += in.read(readBuffer);
        }
        assertEquals(0, in.available());

        socket.close();
        serverSocket.close();
    }

    public void testInitialState() throws Exception {
        Socket s = new Socket();
        try {
            assertFalse(s.isBound());
            assertFalse(s.isClosed());
            assertFalse(s.isConnected());
            assertEquals(-1, s.getLocalPort());
            assertTrue(s.getLocalAddress().isAnyLocalAddress());
            assertNull(s.getLocalSocketAddress());
            assertNull(s.getInetAddress());
            assertEquals(0, s.getPort());
            assertNull(s.getRemoteSocketAddress());
            assertFalse(s.getReuseAddress());
            assertNull(s.getChannel());
        } finally {
            s.close();
        }
    }

    /* Fails on machines with IPv6 connections, both on JRE and j2objc.
    public void testStateAfterClose() throws Exception {
        Socket s = new Socket();
        s.bind(new InetSocketAddress(Inet4Address.getLocalHost(), 0));
        InetSocketAddress boundAddress = (InetSocketAddress) s.getLocalSocketAddress();
        s.close();

        assertTrue(s.isBound());
        assertTrue(s.isClosed());
        assertFalse(s.isConnected());
        assertTrue(s.getLocalAddress().isAnyLocalAddress());
        assertEquals(boundAddress.getPort(), s.getLocalPort());

        InetSocketAddress localAddressAfterClose = (InetSocketAddress) s.getLocalSocketAddress();
        assertTrue(localAddressAfterClose.getAddress().isAnyLocalAddress());
        assertEquals(boundAddress.getPort(), localAddressAfterClose.getPort());
    }
    */

    static class MockServer {
        private ExecutorService executor;
        private ServerSocket serverSocket;
        private int port = -1;

        MockServer() throws IOException {
            executor = Executors.newCachedThreadPool();
            serverSocket = new ServerSocket(0);
            serverSocket.setReuseAddress(true);
            port = serverSocket.getLocalPort();
        }

        public Future<byte[]> enqueue(final byte[] sendBytes, final int receiveByteCount)
                throws IOException {
            return executor.submit(new Callable<byte[]>() {
                @Override public byte[] call() throws Exception {
                    Socket socket = serverSocket.accept();
                    OutputStream out = socket.getOutputStream();
                    out.write(sendBytes);

                    InputStream in = socket.getInputStream();
                    byte[] result = new byte[receiveByteCount];
                    int total = 0;
                    while (total < receiveByteCount) {
                        total += in.read(result, total, result.length - total);
                    }
                    socket.close();
                    return result;
                }
            });
        }

        public void shutdown() throws IOException {
            serverSocket.close();
            executor.shutdown();
        }
    }
}
