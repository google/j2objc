/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import libcore.util.EmptyArray;

/**
 * Produces concatenated messages in language-neutral way. New code
 * should probably use {@link java.util.Formatter} instead.
 * <p>
 * {@code MessageFormat} takes a set of objects, formats them and then
 * inserts the formatted strings into the pattern at the appropriate places.
 * <p>
 * <strong>Note:</strong> {@code MessageFormat} differs from the other
 * {@code Format} classes in that you create a {@code MessageFormat}
 * object with one of its constructors (not with a {@code getInstance}
 * style factory method). The factory methods aren't necessary because
 * {@code MessageFormat} itself doesn't implement locale-specific
 * behavior. Any locale-specific behavior is defined by the pattern that you
 * provide as well as the subformats used for inserted arguments.
 *
 * <h4><a name="patterns">Patterns and their interpretation</a></h4>
 *
 * {@code MessageFormat} uses patterns of the following form:
 * <blockquote>
 *
 * <pre>
 * <i>MessageFormatPattern:</i>
 *         <i>String</i>
 *         <i>MessageFormatPattern</i> <i>FormatElement</i> <i>String</i>
 * <i>FormatElement:</i>
 *         { <i>ArgumentIndex</i> }
 *         { <i>ArgumentIndex</i> , <i>FormatType</i> }
 *         { <i>ArgumentIndex</i> , <i>FormatType</i> , <i>FormatStyle</i> }
 * <i>FormatType: one of </i>
 *         number date time choice
 * <i>FormatStyle:</i>
 *         short
 *         medium
 *         long
 *         full
 *         integer
 *         currency
 *         percent
 *         <i>SubformatPattern</i>
 * <i>String:</i>
 *         <i>StringPart&lt;sub&gt;opt&lt;/sub&gt;</i>
 *         <i>String</i> <i>StringPart</i>
 * <i>StringPart:</i>
 *         ''
 *         ' <i>QuotedString</i> '
 *         <i>UnquotedString</i>
 * <i>SubformatPattern:</i>
 *         <i>SubformatPatternPart&lt;sub&gt;opt&lt;/sub&gt;</i>
 *         <i>SubformatPattern</i> <i>SubformatPatternPart</i>
 * <i>SubFormatPatternPart:</i>
 *         ' <i>QuotedPattern</i> '
 *         <i>UnquotedPattern</i>
 * </pre>
 *
 * </blockquote>
 *
 * <p>
 * Within a <i>String</i>, {@code "''"} represents a single quote. A
 * <i>QuotedString</i> can contain arbitrary characters except single quotes;
 * the surrounding single quotes are removed. An <i>UnquotedString</i> can
 * contain arbitrary characters except single quotes and left curly brackets.
 * Thus, a string that should result in the formatted message "'{0}'" can be
 * written as {@code "'''{'0}''"} or {@code "'''{0}'''"}.
 * <p>
 * Within a <i>SubformatPattern</i>, different rules apply. A <i>QuotedPattern</i>
 * can contain arbitrary characters except single quotes, but the surrounding
 * single quotes are <strong>not</strong> removed, so they may be interpreted
 * by the subformat. For example, {@code "{1,number,$'#',##}"} will
 * produce a number format with the hash-sign quoted, with a result such as:
 * "$#31,45". An <i>UnquotedPattern</i> can contain arbitrary characters except
 * single quotes, but curly braces within it must be balanced. For example,
 * {@code "ab {0} de"} and {@code "ab '}' de"} are valid subformat
 * patterns, but {@code "ab {0'}' de"} and {@code "ab } de"} are
 * not.
 * <dl>
 * <dt><b>Warning:</b></dt>
 * <dd>The rules for using quotes within message format patterns unfortunately
 * have shown to be somewhat confusing. In particular, it isn't always obvious
 * to localizers whether single quotes need to be doubled or not. Make sure to
 * inform localizers about the rules, and tell them (for example, by using
 * comments in resource bundle source files) which strings will be processed by
 * {@code MessageFormat}. Note that localizers may need to use single quotes in
 * translated strings where the original version doesn't have them. <br>
 * Note also that the simplest way to avoid the problem is to use the real
 * apostrophe (single quote) character \u2019 (') for human-readable text, and
 * to use the ASCII apostrophe (\u0027 ' ) only in program syntax, like quoting
 * in {@code MessageFormat}. See the annotations for U+0027 Apostrophe in The Unicode
 * Standard.
 * </dl>
 * <p>
 * The <i>ArgumentIndex</i> value is a non-negative integer written using the
 * digits '0' through '9', and represents an index into the
 * {@code arguments} array passed to the {@code format} methods or
 * the result array returned by the {@code parse} methods.
 * <p>
 * The <i>FormatType</i> and <i>FormatStyle</i> values are used to create a
 * {@code Format} instance for the format element. The following table
 * shows how the values map to {@code Format} instances. Combinations not shown in the
 * table are illegal. A <i>SubformatPattern</i> must be a valid pattern string
 * for the {@code Format} subclass used.
 * <p>
 * <table border=1>
 * <tr>
 * <th>Format Type</th>
 * <th>Format Style</th>
 * <th>Subformat Created</th>
 * </tr>
 * <tr>
 * <td colspan="2"><i>(none)</i></td>
 * <td>{@code null}</td>
 * </tr>
 * <tr>
 * <td rowspan="5">{@code number}</td>
 * <td><i>(none)</i></td>
 * <td>{@code NumberFormat.getInstance(getLocale())}</td>
 * </tr>
 * <tr>
 * <td>{@code integer}</td>
 * <td>{@code NumberFormat.getIntegerInstance(getLocale())}</td>
 * </tr>
 * <tr>
 * <td>{@code currency}</td>
 * <td>{@code NumberFormat.getCurrencyInstance(getLocale())}</td>
 * </tr>
 * <tr>
 * <td>{@code percent}</td>
 * <td>{@code NumberFormat.getPercentInstance(getLocale())}</td>
 * </tr>
 * <tr>
 * <td><i>SubformatPattern</i></td>
 * <td>{@code new DecimalFormat(subformatPattern, new DecimalFormatSymbols(getLocale()))}</td>
 * </tr>
 * <tr>
 * <td rowspan="6">{@code date}</td>
 * <td><i>(none)</i></td>
 * <td>{@code DateFormat.getDateInstance(DateFormat.DEFAULT, getLocale())}</td>
 * </tr>
 * <tr>
 * <td>{@code short}</td>
 * <td>{@code DateFormat.getDateInstance(DateFormat.SHORT, getLocale())}</td>
 * </tr>
 * <tr>
 * <td>{@code medium}</td>
 * <td>{@code DateFormat.getDateInstance(DateFormat.DEFAULT, getLocale())}</td>
 * </tr>
 * <tr>
 * <td>{@code long}</td>
 * <td>{@code DateFormat.getDateInstance(DateFormat.LONG, getLocale())}</td>
 * </tr>
 * <tr>
 * <td>{@code full}</td>
 * <td>{@code DateFormat.getDateInstance(DateFormat.FULL, getLocale())}</td>
 * </tr>
 * <tr>
 * <td><i>SubformatPattern</i></td>
 * <td>{@code new SimpleDateFormat(subformatPattern, getLocale())}</td>
 * </tr>
 * <tr>
 * <td rowspan="6">{@code time}</td>
 * <td><i>(none)</i></td>
 * <td>{@code DateFormat.getTimeInstance(DateFormat.DEFAULT, getLocale())}</td>
 * </tr>
 * <tr>
 * <td>{@code short}</td>
 * <td>{@code DateFormat.getTimeInstance(DateFormat.SHORT, getLocale())}</td>
 * </tr>
 * <tr>
 * <td>{@code medium}</td>
 * <td>{@code DateFormat.getTimeInstance(DateFormat.DEFAULT, getLocale())}</td>
 * </tr>
 * <tr>
 * <td>{@code long}</td>
 * <td>{@code DateFormat.getTimeInstance(DateFormat.LONG, getLocale())}</td>
 * </tr>
 * <tr>
 * <td>{@code full}</td>
 * <td>{@code DateFormat.getTimeInstance(DateFormat.FULL, getLocale())}</td>
 * </tr>
 * <tr>
 * <td><i>SubformatPattern</i></td>
 * <td>{@code new SimpleDateFormat(subformatPattern, getLocale())}</td>
 * </tr>
 * <tr>
 * <td>{@code choice}</td>
 * <td><i>SubformatPattern</i></td>
 * <td>{@code new ChoiceFormat(subformatPattern)}</td>
 * </tr>
 * </table>
 *
 * <h4>Usage Information</h4>
 * <p>
 * Here are some examples of usage: <blockquote>
 *
 * <pre>
 * Object[] arguments = {
 *         Integer.valueOf(7), new Date(System.currentTimeMillis()),
 *         "a disturbance in the Force"};
 * String result = MessageFormat.format(
 *         "At {1,time} on {1,date}, there was {2} on planet {0,number,integer}.",
 *         arguments);
 * <em>
 * Output:
 * </em>
 * At 12:30 PM on Jul 3, 2053, there was a disturbance in the Force on planet 7.
 * </pre>
 *
 * </blockquote>
 * <p>
 * Typically, the message format will come from resources, and the
 * arguments will be dynamically set at runtime.
 * <p>
 * Example 2: <blockquote>
 *
 * <pre>
 * Object[] testArgs = {Long.valueOf(3), "MyDisk"};
 * MessageFormat form = new MessageFormat("The disk \"{1}\" contains {0} file(s).");
 * System.out.println(form.format(testArgs));
 * <em>
 * Output with different testArgs:
 * </em>
 * The disk "MyDisk" contains 0 file(s).
 * The disk "MyDisk" contains 1 file(s).
 * The disk "MyDisk" contains 1,273 file(s).
 * </pre>
 *
 * </blockquote>
 *
 * <p>
 * For more sophisticated patterns, you can use a {@code ChoiceFormat} to
 * get output such as:
 * <blockquote>
 *
 * <pre>
 * MessageFormat form = new MessageFormat("The disk \"{1}\" contains {0}.");
 * double[] filelimits = {0,1,2};
 * String[] filepart = {"no files","one file","{0,number} files"};
 * ChoiceFormat fileform = new ChoiceFormat(filelimits, filepart);
 * form.setFormatByArgumentIndex(0, fileform);
 * Object[] testArgs = {Long.valueOf(12373), "MyDisk"};
 * System.out.println(form.format(testArgs));
 * <em>
 * Output (with different testArgs):
 * </em>
 * The disk "MyDisk" contains no files.
 * The disk "MyDisk" contains one file.
 * The disk "MyDisk" contains 1,273 files.
 * </pre>
 *
 * </blockquote> You can either do this programmatically, as in the above
 * example, or by using a pattern (see {@link ChoiceFormat} for more
 * information) as in: <blockquote>
 *
 * <pre>
 * form.applyPattern("There {0,choice,0#are no files|1#is one file|1&lt;are {0,number,integer} files}.");
 * </pre>
 *
 * </blockquote>
 * <p>
 * <strong>Note:</strong> As we see above, the string produced by a
 * {@code ChoiceFormat} in {@code MessageFormat} is treated
 * specially; occurances of '{' are used to indicated subformats, and cause
 * recursion. If you create both a {@code MessageFormat} and
 * {@code ChoiceFormat} programmatically (instead of using the string
 * patterns), then be careful not to produce a format that recurses on itself,
 * which will cause an infinite loop.
 * <p>
 * When a single argument is parsed more than once in the string, the last match
 * will be the final result of the parsing. For example:
 * <blockquote>
 * <pre>
 * MessageFormat mf = new MessageFormat("{0,number,#.##}, {0,number,#.#}");
 * Object[] objs = {new Double(3.1415)};
 * String result = mf.format(objs);
 * // result now equals "3.14, 3.1"
 * objs = null;
 * objs = mf.parse(result, new ParsePosition(0));
 * // objs now equals {new Double(3.1)}
 * </pre>
 * </blockquote>
 * <p>
 * Likewise, parsing with a {@code MessageFormat} object using patterns
 * containing multiple occurrences of the same argument would return the last
 * match. For example:
 * <blockquote>
 * <pre>
 * MessageFormat mf = new MessageFormat("{0}, {0}, {0}");
 * String forParsing = "x, y, z";
 * Object[] objs = mf.parse(forParsing, new ParsePosition(0));
 * // result now equals {new String("z")}
 * </pre>
 * </blockquote>
 * <h4><a name="synchronization">Synchronization</a></h4>
 * <p>
 * Message formats are not synchronized. It is recommended to create separate
 * format instances for each thread. If multiple threads access a format
 * concurrently, it must be synchronized externally.
 *
 * @see java.util.Formatter
 */
public class MessageFormat extends Format {

    private static final long serialVersionUID = 6479157306784022952L;

    private Locale locale;

    transient private String[] strings;

    private int[] argumentNumbers;

    private Format[] formats;

    private int maxOffset;

    transient private int maxArgumentIndex;

    /**
     * Constructs a new {@code MessageFormat} using the specified pattern and {@code locale}.
     *
     * @param template
     *            the pattern.
     * @param locale
     *            the locale.
     * @throws IllegalArgumentException
     *            if the pattern cannot be parsed.
     */
    public MessageFormat(String template, Locale locale) {
        this.locale = locale;
        applyPattern(template);
    }

    /**
     * Constructs a new {@code MessageFormat} using the specified pattern and
     * the user's default locale.
     * See "<a href="../util/Locale.html#default_locale">Be wary of the default locale</a>".
     *
     * @param template
     *            the pattern.
     * @throws IllegalArgumentException
     *            if the pattern cannot be parsed.
     */
    public MessageFormat(String template) {
        this(template, Locale.getDefault());
    }

    /**
     * Changes this {@code MessageFormat} to use the specified pattern.
     *
     * @param template
     *            the new pattern.
     * @throws IllegalArgumentException
     *            if the pattern cannot be parsed.
     */
    public void applyPattern(String template) {
        int length = template.length();
        StringBuffer buffer = new StringBuffer();
        ParsePosition position = new ParsePosition(0);
        ArrayList<String> localStrings = new ArrayList<String>();
        int argCount = 0;
        int[] args = new int[10];
        int maxArg = -1;
        ArrayList<Format> localFormats = new ArrayList<Format>();
        while (position.getIndex() < length) {
            if (Format.upTo(template, position, buffer, '{')) {
                int arg = 0;
                int offset = position.getIndex();
                if (offset >= length) {
                    throw new IllegalArgumentException("Invalid argument number");
                }
                // Get argument number
                char ch;
                while ((ch = template.charAt(offset++)) != '}' && ch != ',') {
                    if (ch < '0' && ch > '9') {
                        throw new IllegalArgumentException("Invalid argument number");
                    }

                    arg = arg * 10 + (ch - '0');

                    if (arg < 0 || offset >= length) {
                        throw new IllegalArgumentException("Invalid argument number");
                    }
                }
                offset--;
                position.setIndex(offset);
                localFormats.add(parseVariable(template, position));
                if (argCount >= args.length) {
                    int[] newArgs = new int[args.length * 2];
                    System.arraycopy(args, 0, newArgs, 0, args.length);
                    args = newArgs;
                }
                args[argCount++] = arg;
                if (arg > maxArg) {
                    maxArg = arg;
                }
            }
            localStrings.add(buffer.toString());
            buffer.setLength(0);
        }
        this.strings = localStrings.toArray(new String[localStrings.size()]);
        argumentNumbers = args;
        this.formats = localFormats.toArray(new Format[argCount]);
        maxOffset = argCount - 1;
        maxArgumentIndex = maxArg;
    }

    /**
     * Returns a new instance of {@code MessageFormat} with the same pattern and
     * formats as this {@code MessageFormat}.
     *
     * @return a shallow copy of this {@code MessageFormat}.
     * @see java.lang.Cloneable
     */
    @Override
    public Object clone() {
        MessageFormat clone = (MessageFormat) super.clone();
        Format[] array = new Format[formats.length];
        for (int i = formats.length; --i >= 0;) {
            if (formats[i] != null) {
                array[i] = (Format) formats[i].clone();
            }
        }
        clone.formats = array;
        return clone;
    }

    /**
     * Compares the specified object to this {@code MessageFormat} and indicates
     * if they are equal. In order to be equal, {@code object} must be an
     * instance of {@code MessageFormat} and have the same pattern.
     *
     * @param object
     *            the object to compare with this object.
     * @return {@code true} if the specified object is equal to this
     *         {@code MessageFormat}; {@code false} otherwise.
     * @see #hashCode
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof MessageFormat)) {
            return false;
        }
        MessageFormat format = (MessageFormat) object;
        if (maxOffset != format.maxOffset) {
            return false;
        }
        // Must use a loop since the lengths may be different due
        // to serialization cross-loading
        for (int i = 0; i <= maxOffset; i++) {
            if (argumentNumbers[i] != format.argumentNumbers[i]) {
                return false;
            }
        }
        return locale.equals(format.locale)
                && Arrays.equals(strings, format.strings)
                && Arrays.equals(formats, format.formats);
    }

    /**
     * Formats the specified object using the rules of this message format and
     * returns an {@code AttributedCharacterIterator} with the formatted message and
     * attributes. The {@code AttributedCharacterIterator} returned also includes the
     * attributes from the formats of this message format.
     *
     * @param object
     *            the object to format.
     * @return an {@code AttributedCharacterIterator} with the formatted message and
     *         attributes.
     * @throws IllegalArgumentException
     *            if the arguments in the object array cannot be formatted
     *            by this message format.
     */
    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object object) {
        if (object == null) {
            throw new NullPointerException("object == null");
        }

        StringBuffer buffer = new StringBuffer();
        ArrayList<FieldContainer> fields = new ArrayList<FieldContainer>();

        // format the message, and find fields
        formatImpl((Object[]) object, buffer, new FieldPosition(0), fields);

        // create an AttributedString with the formatted buffer
        AttributedString as = new AttributedString(buffer.toString());

        // add MessageFormat field attributes and values to the AttributedString
        for (FieldContainer fc : fields) {
            as.addAttribute(fc.attribute, fc.value, fc.start, fc.end);
        }

        // return the CharacterIterator from AttributedString
        return as.getIterator();
    }

    /**
     * Converts the specified objects into a string which it appends to the
     * specified string buffer using the pattern of this message format.
     * <p>
     * If the {@code field} member of the specified {@code FieldPosition} is
     * {@code MessageFormat.Field.ARGUMENT}, then the begin and end index of
     * this field position is set to the location of the first occurrence of a
     * message format argument. Otherwise, the {@code FieldPosition} is ignored.
     *
     * @param objects
     *            the array of objects to format.
     * @param buffer
     *            the target string buffer to append the formatted message to.
     * @param field
     *            on input: an optional alignment field; on output: the offsets
     *            of the alignment field in the formatted text.
     * @return the string buffer.
     */
    public final StringBuffer format(Object[] objects, StringBuffer buffer,
            FieldPosition field) {
        return formatImpl(objects, buffer, field, null);
    }

    private StringBuffer formatImpl(Object[] objects, StringBuffer buffer,
            FieldPosition position, List<FieldContainer> fields) {
        FieldPosition passedField = new FieldPosition(0);
        for (int i = 0; i <= maxOffset; i++) {
            buffer.append(strings[i]);
            int begin = buffer.length();
            Object arg;
            if (objects != null && argumentNumbers[i] < objects.length) {
                arg = objects[argumentNumbers[i]];
            } else {
                buffer.append('{');
                buffer.append(argumentNumbers[i]);
                buffer.append('}');
                handleArgumentField(begin, buffer.length(), argumentNumbers[i], position, fields);
                continue;
            }
            Format format = formats[i];
            if (format == null || arg == null) {
                if (arg instanceof Number) {
                    format = NumberFormat.getInstance();
                } else if (arg instanceof Date) {
                    format = DateFormat.getInstance();
                } else {
                    buffer.append(arg);
                    handleArgumentField(begin, buffer.length(), argumentNumbers[i], position, fields);
                    continue;
                }
            }
            if (format instanceof ChoiceFormat) {
                String result = format.format(arg);
                MessageFormat mf = new MessageFormat(result);
                mf.setLocale(locale);
                mf.format(objects, buffer, passedField);
                handleArgumentField(begin, buffer.length(), argumentNumbers[i], position, fields);
                handleFormat(format, arg, begin, fields);
            } else {
                format.format(arg, buffer, passedField);
                handleArgumentField(begin, buffer.length(), argumentNumbers[i], position, fields);
                handleFormat(format, arg, begin, fields);
            }
        }
        if (maxOffset + 1 < strings.length) {
            buffer.append(strings[maxOffset + 1]);
        }
        return buffer;
    }

    /**
     * Adds a new FieldContainer with MessageFormat.Field.ARGUMENT field,
     * argIndex, begin and end index to the fields list, or sets the
     * position's begin and end index if it has MessageFormat.Field.ARGUMENT as
     * its field attribute.
     */
    private void handleArgumentField(int begin, int end, int argIndex,
            FieldPosition position, List<FieldContainer> fields) {
        if (fields != null) {
            fields.add(new FieldContainer(begin, end, Field.ARGUMENT, Integer.valueOf(argIndex)));
        } else {
            if (position != null
                    && position.getFieldAttribute() == Field.ARGUMENT
                    && position.getEndIndex() == 0) {
                position.setBeginIndex(begin);
                position.setEndIndex(end);
            }
        }
    }

    /**
     * An inner class to store attributes, values, start and end indices.
     * Instances of this inner class are used as elements for the fields list.
     */
    private static class FieldContainer {
        int start, end;

        AttributedCharacterIterator.Attribute attribute;

        Object value;

        public FieldContainer(int start, int end,
                AttributedCharacterIterator.Attribute attribute, Object value) {
            this.start = start;
            this.end = end;
            this.attribute = attribute;
            this.value = value;
        }
    }

    /**
     * If fields list is not null, find and add the fields of this format to
     * the fields list by iterating through its AttributedCharacterIterator
     *
     * @param format
     *            the format to find fields for
     * @param arg
     *            object to format
     * @param begin
     *            the index where the string this format has formatted begins
     */
    private void handleFormat(Format format, Object arg, int begin, List<FieldContainer> fields) {
        if (fields == null) {
            return;
        }
        AttributedCharacterIterator iterator = format.formatToCharacterIterator(arg);
        while (iterator.getIndex() != iterator.getEndIndex()) {
            int start = iterator.getRunStart();
            int end = iterator.getRunLimit();
            Iterator<?> it = iterator.getAttributes().keySet().iterator();
            while (it.hasNext()) {
                AttributedCharacterIterator.Attribute attribute =
                        (AttributedCharacterIterator.Attribute) it.next();
                Object value = iterator.getAttribute(attribute);
                fields.add(new FieldContainer(begin + start, begin + end, attribute, value));
            }
            iterator.setIndex(end);
        }
    }

    /**
     * Converts the specified objects into a string which it appends to the
     * specified string buffer using the pattern of this message format.
     * <p>
     * If the {@code field} member of the specified {@code FieldPosition} is
     * {@code MessageFormat.Field.ARGUMENT}, then the begin and end index of
     * this field position is set to the location of the first occurrence of a
     * message format argument. Otherwise, the {@code FieldPosition} is ignored.
     * <p>
     * Calling this method is equivalent to calling
     * <blockquote>
     *
     * <pre>
     * format((Object[])object, buffer, field)
     * </pre>
     *
     * </blockquote>
     *
     * @param object
     *            the object to format, must be an array of {@code Object}.
     * @param buffer
     *            the target string buffer to append the formatted message to.
     * @param field
     *            on input: an optional alignment field; on output: the offsets
     *            of the alignment field in the formatted text.
     * @return the string buffer.
     * @throws ClassCastException
     *             if {@code object} is not an array of {@code Object}.
     */
    @Override
    public final StringBuffer format(Object object, StringBuffer buffer,
            FieldPosition field) {
        return format((Object[]) object, buffer, field);
    }

    /**
     * Formats the supplied objects using the specified message format pattern.
     *
     * @param format the format string (see {@link java.util.Formatter#format})
     * @param args
     *            the list of arguments passed to the formatter. If there are
     *            more arguments than required by {@code format},
     *            additional arguments are ignored.
     * @return the formatted result.
     * @throws IllegalArgumentException
     *            if the pattern cannot be parsed.
     */
    public static String format(String format, Object... args) {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) {
                    args[i] = "null";
                }
            }
        }
        return new MessageFormat(format).format(args);
    }

    /**
     * Returns the {@code Format} instances used by this message format.
     *
     * @return an array of {@code Format} instances.
     */
    public Format[] getFormats() {
        return formats.clone();
    }

    /**
     * Returns the formats used for each argument index. If an argument is
     * placed more than once in the pattern string, then this returns the format
     * of the last one.
     *
     * @return an array of formats, ordered by argument index.
     */
    public Format[] getFormatsByArgumentIndex() {
        Format[] answer = new Format[maxArgumentIndex + 1];
        for (int i = 0; i < maxOffset + 1; i++) {
            answer[argumentNumbers[i]] = formats[i];
        }
        return answer;
    }

    /**
     * Sets the format used for the argument at index {@code argIndex} to
     * {@code format}.
     *
     * @param argIndex
     *            the index of the format to set.
     * @param format
     *            the format that will be set at index {@code argIndex}.
     */
    public void setFormatByArgumentIndex(int argIndex, Format format) {
        for (int i = 0; i < maxOffset + 1; i++) {
            if (argumentNumbers[i] == argIndex) {
                formats[i] = format;
            }
        }
    }

    /**
     * Sets the formats used for each argument. The {@code formats} array
     * elements should be in the order of the argument indices.
     *
     * @param formats
     *            the formats in an array.
     */
    public void setFormatsByArgumentIndex(Format[] formats) {
        for (int j = 0; j < formats.length; j++) {
            for (int i = 0; i < maxOffset + 1; i++) {
                if (argumentNumbers[i] == j) {
                    this.formats[i] = formats[j];
                }
            }
        }
    }

    /**
     * Returns the locale used when creating formats.
     *
     * @return the locale used to create formats.
     */
    public Locale getLocale() {
        return locale;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        for (int i = 0; i <= maxOffset; i++) {
            hashCode += argumentNumbers[i] + strings[i].hashCode();
            if (formats[i] != null) {
                hashCode += formats[i].hashCode();
            }
        }
        if (maxOffset + 1 < strings.length) {
            hashCode += strings[maxOffset + 1].hashCode();
        }
        if (locale != null) {
            return hashCode + locale.hashCode();
        }
        return hashCode;
    }

    /**
     * Parses the message arguments from the specified string using the rules of
     * this message format.
     *
     * @param string
     *            the string to parse.
     * @return the array of {@code Object} arguments resulting from the parse.
     * @throws ParseException
     *            if an error occurs during parsing.
     */
    public Object[] parse(String string) throws ParseException {
        ParsePosition position = new ParsePosition(0);
        Object[] result = parse(string, position);
        if (position.getIndex() == 0) {
            throw new ParseException("Parse failure", position.getErrorIndex());
        }
        return result;
    }

    /**
     * Parses the message argument from the specified string starting at the
     * index specified by {@code position}. If the string is successfully
     * parsed then the index of the {@code ParsePosition} is updated to the
     * index following the parsed text. On error, the index is unchanged and the
     * error index of {@code ParsePosition} is set to the index where the error
     * occurred.
     *
     * @param string
     *            the string to parse.
     * @param position
     *            input/output parameter, specifies the start index in
     *            {@code string} from where to start parsing. If parsing is
     *            successful, it is updated with the index following the parsed
     *            text; on error, the index is unchanged and the error index is
     *            set to the index where the error occurred.
     * @return the array of objects resulting from the parse, or {@code null} if
     *         there is an error.
     */
    public Object[] parse(String string, ParsePosition position) {
        if (string == null) {
            return EmptyArray.OBJECT;
        }
        ParsePosition internalPos = new ParsePosition(0);
        int offset = position.getIndex();
        Object[] result = new Object[maxArgumentIndex + 1];
        for (int i = 0; i <= maxOffset; i++) {
            String sub = strings[i];
            if (!string.startsWith(sub, offset)) {
                position.setErrorIndex(offset);
                return null;
            }
            offset += sub.length();
            Object parse;
            Format format = formats[i];
            if (format == null) {
                if (i + 1 < strings.length) {
                    int next = string.indexOf(strings[i + 1], offset);
                    if (next == -1) {
                        position.setErrorIndex(offset);
                        return null;
                    }
                    parse = string.substring(offset, next);
                    offset = next;
                } else {
                    parse = string.substring(offset);
                    offset = string.length();
                }
            } else {
                internalPos.setIndex(offset);
                parse = format.parseObject(string, internalPos);
                if (internalPos.getErrorIndex() != -1) {
                    position.setErrorIndex(offset);
                    return null;
                }
                offset = internalPos.getIndex();
            }
            result[argumentNumbers[i]] = parse;
        }
        if (maxOffset + 1 < strings.length) {
            String sub = strings[maxOffset + 1];
            if (!string.startsWith(sub, offset)) {
                position.setErrorIndex(offset);
                return null;
            }
            offset += sub.length();
        }
        position.setIndex(offset);
        return result;
    }

    /**
     * Parses the message argument from the specified string starting at the
     * index specified by {@code position}. If the string is successfully
     * parsed then the index of the {@code ParsePosition} is updated to the
     * index following the parsed text. On error, the index is unchanged and the
     * error index of {@code ParsePosition} is set to the index where the error
     * occurred.
     *
     * @param string
     *            the string to parse.
     * @param position
     *            input/output parameter, specifies the start index in
     *            {@code string} from where to start parsing. If parsing is
     *            successful, it is updated with the index following the parsed
     *            text; on error, the index is unchanged and the error index is
     *            set to the index where the error occurred.
     * @return the array of objects resulting from the parse, or {@code null} if
     *         there is an error.
     */
    @Override
    public Object parseObject(String string, ParsePosition position) {
        return parse(string, position);
    }

    private int match(String string, ParsePosition position, boolean last,
            String[] tokens) {
        int length = string.length(), offset = position.getIndex(), token = -1;
        while (offset < length && Character.isWhitespace(string.charAt(offset))) {
            offset++;
        }
        for (int i = tokens.length; --i >= 0;) {
            if (string.regionMatches(true, offset, tokens[i], 0, tokens[i]
                    .length())) {
                token = i;
                break;
            }
        }
        if (token == -1) {
            return -1;
        }
        offset += tokens[token].length();
        while (offset < length && Character.isWhitespace(string.charAt(offset))) {
            offset++;
        }
        char ch;
        if (offset < length
                && ((ch = string.charAt(offset)) == '}' || (!last && ch == ','))) {
            position.setIndex(offset + 1);
            return token;
        }
        return -1;
    }

    private Format parseVariable(String string, ParsePosition position) {
        int length = string.length(), offset = position.getIndex();
        char ch;
        if (offset >= length || ((ch = string.charAt(offset++)) != '}' && ch != ',')) {
            throw new IllegalArgumentException("Missing element format");
        }
        position.setIndex(offset);
        if (ch == '}') {
            return null;
        }
        int type = match(string, position, false,
                new String[] { "time", "date", "number", "choice" });
        if (type == -1) {
            throw new IllegalArgumentException("Unknown element format");
        }
        StringBuffer buffer = new StringBuffer();
        ch = string.charAt(position.getIndex() - 1);
        switch (type) {
            case 0: // time
            case 1: // date
                if (ch == '}') {
                    return type == 1 ? DateFormat.getDateInstance(
                            DateFormat.DEFAULT, locale) : DateFormat
                            .getTimeInstance(DateFormat.DEFAULT, locale);
                }
                int dateStyle = match(string, position, true,
                        new String[] { "full", "long", "medium", "short" });
                if (dateStyle == -1) {
                    Format.upToWithQuotes(string, position, buffer, '}', '{');
                    return new SimpleDateFormat(buffer.toString(), locale);
                }
                switch (dateStyle) {
                    case 0:
                        dateStyle = DateFormat.FULL;
                        break;
                    case 1:
                        dateStyle = DateFormat.LONG;
                        break;
                    case 2:
                        dateStyle = DateFormat.MEDIUM;
                        break;
                    case 3:
                        dateStyle = DateFormat.SHORT;
                        break;
                }
                return type == 1 ? DateFormat
                        .getDateInstance(dateStyle, locale) : DateFormat
                        .getTimeInstance(dateStyle, locale);
            case 2: // number
                if (ch == '}') {
                    return NumberFormat.getInstance(locale);
                }
                int numberStyle = match(string, position, true,
                        new String[] { "currency", "percent", "integer" });
                if (numberStyle == -1) {
                    Format.upToWithQuotes(string, position, buffer, '}', '{');
                    return new DecimalFormat(buffer.toString(),
                            new DecimalFormatSymbols(locale));
                }
                switch (numberStyle) {
                    case 0: // currency
                        return NumberFormat.getCurrencyInstance(locale);
                    case 1: // percent
                        return NumberFormat.getPercentInstance(locale);
                }
                return NumberFormat.getIntegerInstance(locale);
        }
        // choice
        try {
            Format.upToWithQuotes(string, position, buffer, '}', '{');
        } catch (IllegalArgumentException e) {
            // ignored
        }
        return new ChoiceFormat(buffer.toString());
    }

    /**
     * Sets the specified format used by this message format.
     *
     * @param offset
     *            the index of the format to change.
     * @param format
     *            the {@code Format} that replaces the old format.
     */
    public void setFormat(int offset, Format format) {
        formats[offset] = format;
    }

    /**
     * Sets the formats used by this message format.
     *
     * @param formats
     *            an array of {@code Format}.
     */
    public void setFormats(Format[] formats) {
        int min = this.formats.length;
        if (formats.length < min) {
            min = formats.length;
        }
        for (int i = 0; i < min; i++) {
            this.formats[i] = formats[i];
        }
    }

    /**
     * Sets the locale to use when creating {@code Format} instances. Changing
     * the locale may change the behavior of {@code applyPattern},
     * {@code toPattern}, {@code format} and {@code formatToCharacterIterator}.
     *
     * @param locale
     *            the new locale.
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
        for (int i = 0; i <= maxOffset; i++) {
            Format format = formats[i];
            // java specification undefined for null argument, change into
            // a more tolerant implementation
            if (format instanceof DecimalFormat) {
                try {
                    formats[i] = new DecimalFormat(((DecimalFormat) format)
                            .toPattern(), new DecimalFormatSymbols(locale));
                } catch (NullPointerException npe){
                    formats[i] = null;
                }
            } else if (format instanceof SimpleDateFormat) {
                try {
                    formats[i] = new SimpleDateFormat(((SimpleDateFormat) format)
                            .toPattern(), locale);
                } catch (NullPointerException npe) {
                    formats[i] = null;
                }
            }
        }
    }

    private String decodeDecimalFormat(StringBuffer buffer, Format format) {
        buffer.append(",number");
        if (format.equals(NumberFormat.getNumberInstance(locale))) {
            // Empty block
        } else if (format.equals(NumberFormat.getIntegerInstance(locale))) {
            buffer.append(",integer");
        } else if (format.equals(NumberFormat.getCurrencyInstance(locale))) {
            buffer.append(",currency");
        } else if (format.equals(NumberFormat.getPercentInstance(locale))) {
            buffer.append(",percent");
        } else {
            buffer.append(',');
            return ((DecimalFormat) format).toPattern();
        }
        return null;
    }

    private String decodeSimpleDateFormat(StringBuffer buffer, Format format) {
        if (format.equals(DateFormat.getTimeInstance(DateFormat.DEFAULT, locale))) {
            buffer.append(",time");
        } else if (format.equals(DateFormat.getDateInstance(DateFormat.DEFAULT,
                locale))) {
            buffer.append(",date");
        } else if (format.equals(DateFormat.getTimeInstance(DateFormat.SHORT,
                locale))) {
            buffer.append(",time,short");
        } else if (format.equals(DateFormat.getDateInstance(DateFormat.SHORT,
                locale))) {
            buffer.append(",date,short");
        } else if (format.equals(DateFormat.getTimeInstance(DateFormat.LONG,
                locale))) {
            buffer.append(",time,long");
        } else if (format.equals(DateFormat.getDateInstance(DateFormat.LONG,
                locale))) {
            buffer.append(",date,long");
        } else if (format.equals(DateFormat.getTimeInstance(DateFormat.FULL,
                locale))) {
            buffer.append(",time,full");
        } else if (format.equals(DateFormat.getDateInstance(DateFormat.FULL,
                locale))) {
            buffer.append(",date,full");
        } else {
            buffer.append(",date,");
            return ((SimpleDateFormat) format).toPattern();
        }
        return null;
    }

    /**
     * Returns the pattern of this message format.
     *
     * @return the pattern.
     */
    public String toPattern() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i <= maxOffset; i++) {
            appendQuoted(buffer, strings[i]);
            buffer.append('{');
            buffer.append(argumentNumbers[i]);
            Format format = formats[i];
            String pattern = null;
            if (format instanceof ChoiceFormat) {
                buffer.append(",choice,");
                pattern = ((ChoiceFormat) format).toPattern();
            } else if (format instanceof DecimalFormat) {
                pattern = decodeDecimalFormat(buffer, format);
            } else if (format instanceof SimpleDateFormat) {
                pattern = decodeSimpleDateFormat(buffer, format);
            } else if (format != null) {
                throw new IllegalArgumentException("Unknown format");
            }
            if (pattern != null) {
                boolean quote = false;
                int index = 0, length = pattern.length(), count = 0;
                while (index < length) {
                    char ch = pattern.charAt(index++);
                    if (ch == '\'') {
                        quote = !quote;
                    }
                    if (!quote) {
                        if (ch == '{') {
                            count++;
                        }
                        if (ch == '}') {
                            if (count > 0) {
                                count--;
                            } else {
                                buffer.append("'}");
                                ch = '\'';
                            }
                        }
                    }
                    buffer.append(ch);
                }
            }
            buffer.append('}');
        }
        if (maxOffset + 1 < strings.length) {
            appendQuoted(buffer, strings[maxOffset + 1]);
        }
        return buffer.toString();
    }

    private void appendQuoted(StringBuffer buffer, String string) {
        int length = string.length();
        for (int i = 0; i < length; i++) {
            char ch = string.charAt(i);
            if (ch == '{' || ch == '}') {
                buffer.append('\'');
                buffer.append(ch);
                buffer.append('\'');
            } else {
                buffer.append(ch);
            }
        }
    }

    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("argumentNumbers", int[].class),
        new ObjectStreamField("formats", Format[].class),
        new ObjectStreamField("locale", Locale.class),
        new ObjectStreamField("maxOffset", int.class),
        new ObjectStreamField("offsets", int[].class),
        new ObjectStreamField("pattern", String.class),
    };

    private void writeObject(ObjectOutputStream stream) throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("argumentNumbers", argumentNumbers);
        Format[] compatibleFormats = formats;
        fields.put("formats", compatibleFormats);
        fields.put("locale", locale);
        fields.put("maxOffset", maxOffset);
        int offset = 0;
        int offsetsLength = maxOffset + 1;
        int[] offsets = new int[offsetsLength];
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i <= maxOffset; i++) {
            offset += strings[i].length();
            offsets[i] = offset;
            pattern.append(strings[i]);
        }
        if (maxOffset + 1 < strings.length) {
            pattern.append(strings[maxOffset + 1]);
        }
        fields.put("offsets", offsets);
        fields.put("pattern", pattern.toString());
        stream.writeFields();
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        ObjectInputStream.GetField fields = stream.readFields();
        argumentNumbers = (int[]) fields.get("argumentNumbers", null);
        formats = (Format[]) fields.get("formats", null);
        locale = (Locale) fields.get("locale", null);
        maxOffset = fields.get("maxOffset", 0);
        int[] offsets = (int[]) fields.get("offsets", null);
        String pattern = (String) fields.get("pattern", null);
        int length;
        if (maxOffset < 0) {
            length = pattern.length() > 0 ? 1 : 0;
        } else {
            length = maxOffset
                    + (offsets[maxOffset] == pattern.length() ? 1 : 2);
        }
        strings = new String[length];
        int last = 0;
        for (int i = 0; i <= maxOffset; i++) {
            strings[i] = pattern.substring(last, offsets[i]);
            last = offsets[i];
        }
        if (maxOffset + 1 < strings.length) {
            strings[strings.length - 1] = pattern.substring(last, pattern
                    .length());
        }
    }

    /**
     * The instances of this inner class are used as attribute keys in
     * {@code AttributedCharacterIterator} that the
     * {@link MessageFormat#formatToCharacterIterator(Object)} method returns.
     * <p>
     * There is no public constructor in this class, the only instances are the
     * constants defined here.
     */
    public static class Field extends Format.Field {

        private static final long serialVersionUID = 7899943957617360810L;

        /**
         * This constant stands for the message argument.
         */
        public static final Field ARGUMENT = new Field("message argument field");

        /**
         * Constructs a new instance of {@code MessageFormat.Field} with the
         * given field name.
         *
         * @param fieldName
         *            the field name.
         */
        protected Field(String fieldName) {
            super(fieldName);
        }
    }
}
