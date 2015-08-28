/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.security.cert;

import java.io.IOException;
import java.io.OutputStream;

/**
 * The Extension part of an X.509 certificate (as specified in <a
 * href="http://www.ietf.org/rfc/rfc3280.txt">RFC 3280 &mdash; Internet X.509
 * Public Key Infrastructure: Certificate and Certificate Revocation List (CRL)
 * Profile</p>):
 *
 * <pre>
 *  Extension  ::=  SEQUENCE  {
 *       extnID      OBJECT IDENTIFIER,
 *       critical    BOOLEAN DEFAULT FALSE,
 *       extnValue   OCTET STRING
 *  }
 * </pre>
 *
 * @since 1.7
 * @hide
 */
public interface Extension {
    /**
     * Returns the OID (Object Identifier) for this extension encoded as a
     * string (e.g., "2.5.29.15").
     */
    String getId();

    /**
     * Returns {@code true} if this extension is critical. If this is true and
     * an implementation does not understand this extension, it must reject it.
     * See RFC 3280 section 4.2 for more information.
     */
    boolean isCritical();

    /**
     * The DER-encoded value of this extension.
     */
    byte[] getValue();

    /**
     * Writes the DER-encoded extension to {@code out}.
     *
     * @throws IOException when there is an encoding error or error writing to
     *             {@code out}
     */
    void encode(OutputStream out) throws IOException;
}
