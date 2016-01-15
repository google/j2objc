/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id: Encodings.java 471981 2006-11-07 04:28:00Z minchau $
 */
package org.apache.xml.serializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;


/**
 * Provides information about encodings. Depends on the Java runtime
 * to provides writers for the different encodings.
 * <p>
 * This class is not a public API. It is only public because it
 * is used outside of this package.
 * 
 * @xsl.usage internal
 */

public final class Encodings extends Object
{
    /**
     * Standard filename for properties file with encodings data.
     */
    private static final String ENCODINGS_FILE = SerializerBase.PKG_PATH+"/Encodings.properties";

    /**
     * Returns a writer for the specified encoding based on
     * an output stream.
     * <p>
     * This is not a public API.
     * @param output The output stream
     * @param encoding The encoding MIME name, not a Java name for the encoding.
     * @return A suitable writer
     * @throws UnsupportedEncodingException There is no convertor
     *  to support this encoding
     * @xsl.usage internal
     */
    static Writer getWriter(OutputStream output, String encoding)
        throws UnsupportedEncodingException
    {

        for (int i = 0; i < _encodings.length; ++i)
        {
            if (_encodings[i].name.equalsIgnoreCase(encoding))
            {
                try
                {
                    String javaName = _encodings[i].javaName;
                	OutputStreamWriter osw = new OutputStreamWriter(output,javaName);
                    return osw; 
                }
                catch (java.lang.IllegalArgumentException iae) // java 1.1.8
                {
                    // keep trying
                }
                catch (UnsupportedEncodingException usee)
                {

                    // keep trying
                }
            }
        }

        try
        {
            return new OutputStreamWriter(output, encoding);
        }
        catch (java.lang.IllegalArgumentException iae) // java 1.1.8
        {
            throw new UnsupportedEncodingException(encoding);
        }
    }

    /**
     * Returns the EncodingInfo object for the specified
     * encoding, never null, although the encoding name 
     * inside the returned EncodingInfo object will be if
     * we can't find a "real" EncodingInfo for the encoding.
     * <p>
     * This is not a public API.
     *
     * @param encoding The encoding
     * @return The object that is used to determine if 
     * characters are in the given encoding.
     * @xsl.usage internal
     */
    static EncodingInfo getEncodingInfo(String encoding)
    {
        EncodingInfo ei;

        String normalizedEncoding = toUpperCaseFast(encoding);
        ei = (EncodingInfo) _encodingTableKeyJava.get(normalizedEncoding);
        if (ei == null)
            ei = (EncodingInfo) _encodingTableKeyMime.get(normalizedEncoding);
        if (ei == null) {
            // We shouldn't have to do this, but just in case.
            ei = new EncodingInfo(null,null, '\u0000');
        }

        return ei;
    }
 
    /**
     * Determines if the encoding specified was recognized by the
     * serializer or not.
     *
     * @param encoding The encoding
     * @return boolean - true if the encoding was recognized else false
     */
    public static boolean isRecognizedEncoding(String encoding)
    {
        EncodingInfo ei;

        String normalizedEncoding = encoding.toUpperCase();
        ei = (EncodingInfo) _encodingTableKeyJava.get(normalizedEncoding);
        if (ei == null)
            ei = (EncodingInfo) _encodingTableKeyMime.get(normalizedEncoding);
        if (ei != null)
            return true;
        return false;
    }
    
    /**
     * A fast and cheap way to uppercase a String that is
     * only made of printable ASCII characters.
     * <p>
     * This is not a public API.
     * @param s a String of ASCII characters
     * @return an uppercased version of the input String,
     * possibly the same String.
     * @xsl.usage internal
     */
    static private String toUpperCaseFast(final String s) {

    	boolean different = false;
    	final int mx = s.length();
		char[] chars = new char[mx];
    	for (int i=0; i < mx; i++) {
    		char ch = s.charAt(i);
            // is the character a lower case ASCII one?
    		if ('a' <= ch && ch <= 'z') {
                // a cheap and fast way to uppercase that is good enough
    			ch = (char) (ch + ('A' - 'a'));
    			different = true; // the uppercased String is different
    		}
    		chars[i] = ch;
    	}
    	
    	// A little optimization, don't call String.valueOf() if
    	// the uppercased string is the same as the input string.
    	final String upper;
    	if (different) 
    		upper = String.valueOf(chars);
    	else
    		upper = s;
    		
    	return upper;
    }

    /** The default encoding, ISO style, ISO style.   */
    static final String DEFAULT_MIME_ENCODING = "UTF-8";

    /**
     * Get the proper mime encoding.  From the XSLT recommendation: "The encoding
     * attribute specifies the preferred encoding to use for outputting the result
     * tree. XSLT processors are required to respect values of UTF-8 and UTF-16.
     * For other values, if the XSLT processor does not support the specified
     * encoding it may signal an error; if it does not signal an error it should
     * use UTF-8 or UTF-16 instead. The XSLT processor must not use an encoding
     * whose name does not match the EncName production of the XML Recommendation
     * [XML]. If no encoding attribute is specified, then the XSLT processor should
     * use either UTF-8 or UTF-16."
     * <p>
     * This is not a public API.
     *
     * @param encoding Reference to java-style encoding string, which may be null,
     * in which case a default will be found.
     *
     * @return The ISO-style encoding string, or null if failure.
     * @xsl.usage internal
     */
    static String getMimeEncoding(String encoding)
    {

        if (null == encoding)
        {
            try
            {

                // Get the default system character encoding.  This may be
                // incorrect if they passed in a writer, but right now there
                // seems to be no way to get the encoding from a writer.
                encoding = System.getProperty("file.encoding", "UTF8");

                if (null != encoding)
                {

                    /*
                    * See if the mime type is equal to UTF8.  If you don't
                    * do that, then  convertJava2MimeEncoding will convert
                    * 8859_1 to "ISO-8859-1", which is not what we want,
                    * I think, and I don't think I want to alter the tables
                    * to convert everything to UTF-8.
                    */
                    String jencoding =
                        (encoding.equalsIgnoreCase("Cp1252")
                            || encoding.equalsIgnoreCase("ISO8859_1")
                            || encoding.equalsIgnoreCase("8859_1")
                            || encoding.equalsIgnoreCase("UTF8"))
                            ? DEFAULT_MIME_ENCODING
                            : convertJava2MimeEncoding(encoding);

                    encoding =
                        (null != jencoding) ? jencoding : DEFAULT_MIME_ENCODING;
                }
                else
                {
                    encoding = DEFAULT_MIME_ENCODING;
                }
            }
            catch (SecurityException se)
            {
                encoding = DEFAULT_MIME_ENCODING;
            }
        }
        else
        {
            encoding = convertJava2MimeEncoding(encoding);
        }

        return encoding;
    }

    /**
     * Try the best we can to convert a Java encoding to a XML-style encoding.
     * <p>
     * This is not a public API.
     * @param encoding non-null reference to encoding string, java style.
     *
     * @return ISO-style encoding string.
     * @xsl.usage internal
     */
    private static String convertJava2MimeEncoding(String encoding)
    {
        EncodingInfo enc =
            (EncodingInfo) _encodingTableKeyJava.get(toUpperCaseFast(encoding));
        if (null != enc)
            return enc.name;
        return encoding;
    }

    /**
     * Try the best we can to convert a Java encoding to a XML-style encoding.
     * <p>
     * This is not a public API.
     *
     * @param encoding non-null reference to encoding string, java style.
     *
     * @return ISO-style encoding string.
     * <p>
     * This method is not a public API.
     * @xsl.usage internal
     */
    public static String convertMime2JavaEncoding(String encoding)
    {

        for (int i = 0; i < _encodings.length; ++i)
        {
            if (_encodings[i].name.equalsIgnoreCase(encoding))
            {
                return _encodings[i].javaName;
            }
        }

        return encoding;
    }

    /**
     * Load a list of all the supported encodings.
     *
     * System property "encodings" formatted using URL syntax may define an
     * external encodings list. Thanks to Sergey Ushakov for the code
     * contribution!
     * @xsl.usage internal
     */
    private static EncodingInfo[] loadEncodingInfo()
    {
        try
        {
            final InputStream is; 
                
            SecuritySupport ss = SecuritySupport.getInstance();
            is = ss.getResourceAsStream(ObjectFactory.findClassLoader(),
                                            ENCODINGS_FILE);

            Properties props = new Properties();
            if (is != null) {
                props.load(is);
                is.close();
            } else {
                // Seems to be no real need to force failure here, let the
                // system do its best... The issue is not really very critical,
                // and the output will be in any case _correct_ though maybe not
                // always human-friendly... :)
                // But maybe report/log the resource problem?
                // Any standard ways to report/log errors (in static context)?
            }

            int totalEntries = props.size();

            List encodingInfo_list = new ArrayList();
            Enumeration keys = props.keys();
            for (int i = 0; i < totalEntries; ++i)
            {
                String javaName = (String) keys.nextElement();
                String val = props.getProperty(javaName);
                int len = lengthOfMimeNames(val);

                String mimeName;
                char highChar;
                if (len == 0)
                {
                    // There is no property value, only the javaName, so try and recover
                    mimeName = javaName;
                    highChar = '\u0000'; // don't know the high code point, will need to test every character
                }
                else
                {
                    try {
                        // Get the substring after the Mime names
                        final String highVal = val.substring(len).trim();
                        highChar = (char) Integer.decode(highVal).intValue();
                    }
                    catch( NumberFormatException e) {
                        highChar = 0;
                    }
                    String mimeNames = val.substring(0, len);
                    StringTokenizer st =
                        new StringTokenizer(mimeNames, ",");
                    for (boolean first = true;
                        st.hasMoreTokens();
                        first = false)
                    {
                        mimeName = st.nextToken();
                        EncodingInfo ei = new EncodingInfo(mimeName, javaName, highChar);
                        encodingInfo_list.add(ei);
                        _encodingTableKeyMime.put(mimeName.toUpperCase(), ei);
                        if (first)
                            _encodingTableKeyJava.put(javaName.toUpperCase(), ei);
                    }
                }
            }
            // Convert the Vector of EncodingInfo objects into an array of them,
            // as that is the kind of thing this method returns.
            EncodingInfo[] ret_ei = new EncodingInfo[encodingInfo_list.size()];
            encodingInfo_list.toArray(ret_ei);
            return ret_ei;
        }
        catch (java.net.MalformedURLException mue)
        {
            throw new org.apache.xml.serializer.utils.WrappedRuntimeException(mue);
        }
        catch (java.io.IOException ioe)
        {
            throw new org.apache.xml.serializer.utils.WrappedRuntimeException(ioe);
        }
    }
    
    /**
     * Get the length of the Mime names within the property value
     * @param val The value of the property, which should contain a comma
     * separated list of Mime names, followed optionally by a space and the
     * high char value
     * @return
     */
    private static int lengthOfMimeNames(String val) {
        // look for the space preceding the optional high char
        int len = val.indexOf(' ');
        // If len is zero it means the optional part is not there, so
        // the value must be all Mime names, so set the length appropriately
        if (len < 0)  
            len = val.length();
        
        return len;
    }

    /**
     * Return true if the character is the high member of a surrogate pair.
     * <p>
     * This is not a public API.
     * @param ch the character to test
     * @xsl.usage internal
     */
    static boolean isHighUTF16Surrogate(char ch) {
        return ('\uD800' <= ch && ch <= '\uDBFF');
    }
    /**
     * Return true if the character is the low member of a surrogate pair.
     * <p>
     * This is not a public API.
     * @param ch the character to test
     * @xsl.usage internal
     */
    static boolean isLowUTF16Surrogate(char ch) {
        return ('\uDC00' <= ch && ch <= '\uDFFF');
    }
    /**
     * Return the unicode code point represented by the high/low surrogate pair.
     * <p>
     * This is not a public API.
     * @param highSurrogate the high char of the high/low pair
     * @param lowSurrogate the low char of the high/low pair
     * @xsl.usage internal
     */
    static int toCodePoint(char highSurrogate, char lowSurrogate) {
        int codePoint =
            ((highSurrogate - 0xd800) << 10)
                + (lowSurrogate - 0xdc00)
                + 0x10000;
        return codePoint;
    }
    /**
     * Return the unicode code point represented by the char.
     * A bit of a dummy method, since all it does is return the char,
     * but as an int value.
     * <p>
     * This is not a public API.
     * @param ch the char.
     * @xsl.usage internal
     */
    static int toCodePoint(char ch) {
        int codePoint = ch;
        return codePoint;
    }
    
    /**
     * Characters with values at or below the high code point are
     * in the encoding. Code point values above this one may or may
     * not be in the encoding, but lower ones certainly are.
     * <p>
     * This is for performance.
     *
     * @param encoding The encoding
     * @return The code point for which characters at or below this code point
     * are in the encoding. Characters with higher code point may or may not be
     * in the encoding. A value of zero is returned if the high code point is unknown.
     * <p>
     * This method is not a public API.
     * @xsl.usage internal
     */
    static public char getHighChar(String encoding)
    {
        final char highCodePoint;
        EncodingInfo ei;

        String normalizedEncoding = toUpperCaseFast(encoding);
        ei = (EncodingInfo) _encodingTableKeyJava.get(normalizedEncoding);
        if (ei == null)
            ei = (EncodingInfo) _encodingTableKeyMime.get(normalizedEncoding);
        if (ei != null)
            highCodePoint =  ei.getHighChar();
        else
            highCodePoint = 0;
        return highCodePoint;
    }

    private static final Hashtable _encodingTableKeyJava = new Hashtable();
    private static final Hashtable _encodingTableKeyMime = new Hashtable();
    private static final EncodingInfo[] _encodings = loadEncodingInfo();
}
