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
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"
#import "objc/runtime.h"

@implementation IOSProtocolClass

@synthesize objcProtocol = protocol_;

- (id)initWithProtocol:(Protocol *)protocol {
  if ((self = [super init])) {
    protocol_ = RETAIN(protocol);
  }
  return self;
}

- (BOOL)isInstance:(id)object {
  return [object conformsToProtocol:protocol_];
}

- (NSString *)getName {
  return NSStringFromProtocol(protocol_);
}

- (int)getModifiers {
  return JavaLangReflectModifier_PUBLIC | JavaLangReflectModifier_INTERFACE;
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
  unsigned int count;
  struct objc_method_description *descriptions =
      protocol_copyMethodDescriptionList(protocol_, YES, YES, &count);
  for (unsigned int i = 0; i < count; i++) {
    SEL sel = descriptions[i].name;
    NSString *key = NSStringFromSelector(sel);
    if (![methodMap objectForKey:key]) {
      [methodMap setObject:[JavaLangReflectMethod methodWithSelector:sel withClass:self]
                    forKey:key];
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
    return [JavaLangReflectMethod methodWithSelector:result withClass:self];
  }
  return nil;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [protocol_ release];
  [super dealloc];
}
#endif

@end
