/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.j2objc.kmp;

import com.google.j2objc.annotations.ObjectiveCName;
import java.util.List;

/*-[
#include "java/lang/Integer.h"
#include "java/util/ArrayList.h"
#include "java/util/List.h"
]-*/

/** Symmetric test adapter converting Java {@link List} and {@link Integer} values to Foundation. */
@ObjectiveCName("KmpTestAdapter")
public final class KmpTestAdapter {

  private KmpTestAdapter() {}

  /** Converts a Java {@link List} to an {@code NSArray}. */
  public static native Object fromJavaUtilList(List<?> list) /*-[
    if (!list) {
      return nil;
    }
    NSMutableArray *array = [NSMutableArray arrayWithCapacity:(NSUInteger) [list size]];
    for (id item in list) {
      [array addObject:item];
    }
    return [NSArray arrayWithArray:array];
  ]-*/;

  /** Converts an {@code NSArray} to a Java {@link List}. */
  public static native <T> List<T> toJavaUtilList(Object list) /*-[
    if (!list) {
      return nil;
    }
    NSArray *array = (NSArray *) list;
    JavaUtilArrayList *result = create_JavaUtilArrayList_initWithInt_((jint) [array count]);
    for (id item in array) {
      [result addWithId:item];
    }
    return result;
  ]-*/;

  /** Converts a boxed {@link Integer} to an {@code NSNumber}. */
  public static native Object fromInteger(Integer value) /*-[
    return value ? [NSNumber numberWithInt:[value intValue]] : nil;
  ]-*/;

  /** Converts an {@code NSNumber} to a boxed {@link Integer}. */
  public static native Integer toInteger(Object value) /*-[
    return value ? JavaLangInteger_valueOfWithInt_([(NSNumber *) value intValue]) : nil;
  ]-*/;
}
