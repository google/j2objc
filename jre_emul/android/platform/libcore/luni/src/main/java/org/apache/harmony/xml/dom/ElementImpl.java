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

import java.util.ArrayList;
import java.util.List;
import libcore.util.Objects;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;

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
public class ElementImpl extends InnerNodeImpl implements Element {

    boolean namespaceAware;
    String namespaceURI;
    String prefix;
    String localName;

    private List<AttrImpl> attributes = new ArrayList<AttrImpl>();

    ElementImpl(DocumentImpl document, String namespaceURI, String qualifiedName) {
        super(document);
        setNameNS(this, namespaceURI, qualifiedName);
    }

    ElementImpl(DocumentImpl document, String name) {
        super(document);
        setName(this, name);
    }

    private int indexOfAttribute(String name) {
        for (int i = 0; i < attributes.size(); i++) {
            AttrImpl attr = attributes.get(i);
            if (Objects.equal(name, attr.getNodeName())) {
                return i;
            }
        }

        return -1;
    }

    private int indexOfAttributeNS(String namespaceURI, String localName) {
        for (int i = 0; i < attributes.size(); i++) {
            AttrImpl attr = attributes.get(i);
            if (Objects.equal(namespaceURI, attr.getNamespaceURI())
                    && Objects.equal(localName, attr.getLocalName())) {
                return i;
            }
        }

        return -1;
    }

    public String getAttribute(String name) {
        Attr attr = getAttributeNode(name);

        if (attr == null) {
            return "";
        }

        return attr.getValue();
    }

    public String getAttributeNS(String namespaceURI, String localName) {
        Attr attr = getAttributeNodeNS(namespaceURI, localName);

        if (attr == null) {
            return "";
        }

        return attr.getValue();
    }

    public AttrImpl getAttributeNode(String name) {
        int i = indexOfAttribute(name);

        if (i == -1) {
            return null;
        }

        return attributes.get(i);
    }

    public AttrImpl getAttributeNodeNS(String namespaceURI, String localName) {
        int i = indexOfAttributeNS(namespaceURI, localName);

        if (i == -1) {
            return null;
        }

        return attributes.get(i);
    }

    @Override
    public NamedNodeMap getAttributes() {
        return new ElementAttrNamedNodeMapImpl();
    }

    /**
     * This implementation walks the entire document looking for an element
     * with the given ID attribute. We should consider adding an index to speed
     * navigation of large documents.
     */
    Element getElementById(String name) {
        for (Attr attr : attributes) {
            if (attr.isId() && name.equals(attr.getValue())) {
                return this;
            }
        }

        /*
         * TODO: Remove this behavior.
         * The spec explicitly says that this is a bad idea. From
         * Document.getElementById(): "Attributes with the name "ID"
         * or "id" are not of type ID unless so defined.
         */
        if (name.equals(getAttribute("id"))) {
            return this;
        }

        for (NodeImpl node : children) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = ((ElementImpl) node).getElementById(name);
                if (element != null) {
                    return element;
                }
            }
        }

        return null;
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

    @Override
    public String getLocalName() {
        return namespaceAware ? localName : null;
    }

    @Override
    public String getNamespaceURI() {
        return namespaceURI;
    }

    @Override
    public String getNodeName() {
        return getTagName();
    }

    public short getNodeType() {
        return Node.ELEMENT_NODE;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    public String getTagName() {
        return prefix != null
                ? prefix + ":" + localName
                : localName;
    }

    public boolean hasAttribute(String name) {
        return indexOfAttribute(name) != -1;
    }

    public boolean hasAttributeNS(String namespaceURI, String localName) {
        return indexOfAttributeNS(namespaceURI, localName) != -1;
    }

    @Override
    public boolean hasAttributes() {
        return !attributes.isEmpty();
    }

    public void removeAttribute(String name) throws DOMException {
        int i = indexOfAttribute(name);

        if (i != -1) {
            attributes.remove(i);
        }
    }

    public void removeAttributeNS(String namespaceURI, String localName)
            throws DOMException {
        int i = indexOfAttributeNS(namespaceURI, localName);

        if (i != -1) {
            attributes.remove(i);
        }
    }

    public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
        AttrImpl oldAttrImpl = (AttrImpl) oldAttr;

        if (oldAttrImpl.getOwnerElement() != this) {
            throw new DOMException(DOMException.NOT_FOUND_ERR, null);
        }

        attributes.remove(oldAttrImpl);
        oldAttrImpl.ownerElement = null;

        return oldAttrImpl;
    }

    public void setAttribute(String name, String value) throws DOMException {
        Attr attr = getAttributeNode(name);

        if (attr == null) {
            attr = document.createAttribute(name);
            setAttributeNode(attr);
        }

        attr.setValue(value);
    }

    public void setAttributeNS(String namespaceURI, String qualifiedName,
            String value) throws DOMException {
        Attr attr = getAttributeNodeNS(namespaceURI, qualifiedName);

        if (attr == null) {
            attr = document.createAttributeNS(namespaceURI, qualifiedName);
            setAttributeNodeNS(attr);
        }

        attr.setValue(value);
    }

    public Attr setAttributeNode(Attr newAttr) throws DOMException {
        AttrImpl newAttrImpl = (AttrImpl) newAttr;

        if (newAttrImpl.document != this.document) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, null);
        }

        if (newAttrImpl.getOwnerElement() != null) {
            throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, null);
        }

        AttrImpl oldAttrImpl = null;

        int i = indexOfAttribute(newAttr.getName());
        if (i != -1) {
            oldAttrImpl = attributes.get(i);
            attributes.remove(i);
        }

        attributes.add(newAttrImpl);
        newAttrImpl.ownerElement = this;

        return oldAttrImpl;
    }

    public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
        AttrImpl newAttrImpl = (AttrImpl) newAttr;

        if (newAttrImpl.document != this.document) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, null);
        }

        if (newAttrImpl.getOwnerElement() != null) {
            throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, null);
        }

        AttrImpl oldAttrImpl = null;

        int i = indexOfAttributeNS(newAttr.getNamespaceURI(), newAttr.getLocalName());
        if (i != -1) {
            oldAttrImpl = attributes.get(i);
            attributes.remove(i);
        }

        attributes.add(newAttrImpl);
        newAttrImpl.ownerElement = this;

        return oldAttrImpl;
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = validatePrefix(prefix, namespaceAware, namespaceURI);
    }

    public class ElementAttrNamedNodeMapImpl implements NamedNodeMap {

        public int getLength() {
            return ElementImpl.this.attributes.size();
        }

        private int indexOfItem(String name) {
            return ElementImpl.this.indexOfAttribute(name);
        }

        private int indexOfItemNS(String namespaceURI, String localName) {
            return ElementImpl.this.indexOfAttributeNS(namespaceURI, localName);
        }

        public Node getNamedItem(String name) {
            return ElementImpl.this.getAttributeNode(name);
        }

        public Node getNamedItemNS(String namespaceURI, String localName) {
            return ElementImpl.this.getAttributeNodeNS(namespaceURI, localName);
        }

        public Node item(int index) {
            return ElementImpl.this.attributes.get(index);
        }

        public Node removeNamedItem(String name) throws DOMException {
            int i = indexOfItem(name);

            if (i == -1) {
                throw new DOMException(DOMException.NOT_FOUND_ERR, null);
            }

            return ElementImpl.this.attributes.remove(i);
        }

        public Node removeNamedItemNS(String namespaceURI, String localName)
                throws DOMException {
            int i = indexOfItemNS(namespaceURI, localName);

            if (i == -1) {
                throw new DOMException(DOMException.NOT_FOUND_ERR, null);
            }

            return ElementImpl.this.attributes.remove(i);
        }

        public Node setNamedItem(Node arg) throws DOMException {
            if (!(arg instanceof Attr)) {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
            }

            return ElementImpl.this.setAttributeNode((Attr)arg);
        }

        public Node setNamedItemNS(Node arg) throws DOMException {
            if (!(arg instanceof Attr)) {
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
            }

            return ElementImpl.this.setAttributeNodeNS((Attr)arg);
        }
    }

    public TypeInfo getSchemaTypeInfo() {
        // TODO: populate this when we support XML Schema
        return NULL_TYPE_INFO;
    }

    public void setIdAttribute(String name, boolean isId) throws DOMException {
        AttrImpl attr = getAttributeNode(name);
        if (attr == null) {
            throw new DOMException(DOMException.NOT_FOUND_ERR,
                    "No such attribute: " + name);
        }
        attr.isId = isId;
    }

    public void setIdAttributeNS(String namespaceURI, String localName,
            boolean isId) throws DOMException {
        AttrImpl attr = getAttributeNodeNS(namespaceURI, localName);
        if (attr == null) {
            throw new DOMException(DOMException.NOT_FOUND_ERR,
                    "No such attribute: " + namespaceURI +  " " + localName);
        }
        attr.isId = isId;
    }

    public void setIdAttributeNode(Attr idAttr, boolean isId) throws DOMException {
        ((AttrImpl) idAttr).isId = isId;
    }
}
