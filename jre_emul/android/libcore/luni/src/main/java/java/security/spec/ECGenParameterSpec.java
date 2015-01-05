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

package java.security.spec;

/**
 * The parameter specification used to generate elliptic curve domain parameters.
 */
public class ECGenParameterSpec implements AlgorithmParameterSpec {
    // Standard (or predefined) name for EC domain
    // parameters to be generated
    private final String name;

    /**
     * Creates a new {@code ECGenParameterSpec} with the specified standard or
     * predefined name of the to-be-generated domain parameter.
     *
     * @param name
     *            the name of the elliptic curve domain parameter.
     */
    public ECGenParameterSpec(String name) {
        this.name = name;
        if (this.name == null) {
            throw new NullPointerException("name == null");
        }
    }

    /**
     * Returns the name (standard or predefined) of the to-be-generated elliptic
     * curve domain parameter.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
}
