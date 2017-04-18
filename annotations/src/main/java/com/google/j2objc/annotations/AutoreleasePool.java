/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
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

package com.google.j2objc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that indicates the translator should inject an autorelease pool
 * around the method body. Only valid on methods that don't return anything.
 *
 * <p>Useful in high-level contexts to ensure that temporary objects allocated within the method or
 * loop are deallocated.
 *
 * <p>Example usage:
 * <pre>
 * // Temporary objects allocated during execution of this method will
 * // be deallocated upon returning from this method.
 * &#64;AutoreleasePool
 * public void doWork() {
 *   ...
 * }
 *
 * public void doWork(Iterable&lt;Runnable&gt; workToDo) {
 *   // Adding @AutoreleasePool on the loop variable causes a separate
 *   // autorelease pool to be attached to each loop iteration, clearing
 *   // up temporary objects after each iteration
 *   for (@AutoreleasePool Runnable item : workToDo) {
 *     item.run();
 *   }
 * }
 * </pre>
 *
 * @author Pankaj Kakkar
 */
@Target({ElementType.METHOD, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.SOURCE)
public @interface AutoreleasePool {
}
