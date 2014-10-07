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
 * This class provides a wrapper for an exception thrown by a {@code Method} or
 * {@code Constructor} invocation.
 *
 * @see Method#invoke
 * @see Constructor#newInstance
 */
public class InvocationTargetException extends ReflectiveOperationException {

    private static final long serialVersionUID = 4085088731926701167L;

    private Throwable target;

    /**
     * Constructs a new {@code InvocationTargetException} instance with a
     * {@code null} cause / target exception.
     */
    protected InvocationTargetException() {
        super((Throwable) null);
    }

    /**
     * Constructs a new {@code InvocationTargetException} instance with its
     * cause / target exception filled in.
     *
     * @param exception
     *            the exception which occurred while running the Method or
     *            Constructor
     */
    public InvocationTargetException(Throwable exception) {
        super(null, exception);
        target = exception;
    }

    /**
     * Constructs a new {@code InvocationTargetException} instance with its
     * cause / target exception and message filled in.
     *
     * @param detailMessage
     *            the detail message for the exception
     * @param exception
     *            the exception which occurred while running the Method or
     *            Constructor
     */
    public InvocationTargetException(Throwable exception, String detailMessage) {
        super(detailMessage, exception);
        target = exception;
    }

    /**
     * Returns the target exception, which may be {@code null}.
     *
     * @return the target exception
     */
    public Throwable getTargetException() {
        return target;
    }

    /**
     * Returns the cause of this exception, which may be {@code null}.
     *
     * @return the cause of this exception
     */
    @Override
    public Throwable getCause() {
        return target;
    }
}
