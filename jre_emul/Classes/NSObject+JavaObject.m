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
//  NSObject+JavaObject.m
//  JreEmulation
//
//  Created by Tom Ball on 8/15/11.
//

#import "NSObject+JavaObject.h"
#import "IOSClass.h"
#import "java/lang/ClassCastException.h"
#import "java/lang/CloneNotSupportedException.h"
#import "java/lang/NullPointerException.h"

// A category that adds Java Object-compatible methods to NSObject.
@implementation NSObject (JavaObject)

- (id)clone {
  if (![self conformsToProtocol:@protocol(NSCopying)] &&
      ![self conformsToProtocol:@protocol(NSMutableCopying)]) {
    id exception = [[JavaLangCloneNotSupportedException alloc] init];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
#if __has_feature(objc_arc)
  // ARC doesn't support NSCopyObject
  [self doesNotRecognizeSelector:_cmd];
  return 0;
#else
  return NSCopyObject(self, 0, NULL);
#endif
}

- (IOSClass *)getClass {
  return [IOSClass classWithClass:[self class]];
}

- (int)compareToWithId:(id)other {
#if __has_feature(objc_arc)
  @throw [[JavaLangClassCastException alloc] init];
#else
  @throw [[[JavaLangClassCastException alloc] init] autorelease];
#endif
  return 0;
}

+ (id)throwNullPointerException {
#if __has_feature(objc_arc)
  @throw [[JavaLangNullPointerException alloc] init];
#else
  @throw [[[JavaLangNullPointerException alloc] init] autorelease];
#endif
  return nil;
}

@end

