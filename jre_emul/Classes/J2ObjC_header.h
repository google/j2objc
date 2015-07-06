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

// Common defines and includes needed by all J2ObjC header files.

#ifndef _J2OBJC_HEADER_H_
#define _J2OBJC_HEADER_H_

#import "IOSObjectArray.h"
#import "J2ObjC_common.h"
#import "JavaObject.h"
#import "NSObject+JavaObject.h"

#define BOXED_INC_AND_DEC_INNER(CNAME, VALUE_METHOD, TYPE, OPNAME, OP) \
  __attribute__((always_inline)) inline TYPE *BoxedPre##OPNAME##CNAME(TYPE **value) { \
    nil_chk(*value); \
    return *value = TYPE##_valueOfWith##CNAME##_([*value VALUE_METHOD] OP 1); \
  } \
  __attribute__((always_inline)) inline TYPE *BoxedPre##OPNAME##Strong##CNAME(TYPE **value) { \
    nil_chk(*value); \
    return JreStrongAssign(value, TYPE##_valueOfWith##CNAME##_([*value VALUE_METHOD] OP 1)); \
  } \
  __attribute__((always_inline)) inline TYPE *BoxedPre##OPNAME##Array##CNAME(JreArrayRef ref) { \
    return IOSObjectArray_SetRef( \
        ref, TYPE##_valueOfWith##CNAME##_([*((TYPE **)ref.pValue) VALUE_METHOD] OP 1)); \
  } \
  __attribute__((always_inline)) inline TYPE *BoxedPost##OPNAME##CNAME(TYPE **value) { \
    nil_chk(*value); \
    TYPE *original = *value; \
    *value = TYPE##_valueOfWith##CNAME##_([*value VALUE_METHOD] OP 1); \
    return original; \
  } \
  __attribute__((always_inline)) inline TYPE *BoxedPost##OPNAME##Strong##CNAME(TYPE **value) { \
    nil_chk(*value); \
    TYPE *original = *value; \
    JreStrongAssign(value, TYPE##_valueOfWith##CNAME##_([*value VALUE_METHOD] OP 1)); \
    return original; \
  } \
  __attribute__((always_inline)) inline TYPE *BoxedPost##OPNAME##Array##CNAME(JreArrayRef ref) { \
    TYPE *original = *ref.pValue; \
    IOSObjectArray_SetRef( \
        ref, TYPE##_valueOfWith##CNAME##_([*((TYPE **)ref.pValue) VALUE_METHOD] OP 1)); \
    return original; \
  }

/*!
 * Defines increment and decrement operators on boxed types. The translator will
 * use this macro to add these operators to the headers of the boxed types.
 *
 * @define BOXED_INC_AND_DEC
 * @param CNAME The capitalized name of the primitive type (eg. "Int").
 * @param VALUE_METHOD The method on the boxed type that returns the value.
 * @param TYPE The boxed type name (eg. "JavaLangInteger").
 */
#define BOXED_INC_AND_DEC(CNAME, VALUE_METHOD, TYPE) \
    BOXED_INC_AND_DEC_INNER(CNAME, VALUE_METHOD, TYPE, Incr, +) \
    BOXED_INC_AND_DEC_INNER(CNAME, VALUE_METHOD, TYPE, Decr, -)

#endif  // _J2OBJC_HEADER_H_
