/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * limitations under the License
 */

package libcore.java.nio.channels;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import libcore.io.IoBridge;
import sun.nio.ch.Net;

import static libcore.io.OsConstants.POLLIN;

/**
 * Tests associated with multicast behavior of DatagramChannel.
 * These tests require IPv6 multicasting enabled network.
 */
public class DatagramChannelMulticastTest extends TestCase {

    private static InetAddress lookup(String s) {
        try {
            return InetAddress.getByName(s);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    // These IP addresses aren't inherently "good" or "bad"; they're just used like that.
    // We use the "good" addresses for our actual group, and the "bad" addresses are for
    // a group that we won't actually set up.
    private static final InetAddress GOOD_MULTICAST_IPv4 = lookup("239.255.0.1");
    private static final InetAddress BAD_MULTICAST_IPv4 = lookup("239.255.0.2");
    private static final InetAddress GOOD_MULTICAST_IPv6 = lookup("ff05::7:7");
    private static final InetAddress BAD_MULTICAST_IPv6 = lookup("ff05::7:8");

    // Special addresses.
    private static final InetAddress WILDCARD_IPv4 = lookup("0.0.0.0");
    private static final InetAddress WILDCARD_IPv6 = lookup("::");

    // Arbitrary unicast addresses. Used when the value doesn't actually matter. e.g. for source
    // filters.
    private static final InetAddress UNICAST_IPv4_1 = lookup("192.168.1.1");
    private static final InetAddress UNICAST_IPv4_2 = lookup("192.168.1.2");
    private static final InetAddress UNICAST_IPv6_1 = lookup("2001:db8::1");
    private static final InetAddress UNICAST_IPv6_2 = lookup("2001:db8::2");

    private NetworkInterface ipv4NetworkInterface;
    private NetworkInterface ipv6NetworkInterface;
    private NetworkInterface loopbackInterface;

    private boolean supportsMulticast;

    @Override
    protected void setUp() throws Exception {
        // The loopback interface isn't actually useful for sending/receiving multicast messages
        // but it can be used as a dummy for tests where that does not matter.
        loopbackInterface = NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress());
        assertNotNull(loopbackInterface);
        assertTrue(loopbackInterface.isLoopback());
        /* J2ObjC removed: iOS supports multicast
        assertFalse(loopbackInterface.supportsMulticast());
         */

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        // Determine if the device is marked to support multicast or not. If this propery is not
        // set we assume the device has an interface capable of supporting multicast.
        /* J2ObjC removed: iOS does not have property
        supportsMulticast = Boolean.parseBoolean(
                System.getProperty("android.cts.device.multicast", "true"));
        if (!supportsMulticast) {
            return;
        }
         */

        while (interfaces.hasMoreElements()
                && (ipv4NetworkInterface == null || ipv6NetworkInterface == null)) {
            NetworkInterface nextInterface = interfaces.nextElement();
            if (willWorkForMulticast(nextInterface)) {
                Enumeration<InetAddress> addresses = nextInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    final InetAddress nextAddress = addresses.nextElement();
                    if (nextAddress instanceof Inet6Address && ipv6NetworkInterface == null) {
                        ipv6NetworkInterface = nextInterface;
                    } else if (nextAddress instanceof Inet4Address
                            && ipv4NetworkInterface == null) {
                        ipv4NetworkInterface = nextInterface;
                    }
                }
            }
        }

        if (ipv4NetworkInterface == null) {
            fail("Test environment must have at least one network interface capable of IPv4"
                    + " multicast");
        }
        if (ipv6NetworkInterface == null) {
            fail("Test environment must have at least one network interface capable of IPv6"
                    + " multicast");
        }
    }

    public void test_open() throws IOException {
        DatagramChannel dc = DatagramChannel.open();

        // Unlike MulticastSocket, DatagramChannel has SO_REUSEADDR set to false by default.
        assertFalse(dc.getOption(StandardSocketOptions.SO_REUSEADDR));

        assertNull(dc.getLocalAddress());
        assertTrue(dc.isOpen());
        assertFalse(dc.isConnected());
    }

    public void test_bind_null() throws Exception {
        DatagramChannel dc = createReceiverChannel();
        assertNotNull(dc.getLocalAddress());
        assertTrue(dc.isOpen());
        assertFalse(dc.isConnected());

        dc.close();
        try {
            dc.getLocalAddress();
            fail();
        } catch (ClosedChannelException expected) {
        }
        assertFalse(dc.isOpen());
        assertFalse(dc.isConnected());
    }

    public void test_joinAnySource_afterClose() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        dc.close();
        try {
            dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface);
            fail();
        } catch (ClosedChannelException expected) {
        }
    }

    public void test_joinAnySource_nullGroupAddress() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        try {
            dc.join(null, ipv4NetworkInterface);
            fail();
        } catch (NullPointerException expected) {
        }
        dc.close();
    }

    public void test_joinAnySource_nullNetworkInterface() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        try {
            dc.join(GOOD_MULTICAST_IPv4, null);
            fail();
        } catch (NullPointerException expected) {
        }
        dc.close();
    }

    public void test_joinAnySource_nonMulticastGroupAddress_IPv4() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        try {
            dc.join(UNICAST_IPv4_1, ipv4NetworkInterface);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        dc.close();
    }

    public void test_joinAnySource_nonMulticastGroupAddress_IPv6() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        try {
            dc.join(UNICAST_IPv6_1, ipv6NetworkInterface);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        dc.close();
    }

    public void test_joinAnySource_IPv4() throws Exception {
        check_joinAnySource(GOOD_MULTICAST_IPv4, BAD_MULTICAST_IPv4, ipv4NetworkInterface);
    }

    public void test_joinAnySource_IPv6() throws Exception {
        check_joinAnySource(GOOD_MULTICAST_IPv6, BAD_MULTICAST_IPv6, ipv6NetworkInterface);
    }

    private void check_joinAnySource(InetAddress group, InetAddress group2,
            NetworkInterface networkInterface) throws Exception {
        if (!supportsMulticast) {
            return;
        }
        // Set up a receiver join the group on ipv4NetworkInterface
        DatagramChannel receiverChannel = createReceiverChannel();
        InetSocketAddress localAddress = (InetSocketAddress) receiverChannel.getLocalAddress();
        receiverChannel.join(group, networkInterface);

        String msg = "Hello World";
        createChannelAndSendMulticastMessage(group, localAddress.getPort(), msg, networkInterface);

        // now verify that we received the data as expected
        ByteBuffer recvBuffer = ByteBuffer.allocate(100);
        receiveExpectedDatagram(receiverChannel, recvBuffer);
        assertEquals(msg, new String(recvBuffer.array(), 0, recvBuffer.position()));

        // now verify that we didn't receive the second message
        String msg2 = "Hello World - Different Group";
        createChannelAndSendMulticastMessage(
                group2, localAddress.getPort(), msg2, networkInterface);
        recvBuffer.position(0);
        checkNoDatagramReceived(receiverChannel);

        receiverChannel.close();
    }

    public void test_joinAnySource_processLimit() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        for (byte i = 1; i <= 25; i++) {
            InetAddress groupAddress = Inet4Address.getByName("239.255.0." + i);
            try {
                dc.join(groupAddress, ipv4NetworkInterface);
            } catch (SocketException e) {
                // There is a limit, that's ok according to the RI docs. For this test a lower bound of 20
                // is used, which appears to be the default linux limit.
                // See /proc/sys/net/ipv4/igmp_max_memberships
                assertTrue(i > 20);
                break;
            }
        }

        dc.close();
    }

    public void test_joinAnySource_blockLimit() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        MembershipKey key = dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface);
        for (byte i = 1; i <= 15; i++) {
            InetAddress sourceAddress = Inet4Address.getByName("10.0.0." + i);
            try {
                key.block(sourceAddress);
            } catch (SocketException e) {
                // There is a limit, that's ok according to the RI docs. For this test a lower bound of 10
                // is used, which appears to be the default linux limit.
                // See /proc/sys/net/ipv4/igmp_max_msf
                assertTrue(i > 10);
                break;
            }
        }

        dc.close();
    }

    /** Confirms that calling join() does not cause an implicit bind() to take place. */
    public void test_joinAnySource_doesNotCauseBind() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = DatagramChannel.open();
        dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface);
        assertNull(dc.getLocalAddress());

        dc.close();
    }

    public void test_joinAnySource_networkInterfaces() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        // Check that we can join on specific interfaces and that we only receive if data is
        // received on that interface. This test is only really useful on devices with multiple
        // non-loopback interfaces.

        ArrayList<NetworkInterface> realInterfaces = new ArrayList<NetworkInterface>();
        Enumeration<NetworkInterface> theInterfaces = NetworkInterface.getNetworkInterfaces();
        while (theInterfaces.hasMoreElements()) {
            NetworkInterface thisInterface = theInterfaces.nextElement();
            if (thisInterface.getInetAddresses().hasMoreElements()) {
                realInterfaces.add(thisInterface);
            }
        }

        for (int i = 0; i < realInterfaces.size(); i++) {
            NetworkInterface thisInterface = realInterfaces.get(i);
            if (!thisInterface.supportsMulticast()) {
                // Skip interfaces that do not support multicast - there's no point in proving
                // they cannot send / receive multicast messages.
                continue;
            }

            // get the first address on the interface

            // start server which is joined to the group and has
            // only asked for packets on this interface
            Enumeration<InetAddress> addresses = thisInterface.getInetAddresses();

            NetworkInterface sendingInterface = null;
            InetAddress group = null;
            if (addresses.hasMoreElements()) {
                InetAddress firstAddress = addresses.nextElement();
                if (firstAddress instanceof Inet4Address) {
                    group = GOOD_MULTICAST_IPv4;
                    sendingInterface = ipv4NetworkInterface;
                } else {
                    // if this interface only seems to support IPV6 addresses
                    group = GOOD_MULTICAST_IPv6;
                    sendingInterface = ipv6NetworkInterface;
                }
            }

            DatagramChannel dc = createReceiverChannel();
            InetSocketAddress localAddress = (InetSocketAddress) dc.getLocalAddress();
            dc.join(group, thisInterface);

            // Now send out a package on sendingInterface. We should only see the packet if we send
            // it on the same interface we are listening on (thisInterface).
            String msg = "Hello World - Again" + thisInterface.getName();
            createChannelAndSendMulticastMessage(
                    group, localAddress.getPort(), msg, sendingInterface);

            ByteBuffer recvBuffer = ByteBuffer.allocate(100);
            if (thisInterface.equals(sendingInterface)) {
                receiveExpectedDatagram(dc, recvBuffer);
                assertEquals(msg, new String(recvBuffer.array(), 0, recvBuffer.position()));
            } else {
                checkNoDatagramReceived(dc);
            }
            dc.close();
        }
    }

    /** Confirms that the scope of each membership is network interface-level. */
    public void test_join_canMixTypesOnDifferentInterfaces() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = DatagramChannel.open();
        MembershipKey membershipKey1 = dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface);
        MembershipKey membershipKey2 = dc.join(GOOD_MULTICAST_IPv4, loopbackInterface, UNICAST_IPv4_1);
        assertNotSame(membershipKey1, membershipKey2);

        dc.close();
    }


    private DatagramChannel createReceiverChannel() throws Exception {
        DatagramChannel dc = DatagramChannel.open();
        dc.bind(null /* leave the OS to determine the port, and use the wildcard address */);
        configureChannelForReceiving(dc);
        return dc;
    }

    public void test_joinAnySource_multiple_joins_IPv4()
            throws Exception {
        check_joinAnySource_multiple_joins(GOOD_MULTICAST_IPv4, ipv4NetworkInterface);
    }

    public void test_joinAnySource_multiple_joins_IPv6()
            throws Exception {
        check_joinAnySource_multiple_joins(GOOD_MULTICAST_IPv6, ipv6NetworkInterface);
    }

    private void check_joinAnySource_multiple_joins(InetAddress group,
            NetworkInterface networkInterface) throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();

        MembershipKey membershipKey1 = dc.join(group, networkInterface);

        MembershipKey membershipKey2 = dc.join(group, loopbackInterface);
        assertFalse(membershipKey1.equals(membershipKey2));

        MembershipKey membershipKey1_2 = dc.join(group, networkInterface);
        assertEquals(membershipKey1, membershipKey1_2);

        dc.close();
    }

    public void test_joinAnySource_multicastLoopOption_IPv4() throws Exception {
        check_joinAnySource_multicastLoopOption(GOOD_MULTICAST_IPv4, ipv4NetworkInterface);
    }

    public void test_multicastLoopOption_IPv6() throws Exception {
        check_joinAnySource_multicastLoopOption(GOOD_MULTICAST_IPv6, ipv6NetworkInterface);
    }

    private void check_joinAnySource_multicastLoopOption(InetAddress group,
            NetworkInterface networkInterface) throws Exception {
        if (!supportsMulticast) {
            return;
        }
        final String message = "Hello, world!";

        DatagramChannel dc = createReceiverChannel();
        dc.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true /* enable loop */);
        dc.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
        configureChannelForReceiving(dc);
        dc.join(group, networkInterface);

        InetSocketAddress localAddress = (InetSocketAddress) dc.getLocalAddress();

        // send the datagram
        byte[] sendData = message.getBytes();
        ByteBuffer sendBuffer = ByteBuffer.wrap(sendData);
        dc.send(sendBuffer, new InetSocketAddress(group, localAddress.getPort()));

        // receive the datagram
        ByteBuffer recvBuffer = ByteBuffer.allocate(100);
        receiveExpectedDatagram(dc, recvBuffer);

        String recvMessage = new String(recvBuffer.array(), 0, recvBuffer.position());
        assertEquals(message, recvMessage);

        // Turn off loop
        dc.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, false /* enable loopback */);

        // send another datagram
        recvBuffer.position(0);
        ByteBuffer sendBuffer2 = ByteBuffer.wrap(sendData);
        dc.send(sendBuffer2, new InetSocketAddress(group, localAddress.getPort()));

        checkNoDatagramReceived(dc);

        dc.close();
    }

    public void testMembershipKeyAccessors_IPv4() throws Exception {
        checkMembershipKeyAccessors(GOOD_MULTICAST_IPv4, ipv4NetworkInterface);
    }

    public void testMembershipKeyAccessors_IPv6() throws Exception {
        checkMembershipKeyAccessors(GOOD_MULTICAST_IPv6, ipv6NetworkInterface);
    }

    private void checkMembershipKeyAccessors(InetAddress group,
            NetworkInterface networkInterface) throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();

        MembershipKey key = dc.join(group, networkInterface);
        assertSame(dc, key.channel());
        assertSame(group, key.group());
        assertTrue(key.isValid());
        assertSame(networkInterface, key.networkInterface());
        assertNull(key.sourceAddress());
        dc.close();
    }

    public void test_dropAnySource_twice_IPv4() throws Exception {
        check_dropAnySource_twice(GOOD_MULTICAST_IPv4, ipv4NetworkInterface);
    }

    public void test_dropAnySource_twice_IPv6() throws Exception {
        check_dropAnySource_twice(GOOD_MULTICAST_IPv6, ipv6NetworkInterface);
    }

    private void check_dropAnySource_twice(InetAddress group,
            NetworkInterface networkInterface) throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        MembershipKey membershipKey = dc.join(group, networkInterface);

        assertTrue(membershipKey.isValid());
        membershipKey.drop();
        assertFalse(membershipKey.isValid());

        // Try to leave a group we are no longer a member of - should do nothing.
        membershipKey.drop();

        dc.close();
    }

    public void test_close_invalidatesMembershipKey() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        MembershipKey membershipKey = dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface);

        assertTrue(membershipKey.isValid());

        dc.close();

        assertFalse(membershipKey.isValid());
    }

    public void test_block_null() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        MembershipKey membershipKey = dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface);
        try {
            membershipKey.block(null);
            fail();
        } catch (NullPointerException expected) {
        }

        dc.close();
    }

    public void test_block_mixedAddressTypes_IPv4() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        MembershipKey membershipKey = dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface);
        try {
            membershipKey.block(UNICAST_IPv6_1);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        dc.close();
    }

    public void test_block_mixedAddressTypes_IPv6() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        MembershipKey membershipKey = dc.join(GOOD_MULTICAST_IPv6, ipv6NetworkInterface);
        try {
            membershipKey.block(UNICAST_IPv4_1);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        dc.close();
    }

    public void test_block_cannotBlockWithSourceSpecificMembership() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        MembershipKey membershipKey = dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface, UNICAST_IPv4_1);
        try {
            membershipKey.block(UNICAST_IPv4_2);
            fail();
        } catch (IllegalStateException expected) {
        }

        dc.close();
    }

    public void test_block_multipleBlocksIgnored() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        MembershipKey membershipKey = dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface);
        membershipKey.block(UNICAST_IPv4_1);

        MembershipKey membershipKey2 = membershipKey.block(UNICAST_IPv4_1);
        assertSame(membershipKey2, membershipKey);

        dc.close();
    }

    public void test_block_wildcardAddress() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        MembershipKey membershipKey = dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface);
        try {
            membershipKey.block(WILDCARD_IPv4);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        dc.close();
    }

    public void test_unblock_multipleUnblocksFail() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        MembershipKey membershipKey = dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface);

        try {
            membershipKey.unblock(UNICAST_IPv4_1);
            fail();
        } catch (IllegalStateException expected) {
        }

        assertTrue(membershipKey.isValid());

        membershipKey.block(UNICAST_IPv4_1);
        membershipKey.unblock(UNICAST_IPv4_1);

        try {
            membershipKey.unblock(UNICAST_IPv4_1);
            fail();
        } catch (IllegalStateException expected) {
        }

        dc.close();
    }

    public void test_unblock_null() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        MembershipKey membershipKey = dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface);
        membershipKey.block(UNICAST_IPv4_1);

        try {
            membershipKey.unblock(null);
            fail();
        } catch (IllegalStateException expected) {
            // Either of these exceptions are fine
        } catch (NullPointerException expected) {
            // Either of these exception are fine
        }

        dc.close();
    }

    public void test_unblock_mixedAddressTypes_IPv4() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        MembershipKey membershipKey = dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface);
        try {
            membershipKey.unblock(UNICAST_IPv6_1);
            fail();
        } catch (IllegalStateException expected) {
            // Either of these exceptions are fine
        } catch (IllegalArgumentException expected) {
            // Either of these exceptions are fine
        }

        dc.close();
    }

    public void test_unblock_mixedAddressTypes_IPv6() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        MembershipKey membershipKey = dc.join(GOOD_MULTICAST_IPv6, ipv6NetworkInterface);
        try {
            membershipKey.unblock(UNICAST_IPv4_1);
            fail();
        } catch (IllegalStateException expected) {
            // Either of these exceptions are fine
        } catch (IllegalArgumentException expected) {
            // Either of these exceptions are fine
        }

        dc.close();
    }

    /** Checks that block() works when the receiver is bound to the multicast group address */
    public void test_block_filtersAsExpected_groupBind_ipv4() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        InetAddress ipv4LocalAddress = getLocalIpv4Address(ipv4NetworkInterface);
        check_block_filtersAsExpected(
                ipv4LocalAddress /* senderBindAddress */,
                GOOD_MULTICAST_IPv4 /* receiverBindAddress */,
                GOOD_MULTICAST_IPv4 /* groupAddress */,
                ipv4NetworkInterface);
    }

    /** Checks that block() works when the receiver is bound to the multicast group address */
    public void test_block_filtersAsExpected_groupBind_ipv6() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        InetAddress ipv6LocalAddress = getLocalIpv6Address(ipv6NetworkInterface);
        check_block_filtersAsExpected(
                ipv6LocalAddress /* senderBindAddress */,
                GOOD_MULTICAST_IPv6 /* receiverBindAddress */,
                GOOD_MULTICAST_IPv6 /* groupAddress */,
                ipv6NetworkInterface);
    }

    /** Checks that block() works when the receiver is bound to the "any" address */
    public void test_block_filtersAsExpected_anyBind_ipv4() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        InetAddress ipv4LocalAddress = getLocalIpv4Address(ipv4NetworkInterface);
        check_block_filtersAsExpected(
                ipv4LocalAddress /* senderBindAddress */,
                WILDCARD_IPv4 /* receiverBindAddress */,
                GOOD_MULTICAST_IPv4 /* groupAddress */,
                ipv4NetworkInterface);
    }

    /** Checks that block() works when the receiver is bound to the "any" address */
    public void test_block_filtersAsExpected_anyBind_ipv6() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        InetAddress ipv6LocalAddress = getLocalIpv6Address(ipv6NetworkInterface);
        check_block_filtersAsExpected(
                ipv6LocalAddress /* senderBindAddress */,
                WILDCARD_IPv6 /* receiverBindAddress */,
                GOOD_MULTICAST_IPv6 /* groupAddress */,
                ipv6NetworkInterface);
    }

    private void check_block_filtersAsExpected(
            InetAddress senderBindAddress, InetAddress receiverBindAddress,
            InetAddress groupAddress, NetworkInterface networkInterface)
            throws Exception {
        DatagramChannel sendingChannel = DatagramChannel.open();
        // In order to block a sender the sender's address must be known. The sendingChannel is
        // explicitly bound to a known, non-loopback address.
        sendingChannel.bind(new InetSocketAddress(senderBindAddress, 0));
        InetSocketAddress sendingAddress = (InetSocketAddress) sendingChannel.getLocalAddress();

        DatagramChannel receivingChannel = DatagramChannel.open();
        configureChannelForReceiving(receivingChannel);
        receivingChannel.bind(
                new InetSocketAddress(receiverBindAddress, 0) /* local port left to the OS to determine */);
        InetSocketAddress localReceivingAddress =
                (InetSocketAddress) receivingChannel.getLocalAddress();
        InetSocketAddress groupSocketAddress =
                new InetSocketAddress(groupAddress, localReceivingAddress.getPort());
        MembershipKey membershipKey =
                receivingChannel.join(groupSocketAddress.getAddress(), networkInterface);

        ByteBuffer receiveBuffer = ByteBuffer.allocate(10);

        BindableChannel channel = new BindableChannel(sendingChannel, networkInterface);

        // Send a message. It should be received.
        String msg1 = "Hello1";
        channel.sendMulticastMessage(msg1, groupSocketAddress);
        Net.poll(receivingChannel.socket().getFileDescriptor$(), POLLIN, 1000);
        InetSocketAddress sourceAddress1 = (InetSocketAddress) receiveExpectedDatagram(receivingChannel, receiveBuffer);
        assertEquals(sourceAddress1, sendingAddress);
        assertEquals(msg1, new String(receiveBuffer.array(), 0, receiveBuffer.position()));

        // Now block the sender
        membershipKey.block(sendingAddress.getAddress());

        // Send a message. It should be filtered.
        String msg2 = "Hello2";
        channel.sendMulticastMessage(msg2, groupSocketAddress);
        try {
            Net.poll(receivingChannel.socket().getFileDescriptor$(), POLLIN, 1000);
            fail();
        } catch (SocketTimeoutException expected) { }
        receiveBuffer.position(0);
        checkNoDatagramReceived(receivingChannel);

        // Now unblock the sender
        membershipKey.unblock(sendingAddress.getAddress());

        // Send a message. It should be received.
        String msg3 = "Hello3";
        channel.sendMulticastMessage(msg3, groupSocketAddress);
        Net.poll(receivingChannel.socket().getFileDescriptor$(), POLLIN, 1000);
        receiveBuffer.position(0);
        InetSocketAddress sourceAddress3 =
                (InetSocketAddress) receiveExpectedDatagram(receivingChannel, receiveBuffer);
        assertEquals(sourceAddress3, sendingAddress);
        assertEquals(msg3, new String(receiveBuffer.array(), 0, receiveBuffer.position()));

        sendingChannel.close();
        receivingChannel.close();
    }

    public void test_joinSourceSpecific_nullGroupAddress() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        try {
            dc.join(null, ipv4NetworkInterface, UNICAST_IPv4_1);
            fail();
        } catch (NullPointerException expected) {
        }
        dc.close();
    }

    public void test_joinSourceSpecific_afterClose() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        dc.close();
        try {
            dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface, UNICAST_IPv4_1);
            fail();
        } catch (ClosedChannelException expected) {
        }
    }

    public void test_joinSourceSpecific_nullNetworkInterface() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        try {
            dc.join(GOOD_MULTICAST_IPv4, null, UNICAST_IPv4_1);
            fail();
        } catch (NullPointerException expected) {
        }
        dc.close();
    }

    public void test_joinSourceSpecific_nonMulticastGroupAddress_IPv4() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        try {
            dc.join(UNICAST_IPv4_1, ipv4NetworkInterface, UNICAST_IPv4_1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        dc.close();
    }

    public void test_joinSourceSpecific_nonMulticastGroupAddress_IPv6() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        try {
            dc.join(UNICAST_IPv6_1, ipv6NetworkInterface, UNICAST_IPv6_1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        dc.close();
    }

    public void test_joinSourceSpecific_nullSourceAddress_IPv4() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        try {
            dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface, null);
            fail();
        } catch (NullPointerException expected) {
        }
        dc.close();
    }

    public void test_joinSourceSpecific_nullSourceAddress_IPv6() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        try {
            dc.join(GOOD_MULTICAST_IPv6, ipv6NetworkInterface, null);
            fail();
        } catch (NullPointerException expected) {
        }
        dc.close();
    }

    public void test_joinSourceSpecific_mixedAddressTypes() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        try {
            dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface, UNICAST_IPv6_1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            dc.join(GOOD_MULTICAST_IPv6, ipv6NetworkInterface, UNICAST_IPv4_1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        dc.close();
    }

    public void test_joinSourceSpecific_nonUnicastSourceAddress_IPv4() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        try {
            dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface, BAD_MULTICAST_IPv4);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        dc.close();
    }

    public void test_joinSourceSpecific_nonUniicastSourceAddress_IPv6() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        try {
            dc.join(GOOD_MULTICAST_IPv6, ipv6NetworkInterface, BAD_MULTICAST_IPv6);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        dc.close();
    }

    public void test_joinSourceSpecific_multipleSourceAddressLimit() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        for (byte i = 1; i <= 20; i++) {
            InetAddress sourceAddress = Inet4Address.getByAddress(new byte[] { 10, 0, 0, i});
            try {
                dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface, sourceAddress);
            } catch (SocketException e) {
                // There is a limit, that's ok according to the RI docs. For this test a lower bound of 10
                // is used, which appears to be the default linux limit. See /proc/sys/net/ipv4/igmp_max_msf
                assertTrue(i > 10);
                break;
            }
        }

        dc.close();
    }

    /**
     * Checks that a source-specific join() works when the receiver is bound to the multicast group
     * address
     */
    public void test_joinSourceSpecific_null() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        InetAddress ipv4LocalAddress = getLocalIpv4Address(ipv4NetworkInterface);
        check_joinSourceSpecific(
                ipv4LocalAddress /* senderBindAddress */,
                GOOD_MULTICAST_IPv4 /* receiverBindAddress */,
                GOOD_MULTICAST_IPv4 /* groupAddress */,
                UNICAST_IPv4_1 /* badSenderAddress */,
                ipv4NetworkInterface);
    }

    /**
     * Checks that a source-specific join() works when the receiver is bound to the multicast group
     * address
     */
    public void test_joinSourceSpecific_groupBind_ipv4() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        InetAddress ipv4LocalAddress = getLocalIpv4Address(ipv4NetworkInterface);
        check_joinSourceSpecific(
                ipv4LocalAddress /* senderBindAddress */,
                GOOD_MULTICAST_IPv4 /* receiverBindAddress */,
                GOOD_MULTICAST_IPv4 /* groupAddress */,
                UNICAST_IPv4_1 /* badSenderAddress */,
                ipv4NetworkInterface);
    }

    /**
     * Checks that a source-specific join() works when the receiver is bound to the multicast group
     * address
     */
    public void test_joinSourceSpecific_groupBind_ipv6() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        InetAddress ipv6LocalAddress = getLocalIpv6Address(ipv6NetworkInterface);
        check_joinSourceSpecific(
                ipv6LocalAddress /* senderBindAddress */,
                GOOD_MULTICAST_IPv6 /* receiverBindAddress */,
                GOOD_MULTICAST_IPv6 /* groupAddress */,
                UNICAST_IPv6_1 /* badSenderAddress */,
                ipv6NetworkInterface);
    }

    /** Checks that a source-specific join() works when the receiver is bound to the "any" address */
    public void test_joinSourceSpecific_anyBind_ipv4() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        InetAddress ipv4LocalAddress = getLocalIpv4Address(ipv4NetworkInterface);
        check_joinSourceSpecific(
                ipv4LocalAddress /* senderBindAddress */,
                WILDCARD_IPv4 /* receiverBindAddress */,
                GOOD_MULTICAST_IPv4 /* groupAddress */,
                UNICAST_IPv4_1 /* badSenderAddress */,
                ipv4NetworkInterface);
    }

    /** Checks that a source-specific join() works when the receiver is bound to the "any" address */
    public void test_joinSourceSpecific_anyBind_ipv6() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        InetAddress ipv6LocalAddress = getLocalIpv6Address(ipv6NetworkInterface);
        check_joinSourceSpecific(
                ipv6LocalAddress /* senderBindAddress */,
                WILDCARD_IPv6 /* receiverBindAddress */,
                GOOD_MULTICAST_IPv6 /* groupAddress */,
                UNICAST_IPv6_1 /* badSenderAddress */,
                ipv6NetworkInterface);
    }

    /**
     * Checks that the source-specific membership is correctly source-filtering.
     *
     * @param senderBindAddress the address to bind the sender socket to
     * @param receiverBindAddress the address to bind the receiver socket to
     * @param groupAddress the group address to join
     * @param badSenderAddress a unicast address to join to perform a negative test
     * @param networkInterface The network interface on which to join the multicast group
     */
    private void check_joinSourceSpecific(
            InetAddress senderBindAddress, InetAddress receiverBindAddress, InetAddress groupAddress,
            InetAddress badSenderAddress, NetworkInterface networkInterface)
            throws Exception {
        DatagramChannel sendingChannel = DatagramChannel.open();
        // In order to be source-specific the sender's address must be known. The sendingChannel is
        // explicitly bound to a known, non-loopback address.
        sendingChannel.bind(new InetSocketAddress(senderBindAddress, 0));
        InetSocketAddress sendingAddress = (InetSocketAddress) sendingChannel.getLocalAddress();

        DatagramChannel receivingChannel = DatagramChannel.open();
        receivingChannel.bind(
                new InetSocketAddress(receiverBindAddress, 0) /* local port left to the OS to determine */);
        configureChannelForReceiving(receivingChannel);

        InetSocketAddress localReceivingAddress =
                (InetSocketAddress) receivingChannel.getLocalAddress();
        InetSocketAddress groupSocketAddress =
                new InetSocketAddress(groupAddress, localReceivingAddress.getPort());
        MembershipKey membershipKey1 = receivingChannel
                .join(groupSocketAddress.getAddress(), networkInterface, senderBindAddress);

        ByteBuffer receiveBuffer = ByteBuffer.allocate(10);
        BindableChannel channel = new BindableChannel(sendingChannel, networkInterface);
        // Send a message. It should be received.
        String msg1 = "Hello1";
        channel.sendMulticastMessage(msg1, groupSocketAddress);
        InetSocketAddress sourceAddress1 =
                (InetSocketAddress) receiveExpectedDatagram(receivingChannel, receiveBuffer);
        assertEquals(sourceAddress1, sendingAddress);
        assertEquals(msg1, new String(receiveBuffer.array(), 0, receiveBuffer.position()));

        membershipKey1.drop();

        receivingChannel.join(groupSocketAddress.getAddress(), networkInterface, badSenderAddress);

        // Send a message. It should not be received.
        String msg2 = "Hello2";
        channel.sendMulticastMessage(msg2, groupSocketAddress);
        checkNoDatagramReceived(receivingChannel);

        receivingChannel.close();
        sendingChannel.close();
    }

    public void test_dropSourceSpecific_twice_IPv4() throws Exception {
        check_dropSourceSpecific_twice(
                GOOD_MULTICAST_IPv4 /* groupAddress */, UNICAST_IPv4_1 /* sourceAddress */,
                ipv4NetworkInterface);
    }

    public void test_dropSourceSpecific_twice_IPv6() throws Exception {
        check_dropSourceSpecific_twice(
                GOOD_MULTICAST_IPv6 /* groupAddress */, UNICAST_IPv6_1 /* sourceAddress */,
                ipv6NetworkInterface);
    }

    private void check_dropSourceSpecific_twice(InetAddress groupAddress, InetAddress sourceAddress,
            NetworkInterface networkInterface)
            throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        MembershipKey membershipKey = dc.join(groupAddress, networkInterface, sourceAddress);

        assertTrue(membershipKey.isValid());
        membershipKey.drop();
        assertFalse(membershipKey.isValid());

        // Try to leave a group we are no longer a member of - should do nothing.
        membershipKey.drop();

        dc.close();
    }

    public void test_dropSourceSpecific_sourceKeysAreIndependent_IPv4() throws Exception {
        check_dropSourceSpecific_sourceKeysAreIndependent(
                GOOD_MULTICAST_IPv4 /* groupAddress */,
                UNICAST_IPv4_1 /* sourceAddress1 */,
                UNICAST_IPv4_2 /* sourceAddress2 */,
                ipv4NetworkInterface);
    }

    public void test_dropSourceSpecific_sourceKeysAreIndependent_IPv6() throws Exception {
        check_dropSourceSpecific_sourceKeysAreIndependent(
                GOOD_MULTICAST_IPv6 /* groupAddress */,
                UNICAST_IPv6_1 /* sourceAddress1 */,
                UNICAST_IPv6_2 /* sourceAddress2 */,
                ipv6NetworkInterface);
    }

    private void check_dropSourceSpecific_sourceKeysAreIndependent(
            InetAddress groupAddress, InetAddress sourceAddress1, InetAddress sourceAddress2,
            NetworkInterface networkInterface) throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        MembershipKey membershipKey1 = dc.join(groupAddress, networkInterface, sourceAddress1);
        MembershipKey membershipKey2 = dc.join(groupAddress, networkInterface, sourceAddress2);
        assertFalse(membershipKey1.equals(membershipKey2));
        assertTrue(membershipKey1.isValid());
        assertTrue(membershipKey2.isValid());

        membershipKey1.drop();

        assertFalse(membershipKey1.isValid());
        assertTrue(membershipKey2.isValid());

        dc.close();
    }

    public void test_drop_keyBehaviorAfterDrop() throws Exception {
        if (!supportsMulticast) {
            return;
        }
        DatagramChannel dc = createReceiverChannel();
        MembershipKey membershipKey = dc.join(GOOD_MULTICAST_IPv4, ipv4NetworkInterface, UNICAST_IPv4_1);
        membershipKey.drop();
        assertFalse(membershipKey.isValid());

        try {
            membershipKey.block(UNICAST_IPv4_1);
            fail();
        } catch (IllegalStateException expected) {
        }

        try {
            membershipKey.unblock(UNICAST_IPv4_1);
            fail();
        } catch (IllegalStateException expected) {
        }

        assertSame(dc, membershipKey.channel());
        assertSame(GOOD_MULTICAST_IPv4, membershipKey.group());
        assertSame(UNICAST_IPv4_1, membershipKey.sourceAddress());
        assertSame(ipv4NetworkInterface, membershipKey.networkInterface());
    }

    private static void configureChannelForReceiving(DatagramChannel receivingChannel)
            throws Exception {
        /* NOTE: At the time of writing setSoTimeout() has no effect in the RI, making
         * these tests hang if the channel is in blocking mode.
         *
         * Therefore this test instead uses configureBlocking(false) together with
         * {@link #receiveWithTimeout} to do our own blocking.
         */
        receivingChannel.configureBlocking(false);
    }

    /**
     * Asserts that a datagram is received from the supplied {@code receivingChannel}
     * when {@link #receiveWithTimeout(DatagramChannel, ByteBuffer, long) polling}
     * with a short timeout.
     */
    private static SocketAddress receiveExpectedDatagram(DatagramChannel receivingChannel,
            ByteBuffer byteBuffer) throws InterruptedException, IOException {
        long timeoutMillis = 50L;
        SocketAddress result = receiveWithTimeout(receivingChannel, byteBuffer, timeoutMillis);
        assertNotNull("Expected Datagram, but none received after " + timeoutMillis + " msec",
                result);
        return result;
    }

    /**
     * Asserts that no datagram is received from the supplied {@code receivingChannel}
     * when {@link #receiveWithTimeout(DatagramChannel, ByteBuffer, long) polling}
     * with a moderate timeout.
     */
    private static void checkNoDatagramReceived(DatagramChannel receivingChannel)
            throws InterruptedException, IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(100);
        long startMillis = System.currentTimeMillis();
        SocketAddress result = receiveWithTimeout(receivingChannel, byteBuffer, 1000L);
        long elapsed = System.currentTimeMillis() - startMillis;
        assertNull("Datagram unexpectedly received after " + elapsed + " msec", result);
    }

    /**
     * Receives a datagram from {@code receivingChannel} and writes the datagram content
     * into {@code byteBuffer}.
     * This method polls periodically until it finds that either a datagram was received,
     * or the indicated timeout has expired.
     *
     * @return the received datagram's source address, or null if no datagram was received
     *         before the timeout was found to have expired.
     */
    private static SocketAddress receiveWithTimeout(DatagramChannel receivingChannel,
            ByteBuffer byteBuffer, long timeoutMillis) throws InterruptedException, IOException {
        long endTimeMillis = System.currentTimeMillis() + timeoutMillis;
        SocketAddress result;
        while (true) {
            result = receivingChannel.receive(byteBuffer);
            if (result != null) {
                break;
            }
            long remainingMillis = endTimeMillis - System.currentTimeMillis();
            if (remainingMillis <= 0) {
                break;
            }
            Thread.sleep(Math.min(20L, remainingMillis + 1));
        }
        return result;
    }

    private static boolean willWorkForMulticast(NetworkInterface iface) throws IOException {
        return iface.isUp()
                // Typically loopback interfaces do not support multicast, but they are ruled out
                // explicitly here anyway.
                && !iface.isLoopback() && iface.supportsMulticast()
                && iface.getInetAddresses().hasMoreElements();
    }

    private static void createChannelAndSendMulticastMessage(
            InetAddress group, int port, String msg, NetworkInterface sendingInterface)
            throws IOException {
        // Any datagram socket can send to a group. It does not need to have joined the group.
        DatagramChannel dc = DatagramChannel.open();
        BindableChannel channel = new BindableChannel(dc, sendingInterface);
        channel.sendMulticastMessage(msg, new InetSocketAddress(group, port));
        dc.close();
    }

    /**
     * A {@link DatagramChannel} which is optionally bound to a {@link NetworkInterface}.
     */
    static class BindableChannel {
        private final DatagramChannel datagramChannel;

        /**
         * @param networkInterface The interface to bind {@code datagramChannel} to, or null to
         *        not bind.
         */
        public BindableChannel(DatagramChannel datagramChannel, NetworkInterface networkInterface)
                throws IOException {
            this.datagramChannel = datagramChannel;
            if (networkInterface != null) {
                // For some reason, if set, this must be set to a real (non-loopback) device for
                // an IPv6 group, but can be loopback for an IPv4 group.
                datagramChannel.setOption(StandardSocketOptions.IP_MULTICAST_IF, networkInterface);
            }
        }

        public void sendMulticastMessage(String msg, InetSocketAddress targetGroupSocketAddress)
                throws IOException {
            ByteBuffer sendBuffer = ByteBuffer.wrap(msg.getBytes());
            datagramChannel.send(sendBuffer, targetGroupSocketAddress);
        }
    }

    private static InetAddress getLocalIpv4Address(NetworkInterface networkInterface) {
        for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
            if (interfaceAddress.getAddress() instanceof Inet4Address) {
                return interfaceAddress.getAddress();
            }
        }
        throw new AssertionFailedError("Unable to find local IPv4 address for " + networkInterface);
    }

    private static InetAddress getLocalIpv6Address(NetworkInterface networkInterface) {
        for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
            if (interfaceAddress.getAddress() instanceof Inet6Address) {
                return interfaceAddress.getAddress();
            }
        }
        throw new AssertionFailedError("Unable to find local IPv6 address for " + networkInterface);
    }
}

