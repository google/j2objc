/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package java.lang.annotation;

/**
 * Indicates that an annotation in the binary representation of a class is
 * syntactically incorrect and the annotation parser is unable to process it.
 * This exception is unlikely to ever occur, given that the code has been
 * compiled by an ordinary Java compiler.
 *
 * @since 1.5
 */
public class AnnotationFormatError extends Error {

    private static final long serialVersionUID = -4256701562333669892L;

    /**
     * Constructs an instance with the message provided.
     *
     * @param message
     *            the details of the error.
     */
    public AnnotationFormatError(String message) {
        super(message);
    }

    /**
     * Constructs an instance with a message and a cause.
     *
     * @param message
     *            the details of the error.
     * @param cause
     *            the cause of the error or {@code null} if none.
     */
    public AnnotationFormatError(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an instance with a cause. If the cause is not
     * {@code null}, then {@code cause.toString()} is used as the
     * error's message.
     *
     * @param cause
     *            the cause of the error or {@code null} if none.
     */
    public AnnotationFormatError(Throwable cause) {
        super(cause == null ? null : cause.toString(), cause);
    }

}
