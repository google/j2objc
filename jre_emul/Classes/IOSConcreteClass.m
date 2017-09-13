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
#import "java/lang/IllegalAccessException.h"
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

- (instancetype)initWithClass:(Class)cls
                     metadata:(const J2ObjcClassInfo *)metadata {
  if ((self = [super initWithMetadata:metadata])) {
    class_ = cls;
  }
  return self;
}

- (instancetype)initWithClass:(Class)cls {
  return [self initWithClass:cls metadata:JreFindMetadata(cls)];
}

- (id)newInstance {
  // Per the JLS spec, throw an InstantiationException if the type is an
  // interface (no class_), array or primitive type (IOSClass types), or void.
  if ([class_ isKindOfClass:[IOSClass class]] || [class_ isMemberOfClass:[JavaLangVoid class]]) {
    @throw AUTORELEASE([[JavaLangInstantiationException alloc] init]);
  }
  // Check if reflection is available.
  if ([self getMetadata]) {
    // Get the nullary constructor.
    JavaLangReflectConstructor *constructor = JreConstructorWithParamTypes(self, nil);
    if (!constructor) {
      @throw AUTORELEASE([[JavaLangInstantiationException alloc] init]);
    } else if ([constructor getModifiers] & JavaLangReflectModifier_PRIVATE) {
      @throw AUTORELEASE([[JavaLangIllegalAccessException alloc]
          initWithNSString:@"Cannot access private constructor."]);
    }
  }
  return AUTORELEASE([[class_ alloc] init]);
}

- (IOSClass *)getSuperclass {
  // Number and Throwable are special cases where its superclass doesn't match Java's.
  const char *clsName = class_getName(class_);
  if (strcmp("NSNumber", clsName) == 0 || strcmp("JavaLangThrowable", clsName) == 0) {
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
  NSString *name = JreClassQualifiedName([self getMetadata]);
  return name ? name : NSStringFromClass(class_);
}

- (NSString *)getSimpleName {
  const J2ObjcClassInfo *metadata = [self getMetadata];
  return metadata ? JreClassTypeName(metadata) : NSStringFromClass(class_);
}

- (NSString *)objcName {
  return NSStringFromClass(class_);
}

- (void)appendMetadataName:(NSMutableString *)str {
  [str appendString:@"L"];
  [str appendString:NSStringFromClass(class_)];
  [str appendString:@";"];
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
  for (int i = 0; i < metadata->methodCount; i++) {
    const J2ObjcMethodInfo *methodInfo = &metadata->methods[i];
    if (methodInfo->returnType) {  // Not a constructor.
      continue;
    }
    if (publicOnly && (methodInfo->modifiers & JavaLangReflectModifier_PUBLIC) == 0) {
      continue;
    }
    JavaLangReflectConstructor *constructor =
        [JavaLangReflectConstructor constructorWithDeclaringClass:iosClass
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

- (JavaLangReflectConstructor *)getConstructor:(IOSObjectArray *)parameterTypes {
  JavaLangReflectConstructor *c = JreConstructorWithParamTypes(self, parameterTypes);
  if (c && ([c getModifiers] & JavaLangReflectModifier_PUBLIC) > 0) {
    return c;
  }
  @throw create_JavaLangNoSuchMethodException_init();
}

- (JavaLangReflectConstructor *)getDeclaredConstructor:(IOSObjectArray *)parameterTypes {
  JavaLangReflectConstructor *c = JreConstructorWithParamTypes(self, parameterTypes);
  if (c) {
    return c;
  }
  @throw create_JavaLangNoSuchMethodException_init();
}

- (IOSObjectArray *)getInterfacesInternal {
  IOSObjectArray *result = __c11_atomic_load(&interfaces_, __ATOMIC_ACQUIRE);
  if (!result) {
    @synchronized(self) {
      result = __c11_atomic_load(&interfaces_, __ATOMIC_RELAXED);
      if (!result) {
        unsigned int count;
        Protocol **protocolList = class_copyProtocolList(class_, &count);
        bool excludeNSCopying = false;
        const char *clsName = class_getName(class_);
        // IOSClass and JavaLangEnum are made to conform to NSCopying so that they can be used as
        // keys in a NSDictionary but in Java they don't implement Cloneable.
        if (strcmp("IOSClass", clsName) == 0 || strcmp("JavaLangEnum", clsName) == 0) {
          excludeNSCopying = true;
        }
        result = IOSClass_NewInterfacesFromProtocolList(protocolList, count, excludeNSCopying);
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
