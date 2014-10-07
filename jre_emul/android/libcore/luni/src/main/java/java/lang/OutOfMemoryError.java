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
 * Thrown when a request for memory is made that can not be satisfied using the
 * available platform resources. Such a request may be made by both the running
 * application or by an internal function of the VM.
 */
public class OutOfMemoryError extends VirtualMachineError {

    private static final long serialVersionUID = 8228564086184010517L;

    /**
     * Constructs a new {@code OutOfMemoryError} that includes the current stack
     * trace.
     */
    public OutOfMemoryError() {
    }

    /**
     * Constructs a new {@code OutOfMemoryError} with the current stack trace
     * and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this error.
     */
    public OutOfMemoryError(String detailMessage) {
        super(detailMessage);
    }
}
