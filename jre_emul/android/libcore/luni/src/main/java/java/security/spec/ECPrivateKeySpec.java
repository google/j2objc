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

import java.math.BigInteger;

/**
 * The parameters specifying an Elliptic Curve (EC) private key.
 */
public class ECPrivateKeySpec implements KeySpec {
    // Private value associated with this key
    private final BigInteger s;
    // Elliptic Curve domain parameters associated with this key
    private final ECParameterSpec params;

    /**
     * Creates a new {@code ECPrivateKeySpec} with the specified private value
     * {@code S} and parameter specification.
     *
     * @param s
     *            the private value {@code S}.
     * @param params
     *            the domain parameter specification.
     */
    public ECPrivateKeySpec(BigInteger s, ECParameterSpec params) {
        this.s = s;
        this.params = params;
        // throw NullPointerException if s or params is null
        if (this.s == null) {
            throw new NullPointerException("s == null");
        }
        if (this.params == null) {
            throw new NullPointerException("params == null");
        }
    }

    /**
     * Returns the domain parameter specification.
     *
     * @return the domain parameter specification.
     */
    public ECParameterSpec getParams() {
        return params;
    }

    /**
     * Returns the private value {@code S}.
     *
     * @return the private value {@code S}.
     */
    public BigInteger getS() {
        return s;
    }
}
