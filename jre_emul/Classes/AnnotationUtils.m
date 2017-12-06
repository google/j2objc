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
//  AnnotationUtils.m
//  JreEmulation
//

#include "J2ObjC_source.h"
#include "java/lang/Boolean.h"
#include "java/lang/Byte.h"
#include "java/lang/Character.h"
#include "java/lang/Double.h"
#include "java/lang/Float.h"
#include "java/lang/IllegalAccessException.h"
#include "java/lang/IllegalStateException.h"
#include "java/lang/Integer.h"
#include "java/lang/Long.h"
#include "java/lang/NullPointerException.h"
#include "java/lang/Short.h"
#include "java/lang/StringBuilder.h"
#include "java/lang/annotation/Annotation.h"
#include "java/lang/reflect/InvocationTargetException.h"
#include "java/lang/reflect/Method.h"
#include "java/util/Arrays.h"

/*!
 @brief <p>Helper methods for working with <code>Annotation</code> instances.

 These functions are based on the Apache Commons Lang class
 <a href=
 "https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/AnnotationUtils.java"
  >org.apache.commons.lang3.AnnotationUtils</a>.
 */

static jint hashMember(NSString *name, id value);
static jboolean isValidAnnotationMemberType(IOSClass *type);
static jboolean memberEquals(IOSClass *type, id o1, id o2);


jboolean JreAnnotationEquals(id a1, id a2) {
  if (a1 == a2) {
    return true;
  }
  if (a1 == nil || a2 == nil) {
    return false;
  }
  IOSClass *type = [a1 annotationType];
  IOSClass *type2 = [a2 annotationType];
  if (![type isEqual:type2]) {
    return false;
  }
  @try {
    IOSObjectArray *methods = [type getDeclaredMethods];
    for (NSUInteger i = 0; i < methods->size_; i++) {
      JavaLangReflectMethod *m = [methods objectAtIndex:i];
      IOSObjectArray *paramTypes = [m getParameterTypes];
      if (paramTypes->size_ == 0 && isValidAnnotationMemberType([m getReturnType])) {
        IOSObjectArray *emptyArgTypes = [IOSObjectArray arrayWithLength:0 type:NSObject_class_()];
        id v1 = [m invokeWithId:a1 withNSObjectArray:emptyArgTypes];
        id v2 = [m invokeWithId:a2 withNSObjectArray:emptyArgTypes];
        if (!memberEquals([m getReturnType], v1, v2)) {
          return false;
        }
      }
    }
  }
  @catch (JavaLangIllegalAccessException *ex) {
    return false;
  }
  @catch (JavaLangReflectInvocationTargetException *ex) {
    return false;
  }
  return true;
}


jint JreAnnotationHashCode(id a) {
  jint result = 0;
  IOSClass *type = [((id<JavaLangAnnotationAnnotation>) nil_chk(a)) annotationType];
  IOSObjectArray *methods = [type getDeclaredMethods];
  IOSObjectArray *emptyArgs = [IOSObjectArray arrayWithLength:0 type:NSObject_class_()];
  for (NSUInteger i = 0; i < methods->size_; i++) {
    JavaLangReflectMethod *m = [methods objectAtIndex:i];
    @try {
      id value = [m invokeWithId:a withNSObjectArray:emptyArgs];
      if (value == nil) {
        @throw create_JavaLangIllegalStateException_initWithNSString_(
            NSString_java_formatWithNSString_withNSObjectArray_(
                @"Annotation method %s returned null",
                [IOSObjectArray arrayWithObjects:(id[]){ m } count:1 type:NSObject_class_()]));
      }
      result += hashMember([m getName], value);
    }
    @catch (JavaLangException *ex) {
      @throw create_JavaLangRuntimeException_initWithJavaLangThrowable_(ex);
    }
  }
  return result;
}


static jboolean isValidAnnotationMemberType(IOSClass *type) {
  if ([type isArray]) {
    type = [type getComponentType];
  }
  if (type == nil) {
    return false;
  }
  return [type isPrimitive] || [type isEnum] || [type isAnnotation]
      || [NSString_class_() isEqual:type] || [IOSClass_class_() isEqual:type];
}


static jboolean annotationArrayMemberEquals(IOSObjectArray *a1, IOSObjectArray *a2) {
  if (a1->size_ != a2->size_) {
    return false;
  }
  for (jint i = 0; i < a1->size_; i++) {
    if (!JreAnnotationEquals(IOSObjectArray_Get(a1, i), IOSObjectArray_Get(a2, i))) {
      return false;
    }
  }
  return true;
}


static jboolean arrayMemberEquals(IOSClass *componentType, id o1, id o2) {
  if ([componentType isAnnotation]) {
    return annotationArrayMemberEquals(o1, o2);
  }
  if ([componentType isEqual:JavaLangByte_get_TYPE()]) {
    return JavaUtilArrays_equalsWithByteArray_withByteArray_(o1, o2);
  }
  if ([componentType isEqual:JavaLangShort_get_TYPE()]) {
    return JavaUtilArrays_equalsWithShortArray_withShortArray_(o1, o2);
  }
  if ([componentType isEqual:JavaLangInteger_get_TYPE()]) {
    return JavaUtilArrays_equalsWithIntArray_withIntArray_(o1, o2);
  }
  if ([componentType isEqual:JavaLangCharacter_get_TYPE()]) {
    return JavaUtilArrays_equalsWithCharArray_withCharArray_(o1, o2);
  }
  if ([componentType isEqual:JavaLangLong_get_TYPE()]) {
    return JavaUtilArrays_equalsWithLongArray_withLongArray_(o1, o2);
  }
  if ([componentType isEqual:JavaLangFloat_get_TYPE()]) {
    return JavaUtilArrays_equalsWithFloatArray_withFloatArray_(o1, o2);
  }
  if ([componentType isEqual:JavaLangDouble_get_TYPE()]) {
    return JavaUtilArrays_equalsWithDoubleArray_withDoubleArray_(o1, o2);
  }
  if ([componentType isEqual:JavaLangBoolean_get_TYPE()]) {
    return JavaUtilArrays_equalsWithBooleanArray_withBooleanArray_(o1, o2);
  }
  return JavaUtilArrays_equalsWithNSObjectArray_withNSObjectArray_(o1, o2);
}

static jboolean memberEquals(IOSClass *type, id o1, id o2) {
  if (o1 == o2) {
    return true;
  }
  if (o1 == nil || o2 == nil) {
    return false;
  }
  if ([type isArray]) {
    return arrayMemberEquals([type getComponentType], o1, o2);
  }
  if ([type isAnnotation]) {
    return JreAnnotationEquals(o1, o2);
  }
  return [o1 isEqual:o2];
}


jint arrayMemberHash(IOSClass *componentType, id o) {
  if ([componentType isEqual:JavaLangByte_get_TYPE()]) {
    return JavaUtilArrays_hashCodeWithByteArray_(o);
  }
  if ([componentType isEqual:JavaLangShort_get_TYPE()]) {
    return JavaUtilArrays_hashCodeWithShortArray_(o);
  }
  if ([componentType isEqual:JavaLangInteger_get_TYPE()]) {
    return JavaUtilArrays_hashCodeWithIntArray_(o);
  }
  if ([componentType isEqual:JavaLangCharacter_get_TYPE()]) {
    return JavaUtilArrays_hashCodeWithCharArray_(o);
  }
  if ([componentType isEqual:JavaLangLong_get_TYPE()]) {
    return JavaUtilArrays_hashCodeWithLongArray_(o);
  }
  if ([componentType isEqual:JavaLangFloat_get_TYPE()]) {
    return JavaUtilArrays_hashCodeWithFloatArray_(o);
  }
  if ([componentType isEqual:JavaLangDouble_get_TYPE()]) {
    return JavaUtilArrays_hashCodeWithDoubleArray_(o);
  }
  if ([componentType isEqual:JavaLangBoolean_get_TYPE()]) {
    return JavaUtilArrays_hashCodeWithBooleanArray_(o);
  }
  return JavaUtilArrays_hashCodeWithNSObjectArray_(o);
}


static jint hashMember(NSString *name, id value) {
  jint part1 = ((jint) [name hash]) * 127;
  if ([[value java_getClass] isArray]) {
    return part1 ^ arrayMemberHash([[value java_getClass] getComponentType], value);
  }
  if ([JavaLangAnnotationAnnotation_class_() isInstance:value]) {
    return part1 ^ JreAnnotationHashCode(value);
  }
  return part1 ^ ((jint) [value hash]);
}
