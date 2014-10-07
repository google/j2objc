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
import java.io.Reader;
import java.io.Writer;

/**
 * A Java interface mapping for the SQL CLOB type.
 * <p>
 * An SQL {@code CLOB} type stores a large array of characters as the value in a
 * column of a database.
 * <p>
 * The {@code java.sql.Clob} interface provides methods for setting and
 * retrieving data in the {@code Clob}, for querying {@code Clob} data length,
 * for searching for data within the {@code Clob}.
 */
public interface Clob {

    /**
     * Gets the value of this {@code Clob} object as an ASCII stream.
     *
     * @return an ASCII {@code InputStream} giving access to the
     *            {@code Clob} data.
     * @throws SQLException
     *             if an error occurs accessing the {@code Clob}.
     */
    public InputStream getAsciiStream() throws SQLException;

    /**
     * Gets the data of this {@code Clob} object in a {@code java.io.Reader}.
     *
     * @return a character stream Reader object giving access to the {@code
     *         Clob} data.
     * @throws SQLException
     *             if an error occurs accessing the {@code Clob}.
     */
    public Reader getCharacterStream() throws SQLException;

    /**
     * Gets a copy of a specified substring in this {@code Clob}.
     *
     * @param pos
     *            the index of the start of the substring in the {@code Clob}.
     * @param length
     *            the length of the data to retrieve.
     * @return A string containing the requested data.
     * @throws SQLException
     *             if an error occurs accessing the {@code Clob}.
     */
    public String getSubString(long pos, int length) throws SQLException;

    /**
     * Retrieves the number of characters in this {@code Clob} object.
     *
     * @return a long value with the number of character in this {@code Clob}.
     * @throws SQLException
     *             if an error occurs accessing the {@code Clob}.
     */
    public long length() throws SQLException;

    /**
     * Retrieves the character position at which a specified {@code Clob} object
     * appears in this {@code Clob} object.
     *
     * @param searchstr
     *            the specified {@code Clob} to search for.
     * @param start
     *            the position within this {@code Clob} to start the search
     * @return a long value with the position at which the specified {@code
     *         Clob} occurs within this {@code Clob}.
     * @throws SQLException
     *             if an error occurs accessing the {@code Clob}.
     */
    public long position(Clob searchstr, long start) throws SQLException;

    /**
     * Retrieves the character position at which a specified substring appears
     * in this {@code Clob} object.
     *
     * @param searchstr
     *            the string to search for.
     * @param start
     *            the position at which to start the search within this {@code
     *            Clob}.
     * @return a long value with the position at which the specified string
     *         occurs within this {@code Clob}.
     * @throws SQLException
     *             if an error occurs accessing the {@code Clob}.
     */
    public long position(String searchstr, long start) throws SQLException;

    /**
     * Retrieves a stream which can be used to write Ascii characters to this
     * {@code Clob} object, starting at specified position.
     *
     * @param pos
     *            the position at which to start the writing.
     * @return an OutputStream which can be used to write ASCII characters to
     *         this {@code Clob}.
     * @throws SQLException
     *             if an error occurs accessing the {@code Clob}.
     */
    public OutputStream setAsciiStream(long pos) throws SQLException;

    /**
     * Retrieves a stream which can be used to write a stream of unicode
     * characters to this {@code Clob} object, at a specified position.
     *
     * @param pos
     *            the position at which to start the writing.
     * @return a Writer which can be used to write unicode characters to this
     *         {@code Clob}.
     * @throws SQLException
     *             if an error occurs accessing the {@code Clob}.
     */
    public Writer setCharacterStream(long pos) throws SQLException;

    /**
     * Writes a given Java String to this {@code Clob} object at a specified
     * position.
     *
     * @param pos
     *            the position at which to start the writing.
     * @param str
     *            the string to write.
     * @return the number of characters written.
     * @throws SQLException
     *             if an error occurs accessing the {@code Clob}.
     */
    public int setString(long pos, String str) throws SQLException;

    /**
     * Writes {@code len} characters of a string, starting at a specified
     * character offset, to this {@code Clob}.
     *
     * @param pos
     *            the position at which to start the writing.
     * @param str
     *            the String to write.
     * @param offset
     *            the offset within {@code str} to start writing from.
     * @param len
     *            the number of characters to write.
     * @return the number of characters written.
     * @throws SQLException
     *             if an error occurs accessing the {@code Clob}.
     */
    public int setString(long pos, String str, int offset, int len)
            throws SQLException;

    /**
     * Truncates this {@code Clob} after the specified number of characters.
     *
     * @param len
     *            the length in characters giving the place to
     *            truncate this {@code Clob}.
     * @throws SQLException
     *             if an error occurs accessing the {@code Clob}.
     */
    public void truncate(long len) throws SQLException;

    /**
     * Frees any resources held by this clob. After {@code free} is called, calling
     * method other than {@code free} will throw {@code SQLException} (calling {@code free}
     * repeatedly will do nothing).
     *
     * @throws SQLException
     */
    public void free() throws SQLException;

    /**
     * Returns a {@link Reader} that reads {@code length} characters from this clob, starting
     * at 1-based offset {code pos}.
     */
    public Reader getCharacterStream(long pos, long length) throws SQLException;
}
