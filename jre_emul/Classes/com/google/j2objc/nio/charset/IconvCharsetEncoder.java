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

package com.google.j2objc.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/*-[
#include <iconv.h>

#define BYTES_PER_CHAR (sizeof(jchar) / sizeof(jbyte))
]-*/

/**
 * Native charset encoder using libiconv.
 *
 * @author Keith Stanger
 */
public class IconvCharsetEncoder extends CharsetEncoder {

  private final long iconvName;
  private long iconvHandle;

  protected IconvCharsetEncoder(
      Charset charset, float averageBytesPerChar, float maxBytesPerChar, byte[] replacement,
      long iconvName) {
    super(charset, averageBytesPerChar, maxBytesPerChar, replacement, /* trusted */ true);
    this.iconvName = iconvName;
  }

  @Override
  protected native void implReset() /*-[
    iconv_close((iconv_t)self->iconvHandle_);
    self->iconvHandle_ = 0LL;
  ]-*/;

  @Override
  protected CoderResult implFlush(ByteBuffer out) {
    return encodeLoop(null, out);
  }

  @Override
  protected native CoderResult encodeLoop(CharBuffer inBuf, ByteBuffer outBuf) /*-[
    jint inSize = 0;
    if (inBuf) {
      inSize = [inBuf remaining];
      if (inSize <= 0) {
        return JavaNioCharsetCoderResult_get_UNDERFLOW();
      }
    }

    iconv_t coder = (iconv_t)self->iconvHandle_;
    if (coder == NULL) {
      coder = iconv_open(
          (const char *)self->iconvName_,
          CFByteOrderGetCurrent() == CFByteOrderLittleEndian ? "UTF-16LE" : "UTF-16BE");
      self->iconvHandle_ = (jlong)coder;
    }

    IOSCharArray *inArray = nil;
    char *inRaw = NULL;
    if (inBuf) {
      if ([inBuf hasArray]) {
        jint pos = [inBuf position];
        inRaw = (char *)&[inBuf array]->buffer_[[inBuf arrayOffset] + pos];
        [inBuf positionWithInt:pos + inSize];
      } else {
        inArray = [IOSCharArray newArrayWithLength:inSize];
        [inBuf getWithCharArray:inArray];
        inRaw = (char *)inArray->buffer_;
      }
    }
    size_t inRawBytes = inSize * BYTES_PER_CHAR;

    (void)nil_chk(outBuf);
    jint outSize = [outBuf remaining];
    IOSByteArray *outArray = nil;
    char *outRaw = NULL;
    size_t outRawBytes = outSize;
    if ([outBuf hasArray]) {
      outRaw = (char *)&[outBuf array]->buffer_[[outBuf arrayOffset] + [outBuf position]];
    } else if (outSize > 0) {
      outArray = [IOSByteArray newArrayWithLength:outSize];
      outRaw = (char *)outArray->buffer_;
    }

    size_t ret = iconv(coder, &inRaw, &inRawBytes, &outRaw, &outRawBytes);

    JavaNioCharsetCoderResult *result = JavaNioCharsetCoderResult_get_UNDERFLOW();

    if (ret == (size_t)-1) {
      switch (errno) {
        case EILSEQ:
        case EINVAL:
          result = JavaNioCharsetCoderResult_malformedForLengthWithInt_(1);
          break;
        case E2BIG:
          result = JavaNioCharsetCoderResult_get_OVERFLOW();
          break;
      }
    }

    if (inRawBytes > 0) {
      (void)nil_chk(inBuf);
      [inBuf positionWithInt:[inBuf position] - ((jint)inRawBytes / BYTES_PER_CHAR)];
    }
    jint encodedBytes = outSize - (jint)outRawBytes;
    if (encodedBytes > 0) {
      if (outArray) {
        [outBuf putWithByteArray:outArray withInt:0 withInt:encodedBytes];
      } else {
        [outBuf positionWithInt:[outBuf position] + encodedBytes];
      }
    }

    [inArray release];
    [outArray release];
    return result;
  ]-*/;

  protected native void finalize() /*-[
    iconv_close((iconv_t)self->iconvHandle_);
  ]-*/;
}
