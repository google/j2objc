/*
 * Copyright (C) 2011 The Android Open Source Project
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

import junit.framework.TestCase;
import java.lang.reflect.Field;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import libcore.io.IoUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkInterfaceTest extends TestCase {
    // http://code.google.com/p/android/issues/detail?id=13784
    private final static int ARPHRD_ETHER = 1; // from if_arp.h
    public void testIPv6() throws Exception {
        NetworkInterface lo = NetworkInterface.getByName("lo0");
        Set<InetAddress> actual = new HashSet<InetAddress>(Collections.list(lo.getInetAddresses()));

        assertFalse(actual.isEmpty());
    }

    /*
    // http://code.google.com/p/android/issues/detail?id=34022
    public void test_collectIpv6Addresses_3digitInterfaceIndex() throws Exception {
        String lines[] = new String[] {
                "fe800000000000000000000000000000 407 40 20 80    wlan0" };
        List<InetAddress> addresses = new ArrayList<InetAddress>(1);
        List<InterfaceAddress> ifAddresses = new ArrayList<InterfaceAddress>(1);

        NetworkInterface.collectIpv6Addresses("wlan0", 1, addresses,
                ifAddresses, lines);
        assertEquals(1, addresses.size());
        assertEquals(1, ifAddresses.size());
        // Make sure the prefix length (field #3) is parsed correctly
        assertEquals(4*16 + 0, ifAddresses.get(0).getNetworkPrefixLength());
    }

    public void test_collectIpv6Addresses_skipsUnmatchedLines() throws Exception {
        String[] lines = new String[] {
                "fe800000000000000000000000000000 40 40 20 80    wlan0",
                "fe100000000000000000000000000000 41 40 20 80    wlan1",
                "feb00000000000000000000000000000 42 40 20 80    wlan2" };
        List<InetAddress> addresses = new ArrayList<InetAddress>(1);
        List<InterfaceAddress> ifAddresses = new ArrayList<InterfaceAddress>(1);

        NetworkInterface.collectIpv6Addresses("wlan0", 1, addresses,
                ifAddresses, lines);
        assertEquals(1, addresses.size());
        assertEquals(1, ifAddresses.size());
    }*/

    public void testInterfaceProperties() throws Exception {
        for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            assertEquals(nif, NetworkInterface.getByName(nif.getName()));
            // Skip interfaces that are inactive
            if (nif.isUp() == false) {
                continue;
            }
            // Ethernet
            if (isEthernet(nif.getName())) {
                assertEquals(6, nif.getHardwareAddress().length);
                for (InterfaceAddress ia : nif.getInterfaceAddresses()) {
                    if (ia.getAddress() instanceof Inet4Address) {
                        assertNotNull(ia.getBroadcast());
                    }
                }
            }
        }
    }

    public void testLoopback() throws Exception {
        NetworkInterface lo = NetworkInterface.getByName("lo0");
        assertNull(lo.getHardwareAddress());
        for (InterfaceAddress ia : lo.getInterfaceAddresses()) {
            assertNull(ia.getBroadcast());
        }
    }

    public void testDumpAll() throws Exception {
        Set<String> allNames = new HashSet<String>();
        Set<Integer> allIndexes = new HashSet<Integer>();
        for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            //System.err.println(nif);
            //System.err.println(nif.getInterfaceAddresses());
            String flags = nif.isUp() ? "UP" : "DOWN";
            if (nif.isLoopback()) {
                flags += " LOOPBACK";
            }
            if (nif.isPointToPoint()) {
                flags += " PTP";
            }
            if (nif.isVirtual()) {
                flags += " VIRTUAL";
            }
            if (nif.supportsMulticast()) {
                flags += " MULTICAST";
            }
            flags += " MTU=" + nif.getMTU();
            byte[] mac = nif.getHardwareAddress();
            if (mac != null) {
                flags += " HWADDR=";
                for (int i = 0; i < mac.length; ++i) {
                    if (i > 0) {
                        flags += ":";
                    }
                    flags += String.format("%02x", mac[i]);
                }
            }
            //System.err.println(flags);
            //System.err.println("-");

            assertFalse(allNames.contains(nif.getName()));
            allNames.add(nif.getName());

            assertFalse(allIndexes.contains(nif.getIndex()));
            allIndexes.add(nif.getIndex());
        }
    }

    // b/28903817
    public void testInterfaceRemoval() throws Exception {
        NetworkInterface lo = NetworkInterface.getByName("lo0");

        // Simulate interface removal by changing it's name to unused value.
        // This works only because getHardwareAddress (and others) is using name to fetch
        // the NI data. If this changes, this test needs an update.
        Field nameField = NetworkInterface.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(lo, "noSuchInterface");

        try {
            lo.getHardwareAddress();
            fail();
        } catch(SocketException expected) {}

        try {
            lo.getMTU();
            fail();
        } catch(SocketException expected) {}

        try {
            lo.isLoopback();
            fail();
        } catch(SocketException expected) {}

        try {
            lo.isUp();
            fail();
        } catch(SocketException expected) {}

        try {
            lo.isPointToPoint();
            fail();
        } catch(SocketException expected) {}

        try {
            lo.supportsMulticast();
            fail();
        } catch(SocketException expected) {}
    }

    // Is ifName a name of a Ethernet device?
    private static Pattern ethernetNamePattern = Pattern.compile("^(eth|wlan)[0-9]+$");
    private static boolean isEthernet(String ifName) throws Exception {
        return ethernetNamePattern.matcher(ifName).matches();
    }
}
