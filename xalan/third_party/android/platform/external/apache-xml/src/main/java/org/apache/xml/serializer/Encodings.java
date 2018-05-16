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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
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
            InputStream is; 
                
            SecuritySupport ss = SecuritySupport.getInstance();
            is = ss.getResourceAsStream(ObjectFactory.findClassLoader(),
                                            ENCODINGS_FILE);
            
            // j2objc: if resource wasn't found, load defaults from string.
            if (is == null) {
                is = new ByteArrayInputStream(
                    ENCODINGS_FILE_STR.getBytes(StandardCharsets.UTF_8));
            }

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
    
    // j2objc: the default resource values, for when resources weren't linked into app.
    // These strings are created by taking each .properties file and removing the
    // comments and whitespace.
    
    private static final String ENCODINGS_FILE_STR =
        "ASCII ASCII,US-ASCII 0x007F\n"
        + "Big5 BIG5,csBig5 0x007F\n"
        + "Big5_HKSCS BIG5-HKSCS 0x007F\n"
        + "Cp037 EBCDIC-CP-US,EBCDIC-CP-CA,EBCDIC-CP-WT,EBCDIC-CP-NL,IBM037 0x0019\n"
        + "Cp273 IBM273,csIBM273 0x0019\n"
        + "Cp274 csIBM274,EBCDIC-BE \n"
        + "Cp275 csIBM275,EBCDIC-BR \n"
        + "Cp277 EBCDIC-CP-DK,EBCDIC-CP-NO,IBM277,csIBM277 0x0019\n"
        + "Cp278 EBCDIC-CP-FI,EBCDIC-CP-SE,IBM278,csIBM278 0x0019\n"
        + "Cp280 EBCDIC-CP-IT,IBM280,csIBM280 0x0019\n"
        + "Cp281 EBCDIC-JP-E,csIBM281 \n"
        + "Cp284 EBCDIC-CP-ES,IBM284,csIBM284 0x0019\n"
        + "Cp285 EBCDIC-CP-GB,IBM284,csIBM285 0x0019\n"
        + "Cp290 EBCDIC-JP-kana,IBM290,csIBM290 0x0019\n"
        + "Cp297 EBCDIC-CP-FR,IBM297,csIBM297 0x0019\n"
        + "Cp420 EBCDIC-CP-AR1,IBM420,csIBM420 0x0019\n"
        + "Cp423 EBCDIC-CP-GR,IBM423,csIBM423 \n"
        + "Cp424 EBCDIC-CP-HE,IBM424,csIBM424 0x0019\n"
        + "Cp437 437,IBM437,csPC8CodePage437 0x007F\n"
        + "Cp500 EBCDIC-CP-CH,EBCDIC-CP-BE,IBM500,csIBM500 0x0019\n"
        + "Cp775 IBM775,csPC775Baltic 0x007F\n"
        + "Cp838 IBM-Thai,838,csIBMThai 0x0019\n"
        + "Cp850 850,csPC850Multilingual,IBM850 0x007F\n"
        + "Cp851 851,IBM851,csIBM851 \n"
        + "Cp852 IBM852,852,csPCp852 0x007F\n"
        + "Cp855 IBM855,855,csIBM855 0x007F\n"
        + "Cp857 IBM857,857,csIBM857 0x007F\n"
        + "Cp858 IBM00858 0x007F\n"
        + "Cp860 860,csIBM860,IBM860 0x007F\n"
        + "Cp861 IBM861,861,csIBM861,cp-is 0x007F\n"
        + "Cp862 IBM862,862,csPCi62LatinHebrew 0x007F\n"
        + "Cp863 IBM863,863,csIBM863 0x007F\n"
        + "Cp864 IBM864,864,csIBM864 0x007F\n"
        + "Cp865 IBM865,865,csIBM865 0x007F\n"
        + "Cp866 IBM866,866,csIBM866 0x007F\n"
        + "Cp868 IBM868,cp-ar,csIBM868 0x007F\n"
        + "Cp869 IBM869,869,cp-gr,csIBM869 0x007F\n"
        + "Cp870 EBCDIC-CP-ROECE,EBCDIC-CP-YU,IBM870,csIBM870 0x0019\n"
        + "Cp871 EBCDIC-CP-IS,IBM871,csIBM871 0x0019\n"
        + "Cp880 EBCDIC-Cyrillic,IBM880,csIBM880 \n"
        + "Cp891 IBM891,csIBM891 \n"
        + "Cp903 IBM903,csIBM903 \n"
        + "Cp904 IBM904,csIBM904 \n"
        + "Cp905 IBM905,csIBM905,EBCDIC-CP-TR \n"
        + "Cp918 EBCDIC-CP-AR2,IBM918,csIBM918 0x0019\n"
        + "Cp936 GBK,MS936,WINDOWS-936 \n"
        + "Cp1026 IBM1026,csIBM1026 0x0019\n"
        + "Cp1047 IBM1047,IBM-1047 0x0019\n"
        + "Cp1140 IBM01140 0x0019\n"
        + "Cp1141 IBM01141 0x0019\n"
        + "Cp1142 IBM01142 0x0019\n"
        + "Cp1143 IBM01143 0x0019\n"
        + "Cp1144 IBM01144 0x0019\n"
        + "Cp1145 IBM01145 0x0019\n"
        + "Cp1146 IBM01146 0x0019\n"
        + "Cp1147 IBM01147 0x0019\n"
        + "Cp1148 IBM01148 0x0019\n"
        + "Cp1149 IBM01149 0x0019\n"
        + "Cp1250 WINDOWS-1250 0x007F\n"
        + "Cp1251 WINDOWS-1251 0x007F\n"
        + "Cp1252 WINDOWS-1252 0x007F\n"
        + "Cp1253 WINDOWS-1253 0x007F\n"
        + "Cp1254 WINDOWS-1254 0x007F\n"
        + "Cp1255 WINDOWS-1255 0x007F\n"
        + "Cp1256 WINDOWS-1256 0x007F\n"
        + "Cp1257 WINDOWS-1257 0x007F\n"
        + "Cp1258 WINDOWS-1258 0x007F\n"
        + "EUC-CN EUC-CN 0x007F\n"
        + "EUC_CN EUC-CN 0x007F\n"
        + "EUC-JP EUC-JP 0x007F\n"
        + "EUC_JP EUC-JP 0x007F\n"
        + "EUC-KR EUC-KR 0x007F\n"
        + "EUC_KR EUC-KR 0x007F\n"
        + "EUC-TW EUC-TW 0x007F\n"
        + "EUC_TW EUC-TW,x-EUC-TW 0x007F\n"
        + "EUCJIS EUC-JP 0x007F\n"
        + "GB2312 GB2312 0x007F\n"
        + "ISO2022CN ISO-2022-CN \n"
        + "ISO2022JP ISO-2022-JP \n"
        + "ISO2022KR ISO-2022-KR 0x007F\n"
        + "ISO8859-1 ISO-8859-1 0x00FF\n"
        + "ISO8859_1 ISO-8859-1 0x00FF\n"
        + "8859-1 ISO-8859-1 0x00FF\n"
        + "8859_1 ISO-8859-1 0x00FF\n"
        + "ISO8859-2 ISO-8859-2 0x00A0\n"
        + "ISO8859_2 ISO-8859-2 0x00A0\n"
        + "8859-2 ISO-8859-2 0x00A0\n"
        + "8859_2 ISO-8859-2 0x00A0\n"
        + "ISO8859-3 ISO-8859-3 0x00A0\n"
        + "ISO8859_3 ISO-8859-3 0x00A0\n"
        + "8859-3 ISO-8859-3 0x00A0\n"
        + "8859_3 ISO-8859-3 0x00A0\n"
        + "ISO8859-4 ISO-8859-4 0x00A0\n"
        + "ISO8859_4 ISO-8859-4 0x00A0\n"
        + "8859-4 ISO-8859-4 0x00A0\n"
        + "8859_4 ISO-8859-4 0x00A0\n"
        + "ISO8859-5 ISO-8859-5 0x00A0\n"
        + "ISO8859_5 ISO-8859-5 0x00A0\n"
        + "8859-5 ISO-8859-5 0x00A0\n"
        + "8859_5 ISO-8859-5 0x00A0\n"
        + "ISO8859-6 ISO-8859-6 0x00A0\n"
        + "ISO8859_6 ISO-8859-6 0x00A0\n"
        + "8859-6 ISO-8859-6 0x00A0\n"
        + "8859_6 ISO-8859-6 0x00A0\n"
        + "ISO8859-7 ISO-8859-7 0x00A0\n"
        + "ISO8859_7 ISO-8859-7 0x00A0\n"
        + "8859-7 ISO-8859-7 0x00A0\n"
        + "8859_7 ISO-8859-7 0x00A0\n"
        + "ISO8859-8 ISO-8859-8 0x00A0\n"
        + "ISO8859_8 ISO-8859-8 0x00A0\n"
        + "8859-8 ISO-8859-8 0x00A0\n"
        + "8859_8 ISO-8859-8 0x00A0\n"
        + "ISO8859-9 ISO-8859-9 0x00CF\n"
        + "ISO8859_9 ISO-8859-9 0x00CF\n"
        + "8859-9 ISO-8859-9 0x00CF\n"
        + "8859_9 ISO-8859-9 0x00CF\n"
        + "ISO8859-10 ISO-8859-10 0x007E\n"
        + "ISO8859_10 ISO-8859-10 0x007E\n"
        + "ISO8859-11 ISO-8859-11 0x007E\n"
        + "ISO8859_11 ISO-8859-11 0x007E\n"
        + "ISO8859-12 ISO-8859-12 0x007F\n"
        + "ISO8859_12 ISO-8859-12 0x007F\n"
        + "ISO8859-13 ISO-8859-13 0x00A0\n"
        + "ISO8859_13 ISO-8859-13 0x00A0\n"
        + "ISO8859-14 ISO-8859-14 0x007E\n"
        + "ISO8859_14 ISO-8859-14 0x007E\n"
        + "ISO8859-15 ISO-8859-15 0x00A3\n"
        + "ISO8859_15 ISO-8859-15 0x00A3\n"
        + "JIS ISO-2022-JP 0x007F\n"
        + "KOI8_R KOI8-R 0x007F\n"
        + "KSC5601 EUC-KR 0x007F\n"
        + "KS_C_5601-1987 KS_C_5601-1987,iso-ir-149,KS_C_5601-1989,KSC_5601,csKSC56011987 0x007F\n"
        + "MacTEC MacRoman \n"
        + "MS932 windows-31j \n"
        + "SJIS SHIFT_JIS 0x007F\n"
        + "TIS620 TIS-620 \n"
        + "UTF8 UTF-8 0xFFFF\n"
        + "Unicode UNICODE,UTF-16 0xFFFF\n";

}
