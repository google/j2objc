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
#import "JavaMetadata.h"
#import "java/lang/NoSuchMethodException.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"
#import "java/lang/reflect/TypeVariable.h"
#import "objc/message.h"
#import "objc/runtime.h"

@implementation ExecutableMember

- (instancetype)initWithMethodSignature:(NSMethodSignature *)methodSignature
                               selector:(SEL)selector
                                  class:(IOSClass *)aClass
                               metadata:(JavaMethodMetadata *)metadata {
  if ((self = [super init])) {
    methodSignature_ = [methodSignature retain];
    selector_ = selector;
    class_ = aClass; // IOSClass types are never dealloced.
    metadata_ = [metadata retain];
  }
  return self;
}

- (NSString *)getName {
  // can't call an abstract method
  [self doesNotRecognizeSelector:_cmd];
  return nil;
}

- (int)getModifiers {
  if (metadata_) {
    return [metadata_ modifiers];
  }
  return JavaLangReflectModifier_PUBLIC;
}

static IOSClass *DecodePrimitiveParamKeyword(NSString *keyword) {
  if ([keyword isEqualToString:@"Byte"]) {
    return [IOSClass byteClass];
  } else if ([keyword isEqualToString:@"Short"]) {
    return [IOSClass shortClass];
  } else if ([keyword isEqualToString:@"Int"]) {
    return [IOSClass intClass];
  } else if ([keyword isEqualToString:@"Long"]) {
    return [IOSClass longClass];
  } else if ([keyword isEqualToString:@"Float"]) {
    return [IOSClass floatClass];
  } else if ([keyword isEqualToString:@"Double"]) {
    return [IOSClass doubleClass];
  } else if ([keyword isEqualToString:@"Char"]) {
    return [IOSClass charClass];
  } else if ([keyword isEqualToString:@"Boolean"]) {
    return [IOSClass booleanClass];
  }
  return nil;
}

static IOSClass *ResolveParameterType(const char *objcType, NSString *paramKeyword) {
  if (![paramKeyword hasPrefix:@"with"] && ![paramKeyword hasPrefix:@"With"]) {
    // Not a direct java translation, do our best with the ObjC type info.
    return decodeTypeEncoding(objcType);
  }
  // Remove "with" or "With" prefix.
  paramKeyword = [paramKeyword substringFromIndex:4];
  IOSClass *type = nil;
  if (*objcType == '@') {
    if ([paramKeyword hasSuffix:@"Array"]) {
      paramKeyword = [paramKeyword substringToIndex:[paramKeyword length] - 5];
      IOSClass *componentType = DecodePrimitiveParamKeyword(paramKeyword);
      if (!componentType) {
        componentType = [IOSClass classForIosName:paramKeyword];
      }
      if (componentType) {
        type = [IOSClass arrayClassWithComponentType:componentType];
      }
    } else {
      type = [IOSClass classForIosName:paramKeyword];
    }
  } else {
    type = DecodePrimitiveParamKeyword(paramKeyword);
  }
  if (type) {
    return type;
  }
  return [IOSClass objectClass];
}

- (IOSObjectArray *)getParameterTypes {
  // First two slots are class and SEL.
  jint nArgs = (jint)[methodSignature_ numberOfArguments] - SKIPPED_ARGUMENTS;
  IOSClass *classClass = [IOSClass classWithClass:[IOSClass class]];
  IOSObjectArray *parameters = [IOSObjectArray arrayWithLength:nArgs type:classClass];

  NSString *selectorStr = NSStringFromSelector(selector_);
  // Remove method name prefix.
  if ([selectorStr hasPrefix:@"init"]) {
    selectorStr = [selectorStr substringFromIndex:4];
  } else if (nArgs > 0) {
    NSRange range = [selectorStr rangeOfString:@":"];
    if (range.location != NSNotFound) {
      // The name ends with the last "WithType" before the first colon.
      range = [selectorStr rangeOfString:@"With" options:NSBackwardsSearch
                                   range:NSMakeRange(0, range.location)];
      if (range.location != NSNotFound) {
        selectorStr = [selectorStr substringFromIndex:range.location];
      }
    }
  }
  NSArray *paramTypes = [selectorStr componentsSeparatedByString:@":"];

  for (jint i = 0; i < nArgs; i++) {
    const char *argType = [methodSignature_ getArgumentTypeAtIndex:i + SKIPPED_ARGUMENTS];
    IOSClass *paramType = ResolveParameterType(argType, [paramTypes objectAtIndex:i]);
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

- (IOSObjectArray *)getGenericExceptionTypes {
  return [self getExceptionTypes];
}

- (BOOL)isSynthetic {
  if (metadata_) {
    return ([metadata_ modifiers] & JavaLangReflectModifier_SYNTHETIC) > 0;
  }
  return NO;
}

- (IOSObjectArray *)getExceptionTypes {
  IOSObjectArray *result = [metadata_ exceptionTypes];
  if (!result) {
    result = [IOSObjectArray arrayWithLength:0 type:[IOSClass getClass]];
  }
  return result;
}

- (NSString *)internalName {
  // can't call an abstract method
  [self doesNotRecognizeSelector:_cmd];
  return nil;
}

#define SANITIZED_METHOD_NAME \
  [[self internalName] stringByReplacingOccurrencesOfString:@":" withString:@"_"]

- (IOSObjectArray *)getDeclaredAnnotations {
  Class cls = class_.objcClass;
  if (cls) {
    NSString *annotationsMethodName =
        [NSString stringWithFormat:@"__annotations_%@", SANITIZED_METHOD_NAME];
    Method annotationsMethod = JreFindClassMethod(cls, [annotationsMethodName UTF8String]);
    if (annotationsMethod) {
      return method_invoke(cls, annotationsMethod);
    }
  }
  IOSClass *annotationType = [IOSClass classWithProtocol:@protocol(JavaLangAnnotationAnnotation)];
  return [IOSObjectArray arrayWithLength:0 type:annotationType];
}

- (IOSObjectArray *)getParameterAnnotations {
  Class cls = class_.objcClass;
  if (cls) {
    NSString *annotationsMethodName =
        [NSString stringWithFormat:@"__annotations_%@_params", SANITIZED_METHOD_NAME];
    Method annotationsMethod = JreFindClassMethod(cls, [annotationsMethodName UTF8String]);
    if (annotationsMethod) {
      return method_invoke(cls, annotationsMethod);
    }
  }
  IOSClass *annotationType = [IOSClass classWithProtocol:@protocol(JavaLangAnnotationAnnotation)];
  return [IOSObjectArray arrayWithDimensions:2 lengths:(int[]){0, 0} type:annotationType];
}

- (NSString *)toGenericString {
  // TODO(tball): implement as part of method metadata.
  return nil;
}

- (BOOL)isVarArgs {
  if (metadata_) {
    return ([metadata_ modifiers] & JavaLangReflectModifier_VARARGS) > 0;
  }
  return NO;
}

- (BOOL)isBridge {
  // Translator doesn't generate bridge methods.
  return NO;
}

- (NSMethodSignature *)signature {
  return methodSignature_;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [methodSignature_ release];
  [metadata_ release];
  [super dealloc];
}
#endif

@end
