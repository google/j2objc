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

import java.util.Map;
import java.util.Properties;

/**
 * A connection represents a link from a Java application to a database. All SQL
 * statements and results are returned within the context of a connection.
 * Database statements that are executed within this context form a
 * database session which forms one or more closed transactions. Especially in
 * distributed applications, multiple concurrent connections may exist accessing
 * the same values of the database. which may lead to the following phenomena
 * (referred to as <i>transaction isolation levels</i>):
 * <ul>
 * <li><i>dirty reads</i>:<br>
 * reading values from table rows that are not committed.</br></li>
 * <li><i>non-repeatable reads</i>:<br>
 * reading table rows more than once in a transaction but getting back different
 * data because other transactions have altered the rows between the reads.</br></li>
 * <li><i>phantom reads</i>:<br>
 * retrieving additional "phantom" rows in the course of repeated table reads
 * because other transactions have inserted additional rows that satisfy an
 * SQL {@code WHERE} clause</br></li>
 * </ul>
 */
public interface Connection extends Wrapper, AutoCloseable {

    /**
     * A constant indicating that transactions are not supported.
     */
    public static final int TRANSACTION_NONE = 0;

    /**
     * No <i>dirty reads</i> are permitted, therefore transactions may not read
     * a row containing uncommitted values - but does not prevent an application
     * from <i>non-repeatable reads</i> and <i>phantom reads</i>.
     */
    public static final int TRANSACTION_READ_COMMITTED = 2;

    /**
     * In the case that reading uncommitted values is allowed, the following
     * incidents may happen which may lead to an invalid results:
     * <ul>
     * <li><i>dirty reads</i></li>
     * <li><i>non-repeatable reads</i></li>
     * <li><i>phantom reads</i></li>
     * </ul>
     */
    public static final int TRANSACTION_READ_UNCOMMITTED = 1;

    /**
     * A constant indicating that <i>dirty reads</i> and <i>non-repeatable
     * reads</i> are <b>prevented</b> but <i>phantom reads</i> can occur.
     */
    public static final int TRANSACTION_REPEATABLE_READ = 4;

    /**
     * The constant that indicates that the following incidents are <b>all
     * prevented</b> (the opposite of {@link #TRANSACTION_READ_UNCOMMITTED}):
     * <ul>
     * <li><i>dirty reads</i></li>
     * <li><i>non-repeatable reads</i></li>
     * <li><i>phantom reads</i></li>
     * </ul>
     */
    public static final int TRANSACTION_SERIALIZABLE = 8;

    /**
     * Discards all warnings that may have arisen for this connection.
     * Subsequent calls to {@link #getWarnings()} will return {@code null}
     * up until a new warning condition occurs.
     *
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public void clearWarnings() throws SQLException;

    /**
     * Causes the instant release of all database and driver connection
     * resources associated with this object. Any subsequent invocations of this
     * method have no effect.
     * <p>
     * It is strongly recommended that all connections are closed before they
     * are dereferenced by the application ready for garbage collection.
     * Although the {@code finalize} method of the connection closes the
     * connection before garbage collection takes place, it is not advisable to
     * leave the {@code close} operation to take place in this way. Mainly
     * because undesired side-effects may appear.
     *
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public void close() throws SQLException;

    /**
     * Commits all of the changes made since the last {@code commit} or
     * {@code rollback} of the associated transaction. All locks in the database
     * held by this connection are also relinquished. Calling this operation on
     * connection objects in {@code auto-commit} mode leads to an error.
     *
     * @throws SQLException
     *             if there is a problem accessing the database or if the target
     *             connection instance is in auto-commit mode.
     */
    public void commit() throws SQLException;

    /**
     * Returns a new instance of {@code Statement} for issuing SQL commands to
     * the remote database.
     * <p>
     * {@code ResultSets} generated by the returned statement will default to
     * type {@code ResultSet.TYPE_FORWARD_ONLY} and concurrency level {@code
     * ResultSet.CONCUR_READ_ONLY}.
     *
     * @return a {@code Statement} object with default settings.
     * @throws SQLException
     *             if there is a problem accessing the database.
     * @see ResultSet
     */
    public Statement createStatement() throws SQLException;

    /**
     * Returns a new instance of {@code Statement} whose associated {@code
     * ResultSet}s have the characteristics specified in the type and
     * concurrency arguments.
     *
     * @param resultSetType
     *            one of the following type specifiers:
     *            <ul>
     *            <li>{@link ResultSet#TYPE_SCROLL_SENSITIVE} </li> <li>
     *            {@link ResultSet#TYPE_SCROLL_INSENSITIVE} </li> <li>
     *            {@link ResultSet#TYPE_FORWARD_ONLY}</li>
     *            </ul>
     * @param resultSetConcurrency
     *            one of the following concurrency mode specifiers:
     *            <ul>
     *            <li>{@link ResultSet#CONCUR_UPDATABLE}</li> <li>
     *            {@link ResultSet#CONCUR_READ_ONLY}</li>
     *            </ul>
     * @return a new instance of {@code Statement} capable of manufacturing
     *         {@code ResultSet}s that satisfy the specified {@code
     *         resultSetType} and {@code resultSetConcurrency} values.
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws SQLException;

    /**
     * Returns a new instance of {@code Statement} whose associated
     * {@code ResultSet}s will have the characteristics specified in the
     * type, concurrency and holdability arguments.
     *
     * @param resultSetType
     *            one of the following type specifiers:
     *            <ul>
     *            <li>{@link ResultSet#TYPE_SCROLL_SENSITIVE}</li>
     *            <li>{@link ResultSet#TYPE_SCROLL_INSENSITIVE}</li>
     *            <li>{@link ResultSet#TYPE_FORWARD_ONLY}</li>
     *            </ul>
     * @param resultSetConcurrency
     *            one of the following concurrency mode specifiers:
     *            <ul>
     *            <li>{@link ResultSet#CONCUR_UPDATABLE}</li>
     *            <li>{@link ResultSet#CONCUR_READ_ONLY}</li>
     *            </ul>
     * @param resultSetHoldability
     *            one of the following holdability mode specifiers:
     *            <ul>
     *            <li>{@link ResultSet#HOLD_CURSORS_OVER_COMMIT}</li>
     *            <li>{@link ResultSet#CLOSE_CURSORS_AT_COMMIT}</li>
     *            </ul>
     * @return a new instance of {@code Statement} capable of
     *         manufacturing {@code ResultSet}s that satisfy the
     *         specified {@code resultSetType},
     *         {@code resultSetConcurrency} and
     *         {@code resultSetHoldability} values.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public Statement createStatement(int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException;

    /**
     * Returns a {@code boolean} indicating whether or not this connection is in
     * the {@code auto-commit} operating mode.
     *
     * @return {@code true} if {@code auto-commit} is on, otherwise {@code
     *         false}.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public boolean getAutoCommit() throws SQLException;

    /**
     * Gets this {@code Connection} object's current catalog name.
     *
     * @return the catalog name. {@code null} if there is no catalog
     *         name.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public String getCatalog() throws SQLException;

    /**
     * Returns the holdability property that any {@code ResultSet} produced by
     * this instance will have.
     *
     * @return one of the following holdability mode specifiers:
     *         <ul>
     *         <li>{@link ResultSet#HOLD_CURSORS_OVER_COMMIT}</li> <li>
     *         {@link ResultSet#CLOSE_CURSORS_AT_COMMIT}</li>
     *         </ul>
     * @throws SQLException
     *             if there is a problem accessing the a database.
     */
    public int getHoldability() throws SQLException;

    /**
     * Gets the metadata about the database referenced by this connection. The
     * returned {@code DatabaseMetaData} describes the database topography,
     * available stored procedures, SQL syntax and so on.
     *
     * @return a {@code DatabaseMetaData} object containing the database
     *         description.
     * @throws SQLException
     *             if there is a problem accessing the a database.
     */
    public DatabaseMetaData getMetaData() throws SQLException;

    /**
     * Returns the transaction isolation level for this connection.
     *
     * @return the transaction isolation value.
     * @throws SQLException
     *             if there is a problem accessing the database.
     * @see #TRANSACTION_NONE
     * @see #TRANSACTION_READ_COMMITTED
     * @see #TRANSACTION_READ_UNCOMMITTED
     * @see #TRANSACTION_REPEATABLE_READ
     * @see #TRANSACTION_SERIALIZABLE
     */
    public int getTransactionIsolation() throws SQLException;

    /**
     * Returns the type mapping associated with this {@code Connection} object.
     * The type mapping must be set on the application level.
     *
     * @return the Type Map as a {@code java.util.Map}.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public Map<String, Class<?>> getTypeMap() throws SQLException;

    /**
     * Gets the first instance of any {@code SQLWarning} objects that may have
     * been created in the use of this connection. If at least one warning has
     * occurred then this operation returns the first one reported. A {@code
     * null} indicates that no warnings have occurred.
     * <p>
     * By invoking the {@link SQLWarning#getNextWarning()} method of the
     * returned {@code SQLWarning} object it is possible to obtain all of
     * this connection's warning objects.
     *
     * @return the first warning as an SQLWarning object (may be {@code null}).
     * @throws SQLException
     *             if there is a problem accessing the database or if the call
     *             has been made on a connection which has been previously
     *             closed.
     */
    public SQLWarning getWarnings() throws SQLException;

    /**
     * Returns a {@code boolean} indicating whether or not this connection is in
     * the {@code closed} state. The {@code closed} state may be entered into as
     * a consequence of a successful invocation of the {@link #close()} method
     * or else if an error has occurred that prevents the connection from
     * functioning normally.
     *
     * @return {@code true} if closed, otherwise {@code false}.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public boolean isClosed() throws SQLException;

    /**
     * Returns a {@code boolean} indicating whether or not this connection is
     * currently in the {@code read-only} state.
     *
     * @return {@code true} if in read-only state, otherwise {@code false}.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public boolean isReadOnly() throws SQLException;

    /**
     * Returns a string representation of the input SQL statement
     * {@code sql} expressed in the underlying system's native SQL
     * syntax.
     *
     * @param sql
     *            the JDBC form of an SQL statement.
     * @return the SQL statement in native database format.
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public String nativeSQL(String sql) throws SQLException;

    /**
     * Returns a new instance of {@code CallableStatement} that may be used for
     * making stored procedure calls to the database.
     *
     * @param sql
     *            the SQL statement that calls the stored function
     * @return a new instance of {@code CallableStatement} representing the SQL
     *         statement. {@code ResultSet}s emitted from this {@code
     *         CallableStatement} will default to type
     *         {@link ResultSet#TYPE_FORWARD_ONLY} and concurrency
     *         {@link ResultSet#CONCUR_READ_ONLY}.
     * @throws SQLException
     *             if a problem occurs accessing the database.
     */
    public CallableStatement prepareCall(String sql) throws SQLException;

    /**
     * Returns a new instance of {@code CallableStatement} that may be used for
     * making stored procedure calls to the database. {@code ResultSet}s emitted
     * from this {@code CallableStatement} will satisfy the specified {@code
     * resultSetType} and {@code resultSetConcurrency} values.
     *
     * @param sql
     *            the SQL statement
     * @param resultSetType
     *            one of the following type specifiers:
     *            <ul>
     *            <li>{@link ResultSet#TYPE_SCROLL_SENSITIVE}</li>
     *            <li>{@link ResultSet#TYPE_SCROLL_INSENSITIVE}</li>
     *            <li>{@link ResultSet#TYPE_FORWARD_ONLY}</li>
     *            </ul>
     * @param resultSetConcurrency
     *            one of the following concurrency mode specifiers:
     *            <ul>
     *            <li>{@link ResultSet#CONCUR_READ_ONLY}</li>
     *            <li>{@link ResultSet#CONCUR_UPDATABLE}</li>
     *            </ul>
     * @return a new instance of {@code CallableStatement} representing the
     *         precompiled SQL statement. {@code ResultSet}s emitted from this
     *         {@code CallableStatement} will satisfy the specified {@code
     *         resultSetType} and {@code resultSetConcurrency} values.
     * @throws SQLException
     *             if a problem occurs accessing the database
     */
    public CallableStatement prepareCall(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException;

    /**
     * Returns a new instance of {@code CallableStatement} that may be used for
     * making stored procedure calls to the database. {@code ResultSet}s created
     * from this {@code CallableStatement} will have characteristics determined
     * by the specified type, concurrency and holdability arguments.
     *
     * @param sql
     *            the SQL statement
     * @param resultSetType
     *            one of the following type specifiers:
     *            <ul>
     *            <li>{@link ResultSet#TYPE_SCROLL_SENSITIVE}</li>
     *            <li>{@link ResultSet#TYPE_SCROLL_INSENSITIVE}</li>
     *            <li>{@link ResultSet#TYPE_FORWARD_ONLY}</li>
     *            </ul>
     * @param resultSetConcurrency
     *            one of the following concurrency mode specifiers:
     *            <ul>
     *            <li>{@link ResultSet#CONCUR_READ_ONLY}</li>
     *            <li>{@link ResultSet#CONCUR_UPDATABLE}</li>
     *            </ul>
     * @param resultSetHoldability
     *            one of the following holdability mode specifiers:
     *            <ul>
     *            <li>{@link ResultSet#HOLD_CURSORS_OVER_COMMIT}</li>
     *            <li>{@link ResultSet#CLOSE_CURSORS_AT_COMMIT}</li>
     *            </ul>
     * @return a new instance of {@code CallableStatement} representing the
     *         precompiled SQL statement. {@code ResultSet}s emitted from this
     *         {@code CallableStatement} will satisfy the specified {@code
     *         resultSetType}, {@code resultSetConcurrency} and {@code
     *         resultSetHoldability} values.
     * @throws SQLException
     *             if a problem occurs accessing the database.
     */
    public CallableStatement prepareCall(String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException;

    /**
     * Returns a new instance of {@code PreparedStatement} that may be used any
     * number of times to execute parameterized requests on the database server.
     * <p>
     * Subject to JDBC driver support, this operation will attempt to send the
     * precompiled version of the statement to the database. If
     * the driver does not support precompiled statements, the statement will
     * not reach the database server until it is executed. This distinction
     * determines the moment when {@code SQLException}s get raised.
     * <p>
     * By default, {@code ResultSet}s from the returned object will be
     * {@link ResultSet#TYPE_FORWARD_ONLY} type with a
     * {@link ResultSet#CONCUR_READ_ONLY} mode of concurrency.
     *
     * @param sql
     *            the SQL statement.
     * @return the {@code PreparedStatement} containing the supplied SQL
     *         statement.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException;

    /**
     * Creates a default {@code PreparedStatement} that can retrieve
     * automatically generated keys. Parameter {@code autoGeneratedKeys} may be
     * used to tell the driver whether such keys should be made accessible.
     * This is only relevant when the {@code sql} statement is an {@code insert}
     * statement.
     * <p>
     * An SQL statement which may have {@code IN} parameters can be stored and
     * precompiled in a {@code PreparedStatement}. The {@code PreparedStatement}
     * can then be then be used to execute the statement multiple times in an
     * efficient way.
     * <p>
     * Subject to JDBC driver support, this operation will attempt to send the
     * precompiled version of the statement to the database. If
     * the driver does not support precompiled statements, the statement will
     * not reach the database server until it is executed. This distinction
     * determines the moment when {@code SQLException}s get raised.
     * <p>
     * By default, {@code ResultSet}s from the returned object will be
     * {@link ResultSet#TYPE_FORWARD_ONLY} type with a
     * {@link ResultSet#CONCUR_READ_ONLY} mode of concurrency.
     *
     * @param sql
     *            the SQL statement.
     * @param autoGeneratedKeys
     *            one of the following generated key options:
     *            <ul>
     *            <li>{@link Statement#RETURN_GENERATED_KEYS}</li>
     *            <li>{@link Statement#NO_GENERATED_KEYS}</li>
     *            </ul>
     * @return a new {@code PreparedStatement} instance representing the input
     *         SQL statement.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
            throws SQLException;

    /**
     * Creates a default {@code PreparedStatement} that can retrieve the
     * auto-generated keys designated by a supplied array. If {@code sql} is an
     * SQL {@code INSERT} statement, the parameter {@code columnIndexes} is expected
     * to hold the index values for each column in the statement's intended
     * database table containing the autogenerated-keys of interest. Otherwise
     * {@code columnIndexes} is ignored.
     * <p>
     * Subject to JDBC driver support, this operation will attempt to send the
     * precompiled version of the statement to the database. If
     * the driver does not support precompiled statements, the statement will
     * not reach the database server until it is executed. This distinction
     * determines the moment when {@code SQLException}s get raised.
     * <p>
     * By default, {@code ResultSet}s from the returned object will be
     * {@link ResultSet#TYPE_FORWARD_ONLY} type with a
     * {@link ResultSet#CONCUR_READ_ONLY} concurrency mode.
     *
     * @param sql
     *            the SQL statement.
     * @param columnIndexes
     *            the indexes of the columns for which auto-generated keys
     *            should be made available.
     * @return the PreparedStatement containing the supplied SQL statement.
     * @throws SQLException
     *             if a problem occurs accessing the database.
     */
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
            throws SQLException;

    /**
     * Creates a {@code PreparedStatement} that generates {@code ResultSet}s
     * with the specified values of {@code resultSetType} and {@code
     * resultSetConcurrency}.
     *
     * @param sql
     *            the SQL statement. It can contain one or more {@code '?'}
     *            {@code IN} parameter placeholders.
     * @param resultSetType
     *            one of the following type specifiers:
     *            <ul>
     *            <li>{@link ResultSet#TYPE_SCROLL_SENSITIVE}</li>
     *            <li>{@link ResultSet#TYPE_SCROLL_INSENSITIVE}</li>
     *            <li>{@link ResultSet#TYPE_FORWARD_ONLY}</li>
     *            </ul>
     * @param resultSetConcurrency
     *            one of the following concurrency mode specifiers:
     *            <ul>
     *            <li>{@link ResultSet#CONCUR_READ_ONLY}</li>
     *            <li>{@link ResultSet#CONCUR_UPDATABLE}</li>
     *            </ul>
     * @return a new instance of {@code PreparedStatement} containing the SQL
     *         statement {@code sql}. {@code ResultSet}s emitted from this
     *         {@code PreparedStatement} will satisfy the specified {@code
     *         resultSetType} and {@code resultSetConcurrency} values.
     * @throws SQLException
     *             if a problem occurs accessing the database.
     */
    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency) throws SQLException;

    /**
     * Creates a {@code PreparedStatement} that generates {@code ResultSet}s
     * with the specified type, concurrency and holdability
     *
     * @param sql
     *            the SQL statement. It can contain one or more {@code '?' IN}
     *            parameter placeholders.
     * @param resultSetType
     *            one of the following type specifiers:
     *            <ul>
     *            <li>{@link ResultSet#TYPE_SCROLL_SENSITIVE}</li>
     *            <li>{@link ResultSet#TYPE_SCROLL_INSENSITIVE}</li>
     *            <li>{@link ResultSet#TYPE_FORWARD_ONLY}</li>
     *            </ul>
     * @param resultSetConcurrency
     *            one of the following concurrency mode specifiers:
     *            <ul>
     *            <li>{@link ResultSet#CONCUR_READ_ONLY}</li>
     *            <li>{@link ResultSet#CONCUR_UPDATABLE}</li>
     *            </ul>
     * @param resultSetHoldability
     *            one of the following holdability mode specifiers:
     *            <ul>
     *            <li>{@link ResultSet#HOLD_CURSORS_OVER_COMMIT}</li>
     *            <li>{@link ResultSet#CLOSE_CURSORS_AT_COMMIT}</li>
     *            </ul>
     * @return a new instance of {@code PreparedStatement} containing the SQL
     *         statement {@code sql}. {@code ResultSet}s emitted from this
     *         {@code PreparedStatement} will satisfy the specified {@code
     *         resultSetType}, {@code resultSetConcurrency} and {@code
     *         resultSetHoldability} values.
     * @throws SQLException
     *             if a problem occurs accessing the database.
     */
    public PreparedStatement prepareStatement(String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability)
            throws SQLException;

    /**
     * Creates a default {@code PreparedStatement} that can retrieve the
     * auto-generated keys designated by a supplied array. If {@code sql} is an
     * SQL {@code INSERT} statement, {@code columnNames} is expected to hold the
     * names of each column in the statement's associated database table
     * containing the autogenerated-keys of interest. Otherwise {@code
     * columnNames} is ignored.
     * <p>
     * Subject to JDBC driver support, this operation will attempt to send the
     * precompiled version of the statement to the database. Alternatively, if
     * the driver is not capable of handling precompiled statements, the
     * statement will not reach the database server until it is executed. This
     * will have a bearing on precisely <i>when</i> {@code SQLException}
     * instances get raised.
     * <p>
     * By default, ResultSets from the returned object will be
     * {@link ResultSet#TYPE_FORWARD_ONLY} type with a
     * {@link ResultSet#CONCUR_READ_ONLY} concurrency mode.
     *
     * @param sql
     *            the SQL statement.
     * @param columnNames
     *            the names of the columns for which auto-generated keys should
     *            be made available.
     * @return the PreparedStatement containing the supplied SQL statement.
     * @throws SQLException
     *             if a problem occurs accessing the database.
     */
    public PreparedStatement prepareStatement(String sql, String[] columnNames)
            throws SQLException;

    /**
     * Releases the specified {@code savepoint} from the present transaction. Once removed,
     * the {@code Savepoint} is considered invalid and should not be referenced
     * further.
     *
     * @param savepoint
     *            the object targeted for removal.
     * @throws SQLException
     *             if there is a problem with accessing the database or if
     *             {@code savepoint} is considered not valid in this
     *             transaction.
     */
    public void releaseSavepoint(Savepoint savepoint) throws SQLException;

    /**
     * Rolls back all updates made so far in this transaction and
     * relinquishes all acquired database locks. It is an error to invoke this
     * operation when in auto-commit mode.
     *
     * @throws SQLException
     *             if there is a problem with the database or if the method is
     *             called while in auto-commit mode of operation.
     */
    public void rollback() throws SQLException;

    /**
     * Undoes all changes made after the supplied {@code Savepoint} object was
     * set. This method should only be used when auto-commit mode is disabled.
     *
     * @param savepoint
     *            the Savepoint to roll back to
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public void rollback(Savepoint savepoint) throws SQLException;

    /**
     * Sets this connection's auto-commit mode {@code on} or {@code off}.
     * <p>
     * Putting a Connection into auto-commit mode means that all associated SQL
     * statements are run and committed as separate transactions.
     * By contrast, setting auto-commit to {@code off} means that associated SQL
     * statements get grouped into transactions that need to be completed by
     * explicit calls to either the {@link #commit()} or {@link #rollback()}
     * methods.
     * <p>
     * Auto-commit is the default mode for new connection instances.
     * <p>
     * When in this mode, commits will automatically occur upon successful SQL
     * statement completion or upon successful completion of an execute.
     * Statements are not considered successfully completed until all associated
     * {@code ResultSet}s and output parameters have been obtained or closed.
     * <p>
     * Calling this operation during an uncommitted transaction will result in
     * it being committed.
     *
     * @param autoCommit
     *            {@code boolean} indication of whether to put the target
     *            connection into auto-commit mode ({@code true}) or not (
     *            {@code false}).
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException;

    /**
     * Sets the catalog name for this connection. This is used to select a
     * subspace of the database for future work. If the driver does not support
     * catalog names, this method is ignored.
     *
     * @param catalog
     *            the catalog name to use.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public void setCatalog(String catalog) throws SQLException;

    /**
     * Sets the holdability of the {@code ResultSet}s created by this Connection.
     *
     * @param holdability
     *            one of the following holdability mode specifiers:
     *            <ul>
     *            <li>{@link ResultSet#CLOSE_CURSORS_AT_COMMIT}</li>
     *            <li>{@link ResultSet#HOLD_CURSORS_OVER_COMMIT}</li>
     *            <li>
     *            </ul>
     * @throws SQLException
     *             if there is a problem accessing the database
     */
    public void setHoldability(int holdability) throws SQLException;

    /**
     * Sets this connection to read-only mode.
     * <p>
     * This serves as a hint to the driver, which can enable database
     * optimizations.
     *
     * @param readOnly
     *            {@code true} to set the Connection to read only mode. {@code
     *            false} disables read-only mode.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public void setReadOnly(boolean readOnly) throws SQLException;

    /**
     * Creates an unnamed {@code Savepoint} in the current transaction.
     *
     * @return a {@code Savepoint} object for this savepoint.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public Savepoint setSavepoint() throws SQLException;

    /**
     * Creates a named {@code Savepoint} in the current transaction.
     *
     * @param name
     *            the name to use for the new {@code Savepoint}.
     * @return a {@code Savepoint} object for this savepoint.
     * @throws SQLException
     *             if there is a problem accessing the database.
     */
    public Savepoint setSavepoint(String name) throws SQLException;

    /**
     * Sets the transaction isolation level for this Connection.
     * <p>
     * If this method is called during a transaction, the results are
     * implementation defined.
     *
     * @param level
     *            the new transaction isolation level to use from the following
     *            list of possible values:
     *            <ul>
     *            <li>{@link #TRANSACTION_READ_COMMITTED}
     *            <li>{@link #TRANSACTION_READ_UNCOMMITTED}
     *            <li>{@link #TRANSACTION_REPEATABLE_READ}
     *            <li>{@link #TRANSACTION_SERIALIZABLE}
     *            </ul>
     * @throws SQLException
     *             if there is a problem with the database or if the value of
     *             {@code level} is not one of the expected constant values.
     */
    public void setTransactionIsolation(int level) throws SQLException;

    /**
     * Sets the {@code TypeMap} for this connection. The input {@code map}
     * should contain mappings between complex Java and SQL types.
     *
     * @param map
     *            the new type map.
     * @throws SQLException
     *             if there is a problem accessing the database or if {@code
     *             map} is not an instance of {@link Map}.
     */
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException;

    /**
     * Returns a new empty Clob.
     * @throws SQLException if this connection is closed, or there's a problem creating a new clob.
     */
    public Clob createClob() throws SQLException;

    /**
     * Returns a new empty Blob.
     * @throws SQLException if this connection is closed, or there's a problem creating a new blob.
     */
    public Blob createBlob() throws SQLException;

    /**
     * Returns a new empty NClob.
     * @throws SQLException if this connection is closed, or there's a problem creating a new nclob.
     */
    public NClob createNClob() throws SQLException;

    /**
     * Returns a new empty SQLXML.
     * @throws SQLException if this connection is closed, or there's a problem creating a new XML.
     */
    public SQLXML createSQLXML() throws SQLException;

    /**
     * Returns true if this connection is still open and valid, false otherwise.
     * @param timeout number of seconds to wait for a response before giving up and returning false,
     * 0 to wait forever
     * @throws SQLException if {@code timeout < 0}
     */
    public boolean isValid(int timeout) throws SQLException;

    /**
     * Sets the client info property {@code name} to {@code value}. A value of null clears the
     * client info property.
     * @throws SQLClientInfoException if this connection is closed, or there's a problem setting
     * the property.
     */
    public void setClientInfo(String name, String value) throws SQLClientInfoException;

    /**
     * Replaces all client info properties with the name/value pairs from {@code properties}.
     * All existing properties are removed. If an exception is thrown, the resulting state of
     * this connection's client info properties is undefined.
     * @throws SQLClientInfoException if this connection is closed, or there's a problem setting
     * a property.
     */
    public void setClientInfo(Properties properties) throws SQLClientInfoException;

    /**
     * Returns the value corresponding to the given client info property, or null if unset.
     * @throws SQLClientInfoException if this connection is closed, or there's a problem getting
     * the property.
     */
    public String getClientInfo(String name) throws SQLException;

    /**
     * Returns a {@link Properties} object containing all client info properties.
     * @throws SQLClientInfoException if this connection is closed, or there's a problem getting
     * a property.
     */
    public Properties getClientInfo() throws SQLException;

    /**
     * Returns a new {@link Array} containing the given {@code elements}.
     * @param typeName the SQL name of the type of the array elements
     * @throws SQLClientInfoException if this connection is closed, or there's a problem creating
     * the array.
     */
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException;

    /**
     * Returns a new {@link Struct} containing the given {@code attributes}.
     * @param typeName the SQL name of the type of the struct attributes
     * @throws SQLClientInfoException if this connection is closed, or there's a problem creating
     * the array.
     */
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException;
}
