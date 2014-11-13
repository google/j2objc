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

package java.util;

/**
 * An {@code ConcurrentModificationException} is thrown when a Collection is
 * modified and an existing iterator on the Collection is used to modify the
 * Collection as well.
 *
 * @see java.lang.RuntimeException
 */
public class ConcurrentModificationException extends RuntimeException {

    private static final long serialVersionUID = -3666751008965953603L;

    /**
     * Constructs a new {@code ConcurrentModificationException} with the current
     * stack trace filled in.
     */
    public ConcurrentModificationException() {
        /*empty*/
    }

    /**
     * Constructs a new {@code ConcurrentModificationException} with the current
     * stack trace and message filled in.
     *
     * @param detailMessage
     *           the detail message for the exception.
     */
    public ConcurrentModificationException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@code ConcurrentModificationException} with the given detail
     * message and cause.
     * @since 1.7
     */
    public ConcurrentModificationException(String detailMessage, Throwable cause) {
        super(detailMessage, cause);
    }

    /**
     * Constructs a new {@code ConcurrentModificationException} with the given cause.
     * @since 1.7
     */
    public ConcurrentModificationException(Throwable cause) {
        super(cause);
    }
}
