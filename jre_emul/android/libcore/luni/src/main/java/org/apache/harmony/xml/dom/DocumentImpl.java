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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;

/**
 * Provides a straightforward implementation of the corresponding W3C DOM
 * interface. The class is used internally only, thus only notable members that
 * are not in the original interface are documented (the W3C docs are quite
 * extensive). Hope that's ok.
 * <p>
 * Some of the fields may have package visibility, so other classes belonging to
 * the DOM implementation can easily access them while maintaining the DOM tree
 * structure.
 */
public final class DocumentImpl extends InnerNodeImpl implements Document {

    private DOMImplementation domImplementation;
    private DOMConfigurationImpl domConfiguration;

    /*
     * The default values of these fields are specified by the Document
     * interface.
     */
    private String documentUri;
    private String inputEncoding;
    private String xmlEncoding;
    private String xmlVersion = "1.0";
    private boolean xmlStandalone = false;
    private boolean strictErrorChecking = true;

    /**
     * A lazily initialized map of user data values for this document's own
     * nodes. The map is weak because the document may live longer than its
     * nodes.
     *
     * <p>Attaching user data directly to the corresponding node would cost a
     * field per node. Under the assumption that user data is rarely needed, we
     * attach user data to the document to save those fields. Xerces also takes
     * this approach.
     */
    private WeakHashMap<NodeImpl, Map<String, UserData>> nodeToUserData;

    public DocumentImpl(DOMImplementationImpl impl, String namespaceURI,
            String qualifiedName, DocumentType doctype, String inputEncoding) {
        super(null);
        this.document = this;
        this.domImplementation = impl;
        this.inputEncoding = inputEncoding;

        if (doctype != null) {
            appendChild(doctype);
        }

        if (qualifiedName != null) {
            appendChild(createElementNS(namespaceURI, qualifiedName));
        }
    }

    private static boolean isXMLIdentifierStart(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c == '_');
    }

    private static boolean isXMLIdentifierPart(char c) {
        return isXMLIdentifierStart(c) || (c >= '0' && c <= '9') || (c == '-') || (c == '.');
    }

    static boolean isXMLIdentifier(String s) {
        if (s.length() == 0) {
            return false;
        }

        if (!isXMLIdentifierStart(s.charAt(0))) {
            return false;
        }

        for (int i = 1; i < s.length(); i++) {
            if (!isXMLIdentifierPart(s.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a shallow copy of the given node. If the node is an element node,
     * its attributes are always copied.
     *
     * @param node a node belonging to any document or DOM implementation.
     * @param operation the operation type to use when notifying user data
     *     handlers of copied element attributes. It is the caller's
     *     responsibility to notify user data handlers of the returned node.
     * @return a new node whose document is this document and whose DOM
     *     implementation is this DOM implementation.
     */
    private NodeImpl shallowCopy(short operation, Node node) {
        switch (node.getNodeType()) {
        case Node.ATTRIBUTE_NODE:
            AttrImpl attr = (AttrImpl) node;
            AttrImpl attrCopy;
            if (attr.namespaceAware) {
                attrCopy = createAttributeNS(attr.getNamespaceURI(), attr.getLocalName());
                attrCopy.setPrefix(attr.getPrefix());
            } else {
                attrCopy = createAttribute(attr.getName());
            }
            attrCopy.setNodeValue(attr.getValue());
            return attrCopy;

        case Node.CDATA_SECTION_NODE:
            return createCDATASection(((CharacterData) node).getData());

        case Node.COMMENT_NODE:
            return createComment(((Comment) node).getData());

        case Node.DOCUMENT_FRAGMENT_NODE:
            return createDocumentFragment();

        case Node.DOCUMENT_NODE:
        case Node.DOCUMENT_TYPE_NODE:
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                    "Cannot copy node of type " + node.getNodeType());

        case Node.ELEMENT_NODE:
            ElementImpl element = (ElementImpl) node;
            ElementImpl elementCopy;
            if (element.namespaceAware) {
                elementCopy = createElementNS(element.getNamespaceURI(), element.getLocalName());
                elementCopy.setPrefix(element.getPrefix());
            } else {
                elementCopy = createElement(element.getTagName());
            }

            NamedNodeMap attributes = element.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                AttrImpl elementAttr = (AttrImpl) attributes.item(i);
                AttrImpl elementAttrCopy = (AttrImpl) shallowCopy(operation, elementAttr);
                notifyUserDataHandlers(operation, elementAttr, elementAttrCopy);
                if (elementAttr.namespaceAware) {
                    elementCopy.setAttributeNodeNS(elementAttrCopy);
                } else {
                    elementCopy.setAttributeNode(elementAttrCopy);
                }
            }
            return elementCopy;

        case Node.ENTITY_NODE:
        case Node.NOTATION_NODE:
            // TODO: implement this when we support these node types
            throw new UnsupportedOperationException();

        case Node.ENTITY_REFERENCE_NODE:
            /*
             * When we support entities in the doctype, this will need to
             * behave differently for clones vs. imports. Clones copy
             * entities by value, copying the referenced subtree from the
             * original document. Imports copy entities by reference,
             * possibly referring to a different subtree in the new
             * document.
             */
            return createEntityReference(node.getNodeName());

        case Node.PROCESSING_INSTRUCTION_NODE:
            ProcessingInstruction pi = (ProcessingInstruction) node;
            return createProcessingInstruction(pi.getTarget(), pi.getData());

        case Node.TEXT_NODE:
            return createTextNode(((Text) node).getData());

        default:
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                    "Unsupported node type " + node.getNodeType());
        }
    }

    /**
     * Returns a copy of the given node or subtree with this document as its
     * owner.
     *
     * @param operation either {@link UserDataHandler#NODE_CLONED} or
     *      {@link UserDataHandler#NODE_IMPORTED}.
     * @param node a node belonging to any document or DOM implementation.
     * @param deep true to recursively copy any child nodes; false to do no such
     *      copying and return a node with no children.
     */
    Node cloneOrImportNode(short operation, Node node, boolean deep) {
        NodeImpl copy = shallowCopy(operation, node);

        if (deep) {
            NodeList list = node.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                copy.appendChild(cloneOrImportNode(operation, list.item(i), deep));
            }
        }

        notifyUserDataHandlers(operation, node, copy);
        return copy;
    }

    public Node importNode(Node importedNode, boolean deep) {
        return cloneOrImportNode(UserDataHandler.NODE_IMPORTED, importedNode, deep);
    }

    /**
     * Detaches the node from its parent (if any) and changes its document to
     * this document. The node's subtree and attributes will remain attached,
     * but their document will be changed to this document.
     */
    public Node adoptNode(Node node) {
        if (!(node instanceof NodeImpl)) {
            return null; // the API specifies this quiet failure
        }
        NodeImpl nodeImpl = (NodeImpl) node;
        switch (nodeImpl.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                AttrImpl attr = (AttrImpl) node;
                if (attr.ownerElement != null) {
                    attr.ownerElement.removeAttributeNode(attr);
                }
                break;

            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.ENTITY_REFERENCE_NODE:
            case Node.PROCESSING_INSTRUCTION_NODE:
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.COMMENT_NODE:
            case Node.ELEMENT_NODE:
                break;

            case Node.DOCUMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
            case Node.ENTITY_NODE:
            case Node.NOTATION_NODE:
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                        "Cannot adopt nodes of type " + nodeImpl.getNodeType());

            default:
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
                        "Unsupported node type " + node.getNodeType());
        }

        Node parent = nodeImpl.getParentNode();
        if (parent != null) {
            parent.removeChild(nodeImpl);
        }

        changeDocumentToThis(nodeImpl);
        notifyUserDataHandlers(UserDataHandler.NODE_ADOPTED, node, null);
        return nodeImpl;
    }

    /**
     * Recursively change the document of {@code node} without also changing its
     * parent node. Only adoptNode() should invoke this method, otherwise nodes
     * will be left in an inconsistent state.
     */
    private void changeDocumentToThis(NodeImpl node) {
        Map<String, UserData> userData = node.document.getUserDataMapForRead(node);
        if (!userData.isEmpty()) {
            getUserDataMap(node).putAll(userData);
        }
        node.document = this;

        // change the document on all child nodes
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            changeDocumentToThis((NodeImpl) list.item(i));
        }

        // change the document on all attribute nodes
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                changeDocumentToThis((AttrImpl) attributes.item(i));
            }
        }
    }

    public Node renameNode(Node node, String namespaceURI, String qualifiedName) {
        if (node.getOwnerDocument() != this) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, null);
        }

        setNameNS((NodeImpl) node, namespaceURI, qualifiedName);
        notifyUserDataHandlers(UserDataHandler.NODE_RENAMED, node, null);
        return node;
    }

    public AttrImpl createAttribute(String name) {
        return new AttrImpl(this, name);
    }

    public AttrImpl createAttributeNS(String namespaceURI, String qualifiedName) {
        return new AttrImpl(this, namespaceURI, qualifiedName);
    }

    public CDATASectionImpl createCDATASection(String data) {
        return new CDATASectionImpl(this, data);
    }

    public CommentImpl createComment(String data) {
        return new CommentImpl(this, data);
    }

    public DocumentFragmentImpl createDocumentFragment() {
        return new DocumentFragmentImpl(this);
    }

    public ElementImpl createElement(String tagName) {
        return new ElementImpl(this, tagName);
    }

    public ElementImpl createElementNS(String namespaceURI, String qualifiedName) {
        return new ElementImpl(this, namespaceURI, qualifiedName);
    }

    public EntityReferenceImpl createEntityReference(String name) {
        return new EntityReferenceImpl(this, name);
    }

    public ProcessingInstructionImpl createProcessingInstruction(String target, String data) {
        return new ProcessingInstructionImpl(this, target, data);
    }

    public TextImpl createTextNode(String data) {
        return new TextImpl(this, data);
    }

    public DocumentType getDoctype() {
        for (LeafNodeImpl child : children) {
            if (child instanceof DocumentType) {
                return (DocumentType) child;
            }
        }

        return null;
    }

    public Element getDocumentElement() {
        for (LeafNodeImpl child : children) {
            if (child instanceof Element) {
                return (Element) child;
            }
        }

        return null;
    }

    public Element getElementById(String elementId) {
        ElementImpl root = (ElementImpl) getDocumentElement();

        return (root == null ? null : root.getElementById(elementId));
    }

    public NodeList getElementsByTagName(String name) {
        NodeListImpl result = new NodeListImpl();
        getElementsByTagName(result, name);
        return result;
    }

    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        NodeListImpl result = new NodeListImpl();
        getElementsByTagNameNS(result, namespaceURI, localName);
        return result;
    }

    public DOMImplementation getImplementation() {
        return domImplementation;
    }

    @Override
    public String getNodeName() {
        return "#document";
    }

    @Override
    public short getNodeType() {
        return Node.DOCUMENT_NODE;
    }

    /**
     * Document elements may have at most one root element and at most one DTD
     * element.
     */
    @Override public Node insertChildAt(Node toInsert, int index) {
        if (toInsert instanceof Element && getDocumentElement() != null) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                    "Only one root element allowed");
        }
        if (toInsert instanceof DocumentType && getDoctype() != null) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR,
                    "Only one DOCTYPE element allowed");
        }
        return super.insertChildAt(toInsert, index);
    }

    @Override public String getTextContent() {
        return null;
    }

    public String getInputEncoding() {
        return inputEncoding;
    }

    public String getXmlEncoding() {
        return xmlEncoding;
    }

    public boolean getXmlStandalone() {
        return xmlStandalone;
    }

    public void setXmlStandalone(boolean xmlStandalone) {
        this.xmlStandalone = xmlStandalone;
    }

    public String getXmlVersion() {
        return xmlVersion;
    }

    public void setXmlVersion(String xmlVersion) {
        this.xmlVersion = xmlVersion;
    }

    public boolean getStrictErrorChecking() {
        return strictErrorChecking;
    }

    public void setStrictErrorChecking(boolean strictErrorChecking) {
        this.strictErrorChecking = strictErrorChecking;
    }

    public String getDocumentURI() {
        return documentUri;
    }

    public void setDocumentURI(String documentUri) {
        this.documentUri = documentUri;
    }

    public DOMConfiguration getDomConfig() {
        if (domConfiguration == null) {
            domConfiguration = new DOMConfigurationImpl();
        }
        return domConfiguration;
    }

    public void normalizeDocument() {
        Element root = getDocumentElement();
        if (root == null) {
            return;
        }

        ((DOMConfigurationImpl) getDomConfig()).normalize(root);
    }

    /**
     * Returns a map with the user data objects attached to the specified node.
     * This map is readable and writable.
     */
    Map<String, UserData> getUserDataMap(NodeImpl node) {
        if (nodeToUserData == null) {
            nodeToUserData = new WeakHashMap<NodeImpl, Map<String, UserData>>();
        }
        Map<String, UserData> userDataMap = nodeToUserData.get(node);
        if (userDataMap == null) {
            userDataMap = new HashMap<String, UserData>();
            nodeToUserData.put(node, userDataMap);
        }
        return userDataMap;
    }

    /**
     * Returns a map with the user data objects attached to the specified node.
     * The returned map may be read-only.
     */
    Map<String, UserData> getUserDataMapForRead(NodeImpl node) {
        if (nodeToUserData == null) {
            return Collections.emptyMap();
        }
        Map<String, UserData> userDataMap = nodeToUserData.get(node);
        return userDataMap == null
                ? Collections.<String, UserData>emptyMap()
                : userDataMap;
    }

    /**
     * Calls {@link UserDataHandler#handle} on each of the source node's
     * value/handler pairs.
     *
     * <p>If the source node comes from another DOM implementation, user data
     * handlers will <strong>not</strong> be notified. The DOM API provides no
     * mechanism to inspect a foreign node's user data.
     */
    private static void notifyUserDataHandlers(
            short operation, Node source, NodeImpl destination) {
        if (!(source instanceof NodeImpl)) {
            return;
        }

        NodeImpl srcImpl = (NodeImpl) source;
        if (srcImpl.document == null) {
            return;
        }

        for (Map.Entry<String, UserData> entry
                : srcImpl.document.getUserDataMapForRead(srcImpl).entrySet()) {
            UserData userData = entry.getValue();
            if (userData.handler != null) {
                userData.handler.handle(
                        operation, entry.getKey(), userData.value, source, destination);
            }
        }
    }
}
