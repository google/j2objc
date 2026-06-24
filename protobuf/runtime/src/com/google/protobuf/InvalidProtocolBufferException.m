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

//  Created by Michelle Chen on 12/07/12.
//
// Counterpart for com.google.protobuf.InvalidProtocolBufferException.

#import "InvalidProtocolBufferException.h"

#import "IOSClass.h"
#import "J2ObjC_source.h"
#import "java/lang/Exception.h"

@implementation ComGoogleProtobufInvalidProtocolBufferException {
  ComGoogleProtobufMessageLite *unfinishedMessage_;
}

- (instancetype)initWithNSString:(NSString *)s {
  if ((self = [super initWithNSString:s])) {
    unfinishedMessage_ = nil;
  }
  return self;
}

- (instancetype)initWithJavaLangThrowable:(JavaLangThrowable *)cause {
  if ((self = [super initWithJavaLangThrowable:cause])) {
    unfinishedMessage_ = nil;
  }
  return self;
}

- (instancetype)initWithNSString:(NSString *)detailMessage
           withJavaLangThrowable:(JavaLangThrowable *)cause {
  if ((self = [super initWithNSString:detailMessage
                withJavaLangThrowable:cause])) {
    unfinishedMessage_ = nil;
  }
  return self;
}

- (ComGoogleProtobufMessageLite *)getUnfinishedMessage {
  return unfinishedMessage_;
}

- (void)setUnfinishedMessage:(ComGoogleProtobufMessageLite *)message {
  unfinishedMessage_ = message;
}

@end

ComGoogleProtobufInvalidProtocolBufferException *
new_ComGoogleProtobufInvalidProtocolBufferException_initWithNSString_(NSString *s) {
  ComGoogleProtobufInvalidProtocolBufferException *self =
      [ComGoogleProtobufInvalidProtocolBufferException alloc];
  return [self initWithNSString:s];
}

ComGoogleProtobufInvalidProtocolBufferException *
create_ComGoogleProtobufInvalidProtocolBufferException_initWithNSString_(NSString *s) {
  return AUTORELEASE(new_ComGoogleProtobufInvalidProtocolBufferException_initWithNSString_(s));
}

ComGoogleProtobufInvalidProtocolBufferException *
new_ComGoogleProtobufInvalidProtocolBufferException_initWithJavaLangThrowable_(
    JavaLangThrowable *cause) {
  ComGoogleProtobufInvalidProtocolBufferException *self =
      [ComGoogleProtobufInvalidProtocolBufferException alloc];
  return [self initWithJavaLangThrowable:cause];
}

ComGoogleProtobufInvalidProtocolBufferException *
create_ComGoogleProtobufInvalidProtocolBufferException_initWithJavaLangThrowable_(
    JavaLangThrowable *cause) {
  return AUTORELEASE(
      new_ComGoogleProtobufInvalidProtocolBufferException_initWithJavaLangThrowable_(cause));
}

ComGoogleProtobufInvalidProtocolBufferException *
new_ComGoogleProtobufInvalidProtocolBufferException_initWithNSString_withJavaLangThrowable_(
    NSString *detailMessage, JavaLangThrowable *cause) {
  ComGoogleProtobufInvalidProtocolBufferException *self =
      [ComGoogleProtobufInvalidProtocolBufferException alloc];
  return [self initWithNSString:detailMessage withJavaLangThrowable:cause];
}

ComGoogleProtobufInvalidProtocolBufferException *
create_ComGoogleProtobufInvalidProtocolBufferException_initWithNSString_withJavaLangThrowable_(
    NSString *detailMessage, JavaLangThrowable *cause) {
  return AUTORELEASE(
      new_ComGoogleProtobufInvalidProtocolBufferException_initWithNSString_withJavaLangThrowable_(
          detailMessage, cause));
}

ComGoogleProtobufInvalidProtocolBufferException *
new_ComGoogleProtobufInvalidProtocolBufferException_initWithNSString_withJavaLangException_(
    NSString *detailMessage, JavaLangException *cause) {
  return new_ComGoogleProtobufInvalidProtocolBufferException_initWithNSString_withJavaLangThrowable_(
      detailMessage, cause);
}

ComGoogleProtobufInvalidProtocolBufferException *
create_ComGoogleProtobufInvalidProtocolBufferException_initWithNSString_withJavaLangException_(
    NSString *detailMessage, JavaLangException *cause) {
  return create_ComGoogleProtobufInvalidProtocolBufferException_initWithNSString_withJavaLangThrowable_(
      detailMessage, cause);
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufInvalidProtocolBufferException)
