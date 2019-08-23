/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.nio.charset;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Tests for the encoding behavior of charsets in {@link StandardCharsets}.
 */
@RunWith(DataProviderRunner.class)
public class StandardCharsetsEncoderTest {

    private static final String DELIMITER = ":";
    /** Big enough for a single codepoint */
    private static final CharBuffer CHAR_BUFFER = CharBuffer.allocate(5);
    /** Big enough for the encoding for a single code point */
    private static final ByteBuffer BYTE_BUFFER = ByteBuffer.allocate(5);

    /**
     * Returns the charsets to test.
     */
    @DataProvider
    public static Charset[] getStandardCharsets() throws Exception {
        List<Charset> charsets = new ArrayList<>();
        for (Field field : StandardCharsets.class.getFields()) {
            if (field.getType() == Charset.class) {
                charsets.add((Charset) field.get(null));
            }
        }
        assertTrue(charsets.size() >= 6);
        return charsets.toArray(new Charset[0]);
    }

    @DataProvider
    public static Object[][] provider_getStandardCharsets() throws Exception {
        Charset[] charsets = getStandardCharsets();
        int n = charsets.length;
        Object[][] result = new Object[n][1];
        for (int i = 0; i < n; i++) {
            result[i][0] = charsets[i];
        }
        return result;
    }

    /**
     * Tests recorded reference data against actual encoder behavior.
     * Reference data files can be created / updated using {@link Dumper} to dump existing Android
     * behavior.
     */
    @UseDataProvider("provider_getStandardCharsets")
    @Test
    public void testCharset(Charset charset) throws Exception {
        String fileName = createFileName(charset);
        CharsetEncoder encoder = charset.newEncoder();
        List<String> failures = new ArrayList<>();
        try (BufferedReader reader = createAsciiReader(openResource(fileName))) {
            String expectedInfo;
            while ((expectedInfo = reader.readLine()) != null) {
                // Ignore comment lines
                if (expectedInfo.startsWith("#")) {
                    continue;
                }
                String[] parts = expectedInfo.split(DELIMITER);
                int codePoint = Integer.parseInt(parts[0]);
                String actualInfo = createCodePointInfo(encoder, codePoint);
                if (!expectedInfo.equals(actualInfo)) {
                    failures.add("Expected=" + expectedInfo + ", actual=" + actualInfo);
                }
            }
        }
        if (!failures.isEmpty()) {
            fail("Failures:\n" + failures);
        }
    }

    private static InputStream openResource(String fileName) {
        InputStream is = StandardCharsetsEncoderTest.class.getResourceAsStream(fileName);
        if (is == null) {
            fail("No resource found: " + fileName);
        }
        return is;
    }

    /**
     * Generates reference data for use by {@link #testCharset(Charset)}. Run the main method in
     * this class to obtain new reference files from current Android behavior. Pass the name of the
     * directory in which to create files.
     *
     * <p>For example:
     * <pre>
     * make vogar && make build-art-host && make core-tests
     * vogar --mode host --runner-type main \
     *   --classpath \
     *   ${ANDROID_PRODUCT_OUT}/obj/JAVA_LIBRARIES/core-tests_intermediates/javalib.jar \
     *   'libcore.java.nio.charset.StandardCharsetsEncoderTest$Dumper' \
     *   -- libcore/luni/src/test/resources/libcore/java/nio/charset
     * </pre>
     */
    public static class Dumper {

        public static void main(String[] args) throws Exception {
            String dir = args[0];
            for (Charset charset : getStandardCharsets()) {
                CharsetEncoder coreEncoder = charset.newEncoder();
                dumpEncodings(coreEncoder, dir + "/" + createFileName(charset));
            }
        }

        /**
         * Generates a set of reference data from the current CharsetEncoder behavior into the
         * specified file.
         */
        private static void dumpEncodings(CharsetEncoder encoder, String fileName)
                throws IOException {
            encoder.onMalformedInput(CodingErrorAction.IGNORE);
            encoder.onUnmappableCharacter(CodingErrorAction.IGNORE);

            try (BufferedWriter writer = createAsciiWriter(new FileOutputStream(fileName))) {
                writeLine(writer, "# Reference encodings for " + encoder.charset().name()
                        + " generated by " + Dumper.class);
                writeLine(writer, "# Encodings are used by " + StandardCharsetsEncoderTest.class);
                writeLine(writer, "# {codepoint}:{canEncode}:{encoding bytes}");

                for (int codePoint = 0; codePoint < 0xfffd; codePoint++) {
                    String codePointInfo = createCodePointInfo(encoder, codePoint);
                    writeLine(writer, codePointInfo);
                }
            }
        }

        private static void writeLine(BufferedWriter writer, String text) throws IOException {
            writer.append(text);
            writer.newLine();
        }
    }

    private static String createCodePointInfo(CharsetEncoder encoder, int codePoint) {
        StringBuilder stringBuilder = new StringBuilder();
        String utf16 = new String(Character.toChars(codePoint));

        // Format: {codepoint:int}:{canEncode():bool}:{encode():bytes}
        stringBuilder.append(Integer.toString(codePoint));
        stringBuilder.append(DELIMITER);
        stringBuilder.append(Boolean.toString(encoder.canEncode(utf16)));
        stringBuilder.append(DELIMITER);

        // Encode
        CHAR_BUFFER.append(utf16);
        CHAR_BUFFER.flip();

        encoder.encode(CHAR_BUFFER, BYTE_BUFFER, true /* endOfInput */);
        encoder.reset();

        BYTE_BUFFER.flip();

        // Append the encoded bytes, if any.
        byte[] bytes = new byte[BYTE_BUFFER.limit()];
        BYTE_BUFFER.get(bytes, 0, BYTE_BUFFER.limit());
        stringBuilder.append(createBytesString(bytes));

        CHAR_BUFFER.clear();
        BYTE_BUFFER.clear();

        return stringBuilder.toString();
    }

    private static String createBytesString(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            int byteValues = Byte.toUnsignedInt(bytes[i]);
            builder.append(Integer.toString(byteValues));
            if (i < bytes.length - 1) {
                builder.append(',');
            }
        }
        return builder.toString();
    }

    private static BufferedWriter createAsciiWriter(OutputStream out) {
        return new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.US_ASCII));
    }

    private static BufferedReader createAsciiReader(InputStream in) {
        return new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII));
    }

    private static String createFileName(Charset charset) {
        return "encodings_" + charset.name() + ".txt";
    }
}
