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

//  Created by Keith Stanger on Apr. 19, 2017.
//
//  Hand written counterpart for com.google.protobuf.MapEntry.

#include "com/google/protobuf/MapEntry.h"

@interface ComGoogleProtobufMapEntry () {
 @public
  id key_;
  id value_;
}
@end

@interface ComGoogleProtobufMapEntry_Builder () {
 @public
  id key_;
  id value_;
}
@end

@implementation ComGoogleProtobufMapEntry

- (instancetype)initWithKey:(id)key value:(id)value {
  if (self == [self init]) {
    key_ = RETAIN_(key);
    value_ = RETAIN_(value);
  }
  return self;
}

- (id)getKey {
  return key_;
}

- (id)getValue {
  return value_;
}

- (ComGoogleProtobufMapEntry_Builder *)toBuilder {
  ComGoogleProtobufMapEntry_Builder *builder =
      AUTORELEASE([[ComGoogleProtobufMapEntry_Builder alloc] init]);
  builder->key_ = RETAIN_(key_);
  builder->value_ = RETAIN_(value_);
  return builder;
}

- (void)dealloc {
  RELEASE_(key_);
  RELEASE_(value_);
  [super dealloc];
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufMapEntry)

@implementation ComGoogleProtobufMapEntry_Builder

- (ComGoogleProtobufMapEntry_Builder *)setKeyWithId:(id)key {
  JreStrongAssign(&key_, key);
  return self;
}

- (ComGoogleProtobufMapEntry_Builder *)setValueWithId:(id)value {
  JreStrongAssign(&value_, value);
  return self;
}

- (ComGoogleProtobufMapEntry *)build {
  return AUTORELEASE([[ComGoogleProtobufMapEntry alloc] initWithKey:key_ value:value_]);
}

- (void)dealloc {
  RELEASE_(key_);
  RELEASE_(value_);
  [super dealloc];
}

@end

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(ComGoogleProtobufMapEntry_Builder)
