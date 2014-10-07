/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package java.util;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * An {@code InvalidPropertiesFormatException} is thrown if loading the XML
 * document defining the properties does not follow the {@code Properties}
 * specification.
 *
 * Even though this Exception inherits the {@code Serializable} interface, it is not
 * serializable. The methods used for serialization throw
 * {@code NotSerializableException}s.
 */
public class InvalidPropertiesFormatException extends IOException {

    private static final long serialVersionUID = 7763056076009360219L;

    /**
     * Constructs a new {@code InvalidPropertiesFormatException} with the
     * current stack trace and message filled in.
     *
     * @param m
     *           the detail message for the exception.
     */
    public InvalidPropertiesFormatException(String m) {
        super(m);
    }

    /**
     * Constructs a new {@code InvalidPropertiesFormatException} with the cause
     * for the Exception.
     *
     * @param c
     *           the cause for the Exception.
     */
    public InvalidPropertiesFormatException(Throwable c) {
        initCause(c);
    }

    private void writeObject(ObjectOutputStream out)
            throws NotSerializableException{
        throw new NotSerializableException();
    }

    private void readObject(ObjectInputStream in)
            throws NotSerializableException{
        throw new NotSerializableException();
    }
}
