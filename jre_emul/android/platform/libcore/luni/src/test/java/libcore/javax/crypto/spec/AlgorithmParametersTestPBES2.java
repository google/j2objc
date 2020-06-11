/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package libcore.javax.crypto.spec;

import java.security.AlgorithmParameters;
import java.security.Key;
import java.util.Arrays;

import junit.framework.TestCase;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class AlgorithmParametersTestPBES2 extends TestCase {

    private static final int[] KEY_SIZES = { 128, 256 };
    int[] SHA_VARIANTS = { 1, 224, 256, 384, 512 };

    private static final PBEParameterSpec TEST_PBE_PARAMETER_SPEC = new PBEParameterSpec(
            new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 }, // salt
            34, // iterationCount
            new IvParameterSpec(new byte[] {
                    40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55 // IV
            }));

    // For SHA variants other than SHA1, known answers generated with:
    //
    // AlgorithmParameters ap = AlgorithmParameters.getInstance(
    //         "PBEWithHmacSHA" + shaVariant + "AndAES_" + keySize,
    //         new com.sun.crypto.provider.SunJCE());
    // AlgorithmParameterSpec spec = TEST_PBE_PARAMETER_SPEC;
    // ap.init(spec);
    // System.out.println("Encoded: " + Arrays.toString(ap.getEncoded()));
    //
    // For SHA1, the RI does encode the prf (SHA1) although it is the default one, and thus it
    // shouldn't be explicitly encoded, according to DER. Checked with an ASN1 decoder that the
    // only difference between our encoding and the RI's one is that SHA1 is not explicitly
    // encoded.
    private static final byte[][] GET_ENCODED_KNOWN_ANSWERS = new byte[][] {
        // PBEWithHmacSHA1AndAES_128
        { 48, 75, 6, 9, 42, -122, 72, -122, -9, 13, 1, 5, 13, 48, 62, 48, 29, 6, 9, 42, -122, 72,
                -122, -9, 13, 1, 5, 12, 48, 16, 4, 8, 0, 1, 2, 3, 4, 5, 6, 7, 2, 1, 34, 2, 1, 16,
                48, 29, 6, 9, 96, -122, 72, 1, 101, 3, 4, 1, 2, 4, 16, 40, 41, 42, 43, 44, 45, 46,
                47, 48, 49, 50, 51, 52, 53, 54, 55 },
        // PBEWithHmacSHA224AndAES_128
        { 48, 89, 6, 9, 42, -122, 72, -122, -9, 13, 1, 5, 13, 48, 76, 48, 43, 6, 9, 42, -122, 72,
                -122, -9, 13, 1, 5, 12, 48, 30, 4, 8, 0, 1, 2, 3, 4, 5, 6, 7, 2, 1, 34, 2, 1, 16,
                48, 12, 6, 8, 42, -122, 72, -122, -9, 13, 2, 8, 5, 0, 48, 29, 6, 9, 96, -122, 72, 1,
                101, 3, 4, 1, 2, 4, 16, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54,
                55 },
        // PBEWithHmacSHA256AndAES_128
        { 48, 89, 6, 9, 42, -122, 72, -122, -9, 13, 1, 5, 13, 48, 76, 48, 43, 6, 9, 42, -122, 72,
                -122, -9, 13, 1, 5, 12, 48, 30, 4, 8, 0, 1, 2, 3, 4, 5, 6, 7, 2, 1, 34, 2, 1, 16,
                48, 12, 6, 8, 42, -122, 72, -122, -9, 13, 2, 9, 5, 0, 48, 29, 6, 9, 96, -122, 72, 1,
                101, 3, 4, 1, 2, 4, 16, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54,
                55 },
        // PBEWithHmacSHA384AndAES_128
        { 48, 89, 6, 9, 42, -122, 72, -122, -9, 13, 1, 5, 13, 48, 76, 48, 43, 6, 9, 42, -122, 72,
                -122, -9, 13, 1, 5, 12, 48, 30, 4, 8, 0, 1, 2, 3, 4, 5, 6, 7, 2, 1, 34, 2, 1, 16,
                48, 12, 6, 8, 42, -122, 72, -122, -9, 13, 2, 10, 5, 0, 48, 29, 6, 9, 96, -122, 72,
                1, 101, 3, 4, 1, 2, 4, 16, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53,
                54, 55 },
        // PBEWithHmacSHA512AndAES_128
        { 48, 89, 6, 9, 42, -122, 72, -122, -9, 13, 1, 5, 13, 48, 76, 48, 43, 6, 9, 42, -122, 72,
                -122, -9, 13, 1, 5, 12, 48, 30, 4, 8, 0, 1, 2, 3, 4, 5, 6, 7, 2, 1, 34, 2, 1, 16,
                48, 12, 6, 8, 42, -122, 72, -122, -9, 13, 2, 11, 5, 0, 48, 29, 6, 9, 96, -122, 72,
                1, 101, 3, 4, 1, 2, 4, 16, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53,
                54, 55 },
        // PBEWithHmacSHA1AndAES_256
        { 48, 75, 6, 9, 42, -122, 72, -122, -9, 13, 1, 5, 13, 48, 62, 48, 29, 6, 9, 42, -122, 72,
                -122, -9, 13, 1, 5, 12, 48, 16, 4, 8, 0, 1, 2, 3, 4, 5, 6, 7, 2, 1, 34, 2, 1, 32,
                48, 29, 6, 9, 96, -122, 72, 1, 101, 3, 4, 1, 42, 4, 16, 40, 41, 42, 43, 44, 45, 46,
                47, 48, 49, 50, 51, 52, 53, 54, 55},
        // PBEWithHmacSHA224AndAES_256
        { 48, 89, 6, 9, 42, -122, 72, -122, -9, 13, 1, 5, 13, 48, 76, 48, 43, 6, 9, 42, -122, 72,
                -122, -9, 13, 1, 5, 12, 48, 30, 4, 8, 0, 1, 2, 3, 4, 5, 6, 7, 2, 1, 34, 2, 1, 32,
                48, 12, 6, 8, 42, -122, 72, -122, -9, 13, 2, 8, 5, 0, 48, 29, 6, 9, 96, -122, 72, 1,
                101, 3, 4, 1, 42, 4, 16, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54,
                55 },
        // PBEWithHmacSHA256AndAES_256
        { 48, 89, 6, 9, 42, -122, 72, -122, -9, 13, 1, 5, 13, 48, 76, 48, 43, 6, 9, 42, -122, 72,
                -122, -9, 13, 1, 5, 12, 48, 30, 4, 8, 0, 1, 2, 3, 4, 5, 6, 7, 2, 1, 34, 2, 1, 32,
                48, 12, 6, 8, 42, -122, 72, -122, -9, 13, 2, 9, 5, 0, 48, 29, 6, 9, 96, -122, 72, 1,
                101, 3, 4, 1, 42, 4, 16, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54,
                55 },
        // PBEWithHmacSHA384AndAES_256
        { 48, 89, 6, 9, 42, -122, 72, -122, -9, 13, 1, 5, 13, 48, 76, 48, 43, 6, 9, 42, -122, 72,
                -122, -9, 13, 1, 5, 12, 48, 30, 4, 8, 0, 1, 2, 3, 4, 5, 6, 7, 2, 1, 34, 2, 1, 32,
                48, 12, 6, 8, 42, -122, 72, -122, -9, 13, 2, 10, 5, 0, 48, 29, 6, 9, 96, -122, 72,
                1, 101, 3, 4, 1, 42, 4, 16, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53,
                54, 55 },
        // PBEWithHmacSHA512AndAES_256
        { 48, 89, 6, 9, 42, -122, 72, -122, -9, 13, 1, 5, 13, 48, 76, 48, 43, 6, 9, 42, -122, 72,
                -122, -9, 13, 1, 5, 12, 48, 30, 4, 8, 0, 1, 2, 3, 4, 5, 6, 7, 2, 1, 34, 2, 1, 32,
                48, 12, 6, 8, 42, -122, 72, -122, -9, 13, 2, 11, 5, 0, 48, 29, 6, 9, 96, -122, 72,
                1, 101, 3, 4, 1, 42, 4, 16, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53,
                54, 55 } };

    // Known answers obtained with:
    // String algorithmName = "PBEWithHmacSHA" + shaVariant + "AndAES_" + keySize;
    // SecretKeyFactory skf = SecretKeyFactory.getInstance(
    //        algorithmName, sunProvider);
    // Key key = skf.generateSecret(pbeKS);
    // Cipher c = Cipher.getInstance(algorithmName, sunProvider);
    //            c.init(Cipher.ENCRYPT_MODE, key, TEST_PBE_PARAMETER_SPEC);
    // byte[] encrypted = c.doFinal(plaintext);
    private static final byte[][] ENCRYPT_KNOWN_ANSWERS = new byte[][]{
        // PBEWithHmacSHA1AndAES_128
        { -42, -84, 94, -75, -84, 12, -83, -100, -76, -58, -78, 82, -78, 11, -70, 67, 92, 111, -90,
                -75, 43, 31, 16, 47, -81, -127, -65, -127, -41, 121, 77, -90, -15, 3, 5, -12, 66,
                37, -80, 107, -99, 106, -59, -79, -32, -7, -27, 110 },
        { 121, 36, -19, 19, -89, 95, -15, 50, -62, 36, -23, -78, -7, 43, 79, -31, 17, 88, -35, -89,
                -11, 108, -8, -64, 77, 57, 64, 36, 70, -102, -65, 77, 74, 20, 9, -121, -9, 69, -115,
                21, 32, -22, -107, -75, -76, 111, 79, 99 },
        // PBEWithHmacSHA224AndAES_128
        { 1, 60, 84, 10, 3, 110, 3, -112, 27, 126, 59, -63, -34, 117, 83, -67, -115, 117, -23, 57,
                -70, -126, 57, -84, -3, -102, -87, 98, 77, 10, -19, -41, 20, -95, 53, -112, -48, 22,
                22, -99, -71, -88, -111, -87, 3, -126, -83, 64 },
        // PBEWithHmacSHA256AndAES_128
        { -102, -122, 69, -75, -11, -61, -13, 82, 122, -97, 112, -27, 61, 22, -28, -34, -66, -47,
                123, 104, 20, -115, 83, -33, -38, 65, -101, -128, -20, -34, 95, 53, -69, 11, -79,
                -78, 37, 2, -81, 126, 97, -10, -69, -56, 89, -22, -25, -72 },
        // PBEWithHmacSHA384AndAES_128
        { -67, -57, -99, -6, 102, 87, -111, 18, 63, 7, -99, 32, -110, -24, 44, -94, -24, 101, 39,
                115, 24, 20, -31, 126, 99, -113, -40, 27, 79, -48, 98, 84, 67, -51, 115, -21, -118,
                9, 11, -117, -25, -73, -106, 36, -28, -18, 96, 16 },
        // PBEWithHmacSHA512AndAES_128
        { -72, 97, -41, -80, 0, 39, -121, 107, -89, -72, -103, -52, 100, 126, -89, -59, -4, 73,
                -116, 0, 69, 95, 23, -25, 67, -81, -23, 1, 18, 57, -73, 89, 79, 124, -128, -113, 12,
                78, 14, 12, 64, 112, -105, -6, 13, -112, 26, -92 },
        // PBEWithHmacSHA1AndAES_256
        { 98, 125, 91, -63, 96, 15, -103, -127, 70, -73, -25, 40, -126, 116, 15, -102, -108, 117,
                -111, -65, -24, -114, 90, -126, -115, 15, -86, -72, -109, 47, 39, -102, -52, 123,
                -4, -50, -99, 33, 92, 32, -110, -6, -2, -114, -116, 2, -16, -106 },
        // PBEWithHmacSHA256AndAES_256
        { 126, -32, 53, 14, -13, 26, 127, -23, -38, -56, 66, 1, 45, -128, -16, -99, -34, -31, -49,
                126, 120, -47, -39, -108, -12, 16, 16, -127, 64, -64, 75, 53, 41, 51, 53, -37, -95,
                3, -87, -100, 103, -55, 30, 5, 29, 8, 93, 123 },
        // PBEWithHmacSHA384AndAES_256
        { 68, 99, -46, -114, 37, -29, 59, -80, 16, 113, 116, 97, -9, -36, -32, 8, 59, -124, -73,
                -66, -105, -57, 41, -78, 86, -128, 90, 51, -29, 108, -24, -62, 87, 94, -87, -5, 126,
                95, -101, -39, -126, -76, 77, -2, 44, -70, -70, -88 },
        // PBEWithHmacSHA512AndAES_256
        { -93, -114, 1, -58, 117, -41, -114, 58, 56, 108, -16, -57, -36, -76, 92, -65, 100, 119, -9,
                8, -93, 113, -3, -85, -31, -26, 20, 115, -45, -56, 30, 106, -16, -66, 4, -53, 2,
                -113, 8, -116, -38, 0, 126, -87, 61, -32, 57, -35}
    };

    public void testGetEncoded_knownAnswers() throws Exception {
        int i = 0;
        for (int keySize : KEY_SIZES) {
            for (int shaVariant : SHA_VARIANTS) {
                AlgorithmParameters ap = AlgorithmParameters.getInstance(
                        "PBEWithHmacSHA"+ shaVariant + "AndAES_" + keySize, "BC");
                ap.init(TEST_PBE_PARAMETER_SPEC);
                assertEquals(
                        Arrays.toString(GET_ENCODED_KNOWN_ANSWERS[i]),
                        Arrays.toString(ap.getEncoded()));
                i++;
            }
        }
    }

    public void test_encodeAndDecode() throws Exception {
        AlgorithmParameters ap = AlgorithmParameters.getInstance(
                "PBEWithHmacSHA224AndAES_128", "BC");
        ap.init(TEST_PBE_PARAMETER_SPEC);
        AlgorithmParameters ap2 = AlgorithmParameters.getInstance(
                "PBEWithHmacSHA224AndAES_128", "BC");
        ap2.init(ap.getEncoded());
        PBEParameterSpec encodedSpec = ap2.getParameterSpec(PBEParameterSpec.class);
        assertEquals(Arrays.toString(TEST_PBE_PARAMETER_SPEC.getSalt()),
                Arrays.toString(encodedSpec.getSalt()));
        assertEquals(TEST_PBE_PARAMETER_SPEC.getIterationCount(), encodedSpec.getIterationCount());
        assertTrue(encodedSpec.getParameterSpec() instanceof IvParameterSpec);
        assertEquals(
                Arrays.toString(
                        ((IvParameterSpec) TEST_PBE_PARAMETER_SPEC.getParameterSpec()).getIV()),
                Arrays.toString(((IvParameterSpec) encodedSpec.getParameterSpec()).getIV()));
    }

    public void test_encryptWithAlgorithmParameters() throws Exception {
        byte[] plaintext = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 , 15, 16, 17, 18, 19,
                20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33 };
        PBEKeySpec pbeKS = new PBEKeySpec("aaaaa".toCharArray());
        int i = 0;
        for (int keySize : KEY_SIZES) {
            for (int shaVariant : SHA_VARIANTS) {
                String algorithmName = "PBEWithHmacSHA" + shaVariant + "AndAES_" + keySize;
                AlgorithmParameters ap = AlgorithmParameters.getInstance(algorithmName, "BC");
                ap.init(TEST_PBE_PARAMETER_SPEC);
                SecretKeyFactory skf = SecretKeyFactory.getInstance(
                        algorithmName, "BC");
                Key key = skf.generateSecret(pbeKS);
                Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding", "BC");
                c.init(Cipher.ENCRYPT_MODE, key, ap);
                byte[] encrypted = c.doFinal(plaintext);
                assertEquals(
                        Arrays.toString(ENCRYPT_KNOWN_ANSWERS[i]),
                        Arrays.toString(encrypted));
                c.init(Cipher.DECRYPT_MODE, key, ap);
                byte[] decrypted = c.doFinal(encrypted);
                assertEquals(
                        Arrays.toString(plaintext),
                        Arrays.toString(decrypted));
                i++;
            }
        }
    }

    public void test_correctNames()  throws Exception {
        for (int keySize : KEY_SIZES) {
            for (int shaVariant : SHA_VARIANTS) {
                String algorithmName = "PBEWithHmacSHA" + shaVariant + "AndAES_" + keySize;
                AlgorithmParameters ap = AlgorithmParameters.getInstance(algorithmName, "BC");
                ap.init(TEST_PBE_PARAMETER_SPEC);
                assertTrue(ap.toString().matches("(?i:.*hmacsha" + shaVariant + ".*)"));
                assertTrue(ap.toString().matches("(?i:.*aes" + +keySize + ".*)"));
            }
        }
    }

    public void test_encryptWithNoSalt() throws Exception {
        // Encrypting with no salt is allowed if using a PKCS5 scheme, which
        // the SHA-based ones aren't but MD5-based ones are.
        String algorithmName = "PBEwithMD5andDES";
        SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithmName, "BC");
        Key key = skf.generateSecret(new PBEKeySpec("aaaaa".toCharArray()));
        Cipher c = Cipher.getInstance(algorithmName, "BC");
        c.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(new byte[0], 1000));
    }
}
