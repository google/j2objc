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
 * Signals that a serialization-related method has been invoked in the wrong
 * place. Some methods in {@code ObjectInputStream} and {@code
 * ObjectOutputStream} can only be called from a nested call to readObject() or
 * writeObject(). Any attempt to call them from another context will cause a
 * {@code NotActiveException} to be thrown. The list of methods that are
 * protected this way is:
 * <ul>
 * <li>{@link ObjectInputStream#defaultReadObject()}</li>
 * <li>{@link ObjectInputStream#registerValidation(ObjectInputValidation, int)}</li>
 * <li>{@link ObjectOutputStream#defaultWriteObject()}</li>
 * </ul>
 */
public class NotActiveException extends ObjectStreamException {

    private static final long serialVersionUID = -3893467273049808895L;

    /**
     * Constructs a new {@code NotActiveException} with its stack trace filled
     * in.
     */
    public NotActiveException() {
    }

    /**
     * Constructs a new {@code NotActiveException} with its stack trace and
     * detail message filled in.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public NotActiveException(String detailMessage) {
        super(detailMessage);
    }
}
