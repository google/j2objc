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
//  AccessibleObject.m
//  JreEmulation
//
//  Created by Tom Ball on 6/18/12.
//

#import "IOSClass.h"
#import "java/lang/AssertionError.h"
#import "java/lang/reflect/AccessibleObject.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"

@implementation JavaLangReflectAccessibleObject

- (instancetype)init {
  if ((self = [super init])) {
    accessible_ = NO;
  }
  return self;
}

- (BOOL)isAccessible {
  return accessible_;
}

- (void)setAccessibleWithBoolean:(BOOL)b {
  accessible_ = b;
}

+ (void)setAccessibleWithJavaLangReflectAccessibleObjectArray:(IOSObjectArray *)objects
                                                  withBoolean:(BOOL)b {
  for (NSObject *o in objects) {
    [(JavaLangReflectAccessibleObject *)o setAccessibleWithBoolean:b];
  }
}

- (id)getAnnotationWithIOSClass:(IOSClass *)annotationType {
  nil_chk(annotationType);
  IOSObjectArray *annotations = [self getAnnotations];
  jint n = annotations->size_;
  for (jint i = 0; i < n; i++) {
    id annotation = annotations->buffer_[i];
    if ([annotationType isInstance:annotation]) {
      return annotation;
    }
  }
  return nil;
}

- (IOSObjectArray *)getDeclaredAnnotations {
  // can't call an abstract method
  [self doesNotRecognizeSelector:_cmd];
  return nil;
}

- (IOSObjectArray *)getAnnotations {
  // Overridden by ExecutableMember to also return inherited members.
  return [self getDeclaredAnnotations];
}

- (BOOL)isAnnotationPresentWithIOSClass:(IOSClass *)annotationType {
  return [self getAnnotationWithIOSClass:annotationType] != nil;
}

- (IOSObjectArray *)getAnnotationsFromAccessor:(JavaLangReflectMethod *)method {
  if (method) {
    IOSObjectArray *noArgs = [IOSObjectArray arrayWithLength:0 type:[NSObject getClass]];
    return (IOSObjectArray *) [method invokeWithId:nil withNSObjectArray:noArgs];
  } else {
    IOSClass *annotationType = [IOSClass classWithProtocol:@protocol(JavaLangAnnotationAnnotation)];
    return [IOSObjectArray arrayWithLength:0 type:annotationType];
  }
}

- (NSString *)toGenericString {
  // can't call an abstract method
  [self doesNotRecognizeSelector:_cmd];
  return nil;
}

@end

BOOL validTypeEncoding(const char *type) {
  return strlen(type) == 1 && strchr("@#cSsilLqQZfdBv", *type);
}

// TODO(tball): is there a reasonable way to make these methods table-driven?

// Return a Obj-C type encoding as a Java type or wrapper type.
IOSClass *decodeTypeEncoding(const char *type) {
  if (strlen(type) > 3 && type[0] == '@') {
    // Format is either '@"type-name"' for classes, or '@"<type-name>"' for protocols.
    char *typeNameAsC = type[2] == '<'
        ? strndup(type + 3, strlen(type) - 5) : strndup(type + 2, strlen(type) - 3);
    NSString *typeName = [NSString stringWithUTF8String:typeNameAsC];
    free(typeNameAsC);
    return [IOSClass forName:typeName];
  }
  switch (type[0]) {
    case '@':
      return [IOSClass objectClass];
    case '#':
      return [IOSClass classWithClass:[IOSClass class]];
    case 'c':
      return [IOSClass byteClass];
    case 'S':
      return [IOSClass charClass];
    case 's':
      return [IOSClass shortClass];
    case 'i':
      return [IOSClass intClass];
    case 'l':
    case 'L':
    case 'q':
    case 'Q':
      return [IOSClass longClass];
    case 'f':
      return [IOSClass floatClass];
    case 'd':
      return [IOSClass doubleClass];
    case 'B':
      return [IOSClass booleanClass];
    case 'v':
      return [IOSClass voidClass];
  }
  NSString *errorMsg =
  [NSString stringWithFormat:@"unknown Java type encoding: '%s'", type];
  @throw AUTORELEASE([[JavaLangAssertionError alloc] initWithNSString:errorMsg]);
}

// Return a description of an Obj-C type encoding.
NSString *describeTypeEncoding(NSString *type) {
  if ([type length] == 1) {
    unichar typeChar = [type characterAtIndex:0];
    switch (typeChar) {
      case '@':
        return @"Object";
      case '#':
        return @"Class";
      case 'c':
        return @"byte";
      case 'S':
        // A Java character is an unsigned two-byte int; in other words,
        // an unsigned short with an encoding of 'S'.
        return @"char";
      case 's':
        return @"short";
      case 'i':
        return @"int";
      case 'q':
      case 'Q':
        return @"long";
      case 'f':
        return @"float";
      case 'd':
        return @"double";
      case 'B':
        return @"boolean";
      case 'v':
        return @"void";
    }
  }
  return [NSString stringWithFormat:@"unknown type encoding: %@", type];
}
