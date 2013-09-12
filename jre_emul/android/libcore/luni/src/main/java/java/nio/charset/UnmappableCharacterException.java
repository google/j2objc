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

package java.nio.charset;

/**
 * An {@code UnmappableCharacterException} is thrown when an unmappable
 * character for the given charset is encountered.
 */
public class UnmappableCharacterException extends CharacterCodingException {

    /*
     * This constant is used during deserialization to check the version
     * which created the serialized object.
     */
    private static final long serialVersionUID = -7026962371537706123L;

    // The length of the unmappable character
    private int inputLength;

    /**
     * Constructs a new {@code UnmappableCharacterException}.
     *
     * @param length
     *            the length of the unmappable character.
     */
    public UnmappableCharacterException(int length) {
        this.inputLength = length;
    }

    /**
     * Returns the length of the unmappable character.
     */
    public int getInputLength() {
        return this.inputLength;
    }

    @Override
    public String getMessage() {
        return "Length: " + inputLength;
    }
}
