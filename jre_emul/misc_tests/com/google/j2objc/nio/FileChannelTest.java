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

package com.google.j2objc.nio;

import com.google.j2objc.annotations.AutoreleasePool;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import junit.framework.TestCase;

/**
 * Functional tests for correct memory behavior of FileChannel.
 *
 * @author Lukhnos Liu
 */
public class FileChannelTest extends TestCase {

  /**
   * Make sure that FileChannelImpl uses @RetainedWith on the stream object field. @Weak should not
   * be used there -- the problem being the channel one gets from FileOutputStream.getChannel() can
   * outlive the stream. When that happens, a dangling pointer can cause either of the two problems:
   * either the pointer is bad, and an EXC_BAD_ADDRESS is thrown right away; or (this is harder to
   * debug) if the stream object is stale, yet the closed flag is set, and the subsequent uses of
   * the channel throws out a ClosedChannelException because the stream closes itself upon its
   * finalize().
   */
  public void testFileChannelRetainedWithFileOutputStream() throws Exception {
    useFileChannel();
  }

  /**
   * A contrived ChannelGetter that autoreleases the FileOutputStream created from a File but
   * retains the resulting FileChannel.
   */
  static class ChannelGetter {
    FileChannel channel;

    @AutoreleasePool
    void createChannel(File f) throws IOException {
      FileOutputStream fos = new FileOutputStream(f);
      channel = fos.getChannel();
    }

    FileChannel get() {
      return channel;
    }
  }

  private void useFileChannel() throws IOException {
    File file = File.createTempFile("j2objc", "tmp");
    ChannelGetter channelGetter = new ChannelGetter();

    channelGetter.createChannel(file);
    // The FileOutputStream used to create the channel is released at this point.

    FileChannel channel = channelGetter.get();
    FileLock lock = channel.lock();
    lock.close();
    channel.close();
    assertTrue(file.delete());
  }
}
