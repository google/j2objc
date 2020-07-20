/*
 * Copyright (C) 2010 The Android Open Source Project
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
 * limitations under the License.
 */

package libcore.javax.crypto;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import junit.framework.TestCase;
import libcore.java.security.StandardNames;

public class SecretKeyFactoryTest extends TestCase {

    private static final char[] PASSWORD = "google".toCharArray();
    /**
     * Salts should be random to reduce effectiveness of dictionary
     * attacks, but need not be kept secret from attackers. For more
     * information, see http://en.wikipedia.org/wiki/Salt_(cryptography)
     */
    private static final byte[] SALT = {0, 1, 2, 3, 4, 5, 6, 7};
    /**
     * The number of iterations should be higher for production
     * strength protection. The tolerable value may vary from device
     * to device, but 8192 should be acceptable for PBKDF2 on a Nexus One.
     */
    private static final int ITERATIONS = 1024;
    private static final int KEY_LENGTH = 128;

    public void test_PBKDF2_required_parameters() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        // PBEKeySpec validates arguments most to be non-null, non-empty, postive, etc.
        // Focus on insufficient PBEKeySpecs

        // PBEKeySpecs password only constructor
        try {
            KeySpec ks = new PBEKeySpec(null);
            factory.generateSecret(ks);
            fail();
        } catch (InvalidKeySpecException expected) {
        }
        try {
            KeySpec ks = new PBEKeySpec(new char[0]);
            factory.generateSecret(ks);
            fail();
        } catch (InvalidKeySpecException expected) {
        }
        try {
            KeySpec ks = new PBEKeySpec(PASSWORD);
            factory.generateSecret(ks);
            fail();
        } catch (InvalidKeySpecException expected) {
        }


        // PBEKeySpecs constructor without key length
        try {
            KeySpec ks = new PBEKeySpec(null, SALT, ITERATIONS);
            factory.generateSecret(ks);
            fail();
        } catch (InvalidKeySpecException expected) {
        }
        try {
            KeySpec ks = new PBEKeySpec(new char[0], SALT, ITERATIONS);
            factory.generateSecret(ks);
            fail();
        } catch (InvalidKeySpecException expected) {
        }
        try {
            KeySpec ks = new PBEKeySpec(PASSWORD, SALT, ITERATIONS);
            factory.generateSecret(ks);
            fail();
        } catch (InvalidKeySpecException expected) {
        }

        try {
            KeySpec ks = new PBEKeySpec(null, SALT, ITERATIONS, KEY_LENGTH);
            factory.generateSecret(ks);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            KeySpec ks = new PBEKeySpec(new char[0], SALT, ITERATIONS, KEY_LENGTH);
            factory.generateSecret(ks);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        KeySpec ks = new PBEKeySpec(PASSWORD, SALT, ITERATIONS, KEY_LENGTH);
        factory.generateSecret(ks);
    }

    public void test_PBKDF2_b3059950() throws Exception {
        byte[] expected = new byte[] {
                        (byte)0x70, (byte)0x74, (byte)0xdb, (byte)0x72,
                        (byte)0x35, (byte)0xd4, (byte)0x11, (byte)0x68,
                        (byte)0x83, (byte)0x7c, (byte)0x14, (byte)0x1f,
                        (byte)0xf6, (byte)0x4a, (byte)0xb0, (byte)0x54
                    };
        test_PBKDF2_UTF8(PASSWORD, SALT, ITERATIONS, KEY_LENGTH, expected);
        test_PBKDF2_8BIT(PASSWORD, SALT, ITERATIONS, KEY_LENGTH, expected);
    }

    /**
     * 64-bit Test vector from RFC 3211
     *
     * See also org.bouncycastle.crypto.test.PKCS5Test
     */
    public void test_PBKDF2_rfc3211_64() throws Exception {
        char[] password = "password".toCharArray();
        byte[] salt = new byte[] {
            (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78,
            (byte)0x78, (byte)0x56, (byte)0x34, (byte)0x12
        };
        int iterations = 5;
        int keyLength = 64;
        byte[] expected = new byte[] {
            (byte)0xD1, (byte)0xDA, (byte)0xA7, (byte)0x86,
            (byte)0x15, (byte)0xF2, (byte)0x87, (byte)0xE6
        };
        test_PBKDF2_UTF8(password, salt, iterations, keyLength, expected);
        test_PBKDF2_8BIT(password, salt, iterations, keyLength, expected);
    }

    /**
     * 192-bit Test vector from RFC 3211
     *
     * See also org.bouncycastle.crypto.test.PKCS5Test
     */
    public void test_PBKDF2_rfc3211_192() throws Exception {
        char[] password = ("All n-entities must communicate with other "
                           + "n-entities via n-1 entiteeheehees").toCharArray();
        byte[] salt = new byte[] {
            (byte)0x12, (byte)0x34, (byte)0x56, (byte)0x78,
            (byte)0x78, (byte)0x56, (byte)0x34, (byte)0x12
        };
        int iterations = 500;
        int keyLength = 192;
        byte[] expected = new byte[] {
            (byte)0x6a, (byte)0x89, (byte)0x70, (byte)0xbf, (byte)0x68, (byte)0xc9,
            (byte)0x2c, (byte)0xae, (byte)0xa8, (byte)0x4a, (byte)0x8d, (byte)0xf2,
            (byte)0x85, (byte)0x10, (byte)0x85, (byte)0x86, (byte)0x07, (byte)0x12,
            (byte)0x63, (byte)0x80, (byte)0xcc, (byte)0x47, (byte)0xab, (byte)0x2d
        };
        test_PBKDF2_UTF8(password, salt, iterations, keyLength, expected);
        test_PBKDF2_8BIT(password, salt, iterations, keyLength, expected);
    }

   /**
    * Unicode Test vector for b/8312059.
    *
    * See also https://code.google.com/p/android/issues/detail?id=40578
    */
    public void test_PBKDF2_b8312059() throws Exception {

        char[] password = "\u0141\u0142".toCharArray();
        byte[] salt = "salt".getBytes(UTF_8);
        int iterations = 4096;
        int keyLength = 160;
        byte[] expected_utf8 = new byte[] {
            (byte)0x4c, (byte)0xe0, (byte)0x6a, (byte)0xb8, (byte)0x48, (byte)0x04,
            (byte)0xb7, (byte)0xe7, (byte)0x72, (byte)0xf2, (byte)0xaf, (byte)0x5e,
            (byte)0x54, (byte)0xe9, (byte)0x03, (byte)0xad, (byte)0x59, (byte)0x64,
            (byte)0x8b, (byte)0xab
        };
        byte[] expected_8bit = new byte[] {
            (byte)0x6e, (byte)0x43, (byte)0xe0, (byte)0x18, (byte)0xc5, (byte)0x50,
            (byte)0x0d, (byte)0xa7, (byte)0xfe, (byte)0x7a, (byte)0x44, (byte)0x4d,
            (byte)0x99, (byte)0x5d, (byte)0x8c, (byte)0xae, (byte)0xc1, (byte)0xc9,
            (byte)0x17, (byte)0xce
        };
        test_PBKDF2_UTF8(password, salt, iterations, keyLength, expected_utf8);
        test_PBKDF2_8BIT(password, salt, iterations, keyLength, expected_8bit);
    }

    private void test_PBKDF2_8BIT(char[] password, byte[] salt, int iterations, int keyLength,
                                  byte[] expected) throws Exception {
        if (!StandardNames.IS_RI) {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1And8bit");
            KeySpec ks = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKey key = factory.generateSecret(ks);
            assertTrue(Arrays.equals(expected, key.getEncoded()));
        }

    }

    private void test_PBKDF2_UTF8(char[] password, byte[] salt, int iterations, int keyLength,
                                  byte[] expected) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec ks = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKey key = factory.generateSecret(ks);
        assertTrue(Arrays.equals(expected, key.getEncoded()));

    }
}
