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
//  IOSArrayClass.m
//  JreEmulation
//
//  Created by Tom Ball on 1/23/12.
//

#import "IOSArrayClass.h"
#import "IOSObjectArray.h"
#import "IOSPrimitiveClass.h"
#import "NSCopying+JavaCloneable.h"
#import "java/io/Serializable.h"
#import "java/lang/InstantiationException.h"
#import "java/lang/reflect/Modifier.h"

@implementation IOSArrayClass

- (instancetype)initWithComponentType:(IOSClass *)type {
  if ((self = [super initWithClass:NULL])) {
    componentType_ = RETAIN_(type);
  }
  return self;
}

- (IOSClass *)getComponentType {
  return componentType_;
}

- (jboolean)isArray {
  return true;
}

- (IOSClass *)getSuperclass {
  return NSObject_class_();
}

- (jboolean)isInstance:(id)object {
  IOSClass *objClass = [object getClass];
  return [objClass isArray] && [componentType_ isAssignableFrom:[objClass getComponentType]];
}

- (jboolean)isAssignableFrom:(IOSClass *)cls {
  return [cls isArray] && [componentType_ isAssignableFrom:[cls getComponentType]];
}

- (NSString *)getName {
  return [self binaryName];
}

- (NSString *)getSimpleName {
  return [[[self getComponentType] getSimpleName] stringByAppendingString:@"[]"];
}

- (NSString *)binaryName {
  return [NSString stringWithFormat:@"[%@", [componentType_ binaryName]];
}

- (NSString *)objcName {
  return [[[self getComponentType] objcName] stringByAppendingString:@"Array"];
}

- (NSString *)getCanonicalName {
  return [NSString stringWithFormat:@"%@[]", [componentType_ getCanonicalName]];
}

- (IOSObjectArray *)getInterfacesInternal {
  static dispatch_once_t onceToken;
  static IOSObjectArray *arrayInterfaces;
  dispatch_once(&onceToken, ^{
    arrayInterfaces = [IOSObjectArray newArrayWithObjects:(id[]){
        NSCopying_class_(), JavaIoSerializable_class_() }
        count:2 type:IOSClass_class_()];
  });
  return arrayInterfaces;
}

- (id)newInstance {
  if (!componentType_) {
    @throw AUTORELEASE([[JavaLangInstantiationException alloc] init]);
  }
  return [IOSObjectArray arrayWithLength:0 type:componentType_];
}

- (int)getModifiers {
  return JavaLangReflectModifier_PUBLIC | JavaLangReflectModifier_ABSTRACT
      | JavaLangReflectModifier_FINAL;
}

- (BOOL)isEqual:(id)anObject {
  return [anObject isKindOfClass:[IOSArrayClass class]] &&
    [componentType_ isEqual:[anObject getComponentType]];
}

- (NSString *)description {
  return [NSString stringWithFormat:@"class %@", [self getName]];
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [componentType_ release];
  [super dealloc];
}
#endif

@end
