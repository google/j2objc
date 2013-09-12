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
//  AccessibleObject.h
//  JreEmulation
//
//  Created by Tom Ball on 6/18/12.
//

#ifndef _AccessibleObject_H_
#define _AccessibleObject_H_

#import <Foundation/Foundation.h>
#import "IOSClass.h"
#import "java/lang/reflect/AnnotatedElement.h"

// Base class for fields, methods, and constructors.
@interface JavaLangReflectAccessibleObject : NSObject < JavaLangReflectAnnotatedElement >

- (BOOL)isAccessible;
- (void)setAccessibleWithBoolean:(BOOL)b;
+ (void)setAccessibleWithJavaLangReflectAccessibleObjectArray:(IOSObjectArray *)objects
                                                  withBoolean:(BOOL)b;

- (id)getAnnotationWithIOSClass:(IOSClass *)annotationClass;
- (BOOL)isAnnotationPresentWithIOSClass:(IOSClass *)annotationClass;
- (IOSObjectArray *)getAnnotations;
- (IOSObjectArray *)getDeclaredAnnotations;

// Protected method.
- (IOSObjectArray *)getAnnotationsFromAccessor:(JavaLangReflectMethod *)method;

@end

// Decodes an Objective-C type encoding, returning the associated iOS class.
// For example, the type encoding 's' is decoded as JavaLangShort.
IOSClass *decodeTypeEncoding(const char *type);

// Return a Java type name for an Objective-C type encoding.  For example,
// "byte" is returned for 'c', since a Java byte is mapped to a C char.
NSString *describeTypeEncoding(NSString *type);

#endif // _AccessibleObject_H_
