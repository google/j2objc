/*
 * Copyright (C) 2021 The Android Open Source Project
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

package libcore.javax.xml.transform.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.ArrayList;
import javax.xml.transform.dom.DOMResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.SAXException;

@RunWith(JUnit4.class)
public class DOMResultTest {

    private Node node;
    private Node documentNode;

    @Before
    public void setUp() throws SAXException, IOException {
        node = new NodeImpl();
        documentNode = new NodeImpl();
    }

    @Test
    public void constructorWithNode() {
        DOMResult result = new DOMResult(node);
        assertNotNull(result);
        assertEquals(node, result.getNode());
    }

    @Test
    public void constructorWithNodeAndString() {
        DOMResult result = new DOMResult(node, "systemId");
        assertNotNull(result);
        assertEquals(node, result.getNode());
        assertEquals("systemId", result.getSystemId());
    }

    @Test
    public void constructorWithNodeAndNode() {
        documentNode.appendChild(node);
        DOMResult result = new DOMResult(documentNode, node);
        assertNotNull(result);
        assertEquals(documentNode, result.getNode());
        assertEquals(node, result.getNextSibling());
    }

    @Test
    public void constructorWithNodeNodeAndString() {
        documentNode.appendChild(node);
        DOMResult result = new DOMResult(documentNode, node, "systemId");
        assertNotNull(result);
        assertEquals(documentNode, result.getNode());
        assertEquals("systemId", result.getSystemId());
        assertEquals(node, result.getNextSibling());
    }

    @Test
    public void getSetSystemId() {
        DOMResult result = new DOMResult(node);
        assertNull(result.getSystemId());

        result.setSystemId("systemId");
        assertEquals("systemId", result.getSystemId());
    }

    @Test
    public void getSetNextSibling() {
        DOMResult result = new DOMResult(node);
        assertNull(result.getNextSibling());

        Node anotherNode = new NodeImpl();
        result.setNextSibling(anotherNode);
        assertNotEquals(node, result.getNextSibling());
        assertEquals(anotherNode, result.getNextSibling());
    }

    private static final class NodeImpl implements Node, Document {

        private final ArrayList<Node> children;

        public NodeImpl() {
            this.children = new ArrayList<>();
        }

        @Override
        public String getNodeName() { return null; }

        @Override
        public String getNodeValue() throws DOMException { return null; }

        @Override
        public void setNodeValue(String nodeValue) throws DOMException {}

        @Override
        public short getNodeType() { return 0; }

        @Override
        public Node getParentNode() { return null; }

        @Override
        public NodeList getChildNodes() { return new NodeListImpl(children); }

        @Override
        public Node getFirstChild() { return children.get(0); }

        @Override
        public Node getLastChild() { return children.get(children.size() - 1); }

        @Override
        public Node getPreviousSibling() { return null; }

        @Override
        public Node getNextSibling() { return null; }

        @Override
        public NamedNodeMap getAttributes() { return null; }

        @Override
        public Document getOwnerDocument() { return null; }

        @Override
        public Node insertBefore(Node newChild, Node refChild) throws DOMException { return null; }

        @Override
        public Node replaceChild(Node newChild, Node oldChild) throws DOMException { return null; }

        @Override
        public Node removeChild(Node oldChild) throws DOMException { return null; }

        @Override
        public Node appendChild(Node newChild) throws DOMException { return null; }

        @Override
        public boolean hasChildNodes() { return false; }

        @Override
        public Node cloneNode(boolean deep) { return null; }

        @Override
        public void normalize() {}

        @Override
        public boolean isSupported(String feature, String version) { return false; }

        @Override
        public String getNamespaceURI() { return null; }

        @Override
        public String getPrefix() { return null; }

        @Override
        public void setPrefix(String prefix) throws DOMException {}

        @Override
        public String getLocalName() { return null; }

        @Override
        public boolean hasAttributes() { return false; }

        @Override
        public String getBaseURI() { return null; }

        @Override
        public short compareDocumentPosition(Node other) throws DOMException {
            return DOCUMENT_POSITION_CONTAINED_BY;
        }

        @Override
        public String getTextContent() throws DOMException { return null; }

        @Override
        public void setTextContent(String textContent) throws DOMException {}

        @Override
        public boolean isSameNode(Node other) { return false; }

        @Override
        public String lookupPrefix(String namespaceURI) { return null; }

        @Override
        public boolean isDefaultNamespace(String namespaceURI) { return false; }

        @Override
        public String lookupNamespaceURI(String prefix) { return null; }

        @Override
        public boolean isEqualNode(Node arg) { return false; }

        @Override
        public Object getFeature(String feature, String version) { return null; }

        @Override
        public Object setUserData(String key, Object data, UserDataHandler handler) { return null; }

        @Override
        public Object getUserData(String key) { return null; }

        @Override
        public DocumentType getDoctype() { return null; }

        @Override
        public DOMImplementation getImplementation() { return null; }

        @Override
        public Element getDocumentElement() { return null; }

        @Override
        public Element createElement(String tagName) throws DOMException { return null; }

        @Override
        public DocumentFragment createDocumentFragment() { return null; }

        @Override
        public Text createTextNode(String data) { return null; }

        @Override
        public Comment createComment(String data) { return null; }

        @Override
        public CDATASection createCDATASection(String data) throws DOMException { return null; }

        @Override
        public ProcessingInstruction createProcessingInstruction(String target, String data)
                throws DOMException  { return null; }

        @Override
        public Attr createAttribute(String name) throws DOMException { return null; }

        @Override
        public EntityReference createEntityReference(String name) throws DOMException {
            return null;
        }

        @Override
        public NodeList getElementsByTagName(String tagname) { return null; }

        @Override
        public Node importNode(Node importedNode, boolean deep) throws DOMException  {
            return null;
        }

        @Override
        public Element createElementNS(String namespaceURI, String qualifiedName)
                throws DOMException { return null; }

        @Override
        public Attr createAttributeNS(String namespaceURI, String qualifiedName)
                throws DOMException { return null; }

        @Override
        public NodeList getElementsByTagNameNS(String namespaceURI, String localName)  {
            return null;
        }

        @Override
        public Element getElementById(String elementId) { return null; }

        @Override
        public String getInputEncoding() { return null; }

        @Override
        public String getXmlEncoding() { return null; }

        @Override
        public boolean getXmlStandalone() { return false; }

        @Override
        public void setXmlStandalone(boolean xmlStandalone) throws DOMException {}

        @Override
        public String getXmlVersion()  { return null; }

        @Override
        public void setXmlVersion(String xmlVersion) throws DOMException {}

        @Override
        public boolean getStrictErrorChecking() { return false; }

        @Override
        public void setStrictErrorChecking(boolean strictErrorChecking) {}

        @Override
        public String getDocumentURI() { return null;  }

        @Override
        public void setDocumentURI(String documentURI) {}

        @Override
        public Node adoptNode(Node source) throws DOMException { return null; }

        @Override
        public DOMConfiguration getDomConfig() { return null; }

        @Override
        public void normalizeDocument() {}

        @Override
        public Node renameNode(Node n, String namespaceURI, String qualifiedName)
                throws DOMException { return null; }
    }

    private static final class NodeListImpl implements NodeList {

        private final ArrayList<Node> list;

        public NodeListImpl(ArrayList<Node> list) {
            this.list = list;
        }

        @Override
        public Node item(int index) {
            return list.get(index);
        }

        @Override
        public int getLength() {
            return list.size();
        }
    }

}
