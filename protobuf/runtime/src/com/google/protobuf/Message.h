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

//  Hand written counterpart of com.google.protobuf.Message and friends.

#ifndef __ComGoogleProtobufMessage_H__
#define __ComGoogleProtobufMessage_H__

#import "JreEmulation.h"

#import "com/google/protobuf/MessageLite.h"
#import "com/google/protobuf/MessageOrBuilder.h"

@class ComGoogleProtobufByteString;
@class ComGoogleProtobufDescriptors_FieldDescriptor;
@class ComGoogleProtobufExtensionRegistryLite;
@class JavaIoInputStream;
@protocol ComGoogleProtobufMessage$Builder;

@protocol ComGoogleProtobufMessage
    <ComGoogleProtobufMessageLite, ComGoogleProtobufMessageOrBuilder, NSObject, JavaObject>

- (id<ComGoogleProtobufMessage$Builder>)toBuilder;
- (id<ComGoogleProtobufMessage$Builder>)newBuilderForType NS_RETURNS_NOT_RETAINED;
- (ComGoogleProtobufByteString *)toByteString;
- (NSData *)toNSData;

+ (id)getDescriptor;

@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleProtobufMessage)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufMessage)

@protocol ComGoogleProtobufMessage$Builder
    <ComGoogleProtobufMessageLite$Builder, ComGoogleProtobufMessageOrBuilder, NSObject, JavaObject>

- (id<ComGoogleProtobufMessage$Builder>)
    setRepeatedFieldWithComGoogleProtobufDescriptors$FieldDescriptor:(id)descriptor
                                                             withInt:(jint)index
                                                              withId:(id)object;

- (id<ComGoogleProtobufMessage$Builder>)
    setFieldWithComGoogleProtobufDescriptors$FieldDescriptor:(id)descriptor
                                                      withId:(id)object;

- (id<ComGoogleProtobufMessage$Builder>)clear;

- (id<ComGoogleProtobufMessage$Builder>)
    clearFieldWithComGoogleProtobufDescriptors$FieldDescriptor:(id)descriptor;

- (id<ComGoogleProtobufMessage$Builder>)
    addRepeatedFieldWithComGoogleProtobufDescriptors$FieldDescriptor:(id)descriptor
                                                              withId:(id)object;

- (id<ComGoogleProtobufMessage$Builder>)
      newBuilderForFieldWithComGoogleProtobufDescriptors$FieldDescriptor:
          (ComGoogleProtobufDescriptors_FieldDescriptor *)fieldDescriptor OBJC_METHOD_FAMILY_NONE;

- (id<ComGoogleProtobufMessage$Builder>)mergeFromWithComGoogleProtobufMessage:
    (id<ComGoogleProtobufMessage>)message;

- (id<ComGoogleProtobufMessage$Builder>)
    mergeFromWithComGoogleProtobufByteString:(ComGoogleProtobufByteString *)data
    withComGoogleProtobufExtensionRegistryLite:
        (ComGoogleProtobufExtensionRegistryLite *)extensionRegistry;

- (id<ComGoogleProtobufMessage$Builder>)mergeFromWithByteArray:(IOSByteArray *)data
    withComGoogleProtobufExtensionRegistryLite:
        (ComGoogleProtobufExtensionRegistryLite *)extensionRegistry;

- (id<ComGoogleProtobufMessage$Builder>)
    mergeFromWithJavaIoInputStream:(JavaIoInputStream *)input;

- (id<ComGoogleProtobufMessage$Builder>)
    mergeFromWithJavaIoInputStream:(JavaIoInputStream *)input
    withComGoogleProtobufExtensionRegistryLite:
        (ComGoogleProtobufExtensionRegistryLite *)extensionRegistry;

- (BOOL)mergeDelimitedFromWithJavaIoInputStream:(JavaIoInputStream *)input;

- (BOOL)mergeDelimitedFromWithJavaIoInputStream:(JavaIoInputStream *)input
    withComGoogleProtobufExtensionRegistryLite:
        (ComGoogleProtobufExtensionRegistryLite *)extensionRegistry;

- (id<ComGoogleProtobufMessage>)build;

- (id<ComGoogleProtobufMessage>)buildPartial;

+ (id)getDescriptor;

@end
// TODO(kstanger): Remove when users have migrated.
#ifdef J2OBJC_RENAME_ALIASES
#define ComGoogleProtobufMessage_Builder ComGoogleProtobufMessage$Builder
#define setRepeatedFieldWithComGoogleProtobufDescriptors_FieldDescriptor setRepeatedFieldWithComGoogleProtobufDescriptors$FieldDescriptor
#define setFieldWithComGoogleProtobufDescriptors_FieldDescriptor setFieldWithComGoogleProtobufDescriptors$FieldDescriptor
#define clearFieldWithComGoogleProtobufDescriptors_FieldDescriptor clearFieldWithComGoogleProtobufDescriptors$FieldDescriptor
#define addRepeatedFieldWithComGoogleProtobufDescriptors_FieldDescriptor addRepeatedFieldWithComGoogleProtobufDescriptors$FieldDescriptor
#define newBuilderForFieldWithComGoogleProtobufDescriptors_FieldDescriptor newBuilderForFieldWithComGoogleProtobufDescriptors$FieldDescriptor
#endif // J2OBJC_RENAME_ALIASES

J2OBJC_EMPTY_STATIC_INIT(ComGoogleProtobufMessage$Builder)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufMessage$Builder)

#endif // __ComGoogleProtobufMessage_H__
