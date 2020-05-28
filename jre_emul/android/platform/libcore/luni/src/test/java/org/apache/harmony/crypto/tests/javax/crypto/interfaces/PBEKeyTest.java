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

package org.apache.harmony.crypto.tests.javax.crypto.interfaces;

import junit.framework.TestCase;

import java.math.BigInteger;

import javax.crypto.interfaces.PBEKey;


/**
 * Tests for <code>PBEKey</code> class field
 *
 */
public class PBEKeyTest extends TestCase {


    /**
     * Test for <code>serialVersionUID</code> field
     */
    public void testField() {
        checkPBEKey key = new checkPBEKey();
        assertEquals("Incorrect serialVersionUID",
                key.getSerVerUID(), //PBEKey.serialVersionUID
                -1430015993304333921L);
    }

    public void test_getIterationCount() throws Exception {
        checkPBEKey key = new checkPBEKey();

        key.getIterationCount();
    }

    public void test_getPassword() throws Exception {
        checkPBEKey key = new checkPBEKey();

        key.getPassword();
    }

    public void test_getSalt() throws Exception {
        checkPBEKey key = new checkPBEKey();

        key.getSalt();
    }

    public class checkPBEKey implements PBEKey {
        public String getAlgorithm() {
            return "SecretKey";
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
        public BigInteger getY() {
            return null;
        }
        public int getIterationCount() {
            return 0;
        }
        public byte[] getSalt() {
            return new byte[0];
        }
        public char[] getPassword() {
            return new char[0];
        }
    }
}
