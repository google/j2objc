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
//  Iterable.h
//  JreEmulation
//
//  Created by kstanger on 8/28/13.
//

#ifndef _JavaLangIterable_H_
#define _JavaLangIterable_H_

@protocol JavaUtilIterator;

#import "JreEmulation.h"

@protocol JavaLangIterable < NSObject, JavaObject, NSFastEnumeration >
- (id<JavaUtilIterator>)iterator;
@end

FOUNDATION_EXPORT NSUInteger JreDefaultFastEnumeration(
    id<JavaLangIterable> obj, NSFastEnumerationState *state, id __unsafe_unretained *stackbuf,
    NSUInteger len);

#endif // _JavaLangIterable_H_
