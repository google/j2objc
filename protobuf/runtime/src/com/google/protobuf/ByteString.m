// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
// https://developers.google.com/protocol-buffers/
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are
// met:
//
//     * Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above
// copyright notice, this list of conditions and the following disclaimer
// in the documentation and/or other materials provided with the
// distribution.
//     * Neither the name of Google Inc. nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
// A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
// OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

//  Created by Keith Stanger on 11/28/12.
//
//  Hand written counterpart of com.google.protobuf.ByteString.

#import "com/google/protobuf/ByteString.h"

J2OBJC_INITIALIZED_DEFN(ComGoogleProtobufByteString)

ComGoogleProtobufByteString *ComGoogleProtobufByteString_EMPTY_;

ComGoogleProtobufByteString *CGPNewByteString(int len) {
  CGPByteString *byteString = NSAllocateObject([CGPByteString class], len, nil);
  byteString->size_ = len;
  return byteString;
}

ComGoogleProtobufByteString *ComGoogleProtobufByteString_copyFromWithByteArray_(
    IOSByteArray *bytes) {
  nil_chk(bytes);  // Ensure Java compatibility.
  CGPByteString *byteString = CGPNewByteString((int)bytes->size_);
  memcpy(byteString->buffer_, bytes->buffer_, bytes->size_);
  return [byteString autorelease];
}

@implementation ComGoogleProtobufByteString

+ (ComGoogleProtobufByteString *)
    copyFromWithByteArray:(IOSByteArray *)bytes
    OBJC_METHOD_FAMILY_NONE {
  return ComGoogleProtobufByteString_copyFromWithByteArray_(bytes);
}

- (IOSByteArray *)toByteArray {
  return [IOSByteArray arrayWithBytes:buffer_ count:size_];
}

- (NSString *)toStringWithNSString:(NSString *)charsetName {
  return [NSString stringWithBytes:[self toByteArray] charsetName:charsetName];
}

- (NSString *)toStringUtf8 {
  return [[[NSString alloc] initWithBytes:buffer_
                                   length:size_
                                 encoding:NSUTF8StringEncoding] autorelease];
}

- (BOOL)isEqual:(id)other {
  if (other == self) {
    return YES;
  }
  if (!(object_getClass(other) == [CGPByteString class])) {
    return NO;
  }
  CGPByteString *otherByteString = (CGPByteString *)other;
  if (size_ != otherByteString->size_) {
    return NO;
  }
  if (size_ == 0) {
    return YES;
  }
  return memcmp(buffer_, otherByteString->buffer_, size_) == 0;
}

- (NSUInteger)hash {
  int h = size_;
  for (int i = 0; i < size_; i++) {
    h = h * 31 + buffer_[i];
  }
  return h;
}

+ (void)initialize {
  if (self == [ComGoogleProtobufByteString class]) {
    ComGoogleProtobufByteString_EMPTY_ = CGPNewByteString(0);
    J2OBJC_SET_INITIALIZED(ComGoogleProtobufByteString)
  }
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufByteString)
