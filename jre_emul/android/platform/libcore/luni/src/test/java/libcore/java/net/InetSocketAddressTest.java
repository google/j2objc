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

package libcore.java.net;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import junit.framework.TestCase;

public class InetSocketAddressTest extends TestCase {
    public void test_ConstructorLjava_lang_StringI() throws Exception {
        try {
            new InetSocketAddress("127.0.0.1", -1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            new InetSocketAddress("127.0.0.1", 65536);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void test_ConstructorLInetAddressI() throws Exception {
        String[] validIPAddresses = {
            "::1.2.3.4",
            "::", "::",
            "1::0", "1::", "::1",
            "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF",
            "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:255.255.255.255",
            "0:0:0:0:0:0:0:0", "0:0:0:0:0:0:0.0.0.0",
            "127.0.0.1", "localhost", "42.42.42.42", "0.0.0.0"
        };

        String[] results = {
            "0:0:0:0:0:0:102:304",
            "0:0:0:0:0:0:0:0", "0:0:0:0:0:0:0:0",
            "1:0:0:0:0:0:0:0", "1:0:0:0:0:0:0:0", "0:0:0:0:0:0:0:1",
            "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff",
            "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff",
            "0:0:0:0:0:0:0:0", "0:0:0:0:0:0:0:0",
            "localhost", "localhost", "42.42.42.42", "0.0.0.0"
        };

        for (int i = 0; i < validIPAddresses.length; i++) {
            InetAddress ia = InetAddress.getByName(validIPAddresses[i]);
            InetSocketAddress isa = new InetSocketAddress(ia, 80);
            assertEquals(80,isa.getPort());
            //assertEquals(results[i], isa.getHostName());
        }

        InetSocketAddress isa = new InetSocketAddress((InetAddress)null, 80);
        assertEquals("::", isa.getHostName());

        try {
            new InetSocketAddress(InetAddress.getByName("localhost"), 65536);
            fail();
        } catch(IllegalArgumentException expected) {
        }

        try {
            new InetSocketAddress(InetAddress.getByName("localhost"), -1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void test_ConstructorI() {
        InetSocketAddress isa = new  InetSocketAddress(65535);
        assertEquals("::", isa.getHostName());
        assertEquals(65535, isa.getPort());

        try {
            new  InetSocketAddress(-1);
            fail();
        } catch (IllegalArgumentException  expected) {
        }

        try {
            new  InetSocketAddress(65536);
            fail();
        } catch (IllegalArgumentException  expected) {
        }
    }

    public void test_equals() throws Exception {
        InetSocketAddress isa1 = new InetSocketAddress(1);
        InetSocketAddress isa2 = new InetSocketAddress(2);
        assertFalse(isa1.equals(isa2));
        InetSocketAddress isa3 = new InetSocketAddress(1);
        assertTrue(isa1.equals(isa3));

        InetAddress localhost = InetAddress.getByName("localhost");
        isa1 = new InetSocketAddress(localhost.getHostName(), 80);
        isa2 = new InetSocketAddress(localhost.getHostAddress(), 80);
        assertTrue(isa1.equals(isa2));
    }

    public void test_getAddress() throws Exception {
        String[] validIPAddresses = {
            "::1.2.3.4", "::", "::", "1::0", "1::", "::1",
            "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:FFFF",
            "FFFF:FFFF:FFFF:FFFF:FFFF:FFFF:255.255.255.255",
            "0:0:0:0:0:0:0:0", "0:0:0:0:0:0:0.0.0.0",
            "127.0.0.1", "localhost", "42.42.42.42", "0.0.0.0"
        };
        for (int i = 0; i < validIPAddresses.length; i++) {
            InetAddress ia = InetAddress.getByName(validIPAddresses[i]);
            InetSocketAddress isa = new InetSocketAddress(ia, 0);
            assertEquals(ia, isa.getAddress());
        }
        InetSocketAddress isa = new InetSocketAddress((InetAddress) null, 0);
        assertNotNull(isa.getAddress());
    }

    public void test_hashCode() throws Exception {
        InetAddress localhost = InetAddress.getByName("localhost");
        InetSocketAddress isa1 = new InetSocketAddress(localhost.getHostName(), 8080);
        InetSocketAddress isa2 = new InetSocketAddress(localhost.getHostAddress(), 8080);
        assertTrue(isa1.hashCode() == isa2.hashCode());

        InetSocketAddress isa3 = new InetSocketAddress("0.0.0.0", 8080);
        assertFalse(isa1.hashCode() == isa3.hashCode());
    }

    public void test_isUnresolved() {
        InetSocketAddress isa1 = new InetSocketAddress("localhost", 80);
        assertFalse(isa1.isUnresolved());

        InetSocketAddress sockAddr = new InetSocketAddress("unknown.host.google.com", 1000);
        assertTrue(sockAddr.isUnresolved());
    }

    public void test_getHostString() throws Exception {
        // When we have a hostname, we'll get it back because that doesn't cost a DNS lookup...
        InetSocketAddress hasHostname = InetSocketAddress.createUnresolved("some host", 1234);
        assertTrue(hasHostname.isUnresolved());
        assertEquals("some host", hasHostname.getHostString());
        assertEquals("some host", hasHostname.getHostName());

        InetSocketAddress hasHostnameAndAddress = new InetSocketAddress(
                InetAddress.getByAddress("some host", new byte[] { 127, 0, 0, 1 }),
                1234);
        assertFalse(hasHostnameAndAddress.isUnresolved());
        assertEquals("some host", hasHostnameAndAddress.getHostString());
        assertEquals("some host", hasHostnameAndAddress.getHostName());

        // Using a host name that is actually an IP.
        InetSocketAddress hostnameIsIp = InetSocketAddress.createUnresolved("127.0.0.1", 1234);
        assertTrue(hostnameIsIp.isUnresolved());
        assertEquals("127.0.0.1", hostnameIsIp.getHostString());
        assertEquals("127.0.0.1", hostnameIsIp.getHostName());

        // When we don't have a hostname, whether or not we do the reverse lookup is the difference
        // between getHostString and getHostName...
        InetAddress address = InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 });
        InetSocketAddress noHostname = new InetSocketAddress(address, 1234);
        assertEquals("127.0.0.1", noHostname.getHostString());
        assertEquals("localhost", noHostname.getHostName());
    }

    public void test_getHostString_cachingBehavior() throws Exception {
        InetAddress inetAddress = InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 });
        InetSocketAddress socketAddress = new InetSocketAddress(inetAddress, 1234);
        assertEquals("127.0.0.1", socketAddress.getHostString());
        assertEquals("localhost", socketAddress.getHostName());
        assertEquals("localhost", socketAddress.getHostString());

        inetAddress = InetAddress.getByName("127.0.0.1");
        socketAddress = new InetSocketAddress(inetAddress, 1234);
        assertEquals("127.0.0.1", socketAddress.getHostString());
        assertEquals("localhost", socketAddress.getHostName());
        assertEquals("localhost", socketAddress.getHostString());
    }
}
