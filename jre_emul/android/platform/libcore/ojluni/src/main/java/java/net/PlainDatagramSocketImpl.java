/*
 * Copyright (c) 2007,2011, Oracle and/or its affiliates. All rights reserved.
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

/* J2ObjC removed
import android.system.ErrnoException;
import android.system.StructGroupReq;


import libcore.io.IoBridge;
import libcore.io.Libcore;
import libcore.util.EmptyArray;

import jdk.net.*;

import static android.system.OsConstants.AF_INET6;
import static android.system.OsConstants.AF_UNSPEC;
import static android.system.OsConstants.IPPROTO_IP;
import static android.system.OsConstants.IP_MULTICAST_ALL;
import static android.system.OsConstants.MSG_PEEK;
import static android.system.OsConstants.POLLERR;
import static android.system.OsConstants.POLLIN;
import static android.system.OsConstants.SOCK_DGRAM;
import static libcore.io.IoBridge.JAVA_IP_MULTICAST_TTL;
import static libcore.io.IoBridge.JAVA_MCAST_JOIN_GROUP;
import static libcore.io.IoBridge.JAVA_MCAST_LEAVE_GROUP;
import static sun.net.ExtendedOptionsImpl.*;
*/

// Android-changed: Rewritten to use android.system POSIX calls and assume AF_INET6.
/*
 * On Unix systems we simply delegate to native methods.
 *
 * @author Chris Hegarty
 */

class PlainDatagramSocketImpl extends AbstractPlainDatagramSocketImpl
{
    static {
        init();
    }

    protected synchronized native void bind0(int lport, InetAddress laddr)
        throws SocketException;

    protected native void send(DatagramPacket p) throws IOException;

    protected synchronized native int peek(InetAddress i) throws IOException;

    protected synchronized native int peekData(DatagramPacket p) throws IOException;

    protected synchronized native void receive0(DatagramPacket p)
        throws IOException;

    protected native void setTimeToLive(int ttl) throws IOException;

    protected native int getTimeToLive() throws IOException;

    protected native void setTTL(byte ttl) throws IOException;

    protected native byte getTTL() throws IOException;

    protected native void join(InetAddress inetaddr, NetworkInterface netIf)
        throws IOException;

    protected native void leave(InetAddress inetaddr, NetworkInterface netIf)
        throws IOException;

    protected native void datagramSocketCreate() throws SocketException;

    protected native void datagramSocketClose();

    protected native void socketSetOption(int opt, Object val)
        throws SocketException;

    protected native Object socketGetOption(int opt) throws SocketException;

    protected native void connect0(InetAddress address, int port) throws SocketException;

    protected native void disconnect0(int family);

    /**
     * Perform class load-time initializations.
     */
    private native static void init();
}
