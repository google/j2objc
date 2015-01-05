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

package java.security.interfaces;

import java.security.InvalidParameterException;
import java.security.SecureRandom;

/**
 * The interface for key generators that can generate DSA key pairs.
 */
public interface DSAKeyPairGenerator {

    /**
     * Initializes this generator with the prime ({@code p}), subprime ({@code
     * q}), and base ({@code g}) values from the specified parameters.
     *
     * @param params
     *            the parameter values.
     * @param random
     *            the source of randomness.
     * @throws InvalidParameterException
     *             if the specified parameter values are {@code null} or
     *             invalid.
     */
    public void initialize(DSAParams params, SecureRandom random)
            throws InvalidParameterException;

    /**
     * Initializes this generator for the specified modulus length. Valid values
     * for the modulus length are the multiples of 8 between 512 and 1024.
     * <p>
     * The parameter {@code genParams} specifies whether this method should
     * generate new prime ({@code p}), subprime ({@code q}), and base ({@code g})
     * values or whether
     * it will use the pre-calculated values for the specified modulus
     * length. Default parameters are available for modulus lengths of 512 and 1024
     * bits.
     *
     * @param modlen
     *            the length of the modulus in bits.
     * @param genParams
     *            whether new values should be generated.
     * @param random
     *            the source of randomness.
     * @throws InvalidParameterException
     *             if the specified modulus length is not valid, or if there are
     *             no pre-calculated values and {@code genParams} is {@code
     *             false}.
     */
    public void initialize(int modlen, boolean genParams, SecureRandom random)
            throws InvalidParameterException;
}
