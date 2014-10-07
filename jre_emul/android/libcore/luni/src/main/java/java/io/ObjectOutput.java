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
 * @see ObjectOutputStream
 * @see ObjectInput
 */
public interface ObjectOutput extends DataOutput, AutoCloseable {
    /**
     * Closes the target stream. Implementations of this method should free any
     * resources used by the stream.
     *
     * @throws IOException
     *             if an error occurs while closing the target stream.
     */
    public void close() throws IOException;

    /**
     * Flushes the target stream. Implementations of this method should ensure
     * that any pending writes are written out to the target stream.
     *
     * @throws IOException
     *             if an error occurs while flushing the target stream.
     */
    public void flush() throws IOException;

    /**
     * Writes the entire contents of the byte array {@code buffer} to the output
     * stream. Blocks until all bytes are written.
     *
     * @param buffer
     *            the buffer to write.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     */
    public void write(byte[] buffer) throws IOException;

    /**
     * Writes {@code count} bytes from the byte array {@code buffer} starting at
     * position {@code offset} to the target stream. Blocks until all bytes are
     * written.
     *
     * @param buffer
     *            the buffer to write.
     * @param offset
     *            the index of the first byte in {@code buffer} to write.
     * @param count
     *            the number of bytes from {@code buffer} to write to the target
     *            stream.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     */
    public void write(byte[] buffer, int offset, int count) throws IOException;

    /**
     * Writes a single byte to the target stream. Only the least significant
     * byte of the integer {@code value} is written to the stream. Blocks until
     * the byte is actually written.
     *
     * @param value
     *            the byte to write.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     */
    public void write(int value) throws IOException;

    /**
     * Writes the specified object {@code obj} to the target stream.
     *
     * @param obj
     *            the object to write.
     * @throws IOException
     *             if an error occurs while writing to the target stream.
     */
    public void writeObject(Object obj) throws IOException;
}
