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

/**
 * Interface used for executing static SQL statements to retrieve query results.
 * The resulting table rows are returned as {@code ResultSet}s. For any given
 * {@code Statement} object, only one {@code ResultSet} can be opened at one
 * time. A call to any of the execution methods of {@code Statement} will cause
 * any previously created {@code ResultSet} object for that {@code Statement} to
 * be closed implicitly.
 * <p>
 * To have multiple {@code ResultSet} objects opened concurrently, multiple
 * {@code Statement} objects must be created and then executed.
 * <p>
 * To obtain such an executable statement one needs to invoke {@code
 * Connection#createStatement}.
 *
 * @see ResultSet
 * @see Connection#createStatement
 */
public interface Statement extends Wrapper, AutoCloseable {

    /**
     * Passing this constant to {@link #getMoreResults} implies that all {@code
     * ResultSet} objects previously kept open should be closed.
     */
    public static final int CLOSE_ALL_RESULTS = 3;

    /**
     * Passing this constant to {@link #getMoreResults} implies that the current
     * {@code ResultSet} object should be closed.
     */
    public static final int CLOSE_CURRENT_RESULT = 1;

    /**
     * Indicates that an error was encountered during execution of a batch
     * statement.
     */
    public static final int EXECUTE_FAILED = -3;

    /**
     * Passing this constant to <i>getMoreResults</i> implies that the current
     * {@code ResultSet} object should not be closed.
     */
    public static final int KEEP_CURRENT_RESULT = 2;

    /**
     * Indicates that generated keys should not be accessible for retrieval.
     */
    public static final int NO_GENERATED_KEYS = 2;

    /**
     * Indicates that generated keys should be accessible for retrieval.
     */
    public static final int RETURN_GENERATED_KEYS = 1;

    /**
     * Indicates that a batch statement was executed with a successful result,
     * but a count of the number of rows it affected is unavailable.
     */
    public static final int SUCCESS_NO_INFO = -2;

    /**
     * Adds a specified SQL command to the list of commands for this {@code
     * Statement}.
     * <p>
     * The list of commands is executed by invoking the {@code executeBatch}
     * method.
     *
     * @param sql
     *            the SQL command as a String. Typically an {@code INSERT} or
     *            {@code UPDATE} statement.
     * @throws SQLException
     *             if an error occurs accessing the database or the database
     *             does not support batch updates.
     */
    public void addBatch(String sql) throws SQLException;

    /**
     * Cancels this statement's execution if both the database and the JDBC
     * driver support aborting an SQL statement in flight. This method can be
     * used by one thread to stop a statement that is executed on another
     * thread.
     *
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public void cancel() throws SQLException;

    /**
     * Clears the current list of SQL commands for this statement.
     *
     * @throws SQLException
     *             if an error occurs accessing the database or the database
     *             does not support batch updates.
     */
    public void clearBatch() throws SQLException;

    /**
     * Clears all {@code SQLWarnings} from this statement.
     *
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public void clearWarnings() throws SQLException;

    /**
     * Releases this statement's database and JDBC driver resources.
     * <p>
     * Using this method to release these resources as soon as possible is
     * strongly recommended.
     * <p>
     * One should not rely on the resources being automatically released when
     * finalized during garbage collection. Doing so can result in unpredictable
     * behavior for the application.
     *
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public void close() throws SQLException;

    /**
     * Executes a supplied SQL statement. This may return multiple {@code
     * ResultSet}s.
     * <p>
     * Use the {@code getResultSet} or {@code getUpdateCount} methods to get the
     * first result and {@code getMoreResults} to get any subsequent results.
     *
     * @param sql
     *            the SQL statement to execute
     * @return {@code true} if the first result is a {@code ResultSet}, {@code
     *         false} if the first result is an update count or if there is no
     *         result.
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public boolean execute(String sql) throws SQLException;

    /**
     * Executes a supplied SQL statement. This may return multiple {@code
     * ResultSet}s. This method allows control of whether auto-generated Keys
     * should be made available for retrieval, if the SQL statement is an
     * {@code INSERT} statement.
     * <p>
     * Use the {@code getResultSet} or {@code getUpdateCount} methods to get the
     * first result and {@code getMoreResults} to get any subsequent results.
     *
     * @param sql
     *            the SQL statement to execute.
     * @param autoGeneratedKeys
     *            a flag indicating whether to make auto generated keys
     *            available for retrieval. This parameter must be one of {@code
     *            Statement.NO_GENERATED_KEYS} or {@code
     *            Statement.RETURN_GENERATED_KEYS}.
     * @return {@code true} if results exists and the first result is a {@code
     *         ResultSet}, {@code false} if the first result is an update count
     *         or if there is no result.
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public boolean execute(String sql, int autoGeneratedKeys)
            throws SQLException;

    /**
     * Executes the supplied SQL statement. This may return multiple {@code
     * ResultSet}s. This method allows retrieval of auto generated keys
     * specified by the supplied array of column indexes, if the SQL statement
     * is an {@code INSERT} statement.
     * <p>
     * Use the {@code getResultSet} or {@code getUpdateCount} methods to get the
     * first result and {@code getMoreResults} to get any subsequent results.
     *
     * @param sql
     *            the SQL statement to execute.
     * @param columnIndexes
     *            an array of indexes of the columns in the inserted row which
     *            should be made available for retrieval via the {@code
     *            getGeneratedKeys} method.
     * @return {@code true} if the first result is a {@code ResultSet}, {@code
     *         false} if the first result is an update count or if there is no
     *         result.
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public boolean execute(String sql, int[] columnIndexes) throws SQLException;

    /**
     * Executes the supplied SQL statement. This may return multiple {@code
     * ResultSet}s. This method allows retrieval of auto generated keys
     * specified by the supplied array of column indexes, if the SQL statement
     * is an {@code INSERT} statement.
     * <p>
     * Use the {@code getResultSet} or {@code getUpdateCount} methods to get the
     * first result and {@code getMoreResults} to get any subsequent results.
     *
     * @param sql
     *            the SQL statement to execute.
     * @param columnNames
     *            an array of column names in the inserted row which should be
     *            made available for retrieval via the {@code getGeneratedKeys}
     *            method.
     * @return {@code true} if the first result is a {@code ResultSet}, {@code
     *         false} if the first result is an update count or if there is no
     *         result
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public boolean execute(String sql, String[] columnNames)
            throws SQLException;

    /**
     * Submits a batch of SQL commands to the database. Returns an array of
     * update counts, if all the commands execute successfully.
     * <p>
     * If one of the commands in the batch fails, this method can throw a
     * {@link BatchUpdateException} and the JDBC driver may or may not process
     * the remaining commands. The JDBC driver must behave consistently with the
     * underlying database, following the "all or nothing" principle. If the
     * driver continues processing, the array of results returned contains the
     * same number of elements as there are commands in the batch, with a
     * minimum of one of the elements having the {@code EXECUTE_FAILED} value.
     *
     * @return an array of update counts, with one entry for each command in the
     *         batch. The elements are ordered according to the order in which
     *         the commands were added to the batch.
     *         <p>
     *         <ol>
     *         <li>If the value of an element is &ge; 0, the corresponding
     *         command completed successfully and the value is the <i>update
     *         count</i> (the number of rows in the database affected by the
     *         command) for that command.</li>
     *         <li>If the value is {@code SUCCESS_NO_INFO}, the command
     *         completed successfully but the number of rows affected is
     *         unknown.
     *         <li>
     *         <li>If the value is {@code EXECUTE_FAILED}, the command failed.
     *         </ol>
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public int[] executeBatch() throws SQLException;

    /**
     * Executes a supplied SQL statement. Returns a single {@code ResultSet}.
     *
     * @param sql
     *            an SQL statement to execute. Typically a {@code SELECT}
     *            statement
     * @return a {@code ResultSet} containing the data produced by the SQL
     *         statement. Never null.
     * @throws SQLException
     *             if an error occurs accessing the database or if the statement
     *             produces anything other than a single {@code ResultSet}.
     */
    public ResultSet executeQuery(String sql) throws SQLException;

    /**
     * Executes the supplied SQL statement. The statement may be an {@code
     * INSERT}, {@code UPDATE} or {@code DELETE} statement or a statement which
     * returns nothing.
     *
     * @param sql
     *            an SQL statement to execute - an SQL {@code INSERT}, {@code
     *            UPDATE}, {@code DELETE} or a statement which returns nothing
     * @return the count of updated rows, or 0 for a statement that returns
     *         nothing.
     * @throws SQLException
     *             if an error occurs accessing the database or if the statement
     *             produces a {@code ResultSet}.
     */
    public int executeUpdate(String sql) throws SQLException;

    /**
     * Executes the supplied SQL statement. This method allows control of
     * whether auto-generated Keys should be made available for retrieval.
     *
     * @param sql
     *            an SQL statement to execute - an SQL {@code INSERT}, {@code
     *            UPDATE}, {@code DELETE} or a statement which does not return
     *            anything.
     * @param autoGeneratedKeys
     *            a flag that indicates whether to allow retrieval of auto
     *            generated keys. Parameter must be one of {@code
     *            Statement.RETURN_GENERATED_KEYS} or {@code
     *            Statement.NO_GENERATED_KEYS}
     * @return the number of updated rows, or 0 if the statement returns
     *         nothing.
     * @throws SQLException
     *             if an error occurs accessing the database or if the statement
     *             produces a {@code ResultSet}.
     */
    public int executeUpdate(String sql, int autoGeneratedKeys)
            throws SQLException;

    /**
     * Executes the supplied SQL statement. This method allows retrieval of auto
     * generated keys specified by the supplied array of column indexes.
     *
     * @param sql
     *            an SQL statement to execute - an SQL {@code INSERT}, {@code
     *            UPDATE}, {@code DELETE} or a statement which returns nothing
     * @param columnIndexes
     *            an array of indexes of the columns in the inserted row which
     *            should be made available for retrieval via the {@code
     *            getGeneratedKeys} method.
     * @return the count of updated rows, or 0 for a statement that returns
     *         nothing.
     * @throws SQLException
     *             if an error occurs accessing the database or if the statement
     *             produces a {@code ResultSet}.
     */
    public int executeUpdate(String sql, int[] columnIndexes)
            throws SQLException;

    /**
     * Executes the supplied SQL statement. This method allows retrieval of auto
     * generated keys specified by the supplied array of column names.
     *
     * @param sql
     *            an SQL statement to execute - an SQL {@code INSERT}, {@code
     *            UPDATE}, {@code DELETE} or a statement which returns nothing
     * @param columnNames
     *            an array of column names in the inserted row which should be
     *            made available for retrieval via the {@code getGeneratedKeys}
     *            method.
     * @return the count of updated rows, or 0 for a statement that returns
     *         nothing.
     * @throws SQLException
     *             if an error occurs accessing the database or if the statement
     *             produces a {@code ResultSet}.
     */
    public int executeUpdate(String sql, String[] columnNames)
            throws SQLException;

    /**
     * Gets the {@code Connection} object which created this statement.
     *
     * @return the {@code Connection} through which this statement is
     *         transmitted to the database.
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public Connection getConnection() throws SQLException;

    /**
     * Gets the default direction for fetching rows for {@code ResultSet}s
     * generated from this statement.
     *
     * @return the default fetch direction, one of:
     *         <ul>
     *         <li>ResultSet.FETCH_FORWARD</li> <li>ResultSet.FETCH_REVERSE</li>
     *         <li>ResultSet.FETCH_UNKNOWN</li>
     *         </ul>
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public int getFetchDirection() throws SQLException;

    /**
     * Gets the default number of rows for a fetch for the {@code ResultSet}
     * objects returned from this statement.
     *
     * @return the default fetch size for {@code ResultSet}s produced by this
     *         statement.
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public int getFetchSize() throws SQLException;

    /**
     * Returns auto generated keys created by executing this statement.
     *
     * @return a {@code ResultSet} containing the auto generated keys - empty if
     *         no keys are generated by this statement.
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public ResultSet getGeneratedKeys() throws SQLException;

    /**
     * Gets the maximum number of bytes which can be returned as values from
     * character and binary type columns in a {@code ResultSet} derived from this
     * statement. This limit applies to {@code BINARY}, {@code VARBINARY},
     * {@code LONGVARBINARY}, {@code CHAR}, {@code VARCHAR}, and {@code
     * LONGVARCHAR} types. Any data exceeding the maximum size is abandoned
     * without announcement.
     *
     * @return the current size limit, where {@code 0} means that there is no
     *         limit.
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public int getMaxFieldSize() throws SQLException;

    /**
     * Gets the maximum number of rows that a {@code ResultSet} can contain when
     * produced from this statement. If the limit is exceeded, the excess rows
     * are discarded silently.
     *
     * @return the current row limit, where {@code 0} means that there is no
     *         limit.
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public int getMaxRows() throws SQLException;

    /**
     * Moves to this statement's next result. Returns {@code true} if it is a
     * {@code ResultSet}. Any current {@code ResultSet} objects previously
     * obtained with {@code getResultSet()} are closed implicitly.
     *
     * @return {@code true} if the next result is a {@code ResultSet}, {@code
     *         false} if the next result is not a {@code ResultSet} or if there
     *         are no more results. Note that if there is no more data, this
     *         method will return {@code false} and {@code getUpdateCount} will
     *         return -1.
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public boolean getMoreResults() throws SQLException;

    /**
     * Moves to this statement's next result. Returns {@code true} if the next
     * result is a {@code ResultSet}. Any current {@code ResultSet} objects
     * previously obtained with {@code getResultSet()} are handled as indicated
     * by a supplied Flag parameter.
     *
     * @param current
     *            a flag indicating what to do with existing {@code ResultSet}s.
     *            This parameter must be one of {@code
     *            Statement.CLOSE_ALL_RESULTS}, {@code
     *            Statement.CLOSE_CURRENT_RESULT} or {@code
     *            Statement.KEEP_CURRENT_RESULT}.
     * @return {@code true} if the next result exists and is a {@code ResultSet}
     *         , {@code false} if the next result is not a {@code ResultSet} or
     *         if there are no more results. Note that if there is no more data,
     *         this method will return {@code false} and {@code getUpdateCount}
     *         will return -1.
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public boolean getMoreResults(int current) throws SQLException;

    /**
     * Gets the timeout value for the statement's execution time. The JDBC
     * driver will wait up to this value for the execution to complete - after
     * the limit is exceeded an SQL {@code Exception} is thrown.
     *
     * @return the current query timeout value, where {@code 0} indicates that
     *         there is no current timeout.
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public int getQueryTimeout() throws SQLException;

    /**
     * Gets the current result. Should only be called once per result.
     *
     * @return the {@code ResultSet} for the current result. {@code null} if the
     *         result is an update count or if there are no more results.
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public ResultSet getResultSet() throws SQLException;

    /**
     * Gets the concurrency setting for {@code ResultSet} objects generated by
     * this statement.
     *
     * @return {@code ResultSet.CONCUR_READ_ONLY} or {@code
     *         ResultSet.CONCUR_UPDATABLE}.
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public int getResultSetConcurrency() throws SQLException;

    /**
     * Gets the cursor hold setting for {@code ResultSet} objects generated by
     * this statement.
     *
     * @return {@code ResultSet.HOLD_CURSORS_OVER_COMMIT} or {@code
     *         ResultSet.CLOSE_CURSORS_AT_COMMIT}
     * @throws SQLException
     *             if there is an error while accessing the database.
     */
    public int getResultSetHoldability() throws SQLException;

    /**
     * Gets the {@code ResultSet} type setting for {@code ResultSet}s derived
     * from this statement.
     *
     * @return {@code ResultSet.TYPE_FORWARD_ONLY} for a {@code ResultSet} where
     *         the cursor can only move forwards, {@code
     *         ResultSet.TYPE_SCROLL_INSENSITIVE} for a {@code ResultSet} which
     *         is scrollable but is not sensitive to changes made by others,
     *         {@code ResultSet.TYPE_SCROLL_SENSITIVE} for a {@code ResultSet}
     *         which is scrollable but is sensitive to changes made by others.
     * @throws SQLException
     *             if there is an error accessing the database.
     */
    public int getResultSetType() throws SQLException;

    /**
     * Gets an update count for the current result if it is not a {@code
     * ResultSet}.
     *
     * @return the current result as an update count. {@code -1} if the current
     *         result is a {@code ResultSet} or if there are no more results.
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public int getUpdateCount() throws SQLException;

    /**
     * Retrieves the first {@code SQLWarning} reported by calls on this
     * statement. If there are multiple warnings, subsequent warnings are
     * chained to the first one. The chain of warnings is cleared each time the
     * statement is executed.
     * <p>
     * Warnings associated with reads from the {@code ResultSet} returned from
     * executing the statement will be attached to the {@code ResultSet}, not the
     * statement object.
     *
     * @return an SQLWarning, null if there are no warnings
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public SQLWarning getWarnings() throws SQLException;

    /**
     * Sets the SQL cursor name. This name is used by subsequent statement
     * execute methods.
     * <p>
     * Cursor names must be unique within one Connection.
     * <p>
     * With the cursor name set, it can then be used in SQL positioned
     * update or delete statements to determine the current row in a {@code
     * ResultSet} generated from this statement. The positioned update or delete
     * must be done with a different statement than this one.
     *
     * @param name
     *            the Cursor name as a string,
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public void setCursorName(String name) throws SQLException;

    /**
     * Sets Escape Processing mode.
     * <p>
     * If Escape Processing is on, the JDBC driver will do escape substitution
     * on an SQL statement before sending it for execution. This does not apply
     * to {@link PreparedStatement}s since they are processed when created,
     * before this method can be called.
     *
     * @param enable
     *            {@code true} to set escape processing mode <i>on</i>, {@code
     *            false} to turn it <i>off</i>.
     * @throws SQLException
     *             if an error occurs accessing the database.
     */
    public void setEscapeProcessing(boolean enable) throws SQLException;

    /**
     * Sets the fetch direction - a hint to the JDBC driver about the direction
     * of processing of rows in {@code ResultSet}s created by this statement.
     * The default fetch direction is {@code FETCH_FORWARD}.
     *
     * @param direction
     *            which fetch direction to use. This parameter should be one of
     *            <ul>
     *            <li>{@code ResultSet.FETCH_UNKNOWN}</li>
     *            <li>{@code ResultSet.FETCH_FORWARD}</li>
     *            <li>{@code ResultSet.FETCH_REVERSE}</li>
     *            </ul>
     * @throws SQLException
     *             if there is an error while accessing the database or if the
     *             fetch direction is unrecognized.
     */
    public void setFetchDirection(int direction) throws SQLException;

    /**
     * Sets the fetch size. This is a hint to the JDBC driver about how many
     * rows should be fetched from the database when more are required by
     * application processing.
     *
     * @param rows
     *            the number of rows that should be fetched. {@code 0} tells the driver
     *            to ignore the hint. Should be less than {@code getMaxRows} for
     *            this statement. Should not be negative.
     * @throws SQLException
     *             if an error occurs accessing the database, or if the rows
     *             parameter is out of range.
     */
    public void setFetchSize(int rows) throws SQLException;

    /**
     * Sets the maximum number of bytes for {@code ResultSet} columns that
     * contain character or binary values. This applies to {@code BINARY},
     * {@code VARBINARY}, {@code LONGVARBINARY}, {@code CHAR}, {@code VARCHAR},
     * and {@code LONGVARCHAR} fields. Any data exceeding the maximum size is
     * abandoned without announcement.
     *
     * @param max
     *            the maximum field size in bytes. {@code 0} means "no limit".
     * @throws SQLException
     *             if an error occurs accessing the database or the {@code max}
     *             value is &lt; {@code 0}.
     */
    public void setMaxFieldSize(int max) throws SQLException;

    /**
     * Sets the maximum number of rows that any {@code ResultSet} can contain.
     * If the number of rows exceeds this value, the additional rows are
     * silently discarded.
     *
     * @param max
     *            the maximum number of rows. {@code 0} means "no limit".
     * @throws SQLException
     *             if an error occurs accessing the database or if max < {@code
     *             0}.
     */
    public void setMaxRows(int max) throws SQLException;

    /**
     * Sets the timeout, in seconds, for queries - how long the driver will
     * allow for completion of a statement execution. If the timeout is
     * exceeded, the query will throw an {@code SQLException}.
     *
     * @param seconds
     *            timeout in seconds. 0 means no timeout ("wait forever")
     * @throws SQLException
     *             if an error occurs accessing the database or if seconds <
     *             {@code 0}.
     */
    public void setQueryTimeout(int seconds) throws SQLException;

    /**
     * Returns true if this statement has been closed, false otherwise.
     */
    public boolean isClosed() throws SQLException;

    /**
     * Hints whether this statement should be pooled. Defaults to false for {@code Statement},
     * but true for {@code CallableStatement} and {@code PreparedStatement}. Pool manager
     * implementations may or may not honor this hint.
     */
    public void setPoolable(boolean poolable) throws SQLException;

    /**
     * Returns true if this statement is poolable, false otherwise.
     */
    public boolean isPoolable() throws SQLException;
}
