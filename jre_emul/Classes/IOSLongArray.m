// Copyright 2011 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

//
//  IOSLongArray.m
//  JreEmulation
//
//  Created by Tom Ball on 6/16/11.
//

#import "IOSLongArray.h"
#import "IOSClass.h"

@implementation IOSLongArray

PRIMITIVE_ARRAY_IMPLEMENTATION(long, Long, long long)

- (NSString *)descriptionOfElementAtIndex:(NSUInteger)index {
  return [NSString stringWithFormat:@"%lld", buffer_[index]];
}

- (IOSClass *)elementType {
  return [IOSClass longClass];
}

+ (IOSClass *)iosClass {
  return [IOSClass arrayClassWithComponentType:[IOSClass longClass]];
}

- (id)copyWithZone:(NSZone *)zone {
  return [[IOSLongArray allocWithZone:zone] initWithLongs:buffer_ count:size_];
}

- (void)dealloc {
  free(buffer_);
#if ! __has_feature(objc_arc)
  [super dealloc];
#endif
}

@end
