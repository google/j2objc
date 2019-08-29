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

import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.ProviderException;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

/**
 * Public and private RSA key implementations for iOS.
 */
public abstract class IosRSAKey implements RSAKey, Key {

  protected transient long iosSecKey = 0L;
  protected BigInteger modulus;

  static final String PUBLIC_KEY_TAG = "com.google.j2objc.security.publickey";
  static final String PRIVATE_KEY_TAG = "com.google.j2objc.security.privatekey";
  private static final long serialVersionUID = 1L;

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

  /**
   * iOS implementation of RSAPublicKey.
   *
   * TODO(tball): add support for macOS.
   */
  public static class IosRSAPublicKey extends IosRSAKey implements RSAPublicKey {

    private BigInteger publicExponent;
    private static final long serialVersionUID = 1L;

    public IosRSAPublicKey(long iosSecKey) {
      super(iosSecKey);
    }

    public IosRSAPublicKey(RSAPublicKeySpec spec) {
      super(spec.getModulus());
      this.publicExponent = spec.getPublicExponent();
    }

    public IosRSAPublicKey(byte[] encoded) {
      super(createPublicSecKeyRef(encoded));
    }

    @Override
    long getSecKeyRef() {
      if (iosSecKey == 0L && publicExponent != null && modulus != null) {
        iosSecKey = createPublicKey();
      }
      return iosSecKey;
    }

    private long createPublicKey() {
      try (DerOutputStream out = new DerOutputStream()) {
        out.putInteger(getModulus());
        out.putInteger(getPublicExponent());

        return createPublicSecKeyRef(
            new DerValue(DerValue.tag_Sequence, out.toByteArray()).toByteArray());
      } catch (IOException e) {
        throw new ProviderException(e); // Should never happen.
      }
    }

    @Override
    public native byte[] getEncoded() /*-[
      NSData *publicKey = nil;
      NSData *publicTag = [ComGoogleJ2objcSecurityIosRSAKey_PUBLIC_KEY_TAG
                           dataUsingEncoding:NSUTF8StringEncoding];

      NSMutableDictionary *publicKeyQuery = [[NSMutableDictionary alloc] init];
      publicKeyQuery[(id)kSecClass] = (id)kSecClassKey;
      publicKeyQuery[(id)kSecAttrApplicationTag] = publicTag;
      publicKeyQuery[(id)kSecAttrKeyType] = (id)kSecAttrKeyTypeRSA;
      publicKeyQuery[(id)kSecAttrKeyClass] = (id)kSecAttrKeyClassPublic;
      publicKeyQuery[(id) kSecReturnData] = (id) kCFBooleanTrue;
      OSStatus status =
          SecItemCopyMatching((CFDictionaryRef)publicKeyQuery, (CFTypeRef *)&publicKey);
      [publicKeyQuery release];

      IOSByteArray *bytes = nil;
      if (status == noErr && publicKey.length > 0) {
        bytes = [IOSByteArray arrayWithBytes:(jbyte *)publicKey.bytes count:publicKey.length];
        [publicKey release];
      } else {
          NSString *msg =
              [NSString stringWithFormat:@"PublicKey getEncoded error %d", (int)status];
          @throw create_JavaSecurityProviderException_initWithNSString_(msg);
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
        if (in.peekByte() == DerValue.tag_BitString) {
          // Strip headers.
          in.getBitString(); // Ignore: bitstring of mod + exp.
          in.getBitString();
          modulus = new BigInteger(in.getBitString());
          in.getBitString();
          publicExponent = new BigInteger(in.getBitString());
        } else {
          DerValue[] values = in.getSequence(2);
          publicExponent = values[0].getBigInteger();
          modulus = values[1].getBigInteger();
        }
      } catch (IOException e) {
        throw new ProviderException("failed decoding public key parameters: " + e);
      }
    }

    /*-[
    NSMutableDictionary *getQuery() {
      NSData *publicTag = [ComGoogleJ2objcSecurityIosRSAKey_PUBLIC_KEY_TAG
          dataUsingEncoding:NSUTF8StringEncoding];

      NSMutableDictionary *query = [NSMutableDictionary dictionary];
      query[(id)kSecClass] = (id)kSecClassKey;
      query[(id)kSecAttrKeyType] = (id)kSecAttrKeyTypeRSA;
      query[(id)kSecAttrKeyClass] = (id)kSecAttrKeyClassPublic;
      query[(id)kSecAttrApplicationTag] = publicTag;
      return query;
    }
    ]-*/

    private static native long createPublicSecKeyRef(byte[] bytes) /*-[
      NSData *publicKey = [[NSData alloc] initWithBytes:(const void *)(bytes->buffer_)
                                                 length:bytes->size_];

      // Delete any previous key definition.
      NSMutableDictionary *publicKeyQuery = getQuery();
      OSStatus status = SecItemDelete((CFDictionaryRef) publicKeyQuery);
      if (status != errSecSuccess && status != errSecItemNotFound) {
        NSString *msg = [NSString stringWithFormat:
            @"Problem removing previous public key from the keychain, OSStatus == %d",
            (int)status];
        @throw create_JavaSecurityProviderException_initWithNSString_(msg);
      }

      // Store key in keychain.
      publicKeyQuery = getQuery();  // remove if not necessary
      publicKeyQuery[(id)kSecAttrCanDecrypt] = (id)kCFBooleanFalse;
      publicKeyQuery[(id)kSecAttrCanDerive] = (id)kCFBooleanFalse;
      publicKeyQuery[(id)kSecAttrCanEncrypt] = (id)kCFBooleanTrue;
      publicKeyQuery[(id)kSecAttrCanSign] = (id)kCFBooleanFalse;
      publicKeyQuery[(id)kSecAttrCanVerify] = (id)kCFBooleanTrue;
      publicKeyQuery[(id)kSecAttrCanUnwrap] = (id)kCFBooleanFalse;
      publicKeyQuery[(id)kSecAttrCanWrap] = (id)kCFBooleanTrue;
      publicKeyQuery[(id)kSecReturnRef] = (id)kCFBooleanTrue;
      publicKeyQuery[(id)kSecValueData] = publicKey;
      SecKeyRef secKeyRef = NULL;
      OSStatus result = SecItemAdd((CFDictionaryRef)publicKeyQuery, (CFTypeRef *)&secKeyRef);
      if (result != errSecSuccess) {
#if TARGET_OS_IPHONE || TARGET_OS_SIMULATOR
        NSString *msg = [NSString stringWithFormat:
            @"Problem adding the public key to the keychain, OSStatus: %d", (int)result];
        @throw create_JavaSecurityProviderException_initWithNSString_(msg);
#else
        NSLog(@"macOS keychain support not implemented, OSStatus: %d", (int)result);
#endif
      }

      [publicKey release];
      return (jlong)secKeyRef;
    ]-*/;
  }

  /**
   * iOS implementation of RSAPublicKey.
   *
   * TODO(tball): add support for macOS.
   */
  public static class IosRSAPrivateKey extends IosRSAKey implements RSAPrivateKey {

    private BigInteger privateExponent;
    private static final long serialVersionUID = 1L;

    public IosRSAPrivateKey(long iosSecKey) {
      super(iosSecKey);
    }

    public IosRSAPrivateKey(RSAPrivateKeySpec spec) {
      super(spec.getModulus());
      this.privateExponent = spec.getPrivateExponent();
    }

    public IosRSAPrivateKey(byte[] encoded) {
      super(createPrivateSecKeyRef(encoded));
    }

    /*-[
    NSMutableDictionary *getPrivateQuery() {
      NSData *privateTag = [ComGoogleJ2objcSecurityIosRSAKey_PRIVATE_KEY_TAG
          dataUsingEncoding:NSUTF8StringEncoding];

      NSMutableDictionary *query = [NSMutableDictionary dictionary];
      query[(id)kSecClass] = (id)kSecClassKey;
      query[(id)kSecAttrKeyType] = (id)kSecAttrKeyTypeRSA;
      query[(id)kSecAttrKeyClass] = (id)kSecAttrKeyClassPrivate;
      query[(id)kSecAttrApplicationTag] = privateTag;
      return query;
    }
    ]-*/

    @Override
    public native byte[] getEncoded() /*-[
      NSData *privateKey = nil;
      NSData *privateTag = [ComGoogleJ2objcSecurityIosRSAKey_PRIVATE_KEY_TAG
                           dataUsingEncoding:NSUTF8StringEncoding];

      NSMutableDictionary *privateKeyQuery = [[NSMutableDictionary alloc] init];
      [privateKeyQuery setObject:(id)kSecClassKey forKey:(id)kSecClass];
      [privateKeyQuery setObject:privateTag forKey:(id)kSecAttrApplicationTag];
      [privateKeyQuery setObject:(id)kSecAttrKeyTypeRSA forKey:(id)kSecAttrKeyType];
      [privateKeyQuery setObject:[NSNumber numberWithBool:true] forKey:(id)kSecReturnData];
      OSStatus status =
          SecItemCopyMatching((CFDictionaryRef)privateKeyQuery, (CFTypeRef *)&privateKey);
      [privateKeyQuery release];

      IOSByteArray *bytes = nil;
      if (status == noErr && privateKey.length > 0) {
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
        in.getBitString(); // Ignore: bitstring of mod + exp.
        in.getBitString();
        modulus = new BigInteger(in.getBitString());
        in.getBitString();
        privateExponent = new BigInteger(in.getBitString());
      } catch (IOException e) {
        throw new ProviderException("failed decoding private key parameters: " + e);
      }
    }

    /**
     * This method adds a private key to the keychain and returns the key-reference. The Security
     * Framework only supports the PKCS#1 private key format, so the header field of a PKCS#8
     * certificate needs to be stripped first.
     */
    private static native long createPrivateSecKeyRef(byte[] bytes) /*-[
      NSData * privateKey = [[[NSData alloc] initWithBytes:(const void *)(bytes->buffer_)
                                                    length:bytes->size_] autorelease];

      // Delete any previous key definition.
      NSMutableDictionary *keyQuery = getPrivateQuery();
      OSStatus status = SecItemDelete((CFDictionaryRef) keyQuery);
      if (status != errSecSuccess && status != errSecItemNotFound) {
        NSString *msg = [NSString stringWithFormat:
            @"Problem removing previous public key from the keychain, OSStatus == %d",
            (int)status];
        @throw create_JavaSecurityProviderException_initWithNSString_(msg);
      }

      // Store key in keychain.
      // Set kSecAttrAccessible to Always, since this fails when the app launches before the phone
      // is unlocked (b/72042384).
#pragma clang diagnostic push
#pragma GCC diagnostic ignored "-Wdeprecated-declarations"
      // Set kSecAttrAccessible to Always, since this fails when the app launches before the phone
      // is unlocked (b/72042384).
      keyQuery[(id)kSecAttrAccessible] = (id)kSecAttrAccessibleAlways;
#pragma clang diagnostic pop
      keyQuery[(id)kSecAttrCanDecrypt] = (id)kCFBooleanTrue;
      keyQuery[(id)kSecAttrCanDerive] = (id)kCFBooleanTrue;
      keyQuery[(id)kSecAttrCanEncrypt] = (id)kCFBooleanTrue;
      keyQuery[(id)kSecAttrCanSign] = (id)kCFBooleanTrue;
      keyQuery[(id)kSecAttrCanVerify] = (id)kCFBooleanTrue;
      keyQuery[(id)kSecAttrCanUnwrap] = (id)kCFBooleanFalse;
      keyQuery[(id)kSecAttrCanWrap] = (id)kCFBooleanTrue;
      keyQuery[(id)kSecReturnRef] = (id)kCFBooleanTrue;
      keyQuery[(id)kSecValueData] = privateKey;
      SecKeyRef secKeyRef = NULL;
      OSStatus osStatus = SecItemAdd((CFDictionaryRef)keyQuery, (CFTypeRef *)&secKeyRef);
      if (osStatus != errSecSuccess) {
#if TARGET_OS_IPHONE || TARGET_OS_SIMULATOR
        NSString *msg = [NSString stringWithFormat:
            @"Problem adding the private key to the keychain, OSStatus: %d", (int)osStatus];
        @throw create_JavaSecurityProviderException_initWithNSString_(msg);
#else
        NSLog(@"macOS keychain support not implemented, OSStatus: %d", (int)osStatus);
#endif
      }

#if TARGET_OS_IPHONE || TARGET_OS_SIMULATOR
      if (secKeyRef == NULL) {
        // Try again, my way.
        // Convert a PKCS#8 key to PKCS#1 key by stripping off the header.
        NSData *pkcs1Key = [privateKey subdataWithRange:NSMakeRange(26, [privateKey length] - 26)];

        keyQuery[(id)kSecAttrKeyType] = (id)kSecAttrKeyTypeRSA;
        keyQuery[(id)kSecAttrKeyClass] = (id)kSecAttrKeyClassPrivate;
        keyQuery[(id)kSecAttrKeySizeInBits] = @2048;
        keyQuery[(id)kSecValueData] = pkcs1Key;

        OSStatus osStatus = SecItemAdd((CFDictionaryRef)keyQuery, (CFTypeRef *)&secKeyRef);

        if (osStatus != errSecSuccess || !secKeyRef) {
          NSString *msg = [NSString stringWithFormat:
              @"Problem adding the private key to the keychain after truncating, OSStatus: %d",
              (int)osStatus];
          @throw create_JavaSecurityProviderException_initWithNSString_(msg);
        }
      }
#endif
      return (jlong)secKeyRef;
    ]-*/;
  }
}
