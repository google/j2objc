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
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ProtocolFamily;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.channels.spi.SelectorProvider;
import java.util.Enumeration;
/* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
import libcore.junit.junit3.TestCaseWithRules;
import libcore.junit.util.ResourceLeakageDetector;
 */

public class DatagramChannelTest extends TestCase {
    /* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
    @Rule
    public ResourceLeakageDetector.LeakageDetectorRule guardRule =
            ResourceLeakageDetector.getRule();
     */

    public void test_read_intoReadOnlyByteArrays() throws Exception {
        ByteBuffer readOnly = ByteBuffer.allocate(1).asReadOnlyBuffer();
        try (DatagramSocket ds = new DatagramSocket(0);
             DatagramChannel dc = DatagramChannel.open()) {
            dc.connect(ds.getLocalSocketAddress());
            try {
                dc.read(readOnly);
                fail();
            } catch (IllegalArgumentException expected) {
            }
            try {
                dc.read(new ByteBuffer[] { readOnly });
                fail();
            } catch (IllegalArgumentException expected) {
            }
            try {
                dc.read(new ByteBuffer[] { readOnly }, 0, 1);
                fail();
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    // http://code.google.com/p/android/issues/detail?id=16579
    public void testNonBlockingRecv() throws Exception {
        DatagramChannel dc = DatagramChannel.open();
        try {
            dc.configureBlocking(false);
            dc.socket().bind(null);
            // Should return immediately, since we're non-blocking.
            assertNull(dc.receive(ByteBuffer.allocate(2048)));
        } finally {
            dc.close();
        }
    }

    public void testInitialState() throws Exception {
        DatagramChannel dc = DatagramChannel.open();
        try {
            DatagramSocket socket = dc.socket();
            assertFalse(socket.isBound());
            assertFalse(socket.getBroadcast());
            assertFalse(socket.isClosed());
            assertFalse(socket.isConnected());
            assertEquals(0, socket.getLocalPort());
            assertTrue(socket.getLocalAddress().isAnyLocalAddress());
            assertNull(socket.getLocalSocketAddress());
            assertNull(socket.getInetAddress());
            assertEquals(-1, socket.getPort());
            assertNull(socket.getRemoteSocketAddress());
            assertFalse(socket.getReuseAddress());

            assertSame(dc, socket.getChannel());
        } finally {
            dc.close();
        }
    }

    public void test_bind_unresolvedAddress() throws IOException {
        DatagramChannel dc = DatagramChannel.open();
        try {
            dc.socket().bind(new InetSocketAddress("unresolvedname", 31415));
            fail();
        } catch (IOException expected) {
        }

        assertTrue(dc.isOpen());
        assertFalse(dc.isConnected());

        dc.close();
    }

    public void test_bind_any_IPv4() throws Exception {
        test_bind_any(InetAddress.getByName("0.0.0.0"));
    }

    public void test_bind_any_IPv6() throws Exception {
        test_bind_any(InetAddress.getByName("::"));
    }

    private void test_bind_any(InetAddress bindAddress) throws Exception {
        DatagramChannel dc = DatagramChannel.open();
        dc.socket().bind(new InetSocketAddress(bindAddress, 0));

        assertTrue(dc.isOpen());
        assertFalse(dc.isConnected());

        InetSocketAddress actualAddress = (InetSocketAddress) dc.socket().getLocalSocketAddress();
        assertTrue(actualAddress.getAddress().isAnyLocalAddress());
        assertTrue(actualAddress.getPort() > 0);

        dc.close();
    }

    public void test_bind_loopback_IPv4() throws Exception {
        test_bind(InetAddress.getByName("127.0.0.1"));
    }

    public void test_bind_loopback_IPv6() throws Exception {
        test_bind(InetAddress.getByName("::1"));
    }

    public void test_bind_IPv4() throws Exception {
        InetAddress bindAddress = getNonLoopbackNetworkInterfaceAddress(true /* ipv4 */);
        test_bind(bindAddress);
    }

    public void test_bind_IPv6() throws Exception {
        InetAddress bindAddress = getNonLoopbackNetworkInterfaceAddress(false /* ipv4 */);
        test_bind(bindAddress);
    }

    private void test_bind(InetAddress bindAddress) throws IOException {
        try (DatagramChannel dc = DatagramChannel.open()) {
            dc.socket().bind(new InetSocketAddress(bindAddress, 0));

            InetSocketAddress actualAddress = (InetSocketAddress) dc.socket().getLocalSocketAddress();
            assertEquals(bindAddress, actualAddress.getAddress());
            assertTrue(actualAddress.getPort() > 0);
        }
    }

    public void test_setOption() throws Exception {
        DatagramChannel dc = DatagramChannel.open();
        // There were problems in the past as the number used here was below the minimum for
        // some platforms (b/27821554). It was increased from 1024 to 4096.
        dc.setOption(StandardSocketOptions.SO_SNDBUF, 4096);

        // Assert that we can read back the option from the channel...
        assertEquals(4096, (int) dc.getOption(StandardSocketOptions.SO_SNDBUF));
        // ... and its socket adaptor.
        assertEquals(4096, dc.socket().getSendBufferSize());

        dc.close();
        try {
            dc.setOption(StandardSocketOptions.SO_SNDBUF, 4096);
            fail();
        } catch (ClosedChannelException expected) {
        }
    }

    // http://b/26292854
    public void test_getFileDescriptor() throws Exception {
        try (DatagramSocket socket = DatagramChannel.open().socket()) {
            socket.getReuseAddress();
            assertNotNull(socket.getFileDescriptor$());
        }
    }

    public void test_bind() throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(Inet4Address.LOOPBACK, 0);
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(socketAddress);
        assertEquals(socketAddress.getAddress(),
                ((InetSocketAddress)(channel.getLocalAddress())).getAddress());
        assertTrue(((InetSocketAddress)(channel.getLocalAddress())).getPort() > 0);

        try {
            channel.bind(socketAddress);
            fail();
        } catch (AlreadyBoundException expected) {
        }

        socketAddress = new InetSocketAddress(Inet4Address.LOOPBACK,
                ((InetSocketAddress)(channel.getLocalAddress())).getPort());
        try (DatagramChannel dc = DatagramChannel.open()){
            dc.bind(socketAddress);
            fail();
        } catch (BindException expected) {}

        channel.close();
        socketAddress = new InetSocketAddress(Inet4Address.LOOPBACK, 0);
        try {
            channel.bind(socketAddress);
            fail();
        } catch (ClosedChannelException expected) {}
    }

    public void test_getRemoteAddress() throws IOException {
        InetSocketAddress socketAddress = new InetSocketAddress(Inet4Address.LOOPBACK, 0);
        try (DatagramChannel clientChannel = DatagramChannel.open();
            DatagramChannel serverChannel = DatagramChannel.open()) {
            serverChannel.bind(socketAddress);

            assertNull(clientChannel.getRemoteAddress());

            clientChannel.connect(serverChannel.getLocalAddress());
            assertEquals(socketAddress.getAddress(),
                    ((InetSocketAddress) (clientChannel.getRemoteAddress())).getAddress());
            assertEquals(((InetSocketAddress) (serverChannel.getLocalAddress())).getPort(),
                    ((InetSocketAddress) (clientChannel.getRemoteAddress())).getPort());
        }
    }

    public void test_open$java_net_ProtocolFamily() throws IOException {
        try (DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET)) {
            channel.bind(new InetSocketAddress(Inet4Address.LOOPBACK, 0));
            assertEquals(SelectorProvider.provider(), channel.provider());
        }

        // Should not support IPv6 Address
        // InetSocketAddress(int) returns IPv6 ANY  address
        try (DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)) {
            dc.bind(new InetSocketAddress(0));
            fail();
        } catch (UnsupportedAddressTypeException expected) {}

        try (DatagramChannel dc = DatagramChannel.open(MockProtocolFamily.MOCK)) {
            fail();
        } catch (UnsupportedOperationException expected) {}

        try (DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET6)) {
            assertSame(dc, dc.bind(new InetSocketAddress(0)));
        }

        // NullPointerException
        try (DatagramChannel dc = DatagramChannel.open(null)) {
            fail();
        } catch (NullPointerException expected) {}
    }

    /* J2ObjC removed: not supported by Junit 4.11 (https://github.com/google/j2objc/issues/1318).
    public void test_closeGuardSupport() throws IOException {
        try(DatagramChannel dc = DatagramChannel.open(StandardProtocolFamily.INET)) {
            guardRule.assertUnreleasedResourceCount(dc, 1);
        }
    }
     */

    private static InetAddress getNonLoopbackNetworkInterfaceAddress(boolean ipv4) throws IOException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                if ( (ipv4 && inetAddress instanceof Inet4Address)
                        || (!ipv4 && inetAddress instanceof Inet6Address)) {
                    return inetAddress;
                }
            }
        }
        return null;
    }

    enum MockProtocolFamily implements ProtocolFamily {
        MOCK,
    }
}
