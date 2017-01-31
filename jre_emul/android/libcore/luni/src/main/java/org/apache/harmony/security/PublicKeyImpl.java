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


package org.apache.harmony.security;

import java.security.PublicKey;


/**
 * PublicKeyImpl
 */
public class PublicKeyImpl implements PublicKey {

    /**
     * @serial
     */
    private static final long serialVersionUID = 7179022516819534075L;


    private byte[] encoding;

    private String algorithm;


    public PublicKeyImpl(String algorithm) {
        this.algorithm = algorithm;
    }


    public String getAlgorithm() {
        return algorithm;
    }


    public String getFormat() {
        return "X.509";
    }


    public byte[] getEncoded() {
        byte[] result = new byte[encoding.length];
        System.arraycopy(encoding, 0, result, 0, encoding.length);
        return result;
    }


    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }


    public void setEncoding(byte[] encoding) {
        this.encoding = new byte[encoding.length];
        System.arraycopy(encoding, 0, this.encoding, 0, encoding.length);
    }
}

