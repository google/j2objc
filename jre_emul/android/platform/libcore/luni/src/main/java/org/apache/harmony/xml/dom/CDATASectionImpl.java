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

import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;

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
public final class CDATASectionImpl extends TextImpl implements CDATASection {

    public CDATASectionImpl(DocumentImpl document, String data) {
        super(document, data);
    }

    @Override
    public String getNodeName() {
        return "#cdata-section";
    }

    @Override
    public short getNodeType() {
        return Node.CDATA_SECTION_NODE;
    }

    /**
     * Splits this CDATA node into parts that do not contain a "]]>" sequence.
     * Any newly created nodes will be inserted before this node.
     */
    public void split() {
        if (!needsSplitting()) {
            return;
        }

        Node parent = getParentNode();
        String[] parts = getData().split("\\]\\]>");
        parent.insertBefore(new CDATASectionImpl(document, parts[0] + "]]"), this);
        for (int p = 1; p < parts.length - 1; p++) {
            parent.insertBefore(new CDATASectionImpl(document, ">" + parts[p] + "]]"), this);
        }
        setData(">" + parts[parts.length - 1]);
    }

    /**
     * Returns true if this CDATA section contains the illegal character
     * sequence "]]>". Such nodes must be {@link #split} before they are
     * serialized.
     */
    public boolean needsSplitting() {
        return buffer.indexOf("]]>") != -1;
    }

    /**
     * Replaces this node with a semantically equivalent text node. This node
     * will be removed from the DOM tree and the new node inserted in its place.
     *
     * @return the replacement node.
     */
    public TextImpl replaceWithText() {
        TextImpl replacement = new TextImpl(document, getData());
        parent.insertBefore(replacement, this);
        parent.removeChild(this);
        return replacement;
    }
}
