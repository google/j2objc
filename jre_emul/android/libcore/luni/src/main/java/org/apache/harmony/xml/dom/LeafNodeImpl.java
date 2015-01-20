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
 * <p>
 * This class represents a Node that has a parent Node, but no children.
 */
public abstract class LeafNodeImpl extends NodeImpl {

    // Maintained by InnerNodeImpl.
    InnerNodeImpl parent;

    // Maintained by InnerNodeImpl.
    int index;

    LeafNodeImpl(DocumentImpl document) {
        super(document);
    }

    public Node getNextSibling() {
        if (parent == null || index + 1 >= parent.children.size()) {
            return null;
        }

        return parent.children.get(index + 1);
    }

    public Node getParentNode() {
        return parent;
    }

    public Node getPreviousSibling() {
        if (parent == null || index == 0) {
            return null;
        }

        return parent.children.get(index - 1);
    }

    boolean isParentOf(Node node) {
        return false;
    }

}
