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

static IOSObjectArray *emptyArray(IOSClass *arrayType) {
  IOSObjectArray *result =
      [[IOSObjectArray alloc] initWithLength:0 type:arrayType];
#if ! __has_feature(objc_arc)
  [result autorelease];
#endif
  return result;
}

- (IOSObjectArray *)getDeclaredMethods {
  return emptyArray([IOSClass classWithClass:[JavaLangReflectMethod class]]);
}

- (IOSObjectArray *)allDeclaredMethods {
  return emptyArray([IOSClass classWithClass:[JavaLangReflectMethod class]]);
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

- (id)boxValue:(J2ObjcRawValue *)rawValue {
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

- (BOOL)unboxValue:(id)value toRawValue:(J2ObjcRawValue *)rawValue {
  if ([value isKindOfClass:[JavaLangByte class]]) {
    char byteValue = [(JavaLangByte *) value charValue];
    switch ([type_ characterAtIndex:0]) {
      case 'B': rawValue->asChar = byteValue; return YES;
      case 'D': rawValue->asDouble = byteValue; return YES;
      case 'F': rawValue->asFloat = byteValue; return YES;
      case 'I': rawValue->asInt = byteValue; return YES;
      case 'J': rawValue->asLong = byteValue; return YES;
      case 'S': rawValue->asShort = byteValue; return YES;
    }
  } else if ([value isKindOfClass:[JavaLangCharacter class]]) {
    unichar charValue = [(JavaLangCharacter *) value charValue];
    switch ([type_ characterAtIndex:0]) {
      case 'C': rawValue->asUnichar = charValue; return YES;
      case 'D': rawValue->asDouble = charValue; return YES;
      case 'F': rawValue->asFloat = charValue; return YES;
      case 'I': rawValue->asInt = charValue; return YES;
      case 'J': rawValue->asLong = charValue; return YES;
    }
  } else if ([value isKindOfClass:[JavaLangDouble class]]) {
    double doubleValue = [(JavaLangDouble *) value doubleValue];
    switch ([type_ characterAtIndex:0]) {
      case 'D': rawValue->asDouble = doubleValue; return YES;
    }
  } else if ([value isKindOfClass:[JavaLangFloat class]]) {
    float floatValue = [(JavaLangFloat *) value floatValue];
    switch ([type_ characterAtIndex:0]) {
      case 'D': rawValue->asDouble = floatValue; return YES;
      case 'F': rawValue->asFloat = floatValue; return YES;
    }
  } else if ([value isKindOfClass:[JavaLangInteger class]]) {
    int intValue = [(JavaLangInteger *) value intValue];
    switch ([type_ characterAtIndex:0]) {
      case 'D': rawValue->asDouble = intValue; return YES;
      case 'F': rawValue->asFloat = intValue; return YES;
      case 'I': rawValue->asInt = intValue; return YES;
      case 'J': rawValue->asLong = intValue; return YES;
    }
  } else if ([value isKindOfClass:[JavaLangLong class]]) {
    long long longValue = [(JavaLangLong *) value longValue];
    switch ([type_ characterAtIndex:0]) {
      case 'D': rawValue->asDouble = longValue; return YES;
      case 'F': rawValue->asFloat = longValue; return YES;
      case 'J': rawValue->asLong = longValue; return YES;
    }
  } else if ([value isKindOfClass:[JavaLangShort class]]) {
    short shortValue = [(JavaLangShort *) value shortValue];
    switch ([type_ characterAtIndex:0]) {
      case 'D': rawValue->asDouble = shortValue; return YES;
      case 'F': rawValue->asFloat = shortValue; return YES;
      case 'I': rawValue->asInt = shortValue; return YES;
      case 'J': rawValue->asLong = shortValue; return YES;
      case 'S': rawValue->asShort = shortValue; return YES;
    }
  } else if ([value isKindOfClass:[JavaLangBoolean class]]) {
    BOOL boolValue = [(JavaLangBoolean *) value booleanValue];
    switch ([type_ characterAtIndex:0]) {
      case 'Z': rawValue->asBOOL = boolValue; return YES;
    }
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
