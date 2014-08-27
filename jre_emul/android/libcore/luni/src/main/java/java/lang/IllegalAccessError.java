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
 * Thrown when the VM notices that a program tries access a field
 * which is not accessible from where it is referenced.
 * <p>
 * Note that this can only occur when inconsistent class files have been loaded.
 */
public class IllegalAccessError extends IncompatibleClassChangeError {

    private static final long serialVersionUID = -8988904074992417891L;

    /**
     * Constructs a new {@code IllegalAccessError} that includes the current
     * stack trace.
     */
    public IllegalAccessError() {
    }

    /**
     * Constructs a new {@code IllegalAccessError} with the current stack trace
     * and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this error.
     */
    public IllegalAccessError(String detailMessage) {
        super(detailMessage);
    }
}
