/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 1997, 2011, Oracle and/or its affiliates. All rights reserved.
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


package java.security;

import java.util.Enumeration;


/* Not used on Android, removed most of the code and made the class methods no-ops */

/**
 * Legacy security code; do not use.
 */
public abstract class Policy {

    public static final PermissionCollection UNSUPPORTED_EMPTY_COLLECTION =
                        new UnsupportedEmptyCollection();

    public static Policy getPolicy()
    {
      return null;
    }

    public static void setPolicy(Policy p)
    {
    }

    public static Policy getInstance(String type, Policy.Parameters params)
                throws NoSuchAlgorithmException {
      return null;
    }

    public static Policy getInstance(String type,
                                Policy.Parameters params,
                                String provider)
                throws NoSuchProviderException, NoSuchAlgorithmException {
      return null;
    }


    public static Policy getInstance(String type,
                                Policy.Parameters params,
                                Provider provider)
                throws NoSuchAlgorithmException {
      return null;
    }

    public Provider getProvider() {
        return null;
    }

    public String getType() {
        return null;
    }

    public Policy.Parameters getParameters() {
        return null;
    }

    public PermissionCollection getPermissions(CodeSource codesource) {
        return null;
    }

    public PermissionCollection getPermissions(ProtectionDomain domain) {
        return null;
    }

    public boolean implies(ProtectionDomain domain, Permission permission) {
        return true;
    }

    public void refresh() { }

    public static interface Parameters { }

    private static class UnsupportedEmptyCollection
        extends PermissionCollection {

        public UnsupportedEmptyCollection() {
        }

        @Override public void add(Permission permission) {
        }

        @Override public boolean implies(Permission permission) {
            return true;
        }

        @Override public Enumeration<Permission> elements() {
            return null;
        }
    }

}
