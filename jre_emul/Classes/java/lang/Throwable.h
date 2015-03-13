// Copyright 2011 Google Inc. All Rights Reserved.
//
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
//  Throwable.h
//  JreEmulation
//
//  Created by Tom Ball on 6/21/11, using j2objc.
//

#ifndef _JavaLangThrowable_H_
#define _JavaLangThrowable_H_

#import "java/io/Serializable.h"
#import "JavaObject.h"

@class JavaIoPrintStream;
@class JavaIoPrintWriter;
@class IOSObjectArray;

@interface JavaLangThrowable : NSException < JavaIoSerializable, JavaObject > {
 @private
  JavaLangThrowable *cause;
  NSString *detailMessage;
  IOSObjectArray *stackTrace;
  IOSObjectArray *suppressedExceptions;
  void **rawCallStack;
  unsigned rawFrameCount;
}
- (instancetype)init;
- (instancetype)initWithNSString:(NSString *)message;
- (instancetype)initWithNSString:(NSString *)message
 withJavaLangThrowable:(JavaLangThrowable *)cause;
- (instancetype)initWithJavaLangThrowable:(JavaLangThrowable *)cause;
- (instancetype)initWithNSString:(NSString *)message
           withJavaLangThrowable:(JavaLangThrowable *)cause
                     withBoolean:(BOOL)enableSuppression
                     withBoolean:(BOOL)writeableStackTrace;
- (JavaLangThrowable *)fillInStackTrace;
- (JavaLangThrowable *)getCause;
- (NSString *)getLocalizedMessage;
- (NSString *)getMessage;
- (IOSObjectArray *)getStackTrace;
- (void)printStackTrace;
- (void)printStackTraceWithJavaIoPrintStream:(JavaIoPrintStream *)ps;
- (void)printStackTraceWithJavaIoPrintWriter:(JavaIoPrintWriter *)w;
- (void)setStackTraceWithJavaLangStackTraceElementArray:(IOSObjectArray *)stackTrace;

// Throwable.initCause() is a public method in the Java API.  The clang
// compiler assumes methods starting with "init" are constructors, which
// when compiled with ARC restricts what code can be in that method.  The
// following forces clang to treat initCause() as a normal method, by
// unsetting its method family.
- (JavaLangThrowable *)initCauseWithJavaLangThrowable:
    (JavaLangThrowable *)cause __attribute__((objc_method_family(none)));

- (void)addSuppressedWithJavaLangThrowable:(JavaLangThrowable *)exception;
- (IOSObjectArray *)getSuppressed;
@end

CF_EXTERN_C_BEGIN

void JavaLangThrowable_init(JavaLangThrowable *self);
JavaLangThrowable *new_JavaLangThrowable_init();

void JavaLangThrowable_initWithNSString_(JavaLangThrowable *self, NSString *message);
JavaLangThrowable *new_JavaLangThrowable_initWithNSString_(NSString *message);

void JavaLangThrowable_initWithNSString_withJavaLangThrowable_(
    JavaLangThrowable *self, NSString *message, JavaLangThrowable *causeArg);
JavaLangThrowable *new_JavaLangThrowable_initWithNSString_withJavaLangThrowable_(
    NSString *message, JavaLangThrowable *causeArg);

void JavaLangThrowable_initWithJavaLangThrowable_(
    JavaLangThrowable *self, JavaLangThrowable *causeArg);
JavaLangThrowable *new_JavaLangThrowable_initWithJavaLangThrowable_(JavaLangThrowable *causeArg);

void JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
    JavaLangThrowable *self, NSString *message, JavaLangThrowable *causeArg, BOOL enableSuppression,
    BOOL writeableStackTrace);
JavaLangThrowable *
    new_JavaLangThrowable_initWithNSString_withJavaLangThrowable_withBoolean_withBoolean_(
    NSString *message, JavaLangThrowable *causeArg, BOOL enableSuppression,
    BOOL writeableStackTrace);

CF_EXTERN_C_END

J2OBJC_EMPTY_STATIC_INIT(JavaLangThrowable)

J2OBJC_TYPE_LITERAL_HEADER(JavaLangThrowable)

#endif // _JavaLangThrowable_H_
