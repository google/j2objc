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
 * An interface which provides comprehensive information about the database
 * management system and its supported features.
 * <p>
 * This interface is implemented by JDBC driver vendors in order to provide
 * information about the underlying database capabilities in association with
 * the JDBC driver.
 * <p>
 * Some of the methods in this interface take string parameters which are
 * patterns. Within these string patterns, {@code '%'} and {@code '_'}
 * characters have special meanings. {@code '%'} means
 * "match any substring of 0 or more characters". {@code '_'} means
 * "match any character". Only metadata entries that match the pattern are
 * returned. If such a search pattern string is set to {@code null}, that
 * argument's criteria are dropped from the search.
 */
public interface DatabaseMetaData extends Wrapper {

    /**
     * States that it may not be permitted to store {@code NULL} values.
     */
    public static final short attributeNoNulls = 0;

    /**
     * States that {@code NULL} values are definitely permitted.
     */
    public static final short attributeNullable = 1;

    /**
     * States that whether {@code NULL} values are permitted is unknown.
     */
    public static final short attributeNullableUnknown = 2;

    /**
     * States the best row identifier is <em>NOT</em> a pseudo column.
     */
    public static final int bestRowNotPseudo = 1;

    /**
     * States that the best row identifier is a pseudo column.
     */
    public static final int bestRowPseudo = 2;

    /**
     * States that the remainder of the current session is used as the scope for
     * the best row identifier.
     */
    public static final int bestRowSession = 2;

    /**
     * States that best row identifier scope lasts only while the row is being
     * used.
     */
    public static final int bestRowTemporary = 0;

    /**
     * States that the remainder of the current transaction is used as the scope
     * for the best row identifier.
     */
    public static final int bestRowTransaction = 1;

    /**
     * States that the best row identifier may or may not be a pseudo column.
     */
    public static final int bestRowUnknown = 0;

    /**
     * States that the column must not allow {@code NULL} values.
     */
    public static final int columnNoNulls = 0;

    /**
     * States that the column definitely allows {@code NULL} values.
     */
    public static final int columnNullable = 1;

    /**
     * States that it is unknown whether the columns may be nulled.
     */
    public static final int columnNullableUnknown = 2;

    /**
     * For the column {@code UPDATE_RULE}, states that when the primary key is
     * updated, the foreign key (imported key) is changed accordingly.
     */
    public static final int importedKeyCascade = 0;

    /**
     * States that the evaluation of foreign key constraints is deferred (delayed
     * until commit).
     */
    public static final int importedKeyInitiallyDeferred = 5;

    /**
     * States that the evaluation of foreign key constraint is {@code IMMEDIATE}
     * .
     */
    public static final int importedKeyInitiallyImmediate = 6;

    /**
     * For the columns {@code UPDATE_RULE} and {@code DELETE_RULE}, states that
     * if the primary key has been imported, it cannot be updated or deleted.
     */
    public static final int importedKeyNoAction = 3;

    /**
     * States that the evaluation of foreign key constraint must not be {@code
     * DEFERRED}.
     */
    public static final int importedKeyNotDeferrable = 7;

    /**
     * States that a primary key must not be updated when imported as a foreign
     * key by some other table. Used for the column {@code UPDATE_RULE}.
     */
    public static final int importedKeyRestrict = 1;

    /**
     * States that when the primary key is modified (updated or deleted) the
     * foreign (imported) key is changed to its default value. Applies to the
     * {@code UPDATE_RULE} and {@code DELETE_RULE} columns.
     */
    public static final int importedKeySetDefault = 4;

    /**
     * States that when the primary key is modified (updated or deleted) the
     * foreign (imported) key is changed to {@code NULL}. Applies to the {@code
     * UPDATE_RULE} and {@code DELETE_RULE} columns.
     */
    public static final int importedKeySetNull = 2;

    /**
     * States that the column stores {@code IN} type parameters.
     */
    public static final int procedureColumnIn = 1;

    /**
     * States that this column stores {@code INOUT} type parameters.
     */
    public static final int procedureColumnInOut = 2;

    /**
     * States that this column stores {@code OUT} type parameters.
     */
    public static final int procedureColumnOut = 4;

    /**
     * States that the column stores results.
     */
    public static final int procedureColumnResult = 3;

    /**
     * States that the column stores return values.
     */
    public static final int procedureColumnReturn = 5;

    /**
     * States that type of the column is unknown.
     */
    public static final int procedureColumnUnknown = 0;

    /**
     * States that {@code NULL} values are not permitted.
     */
    public static final int procedureNoNulls = 0;

    /**
     * States that the procedure does not return a result.
     */
    public static final int procedureNoResult = 1;

    /**
     * States that {@code NULL} values are permitted.
     */
    public static final int procedureNullable = 1;

    /**
     * States that it is unknown whether {@code NULL} values are permitted.
     */
    public static final int procedureNullableUnknown = 2;

    /**
     * States that it is unknown whether or not the procedure returns a result.
     */
    public static final int procedureResultUnknown = 0;

    /**
     * States that the procedure returns a result.
     */
    public static final int procedureReturnsResult = 2;

    /**
     * States that the value is an SQL99 {@code SQLSTATE} value.
     */
    public static final int sqlStateSQL99 = 2;

    /**
     * States that the value is an SQL {@code CLI SQLSTATE} value as defined by
     * the X/Open standard.
     */
    public static final int sqlStateXOpen = 1;

    /**
     * States that this table index is a clustered index.
     */
    public static final short tableIndexClustered = 1;

    /**
     * States that this table index is a hashed index.
     */
    public static final short tableIndexHashed = 2;

    /**
     * States this table's index is neither a clustered index, not a hashed
     * index, and not a table statistics index; i.e. it is something else.
     */
    public static final short tableIndexOther = 3;

    /**
     * States this column has the table's statistics, and that it is returned in
     * conjunction with the table's index description.
     */
    public static final short tableIndexStatistic = 0;

    /**
     * States that a {@code NULL} value is <em>NOT</em> permitted for
     * this data type.
     */
    public static final int typeNoNulls = 0;

    /**
     * States that a {@code NULL} value is permitted for this data type.
     */
    public static final int typeNullable = 1;

    /**
     * States that it is unknown if a {@code NULL} value is permitted for
     * this data type.
     */
    public static final int typeNullableUnknown = 2;

    /**
     * States that this column shall not be used for {@code WHERE} statements
     * with a {@code LIKE} clause.
     */
    public static final int typePredBasic = 2;

    /**
     * States that this column can only be used in a {@code WHERE...LIKE}
     * statement.
     */
    public static final int typePredChar = 1;

    /**
     * States that this column does not support searches.
     */
    public static final int typePredNone = 0;

    /**
     * States that the column is searchable.
     */
    public static final int typeSearchable = 3;

    /**
     * States that the version column is known to be not a pseudo column.
     */
    public static final int versionColumnNotPseudo = 1;

    /**
     * States that this version column is known to be a pseudo column.
     */
    public static final int versionColumnPseudo = 2;

    /**
     * States that the version column may be a pseudo column or not.
     */
    public static final int versionColumnUnknown = 0;

    /**
     * States that the method DatabaseMetaData.getSQLStateType may returns an
     * SQLSTATE value or not.
     */
    public static final int sqlStateSQL = 2;

    /**
     * States that the parameter or column is an IN parameter
     */
    public static final int functionColumnIn = 1;

    /**
     * States that the parameter or column is an INOUT parameter
     */
    public static final int functionColumnInOut = 2;

    /**
     * States that the parameter or column is an OUT parameter
     */
    public static final int functionColumnOut = 3;

    /**
     * States that the parameter or column is a return value
     */
    public static final int functionReturn = 4;

    /**
     * States that the parameter of function is unknown
     */
    public static final int functionColumnUnknown = 0;

    /**
     * States that the parameter or column is a column in a result set
     */
    public static final int functionColumnResult = 5;

    /**
     * States that NULL values are not allowed
     */
    public static final int functionNoNulls = 0;

    /**
     * States that NULL values are allowed
     */
    public static final int functionNullable = 1;

    /**
     * States that whether NULL values are allowed is unknown
     */
    public static final int functionNullableUnknown = 2;

    /**
     * States that it is not known whether the function returns a result or a
     * table
     */
    public static final int functionResultUnknown = 0;

    /**
     * States that the function does not return a table
     */
    public static final int functionNoTable = 1;

    /**
     * States that the function returns a table.
     */
    public static final int functionReturnsTable = 2;

    /**
     * Returns whether all procedures returned by {@link #getProcedures} can be
     * called by the current user.
     *
     * @return {@code true} if all procedures can be called by the current user,
     *         {@code false} otherwise.
     * @throws SQLException
     *             if there is a database error.
     */
    public boolean allProceduresAreCallable() throws SQLException;

    /**
     * Returns whether all the tables returned by {@code getTables} can be used
     * by the current user in a {@code SELECT} statement.
     *
     * @return {@code true} if all the tables can be used,{@code false}
     *         otherwise.
     * @throws SQLException
     *             if there is a database error.
     */
    public boolean allTablesAreSelectable() throws SQLException;

    /**
     * Returns whether a data definition statement in a transaction forces a {@code
     * commit} of the transaction.
     *
     * @return {@code true} if the statement forces a commit, {@code false}
     *         otherwise.
     * @throws SQLException
     *             if there is a database error.
     */
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException;

    /**
     * Returns whether the database ignores data definition statements within a
     * transaction.
     *
     * @return {@code true} if the database ignores a data definition statement,
     *         {@code false} otherwise.
     * @throws SQLException
     *             if there is a database error.
     */
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException;

    /**
     * Returns whether a visible row delete can be detected by calling
     * {@link ResultSet#rowDeleted}.
     *
     * @param type
     *            the type of the {@code ResultSet} involved: {@code
     *            ResultSet.TYPE_FORWARD_ONLY}, {@code
     *            ResultSet.TYPE_SCROLL_INSENSITIVE}, or {@code
     *            ResultSet.TYPE_SCROLL_SENSITIVE}
     * @return {@code true} if the visible row delete can be detected, {@code
     *         false} otherwise.
     * @throws SQLException
     *             if there is a database error.
     */
    public boolean deletesAreDetected(int type) throws SQLException;

    /**
     * Returns whether the return value of {@code getMaxRowSize} includes the
     * SQL data types {@code LONGVARCHAR} and {@code LONGVARBINARY}.
     *
     * @return {@code true} if the return value includes {@code LONGVARBINARY}
     *         and {@code LONGVARCHAR}, otherwise {@code false}.
     * @throws SQLException
     *             if there is a database error.
     */
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException;

    /**
     * Returns a {@code ResultSet} describing a subset of the attributes of a
     * specified SQL User Defined Type (UDT) for a specified schema and catalog.
     * The subset is determined by restricting to those attributes whose
     * name matches the {@code attributeNamePattern} and whose type name
     * matches the {@code typeNamePattern}. Each row of the {@code ResultSet}
     * describes one attribute, and the rows are ordered by the columns {@code TYPE_SCHEM},
     * {@code TYPE_NAME} and {@code ORDINAL_POSITION}. Inherited attributes
     * are not included.
     * <p>
     * The columns of the returned {@code ResultSet} object have the following
     * names and meanings:
     * <ol>
     * <li>{@code TYPE_CAT} - String - the type catalog name (possibly {@code
     * null})</li>
     * <li>{@code TYPE_SCHEM} - String - the type schema name (possibly {@code
     * null})</li>
     * <li>{@code TYPE_NAME} - String - the type name</li>
     * <li>{@code ATTR_NAME} - String - the attribute name</li>
     * <li>{@code DATA_TYPE} - int - the attribute type as defined in {@code
     * java.sql.Types}</li>
     * <li>{@code ATTR_TYPE_NAME} - String - the attribute type name. This
     * depends on the data source. For a {@code UDT} the name is fully
     * qualified. For a {@code REF} it is both fully qualified and represents
     * the target type of the reference.</li>
     * <li>{@code ATTR_SIZE} - int - the column size. When referring to char and
     * date types this value is the maximum number of characters. When referring
     * to numeric types is is the precision.</li>
     * <li>{@code DECIMAL_DIGITS} - int - how many fractional digits are
     * supported</li>
     * <li>{@code NUM_PREC_RADIX} - int - numeric values radix</li>
     * <li>{@code NULLABLE} - int - whether {@code NULL} is permitted:
     * <ul>
     * <li>DatabaseMetaData.attributeNoNulls - {@code NULL} values not permitted</li>
     * <li>DatabaseMetaData.attributeNullable - {@code NULL} values definitely
     * permitted</li>
     * <li>DatabaseMetaData.attributeNullableUnknown - unknown</li>
     * </ul>
     * </li>
     * <li>{@code REMARKS} - String - a comment describing the attribute
     * (possibly {@code null})</li>
     * <li>ATTR_DEF - String - Default value for the attribute (possibly {@code
     * null})</li>
     * <li>{@code SQL_DATA_TYPE} - int - not used</li>
     * <li>SQL_DATETIME_SUB - int - not used</li>
     * <li>CHAR_OCTET_LENGTH - int - for {@code CHAR} types, the max number of
     * bytes in the column</li>
     * <li>ORDINAL_POSITION - int - The index of the column in the table (where
     * the count starts from 1, not 0)</li>
     * <li>IS_NULLABLE - String - {@code "NO"} = the column does not allow {@code
     * NULL}s, {@code "YES"} = the column allows {@code NULL}s, "" = status unknown</li>
     * <li>{@code SCOPE_CATALOG} - String - if the {@code DATA_TYPE} is {@code REF},
     * this gives the catalog of the table corresponding to the attribute's scope.
     * NULL if the {@code DATA_TYPE} is not REF.</li>
     * <li>{@code SCOPE_SCHEMA} - String - if the {@code DATA_TYPE} is {@code REF},
     * this gives the schema of the table corresponding to the attribute's scope.
     * NULL if the {@code DATA_TYPE} is not REF.</li>
     * <li>{@code SCOPE_TABLE} - String - if the {@code DATA_TYPE} is {@code REF},
     * this gives the name of the table corresponding to the attribute's scope.
     * NULL if the {@code DATA_TYPE} is not REF.</li>
     * <li>{@code SOURCE_DATA_TYPE} - String - The source type for a user
     * generated REF type or for a Distinct type. ({@code NULL} if {@code
     * DATA_TYPE} is not DISTINCT or a user generated REF)</li>
     * </ol>
     *
     * @param catalog
     *            a catalog name. {@code null} is used to imply no narrowing of
     *            the search by catalog name. Otherwise, the name must match a
     *            catalog name held in the database, with "" used to retrieve
     *            those without a catalog name.
     * @param schemaPattern
     *            a schema name pattern. {@code null} is used to imply no
     *            narrowing of the search by a schema name. Otherwise, the name
     *            must match a schema name in the database, with "" used to
     *            retrieve those without a schema name.
     * @param typeNamePattern
     *            a type name. This pattern must match the type name stored in
     *            the database.
     * @param attributeNamePattern
     *            an Attribute name. This pattern must match the attribute name as stored in
     *            the database.
     * @return a {@code ResultSet}, where each row is an attribute description.
     * @throws SQLException
     *             if there is a database error.
     */
    public ResultSet getAttributes(String catalog, String schemaPattern,
            String typeNamePattern, String attributeNamePattern)
            throws SQLException;

    /**
     * Returns a list of a table's optimal set of columns that uniquely
     * identify the rows. The results are ordered by {@code SCOPE} (see below).
     * <p>
     * The results are returned as a table, with one entry for each column, as
     * follows:
     * <ol>
     * <li>{@code SCOPE} - short - the {@code SCOPE} of the result, as follows:
     * <ul>
     * <li>{@code DatabaseMetaData.bestRowTemporary} - the result is very temporary,
     * only valid while on the current row</li>
     * <li>{@code DatabaseMetaData.bestRowTransaction} - the result is good for remainder of
     * current transaction</li>
     * <li>{@code DatabaseMetaData.bestRowSession} - the result is good for remainder of
     * database session</li>
     * </ul>
     * </li>
     * <li>{@code COLUMN_NAME} - String - the column name</li>
     * <li>{@code DATA_TYPE} - int - the Type of the data, as defined in {@code
     * java.sql.Types}</li>
     * <li>{@code TYPE_NAME} - String - the Name of the type - database dependent.
     * For UDT types the name is fully qualified</li>
     * <li>{@code COLUMN_SIZE} - int - the precision of the data in the column</li>
     * <li>{@code BUFFER_LENGTH} - int - not used</li>
     * <li>{@code DECIMAL_DIGITS} - short - number of fractional digits</li>
     * <li>{@code PSEUDO_COLUMN} - short - whether this is a pseudo column (e.g.
     * an Oracle {@code ROWID}):
     * <ul>
     * <li>{@code DatabaseMetaData.bestRowUnknown} - it is not known whether this is
     * a pseudo column</li>
     * <li>{@code DatabaseMetaData.bestRowNotPseudo} - the column is not pseudo</li>
     * <li>{@code DatabaseMetaData.bestRowPseudo} - the column is a pseudo column</li>
     * </ul>
     * </li>
     * </ol>
     *
     * @param catalog
     *            a catalog name. {@code null} is used to imply no narrowing of
     *            the search by catalog name. Otherwise, the name must match a
     *            catalog name held in the database, with "" used to retrieve
     *            those without a catalog name.
     * @param schema
     *            a schema name pattern. {@code null} is used to imply no
     *            narrowing of the search by schema name. Otherwise, the name
     *            must match a schema name in the database, with "" used to
     *            retrieve those without a schema name.
     * @param table
     *            the table name. This must match the name of the table as
     *            declared in the database.
     * @param scope
     *            the {@code SCOPE} of interest, values as defined above.
     * @param nullable
     *            {@code true} = include columns that are nullable, {@code
     *            false} = do not include nullable columns.
     * @return a {@code ResultSet} where each row is a description of a column
     *         and the complete set of rows is the optimal set for this table.
     * @throws SQLException
     *             if there is a database error.
     */
    public ResultSet getBestRowIdentifier(String catalog, String schema,
            String table, int scope, boolean nullable) throws SQLException;

    /**
     * Returns the set of catalog names available in this database. The set is
     * returned ordered by catalog name.
     *
     * @return a {@code ResultSet} containing the catalog names, with each row
     *         containing one catalog name (as a {@code String}) in the
     *         single column named {@code TABLE_CAT}.
     * @throws SQLException
     *             if there is a database error.
     */
    public ResultSet getCatalogs() throws SQLException;

    /**
     * Returns the separator that this database uses between a catalog name and
     * table name.
     *
     * @return a String containing the separator.
     * @throws SQLException
     *             if there is a database error.
     */
    public String getCatalogSeparator() throws SQLException;

    /**
     * Returns the term that the database vendor prefers term for "catalog".
     *
     * @return a String with the vendor's term for "catalog".
     * @throws SQLException
     *             if there is a database error.
     */
    public String getCatalogTerm() throws SQLException;

    /**
     * Returns a description of access rights for a table's columns. Only access
     * rights matching the criteria for the column name are returned.
     * <p>
     * The description is returned as a {@code ResultSet} with rows of data for
     * each access right, with columns as follows:
     * <ol>
     * <li>{@code TABLE_CAT} - String - the catalog name (possibly {@code null})</li>
     * <li>{@code TABLE_SCHEM} - String - the schema name (possibly {@code null})</li>
     * <li>{@code TABLE_NAME} - String - the table name</li>
     * <li>{@code COLUMN_NAME} - String - the Column name</li>
     * <li>{@code GRANTOR} - String - the grantor of access (possibly {@code
     * null})</li>
     * <li>{@code PRIVILEGE} - String - Access right - one of SELECT, INSERT,
     * UPDATE, REFERENCES,...</li>
     * <li>{@code IS_GRANTABLE} - String - {@code "YES"} implies that the
     * receiver can grant access to others, {@code "NO"} if the receiver cannot
     * grant access to others, {@code null} if unknown.</li>
     * </ol>
     *
     * @param catalog
     *            a catalog name. {@code null} is used to imply no narrowing of
     *            the search by catalog name. Otherwise, the name must match a
     *            catalog name held in the database, with "" used to retrieve
     *            those without a catalog name.
     * @param schema
     *            a schema name pattern. {@code null} is used to imply no
     *            narrowing of the search by schema name. Otherwise, the name
     *            must match a schema name in the database, with "" used to
     *            retrieve those without a schema name.
     * @param table
     *            the table name. This must match the name of the table as
     *            declared in the database.
     * @param columnNamePattern
     *            the column name. This must match the name of a column in the
     *            table in the database.
     * @return a {@code ResultSet} containing the access rights, one row for
     *         each privilege description.
     * @throws SQLException
     *             if there is a database error.
     */
    public ResultSet getColumnPrivileges(String catalog, String schema,
            String table, String columnNamePattern) throws SQLException;

    /**
     * Returns a description of table columns available in a specified catalog.
     * Only descriptions meeting the specified catalog, schema, table, and column
     * names are returned.
     * <p>
     * The descriptions are returned as a {@code ResultSet} conforming to the
     * following data layout, with one row per table column:
     * <ol>
     * <li>{@code TABLE_CAT} - String - the catalog name (possibly {@code null})</li>
     * <li>{@code TABLE_SCHEM} - String - the schema name (possibly {@code null})</li>
     * <li>{@code TABLE_NAME} - String - the table name</li>
     * <li>{@code COLUMN_NAME} - String - the column name</li>
     * <li>{@code DATA_TYPE} - int - the SQL type as specified in {@code
     * java.sql.Types}</li>
     * <li>{@code TYPE_NAME} - String - the name of the data type, (database-dependent,
     * UDT names are fully qualified)</li>
     * <li>{@code COLUMN_SIZE} - int - the column size (the precision for numeric
     * types, max characters for {@code char} and {@code date} types)</li>
     * <li>{@code BUFFER_LENGTH} - int - Not used</li>
     * <li>{@code DECIMAL_DIGITS} - int - maximum number of fractional digits</li>
     * <li>{@code NUM_PREC_RADIX} - int - the radix for numerical types</li>
     * <li>{@code NULLABLE} - int - whether the column allows {@code null}s:
     * <ul>
     * <li>DatabaseMetaData.columnNoNulls = may not allow {@code NULL}s</li>
     * <li>DatabaseMetaData.columnNullable = does allow {@code NULL}s</li>
     * <li>DatabaseMetaData.columnNullableUnknown = unknown {@code NULL} status</li>
     * </ul>
     * </li>
     * <li>{@code REMARKS} - String - A description of the column (possibly
     * {@code null})</li>
     * <li>{@code COLUMN_DEF} - String - Default value for the column (possibly
     * {@code null})</li>
     * <li>{@code SQL_DATA_TYPE} - int - not used</li>
     * <li>{@code SQL_DATETIME_SUB} - int - not used</li>
     * <li>{@code CHAR_OCTET_LENGTH} - int - maximum number of bytes in the
     * {@code char} type columns</li>
     * <li>{@code ORDINAL_POSITION} - int - the column index in the table (1 based)</li>
     * <li>{@code IS_NULLABLE} - String - {@code "NO"} = column does not allow
     * NULLs, {@code "YES"} = column allows NULLs, "" = {@code NULL} status
     * unknown</li>
     * <li>{@code SCOPE_CATALOG} - String - if the {@code DATA_TYPE} is {@code REF},
     * this gives the catalog of the table corresponding to the attribute's scope.
     * NULL if the {@code DATA_TYPE} is not REF.</li>
     * <li>{@code SCOPE_SCHEMA} - String - if the {@code DATA_TYPE} is {@code REF},
     * this gives the schema of the table corresponding to the attribute's scope.
     * NULL if the {@code DATA_TYPE} is not REF.</li>
     * <li>{@code SCOPE_TABLE} - String - if the {@code DATA_TYPE} is {@code REF},
     * this gives the name of the table corresponding to the attribute's scope.
     * NULL if the {@code DATA_TYPE} is not REF.</li>
     * <li>{@code SOURCE_DATA_TYPE} - String - The source type for a user
     * generated REF type or for a Distinct type. ({@code NULL} if {@code
     * DATA_TYPE} is not DISTINCT or a user generated REF)</li>
     * </ol>
     *
     * @param catalog
     *            a catalog name. {@code null} is used to imply no narrowing of
     *            the search by catalog name. Otherwise, the name must match a
     *            catalog name held in the database, with "" used to retrieve
     *            those without a catalog name.
     * @param schemaPattern
     *            a schema name pattern. {@code null} is used to imply no
     *            narrowing of the search by schema name. Otherwise, the name
     *            must match a schema name in the database, with "" used to
     *            retrieve those without a schema name.
     * @param tableNamePattern
     *            the table name. This must match the name of the table as
     *            declared in the database.
     * @param columnNamePattern
     *            the column name. This must match the name of a column in the
     *            table in the database.
     * @return the descriptions as a {@code ResultSet} with rows in the form
     *         defined above.
     * @throws SQLException
     *             if there is a database error.
     */
    public ResultSet getColumns(String catalog, String schemaPattern,
            String tableNamePattern, String columnNamePattern)
            throws SQLException;

    /**
     * Returns the database connection that created this metadata.
     *
     * @return the connection to the database.
     * @throws SQLException
     *             if there is a database error.
     */
    public Connection getConnection() throws SQLException;

    /**
     * Returns a list of foreign key columns in a given foreign key table that
     * reference the primary key columns of a supplied primary key table. This
     * describes how one table imports the key of another table. It would be
     * expected to return a single foreign key - primary key pair in most cases.
     * <p>
     * The descriptions are returned as a {@code ResultSet} with one row for
     * each foreign key, with the following layout:
     * <ol>
     * <li>{@code PKTABLE_CAT} - String - from the primary key table : Catalog
     * (possibly {@code null})</li>
     * <li>{@code PKTABLE_SCHEM} - String - from the primary key table : Schema
     * (possibly {@code null})</li>
     * <li>{@code PKTABLE_NAME} - String - from the primary key table : name</li>
     * <li>{@code PKCOLUMN_NAME} - String - from the primary key column : name</li>
     * <li>{@code FKTABLE_CAT} - String - from the foreign key table : the
     * catalog name being exported (possibly {@code null})</li>
     * <li>{@code FKTABLE_SCHEM} - String - from the foreign key table : the schema name
     * being exported (possibly {@code null})</li>
     * <li>{@code FKTABLE_NAME} - String - from the foreign key table : the name being
     * exported</li>
     * <li>{@code FKCOLUMN_NAME} - String - from the foreign key column : the name being
     * exported</li>
     * <li>{@code KEY_SEQ} - short - the sequence number (in the foreign key)</li>
     * <li>{@code UPDATE_RULE} - short - a value giving the rule for how to treat the corresponding foreign key when a primary
     * key is updated:
     * <ul>
     * <li>{@code DatabaseMetaData.importedKeyNoAction} - don't allow the
     * primary key to be updated if it is imported as a foreign key</li>
     * <li>{@code DatabaseMetaData.importedKeyCascade} - change the imported key to
     * match the updated primary key</li>
     * <li>{@code DatabaseMetaData.importedKeySetNull} - set the imported key to
     * {@code null}</li>
     * <li>{@code DatabaseMetaData.importedKeySetDefault} - set the imported key
     * to its default value</li>
     * <li>{@code DatabaseMetaData.importedKeyRestrict} - same as {@code
     * importedKeyNoAction}</li>
     * </ul>
     * </li>
     * <li>{@code DELETE_RULE} - short - a value giving the rule for how to treat the foreign key when the corresponding primary
     * key is deleted:
     * <ul>
     * <li>{@code DatabaseMetaData.importedKeyNoAction} - don't allow the
     * primary key to be deleted if it is imported as a foreign key</li>
     * <li>{@code DatabaseMetaData.importedKeyCascade} - delete those rows that
     * import a deleted key</li>
     * <li>{@code DatabaseMetaData.importedKeySetNull} - set the imported key to
     * {@code null}</li>
     * <li>{@code DatabaseMetaData.importedKeySetDefault} - set the imported key
     * to its default value</li>
     * <li>{@code DatabaseMetaData.importedKeyRestrict} - same as
     * importedKeyNoAction</li>
     * </ul>
     * </li>
     * <li>{@code FK_NAME} - String - the foreign key name (possibly {@code null})</li>
     * <li>{@code PK_NAME} - String - the primary key name (possibly {@code null})</li>
     * <li>{@code DEFERRABILITY} - short - whether foreign key constraints can be
     * deferred until commit (see the SQL92 specification for definitions):
     * <ul>
     * <li>{@code DatabaseMetaData.importedKeyInitiallyDeferred}</li>
     * <li>{@code DatabaseMetaData.importedKeyInitiallyImmediate}</li>
     * <li>{@code DatabaseMetaData.importedKeyNotDeferrable}</li>
     * </ul>
     * </li>
     * </ol>
     *
     * @param primaryCatalog
     *            a catalog name for the primary key table. {@code null} is used to imply no narrowing of
     *            the search by catalog name. Otherwise, the name must match a
     *            catalog name held in the database, with "" used to retrieve
     *            those without a catalog name.
     * @param primarySchema
     *            a schema name for the primary key table. {@code null} is used to imply no narrowing of
     *            the search by schema name. Otherwise, the name must match a
     *            schema name in the database, with "" used to retrieve those
     *            without a schema name.
     * @param primaryTable
     *            the name of the table which exports the key. It must match the
     *            name of the table in the database.
     * @param foreignCatalog
     *            a catalog name for the foreign key table. {@code null} is used to imply no narrowing of
     *            the search by catalog name. Otherwise, the name must match a
     *            catalog name held in the database, with "" used to retrieve
     *            those without a catalog name.
     * @param foreignSchema
     *            a schema name for the foreign key table. {@code null} is used to imply no narrowing of
     *            the search by schema name. Otherwise, the name must match a
     *            schema name in the database, with "" used to retrieve those
     *            without a schema name.
     * @param foreignTable
     *            the name of the table importing the key. It must match the
     *            name of the table in the database.
     * @return a {@code ResultSet} containing rows with the descriptions of the
     *         foreign keys laid out according to the format defined above.
     * @throws SQLException
     *             if there is a database error.
     */
    public ResultSet getCrossReference(String primaryCatalog,
            String primarySchema, String primaryTable, String foreignCatalog,
            String foreignSchema, String foreignTable) throws SQLException;

    /**
     * Returns the major version number of the database software.
     *
     * @return the major version number of the database software.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getDatabaseMajorVersion() throws SQLException;

    /**
     * Returns the minor version number of the database software.
     *
     * @return the minor version number of the database software.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getDatabaseMinorVersion() throws SQLException;

    /**
     * Returns the name of the database software.
     *
     * @return a {@code String} with the name of the database software.
     * @throws SQLException
     *             a database error occurred.
     */
    public String getDatabaseProductName() throws SQLException;

    /**
     * Returns the version number of this database software.
     *
     * @return a {@code String} with the version number of the database
     *         software.
     * @throws SQLException
     *             a database error occurred.
     */
    public String getDatabaseProductVersion() throws SQLException;

    /**
     * Returns the default transaction isolation level for this database.
     *
     * @return the default transaction isolation level. One of the following values:
     *         <ul>
     *         <li>{@code TRANSACTION_NONE}</li>
     *         <li>{@code TRANSACTION_READ_COMMITTED}</li>
     *         <li>{@code TRANSACTION_READ_UNCOMMITTED}</li>
     *         <li>{@code TRANSACTION_REPEATABLE_READ}</li>
     *         <li>{@code TRANSACTION_SERIALIZABLE}</li>
     *         </ul>
     * @throws SQLException
     *             a database error occurred.
     */
    public int getDefaultTransactionIsolation() throws SQLException;

    /**
     * Returns the JDBC driver's major version number.
     *
     * @return the driver's major version number.
     */
    public int getDriverMajorVersion();

    /**
     * Returns the JDBC driver's minor version number.
     *
     * @return the driver's minor version number.
     */
    public int getDriverMinorVersion();

    /**
     * Returns the name of this JDBC driver.
     *
     * @return a {@code String} containing the name of the JDBC driver
     * @throws SQLException
     *             a database error occurred.
     */
    public String getDriverName() throws SQLException;

    /**
     * Returns the version number of this JDBC driver.
     *
     * @return a {@code String} containing the complete version number of the
     *         JDBC driver.
     * @throws SQLException
     *             a database error occurred.
     */
    public String getDriverVersion() throws SQLException;

    /**
     * Returns a list of the foreign key columns that reference the primary key
     * columns of a specified table (the foreign keys exported by a table).
     * <p>
     * The list is returned as a {@code ResultSet} with a row for each of the
     * foreign key columns, ordered by {@code FKTABLE_CAT}, {@code
     * FKTABLE_SCHEM}, {@code FKTABLE_NAME}, and {@code KEY_SEQ}, with the
     * format for each row being:
     * <ol>
     * <li>{@code PKTABLE_CAT} - String - from the primary key table : the catalog (possibly
     * {@code null})</li>
     * <li>{@code PKTABLE_SCHEM} - String - from the primary key table : the schema (possibly
     * {@code null})</li>
     * <li>{@code PKTABLE_NAME} - String - from the primary key table : the name</li>
     * <li>{@code PKCOLUMN_NAME} - String - from the primary key column : the name</li>
     * <li>{@code FKTABLE_CAT} - String - from the foreign key table : the catalog name being
     * exported (possibly {@code null})</li>
     * <li>{@code FKTABLE_SCHEM} - String - from the foreign key table : the schema name
     * being exported (possibly {@code null})</li>
     * <li>{@code FKTABLE_NAME} - String - from the foreign key table : the name being
     * exported</li>
     * <li>{@code FKCOLUMN_NAME} - String - from the foreign key column : the name being
     * exported</li>
     * <li>{@code KEY_SEQ} - short - the sequence number (in the foreign key)</li>
     * <li>{@code UPDATE_RULE} - short - a value giving the rule for how to treat the foreign key when the corresponding primary
     * key is updated:
     * <ul>
     * <li>{@code DatabaseMetaData.importedKeyNoAction} - don't allow the
     * primary key to be updated if it is imported as a foreign key</li>
     * <li>{@code DatabaseMetaData.importedKeyCascade} - change the imported key to
     * match the primary key update</li>
     * <li>{@code DatabaseMetaData.importedKeySetNull} - set the imported key to
     * {@code null}</li>
     * <li>{@code DatabaseMetaData.importedKeySetDefault} - set the imported key
     * to its default value</li>
     * <li>{@code DatabaseMetaData.importedKeyRestrict} - same as
     * importedKeyNoAction</li>
     * </ul>
     * </li>
     * <li>{@code DELETE_RULE} - short - how to treat the foreign key when the corresponding primary
     * key is deleted:
     * <ul>
     * <li>{@code DatabaseMetaData.importedKeyNoAction} - don't allow the
     * primary key to be deleted if it is imported as a foreign key</li>
     * <li>{@code DatabaseMetaData.importedKeyCascade} - the deletion should
     * also delete rows that import a deleted key</li>
     * <li>{@code DatabaseMetaData.importedKeySetNull} - the deletion sets the
     * imported key to {@code null}</li>
     * <li>{@code DatabaseMetaData.importedKeySetDefault} - the deletion sets the
     * imported key to its default value</li>
     * <li>{@code DatabaseMetaData.importedKeyRestrict} - same as
     * importedKeyNoAction</li>
     * </ul>
     * </li>
     * <li>{@code FK_NAME} - String - the foreign key name (possibly {@code null})</li>
     * <li>{@code PK_NAME} - String - the primary key name (possibly {@code null})</li>
     * <li>{@code DEFERRABILITY} - short - defines whether the foreign key
     * constraints can be deferred until commit (see the SQL92 specification for
     * definitions):
     * <ul>
     * <li>{@code DatabaseMetaData.importedKeyInitiallyDeferred}</li>
     * <li>{@code DatabaseMetaData.importedKeyInitiallyImmediate}</li>
     * <li>{@code DatabaseMetaData.importedKeyNotDeferrable}</li>
     * </ul>
     * </li>
     * </ol>
     *
     * @param catalog
     *            a catalog name. {@code null} is used to imply no narrowing of
     *            the search by catalog name. Otherwise, the name must match a
     *            catalog name held in the database, with "" used to retrieve
     *            those without a catalog name.
     * @param schema
     *            a schema name. {@code null} is used to imply no narrowing of
     *            the search by schema name. Otherwise, the name must match a
     *            schema name in the database, with "" used to retrieve those
     *            without a schema name.
     * @param table
     *            a table name, which must match the name of a table in the
     *            database
     * @return a {@code ResultSet} containing a row for each of the foreign key
     *         columns, as defined above
     * @throws SQLException
     *             a database error occurred
     */
    public ResultSet getExportedKeys(String catalog, String schema, String table)
            throws SQLException;

    /**
     * Returns a string of characters that may be used in unquoted identifier
     * names. The characters {@code a-z}, {@code A-Z}, {@code 0-9} and {@code _}
     * are always permitted.
     *
     * @return a String containing all the additional permitted characters.
     * @throws SQLException
     *             a database error occurred.
     */
    public String getExtraNameCharacters() throws SQLException;

    /**
     * Returns the string used to quote SQL identifiers. Returns " " (space) if
     * identifier quoting not supported.
     *
     * @return the String used to quote SQL identifiers.
     * @throws SQLException
     *             a database error occurred.
     */
    public String getIdentifierQuoteString() throws SQLException;

    /**
     * Returns a list columns in a table that are both primary keys and
     * referenced by the table's foreign key columns (that is, the primary keys
     * imported by a table).
     * <p>
     * The list returned is a {@code ResultSet} with a row entry for each
     * primary key column, ordered by {@code PKTABLE_CAT}, {@code PKTABLE_SCHEM},
     * {@code PKTABLE_NAME}, and {@code KEY_SEQ}, with the following format:
     * <ol>
     * <li>{@code PKTABLE_CAT} - String - primary key catalog name being
     * imported (possibly {@code null})</li>
     * <li>{@code PKTABLE_SCHEM} - String - primary key schema name being
     * imported (possibly {@code null})</li>
     * <li>{@code PKTABLE_NAME} - String - primary key table name being imported
     * </li>
     * <li>{@code PKCOLUMN_NAME} - String - primary key column name being
     * imported</li>
     * <li>{@code FKTABLE_CAT} - String - foreign key table catalog name
     * (possibly {@code null})</li>
     * <li>{@code FKTABLE_SCHEM} - String - foreign key table schema name
     * (possibly {@code null})</li>
     * <li>{@code FKTABLE_NAME} - String - foreign key table name</li>
     * <li>{@code FKCOLUMN_NAME} - String - foreign key column name</li>
     * <li>{@code KEY_SEQ} - short - sequence number (in the foreign key)</li>
     * <li>{@code UPDATE_RULE} - short - how to treat the foreign key when the corresponding primary
     * key is updated:
     * <ul>
     * <li>{@code DatabaseMetaData.importedKeyNoAction} - don't allow any update of
     * the primary key if it is imported as a foreign key</li>
     * <li>{@code DatabaseMetaData.importedKeyCascade} - change imported key to
     * match the primary key update</li>
     * <li>{@code DatabaseMetaData.importedKeySetNull} - set the imported key to
     * {@code null}</li>
     * <li>{@code DatabaseMetaData.importedKeySetDefault} - set the imported key
     * to its default value</li>
     * <li>{@code DatabaseMetaData.importedKeyRestrict} - same as
     * importedKeyNoAction</li>
     * </ul>
     * </li>
     * <li>{@code DELETE_RULE} - short - how to treat the foreign key when the corresponding primary
     * key is deleted:
     * <ul>
     * <li>{@code DatabaseMetaData.importedKeyNoAction} - don't allow the primary key to be deleted
     * if it is imported as a foreign key</li>
     * <li>{@code DatabaseMetaData.importedKeyCascade} - delete those rows that
     * import a deleted key</li>
     * <li>{@code DatabaseMetaData.importedKeySetNull} - set the imported key to
     * {@code null}</li>
     * <li>{@code DatabaseMetaData.importedKeySetDefault} - set the imported key
     * to its default value</li>
     * <li>{@code DatabaseMetaData.importedKeyRestrict} - same as {@code
     * importedKeyNoAction}</li>
     * </ul>
     * </li>
     * <li>{@code FK_NAME} - String - foreign key name (possibly {@code null})</li>
     * <li>{@code PK_NAME} - String - primary key name (possibly {@code null})</li>
     * <li>{@code DEFERRABILITY} - short - defines whether foreign key
     * constraints can be deferred until commit (see SQL92 specification for
     * definitions):
     * <ul>
     * <li>{@code DatabaseMetaData.importedKeyInitiallyDeferred}</li>
     * <li>{@code DatabaseMetaData.importedKeyInitiallyImmediate}</li>
     * <li>{@code DatabaseMetaData.importedKeyNotDeferrable}</li>
     * </ul>
     * </li>
     * </ol>
     *
     * @param catalog
     *            a catalog name. {@code null} is used to imply no narrowing of
     *            the search by catalog name. Otherwise, the name must match a
     *            catalog name held in the database, with "" used to retrieve
     *            those without a catalog name.
     * @param schema
     *            a schema name. {@code null} is used to imply no narrowing of
     *            the search by schema name. Otherwise, the name must match a
     *            schema name in the database, with "" used to retrieve those
     *            without a schema name.
     * @param table
     *            a table name, which must match the name of a table in the
     *            database.
     * @return a {@code ResultSet} containing the list of primary key columns as
     *         rows in the format defined above.
     * @throws SQLException
     *             a database error occurred.
     */
    public ResultSet getImportedKeys(String catalog, String schema, String table)
            throws SQLException;

    /**
     * Returns a list of indices and statistics for a specified table.
     * <p>
     * The list is returned as a {@code ResultSet}, with one row for each index
     * or statistic. The list is ordered by {@code NON_UNIQUE}, {@code TYPE},
     * {@code INDEX_NAME}, and {@code ORDINAL_POSITION}. Each row has the
     * following format:
     * <ol>
     * <li>{@code TABLE_CAT} - String - table catalog name (possibly {@code
     * null})</li>
     * <li>{@code TABLE_SCHEM} - String - table schema name (possibly {@code
     * null})</li>
     * <li>{@code TABLE_NAME} - String - The table name</li>
     * <li>{@code NON_UNIQUE} - boolean - {@code true} when index values can be
     * non-unique. Must be {@code false} when the TYPE is tableIndexStatistic</li>
     * <li>{@code INDEX_QUALIFIER} - String : index catalog name. {@code null}
     * when the TYPE is 'tableIndexStatistic'</li>
     * <li>{@code INDEX_NAME} - String : index name. {@code null} when TYPE is
     * 'tableIndexStatistic'</li>
     * <li>{@code TYPE} - short - the index type. One of:
     * <ul>
     * <li>{@code DatabaseMetaData.tableIndexStatistic} - table statistics
     * returned with Index descriptions</li>
     * <li>{@code DatabaseMetaData.tableIndexClustered} - a clustered Index</li>
     * <li>{@code DatabaseMetaData.tableIndexHashed} - a hashed Index</li>
     * <li>{@code DatabaseMetaData.tableIndexOther} - other style of Index</li>
     * </ul>
     * </li>
     * <li>{@code ORDINAL_POSITION} - short - column sequence within Index. 0
     * when TYPE is tableIndexStatistic</li>
     * <li>{@code COLUMN_NAME} - String - the column name. {@code null} when
     * TYPE is tableIndexStatistic</li>
     * <li>{@code ASC_OR_DESC} - String - column sort sequence. {@code null} if
     * sequencing not supported or TYPE is tableIndexStatistic; otherwise "A"
     * means sort ascending and "D" means sort descending.</li>
     * <li>{@code CARDINALITY} - int - Number of unique values in the Index. If
     * TYPE is tableIndexStatistic, this is number of rows in the table.</li>
     * <li>{@code PAGES} - int - Number of pages for current Index. If TYPE is
     * tableIndexStatistic, this is number of pages used for the table.</li>
     * <li>{@code FILTER_CONDITION} - String - Filter condition. (possibly null)
     * </li>
     * </ol>
     *
     * @param catalog
     *            a catalog name. {@code null} is used to imply no narrowing of
     *            the search by catalog name. Otherwise, the name must match a
     *            catalog name held in the database, with "" used to retrieve
     *            those without a catalog name.
     * @param schema
     *            a schema name. {@code null} is used to imply no narrowing of
     *            the search by schema name. Otherwise, the name must match a
     *            schema name in the database, with "" used to retrieve those
     *            without a schema name.
     * @param table
     *            a table name, which must match the name of a table in the
     *            database.
     * @param unique
     *            {@code true} means only return indices for unique values,
     *            {@code false} implies that they can be returned even if not
     *            unique.
     * @param approximate
     *            {@code true} implies that the list can contain approximate or
     *            "out of data" values, {@code false} implies that all values
     *            must be precisely accurate
     * @return a {@code ResultSet} containing the list of indices and statistics
     *         for the table, in the format defined above.
     * @throws SQLException
     *             a database error occurred.
     */
    public ResultSet getIndexInfo(String catalog, String schema, String table,
            boolean unique, boolean approximate) throws SQLException;

    /**
     * Returns this driver's major JDBC version number.
     *
     * @return the major JDBC version number.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getJDBCMajorVersion() throws SQLException;

    /**
     * Returns the minor JDBC version number for this driver.
     *
     * @return the Minor JDBC Version Number.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getJDBCMinorVersion() throws SQLException;

    /**
     * Get the maximum number of hex characters in an in-line binary literal for
     * this database.
     *
     * @return the maximum number of hex characters in an in-line binary
     *         literal. If the number is unlimited then the result is zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxBinaryLiteralLength() throws SQLException;

    /**
     * Returns the maximum size of a catalog name in this database.
     *
     * @return the maximum size in characters for a catalog name. If the limit
     *         is unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxCatalogNameLength() throws SQLException;

    /**
     * Returns the maximum size for a character literal in this database.
     *
     * @return the maximum size in characters for a character literal. If the
     *         limit is unknown, or the value is unlimited, then the result is
     *         zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxCharLiteralLength() throws SQLException;

    /**
     * Returns the maximum size for a Column name for this database.
     *
     * @return the maximum number of characters for a Column name. If the limit
     *         is unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxColumnNameLength() throws SQLException;

    /**
     * Get the maximum number of columns in a {@code GROUP BY} clause for this
     * database.
     *
     * @return the maximum number of columns in a {@code GROUP BY} clause. If
     *         the limit is unknown, or the value is unlimited, then the result
     *         is zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxColumnsInGroupBy() throws SQLException;

    /**
     * Returns the maximum number of columns in an Index for this database.
     *
     * @return the maximum number of columns in an Index. If the limit is
     *         unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxColumnsInIndex() throws SQLException;

    /**
     * Returns the maximum number of columns in an {@code ORDER BY} clause for
     * this database.
     *
     * @return the maximum number of columns in an {@code ORDER BY} clause. If
     *         the limit is unknown, or the value is unlimited, then the result
     *         is zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxColumnsInOrderBy() throws SQLException;

    /**
     * Returns the maximum number of columns in a {@code SELECT} list for this
     * database.
     *
     * @return the maximum number of columns in a {@code SELECT} list. If the
     *         limit is unknown, or the value is unlimited, then the result is
     *         zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxColumnsInSelect() throws SQLException;

    /**
     * Returns the maximum number of columns in a table for this database.
     *
     * @return the maximum number of columns in a table. If the limit is
     *         unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxColumnsInTable() throws SQLException;

    /**
     * Returns the database's maximum number of concurrent connections.
     *
     * @return the maximum number of connections. If the limit is unknown, or
     *         the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxConnections() throws SQLException;

    /**
     * Returns the maximum length of a cursor name for this database.
     *
     * @return the maximum number of characters in a cursor name. If the limit
     *         is unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxCursorNameLength() throws SQLException;

    /**
     * Returns the maximum length in bytes for an Index for this database. This
     * covers all the parts of a composite index.
     *
     * @return the maximum length in bytes for an Index. If the limit is
     *         unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxIndexLength() throws SQLException;

    /**
     * Returns the maximum number of characters for a procedure name in this
     * database.
     *
     * @return the maximum number of character for a procedure name. If the
     *         limit is unknown, or the value is unlimited, then the result is
     *         zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxProcedureNameLength() throws SQLException;

    /**
     * Returns the maximum number of bytes within a single row for this
     * database.
     *
     * @return the maximum number of bytes for a single row. If the limit is
     *         unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxRowSize() throws SQLException;

    /**
     * Returns the maximum number of characters in a schema name for this
     * database.
     *
     * @return the maximum number of characters in a schema name. If the limit
     *         is unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxSchemaNameLength() throws SQLException;

    /**
     * Returns the maximum number of characters in an SQL statement for this
     * database.
     *
     * @return the maximum number of characters in an SQL statement. If the
     *         limit is unknown, or the value is unlimited, then the result is
     *         zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxStatementLength() throws SQLException;

    /**
     * Get the maximum number of simultaneously open active statements for this
     * database.
     *
     * @return the maximum number of open active statements. If the limit is
     *         unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxStatements() throws SQLException;

    /**
     * Returns the maximum size for a table name in the database.
     *
     * @return the maximum size in characters for a table name. If the limit is
     *         unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxTableNameLength() throws SQLException;

    /**
     * Returns the maximum number of tables permitted in a {@code SELECT}
     * statement for the database.
     *
     * @return the maximum number of tables permitted in a {@code SELECT}
     *         statement. If the limit is unknown, or the value is unlimited,
     *         then the result is zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxTablesInSelect() throws SQLException;

    /**
     * Returns the maximum number of characters in a user name for the database.
     *
     * @return the maximum number of characters in a user name. If the limit is
     *         unknown, or the value is unlimited, then the result is zero.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getMaxUserNameLength() throws SQLException;

    /**
     * Returns a list of the math functions available with this database. These
     * are used in the JDBC function escape clause and are the Open Group CLI
     * math function names.
     *
     * @return a String which contains the list of math functions as a comma
     *         separated list.
     * @throws SQLException
     *             a database error occurred.
     */
    public String getNumericFunctions() throws SQLException;

    /**
     * Returns a list of the primary key columns of a specified table.
     * <p>
     * The list is returned as a {@code ResultSet} with one row for each primary
     * key column, ordered by {@code COLUMN_NAME}, with each row having the
     * structure as follows:
     * <ol>
     * <li>{@code TABLE_CAT} - String - table catalog name (possibly null)</li>
     * <li>{@code TABLE_SCHEM} - String - table schema name (possibly null)</li>
     * <li>{@code TABLE_NAME} - String - The table name</li>
     * <li>{@code COLUMN_NAME} - String - The column name</li>
     * <li>{@code KEY_SEQ} - short - the sequence number for this column in the
     * primary key</li>
     * <li>{@code PK_NAME} - String - the primary key name (possibly null)</li>
     * </ol>
     *
     * @param catalog
     *            a catalog name. {@code null} is used to imply no narrowing of
     *            the search by catalog name. Otherwise, the name must match a
     *            catalog name held in the database, with the empty string used
     *            to retrieve those without a catalog name.
     * @param schema
     *            a schema name. {@code null} is used to imply no narrowing of
     *            the search by schema name. Otherwise, the name must match a
     *            schema name in the database, with the empty string used to
     *            retrieve those without a schema name.
     * @param table
     *            the name of a table, which must match the name of a table in
     *            the database.
     * @return a {@code ResultSet} containing the list of keys in the format
     *         defined above.
     * @throws SQLException
     *             a database error occurred.
     */
    public ResultSet getPrimaryKeys(String catalog, String schema, String table)
            throws SQLException;

    /**
     * Returns a list of parameter and result columns for the stored procedures
     * belonging to a specified catalog.
     * <p>
     * The list is returned as a {@code ResultSet} with one row for each
     * parameter or result column. The data is ordered by {@code
     * PROCEDURE_SCHEM} and {@code PROCEDURE_NAME}, while for each procedure,
     * the return value (if any) is first, followed by the parameters in the
     * order they appear in the stored procedure call, followed by {@code
     * ResultSet} columns in column number order. Each row has the following
     * structure:
     * <ol>
     * <li>{@code PROCEDURE_CAT} - String - the procedure catalog name</li>
     * <li>{@code PROCEDURE_SCHEM} - String - the procedure schema name
     * (possibly null)</li>
     * <li>{@code PROCEDURE_NAME} - String - the procedure name</li>
     * <li>{@code COLUMN_NAME} - String - the name of the column</li>
     * <li>{@code COLUMN_TYPE} - short - the kind of column or parameter, as
     * follows:
     * <ul>
     * <li>{@code DatabaseMetaData.procedureColumnUnknown} - type unknown</li>
     * <li>{@code DatabaseMetaData.procedureColumnIn} - an {@code IN} parameter</li>
     * <li>{@code DatabaseMetaData.procedureColumnInOut} - an {@code INOUT}
     * parameter</li>
     * <li>{@code DatabaseMetaData.procedureColumnOut} - an {@code OUT}
     * parameter</li>
     * <li>{@code DatabaseMetaData.procedureColumnReturn} - a return value</li>
     * <li>{@code DatabaseMetaData.procedureReturnsResult} - a result column in
     * a result set</li>
     * </ul>
     * </li>
     * <li>{@code DATA_TYPE} - int - the SQL type of the data, as in {@code
     * java.sql.Types}</li>
     * <li>{@code TYPE_NAME} - String - the SQL type name, for a UDT it is fully
     * qualified</li>
     * <li>{@code PRECISION} - int - the precision</li>
     * <li>{@code LENGTH} - int - the length of the data in bytes</li>
     * <li>{@code SCALE} - short - the scale for numeric types</li>
     * <li>{@code RADIX} - short - the Radix for numeric data (typically 2 or
     * 10)</li>
     * <li>{@code NULLABLE} - short - can the data contain {@code null}:
     * <ul>
     * <li>{@code DatabaseMetaData.procedureNoNulls} - {@code NULL}s not
     * permitted</li>
     * <li>{@code DatabaseMetaData.procedureNullable} - {@code NULL}s are
     * permitted</li>
     * <li>{@code DatabaseMetaData.procedureNullableUnknown} - {@code NULL}
     * status unknown</li>
     * </ul>
     * </li>
     * <li>{@code REMARKS} - String - an explanatory comment about the data item
     * </li>
     * </ol>
     *
     * @param catalog
     *            a catalog name. {@code null} is used to imply no narrowing of
     *            the search by catalog name. Otherwise, the name must match a
     *            catalog name held in the database, with "" used to retrieve
     *            those without a catalog name.
     * @param schemaPattern
     *            a schema name pattern. {@code null} is used to imply no
     *            narrowing of the search by schema name. Otherwise, the name
     *            must match a schema name in the database, with "" used to
     *            retrieve those without a schema name.
     * @param procedureNamePattern
     *            a pattern that must match the name of the procedure stored in
     *            the database.
     * @param columnNamePattern
     *            a column name pattern. The name must match the column name
     *            stored in the database.
     * @return a {@code ResultSet} with the list of parameter and result columns
     *         in the format defined above.
     * @throws SQLException
     *             a database error occurred.
     */
    public ResultSet getProcedureColumns(String catalog, String schemaPattern,
            String procedureNamePattern, String columnNamePattern)
            throws SQLException;

    /**
     * Returns a list of the stored procedures available in a specified catalog.
     * <p>
     * The list is returned as a {@code ResultSet} with one row for each stored
     * procedure, ordered by PROCEDURE_SCHEM and PROCEDURE_NAME, with the data
     * in each row as follows:
     * <ol>
     * <li>{@code PROCEDURE_CAT} - String : the procedure catalog name</li>
     * <li>{@code PROCEDURE_SCHEM} - String : the procedure schema name
     * (possibly {@code null})</li>
     * <li>{@code PROCEDURE_NAME} - String : the procedure name</li>
     * <li>{@code Reserved}</li>
     * <li>{@code Reserved}</li>
     * <li>{@code Reserved}</li>
     * <li>{@code REMARKS} - String - information about the procedure</li>
     * <li>{@code PROCEDURE_TYPE} - short : one of:
     * <ul>
     * <li>{@code DatabaseMetaData.procedureResultUnknown} - procedure may
     * return a result</li>
     * <li>{@code DatabaseMetaData.procedureNoResult} - procedure does not
     * return a result</li>
     * <li>{@code DatabaseMetaData.procedureReturnsResult} - procedure
     * definitely returns a result</li>
     * </ul>
     * </li>
     * </ol>
     *
     * @param catalog
     *            a catalog name. {@code null} is used to imply no narrowing of
     *            the search by catalog name. Otherwise, the name must match a
     *            catalog name held in the database, with "" used to retrieve
     *            those without a catalog name.
     * @param schemaPattern
     *            a schema name pattern. {@code null} is used to imply no
     *            narrowing of the search by schema name. Otherwise, the name
     *            must match a schema name in the database, with "" used to
     *            retrieve those without a schema name.
     * @param procedureNamePattern
     *            a procedure name pattern, which must match the procedure name
     *            stored in the database.
     * @return a {@code ResultSet} where each row is a description of a stored
     *         procedure in the format defined above.
     * @throws SQLException
     *             a database error occurred.
     */
    public ResultSet getProcedures(String catalog, String schemaPattern,
            String procedureNamePattern) throws SQLException;

    /**
     * Returns the database vendor's preferred name for "procedure".
     *
     * @return a String with the vendor's preferred name for "procedure".
     * @throws SQLException
     *             a database error occurred.
     */
    public String getProcedureTerm() throws SQLException;

    /**
     * Returns the result set's default holdability.
     *
     * @return one of {@code ResultSet.HOLD_CURSORS_OVER_COMMIT} or {@code
     *         ResultSet.CLOSE_CURSORS_AT_COMMIT}.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getResultSetHoldability() throws SQLException;

    /**
     * Returns a list of the schema names in the database. The list is returned
     * as a {@code ResultSet}, ordered by the schema name, with one row per
     * schema in the following format:
     * <ol>
     * <li>{@code TABLE_SCHEM} - String - the schema name</li> <li>{@code
     * TABLE_CATALOG} - String - the catalog name (possibly {@code null}) </li>
     * </ol>
     *
     * @return a {@code ResultSet} with one row for each schema in the format
     *         defined above.
     * @throws SQLException
     *             a database error occurred.
     */
    public ResultSet getSchemas() throws SQLException;

    /**
     * Returns the database vendor's preferred term for "schema".
     *
     * @return a String which is the vendor's preferred term for schema.
     * @throws SQLException
     *             a database error occurred.
     */
    public String getSchemaTerm() throws SQLException;

    /**
     * Returns the string that is used to escape wildcard characters. This
     * string is used to escape the {@code '_'} and {@code '%'} wildcard
     * characters in catalog search pattern strings. {@code '_'} is used to represent any single
     * character while {@code '%'} is used for a sequence of zero or more
     * characters.
     *
     * @return a String used to escape the wildcard characters.
     * @throws SQLException
     *             a database error occurred.
     */
    public String getSearchStringEscape() throws SQLException;

    /**
     * Returns a list of all the SQL keywords that are NOT also SQL92 keywords
     * for the database.
     *
     * @return a String containing the list of SQL keywords in a comma separated
     *         format.
     * @throws SQLException
     *             a database error occurred.
     */
    public String getSQLKeywords() throws SQLException;

    /**
     * States the type of {@code SQLState} value returned by {@code
     * SQLException.getSQLState}. This can either be the X/Open (now known as
     * Open Group) SQL CLI form or the SQL99 form.
     *
     * @return an integer, which is either {@code
     *         DatabaseMetaData.sqlStateSQL99} or {@code
     *         DatabaseMetaData.sqlStateXOpen}.
     * @throws SQLException
     *             a database error occurred.
     */
    public int getSQLStateType() throws SQLException;

    /**
     * Returns a list of string functions available with the database. These
     * functions are used in JDBC function escape clause and follow the Open
     * Group CLI string function names definition.
     *
     * @return a String containing the list of string functions in comma
     *         separated format.
     * @throws SQLException
     *             a database error occurred.
     */
    public String getStringFunctions() throws SQLException;

    /**
     * Returns a listing of the hierarchies of tables in a specified schema in
     * the database.
     * <p>
     * The listing only contains entries for tables that have a super table.
     * Super tables and corresponding subtables must be defined in the same catalog and schema. The
     * list is returned as a {@code ResultSet}, with one row for each table that
     * has a super table, in the following format:
     * <ol>
     * <li>{@code TABLE_CAT} - String - table catalog name (possibly {@code
     * null})</li>
     * <li>{@code TABLE_SCHEM} - String - Table schema name (possibly {@code
     * null})</li>
     * <li>{@code TABLE_NAME} - String - The table name</li>
     * <li>SUPER{@code TABLE_NAME} - String - The super table name</li>
     * </ol>
     *
     * @param catalog
     *            a catalog name. {@code null} is used to imply no narrowing of
     *            the search by catalog name. Otherwise, the name must match a
     *            catalog name held in the database, with "" used to retrieve
     *            those without a catalog name.
     * @param schemaPattern
     *            a schema name pattern. {@code null} is used to imply no
     *            narrowing of the search by schema name. Otherwise, the name
     *            must match a schema name in the database, with "" used to
     *            retrieve those without a schema name.
     * @param tableNamePattern
     *            a table name, which should match the table name as stored in
     *            the database. it may be a fully qualified name. If it is fully
     *            qualified the catalog name and schema name parameters are
     *            ignored.
     * @return a {@code ResultSet} with one row for each table which has a super
     *         table, in the format defined above. An empty {@code ResultSet} is
     *         returned if the database does not support table hierarchies.
     * @throws SQLException
     *             a database error occurred.
     */
    public ResultSet getSuperTables(String catalog, String schemaPattern,
            String tableNamePattern) throws SQLException;

    /**
     * Returns the User Defined Type (UDT) hierarchies for a given schema. Only
     * the immediate parent/child relationship is described. If a UDT does not
     * have a direct supertype, it is not listed.
     * <p>
     * The listing is returned as a {@code ResultSet} where there is one row for
     * a specific UDT which describes its supertype, with the data organized in
     * columns as follows:
     * <ol>
     * <li>{@code TYPE_CAT} - String - the UDT catalog name (possibly {@code
     * null})</li>
     * <li>{@code TYPE_SCHEM} - String - the UDT schema name (possibly {@code
     * null})</li>
     * <li>{@code TYPE_NAME} - String - the UDT type name</li>
     * <li>SUPER{@code TYPE_CAT} - String - direct supertype's catalog name
     * (possibly {@code null})</li>
     * <li>SUPER{@code TYPE_SCHEM} - String - direct supertype's schema name
     * (possibly {@code null})</li>
     * <li>SUPER{@code TYPE_NAME} - String - direct supertype's name</li>
     * </ol>
     *
     * @param catalog
     *            the catalog name. "" means get the UDTs without a catalog.
     *            {@code null} means don't use the catalog name to restrict the
     *            search.
     * @param schemaPattern
     *            the Schema pattern name. "" means get the UDT's without a
     *            schema.
     * @param typeNamePattern
     *            the UDT name pattern. This may be a fully qualified name. When
     *            a fully qualified name is specified, the catalog name and
     *            schema name parameters are ignored.
     * @return a {@code ResultSet} in which each row gives information about a
     *         particular UDT in the format defined above. An empty ResultSet is
     *         returned for a database that does not support type hierarchies.
     * @throws SQLException
     *             a database error occurred.
     */
    public ResultSet getSuperTypes(String catalog, String schemaPattern,
            String typeNamePattern) throws SQLException;

    /**
     * Returns a list of system functions available with the database. These are
     * names used in the JDBC function escape clause and are Open Group CLI
     * function names.
     *
     * @return a String containing the list of system functions in a comma
     *         separated format.
     * @throws SQLException
     *             a database error occurred.
     */
    public String getSystemFunctions() throws SQLException;

    /**
     * Returns a description of access rights for each table present in a
     * catalog. Table privileges can apply to one or more columns in the table -
     * but are not guaranteed to apply to all columns.
     * <p>
     * The privileges are returned as a {@code ResultSet}, with one row for each
     * privilege, ordered by {@code TABLE_SCHEM}, {@code TABLE_NAME}, {@code
     * PRIVILEGE}, and each row has data as defined in the following column
     * definitions:
     * <ol>
     * <li>{@code TABLE_CAT} - String - table catalog name (possibly {@code
     * null})</li>
     * <li>{@code TABLE_SCHEM} - String - Table schema name (possibly {@code
     * null})</li>
     * <li>{@code TABLE_NAME} - String - The table name</li>
     * <li>GRANTOR - String - who granted the access</li>
     * <li>GRANTEE - String - who received the access grant</li>
     * <li>PRIVILEGE - String - the type of access granted - one of SELECT,
     * INSERT, UPDATE, REFERENCES,...</li>
     * <li>IS_GRANTABLE - String - {@code "YES"} implies the grantee can grant
     * access to others, {@code "NO"} implies guarantee cannot grant access to
     * others, {@code null} means this status is unknown</li>
     * </ol>
     *
     * @param catalog
     *            a catalog name. {@code null} is used to imply no narrowing of
     *            the search by catalog name. Otherwise, the name must match a
     *            catalog name held in the database, with "" used to retrieve
     *            those without a catalog name.
     * @param schemaPattern
     *            a schema name pattern. {@code null} is used to imply no
     *            narrowing of the search by schema name. Otherwise, the name
     *            must match a schema name in the database, with "" used to
     *            retrieve those without a schema name.
     * @param tableNamePattern
     *            a Table Name, which should match the table name as stored in
     *            the database.
     * @return a {@code ResultSet} containing a list with one row for each table
     *         in the format defined above.
     * @throws SQLException
     *             a database error occurred.
     */
    public ResultSet getTablePrivileges(String catalog, String schemaPattern,
            String tableNamePattern) throws SQLException;

    /**
     * Returns a description of the tables in a specified catalog.
     * <p>
     * The descriptions are returned as rows in a {@code ResultSet}, one row for
     * each Table. The ResultSet is ordered by {@code TABLE_TYPE}, {@code
     * TABLE_SCHEM} and {@code TABLE_NAME}. Each row in the ResultSet consists
     * of a series of columns as follows:
     * <ol>
     * <li>{@code TABLE_CAT} - String - table catalog name (possibly {@code
     * null})</li>
     * <li>{@code TABLE_SCHEM} - String - Table schema name (possibly {@code
     * null})</li>
     * <li>{@code TABLE_NAME} - String - The table name</li>
     * <li>{@code TABLE_TYPE} - String - Typical names include "TABLE", "VIEW",
     * "SYSTEM TABLE", "ALIAS", "SYNONYM", "GLOBAL TEMPORARY"</li>
     * <li>{@code REMARKS} - String - A comment describing the table</li>
     * <li>{@code TYPE_CAT} - String - the 'Types' catalog(possibly {@code null}
     * )</li>
     * <li>{@code TYPE_SCHEM} - String - the 'Types' schema(possibly {@code
     * null})</li>
     * <li>{@code TYPE_NAME} - String - the 'Types' name (possibly {@code null})
     * </li>
     * <li>{@code SELF_REFERENCING_COL_NAME} - String - the name of a designated
     * identifier column in a typed table (possibly {@code null})</li>
     * <li>REF_GENERATION - String - one of the following values : "SYSTEM" |
     * "USER" | "DERIVED" - specifies how values in the {@code
     * SELF_REFERENCING_COL_NAME} are created (possibly {@code null})</li>
     * </ol>
     *
     * @param catalog
     *            a catalog name. {@code null} is used to imply no narrowing of
     *            the search by catalog name. Otherwise, the name must match a
     *            catalog name held in the database, with "" used to retrieve
     *            those without a catalog name.
     * @param schemaPattern
     *            a schema name pattern. {@code null} is used to imply no
     *            narrowing of the search by schema name. Otherwise, the name
     *            must match a schema name in the database, with "" used to
     *            retrieve those without a schema name.
     * @param tableNamePattern
     *            a table name, which should match the table name as stored in
     *            the database.
     * @param types
     *            a list of table types to include in the list. {@code null}
     *            implies list all types.
     * @return a {@code ResultSet} with one row per table in the format defined
     *         above.
     * @throws SQLException
     *             a database error occurred.
     */
    public ResultSet getTables(String catalog, String schemaPattern,
            String tableNamePattern, String[] types) throws SQLException;

    /**
     * Returns a list of table types supported by the database.
     * <p>
     * The list is returned as a {@code ResultSet} with one row per table type,
     * ordered by the table type. The information in the {@code ResultSet} is
     * structured into a single column per row, as follows:
     * <ol>
     * <li>{@code TABLE_TYPE} - String - the table type. Typical names include
     * {@code "TABLE"}, {@code "VIEW"}, "{@code SYSTEM TABLE"}, {@code "ALIAS"},
     * {@code "SYNONYM"}, {@code "GLOBAL TEMPORARY"}</li>
     * </ol>
     *
     * @return a {@code ResultSet} with one row per table type in the format
     *         defined above.
     * @throws SQLException
     *             a database error occurred.
     */
    public ResultSet getTableTypes() throws SQLException;

    /**
     * Returns a list of time and date functions available for the database.
     *
     * @return a string containing a comma separated list of the time and date
     *         functions.
     * @throws SQLException
     *             a database error occurred.
     */
    public String getTimeDateFunctions() throws SQLException;

    /**
     * Get a list of the standard SQL types supported by this database. The list
     * is returned as a {@code ResultSet}, with one row for each type, ordered
     * by the {@code DATA_TYPE} value, where the data in each row is structured
     * into the following columns:
     * <ol>
     * <li>{@code TYPE_NAME} - String : the type name</li>
     * <li>{@code DATA_TYPE} - int : the SQL data type value as defined in
     * {@code java.sql.Types}</li>
     * <li>{@code PRECISION} - int - the maximum precision of the type</li>
     * <li>{@code LITERAL_PREFIX} - String : the prefix to be used when quoting
     * a literal value (possibly {@code null})</li>
     * <li>{@code LITERAL_SUFFIX} - String : the suffix to be used when quoting
     * a literal value (possibly {@code null})</li>
     * <li>{@code CREATE_PARAMS} - String : params used when creating the type
     * (possibly {@code null})</li>
     * <li>{@code NULLABLE} - short : shows if the value is nullable:
     * <ul>
     * <li>{@code DatabaseMetaData.typeNoNulls} : {@code NULL}s not permitted</li>
     * <li>{@code DatabaseMetaData.typeNullable} : {@code NULL}s are permitted</li>
     * <li>{@code DatabaseMetaData.typeNullableUnknown} : {@code NULL} status
     * unknown</li>
     * </ul>
     * </li>
     * <li>{@code CASE_SENSITIVE} - boolean : true if the type is case sensitive
     * </li>
     * <li>{@code SEARCHABLE} - short : how this type can be used with {@code WHERE}
     * clauses:
     * <ul>
     * <li>{@code DatabaseMetaData.typePredNone} - {@code WHERE} clauses cannot be used</li>
     * <li>{@code DatabaseMetaData.typePredChar} - support for {@code
     * WHERE...LIKE} only</li>
     * <li>{@code DatabaseMetaData.typePredBasic} - support except for {@code
     * WHERE...LIKE}</li>
     * <li>{@code DatabaseMetaData.typeSearchable} - support for all {@code
     * WHERE} clauses</li>
     * </ul>
     * </li>
     * <li>{@code UNSIGNED_ATTRIBUTE} - boolean - the type is unsigned or not</li>
     * <li>{@code FIXED_PREC_SCALE} - boolean - fixed precision = it can be used
     * as a money value</li>
     * <li>{@code AUTO_INCREMENT} - boolean - can be used as an auto-increment
     * value</li>
     * <li>{@code LOCAL_TYPE_NAME} - String - a localized version of the type
     * name (possibly {@code null})</li>
     * <li>{@code MINIMUM_SCALE} - short - the minimum scale supported</li>
     * <li>{@code MAXIMUM_SCALE} - short - the maximum scale supported</li>
     * <li>{@code SQL_DATA_TYPE} - int - not used</li>
     * <li>{@code SQL_DATETIME_SUB} - int - not used</li>
     * <li>{@code NUM_PREC_RADIX} - int - number radix (typically 2 or 10)</li>
     * </ol>
     *
     * @return a {@code ResultSet} which is structured as described above.
     * @throws SQLException
     *             a database error occurred.
     */
    public ResultSet getTypeInfo() throws SQLException;

    /**
     * Returns a description of the User Defined Types (UDTs) defined in a given
     * schema, which includes the types {@code DISTINCT}, {@code STRUCT} and
     * {@code JAVA_OBJECT}.
     * <p>
     * The types matching the supplied the specified catalog, schema, type name
     * and type are returned as rows in a {@code ResultSet} with columns of
     * information as follows:
     * <ol>
     * <li>{@code TABLE_CAT} - String - catalog name (possibly {@code null})</li>
     * <li>{@code TABLE_SCHEM} - String - schema name (possibly {@code null})</li>
     * <li>{@code TABLE_NAME} - String - The table name</li>
     * <li>{@code CLASS_NAME} - String - The Java class name</li>
     * <li>{@code DATA_TYPE} - int - The SQL type as specified in {@code
     * java.sql.Types}. One of DISTINCT, STRUCT, and JAVA_OBJECT</li>
     * <li>{@code REMARKS} - String - A comment which describes the type</li>
     * <li>{@code BASE_TYPE} - short - A type code. For a DISTINCT type, the
     * source type. For a structured type this is the type that implements the
     * user generated reference type of the {@code SELF_REFERENCING_COLUMN}.
     * This is defined in {@code java.sql.Types}, and will be {@code null} if
     * the {@code DATA_TYPE} does not match these criteria.</li>
     * </ol>
     * <p>
     * If the driver does not support UDTs, the {@code ResultSet} is empty.
     *
     * @param catalog
     *            a catalog name. {@code null} is used to imply no narrowing of
     *            the search by catalog name. Otherwise, the name must match a
     *            catalog name held in the database, with "" used to retrieve
     *            those without a catalog name.
     * @param schemaPattern
     *            a schema name pattern. {@code null} is used to imply no
     *            narrowing of the search using schema name. Otherwise, the name
     *            must match a schema name in the database, with "" used to
     *            retrieve those without a schema name.
     * @param typeNamePattern
     *            a type name pattern, which should match a type name as stored in the
     *            database. It may be fully qualified.
     * @param types
     *            a list of the UDT types to include in the list - one of
     *            {@code DISTINCT}, {@code STRUCT} or {@code JAVA_OBJECT}.
     * @return a {@code ResultSet} in the format described above.
     * @throws SQLException
     *             a database error occurred.
     */
    public ResultSet getUDTs(String catalog, String schemaPattern,
            String typeNamePattern, int[] types) throws SQLException;

    /**
     * Returns the URL for this database.
     *
     * @return the URL for the database. {@code null} if it cannot be generated.
     * @throws SQLException
     *             a database error occurred.
     */
    public String getURL() throws SQLException;

    /**
     * Determine the user name as known by the database.
     *
     * @return the user name.
     * @throws SQLException
     *             a database error occurred.
     */
    public String getUserName() throws SQLException;

    /**
     * Returns which of a table's columns are automatically updated when any
     * value in a row is updated.
     * <p>
     * The result is laid-out in the following columns:
     * <ol>
     * <li>{@code SCOPE} - short - not used</li>
     * <li>{@code COLUMN_NAME} - String - Column name</li>
     * <li>{@code DATA_TYPE} - int - The SQL data type, as defined in {@code
     * java.sql.Types}</li>
     * <li>{@code TYPE_NAME} - String - The SQL type name, data source dependent
     * </li>
     * <li>{@code COLUMN_SIZE} - int - Precision for numeric types</li>
     * <li>{@code BUFFER_LENGTH} - int - Length of a column value in bytes</li>
     * <li>{@code DECIMAL_DIGITS} - short - Number of digits after the decimal
     * point</li>
     * <li>{@code PSEUDO_COLUMN} - short - If this is a pseudo-column (for
     * example, an Oracle {@code ROWID}):
     * <ul>
     * <li>{@code DatabaseMetaData.bestRowUnknown} - don't know whether this is
     * a pseudo column</li>
     * <li>{@code DatabaseMetaData.bestRowNotPseudo} - column is not pseudo</li>
     * <li>{@code DatabaseMetaData.bestRowPseudo} - column is a pseudo column</li>
     * </ul>
     * </li>
     * </ol>
     *
     * @param catalog
     *            a catalog name. {@code null} is used to imply no narrowing of
     *            the search using catalog name. Otherwise, the name must match
     *            a catalog name held in the database, with "" used to retrieve
     *            those without a catalog name.
     * @param schema
     *            a schema name pattern. {@code null} is used to imply no
     *            narrowing of the search using schema names. Otherwise, the
     *            name must match a schema name in the database, with "" used to
     *            retrieve those without a schema name.
     * @param table
     *            a table name. It must match the name of a table in the
     *            database.
     * @return a {@code ResultSet} containing the descriptions, one row for each
     *         column, in the format defined above.
     * @throws SQLException
     *             a database error occurred.
     */
    public ResultSet getVersionColumns(String catalog, String schema,
            String table) throws SQLException;

    /**
     * Determines whether a visible row insert can be detected by calling {@code
     * ResultSet.rowInserted}.
     *
     * @param type
     *            the {@code ResultSet} type. This may be one of {@code
     *            ResultSet.TYPE_SCROLL_SENSITIVE} or {@code
     *            ResultSet.TYPE_SCROLL_INSENSITIVE} or {@code
     *            ResultSet.TYPE_FORWARD_ONLY},
     * @return {@code true} if {@code ResultSet.rowInserted} detects a visible
     *         row insert otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     * @see ResultSet#rowInserted()
     */
    public boolean insertsAreDetected(int type) throws SQLException;

    /**
     * Determine whether a fully qualified table name is prefixed or suffixed to
     * a fully qualified table name.
     *
     * @return {@code true} if the catalog appears at the start of a fully
     *         qualified table name, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean isCatalogAtStart() throws SQLException;

    /**
     * Determines whether the database is in read-only mode.
     *
     * @return {@code true} if the database is in read-only mode, {@code false}
     *         otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean isReadOnly() throws SQLException;

    /**
     * Determines whether updates are made to a copy of, or directly on, Large Objects
     * ({@code LOB}s).
     *
     * @return {@code true} if updates are made to a copy of the Large Object,
     *         {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean locatorsUpdateCopy() throws SQLException;

    /**
     * Determines whether the database handles concatenations between {@code NULL} and
     * non-{@code NULL} values by producing a {@code NULL} output.
     *
     * @return {@code true} if {@code NULL} to non-{@code NULL} concatenations
     *         produce a {@code NULL} result, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean nullPlusNonNullIsNull() throws SQLException;

    /**
     * Determines whether {@code NULL} values are always sorted to the end of sorted
     * results regardless of requested sort order. This means that they will
     * appear at the end of sorted lists whatever other non-{@code NULL} values
     * may be present.
     *
     * @return {@code true} if {@code NULL} values are sorted at the end,
     *         {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean nullsAreSortedAtEnd() throws SQLException;

    /**
     * Determines whether {@code NULL} values are always sorted at the start of the
     * sorted list, irrespective of the sort order. This means that they appear
     * at the start of sorted lists, whatever other values may be present.
     *
     * @return {@code true} if {@code NULL} values are sorted at the start,
     *         {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean nullsAreSortedAtStart() throws SQLException;

    /**
     * Determines whether {@code NULL} values are sorted high - i.e. they are sorted
     * as if they are higher than any other values.
     *
     * @return {@code true} if {@code NULL} values are sorted high, {@code
     *         false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean nullsAreSortedHigh() throws SQLException;

    /**
     * Determines whether {@code NULL} values are sorted low - i.e. they are sorted as
     * if they are lower than any other values.
     *
     * @return {@code true} if {@code NULL} values are sorted low, {@code false}
     *         otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean nullsAreSortedLow() throws SQLException;

    /**
     * Determines whether deletes made by others are visible, for a specified {@code
     * ResultSet} type.
     *
     * @param type
     *            the type of the {@code ResultSet}. It may be either {@code
     *            ResultSet.TYPE_FORWARD_ONLY} or {@code
     *            ResultSet.TYPE_SCROLL_INSENSITIVE}, or {@code
     *            ResultSet.TYPE_SCROLL_SENSITIVE})
     * @return {@code true} if others' deletes are visible, {@code false}
     *         otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean othersDeletesAreVisible(int type) throws SQLException;

    /**
     * Determines whether inserts made by others are visible, for a specified {@code
     * ResultSet} type.
     *
     * @param type
     *            the type of the {@code ResultSet}. May be {@code
     *            ResultSet.TYPE_FORWARD_ONLY}, or {@code
     *            ResultSet.TYPE_SCROLL_INSENSITIVE}, or {@code
     *            ResultSet.TYPE_SCROLL_SENSITIVE}
     * @return {@code true} if others' inserts are visible, otherwise {@code
     *         false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean othersInsertsAreVisible(int type) throws SQLException;

    /**
     * Determines whether updates made by others are visible, for a specified {@code
     * ResultSet} type.
     *
     * @param type
     *            the type of the {@code ResultSet}. May be {@code
     *            ResultSet.TYPE_FORWARD_ONLY}, or {@code
     *            ResultSet.TYPE_SCROLL_INSENSITIVE}, or {@code
     *            ResultSet.TYPE_SCROLL_SENSITIVE}
     * @return {@code true} if others' inserts are visible, otherwise {@code
     *         false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean othersUpdatesAreVisible(int type) throws SQLException;

    /**
     * Determines whether a {@code ResultSet} can see its own deletes, for a
     * specified {@code ResultSet} type.
     *
     * @param type
     *            the type of the {@code ResultSet}: {@code
     *            ResultSet.TYPE_FORWARD_ONLY}, {@code
     *            ResultSet.TYPE_SCROLL_INSENSITIVE}, or {@code
     *            ResultSet.TYPE_SCROLL_SENSITIVE}
     * @return {@code true} if the deletes are seen by the {@code
     *         ResultSet} itself, otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean ownDeletesAreVisible(int type) throws SQLException;

    /**
     * Determines whether a {@code ResultSet} can see its own inserts, for a
     * specified {@code ResultSet} type.
     *
     * @param type
     *            the type of the {@code ResultSet}: {@code
     *            ResultSet.TYPE_FORWARD_ONLY}, {@code
     *            ResultSet.TYPE_SCROLL_INSENSITIVE}, or {@code
     *            ResultSet.TYPE_SCROLL_SENSITIVE}
     * @return {@code true} if the inserts are seen by the {@code
     *         ResultSet} itself, otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean ownInsertsAreVisible(int type) throws SQLException;

    /**
     * Determines whether a {@code ResultSet} can see its own updates, for a
     * specified {@code ResultSet} type.
     *
     * @param type
     *            the type of the {@code ResultSet}: {@code
     *            ResultSet.TYPE_FORWARD_ONLY}, {@code
     *            ResultSet.TYPE_SCROLL_INSENSITIVE}, or {@code
     *            ResultSet.TYPE_SCROLL_SENSITIVE}
     * @return {@code true} if the updates are seen by the {@code
     *         ResultSet} itself, otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean ownUpdatesAreVisible(int type) throws SQLException;

    /**
     * Determines whether the database treats SQL identifiers that are in mixed
     * case (and unquoted) as case insensitive. If {@code true} then the
     * database stores them in lower case.
     *
     * @return {@code true} if unquoted SQL identifiers are stored in lower
     *         case, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean storesLowerCaseIdentifiers() throws SQLException;

    /**
     * Determines whether the database considers mixed case quoted SQL
     * identifiers as case insensitive and stores them in lower case.
     *
     * @return {@code true} if quoted SQL identifiers are stored in lower case,
     *         {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException;

    /**
     * Determines whether the database considers mixed case unquoted SQL
     * identifiers as case insensitive and stores them in mixed case.
     *
     * @return {@code true} if unquoted SQL identifiers as stored in mixed case,
     *         {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean storesMixedCaseIdentifiers() throws SQLException;

    /**
     * Determines whether the database considers identifiers as case insensitive
     * if they are mixed case quoted SQL. The database stores them in mixed
     * case.
     *
     * @return {@code true} if quoted SQL identifiers are stored in mixed case,
     *         {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException;

    /**
     * Determines whether the database considers mixed case unquoted SQL
     * identifiers as case insensitive and stores them in upper case.
     *
     * @return {@code true} if unquoted SQL identifiers are stored in upper
     *         case, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean storesUpperCaseIdentifiers() throws SQLException;

    /**
     * Determines whether the database considers mixed case quoted SQL
     * identifiers as case insensitive and stores them in upper case.
     *
     * @return {@code true} if quoted SQL identifiers are stored in upper case,
     *         {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException;

    /**
     * Determines whether the database supports {@code ALTER TABLE} operation with
     * {@code ADD COLUMN}.
     *
     * @return {@code true} if {@code ALTER TABLE} with {@code ADD COLUMN} is
     *         supported, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsAlterTableWithAddColumn() throws SQLException;

    /**
     * Determines whether the database supports {@code ALTER TABLE} operation with
     * {@code DROP COLUMN}.
     *
     * @return {@code true} if {@code ALTER TABLE} with {@code DROP COLUMN} is
     *         supported, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsAlterTableWithDropColumn() throws SQLException;

    /**
     * Determines whether the database supports the ANSI92 entry level SQL grammar.
     *
     * @return {@code true} if the ANSI92 entry level SQL grammar is supported,
     *         {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsANSI92EntryLevelSQL() throws SQLException;

    /**
     * Determines whether the database supports the ANSI92 full SQL grammar.
     *
     * @return {@code true} if the ANSI92 full SQL grammar is supported, {@code
     *         false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsANSI92FullSQL() throws SQLException;

    /**
     * Determines whether the database supports the ANSI92 intermediate SQL Grammar.
     *
     * @return {@code true} if the ANSI92 intermediate SQL grammar is supported,
     *         {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsANSI92IntermediateSQL() throws SQLException;

    /**
     * Determines whether the database supports batch updates.
     *
     * @return {@code true} if batch updates are supported, {@code false}
     *         otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsBatchUpdates() throws SQLException;

    /**
     * Determines whether catalog names may be used in data manipulation
     * statements.
     *
     * @return {@code true} if catalog names can be used in data manipulation
     *         statements, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsCatalogsInDataManipulation() throws SQLException;

    /**
     * Determines whether catalog names can be used in index definition statements.
     *
     * @return {@code true} if catalog names can be used in index definition
     *         statements, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException;

    /**
     * Determines whether catalog names can be used in privilege definition
     * statements.
     *
     * @return {@code true} if catalog names can be used in privilege definition
     *         statements, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException;

    /**
     * Determines whether catalog names can be used in procedure call statements.
     *
     * @return {@code true} if catalog names can be used in procedure call
     *         statements.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsCatalogsInProcedureCalls() throws SQLException;

    /**
     * Determines whether catalog names may be used in table definition statements.
     *
     * @return {@code true} if catalog names can be used in definition
     *         statements, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsCatalogsInTableDefinitions() throws SQLException;

    /**
     * Determines whether the database supports column aliasing.
     * <p>
     * If aliasing is supported, then the SQL AS clause is used to provide names
     * for computed columns and provide alias names for columns.
     *
     * @return {@code true} if column aliasing is supported, {@code false}
     *         otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsColumnAliasing() throws SQLException;

    /**
     * Determines whether the database supports the {@code CONVERT} operation between
     * SQL types.
     *
     * @return {@code true} if the {@code CONVERT} operation is supported,
     *         {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsConvert() throws SQLException;

    /**
     * Determines whether the database supports {@code CONVERT} operation for two
     * supplied SQL types.
     *
     * @param fromType
     *            the Type to convert from, as defined by {@code java.sql.Types}
     * @param toType
     *            the Type to convert to, as defined by {@code java.sql.Types}
     * @return {@code true} if the {@code CONVERT} operation is supported for
     *         these types, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsConvert(int fromType, int toType)
            throws SQLException;

    /**
     * Determines whether the database supports the Core SQL Grammar for ODBC.
     *
     * @return {@code true} if the Core SQL Grammar is supported, {@code false}
     *         otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsCoreSQLGrammar() throws SQLException;

    /**
     * Determines whether the database supports correlated sub-queries.
     *
     * @return {@code true} if the database does support correlated sub-queries
     *         and {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsCorrelatedSubqueries() throws SQLException;

    /**
     * Determines whether the database allows both data definition and data
     * manipulation statements inside a transaction.
     *
     * @return {@code true} if both types of statement are permitted, {@code
     *         false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsDataDefinitionAndDataManipulationTransactions()
            throws SQLException;

    /**
     * Determines whether the database only allows data manipulation statements inside
     * a transaction.
     *
     * @return {@code true} if data manipulation statements are permitted only within a transaction,
     *         {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsDataManipulationTransactionsOnly()
            throws SQLException;

    /**
     * Determines whether table correlation names are required to be different from
     * the names of the tables, when they are supported.
     *
     * @return {@code true} if correlation names must be different from table
     *         names, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsDifferentTableCorrelationNames() throws SQLException;

    /**
     * Determines whether expressions in {@code ORDER BY} lists are supported.
     *
     * @return {@code true} if expressions in {@code ORDER BY} lists are
     *         supported.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsExpressionsInOrderBy() throws SQLException;

    /**
     * Determines whether the Extended SQL Grammar for ODBC is supported.
     *
     * @return {@code true} if the Extended SQL Grammar is supported, {@code
     *         false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsExtendedSQLGrammar() throws SQLException;

    /**
     * Determines whether the database supports full nested outer joins.
     *
     * @return {@code true} if full nested outer joins are supported, {@code
     *         false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsFullOuterJoins() throws SQLException;

    /**
     * Determines whether auto generated keys can be returned when a statement
     * executes.
     *
     * @return {@code true} if auto generated keys can be returned, {@code
     *         false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsGetGeneratedKeys() throws SQLException;

    /**
     * Determines whether the database supports {@code GROUP BY} clauses.
     *
     * @return {@code true} if the {@code GROUP BY} clause is supported, {@code
     *         false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsGroupBy() throws SQLException;

    /**
     * Determines whether the database supports using a column name in a {@code GROUP
     * BY} clause not included in the {@code SELECT} statement as long as all of
     * the columns in the {@code SELECT} statement are used in the {@code GROUP
     * BY} clause.
     *
     * @return {@code true} if {@code GROUP BY} clauses can use column names in
     *         this way, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsGroupByBeyondSelect() throws SQLException;

    /**
     * Determines whether the database supports using a column name in a {@code GROUP
     * BY} clause that is not in the {@code SELECT} statement.
     *
     * @return {@code true} if {@code GROUP BY} clause can use a column name not
     *         in the {@code SELECT} statement, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsGroupByUnrelated() throws SQLException;

    /**
     * Determines whether the database supports SQL Integrity Enhancement
     * Facility.
     *
     * @return {@code true} if the Integrity Enhancement Facility is supported,
     *         {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsIntegrityEnhancementFacility() throws SQLException;

    /**
     * Determines whether the database supports a {@code LIKE} escape clause.
     *
     * @return {@code true} if LIKE escape clause is supported, {@code false}
     *         otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsLikeEscapeClause() throws SQLException;

    /**
     * Determines whether the database provides limited support for outer join
     * operations.
     *
     * @return {@code true} if there is limited support for outer join
     *         operations, {@code false} otherwise. This will be {@code true} if
     *         {@code supportsFullOuterJoins} returns {@code true}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsLimitedOuterJoins() throws SQLException;

    /**
     * Determines whether the database supports Minimum SQL Grammar for ODBC.
     *
     * @return {@code true} if the Minimum SQL Grammar is supported, {@code
     *         false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsMinimumSQLGrammar() throws SQLException;

    /**
     * Determines whether the database treats mixed case unquoted SQL identifiers as
     * case sensitive storing them in mixed case.
     *
     * @return {@code true} if unquoted SQL identifiers are stored in mixed
     *         case, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsMixedCaseIdentifiers() throws SQLException;

    /**
     * Determines whether the database considers mixed case quoted SQL
     * identifiers as case sensitive, storing them in mixed case.
     *
     * @return {@code true} if quoted SQL identifiers are stored in mixed case,
     *         {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException;

    /**
     * Determines whether it is possible for a single {@code CallableStatement} to
     * return multiple {@code ResultSet}s simultaneously.
     *
     * @return {@code true} if a single {@code CallableStatement} can return
     *         multiple {@code ResultSet}s simultaneously, {@code false}
     *         otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsMultipleOpenResults() throws SQLException;

    /**
     * Determines whether retrieving multiple {@code ResultSet}s from a single
     * call to the {@code execute} method is supported.
     *
     * @return {@code true} if multiple {@code ResultSet}s can be retrieved,
     *         {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsMultipleResultSets() throws SQLException;

    /**
     * Determines whether multiple simultaneous transactions on
     * different connections are supported.
     *
     * @return {@code true} if multiple open transactions are supported, {@code
     *         false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsMultipleTransactions() throws SQLException;

    /**
     * Determines whether callable statements with named parameters is supported.
     *
     * @return {@code true} if named parameters can be used with callable
     *         statements, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsNamedParameters() throws SQLException;

    /**
     * Determines whether columns in the database can be defined as non-nullable.
     *
     * @return {@code true} if columns can be defined non-nullable, {@code
     *         false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsNonNullableColumns() throws SQLException;

    /**
     * Determines whether keeping cursors open across commit operations is
     * supported.
     *
     * @return {@code true} if cursors can be kept open across commit
     *         operations, {@code false} if they might get closed.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException;

    /**
     * Determines whether the database can keep cursors open across rollback
     * operations.
     *
     * @return {@code true} if cursors can be kept open across rollback
     *         operations, {@code false} if they might get closed.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException;

    /**
     * Determines whether keeping statements open across commit operations is
     * supported.
     *
     * @return {@code true} if statements can be kept open, {@code false} if
     *         they might not.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException;

    /**
     * Determines whether keeping statements open across rollback operations is
     * supported.
     *
     * @return {@code true} if statements can be kept open, {@code false} if
     *         they might not.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException;

    /**
     * Determines whether using a column in an {@code ORDER BY} clause that is
     * not in the {@code SELECT} statement is supported.
     *
     * @return {@code true} if it is possible to {@code ORDER} using a column
     *         not in the {@code SELECT}, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsOrderByUnrelated() throws SQLException;

    /**
     * Determines whether outer join operations are supported.
     *
     * @return {@code true} if outer join operations are supported, {@code
     *         false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsOuterJoins() throws SQLException;

    /**
     * Determines whether positioned {@code DELETE} statements are supported.
     *
     * @return {@code true} if the database supports positioned {@code DELETE}
     *         statements.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsPositionedDelete() throws SQLException;

    /**
     * Determines whether positioned {@code UPDATE} statements are supported.
     *
     * @return {@code true} if the database supports positioned {@code UPDATE}
     *         statements, {@code false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsPositionedUpdate() throws SQLException;

    /**
     * Determines whether there is support for a given concurrency style for the
     * given {@code ResultSet}.
     *
     * @param type
     *            the {@code ResultSet} type, as defined in {@code
     *            java.sql.ResultSet}:
     *            <ul>
     *            <li>{@code ResultSet.TYPE_FORWARD_ONLY}</li>
     *            <li>{@code ResultSet.TYPE_SCROLL_INSENSITIVE}</li>
     *            <li>{@code ResultSet.TYPE_SCROLL_SENSITIVE}</li>
     *            </ul>
     * @param concurrency
     *            a concurrency type, which may be one of {@code
     *            ResultSet.CONCUR_READ_ONLY} or {@code
     *            ResultSet.CONCUR_UPDATABLE}.
     * @return {@code true} if that concurrency and {@code ResultSet} type
     *         pairing is supported otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsResultSetConcurrency(int type, int concurrency)
            throws SQLException;

    /**
     * Determines whether the supplied {@code ResultSet} holdability mode is
     * supported.
     *
     * @param holdability
     *            as specified in {@code java.sql.ResultSet}: {@code
     *            ResultSet.HOLD_CURSORS_OVER_COMMIT} or {@code
     *            ResultSet.CLOSE_CURSORS_AT_COMMIT}
     * @return {@code true} if the given ResultSet holdability is supported and
     *         if it isn't then {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsResultSetHoldability(int holdability)
            throws SQLException;

    /**
     * Determines whether the supplied {@code ResultSet} type is supported.
     *
     * @param type
     *            the {@code ResultSet} type as defined in {@code
     *            java.sql.ResultSet}: {@code ResultSet.TYPE_FORWARD_ONLY},
     *            {@code ResultSet.TYPE_SCROLL_INSENSITIVE}, or {@code
     *            ResultSet.TYPE_SCROLL_SENSITIVE}
     * @return {@code true} if the {@code ResultSet} type is supported, {@code
     *         false} otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsResultSetType(int type) throws SQLException;

    /**
     * Determines whether savepoints for transactions are supported.
     *
     * @return {@code true} if savepoints are supported, {@code false}
     *         otherwise.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsSavepoints() throws SQLException;

    /**
     * Determines whether a schema name may be used in a data manipulation
     * statement.
     *
     * @return {@code true} if a schema name can be used in a data manipulation,
     *         otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsSchemasInDataManipulation() throws SQLException;

    /**
     * Determines whether a schema name may be used in an index definition
     * statement.
     *
     * @return {@code true} if a schema name can be used in an index definition,
     *         otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsSchemasInIndexDefinitions() throws SQLException;

    /**
     * Determines whether a database schema name can be used in a privilege
     * definition statement.
     *
     * @return {@code true} if a database schema name may be used in a privilege
     *         definition, otherwise {@code false}
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException;

    /**
     * Determines whether a procedure call statement may be contain in a schema name.
     *
     * @return {@code true} if a schema name can be used in a procedure call,
     *         otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsSchemasInProcedureCalls() throws SQLException;

    /**
     * Determines whether a schema name can be used in a table definition statement.
     *
     * @return {@code true} if a schema name can be used in a table definition,
     *         otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsSchemasInTableDefinitions() throws SQLException;

    /**
     * Determines whether the {@code SELECT FOR UPDATE} statement is supported.
     *
     * @return {@code true} if {@code SELECT FOR UPDATE} statements are
     *         supported, otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsSelectForUpdate() throws SQLException;

    /**
     * Determines whether statement pooling is supported.
     *
     * @return {@code true} of the database does support statement pooling,
     *         otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsStatementPooling() throws SQLException;

    /**
     * Determines whether stored procedure calls using the stored procedure
     * escape syntax is supported.
     *
     * @return {@code true} if stored procedure calls using the stored procedure
     *         escape syntax are supported, otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsStoredProcedures() throws SQLException;

    /**
     * Determines whether subqueries in comparison expressions are supported.
     *
     * @return {@code true} if subqueries are supported in comparison
     *         expressions.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsSubqueriesInComparisons() throws SQLException;

    /**
     * Determines whether subqueries in {@code EXISTS} expressions are supported.
     *
     * @return {@code true} if subqueries are supported in {@code EXISTS}
     *         expressions, otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsSubqueriesInExists() throws SQLException;

    /**
     * Determines whether subqueries in {@code IN} statements are supported.
     *
     * @return {@code true} if subqueries are supported in {@code IN} statements,
     *         otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsSubqueriesInIns() throws SQLException;

    /**
     * Determines whether subqueries in quantified expressions are supported.
     *
     * @return {@code true} if subqueries are supported, otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsSubqueriesInQuantifieds() throws SQLException;

    /**
     * Determines whether the database has table correlation names support.
     *
     * @return {@code true} if table correlation names are supported, otherwise
     *         {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsTableCorrelationNames() throws SQLException;

    /**
     * Determines whether a specified transaction isolation level is supported.
     *
     * @param level
     *            the transaction isolation level, as specified in {@code
     *            java.sql.Connection}: {@code TRANSACTION_NONE}, {@code
     *            TRANSACTION_READ_COMMITTED}, {@code
     *            TRANSACTION_READ_UNCOMMITTED}, {@code
     *            TRANSACTION_REPEATABLE_READ}, {@code TRANSACTION_SERIALIZABLE}
     * @return {@code true} if the specific isolation level is supported,
     *         otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsTransactionIsolationLevel(int level)
            throws SQLException;

    /**
     * Determines whether transactions are supported.
     * <p>
     * If transactions are not supported, then the {@code commit} method does
     * nothing and the transaction isolation level is always {@code
     * TRANSACTION_NONE}.
     *
     * @return {@code true} if transactions are supported, otherwise {@code
     *         false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsTransactions() throws SQLException;

    /**
     * Determines whether the {@code SQL UNION} operation is supported.
     *
     * @return {@code true} of the database does support {@code UNION}, otherwise
     *         {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsUnion() throws SQLException;

    /**
     * Determines whether the {@code SQL UNION ALL} operation is supported.
     *
     * @return {@code true} if the database does support {@code UNION ALL},
     *         otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean supportsUnionAll() throws SQLException;

    /**
     * Determines whether the method {@code ResultSet.rowUpdated} can detect a visible
     * row update for the specified {@code ResultSet} type.
     *
     * @param type
     *            {@code ResultSet} type: {@code ResultSet.TYPE_FORWARD_ONLY},
     *            {@code ResultSet.TYPE_SCROLL_INSENSITIVE}, or {@code
     *            ResultSet.TYPE_SCROLL_SENSITIVE}
     * @return {@code true} detecting changes is possible, otherwise {@code
     *         false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean updatesAreDetected(int type) throws SQLException;

    /**
     * Determines whether this database uses a file for each table.
     *
     * @return {@code true} if the database uses one file for each table,
     *         otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean usesLocalFilePerTable() throws SQLException;

    /**
     * Determines whether this database uses a local file to store tables.
     *
     * @return {@code true} if the database stores tables in a local file,
     *         otherwise {@code false}.
     * @throws SQLException
     *             a database error occurred.
     */
    public boolean usesLocalFiles() throws SQLException;

    /**
     * Determine if a SQLException while autoCommit is true indicates that all
     * open ResultSets are closed, even ones that are holdable
     *
     * @return true if all open ResultSets are closed
     * @throws SQLException
     *             if any error occurs
     */
    boolean autoCommitFailureClosesAllResultSets() throws SQLException;

    /**
     * Returns a list of the client info properties of the driver.
     *
     * @return a list of the client info
     * @throws SQLException
     *             if any error occurs
     */
    ResultSet getClientInfoProperties() throws SQLException;

    /**
     * Returns a description according to the given catalog's system or user
     * function parameters and return type.
     *
     * @param catalog
     *            the given catalong
     * @param schemaPattern
     *            the schema pattern
     * @param functionNamePattern
     *            the function name pattern
     * @param columnNamePattern
     *            the column name pattern
     * @return a description of user functions
     * @throws SQLException
     *             if any error occurs
     */
    ResultSet getFunctionColumns(String catalog, String schemaPattern,
            String functionNamePattern, String columnNamePattern)
            throws SQLException;

    /**
     * Returns a description of the system and user functions available
     * according to the given catalog.
     *
     * @param catalog
     *            the given catalog
     * @param schemaPattern
     *            the schema pattern
     * @param functionNamePattern
     *            the function name pattern
     * @return user functions
     * @throws SQLException
     *             if any error occurs
     */
    ResultSet getFunctions(String catalog, String schemaPattern,
            String functionNamePattern) throws SQLException;

    /**
     * Returns the lifetime for which a RowId object remains valid if this data
     * source supports the SQL ROWID type
     *
     * @return the time of a RowId object that remains valid.
     * @throws SQLException
     *             if any error occurs
     */
    RowIdLifetime getRowIdLifetime() throws SQLException;

    /**
     * Returns the schema names ordered by TABLE_CATALOG and TABLE_SCHEMA.
     *
     * @param catalog
     *            the catalog
     * @param schemaPattern
     *            the schema pattern
     * @return the schema names
     * @throws SQLException
     *             if any error occurs
     */
    ResultSet getSchemas(String catalog, String schemaPattern)
            throws SQLException;

    /**
     * Determine if this database supports invoking user-defined or vendor
     * functions using the stored procedure escape syntax.
     *
     * @return true if this database supports invoking user-defined or vendor
     *         functions using the stored procedure escape syntax.
     * @throws SQLException
     *             if any error occurs
     */
    boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException;
}
