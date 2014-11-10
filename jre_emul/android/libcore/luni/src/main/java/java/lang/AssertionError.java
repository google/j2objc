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
 * Thrown when an assertion has failed.
 *
 * @since 1.4
 */
public class AssertionError extends Error {

    private static final long serialVersionUID = -5013299493970297370L;

    /**
     * Constructs a new {@code AssertionError} with no message.
     */
    public AssertionError() {
    }

    /**
     * Constructs a new {@code AssertionError} with the given detail message and cause.
     * @since 1.7
     */
    public AssertionError(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
    }

    /**
     * Constructs a new {@code AssertionError} with a message based on calling
     * {@link String#valueOf(Object)} with the specified object. If the object
     * is an instance of {@link Throwable}, then it also becomes the cause of
     * this error.
     *
     * @param detailMessage
     *            the object to be converted into the detail message and
     *            optionally the cause.
     */
    public AssertionError(Object detailMessage) {
        super(String.valueOf(detailMessage));
        if (detailMessage instanceof Throwable) {
            initCause((Throwable) detailMessage);
        }
    }

    /**
     * Constructs a new {@code AssertionError} with a message based on calling
     * {@link String#valueOf(boolean)} with the specified boolean value.
     *
     * @param detailMessage
     *            the value to be converted into the message.
     */
    public AssertionError(boolean detailMessage) {
        this(String.valueOf(detailMessage));
    }

    /**
     * Constructs a new {@code AssertionError} with a message based on calling
     * {@link String#valueOf(char)} with the specified character value.
     *
     * @param detailMessage
     *            the value to be converted into the message.
     */
    public AssertionError(char detailMessage) {
        this(String.valueOf(detailMessage));
    }

    /**
     * Constructs a new {@code AssertionError} with a message based on calling
     * {@link String#valueOf(int)} with the specified integer value.
     *
     * @param detailMessage
     *            the value to be converted into the message.
     */
    public AssertionError(int detailMessage) {
        this(Integer.toString(detailMessage));
    }

    /**
     * Constructs a new {@code AssertionError} with a message based on calling
     * {@link String#valueOf(long)} with the specified long value.
     *
     * @param detailMessage
     *            the value to be converted into the message.
     */
    public AssertionError(long detailMessage) {
        this(Long.toString(detailMessage));
    }

    /**
     * Constructs a new {@code AssertionError} with a message based on calling
     * {@link String#valueOf(float)} with the specified float value.
     *
     * @param detailMessage
     *            the value to be converted into the message.
     */
    public AssertionError(float detailMessage) {
        this(Float.toString(detailMessage));
    }

    /**
     * Constructs a new {@code AssertionError} with a message based on calling
     * {@link String#valueOf(double)} with the specified double value.
     *
     * @param detailMessage
     *            the value to be converted into the message.
     */
    public AssertionError(double detailMessage) {
        this(Double.toString(detailMessage));
    }
}
