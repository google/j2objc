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
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;

/*-[
#include <iconv.h>

#define BYTES_PER_CHAR (sizeof(jchar) / sizeof(jbyte))
]-*/

/**
 * Native charset decoder using libiconv.
 *
 * @author Keith Stanger
 */
public class IconvCharsetDecoder extends CharsetDecoder {

  private final long iconvName;
  private long iconvHandle;

  protected IconvCharsetDecoder(
      Charset charset, float averageCharsPerByte, float maxCharsPerByte, long iconvName) {
    super(charset, averageCharsPerByte, maxCharsPerByte);
    this.iconvName = iconvName;
  }

  @Override
  protected native void implReset() /*-[
    iconv_close((iconv_t)self->iconvHandle_);
    self->iconvHandle_ = 0LL;
  ]-*/;

  /*-[
  static jint getMalformedLength(iconv_t coder, char *inPos, size_t inRemaining) {
    // Enable discarding of illegal sequences.
    int discardIllegal = 1;
    iconvctl(coder, ICONV_SET_DISCARD_ILSEQ, &discardIllegal);

    char *in = inPos;
    size_t inSize = inRemaining;
    char outBuf[0];
    char *out = outBuf;
    size_t outSize = 0;
    size_t ret = iconv(coder, &in, &inSize, &out, &outSize);
    int malformedResult = ret == (size_t)-1 ? errno : 0;
    // The number of illegal bytes read.
    size_t malformedLength = inRemaining - inSize;

    // Find the smallest number of bytes that will yield the same result when advanced by that
    // amount.
    for (int i = 1; i < malformedLength; i++) {
      in = inPos + i;
      size_t inSize2 = inRemaining - i;
      ret = iconv(coder, &in, &inSize2, &out, &outSize);
      int newResult = ret == (size_t)-1 ? errno : 0;
      if (newResult == malformedResult && inSize2 == inSize) {
        in = inPos;
        inSize = i + 1;
        ret = iconv(coder, &in, &inSize, &out, &outSize);
        if (ret != (size_t)-1 || errno != EINVAL || inSize != i + 1) {
          malformedLength = i;
          break;
        }
      }
    }

    // Reset discarding of illegal sequences to off.
    discardIllegal = 0;
    iconvctl(coder, ICONV_SET_DISCARD_ILSEQ, &discardIllegal);

    return (jint)malformedLength;
  }
  ]-*/

  @Override
  protected native CoderResult decodeLoop(ByteBuffer inBuf, CharBuffer outBuf) /*-[
    jint inSize = [nil_chk(inBuf) remaining];
    if (inSize <= 0) {
      return JavaNioCharsetCoderResult_get_UNDERFLOW();
    }

    iconv_t coder = (iconv_t)self->iconvHandle_;
    if (coder == NULL) {
      coder = iconv_open(
          CFByteOrderGetCurrent() == CFByteOrderLittleEndian ? "UTF-16LE" : "UTF-16BE",
          (const char *)self->iconvName_);
      self->iconvHandle_ = (jlong)coder;
    }

    IOSByteArray *inArray = nil;
    char *inRaw;
    if ([inBuf hasArray]) {
      jint pos = [inBuf position];
      inRaw = (char *)&[inBuf array]->buffer_[[inBuf arrayOffset] + pos];
      [inBuf positionWithInt:pos + inSize];
    } else {
      inArray = [IOSByteArray newArrayWithLength:inSize];
      [inBuf getWithByteArray:inArray];
      inRaw = (char *)inArray->buffer_;
    }
    size_t inRawBytes = inSize;

    jint outSize = [nil_chk(outBuf) remaining];
    IOSCharArray *outArray = nil;
    char *outRaw = NULL;
    size_t outRawBytes = outSize * BYTES_PER_CHAR;
    if ([outBuf hasArray]) {
      outRaw = (char *)&[outBuf array]->buffer_[[outBuf arrayOffset] + [outBuf position]];
    } else if (outSize > 0) {
      outArray = [IOSCharArray newArrayWithLength:outSize];
      outRaw = (char *)outArray->buffer_;
    }

    size_t ret = iconv(coder, &inRaw, &inRawBytes, &outRaw, &outRawBytes);

    JavaNioCharsetCoderResult *result = JavaNioCharsetCoderResult_get_UNDERFLOW();

    if (ret == (size_t)-1) {
      switch (errno) {
        case EINVAL:
          // Incomplete multibyte sequence at the end of input, return UNDERFLOW.
          break;
        case EILSEQ:
          result = JavaNioCharsetCoderResult_malformedForLengthWithInt_(
              getMalformedLength(coder, inRaw, inRawBytes));
          break;
        case E2BIG:
          result = JavaNioCharsetCoderResult_get_OVERFLOW();
          break;
      }
    }

    if (inRawBytes > 0) {
      [inBuf positionWithInt:[inBuf position] - (jint)inRawBytes];
    }
    jint encodedChars = outSize - ((jint)outRawBytes / BYTES_PER_CHAR);
    if (encodedChars > 0) {
      if (outArray) {
        [outBuf putWithCharArray:outArray withInt:0 withInt:encodedChars];
      } else {
        [outBuf positionWithInt:[outBuf position] + encodedChars];
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
