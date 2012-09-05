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

#import <Foundation/Foundation.h>
#import "java/io/Serializable.h"

@class JavaIoPrintWriter;

@interface JavaLangThrowable : NSException < JavaIoSerializable > {
 @private
  JavaLangThrowable *cause;
  NSString *detailMessage;
}
- (id)init;
- (id)initWithNSString:(NSString *)message;
- (id)initWithNSString:(NSString *)message
 withJavaLangThrowable:(JavaLangThrowable *)cause;
- (id)initWithJavaLangThrowable:(JavaLangThrowable *)cause;
- (JavaLangThrowable *)fillInStackTrace;
- (JavaLangThrowable *)getCause;
- (NSString *)getLocalizedMessage;
- (NSString *)getMessage;
- (NSMutableArray *)getStackTrace;
- (JavaLangThrowable *)initCauseWithJavaLangThrowable:
    (JavaLangThrowable *)cause;
- (void)printStackTrace;
- (void)printStackTraceWithJavaIoPrintWriter:(JavaIoPrintWriter *)writer;
- (void)setStackTraceWithJavaLangStackTraceElementArray:(IOSObjectArray *)stackTrace;
@end
