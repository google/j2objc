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

//  Created by Keith Stanger on Mar. 20, 2013.
//
//  Hand written counterpart for com.google.protobuf.GeneratedMessage and
//  friends.

#ifndef __ComGoogleProtobufGeneratedMessage_H__
#define __ComGoogleProtobufGeneratedMessage_H__

#import "JreEmulation.h"

#import "com/google/protobuf/Extension.h"
#import "com/google/protobuf/Message.h"
#import "com/google/protobuf/MessageOrBuilder.h"
#import "com/google/protobuf/common.h"

@class ComGoogleProtobufDescriptors_FieldDescriptor;
@class ComGoogleProtobufExtensionRegistryLite;
@class ComGoogleProtobufGeneratedMessage_GeneratedExtension;
struct CGPFieldData;

typedef ComGoogleProtobufGeneratedMessage_GeneratedExtension CGPGeneratedExtension;

@interface ComGoogleProtobufGeneratedMessage : NSObject<ComGoogleProtobufMessage>

+ (id)getDescriptor;

@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleProtobufGeneratedMessage)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufGeneratedMessage)

@interface ComGoogleProtobufGeneratedMessage_Builder : NSObject<ComGoogleProtobufMessage_Builder>

- (id)mergeFromWithJavaIoInputStream:(JavaIoInputStream *)input;
- (id)mergeFromWithJavaIoInputStream:(JavaIoInputStream *)input
    withComGoogleProtobufExtensionRegistryLite:
        (ComGoogleProtobufExtensionRegistryLite *)extensionRegistry;

@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleProtobufGeneratedMessage_Builder)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufGeneratedMessage_Builder)

@protocol ComGoogleProtobufGeneratedMessage_ExtendableMessageOrBuilder
    <ComGoogleProtobufMessageOrBuilder>

- (BOOL)hasExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension;

- (id)getExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension;

- (id)getExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension withInt:(int)index;

- (int)getExtensionCountWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension;

// Support older API that accepts Extension instead of ExtensionLite
- (BOOL)hasExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension;
- (id)getExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension;
- (id)getExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension withInt:(int)index;
- (int)getExtensionCountWithComGoogleProtobufExtension:(CGPExtension *)extension;

@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleProtobufGeneratedMessage_ExtendableMessageOrBuilder)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufGeneratedMessage_ExtendableMessageOrBuilder)

@interface ComGoogleProtobufGeneratedMessage_ExtendableMessage :
    ComGoogleProtobufGeneratedMessage<ComGoogleProtobufGeneratedMessage_ExtendableMessageOrBuilder>
@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleProtobufGeneratedMessage_ExtendableMessage)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufGeneratedMessage_ExtendableMessage)

@interface ComGoogleProtobufGeneratedMessage_ExtendableBuilder :
    ComGoogleProtobufGeneratedMessage_Builder
    <ComGoogleProtobufGeneratedMessage_ExtendableMessageOrBuilder>

- (id)setExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension withId:(id)value;

- (id)addExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension withId:(id)value;

- (id)clearExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension;

// Support older API that accepts Extension instead of ExtensionLite
- (id)setExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension withId:(id)value;
- (id)addExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension withId:(id)value;
- (id)clearExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension;

@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleProtobufGeneratedMessage_ExtendableBuilder)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufGeneratedMessage_ExtendableBuilder)

@interface ComGoogleProtobufGeneratedMessage_GeneratedExtension : CGPExtension
@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleProtobufGeneratedMessage_GeneratedExtension)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufGeneratedMessage_GeneratedExtension)

#endif // __ComGoogleProtobufGeneratedMessage_H__
