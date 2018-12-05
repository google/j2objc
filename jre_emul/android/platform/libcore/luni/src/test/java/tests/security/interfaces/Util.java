/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tests.security.interfaces;
import java.math.BigInteger;
import java.security.SecureRandomSpi;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.security.spec.RSAPrivateCrtKeySpec;

/**
 * Utility class to provide some constants
 */
class Util {

    /**
     * Valid P for DSA tests
     */
    static final BigInteger P = new BigInteger(
            "178479572281896551646004364479186243274554253442971675202712037168"
                    + "82805439171286757012622742273566628953929784385654859898"
                    + "28019943266498970695878014699423565775500281013661604573"
                    + "09351370942441879889477647669664876805999161358675121909"
                    + "02875461840550932624652402732307184862051812119809510467"
                    + "6997149499533466361");

    /**
     * Valid Q for DSA tests
     */
    static final BigInteger Q = new BigInteger(
            "764905408100544417452957057404815852894534709423");

    /**
     * Valid G for DSA tests
     */
    static final BigInteger G = new BigInteger(
            "250346303870482828530842176986393415513071912937041425322012361012"
                    + "16575725689706821855929265075265423817009497798948914793"
                    + "36272769721567876826949070538671438636626715308216064610"
                    + "91161573885991070984580607652541845127399865661520191726"
                    + "47818913386618968229835178446104566543814577436312685021"
                    + "713979414153557537");

    /**
     * Value returned using MySecureRandomSpi
     */
    static final BigInteger RND_RET = new BigInteger("10");

    /**
     * Valid RSA parameters
     */
    static final RSAPrivateCrtKeySpec rsaCrtParam = new RSAPrivateCrtKeySpec(
            BigInteger.valueOf(3233), BigInteger.valueOf(17),
            BigInteger.valueOf(2753), BigInteger.valueOf(61),
            BigInteger.valueOf(53), BigInteger.valueOf(53),
            BigInteger.valueOf(49), BigInteger.valueOf(52));

    /**
     * Valid EC parameters
     */
    static final ECParameterSpec ecParam = new ECParameterSpec(
            new EllipticCurve(
                    new ECFieldFp(BigInteger.valueOf(23)),
                    BigInteger.valueOf(5), BigInteger.valueOf(3)),
            new ECPoint(BigInteger.valueOf(1), BigInteger.valueOf(3)),
            BigInteger.valueOf(23), 1);

    private Util() {
    }
}

/**
 * Utility class to provide "random" data.
 * Returned value is always constant 10 if converted to BigInteger
 */
@SuppressWarnings("serial")
class MySecureRandomSpi extends SecureRandomSpi {

    @Override
    protected byte[] engineGenerateSeed(int arg0) {
        return null;
    }

    @Override
    protected void engineNextBytes(byte[] bytes) {
        java.util.Arrays.fill(bytes, (byte) 0);
        bytes[bytes.length - 1] = (byte) 10;
    }

    @Override
    protected void engineSetSeed(byte[] arg0) {
        return;
    }
}

