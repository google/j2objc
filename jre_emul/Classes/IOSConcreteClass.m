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
//  IOSConcreteClass.m
//  JreEmulation
//
//  Created by Keith Stanger on 8/16/13.
//

#import "IOSConcreteClass.h"
#import "JavaMetadata.h"
#import "java/lang/ClassCastException.h"
#import "java/lang/Enum.h"
#import "java/lang/InstantiationException.h"
#import "java/lang/NoSuchMethodException.h"
#import "java/lang/Void.h"
#import "java/lang/reflect/Constructor.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"
#import "objc/runtime.h"

@implementation IOSConcreteClass

@synthesize objcClass = class_;

- (id)initWithClass:(Class)cls {
  if ((self = [super init])) {
    class_ = RETAIN_(cls);
  }
  return self;
}

- (id)newInstance {
  // Per the JLS spec, throw an InstantiationException if the type is an
  // interface (no class_), array or primitive type (IOSClass types), or void.
  if ([class_ isKindOfClass:[IOSClass class]] || [class_ isMemberOfClass:[JavaLangVoid class]]) {
    @throw AUTORELEASE([[JavaLangInstantiationException alloc] init]);
  }
  return AUTORELEASE([[class_ alloc] init]);
}

- (IOSClass *)getSuperclass {
  Class superclass = [class_ superclass];
  if (superclass != nil) {
    return [IOSClass classWithClass:superclass];
  }
  return nil;
}

- (BOOL)isInstance:(id)object {
  return [object isKindOfClass:class_];
}

- (NSString *)getName {
  JavaClassMetadata *metadata = [self getMetadata];
  return metadata ? [metadata qualifiedName] : NSStringFromClass(class_);
}

- (NSString *)getSimpleName {
  JavaClassMetadata *metadata = [self getMetadata];
  return metadata ? metadata.typeName : NSStringFromClass(class_);
}

- (NSString *)objcName {
  return NSStringFromClass(class_);
}

- (BOOL)isAssignableFrom:(IOSClass *)cls {
  return [cls.objcClass isSubclassOfClass:class_];
}

- (IOSClass *)asSubclass:(IOSClass *)cls {
  Class otherClass = cls.objcClass;
  if (otherClass == nil || ![class_ isSubclassOfClass:otherClass]) {
    @throw AUTORELEASE([[JavaLangClassCastException alloc] init]);
  }
  return self;
}

- (BOOL)isEnum {
  JavaClassMetadata *metadata = [self getMetadata];
  if (metadata) {
    return (metadata.modifiers & JavaLangReflectModifier_ENUM) > 0 &&
        [self getSuperclass] == [JavaLangEnum getClass];
  } else {
    return class_ != nil && [NSStringFromClass(class_) hasSuffix:@"Enum"];
  }
}

- (BOOL)isAnonymousClass {
  JavaClassMetadata *metadata = [self getMetadata];
  if (metadata) {
    return (metadata.modifiers & 0x8000) > 0;
  }
  return NO;
}

static BOOL IsConstructor(NSString *name) {
  return [name isEqualToString:@"init"] || [name hasPrefix:@"initWith"];
}

static void CreateMethodWrappers(
    Method *methods, unsigned count, IOSClass* clazz, NSMutableDictionary *map,
    id (^methodCreator)(SEL)) {
  for (NSUInteger i = 0; i < count; i++) {
    SEL sel = method_getName(methods[i]);
    NSString *key = NSStringFromSelector(sel);
    if ([map objectForKey:key]) {
      continue;
    }
    id method = methodCreator(sel);
    if (method) {
      [map setObject:method forKey:NSStringFromSelector(sel)];
    }
  }
}

static void CollectMethodsOrConstructors(
    IOSConcreteClass *iosClass, NSMutableDictionary *methods, id (^methodCreator)(SEL)) {
  unsigned int nInstanceMethods, nClassMethods;
  // Copy first the instance, then the class methods into a combined
  // array of IOSMethod instances.  Method ordering is not defined or
  // important.
  Method *instanceMethods = class_copyMethodList(iosClass->class_, &nInstanceMethods);
  CreateMethodWrappers(instanceMethods, nInstanceMethods, iosClass, methods, methodCreator);

  Method *classMethods = class_copyMethodList(object_getClass(iosClass->class_), &nClassMethods);
  CreateMethodWrappers(classMethods, nClassMethods, iosClass, methods, methodCreator);

  free(instanceMethods);
  free(classMethods);
}

- (void)collectMethods:(NSMutableDictionary *)methodMap {
  JavaClassMetadata *metadata = [self getMetadata];
  CollectMethodsOrConstructors(self, methodMap, ^ id (SEL sel) {
    NSString *selStr = NSStringFromSelector(sel);
    if (!IsConstructor(selStr)) {
      return [JavaLangReflectMethod methodWithSelector:sel withClass:self
          withMetadata:metadata ? [metadata findMethodInfo:selStr] : nil];
    }
    return nil;
  });
}

- (IOSObjectArray *)getDeclaredConstructors {
  NSMutableDictionary *methodMap = [NSMutableDictionary dictionary];
  CollectMethodsOrConstructors(self, methodMap, ^ id (SEL sel) {
    if (IsConstructor(NSStringFromSelector(sel))) {
      return [JavaLangReflectConstructor constructorWithSelector:sel withClass:self];
    }
    return nil;
  });
  return [IOSObjectArray arrayWithNSArray:[methodMap allValues] type:
      [IOSClass classWithClass:[JavaLangReflectConstructor class]]];
}

- (IOSObjectArray *)getConstructors {
  return [self getDeclaredConstructors];
}

static SEL FindSelector(NSString *name, Class cls) {
  unsigned int count;
  SEL result = nil;
  Method *methods = class_copyMethodList(cls, &count);
  for (NSUInteger i = 0; i < count; i++) {
    SEL sel = method_getName(methods[i]);
    if ([name isEqualToString:NSStringFromSelector(sel)]) {
      result = sel;
      break;
    }
  }
  free(methods);
  return result;
}

- (JavaLangReflectMethod *)findMethodWithTranslatedName:(NSString *)objcName {
  SEL selector = FindSelector(objcName, class_);
  if (selector) {
    JavaClassMetadata *metadata = [self getMetadata];
    return [JavaLangReflectMethod methodWithSelector:selector withClass:self
        withMetadata:metadata ? [metadata findMethodInfo:objcName] : nil];
  }
  selector = FindSelector(objcName, object_getClass(class_));
  if (selector) {
    JavaClassMetadata *metadata = [self getMetadata];
    return [JavaLangReflectMethod methodWithSelector:selector withClass:self
        withMetadata:metadata ? [metadata findMethodInfo:objcName] : nil];
  }
  return nil;
}

static JavaLangReflectConstructor *GetConstructorImpl(
    IOSConcreteClass *iosClass, IOSObjectArray *paramTypes) {
  NSString *name = IOSClass_GetTranslatedMethodName(@"init", paramTypes);
  SEL selector = FindSelector(name, iosClass->class_);
  if (selector) {
    return [JavaLangReflectConstructor constructorWithSelector:selector withClass:iosClass];
  }
  @throw AUTORELEASE([[JavaLangNoSuchMethodException alloc] init]);
}

- (JavaLangReflectConstructor *)getConstructor:(IOSObjectArray *)parameterTypes {
  // Java's getConstructor() only returns the constructor if it's public.
  // However, all constructors in Objective-C are public, so this method
  // is identical to getDeclaredConstructor().
  // TODO(tball): Update when modifier metadata is implemented.
  return GetConstructorImpl(self, parameterTypes);
}

- (JavaLangReflectConstructor *)getDeclaredConstructor:(IOSObjectArray *)parameterTypes {
  return GetConstructorImpl(self, parameterTypes);
}

- (IOSObjectArray *)getInterfacesWithArrayType:(IOSClass *)arrayType {
  NSMutableArray *allInterfaces = [NSMutableArray array];
  Class cls = class_;
  while (cls) {
    unsigned int outCount;
    Protocol * __unsafe_unretained *interfaces = class_copyProtocolList(class_, &outCount);
    for (unsigned i = 0; i < outCount; i++) {
      IOSClass *interface = [IOSClass classWithProtocol:interfaces[i]];
      if (![allInterfaces containsObject:interface]) {
        [allInterfaces addObject:interface];
      }
    }
    free(interfaces);
    cls = [cls superclass];
  }
  return [IOSObjectArray arrayWithNSArray:allInterfaces
                                     type:[IOSClass getClass]];
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [class_ release];
  [super dealloc];
}
#endif

@end
