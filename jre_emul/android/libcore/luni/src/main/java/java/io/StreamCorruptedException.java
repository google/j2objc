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
 * Signals that the {@link ObjectInputStream#readObject()} method could not
 * read an object due to missing information (for example, a cyclic reference
 * that doesn't match a previous instance, or a missing class descriptor for the
 * object to be loaded).
 *
 * @see ObjectInputStream
 * @see OptionalDataException
 */
public class StreamCorruptedException extends ObjectStreamException {

    private static final long serialVersionUID = 8983558202217591746L;

    /**
     * Constructs a new {@code StreamCorruptedException} with its stack trace
     * filled in.
     */
    public StreamCorruptedException() {
    }

    /**
     * Constructs a new {@code StreamCorruptedException} with its stack trace
     * and detail message filled in.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public StreamCorruptedException(String detailMessage) {
        super(detailMessage);
    }
}
