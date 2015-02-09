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

import org.w3c.dom.Comment;
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
public final class CommentImpl extends CharacterDataImpl implements Comment {

    CommentImpl(DocumentImpl document, String data) {
        super(document, data);
    }

    @Override
    public String getNodeName() {
        return "#comment";
    }

    @Override
    public short getNodeType() {
        return Node.COMMENT_NODE;
    }

    /**
     * Returns true if this comment contains the illegal character sequence
     * "--". Such nodes may not be serialized.
     */
    public boolean containsDashDash() {
        return buffer.indexOf("--") != -1;
    }
}
