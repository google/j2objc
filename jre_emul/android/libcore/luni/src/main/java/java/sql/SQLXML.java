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
import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * Maps SQL's XML type into Java.
 */
public interface SQLXML {
    /**
     * Frees any resources held by this object. After {@code free} is called, calling
     * method other than {@code free} will throw {@code SQLException} (calling {@code free}
     * repeatedly will do nothing).
     * @throws SQLException
     */
    void free() throws SQLException;

    /**
     * Returns a stream that can be used to read binary data from this SQL {@code XML} object.
     * @throws SQLException if an error occurs accessing the data
     */
    InputStream getBinaryStream() throws SQLException;

    /**
     * Returns a stream that can be used to write binary data to this SQL {@code XML} object.
     * @throws SQLException if an error occurs accessing the data
     */
    OutputStream setBinaryStream() throws SQLException;

    /**
     * Returns a reader that can be used to read character data from this SQL {@code XML} object.
     * @throws SQLException if an error occurs accessing the data
     */
    Reader getCharacterStream() throws SQLException;

    /**
     * Returns a writer that can be used to write character data to this SQL {@code XML} object.
     * @throws SQLException if an error occurs accessing the data
     */
    Writer setCharacterStream() throws SQLException;

    /**
     * Returns this object's data as an XML string.
     * @throws SQLException if an error occurs accessing the data
     */
    String getString() throws SQLException;

    /**
     * Sets this object's data to the given XML string.
     * @throws SQLException if an error occurs accessing the data
     */
    void setString(String value) throws SQLException;

    /**
     * Returns a {@link Source} for reading this object's data.
     * @throws SQLException if an error occurs accessing the data
     */
    <T extends Source> T getSource(Class<T> sourceClass) throws SQLException;

    /**
     * Returns a {@link Result} for writing this object's data.
     * @throws SQLException if an error occurs accessing the data
     */
    <T extends Result> T setResult(Class<T> resultClass) throws SQLException;
}
