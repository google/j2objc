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
import java.net.SocketTimeoutException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * An InputStream backed by a LinkedBlockingQueue to allow offering and polling data chunks from
 * different threads. This is designed to be used with NSURLSession, which offers data in immutable
 * NSData chunks and handles timeout.
 *
 * <p>We use an unbounded LinkedBlockingQueue since the assumuption is that the offering side (data
 * from network) is slower than the polling side (the InputStream consumer). If the CLOSED marker
 * is offered to the queue and the polling side sees the array, the stream is marked as closed. To
 * be defensive, the polling may time out.
 *
 * <p>To be able to pass network errors to the InputStream consumers when they call read(), we
 * require that the offerer calls {@link #endOffering(IOException)} if the connection ends in an
 * error. Subsequent read() calls will throw that exception.
 *
 * <p>Data can be offered to and read from the stream from any thread. The thread safety is
 * guaranteed by the synchronized read method as well as the fact that we want LinkedBlockingQueue
 * to block when polling if the queue is empty. Closing the stream from the offering side must offer
 * CLOSED, and that guarantees that any pending read will encounter that end marker. If multiple
 * {@link #endOffering()} and {@link #close()} are called, it is possible that extra CLOSED markers
 * would be enqueued before the polling side or the {@link #close()} method could set the closed
 * boolean flag to true, but that reduces the total number of locks used, and it's a tradeoff that
 * this InputStream accepts in its design.
 *
 * @author Lukhnos Liu
 */
class DataEnqueuedInputStream extends InputStream {
  /** A chunk that signals no more data is available in the queue. */
  private static final byte[] CLOSED = new byte[0];

  private final long timeoutMillis;
  private final LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue<>();
  private volatile boolean closed;
  private byte[] currentChunk;
  private int currentChunkReadPos = -1;
  private volatile IOException exception;

  /**
   * Create an InputStream with timeout.
   *
   * @param timeoutMillis timeout in millis. If it's negative, the reads will never time out.
   */
  DataEnqueuedInputStream(long timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
  }

  /** Offers a chunk of data to the queue. */
  void offerData(byte[] chunk) {
    if (chunk == null || chunk.length == 0) {
      throw new IllegalArgumentException("chunk must have at least one byte of data");
    }

    if (closed) {
      return;
    }

    // Since this is an unbounded queue, offer() always succeeds. In addition, we don't make a copy
    // of the chunk, as the data source (NSURLSessionDataTask) does not reuse the underlying byte
    // array of a chunk when the chunk is alive.
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
    if (closed) {
      return;
    }

    // closed should never be set by this method--only the polling side should do it, as it means
    // that the CLOSED marker has been encountered.

    if (this.exception == null) {
      this.exception = exception;
    }

    // Since this is an unbounded queue, offer() always succeeds.
    queue.offer(CLOSED);
  }

  @Override
  public void close() throws IOException {
    if (closed) {
      return;
    }

    // Mark as closed so that subsequent reads return immediately.
    closed = true;

    // Still offers the ending signal so that any pending read can be unblocked.
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

  /**
   * Reads from the current chunk or polls a next chunk from the queue. This is synchronized to
   * allow reads to be used on different thread while still guaranteeing their sequentiality.
   */
  @Override
  public synchronized int read(byte[] buf, int offset, int length) throws IOException {
    if (buf == null) {
      throw new IllegalArgumentException("buf must not be null");
    }

    if (!(offset >= 0 && length > 0 && offset < buf.length && length <= (buf.length - offset))) {
      throw new IllegalArgumentException("invalid offset and lengeth");
    }

    // Return early if closed is true; throw the saved exception if needed.
    if (closed) {
      if (exception != null) {
        throw exception;
      }
      return -1;
    }

    // Check if there is already a chunk that hasn't been exhausted; call take() if not.
    if (currentChunk == null) {
      if (currentChunkReadPos != -1) {
        throw new IllegalStateException("currentChunk is null but currentChunkReadPos is not -1");
      }

      byte[] next = null;
      try {
        if (timeoutMillis >= 0) {
          next = queue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
        } else {
          next = queue.take();
        }
      } catch (InterruptedException e) {
        // Unlikely to happen.
        throw new AssertionError(e);
      }
      if (next == null) {
        closed = true;
        SocketTimeoutException timeoutException = new SocketTimeoutException();

        if (exception == null) {
          exception = timeoutException;
          // It is still possible that an endOffer(Exception) races and set exception at this point,
          // but timeoutException is still being thrown, and it's ok for subsequent reads to
          // throw the exception set by endOffer(Exception).
        }

        throw timeoutException;
      }

      // If it's the end marker, acknowledge that so that the buffer will mark itself as closed
      // for reading.
      if (next == CLOSED) {
        closed = true;
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
