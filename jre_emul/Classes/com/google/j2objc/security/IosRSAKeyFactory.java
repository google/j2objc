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

package com.google.j2objc.security;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class IosRSAKeyFactory extends KeyFactorySpi {

  @Override
  protected PublicKey engineGeneratePublic(KeySpec keySpec) throws InvalidKeySpecException {
    if (keySpec instanceof RSAPublicKeySpec) {
      return new IosRSAKey.IosRSAPublicKey((RSAPublicKeySpec) keySpec);
    }
    throw new InvalidKeySpecException(
        "Must use RSAPublicKeySpec; was " + keySpec.getClass().getName());
  }

  @Override
  protected PrivateKey engineGeneratePrivate(KeySpec keySpec)
      throws InvalidKeySpecException {
    if (keySpec instanceof RSAPrivateKeySpec) {
      return new IosRSAKey.IosRSAPrivateKey((RSAPrivateKeySpec) keySpec);
    }
    throw new InvalidKeySpecException(
        "Must use RSAPublicKeySpec; was " + keySpec.getClass().getName());
  }

  @Override
  @SuppressWarnings("unchecked")
  protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec)
      throws InvalidKeySpecException {
    if (key == null) {
      throw new InvalidKeySpecException("key == null");
    }
    if (keySpec == null) {
      throw new InvalidKeySpecException("keySpec == null");
    }

    if (!"RSA".equals(key.getAlgorithm())) {
      throw new InvalidKeySpecException("Key must be a RSA key");
    }

    if (key instanceof RSAPublicKey && RSAPublicKeySpec.class.isAssignableFrom(keySpec)) {
      RSAPublicKey rsaKey = (RSAPublicKey) key;
      return (T) new RSAPublicKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent());
    } else if (key instanceof PublicKey && RSAPublicKeySpec.class.isAssignableFrom(keySpec)) {
      final byte[] encoded = key.getEncoded();
      if (!"X.509".equals(key.getFormat()) || encoded == null) {
        throw new InvalidKeySpecException("Not a valid X.509 encoding");
      }
      RSAPublicKey rsaKey =
          (RSAPublicKey) engineGeneratePublic(new X509EncodedKeySpec(encoded));
      return (T) new RSAPublicKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent());
    } else if (key instanceof RSAPrivateCrtKey
          && RSAPrivateCrtKeySpec.class.isAssignableFrom(keySpec)) {
      RSAPrivateCrtKey rsaKey = (RSAPrivateCrtKey) key;
      return (T) new RSAPrivateCrtKeySpec(rsaKey.getModulus(), rsaKey.getPublicExponent(),
          rsaKey.getPrivateExponent(), rsaKey.getPrimeP(), rsaKey.getPrimeQ(),
          rsaKey.getPrimeExponentP(), rsaKey.getPrimeExponentQ(),
          rsaKey.getCrtCoefficient());
    } else if (key instanceof RSAPrivateCrtKey
          && RSAPrivateKeySpec.class.isAssignableFrom(keySpec)) {
      RSAPrivateCrtKey rsaKey = (RSAPrivateCrtKey) key;
      return (T) new RSAPrivateKeySpec(rsaKey.getModulus(), rsaKey.getPrivateExponent());
    } else if (key instanceof RSAPrivateKey && RSAPrivateKeySpec.class.isAssignableFrom(keySpec)) {
      RSAPrivateKey rsaKey = (RSAPrivateKey) key;
      return (T) new RSAPrivateKeySpec(rsaKey.getModulus(), rsaKey.getPrivateExponent());
    } else if (key instanceof PrivateKey
        && RSAPrivateCrtKeySpec.class.isAssignableFrom(keySpec)) {
      final byte[] encoded = key.getEncoded();
      if (!"PKCS#8".equals(key.getFormat()) || encoded == null) {
        throw new InvalidKeySpecException("Not a valid PKCS#8 encoding");
      }
      RSAPrivateKey privKey =
          (RSAPrivateKey) engineGeneratePrivate(new PKCS8EncodedKeySpec(encoded));
      if (privKey instanceof RSAPrivateCrtKey) {
        RSAPrivateCrtKey rsaKey = (RSAPrivateCrtKey) privKey;
        return (T) new RSAPrivateCrtKeySpec(rsaKey.getModulus(),
            rsaKey.getPublicExponent(), rsaKey.getPrivateExponent(),
            rsaKey.getPrimeP(), rsaKey.getPrimeQ(), rsaKey.getPrimeExponentP(),
            rsaKey.getPrimeExponentQ(), rsaKey.getCrtCoefficient());
      } else {
        throw new InvalidKeySpecException("Encoded key is not an RSAPrivateCrtKey");
      }
    } else if (key instanceof PrivateKey && RSAPrivateKeySpec.class.isAssignableFrom(keySpec)) {
      final byte[] encoded = key.getEncoded();
      if (!"PKCS#8".equals(key.getFormat()) || encoded == null) {
        throw new InvalidKeySpecException("Not a valid PKCS#8 encoding");
      }
      RSAPrivateKey rsaKey =
          (RSAPrivateKey) engineGeneratePrivate(new PKCS8EncodedKeySpec(encoded));
      return (T) new RSAPrivateKeySpec(rsaKey.getModulus(), rsaKey.getPrivateExponent());
    } else if (key instanceof PrivateKey
          && PKCS8EncodedKeySpec.class.isAssignableFrom(keySpec)) {
      final byte[] encoded = key.getEncoded();
      if (!"PKCS#8".equals(key.getFormat())) {
        throw new InvalidKeySpecException("Encoding type must be PKCS#8; was " + key.getFormat());
      } else if (encoded == null) {
        throw new InvalidKeySpecException("Key is not encodable");
      }
      return (T) new PKCS8EncodedKeySpec(encoded);
    } else if (key instanceof PublicKey && X509EncodedKeySpec.class.isAssignableFrom(keySpec)) {
      final byte[] encoded = key.getEncoded();
      if (!"X.509".equals(key.getFormat())) {
        throw new InvalidKeySpecException("Encoding type must be X.509; was " + key.getFormat());
      } else if (encoded == null) {
        throw new InvalidKeySpecException("Key is not encodable");
      }
      return (T) new X509EncodedKeySpec(encoded);
    } else {
      throw new InvalidKeySpecException("Unsupported key type and key spec combination; key="
          + key.getClass().getName() + ", keySpec=" + keySpec.getName());
    }
  }

  @Override
  protected Key engineTranslateKey(Key key) throws InvalidKeyException {
    if (key == null) {
      throw new InvalidKeyException("key == null");
    }
    if ((key instanceof IosRSAKey.IosRSAPublicKey) || (key instanceof IosRSAKey.IosRSAPrivateKey)) {
      return key;
    } else if (key instanceof RSAPublicKey) {
      RSAPublicKey rsaKey = (RSAPublicKey) key;
      try {
        return engineGeneratePublic(new RSAPublicKeySpec(rsaKey.getModulus(),
            rsaKey.getPublicExponent()));
      } catch (InvalidKeySpecException e) {
        throw new InvalidKeyException(e);
      }
    } else if (key instanceof RSAPrivateCrtKey) {
      RSAPrivateCrtKey rsaKey = (RSAPrivateCrtKey) key;
      BigInteger modulus = rsaKey.getModulus();
      BigInteger publicExponent = rsaKey.getPublicExponent();
      BigInteger privateExponent = rsaKey.getPrivateExponent();
      BigInteger primeP = rsaKey.getPrimeP();
      BigInteger primeQ = rsaKey.getPrimeQ();
      BigInteger primeExponentP = rsaKey.getPrimeExponentP();
      BigInteger primeExponentQ = rsaKey.getPrimeExponentQ();
      BigInteger crtCoefficient = rsaKey.getCrtCoefficient();
      try {
        return engineGeneratePrivate(new RSAPrivateCrtKeySpec(modulus,
            publicExponent, privateExponent, primeP, primeQ, primeExponentP,
            primeExponentQ, crtCoefficient));
      } catch (InvalidKeySpecException e) {
        throw new InvalidKeyException(e);
      }
    } else if (key instanceof RSAPrivateKey) {
      RSAPrivateKey rsaKey = (RSAPrivateKey) key;
      BigInteger modulus = rsaKey.getModulus();
      BigInteger privateExponent = rsaKey.getPrivateExponent();
      try {
        return engineGeneratePrivate(new RSAPrivateKeySpec(modulus,
            privateExponent));
      } catch (InvalidKeySpecException e) {
        throw new InvalidKeyException(e);
      }
    } else if ((key instanceof PrivateKey)
        && ("PKCS#8".equals(key.getFormat()))) {
      byte[] encoded = key.getEncoded();
      if (encoded == null) {
        throw new InvalidKeyException("Key does not support encoding");
      }
      try {
        return engineGeneratePrivate(new PKCS8EncodedKeySpec(encoded));
      } catch (InvalidKeySpecException e) {
        throw new InvalidKeyException(e);
      }
    } else if ((key instanceof PublicKey) && ("X.509".equals(key.getFormat()))) {
      byte[] encoded = key.getEncoded();
      if (encoded == null) {
        throw new InvalidKeyException("Key does not support encoding");
      }
      try {
        return engineGeneratePublic(new X509EncodedKeySpec(encoded));
      } catch (InvalidKeySpecException e) {
        throw new InvalidKeyException(e);
      }
    } else {
      throw new InvalidKeyException(
          "Key must be an RSA public or private key; was " + key.getClass().getName());
    }
  }

}
