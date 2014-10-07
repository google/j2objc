/*
 * Copyright (C) 2011 The Android Open Source Project
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

import java.nio.channels.ServerSocketChannel;

public class ServerSocketChannelTest extends junit.framework.TestCase {
    // http://code.google.com/p/android/issues/detail?id=16579
    public void testNonBlockingAccept() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        try {
            ssc.configureBlocking(false);
            ssc.socket().bind(null);
            // Should return immediately, since we're non-blocking.
            assertNull(ssc.accept());
        } finally {
            ssc.close();
        }
    }
}
