/* Licensed to the Apache Software Foundation (ASF) under one or more
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

package java.net;

import java.io.IOException;

/**
 * If a HTTP request has to be retried, this exception will be thrown if the
 * request cannot be retried automatically.
 */
public class HttpRetryException extends IOException {

    private static final long serialVersionUID = -9186022286469111381L;

    private int responseCode;

    private String location = null;

    /**
     * Creates a new {@code HttpRetryException} instance with the specified
     * response code and the given detail message.
     *
     * @param detail
     *            the detail message for this exception.
     * @param code
     *            the HTTP response code from target host.
     */
    public HttpRetryException(String detail, int code) {
        super(detail);
        responseCode = code;
    }

    /**
     * Creates a new {@code HttpRetryException} instance with the specified
     * response code, the given detail message and the value of the location
     * field from the response header.
     *
     * @param detail
     *            the detail message for this exception.
     * @param code
     *            the HTTP response code from target host.
     * @param location
     *            the destination URL of the redirection.
     */
    public HttpRetryException(String detail, int code, String location) {
        super(detail);
        responseCode = code;
        this.location = location;
    }

    /**
     * Gets the location value.
     *
     * @return the stored location from the HTTP header.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets the detail message.
     *
     * @return the detail message.
     */
    public String getReason() {
        return getMessage();
    }

    /**
     * Gets the response code.
     *
     * @return the HTTP response code.
     */
    public int responseCode() {
        return responseCode;
    }
}
