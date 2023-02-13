/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package org.apache.harmony.tests.javax.xml.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Support for SAXParserTest. Shares the element keys used in the golden files.
 * Compares the result of the parser with golden data.
 * Contains the handler classes used to track the output of the parser.
 */
class SAXParserTestSupport {

    public static final char SEPARATOR_ELEMENT = '^';
    public static final char SEPARATOR_STRING = '$';
    public static final char SEPARATOR_DATA = '#';

    public static final String XML_WF = "/wf/";
    public static final String XML_NWF = "/nwf/";

    public static final String XML_WF_OUT_DH = "/out_dh/";
    public static final String XML_WF_OUT_HB = "/out_hb/";

    public static final String XML_SYSTEM_ID = "." + "/systemid/";

    public static final String KEY_IS_START_DOC = "isEndDocument";
    public static final String KEY_IS_END_DOC = "isStartDocument";
    public static final String KEY_TEXT = "text";
    public static final String KEY_ERROR = "error";
    public static final String KEY_FATAL_ERROR = "fatalError";
    public static final String KEY_WARNING = "warning";
    public static final String KEY_END_ELEMENT = "endElement";
    public static final String KEY_END_PREFIX_MAPPING = "endPrefixMapping";
    public static final String KEY_IGNORABLE_WHITE_SPACE =
        "ignorableWhitespace";
    public static final String KEY_NOTATION_DECL = "notationDecl";
    public static final String KEY_PROCESSING_INSTRUCTION =
        "processingInstruction";
    public static final String KEY_RESOLVE_ENTITY = "resolveEntity";
    public static final String KEY_DOCUMENT_LOCATORS = "documentLocators";
    public static final String KEY_SKIPPED_ENTITY = "skippedEntity";
    public static final String KEY_START_ELEMENT = "startElement";
    public static final String KEY_START_PREFIX_MAPPING = "startPrefixMapping";
    public static final String KEY_UNPARSED_ENTITY_DECL = "unparsedEntityDecl";

    static String [] KEYS = {KEY_IS_START_DOC, KEY_IS_END_DOC, KEY_TEXT,
            KEY_ERROR, KEY_FATAL_ERROR, KEY_WARNING, KEY_END_ELEMENT,
            KEY_END_PREFIX_MAPPING, KEY_PROCESSING_INSTRUCTION,
            KEY_SKIPPED_ENTITY, KEY_START_ELEMENT,
            KEY_START_PREFIX_MAPPING};

    static {
        String tmp = System.getProperty("java.io.tmpdir", ".");

        new File(tmp).mkdirs();
        new File(tmp, XML_WF).mkdirs();
        new File(tmp, XML_NWF).mkdirs();
        new File(tmp, XML_WF_OUT_DH).mkdirs();
        new File(tmp, XML_WF_OUT_HB).mkdirs();
    }

    /**
     * Initialize the SAXParserTest reference by filling in the data from the
     * file passed to the method. This will be the reference to compare
     * against with the output of the parser.
     */
    public HashMap<String, String> readFile(String fileName) {
        HashMap<String, String> storage = new HashMap<String, String>();
        try {

            InputStream is = new FileInputStream(fileName);

            int c = is.read();

            StringBuffer str = new StringBuffer();
            int i = 0;
            while(c != -1) {
                if((char)c == SEPARATOR_DATA) {
                  //  if(str.length() > 0) {
                        if(i < KEYS.length) {
                            storage.put(KEYS[i], str.toString());
                        //    System.out.println(str.toString());
                            str.setLength(0);
                            i++;
                        }
                  //  }
                } else {
                    str.append((char)c);
                }
                try {
                    c = is.read();
                } catch (Exception e) {
                    c = -1;
                }
            }
            try {
                is.close();
            } catch (IOException e) {
            }

        } catch(IOException ioe) {
            System.out.println("IOException during processing the file: "
                    + fileName);
        }
        return storage;
    }

    /**
     * Compares the content of two HashMaps. One map should be the reference
     * containing the correct string for each xml document element and the other
     * should contain the elements filled with output from the parser.
     *
     * @param original the reference
     * @param result the result of the parser
     * @return true if they're equal.
     */
    public static boolean equalsMaps(HashMap<String, String> original,
            HashMap<String, String> result) {

        if(original == null && result == null) {
            return true;
        } else {
            if(original.size() != result.size()) return false;

            for(int i = 0; i < KEYS.length; i++) {
                if(!original.get(KEYS[i]).equals(result.get(KEYS[i]))) {
                    System.out.println("for "+KEYS[i]+": original:" +
                            original.get(KEYS[i]));
                    System.out.println();
                    System.out.println("  result:" + result.get(KEYS[i]));
                    System.out.println();
                    return false;
                }
            }
            return true;
        }
    }

    static class MyDefaultHandler extends DefaultHandler {

        public StringBuffer data_isEndDocument = new StringBuffer();
        public StringBuffer data_isStartDocument = new StringBuffer();
        public StringBuffer data_text = new StringBuffer();
        public StringBuffer data_error = new StringBuffer();
        public StringBuffer data_fatalError = new StringBuffer();
        public StringBuffer data_warning = new StringBuffer();
        public StringBuffer data_endElement = new StringBuffer();
        public StringBuffer data_endPrefixMapping = new StringBuffer();
        public StringBuffer data_processingInstruction = new StringBuffer();
        public StringBuffer data_skippedEntity = new StringBuffer();
        public StringBuffer data_startElement = new StringBuffer();
        public StringBuffer data_startPrefixMapping = new StringBuffer();

        public HashMap<String, String> createData() {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put(KEY_IS_END_DOC, data_isEndDocument.toString());
            hm.put(KEY_IS_START_DOC, data_isStartDocument.toString());
            hm.put(KEY_TEXT, data_text.toString());
            hm.put(KEY_ERROR, data_error.toString());
            hm.put(KEY_FATAL_ERROR, data_fatalError.toString());
            hm.put(KEY_WARNING, data_warning.toString());
            hm.put(KEY_END_ELEMENT, data_endElement.toString());
            hm.put(KEY_END_PREFIX_MAPPING, data_endPrefixMapping.toString());

            hm.put(KEY_PROCESSING_INSTRUCTION,
                    data_processingInstruction.toString());
            hm.put(KEY_SKIPPED_ENTITY, data_skippedEntity.toString());
            hm.put(KEY_START_ELEMENT, data_startElement.toString());
            hm.put(KEY_START_PREFIX_MAPPING,
                    data_startPrefixMapping.toString());
            return hm;
        }

        public void printMap() {
            System.out.print(data_isStartDocument.toString() + SEPARATOR_DATA +
                    data_isEndDocument.toString() + SEPARATOR_DATA +
                    data_text.toString() + SEPARATOR_DATA +
                    data_error.toString()+ SEPARATOR_DATA +
                    data_fatalError.toString()+ SEPARATOR_DATA +
                    data_warning.toString()+ SEPARATOR_DATA +
                    data_endElement.toString() + SEPARATOR_DATA+
                    data_endPrefixMapping.toString()+ SEPARATOR_DATA +
                    data_processingInstruction.toString() + SEPARATOR_DATA +
                    data_skippedEntity.toString() +  SEPARATOR_DATA +
                    data_startElement.toString() + SEPARATOR_DATA +
                    data_startPrefixMapping.toString()+ SEPARATOR_DATA);
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            String str = new String(ch, start, length);
            data_text.append(str);
            // different sax parsers are allowed to handle chunking differently,
            // therefore we cannot rely on identical chunks being delivered.
            //data_text.append(ParsingSupport.SEPARATOR_ELEMENT);
        }

        @Override
        public void endDocument() {
            data_isEndDocument.append(true);
            data_isEndDocument.append(SEPARATOR_ELEMENT);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            StringBuffer sb = new StringBuffer();
            sb.append(uri);
            sb.append(SEPARATOR_STRING);
            sb.append(localName);
            sb.append(SEPARATOR_STRING);
            sb.append(qName);
            data_endElement.append(sb);
            data_endElement.append(SEPARATOR_ELEMENT);
        }

        @Override
        public void endPrefixMapping(String prefix) {
            data_endPrefixMapping.append(prefix);
            data_endPrefixMapping.append(SEPARATOR_ELEMENT);
        }

        @Override
        public void error(SAXParseException e) {
            data_error.append(e);
            data_error.append(SEPARATOR_ELEMENT);
        }

        @Override
        public void fatalError(SAXParseException e) {
            data_fatalError.append(e);
            data_fatalError.append(SEPARATOR_ELEMENT);
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) {
            /*    String s = new String(ch, start, length);
            ignorableWhitespace.append(s);
            ignorableWhitespace.append(ParsingSupport.SEPARATOR_ELEMENT);*/
        }

        @Override
        public void notationDecl(String name, String publicId,
                String systemId) {
            /* data_notationDecl.append(name + ParsingSupport.SEPARATOR_STRING +
                              publicId + ParsingSupport.SEPARATOR_STRING +
                              systemId + ParsingSupport.SEPARATOR_STRING);
            data_notationDecl.append(ParsingSupport.SEPARATOR_ELEMENT);*/
        }

        @Override
        public void processingInstruction(String target, String data) {
            data_processingInstruction.append(target + SEPARATOR_STRING + data);
            data_processingInstruction.append(SEPARATOR_ELEMENT);
        }

        @Override
        public InputSource    resolveEntity(String publicId, String systemId) {
            // data_resolveEntity.append(publicId +
            //            ParsingSupport.SEPARATOR_STRING + systemId);
            // data_resolveEntity.append(ParsingSupport.SEPARATOR_ELEMENT);
            return null;
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            //       data_documentLocators.append(locator);
            // data_documentLocators.append(ParsingSupport.SEPARATOR_ELEMENT);
        }

        @Override
        public void skippedEntity(String name) {
            data_skippedEntity.append(name);
            data_skippedEntity.append(SEPARATOR_ELEMENT);
        }

        @Override
        public void startDocument() {
            data_isStartDocument.append(true);
            data_isStartDocument.append(SEPARATOR_ELEMENT);
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) {
            data_startElement.append(uri);
            data_startElement.append(SEPARATOR_STRING);
            data_startElement.append(localName);
            data_startElement.append(SEPARATOR_STRING);
            data_startElement.append(qName);

            for(int i = 0; i < attributes.getLength(); i ++)
                data_startElement.append(
                        SEPARATOR_STRING +attributes.getQName(i) +
                        SEPARATOR_STRING + attributes.getValue(i));

            data_isStartDocument.append(SEPARATOR_ELEMENT);
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) {
            data_startPrefixMapping.append(prefix + SEPARATOR_STRING + uri);
        }

        @Override
        public void unparsedEntityDecl(String name, String publicId,
                String systemId, String notationName) {
            // data_unparsedEntityDecl.append(name
            //     + ParsingSupport.SEPARATOR_STRING + publicId
            //     + ParsingSupport.SEPARATOR_STRING
            //     + systemId + ParsingSupport.SEPARATOR_STRING + notationName);
        }

        @Override
        public void warning(SAXParseException e) {
            data_warning.append(e);
        }
    }

    @SuppressWarnings("deprecation")
    static class MyHandler extends HandlerBase {

        public StringBuffer data_isEndDocument = new StringBuffer();
        public StringBuffer data_isStartDocument = new StringBuffer();
        public StringBuffer data_text = new StringBuffer();
        public StringBuffer data_error = new StringBuffer();
        public StringBuffer data_fatalError = new StringBuffer();
        public StringBuffer data_warning = new StringBuffer();
        public StringBuffer data_endElement = new StringBuffer();
        public StringBuffer data_endPrefixMapping = new StringBuffer();
        public StringBuffer data_processingInstruction = new StringBuffer();
        public StringBuffer data_skippedEntity = new StringBuffer();
        public StringBuffer data_startElement = new StringBuffer();
        public StringBuffer data_startPrefixMapping = new StringBuffer();

        public void printMap() {
            System.out.print(data_isStartDocument.toString() + SEPARATOR_DATA +
                    data_isEndDocument.toString() + SEPARATOR_DATA +
                    data_text.toString() + SEPARATOR_DATA +
                    data_error.toString()+ SEPARATOR_DATA +
                    data_fatalError.toString()+ SEPARATOR_DATA +
                    data_warning.toString()+ SEPARATOR_DATA +
                    data_endElement.toString() + SEPARATOR_DATA+
                    data_endPrefixMapping.toString()+ SEPARATOR_DATA +
                    data_processingInstruction.toString() + SEPARATOR_DATA +
                    data_skippedEntity.toString() +  SEPARATOR_DATA +
                    data_startElement.toString() + SEPARATOR_DATA +
                    data_startPrefixMapping.toString()+ SEPARATOR_DATA);
        }

        public HashMap<String, String> createData() {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put(KEY_IS_END_DOC, data_isEndDocument.toString());
            hm.put(KEY_IS_START_DOC, data_isStartDocument.toString());
            hm.put(KEY_TEXT, data_text.toString());
            hm.put(KEY_ERROR, data_error.toString());
            hm.put(KEY_FATAL_ERROR, data_fatalError.toString());
            hm.put(KEY_WARNING, data_warning.toString());
            hm.put(KEY_END_ELEMENT, data_endElement.toString());
            hm.put(KEY_END_PREFIX_MAPPING, data_endPrefixMapping.toString());
            hm.put(KEY_PROCESSING_INSTRUCTION,
                    data_processingInstruction.toString());
            hm.put(KEY_SKIPPED_ENTITY, data_skippedEntity.toString());
            hm.put(KEY_START_ELEMENT, data_startElement.toString());
            hm.put(KEY_START_PREFIX_MAPPING,
                    data_startPrefixMapping.toString());
            return hm;
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            String str = new String(ch, start, length);
            data_text.append(str);
            // different sax parsers are allowed to handle chunking differently,
            // therefore we cannot rely on identical chunks being delivered.
            //data_text.append(ParsingSupport.SEPARATOR_ELEMENT);
        }

        @Override
        public void    endDocument() {
            data_isEndDocument.append(true);
            data_isEndDocument.append(SEPARATOR_ELEMENT);
        }

        public void endElement(String uri, String localName, String qName) {
            StringBuffer sb = new StringBuffer();
            sb.append(uri);
            sb.append(SEPARATOR_STRING);
            sb.append(localName);
            sb.append(SEPARATOR_STRING);
            sb.append(qName);
            data_endElement.append(sb);
            data_endElement.append(SEPARATOR_ELEMENT);
        }

        @Override
        public void error(SAXParseException e) {
            data_error.append(e);
            data_error.append(SEPARATOR_ELEMENT);
        }

        @Override
        public void fatalError(SAXParseException e) {
            data_fatalError.append(e);
            data_fatalError.append(SEPARATOR_ELEMENT);
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) {

        }

        @Override
        public void notationDecl(String name, String publicId,
                String systemId) {

        }

        @Override
        public void processingInstruction(String target, String data) {
            data_processingInstruction.append(target + SEPARATOR_STRING + data);
            data_processingInstruction.append(SEPARATOR_ELEMENT);
        }

        @Override
        public InputSource    resolveEntity(String publicId, String systemId) {
            return null;
        }

        @Override
        public void setDocumentLocator(Locator locator) {

        }

        @Override
        public void startDocument() {
            data_isStartDocument.append(true);
            data_isStartDocument.append(SEPARATOR_ELEMENT);
        }

        public void startElement(String uri, String localName, String qName,
                Attributes attributes) {
            data_startElement.append(uri);
            data_startElement.append(SEPARATOR_STRING);
            data_startElement.append(localName);
            data_startElement.append(SEPARATOR_STRING);
            data_startElement.append(qName);

            for(int i = 0; i < attributes.getLength(); i ++)
                data_startElement.append(SEPARATOR_STRING
                        + attributes.getQName(i) +
                        SEPARATOR_STRING + attributes.getValue(i));

            data_isStartDocument.append(SEPARATOR_ELEMENT);
        }

        @Override
        public void unparsedEntityDecl(String name, String publicId,
                String systemId, String notationName) {

        }

        @Override
        public void warning(SAXParseException e) {
            data_warning.append(e);
        }
    }
}
