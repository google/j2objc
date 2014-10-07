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


/**
 * Encodes ASN.1 types with BER (X.690)
 *
 * @see <a href="http://asn1.elibel.tm.fr/en/standards/index.htm">ASN.1</a>
 */
public class BerOutputStream {

    /** Encoded byte array */
    public byte[] encoded;

    /** current offset */
    protected int offset;

    /** Current encoded length */
    public int length;

    /** Current encoded content */
    public Object content;

    public final void encodeTag(int tag) {
        encoded[offset++] = (byte) tag; //FIXME long form?

        if (length > 127) { //long form
            int eLen = length >> 8;
            byte numOctets = 1;
            for (; eLen > 0; eLen = eLen >> 8) {
                numOctets++;
            }

            encoded[offset] = (byte) (numOctets | 0x80);
            offset++;

            eLen = length;
            int numOffset = offset + numOctets - 1;
            for (int i = 0; i < numOctets; i++, eLen = eLen >> 8) {
                encoded[numOffset - i] = (byte) eLen; //FIXME long value?
            }
            offset += numOctets;
        } else { //short form
            encoded[offset++] = (byte) length;
        }
    }

    public void encodeANY() {
        System.arraycopy(content, 0, encoded, offset, length);
        offset += length;
    }

    public void encodeBitString() {
        //FIXME check encoding
        BitString bStr = (BitString) content;
        encoded[offset] = (byte) bStr.unusedBits;
        System.arraycopy(bStr.bytes, 0, encoded, offset + 1, length - 1);
        offset += length;
    }

    public void encodeBoolean() {
        if ((Boolean) content) {
            encoded[offset] = (byte) 0xFF;
        } else {
            encoded[offset] = 0x00;
        }
        offset++;
    }

    public void encodeChoice(ASN1Choice choice) {
        throw new RuntimeException("Is not implemented yet"); //FIXME
    }

    public void encodeExplicit(ASN1Explicit explicit) {
        throw new RuntimeException("Is not implemented yet"); //FIXME
    }

    public void encodeGeneralizedTime() {
        System.arraycopy(content, 0, encoded, offset, length);
        offset += length;
    }

    public void encodeUTCTime() {
        System.arraycopy(content, 0, encoded, offset, length);
        offset += length;
    }

    public void encodeInteger() {
        System.arraycopy(content, 0, encoded, offset, length);
        offset += length;
    }

    public void encodeOctetString() {
        System.arraycopy(content, 0, encoded, offset, length);
        offset += length;
    }

    public void encodeOID() {

        int[] oid = (int[]) content;

        int oidLen = length;

        // all subidentifiers except first
        int elem;
        for (int i = oid.length - 1; i > 1; i--, oidLen--) {
            elem = oid[i];
            if (elem > 127) {
                encoded[offset + oidLen - 1] = (byte) (elem & 0x7F);
                elem = elem >> 7;
                for (; elem > 0;) {
                    oidLen--;
                    encoded[offset + oidLen - 1] = (byte) (elem | 0x80);
                    elem = elem >> 7;
                }
            } else {
                encoded[offset + oidLen - 1] = (byte) elem;
            }
        }

        // first subidentifier
        elem = oid[0] * 40 + oid[1];
        if (elem > 127) {
            encoded[offset + oidLen - 1] = (byte) (elem & 0x7F);
            elem = elem >> 7;
            for (; elem > 0;) {
                oidLen--;
                encoded[offset + oidLen - 1] = (byte) (elem | 0x80);
                elem = elem >> 7;
            }
        } else {
            encoded[offset + oidLen - 1] = (byte) elem;
        }

        offset += length;
    }

    public void encodeSequence(ASN1Sequence sequence) {
        throw new RuntimeException("Is not implemented yet"); //FIXME
    }

    public void encodeSequenceOf(ASN1SequenceOf sequenceOf) {
        throw new RuntimeException("Is not implemented yet"); //FIXME
    }

    public void encodeSet(ASN1Set set) {
        throw new RuntimeException("Is not implemented yet"); //FIXME
    }

    public void encodeSetOf(ASN1SetOf setOf) {
        throw new RuntimeException("Is not implemented yet"); //FIXME
    }

    public void encodeString() {
        System.arraycopy(content, 0, encoded, offset, length);
        offset += length;
    }

    public void getChoiceLength(ASN1Choice choice) {
        throw new RuntimeException("Is not implemented yet"); //FIXME
    }

    public void getExplicitLength(ASN1Explicit sequence) {
        throw new RuntimeException("Is not implemented yet"); //FIXME
    }

    public void getSequenceLength(ASN1Sequence sequence) {
        throw new RuntimeException("Is not implemented yet"); //FIXME
    }

    public void getSequenceOfLength(ASN1SequenceOf sequence) {
        throw new RuntimeException("Is not implemented yet"); //FIXME
    }

    public void getSetLength(ASN1Set set) {
        throw new RuntimeException("Is not implemented yet"); //FIXME
    }

    public void getSetOfLength(ASN1SetOf setOf) {
        throw new RuntimeException("Is not implemented yet"); //FIXME
    }
}
