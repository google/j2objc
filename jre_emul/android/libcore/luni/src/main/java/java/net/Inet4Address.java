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

package java.net;

import java.io.ObjectStreamException;
import java.nio.ByteOrder;
import libcore.io.Memory;
import static libcore.io.OsConstants.*;

/**
 * An IPv4 address. See {@link InetAddress}.
 */
public final class Inet4Address extends InetAddress {

    private static final long serialVersionUID = 3286316764910316507L;

    /**
     * @hide
     */
    public static final InetAddress ANY =
            new Inet4Address(new byte[] { 0, 0, 0, 0 }, null);

    /**
     * @hide
     */
    public static final InetAddress ALL =
            new Inet4Address(new byte[] { (byte) 255, (byte) 255,
                                          (byte) 255, (byte) 255 }, null);

    /**
     * @hide
     */
    public static final InetAddress LOOPBACK =
            new Inet4Address(new byte[] { 127, 0, 0, 1 }, "localhost");

    Inet4Address(byte[] ipaddress, String hostName) {
        super(AF_INET, ipaddress, hostName);
    }

    @Override public boolean isAnyLocalAddress() {
        return ipaddress[0] == 0 && ipaddress[1] == 0 && ipaddress[2] == 0 && ipaddress[3] == 0; // 0.0.0.0
    }

    @Override public boolean isLinkLocalAddress() {
        // The RI does not return true for loopback addresses even though RFC 3484 says to do so.
        return ((ipaddress[0] & 0xff) == 169) && ((ipaddress[1] & 0xff) == 254); // 169.254/16
    }

    @Override public boolean isLoopbackAddress() {
        return ((ipaddress[0] & 0xff) == 127); // 127/8
    }

    @Override public boolean isMCGlobal() {
        // Check if we have a prefix of 1110
        if (!isMulticastAddress()) {
            return false;
        }

        int address = Memory.peekInt(ipaddress, 0, ByteOrder.BIG_ENDIAN);
        /*
         * Now check the boundaries of the global space if we have an address
         * that is prefixed by something less than 111000000000000000000001
         * (fortunately we don't have to worry about sign after shifting 8 bits
         * right) it is not multicast. ( < 224.0.1.0)
         */
        if (address >>> 8 < 0xE00001) {
            return false;
        }

        /*
         * Now check the high boundary which is prefixed by 11101110 = 0xEE. If
         * the value is higher than this than it is not MCGlobal ( >
         * 238.255.255.255 )
         */
        if (address >>> 24 > 0xEE) {
            return false;
        }

        return true;
    }

    @Override public boolean isMCLinkLocal() {
        return ((ipaddress[0] & 0xff) == 224) && (ipaddress[1] == 0) && (ipaddress[2] == 0); // 224.0.0/24
    }

    @Override public boolean isMCNodeLocal() {
        return false;
    }

    @Override public boolean isMCOrgLocal() {
        return ((ipaddress[0] & 0xff) == 239) && ((ipaddress[1] & 0xfc) == 192); // 239.192/14
    }

    @Override public boolean isMCSiteLocal() {
        return ((ipaddress[0] & 0xff) == 239) && ((ipaddress[1] & 0xff) == 255); // 239.255/16
    }

    @Override public boolean isMulticastAddress() {
        return (ipaddress[0] & 0xf0) == 224; // 224/4
    }

    @Override public boolean isSiteLocalAddress() {
        if ((ipaddress[0] & 0xff) == 10) { // 10/8
            return true;
        } else if (((ipaddress[0] & 0xff) == 172) && ((ipaddress[1] & 0xf0) == 16)) { // 172.16/12
            return true;
        } else if (((ipaddress[0] & 0xff) == 192) && ((ipaddress[1] & 0xff) == 168)) { // 192.168/16
            return true;
        }
        return false;
    }

    private Object writeReplace() throws ObjectStreamException {
        return new Inet4Address(ipaddress, hostName);
    }
}
