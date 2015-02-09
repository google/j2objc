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
//  Method.h
//  JreEmulation
//
//  Created by Tom Ball on 11/07/11.
//

#ifndef _JavaLangReflectMethod_H_
#define _JavaLangReflectMethod_H_

#import "J2ObjC_common.h"
#import "java/lang/reflect/ExecutableMember.h"
#import "java/lang/reflect/GenericDeclaration.h"
#import "java/lang/reflect/Member.h"

@class IOSClass;
@class IOSObjectArray;
@class JavaMethodMetadata;

// A native implementation of java.lang.reflect.Method.  Its methods are
// limited to those that can be derived from an Objective-C Method instance,
// so instances can be created and released as needed.
@interface JavaLangReflectMethod : ExecutableMember
    < JavaLangReflectGenericDeclaration, JavaLangReflectMember > {
  BOOL isStatic_;
}

+ (instancetype)methodWithMethodSignature:(NSMethodSignature *)methodSignature
                                 selector:(SEL)selector
                                    class:(IOSClass *)aClass
                                 isStatic:(BOOL)isStatic
                                 metadata:(JavaMethodMetadata *)metadata;

// iOS version of Method.getReturnType();
- (IOSClass *)getReturnType;

// Returns type.
- (id<JavaLangReflectType>)getGenericReturnType;

// iOS version of Method.invoke().
//
// @param object the instance to invoke this method on, or if null,
//     the class that implements this method.
// @return the result of this invocation; if a primitive type is returned,
//     it is wrapped in a Foundation wrapper class instance.
- (NSObject *)invokeWithId:(id)object
               withNSObjectArray:(IOSObjectArray *)arguments;

// Returns default value.
- (id)getDefaultValue;

@end

J2OBJC_EMPTY_STATIC_INIT(JavaLangReflectMethod)

J2OBJC_TYPE_LITERAL_HEADER(JavaLangReflectMethod)

#endif // _JavaLangReflectMethod_H_
