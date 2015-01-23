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
//  IOSMappedClass.m
//  JreEmulation
//
//  Created by Tom Ball on 12/10/13.
//

#import "IOSMappedClass.h"
#import "IOSObjectArray.h"
#import "JavaMetadata.h"
#import "java/lang/Package.h"
#import "java/lang/reflect/Method.h"
#import "java/lang/reflect/Modifier.h"

// Class representation for a mapped class, such as NSObject or NSString.
// All reflection information is determined by metadata, to avoid returning
// Objective-C specific methods or classes.
@implementation IOSMappedClass

- (instancetype)initWithClass:(Class)cls package:(NSString *)package name:(NSString *)name {
  if ((self = [super initWithClass:cls])) {
    package_ = RETAIN_(package);
    name_ = RETAIN_(name);
  }
  return self;
}

- (NSString *)getName {
  return [NSString stringWithFormat:@"%@.%@", package_, name_];
}

- (NSString *)getSimpleName {
  return name_;
}

- (NSString *)objcName {
  return NSStringFromClass(class_);
}

- (id)getPackage {
  return AUTORELEASE([[JavaLangPackage alloc] initWithNSString:package_
                                                  withNSString:nil
                                                  withNSString:nil
                                                  withNSString:nil
                                                  withNSString:nil
                                                  withNSString:nil
                                                  withNSString:nil
                                                withJavaNetURL:nil]);
}

static void CollectMethodsOrConstructors(IOSMappedClass *self,
                                         NSMutableDictionary *methodMap,
                                         BOOL publicOnly,
                                         BOOL constructors) {
  JavaClassMetadata *metadata = [self getMetadata];
  IOSObjectArray *methodInfos = [metadata allMethods];
  for (unsigned i = 0; i < metadata.methodCount; i++) {
    JavaMethodMetadata *info = [methodInfos objectAtIndex:i];
    if ([info isConstructor] == constructors) {
      int mods = [info modifiers];
      if (publicOnly && !(mods & JavaLangReflectModifier_PUBLIC)) {
        continue;
      }
      SEL sel = [info selector];
      BOOL isStatic = (mods & JavaLangReflectModifier_STATIC) != 0;
      NSMethodSignature *signature = nil;
      if (isStatic) {
        signature = [self->class_ methodSignatureForSelector:sel];
      } else {
        signature = [self->class_ instanceMethodSignatureForSelector:sel];
      }
      if (signature) {
        JavaLangReflectMethod *method = [JavaLangReflectMethod methodWithMethodSignature:signature
                                                                                selector:sel
                                                                                   class:self
                                                                                isStatic:isStatic
                                                                                metadata:info];
        [methodMap setObject:method forKey:[info name]];
      }
    }
  }
}

- (void)collectMethods:(NSMutableDictionary *)methodMap
            publicOnly:(BOOL)publicOnly {
  CollectMethodsOrConstructors(self, methodMap, publicOnly, NO);
}

- (IOSObjectArray *)getDeclaredConstructors {
  NSMutableDictionary *methodMap = [NSMutableDictionary dictionary];
  CollectMethodsOrConstructors(self, methodMap, NO, YES);
  return [IOSObjectArray arrayWithNSArray:[methodMap allValues]
                                     type:JavaLangReflectMethod_class_()];
}

- (IOSObjectArray *)getConstructors {
  NSMutableDictionary *methodMap = [NSMutableDictionary dictionary];
  CollectMethodsOrConstructors(self, methodMap, YES, YES);
  return [IOSObjectArray arrayWithNSArray:[methodMap allValues]
                                     type:JavaLangReflectMethod_class_()];
}

- (BOOL)isEnum {
  return NO;
}

- (BOOL)isAnonymousClass {
  return NO;
}

@end
