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
#import "com/google/j2objc/security/IosRSAKeyFactory.h"
]-*/

public class IosRSAKeyPairGenerator extends KeyPairGeneratorSpi {

  /**
   * Default RSA key size 2048 bits.
   */
  private int keySize = 2048;

  @Override
  public native KeyPair generateKeyPair() /*-[
  	// Keys have to be deleted first, else the method will retrieve previous keys.
    // Delete any Public previous key definition.
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
    [publicKeyAttr setObject:publicTag forKey:(id)kSecAttrApplicationTag];
    [publicKeyAttr setObject:@YES forKey:(id)kSecAttrIsPermanent];

    NSData *privateTag = [ComGoogleJ2objcSecurityIosRSAKey_get_PRIVATE_KEY_TAG()
                          dataUsingEncoding:NSUTF8StringEncoding];
    [privateKeyAttr setObject:privateTag forKey:(id)kSecAttrApplicationTag];
    [privateKeyAttr setObject:@YES forKey:(id)kSecAttrIsPermanent];

    [keyPairAttr setObject:(id)kSecAttrKeyTypeRSA forKey:(id)kSecAttrKeyType];
    [keyPairAttr setObject:[NSNumber numberWithUnsignedInteger:keySize_]
                    forKey:(id)kSecAttrKeySizeInBits];
    [keyPairAttr setObject:privateKeyAttr forKey:(id)kSecPrivateKeyAttrs];
    [keyPairAttr setObject:publicKeyAttr forKey:(id)kSecPublicKeyAttrs];

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
  -(void) deleteKey:(NSString *)tag   
  		   keyClass:(CFStringRef) keyClass {
  		   
    NSData *publicTag = [tag dataUsingEncoding:NSUTF8StringEncoding];

    NSMutableDictionary *query = [NSMutableDictionary dictionary];
    query[(id)kSecClass] = (id)kSecClassKey;
    query[(id)kSecAttrKeyType] = (id)kSecAttrKeyTypeRSA;
    query[(id)kSecAttrKeyClass] = (id)keyClass;
    query[(id)kSecAttrApplicationTag] = tag;
	OSStatus status = SecItemDelete((CFDictionaryRef) query);
    if (status != errSecSuccess && status != errSecItemNotFound) {
        NSString *msg = [NSString stringWithFormat:
          @"Problem removing previous public key from the keychain, OSStatus == %d",
          (int)status];
          NSLog (@"%@", msg);
          //TODO(tball):  @throw is causing this error error
          // mplicit declaration of function 'create_JavaSecurityProviderException_initWithNSString_' is invalid in C99
          // [-Werror,-Wimplicit-function-declaration]
          // @throw create_JavaSecurityProviderException_initWithNSString_(msg);
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
