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
 * Secure hash algorithm (SHA) message digest, which is implemented using
 * the iOS Security Framework.
 *
 * @author Tom Ball
 */
public abstract class IosSHAMessageDigest extends MessageDigest implements Cloneable {

  // Malloc'd CommonCrypto context.
  protected long shaCtx;

  public IosSHAMessageDigest(String algorithm) {
    super(algorithm);
  }

  native void close() /*-[
    if (shaCtx_ != 0LL) {
      free((void *)shaCtx_);
      shaCtx_ = 0LL;
    }
  ]-*/;

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

  public static class SHA1 extends IosSHAMessageDigest {

    public SHA1() {
      super("SHA-1");
      allocContext();
    }

    @Override
    protected native int engineGetDigestLength() /*-[
      return CC_SHA1_DIGEST_LENGTH;
    ]-*/;

    private native void allocContext() /*-[
      self->shaCtx_ = (jlong)calloc(1, sizeof(CC_SHA1_CTX));
      [self engineReset];
    ]-*/;

    @Override
    protected native void engineReset() /*-[
      CC_SHA1_Init((CC_SHA1_CTX *)shaCtx_);
    ]-*/;

    @Override
    protected native void engineUpdate(byte input) /*-[
      CC_SHA1_Update((CC_SHA1_CTX *)shaCtx_, &input, 1);
    ]-*/;

    @Override
    protected native void engineUpdate(byte[] input, int offset, int len) /*-[
      IOSArray_checkRange(input->size_, offset, len);
      CC_SHA1_Update((CC_SHA1_CTX *)shaCtx_, input->buffer_ + offset, len);
    ]-*/;

    @Override
    protected native byte[] engineDigest() /*-[
      IOSByteArray *md = [IOSByteArray arrayWithLength:CC_SHA1_DIGEST_LENGTH];
      CC_SHA1_Final((unsigned char *)md->buffer_, (CC_SHA1_CTX *)shaCtx_);
      [self engineReset];
      return md;
    ]-*/;

    public native Object clone() throws CloneNotSupportedException /*-[
      ComGoogleJ2objcSecurityIosSHAMessageDigest_SHA1 *obj =
          (ComGoogleJ2objcSecurityIosSHAMessageDigest_SHA1 *) [super java_clone];
      ComGoogleJ2objcSecurityIosSHAMessageDigest_SHA1_allocContext(obj);
      if (shaCtx_ != 0LL) {
        memcpy((void *)obj->shaCtx_, (const void *)shaCtx_, sizeof(CC_SHA1_CTX));
      }
      return obj;
    ]-*/;
  }

  public static class SHA256 extends IosSHAMessageDigest {

    public SHA256() {
      super("SHA-256");
      allocContext();
    }

    @Override
    protected native int engineGetDigestLength() /*-[
      return CC_SHA256_DIGEST_LENGTH;
    ]-*/;

    private native void allocContext() /*-[
      self->shaCtx_ = (jlong)calloc(1, sizeof(CC_SHA256_CTX));
      [self engineReset];
    ]-*/;

    @Override
    protected native void engineReset() /*-[
      CC_SHA256_Init((CC_SHA256_CTX *)shaCtx_);
    ]-*/;

    @Override
    protected native void engineUpdate(byte input) /*-[
      CC_SHA256_Update((CC_SHA256_CTX *)shaCtx_, &input, 1);
    ]-*/;

    @Override
    protected native void engineUpdate(byte[] input, int offset, int len) /*-[
      IOSArray_checkRange(input->size_, offset, len);
      CC_SHA256_Update((CC_SHA256_CTX *)shaCtx_, input->buffer_ + offset, len);
    ]-*/;

    @Override
    protected native byte[] engineDigest() /*-[
      IOSByteArray *md = [IOSByteArray arrayWithLength:CC_SHA256_DIGEST_LENGTH];
      CC_SHA256_Final((unsigned char *)md->buffer_, (CC_SHA256_CTX *)shaCtx_);
      [self engineReset];
      return md;
    ]-*/;

    public native Object clone() throws CloneNotSupportedException /*-[
      ComGoogleJ2objcSecurityIosSHAMessageDigest_SHA256 *obj =
          (ComGoogleJ2objcSecurityIosSHAMessageDigest_SHA256 *) [super java_clone];
      ComGoogleJ2objcSecurityIosSHAMessageDigest_SHA256_allocContext(obj);
      if (shaCtx_ != 0LL) {
        memcpy((void *)obj->shaCtx_, (const void *)shaCtx_, sizeof(CC_SHA256_CTX));
      }
      return obj;
    ]-*/;
  }

  public static class SHA384 extends IosSHAMessageDigest {

    public SHA384() {
      super("SHA-384");
      allocContext();
    }

    @Override
    protected native int engineGetDigestLength() /*-[
      return CC_SHA384_DIGEST_LENGTH;
    ]-*/;

    private native void allocContext() /*-[
      // SHA384 and SHA512 use the same context struct.
      self->shaCtx_ = (jlong)calloc(1, sizeof(CC_SHA512_CTX));
      [self engineReset];
    ]-*/;

    @Override
    protected native void engineReset() /*-[
      CC_SHA384_Init((CC_SHA512_CTX *)shaCtx_);
    ]-*/;

    @Override
    protected native void engineUpdate(byte input) /*-[
      CC_SHA384_Update((CC_SHA512_CTX *)shaCtx_, &input, 1);
    ]-*/;

    @Override
    protected native void engineUpdate(byte[] input, int offset, int len) /*-[
      IOSArray_checkRange(input->size_, offset, len);
      CC_SHA384_Update((CC_SHA512_CTX *)shaCtx_, input->buffer_ + offset, len);
    ]-*/;

    @Override
    protected native byte[] engineDigest() /*-[
      IOSByteArray *md = [IOSByteArray arrayWithLength:CC_SHA384_DIGEST_LENGTH];
      CC_SHA384_Final((unsigned char *)md->buffer_, (CC_SHA512_CTX *)shaCtx_);
      [self engineReset];
      return md;
    ]-*/;

    public native Object clone() throws CloneNotSupportedException /*-[
      ComGoogleJ2objcSecurityIosSHAMessageDigest_SHA384 *obj =
          (ComGoogleJ2objcSecurityIosSHAMessageDigest_SHA384 *) [super java_clone];
      ComGoogleJ2objcSecurityIosSHAMessageDigest_SHA384_allocContext(obj);
      if (shaCtx_ != 0LL) {
        memcpy((void *)obj->shaCtx_, (const void *)shaCtx_, sizeof(CC_SHA512_CTX));
      }
      return obj;
    ]-*/;
  }

  public static class SHA512 extends IosSHAMessageDigest {

    public SHA512() {
      super("SHA-512");
      allocContext();
    }

    @Override
    protected native int engineGetDigestLength() /*-[
      return CC_SHA512_DIGEST_LENGTH;
    ]-*/;

    private native void allocContext() /*-[
      self->shaCtx_ = (jlong)calloc(1, sizeof(CC_SHA512_CTX));
      [self engineReset];
    ]-*/;

    @Override
    protected native void engineReset() /*-[
      CC_SHA512_Init((CC_SHA512_CTX *)shaCtx_);
    ]-*/;

    @Override
    protected native void engineUpdate(byte input) /*-[
      CC_SHA512_Update((CC_SHA512_CTX *)shaCtx_, &input, 1);
    ]-*/;

    @Override
    protected native void engineUpdate(byte[] input, int offset, int len) /*-[
      IOSArray_checkRange(input->size_, offset, len);
      CC_SHA512_Update((CC_SHA512_CTX *)shaCtx_, input->buffer_ + offset, len);
    ]-*/;

    @Override
    protected native byte[] engineDigest() /*-[
      IOSByteArray *md = [IOSByteArray arrayWithLength:CC_SHA512_DIGEST_LENGTH];
      CC_SHA512_Final((unsigned char *)md->buffer_, (CC_SHA512_CTX *)shaCtx_);
      [self engineReset];
      return md;
    ]-*/;

    public native Object clone() throws CloneNotSupportedException /*-[
      ComGoogleJ2objcSecurityIosSHAMessageDigest_SHA512 *obj =
          (ComGoogleJ2objcSecurityIosSHAMessageDigest_SHA512 *) [super java_clone];
      ComGoogleJ2objcSecurityIosSHAMessageDigest_SHA512_allocContext(obj);
      if (shaCtx_ != 0LL) {
        memcpy((void *)obj->shaCtx_, (const void *)shaCtx_, sizeof(CC_SHA512_CTX));
      }
      return obj;
    ]-*/;
  }
}
