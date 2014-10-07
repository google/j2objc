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
import java.util.ArrayList;


/**
 * Decodes ASN.1 types encoded with BER (X.690)
 *
 * @see <a href="http://asn1.elibel.tm.fr/en/standards/index.htm">ASN.1</a>
 */

public class BerInputStream {

    private final InputStream in;
    protected byte[] buffer;

    /**
     * The position in the buffer.
     * Next read must place data into the buffer from this offset
     */
    protected int offset = 0;

    /**
     * The buffer increment size.
     * Must be reasonable big to reallocate memory not to often.
     * Primary is used for decoding indefinite length encoding
     */
    private static final int BUF_INCREASE_SIZE = 1024 * 16;

    /** Indicates indefinite length of the current type */
    protected static final int INDEFINIT_LENGTH = -1;

    /** Current decoded tag */
    public int tag;

    /** Current decoded length */
    protected int length;

    /** Current decoded content */
    public Object content;

    /** Current decoded tag offset */
    protected int tagOffset;

    /** Current decoded content offset */
    protected int contentOffset;

    /**
     * Creates stream for decoding.
     */
    public BerInputStream(byte[] encoded) throws IOException {
        this(encoded, 0, encoded.length);
    }

    /**
     * Creates stream for decoding.
     *
     * @param encoded bytes array to be decoded
     * @param offset the encoding offset
     * @param expectedLength expected length of full encoding, this includes
     *     identifier, length an content octets
     */
    public BerInputStream(byte[] encoded, int offset, int expectedLength) throws IOException {
        this.in = null;
        this.buffer = encoded;
        this.offset = offset;

        next();

        // compare expected and decoded length
        if (length != INDEFINIT_LENGTH
                && (offset + expectedLength) != (this.offset + this.length)) {
            throw new ASN1Exception("Wrong content length");
        }
    }

    /**
     * Creates stream for decoding.
     *
     * Allocates initial buffer of default size
     */
    public BerInputStream(InputStream in) throws IOException {
        this(in, BUF_INCREASE_SIZE);
    }

    /**
     * Creates stream for decoding.
     *
     * @param initialSize the internal buffer initial size
     */
    public BerInputStream(InputStream in, int initialSize) throws IOException {
        this.in = in;
        buffer = new byte[initialSize];

        next();

        if (length != INDEFINIT_LENGTH) {
            // input stream has definite length encoding
            // check allocated length to avoid further reallocations
            if (buffer.length < (length + offset)) {
                byte[] newBuffer = new byte[length + offset];
                System.arraycopy(buffer, 0, newBuffer, 0, offset);
                buffer = newBuffer;
            }
        } else {
            isIndefinedLength = true;
            throw new ASN1Exception("Decoding indefinite length encoding is not supported");
        }
    }

    /**
     * Resets this stream to initial state.
     *
     * @param encoded a new bytes array to be decoded
     * @throws IOException if an error occurs
     */
    public final void reset(byte[] encoded) throws IOException {
        buffer = encoded;
        next();
    }

    /**
     * Decodes next encoded type.
     * Initializes tag, length, tagOffset and contentOffset variables
     *
     * @return next decoded tag
     * @throws IOException if error occured
     */
    public int next() throws IOException {
        tagOffset = offset;

        // read tag
        tag = read();

        // read length
        length = read();
        if (length != 0x80) { // definite form
            // long or short length form
            if ((length & 0x80) != 0) { // long form
                int numOctets = length & 0x7F;

                if (numOctets > 5) {
                    throw new ASN1Exception("Too long encoding at [" + tagOffset + "]"); //FIXME message
                }

                // collect this value length
                length = read();
                for (int i = 1; i < numOctets; i++) {
                    int ch = read();
                    length = (length << 8) + ch;//read();
                }

                if (length > 0xFFFFFF) {
                    throw new ASN1Exception("Too long encoding at [" + tagOffset + "]"); //FIXME message
                }
            }
        } else { //indefinite form
            length = INDEFINIT_LENGTH;
        }
        contentOffset = offset;

        return tag;
    }

    /**
     * Returns the length of the encoding
     */
    public static int getLength(byte[] encoding) {
        int length = encoding[1] & 0xFF;
        int numOctets = 0;
        if ((length & 0x80) != 0) { // long form
            numOctets = length & 0x7F;

            // collect this value length
            length = encoding[2] & 0xFF;
            for (int i = 3; i < numOctets + 2; i++) {
                length = (length << 8) + (encoding[i] & 0xFF);
            }
        }
        //    tag length long_form content
        return 1 + 1 + numOctets + length;
    }

    /**
     * Decodes ASN.1 bitstring type
     */
    public void readBitString() throws IOException {
        if (tag == ASN1Constants.TAG_BITSTRING) {

            if (length == 0) {
                throw new ASN1Exception("ASN.1 Bitstring: wrong length. Tag at [" + tagOffset + "]");
            }

            readContent();

            // content: check unused bits
            if (buffer[contentOffset] > 7) {
                throw new ASN1Exception("ASN.1 Bitstring: wrong content at [" + contentOffset
                        + "]. A number of unused bits MUST be in range 0 to 7");
            }

            if (length == 1 && buffer[contentOffset] != 0) {
                throw new ASN1Exception("ASN.1 Bitstring: wrong content at [" + contentOffset
                        + "]. For empty string unused bits MUST be 0");
            }

        } else if (tag == ASN1Constants.TAG_C_BITSTRING) {
            throw new ASN1Exception("Decoding constructed ASN.1 bitstring  type is not provided");
        } else {
            throw expected("bitstring");
        }
    }

    /**
     * Decodes ASN.1 Enumerated type
     */
    public void readEnumerated() throws IOException {
        if (tag != ASN1Constants.TAG_ENUM) {
            throw expected("enumerated");
        }

        // check encoded length
        if (length == 0) {
            throw new ASN1Exception("ASN.1 enumerated: wrong length for identifier at ["
                    + tagOffset + "]");
        }

        readContent();

        // check encoded content
        if (length > 1) {
            int bits = buffer[contentOffset] & 0xFF;
            if (buffer[contentOffset + 1] < 0) {
                bits += 0x100;
            }

            if (bits == 0 || bits == 0x1FF) {
                throw new ASN1Exception("ASN.1 enumerated: wrong content at [" + contentOffset
                        + "]. An integer MUST be encoded in minimum number of octets");
            }
        }
    }

    /**
     * Decodes ASN.1 boolean type
     */
    public void readBoolean() throws IOException {
        if (tag != ASN1Constants.TAG_BOOLEAN) {
            throw expected("boolean");
        }

        // check encoded length
        if (length != 1) {
            throw new ASN1Exception("Wrong length for ASN.1 boolean at [" + tagOffset + "]");
        }

        readContent();
    }

    /** The last choice index */
    public int choiceIndex;

    /** Keeps last decoded: year, month, day, hour, minute, second, millisecond */
    public int[] times;

    /**
     * Decodes ASN.1 GeneralizedTime type
     *
     * @throws IOException if error occured
     */
    public void readGeneralizedTime() throws IOException {
        if (tag == ASN1Constants.TAG_GENERALIZEDTIME) {
            // FIXME: any other optimizations?
            readContent();
            // FIXME store string somewhere to allow a custom time type perform
            // additional checks

            // check syntax: the last char MUST be Z
            if (buffer[offset - 1] != 'Z') {
                // FIXME support only format that is acceptable for DER
                throw new ASN1Exception("ASN.1 GeneralizedTime: encoded format is not implemented");
            }

            // check syntax: MUST be YYYYMMDDHHMMSS[(./,)DDD]'Z'
            if (length != 15 && (length < 17 || length > 19)) {
                throw new ASN1Exception("ASN.1 GeneralizedTime wrongly encoded at ["
                        + contentOffset + "]");
            }

            // check content: milliseconds
            if (length > 16) {
                byte char14 = buffer[contentOffset + 14];
                if (char14 != '.' && char14 != ',') {
                    throw new ASN1Exception("ASN.1 GeneralizedTime wrongly encoded at ["
                            + contentOffset + "]");
                }
            }

            if (times == null) {
                times = new int[7];
            }
            times[0] = strToInt(contentOffset, 4); // year
            times[1] = strToInt(contentOffset + 4, 2); // month
            times[2] = strToInt(contentOffset + 6, 2); // day
            times[3] = strToInt(contentOffset + 8, 2); // hour
            times[4] = strToInt(contentOffset + 10, 2); // minute
            times[5] = strToInt(contentOffset + 12, 2); // second

            if (length > 16) {
                // FIXME optimize me
                times[6] = strToInt(contentOffset + 15, length - 16);

                if (length == 17) {
                    times[6] = times[6] * 100;
                } else if (length == 18) {
                    times[6] = times[6] * 10;
                }
            }

            // FIXME check all values for valid numbers!!!
        } else if (tag == ASN1Constants.TAG_C_GENERALIZEDTIME) {
            throw new ASN1Exception("Decoding constructed ASN.1 GeneralizedTime type is not supported");
        } else {
            throw expected("GeneralizedTime");
        }
    }

    /**
     * Decodes ASN.1 UTCTime type
     *
     * @throws IOException if an I/O error occurs or the end of the stream is reached
     */
    public void readUTCTime() throws IOException {
        if (tag == ASN1Constants.TAG_UTCTIME) {
            switch (length) {
            case ASN1UTCTime.UTC_HM:
            case ASN1UTCTime.UTC_HMS:
                break;
            case ASN1UTCTime.UTC_LOCAL_HM:
            case ASN1UTCTime.UTC_LOCAL_HMS:
                // FIXME only coordinated universal time formats are supported
                throw new ASN1Exception("ASN.1 UTCTime: local time format is not supported");
            default:
                throw new ASN1Exception("ASN.1 UTCTime: wrong length, identifier at " + tagOffset);
            }

            // FIXME: any other optimizations?
            readContent();

            // FIXME store string somewhere to allow a custom time type perform
            // additional checks

            // check syntax: the last char MUST be Z
            if (buffer[offset - 1] != 'Z') {
                throw new ASN1Exception("ASN.1 UTCTime wrongly encoded at ["
                        + contentOffset + ']');
            }

            if (times == null) {
                times = new int[7];
            }

            times[0] = strToInt(contentOffset, 2); // year
            if (times[0] > 49) {
                times[0] += 1900;
            } else {
                times[0] += 2000;
            }

            times[1] = strToInt(contentOffset + 2, 2); // month
            times[2] = strToInt(contentOffset + 4, 2); // day
            times[3] = strToInt(contentOffset + 6, 2); // hour
            times[4] = strToInt(contentOffset + 8, 2); // minute

            if (length == ASN1UTCTime.UTC_HMS) {
                times[5] = strToInt(contentOffset + 10, 2); // second
            }

            // FIXME check all time values for valid numbers!!!
        } else if (tag == ASN1Constants.TAG_C_UTCTIME) {
            throw new ASN1Exception("Decoding constructed ASN.1 UTCTime type is not supported");
        } else {
            throw expected("UTCTime");
        }
    }

    private int strToInt(int off, int count) throws ASN1Exception {
        int result = 0;
        for (int i = off, end = off + count; i < end; i++) {
            int c = buffer[i] - 48;
            if (c < 0 || c > 9) {
                throw new ASN1Exception("Time encoding has invalid char");
            }
            result = result * 10 + c;
        }
        return result;
    }

    /**
     * Decodes ASN.1 Integer type
     */
    public void readInteger() throws IOException {
        if (tag != ASN1Constants.TAG_INTEGER) {
            throw expected("integer");
        }

        // check encoded length
        if (length < 1) {
            throw new ASN1Exception("Wrong length for ASN.1 integer at [" + tagOffset + "]");
        }

        readContent();

        // check encoded content
        if (length > 1) {
            byte firstByte = buffer[offset - length];
            byte secondByte = (byte) (buffer[offset - length + 1] & 0x80);

            if (firstByte == 0 && secondByte == 0 || firstByte == (byte) 0xFF
                    && secondByte == (byte) 0x80) {
                throw new ASN1Exception("Wrong content for ASN.1 integer at [" + (offset - length) + "]. An integer MUST be encoded in minimum number of octets");
            }
        }
    }

    /**
     * Decodes ASN.1 Octetstring type
     */
    public void readOctetString() throws IOException {
        if (tag == ASN1Constants.TAG_OCTETSTRING) {
            readContent();
        } else if (tag == ASN1Constants.TAG_C_OCTETSTRING) {
            throw new ASN1Exception("Decoding constructed ASN.1 octet string type is not supported");
        } else {
            throw expected("octetstring");
        }
    }

    private ASN1Exception expected(String what) throws ASN1Exception {
        throw new ASN1Exception("ASN.1 " + what + " identifier expected at [" + tagOffset + "], got " + Integer.toHexString(tag));
    }

    public int oidElement;

    /**
     * Decodes ASN.1 ObjectIdentifier type
     */
    public void readOID() throws IOException {
        if (tag != ASN1Constants.TAG_OID) {
            throw expected("OID");
        }

        // check encoded length
        if (length < 1) {
            throw new ASN1Exception("Wrong length for ASN.1 object identifier at [" + tagOffset + "]");
        }

        readContent();

        // check content: last encoded byte (8th bit MUST be zero)
        if ((buffer[offset - 1] & 0x80) != 0) {
            throw new ASN1Exception("Wrong encoding at [" + (offset - 1) + "]");
        }

        oidElement = 1;
        for (int i = 0; i < length; i++, ++oidElement) {
            while ((buffer[contentOffset + i] & 0x80) == 0x80) {
                i++;
            }
        }
    }

    /**
     * Decodes ASN.1 Sequence type
     */
    public void readSequence(ASN1Sequence sequence) throws IOException {
        if (tag != ASN1Constants.TAG_C_SEQUENCE) {
            throw expected("sequence");
        }

        int begOffset = offset;
        int endOffset = begOffset + length;

        ASN1Type[] type = sequence.type;

        int i = 0;

        if (isVerify) {

            for (; (offset < endOffset) && (i < type.length); i++) {

                next();
                while (!type[i].checkTag(tag)) {
                    // check whether it is optional component or not
                    if (!sequence.OPTIONAL[i] || (i == type.length - 1)) {
                        throw new ASN1Exception("ASN.1 Sequence: mandatory value is missing at [" + tagOffset + "]");
                    }
                    i++;
                }

                type[i].decode(this);
            }

            // check the rest of components
            for (; i < type.length; i++) {
                if (!sequence.OPTIONAL[i]) {
                    throw new ASN1Exception("ASN.1 Sequence: mandatory value is missing at [" + tagOffset + "]");
                }
            }

        } else {
            int seqTagOffset = tagOffset; //store tag offset

            Object[] values = new Object[type.length];
            for (; (offset < endOffset) && (i < type.length); i++) {

                next();
                while (!type[i].checkTag(tag)) {
                    // check whether it is optional component or not
                    if (!sequence.OPTIONAL[i] || (i == type.length - 1)) {
                        throw new ASN1Exception("ASN.1 Sequence: mandatory value is missing at [" + tagOffset + "]");
                    }

                    // sets default value
                    if (sequence.DEFAULT[i] != null) {
                        values[i] = sequence.DEFAULT[i];
                    }
                    i++;
                }
                values[i] = type[i].decode(this);
            }

            // check the rest of components
            for (; i < type.length; i++) {
                if (!sequence.OPTIONAL[i]) {
                    throw new ASN1Exception("ASN.1 Sequence: mandatory value is missing at [" + tagOffset + "]");
                }
                if (sequence.DEFAULT[i] != null) {
                    values[i] = sequence.DEFAULT[i];
                }
            }
            content = values;

            tagOffset = seqTagOffset; //retrieve tag offset
        }

        if (offset != endOffset) {
            throw new ASN1Exception("Wrong encoding at [" + begOffset + "]. Content's length and encoded length are not the same");
        }
    }

    /**
     * Decodes ASN.1 SequenceOf type
     */
    public void readSequenceOf(ASN1SequenceOf sequenceOf) throws IOException {
        if (tag != ASN1Constants.TAG_C_SEQUENCEOF) {
            throw expected("sequenceOf");
        }

        decodeValueCollection(sequenceOf);
    }

    /**
     * Decodes ASN.1 Set type
     */
    public void readSet(ASN1Set set) throws IOException {
        if (tag != ASN1Constants.TAG_C_SET) {
            throw expected("set");
        }

        throw new ASN1Exception("Decoding ASN.1 Set type is not supported");
    }

    /**
     * Decodes ASN.1 SetOf type
     */
    public void readSetOf(ASN1SetOf setOf) throws IOException {
        if (tag != ASN1Constants.TAG_C_SETOF) {
            throw expected("setOf");
        }

        decodeValueCollection(setOf);
    }

    private void decodeValueCollection(ASN1ValueCollection collection) throws IOException {
        int begOffset = offset;
        int endOffset = begOffset + length;

        ASN1Type type = collection.type;

        if (isVerify) {
            while (endOffset > offset) {
                next();
                type.decode(this);
            }
        } else {
            int seqTagOffset = tagOffset; //store tag offset

            ArrayList<Object> values = new ArrayList<Object>();
            while (endOffset > offset) {
                next();
                values.add(type.decode(this));
            }

            values.trimToSize();
            content = values;

            tagOffset = seqTagOffset; //retrieve tag offset
        }

        if (offset != endOffset) {
            throw new ASN1Exception("Wrong encoding at [" + begOffset + "]. Content's length and encoded length are not the same");
        }
    }

    /**
     * Decodes ASN.1 String type
     *
     * @throws IOException if an I/O error occurs or the end of the stream is reached
     */
    public void readString(ASN1StringType type) throws IOException {
        if (tag == type.id) {
            readContent();
        } else if (tag == type.constrId) {
            throw new ASN1Exception("Decoding constructed ASN.1 string type is not provided");
        } else {
            throw expected("string");
        }
    }

    /**
     * Returns encoded array.
     *
     * MUST be invoked after decoding corresponding ASN.1 notation
     */
    public byte[] getEncoded() {
        byte[] encoded = new byte[offset - tagOffset];
        System.arraycopy(buffer, tagOffset, encoded, 0, encoded.length);
        return encoded;
    }

    /**
     * Returns internal buffer used for decoding
     */
    public final byte[] getBuffer() {
        return buffer;
    }

    /**
     * Returns length of the current content for decoding
     */
    public final int getLength() {
        return length;
    }

    /**
     * Returns the current offset
     */
    public final int getOffset() {
        return offset;
    }

    /**
     * Returns end offset for the current encoded type
     */
    public final int getEndOffset() {
        return offset + length;
    }

    /**
     * Returns start offset for the current encoded type
     */
    public final int getTagOffset() {
        return tagOffset;
    }

    /**
     * Indicates verify or store mode.
     *
     * In store mode a decoded content is stored in a newly allocated
     * appropriate object. The <code>content</code> variable holds
     * a reference to the last created object.
     *
     * In verify mode a decoded content is not stored.
     */
    // FIXME it is used only for one case
    // decoding PCKS#8 Private Key Info notation
    // remove this option because it does decoding more complex
    protected boolean isVerify;

    /**
     * Sets verify mode.
     */
    public final void setVerify() {
        isVerify = true;
    }

    /**
     * Indicates defined or indefined reading mode for associated InputStream.
     *
     * This mode is defined by reading a length
     * for a first ASN.1 type from InputStream.
     */
    protected boolean isIndefinedLength;

    /**
     * Reads the next encoded byte from the encoded input stream.
     */
    protected int read() throws IOException {
        if (offset == buffer.length) {
            throw new ASN1Exception("Unexpected end of encoding");
        }

        if (in == null) {
            return buffer[offset++] & 0xFF;
        } else {
            int octet = in.read();
            if (octet == -1) {
                throw new ASN1Exception("Unexpected end of encoding");
            }

            buffer[offset++] = (byte) octet;

            return octet;
        }
    }

    /**
     * Reads the next encoded content from the encoded input stream.
     * The method MUST be used for reading a primitive encoded content.
     */
    public void readContent() throws IOException {
        if (offset + length > buffer.length) {
            throw new ASN1Exception("Unexpected end of encoding");
        }

        if (in == null) {
            offset += length;
        } else {
            int bytesRead = in.read(buffer, offset, length);

            if (bytesRead != length) {
                // if input stream didn't return all data at once
                // try to read it in several blocks
                int c = bytesRead;
                do {
                    if (c < 1 || bytesRead > length) {
                        throw new ASN1Exception("Failed to read encoded content");
                    }
                    c = in.read(buffer, offset + bytesRead, length - bytesRead);
                    bytesRead += c;
                } while (bytesRead != length);
            }

            offset += length;
        }
    }

    /**
     * Reallocates the buffer in order to make it
     * exactly the size of data it contains
     */
    public void compactBuffer() {
        if (offset != buffer.length) {
            byte[] newBuffer = new byte[offset];
            // restore buffer content
            System.arraycopy(buffer, 0, newBuffer, 0, offset);
            // set new buffer
            buffer = newBuffer;
        }
    }
    private Object[][] pool;

    public void put(Object key, Object entry) {
        if (pool == null) {
            pool = new Object[2][10];
        }

        int i = 0;
        for (; i < pool[0].length && pool[0][i] != null; i++) {
            if (pool[0][i] == key) {
                pool[1][i] = entry;
                return;
            }
        }

        if (i == pool[0].length) {
            Object[][] newPool = new Object[pool[0].length * 2][2];
            System.arraycopy(pool[0], 0, newPool[0], 0, pool[0].length);
            System.arraycopy(pool[1], 0, newPool[1], 0, pool[0].length);
            pool = newPool;
        } else {
            pool[0][i] = key;
            pool[1][i] = entry;
        }
    }

    public Object get(Object key) {
        if (pool == null) {
            return null;
        }

        for (int i = 0; i < pool[0].length; i++) {
            if (pool[0][i] == key) {
                return pool[1][i];
            }
        }
        return null;
    }
}
