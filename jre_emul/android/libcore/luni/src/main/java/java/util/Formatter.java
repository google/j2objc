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

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.charset.Charset;
import libcore.icu.LocaleData;
import libcore.icu.NativeDecimalFormat;
import libcore.io.IoUtils;

/**
 * Formats arguments according to a format string (like {@code printf} in C).
 * <p>
 * It's relatively rare to use a {@code Formatter} directly. A variety of classes offer convenience
 * methods for accessing formatter functionality.
 * Of these, {@link String#format} is generally the most useful.
 * {@link java.io.PrintStream} and {@link java.io.PrintWriter} both offer
 * {@code format} and {@code printf} methods.
 * <p>
 * <i>Format strings</i> consist of plain text interspersed with format specifiers, such
 * as {@code "name: %s weight: %03dkg\n"}. Being a Java string, the usual Java string literal
 * backslash escapes are of course available.
 * <p>
 * <i>Format specifiers</i> (such as {@code "%s"} or {@code "%03d"} in the example) start with a
 * {@code %} and describe how to format their corresponding argument. It includes an optional
 * argument index, optional flags, an optional width, an optional precision, and a mandatory
 * conversion type.
 * In the example, {@code "%s"} has no flags, no width, and no precision, while
 * {@code "%03d"} has the flag {@code 0}, the width 3, and no precision.
 * <p>
 * Not all combinations of argument index, flags, width, precision, and conversion type
 * are valid.
 * <p>
 * <i>Argument index</i>. Normally, each format specifier consumes the next argument to
 * {@code format}.
 * For convenient localization, it's possible to reorder arguments so that they appear in a
 * different order in the output than the order in which they were supplied.
 * For example, {@code "%4$s"} formats the fourth argument ({@code 4$}) as a string ({@code s}).
 * It's also possible to reuse an argument with {@code <}. For example,
 * {@code format("%o %&lt;d %&lt;x", 64)} results in {@code "100 64 40"}.
 * <p>
 * <i>Flags</i>. The available flags are:
 * <p>
 * <table BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor"> <TD COLSPAN=4> <B>Flags</B> </TD> </tr>
 * <tr>
 * <td width="5%">{@code ,}</td>
 * <td width="25%">Use grouping separators for large numbers. (Decimal only.)</td>
 * <td width="30%">{@code format("%,d", 1024);}</td>
 * <td width="30%">{@code 1,234}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code +}</td>
 * <td width="25%">Always show sign. (Decimal only.)</td>
 * <td width="30%">{@code format("%+d, %+4d", 5, 5);}</td>
 * <td width="30%"><pre>+5,   +5</pre></td>
 * </tr>
 * <tr>
 * <td width="5%">{@code  }</td>
 * <td width="25%">A space indicates that non-negative numbers
 * should have a leading space. (Decimal only.)</td>
 * <td width="30%">{@code format("x% d% 5d", 4, 4);}</td>
 * <td width="30%"><pre>x 4    4</pre></td>
 * </tr>
 * <tr>
 * <td width="5%">{@code (}</td>
 * <td width="25%">Put parentheses around negative numbers. (Decimal only.)</td>
 * <td width="30%">{@code format("%(d, %(d, %(6d", 12, -12, -12);}</td>
 * <td width="30%"><pre>12, (12),   (12)</pre></td>
 * </tr>
 * <tr>
 * <td width="5%">{@code -}</td>
 * <td width="25%">Left-justify. (Requires width.)</td>
 * <td width="30%">{@code format("%-6dx", 5);}<br/>{@code format("%-3C, %3C", 'd', 0x65);}</td>
 * <td width="30%"><pre>5      x</pre><br/><pre>D  ,   E</pre></td>
 * </tr>
 * <tr>
 * <td width="5%">{@code 0}</td>
 * <td width="25%">Pad the number with leading zeros. (Requires width.)</td>
 * <td width="30%">{@code format("%07d, %03d", 4, 5555);}</td>
 * <td width="30%">{@code 0000004, 5555}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code #}</td>
 * <td width="25%">Alternate form. (Octal and hex only.) </td>
 * <td width="30%">{@code format("%o %#o", 010, 010);}<br/>{@code format("%x %#x", 0x12, 0x12);}</td>
 * <td width="30%">{@code 10 010}<br/>{@code 12 0x12}</td>
 * </tr>
 * </table>
 * <p>
 * <i>Width</i>. The width is a decimal integer specifying the minimum number of characters to be
 * used to represent the argument. If the result would otherwise be shorter than the width, padding
 * will be added (the exact details of which depend on the flags). Note that you can't use width to
 * truncate a field, only to make it wider: see precision for control over the maximum width.
 * <p>
 * <i>Precision</i>. The precision is a {@code .} followed by a decimal integer, giving the minimum
 * number of digits for {@code d}, {@code o}, {@code x}, or {@code X}; the minimum number of digits
 * after the decimal point for {@code a}, {@code A}, {@code e}, {@code E}, {@code f}, or {@code F};
 * the maximum number of significant digits for {@code g} or {@code G}; or the maximum number of
 * characters for {@code s} or {@code S}.
 * <p>
 * <i>Conversion type</i>. One or two characters describing how to interpret the argument. Most
 * conversions are a single character, but date/time conversions all start with {@code t} and
 * have a single extra character describing the desired output.
 * <p>
 * Many conversion types have a corresponding uppercase variant that converts its result to
 * uppercase using the rules of the relevant locale (either the default or the locale set for
 * this formatter).
 * <p>
 * This table shows the available single-character (non-date/time) conversion types:
 * <table BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TD COLSPAN=4>
 * <B>String conversions</B>
 * <br>
 * All types are acceptable arguments. Values of type {@link Formattable} have their
 * {@code formatTo} method invoked; all other types use {@code toString}.
 * </TD>
 * </tr>
 * <tr>
 * <td width="5%">{@code s}</td>
 * <td width="25%">String.</td>
 * <td width="30%">{@code format("%s %s", "hello", "Hello");}</td>
 * <td width="30%">{@code hello Hello}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code S}</td>
 * <td width="25%">Uppercase string.</td>
 * <td width="30%">{@code format("%S %S", "hello", "Hello");}</td>
 * <td width="30%">{@code HELLO HELLO}</td>
 * </tr>
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TD COLSPAN=4>
 * <B>Character conversions</B>
 * <br>
 * Byte, Character, Short, and Integer (and primitives that box to those types) are all acceptable
 * as character arguments. Any other type is an error.
 * </TD>
 * </tr>
 * <tr>
 * <td width="5%">{@code c}</td>
 * <td width="25%">Character.</td>
 * <td width="30%">{@code format("%c %c", 'd', 'E');}</td>
 * <td width="30%">{@code d E}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code C}</td>
 * <td width="25%">Uppercase character.</td>
 * <td width="30%">{@code format("%C %C", 'd', 'E');}</td>
 * <td width="30%">{@code D E}</td>
 * </tr>
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TD COLSPAN=4>
 * <B>Integer conversions</B>
 * <br>
 * Byte, Short, Integer, Long, and BigInteger (and primitives that box to those types) are all
 * acceptable as integer arguments. Any other type is an error.
 * </TD>
 * </tr>
 * <tr>
 * <td width="5%">{@code d}</td>
 * <td width="25%">Decimal.</td>
 * <td width="30%">{@code format("%d", 26);}</td>
 * <td width="30%">{@code 26}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code o}</td>
 * <td width="25%">Octal.</td>
 * <td width="30%">{@code format("%o", 032);}</td>
 * <td width="30%">{@code 32}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code x}, {@code X}</td>
 * <td width="25%">Hexadecimal.</td>
 * <td width="30%">{@code format("%x %X", 0x1a, 0x1a);}</td>
 * <td width="30%">{@code 1a 1A}</td>
 * </tr>
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TD COLSPAN=4><B>Floating-point conversions</B>
 * <br>
 * Float, Double, and BigDecimal (and primitives that box to those types) are all acceptable as
 * floating-point arguments. Any other type is an error.
 * </TD>
 * </tr>
 * <tr>
 * <td width="5%">{@code f}</td>
 * <td width="25%">Decimal floating point.</td>
 * <td width="30%"><pre>
format("%f", 123.456f);
format("%.1f", 123.456f);
format("%1.5f", 123.456f);
format("%10f", 123.456f);
format("%6.0f", 123.456f);</td>
 * <td width="30%" valign="top"><pre>
123.456001
123.5
123.45600
123.456001
&nbsp;&nbsp;&nbsp;123</pre></td>
 * </tr>
 * <tr>
 * <td width="5%">{@code e}, {@code E}</td>
 * <td width="25%">Engineering/exponential floating point.</td>
 * <td width="30%"><pre>
format("%e", 123.456f);
format("%.1e", 123.456f);
format("%1.5E", 123.456f);
format("%10E", 123.456f);
format("%6.0E", 123.456f);</td>
 * <td width="30%" valign="top"><pre>
1.234560e+02
1.2e+02
1.23456E+02
1.234560E+02
&nbsp;1E+02</pre></td>
 * </tr>
 * <tr>
 * <td width="5%" valign="top">{@code g}, {@code G}</td>
 * <td width="25%" valign="top">Decimal or engineering, depending on the magnitude of the value.</td>
 * <td width="30%" valign="top">{@code format("%g %g", 0.123, 0.0000123);}</td>
 * <td width="30%" valign="top">{@code 0.123000 1.23000e-05}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code a}, {@code A}</td>
 * <td width="25%">Hexadecimal floating point.</td>
 * <td width="30%">{@code format("%a", 123.456f);}</td>
 * <td width="30%">{@code 0x1.edd2f2p6}</td>
 * </tr>
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TD COLSPAN=4>
 * <B>Boolean conversion</B>
 * <br>
 * Accepts Boolean values. {@code null} is considered false, and instances of all other
 * types are considered true.
 * </TD>
 * </tr>
 * <tr>
 * <td width="5%">{@code b}, {@code B}</td>
 * <td width="25%">Boolean.</td>
 * <td width="30%">{@code format("%b %b", true, false);}<br>{@code format("%B %B", true, false);}<br>{@code format("%b", null);}<br>{@code format("%b", "hello");}</td>
 * <td width="30%">{@code true false}<br>{@code TRUE FALSE}<br>{@code false}<br>{@code true}</td>
 * </tr>
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TD COLSPAN=4>
 * <B>Hash code conversion</B>
 * <br>
 * Invokes {@code hashCode} on its argument, which may be of any type.
 * </TD>
 * </tr>
 * <tr>
 * <td width="5%">{@code h}, {@code H}</td>
 * <td width="25%">Hexadecimal hash code.</td>
 * <td width="30%">{@code format("%h", this);}<br>{@code format("%H", this);}<br>{@code format("%h", null);}</td>
 * <td width="30%">{@code 190d11}<br>{@code 190D11}<br>{@code null}</td>
 * </tr>
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TD COLSPAN=4>
 * <B>Zero-argument conversions</B></TD>
 * </tr>
 * <tr>
 * <td width="5%">{@code %}</td>
 * <td width="25%">A literal % character.</td>
 * <td width="30%">{@code format("%d%%", 50);}</td>
 * <td width="30%">{@code 50%}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code n}</td>
 * <td width="25%">Newline. (The value of {@link System#lineSeparator}.)</td>
 * <td width="30%">{@code format("first%nsecond");}</td>
 * <td width="30%">{@code first\nsecond}</td>
 * </tr>
 * </table>
 * <p>
 * It's also possible to format dates and times with {@code Formatter}, though you should
 * use {@link java.text.SimpleDateFormat} (probably via the factory methods in
 * {@link java.text.DateFormat}) instead.
 * The facilities offered by {@code Formatter} are low-level and place the burden of localization
 * on the developer. Using {@link java.text.DateFormat#getDateInstance},
 * {@link java.text.DateFormat#getTimeInstance}, and
 * {@link java.text.DateFormat#getDateTimeInstance} is preferable for dates and times that will be
 * presented to a human. Those methods will select the best format strings for the user's locale.
 * <p>
 * The best non-localized form is <a href="http://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a>,
 * which you can get with {@code "%tF"} (2010-01-22), {@code "%tF %tR"} (2010-01-22 13:39),
 * {@code "%tF %tT"} (2010-01-22 13:39:15), or {@code "%tF %tT%z"} (2010-01-22 13:39:15-0800).
 * <p>
 * This table shows the date/time conversions, but you should use {@link java.text.SimpleDateFormat}
 * instead:
 * <table BORDER="1" WIDTH="100%" CELLPADDING="3" CELLSPACING="0" SUMMARY="">
 * <tr BGCOLOR="#CCCCFF" CLASS="TableHeadingColor">
 * <TD COLSPAN=4><B>Date/time conversions</B>
 * <br>
 * Calendar, Date, and Long (representing milliseconds past the epoch) are all acceptable
 * as date/time arguments. Any other type is an error. The epoch is 1970-01-01 00:00:00 UTC.
 * <font color="red">Use {@link java.text.SimpleDateFormat} instead.</font>
 * </TD>
 * </tr>
 * <tr>
 * <td width="5%">{@code ta}</td>
 * <td width="25%">Localized weekday name (abbreviated).</td>
 * <td width="30%">{@code format("%ta", cal, cal);}</td>
 * <td width="30%">{@code Tue}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tA}</td>
 * <td width="25%">Localized weekday name (full).</td>
 * <td width="30%">{@code format("%tA", cal, cal);}</td>
 * <td width="30%">{@code Tuesday}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tb}</td>
 * <td width="25%">Localized month name (abbreviated).</td>
 * <td width="30%">{@code format("%tb", cal);}</td>
 * <td width="30%">{@code Apr}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tB}</td>
 * <td width="25%">Localized month name (full).</td>
 * <td width="30%">{@code format("%tB", cal);}</td>
 * <td width="30%">{@code April}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tc}</td>
 * <td width="25%">C library <i>asctime(3)</i>-like output. Do not use.</td>
 * <td width="30%">{@code format("%tc", cal);}</td>
 * <td width="30%">{@code Tue Apr 01 16:19:17 CEST 2008}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tC}</td>
 * <td width="25%">2-digit century.</td>
 * <td width="30%">{@code format("%tC", cal);}</td>
 * <td width="30%">{@code 20}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code td}</td>
 * <td width="25%">2-digit day of month (01-31).</td>
 * <td width="30%">{@code format("%td", cal);}</td>
 * <td width="30%">{@code 01}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tD}</td>
 * <td width="25%">Ambiguous US date format (MM/DD/YY). Do not use.</td>
 * <td width="30%">{@code format("%tD", cal);}</td>
 * <td width="30%">{@code 04/01/08}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code te}</td>
 * <td width="25%">Day of month (1-31).</td>
 * <td width="30%">{@code format("%te", cal);}</td>
 * <td width="30%">{@code 1}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tF}</td>
 * <td width="25%">Full date in ISO 8601 format (YYYY-MM-DD).</td>
 * <td width="30%">{@code format("%tF", cal);}</td>
 * <td width="30%">{@code 2008-04-01}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code th}</td>
 * <td width="25%">Synonym for {@code %tb}.</td>
 * <td width="30%"></td>
 * <td width="30%"></td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tH}</td>
 * <td width="25%">2-digit 24-hour hour of day (00-23).</td>
 * <td width="30%">{@code format("%tH", cal);}</td>
 * <td width="30%">{@code 16}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tI}</td>
 * <td width="25%">2-digit 12-hour hour of day (01-12).</td>
 * <td width="30%">{@code format("%tI", cal);}</td>
 * <td width="30%">{@code 04}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tj}</td>
 * <td width="25%">3-digit day of year (001-366).</td>
 * <td width="30%">{@code format("%tj", cal);}</td>
 * <td width="30%">{@code 092}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tk}</td>
 * <td width="25%">24-hour hour of day (0-23).</td>
 * <td width="30%">{@code format("%tk", cal);}</td>
 * <td width="30%">{@code 16}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tl}</td>
 * <td width="25%">12-hour hour of day (1-12).</td>
 * <td width="30%">{@code format("%tl", cal);}</td>
 * <td width="30%">{@code 4}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tL}</td>
 * <td width="25%">Milliseconds.</td>
 * <td width="30%">{@code format("%tL", cal);}</td>
 * <td width="30%">{@code 359}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tm}</td>
 * <td width="25%">2-digit month of year (01-12).</td>
 * <td width="30%">{@code format("%tm", cal);}</td>
 * <td width="30%">{@code 04}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tM}</td>
 * <td width="25%">2-digit minute.</td>
 * <td width="30%">{@code format("%tM", cal);}</td>
 * <td width="30%">{@code 08}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tN}</td>
 * <td width="25%">Nanoseconds.</td>
 * <td width="30%">{@code format("%tN", cal);}</td>
 * <td width="30%">{@code 359000000}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tp}</td>
 * <td width="25%">a.m. or p.m.</td>
 * <td width="30%">{@code format("%tp %Tp", cal, cal);}</td>
 * <td width="30%">{@code pm PM}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tQ}</td>
 * <td width="25%">Milliseconds since the epoch.</td>
 * <td width="30%">{@code format("%tQ", cal);}</td>
 * <td width="30%">{@code 1207059412656}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tr}</td>
 * <td width="25%">Full 12-hour time ({@code %tI:%tM:%tS %Tp}).</td>
 * <td width="30%">{@code format("%tr", cal);}</td>
 * <td width="30%">{@code 04:15:32 PM}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tR}</td>
 * <td width="25%">Short 24-hour time ({@code %tH:%tM}).</td>
 * <td width="30%">{@code format("%tR", cal);}</td>
 * <td width="30%">{@code 16:15}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code ts}</td>
 * <td width="25%">Seconds since the epoch.</td>
 * <td width="30%">{@code format("%ts", cal);}</td>
 * <td width="30%">{@code 1207059412}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tS}</td>
 * <td width="25%">2-digit seconds (00-60).</td>
 * <td width="30%">{@code format("%tS", cal);}</td>
 * <td width="30%">{@code 17}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tT}</td>
 * <td width="25%">Full 24-hour time ({@code %tH:%tM:%tS}).</td>
 * <td width="30%">{@code format("%tT", cal);}</td>
 * <td width="30%">{@code 16:15:32}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code ty}</td>
 * <td width="25%">2-digit year (00-99).</td>
 * <td width="30%">{@code format("%ty", cal);}</td>
 * <td width="30%">{@code 08}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tY}</td>
 * <td width="25%">4-digit year.</td>
 * <td width="30%">{@code format("%tY", cal);}</td>
 * <td width="30%">{@code 2008}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tz}</td>
 * <td width="25%">Time zone GMT offset.</td>
 * <td width="30%">{@code format("%tz", cal);}</td>
 * <td width="30%">{@code +0100}</td>
 * </tr>
 * <tr>
 * <td width="5%">{@code tZ}</td>
 * <td width="25%">Localized time zone abbreviation.</td>
 * <td width="30%">{@code format("%tZ", cal);}</td>
 * <td width="30%">{@code CEST}</td>
 * </tr>
 * </table>
 * <p>
 * As with the other conversions, date/time conversion has an uppercase format. Replacing
 * {@code %t} with {@code %T} will uppercase the field according to the rules of the formatter's
 * locale.
 * <p><i>Number localization</i>. Some conversions use localized decimal digits rather than the
 * usual ASCII digits. So formatting {@code 123} with {@code %d} will give 123 in English locales
 * but &#x0661;&#x0662;&#x0663; in appropriate Arabic locales, for example. This number localization
 * occurs for the decimal integer conversion {@code %d}, the floating point conversions {@code %e},
 * {@code %f}, and {@code %g}, and all date/time {@code %t} or {@code %T} conversions, but no other
 * conversions.
 * <p><i>Thread safety</i>. Formatter is not thread-safe.
 *
 * @since 1.5
 * @see java.text.DateFormat
 * @see Formattable
 * @see java.text.SimpleDateFormat
 */
public final class Formatter implements Closeable, Flushable {
    private static final char[] ZEROS = new char[] { '0', '0', '0', '0', '0', '0', '0', '0', '0' };

    /**
     * The enumeration giving the available styles for formatting very large
     * decimal numbers.
     */
    public enum BigDecimalLayoutForm {
        /**
         * Use scientific style for BigDecimals.
         */
        SCIENTIFIC,
        /**
         * Use normal decimal/float style for BigDecimals.
         */
        DECIMAL_FLOAT
    }

    // User-settable parameters.
    private Appendable out;
    private Locale locale;

    // Implementation details.
    private Object arg;
    private boolean closed = false;
    private FormatToken formatToken;
    private IOException lastIOException;
    private LocaleData localeData;

    private static class CachedDecimalFormat {
        public NativeDecimalFormat decimalFormat;
        public LocaleData currentLocaleData;
        public String currentPattern;

        public CachedDecimalFormat() {
        }

        public NativeDecimalFormat update(LocaleData localeData, String pattern) {
            if (decimalFormat == null) {
                currentPattern = pattern;
                currentLocaleData = localeData;
                decimalFormat = new NativeDecimalFormat(currentPattern, currentLocaleData);
            }
            if (!pattern.equals(currentPattern)) {
                decimalFormat.applyPattern(pattern);
                currentPattern = pattern;
            }
            if (localeData != currentLocaleData) {
                decimalFormat.setDecimalFormatSymbols(localeData);
                currentLocaleData = localeData;
            }
            return decimalFormat;
        }
    }

    private static final ThreadLocal<CachedDecimalFormat> cachedDecimalFormat = new ThreadLocal<CachedDecimalFormat>() {
        @Override protected CachedDecimalFormat initialValue() {
            return new CachedDecimalFormat();
        }
    };

    /**
     * Creates a native peer if we don't already have one, or reconfigures an existing one.
     * This means we get to reuse the peer in cases like "x=%.2f y=%.2f".
     */
    private NativeDecimalFormat getDecimalFormat(String pattern) {
        return cachedDecimalFormat.get().update(localeData, pattern);
    }

    /**
     * Constructs a {@code Formatter}.
     *
     * <p>The output is written to a {@code StringBuilder} which can be acquired by invoking
     * {@link #out()} and whose content can be obtained by calling {@code toString}.
     *
     * <p>The {@code Locale} used is the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     */
    public Formatter() {
        this(new StringBuilder(), Locale.getDefault());
    }

    /**
     * Constructs a {@code Formatter} whose output will be written to the
     * specified {@code Appendable}.
     *
     * <p>The {@code Locale} used is the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     *
     * @param a
     *            the output destination of the {@code Formatter}. If {@code a} is {@code null},
     *            then a {@code StringBuilder} will be used.
     */
    public Formatter(Appendable a) {
        this(a, Locale.getDefault());
    }

    /**
     * Constructs a {@code Formatter} with the specified {@code Locale}.
     *
     * <p>The output is written to a {@code StringBuilder} which can be acquired by invoking
     * {@link #out()} and whose content can be obtained by calling {@code toString}.
     *
     * @param l
     *            the {@code Locale} of the {@code Formatter}. If {@code l} is {@code null},
     *            then no localization will be used.
     */
    public Formatter(Locale l) {
        this(new StringBuilder(), l);
    }

    /**
     * Constructs a {@code Formatter} with the specified {@code Locale}
     * and whose output will be written to the
     * specified {@code Appendable}.
     *
     * @param a
     *            the output destination of the {@code Formatter}. If {@code a} is {@code null},
     *            then a {@code StringBuilder} will be used.
     * @param l
     *            the {@code Locale} of the {@code Formatter}. If {@code l} is {@code null},
     *            then no localization will be used.
     */
    public Formatter(Appendable a, Locale l) {
        if (a == null) {
            out = new StringBuilder();
        } else {
            out = a;
        }
        locale = l;
    }

    /**
     * Constructs a {@code Formatter} whose output is written to the specified file.
     *
     * <p>The charset of the {@code Formatter} is the default charset.
     *
     * <p>The {@code Locale} used is the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     *
     * @param fileName
     *            the filename of the file that is used as the output
     *            destination for the {@code Formatter}. The file will be truncated to
     *            zero size if the file exists, or else a new file will be
     *            created. The output of the {@code Formatter} is buffered.
     * @throws FileNotFoundException
     *             if the filename does not denote a normal and writable file,
     *             or if a new file cannot be created, or if any error arises when
     *             opening or creating the file.
     */
    public Formatter(String fileName) throws FileNotFoundException {
        this(new File(fileName));

    }

    /**
     * Constructs a {@code Formatter} whose output is written to the specified file.
     *
     * <p>The {@code Locale} used is the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     *
     * @param fileName
     *            the filename of the file that is used as the output
     *            destination for the {@code Formatter}. The file will be truncated to
     *            zero size if the file exists, or else a new file will be
     *            created. The output of the {@code Formatter} is buffered.
     * @param csn
     *            the name of the charset for the {@code Formatter}.
     * @throws FileNotFoundException
     *             if the filename does not denote a normal and writable file,
     *             or if a new file cannot be created, or if any error arises when
     *             opening or creating the file.
     * @throws UnsupportedEncodingException
     *             if the charset with the specified name is not supported.
     */
    public Formatter(String fileName, String csn) throws FileNotFoundException,
            UnsupportedEncodingException {
        this(new File(fileName), csn);
    }

    /**
     * Constructs a {@code Formatter} with the given {@code Locale} and charset,
     * and whose output is written to the specified file.
     *
     * @param fileName
     *            the filename of the file that is used as the output
     *            destination for the {@code Formatter}. The file will be truncated to
     *            zero size if the file exists, or else a new file will be
     *            created. The output of the {@code Formatter} is buffered.
     * @param csn
     *            the name of the charset for the {@code Formatter}.
     * @param l
     *            the {@code Locale} of the {@code Formatter}. If {@code l} is {@code null},
     *            then no localization will be used.
     * @throws FileNotFoundException
     *             if the filename does not denote a normal and writable file,
     *             or if a new file cannot be created, or if any error arises when
     *             opening or creating the file.
     * @throws UnsupportedEncodingException
     *             if the charset with the specified name is not supported.
     */
    public Formatter(String fileName, String csn, Locale l)
            throws FileNotFoundException, UnsupportedEncodingException {

        this(new File(fileName), csn, l);
    }

    /**
     * Constructs a {@code Formatter} whose output is written to the specified {@code File}.
     *
     * The charset of the {@code Formatter} is the default charset.
     *
     * <p>The {@code Locale} used is the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     *
     * @param file
     *            the {@code File} that is used as the output destination for the
     *            {@code Formatter}. The {@code File} will be truncated to zero size if the {@code File}
     *            exists, or else a new {@code File} will be created. The output of the
     *            {@code Formatter} is buffered.
     * @throws FileNotFoundException
     *             if the {@code File} is not a normal and writable {@code File}, or if a
     *             new {@code File} cannot be created, or if any error rises when opening or
     *             creating the {@code File}.
     */
    public Formatter(File file) throws FileNotFoundException {
        this(new FileOutputStream(file));
    }

    /**
     * Constructs a {@code Formatter} with the given charset,
     * and whose output is written to the specified {@code File}.
     *
     * <p>The {@code Locale} used is the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     *
     * @param file
     *            the {@code File} that is used as the output destination for the
     *            {@code Formatter}. The {@code File} will be truncated to zero size if the {@code File}
     *            exists, or else a new {@code File} will be created. The output of the
     *            {@code Formatter} is buffered.
     * @param csn
     *            the name of the charset for the {@code Formatter}.
     * @throws FileNotFoundException
     *             if the {@code File} is not a normal and writable {@code File}, or if a
     *             new {@code File} cannot be created, or if any error rises when opening or
     *             creating the {@code File}.
     * @throws UnsupportedEncodingException
     *             if the charset with the specified name is not supported.
     */
    public Formatter(File file, String csn) throws FileNotFoundException,
            UnsupportedEncodingException {
        this(file, csn, Locale.getDefault());
    }

    /**
     * Constructs a {@code Formatter} with the given {@code Locale} and charset,
     * and whose output is written to the specified {@code File}.
     *
     * @param file
     *            the {@code File} that is used as the output destination for the
     *            {@code Formatter}. The {@code File} will be truncated to zero size if the {@code File}
     *            exists, or else a new {@code File} will be created. The output of the
     *            {@code Formatter} is buffered.
     * @param csn
     *            the name of the charset for the {@code Formatter}.
     * @param l
     *            the {@code Locale} of the {@code Formatter}. If {@code l} is {@code null},
     *            then no localization will be used.
     * @throws FileNotFoundException
     *             if the {@code File} is not a normal and writable {@code File}, or if a
     *             new {@code File} cannot be created, or if any error rises when opening or
     *             creating the {@code File}.
     * @throws UnsupportedEncodingException
     *             if the charset with the specified name is not supported.
     */
    public Formatter(File file, String csn, Locale l)
            throws FileNotFoundException, UnsupportedEncodingException {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(file);
            out = new BufferedWriter(new OutputStreamWriter(fout, csn));
        } catch (RuntimeException e) {
            IoUtils.closeQuietly(fout);
            throw e;
        } catch (UnsupportedEncodingException e) {
            IoUtils.closeQuietly(fout);
            throw e;
        }

        locale = l;
    }

    /**
     * Constructs a {@code Formatter} whose output is written to the specified {@code OutputStream}.
     *
     * <p>The charset of the {@code Formatter} is the default charset.
     *
     * <p>The {@code Locale} used is the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     *
     * @param os
     *            the stream to be used as the destination of the {@code Formatter}.
     */
    public Formatter(OutputStream os) {
        out = new BufferedWriter(new OutputStreamWriter(os, Charset.defaultCharset()));
        locale = Locale.getDefault();
    }

    /**
     * Constructs a {@code Formatter} with the given charset,
     * and whose output is written to the specified {@code OutputStream}.
     *
     * <p>The {@code Locale} used is the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     *
     * @param os
     *            the stream to be used as the destination of the {@code Formatter}.
     * @param csn
     *            the name of the charset for the {@code Formatter}.
     * @throws UnsupportedEncodingException
     *             if the charset with the specified name is not supported.
     */
    public Formatter(OutputStream os, String csn) throws UnsupportedEncodingException {
        this(os, csn, Locale.getDefault());
    }

    /**
     * Constructs a {@code Formatter} with the given {@code Locale} and charset,
     * and whose output is written to the specified {@code OutputStream}.
     *
     * @param os
     *            the stream to be used as the destination of the {@code Formatter}.
     * @param csn
     *            the name of the charset for the {@code Formatter}.
     * @param l
     *            the {@code Locale} of the {@code Formatter}. If {@code l} is {@code null},
     *            then no localization will be used.
     * @throws UnsupportedEncodingException
     *             if the charset with the specified name is not supported.
     */
    public Formatter(OutputStream os, String csn, Locale l) throws UnsupportedEncodingException {
        out = new BufferedWriter(new OutputStreamWriter(os, csn));
        locale = l;
    }

    /**
     * Constructs a {@code Formatter} whose output is written to the specified {@code PrintStream}.
     *
     * <p>The charset of the {@code Formatter} is the default charset.
     *
     * <p>The {@code Locale} used is the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     *
     * @param ps
     *            the {@code PrintStream} used as destination of the {@code Formatter}. If
     *            {@code ps} is {@code null}, then a {@code NullPointerException} will
     *            be raised.
     */
    public Formatter(PrintStream ps) {
        if (ps == null) {
            throw new NullPointerException("ps == null");
        }
        out = ps;
        locale = Locale.getDefault();
    }

    private void checkNotClosed() {
        if (closed) {
            throw new FormatterClosedException();
        }
    }

    /**
     * Returns the {@code Locale} of the {@code Formatter}.
     *
     * @return the {@code Locale} for the {@code Formatter} or {@code null} for no {@code Locale}.
     * @throws FormatterClosedException
     *             if the {@code Formatter} has been closed.
     */
    public Locale locale() {
        checkNotClosed();
        return locale;
    }

    /**
     * Returns the output destination of the {@code Formatter}.
     *
     * @return the output destination of the {@code Formatter}.
     * @throws FormatterClosedException
     *             if the {@code Formatter} has been closed.
     */
    public Appendable out() {
        checkNotClosed();
        return out;
    }

    /**
     * Returns the content by calling the {@code toString()} method of the output
     * destination.
     *
     * @return the content by calling the {@code toString()} method of the output
     *         destination.
     * @throws FormatterClosedException
     *             if the {@code Formatter} has been closed.
     */
    @Override
    public String toString() {
        checkNotClosed();
        return out.toString();
    }

    /**
     * Flushes the {@code Formatter}. If the output destination is {@link Flushable},
     * then the method {@code flush()} will be called on that destination.
     *
     * @throws FormatterClosedException
     *             if the {@code Formatter} has been closed.
     */
    public void flush() {
        checkNotClosed();
        if (out instanceof Flushable) {
            try {
                ((Flushable) out).flush();
            } catch (IOException e) {
                lastIOException = e;
            }
        }
    }

    /**
     * Closes the {@code Formatter}. If the output destination is {@link Closeable},
     * then the method {@code close()} will be called on that destination.
     *
     * If the {@code Formatter} has been closed, then calling the this method will have no
     * effect.
     *
     * Any method but the {@link #ioException()} that is called after the
     * {@code Formatter} has been closed will raise a {@code FormatterClosedException}.
     */
    public void close() {
        if (!closed) {
            closed = true;
            try {
                if (out instanceof Closeable) {
                    ((Closeable) out).close();
                }
            } catch (IOException e) {
                lastIOException = e;
            }
        }
    }

    /**
     * Returns the last {@code IOException} thrown by the {@code Formatter}'s output
     * destination. If the {@code append()} method of the destination does not throw
     * {@code IOException}s, the {@code ioException()} method will always return {@code null}.
     *
     * @return the last {@code IOException} thrown by the {@code Formatter}'s output
     *         destination.
     */
    public IOException ioException() {
        return lastIOException;
    }

    /**
     * Writes a formatted string to the output destination of the {@code Formatter}.
     *
     * @param format
     *            a format string.
     * @param args
     *            the arguments list used in the {@code format()} method. If there are
     *            more arguments than those specified by the format string, then
     *            the additional arguments are ignored.
     * @return this {@code Formatter}.
     * @throws IllegalFormatException
     *             if the format string is illegal or incompatible with the
     *             arguments, or if fewer arguments are sent than those required by
     *             the format string, or any other illegal situation.
     * @throws FormatterClosedException
     *             if the {@code Formatter} has been closed.
     */
    public Formatter format(String format, Object... args) {
        return format(this.locale, format, args);
    }

    /**
     * Writes a formatted string to the output destination of the {@code Formatter}.
     *
     * @param l
     *            the {@code Locale} used in the method. If {@code locale} is
     *            {@code null}, then no localization will be applied. This
     *            parameter does not change this Formatter's default {@code Locale}
     *            as specified during construction, and only applies for the
     *            duration of this call.
     * @param format
     *            a format string.
     * @param args
     *            the arguments list used in the {@code format()} method. If there are
     *            more arguments than those specified by the format string, then
     *            the additional arguments are ignored.
     * @return this {@code Formatter}.
     * @throws IllegalFormatException
     *             if the format string is illegal or incompatible with the
     *             arguments, or if fewer arguments are sent than those required by
     *             the format string, or any other illegal situation.
     * @throws FormatterClosedException
     *             if the {@code Formatter} has been closed.
     */
    public Formatter format(Locale l, String format, Object... args) {
        Locale originalLocale = locale;
        try {
            this.locale = (l == null ? Locale.US : l);
            this.localeData = LocaleData.get(locale);
            doFormat(format, args);
        } finally {
            this.locale = originalLocale;
        }
        return this;
    }

    private void doFormat(String format, Object... args) {
        checkNotClosed();

        FormatSpecifierParser fsp = new FormatSpecifierParser(format);
        int currentObjectIndex = 0;
        Object lastArgument = null;
        boolean hasLastArgumentSet = false;

        int length = format.length();
        int i = 0;
        while (i < length) {
            // Find the maximal plain-text sequence...
            int plainTextStart = i;
            int nextPercent = format.indexOf('%', i);
            int plainTextEnd = (nextPercent == -1) ? length : nextPercent;
            // ...and output it.
            if (plainTextEnd > plainTextStart) {
                outputCharSequence(format, plainTextStart, plainTextEnd);
            }
            i = plainTextEnd;
            // Do we have a format specifier?
            if (i < length) {
                FormatToken token = fsp.parseFormatToken(i + 1);

                Object argument = null;
                if (token.requireArgument()) {
                    int index = token.getArgIndex() == FormatToken.UNSET ? currentObjectIndex++ : token.getArgIndex();
                    argument = getArgument(args, index, fsp, lastArgument, hasLastArgumentSet);
                    lastArgument = argument;
                    hasLastArgumentSet = true;
                }

                CharSequence substitution = transform(token, argument);
                // The substitution is null if we called Formattable.formatTo.
                if (substitution != null) {
                    outputCharSequence(substitution, 0, substitution.length());
                }
                i = fsp.i;
            }
        }
    }

    // Fixes http://code.google.com/p/android/issues/detail?id=1767.
    private void outputCharSequence(CharSequence cs, int start, int end) {
        try {
            out.append(cs, start, end);
        } catch (IOException e) {
            lastIOException = e;
        }
    }

    private Object getArgument(Object[] args, int index, FormatSpecifierParser fsp,
            Object lastArgument, boolean hasLastArgumentSet) {
        if (index == FormatToken.LAST_ARGUMENT_INDEX && !hasLastArgumentSet) {
            throw new MissingFormatArgumentException("<");
        }

        if (args == null) {
            return null;
        }

        if (index >= args.length) {
            throw new MissingFormatArgumentException(fsp.getFormatSpecifierText());
        }

        if (index == FormatToken.LAST_ARGUMENT_INDEX) {
            return lastArgument;
        }

        return args[index];
    }

    /*
     * Complete details of a single format specifier parsed from a format string.
     */
    private static class FormatToken {
        static final int LAST_ARGUMENT_INDEX = -2;

        static final int UNSET = -1;

        static final int FLAGS_UNSET = 0;

        static final int DEFAULT_PRECISION = 6;

        static final int FLAG_ZERO = 1 << 4;

        private int argIndex = UNSET;

        // These have package access for performance. They used to be represented by an int bitmask
        // and accessed via methods, but Android's JIT doesn't yet do a good job of such code.
        // Direct field access, on the other hand, is fast.
        boolean flagComma;
        boolean flagMinus;
        boolean flagParenthesis;
        boolean flagPlus;
        boolean flagSharp;
        boolean flagSpace;
        boolean flagZero;

        private char conversionType = (char) UNSET;
        private char dateSuffix;

        private int precision = UNSET;
        private int width = UNSET;

        private StringBuilder strFlags;

        // Tests whether there were no flags, no width, and no precision specified.
        boolean isDefault() {
            return !flagComma && !flagMinus && !flagParenthesis && !flagPlus && !flagSharp &&
                    !flagSpace && !flagZero && width == UNSET && precision == UNSET;
        }

        boolean isPrecisionSet() {
            return precision != UNSET;
        }

        int getArgIndex() {
            return argIndex;
        }

        void setArgIndex(int index) {
            argIndex = index;
        }

        int getWidth() {
            return width;
        }

        void setWidth(int width) {
            this.width = width;
        }

        int getPrecision() {
            return precision;
        }

        void setPrecision(int precise) {
            this.precision = precise;
        }

        String getStrFlags() {
            return (strFlags != null) ? strFlags.toString() : "";
        }

        /*
         * Sets qualified char as one of the flags. If the char is qualified,
         * sets it as a flag and returns true. Or else returns false.
         */
        boolean setFlag(int ch) {
            boolean dupe = false;
            switch (ch) {
            case ',':
                dupe = flagComma;
                flagComma = true;
                break;
            case '-':
                dupe = flagMinus;
                flagMinus = true;
                break;
            case '(':
                dupe = flagParenthesis;
                flagParenthesis = true;
                break;
            case '+':
                dupe = flagPlus;
                flagPlus = true;
                break;
            case '#':
                dupe = flagSharp;
                flagSharp = true;
                break;
            case ' ':
                dupe = flagSpace;
                flagSpace = true;
                break;
            case '0':
                dupe = flagZero;
                flagZero = true;
                break;
            default:
                return false;
            }
            if (dupe) {
                // The RI documentation implies we're supposed to report all the flags, not just
                // the first duplicate, but the RI behaves the same as we do.
                throw new DuplicateFormatFlagsException(String.valueOf(ch));
            }
            if (strFlags == null) {
                strFlags = new StringBuilder(7); // There are seven possible flags.
            }
            strFlags.append((char) ch);
            return true;
        }

        char getConversionType() {
            return conversionType;
        }

        void setConversionType(char c) {
            conversionType = c;
        }

        char getDateSuffix() {
            return dateSuffix;
        }

        void setDateSuffix(char c) {
            dateSuffix = c;
        }

        boolean requireArgument() {
            return conversionType != '%' && conversionType != 'n';
        }

        void checkFlags(Object arg) {
            // Work out which flags are allowed.
            boolean allowComma = false;
            boolean allowMinus = true;
            boolean allowParenthesis = false;
            boolean allowPlus = false;
            boolean allowSharp = false;
            boolean allowSpace = false;
            boolean allowZero = false;
            // Precision and width?
            boolean allowPrecision = true;
            boolean allowWidth = true;
            // Argument?
            boolean allowArgument = true;
            switch (conversionType) {
            // Character and date/time.
            case 'c': case 'C': case 't': case 'T':
                // Only '-' is allowed.
                allowPrecision = false;
                break;

            // String.
            case 's': case 'S':
                if (arg instanceof Formattable) {
                    allowSharp = true;
                }
                break;

            // Floating point.
            case 'g': case 'G':
                allowComma = allowParenthesis = allowPlus = allowSpace = allowZero = true;
                break;
            case 'f':
                allowComma = allowParenthesis = allowPlus = allowSharp = allowSpace = allowZero = true;
                break;
            case 'e': case 'E':
                allowParenthesis = allowPlus = allowSharp = allowSpace = allowZero = true;
                break;
            case 'a': case 'A':
                allowPlus = allowSharp = allowSpace = allowZero = true;
                break;

            // Integral.
            case 'd':
                allowComma = allowParenthesis = allowPlus = allowSpace = allowZero = true;
                allowPrecision = false;
                break;
            case 'o': case 'x': case 'X':
                allowSharp = allowZero = true;
                if (arg == null || arg instanceof BigInteger) {
                    allowParenthesis = allowPlus = allowSpace = true;
                }
                allowPrecision = false;
                break;

            // Special.
            case 'n':
                // Nothing is allowed.
                allowMinus = false;
                allowArgument = allowPrecision = allowWidth = false;
                break;
            case '%':
                // The only flag allowed is '-', and no argument or precision is allowed.
                allowArgument = false;
                allowPrecision = false;
                break;

            // Booleans and hash codes.
            case 'b': case 'B': case 'h': case 'H':
                break;

            default:
                throw unknownFormatConversionException();
            }

            // Check for disallowed flags.
            String mismatch = null;
            if (!allowComma && flagComma) {
                mismatch = ",";
            } else if (!allowMinus && flagMinus) {
                mismatch = "-";
            } else if (!allowParenthesis && flagParenthesis) {
                mismatch = "(";
            } else if (!allowPlus && flagPlus) {
                mismatch = "+";
            } else if (!allowSharp && flagSharp) {
                mismatch = "#";
            } else if (!allowSpace && flagSpace) {
                mismatch = " ";
            } else if (!allowZero && flagZero) {
                mismatch = "0";
            }
            if (mismatch != null) {
                if (conversionType == 'n') {
                    // For no good reason, %n is a special case...
                    throw new IllegalFormatFlagsException(mismatch);
                } else {
                    throw new FormatFlagsConversionMismatchException(mismatch, conversionType);
                }
            }

            // Check for a missing width with flags that require a width.
            if ((flagMinus || flagZero) && width == UNSET) {
                throw new MissingFormatWidthException("-" + conversionType);
            }

            // Check that no-argument conversion types don't have an argument.
            // Note: the RI doesn't enforce this.
            if (!allowArgument && argIndex != UNSET) {
                throw new IllegalFormatFlagsException("%" + conversionType +
                        " doesn't take an argument");
            }

            // Check that we don't have a precision or width where they're not allowed.
            if (!allowPrecision && precision != UNSET) {
                throw new IllegalFormatPrecisionException(precision);
            }
            if (!allowWidth && width != UNSET) {
                throw new IllegalFormatWidthException(width);
            }

            // Some combinations make no sense...
            if (flagPlus && flagSpace) {
                throw new IllegalFormatFlagsException("the '+' and ' ' flags are incompatible");
            }
            if (flagMinus && flagZero) {
                throw new IllegalFormatFlagsException("the '-' and '0' flags are incompatible");
            }
        }

        public UnknownFormatConversionException unknownFormatConversionException() {
            if (conversionType == 't' || conversionType == 'T') {
                throw new UnknownFormatConversionException(String.format("%c%c",
                        conversionType, dateSuffix));
            }
            throw new UnknownFormatConversionException(String.valueOf(conversionType));
        }
    }

    /*
     * Gets the formatted string according to the format token and the
     * argument.
     */
    private CharSequence transform(FormatToken token, Object argument) {
        this.formatToken = token;
        this.arg = argument;

        // There are only two format specifiers that matter: "%d" and "%s".
        // Nothing else is common in the wild. We fast-path these two to
        // avoid the heavyweight machinery needed to cope with flags, width,
        // and precision.
        if (token.isDefault()) {
            switch (token.getConversionType()) {
            case 's':
                if (arg == null) {
                    return "null";
                } else if (!(arg instanceof Formattable)) {
                    return arg.toString();
                }
                break;
            case 'd':
                boolean needLocalizedDigits = (localeData.zeroDigit != '0');
                if (out instanceof StringBuilder && !needLocalizedDigits) {
                    if (arg instanceof Integer || arg instanceof Short || arg instanceof Byte) {
                        IntegralToString.appendInt((StringBuilder) out, ((Number) arg).intValue());
                        return null;
                    } else if (arg instanceof Long) {
                        IntegralToString.appendLong((StringBuilder) out, ((Long) arg).longValue());
                        return null;
                    }
                }
                if (arg instanceof Integer || arg instanceof Long || arg instanceof Short || arg instanceof Byte) {
                    String result = arg.toString();
                    return needLocalizedDigits ? localizeDigits(result) : result;
                }
            }
        }

        formatToken.checkFlags(arg);
        CharSequence result;
        switch (token.getConversionType()) {
        case 'B': case 'b':
            result = transformFromBoolean();
            break;
        case 'H': case 'h':
            result = transformFromHashCode();
            break;
        case 'S': case 's':
            result = transformFromString();
            break;
        case 'C': case 'c':
            result = transformFromCharacter();
            break;
        case 'd': case 'o': case 'x': case 'X':
            if (arg == null || arg instanceof BigInteger) {
                result = transformFromBigInteger();
            } else {
                result = transformFromInteger();
            }
            break;
        case 'A': case 'a': case 'E': case 'e': case 'f': case 'G': case 'g':
            result = transformFromFloat();
            break;
        case '%':
            result = transformFromPercent();
            break;
        case 'n':
            result = System.lineSeparator();
            break;
        case 't': case 'T':
            result = transformFromDateTime();
            break;
        default:
            throw token.unknownFormatConversionException();
        }

        if (Character.isUpperCase(token.getConversionType())) {
            if (result != null) {
                result = result.toString().toUpperCase(locale);
            }
        }
        return result;
    }

    private IllegalFormatConversionException badArgumentType() {
        throw new IllegalFormatConversionException(formatToken.getConversionType(), arg.getClass());
    }

    /**
     * Returns a CharSequence corresponding to {@code s} with all the ASCII digits replaced
     * by digits appropriate to this formatter's locale. Other characters remain unchanged.
     */
    private CharSequence localizeDigits(CharSequence s) {
        int length = s.length();
        int offsetToLocalizedDigits = localeData.zeroDigit - '0';
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; ++i) {
            char ch = s.charAt(i);
            if (ch >= '0' && ch <= '9') {
                ch += offsetToLocalizedDigits;
            }
            result.append(ch);
        }
        return result;
    }

    /**
     * Inserts the grouping separator every 3 digits. DecimalFormat lets you configure grouping
     * size, but you can't access that from Formatter, and the default is every 3 digits.
     */
    private CharSequence insertGrouping(CharSequence s) {
        StringBuilder result = new StringBuilder(s.length() + s.length()/3);

        // A leading '-' doesn't want to be included in the grouping.
        int digitsLength = s.length();
        int i = 0;
        if (s.charAt(0) == '-') {
            --digitsLength;
            ++i;
            result.append('-');
        }

        // Append the digits that come before the first separator.
        int headLength = digitsLength % 3;
        if (headLength == 0) {
            headLength = 3;
        }
        result.append(s, i, i + headLength);
        i += headLength;

        // Append the remaining groups.
        for (; i < s.length(); i += 3) {
            result.append(localeData.groupingSeparator);
            result.append(s, i, i + 3);
        }
        return result;
    }

    private CharSequence transformFromBoolean() {
        CharSequence result;
        if (arg instanceof Boolean) {
            result = arg.toString();
        } else if (arg == null) {
            result = "false";
        } else {
            result = "true";
        }
        return padding(result, 0);
    }

    private CharSequence transformFromHashCode() {
        CharSequence result;
        if (arg == null) {
            result = "null";
        } else {
            result = Integer.toHexString(arg.hashCode());
        }
        return padding(result, 0);
    }

    private CharSequence transformFromString() {
        if (arg instanceof Formattable) {
            int flags = 0;
            if (formatToken.flagMinus) {
                flags |= FormattableFlags.LEFT_JUSTIFY;
            }
            if (formatToken.flagSharp) {
                flags |= FormattableFlags.ALTERNATE;
            }
            if (Character.isUpperCase(formatToken.getConversionType())) {
                flags |= FormattableFlags.UPPERCASE;
            }
            ((Formattable) arg).formatTo(this, flags, formatToken.getWidth(),
                    formatToken.getPrecision());
            // all actions have been taken out in the
            // Formattable.formatTo, thus there is nothing to do, just
            // returns null, which tells the Parser to add nothing to the
            // output.
            return null;
        }
        CharSequence result = arg != null ? arg.toString() : "null";
        return padding(result, 0);
    }

    private CharSequence transformFromCharacter() {
        if (arg == null) {
            return padding("null", 0);
        }
        if (arg instanceof Character) {
            return padding(String.valueOf(arg), 0);
        } else if (arg instanceof Byte || arg instanceof Short || arg instanceof Integer) {
            int codePoint = ((Number) arg).intValue();
            if (!Character.isValidCodePoint(codePoint)) {
                throw new IllegalFormatCodePointException(codePoint);
            }
            CharSequence result = (codePoint < Character.MIN_SUPPLEMENTARY_CODE_POINT)
                    ? String.valueOf((char) codePoint)
                    : String.valueOf(Character.toChars(codePoint));
            return padding(result, 0);
        } else {
            throw badArgumentType();
        }
    }

    private CharSequence transformFromPercent() {
        return padding("%", 0);
    }

    private CharSequence padding(CharSequence source, int startIndex) {
        int start = startIndex;
        int width = formatToken.getWidth();
        int precision = formatToken.getPrecision();

        int length = source.length();
        if (precision >= 0) {
            length = Math.min(length, precision);
            if (source instanceof StringBuilder) {
                ((StringBuilder) source).setLength(length);
            } else {
                source = source.subSequence(0, length);
            }
        }
        if (width > 0) {
            width = Math.max(source.length(), width);
        }
        if (length >= width) {
            return source;
        }

        char paddingChar = '\u0020'; // space as padding char.
        if (formatToken.flagZero) {
            if (formatToken.getConversionType() == 'd') {
                paddingChar = localeData.zeroDigit;
            } else {
                paddingChar = '0'; // No localized digits for bases other than decimal.
            }
        } else {
            // if padding char is space, always pad from the start.
            start = 0;
        }
        char[] paddingChars = new char[width - length];
        Arrays.fill(paddingChars, paddingChar);

        boolean paddingRight = formatToken.flagMinus;
        StringBuilder result = toStringBuilder(source);
        if (paddingRight) {
            result.append(paddingChars);
        } else {
            result.insert(start, paddingChars);
        }
        return result;
    }

    private StringBuilder toStringBuilder(CharSequence cs) {
        return cs instanceof StringBuilder ? (StringBuilder) cs : new StringBuilder(cs);
    }

    private StringBuilder wrapParentheses(StringBuilder result) {
        result.setCharAt(0, '('); // Replace the '-'.
        if (formatToken.flagZero) {
            formatToken.setWidth(formatToken.getWidth() - 1);
            result = (StringBuilder) padding(result, 1);
            result.append(')');
        } else {
            result.append(')');
            result = (StringBuilder) padding(result, 0);
        }
        return result;
    }

    private CharSequence transformFromInteger() {
        int startIndex = 0;
        StringBuilder result = new StringBuilder();
        char currentConversionType = formatToken.getConversionType();

        long value;
        if (arg instanceof Long) {
            value = ((Long) arg).longValue();
        } else if (arg instanceof Integer) {
            value = ((Integer) arg).longValue();
        } else if (arg instanceof Short) {
            value = ((Short) arg).longValue();
        } else if (arg instanceof Byte) {
            value = ((Byte) arg).longValue();
        } else {
            throw badArgumentType();
        }

        if (formatToken.flagSharp) {
            if (currentConversionType == 'o') {
                result.append("0");
                startIndex += 1;
            } else {
                result.append("0x");
                startIndex += 2;
            }
        }

        if (currentConversionType == 'd') {
            CharSequence digits = Long.toString(value);
            if (formatToken.flagComma) {
                digits = insertGrouping(digits);
            }
            if (localeData.zeroDigit != '0') {
                digits = localizeDigits(digits);
            }
            result.append(digits);

            if (value < 0) {
                if (formatToken.flagParenthesis) {
                    return wrapParentheses(result);
                } else if (formatToken.flagZero) {
                    startIndex++;
                }
            } else {
                if (formatToken.flagPlus) {
                    result.insert(0, '+');
                    startIndex += 1;
                } else if (formatToken.flagSpace) {
                    result.insert(0, ' ');
                    startIndex += 1;
                }
            }
        } else {
            // Undo sign-extension, since we'll be using Long.to(Octal|Hex)String.
            if (arg instanceof Byte) {
                value &= 0xffL;
            } else if (arg instanceof Short) {
                value &= 0xffffL;
            } else if (arg instanceof Integer) {
                value &= 0xffffffffL;
            }
            if (currentConversionType == 'o') {
                result.append(Long.toOctalString(value));
            } else {
                result.append(Long.toHexString(value));
            }
        }

        return padding(result, startIndex);
    }

    private CharSequence transformFromNull() {
        formatToken.flagZero = false;
        return padding("null", 0);
    }

    private CharSequence transformFromBigInteger() {
        int startIndex = 0;
        StringBuilder result = new StringBuilder();
        BigInteger bigInt = (BigInteger) arg;
        char currentConversionType = formatToken.getConversionType();

        if (bigInt == null) {
            return transformFromNull();
        }

        boolean isNegative = (bigInt.compareTo(BigInteger.ZERO) < 0);

        if (currentConversionType == 'd') {
            CharSequence digits = bigInt.toString(10);
            if (formatToken.flagComma) {
                digits = insertGrouping(digits);
            }
            result.append(digits);
        } else if (currentConversionType == 'o') {
            // convert BigInteger to a string presentation using radix 8
            result.append(bigInt.toString(8));
        } else {
            // convert BigInteger to a string presentation using radix 16
            result.append(bigInt.toString(16));
        }
        if (formatToken.flagSharp) {
            startIndex = isNegative ? 1 : 0;
            if (currentConversionType == 'o') {
                result.insert(startIndex, "0");
                startIndex += 1;
            } else if (currentConversionType == 'x' || currentConversionType == 'X') {
                result.insert(startIndex, "0x");
                startIndex += 2;
            }
        }

        if (!isNegative) {
            if (formatToken.flagPlus) {
                result.insert(0, '+');
                startIndex += 1;
            }
            if (formatToken.flagSpace) {
                result.insert(0, ' ');
                startIndex += 1;
            }
        }

        /* pad paddingChar to the output */
        if (isNegative && formatToken.flagParenthesis) {
            return wrapParentheses(result);
        }
        if (isNegative && formatToken.flagZero) {
            startIndex++;
        }
        return padding(result, startIndex);
    }

    private CharSequence transformFromDateTime() {
        if (arg == null) {
            return transformFromNull();
        }

        Calendar calendar;
        if (arg instanceof Calendar) {
            calendar = (Calendar) arg;
        } else {
            Date date = null;
            if (arg instanceof Long) {
                date = new Date(((Long) arg).longValue());
            } else if (arg instanceof Date) {
                date = (Date) arg;
            } else {
                throw badArgumentType();
            }
            calendar = Calendar.getInstance(locale);
            calendar.setTime(date);
        }

        StringBuilder result = new StringBuilder();
        if (!appendT(result, formatToken.getDateSuffix(), calendar)) {
            throw formatToken.unknownFormatConversionException();
        }
        return padding(result, 0);
    }

    private boolean appendT(StringBuilder result, char conversion, Calendar calendar) {
        switch (conversion) {
        case 'A':
            result.append(localeData.longWeekdayNames[calendar.get(Calendar.DAY_OF_WEEK)]);
            return true;
        case 'a':
            result.append(localeData.shortWeekdayNames[calendar.get(Calendar.DAY_OF_WEEK)]);
            return true;
        case 'B':
            result.append(localeData.longMonthNames[calendar.get(Calendar.MONTH)]);
            return true;
        case 'b': case 'h':
            result.append(localeData.shortMonthNames[calendar.get(Calendar.MONTH)]);
            return true;
        case 'C':
            appendLocalized(result, calendar.get(Calendar.YEAR) / 100, 2);
            return true;
        case 'D':
            appendT(result, 'm', calendar);
            result.append('/');
            appendT(result, 'd', calendar);
            result.append('/');
            appendT(result, 'y', calendar);
            return true;
        case 'F':
            appendT(result, 'Y', calendar);
            result.append('-');
            appendT(result, 'm', calendar);
            result.append('-');
            appendT(result, 'd', calendar);
            return true;
        case 'H':
            appendLocalized(result, calendar.get(Calendar.HOUR_OF_DAY), 2);
            return true;
        case 'I':
            appendLocalized(result, to12Hour(calendar.get(Calendar.HOUR)), 2);
            return true;
        case 'L':
            appendLocalized(result, calendar.get(Calendar.MILLISECOND), 3);
            return true;
        case 'M':
            appendLocalized(result, calendar.get(Calendar.MINUTE), 2);
            return true;
        case 'N':
            appendLocalized(result, calendar.get(Calendar.MILLISECOND) * 1000000L, 9);
            return true;
        case 'Q':
            appendLocalized(result, calendar.getTimeInMillis(), 0);
            return true;
        case 'R':
            appendT(result, 'H', calendar);
            result.append(':');
            appendT(result, 'M', calendar);
            return true;
        case 'S':
            appendLocalized(result, calendar.get(Calendar.SECOND), 2);
            return true;
        case 'T':
            appendT(result, 'H', calendar);
            result.append(':');
            appendT(result, 'M', calendar);
            result.append(':');
            appendT(result, 'S', calendar);
            return true;
        case 'Y':
            appendLocalized(result, calendar.get(Calendar.YEAR), 4);
            return true;
        case 'Z':
            TimeZone timeZone = calendar.getTimeZone();
            result.append(timeZone.getDisplayName(timeZone.inDaylightTime(calendar.getTime()),
                    TimeZone.SHORT, locale));
            return true;
        case 'c':
            appendT(result, 'a', calendar);
            result.append(' ');
            appendT(result, 'b', calendar);
            result.append(' ');
            appendT(result, 'd', calendar);
            result.append(' ');
            appendT(result, 'T', calendar);
            result.append(' ');
            appendT(result, 'Z', calendar);
            result.append(' ');
            appendT(result, 'Y', calendar);
            return true;
        case 'd':
            appendLocalized(result, calendar.get(Calendar.DAY_OF_MONTH), 2);
            return true;
        case 'e':
            appendLocalized(result, calendar.get(Calendar.DAY_OF_MONTH), 0);
            return true;
        case 'j':
            appendLocalized(result, calendar.get(Calendar.DAY_OF_YEAR), 3);
            return true;
        case 'k':
            appendLocalized(result, calendar.get(Calendar.HOUR_OF_DAY), 0);
            return true;
        case 'l':
            appendLocalized(result, to12Hour(calendar.get(Calendar.HOUR)), 0);
            return true;
        case 'm':
            // Calendar.JANUARY is 0; humans want January represented as 1.
            appendLocalized(result, calendar.get(Calendar.MONTH) + 1, 2);
            return true;
        case 'p':
            result.append(localeData.amPm[calendar.get(Calendar.AM_PM)].toLowerCase(locale));
            return true;
        case 'r':
            appendT(result, 'I', calendar);
            result.append(':');
            appendT(result, 'M', calendar);
            result.append(':');
            appendT(result, 'S', calendar);
            result.append(' ');
            result.append(localeData.amPm[calendar.get(Calendar.AM_PM)]);
            return true;
        case 's':
            appendLocalized(result, calendar.getTimeInMillis() / 1000, 0);
            return true;
        case 'y':
            appendLocalized(result, calendar.get(Calendar.YEAR) % 100, 2);
            return true;
        case 'z':
            long offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
            char sign = '+';
            if (offset < 0) {
                sign = '-';
                offset = -offset;
            }
            result.append(sign);
            appendLocalized(result, offset / 3600000, 2);
            appendLocalized(result, (offset % 3600000) / 60000, 2);
            return true;
        }
        return false;
    }

    private int to12Hour(int hour) {
        return hour == 0 ? 12 : hour;
    }

    private void appendLocalized(StringBuilder result, long value, int width) {
        int paddingIndex = result.length();
        char zeroDigit = localeData.zeroDigit;
        if (zeroDigit == '0') {
            result.append(value);
        } else {
            result.append(localizeDigits(Long.toString(value)));
        }
        int zeroCount = width - (result.length() - paddingIndex);
        if (zeroCount <= 0) {
            return;
        }
        if (zeroDigit == '0') {
            result.insert(paddingIndex, ZEROS, 0, zeroCount);
        } else {
            for (int i = 0; i < zeroCount; ++i) {
                result.insert(paddingIndex, zeroDigit);
            }
        }
    }

    private CharSequence transformFromSpecialNumber(double d) {
        String source = null;
        if (Double.isNaN(d)) {
            source = "NaN";
        } else if (d == Double.POSITIVE_INFINITY) {
            if (formatToken.flagPlus) {
                source = "+Infinity";
            } else if (formatToken.flagSpace) {
                source = " Infinity";
            } else {
                source = "Infinity";
            }
        } else if (d == Double.NEGATIVE_INFINITY) {
            if (formatToken.flagParenthesis) {
                source = "(Infinity)";
            } else {
                source = "-Infinity";
            }
        } else {
            return null;
        }

        formatToken.setPrecision(FormatToken.UNSET);
        formatToken.flagZero = false;
        return padding(source, 0);
    }

    private CharSequence transformFromFloat() {
        if (arg == null) {
            return transformFromNull();
        } else if (arg instanceof Float || arg instanceof Double) {
            Number number = (Number) arg;
            double d = number.doubleValue();
            if (d != d || d == Double.POSITIVE_INFINITY || d == Double.NEGATIVE_INFINITY) {
                return transformFromSpecialNumber(d);
            }
        } else if (arg instanceof BigDecimal) {
            // BigDecimal can't represent NaN or infinities, but its doubleValue method will return
            // infinities if the BigDecimal is too big for a double.
        } else {
            throw badArgumentType();
        }

        char conversionType = formatToken.getConversionType();
        if (conversionType != 'a' && conversionType != 'A' && !formatToken.isPrecisionSet()) {
            formatToken.setPrecision(FormatToken.DEFAULT_PRECISION);
        }

        StringBuilder result = new StringBuilder();
        switch (conversionType) {
        case 'a': case 'A':
            transformA(result);
            break;
        case 'e': case 'E':
            transformE(result);
            break;
        case 'f':
            transformF(result);
            break;
        case 'g':
        case 'G':
            transformG(result);
            break;
        default:
            throw formatToken.unknownFormatConversionException();
        }

        formatToken.setPrecision(FormatToken.UNSET);

        int startIndex = 0;
        if (result.charAt(0) == localeData.minusSign) {
            if (formatToken.flagParenthesis) {
                return wrapParentheses(result);
            }
        } else {
            if (formatToken.flagSpace) {
                result.insert(0, ' ');
                startIndex++;
            }
            if (formatToken.flagPlus) {
                result.insert(0, '+');
                startIndex++;
            }
        }

        char firstChar = result.charAt(0);
        if (formatToken.flagZero && (firstChar == '+' || firstChar == localeData.minusSign)) {
            startIndex = 1;
        }

        if (conversionType == 'a' || conversionType == 'A') {
            startIndex += 2;
        }
        return padding(result, startIndex);
    }

    private void transformE(StringBuilder result) {
        // All zeros in this method are *pattern* characters, so no localization.
        final int precision = formatToken.getPrecision();
        String pattern = "0E+00";
        if (precision > 0) {
            StringBuilder sb = new StringBuilder("0.");
            char[] zeros = new char[precision];
            Arrays.fill(zeros, '0');
            sb.append(zeros);
            sb.append("E+00");
            pattern = sb.toString();
        }

        NativeDecimalFormat nf = getDecimalFormat(pattern);
        char[] chars;
        if (arg instanceof BigDecimal) {
            chars = nf.formatBigDecimal((BigDecimal) arg, null);
        } else {
            chars = nf.formatDouble(((Number) arg).doubleValue(), null);
        }
        // Unlike %f, %e uses 'e' (regardless of what the DecimalFormatSymbols would have us use).
        for (int i = 0; i < chars.length; ++i) {
            if (chars[i] == 'E') {
                chars[i] = 'e';
            }
        }
        result.append(chars);
        // The # flag requires that we always output a decimal separator.
        if (formatToken.flagSharp && precision == 0) {
            int indexOfE = result.indexOf("e");
            result.insert(indexOfE, localeData.decimalSeparator);
        }
    }

    private void transformG(StringBuilder result) {
        int precision = formatToken.getPrecision();
        if (precision == 0) {
            precision = 1;
        }
        formatToken.setPrecision(precision);

        double d = ((Number) arg).doubleValue();
        if (d == 0.0) {
            precision--;
            formatToken.setPrecision(precision);
            transformF(result);
            return;
        }

        boolean requireScientificRepresentation = true;
        d = Math.abs(d);
        if (Double.isInfinite(d)) {
            precision = formatToken.getPrecision();
            precision--;
            formatToken.setPrecision(precision);
            transformE(result);
            return;
        }
        BigDecimal b = new BigDecimal(d, new MathContext(precision));
        d = b.doubleValue();
        long l = b.longValue();

        if (d >= 1 && d < Math.pow(10, precision)) {
            if (l < Math.pow(10, precision)) {
                requireScientificRepresentation = false;
                precision -= String.valueOf(l).length();
                precision = precision < 0 ? 0 : precision;
                l = Math.round(d * Math.pow(10, precision + 1));
                if (String.valueOf(l).length() <= formatToken.getPrecision()) {
                    precision++;
                }
                formatToken.setPrecision(precision);
            }
        } else {
            l = b.movePointRight(4).longValue();
            if (d >= Math.pow(10, -4) && d < 1) {
                requireScientificRepresentation = false;
                precision += 4 - String.valueOf(l).length();
                l = b.movePointRight(precision + 1).longValue();
                if (String.valueOf(l).length() <= formatToken.getPrecision()) {
                    precision++;
                }
                l = b.movePointRight(precision).longValue();
                if (l >= Math.pow(10, precision - 4)) {
                    formatToken.setPrecision(precision);
                }
            }
        }
        if (requireScientificRepresentation) {
            precision = formatToken.getPrecision();
            precision--;
            formatToken.setPrecision(precision);
            transformE(result);
        } else {
            transformF(result);
        }
    }

    private void transformF(StringBuilder result) {
        // All zeros in this method are *pattern* characters, so no localization.
        String pattern = "0.000000";
        final int precision = formatToken.getPrecision();
        if (formatToken.flagComma || precision != FormatToken.DEFAULT_PRECISION) {
            StringBuilder patternBuilder = new StringBuilder();
            if (formatToken.flagComma) {
                patternBuilder.append(',');
                int groupingSize = 3;
                char[] sharps = new char[groupingSize - 1];
                Arrays.fill(sharps, '#');
                patternBuilder.append(sharps);
            }
            patternBuilder.append('0');
            if (precision > 0) {
                patternBuilder.append('.');
                for (int i = 0; i < precision; ++i) {
                    patternBuilder.append('0');
                }
            }
            pattern = patternBuilder.toString();
        }

        NativeDecimalFormat nf = getDecimalFormat(pattern);
        if (arg instanceof BigDecimal) {
            result.append(nf.formatBigDecimal((BigDecimal) arg, null));
        } else {
            result.append(nf.formatDouble(((Number) arg).doubleValue(), null));
        }
        // The # flag requires that we always output a decimal separator.
        if (formatToken.flagSharp && precision == 0) {
            result.append(localeData.decimalSeparator);
        }
    }

    private void transformA(StringBuilder result) {
        if (arg instanceof Float) {
            result.append(Float.toHexString(((Float) arg).floatValue()));
        } else if (arg instanceof Double) {
            result.append(Double.toHexString(((Double) arg).doubleValue()));
        } else {
            throw badArgumentType();
        }

        if (!formatToken.isPrecisionSet()) {
            return;
        }

        int precision = formatToken.getPrecision();
        if (precision == 0) {
            precision = 1;
        }
        int indexOfFirstFractionalDigit = result.indexOf(".") + 1;
        int indexOfP = result.indexOf("p");
        int fractionalLength = indexOfP - indexOfFirstFractionalDigit;

        if (fractionalLength == precision) {
            return;
        }

        if (fractionalLength < precision) {
            char[] zeros = new char[precision - fractionalLength];
            Arrays.fill(zeros, '0'); // %a shouldn't be localized.
            result.insert(indexOfP, zeros);
            return;
        }
        result.delete(indexOfFirstFractionalDigit + precision, indexOfP);
    }

    private static class FormatSpecifierParser {
        private String format;
        private int length;

        private int startIndex;
        private int i;

        /**
         * Constructs a new parser for the given format string.
         */
        FormatSpecifierParser(String format) {
            this.format = format;
            this.length = format.length();
        }

        /**
         * Returns a FormatToken representing the format specifier starting at 'offset'.
         * @param offset the first character after the '%'
         */
        FormatToken parseFormatToken(int offset) {
            this.startIndex = offset;
            this.i = offset;
            return parseArgumentIndexAndFlags(new FormatToken());
        }

        /**
         * Returns a string corresponding to the last format specifier that was parsed.
         * Used to construct error messages.
         */
        String getFormatSpecifierText() {
            return format.substring(startIndex, i);
        }

        private int peek() {
            return (i < length) ? format.charAt(i) : -1;
        }

        private char advance() {
            if (i >= length) {
                throw unknownFormatConversionException();
            }
            return format.charAt(i++);
        }

        private UnknownFormatConversionException unknownFormatConversionException() {
            throw new UnknownFormatConversionException(getFormatSpecifierText());
        }

        private FormatToken parseArgumentIndexAndFlags(FormatToken token) {
            // Parse the argument index, if there is one.
            int position = i;
            int ch = peek();
            if (Character.isDigit((char) ch)) {
                int number = nextInt();
                if (peek() == '$') {
                    // The number was an argument index.
                    advance(); // Swallow the '$'.
                    if (number == FormatToken.UNSET) {
                        throw new MissingFormatArgumentException(getFormatSpecifierText());
                    }
                    // k$ stands for the argument whose index is k-1 except that
                    // 0$ and 1$ both stand for the first element.
                    token.setArgIndex(Math.max(0, number - 1));
                } else {
                    if (ch == '0') {
                        // The digit zero is a format flag, so reparse it as such.
                        i = position;
                    } else {
                        // The number was a width. This means there are no flags to parse.
                        return parseWidth(token, number);
                    }
                }
            } else if (ch == '<') {
                token.setArgIndex(FormatToken.LAST_ARGUMENT_INDEX);
                advance();
            }

            // Parse the flags.
            while (token.setFlag(peek())) {
                advance();
            }

            // What comes next?
            ch = peek();
            if (Character.isDigit((char) ch)) {
                return parseWidth(token, nextInt());
            } else if (ch == '.') {
                return parsePrecision(token);
            } else {
                return parseConversionType(token);
            }
        }

        // We pass the width in because in some cases we've already parsed it.
        // (Because of the ambiguity between argument indexes and widths.)
        private FormatToken parseWidth(FormatToken token, int width) {
            token.setWidth(width);
            int ch = peek();
            if (ch == '.') {
                return parsePrecision(token);
            } else {
                return parseConversionType(token);
            }
        }

        private FormatToken parsePrecision(FormatToken token) {
            advance(); // Swallow the '.'.
            int ch = peek();
            if (Character.isDigit((char) ch)) {
                token.setPrecision(nextInt());
                return parseConversionType(token);
            } else {
                // The precision is required but not given by the format string.
                throw unknownFormatConversionException();
            }
        }

        private FormatToken parseConversionType(FormatToken token) {
            char conversionType = advance(); // A conversion type is mandatory.
            token.setConversionType(conversionType);
            if (conversionType == 't' || conversionType == 'T') {
                char dateSuffix = advance(); // A date suffix is mandatory for 't' or 'T'.
                token.setDateSuffix(dateSuffix);
            }
            return token;
        }

        // Parses an integer (of arbitrary length, but typically just one digit).
        private int nextInt() {
            long value = 0;
            while (i < length && Character.isDigit(format.charAt(i))) {
                value = 10 * value + (format.charAt(i++) - '0');
                if (value > Integer.MAX_VALUE) {
                    return failNextInt();
                }
            }
            return (int) value;
        }

        // Swallow remaining digits to resync our attempted parse, but return failure.
        private int failNextInt() {
            while (Character.isDigit((char) peek())) {
                advance();
            }
            return FormatToken.UNSET;
        }
    }
}
