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

/**
 * A {@code ConnectException} is thrown if a connection cannot be established to
 * a remote host on a specific port.
 *
 * <p>Most applications <strong>should not</strong> catch this exception; it is
 * more robust to catch the superclass {@code SocketException}.
 */
public class ConnectException extends SocketException {

    private static final long serialVersionUID = 3831404271622369215L;

    /**
     * Constructs a new instance.
     */
    public ConnectException() {
    }

    /**
     * Constructs a new instance with the given detail message.
     */
    public ConnectException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new instance with the given detail message and cause.
     * @hide
     */
    public ConnectException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
    }
}
