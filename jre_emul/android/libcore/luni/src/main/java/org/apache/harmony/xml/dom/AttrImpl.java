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

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
public final class AttrImpl extends NodeImpl implements Attr {

    // Maintained by ElementImpl.
    ElementImpl ownerElement;
    boolean isId;

    boolean namespaceAware;
    String namespaceURI;
    String prefix;
    String localName;

    private String value = "";

    AttrImpl(DocumentImpl document, String namespaceURI, String qualifiedName) {
        super(document);
        setNameNS(this, namespaceURI, qualifiedName);
    }

    AttrImpl(DocumentImpl document, String name) {
        super(document);
        setName(this, name);
    }

    @Override
    public String getLocalName() {
        return namespaceAware ? localName : null;
    }

    public String getName() {
        return prefix != null
                ? prefix + ":" + localName
                : localName;
    }

    @Override
    public String getNamespaceURI() {
        return namespaceURI;
    }

    @Override
    public String getNodeName() {
        return getName();
    }

    public short getNodeType() {
        return Node.ATTRIBUTE_NODE;
    }

    @Override
    public String getNodeValue() {
        return getValue();
    }

    public Element getOwnerElement() {
        return ownerElement;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    public boolean getSpecified() {
        return value != null;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = validatePrefix(prefix, namespaceAware, namespaceURI);
    }

    public void setValue(String value) throws DOMException {
        this.value = value;
    }

    public TypeInfo getSchemaTypeInfo() {
        // TODO: populate this when we support XML Schema
        return NULL_TYPE_INFO;
    }

    public boolean isId() {
        return isId;
    }
}
