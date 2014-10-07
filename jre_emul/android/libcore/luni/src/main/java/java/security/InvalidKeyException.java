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

package java.security;

/**
 * {@code InvalidKeyException} indicates exceptional conditions, caused by an
 * invalid key.
 */
public class InvalidKeyException extends KeyException {

    private static final long serialVersionUID = 5698479920593359816L;

    /**
     * Constructs a new instance of {@code InvalidKeyException} with the given
     * message.
     *
     * @param msg
     *            the detail message for this exception.
     */
    public InvalidKeyException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new instance of {@code InvalidKeyException}.
     */
    public InvalidKeyException() {
    }

    /**
     * Constructs a new instance of {@code InvalidKeyException} with the given
     * message and the cause.
     *
     * @param message
     *            the detail message for this exception.
     * @param cause
     *            the exception which is the cause for this exception.
     */
    public InvalidKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new instance of {@code InvalidKeyException} with the cause.
     *
     * @param cause
     *            the exception which is the cause for this exception.
     */
    public InvalidKeyException(Throwable cause) {
        super(cause);
    }
}
