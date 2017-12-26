/*
 * Copyright (c) 2002, 2005, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.net;
import java.io.IOException;
/*
 * Package private interface to "implementation" used by
 * {@link InetAddress}.
 * <p>
 * See {@link java.net.Inet4AddressImp} and
 * {@link java.net.Inet6AddressImp}.
 *
 * @since 1.4
 */
interface InetAddressImpl {
    /**
     * Lookup all addresses for {@code hostname} on the given {@code netId}.
     */
    InetAddress[] lookupAllHostAddr(String hostname, int netId) throws UnknownHostException;

    /**
     * Reverse-lookup the host name for a given {@code addr}.
     */
    String getHostByAddr(byte[] addr) throws UnknownHostException;

    /**
     * Clear address caches (if any).
     */
    public void clearAddressCache();

    /**
     * Return the "any" local address.
     */
    InetAddress anyLocalAddress();

    /**
     * Return a list of loop back adresses for this implementation.
     */
    InetAddress[] loopbackAddresses();

    /**
     * Whether {@code addr} is reachable over {@code netif}.
     */
    boolean isReachable(InetAddress addr, int timeout, NetworkInterface netif,
                        int ttl) throws IOException;
}
