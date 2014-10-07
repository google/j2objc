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
 * An interface used to get information about the types and properties of
 * parameters in a {@code PreparedStatement}.
 */
public interface ParameterMetaData extends Wrapper {

    /**
     * Indicates that the parameter mode is {@code IN}.
     */
    public static final int parameterModeIn = 1;

    /**
     * Indicates that the parameter mode is {@code INOUT}.
     */
    public static final int parameterModeInOut = 2;

    /**
     * Indicates that the parameter mode is {@code OUT}.
     */
    public static final int parameterModeOut = 4;

    /**
     * Indicates that the parameter mode is not known.
     */
    public static final int parameterModeUnknown = 0;

    /**
     * Indicates that a parameter is not permitted to be {@code NULL}.
     */
    public static final int parameterNoNulls = 0;

    /**
     * Indicates that a parameter is permitted to be {@code NULL}.
     */
    public static final int parameterNullable = 1;

    /**
     * Indicates that whether a parameter is allowed to be {@code null} or not
     * is not known.
     */
    public static final int parameterNullableUnknown = 2;

    /**
     * Gets the fully-qualified name of the Java class which should be passed as
     * a parameter to the method {@code PreparedStatement.setObject}.
     *
     * @param paramIndex
     *            the index number of the parameter, where the first parameter
     *            has index 1.
     * @return the fully qualified Java class name of the parameter with the
     *         specified index. This class name is used for custom mapping
     *         between SQL types and Java objects.
     * @throws SQLException
     *             if a database error happens.
     */
    public String getParameterClassName(int paramIndex) throws SQLException;

    /**
     * Gets the number of parameters in the {@code PreparedStatement} for which
     * this {@code ParameterMetaData} contains information.
     *
     * @return the number of parameters.
     * @throws SQLException
     *             if a database error happens.
     */
    public int getParameterCount() throws SQLException;

    /**
     * Gets the mode of the specified parameter. Can be one of:
     * <ul>
     * <li>ParameterMetaData.parameterModeIn</li>
     * <li>ParameterMetaData.parameterModeOut</li>
     * <li>ParameterMetaData.parameterModeInOut</li>
     * <li>ParameterMetaData.parameterModeUnknown</li>
     * </ul>
     *
     * @param paramIndex
     *            the index number of the parameter, where the first parameter
     *            has index 1.
     * @return the parameter's mode.
     * @throws SQLException
     *             if a database error happens.
     */
    public int getParameterMode(int paramIndex) throws SQLException;

    /**
     * Gets the SQL type of a specified parameter.
     *
     * @param paramIndex
     *            the index number of the parameter, where the first parameter
     *            has index 1.
     * @return the SQL type of the parameter as defined in {@code
     *         java.sql.Types}.
     * @throws SQLException
     *             if a database error happens.
     */
    public int getParameterType(int paramIndex) throws SQLException;

    /**
     * Gets the database-specific type name of a specified parameter.
     *
     * @param paramIndex
     *            the index number of the parameter, where the first parameter
     *            has index 1.
     * @return the type name for the parameter as used by the database. A
     *         fully-qualified name is returned if the parameter is a <i>User
     *         Defined Type</i> (UDT).
     * @throws SQLException
     *             if a database error happens.
     */
    public String getParameterTypeName(int paramIndex) throws SQLException;

    /**
     * Gets the number of decimal digits for a specified parameter.
     *
     * @param paramIndex
     *            the index number of the parameter, where the first parameter
     *            has index 1.
     * @return the number of decimal digits ("the precision") for the parameter.
     *         {@code 0} if the parameter is not a numeric type.
     * @throws SQLException
     *             if a database error happens.
     */
    public int getPrecision(int paramIndex) throws SQLException;

    /**
     * Gets the number of digits after the decimal point for a specified
     * parameter.
     *
     * @param paramIndex
     *            the index number of the parameter, where the first parameter
     *            has index 1.
     * @return the number of digits after the decimal point ("the scale") for
     *         the parameter. {@code 0} if the parameter is not a numeric type.
     * @throws SQLException
     *             if a database error happens.
     */
    public int getScale(int paramIndex) throws SQLException;

    /**
     * Gets whether {@code null} values are allowed for the specified parameter.
     * The returned value is one of:
     * <ul>
     * <li>ParameterMetaData.parameterNoNulls</li>
     * <li>ParameterMetaData.parameterNullable</li>
     * <li>ParameterMetaData.parameterNullableUnknown</li>
     * </ul>
     *
     * @param paramIndex
     *            the index number of the parameter, where the first parameter
     *            has index 1.
     * @return the int code indicating the nullability of the parameter.
     * @throws SQLException
     *             if a database error is encountered.
     */
    public int isNullable(int paramIndex) throws SQLException;

    /**
     * Gets whether values for the specified parameter can be signed numbers.
     *
     * @param paramIndex
     *            the index number of the parameter, where the first parameter
     *            has index 1.
     * @return {@code true} if values can be signed numbers for this parameter,
     *         {@code false} otherwise.
     * @throws SQLException
     *             if a database error happens.
     */
    public boolean isSigned(int paramIndex) throws SQLException;
}
