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
import java.math.BigInteger;

/**
 * This class represents ASN.1 Integer type.
 *
 * @see <a href="http://asn1.elibel.tm.fr/en/standards/index.htm">ASN.1</a>
 */
public final class ASN1Integer extends ASN1Primitive {

    /** default implementation */
    private static final ASN1Integer ASN1 = new ASN1Integer();

    /**
     * Constructs ASN.1 Integer type
     *
     * The constructor is provided for inheritance purposes
     * when there is a need to create a custom ASN.1 Integer type.
     * To get a default implementation it is recommended to use
     * getInstance() method.
     */
    public ASN1Integer() {
        super(TAG_INTEGER);
    }

    /**
     * Returns ASN.1 Integer type default implementation
     *
     * The default implementation works with encoding
     * that is represented as byte array in two's-complement notation.
     *
     * @return ASN.1 Integer type default implementation
     */
    public static ASN1Integer getInstance() {
        return ASN1;
    }

    public Object decode(BerInputStream in) throws IOException {
        in.readInteger();

        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }

    /**
     * Extracts array of bytes from BER input stream.
     *
     * @return array of bytes
     */
    public Object getDecodedObject(BerInputStream in) throws IOException {
        byte[] bytesEncoded = new byte[in.length];
        System.arraycopy(in.buffer, in.contentOffset, bytesEncoded, 0,
                in.length);
        return bytesEncoded;
    }

    public void encodeContent(BerOutputStream out) {
        out.encodeInteger();
    }

    public void setEncodingContent(BerOutputStream out) {
        out.length = ((byte[]) out.content).length;
    }

    /**
     * Converts decoded ASN.1 Integer to int value.
     * If the object represents an integer value
     * larger than 32 bits, the high bits will be lost.
     *
     * @param decoded a decoded object corresponding to this type
     * @return decoded int value.
     */
    public static int toIntValue(Object decoded) {
        return new BigInteger((byte[]) decoded).intValue();
    }

    /**
     * Converts decoded ASN.1 Integer to a BigInteger.
     *
     * @param decoded a decoded object corresponding to this type
     * @return decoded BigInteger value.
     */
    public static BigInteger toBigIntegerValue(Object decoded) {
        return new BigInteger((byte[]) decoded);
    }

    /**
     * Converts primitive int value to a form most suitable for encoding.
     *
     * @param value primitive value to be encoded
     * @return object suitable for encoding
     */
    public static Object fromIntValue(int value) {
        return BigInteger.valueOf(value).toByteArray();
    }
}
