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

// Common defines and includes needed by all J2ObjC source files.

#ifndef _J2OBJC_SOURCE_H_
#define _J2OBJC_SOURCE_H_

#import "J2ObjC_common.h"
#import "JavaObject.h"
#import "IOSClass.h"  // Type literal accessors.
#import "IOSObjectArray.h"
#import "IOSPrimitiveArray.h"
#import "IOSReflection.h"  // Metadata methods.
#import "NSCopying+JavaCloneable.h"
#import "NSNumber+JavaNumber.h"
#import "NSObject+JavaObject.h"
#import "NSString+JavaString.h"
#import "jni.h"
#import <libkern/OSAtomic.h>  // OSMemoryBarrier used in initialize methods.

// Only expose this function to ARC generated code.
#if __has_feature(objc_arc)
FOUNDATION_EXPORT void JreRelease(id obj);
#endif

// Defined in JreEmulation.m
FOUNDATION_EXPORT id GetNonCapturingLambda(Protocol *protocol,
    NSString *blockClassName, SEL methodSelector, id block);
FOUNDATION_EXPORT id GetCapturingLambda(Protocol *protocol,
    NSString *blockClassName, SEL methodSelector, id wrapperBlock, id block);

/*!
 * Returns correct result when casting a double to an integral type. In C, a
 * float >= Integer.MAX_VALUE (allowing for rounding) returns 0x80000000,
 * while Java requires 0x7FFFFFFF.  A double >= Long.MAX_VALUE returns
 * 0x8000000000000000L, while Java requires 0x7FFFFFFFFFFFFFFFL.
 */
__attribute__((always_inline)) inline jint JreFpToInt(jdouble d) {
  jint tmp = (jint)d;
  return tmp == (jint)0x80000000 ? (d >= 0 ? 0x7FFFFFFF : tmp) : tmp;
}
__attribute__((always_inline)) inline jlong JreFpToLong(jdouble d) {
  jlong tmp = (jlong)d;
  return tmp == (jlong)0x8000000000000000LL ? (d >= 0 ? 0x7FFFFFFFFFFFFFFFL : tmp) : tmp;
}
__attribute__((always_inline)) inline jchar JreFpToChar(jdouble d) {
  unsigned tmp = (unsigned)d;
  return tmp > 0xFFFF || (tmp == 0 && d > 0) ? 0xFFFF : (jchar)tmp;
}

#define ARITHMETIC_OPERATOR_DEFN(NAME, TYPE, OPNAME, OP, PNAME, PTYPE, CAST) \
  __attribute__((always_inline)) inline TYPE Jre##OPNAME##AssignVolatile##NAME##PNAME( \
      volatile_##TYPE *pLhs, PTYPE rhs) { \
    TYPE result = CAST(__c11_atomic_load(pLhs, __ATOMIC_SEQ_CST) OP rhs); \
    __c11_atomic_store(pLhs, result, __ATOMIC_SEQ_CST); \
    return result; \
  }
#define ARITHMETIC_OPERATORS_DEFN(NAME, TYPE, PNAME, PTYPE, CAST) \
  ARITHMETIC_OPERATOR_DEFN(NAME, TYPE, Plus, +, PNAME, PTYPE, CAST) \
  ARITHMETIC_OPERATOR_DEFN(NAME, TYPE, Minus, -, PNAME, PTYPE, CAST) \
  ARITHMETIC_OPERATOR_DEFN(NAME, TYPE, Times, *, PNAME, PTYPE, CAST) \
  ARITHMETIC_OPERATOR_DEFN(NAME, TYPE, Divide, /, PNAME, PTYPE, CAST)

ARITHMETIC_OPERATORS_DEFN(Char, jchar, I, jint, (jchar))
ARITHMETIC_OPERATORS_DEFN(Char, jchar, J, jlong, (jchar))
ARITHMETIC_OPERATORS_DEFN(Char, jchar, F, jfloat, JreFpToChar)
ARITHMETIC_OPERATORS_DEFN(Char, jchar, D, jdouble, JreFpToChar)
ARITHMETIC_OPERATORS_DEFN(Byte, jbyte, I, jint, (jbyte))
ARITHMETIC_OPERATORS_DEFN(Byte, jbyte, J, jlong, (jbyte))
ARITHMETIC_OPERATORS_DEFN(Byte, jbyte, F, jfloat, JreFpToInt)
ARITHMETIC_OPERATORS_DEFN(Byte, jbyte, D, jdouble, JreFpToInt)
ARITHMETIC_OPERATORS_DEFN(Short, jshort, I, jint, (jshort))
ARITHMETIC_OPERATORS_DEFN(Short, jshort, J, jlong, (jshort))
ARITHMETIC_OPERATORS_DEFN(Short, jshort, F, jfloat, JreFpToInt)
ARITHMETIC_OPERATORS_DEFN(Short, jshort, D, jdouble, JreFpToInt)
ARITHMETIC_OPERATORS_DEFN(Int, jint, I, jint, (jint))
ARITHMETIC_OPERATORS_DEFN(Int, jint, J, jlong, (jint))
ARITHMETIC_OPERATORS_DEFN(Int, jint, F, jfloat, JreFpToInt)
ARITHMETIC_OPERATORS_DEFN(Int, jint, D, jdouble, JreFpToInt)
ARITHMETIC_OPERATORS_DEFN(Long, jlong, J, jlong, (jlong))
ARITHMETIC_OPERATORS_DEFN(Long, jlong, F, jfloat, JreFpToLong)
ARITHMETIC_OPERATORS_DEFN(Long, jlong, D, jdouble, JreFpToLong)
ARITHMETIC_OPERATORS_DEFN(Float, jfloat, F, jfloat, (jfloat))
ARITHMETIC_OPERATORS_DEFN(Float, jfloat, D, jdouble, (jfloat))
ARITHMETIC_OPERATORS_DEFN(Double, jdouble, D, jdouble, (jdouble))
#undef ARITHMETIC_OPERATORS_DEFN

#define MOD_ASSIGN_FP_DEFN(NAME, TYPE, FUNC, PNAME, PTYPE, CAST) \
  __attribute__((always_inline)) inline TYPE JreModAssign##NAME##PNAME(TYPE *pLhs, PTYPE rhs) { \
    return *pLhs = CAST(FUNC(*pLhs, rhs)); \
  } \
  __attribute__((always_inline)) inline TYPE JreModAssignVolatile##NAME##PNAME( \
      volatile_##TYPE *pLhs, PTYPE rhs) { \
    TYPE result = CAST(FUNC(__c11_atomic_load(pLhs, __ATOMIC_SEQ_CST), rhs)); \
    __c11_atomic_store(pLhs, result, __ATOMIC_SEQ_CST); \
    return result; \
  }

ARITHMETIC_OPERATOR_DEFN(Char, jchar, Mod, %, I, jint, (jchar))
ARITHMETIC_OPERATOR_DEFN(Char, jchar, Mod, %, J, jlong, (jchar))
MOD_ASSIGN_FP_DEFN(Char, jchar, fmodf, F, jfloat, JreFpToChar)
MOD_ASSIGN_FP_DEFN(Char, jchar, fmod, D, jdouble, JreFpToChar)
ARITHMETIC_OPERATOR_DEFN(Byte, jbyte, Mod, %, I, jint, (jbyte))
ARITHMETIC_OPERATOR_DEFN(Byte, jbyte, Mod, %, J, jlong, (jbyte))
MOD_ASSIGN_FP_DEFN(Byte, jbyte, fmodf, F, jfloat, JreFpToInt)
MOD_ASSIGN_FP_DEFN(Byte, jbyte, fmod, D, jdouble, JreFpToInt)
ARITHMETIC_OPERATOR_DEFN(Short, jshort, Mod, %, I, jint, (jshort))
ARITHMETIC_OPERATOR_DEFN(Short, jshort, Mod, %, J, jlong, (jshort))
MOD_ASSIGN_FP_DEFN(Short, jshort, fmodf, F, jfloat, JreFpToInt)
MOD_ASSIGN_FP_DEFN(Short, jshort, fmod, D, jdouble, JreFpToInt)
ARITHMETIC_OPERATOR_DEFN(Int, jint, Mod, %, I, jint, (jint))
ARITHMETIC_OPERATOR_DEFN(Int, jint, Mod, %, J, jlong, (jint))
MOD_ASSIGN_FP_DEFN(Int, jint, fmodf, F, jfloat, JreFpToInt)
MOD_ASSIGN_FP_DEFN(Int, jint, fmod, D, jdouble, JreFpToInt)
ARITHMETIC_OPERATOR_DEFN(Long, jlong, Mod, %, J, jlong, (jlong))
MOD_ASSIGN_FP_DEFN(Long, jlong, fmodf, F, jfloat, JreFpToLong)
MOD_ASSIGN_FP_DEFN(Long, jlong, fmod, D, jdouble, JreFpToLong)
MOD_ASSIGN_FP_DEFN(Float, jfloat, fmodf, F, jfloat, (jfloat))
MOD_ASSIGN_FP_DEFN(Float, jfloat, fmod, D, jdouble, (jfloat))
MOD_ASSIGN_FP_DEFN(Double, jdouble, fmod, D, jdouble, (jdouble))
#undef MOD_ASSIGN_FP_DEFN
#undef ARITHMETIC_OPERATOR_DEFN

#define SHIFT_OPERATORS_DEFN(NAME, TYPE, UTYPE, MASK) \
  __attribute__((always_inline)) inline TYPE JreLShift##NAME(TYPE lhs, jlong rhs) { \
    return lhs << (rhs & MASK); \
  } \
  __attribute__((always_inline)) inline TYPE JreRShift##NAME(TYPE lhs, jlong rhs) { \
    return lhs >> (rhs & MASK); \
  } \
  __attribute__((always_inline)) inline TYPE JreURShift##NAME(TYPE lhs, jlong rhs) { \
    return (TYPE) (((UTYPE) lhs) >> (rhs & MASK)); \
  }

#define SHIFT_ASSIGN_OPERATORS_DEFN(NAME, TYPE, UTYPE, MASK) \
  __attribute__((always_inline)) inline TYPE JreLShiftAssign##NAME(TYPE *pLhs, jlong rhs) { \
    return *pLhs = (TYPE) (*pLhs << (rhs & MASK)); \
  } \
  __attribute__((always_inline)) inline TYPE JreRShiftAssign##NAME(TYPE *pLhs, jlong rhs) { \
    return *pLhs = (TYPE) (*pLhs >> (rhs & MASK)); \
  } \
  __attribute__((always_inline)) inline TYPE JreURShiftAssign##NAME(TYPE *pLhs, jlong rhs) { \
    return *pLhs = (TYPE) (((UTYPE) *pLhs) >> (rhs & MASK)); \
  } \
  __attribute__((always_inline)) inline TYPE JreLShiftAssignVolatile##NAME( \
      volatile_##TYPE *pLhs, jlong rhs) { \
    TYPE result = __c11_atomic_load(pLhs, __ATOMIC_SEQ_CST) << (rhs & MASK); \
    __c11_atomic_store(pLhs, result, __ATOMIC_SEQ_CST); \
    return result; \
  } \
  __attribute__((always_inline)) inline TYPE JreRShiftAssignVolatile##NAME( \
      volatile_##TYPE *pLhs, jlong rhs) { \
    TYPE result = __c11_atomic_load(pLhs, __ATOMIC_SEQ_CST) >> (rhs & MASK); \
    __c11_atomic_store(pLhs, result, __ATOMIC_SEQ_CST); \
    return result; \
  } \
  __attribute__((always_inline)) inline TYPE JreURShiftAssignVolatile##NAME( \
      volatile_##TYPE *pLhs, jlong rhs) { \
    TYPE result = ((UTYPE)__c11_atomic_load(pLhs, __ATOMIC_SEQ_CST)) >> (rhs & MASK); \
    __c11_atomic_store(pLhs, result, __ATOMIC_SEQ_CST); \
    return result; \
  }

// Shift masks are determined by the JLS spec, section 15.19.
SHIFT_OPERATORS_DEFN(32, jint, uint32_t, 0x1f)
SHIFT_OPERATORS_DEFN(64, jlong, uint64_t, 0x3f)
SHIFT_ASSIGN_OPERATORS_DEFN(Char, jchar, uint32_t, 0x1f)
SHIFT_ASSIGN_OPERATORS_DEFN(Byte, jbyte, uint32_t, 0x1f)
SHIFT_ASSIGN_OPERATORS_DEFN(Short, jshort, uint32_t, 0x1f)
SHIFT_ASSIGN_OPERATORS_DEFN(Int, jint, uint32_t, 0x1f)
SHIFT_ASSIGN_OPERATORS_DEFN(Long, jlong, uint64_t, 0x3f)
#undef SHIFT_OPERATORS_DEFN
#undef SHIFT_ASSIGN_OPERATORS_DEFN

#define BIT_OPERATOR_DEFN(NAME, TYPE, OPNAME, OP) \
  __attribute__((always_inline)) inline TYPE Bit##OPNAME##AssignVolatile##NAME( \
      volatile_##TYPE *pLhs, TYPE rhs) { \
    TYPE result = __c11_atomic_load(pLhs, __ATOMIC_SEQ_CST) OP rhs; \
    __c11_atomic_store(pLhs, result, __ATOMIC_SEQ_CST); \
    return result; \
  }
#define BIT_OPERATORS_DEFN(NAME, TYPE) \
  BIT_OPERATOR_DEFN(NAME, TYPE, And, &) \
  BIT_OPERATOR_DEFN(NAME, TYPE, Or, |) \
  BIT_OPERATOR_DEFN(NAME, TYPE, Xor, ^)

BIT_OPERATORS_DEFN(Boolean, jboolean)
BIT_OPERATORS_DEFN(Char, jchar)
BIT_OPERATORS_DEFN(Byte, jbyte)
BIT_OPERATORS_DEFN(Short, jshort)
BIT_OPERATORS_DEFN(Int, jint)
BIT_OPERATORS_DEFN(Long, jlong)
#undef BIT_OPERATOR_DEFN
#undef BIT_OPERATORS_DEFN

#endif  // _J2OBJC_SOURCE_H_
