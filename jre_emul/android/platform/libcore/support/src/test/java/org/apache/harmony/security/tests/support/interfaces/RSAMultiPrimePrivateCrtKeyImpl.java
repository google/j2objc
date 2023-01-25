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

import java.security.interfaces.RSAMultiPrimePrivateCrtKey;
import java.math.BigInteger;
import java.security.spec.RSAOtherPrimeInfo;

/**
 * Additional class for verification RSAMultiPrimePrivateCrtKey interface
 */
public class RSAMultiPrimePrivateCrtKeyImpl implements RSAMultiPrimePrivateCrtKey {

    static final long serialVersionUID = 123;

    private BigInteger crtCoefficient = null;
    private BigInteger publicExponent = null;
    private BigInteger primeExponentP = null;
    private BigInteger primeExponentQ = null;
    private BigInteger primeP = null;
    private BigInteger primeQ = null;
    private RSAOtherPrimeInfo[] otherPrimeInfo = null;

    public RSAMultiPrimePrivateCrtKeyImpl(BigInteger publicExp,
                                          BigInteger primeExpP,
                                          BigInteger primeExpQ,
                                          BigInteger prP,
                                          BigInteger prQ,
                                          BigInteger crtCft,
                                          RSAOtherPrimeInfo[] otherPrmInfo) {
        publicExponent = publicExp;
        primeExponentP = primeExpP;
        primeExponentQ = primeExpQ;
        primeP = prP;
        primeQ = prQ;
        crtCoefficient = crtCft;
        otherPrimeInfo = otherPrmInfo;
    }

    public BigInteger getCrtCoefficient() {
        return crtCoefficient;
    }
    public RSAOtherPrimeInfo[] getOtherPrimeInfo() {
        return otherPrimeInfo;
    }
    public BigInteger getPrimeExponentP() {
        return primeExponentP;
    }
    public BigInteger getPrimeExponentQ() {
        return primeExponentQ;
    }
    public BigInteger getPrimeP() {
        return primeP;
    }
    public BigInteger getPrimeQ() {
        return primeQ;
    }
    public BigInteger getPublicExponent() {
        return publicExponent;
    }

    public BigInteger getPrivateExponent() {
        return null;
    }
    public String getFormat() {
        return null;
    }
    public byte[] getEncoded() {
        return null;
    }
    public String getAlgorithm() {
        return null;
    }
    public BigInteger getModulus() {
        return null;
    }
}