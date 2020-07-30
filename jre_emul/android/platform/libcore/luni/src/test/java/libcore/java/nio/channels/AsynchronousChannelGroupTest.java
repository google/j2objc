/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * limitations under the License
 */

package libcore.java.nio.channels;

import junit.framework.TestCase;

import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.ShutdownChannelGroupException;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


public class AsynchronousChannelGroupTest extends TestCase {

    // Maximum time to wait in seconds for the termination.
    private static final int TIMEOUT = 2;

    public void test_withFixedThreadPool() throws Exception {
        AsynchronousChannelGroup acg = AsynchronousChannelGroup.withFixedThreadPool(1,
                new TestThreadFactory());

        assertEquals(AsynchronousChannelProvider.provider(), acg.provider());
        assertFalse(acg.isShutdown());
        assertFalse(acg.isTerminated());

        // Close the channel.
        acg.shutdownNow();
    }

    public void test_withFixedThreadPool_IllegalArgumentException() throws Exception {
        try {
            int invalidPoolSize = -1;
            AsynchronousChannelGroup acg = AsynchronousChannelGroup.withFixedThreadPool(
                    invalidPoolSize, new TestThreadFactory());
            fail();
        } catch (IllegalArgumentException expected) {}
    }

    public void test_withCachedThreadPool() throws Exception {
        AsynchronousChannelGroup acg = AsynchronousChannelGroup.withCachedThreadPool(
                Executors.newFixedThreadPool(5), 1);

        assertEquals(AsynchronousChannelProvider.provider(), acg.provider());
        assertFalse(acg.isShutdown());
        assertFalse(acg.isTerminated());

        // Check with the negative initialSize value. It should not throw any error.
        AsynchronousChannelGroup.withCachedThreadPool(Executors.newFixedThreadPool(5), -1);

        // Close the channel.
        acg.shutdownNow();
    }

    public void test_withThreadPool() throws Exception {
        AsynchronousChannelGroup acg = AsynchronousChannelGroup.withThreadPool(
                Executors.newFixedThreadPool(5));

        assertEquals(AsynchronousChannelProvider.provider(), acg.provider());
        assertFalse(acg.isShutdown());
        assertFalse(acg.isTerminated());

        // Close the group.
        acg.shutdownNow();
    }

    public void test_shutdown() throws Exception {
        AsynchronousChannelGroup acg = AsynchronousChannelGroup.withCachedThreadPool(
                Executors.newFixedThreadPool(5), 1);

        // Bind a channel to the group.
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open(acg);

        // Shutdown channel group.
        acg.shutdown();

        assertTrue(acg.isShutdown());

        // Bounded channel should still be open.
        assertTrue(assc.isOpen());

        // It should not be possible to bind a new channel.
        try {
            AsynchronousServerSocketChannel.open(acg);
            fail();
        } catch (ShutdownChannelGroupException expected) {}

        // ExecutorService hasn't yet terminated as the channel is still open.
        assertFalse(acg.awaitTermination(2, TimeUnit.SECONDS));

        // Check invoking shutdown twice.
        acg.shutdown();

        // Close the group and the associated channel.
        acg.shutdownNow();
    }

    public void test_shutdownNow() throws Exception {
        AsynchronousChannelGroup acg = AsynchronousChannelGroup.withCachedThreadPool(
                Executors.newFixedThreadPool(5), 1);

        // Bind a channel to the group.
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open(acg);

        // Close the group as well as the channel bounded to it.
        acg.shutdownNow();

        assertTrue(acg.isShutdown());
        assertFalse(assc.isOpen());

        // It should not be possible to bind a new channel.
        try {
            AsynchronousServerSocketChannel.open(acg);
            fail();
        } catch (ShutdownChannelGroupException expected) {}

        acg.shutdownNow();
    }

    public void test_isTerminated() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        AsynchronousChannelGroup acg = AsynchronousChannelGroup.withCachedThreadPool(
                executorService, 1);

        // Bind a channel to the group.
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open(acg);

        assertFalse(acg.isTerminated());

        acg.shutdownNow();

        // Wait for the termination.
        assertTrue(acg.awaitTermination(TIMEOUT, TimeUnit.SECONDS));
        assertTrue(acg.isTerminated());
        assertTrue(executorService.isTerminated());
    }

    public void test_awaitTermination() throws Exception {
        AsynchronousChannelGroup acg = AsynchronousChannelGroup.withCachedThreadPool(
                Executors.newFixedThreadPool(5), 1);

        // Bind a channel to the group.
        AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open(acg);

        // Timeout as the group hasn't yet shutdown.
        assertFalse(acg.awaitTermination(TIMEOUT, TimeUnit.SECONDS));

        acg.shutdownNow();

        assertTrue(acg.awaitTermination(TIMEOUT, TimeUnit.SECONDS));
        assertTrue(acg.isTerminated());
    }

    private static class TestThreadFactory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(false);
            return t;
        }
    }
}
