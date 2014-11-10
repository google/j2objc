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
 * {@code Error} is the superclass of all classes that represent unrecoverable
 * errors. When errors are thrown, they should not be caught by application
 * code.
 *
 * @see Throwable
 * @see Exception
 * @see RuntimeException
 */
public class Error extends Throwable {

    private static final long serialVersionUID = 4980196508277280342L;

    /**
     * Constructs a new {@code Error} that includes the current stack trace.
     */
    public Error() {
    }

    /**
     * Constructs a new {@code Error} with the current stack trace and the
     * specified detail message.
     *
     * @param detailMessage
     *            the detail message for this error.
     */
    public Error(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@code Error} with the current stack trace, the
     * specified detail message and the specified cause.
     *
     * @param detailMessage
     *            the detail message for this error.
     * @param throwable
     *            the cause of this error.
     */
    public Error(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Constructs a new {@code Error} with the current stack trace and the
     * specified cause.
     *
     * @param throwable
     *            the cause of this error.
     */
    public Error(Throwable throwable) {
        super(throwable);
    }
}
