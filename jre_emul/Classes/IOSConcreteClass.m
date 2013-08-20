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
#import "java/lang/ClassCastException.h"
#import "java/lang/InstantiationException.h"
#import "java/lang/NoSuchMethodException.h"
#import "java/lang/Void.h"
#import "java/lang/reflect/Constructor.h"
#import "java/lang/reflect/Method.h"
#import "objc/runtime.h"

@implementation IOSConcreteClass

@synthesize objcClass = class_;

- (id)initWithClass:(Class)cls {
  if ((self = [super init])) {
    class_ = RETAIN(cls);
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

- (IOSClass *)getEnclosingClass {
  NSString *className = NSStringFromClass(class_);
  NSRange r = [className rangeOfString:@"_" options:NSBackwardsSearch];
  if (r.location == NSNotFound) {
    return nil;
  }
  NSString *enclosingName = [className substringToIndex:r.location];
  Class class = NSClassFromString(enclosingName);
  return class ? [IOSClass classWithClass:class] : nil;
}

- (BOOL)isEnum {
  return class_ != nil && [NSStringFromClass(class_) hasSuffix:@"Enum"];
}

- (BOOL)isMemberClass {
  IOSClass *enclosingClass = [self getEnclosingClass];
  if (!enclosingClass) {
    return NO;
  }

  // Extract member class name.
  NSString *className = NSStringFromClass(class_);
  NSString *enclosingClassName = [enclosingClass getName];
  NSString *memberClassName =
      [className substringFromIndex:[enclosingClassName length] + 1];  // Include trailing '_'.

  // Anonymous classes are not considered member classes.
  NSRange range = [memberClassName rangeOfString:@"$"];
  return range.location == NSNotFound;
}

- (BOOL)isAnonymousClass {
  return strchr(class_getName(class_), '?') != NULL;
}

static void AddMethodOrConstructor(
    SEL sel, IOSClass *clazz, NSMutableDictionary *map, BOOL fetchConstructors) {
  NSString *key = NSStringFromSelector(sel);
  BOOL isConstructor =
      [key isEqualToString:@"init"] || [key hasPrefix:@"initWith"];
  if (isConstructor == fetchConstructors && ![map objectForKey:key]) {
    id executable = isConstructor ?
        [JavaLangReflectConstructor constructorWithSelector:sel withClass:clazz] :
        [JavaLangReflectMethod methodWithSelector:sel withClass:clazz];
    [map setObject:executable forKey:key];
  }
}

static void CreateMethodWrappers(
    Method *methods, unsigned count, IOSClass* clazz, NSMutableDictionary *map,
    BOOL fetchConstructors) {
  for (NSUInteger i = 0; i < count; i++) {
    SEL sel = method_getName(methods[i]);
    AddMethodOrConstructor(sel, clazz, map, fetchConstructors);
  }
}

static void CollectMethodsOrConstructors(
    IOSConcreteClass *iosClass, NSMutableDictionary *methods, BOOL fetchConstructors) {
  unsigned int nInstanceMethods, nClassMethods;
  // Copy first the instance, then the class methods into a combined
  // array of IOSMethod instances.  Method ordering is not defined or
  // important.
  Method *instanceMethods = class_copyMethodList(iosClass->class_, &nInstanceMethods);
  CreateMethodWrappers(instanceMethods, nInstanceMethods, iosClass, methods, fetchConstructors);

  Method *classMethods = class_copyMethodList(object_getClass(iosClass->class_), &nClassMethods);
  CreateMethodWrappers(classMethods, nClassMethods, iosClass, methods, fetchConstructors);

  free(instanceMethods);
  free(classMethods);
}

- (void)collectMethods:(NSMutableDictionary *)methodMap {
  CollectMethodsOrConstructors(self, methodMap, NO);
}

- (IOSObjectArray *)getDeclaredConstructors {
  NSMutableDictionary *methodMap = [NSMutableDictionary dictionary];
  CollectMethodsOrConstructors(self, methodMap, YES);
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
    return [JavaLangReflectMethod methodWithSelector:selector withClass:self];
  }
  selector = FindSelector(objcName, object_getClass(class_));
  if (selector) {
    return [JavaLangReflectMethod methodWithSelector:selector withClass:self];
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
  // TODO(user): Update when modifier metadata is implemented.
  return GetConstructorImpl(self, parameterTypes);
}

- (JavaLangReflectConstructor *)getDeclaredConstructor:(IOSObjectArray *)parameterTypes {
  return GetConstructorImpl(self, parameterTypes);
}

- (IOSObjectArray *)getInterfacesWithArrayType:(IOSClass *)arrayType {
  unsigned int outCount;
  Protocol * __unsafe_unretained *interfaces = class_copyProtocolList(class_, &outCount);
  IOSObjectArray *result = [IOSObjectArray arrayWithLength:outCount type:arrayType];
  for (unsigned i = 0; i < outCount; i++) {
    [result replaceObjectAtIndex:i withObject:[IOSClass classWithProtocol:interfaces[i]]];
  }
  free(interfaces);
  return result;
}

#if ! __has_feature(objc_arc)
- (void)dealloc {
  [class_ release];
  [super dealloc];
}
#endif

@end
