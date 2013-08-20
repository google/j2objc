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

#import "java/lang/AssertionError.h"
#import "java/lang/Boolean.h"
#import "java/lang/Byte.h"
#import "java/lang/Character.h"
#import "java/lang/Double.h"
#import "java/lang/Float.h"
#import "java/lang/Integer.h"
#import "java/lang/Long.h"
#import "java/lang/Short.h"
#import "java/lang/Void.h"
#import "java/lang/reflect/AccessibleObject.h"
#import "java/lang/reflect/Method.h"

@implementation JavaLangReflectAccessibleObject

- (BOOL)isAccessible {
  // Everything in Objective-C is accessible at runtime.
  return YES;
}

- (void)setAccessibleWithBOOL:(BOOL)b {
  // do nothing
}

+ (void)setAccessibleWithJavaLangReflectAccessibleObjectArray:(IOSObjectArray *)objects
                                                     withBOOL:(BOOL)b {
  // do nothing
}

- (id)getAnnotationWithIOSClass:(IOSClass *)annotationType {
  nil_chk(annotationType);
  IOSObjectArray *annotations = [self getAnnotations];
  NSUInteger n = [annotations count];
  for (NSUInteger i = 0; i < n; i++) {
    id annotation = [annotations objectAtIndex:i];
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

@end

// TODO(user): is there a reasonable way to make these methods table-driven?

// Return a Obj-C type encoding as a Java type or wrapper type.
IOSClass *decodeTypeEncoding(const char *type) {
  Class typeClass = nil;
  if (strlen(type) > 1 && type[0] == '@') {
    // Format is '@"type-name"'.
    char *typeNameAsC = strndup(type + 2, strlen(type) - 3);
    NSString *typeName = [NSString stringWithUTF8String:typeNameAsC];
    free(typeNameAsC);
    return [IOSClass forName:typeName];
  }
  switch (type[0]) {
    case '@':
      typeClass = [NSObject class];
      break;
    case '#':
      typeClass = [IOSClass class];
      break;
    case 'c':
      typeClass = [JavaLangByte class];
      break;
    case 'S':
      typeClass = [JavaLangCharacter class];
      break;
    case 's':
      typeClass = [JavaLangShort class];
      break;
    case 'i':
      typeClass = [JavaLangInteger class];
      break;
    case 'l':
    case 'L':
    case 'q':
    case 'Q':
      typeClass = [JavaLangLong class];
      break;
    case 'f':
      typeClass = [JavaLangFloat class];
      break;
    case 'd':
      typeClass = [JavaLangDouble class];
      break;
    case 'B':
      typeClass = [JavaLangBoolean class];
      break;
    case 'v':
      typeClass = [JavaLangVoid class];
      break;
  }
  if (typeClass == nil) {
    NSString *errorMsg =
    [NSString stringWithFormat:@"unknown Java type encoding: '%s'", type];
    id exception = [[JavaLangAssertionError alloc] initWithNSString:errorMsg];
#if ! __has_feature(objc_arc)
    [exception autorelease];
#endif
    @throw exception;
  }
  return [IOSClass classWithClass:typeClass];
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
