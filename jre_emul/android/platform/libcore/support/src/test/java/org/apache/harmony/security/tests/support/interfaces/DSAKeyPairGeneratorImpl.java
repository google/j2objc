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

package org.apache.harmony.security.tests.support.interfaces;

import java.security.interfaces.DSAKeyPairGenerator;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.DSAParams;
import java.security.InvalidParameterException;

/**
 * Additional class for verification DSAKeyPairGenerator interface
 */
public class DSAKeyPairGeneratorImpl implements DSAKeyPairGenerator {

    private KeyPairGenerator dsaKeyPairGenerator = null;
    private SecureRandom secureRandom = null;
    private DSAParams dsaParams = null;
    private int lengthModulus = 0;

    public DSAKeyPairGeneratorImpl(DSAParams dsap) {
        dsaKeyPairGenerator = null;
        try {
            dsaKeyPairGenerator = KeyPairGenerator.getInstance("DSA");
        } catch (Exception e) {
            dsaKeyPairGenerator = null;
        }
        dsaParams = dsap;
    }

    public void initialize(DSAParams params, SecureRandom random)
                           throws InvalidParameterException {
        if (random == null) {
            throw new InvalidParameterException("Incorrect random");
        }
        if (params == null) {
            throw new InvalidParameterException("Incorrect params");
        }
        secureRandom = random;
        dsaParams = params;

    }

    public void initialize(int modlen, boolean genParams, SecureRandom random)
                           throws InvalidParameterException {
        int len = 512;
        while (len <= 1024) {
            if (len == modlen) {
                lengthModulus = modlen;
                break;
            } else {
                len = len + 8;
                if (len == 1032) {
                    throw new InvalidParameterException("Incorrect modlen");
                }
            }
        }
        if (modlen < 512 || modlen > 1024) {
            throw new InvalidParameterException("Incorrect modlen");
        }
        if (random == null) {
            throw new InvalidParameterException("Incorrect random");
        }
        if (genParams == false && dsaParams == null) {
            throw new InvalidParameterException("there are not precomputed parameters");
        }
        secureRandom = random;
    }
}
