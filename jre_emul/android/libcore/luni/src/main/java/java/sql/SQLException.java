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

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An exception that indicates a failed JDBC operation.
 * It provides the following information about problems encountered with
 * database access:
 * <ul>
 *   <li>A message string.</li>
 *   <li>A {@code SQLState} error description string following either
 * <a href="http://en.wikipedia.org/wiki/SQL:1999">SQL 99</a> or X/OPEN {@code SQLState}
 * conventions. {@link DatabaseMetaData#getSQLStateType} exposes the specific convention in
 * use.</li>
 *   <li>A database-specific error code.</li>
 *   <li>The next exception in the chain.</li>
 * </ul>
 *
 * @see DatabaseMetaData
 */
public class SQLException extends Exception implements Serializable, Iterable<Throwable> {

    private static final long serialVersionUID = 2135244094396331484L;

    private String SQLState = null;

    private int vendorCode = 0;

    private SQLException next = null;

    /**
     * Creates an {@code SQLException} object. The reason string is set to
     * {@code null}, the {@code SQLState} string is set to {@code null} and the
     * error code is set to 0.
     */
    public SQLException() {
    }

    /**
     * Creates an {@code SQLException} object. The reason string is set to the given
     * reason string, the {@code SQLState} string is set to {@code null} and the error code is
     * set to 0.
     *
     * @param theReason
     *            the string to use as the Reason string
     */
    public SQLException(String theReason) {
        this(theReason, null, 0);
    }

    /**
     * Creates an {@code SQLException} object. The reason string is set to the
     * given reason string, the {@code SQLState} string is set to the given
     * {@code SQLState} string and the error code is set to 0.
     *
     * @param theReason
     *            the string to use as the reason string.
     * @param theSQLState
     *            the string to use as the {@code SQLState} string.
     */
    public SQLException(String theReason, String theSQLState) {
        this(theReason, theSQLState, 0);
    }

    /**
     * Creates an {@code SQLException} object. The reason string is set to the
     * given reason string, the {@code SQLState} string is set to the given
     * {@code SQLState} string and the error code is set to the given error code
     * value.
     *
     * @param theReason
     *            the string to use as the reason string.
     * @param theSQLState
     *            the string to use as the {@code SQLState} string.
     * @param theErrorCode
     *            the integer value for the error code.
     */
    public SQLException(String theReason, String theSQLState, int theErrorCode) {
        super(theReason);
        SQLState = theSQLState;
        vendorCode = theErrorCode;
    }

    /**
     * Creates an SQLException object. The Reason string is set to the null if
     * cause == null or cause.toString() if cause!=null,and the cause Throwable
     * object is set to the given cause Throwable object.
     *
     * @param theCause
     *            the Throwable object for the underlying reason this
     *            SQLException
     *
     * @since 1.6
     */
    public SQLException(Throwable theCause) {
        this(theCause == null ? null : theCause.toString(), null, 0, theCause);
    }

    /**
     * Creates an SQLException object. The Reason string is set to the given and
     * the cause Throwable object is set to the given cause Throwable object.
     *
     * @param theReason
     *            the string to use as the Reason string
     * @param theCause
     *            the Throwable object for the underlying reason this
     *            SQLException
     *
     * @since 1.6
     */
    public SQLException(String theReason, Throwable theCause) {
        super(theReason, theCause);
    }

    /**
     * Creates an SQLException object. The Reason string is set to the given
     * reason string, the SQLState string is set to the given SQLState string
     * and the cause Throwable object is set to the given cause Throwable
     * object.
     *
     * @param theReason
     *            the string to use as the Reason string
     * @param theSQLState
     *            the string to use as the SQLState string
     * @param theCause
     *            the Throwable object for the underlying reason this
     *            SQLException
     * @since 1.6
     */
    public SQLException(String theReason, String theSQLState, Throwable theCause) {
        super(theReason, theCause);
        SQLState = theSQLState;
    }

    /**
     * Creates an SQLException object. The Reason string is set to the given
     * reason string, the SQLState string is set to the given SQLState string ,
     * the Error Code is set to the given error code value, and the cause
     * Throwable object is set to the given cause Throwable object.
     *
     * @param theReason
     *            the string to use as the Reason string
     * @param theSQLState
     *            the string to use as the SQLState string
     * @param theErrorCode
     *            the integer value for the error code
     * @param theCause
     *            the Throwable object for the underlying reason this
     *            SQLException
     * @since 1.6
     */
    public SQLException(String theReason, String theSQLState, int theErrorCode,
            Throwable theCause) {
        this(theReason, theSQLState, theCause);
        vendorCode = theErrorCode;
    }

    /**
     * Returns the integer error code for this {@code SQLException}.
     *
     * @return The integer error code for this {@code SQLException}. The meaning
     *         of the code is specific to the vendor of the database.
     */
    public int getErrorCode() {
        return vendorCode;
    }

    /**
     * Retrieves the {@code SQLException} chained to this {@code SQLException},
     * if any.
     *
     * @return The {@code SQLException} chained to this {@code SQLException}.
     *         {@code null} if there is no {@code SQLException} chained to this
     *         {@code SQLException}.
     */
    public SQLException getNextException() {
        return next;
    }

    /**
     * Retrieves the {@code SQLState} description string for this {@code
     * SQLException} object.
     *
     * @return The {@code SQLState} string for this {@code SQLException} object.
     *         This is an error description string which follows either the SQL
     *         99 conventions or the X/OPEN {@code SQLstate} conventions. The
     *         potential values of the {@code SQLState} string are described in
     *         each of the specifications. Which of the conventions is being
     *         used by the {@code SQLState} string can be discovered by using
     *         the {@code getSQLStateType} method of the {@code
     *         DatabaseMetaData} interface.
     */
    public String getSQLState() {
        return SQLState;
    }

    /**
     * Obsolete. Appends {@code ex} to the end of this chain.
     */
    public void setNextException(SQLException ex) {
        if (next != null) {
            next.setNextException(ex);
        } else {
            next = ex;
        }
    }

    /**
     * Obsolete. {@link #getCause()} should be used instead of this iterator. Returns an iterator
     * over the exceptions added with {@link #setNextException}.
     */
    public Iterator<Throwable> iterator() {
        return new InternalIterator(this);
    }

    private static class InternalIterator implements Iterator<Throwable> {

        private SQLException current;

        InternalIterator(SQLException e) {
            current = e;
        }

        public boolean hasNext() {
            return current != null;
        }

        public Throwable next() {
            if (current == null) {
                throw new NoSuchElementException();
            }
            SQLException ret = current;
            current = current.next;
            return ret;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
