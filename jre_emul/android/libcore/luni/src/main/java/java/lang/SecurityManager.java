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

package java.lang;

import java.io.FileDescriptor;
import java.security.Permission;

/**
 * Legacy security code; do not use.
 *
 * <p>Security managers do <strong>not</strong> provide a
 * secure environment for executing untrusted code. Untrusted code cannot be
 * safely isolated within the Dalvik VM.
 */
public class SecurityManager {
    /**
     * @deprecated Use {@link #checkPermission}
     */
    @Deprecated
    protected boolean inCheck;

    public SecurityManager() { }

    public void checkAccept(String host, int port) { }

    public void checkAccess(Thread thread) { }

    public void checkAccess(ThreadGroup group) { }

    public void checkConnect(String host, int port) { }

    public void checkConnect(String host, int port, Object context) { }

    public void checkCreateClassLoader() { }

    public void checkDelete(String file) { }

    public void checkExec(String cmd) { }

    public void checkExit(int status) { }

    public void checkLink(String libName) { }

    public void checkListen(int port) { }

    public void checkMemberAccess(Class<?> cls, int type) { }

    /* TODO(tball): enable when java.net is supported.
    public void checkMulticast(InetAddress maddr) { }

    /**
     * @deprecated use {@link #checkMulticast(java.net.InetAddress)}
     * /
    @Deprecated public void checkMulticast(InetAddress maddr, byte ttl) { }
    */

    public void checkPackageAccess(String packageName) { }

    public void checkPackageDefinition(String packageName) { }

    public void checkPropertiesAccess() { }

    public void checkPropertyAccess(String key) { }

    public void checkRead(FileDescriptor fd) { }

    public void checkRead(String file) { }

    public void checkRead(String file, Object context) { }

    public void checkSecurityAccess(String target) { }

    public void checkSetFactory() { }

    public boolean checkTopLevelWindow(Object window) { return true; }

    public void checkSystemClipboardAccess() { }

    public void checkAwtEventQueueAccess() { }

    public void checkPrintJobAccess() { }

    public void checkWrite(FileDescriptor fd) { }

    public void checkWrite(String file) { }

    /**
     * @deprecated Use {@link #checkPermission}.
     */
    @Deprecated public boolean getInCheck() { return inCheck; }

    protected Class[] getClassContext() { return null; }

    /**
     * @deprecated Use {@link #checkPermission}.
     */
    @Deprecated protected ClassLoader currentClassLoader() { return null; }

    /**
     * @deprecated Use {@link #checkPermission}.
     */
    @Deprecated protected int classLoaderDepth() {
        return -1;
    }

    /**
     * @deprecated Use {@link #checkPermission}.
     */
    @Deprecated protected Class<?> currentLoadedClass() { return null; }

    /**
     * @deprecated Use {@link #checkPermission}.
     */
    @Deprecated protected int classDepth(String name) { return -1; }

    /**
     * @deprecated Use {@link #checkPermission}.
     */
    @Deprecated protected boolean inClass(String name) { return false; }

    /**
     * @deprecated Use {@link #checkPermission}
     */
    @Deprecated protected boolean inClassLoader() { return false; }

    /**
     * Returns the current thread's thread group.
     */
    public ThreadGroup getThreadGroup() {
        return Thread.currentThread().getThreadGroup();
    }

    public Object getSecurityContext() { return null; }

    public void checkPermission(Permission permission) { }

    public void checkPermission(Permission permission, Object context) { }
}
