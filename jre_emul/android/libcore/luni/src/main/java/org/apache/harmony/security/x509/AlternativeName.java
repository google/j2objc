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

package org.apache.harmony.security.x509;

import java.io.IOException;

/**
 * This class implements the values of Subject Alternative Name
 * (OID is 2.5.29.17) and Issuer Alternative Name extensions
 * (OID is 2.5.29.18).<br>
 * For more information about these extensions see RFC 3280
 * at http://www.ietf.org/rfc/rfc3280.txt
 */
public final class AlternativeName extends ExtensionValue {

    // constants indicating which alternative name is presented
    // by this object
    public static final boolean ISSUER = false;
    public static final boolean SUBJECT = true;

    /** indicating which alternative name is presented by this object */
    private boolean which;
    /** the alternative names */
    private GeneralNames alternativeNames;

    /**
     * Creates the extension object on the base of its encoded form.
     * @param which specifies which alternative names are given
     * (Subject's or Issuer's)
     */
    public AlternativeName(boolean which, byte[] encoding) throws IOException {
        super(encoding);
        this.which = which;
        this.alternativeNames = (GeneralNames) GeneralNames.ASN1.decode(encoding);
    }

    /**
     * Returns ASN.1 encoded form of this X.509 AlternativeName value.
     */
    @Override public byte[] getEncoded() {
        if (encoding == null) {
            encoding = GeneralNames.ASN1.encode(alternativeNames);
        }
        return encoding;
    }

    @Override public void dumpValue(StringBuilder sb, String prefix) {
        sb.append(prefix).append((which) ? "Subject" : "Issuer").append(" Alternative Names [\n");
        alternativeNames.dumpValue(sb, prefix + "  ");
        sb.append(prefix).append("]\n");
    }
}
