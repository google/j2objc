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

package java.io;

import java.util.Formatter;
import java.util.IllegalFormatException;
import java.util.Locale;

/**
 * Wraps either an existing {@link OutputStream} or an existing {@link Writer}
 * and provides convenience methods for printing common data types in a human
 * readable format. No {@code IOException} is thrown by this class. Instead,
 * callers should use {@link #checkError()} to see if a problem has occurred in
 * this writer.
 */
public class PrintWriter extends Writer {
    /**
     * The writer to print data to.
     */
    protected Writer out;

    /**
     * Indicates whether this PrintWriter is in an error state.
     */
    private boolean ioError;

    /**
     * Indicates whether or not this PrintWriter should flush its contents after
     * printing a new line.
     */
    private boolean autoFlush;

    /**
     * Constructs a new {@code PrintWriter} with {@code out} as its target
     * stream. By default, the new print writer does not automatically flush its
     * contents to the target stream when a newline is encountered.
     *
     * @param out
     *            the target output stream.
     * @throws NullPointerException
     *             if {@code out} is {@code null}.
     */
    public PrintWriter(OutputStream out) {
        this(new OutputStreamWriter(out), false);
    }

    /**
     * Constructs a new {@code PrintWriter} with {@code out} as its target
     * stream. The parameter {@code autoFlush} determines if the print writer
     * automatically flushes its contents to the target stream when a newline is
     * encountered.
     *
     * @param out
     *            the target output stream.
     * @param autoFlush
     *            indicates whether contents are flushed upon encountering a
     *            newline sequence.
     * @throws NullPointerException
     *             if {@code out} is {@code null}.
     */
    public PrintWriter(OutputStream out, boolean autoFlush) {
        this(new OutputStreamWriter(out), autoFlush);
    }

    /**
     * Constructs a new {@code PrintWriter} with {@code wr} as its target
     * writer. By default, the new print writer does not automatically flush its
     * contents to the target writer when a newline is encountered.
     *
     * @param wr
     *            the target writer.
     * @throws NullPointerException
     *             if {@code wr} is {@code null}.
     */
    public PrintWriter(Writer wr) {
        this(wr, false);
    }

    /**
     * Constructs a new {@code PrintWriter} with {@code out} as its target
     * writer. The parameter {@code autoFlush} determines if the print writer
     * automatically flushes its contents to the target writer when a newline is
     * encountered.
     *
     * @param wr
     *            the target writer.
     * @param autoFlush
     *            indicates whether to flush contents upon encountering a
     *            newline sequence.
     * @throws NullPointerException
     *             if {@code out} is {@code null}.
     */
    public PrintWriter(Writer wr, boolean autoFlush) {
        super(wr);
        this.autoFlush = autoFlush;
        out = wr;
    }

    /**
     * Constructs a new {@code PrintWriter} with {@code file} as its target. The
     * VM's default character set is used for character encoding.
     * The print writer does not automatically flush its contents to the target
     * file when a newline is encountered. The output to the file is buffered.
     *
     * @param file
     *            the target file. If the file already exists, its contents are
     *            removed, otherwise a new file is created.
     * @throws FileNotFoundException
     *             if an error occurs while opening or creating the target file.
     */
    public PrintWriter(File file) throws FileNotFoundException {
        this(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file))), false);
    }

    /**
     * Constructs a new {@code PrintWriter} with {@code file} as its target. The
     * character set named {@code csn} is used for character encoding.
     * The print writer does not automatically flush its contents to the target
     * file when a newline is encountered. The output to the file is buffered.
     *
     * @param file
     *            the target file. If the file already exists, its contents are
     *            removed, otherwise a new file is created.
     * @param csn
     *            the name of the character set used for character encoding.
     * @throws FileNotFoundException
     *             if an error occurs while opening or creating the target file.
     * @throws NullPointerException
     *             if {@code csn} is {@code null}.
     * @throws UnsupportedEncodingException
     *             if the encoding specified by {@code csn} is not supported.
     */
    public PrintWriter(File file, String csn) throws FileNotFoundException,
            UnsupportedEncodingException {
        this(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)), csn),
                false);
    }

    /**
     * Constructs a new {@code PrintWriter} with the file identified by {@code
     * fileName} as its target. The VM's default character set is
     * used for character encoding. The print writer does not automatically
     * flush its contents to the target file when a newline is encountered. The
     * output to the file is buffered.
     *
     * @param fileName
     *            the target file's name. If the file already exists, its
     *            contents are removed, otherwise a new file is created.
     * @throws FileNotFoundException
     *             if an error occurs while opening or creating the target file.
     */
    public PrintWriter(String fileName) throws FileNotFoundException {
        this(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(fileName))),
                false);
    }

     /**
     * Constructs a new {@code PrintWriter} with the file identified by {@code
     * fileName} as its target. The character set named {@code csn} is used for
     * character encoding. The print writer does not automatically flush its
     * contents to the target file when a newline is encountered. The output to
     * the file is buffered.
     *
     * @param fileName
     *            the target file's name. If the file already exists, its
     *            contents are removed, otherwise a new file is created.
     * @param csn
     *            the name of the character set used for character encoding.
     * @throws FileNotFoundException
     *             if an error occurs while opening or creating the target file.
     * @throws NullPointerException
     *             if {@code csn} is {@code null}.
     * @throws UnsupportedEncodingException
     *             if the encoding specified by {@code csn} is not supported.
     */
    public PrintWriter(String fileName, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        this(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(fileName)), csn),
                false);
    }

    /**
     * Flushes this writer and returns the value of the error flag.
     *
     * @return {@code true} if either an {@code IOException} has been thrown
     *         previously or if {@code setError()} has been called;
     *         {@code false} otherwise.
     * @see #setError()
     */
    public boolean checkError() {
        Writer delegate = out;
        if (delegate == null) {
            return ioError;
        }

        flush();
        return ioError || delegate.checkError();
    }

    /**
     * Sets the error state of the stream to false.
     * @since 1.6
     */
    protected void clearError() {
        synchronized (lock) {
            ioError = false;
        }
    }

    /**
     * Closes this print writer. Flushes this writer and then closes the target.
     * If an I/O error occurs, this writer's error flag is set to {@code true}.
     */
    @Override
    public void close() {
        synchronized (lock) {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    setError();
                }
                out = null;
            }
        }
    }

    /**
     * Ensures that all pending data is sent out to the target. It also
     * flushes the target. If an I/O error occurs, this writer's error
     * state is set to {@code true}.
     */
    @Override
    public void flush() {
        synchronized (lock) {
            if (out != null) {
                try {
                    out.flush();
                } catch (IOException e) {
                    setError();
                }
            } else {
                setError();
            }
        }
    }

    /**
     * Formats {@code args} according to the format string {@code format}, and writes the result
     * to this stream. This method uses the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     * If automatic flushing is enabled then the buffer is flushed as well.
     *
     * @param format the format string (see {@link java.util.Formatter#format})
     * @param args
     *            the list of arguments passed to the formatter. If there are
     *            more arguments than required by {@code format},
     *            additional arguments are ignored.
     * @return this writer.
     * @throws IllegalFormatException
     *             if the format string is illegal or incompatible with the
     *             arguments, if there are not enough arguments or if any other
     *             error regarding the format string or arguments is detected.
     * @throws NullPointerException if {@code format == null}
     */
    public PrintWriter format(String format, Object... args) {
        return format(Locale.getDefault(), format, args);
    }

    /**
     * Writes a string formatted by an intermediate {@code Formatter} to the
     * target using the specified locale, format string and arguments. If
     * automatic flushing is enabled then this writer is flushed.
     *
     * @param l
     *            the locale used in the method. No localization will be applied
     *            if {@code l} is {@code null}.
     * @param format the format string (see {@link java.util.Formatter#format})
     * @param args
     *            the list of arguments passed to the formatter. If there are
     *            more arguments than required by {@code format},
     *            additional arguments are ignored.
     * @return this writer.
     * @throws IllegalFormatException
     *             if the format string is illegal or incompatible with the
     *             arguments, if there are not enough arguments or if any other
     *             error regarding the format string or arguments is detected.
     * @throws NullPointerException if {@code format == null}
     */
    public PrintWriter format(Locale l, String format, Object... args) {
        if (format == null) {
            throw new NullPointerException("format == null");
        }
        new Formatter(this, l).format(format, args);
        if (autoFlush) {
            flush();
        }
        return this;
    }

    /**
     * Prints a formatted string. The behavior of this method is the same as
     * this writer's {@code #format(String, Object...)} method.
     *
     * <p>The {@code Locale} used is the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     *
     * @param format the format string (see {@link java.util.Formatter#format})
     * @param args
     *            the list of arguments passed to the formatter. If there are
     *            more arguments than required by {@code format},
     *            additional arguments are ignored.
     * @return this writer.
     * @throws IllegalFormatException
     *             if the format string is illegal or incompatible with the
     *             arguments, if there are not enough arguments or if any other
     *             error regarding the format string or arguments is detected.
     * @throws NullPointerException if {@code format == null}
     */
    public PrintWriter printf(String format, Object... args) {
        return format(format, args);
    }

    /**
     * Prints a formatted string. The behavior of this method is the same as
     * this writer's {@code #format(Locale, String, Object...)} method.
     *
     * @param l
     *            the locale used in the method. No localization will be applied
     *            if {@code l} is {@code null}.
     * @param format the format string (see {@link java.util.Formatter#format})
     * @param args
     *            the list of arguments passed to the formatter. If there are
     *            more arguments than required by {@code format},
     *            additional arguments are ignored.
     * @return this writer.
     * @throws IllegalFormatException
     *             if the format string is illegal or incompatible with the
     *             arguments, if there are not enough arguments or if any other
     *             error regarding the format string or arguments is detected.
     * @throws NullPointerException if {@code format == null}
     */
    public PrintWriter printf(Locale l, String format, Object... args) {
        return format(l, format, args);
    }

    /**
     * Prints the string representation of the specified character array
     * to the target.
     *
     * @param charArray
     *            the character array to print to the target.
     * @see #print(String)
     */
    public void print(char[] charArray) {
        print(new String(charArray, 0, charArray.length));
    }

    /**
     * Prints the string representation of the specified character to the
     * target.
     *
     * @param ch
     *            the character to print to the target.
     * @see #print(String)
     */
    public void print(char ch) {
        print(String.valueOf(ch));
    }

    /**
     * Prints the string representation of the specified double to the target.
     *
     * @param dnum
     *            the double value to print to the target.
     * @see #print(String)
     */
    public void print(double dnum) {
        print(String.valueOf(dnum));
    }

    /**
     * Prints the string representation of the specified float to the target.
     *
     * @param fnum
     *            the float value to print to the target.
     * @see #print(String)
     */
    public void print(float fnum) {
        print(String.valueOf(fnum));
    }

    /**
     * Prints the string representation of the specified integer to the target.
     *
     * @param inum
     *            the integer value to print to the target.
     * @see #print(String)
     */
    public void print(int inum) {
        print(String.valueOf(inum));
    }

    /**
     * Prints the string representation of the specified long to the target.
     *
     * @param lnum
     *            the long value to print to the target.
     * @see #print(String)
     */
    public void print(long lnum) {
        print(String.valueOf(lnum));
    }

    /**
     * Prints the string representation of the specified object to the target.
     *
     * @param obj
     *            the object to print to the target.
     * @see #print(String)
     */
    public void print(Object obj) {
        print(String.valueOf(obj));
    }

    /**
     * Prints a string to the target. The string is converted to an array of
     * bytes using the encoding chosen during the construction of this writer.
     * The bytes are then written to the target with {@code write(int)}.
     * <p>
     * If an I/O error occurs, this writer's error flag is set to {@code true}.
     *
     * @param str
     *            the string to print to the target.
     * @see #write(int)
     */
    public void print(String str) {
        write(str != null ? str : String.valueOf((Object) null));
    }

    /**
     * Prints the string representation of the specified boolean to the target.
     *
     * @param bool
     *            the boolean value to print the target.
     * @see #print(String)
     */
    public void print(boolean bool) {
        print(String.valueOf(bool));
    }

    /**
     * Prints a newline. Flushes this writer if the autoFlush flag is set to {@code true}.
     */
    public void println() {
        synchronized (lock) {
            print(System.lineSeparator());
            if (autoFlush) {
                flush();
            }
        }
    }

    /**
     * Prints the string representation of the character array {@code chars} followed by a newline.
     * Flushes this writer if the autoFlush flag is set to {@code true}.
     */
    public void println(char[] chars) {
        println(new String(chars, 0, chars.length));
    }

    /**
     * Prints the string representation of the char {@code c} followed by a newline.
     * Flushes this writer if the autoFlush flag is set to {@code true}.
     */
    public void println(char c) {
        println(String.valueOf(c));
    }

    /**
     * Prints the string representation of the double {@code d} followed by a newline.
     * Flushes this writer if the autoFlush flag is set to {@code true}.
     */
    public void println(double d) {
        println(String.valueOf(d));
    }

    /**
     * Prints the string representation of the float {@code f} followed by a newline.
     * Flushes this writer if the autoFlush flag is set to {@code true}.
     */
    public void println(float f) {
        println(String.valueOf(f));
    }

    /**
     * Prints the string representation of the int {@code i} followed by a newline.
     * Flushes this writer if the autoFlush flag is set to {@code true}.
     */
    public void println(int i) {
        println(String.valueOf(i));
    }

    /**
     * Prints the string representation of the long {@code l} followed by a newline.
     * Flushes this writer if the autoFlush flag is set to {@code true}.
     */
    public void println(long l) {
        println(String.valueOf(l));
    }

    /**
     * Prints the string representation of the object {@code o}, or {@code "null},
     * followed by a newline.
     * Flushes this writer if the autoFlush flag is set to {@code true}.
     */
    public void println(Object obj) {
        println(String.valueOf(obj));
    }

    /**
     * Prints the string representation of the string {@code s} followed by a newline.
     * Flushes this writer if the autoFlush flag is set to {@code true}.
     *
     * <p>The string is converted to an array of bytes using the
     * encoding chosen during the construction of this writer. The bytes are
     * then written to the target with {@code write(int)}. Finally, this writer
     * is flushed if the autoFlush flag is set to {@code true}.
     *
     * <p>If an I/O error occurs, this writer's error flag is set to {@code true}.
     */
    public void println(String str) {
        synchronized (lock) {
            print(str);
            println();
        }
    }

    /**
     * Prints the string representation of the boolean {@code b} followed by a newline.
     * Flushes this writer if the autoFlush flag is set to {@code true}.
     */
    public void println(boolean b) {
        println(String.valueOf(b));
    }

    /**
     * Sets the error flag of this writer to true.
     */
    protected void setError() {
        synchronized (lock) {
            ioError = true;
        }
    }

    /**
     * Writes the character buffer {@code buf} to the target.
     *
     * @param buf
     *            the non-null array containing characters to write.
     */
    @Override
    public void write(char[] buf) {
        write(buf, 0, buf.length);
    }

    /**
     * Writes {@code count} characters from {@code buffer} starting at {@code
     * offset} to the target.
     * <p>
     * This writer's error flag is set to {@code true} if this writer is closed
     * or an I/O error occurs.
     *
     * @param buf
     *            the buffer to write to the target.
     * @param offset
     *            the index of the first character in {@code buffer} to write.
     * @param count
     *            the number of characters in {@code buffer} to write.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if {@code
     *             offset + count} is greater than the length of {@code buf}.
     */
    @Override
    public void write(char[] buf, int offset, int count) {
        doWrite(buf, offset, count);
    }

    /**
     * Writes one character to the target. Only the two least significant bytes
     * of the integer {@code oneChar} are written.
     * <p>
     * This writer's error flag is set to {@code true} if this writer is closed
     * or an I/O error occurs.
     *
     * @param oneChar
     *            the character to write to the target.
     */
    @Override
    public void write(int oneChar) {
        doWrite(new char[] { (char) oneChar }, 0, 1);
    }

    private final void doWrite(char[] buf, int offset, int count) {
        synchronized (lock) {
            if (out != null) {
                try {
                    out.write(buf, offset, count);
                } catch (IOException e) {
                    setError();
                }
            } else {
                setError();
            }
        }
    }

    /**
     * Writes the characters from the specified string to the target.
     *
     * @param str
     *            the non-null string containing the characters to write.
     */
    @Override
    public void write(String str) {
        write(str.toCharArray());
    }

    /**
     * Writes {@code count} characters from {@code str} starting at {@code
     * offset} to the target.
     *
     * @param str
     *            the non-null string containing the characters to write.
     * @param offset
     *            the index of the first character in {@code str} to write.
     * @param count
     *            the number of characters from {@code str} to write.
     * @throws IndexOutOfBoundsException
     *             if {@code offset < 0} or {@code count < 0}, or if {@code
     *             offset + count} is greater than the length of {@code str}.
     */
    @Override
    public void write(String str, int offset, int count) {
        write(str.substring(offset, offset + count).toCharArray());
    }

    /**
     * Appends the character {@code c} to the target.
     *
     * @param c
     *            the character to append to the target.
     * @return this writer.
     */
    @Override
    public PrintWriter append(char c) {
        write(c);
        return this;
    }

    /**
     * Appends the character sequence {@code csq} to the target. This
     * method works the same way as {@code PrintWriter.print(csq.toString())}.
     * If {@code csq} is {@code null}, then the string "null" is written
     * to the target.
     *
     * @param csq
     *            the character sequence appended to the target.
     * @return this writer.
     */
    @Override
    public PrintWriter append(CharSequence csq) {
        if (csq == null) {
            csq = "null";
        }
        append(csq, 0, csq.length());
        return this;
    }

    /**
     * Appends a subsequence of the character sequence {@code csq} to the
     * target. This method works the same way as {@code
     * PrintWriter.print(csq.subsequence(start, end).toString())}. If {@code
     * csq} is {@code null}, then the specified subsequence of the string "null"
     * will be written to the target.
     *
     * @param csq
     *            the character sequence appended to the target.
     * @param start
     *            the index of the first char in the character sequence appended
     *            to the target.
     * @param end
     *            the index of the character following the last character of the
     *            subsequence appended to the target.
     * @return this writer.
     * @throws StringIndexOutOfBoundsException
     *             if {@code start > end}, {@code start < 0}, {@code end < 0} or
     *             either {@code start} or {@code end} are greater or equal than
     *             the length of {@code csq}.
     */
    @Override
    public PrintWriter append(CharSequence csq, int start, int end) {
        if (csq == null) {
            csq = "null";
        }
        String output = csq.subSequence(start, end).toString();
        write(output, 0, output.length());
        return this;
    }
}
