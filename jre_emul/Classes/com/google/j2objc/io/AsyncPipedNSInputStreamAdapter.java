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

import com.google.j2objc.annotations.Weak;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

/*-[
#include "java/lang/AssertionError.h"
@interface ComGoogleJ2objcIoAsyncPipedNSInputStreamAdapter_OutputStreamAdapter ()
    <NSStreamDelegate>
@end
]-*/

/**
 * An NSInputStream adapter piped to an NSOutputStream that in turn requests data via a {@link
 * java.io.OutputStream} asynchronously.
 *
 * <p>The main use case is to enable J2ObjC apps to obtain an NSInputStream that they can offer data
 * to and then pass the stream to another object that takes one. NSMutableURLRequest's
 * HTTPBodyStream is one such example.
 *
 * <p>The fundamental problem here is that streams in Java and streams in Objective-C (Foundation to
 * be more precise) have different designs. If you pipe an NSOutputStream to an NSInputStream, that
 * output stream requests data from you in an asynchronous manner using a callback, whereas Java's
 * OutputStream is synchronous. In addition, OutputStream.write(byte[], int, int) assumes that all
 * the bytes will be written to in one go, whereas -[NSOutputStream read:maxLength:] returns the
 * actual bytes written.
 *
 * <p>To use this adapter, call the {@link #create(Delegate, int)} method. It returns a native
 * NSInputStream that you can pass to the target data consumer. To write data to this piped stream,
 * implement the sole delegate method, and use the suppiled Java OutputStream to offer data.
 *
 * <p>If you need to offer your data synchronously, you will need to consider using a pair of {@link
 * java.io.PipedInputStream} and {@link java.io.PipedOutputStream}, and offer the data using the
 * PipedOutputStream, and in your {@link Delegate#offerData(OutputStream)}, read data from the
 * PipedInputStream.
 *
 * <p>It is safe to close the provided OutputStream multiple times. It is also safe to send -close
 * to the underlying NSInputStream and NSOutputStream more than once. If the NSInputStream is closed
 * by the consuming end, the OutputStream will close soon after, and any unread data is discarded.
 *
 * @author Lukhnos Liu
 */
public final class AsyncPipedNSInputStreamAdapter {

  /** Delegate for providing data to the piped NSInputStream. */
  public interface Delegate {
    /**
     * Offers data to the provided output stream or closes the stream if no more data is available.
     *
     * <p>This method is always invoked in a separate thread owned by the adapter. It is safe to
     * call {@link OutputStream#close()} at any time, but you should do all your work within this
     * method and you must not retain a reference to this stream for later consumption elsewhere.
     *
     * @param stream A Java OutputStream.
     */
    void offerData(OutputStream stream);
  }

  /**
   * Wraps an NSOutputStream and handles the writing of leftover data.
   */
  static final class OutputStreamAdapter extends OutputStream {
    private Delegate delegate;
    private Object nativeOutputStream; // NSOutputStream
    private Object leftoverData; // NSData
    @Weak private Object threadForClosing; // NSThread

    /** If true, once the remaining leftover data is written, close() will be called. */
    private boolean closeAfterLeftoverCleared;

    /** If true, the real native close is already scheduled in the dedicated thread. */
    private boolean closeScheduled;

    private static final Logger logger = Logger.getLogger(OutputStreamAdapter.class.getName());

    OutputStreamAdapter(Delegate delegate, Object nativeOutputStream) {
      this.delegate = delegate;
      this.nativeOutputStream = nativeOutputStream;
    }

    @Override
    public native void write(byte[] b, int off, int len) throws IOException /*-[
      if (leftoverData_) {
        @throw create_JavaLangAssertionError_initWithId_(@"Must not have leftover data");
      }

      // Attempt to write everything.
      uint8_t *ptr = (uint8_t *) [b byteRefAtIndex:off];
      NSInteger written = [(NSOutputStream *)nativeOutputStream_ write:ptr maxLength:len];

      if (written < 0) {
        // Stream already closed. Just return.
        return;
      }

      // If the stream buffer cannot accommodate this chunk, we need to extract the leftover.
      if (written < len) {
        leftoverData_ = [[NSData alloc] initWithBytes:(ptr + written) length:(len - written)];
      }
    ]-*/;

    @Override
    public void close() throws IOException {
      // This method and writeleftoverData must be sequential.
      synchronized (nativeOutputStream) {
        if (leftoverData != null) {
          closeAfterLeftoverCleared = true;
        } else {
          scheduleClose();
        }
      }
    }

    @Override
    public void write(int b) throws IOException {
      // This method is inefficient and its use is discouraged, especially on mobile.
      logger.warning("consider avoiding using write(int)");

      byte[] data = new byte[1];
      data[0] = (byte) (b & 0xff);
      write(data);
    }

    /** Writes the leftover data to the native output stream. */
    native boolean writeLeftoverData() /*-[
      @synchronized (nativeOutputStream_) {
        if (!leftoverData_) {
          return false;
        }

        // Attempt to write the leftover in one go.
        uint8_t *ptr = (uint8_t *) [leftoverData_ bytes];
        NSUInteger len = [leftoverData_ length];
        NSInteger written = [(NSOutputStream *)nativeOutputStream_ write:ptr maxLength:len];
        NSData *nextLeftover = nil;

        if (written >= 0) {
          NSUInteger uWritten = (NSUInteger) written;

          // If only part of it is written, make the remainder the new leftover.
          if (uWritten < len) {
            NSRange subdataRange = NSMakeRange(uWritten, len - uWritten);
            nextLeftover = [leftoverData_ subdataWithRange:subdataRange];
          }
        }

        // written < 0 means the stream is closed, do nothing in that case.

        // Do not use autorelease to reduce memory pressure.
        [leftoverData_ release];
        leftoverData_ = [nextLeftover retain];

        // Close if needed.
        if (closeAfterLeftoverCleared_ && !leftoverData_) {
          [self scheduleClose];
        }

        return true;
      }
    ]-*/;

    /** Spawns a new thread to use a runloop to handle the asynchronous data requests. */
    native void start() /*-[
      [NSThread detachNewThreadSelector:@selector(run) toTarget:self withObject:nil];
    ]-*/;

    /** Schedule the actual native close on the dedicated thread. */
    native void scheduleClose() /*-[
      @synchronized (self) {
        if (closeScheduled_) {
          return;
        }
        closeScheduled_ = true;
      }

      [self performSelector:@selector(doClose)
                   onThread:(NSThread *)threadForClosing_
                 withObject:nil
              waitUntilDone:NO];
    ]-*/;

    /*-[
    // Closes the stream *and* removes the stream from the runloop.
    - (void)doClose {
      [(NSOutputStream *)nativeOutputStream_ close];
      [(NSOutputStream *)nativeOutputStream_ removeFromRunLoop:[NSRunLoop currentRunLoop]
                                                       forMode:NSRunLoopCommonModes];
      [(NSOutputStream *)nativeOutputStream_ setDelegate:nil];
      [delegate_ release];
      delegate_ = nil;
      threadForClosing_ = nil;

      // Stop the runloop. After the runloop exits, -run will exit, and the thread will terminate.
      CFRunLoopStop(CFRunLoopGetCurrent());
    }

    // Schedules the output stream in the dedicated thread's runloop.
    - (void)run {
      @autoreleasepool {
        // No need to retain the thread as the reference is no longer used after scheduled closing.
        threadForClosing_ = [NSThread currentThread];

        @try {
          NSRunLoop *runLoop = [NSRunLoop currentRunLoop];
          [(NSOutputStream *)nativeOutputStream_ setDelegate:self];
          [(NSOutputStream *)nativeOutputStream_ scheduleInRunLoop:runLoop
                                                           forMode:NSRunLoopCommonModes];
          [(NSOutputStream *)nativeOutputStream_ open];

          // Run forever until the event source (the output stream) is exhausted.
          CFRunLoopRun();
        }
        @catch (NSException *e) {
          NSLog(@"unexpected exception: %@", e);
        }
      }
    }

    // The NSOutputStream delegate method.
    - (void)stream:(NSStream *)aStream handleEvent:(NSStreamEvent)eventCode {
      switch (eventCode) {
        case NSStreamEventNone:
          // No-op.
          break;
        case NSStreamEventOpenCompleted:
          // No-op.
          break;
        case NSStreamEventHasBytesAvailable:
          // Does not apply to NSOutputStream
          break;
        case NSStreamEventEndEncountered:
          [self scheduleClose];
          break;
        case NSStreamEventErrorOccurred:
          // Should not happen, and the only thing we can do is to close the stream.
          [self scheduleClose];
          break;
        case NSStreamEventHasSpaceAvailable:
          // If close is scheduled, do nothing. Otherwise, only ask our delegate to offer more data
          // if there is no more leftover data.
          if (!closeScheduled_ && ![self writeLeftoverData]) {
            @try {
              [delegate_ offerDataWithJavaIoOutputStream:self];
            }
            @catch (NSException *e) {
              // Ignore error. There's nothing we can do here.
              [aStream close];
            }
          }
          break;
      }
    }
    ]-*/

  }

  private AsyncPipedNSInputStreamAdapter() {}

  /**
   * Creates a native NSInputStream that is piped to a NSOutpuStream, which in turn requests data
   * from the supplied delegate asynchronously.
   *
   * <p>Please note that the returned NSInputStream is not yet open. This is to allow the stream to
   * be used by other Foundation API (such as NSMutableURLRequest) and is consistent with other
   * NSInputStream initializers.
   *
   * @param delegate the delegate.
   * @param bufferSize the size of the internal buffer used to pipe the NSOutputStream to the
   *     NSInputStream.
   * @return a native NSInputStream.
   */
  public static Object create(Delegate delegate, int bufferSize) {
    if (bufferSize < 1) {
      throw new IllegalArgumentException("Invalid buffer size: " + bufferSize);
    }

    if (delegate == null) {
      throw new IllegalArgumentException("Delegate must not be null");
    }

    return nativeCreate(delegate, bufferSize);
  }

  static native Object nativeCreate(Delegate delegate, int bufferSize) /*-[
    CFReadStreamRef readStreamRef;
    CFWriteStreamRef writeStreamRef;

    // Create a bound (piped) pair of streams.
    CFStreamCreateBoundPair(NULL, &readStreamRef, &writeStreamRef, bufferSize);

    if (!readStreamRef) {
      @throw create_JavaLangAssertionError_initWithId_(@"Failed to obtain an NSInputStream");
    }

    if (!writeStreamRef) {
      @throw create_JavaLangAssertionError_initWithId_(@"Failed to obtain an NSOutputStream");
    }

    // Both readStreamRef and writeStreamRef have retain count 1 at this point.

    ComGoogleJ2objcIoAsyncPipedNSInputStreamAdapter_OutputStreamAdapter *adapter;
    adapter = [[ComGoogleJ2objcIoAsyncPipedNSInputStreamAdapter_OutputStreamAdapter alloc]
        initWithComGoogleJ2objcIoAsyncPipedNSInputStreamAdapter_Delegate:delegate
        withId:(NSOutputStream *)writeStreamRef];

    // writeStreamRef is now retained by the adapter, so call release once.
    CFRelease(writeStreamRef);

    [adapter start];

    // adapter is now retained by its own dedicated thread.
    [adapter autorelease];

    // Autorelease the underlying CFReadStream object.
    return [(NSInputStream *)readStreamRef autorelease];
  ]-*/;
}
