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

#import "java/lang/Iterable.h"

NSUInteger JreDefaultFastEnumeration(
    __unsafe_unretained id<JavaLangIterable> obj, NSFastEnumerationState *state,
    __unsafe_unretained id *stackbuf, NSUInteger len) {
  SEL hasNextSel = @selector(hasNext);
  SEL nextSel = @selector(next);
  __unsafe_unretained id iter = (ARCBRIDGE id) (void *) state->extra[0];
  if (!iter) {
    state->mutationsPtr = (unsigned long *) (ARCBRIDGE void *) obj;
    // The for/in loop could break early so we have no guarantee of being able
    // to release the iterator. As long as the current autorelease pool is not
    // cleared within the loop, this should be fine.
    iter = [obj iterator];
    state->extra[0] = (unsigned long) iter;
    state->extra[1] = (unsigned long) [iter methodForSelector:hasNextSel];
    state->extra[2] = (unsigned long) [iter methodForSelector:nextSel];
  }
  IMP hasNextImpl = (IMP) state->extra[1];
  IMP nextImpl = (IMP) state->extra[2];
  NSUInteger objCount = 0;
  state->itemsPtr = stackbuf;
  while (hasNextImpl(iter, hasNextSel) && objCount < len) {
    *stackbuf++ = nextImpl(iter, nextSel);
    objCount++;
  }
  return objCount;
}
