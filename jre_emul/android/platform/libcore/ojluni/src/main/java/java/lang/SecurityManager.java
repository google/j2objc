/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1995, 2018, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

import java.security.*;
import java.io.FileDescriptor;
import java.net.InetAddress;

// Android-changed: Stubbed the implementation.  Android doesn't support SecurityManager.
// SecurityManager can only check access by Java code, so it can be bypassed by using
// native code.  Applications should rely on Android permissions, process separation,
// other other methods for security purposes.
/**
 * Legacy security code; do not use.
 *
 * <p>Security managers do <i>not</i> provide a secure environment for
 * executing untrusted code and are unsupported on Android. Untrusted code
 * cannot be safely isolated within a single VM on Android. Application
 * developers can assume that there's no SecurityManager installed,
 * i.e. {@link java.lang.System#getSecurityManager()} will return null.
 */
public
class SecurityManager {

    /**
     * @deprecated Use {@link #checkPermission} instead.
     */
    @Deprecated
    protected boolean inCheck;

    /**
     * @deprecated Use {@link #checkPermission} instead.
     */
    @Deprecated
    public boolean getInCheck() {
        return inCheck;
    }

    public SecurityManager() {

    }

    protected Class[] getClassContext() {
        return null;
    }

    /**
     * @deprecated Use {@link #checkPermission} instead.
     */
    @Deprecated
    protected ClassLoader currentClassLoader()
    {
        return null;
    }

    /**
     * @deprecated Use {@link #checkPermission} instead.
     */
    @Deprecated
    protected Class<?> currentLoadedClass() {
        return null;
    }

    /**
     * @deprecated Use {@link #checkPermission} instead.
     */
    @Deprecated
    protected int classDepth(String name) {
        return -1;
    }

    /**
     * @deprecated Use {@link #checkPermission} instead.
     */
    @Deprecated
    protected int classLoaderDepth()
    {
        return -1;
    }

    /**
     * @deprecated Use {@link #checkPermission} instead.
     */
    @Deprecated
    protected boolean inClass(String name) {
        return false;
    }

    /**
     * @deprecated Use {@link #checkPermission} instead.
     */
    @Deprecated
    protected boolean inClassLoader() {
        return false;
    }

    public Object getSecurityContext() {
        return null;
    }

    public void checkPermission(Permission perm) {

    }

    public void checkPermission(Permission perm, Object context) {

    }

    public void checkCreateClassLoader() {

    }

    public void checkAccess(Thread t) { }

    public void checkAccess(ThreadGroup g) { }

    public void checkExit(int status) { }

    public void checkExec(String cmd) { }

    public void checkLink(String lib) { }

    public void checkRead(FileDescriptor fd) { }

    public void checkRead(String file) { }

    public void checkRead(String file, Object context) { }

    public void checkWrite(FileDescriptor fd) { }

    public void checkWrite(String file) { }

    public void checkDelete(String file) { }

    public void checkConnect(String host, int port) { }

    public void checkConnect(String host, int port, Object context) { }

    public void checkListen(int port) { }

    public void checkAccept(String host, int port) { }

    public void checkMulticast(InetAddress maddr) { }

    /**
     * @deprecated use {@link #checkMulticast(java.net.InetAddress)} instead.
     */
    @Deprecated
    public void checkMulticast(InetAddress maddr, byte ttl) { }

    public void checkPropertiesAccess() { }

    public void checkPropertyAccess(String key) { }

    /**
     * @deprecated this method is deprecated.
     */
    @Deprecated
    public boolean checkTopLevelWindow(Object window) {
        return true;
    }

    public void checkPrintJobAccess() { }

    public void checkSystemClipboardAccess() { }

    public void checkAwtEventQueueAccess() { }

    public void checkPackageAccess(String pkg) { }

    public void checkPackageDefinition(String pkg) { }

    public void checkSetFactory() { }

    /**
     * @deprecated this method is deprecated.
     */
    @Deprecated
    public void checkMemberAccess(Class<?> clazz, int which) { }

    public void checkSecurityAccess(String target) { }

    /**
     * Returns the current thread's thread group.
     */
    public ThreadGroup getThreadGroup() {
        return Thread.currentThread().getThreadGroup();
    }

}
