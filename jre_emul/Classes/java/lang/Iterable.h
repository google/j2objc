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

// Copy of generated header of Iterable.java.
// TODO(tball): remove when apps no longer reference j2objc/jre_emul/Classes/java/lang/Iterable.h.
// NOLINTBEGIN
#include "J2ObjC_header.h"

#pragma push_macro("INCLUDE_ALL_JavaLangIterable")
#ifdef RESTRICT_JavaLangIterable
#define INCLUDE_ALL_JavaLangIterable 0
#else
#define INCLUDE_ALL_JavaLangIterable 1
#endif
#undef RESTRICT_JavaLangIterable

#if !defined (JavaLangIterable_) && (INCLUDE_ALL_JavaLangIterable || defined(INCLUDE_JavaLangIterable))
#define JavaLangIterable_

@protocol JavaUtilFunctionConsumer;
@protocol JavaUtilIterator;
@protocol JavaUtilSpliterator;

@protocol JavaLangIterable < NSFastEnumeration, JavaObject >

- (id<JavaUtilIterator>)iterator;

- (void)forEachWithJavaUtilFunctionConsumer:(id<JavaUtilFunctionConsumer>)action;

- (id<JavaUtilSpliterator>)spliterator;

@end

J2OBJC_EMPTY_STATIC_INIT(JavaLangIterable)

FOUNDATION_EXPORT void JavaLangIterable_forEachWithJavaUtilFunctionConsumer_(id<JavaLangIterable> self, id<JavaUtilFunctionConsumer> action);

FOUNDATION_EXPORT id<JavaUtilSpliterator> JavaLangIterable_spliterator(id<JavaLangIterable> self);

J2OBJC_TYPE_LITERAL_HEADER(JavaLangIterable)

#endif

#pragma pop_macro("INCLUDE_ALL_JavaLangIterable")
// NOLINTEND
