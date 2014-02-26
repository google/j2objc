/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libcore.java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import libcore.io.Libcore;
import libcore.io.OsConstants;
import junit.framework.TestCase;
import tests.net.StuckServer;

public class SelectorTest extends TestCase {
    public void testNonBlockingConnect_immediate() throws Exception {
        // Test the case where we [probably] connect immediately.
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();
        try {
            ssc.configureBlocking(false);
            ssc.socket().bind(null);

            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.connect(ssc.socket().getLocalSocketAddress());
            SelectionKey key = sc.register(selector, SelectionKey.OP_CONNECT);
            assertEquals(1, selector.select());
            assertEquals(SelectionKey.OP_CONNECT, key.readyOps());
            sc.finishConnect();
        } finally {
            selector.close();
            ssc.close();
        }
    }

    public void testNonBlockingConnect_slow() throws Exception {
        // Test the case where we have to wait for the connection.
        Selector selector = Selector.open();
        StuckServer ss = new StuckServer(true);
        try {
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.connect(ss.getLocalSocketAddress());
            SelectionKey key = sc.register(selector, SelectionKey.OP_CONNECT);
            assertEquals(1, selector.select());
            assertEquals(SelectionKey.OP_CONNECT, key.readyOps());
            sc.finishConnect();
        } finally {
            selector.close();
            ss.close();
        }
    }

    // This test won't work on the host until/unless we start using libcorkscrew there.
    // The runtime itself blocks SIGQUIT, so that doesn't cause poll(2) to EINTR directly.
    // The EINTR is caused by the way libcorkscrew works.
    public void testEINTR() throws Exception {
        Selector selector = Selector.open();
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    Thread.sleep(2000);
                    Libcore.os.kill(Libcore.os.getpid(), OsConstants.SIGQUIT);
                } catch (Exception ex) {
                    fail();
                }
            }
        }).start();
        assertEquals(0, selector.select());
    }

    // http://code.google.com/p/android/issues/detail?id=15388
    public void testInterrupted() throws IOException {
        Selector selector = Selector.open();
        try {
            Thread.currentThread().interrupt();
            int count = selector.select();
            assertEquals(0, count);
        } finally {
            selector.close();
        }
    }

    public void testManyWakeupCallsTriggerOnlyOneWakeup() throws Exception {
        final Selector selector = Selector.open();
        try {
            selector.wakeup();
            selector.wakeup();
            selector.wakeup();
            selector.select();

            // create a latch that will reach 0 when select returns
            final CountDownLatch selectReturned = new CountDownLatch(1);
            Thread thread = new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        selector.select();
                        selectReturned.countDown();
                    } catch (IOException ignored) {
                    }
                }
            });
            thread.start();

            // select doesn't ever return, so await() times out and returns false
            assertFalse(selectReturned.await(2, TimeUnit.SECONDS));
        } finally {
            selector.close();
        }
    }

    // We previously leaked a file descriptor for each selector instance created.
    //
    // http://code.google.com/p/android/issues/detail?id=5993
    // http://code.google.com/p/android/issues/detail?id=4825
    public void testLeakingPipes() throws IOException {
        for (int i = 0; i < 2000; i++) {
            Selector selector = Selector.open();
            selector.close();
        }
    }

    public void test_57456() throws Exception {
      Selector selector = Selector.open();
      ServerSocketChannel ssc = ServerSocketChannel.open();

      try {
        // Connect.
        ssc.configureBlocking(false);
        ssc.socket().bind(null);
        SocketChannel sc = SocketChannel.open();
        sc.connect(ssc.socket().getLocalSocketAddress());
        sc.finishConnect();

        // Switch to non-blocking so we can use a Selector.
        sc.configureBlocking(false);

        // Have the 'server' write something.
        ssc.accept().write(ByteBuffer.allocate(128));

        // At this point, the client should be able to read or write immediately.
        // (It shouldn't be able to connect because it's already connected.)
        SelectionKey key = sc.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        assertEquals(1, selector.select());
        assertEquals(SelectionKey.OP_READ | SelectionKey.OP_WRITE, key.readyOps());
        assertEquals(0, selector.select());
      } finally {
        selector.close();
        ssc.close();
      }
    }
}
