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
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
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
public final class DocumentTypeImpl extends LeafNodeImpl implements DocumentType {

    private String qualifiedName;

    private String publicId;

    private String systemId;

    public DocumentTypeImpl(DocumentImpl document, String qualifiedName,
            String publicId, String systemId) {
        super(document);

        if (qualifiedName == null || "".equals(qualifiedName)) {
            throw new DOMException(DOMException.NAMESPACE_ERR, qualifiedName);
        }

        int prefixSeparator = qualifiedName.lastIndexOf(":");
        if (prefixSeparator != -1) {
            String prefix = qualifiedName.substring(0, prefixSeparator);
            String localName = qualifiedName.substring(prefixSeparator + 1);

            if (!DocumentImpl.isXMLIdentifier(prefix)) {
                throw new DOMException(DOMException.NAMESPACE_ERR, qualifiedName);
            }

            if (!DocumentImpl.isXMLIdentifier(localName)) {
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, qualifiedName);
            }
        } else {
            if (!DocumentImpl.isXMLIdentifier(qualifiedName)) {
                throw new DOMException(DOMException.INVALID_CHARACTER_ERR, qualifiedName);
            }
        }

        this.qualifiedName = qualifiedName;
        this.publicId = publicId;
        this.systemId = systemId;
    }

    @Override
    public String getNodeName() {
        return qualifiedName;
    }

    @Override
    public short getNodeType() {
        return Node.DOCUMENT_TYPE_NODE;
    }

    public NamedNodeMap getEntities() {
        // TODO Dummy. Implement this later, if at all (we're DOM level 2 only).
        return null;
    }

    public String getInternalSubset() {
        // TODO Dummy. Implement this later, if at all (we're DOM level 2 only).
        return null;
    }

    public String getName() {
        return qualifiedName;
    }

    public NamedNodeMap getNotations() {
        // TODO Dummy. Implement this later, if at all (we're DOM level 2 only).
        return null;
    }

    public String getPublicId() {
        return publicId;
    }

    public String getSystemId() {
        return systemId;
    }

    @Override public String getTextContent() throws DOMException {
        return null;
    }
}
