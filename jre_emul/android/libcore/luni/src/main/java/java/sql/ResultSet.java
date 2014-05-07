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
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;

/**
 * An interface for an object which represents a database table entry, returned
 * as the result of the query to the database.
 * <p>
 * {@code ResultSet}s have a cursor which points to the current data table row.
 * When the {@code ResultSet} is created, the cursor's location is one position
 * ahead of the first row. To move the cursor to the first and consecutive rows,
 * use the {@code next} method. The {@code next} method returns {@code true} as
 * long as there are more rows in the {@code ResultSet}, otherwise it returns
 * {@code false}.
 * <p>
 * The default type of {@code ResultSet} can not be updated and its cursor can
 * only advance forward through the rows of data. This means that it is only
 * possible to read through it once. However, other kinds of {@code ResultSet}
 * are implemented: an <i>updatable</i> type and also types where the cursor can
 * be <i>scrolled</i> forward and backward through the rows of data. How such a
 * {@code ResultSet} is created is demonstrated in the following example:
 * <ul>
 * <dd>
 *         {@code Connection con;}</dd>
 * <dd>{@code Statement aStatement = con.createStatement(
 * ResultSet.TYPE_SCROLL_SENSITIVE,}{@code ResultSet.CONCUR_UPDATABLE );}</dd>
 * <dd>{@code ResultSet theResultSet =
 * theStatement.executeQuery("SELECT price, quantity FROM STOCKTABLE");}</dd>
 * <dd>{@code // theResultSet is both scrollable and updatable}</dd> </ul>
 * <p>
 * The {@code ResultSet} interface provides a series of methods for retrieving
 * data from columns in the current row, such as {@code getDate} and {@code
 * getFloat}. The columns are retrieved either by their index number (starting
 * at 1) or by their name - there are separate methods for both techniques of
 * column addressing. The column names are case insensitive. If several columns
 * have the same name, then the getter methods use the first matching column.
 * This means that if column names are used, it is not possible to guarantee
 * that the name will retrieve data from the intended column - for certainty it
 * is better to use column indexes. Ideally the columns should be read
 * left-to-right and read once only, since not all databases are optimized to
 * handle other techniques of reading the data.
 * <p>
 * When reading data via the appropriate getter methods, the JDBC driver maps
 * the SQL data retrieved from the database to the Java type implied by the
 * method invoked by the application. The JDBC specification has a table for the
 * mappings from SQL types to Java types.
 * <p>
 * There are also methods for writing data into the {@code ResultSet}, such as
 * {@code updateInt} and {@code updateString}. The update methods can be used
 * either to modify the data of an existing row or to insert new data rows into
 * the {@code ResultSet} . Modification of existing data involves moving the
 * cursor to the row which needs modification and then using the update methods
 * to modify the data, followed by calling the {@code ResultSet.updateRow}
 * method. For insertion of new rows, the cursor is first moved to a special row
 * called the <i>Insert Row</i>, data is added using the update methods,
 * followed by calling the {@code ResultSet.insertRow} method.
 * <p>
 * A {@code ResultSet} is closed if the statement which generated it closes, the
 * statement is executed again, or the same statement's next {@code ResultSet}
 * is retrieved (if the statement returned of multiple results).
 */
public interface ResultSet extends Wrapper, AutoCloseable {

    /**
     * A constant used to indicate that a {@code ResultSet} object must be
     * closed when the method {@code Connection.commit} is invoked.
     */
    public static final int CLOSE_CURSORS_AT_COMMIT = 2;

    /**
     * A constant used to indicate that a {@code ResultSet} object must not be
     * closed when the method {@code Connection.commit} is invoked.
     */
    public static final int HOLD_CURSORS_OVER_COMMIT = 1;

    /**
     * A constant used to indicate the concurrency mode for a {@code ResultSet}
     * object that cannot be updated.
     */
    public static final int CONCUR_READ_ONLY = 1007;

    /**
     * A constant used to indicate the concurrency mode for a {@code ResultSet}
     * object that can be updated.
     */
    public static final int CONCUR_UPDATABLE = 1008;

    /**
     * A constant used to indicate processing of the rows of a {@code ResultSet}
     * in the forward direction, first to last.
     */
    public static final int FETCH_FORWARD = 1000;

    /**
     * A constant used to indicate processing of the rows of a {@code ResultSet}
     * in the reverse direction, last to first.
     */
    public static final int FETCH_REVERSE = 1001;

    /**
     * A constant used to indicate that the order of processing of the rows of a
     * {@code ResultSet} is unknown.
     */
    public static final int FETCH_UNKNOWN = 1002;

    /**
     * A constant used to indicate a {@code ResultSet} object whose cursor can
     * only move forward.
     */
    public static final int TYPE_FORWARD_ONLY = 1003;

    /**
     * A constant used to indicate a {@code ResultSet} object which is
     * scrollable but is insensitive to changes made by others.
     */
    public static final int TYPE_SCROLL_INSENSITIVE = 1004;

    /**
     * A constant used to indicate a {@code ResultSet} object which is
     * scrollable and sensitive to changes made by others.
     */
    public static final int TYPE_SCROLL_SENSITIVE = 1005;

    /**
     * Moves the cursor to a specified row number in the {@code ResultSet}.
     *
     * @param row
     *            the index of the row starting at index 1. Index {@code -1}
     *            returns the last row.
     * @return {@code true} if the new cursor position is on the {@code
     *         ResultSet}, {@code false} otherwise.
     * @throws SQLException
     *             if a database error happens.
     */
    public boolean absolute(int row) throws SQLException;

    /**
     * Moves the cursor to the end of the {@code ResultSet}, after the last row.
     *
     * @throws SQLException
     *             if a database error happens.
     */
    public void afterLast() throws SQLException;

    /**
     * Moves the cursor to the start of the {@code ResultSet}, before the first
     * row.
     *
     * @throws SQLException
     *             if a database error happens.
     */
    public void beforeFirst() throws SQLException;

    /**
     * Cancels any updates made to the current row in the {@code ResultSet}.
     *
     * @throws SQLException
     *             if a database error happens.
     */
    public void cancelRowUpdates() throws SQLException;

    /**
     * Clears all warnings related to this {@code ResultSet}.
     *
     * @throws SQLException
     *             if a database error happens.
     */
    public void clearWarnings() throws SQLException;

    /**
     * Releases this {@code ResultSet}'s database and JDBC resources. You are
     * strongly advised to use this method rather than relying on the release
     * being done when the {@code ResultSet}'s finalize method is called during
     * garbage collection process. Note that the {@code close()} method might
     * take some time to complete since it is dependent on the behavior of the
     * connection to the database and the database itself.
     *
     * @throws SQLException
     *             if a database error happens.
     */
    public void close() throws SQLException;

    /**
     * Deletes the current row from the {@code ResultSet} and from the
     * underlying database.
     *
     * @throws SQLException
     *             if a database error happens.
     */
    public void deleteRow() throws SQLException;

    /**
     * Gets the index number for a column in the {@code ResultSet} from the
     * provided column name.
     *
     * @param columnName
     *            the column name.
     * @return the column's index in the {@code ResultSet} identified by column
     *         name.
     * @throws SQLException
     *             if a database error happens.
     */
    public int findColumn(String columnName) throws SQLException;

    /**
     * Shifts the cursor position to the first row in the {@code ResultSet}.
     *
     * @return {@code true} if the position is in a legitimate row, {@code
     *         false} if the {@code ResultSet} contains no rows.
     * @throws SQLException
     *             if a database error happens.
     */
    public boolean first() throws SQLException;

    /**
     * Gets the content of a column specified by column index in the current row
     * of this {@code ResultSet} as a {@code java.sql.Array}.
     *
     * @param columnIndex
     *            the index of the column to read
     * @return a {@code java.sql.Array} with the data from the column.
     * @throws SQLException
     *             if a database error happens.
     */
    public Array getArray(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name as a {@code
     * java.sql.Array}.
     *
     * @param colName
     *            the name of the column to read.
     * @return a {@code java.sql.Array} with the data from the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public Array getArray(String colName) throws SQLException;

    /**
     * Gets the value of a column specified by column index as an ASCII
     * character stream.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return an {@code InputStream} with the data from the column.
     * @throws SQLException
     *             if a database error happens.
     */
    public InputStream getAsciiStream(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name as an ASCII character
     * stream.
     *
     * @param columnName
     *            the name of the column to read
     * @return an {@code InputStream} with the data from the column.
     * @throws SQLException
     *             if a database error happens.
     */
    public InputStream getAsciiStream(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified by column index as a {@code
     * java.math.BigDecimal}.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return a {@code BigDecimal} with the value of the column.
     * @throws SQLException
     *             if a database error happens.
     */
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column index as a {@code
     * java.math.BigDecimal}.
     *
     * @deprecated Use {@link #getBigDecimal(int)} or {@link #getBigDecimal(String)} instead.
     * @param columnIndex
     *            the index of the column to read.
     * @param scale
     *            the number of digits after the decimal point
     * @return a {@code BigDecimal} with the value of the column.
     * @throws SQLException
     *             if a database error happens.
     */
    @Deprecated
    public BigDecimal getBigDecimal(int columnIndex, int scale)
            throws SQLException;

    /**
     * Gets the value of a column specified by column name, as a {@code
     * java.math.BigDecimal}.
     *
     * @param columnName
     *            the name of the column to read.
     * @return a BigDecimal with value of the column.
     * @throws SQLException
     *             if a database error happens.
     */
    public BigDecimal getBigDecimal(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified by column name, as a {@code
     * java.math.BigDecimal}.
     *
     * @deprecated Use {@link #getBigDecimal(int)} or {@link #getBigDecimal(String)} instead.
     * @param columnName
     *            the name of the column to read.
     * @param scale
     *            the number of digits after the decimal point
     * @return a BigDecimal with value of the column.
     * @throws SQLException
     *             if a database error happens.
     */
    @Deprecated
    public BigDecimal getBigDecimal(String columnName, int scale)
            throws SQLException;

    /**
     * Gets the value of a column specified by column index as a binary
     * stream.
     * <p>
     * This method can be used to read {@code LONGVARBINARY} values. All of the
     * data in the {@code InputStream} should be read before getting data from
     * any other column. A further call to a getter method will implicitly close
     * the {@code InputStream}.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return an {@code InputStream} with the data from the column. If the
     *         column value is SQL {@code NULL}, {@code null} is returned.
     * @throws SQLException
     *             if a database error happens.
     */
    public InputStream getBinaryStream(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name as a binary stream.
     * <p>
     * This method can be used to read {@code LONGVARBINARY} values. All of the
     * data in the {@code InputStream} should be read before getting data from
     * any other column. A further call to a getter method will implicitly close
     * the {@code InputStream}.
     *
     * @param columnName
     *            the name of the column to read.
     * @return an {@code InputStream} with the data from the column if the
     *         column value is SQL {@code NULL}, {@code null} is returned.
     * @throws SQLException
     *             if a database error happens.
     */
    public InputStream getBinaryStream(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified by column index as a {@code
     * java.sql.Blob} object.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return a {@code java.sql.Blob} with the value of the column.
     * @throws SQLException
     *             if a database error happens.
     */
    public Blob getBlob(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name, as a {@code
     * java.sql.Blob} object.
     *
     * @param columnName
     *            the name of the column to read.
     * @return a {@code java.sql.Blob} with the value of the column.
     * @throws SQLException
     *             if a database error happens.
     */
    public Blob getBlob(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified by column index as a {@code boolean}
     * .
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return a {@code boolean} value from the column. If the column is SQL
     *         {@code NULL}, {@code false} is returned.
     * @throws SQLException
     *             if a database error happens.
     */
    public boolean getBoolean(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name, as a {@code boolean}
     * .
     *
     * @param columnName
     *            the name of the column to read.
     * @return a {@code boolean} value from the column. If the column is SQL
     *         {@code NULL}, {@code false} is returned.
     * @throws SQLException
     *             if a database error happens.
     */
    public boolean getBoolean(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified by column index as a {@code byte}.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return a {@code byte} equal to the value of the column. 0 if the value
     *         is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public byte getByte(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name as a {@code byte}.
     *
     * @param columnName
     *            the name of the column to read.
     * @return a {@code byte} equal to the value of the column. 0 if the value
     *         is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public byte getByte(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified by column index as a byte array.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return a byte array containing the value of the column. {@code null} if
     *         the column contains SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public byte[] getBytes(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name as a byte array.
     *
     * @param columnName
     *            the name of the column to read.
     * @return a byte array containing the value of the column. {@code null} if
     *         the column contains SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public byte[] getBytes(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified by column index as a {@code
     * java.io.Reader} object.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return a {@code Reader} holding the value of the column. {@code null} if
     *         the column value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     * @see java.io.Reader
     */
    public Reader getCharacterStream(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name as a {@code
     * java.io.Reader} object.
     *
     * @param columnName
     *            the name of the column to read.
     * @return a {@code Reader} holding the value of the column. {@code null} if
     *         the column value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Reader getCharacterStream(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified by column index as a {@code
     * java.sql.Clob}.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return a {@code Clob} object representing the value in the column.
     *         {@code null} if the value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Clob getClob(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name as a {@code
     * java.sql.Clob}.
     *
     * @param colName
     *            the name of the column to read.
     * @return a {@code Clob} object representing the value in the column.
     *         {@code null} if the value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Clob getClob(String colName) throws SQLException;

    /**
     * Gets the concurrency mode of this {@code ResultSet}.
     *
     * @return the concurrency mode - one of: {@code ResultSet.CONCUR_READ_ONLY}
     *         , {@code ResultSet.CONCUR_UPDATABLE}.
     * @throws SQLException
     *             if a database error happens.
     */
    public int getConcurrency() throws SQLException;

    /**
     * Gets the name of the SQL cursor of this {@code ResultSet}.
     *
     * @return the SQL cursor name.
     * @throws SQLException
     *             if a database error happens.
     */
    public String getCursorName() throws SQLException;

    /**
     * Gets the value of a column specified by column index as a {@code
     * java.sql.Date}.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return a {@code java.sql.Date} matching the column value. {@code null}
     *         if the column is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Date getDate(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column index as a {@code
     * java.sql.Date}. This method uses a supplied calendar to compute the Date.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @param cal
     *            a {@code java.util.Calendar} to use in constructing the Date.
     * @return a {@code java.sql.Date} matching the column value. {@code null}
     *         if the column is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Date getDate(int columnIndex, Calendar cal) throws SQLException;

    /**
     * Gets the value of a column specified by column name as a {@code
     * java.sql.Date}.
     *
     * @param columnName
     *            the name of the column to read.
     * @return a {@code java.sql.Date} matching the column value. {@code null}
     *         if the column is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Date getDate(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified by column name, as a {@code
     * java.sql.Date} object.
     *
     * @param columnName
     *            the name of the column to read.
     * @param cal
     *            {@code java.util.Calendar} to use in constructing the Date.
     * @return a {@code java.sql.Date} matching the column value. {@code null}
     *         if the column is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Date getDate(String columnName, Calendar cal) throws SQLException;

    /**
     * Gets the value of a column specified by column index as a {@code double}
     * value.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return a {@code double} equal to the column value. {@code 0.0} if the
     *         column is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public double getDouble(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name as a {@code double}
     * value.
     *
     * @param columnName
     *            the name of the column to read.
     * @return a {@code double} equal to the column value. {@code 0.0} if the
     *         column is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public double getDouble(String columnName) throws SQLException;

    /**
     * Gets the direction in which rows are fetched for this {@code ResultSet}
     * object.
     *
     * @return the fetch direction. Will be one of:
     *         <ul>
     *         <li>ResultSet.FETCH_FORWARD</li><li>ResultSet.FETCH_REVERSE</li>
     *         <li>ResultSet.FETCH_UNKNOWN</li>
     *         </ul>
     * @throws SQLException
     *             if a database error happens.
     */
    public int getFetchDirection() throws SQLException;

    /**
     * Gets the fetch size (in number of rows) for this {@code ResultSet}.
     *
     * @return the fetch size as an int
     * @throws SQLException
     *             if a database error happens.
     */
    public int getFetchSize() throws SQLException;

    /**
     * Gets the value of a column specified by column index as a {@code float}
     * value.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return a {@code float} equal to the column value. {@code 0.0} if the
     *         column is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public float getFloat(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name as a {@code float}
     * value.
     *
     * @param columnName
     *            the name of the column to read.
     * @return a {@code float} equal to the column value. {@code 0.0} if the
     *         column is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public float getFloat(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified by column index as an {@code int}
     * value.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return an {@code int} equal to the column value. {@code 0} if the
     *         column is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public int getInt(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name, as an {@code int}
     * value.
     *
     * @param columnName
     *            the name of the column to read.
     * @return an {@code int} equal to the column value. {@code 0} if the
     *         column is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public int getInt(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified by column index as a {@code long}
     * value.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return a {@code long} equal to the column value. {@code 0} if the
     *         column is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public long getLong(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name, as a {@code long}
     * value.
     *
     * @param columnName
     *            the name of the column to read.
     * @return a {@code long} equal to the column value. {@code 0} if the
     *         column is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public long getLong(String columnName) throws SQLException;

    /**
     * Gets the metadata for this {@code ResultSet}. This defines the number,
     * types and properties of the columns in the {@code ResultSet}.
     *
     * @return a {@code ResultSetMetaData} object with information about this
     *         {@code ResultSet}.
     * @throws SQLException
     *             if a database error happens.
     */
    public ResultSetMetaData getMetaData() throws SQLException;

    /**
     * Gets the value of a specified column as a Java {@code Object}. The type
     * of the returned object will be the default according to the column's SQL
     * type, following the JDBC specification for built-in types.
     * <p>
     * For SQL User Defined Types, if a column value is Structured or Distinct,
     * this method behaves the same as a call to: {@code
     * getObject(columnIndex,this.getStatement().getConnection().getTypeMap())}
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return an {@code Object} containing the value of the column. {@code
     *         null} if the column value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Object getObject(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column index as a Java {@code
     * Object}.
     * <p>
     * The type of the Java object will be determined by the supplied Map to
     * perform the mapping of SQL {@code Struct} or Distinct types into Java
     * objects.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @param map
     *            a {@code java.util.Map} containing a mapping from SQL Type
     *            names to Java classes.
     * @return an {@code Object} containing the value of the column. {@code
     *         null} if the column value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Object getObject(int columnIndex, Map<String, Class<?>> map)
            throws SQLException;

    /**
     * Gets the value of a specified column as a Java {@code Object}. The type
     * of the returned object will be the default according to the column's SQL
     * type, following the JDBC specification for built-in types.
     * <p>
     * For SQL User Defined Types, if a column value is structured or distinct,
     * this method behaves the same as a call to: {@code
     * getObject(columnIndex,this.getStatement().getConnection().getTypeMap())}
     *
     * @param columnName
     *            the name of the column to read.
     * @return an {@code Object} containing the value of the column. {@code
     *         null} if the column value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Object getObject(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified by column name as a Java {@code
     * Object}.
     * <p>
     * The type of the Java object will be determined by the supplied Map to
     * perform the mapping of SQL Struct or Distinct types into Java objects.
     *
     * @param columnName
     *            the name of the column to read.
     * @param map
     *            a {@code java.util.Map} containing a mapping from SQL Type names to
     *            Java classes.
     * @return an {@code Object} containing the value of the column. {@code
     *         null} if the column value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Object getObject(String columnName, Map<String, Class<?>> map)
            throws SQLException;

    /**
     * Gets the value of a column specified by column index as a Java {@code
     * java.sql.Ref}.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return a Ref representing the value of the SQL REF in the column
     * @throws SQLException
     *             if a database error happens.
     */
    public Ref getRef(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name as a Java {@code
     * java.sql.Ref}.
     *
     * @param colName
     *            the name of the column to read.
     * @return a Ref representing the value of the SQL {@code REF} in the column
     * @throws SQLException
     *             if a database error happens.
     */
    public Ref getRef(String colName) throws SQLException;

    /**
     * Gets the number of the current row in the {@code ResultSet}. Row numbers
     * start at 1 for the first row.
     *
     * @return the index number of the current row. {@code 0} is returned if
     *         there is no current row.
     * @throws SQLException
     *             if a database error happens.
     */
    public int getRow() throws SQLException;

    /**
     * Gets the value of a column specified by column index as a short value.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return a short value equal to the value of the column. {@code 0} if
     *         the value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public short getShort(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name, as a short value.
     *
     * @param columnName
     *            the name of the column to read.
     * @return a short value equal to the value of the column. {@code 0} if
     *         the value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public short getShort(String columnName) throws SQLException;

    /**
     * Gets the statement that produced this {@code ResultSet}. If the {@code
     * ResultSet} was not created by a statement (i.e. because it was returned
     * from one of the {@link DatabaseMetaData} methods), {@code null} is
     * returned.
     *
     * @return the Statement which produced this {@code ResultSet}, or {@code
     *         null} if the {@code ResultSet} was not created by a Statement.
     * @throws SQLException
     *             if a database error happens.
     */
    public Statement getStatement() throws SQLException;

    /**
     * Gets the value of a column specified by column index as a String.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return the String representing the value of the column, {@code null} if
     *         the column is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public String getString(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name, as a String.
     *
     * @param columnName
     *            the name of the column to read.
     * @return the String representing the value of the column, {@code null} if
     *         the column is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public String getString(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified by column index as a {@code
     * java.sql.Time} value.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return a Time representing the column value, {@code null} if the column
     *         value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Time getTime(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column index as a {@code
     * java.sql.Time} value. The supplied {@code Calendar} is used to
     * map the SQL {@code Time} value to a Java Time value.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @param cal
     *            a {@code Calendar} to use in creating the Java Time value.
     * @return a Time representing the column value, {@code null} if the column
     *         value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Time getTime(int columnIndex, Calendar cal) throws SQLException;

    /**
     * Gets the value of a column specified by column name, as a {@code
     * java.sql.Time} value.
     *
     * @param columnName
     *            the name of the column to read.
     * @return the column value, {@code null} if the column value is SQL {@code
     *         NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Time getTime(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified by column index, as a {@code
     * java.sql.Time} value. The supplied {@code Calendar} is used to
     * map the SQL {@code Time} value to a Java Time value.
     *
     * @param columnName
     *            the name of the column to read.
     * @param cal
     *            a {@code Calendar} to use in creating the Java time value.
     * @return a Time representing the column value, {@code null} if the column
     *         value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Time getTime(String columnName, Calendar cal) throws SQLException;

    /**
     * Gets the value of a column specified by column index as a {@code
     * java.sql.Timestamp} value.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return a timestamp representing the column value, {@code null} if the
     *         column value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Timestamp getTimestamp(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column index, as a {@code
     * java.sql.Timestamp} value. The supplied Calendar is used when mapping
     * the SQL {@code Timestamp} value to a Java {@code Timestamp} value.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @param cal
     *            Calendar to use in creating the Java timestamp value.
     * @return a timestamp representing the column value, {@code null} if the
     *         column value is SQL NULL.
     * @throws SQLException
     *             if a database error happens.
     */
    public Timestamp getTimestamp(int columnIndex, Calendar cal)
            throws SQLException;

    /**
     * Gets the value of a column specified by column name, as a {@code
     * java.sql.Timestamp} value.
     *
     * @param columnName
     *            the name of the column to read.
     * @return a timestamp representing the column value, {@code null} if the
     *         column value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Timestamp getTimestamp(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified by column name, as a {@code
     * java.sql.Timestamp} value. The supplied Calendar is used when mapping
     * the SQL {@code Timestamp} value to a Java {@code Timestamp} value.
     *
     * @param columnName
     *            the name of the column to read.
     * @param cal
     *            Calendar to use in creating the Java {@code Timestamp} value.
     * @return a timestamp representing the column value, {@code null} if the
     *         column value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public Timestamp getTimestamp(String columnName, Calendar cal)
            throws SQLException;

    /**
     * Gets the type of the {@code ResultSet}.
     *
     * @return The {@code ResultSet} type, one of:
     *         <ul>
     *         <li>{@code ResultSet.TYPE_FORWARD_ONLY}</li> <li>{@code
     *         ResultSet.TYPE_SCROLL_INSENSITIVE}</li> <li>{@code
     *         ResultSet.TYPE_SCROLL_SENSITIVE}</li>
     *         </ul>
     * @throws SQLException
     *             if there is a database error.
     */
    public int getType() throws SQLException;

    /**
     * Gets the value of the column as an {@code InputStream} of unicode
     * characters.
     *
     * @deprecated Use {@link #getCharacterStream} instead.
     * @param columnIndex
     *            the index of the column to read.
     * @return an {@code InputStream} holding the value of the column. {@code
     *         null} if the column value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    @Deprecated
    public InputStream getUnicodeStream(int columnIndex) throws SQLException;

    /**
     * Gets the value of the column as an {@code InputStream} of Unicode
     * characters.
     *
     * @deprecated Use {@link #getCharacterStream} instead.
     * @param columnName
     *            the name of the column to read.
     * @return an {@code InputStream} holding the value of the column. {@code
     *         null} if the column value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    @Deprecated
    public InputStream getUnicodeStream(String columnName) throws SQLException;

    /**
     * Gets the value of a column specified by column index as a {@code
     * java.net.URL}.
     *
     * @param columnIndex
     *            the index of the column to read.
     * @return a URL. {@code null} if the column value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public URL getURL(int columnIndex) throws SQLException;

    /**
     * Gets the value of a column specified by column name as a {@code
     * java.net.URL} object.
     *
     * @param columnName
     *            the name of the column to read.
     * @return the column vaule as a URL. {@code null} if the column value is SQL {@code NULL}.
     * @throws SQLException
     *             if a database error happens.
     */
    public URL getURL(String columnName) throws SQLException;

    /**
     * Gets the first warning generated by calls on this {@code ResultSet}.
     * Subsequent warnings on this {@code ResultSet} are chained to the first
     * one.
     * <p>
     * The warnings are cleared when a new Row is read from the {@code
     * ResultSet}. The warnings returned by this method are only the warnings
     * generated by {@code ResultSet} method calls - warnings generated by
     * Statement methods are held by the Statement.
     * <p>
     * An {@code SQLException} is generated if this method is called on a closed
     * {@code ResultSet}.
     *
     * @return an SQLWarning which is the first warning for this {@code
     *         ResultSet}. {@code null} if there are no warnings.
     * @throws SQLException
     *             if a database error happens.
     */
    public SQLWarning getWarnings() throws SQLException;

    /**
     * Insert the insert row into the {@code ResultSet} and into the underlying
     * database. The cursor must be set to the Insert Row before this method is
     * invoked.
     *
     * @throws SQLException
     *             if a database error happens. Particular cases include the
     *             cursor not being on the Insert Row or if any columns in the
     *             row do not have a value where the column is declared as
     *             not-nullable.
     */
    public void insertRow() throws SQLException;

    /**
     * Gets if the cursor is after the last row of the {@code ResultSet}.
     *
     * @return {@code true} if the cursor is after the last row in the {@code
     *         ResultSet}, {@code false} if the cursor is at any other position
     *         in the {@code ResultSet}.
     * @throws SQLException
     *             if a database error happens.
     */
    public boolean isAfterLast() throws SQLException;

    /**
     * Gets if the cursor is before the first row of the {@code ResultSet}.
     *
     * @return {@code true} if the cursor is before the first row in the {@code
     *         ResultSet}, {@code false} if the cursor is at any other position
     *         in the {@code ResultSet}.
     * @throws SQLException
     *             if a database error happens.
     */
    public boolean isBeforeFirst() throws SQLException;

    /**
     * Gets if the cursor is on the first row of the {@code ResultSet}.
     *
     * @return {@code true} if the cursor is on the first row in the {@code
     *         ResultSet}, {@code false} if the cursor is at any other position
     *         in the {@code ResultSet}.
     * @throws SQLException
     *             if a database error happens.
     */
    public boolean isFirst() throws SQLException;

    /**
     * Gets if the cursor is on the last row of the {@code ResultSet}
     *
     * @return {@code true} if the cursor is on the last row in the {@code
     *         ResultSet}, {@code false} if the cursor is at any other position
     *         in the {@code ResultSet}.
     * @throws SQLException
     *             if a database error happens.
     */
    public boolean isLast() throws SQLException;

    /**
     * Shifts the cursor position to the last row of the {@code ResultSet}.
     *
     * @return {@code true} if the new position is in a legitimate row, {@code
     *         false} if the {@code ResultSet} contains no rows.
     * @throws SQLException
     *             if there is a database error.
     */
    public boolean last() throws SQLException;

    /**
     * Moves the cursor to the remembered position, namely the
     * row that was the current row before a call to {@code moveToInsertRow}.
     * This only applies if the cursor is on the Insert Row.
     *
     * @throws SQLException
     *             if a database error happens.
     */
    public void moveToCurrentRow() throws SQLException;

    /**
     * Moves the cursor position to the Insert Row. The current position is
     * remembered and the cursor is positioned at the Insert Row. The columns in
     * the Insert Row should be filled in with the appropriate update methods,
     * before calling {@code insertRow} to insert the new row into the database.
     *
     * @throws SQLException
     *             if a database error happens.
     */
    public void moveToInsertRow() throws SQLException;

    /**
     * Shifts the cursor position down one row in this {@code ResultSet} object.
     * <p>
     * Any input streams associated with the current row are closed and any
     * warnings are cleared.
     *
     * @return {@code true} if the updated cursor position is pointing to a
     *         valid row, {@code false} otherwise (i.e. when the cursor is after
     *         the last row in the {@code ResultSet}).
     * @throws SQLException
     *             if a database error happens.
     */
    public boolean next() throws SQLException;

    /**
     * Relocates the cursor position to the preceding row in this {@code
     * ResultSet}.
     *
     * @return {@code true} if the new position is in a legitimate row, {@code
     *         false} if the cursor is now before the first row.
     * @throws SQLException
     *             if a database error happens.
     */
    public boolean previous() throws SQLException;

    /**
     * Refreshes the current row with its most up to date value in the database.
     * Must not be called when the cursor is on the Insert Row.
     * <p>
     * If any columns in the current row have been updated but the {@code
     * updateRow} has not been called, then the updates are lost when this
     * method is called.
     *
     * @throws SQLException
     *             if a database error happens., including if the current row is
     *             the Insert row.
     */
    public void refreshRow() throws SQLException;

    /**
     * Moves the cursor position up or down by a specified number of rows. If
     * the new position is beyond the start row (or end row), the cursor position is
     * set before the first row (or, respectively, after the last row).
     *
     * @param rows
     *            a number of rows to move the cursor - may be positive or
     *            negative
     * @return {@code true} if the new cursor position is on a row, {@code
     *         false} otherwise
     * @throws SQLException
     *             if a database error happens.
     */
    public boolean relative(int rows) throws SQLException;

    /**
     * Indicates whether a row has been deleted. This method depends on whether
     * the JDBC driver and database can detect deletions.
     *
     * @return {@code true} if a row has been deleted and if deletions are
     *         detected, {@code false} otherwise.
     * @throws SQLException
     *             if a database error happens.
     */
    public boolean rowDeleted() throws SQLException;

    /**
     * Indicates whether the current row has had an insertion operation. This
     * method depends on whether the JDBC driver and database can detect
     * insertions.
     *
     * @return {@code true} if a row has been inserted and if insertions are
     *         detected, {@code false} otherwise.
     * @throws SQLException
     *             if a database error happens.
     */
    public boolean rowInserted() throws SQLException;

    /**
     * Indicates whether the current row has been updated. This method depends
     * on whether the JDBC driver and database can detect updates.
     *
     * @return {@code true} if the current row has been updated and if updates
     *         can be detected, {@code false} otherwise.
     * @throws SQLException
     *             if a database error happens.
     */
    public boolean rowUpdated() throws SQLException;

    /**
     * Indicates which direction (forward/reverse) will be used to process the
     * rows of this {@code ResultSet} object. This is treated as a hint by the
     * JDBC driver.
     *
     * @param direction
     *            can be {@code ResultSet.FETCH_FORWARD}, {@code
     *            ResultSet.FETCH_REVERSE}, or {@code ResultSet.FETCH_UNKNOWN}
     * @throws SQLException
     *             if there is a database error.
     */
    public void setFetchDirection(int direction) throws SQLException;

    /**
     * Indicates the number of rows to fetch from the database when extra rows
     * are required for this {@code ResultSet}. This used as a hint to the JDBC
     * driver.
     *
     * @param rows
     *            the number of rows to fetch. {@code 0} implies that the JDBC
     *            driver can make its own decision about the fetch size. The
     *            number should not be greater than the maximum number of rows
     *            established by the statement that generated the {@code
     *            ResultSet}.
     * @throws SQLException
     *             if a database error happens.
     */
    public void setFetchSize(int rows) throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code
     * java.sql.Array} value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateArray(int columnIndex, Array x) throws SQLException;

    /**
     * Updates a column specified by a column name with a {@code java.sql.Array}
     * value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateArray(String columnName, Array x) throws SQLException;

    /**
     * Updates a column specified by a column index with an ASCII stream value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @param length
     *            the length of the data to write from the stream
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateAsciiStream(int columnIndex, InputStream x, int length)
            throws SQLException;

    /**
     * Updates a column specified by a column name with an Ascii stream value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @param length
     *            the length of the data to write from the stream
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateAsciiStream(String columnName, InputStream x, int length)
            throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code
     * java.sql.BigDecimal} value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateBigDecimal(int columnIndex, BigDecimal x)
            throws SQLException;

    /**
     * Updates a column specified by a column name with a {@code
     * java.sql.BigDecimal} value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateBigDecimal(String columnName, BigDecimal x)
            throws SQLException;

    /**
     * Updates a column specified by a column index with a binary stream value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @param length
     *            the number of bytes to be read from the the stream.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateBinaryStream(int columnIndex, InputStream x, int length)
            throws SQLException;

    /**
     * Updates a column specified by a column name with a binary stream value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @param length
     *            he number of bytes to be read from the the stream.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateBinaryStream(String columnName, InputStream x, int length)
            throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code java.sql.Blob}
     * value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateBlob(int columnIndex, Blob x) throws SQLException;

    /**
     * Updates a column specified by a column name with a {@code java.sql.Blob}
     * value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateBlob(String columnName, Blob x) throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code boolean}
     * value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateBoolean(int columnIndex, boolean x) throws SQLException;

    /**
     * Updates a column specified by a column name with a {@code boolean} value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateBoolean(String columnName, boolean x) throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code byte} value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateByte(int columnIndex, byte x) throws SQLException;

    /**
     * Updates a column specified by a column name with a {@code byte} value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateByte(String columnName, byte x) throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code byte} array
     * value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateBytes(int columnIndex, byte[] x) throws SQLException;

    /**
     * Updates a column specified by a column name with a byte array value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateBytes(String columnName, byte[] x) throws SQLException;

    /**
     * Updates a column specified by a column index with a character stream
     * value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @param length
     *            the length of data to write from the stream
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateCharacterStream(int columnIndex, Reader x, int length)
            throws SQLException;

    /**
     * Updates a column specified by a column name with a character stream
     * value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param reader
     *            the new value for the specified column.
     * @param length
     *            the length of data to write from the Reader
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateCharacterStream(String columnName, Reader reader,
            int length) throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code java.sql.Clob}
     * value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateClob(int columnIndex, Clob x) throws SQLException;

    /**
     * Updates a column specified by a column name with a {@code java.sql.Clob}
     * value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateClob(String columnName, Clob x) throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code java.sql.Date}
     * value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateDate(int columnIndex, Date x) throws SQLException;

    /**
     * Updates a column specified by a column name with a {@code java.sql.Date}
     * value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateDate(String columnName, Date x) throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code double} value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateDouble(int columnIndex, double x) throws SQLException;

    /**
     * Updates a column specified by a column name with a {@code double} value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateDouble(String columnName, double x) throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code float} value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateFloat(int columnIndex, float x) throws SQLException;

    /**
     * Updates a column specified by a column name with a {@code float} value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateFloat(String columnName, float x) throws SQLException;

    /**
     * Updates a column specified by a column index with an {@code int} value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateInt(int columnIndex, int x) throws SQLException;

    /**
     * Updates a column specified by a column name with an {@code int} value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateInt(String columnName, int x) throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code long} value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column..
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateLong(int columnIndex, long x) throws SQLException;

    /**
     * Updates a column specified by a column name with a {@code long} value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateLong(String columnName, long x) throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code null} value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateNull(int columnIndex) throws SQLException;

    /**
     * Updates a column specified by a column name with a {@code null} value.
     *
     * @param columnName
     *            the name of the column to update.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateNull(String columnName) throws SQLException;

    /**
     * Updates a column specified by a column index with an {@code Object}
     * value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateObject(int columnIndex, Object x) throws SQLException;

    /**
     * Updates a column specified by a column index with an {@code Object}
     * value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @param scale
     *            for the types {@code java.sql.Types.DECIMAL} or {@code
     *            java.sql.Types.NUMERIC}, this specifies the number of digits
     *            after the decimal point.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateObject(int columnIndex, Object x, int scale)
            throws SQLException;

    /**
     * Updates a column specified by a column name with an {@code Object} value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateObject(String columnName, Object x) throws SQLException;

    /**
     * Updates a column specified by a column name with an {@code Object} value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @param scale
     *            for the types {@code java.sql.Types.DECIMAL} or {@code
     *            java.sql.Types.NUMERIC}, this specifies the number of digits
     *            after the decimal point.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateObject(String columnName, Object x, int scale)
            throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code java.sql.Ref}
     * value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateRef(int columnIndex, Ref x) throws SQLException;

    /**
     * Updates a column specified by a column name with a {@code java.sql.Ref}
     * value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateRef(String columnName, Ref x) throws SQLException;

    /**
     * Updates the database with the new contents of the current row of this
     * {@code ResultSet} object.
     *
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateRow() throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code short} value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateShort(int columnIndex, short x) throws SQLException;

    /**
     * Updates a column specified by a column name with a {@code short} value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateShort(String columnName, short x) throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code String} value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateString(int columnIndex, String x) throws SQLException;

    /**
     * Updates a column specified by a column name with a {@code String} value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateString(String columnName, String x) throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code Time} value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateTime(int columnIndex, Time x) throws SQLException;

    /**
     * Updates a column specified by a column name with a {@code Time} value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateTime(String columnName, Time x) throws SQLException;

    /**
     * Updates a column specified by a column index with a {@code Timestamp}
     * value.
     *
     * @param columnIndex
     *            the index of the column to update.
     * @param x
     *            the new timestamp value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateTimestamp(int columnIndex, Timestamp x)
            throws SQLException;

    /**
     * Updates a column specified by column name with a {@code Timestamp} value.
     *
     * @param columnName
     *            the name of the column to update.
     * @param x
     *            the new timestamp value for the specified column.
     * @throws SQLException
     *             if a database error happens.
     */
    public void updateTimestamp(String columnName, Timestamp x)
            throws SQLException;

    /**
     * Determines whether the last column read from this {@code ResultSet}
     * contained SQL {@code NULL}.
     *
     * @return {@code {@code true} if the last column contained SQL {@code
     *         NULL}, {@code false} otherwise
     * @throws SQLException
     *             if a database error happens.
     */
    public boolean wasNull() throws SQLException;

    /**
     * Returns a {@code RowId} corresponding to the SQL ROWID at the 1-based {@code columnIndex}.
     * @throws SQLException
     */
    public RowId getRowId(int columnIndex) throws SQLException;

    /**
     * Returns a {@code RowId} corresponding to the SQL ROWID at the named column.
     * @throws SQLException
     */
    public RowId getRowId(String columnLabel) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateRowId(int columnIndex, RowId value) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateRowId(String columnLabel, RowId value) throws SQLException;

    /**
     * Returns the holdability of this result set: {@link #HOLD_CURSORS_OVER_COMMIT} or
     * {@link #CLOSE_CURSORS_AT_COMMIT}.
     * @throws SQLException
     */
    public int getHoldability() throws SQLException;

    /**
     * Returns true if this result set has been closed, false otherwise.
     * @throws SQLException
     */
    public boolean isClosed() throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateNString(int columnIndex, String nString) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateNString(String columnLabel, String nString) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException;

    /**
     * Returns an {@code NClob} corresponding to the value at the 1-based {@code columnIndex}.
     * @throws SQLException
     */
    public NClob getNClob(int columnIndex) throws SQLException;

    /**
     * Returns an {@code NClob} corresponding to the value in the named column.
     * @throws SQLException
     */
    public NClob getNClob(String columnLabel) throws SQLException;

    /**
     * Returns an {@code SQLXML} corresponding to the value at the 1-based {@code columnIndex}.
     * @throws SQLException
     */
    public SQLXML getSQLXML(int columnIndex) throws SQLException;

    /**
     * Returns an {@code SQLXML} corresponding to the value in the named column.
     * @throws SQLException
     */
    public SQLXML getSQLXML(String columnLabel) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException;

    /**
     * Returns a {@code String} corresponding to the value at the 1-based {@code columnIndex}.
     * @throws SQLException
     */
    public String getNString(int columnIndex) throws SQLException;

    /**
     * Returns a {@code String} corresponding to the value in the named column.
     * @throws SQLException
     */
    public String getNString(String columnLabel) throws SQLException;

    /**
     * Returns a {@code Reader} corresponding to the value at the 1-based {@code columnIndex}.
     * @throws SQLException
     */
    public Reader getNCharacterStream(int columnIndex) throws SQLException;

    /**
     * Returns a {@code Reader} corresponding to the value in the named column.
     * @throws SQLException
     */
    public Reader getNCharacterStream(String columnLabel) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateClob(int columnIndex, Reader reader) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateClob(String columnLabel, Reader reader) throws SQLException;

    /**
     * Updates the value at the 1-based {@code columnIndex}.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateNClob(int columnIndex, Reader reader) throws SQLException;

    /**
     * Updates the value in the named column.
     * The underlying database isn't changed until the next row update or insert operation.
     * @throws SQLException
     */
    public void updateNClob(String columnLabel, Reader reader) throws SQLException;
}
