/*
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

package com.google.j2objc.security;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import junit.framework.TestCase;

/**
 * Unit tests for {@link IosRSAKeyPairGenerator}.
 *
 * @author Tom Ball
 */
public class IosRSAKeyPairGeneratorTest extends TestCase {

  // TODO(tball): remove when macOS signature support implemented.
  private static boolean supportedPlatform() {
   return System.getProperty("os.name").equals("iPhone");
  }

  public void testKeyPairGeneration() throws Exception {
    if (supportedPlatform()) {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
      keyGen.initialize(2048);
      KeyPair keyPair = keyGen.generateKeyPair();
      RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
      assertNotNull("null public key", publicKey);
      BigInteger exponent = publicKey.getPublicExponent();
      assertNotNull("null exponent", exponent);
      assertFalse(exponent.intValue() == 0);
      BigInteger modulus = publicKey.getModulus();
      assertNotNull("null modulus", modulus);
      assertFalse(modulus.intValue() == 0);
    }
  }

  // TODO(tball): b/64843435
//  public void testKeyPairsUnique() throws Exception {
//    if (supportedPlatform()) {
//      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
//      keyGen.initialize(2048);
//
//      KeyPair firstKeyPair = keyGen.generateKeyPair();
//      RSAPublicKey firstPublicKey = (RSAPublicKey) firstKeyPair.getPublic();
//      RSAPrivateKey firstPrivateKey = (RSAPrivateKey) firstKeyPair.getPrivate();
//
//      KeyPair secondKeyPair = keyGen.generateKeyPair();
//      RSAPublicKey secondPublicKey = (RSAPublicKey) secondKeyPair.getPublic();
//      RSAPrivateKey secondPrivateKey = (RSAPrivateKey) secondKeyPair.getPrivate();
//
//      assertFalse("public keys have same exponent",
//          firstPublicKey.getPublicExponent().equals(secondPublicKey.getPublicExponent()));
//
//      assertFalse("private keys have same exponent",
//          firstPrivateKey.getPrivateExponent().equals(secondPrivateKey.getPrivateExponent()));
//    }
//  }
}
