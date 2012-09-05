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
 * An {@code EmptyStackException} is thrown if the pop/peek method of a stack is
 * executed on an empty stack.
 *
 * @see java.lang.RuntimeException
 */
public class EmptyStackException extends RuntimeException {

    private static final long serialVersionUID = 5084686378493302095L;

    /**
     * Constructs a new {@code EmptyStackException} with the stack trace filled
     * in.
     */
    public EmptyStackException() {
        super();
    }
}
