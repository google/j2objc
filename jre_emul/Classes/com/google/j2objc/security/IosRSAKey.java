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

  private static native long decode(byte[] encoded) /*-[
    CFDataRef data = CFDataCreateWithBytesNoCopy(kCFAllocatorDefault,
        (const UInt8 *)encoded->buffer_, encoded->size_, kCFAllocatorNull);
    jlong secKey = (jlong) SecCertificateCreateWithData(NULL, data);
    CFRelease(data);
    return secKey;
  ]-*/;

  protected abstract void decodeParameters();

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
      this(decode(encoded));
    }

    @Override
    long getSecKeyRef() {
      if (iosSecKey == 0L) {
        iosSecKey = createPublicKey();
      }
      return iosSecKey;
    }

    private long createPublicKey() {
      try (DerOutputStream out = new DerOutputStream()) {
        out.putInteger(getModulus());
        out.putInteger(getPublicExponent());

        // https://stackoverflow.com/questions/2922622/how-to-get-the-size-of-a-rsa-key-in-java
        int bitLength = getModulus().bitLength();
        int keySize = ((bitLength + 127) / 128) * 128;

        return createPublicSecKeyRef(
            new DerValue(DerValue.tag_Sequence, out.toByteArray()).toByteArray(), keySize);
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
        in.getBitString(); // Ignore: bitstring of mod + exp.
        in.getBitString();
        modulus = new BigInteger(in.getBitString());
        in.getBitString();
        publicExponent = new BigInteger(in.getBitString());
      } catch (IOException e) {
        // Should never happen, since bytes are extracted from a valid iOS secKeyRef.
        throw new AssertionError("failed decoding key parameters: " + e);
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

    private native long createPublicSecKeyRef(byte[] bytes, int keySizeInBits) /*-[
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
      NSNumber *keySize = [NSNumber numberWithInt:keySizeInBits];
      publicKeyQuery[(id)kSecAttrEffectiveKeySize] = keySize;
      publicKeyQuery[(id)kSecAttrKeySizeInBits] = keySize;
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
#if TARGET_OS_OSX
        CFStringRef errMsg = SecCopyErrorMessageString(result, NULL);
        NSString *msg = [NSString stringWithFormat:
            @"Problem adding the public key to the keychain: %@", (NSString *)errMsg];
        CFRelease(errMsg);
#else
        NSString *msg = [NSString stringWithFormat:
            @"Problem adding the public key to the keychain, OSStatus == %d", (int)status];
#endif
        @throw create_JavaSecurityProviderException_initWithNSString_(msg);
      }

      [publicKey release];
      return (jlong)secKeyRef;
    ]-*/;
  }

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
      this(decode(encoded));
    }

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
        in.getBitString(); // Ignore: bitstring of mod + exp.
        in.getBitString();
        modulus = new BigInteger(in.getBitString());
        in.getBitString();
        privateExponent = new BigInteger(in.getBitString());
      } catch (IOException e) {
        // Should never happen, since bytes are extracted from a valid iOS secKeyRef.
        throw new AssertionError("failed decoding key parameters: " + e);
      }
    }
  }
}
