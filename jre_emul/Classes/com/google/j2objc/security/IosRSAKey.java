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

import org.apache.harmony.security.asn1.BitString;
import org.apache.harmony.security.asn1.DerInputStream;

import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public abstract class IosRSAKey implements RSAKey, Key {

  protected transient long iosSecKey;
  protected BigInteger modulus;

  static final String PUBLIC_KEY_TAG = "com.google.j2objc.security.publickey";
  static final String PRIVATE_KEY_TAG = "com.google.j2objc.security.privatekey";

  public IosRSAKey(BigInteger modulus) {
    this.modulus = modulus;
  }

  public IosRSAKey(long iosSecKey) {
    this.iosSecKey = iosSecKey;
  }

  @Override
  public String getAlgorithm() {
    return "RSA";
  }

  @Override
  public String getFormat() {
    return "X.509";
  }

  @Override
  public BigInteger getModulus() {
    if (modulus == null) {
      decodeParameters();
    }
    return modulus;
  }

  long getSecKeyRef() {
    return iosSecKey;
  }

  protected abstract void decodeParameters();

  public static class IosRSAPublicKey extends IosRSAKey implements RSAPublicKey {

    private BigInteger publicExponent;

    public IosRSAPublicKey(long iosSecKey) {
      super(iosSecKey);
    }

    public IosRSAPublicKey(RSAPublicKeySpec spec) {
      super(spec.getModulus());
      this.publicExponent = spec.getPublicExponent();
    }

    @Override
    public native byte[] getEncoded() /*-[
      NSData *publicKey = nil;
      NSData *publicTag = [ComGoogleJ2objcSecurityIosRSAKey_PUBLIC_KEY_TAG_
                           dataUsingEncoding:NSUTF8StringEncoding];

      NSMutableDictionary *publicKeyQuery = [[NSMutableDictionary alloc] init];
      [publicKeyQuery setObject:(id)kSecClassKey forKey:(id)kSecClass];
      [publicKeyQuery setObject:publicTag forKey:(id)kSecAttrApplicationTag];
      [publicKeyQuery setObject:(id)kSecAttrKeyTypeRSA forKey:(id)kSecAttrKeyType];
      [publicKeyQuery setObject:[NSNumber numberWithBool:YES] forKey:(id)kSecReturnData];
      OSStatus result =
          SecItemCopyMatching((CFDictionaryRef)publicKeyQuery, (CFTypeRef *)&publicKey);
      [publicKeyQuery release];

      IOSByteArray *bytes = nil;
      if (result == noErr && publicKey.length > 0) {
        bytes = [IOSByteArray arrayWithBytes:(jbyte *)publicKey.bytes count:publicKey.length];
        [publicKey release];
      }
      return bytes;
    ]-*/;

    @Override
    public BigInteger getPublicExponent() {
      if (publicExponent == null) {
        decodeParameters();
      }
      return publicExponent;
    }

    @Override
    protected void decodeParameters() {
      byte[] bytes = getEncoded();
      if (bytes == null) {
        return;
      }
      try {
        DerInputStream in = new DerInputStream(bytes);
        in.readBitString(); // Ignore: bitstring of mod + exp.
        in.readBitString();
        modulus = new BigInteger(((BitString) in.content).bytes);
        in.readBitString();
        publicExponent = new BigInteger(((BitString) in.content).bytes);
      } catch (IOException e) {
        // Should never happen, since bytes are extracted from a valid iOS secKeyRef.
        throw new AssertionError("failed decoding key parameters: " + e);
      }
    }
  }

  public static class IosRSAPrivateKey extends IosRSAKey implements RSAPrivateKey {

    private BigInteger privateExponent;

    public IosRSAPrivateKey(long iosSecKey) {
      super(iosSecKey);
    }

    public IosRSAPrivateKey(RSAPrivateKeySpec spec) {
      super(spec.getModulus());
      this.privateExponent = spec.getPrivateExponent();
    }

    @Override
    public native byte[] getEncoded() /*-[
      NSData *privateKey = nil;
      NSData *privateTag = [ComGoogleJ2objcSecurityIosRSAKey_PRIVATE_KEY_TAG_
                           dataUsingEncoding:NSUTF8StringEncoding];

      NSMutableDictionary *privateKeyQuery = [[NSMutableDictionary alloc] init];
      [privateKeyQuery setObject:(id)kSecClassKey forKey:(id)kSecClass];
      [privateKeyQuery setObject:privateTag forKey:(id)kSecAttrApplicationTag];
      [privateKeyQuery setObject:(id)kSecAttrKeyTypeRSA forKey:(id)kSecAttrKeyType];
      [privateKeyQuery setObject:[NSNumber numberWithBool:YES] forKey:(id)kSecReturnData];
      OSStatus result =
          SecItemCopyMatching((CFDictionaryRef)privateKeyQuery, (CFTypeRef *)&privateKey);
      [privateKeyQuery release];

      IOSByteArray *bytes = nil;
      if (result == noErr && privateKey.length > 0) {
        bytes = [IOSByteArray arrayWithBytes:(jbyte *)privateKey.bytes count:privateKey.length];
        [privateKey release];
      }
      return bytes;
    ]-*/;

    @Override
    public BigInteger getPrivateExponent() {
      if (privateExponent == null) {
        decodeParameters();
      }
      return privateExponent;
    }

    protected void decodeParameters() {
      byte[] bytes = getEncoded();
      if (bytes == null) {
        return;
      }
      try {
        DerInputStream in = new DerInputStream(bytes);
        in.readBitString(); // Ignore: bitstring of mod + exp.
        in.readBitString();
        modulus = new BigInteger(((BitString) in.content).bytes);
        in.readBitString();
        privateExponent = new BigInteger(((BitString) in.content).bytes);
      } catch (IOException e) {
        // Should never happen, since bytes are extracted from a valid iOS secKeyRef.
        throw new AssertionError("failed decoding key parameters: " + e);
      }
    }
  }
}
