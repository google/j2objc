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

package java.lang.reflect;

/**
 * This class provides a wrapper for an undeclared, checked exception thrown by
 * an InvocationHandler.
 *
 * @see java.lang.reflect.InvocationHandler#invoke
 */
public class UndeclaredThrowableException extends RuntimeException {

    private static final long serialVersionUID = 330127114055056639L;

    private Throwable undeclaredThrowable;

    /**
     * Constructs a new {@code UndeclaredThrowableException} instance with the
     * undeclared, checked exception that occurred.
     *
     * @param exception
     *            the undeclared, checked exception that occurred
     */
    public UndeclaredThrowableException(Throwable exception) {
        this.undeclaredThrowable = exception;
        initCause(exception);
    }

    /**
     * Constructs a new {@code UndeclaredThrowableException} instance with the
     * undeclared, checked exception that occurred and a message.
     *
     * @param detailMessage
     *            the detail message for the exception
     * @param exception
     *            the undeclared, checked exception that occurred
     */
    public UndeclaredThrowableException(Throwable exception,
            String detailMessage) {
        super(detailMessage);
        this.undeclaredThrowable = exception;
        initCause(exception);
    }

    /**
     * Returns the undeclared, checked exception that occurred, which may be
     * {@code null}.
     *
     * @return the undeclared, checked exception that occurred
     */
    public Throwable getUndeclaredThrowable() {
        return undeclaredThrowable;
    }

    /**
     * Returns the undeclared, checked exception that occurred, which may be
     * {@code null}.
     *
     * @return the undeclared, checked exception that occurred
     */
    @Override
    public Throwable getCause() {
        return undeclaredThrowable;
    }
}
