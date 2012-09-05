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

package org.apache.harmony.nio.tests.java.nio;

import java.nio.ByteOrder;

import junit.framework.TestCase;

/**
 * Test java.nio.ByteOrder
 * 
 */
public class ByteOrderTest extends TestCase {

    public void testToString() {
        assertEquals(ByteOrder.BIG_ENDIAN.toString(), "BIG_ENDIAN");
        assertEquals(ByteOrder.LITTLE_ENDIAN.toString(), "LITTLE_ENDIAN");
    }

    public void testNativeOrder() {
        ByteOrder o = ByteOrder.nativeOrder();
        assertTrue(o == ByteOrder.BIG_ENDIAN || o == ByteOrder.LITTLE_ENDIAN);
    }

}
