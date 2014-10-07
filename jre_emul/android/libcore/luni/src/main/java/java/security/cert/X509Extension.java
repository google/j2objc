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

package java.security.cert;

import java.util.Set;

/**
 * The interface specifying an X.509 Certificate or CRL extension.
 */
public interface X509Extension {

    /**
     * Returns the set of OIDs of the extension(s) marked as CRITICAL, that this
     * implementation manages.
     *
     * @return the set of extension OIDs marked as CRITIAL, an empty set if none
     *         are marked as CRITICAL, or {@code null} if no extensions are
     *         present.
     */
    public Set<String> getCriticalExtensionOIDs();

    /**
     * Returns the extension value as DER-encoded OCTET string for the specified
     * OID.
     *
     * @param oid
     *            the object identifier to get the extension value for.
     * @return the extension value as DER-encoded OCTET string, or {@code null}
     *         if no extension for the specified OID can be found.
     */
    public byte[] getExtensionValue(String oid);

    /**
     * Returns the set of OIDs of the extension(s) marked as NON-CRITICAL, that
     * this implementation manages.
     *
     * @return the set of extension OIDs marked as NON-CRITIAL, an empty set if
     *         none are marked as NON-.CRITICAL, or {@code null} if no
     *         extensions are present.
     */
    public Set<String> getNonCriticalExtensionOIDs();

    /**
     * Returns whether this instance has an extension marked as CRITICAL that it
     * cannot support.
     *
     * @return {@code true} if an unsupported CRITICAL extension is present,
     *         {@code false} otherwise.
     */
    public boolean hasUnsupportedCriticalExtension();
}
