/*
 * Copyright (C) 2018 The Android Open Source Project
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
package libcore.net;

import android.system.GaiException;
import android.system.StructAddrinfo;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import libcore.io.Libcore;
import libcore.io.NetworkOs;

import static libcore.io.OsConstants.AF_INET;
import static libcore.io.OsConstants.AI_NUMERICHOST;

/**
 * Android specific utility methods for {@link InetAddress} instances.
 *
 * @hide
 */
@libcore.api.CorePlatformApi
public class InetAddressUtils {

    private static final int NETID_UNSET = 0;

    private InetAddressUtils() {
    }

    /**
     * Checks to see if the {@code address} is a numeric address (such as {@code "192.0.2.1"} or
     * {@code "2001:db8::1:2"}).
     *
     * <p>A numeric address is either an IPv4 address containing exactly 4 decimal numbers or an
     * IPv6 numeric address. IPv4 addresses that consist of either hexadecimal or octal digits or
     * do not have exactly 4 numbers are not treated as numeric.
     *
     * <p>This method will never do a DNS lookup.
     *
     * @param address the address to parse.
     * @return true if the supplied address is numeric, false otherwise.
     */
    @libcore.api.CorePlatformApi
    public static boolean isNumericAddress(String address) {
        return parseNumericAddressNoThrow(address) != null;
    }

    /**
     * Returns an InetAddress corresponding to the given numeric address (such
     * as {@code "192.168.0.1"} or {@code "2001:4860:800d::68"}).
     *
     * <p>See {@link #isNumericAddress(String)} for a definition as to what constitutes a numeric
     * address.
     *
     * <p>This method will never do a DNS lookup.
     *
     * @param address the address to parse, must be numeric.
     * @return an {@link InetAddress} instance corresponding to the address.
     * @throws IllegalArgumentException if {@code address} is not a numeric address.
     */
    @libcore.api.CorePlatformApi
    public static InetAddress parseNumericAddress(String address) {
        InetAddress result = parseNumericAddressNoThrow(address);
        if (result == null) {
            throw new IllegalArgumentException("Not a numeric address: " + address);
        }
        return result;
    }

    public static InetAddress parseNumericAddressNoThrow(String address) {
        StructAddrinfo hints = new StructAddrinfo();
        hints.ai_flags = AI_NUMERICHOST;
        InetAddress[] addresses = null;
        try {
            /* J2ObjC modified.
            addresses = Libcore.os.android_getaddrinfo(address, hints, NETID_UNSET);
             */
            addresses = NetworkOs.getaddrinfo(address, hints);
        } catch (GaiException ignored) {
        }
        if (addresses == null) {
            return null;
        }
        return addresses[0];
    }

    /**
     * Like {@link #parseNumericAddressNoThrow(String)}}, but strips optional []
     * around a numeric IPv6 address.
     */
    public static InetAddress parseNumericAddressNoThrowStripOptionalBrackets(String address) {
        // Accept IPv6 addresses (only) in square brackets for compatibility.
        if (address.startsWith("[") && address.endsWith("]") && address.indexOf(':') != -1) {
            address = address.substring(1, address.length() - 1);
        }
        return InetAddressUtils.parseNumericAddressNoThrow(address);
    }
}
