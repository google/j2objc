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

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * A {@code Properties} object is a {@code Hashtable} where the keys and values
 * must be {@code String}s. Each property can have a default
 * {@code Properties} list which specifies the default
 * values to be used when a given key is not found in this {@code Properties}
 * instance.
 *
 * <a name="character_encoding"><h3>Character Encoding</h3></a>
 * <p>Note that in some cases {@code Properties} uses ISO-8859-1 instead of UTF-8.
 * ISO-8859-1 is only capable of representing a tiny subset of Unicode.
 * Use either the {@code loadFromXML}/{@code storeToXML} methods (which use UTF-8 by
 * default) or the {@code load}/{@code store} overloads that take
 * an {@code OutputStreamWriter} (so you can supply a UTF-8 instance) instead.
 *
 * @see Hashtable
 * @see java.lang.System#getProperties
 */
public class Properties extends Hashtable<Object, Object> {

    private static final long serialVersionUID = 4112578634029874840L;

    private static final String PROP_DTD_NAME = "http://java.sun.com/dtd/properties.dtd";

    private static final String PROP_DTD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "    <!ELEMENT properties (comment?, entry*) >"
            + "    <!ATTLIST properties version CDATA #FIXED \"1.0\" >"
            + "    <!ELEMENT comment (#PCDATA) >"
            + "    <!ELEMENT entry (#PCDATA) >"
            + "    <!ATTLIST entry key CDATA #REQUIRED >";

    /**
     * The default values for keys not found in this {@code Properties}
     * instance.
     */
    protected Properties defaults;

    private static final int NONE = 0, SLASH = 1, UNICODE = 2, CONTINUE = 3,
            KEY_DONE = 4, IGNORE = 5;

    /**
     * Constructs a new {@code Properties} object.
     */
    public Properties() {
    }

    /**
     * Constructs a new {@code Properties} object using the specified default
     * {@code Properties}.
     *
     * @param properties
     *            the default {@code Properties}.
     */
    public Properties(Properties properties) {
        defaults = properties;
    }

    private void dumpString(StringBuilder buffer, String string, boolean key) {
        int i = 0;
        if (!key && i < string.length() && string.charAt(i) == ' ') {
            buffer.append("\\ ");
            i++;
        }

        for (; i < string.length(); i++) {
            char ch = string.charAt(i);
            switch (ch) {
            case '\t':
                buffer.append("\\t");
                break;
            case '\n':
                buffer.append("\\n");
                break;
            case '\f':
                buffer.append("\\f");
                break;
            case '\r':
                buffer.append("\\r");
                break;
            default:
                if ("\\#!=:".indexOf(ch) >= 0 || (key && ch == ' ')) {
                    buffer.append('\\');
                }
                if (ch >= ' ' && ch <= '~') {
                    buffer.append(ch);
                } else {
                    String hex = Integer.toHexString(ch);
                    buffer.append("\\u");
                    for (int j = 0; j < 4 - hex.length(); j++) {
                        buffer.append("0");
                    }
                    buffer.append(hex);
                }
            }
        }
    }

    /**
     * Searches for the property with the specified name. If the property is not
     * found, the default {@code Properties} are checked. If the property is not
     * found in the default {@code Properties}, {@code null} is returned.
     *
     * @param name
     *            the name of the property to find.
     * @return the named property value, or {@code null} if it can't be found.
     */
    public String getProperty(String name) {
        Object result = super.get(name);
        String property = result instanceof String ? (String) result : null;
        if (property == null && defaults != null) {
            property = defaults.getProperty(name);
        }
        return property;
    }

    /**
     * Searches for the property with the specified name. If the property is not
     * found, it looks in the default {@code Properties}. If the property is not
     * found in the default {@code Properties}, it returns the specified
     * default.
     *
     * @param name
     *            the name of the property to find.
     * @param defaultValue
     *            the default value.
     * @return the named property value.
     */
    public String getProperty(String name, String defaultValue) {
        Object result = super.get(name);
        String property = result instanceof String ? (String) result : null;
        if (property == null && defaults != null) {
            property = defaults.getProperty(name);
        }
        if (property == null) {
            return defaultValue;
        }
        return property;
    }

    /**
     * Lists the mappings in this {@code Properties} to {@code out} in a human-readable form.
     * Note that values are truncated to 37 characters, so this method is rarely useful.
     */
    public void list(PrintStream out) {
        listToAppendable(out);
    }

    /**
     * Lists the mappings in this {@code Properties} to {@code out} in a human-readable form.
     * Note that values are truncated to 37 characters, so this method is rarely useful.
     */
    public void list(PrintWriter out) {
        listToAppendable(out);
    }

    private void listToAppendable(Appendable out) {
        try {
            if (out == null) {
                throw new NullPointerException("out == null");
            }
            StringBuilder sb = new StringBuilder(80);
            Enumeration<?> keys = propertyNames();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                sb.append(key);
                sb.append('=');
                String property = (String) super.get(key);
                Properties def = defaults;
                while (property == null) {
                    property = (String) def.get(key);
                    def = def.defaults;
                }
                if (property.length() > 40) {
                    sb.append(property.substring(0, 37));
                    sb.append("...");
                } else {
                    sb.append(property);
                }
                sb.append(System.lineSeparator());
                out.append(sb.toString());
                sb.setLength(0);
            }
        } catch (IOException ex) {
            // Appendable.append throws IOException, but PrintStream and PrintWriter don't.
            throw new AssertionError(ex);
        }
    }

    /**
     * Loads properties from the specified {@code InputStream}, assumed to be ISO-8859-1.
     * See "<a href="#character_encoding">Character Encoding</a>".
     *
     * @param in the {@code InputStream}
     * @throws IOException
     */
    public synchronized void load(InputStream in) throws IOException {
        if (in == null) {
            throw new NullPointerException("in == null");
        }
        load(new InputStreamReader(in, "ISO-8859-1"));
    }

    /**
     * Loads properties from the specified {@code Reader}.
     * The properties file is interpreted according to the following rules:
     * <ul>
     * <li>Empty lines are ignored.</li>
     * <li>Lines starting with either a "#" or a "!" are comment lines and are
     * ignored.</li>
     * <li>A backslash at the end of the line escapes the following newline
     * character ("\r", "\n", "\r\n"). If there's whitespace after the
     * backslash it will just escape that whitespace instead of concatenating
     * the lines. This does not apply to comment lines.</li>
     * <li>A property line consists of the key, the space between the key and
     * the value, and the value. The key goes up to the first whitespace, "=" or
     * ":" that is not escaped. The space between the key and the value contains
     * either one whitespace, one "=" or one ":" and any amount of additional
     * whitespace before and after that character. The value starts with the
     * first character after the space between the key and the value.</li>
     * <li>Following escape sequences are recognized: "\ ", "\\", "\r", "\n",
     * "\!", "\#", "\t", "\b", "\f", and "&#92;uXXXX" (unicode character).</li>
     * </ul>
     *
     * @param in the {@code Reader}
     * @throws IOException
     * @since 1.6
     */
    @SuppressWarnings("fallthrough")
    public synchronized void load(Reader in) throws IOException {
        if (in == null) {
            throw new NullPointerException("in == null");
        }
        int mode = NONE, unicode = 0, count = 0;
        char nextChar;
        char buf[] = new char[40];
        int offset = 0, keyLength = -1, intVal;
        boolean firstChar = true;

        BufferedReader br = new BufferedReader(in);

        while (true) {
            intVal = br.read();
            if (intVal == -1) {
                break;
            }
            nextChar = (char) intVal;

            if (offset == buf.length) {
                char[] newBuf = new char[buf.length * 2];
                System.arraycopy(buf, 0, newBuf, 0, offset);
                buf = newBuf;
            }
            if (mode == UNICODE) {
                int digit = Character.digit(nextChar, 16);
                if (digit >= 0) {
                    unicode = (unicode << 4) + digit;
                    if (++count < 4) {
                        continue;
                    }
                } else if (count <= 4) {
                    throw new IllegalArgumentException("Invalid Unicode sequence: illegal character");
                }
                mode = NONE;
                buf[offset++] = (char) unicode;
                if (nextChar != '\n') {
                    continue;
                }
            }
            if (mode == SLASH) {
                mode = NONE;
                switch (nextChar) {
                case '\r':
                    mode = CONTINUE; // Look for a following \n
                    continue;
                case '\n':
                    mode = IGNORE; // Ignore whitespace on the next line
                    continue;
                case 'b':
                    nextChar = '\b';
                    break;
                case 'f':
                    nextChar = '\f';
                    break;
                case 'n':
                    nextChar = '\n';
                    break;
                case 'r':
                    nextChar = '\r';
                    break;
                case 't':
                    nextChar = '\t';
                    break;
                case 'u':
                    mode = UNICODE;
                    unicode = count = 0;
                    continue;
                }
            } else {
                switch (nextChar) {
                case '#':
                case '!':
                    if (firstChar) {
                        while (true) {
                            intVal = br.read();
                            if (intVal == -1) {
                                break;
                            }
                            nextChar = (char) intVal;
                            if (nextChar == '\r' || nextChar == '\n') {
                                break;
                            }
                        }
                        continue;
                    }
                    break;
                case '\n':
                    if (mode == CONTINUE) { // Part of a \r\n sequence
                        mode = IGNORE; // Ignore whitespace on the next line
                        continue;
                    }
                    // fall into the next case
                case '\r':
                    mode = NONE;
                    firstChar = true;
                    if (offset > 0 || (offset == 0 && keyLength == 0)) {
                        if (keyLength == -1) {
                            keyLength = offset;
                        }
                        String temp = new String(buf, 0, offset);
                        put(temp.substring(0, keyLength), temp
                                .substring(keyLength));
                    }
                    keyLength = -1;
                    offset = 0;
                    continue;
                case '\\':
                    if (mode == KEY_DONE) {
                        keyLength = offset;
                    }
                    mode = SLASH;
                    continue;
                case ':':
                case '=':
                    if (keyLength == -1) { // if parsing the key
                        mode = NONE;
                        keyLength = offset;
                        continue;
                    }
                    break;
                }
                if (Character.isWhitespace(nextChar)) {
                    if (mode == CONTINUE) {
                        mode = IGNORE;
                    }
                    // if key length == 0 or value length == 0
                    if (offset == 0 || offset == keyLength || mode == IGNORE) {
                        continue;
                    }
                    if (keyLength == -1) { // if parsing the key
                        mode = KEY_DONE;
                        continue;
                    }
                }
                if (mode == IGNORE || mode == CONTINUE) {
                    mode = NONE;
                }
            }
            firstChar = false;
            if (mode == KEY_DONE) {
                keyLength = offset;
                mode = NONE;
            }
            buf[offset++] = nextChar;
        }
        if (mode == UNICODE && count <= 4) {
            throw new IllegalArgumentException("Invalid Unicode sequence: expected format \\uxxxx");
        }
        if (keyLength == -1 && offset > 0) {
            keyLength = offset;
        }
        if (keyLength >= 0) {
            String temp = new String(buf, 0, offset);
            String key = temp.substring(0, keyLength);
            String value = temp.substring(keyLength);
            if (mode == SLASH) {
                value += "\u0000";
            }
            put(key, value);
        }
    }

    /**
     * Returns all of the property names (keys) in this {@code Properties} object.
     */
    public Enumeration<?> propertyNames() {
        Hashtable<Object, Object> selected = new Hashtable<Object, Object>();
        selectProperties(selected, false);
        return selected.keys();
    }

    /**
     * Returns those property names (keys) in this {@code Properties} object for which
     * both key and value are strings.
     *
     * @return a set of keys in the property list
     * @since 1.6
     */
    public Set<String> stringPropertyNames() {
        Hashtable<String, Object> stringProperties = new Hashtable<String, Object>();
        selectProperties(stringProperties, true);
        return Collections.unmodifiableSet(stringProperties.keySet());
    }

    private <K> void selectProperties(Hashtable<K, Object> selectProperties, final boolean isStringOnly) {
        if (defaults != null) {
            defaults.selectProperties(selectProperties, isStringOnly);
        }
        Enumeration<Object> keys = keys();
        while (keys.hasMoreElements()) {
            @SuppressWarnings("unchecked")
            K key = (K) keys.nextElement();
            if (isStringOnly && !(key instanceof String)) {
                // Only select property with string key and value
                continue;
            }
            Object value = get(key);
            selectProperties.put(key, value);
        }
    }

    /**
     * Saves the mappings in this {@code Properties} to the specified {@code
     * OutputStream}, putting the specified comment at the beginning. The output
     * from this method is suitable for being read by the
     * {@link #load(InputStream)} method.
     *
     * @param out the {@code OutputStream} to write to.
     * @param comment the comment to add at the beginning.
     * @throws ClassCastException if the key or value of a mapping is not a
     *                String.
     * @deprecated This method ignores any {@code IOException} thrown while
     *             writing -- use {@link #store} instead for better exception
     *             handling.
     */
    @Deprecated
    public void save(OutputStream out, String comment) {
        try {
            store(out, comment);
        } catch (IOException e) {
        }
    }

    /**
     * Maps the specified key to the specified value. If the key already exists,
     * the old value is replaced. The key and value cannot be {@code null}.
     *
     * @param name
     *            the key.
     * @param value
     *            the value.
     * @return the old value mapped to the key, or {@code null}.
     */
    public Object setProperty(String name, String value) {
        return put(name, value);
    }

    /**
     * Stores properties to the specified {@code OutputStream}, using ISO-8859-1.
     * See "<a href="#character_encoding">Character Encoding</a>".
     *
     * @param out the {@code OutputStream}
     * @param comment an optional comment to be written, or null
     * @throws IOException
     * @throws ClassCastException if a key or value is not a string
     */
    public synchronized void store(OutputStream out, String comment) throws IOException {
        store(new OutputStreamWriter(out, "ISO-8859-1"), comment);
    }

    /**
     * Stores the mappings in this {@code Properties} object to {@code out},
     * putting the specified comment at the beginning.
     *
     * @param writer the {@code Writer}
     * @param comment an optional comment to be written, or null
     * @throws IOException
     * @throws ClassCastException if a key or value is not a string
     * @since 1.6
     */
    public synchronized void store(Writer writer, String comment) throws IOException {
        if (comment != null) {
            writer.write("#");
            writer.write(comment);
            writer.write(System.lineSeparator());
        }
        writer.write("#");
        writer.write(new Date().toString());
        writer.write(System.lineSeparator());

        StringBuilder sb = new StringBuilder(200);
        for (Map.Entry<Object, Object> entry : entrySet()) {
            String key = (String) entry.getKey();
            dumpString(sb, key, true);
            sb.append('=');
            dumpString(sb, (String) entry.getValue(), false);
            sb.append(System.lineSeparator());
            writer.write(sb.toString());
            sb.setLength(0);
        }
        writer.flush();
    }

    /**
     * Loads the properties from an {@code InputStream} containing the
     * properties in XML form. The XML document must begin with (and conform to)
     * following DOCTYPE:
     *
     * <pre>
     * &lt;!DOCTYPE properties SYSTEM &quot;http://java.sun.com/dtd/properties.dtd&quot;&gt;
     * </pre>
     *
     * Also the content of the XML data must satisfy the DTD but the xml is not
     * validated against it. The DTD is not loaded from the SYSTEM ID. After
     * this method returns the InputStream is not closed.
     *
     * @param in the InputStream containing the XML document.
     * @throws IOException in case an error occurs during a read operation.
     * @throws InvalidPropertiesFormatException if the XML data is not a valid
     *             properties file.
     */
    public synchronized void loadFromXML(InputStream in) throws IOException,
            InvalidPropertiesFormatException {
        if (in == null) {
            throw new NullPointerException("in == null");
        }
        
        try {
            XMLReader reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(new DefaultHandler() {
                private String key;
                
                @Override
                public void startElement(String uri, String localName,
                        String qName, Attributes attributes) throws SAXException {
            	key = null;
                    if (qName.equals("entry")) {
                        key = attributes.getValue("key");
                    }
                }
                
                @Override
                public void characters(char[] ch, int start, int length)
                        throws SAXException {
                    if (key != null) {
                        String value = new String(ch, start, length);
                        put(key, value);
                        key = null;
                    }
                }
            });
            reader.parse(new InputSource(in));
        } catch (SAXException e) {
            throw new InvalidPropertiesFormatException(e);
        }
    }

    /**
     * Writes all properties stored in this instance into the {@code
     * OutputStream} in XML representation. The DOCTYPE is
     *
     * <pre>
     * &lt;!DOCTYPE properties SYSTEM &quot;http://java.sun.com/dtd/properties.dtd&quot;&gt;
     * </pre>
     *
     * If the comment is null, no comment is added to the output. UTF-8 is used
     * as the encoding. The {@code OutputStream} is not closed at the end. A
     * call to this method is the same as a call to {@code storeToXML(os,
     * comment, "UTF-8")}.
     *
     * @param os the {@code OutputStream} to write to.
     * @param comment the comment to add. If null, no comment is added.
     * @throws IOException if an error occurs during writing to the output.
     */
    public void storeToXML(OutputStream os, String comment) throws IOException {
        storeToXML(os, comment, "UTF-8");
    }

    /**
     * Writes all properties stored in this instance into the {@code
     * OutputStream} in XML representation. The DOCTYPE is
     *
     * <pre>
     * &lt;!DOCTYPE properties SYSTEM &quot;http://java.sun.com/dtd/properties.dtd&quot;&gt;
     * </pre>
     *
     * If the comment is null, no comment is added to the output. The parameter
     * {@code encoding} defines which encoding should be used. The {@code
     * OutputStream} is not closed at the end.
     *
     * @param os the {@code OutputStream} to write to.
     * @param comment the comment to add. If null, no comment is added.
     * @param encoding the code identifying the encoding that should be used to
     *            write into the {@code OutputStream}.
     * @throws IOException if an error occurs during writing to the output.
     */
    public synchronized void storeToXML(OutputStream os, String comment,
            String encoding) throws IOException {

        if (os == null) {
            throw new NullPointerException("os == null");
        } else if (encoding == null) {
            throw new NullPointerException("encoding == null");
        }

        /*
         * We can write to XML file using encoding parameter but note that some
         * aliases for encodings are not supported by the XML parser. Thus we
         * have to know canonical name for encoding used to store data in XML
         * since the XML parser must recognize encoding name used to store data.
         */

        String encodingCanonicalName;
        try {
            encodingCanonicalName = Charset.forName(encoding).name();
        } catch (IllegalCharsetNameException e) {
            System.out.println("Warning: encoding name " + encoding
                    + " is illegal, using UTF-8 as default encoding");
            encodingCanonicalName = "UTF-8";
        } catch (UnsupportedCharsetException e) {
            System.out.println("Warning: encoding " + encoding
                    + " is not supported, using UTF-8 as default encoding");
            encodingCanonicalName = "UTF-8";
        }

        PrintStream printStream = new PrintStream(os, false,
                encodingCanonicalName);

        printStream.print("<?xml version=\"1.0\" encoding=\"");
        printStream.print(encodingCanonicalName);
        printStream.println("\"?>");

        printStream.print("<!DOCTYPE properties SYSTEM \"");
        printStream.print(PROP_DTD_NAME);
        printStream.println("\">");

        printStream.println("<properties>");

        if (comment != null) {
            printStream.print("<comment>");
            printStream.print(substitutePredefinedEntries(comment));
            printStream.println("</comment>");
        }

        for (Map.Entry<Object, Object> entry : entrySet()) {
            String keyValue = (String) entry.getKey();
            String entryValue = (String) entry.getValue();
            printStream.print("<entry key=\"");
            printStream.print(substitutePredefinedEntries(keyValue));
            printStream.print("\">");
            printStream.print(substitutePredefinedEntries(entryValue));
            printStream.println("</entry>");
        }
        printStream.println("</properties>");
        printStream.flush();
    }

    private String substitutePredefinedEntries(String s) {
        // substitution for predefined character entities to use them safely in XML.
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        s = s.replaceAll("'", "&apos;");
        s = s.replaceAll("\"", "&quot;");
        return s;
    }
}
