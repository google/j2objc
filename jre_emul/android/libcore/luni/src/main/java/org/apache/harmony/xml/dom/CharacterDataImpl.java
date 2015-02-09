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

import org.w3c.dom.CharacterData;
import org.w3c.dom.DOMException;

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
public abstract class CharacterDataImpl extends LeafNodeImpl implements
        CharacterData {

    protected StringBuffer buffer;

    CharacterDataImpl(DocumentImpl document, String data) {
        super(document);
        setData(data);
    }

    public void appendData(String arg) throws DOMException {
        buffer.append(arg);
    }

    public void deleteData(int offset, int count) throws DOMException {
        buffer.delete(offset, offset + count);
    }

    public String getData() throws DOMException {
        return buffer.toString();
    }

    /**
     * Appends this node's text content to the given builder.
     */
    public void appendDataTo(StringBuilder stringBuilder) {
        stringBuilder.append(buffer);
    }

    public int getLength() {
        return buffer.length();
    }

    @Override
    public String getNodeValue() {
        return getData();
    }

    public void insertData(int offset, String arg) throws DOMException {
        try {
            buffer.insert(offset, arg);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR, null);
        }
    }

    public void replaceData(int offset, int count, String arg)
            throws DOMException {
        try {
            buffer.replace(offset, offset + count, arg);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR, null);
        }
    }

    public void setData(String data) throws DOMException {
        buffer = new StringBuffer(data);
    }

    public String substringData(int offset, int count) throws DOMException {
        try {
            return buffer.substring(offset, offset + count);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR, null);
        }
    }

}
