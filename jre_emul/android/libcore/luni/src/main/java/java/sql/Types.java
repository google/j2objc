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
 * A class which defines constants used to identify generic SQL types, also
 * called JDBC types. The type constant values are equivalent to those defined
 * by X/OPEN.
 */
public class Types {

    /*
     * Private constructor to prevent instantiation.
     */
    private Types() {
    }

    /**
     * The type code that identifies the SQL type {@code ARRAY}.
     */
    public static final int ARRAY = 2003;

    /**
     * The type code that identifies the SQL type {@code BIGINT}.
     */
    public static final int BIGINT = -5;

    /**
     * The type code that identifies the SQL type {@code BINARY}.
     */
    public static final int BINARY = -2;

    /**
     * The type code that identifies the SQL type {@code BIT}.
     */
    public static final int BIT = -7;

    /**
     * The type code that identifies the SQL type {@code BLOB}.
     */
    public static final int BLOB = 2004;

    /**
     * The type code that identifies the SQL type {@code BOOLEAN}.
     */
    public static final int BOOLEAN = 16;

    /**
     * The type code that identifies the SQL type {@code CHAR}.
     */
    public static final int CHAR = 1;

    /**
     * The type code that identifies the SQL type {@code CLOB}.
     */
    public static final int CLOB = 2005;

    /**
     * The type code that identifies the SQL type {@code DATALINK}.
     */
    public static final int DATALINK = 70;

    /**
     * The type code that identifies the SQL type {@code DATE}.
     */
    public static final int DATE = 91;

    /**
     * The type code that identifies the SQL type {@code DECIMAL}.
     */
    public static final int DECIMAL = 3;

    /**
     * The type code that identifies the SQL type {@code DISTINCT}.
     */
    public static final int DISTINCT = 2001;

    /**
     * The type code that identifies the SQL type {@code DOUBLE}.
     */
    public static final int DOUBLE = 8;

    /**
     * The type code that identifies the SQL type {@code FLOAT}.
     */
    public static final int FLOAT = 6;

    /**
     * The type code that identifies the SQL type {@code INTEGER}.
     */
    public static final int INTEGER = 4;

    /**
     * The type code that identifies the SQL type {@code JAVA_OBJECT}.
     */
    public static final int JAVA_OBJECT = 2000;

    /**
     * The type code that identifies the SQL type {@code LONGVARBINARY}.
     */
    public static final int LONGVARBINARY = -4;

    /**
     * The type code that identifies the SQL type {@code LONGVARCHAR}.
     */
    public static final int LONGVARCHAR = -1;

    /**
     * The type code that identifies the SQL type {@code NULL}.
     */
    public static final int NULL = 0;

    /**
     * The type code that identifies the SQL type {@code NUMERIC}.
     */
    public static final int NUMERIC = 2;

    /**
     * The type code that identifies that the SQL type is database specific and
     * is mapped to a Java object, accessed via the methods
     * {@code getObject} and {@code setObject}.
     */
    public static final int OTHER = 1111;

    /**
     * The type code that identifies the SQL type {@code REAL}.
     */
    public static final int REAL = 7;

    /**
     * The type code that identifies the SQL type {@code REF}.
     */
    public static final int REF = 2006;

    /**
     * The type code that identifies the SQL type {@code SMALLINT}.
     */
    public static final int SMALLINT = 5;

    /**
     * The type code that identifies the SQL type {@code STRUCT}.
     */
    public static final int STRUCT = 2002;

    /**
     * The type code that identifies the SQL type {@code TIME}.
     */
    public static final int TIME = 92;

    /**
     * The type code that identifies the SQL type {@code TIMESTAMP}.
     */
    public static final int TIMESTAMP = 93;

    /**
     * The type code that identifies the SQL type {@code TINYINT}.
     */
    public static final int TINYINT = -6;

    /**
     * The type code that identifies the SQL type {@code VARBINARY}.
     */
    public static final int VARBINARY = -3;

    /**
     * The type code that identifies the SQL type {@code VARCHAR}.
     */
    public static final int VARCHAR = 12;

    /**
     * The type code that identifies the SQL type ROWID.
     */
    public static final int ROWID = -8;

    /**
     * The type code that identifies the SQL type NCHAR.
     */
    public static final int NCHAR = -15;

    /**
     * The type code that identifies the SQL type NVARCHAR.
     */
    public static final int NVARCHAR = -9;

    /**
     * The type code that identifies the SQL type LONGNVARCHAR.
     */
    public static final int LONGNVARCHAR = -16;

    /**
     * The type code that identifies the SQL type NCLOB.
     */
    public static final int NCLOB = 2011;

    /**
     * The type code that identifies the SQL type SQLXML.
     */
    public static final int SQLXML = 2009;
}
