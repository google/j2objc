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

/**
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package org.apache.harmony.security.tests.java.security;

import java.security.PublicKey;

import junit.framework.TestCase;

/**
 * Tests for <code>PublicKey</code> class field
 *
 */
public class PublicKeyTest extends TestCase {

    /**
     * Test for <code>serialVersionUID</code> field
     */
    public void testField() {
        checkPublicKey cPK = new checkPublicKey();
        assertEquals("Incorrect serialVersionUID", cPK.getSerVerUID(), //PublicKey.serialVersionUID,
                7187392471159151072L);
    }

    public class checkPublicKey implements PublicKey {
        public String getAlgorithm() {
            return "PublicKey";
        }
        public String getFormat() {
            return "Format";
        }
        public byte[] getEncoded() {
            return new byte[0];
        }
        public long getSerVerUID() {
            return serialVersionUID;
        }
    }
}
