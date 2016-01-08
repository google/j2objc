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
#import "IOSReflection.h"
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

@interface IOSConcreteClass () {
  _Atomic(IOSObjectArray *) interfaces_;
}
@end

@implementation IOSConcreteClass

@synthesize objcClass = class_;

- (instancetype)initWithClass:(Class)cls {
  if ((self = [super initWithClass:cls])) {
    class_ = cls;
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
  // Number and Throwable are special cases where their superclass doesn't match Java's.
  if (strcmp("NSNumber", class_getName(class_)) == 0
      || strcmp("JavaLangThrowable", class_getName(class_)) == 0) {
    return NSObject_class_();
  }
  Class superclass = [class_ superclass];
  if (superclass != nil) {
    return IOSClass_fromClass(superclass);
  }
  return nil;
}

- (jboolean)isInstance:(id)object {
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

- (jboolean)isAssignableFrom:(IOSClass *)cls {
  return class_ == [NSObject class] ? ![cls isPrimitive] : [cls.objcClass isSubclassOfClass:class_];
}

- (jboolean)isEnum {
  JavaClassMetadata *metadata = [self getMetadata];
  if (metadata) {
    return (metadata.modifiers & JavaLangReflectModifier_ENUM) > 0 &&
        [self getSuperclass] == JavaLangEnum_class_();
  } else {
    return class_ != nil && strcmp(class_getName(class_getSuperclass(class_)), "JavaLangEnum") == 0;
  }
}

- (jboolean)isAnonymousClass {
  JavaClassMetadata *metadata = [self getMetadata];
  if (metadata) {
    return (metadata.modifiers & 0x8000) > 0;
  }
  return false;
}

static jboolean IsConstructor(NSString *name) {
  return [name isEqualToString:@"init"] || [name hasPrefix:@"initWith"];
}

// Returns true if the parameter and return types are all Objective-C
// classes or primitive types.
static jboolean IsValidMethod(ExecutableMember *method) {
  if (!validTypeEncoding([method.signature methodReturnType])) {
    return false;
  }
  NSUInteger nArgs = [method.signature numberOfArguments];
  // Check each argument type, skipping the self and selector arguments.
  for (NSUInteger i = 2; i < nArgs; i++) {
    if (!validTypeEncoding([method.signature getArgumentTypeAtIndex:i])) {
      return false;
    }
  }
  return true;
}

static void CreateMethodWrappers(
    Method *methods, unsigned count, IOSClass* clazz, NSMutableDictionary *map,
    jboolean publicOnly, jboolean classMethods,
    id (^methodCreator)(NSMethodSignature *, SEL, jboolean)) {
  for (NSUInteger i = 0; i < count; i++) {
    Method method = methods[i];
    SEL sel = method_getName(method);
    NSString *key = NSStringFromSelector(sel);
    if ([map objectForKey:key]) {
      continue;
    }
    NSMethodSignature *methodSignature = JreSignatureOrNull(method_getDescription(method));
    if (!methodSignature) {
      continue;
    }
    ExecutableMember *wrapper = methodCreator(methodSignature, sel, classMethods);
    if (wrapper) {
      if (publicOnly && ([wrapper getModifiers] & JavaLangReflectModifier_PUBLIC) == 0) {
        continue;
      }
      if (!IsValidMethod(wrapper)) {
        continue;
      }
      [map setObject:wrapper forKey:key];
    }
  }
}

static void CollectMethodsOrConstructors(
    IOSConcreteClass *iosClass, NSMutableDictionary *methods, jboolean publicOnly,
    id (^methodCreator)(NSMethodSignature *, SEL, jboolean)) {
  unsigned int nInstanceMethods, nClassMethods;
  // Copy first the instance, then the class methods into a combined
  // array of IOSMethod instances.  Method ordering is not defined or
  // important.
  Method *instanceMethods = class_copyMethodList(iosClass->class_, &nInstanceMethods);
  CreateMethodWrappers(
      instanceMethods, nInstanceMethods, iosClass, methods, publicOnly, false, methodCreator);

  Method *classMethods = class_copyMethodList(object_getClass(iosClass->class_), &nClassMethods);
  CreateMethodWrappers(
      classMethods, nClassMethods, iosClass, methods, publicOnly, true, methodCreator);

  free(instanceMethods);
  free(classMethods);
}

- (void)collectMethods:(NSMutableDictionary *)methodMap
            publicOnly:(jboolean)publicOnly {
  JavaClassMetadata *metadata = [self getMetadata];
  CollectMethodsOrConstructors(
      self, methodMap, publicOnly, ^ id (NSMethodSignature *signature, SEL sel, jboolean isStatic) {
    NSString *selStr = NSStringFromSelector(sel);
    if (!IsConstructor(selStr)) {
      JavaMethodMetadata *methodMetadata = [metadata findMethodMetadata:selStr];
      if (metadata && !methodMetadata) {
        return nil;  // Selector not in method list.
      }
      return [JavaLangReflectMethod methodWithMethodSignature:signature
                                                     selector:sel
                                                        class:self
                                                     isStatic:isStatic
                                                     metadata:methodMetadata];
    }
    return nil;
  });
}

IOSObjectArray *getConstructorsImpl(IOSConcreteClass *clazz, jboolean publicOnly) {
  JavaClassMetadata *metadata = [clazz getMetadata];
  NSMutableDictionary *methodMap = [NSMutableDictionary dictionary];
  CollectMethodsOrConstructors(
      clazz, methodMap, publicOnly,
      ^ id (NSMethodSignature *signature, SEL sel, jboolean isStatic) {
    if (IsConstructor(NSStringFromSelector(sel))) {
      NSString *selStr = NSStringFromSelector(sel);
      JavaMethodMetadata *methodMetadata = [metadata findMethodMetadata:selStr];
      if (metadata && !methodMetadata) {
        return nil;  // Selector not in method list.
      }
      return [JavaLangReflectConstructor constructorWithMethodSignature:signature
                                                               selector:sel
                                                                  class:clazz
                                                               metadata:methodMetadata];
    }
    return nil;
  });
  return [IOSObjectArray arrayWithNSArray:[methodMap allValues]
      type:JavaLangReflectConstructor_class_()];
}

- (IOSObjectArray *)getDeclaredConstructors {
  return getConstructorsImpl(self, false);
}

- (IOSObjectArray *)getConstructors {
  return getConstructorsImpl(self, true);
}

- (JavaLangReflectMethod *)findMethodWithTranslatedName:(NSString *)objcName
                                        checkSupertypes:(jboolean)checkSupertypes {
  const char *name = [objcName UTF8String];
  jboolean isStatic = false;
  Method method = JreFindInstanceMethod(class_, name);
  if (!method) {
    method = JreFindClassMethod(class_, name);
    isStatic = true;
  }
  if (!method) {
    return nil;
  }
  NSMethodSignature *signature = JreSignatureOrNull(method_getDescription(method));
  if (!signature) {
    return nil;
  }
  JavaClassMetadata *metadata = [self getMetadata];
  return [JavaLangReflectMethod methodWithMethodSignature:signature
                                                 selector:method_getName(method)
                                                    class:self
                                                 isStatic:isStatic
                                                 metadata:[metadata findMethodMetadata:objcName]];
}

static JavaLangReflectConstructor *GetConstructorImpl(
    IOSConcreteClass *iosClass, NSString *name) {
  Method method = JreFindInstanceMethod(iosClass->class_, [name UTF8String]);
  if (method) {
    NSMethodSignature *signature = JreSignatureOrNull(method_getDescription(method));
    if (signature) {
      JavaClassMetadata *metadata = [iosClass getMetadata];
      return [JavaLangReflectConstructor
          constructorWithMethodSignature:signature
                                selector:method_getName(method)
                                   class:iosClass
                                metadata:[metadata findMethodMetadata:name]];
    }
  }
  @throw AUTORELEASE([[JavaLangNoSuchMethodException alloc] init]);
}

- (JavaLangReflectConstructor *)getConstructor:(IOSObjectArray *)parameterTypes {
  JavaLangReflectConstructor *c =
      GetConstructorImpl(self, IOSClass_GetTranslatedMethodName(nil, @"init", parameterTypes));
  if (([c getModifiers] & JavaLangReflectModifier_PUBLIC) > 0) {
    return c;
  }
  @throw AUTORELEASE([[JavaLangNoSuchMethodException alloc] init]);
}

- (JavaLangReflectConstructor *)getDeclaredConstructor:(IOSObjectArray *)parameterTypes {
  return
      GetConstructorImpl(self, IOSClass_GetTranslatedMethodName(nil, @"init", parameterTypes));
}

- (JavaLangReflectConstructor *)findConstructorWithTranslatedName:(NSString *)selector {
  return GetConstructorImpl(self, selector);
}

- (IOSObjectArray *)getInterfacesInternal {
  IOSObjectArray *result = __c11_atomic_load(&interfaces_, __ATOMIC_ACQUIRE);
  if (!result) {
    @synchronized(self) {
      result = __c11_atomic_load(&interfaces_, __ATOMIC_RELAXED);
      if (!result) {
        unsigned int count;
        Protocol **protocolList = class_copyProtocolList(class_, &count);
        result = IOSClass_NewInterfacesFromProtocolList(protocolList, count);
        __c11_atomic_store(&interfaces_, result, __ATOMIC_RELEASE);
        free(protocolList);
      }
    }
  }
  return result;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [class_ release];
  [super dealloc];
}
#endif

@end
