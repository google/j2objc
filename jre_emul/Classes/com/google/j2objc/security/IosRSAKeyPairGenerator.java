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
#include "com/google/j2objc/security/IosRSAKey.h"
#include "com/google/j2objc/security/IosRSAKeyFactory.h"
#include "java/security/ProviderException.h"
]-*/

public class IosRSAKeyPairGenerator extends KeyPairGeneratorSpi {

  /**
   * Default RSA key size 2048 bits.
   */
  private int keySize = 2048;

  @Override
  public native KeyPair generateKeyPair() /*-[
    // Delete any previous key definition.
    [self deleteKey:ComGoogleJ2objcSecurityIosRSAKey_PUBLIC_KEY_TAG
        keyClass:kSecAttrKeyClassPublic];

    [self deleteKey:ComGoogleJ2objcSecurityIosRSAKey_PRIVATE_KEY_TAG
        keyClass:kSecAttrKeyClassPrivate];

    // Requested keypair attributes.
    NSMutableDictionary * keyPairAttr = [[NSMutableDictionary alloc] init];
    NSMutableDictionary *publicKeyAttr = [[NSMutableDictionary alloc] init];
    NSMutableDictionary * privateKeyAttr = [[NSMutableDictionary alloc] init];

    NSData *publicTag = [ComGoogleJ2objcSecurityIosRSAKey_get_PUBLIC_KEY_TAG()
                         dataUsingEncoding:NSUTF8StringEncoding];
    publicKeyAttr[(id)kSecAttrApplicationTag] = publicTag;
    publicKeyAttr[(id)kSecAttrIsPermanent] = (id)kCFBooleanTrue;

    NSData *privateTag = [ComGoogleJ2objcSecurityIosRSAKey_get_PRIVATE_KEY_TAG()
                          dataUsingEncoding:NSUTF8StringEncoding];
    privateKeyAttr[(id)kSecAttrApplicationTag] = publicTag;
    privateKeyAttr[(id)kSecAttrIsPermanent] = (id)kCFBooleanTrue;

    keyPairAttr[(id)kSecAttrKeyType] = (id)kSecAttrKeyTypeRSA;
    keyPairAttr[(id)kSecAttrKeySizeInBits] = [NSNumber numberWithUnsignedInteger:keySize_];
    keyPairAttr[(id)kSecPublicKeyAttrs] = publicKeyAttr;
    keyPairAttr[(id)kSecPublicKeyAttrs] = privateKeyAttr;

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

  /*-[
  - (void) deleteKey:(NSString *)tag
            keyClass:(CFStringRef) keyClass {
    NSData *tagBytes = [tag dataUsingEncoding:NSUTF8StringEncoding];

    NSMutableDictionary *query = [NSMutableDictionary dictionary];
    query[(id)kSecClass] = (id)kSecClassKey;
    query[(id)kSecAttrKeyType] = (id)kSecAttrKeyTypeRSA;
    query[(id)kSecAttrKeyClass] = (id)keyClass;
    query[(id)kSecAttrApplicationTag] = tagBytes;
    OSStatus status = SecItemDelete((CFDictionaryRef) query);
    if (status != errSecSuccess && status != errSecItemNotFound) {
#if TARGET_OS_IPHONE || TARGET_OS_SIMULATOR
      NSString *msg = [NSString stringWithFormat:
          @"Problem removing previous public key from the keychain, OSStatus: %d", (int)status];
      @throw create_JavaSecurityProviderException_initWithNSString_(msg);
#else
      NSLog(@"macOS keychain support not implemented, OSStatus: %d", (int)status);
#endif
    }
  }
  ]-*/

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
