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

#include "J2ObjC_header.h"

@protocol JavaUtilFunctionConsumer;
@protocol JavaUtilIterator;
@protocol JavaUtilSpliterator;

/**
 * Instances of classes that implement this interface can be used with
 * the enhanced for loop.
 */
@protocol JavaLangIterable <NSFastEnumeration, JavaObject>

- (id<JavaUtilIterator>)iterator;

- (void)forEachWithJavaUtilFunctionConsumer:(id<JavaUtilFunctionConsumer>)action;

- (id<JavaUtilSpliterator>)spliterator;

@end

@interface JavaLangIterable : NSObject < JavaLangIterable >
@end

J2OBJC_EMPTY_STATIC_INIT(JavaLangIterable)

J2OBJC_TYPE_LITERAL_HEADER(JavaLangIterable)

FOUNDATION_EXPORT NSUInteger JreDefaultFastEnumeration(
    id<JavaLangIterable> obj, NSFastEnumerationState *state, id __unsafe_unretained *stackbuf,
    NSUInteger len);

FOUNDATION_EXPORT void JavaLangIterable_forEachWithJavaUtilFunctionConsumer_(id<JavaLangIterable> self, id<JavaUtilFunctionConsumer> action);

FOUNDATION_EXPORT id<JavaUtilSpliterator> JavaLangIterable_spliterator(id<JavaLangIterable> self);

#endif // _JavaLangIterable_H_
