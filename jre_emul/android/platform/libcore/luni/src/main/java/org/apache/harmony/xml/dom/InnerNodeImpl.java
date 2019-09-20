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
import org.w3c.dom.DOMException;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides a straightforward implementation of the corresponding W3C DOM
 * interface. The class is used internally only, thus only notable members that
 * are not in the original interface are documented (the W3C docs are quite
 * extensive).
 *
 * <p>Some of the fields may have package visibility, so other classes belonging
 * to the DOM implementation can easily access them while maintaining the DOM
 * tree structure.
 *
 * <p>This class represents a Node that has a parent Node as well as
 * (potentially) a number of children.
 *
 * <p>Some code was adapted from Apache Xerces.
 */
public abstract class InnerNodeImpl extends LeafNodeImpl {

    // Maintained by LeafNodeImpl and ElementImpl.
    List<LeafNodeImpl> children = new ArrayList<LeafNodeImpl>();

    protected InnerNodeImpl(DocumentImpl document) {
        super(document);
    }

    public Node appendChild(Node newChild) throws DOMException {
        return insertChildAt(newChild, children.size());
    }

    public NodeList getChildNodes() {
        NodeListImpl list = new NodeListImpl();

        for (NodeImpl node : children) {
            list.add(node);
        }

        return list;
    }

    public Node getFirstChild() {
        return (!children.isEmpty() ? children.get(0) : null);
    }

    public Node getLastChild() {
        return (!children.isEmpty() ? children.get(children.size() - 1) : null);
    }

    public Node getNextSibling() {
        if (parent == null || index + 1 >= parent.children.size()) {
            return null;
        }

        return parent.children.get(index + 1);
    }

    public boolean hasChildNodes() {
        return children.size() != 0;
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        LeafNodeImpl refChildImpl = (LeafNodeImpl) refChild;

        if (refChildImpl == null) {
            return appendChild(newChild);
        }

        if (refChildImpl.document != document) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, null);
        }

        if (refChildImpl.parent != this) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
        }

        return insertChildAt(newChild, refChildImpl.index);
    }

    /**
     * Inserts {@code newChild} at {@code index}. If it is already child of
     * another node, it is removed from there.
     */
    Node insertChildAt(Node newChild, int index) throws DOMException {
        if (newChild instanceof DocumentFragment) {
            NodeList toAdd = newChild.getChildNodes();
            for (int i = 0; i < toAdd.getLength(); i++) {
                insertChildAt(toAdd.item(i), index + i);
            }
            return newChild;
        }

        LeafNodeImpl toInsert = (LeafNodeImpl) newChild;
        if (toInsert.document != null && document != null && toInsert.document != document) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, null);
        }
        if (toInsert.isParentOf(this)) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
        }

        if (toInsert.parent != null) {
            int oldIndex = toInsert.index;
            toInsert.parent.children.remove(oldIndex);
            toInsert.parent.refreshIndices(oldIndex);
        }

        children.add(index, toInsert);
        toInsert.parent = this;
        refreshIndices(index);

        return newChild;
    }

    public boolean isParentOf(Node node) {
        LeafNodeImpl nodeImpl = (LeafNodeImpl) node;

        while (nodeImpl != null) {
            if (nodeImpl == this) {
                return true;
            }

            nodeImpl = nodeImpl.parent;
        }

        return false;
    }

    /**
     * Normalize the text nodes within this subtree. Although named similarly,
     * this method is unrelated to Document.normalize.
     */
    @Override
    public final void normalize() {
        Node next;
        for (Node node = getFirstChild(); node != null; node = next) {
            next = node.getNextSibling();
            node.normalize();

            if (node.getNodeType() == Node.TEXT_NODE) {
                ((TextImpl) node).minimize();
            }
        }
    }

    private void refreshIndices(int fromIndex) {
        for (int i = fromIndex; i < children.size(); i++) {
            children.get(i).index = i;
        }
    }

    public Node removeChild(Node oldChild) throws DOMException {
        LeafNodeImpl oldChildImpl = (LeafNodeImpl) oldChild;

        if (oldChildImpl.document != document) {
            throw new DOMException(DOMException.WRONG_DOCUMENT_ERR, null);
        }
        if (oldChildImpl.parent != this) {
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, null);
        }

        int index = oldChildImpl.index;
        children.remove(index);
        oldChildImpl.parent = null;
        refreshIndices(index);

        return oldChild;
    }

    /**
     * Removes {@code oldChild} and adds {@code newChild} in its place. This
     * is not atomic.
     */
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        int index = ((LeafNodeImpl) oldChild).index;
        removeChild(oldChild);
        insertChildAt(newChild, index);
        return oldChild;
    }

    public String getTextContent() throws DOMException {
        Node child = getFirstChild();
        if (child == null) {
            return "";
        }

        Node next = child.getNextSibling();
        if (next == null) {
            return hasTextContent(child) ? child.getTextContent() : "";
        }

        StringBuilder buf = new StringBuilder();
        getTextContent(buf);
        return buf.toString();
    }

    void getTextContent(StringBuilder buf) throws DOMException {
        Node child = getFirstChild();
        while (child != null) {
            if (hasTextContent(child)) {
                ((NodeImpl) child).getTextContent(buf);
            }
            child = child.getNextSibling();
        }
    }

    final boolean hasTextContent(Node child) {
        // TODO: skip text nodes with ignorable whitespace?
        return child.getNodeType() != Node.COMMENT_NODE
                && child.getNodeType() != Node.PROCESSING_INSTRUCTION_NODE;
    }

    void getElementsByTagName(NodeListImpl out, String name) {
        for (NodeImpl node : children) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ElementImpl element = (ElementImpl) node;
                if (matchesNameOrWildcard(name, element.getNodeName())) {
                    out.add(element);
                }
                element.getElementsByTagName(out, name);
            }
        }
    }

    void getElementsByTagNameNS(NodeListImpl out, String namespaceURI, String localName) {
        for (NodeImpl node : children) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                ElementImpl element = (ElementImpl) node;
                if (matchesNameOrWildcard(namespaceURI, element.getNamespaceURI())
                        && matchesNameOrWildcard(localName, element.getLocalName())) {
                    out.add(element);
                }
                element.getElementsByTagNameNS(out, namespaceURI, localName);
            }
        }
    }

    /**
     * Returns true if {@code pattern} equals either "*" or {@code s}. Pattern
     * may be {@code null}.
     */
    private static boolean matchesNameOrWildcard(String pattern, String s) {
        return "*".equals(pattern) || Objects.equal(pattern, s);
    }
}
