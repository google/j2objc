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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactorySpi;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

/*-[
#import <Security/SecRandom.h>
#import "com/google/j2objc/security/cert/IosX509Certificate.h"
]-*/

/**
 * Certificate factory provider, implemented using the iOS Security Framework.
 *
 * @author Tom Ball
 */
public class IosCertificateFactory extends CertificateFactorySpi {

  private static final int BEGIN_CERT_LINE_LENGTH = "-----BEGIN CERTIFICATE-----\n".length();
  private static final int END_CERT_LINE_LENGTH = "-----END CERTIFICATE-----\n".length();

  @Override
  public Certificate engineGenerateCertificate(InputStream inStream)
      throws CertificateException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      byte[] buf = new byte[512];
      int count;
      while ((count = inStream.read(buf)) >= 0) {
        out.write(buf, 0, count);
      }
      inStream.close();
    } catch (IOException e) {
      throw new CertificateException(e);
    }
    return iosGenerateCertificate(maybeDecodeBase64(out.toByteArray()));
  }

  /**
   * Test whether array is a Base64-encoded certificate. If so, return
   * the decoded content instead of the specified array.
   *
   * @see CertificateFactorySpi#engineGenerateCertificate(InputStream)
   */
  private byte[] maybeDecodeBase64(byte[] byteArray) {
    try {
      String pem = new String(byteArray);
      // Remove required begin/end lines.
      pem = pem.substring(BEGIN_CERT_LINE_LENGTH, pem.length() - END_CERT_LINE_LENGTH);
      return Base64.getDecoder().decode(pem);
    } catch (Exception e) {
      // Not a valid PEM encoded certificate, return original array.
      return byteArray;
    }
  }

  private native Certificate iosGenerateCertificate(byte[] bytes) throws CertificateException /*-[
    NSData *data = AUTORELEASE([[NSData alloc] initWithBytes:bytes->buffer_ length:bytes->size_]);
    SecCertificateRef newCertificate =
        SecCertificateCreateWithData(NULL, (__bridge CFDataRef) data);
    if (!newCertificate) {
      @throw AUTORELEASE([[JavaSecurityCertCertificateException alloc]
                          initWithNSString:@"not a valid DER-encoded X.509 certificate"]);
    }
    return AUTORELEASE([[ComGoogleJ2objcSecurityCertIosX509Certificate alloc]
                        initWithLong:(long long) newCertificate]);
  ]-*/;

  @Override
  public Collection<? extends Certificate> engineGenerateCertificates(
      InputStream inStream) throws CertificateException {
    List<Certificate> result = new ArrayList<Certificate>();
    Certificate cert;
    while ((cert = engineGenerateCertificate(inStream)) != null) {
      result.add(cert);
    }
    return result;
  }

  @Override
  public CRL engineGenerateCRL(InputStream inStream) throws CRLException {
    //TODO(tball): implement when requested.
    return null;
  }

  @Override
  public Collection<? extends CRL> engineGenerateCRLs(InputStream inStream)
      throws CRLException {
    List<CRL> result = new ArrayList<CRL>();
    CRL crl;
    while ((crl = engineGenerateCRL(inStream)) != null) {
      result.add(crl);
    }
    return result;
  }
}
