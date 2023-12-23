/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import libcore.io.IoUtils;
import libcore.io.Libcore;
/* J2ObjC removed.
import android.compat.Compatibility;
import android.compat.annotation.ChangeId;
import android.compat.annotation.EnabledSince;
import dalvik.annotation.compat.VersionCodes;
import android.system.StructIfaddrs;
import sun.security.action.*;
import java.security.AccessController;
 */

import static libcore.io.OsConstants.*;

// Android-note: NetworkInterface has been rewritten to avoid native code.
// Fix upstream bug not returning link-down interfaces. http://b/26238832
// Android-added: Document restrictions for non-system apps. http://b/170188668
/**
 * This class represents a Network Interface made up of a name,
 * and a list of IP addresses assigned to this interface.
 * It is used to identify the local interface on which a multicast group
 * is joined.
 *
 * Interfaces are normally known by names such as "le0".
 * <p>
 * <a name="access-restrictions"></a>Note that information about
 * {@link NetworkInterface}s may be restricted. For example, non-system apps
 * will only have access to information about {@link NetworkInterface}s that are
 * associated with an {@link InetAddress}.
 *
 * @since 1.4
 */
public final class NetworkInterface {
    // Android-added: Anonymized address for apps targeting old API versions. http://b/170188668
    /**
     * If this change is enabled, {@link #getHardwareAddress()} returns null when the hardware
     * address is <a href="#access-restrictions">inaccessible</a>. If the change is disabled, the
     * default MAC address (02:00:00:00:00:00) is returned instead.
     *
     * @hide
     */
    /* J2ObjC removed
    @ChangeId
    @EnabledSince(targetSdkVersion=VersionCodes.R)
    */
    public static final long RETURN_NULL_HARDWARE_ADDRESS = 170188668L;
    // The default hardware address is a zeroed-out MAC address with only its
    // locally-administered bit set, returned to apps targeting older API versions if they would
    // otherwise see a null MAC address.
    // Matches android.net.wifi.WifiInfo.DEFAULT_MAC_ADDRESS
    private static final byte[] DEFAULT_MAC_ADDRESS = {
        0x02, 0x00, 0x00, 0x00, 0x00, 0x00 };

    private String name;
    private String displayName;
    private int index;
    private InetAddress addrs[];
    private InterfaceAddress bindings[];
    // Android-changed: Rewrote NetworkInterface on top of Libcore.io.
    // private NetworkInterface childs[];
    private List<NetworkInterface> childs;
    private NetworkInterface parent = null;
    private boolean virtual = false;
    private static final NetworkInterface defaultInterface;
    private static final int defaultIndex; /* index of defaultInterface */

    // Android-changed: Fix upstream bug not returning link-down interfaces. http://b/26238832
    private byte[] hardwareAddr;

    static {
        // Android-removed: Android doesn't need to call native init.
        /*
        AccessController.doPrivileged(
            new java.security.PrivilegedAction<Void>() {
                public Void run() {
                    System.loadLibrary("net");
                    return null;
                }
            });
        */
        /* J2ObjC added. */
        init();

        defaultInterface = DefaultInterface.getDefault();
        if (defaultInterface != null) {
            defaultIndex = defaultInterface.getIndex();
        } else {
            defaultIndex = 0;
        }
    }

    /**
     * Returns an NetworkInterface object with index set to 0 and name to null.
     * Setting such an interface on a MulticastSocket will cause the
     * kernel to choose one interface for sending multicast packets.
     *
     */
    NetworkInterface() {
    }

    NetworkInterface(String name, int index, InetAddress[] addrs) {
        this.name = name;
        this.index = index;
        this.addrs = addrs;
    }

    /**
     * Get the name of this network interface.
     *
     * @return the name of this network interface
     */
    public String getName() {
            return name;
    }

    /**
     * Convenience method to return an Enumeration with all or a
     * subset of the InetAddresses bound to this network interface.
     * <p>
     * If there is a security manager, its {@code checkConnect}
     * method is called for each InetAddress. Only InetAddresses where
     * the {@code checkConnect} doesn't throw a SecurityException
     * will be returned in the Enumeration. However, if the caller has the
     * {@link NetPermission}("getNetworkInformation") permission, then all
     * InetAddresses are returned.
     * @return an Enumeration object with all or a subset of the InetAddresses
     * bound to this network interface
     */
    public Enumeration<InetAddress> getInetAddresses() {

        class checkedAddresses implements Enumeration<InetAddress> {

            private int i=0, count=0;
            private InetAddress local_addrs[];

            checkedAddresses() {
                local_addrs = new InetAddress[addrs.length];
                boolean trusted = true;

                SecurityManager sec = System.getSecurityManager();
                if (sec != null) {
                    try {
                        sec.checkPermission(new NetPermission("getNetworkInformation"));
                    } catch (SecurityException e) {
                        trusted = false;
                    }
                }
                for (int j=0; j<addrs.length; j++) {
                    try {
                        if (sec != null && !trusted) {
                            sec.checkConnect(addrs[j].getHostAddress(), -1);
                        }
                        local_addrs[count++] = addrs[j];
                    } catch (SecurityException e) { }
                }

            }

            public InetAddress nextElement() {
                if (i < count) {
                    return local_addrs[i++];
                } else {
                    throw new NoSuchElementException();
                }
            }

            public boolean hasMoreElements() {
                return (i < count);
            }
        }
        return new checkedAddresses();

    }

    /**
     * Get a List of all or a subset of the {@code InterfaceAddresses}
     * of this network interface.
     * <p>
     * If there is a security manager, its {@code checkConnect}
     * method is called with the InetAddress for each InterfaceAddress.
     * Only InterfaceAddresses where the {@code checkConnect} doesn't throw
     * a SecurityException will be returned in the List.
     *
     * @return a {@code List} object with all or a subset of the
     *         InterfaceAddresss of this network interface
     * @since 1.6
     */
    public java.util.List<InterfaceAddress> getInterfaceAddresses() {
        java.util.List<InterfaceAddress> lst = new java.util.ArrayList<InterfaceAddress>(1);
        // BEGIN Android-changed: Cherry-picked upstream OpenJDK9 change rev 59a110a38cea
        // http://b/30628919
        if (bindings != null) {
            SecurityManager sec = System.getSecurityManager();
            for (int j=0; j<bindings.length; j++) {
                try {
                    if (sec != null) {
                        sec.checkConnect(bindings[j].getAddress().getHostAddress(), -1);
                    }
                    lst.add(bindings[j]);
                } catch (SecurityException e) { }
            }
        }
        // END Android-changed: Cherry-picked upstream OpenJDK9 change rev 59a110a38cea
        return lst;
    }

    /**
     * Get an Enumeration with all the subinterfaces (also known as virtual
     * interfaces) attached to this network interface.
     * <p>
     * For instance eth0:1 will be a subinterface to eth0.
     *
     * @return an Enumeration object with all of the subinterfaces
     * of this network interface
     * @since 1.6
     */
    public Enumeration<NetworkInterface> getSubInterfaces() {
        // Android-changed: Rewrote NetworkInterface on top of Libcore.io.
        return Collections.enumeration(childs);
    }

    /**
     * Returns the parent NetworkInterface of this interface if this is
     * a subinterface, or {@code null} if it is a physical
     * (non virtual) interface or has no parent.
     *
     * @return The {@code NetworkInterface} this interface is attached to.
     * @since 1.6
     */
    public NetworkInterface getParent() {
        return parent;
    }

    /**
     * Returns the index of this network interface. The index is an integer greater
     * or equal to zero, or {@code -1} for unknown. This is a system specific value
     * and interfaces with the same name can have different indexes on different
     * machines.
     *
     * @return the index of this network interface or {@code -1} if the index is
     *         unknown
     * @see #getByIndex(int)
     * @since 1.7
     */
    public int getIndex() {
        return index;
    }

    /**
     * Get the display name of this network interface.
     * A display name is a human readable String describing the network
     * device.
     *
     * @return a non-empty string representing the display name of this network
     *         interface, or null if no display name is available.
     */
    public String getDisplayName() {
        /* strict TCK conformance */
        return "".equals(displayName) ? null : displayName;
    }

    // Android-added: Document restrictions for non-system apps. http://b/170188668
    /**
     * Searches for the network interface with the specified name.
     *
     * @param   name
     *          The name of the network interface.
     *
     * @return  A {@code NetworkInterface} with the specified name,
     *          or {@code null} if the network interface with the specified
     *          name does not exist or <a href="#access-restrictions">can't be
     *          accessed</a>.
     *
     * @throws  SocketException
     *          If an I/O error occurs.
     *
     * @throws  NullPointerException
     *          If the specified name is {@code null}.
     */
    public static NetworkInterface getByName(String name) throws SocketException {
        if (name == null)
            throw new NullPointerException();

        /* J2ObjC modified: use native method.
        // Android-changed: Rewrote NetworkInterface on top of Libcore.io.
        NetworkInterface[] nis = getAll();
        for (NetworkInterface ni : nis) {
            if (ni.getName().equals(name)) {
                return ni;
            }
        }
        return null;
         */
        return getByName0(name);
    }

    // Android-added: Document restrictions for non-system apps. http://b/170188668
    /**
     * Get a network interface given its index.
     *
     * @param index an integer, the index of the interface
     * @return the NetworkInterface obtained from its index, or {@code null} if
     *         an interface with the specified index does not exist or
     *         <a href="#access-restrictions">can't be accessed</a>.
     * @throws  SocketException  if an I/O error occurs.
     * @throws  IllegalArgumentException if index has a negative value
     * @see #getIndex()
     * @since 1.7
     */
    public static NetworkInterface getByIndex(int index) throws SocketException {
        if (index < 0)
            throw new IllegalArgumentException("Interface index can't be negative");

        /* J2ObjC modified: use native method.
        // Android-changed: Rewrote NetworkInterface on top of Libcore.io.
        NetworkInterface[] nis = getAll();
        for (NetworkInterface ni : nis) {
            if (ni.getIndex() == index) {
                return ni;
            }
        }
        return null;
         */
        return getByIndex0(index);
    }

    /**
     * Convenience method to search for a network interface that
     * has the specified Internet Protocol (IP) address bound to
     * it.
     * <p>
     * If the specified IP address is bound to multiple network
     * interfaces it is not defined which network interface is
     * returned.
     *
     * @param   addr
     *          The {@code InetAddress} to search with.
     *
     * @return  A {@code NetworkInterface}
     *          or {@code null} if there is no network interface
     *          with the specified IP address.
     *
     * @throws  SocketException
     *          If an I/O error occurs.
     *
     * @throws  NullPointerException
     *          If the specified address is {@code null}.
     */
    public static NetworkInterface getByInetAddress(InetAddress addr) throws SocketException {
        if (addr == null) {
            throw new NullPointerException();
        }
        if (!(addr instanceof Inet4Address || addr instanceof Inet6Address)) {
            throw new IllegalArgumentException ("invalid address type");
        }

        /* J2ObjC modified: use native method.
        // Android-changed: Rewrote NetworkInterface on top of Libcore.io.
        NetworkInterface[] nis = getAll();
        for (NetworkInterface ni : nis) {
            for (InetAddress inetAddress : Collections.list(ni.getInetAddresses())) {
                if (inetAddress.equals(addr)) {
                    return ni;
                }
            }
        }
        return null;
         */
        return getByInetAddress0(addr);
    }

    // Android-added: Document restrictions for non-system apps. http://b/170188668
    // Android-added: Note about NullPointerException in older versions. http://b/206053582
    /**
     * Returns all the interfaces on this machine. The {@code Enumeration}
     * contains at least one element, possibly representing a loopback
     * interface that only supports communication between entities on
     * this machine.
     *
     * NOTE: can use getNetworkInterfaces()+getInetAddresses()
     *       to obtain all IP addresses for this node
     * <p>
     * For non-system apps, this method will only return information for
     * {@link NetworkInterface}s associated with an {@link InetAddress}.
     * <p>
     * ANDROID NOTE: On Android versions before S (API level 31), this method may throw a
     *               NullPointerException if called in an environment where there is a virtual
     *               interface without a parent interface present.
     *
     * @return an Enumeration of NetworkInterfaces found on this machine
     *         that <a href="#access-restrictions">are accessible</a>.
     * @exception  SocketException  if an I/O error occurs.
     */

    public static Enumeration<NetworkInterface> getNetworkInterfaces()
        throws SocketException {
        final NetworkInterface[] netifs = getAll();
        // Android-changed: Rewrote NetworkInterface on top of Libcore.io.
        // // specified to return null if no network interfaces
        // if (netifs == null)
        if (netifs.length == 0)
            return null;

        // Android-changed: Rewrote NetworkInterface on top of Libcore.io.
        /*
        return new Enumeration<NetworkInterface>() {
            private int i = 0;
            public NetworkInterface nextElement() {
                if (netifs != null && i < netifs.length) {
                    NetworkInterface netif = netifs[i++];
                    return netif;
                } else {
                    throw new NoSuchElementException();
                }
            }

            public boolean hasMoreElements() {
                return (netifs != null && i < netifs.length);
            }
        };
        */
        return Collections.enumeration(Arrays.asList(netifs));
    }

    /* J2ObjC removed: use native implementation.
    // BEGIN Android-changed: Rewrote NetworkInterface on top of Libcore.io.
    // private native static NetworkInterface[] getAll()
    //    throws SocketException;
    private static NetworkInterface[] getAll() throws SocketException {
        // Group Ifaddrs by interface name.
        Map<String, List<StructIfaddrs>> inetMap = new HashMap<>();

        StructIfaddrs[] ifaddrs;
        try {
            ifaddrs = Libcore.os.getifaddrs();
            // Defensive check for b/217749090: ifaddrs should never be null.
            if (ifaddrs == null) {
                throw new SocketException("Failed to query network interfaces.");
            }
        } catch (ErrnoException e) {
            throw e.rethrowAsSocketException();
        }

        for (StructIfaddrs ifa : ifaddrs) {
            String name = ifa.ifa_name;

            List<StructIfaddrs> ifas;
            if ((ifas = inetMap.get(name)) == null) {
                ifas = new ArrayList<>();
                inetMap.put(name, ifas);
            }

            ifas.add(ifa);
        }

        // Populate NetworkInterface instances.
        Map<String, NetworkInterface> nis = new HashMap<>(inetMap.size());
        for (Map.Entry<String, List<StructIfaddrs>> e : inetMap.entrySet()) {
            String name = e.getKey();
            int index = Libcore.os.if_nametoindex(e.getKey());
            if (index == 0) {
                // This interface has gone away between getifaddrs and if_nametoindex
                continue;
            }

            NetworkInterface ni = new NetworkInterface(name, index, null);
            ni.displayName = name;

            List<InetAddress> addrs = new ArrayList<>();
            List<InterfaceAddress> binds = new ArrayList<>();

            for (StructIfaddrs ifa : e.getValue()) {
                if (ifa.ifa_addr != null) {
                    addrs.add(ifa.ifa_addr);
                    binds.add(new InterfaceAddress(ifa.ifa_addr, (Inet4Address) ifa.ifa_broadaddr,
                                                   ifa.ifa_netmask));
                }

                if (ifa.hwaddr != null) {
                    ni.hardwareAddr = ifa.hwaddr;
                }
            }

            ni.addrs = addrs.toArray(new InetAddress[addrs.size()]);
            ni.bindings = binds.toArray(new InterfaceAddress[binds.size()]);
            ni.childs = new ArrayList<>(0);
            nis.put(name, ni);
        }

        // Populate childs/parent.
        for (Map.Entry<String, NetworkInterface> e : nis.entrySet()) {
            NetworkInterface ni = e.getValue();
            String niName = ni.getName();
            int colonIdx = niName.indexOf(':');
            if (colonIdx != -1) {
                // This is a virtual interface.
                String parentName = niName.substring(0, colonIdx);
                NetworkInterface parent = nis.get(parentName);

                ni.virtual = true;

                if (parent != null) {
                    ni.parent = parent;
                    parent.childs.add(ni);
                }
            }
        }

        return nis.values().toArray(new NetworkInterface[nis.size()]);
    }
    // END Android-changed: Rewrote NetworkInterface on top of Libcore.io.
     */

    /* J2ObjC added: use native methods. */
    private native static NetworkInterface[] getAll()
            throws SocketException;

    private native static NetworkInterface getByName0(String name)
            throws SocketException;

    private native static NetworkInterface getByIndex0(int index)
            throws SocketException;

    private native static NetworkInterface getByInetAddress0(InetAddress addr)
            throws SocketException;


    /**
     * Returns whether a network interface is up and running.
     *
     * @return  {@code true} if the interface is up and running.
     * @exception       SocketException if an I/O error occurs.
     * @since 1.6
     */

    public boolean isUp() throws SocketException {
        /* J2ObjC modified: use native method.
        // Android-changed: Rewrote NetworkInterface on top of Libcore.io.
        final int mask = IFF_UP | IFF_RUNNING;
        return (getFlags() & mask) == mask;
         */
        return isUp0(name, index);
    }

    /**
     * Returns whether a network interface is a loopback interface.
     *
     * @return  {@code true} if the interface is a loopback interface.
     * @exception       SocketException if an I/O error occurs.
     * @since 1.6
     */

    public boolean isLoopback() throws SocketException {
        /* J2ObjC modified: use native method.
        // Android-changed: Rewrote NetworkInterface on top of Libcore.io.
        return (getFlags() & IFF_LOOPBACK) != 0;
         */
        return isLoopback0(name, index);
    }

    /**
     * Returns whether a network interface is a point to point interface.
     * A typical point to point interface would be a PPP connection through
     * a modem.
     *
     * @return  {@code true} if the interface is a point to point
     *          interface.
     * @exception       SocketException if an I/O error occurs.
     * @since 1.6
     */

    public boolean isPointToPoint() throws SocketException {
        /* J2ObjC modified: use native method.
        // Android-changed: Rewrote NetworkInterface on top of Libcore.io.
        return (getFlags() & IFF_POINTOPOINT) != 0;
         */
        return isP2P0(name, index);
    }

    /**
     * Returns whether a network interface supports multicasting or not.
     *
     * @return  {@code true} if the interface supports Multicasting.
     * @exception       SocketException if an I/O error occurs.
     * @since 1.6
     */

    public boolean supportsMulticast() throws SocketException {
        /* J2ObjC modified: use native method.
        // Android-changed: Rewrote NetworkInterface on top of Libcore.io.
        return (getFlags() & IFF_MULTICAST) != 0;
         */
        return supportsMulticast0(name, index);
    }

    // Android-added: Restrictions for non-system apps. http://b/170188668
    /**
     * Returns the hardware address (usually MAC) of the interface if it
     * has one and if it can be accessed given the current privileges.
     * If a security manager is set, then the caller must have
     * the permission {@link NetPermission}("getNetworkInformation").
     *
     * @return  a byte array containing the address, or {@code null} if
     *          the address doesn't exist, is not accessible or a security
     *          manager is set and the caller does not have the permission
     *          NetPermission("getNetworkInformation"). For example, this
     *          method will generally return {@code null} when called by
     *          non-system apps (or 02:00:00:00:00:00 for apps having
     *          {@code targetSdkVersion < android.os.Build.VERSION_CODES.R}).
     *
     * @exception       SocketException if an I/O error occurs.
     * @since 1.6
     */
    public byte[] getHardwareAddress() throws SocketException {
        // BEGIN Android-changed: Fix upstream not returning link-down interfaces. http://b/26238832
        /*
        for (InetAddress addr : addrs) {
            if (addr instanceof Inet4Address) {
                return getMacAddr0(((Inet4Address)addr).getAddress(), name, index);
            }
        }
        return getMacAddr0(null, name, index);
         */
        NetworkInterface ni = getByName(name);
        if (ni == null) {
            throw new SocketException("NetworkInterface doesn't exist anymore");
        }
        
        /* J2ObjC removed
        // Return 02:00:00:00:00:00 for apps having a target SDK version < R if they would have
        // otherwise gotten a null MAC address (excluding loopback).
        if (ni.hardwareAddr == null && !"lo".equals(name)
                && !Compatibility.isChangeEnabled(RETURN_NULL_HARDWARE_ADDRESS)) {
            return DEFAULT_MAC_ADDRESS.clone();
        }
        */
        return ni.hardwareAddr;
        // END Android-changed: Fix upstream not returning link-down interfaces. http://b/26238832
    }

    /**
     * Returns the Maximum Transmission Unit (MTU) of this interface.
     *
     * @return the value of the MTU for that interface.
     * @exception       SocketException if an I/O error occurs.
     * @since 1.6
     */
    public int getMTU() throws SocketException {
        /* J2ObjC modified: use native method.
        // Android-changed: Rewrote NetworkInterface on top of Libcore.io.
        // return getMTU0(name, index);
        FileDescriptor fd = null;
        try {
            fd = Libcore.rawOs.socket(AF_INET, SOCK_DGRAM, 0);
            return Libcore.rawOs.ioctlMTU(fd, name);
        } catch (ErrnoException e) {
            throw e.rethrowAsSocketException();
        } catch (Exception ex) {
            throw new SocketException(ex);
        } finally {
            IoUtils.closeQuietly(fd);
        }
         */
        return getMTU0(name, index);
    }

    /**
     * Returns whether this interface is a virtual interface (also called
     * subinterface).
     * Virtual interfaces are, on some systems, interfaces created as a child
     * of a physical interface and given different settings (like address or
     * MTU). Usually the name of the interface will the name of the parent
     * followed by a colon (:) and a number identifying the child since there
     * can be several virtual interfaces attached to a single physical
     * interface.
     *
     * @return {@code true} if this interface is a virtual interface.
     * @since 1.6
     */
    public boolean isVirtual() {
        return virtual;
    }

    /* J2ObjC added: native implementation. */
    private native static void init();
    private native static boolean isUp0(String name, int ind) throws SocketException;
    private native static boolean isLoopback0(String name, int ind) throws SocketException;
    private native static boolean supportsMulticast0(String name, int ind) throws SocketException;
    private native static boolean isP2P0(String name, int ind) throws SocketException;
    private native static byte[] getMacAddr0(byte[] inAddr, String name, int ind) throws SocketException;
    private native static int getMTU0(String name, int ind) throws SocketException;

    /* J2ObjC removed: use native methods instead.
    // BEGIN Android-added: Rewrote NetworkInterface on top of Libcore.io.
    private int getFlags() throws SocketException {
        FileDescriptor fd = null;
        try {
            fd = Libcore.rawOs.socket(AF_INET, SOCK_DGRAM, 0);
            return Libcore.rawOs.ioctlFlags(fd, name);
        } catch (ErrnoException e) {
            throw e.rethrowAsSocketException();
        } catch (Exception ex) {
            throw new SocketException(ex);
        } finally {
            IoUtils.closeQuietly(fd);
        }
    }
    // END Android-added: Rewrote NetworkInterface on top of Libcore.io.
     */

    /**
     * Compares this object against the specified object.
     * The result is {@code true} if and only if the argument is
     * not {@code null} and it represents the same NetworkInterface
     * as this object.
     * <p>
     * Two instances of {@code NetworkInterface} represent the same
     * NetworkInterface if both name and addrs are the same for both.
     *
     * @param   obj   the object to compare against.
     * @return  {@code true} if the objects are the same;
     *          {@code false} otherwise.
     * @see     java.net.InetAddress#getAddress()
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof NetworkInterface)) {
            return false;
        }
        NetworkInterface that = (NetworkInterface)obj;
        if (this.name != null ) {
            if (!this.name.equals(that.name)) {
                return false;
            }
        } else {
            if (that.name != null) {
                return false;
            }
        }

        if (this.addrs == null) {
            return that.addrs == null;
        } else if (that.addrs == null) {
            return false;
        }

        /* Both addrs not null. Compare number of addresses */

        if (this.addrs.length != that.addrs.length) {
            return false;
        }

        InetAddress[] thatAddrs = that.addrs;
        int count = thatAddrs.length;

        for (int i=0; i<count; i++) {
            boolean found = false;
            for (int j=0; j<count; j++) {
                if (addrs[i].equals(thatAddrs[j])) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        return name == null? 0: name.hashCode();
    }

    public String toString() {
        String result = "name:";
        result += name == null? "null": name;
        if (displayName != null) {
            result += " (" + displayName + ")";
        }
        return result;
    }

    // Android-removed: Android doesn't need to call native init.
    // private static native void init();

    /**
     * Returns the default network interface of this system
     *
     * @return the default interface
     */
    static NetworkInterface getDefault() {
        return defaultInterface;
    }
}
