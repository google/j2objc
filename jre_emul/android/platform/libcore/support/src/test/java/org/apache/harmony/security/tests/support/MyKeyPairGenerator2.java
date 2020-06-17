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

import java.security.InvalidParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

/**
 *  Additional class extends KeyPairGenerator
 *
 */

public class MyKeyPairGenerator2 extends KeyPairGenerator {
    int keySize;

    SecureRandom secureRandom;

    public MyKeyPairGenerator2() {
        super("MyKeyPairGenerator2");
    }

    public String getAlgorithm() {
        return "MyKeyPairGenerator2";
    }

    public static final String getResAlgorithm() {
        return "MyKeyPairGenerator2";
    }

    public MyKeyPairGenerator2(String pp) {
        super(pp);
    }

    public void initialize(int keysize, SecureRandom random) {
        if (keysize < 64) {
            throw new InvalidParameterException("Incorrect keysize parameter");
        }
        keySize = keysize;
        secureRandom = random;
    }

    public KeyPair generateKeyPair() {
        return null;
    }
}

