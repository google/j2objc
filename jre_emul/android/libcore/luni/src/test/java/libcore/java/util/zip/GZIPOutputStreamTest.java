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

import junit.framework.TestCase;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

public final class GZIPOutputStreamTest extends TestCase {
  public void testShortMessage() throws IOException {
    byte[] data = gzip(("Hello World").getBytes("UTF-8"));
    assertEquals("[31, -117, 8, 0, 0, 0, 0, 0, 0, 0, -13, 72, -51, -55, -55, 87, 8, -49, " +
                 "47, -54, 73, 1, 0, 86, -79, 23, 74, 11, 0, 0, 0]", Arrays.toString(data));
  }

  public void testLongMessage() throws IOException {
    byte[] data = new byte[1024 * 1024];
    new Random().nextBytes(data);
    assertTrue(Arrays.equals(data, GZIPInputStreamTest.gunzip(gzip(data))));
  }

  public static byte[] gzip(byte[] bytes) throws IOException {
    ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
    OutputStream gzippedOut = new GZIPOutputStream(bytesOut);
    gzippedOut.write(bytes);
    gzippedOut.close();
    return bytesOut.toByteArray();
  }

  public void testSyncFlushEnabled() throws Exception {
    InputStream in = DeflaterOutputStreamTest.createInflaterStream(GZIPOutputStream.class, true);
    assertEquals(1, in.read());
    assertEquals(2, in.read());
    assertEquals(3, in.read());
    in.close();
  }

  public void testSyncFlushDisabled() throws Exception {
    InputStream in = DeflaterOutputStreamTest.createInflaterStream(GZIPOutputStream.class, false);
    try {
      in.read();
      fail();
    } catch (IOException expected) {
    }
    in.close();
  }

  // https://code.google.com/p/android/issues/detail?id=62589
  public void testFlushAfterFinish() throws Exception {
    byte[] responseBytes = "Some data to gzip".getBytes();
    ByteArrayOutputStream output = new ByteArrayOutputStream(responseBytes.length);
    GZIPOutputStream gzipOutputStream = new GZIPOutputStream(output, true);
    gzipOutputStream.write(responseBytes);
    gzipOutputStream.finish();
    // Calling flush() after finish() shouldn't throw.
    gzipOutputStream.flush();
    gzipOutputStream.close();
  }
}
