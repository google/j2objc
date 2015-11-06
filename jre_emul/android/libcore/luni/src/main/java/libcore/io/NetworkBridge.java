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

package libcore.io;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.BindException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketOptions;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import static libcore.io.OsConstants.*;

/**
 * Implements java.net semantics in terms of the underlying POSIX system calls.
 */
public final class NetworkBridge {

    private NetworkBridge() {
    }

    public static void bind(FileDescriptor fd, InetAddress address, int port) throws SocketException {
        if (address instanceof Inet6Address) {
            Inet6Address inet6Address = (Inet6Address) address;
            if (inet6Address.getScopeId() == 0 && inet6Address.isLinkLocalAddress()) {
                // Linux won't let you bind a link-local address without a scope id.
                // Find one.
                NetworkInterface nif = NetworkInterface.getByInetAddress(address);
                if (nif == null) {
                    throw new SocketException("Can't bind to a link-local address without a scope id: " + address);
                }
                try {
                    address = Inet6Address.getByAddress(address.getHostName(), address.getAddress(), nif.getIndex());
                } catch (UnknownHostException ex) {
                    throw new AssertionError(ex); // Can't happen.
                }
            }
        }
        try {
            NetworkOs.bind(fd, address, port);
        } catch (ErrnoException errnoException) {
            throw new BindException(errnoException.getMessage(), errnoException);
        }
    }

    /**
     * Connects socket 'fd' to 'inetAddress' on 'port', with no timeout. The lack of a timeout
     * means this method won't throw SocketTimeoutException.
     */
    public static void connect(FileDescriptor fd, InetAddress inetAddress, int port) throws SocketException {
        try {
            NetworkBridge.connect(fd, inetAddress, port, 0);
        } catch (SocketTimeoutException ex) {
            throw new AssertionError(ex); // Can't happen for a connect without a timeout.
        }
    }

    /**
     * Connects socket 'fd' to 'inetAddress' on 'port', with a the given 'timeoutMs'.
     * Use timeoutMs == 0 for a blocking connect with no timeout.
     */
    public static void connect(FileDescriptor fd, InetAddress inetAddress, int port, int timeoutMs) throws SocketException, SocketTimeoutException {
        try {
            connectErrno(fd, inetAddress, port, timeoutMs);
        } catch (ErrnoException errnoException) {
            throw new ConnectException(connectDetail(inetAddress, port, timeoutMs, errnoException), errnoException);
        } catch (SocketException ex) {
            throw ex; // We don't want to doubly wrap these.
        } catch (SocketTimeoutException ex) {
            throw ex; // We don't want to doubly wrap these.
        } catch (IOException ex) {
            throw new SocketException(ex);
        }
    }

    private static void connectErrno(FileDescriptor fd, InetAddress inetAddress, int port, int timeoutMs) throws ErrnoException, IOException {
        // With no timeout, just call connect(2) directly.
        if (timeoutMs == 0) {
            NetworkOs.connect(fd, inetAddress, port);
            return;
        }

        // For connect with a timeout, we:
        //   1. set the socket to non-blocking,
        //   2. connect(2),
        //   3. loop using poll(2) to decide whether we're connected, whether we should keep
        //      waiting, or whether we've seen a permanent failure and should give up,
        //   4. set the socket back to blocking.

        // 1. set the socket to non-blocking.
        IoUtils.setBlocking(fd, false);

        // 2. call connect(2) non-blocking.
        long finishTimeMs = System.currentTimeMillis() + timeoutMs;
        try {
            NetworkOs.connect(fd, inetAddress, port);
            IoUtils.setBlocking(fd, true); // 4. set the socket back to blocking.
            return; // We connected immediately.
        } catch (ErrnoException errnoException) {
            if (errnoException.errno == ETIMEDOUT) {
                throw new SocketTimeoutException(connectDetail(inetAddress, port, timeoutMs, null));
            }
            if (errnoException.errno != EINPROGRESS) {
                throw errnoException;
            }
            // EINPROGRESS means we should keep trying...
        }

        // 3. loop using poll(2).
        int remainingTimeoutMs;
        do {
            remainingTimeoutMs = (int) (finishTimeMs - System.currentTimeMillis());
            if (remainingTimeoutMs <= 0) {
                throw new SocketTimeoutException(connectDetail(inetAddress, port, timeoutMs, null));
            }
        } while (!NetworkBridge.isConnected(fd, inetAddress, port, timeoutMs, remainingTimeoutMs));
        IoUtils.setBlocking(fd, true); // 4. set the socket back to blocking.
    }

    private static String connectDetail(InetAddress inetAddress, int port, int timeoutMs, ErrnoException cause) {
        String detail = "failed to connect to " + inetAddress + " (port " + port + ")";
        if (timeoutMs > 0) {
            detail += " after " + timeoutMs + "ms";
        }
        if (cause != null) {
            detail += ": " + cause.getMessage();
        }
        return detail;
    }

    public static void closeSocket(FileDescriptor fd) throws IOException {
        if (!fd.valid()) {
            // Socket.close doesn't throw if you try to close an already-closed socket.
            return;
        }
        int intFd = fd.getInt$();
        fd.setInt$(-1);
        FileDescriptor oldFd = new FileDescriptor();
        oldFd.setInt$(intFd);
        AsynchronousCloseMonitor.signalBlockedThreads(oldFd);
        try {
            Libcore.os.close(oldFd);
        } catch (ErrnoException errnoException) {
            // TODO: are there any cases in which we should throw?
        }
    }

    public static boolean isConnected(FileDescriptor fd, InetAddress inetAddress, int port, int timeoutMs, int remainingTimeoutMs) throws IOException {
        ErrnoException cause;
        try {
            StructPollfd[] pollFds = new StructPollfd[] { new StructPollfd() };
            pollFds[0].fd = fd;
            pollFds[0].events = (short) POLLOUT;
            int rc = Libcore.os.poll(pollFds, remainingTimeoutMs);
            if (rc == 0) {
                return false; // Timeout.
            }
            int connectError = NetworkOs.getsockoptInt(fd, SOL_SOCKET, SO_ERROR);
            if (connectError == 0) {
                return true; // Success!
            }
            throw new ErrnoException("isConnected", connectError); // The connect(2) failed.
        } catch (ErrnoException errnoException) {
            if (!fd.valid()) {
                throw new SocketException("Socket closed");
            }
            if (errnoException.errno == EINTR) {
                return false; // Punt and ask the caller to try again.
            } else {
                cause = errnoException;
            }
        }
        String detail = connectDetail(inetAddress, port, timeoutMs, cause);
        if (cause.errno == ETIMEDOUT) {
            throw new SocketTimeoutException(detail, cause);
        }
        throw new ConnectException(detail, cause);
    }

    // Socket options used by java.net but not exposed in SocketOptions.
    public static final int JAVA_MCAST_JOIN_GROUP = 19;
    public static final int JAVA_MCAST_LEAVE_GROUP = 20;
    public static final int JAVA_MCAST_JOIN_SOURCE_GROUP = 21;
    public static final int JAVA_MCAST_LEAVE_SOURCE_GROUP = 22;
    public static final int JAVA_MCAST_BLOCK_SOURCE = 23;
    public static final int JAVA_MCAST_UNBLOCK_SOURCE = 24;
    public static final int JAVA_IP_MULTICAST_TTL = 17;

    /**
     * java.net has its own socket options similar to the underlying Unix ones. We paper over the
     * differences here.
     */
    public static Object getSocketOption(FileDescriptor fd, int option) throws SocketException {
        try {
            return getSocketOptionErrno(fd, option);
        } catch (ErrnoException errnoException) {
            throw new SocketException(errnoException.getMessage(), errnoException);
        }
    }

    private static Object getSocketOptionErrno(FileDescriptor fd, int option) throws ErrnoException, SocketException {
        switch (option) {
        case SocketOptions.IP_MULTICAST_IF:
            // This is IPv4-only.
            return NetworkOs.getsockoptInAddr(fd, IPPROTO_IP, IP_MULTICAST_IF);
        case SocketOptions.IP_MULTICAST_IF2:
            // This is IPv6-only.
            return NetworkOs.getsockoptInt(fd, IPPROTO_IPV6, IPV6_MULTICAST_IF);
        case SocketOptions.IP_MULTICAST_LOOP:
            // Since setting this from java.net always sets IPv4 and IPv6 to the same value,
            // it doesn't matter which we return.
            return booleanFromInt(NetworkOs.getsockoptInt(fd, IPPROTO_IPV6, IPV6_MULTICAST_LOOP));
        case NetworkBridge.JAVA_IP_MULTICAST_TTL:
            // Since setting this from java.net always sets IPv4 and IPv6 to the same value,
            // it doesn't matter which we return.
            return NetworkOs.getsockoptInt(fd, IPPROTO_IPV6, IPV6_MULTICAST_HOPS);
        case SocketOptions.IP_TOS:
            // Since setting this from java.net always sets IPv4 and IPv6 to the same value,
            // it doesn't matter which we return.
            return NetworkOs.getsockoptInt(fd, IPPROTO_IPV6, IPV6_TCLASS);
        case SocketOptions.SO_BROADCAST:
            return booleanFromInt(NetworkOs.getsockoptInt(fd, SOL_SOCKET, SO_BROADCAST));
        case SocketOptions.SO_KEEPALIVE:
            return booleanFromInt(NetworkOs.getsockoptInt(fd, SOL_SOCKET, SO_KEEPALIVE));
        case SocketOptions.SO_LINGER:
            StructLinger linger = NetworkOs.getsockoptLinger(fd, SOL_SOCKET, SO_LINGER);
            if (!linger.isOn()) {
                return false;
            }
            return linger.l_linger;
        case SocketOptions.SO_OOBINLINE:
            return booleanFromInt(NetworkOs.getsockoptInt(fd, SOL_SOCKET, SO_OOBINLINE));
        case SocketOptions.SO_RCVBUF:
            return NetworkOs.getsockoptInt(fd, SOL_SOCKET, SO_RCVBUF);
        case SocketOptions.SO_REUSEADDR:
            return booleanFromInt(NetworkOs.getsockoptInt(fd, SOL_SOCKET, SO_REUSEADDR));
        case SocketOptions.SO_SNDBUF:
            return NetworkOs.getsockoptInt(fd, SOL_SOCKET, SO_SNDBUF);
        case SocketOptions.SO_TIMEOUT:
            return (int) NetworkOs.getsockoptTimeval(fd, SOL_SOCKET, SO_RCVTIMEO).toMillis();
        case SocketOptions.TCP_NODELAY:
            return booleanFromInt(NetworkOs.getsockoptInt(fd, IPPROTO_TCP, TCP_NODELAY));
        default:
            throw new SocketException("Unknown socket option: " + option);
        }
    }

    private static boolean booleanFromInt(int i) {
        return (i != 0);
    }

    private static int booleanToInt(boolean b) {
        return b ? 1 : 0;
    }

    /**
     * java.net has its own socket options similar to the underlying Unix ones. We paper over the
     * differences here.
     */
    public static void setSocketOption(FileDescriptor fd, int option, Object value) throws SocketException {
        try {
            setSocketOptionErrno(fd, option, value);
        } catch (ErrnoException errnoException) {
            throw new SocketException(errnoException.getMessage(), errnoException);
        }
    }

    private static void setSocketOptionErrno(FileDescriptor fd, int option, Object value) throws ErrnoException, SocketException {
        switch (option) {
        case SocketOptions.IP_MULTICAST_IF:
            throw new UnsupportedOperationException("Use IP_MULTICAST_IF2 on Android");
        case SocketOptions.IP_MULTICAST_IF2:
            // Although IPv6 was cleaned up to use int, IPv4 uses an ip_mreqn containing an int.
            NetworkOs.setsockoptIpMreqn(fd, IPPROTO_IP, IP_MULTICAST_IF, (Integer) value);
            NetworkOs.setsockoptInt(fd, IPPROTO_IPV6, IPV6_MULTICAST_IF, (Integer) value);
            return;
        case SocketOptions.IP_MULTICAST_LOOP:
            // Although IPv6 was cleaned up to use int, IPv4 multicast loopback uses a byte.
            NetworkOs.setsockoptByte(fd, IPPROTO_IP, IP_MULTICAST_LOOP, booleanToInt((Boolean) value));
            NetworkOs.setsockoptInt(fd, IPPROTO_IPV6, IPV6_MULTICAST_LOOP, booleanToInt((Boolean) value));
            return;
        case NetworkBridge.JAVA_IP_MULTICAST_TTL:
            // Although IPv6 was cleaned up to use int, and IPv4 non-multicast TTL uses int,
            // IPv4 multicast TTL uses a byte.
            NetworkOs.setsockoptByte(fd, IPPROTO_IP, IP_MULTICAST_TTL, (Integer) value);
            NetworkOs.setsockoptInt(fd, IPPROTO_IPV6, IPV6_MULTICAST_HOPS, (Integer) value);
            return;
        case SocketOptions.IP_TOS:
            NetworkOs.setsockoptInt(fd, IPPROTO_IP, IP_TOS, (Integer) value);
            NetworkOs.setsockoptInt(fd, IPPROTO_IPV6, IPV6_TCLASS, (Integer) value);
            return;
        case SocketOptions.SO_BROADCAST:
            NetworkOs.setsockoptInt(fd, SOL_SOCKET, SO_BROADCAST, booleanToInt((Boolean) value));
            return;
        case SocketOptions.SO_KEEPALIVE:
            NetworkOs.setsockoptInt(fd, SOL_SOCKET, SO_KEEPALIVE, booleanToInt((Boolean) value));
            return;
        case SocketOptions.SO_LINGER:
            boolean on = false;
            int seconds = 0;
            if (value instanceof Integer) {
                on = true;
                seconds = Math.min((Integer) value, 65535);
            }
            StructLinger linger = new StructLinger(booleanToInt(on), seconds);
            NetworkOs.setsockoptLinger(fd, SOL_SOCKET, SO_LINGER, linger);
            return;
        case SocketOptions.SO_OOBINLINE:
            NetworkOs.setsockoptInt(fd, SOL_SOCKET, SO_OOBINLINE, booleanToInt((Boolean) value));
            return;
        case SocketOptions.SO_RCVBUF:
            NetworkOs.setsockoptInt(fd, SOL_SOCKET, SO_RCVBUF, (Integer) value);
            return;
        case SocketOptions.SO_REUSEADDR:
            NetworkOs.setsockoptInt(fd, SOL_SOCKET, SO_REUSEADDR, booleanToInt((Boolean) value));
            return;
        case SocketOptions.SO_SNDBUF:
            NetworkOs.setsockoptInt(fd, SOL_SOCKET, SO_SNDBUF, (Integer) value);
            return;
        case SocketOptions.SO_TIMEOUT:
            int millis = (Integer) value;
            StructTimeval tv = StructTimeval.fromMillis(millis);
            NetworkOs.setsockoptTimeval(fd, SOL_SOCKET, SO_RCVTIMEO, tv);
            return;
        case SocketOptions.TCP_NODELAY:
            NetworkOs.setsockoptInt(fd, IPPROTO_TCP, TCP_NODELAY, booleanToInt((Boolean) value));
            return;
        case NetworkBridge.JAVA_MCAST_JOIN_GROUP:
        case NetworkBridge.JAVA_MCAST_LEAVE_GROUP:
        {
            StructGroupReq groupReq = (StructGroupReq) value;
            int level = (groupReq.gr_group instanceof Inet4Address) ? IPPROTO_IP : IPPROTO_IPV6;
            int op = (option == JAVA_MCAST_JOIN_GROUP) ? MCAST_JOIN_GROUP : MCAST_LEAVE_GROUP;
            NetworkOs.setsockoptGroupReq(fd, level, op, groupReq);
            return;
        }
        case NetworkBridge.JAVA_MCAST_JOIN_SOURCE_GROUP:
        case NetworkBridge.JAVA_MCAST_LEAVE_SOURCE_GROUP:
        case NetworkBridge.JAVA_MCAST_BLOCK_SOURCE:
        case NetworkBridge.JAVA_MCAST_UNBLOCK_SOURCE:
        {
            StructGroupSourceReq groupSourceReq = (StructGroupSourceReq) value;
            int level = (groupSourceReq.gsr_group instanceof Inet4Address)
                ? IPPROTO_IP : IPPROTO_IPV6;
            int op = getGroupSourceReqOp(option);
            NetworkOs.setsockoptGroupSourceReq(fd, level, op, groupSourceReq);
            return;
        }
        default:
            throw new SocketException("Unknown socket option: " + option);
        }
    }

    private static int getGroupSourceReqOp(int javaValue) {
        switch (javaValue) {
            case NetworkBridge.JAVA_MCAST_JOIN_SOURCE_GROUP:
                return MCAST_JOIN_SOURCE_GROUP;
            case NetworkBridge.JAVA_MCAST_LEAVE_SOURCE_GROUP:
                return MCAST_LEAVE_SOURCE_GROUP;
            case NetworkBridge.JAVA_MCAST_BLOCK_SOURCE:
                return MCAST_BLOCK_SOURCE;
            case NetworkBridge.JAVA_MCAST_UNBLOCK_SOURCE:
                return MCAST_UNBLOCK_SOURCE;
            default:
                throw new AssertionError(
                        "Unknown java value for setsocketopt op lookup: " + javaValue);
        }
    }

    public static int sendto(FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, InetAddress inetAddress, int port) throws IOException {
        boolean isDatagram = (inetAddress != null);
        if (!isDatagram && byteCount <= 0) {
            return 0;
        }
        int result;
        try {
            result = NetworkOs.sendto(fd, bytes, byteOffset, byteCount, flags, inetAddress, port);
        } catch (ErrnoException errnoException) {
            result = maybeThrowAfterSendto(isDatagram, errnoException);
        }
        return result;
    }

    public static int sendto(FileDescriptor fd, ByteBuffer buffer, int flags, InetAddress inetAddress, int port) throws IOException {
        boolean isDatagram = (inetAddress != null);
        if (!isDatagram && buffer.remaining() == 0) {
            return 0;
        }
        int result;
        try {
            result = NetworkOs.sendto(fd, buffer, flags, inetAddress, port);
        } catch (ErrnoException errnoException) {
            result = maybeThrowAfterSendto(isDatagram, errnoException);
        }
        return result;
    }

    private static int maybeThrowAfterSendto(boolean isDatagram, ErrnoException errnoException) throws SocketException {
        if (isDatagram) {
            if (errnoException.errno == ECONNRESET || errnoException.errno == ECONNREFUSED) {
                return 0;
            }
        } else {
            if (errnoException.errno == EAGAIN) {
                // We were asked to write to a non-blocking socket, but were told
                // it would block, so report "no bytes written".
                return 0;
            }
        }
        throw new SocketException(errnoException.getMessage(), errnoException);
    }

    public static int recvfrom(boolean isRead, FileDescriptor fd, byte[] bytes, int byteOffset, int byteCount, int flags, DatagramPacket packet, boolean isConnected) throws IOException {
        int result;
        try {
            InetSocketAddress srcAddress = (packet != null && !isConnected) ? new InetSocketAddress() : null;
            result = NetworkOs.recvfrom(fd, bytes, byteOffset, byteCount, flags, srcAddress);
            result = postRecvfrom(isRead, packet, isConnected, srcAddress, result);
        } catch (ErrnoException errnoException) {
            result = maybeThrowAfterRecvfrom(isRead, isConnected, errnoException);
        }
        return result;
    }

    public static int recvfrom(boolean isRead, FileDescriptor fd, ByteBuffer buffer, int flags, DatagramPacket packet, boolean isConnected) throws IOException {
        int result;
        try {
            InetSocketAddress srcAddress = (packet != null && !isConnected) ? new InetSocketAddress() : null;
            result = NetworkOs.recvfrom(fd, buffer, flags, srcAddress);
            result = postRecvfrom(isRead, packet, isConnected, srcAddress, result);
        } catch (ErrnoException errnoException) {
            result = maybeThrowAfterRecvfrom(isRead, isConnected, errnoException);
        }
        return result;
    }

    private static int postRecvfrom(boolean isRead, DatagramPacket packet, boolean isConnected, InetSocketAddress srcAddress, int byteCount) {
        if (isRead && byteCount == 0) {
            return -1;
        }
        if (packet != null) {
            packet.setReceivedLength(byteCount);
            if (!isConnected) {
                packet.setAddress(srcAddress.getAddress());
                packet.setPort(srcAddress.getPort());
            }
        }
        return byteCount;
    }

    private static int maybeThrowAfterRecvfrom(boolean isRead, boolean isConnected, ErrnoException errnoException) throws SocketException, SocketTimeoutException {
        if (isRead) {
            if (errnoException.errno == EAGAIN) {
                return 0;
            } else {
                throw new SocketException(errnoException.getMessage(), errnoException);
            }
        } else {
            if (isConnected && errnoException.errno == ECONNREFUSED) {
                throw new PortUnreachableException("", errnoException);
            } else if (errnoException.errno == EAGAIN) {
                throw new SocketTimeoutException(errnoException);
            } else {
                throw new SocketException(errnoException.getMessage(), errnoException);
            }
        }
    }

    public static FileDescriptor socket(boolean stream) throws SocketException {
        FileDescriptor fd;
        try {
            fd = Libcore.os.socket(AF_INET6, stream ? SOCK_STREAM : SOCK_DGRAM, 0);

            // The RFC (http://www.ietf.org/rfc/rfc3493.txt) says that IPV6_MULTICAST_HOPS defaults
            // to 1. The Linux kernel (at least up to 2.6.38) accidentally defaults to 64 (which
            // would be correct for the *unicast* hop limit).
            // See http://www.spinics.net/lists/netdev/msg129022.html, though no patch appears to
            // have been applied as a result of that discussion. If that bug is ever fixed, we can
            // remove this code. Until then, we manually set the hop limit on IPv6 datagram sockets.
            // (IPv4 is already correct.)
            if (!stream) {
                NetworkOs.setsockoptInt(fd, IPPROTO_IPV6, IPV6_MULTICAST_HOPS, 1);
            }

            return fd;
        } catch (ErrnoException errnoException) {
            throw new SocketException(errnoException.getMessage(), errnoException);
        }
    }

    public static InetAddress getSocketLocalAddress(FileDescriptor fd) throws SocketException {
        try {
            SocketAddress sa = NetworkOs.getsockname(fd);
            InetSocketAddress isa = (InetSocketAddress) sa;
            return isa.getAddress();
        } catch (ErrnoException errnoException) {
            throw new SocketException(errnoException.getMessage(), errnoException);
        }
    }

    public static int getSocketLocalPort(FileDescriptor fd) throws SocketException {
        try {
            SocketAddress sa = NetworkOs.getsockname(fd);
            InetSocketAddress isa = (InetSocketAddress) sa;
            return isa.getPort();
        } catch (ErrnoException errnoException) {
            throw new SocketException(errnoException.getMessage(), errnoException);
        }
    }
}
