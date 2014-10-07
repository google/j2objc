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
 * An exception, which is subclass of SQLException, is thrown when the
 * connection operation that failed will not succeed when the operation is
 * retried without the cause of the failure being corrected.
 */
public class SQLNonTransientConnectionException extends
        SQLNonTransientException {

    private static final long serialVersionUID = -5852318857474782892L;

    /**
     * Creates an SQLNonTransientConnectionException object. The Reason string
     * is set to null, the SQLState string is set to null and the Error Code is
     * set to 0.
     */
    public SQLNonTransientConnectionException() {
    }

    /**
     * Creates an SQLNonTransientConnectionException object. The Reason string
     * is set to the given reason string, the SQLState string is set to null and
     * the Error Code is set to 0.
     *
     * @param reason
     *            the string to use as the Reason string
     */
    public SQLNonTransientConnectionException(String reason) {
        super(reason, null, 0);
    }

    /**
     * Creates an SQLNonTransientConnectionException object. The Reason string
     * is set to the given reason string, the SQLState string is set to the
     * given SQLState string and the Error Code is set to 0.
     *
     * @param reason
     *            the string to use as the Reason string
     * @param sqlState
     *            the string to use as the SQLState string
     */
    public SQLNonTransientConnectionException(String reason, String sqlState) {
        super(reason, sqlState, 0);
    }

    /**
     * Creates an SQLNonTransientConnectionException object. The Reason string
     * is set to the given reason string, the SQLState string is set to the
     * given SQLState string and the Error Code is set to the given error code
     * value.
     *
     * @param reason
     *            the string to use as the Reason string
     * @param sqlState
     *            the string to use as the SQLState string
     * @param vendorCode
     *            the integer value for the error code
     */
    public SQLNonTransientConnectionException(String reason, String sqlState,
            int vendorCode) {
        super(reason, sqlState, vendorCode);
    }

    /**
     * Creates an SQLNonTransientConnectionException object. The Reason string
     * is set to the null if cause == null or cause.toString() if
     * cause!=null,and the cause Throwable object is set to the given cause
     * Throwable object.
     *
     * @param cause
     *            the Throwable object for the underlying reason this
     *            SQLException
     */
    public SQLNonTransientConnectionException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates an SQLNonTransientConnectionException object. The Reason string
     * is set to the given and the cause Throwable object is set to the given
     * cause Throwable object.
     *
     * @param reason
     *            the string to use as the Reason string
     * @param cause
     *            the Throwable object for the underlying reason this
     *            SQLException
     */
    public SQLNonTransientConnectionException(String reason, Throwable cause) {
        super(reason, cause);
    }

    /**
     * Creates an SQLNonTransientConnectionException object. The Reason string
     * is set to the given reason string, the SQLState string is set to the
     * given SQLState string and the cause Throwable object is set to the given
     * cause Throwable object.
     *
     * @param reason
     *            the string to use as the Reason string
     * @param sqlState
     *            the string to use as the SQLState string
     * @param cause
     *            the Throwable object for the underlying reason this
     *            SQLException
     */
    public SQLNonTransientConnectionException(String reason, String sqlState,
            Throwable cause) {
        super(reason, sqlState, cause);
    }

    /**
     * Creates an SQLNonTransientConnectionException object. The Reason string
     * is set to the given reason string, the SQLState string is set to the
     * given SQLState string , the Error Code is set to the given error code
     * value, and the cause Throwable object is set to the given cause Throwable
     * object.
     *
     * @param reason
     *            the string to use as the Reason string
     * @param sqlState
     *            the string to use as the SQLState string
     * @param vendorCode
     *            the integer value for the error code
     * @param cause
     *            the Throwable object for the underlying reason this
     *            SQLException
     */
    public SQLNonTransientConnectionException(String reason, String sqlState,
            int vendorCode, Throwable cause) {
        super(reason, sqlState, vendorCode, cause);
    }
}
