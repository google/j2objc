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

package org.apache.harmony.xml.parsers;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import javax.xml.parsers.DocumentBuilder;
import libcore.io.IoUtils;
import org.apache.harmony.xml.dom.CDATASectionImpl;
import org.apache.harmony.xml.dom.DOMImplementationImpl;
import org.apache.harmony.xml.dom.DocumentImpl;
import org.apache.harmony.xml.dom.DocumentTypeImpl;
import org.apache.harmony.xml.dom.TextImpl;
import org.kxml2.io.KXmlParser;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Builds a DOM using KXmlParser.
 */
class DocumentBuilderImpl extends DocumentBuilder {

    private static DOMImplementationImpl dom = DOMImplementationImpl.getInstance();

    private boolean coalescing;
    private EntityResolver entityResolver;
    private ErrorHandler errorHandler;
    private boolean ignoreComments;
    private boolean ignoreElementContentWhitespace;
    private boolean namespaceAware;
    // adding a new field? don't forget to update reset().

    @Override public void reset() {
        coalescing = false;
        entityResolver = null;
        errorHandler = null;
        ignoreComments = false;
        ignoreElementContentWhitespace = false;
        namespaceAware = false;
    }

    @Override
    public DOMImplementation getDOMImplementation() {
        return dom;
    }

    @Override
    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    @Override
    public boolean isValidating() {
        return false;
    }

    @Override
    public Document newDocument() {
        return dom.createDocument(null, null, null);
    }

    @Override
    public Document parse(InputSource source) throws SAXException, IOException {
        if (source == null) {
            throw new IllegalArgumentException("source == null");
        }

        String namespaceURI = null;
        String qualifiedName = null;
        DocumentType doctype = null;
        String inputEncoding = source.getEncoding();
        String systemId = source.getSystemId();
        DocumentImpl document = new DocumentImpl(
                dom, namespaceURI, qualifiedName, doctype, inputEncoding);
        document.setDocumentURI(systemId);

        KXmlParser parser = new KXmlParser();
        try {
            parser.keepNamespaceAttributes();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, namespaceAware);

            if (source.getByteStream() != null) {
                parser.setInput(source.getByteStream(), inputEncoding);
            } else if (source.getCharacterStream() != null) {
                parser.setInput(source.getCharacterStream());
            } else if (systemId != null) {
                URL url = new URL(systemId);
                URLConnection urlConnection = url.openConnection();
                urlConnection.connect();
                // TODO: if null, extract the inputEncoding from the Content-Type header?
                parser.setInput(urlConnection.getInputStream(), inputEncoding);
            } else {
                throw new SAXParseException("InputSource needs a stream, reader or URI", null);
            }

            if (parser.nextToken() == XmlPullParser.END_DOCUMENT) {
                throw new SAXParseException("Unexpected end of document", null);
            }

            parse(parser, document, document, XmlPullParser.END_DOCUMENT);

            parser.require(XmlPullParser.END_DOCUMENT, null, null);
        } catch (XmlPullParserException ex) {
            if (ex.getDetail() instanceof IOException) {
                throw (IOException) ex.getDetail();
            }
            if (ex.getDetail() instanceof RuntimeException) {
                throw (RuntimeException) ex.getDetail();
            }

            LocatorImpl locator = new LocatorImpl();

            locator.setPublicId(source.getPublicId());
            locator.setSystemId(systemId);
            locator.setLineNumber(ex.getLineNumber());
            locator.setColumnNumber(ex.getColumnNumber());

            SAXParseException newEx = new SAXParseException(ex.getMessage(), locator);

            if (errorHandler != null) {
                errorHandler.error(newEx);
            }

            throw newEx;
        } finally {
            IoUtils.closeQuietly(parser);
        }

        return document;
    }

    /**
     * Implements the whole parsing of the XML document. The XML pull parser is
     * actually more of a tokenizer, and we are doing a classical recursive
     * descent parsing (the method invokes itself for XML elements). Our
     * approach to parsing does accept some illegal documents (more than one
     * root element, for example). The assumption is that the DOM implementation
     * throws the proper exceptions in these cases.
     *
     * @param parser The XML pull parser we're reading from.
     * @param document The document we're building.
     * @param node The node we're currently on (initially the document itself).
     * @param endToken The token that will end this recursive call. Either
     *        XmlPullParser.END_DOCUMENT or XmlPullParser.END_TAG.
     *
     * @throws XmlPullParserException If a parsing error occurs.
     * @throws IOException If a general IO error occurs.
     */
    private void parse(KXmlParser parser, DocumentImpl document, Node node,
            int endToken) throws XmlPullParserException, IOException {

        int token = parser.getEventType();

        /*
         * The main parsing loop. The precondition is that we are already on the
         * token to be processed. This holds for each iteration of the loop, so
         * the inner statements have to ensure that (in particular the recursive
         * call).
         */
        while (token != endToken && token != XmlPullParser.END_DOCUMENT) {
            if (token == XmlPullParser.PROCESSING_INSTRUCTION) {
                /*
                 * Found a processing instructions. We need to split the token
                 * text at the first whitespace character.
                 */
                String text = parser.getText();

                int dot = text.indexOf(' ');

                String target = (dot != -1 ? text.substring(0, dot) : text);
                String data = (dot != -1 ? text.substring(dot + 1) : "");

                node.appendChild(document.createProcessingInstruction(target,
                        data));
            } else if (token == XmlPullParser.DOCDECL) {
                String name = parser.getRootElementName();
                String publicId = parser.getPublicId();
                String systemId = parser.getSystemId();
                document.appendChild(new DocumentTypeImpl(document, name, publicId, systemId));

            } else if (token == XmlPullParser.COMMENT) {
                /*
                 * Found a comment. We simply take the token text, but we only
                 * create a node if the client wants to see comments at all.
                 */
                if (!ignoreComments) {
                    node.appendChild(document.createComment(parser.getText()));
                }
            } else if (token == XmlPullParser.IGNORABLE_WHITESPACE) {
                /*
                 * Found some ignorable whitespace. We only add it if the client
                 * wants to see whitespace. Whitespace before and after the
                 * document element is always ignored.
                 */
                if (!ignoreElementContentWhitespace && document != node) {
                    appendText(document, node, token, parser.getText());
                }
            } else if (token == XmlPullParser.TEXT || token == XmlPullParser.CDSECT) {
                /*
                 * Found a piece of text (possibly encoded as a CDATA section).
                 * That's the easiest case. We simply take it and create a new text node,
                 * or merge with an adjacent text node.
                 */
                appendText(document, node, token, parser.getText());
            } else if (token == XmlPullParser.ENTITY_REF) {
                /*
                 * Found an entity reference. If an entity resolver is
                 * installed, we replace it by text (if possible). Otherwise we
                 * add an entity reference node.
                 */
                String entity = parser.getName();

                if (entityResolver != null) {
                    // TODO Implement this...
                }

                String resolved = resolvePredefinedOrCharacterEntity(entity);
                if (resolved != null) {
                    appendText(document, node, token, resolved);
                } else {
                    node.appendChild(document.createEntityReference(entity));
                }
            } else if (token == XmlPullParser.START_TAG) {
                /*
                 * Found an element start tag. We create an element node with
                 * the proper info and attributes. We then invoke parse()
                 * recursively to handle the next level of nesting. When we
                 * return from this call, we check that we are on the proper
                 * element end tag. The whole handling differs somewhat
                 * depending on whether the parser is namespace-aware or not.
                 */
                if (namespaceAware) {
                    // Collect info for element node
                    String namespace = parser.getNamespace();
                    String name = parser.getName();
                    String prefix = parser.getPrefix();

                    if ("".equals(namespace)) {
                        namespace = null;
                    }

                    // Create element node and wire it correctly
                    Element element = document.createElementNS(namespace, name);
                    element.setPrefix(prefix);
                    node.appendChild(element);

                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        // Collect info for a single attribute node
                        String attrNamespace = parser.getAttributeNamespace(i);
                        String attrPrefix = parser.getAttributePrefix(i);
                        String attrName = parser.getAttributeName(i);
                        String attrValue = parser.getAttributeValue(i);

                        if ("".equals(attrNamespace)) {
                            attrNamespace = null;
                        }

                        // Create attribute node and wire it correctly
                        Attr attr = document.createAttributeNS(attrNamespace, attrName);
                        attr.setPrefix(attrPrefix);
                        attr.setValue(attrValue);
                        element.setAttributeNodeNS(attr);
                    }

                    // Recursive descent
                    token = parser.nextToken();
                    parse(parser, document, element, XmlPullParser.END_TAG);

                    // Expect the element's end tag here
                    parser.require(XmlPullParser.END_TAG, namespace, name);

                } else {
                    // Collect info for element node
                    String name = parser.getName();

                    // Create element node and wire it correctly
                    Element element = document.createElement(name);
                    node.appendChild(element);

                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        // Collect info for a single attribute node
                        String attrName = parser.getAttributeName(i);
                        String attrValue = parser.getAttributeValue(i);

                        // Create attribute node and wire it correctly
                        Attr attr = document.createAttribute(attrName);
                        attr.setValue(attrValue);
                        element.setAttributeNode(attr);
                    }

                    // Recursive descent
                    token = parser.nextToken();
                    parse(parser, document, element, XmlPullParser.END_TAG);

                    // Expect the element's end tag here
                    parser.require(XmlPullParser.END_TAG, "", name);
                }
            }

            token = parser.nextToken();
        }
    }

    /**
     * @param token the XML pull parser token type, such as XmlPullParser.CDSECT
     *      or XmlPullParser.ENTITY_REF.
     */
    private void appendText(DocumentImpl document, Node parent, int token, String text) {
        // Ignore empty runs.
        if (text.isEmpty()) {
            return;
        }
        // Merge with any previous text node if possible.
        if (coalescing || token != XmlPullParser.CDSECT) {
            Node lastChild = parent.getLastChild();
            if (lastChild != null && lastChild.getNodeType() == Node.TEXT_NODE) {
                Text textNode = (Text) lastChild;
                textNode.appendData(text);
                return;
            }
        }
        // Okay, we really do need a new text node
        parent.appendChild(token == XmlPullParser.CDSECT
                ? new CDATASectionImpl(document, text)
                : new TextImpl(document, text));
    }

    @Override
    public void setEntityResolver(EntityResolver resolver) {
        entityResolver = resolver;
    }

    @Override
    public void setErrorHandler(ErrorHandler handler) {
        errorHandler = handler;
    }

    /**
     * Controls whether this DocumentBuilder ignores comments.
     */
    public void setIgnoreComments(boolean value) {
        ignoreComments = value;
    }

    public void setCoalescing(boolean value) {
        coalescing = value;
    }

    /**
     * Controls whether this DocumentBuilder ignores element content whitespace.
     */
    public void setIgnoreElementContentWhitespace(boolean value) {
        ignoreElementContentWhitespace = value;
    }

    /**
     * Controls whether this DocumentBuilder is namespace-aware.
     */
    public void setNamespaceAware(boolean value) {
        namespaceAware = value;
    }

    /**
     * Returns the replacement text or null if {@code entity} isn't predefined.
     */
    private String resolvePredefinedOrCharacterEntity(String entityName) {
        // Character references, section 4.1 of the XML specification.
        if (entityName.startsWith("#x")) {
            return resolveCharacterReference(entityName.substring(2), 16);
        } else if (entityName.startsWith("#")) {
            return resolveCharacterReference(entityName.substring(1), 10);
        }
        // Predefined entities, section 4.6 of the XML specification.
        if ("lt".equals(entityName)) {
            return "<";
        } else if ("gt".equals(entityName)) {
            return ">";
        } else if ("amp".equals(entityName)) {
            return "&";
        } else if ("apos".equals(entityName)) {
            return "'";
        } else if ("quot".equals(entityName)) {
            return "\"";
        } else {
            return null;
        }
    }

    private String resolveCharacterReference(String value, int base) {
        try {
            int ch = Integer.parseInt(value, base);
            if (ch < 0 || ch > Character.MAX_VALUE) {
                return null;
            }
            return String.valueOf((char) ch);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
