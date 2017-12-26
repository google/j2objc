/*
 * Copyright (C) 2014 The Android Open Source Project
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
import android.system.ErrnoException;
import android.system.GaiException;
import android.system.StructAddrinfo;
import dalvik.system.BlockGuard;
import libcore.io.NetworkOs;

import java.io.IOException;

import static libcore.io.OsConstants.AF_UNSPEC;
import static libcore.io.OsConstants.AI_ADDRCONFIG;
import static libcore.io.OsConstants.EACCES;
import static libcore.io.OsConstants.SOCK_STREAM;

/*
 * Package private implementation of InetAddressImpl for dual
 * IPv4/IPv6 stack. {@code #anyLocalAddress()} will always return an IPv6 address.
 *
 * @since 1.4
 */

class Inet6AddressImpl implements InetAddressImpl {

    // @GuardedBy(Inet6AddressImpl.class)
    private static InetAddress anyLocalAddress;
    // @GuardedBy(Inet6AddressImpl.class)
    private static InetAddress[] loopbackAddresses;

    private static final AddressCache addressCache = new AddressCache();

    @Override
    public InetAddress[] lookupAllHostAddr(String host, int netId) throws UnknownHostException {
        if (host == null || host.isEmpty()) {
            // Android-changed : Return both the Inet4 and Inet6 loopback addresses
            // when host == null or empty.
            return loopbackAddresses();
        }

        // Is it a numeric address?
        InetAddress result = InetAddress.parseNumericAddressNoThrow(host);
        if (result != null) {
            result = InetAddress.disallowDeprecatedFormats(host, result);
            if (result == null) {
                throw new UnknownHostException("Deprecated IPv4 address format: " + host);
            }
            return new InetAddress[] { result };
        }

        return lookupHostByName(host, netId);
    }

    /**
     * Resolves a hostname to its IP addresses using a cache.
     *
     * @param host the hostname to resolve.
     * @param netId the network to perform resolution upon.
     * @return the IP addresses of the host.
     */
    private static InetAddress[] lookupHostByName(String host, int netId)
            throws UnknownHostException {
        BlockGuard.getThreadPolicy().onNetwork();
        // Do we have a result cached?
        Object cachedResult = addressCache.get(host, netId);
        if (cachedResult != null) {
            if (cachedResult instanceof InetAddress[]) {
                // A cached positive result.
                return (InetAddress[]) cachedResult;
            } else {
                // A cached negative result.
                throw new UnknownHostException((String) cachedResult);
            }
        }
        try {
            StructAddrinfo hints = new StructAddrinfo();
            hints.ai_flags = AI_ADDRCONFIG;
            hints.ai_family = AF_UNSPEC;
            // If we don't specify a socket type, every address will appear twice, once
            // for SOCK_STREAM and one for SOCK_DGRAM. Since we do not return the family
            // anyway, just pick one.
            hints.ai_socktype = SOCK_STREAM;
            InetAddress[] addresses = NetworkOs.getaddrinfo(host, hints);
            // TODO: should getaddrinfo set the hostname of the InetAddresses it returns?
            for (InetAddress address : addresses) {
                address.holder().hostName = host;
            }
            addressCache.put(host, netId, addresses);
            return addresses;
        } catch (GaiException gaiException) {
            // If the failure appears to have been a lack of INTERNET permission, throw a clear
            // SecurityException to aid in debugging this common mistake.
            // http://code.google.com/p/android/issues/detail?id=15722
            if (gaiException.getCause() instanceof ErrnoException) {
                if (((ErrnoException) gaiException.getCause()).errno == EACCES) {
                    throw new SecurityException("Permission denied (missing INTERNET permission?)", gaiException);
                }
            }
            // Otherwise, throw an UnknownHostException.
            String detailMessage = "Unable to resolve host \"" + host + "\": " + NetworkOs.gai_strerror(gaiException.error);
            addressCache.putUnknownHost(host, netId, detailMessage);
            throw gaiException.rethrowAsUnknownHostException(detailMessage);
        }
    }

    @Override
    public String getHostByAddr(byte[] addr) throws UnknownHostException {
        BlockGuard.getThreadPolicy().onNetwork();

        return getHostByAddr0(addr);
    }

    @Override
    public void clearAddressCache() {
        addressCache.clear();
    }

    @Override
    public boolean isReachable(InetAddress addr, int timeout, NetworkInterface netif, int ttl) throws IOException {
        byte[] ifaddr = null;
        int scope = -1;
        int netif_scope = -1;
        if (netif != null) {
            /*
             * Let's make sure we bind to an address of the proper family.
             * Which means same family as addr because at this point it could
             * be either an IPv6 address or an IPv4 address (case of a dual
             * stack system).
             */
            java.util.Enumeration it = netif.getInetAddresses();
            InetAddress inetaddr = null;
            while (it.hasMoreElements()) {
                inetaddr = (InetAddress) it.nextElement();
                if (inetaddr.getClass().isInstance(addr)) {
                    ifaddr = inetaddr.getAddress();
                    if (inetaddr instanceof Inet6Address) {
                        netif_scope = ((Inet6Address) inetaddr).getScopeId();
                    }
                    break;
                }
            }
            if (ifaddr == null) {
                // Interface doesn't support the address family of
                // the destination
                return false;
            }
        }
        if (addr instanceof Inet6Address)
            scope = ((Inet6Address) addr).getScopeId();

        BlockGuard.getThreadPolicy().onNetwork();

        // Never throw an IOException from isReachable. If something terrible happens either
        // with the network interface in question (or with the destination), then just return
        // false (i.e, state that the address is unreachable.
        try {
            return isReachable0(addr.getAddress(), scope, timeout, ifaddr, ttl, netif_scope);
        } catch (IOException ioe) {
            return false;
        }
    }

    @Override
    public InetAddress anyLocalAddress() {
        synchronized (Inet6AddressImpl.class) {
            // We avoid initializing anyLocalAddress during <clinit> to avoid issues
            // caused by the dependency chains of these classes. InetAddress depends on
            // InetAddressImpl, but Inet6Address & Inet4Address are its subclasses.
            // Also see {@code loopbackAddresses).
            if (anyLocalAddress == null) {
                Inet6Address anyAddress = new Inet6Address();
                anyAddress.holder().hostName = "::";
                anyLocalAddress = anyAddress;
            }

            return anyLocalAddress;
        }
    }

    @Override
    public InetAddress[] loopbackAddresses() {
        synchronized (Inet6AddressImpl.class) {
            // We avoid initializing anyLocalAddress during <clinit> to avoid issues
            // caused by the dependency chains of these classes. InetAddress depends on
            // InetAddressImpl, but Inet6Address & Inet4Address are its subclasses.
            // Also see {@code anyLocalAddress).
            if (loopbackAddresses == null) {
                loopbackAddresses = new InetAddress[]{Inet6Address.LOOPBACK, Inet4Address.LOOPBACK};
            }

            return loopbackAddresses;
        }
    }

    private native String getHostByAddr0(byte[] addr) throws UnknownHostException;
    private native boolean isReachable0(byte[] addr, int scope, int timeout, byte[] inf, int ttl, int if_scope) throws IOException;
}
