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

package java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import libcore.io.Memory;

/**
 * UUID is an immutable representation of a 128-bit universally unique
 * identifier (UUID).
 * <p>
 * There are multiple, variant layouts of UUIDs, but this class is based upon
 * variant 2 of <a href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>, the
 * Leach-Salz variant. This class can be used to model alternate variants, but
 * most of the methods will be unsupported in those cases; see each method for
 * details.
 *
 * @since 1.5
 */
public final class UUID implements Serializable, Comparable<UUID> {

    private static final long serialVersionUID = -4856846361193249489L;

    private static SecureRandom rng;

    private long mostSigBits;
    private long leastSigBits;

    private transient int variant;
    private transient int version;
    private transient long timestamp;
    private transient int clockSequence;
    private transient long node;
    private transient int hash;

    /**
     * <p>
     * Constructs an instance with the specified bits.
     *
     * @param mostSigBits
     *            The 64 most significant bits of the UUID.
     * @param leastSigBits
     *            The 64 least significant bits of the UUID.
     */
    public UUID(long mostSigBits, long leastSigBits) {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
        init();
    }

    /**
     * <p>
     * Sets up the transient fields of this instance based on the current values
     * of the {@code mostSigBits} and {@code leastSigBits} fields.
     */
    private void init() {
        // setup hash field
        int msbHash = (int) (mostSigBits ^ (mostSigBits >>> 32));
        int lsbHash = (int) (leastSigBits ^ (leastSigBits >>> 32));
        hash = msbHash ^ lsbHash;

        // setup variant field
        if ((leastSigBits & 0x8000000000000000L) == 0) {
            // MSB0 not set, NCS backwards compatibility variant
            variant = 0;
        } else if ((leastSigBits & 0x4000000000000000L) != 0) {
            // MSB1 set, either MS reserved or future reserved
            variant = (int) ((leastSigBits & 0xE000000000000000L) >>> 61);
        } else {
            // MSB1 not set, RFC 4122 variant
            variant = 2;
        }

        // setup version field
        version = (int) ((mostSigBits & 0x000000000000F000) >>> 12);

        if (variant != 2 && version != 1) {
            return;
        }

        // setup timestamp field
        long timeLow = (mostSigBits & 0xFFFFFFFF00000000L) >>> 32;
        long timeMid = (mostSigBits & 0x00000000FFFF0000L) << 16;
        long timeHigh = (mostSigBits & 0x0000000000000FFFL) << 48;
        timestamp = timeLow | timeMid | timeHigh;

        // setup clock sequence field
        clockSequence = (int) ((leastSigBits & 0x3FFF000000000000L) >>> 48);

        // setup node field
        node = (leastSigBits & 0x0000FFFFFFFFFFFFL);
    }

    /**
     * <p>
     * Generates a variant 2, version 4 (randomly generated number) UUID as per
     * <a href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
     *
     * @return an UUID instance.
     */
    public static UUID randomUUID() {
        byte[] data = new byte[16];
        // lock on the class to protect lazy init
        synchronized (UUID.class) {
            if (rng == null) {
                rng = new SecureRandom();
            }
        }
        rng.nextBytes(data);
        return makeUuid(data, 4);
    }

    /**
     * <p>
     * Generates a variant 2, version 3 (name-based, MD5-hashed) UUID as per <a
     * href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
     *
     * @param name
     *            the name used as byte array to create an UUID.
     * @return an UUID instance.
     */
    public static UUID nameUUIDFromBytes(byte[] name) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return makeUuid(md.digest(name), 3);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private static UUID makeUuid(byte[] hash, int version) {
        long msb = Memory.peekLong(hash, 0, ByteOrder.BIG_ENDIAN);
        long lsb = Memory.peekLong(hash, 8, ByteOrder.BIG_ENDIAN);
        // Set the version field.
        msb &= ~(0xfL << 12);
        msb |= ((long) version) << 12;
        // Set the variant field to 2. Note that the variant field is variable-width,
        // so supporting other variants is not just a matter of changing the constant 2 below!
        lsb &= ~(0x3L << 62);
        lsb |= 2L << 62;
        return new UUID(msb, lsb);
    }

    /**
     * <p>
     * Parses a UUID string with the format defined by {@link #toString()}.
     *
     * @param uuid
     *            the UUID string to parse.
     * @return an UUID instance.
     * @throws NullPointerException
     *             if {@code uuid} is {@code null}.
     * @throws IllegalArgumentException
     *             if {@code uuid} is not formatted correctly.
     */
    public static UUID fromString(String uuid) {
        if (uuid == null) {
            throw new NullPointerException("uuid == null");
        }

        String[] parts = uuid.split("-");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid UUID: " + uuid);
        }

        long m1 = Long.parsePositiveLong(parts[0], 16);
        long m2 = Long.parsePositiveLong(parts[1], 16);
        long m3 = Long.parsePositiveLong(parts[2], 16);

        long lsb1 = Long.parsePositiveLong(parts[3], 16);
        long lsb2 = Long.parsePositiveLong(parts[4], 16);

        long msb = (m1 << 32) | (m2 << 16) | m3;
        long lsb = (lsb1 << 48) | lsb2;

        return new UUID(msb, lsb);
    }

    /**
     * <p>
     * The 64 least significant bits of the UUID.
     *
     * @return the 64 least significant bits.
     */
    public long getLeastSignificantBits() {
        return leastSigBits;
    }

    /**
     * <p>
     * The 64 most significant bits of the UUID.
     *
     * @return the 64 most significant bits.
     */
    public long getMostSignificantBits() {
        return mostSigBits;
    }

    /**
     * <p>
     * The version of the variant 2 UUID as per <a
     * href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>. If the variant
     * is not 2, then the version will be 0.
     * <ul>
     * <li>1 - Time-based UUID</li>
     * <li>2 - DCE Security UUID</li>
     * <li>3 - Name-based with MD5 hashing UUID ({@link #nameUUIDFromBytes(byte[])})</li>
     * <li>4 - Randomly generated UUID ({@link #randomUUID()})</li>
     * <li>5 - Name-based with SHA-1 hashing UUID</li>
     * </ul>
     *
     * @return an {@code int} value.
     */
    public int version() {
        return version;
    }

    /**
     * <p>
     * The variant of the UUID as per <a
     * href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
     * <ul>
     * <li>0 - Reserved for NCS compatibility</li>
     * <li>2 - RFC 4122/Leach-Salz</li>
     * <li>6 - Reserved for Microsoft Corporation compatibility</li>
     * <li>7 - Reserved for future use</li>
     * </ul>
     *
     * @return an {@code int} value.
     */
    public int variant() {
        return variant;
    }

    /**
     * <p>
     * The timestamp value of the version 1, variant 2 UUID as per <a
     * href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
     *
     * @return a {@code long} value.
     * @throws UnsupportedOperationException
     *             if {@link #version()} is not 1.
     */
    public long timestamp() {
        if (version != 1) {
            throw new UnsupportedOperationException();
        }
        return timestamp;
    }

    /**
     * <p>
     * The clock sequence value of the version 1, variant 2 UUID as per <a
     * href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
     *
     * @return a {@code long} value.
     * @throws UnsupportedOperationException
     *             if {@link #version()} is not 1.
     */
    public int clockSequence() {
        if (version != 1) {
            throw new UnsupportedOperationException();
        }
        return clockSequence;
    }

    /**
     * <p>
     * The node value of the version 1, variant 2 UUID as per <a
     * href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
     *
     * @return a {@code long} value.
     * @throws UnsupportedOperationException
     *             if {@link #version()} is not 1.
     */
    public long node() {
        if (version != 1) {
            throw new UnsupportedOperationException();
        }
        return node;
    }

    /**
     * <p>
     * Compares this UUID to the specified UUID. The natural ordering of UUIDs
     * is based upon the value of the bits from most significant to least
     * significant.
     *
     * @param uuid
     *            the UUID to compare to.
     * @return a value of -1, 0 or 1 if this UUID is less than, equal to or
     *         greater than {@code uuid}.
     */
    public int compareTo(UUID uuid) {
        if (uuid == this) {
            return 0;
        }

        if (this.mostSigBits != uuid.mostSigBits) {
            return this.mostSigBits < uuid.mostSigBits ? -1 : 1;
        }

        // assert this.mostSigBits == uuid.mostSigBits;

        if (this.leastSigBits != uuid.leastSigBits) {
            return this.leastSigBits < uuid.leastSigBits ? -1 : 1;
        }

        // assert this.leastSigBits == uuid.leastSigBits;

        return 0;
    }

    /**
     * <p>
     * Compares this UUID to another object for equality. If {@code object}
     * is not {@code null}, is a UUID instance, and all bits are equal, then
     * {@code true} is returned.
     *
     * @param object
     *            the {@code Object} to compare to.
     * @return {@code true} if this UUID is equal to {@code object}
     *         or {@code false} if not.
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (this == object) {
            return true;
        }

        if (!(object instanceof UUID)) {
            return false;
        }

        UUID that = (UUID) object;

        return (this.leastSigBits == that.leastSigBits)
                && (this.mostSigBits == that.mostSigBits);
    }

    /**
     * <p>
     * Returns a hash value for this UUID that is consistent with the
     * {@link #equals(Object)} method.
     *
     * @return an {@code int} value.
     */
    @Override
    public int hashCode() {
        return hash;
    }

    /**
     * <p>
     * Returns a string representation of this UUID in the following format, as
     * per <a href="http://www.ietf.org/rfc/rfc4122.txt">RFC 4122</a>.
     *
     * <pre>
     *            UUID                   = time-low &quot;-&quot; time-mid &quot;-&quot;
     *                                     time-high-and-version &quot;-&quot;
     *                                     clock-seq-and-reserved
     *                                     clock-seq-low &quot;-&quot; node
     *            time-low               = 4hexOctet
     *            time-mid               = 2hexOctet
     *            time-high-and-version  = 2hexOctet
     *            clock-seq-and-reserved = hexOctet
     *            clock-seq-low          = hexOctet
     *            node                   = 6hexOctet
     *            hexOctet               = hexDigit hexDigit
     *            hexDigit =
     *                &quot;0&quot; / &quot;1&quot; / &quot;2&quot; / &quot;3&quot; / &quot;4&quot; / &quot;5&quot; / &quot;6&quot; / &quot;7&quot; / &quot;8&quot; / &quot;9&quot; /
     *                &quot;a&quot; / &quot;b&quot; / &quot;c&quot; / &quot;d&quot; / &quot;e&quot; / &quot;f&quot; /
     *                &quot;A&quot; / &quot;B&quot; / &quot;C&quot; / &quot;D&quot; / &quot;E&quot; / &quot;F&quot;
     * </pre>
     *
     * @return a String instance.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(36);
        String msbStr = Long.toHexString(mostSigBits);
        if (msbStr.length() < 16) {
            int diff = 16 - msbStr.length();
            for (int i = 0; i < diff; i++) {
                builder.append('0');
            }
        }
        builder.append(msbStr);
        builder.insert(8, '-');
        builder.insert(13, '-');
        builder.append('-');
        String lsbStr = Long.toHexString(leastSigBits);
        if (lsbStr.length() < 16) {
            int diff = 16 - lsbStr.length();
            for (int i = 0; i < diff; i++) {
                builder.append('0');
            }
        }
        builder.append(lsbStr);
        builder.insert(23, '-');
        return builder.toString();
    }

    /**
     * <p>
     * Resets the transient fields to match the behavior of the constructor.
     *
     * @param in
     *            the {@code InputStream} to read from.
     * @throws IOException
     *             if {@code in} throws it.
     * @throws ClassNotFoundException
     *             if {@code in} throws it.
     */
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        // read in non-transient fields
        in.defaultReadObject();
        // setup transient fields
        init();
    }
}
