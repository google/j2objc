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
 * This {@code SocketException} may be thrown during socket creation or setting
 * options, and is the superclass of all other socket related exceptions.
 */
public class SocketException extends IOException {

    private static final long serialVersionUID = -5935874303556886934L;

    /**
     * Constructs a new instance.
     */
    public SocketException() {
    }

    /**
     * Constructs a new instance with the given detail message.
     */
    public SocketException(String detailMessage) {
        super(detailMessage);
    }
    /**
     * Constructs a new instance with the given cause.
     * @hide internal use only
     */
    public SocketException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new instance with given detail message and cause.
     * @hide internal use only
     */
    public SocketException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
    }
}
