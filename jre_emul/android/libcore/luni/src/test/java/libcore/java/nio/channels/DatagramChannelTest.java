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

import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class DatagramChannelTest extends junit.framework.TestCase {
    public void test_read_intoReadOnlyByteArrays() throws Exception {
        ByteBuffer readOnly = ByteBuffer.allocate(1).asReadOnlyBuffer();
        DatagramSocket ds = new DatagramSocket(0);
        DatagramChannel dc = DatagramChannel.open();
        dc.connect(ds.getLocalSocketAddress());
        try {
            dc.read(readOnly);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            dc.read(new ByteBuffer[] { readOnly });
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            dc.read(new ByteBuffer[] { readOnly }, 0, 1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    // http://code.google.com/p/android/issues/detail?id=16579
    public void testNonBlockingRecv() throws Exception {
        DatagramChannel dc = DatagramChannel.open();
        try {
            dc.configureBlocking(false);
            dc.socket().bind(null);
            // Should return immediately, since we're non-blocking.
            assertNull(dc.receive(ByteBuffer.allocate(2048)));
        } finally {
            dc.close();
        }
    }
}
