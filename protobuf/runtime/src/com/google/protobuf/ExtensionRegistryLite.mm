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

#import "com/google/protobuf/ExtensionRegistryLite.h"

#include <map>

#import "com/google/protobuf/Descriptors_PackagePrivate.h"
#import "com/google/protobuf/Extension.h"

typedef std::pair<const CGPDescriptor *, int> ExtensionRegistryKey;
typedef std::map<ExtensionRegistryKey, CGPFieldDescriptor *> ExtensionRegistryMap;

@interface ComGoogleProtobufExtensionRegistryLite () {
 @package
  ExtensionRegistryMap map_;
}
@end

@implementation ComGoogleProtobufExtensionRegistryLite

- (ComGoogleProtobufExtensionRegistryLite *)getUnmodifiable {
  return self;
}

@end

CGPExtensionRegistryLite *ComGoogleProtobufExtensionRegistryLite_newInstance() {
  ComGoogleProtobufExtensionRegistryLite_initialize();
  return [[[CGPExtensionRegistryLite alloc] init] autorelease];
}

void CGPExtensionRegistryAdd(CGPExtensionRegistryLite *registry, CGPExtension *extension) {
  CGPFieldDescriptor *field = extension->fieldDescriptor_;
  CGPDescriptor *containingType = CGPFieldGetContainingType(field);
  registry->map_[ExtensionRegistryKey(containingType, CGPFieldGetNumber(field))] = field;
}

CGPFieldDescriptor *CGPExtensionRegistryFind(
    CGPExtensionRegistryLite *registry, CGPDescriptor *descriptor, int fieldNumber) {
  ExtensionRegistryMap *map = &registry->map_;
  ExtensionRegistryMap::iterator it = map->find(ExtensionRegistryKey(descriptor, fieldNumber));
  if (it != map->end()) {
    return it->second;
  }
  return nil;
}
