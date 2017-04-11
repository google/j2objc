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
package org.apache.harmony.tests.java.util;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.charset.Charset;
import java.security.Permission;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.DuplicateFormatFlagsException;
import java.util.FormatFlagsConversionMismatchException;
import java.util.Formattable;
import java.util.FormattableFlags;
import java.util.Formatter;
import java.util.FormatterClosedException;
import java.util.IllegalFormatCodePointException;
import java.util.IllegalFormatConversionException;
import java.util.IllegalFormatException;
import java.util.IllegalFormatFlagsException;
import java.util.IllegalFormatPrecisionException;
import java.util.IllegalFormatWidthException;
import java.util.Locale;
import java.util.MissingFormatArgumentException;
import java.util.MissingFormatWidthException;
import java.util.TimeZone;
import java.util.UnknownFormatConversionException;
import java.util.Formatter.BigDecimalLayoutForm;

import junit.framework.TestCase;

public class FormatterTest extends TestCase {
    private boolean root;

    class MockAppendable implements Appendable {
        public Appendable append(CharSequence arg0) throws IOException {
            return null;
        }

        public Appendable append(char arg0) throws IOException {
            return null;
        }

        public Appendable append(CharSequence arg0, int arg1, int arg2)
                throws IOException {
            return null;
        }
    }

    class MockFormattable implements Formattable {
        public void formatTo(Formatter formatter, int flags, int width,
                int precision) throws IllegalFormatException {
            if ((flags & FormattableFlags.UPPERCASE) != 0) {
                formatter.format("CUSTOMIZED FORMAT FUNCTION" + " WIDTH: "
                        + width + " PRECISION: " + precision);
            } else {
                formatter.format("customized format function" + " width: "
                        + width + " precision: " + precision);
            }
        }

        public String toString() {
            return "formattable object";
        }

        public int hashCode() {
            return 0xf;
        }
    }

    class MockDestination implements Appendable, Flushable {

        private StringBuilder data = new StringBuilder();

        private boolean enabled = false;

        public Appendable append(char c) throws IOException {
            if (enabled) {
                data.append(c);
                enabled = true; // enable it after the first append
            } else {
                throw new IOException();
            }
            return this;
        }

        public Appendable append(CharSequence csq) throws IOException {
            if (enabled) {
                data.append(csq);
                enabled = true; // enable it after the first append
            } else {
                throw new IOException();
            }
            return this;
        }

        public Appendable append(CharSequence csq, int start, int end)
                throws IOException {
            if (enabled) {
                data.append(csq, start, end);
                enabled = true; // enable it after the first append
            } else {
                throw new IOException();
            }
            return this;
        }

        public void flush() throws IOException {
            throw new IOException("Always throw IOException");
        }

        public String toString() {
            return data.toString();
        }
    }

    private File notExist;

    private File fileWithContent;

    private File readOnly;

    private File secret;

    private TimeZone defaultTimeZone;

    private Locale defaultLocale;

    /**
     * java.util.Formatter#Formatter()
     */
    public void test_Constructor() {
        Formatter f = new Formatter();
        assertNotNull(f);
        assertTrue(f.out() instanceof StringBuilder);
        assertEquals(f.locale(), Locale.getDefault());
        assertNotNull(f.toString());
    }

    /**
     * java.util.Formatter#Formatter(Appendable)
     */
    public void test_ConstructorLjava_lang_Appendable() {
        MockAppendable ma = new MockAppendable();
        Formatter f1 = new Formatter(ma);
        assertEquals(ma, f1.out());
        assertEquals(f1.locale(), Locale.getDefault());
        assertNotNull(f1.toString());

        Formatter f2 = new Formatter((Appendable) null);
        /*
         * If a(the input param) is null then a StringBuilder will be created
         * and the output can be attained by invoking the out() method. But RI
         * raises an error of FormatterClosedException when invoking out() or
         * toString().
         */
        Appendable sb = f2.out();
        assertTrue(sb instanceof StringBuilder);
        assertNotNull(f2.toString());
    }

    /**
     * java.util.Formatter#Formatter(Locale)
     */
    public void test_ConstructorLjava_util_Locale() {
        Formatter f1 = new Formatter(Locale.FRANCE);
        assertTrue(f1.out() instanceof StringBuilder);
        assertEquals(f1.locale(), Locale.FRANCE);
        assertNotNull(f1.toString());

        Formatter f2 = new Formatter((Locale) null);
        assertNull(f2.locale());
        assertTrue(f2.out() instanceof StringBuilder);
        assertNotNull(f2.toString());
    }

    /**
     * java.util.Formatter#Formatter(Appendable, Locale)
     */
    public void test_ConstructorLjava_lang_AppendableLjava_util_Locale() {
        MockAppendable ma = new MockAppendable();
        Formatter f1 = new Formatter(ma, Locale.CANADA);
        assertEquals(ma, f1.out());
        assertEquals(f1.locale(), Locale.CANADA);

        Formatter f2 = new Formatter(ma, null);
        assertNull(f2.locale());
        assertEquals(ma, f1.out());

        Formatter f3 = new Formatter(null, Locale.GERMAN);
        assertEquals(f3.locale(), Locale.GERMAN);
        assertTrue(f3.out() instanceof StringBuilder);
    }

    /**
     * java.util.Formatter#Formatter(String)
     */
    public void test_ConstructorLjava_lang_String() throws IOException {
        Formatter f = null;
        try {
            f = new Formatter((String) null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e1) {
            // expected
        }

        f = new Formatter(notExist.getPath());
        assertEquals(f.locale(), Locale.getDefault());
        f.close();

        f = new Formatter(fileWithContent.getPath());
        assertEquals(0, fileWithContent.length());
        f.close();

        if (!root) {
            try {
                f = new Formatter(readOnly.getPath());
                fail("should throw FileNotFoundException");
            } catch (FileNotFoundException e) {
                // expected
            }
        }
    }

    /**
     * java.util.Formatter#Formatter(String, String)
     */
    public void test_ConstructorLjava_lang_StringLjava_lang_String()
            throws IOException {
        Formatter f = null;
        try {
            f = new Formatter((String) null, Charset.defaultCharset().name());
            fail("should throw NullPointerException");
        } catch (NullPointerException e1) {
            // expected
        }

        try {
            f = new Formatter(notExist.getPath(), null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e2) {
            // expected
        }

        f = new Formatter(notExist.getPath(), Charset.defaultCharset().name());
        assertEquals(f.locale(), Locale.getDefault());
        f.close();

        try {
            f = new Formatter(notExist.getPath(), "ISO 1111-1");
            fail("should throw UnsupportedEncodingException");
        } catch (UnsupportedEncodingException e1) {
            // expected
        }

        f = new Formatter(fileWithContent.getPath(), "UTF-16BE");
        assertEquals(0, fileWithContent.length());
        f.close();

        if (!root) {
            try {
                f = new Formatter(readOnly.getPath(), "UTF-16BE");
                fail("should throw FileNotFoundException");
            } catch (FileNotFoundException e) {
                // expected
            }
        }
    }

    /**
     * java.util.Formatter#Formatter(String, String, Locale)
     */
    public void test_ConstructorLjava_lang_StringLjava_lang_StringLjava_util_Locale()
            throws IOException {
        Formatter f = null;
        try {
            f = new Formatter((String) null, Charset.defaultCharset().name(),
                    Locale.KOREA);
            fail("should throw NullPointerException");
        } catch (NullPointerException e1) {
            // expected
        }

        try {
            f = new Formatter(notExist.getPath(), null, Locale.KOREA);
            fail("should throw NullPointerException");
        } catch (NullPointerException e2) {
            // expected
        }

        f = new Formatter(notExist.getPath(), Charset.defaultCharset().name(),
                null);
        assertNotNull(f);
        f.close();

        f = new Formatter(notExist.getPath(), Charset.defaultCharset().name(),
                Locale.KOREA);
        assertEquals(f.locale(), Locale.KOREA);
        f.close();

        try {
            f = new Formatter(notExist.getPath(), "ISO 1111-1", Locale.CHINA);
            fail("should throw UnsupportedEncodingException");
        } catch (UnsupportedEncodingException e1) {
            // expected
        }

        f = new Formatter(fileWithContent.getPath(), "UTF-16BE",
                Locale.CANADA_FRENCH);
        assertEquals(0, fileWithContent.length());
        f.close();

        if (!root) {
            try {
                f = new Formatter(readOnly.getPath(), Charset.defaultCharset()
                        .name(), Locale.ITALY);
                fail("should throw FileNotFoundException");
            } catch (FileNotFoundException e) {
                // expected
            }
        }
    }

    /**
     * java.util.Formatter#Formatter(File)
     */
    public void test_ConstructorLjava_io_File() throws IOException {
        Formatter f = null;
        try {
            f = new Formatter((File) null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e1) {
            // expected
        }

        f = new Formatter(notExist);
        assertEquals(f.locale(), Locale.getDefault());
        f.close();

        f = new Formatter(fileWithContent);
        assertEquals(0, fileWithContent.length());
        f.close();

        if (!root) {
            try {
                f = new Formatter(readOnly);
                fail("should throw FileNotFoundException");
            } catch (FileNotFoundException e) {
                // expected
            }
        }
    }

    /**
     * java.util.Formatter#Formatter(File, String)
     */
    public void test_ConstructorLjava_io_FileLjava_lang_String()
            throws IOException {
        Formatter f = null;
        try {
            f = new Formatter((File) null, Charset.defaultCharset().name());
            fail("should throw NullPointerException");
        } catch (NullPointerException e1) {
            // expected
        }

        f = new Formatter(notExist, Charset.defaultCharset().name());
        assertEquals(f.locale(), Locale.getDefault());
        f.close();

        f = new Formatter(fileWithContent, "UTF-16BE");
        assertEquals(0, fileWithContent.length());
        f.close();

        if (!root) {
            try {
                f = new Formatter(readOnly, Charset.defaultCharset().name());
                fail("should throw FileNotFoundException");
            } catch (FileNotFoundException e) {
                // expected
            }
        }

        try {
            f = new Formatter(notExist, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e2) {
            // expected
        } finally {
            if (notExist.exists()) {
                // Fail on RI on Windows, because output stream is created and
                // not closed when exception thrown
                assertTrue(notExist.delete());
            }
        }

        try {
            f = new Formatter(notExist, "ISO 1111-1");
            fail("should throw UnsupportedEncodingException");
        } catch (UnsupportedEncodingException e1) {
            // expected
        } finally {
            if (notExist.exists()) {
                // Fail on RI on Windows, because output stream is created and
                // not closed when exception thrown
                assertTrue(notExist.delete());
            }
        }
    }

    /**
     * java.util.Formatter#Formatter(File, String, Locale)
     */
    public void test_ConstructorLjava_io_FileLjava_lang_StringLjava_util_Locale()
            throws IOException {
        Formatter f = null;
        try {
            f = new Formatter((File) null, Charset.defaultCharset().name(),
                    Locale.KOREA);
            fail("should throw NullPointerException");
        } catch (NullPointerException e1) {
            // expected
        }

        try {
            f = new Formatter(notExist, null, Locale.KOREA);
            fail("should throw NullPointerException");
        } catch (NullPointerException e2) {
            // expected
        }

        f = new Formatter(notExist, Charset.defaultCharset().name(), null);
        assertNotNull(f);
        f.close();

        f = new Formatter(notExist, Charset.defaultCharset().name(),
                Locale.KOREA);
        assertEquals(f.locale(), Locale.KOREA);
        f.close();

        try {
            f = new Formatter(notExist, "ISO 1111-1", Locale.CHINA);
            fail("should throw UnsupportedEncodingException");
        } catch (UnsupportedEncodingException e1) {
            // expected
        }
        f = new Formatter(fileWithContent.getPath(), "UTF-16BE",
                Locale.CANADA_FRENCH);
        assertEquals(0, fileWithContent.length());
        f.close();

        if (!root) {
            try {
                f = new Formatter(readOnly.getPath(), Charset.defaultCharset()
                        .name(), Locale.ITALY);
                fail("should throw FileNotFoundException");
            } catch (FileNotFoundException e) {
                // expected
            }
        }
    }

    /**
     * java.util.Formatter#Formatter(PrintStream)
     */
    public void test_ConstructorLjava_io_PrintStream() throws IOException {
        Formatter f = null;
        try {
            f = new Formatter((PrintStream) null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e1) {
            // expected
        }

        PrintStream ps = new PrintStream(notExist, "UTF-16BE");
        f = new Formatter(ps);
        assertEquals(Locale.getDefault(), f.locale());
        f.close();
    }

    /**
     * java.util.Formatter#Formatter(OutputStream)
     */
    public void test_ConstructorLjava_io_OutputStream() throws IOException {
        Formatter f = null;
        try {
            f = new Formatter((OutputStream) null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e1) {
            // expected
        }

        OutputStream os = new FileOutputStream(notExist);
        f = new Formatter(os);
        assertEquals(Locale.getDefault(), f.locale());
        f.close();
    }

    /**
     * java.util.Formatter#Formatter(OutputStream, String)
     */
    public void test_ConstructorLjava_io_OutputStreamLjava_lang_String()
            throws IOException {
        Formatter f = null;
        try {
            f = new Formatter((OutputStream) null, Charset.defaultCharset()
                    .name());
            fail("should throw NullPointerException");
        } catch (NullPointerException e1) {
            // expected
        }

        OutputStream os = null;
        try {
            os = new FileOutputStream(notExist);
            f = new Formatter(os, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException e2) {
            // expected
        } finally {
            os.close();
        }

        try {
            os = new PipedOutputStream();
            f = new Formatter(os, "TMP-1111");
            fail("should throw UnsupportedEncodingException");
        } catch (UnsupportedEncodingException e1) {
            // expected
        } finally {
            os.close();
        }

        os = new FileOutputStream(fileWithContent);
        f = new Formatter(os, "UTF-16BE");
        assertEquals(Locale.getDefault(), f.locale());
        f.close();
    }

    /**
     * Test method for 'java.util.Formatter.Formatter(OutputStream, String,
     * Locale)
     */
    public void test_ConstructorLjava_io_OutputStreamLjava_lang_StringLjava_util_Locale()
            throws IOException {
        Formatter f = null;
        try {
            f = new Formatter((OutputStream) null, Charset.defaultCharset()
                    .name(), Locale.getDefault());
            fail("should throw NullPointerException");
        } catch (NullPointerException e1) {
            // expected
        }

        OutputStream os = null;
        try {
            os = new FileOutputStream(notExist);
            f = new Formatter(os, null, Locale.getDefault());
            fail("should throw NullPointerException");
        } catch (NullPointerException e2) {
            // expected
        } finally {
            os.close();
        }

        os = new FileOutputStream(notExist);
        f = new Formatter(os, Charset.defaultCharset().name(), null);
        f.close();

        try {
            os = new PipedOutputStream();
            f = new Formatter(os, "TMP-1111", Locale.getDefault());
            fail("should throw UnsupportedEncodingException");
        } catch (UnsupportedEncodingException e1) {
            // expected
        }

        os = new FileOutputStream(fileWithContent);
        f = new Formatter(os, "UTF-16BE", Locale.ENGLISH);
        assertEquals(Locale.ENGLISH, f.locale());
        f.close();
    }

    /**
     * java.util.Formatter#locale()
     */
    public void test_locale() {
        Formatter f = null;
        f = new Formatter((Locale) null);
        assertNull(f.locale());

        f.close();
        try {
            f.locale();
            fail("should throw FormatterClosedException");
        } catch (FormatterClosedException e) {
            // expected
        }
    }

    /**
     * java.util.Formatter#out()
     */
    public void test_out() {
        Formatter f = null;
        f = new Formatter();
        assertNotNull(f.out());
        assertTrue(f.out() instanceof StringBuilder);
        f.close();
        try {
            f.out();
            fail("should throw FormatterClosedException");
        } catch (FormatterClosedException e) {
            // expected
        }

    }

    /**
     * java.util.Formatter#flush()
     */
    public void test_flush() throws IOException {
        Formatter f = null;
        f = new Formatter(notExist);
        assertTrue(f instanceof Flushable);
        f.close();
        try {
            f.flush();
            fail("should throw FormatterClosedException");
        } catch (FormatterClosedException e) {
            // expected
        }

        f = new Formatter();
        // For destination that does not implement Flushable
        // No exception should be thrown
        f.flush();
    }

    /**
     * java.util.Formatter#close()
     */
    public void test_close() throws IOException {
        Formatter f = new Formatter(notExist);
        assertTrue(f instanceof Closeable);
        f.close();
        // close next time will not throw exception
        f.close();
        assertNull(f.ioException());
    }

    /**
     * java.util.Formatter#toString()
     */
    public void test_toString() {
        Formatter f = new Formatter();
        assertNotNull(f.toString());
        assertEquals(f.out().toString(), f.toString());
        f.close();
        try {
            f.toString();
            fail("should throw FormatterClosedException");
        } catch (FormatterClosedException e) {
            // expected
        }
    }

    /**
     * java.util.Formatter#ioException()
     */
    public void test_ioException() throws IOException {
        Formatter f = null;
        f = new Formatter(new MockDestination());
        assertNull(f.ioException());
        f.flush();
        assertNotNull(f.ioException());
        f.close();

        MockDestination md = new MockDestination();
        f = new Formatter(md);
        f.format("%s%s", "1", "2");
        // format stop working after IOException
        assertNotNull(f.ioException());
        assertEquals("", f.toString());
    }

    /**
     * java.util.Formatter#format(String, Object...) for null parameter
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_null() {
        Formatter f = new Formatter();
        try {
            f.format((String) null, "parameter");
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }

        f = new Formatter();
        f.format("hello", (Object[]) null);
        assertEquals("hello", f.toString());
    }

    /**
     * java.util.Formatter#format(String, Object...) for argument index
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_ArgIndex() {
        Formatter formatter = new Formatter(Locale.US);
        formatter.format("%1$s%2$s%3$s%4$s%5$s%6$s%7$s%8$s%9$s%11$s%10$s", "1",
                "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
        assertEquals("1234567891110", formatter.toString());

        formatter = new Formatter(Locale.JAPAN);
        formatter.format("%0$s", "hello");
        assertEquals("hello", formatter.toString());

        try {
            formatter = new Formatter(Locale.US);
            formatter.format("%-1$s", "1", "2");
            fail("should throw UnknownFormatConversionException");
        } catch (UnknownFormatConversionException e) {
            // expected
        }

        try {
            formatter = new Formatter(Locale.US);
            formatter.format("%$s", "hello", "2");
            fail("should throw UnknownFormatConversionException");
        } catch (UnknownFormatConversionException e) {
            // expected
        }

        try {
            Formatter f = new Formatter(Locale.US);
            f.format("%", "string");
            fail("should throw UnknownFormatConversionException");
        } catch (UnknownFormatConversionException e) {
            // expected
        }

        formatter = new Formatter(Locale.FRANCE);
        formatter.format("%1$s%2$s%3$s%4$s%5$s%6$s%7$s%8$s%<s%s%s%<s", "1",
                "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
        assertEquals("123456788122", formatter.toString());

        formatter = new Formatter(Locale.FRANCE);
        formatter.format(
                "xx%1$s22%2$s%s%<s%5$s%<s&%7$h%2$s%8$s%<s%s%s%<ssuffix", "1",
                "2", "3", "4", "5", "6", 7, "8", "9", "10", "11");
        assertEquals("xx12221155&7288233suffix", formatter.toString());

        try {
            formatter.format("%<s", "hello");
            fail("should throw MissingFormatArgumentException");
        } catch (MissingFormatArgumentException e) {
            // expected
        }

        formatter = new Formatter(Locale.US);
        try {
            formatter.format("%123$s", "hello");
            fail("should throw MissingFormatArgumentException");
        } catch (MissingFormatArgumentException e) {
            // expected
        }

        formatter = new Formatter(Locale.US);
        try {
            // 2147483648 is the value of Integer.MAX_VALUE + 1
            formatter.format("%2147483648$s", "hello");
            fail("should throw MissingFormatArgumentException");
        } catch (MissingFormatArgumentException e) {
            // expected
        }

        try {
            // 2147483647 is the value of Integer.MAX_VALUE
            formatter.format("%2147483647$s", "hello");
            fail("should throw MissingFormatArgumentException");
        } catch (MissingFormatArgumentException e) {
            // expected
        }

        formatter = new Formatter(Locale.US);
        try {
            formatter.format("%s%s", "hello");
            fail("should throw MissingFormatArgumentException");
        } catch (MissingFormatArgumentException e) {
            // expected
        }

        formatter = new Formatter(Locale.US);
        formatter.format("$100", 100);
        assertEquals("$100", formatter.toString());

        formatter = new Formatter(Locale.UK);
        formatter.format("%01$s", "string");
        assertEquals("string", formatter.toString());
    }

    /**
     * java.util.Formatter#format(String, Object...) for width
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_Width() {
        Formatter f = new Formatter(Locale.US);
        f.format("%1$8s", "1");
        assertEquals("       1", f.toString());

        f = new Formatter(Locale.US);
        f.format("%1$-1%", "string");
        assertEquals("%", f.toString());

        f = new Formatter(Locale.ITALY);
        // 2147483648 is the value of Integer.MAX_VALUE + 1
        f.format("%2147483648s", "string");
        assertEquals("string", f.toString());

        // the value of Integer.MAX_VALUE will allocate about 4G bytes of
        // memory.
        // It may cause OutOfMemoryError, so this value is not tested
    }

    /**
     * java.util.Formatter#format(String, Object...) for precision
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_Precision() {
        Formatter f = new Formatter(Locale.US);
        f.format("%.5s", "123456");
        assertEquals("12345", f.toString());

        f = new Formatter(Locale.US);
        // 2147483648 is the value of Integer.MAX_VALUE + 1
        f.format("%.2147483648s", "...");
        assertEquals("...", f.toString());

        // the value of Integer.MAX_VALUE will allocate about 4G bytes of
        // memory.
        // It may cause OutOfMemoryError, so this value is not tested

        f = new Formatter(Locale.US);
        f.format("%10.0b", Boolean.TRUE);
        assertEquals("          ", f.toString());

        f = new Formatter(Locale.US);
        f.format("%10.01s", "hello");
        assertEquals("         h", f.toString());

        try {
            f = new Formatter(Locale.US);
            f.format("%.s", "hello", "2");
            fail("should throw Exception");
        } catch (UnknownFormatConversionException
                 | IllegalFormatPrecisionException expected) {
            // expected
        }

        try {
            f = new Formatter(Locale.US);
            f.format("%.-5s", "123456");
            fail("should throw Exception");
        } catch (UnknownFormatConversionException
                 | IllegalFormatPrecisionException expected) {
            // expected
        }

        try {
            f = new Formatter(Locale.US);
            f.format("%1.s", "hello", "2");
            fail("should throw Exception");
        } catch (UnknownFormatConversionException
                 | IllegalFormatPrecisionException expected) {
            // expected
        }

        f = new Formatter(Locale.US);
        f.format("%5.1s", "hello");
        assertEquals("    h", f.toString());

        f = new Formatter(Locale.FRANCE);
        f.format("%.0s", "hello", "2");
        assertEquals("", f.toString());
    }

    /**
     * java.util.Formatter#format(String, Object...) for line sperator
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_LineSeparator() {
        Formatter f = null;

        /* J2ObjC: Setting line.separator has no effect, see System.lineSeparator().
        String oldSeparator = System.getProperty("line.separator");
        try {
            System.setProperty("line.separator", "!\n");

            f = new Formatter(Locale.US);
            f.format("%1$n", 1);
            assertEquals("!\n", f.toString());

            f = new Formatter(Locale.KOREAN);
            f.format("head%1$n%2$n", 1, new Date());
            assertEquals("head!\n!\n", f.toString());

            f = new Formatter(Locale.US);
            f.format("%n%s", "hello");
            assertEquals("!\nhello", f.toString());
        } finally {
            System.setProperty("line.separator", oldSeparator);
        }*/

        f = new Formatter(Locale.US);
        try {
            f.format("%-n");
            fail("should throw IllegalFormatFlagsException: %-n");
        } catch (IllegalFormatFlagsException e) {
            // expected
        }
        try {
            f.format("%+n");
            fail("should throw IllegalFormatFlagsException: %+n");
        } catch (IllegalFormatFlagsException e) {
            // expected
        }
        try {
            f.format("%#n");
            fail("should throw IllegalFormatFlagsException: %#n");
        } catch (IllegalFormatFlagsException e) {
            // expected
        }
        try {
            f.format("% n");
            fail("should throw IllegalFormatFlagsException: % n");
        } catch (IllegalFormatFlagsException e) {
            // expected
        }
        try {
            f.format("%0n");
            fail("should throw IllegalFormatFlagsException: %0n");
        } catch (IllegalFormatFlagsException e) {
            // expected
        }
        try {
            f.format("%,n");
            fail("should throw IllegalFormatFlagsException: %,n");
        } catch (IllegalFormatFlagsException e) {
            // expected
        }
        try {
            f.format("%(n");
            fail("should throw IllegalFormatFlagsException: %(n");
        } catch (IllegalFormatFlagsException e) {
            // expected
        }

        f = new Formatter(Locale.US);
        try {
            f.format("%4n");
            fail("should throw IllegalFormatWidthException");
        } catch (IllegalFormatWidthException e) {
            // expected
        }

        f = new Formatter(Locale.US);
        try {
            f.format("%-4n");
            fail("should throw IllegalFormatWidthException");
        } catch (IllegalFormatWidthException e) {
            // expected
        }

        f = new Formatter(Locale.US);
        try {
            f.format("%.9n");
            fail("should throw IllegalFormatPrecisionException");
        } catch (IllegalFormatPrecisionException e) {
            // expected
        }

        f = new Formatter(Locale.US);
        try {
            f.format("%5.9n");
            fail("should throw IllegalFormatPrecisionException");
        } catch (IllegalFormatPrecisionException e) {
            // expected
        }

        //System.setProperty("line.separator", oldSeparator);
    }

    /**
     * java.util.Formatter#format(String, Object...) for percent
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_Percent() {
        Formatter f = null;

        f = new Formatter(Locale.ENGLISH);
        f.format("%1$%", 100);
        assertEquals("%", f.toString());

        f = new Formatter(Locale.CHINA);
        f.format("%1$%%%", "hello", new Object());
        assertEquals("%%", f.toString());

        f = new Formatter(Locale.CHINA);
        f.format("%%%s", "hello");
        assertEquals("%hello", f.toString());

        f = new Formatter(Locale.US);
        try {
            f.format("%.9%");
            fail("should throw IllegalFormatPrecisionException");
        } catch (IllegalFormatPrecisionException e) {
            // expected
        }

        f = new Formatter(Locale.US);
        try {
            f.format("%5.9%");
            fail("should throw IllegalFormatPrecisionException");
        } catch (IllegalFormatPrecisionException e) {
            // expected
        }

        /* J2ObjC: Throws IllegalFormatFlagsException.
        f = new Formatter(Locale.US);
        assertFormatFlagsConversionMismatchException(f, "%+%");
        assertFormatFlagsConversionMismatchException(f, "%#%");
        assertFormatFlagsConversionMismatchException(f, "% %");
        assertFormatFlagsConversionMismatchException(f, "%0%");
        assertFormatFlagsConversionMismatchException(f, "%,%");
        assertFormatFlagsConversionMismatchException(f, "%(%");*/


        // J2ObjC: Seems to be more compatible with the RI than Android, strangely.
        // f = new Formatter(Locale.KOREAN);
        // f.format("%4%", 1);
        // /*
        //  * fail on RI the output string should be right justified by appending
        //  * spaces till the whole string is 4 chars width.
        //  */
        // assertEquals("   %", f.toString());

        // f = new Formatter(Locale.US);
        // f.format("%-4%", 100);
        // /*
        //  * fail on RI, throw UnknownFormatConversionException the output string
        //  * should be left justified by appending spaces till the whole string is
        //  * 4 chars width.
        //  */
        // assertEquals("%   ", f.toString());
    }

    private void assertFormatFlagsConversionMismatchException(Formatter f, String str) {
        try {
            f.format(str);
            fail("should throw FormatFlagsConversionMismatchException: "
                    + str);
            /*
            * error on RI, throw IllegalFormatFlagsException specification
            * says FormatFlagsConversionMismatchException should be thrown
            */
        } catch (FormatFlagsConversionMismatchException e) {
            // expected
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for flag
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_Flag() {
        Formatter f = new Formatter(Locale.US);
        try {
            f.format("%1$-#-8s", "something");
            fail("should throw DuplicateFormatFlagsException");
        } catch (DuplicateFormatFlagsException e) {
            // expected
        }

        final char[] chars = { '-', '#', '+', ' ', '0', ',', '(', '%', '<' };
        Arrays.sort(chars);
        f = new Formatter(Locale.US);
        for (char i = 0; i <= 256; i++) {
            // test 8 bit character
            if (Arrays.binarySearch(chars, i) >= 0 || Character.isDigit(i)
                    || Character.isLetter(i)) {
                // Do not test 0-9, a-z, A-Z and characters in the chars array.
                // They are characters used as flags, width or conversions
                continue;
            }
            try {
                f.format("%" + i + "s", 1);
                fail("should throw UnknownFormatConversionException");
            } catch (UnknownFormatConversionException e) {
                // expected
            } catch (IllegalFormatPrecisionException e) {
                // If i is '.', s can also be interpreted as an illegal precision.
                if (i != '.') {
                    throw e;
                }
            }
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for general
     * conversion b/B
     */
    public void test_format_LString$LObject_GeneralConversionB() {
        final Object[][] triple = {
                { Boolean.FALSE, "%3.2b", " fa", },
                { Boolean.FALSE, "%-4.6b", "false", },
                { Boolean.FALSE, "%.2b", "fa", },
                { Boolean.TRUE, "%3.2b", " tr", },
                { Boolean.TRUE, "%-4.6b", "true", },
                { Boolean.TRUE, "%.2b", "tr", },
                { new Character('c'), "%3.2b", " tr", },
                { new Character('c'), "%-4.6b", "true", },
                { new Character('c'), "%.2b", "tr", },
                { new Byte((byte) 0x01), "%3.2b", " tr", },
                { new Byte((byte) 0x01), "%-4.6b", "true", },
                { new Byte((byte) 0x01), "%.2b", "tr", },
                { new Short((short) 0x0001), "%3.2b", " tr", },
                { new Short((short) 0x0001), "%-4.6b", "true", },
                { new Short((short) 0x0001), "%.2b", "tr", },
                { new Integer(1), "%3.2b", " tr", },
                { new Integer(1), "%-4.6b", "true", },
                { new Integer(1), "%.2b", "tr", },
                { new Float(1.1f), "%3.2b", " tr", },
                { new Float(1.1f), "%-4.6b", "true", },
                { new Float(1.1f), "%.2b", "tr", },
                { new Double(1.1d), "%3.2b", " tr", },
                { new Double(1.1d), "%-4.6b", "true", },
                { new Double(1.1d), "%.2b", "tr", },
                { "", "%3.2b", " tr", },
                { "", "%-4.6b", "true", },
                { "", "%.2b", "tr", },
                { "string content", "%3.2b", " tr", },
                { "string content", "%-4.6b", "true", },
                { "string content", "%.2b", "tr", },
                { new MockFormattable(), "%3.2b", " tr", },
                { new MockFormattable(), "%-4.6b", "true", },
                { new MockFormattable(), "%.2b", "tr", },
                { (Object) null, "%3.2b", " fa", },
                { (Object) null, "%-4.6b", "false", },
                { (Object) null, "%.2b", "fa", },
        };


        final int input = 0;
        final int pattern = 1;
        final int output = 2;
        Formatter f = null;
        for (int i = 0; i < triple.length; i++) {
            f = new Formatter(Locale.FRANCE);
            f.format((String) triple[i][pattern], triple[i][input]);
            assertEquals("triple[" + i + "]:" + triple[i][input]
                    + ",pattern[" + i + "]:" + triple[i][pattern], triple[i][output], f.toString());

            f = new Formatter(Locale.GERMAN);
            f.format(((String) triple[i][pattern]).toUpperCase(Locale.US), triple[i][input]);
            assertEquals("triple[" + i + "]:" + triple[i][input]
                    + ",pattern[" + i + "]:" + triple[i][pattern], ((String) triple[i][output])
                    .toUpperCase(Locale.US), f.toString());
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for general
     * conversion type 's' and 'S'
     */
    public void test_format_LString$LObject_GeneralConversionS() {

        final Object[][] triple = {
                { Boolean.FALSE, "%2.3s", "fal", },
                { Boolean.FALSE, "%-6.4s", "fals  ", },
                { Boolean.FALSE, "%.5s", "false", },
                { Boolean.TRUE, "%2.3s", "tru", },
                { Boolean.TRUE, "%-6.4s", "true  ", },
                { Boolean.TRUE, "%.5s", "true", },
                { new Character('c'), "%2.3s", " c", },
                { new Character('c'), "%-6.4s", "c     ", },
                { new Character('c'), "%.5s", "c", },
                { new Byte((byte) 0x01), "%2.3s", " 1", },
                { new Byte((byte) 0x01), "%-6.4s", "1     ", },
                { new Byte((byte) 0x01), "%.5s", "1", },
                { new Short((short) 0x0001), "%2.3s", " 1", },
                { new Short((short) 0x0001), "%-6.4s", "1     ", },
                { new Short((short) 0x0001), "%.5s", "1", },
                { new Integer(1), "%2.3s", " 1", },
                { new Integer(1), "%-6.4s", "1     ", },
                { new Integer(1), "%.5s", "1", },
                { new Float(1.1f), "%2.3s", "1.1", },
                { new Float(1.1f), "%-6.4s", "1.1   ", },
                { new Float(1.1f), "%.5s", "1.1", },
                { new Double(1.1d), "%2.3s", "1.1", },
                { new Double(1.1d), "%-6.4s", "1.1   ", },
                { new Double(1.1d), "%.5s", "1.1", },
                { "", "%2.3s", "  ", },
                { "", "%-6.4s", "      ", },
                { "", "%.5s", "", },
                { "string content", "%2.3s", "str", },
                { "string content", "%-6.4s", "stri  ", },
                { "string content", "%.5s", "strin", },
                { new MockFormattable(), "%2.3s", "customized format function width: 2 precision: 3", },
                { new MockFormattable(), "%-6.4s", "customized format function width: 6 precision: 4", },
                { new MockFormattable(), "%.5s", "customized format function width: -1 precision: 5", },
                { (Object) null, "%2.3s", "nul", },
                { (Object) null, "%-6.4s", "null  ", },
                { (Object) null, "%.5s", "null", },
        };


        final int input = 0;
        final int pattern = 1;
        final int output = 2;
        Formatter f = null;
        for (int i = 0; i < triple.length; i++) {
            f = new Formatter(Locale.FRANCE);
            f.format((String) triple[i][pattern], triple[i][input]);
            assertEquals("triple[" + i + "]:" + triple[i][input]
                    + ",pattern[" + i + "]:" + triple[i][pattern], triple[i][output], f.toString());

            f = new Formatter(Locale.GERMAN);
            f.format(((String) triple[i][pattern]).toUpperCase(Locale.US), triple[i][input]);
            assertEquals("triple[" + i + "]:" + triple[i][input]
                    + ",pattern[" + i + "]:" + triple[i][pattern], ((String) triple[i][output])
                    .toUpperCase(Locale.US), f.toString());
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for general
     * conversion type 'h' and 'H'
     */
    public void test_format_LString$LObject_GeneralConversionH() {

        final Object[] input = {
                Boolean.FALSE,
                Boolean.TRUE,
                new Character('c'),
                new Byte((byte) 0x01),
                new Short((short) 0x0001),
                new Integer(1),
                new Float(1.1f),
                new Double(1.1d),
                "",
                "string content",
                new MockFormattable(),
                (Object) null,
        };

        Formatter f = null;
        for (int i = 0; i < input.length - 1; i++) {
            f = new Formatter(Locale.FRANCE);
            f.format("%h", input[i]);
            assertEquals("triple[" + i + "]:" + input[i],
                    Integer.toHexString(input[i].hashCode()), f.toString());

            f = new Formatter(Locale.GERMAN);
            f.format("%H", input[i]);
            assertEquals("triple[" + i + "]:" + input[i],
                    Integer.toHexString(input[i].hashCode()).toUpperCase(Locale.US), f.toString());
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for general
     * conversion other cases
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_GeneralConversionOther() {
        /*
         * In Turkish locale, the upper case of '\u0069' is '\u0130'. The
         * following test indicate that '\u0069' is coverted to upper case
         * without using the turkish locale.
         */
        Formatter f = new Formatter(new Locale("tr"));
        f.format("%S", "\u0069");
        //assertEquals("\u0049", f.toString());  J2ObjC changed.
        assertEquals("\u0130", f.toString());

        final Object[] input = {
                Boolean.FALSE,
                Boolean.TRUE,
                new Character('c'),
                new Byte((byte) 0x01),
                new Short((short) 0x0001),
                new Integer(1),
                new Float(1.1f),
                new Double(1.1d),
                "",
                "string content",
                new MockFormattable(),
                (Object) null,
        };
        f = new Formatter(Locale.GERMAN);
        for (int i = 0; i < input.length; i++) {
            if (!(input[i] instanceof Formattable)) {
                try {
                    f.format("%#s", input[i]);
                    /*
                     * fail on RI, spec says if the '#' flag is present and the
                     * argument is not a Formattable , then a
                     * FormatFlagsConversionMismatchException will be thrown.
                     */
                    fail("should throw FormatFlagsConversionMismatchException");
                } catch (FormatFlagsConversionMismatchException e) {
                    // expected
                }
            } else {
                f.format("%#s%<-#8s", input[i]);
                assertEquals(
                        "customized format function width: -1 precision: -1customized format function width: 8 precision: -1",
                        f.toString());
            }
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for general
     * conversion exception
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_GeneralConversionException() {
        final String[] flagMismatch = { "%#b", "%+b", "% b", "%0b", "%,b",
                "%(b", "%#B", "%+B", "% B", "%0B", "%,B", "%(B", "%#h", "%+h",
                "% h", "%0h", "%,h", "%(h", "%#H", "%+H", "% H", "%0H", "%,H",
                "%(H", "%+s", "% s", "%0s", "%,s", "%(s", "%+S", "% S", "%0S",
                "%,S", "%(S" };

        Formatter f = new Formatter(Locale.US);

        for (int i = 0; i < flagMismatch.length; i++) {
            try {
                f.format(flagMismatch[i], "something");
                fail("should throw FormatFlagsConversionMismatchException");
            } catch (FormatFlagsConversionMismatchException e) {
                // expected
            }
        }

        final String[] missingWidth = { "%-b", "%-B", "%-h", "%-H", "%-s",
                "%-S", };
        for (int i = 0; i < missingWidth.length; i++) {
            try {
                f.format(missingWidth[i], "something");
                fail("should throw MissingFormatWidthException");
            } catch (MissingFormatWidthException e) {
                // expected
            }
        }

        // Regression test
        f = new Formatter();
        try {
            f.format("%c", (byte) -0x0001);
            fail("Should throw IllegalFormatCodePointException");
        } catch (IllegalFormatCodePointException e) {
            // expected
        }

        f = new Formatter();
        try {
            f.format("%c", (short) -0x0001);
            fail("Should throw IllegalFormatCodePointException");
        } catch (IllegalFormatCodePointException e) {
            // expected
        }

        f = new Formatter();
        try {
            f.format("%c", -0x0001);
            fail("Should throw IllegalFormatCodePointException");
        } catch (IllegalFormatCodePointException e) {
            // expected
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for Character
     * conversion
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_CharacterConversion() {
        Formatter f = new Formatter(Locale.US);
        final Object[] illArgs = { Boolean.TRUE, new Float(1.1f),
                new Double(1.1d), "string content", new Float(1.1f), new Date() };
        for (int i = 0; i < illArgs.length; i++) {
            try {
                f.format("%c", illArgs[i]);
                fail("should throw IllegalFormatConversionException");
            } catch (IllegalFormatConversionException e) {
                // expected
            }
        }

        try {
            f.format("%c", Integer.MAX_VALUE);
            fail("should throw IllegalFormatCodePointException");
        } catch (IllegalFormatCodePointException e) {
            // expected
        }

        try {
            f.format("%#c", 'c');
            fail("should throw FormatFlagsConversionMismatchException");
        } catch (FormatFlagsConversionMismatchException e) {
            // expected
        }

        final Object[][] triple = {
                { 'c', "%c", "c" },
                { 'c', "%-2c", "c " },
                { '\u0123', "%c", "\u0123" },
                { '\u0123', "%-2c", "\u0123 " },
                { (byte) 0x11, "%c", "\u0011" },
                { (byte) 0x11, "%-2c", "\u0011 " },
                { (short) 0x1111, "%c", "\u1111" },
                { (short) 0x1111, "%-2c", "\u1111 " },
                { 0x11, "%c", "\u0011" },
                { 0x11, "%-2c", "\u0011 " },
        };

        final int input = 0;
        final int pattern = 1;
        final int output = 2;
        for (int i = 0; i < triple.length; i++) {
            f = new Formatter(Locale.US);
            f.format((String) triple[i][pattern], triple[i][input]);
            assertEquals(triple[i][output], f.toString());
        }

        f = new Formatter(Locale.US);
        f.format("%c", 0x10000);
        assertEquals(0x10000, f.toString().codePointAt(0));

        try {
            f.format("%2.2c", 'c');
            fail("should throw IllegalFormatPrecisionException");
        } catch (IllegalFormatPrecisionException e) {
            // expected
        }

        f = new Formatter(Locale.US);
        f.format("%C", 'w');
        // error on RI, throw UnknownFormatConversionException
        // RI do not support converter 'C'
        assertEquals("W", f.toString());

        f = new Formatter(Locale.JAPAN);
        f.format("%Ced", 0x1111);
        // error on RI, throw UnknownFormatConversionException
        // RI do not support converter 'C'
        assertEquals("\u1111ed", f.toString());
    }


    /**
     * java.util.Formatter#format(String, Object...) for legal
     * Byte/Short/Integer/Long conversion type 'd'
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_ByteShortIntegerLongConversionD() {
        final Object[][] triple = {
                { 0, "%d", "0" },
                { 0, "%10d", "         0" },
                { 0, "%-1d", "0" },
                { 0, "%+d", "+0" },
                { 0, "% d", " 0" },
                { 0, "%,d", "0" },
                { 0, "%(d", "0" },
                { 0, "%08d", "00000000" },
                { 0, "%-+,(11d", "+0         " },
                { 0, "%0 ,(11d", " 0000000000" },

                { (byte) 0xff, "%d", "-1" },
                { (byte) 0xff, "%10d", "        -1" },
                { (byte) 0xff, "%-1d", "-1" },
                { (byte) 0xff, "%+d", "-1" },
                { (byte) 0xff, "% d", "-1" },
                { (byte) 0xff, "%,d", "-1" },
                { (byte) 0xff, "%(d", "(1)" },
                { (byte) 0xff, "%08d", "-0000001" },
                { (byte) 0xff, "%-+,(11d", "(1)        " },
                { (byte) 0xff, "%0 ,(11d", "(000000001)" },

                { (short) 0xf123, "%d", "-3805" },
                { (short) 0xf123, "%10d", "     -3805" },
                { (short) 0xf123, "%-1d", "-3805" },
                { (short) 0xf123, "%+d", "-3805" },
                { (short) 0xf123, "% d", "-3805" },
                { (short) 0xf123, "%,d", "-3.805" },
                { (short) 0xf123, "%(d", "(3805)" },
                { (short) 0xf123, "%08d", "-0003805" },
                { (short) 0xf123, "%-+,(11d", "(3.805)    " },
                { (short) 0xf123, "%0 ,(11d", "(00003.805)" },

                { 0x123456, "%d", "1193046" },
                { 0x123456, "%10d", "   1193046" },
                { 0x123456, "%-1d", "1193046" },
                { 0x123456, "%+d", "+1193046" },
                { 0x123456, "% d", " 1193046" },
                { 0x123456, "%,d", "1.193.046" },
                { 0x123456, "%(d", "1193046" },
                { 0x123456, "%08d", "01193046" },
                { 0x123456, "%-+,(11d", "+1.193.046 " },
                { 0x123456, "%0 ,(11d", " 01.193.046" },

                { -3, "%d", "-3" },
                { -3, "%10d", "        -3" },
                { -3, "%-1d", "-3" },
                { -3, "%+d", "-3" },
                { -3, "% d", "-3" },
                { -3, "%,d", "-3" },
                { -3, "%(d", "(3)" },
                { -3, "%08d", "-0000003" },
                { -3, "%-+,(11d", "(3)        " },
                { -3, "%0 ,(11d", "(000000003)" },

                { 0x7654321L, "%d", "124076833" },
                { 0x7654321L, "%10d", " 124076833" },
                { 0x7654321L, "%-1d", "124076833" },
                { 0x7654321L, "%+d", "+124076833" },
                { 0x7654321L, "% d", " 124076833" },
                { 0x7654321L, "%,d", "124.076.833" },
                { 0x7654321L, "%(d", "124076833" },
                { 0x7654321L, "%08d", "124076833" },
                { 0x7654321L, "%-+,(11d", "+124.076.833" },
                { 0x7654321L, "%0 ,(11d", " 124.076.833" },

                { -1L, "%d", "-1" },
                { -1L, "%10d", "        -1" },
                { -1L, "%-1d", "-1" },
                { -1L, "%+d", "-1" },
                { -1L, "% d", "-1" },
                { -1L, "%,d", "-1" },
                { -1L, "%(d", "(1)" },
                { -1L, "%08d", "-0000001" },
                { -1L, "%-+,(11d", "(1)        " },
                { -1L, "%0 ,(11d", "(000000001)" },
        };

        final int input = 0;
        final int pattern = 1;
        final int output = 2;
        Formatter f;
        for (int i = 0; i < triple.length; i++) {
            f = new Formatter(Locale.GERMAN);
            f.format((String) triple[i][pattern],
                    triple[i][input]);
            assertEquals("triple[" + i + "]:" + triple[i][input] + ",pattern["
                    + i + "]:" + triple[i][pattern], triple[i][output], f
                    .toString());
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for legal
     * Byte/Short/Integer/Long conversion type 'o'
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_ByteShortIntegerLongConversionO() {
        final Object[][] triple = {
                { 0, "%o", "0" },
                { 0, "%-6o", "0     " },
                { 0, "%08o", "00000000" },
                { 0, "%#o", "00" },
                { 0, "%0#11o", "00000000000" },
                { 0, "%-#9o", "00       " },

                { (byte) 0xff, "%o", "377" },
                { (byte) 0xff, "%-6o", "377   " },
                { (byte) 0xff, "%08o", "00000377" },
                { (byte) 0xff, "%#o", "0377" },
                { (byte) 0xff, "%0#11o", "00000000377" },
                { (byte) 0xff, "%-#9o", "0377     " },

                { (short) 0xf123, "%o", "170443" },
                { (short) 0xf123, "%-6o", "170443" },
                { (short) 0xf123, "%08o", "00170443" },
                { (short) 0xf123, "%#o", "0170443" },
                { (short) 0xf123, "%0#11o", "00000170443" },
                { (short) 0xf123, "%-#9o", "0170443  " },

                { 0x123456, "%o", "4432126" },
                { 0x123456, "%-6o", "4432126" },
                { 0x123456, "%08o", "04432126" },
                { 0x123456, "%#o", "04432126" },
                { 0x123456, "%0#11o", "00004432126" },
                { 0x123456, "%-#9o", "04432126 " },

                { -3, "%o", "37777777775" },
                { -3, "%-6o", "37777777775" },
                { -3, "%08o", "37777777775" },
                { -3, "%#o", "037777777775" },
                { -3, "%0#11o", "037777777775" },
                { -3, "%-#9o", "037777777775" },

                { 0x7654321L, "%o", "731241441" },
                { 0x7654321L, "%-6o", "731241441" },
                { 0x7654321L, "%08o", "731241441" },
                { 0x7654321L, "%#o", "0731241441" },
                { 0x7654321L, "%0#11o", "00731241441" },
                { 0x7654321L, "%-#9o", "0731241441" },

                { -1L, "%o", "1777777777777777777777" },
                { -1L, "%-6o", "1777777777777777777777" },
                { -1L, "%08o", "1777777777777777777777" },
                { -1L, "%#o", "01777777777777777777777" },
                { -1L, "%0#11o", "01777777777777777777777" },
                { -1L, "%-#9o", "01777777777777777777777" },
        };

        final int input = 0;
        final int pattern = 1;
        final int output = 2;
        Formatter f;
        for (int i = 0; i < triple.length; i++) {
            f = new Formatter(Locale.ITALY);
            f.format((String) triple[i][pattern],
                    triple[i][input]);
            assertEquals("triple[" + i + "]:" + triple[i][input] + ",pattern["
                    + i + "]:" + triple[i][pattern], triple[i][output], f
                    .toString());
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for legal
     * Byte/Short/Integer/Long conversion type 'x' and 'X'
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_ByteShortIntegerLongConversionX() {
        final Object[][] triple = {
                { 0, "%x", "0" },
                { 0, "%-8x", "0       " },
                { 0, "%06x", "000000" },
                { 0, "%#x", "0x0" },
                { 0, "%0#12x", "0x0000000000" },
                { 0, "%-#9x", "0x0      " },

                { (byte) 0xff, "%x", "ff" },
                { (byte) 0xff, "%-8x", "ff      " },
                { (byte) 0xff, "%06x", "0000ff" },
                { (byte) 0xff, "%#x", "0xff" },
                { (byte) 0xff, "%0#12x", "0x00000000ff" },
                { (byte) 0xff, "%-#9x", "0xff     " },

                { (short) 0xf123, "%x", "f123" },
                { (short) 0xf123, "%-8x", "f123    " },
                { (short) 0xf123, "%06x", "00f123" },
                { (short) 0xf123, "%#x", "0xf123" },
                { (short) 0xf123, "%0#12x", "0x000000f123" },
                { (short) 0xf123, "%-#9x", "0xf123   " },

                { 0x123456, "%x", "123456" },
                { 0x123456, "%-8x", "123456  " },
                { 0x123456, "%06x", "123456" },
                { 0x123456, "%#x", "0x123456" },
                { 0x123456, "%0#12x", "0x0000123456" },
                { 0x123456, "%-#9x", "0x123456 " },

                { -3, "%x", "fffffffd" },
                { -3, "%-8x", "fffffffd" },
                { -3, "%06x", "fffffffd" },
                { -3, "%#x", "0xfffffffd" },
                { -3, "%0#12x", "0x00fffffffd" },
                { -3, "%-#9x", "0xfffffffd" },

                { 0x7654321L, "%x", "7654321" },
                { 0x7654321L, "%-8x", "7654321 " },
                { 0x7654321L, "%06x", "7654321" },
                { 0x7654321L, "%#x", "0x7654321" },
                { 0x7654321L, "%0#12x", "0x0007654321" },
                { 0x7654321L, "%-#9x", "0x7654321" },

                { -1L, "%x", "ffffffffffffffff" },
                { -1L, "%-8x", "ffffffffffffffff" },
                { -1L, "%06x", "ffffffffffffffff" },
                { -1L, "%#x", "0xffffffffffffffff" },
                { -1L, "%0#12x", "0xffffffffffffffff" },
                { -1L, "%-#9x", "0xffffffffffffffff" },
        };

        final int input = 0;
        final int pattern = 1;
        final int output = 2;
        Formatter f;
        for (int i = 0; i < triple.length; i++) {
            f = new Formatter(Locale.FRANCE);
            f.format((String) triple[i][pattern],
                    triple[i][input]);
            assertEquals("triple[" + i + "]:" + triple[i][input] + ",pattern["
                    + i + "]:" + triple[i][pattern], triple[i][output], f
                    .toString());

            f = new Formatter(Locale.FRANCE);
            f.format((String) triple[i][pattern],
                    triple[i][input]);
            assertEquals("triple[" + i + "]:" + triple[i][input] + ",pattern["
                    + i + "]:" + triple[i][pattern], triple[i][output], f
                    .toString());
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for Date/Time
     * conversion
     * J2ObjC: Noticing some incompatibilities on Mac/iOS.
    public void test_formatLjava_lang_String$Ljava_lang_Object_DateTimeConversion() {
        Formatter f = null;
        Date now = new Date(1147327147578L);

        Calendar paris = Calendar.getInstance(TimeZone
                .getTimeZone("Europe/Paris"), Locale.FRANCE);
        paris.set(2006, 4, 8, 12, 0, 0);
        paris.set(Calendar.MILLISECOND, 453);
        Calendar china = Calendar.getInstance(
                TimeZone.getTimeZone("GMT-08:00"), Locale.CHINA);
        china.set(2006, 4, 8, 12, 0, 0);
        china.set(Calendar.MILLISECOND, 609);

        final Object[][] lowerCaseGermanTriple = {
                { 0L, 'a', "Do." },  //$NON-NLS-2$
                { Long.MAX_VALUE, 'a', "So." },  //$NON-NLS-2$
                { -1000L, 'a', "Do." },  //$NON-NLS-2$
                { new Date(1147327147578L), 'a', "Do." },  //$NON-NLS-2$
                { paris, 'a', "Mo." },  //$NON-NLS-2$
                { china, 'a', "Mo." },  //$NON-NLS-2$
                { 0L, 'b', "Jan" },  //$NON-NLS-2$
                { Long.MAX_VALUE, 'b', "Aug" },  //$NON-NLS-2$
                { -1000L, 'b', "Jan" },  //$NON-NLS-2$
                { new Date(1147327147578L), 'b', "Mai" },  //$NON-NLS-2$
                { paris, 'b', "Mai" },  //$NON-NLS-2$
                { china, 'b', "Mai" },  //$NON-NLS-2$
                { 0L, 'c', "Do. Jan 01 08:00:00 GMT+08:00 1970" },  //$NON-NLS-2$
                { Long.MAX_VALUE, 'c', "So. Aug 17 15:18:47 GMT+08:00 292278994" },  //$NON-NLS-2$
                { -1000L, 'c', "Do. Jan 01 07:59:59 GMT+08:00 1970" },  //$NON-NLS-2$
                { new Date(1147327147578L), 'c', "Do. Mai 11 13:59:07 GMT+08:00 2006" },  //$NON-NLS-2$
                { paris, 'c', "Mo. Mai 08 12:00:00 MESZ 2006" },  //$NON-NLS-2$
                { china, 'c', "Mo. Mai 08 12:00:00 GMT-08:00 2006" },  //$NON-NLS-2$
                { 0L, 'd', "01" },  //$NON-NLS-2$
                { Long.MAX_VALUE, 'd', "17" },  //$NON-NLS-2$
                { -1000L, 'd', "01" },  //$NON-NLS-2$
                { new Date(1147327147578L), 'd', "11" },  //$NON-NLS-2$
                { paris, 'd', "08" },  //$NON-NLS-2$
                { china, 'd', "08" },  //$NON-NLS-2$
                { 0L, 'e', "1" },  //$NON-NLS-2$
                { Long.MAX_VALUE, 'e', "17" },  //$NON-NLS-2$
                { -1000L, 'e', "1" },  //$NON-NLS-2$
                { new Date(1147327147578L), 'e', "11" },  //$NON-NLS-2$
                { paris, 'e', "8" },  //$NON-NLS-2$
                { china, 'e', "8" },  //$NON-NLS-2$
                { 0L, 'h', "Jan" },  //$NON-NLS-2$
                { Long.MAX_VALUE, 'h', "Aug" },  //$NON-NLS-2$
                { -1000L, 'h', "Jan" },  //$NON-NLS-2$
                { new Date(1147327147578L), 'h', "Mai" },  //$NON-NLS-2$
                { paris, 'h', "Mai" },  //$NON-NLS-2$
                { china, 'h', "Mai" },  //$NON-NLS-2$
                { 0L, 'j', "001" },  //$NON-NLS-2$
                { Long.MAX_VALUE, 'j', "229" },  //$NON-NLS-2$
                { -1000L, 'j', "001" },  //$NON-NLS-2$
                { new Date(1147327147578L), 'j', "131" },  //$NON-NLS-2$
                { paris, 'j', "128" },  //$NON-NLS-2$
                { china, 'j', "128" },  //$NON-NLS-2$
                { 0L, 'k', "8" },  //$NON-NLS-2$
                { Long.MAX_VALUE, 'k', "15" },  //$NON-NLS-2$
                { -1000L, 'k', "7" },  //$NON-NLS-2$
                { new Date(1147327147578L), 'k', "13" },  //$NON-NLS-2$
                { paris, 'k', "12" },  //$NON-NLS-2$
                { china, 'k', "12" },  //$NON-NLS-2$
                { 0L, 'l', "8" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'l', "3" }, //$NON-NLS-2$
                { -1000L, 'l', "7" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'l', "1" }, //$NON-NLS-2$
                { paris, 'l', "12" }, //$NON-NLS-2$
                { china, 'l', "12" }, //$NON-NLS-2$
                { 0L, 'm', "01" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'm', "08" }, //$NON-NLS-2$
                { -1000L, 'm', "01" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'm', "05" }, //$NON-NLS-2$
                { paris, 'm', "05" }, //$NON-NLS-2$
                { china, 'm', "05" }, //$NON-NLS-2$
                { 0L, 'p', "vorm." }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'p', "nachm." }, //$NON-NLS-2$
                { -1000L, 'p', "vorm." }, //$NON-NLS-2$
                { new Date(1147327147578L), 'p', "nachm." }, //$NON-NLS-2$
                { paris, 'p', "nachm." }, //$NON-NLS-2$
                { china, 'p', "nachm." }, //$NON-NLS-2$
                { 0L, 'r', "08:00:00 vorm." }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'r', "03:18:47 nachm." }, //$NON-NLS-2$
                { -1000L, 'r', "07:59:59 vorm." }, //$NON-NLS-2$
                { new Date(1147327147578L), 'r', "01:59:07 nachm." }, //$NON-NLS-2$
                { paris, 'r', "12:00:00 nachm." }, //$NON-NLS-2$
                { china, 'r', "12:00:00 nachm." }, //$NON-NLS-2$
                { 0L, 's', "0" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 's', "9223372036854775" }, //$NON-NLS-2$
                { -1000L, 's', "-1" }, //$NON-NLS-2$
                { new Date(1147327147578L), 's', "1147327147" }, //$NON-NLS-2$
                { paris, 's', "1147082400" }, //$NON-NLS-2$
                { china, 's', "1147118400" }, //$NON-NLS-2$
                { 0L, 'y', "70" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'y', "94" }, //$NON-NLS-2$
                { -1000L, 'y', "70" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'y', "06" }, //$NON-NLS-2$
                { paris, 'y', "06" }, //$NON-NLS-2$
                { china, 'y', "06" }, //$NON-NLS-2$
                { 0L, 'z', "+0800" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'z', "+0800" }, //$NON-NLS-2$
                { -1000L, 'z', "+0800" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'z', "+0800" }, //$NON-NLS-2$
                { paris, 'z', "+0100" }, //$NON-NLS-2$
                { china, 'z', "-0800" }, //$NON-NLS-2$

        };

        final Object[][] lowerCaseFranceTriple = {
                { 0L, 'a', "jeu." }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'a', "dim." }, //$NON-NLS-2$
                { -1000L, 'a', "jeu." }, //$NON-NLS-2$
                { new Date(1147327147578L), 'a', "jeu." }, //$NON-NLS-2$
                { paris, 'a', "lun." }, //$NON-NLS-2$
                { china, 'a', "lun." }, //$NON-NLS-2$
                { 0L, 'b', "janv." }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'b', "ao\u00fbt" }, //$NON-NLS-2$
                { -1000L, 'b', "janv." }, //$NON-NLS-2$
                { new Date(1147327147578L), 'b', "mai" }, //$NON-NLS-2$
                { paris, 'b', "mai" }, //$NON-NLS-2$
                { china, 'b', "mai" }, //$NON-NLS-2$
                { 0L, 'c', "jeu. janv. 01 08:00:00 UTC+08:00 1970" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'c', "dim. ao\u00fbt 17 15:18:47 UTC+08:00 292278994" }, //$NON-NLS-2$
                { -1000L, 'c', "jeu. janv. 01 07:59:59 UTC+08:00 1970" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'c', "jeu. mai 11 13:59:07 UTC+08:00 2006" }, //$NON-NLS-2$
                { paris, 'c', "lun. mai 08 12:00:00 HAEC 2006" }, //$NON-NLS-2$
                { china, 'c', "lun. mai 08 12:00:00 UTC-08:00 2006" }, //$NON-NLS-2$
                { 0L, 'd', "01" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'd', "17" }, //$NON-NLS-2$
                { -1000L, 'd', "01" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'd', "11" }, //$NON-NLS-2$
                { paris, 'd', "08" }, //$NON-NLS-2$
                { china, 'd', "08" }, //$NON-NLS-2$
                { 0L, 'e', "1" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'e', "17" }, //$NON-NLS-2$
                { -1000L, 'e', "1" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'e', "11" }, //$NON-NLS-2$
                { paris, 'e', "8" }, //$NON-NLS-2$
                { china, 'e', "8" }, //$NON-NLS-2$
                { 0L, 'h', "janv." }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'h', "ao\u00fbt" }, //$NON-NLS-2$
                { -1000L, 'h', "janv." }, //$NON-NLS-2$
                { new Date(1147327147578L), 'h', "mai" }, //$NON-NLS-2$
                { paris, 'h', "mai" }, //$NON-NLS-2$
                { china, 'h', "mai" }, //$NON-NLS-2$
                { 0L, 'j', "001" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'j', "229" }, //$NON-NLS-2$
                { -1000L, 'j', "001" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'j', "131" }, //$NON-NLS-2$
                { paris, 'j', "128" }, //$NON-NLS-2$
                { china, 'j', "128" }, //$NON-NLS-2$
                { 0L, 'k', "8" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'k', "15" }, //$NON-NLS-2$
                { -1000L, 'k', "7" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'k', "13" }, //$NON-NLS-2$
                { paris, 'k', "12" }, //$NON-NLS-2$
                { china, 'k', "12" }, //$NON-NLS-2$
                { 0L, 'l', "8" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'l', "3" }, //$NON-NLS-2$
                { -1000L, 'l', "7" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'l', "1" }, //$NON-NLS-2$
                { paris, 'l', "12" }, //$NON-NLS-2$
                { china, 'l', "12" }, //$NON-NLS-2$
                { 0L, 'm', "01" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'm', "08" }, //$NON-NLS-2$
                { -1000L, 'm', "01" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'm', "05" }, //$NON-NLS-2$
                { paris, 'm', "05" }, //$NON-NLS-2$
                { china, 'm', "05" }, //$NON-NLS-2$
                { 0L, 'p', "am" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'p', "pm" }, //$NON-NLS-2$
                { -1000L, 'p', "am" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'p', "pm" }, //$NON-NLS-2$
                { paris, 'p', "pm" }, //$NON-NLS-2$
                { china, 'p', "pm" }, //$NON-NLS-2$
                { 0L, 'r', "08:00:00 AM" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'r', "03:18:47 PM" }, //$NON-NLS-2$
                { -1000L, 'r', "07:59:59 AM" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'r', "01:59:07 PM" }, //$NON-NLS-2$
                { paris, 'r', "12:00:00 PM" }, //$NON-NLS-2$
                { china, 'r', "12:00:00 PM" }, //$NON-NLS-2$
                { 0L, 's', "0" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 's', "9223372036854775" }, //$NON-NLS-2$
                { -1000L, 's', "-1" }, //$NON-NLS-2$
                { new Date(1147327147578L), 's', "1147327147" }, //$NON-NLS-2$
                { paris, 's', "1147082400" }, //$NON-NLS-2$
                { china, 's', "1147118400" }, //$NON-NLS-2$
                { 0L, 'y', "70" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'y', "94" }, //$NON-NLS-2$
                { -1000L, 'y', "70" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'y', "06" }, //$NON-NLS-2$
                { paris, 'y', "06" }, //$NON-NLS-2$
                { china, 'y', "06" }, //$NON-NLS-2$
                { 0L, 'z', "+0800" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'z', "+0800" }, //$NON-NLS-2$
                { -1000L, 'z', "+0800" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'z', "+0800" }, //$NON-NLS-2$
                { paris, 'z', "+0100" }, //$NON-NLS-2$
                { china, 'z', "-0800" }, //$NON-NLS-2$

        };

        final Object[][] lowerCaseJapanTriple = {
                { 0L, 'a', "\u6728" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'a', "\u65e5" }, //$NON-NLS-2$
                { -1000L, 'a', "\u6728" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'a', "\u6728" }, //$NON-NLS-2$
                { paris, 'a', "\u6708" }, //$NON-NLS-2$
                { china, 'a', "\u6708" }, //$NON-NLS-2$
                { 0L, 'b', "1\u6708" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'b', "8\u6708" }, //$NON-NLS-2$
                { -1000L, 'b', "1\u6708" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'b', "5\u6708" }, //$NON-NLS-2$
                { paris, 'b', "5\u6708" }, //$NON-NLS-2$
                { china, 'b', "5\u6708" }, //$NON-NLS-2$
                { 0L, 'c', "\u6728 1\u6708 01 08:00:00 GMT+08:00 1970" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'c', "\u65e5 8\u6708 17 15:18:47 GMT+08:00 292278994" }, //$NON-NLS-2$
                { -1000L, 'c', "\u6728 1\u6708 01 07:59:59 GMT+08:00 1970" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'c', "\u6728 5\u6708 11 13:59:07 GMT+08:00 2006" }, //$NON-NLS-2$
                { paris, 'c', "\u6708 5\u6708 08 12:00:00 GMT+02:00 2006" }, //$NON-NLS-2$
                { china, 'c', "\u6708 5\u6708 08 12:00:00 GMT-08:00 2006" }, //$NON-NLS-2$
                { 0L, 'd', "01" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'd', "17" }, //$NON-NLS-2$
                { -1000L, 'd', "01" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'd', "11" }, //$NON-NLS-2$
                { paris, 'd', "08" }, //$NON-NLS-2$
                { china, 'd', "08" }, //$NON-NLS-2$
                { 0L, 'e', "1" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'e', "17" }, //$NON-NLS-2$
                { -1000L, 'e', "1" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'e', "11" }, //$NON-NLS-2$
                { paris, 'e', "8" }, //$NON-NLS-2$
                { china, 'e', "8" }, //$NON-NLS-2$
                { 0L, 'h', "1\u6708" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'h', "8\u6708" }, //$NON-NLS-2$
                { -1000L, 'h', "1\u6708" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'h', "5\u6708" }, //$NON-NLS-2$
                { paris, 'h', "5\u6708" }, //$NON-NLS-2$
                { china, 'h', "5\u6708" }, //$NON-NLS-2$
                { 0L, 'j', "001" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'j', "229" }, //$NON-NLS-2$
                { -1000L, 'j', "001" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'j', "131" }, //$NON-NLS-2$
                { paris, 'j', "128" }, //$NON-NLS-2$
                { china, 'j', "128" }, //$NON-NLS-2$
                { 0L, 'k', "8" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'k', "15" }, //$NON-NLS-2$
                { -1000L, 'k', "7" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'k', "13" }, //$NON-NLS-2$
                { paris, 'k', "12" }, //$NON-NLS-2$
                { china, 'k', "12" }, //$NON-NLS-2$
                { 0L, 'l', "8" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'l', "3" }, //$NON-NLS-2$
                { -1000L, 'l', "7" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'l', "1" }, //$NON-NLS-2$
                { paris, 'l', "12" }, //$NON-NLS-2$
                { china, 'l', "12" }, //$NON-NLS-2$
                { 0L, 'm', "01" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'm', "08" }, //$NON-NLS-2$
                { -1000L, 'm', "01" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'm', "05" }, //$NON-NLS-2$
                { paris, 'm', "05" }, //$NON-NLS-2$
                { china, 'm', "05" }, //$NON-NLS-2$
                { 0L, 'p', "\u5348\u524d" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'p', "\u5348\u5f8c" }, //$NON-NLS-2$
                { -1000L, 'p', "\u5348\u524d" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'p', "\u5348\u5f8c" }, //$NON-NLS-2$
                { paris, 'p', "\u5348\u5f8c" }, //$NON-NLS-2$
                { china, 'p', "\u5348\u5f8c" }, //$NON-NLS-2$
                { 0L, 'r', "08:00:00 \u5348\u524d" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'r', "03:18:47 \u5348\u5f8c" }, //$NON-NLS-2$
                { -1000L, 'r', "07:59:59 \u5348\u524d" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'r', "01:59:07 \u5348\u5f8c" }, //$NON-NLS-2$
                { paris, 'r', "12:00:00 \u5348\u5f8c" }, //$NON-NLS-2$
                { china, 'r', "12:00:00 \u5348\u5f8c" }, //$NON-NLS-2$
                { 0L, 's', "0" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 's', "9223372036854775" }, //$NON-NLS-2$
                { -1000L, 's', "-1" }, //$NON-NLS-2$
                { new Date(1147327147578L), 's', "1147327147" }, //$NON-NLS-2$
                { paris, 's', "1147082400" }, //$NON-NLS-2$
                { china, 's', "1147118400" }, //$NON-NLS-2$
                { 0L, 'y', "70" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'y', "94" }, //$NON-NLS-2$
                { -1000L, 'y', "70" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'y', "06" }, //$NON-NLS-2$
                { paris, 'y', "06" }, //$NON-NLS-2$
                { china, 'y', "06" }, //$NON-NLS-2$
                { 0L, 'z', "+0800" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'z', "+0800" }, //$NON-NLS-2$
                { -1000L, 'z', "+0800" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'z', "+0800" }, //$NON-NLS-2$
                { paris, 'z', "+0100" }, //$NON-NLS-2$
                { china, 'z', "-0800" }, //$NON-NLS-2$
        };

        final int input = 0;
        final int pattern = 1;
        final int output = 2;
        for (int i = 0; i < 90; i++) {
            // go through legal conversion
            String formatSpecifier = "%t" + lowerCaseGermanTriple[i][pattern]; //$NON-NLS-2$
            String formatSpecifierUpper = "%T" + lowerCaseGermanTriple[i][pattern]; //$NON-NLS-2$
            // test '%t'
            f = new Formatter(Locale.GERMAN);
            f.format(formatSpecifier, lowerCaseGermanTriple[i][input]);
            assertEquals("Format pattern: " + formatSpecifier //$NON-NLS-2$
                    + " Argument: " + lowerCaseGermanTriple[i][input], //$NON-NLS-2$
                    lowerCaseGermanTriple[i][output], f.toString());

            f = new Formatter(Locale.GERMAN);
            f.format(Locale.FRANCE, formatSpecifier, lowerCaseFranceTriple[i][input]);
            assertEquals("Format pattern: " + formatSpecifier //$NON-NLS-2$
                    + " Argument: " + lowerCaseFranceTriple[i][input], //$NON-NLS-2$
                    lowerCaseFranceTriple[i][output], f.toString());

            f = new Formatter(Locale.GERMAN);
            f.format(Locale.JAPAN, formatSpecifier, lowerCaseJapanTriple[i][input]);
            assertEquals("Format pattern: " + formatSpecifier //$NON-NLS-2$
                    + " Argument: " + lowerCaseJapanTriple[i][input], //$NON-NLS-2$
                    lowerCaseJapanTriple[i][output], f.toString());

            // test '%T'
            f = new Formatter(Locale.GERMAN);
            f.format(formatSpecifierUpper, lowerCaseGermanTriple[i][input]);
            assertEquals("Format pattern: " + formatSpecifierUpper //$NON-NLS-2$
                    + " Argument: " + lowerCaseGermanTriple[i][input], //$NON-NLS-2$
                    ((String) lowerCaseGermanTriple[i][output])
                            .toUpperCase(Locale.US), f.toString());

            f = new Formatter(Locale.GERMAN);
            f.format(Locale.FRANCE, formatSpecifierUpper, lowerCaseFranceTriple[i][input]);
            assertEquals("Format pattern: " + formatSpecifierUpper //$NON-NLS-2$
                    + " Argument: " + lowerCaseFranceTriple[i][input], //$NON-NLS-2$
                    ((String) lowerCaseFranceTriple[i][output])
                            .toUpperCase(Locale.US), f.toString());

            f = new Formatter(Locale.GERMAN);
            f.format(Locale.JAPAN, formatSpecifierUpper, lowerCaseJapanTriple[i][input]);
            assertEquals("Format pattern: " + formatSpecifierUpper //$NON-NLS-2$
                    + " Argument: " + lowerCaseJapanTriple[i][input], //$NON-NLS-2$
                    ((String) lowerCaseJapanTriple[i][output])
                            .toUpperCase(Locale.US), f.toString());
        }

        final Object[][] upperCaseGermanTriple = {
                { 0L, 'A', "Donnerstag" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'A', "Sonntag" }, //$NON-NLS-2$
                { -1000L, 'A', "Donnerstag" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'A', "Donnerstag" }, //$NON-NLS-2$
                { paris, 'A', "Montag" }, //$NON-NLS-2$
                { china, 'A', "Montag" }, //$NON-NLS-2$
                { 0L, 'B', "Januar" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'B', "August" }, //$NON-NLS-2$
                { -1000L, 'B', "Januar" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'B', "Mai" }, //$NON-NLS-2$
                { paris, 'B', "Mai" }, //$NON-NLS-2$
                { china, 'B', "Mai" }, //$NON-NLS-2$
                { 0L, 'C', "19" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'C', "2922789" }, //$NON-NLS-2$
                { -1000L, 'C', "19" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'C', "20" }, //$NON-NLS-2$
                { paris, 'C', "20" }, //$NON-NLS-2$
                { china, 'C', "20" }, //$NON-NLS-2$
                { 0L, 'D', "01/01/70" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'D', "08/17/94" }, //$NON-NLS-2$
                { -1000L, 'D', "01/01/70" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'D', "05/11/06" }, //$NON-NLS-2$
                { paris, 'D', "05/08/06" }, //$NON-NLS-2$
                { china, 'D', "05/08/06" }, //$NON-NLS-2$
                { 0L, 'F', "1970-01-01" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'F', "292278994-08-17" }, //$NON-NLS-2$
                { -1000L, 'F', "1970-01-01" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'F', "2006-05-11" }, //$NON-NLS-2$
                { paris, 'F', "2006-05-08" }, //$NON-NLS-2$
                { china, 'F', "2006-05-08" }, //$NON-NLS-2$
                { 0L, 'H', "08" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'H', "15" }, //$NON-NLS-2$
                { -1000L, 'H', "07" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'H', "13" }, //$NON-NLS-2$
                { paris, 'H', "12" }, //$NON-NLS-2$
                { china, 'H', "12" }, //$NON-NLS-2$
                { 0L, 'I', "08" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'I', "03" }, //$NON-NLS-2$
                { -1000L, 'I', "07" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'I', "01" }, //$NON-NLS-2$
                { paris, 'I', "12" }, //$NON-NLS-2$
                { china, 'I', "12" }, //$NON-NLS-2$
                { 0L, 'L', "000" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'L', "807" }, //$NON-NLS-2$
                { -1000L, 'L', "000" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'L', "578" }, //$NON-NLS-2$
                { paris, 'L', "453" }, //$NON-NLS-2$
                { china, 'L', "609" }, //$NON-NLS-2$
                { 0L, 'M', "00" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'M', "18" }, //$NON-NLS-2$
                { -1000L, 'M', "59" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'M', "59" }, //$NON-NLS-2$
                { paris, 'M', "00" }, //$NON-NLS-2$
                { china, 'M', "00" }, //$NON-NLS-2$
                { 0L, 'N', "000000000" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'N', "807000000" }, //$NON-NLS-2$
                { -1000L, 'N', "000000000" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'N', "578000000" }, //$NON-NLS-2$
                { paris, 'N', "609000000" }, //$NON-NLS-2$
                { china, 'N', "609000000" }, //$NON-NLS-2$
                { 0L, 'Q', "0" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'Q', "9223372036854775807" }, //$NON-NLS-2$
                { -1000L, 'Q', "-1000" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'Q', "1147327147578" }, //$NON-NLS-2$
                { paris, 'Q', "1147082400453" }, //$NON-NLS-2$
                { china, 'Q', "1147118400609" }, //$NON-NLS-2$
                { 0L, 'R', "08:00" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'R', "15:18" }, //$NON-NLS-2$
                { -1000L, 'R', "07:59" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'R', "13:59" }, //$NON-NLS-2$
                { paris, 'R', "12:00" }, //$NON-NLS-2$
                { china, 'R', "12:00" }, //$NON-NLS-2$
                { 0L, 'S', "00" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'S', "47" }, //$NON-NLS-2$
                { -1000L, 'S', "59" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'S', "07" }, //$NON-NLS-2$
                { paris, 'S', "00" }, //$NON-NLS-2$
                { china, 'S', "00" }, //$NON-NLS-2$
                { 0L, 'T', "08:00:00" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'T', "15:18:47" }, //$NON-NLS-2$
                { -1000L, 'T', "07:59:59" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'T', "13:59:07" }, //$NON-NLS-2$
                { paris, 'T', "12:00:00" }, //$NON-NLS-2$
                { china, 'T', "12:00:00" }, //$NON-NLS-2$
                { 0L, 'Y', "1970" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'Y', "292278994" }, //$NON-NLS-2$
                { -1000L, 'Y', "1970" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'Y', "2006" }, //$NON-NLS-2$
                { paris, 'Y', "2006" }, //$NON-NLS-2$
                { china, 'Y', "2006" }, //$NON-NLS-2$
                { 0L, 'Z', "CST" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'Z', "CST" }, //$NON-NLS-2$
                { -1000L, 'Z', "CST" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'Z', "CST" }, //$NON-NLS-2$
                { paris, 'Z', "CEST" }, //$NON-NLS-2$
                { china, 'Z', "GMT-08:00" }, //$NON-NLS-2$

        };

        final Object[][] upperCaseFranceTriple = {
                { 0L, 'A', "jeudi" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'A', "dimanche" }, //$NON-NLS-2$
                { -1000L, 'A', "jeudi" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'A', "jeudi" }, //$NON-NLS-2$
                { paris, 'A', "lundi" }, //$NON-NLS-2$
                { china, 'A', "lundi" }, //$NON-NLS-2$
                { 0L, 'B', "janvier" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'B', "ao\u00fbt" }, //$NON-NLS-2$
                { -1000L, 'B', "janvier" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'B', "mai" }, //$NON-NLS-2$
                { paris, 'B', "mai" }, //$NON-NLS-2$
                { china, 'B', "mai" }, //$NON-NLS-2$
                { 0L, 'C', "19" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'C', "2922789" }, //$NON-NLS-2$
                { -1000L, 'C', "19" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'C', "20" }, //$NON-NLS-2$
                { paris, 'C', "20" }, //$NON-NLS-2$
                { china, 'C', "20" }, //$NON-NLS-2$
                { 0L, 'D', "01/01/70" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'D', "08/17/94" }, //$NON-NLS-2$
                { -1000L, 'D', "01/01/70" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'D', "05/11/06" }, //$NON-NLS-2$
                { paris, 'D', "05/08/06" }, //$NON-NLS-2$
                { china, 'D', "05/08/06" }, //$NON-NLS-2$
                { 0L, 'F', "1970-01-01" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'F', "292278994-08-17" }, //$NON-NLS-2$
                { -1000L, 'F', "1970-01-01" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'F', "2006-05-11" }, //$NON-NLS-2$
                { paris, 'F', "2006-05-08" }, //$NON-NLS-2$
                { china, 'F', "2006-05-08" }, //$NON-NLS-2$
                { 0L, 'H', "08" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'H', "15" }, //$NON-NLS-2$
                { -1000L, 'H', "07" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'H', "13" }, //$NON-NLS-2$
                { paris, 'H', "12" }, //$NON-NLS-2$
                { china, 'H', "12" }, //$NON-NLS-2$
                { 0L, 'I', "08" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'I', "03" }, //$NON-NLS-2$
                { -1000L, 'I', "07" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'I', "01" }, //$NON-NLS-2$
                { paris, 'I', "12" }, //$NON-NLS-2$
                { china, 'I', "12" }, //$NON-NLS-2$
                { 0L, 'L', "000" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'L', "807" }, //$NON-NLS-2$
                { -1000L, 'L', "000" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'L', "578" }, //$NON-NLS-2$
                { paris, 'L', "453" }, //$NON-NLS-2$
                { china, 'L', "609" }, //$NON-NLS-2$
                { 0L, 'M', "00" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'M', "18" }, //$NON-NLS-2$
                { -1000L, 'M', "59" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'M', "59" }, //$NON-NLS-2$
                { paris, 'M', "00" }, //$NON-NLS-2$
                { china, 'M', "00" }, //$NON-NLS-2$
                { 0L, 'N', "000000000" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'N', "807000000" }, //$NON-NLS-2$
                { -1000L, 'N', "000000000" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'N', "578000000" }, //$NON-NLS-2$
                { paris, 'N', "453000000" }, //$NON-NLS-2$
                { china, 'N', "468000000" }, //$NON-NLS-2$
                { 0L, 'Q', "0" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'Q', "9223372036854775807" }, //$NON-NLS-2$
                { -1000L, 'Q', "-1000" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'Q', "1147327147578" }, //$NON-NLS-2$
                { paris, 'Q', "1147082400453" }, //$NON-NLS-2$
                { china, 'Q', "1147118400609" }, //$NON-NLS-2$
                { 0L, 'R', "08:00" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'R', "15:18" }, //$NON-NLS-2$
                { -1000L, 'R', "07:59" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'R', "13:59" }, //$NON-NLS-2$
                { paris, 'R', "12:00" }, //$NON-NLS-2$
                { china, 'R', "12:00" }, //$NON-NLS-2$
                { 0L, 'S', "00" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'S', "47" }, //$NON-NLS-2$
                { -1000L, 'S', "59" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'S', "07" }, //$NON-NLS-2$
                { paris, 'S', "00" }, //$NON-NLS-2$
                { china, 'S', "00" }, //$NON-NLS-2$
                { 0L, 'T', "08:00:00" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'T', "15:18:47" }, //$NON-NLS-2$
                { -1000L, 'T', "07:59:59" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'T', "13:59:07" }, //$NON-NLS-2$
                { paris, 'T', "12:00:00" }, //$NON-NLS-2$
                { china, 'T', "12:00:00" }, //$NON-NLS-2$
                { 0L, 'Y', "1970" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'Y', "292278994" }, //$NON-NLS-2$
                { -1000L, 'Y', "1970" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'Y', "2006" }, //$NON-NLS-2$
                { paris, 'Y', "2006" }, //$NON-NLS-2$
                { china, 'Y', "2006" }, //$NON-NLS-2$
                { 0L, 'Z', "CST" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'Z', "CST" }, //$NON-NLS-2$
                { -1000L, 'Z', "CST" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'Z', "CST" }, //$NON-NLS-2$
                { paris, 'Z', "CEST" }, //$NON-NLS-2$
                { china, 'Z', "GMT-08:00" }, //$NON-NLS-2$

        };

        final Object[][] upperCaseJapanTriple = {
                { 0L, 'A', "\u6728\u66dc\u65e5" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'A', "\u65e5\u66dc\u65e5" }, //$NON-NLS-2$
                { -1000L, 'A', "\u6728\u66dc\u65e5" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'A', "\u6728\u66dc\u65e5" }, //$NON-NLS-2$
                { paris, 'A', "\u6708\u66dc\u65e5" }, //$NON-NLS-2$
                { china, 'A', "\u6708\u66dc\u65e5" }, //$NON-NLS-2$
                { 0L, 'B', "1\u6708" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'B', "8\u6708" }, //$NON-NLS-2$
                { -1000L, 'B', "1\u6708" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'B', "5\u6708" }, //$NON-NLS-2$
                { paris, 'B', "5\u6708" }, //$NON-NLS-2$
                { china, 'B', "5\u6708" }, //$NON-NLS-2$
                { 0L, 'C', "19" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'C', "2922789" }, //$NON-NLS-2$
                { -1000L, 'C', "19" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'C', "20" }, //$NON-NLS-2$
                { paris, 'C', "20" }, //$NON-NLS-2$
                { china, 'C', "20" }, //$NON-NLS-2$
                { 0L, 'D', "01/01/70" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'D', "08/17/94" }, //$NON-NLS-2$
                { -1000L, 'D', "01/01/70" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'D', "05/11/06" }, //$NON-NLS-2$
                { paris, 'D', "05/08/06" }, //$NON-NLS-2$
                { china, 'D', "05/08/06" }, //$NON-NLS-2$
                { 0L, 'F', "1970-01-01" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'F', "292278994-08-17" }, //$NON-NLS-2$
                { -1000L, 'F', "1970-01-01" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'F', "2006-05-11" }, //$NON-NLS-2$
                { paris, 'F', "2006-05-08" }, //$NON-NLS-2$
                { china, 'F', "2006-05-08" }, //$NON-NLS-2$
                { 0L, 'H', "08" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'H', "15" }, //$NON-NLS-2$
                { -1000L, 'H', "07" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'H', "13" }, //$NON-NLS-2$
                { paris, 'H', "12" }, //$NON-NLS-2$
                { china, 'H', "12" }, //$NON-NLS-2$
                { 0L, 'I', "08" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'I', "03" }, //$NON-NLS-2$
                { -1000L, 'I', "07" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'I', "01" }, //$NON-NLS-2$
                { paris, 'I', "12" }, //$NON-NLS-2$
                { china, 'I', "12" }, //$NON-NLS-2$
                { 0L, 'L', "000" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'L', "807" }, //$NON-NLS-2$
                { -1000L, 'L', "000" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'L', "578" }, //$NON-NLS-2$
                { paris, 'L', "453" }, //$NON-NLS-2$
                { china, 'L', "609" }, //$NON-NLS-2$
                { 0L, 'M', "00" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'M', "18" }, //$NON-NLS-2$
                { -1000L, 'M', "59" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'M', "59" }, //$NON-NLS-2$
                { paris, 'M', "00" }, //$NON-NLS-2$
                { china, 'M', "00" }, //$NON-NLS-2$
                { 0L, 'N', "000000000" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'N', "807000000" }, //$NON-NLS-2$
                { -1000L, 'N', "000000000" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'N', "578000000" }, //$NON-NLS-2$
                { paris, 'N', "453000000" }, //$NON-NLS-2$
                { china, 'N', "468000000" }, //$NON-NLS-2$
                { 0L, 'Q', "0" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'Q', "9223372036854775807" }, //$NON-NLS-2$
                { -1000L, 'Q', "-1000" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'Q', "1147327147578" }, //$NON-NLS-2$
                { paris, 'Q', "1147082400453" }, //$NON-NLS-2$
                { china, 'Q', "1147118400609" }, //$NON-NLS-2$
                { 0L, 'R', "08:00" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'R', "15:18" }, //$NON-NLS-2$
                { -1000L, 'R', "07:59" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'R', "13:59" }, //$NON-NLS-2$
                { paris, 'R', "12:00" }, //$NON-NLS-2$
                { china, 'R', "12:00" }, //$NON-NLS-2$
                { 0L, 'S', "00" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'S', "47" }, //$NON-NLS-2$
                { -1000L, 'S', "59" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'S', "07" }, //$NON-NLS-2$
                { paris, 'S', "00" }, //$NON-NLS-2$
                { china, 'S', "00" }, //$NON-NLS-2$
                { 0L, 'T', "08:00:00" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'T', "15:18:47" }, //$NON-NLS-2$
                { -1000L, 'T', "07:59:59" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'T', "13:59:07" }, //$NON-NLS-2$
                { paris, 'T', "12:00:00" }, //$NON-NLS-2$
                { china, 'T', "12:00:00" }, //$NON-NLS-2$
                { 0L, 'Y', "1970" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'Y', "292278994" }, //$NON-NLS-2$
                { -1000L, 'Y', "1970" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'Y', "2006" }, //$NON-NLS-2$
                { paris, 'Y', "2006" }, //$NON-NLS-2$
                { china, 'Y', "2006" }, //$NON-NLS-2$
                { 0L, 'Z', "CST" }, //$NON-NLS-2$
                { Long.MAX_VALUE, 'Z', "CST" }, //$NON-NLS-2$
                { -1000L, 'Z', "CST" }, //$NON-NLS-2$
                { new Date(1147327147578L), 'Z', "CST" }, //$NON-NLS-2$
                { paris, 'Z', "CEST" }, //$NON-NLS-2$
                { china, 'Z', "GMT-08:00" }, //$NON-NLS-2$
        };


        for (int i = 0; i < 90; i++) {
            String formatSpecifier = "%t" + upperCaseGermanTriple[i][pattern]; //$NON-NLS-2$
            String formatSpecifierUpper = "%T" + upperCaseGermanTriple[i][pattern]; //$NON-NLS-2$
            if ((Character) upperCaseGermanTriple[i][pattern] == 'N') {
                // result can't be predicted on RI, so skip this test
                continue;
            }
            // test '%t'
            f = new Formatter(Locale.JAPAN);
            f.format(formatSpecifier, upperCaseJapanTriple[i][input]);
            assertEquals("Format pattern: " + formatSpecifier //$NON-NLS-2$
                    + " Argument: " + upperCaseJapanTriple[i][input], //$NON-NLS-2$
                    upperCaseJapanTriple[i][output], f.toString());

            f = new Formatter(Locale.JAPAN);
            f.format(Locale.GERMAN, formatSpecifier, upperCaseGermanTriple[i][input]);
            assertEquals("Format pattern: " + formatSpecifier //$NON-NLS-2$
                    + " Argument: " + upperCaseGermanTriple[i][input], //$NON-NLS-2$
                    upperCaseGermanTriple[i][output], f.toString());

            f = new Formatter(Locale.JAPAN);
            f.format(Locale.FRANCE, formatSpecifier, upperCaseFranceTriple[i][input]);
            assertEquals("Format pattern: " + formatSpecifier //$NON-NLS-2$
                    + " Argument: " + upperCaseFranceTriple[i][input], //$NON-NLS-2$
                    upperCaseFranceTriple[i][output], f.toString());

            // test '%T'
            f = new Formatter(Locale.GERMAN);
            f.format(formatSpecifierUpper, upperCaseGermanTriple[i][input]);
            assertEquals("Format pattern: " + formatSpecifierUpper //$NON-NLS-2$
                    + " Argument: " + upperCaseGermanTriple[i][input], //$NON-NLS-2$
                    ((String) upperCaseGermanTriple[i][output])
                            .toUpperCase(Locale.US), f.toString());

            f = new Formatter(Locale.GERMAN);
            f.format(Locale.JAPAN, formatSpecifierUpper, upperCaseJapanTriple[i][input]);
            assertEquals("Format pattern: " + formatSpecifierUpper //$NON-NLS-2$
                    + " Argument: " + upperCaseJapanTriple[i][input], //$NON-NLS-2$
                    ((String) upperCaseJapanTriple[i][output])
                            .toUpperCase(Locale.US), f.toString());

            f = new Formatter(Locale.GERMAN);
            f.format(Locale.FRANCE, formatSpecifierUpper, upperCaseFranceTriple[i][input]);
            assertEquals("Format pattern: " + formatSpecifierUpper //$NON-NLS-2$
                    + " Argument: " + upperCaseFranceTriple[i][input], //$NON-NLS-2$
                    ((String) upperCaseFranceTriple[i][output])
                            .toUpperCase(Locale.US), f.toString());
        }

        f = new Formatter(Locale.US);
        f.format("%-10ta", now); //$NON-NLS-2$
        assertEquals("Thu       ", f.toString()); //$NON-NLS-2$

        f = new Formatter(Locale.US);
        f.format("%10000000000000000000000000000000001ta", now); //$NON-NLS-2$
        assertEquals("Thu", f.toString().trim()); //$NON-NLS-2$
    }*/

    /**
     * java.util.Formatter#format(String, Object...) for null argment for
     * Byte/Short/Integer/Long/BigInteger conversion
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_ByteShortIntegerLongNullConversion() {

        Formatter f = new Formatter(Locale.FRANCE);
        f.format("%d%<o%<x%<5X", (Integer) null);
        assertEquals("nullnullnull NULL", f.toString());

        f = new Formatter(Locale.GERMAN);
        f.format("%d%<#03o %<0#4x%<6X", (Long) null);
        assertEquals("nullnull null  NULL", f.toString());

        f = new Formatter(Locale.GERMAN);
        f.format("%(+,07d%<o %<x%<6X", (Byte) null);
        assertEquals("   nullnull null  NULL", f.toString());

        f = new Formatter(Locale.ITALY);
        f.format("%(+,07d%<o %<x%<0#6X", (Short) null);
        assertEquals("   nullnull null  NULL", f.toString());

        f = new Formatter(Locale.GERMAN);
        f.format("%(+,-7d%<( o%<+(x %<( 06X", (BigInteger) null);
        assertEquals("null   nullnull   NULL", f.toString());
    }

    /**
     * java.util.Formatter#format(String, Object...) for legal
     * BigInteger conversion type 'd'
     */
    public void test_formatLjava_lang_String$LBigInteger() {
        final Object[][] tripleD = {
                { new BigInteger("123456789012345678901234567890"), "%d", "123456789012345678901234567890" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%10d", "123456789012345678901234567890" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%-1d", "123456789012345678901234567890" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%+d", "+123456789012345678901234567890" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "% d", " 123456789012345678901234567890" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%,d", "123.456.789.012.345.678.901.234.567.890" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%(d", "123456789012345678901234567890" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%08d", "123456789012345678901234567890" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%-+,(11d", "+123.456.789.012.345.678.901.234.567.890" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%0 ,(11d", " 123.456.789.012.345.678.901.234.567.890" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%d", "-9876543210987654321098765432100000" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%10d", "-9876543210987654321098765432100000" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%-1d", "-9876543210987654321098765432100000" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%+d", "-9876543210987654321098765432100000" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "% d", "-9876543210987654321098765432100000" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%,d", "-9.876.543.210.987.654.321.098.765.432.100.000" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%(d", "(9876543210987654321098765432100000)" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%08d", "-9876543210987654321098765432100000" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%-+,(11d", "(9.876.543.210.987.654.321.098.765.432.100.000)" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%0 ,(11d", "(9.876.543.210.987.654.321.098.765.432.100.000)" }, //$NON-NLS-2$
        };

        final int input = 0;
        final int pattern = 1;
        final int output = 2;
        Formatter f;
        for (int i = 0; i < tripleD.length; i++) {
            f = new Formatter(Locale.GERMAN);
            f.format((String) tripleD[i][pattern],
                    tripleD[i][input]);
            assertEquals("triple[" + i + "]:" + tripleD[i][input] + ",pattern["
                    + i + "]:" + tripleD[i][pattern], tripleD[i][output], f
                    .toString());

        }

        final Object[][] tripleO = {
                { new BigInteger("123456789012345678901234567890"), "%o", "143564417755415637016711617605322" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%-6o", "143564417755415637016711617605322" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%08o", "143564417755415637016711617605322" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%#o", "0143564417755415637016711617605322" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%0#11o", "0143564417755415637016711617605322" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%-#9o", "0143564417755415637016711617605322" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%o", "-36336340043453651353467270113157312240" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%-6o", "-36336340043453651353467270113157312240" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%08o", "-36336340043453651353467270113157312240" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%#o", "-036336340043453651353467270113157312240" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%0#11o", "-036336340043453651353467270113157312240" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%-#9o", "-036336340043453651353467270113157312240" }, //$NON-NLS-2$
        };
        for (int i = 0; i < tripleO.length; i++) {
            f = new Formatter(Locale.ITALY);
            f.format((String) tripleO[i][pattern],
                    tripleO[i][input]);
            assertEquals("triple[" + i + "]:" + tripleO[i][input] + ",pattern["
                    + i + "]:" + tripleO[i][pattern], tripleO[i][output], f
                    .toString());

        }

        final Object[][] tripleX = {
                { new BigInteger("123456789012345678901234567890"), "%x", "18ee90ff6c373e0ee4e3f0ad2" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%-8x", "18ee90ff6c373e0ee4e3f0ad2" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%06x", "18ee90ff6c373e0ee4e3f0ad2" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%#x", "0x18ee90ff6c373e0ee4e3f0ad2" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%0#12x", "0x18ee90ff6c373e0ee4e3f0ad2" }, //$NON-NLS-2$
                { new BigInteger("123456789012345678901234567890"), "%-#9x", "0x18ee90ff6c373e0ee4e3f0ad2" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%x", "-1e6f380472bd4bae6eb8259bd94a0" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%-8x", "-1e6f380472bd4bae6eb8259bd94a0" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%06x", "-1e6f380472bd4bae6eb8259bd94a0" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%#x", "-0x1e6f380472bd4bae6eb8259bd94a0" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%0#12x", "-0x1e6f380472bd4bae6eb8259bd94a0" }, //$NON-NLS-2$
                { new BigInteger("-9876543210987654321098765432100000"), "%-#9x", "-0x1e6f380472bd4bae6eb8259bd94a0" }, //$NON-NLS-2$
        };

        for (int i = 0; i < tripleX.length; i++) {
            f = new Formatter(Locale.FRANCE);
            f.format((String) tripleX[i][pattern],
                    tripleX[i][input]);
            assertEquals("triple[" + i + "]:" + tripleX[i][input] + ",pattern["
                    + i + "]:" + tripleX[i][pattern], tripleX[i][output], f
                    .toString());

        }

        f = new Formatter(Locale.GERMAN);
        f.format("%(+,-7d%<( o%<+(x %<( 06X", (BigInteger) null);
        assertEquals("null   nullnull   NULL", f.toString());
    }

    /**
     * java.util.Formatter#format(String, Object...) for padding of
     * BigInteger conversion
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_BigIntegerPaddingConversion() {
        Formatter f = null;

        BigInteger bigInt = new BigInteger("123456789012345678901234567890");
        f = new Formatter(Locale.GERMAN);
        f.format("%32d", bigInt);
        assertEquals("  123456789012345678901234567890", f.toString());

        f = new Formatter(Locale.GERMAN);
        f.format("%+32x", bigInt);
        assertEquals("      +18ee90ff6c373e0ee4e3f0ad2", f.toString());

        f = new Formatter(Locale.GERMAN);
        f.format("% 32o", bigInt);
        assertEquals(" 143564417755415637016711617605322", f.toString());

        BigInteger negBigInt = new BigInteger(
                "-1234567890123456789012345678901234567890");
        f = new Formatter(Locale.GERMAN);
        f.format("%( 040X", negBigInt);
        assertEquals("(000003A0C92075C0DBF3B8ACBC5F96CE3F0AD2)", f.toString());

        f = new Formatter(Locale.GERMAN);
        f.format("%+(045d", negBigInt);
        assertEquals("(0001234567890123456789012345678901234567890)", f
                .toString());

        f = new Formatter(Locale.GERMAN);
        f.format("%+,-(60d", negBigInt);
        assertEquals(
                "(1.234.567.890.123.456.789.012.345.678.901.234.567.890)     ",
                f.toString());
    }

    /**
     * java.util.Formatter#format(String, Object...) for BigInteger
     * conversion exception
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_BigIntegerConversionException() {
        Formatter f = null;

        final String[] flagsConversionMismatches = { "%#d", "%,o", "%,x", "%,X" };
        for (int i = 0; i < flagsConversionMismatches.length; i++) {
            try {
                f = new Formatter(Locale.CHINA);
                f.format(flagsConversionMismatches[i], new BigInteger("1"));
                fail("should throw FormatFlagsConversionMismatchException");
            } catch (FormatFlagsConversionMismatchException e) {
                // expected
            }
        }

        final String[] missingFormatWidths = { "%-0d", "%0d", "%-d", "%-0o",
                "%0o", "%-o", "%-0x", "%0x", "%-x", "%-0X", "%0X", "%-X" };
        for (int i = 0; i < missingFormatWidths.length; i++) {
            try {
                f = new Formatter(Locale.KOREA);
                f.format(missingFormatWidths[i], new BigInteger("1"));
                fail("should throw MissingFormatWidthException");
            } catch (MissingFormatWidthException e) {
                // expected
            }
        }

        final String[] illFlags = { "%+ d", "%-08d", "%+ o", "%-08o", "%+ x",
                "%-08x", "%+ X", "%-08X" };
        for (int i = 0; i < illFlags.length; i++) {
            try {
                f = new Formatter(Locale.CANADA);
                f.format(illFlags[i], new BigInteger("1"));
                fail("should throw IllegalFormatFlagsException");
            } catch (IllegalFormatFlagsException e) {
                // expected
            }
        }

        final String[] precisionExceptions = { "%.4d", "%2.5o", "%8.6x",
                "%11.17X" };
        for (int i = 0; i < precisionExceptions.length; i++) {
            try {
                f = new Formatter(Locale.US);
                f.format(precisionExceptions[i], new BigInteger("1"));
                fail("should throw IllegalFormatPrecisionException");
            } catch (IllegalFormatPrecisionException e) {
                // expected
            }
        }

        f = new Formatter(Locale.US);
        try {
            f.format("%D", new BigInteger("1"));
            fail("should throw UnknownFormatConversionException");
        } catch (UnknownFormatConversionException e) {
            // expected
        }

        f = new Formatter(Locale.US);
        try {
            f.format("%O", new BigInteger("1"));
            fail("should throw UnknownFormatConversionException");
        } catch (UnknownFormatConversionException e) {
            // expected
        }

        try {
            f = new Formatter();
            f.format("%010000000000000000000000000000000001d", new BigInteger(
                    "1"));
            fail("should throw MissingFormatWidthException");
        } catch (MissingFormatWidthException e) {
            // expected
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for BigInteger
     * exception throwing order
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_BigIntegerExceptionOrder() {
        Formatter f = null;
        BigInteger big = new BigInteger("100");

        /*
         * Order summary: UnknownFormatConversionException >
         * MissingFormatWidthException > IllegalFormatFlagsException >
         * IllegalFormatPrecisionException > IllegalFormatConversionException >
         * FormatFlagsConversionMismatchException
         *
         */
        f = new Formatter(Locale.US);
        try {
            f.format("%(o", false);
            fail();
        } catch (FormatFlagsConversionMismatchException expected) {
        } catch (IllegalFormatConversionException expected) {
        }

        try {
            f.format("%.4o", false);
            fail();
        } catch (IllegalFormatPrecisionException expected) {
        } catch (IllegalFormatConversionException expected) {
        }

        try {
            f.format("%+ .4o", big);
            fail();
        } catch (IllegalFormatPrecisionException expected) {
        } catch (IllegalFormatFlagsException expected) {
        }

        try {
            f.format("%+ -o", big);
            fail();
        } catch (MissingFormatWidthException expected) {
        } catch (IllegalFormatFlagsException expected) {
        }

        try {
            f.format("%-O", big);
            fail();
        } catch (MissingFormatWidthException expected) {
        } catch (UnknownFormatConversionException expected) {
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for Float/Double
     * conversion type 'e' and 'E'
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_FloatConversionE() {
        Formatter f = null;
        final Object[][] tripleE = {
                { 0f, "%e", "0.000000e+00" },
                { 0f, "%#.0e", "0.e+00" },
                { 0f, "%#- (9.8e", " 0.00000000e+00" },
                { 0f, "%#+0(8.4e", "+0.0000e+00" },
                { 0f, "%-+(1.6e", "+0.000000e+00" },
                { 0f, "% 0(12e", " 0.000000e+00" },

                { 101f, "%e", "1.010000e+02" },
                { 101f, "%#.0e", "1.e+02" },
                { 101f, "%#- (9.8e", " 1.01000000e+02" },
                { 101f, "%#+0(8.4e", "+1.0100e+02" },
                { 101f, "%-+(1.6e", "+1.010000e+02" },
                { 101f, "% 0(12e", " 1.010000e+02" },

                { 1.f, "%e", "1.000000e+00" },
                { 1.f, "%#.0e", "1.e+00" },
                { 1.f, "%#- (9.8e", " 1.00000000e+00" },
                { 1.f, "%#+0(8.4e", "+1.0000e+00" },
                { 1.f, "%-+(1.6e", "+1.000000e+00" },
                { 1.f, "% 0(12e", " 1.000000e+00" },

                { -98f, "%e", "-9.800000e+01" },
                { -98f, "%#.0e", "-1.e+02" },
                { -98f, "%#- (9.8e", "(9.80000000e+01)" },
                { -98f, "%#+0(8.4e", "(9.8000e+01)" },
                { -98f, "%-+(1.6e", "(9.800000e+01)" },
                { -98f, "% 0(12e", "(9.800000e+01)" },

                { 1.23f, "%e", "1.230000e+00" },
                { 1.23f, "%#.0e", "1.e+00" },
                { 1.23f, "%#- (9.8e", " 1.23000002e+00" },
                { 1.23f, "%#+0(8.4e", "+1.2300e+00" },
                { 1.23f, "%-+(1.6e", "+1.230000e+00" },
                { 1.23f, "% 0(12e", " 1.230000e+00" },

                { 34.1234567f, "%e", "3.412346e+01" },
                { 34.1234567f, "%#.0e", "3.e+01" },
                { 34.1234567f, "%#- (9.8e", " 3.41234550e+01" },
                { 34.1234567f, "%#+0(8.4e", "+3.4123e+01" },
                { 34.1234567f, "%-+(1.6e", "+3.412346e+01" },
                { 34.1234567f, "% 0(12e", " 3.412346e+01" },

                { -.12345f, "%e", "-1.234500e-01" },
                { -.12345f, "%#.0e", "-1.e-01" },
                { -.12345f, "%#- (9.8e", "(1.23450004e-01)" },
                { -.12345f, "%#+0(8.4e", "(1.2345e-01)" },
                { -.12345f, "%-+(1.6e", "(1.234500e-01)" },
                { -.12345f, "% 0(12e", "(1.234500e-01)" },

                { -9876.1234567f, "%e", "-9.876123e+03" },
                { -9876.1234567f, "%#.0e", "-1.e+04" },
                { -9876.1234567f, "%#- (9.8e", "(9.87612305e+03)" },
                { -9876.1234567f, "%#+0(8.4e", "(9.8761e+03)" },
                { -9876.1234567f, "%-+(1.6e", "(9.876123e+03)" },
                { -9876.1234567f, "% 0(12e", "(9.876123e+03)" },

                { Float.MAX_VALUE, "%e", "3.402823e+38" },
                { Float.MAX_VALUE, "%#.0e", "3.e+38" },
                { Float.MAX_VALUE, "%#- (9.8e", " 3.40282347e+38" },
                { Float.MAX_VALUE, "%#+0(8.4e", "+3.4028e+38" },
                { Float.MAX_VALUE, "%-+(1.6e", "+3.402823e+38" },
                { Float.MAX_VALUE, "% 0(12e", " 3.402823e+38" },

                { Float.MIN_VALUE, "%e", "1.401298e-45" },
                { Float.MIN_VALUE, "%#.0e", "1.e-45" },
                { Float.MIN_VALUE, "%#- (9.8e", " 1.40129846e-45" },
                { Float.MIN_VALUE, "%#+0(8.4e", "+1.4013e-45" },
                { Float.MIN_VALUE, "%-+(1.6e", "+1.401298e-45" },
                { Float.MIN_VALUE, "% 0(12e", " 1.401298e-45" },

                { Float.NaN, "%e", "NaN" },
                { Float.NaN, "%#.0e", "NaN" },
                { Float.NaN, "%#- (9.8e", "NaN      " },
                { Float.NaN, "%#+0(8.4e", "     NaN" },
                { Float.NaN, "%-+(1.6e", "NaN" },
                { Float.NaN, "% 0(12e", "         NaN" },


                { Float.NEGATIVE_INFINITY, "%e", "-Infinity" },
                { Float.NEGATIVE_INFINITY, "%#.0e", "-Infinity" },
                { Float.NEGATIVE_INFINITY, "%#- (9.8e", "(Infinity)" },
                { Float.NEGATIVE_INFINITY, "%#+0(8.4e", "(Infinity)" },
                { Float.NEGATIVE_INFINITY, "%-+(1.6e", "(Infinity)" },
                { Float.NEGATIVE_INFINITY, "% 0(12e", "  (Infinity)" },

                { Float.NEGATIVE_INFINITY, "%e", "-Infinity" },
                { Float.NEGATIVE_INFINITY, "%#.0e", "-Infinity" },
                { Float.NEGATIVE_INFINITY, "%#- (9.8e", "(Infinity)" },
                { Float.NEGATIVE_INFINITY, "%#+0(8.4e", "(Infinity)" },
                { Float.NEGATIVE_INFINITY, "%-+(1.6e", "(Infinity)" },
                { Float.NEGATIVE_INFINITY, "% 0(12e", "  (Infinity)" },

                { 0d, "%e", "0.000000e+00" },
                { 0d, "%#.0e", "0.e+00" },
                { 0d, "%#- (9.8e", " 0.00000000e+00" },
                { 0d, "%#+0(8.4e", "+0.0000e+00" },
                { 0d, "%-+(1.6e", "+0.000000e+00" },
                { 0d, "% 0(12e", " 0.000000e+00" },

                { 1d, "%e", "1.000000e+00" },
                { 1d, "%#.0e", "1.e+00" },
                { 1d, "%#- (9.8e", " 1.00000000e+00" },
                { 1d, "%#+0(8.4e", "+1.0000e+00" },
                { 1d, "%-+(1.6e", "+1.000000e+00" },
                { 1d, "% 0(12e", " 1.000000e+00" },

                { -1d, "%e", "-1.000000e+00" },
                { -1d, "%#.0e", "-1.e+00" },
                { -1d, "%#- (9.8e", "(1.00000000e+00)" },
                { -1d, "%#+0(8.4e", "(1.0000e+00)" },
                { -1d, "%-+(1.6e", "(1.000000e+00)" },
                { -1d, "% 0(12e", "(1.000000e+00)" },


                { .00000001d, "%e", "1.000000e-08" },
                { .00000001d, "%#.0e", "1.e-08" },
                { .00000001d, "%#- (9.8e", " 1.00000000e-08" },
                { .00000001d, "%#+0(8.4e", "+1.0000e-08" },
                { .00000001d, "%-+(1.6e", "+1.000000e-08" },
                { .00000001d, "% 0(12e", " 1.000000e-08" },

                { 9122.10d, "%e", "9.122100e+03" },
                { 9122.10d, "%#.0e", "9.e+03" },
                { 9122.10d, "%#- (9.8e", " 9.12210000e+03" },
                { 9122.10d, "%#+0(8.4e", "+9.1221e+03" },
                { 9122.10d, "%-+(1.6e", "+9.122100e+03" },
                { 9122.10d, "% 0(12e", " 9.122100e+03" },

                { 0.1d, "%e", "1.000000e-01" },
                { 0.1d, "%#.0e", "1.e-01" },
                { 0.1d, "%#- (9.8e", " 1.00000000e-01" },
                { 0.1d, "%#+0(8.4e", "+1.0000e-01" },
                { 0.1d, "%-+(1.6e", "+1.000000e-01" },
                { 0.1d, "% 0(12e", " 1.000000e-01" },

                { -2.d, "%e", "-2.000000e+00" },
                { -2.d, "%#.0e", "-2.e+00" },
                { -2.d, "%#- (9.8e", "(2.00000000e+00)" },
                { -2.d, "%#+0(8.4e", "(2.0000e+00)" },
                { -2.d, "%-+(1.6e", "(2.000000e+00)" },
                { -2.d, "% 0(12e", "(2.000000e+00)" },

                { -.39d, "%e", "-3.900000e-01" },
                { -.39d, "%#.0e", "-4.e-01" },
                { -.39d, "%#- (9.8e", "(3.90000000e-01)" },
                { -.39d, "%#+0(8.4e", "(3.9000e-01)" },
                { -.39d, "%-+(1.6e", "(3.900000e-01)" },
                { -.39d, "% 0(12e", "(3.900000e-01)" },

                { -1234567890.012345678d, "%e", "-1.234568e+09" },
                { -1234567890.012345678d, "%#.0e", "-1.e+09" },
                { -1234567890.012345678d, "%#- (9.8e", "(1.23456789e+09)" },
                { -1234567890.012345678d, "%#+0(8.4e", "(1.2346e+09)" },
                { -1234567890.012345678d, "%-+(1.6e", "(1.234568e+09)" },
                { -1234567890.012345678d, "% 0(12e", "(1.234568e+09)" },

                { Double.MAX_VALUE, "%e", "1.797693e+308" },
                { Double.MAX_VALUE, "%#.0e", "2.e+308" },
                { Double.MAX_VALUE, "%#- (9.8e", " 1.79769313e+308" },
                { Double.MAX_VALUE, "%#+0(8.4e", "+1.7977e+308" },
                { Double.MAX_VALUE, "%-+(1.6e", "+1.797693e+308" },
                { Double.MAX_VALUE, "% 0(12e", " 1.797693e+308" },

                { Double.MIN_VALUE, "%e", "4.900000e-324" },
                { Double.MIN_VALUE, "%#.0e", "5.e-324" },
                { Double.MIN_VALUE, "%#- (9.8e", " 4.90000000e-324" },
                { Double.MIN_VALUE, "%#+0(8.4e", "+4.9000e-324" },
                { Double.MIN_VALUE, "%-+(1.6e", "+4.900000e-324" },
                { Double.MIN_VALUE, "% 0(12e", " 4.900000e-324" },

                { Double.NaN, "%e", "NaN" },
                { Double.NaN, "%#.0e", "NaN" },
                { Double.NaN, "%#- (9.8e", "NaN      " },
                { Double.NaN, "%#+0(8.4e", "     NaN" },
                { Double.NaN, "%-+(1.6e", "NaN" },
                { Double.NaN, "% 0(12e", "         NaN" },

                { Double.NEGATIVE_INFINITY, "%e", "-Infinity" },
                { Double.NEGATIVE_INFINITY, "%#.0e", "-Infinity" },
                { Double.NEGATIVE_INFINITY, "%#- (9.8e", "(Infinity)" },
                { Double.NEGATIVE_INFINITY, "%#+0(8.4e", "(Infinity)" },
                { Double.NEGATIVE_INFINITY, "%-+(1.6e", "(Infinity)" },
                { Double.NEGATIVE_INFINITY, "% 0(12e", "  (Infinity)" },

                { Double.POSITIVE_INFINITY, "%e", "Infinity" },
                { Double.POSITIVE_INFINITY, "%#.0e", "Infinity" },
                { Double.POSITIVE_INFINITY, "%#- (9.8e", " Infinity" },
                { Double.POSITIVE_INFINITY, "%#+0(8.4e", "+Infinity" },
                { Double.POSITIVE_INFINITY, "%-+(1.6e", "+Infinity" },
                { Double.POSITIVE_INFINITY, "% 0(12e", "    Infinity" },
        };
        final int input = 0;
        final int pattern = 1;
        final int output = 2;
        for (int i = 0; i < tripleE.length; i++) {
            f = new Formatter(Locale.US);
            f.format((String) tripleE[i][pattern], tripleE[i][input]);
            assertEquals("triple[" + i + "]:" + tripleE[i][input] + ",pattern["
                    + i + "]:" + tripleE[i][pattern],
                    tripleE[i][output], f.toString());

            // test for conversion type 'E'
            f = new Formatter(Locale.US);
            f.format(((String) tripleE[i][pattern]).toUpperCase(), tripleE[i][input]);
            assertEquals("triple[" + i + "]:" + tripleE[i][input] + ",pattern["
                    + i + "]:" + tripleE[i][pattern], ((String) tripleE[i][output])
                    .toUpperCase(Locale.UK), f.toString());
        }

        f = new Formatter(Locale.GERMAN);
        f.format("%e", 1001f);
        /*
         * fail on RI, spec says 'e' requires the output to be formatted in
         * general scientific notation and the localization algorithm is
         * applied. But RI format this case to 1.001000e+03, which does not
         * conform to the German Locale
         */
        assertEquals("1,001000e+03", f.toString());
    }

    /**
     * java.util.Formatter#format(String, Object...) for Float/Double
     * conversion type 'g' and 'G'
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_FloatConversionG() {
        Formatter f = null;
        final Object[][] tripleG = {
                { 1001f, "%g", "1001.00" },
                { 1001f, "%- (,9.8g", " 1,001.0000" },
                { 1001f, "%+0(,8.4g", "+001,001" },
                { 1001f, "%-+(,1.6g", "+1,001.00" },
                { 1001f, "% 0(,12.0g", " 0000001e+03" },

                { 1.f, "%g", "1.00000" },
                { 1.f, "%- (,9.8g", " 1.0000000" },
                { 1.f, "%+0(,8.4g", "+001.000" },
                { 1.f, "%-+(,1.6g", "+1.00000" },
                { 1.f, "% 0(,12.0g", " 00000000001" },

                { -98f, "%g", "-98.0000" },
                { -98f, "%- (,9.8g", "(98.000000)" },
                { -98f, "%+0(,8.4g", "(098.00)" },
                { -98f, "%-+(,1.6g", "(98.0000)" },
                { -98f, "% 0(,12.0g", "(000001e+02)" },

                { 0.000001f, "%g", "1.00000e-06" },
                { 0.000001f, "%- (,9.8g", " 1.0000000e-06" },
                { 0.000001f, "%+0(,8.4g", "+1.000e-06" },
                { 0.000001f, "%-+(,1.6g", "+1.00000e-06" },
                { 0.000001f, "% 0(,12.0g", " 0000001e-06" },

                { 345.1234567f, "%g", "345.123" },
                { 345.1234567f, "%- (,9.8g", " 345.12344" },
                { 345.1234567f, "%+0(,8.4g", "+00345.1" },
                { 345.1234567f, "%-+(,1.6g", "+345.123" },
                { 345.1234567f, "% 0(,12.0g", " 0000003e+02" },

                { -.00000012345f, "%g", "-1.23450e-07" },
                { -.00000012345f, "%- (,9.8g", "(1.2344999e-07)" },
                { -.00000012345f, "%+0(,8.4g", "(1.234e-07)" },
                { -.00000012345f, "%-+(,1.6g", "(1.23450e-07)" },
                { -.00000012345f, "% 0(,12.0g", "(000001e-07)" },

                { -987.1234567f, "%g", "-987.123" },
                { -987.1234567f, "%- (,9.8g", "(987.12347)" },
                { -987.1234567f, "%+0(,8.4g", "(0987.1)" },
                { -987.1234567f, "%-+(,1.6g", "(987.123)" },
                { -987.1234567f, "% 0(,12.0g", "(000001e+03)" },

                { Float.MAX_VALUE, "%g", "3.40282e+38" },
                { Float.MAX_VALUE, "%- (,9.8g", " 3.4028235e+38" },
                { Float.MAX_VALUE, "%+0(,8.4g", "+3.403e+38" },
                { Float.MAX_VALUE, "%-+(,1.6g", "+3.40282e+38" },
                { Float.MAX_VALUE, "% 0(,12.0g", " 0000003e+38" },

                { Float.MIN_VALUE, "%g", "1.40130e-45" },
                { Float.MIN_VALUE, "%- (,9.8g", " 1.4012985e-45" },
                { Float.MIN_VALUE, "%+0(,8.4g", "+1.401e-45" },
                { Float.MIN_VALUE, "%-+(,1.6g", "+1.40130e-45" },
                { Float.MIN_VALUE, "% 0(,12.0g", " 0000001e-45" },

                { Float.NaN, "%g", "NaN" },
                { Float.NaN, "%- (,9.8g", "NaN      " },
                { Float.NaN, "%+0(,8.4g", "     NaN" },
                { Float.NaN, "%-+(,1.6g", "NaN" },
                { Float.NaN, "% 0(,12.0g", "         NaN" },

                { Float.NEGATIVE_INFINITY, "%g", "-Infinity" },
                { Float.NEGATIVE_INFINITY, "%- (,9.8g", "(Infinity)" },
                { Float.NEGATIVE_INFINITY, "%+0(,8.4g", "(Infinity)" },
                { Float.NEGATIVE_INFINITY, "%-+(,1.6g", "(Infinity)" },
                { Float.NEGATIVE_INFINITY, "% 0(,12.0g", "  (Infinity)" },

                { Float.POSITIVE_INFINITY, "%g", "Infinity" },
                { Float.POSITIVE_INFINITY, "%- (,9.8g", " Infinity" },
                { Float.POSITIVE_INFINITY, "%+0(,8.4g", "+Infinity" },
                { Float.POSITIVE_INFINITY, "%-+(,1.6g", "+Infinity" },
                { Float.POSITIVE_INFINITY, "% 0(,12.0g", "    Infinity" },

                { 1d, "%g", "1.00000" },
                { 1d, "%- (,9.8g", " 1.0000000" },
                { 1d, "%+0(,8.4g", "+001.000" },
                { 1d, "%-+(,1.6g", "+1.00000" },
                { 1d, "% 0(,12.0g", " 00000000001" },

                { -1d, "%g", "-1.00000" },
                { -1d, "%- (,9.8g", "(1.0000000)" },
                { -1d, "%+0(,8.4g", "(01.000)" },
                { -1d, "%-+(,1.6g", "(1.00000)" },
                { -1d, "% 0(,12.0g", "(0000000001)" },

                { .00000001d, "%g", "1.00000e-08" },
                { .00000001d, "%- (,9.8g", " 1.0000000e-08" },
                { .00000001d, "%+0(,8.4g", "+1.000e-08" },
                { .00000001d, "%-+(,1.6g", "+1.00000e-08" },
                { .00000001d, "% 0(,12.0g", " 0000001e-08" },

                { 1912.10d, "%g", "1912.10" },
                { 1912.10d, "%- (,9.8g", " 1,912.1000" },
                { 1912.10d, "%+0(,8.4g", "+001,912" },
                { 1912.10d, "%-+(,1.6g", "+1,912.10" },
                { 1912.10d, "% 0(,12.0g", " 0000002e+03" },

                { 0.1d, "%g", "0.100000" },
                { 0.1d, "%- (,9.8g", " 0.10000000" },
                { 0.1d, "%+0(,8.4g", "+00.1000" },
                { 0.1d, "%-+(,1.6g", "+0.100000" },
                { 0.1d, "% 0(,12.0g", " 000000000.1" },

                { -2.d, "%g", "-2.00000" },
                { -2.d, "%- (,9.8g", "(2.0000000)" },
                { -2.d, "%+0(,8.4g", "(02.000)" },
                { -2.d, "%-+(,1.6g", "(2.00000)" },
                { -2.d, "% 0(,12.0g", "(0000000002)" },

                { -.00039d, "%g", "-0.000390000" },
                { -.00039d, "%- (,9.8g", "(0.00039000000)" },
                { -.00039d, "%+0(,8.4g", "(0.0003900)" },
                { -.00039d, "%-+(,1.6g", "(0.000390000)" },
                { -.00039d, "% 0(,12.0g", "(00000.0004)" },

                { -1234567890.012345678d, "%g", "-1.23457e+09" },
                { -1234567890.012345678d, "%- (,9.8g", "(1.2345679e+09)" },
                { -1234567890.012345678d, "%+0(,8.4g", "(1.235e+09)" },
                { -1234567890.012345678d, "%-+(,1.6g", "(1.23457e+09)" },
                { -1234567890.012345678d, "% 0(,12.0g", "(000001e+09)" },

                { Double.MAX_VALUE, "%g", "1.79769e+308" },
                { Double.MAX_VALUE, "%- (,9.8g", " 1.7976931e+308" },
                { Double.MAX_VALUE, "%+0(,8.4g", "+1.798e+308" },
                { Double.MAX_VALUE, "%-+(,1.6g", "+1.79769e+308" },
                { Double.MAX_VALUE, "% 0(,12.0g", " 000002e+308" },

                { Double.MIN_VALUE, "%g", "4.90000e-324" },
                { Double.MIN_VALUE, "%- (,9.8g", " 4.9000000e-324" },
                { Double.MIN_VALUE, "%+0(,8.4g", "+4.900e-324" },
                { Double.MIN_VALUE, "%-+(,1.6g", "+4.90000e-324" },
                { Double.MIN_VALUE, "% 0(,12.0g", " 000005e-324" },

                { Double.NaN, "%g", "NaN" },
                { Double.NaN, "%- (,9.8g", "NaN      " },
                { Double.NaN, "%+0(,8.4g", "     NaN" },
                { Double.NaN, "%-+(,1.6g", "NaN" },
                { Double.NaN, "% 0(,12.0g", "         NaN" },

                { Double.NEGATIVE_INFINITY, "%g", "-Infinity" },
                { Double.NEGATIVE_INFINITY, "%- (,9.8g", "(Infinity)" },
                { Double.NEGATIVE_INFINITY, "%+0(,8.4g", "(Infinity)" },
                { Double.NEGATIVE_INFINITY, "%-+(,1.6g", "(Infinity)" },
                { Double.NEGATIVE_INFINITY, "% 0(,12.0g", "  (Infinity)" },

                { Double.POSITIVE_INFINITY, "%g", "Infinity" },
                { Double.POSITIVE_INFINITY, "%- (,9.8g", " Infinity" },
                { Double.POSITIVE_INFINITY, "%+0(,8.4g", "+Infinity" },
                { Double.POSITIVE_INFINITY, "%-+(,1.6g", "+Infinity" },
                { Double.POSITIVE_INFINITY, "% 0(,12.0g", "    Infinity" },

        };
        final int input = 0;
        final int pattern = 1;
        final int output = 2;
        for (int i = 0; i < tripleG.length; i++) {

            f = new Formatter(Locale.US);
            f.format((String) tripleG[i][pattern], tripleG[i][input]);
            assertEquals("triple[" + i + "]:" + tripleG[i][input] + ",pattern["
                    + i + "]:" + tripleG[i][pattern],
                    tripleG[i][output], f.toString());

            // test for conversion type 'G'
            f = new Formatter(Locale.US);
            f.format(((String) tripleG[i][pattern]).toUpperCase(), tripleG[i][input]);
            assertEquals("triple[" + i + "]:" + tripleG[i][input] + ",pattern["
                    + i + "]:" + tripleG[i][pattern], ((String) tripleG[i][output])
                    .toUpperCase(Locale.UK), f.toString());
        }

        f = new Formatter(Locale.US);
        f.format("%.5g", 0f);
        assertEquals("0.0000", f.toString());

        f = new Formatter(Locale.US);
        f.format("%.0g", 0f);
        /*
         * fail on RI, spec says if the precision is 0, then it is taken to be
         * 1. but RI throws ArrayIndexOutOfBoundsException.
         */
        assertEquals("0", f.toString());

        f = new Formatter(Locale.GERMAN);
        f.format("%g", 1001f);
        /*
         * fail on RI, spec says 'g' requires the output to be formatted in
         * general scientific notation and the localization algorithm is
         * applied. But RI format this case to 1001.00, which does not conform
         * to the German Locale
         */
        assertEquals("1001,00", f.toString());
    }

    /**
     * java.util.Formatter#format(String, Object...) for Float/Double
     * conversion type 'g' and 'G' overflow
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_FloatConversionG_Overflow() {
        Formatter f = new Formatter();
        f.format("%g", 999999.5);
        assertEquals("1.00000e+06", f.toString());

        f = new Formatter();
        f.format("%g", 99999.5);
        assertEquals("99999.5", f.toString());

        f = new Formatter();
        f.format("%.4g", 99.95);
        assertEquals("99.95", f.toString());

        f = new Formatter();
        f.format("%g", 99.95);
        assertEquals("99.9500", f.toString());

        f = new Formatter();
        f.format("%g", 0.9);
        assertEquals("0.900000", f.toString());

        f = new Formatter();
        f.format("%.0g", 0.000095);
        assertEquals("0.0001", f.toString());

        f = new Formatter();
        f.format("%g", 0.0999999);
        assertEquals("0.0999999", f.toString());

        f = new Formatter();
        f.format("%g", 0.00009);
        assertEquals("9.00000e-05", f.toString());
    }

    /**
     * java.util.Formatter#format(String, Object...) for Float/Double
     * conversion type 'f'
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_FloatConversionF() {
        Formatter f = null;

        final Object[][] tripleF = {
                { 0f, "%f", "0,000000" },
                { 0f, "%#.3f", "0,000" },
                { 0f, "%,5f", "0,000000" },
                { 0f, "%- (12.0f", " 0          " },
                { 0f, "%#+0(1.6f", "+0,000000" },
                { 0f, "%-+(8.4f", "+0,0000 " },
                { 0f, "% 0#(9.8f", " 0,00000000" },

                { 1234f, "%f", "1234,000000" },
                { 1234f, "%#.3f", "1234,000" },
                { 1234f, "%,5f", "1.234,000000" },
                { 1234f, "%- (12.0f", " 1234       " },
                { 1234f, "%#+0(1.6f", "+1234,000000" },
                { 1234f, "%-+(8.4f", "+1234,0000" },
                { 1234f, "% 0#(9.8f", " 1234,00000000" },

                { 1.f, "%f", "1,000000" },
                { 1.f, "%#.3f", "1,000" },
                { 1.f, "%,5f", "1,000000" },
                { 1.f, "%- (12.0f", " 1          " },
                { 1.f, "%#+0(1.6f", "+1,000000" },
                { 1.f, "%-+(8.4f", "+1,0000 " },
                { 1.f, "% 0#(9.8f", " 1,00000000" },

                { -98f, "%f", "-98,000000" },
                { -98f, "%#.3f", "-98,000" },
                { -98f, "%,5f", "-98,000000" },
                { -98f, "%- (12.0f", "(98)        " },
                { -98f, "%#+0(1.6f", "(98,000000)" },
                { -98f, "%-+(8.4f", "(98,0000)" },
                { -98f, "% 0#(9.8f", "(98,00000000)" },

                { 0.000001f, "%f", "0,000001" },
                { 0.000001f, "%#.3f", "0,000" },
                { 0.000001f, "%,5f", "0,000001" },
                { 0.000001f, "%- (12.0f", " 0          " },
                { 0.000001f, "%#+0(1.6f", "+0,000001" },
                { 0.000001f, "%-+(8.4f", "+0,0000 " },
                { 0.000001f, "% 0#(9.8f", " 0,00000100" },

                { 345.1234567f, "%f", "345,123444" },
                { 345.1234567f, "%#.3f", "345,123" },
                { 345.1234567f, "%,5f", "345,123444" },
                { 345.1234567f, "%- (12.0f", " 345        " },
                { 345.1234567f, "%#+0(1.6f", "+345,123444" },
                { 345.1234567f, "%-+(8.4f", "+345,1234" },
                { 345.1234567f, "% 0#(9.8f", " 345,12344360" },

                { -.00000012345f, "%f", "-0,000000" },
                { -.00000012345f, "%#.3f", "-0,000" },
                { -.00000012345f, "%,5f", "-0,000000" },
                { -.00000012345f, "%- (12.0f", "(0)         " },
                { -.00000012345f, "%#+0(1.6f", "(0,000000)" },
                { -.00000012345f, "%-+(8.4f", "(0,0000)" },
                { -.00000012345f, "% 0#(9.8f", "(0,00000012)" },

                { -987654321.1234567f, "%f", "-987654336,000000" },
                { -987654321.1234567f, "%#.3f", "-987654336,000" },
                { -987654321.1234567f, "%,5f", "-987.654.336,000000" },
                { -987654321.1234567f, "%- (12.0f", "(987654336) " },
                { -987654321.1234567f, "%#+0(1.6f", "(987654336,000000)" },
                { -987654321.1234567f, "%-+(8.4f", "(987654336,0000)" },
                { -987654321.1234567f, "% 0#(9.8f", "(987654336,00000000)" },

                { Float.MAX_VALUE, "%f", "340282346638528860000000000000000000000,000000" },
                { Float.MAX_VALUE, "%#.3f", "340282346638528860000000000000000000000,000" },
                { Float.MAX_VALUE, "%,5f", "340.282.346.638.528.860.000.000.000.000.000.000.000,000000" },
                { Float.MAX_VALUE, "%- (12.0f", " 340282346638528860000000000000000000000" },
                { Float.MAX_VALUE, "%#+0(1.6f", "+340282346638528860000000000000000000000,000000" },
                { Float.MAX_VALUE, "%-+(8.4f", "+340282346638528860000000000000000000000,0000" },
                { Float.MAX_VALUE, "% 0#(9.8f", " 340282346638528860000000000000000000000,00000000" },

                { Float.MIN_VALUE, "%f", "0,000000" },
                { Float.MIN_VALUE, "%#.3f", "0,000" },
                { Float.MIN_VALUE, "%,5f", "0,000000" },
                { Float.MIN_VALUE, "%- (12.0f", " 0          " },
                { Float.MIN_VALUE, "%#+0(1.6f", "+0,000000" },
                { Float.MIN_VALUE, "%-+(8.4f", "+0,0000 " },
                { Float.MIN_VALUE, "% 0#(9.8f", " 0,00000000" },

                { Float.NaN, "%f", "NaN" },
                { Float.NaN, "%#.3f", "NaN" },
                { Float.NaN, "%,5f", "  NaN" },
                { Float.NaN, "%- (12.0f", "NaN         " },
                { Float.NaN, "%#+0(1.6f", "NaN" },
                { Float.NaN, "%-+(8.4f", "NaN     " },
                { Float.NaN, "% 0#(9.8f", "      NaN" },

                { Float.NEGATIVE_INFINITY, "%f", "-Infinity" },
                { Float.NEGATIVE_INFINITY, "%#.3f", "-Infinity" },
                { Float.NEGATIVE_INFINITY, "%,5f", "-Infinity" },
                { Float.NEGATIVE_INFINITY, "%- (12.0f", "(Infinity)  " },
                { Float.NEGATIVE_INFINITY, "%#+0(1.6f", "(Infinity)" },
                { Float.NEGATIVE_INFINITY, "%-+(8.4f", "(Infinity)" },
                { Float.NEGATIVE_INFINITY, "% 0#(9.8f", "(Infinity)" },

                { Float.POSITIVE_INFINITY, "%f", "Infinity" },
                { Float.POSITIVE_INFINITY, "%#.3f", "Infinity" },
                { Float.POSITIVE_INFINITY, "%,5f", "Infinity" },
                { Float.POSITIVE_INFINITY, "%- (12.0f", " Infinity   " },
                { Float.POSITIVE_INFINITY, "%#+0(1.6f", "+Infinity" },
                { Float.POSITIVE_INFINITY, "%-+(8.4f", "+Infinity" },
                { Float.POSITIVE_INFINITY, "% 0#(9.8f", " Infinity" },


                { 0d, "%f", "0,000000" },
                { 0d, "%#.3f", "0,000" },
                { 0d, "%,5f", "0,000000" },
                { 0d, "%- (12.0f", " 0          " },
                { 0d, "%#+0(1.6f", "+0,000000" },
                { 0d, "%-+(8.4f", "+0,0000 " },
                { 0d, "% 0#(9.8f", " 0,00000000" },

                { 1d, "%f", "1,000000" },
                { 1d, "%#.3f", "1,000" },
                { 1d, "%,5f", "1,000000" },
                { 1d, "%- (12.0f", " 1          " },
                { 1d, "%#+0(1.6f", "+1,000000" },
                { 1d, "%-+(8.4f", "+1,0000 " },
                { 1d, "% 0#(9.8f", " 1,00000000" },

                { -1d, "%f", "-1,000000" },
                { -1d, "%#.3f", "-1,000" },
                { -1d, "%,5f", "-1,000000" },
                { -1d, "%- (12.0f", "(1)         " },
                { -1d, "%#+0(1.6f", "(1,000000)" },
                { -1d, "%-+(8.4f", "(1,0000)" },
                { -1d, "% 0#(9.8f", "(1,00000000)" },

                { .00000001d, "%f", "0,000000" },
                { .00000001d, "%#.3f", "0,000" },
                { .00000001d, "%,5f", "0,000000" },
                { .00000001d, "%- (12.0f", " 0          " },
                { .00000001d, "%#+0(1.6f", "+0,000000" },
                { .00000001d, "%-+(8.4f", "+0,0000 " },
                { .00000001d, "% 0#(9.8f", " 0,00000001" },

                { 1000.10d, "%f", "1000,100000" },
                { 1000.10d, "%#.3f", "1000,100" },
                { 1000.10d, "%,5f", "1.000,100000" },
                { 1000.10d, "%- (12.0f", " 1000       " },
                { 1000.10d, "%#+0(1.6f", "+1000,100000" },
                { 1000.10d, "%-+(8.4f", "+1000,1000" },
                { 1000.10d, "% 0#(9.8f", " 1000,10000000" },

                { 0.1d, "%f", "0,100000" },
                { 0.1d, "%#.3f", "0,100" },
                { 0.1d, "%,5f", "0,100000" },
                { 0.1d, "%- (12.0f", " 0          " },
                { 0.1d, "%#+0(1.6f", "+0,100000" },
                { 0.1d, "%-+(8.4f", "+0,1000 " },
                { 0.1d, "% 0#(9.8f", " 0,10000000" },

                { -2.d, "%f", "-2,000000" },
                { -2.d, "%#.3f", "-2,000" },
                { -2.d, "%,5f", "-2,000000" },
                { -2.d, "%- (12.0f", "(2)         " },
                { -2.d, "%#+0(1.6f", "(2,000000)" },
                { -2.d, "%-+(8.4f", "(2,0000)" },
                { -2.d, "% 0#(9.8f", "(2,00000000)" },

                { -.00009d, "%f", "-0,000090" },
                { -.00009d, "%#.3f", "-0,000" },
                { -.00009d, "%,5f", "-0,000090" },
                { -.00009d, "%- (12.0f", "(0)         " },
                { -.00009d, "%#+0(1.6f", "(0,000090)" },
                { -.00009d, "%-+(8.4f", "(0,0001)" },
                { -.00009d, "% 0#(9.8f", "(0,00009000)" },

                { -1234567890.012345678d, "%f", "-1234567890,012346" },
                { -1234567890.012345678d, "%#.3f", "-1234567890,012" },
                { -1234567890.012345678d, "%,5f", "-1.234.567.890,012346" },
                { -1234567890.012345678d, "%- (12.0f", "(1234567890)" },
                { -1234567890.012345678d, "%#+0(1.6f", "(1234567890,012346)" },
                { -1234567890.012345678d, "%-+(8.4f", "(1234567890,0123)" },
                { -1234567890.012345678d, "% 0#(9.8f", "(1234567890,01234580)" },

                { Double.MAX_VALUE, "%f", "179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,000000" },
                { Double.MAX_VALUE, "%#.3f", "179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,000" },
                { Double.MAX_VALUE, "%,5f", "179.769.313.486.231.570.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000.000,000000" },
                { Double.MAX_VALUE, "%- (12.0f", " 179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000" },
                { Double.MAX_VALUE, "%#+0(1.6f", "+179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,000000" },
                { Double.MAX_VALUE, "%-+(8.4f", "+179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,0000" },
                { Double.MAX_VALUE, "% 0#(9.8f", " 179769313486231570000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000,00000000" },

                { Double.MIN_VALUE, "%f", "0,000000" },
                { Double.MIN_VALUE, "%#.3f", "0,000" },
                { Double.MIN_VALUE, "%,5f", "0,000000" },
                { Double.MIN_VALUE, "%- (12.0f", " 0          " },
                { Double.MIN_VALUE, "%#+0(1.6f", "+0,000000" },
                { Double.MIN_VALUE, "%-+(8.4f", "+0,0000 " },
                { Double.MIN_VALUE, "% 0#(9.8f", " 0,00000000" },

                { Double.NaN, "%f", "NaN" },
                { Double.NaN, "%#.3f", "NaN" },
                { Double.NaN, "%,5f", "  NaN" },
                { Double.NaN, "%- (12.0f", "NaN         " },
                { Double.NaN, "%#+0(1.6f", "NaN" },
                { Double.NaN, "%-+(8.4f", "NaN     " },
                { Double.NaN, "% 0#(9.8f", "      NaN" },

                { Double.POSITIVE_INFINITY, "%f", "Infinity" },
                { Double.POSITIVE_INFINITY, "%#.3f", "Infinity" },
                { Double.POSITIVE_INFINITY, "%,5f", "Infinity" },
                { Double.POSITIVE_INFINITY, "%- (12.0f", " Infinity   " },
                { Double.POSITIVE_INFINITY, "%#+0(1.6f", "+Infinity" },
                { Double.POSITIVE_INFINITY, "%-+(8.4f", "+Infinity" },
                { Double.POSITIVE_INFINITY, "% 0#(9.8f", " Infinity" },

                { Double.NEGATIVE_INFINITY, "%f", "-Infinity" },
                { Double.NEGATIVE_INFINITY, "%#.3f", "-Infinity" },
                { Double.NEGATIVE_INFINITY, "%,5f", "-Infinity" },
                { Double.NEGATIVE_INFINITY, "%- (12.0f", "(Infinity)  " },
                { Double.NEGATIVE_INFINITY, "%#+0(1.6f", "(Infinity)" },
                { Double.NEGATIVE_INFINITY, "%-+(8.4f", "(Infinity)" },
                { Double.NEGATIVE_INFINITY, "% 0#(9.8f", "(Infinity)" },
        };
        final int input = 0;
        final int pattern = 1;
        final int output = 2;
        for (int i = 0; i < tripleF.length; i++) {
            f = new Formatter(Locale.GERMAN);
            f.format((String) tripleF[i][pattern], tripleF[i][input]);
            assertEquals("triple[" + i + "]:" + tripleF[i][input] + ",pattern["
                    + i + "]:" + tripleF[i][pattern],
                    tripleF[i][output], f.toString());
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for Float/Double
     * conversion type 'a' and 'A'
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_FloatConversionA() {
        Formatter f = null;
        final Object[][] tripleA = {
                { -0f, "%a", "-0x0.0p0" },
                { -0f, "%#.3a", "-0x0.000p0" },
                { -0f, "%5a", "-0x0.0p0" },
                { -0f, "%- 12.0a", "-0x0.0p0    " },
                { -0f, "%#+01.6a", "-0x0.000000p0" },
                { -0f, "%-+8.4a", "-0x0.0000p0" },

                { 0f, "%a", "0x0.0p0" },
                { 0f, "%#.3a", "0x0.000p0" },
                { 0f, "%5a", "0x0.0p0" },
                { 0f, "%- 12.0a", " 0x0.0p0    " },
                { 0f, "%#+01.6a", "+0x0.000000p0" },
                { 0f, "%-+8.4a", "+0x0.0000p0" },

                { 1234f, "%a", "0x1.348p10" },
                { 1234f, "%#.3a", "0x1.348p10" },
                { 1234f, "%5a", "0x1.348p10" },
                { 1234f, "%- 12.0a", " 0x1.3p10   " },
                { 1234f, "%#+01.6a", "+0x1.348000p10" },
                { 1234f, "%-+8.4a", "+0x1.3480p10" },

                { 1.f, "%a", "0x1.0p0" },
                { 1.f, "%#.3a", "0x1.000p0" },
                { 1.f, "%5a", "0x1.0p0" },
                { 1.f, "%- 12.0a", " 0x1.0p0    " },
                { 1.f, "%#+01.6a", "+0x1.000000p0" },
                { 1.f, "%-+8.4a", "+0x1.0000p0" },

                { -98f, "%a", "-0x1.88p6" },
                { -98f, "%#.3a", "-0x1.880p6" },
                { -98f, "%5a", "-0x1.88p6" },
                { -98f, "%- 12.0a", "-0x1.8p6    " },
                { -98f, "%#+01.6a", "-0x1.880000p6" },
                { -98f, "%-+8.4a", "-0x1.8800p6" },

                { 345.1234567f, "%a", "0x1.591f9ap8" },
                { 345.1234567f, "%5a", "0x1.591f9ap8" },
                { 345.1234567f, "%#+01.6a", "+0x1.591f9ap8" },

                { -987654321.1234567f, "%a", "-0x1.d6f346p29" },
                { -987654321.1234567f, "%#.3a", "-0x1.d6fp29" },
                { -987654321.1234567f, "%5a", "-0x1.d6f346p29" },
                { -987654321.1234567f, "%- 12.0a", "-0x1.dp29   " },
                { -987654321.1234567f, "%#+01.6a", "-0x1.d6f346p29" },
                { -987654321.1234567f, "%-+8.4a", "-0x1.d6f3p29" },

                { Float.MAX_VALUE, "%a", "0x1.fffffep127" },
                { Float.MAX_VALUE, "%5a", "0x1.fffffep127" },
                { Float.MAX_VALUE, "%#+01.6a", "+0x1.fffffep127" },

                { Float.NaN, "%a", "NaN" },
                { Float.NaN, "%#.3a", "NaN" },
                { Float.NaN, "%5a", "  NaN" },
                { Float.NaN, "%- 12.0a", "NaN         " },
                { Float.NaN, "%#+01.6a", "NaN" },
                { Float.NaN, "%-+8.4a", "NaN     " },

                { Float.NEGATIVE_INFINITY, "%a", "-Infinity" },
                { Float.NEGATIVE_INFINITY, "%#.3a", "-Infinity" },
                { Float.NEGATIVE_INFINITY, "%5a", "-Infinity" },
                { Float.NEGATIVE_INFINITY, "%- 12.0a", "-Infinity   " },
                { Float.NEGATIVE_INFINITY, "%#+01.6a", "-Infinity" },
                { Float.NEGATIVE_INFINITY, "%-+8.4a", "-Infinity" },

                { Float.POSITIVE_INFINITY, "%a", "Infinity" },
                { Float.POSITIVE_INFINITY, "%#.3a", "Infinity" },
                { Float.POSITIVE_INFINITY, "%5a", "Infinity" },
                { Float.POSITIVE_INFINITY, "%- 12.0a", " Infinity   " },
                { Float.POSITIVE_INFINITY, "%#+01.6a", "+Infinity" },
                { Float.POSITIVE_INFINITY, "%-+8.4a", "+Infinity" },

                { -0d, "%a", "-0x0.0p0" },
                { -0d, "%#.3a", "-0x0.000p0" },
                { -0d, "%5a", "-0x0.0p0" },
                { -0d, "%- 12.0a", "-0x0.0p0    " },
                { -0d, "%#+01.6a", "-0x0.000000p0" },
                { -0d, "%-+8.4a", "-0x0.0000p0" },

                { 0d, "%a", "0x0.0p0" },
                { 0d, "%#.3a", "0x0.000p0" },
                { 0d, "%5a", "0x0.0p0" },
                { 0d, "%- 12.0a", " 0x0.0p0    " },
                { 0d, "%#+01.6a", "+0x0.000000p0" },
                { 0d, "%-+8.4a", "+0x0.0000p0" },

                { 1d, "%a", "0x1.0p0" },
                { 1d, "%#.3a", "0x1.000p0" },
                { 1d, "%5a", "0x1.0p0" },
                { 1d, "%- 12.0a", " 0x1.0p0    " },
                { 1d, "%#+01.6a", "+0x1.000000p0" },
                { 1d, "%-+8.4a", "+0x1.0000p0" },

                { -1d, "%a", "-0x1.0p0" },
                { -1d, "%#.3a", "-0x1.000p0" },
                { -1d, "%5a", "-0x1.0p0" },
                { -1d, "%- 12.0a", "-0x1.0p0    " },
                { -1d, "%#+01.6a", "-0x1.000000p0" },
                { -1d, "%-+8.4a", "-0x1.0000p0" },

                { .00000001d, "%a", "0x1.5798ee2308c3ap-27" },
                { .00000001d, "%5a", "0x1.5798ee2308c3ap-27" },
                { .00000001d, "%- 12.0a", " 0x1.5p-27  " },
                { .00000001d, "%#+01.6a", "+0x1.5798eep-27" },

                { 1000.10d, "%a", "0x1.f40cccccccccdp9" },
                { 1000.10d, "%5a", "0x1.f40cccccccccdp9" },
                { 1000.10d, "%- 12.0a", " 0x1.fp9    " },

                { 0.1d, "%a", "0x1.999999999999ap-4" },
                { 0.1d, "%5a", "0x1.999999999999ap-4" },

                { -2.d, "%a", "-0x1.0p1" },
                { -2.d, "%#.3a", "-0x1.000p1" },
                { -2.d, "%5a", "-0x1.0p1" },
                { -2.d, "%- 12.0a", "-0x1.0p1    " },
                { -2.d, "%#+01.6a", "-0x1.000000p1" },
                { -2.d, "%-+8.4a", "-0x1.0000p1" },

                { -.00009d, "%a", "-0x1.797cc39ffd60fp-14" },
                { -.00009d, "%5a", "-0x1.797cc39ffd60fp-14" },

                { -1234567890.012345678d, "%a", "-0x1.26580b480ca46p30" },
                { -1234567890.012345678d, "%5a", "-0x1.26580b480ca46p30" },
                { -1234567890.012345678d, "%- 12.0a", "-0x1.2p30   " },
                { -1234567890.012345678d, "%#+01.6a", "-0x1.26580bp30" },
                { -1234567890.012345678d, "%-+8.4a", "-0x1.2658p30" },

                { Double.MAX_VALUE, "%a", "0x1.fffffffffffffp1023" },
                { Double.MAX_VALUE, "%5a", "0x1.fffffffffffffp1023" },

                { Double.MIN_VALUE, "%a", "0x0.0000000000001p-1022" },
                { Double.MIN_VALUE, "%5a", "0x0.0000000000001p-1022" },

                { Double.NaN, "%a", "NaN" },
                { Double.NaN, "%#.3a", "NaN" },
                { Double.NaN, "%5a", "  NaN" },
                { Double.NaN, "%- 12.0a", "NaN         " },
                { Double.NaN, "%#+01.6a", "NaN" },
                { Double.NaN, "%-+8.4a", "NaN     " },

                { Double.NEGATIVE_INFINITY, "%a", "-Infinity" },
                { Double.NEGATIVE_INFINITY, "%#.3a", "-Infinity" },
                { Double.NEGATIVE_INFINITY, "%5a", "-Infinity" },
                { Double.NEGATIVE_INFINITY, "%- 12.0a", "-Infinity   " },
                { Double.NEGATIVE_INFINITY, "%#+01.6a", "-Infinity" },
                { Double.NEGATIVE_INFINITY, "%-+8.4a", "-Infinity" },

                { Double.POSITIVE_INFINITY, "%a", "Infinity" },
                { Double.POSITIVE_INFINITY, "%#.3a", "Infinity" },
                { Double.POSITIVE_INFINITY, "%5a", "Infinity" },
                { Double.POSITIVE_INFINITY, "%- 12.0a", " Infinity   " },
                { Double.POSITIVE_INFINITY, "%#+01.6a", "+Infinity" },
                { Double.POSITIVE_INFINITY, "%-+8.4a", "+Infinity" },

        };
        final int input = 0;
        final int pattern = 1;
        final int output = 2;
        for (int i = 0; i < tripleA.length; i++) {
            f = new Formatter(Locale.UK);
            f.format((String) tripleA[i][pattern], tripleA[i][input]);
            assertEquals("triple[" + i + "]:" + tripleA[i][input] + ",pattern["
                    + i + "]:" + tripleA[i][pattern],
                    tripleA[i][output], f.toString());

            // test for conversion type 'A'
            f = new Formatter(Locale.UK);
            f.format(((String) tripleA[i][pattern]).toUpperCase(), tripleA[i][input]);
            assertEquals("triple[" + i + "]:" + tripleA[i][input] + ",pattern["
                    + i + "]:" + tripleA[i][pattern], ((String) tripleA[i][output])
                    .toUpperCase(Locale.UK), f.toString());
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for BigDecimal
     * conversion type 'e' and 'E'
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_BigDecimalConversionE() {
        Formatter f = null;
        final Object[][] tripleE = {
                { BigDecimal.ZERO, "%e", "0.000000e+00" },
                { BigDecimal.ZERO, "%#.0e", "0.e+00" },
                { BigDecimal.ZERO, "%# 9.8e", " 0.00000000e+00" },
                { BigDecimal.ZERO, "%#+0(8.4e", "+0.0000e+00" },
                { BigDecimal.ZERO, "%-+17.6e", "+0.000000e+00    " },
                { BigDecimal.ZERO, "% 0(20e", " 00000000.000000e+00" },

                { BigDecimal.ONE, "%e", "1.000000e+00" },
                { BigDecimal.ONE, "%#.0e", "1.e+00" },
                { BigDecimal.ONE, "%# 9.8e", " 1.00000000e+00" },
                { BigDecimal.ONE, "%#+0(8.4e", "+1.0000e+00" },
                { BigDecimal.ONE, "%-+17.6e", "+1.000000e+00    " },
                { BigDecimal.ONE, "% 0(20e", " 00000001.000000e+00" },

                { BigDecimal.TEN, "%e", "1.000000e+01" },
                { BigDecimal.TEN, "%#.0e", "1.e+01" },
                { BigDecimal.TEN, "%# 9.8e", " 1.00000000e+01" },
                { BigDecimal.TEN, "%#+0(8.4e", "+1.0000e+01" },
                { BigDecimal.TEN, "%-+17.6e", "+1.000000e+01    " },
                { BigDecimal.TEN, "% 0(20e", " 00000001.000000e+01" },

                { new BigDecimal(-1), "%e", "-1.000000e+00" },
                { new BigDecimal(-1), "%#.0e", "-1.e+00" },
                { new BigDecimal(-1), "%# 9.8e", "-1.00000000e+00" },
                { new BigDecimal(-1), "%#+0(8.4e", "(1.0000e+00)" },
                { new BigDecimal(-1), "%-+17.6e", "-1.000000e+00    " },
                { new BigDecimal(-1), "% 0(20e", "(0000001.000000e+00)" },

                { new BigDecimal("5.000E999"), "%e", "5.000000e+999" },
                { new BigDecimal("5.000E999"), "%#.0e", "5.e+999" },
                { new BigDecimal("5.000E999"), "%# 9.8e", " 5.00000000e+999" },
                { new BigDecimal("5.000E999"), "%#+0(8.4e", "+5.0000e+999" },
                { new BigDecimal("5.000E999"), "%-+17.6e", "+5.000000e+999   " },
                { new BigDecimal("5.000E999"), "% 0(20e", " 0000005.000000e+999" },

                { new BigDecimal("-5.000E999"), "%e", "-5.000000e+999" },
                { new BigDecimal("-5.000E999"), "%#.0e", "-5.e+999" },
                { new BigDecimal("-5.000E999"), "%# 9.8e", "-5.00000000e+999" },
                { new BigDecimal("-5.000E999"), "%#+0(8.4e", "(5.0000e+999)" },
                { new BigDecimal("-5.000E999"), "%-+17.6e", "-5.000000e+999   " },
                { new BigDecimal("-5.000E999"), "% 0(20e", "(000005.000000e+999)" },
        };
        final int input = 0;
        final int pattern = 1;
        final int output = 2;
        for (int i = 0; i < tripleE.length; i++) {
            f = new Formatter(Locale.US);
            f.format((String) tripleE[i][pattern], tripleE[i][input]);
            assertEquals("triple[" + i + "]:" + tripleE[i][input] + ",pattern["
                    + i + "]:" + tripleE[i][pattern],
                    tripleE[i][output], f.toString());

            // test for conversion type 'E'
            f = new Formatter(Locale.US);
            f.format(((String) tripleE[i][pattern]).toUpperCase(), tripleE[i][input]);
            assertEquals("triple[" + i + "]:" + tripleE[i][input] + ",pattern["
                    + i + "]:" + tripleE[i][pattern], ((String) tripleE[i][output])
                    .toUpperCase(Locale.US), f.toString());
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for BigDecimal
     * conversion type 'g' and 'G'
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_BigDecimalConversionG() {
        Formatter f = null;
        final Object[][] tripleG = {
                { BigDecimal.ZERO, "%g", "0.00000" },
                { BigDecimal.ZERO, "%.5g", "0.0000" },
                { BigDecimal.ZERO, "%- (,9.8g", " 0.0000000" },
                { BigDecimal.ZERO, "%+0(,8.4g", "+000.000" },
                { BigDecimal.ZERO, "%-+10.6g", "+0.00000  " },
                { BigDecimal.ZERO, "% 0(,12.0g", " 00000000000" },
                { BigDecimal.ONE, "%g", "1.00000" },
                { BigDecimal.ONE, "%.5g", "1.0000" },
                { BigDecimal.ONE, "%- (,9.8g", " 1.0000000" },
                { BigDecimal.ONE, "%+0(,8.4g", "+001.000" },
                { BigDecimal.ONE, "%-+10.6g", "+1.00000  " },
                { BigDecimal.ONE, "% 0(,12.0g", " 00000000001" },

                { new BigDecimal(-1), "%g", "-1.00000" },
                { new BigDecimal(-1), "%.5g", "-1.0000" },
                { new BigDecimal(-1), "%- (,9.8g", "(1.0000000)" },
                { new BigDecimal(-1), "%+0(,8.4g", "(01.000)" },
                { new BigDecimal(-1), "%-+10.6g", "-1.00000  " },
                { new BigDecimal(-1), "% 0(,12.0g", "(0000000001)" },

                { new BigDecimal(-0.000001), "%g", "-1.00000e-06" },
                { new BigDecimal(-0.000001), "%.5g", "-1.0000e-06" },
                { new BigDecimal(-0.000001), "%- (,9.8g", "(1.0000000e-06)" },
                { new BigDecimal(-0.000001), "%+0(,8.4g", "(1.000e-06)" },
                { new BigDecimal(-0.000001), "%-+10.6g", "-1.00000e-06" },
                { new BigDecimal(-0.000001), "% 0(,12.0g", "(000001e-06)" },

                { new BigDecimal(0.0002), "%g", "0.000200000" },
                { new BigDecimal(0.0002), "%.5g", "0.00020000" },
                { new BigDecimal(0.0002), "%- (,9.8g", " 0.00020000000" },
                { new BigDecimal(0.0002), "%+0(,8.4g", "+0.0002000" },
                { new BigDecimal(0.0002), "%-+10.6g", "+0.000200000" },
                { new BigDecimal(0.0002), "% 0(,12.0g", " 000000.0002" },

                { new BigDecimal(-0.003), "%g", "-0.00300000" },
                { new BigDecimal(-0.003), "%.5g", "-0.0030000" },
                { new BigDecimal(-0.003), "%- (,9.8g", "(0.0030000000)" },
                { new BigDecimal(-0.003), "%+0(,8.4g", "(0.003000)" },
                { new BigDecimal(-0.003), "%-+10.6g", "-0.00300000" },
                { new BigDecimal(-0.003), "% 0(,12.0g", "(000000.003)" },

                { new BigDecimal("5.000E999"), "%g", "5.00000e+999" },
                { new BigDecimal("5.000E999"), "%.5g", "5.0000e+999" },
                { new BigDecimal("5.000E999"), "%- (,9.8g", " 5.0000000e+999" },
                { new BigDecimal("5.000E999"), "%+0(,8.4g", "+5.000e+999" },
                { new BigDecimal("5.000E999"), "%-+10.6g", "+5.00000e+999" },
                { new BigDecimal("5.000E999"), "% 0(,12.0g", " 000005e+999" },

                { new BigDecimal("-5.000E999"), "%g", "-5.00000e+999" },
                { new BigDecimal("-5.000E999"), "%.5g", "-5.0000e+999" },
                { new BigDecimal("-5.000E999"), "%- (,9.8g", "(5.0000000e+999)" },
                { new BigDecimal("-5.000E999"), "%+0(,8.4g", "(5.000e+999)" },
                { new BigDecimal("-5.000E999"), "%-+10.6g", "-5.00000e+999" },
                { new BigDecimal("-5.000E999"), "% 0(,12.0g", "(00005e+999)" },
        };
        final int input = 0;
        final int pattern = 1;
        final int output = 2;
        for (int i = 0; i < tripleG.length; i++) {
            f = new Formatter(Locale.US);
            f.format((String) tripleG[i][pattern], tripleG[i][input]);
            assertEquals("triple[" + i + "]:" + tripleG[i][input] + ",pattern["
                    + i + "]:" + tripleG[i][pattern],
                    tripleG[i][output], f.toString());

            // test for conversion type 'G'
            f = new Formatter(Locale.US);
            f.format(((String) tripleG[i][pattern]).toUpperCase(), tripleG[i][input]);
            assertEquals("triple[" + i + "]:" + tripleG[i][input] + ",pattern["
                    + i + "]:" + tripleG[i][pattern], ((String) tripleG[i][output])
                    .toUpperCase(Locale.US), f.toString());
        }

        f = new Formatter(Locale.GERMAN);
        f.format("%- (,9.6g", new BigDecimal("4E6"));
        /*
         * fail on RI, spec says 'g' requires the output to be formatted in
         * general scientific notation and the localization algorithm is
         * applied. But RI format this case to 4.00000e+06, which does not
         * conform to the German Locale
         */
        assertEquals(" 4,00000e+06", f.toString());
    }

    /**
     * java.util.Formatter#format(String, Object...) for BigDecimal
     * conversion type 'f'
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_BigDecimalConversionF() {

        Formatter f = null;
        final int input = 0;
        final int pattern = 1;
        final int output = 2;
        final Object[][] tripleF = {
                { BigDecimal.ZERO, "%f", "0.000000" },
                { BigDecimal.ZERO, "%#.3f", "0.000" },
                { BigDecimal.ZERO, "%#,5f", "0.000000" },
                { BigDecimal.ZERO, "%- #(12.0f", " 0.         " },
                { BigDecimal.ZERO, "%#+0(1.6f", "+0.000000" },
                { BigDecimal.ZERO, "%-+(8.4f", "+0.0000 " },
                { BigDecimal.ZERO, "% 0#(9.8f", " 0.00000000" },
                { BigDecimal.ONE, "%f", "1.000000" },
                { BigDecimal.ONE, "%#.3f", "1.000" },
                { BigDecimal.ONE, "%#,5f", "1.000000" },
                { BigDecimal.ONE, "%- #(12.0f", " 1.         " },
                { BigDecimal.ONE, "%#+0(1.6f", "+1.000000" },
                { BigDecimal.ONE, "%-+(8.4f", "+1.0000 " },
                { BigDecimal.ONE, "% 0#(9.8f", " 1.00000000" },
                { BigDecimal.TEN, "%f", "10.000000" },
                { BigDecimal.TEN, "%#.3f", "10.000" },
                { BigDecimal.TEN, "%#,5f", "10.000000" },
                { BigDecimal.TEN, "%- #(12.0f", " 10.        " },
                { BigDecimal.TEN, "%#+0(1.6f", "+10.000000" },
                { BigDecimal.TEN, "%-+(8.4f", "+10.0000" },
                { BigDecimal.TEN, "% 0#(9.8f", " 10.00000000" },
                { new BigDecimal(-1), "%f", "-1.000000" },
                { new BigDecimal(-1), "%#.3f", "-1.000" },
                { new BigDecimal(-1), "%#,5f", "-1.000000" },
                { new BigDecimal(-1), "%- #(12.0f", "(1.)        " },
                { new BigDecimal(-1), "%#+0(1.6f", "(1.000000)" },
                { new BigDecimal(-1), "%-+(8.4f", "(1.0000)" },
                { new BigDecimal(-1), "% 0#(9.8f", "(1.00000000)" },
                { new BigDecimal("9999999999999999999999999999999999999999999"), "%f", "9999999999999999999999999999999999999999999.000000" },
                { new BigDecimal("9999999999999999999999999999999999999999999"), "%#.3f", "9999999999999999999999999999999999999999999.000" },
                { new BigDecimal("9999999999999999999999999999999999999999999"), "%#,5f", "9,999,999,999,999,999,999,999,999,999,999,999,999,999,999.000000" },
                { new BigDecimal("9999999999999999999999999999999999999999999"), "%- #(12.0f", " 9999999999999999999999999999999999999999999." },
                { new BigDecimal("9999999999999999999999999999999999999999999"), "%#+0(1.6f", "+9999999999999999999999999999999999999999999.000000" },
                { new BigDecimal("9999999999999999999999999999999999999999999"), "%-+(8.4f", "+9999999999999999999999999999999999999999999.0000" },
                { new BigDecimal("9999999999999999999999999999999999999999999"), "% 0#(9.8f", " 9999999999999999999999999999999999999999999.00000000" },
                { new BigDecimal("-9999999999999999999999999999999999999999999"), "%f", "-9999999999999999999999999999999999999999999.000000" },
                { new BigDecimal("-9999999999999999999999999999999999999999999"), "%#.3f", "-9999999999999999999999999999999999999999999.000" },
                { new BigDecimal("-9999999999999999999999999999999999999999999"), "%#,5f", "-9,999,999,999,999,999,999,999,999,999,999,999,999,999,999.000000" },
                { new BigDecimal("-9999999999999999999999999999999999999999999"), "%- #(12.0f", "(9999999999999999999999999999999999999999999.)" },
                { new BigDecimal("-9999999999999999999999999999999999999999999"), "%#+0(1.6f", "(9999999999999999999999999999999999999999999.000000)" },
                { new BigDecimal("-9999999999999999999999999999999999999999999"), "%-+(8.4f", "(9999999999999999999999999999999999999999999.0000)" },
                { new BigDecimal("-9999999999999999999999999999999999999999999"), "% 0#(9.8f", "(9999999999999999999999999999999999999999999.00000000)" },
        };
        for (int i = 0; i < tripleF.length; i++) {
            f = new Formatter(Locale.US);
            f.format((String) tripleF[i][pattern], tripleF[i][input]);
            assertEquals("triple[" + i + "]:" + tripleF[i][input] + ",pattern["
                    + i + "]:" + tripleF[i][pattern], tripleF[i][output], f.toString());
        }

        f = new Formatter(Locale.US);
        f.format("%f", new BigDecimal("5.0E9"));
        // error on RI
        // RI throw ArrayIndexOutOfBoundsException
        assertEquals("5000000000.000000", f.toString());
    }

    /**
     * java.util.Formatter#format(String, Object...) for exceptions in
     * Float/Double/BigDecimal conversion type 'e', 'E', 'g', 'G', 'f', 'a', 'A'
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_FloatDoubleBigDecimalConversionException() {
        Formatter f = null;

        final char[] conversions = { 'e', 'E', 'g', 'G', 'f', 'a', 'A' };
        final Object[] illArgs = { false, (byte) 1, (short) 2, 3, (long) 4,
                new BigInteger("5"), new Character('c'), new Object(),
                new Date() };
        for (int i = 0; i < illArgs.length; i++) {
            for (int j = 0; j < conversions.length; j++) {
                try {
                    f = new Formatter(Locale.UK);
                    f.format("%" + conversions[j], illArgs[i]);
                    fail("should throw IllegalFormatConversionException");
                } catch (IllegalFormatConversionException e) {
                    // expected
                }
            }
        }

        try {
            f = new Formatter(Locale.UK);
            f.format("%a", new BigDecimal(1));
            fail("should throw IllegalFormatConversionException");
        } catch (IllegalFormatConversionException e) {
            // expected
        }

        try {
            f = new Formatter(Locale.UK);
            f.format("%A", new BigDecimal(1));
            fail("should throw IllegalFormatConversionException");
        } catch (IllegalFormatConversionException e) {
            // expected
        }

        final String[] flagsConversionMismatches = { "%,e", "%,E", "%#g",
                "%#G", "%,a", "%,A", "%(a", "%(A" };
        for (int i = 0; i < flagsConversionMismatches.length; i++) {
            try {
                f = new Formatter(Locale.CHINA);
                f.format(flagsConversionMismatches[i], new BigDecimal(1));
                fail("should throw FormatFlagsConversionMismatchException");
            } catch (FormatFlagsConversionMismatchException e) {
                // expected
            }
            try {
                f = new Formatter(Locale.JAPAN);
                f.format(flagsConversionMismatches[i], (BigDecimal) null);
                fail("should throw FormatFlagsConversionMismatchException");
            } catch (FormatFlagsConversionMismatchException e) {
                // expected
            }
        }

        final String[] missingFormatWidths = { "%-0e", "%0e", "%-e", "%-0E",
                "%0E", "%-E", "%-0g", "%0g", "%-g", "%-0G", "%0G", "%-G",
                "%-0f", "%0f", "%-f", "%-0a", "%0a", "%-a", "%-0A", "%0A",
                "%-A" };
        for (int i = 0; i < missingFormatWidths.length; i++) {
            try {
                f = new Formatter(Locale.KOREA);
                f.format(missingFormatWidths[i], 1f);
                fail("should throw MissingFormatWidthException");
            } catch (MissingFormatWidthException e) {
                // expected
            }

            try {
                f = new Formatter(Locale.KOREA);
                f.format(missingFormatWidths[i], (Float) null);
                fail("should throw MissingFormatWidthException");
            } catch (MissingFormatWidthException e) {
                // expected
            }
        }

        final String[] illFlags = { "%+ e", "%+ E", "%+ g", "%+ G", "%+ f",
                "%+ a", "%+ A", "%-03e", "%-03E", "%-03g", "%-03G", "%-03f",
                "%-03a", "%-03A" };
        for (int i = 0; i < illFlags.length; i++) {
            try {
                f = new Formatter(Locale.CANADA);
                f.format(illFlags[i], 1.23d);
                fail("should throw IllegalFormatFlagsException");
            } catch (IllegalFormatFlagsException e) {
                // expected
            }

            try {
                f = new Formatter(Locale.CANADA);
                f.format(illFlags[i], (Double) null);
                fail("should throw IllegalFormatFlagsException");
            } catch (IllegalFormatFlagsException e) {
                // expected
            }
        }

        f = new Formatter(Locale.US);
        try {
            f.format("%F", 1);
            fail("should throw UnknownFormatConversionException");
        } catch (UnknownFormatConversionException e) {
            // expected
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for
     * Float/Double/BigDecimal exception throwing order
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_FloatDoubleBigDecimalExceptionOrder() {
        Formatter f = null;

        /*
         * Summary: UnknownFormatConversionException >
         * MissingFormatWidthException > IllegalFormatFlagsException >
         * FormatFlagsConversionMismatchException >
         * IllegalFormatConversionException
         *
         */
        try {
            // compare FormatFlagsConversionMismatchException and
            // IllegalFormatConversionException
            f = new Formatter(Locale.US);
            f.format("%,e", (byte) 1);
            fail("should throw FormatFlagsConversionMismatchException");
        } catch (FormatFlagsConversionMismatchException e) {
            // expected
        }

        try {
            // compare IllegalFormatFlagsException and
            // FormatFlagsConversionMismatchException
            f = new Formatter(Locale.US);
            f.format("%+ ,e", 1f);
            fail("should throw IllegalFormatFlagsException");
        } catch (IllegalFormatFlagsException e) {
            // expected
        }

        try {
            // compare MissingFormatWidthException and
            // IllegalFormatFlagsException
            f = new Formatter(Locale.US);
            f.format("%+ -e", 1f);
            fail("should throw MissingFormatWidthException");
        } catch (MissingFormatWidthException e) {
            // expected
        }

        try {
            // compare UnknownFormatConversionException and
            // MissingFormatWidthException
            f = new Formatter(Locale.US);
            f.format("%-F", 1f);
            fail("should throw UnknownFormatConversionException");
        } catch (UnknownFormatConversionException e) {
            // expected
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for BigDecimal
     * exception throwing order
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_BigDecimalExceptionOrder() {
        Formatter f = null;
        BigDecimal bd = new BigDecimal("1.0");

        /*
         * Summary: UnknownFormatConversionException >
         * MissingFormatWidthException > IllegalFormatFlagsException >
         * FormatFlagsConversionMismatchException >
         * IllegalFormatConversionException
         *
         */
        try {
            // compare FormatFlagsConversionMismatchException and
            // IllegalFormatConversionException
            f = new Formatter(Locale.US);
            f.format("%,e", (byte) 1);
            fail("should throw FormatFlagsConversionMismatchException");
        } catch (FormatFlagsConversionMismatchException e) {
            // expected
        }

        try {
            // compare IllegalFormatFlagsException and
            // FormatFlagsConversionMismatchException
            f = new Formatter(Locale.US);
            f.format("%+ ,e", bd);
            fail("should throw IllegalFormatFlagsException");
        } catch (IllegalFormatFlagsException e) {
            // expected
        }

        try {
            // compare MissingFormatWidthException and
            // IllegalFormatFlagsException
            f = new Formatter(Locale.US);
            f.format("%+ -e", bd);
            fail("should throw MissingFormatWidthException");
        } catch (MissingFormatWidthException e) {
            // expected
        }

        // compare UnknownFormatConversionException and
        // MissingFormatWidthException
        try {
            f = new Formatter(Locale.US);
            f.format("%-F", bd);
            fail("should throw UnknownFormatConversionException");
        } catch (UnknownFormatConversionException e) {
            // expected
        }
    }

    /**
     * java.util.Formatter#format(String, Object...) for null argment for
     * Float/Double/BigDecimal conversion
     */
    public void test_formatLjava_lang_String$Ljava_lang_Object_FloatDoubleBigDecimalNullConversion() {
        Formatter f = null;

        // test (Float)null
        f = new Formatter(Locale.FRANCE);
        f.format("%#- (9.0e", (Float) null);
        assertEquals("         ", f.toString());

        f = new Formatter(Locale.GERMAN);
        f.format("%-+(1.6E", (Float) null);
        assertEquals("NULL", f.toString());

        f = new Formatter(Locale.UK);
        f.format("%+0(,8.4g", (Float) null);
        assertEquals("    null", f.toString());

        f = new Formatter(Locale.FRANCE);
        f.format("%- (9.8G", (Float) null);
        assertEquals("NULL     ", f.toString());

        f = new Formatter(Locale.FRANCE);
        f.format("%- (12.1f", (Float) null);
        assertEquals("n           ", f.toString());

        f = new Formatter(Locale.FRANCE);
        f.format("% .4a", (Float) null);
        assertEquals("null", f.toString());

        f = new Formatter(Locale.FRANCE);
        f.format("%06A", (Float) null);
        assertEquals("  NULL", f.toString());

        // test (Double)null
        f = new Formatter(Locale.GERMAN);
        f.format("%- (9e", (Double) null);
        assertEquals("null     ", f.toString());

        f = new Formatter(Locale.GERMAN);
        f.format("%#-+(1.6E", (Double) null);
        assertEquals("NULL", f.toString());

        f = new Formatter(Locale.GERMAN);
        f.format("%+0(6.4g", (Double) null);
        assertEquals("  null", f.toString());

        f = new Formatter(Locale.GERMAN);
        f.format("%- (,5.8G", (Double) null);
        assertEquals("NULL ", f.toString());

        f = new Formatter(Locale.GERMAN);
        f.format("% (.4f", (Double) null);
        assertEquals("null", f.toString());

        f = new Formatter(Locale.GERMAN);
        f.format("%#.6a", (Double) null);
        assertEquals("null", f.toString());

        f = new Formatter(Locale.GERMAN);
        f.format("% 2.5A", (Double) null);
        assertEquals("NULL", f.toString());

        // test (BigDecimal)null
        f = new Formatter(Locale.UK);
        f.format("%#- (6.2e", (BigDecimal) null);
        assertEquals("nu    ", f.toString());

        f = new Formatter(Locale.UK);
        f.format("%-+(1.6E", (BigDecimal) null);
        assertEquals("NULL", f.toString());

        f = new Formatter(Locale.UK);
        f.format("%+-(,5.3g", (BigDecimal) null);
        assertEquals("nul  ", f.toString());

        f = new Formatter(Locale.UK);
        f.format("%0 3G", (BigDecimal) null);
        assertEquals("NULL", f.toString());

        f = new Formatter(Locale.UK);
        f.format("%0 (9.0G", (BigDecimal) null);
        assertEquals("         ", f.toString());

        f = new Formatter(Locale.UK);
        f.format("% (.5f", (BigDecimal) null);
        assertEquals("null", f.toString());

        f = new Formatter(Locale.UK);
        f.format("%06a", (BigDecimal) null);
        assertEquals("  null", f.toString());

        f = new Formatter(Locale.UK);
        f.format("% .5A", (BigDecimal) null);
        assertEquals("NULL", f.toString());
    }

    /**
     * java.util.Formatter.BigDecimalLayoutForm#values()
     */
    public void test_values() {
        BigDecimalLayoutForm[] vals = BigDecimalLayoutForm.values();
        assertEquals("Invalid length of enum values", 2, vals.length);
        assertEquals("Wrong scientific value in enum", BigDecimalLayoutForm.SCIENTIFIC, vals[0]);
        assertEquals("Wrong dec float value in enum", BigDecimalLayoutForm.DECIMAL_FLOAT, vals[1]);
    }

    /**
     * java.util.Formatter.BigDecimalLayoutForm#valueOf(String)
     */
    public void test_valueOfLjava_lang_String() {
        BigDecimalLayoutForm sci = BigDecimalLayoutForm.valueOf("SCIENTIFIC");
        assertEquals("Wrong scientific value in enum", BigDecimalLayoutForm.SCIENTIFIC, sci);

        BigDecimalLayoutForm decFloat = BigDecimalLayoutForm.valueOf("DECIMAL_FLOAT");
        assertEquals("Wrong dec float value from valueOf ", BigDecimalLayoutForm.DECIMAL_FLOAT, decFloat);
    }

    /*
     * Regression test for Harmony-5845
     * test the short name for timezone whether uses DaylightTime or not
     */
    public void test_DaylightTime() {
        Locale.setDefault(Locale.US);
        Calendar c1 = new GregorianCalendar(2007, 0, 1);
        Calendar c2 = new GregorianCalendar(2007, 7, 1);

        for (String tz : TimeZone.getAvailableIDs()) {
            if (tz.equals("America/Los_Angeles")) {
                c1.setTimeZone(TimeZone.getTimeZone(tz));
                c2.setTimeZone(TimeZone.getTimeZone(tz));
                assertTrue(String.format("%1$tZ%2$tZ", c1, c2).equals("PSTPDT"));
            }
            if (tz.equals("America/Panama")) {
                c1.setTimeZone(TimeZone.getTimeZone(tz));
                c2.setTimeZone(TimeZone.getTimeZone(tz));
                assertTrue(String.format("%1$tZ%2$tZ", c1, c2).equals("ESTEST"));
            }
        }
    }

    /*
     * Regression test for Harmony-5845
     * test scientific notation to follow RI's behavior
     */
    public void test_ScientificNotation() {
        Formatter f = new Formatter();
        MathContext mc = new MathContext(30);
        BigDecimal value = new BigDecimal(0.1, mc);
        f.format("%.30G", value);

        String result = f.toString();
        String expected = "0.100000000000000005551115123126";
        assertEquals(expected, result);
    }


    /**
     * Setup resource files for testing
     */
    protected void setUp() throws IOException {
        root = System.getProperty("user.name").equalsIgnoreCase("root");
        notExist = File.createTempFile("notexist", null);
        notExist.delete();

        fileWithContent = File.createTempFile("filewithcontent", null);
        BufferedOutputStream bw = new BufferedOutputStream(
                new FileOutputStream(fileWithContent));
        bw.write(1);// write something into the file
        bw.close();

        readOnly = File.createTempFile("readonly", null);
        readOnly.setReadOnly();

        secret = File.createTempFile("secret", null);

        defaultLocale = Locale.getDefault();

        defaultTimeZone = TimeZone.getDefault();
        TimeZone cst = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(cst);
    }

    /**
     * Delete the resource files if they exist
     */
    protected void tearDown() {
        if (notExist.exists()) {
            notExist.delete();
        }

        if (fileWithContent.exists()) {
            fileWithContent.delete();
        }
        if (readOnly.exists()) {
            readOnly.delete();
        }
        if (secret.exists()) {
            secret.delete();
        }

        Locale.setDefault(defaultLocale);
        TimeZone.setDefault(defaultTimeZone);
    }
}
