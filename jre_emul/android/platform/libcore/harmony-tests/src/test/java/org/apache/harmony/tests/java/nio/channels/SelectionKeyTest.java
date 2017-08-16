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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import junit.framework.TestCase;

/*
 * Tests for SelectionKey and its default implementation
 */
public class SelectionKeyTest extends TestCase {

    Selector selector;

    SocketChannel sc;

    SelectionKey selectionKey;

    private static String LOCAL_ADDR = "127.0.0.1";

    protected void setUp() throws Exception {
        super.setUp();
        selector = Selector.open();
        sc = SocketChannel.open();
        sc.configureBlocking(false);
        selectionKey = sc.register(selector, SelectionKey.OP_CONNECT);
    }

    protected void tearDown() throws Exception {
        selectionKey.cancel();
        selectionKey = null;
        selector.close();
        selector = null;
        super.tearDown();
    }

    static class MockSelectionKey extends SelectionKey {
        private int interestOps;

        MockSelectionKey(int ops) {
            interestOps = ops;
        }

        public void cancel() {
            // do nothing
        }

        public SelectableChannel channel() {
            return null;
        }

        public int interestOps() {
            return 0;
        }

        public SelectionKey interestOps(int operations) {
            return null;
        }

        public boolean isValid() {
            return true;
        }

        public int readyOps() {
            return interestOps;
        }

        public Selector selector() {
            return null;
        }
    }

    /**
     * @tests java.nio.channels.SelectionKey#attach(Object)
     */
    public void test_attach() {
        MockSelectionKey mockSelectionKey = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        // no previous, return null
        Object o = new Object();
        Object check = mockSelectionKey.attach(o);
        assertNull(check);

        // null parameter is ok
        check = mockSelectionKey.attach(null);
        assertSame(o, check);

        check = mockSelectionKey.attach(o);
        assertNull(check);
    }

    /**
     * @tests java.nio.channels.SelectionKey#attachment()
     */
    public void test_attachment() {
        MockSelectionKey mockSelectionKey = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        assertNull(mockSelectionKey.attachment());
        Object o = new Object();
        mockSelectionKey.attach(o);
        assertSame(o, mockSelectionKey.attachment());
    }

    /**
     * @tests java.nio.channels.SelectionKey#channel()
     */
    public void test_channel() {
        assertSame(sc, selectionKey.channel());
        // can be invoked even canceled
        selectionKey.cancel();
        assertSame(sc, selectionKey.channel());
    }

    /**
     * @tests java.nio.channels.SelectionKey#interestOps()
     */
    public void test_interestOps() {
        assertEquals(SelectionKey.OP_CONNECT, selectionKey.interestOps());
    }

    /**
     * @tests java.nio.channels.SelectionKey#interestOps(int)
     */
    public void test_interestOpsI() {
        selectionKey.interestOps(SelectionKey.OP_WRITE);
        assertEquals(SelectionKey.OP_WRITE, selectionKey.interestOps());

        try {
            selectionKey.interestOps(SelectionKey.OP_ACCEPT);
            fail("should throw IAE.");
        } catch (IllegalArgumentException ex) {
            // expected;
        }

        try {
            selectionKey.interestOps(~sc.validOps());
            fail("should throw IAE.");
        } catch (IllegalArgumentException ex) {
            // expected;
        }
        try {
            selectionKey.interestOps(-1);
            fail("should throw IAE.");
        } catch (IllegalArgumentException ex) {
            // expected;
        }
        
    }

    /**
     * @tests java.nio.channels.SelectionKey#isValid()
     */
    public void test_isValid() {
        assertTrue(selectionKey.isValid());
    }

    /**
     * @tests java.nio.channels.SelectionKey#isValid()
     */
    public void test_isValid_KeyCancelled() {
        selectionKey.cancel();
        assertFalse(selectionKey.isValid());
    }

    /**
     * @tests java.nio.channels.SelectionKey#isValid()
     */
    public void test_isValid_ChannelColsed() throws IOException {
        sc.close();
        assertFalse(selectionKey.isValid());
    }

    /**
     * @tests java.nio.channels.SelectionKey#isValid()
     */
    public void test_isValid_SelectorClosed() throws IOException {
        selector.close();
        assertFalse(selectionKey.isValid());
    }

    /**
     * @tests java.nio.channels.SelectionKey#isAcceptable()
     */
    public void test_isAcceptable() throws IOException {
        MockSelectionKey mockSelectionKey1 = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        assertTrue(mockSelectionKey1.isAcceptable());
        MockSelectionKey mockSelectionKey2 = new MockSelectionKey(SelectionKey.OP_CONNECT);
        assertFalse(mockSelectionKey2.isAcceptable());
    }

    /**
     * @tests java.nio.channels.SelectionKey#isConnectable()
     */
    public void test_isConnectable() {
        MockSelectionKey mockSelectionKey1 = new MockSelectionKey(SelectionKey.OP_CONNECT);
        assertTrue(mockSelectionKey1.isConnectable());
        MockSelectionKey mockSelectionKey2 = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        assertFalse(mockSelectionKey2.isConnectable());
    }

    /**
     * @tests java.nio.channels.SelectionKey#isReadable()
     */
    public void test_isReadable() {
        MockSelectionKey mockSelectionKey1 = new MockSelectionKey(SelectionKey.OP_READ);
        assertTrue(mockSelectionKey1.isReadable());
        MockSelectionKey mockSelectionKey2 = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        assertFalse(mockSelectionKey2.isReadable());
    }

    /**
     * @tests java.nio.channels.SelectionKey#isWritable()
     */
    public void test_isWritable() {
        MockSelectionKey mockSelectionKey1 = new MockSelectionKey(SelectionKey.OP_WRITE);
        assertTrue(mockSelectionKey1.isWritable());
        MockSelectionKey mockSelectionKey2 = new MockSelectionKey(SelectionKey.OP_ACCEPT);
        assertFalse(mockSelectionKey2.isWritable());
    }

    /**
     * @tests java.nio.channels.SelectionKey#cancel()
     */
    public void test_cancel() {
        selectionKey.cancel();
        try {
            selectionKey.isAcceptable();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
            // expected
        }
        try {
            selectionKey.isConnectable();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
            // expected
        }
        try {
            selectionKey.isReadable();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
            // expected
        }
        try {
            selectionKey.isWritable();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
            // expected
        }
        
        try {
            selectionKey.readyOps();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
            // expected
        }
        
        try {
            selectionKey.interestOps(SelectionKey.OP_CONNECT);
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
            // expected
        }
        
        try {
            selectionKey.interestOps();
            fail("should throw CancelledKeyException.");
        } catch (CancelledKeyException ex) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.SelectionKey#readyOps()
     */
    public void test_readyOps() throws IOException {
        ServerSocket ss = new ServerSocket(0);
        try {
            sc.connect(new InetSocketAddress(LOCAL_ADDR, ss.getLocalPort()));
            assertEquals(0, selectionKey.readyOps());
            assertFalse(selectionKey.isConnectable());
            selector.select();
            assertEquals(SelectionKey.OP_CONNECT, selectionKey.readyOps());
        } finally {
            ss.close();
            ss = null;
        }
      
    }

    /**
     * @tests java.nio.channels.SelectionKey#selector()
     */
    public void test_selector() {
        assertSame(selector, selectionKey.selector());
        selectionKey.cancel();
        assertSame(selector, selectionKey.selector());
    }
}
