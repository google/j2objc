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
//  DebugUtils.h: low-level functions to aid debugging.
//  JreEmulation
//
//  Created by Tom Ball on 3/4/14.
//

#ifndef JreEmulation_DebugUtils_h
#define JreEmulation_DebugUtils_h

#import "JreEmulation.h"

@interface DebugUtils : NSObject {
}

+(void)logStack:(NSUInteger)nFrames;
+(void)logStack:(NSUInteger)nFrames withMessage:(NSString *)msg;

@end

#endif
