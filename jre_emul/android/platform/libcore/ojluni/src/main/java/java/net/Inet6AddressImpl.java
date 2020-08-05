/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
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

import libcore.io.IoBridge;
import libcore.io.NetworkOs;

import java.io.FileDescriptor;
import java.io.IOException;
import libcore.net.InetAddressUtils;

import static libcore.io.OsConstants.AF_INET;
import static libcore.io.OsConstants.AF_INET6;
import static libcore.io.OsConstants.AF_UNSPEC;
import static libcore.io.OsConstants.AI_ADDRCONFIG;
import static libcore.io.OsConstants.EACCES;
import static libcore.io.OsConstants.ECONNREFUSED;
import static libcore.io.OsConstants.EPERM;
import static libcore.io.OsConstants.NI_NAMEREQD;
import static libcore.io.OsConstants.SOCK_DGRAM;
import static libcore.io.OsConstants.SOCK_STREAM;

// Android-note: Android-specific behavior and Linux-based implementation
// http://b/36933260 Implement root-less ICMP for isReachable()
// http://b/28609551 Rewrite getHostByAddr0 using POSIX library Libcore.os.
// http://b/25861497 Add BlockGuard checks.
// http://b/26700324 Fix odd dependency chains of the static InetAddress.
// anyLocalAddress() Let anyLocalAddress() always return an IPv6 address.
// Let loopbackAddresses() return both Inet4 and Inet6 loopbacks.
// Rewrote hostname lookup methods on top of Libcore.os. Merge implementation from InetAddress
//   and remove native methods in this class
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

    // BEGIN Android-changed: Rewrote hostname lookup methods on top of Libcore.os.
    /*
    public native String getLocalHostName() throws UnknownHostException;
    public native InetAddress[]
        lookupAllHostAddr(String hostname) throws UnknownHostException;
    public native String getHostByAddr(byte[] addr) throws UnknownHostException;
    private native boolean isReachable0(byte[] addr, int scope, int timeout, byte[] inf, int ttl, int if_scope) throws IOException;
    */
    @Override
    public InetAddress[] lookupAllHostAddr(String host, int netId) throws UnknownHostException {
        if (host == null || host.isEmpty()) {
            // Android-changed: Return both the Inet4 and Inet6 loopback addresses
            // when host == null or empty.
            return loopbackAddresses();
        }

        // Is it a numeric address?
        InetAddress result = InetAddressUtils.parseNumericAddressNoThrowStripOptionalBrackets(host);
        if (result != null) {
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
            /* J2ObjC modified.
            InetAddress[] addresses = Libcore.os.android_getaddrinfo(host, hints, netId);
             */
            InetAddress[] addresses = NetworkOs.getaddrinfo(host, hints);
            // TODO: should getaddrinfo set the hostname of the InetAddresses it returns?
            for (InetAddress address : addresses) {
                address.holder().hostName = host;
                address.holder().originalHostName = host;
            }
            addressCache.put(host, netId, addresses);
            return addresses;
        } catch (GaiException gaiException) {
            // If the failure appears to have been a lack of INTERNET permission, throw a clear
            // SecurityException to aid in debugging this common mistake.
            // http://code.google.com/p/android/issues/detail?id=15722
            if (gaiException.getCause() instanceof ErrnoException) {
                int errno = ((ErrnoException) gaiException.getCause()).errno;
                if (errno == EACCES || errno == EPERM) {
                    throw new SecurityException("Permission denied (missing INTERNET permission?)", gaiException);
                }
            }
            // Otherwise, throw an UnknownHostException.
            String detailMessage = "Unable to resolve host \"" + host + "\": " +
                    /* J2ObjC modified.
                    Libcore.os.gai_strerror(gaiException.error);
                     */
                    NetworkOs.gai_strerror(gaiException.error);
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
    // END Android-changed: Rewrote hostname lookup methods on top of Libcore.os.

    @Override
    public boolean isReachable(InetAddress addr, int timeout, NetworkInterface netif, int ttl) throws IOException {
        /* J2ObjC modified: use native method instead.
        // Android-changed: rewritten on the top of IoBridge and Libcore.os.
        InetAddress sourceAddr = null;
        if (netif != null) {
            /*
             * Let's make sure we bind to an address of the proper family.
             * Which means same family as addr because at this point it could
             * be either an IPv6 address or an IPv4 address (case of a dual
             * stack system).
             *
            java.util.Enumeration<InetAddress> it = netif.getInetAddresses();
            InetAddress inetaddr = null;
            while (it.hasMoreElements()) {
                inetaddr = it.nextElement();
                if (inetaddr.getClass().isInstance(addr)) {
                    sourceAddr = inetaddr;
                    break;
                }
            }

            if (sourceAddr == null) {
                // Interface doesn't support the address family of
                // the destination
                return false;
            }
        }

        // Android-changed: http://b/36933260 Implement root-less ICMP for isReachable().
            /*
            if (addr instanceof Inet6Address)
                scope = ((Inet6Address) addr).getScopeId();
            return isReachable0(addr.getAddress(), scope, timeout, ifaddr, ttl, netif_scope);
            *
        // Try ICMP first
            if (icmpEcho(addr, timeout, sourceAddr, ttl)) {
            return true;
        }

        // No good, let's fall back to TCP
            return tcpEcho(addr, timeout, sourceAddr, ttl);
         */

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

    /* J2ObjC removed: use native method instead.
    // BEGIN Android-added: http://b/36933260 Implement root-less ICMP for isReachable().
    private boolean tcpEcho(InetAddress addr, int timeout, InetAddress sourceAddr, int ttl)
            throws IOException {
        FileDescriptor fd = null;
        try {
            fd = IoBridge.socket(AF_INET6, SOCK_STREAM, 0);
            if (ttl > 0) {
                IoBridge.setSocketOption(fd, IoBridge.JAVA_IP_TTL, ttl);
            }
            if (sourceAddr != null) {
                IoBridge.bind(fd, sourceAddr, 0);
            }
            IoBridge.connect(fd, addr, 7 /* Echo-protocol port *, timeout);
            return true;
        } catch (IOException e) {
            // Connection refused by remote (ECONNREFUSED) implies reachable. Otherwise silently
            // ignore the exception and return false.
            Throwable cause = e.getCause();
            return cause instanceof ErrnoException
                    && ((ErrnoException) cause).errno == ECONNREFUSED;
        } finally {
            IoBridge.closeAndSignalBlockedThreads(fd);
        }
    }

    protected boolean icmpEcho(InetAddress addr, int timeout, InetAddress sourceAddr, int ttl)
            throws IOException {

        FileDescriptor fd = null;
        try {
            boolean isIPv4 = addr instanceof Inet4Address;
            int domain = isIPv4 ? AF_INET : AF_INET6;
            int icmpProto = isIPv4 ? IPPROTO_ICMP : IPPROTO_ICMPV6;
            fd = IoBridge.socket(domain, SOCK_DGRAM, icmpProto);

            if (ttl > 0) {
                IoBridge.setSocketOption(fd, IoBridge.JAVA_IP_TTL, ttl);
            }
            if (sourceAddr != null) {
                IoBridge.bind(fd, sourceAddr, 0);
            }

            byte[] packet;

            // ICMP is unreliable, try sending requests every second until timeout.
            for (int to = timeout, seq = 1; to > 0; ++seq) {
                int sockTo = to >= 1000 ? 1000 : to;

                IoBridge.setSocketOption(fd, SocketOptions.SO_TIMEOUT, sockTo);

                packet = IcmpHeaders.createIcmpEchoHdr(isIPv4, seq);
                IoBridge.sendto(fd, packet, 0, packet.length, 0, addr, 0);
                final int icmpId = IoBridge.getLocalInetSocketAddress(fd).getPort();

                byte[] received = new byte[packet.length];
                DatagramPacket receivedPacket = new DatagramPacket(received, packet.length);
                int size = IoBridge
                        .recvfrom(true, fd, received, 0, received.length, 0, receivedPacket, false);
                if (size == packet.length) {
                    byte expectedType = isIPv4 ? (byte) ICMP_ECHOREPLY
                            : (byte) ICMP6_ECHO_REPLY;
                    if (receivedPacket.getAddress().equals(addr)
                            && received[0] == expectedType
                            && received[4] == (byte) (icmpId >> 8)
                            && received[5] == (byte) icmpId) {
                        int receivedSequence = ((received[6] & 0xff) << 8) + (received[7] & 0xff);
                        if (receivedSequence <= seq) {
                            return true;
                        }
                    }
                }
                to -= sockTo;
            }
        } catch (IOException e) {
            // Silently ignore and fall back.
        } finally {
            if (fd != null) {
                try {
                    Libcore.os.close(fd);
                } catch (ErrnoException e) { }
            }
        }

        return false;
    }
    // END Android-added: http://b/36933260 Implement root-less ICMP for isReachable().
     */

    // BEGIN Android-changed: Let anyLocalAddress() always return an IPv6 address.
    @Override
    public InetAddress anyLocalAddress() {
        synchronized (Inet6AddressImpl.class) {
            // We avoid initializing anyLocalAddress during <clinit> to avoid issues
            // caused by the dependency chains of these classes. InetAddress depends on
            // InetAddressImpl, but Inet6Address & Inet4Address are its subclasses.
            // Also see {@code loopbackAddresses). http://b/26700324
            if (anyLocalAddress == null) {
                Inet6Address anyAddress = new Inet6Address();
                anyAddress.holder().hostName = "::";
                anyLocalAddress = anyAddress;
            }

            return anyLocalAddress;
        }
    }
    // END Android-changed: Let anyLocalAddress() always return an IPv6 address.

    // BEGIN Android-changed: Let loopbackAddresses() return both Inet4 and Inet6 loopbacks.
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
    // END Android-changed: Let loopbackAddresses() return both Inet4 and Inet6 loopbacks.

    /* J2ObjC removed: use native method instead.
    // BEGIN Android-changed: b/28609551 Rewrite getHostByAddr0 using POSIX library Libcore.os.
    private String getHostByAddr0(byte[] addr) throws UnknownHostException {
        // Android-changed: Rewritten on the top of Libcore.os
        InetAddress hostaddr = InetAddress.getByAddress(addr);
        try {
            return Libcore.os.getnameinfo(hostaddr, NI_NAMEREQD);
        } catch (GaiException e) {
            UnknownHostException uhe = new UnknownHostException(hostaddr.toString());
            uhe.initCause(e);
            throw uhe;
        }
    }
    // END Android-changed: b/28609551 Rewrite getHostByAddr0 using POSIX library Libcore.os.
     */

    /* J2ObjC added: native implementation. */
    private native String getHostByAddr0(byte[] addr) throws UnknownHostException;
    private native boolean isReachable0(byte[] addr, int scope, int timeout, byte[] inf, int ttl, int if_scope) throws IOException;
}
