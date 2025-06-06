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

#ifndef __ComGoogleProtobufTextFormat_H__
#define __ComGoogleProtobufTextFormat_H__


#import "JreEmulation.h"
#include "com/google/protobuf/common.h"
#include "com/google/protobuf/MessageOrBuilder.h"

@class ComGoogleProtobufTextFormat_Printer;
@class ComGoogleProtobufTextFormat;

@interface ComGoogleProtobufTextFormat : NSObject

+ (ComGoogleProtobufTextFormat_Printer *)printer;

@end

@interface ComGoogleProtobufTextFormat_Printer : NSObject

- (NSString *)printToStringWithComGoogleProtobufMessageOrBuilder:(id<ComGoogleProtobufMessageOrBuilder>)messageOrBuilder;

@end

CF_EXTERN_C_BEGIN

ComGoogleProtobufTextFormat_Printer *ComGoogleProtobufTextFormat_printer(void);

CF_EXTERN_C_END

#endif // __ComGoogleProtobufTextFormat_H__