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

// Counterpart for com.google.protobuf.ByteString.

#ifndef __ComGoogleProtobufByteString_H__
#define __ComGoogleProtobufByteString_H__

#include "J2ObjC_header.h"

@class IOSByteArray;

@interface ComGoogleProtobufByteString : NSObject {
 @package
  jint size_;
  int8_t buffer_[0];
}

+ (ComGoogleProtobufByteString *)
    copyFromWithByteArray:(IOSByteArray *)bytes OBJC_METHOD_FAMILY_NONE;

- (jbyte)byteAtWithInt:(jint)index;
- (IOSByteArray *)toByteArray;
- (NSString *)toStringWithNSString:(NSString *)charsetName;
- (NSString *)toStringUtf8;

@end

typedef ComGoogleProtobufByteString CGPByteString;

FOUNDATION_EXPORT ComGoogleProtobufByteString *ComGoogleProtobufByteString_copyFromWithByteArray_(
    IOSByteArray *bytes);

FOUNDATION_EXPORT ComGoogleProtobufByteString
    *ComGoogleProtobufByteString_copyFromUtf8WithNSString_(NSString *text);

FOUNDATION_EXPORT ComGoogleProtobufByteString *CGPNewByteString(jint len);

J2OBJC_STATIC_INIT(ComGoogleProtobufByteString)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufByteString)

FOUNDATION_EXPORT ComGoogleProtobufByteString *ComGoogleProtobufByteString_EMPTY_;
J2OBJC_STATIC_FIELD_GETTER(ComGoogleProtobufByteString, EMPTY_, ComGoogleProtobufByteString *)

#endif // __ComGoogleProtobufByteString_H__
