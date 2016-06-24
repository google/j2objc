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
//  IOSReflection.h
//  JreEmulation
//
//  Created by Tom Ball on 9/23/13.
//

#ifndef JreEmulation_IOSReflection_h
#define JreEmulation_IOSReflection_h

#include "IOSMetadata.h"
#include "J2ObjC_common.h"
#include "java/lang/reflect/Modifier.h"

#import "objc/runtime.h"

@class IOSClass;
@class JavaLangReflectConstructor;
@class JavaLangReflectMethod;

// An empty class info struct to be used by certain kinds of class objects like
// arrays and proxies.
FOUNDATION_EXPORT const J2ObjcClassInfo JreEmptyClassInfo;

CF_EXTERN_C_BEGIN

const J2ObjcClassInfo *JreFindMetadata(Class cls);
IOSClass *JreClassForString(const char *str);
IOSObjectArray *JreParseClassList(const char *listStr);
Method JreFindInstanceMethod(Class cls, const char *name);
Method JreFindClassMethod(Class cls, const char *name);
struct objc_method_description *JreFindMethodDescFromList(
    SEL sel, struct objc_method_description *methods, unsigned int count);
struct objc_method_description *JreFindMethodDescFromMethodList(
    SEL sel, Method *methods, unsigned int count);

__attribute__((always_inline)) inline const void *JrePtrAtIndex(const void **ptrTable, ptr_idx i) {
  return i < 0 ? NULL : ptrTable[i];
}

// J2ObjcClassInfo accessor functions.
NSString *JreClassTypeName(const J2ObjcClassInfo *metadata);
NSString *JreClassQualifiedName(const J2ObjcClassInfo *metadata);
NSString *JreClassPackageName(const J2ObjcClassInfo *metadata);

// Field and method lookup functions.
const J2ObjcFieldInfo *JreFindFieldInfo(const J2ObjcClassInfo *metadata, const char *fieldName);
// Find a method or constructor declared in the given class.
JavaLangReflectMethod *JreMethodWithNameAndParamTypes(
    IOSClass *iosClass, NSString *name, IOSObjectArray *paramTypes);
JavaLangReflectConstructor *JreConstructorWithParamTypes(
    IOSClass *iosClass, IOSObjectArray *paramTypes);
JavaLangReflectMethod *JreMethodForSelector(IOSClass *iosClass, const char *selector);
JavaLangReflectConstructor *JreConstructorForSelector(IOSClass *iosClass, const char *selector);
// Find a method in the given class or its hierarchy.
JavaLangReflectMethod *JreMethodWithNameAndParamTypesInherited(
    IOSClass *iosClass, NSString *name, IOSObjectArray *types);
JavaLangReflectMethod *JreMethodForSelectorInherited(IOSClass *iosClass, const char *selector);

// J2ObjcMethodInfo accessor functions.
NSString *JreMethodGenericString(const J2ObjcMethodInfo *metadata, const void **ptrTable);

__attribute__((always_inline)) inline const char *JreMethodJavaName(
    const J2ObjcMethodInfo *metadata, const void **ptrTable) {
  const char *javaName = JrePtrAtIndex(ptrTable, metadata->javaNameIdx);
  return javaName ? javaName : metadata->selector;
}

// metadata must not be NULL.
__attribute__((always_inline)) inline SEL JreMethodSelector(const J2ObjcMethodInfo *metadata) {
  return sel_registerName(metadata->selector);
}

CF_EXTERN_C_END

#endif // JreEmulation_IOSReflection_h
