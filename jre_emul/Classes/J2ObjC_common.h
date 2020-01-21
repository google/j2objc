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

// Common definitions needed by all J2ObjC generated files.

#ifndef _J2OBJC_COMMON_H_
#define _J2OBJC_COMMON_H_

#pragma clang system_header
#pragma clang diagnostic ignored "-Wignored-attributes"

#import <Foundation/Foundation.h>

#import "J2ObjC_types.h"

#define J2OBJC_USE_GC 1
#ifdef J2OBJC_USE_GC
#import "ARGC/ARGC.h"
#endif

@class IOSClass;
@protocol JavaLangIterable;

#ifndef __has_feature
#define __has_feature(x) 0  // Compatibility with non-clang compilers.
#endif

# ifndef OBJC_METHOD_FAMILY_NONE
#  if __has_attribute(objc_method_family)
#   define OBJC_METHOD_FAMILY_NONE __attribute__((objc_method_family(none)))
#  else
#   define OBJC_METHOD_FAMILY_NONE
#  endif
# endif

# if __has_feature(objc_arc)
#  define ARCBRIDGE __bridge
#  define ARCBRIDGE_TRANSFER __bridge_transfer
#  define ARC_CONSUME_PARAMETER __attribute((ns_consumed))
#  define AUTORELEASE(x) x
#  define RELEASE_(x) (void)x
#  define RETAIN_(x) x
#  define RETAIN_AND_AUTORELEASE(x) x
#  define DEALLOC_(x)
# else
#  define ARCBRIDGE
#  define ARCBRIDGE_TRANSFER
#  define ARC_CONSUME_PARAMETER
#  define AUTORELEASE(x) [x autorelease]
#  define RELEASE_(x) [x release]
#  define RETAIN_(x) [x retain]
#  define RETAIN_AND_AUTORELEASE(x) [[x retain] autorelease]
#  define DEALLOC_(x) [x dealloc]
# endif

#ifdef J2OBJC_DISABLE_ALL_CHECKS
 #define J2OBJC_DISABLE_NIL_CHECKS 1
 #define J2OBJC_DISABLE_CAST_CHECKS 1
 #define J2OBJC_DISABLE_ARRAY_CHECKS 1
#endif

#ifdef J2OBJC_DISABLE_ARRAY_CHECKS
 #define J2OBJC_DISABLE_ARRAY_BOUND_CHECKS 1
 #define J2OBJC_DISABLE_ARRAY_TYPE_CHECKS 1
#endif

CF_EXTERN_C_BEGIN

id JreThrowNullPointerException(id p) __attribute__((noreturn)) J2OBJC_METHOD_ATTR;
void JreThrowClassCastException(id p, Class cls) __attribute__((noreturn)) J2OBJC_METHOD_ATTR;
void JreThrowClassCastExceptionWithIOSClass(id p, IOSClass *cls) __attribute__((noreturn)) J2OBJC_METHOD_ATTR;

#ifdef J2OBJC_USE_GC
#define JavaLangObject ARGCObject

#define JreStrongAssign                 ARGC_assignStrongObject
#define JreStrongAssignAndConsume       ARGC_assignStrongObject
__attribute__((always_inline)) inline id JreStrongAssignAndGet(__strong id *pIvar, id value) J2OBJC_METHOD_ATTR {
    JreStrongAssign(pIvar, value);
    return value;
}

#define JreNativeFieldAssign              ARGC_assignStrongObject
#define JreNativeFieldAssignAndConsume    ARGC_assignStrongObject
__attribute__((always_inline)) inline id JreNativeFieldAssignAndGet(__strong id *pIvar, id value) J2OBJC_METHOD_ATTR {
    JreNativeFieldAssign(pIvar, value);
    return value;
}

#define JreObjectFieldAssign            ARGC_assignARGCObject
#define JreObjectFieldAssignAndConsume  ARGC_assignARGCObject
__attribute__((always_inline)) inline id JreObjectFieldAssignAndGet(__unsafe_unretained id *pIvar, id value) J2OBJC_METHOD_ATTR {
    JreObjectFieldAssign(pIvar, value);
    return value;
}

#define JreGenericFieldAssign           ARGC_assignGenericObject
#define JreGenericFieldAssignAndConsume ARGC_assignGenericObject
__attribute__((always_inline)) inline id JreGenericFieldAssignAndGet(__unsafe_unretained id *pIvar, id value) J2OBJC_METHOD_ATTR {
    JreGenericFieldAssign(pIvar, value);
    return value;
}

#else

id JreStrongAssign(__strong id *pIvar, id value);
id JreStrongAssignAndConsume(__strong id *pIvar, NS_RELEASES_ARGUMENT id value);
#endif

id JreLoadVolatileId(volatile_id *pVar);
id JreAssignVolatileId(volatile_id *pVar, __unsafe_unretained id value);
id JreVolatileStrongAssign(volatile_id *pIvar, __unsafe_unretained id value);
jboolean JreCompareAndSwapVolatileStrongId(volatile_id *pVar, __unsafe_unretained id expected, __unsafe_unretained id newValue);
id JreExchangeVolatileStrongId(volatile_id *pVar, __unsafe_unretained id newValue);
void JreReleaseVolatile(volatile_id *pVar);
#ifdef J2OBJC_USE_GC
id JreVolatileNativeAssign(volatile_id *pIvar, __unsafe_unretained id value);
#else
void JreCloneVolatile(volatile_id *pVar, volatile_id *pOther);
#endif
void JreCloneVolatileStrong(volatile_id *pVar, volatile_id *pOther);

id JreRetainedWithAssign(id parent, __strong id *pIvar, __unsafe_unretained id value);
id JreVolatileRetainedWithAssign(id parent, volatile_id *pIvar, __unsafe_unretained id value);
void JreRetainedWithRelease(__unsafe_unretained id parent, __unsafe_unretained id child);
void JreVolatileRetainedWithRelease(__unsafe_unretained id parent, volatile_id *pVar);

NSString *JreStrcat(const char *types, ...);

jboolean JreAnnotationEquals(id a1, id a2);
jint JreAnnotationHashCode(id a);

NSUInteger JreDefaultFastEnumeration(
    id<JavaLangIterable> obj, NSFastEnumerationState *state, id __unsafe_unretained *stackbuf);

CF_EXTERN_C_END

/*!
 * The nil_chk macro is used wherever a Java object is dereferenced and needs to
 * be checked for null. A macro is used instead of an inline function because it
 * allows the line number of the dereference to be derived from the stack frame.
 *
 * @param p The object to check for nil.
 */
#ifdef J2OBJC_DISABLE_NIL_CHECKS
#define nil_chk(p) p
#else
#define nil_chk(p) (p ?: JreThrowNullPointerException(nil))
#endif

// #if !__has_feature(objc_arc)
__attribute__((always_inline)) inline __unsafe_unretained id JreAutoreleasedAssign(
    __unsafe_unretained id *pIvar, __unsafe_unretained  id value) J2OBJC_METHOD_ATTR {
    AUTORELEASE(value);
    JreGenericFieldAssign(pIvar, value);
    return value;
}
// #endif

// #if !__has_feature(objc_arc)
__attribute__((always_inline)) inline id JreRetainedLocalValue(id value) J2OBJC_METHOD_ATTR {
  return AUTORELEASE(RETAIN_(value));
}
// #endif

/*!
 * Utility macro for passing an argument that contains a comma.
 */
#define J2OBJC_ARG(...) __VA_ARGS__

#define J2OBJC_VOLATILE_ACCESS_DEFN(NAME, TYPE) \
  __attribute__((always_inline)) inline TYPE JreLoadVolatile##NAME(volatile_##TYPE *pVar) J2OBJC_METHOD_ATTR { \
    return __c11_atomic_load(pVar, __ATOMIC_SEQ_CST); \
  } \
  __attribute__((always_inline)) inline TYPE JreAssignVolatile##NAME( \
      volatile_##TYPE *pVar, TYPE value) J2OBJC_METHOD_ATTR { \
    __c11_atomic_store(pVar, value, __ATOMIC_SEQ_CST); \
    return value; \
  }

J2OBJC_VOLATILE_ACCESS_DEFN(Boolean, jboolean)
J2OBJC_VOLATILE_ACCESS_DEFN(Char, jchar)
J2OBJC_VOLATILE_ACCESS_DEFN(Byte, jbyte)
J2OBJC_VOLATILE_ACCESS_DEFN(Short, jshort)
J2OBJC_VOLATILE_ACCESS_DEFN(Int, jint)
J2OBJC_VOLATILE_ACCESS_DEFN(Long, jlong)
J2OBJC_VOLATILE_ACCESS_DEFN(Float, jfloat)
J2OBJC_VOLATILE_ACCESS_DEFN(Double, jdouble)
#undef J2OBJC_VOLATILE_ACCESS_DEFN

/*!
 * Defines the initialized flag for a class.
 *
 * @define J2OBJC_INITIALIZED_DEFN
 * @param CLASS The class for which the initialized flag is defined.
 */
#define J2OBJC_INITIALIZED_DEFN(CLASS) \
   static _Atomic(jboolean) CLASS##__initialized;

/*!
 * Defines the code to set a class's initialized flag. This should be used at
 * the end of each class's initialize class method.
 *
 * @define J2OBJC_SET_INITIALIZED
 * @param CLASS The class who's flag is to be set.
 */
#define J2OBJC_SET_INITIALIZED(CLASS) \
  __c11_atomic_store(&CLASS##__initialized, true, __ATOMIC_RELEASE);

/*!
 * Defines an init function for a class that will ensure that the class is
 * initialized. For class "Foo" the function will have the following signature:
 *   inline void Foo_initialize();
 *
 * @define J2OBJC_STATIC_INIT
 * @param CLASS The class to declare the init function for.
 */
#define J2OBJC_STATIC_INIT(CLASS) \
  FOUNDATION_EXPORT void CLASS##_initialize(void);

/*!
 * Defines an empty init function for a class that has no initialization code.
 *
 * @define J2OBJC_EMPTY_STATIC_INIT
 * @param CLASS The class to declare the init function for.
 */
#define J2OBJC_EMPTY_STATIC_INIT(CLASS) \
  __attribute__((always_inline)) inline void CLASS##_initialize(void) {}

FOUNDATION_EXPORT void empty_static_initialize(void);

/*!
 * Declares the type literal accessor for a type. This macro should be added to
 * the header of each generated Java type.
 *
 * @define J2OBJC_TYPE_LITERAL_HEADER
 * @param TYPE The name of the type to declare the accessor for.
 */
#define J2OBJC_TYPE_LITERAL_HEADER(TYPE) \
  FOUNDATION_EXPORT IOSClass *TYPE##_class_(void);

#define J2OBJC_CLASS_INITIALIZE_SOURCE(CLASS) \
  static _Atomic(jboolean) CLASS##__initialized = false; \
  void CLASS##_initialize() { \
    if (!CLASS##__initialized) { \
      jboolean compare = false; \
      if (__c11_atomic_compare_exchange_strong(&CLASS##__initialized, &compare, true, \
          __ATOMIC_SEQ_CST, __ATOMIC_SEQ_CST)) { \
        CLASS##__clinit__(); \
      } \
    } \
  }

/*!
 * Defines the type literal accessor for a class or enum type. This macro should
 * be added to the implementation of each generated Java type.
 *
 * @define J2OBJC_CLASS_TYPE_LITERAL_SOURCE
 * @param TYPE The name of the type to define the accessor for.
 */
#define J2OBJC_CLASS_TYPE_LITERAL_SOURCE(TYPE) \
  IOSClass *TYPE##_class_() { \
    static IOSClass *cls; \
    static dispatch_once_t token; \
    dispatch_once(&token, ^{ cls = IOSClass_fromClass([TYPE class]); }); \
    return cls; \
  }


/*!
 * Defines the type literal accessor for a interface or annotation type. This
 * macro should be added to the implementation of each generated Java type.
 *
 * @define J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE
 * @param TYPE The name of the type to define the accessor for.
 */
#define J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(TYPE) \
  IOSClass *TYPE##_class_() { \
    static IOSClass *cls; \
    static dispatch_once_t token; \
    dispatch_once(&token, ^{ cls = IOSClass_fromProtocol((id)@protocol(TYPE)); }); \
    return cls; \
  }


#ifdef J2OBJC_USE_GC
#define J2OBJC_FIELD_SETTER(CLASS, REF, FIELD, TYPE) \
__attribute__((unused)) static inline void CLASS##_set_##FIELD(CLASS *instance, TYPE value) J2OBJC_METHOD_ATTR { \
 Jre##REF##FieldAssign(&instance->FIELD, value); \
}\
__attribute__((unused)) static inline void CLASS##_setAndConsume_##FIELD( \
CLASS *instance, NS_RELEASES_ARGUMENT TYPE value) J2OBJC_METHOD_ATTR { \
 Jre##REF##FieldAssignAndConsume(&instance->FIELD, value); \
}
#elif __has_feature(objc_arc)
#define J2OBJC_FIELD_SETTER(CLASS, FIELD, TYPE) \
  __attribute__((unused)) static inline void CLASS##_set_##FIELD(CLASS *instance, TYPE value) J2OBJC_METHOD_ATTR { \
     instance->FIELD = value; \
  }
#else
#define J2OBJC_FIELD_SETTER(CLASS, FIELD, TYPE) \
__attribute__((unused)) static inline void CLASS##_set_##FIELD(CLASS *instance, TYPE value) J2OBJC_METHOD_ATTR { \
 JreStrongAssign(&instance->FIELD, value); \
}\
__attribute__((unused)) static inline void CLASS##_setAndConsume_##FIELD( \
CLASS *instance, NS_RELEASES_ARGUMENT TYPE value) J2OBJC_METHOD_ATTR { \
 JreStrongAssignAndConsume(&instance->FIELD, value); \
}
#endif

#define J2OBJC_VOLATILE_FIELD_SETTER(CLASS, FIELD, TYPE) \
  __attribute__((unused)) static inline void CLASS##_set_##FIELD(CLASS *instance, TYPE value) J2OBJC_METHOD_ATTR { \
     JreVolatileStrongAssign(&instance->FIELD, value); \
  }

/*!
 * Adds noop implementations for the memory management methods. This helps to
 * avoid the cost of incrementing and decrementing the retain count for objects
 * that should never be dealloc'ed.
 *
 * @define J2OBJC_ETERNAL_SINGLETON
 */
#ifdef J2OBJC_USE_GC
#define J2OBJC_ETERNAL_SINGLETON // error!! NO Singlton surpported in ARC.
#else
#define J2OBJC_ETERNAL_SINGLETON \
  - (id)retain { return self; } \
  - (oneway void)release {} \
  - (id)autorelease { return self; }
#endif


#endif // _J2OBJC_COMMON_H_
