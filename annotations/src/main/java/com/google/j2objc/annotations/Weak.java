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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation that indicates a variable has a weak relationship to its owner. The variable will be
 * declared with the __weak annotation if compiled with either ARC ("-fobjc-arc" clang flag) or the
 * "-fobjc-weak" clang flag (set by j2objc script).
 *
 * <p>If compiled with neither clang flag, the variable will be declared with the
 * __unsafe_unretained annotation. However, it's recommended that either ARC compilation or using
 * the "-fobjc-weak" flag be used.
 *
 * @author Tom Ball
 */
@Target({FIELD, LOCAL_VARIABLE, PARAMETER})
@Retention(CLASS)
public @interface Weak {}
