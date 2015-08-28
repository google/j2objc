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
 * Implicitly tagged ASN.1 type.
 *
 * @see <a href="http://asn1.elibel.tm.fr/en/standards/index.htm">ASN.1</a>
 */
public final class ASN1Implicit extends ASN1Type {

    /** primitive type of tagging */
    private static final int TAGGING_PRIMITIVE = 0;

    /** constructed type of tagging */
    private static final int TAGGING_CONSTRUCTED = 1;

    /** string type of tagging */
    private static final int TAGGING_STRING = 2;

    /** tagged ASN.1 type */
    private final ASN1Type type;

    /**
     * type of tagging. There are three of them
     * 1) primitive: only primitive identifier is valid
     * 2) constructed: only constructed identifier is valid
     * 3) string: both identifiers are valid
     */
    private final int taggingType;

    /**
     * Constructs implicitly tagged ASN.1 type
     * with context-specific tag class and specified tag number.
     *
     * @param tagNumber - ASN.1 tag number
     * @param type - ASN.1 type to be tagged
     * @throws IllegalArgumentException - if tagNumber or type is invalid
     */
    public ASN1Implicit(int tagNumber, ASN1Type type) {
        super(CLASS_CONTEXTSPECIFIC, tagNumber);

        if ((type instanceof ASN1Choice) || (type instanceof ASN1Any)) {
            // According to X.680:
            // 'The IMPLICIT alternative shall not be used if the type
            // defined by "Type" is an untagged choice type or an
            // untagged open type'
            throw new IllegalArgumentException("Implicit tagging can not be used for ASN.1 ANY or CHOICE type");
        }

        this.type = type;

        if (type.checkTag(type.id)) {
            if (type.checkTag(type.constrId)) {
                // the base encoding can be primitive ot constructed
                // use both encodings
                taggingType = TAGGING_STRING;
            } else {
                // if the base encoding is primitive use primitive encoding
                taggingType = TAGGING_PRIMITIVE;
            }
        } else {
            // if the base encoding is constructed use constructed encoding
            taggingType = TAGGING_CONSTRUCTED;
        }
    }

    public final boolean checkTag(int identifier) {
        switch (taggingType) {
        case TAGGING_PRIMITIVE:
            return id == identifier;
        case TAGGING_CONSTRUCTED:
            return constrId == identifier;
        default: // TAGGING_STRING
            return id == identifier || constrId == identifier;
        }
    }

    public Object decode(BerInputStream in) throws IOException {
        if (!checkTag(in.tag)) {
            // FIXME need look for tagging type
            throw new ASN1Exception("ASN.1 implicitly tagged type expected at " +
                    "[" + in.tagOffset + "]. Expected tag: " + Integer.toHexString(id) + ", " +
                    "but got " + Integer.toHexString(in.tag));
        }

        // substitute identifier for further decoding
        if (id == in.tag) {
            in.tag = type.id;
        } else {
            in.tag = type.constrId;
        }
        in.content = type.decode(in);

        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }

    public void encodeASN(BerOutputStream out) {
        //FIXME need another way for specifying identifier to be encoded
        if (taggingType == TAGGING_CONSTRUCTED) {
            out.encodeTag(constrId);
        } else {
            out.encodeTag(id);
        }
        encodeContent(out);
    }

    public void encodeContent(BerOutputStream out) {
        type.encodeContent(out);
    }

    public void setEncodingContent(BerOutputStream out) {
        type.setEncodingContent(out);
    }
}
