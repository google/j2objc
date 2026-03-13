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
#include "com/google/protobuf/ExtensionRegistryLite.h"
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

- (bool)hasExtensionWithComGoogleProtobufExtensionLite:(ComGoogleProtobufExtensionLite *)extension;
- (bool)hasExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension;
- (bool)hasExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension;

- (id)getExtensionWithComGoogleProtobufExtensionLite:(ComGoogleProtobufExtensionLite *)extension;
- (id)getExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension;
- (id)getExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension;

- (id)getExtensionWithComGoogleProtobufExtensionLite:(ComGoogleProtobufExtensionLite *)extension
                                             withInt:(jint)index;
- (id)getExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension withInt:(jint)index;
- (id)getExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
          (CGPGeneratedExtension *)extension
                                                                   withInt:(jint)index;

- (jint)getExtensionCountWithComGoogleProtobufExtensionLite:
    (ComGoogleProtobufExtensionLite *)extension;
- (jint)getExtensionCountWithComGoogleProtobufExtension:(CGPExtension *)extension;
- (jint)getExtensionCountWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension;

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

- (id)setExtensionWithComGoogleProtobufExtensionLite:(ComGoogleProtobufExtensionLite *)extension
                                              withId:(id)value;
- (id)setExtensionWithComGoogleProtobufExtensionLite:(ComGoogleProtobufExtensionLite *)extension
                                             withInt:(jint)index
                                              withId:(id)value;
- (id)setExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension withId:(id)value;
- (id)setExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension
                                         withInt:(jint)index
                                          withId:(id)value;
- (id)setExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
          (CGPGeneratedExtension *)extension
                                                                    withId:(id)value;
- (id)setExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
          (CGPGeneratedExtension *)extension
                                                                   withInt:(jint)index
                                                                    withId:(id)value;

- (id)addExtensionWithComGoogleProtobufExtensionLite:(ComGoogleProtobufExtensionLite *)extension
                                              withId:(id)value;
- (id)addExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension withId:(id)value;
- (id)addExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
          (CGPGeneratedExtension *)extension
                                                                    withId:(id)value;

- (id)clearExtensionWithComGoogleProtobufExtensionLite:(ComGoogleProtobufExtensionLite *)extension;
- (id)clearExtensionWithComGoogleProtobufExtension:(CGPExtension *)extension;
- (id)clearExtensionWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (CGPGeneratedExtension *)extension;

@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleProtobufGeneratedMessage_ExtendableBuilder)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufGeneratedMessage_ExtendableBuilder)

@interface ComGoogleProtobufGeneratedMessage_GeneratedExtension : CGPExtension
@end

J2OBJC_EMPTY_STATIC_INIT(ComGoogleProtobufGeneratedMessage_GeneratedExtension)

J2OBJC_TYPE_LITERAL_HEADER(ComGoogleProtobufGeneratedMessage_GeneratedExtension)

/*!
 * Defines the class instance name for a type.
 *
 * @define J2OBJC_PROTO_TYPE_LITERAL_CLASS_INSTANCE_NAME
 * @param TYPE The name of the type to define the class instance name for.
 */
#define J2OBJC_PROTO_TYPE_LITERAL_CLASS_INSTANCE_NAME(TYPE) TYPE##_class_instance_

/*!
 * Declares the type literal accessor for a proto type.
 * Different from J2OBJC_TYPE_LITERAL_HEADER because it depends on the initializer method instead
 * of a dispatch once to initialize the class instance.
 *
 * @define J2OBJC_PROTO_TYPE_LITERAL_HEADER
 * @param TYPE The name of the type to declare the accessor for.
 */
#define J2OBJC_PROTO_TYPE_LITERAL_HEADER(TYPE)         \
  FOUNDATION_EXPORT IOSClass *J2OBJC_PROTO_TYPE_LITERAL_CLASS_INSTANCE_NAME(TYPE);  \
  CGP_ALWAYS_INLINE IOSClass *TYPE##_class_(void) {    \
    TYPE##_initialize();                               \
    return J2OBJC_PROTO_TYPE_LITERAL_CLASS_INSTANCE_NAME(TYPE);                     \
  }                                                    \

/*!
 * Declares the storage for the type literal for a proto type.
 * Different from J2OBJC_TYPE_LITERAL_HEADER because it depends on the initializer method instead
 * of a dispatch once to initialize the class instance.
 *
 * @define J2OBJC_PROTO_CLASS_TYPE_LITERAL_SOURCE
 * @param TYPE The name of the type to define the accessor for.
 */
#define J2OBJC_PROTO_CLASS_TYPE_LITERAL_SOURCE(TYPE) \
  IOSClass *J2OBJC_PROTO_TYPE_LITERAL_CLASS_INSTANCE_NAME(TYPE);                  \

/*!
 * Defines the build class instance name for a type.
 *
 * @define J2OBJC_PROTO_TYPE_LITERAL_CLASS_INSTANCE_NAME
 * @param TYPE The name of the type to define the class instance name for.
 */
#define J2OBJC_PROTO_TYPE_LITERAL_BUILDER_CLASS_INSTANCE_NAME(TYPE) TYPE##_builder_class_instance_

/*!
 * Defines the build class instance name for a type.
 *
 * @define J2OBJC_PROTO_TYPE_LITERAL_CLASS_INSTANCE_NAME
 * @param TYPE The name of the type to define the class instance name for.
 */
#define J2OBJC_PROTO_TYPE_LITERAL_ORBUILDER_CLASS_INSTANCE_NAME(TYPE) TYPE##OrBuilder_class_instance_

/*!
 * Declares the type literal accessor for a proto type.
 * Different from J2OBJC_TYPE_LITERAL_HEADER because it depends on the initializer method instead
 * of a dispatch once to initialize the class instance.
 *
 * @define J2OBJC_PROTO_TYPE_LITERAL_HEADER
 * @param TYPE The name of the type to declare the accessor for.
 */
#define J2OBJC_PROTO_MSG_TYPE_LITERAL_HEADER(TYPE)                                            \
  FOUNDATION_EXPORT IOSClass *J2OBJC_PROTO_TYPE_LITERAL_CLASS_INSTANCE_NAME(TYPE);            \
  FOUNDATION_EXPORT IOSClass *J2OBJC_PROTO_TYPE_LITERAL_BUILDER_CLASS_INSTANCE_NAME(TYPE);    \
  FOUNDATION_EXPORT IOSClass *J2OBJC_PROTO_TYPE_LITERAL_ORBUILDER_CLASS_INSTANCE_NAME(TYPE);  \
  CGP_ALWAYS_INLINE IOSClass *TYPE##_class_(void) {                                           \
    TYPE##_initialize();                                                                      \
    return J2OBJC_PROTO_TYPE_LITERAL_CLASS_INSTANCE_NAME(TYPE);                               \
  }                                                                                           \
  CGP_ALWAYS_INLINE IOSClass *TYPE##_Builder_class_(void) {                                   \
    TYPE##_initialize();                                                                      \
    return J2OBJC_PROTO_TYPE_LITERAL_BUILDER_CLASS_INSTANCE_NAME(TYPE);                       \
  }                                                                                           \
  CGP_ALWAYS_INLINE IOSClass *TYPE##OrBuilder_class_(void) {                                  \
    TYPE##_initialize();                                                                      \
    return J2OBJC_PROTO_TYPE_LITERAL_ORBUILDER_CLASS_INSTANCE_NAME(TYPE);                     \
  }

/*!
 * Declares the storage for the type literal for a proto type.
 * Different from J2OBJC_TYPE_LITERAL_HEADER because it depends on the initializer method instead
 * of a dispatch once to initialize the class instance.
 *
 * @define J2OBJC_PROTO_MSG_CLASS_TYPE_LITERAL_SOURCE
 * @param TYPE The name of the type to define the accessor for.
 */
#define J2OBJC_PROTO_MSG_CLASS_TYPE_LITERAL_SOURCE(TYPE)                   \
  IOSClass *J2OBJC_PROTO_TYPE_LITERAL_CLASS_INSTANCE_NAME(TYPE);           \
  IOSClass *J2OBJC_PROTO_TYPE_LITERAL_BUILDER_CLASS_INSTANCE_NAME(TYPE);   \
  IOSClass *J2OBJC_PROTO_TYPE_LITERAL_ORBUILDER_CLASS_INSTANCE_NAME(TYPE);

// CGPFunctions are for internal implementation only.
CF_EXTERN_C_BEGIN

NS_RETURNS_RETAINED ComGoogleProtobufGeneratedMessage *CGPNewMessage(
    ComGoogleProtobufDescriptors_Descriptor *descriptor);

NS_RETURNS_RETAINED ComGoogleProtobufGeneratedMessage_Builder *CGPNewBuilder(
    ComGoogleProtobufDescriptors_Descriptor *descriptor);

NS_RETURNS_RETAINED ComGoogleProtobufGeneratedMessage_Builder *CGPBuilderFromPrototype(
    CGPDescriptor *descriptor, ComGoogleProtobufGeneratedMessage *prototype);

void CGPMergeFromRawData(id msg, CGPDescriptor *descriptor, const char *data, uint32_t length);

NS_RETURNS_RETAINED ComGoogleProtobufGeneratedMessage *CGPParseFromByteArray(
    CGPDescriptor *descriptor, IOSByteArray *bytes,
    ComGoogleProtobufExtensionRegistryLite *registry);

NS_RETURNS_RETAINED ComGoogleProtobufGeneratedMessage *CGPParseFromInputStream(
    CGPDescriptor *descriptor, JavaIoInputStream *input,
    ComGoogleProtobufExtensionRegistryLite *registry);

NS_RETURNS_RETAINED ComGoogleProtobufGeneratedMessage *CGPParseFromByteString(
    CGPDescriptor *descriptor, ComGoogleProtobufByteString *byteString,
    ComGoogleProtobufExtensionRegistryLite *registry);

NS_RETURNS_RETAINED ComGoogleProtobufGeneratedMessage *CGPParseDelimitedFromInputStream(
    CGPDescriptor *descriptor, JavaIoInputStream *input,
    ComGoogleProtobufExtensionRegistryLite *registry);

CF_EXTERN_C_END
#endif  // __ComGoogleProtobufGeneratedMessage_H__
