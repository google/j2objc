/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/**
 *******************************************************************************
 * Copyright (C) 2005-2015, International Business Machines Corporation and
 * others. All Rights Reserved.
 *******************************************************************************
 */
package android.icu.dev.test.charsetdet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.icu.dev.test.TestFmwk;
import android.icu.dev.test.TestUtil;
import android.icu.dev.test.TestUtil.JavaVendor;
import android.icu.impl.Utility;
import android.icu.text.CharsetDetector;
import android.icu.text.CharsetMatch;


/**
 * @author andy
 */
public class TestCharsetDetector extends TestFmwk
{
    public TestCharsetDetector()
    {
    }

    private void CheckAssert(boolean exp) {
        if (exp == false) {
            String msg;
            try {
                throw new Exception();
            }
            catch (Exception e) {
                StackTraceElement failPoint = e.getStackTrace()[1];
                msg = "Test failure in file " + failPoint.getFileName() +
                             " at line " + failPoint.getLineNumber();
            }
            errln(msg);
        }
        
    }
    
    private String stringFromReader(Reader reader)
    {
        StringBuffer sb = new StringBuffer();
        char[] buffer   = new char[1024];
        int bytesRead   = 0;
        
        try {
            while ((bytesRead = reader.read(buffer, 0, 1024)) >= 0) {
                sb.append(buffer, 0, bytesRead);
            }
            
            return sb.toString();
        } catch (Exception e) {
            errln("stringFromReader() failed: " + e.toString());
            return null;
        }
    }
    
    @Test
    public void TestConstruction() {
        int i;
        CharsetDetector  det = new CharsetDetector();
        if(det==null){
            errln("Could not construct a charset detector");
        }
        String [] charsetNames = CharsetDetector.getAllDetectableCharsets();
        CheckAssert(charsetNames.length != 0);
        for (i=0; i<charsetNames.length; i++) {
            CheckAssert(charsetNames[i].equals("") == false); 
            // System.out.println("\"" + charsetNames[i] + "\"");
        }

        final String[] defDisabled = {
            "IBM420_rtl", "IBM420_ltr",
            "IBM424_rtl", "IBM424_ltr"
        };
        String[] activeCharsetNames = det.getDetectableCharsets();
        for (String cs : activeCharsetNames) {
            // the charset must be included in all list
            boolean found = false;
            for (String cs0 : charsetNames) {
                if (cs0.equals(cs)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                errln(cs + " is not included in the all charset list." );
            }

            // some charsets are disabled by default
            found = false;
            for (String cs1 : defDisabled) {
                if (cs1.equals(cs)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                errln(cs + " should not be included in the default charset list.");
            }
        }
    }

    @Test
    public void TestInputFilter() throws Exception
    {
        String s = "<a> <lot> <of> <English> <inside> <the> <markup> Un tr\u00E8s petit peu de Fran\u00E7ais. <to> <confuse> <the> <detector>";
        byte[] bytes = s.getBytes("ISO-8859-1");
        CharsetDetector det = new CharsetDetector();
        CharsetMatch m;
        
        det.enableInputFilter(true);
        if (!det.inputFilterEnabled()){
            errln("input filter should be enabled");
        }
        
        det.setText(bytes);
        m = det.detect();
        
        if (! m.getLanguage().equals("fr")) {
            errln("input filter did not strip markup!");
        }
        
        det.enableInputFilter(false);
        det.setText(bytes);
        m = det.detect();
        
        if (! m.getLanguage().equals("en")) {
            errln("unfiltered input did not detect as English!");
        }
    }
    
    @Test
    public void TestUTF8() throws Exception {
        
        String  s = "This is a string with some non-ascii characters that will " +
                    "be converted to UTF-8, then shoved through the detection process.  " +
                    "\u0391\u0392\u0393\u0394\u0395" +
                    "Sure would be nice if our source could contain Unicode directly!";
        byte [] bytes = s.getBytes("UTF-8");
        CharsetDetector det = new CharsetDetector();
        String retrievedS;
        Reader reader;
        
        retrievedS = det.getString(bytes, "UTF-8");
        CheckAssert(s.equals(retrievedS));
        
        reader = det.getReader(new ByteArrayInputStream(bytes), "UTF-8");
        try {
            CheckAssert(s.equals(stringFromReader(reader)));
        } finally {
            reader.close();
        }
        det.setDeclaredEncoding("UTF-8"); // Jitterbug 4451, for coverage
    }
    
    @Test
    public void TestUTF16() throws Exception
    {
        String source = 
                "u0623\u0648\u0631\u0648\u0628\u0627, \u0628\u0631\u0645\u062c\u064a\u0627\u062a " +
                "\u0627\u0644\u062d\u0627\u0633\u0648\u0628 \u002b\u0020\u0627\u0646\u062a\u0631\u0646\u064a\u062a";
        
        byte[] beBytes = source.getBytes("UnicodeBig");
        byte[] leBytes = source.getBytes("UnicodeLittle");
        CharsetDetector det = new CharsetDetector();
        CharsetMatch m;
        
        det.setText(beBytes);
        m = det.detect();
        
        if (! m.getName().equals("UTF-16BE")) {
            errln("Encoding detection failure: expected UTF-16BE, got " + m.getName());
        }
        
        det.setText(leBytes);
        m = det.detect();
        
        if (! m.getName().equals("UTF-16LE")) {
            errln("Encoding detection failure: expected UTF-16LE, got " + m.getName());
        }

        // Jitterbug 4451, for coverage
        int confidence = m.getConfidence(); 
        if(confidence != 100){
            errln("Did not get the expected confidence level " + confidence);
        }
    }
    
    @Test
    public void TestC1Bytes() throws Exception
    {
        String sISO =
            "This is a small sample of some English text. Just enough to be sure that it detects correctly.";
        
        String sWindows =
            "This is another small sample of some English text. Just enough to be sure that it detects correctly. It also includes some \u201CC1\u201D bytes.";

        byte[] bISO     = sISO.getBytes("ISO-8859-1");
        byte[] bWindows = sWindows.getBytes("windows-1252");
        
        CharsetDetector det = new CharsetDetector();
        CharsetMatch m;
        
        det.setText(bWindows);
        m = det.detect();
        
        if (!m.getName().equals("windows-1252")) {
            errln("Text with C1 bytes not correctly detected as windows-1252.");
            return;
        }
        
        det.setText(bISO);
        m = det.detect();
        
        if (!m.getName().equals("ISO-8859-1")) {
            errln("Text without C1 bytes not correctly detected as ISO-8859-1.");
        }
    }
    
    @Test
    public void TestShortInput() {
        // Test that detection with very short byte strings does not crash and burn.
        // The shortest input that should produce positive detection result is two bytes, 
        //   a UTF-16 BOM.
        // TODO:  Detector confidence levels needs to be refined for very short input.
        //        Too high now, for some charsets that happen to be compatible with a few bytes of input.
        byte [][]  shortBytes = new byte [][] 
            {
                {},
                {(byte)0x0a},
                {(byte)'A', (byte)'B'},
                {(byte)'A', (byte)'B', (byte)'C'},
                {(byte)'A', (byte)'B', (byte)'C', (byte)'D'}
            };
        
        CharsetDetector det = new CharsetDetector();
        CharsetMatch m;
        for (int i=0; i<shortBytes.length; i++) {
            det.setText(shortBytes[i]);
            m = det.detect();
            logln("i=" + i + " -> " + m.getName());
        }
    }
    
    @Test
    public void TestBufferOverflow()
    {
        byte testStrings[][] = {
            {(byte) 0x80, (byte) 0x20, (byte) 0x54, (byte) 0x68, (byte) 0x69, (byte) 0x73, (byte) 0x20, (byte) 0x69, (byte) 0x73, (byte) 0x20, (byte) 0x45, (byte) 0x6E, (byte) 0x67, (byte) 0x6C, (byte) 0x69, (byte) 0x73, (byte) 0x68, (byte) 0x20, (byte) 0x1b}, /* A partial ISO-2022 shift state at the end */
            {(byte) 0x80, (byte) 0x20, (byte) 0x54, (byte) 0x68, (byte) 0x69, (byte) 0x73, (byte) 0x20, (byte) 0x69, (byte) 0x73, (byte) 0x20, (byte) 0x45, (byte) 0x6E, (byte) 0x67, (byte) 0x6C, (byte) 0x69, (byte) 0x73, (byte) 0x68, (byte) 0x20, (byte) 0x1b, (byte) 0x24}, /* A partial ISO-2022 shift state at the end */
            {(byte) 0x80, (byte) 0x20, (byte) 0x54, (byte) 0x68, (byte) 0x69, (byte) 0x73, (byte) 0x20, (byte) 0x69, (byte) 0x73, (byte) 0x20, (byte) 0x45, (byte) 0x6E, (byte) 0x67, (byte) 0x6C, (byte) 0x69, (byte) 0x73, (byte) 0x68, (byte) 0x20, (byte) 0x1b, (byte) 0x24, (byte) 0x28}, /* A partial ISO-2022 shift state at the end */
            {(byte) 0x80, (byte) 0x20, (byte) 0x54, (byte) 0x68, (byte) 0x69, (byte) 0x73, (byte) 0x20, (byte) 0x69, (byte) 0x73, (byte) 0x20, (byte) 0x45, (byte) 0x6E, (byte) 0x67, (byte) 0x6C, (byte) 0x69, (byte) 0x73, (byte) 0x68, (byte) 0x20, (byte) 0x1b, (byte) 0x24, (byte) 0x28, (byte) 0x44}, /* A complete ISO-2022 shift state at the end with a bad one at the start */
            {(byte) 0x1b, (byte) 0x24, (byte) 0x28, (byte) 0x44}, /* A complete ISO-2022 shift state at the end */
            {(byte) 0xa1}, /* Could be a single byte shift-jis at the end */
            {(byte) 0x74, (byte) 0x68, (byte) 0xa1}, /* Could be a single byte shift-jis at the end */
            {(byte) 0x74, (byte) 0x68, (byte) 0x65, (byte) 0xa1} /* Could be a single byte shift-jis at the end, but now we have English creeping in. */
        };
        
        String testResults[] = {
            "windows-1252",
            "windows-1252",
            "windows-1252",
            "windows-1252",
            "ISO-2022-JP",
            null,
            null,
            "ISO-8859-1"
        };
        
        CharsetDetector det = new CharsetDetector();
        CharsetMatch match;

        det.setDeclaredEncoding("ISO-2022-JP");

        for (int idx = 0; idx < testStrings.length; idx += 1) {
            det.setText(testStrings[idx]);
            match = det.detect();

            if (match == null) {
                if (testResults[idx] != null) {
                    errln("Unexpectedly got no results at index " + idx);
                }
                else {
                    logln("Got no result as expected at index " + idx);
                }
                continue;
            }

            if (testResults[idx] == null || ! testResults[idx].equals(match.getName())) {
                errln("Unexpectedly got " + match.getName() + " instead of " + testResults[idx] +
                      " at index " + idx + " with confidence " + match.getConfidence());
                return;
            }
        }
    }

    @Test
    public void TestDetection()
    {
        //
        //  Open and read the test data file.
        //
        //InputStreamReader isr = null;
        
        try {
            InputStream is = TestCharsetDetector.class.getResourceAsStream("CharsetDetectionTests.xml");
            if (is == null) {
                errln("Could not open test data file CharsetDetectionTests.xml");
                return;
            }

            // Set up an xml parser.
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            
            factory.setIgnoringComments(true);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            // Parse the xml content from the test case file.
            Document doc;
            try {
                doc = builder.parse(is, null);
            } finally {
                is.close();
            }
            Element root = doc.getDocumentElement();
            
            NodeList testCases = root.getElementsByTagName("test-case");
            
            // Process each test case
            Map<String, byte[]> encToBytes = new TreeMap<String, byte[]>();
            for (int n = 0; n < testCases.getLength(); n += 1) {
                Node testCase = testCases.item(n);
                NamedNodeMap attrs = testCase.getAttributes();
                NodeList testData  = testCase.getChildNodes();
                StringBuffer testText = new StringBuffer();
                String id = attrs.getNamedItem("id").getNodeValue();
                String encodings = attrs.getNamedItem("encodings").getNodeValue();

                // Collect the test case text and optional bytes.
                // A <bytes encoding="name">ASCII with \xhh</bytes> element
                // specifies the byte sequence to be tested.
                // This is useful when not all platforms encode the test text the same way
                // (or do not support encoding for that charset).
                for (int t = 0; t < testData.getLength(); t += 1) {
                    Node node = testData.item(t);
                    if (node.getNodeType() == Node.TEXT_NODE) {
                        testText.append(node.getNodeValue());
                    } else if (node.getNodeType() == Node.ELEMENT_NODE &&
                            node.getNodeName().equals("bytes")) {
                        String name = node.getAttributes().getNamedItem("encoding").getNodeValue();
                        Node valueNode = node.getFirstChild();
                        if (valueNode.getNodeType() != Node.TEXT_NODE) {
                            throw new IllegalArgumentException("<bytes> node does not contain text");
                        }
                        // The bytes are stored as ASCII characters and \xhh escaped bytes.
                        // We unescape the string to turn the \xhh into chars U+0000..U+00ff,
                        // then use the deprecated String.getBytes() to turn those into bytes
                        // by essentially casting each char to a byte.
                        String bytesString = Utility.unescape(valueNode.getNodeValue());
                        byte[] bytes = new byte[bytesString.length()];
                        bytesString.getBytes(0, bytesString.length(), bytes, 0);
                        encToBytes.put(name, bytes);
                    } else {
                        throw new IllegalArgumentException("unknown <test-case> child node: " + node);
                    }
                }

                // Process test text with each encoding / language pair.
                String testString = testText.toString();
                String[] encodingList = encodings.split(" ");
                for (int e = 0; e < encodingList.length; e += 1) {
                    String[] params = encodingList[e].split("/");
                    String encoding = params[0];
                    String language = params.length == 1 || params[1].length() == 0 ? null : params[1];

                    // With a few charsets, the conversion back to Unicode
                    // may depend on the implementation.
                    boolean checkRoundtrip =
                            !encoding.startsWith("UTF-32") &&
                            !(params.length >= 3 && params[2].equals("noroundtrip"));
                    checkEncoding(testString, encoding, language, checkRoundtrip,
                            encToBytes.get(encoding), id);
                }
                encToBytes.clear();
            }
            
        } catch (Exception e) {
            errln("exception while processing test cases: " + e.toString());
        }
    }

    private void checkMatch(CharsetDetector det, String testString,
            String encoding, String language, boolean checkRoundtrip, String id) throws Exception {
        CharsetMatch m = det.detect();
        if (! m.getName().equals(encoding)) {
            errln(id + ": encoding detection failure - expected " + encoding + ", got " + m.getName());
            return;
        }
        
        String charsetMatchLanguage = m.getLanguage();
        if ((language != null && !charsetMatchLanguage.equals(language))
            || (language == null && charsetMatchLanguage != null)
            || (language != null && charsetMatchLanguage == null))
        {
            errln(id + ", " + encoding + ": language detection failure - expected " + language + ", got " + m.getLanguage());
        }

        if (!checkRoundtrip) {
            return;
        }

        // TODO temporary workaround for IBM Java 8 ISO-2022-KR problem
        if (encoding.equals("ISO-2022-KR") && TestUtil.getJavaVendor() == JavaVendor.IBM && TestUtil.getJavaVersion() == 8) {
            logln("Skipping roundtrip check on IBM Java 8: " + id + ", " + encoding);
            return;
        }
        
        String decoded = m.getString();
        
        if (! testString.equals(decoded)) {
            errln(id + ", " + encoding + ": getString() didn't return the original string!");
        }
        
        decoded = stringFromReader(m.getReader());
        
        if (! testString.equals(decoded)) {
            errln(id + ", " + encoding + ": getReader() didn't yield the original string!");
        }
    }
    
    private void checkEncoding(String testString,
            String encoding, String language, boolean checkRoundtrip,
            byte[] bytes, String id) {
        if (bytes == null) {
            try {
                bytes = testString.getBytes(encoding);
            } catch (UnsupportedOperationException uoe) {
                // Ignore any converters that can't
                // convert from Unicode.
                logln("Unsupported encoding for conversion from Unicode: " + encoding);
                return;
            } catch (UnsupportedEncodingException uee) {
                // Ignore any encodings that this runtime
                // doesn't support.
                logln("Unsupported encoding: " + encoding);
                return;
            }
        }

        try {
            CharsetDetector det = new CharsetDetector();

            det.setText(bytes);
            checkMatch(det, testString, encoding, language, checkRoundtrip, id);

            det.setText(new ByteArrayInputStream(bytes));
            checkMatch(det, testString, encoding, language, checkRoundtrip, id);
         } catch (Exception e) {
            errln(id + ": " + e.toString() + "enc=" + encoding);
            e.printStackTrace();
        }
    }
    
    @Test
    public void TestJapanese() throws Exception {
        String s = "\u3000\u3001\u3002\u3003\u3005\u3006\u3007\u3008\u3009\u300A\u300B\u300C\u300D\u300E\u300F\u3010\u3011\u3012\u3013\u3014" + 
        "\u3015\u301C\u3041\u3042\u3043\u3044\u3045\u3046\u3047\u3048\u3049\u304A\u304B\u304C\u304D\u304E\u304F\u3050\u3051\u3052" + 
        "\u3053\u3054\u3055\u3056\u3057\u3058\u3059\u305A\u305B\u305C\u305D\u305E\u305F\u3060\u3061\u3062\u3063\u3064\u3065\u3066" + 
        "\u3067\u3068\u3069\u306A\u306B\u306C\u306D\u306E\u306F\u3070\u3071\u3072\u3073\u3074\u3075\u3076\u3077\u3078\u3079\u307A" + 
        "\u307B\u307C\u307D\u307E\u307F\u3080\u3081\u3082\u3083\u3084\u3085\u3086\u3087\u3088\u3089\u308A\u308B\u308C\u308D\u308E" + 
        "\u308F\u3090\u3091\u3092\u3093\u309B\u309C\u309D\u309E\u30A1\u30A2\u30A3\u30A4\u30A5\u30A6\u30A7\u30A8\u30A9\u30AA\u30AB" + 
        "\u30AC\u30AD\u30AE\u30AF\u30B0\u30B1\u30B2\u30B3\u30B4\u30B5\u30B6\u30B7\u30B8\u30B9\u30BA\u30BB\u30BC\u30BD\u30BE\u30BF" + 
        "\u30C0\u30C1\u30C2\u30C3\u30C4\u30C5\u30C6\u30C7\u30C8\u30C9\u30CA\u30CB\u30CC\u30CD\u30CE\u30CF\u30D0\u30D1\u30D2\u30D3" + 
        "\u30D4\u30D5\u30D6\u30D7\u30D8\u30D9\u30DA\u30DB\u30DC\u30DD\u30DE\u30DF\u30E0\u30E1\u30E2\u30E3\u30E4\u30E5\u30E6\u30E7" + 
        "\u30E8\u30E9\u30EA\u30EB\u30EC\u30ED\u30EE\u30EF\u30F0\u30F1\u30F2\u30F3\u30F4\u30F5\u30F6\u30FB\u30FC\u30FD\u30FE\u4E00" + 
        "\u4E01\u4E02\u4E03\u4E04\u4E05\u4E07\u4E08\u4E09\u4E0A\u4E0B\u4E0C\u4E0D\u4E0E\u4E10\u4E11\u4E12\u4E14\u4E15\u4E16\u4E17" + 
        "\u4E18\u4E19\u4E1E\u4E1F\u4E21\u4E23\u4E24\u4E26\u4E28\u4E2A\u4E2B\u4E2D\u4E2E\u4E2F\u4E30\u4E31\u4E32\u4E35\u4E36\u4E38" + 
        "\u4E39\u4E3B\u4E3C\u4E3F\u4E40\u4E41\u4E42\u4E43\u4E44\u4E45\u4E47\u4E4B\u4E4D\u4E4E\u4E4F\u4E51\u4E55\u4E56\u4E57\u4E58" + 
        "\u4E59\u4E5A\u4E5C\u4E5D\u4E5E\u4E5F\u4E62\u4E63\u4E68\u4E69\u4E71\u4E73\u4E74\u4E75\u4E79\u4E7E\u4E7F\u4E80\u4E82\u4E85" + 
        "\u4E86\u4E88\u4E89\u4E8A\u4E8B\u4E8C";
        
        CharsetDetector det = new CharsetDetector();
        CharsetMatch m;
        String charsetMatch;
        byte[] bytes;
        {
            bytes = s.getBytes("EUC-JP");
            det.setText(bytes);
            m = det.detect();
            charsetMatch = m.getName();
            CheckAssert(charsetMatch.equals("EUC-JP"));
            
            // Tests "public String getLanguage()"
            CheckAssert(m.getLanguage().equals("ja"));
        }
    }

    @Test
    public void TestArabic() throws Exception {
        String  s = "\u0648\u0636\u0639\u062A \u0648\u0646\u0641\u0630\u062A \u0628\u0631\u0627" +
        "\u0645\u062C \u062A\u0623\u0645\u064A\u0646 \u0639\u062F\u064A\u062F\u0629 \u0641\u064A " + 
        "\u0645\u0624\u0633\u0633\u0629 \u0627\u0644\u062A\u0623\u0645\u064A\u0646 \u0627\u0644"  + 
        "\u0648\u0637\u0646\u064A, \u0645\u0639 \u0645\u0644\u0627\u0626\u0645\u062A\u0647\u0627 " + 
        "\u062F\u0627\u0626\u0645\u0627 \u0644\u0644\u0627\u062D\u062A\u064A\u0627\u062C" + 
        "\u0627\u062A \u0627\u0644\u0645\u062A\u063A\u064A\u0631\u0629 \u0644\u0644\u0645\u062C" + 
        "\u062A\u0645\u0639 \u0648\u0644\u0644\u062F\u0648\u0644\u0629. \u062A\u0648\u0633\u0639" + 
        "\u062A \u0648\u062A\u0637\u0648\u0631\u062A \u0627\u0644\u0645\u0624\u0633\u0633\u0629 " + 
        "\u0628\u0647\u062F\u0641 \u0636\u0645\u0627\u0646 \u0634\u0628\u0643\u0629 \u0623\u0645" + 
        "\u0627\u0646 \u0644\u0633\u0643\u0627\u0646 \u062F\u0648\u0644\u0629 \u0627\u0633\u0631" + 
        "\u0627\u0626\u064A\u0644 \u0628\u0648\u062C\u0647 \u0627\u0644\u0645\u062E\u0627\u0637" + 
        "\u0631 \u0627\u0644\u0627\u0642\u062A\u0635\u0627\u062F\u064A\u0629 \u0648\u0627\u0644" + 
        "\u0627\u062C\u062A\u0645\u0627\u0639\u064A\u0629.";

        CharsetDetector det = new CharsetDetector();
        det.setDetectableCharset("IBM424_rtl", true);
        det.setDetectableCharset("IBM424_ltr", true);
        det.setDetectableCharset("IBM420_rtl", true);
        det.setDetectableCharset("IBM420_ltr", true);
        CharsetMatch m;
        String charsetMatch;
        byte[] bytes;
        {
            bytes = s.getBytes("windows-1256");
            det.setText(bytes);
            m = det.detect();
            charsetMatch = m.getName();
            CheckAssert(charsetMatch.equals("windows-1256"));
            
            // Tests "public String getLanguage()"
            CheckAssert(m.getLanguage().endsWith("ar"));
        }

        {
            // We cannot rely on IBM420 converter in Sun Java
            /*
            bytes = s.getBytes("IBM420");
            */
            bytes = new byte[] {
                (byte)0xCF, (byte)0x8D, (byte)0x9A, (byte)0x63, (byte)0x40, (byte)0xCF, (byte)0xBD, (byte)0xAB,
                (byte)0x74, (byte)0x63, (byte)0x40, (byte)0x58, (byte)0x75, (byte)0x56, (byte)0xBB, (byte)0x67,
                (byte)0x40, (byte)0x63, (byte)0x49, (byte)0xBB, (byte)0xDC, (byte)0xBD, (byte)0x40, (byte)0x9A,
                (byte)0x73, (byte)0xDC, (byte)0x73, (byte)0x62, (byte)0x40, (byte)0xAB, (byte)0xDC, (byte)0x40,
                (byte)0xBB, (byte)0x52, (byte)0x77, (byte)0x77, (byte)0x62, (byte)0x40, (byte)0x56, (byte)0xB1,
                (byte)0x63, (byte)0x49, (byte)0xBB, (byte)0xDC, (byte)0xBD, (byte)0x40, (byte)0x56, (byte)0xB1,
                (byte)0xCF, (byte)0x8F, (byte)0xBD, (byte)0xDC, (byte)0x6B, (byte)0x40, (byte)0xBB, (byte)0x9A,
                (byte)0x40, (byte)0xBB, (byte)0xB1, (byte)0x56, (byte)0x55, (byte)0xBB, (byte)0x63, (byte)0xBF,
                (byte)0x56, (byte)0x40, (byte)0x73, (byte)0x56, (byte)0x55, (byte)0xBB, (byte)0x56, (byte)0x40,
                (byte)0xB1, (byte)0xB1, (byte)0x56, (byte)0x69, (byte)0x63, (byte)0xDC, (byte)0x56, (byte)0x67,
                (byte)0x56, (byte)0x63, (byte)0x40, (byte)0x56, (byte)0xB1, (byte)0xBB, (byte)0x63, (byte)0x9E,
                (byte)0xDC, (byte)0x75, (byte)0x62, (byte)0x40, (byte)0xB1, (byte)0xB1, (byte)0xBB, (byte)0x67,
                (byte)0x63, (byte)0xBB, (byte)0x9A, (byte)0x40, (byte)0xCF, (byte)0xB1, (byte)0xB1, (byte)0x73,
                (byte)0xCF, (byte)0xB1, (byte)0x62, (byte)0x4B, (byte)0x40, (byte)0x63, (byte)0xCF, (byte)0x77,
                (byte)0x9A, (byte)0x63, (byte)0x40, (byte)0xCF, (byte)0x63, (byte)0x8F, (byte)0xCF, (byte)0x75,
                (byte)0x63, (byte)0x40, (byte)0x56, (byte)0xB1, (byte)0xBB, (byte)0x52, (byte)0x77, (byte)0x77,
                (byte)0x62, (byte)0x40, (byte)0x58, (byte)0xBF, (byte)0x73, (byte)0xAB, (byte)0x40, (byte)0x8D,
                (byte)0xBB, (byte)0x56, (byte)0xBD, (byte)0x40, (byte)0x80, (byte)0x58, (byte)0xAF, (byte)0x62,
                (byte)0x40, (byte)0x49, (byte)0xBB, (byte)0x56, (byte)0xBD, (byte)0x40, (byte)0xB1, (byte)0x77,
                (byte)0xAF, (byte)0x56, (byte)0xBD, (byte)0x40, (byte)0x73, (byte)0xCF, (byte)0xB1, (byte)0x62,
                (byte)0x40, (byte)0x56, (byte)0x77, (byte)0x75, (byte)0x56, (byte)0x55, (byte)0xDC, (byte)0xB1,
                (byte)0x40, (byte)0x58, (byte)0xCF, (byte)0x67, (byte)0xBF, (byte)0x40, (byte)0x56, (byte)0xB1,
                (byte)0xBB, (byte)0x71, (byte)0x56, (byte)0x8F, (byte)0x75, (byte)0x40, (byte)0x56, (byte)0xB1,
                (byte)0x56, (byte)0xAD, (byte)0x63, (byte)0x8B, (byte)0x56, (byte)0x73, (byte)0xDC, (byte)0x62,
                (byte)0x40, (byte)0xCF, (byte)0x56, (byte)0xB1, (byte)0x56, (byte)0x67, (byte)0x63, (byte)0xBB,
                (byte)0x56, (byte)0x9A, (byte)0xDC, (byte)0x62, (byte)0x4B,
            };
            det.setText(bytes);
            m = det.detect();
            charsetMatch = m.getName();
            CheckAssert(charsetMatch.equals("IBM420_rtl"));
            
         // Tests "public String getLanguage()"
            CheckAssert(m.getLanguage().endsWith("ar"));
        }

        {
            // We cannot rely on IBM420 converter in Sun Java
            /*
            StringBuffer ltrStrBuf = new StringBuffer(s);
            ltrStrBuf = ltrStrBuf.reverse();
            bytes = ltrStrBuf.toString().getBytes("IBM420");
            */
            bytes = new byte[] {
                (byte)0x4B, (byte)0x62, (byte)0xDC, (byte)0x9A, (byte)0x56, (byte)0xBB, (byte)0x63, (byte)0x67,
                (byte)0x56, (byte)0xB1, (byte)0x56, (byte)0xCF, (byte)0x40, (byte)0x62, (byte)0xDC, (byte)0x73,
                (byte)0x56, (byte)0x8B, (byte)0x63, (byte)0xAD, (byte)0x56, (byte)0xB1, (byte)0x56, (byte)0x40,
                (byte)0x75, (byte)0x8F, (byte)0x56, (byte)0x71, (byte)0xBB, (byte)0xB1, (byte)0x56, (byte)0x40,
                (byte)0xBF, (byte)0x67, (byte)0xCF, (byte)0x58, (byte)0x40, (byte)0xB1, (byte)0xDC, (byte)0x55,
                (byte)0x56, (byte)0x75, (byte)0x77, (byte)0x56, (byte)0x40, (byte)0x62, (byte)0xB1, (byte)0xCF,
                (byte)0x73, (byte)0x40, (byte)0xBD, (byte)0x56, (byte)0xAF, (byte)0x77, (byte)0xB1, (byte)0x40,
                (byte)0xBD, (byte)0x56, (byte)0xBB, (byte)0x49, (byte)0x40, (byte)0x62, (byte)0xAF, (byte)0x58,
                (byte)0x80, (byte)0x40, (byte)0xBD, (byte)0x56, (byte)0xBB, (byte)0x8D, (byte)0x40, (byte)0xAB,
                (byte)0x73, (byte)0xBF, (byte)0x58, (byte)0x40, (byte)0x62, (byte)0x77, (byte)0x77, (byte)0x52,
                (byte)0xBB, (byte)0xB1, (byte)0x56, (byte)0x40, (byte)0x63, (byte)0x75, (byte)0xCF, (byte)0x8F,
                (byte)0x63, (byte)0xCF, (byte)0x40, (byte)0x63, (byte)0x9A, (byte)0x77, (byte)0xCF, (byte)0x63,
                (byte)0x40, (byte)0x4B, (byte)0x62, (byte)0xB1, (byte)0xCF, (byte)0x73, (byte)0xB1, (byte)0xB1,
                (byte)0xCF, (byte)0x40, (byte)0x9A, (byte)0xBB, (byte)0x63, (byte)0x67, (byte)0xBB, (byte)0xB1,
                (byte)0xB1, (byte)0x40, (byte)0x62, (byte)0x75, (byte)0xDC, (byte)0x9E, (byte)0x63, (byte)0xBB,
                (byte)0xB1, (byte)0x56, (byte)0x40, (byte)0x63, (byte)0x56, (byte)0x67, (byte)0x56, (byte)0xDC,
                (byte)0x63, (byte)0x69, (byte)0x56, (byte)0xB1, (byte)0xB1, (byte)0x40, (byte)0x56, (byte)0xBB,
                (byte)0x55, (byte)0x56, (byte)0x73, (byte)0x40, (byte)0x56, (byte)0xBF, (byte)0x63, (byte)0xBB,
                (byte)0x55, (byte)0x56, (byte)0xB1, (byte)0xBB, (byte)0x40, (byte)0x9A, (byte)0xBB, (byte)0x40,
                (byte)0x6B, (byte)0xDC, (byte)0xBD, (byte)0x8F, (byte)0xCF, (byte)0xB1, (byte)0x56, (byte)0x40,
                (byte)0xBD, (byte)0xDC, (byte)0xBB, (byte)0x49, (byte)0x63, (byte)0xB1, (byte)0x56, (byte)0x40,
                (byte)0x62, (byte)0x77, (byte)0x77, (byte)0x52, (byte)0xBB, (byte)0x40, (byte)0xDC, (byte)0xAB,
                (byte)0x40, (byte)0x62, (byte)0x73, (byte)0xDC, (byte)0x73, (byte)0x9A, (byte)0x40, (byte)0xBD,
                (byte)0xDC, (byte)0xBB, (byte)0x49, (byte)0x63, (byte)0x40, (byte)0x67, (byte)0xBB, (byte)0x56,
                (byte)0x75, (byte)0x58, (byte)0x40, (byte)0x63, (byte)0x74, (byte)0xAB, (byte)0xBD, (byte)0xCF,
                (byte)0x40, (byte)0x63, (byte)0x9A, (byte)0x8D, (byte)0xCF,
            };

            det.setText(bytes);
            m = det.detect();
            charsetMatch = m.getName();
            CheckAssert(charsetMatch.equals("IBM420_ltr"));
        }
    }

    @Test
    public void TestHebrew() throws Exception {
        String  s =  "\u05D4\u05E4\u05E8\u05E7\u05DC\u05D9\u05D8 \u05D4\u05E6\u05D1\u05D0\u05D9 \u05D4" +
            "\u05E8\u05D0\u05E9\u05D9, \u05EA\u05EA \u05D0\u05DC\u05D5\u05E3 \u05D0\u05D1\u05D9" + 
            "\u05D7\u05D9 \u05DE\u05E0\u05D3\u05DC\u05D1\u05DC\u05D9\u05D8, \u05D4\u05D5\u05E8" + 
            "\u05D4 \u05E2\u05DC \u05E4\u05EA\u05D9\u05D7\u05EA \u05D7\u05E7\u05D9\u05E8\u05EA " + 
            "\u05DE\u05E6\"\u05D7 \u05D1\u05E2\u05E7\u05D1\u05D5\u05EA \u05E2\u05D3\u05D5\u05D9" + 
            "\u05D5\u05EA \u05D7\u05D9\u05D9\u05DC\u05D9 \u05E6\u05D4\"\u05DC \u05DE\u05DE\u05D1" + 
            "\u05E6\u05E2 \u05E2\u05D5\u05E4\u05E8\u05EA \u05D9\u05E6\u05D5\u05E7\u05D4 \u05D1+ " +
            "\u05E8\u05E6\u05D5\u05E2\u05EA \u05E2\u05D6\u05D4. \u05DC\u05D3\u05D1\u05E8\u05D9 " + 
            "\u05D4\u05E4\u05E6\"\u05E8, \u05DE\u05D4\u05E2\u05D3\u05D5\u05D9\u05D5\u05EA \u05E2" +
            "\u05D5\u05DC\u05D4 \u05EA\u05DE\u05D5\u05E0\u05D4 \u05E9\u05DC \"\u05D4\u05EA\u05E0" + 
            "\u05D4\u05D2\u05D5\u05EA \u05E4\u05E1\u05D5\u05DC\u05D4 \u05DC\u05DB\u05D0\u05D5\u05E8" + 
            "\u05D4 \u05E9\u05DC \u05D7\u05D9\u05D9\u05DC\u05D9\u05DD \u05D1\u05DE\u05D4\u05DC\u05DA" + 
            " \u05DE\u05D1\u05E6\u05E2 \u05E2\u05D5\u05E4\u05E8\u05EA \u05D9\u05E6\u05D5\u05E7\u05D4\"." + 
            " \u05DE\u05E0\u05D3\u05DC\u05D1\u05DC\u05D9\u05D8 \u05E7\u05D9\u05D1\u05DC \u05D0\u05EA" +
            " \u05D4\u05D7\u05DC\u05D8\u05EA\u05D5 \u05DC\u05D0\u05D7\u05E8 \u05E9\u05E2\u05D9\u05D9" +
            "\u05DF \u05D1\u05EA\u05DE\u05DC\u05D9\u05DC \u05D4\u05E2\u05D3\u05D5\u05D9\u05D5\u05EA";
        
        CharsetMatch m = _test1255(s);
        String charsetMatch = m.getName();
        CheckAssert(charsetMatch.equals("ISO-8859-8-I"));
        CheckAssert(m.getLanguage().equals("he"));
        
        m = _test1255_reverse(s);
        charsetMatch = m.getName();
        CheckAssert(charsetMatch.equals("ISO-8859-8"));
        CheckAssert(m.getLanguage().equals("he"));
        
        m = _testIBM424_he_rtl(s);
        charsetMatch = m.getName();
        CheckAssert(charsetMatch.equals("IBM424_rtl"));
        CheckAssert(m.getLanguage().equals("he"));
        try {
            m.getString();
        } catch (Exception ex) {
            errln("Error getting string for charsetMatch: " + charsetMatch);
        }
        
        m = _testIBM424_he_ltr(s);
        charsetMatch = m.getName();
        CheckAssert(charsetMatch.equals("IBM424_ltr"));
        CheckAssert(m.getLanguage().equals("he"));
        try {
            m.getString();
        } catch (Exception ex) {
            errln("Error getting string for charsetMatch: " + charsetMatch);
        }
    }
    
    private CharsetMatch _test1255(String s) throws Exception {
        byte [] bytes = s.getBytes("ISO-8859-8");
        CharsetDetector det = new CharsetDetector();
        det.setText(bytes);
        CharsetMatch m = det.detect();
        return m;
    }
    
    private CharsetMatch _test1255_reverse(String s) throws Exception {
        StringBuffer reverseStrBuf = new StringBuffer(s);
        reverseStrBuf = reverseStrBuf.reverse();
        byte [] bytes = reverseStrBuf.toString().getBytes("ISO-8859-8");
        
        CharsetDetector det = new CharsetDetector();
        det.setText(bytes);
        CharsetMatch m = det.detect();
        return m;
    }
    
    private CharsetMatch _testIBM424_he_rtl(String s) throws Exception {
        byte [] bytes = s.getBytes("IBM424");
        CharsetDetector det = new CharsetDetector();
        det.setDetectableCharset("IBM424_rtl", true);
        det.setDetectableCharset("IBM424_ltr", true);
        det.setDetectableCharset("IBM420_rtl", true);
        det.setDetectableCharset("IBM420_ltr", true);
        det.setText(bytes);
        CharsetMatch m = det.detect();
        return m;
    }
    
    private CharsetMatch _testIBM424_he_ltr(String s) throws Exception {
        /**
         * transformation of input string to CP420 left to right requires reversing the string
         */    
        
        StringBuffer ltrStrBuf = new StringBuffer(s);
        ltrStrBuf = ltrStrBuf.reverse();
        byte [] bytes = ltrStrBuf.toString().getBytes("IBM424");
        
        CharsetDetector det = new CharsetDetector();
        det.setDetectableCharset("IBM424_rtl", true);
        det.setDetectableCharset("IBM424_ltr", true);
        det.setDetectableCharset("IBM420_rtl", true);
        det.setDetectableCharset("IBM420_ltr", true);
        det.setText(bytes);
        CharsetMatch m = det.detect();
        return m;
    }
    
    /*
     * Test the method int match(CharsetDetector det) in CharsetRecog_UTF_16_LE
     */
    @Test
    public void TestCharsetRecog_UTF_16_LE_Match() {
        byte[] in = { Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE };
        CharsetDetector cd = new CharsetDetector();
        // Tests when if (input.length>=4 && input[2] == 0x00 && input[3] == 0x00) is true inside the
        // match(CharsetDetector) method of CharsetRecog_UTF_16_LE
        try {
            cd.setText(in);
        } catch (Exception e) {
            errln("CharsetRecog_UTF_16_LE.match(CharsetDetector) was not suppose to return an exception.");
        }
    }

    //
    //  Bug #8309, test case submitted with original bug report.
    //
    @Test
    public void TestFreshDetectorEachTime() throws Exception
      {
          CharsetDetector detector1 = new CharsetDetector();
          byte[] data1 = createData1();
          detector1.setText(data1);
          CharsetMatch match1 = detector1.detect();
          assertEquals("Expected GB18030", "GB18030", match1.getName());
  
          CharsetDetector detector2 = new CharsetDetector();
          byte[] data2 = createData2();
          detector2.setText(data2);
          CharsetMatch match2 = detector2.detect();
          // It is actually GB18030 but the sample size is way too small to be reliable.
          assertEquals("Expected ISO-8859-1, even though that isn't strictly correct", "ISO-8859-1", match2.getName());
      }
  
    @Test
    public void TestReusingDetector() throws Exception
      {
          CharsetDetector detector = new CharsetDetector();
 
          byte[] data1 = createData1();
          detector.setText(data1);
          CharsetMatch match1 = detector.detect();
          assertEquals("Expected GB18030", "GB18030", match1.getName());
          byte[] data2 = createData2();
          detector.setText(data2);
          CharsetMatch match2 = detector.detect();
          assertEquals("Expected ISO-8859-1, even though that isn't strictly correct", "ISO-8859-1", match2.getName());
          // calling detect() one more time without changing the input data
          CharsetMatch match2a = detector.detect();
          assertEquals("[second]Expected ISO-8859-1, even though that isn't strictly correct", "ISO-8859-1", match2a.getName());
      }
  
      private static byte[] createData1()
      {
         return bytesFromString("3B 3B 3B 20 2D 2A 2D 20 4D 6F 64 65 3A 20 4C 49 53 50 3B 20 53 79 6E 74 61 78 " +
                                "3A 20 43 6F 6D 6D 6F 6E 2D 6C 69 73 70 3B 20 50 61 63 6B 61 67 65 3A 20 41 4C " +
                                "45 4D 42 49 43 20 3B 20 42 61 73 65 3A 20 31 30 20 2D 2A 2D 0D 0A 0D 0A 28 69 " +
                                "6E 2D 70 61 63 6B 61 67 65 20 22 41 4C 45 4D 42 49 43 22 29 0D 0A 0D 0A 28 6E " +
                                "6F 74 69 63 65 20 22 43 6F 70 79 72 69 67 68 74 20 74 68 65 20 4D 49 54 52 45 " +
                                "20 43 6F 72 70 6F 72 61 74 69 6F 6E 20 31 39 39 37 2D 31 39 39 38 2E 20 20 41 " +
                                "6C 6C 20 72 69 67 68 74 73 20 72 65 73 65 72 76 65 64 2E 22 29 0D 0A 0D 0A 3B " +
                                "3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B " +
                                "3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B " +
                                "3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 0D 0A 3B 3B 3B 20 20 " +
                                "20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 " +
                                "20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 " +
                                "20 20 20 20 20 20 20 20 20 20 20 20 3B 3B 3B 0D 0A 3B 3B 3B 20 20 20 43 68 69 " +
                                "6E 65 73 65 20 73 75 66 66 69 78 2C 20 70 72 65 66 69 78 20 61 6E 64 20 61 66 " +
                                "66 69 78 20 70 72 65 64 69 63 61 74 65 73 2E 09 09 20 20 20 20 20 3B 3B 3B 0D " +
                                "0A 3B 3B 3B 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 " +
                                "20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 " +
                                "20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 20 3B 3B 3B 0D 0A 3B 3B 3B " +
                                "3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B " +
                                "3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B " +
                                "3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 3B 0D 0A 0D 0A 28 64 65 66 76 " +
                                "61 72 20 2A 7A 68 2D 6E 61 6D 65 2D 66 69 6E 61 6C 2D 63 68 61 72 73 2A 0D 0A " +
                                "20 20 20 20 27 28 3B 3B 20 63 6F 6D 6D 6F 6E 20 4A 61 70 61 6E 65 73 65 20 50 " +
                                "65 72 73 6E 61 6D 65 20 65 6E 64 69 6E 67 0D 0A 20 20 20 20 20 20 22 CC AB C0 " +
                                "C9 22 0D 0A 20 20 20 20 20 20 3B 3B 20 4D 65 79 65 72 3F 0D 0A 20 20 20 20 20 " +
                                "20 3B 3B 20 C3 B7 C8 D5 0D 0A 20 20 20 20 20 20 22 B0 A2 22 20 22 B0 A3 22 20 " +
                                "22 B0 B2 22 20 22 B0 BA 22 20 22 B0 C2 22 20 22 B0 CD 22 0D 0A 20 20 20 20 20 " +
                                "20 22 B0 D7 22 20 22 B0 DD 22 20 22 B0 E0 22 20 22 B0 EE 22 20 22 B1 A4 22 20 " +
                                "22 B1 AB 22 0D 0A 20 20 20 20 20 20 22 B1 B4 22 20 22 B1 BE 22 20 22 B1 C8 22 " +
                                "20 22 B1 CF 22 20 22 B1 F6 22 20 22 B2 A8 22 0D 0A 20 20 20 20 20 20 22 B2 A9 " +
                                "22 20 22 B2 AA 22 20 22 B2 AE 22 20 22 B2 B7 22 20 22 B2 BC 22 20 22 B2 C9 22 " +
                                "0D 0A 20 20 20 20 20 20 22 B2 DF 22 20 22 B2 E9 22 20 22 B2 EC 22 20 22 B2 EE " +
                                "22 20 22 B3 B9 22 20 22 B4 C4 22 0D 0A 20 20 20 20 20 20 22 B4 EF 22 20 22 B4 " +
                                "F3 22 20 22 B4 F7 22 20 22 B4 FA 22 20 22 B5 A4 22 20 22 B5 B1 22 0D 0A 20 20 " +
                                "20 20 20 20 22 B5 C0 22 20 22 B5 C2 22 20 22 B5 C3 22 20 22 B5 C7 22 20 22 B5 " +
                                "CF 22 20 22 B5 D0 22 0D 0A 20 20 20 20 20 20 22 B5 D9 22 20 22 B5 DA 22 20 22 " +
                                "B5 D8 22 20 22 B6 A1 22 20 22 B6 AB 22 20 22 B6 BC 22 0D 0A 20 20 20 20 20 20 " +
                                "22 B6 C5 22 20 22 B6 D6 22 20 22 B6 D8 22 20 22 B6 D9 22 20 22 B6 E0 22 20 22 " +
                                "B6 F2 22 0D 0A 20 20 20 20 20 20 22 B6 F7 22 20 22 B6 FA 22 20 22 B6 FB 22 20 " +
                                "22 B7 A8 22 20 22 B7 B2 22 20 22 B7 B6 22 0D 0A 20 20 20 20 20 20 22 B7 BD 22 " +
                                "20 22 B7 C6 22 20 22 B7 D1 22 20 22 B7 D2 22 20 22 B7 E1 22 20 22 B7 EB 22 0D " +
                                "0A 20 20 20 20 20 20 22 B7 F0 22 20 22 B7 F2 22 20 22 B8 A3 22 20 22 B8 A5 22 " +
                                "20 22 B8 BB 22 20 22 B8 C7 22 0D 0A 20 20 20 20 20 20 22 B8 D4 22 20 22 B8 DF " +
                                "22 20 22 B8 E7 22 20 22 B8 EA 22 20 22 B8 F1 22 20 22 B8 F7 22 0D 0A 20 20 20 " +
                                "20 20 20 22 B8 F9 22 20 22 B9 B1 22 20 22 B9 C5 22 20 22 B9 E7 22 20 22 B9 FB " +
                                "22 20 22 B9 FE 22 0D 0A 20 20 20 20 20 20 22 BA A3 22 20 22 BA B2 22 20 22 BA " +
                                "BA 22 20 22 BA C0 22 20 22 BA D5 22 20 22 BA DA 22 0D 0A 20 20 20 20 20 20 22 " +
                                "BA E0 22 20 22 BA E9 22 20 22 BB AA 22 20 22 BB DD 22 20 22 BB F4 22 20 22 BB " +
                                "F9 22 0D 0A 20 20 20 20 20 20 22 BC AA 22 20 22 BC BE 22 20 22 BC CE 22 20 22 " +
                                "BC D3 22 20 22 BC D6 22 20 22 BC F2 22 0D 0A 20 20 20 20 20 20 22 BD AA 22 20 " +
                                "22 BD DC 22 20 22 BD F0 22 20 22 BD F1 22 20 22 BD F2 22 20 22 BF A1 22 0D 0A " +
                                "20 20 20 20 20 20 22 BF A8 22 20 22 BF AD 22 20 22 BF B2 22 20 22 BF B5 22 20 " +
                                "22 BF BC 22 20 22 BF C6 22 0D 0A 20 20 20 20 20 20 22 BF CB 22 20 22 BF CF 22 " +
                                "20 22 BF E2 22 20 22 BF E4 22 20 22 C0 A4 22 20 22 C0 A5 22 0D 0A 20 20 20 20 " +
                                "20 20 22 C0 AD 22 20 22 C0 B0 22 20 22 C0 B3 22 20 22 C0 B5 22 20 22 C0 BC 22 " +
                                "20 22 C0 CA 22 0D 0A 20 20 20 20 20 20 22 C0 CD 22 20 22 C0 D5 22 20 22 C0 D6 " +
                                "22 20 22 C0 D7 22 20 22 C0 E8 22 20 22 C0 EE 22 0D 0A 20 20 20 20 20 20 22 C0 " +
                                "EF 22 20 22 C0 F1 22 20 22 C0 F2 22 20 22 C0 F6 22 20 22 C0 FB 22 20 22 C1 AB " +
                                "22 0D 0A 20 20 20 20 20 20 22 C1 AE 22 20 22 C1 BC 22 20 22 C1 D0 22 20 22 C1 " +
                                "D2 22 20 22 C1 D5 22 20 22 C1 D6 22 0D 0A 20 20 20 20 20 20 22 C1 F5 22 20 22 " +
                                "C1 F8 22 20 22 C1 FA 22 20 22 C2 A1 22 20 22 C2 AC 22 20 22 C2 B3 22 0D 0A 20 " +
                                "20 20 20 20 20 22 C2 D4 22 20 22 C2 D7 22 20 22 C2 DE 22 20 22 C2 E5 22 20 22 " +
                                "C2 EA 22 20 22 C2 ED 22 0D 0A 20 20 20 20 20 20 22 C2 F5 22 20 22 C2 FC 22 20 " +
                                "22 C3 A2 22 20 22 C3 B7 22 20 22 C3 C5 22 20 22 C3 C9 22 0D 0A 20 20 20 20 20 " +
                                "20 22 C3 D7 22 20 22 C3 DC 22 20 22 C3 F7 22 20 22 C4 A6 22 20 22 C4 AA 22 20 " +
                                "22 C4 AC 22 0D 0A 20 20 20 20 20 20 22 C4 B7 22 20 22 C4 B8 22 20 22 C4 C3 22 " +
                                "20 22 C4 C7 22 20 22 C4 C8 22 20 22 C4 C9 22 0D 0A 20 20 20 20 20 20 22 C4 CE " +
                                "22 20 22 C4 CF 22 20 22 C4 DA 22 20 22 C4 DD 22 20 22 C4 E1 22 20 22 C4 F9 22 " +
                                "0D 0A 20 20 20 20 20 20 22 C4 FE 22 20 22 C5 A6 22 20 22 C5 A9 22 20 22 C5 AC " +
                                "22 20 22 C5 B5 22 20 22 C5 B7 22 0D 0A 20 20 20 20 20 20 22 C5 C1 22 20 22 C5 " +
                                "CB 22 20 22 C5 D3 22 20 22 C5 E5 22 20 22 C5 ED 22 20 22 C5 EE 22 0D 0A 20 20 " +
                                "20 20 20 20 22 C6 A4 22 20 22 C6 BD 22 20 22 C6 D5 22 20 22 C6 E6 22 20 22 C6 " +
                                "EB 22 20 22 C6 F5 22 0D 0A 20 20 20 20 20 20 22 C7 A1 22 20 22 C7 C7 22 20 22 " +
                                "C7 D0 22 20 22 C7 D5 22 20 22 C7 D9 22 20 22 C7 E5 22 0D 0A 20 20 20 20 20 20 " +
                                "22 C7 ED 22 20 22 C7 F0 22 20 22 C7 F5 22 20 22 C8 AA 22 20 22 C8 C3 22 20 22 " +
                                "C8 C8 22 0D 0A 20 20 20 20 20 20 22 C8 E5 22 20 22 C8 F6 22 20 22 C8 F8 22 20 " +
                                "22 C8 FB 22 20 22 C8 FC 22 20 22 C9 A3 22 0D 0A 20 20 20 20 20 20 22 C9 AA 22 " +
                                "20 22 C9 AD 22 20 22 C9 AF 22 20 22 C9 B3 22 20 22 C9 BA 22 0D 0A 20 20 20 20 " +
                                "20 20 3B 3B 20 22 C9 CF 22 0D 0A 20 20 20 20 20 20 22 C9 D0 22 20 22 C9 DC 22 " +
                                "20 22 C9 E1 22 20 22 C9 EA 22 20 22 C9 FA 22 20 22 CA A9 22 0D 0A 20 20 20 20 " +
                                "20 20 22 CA AB 22 20 22 CA AF 22 20 22 CA B2 22 20 22 CA BF 22 20 22 CA D2 22 " +
                                "20 22 CB B9 22 0D 0A 20 20 20 20 20 20 22 CB BC 22 20 22 CB BF 22 20 22 CB C9 " +
                                "22 20 22 CB D5 22 20 22 CB D8 22 20 22 CB E7 22 0D 0A 20 20 20 20 20 20 22 CB " +
                                "F7 22 20 22 CB FE 22 20 22 CC A9 22 20 22 CC AB 22 20 22 CC B9 22 20 22 CC C0 " +
                                "22 0D 0A 20 20 20 20 20 20 22 CC C6 22 20 22 CC D5 22 20 22 CC D8 22 20 22 CC " +
                                "E1 22 20 22 CD A1 22 20 22 CD A2 22 0D 0A 20 20 20 20 20 20 22 CD A8 22 20 22 " +
                                "CD B8 22 20 22 CD BC 22 20 22 CD D0 22 20 22 CD D1 22 20 22 CD DE 22 0D 0A 20 " +
                                "20 20 20 20 20 22 CD DF 22 20 22 CD F2 22 20 22 CD FA 22 20 22 CD FE 22 20 22 " +
                                "CE A2 22 20 22 CE A4 22 0D 0A 20 20 20 20 20 20 22 CE AC 22 20 22 CE C0 22 20 " +
                                "22 CE C2 22 20 22 CE C4 22 20 22 CE CC 22 20 22 CE D6 22 0D 0A 20 20 20 20 20 " +
                                "20 22 CE DA 22 20 22 CE F7 22 20 22 CE FD 22 20 22 CF A3 22 20 22 CF BC 22 20 " +
                                "22 CF C4 22 0D 0A 20 20 20 20 20 20 22 CF E3 22 0D 0A 20 20 20 20 20 20 3B 3B " +
                                "20 70 72 65 70 6F 73 69 74 69 6F 6E 0D 0A 20 20 20 20 20 20 3B 3B 20 22 CF F2 " +
                                "22 0D 0A 20 20 20 20 20 20 22 D0 A4 22 20 22 D0 AA 22 20 22 D0 BB 22 0D 0A 20 " +
                                "20 20 20 20 20 22 D0 C0 22 20 22 D0 C1 22 20 22 D0 CB 22 20 22 D0 D0 22 20 22 " +
                                "D0 D2 22 20 22 D0 DB 22 0D 0A 20 20 20 20 20 20 22 D0 DD 22 20 22 D1 B7 22 20 " +
                                "22 D1 C0 22 20 22 D1 C7 22 20 22 D1 D3 22 20 22 D1 EF 22 0D 0A 20 20 20 20 20 " +
                                "20 22 D2 AE 22 20 22 D2 B6 22 20 22 D2 C1 22 20 22 D2 F2 22 20 22 D3 A2 22 20 " +
                                "22 D3 C8 22 0D 0A 20 20 20 20 20 20 22 D4 BC 22 20 22 D4 D7 22 20 22 D4 DE 22 " +
                                "20 22 D4 E7 22 20 22 D4 F3 22 20 22 D4 F8 22 0D 0A 20 20 20 20 20 20 22 D4 FA " +
                                "22 20 22 D5 B2 22 20 22 D5 C2 22 20 22 D6 A5 22 20 22 D6 BA 22 20 22 D6 CE 22 " +
                                "0D 0A 20 20 20 20 20 20 22 D6 E9 22 20 22 D7 C8 22 20 22 D7 CC 22 20 22 D7 DA " +
                                "22 20 22 D7 F4 22 20 22 E1 AF 22 0D 0A 20 20 20 20 20 20 22 E6 AB 22 20 22 E6 " +
                                "DA 22 20 22 E7 D1 22 20 22 E7 EA 22 20 22 E7 F7 22 20 22 E8 A7 22 0D 0A 20 20 " +
                                "20 20 20 20 22 E9 AA 22 20 22 EB F8 22 20 22 F7 EB 22 0D 0A 20 20 20 20 20 20 " +
                                "29 29 0D 0A 0D 0A 28 64 65 66 76 61 72 20 2A 7A 68 2D 6E 61 6D 65 2D 69 6E 69 " +
                                "74 69 61 6C 2D 63 68 61 72 73 2A 0D 0A 20 20 20 20 27 28 22 B0 A2 22 20 22 B0 " +
                                "A3 22 20 22 B0 AC 22 20 22 B0 AE 22 20 22 B0 B2 22 20 22 B0 BA 22 20 22 B0 C2 " +
                                "22 0D 0A 20 20 20 20 20 20 22 B0 C4 22 20 22 B0 CD 22 20 22 B0 D7 22 20 22 B0 " +
                                "DD 22 20 22 B0 E0 22 20 22 B0 EE 22 0D 0A 20 20 20 20 20 20 22 B1 A3 22 20 22 " +
                                "B1 AB 22 20 22 B1 B4 22 20 22 B1 BE 22 20 22 B1 C8 22 20 22 B1 CB 22 0D 0A 20 " +
                                "20 20 20 20 20 22 B1 CF 22 20 22 B1 F6 22 20 22 B2 A8 22 20 22 B2 A9 22 20 22 " +
                                "B2 AE 22 20 22 B2 BC 22 0D 0A 20 20 20 20 20 20 22 B2 CC 22 20 22 B2 E9 22 20 " +
                                "22 B2 F1 22 20 22 B2 FD 22 20 22 B3 B9 22 20 22 B3 FE 22 0D 0A 20 20 20 20 20 " +
                                "20 22 B4 C4 22 20 22 B4 EF 22 20 22 B4 F7 22 20 22 B4 FA 22 20 22 B5 A4 22 20 " +
                                "22 B5 B1 22 0D 0A 20 20 20 20 20 20 22 B5 C0 22 20 22 B5 C2 22 20 22 B5 C7 22 " +
                                "20 22 B5 CB 22 20 22 B5 CF 22 20 22 B5 D2 22 0D 0A 20 20 20 20 20 20 22 B5 D9 " +
                                "22 20 22 B6 A1 22 20 22 B6 C5 22 20 22 B6 CB 22 20 22 B6 D8 22 20 22 B6 D9 22 " +
                                "0D 0A 20 20 20 20 20 20 22 B6 E0 22 20 22 B6 F2 22 20 22 B6 F7 22 20 22 B7 A8 " +
                                "22 20 22 B7 B6 22 20 22 B7 BD 22 0D 0A 20 20 20 20 20 20 22 B7 C6 22 20 22 B7 " +
                                "D1 22 20 22 B7 D2 22 20 22 B7 EB 22 20 22 B7 F2 22 20 22 B7 F0 22 0D 0A 20 20 " +
                                "20 20 20 20 22 B7 FC 22 20 22 B8 A3 22 20 22 B8 A5 22 20 22 B8 BB 22 20 22 B8 " +
                                "C7 22 20 22 B8 CA 22 0D 0A 20 20 20 20 20 20 22 B8 D4 22 20 22 B8 DF 22 20 22 " +
                                "B8 E7 22 20 22 B8 EA 22 20 22 B8 F1 22 20 22 B8 F9 22 0D 0A 20 20 20 20 20 20 " +
                                "22 B9 B1 22 20 22 B9 C5 22 20 22 B9 CF 22 20 22 B9 E7 22 20 22 B9 FA 22 20 22 " +
                                "B9 FE 22 0D 0A 20 20 20 20 20 20 22 BA A3 22 20 22 BA BA 22 20 22 BA BC 22 20 " +
                                "22 BA C0 22 20 22 BA D5 22 20 22 BA DA 22 0D 0A 20 20 20 20 20 20 22 BA E0 22 " +
                                "20 22 BA F4 22 20 22 BA FA 22 20 22 BB AA 22 20 22 BB B3 22 20 22 BB DD 22 0D " +
                                "0A 20 20 20 20 20 20 22 BB F4 22 20 22 BB F9 22 20 22 BC AA 22 20 22 BC B8 22 " +
                                "20 22 BC D3 22 20 22 BC D6 22 0D 0A 20 20 20 20 20 20 22 BC F2 22 20 22 BC FB " +
                                "22 20 22 BD AD 22 20 22 BD DC 22 20 22 BD F0 22 20 22 BD F2 22 0D 0A 20 20 20 " +
                                "20 20 20 22 BE AE 22 20 22 BF A8 22 20 22 BF AD 22 20 22 BF B2 22 20 22 BF B5 " +
                                "22 20 22 BF BC 22 0D 0A 20 20 20 20 20 20 22 BF C2 22 20 22 BF C6 22 20 22 BF " +
                                "CB 22 20 22 BF CF 22 20 22 BF D7 22 20 22 BF E2 22 0D 0A 20 20 20 20 20 20 22 " +
                                "BF E4 22 20 22 BF EF 22 20 22 BF FC 22 20 22 BF FD 22 20 22 C0 A5 22 20 22 C0 " +
                                "AD 22 0D 0A 20 20 20 20 20 20 22 C0 B0 22 20 22 C0 B3 22 20 22 C0 B5 22 20 22 " +
                                "C0 BC 22 20 22 C0 CA 22 20 22 C0 CD 22 0D 0A 20 20 20 20 20 20 22 C0 D5 22 20 " +
                                "22 C0 D7 22 20 22 C0 E8 22 20 22 C0 ED 22 20 22 C0 EE 22 20 22 C0 EF 22 0D 0A " +
                                "20 20 20 20 20 20 22 C0 F2 22 20 22 C0 F6 22 20 22 C0 FB 22 20 22 C1 D0 22 20 " +
                                "22 C1 D5 22 20 22 C1 D6 22 0D 0A 20 20 20 20 20 20 22 C1 F5 22 20 22 C1 F8 22 " +
                                "20 22 C1 FA 22 20 22 C2 A1 22 20 22 C2 AC 22 20 22 C2 B3 22 0D 0A 20 20 20 20 " +
                                "20 20 22 C2 B6 22 20 22 C2 B7 22 20 22 C2 C0 22 20 22 C2 D7 22 20 22 C2 DE 22 " +
                                "20 22 C2 E5 22 0D 0A 20 20 20 20 20 20 22 C2 EA 22 20 22 C2 ED 22 20 22 C2 F3 " +
                                "22 20 22 C2 F5 22 20 22 C2 FC 22 20 22 C3 A2 22 0D 0A 20 20 20 20 20 20 22 C3 " +
                                "AB 22 20 22 C3 B7 22 20 22 C3 C5 22 20 22 C3 C9 22 20 22 C3 CF 22 20 22 C3 D7 " +
                                "22 0D 0A 20 20 20 20 20 20 22 C3 F7 22 20 22 C4 A6 22 20 22 C4 AA 22 20 22 C4 " +
                                "AB 22 20 22 C4 AC 22 20 22 C4 B7 22 0D 0A 20 20 20 20 20 20 22 C4 C2 22 20 22 " +
                                "C4 C9 22 20 22 C4 CE 22 20 22 C4 CF 22 20 22 C4 DA 22 20 22 C4 DD 22 0D 0A 20 " +
                                "20 20 20 20 20 22 C4 E1 22 20 22 C4 FE 22 20 22 C5 A3 22 20 22 C5 A6 22 20 22 " +
                                "C5 A9 22 20 22 C5 AC 22 0D 0A 20 20 20 20 20 20 22 C5 B5 22 20 22 C5 B7 22 20 " +
                                "22 C5 C1 22 20 22 C5 C9 22 20 22 C5 CB 22 20 22 C5 D3 22 0D 0A 20 20 20 20 20 " +
                                "20 22 C5 E0 22 20 22 C5 E5 22 20 22 C5 ED 22 20 22 C6 A4 22 20 22 C6 BD 22 20 " +
                                "22 C6 D3 22 0D 0A 20 20 20 20 20 20 22 C6 D5 22 20 22 C6 CF 22 20 22 C6 E6 22 " +
                                "20 22 C6 EB 22 20 22 C7 A1 22 20 22 C7 AE 22 0D 0A 20 20 20 20 20 20 22 C7 C5 " +
                                "22 20 22 C7 C7 22 20 22 C7 D0 22 20 22 C7 D5 22 20 22 C7 ED 22 20 22 C7 F0 22 " +
                                "0D 0A 20 20 20 20 20 20 22 C7 F1 22 20 22 C8 C8 22 20 22 C8 D9 22 20 22 C8 E5 " +
                                "22 20 22 C8 F0 22 20 22 C8 F6 22 0D 0A 20 20 20 20 20 20 22 C8 F8 22 20 22 C8 " +
                                "FB 22 20 22 C8 FC 22 20 22 C9 A3 22 20 22 C9 AA 22 20 22 C9 AD 22 0D 0A 20 20 " +
                                "20 20 20 20 22 C9 AF 22 20 22 C9 B3 22 20 22 C9 DC 22 20 22 C9 E1 22 20 22 C9 " +
                                "EA 22 20 22 CA A5 22 0D 0A 20 20 20 20 20 20 22 CA A9 22 20 22 CA B7 22 20 22 " +
                                "CA E6 22 20 22 CB B7 22 20 22 CB B9 22 20 22 CB BC 22 0D 0A 20 20 20 20 20 20 " +
                                "22 CB C9 22 20 22 CB D5 22 20 22 CB F7 22 20 22 CB F9 22 20 22 CB FE 22 20 22 " +
                                "CC A9 22 0D 0A 20 20 20 20 20 20 22 CC B9 22 20 22 CC C0 22 20 22 CC C6 22 20 " +
                                "22 CC D5 22 20 22 CC D8 22 20 22 CC FA 22 0D 0A 20 20 20 20 20 20 22 CD A2 22 " +
                                "20 22 CD A8 22 20 22 CD BC 22 20 22 CD CF 22 20 22 CD D0 22 20 22 CD DF 22 0D " +
                                "0A 20 20 20 20 20 20 22 CD F2 22 20 22 CD FA 22 20 22 CD FE 22 20 22 CE A4 22 " +
                                "20 22 CE AC 22 20 22 CE BA 22 0D 0A 20 20 20 20 20 20 22 CE C2 22 20 22 CE C4 " +
                                "22 20 22 CE CC 22 20 22 CE D6 22 20 22 CE DA 22 20 22 CE E4 22 0D 0A 20 20 20 " +
                                "20 20 20 22 CE E9 22 20 22 CE F7 22 20 22 CE FD 22 20 22 CF A3 22 20 22 CF A4 " +
                                "22 20 22 CF AF 22 0D 0A 20 20 20 20 20 20 22 CF C4 22 20 22 CF E3 22 20 22 D0 " +
                                "A4 22 20 22 D0 AA 22 20 22 D0 BB 22 20 22 D0 C0 22 0D 0A 20 20 20 20 20 20 22 " +
                                "D0 C1 22 20 22 D0 DD 22 20 22 D0 DE 22 20 22 D0 ED 22 20 22 D0 F0 22 20 22 D1 " +
                                "A6 22 0D 0A 20 20 20 20 20 20 22 D1 A9 22 20 22 D1 C5 22 20 22 D1 C7 22 20 22 " +
                                "D1 D3 22 20 22 D1 EF 22 20 22 D2 AB 22 0D 0A 20 20 20 20 20 20 22 D2 AE 22 20 " +
                                "22 D2 B6 22 20 22 D2 C1 22 20 22 D2 D7 22 20 22 D3 A1 22 20 22 D3 A2 22 0D 0A " +
                                "20 20 20 20 20 20 22 D3 C8 22 20 22 D3 DA 22 20 22 D3 EA 22 20 22 D4 BC 22 20 " +
                                "22 D4 DE 22 20 22 D4 F3 22 0D 0A 20 20 20 20 20 20 22 D4 F8 22 20 22 D5 A7 22 " +
                                "20 22 D4 FA 22 20 22 D5 B2 22 20 22 D5 C5 22 20 22 D5 E4 22 0D 0A 20 20 20 20 " +
                                "20 20 22 D6 A5 22 20 22 D6 EC 22 20 22 D7 C8 22 20 22 D7 DA 22 20 22 D7 F4 22 " +
                                "20 22 E7 EA 22 0D 0A 20 20 20 20 20 20 22 E8 A7 22 20 22 EB F8 22 20 22 F7 EC " +
                                "22 0D 0A 20 20 20 20 20 20 29 29 0D 0A 0D 0A 28 64 65 66 76 61 72 20 2A 7A 68 " +
                                "2D 6E 61 6D 65 2D 6D 65 64 69 61 6C 2D 63 68 61 72 73 2A 0D 0A 20 20 20 20 27 " +
                                "28 22 A1 A4 22 20 22 A3 AE 22 20 22 B0 A2 22 20 22 B0 A3 22 20 22 B0 AC 22 20 " +
                                "22 B0 AE 22 0D 0A 20 20 20 20 20 20 22 B0 B2 22 20 22 B0 BA 22 20 22 B0 C2 22 " +
                                "20 22 B0 C4 22 20 22 B0 CD 22 20 22 B0 D7 22 0D 0A 20 20 20 20 20 20 22 B0 DD " +
                                "22 20 22 B0 E0 22 20 22 B0 EE 22 20 22 B1 AB 22 20 22 B1 B1 22 20 22 B1 B4 22 " +
                                "0D 0A 20 20 20 20 20 20 22 B1 B6 22 20 22 B1 BE 22 20 22 B1 C8 22 20 22 B1 CB " +
                                "22 20 22 B1 D9 22 20 22 B1 F0 22 0D 0A 20 20 20 20 20 20 22 B1 F6 22 20 22 B2 " +
                                "A8 22 20 22 B2 AA 22 20 22 B2 A9 22 20 22 B2 AE 22 20 22 B2 B7 22 0D 0A 20 20 " +
                                "20 20 20 20 22 B2 BC 22 20 22 B2 DF 22 20 22 B2 E9 22 20 22 B2 F1 22 20 22 B3 " +
                                "B9 22 20 22 B3 C2 22 0D 0A 20 20 20 20 20 20 22 B4 C4 22 20 22 B4 CE 22 20 22 " +
                                "B4 EF 22 20 22 B4 F3 22 20 22 B4 F7 22 20 22 B4 FA 22 0D 0A 20 20 20 20 20 20 " +
                                "22 B5 A4 22 20 22 B5 B1 22 20 22 B5 C0 22 20 22 B5 C2 22 20 22 B5 C3 22 20 22 " +
                                "B5 C7 22 0D 0A 20 20 20 20 20 20 22 B5 CB 22 20 22 B5 CF 22 20 22 B5 D2 22 20 " +
                                "22 B5 D8 22 20 22 B5 DA 22 20 22 B5 D9 22 0D 0A 20 20 20 20 20 20 22 B6 A1 22 " +
                                "20 22 B6 AB 22 20 22 B6 C5 22 20 22 B6 D9 22 20 22 B6 E0 22 20 22 B6 F2 22 0D " +
                                "0A 20 20 20 20 20 20 22 B6 ED 22 20 22 B6 F7 22 20 22 B6 FB 22 20 22 B7 A8 22 " +
                                "20 22 B7 B6 22 20 22 B7 BD 22 0D 0A 20 20 20 20 20 20 22 B7 C6 22 20 22 B7 C7 " +
                                "22 20 22 B7 D1 22 20 22 B7 D2 22 20 22 B7 E1 22 20 22 B7 EB 22 0D 0A 20 20 20 " +
                                "20 20 20 22 B7 F0 22 20 22 B7 F2 22 20 22 B8 A3 22 20 22 B8 A5 22 20 22 B8 B5 " +
                                "22 20 22 B8 BB 22 0D 0A 20 20 20 20 20 20 22 B8 C7 22 20 22 B8 CA 22 20 22 B8 " +
                                "DF 22 20 22 B8 E7 22 20 22 B8 EA 22 20 22 B8 EF 22 0D 0A 20 20 20 20 20 20 22 " +
                                "B8 F0 22 20 22 B8 F1 22 20 22 B8 F7 22 20 22 B8 F9 22 20 22 B8 FC 22 20 22 B9 " +
                                "B1 22 0D 0A 20 20 20 20 20 20 22 B9 C3 22 20 22 B9 C5 22 20 22 B9 CF 22 20 22 " +
                                "B9 E7 22 20 22 B9 FA 22 20 22 B9 FE 22 0D 0A 20 20 20 20 20 20 22 BA A3 22 20 " +
                                "22 BA B1 22 20 22 BA B2 22 20 22 BA BA 22 20 22 BA C0 22 20 22 BA C1 22 0D 0A " +
                                "20 20 20 20 20 20 22 BA D5 22 20 22 BA DA 22 20 22 BA E0 22 20 22 BA E3 22 20 " +
                                "22 BA E9 22 20 22 BA EE 22 0D 0A 20 20 20 20 20 20 22 BA FA 22 20 22 BB AA 22 " +
                                "20 22 BB B3 22 20 22 BB DD 22 20 22 BB F4 22 20 22 BB F9 22 0D 0A 20 20 20 20 " +
                                "20 20 22 BC AA 22 20 22 BC BE 22 20 22 BC CE 22 20 22 BC D3 22 20 22 BC D6 22 " +
                                "20 22 BD DC 22 0D 0A 20 20 20 20 20 20 22 BD F0 22 20 22 BD F2 22 20 22 BF A8 " +
                                "22 20 22 BF AA 22 20 22 BF AD 22 20 22 BF B2 22 0D 0A 20 20 20 20 20 20 22 BF " +
                                "B5 22 20 22 BF BC 22 20 22 BF C2 22 20 22 BF C6 22 20 22 BF CB 22 20 22 BF A6 " +
                                "22 0D 0A 20 20 20 20 20 20 22 BF CF 22 20 22 BF D7 22 20 22 BF DB 22 20 22 BF " +
                                "E2 22 20 22 BF E4 22 20 22 BF FC 22 0D 0A 20 20 20 20 20 20 22 C0 A5 22 20 22 " +
                                "C0 AA 22 20 22 C0 AD 22 20 22 C0 B0 22 20 22 C0 B3 22 20 22 C0 B4 22 0D 0A 20 " +
                                "20 20 20 20 20 22 C0 B5 22 20 22 C0 BC 22 20 22 C0 CA 22 20 22 C0 CD 22 20 22 " +
                                "C0 D5 22 20 22 C0 D7 22 0D 0A 20 20 20 20 20 20 22 C0 D9 22 20 22 C0 E8 22 20 " +
                                "22 C0 ED 22 20 22 C0 EE 22 20 22 C0 EF 22 20 22 C0 F1 22 0D 0A 20 20 20 20 20 " +
                                "20 22 C0 F2 22 20 22 C0 F6 22 20 22 C0 FA 22 20 22 C0 FB 22 20 22 C1 A2 22 20 " +
                                "22 C1 AC 22 0D 0A 20 20 20 20 20 20 22 C1 AB 22 20 22 C1 AA 22 20 22 C1 AE 22 " +
                                "20 22 C1 BF 22 20 22 C1 D0 22 20 22 C1 D2 22 0D 0A 20 20 20 20 20 20 22 C1 D5 " +
                                "22 20 22 C1 D6 22 20 22 C1 F4 22 20 22 C1 F8 22 20 22 C1 FA 22 20 22 C2 A1 22 " +
                                "0D 0A 20 20 20 20 20 20 22 C2 AC 22 20 22 C2 B3 22 20 22 C2 B6 22 20 22 C2 B7 " +
                                "22 20 22 C2 C0 22 20 22 C2 D4 22 0D 0A 20 20 20 20 20 20 22 C2 D7 22 20 22 C2 " +
                                "D8 22 20 22 C2 DC 22 20 22 C2 DE 22 20 22 C2 E5 22 20 22 C2 EA 22 0D 0A 20 20 " +
                                "20 20 20 20 22 C2 ED 22 20 22 C2 F3 22 20 22 C2 F5 22 20 22 C2 FA 22 20 22 C2 " +
                                "FC 22 20 22 C3 A2 22 0D 0A 20 20 20 20 20 20 22 C3 A9 22 20 22 C3 B7 22 20 22 " +
                                "C3 C0 22 20 22 C3 C5 22 20 22 C3 C9 22 20 22 C3 D7 22 0D 0A 20 20 20 20 20 20 " +
                                "22 C3 DC 22 20 22 C3 F4 22 20 22 C3 F7 22 20 22 C4 A6 22 20 22 C4 AA 22 20 22 " +
                                "C4 AC 22 0D 0A 20 20 20 20 20 20 22 C4 AD 22 20 22 C4 B7 22 20 22 C4 BE 22 20 " +
                                "22 C4 C2 22 20 22 C4 C3 22 20 22 C4 C7 22 0D 0A 20 20 20 20 20 20 22 C4 C8 22 " +
                                "20 22 C4 C9 22 20 22 C4 CE 22 20 22 C4 CF 22 20 22 C4 DA 22 20 22 C4 DD 22 0D " +
                                "0A 20 20 20 20 20 20 22 C4 E1 22 20 22 C4 EA 22 20 22 C4 EE 22 20 22 C4 F9 22 " +
                                "20 22 C4 FE 22 20 22 C5 A6 22 0D 0A 20 20 20 20 20 20 22 C5 A9 22 20 22 C5 AC " +
                                "22 20 22 C5 B5 22 20 22 C5 B7 22 20 22 C5 C1 22 20 22 C5 C9 22 0D 0A 20 20 20 " +
                                "20 20 20 22 C5 CB 22 20 22 C5 D3 22 20 22 C5 E0 22 20 22 C5 E5 22 20 22 C5 ED " +
                                "22 20 22 C5 EE 22 0D 0A 20 20 20 20 20 20 22 C6 A4 22 20 22 C6 BD 22 20 22 C6 " +
                                "C3 22 20 22 C6 D5 22 20 22 C6 E1 22 20 22 C6 E6 22 0D 0A 20 20 20 20 20 20 22 " +
                                "C6 EB 22 20 22 C6 E4 22 20 22 C6 F5 22 20 22 C7 A1 22 20 22 C7 AE 22 20 22 C7 " +
                                "BF 22 0D 0A 20 20 20 20 20 20 22 C7 C7 22 20 22 C7 D0 22 20 22 C7 D5 22 20 22 " +
                                "C7 D9 22 20 22 C7 ED 22 20 22 C7 F0 22 0D 0A 20 20 20 20 20 20 22 C7 F3 22 20 " +
                                "22 C8 B4 22 20 22 C8 C8 22 20 22 C8 E5 22 20 22 C8 F4 22 20 22 C8 F6 22 0D 0A " +
                                "20 20 20 20 20 20 22 C8 F8 22 20 22 C8 FB 22 20 22 C8 FC 22 20 22 C9 A3 22 20 " +
                                "22 C9 AA 22 20 22 C9 AD 22 0D 0A 20 20 20 20 20 20 22 C9 AF 22 20 22 C9 B3 22 " +
                                "20 22 C9 BA 22 20 22 C9 BD 22 20 22 C9 DC 22 20 22 C9 E1 22 0D 0A 20 20 20 20 " +
                                "20 20 22 C9 EA 22 20 22 CA A2 22 20 22 CA A9 22 20 22 CA B2 22 20 22 CA B7 22 " +
                                "20 22 CA BF 22 0D 0A 20 20 20 20 20 20 22 CA E6 22 20 22 CB B9 22 20 22 CB BC " +
                                "22 20 22 CB BE 22 20 22 CB BF 22 20 22 CB C9 22 0D 0A 20 20 20 20 20 20 22 CB " +
                                "D5 22 20 22 CB F7 22 20 22 CB FE 22 20 22 CC A9 22 20 22 CC AB 22 20 22 CC B9 " +
                                "22 0D 0A 20 20 20 20 20 20 22 CC C0 22 20 22 CC C6 22 20 22 CC D5 22 20 22 CC " +
                                "D1 22 20 22 CC D8 22 20 22 CC E8 22 0D 0A 20 20 20 20 20 20 22 CC FA 22 20 22 " +
                                "CD A1 22 20 22 CD A2 22 20 22 CD A8 22 20 22 CD BC 22 20 22 CD BD 22 0D 0A 20 " +
                                "20 20 20 20 20 22 CD C2 22 20 22 CD CB 22 20 22 CD D0 22 20 22 CD DF 22 20 22 " +
                                "CD F2 22 20 22 CD FA 22 0D 0A 20 20 20 20 20 20 22 CD FE 22 20 22 CE A4 22 20 " +
                                "22 CE AC 22 20 22 CE B0 22 20 22 CE BA 22 20 22 CE C0 22 0D 0A 20 20 20 20 20 " +
                                "20 22 CE C2 22 20 22 CE C4 22 20 22 CE CC 22 20 22 CE D6 22 20 22 CE DA 22 20 " +
                                "22 CE E4 22 0D 0A 20 20 20 20 20 20 22 CE E9 22 20 22 CE EE 22 20 22 CE F7 22 " +
                                "20 22 CE FD 22 20 22 CF A3 22 20 22 CF AF 22 0D 0A 20 20 20 20 20 20 22 CF C4 " +
                                "22 20 22 CF E3 22 20 22 CF FE 22 20 22 D0 A4 22 20 22 D0 AA 22 20 22 D0 BB 22 " +
                                "0D 0A 20 20 20 20 20 20 22 D0 C0 22 20 22 D0 C1 22 20 22 D0 C2 22 20 22 D0 CB " +
                                "22 20 22 D0 D2 22 20 22 D0 DD 22 0D 0A 20 20 20 20 20 20 22 D0 DE 22 20 22 D0 " +
                                "F5 22 20 22 D1 A9 22 20 22 D1 C7 22 20 22 E6 AB 22 20 22 D1 EF 22 0D 0A 20 20 " +
                                "20 20 20 20 22 D1 F4 22 20 22 D2 AE 22 20 22 D2 B6 22 20 22 D2 C0 22 20 22 D2 " +
                                "C1 22 20 22 D2 D4 22 0D 0A 20 20 20 20 20 20 22 D2 D7 22 20 22 D2 F2 22 20 22 " +
                                "D3 A2 22 20 22 D3 C0 22 20 22 D3 C8 22 20 22 D3 D0 22 0D 0A 20 20 20 20 20 20 " +
                                "22 D3 D1 22 20 22 D3 D7 22 20 22 D3 F4 22 20 22 D4 BC 22 20 22 D4 DE 22 20 22 " +
                                "D4 F3 22 0D 0A 20 20 20 20 20 20 22 D4 F6 22 20 22 D4 F8 22 20 22 D4 FA 22 20 " +
                                "22 D5 B2 22 20 22 D5 E4 22 20 22 D5 FD 22 0D 0A 20 20 20 20 20 20 22 D6 CE 22 " +
                                "20 22 D6 DC 22 20 22 D6 EC 22 20 22 D7 BF 22 20 22 D7 C8 22 20 22 D7 CC 22 0D " +
                                "0A 20 20 20 20 20 20 22 D7 D3 22 20 22 D7 E6 22 20 22 D7 F4 22 20 22 DA F7 22 " +
                                "20 22 E1 AF 22 20 22 E7 D1 22 0D 0A 20 20 20 20 20 20 22 E7 EA 22 20 22 E8 A7 " +
                                "22 20 22 E9 A6 22 20 22 EB F8 22 20 22 EC B3 22 20 22 F1 E3 22 0D 0A 20 20 20 " +
                                "20 20 20 22 F7 EB 22 20 22 F7 EC 22 0D 0A 20 20 20 20 20 20 29 29 0D 0A 0D 0A " +
                                "0D 0A 28 64 65 66 75 6E 20 7A 68 2D 6E 61 6D 65 2D 73 75 66 66 69 78 3F 20 28 " +
                                "73 74 72 69 6E 67 29 0D 0A 20 20 28 6D 65 6D 62 65 72 20 73 74 72 69 6E 67 20 " +
                                "2A 7A 68 2D 6E 61 6D 65 2D 66 69 6E 61 6C 2D 63 68 61 72 73 2A 20 3A 74 65 73 " +
                                "74 20 23 27 6D 61 74 63 68 69 6E 67 2D 73 75 66 66 69 78 3F 29 29 0D 0A 0D 0A " +
                                "28 64 65 66 75 6E 20 7A 68 2D 6E 61 6D 65 2D 70 72 65 66 69 78 3F 20 28 73 74 " +
                                "72 69 6E 67 29 0D 0A 20 20 28 6D 65 6D 62 65 72 20 73 74 72 69 6E 67 20 2A 7A " +
                                "68 2D 6E 61 6D 65 2D 69 6E 69 74 69 61 6C 2D 63 68 61 72 73 2A 20 3A 74 65 73 " +
                                "74 20 23 27 6D 61 74 63 68 69 6E 67 2D 70 72 65 66 69 78 3F 29 29 0D 0A 0D 0A " +
                                "28 64 65 66 75 6E 20 7A 68 2D 6E 61 6D 65 2D 69 6E 66 69 78 3F 20 28 73 74 72 " +
                                "69 6E 67 29 0D 0A 20 20 28 6D 65 6D 62 65 72 20 73 74 72 69 6E 67 20 2A 7A 68 " +
                                "2D 6E 61 6D 65 2D 6D 65 64 69 61 6C 2D 63 68 61 72 73 2A 20 3A 74 65 73 74 20 " +
                                "23 27 6D 61 74 63 68 69 6E 67 2D 73 75 62 73 74 72 69 6E 67 3F 29 29 0D 0A 09 " +
                                "20 20 0D 0A");
      }
  
      private static byte[] createData2()
      {
          return bytesFromString("0A D0 A1 CA B1 20 3B 3B 20 48 6F 75 72 28 73 29 0A D0 C7 C6 DA 20 3B 3B 20 57 " +
                                 "65 65 6B 28 73 29 0A B5 B1 B5 D8 20 CA B1 BC E4 20 3B 3B 20 6C 6F 63 61 6C 20 " +
                                 "74 69 6D 65 0A");
      }
  
  
      /**
       * Creates a byte array by decoding the hex string passed in.
       *
       * @param data hex string data.
       * @return binary data in a byte array.
       */
      private static byte[] bytesFromString(String data)
      {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          int index = 0;
          while (index < data.length())
          {
           char[] number = new char[2];
              number[0] = data.charAt(index++);
              if (Character.isLetterOrDigit(number[0]))
              {
                  number[1] = data.charAt(index++);
                  if (!Character.isLetterOrDigit(number[1]))
                  {
                      throw new IllegalArgumentException("Invalid input: " + data);
                  }
                  out.write(Integer.parseInt(new String(number), 16));
              }
          }
  
          return out.toByteArray();
      }
      //
      // End of Bug #8309 Test Case
      //


    @Test
    public void TestBug9267() {
        // Test a long input of Lam Alef characters for CharsetRecog_IBM420_ar.
        // Bug 9267 was an array out of bounds problem in the unshaping code for these.
        byte [] input = new byte [7700]; 
        int i;
        for (i=0; i<input.length; i++) {
          input[i] = (byte)0xb2;
        }
        CharsetDetector det = new CharsetDetector();
        det.setText(input);
        det.detect();
    }
    
    @Test
    public void TestBug6954 () throws Exception {
        // Ticket 6954 - trouble with the haveC1Bytes flag that is used to distinguish between
        //  similar Windows and non-Windows SBCS encodings. State was kept in the shared
        //  Charset Recognizer objects, and could be overwritten.
        String sISO = "This is a small sample of some English text. Just enough to be sure that it detects correctly.";
        String sWindows = "This is another small sample of some English text. Just enough to be sure that it detects correctly."
                        + "It also includes some \u201CC1\u201D bytes.";

        byte[] bISO     = sISO.getBytes("ISO-8859-1");
        byte[] bWindows = sWindows.getBytes("windows-1252");

        // First do a plain vanilla detect of 1252 text

        CharsetDetector csd1 = new CharsetDetector();
        csd1.setText(bWindows);
        CharsetMatch match1 = csd1.detect();
        String name1 = match1.getName();
        assertEquals("Initial detection of charset", "windows-1252", name1);

        // Next, using a completely separate detector, detect some 8859-1 text

        CharsetDetector csd2 = new CharsetDetector();
        csd2.setText(bISO);
        CharsetMatch match2 = csd2.detect();
        String name2 = match2.getName();
        assertEquals("Initial use of second detector", "ISO-8859-1", name2);
        
        // Recheck the 1252 results from the first detector, which should not have been
        //  altered by the use of a different detector.

        name1 = match1.getName();
        assertEquals("Wrong charset name after running a second charset detector", "windows-1252", name1);
    }
    
    @Test
    public void TestBug6889() {
        // Verify that CharsetDetector.detectAll() does not return the same encoding multiple times.
        String text =
            "This is a small sample of some English text. Just enough to be sure that it detects correctly.";
        byte[] textBytes;
        try {
            textBytes = text.getBytes("ISO-8859-1");
        }
        catch (Exception e) {
            fail("Unexpected exception " + e.toString());
            return;
        }
        
        CharsetDetector det = new CharsetDetector();
        det.setText(textBytes);
        CharsetMatch matches[] = det.detectAll();
        
        HashSet<String> detectedEncodings = new HashSet<String>();
        for (CharsetMatch m: matches) {
            assertTrue("Charset " + m.getName() + " encountered before",
                        detectedEncodings.add(m.getName()));
        }   
    }
    
    @Test
    public void TestMultithreaded() {
        String  s = "This is some random plain text to run charset detection on.";
        final byte [] bytes;
        try {
            bytes = s.getBytes("ISO-8859-1");
        }
        catch (Exception e) {
            fail("Unexpected exception " + e.toString());
            return;
        }
        
        class WorkerThread extends Thread {
            WorkerThread(int num) {
                n = num;
            }           
            private int n;            
            public void run() {
                // System.out.println("Thread " + n + " is running.");
                CharsetDetector det = new CharsetDetector();
                det.setText(bytes);                
                for (int i=0; i<10000; i++) {
                    CharsetMatch matches[] = det.detectAll();
                    for (CharsetMatch m: matches) {
                        assertNotNull("Failure in thread " + n, m);
                    }
                }
                // System.out.println("Thread " + n + " is finished.");
            }
        }
        
        Thread threads[] = new Thread[10];
        for (int i=0; i<10; i++) {
            threads[i] = new WorkerThread(i);
            threads[i].start();
        }
        for (Thread thread: threads) {
            try {
                thread.join();
            } catch(Exception e) {
                fail("Unexpected exception " +  e.toString());
                return;
            }
        }
    }

      
}
