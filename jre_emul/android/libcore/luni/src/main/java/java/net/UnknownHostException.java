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
 * Thrown when a hostname can not be resolved.
 */
public class UnknownHostException extends IOException {

    private static final long serialVersionUID = -4639126076052875403L;

    /**
     * Constructs a new {@code UnknownHostException} instance with no detail message.
     * Callers should usually supply a detail message.
     */
    public UnknownHostException() {
    }

    /**
     * Constructs a new {@code UnknownHostException} instance with the given detail message.
     * The detail message should generally contain the hostname and a reason for the failure,
     * if known.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public UnknownHostException(String detailMessage) {
        super(detailMessage);
    }
}
