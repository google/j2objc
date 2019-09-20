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
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

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
public final class DOMImplementationImpl implements DOMImplementation {

    // Singleton instance.
    private static DOMImplementationImpl instance;

    DOMImplementationImpl() {
    }

    public Document createDocument(String namespaceURI, String qualifiedName,
            DocumentType doctype) throws DOMException {
        return new DocumentImpl(this, namespaceURI, qualifiedName, doctype, null);
    }

    public DocumentType createDocumentType(String qualifiedName,
            String publicId, String systemId) throws DOMException {
        return new DocumentTypeImpl(null, qualifiedName, publicId, systemId);
    }

    public boolean hasFeature(String feature, String version) {
        boolean anyVersion = version == null || version.length() == 0;
        if (feature.startsWith("+")) {
            feature = feature.substring(1);
        }

        // TODO: fully implement these APIs:
        // "LS" (org.w3c.dom.ls) versions "3.0"
        // "ElementTraversal" (org.w3c.dom.traversal) versions "1.0"

        if (feature.equalsIgnoreCase("Core")) {
            return anyVersion || version.equals("1.0") || version.equals("2.0") || version.equals("3.0");
        } else if (feature.equalsIgnoreCase("XML")) {
            return anyVersion || version.equals("1.0") || version.equals("2.0") || version.equals("3.0");
        } else if (feature.equalsIgnoreCase("XMLVersion")) {
            return anyVersion || version.equals("1.0") || version.equals("1.1");
        } else {
            return false;
        }
    }

    /**
     * Requests the singleton instance of the class. Creates it first, if
     * necessary.
     *
     * @return The singleton Android DOMImplementationImpl instance.
     */
    public static DOMImplementationImpl getInstance() {
        if (instance == null) {
            instance = new DOMImplementationImpl();
        }

        return instance;
    }

    public Object getFeature(String feature, String version) {
        return hasFeature(feature, version) ? this : null;
    }
}
