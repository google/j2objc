/*
 * Copyright (C) 2007 The Android Open Source Project
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
package org.apache.harmony.tests.javax.xml.parsers;

import dalvik.annotation.KnownFailure;

import junit.framework.TestCase;

import org.apache.harmony.tests.javax.xml.parsers.SAXParserTestSupport.MyDefaultHandler;
import org.apache.harmony.tests.javax.xml.parsers.SAXParserTestSupport.MyHandler;
import org.apache.harmony.tests.org.xml.sax.support.BrokenInputStream;
import org.apache.harmony.tests.org.xml.sax.support.MethodLogger;
import org.apache.harmony.tests.org.xml.sax.support.MockHandler;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import tests.support.resource.Support_Resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

@SuppressWarnings("deprecation")
public class SAXParserTest extends TestCase {

    private class MockSAXParser extends SAXParser {
        public MockSAXParser() {
            super();
        }

        /*
         * @see javax.xml.parsers.SAXParser#getParser()
         */
        @Override
        public Parser getParser() throws SAXException {
            // it is a fake
            return null;
        }

        /*
         * @see javax.xml.parsers.SAXParser#getProperty(java.lang.String)
         */
        @Override
        public Object getProperty(String name) throws SAXNotRecognizedException,
                SAXNotSupportedException {
            // it is a fake
            return null;
        }

        /*
         * @see javax.xml.parsers.SAXParser#getXMLReader()
         */
        @Override
        public XMLReader getXMLReader() throws SAXException {
            // it is a fake
            return null;
        }

        /*
         * @see javax.xml.parsers.SAXParser#isNamespaceAware()
         */
        @Override
        public boolean isNamespaceAware() {
            // it is a fake
            return false;
        }

        /*
         * @see javax.xml.parsers.SAXParser#isValidating()
         */
        @Override
        public boolean isValidating() {
            // it is a fake
            return false;
        }

        /*
         * @see javax.xml.parsers.SAXParser#setProperty(java.lang.String,
         * java.lang.Object)
         */
        @Override
        public void setProperty(String name, Object value) throws
                SAXNotRecognizedException, SAXNotSupportedException {
            // it is a fake
        }
    }

    private static final String LEXICAL_HANDLER_PROPERTY
            = "http://xml.org/sax/properties/lexical-handler";

    SAXParserFactory spf;

    SAXParser parser;

    static HashMap<String, String> ns;

    static Vector<String> el;

    static HashMap<String, String> attr;

    SAXParserTestSupport sp = new SAXParserTestSupport();

    File [] list_wf;
    File [] list_nwf;
    File [] list_out_dh;
    File [] list_out_hb;

    boolean validating = false;

    private InputStream getResource(String name) {
        return this.getClass().getResourceAsStream(name);
    }

    public void initFiles() throws Exception {
        // we differntiate between a validating and a non validating parser
        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            validating = parser.isValidating();
        } catch (Exception e) {
            fail("could not obtain a SAXParser");
        }

        String tmpPath = System.getProperty("java.io.tmpdir");

        // nwf = not well formed, wf = well formed
        list_wf = new File[] {new File(tmpPath + "/" +
                SAXParserTestSupport.XML_WF + "staff.xml")};
        list_nwf = new File[] {new File(tmpPath + "/" +
                SAXParserTestSupport.XML_NWF + "staff.xml")};
        list_out_dh = new File[] {new File(tmpPath + "/" +
                SAXParserTestSupport.XML_WF_OUT_DH + "staff.out")};
        list_out_hb = new File[] {new File(tmpPath + "/" +
                SAXParserTestSupport.XML_WF_OUT_HB + "staff.out")};

        list_wf[0].deleteOnExit();
        list_nwf[0].deleteOnExit();
        list_out_hb[0].deleteOnExit();
        list_out_dh[0].deleteOnExit();


        Support_Resources.copyLocalFileto(list_wf[0],
                getResource(SAXParserTestSupport.XML_WF + "staff.xml"));
        Support_Resources.copyLocalFileto(new File(
                tmpPath + "/" + SAXParserTestSupport.XML_WF + "staff.dtd"),
                getResource(SAXParserTestSupport.XML_WF + "staff.dtd"));

        Support_Resources.copyLocalFileto(list_nwf[0],
                getResource(SAXParserTestSupport.XML_NWF + "staff.xml"));
        Support_Resources.copyLocalFileto(new File(
                tmpPath + "/" + SAXParserTestSupport.XML_NWF + "staff.dtd"),
                getResource(SAXParserTestSupport.XML_NWF + "staff.dtd"));

        Support_Resources.copyLocalFileto(list_out_dh[0],
                getResource(SAXParserTestSupport.XML_WF_OUT_DH + "staff.out"));
        Support_Resources.copyLocalFileto(list_out_hb[0],
                getResource(SAXParserTestSupport.XML_WF_OUT_HB + "staff.out"));
    }

    @Override
    protected void setUp() throws Exception {
        spf = SAXParserFactory.newInstance();
        parser = spf.newSAXParser();
        assertNotNull(parser);

        ns = new HashMap<String, String>();
        attr = new HashMap<String, String>();
        el = new Vector<String>();
        initFiles();
    }

    @Override
    protected void tearDown() throws Exception {
    }

//    public static void main(String[] args) throws Exception {
//        SAXParserTest st = new SAXParserTest();
//        st.setUp();
//        st.generateDataFromReferenceImpl();
//
//    }
//
//    private void generateDataFromReferenceImpl() {
//        try {
//            for(int i = 0; i < list_wf.length; i++) {
//                MyDefaultHandler dh = new MyDefaultHandler();
//                InputStream is = new FileInputStream(list_wf[i]);
//                parser.parse(is, dh, ParsingSupport.XML_SYSTEM_ID);
//                HashMap refHm = dh.createData();
//
//                StringBuilder sb = new StringBuilder();
//                for (int j = 0; j < ParsingSupport.KEYS.length; j++) {
//                    String key = ParsingSupport.KEYS[j];
//                    sb.append(refHm.get(key)).append(
//                            ParsingSupport.SEPARATOR_DATA);
//                }
//                FileWriter fw = new FileWriter("/tmp/build_dh"+i+".out");
//                fw.append(sb.toString());
//                fw.close();
//            }
//
//            for(int i = 0; i < list_nwf.length; i++) {
//                MyHandler hb = new MyHandler();
//                InputStream is = new FileInputStream(list_wf[i]);
//                parser.parse(is, hb, ParsingSupport.XML_SYSTEM_ID);
//                HashMap refHm = hb.createData();
//
//                StringBuilder sb = new StringBuilder();
//                for (int j = 0; j < ParsingSupport.KEYS.length; j++) {
//                    String key = ParsingSupport.KEYS[j];
//                    sb.append(refHm.get(key)).append(
//                            ParsingSupport.SEPARATOR_DATA);
//                }
//                FileWriter fw = new FileWriter("/tmp/build_hb"+i+".out");
//                fw.append(sb.toString());
//                fw.close();
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void testSAXParser() {
        try {
            new MockSAXParser();
        } catch (Exception e) {
            fail("unexpected exception " + e.toString());
        }
    }

    /**
     * javax.xml.parser.SAXParser#getSchema().
     * TODO getSchema() IS NOT SUPPORTED
     */
    /*   public void test_getSchema() {
        assertNull(parser.getSchema());
        SchemaFactory sf =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            Schema schema = sf.newSchema();
            spf.setSchema(schema);
            assertNotNull(spf.newSAXParser().getSchema());
        } catch (ParserConfigurationException pce) {
            fail("Unexpected ParserConfigurationException " + pce.toString());
        } catch (SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
    }
     */

    public void testIsNamespaceAware() {
        try {
            spf.setNamespaceAware(true);
            assertTrue(spf.newSAXParser().isNamespaceAware());
            spf.setNamespaceAware(false);
            assertFalse(spf.newSAXParser().isNamespaceAware());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testIsValidating() {
        try {
            spf.setValidating(false);
            assertFalse(spf.newSAXParser().isValidating());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testIsXIncludeAware() {
        try {
            spf.setXIncludeAware(false);
            assertFalse(spf.newSAXParser().isXIncludeAware());
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void test_parseLjava_io_FileLorg_xml_sax_helpers_DefaultHandler() throws Exception {

        for(int i = 0; i < list_wf.length; i++) {
            HashMap<String, String> hm =
                new SAXParserTestSupport().readFile(list_out_dh[i].getPath());
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse(list_wf[i], dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }

        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyDefaultHandler dh = new MyDefaultHandler();
                parser.parse(list_nwf[i], dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
                //expected
            }
        }

        try {
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse((File) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
            //expected
        }

        try {
            parser.parse(list_wf[0], (DefaultHandler) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        }
    }

    public void testParseFileHandlerBase() {
        for(int i = 0; i < list_wf.length; i++) {
            try {
                HashMap<String, String> hm = sp.readFile(
                        list_out_hb[i].getPath());
                MyHandler dh = new MyHandler();
                parser.parse(list_wf[i], dh);
                assertTrue(SAXParserTestSupport.equalsMaps(hm,
                        dh.createData()));
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            } catch (SAXException sax) {
                fail("Unexpected SAXException " + sax.toString());
            }
        }

        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyHandler dh = new MyHandler();
                parser.parse(list_nwf[i], dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
                //expected
            } catch (FileNotFoundException fne) {
                fail("Unexpected FileNotFoundException " + fne.toString());
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            }
        }

        try {
            MyHandler dh = new MyHandler();
            parser.parse((File) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
            //expected
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }

        try {
            parser.parse(list_wf[0], (HandlerBase) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        } catch (FileNotFoundException fne) {
            fail("Unexpected FileNotFoundException " + fne.toString());
        } catch(IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
    }

    public void test_parseLorg_xml_sax_InputSourceLorg_xml_sax_helpers_DefaultHandler()
            throws Exception {
        for(int i = 0; i < list_wf.length; i++) {
            HashMap<String, String> hm = new SAXParserTestSupport().readFile(
                    list_out_dh[i].getPath());
            MyDefaultHandler dh = new MyDefaultHandler();
            InputSource is = new InputSource(new FileInputStream(list_wf[i]));
            parser.parse(is, dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }

        for (File file : list_nwf) {
            try {
                MyDefaultHandler dh = new MyDefaultHandler();
                InputSource is = new InputSource(new FileInputStream(file));
                parser.parse(is, dh);
                fail("SAXException is not thrown");
            } catch (SAXException expected) {
            }
        }

        try {
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse((InputSource) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch (IllegalArgumentException expected) {
        }

        InputSource is = new InputSource(new FileInputStream(list_wf[0]));
        parser.parse(is, (DefaultHandler) null);

        InputStream in = null;
        try {
            in = new BrokenInputStream(new FileInputStream(list_wf[0]), 10);
            is = new InputSource(in);
            parser.parse(is, (DefaultHandler) null);
            fail("IOException expected");
        } catch(IOException expected) {
        } finally {
            in.close();
        }
    }

    public void testParseInputSourceHandlerBase() throws Exception {
        for(int i = 0; i < list_wf.length; i++) {
            HashMap<String, String> hm = sp.readFile(list_out_hb[i].getPath());
            MyHandler dh = new MyHandler();
            InputSource is = new InputSource(new FileInputStream(list_wf[i]));
            parser.parse(is, dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }

        for (File file : list_nwf) {
            try {
                MyHandler dh = new MyHandler();
                InputSource is = new InputSource(new FileInputStream(file));
                parser.parse(is, dh);
                fail("SAXException is not thrown");
            } catch (SAXException expected) {
            }
        }

        try {
            MyHandler dh = new MyHandler();
            parser.parse((InputSource) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(IllegalArgumentException expected) {
        }

        InputSource is = new InputSource(new FileInputStream(list_wf[0]));
        parser.parse(is, (HandlerBase) null);

        // Reader case
        is = new InputSource(new InputStreamReader(new FileInputStream(list_wf[0])));
        parser.parse(is, (HandlerBase) null);

        // SystemID case
        is = new InputSource(list_wf[0].toURI().toString());
        parser.parse(is, (HandlerBase) null);

        // Inject IOException
        InputStream in = null;
        try {
            in = new BrokenInputStream(new FileInputStream(list_wf[0]), 10);
            parser.parse(in, (HandlerBase) null, SAXParserTestSupport.XML_SYSTEM_ID);
            fail("IOException expected");
        } catch(IOException expected) {
        } finally {
            in.close();
        }
    }

    public void test_parseLjava_io_InputStreamLorg_xml_sax_helpers_DefaultHandler() throws Exception {

        for(int i = 0; i < list_wf.length; i++) {

            HashMap<String, String> hm = new SAXParserTestSupport().readFile(
                    list_out_dh[i].getPath());
            MyDefaultHandler dh = new MyDefaultHandler();
            InputStream is = new FileInputStream(list_wf[i]);
            parser.parse(is, dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }

        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyDefaultHandler dh = new MyDefaultHandler();
                InputStream is = new FileInputStream(list_nwf[i]);
                parser.parse(is, dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
                //expected
            }
        }

        try {
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse((InputStream) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
            //expected
        }

        try {
            InputStream is = new FileInputStream(list_wf[0]);
            parser.parse(is, (DefaultHandler) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        }
    }

    @KnownFailure("We supply optional qnames, but this test doesn't expect them")
    public void test_parseLjava_io_InputStreamLorg_xml_sax_helpers_DefaultHandlerLjava_lang_String() {
        for(int i = 0; i < list_wf.length; i++) {
            try {
                HashMap<String, String> hm = sp.readFile(
                        list_out_hb[i].getPath());
                MyDefaultHandler dh = new MyDefaultHandler();
                InputStream is = new FileInputStream(list_wf[i]);
                parser.parse(is, dh, SAXParserTestSupport.XML_SYSTEM_ID);
                assertEquals(hm, dh.createData());
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            } catch (SAXException sax) {
                fail("Unexpected SAXException " + sax.toString());
            }
        }

        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyDefaultHandler dh = new MyDefaultHandler();
                InputStream is = new FileInputStream(list_nwf[i]);
                parser.parse(is, dh, SAXParserTestSupport.XML_SYSTEM_ID);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
                //expected
            } catch (FileNotFoundException fne) {
                fail("Unexpected FileNotFoundException " + fne.toString());
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            }
        }

        try {
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse((InputStream) null, dh,
                    SAXParserTestSupport.XML_SYSTEM_ID);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
            //expected
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }

        try {
            InputStream is = new FileInputStream(list_wf[0]);
            parser.parse(is, (DefaultHandler) null,
                    SAXParserTestSupport.XML_SYSTEM_ID);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        } catch (FileNotFoundException fne) {
            fail("Unexpected FileNotFoundException " + fne.toString());
        } catch(IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
//
//        for(int i = 0; i < list_wf.length; i++) {
//
//            HashMap<String, String> hm = new SAXParserTestSupport().readFile(
//                    list_out_dh[i].getPath());
//            MyDefaultHandler dh = new MyDefaultHandler();
//            InputStream is = new FileInputStream(list_wf[i]);
//            parser.parse(is, dh, SAXParserTestSupport.XML_SYSTEM_ID);
//            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
//        }
//
//        for(int i = 0; i < list_nwf.length; i++) {
//            try {
//                MyDefaultHandler dh = new MyDefaultHandler();
//                InputStream is = new FileInputStream(list_nwf[i]);
//                parser.parse(is, dh, SAXParserTestSupport.XML_SYSTEM_ID);
//                fail("SAXException is not thrown");
//            } catch(org.xml.sax.SAXException se) {
//                //expected
//            }
//        }
//
//        try {
//            MyDefaultHandler dh = new MyDefaultHandler();
//            parser.parse((InputStream) null, dh,
//                    SAXParserTestSupport.XML_SYSTEM_ID);
//            fail("java.lang.IllegalArgumentException is not thrown");
//        } catch(java.lang.IllegalArgumentException iae) {
//            //expected
//        }
//
//        try {
//            InputStream is = new FileInputStream(list_wf[0]);
//            parser.parse(is, (DefaultHandler) null,
//                    SAXParserTestSupport.XML_SYSTEM_ID);
//        } catch(java.lang.IllegalArgumentException iae) {
//            fail("java.lang.IllegalArgumentException is thrown");
//        }
//
//        // TODO commented out since our parser is nonvalidating and thus never
//        // tries to load staff.dtd in "/" ... and therefore never can fail with
//        // an IOException
//        /*try {
//            MyDefaultHandler dh = new MyDefaultHandler();
//            InputStream is = new FileInputStream(list_wf[0]);
//            parser.parse(is, dh, "/");
//            fail("Expected IOException was not thrown");
//        } catch(IOException ioe) {
//            // expected
//        }*/
    }

    public void testParseInputStreamHandlerBase() throws Exception {
        for(int i = 0; i < list_wf.length; i++) {
            HashMap<String, String> hm = sp.readFile(list_out_hb[i].getPath());
            MyHandler dh = new MyHandler();
            InputStream is = new FileInputStream(list_wf[i]);
            parser.parse(is, dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }

        for (File file : list_nwf) {
            try {
                MyHandler dh = new MyHandler();
                InputStream is = new FileInputStream(file);
                parser.parse(is, dh);
                fail("SAXException is not thrown");
            } catch (SAXException expected) {
            }
        }

        try {
            MyHandler dh = new MyHandler();
            parser.parse((InputStream) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch (IllegalArgumentException expected) {
        }

        InputStream is = new FileInputStream(list_wf[0]);
        parser.parse(is, (HandlerBase) null);

        // Inject IOException
        try {
            is = new BrokenInputStream(new FileInputStream(list_wf[0]), 10);
            parser.parse(is, (HandlerBase) null);
            fail("IOException expected");
        } catch(IOException e) {
            // Expected
        } finally {
            is.close();
        }
    }

    public void testParseInputStreamHandlerBaseString() throws Exception {
        for(int i = 0; i < list_wf.length; i++) {
            HashMap<String, String> hm = sp.readFile(list_out_hb[i].getPath());
            MyHandler dh = new MyHandler();
            InputStream is = new FileInputStream(list_wf[i]);
            parser.parse(is, dh, SAXParserTestSupport.XML_SYSTEM_ID);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }

        for (File file : list_nwf) {
            try {
                MyHandler dh = new MyHandler();
                InputStream is = new FileInputStream(file);
                parser.parse(is, dh, SAXParserTestSupport.XML_SYSTEM_ID);
                fail("SAXException is not thrown");
            } catch (SAXException expected) {
            }
        }

        try {
            MyHandler dh = new MyHandler();
            parser.parse(null, dh, SAXParserTestSupport.XML_SYSTEM_ID);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(IllegalArgumentException expected) {
        }

        InputStream is = new FileInputStream(list_wf[0]);
        parser.parse(is, (HandlerBase) null, SAXParserTestSupport.XML_SYSTEM_ID);

        // Inject IOException
        try {
            is = new BrokenInputStream(new FileInputStream(list_wf[0]), 10);
            parser.parse(is, (HandlerBase) null, SAXParserTestSupport.XML_SYSTEM_ID);
            fail("IOException expected");
        } catch(IOException expected) {
        } finally {
            is.close();
        }
    }

    public void test_parseLjava_lang_StringLorg_xml_sax_helpers_DefaultHandler() throws Exception {

        for(int i = 0; i < list_wf.length; i++) {

            HashMap<String, String> hm = new SAXParserTestSupport().readFile(
                    list_out_dh[i].getPath());
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse(list_wf[i].toURI().toString(), dh);
            assertTrue(SAXParserTestSupport.equalsMaps(hm, dh.createData()));
        }

        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyDefaultHandler dh = new MyDefaultHandler();
                parser.parse(list_nwf[i].toURI().toString(), dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
                //expected
            }
        }

        try {
            MyDefaultHandler dh = new MyDefaultHandler();
            parser.parse((String) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
            //expected
        }

        try {
            parser.parse(list_wf[0].toURI().toString(), (DefaultHandler) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        }
    }

    public void testParseStringHandlerBase() {
        for(int i = 0; i < list_wf.length; i++) {
            try {
                HashMap<String, String> hm = sp.readFile(
                        list_out_hb[i].getPath());
                MyHandler dh = new MyHandler();
                parser.parse(list_wf[i].toURI().toString(), dh);
                assertTrue(SAXParserTestSupport.equalsMaps(hm,
                        dh.createData()));
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            } catch (SAXException sax) {
                fail("Unexpected SAXException " + sax.toString());
            }
        }

        for(int i = 0; i < list_nwf.length; i++) {
            try {
                MyHandler dh = new MyHandler();
                parser.parse(list_nwf[i].toURI().toString(), dh);
                fail("SAXException is not thrown");
            } catch(org.xml.sax.SAXException se) {
                //expected
            } catch (FileNotFoundException fne) {
                fail("Unexpected FileNotFoundException " + fne.toString());
            } catch (IOException ioe) {
                fail("Unexpected IOException " + ioe.toString());
            }
        }

        try {
            MyHandler dh = new MyHandler();
            parser.parse((String) null, dh);
            fail("java.lang.IllegalArgumentException is not thrown");
        } catch(java.lang.IllegalArgumentException iae) {
            //expected
        } catch (IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }

        try {
            parser.parse(list_wf[0].toURI().toString(), (HandlerBase) null);
        } catch(java.lang.IllegalArgumentException iae) {
            fail("java.lang.IllegalArgumentException is thrown");
        } catch (FileNotFoundException fne) {
            fail("Unexpected FileNotFoundException " + fne.toString());
        } catch(IOException ioe) {
            fail("Unexpected IOException " + ioe.toString());
        } catch(SAXException sax) {
            fail("Unexpected SAXException " + sax.toString());
        }
    }

    public void testReset() {
        try {
            spf = SAXParserFactory.newInstance();
            parser = spf.newSAXParser();

            parser.setProperty(LEXICAL_HANDLER_PROPERTY, new MockHandler(new MethodLogger()));
            parser.reset();
            assertEquals(null, parser.getProperty(LEXICAL_HANDLER_PROPERTY));
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testGetParser() {
        spf = SAXParserFactory.newInstance();
        try {
            Parser parser = spf.newSAXParser().getParser();
            assertNotNull(parser);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testGetReader() {
        spf = SAXParserFactory.newInstance();
        try {
            XMLReader reader = spf.newSAXParser().getXMLReader();
            assertNotNull(reader);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    public void testSetGetProperty() {
        // Ordinary case
        String validName = "http://xml.org/sax/properties/lexical-handler";
        LexicalHandler validValue = new MockHandler(new MethodLogger());

        try {
            SAXParser parser = spf.newSAXParser();
            parser.setProperty(validName, validValue);
            assertEquals(validValue, parser.getProperty(validName));

            parser.setProperty(validName, null);
            assertEquals(null, parser.getProperty(validName));
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        // Unsupported property
        try {
            SAXParser parser = spf.newSAXParser();
            parser.setProperty("foo", "bar");
            fail("SAXNotRecognizedException expected");
        } catch (SAXNotRecognizedException e) {
            // Expected
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        try {
            SAXParser parser = spf.newSAXParser();
            parser.getProperty("foo");
            fail("SAXNotRecognizedException expected");
        } catch (SAXNotRecognizedException e) {
            // Expected
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        // No name case
        try {
            SAXParser parser = spf.newSAXParser();
            parser.setProperty(null, "bar");
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }

        try {
            SAXParser parser = spf.newSAXParser();
            parser.getProperty(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
            // Expected
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

}
