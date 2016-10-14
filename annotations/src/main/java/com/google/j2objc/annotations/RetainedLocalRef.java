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

package com.google.j2objc.annotations;

import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation that indicates a local variable should be retained outside of any
 * subsequent AutoreleasePool use. Otherwise, if a local variable has a copy
 * of an object in a container that is removed in an AutoreleasePool, it will
 * be deallocated before the local variable goes out of scope.
 * <p>
 * For example, a ThreadPoolExecutor is used to process a list of tasks,
 * removing each task from the list as it is processed. ThreadPoolExecutor
 * tasks are run inside of an AutoreleasePool, since these executors are often
 * long-lived. If a local variable is initialized to one of the task list's
 * elements, by default that variable won't be valid (will be deallocated)
 * after task processing. Adding a LocalRetain annotation to the local
 * variable ensures it is still valid after task processing.
 *
 * @author Tom Ball
 */
@Target(LOCAL_VARIABLE)
@Retention(SOURCE)
public @interface RetainedLocalRef {
}
