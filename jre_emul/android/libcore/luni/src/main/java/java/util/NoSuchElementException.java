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
 * Thrown when trying to retrieve an element
 * past the end of an Enumeration or Iterator.
 */
public class NoSuchElementException extends RuntimeException {

    private static final long serialVersionUID = 6769829250639411880L;

    /**
     * Constructs a new {@code NoSuchElementException} with the current stack
     * trace filled in.
     */
    public NoSuchElementException() {
    }

    /**
     * Constructs a new {@code NoSuchElementException} with the current stack
     * trace and message filled in.
     *
     * @param detailMessage
     *           the detail message for the exception.
     */
    public NoSuchElementException(String detailMessage) {
        super(detailMessage);
    }

}
