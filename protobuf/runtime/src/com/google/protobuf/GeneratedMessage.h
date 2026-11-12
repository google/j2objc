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

#include "JreEmulation.h"

#include "com/google/protobuf/AbstractMessage.h"
#include "com/google/protobuf/Extension.h"
#include "com/google/protobuf/Message.h"
#include "com/google/protobuf/MessageOrBuilder.h"
#include "com/google/protobuf/common.h"

@class ComGoogleProtobufByteString;
@class ComGoogleProtobufDescriptors_FieldDescriptor;
@class ComGoogleProtobufExtensionRegistryLite;
@class ComGoogleProtobufGeneratedMessage_GeneratedExtension;
struct CGPFieldData;

typedef ComGoogleProtobufGeneratedMessage_GeneratedExtension CGPGeneratedExtension;

@interface ComGoogleProtobufGeneratedMessage : ComGoogleProtobufAbstractMessage

+ (nonnull ComGoogleProtobufDescriptors_Descriptor *)getDescriptor;
+ (nonnull instancetype)getDefaultInstance;
- (nonnull instancetype)getDefaultInstanceForType;
+ (instancetype)parseFromWithComGoogleProtobufByteString:(ComGoogleProtobufByteString *)byteString;
+ (instancetype)parseFromWithComGoogleProtobufByteString:(ComGoogleProtobufByteString *)byteString
              withComGoogleProtobufExtensionRegistryLite:
                  (ComGoogleProtobufExtensionRegistryLite *)registry;
+ (instancetype)parseFromNSData:(NSData *)data;
+ (instancetype)parseFromNSData:(NSData *)data
                       registry:(ComGoogleProtobufExtensionRegistryLite *)registry;
+ (instancetype)parseFromWithByteArray:(IOSByteArray *)bytes;
+ (instancetype)parseFromWithByteArray:(IOSByteArray *)bytes
    withComGoogleProtobufExtensionRegistryLite:(ComGoogleProtobufExtensionRegistryLite *)registry;
+ (instancetype)parseFromWithJavaIoInputStream:(JavaIoInputStream *)input;
+ (instancetype)parseFromWithJavaIoInputStream:(JavaIoInputStream *)bytes
    withComGoogleProtobufExtensionRegistryLite:(ComGoogleProtobufExtensionRegistryLite *)registry;
+ (instancetype)parseDelimitedFromWithJavaIoInputStream:(JavaIoInputStream *)input;
+ (instancetype)parseDelimitedFromWithJavaIoInputStream:(JavaIoInputStream *)bytes
             withComGoogleProtobufExtensionRegistryLite:
                 (ComGoogleProtobufExtensionRegistryLite *)registry;
@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleProtobufGeneratedMessage)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufGeneratedMessage)

@interface ComGoogleProtobufGeneratedMessage_Builder : ComGoogleProtobufAbstractMessage_Builder

- (instancetype)mergeFromWithJavaIoInputStream:(JavaIoInputStream *)input;
- (instancetype)mergeFromWithJavaIoInputStream:(JavaIoInputStream *)input
    withComGoogleProtobufExtensionRegistryLite:
        (ComGoogleProtobufExtensionRegistryLite *)extensionRegistry;
- (instancetype)mergeFromWithComGoogleProtobufByteString:(ComGoogleProtobufByteString *)data;
- (instancetype)mergeFromWithComGoogleProtobufByteString:(ComGoogleProtobufByteString *)data
              withComGoogleProtobufExtensionRegistryLite:
                  (ComGoogleProtobufExtensionRegistryLite *)extensionRegistry;
- (instancetype)mergeFromWithByteArray:(IOSByteArray *)data;
- (instancetype)mergeFromWithByteArray:(IOSByteArray *)data
    withComGoogleProtobufExtensionRegistryLite:
        (ComGoogleProtobufExtensionRegistryLite *)extensionRegistry;
- (instancetype)mergeFromWithComGoogleProtobufMessage:(id<ComGoogleProtobufMessage>)message;

@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleProtobufGeneratedMessage_Builder)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufGeneratedMessage_Builder)

@protocol
    ComGoogleProtobufGeneratedMessage_ExtendableMessageOrBuilder <ComGoogleProtobufMessageOrBuilder,
                                                                  JavaObject>

- (bool)hasExtensionWithComGoogleProtobufExtensionLite:(ComGoogleProtobufExtensionLite *)extension
    NS_SWIFT_NAME(hasExtension(with:));
- (bool)hasExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension
    NS_SWIFT_NAME(hasExtension(with:));
- (bool)hasExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension NS_SWIFT_NAME(hasExtension(with:));

- (id)getExtensionWithComGoogleProtobufExtensionLite:(ComGoogleProtobufExtensionLite *)extension
    NS_SWIFT_NAME(getExtension(with:));
- (id)getExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension
    NS_SWIFT_NAME(getExtension(with:));
- (id)getExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension NS_SWIFT_NAME(getExtension(with:));

- (id)getExtensionWithComGoogleProtobufExtensionLite:(ComGoogleProtobufExtensionLite *)extension
                                             withInt:(jint)index
    NS_SWIFT_NAME(getExtension(with:with:));
- (id)getExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension
                                         withInt:(jint)index
    NS_SWIFT_NAME(getExtension(with:with:));
- (id)getExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
          (CGPGeneratedExtension *)extension
                                                                   withInt:(jint)index
    NS_SWIFT_NAME(getExtension(with:with:));

- (jint)getExtensionCountWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension NS_SWIFT_NAME(getExtensionCount(with:));
- (jint)getExtensionCountWithComGoogleProtobufExtension:(CGPExtension *)extension
    NS_SWIFT_NAME(getExtensionCount(with:));
- (jint)getExtensionCountWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension NS_SWIFT_NAME(getExtensionCount(with:));

@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleProtobufGeneratedMessage_ExtendableMessageOrBuilder)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufGeneratedMessage_ExtendableMessageOrBuilder)

@interface ComGoogleProtobufGeneratedMessage_ExtendableMessage
    : ComGoogleProtobufGeneratedMessage <
          ComGoogleProtobufGeneratedMessage_ExtendableMessageOrBuilder>
@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleProtobufGeneratedMessage_ExtendableMessage)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufGeneratedMessage_ExtendableMessage)

@interface ComGoogleProtobufGeneratedMessage_ExtendableBuilder
    : ComGoogleProtobufGeneratedMessage_Builder <
          ComGoogleProtobufGeneratedMessage_ExtendableMessageOrBuilder>

- (nonnull instancetype)setExtensionWithComGoogleProtobufExtensionLite:
                            (ComGoogleProtobufExtensionLite *)extension
                                                                withId:(id)value
    NS_SWIFT_NAME(setExtensionWith(_:withId:));
- (nonnull instancetype)setExtensionWithComGoogleProtobufExtensionLite:
                            (ComGoogleProtobufExtensionLite *)extension
                                                               withInt:(jint)index
                                                                withId:(id)value
    NS_SWIFT_NAME(setExtensionWith(_:with:withId:));
- (nonnull instancetype)setExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension
                                                            withId:(id)value
    NS_SWIFT_NAME(setExtensionWith(_:withId:));
- (nonnull instancetype)setExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension
                                                           withInt:(jint)index
                                                            withId:(id)value
    NS_SWIFT_NAME(setExtensionWith(_:with:withId:));
- (nonnull instancetype)
    setExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
        (CGPGeneratedExtension *)extension
                                                                  withId:(id)value
    NS_SWIFT_NAME(setExtensionWith(_:withId:));
- (nonnull instancetype)
    setExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
        (CGPGeneratedExtension *)extension
                                                                 withInt:(jint)index
                                                                  withId:(id)value
    NS_SWIFT_NAME(setExtensionWith(_:with:withId:));

- (nonnull instancetype)addExtensionWithComGoogleProtobufExtensionLite:
                            (ComGoogleProtobufExtensionLite *)extension
                                                                withId:(id)value
    NS_SWIFT_NAME(addExtension(with:withId:));
- (nonnull instancetype)addExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension
                                                            withId:(id)value
    NS_SWIFT_NAME(addExtension(with:withId:));
- (nonnull instancetype)
    addExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
        (CGPGeneratedExtension *)extension
                                                                  withId:(id)value
    NS_SWIFT_NAME(addExtension(with:withId:));

- (nonnull instancetype)clearExtensionWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension NS_SWIFT_NAME(clearExtension(with:));
- (nonnull instancetype)clearExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension
    NS_SWIFT_NAME(clearExtension(with:));
- (nonnull instancetype)clearExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension NS_SWIFT_NAME(clearExtension(with:));

@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleProtobufGeneratedMessage_ExtendableBuilder)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufGeneratedMessage_ExtendableBuilder)

@interface ComGoogleProtobufGeneratedMessage_GeneratedExtension : CGPExtension
@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleProtobufGeneratedMessage_GeneratedExtension)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufGeneratedMessage_GeneratedExtension)

#endif  // __ComGoogleProtobufGeneratedMessage_H__
