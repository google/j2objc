/*
 * Copyright (c) 1998, 2010, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

import java.security.AccessController;

import android.system.ErrnoException;

import dalvik.system.BlockGuard;

import libcore.io.Libcore;
import libcore.io.OsConstants;

import sun.security.action.GetPropertyAction;


class UnixFileSystem extends FileSystem {

    private final char slash;
    private final char colon;
    private final String javaHome;

    public UnixFileSystem() {
        slash = AccessController.doPrivileged(
            new GetPropertyAction("file.separator")).charAt(0);
        colon = AccessController.doPrivileged(
            new GetPropertyAction("path.separator")).charAt(0);
        javaHome = AccessController.doPrivileged(
            new GetPropertyAction("java.home"));
    }


    /* -- Normalization and construction -- */

    public char getSeparator() {
        return slash;
    }

    public char getPathSeparator() {
        return colon;
    }

    /*
     * A normal Unix pathname does not contain consecutive slashes and does not end
     * with a slash. The empty string and "/" are special cases that are also
     * considered normal.
     */
    public String normalize(String pathname) {
        int n = pathname.length();
        char[] normalized = pathname.toCharArray();
        int index = 0;
        char prevChar = 0;
        for (int i = 0; i < n; i++) {
            char current = normalized[i];
            // Remove duplicate slashes.
            if (!(current == '/' && prevChar == '/')) {
                normalized[index++] = current;
            }

            prevChar = current;
        }

        // Omit the trailing slash, except when pathname == "/".
        if (prevChar == '/' && n > 1) {
            index--;
        }

        return (index != n) ? new String(normalized, 0, index) : pathname;
    }

    public int prefixLength(String pathname) {
        if (pathname.length() == 0) return 0;
        return (pathname.charAt(0) == '/') ? 1 : 0;
    }

    // Invariant: Both |parent| and |child| are normalized paths.
    public String resolve(String parent, String child) {
        if (child.isEmpty() || child.equals("/")) {
            return parent;
        }

        if (child.charAt(0) == '/') {
            if (parent.equals("/")) return child;
            return parent + child;
        }

        if (parent.equals("/")) return parent + child;
        return parent + '/' + child;
    }

    public String getDefaultParent() {
        return "/";
    }

    public String fromURIPath(String path) {
        String p = path;
        if (p.endsWith("/") && (p.length() > 1)) {
            // "/foo/" --> "/foo", but "/" --> "/"
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }


    /* -- Path operations -- */

    public boolean isAbsolute(File f) {
        return (f.getPrefixLength() != 0);
    }

    public String resolve(File f) {
        if (isAbsolute(f)) return f.getPath();
        return resolve(System.getProperty("user.dir"), f.getPath());
    }

    // Caches for canonicalization results to improve startup performance.
    // The first cache handles repeated canonicalizations of the same path
    // name. The prefix cache handles repeated canonicalizations within the
    // same directory, and must not create results differing from the true
    // canonicalization algorithm in canonicalize_md.c. For this reason the
    // prefix cache is conservative and is not used for complex path names.
    private ExpiringCache cache = new ExpiringCache();
    // On Unix symlinks can jump anywhere in the file system, so we only
    // treat prefixes in java.home as trusted and cacheable in the
    // canonicalization algorithm
    private ExpiringCache javaHomePrefixCache = new ExpiringCache();

    public String canonicalize(String path) throws IOException {
        if (!useCanonCaches) {
            return canonicalize0(path);
        } else {
            String res = cache.get(path);
            if (res == null) {
                String dir = null;
                String resDir = null;
                if (useCanonPrefixCache) {
                    // Note that this can cause symlinks that should
                    // be resolved to a destination directory to be
                    // resolved to the directory they're contained in
                    dir = parentOrNull(path);
                    if (dir != null) {
                        resDir = javaHomePrefixCache.get(dir);
                        if (resDir != null) {
                            // Hit only in prefix cache; full path is canonical
                            String filename = path.substring(1 + dir.length());
                            res = resDir + slash + filename;
                            cache.put(dir + slash + filename, res);
                        }
                    }
                }
                if (res == null) {
                    // j2objc: removed BlockGuard support.
                    res = canonicalize0(path);
                    cache.put(path, res);
                    if (useCanonPrefixCache &&
                        dir != null && dir.startsWith(javaHome)) {
                        resDir = parentOrNull(res);
                        // Note that we don't allow a resolved symlink
                        // to elsewhere in java.home to pollute the
                        // prefix cache (java.home prefix cache could
                        // just as easily be a set at this point)
                        if (resDir != null && resDir.equals(dir)) {
                            File f = new File(res);
                            if (f.exists() && !f.isDirectory()) {
                                javaHomePrefixCache.put(dir, resDir);
                            }
                        }
                    }
                }
            }
            return res;
        }
    }
    private native String canonicalize0(String path) throws IOException;
    // Best-effort attempt to get parent of this path; used for
    // optimization of filename canonicalization. This must return null for
    // any cases where the code in canonicalize_md.c would throw an
    // exception or otherwise deal with non-simple pathnames like handling
    // of "." and "..". It may conservatively return null in other
    // situations as well. Returning null will cause the underlying
    // (expensive) canonicalization routine to be called.
    static String parentOrNull(String path) {
        if (path == null) return null;
        char sep = File.separatorChar;
        int last = path.length() - 1;
        int idx = last;
        int adjacentDots = 0;
        int nonDotCount = 0;
        while (idx > 0) {
            char c = path.charAt(idx);
            if (c == '.') {
                if (++adjacentDots >= 2) {
                    // Punt on pathnames containing . and ..
                    return null;
                }
            } else if (c == sep) {
                if (adjacentDots == 1 && nonDotCount == 0) {
                    // Punt on pathnames containing . and ..
                    return null;
                }
                if (idx == 0 ||
                    idx >= last - 1 ||
                    path.charAt(idx - 1) == sep) {
                    // Punt on pathnames containing adjacent slashes
                    // toward the end
                    return null;
                }
                return path.substring(0, idx);
            } else {
                ++nonDotCount;
                adjacentDots = 0;
            }
            --idx;
        }
        return null;
    }

    /* -- Attribute accessors -- */

    private native int getBooleanAttributes0(String abspath);

    public int getBooleanAttributes(File f) {
        // j2objc: removed BlockGuard support.

        int rv = getBooleanAttributes0(f.getPath());
        String name = f.getName();
        boolean hidden = (name.length() > 0) && (name.charAt(0) == '.');
        return rv | (hidden ? BA_HIDDEN : 0);
    }

    // Android-changed: Access files through common interface.
    public boolean checkAccess(File f, int access) {
        final int mode;
        switch (access) {
            case FileSystem.ACCESS_OK:
                mode = OsConstants.F_OK;
                break;
            case FileSystem.ACCESS_READ:
                mode = OsConstants.R_OK;
                break;
            case FileSystem.ACCESS_WRITE:
                mode = OsConstants.W_OK;
                break;
            case FileSystem.ACCESS_EXECUTE:
                mode = OsConstants.X_OK;
                break;
            default:
                throw new IllegalArgumentException("Bad access mode: " + access);
        }

        try {
            return Libcore.os.access(f.getPath(), mode);
        } catch (ErrnoException e) {
            return false;
        }
    }

    // Android-changed: Add method to intercept native method call; BlockGuard support.
    public long getLastModifiedTime(File f) {
        // j2objc: removed BlockGuard support.
        return getLastModifiedTime0(f);
    }
    private native long getLastModifiedTime0(File f);

    // Android-changed: Access files through common interface.
    public long getLength(File f) {
        try {
            return Libcore.os.stat(f.getPath()).st_size;
        } catch (ErrnoException e) {
            return 0;
        }
    }

    // Android-changed: Add method to intercept native method call; BlockGuard support.
    public boolean setPermission(File f, int access, boolean enable, boolean owneronly) {
        // j2objc: removed BlockGuard support.
        return setPermission0(f, access, enable, owneronly);
    }
    private native boolean setPermission0(File f, int access, boolean enable, boolean owneronly);

    /* -- File operations -- */
    // Android-changed: Add method to intercept native method call; BlockGuard support.
    public boolean createFileExclusively(String path) throws IOException {
        // j2objc: removed BlockGuard support.
        return createFileExclusively0(path);
    }
    private native boolean createFileExclusively0(String path) throws IOException;

    public boolean delete(File f) {
        // Keep canonicalization caches in sync after file deletion
        // and renaming operations. Could be more clever than this
        // (i.e., only remove/update affected entries) but probably
        // not worth it since these entries expire after 30 seconds
        // anyway.
        cache.clear();
        javaHomePrefixCache.clear();
        // BEGIN Android-changed: Access files through common interface.
        try {
            Libcore.os.remove(f.getPath());
            return true;
        } catch (ErrnoException e) {
            return false;
        }
        // END Android-changed: Access files through common interface.
    }

    // Android-removed: Access files through common interface.
    // private native boolean delete0(File f);

    // Android-changed: Add method to intercept native method call; BlockGuard support.
    public String[] list(File f) {
        // j2objc: removed BlockGuard support.
        return list0(f);
    }
    private native String[] list0(File f);

    // Android-changed: Add method to intercept native method call; BlockGuard support.
    public boolean createDirectory(File f) {
        // j2objc: removed BlockGuard support.
        return createDirectory0(f);
    }
    private native boolean createDirectory0(File f);

    public boolean rename(File f1, File f2) {
        // Keep canonicalization caches in sync after file deletion
        // and renaming operations. Could be more clever than this
        // (i.e., only remove/update affected entries) but probably
        // not worth it since these entries expire after 30 seconds
        // anyway.
        cache.clear();
        javaHomePrefixCache.clear();
        // BEGIN Android-changed: Access files through common interface.
        try {
            Libcore.os.rename(f1.getPath(), f2.getPath());
            return true;
        } catch (ErrnoException e) {
            return false;
        }
        // END Android-changed: Access files through common interface.
    }

    // Android-removed: Access files through common interface.
    // private native boolean rename0(File f1, File f2);

    // Android-changed: Add method to intercept native method call; BlockGuard support.
    public boolean setLastModifiedTime(File f, long time) {
        // j2objc: removed BlockGuard support.
        return setLastModifiedTime0(f, time);
    }
    private native boolean setLastModifiedTime0(File f, long time);

    // Android-changed: Add method to intercept native method call; BlockGuard support.
    public boolean setReadOnly(File f) {
        // j2objc: removed BlockGuard support.
        return setReadOnly0(f);
    }
    private native boolean setReadOnly0(File f);


    /* -- Filesystem interface -- */

    public File[] listRoots() {
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkRead("/");
            }
            return new File[] { new File("/") };
        } catch (SecurityException x) {
            return new File[0];
        }
    }

    /* -- Disk usage -- */
    // Android-changed: Add method to intercept native method call; BlockGuard support.
    public long getSpace(File f, int t) {
        // j2objc: removed BlockGuard support.

        return getSpace0(f, t);
    }
    private native long getSpace0(File f, int t);

    /* -- Basic infrastructure -- */

    public int compare(File f1, File f2) {
        return f1.getPath().compareTo(f2.getPath());
    }

    public int hashCode(File f) {
        return f.getPath().hashCode() ^ 1234321;
    }


//    j2objc: removed
//    private static native void initIDs();
//
//    static {
//        initIDs();
//    }

}
