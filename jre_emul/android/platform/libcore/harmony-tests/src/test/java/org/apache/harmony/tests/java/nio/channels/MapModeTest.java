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

package org.apache.harmony.tests.java.nio.channels;

import java.nio.channels.FileChannel;

import junit.framework.TestCase;

/**
 * Tests for FileChannel.MapMode
 */
public class MapModeTest extends TestCase {

    /**
     * java.nio.channels.FileChannel.MapMode#PRIVATE,READONLY,READWRITE
     */
    public void test_PRIVATE_READONLY_READWRITE() {
        assertNotNull(FileChannel.MapMode.PRIVATE);
        assertNotNull(FileChannel.MapMode.READ_ONLY);
        assertNotNull(FileChannel.MapMode.READ_WRITE);

        assertFalse(FileChannel.MapMode.PRIVATE
                .equals(FileChannel.MapMode.READ_ONLY));
        assertFalse(FileChannel.MapMode.PRIVATE
                .equals(FileChannel.MapMode.READ_WRITE));
        assertFalse(FileChannel.MapMode.READ_ONLY
                .equals(FileChannel.MapMode.READ_WRITE));
    }

    /**
     * java.nio.channels.FileChannel.MapMode#toString()
     */
    public void test_toString() {
        assertNotNull(FileChannel.MapMode.PRIVATE.toString());
        assertNotNull(FileChannel.MapMode.READ_ONLY.toString());
        assertNotNull(FileChannel.MapMode.READ_WRITE.toString());
    }
}
