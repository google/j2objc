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

package java.lang;

/**
 * The wrapper for the primitive type {@code byte}.
 *
 * @since 1.1
 */
//@FindBugsSuppressWarnings("DM_NUMBER_CTOR")
public final class Byte extends Number implements Comparable<Byte> {

    private static final long serialVersionUID = -7183698231559129828L;

    /**
     * The value which the receiver represents.
     */
    private final byte value;

    /**
     * The maximum {@code Byte} value, 2<sup>7</sup>-1.
     */
    public static final byte MAX_VALUE = (byte) 0x7F;

    /**
     * The minimum {@code Byte} value, -2<sup>7</sup>.
     */
    public static final byte MIN_VALUE = (byte) 0x80;

    /**
     * The number of bits needed to represent a {@code Byte} value in two's
     * complement form.
     *
     * @since 1.5
     */
    public static final int SIZE = 8;

    /**
     * The {@link Class} object that represents the primitive type {@code byte}.
     */
    @SuppressWarnings("unchecked")
    public static final Class<Byte> TYPE
            = (Class<Byte>) byte[].class.getComponentType();
    // Note: Byte.TYPE can't be set to "byte.class", since *that* is
    // defined to be "java.lang.Byte.TYPE";

    /**
     * Constructs a new {@code Byte} with the specified primitive byte value.
     *
     * @param value
     *            the primitive byte value to store in the new instance.
     */
    public Byte(byte value) {
        this.value = value;
    }

    /**
     * Constructs a new {@code Byte} from the specified string.
     *
     * @param string
     *            the string representation of a single byte value.
     * @throws NumberFormatException
     *             if {@code string} cannot be parsed as a byte value.
     * @see #parseByte(String)
     */
    public Byte(String string) throws NumberFormatException {
        this(parseByte(string));
    }

    /**
     * Gets the primitive value of this byte.
     *
     * @return this object's primitive value.
     */
    @Override
    public byte byteValue() {
        return value;
    }

    /**
     * Compares this object to the specified byte object to determine their
     * relative order.
     *
     * @param object
     *            the byte object to compare this object to.
     * @return a negative value if the value of this byte is less than the value
     *         of {@code object}; 0 if the value of this byte and the value of
     *         {@code object} are equal; a positive value if the value of this
     *         byte is greater than the value of {@code object}.
     * @see java.lang.Comparable
     * @since 1.2
     */
    public int compareTo(Byte object) {
        return compare(value, object.value);
    }

    /**
     * Compares two {@code byte} values.
     * @return 0 if lhs = rhs, less than 0 if lhs &lt; rhs, and greater than 0 if lhs &gt; rhs.
     * @since 1.7
     */
    public static int compare(byte lhs, byte rhs) {
        return lhs > rhs ? 1 : (lhs < rhs ? -1 : 0);
    }

    /**
     * Parses the specified string and returns a {@code Byte} instance if the
     * string can be decoded into a single byte value. The string may be an
     * optional minus sign "-" followed by a hexadecimal ("0x..." or "#..."),
     * octal ("0..."), or decimal ("...") representation of a byte.
     *
     * @param string
     *            a string representation of a single byte value.
     * @return a {@code Byte} containing the value represented by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} cannot be parsed as a byte value.
     */
    public static Byte decode(String string) throws NumberFormatException {
        int intValue = Integer.decode(string);
        byte result = (byte) intValue;
        if (result == intValue) {
            return valueOf(result);
        }
        throw new NumberFormatException("Value out of range for byte: \"" + string + "\"");
    }

    @Override
    public double doubleValue() {
        return value;
    }

    /**
     * Compares this object with the specified object and indicates if they are
     * equal. In order to be equal, {@code object} must be an instance of
     * {@code Byte} and have the same byte value as this object.
     *
     * @param object
     *            the object to compare this byte with.
     * @return {@code true} if the specified object is equal to this
     *         {@code Byte}; {@code false} otherwise.
     */
    @Override
    //@FindBugsSuppressWarnings("RC_REF_COMPARISON")
    public boolean equals(Object object) {
        return (object == this) || ((object instanceof Byte) && (((Byte) object).value == value));
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public int intValue() {
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    /**
     * Parses the specified string as a signed decimal byte value. The ASCII
     * character \u002d ('-') is recognized as the minus sign.
     *
     * @param string
     *            the string representation of a single byte value.
     * @return the primitive byte value represented by {@code string}.
     * @throws NumberFormatException
     *             if {@code string} can not be parsed as a byte value.
     */
    public static byte parseByte(String string) throws NumberFormatException {
        return parseByte(string, 10);
    }

    /**
     * Parses the specified string as a signed byte value using the specified
     * radix. The ASCII character \u002d ('-') is recognized as the minus sign.
     *
     * @param string
     *            the string representation of a single byte value.
     * @param radix
     *            the radix to use when parsing.
     * @return the primitive byte value represented by {@code string} using
     *         {@code radix}.
     * @throws NumberFormatException
     *             if {@code string} can not be parsed as a byte value, or
     *             {@code radix < Character.MIN_RADIX ||
     *             radix > Character.MAX_RADIX}.
     */
    public static byte parseByte(String string, int radix) throws NumberFormatException {
        int intValue = Integer.parseInt(string, radix);
        byte result = (byte) intValue;
        if (result == intValue) {
            return result;
        }
        throw new NumberFormatException("Value out of range for byte: \"" + string + "\"");
    }

    @Override
    public short shortValue() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    /**
     * Returns a two-digit hex string. That is, -1 becomes "ff" or "FF" and 2 becomes "02".
     * @hide internal use only
     */
    public static String toHexString(byte b, boolean upperCase) {
        return IntegralToString.byteToHexString(b, upperCase);
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * specified byte value.
     *
     * @param value
     *            the byte to convert to a string.
     * @return a printable representation of {@code value}.
     */
    public static String toString(byte value) {
        return Integer.toString(value);
    }

    /**
     * Parses the specified string as a signed decimal byte value.
     *
     * @param string
     *            the string representation of a single byte value.
     * @return a {@code Byte} instance containing the byte value represented by
     *         {@code string}.
     * @throws NumberFormatException
     *             if {@code string} can not be parsed as a byte value.
     * @see #parseByte(String)
     */
    public static Byte valueOf(String string) throws NumberFormatException {
        return valueOf(parseByte(string));
    }

    /**
     * Parses the specified string as a signed byte value using the specified
     * radix.
     *
     * @param string
     *            the string representation of a single byte value.
     * @param radix
     *            the radix to use when parsing.
     * @return a {@code Byte} instance containing the byte value represented by
     *         {@code string} using {@code radix}.
     * @throws NumberFormatException
     *             if {@code string} can not be parsed as a byte value, or
     *             {@code radix < Character.MIN_RADIX ||
     *             radix > Character.MAX_RADIX}.
     * @see #parseByte(String, int)
     */
    public static Byte valueOf(String string, int radix) throws NumberFormatException {
        return valueOf(parseByte(string, radix));
    }

    /**
     * Returns a {@code Byte} instance for the specified byte value.
     * <p>
     * If it is not necessary to get a new {@code Byte} instance, it is
     * recommended to use this method instead of the constructor, since it
     * maintains a cache of instances which may result in better performance.
     *
     * @param b
     *            the byte value to store in the instance.
     * @return a {@code Byte} instance containing {@code b}.
     * @since 1.5
     */
    public static Byte valueOf(byte b) {
        return VALUES[b + 128];
    }

    /**
     * A cache of instances used by {@link Byte#valueOf(byte)} and auto-boxing
     */
    private static final Byte[] VALUES = new Byte[256];

    static {
        fillValues(VALUES);
    }

    private static native void fillValues(Byte[] values) /*-[
      Class self = [JavaLangByte class];
      size_t objSize = class_getInstanceSize(self);
      uintptr_t ptr = (uintptr_t)calloc(objSize, 256);
      id *buf = values->buffer_;
      for (jint i = -128; i < 128; i++) {
        id obj = objc_constructInstance(self, (void *)ptr);
        JavaLangByte_initWithByte_(obj, (jbyte)i);
        *(buf++) = obj;
        ptr += objSize;
      }
    ]-*/;

    /*
     * These ObjC methods are needed to support subclassing of NSNumber.
     * objCType and charValue are used by descriptionWithLocale:.
     * getValue: is used by copyWithZone:.
     */
    /*-[
    - (const char *)objCType {
      return "c";
    }

    - (void)getValue:(void *)buffer {
      *((char *) buffer) = value_;
    }
    ]-*/
}
