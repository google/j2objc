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
* @author Stepan M. Mishura
* @version $Revision$
*/

package org.apache.harmony.security.asn1;

import java.io.IOException;
import java.io.InputStream;

/**
 * Decodes ASN.1 types encoded with DER (X.690)
 *
 * @see <a href="http://asn1.elibel.tm.fr/en/standards/index.htm">ASN.1</a>
 */
public final class DerInputStream extends BerInputStream {

    /** mask for verifying unused bits for ASN.1 bitstring */
    private static final byte[] UNUSED_BITS_MASK = new byte[] { 0x01, 0x03,
            0x07, 0x0F, 0x1F, 0x3F, 0x7F };

    public DerInputStream(byte[] encoded) throws IOException {
        super(encoded, 0, encoded.length);
    }

    public DerInputStream(byte[] encoded, int offset, int encodingLen) throws IOException {
        super(encoded, offset, encodingLen);
    }

    public DerInputStream(InputStream in) throws IOException {
        super(in);
    }

    public int next() throws IOException {
        int tag = super.next();

        if (length == INDEFINIT_LENGTH) {
            throw new ASN1Exception("DER: only definite length encoding MUST be used");
        }

        // FIXME add check: length encoding uses minimum number of octets

        return tag;
    }

    public void readBitString() throws IOException {
        if (tag == ASN1Constants.TAG_C_BITSTRING) {
            throw new ASN1Exception("ASN.1 bitstring: constructed identifier at [" + tagOffset
                    + "]. Not valid for DER.");
        }

        super.readBitString();

        //check: unused bits values - MUST be 0
        if (length > 1
                && buffer[contentOffset] != 0
                && (buffer[offset - 1] & UNUSED_BITS_MASK[buffer[contentOffset] - 1]) != 0) {
            throw new ASN1Exception("ASN.1 bitstring: wrong content at [" + contentOffset
                    + "]. DER requires zero unused bits in final octet.");
        }
    }

    public void readBoolean() throws IOException {
        super.readBoolean();

        // check encoded content
        if (buffer[contentOffset] != 0 && buffer[contentOffset] != (byte) 0xFF) {
            throw new ASN1Exception("ASN.1 boolean: wrong content at [" + contentOffset
                    + "]. DER allows only 0x00 or 0xFF values");
        }
    }

    public void readOctetString() throws IOException {
        if (tag == ASN1Constants.TAG_C_OCTETSTRING) {
            throw new ASN1Exception("ASN.1 octetstring: constructed identifier at [" + tagOffset
                    + "]. Not valid for DER.");
        }
        super.readOctetString();
    }

    public void readSequence(ASN1Sequence sequence) throws IOException {
        //
        // According to ASN.1 DER spec. sequence MUST not include
        // any encoding which value is equal to its default value
        //
        // Verification of this assertion is not implemented
        //
        super.readSequence(sequence);
    }

    public void readSetOf(ASN1SetOf setOf) throws IOException {
        //
        // According to ASN.1 DER spec. set of MUST appear in
        // ascending order (short component are padded for comparison)
        //
        // Verification of this assertion is not implemented
        //
        super.readSetOf(setOf);
    }

    public void readString(ASN1StringType type) throws IOException {
        if (tag == type.constrId) {
            throw new ASN1Exception("ASN.1 string: constructed identifier at [" + tagOffset
                    + "]. Not valid for DER.");
        }
        super.readString(type);
    }

    public void readUTCTime() throws IOException {
        if (tag == ASN1Constants.TAG_C_UTCTIME) {
            // It is a string type and it can be encoded as primitive or constructed.
            throw new ASN1Exception("ASN.1 UTCTime: constructed identifier at [" + tagOffset
                    + "]. Not valid for DER.");
        }

        // check format: DER uses YYMMDDHHMMSS'Z' only
        if (length != ASN1UTCTime.UTC_HMS) {
            throw new ASN1Exception("ASN.1 UTCTime: wrong format for DER, identifier at ["
                    + tagOffset + "]");
        }

        super.readUTCTime();
    }

    public void readGeneralizedTime() throws IOException {
        if (tag == ASN1Constants.TAG_C_GENERALIZEDTIME) {
            // It is a string type and it can be encoded as primitive or constructed.
            throw new ASN1Exception("ASN.1 GeneralizedTime: constructed identifier at ["
                    + tagOffset + "]. Not valid for DER.");
        }

        super.readGeneralizedTime();
    }
}
