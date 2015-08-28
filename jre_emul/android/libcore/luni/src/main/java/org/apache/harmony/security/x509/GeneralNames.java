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

/**
* @author Alexander Y. Kleymenov
* @version $Revision$
*/

package org.apache.harmony.security.x509;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.harmony.security.asn1.ASN1SequenceOf;
import org.apache.harmony.security.asn1.ASN1Type;
import org.apache.harmony.security.asn1.BerInputStream;


/**
 * The class encapsulates the ASN.1 DER encoding/decoding work
 * with the GeneralNames structure which is a part of X.509 certificate
 * (as specified in RFC 3280 -
 *  Internet X.509 Public Key Infrastructure.
 *  Certificate and Certificate Revocation List (CRL) Profile.
 *  http://www.ietf.org/rfc/rfc3280.txt):
 *
 *
 * <pre>
 *   GeneralNames ::= SEQUENCE SIZE (1..MAX) OF GeneralName
 * </pre>
 *
 * @see org.apache.harmony.security.x509.NameConstraints
 * @see org.apache.harmony.security.x509.GeneralSubtree
 */
public final class GeneralNames {
    /** the values of GeneralName */
    private List<GeneralName> generalNames;
    /** the ASN.1 encoded form of GeneralNames */
    private byte[] encoding;

    public GeneralNames() {
        generalNames = new ArrayList<GeneralName>();
    }

    public GeneralNames(List<GeneralName> generalNames) {
        this.generalNames = generalNames;
    }

    private GeneralNames(List<GeneralName> generalNames, byte[] encoding) {
        this.generalNames = generalNames;
        this.encoding = encoding;
    }

    /**
     * Returns the list of values.
     */
    public List<GeneralName> getNames() {
        if ((generalNames == null) || (generalNames.size() == 0)) {
            return null;
        }
        return new ArrayList<GeneralName>(generalNames);
    }

    /**
     * Returns the collection of pairs: (Integer (tag), Object (name value))*
     */
    public Collection<List<?>> getPairsList() {
        Collection<List<?>> result = new ArrayList<List<?>>();
        if (generalNames == null) {
            return result;
        }
        for (GeneralName generalName : generalNames) {
            /*
             * If we have an error decoding one of the GeneralNames, we'll just
             * omit it from the final list.
             */
            final List<Object> genNameList;
            try {
                genNameList = generalName.getAsList();
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            result.add(genNameList);
        }
        return result;
    }

    public void addName(GeneralName name) {
        encoding = null;
        if (generalNames == null) {
            generalNames = new ArrayList<GeneralName>();
        }
        generalNames.add(name);
    }

    /**
     * Returns ASN.1 encoded form of this X.509 GeneralNames value.
     */
    public byte[] getEncoded() {
        if (encoding == null) {
            encoding = ASN1.encode(this);
        }
        return encoding;
    }

    public void dumpValue(StringBuilder sb, String prefix) {
        if (generalNames == null) {
            return;
        }
        for (GeneralName generalName : generalNames) {
            sb.append(prefix);
            sb.append(generalName);
            sb.append('\n');
        }
    }

    /**
     * ASN.1 DER X.509 GeneralNames encoder/decoder class.
     */
    public static final ASN1Type ASN1 = new ASN1SequenceOf(GeneralName.ASN1) {
        @Override public Object getDecodedObject(BerInputStream in) {
            return new GeneralNames((List<GeneralName>) in.content, in.getEncoded());
        }

        @Override public Collection getValues(Object object) {
            GeneralNames gns = (GeneralNames) object;
            return gns.generalNames;
        }
    };
}
