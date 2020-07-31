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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

public class DirectByteBufferTest extends ByteBufferTest {

    protected void setUp() throws Exception {
        super.setUp();
        buf = ByteBuffer.allocateDirect(BUFFER_LENGTH*Byte.BYTES);
        baseBuf = buf;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        buf = null;
        baseBuf = null;
    }

    /**
     * @tests java.nio.ByteBuffer#allocateDirect(int)
     */
    public void testAllocatedByteBuffer_IllegalArg() {
        try {
            ByteBuffer.allocateDirect(-1);
            fail("Should throw Exception"); //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testIsDirect() {
        assertTrue(buf.isDirect());
    }

    public void testHasArray() {
        // Android direct byte buffers have backing arrays.
        assertTrue(buf.hasArray());
        // assertFalse(buf.hasArray());
    }

    public void testIsReadOnly() {
        assertFalse(buf.isReadOnly());
    }

    // http://b/19692084
    // http://b/21491780
    public void testUnalignedReadsAndWrites() {
        // We guarantee that the first byte of the buffer is 8 byte aligned.
        ByteBuffer buf = ByteBuffer.allocateDirect(23);
        // Native order is always little endian, so this forces swaps.
        buf.order(ByteOrder.BIG_ENDIAN);

        for (int i = 0; i < 8; ++i) {
            buf.position(i);

            // 2 byte swaps.
            ShortBuffer shortBuf = buf.asShortBuffer();
            short[] shortArray = new short[]{42, 24};

            // Write.
            shortBuf.put(shortArray);
            // Read
            shortBuf.flip();
            shortBuf.get(shortArray);
            // Assert Equality
            assertEquals(42, shortArray[0]);
            assertEquals(24, shortArray[1]);

            buf.position(i);
            // 4 byte swaps.
            IntBuffer intBuf = buf.asIntBuffer();
            int[] intArray = new int[]{967, 1983};
            // Write.
            intBuf.put(intArray);
            // Read
            intBuf.flip();
            intBuf.get(intArray);
            // Assert Equality
            assertEquals(967, intArray[0]);
            assertEquals(1983, intArray[1]);


            buf.position(i);
            // 8 byte swaps.
            LongBuffer longBuf = buf.asLongBuffer();
            long[] longArray = new long[]{2147484614L, 2147485823L};
            // Write.
            longBuf.put(longArray);
            // Read
            longBuf.flip();
            longBuf.get(longArray);
            // Assert Equality
            assertEquals(2147484614L, longArray[0]);
            assertEquals(2147485823L, longArray[1]);
        }
    }

    public void testIsAccessible() {
        buf.clear();
        assertTrue(buf.isAccessible());
        buf.get(0);
        buf.setAccessible(false);
        try {
            buf.get(0);
            fail("should throw exception");
        } catch (IllegalStateException e) {
            // expected
        }
        buf.setAccessible(true);
        buf.get(0);
    }
}
