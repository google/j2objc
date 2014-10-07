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

/**
 * This interface represents an SQL Ref - a data object containing a cursor
 * or pointer to a result table.
 * <p>
 * The data structure identified by an instance of Ref is held in the
 * database, so the data is not necessarily read and converted
 * into a Java object until {@code getObject} is called. However, if
 * the database supports the {@code Ref} type, it is not typically
 * necessary to get the underlying object before using it in a method call -
 * the {@code Ref} object can be used in place of the data structure.
 * <p>
 * A {@code Ref} object is stored into the database using the
 * {@link PreparedStatement#setRef(int, Ref)} method.
 */
public interface Ref {

    /**
     * Gets the fully-qualified SQL name of the SQL structured type that this
     * {@code Ref} references.
     *
     * @return the fully qualified name of the SQL structured type.
     * @throws SQLException
     *             if there is a database error.
     */
    public String getBaseTypeName() throws SQLException;

    /**
     * Gets the SQL structured type instance referenced by this {@code Ref}.
     *
     * @return a Java object whose type is defined by the mapping for the SQL
     *         structured type.
     * @throws SQLException
     *             if there is a database error.
     */
    public Object getObject() throws SQLException;

    /**
     * Returns the associated object and uses the relevant mapping to convert it
     * to a Java type.
     *
     * @param map
     *            the mapping for type conversion.
     * @return a Java object whose type is defined by the mapping for the SQL
     *         structured type.
     * @throws SQLException
     *             if there is a database error.
     */
    public Object getObject(Map<String, Class<?>> map) throws SQLException;

    /**
     * Sets the value of the structured type that this {@code Ref} references to
     * a supplied object.
     *
     * @param value
     *            the {@code Object} representing the new SQL structured type
     *            that this {@code Ref} references.
     * @throws SQLException
     *             if there is a database error.
     */
    public void setObject(Object value) throws SQLException;
}
