/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.net;

import java.io.IOException;

/**
 * Is thrown if no appropriate {@code ContentHandler} could be found for a
 * particular service requested by the URL connection. This could be happened if
 * there is an invalid MIME type or the application wants to send data over a
 * read-only connection.
 */
public class UnknownServiceException extends IOException {

    private static final long serialVersionUID = -4169033248853639508L;

    /**
     * Constructs a new instance.
     */
    public UnknownServiceException() {
    }

    /**
     * Constructs a new instance with the given detail message.
     */
    public UnknownServiceException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new instance with given detail message and cause.
     * @hide internal use only
     */
    public UnknownServiceException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
    }
}
