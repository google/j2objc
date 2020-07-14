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

#include "com/google/protobuf/ExtensionRegistry.h"

#include "com/google/protobuf/Descriptors_PackagePrivate.h"
#include "com/google/protobuf/Extension.h"
#include "com/google/protobuf/GeneratedMessage.h"

J2OBJC_INITIALIZED_DEFN(ComGoogleProtobufExtensionRegistry)

static CGPExtensionRegistry *CGPExtensionRegistry_EMPTY_;

@implementation ComGoogleProtobufExtensionRegistry

+ (CGPExtensionRegistry *)newInstance {
  return [[[CGPExtensionRegistry alloc] init] autorelease];
}

+ (CGPExtensionRegistry *)getEmptyRegistry {
  return CGPExtensionRegistry_EMPTY_;
}

- (void)addWithComGoogleProtobufExtension:(CGPExtension *)extension {
  CGPExtensionRegistryAdd(self, extension);
}

- (void)addWithComGoogleProtobufGeneratedMessage_GeneratedExtension:
    (ComGoogleProtobufGeneratedMessage_GeneratedExtension *)extension {
  CGPExtensionRegistryAdd(self, extension);
}

- (ComGoogleProtobufExtensionRegistry_ExtensionInfo *)
    findExtensionByNumberWithComGoogleProtobufDescriptors_Descriptor:(CGPDescriptor *)descriptor
    withInt:(jint)fieldId {
  CGPFieldDescriptor *field = CGPExtensionRegistryFind(self, descriptor, fieldId);
  if (field != nil) {
    return [[[CGPExtensionInfo alloc] initWithField:field] autorelease];
  }
  return nil;
}

- (ComGoogleProtobufExtensionRegistry *)getUnmodifiable {
  return self;
}

+ (void)initialize {
  if (self == [CGPExtensionRegistry class]) {
    CGPExtensionRegistry_EMPTY_ = [[CGPExtensionRegistry alloc] init];
    J2OBJC_SET_INITIALIZED(ComGoogleProtobufExtensionRegistry)
  }
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufExtensionRegistry)

ComGoogleProtobufExtensionRegistry *ComGoogleProtobufExtensionRegistry_newInstance() {
  ComGoogleProtobufExtensionRegistry_initialize();
  return [[[CGPExtensionRegistry alloc] init] autorelease];
}

ComGoogleProtobufExtensionRegistry *ComGoogleProtobufExtensionRegistry_getEmptyRegistry() {
  ComGoogleProtobufExtensionRegistry_initialize();
  return CGPExtensionRegistry_EMPTY_;
}

void ComGoogleProtobufExtensionRegistry_initWithBoolean_(
    ComGoogleProtobufExtensionRegistry *self, jboolean empty) {
  ComGoogleProtobufExtensionRegistryLite_initWithBoolean_(self, empty);
}

ComGoogleProtobufExtensionRegistry_ExtensionInfo *
ComGoogleProtobufExtensionRegistry_newExtensionInfoWithComGoogleProtobufExtension_(
    ComGoogleProtobufExtension *extension) {
  return [[ComGoogleProtobufExtensionRegistry_ExtensionInfo alloc]
      initWithField:[extension getDescriptor]];
}


@implementation ComGoogleProtobufExtensionRegistry_ExtensionInfo

- (instancetype)initWithField:(CGPFieldDescriptor *)field {
  if (self = [super init]) {
    descriptor_ = field;
    if (CGPFieldTypeIsMessage(field)) {
      // No need to retain. Default message values are eternal.
      defaultInstance_ = CGPFieldGetDefaultValue(field);
    }
  }
  return self;
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufExtensionRegistry_ExtensionInfo)
