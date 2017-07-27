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

package org.apache.harmony.tests.java.nio.channels.spi;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectionKey;

import junit.framework.TestCase;

public class AbstractSelectionKeyTest extends TestCase {

    /**
     * @tests AbstractSelectionKey#isValid() without selector
     */
    public void test_isValid() throws Exception {
        MockSelectionKey testKey = new MockSelectionKey();
        assertTrue(testKey.isValid());
    }

    /**
     * @tests AbstractSelectionKey#cancel
     */
    public void test_cancel() throws Exception {
        MockSelectionKey testKey = new MockSelectionKey();
        try {
            testKey.cancel();
            fail("should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected: no selector available
        }
        assertFalse(testKey.isValid());
    }

    private class MockSelectionKey extends AbstractSelectionKey {

        MockSelectionKey() {
            super();
        }

        public SelectableChannel channel() {
            return null;
        }

        public Selector selector() {
            return null;
        }

        public int interestOps() {
            return 0;
        }

        public SelectionKey interestOps(int arg0) {
            return null;
        }

        public int readyOps() {
            return 0;
        }
    }
}
