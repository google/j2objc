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
import java.nio.channels.Channels;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.Pipe;
import java.nio.channels.WritableByteChannel;
import junit.framework.TestCase;

public final class ChannelsTest extends TestCase {

    public void testStreamNonBlocking() throws IOException {
        Pipe.SourceChannel sourceChannel = createNonBlockingChannel("abc".getBytes("UTF-8"));
        try {
            Channels.newInputStream(sourceChannel).read();
            fail();
        } catch (IllegalBlockingModeException expected) {
        }
    }

    /**
     * This fails on the RI which violates its own promise to throw when
     * read in non-blocking mode.
     */
    public void testReaderNonBlocking() throws IOException {
        Pipe.SourceChannel sourceChannel = createNonBlockingChannel("abc".getBytes("UTF-8"));
        try {
            Channels.newReader(sourceChannel, "UTF-8").read();
            fail();
        } catch (IllegalBlockingModeException expected) {
        }
    }

    private Pipe.SourceChannel createNonBlockingChannel(byte[] content) throws IOException {
        Pipe pipe = Pipe.open();
        WritableByteChannel sinkChannel = pipe.sink();
        sinkChannel.write(ByteBuffer.wrap(content));
        Pipe.SourceChannel sourceChannel = pipe.source();
        sourceChannel.configureBlocking(false);
        return sourceChannel;
    }
}

