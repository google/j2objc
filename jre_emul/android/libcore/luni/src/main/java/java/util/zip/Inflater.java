/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util.zip;

/*-[
#import "zlib.h"
#import "java/io/FileInputStream.h"
#import "java/lang/IllegalArgumentException.h"
#import "java/lang/OutOfMemoryError.h"

#define DEF_WBITS 15
]-*/

import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.util.Arrays;

/**
 * This class decompresses data that was compressed using the <i>DEFLATE</i>
 * algorithm (see <a href="http://www.gzip.org/algorithm.txt">specification</a>).
 *
 * <p>It is usually more convenient to use {@link InflaterInputStream}.
 *
 * <p>To decompress an in-memory {@code byte[]} to another in-memory {@code byte[]} manually:
 * <pre>
 *     byte[] compressedBytes = ...
 *     int decompressedByteCount = ... // From your format's metadata.
 *     Inflater inflater = new Inflater();
 *     inflater.setInput(compressedBytes, 0, compressedBytes.length);
 *     byte[] decompressedBytes = new byte[decompressedByteCount];
 *     if (inflater.inflate(decompressedBytes) != decompressedByteCount) {
 *         throw new AssertionError();
 *     }
 *     inflater.end();
 * </pre>
 * <p>In situations where you don't have all the input in one array (or have so much
 * input that you want to feed it to the inflater in chunks), it's possible to call
 * {@link #setInput} repeatedly, but you're much better off using {@link InflaterInputStream}
 * to handle all this for you.
 *
 * <p>If you don't know how big the decompressed data will be, you can call {@link #inflate}
 * repeatedly on a temporary buffer, copying the bytes to a {@link java.io.ByteArrayOutputStream},
 * but this is probably another sign you'd be better off using {@link InflaterInputStream}.
 *
 * Ported to j2objc by Alexander Jarvis
 */
public class Inflater {

    private int inLength;

    private int inRead; // Set by inflateImpl.
    private boolean finished; // Set by inflateImpl.
    private boolean needsDictionary; // Set by inflateImpl.

    private long streamHandle = -1;
    private long inBuffer = 0L; // Address to malloc'd memory for next_in.

    private final CloseGuard guard = CloseGuard.get();

    /**
     * This constructor creates an inflater that expects a header from the input
     * stream. Use {@link #Inflater(boolean)} if the input comes without a ZLIB
     * header.
     */
    public Inflater() {
        this(false);
    }

    /**
     * This constructor allows to create an inflater that expects no header from
     * the input stream.
     *
     * @param noHeader
     *            {@code true} indicates that no ZLIB header comes with the
     *            input.
     */
    public Inflater(boolean noHeader) {
        streamHandle = createStream(noHeader);
        guard.open("end");
    }

    private native long createStream(boolean noHeader) /*-[
        z_stream *zStream = (z_stream *) malloc(sizeof(z_stream));
        zStream->opaque = Z_NULL;
        zStream->zalloc = Z_NULL;
        zStream->zfree = Z_NULL;
        zStream->adler = 1;

        int err = inflateInit2(zStream, noHeader ? -DEF_WBITS : DEF_WBITS);
        if (err != Z_OK) {
          free(zStream);
          @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] init]);
        }
        return (long long) zStream;
    ]-*/;

    /**
     * Releases resources associated with this {@code Inflater}. Any unused
     * input or output is discarded. This method should be called explicitly in
     * order to free native resources as soon as possible. After {@code end()} is
     * called, other methods will typically throw {@code IllegalStateException}.
     */
    public synchronized void end() {
        guard.close();
        if (streamHandle != -1) {
            endImpl(streamHandle);
            inRead = 0;
            inLength = 0;
            streamHandle = -1;
        }
    }

    private native void endImpl(long handle) /*-[
        z_stream *zStream = (z_stream*) handle;
        inflateEnd(zStream);
        free((void*) self->inBuffer_);
        free(zStream);
    ]-*/;

    @Override protected void finalize() {
        try {
            if (guard != null) {
                guard.warnIfOpen();
            }
            end();
        } finally {
            try {
                super.finalize();
            } catch (Throwable t) {
                throw new AssertionError(t);
            }
        }
    }

    /**
     * Indicates if the {@code Inflater} has inflated the entire deflated
     * stream. If deflated bytes remain and {@link #needsInput} returns {@code
     * true} this method will return {@code false}. This method should be
     * called after all deflated input is supplied to the {@code Inflater}.
     *
     * @return {@code true} if all input has been inflated, {@code false}
     *         otherwise.
     */
    public synchronized boolean finished() {
        return finished;
    }

    /**
     * Returns the {@link Adler32} checksum of the bytes inflated so far, or the
     * checksum of the preset dictionary if {@link #needsDictionary} returns true.
     */
    public synchronized int getAdler() {
        checkOpen();
        return getAdlerImpl(streamHandle);
    }

    private native int getAdlerImpl(long handle) /*-[
        z_stream *zStream = (z_stream*) handle;
        return (int) zStream->adler;
    ]-*/;

    /**
     * Returns the total number of bytes read by the {@code Inflater}. This
     * method is the same as {@link #getTotalIn} except that it returns a
     * {@code long} value instead of an integer.
     */
    public synchronized long getBytesRead() {
        checkOpen();
        return getTotalInImpl(streamHandle);
    }

    /**
     * Returns a the total number of bytes written by this {@code Inflater}. This
     * method is the same as {@code getTotalOut} except it returns a
     * {@code long} value instead of an integer.
     */
    public synchronized long getBytesWritten() {
        checkOpen();
        return getTotalOutImpl(streamHandle);
    }

    /**
     * Returns the number of bytes of current input remaining to be read by this
     * inflater.
     */
    public synchronized int getRemaining() {
        return inLength - inRead;
    }

    /**
     * Returns the offset of the next byte to read in the underlying buffer.
     *
     * For internal use only.
     */
    synchronized int getCurrentOffset() {
        return inRead;
    }

    /**
     * Returns the total number of bytes of input read by this {@code Inflater}. This
     * method is limited to 32 bits; use {@link #getBytesRead} instead.
     */
    public synchronized int getTotalIn() {
        checkOpen();
        return (int) Math.min(getTotalInImpl(streamHandle), (long) Integer.MAX_VALUE);
    }

    private native long getTotalInImpl(long handle) /*-[
        z_stream *zStream = (z_stream*) handle;
        return zStream->total_in;
    ]-*/;

    /**
     * Returns the total number of bytes written to the output buffer by this {@code
     * Inflater}. The method is limited to 32 bits; use {@link #getBytesWritten} instead.
     */
    public synchronized int getTotalOut() {
        checkOpen();
        return (int) Math.min(getTotalOutImpl(streamHandle), (long) Integer.MAX_VALUE);
    }

    private native long getTotalOutImpl(long handle) /*-[
        z_stream *zStream = (z_stream*) handle;
        return zStream->total_out;
    ]-*/;

    /**
     * Inflates bytes from the current input and stores them in {@code buf}.
     *
     * @param buf
     *            the buffer where decompressed data bytes are written.
     * @return the number of bytes inflated.
     * @throws DataFormatException
     *             if the underlying stream is corrupted or was not compressed
     *             using a {@code Deflater}.
     */
    public int inflate(byte[] buf) throws DataFormatException {
        return inflate(buf, 0, buf.length);
    }

    /**
     * Inflates up to {@code byteCount} bytes from the current input and stores them in
     * {@code buf} starting at {@code offset}.
     *
     * @throws DataFormatException
     *             if the underlying stream is corrupted or was not compressed
     *             using a {@code Deflater}.
     * @return the number of bytes inflated.
     */
    public synchronized int inflate(byte[] buf, int offset, int byteCount) throws DataFormatException {
        Arrays.checkOffsetAndCount(buf.length, offset, byteCount);

        checkOpen();

        if (needsInput()) {
            return 0;
        }

        boolean neededDict = needsDictionary;
        needsDictionary = false;
        int result = inflateImpl(buf, offset, byteCount, streamHandle);
        if (needsDictionary && neededDict) {
            throw new DataFormatException("Needs dictionary");
        }
        return result;
    }

    private native int inflateImpl(byte[] buf, int offset, int byteCount, long handle) /*-[
      z_stream *zStream = (z_stream*) handle;
      if (!buf) {
        return -1;
      }
      if (buf->size_ > 0) {
        zStream->next_out = (Bytef *) [buf byteRefAtIndex:offset];
      }
      zStream->avail_out = byteCount;

      Bytef *initialNextIn = zStream->next_in;
      Bytef *initialNextOut = zStream->next_out;

      int err = inflate(zStream, Z_SYNC_FLUSH);
      switch (err) {
        case Z_OK:
          break;
        case Z_NEED_DICT:
          self->needsDictionary_ = YES;
          break;
        case Z_STREAM_END:
          self->finished_ = YES;
          break;
        case Z_STREAM_ERROR:
          return 0;
        default:
          @throw AUTORELEASE([[JavaUtilZipDataFormatException alloc] init]);
      }

      int bytesRead = (int) (zStream->next_in - initialNextIn);
      int bytesWritten = (int) (zStream->next_out - initialNextOut);

      self->inRead_ += bytesRead;
      return bytesWritten;
    ]-*/;


    /**
     * Returns true if the input bytes were compressed with a preset
     * dictionary. This method should be called if the first call to {@link #inflate} returns 0,
     * to determine whether a dictionary is required. If so, {@link #setDictionary}
     * should be called with the appropriate dictionary before calling {@code
     * inflate} again. Use {@link #getAdler} to determine which dictionary is required.
     */
    public synchronized boolean needsDictionary() {
        return needsDictionary;
    }

    /**
     * Returns true if {@link #setInput} must be called before inflation can continue.
     */
    public synchronized boolean needsInput() {
        return inRead == inLength;
    }

    /**
     * Resets this {@code Inflater}. Should be called prior to inflating a new
     * set of data.
     */
    public synchronized void reset() {
        checkOpen();
        finished = false;
        needsDictionary = false;
        inLength = inRead = 0;
        resetImpl(streamHandle);
    }

    private native void resetImpl(long handle) /*-[
      z_stream *zStream = (z_stream *) handle;
      int err = 0;
      err = inflateReset(zStream);
      if (err != Z_OK) {
        @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] init]);
      }
    ]-*/;

    /**
     * Sets the preset dictionary to be used for inflation to {@code dictionary}.
     * See {@link #needsDictionary} for details.
     */
    public synchronized void setDictionary(byte[] dictionary) {
        setDictionary(dictionary, 0, dictionary.length);
    }

    /**
     * Sets the preset dictionary to be used for inflation to a subsequence of {@code dictionary}
     * starting at {@code offset} and continuing for {@code byteCount} bytes. See {@link
     * #needsDictionary} for details.
     */
    public synchronized void setDictionary(byte[] dictionary, int offset, int byteCount) {
        checkOpen();
        Arrays.checkOffsetAndCount(dictionary.length, offset, byteCount);
        setDictionaryImpl(dictionary, offset, byteCount, streamHandle);
    }

    private native void setDictionaryImpl(byte[] buf, int offset, int byteCount, long handle) /*-[
        z_stream *zStream = (z_stream*) handle;
        const Bytef *dictionary = (const Bytef *) [buf byteRefAtIndex:offset];
        int err = inflateSetDictionary(zStream, dictionary, byteCount);
        if (err != Z_OK) {
          @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] init]);
        }
    ]-*/;

    /**
     * Sets the current input to to be decompressed. This method should only be
     * called if {@link #needsInput} returns {@code true}.
     */
    public synchronized void setInput(byte[] buf) {
        setInput(buf, 0, buf.length);
    }

    /**
     * Sets the current input to to be decompressed. This method should only be
     * called if {@link #needsInput} returns {@code true}.
     */
    public synchronized void setInput(byte[] buf, int offset, int byteCount) {
        checkOpen();
        Arrays.checkOffsetAndCount(buf.length, offset, byteCount);
        inRead = 0;
        inLength = byteCount;
        setInputImpl(buf, offset, byteCount, streamHandle);
    }

    private native void setInputImpl(byte[] buf, int offset, int byteCount, long handle) /*-[
      z_stream *zStream = (z_stream *) handle;
      char *baseAddr = (char *) malloc(byteCount);
      if (baseAddr == NULL) {
        @throw AUTORELEASE([[JavaLangOutOfMemoryError alloc] init]);
      }
      if (self->inBuffer_ != 0L) {
        free((void *) self->inBuffer_);
      }
      self->inBuffer_ = (long long) baseAddr;
      zStream->next_in = (Bytef *) baseAddr;
      zStream->avail_in = byteCount;
      if (byteCount > 0) {
        memcpy(baseAddr, [buf byteRefAtIndex:offset], byteCount);
      }
    ]-*/;

    synchronized int setFileInput(FileDescriptor fd, long offset, int byteCount) {
        checkOpen();
        inRead = 0;
        inLength = setFileInputImpl(fd, offset, byteCount, streamHandle);
        return inLength;
    }

    private native int setFileInputImpl(FileDescriptor fd, long offset, int byteCount,
        long handle) /*-[
      JavaIoFileInputStream *fileIn =
          [[JavaIoFileInputStream alloc] initWithJavaIoFileDescriptor:fd];
      [fileIn skipWithLong:offset];
      IOSByteArray *in = [IOSByteArray arrayWithLength:byteCount];
      [fileIn readWithByteArray:in withInt:0 withInt:byteCount];
      [self setInputImplWithByteArray:in withInt:0 withInt:byteCount withLong:handle];
      [fileIn close];
      RELEASE_(fileIn);
      return byteCount;
    ]-*/;

    private void checkOpen() {
        if (streamHandle == -1) {
            throw new IllegalStateException("attempt to use Inflater after calling end");
        }
    }
}
