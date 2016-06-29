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

// ASN.1 Decoder
import org.apache.harmony.security.utils.AlgNameMapper;
import org.apache.harmony.security.x509.Certificate;
import org.apache.harmony.security.x509.Extension;
import org.apache.harmony.security.x509.Extensions;
import org.apache.harmony.security.x509.TBSCertificate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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
#include "java/security/cert/CertificateExpiredException.h"
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

  private Certificate certificate;
  private TBSCertificate tbsCert;
  private Extensions extensions;

  public IosX509Certificate(long secCertificateRef) {
    this.secCertificateRef = secCertificateRef;
  }

  /**
   * This implementation is modeled after Apache Harmony's X509CertImpl.
   */
  public void lazyDecoding() {
    if (this.certificate == null) {
      try {
        // decode the Certificate object
        this.certificate = (Certificate)
            Certificate.ASN1.decode(new ByteArrayInputStream(getEncoded()));

        // cache the values of TBSCertificate and Extensions
        this.tbsCert = certificate.getTbsCertificate();
        this.extensions = tbsCert.getExtensions();
      } catch (CertificateEncodingException e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  protected native void finalize() throws Throwable /*-[
    CFRelease((SecCertificateRef) secCertificateRef_);
  ]-*/;

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
      @throw create_JavaLangRuntimeException_initWithNSString_(errMsg);
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
      @throw create_JavaLangRuntimeException_initWithNSString_(errMsg);
    }
    if (trustResult != kSecTrustResultProceed && trustResult != kSecTrustResultUnspecified) {
      @throw create_JavaSecurityCertCertificateExpiredException_init();
    }
    // It's valid!
  ]-*/;


  @Override
  public String toString() {
    lazyDecoding();
    return certificate.toString();
  }

  /**
   * This can be used for full certificate pinning
   */
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

  // The X509 certificate properties are indirectly available from the iOS Security
  // Framework API. The ASN.1 decoder from Apache Harmony is used to expand the raw
  // format returned by the Security Framework.

  // #getPublicKey#getEncoded can be used for public key pinning.
  @Override
  public PublicKey getPublicKey() {
    lazyDecoding();
    return tbsCert.getSubjectPublicKeyInfo().getPublicKey();
  }

  @Override
  public BigInteger getSerialNumber() {
    lazyDecoding();
    return tbsCert.getSerialNumber();
  }

  @Override
  public Set<String> getCriticalExtensionOIDs() {
    lazyDecoding();
    return extensions.getCriticalExtensions();
  }

  @Override
  public byte[] getExtensionValue(String oid) {
    lazyDecoding();
    Extension ext = extensions.getExtensionByOID(oid);
    return (ext == null) ? null : ext.getRawExtnValue();
  }

  @Override
  public Set<String> getNonCriticalExtensionOIDs() {
    lazyDecoding();
    return extensions.getNonCriticalExtensions();
  }

  @Override
  public boolean hasUnsupportedCriticalExtension() {
    lazyDecoding();
    return extensions.hasUnsupportedCritical();
  }

  @Override
  public int getVersion() {
    lazyDecoding();
    return tbsCert.getVersion();
  }

  @Override
  public Principal getIssuerDN() {
    lazyDecoding();
    return tbsCert.getIssuer().getX500Principal();
  }

  @Override
  public Principal getSubjectDN() {
    lazyDecoding();
    return tbsCert.getSubject().getX500Principal();
  }

  @Override
  public Date getNotBefore() {
    lazyDecoding();
    return new Date(tbsCert.getValidity().getNotBefore().getTime());
  }

  @Override
  public Date getNotAfter() {
    lazyDecoding();
    return new Date(tbsCert.getValidity().getNotAfter().getTime());
  }

  @Override
  public byte[] getTBSCertificate() throws CertificateEncodingException {
    lazyDecoding();
    return tbsCert.getEncoded();
  }

  @Override
  public byte[] getSignature() {
    lazyDecoding();
    return certificate.getSignatureValue();
  }

  @Override
  public String getSigAlgName() {
    lazyDecoding();

    // If info was not retrieved (and cached), do it now:
    final String sigAlgOID = tbsCert.getSignature().getAlgorithm();

    // Retrieve the name of the signing algorithm.
    String sigAlgName = AlgNameMapper.map2AlgName(sigAlgOID);
    if (sigAlgName == null) {
      // if could not be found, use OID as a name
      sigAlgName = sigAlgOID;
    }
    return sigAlgName;
  }

  @Override
  public String getSigAlgOID() {
    // See org.apache.harmony.security.provider.cert.X509CertImpl.
    return getSigAlgName();
  }

  @Override
  public byte[] getSigAlgParams() {
    lazyDecoding();
    return tbsCert.getSignature().getParameters();
  }

  @Override
  public boolean[] getIssuerUniqueID() {
    lazyDecoding();
    return tbsCert.getIssuerUniqueID();
  }

  @Override
  public boolean[] getSubjectUniqueID() {
    lazyDecoding();
    return tbsCert.getSubjectUniqueID();
  }

  @Override
  public boolean[] getKeyUsage() {
    lazyDecoding();
    return extensions.valueOfKeyUsage();
  }

  @Override
  public int getBasicConstraints() {
    lazyDecoding();
    return extensions.valueOfBasicConstraints();
  }
}
