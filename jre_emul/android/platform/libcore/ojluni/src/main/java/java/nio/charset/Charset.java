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

package java.nio.charset;

import com.google.j2objc.nio.charset.IOSCharset;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A charset is a named mapping between Unicode characters and byte sequences. Every
 * {@code Charset} can <i>decode</i>, converting a byte sequence into a sequence of characters,
 * and some can also <i>encode</i>, converting a sequence of characters into a byte sequence.
 * Use the method {@link #canEncode} to find out whether a charset supports both.
 *
 * <h4>Characters</h4>
 * <p>In the context of this class, <i>character</i> always refers to a Java character: a Unicode
 * code point in the range U+0000 to U+FFFF. (Java represents supplementary characters using surrogates.)
 * Not all byte sequences will represent a character, and not
 * all characters can necessarily be represented by a given charset. The method {@link #contains}
 * can be used to determine whether every character representable by one charset can also be
 * represented by another (meaning that a lossless transformation is possible from the contained
 * to the container).
 *
 * <h4>Encodings</h4>
 * <p>There are many possible ways to represent Unicode characters as byte sequences.
 * See <a href="http://www.unicode.org/reports/tr17/">UTR#17: Unicode Character Encoding Model</a>
 * for detailed discussion.
 *
 * <p>The most important mappings capable of representing every character are the Unicode
 * Transformation Format (UTF) charsets. Of those, UTF-8 and the UTF-16 family are the most
 * common. UTF-8 (described in <a href="http://www.ietf.org/rfc/rfc3629.txt">RFC 3629</a>)
 * encodes a character using 1 to 4 bytes. UTF-16 uses exactly 2 bytes per character (potentially
 * wasting space, but allowing efficient random access into BMP text), and UTF-32 uses
 * exactly 4 bytes per character (trading off even more space for efficient random access into text
 * that includes supplementary characters).
 *
 * <p>UTF-16 and UTF-32 encode characters directly, using their code point as a two- or four-byte
 * integer. This means that any given UTF-16 or UTF-32 byte sequence is either big- or
 * little-endian. To assist decoders, Unicode includes a special <i>byte order mark</i> (BOM)
 * character U+FEFF used to determine the endianness of a sequence. The corresponding byte-swapped
 * code point U+FFFE is guaranteed never to be assigned. If a UTF-16 decoder sees
 * {@code 0xfe, 0xff}, for example, it knows it's reading a big-endian byte sequence, while
 * {@code 0xff, 0xfe}, would indicate a little-endian byte sequence.
 *
 * <p>UTF-8 can contain a BOM, but since the UTF-8 encoding of a character always uses the same
 * byte sequence, there is no information about endianness to convey. Seeing the bytes
 * corresponding to the UTF-8 encoding of U+FEFF ({@code 0xef, 0xbb, 0xbf}) would only serve to
 * suggest that you're reading UTF-8. Note that BOMs are decoded as the U+FEFF character, and
 * will appear in the output character sequence. This means that a disadvantage to including a BOM
 * in UTF-8 is that most applications that use UTF-8 do not expect to see a BOM. (This is also a
 * reason to prefer UTF-8: it's one less complication to worry about.)
 *
 * <p>Because a BOM indicates how the data that follows should be interpreted, a BOM should occur
 * as the first character in a character sequence.
 *
 * <p>See the <a href="http://unicode.org/faq/utf_bom.html#BOM">Byte Order Mark (BOM) FAQ</a> for
 * more about dealing with BOMs.
 *
 * <h4>Endianness and BOM behavior</h4>
 *
 * <p>The following tables show the endianness and BOM behavior of the UTF-16 variants.
 *
 * <p>This table shows what the encoder writes. "BE" means that the byte sequence is big-endian,
 * "LE" means little-endian. "BE BOM" means a big-endian BOM (that is, {@code 0xfe, 0xff}).
 * <p><table width="100%">
 * <tr> <th>Charset</th>  <th>Encoder writes</th>  </tr>
 * <tr> <td>UTF-16BE</td> <td>BE, no BOM</td>      </tr>
 * <tr> <td>UTF-16LE</td> <td>LE, no BOM</td>      </tr>
 * <tr> <td>UTF-16</td>   <td>BE, with BE BOM</td> </tr>
 * </table>
 *
 * <p>The next table shows how each variant's decoder behaves when reading a byte sequence.
 *
 * <p>The phrase "includes BOM" means that the output includes the U+FEFF byte order mark character.
 *
 * <p><table width="100%">
 * <tr> <th>Charset</th>  <th>BE BOM</th>           <th>LE BOM</th>           <th>No BOM</th> </tr>
 * <tr> <td>UTF-16BE</td> <td>BE, includes BOM</td> <td>BE, failure</td>      <td>BE</td>     </tr>
 * <tr> <td>UTF-16LE</td> <td>LE, failure</td>      <td>LE, includes BOM</td> <td>LE</td>     </tr>
 * <tr> <td>UTF-16</td>   <td>BE</td>               <td>LE</td>               <td>BE</td>     </tr>
 * </table>
 *
 * <h4>Charset names</h4>
 * <p>A charset has a canonical name, returned by {@link #name}. Most charsets will
 * also have one or more aliases, returned by {@link #aliases}. A charset can be looked up
 * by canonical name or any of its aliases using {@link #forName}.
 *
 * <h4>Guaranteed-available charsets</h4>
 * <p>The following charsets are available on every Java implementation:
 * <ul>
 * <li>ISO-8859-1
 * <li>US-ASCII
 * <li>UTF-16
 * <li>UTF-16BE
 * <li>UTF-16LE
 * <li>UTF-8
 * </ul>
 * <p>All of these charsets support both decoding and encoding. The charsets whose names begin
 * "UTF" can represent all characters, as mentioned above. The "ISO-8859-1" and "US-ASCII" charsets
 * can only represent small subsets of these characters. Except when required to do otherwise for
 * compatibility, new code should use one of the UTF charsets listed above. The platform's default
 * charset is UTF-8. (This is in contrast to some older implementations, where the default charset
 * depended on the user's locale.)
 *
 * <p>Most implementations will support hundreds of charsets. Use {@link #availableCharsets} or
 * {@link #isSupported} to see what's available. If you intend to use the charset if it's
 * available, just call {@link #forName} and catch the exceptions it throws if the charset isn't
 * available.
 *
 * <p>Additional charsets can be made available by configuring one or more charset
 * providers through provider configuration files. Such files are always named
 * as "java.nio.charset.spi.CharsetProvider" and located in the
 * "META-INF/services" directory of one or more classpaths. The files should be
 * encoded in "UTF-8". Each line of their content specifies the class name of a
 * charset provider which extends {@link java.nio.charset.spi.CharsetProvider}.
 * A line should end with '\r', '\n' or '\r\n'. Leading and trailing whitespace
 * is trimmed. Blank lines, and lines (after trimming) starting with "#" which are
 * regarded as comments, are both ignored. Duplicates of names already found are also
 * ignored. Both the configuration files and the provider classes will be loaded
 * using the thread context class loader.
 *
 * <p>Although class is thread-safe, the {@link CharsetDecoder} and {@link CharsetEncoder} instances
 * it returns are inherently stateful.
 */
public abstract class Charset implements Comparable<Charset> {
    private static final HashMap<String, Charset> CACHED_CHARSETS = new HashMap<String, Charset>();

    private final String canonicalName;

    private final HashSet<String> aliasesSet;

    /**
     * Constructs a <code>Charset</code> object. Duplicated aliases are
     * ignored.
     *
     * @param canonicalName
     *            the canonical name of the charset.
     * @param aliases
     *            an array containing all aliases of the charset. May be null.
     * @throws IllegalCharsetNameException
     *             on an illegal value being supplied for either
     *             <code>canonicalName</code> or for any element of
     *             <code>aliases</code>.
     */
    protected Charset(String canonicalName, String[] aliases) {
        // check whether the given canonical name is legal
        checkCharsetName(canonicalName);
        this.canonicalName = canonicalName;
        // check each alias and put into a set
        this.aliasesSet = new HashSet<String>();
        if (aliases != null) {
            for (String alias : aliases) {
                checkCharsetName(alias);
                this.aliasesSet.add(alias);
            }
        }
    }

    private static void checkCharsetName(String name) {
        if (name.isEmpty()) {
            throw new IllegalCharsetNameException(name);
        }
        int length = name.length();
        for (int i = 0; i < length; ++i) {
            if (!isValidCharsetNameCharacter(name.charAt(i))) {
                throw new IllegalCharsetNameException(name);
            }
        }
    }

    private static boolean isValidCharsetNameCharacter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') ||
                c == '-' || c == '.' || c == ':' || c == '_';
    }

    /**
     * Returns an immutable case-insensitive map from canonical names to {@code Charset} instances.
     * If multiple charsets have the same canonical name, it is unspecified which is returned in
     * the map. This method may be slow. If you know which charset you're looking for, use
     * {@link #forName}.
     * @return an immutable case-insensitive map from canonical names to {@code Charset} instances
     */
    public static SortedMap<String, Charset> availableCharsets() {
        // Start with a copy of the built-in charsets...
        @SuppressWarnings("unchecked")
        TreeMap<String, Charset> charsets = new TreeMap<String, Charset>(String.CASE_INSENSITIVE_ORDER);
        for (String charsetName : IOSCharset.getAvailableCharsetNames()) {
            Charset charset = IOSCharset.charsetForName(charsetName);
            charsets.put(charset.name(), charset);
        }
        return Collections.unmodifiableSortedMap(charsets);
    }

    private static Charset cacheCharset(String charsetName, Charset cs) {
        synchronized (CACHED_CHARSETS) {
            // Get the canonical name for this charset, and the canonical instance from the table.
            String canonicalName = cs.name();
            Charset canonicalCharset = CACHED_CHARSETS.get(canonicalName);
            if (canonicalCharset == null) {
                canonicalCharset = cs;
            }

            // Cache the charset by its canonical name...
            CACHED_CHARSETS.put(canonicalName, canonicalCharset);

            // And the name the user used... (Section 1.4 of http://unicode.org/reports/tr22/ means
            // that many non-alias, non-canonical names are valid. For example, "utf8" isn't an
            // alias of the canonical name "UTF-8", but we shouldn't penalize consistent users of
            // such names unduly.)
            CACHED_CHARSETS.put(charsetName, canonicalCharset);

            // And all its aliases...
            for (String alias : cs.aliasesSet) {
                CACHED_CHARSETS.put(alias, canonicalCharset);
            }

            return canonicalCharset;
        }
    }

    /**
     * Returns a {@code Charset} instance for the named charset.
     *
     * @param charsetName a charset name (either canonical or an alias)
     * @throws IllegalCharsetNameException
     *             if the specified charset name is illegal.
     * @throws UnsupportedCharsetException
     *             if the desired charset is not supported by this runtime.
     */
    public static Charset forName(String charsetName) {
        // Is this charset in our cache?
        Charset cs;
        synchronized (CACHED_CHARSETS) {
            cs = CACHED_CHARSETS.get(charsetName);
            if (cs != null) {
                return cs;
            }
        }

        if (charsetName == null) {
            throw new IllegalCharsetNameException(null);
        }

        // Is this a built-in charset supported by iOS?
        checkCharsetName(charsetName);
        cs = IOSCharset.charsetForName(charsetName);
        if (cs != null) {
            return cacheCharset(charsetName, cs);
        }

        throw new UnsupportedCharsetException(charsetName);
    }

    /**
     * Equivalent to {@code forName} but only throws {@code UnsupportedEncodingException},
     * which is all pre-nio code claims to throw.
     *
     * @hide internal use only
     */
    public static Charset forNameUEE(String charsetName) throws UnsupportedEncodingException {
        try {
            return Charset.forName(charsetName);
        } catch (Exception cause) {
            UnsupportedEncodingException ex = new UnsupportedEncodingException(charsetName);
            ex.initCause(cause);
            throw ex;
        }
    }

    /**
     * Determines whether the specified charset is supported by this runtime.
     *
     * @param charsetName
     *            the name of the charset.
     * @return true if the specified charset is supported, otherwise false.
     * @throws IllegalCharsetNameException
     *             if the specified charset name is illegal.
     */
    public static boolean isSupported(String charsetName) {
        try {
            forName(charsetName);
            return true;
        } catch (UnsupportedCharsetException ex) {
            return false;
        }
    }

    /**
     * Determines whether this charset is a superset of the given charset. A charset C1 contains
     * charset C2 if every character representable by C2 is also representable by C1. This means
     * that lossless conversion is possible from C2 to C1 (but not necessarily the other way
     * round). It does <i>not</i> imply that the two charsets use the same byte sequences for the
     * characters they share.
     *
     * <p>Note that this method is allowed to be conservative, and some implementations may return
     * false when this charset does contain the other charset. Android's implementation is precise,
     * and will always return true in such cases.
     *
     * @param charset
     *            a given charset.
     * @return true if this charset is a super set of the given charset,
     *         false if it's unknown or this charset is not a superset of
     *         the given charset.
     */
    public abstract boolean contains(Charset charset);

    /**
     * Gets a new instance of an encoder for this charset.
     *
     * @return a new instance of an encoder for this charset.
     */
    public abstract CharsetEncoder newEncoder();

    /**
     * Gets a new instance of a decoder for this charset.
     *
     * @return a new instance of a decoder for this charset.
     */
    public abstract CharsetDecoder newDecoder();

    /**
     * Gets the canonical name of this charset.
     *
     * @return this charset's name in canonical form.
     */
    public final String name() {
        return this.canonicalName;
    }

    /**
     * Gets the set of this charset's aliases.
     *
     * @return an unmodifiable set of this charset's aliases.
     */
    public final Set<String> aliases() {
        return Collections.unmodifiableSet(this.aliasesSet);
    }

    /**
     * Gets the name of this charset for the default locale.
     *
     * <p>The default implementation returns the canonical name of this charset.
     * Subclasses may return a localized display name.
     *
     * @return the name of this charset for the default locale.
     */
    public String displayName() {
        return this.canonicalName;
    }

    /**
     * Gets the name of this charset for the specified locale.
     *
     * <p>The default implementation returns the canonical name of this charset.
     * Subclasses may return a localized display name.
     *
     * @param l
     *            a certain locale
     * @return the name of this charset for the specified locale
     */
    public String displayName(Locale l) {
        return this.canonicalName;
    }

    /**
     * Indicates whether this charset is known to be registered in the IANA
     * Charset Registry.
     *
     * @return true if the charset is known to be registered, otherwise returns
     *         false.
     */
    public final boolean isRegistered() {
        return !canonicalName.startsWith("x-") && !canonicalName.startsWith("X-");
    }

    /**
     * Returns true if this charset supports encoding, false otherwise.
     *
     * @return true if this charset supports encoding, false otherwise.
     */
    public boolean canEncode() {
        return true;
    }

    /**
     * Returns a new {@code ByteBuffer} containing the bytes encoding the characters from
     * {@code buffer}.
     * This method uses {@code CodingErrorAction.REPLACE}.
     *
     * <p>Applications should generally create a {@link CharsetEncoder} using {@link #newEncoder}
     * for performance.
     *
     * @param buffer
     *            the character buffer containing the content to be encoded.
     * @return the result of the encoding.
     */
    public final ByteBuffer encode(CharBuffer buffer) {
        try {
            return newEncoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE).encode(
                            buffer);
        } catch (CharacterCodingException ex) {
            throw new Error(ex.getMessage(), ex);
        }
    }

    /**
     * Returns a new {@code ByteBuffer} containing the bytes encoding the characters from {@code s}.
     * This method uses {@code CodingErrorAction.REPLACE}.
     *
     * <p>Applications should generally create a {@link CharsetEncoder} using {@link #newEncoder}
     * for performance.
     *
     * @param s the string to be encoded.
     * @return the result of the encoding.
     */
    public final ByteBuffer encode(String s) {
        return encode(CharBuffer.wrap(s));
    }

    /**
     * Returns a new {@code CharBuffer} containing the characters decoded from {@code buffer}.
     * This method uses {@code CodingErrorAction.REPLACE}.
     *
     * <p>Applications should generally create a {@link CharsetDecoder} using {@link #newDecoder}
     * for performance.
     *
     * @param buffer
     *            the byte buffer containing the content to be decoded.
     * @return a character buffer containing the output of the decoding.
     */
    public final CharBuffer decode(ByteBuffer buffer) {
        try {
            return newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE).decode(buffer);
        } catch (CharacterCodingException ex) {
            throw new Error(ex.getMessage(), ex);
        }
    }

    /*
     * -------------------------------------------------------------------
     * Methods implementing parent interface Comparable
     * -------------------------------------------------------------------
     */

    /**
     * Compares this charset with the given charset. This comparison is
     * based on the case insensitive canonical names of the charsets.
     *
     * @param charset
     *            the given object to be compared with.
     * @return a negative integer if less than the given object, a positive
     *         integer if larger than it, or 0 if equal to it.
     */
    public final int compareTo(Charset charset) {
        return this.canonicalName.compareToIgnoreCase(charset.canonicalName);
    }

    /*
     * -------------------------------------------------------------------
     * Methods overriding parent class Object
     * -------------------------------------------------------------------
     */

    /**
     * Determines whether this charset equals to the given object. They are
     * considered to be equal if they have the same canonical name.
     *
     * @param obj
     *            the given object to be compared with.
     * @return true if they have the same canonical name, otherwise false.
     */
    @Override
    public final boolean equals(Object obj) {
        if (obj instanceof Charset) {
            Charset that = (Charset) obj;
            return this.canonicalName.equals(that.canonicalName);
        }
        return false;
    }

    /**
     * Gets the hash code of this charset.
     *
     * @return the hash code of this charset.
     */
    @Override
    public final int hashCode() {
        return this.canonicalName.hashCode();
    }

    /**
     * Gets a string representation of this charset. Usually this contains the
     * canonical name of the charset.
     *
     * @return a string representation of this charset.
     */
    @Override
    public final String toString() {
        return getClass().getName() + "[" + this.canonicalName + "]";
    }

    /**
     * Returns the system's default charset. This is determined during VM startup, and will not
     * change thereafter. On Android, the default charset is UTF-8.
     */
    public static Charset defaultCharset() {
        return IOSCharset.DEFAULT_CHARSET;
    }
}
