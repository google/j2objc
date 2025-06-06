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

#import "IOSClass.h"  // Type literal accessors.
#import "IOSMetadata.h"
#import "IOSObjectArray.h"
#import "IOSPrimitiveArray.h"
#import "J2ObjC_common.h"
#import "JavaObject.h"
#import "NSCopying+JavaCloneable.h"
#import "NSNumber+JavaNumber.h"
#import "NSObject+JavaObject.h"
#import "NSString+JavaString.h"
#import "jni.h"
#import "objc/runtime.h"

// "I" is defined in complex.h, which results in errors if that file is also
// included.
#pragma push_macro("I")
#undef I

__attribute__ ((unused)) static inline id cast_chk(id __unsafe_unretained p, Class clazz) {
  if (__builtin_expect(p && ![p isKindOfClass:clazz], 0)) {
    JreThrowClassCastException(p, clazz);
  }
  return p;
}

// Similar to above, but with an IOSClass parameter instead of a Class
// parameter. This check is necessary for interface and array types and is
// faster than a conformsToProtocol check for interfaces.
__attribute__((always_inline)) inline id cast_check(id __unsafe_unretained p, IOSClass *cls) {
  if (__builtin_expect(p && ![cls isInstance:p], 0)) {
    JreThrowClassCastExceptionWithIOSClass(p, cls);
  }
  return p;
}

FOUNDATION_EXPORT void JreThrowAssertionError(id __unsafe_unretained msg);

#ifndef NS_BLOCK_ASSERTIONS
#define JreAssert(cond, msg) do { if (!(cond)) JreThrowAssertionError(msg); } while(0)
#else
#define JreAssert(cond, msg)
#endif

// Only expose this function to ARC generated code.
#if __has_feature(objc_arc)
FOUNDATION_EXPORT void JreRelease(id obj);
#endif

FOUNDATION_EXPORT void JreFinalize(id self);

__attribute__((always_inline)) inline void JreCheckFinalize(id self, Class cls) {
  // Use [self java_getClass].objcClass instead of [self class] in case the object
  // has it's class swizzled.
  if ([self java_getClass].objcClass == cls) {
    JreFinalize(self);
  }
}

/*!
 * Defines a mapping of a Java name to its iOS equivalent. These are defined for
 * any Java name that has an iOS name that doesn't follow the default camel-cased
 * name mangling pattern.
 */
typedef struct J2ObjcNameMapping {
  const char * const java_name;
  const char * const ios_name;
} J2ObjcNameMapping;

/*!
 * Defines a mapping between Java and iOS names, using a custom data segment.
 */
#define J2OBJC_NAME_MAPPING(CLASS, JAVANAME, IOSNAME) \
  static J2ObjcNameMapping CLASS##_mapping __attribute__((used, no_sanitize("address"), \
  section("__DATA,__j2objc_aliases"))) = { JAVANAME, IOSNAME };

/*!
 * Defines a data element that corresponds to a Java resource file. These are
 * created using the gen_resource_source.py script. The name_hash is the hash
 * of full_name, used to find a named resource more quickly.
 */
typedef struct J2ObjcResourceDefinition {
  const char * const full_name;
  const int8_t * const data;
  const int32_t length;
  const int32_t name_hash;
} J2ObjcResourceDefinition;

// Preprocessor trick to add quotes to a macro arg:
// https://stackoverflow.com/questions/3419332/c-preprocessor-stringify-the-result-of-a-macro
#define Q(x) #x
#define QUOTE(x) Q(x)

/*!
 * Defines a data resource using a custom data segment. The data segment name length cannot
 * exceed 16 characters.
 */
#define J2OBJC_RESOURCE(BUF, LEN, HASH) \
  static J2ObjcResourceDefinition BUF##_resource __attribute__((used, no_sanitize("address"), \
  section("__DATA,__j2objcresource"))) = { QUOTE(BUF), BUF, LEN, HASH };

FOUNDATION_EXPORT int32_t JreIndexOfStr(NSString *str, NSString **values, int32_t size);
FOUNDATION_EXPORT NSString *JreEnumConstantName(IOSClass *enumClass, int32_t ordinal);

/*!
 * Macros that simplify the syntax for loading of static fields.
 *
 * @define JreLoadStatic
 * @define JreLoadStaticRef
 * @param CLASS The Objective-C class name of the containing class.
 * @param FIELD The name of the static field.
 */
#define JreLoadStatic(CLASS, FIELD) (CLASS##_initialize(), CLASS##_##FIELD)
#define JreLoadStaticRef(CLASS, FIELD) (CLASS##_initialize(), &CLASS##_##FIELD)

/*!
 * Macros for loading enum values.
 * JreEnum provides direct access to the enum value and should only be used
 * internal to the enum class.
 * JreLoadEnum provides the enum value while ensuring the enum class is
 * initialized.
 *
 * @define JreEnum
 * @define JreLoadEnum
 * @param CLASS The enum class name.
 * @param VALUE The enum value name.
 */
#define JreEnum(CLASS, VALUE) CLASS##_values_[CLASS##_Enum_##VALUE]
#define JreLoadEnum(CLASS, VALUE) (CLASS##_initialize(), CLASS##_values_[CLASS##_Enum_##VALUE])

/*!
 * The implementations for retaining and releasing constructors.
 *
 * @define J2OBJC_NEW_IMPL
 * @define J2OBJC_CREATE_IMPL
 * @param CLASS The declaring class
 * @param NAME The constructor name. (eg. initWithInt_)
 * @param ... Parameters to be passed to the initializer.
 */
#if __has_feature(objc_arc)
#define J2OBJC_NEW_IMPL(CLASS, NAME, ...) \
  CLASS *self = [CLASS alloc]; \
  CLASS##_##NAME(self, ##__VA_ARGS__); \
  return self;
#define J2OBJC_CREATE_IMPL(CLASS, NAME, ...) \
  return new_##CLASS##_##NAME(__VA_ARGS__);
#else
#define J2OBJC_NEW_IMPL(CLASS, NAME, ...) \
  CLASS *self = [CLASS alloc]; \
  bool needsRelease = true; \
  @try { \
    CLASS##_##NAME(self, ##__VA_ARGS__); \
    needsRelease = false; \
  } @finally { \
    if (__builtin_expect(needsRelease, 0)) { \
      [self autorelease]; \
    } \
  } \
  return self;
#define J2OBJC_CREATE_IMPL(CLASS, NAME, ...) \
  CLASS *self = [[CLASS alloc] autorelease]; \
  CLASS##_##NAME(self, ##__VA_ARGS__); \
  return self;
#endif

#define J2OBJC_IGNORE_DESIGNATED_BEGIN \
  _Pragma("clang diagnostic push") \
  _Pragma("clang diagnostic ignored \"-Wobjc-designated-initializers\"")
#define J2OBJC_IGNORE_DESIGNATED_END \
  _Pragma("clang diagnostic pop")

/*!
 * Returns correct result when casting a double to an integral type. In C, a
 * float >= Integer.MAX_VALUE (allowing for rounding) returns 0x80000000,
 * while Java requires 0x7FFFFFFF.  A double >= Long.MAX_VALUE returns
 * 0x8000000000000000L, while Java requires 0x7FFFFFFFFFFFFFFFL. Also in C, a
 * floating point NaN value casts to the equivalent MIN_VALUE while Java
 * requires that NaN casts to 0. (JLS 5.1.3)
 */
__attribute__((always_inline))
__attribute__((no_sanitize("float-cast-overflow")))
inline int32_t JreFpToInt(double d) {
  int32_t tmp = (int32_t)d;
  return tmp == (int32_t)0x80000000 ? (d != d ? 0 : (d >= 0 ? 0x7FFFFFFF : tmp)) : tmp;
}
__attribute__((always_inline))
__attribute__((no_sanitize("float-cast-overflow")))
inline int64_t JreFpToLong(double d) {
  int64_t tmp = (int64_t)d;
  return tmp == (int64_t)0x8000000000000000LL
      ? (d != d ? 0 : (d >= 0 ? 0x7FFFFFFFFFFFFFFFL : tmp)) : tmp;
}
__attribute__((always_inline))
__attribute__((no_sanitize("float-cast-overflow")))
inline uint16_t JreFpToChar(double d) {
  unsigned tmp = (unsigned)d;
  return tmp > 0xFFFF || (tmp == 0 && d > 0) ? 0xFFFF : (uint16_t)tmp;
}
__attribute__((always_inline))
inline int16_t JreFpToShort(double d) {
  return (int16_t)JreFpToInt(d);
}
__attribute__((always_inline))
__attribute__((no_sanitize("float-cast-overflow")))
inline int8_t JreFpToByte(double d) {
  return (int8_t)JreFpToInt(d);
}

#define ARITHMETIC_OPERATOR_DEFN(NAME, TYPE, OPNAME, OP, PNAME, PTYPE, CAST) \
  __attribute__((always_inline)) inline TYPE Jre##OPNAME##Assign##NAME##PNAME( \
      TYPE *pLhs, PTYPE rhs) { \
    return *pLhs = CAST(*pLhs OP rhs); \
  }
#define ARITHMETIC_VOLATILE_OPERATOR_DEFN(NAME, TYPE, OPNAME, OP, PNAME, PTYPE, CAST) \
  __attribute__((always_inline)) inline TYPE Jre##OPNAME##AssignVolatile##NAME##PNAME( \
      volatile_##TYPE *pLhs, PTYPE rhs) { \
    TYPE result = CAST(__c11_atomic_load(pLhs, __ATOMIC_SEQ_CST) OP rhs); \
    __c11_atomic_store(pLhs, result, __ATOMIC_SEQ_CST); \
    return result; \
  }
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
#define ARITHMETIC_INTEGRAL_OPERATORS_DEFN(NAME, TYPE, PNAME, PTYPE) \
  ARITHMETIC_VOLATILE_OPERATOR_DEFN(NAME, TYPE, Plus, +, PNAME, PTYPE, (TYPE)) \
  ARITHMETIC_VOLATILE_OPERATOR_DEFN(NAME, TYPE, Minus, -, PNAME, PTYPE, (TYPE)) \
  ARITHMETIC_VOLATILE_OPERATOR_DEFN(NAME, TYPE, Times, *, PNAME, PTYPE, (TYPE)) \
  ARITHMETIC_VOLATILE_OPERATOR_DEFN(NAME, TYPE, Divide, /, PNAME, PTYPE, (TYPE)) \
  ARITHMETIC_VOLATILE_OPERATOR_DEFN(NAME, TYPE, Mod, %, PNAME, PTYPE, (TYPE))
#define ARITHMETIC_FP_OPERATORS_DEFN(NAME, TYPE, PNAME, PTYPE, MODFUNC, CAST) \
  ARITHMETIC_OPERATOR_DEFN(NAME, TYPE, Plus, +, PNAME, PTYPE, CAST) \
  ARITHMETIC_VOLATILE_OPERATOR_DEFN(NAME, TYPE, Plus, +, PNAME, PTYPE, CAST) \
  ARITHMETIC_OPERATOR_DEFN(NAME, TYPE, Minus, -, PNAME, PTYPE, CAST) \
  ARITHMETIC_VOLATILE_OPERATOR_DEFN(NAME, TYPE, Minus, -, PNAME, PTYPE, CAST) \
  ARITHMETIC_OPERATOR_DEFN(NAME, TYPE, Times, *, PNAME, PTYPE, CAST) \
  ARITHMETIC_VOLATILE_OPERATOR_DEFN(NAME, TYPE, Times, *, PNAME, PTYPE, CAST) \
  ARITHMETIC_OPERATOR_DEFN(NAME, TYPE, Divide, /, PNAME, PTYPE, CAST) \
  ARITHMETIC_VOLATILE_OPERATOR_DEFN(NAME, TYPE, Divide, /, PNAME, PTYPE, CAST) \
  MOD_ASSIGN_FP_DEFN(NAME, TYPE, MODFUNC, PNAME, PTYPE, CAST)

ARITHMETIC_INTEGRAL_OPERATORS_DEFN(Char, uint16_t, I, int32_t)
ARITHMETIC_INTEGRAL_OPERATORS_DEFN(Char, uint16_t, J, int64_t)
ARITHMETIC_FP_OPERATORS_DEFN(Char, uint16_t, F, float, fmodf, JreFpToChar)
ARITHMETIC_FP_OPERATORS_DEFN(Char, uint16_t, D, double, fmod, JreFpToChar)
ARITHMETIC_INTEGRAL_OPERATORS_DEFN(Byte, int8_t, I, int32_t)
ARITHMETIC_INTEGRAL_OPERATORS_DEFN(Byte, int8_t, J, int64_t)
ARITHMETIC_FP_OPERATORS_DEFN(Byte, int8_t, F, float, fmodf, JreFpToByte)
ARITHMETIC_FP_OPERATORS_DEFN(Byte, int8_t, D, double, fmod, JreFpToByte)
ARITHMETIC_INTEGRAL_OPERATORS_DEFN(Short, int16_t, I, int32_t)
ARITHMETIC_INTEGRAL_OPERATORS_DEFN(Short, int16_t, J, int64_t)
ARITHMETIC_FP_OPERATORS_DEFN(Short, int16_t, F, float, fmodf, JreFpToShort)
ARITHMETIC_FP_OPERATORS_DEFN(Short, int16_t, D, double, fmod, JreFpToShort)
ARITHMETIC_INTEGRAL_OPERATORS_DEFN(Int, int32_t, I, int32_t)
ARITHMETIC_INTEGRAL_OPERATORS_DEFN(Int, int32_t, J, int64_t)
ARITHMETIC_FP_OPERATORS_DEFN(Int, int32_t, F, float, fmodf, JreFpToInt)
ARITHMETIC_FP_OPERATORS_DEFN(Int, int32_t, D, double, fmod, JreFpToInt)
ARITHMETIC_INTEGRAL_OPERATORS_DEFN(Long, int64_t, J, int64_t)
ARITHMETIC_FP_OPERATORS_DEFN(Long, int64_t, F, float, fmodf, JreFpToLong)
ARITHMETIC_FP_OPERATORS_DEFN(Long, int64_t, D, double, fmod, JreFpToLong)
ARITHMETIC_FP_OPERATORS_DEFN(Float, float, F, float, fmodf, (float))
ARITHMETIC_FP_OPERATORS_DEFN(Float, float, D, double, fmod, (float))
ARITHMETIC_FP_OPERATORS_DEFN(Double, double, D, double, fmod, (double))
#undef ARITHMETIC_OPERATOR_DEFN
#undef ARITHMETIC_VOLATILE_OPERATOR_DEFN
#undef MOD_ASSIGN_FP_DEFN
#undef ARITHMETIC_INTEGRAL_OPERATORS_DEFN
#undef ARITHMETIC_FP_OPERATORS_DEFN

#define SHIFT_OPERATORS_DEFN(NAME, TYPE, UTYPE, MASK) \
  __attribute__((always_inline)) inline TYPE JreLShift##NAME(TYPE lhs, int64_t rhs) { \
    return lhs << (rhs & MASK); \
  } \
  __attribute__((always_inline)) inline TYPE JreRShift##NAME(TYPE lhs, int64_t rhs) { \
    return lhs >> (rhs & MASK); \
  } \
  __attribute__((always_inline)) inline TYPE JreURShift##NAME(TYPE lhs, int64_t rhs) { \
    return (TYPE) (((UTYPE) lhs) >> (rhs & MASK)); \
  }

#define SHIFT_ASSIGN_OPERATORS_DEFN(NAME, TYPE, UTYPE, MASK) \
  __attribute__((always_inline)) inline TYPE JreLShiftAssign##NAME(TYPE *pLhs, int64_t rhs) { \
    return *pLhs = (TYPE) (*pLhs << (rhs & MASK)); \
  } \
  __attribute__((always_inline)) inline TYPE JreRShiftAssign##NAME(TYPE *pLhs, int64_t rhs) { \
    return *pLhs = (TYPE) (*pLhs >> (rhs & MASK)); \
  } \
  __attribute__((always_inline)) inline TYPE JreURShiftAssign##NAME(TYPE *pLhs, int64_t rhs) { \
    return *pLhs = (TYPE) (((UTYPE) *pLhs) >> (rhs & MASK)); \
  } \
  __attribute__((always_inline)) inline TYPE JreLShiftAssignVolatile##NAME( \
      volatile_##TYPE *pLhs, int64_t rhs) { \
    TYPE result = (TYPE)(__c11_atomic_load(pLhs, __ATOMIC_SEQ_CST) << (rhs & MASK)); \
    __c11_atomic_store(pLhs, result, __ATOMIC_SEQ_CST); \
    return result; \
  } \
  __attribute__((always_inline)) inline TYPE JreRShiftAssignVolatile##NAME( \
      volatile_##TYPE *pLhs, int64_t rhs) { \
    TYPE result = __c11_atomic_load(pLhs, __ATOMIC_SEQ_CST) >> (rhs & MASK); \
    __c11_atomic_store(pLhs, result, __ATOMIC_SEQ_CST); \
    return result; \
  } \
  __attribute__((always_inline)) inline TYPE JreURShiftAssignVolatile##NAME( \
      volatile_##TYPE *pLhs, int64_t rhs) { \
    TYPE result = (TYPE)(((UTYPE)__c11_atomic_load(pLhs, __ATOMIC_SEQ_CST)) >> (rhs & MASK)); \
    __c11_atomic_store(pLhs, result, __ATOMIC_SEQ_CST); \
    return result; \
  }

// Shift masks are determined by the JLS spec, section 15.19.
SHIFT_OPERATORS_DEFN(32, int32_t, uint32_t, 0x1f)
SHIFT_OPERATORS_DEFN(64, int64_t, uint64_t, 0x3f)
SHIFT_ASSIGN_OPERATORS_DEFN(Char, uint16_t, uint32_t, 0x1f)
SHIFT_ASSIGN_OPERATORS_DEFN(Byte, int8_t, uint32_t, 0x1f)
SHIFT_ASSIGN_OPERATORS_DEFN(Short, int16_t, uint32_t, 0x1f)
SHIFT_ASSIGN_OPERATORS_DEFN(Int, int32_t, uint32_t, 0x1f)
SHIFT_ASSIGN_OPERATORS_DEFN(Long, int64_t, uint64_t, 0x3f)
#undef SHIFT_OPERATORS_DEFN
#undef SHIFT_ASSIGN_OPERATORS_DEFN

#define BIT_OPERATOR_DEFN(NAME, TYPE, OPNAME, OP) \
  __attribute__((always_inline)) inline TYPE JreBit##OPNAME##AssignVolatile##NAME( \
      volatile_##TYPE *pLhs, TYPE rhs) { \
    TYPE result = __c11_atomic_load(pLhs, __ATOMIC_SEQ_CST) OP rhs; \
    __c11_atomic_store(pLhs, result, __ATOMIC_SEQ_CST); \
    return result; \
  }
#define BIT_OPERATORS_DEFN(NAME, TYPE) \
  BIT_OPERATOR_DEFN(NAME, TYPE, And, &) \
  BIT_OPERATOR_DEFN(NAME, TYPE, Or, |) \
  BIT_OPERATOR_DEFN(NAME, TYPE, Xor, ^)

BIT_OPERATORS_DEFN(Boolean, bool)
BIT_OPERATORS_DEFN(Char, uint16_t)
BIT_OPERATORS_DEFN(Byte, int8_t)
BIT_OPERATORS_DEFN(Short, int16_t)
BIT_OPERATORS_DEFN(Int, int32_t)
BIT_OPERATORS_DEFN(Long, int64_t)
#undef BIT_OPERATOR_DEFN
#undef BIT_OPERATORS_DEFN

#define JRE_HANDLE_DIV_BY_ZERO(NAME, TYPE, OP) \
  __attribute__((always_inline)) inline TYPE Jre##NAME(TYPE op1, TYPE op2) { \
    if (op2 == 0) { \
      JreThrowArithmeticExceptionWithNSString(@"/ by zero"); \
    } \
    return op1 OP op2; \
  }

JRE_HANDLE_DIV_BY_ZERO(IntDiv, int32_t, /);
JRE_HANDLE_DIV_BY_ZERO(LongDiv, int64_t, /);
JRE_HANDLE_DIV_BY_ZERO(IntMod, int32_t, %);
JRE_HANDLE_DIV_BY_ZERO(LongMod, int64_t, %);

// Support for the "==" and "!=" operators. Objective C coalescing of
// string literals only happens with linked bundles, so the same literal string in
// an app and an app extention or dynamic library will have different addresses.
// Support for the "==" and "!=" operators. Objective C coalescing of
// string literals only happens with linked bundles, so the same literal string in
// an app and an app extention or dynamic library will have different addresses.
__attribute__((always_inline)) inline bool JreObjectEqualsEquals(id objA, id objB) {
  return objA == objB ||
         ([objA isKindOfClass:[NSString class]] ? [(NSString *)objA isEqual:objB] : false);
}

__attribute__((always_inline)) inline bool JreStringEqualsEquals(NSString *strA, NSString *strB) {
  return strA == strB || [strA isEqualToString:strB];
}

#pragma pop_macro("I")

#endif  // _J2OBJC_SOURCE_H_
