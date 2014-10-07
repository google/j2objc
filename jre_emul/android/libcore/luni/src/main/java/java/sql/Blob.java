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

package java.sql;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A Java interface representing the SQL {@code BLOB} type.
 * <p>
 * An SQL {@code BLOB} type stores a large array of binary data (bytes) as the
 * value in a column of a database.
 * <p>
 * The {@code java.sql.Blob} interface provides methods for setting and
 * retrieving data in the {@code Blob}, for querying {@code Blob} data length,
 * and for searching for data within the {@code Blob}.
 */
public interface Blob {

    /**
     * Retrieves this {@code Blob} object as a binary stream.
     *
     * @return a binary {@code InputStream} giving access to the {@code Blob}
     *         data.
     * @throws SQLException
     *             if an error occurs accessing the {@code Blob}.
     */
    public InputStream getBinaryStream() throws SQLException;

    /**
     * Retrieves {@code length} bytes from this {@code Blob}, starting at 1-based
     * offset {@code pos}, and returns them as a binary stream.
     *
     * @return a binary {@code InputStream} giving access to the {@code Blob}
     *         data.
     * @throws SQLException
     *             if an error occurs accessing the {@code Blob}.
     */
    public InputStream getBinaryStream(long pos, long length) throws SQLException;

    /**
     * Gets a portion of the value of this {@code Blob} as an array of bytes.
     *
     * @param pos
     *            the position of the first byte in the {@code Blob} to get,
     *            where the first byte in the {@code Blob} has position 1.
     * @param length
     *            the number of bytes to get.
     * @return a byte array containing the data from the {@code Blob}, starting
     *         at {@code pos} and is up to {@code length} bytes long.
     * @throws SQLException
     *             if an error occurs accessing the {@code Blob}.
     */
    public byte[] getBytes(long pos, int length) throws SQLException;

    /**
     * Gets the number of bytes in this {@code Blob} object.
     *
     * @return a {@code long} value with the length of the {@code Blob} in
     *         bytes.
     * @throws SQLException
     *             if an error occurs accessing the {@code Blob}.
     */
    public long length() throws SQLException;

    /**
     * Search for the position in this {@code Blob} at which a specified pattern
     * begins, starting at a specified position within the {@code Blob}.
     *
     * @param pattern
     *            a {@code Blob} containing the pattern of data to search for in
     *            this {@code Blob}.
     * @param start
     *            the position within this {@code Blob} to start the search,
     *            where the first position in the {@code Blob} is {@code 1}.
     * @return a {@code long} value with the position at which the pattern
     *         begins. Returns {@code -1} if the pattern is not found in this
     *         {@code Blob}.
     * @throws SQLException
     *             if an error occurs accessing the {@code Blob}.
     */
    public long position(Blob pattern, long start) throws SQLException;

    /**
     * Search for the position in this {@code Blob} at which the specified
     * pattern begins, starting at a specified position within the {@code Blob}.
     *
     * @param pattern
     *            a byte array containing the pattern of data to search for in
     *            this {@code Blob}.
     * @param start
     *            the position within this {@code Blob} to start the search,
     *            where the first position in the {@code Blob} is {@code 1}.
     * @return a {@code long} value with the position at which the pattern
     *         begins. Returns {@code -1} if the pattern is not found in this
     *         {@code Blob}.
     * @throws SQLException
     *             if an error occurs accessing the {@code Blob}.
     */
    public long position(byte[] pattern, long start) throws SQLException;

    /**
     * Gets a stream that can be used to write binary data to this {@code Blob}.
     *
     * @param pos
     *            the position within this {@code Blob} at which to start
     *            writing, where the first position in the {@code Blob} is
     *            {@code 1}.
     * @return a binary {@code InputStream} which can be used to write data into
     *         the {@code Blob} starting at the specified position.
     * @throws SQLException
     *             if an error occurs accessing the {@code Blob}.
     */
    public OutputStream setBinaryStream(long pos) throws SQLException;

    /**
     * Writes a specified array of bytes to this {@code Blob} object, starting
     * at a specified position. Returns the number of bytes written.
     *
     * @param pos
     *            the position within this {@code Blob} at which to start
     *            writing, where the first position in the {@code Blob} is
     *            {@code 1}.
     * @param theBytes
     *            an array of bytes to write into the {@code Blob}.
     * @return an integer containing the number of bytes written to the {@code
     *         Blob}.
     * @throws SQLException
     *             if an error occurs accessing the {@code Blob}.
     */
    public int setBytes(long pos, byte[] theBytes) throws SQLException;

    /**
     * Writes a portion of a specified byte array to this {@code Blob}. Returns
     * the number of bytes written.
     *
     * @param pos
     *            the position within this {@code Blob} at which to start
     *            writing, where the first position in the {@code Blob} is
     *            {@code 1}.
     * @param theBytes
     *            an array of bytes to write into the {@code Blob}.
     * @param offset
     *            the offset into the byte array from which to start writing
     *            data - the first byte in the array has offset {@code 0}.
     * @param len
     *            the length of data to write in number of bytes.
     * @return an integer containing the number of bytes written to the {@code
     *         Blob}.
     * @throws SQLException
     *             if an error occurs accessing the {@code Blob}.
     */
    public int setBytes(long pos, byte[] theBytes, int offset, int len)
            throws SQLException;

    /**
     * Truncate the value of this {@code Blob} object to a specified length in
     * bytes.
     *
     * @param len
     *            the length of data in bytes after which this {@code Blob}
     *            is to be truncated.
     * @throws SQLException
     *             if an error occurs accessing the {@code Blob}.
     */
    public void truncate(long len) throws SQLException;

    /**
     * Frees any resources held by this blob. After {@code free} is called, calling
     * method other than {@code free} will throw {@code SQLException} (calling {@code free}
     * repeatedly will do nothing).
     *
     * @throws SQLException
     */
    public void free() throws SQLException;
}
