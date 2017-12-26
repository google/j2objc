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

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import junit.framework.TestCase;

/*
 * Tests for SelectableChannel
 */
public class SelectableChannelTest extends TestCase {

    /**
     * @tests SelectableChannel#register(Selector, int)
     */
    public void test_register_LSelectorI() throws IOException {
        MockSelectableChannel msc = new MockSelectableChannel();
        // Verify that calling register(Selector, int) leads to the method
        // register(Selector, int, Object) being called with a null value 
        // for the third argument.
        msc.register(Selector.open(), SelectionKey.OP_ACCEPT);
        assertTrue(msc.isCalled);
    }

    private class MockSelectableChannel extends SelectableChannel {

        private boolean isCalled = false;

        public Object blockingLock() {
            return null;
        }

        public SelectableChannel configureBlocking(boolean block)
                throws IOException {
            return null;
        }

        public boolean isBlocking() {
            return false;
        }

        public boolean isRegistered() {
            return false;
        }

        public SelectionKey keyFor(Selector sel) {
            return null;
        }

        public SelectorProvider provider() {
            return null;
        }

        public SelectionKey register(Selector sel, int ops, Object att)
                throws ClosedChannelException {
            if (null == att) {
                isCalled = true;
            }
            return null;
        }

        public int validOps() {
            return 0;
        }

        protected void implCloseChannel() throws IOException {
            // empty
        }
    }
}
