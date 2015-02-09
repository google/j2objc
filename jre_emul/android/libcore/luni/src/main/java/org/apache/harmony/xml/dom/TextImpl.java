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

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

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
public class TextImpl extends CharacterDataImpl implements Text {

    public TextImpl(DocumentImpl document, String data) {
        super(document, data);
    }

    @Override
    public String getNodeName() {
        return "#text";
    }

    @Override
    public short getNodeType() {
        return Node.TEXT_NODE;
    }

    public final Text splitText(int offset) throws DOMException {
        Text newText = document.createTextNode(
                substringData(offset, getLength() - offset));
        deleteData(0, offset);

        Node refNode = getNextSibling();
        if (refNode == null) {
            getParentNode().appendChild(newText);
        } else {
            getParentNode().insertBefore(newText, refNode);
        }

        return this;
    }

    public final boolean isElementContentWhitespace() {
        // Undefined because we don't validate. Whether whitespace characters
        // constitute "element content whitespace" is defined by the containing
        // element's declaration (DTD) and we don't parse that.
        // TODO: wire this up when we support document validation
        return false;
    }

    public final String getWholeText() {
        // TODO: support entity references. This code should expand through
        // the child elements of entity references.
        //     http://code.google.com/p/android/issues/detail?id=6807

        StringBuilder result = new StringBuilder();
        for (TextImpl n = firstTextNodeInCurrentRun(); n != null; n = n.nextTextNode()) {
            n.appendDataTo(result);
        }
        return result.toString();
    }

    public final Text replaceWholeText(String content) throws DOMException {
        // TODO: support entity references. This code should expand and replace
        // the child elements of entity references.
        //     http://code.google.com/p/android/issues/detail?id=6807

        Node parent = getParentNode();
        Text result = null;

        // delete all nodes in the current run of text...
        for (TextImpl n = firstTextNodeInCurrentRun(); n != null; ) {

            // ...except the current node if we have content for it
            if (n == this && content != null && content.length() > 0) {
                setData(content);
                result = this;
                n = n.nextTextNode();

            } else {
                Node toRemove = n; // because removeChild() detaches siblings
                n = n.nextTextNode();
                parent.removeChild(toRemove);
            }
        }

        return result;
    }

    /**
     * Returns the first text or CDATA node in the current sequence of text and
     * CDATA nodes.
     */
    private TextImpl firstTextNodeInCurrentRun() {
        TextImpl firstTextInCurrentRun = this;
        for (Node p = getPreviousSibling(); p != null; p = p.getPreviousSibling()) {
            short nodeType = p.getNodeType();
            if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
                firstTextInCurrentRun = (TextImpl) p;
            } else {
                break;
            }
        }
        return firstTextInCurrentRun;
    }

    /**
     * Returns the next sibling node if it exists and it is text or CDATA.
     * Otherwise returns null.
     */
    private TextImpl nextTextNode() {
        Node nextSibling = getNextSibling();
        if (nextSibling == null) {
            return null;
        }

        short nodeType = nextSibling.getNodeType();
        return nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE
                ? (TextImpl) nextSibling
                : null;
    }

    /**
     * Tries to remove this node using itself and the previous node as context.
     * If this node's text is empty, this node is removed and null is returned.
     * If the previous node exists and is a text node, this node's text will be
     * appended to that node's text and this node will be removed.
     *
     * <p>Although this method alters the structure of the DOM tree, it does
     * not alter the document's semantics.
     *
     * @return the node holding this node's text and the end of the operation.
     *     Can be null if this node contained the empty string.
     */
    public final TextImpl minimize() {
        if (getLength() == 0) {
            parent.removeChild(this);
            return null;
        }

        Node previous = getPreviousSibling();
        if (previous == null || previous.getNodeType() != Node.TEXT_NODE) {
            return this;
        }

        TextImpl previousText = (TextImpl) previous;
        previousText.buffer.append(buffer);
        parent.removeChild(this);
        return previousText;
    }
}
