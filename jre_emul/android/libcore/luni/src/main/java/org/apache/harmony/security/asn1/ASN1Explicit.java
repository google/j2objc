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
* @author Vladimir N. Molotkov, Stepan M. Mishura
* @version $Revision$
*/

package org.apache.harmony.security.asn1;

import java.io.IOException;

/**
 * This class represents explicitly tagged ASN.1 type.
 *
 * @see <a href="http://asn1.elibel.tm.fr/en/standards/index.htm">ASN.1</a>
 */
public final class ASN1Explicit extends ASN1Constructed {

    /** Tagged type */
    public final ASN1Type type;

    /**
     * Constructs explicitly tagged ASN.1 type
     * with context-specific tag class and specified tag number.
     *
     * @param tagNumber - ASN.1 tag number
     * @param type - ASN.1 type to be tagged
     * @throws IllegalArgumentException - if tagNumber is invalid
     */
    public ASN1Explicit(int tagNumber, ASN1Type type) {
        this(CLASS_CONTEXTSPECIFIC, tagNumber, type);
    }

    /**
     * Constructs explicitly tagged ASN.1 type.
     *
     * @param tagClass - ASN.1 tag class.
     * @param tagNumber - ASN.1 tag number
     * @param type - ASN.1 type to be tagged
     * @throws IllegalArgumentException - if tagClass or tagNumber is invalid
     */
    public ASN1Explicit(int tagClass, int tagNumber, ASN1Type type) {
        super(tagClass, tagNumber);
        this.type = type;
    }

    public Object decode(BerInputStream in) throws IOException {
        if (constrId != in.tag) {
            throw new ASN1Exception("ASN.1 explicitly tagged type is expected at [" +
                    in.tagOffset + "]. Expected tag: " + Integer.toHexString(constrId) + ", " +
                    "but encountered tag " + Integer.toHexString(in.tag));
        }
        in.next();

        in.content = type.decode(in);

        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }

    public void encodeContent(BerOutputStream out) {
        out.encodeExplicit(this);
    }

    public void setEncodingContent(BerOutputStream out) {
        out.getExplicitLength(this);
    }

    @Override public String toString() {
        return super.toString() + " for type " + type;
    }
}
