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

package com.google.j2objc.io;

import com.google.j2objc.annotations.AutoreleasePool;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;
import junit.framework.TestCase;

/*-[
@interface ComGoogleJ2objcIoAsyncPipedNSInputStreamAdapterTest_NativeInputStreamConsumer ()
    <NSStreamDelegate>
@end
]-*/

/**
 * Tests for AsyncPipedNSInputStreamAdapter.
 *
 * @author Lukhnos Liu
 */
public class AsyncPipedNSInputStreamAdapterTest extends TestCase {

  // Use a large data source to create some memory pressure, and make read and write buffer sizes
  // "misaligned" with the stream buffer size to test handling of leftover data.
  private static final int SOURCE_DATA_SIZE = 1024 * 1024;
  private static final int STREAM_BUFFER_SIZE = 128 * 1024;
  private static final int READ_BUFFER_SIZE = 120000;
  private static final int WRITE_CHUNK_SIZE = STREAM_BUFFER_SIZE + 1111;
  private static final int PARTIAL_SIZE = 212345;

  byte[] randomData;

  /**
   * An NSInputStream consumer that uses a run loop in the current thread to block until the
   * delegate method sees the end.
   */
  static class NativeInputStreamConsumer {
    final byte[] readBuffer = new byte[READ_BUFFER_SIZE];
    final int stopReadingAt;
    Object accumulatedData;

    NativeInputStreamConsumer() {
      stopReadingAt = -1;
    }

    NativeInputStreamConsumer(int stopReadingAt) {
      this.stopReadingAt = stopReadingAt;
    }

    native byte[] getBytes() /*-[
      return [IOSByteArray arrayWithNSData:(NSData *)accumulatedData_];
    ]-*/;

    native void readUntilEnd(Object inputStream) /*-[
      if (!accumulatedData_) {
        accumulatedData_ = [[NSMutableData alloc] init];
      }

      NSRunLoop *runLoop = [NSRunLoop currentRunLoop];
      [(NSInputStream *)inputStream setDelegate:self];
      [(NSInputStream *)inputStream scheduleInRunLoop:runLoop forMode:NSRunLoopCommonModes];
      [(NSInputStream *)inputStream open];
      CFRunLoopRun();
    ]-*/;

    /*-[
      - (void)closeAndQuit:(NSStream *)aStream {
        [aStream close];
        [aStream removeFromRunLoop:[NSRunLoop currentRunLoop] forMode:NSRunLoopCommonModes];

        // For NSInputStream, we need to call this explicitly to quit the runloop.
        CFRunLoopStop(CFRunLoopGetCurrent());
      }

      - (void)stream:(NSStream *)aStream handleEvent:(NSStreamEvent)eventCode {
        switch (eventCode) {
          case NSStreamEventNone:
          case NSStreamEventOpenCompleted:
          case NSStreamEventHasSpaceAvailable:
            break;
          case NSStreamEventErrorOccurred:
          case NSStreamEventEndEncountered:
            break;
          case NSStreamEventHasBytesAvailable: {
            if (stopReadingAt_ >= 0 && [accumulatedData_ length] >= stopReadingAt_) {
              [self closeAndQuit:aStream];
            } else {
              uint8_t *ptr = (uint8_t *)[readBuffer_ byteRefAtIndex:0];
              jint readSize = [readBuffer_ length];

              if (stopReadingAt_ >= 0) {
                jint remainder = stopReadingAt_ - (jint)[(NSMutableData *)accumulatedData_ length];
                if (remainder < readSize) {
                  readSize = remainder;
                }
              }

              NSInteger bytesRead = [(NSInputStream *)aStream read:ptr maxLength:readSize];
              if (bytesRead > 0) {
                [(NSMutableData *)accumulatedData_ appendBytes:(uint8_t *)ptr length:bytesRead];
                if (stopReadingAt_ >= 0 && [accumulatedData_ length] >= stopReadingAt_) {
                  [self closeAndQuit:aStream];
                }
              } else {
                [self closeAndQuit:aStream];
              }
            }
            break;
          }
        }
      }

    ]-*/
  }

  /**
   * An asynchronous data provider.
   */
  static class DataProvider implements AsyncPipedNSInputStreamAdapter.Delegate {
    int offset;
    final byte[] data;
    final int dataSize;

    DataProvider(byte[] data) {
      this.data = data;
      dataSize = this.data.length;
    }

    DataProvider(byte[] data, int stopWritingAt) {
      this.data = data;
      dataSize = stopWritingAt;
    }

    int getTotalWritten() {
      return offset;
    }

    @Override
    public void offerData(OutputStream stream) {
      try {
        int remaining = dataSize - offset;
        int len = (remaining > WRITE_CHUNK_SIZE) ? WRITE_CHUNK_SIZE : remaining;

        if (len == 0) {
          stream.close();
        } else {
          stream.write(data, offset, len);
          offset += len;
          if (offset >= dataSize) {
            stream.close();
          }
        }
      } catch (IOException e) {
        throw new AssertionError(e);
      }
    }

  }

  @Override
  protected void setUp() throws Exception {
    randomData = new byte[SOURCE_DATA_SIZE];
    new Random().nextBytes(randomData);
  }

  @Override
  protected void tearDown() throws Exception {
    // Reduce memory pressure.
    randomData = null;
  }

  @AutoreleasePool
  public void testFullWriteAndRead() {
    DataProvider provider = new DataProvider(randomData);
    NativeInputStreamConsumer consumer = new NativeInputStreamConsumer();
    Object stream = AsyncPipedNSInputStreamAdapter.create(provider, STREAM_BUFFER_SIZE);
    consumer.readUntilEnd(stream);

    assertTrue("The entire source is read", Arrays.equals(randomData, consumer.getBytes()));
  }

  @AutoreleasePool
  public void testNothingWritten() {
    DataProvider provider = new DataProvider(randomData, 0);
    NativeInputStreamConsumer consumer = new NativeInputStreamConsumer();
    Object stream = AsyncPipedNSInputStreamAdapter.create(provider, STREAM_BUFFER_SIZE);
    consumer.readUntilEnd(stream);
    assertEquals(0, provider.getTotalWritten());
    assertEquals(0, consumer.getBytes().length);
  }

  @AutoreleasePool
  public void testNothingRead() {
    DataProvider provider = new DataProvider(randomData);
    NativeInputStreamConsumer consumer = new NativeInputStreamConsumer(0);
    Object stream = AsyncPipedNSInputStreamAdapter.create(provider, STREAM_BUFFER_SIZE);
    consumer.readUntilEnd(stream);
    assertTrue("May provide more than actually read", provider.getTotalWritten() >= 0);
    assertEquals(0, consumer.getBytes().length);
  }

  @AutoreleasePool
  public void testPartialRead() {
    DataProvider provider = new DataProvider(randomData);
    NativeInputStreamConsumer consumer = new NativeInputStreamConsumer(PARTIAL_SIZE);
    Object stream = AsyncPipedNSInputStreamAdapter.create(provider, STREAM_BUFFER_SIZE);
    consumer.readUntilEnd(stream);
    assertTrue("May provide more than actually read", provider.getTotalWritten() >= PARTIAL_SIZE);
    assertEquals(PARTIAL_SIZE, consumer.getBytes().length);
    assertTrue(Arrays.equals(Arrays.copyOfRange(randomData, 0, PARTIAL_SIZE), consumer.getBytes()));
  }

  @AutoreleasePool
  public void testPartialWrite() {
    DataProvider provider = new DataProvider(randomData, PARTIAL_SIZE);
    NativeInputStreamConsumer consumer = new NativeInputStreamConsumer();
    Object stream = AsyncPipedNSInputStreamAdapter.create(provider, STREAM_BUFFER_SIZE);
    consumer.readUntilEnd(stream);
    assertEquals(PARTIAL_SIZE, provider.getTotalWritten());
    assertEquals(PARTIAL_SIZE, consumer.getBytes().length);
    assertTrue(
        "Part of the source is read",
        Arrays.equals(Arrays.copyOfRange(randomData, 0, PARTIAL_SIZE), consumer.getBytes()));
  }

  @AutoreleasePool
  public void testTrivialCreate() {
    DataProvider provider = new DataProvider(randomData);
    Object stream = AsyncPipedNSInputStreamAdapter.create(provider, STREAM_BUFFER_SIZE);
    assertNotNull(stream);

    // This is to test that the background thread created by the adapter is retaining the stream
    // objects properly. If it were not, the program would crash after this method exits and its
    // outer autorelease pool drains.
  }
}
