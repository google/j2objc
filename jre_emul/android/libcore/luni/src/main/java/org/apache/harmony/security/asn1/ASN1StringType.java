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
import java.nio.charset.StandardCharsets;

/**
 * This class is the super class for all string ASN.1 types
 *
 * @see <a href="http://asn1.elibel.tm.fr/en/standards/index.htm">ASN.1</a>
 */
public abstract class ASN1StringType extends ASN1Type {

    private static class ASN1StringUTF8Type extends ASN1StringType {
        public ASN1StringUTF8Type(int tagNumber) {
            super(tagNumber);
        }

        @Override
        public Object getDecodedObject(BerInputStream in) throws IOException {
            return new String(in.buffer, in.contentOffset, in.length, StandardCharsets.UTF_8);
        }

        @Override
        public void setEncodingContent(BerOutputStream out) {
            byte[] bytes = ((String) out.content).getBytes(StandardCharsets.UTF_8);
            out.content = bytes;
            out.length = bytes.length;
        }
    }

    // TODO: what about defining them as separate classes?
    // TODO: check decoded/encoded characters
    public static final ASN1StringType BMPSTRING = new ASN1StringType(TAG_BMPSTRING) {};

    public static final ASN1StringType IA5STRING = new ASN1StringType(TAG_IA5STRING) {};

    public static final ASN1StringType GENERALSTRING = new ASN1StringType(TAG_GENERALSTRING) {};

    public static final ASN1StringType PRINTABLESTRING = new ASN1StringType(TAG_PRINTABLESTRING) {};

    public static final ASN1StringType TELETEXSTRING = new ASN1StringUTF8Type(TAG_TELETEXSTRING) {};

    public static final ASN1StringType UNIVERSALSTRING = new ASN1StringType(TAG_UNIVERSALSTRING) {};

    public static final ASN1StringType UTF8STRING = new ASN1StringUTF8Type(TAG_UTF8STRING) {};

    public ASN1StringType(int tagNumber) {
        super(tagNumber);
    }

    /**
     * Tests provided identifier.
     *
     * @param identifier identifier to be verified
     * @return true if identifier correspond to primitive or constructed
     *     identifier of this ASN.1 string type, otherwise false
     */
    public final boolean checkTag(int identifier) {
        return this.id == identifier || this.constrId == identifier;
    }

    public Object decode(BerInputStream in) throws IOException {
        in.readString(this);

        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }

    /**
     * Extracts String object from BER input stream.
     */
    public Object getDecodedObject(BerInputStream in) throws IOException {
        /* To ensure we get the correct encoding on non-ASCII platforms, specify
           that we wish to convert from ASCII to the default platform encoding */
        return new String(in.buffer, in.contentOffset, in.length, StandardCharsets.ISO_8859_1);
    }

    public void encodeASN(BerOutputStream out) {
        out.encodeTag(id);
        encodeContent(out);
    }

    public void encodeContent(BerOutputStream out) {
        out.encodeString();
    }

    public void setEncodingContent(BerOutputStream out) {
        byte[] bytes = ((String) out.content).getBytes(StandardCharsets.UTF_8);
        out.content = bytes;
        out.length = bytes.length;
    }
}
