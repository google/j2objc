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

package libcore.java.util.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;
import junit.framework.TestCase;

public class DeflaterOutputStreamTest extends TestCase {

    public void testSyncFlushEnabled() throws Exception {
        InputStream in = createInflaterStream(DeflaterOutputStream.class, true);
        assertEquals(1, in.read());
        assertEquals(2, in.read());
        assertEquals(3, in.read());
        in.close();
    }

    public void testSyncFlushDisabled() throws Exception {
        InputStream in = createInflaterStream(DeflaterOutputStream.class, false);
        try {
            in.read();
            fail();
        } catch (IOException expected) {
        }
        in.close();
    }

    /**
     * Creates an optionally-flushing deflater stream, writes some bytes to it,
     * and flushes it. Returns an inflater stream that reads this deflater's
     * output.
     *
     * <p>These bytes are written on a separate thread so that when the inflater
     * stream is read, that read will fail when no bytes are available. Failing
     * takes 3 seconds, co-ordinated by PipedInputStream's 'broken pipe'
     * timeout. The 3 second delay is unfortunate but seems to be the easiest
     * way demonstrate that data is unavailable. Ie. other techniques will cause
     * the dry read to block indefinitely.
     */
    static InputStream createInflaterStream(final Class<?> c, final boolean flushing) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final PipedOutputStream pout = new PipedOutputStream();
        PipedInputStream pin = new PipedInputStream(pout);

        executor.submit(new Callable<Void>() {
            public Void call() throws Exception {
                OutputStream out;
                if (c == DeflaterOutputStream.class) {
                    out = new DeflaterOutputStream(pout, flushing);
                } else if (c == GZIPOutputStream.class) {
                    out = new GZIPOutputStream(pout, flushing);
                } else {
                    throw new AssertionError();
                }
                out.write(1);
                out.write(2);
                out.write(3);
                out.flush();
                return null;
            }
        }).get();
        executor.shutdown();

        if (c == DeflaterOutputStream.class) {
            return new InflaterInputStream(pin);
        } else if (c == GZIPOutputStream.class) {
            return new GZIPInputStream(pin);
        } else {
            throw new AssertionError();
        }
    }

    /**
     * Confirm that a DeflaterOutputStream constructed with Deflater
     * with flushParm == SYNC_FLUSH does not need to to be flushed.
     *
     * http://b/4005091
     */
    public void testSyncFlushDeflater() throws Exception {
        Deflater def = new Deflater();
        Field f = def.getClass().getDeclaredField("flushParm");
        f.setAccessible(true);
        f.setInt(def, Deflater.SYNC_FLUSH);

        final int deflaterBufferSize = 512;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos, def, deflaterBufferSize);

        // make output buffer large enough that even if compressed it
        // won't all fit within the deflaterBufferSize.
        final int outputBufferSize = 128 * deflaterBufferSize;
        byte[] output = new byte[outputBufferSize];
        for (int i = 0; i < output.length; i++) {
            output[i] = (byte) i;
        }
        dos.write(output);
        byte[] compressed = baos.toByteArray();
        // this main reason for this assert is to make sure that the
        // compressed byte count is larger than the
        // deflaterBufferSize. However, when the original bug exists,
        // it will also fail because the compressed length will be
        // exactly the length of the deflaterBufferSize.
        assertTrue("compressed=" + compressed.length
                   + " but deflaterBufferSize=" + deflaterBufferSize,
                   compressed.length > deflaterBufferSize);

        // assert that we returned data matches the input exactly.
        ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
        InflaterInputStream iis = new InflaterInputStream(bais);
        byte[] input = new byte[output.length];
        int total = 0;
        while (true)  {
            int n = iis.read(input, total, input.length - total);
            if (n == -1) {
                break;
            }
            total += n;
            if (total == input.length) {
                try {
                    iis.read();
                    fail();
                } catch (EOFException expected) {
                    break;
                }
            }
        }
        assertEquals(output.length, total);
        assertTrue(Arrays.equals(input, output));

        // ensure Deflater.finish has not been called at any point
        // during the test, since that would lead to the results being
        // flushed even without SYNC_FLUSH being used
        assertFalse(def.finished());

        // Quieten CloseGuard.
        def.end();
        iis.close();
    }
}
