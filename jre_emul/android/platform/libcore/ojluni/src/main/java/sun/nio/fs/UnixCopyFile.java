/*
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
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

package sun.nio.fs;

import java.nio.file.*;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import com.sun.nio.file.ExtendedCopyOption;

import static sun.nio.fs.UnixNativeDispatcher.*;
import static sun.nio.fs.UnixConstants.*;


/**
 * Unix implementation of Path#copyTo and Path#moveTo methods.
 */

class UnixCopyFile {
    private UnixCopyFile() {  }

    // The flags that control how a file is copied or moved
    private static class Flags {
        boolean replaceExisting;
        boolean atomicMove;
        boolean followLinks;
        boolean interruptible;

        // the attributes to copy
        boolean copyBasicAttributes;
        boolean copyPosixAttributes;
        boolean copyNonPosixAttributes;

        // flags that indicate if we should fail if attributes cannot be copied
        boolean failIfUnableToCopyBasic;
        boolean failIfUnableToCopyPosix;
        boolean failIfUnableToCopyNonPosix;

        static Flags fromCopyOptions(CopyOption... options) {
            Flags flags = new Flags();
            flags.followLinks = true;
            for (CopyOption option: options) {
                if (option == StandardCopyOption.REPLACE_EXISTING) {
                    flags.replaceExisting = true;
                    continue;
                }
                if (option == LinkOption.NOFOLLOW_LINKS) {
                    flags.followLinks = false;
                    continue;
                }
                if (option == StandardCopyOption.COPY_ATTRIBUTES) {
                    // copy all attributes but only fail if basic attributes
                    // cannot be copied
                    flags.copyBasicAttributes = true;
                    flags.copyPosixAttributes = true;
                    flags.copyNonPosixAttributes = true;
                    flags.failIfUnableToCopyBasic = true;
                    continue;
                }
                if (option == ExtendedCopyOption.INTERRUPTIBLE) {
                    flags.interruptible = true;
                    continue;
                }
                if (option == null)
                    throw new NullPointerException();
                throw new UnsupportedOperationException("Unsupported copy option");
            }
            return flags;
        }

        static Flags fromMoveOptions(CopyOption... options) {
            Flags flags = new Flags();
            for (CopyOption option: options) {
                if (option == StandardCopyOption.ATOMIC_MOVE) {
                    flags.atomicMove = true;
                    continue;
                }
                if (option == StandardCopyOption.REPLACE_EXISTING) {
                    flags.replaceExisting = true;
                    continue;
                }
                if (option == LinkOption.NOFOLLOW_LINKS) {
                    // ignore
                    continue;
                }
                if (option == null)
                    throw new NullPointerException();
                throw new UnsupportedOperationException("Unsupported copy option");
            }

            // a move requires that all attributes be copied but only fail if
            // the basic attributes cannot be copied
            flags.copyBasicAttributes = true;
            flags.copyPosixAttributes = true;
            flags.copyNonPosixAttributes = true;
            flags.failIfUnableToCopyBasic = true;
            return flags;
        }
    }

    // -- native methods --

    static native void transfer(int dst, int src, long addressToPollForCancel)
        throws UnixException;

    // Android-removed: Code to load native libraries, doesn't make sense on Android.
    /*
    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                System.loadLibrary("nio");
                return null;
            }});
    }
    */

}
