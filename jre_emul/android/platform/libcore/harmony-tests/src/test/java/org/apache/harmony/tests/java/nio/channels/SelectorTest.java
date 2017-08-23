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
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Set;
import junit.framework.TestCase;

/*
 * Tests for Selector and its default implementation
 */
public class SelectorTest extends TestCase {
    private static final int WAIT_TIME = 100;

    private SocketAddress localAddress;

    private Selector selector;

    private ServerSocketChannel ssc;

    private enum SelectType {
        NULL, TIMEOUT, NOW
    };

    protected void setUp() throws Exception {
        super.setUp();
        ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ServerSocket ss = ssc.socket();
        ss.bind(null);
        localAddress = ss.getLocalSocketAddress();
        selector = Selector.open();
    }

    protected void tearDown() throws Exception {
        try {
            ssc.close();
        } catch (Exception e) {
            // do nothing
        }
        try {
            selector.close();
        } catch (Exception e) {
            // do nothing
        }
        super.tearDown();
    }

    /**
     * @tests java.nio.channels.Selector#open()
     */
    public void test_open() throws IOException {
        assertNotNull(selector);
    }

    /**
     * @tests Selector#isOpen()
     */
    public void test_isOpen() throws IOException {
        assertTrue(selector.isOpen());
        selector.close();
        assertFalse(selector.isOpen());
    }

    /**
     * @tests java.nio.channels.Selector#provider()
     */
    public void test_provider() throws IOException {
        // should be system default provider
        assertNotNull(selector.provider());
        assertSame(SelectorProvider.provider(), selector.provider());
    }

    /**
     * @tests java.nio.channels.Selector#keys()
     */
    public void test_keys() throws IOException {
        SelectionKey key = ssc.register(selector, SelectionKey.OP_ACCEPT);

        Set<SelectionKey> keySet = selector.keys();
        Set<SelectionKey> keySet2 = selector.keys();

        assertSame(keySet, keySet2);
        assertEquals(1,keySet.size());
        SelectionKey key2 = keySet.iterator().next();
        assertEquals(key,key2);

        // Any attempt to modify keys will cause UnsupportedOperationException
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        SelectionKey key3 = sc.register(selector, SelectionKey.OP_READ);
        try {
            keySet2.add(key3);
            fail("should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        try {
            keySet2.remove(key3);
            fail("should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
        try {
            keySet2.clear();
            fail("should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }

        selector.close();
        try {
            selector.keys();
            fail("should throw ClosedSelectorException");
        } catch (ClosedSelectorException e) {
            // expected
        }
    }

    /**
     * @tests java.nio.channels.Selector#keys()
     */
    public void test_selectedKeys() throws IOException {
        SocketChannel sc = SocketChannel.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT);
        try {
            int count = 0;
            sc.connect(localAddress);
            count = blockingSelect(SelectType.NULL, 0);
            assertEquals(1, count);
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Set<SelectionKey> selectedKeys2 = selector.selectedKeys();
            assertSame(selectedKeys, selectedKeys2);

            assertEquals(1, selectedKeys.size());
            assertEquals(ssc.keyFor(selector), selectedKeys.iterator().next());
            // add one key into selectedKeys
            try {
                selectedKeys.add(ssc.keyFor(selector));
                fail("Should throw UnsupportedOperationException");
            } catch (UnsupportedOperationException e) {
                // expected
            }

            // no exception should be thrown
            selectedKeys.clear();

            Set<SelectionKey> selectedKeys3 = selector.selectedKeys();
            assertSame(selectedKeys, selectedKeys3);

            ssc.keyFor(selector).cancel();
            assertEquals(0, selectedKeys.size());
            selector.close();
            try {
                selector.selectedKeys();
                fail("should throw ClosedSelectorException");
            } catch (ClosedSelectorException e) {
                // expected
            }
        } finally {
            sc.close();
        }
    }

    /**
     * @tests java.nio.channel.Selector#selectNow()
     */
    public void test_selectNow() throws IOException {
        assert_select_OP_ACCEPT(SelectType.NOW, 0);
        assert_select_OP_CONNECT(SelectType.NOW, 0);
        assert_select_OP_READ(SelectType.NOW, 0);
        assert_select_OP_WRITE(SelectType.NOW, 0);
    }

    /**
     * @tests java.nio.channel.Selector#selectNow()
     */
    public void test_selectNow_SelectorClosed() throws IOException {
        assert_select_SelectorClosed(SelectType.NOW, 0);
    }

    public void test_selectNow_Timeout() throws IOException {
        // make sure selectNow doesn't block
        selector.selectNow();
    }

    // J2ObjC b/64848117
    /**
     * @tests java.nio.channel.Selector#select()
     */
//    public void test_select() throws IOException {
//        assert_select_OP_ACCEPT(SelectType.NULL, 0);
//        assert_select_OP_CONNECT(SelectType.NULL, 0);
//        assert_select_OP_READ(SelectType.NULL, 0);
//        assert_select_OP_WRITE(SelectType.NULL, 0);
//    }

    /**
     * @tests java.nio.channel.Selector#select()
     */
    public void test_select_SelectorClosed() throws IOException {
        assert_select_SelectorClosed(SelectType.NULL, 0);
    }

    // J2ObjC b/64848117
    /**
     * @tests java.nio.channel.Selector#select(long)
     */
//    public void test_selectJ() throws IOException {
//        assert_select_OP_ACCEPT(SelectType.TIMEOUT, 0);
//        assert_select_OP_CONNECT(SelectType.TIMEOUT, 0);
//        assert_select_OP_READ(SelectType.TIMEOUT, 0);
//        assert_select_OP_WRITE(SelectType.TIMEOUT, 0);
//
//        assert_select_OP_ACCEPT(SelectType.TIMEOUT, WAIT_TIME);
//        assert_select_OP_CONNECT(SelectType.TIMEOUT, WAIT_TIME);
//        assert_select_OP_READ(SelectType.TIMEOUT, WAIT_TIME);
//        assert_select_OP_WRITE(SelectType.TIMEOUT, WAIT_TIME);
//    }

    /**
     * @tests java.nio.channel.Selector#select(long)
     */
    public void test_selectJ_SelectorClosed() throws IOException {
        assert_select_SelectorClosed(SelectType.TIMEOUT, 0);
        selector = Selector.open();
        assert_select_SelectorClosed(SelectType.TIMEOUT, WAIT_TIME);
    }

    /**
     * @tests java.nio.channel.Selector#select(long)
     */
    public void test_selectJ_Exception() throws IOException {
        try {
            selector.select(-1);
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void test_selectJ_Timeout() throws IOException {
        // make sure select(timeout) doesn't block
        selector.select(WAIT_TIME);
    }

    public void test_selectJ_Empty_Keys() throws IOException {
        // regression test, see HARMONY-3888.
        // make sure select(long) does wait for specified amount of
        // time if keys.size() == 0 (initial state of selector).

        final long SELECT_TIMEOUT_MS = 2000;

        long t0 = System.nanoTime();
        selector.select(SELECT_TIMEOUT_MS);
        long t1 = System.nanoTime();

        long waitMs = (t1 - t0) / 1000L / 1000L;
        assertTrue(waitMs >= SELECT_TIMEOUT_MS);
        assertTrue(waitMs < 5*SELECT_TIMEOUT_MS);
    }

    /**
     * @tests java.nio.channels.Selector#wakeup()
     */
    public void test_wakeup() throws IOException {
        /*
         * make sure the test does not block on select
         */
        selector.wakeup();
        selectOnce(SelectType.NULL, 0);
        selector.wakeup();
        selectOnce(SelectType.TIMEOUT, 0);

        // try to wakeup select. The invocation sequence of wakeup and select
        // doesn't affect test result.
        new Thread() {
            public void run() {

                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                    // ignore
                }
                selector.wakeup();
            }
        }.start();
        selectOnce(SelectType.NULL, 0);

        // try to wakeup select. The invocation sequence of wakeup and select
        // doesn't affect test result.
        new Thread() {
            public void run() {

                try {
                    Thread.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                    // ignore
                }
                selector.wakeup();
            }
        }.start();
        selectOnce(SelectType.TIMEOUT, 0);
    }

    public void test_keySetViewsModifications() throws IOException {
        Set<SelectionKey> keys = selector.keys();

        SelectionKey key1 = ssc.register(selector, SelectionKey.OP_ACCEPT);

        assertTrue(keys.contains(key1));

        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        SelectionKey key2 = sc.register(selector, SelectionKey.OP_READ);

        assertTrue(keys.contains(key1));
        assertTrue(keys.contains(key2));

        key1.cancel();
        assertTrue(keys.contains(key1));

        selector.selectNow();
        assertFalse(keys.contains(key1));
        assertTrue(keys.contains(key2));
     }

    /**
     * This test cancels a key while selecting to verify that the cancelled
     * key set is processed both before and after the call to the underlying
     * operating system.
     */
    public void test_cancelledKeys() throws Exception {
        final AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
        final AtomicBoolean complete = new AtomicBoolean();

        final Pipe pipe = Pipe.open();
        pipe.source().configureBlocking(false);
        final SelectionKey key = pipe.source().register(selector, SelectionKey.OP_READ);

        Thread thread = new Thread() {
            public void run() {
                try {
                    // make sure to call key.cancel() while the main thread is selecting
                    Thread.sleep(500);
                    key.cancel();
                    assertFalse(key.isValid());
                    pipe.sink().write(ByteBuffer.allocate(4)); // unblock select()
                } catch (Throwable e) {
                    failure.set(e);
                } finally {
                    complete.set(true);
                }
            }
        };
        assertTrue(key.isValid());

        thread.start();
        do {
            assertEquals(0, selector.select(5000)); // blocks
            assertEquals(0, selector.selectedKeys().size());
        } while (!complete.get()); // avoid spurious interrupts
        assertFalse(key.isValid());

        thread.join();
        assertNull(failure.get());
    }

    public void testOpChange() throws Exception {
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_CONNECT);
        try {
            sc.connect(localAddress);
            int count = blockingSelect(SelectType.TIMEOUT, 100);
            assertEquals(1, count);
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            assertEquals(1, selectedKeys.size());
            SelectionKey key = selectedKeys.iterator().next();
            assertEquals(sc.keyFor(selector), key);
            assertEquals(SelectionKey.OP_CONNECT, key.readyOps());
            // select again, it should return 0
            count = selectOnce(SelectType.TIMEOUT, 100);
            assertEquals(0, count);
            // but selectedKeys remains the same as previous
            assertSame(selectedKeys, selector.selectedKeys());
            sc.finishConnect();

            // same selector, but op is changed
            SelectionKey key1 = sc.register(selector, SelectionKey.OP_WRITE);
            assertEquals(key, key1);
            count = blockingSelect(SelectType.TIMEOUT, 100);
            assertEquals(1, count);
            selectedKeys = selector.selectedKeys();
            assertEquals(1, selectedKeys.size());
            key = selectedKeys.iterator().next();
            assertEquals(key, key1);
            assertEquals(SelectionKey.OP_WRITE, key.readyOps());

            selectedKeys.clear();
        } finally {
            try {
                ssc.accept().close();
            } catch (Exception e) {
                // do nothing
            }
            try {
                sc.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    /* J2ObjC: not supported on iOS.
    public void test_nonBlockingConnect() throws IOException {
        SocketChannel channel = null;
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.register(selector, SelectionKey.OP_CONNECT);
            channel.connect(localAddress);
            channel.finishConnect();
            selector.select();
            assertEquals(0, selector.selectedKeys().size());
        } finally {
            channel.close();
        }
    }
    */

    private void assert_select_SelectorClosed(SelectType type, int timeout)
            throws IOException {
        // selector is closed
        selector.close();
        try {
            selectOnce(type, timeout);
            fail("should throw ClosedSelectorException");
        } catch (ClosedSelectorException e) {
            // expected
        }
    }

    private void assert_select_OP_ACCEPT(SelectType type, int timeout)
            throws IOException, ClosedChannelException {
        SocketChannel sc = SocketChannel.open();
        SocketChannel client = null;
        try {
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            sc.connect(localAddress);
            int count = blockingSelect(type, timeout);
            assertEquals(1, count);
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            assertEquals(1, selectedKeys.size());
            SelectionKey key = selectedKeys.iterator().next();
            assertEquals(ssc.keyFor(selector), key);
            assertEquals(SelectionKey.OP_ACCEPT, key.readyOps());
            // select again, it should return 0
            count = selectOnce(type, timeout);
            assertEquals(0,count);
            // but selectedKeys remains the same as previous
            assertSame(selectedKeys, selector.selectedKeys());
            client = ssc.accept();
            selectedKeys.clear();
        } finally {
            try {
                sc.close();
            } catch (IOException e) {
                // do nothing
            }
            if (null != client) {
                client.close();
            }
        }
        ssc.keyFor(selector).cancel();
    }

    private void assert_select_OP_CONNECT(SelectType type, int timeout)
            throws IOException, ClosedChannelException {
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_CONNECT);
        try {
            sc.connect(localAddress);
            int count = blockingSelect(type, timeout);
            assertEquals(1, count);
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            assertEquals(1, selectedKeys.size());
            SelectionKey key = selectedKeys.iterator().next();
            assertEquals(sc.keyFor(selector), key);
            assertEquals(SelectionKey.OP_CONNECT, key.readyOps());
            // select again, it should return 0
            count = selectOnce(type, timeout);
            assertEquals(0, count);
            // but selectedKeys remains the same as previous
            assertSame(selectedKeys, selector.selectedKeys());
            sc.finishConnect();
            selectedKeys.clear();
        } finally {
            try {
                ssc.accept().close();
            } catch (Exception e) {
                // do nothing
            }

            try {
                sc.close();
            } catch (IOException e) {
                // do nothing
            }
        }
    }

    private void assert_select_OP_READ(SelectType type, int timeout)
            throws IOException {
        SocketChannel sc = SocketChannel.open();
        SocketChannel client = null;
        SocketChannel sc2 = SocketChannel.open();
        SocketChannel client2 = null;
        try {
            ssc.configureBlocking(true);
            sc.connect(localAddress);
            client = ssc.accept();
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ);
            client.configureBlocking(true);

            sc2.connect(localAddress);
            client2 = ssc.accept();
            sc2.configureBlocking(false);
            sc2.register(selector, SelectionKey.OP_READ);
            client2.configureBlocking(true);

            client.write(ByteBuffer.wrap("a".getBytes()));
            int count = blockingSelect(type, timeout);
            assertEquals(1, count);
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            assertEquals(1, selectedKeys.size());
            SelectionKey key = selectedKeys.iterator().next();
            assertEquals(sc.keyFor(selector), key);
            assertEquals(SelectionKey.OP_READ, key.readyOps());
            // select again, it should return 0
            count = selectOnce(type, timeout);
            assertEquals(0, count);
            // but selectedKeys remains the same as previous
            assertSame(selectedKeys, selector.selectedKeys());

            sc.read(ByteBuffer.allocate(8));

            // the second SocketChannel should be selected this time
            client2.write(ByteBuffer.wrap("a".getBytes()));
            count = blockingSelect(type, timeout);
            assertEquals(1, count);
            // selectedKeys still includes the key of sc, because the key of sc
            // is not removed last time.
            selectedKeys = selector.selectedKeys();
            assertEquals(2, selectedKeys.size());
        } finally {
            if (null != client) {
                try {
                    client.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            if (null != client2) {
                try {
                    client2.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            try {
                sc.close();
            } catch (Exception e) {
                // ignore
            }
            try {
                sc2.close();
            } catch (Exception e) {
                // ignore
            }
            ssc.configureBlocking(false);
        }
    }

    private void assert_select_OP_WRITE(SelectType type, int timeout)
            throws IOException {
        SocketChannel sc = SocketChannel.open();
        SocketChannel client = null;
        try {
            sc.connect(localAddress);
            ssc.configureBlocking(true);
            client = ssc.accept();
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_WRITE);
            int count = blockingSelect(type, timeout);
            assertEquals(1, count);
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            assertEquals(1, selectedKeys.size());
            SelectionKey key = selectedKeys.iterator().next();
            assertEquals(sc.keyFor(selector), key);
            assertEquals(SelectionKey.OP_WRITE, key.readyOps());
            // select again, it should return 0
            count = selectOnce(type, timeout);
            assertEquals(0, count);
            // but selectedKeys remains the same as previous
            assertSame(selectedKeys, selector.selectedKeys());
        } finally {
            if (null != client) {
                client.close();
            }
            try {
                sc.close();
            } catch (IOException e) {
                // do nothing
            }
            ssc.configureBlocking(false);
        }
    }

    private int blockingSelect(SelectType type, int timeout) throws IOException {
        int ret = 0;
        do {
            ret = selectOnce(type, timeout);
            if (ret > 0) {
                return ret;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // ignore
            }
        } while (true);
    }

    private int selectOnce(SelectType type, int timeout) throws IOException {
        int ret = 0;
        switch (type) {
        case NULL:
            ret = selector.select();
            break;
        case TIMEOUT:
            ret = selector.select(timeout);
            break;
        case NOW:
            ret = selector.selectNow();
            break;
        }
        return ret;
    }

}
