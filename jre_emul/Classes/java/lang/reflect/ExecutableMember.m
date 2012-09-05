//
//  ExecutableMember.m
//  JreEmulation
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
//  Created by Tom Ball on 11/11/11.
//

#import "ExecutableMember.h"
#import "IOSClass.h"
#import "IOSObjectArray.h"
#import "java/lang/NoSuchMethodException.h"
#import "java/lang/reflect/Modifier.h"

@implementation ExecutableMember

- (id)initWithSelector:(SEL)aSelector withClass:(IOSClass *)aClass {
  if ((self = [super init])) {
    selector_ = aSelector;
    class_ = aClass.objcClass;
    classMethod_ = ![class_ instancesRespondToSelector:selector_];
    if (classMethod_) {
      methodSignature_ = [class_ methodSignatureForSelector:selector_];
    } else {
      methodSignature_ = [class_ instanceMethodSignatureForSelector:selector_];
    }
    if (methodSignature_ == nil) {
      id exception =
          [[JavaLangNoSuchMethodException alloc]
              initWithNSString:NSStringFromSelector(aSelector)];
#if ! __has_feature(objc_arc)
      [exception autorelease];
#endif
      @throw exception;
    }
  }
  return self;
}

- (int)getModifiers {
  int mods = JavaLangReflectModifier_PUBLIC;
  if (classMethod_) {
    mods |= JavaLangReflectModifier_STATIC;
  }
  return mods;
}

- (IOSObjectArray *)getParameterTypes {
  // First two slots are class and SEL.
  NSUInteger nArgs = [methodSignature_ numberOfArguments] - SKIPPED_ARGUMENTS;
  IOSClass *classClass = [IOSClass classWithClass:[IOSClass class]];
  IOSObjectArray *parameters =
      [[IOSObjectArray alloc] initWithLength:nArgs type:classClass];
#if ! __has_feature(objc_arc)
  [parameters autorelease];
#endif

  for (NSUInteger i = 0; i < nArgs; i++) { 
    const char *argType = 
        [methodSignature_ getArgumentTypeAtIndex:i + SKIPPED_ARGUMENTS];
    IOSClass *paramType = decodeTypeEncoding(*argType);
    [parameters replaceObjectAtIndex:i withObject:paramType];
  }
  return parameters;
}

// Returns the class this executable is a member of.
- (IOSClass *)getDeclaringClass {
  return [IOSClass classWithClass:class_];
}

@end
