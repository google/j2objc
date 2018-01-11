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

  // Malloc'd CommonCrypto context.
  protected long ctx;

  public IosMD5MessageDigest() {
    super("MD5");
    allocContext();
  }

  @Override
  protected native int engineGetDigestLength() /*-[
    return CC_MD5_DIGEST_LENGTH;
  ]-*/;

  private native void allocContext() /*-[
    self->ctx_ = (jlong)calloc(1, sizeof(CC_MD5_CTX));
    [self engineReset];
  ]-*/;

  @Override
  protected native void engineReset() /*-[
    CC_MD5_Init((CC_MD5_CTX *)ctx_);
  ]-*/;

  @Override
  protected native void engineUpdate(byte input) /*-[
    CC_MD5_Update((CC_MD5_CTX *)ctx_, &input, 1);
  ]-*/;

  @Override
  protected native void engineUpdate(byte[] input, int offset, int len) /*-[
    IOSArray_checkRange(input->size_, offset, len);
    CC_MD5_Update((CC_MD5_CTX *)ctx_, input->buffer_ + offset, len);
  ]-*/;

  @Override
  protected native byte[] engineDigest() /*-[
    IOSByteArray *md = [IOSByteArray arrayWithLength:CC_MD5_DIGEST_LENGTH];
    CC_MD5_Final((unsigned char *)md->buffer_, (CC_MD5_CTX *)ctx_);
    [self engineReset];
    return md;
  ]-*/;

  public native Object clone() throws CloneNotSupportedException /*-[
    ComGoogleJ2objcSecurityIosMD5MessageDigest *obj =
        (ComGoogleJ2objcSecurityIosMD5MessageDigest *) [super java_clone];
    ComGoogleJ2objcSecurityIosMD5MessageDigest_allocContext(obj);
    if (ctx_ != 0LL) {
      memcpy((void *)obj->ctx_, (const void *)ctx_, sizeof(CC_MD5_CTX));
    }
    return obj;
  ]-*/;

  native void close() /*-[
    if (ctx_ != 0LL) {
      free((void *)ctx_);
      ctx_ = 0LL;
    }
  ]-*/;

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }
}
