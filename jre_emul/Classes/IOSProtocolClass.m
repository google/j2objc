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

- (instancetype)initWithProtocol:(Protocol *)protocol {
  if ((self = [super init])) {
    protocol_ = RETAIN_(protocol);
  }
  return self;
}

static jboolean ConformsToProtocol(IOSClass *cls, IOSProtocolClass *protocol) {
  if (!cls) {
    return NO;
  }
  if (cls == protocol) {
    return YES;
  }
  IOSObjectArray *interfaces = [cls getInterfacesInternal];
  for (int i = 0; i < interfaces->size_; i++) {
    IOSClass *interface = interfaces->buffer_[i];
    if (interface == protocol || ConformsToProtocol(interface, protocol)) {
      return YES;
    }
  }
  return ConformsToProtocol([cls getSuperclass], protocol);
}

- (jboolean)isInstance:(id)object {
  return ConformsToProtocol([object getClass], self);
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
  JavaClassMetadata *metadata = [self getMetadata];
  if (metadata) {
    return metadata.modifiers
        & (JavaLangReflectModifier_INTERFACE | JavaLangReflectModifier_interfaceModifiers());
  }
  return JavaLangReflectModifier_PUBLIC | JavaLangReflectModifier_INTERFACE |
      JavaLangReflectModifier_ABSTRACT | JavaLangReflectModifier_STATIC;
}

- (BOOL)isAssignableFrom:(IOSClass *)cls {
  return ConformsToProtocol(cls, self);
}

- (BOOL)isInterface {
  return YES;
}

// All protocol methods are public, so publicOnly flag is ignored.
- (void)collectMethods:(NSMutableDictionary *)methodMap
            publicOnly:(BOOL)publicOnly {
  JavaClassMetadata *metadata = [self getMetadata];
  unsigned int count;
  struct objc_method_description *descriptions =
      protocol_copyMethodDescriptionList(protocol_, YES, YES, &count);
  for (unsigned int i = 0; i < count; i++) {
    struct objc_method_description *methodDesc = &descriptions[i];
    SEL sel = methodDesc->name;
    NSString *key = NSStringFromSelector(sel);
    if (![methodMap objectForKey:key]) {
      JavaMethodMetadata *methodMetadata = [metadata findMethodMetadata:key];
      if (metadata && !methodMetadata) {
        continue;  // Selector not in method list.
      }
      NSMethodSignature *signature = JreSignatureOrNull(methodDesc);
      if (!signature) {
        continue;
      }
      JavaLangReflectMethod *method =
          [JavaLangReflectMethod methodWithMethodSignature:signature
                                                  selector:sel
                                                     class:self
                                                  isStatic:NO
                                                  metadata:methodMetadata];
      [methodMap setObject:method forKey:key];
    }
  }
  free(descriptions);
}

- (JavaLangReflectMethod *)findMethodWithTranslatedName:(NSString *)objcName
                                        checkSupertypes:(BOOL)checkSupertypes {
  unsigned int count;
  JavaLangReflectMethod *result = nil;
  struct objc_method_description *descriptions =
      protocol_copyMethodDescriptionList(protocol_, YES, YES, &count);
  for (unsigned int i = 0; i < count; i++) {
    struct objc_method_description *methodDesc = &descriptions[i];
    SEL sel = methodDesc->name;
    if ([objcName isEqualToString:NSStringFromSelector(sel)]) {
      NSMethodSignature *signature = JreSignatureOrNull(methodDesc);
      if (signature) {
        JavaMethodMetadata *methodMetadata = [[self getMetadata] findMethodMetadata:objcName];
        result = [JavaLangReflectMethod methodWithMethodSignature:signature
                                                         selector:sel
                                                            class:self
                                                         isStatic:NO
                                                         metadata:methodMetadata];
      }
      break;
    }
  }
  free(descriptions);
  if (!result) {
    // Search backing class, if any.
    Class backingClass = objc_getClass(protocol_getName(protocol_));
    if (backingClass) {
      const char *name = [objcName UTF8String];
      Method method = JreFindClassMethod(backingClass, name);
      if (method) {
        NSMethodSignature *signature = JreSignatureOrNull(method_getDescription(method));
        if (signature) {
          JavaClassMetadata *metadata = [self getMetadata];
          JavaMethodMetadata *methodData = [metadata findMethodMetadata:objcName];
          result = [JavaLangReflectMethod methodWithMethodSignature:signature
                                                           selector:method_getName(method)
                                                              class:self
                                                           isStatic:YES
                                                           metadata:methodData];
        }
      }
    }
  }
  if (!result && checkSupertypes) {
    // Search super-interfaces.
    for (IOSClass *cls in [self getInterfacesInternal]) {
      if (cls != self) {
        result = [cls findMethodWithTranslatedName:objcName checkSupertypes:checkSupertypes];
        if (result) {
          break;
        }
      }
    }
  }
  return result;
}

- (IOSObjectArray *)getInterfacesInternal {
  IOSObjectArray *result = interfaces_;
  OSMemoryBarrier();
  if (!result) {
    @synchronized(self) {
      result = interfaces_;
      if (!result) {
        unsigned int count;
        Protocol **protocolList = protocol_copyProtocolList(protocol_, &count);
        result = IOSClass_NewInterfacesFromProtocolList(protocolList, count);
        free(protocolList);
        OSMemoryBarrier();
        interfaces_ = result;
      }
    }
  }
  return result;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [protocol_ release];
  [super dealloc];
}
#endif

@end
