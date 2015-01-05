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

import java.security.Key;

import junit.framework.TestCase;

/**
 * Tests for <code>Key</code> class field
 *
 */
public class KeyTest extends TestCase {

    /**
     * Test for <code>serialVersionUID</code> field
     */
    public void testField() {
        checkKey mk = new checkKey();
        assertEquals("Incorrect serialVersionUID", mk.getSerVerUID(), //Key.serialVersionUID,
                6603384152749567654L);
    }

    public class checkKey implements Key {
        public String getAlgorithm() {
            return "Key";
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


