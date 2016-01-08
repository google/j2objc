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

#import "IOSPrimitiveArray.h"
#import "IOSPrimitiveClass.h"
#import "IOSObjectArray.h"
#import "java/lang/Boolean.h"
#import "java/lang/Byte.h"
#import "java/lang/Character.h"
#import "java/lang/Double.h"
#import "java/lang/Float.h"
#import "java/lang/Integer.h"
#import "java/lang/Long.h"
#import "java/lang/NoSuchMethodException.h"
#import "java/lang/Short.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"

@implementation IOSPrimitiveClass

- (instancetype)initWithName:(NSString *)name type:(NSString *)type {
  if ((self = [super initWithClass:NULL])) {
    name_ = RETAIN_(name);
    type_ = RETAIN_(type);
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

- (jboolean)isAssignableFrom:(IOSClass *)cls {
  return [self isEqual:cls];
}

- (jboolean)isInstance:(id)object {
  return false;  // Objects can't be primitives.
}

- (int)getModifiers {
  return JavaLangReflectModifier_PUBLIC | JavaLangReflectModifier_FINAL |
      JavaLangReflectModifier_ABSTRACT;
}

- (IOSObjectArray *)getDeclaredMethods {
  return [IOSObjectArray arrayWithLength:0 type:JavaLangReflectMethod_class_()];
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

- (jboolean)isPrimitive {
  return true;
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

- (Class)objcArrayClass {
  switch ([type_ characterAtIndex:0]) {
    case 'B': return [IOSByteArray class];
    case 'C': return [IOSCharArray class];
    case 'D': return [IOSDoubleArray class];
    case 'F': return [IOSFloatArray class];
    case 'I': return [IOSIntArray class];
    case 'J': return [IOSLongArray class];
    case 'S': return [IOSShortArray class];
    case 'Z': return [IOSBooleanArray class];
  }
  return nil;
}

- (size_t)getSizeof {
  switch ([type_ characterAtIndex:0]) {
    case 'B': return sizeof(jbyte);
    case 'C': return sizeof(jchar);
    case 'D': return sizeof(jdouble);
    case 'F': return sizeof(jfloat);
    case 'I': return sizeof(jint);
    case 'J': return sizeof(jlong);
    case 'S': return sizeof(jshort);
    case 'Z': return sizeof(jboolean);
  }
  return 0;
}

- (id)__boxValue:(J2ObjcRawValue *)rawValue {
  switch ([type_ characterAtIndex:0]) {
    case 'B': return JavaLangByte_valueOfWithByte_(rawValue->asChar);
    case 'C': return JavaLangCharacter_valueOfWithChar_(rawValue->asUnichar);
    case 'D': return JavaLangDouble_valueOfWithDouble_(rawValue->asDouble);
    case 'F': return JavaLangFloat_valueOfWithFloat_(rawValue->asFloat);
    case 'I': return JavaLangInteger_valueOfWithInt_(rawValue->asInt);
    case 'J': return JavaLangLong_valueOfWithLong_(rawValue->asLong);
    case 'S': return JavaLangShort_valueOfWithShort_(rawValue->asShort);
    case 'Z': return JavaLangBoolean_valueOfWithBoolean_(rawValue->asBOOL);
  }
  return nil;
}

- (id)wrapperClass {
  switch ([type_ characterAtIndex:0]) {
    case 'B': return JavaLangByte_class_();
    case 'C': return JavaLangCharacter_class_();
    case 'D': return JavaLangDouble_class_();
    case 'F': return JavaLangFloat_class_();
    case 'I': return JavaLangInteger_class_();
    case 'J': return JavaLangLong_class_();
    case 'S': return JavaLangShort_class_();
    case 'Z': return JavaLangBoolean_class_();
  }
  return nil;
}

- (jboolean)__unboxValue:(id)value toRawValue:(J2ObjcRawValue *)rawValue {
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
  return false;
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
    case 'Z': rawValue->asBOOL = *(jboolean *)addr; return;
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
    case 'Z': *(jboolean *)addr = rawValue->asBOOL; return;
  }
}

- (jboolean)__convertRawValue:(J2ObjcRawValue *)rawValue toType:(IOSClass *)toType {
  if (![toType isPrimitive]) {
    return false;
  }
  unichar toTypeChar = [((IOSPrimitiveClass *)toType)->type_ characterAtIndex:0];
  switch ([type_ characterAtIndex:0]) {
    case 'B':
      switch (toTypeChar) {
        case 'B': return true;
        case 'D': rawValue->asDouble = rawValue->asChar; return true;
        case 'F': rawValue->asFloat = rawValue->asChar; return true;
        case 'I': rawValue->asInt = rawValue->asChar; return true;
        case 'J': rawValue->asLong = rawValue->asChar; return true;
        case 'S': rawValue->asShort = rawValue->asChar; return true;
      }
      return false;
    case 'C':
      switch (toTypeChar) {
        case 'C': return true;
        case 'D': rawValue->asDouble = rawValue->asUnichar; return true;
        case 'F': rawValue->asFloat = rawValue->asUnichar; return true;
        case 'I': rawValue->asInt = rawValue->asUnichar; return true;
        case 'J': rawValue->asLong = rawValue->asUnichar; return true;
      }
      return false;
    case 'D':
      switch (toTypeChar) {
        case 'D': return true;
      }
      return false;
    case 'F':
      switch (toTypeChar) {
        case 'D': rawValue->asDouble = rawValue->asFloat; return true;
        case 'F': return true;
      }
      return false;
    case 'I':
      switch (toTypeChar) {
        case 'D': rawValue->asDouble = rawValue->asInt; return true;
        case 'F': rawValue->asFloat = rawValue->asInt; return true;
        case 'I': return true;
        case 'J': rawValue->asLong = rawValue->asInt; return true;
      }
      return false;
    case 'J':
      switch (toTypeChar) {
        case 'D': rawValue->asDouble = rawValue->asLong; return true;
        case 'F': rawValue->asFloat = rawValue->asLong; return true;
        case 'J': return true;
      }
      return false;
    case 'S':
      switch (toTypeChar) {
        case 'D': rawValue->asDouble = rawValue->asShort; return true;
        case 'F': rawValue->asFloat = rawValue->asShort; return true;
        case 'I': rawValue->asInt = rawValue->asShort; return true;
        case 'J': rawValue->asLong = rawValue->asShort; return true;
        case 'S': return true;
      }
      return false;
    case 'Z':
      switch (toTypeChar) {
        case 'Z': return true;
      }
      return false;
  }
  return false;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [name_ release];
  [type_ release];
  [super dealloc];
}
#endif

@end
