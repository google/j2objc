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

/**
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package org.apache.harmony.security.tests.support;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

/**
 * Additional class for verification of KeyPairGeneratorSpi and KeyPairGenerator
 *
 */

public class MyKeyPairGeneratorSpi extends KeyPairGeneratorSpi {

    public void initialize(int keysize, SecureRandom random) {
        if (keysize < 100) {
            throw new InvalidParameterException(
                    "Invalid keysize: less than 100");
        }
        if (random == null) {
            throw new IllegalArgumentException("Invalid random");
        }
    }

    public KeyPair generateKeyPair() {
        return null;
    }

    public void initialize(AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidAlgorithmParameterException {
        if (random == null) {
            throw new UnsupportedOperationException(
                    "Not supported for null random");
        }
    }
}