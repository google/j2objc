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

package java.io;

/**
 * Signals a general, I/O-related error. Error details may be specified when
 * calling the constructor, as usual. Note there are also several subclasses of
 * this class for more specific error situations, such as
 * {@link FileNotFoundException} or {@link EOFException}.
 */
public class IOException extends Exception {

    private static final long serialVersionUID = 7818375828146090155L;

    /**
     * Constructs a new {@code IOException} with its stack trace filled in.
     */
    public IOException() {
    }

    /**
     * Constructs a new {@code IOException} with its stack trace and detail
     * message filled in.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public IOException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new instance of this class with detail message and cause
     * filled in.
     *
     * @param message
     *            The detail message for the exception.
     * @param cause
     *            The detail cause for the exception.
     * @since 1.6
     */
    public IOException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new instance of this class with its detail cause filled in.
     *
     * @param cause
     *            The detail cause for the exception.
     * @since 1.6
     */
    public IOException(Throwable cause) {
        super(cause == null ? null : cause.toString(), cause);
    }
}
