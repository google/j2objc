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
// Throwable.h
// JreEmulation
//
// Backwards-compatibility header for those classes that haven't
// upgraded to use the java.lang.Throwable->NSException mapping.
// TODO(tball): remove file when clients have updated.
//

#ifndef JavaLangThrowable_H
#define JavaLangThrowable_H

#include "J2ObjC_header.h"
#include "NSException+JavaThrowable.h"

// Compatibility alias and methods are defined in NSException+JavaThrowable.h

__attribute__((always_inline))
inline void JavaLangThrowable_init(JavaLangThrowable *self) {
  NSException_init(self);
}

__attribute__((always_inline))
inline JavaLangThrowable *new_JavaLangThrowable_init() {
  return new_NSException_init();
}

__attribute__((always_inline))
inline void JavaLangThrowable_initWithNSString_(JavaLangThrowable *self, NSString *detailMessage) {
  NSException_initWithNSString_(self, detailMessage);
}

__attribute__((always_inline))
inline JavaLangThrowable *new_JavaLangThrowable_initWithNSString_(NSString *detailMessage) {
  return new_NSException_initWithNSString_(detailMessage);
}

__attribute__((always_inline))
inline void JavaLangThrowable_initWithNSString_withJavaLangThrowable_(
    JavaLangThrowable *self, NSString *detailMessage, JavaLangThrowable *cause) {
  NSException_initWithNSString_withNSException_(self, detailMessage, cause);
}

__attribute__((always_inline))
inline JavaLangThrowable *new_JavaLangThrowable_initWithNSString_withJavaLangThrowable_(
    NSString *detailMessage, JavaLangThrowable *cause) {
  return new_NSException_initWithNSString_withNSException_(detailMessage, cause);
}

__attribute__((always_inline))
inline void JavaLangThrowable_initWithJavaLangThrowable_(
    JavaLangThrowable *self, JavaLangThrowable *cause) {
  NSException_initWithNSException_(self, cause);
}

__attribute__((always_inline))
inline JavaLangThrowable *new_JavaLangThrowable_initWithJavaLangThrowable_(
    JavaLangThrowable *cause) {
  return new_NSException_initWithNSException_(cause);
}

__attribute__((always_inline))
inline void JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
    JavaLangThrowable *self, NSString *detailMessage, JavaLangThrowable *cause,
    jboolean enableSuppression, jboolean writableStackTrace) {
  NSException_initWithNSString_withNSException_withBoolean_withBoolean_(
      self, detailMessage, cause, enableSuppression, writableStackTrace);
}

__attribute__((always_inline))
inline JavaLangThrowable *
new_JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
    NSString *detailMessage, JavaLangThrowable *cause, jboolean enableSuppression,
    jboolean writableStackTrace) {
  return new_NSException_initWithNSString_withNSException_withBoolean_withBoolean_(
      detailMessage, cause, enableSuppression, writableStackTrace);
}

#endif // JavaLangThrowable_H
