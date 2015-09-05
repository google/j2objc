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

#import "J2ObjC_source.h"
#import "java/lang/AssertionError.h"
#import "java/lang/annotation/Annotation.h"
#import "java/lang/reflect/AccessibleObject.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"

@implementation JavaLangReflectAccessibleObject

- (instancetype)init {
  JavaLangReflectAccessibleObject_init(self);
  return self;
}

void JavaLangReflectAccessibleObject_init(JavaLangReflectAccessibleObject *self) {
  NSObject_init(self);
  self->accessible_ = NO;
}

JavaLangReflectAccessibleObject *new_JavaLangReflectAccessibleObject_init() {
  JavaLangReflectAccessibleObject *self = [JavaLangReflectAccessibleObject alloc];
  JavaLangReflectAccessibleObject_init(self);
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
  JavaLangReflectAccessibleObject_setAccessibleWithJavaLangReflectAccessibleObjectArray_withBoolean_(
    objects, b);
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
    IOSObjectArray *noArgs = [IOSObjectArray arrayWithLength:0 type:NSObject_class_()];
    return (IOSObjectArray *) [method invokeWithId:nil withNSObjectArray:noArgs];
  } else {
    return [IOSObjectArray arrayWithLength:0 type:JavaLangAnnotationAnnotation_class_()];
  }
}

- (NSString *)toGenericString {
  // can't call an abstract method
  [self doesNotRecognizeSelector:_cmd];
  return nil;
}

+ (const J2ObjcClassInfo *)__metadata {
  static const J2ObjcMethodInfo methods[] = {
    { "isAccessible", NULL, "Z", 0x1, NULL },
    { "setAccessibleWithBoolean:", "setAccessible", "V", 0x1, NULL },
    { "setAccessibleWithJavaLangReflectAccessibleObjectArray:withBoolean:", "setAccessible", "V", 0x9, NULL },
    { "getAnnotationWithIOSClass:", "getAnnotation", "TT;", 0x1, NULL },
    { "isAnnotationPresentWithIOSClass:", "isAnnotationPresent", "Z", 0x1, NULL },
    { "getAnnotations", NULL, "[Ljava.lang.annotation.Annotation;", 0x1, NULL },
    { "getDeclaredAnnotations", NULL, "[Ljava.lang.annotation.Annotation;", 0x1, NULL },
    { "init", NULL, NULL, 0x1, NULL },
  };
  static const J2ObjcClassInfo _JavaLangReflectAccessibleObject = {
    1, "AccessibleObject", "java.lang.reflect", NULL, 0x1, 8, methods, 0, NULL, 0, NULL
  };
  return &_JavaLangReflectAccessibleObject;
}

@end

void JavaLangReflectAccessibleObject_setAccessibleWithJavaLangReflectAccessibleObjectArray_withBoolean_(
    IOSObjectArray *objects, BOOL b) {
  for (JavaLangReflectAccessibleObject *o in objects) {
    [o setAccessibleWithBoolean:b];
  }
}

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
      return NSObject_class_();
    case '#':
      return IOSClass_class_();
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

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(JavaLangReflectAccessibleObject)
