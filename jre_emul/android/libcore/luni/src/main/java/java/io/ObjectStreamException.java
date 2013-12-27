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
 * Signals some sort of problem during either serialization or deserialization
 * of objects. This is actually the superclass of several other, more specific
 * exception classes.
 *
 * @see InvalidObjectException
 * @see NotActiveException
 * @see NotSerializableException
 * @see OptionalDataException
 * @see StreamCorruptedException
 * @see WriteAbortedException
 */
public abstract class ObjectStreamException extends IOException {

    private static final long serialVersionUID = 7260898174833392607L;

    /**
     * Constructs a new {@code ObjectStreamException} with its stack trace
     * filled in.
     */
    protected ObjectStreamException() {
    }

    /**
     * Constructs a new {@code ObjectStreamException} with its stack trace and
     * detail message filled in.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    protected ObjectStreamException(String detailMessage) {
        super(detailMessage);
    }
}
