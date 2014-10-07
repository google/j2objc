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
 * Wraps an existing {@link Writer} and performs some transformation on the
 * output data while it is being written. Transformations can be anything from a
 * simple byte-wise filtering output data to an on-the-fly compression or
 * decompression of the underlying writer. Writers that wrap another writer and
 * provide some additional functionality on top of it usually inherit from this
 * class.
 *
 * @see FilterReader
 */
public abstract class FilterWriter extends Writer {

    /**
     * The Writer being filtered.
     */
    protected Writer out;

    /**
     * Constructs a new FilterWriter on the Writer {@code out}. All writes are
     * now filtered through this writer.
     *
     * @param out
     *            the target Writer to filter writes on.
     */
    protected FilterWriter(Writer out) {
        super(out);
        this.out = out;
    }

    /**
     * Closes this writer. This implementation closes the target writer.
     *
     * @throws IOException
     *             if an error occurs attempting to close this writer.
     */
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            out.close();
        }
    }

    /**
     * Flushes this writer to ensure all pending data is sent out to the target
     * writer. This implementation flushes the target writer.
     *
     * @throws IOException
     *             if an error occurs attempting to flush this writer.
     */
    @Override
    public void flush() throws IOException {
        synchronized (lock) {
            out.flush();
        }
    }

    /**
     * Writes {@code count} characters from the char array {@code buffer}
     * starting at position {@code offset} to the target writer.
     *
     * @param buffer
     *            the buffer to write.
     * @param offset
     *            the index of the first character in {@code buffer} to write.
     * @param count
     *            the number of characters in {@code buffer} to write.
     * @throws IOException
     *             if an error occurs while writing to this writer.
     */
    @Override
    public void write(char[] buffer, int offset, int count) throws IOException {
        synchronized (lock) {
            out.write(buffer, offset, count);
        }
    }

    /**
     * Writes the specified character {@code oneChar} to the target writer. Only the
     * two least significant bytes of the integer {@code oneChar} are written.
     *
     * @param oneChar
     *            the char to write to the target writer.
     * @throws IOException
     *             if an error occurs while writing to this writer.
     */
    @Override
    public void write(int oneChar) throws IOException {
        synchronized (lock) {
            out.write(oneChar);
        }
    }

    /**
     * Writes {@code count} characters from the string {@code str} starting at
     * position {@code index} to this writer. This implementation writes
     * {@code str} to the target writer.
     *
     * @param str
     *            the string to be written.
     * @param offset
     *            the index of the first character in {@code str} to write.
     * @param count
     *            the number of chars in {@code str} to write.
     * @throws IOException
     *             if an error occurs while writing to this writer.
     */
    @Override
    public void write(String str, int offset, int count) throws IOException {
        synchronized (lock) {
            out.write(str, offset, count);
        }
    }
}
