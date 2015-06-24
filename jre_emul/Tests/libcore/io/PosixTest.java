/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.io;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Unit tests for {@link libcore.io.Posix}.
 */
public class PosixTest extends TestCase {

  // Verify Issue 554 fix, where Posix.preadBytes used incorrect system call.
  public void testPreadBytes() throws Exception{
    final String testString = "hello, world!";
    byte[] bytesToWrite = testString.getBytes("UTF-8");
    ByteBuffer buf = ByteBuffer.allocate(bytesToWrite.length);

    File tmpFile = File.createTempFile("preadbug-", ".tmp");
    tmpFile.deleteOnExit();

    try (
        FileOutputStream fos = new FileOutputStream(tmpFile)) {
      fos.write(bytesToWrite);
    }

    try (
        RandomAccessFile raf = new RandomAccessFile(tmpFile, "r");
        FileChannel channel = raf.getChannel()) {
      channel.read(buf, 0);
    }

    String dstString = new String(buf.array(), "UTF-8");
    assertEquals(testString, dstString);
  }
}
