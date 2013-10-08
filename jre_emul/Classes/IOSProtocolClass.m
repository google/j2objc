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
//  IOSProtocolClass.m
//  JreEmulation
//
//  Created by Keith Stanger on 8/16/13.
//

#import "IOSProtocolClass.h"
#import "JavaMetadata.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"
#import "objc/runtime.h"

@implementation IOSProtocolClass

@synthesize objcProtocol = protocol_;

- (id)initWithProtocol:(Protocol *)protocol {
  if ((self = [super init])) {
    protocol_ = RETAIN_(protocol);
  }
  return self;
}

- (BOOL)isInstance:(id)object {
  return [object conformsToProtocol:protocol_];
}

- (NSString *)getName {
  JavaClassMetadata *metadata = [self getMetadata];
  return metadata ? [metadata qualifiedName] : NSStringFromProtocol(protocol_);
}

- (NSString *)getSimpleName {
  JavaClassMetadata *metadata = [self getMetadata];
  return metadata ? RETAIN_(metadata.typeName) : NSStringFromProtocol(protocol_);
}

- (NSString *)objcName {
  return NSStringFromProtocol(protocol_);
}

// Returns the class with the same name as the protocol, if it exists.
- (Class) objcClass {
  return objc_lookUpClass(protocol_getName(protocol_));
}

- (int)getModifiers {
  return JavaLangReflectModifier_PUBLIC | JavaLangReflectModifier_INTERFACE |
      JavaLangReflectModifier_ABSTRACT | JavaLangReflectModifier_STATIC;
}

- (BOOL)isAssignableFrom:(IOSClass *)cls {
  Protocol *otherProtocol = cls.objcProtocol;
  if (otherProtocol) {
    return protocol_conformsToProtocol(otherProtocol, protocol_);
  }
  return [cls.objcClass conformsToProtocol:protocol_];
}

- (BOOL)isInterface {
  return YES;
}

- (void)collectMethods:(NSMutableDictionary *)methodMap {
  JavaClassMetadata *metadata = [self getMetadata];
  unsigned int count;
  struct objc_method_description *descriptions =
      protocol_copyMethodDescriptionList(protocol_, YES, YES, &count);
  for (unsigned int i = 0; i < count; i++) {
    SEL sel = descriptions[i].name;
    NSString *key = NSStringFromSelector(sel);
    if (![methodMap objectForKey:key]) {
      JavaLangReflectMethod *method = [JavaLangReflectMethod methodWithSelector:sel withClass:self
          withMetadata:metadata ? [metadata findMethodInfo:key] : nil];
      [methodMap setObject:method forKey:key];
    }
  }
  free(descriptions);
}

- (JavaLangReflectMethod *)findMethodWithTranslatedName:(NSString *)objcName {
  unsigned int count;
  SEL result = nil;
  struct objc_method_description *descriptions =
      protocol_copyMethodDescriptionList(protocol_, YES, YES, &count);
  for (unsigned int i = 0; i < count; i++) {
    SEL sel = descriptions[i].name;
    if ([objcName isEqualToString:NSStringFromSelector(sel)]) {
      result = sel;
      break;
    }
  }
  free(descriptions);
  if (result) {
    JavaClassMetadata *metadata = [self getMetadata];
    return [JavaLangReflectMethod methodWithSelector:result withClass:self
        withMetadata:metadata ? [metadata findMethodInfo:objcName] : nil];
  }
  return nil;
}

- (IOSObjectArray *)getInterfacesWithArrayType:(IOSClass *)arrayType {
  unsigned int outCount;
  Protocol * __unsafe_unretained *interfaces = protocol_copyProtocolList(protocol_, &outCount);
  IOSObjectArray *result = [IOSObjectArray arrayWithLength:outCount type:[IOSClass getClass]];
  for (unsigned i = 0; i < outCount; i++) {
    IOSClass *interface = [IOSClass classWithProtocol:interfaces[i]];
    [result replaceObjectAtIndex:i withObject:interface];
  }
  free(interfaces);
  return result;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [protocol_ release];
  [super dealloc];
}
#endif

@end
