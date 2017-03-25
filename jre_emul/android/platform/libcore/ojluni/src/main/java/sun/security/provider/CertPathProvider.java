/*
 * Copyright 2016 The Android Open Source Project
 * Copyright (c) 1996, 2006, Oracle and/or its affiliates. All rights reserved.
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

package sun.security.provider;

import java.security.Provider;

/**
 * A security provider that provides the OpenJDK version of the CertPathBuilder and
 * CertPathVerifier.
 */
public final class CertPathProvider extends Provider {

    public CertPathProvider() {
        super("CertPathProvider", 1.0, "Provider of CertPathBuilder and CertPathVerifier");

        // CertPathBuilder
        put("CertPathBuilder.PKIX", "sun.security.provider.certpath.SunCertPathBuilder");
        put("CertPathBuilder.PKIX ImplementedIn", "Software");
        put("CertPathBuilder.PKIX ValidationAlgorithm", "RFC3280");

        // CertPathValidator
        put("CertPathValidator.PKIX", "sun.security.provider.certpath.PKIXCertPathValidator");
        put("CertPathValidator.PKIX ImplementedIn", "Software");
        put("CertPathValidator.PKIX ValidationAlgorithm", "RFC3280");
    }
}
