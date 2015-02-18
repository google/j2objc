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
 * {@code KeyStoreException} is a general {@code KeyStore} exception.
 *
 * @see KeyStore
 */
public class KeyStoreException extends GeneralSecurityException {

    private static final long serialVersionUID = -1119353179322377262L;

    /**
     * Constructs a new instance of {@code KeyStoreException} with the
     * given message.
     *
     * @param msg
     *            the detail message for this exception.
     */
    public KeyStoreException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new instance of {@code KeyStoreException}.
     */
    public KeyStoreException() {
    }

    /**
     * Constructs a new instance of {@code KeyStoreException} with the
     * given message and the cause.
     *
     * @param message
     *            the detail message for this exception.
     * @param cause
     *            the exception which is the cause for this exception.
     */
    public KeyStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new instance of {@code KeyStoreException} with the
     * cause.
     *
     * @param cause
     *            the exception which is the cause for this exception.
     */
    public KeyStoreException(Throwable cause) {
        super(cause);
    }
}
