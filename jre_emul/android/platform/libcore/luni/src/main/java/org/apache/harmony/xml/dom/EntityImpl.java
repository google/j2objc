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

import org.w3c.dom.Entity;
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
public class EntityImpl extends NodeImpl implements Entity {

    private String notationName;

    private String publicID;

    private String systemID;

    EntityImpl(DocumentImpl document, String notationName, String publicID,
            String systemID) {
        super(document);
        this.notationName = notationName;
        this.publicID = publicID;
        this.systemID = systemID;
    }

    @Override
    public String getNodeName() {
        return getNotationName();
    }

    @Override
    public short getNodeType() {
        return Node.ENTITY_NODE;
    }

    public String getNotationName() {
        return notationName;
    }

    public String getPublicId() {
        return publicID;
    }

    public String getSystemId() {
        return systemID;
    }

    public String getInputEncoding() {
        throw new UnsupportedOperationException(); // TODO
    }

    public String getXmlEncoding() {
        throw new UnsupportedOperationException(); // TODO
    }

    public String getXmlVersion() {
        throw new UnsupportedOperationException(); // TODO
    }
}
