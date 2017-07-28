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
#import "IOSReflection.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"
#import "objc/runtime.h"

@interface IOSProtocolClass () {
  Protocol *protocol_;
  _Atomic(IOSObjectArray *) interfaces_;
}
@end

@implementation IOSProtocolClass

static Class GetBackingClass(Protocol *protocol) {
  return objc_lookUpClass(protocol_getName(protocol));
}

@synthesize objcProtocol = protocol_;

- (instancetype)initWithProtocol:(Protocol *)protocol {
  if ((self = [super initWithMetadata:JreFindMetadata(GetBackingClass(protocol))])) {
    protocol_ = RETAIN_(protocol);
  }
  return self;
}

static jboolean ConformsToProtocol(IOSClass *cls, IOSProtocolClass *protocol) {
  if (!cls) {
    return false;
  }
  if (cls == protocol) {
    return true;
  }
  IOSObjectArray *interfaces = [cls getInterfacesInternal];
  for (int i = 0; i < interfaces->size_; i++) {
    IOSClass *interface = interfaces->buffer_[i];
    if (interface == protocol || ConformsToProtocol(interface, protocol)) {
      return true;
    }
  }
  return ConformsToProtocol([cls getSuperclass], protocol);
}

- (jboolean)isInstance:(id)object {
  return ConformsToProtocol([object java_getClass], self);
}

- (NSString *)description {
  return [NSString stringWithFormat:@"interface %@", [self getName]];
}

- (NSString *)getName {
  NSString *name = JreClassQualifiedName([self getMetadata]);
  return name ? name : NSStringFromProtocol(protocol_);
}

- (NSString *)getSimpleName {
  const J2ObjcClassInfo *metadata = [self getMetadata];
  return metadata ? JreClassTypeName(metadata) : NSStringFromProtocol(protocol_);
}

- (NSString *)objcName {
  return NSStringFromProtocol(protocol_);
}

- (void)appendMetadataName:(NSMutableString *)str {
  [str appendString:@"L"];
  [str appendString:NSStringFromProtocol(protocol_)];
  [str appendString:@";"];
}

// Returns the class with the same name as the protocol, if it exists.
- (Class) objcClass {
  return GetBackingClass(protocol_);
}

- (int)getModifiers {
  const J2ObjcClassInfo *metadata = [self getMetadata];
  if (metadata) {
    return metadata->modifiers
        & (JavaLangReflectModifier_INTERFACE | JavaLangReflectModifier_interfaceModifiers());
  }
  return JavaLangReflectModifier_PUBLIC | JavaLangReflectModifier_INTERFACE |
      JavaLangReflectModifier_ABSTRACT | JavaLangReflectModifier_STATIC;
}

- (jboolean)isAssignableFrom:(IOSClass *)cls {
  return ConformsToProtocol(cls, self);
}

- (jboolean)isInterface {
  return true;
}

- (IOSObjectArray *)getInterfacesInternal {
  IOSObjectArray *result = __c11_atomic_load(&interfaces_, __ATOMIC_ACQUIRE);
  if (!result) {
    @synchronized(self) {
      result = __c11_atomic_load(&interfaces_, __ATOMIC_RELAXED);
      if (!result) {
        unsigned int count;
        Protocol **protocolList = protocol_copyProtocolList(protocol_, &count);
        result = IOSClass_NewInterfacesFromProtocolList(protocolList, count, false);
        __c11_atomic_store(&interfaces_, result, __ATOMIC_RELEASE);
        free(protocolList);
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
