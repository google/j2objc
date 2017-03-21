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

import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.util.Date;
import sun.security.x509.X509CertImpl;

/*-[
#include "NSDataInputStream.h"
#include "java/lang/RuntimeException.h"
#include "java/security/cert/CertificateExpiredException.h"

#include "CoreFoundation/CFDate.h"
#include "Security/Security.h"
]-*/

/**
 * An iOS X509 certificate, which wraps a Security Framework-generated
 * certificate reference.
 *
 * @author Tom Ball
 */
public class IosX509Certificate extends X509CertImpl {

  private long secCertificateRef;

  public IosX509Certificate(long secCertificateRef) throws CertificateException {
    super(getEncodedImpl(secCertificateRef));
    this.secCertificateRef = secCertificateRef;
  }

  @Override
  public byte[] getEncoded() throws CertificateEncodingException {
    return getEncodedImpl(secCertificateRef);
  }

  static native byte[] getEncodedImpl(long secRef) /*-[
    CFDataRef dataRef = SecCertificateCopyData((SecCertificateRef) secRef);
    CFIndex length = CFDataGetLength(dataRef);
    IOSByteArray *result = [IOSByteArray arrayWithLength:(jint)length];
    CFDataGetBytes(dataRef, CFRangeMake(0, length), (UInt8 *) result->buffer_);
    return result;
  ]-*/;

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
  protected native void finalize() throws Throwable /*-[
    CFRelease((SecCertificateRef) secCertificateRef_);
  ]-*/;
}
