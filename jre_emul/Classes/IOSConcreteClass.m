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
  // Number is a special case where its superclass doesn't match Java's.
  if (strcmp("NSNumber", class_getName(class_)) == 0) {
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
  const J2ObjcClassInfo *metadata = [self getMetadata];
  return metadata ? JreClassQualifiedName(metadata) : NSStringFromClass(class_);
}

- (NSString *)getSimpleName {
  const J2ObjcClassInfo *metadata = [self getMetadata];
  return metadata ? JreClassTypeName(metadata) : NSStringFromClass(class_);
}

- (NSString *)objcName {
  return NSStringFromClass(class_);
}

- (jboolean)isAssignableFrom:(IOSClass *)cls {
  return class_ == [NSObject class] ? ![cls isPrimitive] : [cls.objcClass isSubclassOfClass:class_];
}

- (jboolean)isEnum {
  const J2ObjcClassInfo *metadata = [self getMetadata];
  if (metadata) {
    return (metadata->modifiers & JavaLangReflectModifier_ENUM) > 0 &&
        [self getSuperclass] == JavaLangEnum_class_();
  } else {
    return class_ != nil && strcmp(class_getName(class_getSuperclass(class_)), "JavaLangEnum") == 0;
  }
}

- (jboolean)isAnonymousClass {
  const J2ObjcClassInfo *metadata = [self getMetadata];
  if (metadata) {
    return (metadata->modifiers & 0x8000) > 0;
  }
  return false;
}

static IOSObjectArray *GetConstructorsImpl(IOSConcreteClass *iosClass, bool publicOnly) {
  const J2ObjcClassInfo *metadata = IOSClass_GetMetadataOrFail(iosClass);
  if (metadata->methodCount == 0) {
    return [IOSObjectArray arrayWithLength:0 type:JavaLangReflectConstructor_class_()];
  }
  NSMutableArray *constructors = [[NSMutableArray alloc] init];
  unsigned int count;
  Method *nativeMethods = class_copyMethodList(iosClass->class_, &count);
  for (int i = 0; i < metadata->methodCount; i++) {
    const J2ObjcMethodInfo *methodInfo = &metadata->methods[i];
    if (methodInfo->returnType) {  // Not a constructor.
      continue;
    }
    if (publicOnly && (methodInfo->modifiers & JavaLangReflectModifier_PUBLIC) == 0) {
      continue;
    }
    SEL sel = JreMethodSelector(methodInfo);
    struct objc_method_description *methodDesc =
        JreFindMethodDescFromMethodList(sel, nativeMethods, count);
    if (!methodDesc) {
      continue;
    }
    NSMethodSignature *signature = [NSMethodSignature signatureWithObjCTypes:methodDesc->types];
    JavaLangReflectConstructor *constructor =
        [JavaLangReflectConstructor constructorWithMethodSignature:signature
                                                          selector:sel
                                                             class:iosClass
                                                          metadata:methodInfo];
    [constructors addObject:constructor];
  }
  IOSObjectArray *result = [IOSObjectArray arrayWithNSArray:constructors
                                                       type:JavaLangReflectConstructor_class_()];
  [constructors release];
  return result;
}

- (IOSObjectArray *)getDeclaredConstructors {
  return GetConstructorsImpl(self, false);
}

- (IOSObjectArray *)getConstructors {
  return GetConstructorsImpl(self, true);
}

- (JavaLangReflectMethod *)findMethodWithTranslatedName:(NSString *)objcName
                                        checkSupertypes:(jboolean)checkSupertypes {
  const char *name = [objcName UTF8String];

  // NSException's isEquals: method returns different value than java.lang.Throwable's,
  // and should use the default Object.equals() implementation. This can't be fixed by
  // overriding in Throwable, as serialization checks for the default equals() by
  // checking that the declaring class is Object. Since isEqual: and hash are tied
  // together, both methods are skipped so that they are reported as being declared by
  // NSObject.
  if (strcmp(class_getName(class_), "NSException") == 0
    && (strcmp(name, "isEqual:") == 0 || strcmp(name, "hash") == 0)) {
    return nil;
  }

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
  const J2ObjcClassInfo *metadata = [self getMetadata];
  return [JavaLangReflectMethod methodWithMethodSignature:signature
                                                 selector:method_getName(method)
                                                    class:self
                                                 isStatic:isStatic
                                                 metadata:JreFindMethodInfo(metadata, objcName)];
}

static JavaLangReflectConstructor *GetConstructorImpl(
    IOSConcreteClass *iosClass, NSString *name) {
  Method method = JreFindInstanceMethod(iosClass->class_, [name UTF8String]);
  if (method) {
    NSMethodSignature *signature = JreSignatureOrNull(method_getDescription(method));
    if (signature) {
      const J2ObjcClassInfo *metadata = [iosClass getMetadata];
      return [JavaLangReflectConstructor
          constructorWithMethodSignature:signature
                                selector:method_getName(method)
                                   class:iosClass
                                metadata:JreFindMethodInfo(metadata, name)];
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
