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

import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation that indicates an inner class has a weak relationship to its owning class.
 *
 * Lambdas can be given a weak outer reference by declaring a local variable with this annotation
 * and assigning the lambda expression directly to the local variable. WeakOuter is only allowed on
 * local variables, not fields, to encourage the annotation to be used where the lambda is declared.
 *
 * @author Tom Ball
 */
@Target({TYPE, LOCAL_VARIABLE})
@Retention(SOURCE)
public @interface WeakOuter {
}
