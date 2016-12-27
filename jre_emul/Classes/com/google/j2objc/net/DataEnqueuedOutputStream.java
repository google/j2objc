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

package com.google.j2objc.net;

import com.google.j2objc.io.AsyncPipedNSInputStreamAdapter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * An OutputStream backed by a one-element LinkedBlockingQueue and piped to an asynchronous
 * NSInputStream. A one-element LinkedBlockingQueue gives us a timeout mechanism when the offering
 * side works faster than the polling side, which is what happens when we use the OutputStream to
 * write upload data to the network. The async, piped NSInputStream then calls the delegate method
 * implemented here (see {@link com.google.j2objc.io.AsyncPipedNSInputStreamAdapter.Delegate} for
 * details) to poll the queue to obtain the next data chunk, and the NSInputStream is consumed by an
 * NSURLSession's data task as the HTTP request body stream.
 *
 * <p>Since the LinkedBlockingQueue only has one element, if the currently enqueued sole chunk
 * cannot be consumed in time, the next offer() call will timeout. We throw a SocketTimeoutException
 * to the caller of write() and mark the stream as closed.
 *
 * <p>Although this OutputStream is thread-safe, only one thread should be writing to the stream at
 * any given time.
 *
 * @author Lukhnos Liu
 */
class DataEnqueuedOutputStream extends OutputStream
    implements AsyncPipedNSInputStreamAdapter.Delegate {
  /** A chunk that signals no more data is available in the queue. */
  private static final byte[] CLOSED = new byte[0];

  private final LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<>(1);
  private final long timeoutMillis;
  private volatile boolean closedByWriter;

  /**
   * Create an OutputStream with timeout.
   *
   * @param timeoutMillis timeout in millis. If it's negative, the writes will never time out.
   */
  DataEnqueuedOutputStream(long timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
  }

  @Override
  public synchronized void close() throws IOException {
    if (closedByWriter) {
      return;
    }

    try {
      // Enqueue the CLOSED marker with timeout. When that happens, the polling side will also
      // eventually timeout.
      if (!queue.offer(CLOSED, timeoutMillis, TimeUnit.MILLISECONDS)) {
        throw new SocketTimeoutException();
      }
    } catch (InterruptedException e) {
      // Unlikely to happen.
      throw new AssertionError(e);
    }

    closedByWriter = true;
  }

  @Override
  public void write(int b) throws IOException {
    write(new byte[] {(byte) b}, 0, 1);
  }

  @Override
  public void write(byte[] buf) throws IOException {
    write(buf, 0, buf.length);
  }

  @Override
  public void write(byte[] buf, int offset, int length) throws IOException {
    if (buf == null) {
      throw new IllegalArgumentException("buf must not be null");
    }

    if (!(offset >= 0 && length >= 0 && offset < buf.length && length <= (buf.length - offset))) {
      throw new IllegalArgumentException("invalid offset and lengeth");
    }

    if (closedByWriter) {
      throw new IOException("stream already closed");
    }

    if (length == 0) {
      return;
    }

    try {
      // Make sure to enqueue a copy; it is possible for the writer to reuse the buffer, and not
      // enqueuing a copy would result in overwritten and therefore corrupt data.
      byte[] chunk = Arrays.copyOfRange(buf, offset, offset + length);

      if (timeoutMillis >= 0) {
        if (!queue.offer(chunk, timeoutMillis, TimeUnit.MILLISECONDS)) {
          throw new SocketTimeoutException();
        }
      } else {
        queue.put(chunk);
      }
    } catch (InterruptedException e) {
      // Unlikely to happen.
      throw new AssertionError(e);
    }
  }

  /** Offers data from the queue to the OutputStream piped to the adapted NSInputStream. */
  @Override
  public void offerData(OutputStream stream) {
    byte[] next = null;
    try {
      next = queue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
    } catch (InterruptedException ignored) {
      // If this ever times out (unlikely given our assumption), next stays null.
    }

    try {
      // Only if next is not null nor CLOSED do we write the chunk.
      if (next != null && next != CLOSED) {
        stream.write(next);
        return;
      }
    } catch (IOException ignored) {
      // Any errors in the piped (NS)OutputStream (unlikely) fall through to the next section.
    }

    // Close the piped (NS)OutputStream and ignore any errors.
    try {
      stream.close();
    } catch (IOException ignored) {
      // Ignored.
    }
  }
}
