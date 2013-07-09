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
// Strong reference info.
//

#ifndef _JreMemDebugStrongReference_H_
#define _JreMemDebugStrongReference_H_

#import <Foundation/Foundation.h>

@interface JreMemDebugStrongReference : NSObject

+ (JreMemDebugStrongReference *)strongReferenceWithObject:(id)object name:(NSString *)name;

#if __has_feature(objc_arc)
@property (nonatomic, weak) id object;
@property (nonatomic, strong) NSString *name;
#else
@property (nonatomic, assign) id object;
@property (nonatomic, retain) NSString *name;
#endif

@end

#endif // _JreMemDebugStrongReference_H_
