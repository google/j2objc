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

import java.util.HashMap;
import java.util.Map;

/**
 * An exception, which is subclass of SQLException, is thrown when one or more
 * client info properties could not be set on a Connection.
 */
public class SQLClientInfoException extends SQLException {
    private static final long serialVersionUID = -4319604256824655880L;

    final private Map<String, ClientInfoStatus> failedProperties;

    /**
     * Creates an SQLClientInfoException object. The Reason string is set to
     * null, the SQLState string is set to null and the Error Code is set to 0.
     */
    public SQLClientInfoException() {
        this.failedProperties = null;
    }

    /**
     * Creates an SQLClientInfoException object. The Reason string is set to the
     * given reason string, the SQLState string is set to null and the Error
     * Code is set to 0, and the Map<String,ClientInfoStatus> object is set to
     * the failed properties.
     *
     * @param failedProperties
     *            the Map<String,ClientInfoStatus> object to use as the
     *            property values
     */
    public SQLClientInfoException(Map<String, ClientInfoStatus> failedProperties) {
        this.failedProperties = new HashMap<String, ClientInfoStatus>(failedProperties);
    }

    /**
     * Creates an SQLClientInfoException object. The Reason string is set to the
     * null if cause == null or cause.toString() if cause!=null, the cause
     * Throwable object is set to the given cause Throwable object, and the Map<String,ClientInfoStatus>
     * object is set to the failed properties.
     *
     * @param failedProperties
     *            the Map<String,ClientInfoStatus> object to use as the
     *            property values
     * @param cause
     *            the Throwable object for the underlying reason this
     *            SQLException
     */
    public SQLClientInfoException(
            Map<String, ClientInfoStatus> failedProperties, Throwable cause) {
        super(cause);
        this.failedProperties = new HashMap<String, ClientInfoStatus>(failedProperties);
    }

    /**
     * Creates an SQLClientInfoException object. The Reason string is set to
     * reason, and the Map<String,ClientInfoStatus> object is set to the failed
     * properties.
     *
     * @param reason
     *            the string to use as the Reason string
     * @param failedProperties
     *            the Map<String,ClientInfoStatus> object to use as the
     *            property values
     */
    public SQLClientInfoException(String reason,
            Map<String, ClientInfoStatus> failedProperties) {
        super(reason);
        this.failedProperties = new HashMap<String, ClientInfoStatus>(failedProperties);
    }

    /**
     * Creates an SQLClientInfoException object. The Reason string is set to
     * reason, the cause Throwable object is set to the given cause Throwable
     * object, and the Map<String,ClientInfoStatus> object is set to the failed
     * properties.
     *
     * @param reason
     *            the string to use as the Reason string
     * @param failedProperties
     *            the Map<String,ClientInfoStatus> object to use as the
     *            property values
     * @param cause
     *            the Throwable object for the underlying reason this
     *            SQLException
     */
    public SQLClientInfoException(String reason,
            Map<String, ClientInfoStatus> failedProperties, Throwable cause) {
        super(reason, cause);
        this.failedProperties = new HashMap<String, ClientInfoStatus>(failedProperties);
    }

    /**
     * Creates an SQLClientInfoException object. The Reason string is set to
     * reason, the SQLState string is set to the sqlState, the Error Code is set
     * to the vendorCode and the Map<String,ClientInfoStatus> object is set to
     * the failed properties.
     *
     * @param reason
     *            the string to use as the Reason string
     * @param sqlState
     *            the string to use as the SQLState string
     * @param vendorCode
     *            the integer value for the error code
     * @param failedProperties
     *            the Map<String,ClientInfoStatus> object to use as the
     *            property values
     *
     */
    public SQLClientInfoException(String reason, String sqlState,
            int vendorCode, Map<String, ClientInfoStatus> failedProperties) {
        super(reason, sqlState, vendorCode);
        this.failedProperties = new HashMap<String, ClientInfoStatus>(failedProperties);
    }

    /**
     * Creates an SQLClientInfoException object. The Reason string is set to
     * reason, the SQLState string is set to the sqlState, the Error Code is set
     * to the vendorCode the cause Throwable object is set to the given cause
     * Throwable object, and the Map<String,ClientInfoStatus> object is set to
     * the failed properties.
     *
     * @param reason
     *            the string to use as the Reason string
     * @param sqlState
     *            the string to use as the SQLState string
     * @param vendorCode
     *            the integer value for the error code
     * @param failedProperties
     *            the Map<String,ClientInfoStatus> object to use as the
     *            property values
     * @param cause
     *            the Throwable object for the underlying reason this
     *            SQLException
     */
    public SQLClientInfoException(String reason, String sqlState,
            int vendorCode, Map<String, ClientInfoStatus> failedProperties,
            Throwable cause) {
        super(reason, sqlState, vendorCode, cause);
        this.failedProperties = new HashMap<String, ClientInfoStatus>(failedProperties);
    }

    /**
     * Creates an SQLClientInfoException object. The Reason string is set to
     * reason, the SQLState string is set to the sqlState, and the Map<String,ClientInfoStatus>
     * object is set to the failed properties.
     *
     * @param reason
     *            the string to use as the Reason string
     * @param sqlState
     *            the string to use as the SQLState string
     * @param failedProperties
     *            the Map<String,ClientInfoStatus> object to use as the
     *            property values
     */
    public SQLClientInfoException(String reason, String sqlState,
            Map<String, ClientInfoStatus> failedProperties) {
        super(reason, sqlState);
        this.failedProperties = new HashMap<String, ClientInfoStatus>(failedProperties);
    }

    /**
     * Creates an SQLClientInfoException object. The Reason string is set to
     * reason, the SQLState string is set to the sqlState, the Error Code is set
     * to the vendorCode, and the Map<String,ClientInfoStatus> object is set to
     * the failed properties.
     *
     * @param reason
     *            the string to use as the Reason string
     * @param sqlState
     *            the string to use as the SQLState string
     * @param failedProperties
     *            the Map<String,ClientInfoStatus> object to use as the
     *            property values
     * @param cause
     *            the Throwable object for the underlying reason this
     *            SQLException
     */
    public SQLClientInfoException(String reason, String sqlState,
            Map<String, ClientInfoStatus> failedProperties, Throwable cause) {
        super(reason, sqlState, cause);
        this.failedProperties = new HashMap<String, ClientInfoStatus>(failedProperties);
    }

    /**
     * returns that the client info properties which could not be set
     *
     * @return the list of ClientInfoStatus objects indicate client info
     *         properties
     */
    public Map<String, ClientInfoStatus> getFailedProperties() {
        return failedProperties;
    }
}