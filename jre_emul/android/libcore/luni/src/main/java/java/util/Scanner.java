/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package java.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.io.IoUtils;

/**
 * A parser that parses a text string of primitive types and strings with the
 * help of regular expressions. This class is not as useful as it might seem.
 * It's very inefficient for communicating between machines; you should use JSON,
 * protobufs, or even XML for that. Very simple uses might get away with {@link String#split}.
 * For input from humans, the use of locale-specific regular expressions make it not only
 * expensive but also somewhat unpredictable.
 *
 * <p>This class supports localized numbers and various
 * radixes. The input is broken into tokens by the delimiter pattern, which is
 * {@code \\p{javaWhitespace}} by default.
 *
 * <p>Example:
 * <pre>
 * Scanner s = new Scanner("1A true");
 * assertEquals(26, s.nextInt(16));
 * assertEquals(true, s.nextBoolean());
 * </pre>
 *
 * <p>The {@code Scanner} class is not thread-safe.
 */
public final class Scanner implements Closeable, Iterator<String> {

    private static final String NL = "\n|\r\n|\r|\u0085|\u2028|\u2029";

    // Default delimiting pattern.
    private static final Pattern DEFAULT_DELIMITER = Pattern.compile("\\p{javaWhitespace}+");

    // The boolean's pattern.
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);

    // Pattern used to recognize line terminator.
    private static final Pattern LINE_TERMINATOR = Pattern.compile(NL);

    // Pattern used to recognize multiple line terminators.
    private static final Pattern MULTI_LINE_TERMINATOR = Pattern.compile("(" + NL + ")+");

    // Pattern used to recognize a line with a line terminator.
    private static final Pattern LINE_PATTERN = Pattern.compile(".*(" + NL + ")|.+$");

    // The pattern matches anything.
    private static final Pattern ANY_PATTERN = Pattern.compile("(?s).*");

    private static final int DEFAULT_RADIX = 10;

    // The input source of scanner.
    private Readable input;

    private CharBuffer buffer = CharBuffer.allocate(1024);

    private Pattern delimiter = DEFAULT_DELIMITER;

    private Matcher matcher;

    private int currentRadix = DEFAULT_RADIX;

    private Locale locale = Locale.getDefault();

    // The position where find begins.
    private int findStartIndex = 0;

    // The last find start position.
    private int preStartIndex = findStartIndex;

    // The length of the buffer.
    private int bufferLength = 0;

    // Record the status of this scanner. True if the scanner is closed.
    private boolean closed = false;

    private IOException lastIOException;

    private boolean matchSuccessful = false;

    private DecimalFormat decimalFormat;

    // Records whether the underlying readable has more input.
    private boolean inputExhausted = false;

    private Object cachedNextValue = null;
    private int cachedNextIndex = -1;

    private Pattern cachedFloatPattern = null;

    private int cachedIntegerPatternRadix = -1;
    private Pattern cachedIntegerPattern = null;

    /**
     * Creates a {@code Scanner} with the specified {@code File} as input. The default charset
     * is applied when reading the file.
     *
     * @param src
     *            the file to be scanned.
     * @throws FileNotFoundException
     *             if the specified file does not exist.
     */
    public Scanner(File src) throws FileNotFoundException {
        this(src, Charset.defaultCharset().name());
    }

    /**
     * Creates a {@code Scanner} with the specified {@code File} as input. The specified charset
     * is applied when reading the file.
     *
     * @param src
     *            the file to be scanned.
     * @param charsetName
     *            the name of the encoding type of the file.
     * @throws FileNotFoundException
     *             if the specified file does not exist.
     * @throws IllegalArgumentException
     *             if the specified coding does not exist.
     */
    public Scanner(File src, String charsetName) throws FileNotFoundException {
        if (src == null) {
            throw new NullPointerException("src == null");
        }
        FileInputStream fis = new FileInputStream(src);
        if (charsetName == null) {
            throw new IllegalArgumentException("charsetName == null");
        }

        InputStreamReader streamReader;
        try {
            streamReader = new InputStreamReader(fis, charsetName);
        } catch (UnsupportedEncodingException e) {
            IoUtils.closeQuietly(fis);
            throw new IllegalArgumentException(e.getMessage());
        }
        initialize(streamReader);
    }

    /**
     * Creates a {@code Scanner} on the specified string.
     *
     * @param src
     *            the string to be scanned.
     */
    public Scanner(String src) {
        initialize(new StringReader(src));
    }

    /**
     * Creates a {@code Scanner} on the specified {@code InputStream}. The default charset is
     * applied when decoding the input.
     *
     * @param src
     *            the {@code InputStream} to be scanned.
     */
    public Scanner(InputStream src) {
        this(src, Charset.defaultCharset().name());
    }

    /**
     * Creates a {@code Scanner} on the specified {@code InputStream}. The specified charset is
     * applied when decoding the input.
     *
     * @param src
     *            the {@code InputStream} to be scanned.
     * @param charsetName
     *            the encoding type of the {@code InputStream}.
     * @throws IllegalArgumentException
     *             if the specified character set is not found.
     */
    public Scanner(InputStream src, String charsetName) {
        if (src == null) {
            throw new NullPointerException("src == null");
        }

        InputStreamReader streamReader;
        try {
            streamReader = new InputStreamReader(src, charsetName);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        initialize(streamReader);
    }

    /**
     * Creates a {@code Scanner} with the specified {@code Readable} as input.
     *
     * @param src
     *            the {@code Readable} to be scanned.
     */
    public Scanner(Readable src) {
        if (src == null) {
            throw new NullPointerException("src == null");
        }
        initialize(src);
    }

    /**
     * Creates a {@code Scanner} with the specified {@code ReadableByteChannel} as
     * input. The default charset is applied when decoding the input.
     *
     * @param src
     *            the {@code ReadableByteChannel} to be scanned.
     */
    public Scanner(ReadableByteChannel src) {
        this(src, Charset.defaultCharset().name());
    }

    /**
     * Creates a {@code Scanner} with the specified {@code ReadableByteChannel} as
     * input. The specified charset is applied when decoding the input.
     *
     * @param src
     *            the {@code ReadableByteChannel} to be scanned.
     * @param charsetName
     *            the encoding type of the content.
     * @throws IllegalArgumentException
     *             if the specified character set is not found.
     */
    public Scanner(ReadableByteChannel src, String charsetName) {
        if (src == null) {
            throw new NullPointerException("src == null");
        }
        if (charsetName == null) {
            throw new IllegalArgumentException("charsetName == null");
        }
        initialize(Channels.newReader(src, charsetName));
    }

    private void initialize(Readable input) {
        this.input = input;
        matcher = delimiter.matcher("");
        matcher.useTransparentBounds(true);
        matcher.useAnchoringBounds(false);
    }

    /**
     * Closes this {@code Scanner} and the underlying input if the input implements
     * {@code Closeable}. If the {@code Scanner} has been closed, this method will have
     * no effect. Any scanning operation called after calling this method will throw
     * an {@code IllegalStateException}.
     *
     * @see Closeable
     */
    public void close() {
        if (closed) {
            return;
        }
        if (input instanceof Closeable) {
            try {
                ((Closeable) input).close();
            } catch (IOException e) {
                lastIOException = e;
            }
        }
        closed = true;
    }

    /**
     * Returns the delimiter {@code Pattern} in use by this {@code Scanner}.
     *
     * @return the delimiter {@code Pattern} in use by this {@code Scanner}.
     */
    public Pattern delimiter() {
        return delimiter;
    }

    /**
     * Tries to find the pattern in the input. Delimiters are ignored. If the
     * pattern is found before line terminator, the matched string will be
     * returned, and the {@code Scanner} will advance to the end of the matched string.
     * Otherwise, {@code null} will be returned and the {@code Scanner} will not advance.
     * When waiting for input, the {@code Scanner} may be blocked. All the
     * input may be cached if no line terminator exists in the buffer.
     *
     * @param pattern
     *            the pattern to find in the input.
     * @return the matched string or {@code null} if the pattern is not found
     *         before the next line terminator.
     * @throws IllegalStateException
     *             if the {@code Scanner} is closed.
     */
    public String findInLine(Pattern pattern) {
        checkOpen();
        checkNotNull(pattern);
        int horizonLineSeparator = 0;

        matcher.usePattern(MULTI_LINE_TERMINATOR);
        matcher.region(findStartIndex, bufferLength);

        boolean findComplete = false;
        int terminatorLength = 0;
        while (!findComplete) {
            if (matcher.find()) {
                horizonLineSeparator = matcher.start();
                terminatorLength = matcher.end() - matcher.start();
                findComplete = true;
            } else {
                if (!inputExhausted) {
                    readMore();
                    resetMatcher();
                } else {
                    horizonLineSeparator = bufferLength;
                    findComplete = true;
                }
            }
        }

        matcher.usePattern(pattern);

        /*
         * TODO The following 2 statements are used to deal with regex's bug.
         * java.util.regex.Matcher.region(int start, int end) implementation
         * does not have any effects when called. They will be removed once the
         * bug is fixed.
         */
        int oldLimit = buffer.limit();
        // Considering the look ahead feature, the line terminator should be involved as RI
        buffer.limit(horizonLineSeparator + terminatorLength);
        // ========== To deal with regex bug ====================

        // Considering the look ahead feature, the line terminator should be involved as RI
        matcher.region(findStartIndex, horizonLineSeparator + terminatorLength);
        if (matcher.find()) {
            // The scanner advances past the input that matched
            findStartIndex = matcher.end();
            // If the matched pattern is immediately followed by line
            // terminator.
            if (horizonLineSeparator == matcher.end()) {
                findStartIndex += terminatorLength;
            }
            // the line terminator itself should not be a part of
            // the match result according to the Spec
            if (horizonLineSeparator != bufferLength
                    && (horizonLineSeparator + terminatorLength == matcher
                            .end())) {
                // ========== To deal with regex bug ====================
                buffer.limit(oldLimit);
                // ========== To deal with regex bug ====================

                matchSuccessful = false;
                return null;
            }
            matchSuccessful = true;

            // ========== To deal with regex bug ====================
            buffer.limit(oldLimit);
            // ========== To deal with regex bug ====================

            return matcher.group();
        }

        // ========== To deal with regex bug ====================
        buffer.limit(oldLimit);
        // ========== To deal with regex bug ====================

        matchSuccessful = false;
        return null;
    }

    /**
     * Compiles the pattern string and tries to find a substring matching it in the input data. The
     * delimiter will be ignored. This is the same as invoking
     * {@code findInLine(Pattern.compile(pattern))}.
     *
     * @param pattern
     *            a string used to construct a pattern which is in turn used to
     *            match a substring of the input data.
     * @return the matched string or {@code null} if the pattern is not found
     *         before the next line terminator.
     * @throws IllegalStateException
     *             if the {@code Scanner} is closed.
     * @see #findInLine(Pattern)
     */
    public String findInLine(String pattern) {
        return findInLine(Pattern.compile(pattern));
    }

    /**
     * Tries to find the pattern in the input between the current position and the specified
     * horizon. Delimiters are ignored. If the pattern is found, the matched
     * string will be returned, and the {@code Scanner} will advance to the end of the
     * matched string. Otherwise, null will be returned and {@code Scanner} will not
     * advance. When waiting for input, the {@code Scanner} may be blocked.
     * <p>
     * The {@code Scanner}'s search will never go more than {@code horizon} code points from current
     * position. The position of {@code horizon} does have an effect on the result of the
     * match. For example, when the input is "123" and current position is at zero,
     * <code>findWithinHorizon(Pattern.compile("\\p{Digit}{3}"), 2)</code>
     * will return {@code null}, while
     * <code>findWithinHorizon(Pattern.compile("\\p{Digit}{3}"), 3)</code>
     * will return {@code "123"}. {@code horizon} is treated as a transparent,
     * non-anchoring bound. (refer to
     * {@link Matcher#useTransparentBounds(boolean)} and
     * {@link Matcher#useAnchoringBounds(boolean)})
     * <p>
     * A {@code horizon} whose value is zero will be ignored and the whole input will be
     * used for search. In this situation, all the input may be cached.
     *
     * @param pattern
     *            the pattern used to scan.
     * @param horizon
     *            the search limit.
     * @return the matched string or {@code null} if the pattern is not found
     *         within the specified {@code horizon}.
     * @throws IllegalStateException
     *             if the {@code Scanner} is closed.
     * @throws IllegalArgumentException
     *             if {@code horizon} is less than zero.
     */
    public String findWithinHorizon(Pattern pattern, int horizon) {
        checkOpen();
        checkNotNull(pattern);
        if (horizon < 0) {
            throw new IllegalArgumentException("horizon < 0");
        }
        matcher.usePattern(pattern);

        String result = null;
        int horizonEndIndex = (horizon == 0) ? Integer.MAX_VALUE : findStartIndex + horizon;
        while (true) {
            // If horizon > 0, then search up to
            // min( bufferLength, findStartIndex + horizon).
            // Otherwise search until readable is exhausted.
            int findEndIndex = Math.min(horizonEndIndex, bufferLength);
            // If horizon == 0, consider horizon as always outside buffer.
            boolean isHorizonInBuffer = (horizonEndIndex <= bufferLength);
            // First, try to find pattern within buffer. If pattern can not be
            // found in buffer, then expand the buffer and try again,
            // util horizonEndIndex is exceeded or no more input left.
            matcher.region(findStartIndex, findEndIndex);
            if (matcher.find()) {
                if ((horizon == 0 && !matcher.hitEnd()) || isHorizonInBuffer || inputExhausted) {
                    result = matcher.group();
                    break;
                }
            } else {
                // Pattern is not found in buffer while horizonEndIndex is
                // within buffer, or input is exhausted. Under this situation,
                // it can be judged that find fails.
                if (isHorizonInBuffer || inputExhausted) {
                    break;
                }
            }

            // Expand buffer and reset matcher if needed.
            if (!inputExhausted) {
                readMore();
                resetMatcher();
            }
        }
        if (result != null) {
            findStartIndex = matcher.end();
            matchSuccessful = true;
        } else {
            matchSuccessful = false;
        }
        return result;
    }

    /**
     * Tries to find the pattern in the input between the current position and the specified
     * {@code horizon}. Delimiters are ignored. This call is the same as invoking
     * {@code findWithinHorizon(Pattern.compile(pattern))}.
     *
     * @param pattern
     *            the pattern used to scan.
     * @param horizon
     *            the search limit.
     * @return the matched string, or {@code null} if the pattern is not found
     *         within the specified horizon.
     * @throws IllegalStateException
     *             if the {@code Scanner} is closed.
     * @throws IllegalArgumentException
     *             if {@code horizon} is less than zero.
     * @see #findWithinHorizon(Pattern, int)
     */
    public String findWithinHorizon(String pattern, int horizon) {
        return findWithinHorizon(Pattern.compile(pattern), horizon);
    }

    /**
     * Returns whether this {@code Scanner} has one or more tokens remaining to parse.
     * This method will block if the data is still being read.
     *
     * @return {@code true} if this {@code Scanner} has one or more tokens remaining,
     *         otherwise {@code false}.
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed.
     */
    public boolean hasNext() {
        return hasNext(ANY_PATTERN);
    }

    /**
     * Returns whether this {@code Scanner} has one or more tokens remaining to parse
     * and the next token matches the given pattern. This method will block if the data is
     * still being read.
     *
     * @param pattern
     *            the pattern to check for.
     * @return {@code true} if this {@code Scanner} has more tokens and the next token
     *         matches the pattern, {@code false} otherwise.
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed.
     */
    public boolean hasNext(Pattern pattern) {
        checkOpen();
        checkNotNull(pattern);
        matchSuccessful = false;
        prepareForScan();
        // if the next token exists, set the match region, otherwise return
        // false
        if (!setTokenRegion()) {
            recoverPreviousStatus();
            return false;
        }
        matcher.usePattern(pattern);
        boolean hasNext = false;
        // check whether next token matches the specified pattern
        if (matcher.matches()) {
            cachedNextIndex = findStartIndex;
            matchSuccessful = true;
            hasNext = true;
        }
        recoverPreviousStatus();
        return hasNext;
    }

    /**
     * Returns {@code true} if this {@code Scanner} has one or more tokens remaining to parse
     * and the next token matches a pattern compiled from the given string. This method will
     * block if the data is still being read. This call is equivalent to
     * {@code hasNext(Pattern.compile(pattern))}.
     *
     * @param pattern
     *            the string specifying the pattern to scan for
     * @return {@code true} if the specified pattern matches this {@code Scanner}'s
     *         next token, {@code false} otherwise.
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed.
     */
    public boolean hasNext(String pattern) {
        return hasNext(Pattern.compile(pattern));
    }

    /**
     * Returns whether the next token can be translated into a valid
     * {@code BigDecimal}.
     *
     * @return {@code true} if the next token can be translated into a valid
     *         {@code BigDecimal}, otherwise {@code false.}
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed.
     */
    public boolean hasNextBigDecimal() {
        Pattern floatPattern = getFloatPattern();
        boolean isBigDecimalValue = false;
        if (hasNext(floatPattern)) {
            String floatString = matcher.group();
            floatString = removeLocaleInfoFromFloat(floatString);
            try {
                cachedNextValue = new BigDecimal(floatString);
                isBigDecimalValue = true;
            } catch (NumberFormatException e) {
                matchSuccessful = false;
            }
        }
        return isBigDecimalValue;
    }

    /**
     * Returns whether the next token can be translated into a valid
     * {@code BigInteger} in the default radix.
     *
     * @return {@code true} if the next token can be translated into a valid
     *         {@code BigInteger}, otherwise {@code false}.
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed.
     */
    public boolean hasNextBigInteger() {
        return hasNextBigInteger(currentRadix);
    }

    /**
     * Returns whether the next token can be translated into a valid
     * {@code BigInteger} in the specified radix.
     *
     * @param radix
     *            the radix used to translate the token into a
     *            {@code BigInteger}.
     * @return {@code true} if the next token can be translated into a valid
     *         {@code BigInteger}, otherwise {@code false}.
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed.
     */
    public boolean hasNextBigInteger(int radix) {
        Pattern integerPattern = getIntegerPattern(radix);
        boolean isBigIntegerValue = false;
        if (hasNext(integerPattern)) {
            String intString = matcher.group();
            intString = removeLocaleInfo(intString, int.class);
            try {
                cachedNextValue = new BigInteger(intString, radix);
                isBigIntegerValue = true;
            } catch (NumberFormatException e) {
                matchSuccessful = false;
            }
        }
        return isBigIntegerValue;
    }

    /**
     * Returns whether the next token can be translated into a valid
     * {@code boolean} value.
     *
     * @return {@code true} if the next token can be translated into a valid
     *         {@code boolean} value, otherwise {@code false}.
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed.
     */
    public boolean hasNextBoolean() {
        return hasNext(BOOLEAN_PATTERN);
    }

    /**
     * Returns whether the next token can be translated into a valid
     * {@code byte} value in the default radix.
     *
     * @return {@code true} if the next token can be translated into a valid
     *         {@code byte} value, otherwise {@code false}.
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed.
     */
    public boolean hasNextByte() {
        return hasNextByte(currentRadix);
    }

    /**
     * Returns whether the next token can be translated into a valid
     * {@code byte} value in the specified radix.
     *
     * @param radix
     *            the radix used to translate the token into a {@code byte}
     *            value
     * @return {@code true} if the next token can be translated into a valid
     *         {@code byte} value, otherwise {@code false}.
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed.
     */
    public boolean hasNextByte(int radix) {
        Pattern integerPattern = getIntegerPattern(radix);
        boolean isByteValue = false;
        if (hasNext(integerPattern)) {
            String intString = matcher.group();
            intString = removeLocaleInfo(intString, int.class);
            try {
                cachedNextValue = Byte.valueOf(intString, radix);
                isByteValue = true;
            } catch (NumberFormatException e) {
                matchSuccessful = false;
            }
        }
        return isByteValue;
    }

    /**
     * Returns whether the next token translated into a valid {@code double}
     * value.
     *
     * @return {@code true} if the next token can be translated into a valid
     *         {@code double} value, otherwise {@code false}.
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed.
     */
    public boolean hasNextDouble() {
        Pattern floatPattern = getFloatPattern();
        boolean isDoubleValue = false;
        if (hasNext(floatPattern)) {
            String floatString = matcher.group();
            floatString = removeLocaleInfoFromFloat(floatString);
            try {
                cachedNextValue = Double.valueOf(floatString);
                isDoubleValue = true;
            } catch (NumberFormatException e) {
                matchSuccessful = false;
            }
        }
        return isDoubleValue;
    }

    /**
     * Returns whether the next token can be translated into a valid
     * {@code float} value.
     *
     * @return {@code true} if the next token can be translated into a valid
     *         {@code float} value, otherwise {@code false}.
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed.
     */
    public boolean hasNextFloat() {
        Pattern floatPattern = getFloatPattern();
        boolean isFloatValue = false;
        if (hasNext(floatPattern)) {
            String floatString = matcher.group();
            floatString = removeLocaleInfoFromFloat(floatString);
            try {
                cachedNextValue = Float.valueOf(floatString);
                isFloatValue = true;
            } catch (NumberFormatException e) {
                matchSuccessful = false;
            }
        }
        return isFloatValue;
    }

    /**
     * Returns whether the next token can be translated into a valid {@code int}
     * value in the default radix.
     *
     * @return {@code true} if the next token can be translated into a valid
     *         {@code int} value, otherwise {@code false}.
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed,
     */
    public boolean hasNextInt() {
        return hasNextInt(currentRadix);
    }

    /**
     * Returns whether the next token can be translated into a valid {@code int}
     * value in the specified radix.
     *
     * @param radix
     *            the radix used to translate the token into an {@code int}
     *            value.
     * @return {@code true} if the next token in this {@code Scanner}'s input can be
     *         translated into a valid {@code int} value, otherwise
     *         {@code false}.
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed.
     */
    public boolean hasNextInt(int radix) {
        Pattern integerPattern = getIntegerPattern(radix);
        boolean isIntValue = false;
        if (hasNext(integerPattern)) {
            String intString = matcher.group();
            intString = removeLocaleInfo(intString, int.class);
            try {
                cachedNextValue = Integer.valueOf(intString, radix);
                isIntValue = true;
            } catch (NumberFormatException e) {
                matchSuccessful = false;
            }
        }
        return isIntValue;
    }

    /**
     * Returns true if there is a line terminator in the input.
     * This method may block.
     *
     * @throws IllegalStateException if this {@code Scanner} is closed.
     */
    public boolean hasNextLine() {
        prepareForScan();
        String result = findWithinHorizon(LINE_PATTERN, 0);
        recoverPreviousStatus();
        return result != null;
    }

    /**
     * Returns whether the next token can be translated into a valid
     * {@code long} value in the default radix.
     *
     * @return {@code true} if the next token can be translated into a valid
     *         {@code long} value, otherwise {@code false}.
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed.
     */
    public boolean hasNextLong() {
        return hasNextLong(currentRadix);
    }

    /**
     * Returns whether the next token can be translated into a valid
     * {@code long} value in the specified radix.
     *
     * @param radix
     *            the radix used to translate the token into a {@code long}
     *            value.
     * @return {@code true} if the next token can be translated into a valid
     *         {@code long} value, otherwise {@code false}.
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed.
     */
    public boolean hasNextLong(int radix) {
        Pattern integerPattern = getIntegerPattern(radix);
        boolean isLongValue = false;
        if (hasNext(integerPattern)) {
            String intString = matcher.group();
            intString = removeLocaleInfo(intString, int.class);
            try {
                cachedNextValue = Long.valueOf(intString, radix);
                isLongValue = true;
            } catch (NumberFormatException e) {
                matchSuccessful = false;
            }
        }
        return isLongValue;
    }

    /**
     * Returns whether the next token can be translated into a valid
     * {@code short} value in the default radix.
     *
     * @return {@code true} if the next token can be translated into a valid
     *         {@code short} value, otherwise {@code false}.
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed.
     */
    public boolean hasNextShort() {
        return hasNextShort(currentRadix);
    }

    /**
     * Returns whether the next token can be translated into a valid
     * {@code short} value in the specified radix.
     *
     * @param radix
     *            the radix used to translate the token into a {@code short}
     *            value.
     * @return {@code true} if the next token can be translated into a valid
     *         {@code short} value, otherwise {@code false}.
     * @throws IllegalStateException
     *             if the {@code Scanner} has been closed.
     */
    public boolean hasNextShort(int radix) {
        Pattern integerPattern = getIntegerPattern(radix);
        boolean isShortValue = false;
        if (hasNext(integerPattern)) {
            String intString = matcher.group();
            intString = removeLocaleInfo(intString, int.class);
            try {
                cachedNextValue = Short.valueOf(intString, radix);
                isShortValue = true;
            } catch (NumberFormatException e) {
                matchSuccessful = false;
            }
        }
        return isShortValue;
    }

    /**
     * Returns the last {@code IOException} that was raised while reading from the underlying
     * input, or {@code null} if none was thrown.
     */
    public IOException ioException() {
        return lastIOException;
    }

    /**
     * Returns the {@code Locale} of this {@code Scanner}.
     */
    public Locale locale() {
        return locale;
    }

    private void setLocale(Locale locale) {
        this.locale = locale;
        this.decimalFormat = null;
        this.cachedFloatPattern = null;
        this.cachedIntegerPatternRadix = -1;
        this.cachedIntegerPattern = null;
    }

    /**
     * Returns the result of the last matching operation.
     * <p>
     * The next* and find* methods return the match result in the case of a
     * successful match.
     *
     * @return the match result of the last successful match operation
     * @throws IllegalStateException
     *             if the match result is not available, of if the last match
     *             was not successful.
     */
    public MatchResult match() {
        if (!matchSuccessful) {
            throw new IllegalStateException();
        }
        return matcher.toMatchResult();
    }

    /**
     * Returns the next token. The token will be both prefixed and suffixed by
     * the delimiter that is currently being used (or a string that matches the
     * delimiter pattern). This method will block if input is being read.
     *
     * @return the next complete token.
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     */
    public String next() {
        return next(ANY_PATTERN);
    }

    /**
     * Returns the next token if it matches the specified pattern. The token
     * will be both prefixed and suffixed by the delimiter that is currently
     * being used (or a string that matches the delimiter pattern). This method will block
     * if input is being read.
     *
     * @param pattern
     *            the specified pattern to scan.
     * @return the next token.
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     * @throws InputMismatchException
     *             if the next token does not match the pattern given.
     */
    public String next(Pattern pattern) {
        checkOpen();
        checkNotNull(pattern);
        matchSuccessful = false;
        prepareForScan();
        if (!setTokenRegion()) {
            recoverPreviousStatus();
            // if setting match region fails
            throw new NoSuchElementException();
        }
        matcher.usePattern(pattern);
        if (!matcher.matches()) {
            recoverPreviousStatus();
            throw new InputMismatchException();

        }
        matchSuccessful = true;
        return matcher.group();
    }

    /**
     * Returns the next token if it matches the specified pattern. The token
     * will be both prefixed and suffixed by the delimiter that is currently
     * being used (or a string that matches the delimiter pattern). This method will block
     * if input is being read. Calling this method is equivalent to
     * {@code next(Pattern.compile(pattern))}.
     *
     * @param pattern
     *            the string specifying the pattern to scan for.
     * @return the next token.
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     * @throws InputMismatchException
     *             if the next token does not match the pattern given.
     */
    public String next(String pattern) {
        return next(Pattern.compile(pattern));
    }

    /**
     * Returns the next token as a {@code BigDecimal}. This method will block if input is
     * being read. If the next token can be translated into a {@code BigDecimal}
     * the following is done: All {@code Locale}-specific prefixes, group separators,
     * and {@code Locale}-specific suffixes are removed. Then non-ASCII digits are
     * mapped into ASCII digits via {@link Character#digit(char, int)}, and a
     * negative sign (-) is added if the {@code Locale}-specific negative prefix or
     * suffix was present. Finally the resulting string is passed to
     * {@code BigDecimal(String) }.
     *
     * @return the next token as a {@code BigDecimal}.
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             {@code BigDecimal}.
     */
    public BigDecimal nextBigDecimal() {
        checkOpen();
        Object obj = cachedNextValue;
        cachedNextValue = null;
        if (obj instanceof BigDecimal) {
            findStartIndex = cachedNextIndex;
            return (BigDecimal) obj;
        }
        Pattern floatPattern = getFloatPattern();
        String floatString = next(floatPattern);
        floatString = removeLocaleInfoFromFloat(floatString);
        BigDecimal bigDecimalValue;
        try {
            bigDecimalValue = new BigDecimal(floatString);
        } catch (NumberFormatException e) {
            matchSuccessful = false;
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
        return bigDecimalValue;
    }

    /**
     * Returns the next token as a {@code BigInteger} in the current radix.
     * This method may block for more input.
     *
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             {@code BigInteger}.
     */
    public BigInteger nextBigInteger() {
        return nextBigInteger(currentRadix);
    }

    /**
     * Returns the next token as a {@code BigInteger} with the specified radix.
     * This method will block if input is being read. If the next token can be translated
     * into a {@code BigInteger} the following is done: All {@code Locale}-specific
     * prefixes, group separators, and {@code Locale}-specific suffixes are removed.
     * Then non-ASCII digits are mapped into ASCII digits via
     * {@link Character#digit(char, int)}, and a negative sign (-) is added if the
     * {@code Locale}-specific negative prefix or suffix was present. Finally the
     * resulting String is passed to {@link BigInteger#BigInteger(String, int)}}
     * with the specified radix.
     *
     * @param radix
     *            the radix used to translate the token into a
     *            {@code BigInteger}.
     * @return the next token as a {@code BigInteger}
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             {@code BigInteger}.
     */
    public BigInteger nextBigInteger(int radix) {
        checkOpen();
        Object obj = cachedNextValue;
        cachedNextValue = null;
        if (obj instanceof BigInteger) {
            findStartIndex = cachedNextIndex;
            return (BigInteger) obj;
        }
        Pattern integerPattern = getIntegerPattern(radix);
        String intString = next(integerPattern);
        intString = removeLocaleInfo(intString, int.class);
        BigInteger bigIntegerValue;
        try {
            bigIntegerValue = new BigInteger(intString, radix);
        } catch (NumberFormatException e) {
            matchSuccessful = false;
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
        return bigIntegerValue;
    }

    /**
     * Returns the next token as a {@code boolean}. This method will block if input is
     * being read.
     *
     * @return the next token as a {@code boolean}.
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             {@code boolean} value.
     */
    public boolean nextBoolean() {
        return Boolean.parseBoolean(next(BOOLEAN_PATTERN));
    }

    /**
     * Returns the next token as a {@code byte} in the current radix.
     * This method may block for more input.
     *
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             {@code byte} value.
     */
    public byte nextByte() {
        return nextByte(currentRadix);
    }

    /**
     * Returns the next token as a {@code byte} with the specified radix. Will
     * block if input is being read. If the next token can be translated into a
     * {@code byte} the following is done: All {@code Locale}-specific prefixes, group
     * separators, and {@code Locale}-specific suffixes are removed. Then non-ASCII
     * digits are mapped into ASCII digits via
     * {@link Character#digit(char, int)}, and a negative sign (-) is added if the
     * {@code Locale}-specific negative prefix or suffix was present. Finally the
     * resulting String is passed to {@link Byte#parseByte(String, int)}} with
     * the specified radix.
     *
     * @param radix
     *            the radix used to translate the token into {@code byte} value.
     * @return the next token as a {@code byte}.
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             {@code byte} value.
     */
    @SuppressWarnings("boxing")
    public byte nextByte(int radix) {
        checkOpen();
        Object obj = cachedNextValue;
        cachedNextValue = null;
        if (obj instanceof Byte) {
            findStartIndex = cachedNextIndex;
            return (Byte) obj;
        }
        Pattern integerPattern = getIntegerPattern(radix);
        String intString = next(integerPattern);
        intString = removeLocaleInfo(intString, int.class);
        byte byteValue = 0;
        try {
            byteValue = Byte.parseByte(intString, radix);
        } catch (NumberFormatException e) {
            matchSuccessful = false;
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
        return byteValue;
    }

    /**
     * Returns the next token as a {@code double}. This method will block if input is being
     * read. If the next token can be translated into a {@code double} the
     * following is done: All {@code Locale}-specific prefixes, group separators, and
     * {@code Locale}-specific suffixes are removed. Then non-ASCII digits are mapped
     * into ASCII digits via {@link Character#digit(char, int)}, and a negative
     * sign (-) is added if the {@code Locale}-specific negative prefix or suffix was
     * present. Finally the resulting String is passed to
     * {@link Double#parseDouble(String)}}. If the token matches the localized
     * NaN or infinity strings, it is also passed to
     * {@link Double#parseDouble(String)}}.
     *
     * @return the next token as a {@code double}.
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             {@code double} value.
     */
    @SuppressWarnings("boxing")
    public double nextDouble() {
        checkOpen();
        Object obj = cachedNextValue;
        cachedNextValue = null;
        if (obj instanceof Double) {
            findStartIndex = cachedNextIndex;
            return (Double) obj;
        }
        Pattern floatPattern = getFloatPattern();
        String floatString = next(floatPattern);
        floatString = removeLocaleInfoFromFloat(floatString);
        double doubleValue;
        try {
            doubleValue = Double.parseDouble(floatString);
        } catch (NumberFormatException e) {
            matchSuccessful = false;
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
        return doubleValue;
    }

    /**
     * Returns the next token as a {@code float}. This method will block if input is being
     * read. If the next token can be translated into a {@code float} the
     * following is done: All {@code Locale}-specific prefixes, group separators, and
     * {@code Locale}-specific suffixes are removed. Then non-ASCII digits are mapped
     * into ASCII digits via {@link Character#digit(char, int)}, and a negative
     * sign (-) is added if the {@code Locale}-specific negative prefix or suffix was
     * present. Finally the resulting String is passed to
     * {@link Float#parseFloat(String)}}.If the token matches the localized NaN
     * or infinity strings, it is also passed to
     * {@link Float#parseFloat(String)}}.
     *
     * @return the next token as a {@code float}.
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             {@code float} value.
     */
    @SuppressWarnings("boxing")
    public float nextFloat() {
        checkOpen();
        Object obj = cachedNextValue;
        cachedNextValue = null;
        if (obj instanceof Float) {
            findStartIndex = cachedNextIndex;
            return (Float) obj;
        }
        Pattern floatPattern = getFloatPattern();
        String floatString = next(floatPattern);
        floatString = removeLocaleInfoFromFloat(floatString);
        float floatValue;
        try {
            floatValue = Float.parseFloat(floatString);
        } catch (NumberFormatException e) {
            matchSuccessful = false;
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
        return floatValue;
    }

    /**
     * Returns the next token as an {@code int} in the current radix.
     * This method may block for more input.
     *
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             {@code int} value.
     */
    public int nextInt() {
        return nextInt(currentRadix);
    }

    /**
     * Returns the next token as an {@code int} with the specified radix. This method will
     * block if input is being read. If the next token can be translated into an
     * {@code int} the following is done: All {@code Locale}-specific prefixes, group
     * separators, and {@code Locale}-specific suffixes are removed. Then non-ASCII
     * digits are mapped into ASCII digits via
     * {@link Character#digit(char, int)}, and a negative sign (-) is added if the
     * {@code Locale}-specific negative prefix or suffix was present. Finally the
     * resulting String is passed to {@link Integer#parseInt(String, int)} with
     * the specified radix.
     *
     * @param radix
     *            the radix used to translate the token into an {@code int}
     *            value.
     * @return the next token as an {@code int}.
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             {@code int} value.
     */
    @SuppressWarnings("boxing")
    public int nextInt(int radix) {
        checkOpen();
        Object obj = cachedNextValue;
        cachedNextValue = null;
        if (obj instanceof Integer) {
            findStartIndex = cachedNextIndex;
            return (Integer) obj;
        }
        Pattern integerPattern = getIntegerPattern(radix);
        String intString = next(integerPattern);
        intString = removeLocaleInfo(intString, int.class);
        int intValue;
        try {
            intValue = Integer.parseInt(intString, radix);
        } catch (NumberFormatException e) {
            matchSuccessful = false;
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
        return intValue;
    }

    /**
     * Returns the skipped input and advances the {@code Scanner} to the beginning of
     * the next line. The returned result will exclude any line terminator. When
     * searching, if no line terminator is found, then a large amount of input
     * will be cached. If no line at all can be found, a {@code NoSuchElementException}
     * will be thrown.
     *
     * @return the skipped line.
     * @throws IllegalStateException
     *             if the {@code Scanner} is closed.
     * @throws NoSuchElementException
     *             if no line can be found, e.g. when input is an empty string.
     */
    public String nextLine() {
        checkOpen();

        matcher.usePattern(LINE_PATTERN);
        matcher.region(findStartIndex, bufferLength);

        String result;
        while (true) {
            if (matcher.find()) {
                if (inputExhausted || matcher.end() != bufferLength
                        || bufferLength < buffer.capacity()) {
                    matchSuccessful = true;
                    findStartIndex = matcher.end();
                    result = matcher.group();
                    break;
                }
            } else {
                if (inputExhausted) {
                    matchSuccessful = false;
                    throw new NoSuchElementException();
                }
            }
            if (!inputExhausted) {
                readMore();
                resetMatcher();
            }
        }
        // Find text without line terminator here.
        if (result != null) {
            Matcher terminatorMatcher = LINE_TERMINATOR.matcher(result);
            if (terminatorMatcher.find()) {
                result = result.substring(0, terminatorMatcher.start());
            }
        }
        return result;
    }

    /**
     * Returns the next token as a {@code long} in the current radix.
     * This method may block for more input.
     *
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             {@code long} value.
     */
    public long nextLong() {
        return nextLong(currentRadix);
    }

    /**
     * Returns the next token as a {@code long} with the specified radix. This method will
     * block if input is being read. If the next token can be translated into a
     * {@code long} the following is done: All {@code Locale}-specific prefixes, group
     * separators, and {@code Locale}-specific suffixes are removed. Then non-ASCII
     * digits are mapped into ASCII digits via
     * {@link Character#digit(char, int)}, and a negative sign (-) is added if the
     * {@code Locale}-specific negative prefix or suffix was present. Finally the
     * resulting String is passed to {@link Long#parseLong(String, int)}} with
     * the specified radix.
     *
     * @param radix
     *            the radix used to translate the token into a {@code long}
     *            value.
     * @return the next token as a {@code long}.
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             {@code long} value.
     */
    @SuppressWarnings("boxing")
    public long nextLong(int radix) {
        checkOpen();
        Object obj = cachedNextValue;
        cachedNextValue = null;
        if (obj instanceof Long) {
            findStartIndex = cachedNextIndex;
            return (Long) obj;
        }
        Pattern integerPattern = getIntegerPattern(radix);
        String intString = next(integerPattern);
        intString = removeLocaleInfo(intString, int.class);
        long longValue;
        try {
            longValue = Long.parseLong(intString, radix);
        } catch (NumberFormatException e) {
            matchSuccessful = false;
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
        return longValue;
    }

    /**
     * Returns the next token as a {@code short} in the current radix.
     * This method may block for more input.
     *
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             {@code short} value.
     */
    public short nextShort() {
        return nextShort(currentRadix);
    }

    /**
     * Returns the next token as a {@code short} with the specified radix. This method will
     * block if input is being read. If the next token can be translated into a
     * {@code short} the following is done: All {@code Locale}-specific prefixes, group
     * separators, and {@code Locale}-specific suffixes are removed. Then non-ASCII
     * digits are mapped into ASCII digits via
     * {@link Character#digit(char, int)}, and a negative sign (-) is added if the
     * {@code Locale}-specific negative prefix or suffix was present. Finally the
     * resulting String is passed to {@link Short#parseShort(String, int)}}
     * with the specified radix.
     *
     * @param radix
     *            the radix used to translate the token into {@code short}
     *            value.
     * @return the next token as a {@code short}.
     * @throws IllegalStateException
     *             if this {@code Scanner} has been closed.
     * @throws NoSuchElementException
     *             if input has been exhausted.
     * @throws InputMismatchException
     *             if the next token can not be translated into a valid
     *             {@code short} value.
     */
    @SuppressWarnings("boxing")
    public short nextShort(int radix) {
        checkOpen();
        Object obj = cachedNextValue;
        cachedNextValue = null;
        if (obj instanceof Short) {
            findStartIndex = cachedNextIndex;
            return (Short) obj;
        }
        Pattern integerPattern = getIntegerPattern(radix);
        String intString = next(integerPattern);
        intString = removeLocaleInfo(intString, int.class);
        short shortValue;
        try {
            shortValue = Short.parseShort(intString, radix);
        } catch (NumberFormatException e) {
            matchSuccessful = false;
            recoverPreviousStatus();
            throw new InputMismatchException();
        }
        return shortValue;
    }

    /**
     * Return the radix of this {@code Scanner}.
     *
     * @return the radix of this {@code Scanner}
     */
    public int radix() {
        return currentRadix;
    }

    /**
     * Tries to use specified pattern to match input starting from the current position.
     * The delimiter will be ignored. If a match is found, the matched input will be
     * skipped. If an anchored match of the specified pattern succeeds, the corresponding input
     * will also be skipped. Otherwise, a {@code NoSuchElementException} will be thrown.
     * Patterns that can match a lot of input may cause the {@code Scanner} to read
     * in a large amount of input.
     *
     * @param pattern
     *            used to skip over input.
     * @return the {@code Scanner} itself.
     * @throws IllegalStateException
     *             if the {@code Scanner} is closed.
     * @throws NoSuchElementException
     *             if the specified pattern match fails.
     */
    public Scanner skip(Pattern pattern) {
        checkOpen();
        checkNotNull(pattern);
        matcher.usePattern(pattern);
        matcher.region(findStartIndex, bufferLength);
        while (true) {
            if (matcher.lookingAt()) {
                boolean matchInBuffer = matcher.end() < bufferLength
                        || (matcher.end() == bufferLength && inputExhausted);
                if (matchInBuffer) {
                    matchSuccessful = true;
                    findStartIndex = matcher.end();
                    break;
                }
            } else {
                if (inputExhausted) {
                    matchSuccessful = false;
                    throw new NoSuchElementException();
                }
            }
            if (!inputExhausted) {
                readMore();
                resetMatcher();
            }
        }
        return this;
    }

    /**
     * Tries to use the specified string to construct a pattern and then uses
     * the constructed pattern to match input starting from the current position. The
     * delimiter will be ignored. This call is the same as invoke
     * {@code skip(Pattern.compile(pattern))}.
     *
     * @param pattern
     *            the string used to construct a pattern which in turn is used to
     *            match input.
     * @return the {@code Scanner} itself.
     * @throws IllegalStateException
     *             if the {@code Scanner} is closed.
     */
    public Scanner skip(String pattern) {
        return skip(Pattern.compile(pattern));
    }

    /**
     * Returns a string representation of this {@code Scanner}. The information
     * returned may be helpful for debugging. The format of the string is unspecified.
     *
     * @return a string representation of this {@code Scanner}.
     */
    @Override
    public String toString() {
        return getClass().getName() +
                "[delimiter=" + delimiter +
                ",findStartIndex=" + findStartIndex +
                ",matchSuccessful=" + matchSuccessful +
                ",closed=" + closed +
                "]";
    }

    /**
     * Sets the delimiting pattern of this {@code Scanner}.
     *
     * @param pattern
     *            the delimiting pattern to use.
     * @return this {@code Scanner}.
     */
    public Scanner useDelimiter(Pattern pattern) {
        delimiter = pattern;
        return this;
    }

    /**
     * Sets the delimiting pattern of this {@code Scanner} with a pattern compiled from
     * the supplied string value.
     *
     * @param pattern
     *            a string from which a {@code Pattern} can be compiled.
     * @return this {@code Scanner}.
     */
    public Scanner useDelimiter(String pattern) {
        return useDelimiter(Pattern.compile(pattern));
    }

    /**
     * Sets the {@code Locale} of this {@code Scanner} to a specified {@code Locale}.
     *
     * @param l
     *            the specified {@code Locale} to use.
     * @return this {@code Scanner}.
     */
    public Scanner useLocale(Locale l) {
        if (l == null) {
            throw new NullPointerException("l == null");
        }
        setLocale(l);
        return this;
    }

    /**
     * Sets the radix of this {@code Scanner} to the specified radix.
     *
     * @param radix
     *            the specified radix to use.
     * @return this {@code Scanner}.
     */
    public Scanner useRadix(int radix) {
        checkRadix(radix);
        this.currentRadix = radix;
        return this;
    }

    private void checkRadix(int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            throw new IllegalArgumentException("Invalid radix: " + radix);
        }
    }

    /**
     * Remove is not a supported operation on {@code Scanner}.
     *
     * @throws UnsupportedOperationException
     *             if this method is invoked.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void checkOpen() {
        if (closed) {
            throw new IllegalStateException();
        }
    }

    private void checkNotNull(Pattern pattern) {
        if (pattern == null) {
            throw new NullPointerException("pattern == null");
        }
    }

    /*
     * Change the matcher's input after modifying the contents of the buffer.
     * The current implementation of Matcher causes a copy of the buffer to be taken.
     */
    private void resetMatcher() {
        matcher.reset(buffer);
        matcher.region(findStartIndex, bufferLength);
    }

    /*
     * Recover buffer space for characters that are already processed and save the matcher's state
     * in case parsing fails. See recoverPrevousState. This method must be called before
     * any buffer offsets are calculated.
     */
    private void prepareForScan() {
        // Compacting the buffer recovers space taken by already processed characters. This does not
        // prevent the buffer growing in all situations but keeps the buffer small when delimiters
        // exist regularly.
        if (findStartIndex >= buffer.capacity() / 2) {
            // When over half the buffer is filled with characters no longer being considered by the
            // scanner we take the cost of compacting the buffer.

            // Move all characters from [findStartIndex, findStartIndex + remaining()) to
            // [0, remaining()).
            int oldPosition = buffer.position();
            buffer.position(findStartIndex);
            buffer.compact();
            buffer.position(oldPosition);

            // Update Scanner state to reflect the new buffer state.
            bufferLength -= findStartIndex;
            findStartIndex = 0;
            preStartIndex = -1;

            // The matcher must also be informed that the buffer has changed because it operates on
            // a String copy.
            resetMatcher();
        }

        // Save the matcher's last find position so it can be returned to if the next token cannot
        // be parsed.
        preStartIndex = findStartIndex;
    }

    /*
     * Change the matcher's status to last find position
     */
    private void recoverPreviousStatus() {
        findStartIndex = preStartIndex;
    }

    private Pattern getIntegerPattern(int radix) {
        checkRadix(radix);

        if (decimalFormat == null) {
            decimalFormat = (DecimalFormat) NumberFormat.getInstance(locale);
        }

        if (cachedIntegerPatternRadix == radix) {
            return cachedIntegerPattern;
        }

        String digits = "0123456789abcdefghijklmnopqrstuvwxyz";
        String ASCIIDigit = digits.substring(0, radix);
        String nonZeroASCIIDigit = digits.substring(1, radix);

        String digit = "((?i)[" + ASCIIDigit + "]|\\p{javaDigit})";
        String nonZeroDigit = "((?i)[" + nonZeroASCIIDigit + "]|([\\p{javaDigit}&&[^0]]))";
        String numeral = getNumeral(digit, nonZeroDigit);

        String regex = "(([-+]?(" + numeral + ")))|" +
            "(" + addPositiveSign(numeral) + ")|" +
            "(" + addNegativeSign(numeral) + ")";

        cachedIntegerPatternRadix = radix;
        cachedIntegerPattern = Pattern.compile(regex);
        return cachedIntegerPattern;
    }

    private Pattern getFloatPattern() {
        if (decimalFormat == null) {
            decimalFormat = (DecimalFormat) NumberFormat.getInstance(locale);
        }

        if (cachedFloatPattern != null) {
            return cachedFloatPattern;
        }

        DecimalFormatSymbols dfs = decimalFormat.getDecimalFormatSymbols();

        String digit = "([0-9]|(\\p{javaDigit}))";
        String nonZeroDigit = "[\\p{javaDigit}&&[^0]]";
        String numeral = getNumeral(digit, nonZeroDigit);

        String decimalSeparator = "\\" + dfs.getDecimalSeparator();
        String decimalNumeral = "(" + numeral + "|" +
            numeral + decimalSeparator + digit + "*+|" +
            decimalSeparator + digit + "++)";
        String exponent = "([eE][+-]?" + digit + "+)?";

        String decimal = "(([-+]?" + decimalNumeral + "(" + exponent + "?)" + ")|" +
            "(" + addPositiveSign(decimalNumeral) + "(" + exponent + "?)" + ")|" +
            "(" + addNegativeSign(decimalNumeral) + "(" + exponent + "?)" + "))";

        String hexFloat = "([-+]?0[xX][0-9a-fA-F]*\\.[0-9a-fA-F]+([pP][-+]?[0-9]+)?)";
        String localNaN = dfs.getNaN();
        String localeInfinity = dfs.getInfinity();
        String nonNumber = "(NaN|\\Q" + localNaN + "\\E|Infinity|\\Q" + localeInfinity + "\\E)";
        String signedNonNumber = "((([-+]?(" + nonNumber + ")))|" +
            "(" + addPositiveSign(nonNumber) + ")|" +
            "(" + addNegativeSign(nonNumber) + "))";

        cachedFloatPattern = Pattern.compile(decimal + "|" + hexFloat + "|" + signedNonNumber);
        return cachedFloatPattern;
    }

    private String getNumeral(String digit, String nonZeroDigit) {
        String groupSeparator = "\\" + decimalFormat.getDecimalFormatSymbols().getGroupingSeparator();
        String groupedNumeral = "(" + nonZeroDigit + digit + "?" + digit + "?" +
            "(" + groupSeparator + digit + digit + digit + ")+)";
        return "((" + digit + "++)|" + groupedNumeral + ")";
    }

    /*
     * Add the locale specific positive prefixes and suffixes to the pattern
     */
    private String addPositiveSign(String unsignedNumeral) {
        String positivePrefix = "";
        String positiveSuffix = "";
        if (!decimalFormat.getPositivePrefix().isEmpty()) {
            positivePrefix = "\\Q" + decimalFormat.getPositivePrefix() + "\\E";
        }
        if (!decimalFormat.getPositiveSuffix().isEmpty()) {
            positiveSuffix = "\\Q" + decimalFormat.getPositiveSuffix() + "\\E";
        }
        return positivePrefix + unsignedNumeral + positiveSuffix;
    }

    /*
     * Add the locale specific negative prefixes and suffixes to the pattern
     */
    private String addNegativeSign(String unsignedNumeral) {
        String negativePrefix = "";
        String negativeSuffix = "";
        if (!decimalFormat.getNegativePrefix().isEmpty()) {
            negativePrefix = "\\Q" + decimalFormat.getNegativePrefix() + "\\E";
        }
        if (!decimalFormat.getNegativeSuffix().isEmpty()) {
            negativeSuffix = "\\Q" + decimalFormat.getNegativeSuffix() + "\\E";
        }
        return negativePrefix + unsignedNumeral + negativeSuffix;
    }

    /*
     * Remove locale related information from float String
     */
    private String removeLocaleInfoFromFloat(String floatString) {
        // If the token is HexFloat
        if (floatString.indexOf('x') != -1 || floatString.indexOf('X') != -1) {
            return floatString;
        }

        // If the token is scientific notation
        int exponentIndex;
        if ((exponentIndex = floatString.indexOf('e')) != -1 || (exponentIndex = floatString.indexOf('E')) != -1) {
            String decimalNumeralString = floatString.substring(0, exponentIndex);
            String exponentString = floatString.substring(exponentIndex + 1, floatString.length());
            decimalNumeralString = removeLocaleInfo(decimalNumeralString, float.class);
            return decimalNumeralString + "e" + exponentString;
        }
        return removeLocaleInfo(floatString, float.class);
    }

    /*
     * Remove the locale specific prefixes, group separators, and locale
     * specific suffixes from input string
     */
    private String removeLocaleInfo(String token, Class<?> type) {
        DecimalFormatSymbols dfs = decimalFormat.getDecimalFormatSymbols();

        StringBuilder tokenBuilder = new StringBuilder(token);
        boolean negative = removeLocaleSign(tokenBuilder);
        // Remove group separator
        String groupSeparator = String.valueOf(dfs.getGroupingSeparator());
        int separatorIndex;
        while ((separatorIndex = tokenBuilder.indexOf(groupSeparator)) != -1) {
            tokenBuilder.delete(separatorIndex, separatorIndex + 1);
        }
        // Remove decimal separator
        String decimalSeparator = String.valueOf(dfs.getDecimalSeparator());
        separatorIndex = tokenBuilder.indexOf(decimalSeparator);
        StringBuilder result = new StringBuilder("");
        if (type == int.class) {
            for (int i = 0; i < tokenBuilder.length(); i++) {
                if (Character.digit(tokenBuilder.charAt(i), Character.MAX_RADIX) != -1) {
                    result.append(tokenBuilder.charAt(i));
                }
            }
        } else if (type == float.class) {
            if (tokenBuilder.toString().equals(dfs.getNaN())) {
                result.append("NaN");
            } else if (tokenBuilder.toString().equals(dfs.getInfinity())) {
                result.append("Infinity");
            } else {
                for (int i = 0; i < tokenBuilder.length(); i++) {
                    if (Character.digit(tokenBuilder.charAt(i), 10) != -1) {
                        result.append(Character.digit(tokenBuilder.charAt(i), 10));
                    }
                }
            }
        } else {
            throw new AssertionError("Unsupported type: " + type);
        }
        // Token is NaN or Infinity
        if (result.length() == 0) {
            result = tokenBuilder;
        }
        if (separatorIndex != -1) {
            result.insert(separatorIndex, ".");
        }
        // If input is negative
        if (negative) {
            result.insert(0, '-');
        }
        return result.toString();
    }

    /*
     * Remove positive and negative sign from the parameter stringBuilder, and
     * return whether the input string is negative
     */
    private boolean removeLocaleSign(StringBuilder tokenBuilder) {
        String positivePrefix = decimalFormat.getPositivePrefix();
        String positiveSuffix = decimalFormat.getPositiveSuffix();
        String negativePrefix = decimalFormat.getNegativePrefix();
        String negativeSuffix = decimalFormat.getNegativeSuffix();

        if (tokenBuilder.indexOf("+") == 0) {
            tokenBuilder.delete(0, 1);
        }
        if (!positivePrefix.isEmpty() && tokenBuilder.indexOf(positivePrefix) == 0) {
            tokenBuilder.delete(0, positivePrefix.length());
        }
        if (!positiveSuffix.isEmpty() && tokenBuilder.indexOf(positiveSuffix) != -1) {
            tokenBuilder.delete(tokenBuilder.length() - positiveSuffix.length(),
                                tokenBuilder.length());
        }
        boolean negative = false;
        if (tokenBuilder.indexOf("-") == 0) {
            tokenBuilder.delete(0, 1);
            negative = true;
        }
        if (!negativePrefix.isEmpty() && tokenBuilder.indexOf(negativePrefix) == 0) {
            tokenBuilder.delete(0, negativePrefix.length());
            negative = true;
        }
        if (!negativeSuffix.isEmpty() && tokenBuilder.indexOf(negativeSuffix) != -1) {
            tokenBuilder.delete(tokenBuilder.length() - negativeSuffix.length(),
                                tokenBuilder.length());
            negative = true;
        }
        return negative;
    }

    /*
     * Find the prefixed delimiter and suffixed delimiter in the input resource
     * and set the start index and end index of Matcher region. If the suffixed
     * delimiter does not exist, the end index is set to be end of input.
     */
    private boolean setTokenRegion() {
        // The position where token begins
        int tokenStartIndex;
        // The position where token ends
        int tokenEndIndex;
        // Use delimiter pattern
        matcher.usePattern(delimiter);
        matcher.region(findStartIndex, bufferLength);

        tokenStartIndex = findPreDelimiter();
        if (setHeadTokenRegion(tokenStartIndex)) {
            return true;
        }
        tokenEndIndex = findDelimiterAfter();
        // If the second delimiter is not found
        if (tokenEndIndex == -1) {
            // Just first Delimiter Exists
            if (findStartIndex == bufferLength) {
                return false;
            }
            tokenEndIndex = bufferLength;
            findStartIndex = bufferLength;
        }

        matcher.region(tokenStartIndex, tokenEndIndex);
        return true;
    }

    /*
     * Find prefix delimiter
     */
    private int findPreDelimiter() {
        int tokenStartIndex;
        boolean findComplete = false;
        while (!findComplete) {
            if (matcher.find()) {
                findComplete = true;
                // If just delimiter remains
                if (matcher.start() == findStartIndex && matcher.end() == bufferLength) {
                    // If more input resource exists
                    if (!inputExhausted) {
                        readMore();
                        resetMatcher();
                        findComplete = false;
                    }
                }
            } else {
                if (!inputExhausted) {
                    readMore();
                    resetMatcher();
                } else {
                    return -1;
                }
            }
        }
        tokenStartIndex = matcher.end();
        findStartIndex = tokenStartIndex;
        return tokenStartIndex;
    }

    /*
     * Handle some special cases
     */
    private boolean setHeadTokenRegion(int findIndex) {
        int tokenStartIndex;
        int tokenEndIndex;
        boolean setSuccess = false;
        // If no delimiter exists, but something exists in this scanner
        if (findIndex == -1 && preStartIndex != bufferLength) {
            tokenStartIndex = preStartIndex;
            tokenEndIndex = bufferLength;
            findStartIndex = bufferLength;
            matcher.region(tokenStartIndex, tokenEndIndex);
            setSuccess = true;
        }
        // If the first delimiter of scanner is not at the find start position
        if (findIndex != -1 && preStartIndex != matcher.start()) {
            tokenStartIndex = preStartIndex;
            tokenEndIndex = matcher.start();
            findStartIndex = matcher.start();
            // set match region and return
            matcher.region(tokenStartIndex, tokenEndIndex);
            setSuccess = true;
        }
        return setSuccess;
    }

    private int findDelimiterAfter() {
        int tokenEndIndex;
        boolean findComplete = false;
        while (!findComplete) {
            if (matcher.find()) {
                findComplete = true;
                if (matcher.start() == findStartIndex && matcher.start() == matcher.end()) {
                    findComplete = false;
                }
            } else {
                if (!inputExhausted) {
                    readMore();
                    resetMatcher();
                } else {
                    return -1;
                }
            }
        }
        tokenEndIndex = matcher.start();
        findStartIndex = tokenEndIndex;
        return tokenEndIndex;
    }

    /*
     * Read more data from underlying Readable. If nothing is available or I/O
     * operation fails, global boolean variable inputExhausted will be set to
     * true, otherwise set to false.
     */
    private void readMore() {
        int oldPosition = buffer.position();
        int oldBufferLength = bufferLength;
        // Increase capacity if empty space is not enough
        if (bufferLength >= buffer.capacity()) {
            expandBuffer();
        }

        // Read input resource
        int readCount;
        try {
            buffer.limit(buffer.capacity());
            buffer.position(oldBufferLength);
            while ((readCount = input.read(buffer)) == 0) {
                // nothing to do here
            }
        } catch (IOException e) {
            // Consider the scenario: readable puts 4 chars into
            // buffer and then an IOException is thrown out. In this case,
            // buffer is actually grown, but readable.read() will never return.
            bufferLength = buffer.position();
            // Use -1 to record IOException occurring, and no more input can be read.
            readCount = -1;
            lastIOException = e;
        }

        buffer.flip();
        buffer.position(oldPosition);
        if (readCount == -1) {
            inputExhausted = true;
        } else {
            bufferLength = readCount + bufferLength;
        }
    }

    // Expand the size of internal buffer.
    private void expandBuffer() {
        int oldPosition = buffer.position();
        int oldCapacity = buffer.capacity();
        int oldLimit = buffer.limit();
        int newCapacity = oldCapacity * 2;
        char[] newBuffer = new char[newCapacity];
        System.arraycopy(buffer.array(), 0, newBuffer, 0, oldLimit);
        buffer = CharBuffer.wrap(newBuffer, 0, newCapacity);
        buffer.position(oldPosition);
        buffer.limit(oldLimit);
    }

    /**
     * Resets this scanner's delimiter, locale, and radix.
     *
     * @return this scanner
     * @since 1.6
     */
    public Scanner reset() {
        delimiter = DEFAULT_DELIMITER;
        setLocale(Locale.getDefault());
        currentRadix = DEFAULT_RADIX;
        return this;
    }
}
