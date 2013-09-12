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
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"
#import "java/lang/reflect/TypeVariable.h"
#import "objc/runtime.h"

@implementation ExecutableMember

- (id)initWithSelector:(SEL)aSelector withClass:(IOSClass *)aClass {
  if ((self = [super init])) {
    selector_ = aSelector;
    class_ = aClass;
    if (class_.objcClass) {
      classMethod_ = ![class_.objcClass instancesRespondToSelector:selector_];
      if (classMethod_) {
        methodSignature_ =
            [class_.objcClass methodSignatureForSelector:selector_];
      } else {
        methodSignature_ =
            [class_.objcClass instanceMethodSignatureForSelector:selector_];
      }
    } else {
      assert (class_.objcProtocol);
      struct objc_method_description methodDesc =
        protocol_getMethodDescription(class_.objcProtocol, aSelector, YES, YES);
      if (methodDesc.name && methodDesc.types) {  // If method exists ...
        methodSignature_ =
            [NSMethodSignature signatureWithObjCTypes:methodDesc.types];
      }
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


- (NSString *)getName {
  // can't call an abstract method
  [self doesNotRecognizeSelector:_cmd];
  return nil;
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
    IOSClass *paramType = decodeTypeEncoding(argType);
    [parameters replaceObjectAtIndex:i withObject:paramType];
  }
  return parameters;
}

// Returns the class this executable is a member of.
- (IOSClass *)getDeclaringClass {
  return class_;
}

- (IOSObjectArray *)getTypeParameters {
  IOSClass *typeVariableType = [IOSClass classWithProtocol:@protocol(JavaLangReflectTypeVariable)];
  return[IOSObjectArray arrayWithLength:0 type:typeVariableType];
}


- (IOSObjectArray *)getGenericParameterTypes {
  return [self getParameterTypes];
}

- (BOOL)isSynthetic {
  return NO;
}

- (IOSObjectArray *)getExceptionTypes {
  JavaLangReflectMethod *method = [self getExceptionsAccessor:[self getName]];
  if (method) {
    IOSObjectArray *noArgs = [IOSObjectArray arrayWithLength:0 type:[NSObject getClass]];
    return (IOSObjectArray *) [method invokeWithId:nil withNSObjectArray:noArgs];
  } else {
    return [IOSObjectArray arrayWithLength:0 type:[IOSClass getClass]];
  }
}

- (IOSObjectArray *)getParameterAnnotations {
  // can't call an abstract method
  [self doesNotRecognizeSelector:_cmd];
  return nil;
}

static JavaLangReflectMethod *getAccessor(IOSClass *class, NSString *method, NSString *accessor) {
  NSString *accessorMethod = [NSString stringWithFormat:@"__%@_%@", accessor,
     [method stringByReplacingOccurrencesOfString:@":" withString:@"_"]];
  IOSObjectArray *methods = [class getDeclaredMethods];
  NSUInteger n = [methods count];
  for (NSUInteger i = 0; i < n; i++) {
    JavaLangReflectMethod *method = methods->buffer_[i];
    if ([accessorMethod isEqualToString:[method getName]] &&
        [[method getParameterTypes] count] == 0) {
      return method;
    }
  }
  return nil;  // No accessor for this member.
}

- (JavaLangReflectMethod *)getAnnotationsAccessor:(NSString *)methodName {
  return getAccessor(class_, methodName, @"annotations");
}

- (JavaLangReflectMethod *)getExceptionsAccessor:(NSString *)methodName {
  return getAccessor(class_, methodName, @"exceptions");
}

- (JavaLangReflectMethod *)getParameterAnnotationsAccessor:(NSString *)methodName {
  return [self getAnnotationsAccessor:[NSString stringWithFormat:@"%@_params", methodName]];
}

@end
