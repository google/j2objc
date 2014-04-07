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
* @author Alexey V. Varlamov, Stepan M. Mishura
* @version $Revision$
*/

package org.apache.harmony.security.asn1;

/**
 * Represents ASN.1 bit string value
 *
 * @see <a href="http://asn1.elibel.tm.fr/en/standards/index.htm">ASN.1</a>
 */
public final class BitString {

    private static final byte[] SET_MASK = { (byte) 128, 64, 32, 16, 8, 4, 2, 1 };

    private static final byte[] RESET_MASK = { 0x7f, (byte) 0xbf, (byte) 0xdf,
            (byte) 0xef, (byte) 0xf7, (byte) 0xfb, (byte) 0xfd, (byte) 0xfe, };

    /** Sequence of bits padded with unused bits. */
    public final byte[] bytes;

    /** Number of unused bits in the last byte. */
    public final int unusedBits;

    /**
     * @param bytes array of bytes that represents bit string,
     *                including unused bits
     * @param unusedBits number of unused bits
     * @throws IllegalArgumentException - if parameters are invalid
     */
    public BitString(byte[] bytes, int unusedBits) {
        // constraints are set according X.690
        if (unusedBits < 0 || unusedBits > 7) {
            throw new IllegalArgumentException("Number of unused bits MUST be in range 0-7");
        }

        if (bytes.length == 0 && unusedBits != 0) {
            throw new IllegalArgumentException("For empty bit string unused bits MUST be 0");
        }

        this.bytes = bytes;
        this.unusedBits = unusedBits;
    }

    public BitString(boolean[] values) {
        unusedBits = values.length % 8;
        int size = values.length / 8;
        if (unusedBits != 0) {
            size++;
        }
        bytes = new byte[size];
        for (int i = 0; i < values.length; i++) {
            setBit(i, values[i]);
        }
    }

    public boolean getBit(int bit) {
        int offset = bit % 8;
        int index = bit / 8;
        return (bytes[index] & SET_MASK[offset]) != 0;
    }

    public void setBit(int bit, boolean value) {
        int offset = bit % 8;
        int index = bit / 8;
        if (value) {
            bytes[index] |= SET_MASK[offset];
        } else {
            bytes[index] &= RESET_MASK[offset];
        }
    }

    public boolean[] toBooleanArray() {
        boolean[] result = new boolean[bytes.length * 8 - unusedBits];
        for (int i = 0; i < result.length; i++) {
            result[i] = getBit(i);
        }
        return result;
    }
}
