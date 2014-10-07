// Copyright 2012 Google Inc. All Rights Reserved.
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
//  IOSArrayClass.m
//  JreEmulation
//
//  Created by Tom Ball on 1/23/12.
//

#import "IOSArrayClass.h"
#import "IOSObjectArray.h"
#import "IOSPrimitiveClass.h"
#import "java/lang/InstantiationException.h"

@implementation IOSArrayClass

- (instancetype)initWithComponentType:(IOSClass *)type {
  if ((self = [super init])) {
    componentType_ = RETAIN_(type);
  }
  return self;
}

- (IOSClass *)getComponentType {
  return componentType_;
}

- (BOOL)isArray {
  return YES;
}

- (IOSClass *)getSuperclass {
  return [IOSClass objectClass];
}

- (BOOL)isInstance:(id)object {
  IOSClass *objClass = [object getClass];
  return [objClass isArray] && [componentType_ isAssignableFrom:[objClass getComponentType]];
}

- (BOOL)isAssignableFrom:(IOSClass *)cls {
  return [cls isArray] && [componentType_ isAssignableFrom:[cls getComponentType]];
}

- (NSString *)getName {
  return [self binaryName];
}

- (NSString *)getSimpleName {
  return [[[self getComponentType] getName] stringByAppendingString:@"[]"];
}

- (NSString *)binaryName {
  return [NSString stringWithFormat:@"[%@", [componentType_ binaryName]];
}

- (NSString *)objcName {
  return [[[self getComponentType] objcName] stringByAppendingString:@"Array"];
}

- (id)newInstance {
  if (!componentType_) {
    @throw AUTORELEASE([[JavaLangInstantiationException alloc] init]);
  }
  return [IOSObjectArray arrayWithLength:0 type:componentType_];
}

- (BOOL)isEqual:(id)anObject {
  return [anObject isKindOfClass:[IOSArrayClass class]] &&
    [componentType_ isEqual:[anObject getComponentType]];
}

- (NSString *)description {
  return [NSString stringWithFormat:@"class %@", [self getSimpleName]];
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [componentType_ release];
  [super dealloc];
}
#endif

@end
