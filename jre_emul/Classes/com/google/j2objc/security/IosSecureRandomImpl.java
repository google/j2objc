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

import java.security.SecureRandomSpi;

/*-[
#import "java/lang/InternalError.h"
#import <Security/SecRandom.h>
]-*/

/**
 * Secure random number provider, implemented using the iOS Security Framework.
 *
 * @author Tom Ball
 */
public class IosSecureRandomImpl extends SecureRandomSpi {

  @Override
  protected void engineSetSeed(byte[] seed) {
    /* not used */
    if (seed == null) {  // Unit test.
      throw new NullPointerException();
    }
  }

  @Override
  protected native void engineNextBytes(byte[] bytes) /*-[
    (void)nil_chk(bytes);
    int error = SecRandomCopyBytes(kSecRandomDefault, bytes->size_, (uint8_t *) bytes->buffer_);
    if (error != 0) {
      NSString *errorMsg =
          [NSString stringWithFormat:@"SecRandomCopyBytes error: %s", strerror(error)];
      @throw AUTORELEASE([[JavaLangInternalError alloc] initWithNSString:errorMsg]);
    }
  ]-*/;

  @Override
  protected byte[] engineGenerateSeed(int numBytes) {
    byte[] result = new byte[numBytes];
    engineNextBytes(result);
    return result;
  }

}
