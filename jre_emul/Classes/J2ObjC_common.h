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

#import <CoreFoundation/CoreFoundation.h>
#import <Foundation/Foundation.h>

#import "J2ObjC_types.h"
#import "pthread.h"

//#define J2OBJC_USE_GC 1

#define ARGC_WEAK_REF __unsafe_unretained

#if J2OBJC_USE_GC
  #define ARGC_FIELD_REF __unsafe_unretained
  #import "ARGC/ARGC.h"
#else
  #define ARGC_FIELD_REF __strong
  id ARGC_allocateArray(Class cls, NSUInteger extraBytes, NSZone* zone) NS_RETURNS_RETAINED J2OBJC_METHOD_ATTR;
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
#  define ARCBRIDGE_RETAINED __bridge_retained
#  define ARCBRIDGE_TRANSFER __bridge_transfer
#  define ARC_CONSUME_PARAMETER __attribute((ns_consumed))
#  define AUTORELEASE(x) x
#  define RELEASE_(x) (void)x
#  define RETAIN_(x) x
#  define RETAIN_AND_AUTORELEASE(x) x
#  define DEALLOC_(x)
# else
#  define ARCBRIDGE
#  define ARCBRIDGE_RETAINED
#  define ARCBRIDGE_TRANSFER
#  define ARC_CONSUME_PARAMETER
#  define AUTORELEASE(x) [x autorelease]
#  define RELEASE_(x) [x release]
#  define RETAIN_(x) [x retain]
#  define RETAIN_AND_AUTORELEASE(x) [[x retain] autorelease]
#  define DEALLOC_(x) [x dealloc]
# endif

CF_EXTERN_C_BEGIN

id JreThrowNullPointerException() __attribute__((noreturn)) J2OBJC_METHOD_ATTR;
void JreThrowClassCastException(id p, Class cls) __attribute__((noreturn)) J2OBJC_METHOD_ATTR;
void JreThrowClassCastExceptionWithIOSClass(id p, IOSClass *cls) __attribute__((noreturn)) J2OBJC_METHOD_ATTR;
void JreThrowArithmeticExceptionWithNSString(NSString *msg) __attribute__((noreturn)) J2OBJC_METHOD_ATTR;

#if J2OBJC_USE_GC
#define JavaLangObject ARGCObject

__attribute__((always_inline)) inline id JreStrongAssign(__strong id *pIvar, __unsafe_unretained id value) {
  ARGC_assignStrongObject(pIvar, value);
  return value;
}

__attribute__((always_inline)) inline id JreNativeFieldAssign(__strong id *pIvar, __unsafe_unretained id value) {
  ARGC_assignStrongObject(pIvar, value);
  return value;
}

__attribute__((always_inline)) inline id JreObjectFieldAssign(__unsafe_unretained id *pIvar, __unsafe_unretained id value) J2OBJC_METHOD_ATTR {
  ARGC_assignARGCObject(pIvar, value);
  return value;
}

__attribute__((always_inline)) inline id JreWeakRefFieldAssign(__unsafe_unretained id *pIvar, __unsafe_unretained id value) J2OBJC_METHOD_ATTR {
  ARGC_assignGenericObject(pIvar, value);
  return value;
}

__attribute__((always_inline)) inline id JreGenericFieldAssign(__unsafe_unretained id *pIvar, __unsafe_unretained id value) J2OBJC_METHOD_ATTR {
  ARGC_assignGenericObject(pIvar, value);
  return value;
}

#define JreStrongAssignAndConsume       JreStrongAssign
#define JreStaticAssign                 JreStrongAssign
#define JreStaticAssignAndConsume       JreStrongAssign

#define JreObjectFieldAssignAndConsume  JreObjectFieldAssign
#define JreNativeFieldAssignAndConsume  JreNativeFieldAssign
#define JreWeakRefFieldAssignAndConsume JreWeakRefFieldAssign
#define JreGenericFieldAssignAndConsume JreGenericFieldAssign

#else
@interface JavaLangObject : NSObject
@end

__attribute__((always_inline)) inline id JreStrongAssign(__strong id *pIvar, __unsafe_unretained id value) {
  AUTORELEASE(*pIvar);
  *pIvar = RETAIN_(value);
  return value;
}

__attribute__((always_inline)) inline id JreStrongAssignAndConsume(__strong id *pIvar, NS_RELEASES_ARGUMENT id value) {
  AUTORELEASE(*pIvar);
  *pIvar = value;
  return value;
}

__attribute__((always_inline)) inline id JreWeakRefFieldAssign(__unsafe_unretained id *pIvar, __unsafe_unretained id value) {
  AUTORELEASE(*pIvar);
  *pIvar = RETAIN_(value);
  return value;
}

__attribute__((always_inline)) inline id JreWeakRefFieldAssignAndConsume(__unsafe_unretained id *pIvar, NS_RELEASES_ARGUMENT id value) {
  AUTORELEASE(*pIvar);
  *pIvar = value;
  return value;
}


#define JreStaticAssign                  JreStrongAssign
#define JreStaticAssignAndConsume        JreStrongAssignAndConsume
#define JreNativeFieldAssign             JreStrongAssign
#define JreNativeFieldAssignAndConsume   JreStrongAssignAndConsume
#define JreObjectFieldAssign             JreStrongAssign
#define JreObjectFieldAssignAndConsume   JreStrongAssignAndConsume
#define JreGenericFieldAssign            JreStrongAssign
#define JreGenericFieldAssignAndConsume  JreStrongAssignAndConsume

#endif

id JreLoadVolatileId(volatile_id *pVar);
id JreAssignVolatileId(volatile_id *pVar, __unsafe_unretained id value);
id JreVolatileStrongAssign(volatile_id *pIvar, __unsafe_unretained id value);
jboolean JreCompareAndSwapVolatileStrongId(volatile_id *pVar, __unsafe_unretained id expected, __unsafe_unretained id newValue);
id JreExchangeVolatileStrongId(volatile_id *pVar, __unsafe_unretained id newValue);
void JreReleaseVolatile(volatile_id *pVar);

#define JreRetainedLocalValue(value) AUTORELEASE(RETAIN_((id)(value)))
id JreVolatileNativeAssign(volatile_id *pIvar, __unsafe_unretained id value);
void JreCloneVolatile(volatile_id *pVar, volatile_id *pOther);
void JreCloneVolatileStrong(volatile_id *pVar, volatile_id *pOther);
void JreReleaseVolatile(volatile_id *pVar);

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
#define nil_chk(p) (p ?: JreThrowNullPointerException())


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


FOUNDATION_EXPORT NSString* JreStringConstant(NSString* str);

#define JreString(idx, text)  _string_##idx

FOUNDATION_EXPORT void empty_static_initialize(void);

FOUNDATION_EXPORT void IOSClass_init_class_(pthread_t* initToken, Class cls, void(*clinit)());
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
  void CLASS##_initialize() { \
    static pthread_t CLASS##_initToken = 0; \
    if (CLASS##_initToken != (pthread_t)~0L) { \
      IOSClass_init_class_(&CLASS##_initToken, CLASS.class, CLASS##__clinit__); \
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


#define J2OBJC_FIELD_SETTER(CLASS, REF_TYPE, FIELD, TYPE) \
__attribute__((unused)) static inline void CLASS##_set_##FIELD(CLASS *instance, TYPE value) J2OBJC_METHOD_ATTR { \
 Jre##REF_TYPE##FieldAssign(&instance->FIELD, value); \
}\
__attribute__((unused)) static inline void CLASS##_setAndConsume_##FIELD( \
CLASS *instance, NS_RELEASES_ARGUMENT TYPE value) J2OBJC_METHOD_ATTR { \
 Jre##REF_TYPE##FieldAssignAndConsume(&instance->FIELD, value); \
}

#define J2OBJC_VOLATILE_FIELD_SETTER(CLASS, REF_TYPE, FIELD, TYPE) \
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
#if __has_feature(objc_arc)
#define J2OBJC_ETERNAL_SINGLETON // error!! NO Singlton surpported in ARC.
#else
#define J2OBJC_ETERNAL_SINGLETON \
  - (id)retain { return self; } \
  - (oneway void)release {} \
  - (id)autorelease { return self; }
#endif

/*!
 A type to represent an Objective C class.
 This is actually an `objc_class` but the runtime headers will not allow us to
 reference `objc_class`, so we have defined our own.

 Adapted from:
 https://github.com/protocolbuffers/protobuf/blob/master/objectivec/GPBRuntimeTypes.h
*/
typedef struct J2ObjCClass_t J2ObjCClass_t;

/*!
 Macros for generating a Class from a class name. These are used wherever a
 static Objective C class reference is needed for a generated class. Unlike
 "[classname class]", this macro doesn't trigger class initialization, avoiding
 the chance of Objective C initialization deadlocks.

 Adapted from:
 https://github.com/protocolbuffers/protobuf/blob/master/objectivec/GPBUtilities_PackagePrivate.h
 */
#define J2OBJC_CLASS_SYMBOL(name) OBJC_CLASS_$_##name
#define J2OBJC_CLASS_REFERENCE(name) \
    ((__bridge Class)&(J2OBJC_CLASS_SYMBOL(name)))
#define J2OBJC_CLASS_DECLARATION(name) \
    extern const J2ObjCClass_t J2OBJC_CLASS_SYMBOL(name)

#endif // _J2OBJC_COMMON_H_
