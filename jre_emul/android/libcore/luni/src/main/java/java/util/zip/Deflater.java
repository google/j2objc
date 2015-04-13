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

import dalvik.system.CloseGuard;
import java.util.Arrays;
import libcore.util.EmptyArray;

/*-[
#import "java/lang/OutOfMemoryError.h"
#import "java/util/zip/DataFormatException.h"
#import "zlib.h"

#define DEF_MEM_LEVEL 8
#define DEF_WBITS 15
]-*/

/**
 * This class compresses data using the <i>DEFLATE</i> algorithm (see <a
 * href="http://www.gzip.org/algorithm.txt">specification</a>).
 *
 * <p>It is usually more convenient to use {@link DeflaterOutputStream}.
 *
 * <p>To compress an in-memory {@code byte[]} to another in-memory {@code byte[]} manually:
 * <pre>
 *     byte[] originalBytes = ...
 *
 *     Deflater deflater = new Deflater();
 *     deflater.setInput(originalBytes);
 *     deflater.finish();
 *
 *     ByteArrayOutputStream baos = new ByteArrayOutputStream();
 *     byte[] buf = new byte[8192];
 *     while (!deflater.finished()) {
 *         int byteCount = deflater.deflate(buf);
 *         baos.write(buf, 0, byteCount);
 *     }
 *     deflater.end();
 *
 *     byte[] compressedBytes = baos.toByteArray();
 * </pre>
 * <p>In situations where you don't have all the input in one array (or have so much
 * input that you want to feed it to the deflater in chunks), it's possible to call
 * {@link #setInput setInput} repeatedly, but you're much better off using
 * {@link DeflaterOutputStream} to handle all this for you. {@link DeflaterOutputStream} also helps
 * minimize memory requirements&nbsp;&mdash; the sample code above is very expensive.
 *
 * <a name="compression_level"><h3>Compression levels</h3></a>
 * <p>A compression level must be {@link #DEFAULT_COMPRESSION} to compromise between speed and
 * compression (currently equivalent to level 6), or between 0 ({@link #NO_COMPRESSION}, where
 * the input is simply copied) and 9 ({@link #BEST_COMPRESSION}). Level 1 ({@link #BEST_SPEED})
 * performs some compression, but with minimal speed overhead.
 */
public class Deflater {

    /**
     * This <a href="#compression_level">compression level</a> gives the best compression,
     * but takes the most time.
     */
    public static final int BEST_COMPRESSION = 9;

    /**
     * This <a href="#compression_level">compression level</a> gives minimal compression,
     * but takes the least time (of any level that actually performs compression;
     * see {@link #NO_COMPRESSION}).
     */
    public static final int BEST_SPEED = 1;

    /**
     * This <a href="#compression_level">compression level</a> does no compression.
     * It is even faster than {@link #BEST_SPEED}.
     */
    public static final int NO_COMPRESSION = 0;

    /**
     * The default <a href="#compression_level">compression level</a>.
     * This is a trade-off between speed and compression, currently equivalent to level 6.
     */
    public static final int DEFAULT_COMPRESSION = -1;

    /**
     * The default compression strategy.
     */
    public static final int DEFAULT_STRATEGY = 0;

    /**
     * The default compression method.
     */
    public static final int DEFLATED = 8;

    /**
     * A compression strategy.
     */
    public static final int FILTERED = 1;

    /**
     * A compression strategy.
     */
    public static final int HUFFMAN_ONLY = 2;

    /**
     * Use buffering for best compression.
     * @since 1.7
     */
    public static final int NO_FLUSH = 0;

    /**
     * Flush buffers so recipients can immediately decode the data sent thus
     * far. This mode may degrade compression.
     * @since 1.7
     */
    public static final int SYNC_FLUSH = 2;

    /**
     * Flush buffers so recipients can immediately decode the data sent thus
     * far. The compression state is also reset to permit random access and
     * recovery for clients who have discarded or damaged their own copy. This
     * mode may degrade compression.
     * @since 1.7
     */
    public static final int FULL_FLUSH = 3;

    /**
     * Flush buffers and mark the end of the data stream.
     */
    private static final int FINISH = 4;

    /**
     * The ugly name flushParm is for RI compatibility, should code need to access this
     * field via reflection if it's not able to use public API to choose what
     * kind of flushing it gets.
     */
    private int flushParm = NO_FLUSH;

    private boolean finished;

    private int compressLevel = DEFAULT_COMPRESSION;

    private int strategy = DEFAULT_STRATEGY;

    private long streamHandle = -1;
    private long inBuffer = 0L; // Address to malloc'd memory for next_in.

    private byte[] inputBuffer;

    private int inRead;

    private int inLength;

    private final CloseGuard guard = CloseGuard.get();

    /**
     * Constructs a new {@code Deflater} instance using the
     * default <a href="#compression_level">compression level</a>.
     * The compression strategy can be specified with {@link #setStrategy}. A
     * header is added to the output by default; use {@link
     * #Deflater(int, boolean)} if you need to omit the header.
     */
    public Deflater() {
        this(DEFAULT_COMPRESSION, false);
    }

    /**
     * Constructs a new {@code Deflater} instance with the
     * given <a href="#compression_level">compression level</a>.
     * The compression strategy can be specified with {@link #setStrategy}.
     * A header is added to the output by default; use
     * {@link #Deflater(int, boolean)} if you need to omit the header.
     */
    public Deflater(int level) {
        this(level, false);
    }

    /**
     * Constructs a new {@code Deflater} instance with the
     * given <a href="#compression_level">compression level</a>.
     * If {@code noHeader} is true, no ZLIB header is added to the
     * output. In a zip file, every entry (compressed file) comes with such a
     * header. The strategy can be specified using {@link #setStrategy}.
     */
    public Deflater(int level, boolean noHeader) {
        if (level < DEFAULT_COMPRESSION || level > BEST_COMPRESSION) {
            throw new IllegalArgumentException("Bad level: " + level);
        }
        compressLevel = level;
        streamHandle = createStream(compressLevel, strategy, noHeader);
        guard.open("end");
    }

    /**
     * Deflates the data (previously passed to {@link #setInput setInput}) into the
     * supplied buffer.
     *
     * @return number of bytes of compressed data written to {@code buf}.
     */
    public int deflate(byte[] buf) {
        return deflate(buf, 0, buf.length);
    }

    /**
     * Deflates data (previously passed to {@link #setInput setInput}) into a specific
     * region within the supplied buffer.
     *
     * @return the number of bytes of compressed data written to {@code buf}.
     */
    public synchronized int deflate(byte[] buf, int offset, int byteCount) {
        return deflateImpl(buf, offset, byteCount, flushParm);
    }

    /**
     * Deflates data (previously passed to {@link #setInput setInput}) into a specific
     * region within the supplied buffer, optionally flushing the input buffer.
     *
     * @param flush one of {@link #NO_FLUSH}, {@link #SYNC_FLUSH} or {@link #FULL_FLUSH}.
     * @return the number of compressed bytes written to {@code buf}. If this
     *      equals {@code byteCount}, the number of bytes of input to be flushed
     *      may have exceeded the output buffer's capacity. In this case,
     *      finishing a flush will require the output buffer to be drained
     *      and additional calls to {@link #deflate} to be made.
     * @throws IllegalArgumentException if {@code flush} is invalid.
     * @since 1.7
     */
    public synchronized int deflate(byte[] buf, int offset, int byteCount, int flush) {
        if (flush != NO_FLUSH && flush != SYNC_FLUSH && flush != FULL_FLUSH) {
            throw new IllegalArgumentException("Bad flush value: " + flush);
        }
        return deflateImpl(buf, offset, byteCount, flush);
    }

    private synchronized int deflateImpl(byte[] buf, int offset, int byteCount, int flush) {
        checkOpen();
        Arrays.checkOffsetAndCount(buf.length, offset, byteCount);
        if (inputBuffer == null) {
            setInput(EmptyArray.BYTE);
        }
        return deflateImpl(buf, offset, byteCount, streamHandle, flush);
    }

    private native int deflateImpl(byte[] buf, int offset, int byteCount, long handle,
        int flushParm) /*-[
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

      int err = deflate(zStream, flushParm);
      switch (err) {
        case Z_OK:
          break;
        case Z_STREAM_END:
          self->finished_ = YES;
          break;
        case Z_BUF_ERROR:
          // zlib reports this "if no progress is possible (for example avail_in or avail_out was
          // zero) ... Z_BUF_ERROR is not fatal, and deflate() can be called again with more
          // input and more output space to continue compressing".
          break;
        default:
          @throw AUTORELEASE([[JavaUtilZipDataFormatException alloc] init]);
      }

      int bytesRead = (int) (zStream->next_in - initialNextIn);
      int bytesWritten = (int) (zStream->next_out - initialNextOut);

      self->inRead_ += bytesRead;
      return bytesWritten;
    ]-*/;

    /**
     * Frees all resources held onto by this deflating algorithm. Any unused
     * input or output is discarded. This method should be called explicitly in
     * order to free native resources as soon as possible. After {@code end()} is
     * called, other methods will typically throw {@code IllegalStateException}.
     */
    public synchronized void end() {
        guard.close();
        endImpl();
    }

    private void endImpl() {
        if (streamHandle != -1) {
            endImpl(streamHandle);
            inputBuffer = null;
            streamHandle = -1;
        }
    }

    private native void endImpl(long handle) /*-[
        z_stream *zStream = (z_stream*) handle;
        deflateEnd(zStream);
        free((void*) self->inBuffer_);
        free(zStream);
    ]-*/;

    @Override protected void finalize() {
        try {
            if (guard != null) {
                guard.warnIfOpen();
            }
            synchronized (this) {
                end(); // to allow overriding classes to clean up
                endImpl(); // in case those classes don't call super.end()
            }
        } finally {
            try {
                super.finalize();
            } catch (Throwable t) {
                throw new AssertionError(t);
            }
        }
    }

    /**
     * Indicates to the {@code Deflater} that all uncompressed input has been provided
     * to it.
     *
     * @see #finished
     */
    public synchronized void finish() {
        flushParm = FINISH;
    }

    /**
     * Returns true if {@link #finish finish} has been called and all
     * data provided by {@link #setInput setInput} has been
     * successfully compressed and consumed by {@link #deflate deflate}.
     */
    public synchronized boolean finished() {
        return finished;
    }

    /**
     * Returns the {@link Adler32} checksum of the uncompressed data read so far.
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
     * Returns the total number of bytes of input read by this {@code Deflater}. This
     * method is limited to 32 bits; use {@link #getBytesRead} instead.
     */
    public synchronized int getTotalIn() {
        checkOpen();
        return (int) getTotalInImpl(streamHandle);
    }

    private native long getTotalInImpl(long handle) /*-[
        z_stream *zStream = (z_stream*) handle;
        return zStream->total_in;
    ]-*/;

    /**
     * Returns the total number of bytes written to the output buffer by this {@code
     * Deflater}. The method is limited to 32 bits; use {@link #getBytesWritten} instead.
     */
    public synchronized int getTotalOut() {
        checkOpen();
        return (int) getTotalOutImpl(streamHandle);
    }

    private native long getTotalOutImpl(long handle) /*-[
        z_stream *zStream = (z_stream*) handle;
        return zStream->total_out;
    ]-*/;

    /**
     * Returns true if {@link #setInput setInput} must be called before deflation can continue.
     * If all uncompressed data has been provided to the {@code Deflater},
     * {@link #finish} must be called to ensure the compressed data is output.
     */
    public synchronized boolean needsInput() {
        if (inputBuffer == null) {
            return true;
        }
        return inRead == inLength;
    }

    /**
     * Resets the {@code Deflater} to accept new input without affecting any
     * previous compression strategy or level settings. This
     * operation <i>must</i> be called after {@link #finished} returns
     * true if the {@code Deflater} is to be reused.
     */
    public synchronized void reset() {
        checkOpen();
        flushParm = NO_FLUSH;
        finished = false;
        resetImpl(streamHandle);
        inputBuffer = null;
    }

    private native void resetImpl(long handle) /*-[
        z_stream *zStream = (z_stream*) handle;
        int err = deflateReset(zStream);
        if (err != Z_OK) {
          @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] init]);
        }
    ]-*/;

    /**
     * Sets the dictionary to be used for compression by this {@code Deflater}.
     * This method can only be called if this {@code Deflater} supports the writing
     * of ZLIB headers. This is the default, but can be overridden
     * using {@link #Deflater(int, boolean)}.
     */
    public void setDictionary(byte[] dictionary) {
        setDictionary(dictionary, 0, dictionary.length);
    }

    /**
     * Sets the dictionary to be used for compression by this {@code Deflater}.
     * This method can only be called if this {@code Deflater} supports the writing
     * of ZLIB headers. This is the default, but can be overridden
     * using {@link #Deflater(int, boolean)}.
     */
    public synchronized void setDictionary(byte[] buf, int offset, int byteCount) {
        checkOpen();
        Arrays.checkOffsetAndCount(buf.length, offset, byteCount);
        setDictionaryImpl(buf, offset, byteCount, streamHandle);
    }

    private native void setDictionaryImpl(byte[] buf, int offset, int byteCount, long handle) /*-[
        z_stream *zStream = (z_stream*) handle;
        const Bytef *dictionary = (const Bytef *) [buf byteRefAtIndex:offset];
        int err = deflateSetDictionary(zStream, dictionary, byteCount);
        if (err != Z_OK) {
          @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] init]);
        }
    ]-*/;

    /**
     * Sets the input buffer the {@code Deflater} will use to extract uncompressed bytes
     * for later compression.
     */
    public void setInput(byte[] buf) {
        setInput(buf, 0, buf.length);
    }

    /**
     * Sets the input buffer the {@code Deflater} will use to extract uncompressed bytes
     * for later compression.
     */
    public synchronized void setInput(byte[] buf, int offset, int byteCount) {
        checkOpen();
        Arrays.checkOffsetAndCount(buf.length, offset, byteCount);
        inLength = byteCount;
        inRead = 0;
        if (inputBuffer == null) {
            setLevelsImpl(compressLevel, strategy, streamHandle);
        }
        inputBuffer = buf;
        setInputImpl(buf, offset, byteCount, streamHandle);
    }

    private native void setLevelsImpl(int level, int strategy, long handle) /*-[
      z_stream *zStream = (z_stream*) handle;
      zStream->next_out = NULL;
      zStream->avail_out = 0;
      int err = deflateParams(zStream, level, strategy);
      if (err != Z_OK) {
        @throw AUTORELEASE([[JavaLangIllegalStateException alloc] init]);
      }
    ]-*/;

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

    /**
     * Sets the given <a href="#compression_level">compression level</a>
     * to be used when compressing data. This value must be set
     * prior to calling {@link #setInput setInput}.
     * @throws IllegalArgumentException
     *                If the compression level is invalid.
     */
    public synchronized void setLevel(int level) {
        if (level < DEFAULT_COMPRESSION || level > BEST_COMPRESSION) {
            throw new IllegalArgumentException("Bad level: " + level);
        }
        if (inputBuffer != null) {
            throw new IllegalStateException("setLevel cannot be called after setInput");
        }
        compressLevel = level;
    }

    /**
     * Sets the compression strategy to be used. The strategy must be one of
     * FILTERED, HUFFMAN_ONLY or DEFAULT_STRATEGY. This value must be set prior
     * to calling {@link #setInput setInput}.
     *
     * @throws IllegalArgumentException
     *                If the strategy specified is not one of FILTERED,
     *                HUFFMAN_ONLY or DEFAULT_STRATEGY.
     */
    public synchronized void setStrategy(int strategy) {
        if (strategy < DEFAULT_STRATEGY || strategy > HUFFMAN_ONLY) {
            throw new IllegalArgumentException("Bad strategy: " + strategy);
        }
        if (inputBuffer != null) {
            throw new IllegalStateException("setStrategy cannot be called after setInput");
        }
        this.strategy = strategy;
    }

    /**
     * Returns the total number of bytes read by the {@code Deflater}. This
     * method is the same as {@link #getTotalIn} except that it returns a
     * {@code long} value instead of an integer.
     */
    public synchronized long getBytesRead() {
        checkOpen();
        return getTotalInImpl(streamHandle);
    }

    /**
     * Returns a the total number of bytes written by this {@code Deflater}. This
     * method is the same as {@code getTotalOut} except it returns a
     * {@code long} value instead of an integer.
     */
    public synchronized long getBytesWritten() {
        checkOpen();
        return getTotalOutImpl(streamHandle);
    }

    private native long createStream(int level, int strategy, boolean noHeader) /*-[
        z_stream *zStream = (z_stream *) malloc(sizeof(z_stream));
        int err = 0;

        zStream->opaque = Z_NULL;
        zStream->zalloc = Z_NULL;
        zStream->zfree = Z_NULL;
        zStream->adler = 1;

        int windowBits = noHeader ? -DEF_WBITS : DEF_WBITS;
        int memLevel = DEF_MEM_LEVEL;
        err = deflateInit2(zStream, level, Z_DEFLATED, windowBits, memLevel, strategy);
        if (err != Z_OK) {
          free(zStream);
          @throw AUTORELEASE([[JavaLangIllegalArgumentException alloc] init]);
        }
        return (long long) zStream;
    ]-*/;

    private void checkOpen() {
        if (streamHandle == -1) {
            throw new IllegalStateException("attempt to use Deflater after calling end");
        }
    }
}
