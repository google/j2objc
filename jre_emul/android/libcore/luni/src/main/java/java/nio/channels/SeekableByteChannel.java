/*
 * Copyright (C) 2014 The Android Open Source Project
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

package java.nio.channels;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * An interface for channels that keep a pointer to a current position within an underlying
 * byte-based data source such as a file.
 *
 * <p>SeekableByteChannels have a pointer into the underlying data source which is referred to as a
 * <em>position</em>. The position can be manipulated by moving it within the data source, and the
 * current position can be queried.
 *
 * <p>SeekableByteChannels also have an associated <em>size</em>. The size of the channel is the
 * number of bytes that the data source currently contains. The size of the data source can be
 * manipulated by adding more bytes to the end or by removing bytes from the end. See
 * {@link #truncate}, {@link #position} and {@link #write} for details. The current size can also
 * be queried.
 *
 * @hide Until ready for a public API change
 * @since 1.7
 */
public interface SeekableByteChannel extends ByteChannel {

  /**
   * Returns the current position as a positive number of bytes from the start of the underlying
   * data source.
   *
   * @throws ClosedChannelException
   *     if this channel is closed.
   * @throws IOException
   *     if another I/O error occurs.
   */
  long position() throws IOException;

  /**
   * Sets the channel's position to {@code newPosition}.
   *
   * <p>The argument is the number of bytes counted from the start of the data source. The position
   * cannot be set to a value that is negative. The new position can be set beyond the current
   * size. If set beyond the current size, attempts to read will return end-of-file. Write
   * operations will succeed but they will fill the bytes between the current end of the data
   * source
   * and the new position with the required number of (unspecified) byte values.
   *
   * @return the channel.
   * @throws IllegalArgumentException
   *     if the new position is negative.
   * @throws ClosedChannelException
   *     if this channel is closed.
   * @throws IOException
   *     if another I/O error occurs.
   */
  SeekableByteChannel position(long newPosition) throws IOException;

  /**
   * Returns the size of the data source underlying this channel in bytes.
   *
   * @throws ClosedChannelException
   *     if this channel is closed.
   * @throws IOException
   *     if an I/O error occurs.
   */
  long size() throws IOException;

  /**
   * Truncates the data source underlying this channel to a given size. Any bytes beyond the given
   * size are removed. If there are no bytes beyond the given size then the contents are
   * unmodified.
   *
   * <p>If the position is currently greater than the given size, then it is set to the new size.
   *
   * @return this channel.
   * @throws IllegalArgumentException
   *     if the requested size is negative.
   * @throws ClosedChannelException
   *     if this channel is closed.
   * @throws NonWritableChannelException
   *     if the channel cannot be written to.
   * @throws IOException
   *     if another I/O error occurs.
   */
  SeekableByteChannel truncate(long size) throws IOException;

  /**
   * Writes bytes from the given byte buffer to this channel.
   *
   * <p>The bytes are written starting at the channel's current position, and after some number of
   * bytes are written (up to the {@link java.nio.Buffer#remaining() remaining} number of bytes in
   * the buffer) the channel's position is increased by the number of bytes actually written.
   *
   * <p>If the channel's position is beyond the current end of the underlying data source, then the
   * data source is first extended up to the given position by the required number of unspecified
   * byte values.
   *
   * @param buffer
   *     the byte buffer containing the bytes to be written.
   * @return the number of bytes actually written.
   * @throws NonWritableChannelException
   *     if the channel was not opened for writing.
   * @throws ClosedChannelException
   *     if the channel was already closed.
   * @throws AsynchronousCloseException
   *     if another thread closes the channel during the write.
   * @throws ClosedByInterruptException
   *     if another thread interrupts the calling thread while this operation is in progress. The
   *     interrupt state of the calling thread is set and the channel is closed.
   * @throws IOException
   *     if another I/O error occurs, details are in the message.
   */
  @Override
  int write(ByteBuffer buffer) throws IOException;

  /**
   * Reads bytes from this channel into the given buffer.
   *
   * <p>If the channels position is beyond the current end of the underlying data source then
   * end-of-file (-1) is returned.
   *
   * <p>The bytes are read starting at the channel's current position, and after some number of
   * bytes are read (up to the {@link java.nio.Buffer#remaining() remaining} number of bytes in the
   * buffer) the channel's position is increased by the number of bytes actually read. The bytes
   * will be read into the buffer starting at the buffer's current
   * {@link java.nio.Buffer#position() position}. The buffer's {@link java.nio.Buffer#limit()
   * limit} is not changed.
   *
   * <p>The call may block if other threads are also attempting to read from the same channel.
   *
   * @param buffer
   *     the byte buffer to receive the bytes.
   * @return the number of bytes actually read, or -1 if the end of the data has been reached
   * @throws AsynchronousCloseException
   *     if another thread closes the channel during the read.
   * @throws ClosedByInterruptException
   *     if another thread interrupts the calling thread while the operation is in progress. The
   *     interrupt state of the calling thread is set and the channel is closed.
   * @throws ClosedChannelException
   *     if the channel is closed.
   * @throws IOException
   *     another I/O error occurs, details are in the message.
   * @throws NonReadableChannelException
   *     if the channel was not opened for reading.
   */
  @Override
  int read(ByteBuffer buffer) throws IOException;

}
