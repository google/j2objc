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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.Pipe;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.concurrent.Future;
import junit.framework.TestCase;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


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

    public void testInputStreamAsynchronousByteChannel() throws Exception {
        AsynchronousByteChannel abc = mock(AsynchronousByteChannel.class);
        InputStream is = Channels.newInputStream(abc);
        Future<Integer> result = mock(Future.class);
        ArgumentCaptor<ByteBuffer> bbCaptor = ArgumentCaptor.forClass(ByteBuffer.class);
        final byte[] bytesRead = new byte[10];

        when(abc.read(bbCaptor.capture())).thenReturn(result);
        when(result.get()).thenAnswer(
            new Answer<Integer>() {
                public Integer answer(InvocationOnMock invocation) {
                    ByteBuffer bb = bbCaptor.getValue();
                    assertEquals(bytesRead.length, bb.remaining());
                    // Write '7' bytes
                    bb.put(new byte[] {0, 1, 2, 3, 4, 5, 6});
                    return 7;
                }
            });

        assertEquals(7, is.read(bytesRead));
        // Only 7 bytes of data should be written into the buffer
        byte[] bytesExpected = new byte[] { 0, 1, 2, 3, 4, 5, 6, 0, 0, 0 };
        assertTrue(Arrays.equals(bytesExpected, bytesRead));

        Mockito.verify(abc).read(isA(ByteBuffer.class));
        Mockito.verify(result).get();
    }

    public void testOutputStreamAsynchronousByteChannel() throws Exception {
        AsynchronousByteChannel abc = mock(AsynchronousByteChannel.class);
        OutputStream os = Channels.newOutputStream(abc);
        Future<Integer> result = mock(Future.class);
        ArgumentCaptor<ByteBuffer> bbCaptor = ArgumentCaptor.forClass(ByteBuffer.class);
        final byte[] data = "world".getBytes();

        when(abc.write(bbCaptor.capture())).thenReturn(result);
        when(result.get()).thenAnswer(
            new Answer<Integer>() {
                public Integer answer(InvocationOnMock invocation) {
                    ByteBuffer bb = bbCaptor.getValue();
                    assertEquals(data.length, bb.remaining());
                    byte[] readData = new byte[data.length];
                    // Read the whole thing
                    bb.get(readData);
                    assertTrue(Arrays.equals(data, readData));
                    return data.length;
                }
            });

        os.write(data);

        Mockito.verify(abc).write(isA(ByteBuffer.class));
        Mockito.verify(result).get();
  }

}
