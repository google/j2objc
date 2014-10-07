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
import java.io.InputStream;


/**
 * This abstract class is the super class for all ASN.1 types
 *
 * @see <a href="http://asn1.elibel.tm.fr/en/standards/index.htm">ASN.1</a>
 */
public abstract class ASN1Type implements ASN1Constants {

    /** Integer representation of primitive identifier. */
    public final int id;

    /** Integer representation of constructed identifier. */
    public final int constrId;

    /**
     * Constructs a primitive, universal ASN.1 type.
     *
     * @param tagNumber - ASN.1 tag number
     * @throws IllegalArgumentException - if tagNumber is invalid
     */
    public ASN1Type(int tagNumber) {
        this(CLASS_UNIVERSAL, tagNumber);
    }

    /**
     * Constructs an ASN.1 type.
     *
     * @param tagClass - tag class. MUST be
     *     CLASS_UNIVERSAL, CLASS_APPLICATION, CLASS_CONTEXTSPECIFIC, CLASS_PRIVATE
     * @param tagNumber - ASN.1 tag number.
     * @throws IllegalArgumentException - if tagClass or tagNumber is invalid
     */
    public ASN1Type(int tagClass, int tagNumber) {
        if (tagNumber < 0) {
            throw new IllegalArgumentException("tagNumber < 0");
        }

        if (tagClass != CLASS_UNIVERSAL && tagClass != CLASS_APPLICATION
                && tagClass != CLASS_CONTEXTSPECIFIC
                && tagClass != CLASS_PRIVATE) {
            throw new IllegalArgumentException("invalid tagClass");
        }

        if (tagNumber < 31) {
            // short form
            this.id = tagClass + tagNumber;
        } else {
            // long form
            throw new IllegalArgumentException("tag long form not implemented");
        }
        this.constrId = this.id + PC_CONSTRUCTED;
    }

    public final Object decode(byte[] encoded) throws IOException {
        return decode(new DerInputStream(encoded));
    }

    public final Object decode(byte[] encoded, int offset, int encodingLen)
            throws IOException {
        return decode(new DerInputStream(encoded, offset, encodingLen));
    }

    public final Object decode(InputStream in) throws IOException {
        return decode(new DerInputStream(in));
    }

    public final void verify(byte[] encoded) throws IOException {
        DerInputStream decoder = new DerInputStream(encoded);
        decoder.setVerify();
        decode(decoder);
    }

    public final void verify(InputStream in) throws IOException {
        DerInputStream decoder = new DerInputStream(in);
        decoder.setVerify();
        decode(decoder);
    }

    public final byte[] encode(Object object) {
        DerOutputStream out = new DerOutputStream(this, object);
        return out.encoded;
    }

    /**
     * Decodes ASN.1 type.
     *
     * @throws IOException if an I/O error occurs or the end of the stream is reached
     */
    public abstract Object decode(BerInputStream in) throws IOException;

    /**
     * Tests provided identifier.
     *
     * @param identifier identifier to be verified
     * @return true if identifier is associated with this ASN.1 type
     */
    public abstract boolean checkTag(int identifier);

    /**
     * Creates decoded object.
     *
     * Derived classes should override this method to provide creation for a
     * selected class of objects during decoding.
     *
     * The default implementation returns an object created by decoding stream.
     */
    protected Object getDecodedObject(BerInputStream in) throws IOException {
        return in.content;
    }

    /**
     * Encodes ASN.1 type.
     */
    public abstract void encodeASN(BerOutputStream out);

    public abstract void encodeContent(BerOutputStream out);

    public abstract void setEncodingContent(BerOutputStream out);

    public int getEncodedLength(BerOutputStream out) { //FIXME name
        //tag length
        int len = 1; //FIXME tag length = 1. what about long form?
        //for (; tag > 0; tag = tag >> 8, len++);

        // length length :-)
        len++;
        if (out.length > 127) {

            len++;
            for (int cur = out.length >> 8; cur > 0; len++) {
                cur = cur >> 8;
            }
        }
        len += out.length;

        return len;
    }

    @Override public String toString() {
        // TODO decide whether this method is necessary
        return getClass().getName() + "(tag: 0x" + Integer.toHexString(0xff & this.id) + ")";
    }
}
