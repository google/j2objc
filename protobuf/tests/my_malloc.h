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
//  Created by Keith Stanger on 9/24/13.
//
//  Hooks into malloc to record memory allocation and deallocation.
//  NOTE: Make sure to set NSZombieEnabled=NO. Having zombies on will interfere
//  with the reporting of memory usage because it causes NSStrings to be
//  allocated when an object is deallocated.

#ifndef __my_malloc__
#define __my_malloc__

#import <Foundation/Foundation.h>

FOUNDATION_EXPORT void my_malloc_install();
FOUNDATION_EXPORT void my_malloc_clear();
FOUNDATION_EXPORT void my_malloc_reset();
FOUNDATION_EXPORT void my_malloc_reset_and_report();

#endif // __my_malloc__
