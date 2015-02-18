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
#import <libkern/OSAtomic.h>  // OSMemoryBarrier used in initialize methods.

// Only expose this function to ARC generated code.
#if __has_feature(objc_arc)
FOUNDATION_EXPORT void JreRelease(id obj);
#endif

#endif  // _J2OBJC_SOURCE_H_
