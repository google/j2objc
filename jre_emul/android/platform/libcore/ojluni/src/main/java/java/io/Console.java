/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.io;

/* J2ObjC removed
import java.util.*;
import java.nio.charset.Charset;
import sun.nio.cs.StreamDecoder;
import sun.nio.cs.StreamEncoder;
*/

import java.util.Formatter;
import libcore.io.Libcore;

/**
 * Methods to access the character-based console device, if any, associated
 * with the current Java virtual machine.
 *
 * <p> Whether a virtual machine has a console is dependent upon the
 * underlying platform and also upon the manner in which the virtual
 * machine is invoked.  If the virtual machine is started from an
 * interactive command line without redirecting the standard input and
 * output streams then its console will exist and will typically be
 * connected to the keyboard and display from which the virtual machine
 * was launched.  If the virtual machine is started automatically, for
 * example by a background job scheduler, then it will typically not
 * have a console.
 * <p>
 * If this virtual machine has a console then it is represented by a
 * unique instance of this class which can be obtained by invoking the
 * {@link java.lang.System#console()} method.  If no console device is
 * available then an invocation of that method will return <tt>null</tt>.
 * <p>
 * Read and write operations are synchronized to guarantee the atomic
 * completion of critical operations; therefore invoking methods
 * {@link #readLine()}, {@link #readPassword()}, {@link #format format()},
 * {@link #printf printf()} as well as the read, format and write operations
 * on the objects returned by {@link #reader()} and {@link #writer()} may
 * block in multithreaded scenarios.
 * <p>
 * Invoking <tt>close()</tt> on the objects returned by the {@link #reader()}
 * and the {@link #writer()} will not close the underlying stream of those
 * objects.
 * <p>
 * The console-read methods return <tt>null</tt> when the end of the
 * console input stream is reached, for example by typing control-D on
 * Unix or control-Z on Windows.  Subsequent read operations will succeed
 * if additional characters are later entered on the console's input
 * device.
 * <p>
 * Unless otherwise specified, passing a <tt>null</tt> argument to any method
 * in this class will cause a {@link NullPointerException} to be thrown.
 * <p>
 * <b>Security note:</b>
 * If an application needs to read a password or other secure data, it should
 * use {@link #readPassword()} or {@link #readPassword(String, Object...)} and
 * manually zero the returned character array after processing to minimize the
 * lifetime of sensitive data in memory.
 *
 * <blockquote><pre>{@code
 * Console cons;
 * char[] passwd;
 * if ((cons = System.console()) != null &&
 *     (passwd = cons.readPassword("[%s]", "Password:")) != null) {
 *     ...
 *     java.util.Arrays.fill(passwd, ' ');
 * }
 * }</pre></blockquote>
 *
 * @author  Xueming Shen
 * @since   1.6
 */

public final class Console implements Flushable
{
    private static final Object CONSOLE_LOCK = new Object();

    private static final Console console = makeConsole();

    private final ConsoleReader reader;
    private final PrintWriter writer;

    /**
     * Secret accessor for {@code System.console}.
     * @hide
     */
    public static Console getConsole() {
        return console;
    }

    private static Console makeConsole() {
        // We don't care about stderr, because this class only uses stdin and stdout.
        if (!Libcore.os.isatty(FileDescriptor.in) || !Libcore.os.isatty(FileDescriptor.out)) {
            return null;
        }
        try {
            return new Console(System.in, System.out);
        } catch (UnsupportedEncodingException ex) {
            throw new AssertionError(ex);
        }
    }

    private Console(InputStream in, OutputStream out) throws UnsupportedEncodingException {
        this.reader = new ConsoleReader(in);
        this.writer = new ConsoleWriter(out);
    }

   /**
    * Retrieves the unique {@link java.io.PrintWriter PrintWriter} object
    * associated with this console.
    *
    * @return  The printwriter associated with this console
    */
    public PrintWriter writer() {
        return writer;
    }


   /**
    * Retrieves the unique {@link java.io.Reader Reader} object associated
    * with this console.
    * <p>
    * This method is intended to be used by sophisticated applications, for
    * example, a {@link java.util.Scanner} object which utilizes the rich
    * parsing/scanning functionality provided by the <tt>Scanner</tt>:
    * <blockquote><pre>
    * Console con = System.console();
    * if (con != null) {
    *     Scanner sc = new Scanner(con.reader());
    *     ...
    * }
    * </pre></blockquote>
    * <p>
    * For simple applications requiring only line-oriented reading, use
    * <tt>{@link #readLine}</tt>.
    * <p>
    * The bulk read operations {@link java.io.Reader#read(char[]) read(char[]) },
    * {@link java.io.Reader#read(char[], int, int) read(char[], int, int) } and
    * {@link java.io.Reader#read(java.nio.CharBuffer) read(java.nio.CharBuffer)}
    * on the returned object will not read in characters beyond the line
    * bound for each invocation, even if the destination buffer has space for
    * more characters. The {@code Reader}'s {@code read} methods may block if a
    * line bound has not been entered or reached on the console's input device.
    * A line bound is considered to be any one of a line feed (<tt>'\n'</tt>),
    * a carriage return (<tt>'\r'</tt>), a carriage return followed immediately
    * by a linefeed, or an end of stream.
    *
    * @return  The reader associated with this console
    */
    public Reader reader() {
        return reader;
    }

   /**
    * Writes a formatted string to this console's output stream using
    * the specified format string and arguments.
    *
    * @param  fmt
    *         A format string as described in <a
    *         href="../util/Formatter.html#syntax">Format string syntax</a>
    *
    * @param  args
    *         Arguments referenced by the format specifiers in the format
    *         string.  If there are more arguments than format specifiers, the
    *         extra arguments are ignored.  The number of arguments is
    *         variable and may be zero.  The maximum number of arguments is
    *         limited by the maximum dimension of a Java array as defined by
    *         <cite>The Java&trade; Virtual Machine Specification</cite>.
    *         The behaviour on a
    *         <tt>null</tt> argument depends on the <a
    *         href="../util/Formatter.html#syntax">conversion</a>.
    *
    * @throws  IllegalFormatException
    *          If a format string contains an illegal syntax, a format
    *          specifier that is incompatible with the given arguments,
    *          insufficient arguments given the format string, or other
    *          illegal conditions.  For specification of all possible
    *          formatting errors, see the <a
    *          href="../util/Formatter.html#detail">Details</a> section
    *          of the formatter class specification.
    *
    * @return  This console
    */
    public Console format(String fmt, Object ...args) {
        try (Formatter f = new Formatter(writer)) {
        f.format(fmt, args);
        f.flush();
        return this;
      }
    }

   /**
    * A convenience method to write a formatted string to this console's
    * output stream using the specified format string and arguments.
    *
    * <p> An invocation of this method of the form <tt>con.printf(format,
    * args)</tt> behaves in exactly the same way as the invocation of
    * <pre>con.format(format, args)</pre>.
    *
    * @param  format
    *         A format string as described in <a
    *         href="../util/Formatter.html#syntax">Format string syntax</a>.
    *
    * @param  args
    *         Arguments referenced by the format specifiers in the format
    *         string.  If there are more arguments than format specifiers, the
    *         extra arguments are ignored.  The number of arguments is
    *         variable and may be zero.  The maximum number of arguments is
    *         limited by the maximum dimension of a Java array as defined by
    *         <cite>The Java&trade; Virtual Machine Specification</cite>.
    *         The behaviour on a
    *         <tt>null</tt> argument depends on the <a
    *         href="../util/Formatter.html#syntax">conversion</a>.
    *
    * @throws  IllegalFormatException
    *          If a format string contains an illegal syntax, a format
    *          specifier that is incompatible with the given arguments,
    *          insufficient arguments given the format string, or other
    *          illegal conditions.  For specification of all possible
    *          formatting errors, see the <a
    *          href="../util/Formatter.html#detail">Details</a> section of the
    *          formatter class specification.
    *
    * @return  This console
    */
    public Console printf(String format, Object ... args) {
        return format(format, args);
    }


   /**
    * Reads a single line of text from the console.
    *
    * @throws IOError
    *         If an I/O error occurs.
    *
    * @return  A string containing the line read from the console, not
    *          including any line-termination characters, or <tt>null</tt>
    *          if an end of stream has been reached.
    */
    public String readLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

   /**
    * Provides a formatted prompt, then reads a password or passphrase from
    * the console with echoing disabled.
    *
    * @param  fmt
    *         A format string as described in <a
    *         href="../util/Formatter.html#syntax">Format string syntax</a>
    *         for the prompt text.
    *
    * @param  args
    *         Arguments referenced by the format specifiers in the format
    *         string.  If there are more arguments than format specifiers, the
    *         extra arguments are ignored.  The maximum number of arguments is
    *         limited by the maximum dimension of a Java array as defined by
    *         <cite>The Java&trade; Virtual Machine Specification</cite>.
    *
    * @throws  IllegalFormatException
    *          If a format string contains an illegal syntax, a format
    *          specifier that is incompatible with the given arguments,
    *          insufficient arguments given the format string, or other
    *          illegal conditions.  For specification of all possible
    *          formatting errors, see the <a
    *          href="../util/Formatter.html#detail">Details</a>
    *          section of the formatter class specification.
    *
    * @throws IOError
    *         If an I/O error occurs.
    *
    * @return  A character array containing the password or passphrase read
    *          from the console, not including any line-termination characters,
    *          or <tt>null</tt> if an end of stream has been reached.
    */
    public char[] readPassword(String fmt, Object ... args) {
        throw new UnsupportedOperationException();
    }

   /**
    * Reads a password or passphrase from the console with echoing disabled
    *
    * @throws IOError
    *         If an I/O error occurs.
    *
    * @return  A character array containing the password or passphrase read
    *          from the console, not including any line-termination characters,
    *          or <tt>null</tt> if an end of stream has been reached.
    */
    public char[] readPassword() {
        throw new UnsupportedOperationException();
    }

    /**
     * Flushes the console and forces any buffered output to be written
     * immediately .
     */
    public void flush() {
        writer.flush();
    }

    private static class ConsoleReader extends BufferedReader {
        public ConsoleReader(InputStream in) throws UnsupportedEncodingException {
            super(new InputStreamReader(in, System.getProperty("file.encoding")), 256);
            lock = CONSOLE_LOCK;
        }

        @Override
        public void close() {
            // Console.reader cannot be closed.
        }
    }

    private static class ConsoleWriter extends PrintWriter {
        public ConsoleWriter(OutputStream out) {
            super(out, true);
            lock = CONSOLE_LOCK;
        }

        @Override
        public void close() {
            // Console.writer cannot be closed.
            flush();
        }
    }
}
