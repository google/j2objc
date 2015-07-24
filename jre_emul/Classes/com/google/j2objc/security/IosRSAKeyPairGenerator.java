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

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

/*-[
#import "com/google/j2objc/security/IosRSAKey.h"
]-*/

public class IosRSAKeyPairGenerator extends KeyPairGeneratorSpi {

  /**
   * Default RSA key size 2048 bits.
   */
  private int keySize = 2048;

  @Override
  public native KeyPair generateKeyPair() /*-[
    // Requested keypair attributes.
    NSMutableDictionary * privateKeyAttr = [[NSMutableDictionary alloc] init];
    [privateKeyAttr setObject:[NSNumber numberWithBool:YES] forKey:(id)kSecAttrIsPermanent];
    NSData *privateTag = [ComGoogleJ2objcSecurityIosRSAKey_PRIVATE_KEY_TAG_
                          dataUsingEncoding:NSUTF8StringEncoding];
    [privateKeyAttr setObject:privateTag forKey:(id)kSecAttrApplicationTag];

    NSMutableDictionary * publicKeyAttr = [[NSMutableDictionary alloc] init];
    [publicKeyAttr setObject:[NSNumber numberWithBool:YES] forKey:(id)kSecAttrIsPermanent];
    NSData *publicTag = [ComGoogleJ2objcSecurityIosRSAKey_PUBLIC_KEY_TAG_
                         dataUsingEncoding:NSUTF8StringEncoding];
    [publicKeyAttr setObject:publicTag forKey:(id)kSecAttrApplicationTag];

    NSMutableDictionary * keyPairAttr = [[NSMutableDictionary alloc] init];
    [keyPairAttr setObject:(id)kSecAttrKeyTypeRSA forKey:(id)kSecAttrKeyType];
    [keyPairAttr setObject:[NSNumber numberWithUnsignedInteger:keySize_]
                    forKey:(id)kSecAttrKeySizeInBits];
    [keyPairAttr setObject:privateKeyAttr forKey:@"private"];
    [keyPairAttr setObject:publicKeyAttr forKey:@"public"];

    SecKeyRef publicKeyRef = NULL;
    SecKeyRef privateKeyRef = NULL;
    SecKeyGeneratePair((CFDictionaryRef)keyPairAttr, &publicKeyRef, &privateKeyRef);
    [privateKeyAttr release];
    [publicKeyAttr release];
    [keyPairAttr release];

    ComGoogleJ2objcSecurityIosRSAKey_IosRSAPublicKey *publicKey =
        [[ComGoogleJ2objcSecurityIosRSAKey_IosRSAPublicKey alloc]
            initWithLong:(long long)publicKeyRef];
    ComGoogleJ2objcSecurityIosRSAKey_IosRSAPrivateKey *privateKey =
        [[ComGoogleJ2objcSecurityIosRSAKey_IosRSAPrivateKey alloc]
            initWithLong:(long long)privateKeyRef];
    JavaSecurityKeyPair *keyPair =
        AUTORELEASE([[JavaSecurityKeyPair alloc] initWithJavaSecurityPublicKey:publicKey
                                                    withJavaSecurityPrivateKey:privateKey]);
    [publicKey release];
    [privateKey release];
    return keyPair;
  ]-*/;

  @Override
  public void initialize(int keySize, SecureRandom random) {
    this.keySize = keySize;
  }

  @Override
  public void initialize(AlgorithmParameterSpec params, SecureRandom random)
      throws InvalidAlgorithmParameterException {
    if (!(params instanceof RSAKeyGenParameterSpec)) {
      throw new InvalidAlgorithmParameterException("Only RSAKeyGenParameterSpec supported");
    }
    this.keySize = ((RSAKeyGenParameterSpec) params).getKeysize();
  }
}
