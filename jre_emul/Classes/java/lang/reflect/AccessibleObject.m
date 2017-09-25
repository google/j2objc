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
  self->accessible_ = false;
}

- (jboolean)isAccessible {
  return accessible_;
}

- (void)setAccessibleWithBoolean:(jboolean)b {
  accessible_ = b;
}

+ (void)setAccessibleWithJavaLangReflectAccessibleObjectArray:(IOSObjectArray *)objects
                                                  withBoolean:(jboolean)b {
  JavaLangReflectAccessibleObject_setAccessibleWithJavaLangReflectAccessibleObjectArray_withBoolean_(
    objects, b);
}

- (id)getAnnotationWithIOSClass:(IOSClass *)annotationType {
  (void)nil_chk(annotationType);
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
  // Overridden by Executable to also return inherited members.
  return [self getDeclaredAnnotations];
}

- (jboolean)isAnnotationPresentWithIOSClass:(IOSClass *)annotationType {
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

// Default methods in java.lang.reflect.AnnotatedElement.
- (IOSObjectArray *)getAnnotationsByTypeWithIOSClass:(IOSClass *)arg0 {
  return JavaLangReflectAnnotatedElement_getAnnotationsByTypeWithIOSClass_(self, arg0);
}

- (id<JavaLangAnnotationAnnotation>)getDeclaredAnnotationWithIOSClass:(IOSClass *)arg0 {
  return JavaLangReflectAnnotatedElement_getDeclaredAnnotationWithIOSClass_(self, arg0);
}

- (IOSObjectArray *)getDeclaredAnnotationsByTypeWithIOSClass:(IOSClass *)arg0 {
  return JavaLangReflectAnnotatedElement_getDeclaredAnnotationsByTypeWithIOSClass_(self, arg0);
}

+ (const J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { NULL, NULL, 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "Z", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "V", 0x1, 0, 1, -1, -1, -1, -1 },
    { NULL, "V", 0x9, 0, 2, -1, -1, -1, -1 },
    { NULL, "LJavaLangAnnotationAnnotation;", 0x1, 3, 4, -1, 5, -1, -1 },
    { NULL, "Z", 0x1, 6, 4, -1, 7, -1, -1 },
    { NULL, "[LJavaLangAnnotationAnnotation;", 0x1, -1, -1, -1, -1, -1, -1 },
    { NULL, "[LJavaLangAnnotationAnnotation;", 0x1, -1, -1, -1, -1, -1, -1 },
  };
  #pragma clang diagnostic push
  #pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
  methods[0].selector = @selector(init);
  methods[1].selector = @selector(isAccessible);
  methods[2].selector = @selector(setAccessibleWithBoolean:);
  methods[3].selector = @selector(setAccessibleWithJavaLangReflectAccessibleObjectArray:withBoolean:);
  methods[4].selector = @selector(getAnnotationWithIOSClass:);
  methods[5].selector = @selector(isAnnotationPresentWithIOSClass:);
  methods[6].selector = @selector(getAnnotations);
  methods[7].selector = @selector(getDeclaredAnnotations);
  #pragma clang diagnostic pop
  static const void *ptrTable[] = {
    "setAccessible", "Z", "[LJavaLangReflectAccessibleObject;Z", "getAnnotation", "LIOSClass;",
    "<T::Ljava/lang/annotation/Annotation;>(Ljava/lang/Class<TT;>;)TT;", "isAnnotationPresent",
    "(Ljava/lang/Class<+Ljava/lang/annotation/Annotation;>;)Z" };
  static const J2ObjcClassInfo _JavaLangReflectAccessibleObject = {
    "AccessibleObject", "java.lang.reflect", ptrTable, methods, NULL, 7, 0x1, 8, 0, -1, -1, -1, -1,
    -1 };
  return &_JavaLangReflectAccessibleObject;
}

@end

void JavaLangReflectAccessibleObject_setAccessibleWithJavaLangReflectAccessibleObjectArray_withBoolean_(
    IOSObjectArray *objects, jboolean b) {
  for (JavaLangReflectAccessibleObject *o in objects) {
    [o setAccessibleWithBoolean:b];
  }
}

J2OBJC_CLASS_TYPE_LITERAL_SOURCE(JavaLangReflectAccessibleObject)
