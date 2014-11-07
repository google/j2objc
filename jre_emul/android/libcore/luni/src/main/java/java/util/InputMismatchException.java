/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package java.util;

import java.io.Serializable;

/**
 * An {@code InputMismatchException} is thrown by a scanner to indicate that the
 * next token does not match or is out of range for the type specified in the
 * pattern.
 *
 * @see Scanner
 * @see java.lang.RuntimeException
 */
public class InputMismatchException extends NoSuchElementException implements
        Serializable {

    private static final long serialVersionUID = 8811230760997066428L;

    /**
     * Constructs a new {@code InputMismatchException} with the current stack
     * trace filled in.
     */
    public InputMismatchException() {
    }

    /**
     * Constructs a new {@code InputMismatchException} with the stack trace
     * filled in and {@code msg} as its error message.
     *
     * @param msg
     *           the specified error message.
     */
    public InputMismatchException(String msg) {
        super(msg);
    }
}
