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
//  ExecutableMember.h
//  JreEmulation
//
//  Created by Tom Ball on 11/11/11.
//

#ifndef _ExecutableMember_H_
#define _ExecutableMember_H_

#import "java/lang/reflect/AccessibleObject.h"
#import "java/lang/reflect/Member.h"

// The first arguments all messages have are self and _cmd.
// These are unmodified when specifying method-specific arguments.
#define SKIPPED_ARGUMENTS 2

@class IOSClass;
@class IOSObjectArray;

// Common parent of Member and Constructor with their shared functionality.
// This class isn't directly called from translated Java, since Java's
// Method and Constructor classes just duplicate their common code.
@interface ExecutableMember : JavaLangReflectAccessibleObject {
 @protected
  IOSClass *class_;
  SEL selector_;
  BOOL classMethod_;
  NSMethodSignature *methodSignature_;
}

- (id)initWithSelector:(SEL)aSelector withClass:(IOSClass *)aClass;

- (NSString *)getName;

// This method returns Modifier.PUBLIC (1) for an instance method, or
// Modifier.PUBLIC | Modifier.STATIC (9) for a class method.  Even though
// iOS init methods are instance methods, constructors are always returned
// as class methods.
//
// Note: an enum isn't used because the Java API is defined with an int.
// This is because reflection was added to Java before enum support was.
- (int)getModifiers;

// Returns the class this executable is a member of.
- (IOSClass *)getDeclaringClass;

// Returns the types of any declared exceptions.
- (IOSObjectArray *)getExceptionTypes;

// Returns the parameter types for this executable member.
//
// @return an array of strings.
- (IOSObjectArray *)getParameterTypes;
- (IOSObjectArray *)getGenericParameterTypes;

// Empty array always returned for iOS.
- (IOSObjectArray *)getTypeParameters;

// Always false for iOS.
- (BOOL)isSynthetic;

- (IOSObjectArray *)getParameterAnnotations;

// Protected methods.
- (JavaLangReflectMethod *)getAnnotationsAccessor:(NSString *)methodName;
- (JavaLangReflectMethod *)getExceptionsAccessor:(NSString *)methodName;
- (JavaLangReflectMethod *)getParameterAnnotationsAccessor:(NSString *)methodName;

@end

#endif // _ExecutableMember_H_
