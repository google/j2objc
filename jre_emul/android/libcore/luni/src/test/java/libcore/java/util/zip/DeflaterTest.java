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

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import junit.framework.TestCase;

public class DeflaterTest extends TestCase {

    private byte[] compressed = new byte[32];
    private byte[] decompressed = new byte[20];
    private Deflater deflater = new Deflater();
    private Inflater inflater = new Inflater();
    private int totalDeflated = 0;
    private int totalInflated = 0;

    public void testDeflate() throws DataFormatException {
        deflater.setInput(new byte[] { 1, 2, 3 });
        deflateInflate(Deflater.NO_FLUSH);
        assertTrue(totalInflated < 3);
        assertEquals(0, decompressed[2]); // the 3rd byte shouldn't have been flushed yet

        deflater.setInput(new byte[] { 4, 5, 6 });
        deflateInflate(Deflater.SYNC_FLUSH);
        assertEquals(6, totalInflated);
        assertDecompressed(1, 2, 3, 4, 5, 6);
        assertEquals(0, inflater.inflate(decompressed));

        deflater.setInput(new byte[] { 7, 8, 9 });
        deflateInflate(Deflater.FULL_FLUSH);
        assertEquals(9, totalInflated);
        assertDecompressed(1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertEquals(0, inflater.inflate(decompressed));
        inflater = new Inflater(true); // safe because we did a FULL_FLUSH

        deflater.setInput(new byte[] { 10, 11, 12 });
        deflateInflate(Deflater.SYNC_FLUSH);
        assertEquals(12, totalInflated);
        assertDecompressed(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
        assertEquals(0, inflater.inflate(decompressed));
    }

    private void deflateInflate(int flush) throws DataFormatException {
        int lastDeflated = deflater.deflate(compressed, totalDeflated,
                compressed.length - totalDeflated, flush);
        assertTrue(inflater.needsInput());
        inflater.setInput(compressed, totalDeflated, lastDeflated);
        totalDeflated += lastDeflated;
        totalInflated += inflater.inflate(decompressed, totalInflated,
                decompressed.length - totalInflated);
    }

    private void assertDecompressed(int... expected) {
        for (int i = 0; i < decompressed.length; i++) {
            int expectedValue = i < expected.length ? expected[i] : 0;
            assertEquals(expectedValue, decompressed[i]);
        }
    }

    /**
     * Deflating without calling setInput() is the same as deflating an empty
     * byte array.
     */
    public void testDeflateWithoutSettingInput() throws Exception {
        deflateInflate(Deflater.FULL_FLUSH);
        assertTrue(totalDeflated > 0); // the deflated form should be non-empty
        assertEquals(0, totalInflated);
    }
}
