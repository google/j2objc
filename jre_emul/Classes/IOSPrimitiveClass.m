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
//  IOSPrimitiveClass.m
//  JreEmulation
//
//  Created by Tom Ball on 1/22/12.
//

#import "IOSPrimitiveClass.h"
#import "IOSObjectArray.h"
#import "java/lang/AssertionError.h"
#import "java/lang/Boolean.h"
#import "java/lang/Byte.h"
#import "java/lang/Character.h"
#import "java/lang/Double.h"
#import "java/lang/Float.h"
#import "java/lang/Integer.h"
#import "java/lang/InstantiationException.h"
#import "java/lang/Long.h"
#import "java/lang/NoSuchMethodException.h"
#import "java/lang/Short.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"

@implementation IOSPrimitiveClass

- (id)initWithName:(NSString *)name type:(NSString *)type {
  if ((self = [super init])) {
#if __has_feature(objc_arc)
    name_ = name;
    type_ = type;
#else
    name_ = [name retain];
    type_ = [type retain];
#endif
  }
  return self;
}

- (NSString *)getName {
  return name_;
}

- (NSString *)getSimpleName {
  return name_;
}

- (NSString *)getCanonicalName {
  return name_;
}

- (NSString *)objcName {
  return name_;
}

- (NSString *)description {
  return name_;
}

- (BOOL)isAssignableFrom:(IOSClass *)cls {
  return [self isEqual:cls];
}

- (BOOL)isInstance:(id)object {
  return NO;  // Objects can't be primitives.
}

- (int)getModifiers {
  return JavaLangReflectModifier_PUBLIC | JavaLangReflectModifier_FINAL |
      JavaLangReflectModifier_ABSTRACT;
}

- (IOSObjectArray *)getDeclaredMethods {
  return [IOSObjectArray arrayWithLength:0
      type:[IOSClass classWithClass:[JavaLangReflectMethod class]]];
}

- (JavaLangReflectMethod *)getMethod:(NSString *)name, ... {
  id exception = [[JavaLangNoSuchMethodException alloc] init];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  @throw exception;
  return nil;
}

- (JavaLangReflectMethod *)getDeclaredMethod:(NSString *)name, ... {
  id exception = [[JavaLangNoSuchMethodException alloc] init];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  @throw exception;
  return nil;
}

- (JavaLangReflectConstructor *)
getConstructorWithClasses:(IOSClass *)firstClass, ... {
  id exception = [[JavaLangNoSuchMethodException alloc] init];
#if ! __has_feature(objc_arc)
  [exception autorelease];
#endif
  @throw exception;
  return nil;
}

- (BOOL)isPrimitive {
  return YES;
}

// isEqual and hash are uniquely identified by their name.
- (BOOL)isEqual:(id)anObject {
  if (![anObject isKindOfClass:[IOSPrimitiveClass class]]) {
    return NO;
  }
  IOSPrimitiveClass *other = (IOSPrimitiveClass *)anObject;
  return [name_ isEqual:other->name_];
}

- (NSUInteger)hash {
  return [name_ hash];
}

- (NSString *)binaryName {
  return type_;
}

- (id)__boxValue:(J2ObjcRawValue *)rawValue {
  switch ([type_ characterAtIndex:0]) {
    case 'B': return [JavaLangByte valueOfWithByte:rawValue->asChar];
    case 'C': return [JavaLangCharacter valueOfWithChar:rawValue->asUnichar];
    case 'D': return [JavaLangDouble valueOfWithDouble:rawValue->asDouble];
    case 'F': return [JavaLangFloat valueOfWithFloat:rawValue->asFloat];
    case 'I': return [JavaLangInteger valueOfWithInt:rawValue->asInt];
    case 'J': return [JavaLangLong valueOfWithLong:rawValue->asLong];
    case 'S': return [JavaLangShort valueOfWithShort:rawValue->asShort];
    case 'Z': return [JavaLangBoolean valueOfWithBoolean:rawValue->asBOOL];
  }
  return nil;
}

- (id)wrapperClass {
  switch ([type_ characterAtIndex:0]) {
    case 'B': return [IOSClass classWithClass:[JavaLangByte class]];
    case 'C': return [IOSClass classWithClass:[JavaLangCharacter class]];
    case 'D': return [IOSClass classWithClass:[JavaLangDouble class]];
    case 'F': return [IOSClass classWithClass:[JavaLangFloat class]];
    case 'I': return [IOSClass classWithClass:[JavaLangInteger class]];
    case 'J': return [IOSClass classWithClass:[JavaLangLong class]];
    case 'S': return [IOSClass classWithClass:[JavaLangShort class]];
    case 'Z': return [IOSClass classWithClass:[JavaLangBoolean class]];
  }
  return nil;
}

- (BOOL)__unboxValue:(id)value toRawValue:(J2ObjcRawValue *)rawValue {
  IOSClass *fromType = nil;
  if ([value isKindOfClass:[JavaLangByte class]]) {
    rawValue->asChar = [(JavaLangByte *) value charValue];
    fromType = [IOSClass byteClass];
  } else if ([value isKindOfClass:[JavaLangCharacter class]]) {
    rawValue->asUnichar = [(JavaLangCharacter *) value charValue];
    fromType = [IOSClass charClass];
  } else if ([value isKindOfClass:[JavaLangDouble class]]) {
    rawValue->asDouble = [(JavaLangDouble *) value doubleValue];
    fromType = [IOSClass doubleClass];
  } else if ([value isKindOfClass:[JavaLangFloat class]]) {
    rawValue->asFloat = [(JavaLangFloat *) value floatValue];
    fromType = [IOSClass floatClass];
  } else if ([value isKindOfClass:[JavaLangInteger class]]) {
    rawValue->asInt = [(JavaLangInteger *) value intValue];
    fromType = [IOSClass intClass];
  } else if ([value isKindOfClass:[JavaLangLong class]]) {
    rawValue->asLong = [(JavaLangLong *) value longValue];
    fromType = [IOSClass longClass];
  } else if ([value isKindOfClass:[JavaLangShort class]]) {
    rawValue->asShort = [(JavaLangShort *) value shortValue];
    fromType = [IOSClass shortClass];
  } else if ([value isKindOfClass:[JavaLangBoolean class]]) {
    rawValue->asBOOL = [(JavaLangBoolean *) value booleanValue];
    fromType = [IOSClass booleanClass];
  }

  if (fromType) {
    return [fromType __convertRawValue:rawValue toType:self];
  }
  return NO;
}

- (void)__readRawValue:(J2ObjcRawValue *)rawValue fromAddress:(const void *)addr {
  switch ([type_ characterAtIndex:0]) {
    case 'B': rawValue->asChar = *(char *)addr; return;
    case 'C': rawValue->asUnichar = *(unichar *)addr; return;
    case 'D': rawValue->asDouble = *(double *)addr; return;
    case 'F': rawValue->asFloat = *(float *)addr; return;
    case 'I': rawValue->asInt = *(int *)addr; return;
    case 'J': rawValue->asLong = *(long long *)addr; return;
    case 'S': rawValue->asShort = *(short *)addr; return;
    case 'Z': rawValue->asBOOL = *(BOOL *)addr; return;
  }
}

- (void)__writeRawValue:(J2ObjcRawValue *)rawValue toAddress:(const void *)addr {
  switch ([type_ characterAtIndex:0]) {
    case 'B': *(char *)addr = rawValue->asChar; return;
    case 'C': *(unichar *)addr = rawValue->asUnichar; return;
    case 'D': *(double *)addr = rawValue->asDouble; return;
    case 'F': *(float *)addr = rawValue->asFloat; return;
    case 'I': *(int *)addr = rawValue->asInt; return;
    case 'J': *(long long *)addr = rawValue->asLong; return;
    case 'S': *(short *)addr = rawValue->asShort; return;
    case 'Z': *(BOOL *)addr = rawValue->asBOOL; return;
  }
}

- (BOOL)__convertRawValue:(J2ObjcRawValue *)rawValue toType:(IOSClass *)toType {
  if (![toType isPrimitive]) {
    return NO;
  }
  unichar toTypeChar = [((IOSPrimitiveClass *)toType)->type_ characterAtIndex:0];
  switch ([type_ characterAtIndex:0]) {
    case 'B':
      switch (toTypeChar) {
        case 'B': return YES;
        case 'D': rawValue->asDouble = rawValue->asChar; return YES;
        case 'F': rawValue->asFloat = rawValue->asChar; return YES;
        case 'I': rawValue->asInt = rawValue->asChar; return YES;
        case 'J': rawValue->asLong = rawValue->asChar; return YES;
        case 'S': rawValue->asShort = rawValue->asChar; return YES;
      }
      return NO;
    case 'C':
      switch (toTypeChar) {
        case 'C': return YES;
        case 'D': rawValue->asDouble = rawValue->asUnichar; return YES;
        case 'F': rawValue->asFloat = rawValue->asUnichar; return YES;
        case 'I': rawValue->asInt = rawValue->asUnichar; return YES;
        case 'J': rawValue->asLong = rawValue->asUnichar; return YES;
      }
      return NO;
    case 'D':
      switch (toTypeChar) {
        case 'D': return YES;
      }
      return NO;
    case 'F':
      switch (toTypeChar) {
        case 'D': rawValue->asDouble = rawValue->asFloat; return YES;
        case 'F': return YES;
      }
      return NO;
    case 'I':
      switch (toTypeChar) {
        case 'D': rawValue->asDouble = rawValue->asInt; return YES;
        case 'F': rawValue->asFloat = rawValue->asInt; return YES;
        case 'I': return YES;
        case 'J': rawValue->asLong = rawValue->asInt; return YES;
      }
      return NO;
    case 'J':
      switch (toTypeChar) {
        case 'D': rawValue->asDouble = rawValue->asLong; return YES;
        case 'F': rawValue->asFloat = rawValue->asLong; return YES;
        case 'J': return YES;
      }
      return NO;
    case 'S':
      switch (toTypeChar) {
        case 'D': rawValue->asDouble = rawValue->asShort; return YES;
        case 'F': rawValue->asFloat = rawValue->asShort; return YES;
        case 'I': rawValue->asInt = rawValue->asShort; return YES;
        case 'J': rawValue->asLong = rawValue->asShort; return YES;
        case 'S': return YES;
      }
      return NO;
    case 'Z':
      switch (toTypeChar) {
        case 'Z': return YES;
      }
      return NO;
  }
  return NO;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [name_ release];
  [type_ release];
  [super dealloc];
}
#endif

@end
