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

package libcore.java.nio.channels.spi;

import junit.framework.TestCase;

import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class AsynchronousChannelProviderTest extends TestCase {

    public void test_open_methods() throws Exception {
        AsynchronousChannelProvider provider = AsynchronousChannelProvider.provider();

        assertNotNull(provider);
        assertSame(AsynchronousChannelProvider.provider(), provider);

        assertNotNull(provider.openAsynchronousChannelGroup(1, new TestThreadFactory()));

        assertNotNull(provider.openAsynchronousChannelGroup(Executors.newSingleThreadExecutor(),
                1));

        assertNotNull(provider.openAsynchronousServerSocketChannel(
                AsynchronousChannelGroup.withFixedThreadPool(1, new TestThreadFactory())));

        assertNotNull(provider.openAsynchronousSocketChannel(
                AsynchronousChannelGroup.withFixedThreadPool(1, new TestThreadFactory())));

        assertNotNull(provider.openAsynchronousChannelGroup(1, new TestThreadFactory()));
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
