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
 * An interface which provides facilities for manipulating an SQL structured type
 * as a Java object. The {@code Struct} object has a value for each attribute of the SQL structured
 * type.
 */
public interface Struct {

    /**
     * Gets the SQL Type name of the SQL structured type that this {@code
     * Struct} represents.
     *
     * @return the fully qualified name of SQL structured type.
     * @throws SQLException
     *             if a database error occurs.
     */
    public String getSQLTypeName() throws SQLException;

    /**
     * Gets the values of the attributes of this SQL structured type. This
     * method uses the type map associated with the {@link Connection} for
     * customized type mappings. Where there is no entry in the type mapping
     * which matches this structured type, the JDBC driver uses the standard
     * mapping.
     *
     * @return an {@code Object} array containing the ordered attributes.
     * @throws SQLException
     *             if a database error occurs.
     */
    public Object[] getAttributes() throws SQLException;

    /**
     * Gets the values of the attributes of this SQL structured type. This
     * method uses the supplied type mapping to determine how to map SQL types
     * to their corresponding Java objects. In the
     * case where there is no entry in the type mapping which matches this
     * structured type, the JDBC driver uses the default mapping. The {@code
     * Connection} type map is <i>never</i> utilized by this method.
     *
     * @param theMap
     *            a Map describing how SQL Type names are mapped to classes.
     * @return an Object array containing the ordered attributes,.
     * @throws SQLException
     *             if a database error occurs.
     */
    public Object[] getAttributes(Map<String, Class<?>> theMap)
            throws SQLException;
}
