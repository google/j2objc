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

package com.google.j2objc.security.cert;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

/*-[
#include "NSDataInputStream.h"
#include "java/lang/RuntimeException.h"
#include "org/apache/harmony/security/asn1/ASN1Integer.h"
#include "org/apache/harmony/security/asn1/BerInputStream.h"

#include "CoreFoundation/CFDate.h"
#include "Security/Security.h"
]-*/

/**
 * An iOS X509 certificate, which wraps a Security Framework-generated
 * certificate reference.
 *
 * @author Tom Ball
 */
public class IosX509Certificate extends X509Certificate {

  private long secCertificateRef;

  public IosX509Certificate(long secCertificateRef) {
    this.secCertificateRef = secCertificateRef;
  }

/*-[
- (void)dealloc {
  CFRelease((SecCertificateRef) secCertificateRef_);
#if ! __has_feature(objc_arc)
  [super dealloc];
#endif
}
]-*/

  @Override
  public void checkValidity() throws CertificateExpiredException,
      CertificateNotYetValidException {
    checkValidity(new Date());
  }

  @Override
  public native void checkValidity(Date date) throws CertificateExpiredException,
      CertificateNotYetValidException /*-[
    // Create an X509 trust policy for this certificate.
    SecPolicyRef policy = SecPolicyCreateBasicX509();
    SecCertificateRef certArray[1] = { (SecCertificateRef) secCertificateRef_ };
    CFArrayRef certs = CFArrayCreate(NULL, (const void **) certArray, 1, NULL);
    SecTrustRef trust;
    OSStatus status = SecTrustCreateWithCertificates(certs, policy, &trust);
    CFRelease(certs);
    CFRelease(policy);
    if (status != noErr) {
      NSString *errMsg =
          [NSString stringWithFormat:@"failed validating certificate, error: %d", (int) status];
      @throw AUTORELEASE([[JavaLangRuntimeException alloc] initWithNSString:errMsg]);
    }

    // Verify it is valid for the specified date.
    double requestedTime = [date getTime] / 1000.0;
    NSDate *nsDate = [NSDate dateWithTimeIntervalSince1970:requestedTime];
    NSLog(@"trust date: %@", nsDate);
    SecTrustResultType trustResult;
    SecTrustSetVerifyDate(trust, (ARCBRIDGE CFDateRef) nsDate);
    status = SecTrustEvaluate(trust, &trustResult);
    RELEASE_(nsDate);
    if (status != noErr) {
      NSString *errMsg =
          [NSString stringWithFormat:@"failed evaluating trust, error: %d", (int) status];
      @throw AUTORELEASE([[JavaLangRuntimeException alloc] initWithNSString:errMsg]);
    }
    if (trustResult != kSecTrustResultProceed && trustResult != kSecTrustResultUnspecified) {
      @throw AUTORELEASE([[JavaSecurityCertCertificateExpiredException alloc] init]);
    }
    // It's valid!
  ]-*/;

  @Override
  public native String toString() /*-[
    return (ARCBRIDGE NSString *) SecCertificateCopySubjectSummary(
        (SecCertificateRef) secCertificateRef_);
  ]-*/;

  @Override
  public native byte[] getEncoded() throws CertificateEncodingException /*-[
    CFDataRef dataRef = SecCertificateCopyData((SecCertificateRef) secCertificateRef_);
    CFIndex length = CFDataGetLength(dataRef);
    IOSByteArray *result = [IOSByteArray arrayWithLength:(jint)length];
    CFDataGetBytes(dataRef, CFRangeMake(0, length), (UInt8 *) result->buffer_);
    return result;
  ]-*/;

  // AssertionErrors are thrown for the two verify() methods, so that we aren't
  // accidentally "verifying" unchecked keys.

  @Override
  public void verify(PublicKey key) throws CertificateException,
      NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException,
      SignatureException {
    // TODO(tball): implement when key signing is implemented.
    throw new AssertionError("not implemented");
  }

  @Override
  public void verify(PublicKey key, String sigProvider)
      throws CertificateException, NoSuchAlgorithmException,
      InvalidKeyException, NoSuchProviderException, SignatureException {
    // TODO(tball): implement when key signing is implemented.
    throw new AssertionError("not implemented");
  }

  // The X509 certificate properties are not available from the iOS Security
  // Framework API. To get this properties would require an ASN.1 decoder, so
  // only implement these when they are required.

  @Override
  public PublicKey getPublicKey() {
    return null;
  }

  @Override
  public BigInteger getSerialNumber() {
    return null;
  }

  @Override
  public Set<String> getCriticalExtensionOIDs() {
    return null;
  }

  @Override
  public byte[] getExtensionValue(String oid) {
    return null;
  }

  @Override
  public Set<String> getNonCriticalExtensionOIDs() {
    return null;
  }

  @Override
  public boolean hasUnsupportedCriticalExtension() {
    return false;
  }

  @Override
  public int getVersion() {
    return 0;
  }

  @Override
  public Principal getIssuerDN() {
    return null;
  }

  @Override
  public Principal getSubjectDN() {
    return null;
  }

  @Override
  public Date getNotBefore() {
    return null;
  }

  @Override
  public Date getNotAfter() {
    return null;
  }

  @Override
  public byte[] getTBSCertificate() throws CertificateEncodingException {
    return null;
  }

  @Override
  public byte[] getSignature() {
    return null;
  }

  @Override
  public String getSigAlgName() {
    return null;
  }

  @Override
  public String getSigAlgOID() {
    return null;
  }

  @Override
  public byte[] getSigAlgParams() {
    return null;
  }

  @Override
  public boolean[] getIssuerUniqueID() {
    return null;
  }

  @Override
  public boolean[] getSubjectUniqueID() {
    return null;
  }

  @Override
  public boolean[] getKeyUsage() {
    return null;
  }

  @Override
  public int getBasicConstraints() {
    return 0;
  }
}
