/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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


import java.net.URL;
import java.net.SocketPermission;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.*;

// Android-changed: Stubbed the implementation.  Android doesn't support SecurityManager.
// See comments in java.lang.SecurityManager for details.
/**
 * Legacy security code; do not use.
 */

public class CodeSource implements java.io.Serializable {

    /**
     * The code location.
     *
     * @serial
     */
    private URL location;

    public CodeSource(URL url, java.security.cert.Certificate certs[]) {
        this.location = url;
    }

    public CodeSource(URL url, CodeSigner[] signers) {
        this.location = url;
    }

    public final URL getLocation() {
        /* since URL is practically immutable, returning itself is not
           a security problem */
        return this.location;
    }

    public final java.security.cert.Certificate[] getCertificates() { return null; }

    public final CodeSigner[] getCodeSigners() { return null; }

    public boolean implies(CodeSource codesource) { return true; }
}
