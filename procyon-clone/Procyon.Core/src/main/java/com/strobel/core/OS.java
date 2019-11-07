/**
 * JLibs: Common Utilities for Java
 * Copyright (C) 2009  Santhosh Kumar T <santhosh.tekuri@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */

package com.strobel.core;

import com.strobel.util.ContractUtils;

public enum OS {
    WINDOWS_NT("Windows NT"),
    WINDOWS_95("Windows 95"),
    WINDOWS_98("Windows 98"),
    WINDOWS_2000("Windows 2000"),
    WINDOWS_VISTA("Windows Vista"),
    WINDOWS_7("Windows 7"),
    WINDOWS_OTHER("Windows"),

    SOLARIS("Solaris"),
    LINUX("Linux"),
    HP_UX("HP-UX"),
    IBM_AIX("AIX"),
    SGI_IRIX("Irix"),
    SUN_OS("SunOS"),
    COMPAQ_TRU64_UNIX("Digital UNIX"),
    MAC("Mac OS X", "Darwin"),
    FREE_BSD("freebsd"),
    // add new unix versions here

    OS2("OS/2"),
    COMPAQ_OPEN_VMS("OpenVMS"),

    /**
     * Unrecognized OS
     */
    OTHER("");

    private final String names[];

    private OS(final String... names) {
        this.names = names;
    }

    /**
     * @return true if this OS belongs to windows family
     */
    public boolean isWindows() {
        return ordinal() <= WINDOWS_OTHER.ordinal();
    }

    /**
     * @return true if this OS belongs to *nix family
     */
    public boolean isUnix() {
        return ordinal() > WINDOWS_OTHER.ordinal() && ordinal() < OS2.ordinal();
    }

    /**
     * @param osName name of OS as returned by <code>System.getProperty("os.name")</code>
     *
     * @return OS for the specified {@code osName}
     */
    public static OS get(String osName) {
        osName = osName.toLowerCase();
        for (final OS os : values()) {
            for (final String name : os.names) {
                if (osName.contains(name.toLowerCase())) {
                    return os;
                }
            }
        }
        throw ContractUtils.unreachable();
    }

    private static OS current;

    /**
     * @return OS on which this JVM is running
     */
    public static OS get() {
        if (current == null) {
            current = get(System.getProperty("os.name"));
        }
        return current;
    }
}