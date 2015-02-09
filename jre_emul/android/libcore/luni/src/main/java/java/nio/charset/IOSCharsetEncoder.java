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
 * iOS native charset encoder.
 *
 * @author Tom Ball
 */
public class IOSCharsetEncoder extends CharsetEncoder {
  private char[] inBuffer;
  private byte[] byteBuffer;
  private int outIndex;

  protected IOSCharsetEncoder(Charset charset, float maxBytesPerChar) {
    super(charset, maxBytesPerChar, maxBytesPerChar, new byte[] { (byte) '?' });
  }

  @Override
  public ByteBuffer encode(CharBuffer in) throws CharacterCodingException {
    return ByteBuffer.wrap(encodeImpl(in));
  }

  @Override
  protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
    if (byteBuffer != null) {
      while (out.hasRemaining() && outIndex < byteBuffer.length) {
        out.put(byteBuffer[outIndex++]);
      }
      if (outIndex == byteBuffer.length){
        byteBuffer = null;
        return CoderResult.UNDERFLOW;
      } else {
        return CoderResult.OVERFLOW;
      }
    } else if (in.hasRemaining()) {
      byte[] b = encodeImpl(in);
      if (b.length == 0) {
        return CoderResult.UNDERFLOW;
      }
      if (out.remaining() < b.length){
        byteBuffer = b;
        outIndex = 0;
        return encodeLoop(in, out);
      } else {
        out.put(b);
      }
    }
    return CoderResult.UNDERFLOW;  // All input data was decoded.
  }

  private byte[] encodeImpl(CharBuffer in) {
    Charset cs = charset();
    if (!(cs instanceof IOSCharset)) {
      throw new UnsupportedCharsetException(cs.name());
    }
    char[] chars;
    int i;
    if (inBuffer != null) {
      i = inBuffer.length;
      chars = new char[i + in.remaining()];
      System.arraycopy(inBuffer, 0, chars, 0, inBuffer.length);
      inBuffer = null;
    } else {
      if (((IOSCharset) cs).nsEncoding() == /* NSUnicodeStringEncoding */ 10L) {
        // Prepend required BOM for Java's big-endian encoding default.
        chars = new char[in.remaining() + 1];
        chars[0] = (char) 0xFEFF;
        i = 1;
      } else {
        i = 0;
        chars = new char[in.remaining()];
      }
    }
    in.get(chars, i, chars.length - i);
    byte[] bytes = encode(chars, ((IOSCharset) cs).nsEncoding());
    if (bytes.length == 0) {
      inBuffer = chars;
    } else {
      inBuffer = null;
    }
    return bytes;
  }

  private static native byte[] encode(char[] in, long encoding) /*-[
    if (encoding == NSUnicodeStringEncoding) {
      // Force encoding to be big-endian to match Java encoding.
      encoding = NSUTF16BigEndianStringEncoding;
    }
    NSString *s = [NSString stringWithCharacters:inArg->buffer_
                                          length:inArg->size_];
    NSData *data = [s dataUsingEncoding:(NSStringEncoding) encoding allowLossyConversion:NO];
    return [IOSByteArray arrayWithBytes:(const jbyte *)[data bytes] count:(jint)[data length]];
  ]-*/;
}
