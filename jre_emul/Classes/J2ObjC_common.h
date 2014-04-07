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

// Common defines needed by all J2ObjC header files.

#ifndef _J2OBJC_COMMON_H_
#define _J2OBJC_COMMON_H_

#import <Foundation/Foundation.h>

/*!
 * Defines an init function for a class that will ensure that the class is
 * initialized. For class "Foo" the function will have the following signature:
 *   inline void Foo_init();
 *
 * @define J2OBJC_STATIC_INIT
 * @param CLASS The class to declare the init function for.
 */
#define J2OBJC_STATIC_INIT(CLASS) \
  __attribute__((always_inline)) inline void CLASS##_init() { \
    if (!__builtin_expect(CLASS##_initialized, YES)) { \
      [CLASS class]; \
    } \
  }

/*!
 * Defines the getter for a static variable. For class "Foo" and field "bar_"
 * with type "int" the getter will have the following signature:
 *   inline int Foo_get_bar_();
 *
 * @define J2OBJC_STATIC_FIELD_GETTER
 * @param CLASS The class containing the static variable.
 * @param FIELD The name of the static variable.
 * @param TYPE The type of the static variable.
 */
#define J2OBJC_STATIC_FIELD_GETTER(CLASS, FIELD, TYPE) \
  __attribute__((always_inline)) inline TYPE CLASS##_get_##FIELD() { \
    CLASS##_init(); \
    return CLASS##_##FIELD; \
  }

/*!
 * Defines the reference getter for a static variable. For class "Foo" and field
 * "bar_" with type "int" the getter will have the following signature:
 *   inline int *Foo_getRef_bar_();
 *
 * @define J2OBJC_STATIC_FIELD_REF_GETTER
 * @param CLASS The class containing the static variable.
 * @param FIELD The name of the static variable.
 * @param TYPE The type of the static variable.
 */
#define J2OBJC_STATIC_FIELD_REF_GETTER(CLASS, FIELD, TYPE) \
  __attribute__((always_inline)) inline TYPE *CLASS##_getRef_##FIELD() { \
    CLASS##_init(); \
    return &CLASS##_##FIELD; \
  }

/*!
 * Defines the setter for a static variable with an object type. For class "Foo"
 * and field "bar_" with type "NSString *" the getter will have the following
 * signature:
 *   inline NSString *Foo_set_bar_(NSString *value);
 *
 * @define J2OBJC_STATIC_FIELD_SETTER
 * @param CLASS The class containing the static variable.
 * @param FIELD The name of the static variable.
 * @param TYPE The type of the static variable.
 */
#if __has_feature(objc_arc)
#define J2OBJC_STATIC_FIELD_SETTER(CLASS, FIELD, TYPE) \
  __attribute__((always_inline)) inline TYPE CLASS##_set_##FIELD(TYPE value) { \
    CLASS##_init(); \
    return CLASS##_##FIELD = value; \
  }
#else
#define J2OBJC_STATIC_FIELD_SETTER(CLASS, FIELD, TYPE) \
  __attribute__((always_inline)) inline TYPE CLASS##_set_##FIELD(TYPE value) { \
    CLASS##_init(); \
    return JreOperatorRetainedAssign(&CLASS##_##FIELD, nil, value); \
  }
#endif

/*!
 * Adds noop implementations for the memory management methods. This helps to
 * avoid the cost of incrementing and decrementing the retain count for objects
 * that should never be dealloc'ed.
 *
 * @define J2OBJC_ETERNAL_SINGLETON
 */
#define J2OBJC_ETERNAL_SINGLETON \
  - (id)retain { return self; } \
  - (oneway void)release {} \
  - (id)autorelease { return self; }

#endif // _J2OBJC_COMMON_H_
