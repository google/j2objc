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
 * Signals that an object that is not serializable has been passed into the
 * {@code ObjectOutput.writeObject()} method. This can happen if the object
 * does not implement {@code Serializable} or {@code Externalizable}, or if it
 * is serializable but it overrides {@code writeObject(ObjectOutputStream)} and
 * explicitly prevents serialization by throwing this type of exception.
 *
 * @see ObjectOutput#writeObject(Object)
 * @see ObjectOutputStream#writeObject(Object)
 */
public class NotSerializableException extends ObjectStreamException {

    private static final long serialVersionUID = 2906642554793891381L;

    /**
     * Constructs a new {@code NotSerializableException} with its stack trace
     * filled in.
     */
    public NotSerializableException() {
    }

    /**
     * Constructs a new {@link NotSerializableException} with its stack trace
     * and detail message filled in.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public NotSerializableException(String detailMessage) {
        super(detailMessage);
    }
}
