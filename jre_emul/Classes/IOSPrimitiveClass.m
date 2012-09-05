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
//  IOSPrimitiveClass.m
//  JreEmulation
//
//  Created by Tom Ball on 1/22/12.
//

#import "IOSPrimitiveClass.h"
#import "IOSObjectArray.h"
#import "java/lang/AssertionError.h"
#import "java/lang/InstantiationException.h"
#import "java/lang/NoSuchMethodException.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"

@implementation IOSPrimitiveClass

- (id)initWithName:(NSString *)name type:(NSString *)type {
  if ((self = [super init])) {
#if __has_feature(objc_arc)
    name_ = name;
    type_ = type;
#else
    name_ = [name retain];
    type_ = [type retain];
#endif
  }
  return self;
}

- (NSString *)getName {
  return name_;
}

- (NSString *)getSimpleName {
  return name_;
}

- (NSString *)getCanonicalName {
  return name_;
}

- (NSString *)description {
  return name_;
}

- (IOSClass *)getSuperclass {
  return nil;
}

- (BOOL)isInstance:(id)object {
  return NO;  // Objects can't be primitives.
}

- (int)getModifiers {
  return JavaLangReflectModifier_PUBLIC | JavaLangReflectModifier_FINAL |
      JavaLangReflectModifier_ABSTRACT;
}

static IOSObjectArray *emptyArray(IOSClass *arrayType) {
  return [[IOSObjectArray alloc] initWithLength:0 type:arrayType];
}

- (IOSObjectArray *)getDeclaredMethods {
  return emptyArray([IOSClass classWithClass:[JavaLangReflectMethod class]]);
}

- (JavaLangReflectMethod *)getMethod:(NSString *)name, ... {
  id exception = [[JavaLangNoSuchMethodException alloc] init];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  @throw exception;
  return nil;
}

- (JavaLangReflectMethod *)getDeclaredMethod:(NSString *)name, ... {
  id exception = [[JavaLangNoSuchMethodException alloc] init];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  @throw exception;
  return nil;
}

- (JavaLangReflectConstructor *)
getConstructorWithClasses:(IOSClass *)firstClass, ... {
  id exception = [[JavaLangNoSuchMethodException alloc] init];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  @throw exception;
  return nil;
}

- (id)newInstance {
  id exception = [[JavaLangInstantiationException alloc] init];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  @throw exception;
  return nil;
}

- (id)initWithProtocol:(Protocol *)protocol {
  id exception = [[JavaLangAssertionError alloc] init];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  @throw exception;
  return nil;
}

- (id)classWithProtocol:(Protocol *)protocol {
  id exception = [[JavaLangAssertionError alloc] init];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  @throw exception;
  return nil;
}

// isEqual and hash are uniquely identified by their name.
- (BOOL)isEqual:(id)anObject {
  if (![anObject isKindOfClass:[IOSPrimitiveClass class]]) {
    return NO;
  }
  IOSPrimitiveClass *other = (IOSPrimitiveClass *)anObject;
  return [name_ isEqual:other->name_];
}

- (NSUInteger)hash {
  return [name_ hash];
}

- (NSString *)binaryName {
  return type_;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [name_ release];
  [type_ release];
  [super dealloc];
}
#endif

@end
