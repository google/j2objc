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
 * Thrown when the depth of the stack of the running program exceeds some
 * platform or VM specific limit. Typically, this will occur only
 * when a program becomes infinitely recursive, but it can also occur in
 * correctly written (but deeply recursive) programs.
 */
public class StackOverflowError extends VirtualMachineError {

    private static final long serialVersionUID = 8609175038441759607L;

    /**
     * Constructs a new {@code StackOverflowError} that includes the current
     * stack trace.
     */
    public StackOverflowError() {
    }

    /**
     * Constructs a new {@code StackOverflowError} with the current stack trace
     * and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public StackOverflowError(String detailMessage) {
        super(detailMessage);
    }
}
