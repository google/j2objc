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
//  IOSReference.h
//  JreEmulation
//
//  Created by Tom Ball on 8/15/13.
//

#ifndef _IOSREFERENCE_H_
#define _IOSREFERENCE_H_

#import "JreEmulation.h"

@class JavaLangRefReference;

// Helper class for java.lang.ref.Reference. It uses method swizzling
// to hook the referent object's dealloc method, so the reference's
// field is zeroed and, optionally, a copy of the referent is queued
// in the associated reference queue.
@interface IOSReference : NSObject

// Methods should only be called by java.lang.ref.Reference.
+ (void)initReferent:(JavaLangRefReference *)reference;
+ (void)strengthenReferent:(JavaLangRefReference *)reference;
+ (void)weakenReferent:(JavaLangRefReference *)reference;
+ (void)deallocReferent:(JavaLangRefReference *)reference;

// Test-only method to fake a low-memory condition.
+ (void)handleMemoryWarning:(NSNotification *)notification;

@end

#endif // _IOSREFERENCE_H_
