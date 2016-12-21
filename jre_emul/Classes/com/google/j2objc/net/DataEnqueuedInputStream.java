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

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An InputStream backed by a LinkedBlockingQueue to allow offering and polling data chunks from
 * different threads. This is designed to be used with NSURLSession, which offers data in immutable
 * NSData chunks and handles timeout.
 *
 * <p>We use an unbounded LinkedBlockingQueue since the assumuption is that the offering side (data
 * from network) is slower than the polling side (the InputStream consumer). We use take() to poll
 * the queue, which blocks until the next chunk is available. This is ok, because we use an empty
 * byte array to signal that no more data is available. Once a currently blocking poll sees that
 * empty byte array, the closed flag is turned to true, and subsequent polls will bail.
 *
 * <p>To be able to pass network errors to the InputStream consumers when they call read(), we
 * require that the offerer calls {@link endOffering(IOException)} if the connection ends in an
 * error. Subsequent read() calls will throw that exception.
 *
 * <p>Although this InputStream is thread-safe, only one thread should be reading from the stream at
 * any given time.
 *
 * @author Lukhnos Liu
 */
class DataEnqueuedInputStream extends InputStream {
  /** A chunk that signals no more data is available in the queue. */
  private static final byte[] CLOSED = new byte[0];

  private final LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();
  private Object closedGuard = new Object();
  private boolean closed;
  private byte[] currentChunk;
  private int currentChunkReadPos = -1;
  private IOException exception;

  /** Offers a chunk of data to the queue. */
  void offerData(byte[] chunk) {
    if (chunk == null || chunk.length == 0) {
      throw new IllegalArgumentException("chunk must have at least one byte of data");
    }

    // If already closed, don't enqueue the chunk.
    synchronized (closedGuard) {
      if (closed) {
        return;
      }
    }

    queue.offer(chunk);
  }

  /**
   * Signals that no more data is available without errors. It is ok to call this multiple times.
   */
  void endOffering() {
    endOffering(null);
  }

  /** Signals that no more data is available and an exception should be thrown. */
  void endOffering(IOException exception) {
    synchronized (closedGuard) {
      if (closed) {
        return;
      }
      this.exception = exception;
    }

    queue.offer(CLOSED);
  }

  @Override
  public void close() throws IOException {
    endOffering();
  }

  @Override
  public int read() throws IOException {
    byte[] b = new byte[1];
    int res = read(b, 0, 1);
    return (res != -1) ? b[0] & 0xff : -1;
  }

  @Override
  public int read(byte[] buf) throws IOException {
    return read(buf, 0, buf.length);
  }

  @Override
  public synchronized int read(byte[] buf, int offset, int length) throws IOException {
    if (buf == null) {
      throw new IllegalArgumentException("buf must not be null");
    }

    if (!(offset >= 0 && length > 0 && offset < buf.length && length <= (buf.length - offset))) {
      throw new IllegalArgumentException("invalid offset and lengeth");
    }

    // Return early if closed is true; throw the saved exception if needed.
    synchronized (closedGuard) {
      if (closed) {
        if (exception != null) {
          throw exception;
        }
        return -1;
      }
    }

    // Check if there is already a chunk that hasn't been exhausted; call take() if not.
    if (currentChunk == null) {
      if (currentChunkReadPos != -1) {
        throw new IllegalStateException("currentChunk is null but currentChunkReadPos is not -1");
      }

      byte[] next;
      try {
        // It's ok the block on this. See the top-level Javadoc for explanations.
        next = queue.take();
      } catch (InterruptedException e) {
        // Unlikely to happen.
        throw new AssertionError(e);
      }

      // If it's the end marker, acknowledge that so that the buffer will mark itself as closed
      // for reading.
      if (next == CLOSED) {
        synchronized (closedGuard) {
          closed = true;
        }
        if (exception != null) {
          throw exception;
        }
        return -1;
      }
      currentChunk = next;
      currentChunkReadPos = 0;
    }

    int available = currentChunk.length - currentChunkReadPos;
    if (length < available) {
      // Copy from the currentChunk.
      System.arraycopy(currentChunk, currentChunkReadPos, buf, offset, length);
      currentChunkReadPos += length;
      return length;
    } else {
      // Copy the entire currentChunk.
      System.arraycopy(currentChunk, currentChunkReadPos, buf, offset, available);
      currentChunk = null;
      currentChunkReadPos = -1;
      return available;
    }
  }
}
