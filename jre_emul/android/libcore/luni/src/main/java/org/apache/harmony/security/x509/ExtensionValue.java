/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.harmony.security.x509;

import org.apache.harmony.security.utils.Array;

/**
 * Base class for extension value structures.
 */
public class ExtensionValue {

    /** Encoded form of the extension. */
    protected byte[] encoding;

    /** Default constructor. */
    public ExtensionValue() {}

    /** Creates the object on the base of its encoded form. */
    public ExtensionValue(byte[] encoding) {
        this.encoding = encoding;
    }

    /** Returns encoded form of the object. */
    public byte[] getEncoded() {
        return encoding;
    }

    public void dumpValue(StringBuilder sb, String prefix) {
        sb.append(prefix).append("Unparseable extension value:\n");
        if (encoding == null) {
            encoding = getEncoded();
        }
        if (encoding == null) {
            sb.append("NULL\n");
        } else {
            sb.append(Array.toString(encoding, prefix));
        }
    }

    public void dumpValue(StringBuilder sb) {
        dumpValue(sb, "");
    }
}
