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

package org.apache.harmony.xml.dom;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

/**
 * A straightforward implementation of the corresponding W3C DOM node.
 *
 * <p>Some fields have package visibility so other classes can access them while
 * maintaining the DOM structure.
 *
 * <p>This class represents a Node that has neither a parent nor children.
 * Subclasses may have either.
 *
 * <p>Some code was adapted from Apache Xerces.
 */
public abstract class NodeImpl implements Node {

    private static final NodeList EMPTY_LIST = new NodeListImpl();

    static final TypeInfo NULL_TYPE_INFO = new TypeInfo() {
        public String getTypeName() {
            return null;
        }
        public String getTypeNamespace() {
            return null;
        }
        public boolean isDerivedFrom(
                String typeNamespaceArg, String typeNameArg, int derivationMethod) {
            return false;
        }
    };

    /**
     * The containing document. This is non-null except for DocumentTypeImpl
     * nodes created by the DOMImplementation.
     */
    DocumentImpl document;

    NodeImpl(DocumentImpl document) {
        this.document = document;
    }

    public Node appendChild(Node newChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
    }

    public final Node cloneNode(boolean deep) {
        return document.cloneOrImportNode(UserDataHandler.NODE_CLONED, this, deep);
    }

    public NamedNodeMap getAttributes() {
        return null;
    }

    public NodeList getChildNodes() {
        return EMPTY_LIST;
    }

    public Node getFirstChild() {
        return null;
    }

    public Node getLastChild() {
        return null;
    }

    public String getLocalName() {
        return null;
    }

    public String getNamespaceURI() {
        return null;
    }

    public Node getNextSibling() {
        return null;
    }

    public String getNodeName() {
        return null;
    }

    public abstract short getNodeType();

    public String getNodeValue() throws DOMException {
        return null;
    }

    public final Document getOwnerDocument() {
        return document == this ? null : document;
    }

    public Node getParentNode() {
        return null;
    }

    public String getPrefix() {
        return null;
    }

    public Node getPreviousSibling() {
        return null;
    }

    public boolean hasAttributes() {
        return false;
    }

    public boolean hasChildNodes() {
        return false;
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
    }

    public boolean isSupported(String feature, String version) {
        return DOMImplementationImpl.getInstance().hasFeature(feature, version);
    }

    public void normalize() {
    }

    public Node removeChild(Node oldChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
    }

    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
    }

    public final void setNodeValue(String nodeValue) throws DOMException {
        switch (getNodeType()) {
            case CDATA_SECTION_NODE:
            case COMMENT_NODE:
            case TEXT_NODE:
                ((CharacterData) this).setData(nodeValue);
                return;

            case PROCESSING_INSTRUCTION_NODE:
                ((ProcessingInstruction) this).setData(nodeValue);
                return;

            case ATTRIBUTE_NODE:
                ((Attr) this).setValue(nodeValue);
                return;

            case ELEMENT_NODE:
            case ENTITY_REFERENCE_NODE:
            case ENTITY_NODE:
            case DOCUMENT_NODE:
            case DOCUMENT_TYPE_NODE:
            case DOCUMENT_FRAGMENT_NODE:
            case NOTATION_NODE:
                return; // do nothing!

            default:
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                        "Unsupported node type " + getNodeType());
        }
    }

    public void setPrefix(String prefix) throws DOMException {
    }

    /**
     * Validates the element or attribute namespace prefix on this node.
     *
     * @param namespaceAware whether this node is namespace aware
     * @param namespaceURI this node's namespace URI
     */
    static String validatePrefix(String prefix, boolean namespaceAware, String namespaceURI) {
        if (!namespaceAware) {
            throw new DOMException(DOMException.NAMESPACE_ERR, prefix);
        }

        if (prefix != null) {
            if (namespaceURI == null
                    || !DocumentImpl.isXMLIdentifier(prefix)
                    || "xml".equals(prefix) && !"http://www.w3.org/XML/1998/namespace".equals(namespaceURI)
                    || "xmlns".equals(prefix) && !"http://www.w3.org/2000/xmlns/".equals(namespaceURI)) {
                throw new DOMException(DOMException.NAMESPACE_ERR, prefix);
            }
        }

        return prefix;
    }

    /**
     * Sets {@code node} to be namespace-aware and assigns its namespace URI
     * and qualified name.
     *
     * @param node an element or attribute node.
     * @param namespaceURI this node's namespace URI. May be null.
     * @param qualifiedName a possibly-prefixed name like "img" or "html:img".
     */
    static void setNameNS(NodeImpl node, String namespaceURI, String qualifiedName) {
        if (qualifiedName == null) {
            throw new DOMException(DOMException.NAMESPACE_ERR, qualifiedName);
        }

        String prefix = null;
        int p = qualifiedName.lastIndexOf(":");
        if (p != -1) {
            prefix = validatePrefix(qualifiedName.substring(0, p), true, namespaceURI);
            qualifiedName = qualifiedName.substring(p + 1);
        }

        if (!DocumentImpl.isXMLIdentifier(qualifiedName)) {
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, qualifiedName);
        }

        switch (node.getNodeType()) {
        case ATTRIBUTE_NODE:
            if ("xmlns".equals(qualifiedName)
                    && !"http://www.w3.org/2000/xmlns/".equals(namespaceURI)) {
                throw new DOMException(DOMException.NAMESPACE_ERR, qualifiedName);
            }

            AttrImpl attr = (AttrImpl) node;
            attr.namespaceAware = true;
            attr.namespaceURI = namespaceURI;
            attr.prefix = prefix;
            attr.localName = qualifiedName;
            break;

        case ELEMENT_NODE:
            ElementImpl element = (ElementImpl) node;
            element.namespaceAware = true;
            element.namespaceURI = namespaceURI;
            element.prefix = prefix;
            element.localName = qualifiedName;
            break;

        default:
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                    "Cannot rename nodes of type " + node.getNodeType());
        }
    }

    /**
     * Sets {@code node} to be not namespace-aware and assigns its name.
     *
     * @param node an element or attribute node.
     */
    static void setName(NodeImpl node, String name) {
        int prefixSeparator = name.lastIndexOf(":");
        if (prefixSeparator != -1) {
            String prefix = name.substring(0, prefixSeparator);
            String localName = name.substring(prefixSeparator + 1);
            if (!DocumentImpl.isXMLIdentifier(prefix) || !DocumentImpl.isXMLIdentifier(localName)) {
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, name);
            }
        } else if (!DocumentImpl.isXMLIdentifier(name)) {
            throw new DOMException(DOMException.INVALID_CHARACTER_ERR, name);
        }

        switch (node.getNodeType()) {
        case ATTRIBUTE_NODE:
            AttrImpl attr = (AttrImpl) node;
            attr.namespaceAware = false;
            attr.localName = name;
            break;

        case ELEMENT_NODE:
            ElementImpl element = (ElementImpl) node;
            element.namespaceAware = false;
            element.localName = name;
            break;

        default:
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                    "Cannot rename nodes of type " + node.getNodeType());
        }
    }

    public final String getBaseURI() {
        switch (getNodeType()) {
            case DOCUMENT_NODE:
                return sanitizeUri(((Document) this).getDocumentURI());

            case ELEMENT_NODE:
                Element element = (Element) this;
                String uri = element.getAttributeNS(
                        "http://www.w3.org/XML/1998/namespace", "base"); // or "xml:base"

                try {
                    // if this node has no base URI, return the parent's.
                    if (uri == null || uri.isEmpty()) {
                        return getParentBaseUri();
                    }

                    // if this node's URI is absolute, return it
                    if (new URI(uri).isAbsolute()) {
                        return uri;
                    }

                    // this node has a relative URI. Try to resolve it against the
                    // parent, but if that doesn't work just give up and return null.
                    String parentUri = getParentBaseUri();
                    if (parentUri == null) {
                        return null;
                    }

                    return new URI(parentUri).resolve(uri).toString();
                } catch (URISyntaxException e) {
                    return null;
                }

            case PROCESSING_INSTRUCTION_NODE:
                return getParentBaseUri();

            case NOTATION_NODE:
            case ENTITY_NODE:
                // When we support these node types, the parser should
                // initialize a base URI field on these nodes.
                return null;

            case ENTITY_REFERENCE_NODE:
                // TODO: get this value from the parser, falling back to the
                // referenced entity's baseURI if that doesn't exist
                return null;

            case DOCUMENT_TYPE_NODE:
            case DOCUMENT_FRAGMENT_NODE:
            case ATTRIBUTE_NODE:
            case TEXT_NODE:
            case CDATA_SECTION_NODE:
            case COMMENT_NODE:
                return null;

            default:
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                        "Unsupported node type " + getNodeType());
        }
    }

    private String getParentBaseUri() {
        Node parentNode = getParentNode();
        return parentNode != null ? parentNode.getBaseURI() : null;
    }

    /**
     * Returns the sanitized input if it is a URI, or {@code null} otherwise.
     */
    private String sanitizeUri(String uri) {
        if (uri == null || uri.length() == 0) {
            return null;
        }
        try {
            return new URI(uri).toString();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public short compareDocumentPosition(Node other)
            throws DOMException {
        throw new UnsupportedOperationException(); // TODO
    }

    public String getTextContent() throws DOMException {
        return getNodeValue();
    }

    void getTextContent(StringBuilder buf) throws DOMException {
        String content = getNodeValue();
        if (content != null) {
            buf.append(content);
        }
    }

    public final void setTextContent(String textContent) throws DOMException {
        switch (getNodeType()) {
            case DOCUMENT_TYPE_NODE:
            case DOCUMENT_NODE:
                return; // do nothing!

            case ELEMENT_NODE:
            case ENTITY_NODE:
            case ENTITY_REFERENCE_NODE:
            case DOCUMENT_FRAGMENT_NODE:
                // remove all existing children
                Node child;
                while ((child = getFirstChild()) != null) {
                    removeChild(child);
                }
                // create a text node to hold the given content
                if (textContent != null && textContent.length() != 0) {
                    appendChild(document.createTextNode(textContent));
                }
                return;

            case ATTRIBUTE_NODE:
            case TEXT_NODE:
            case CDATA_SECTION_NODE:
            case PROCESSING_INSTRUCTION_NODE:
            case COMMENT_NODE:
            case NOTATION_NODE:
                setNodeValue(textContent);
                return;

            default:
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                        "Unsupported node type " + getNodeType());
        }
    }

    public boolean isSameNode(Node other) {
        return this == other;
    }

    /**
     * Returns the element whose namespace definitions apply to this node. Use
     * this element when mapping prefixes to URIs and vice versa.
     */
    private NodeImpl getNamespacingElement() {
        switch (this.getNodeType()) {
            case ELEMENT_NODE:
                return this;

            case DOCUMENT_NODE:
                return (NodeImpl) ((Document) this).getDocumentElement();

            case ENTITY_NODE:
            case NOTATION_NODE:
            case DOCUMENT_FRAGMENT_NODE:
            case DOCUMENT_TYPE_NODE:
                return null;

            case ATTRIBUTE_NODE:
                return (NodeImpl) ((Attr) this).getOwnerElement();

            case TEXT_NODE:
            case CDATA_SECTION_NODE:
            case ENTITY_REFERENCE_NODE:
            case PROCESSING_INSTRUCTION_NODE:
            case COMMENT_NODE:
                return getContainingElement();

            default:
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                        "Unsupported node type " + getNodeType());
        }
    }

    /**
     * Returns the nearest ancestor element that contains this node.
     */
    private NodeImpl getContainingElement() {
        for (Node p = getParentNode(); p != null; p = p.getParentNode()) {
            if (p.getNodeType() == ELEMENT_NODE) {
                return (NodeImpl) p;
            }
        }
        return null;
    }

    public final String lookupPrefix(String namespaceURI) {
        if (namespaceURI == null) {
            return null;
        }

        // the XML specs define some prefixes (like "xml" and "xmlns") but this
        // API is explicitly defined to ignore those.

        NodeImpl target = getNamespacingElement();
        for (NodeImpl node = target; node != null; node = node.getContainingElement()) {
            // check this element's namespace first
            if (namespaceURI.equals(node.getNamespaceURI())
                    && target.isPrefixMappedToUri(node.getPrefix(), namespaceURI)) {
                return node.getPrefix();
            }

            // search this element for an attribute of this form:
            //   xmlns:foo="http://namespaceURI"
            if (!node.hasAttributes()) {
                continue;
            }
            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0, length = attributes.getLength(); i < length; i++) {
                Node attr = attributes.item(i);
                if (!"http://www.w3.org/2000/xmlns/".equals(attr.getNamespaceURI())
                        || !"xmlns".equals(attr.getPrefix())
                        || !namespaceURI.equals(attr.getNodeValue())) {
                    continue;
                }
                if (target.isPrefixMappedToUri(attr.getLocalName(), namespaceURI)) {
                    return attr.getLocalName();
                }
            }
        }

        return null;
    }

    /**
     * Returns true if the given prefix is mapped to the given URI on this
     * element. Since child elements can redefine prefixes, this check is
     * necessary: {@code
     * <foo xmlns:a="http://good">
     *   <bar xmlns:a="http://evil">
     *     <a:baz />
     *   </bar>
     * </foo>}
     *
     * @param prefix the prefix to find. Nullable.
     * @param uri the URI to match. Non-null.
     */
    boolean isPrefixMappedToUri(String prefix, String uri) {
        if (prefix == null) {
            return false;
        }

        String actual = lookupNamespaceURI(prefix);
        return uri.equals(actual);
    }

    public final boolean isDefaultNamespace(String namespaceURI) {
        String actual = lookupNamespaceURI(null); // null yields the default namespace
        return namespaceURI == null
                ? actual == null
                : namespaceURI.equals(actual);
    }

    public final String lookupNamespaceURI(String prefix) {
        NodeImpl target = getNamespacingElement();
        for (NodeImpl node = target; node != null; node = node.getContainingElement()) {
            // check this element's namespace first
            String nodePrefix = node.getPrefix();
            if (node.getNamespaceURI() != null) {
                if (prefix == null // null => default prefix
                        ? nodePrefix == null
                        : prefix.equals(nodePrefix)) {
                    return node.getNamespaceURI();
                }
            }

            // search this element for an attribute of the appropriate form.
            //    default namespace: xmlns="http://resultUri"
            //          non default: xmlns:specifiedPrefix="http://resultUri"
            if (!node.hasAttributes()) {
                continue;
            }
            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0, length = attributes.getLength(); i < length; i++) {
                Node attr = attributes.item(i);
                if (!"http://www.w3.org/2000/xmlns/".equals(attr.getNamespaceURI())) {
                    continue;
                }
                if (prefix == null // null => default prefix
                        ? "xmlns".equals(attr.getNodeName())
                        : "xmlns".equals(attr.getPrefix()) && prefix.equals(attr.getLocalName())) {
                    String value = attr.getNodeValue();
                    return value.length() > 0 ? value : null;
                }
            }
        }

        return null;
    }

    /**
     * Returns a list of objects such that two nodes are equal if their lists
     * are equal. Be careful: the lists may contain NamedNodeMaps and Nodes,
     * neither of which override Object.equals(). Such values must be compared
     * manually.
     */
    private static List<Object> createEqualityKey(Node node) {
        List<Object> values = new ArrayList<Object>();
        values.add(node.getNodeType());
        values.add(node.getNodeName());
        values.add(node.getLocalName());
        values.add(node.getNamespaceURI());
        values.add(node.getPrefix());
        values.add(node.getNodeValue());
        for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
            values.add(child);
        }

        switch (node.getNodeType()) {
            case DOCUMENT_TYPE_NODE:
                DocumentTypeImpl doctype = (DocumentTypeImpl) node;
                values.add(doctype.getPublicId());
                values.add(doctype.getSystemId());
                values.add(doctype.getInternalSubset());
                values.add(doctype.getEntities());
                values.add(doctype.getNotations());
                break;

            case ELEMENT_NODE:
                Element element = (Element) node;
                values.add(element.getAttributes());
                break;
        }

        return values;
    }

    public final boolean isEqualNode(Node arg) {
        if (arg == this) {
            return true;
        }

        List<Object> listA = createEqualityKey(this);
        List<Object> listB = createEqualityKey(arg);

        if (listA.size() != listB.size()) {
            return false;
        }

        for (int i = 0; i < listA.size(); i++) {
            Object a = listA.get(i);
            Object b = listB.get(i);

            if (a == b) {
                continue;

            } else if (a == null || b == null) {
                return false;

            } else if (a instanceof String || a instanceof Short) {
                if (!a.equals(b)) {
                    return false;
                }

            } else if (a instanceof NamedNodeMap) {
                if (!(b instanceof NamedNodeMap)
                        || !namedNodeMapsEqual((NamedNodeMap) a, (NamedNodeMap) b)) {
                    return false;
                }

            } else if (a instanceof Node) {
                if (!(b instanceof Node)
                        || !((Node) a).isEqualNode((Node) b)) {
                    return false;
                }

            } else {
                throw new AssertionError(); // unexpected type
            }
        }

        return true;
    }

    private boolean namedNodeMapsEqual(NamedNodeMap a, NamedNodeMap b) {
        if (a.getLength() != b.getLength()) {
            return false;
        }
        for (int i = 0; i < a.getLength(); i++) {
            Node aNode = a.item(i);
            Node bNode = aNode.getLocalName() == null
                    ? b.getNamedItem(aNode.getNodeName())
                    : b.getNamedItemNS(aNode.getNamespaceURI(), aNode.getLocalName());
            if (bNode == null || !aNode.isEqualNode(bNode)) {
                return false;
            }
        }
        return true;
    }

    public final Object getFeature(String feature, String version) {
        return isSupported(feature, version) ? this : null;
    }

    public final Object setUserData(String key, Object data, UserDataHandler handler) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        Map<String, UserData> map = document.getUserDataMap(this);
        UserData previous = data == null
                ? map.remove(key)
                : map.put(key, new UserData(data, handler));
        return previous != null ? previous.value : null;
    }

    public final Object getUserData(String key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        Map<String, UserData> map = document.getUserDataMapForRead(this);
        UserData userData = map.get(key);
        return userData != null ? userData.value : null;
    }

    static class UserData {
        final Object value;
        final UserDataHandler handler;
        UserData(Object value, UserDataHandler handler) {
            this.value = value;
            this.handler = handler;
        }
    }
}
