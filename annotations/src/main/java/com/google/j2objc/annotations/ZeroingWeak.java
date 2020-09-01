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
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation that indicates a variable has a weak relationship to its owner. The variable will be
 * annotated with "weak" in ARC, and converted to WeakReference in manual reference counting.
 *
 * <p>Because reading from such variables may give null, this annotation must be used in combination
 * with @Nullable annotation, and can not be used in combination with @NonNull.
 *
 * @author Michał Pociecha-Łoś
 */
@Target(FIELD)
@Retention(CLASS)
public @interface ZeroingWeak {}
