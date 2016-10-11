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
//  IOSProxyClass.m
//  JreEmulation
//
//  Created by Keith Stanger on May 4, 2016.
//

#import "IOSProxyClass.h"

#import "IOSReflection.h"
#import "java/lang/NoSuchFieldException.h"
#import "java/lang/reflect/Field.h"
#import "java/lang/reflect/Proxy.h"

@implementation IOSProxyClass

- (instancetype)initWithClass:(Class)cls {
  static const void *ptrTable[] = { "LJavaLangReflectInvocationHandler;" } ;
  static J2ObjcMethodInfo proxyMethods[] = {{NULL, NULL, 0x1, -1, 0, -1, -1, -1, -1 }};
  proxyMethods[0].selector = @selector(initWithJavaLangReflectInvocationHandler:);
  static const J2ObjcClassInfo proxyClassMetadata = {
    NULL, NULL, ptrTable, proxyMethods, NULL, J2OBJC_METADATA_VERSION, 0x0, 1, 0, -1, -1, -1, -1, -1
  };
  return [self initWithClass:cls metadata:&proxyClassMetadata];
}

- (NSString *)getName {
  return NSStringFromClass(class_);
}

- (NSString *)getSimpleName {
  return NSStringFromClass(class_);
}

- (IOSObjectArray *)getDeclaredFields {
  return [IOSObjectArray arrayWithLength:0 type:JavaLangReflectField_class_()];
}

- (IOSObjectArray *)getFields {
  return [IOSObjectArray arrayWithLength:0 type:JavaLangReflectField_class_()];
}

- (JavaLangReflectField *)getDeclaredField:(NSString *)name {
  @throw create_JavaLangNoSuchFieldException_initWithNSString_(name);
}

- (JavaLangReflectField *)getField:(NSString *)name {
  @throw create_JavaLangNoSuchFieldException_initWithNSString_(name);
}

@end
