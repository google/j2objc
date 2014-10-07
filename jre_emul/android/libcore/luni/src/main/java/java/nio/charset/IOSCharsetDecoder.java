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

package java.nio.charset;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

/**
 * iOS native charset decoder.
 *
 * @author Tom Ball
 */
public class IOSCharsetDecoder extends CharsetDecoder {
  private byte[] inBuffer;
  private char[] charBuffer;
  private int outIndex;

  protected IOSCharsetDecoder(Charset charset) {
    super(charset, 1, 4);
  }

  @Override
  protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
    if (charBuffer != null) {
      while (out.hasRemaining() && outIndex < charBuffer.length) {
        out.put(charBuffer[outIndex++]);
      }
      if (outIndex == charBuffer.length){
        charBuffer = null;
      }
    } else if (in.hasRemaining()) {
      String s = decodeImpl(in);
      if (s.isEmpty()) {
        return CoderResult.UNDERFLOW;
      }
      if (out.remaining() < s.length()) {
        charBuffer = s.toCharArray();
        outIndex = 0;
        return decodeLoop(in, out);
      } else {
        out.put(s);
      }
    }
    return CoderResult.UNDERFLOW;  // All input data was decoded.
  }

  @Override
  public CharBuffer decode(ByteBuffer in) throws CharacterCodingException {
    String s = decodeImpl(in);
    return CharBuffer.wrap(s);
  }

  private String decodeImpl(ByteBuffer in) {
    Charset cs = charset();
    if (!(cs instanceof IOSCharset)) {
      throw new UnsupportedCharsetException(cs.name());
    }
    byte[] bytes;
    int i;
    if (inBuffer != null) {
      i = inBuffer.length;
      bytes = new byte[i + in.remaining()];
      System.arraycopy(inBuffer, 0, bytes, 0, inBuffer.length);
      inBuffer = null;
    } else {
      i = 0;
      bytes = new byte[in.remaining()];
    }
    in.get(bytes, i, bytes.length - i);
    String s = decode(bytes, ((IOSCharset) cs).nsEncoding());
    if (s.isEmpty()) {
      inBuffer = bytes;
    } else {
      inBuffer = null;
    }
    return s;
  }

  private static native String decode(byte[] in, long encoding) /*-[
    NSString *result = AUTORELEASE([[NSString alloc] initWithBytes:inArg->buffer_
                                                            length:inArg->size_
                                                          encoding:(NSStringEncoding) encoding]);
    // NSString will return nil if the byte sequence can't be encoded. Since
    // that may happen when a byte sequence is larger than a buffer, return
    // an empty string so that the caller stashes the partial buffer.
    return result ? result : @"";
  ]-*/;

  public int available() {
    return charBuffer != null ? (charBuffer.length - outIndex) : 0;
  }
}
