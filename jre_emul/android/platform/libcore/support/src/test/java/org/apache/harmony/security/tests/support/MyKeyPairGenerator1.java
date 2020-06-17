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
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

/**
 * Additional class extends KeyPairGenerator
 *
 */

public class MyKeyPairGenerator1 extends KeyPairGenerator {
    public int keySize;

    public SecureRandom secureRandom;

    public AlgorithmParameterSpec paramSpec;

    public MyKeyPairGenerator1() {
        super("MyKeyPairGenerator1");
    }

    public MyKeyPairGenerator1(String pp) {
        super(pp);
    }

    public String getAlgorithm() {
        return "MyKeyPairGenerator1";
    }

    public static final String getResAlgorithm() {
        return "MyKeyPairGenerator1";
    }

    public void initialize(int keysize, SecureRandom random) {
        if ((keysize < 0) || ((keysize % 100) != 0)) {
            throw new InvalidParameterException("Incorrect keysize parameter");
        }
        if (random == null) {
            throw new InvalidParameterException("Incorrect random");
        }
        keySize = keysize;
        secureRandom = random;
    }

    public KeyPair generateKeyPair() {
        try {
            return new KeyPair(new PubKey(), new PrivKey());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void initialize(AlgorithmParameterSpec param, SecureRandom random)
            throws InvalidAlgorithmParameterException {
        if (random == null) {
            throw new InvalidParameterException("Incorrect random");
        }
        if (param == null) {
            throw new InvalidAlgorithmParameterException("Incorrect param");
        }
        paramSpec = param;
        secureRandom = random;
    }

    public class PubKey implements PublicKey {
        private String algorithm;

        private String format;

        private byte[] encoded;

        public PubKey() {
            this.algorithm = "MyKeyPairGenerator1";
            this.format = "test1";
            this.encoded = new byte[10];
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public String getFormat() {
            return format;
        }

        public byte[] getEncoded() {
            return encoded;
        }
    }

    public class PrivKey implements PrivateKey {
        private String algorithm;

        private String format;

        private byte[] encoded;

        public PrivKey() {
            this.algorithm = "MyKeyPairGenerator1";
            this.format = "test1";
            this.encoded = new byte[10];
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public String getFormat() {
            return format;
        }

        public byte[] getEncoded() {
            return encoded;
        }
    }

}