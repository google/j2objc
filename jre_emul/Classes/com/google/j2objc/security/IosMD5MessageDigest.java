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

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;

/*-[
#import "CommonCrypto/CommonDigest.h"
]-*/

/**
 * MD5 message digest which is implemented using the iOS Security Framework.
 *
 * @author Tom Ball
 */
public class IosMD5MessageDigest extends MessageDigest implements Cloneable {

  private ByteArrayOutputStream buffer;

  public IosMD5MessageDigest() {
    super("MD5");
    buffer = new ByteArrayOutputStream();
  }

  @Override
  protected void engineUpdate(byte input) {
    buffer.write(input);
  }

  @Override
  protected void engineUpdate(byte[] input, int offset, int len) {
    buffer.write(input, offset, len);
  }

  @Override
  protected native byte[] engineDigest() /*-[
    IOSByteArray *bytes = [buffer_ toByteArray];
    unsigned char digest[CC_MD5_DIGEST_LENGTH];
    CC_MD5(bytes->buffer_, (unsigned) bytes->size_, digest);
    return [IOSByteArray arrayWithBytes:(jbyte *)digest count:CC_MD5_DIGEST_LENGTH];
  ]-*/;

  @Override
  protected void engineReset() {
    buffer.reset();
  }

  @Override
  protected native int engineGetDigestLength() /*-[
    return CC_MD5_DIGEST_LENGTH;
  ]-*/;
}
