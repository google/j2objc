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
package libcore.java.nio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import junit.framework.TestCase;

public final class OldDirectShortBufferTest extends TestCase {

    /**
     * Regression for http://code.google.com/p/android/issues/detail?id=3279
     */
    public void testPutWhenOffsetIsNonZero() {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(40);
        byteBuffer.order(ByteOrder.nativeOrder());
        ShortBuffer shortBuffer = byteBuffer.asShortBuffer();

        short[] source = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

        shortBuffer.put(source, 2, 2);
        shortBuffer.put(source, 4, 2);
        assertEquals(4, shortBuffer.get(0));
        assertEquals(5, shortBuffer.get(1));
        assertEquals(6, shortBuffer.get(2));
        assertEquals(7, shortBuffer.get(3));
    }
}
