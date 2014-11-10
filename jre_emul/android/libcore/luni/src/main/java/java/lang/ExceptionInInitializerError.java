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

package java.lang;

/**
 * Thrown when an exception occurs during class initialization.
 */
public class ExceptionInInitializerError extends LinkageError {

    private static final long serialVersionUID = 1521711792217232256L;

    private Throwable exception;

    /**
     * Constructs a new {@code ExceptionInInitializerError} that includes the
     * current stack trace.
     */
    public ExceptionInInitializerError() {
        initCause(null);
    }

    /**
     * Constructs a new {@code ExceptionInInitializerError} with the current
     * stack trace and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this error.
     */
    public ExceptionInInitializerError(String detailMessage) {
        super(detailMessage);
        initCause(null);
    }

    /**
     * Constructs a new {@code ExceptionInInitializerError} with the current
     * stack trace and the specified cause. The exception should be the one
     * which originally occurred in the class initialization code.
     *
     * @param exception
     *            the exception that caused this error.
     */
    public ExceptionInInitializerError(Throwable exception) {
        this.exception = exception;
        initCause(exception);
    }

    /**
     * Returns the exception that is the cause of this error.
     *
     * @return the exception that caused this error.
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * Returns the cause of this error, or {@code null} if there is no cause.
     *
     * @return the exception that caused this error.
     */
    @Override
    public Throwable getCause() {
        return exception;
    }
}
