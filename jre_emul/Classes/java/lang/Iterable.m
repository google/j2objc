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

#include "java/lang/Iterable.h"

#include "J2ObjC_source.h"
#include "java/util/Iterator.h"
#include "java/util/Iterator.h"
#include "java/util/Objects.h"
#include "java/util/Spliterator.h"
#include "java/util/Spliterators.h"
#include "java/util/function/Consumer.h"

#pragma clang diagnostic ignored "-Wprotocol"

@implementation JavaLangIterable

- (void)forEachWithJavaUtilFunctionConsumer:(id<JavaUtilFunctionConsumer>)action {
  JavaLangIterable_forEachWithJavaUtilFunctionConsumer_(self, action);
}

- (id<JavaUtilSpliterator>)spliterator {
  return JavaLangIterable_spliterator(self);
}

+ (const J2ObjcClassInfo *)__metadata {
  static J2ObjcMethodInfo methods[] = {
    { NULL, "LJavaUtilIterator;", 0x401, -1, -1, -1, 0, -1, -1 },
    { NULL, "V", 0x1, 1, 2, -1, 3, -1, -1 },
    { NULL, "LJavaUtilSpliterator;", 0x1, -1, -1, -1, 4, -1, -1 },
  };
  #pragma clang diagnostic push
  #pragma clang diagnostic ignored "-Wobjc-multiple-method-names"
  methods[0].selector = @selector(iterator);
  methods[1].selector = @selector(forEachWithJavaUtilFunctionConsumer:);
  methods[2].selector = @selector(spliterator);
  #pragma clang diagnostic pop
  static const void *ptrTable[] = {
    "()Ljava/util/Iterator<TT;>;", "forEach", "LJavaUtilFunctionConsumer;",
    "(Ljava/util/function/Consumer<-TT;>;)V", "()Ljava/util/Spliterator<TT;>;",
    "<T:Ljava/lang/Object;>Ljava/lang/Object;" };
  static const J2ObjcClassInfo _JavaLangIterable = {
    "Iterable", "java.lang", ptrTable, methods, NULL, 7, 0x609, 3, 0, -1, -1, -1, 5, -1 };
  return &_JavaLangIterable;
}

@end

void JavaLangIterable_forEachWithJavaUtilFunctionConsumer_(id<JavaLangIterable> self, id<JavaUtilFunctionConsumer> action) {
  JavaUtilObjects_requireNonNullWithId_(action);
  for (id __strong t in self) {
    [((id<JavaUtilFunctionConsumer>) nil_chk(action)) acceptWithId:t];
  }
}

id<JavaUtilSpliterator> JavaLangIterable_spliterator(id<JavaLangIterable> self) {
  return JavaUtilSpliterators_spliteratorUnknownSizeWithJavaUtilIterator_withInt_([self iterator], 0);
}

J2OBJC_INTERFACE_TYPE_LITERAL_SOURCE(JavaLangIterable)

NSUInteger JreDefaultFastEnumeration(
    __unsafe_unretained id<JavaLangIterable> obj, NSFastEnumerationState *state,
    __unsafe_unretained id *stackbuf, NSUInteger len) {
  SEL hasNextSel = @selector(hasNext);
  SEL nextSel = @selector(next);
  __unsafe_unretained id iter = (ARCBRIDGE id) (void *) state->extra[0];
  if (!iter) {
    static unsigned long no_mutation = 1;
    state->mutationsPtr = &no_mutation;
    // The for/in loop could break early so we have no guarantee of being able
    // to release the iterator. As long as the current autorelease pool is not
    // cleared within the loop, this should be fine.
    iter = nil_chk([obj iterator]);
    state->extra[0] = (unsigned long) iter;
    state->extra[1] = (unsigned long) [iter methodForSelector:hasNextSel];
    state->extra[2] = (unsigned long) [iter methodForSelector:nextSel];
  }
  jboolean (*hasNextImpl)(id, SEL) = (jboolean (*)(id, SEL)) state->extra[1];
  id (*nextImpl)(id, SEL) = (id (*)(id, SEL)) state->extra[2];
  NSUInteger objCount = 0;
  state->itemsPtr = stackbuf;
  while (hasNextImpl(iter, hasNextSel) && objCount < len) {
    *stackbuf++ = nextImpl(iter, nextSel);
    objCount++;
  }
  return objCount;
}
