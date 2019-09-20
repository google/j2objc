/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.io;

/**
 * Defines an interface for classes that allow reading serialized objects.
 *
 * @see ObjectInputStream
 * @see ObjectOutput
 */
public interface ObjectInput extends DataInput, AutoCloseable {
    /**
     * Indicates the number of bytes of primitive data that can be read without
     * blocking.
     *
     * @return the number of bytes available.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public int available() throws IOException;

    /**
     * Closes this stream. Implementations of this method should free any
     * resources used by the stream.
     *
     * @throws IOException
     *             if an I/O error occurs while closing the input stream.
     */
    public void close() throws IOException;

    /**
     * Reads a single byte from this stream and returns it as an integer in the
     * range from 0 to 255. Returns -1 if the end of this stream has been
     * reached. Blocks if no input is available.
     *
     * @return the byte read or -1 if the end of this stream has been reached.
     * @throws IOException
     *             if this stream is closed or another I/O error occurs.
     */
    public int read() throws IOException;

    /**
     * Reads bytes from this stream into the byte array {@code buffer}. Blocks
     * while waiting for input.
     *
     * @param buffer
     *            the array in which to store the bytes read.
     * @return the number of bytes read or -1 if the end of this stream has been
     *         reached.
     * @throws IOException
     *             if this stream is closed or another I/O error occurs.
     */
    public int read(byte[] buffer) throws IOException;

    /**
     * Reads at most {@code count} bytes from this stream and stores them in
     * byte array {@code buffer} starting at offset {@code count}. Blocks while
     * waiting for input.
     *
     * @param buffer
     *            the array in which to store the bytes read.
     * @param offset
     *            the initial position in {@code buffer} to store the bytes read
     *            from this stream.
     * @param count
     *            the maximum number of bytes to store in {@code buffer}.
     * @return the number of bytes read or -1 if the end of this stream has been
     *         reached.
     * @throws IOException
     *             if this stream is closed or another I/O error occurs.
     */
    public int read(byte[] buffer, int offset, int count) throws IOException;

    /**
     * Reads the next object from this stream.
     *
     * @return the object read.
     *
     * @throws ClassNotFoundException
     *             if the object's class cannot be found.
     * @throws IOException
     *             if this stream is closed or another I/O error occurs.
     */
    public Object readObject() throws ClassNotFoundException, IOException;

    /**
     * Skips {@code byteCount} bytes on this stream. Less than {@code byteCount} byte are
     * skipped if the end of this stream is reached before the operation
     * completes.
     *
     * @return the number of bytes actually skipped.
     * @throws IOException
     *             if this stream is closed or another I/O error occurs.
     */
    public long skip(long byteCount) throws IOException;
}
