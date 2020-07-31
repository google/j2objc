/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.harmony.tests.java.nio;

import java.nio.DoubleBuffer;

public class HeapDoubleBufferTest extends DoubleBufferTest {
    protected void setUp() throws Exception {
        super.setUp();
        buf = DoubleBuffer.allocate(BUFFER_LENGTH); 
        loadTestData1(buf);
        baseBuf = buf;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        buf = null;
        baseBuf = null;
    }
    
    public void testAllocatedDoubleBuffer_IllegalArg() {
        try {
            DoubleBuffer.allocate(-1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
