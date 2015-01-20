/*
 * Copyright (C) 2011 The Android Open Source Project
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

import org.w3c.dom.DOMError;
import org.w3c.dom.DOMLocator;
import org.w3c.dom.Node;

public final class DOMErrorImpl implements DOMError {
    private static final DOMLocator NULL_DOM_LOCATOR = new DOMLocator() {
        public int getLineNumber() {
            return -1;
        }
        public int getColumnNumber() {
            return -1;
        }
        public int getByteOffset() {
            return -1;
        }
        public int getUtf16Offset() {
            return -1;
        }
        public Node getRelatedNode() {
            return null;
        }
        public String getUri() {
            return null;
        }
    };

    private final short severity;
    private final String type;

    public DOMErrorImpl(short severity, String type) {
        this.severity = severity;
        this.type = type;
    }

    public short getSeverity() {
        return severity;
    }

    public String getMessage() {
        return type;
    }

    public String getType() {
        return type;
    }

    public Object getRelatedException() {
        return null;
    }

    public Object getRelatedData() {
        return null;
    }

    public DOMLocator getLocation() {
        return NULL_DOM_LOCATOR;
    }
}
