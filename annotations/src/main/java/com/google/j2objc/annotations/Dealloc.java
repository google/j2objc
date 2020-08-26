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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotates a method which will be called from "- (void)dealloc" method in the transpiled ObjC
 * code.
 *
 * <p>Annotated method must be private, non-static, return void and have no parameters. If there are
 * multiple annotated methods, they will be called in declaration order, before calling finalize().
 *
 * <p>This annotation is designed to be used in combination with @Weak (which translates to
 * unsafe_unretained) to break retain-cycles and make sure that there are no dangling pointers in
 * the transpiled ObjC code. It may involve nullifying @Weak references or removing them from
 * internal data structures.
 *
 * @author Michał Pociecha-Łoś
 */
@Target(METHOD)
@Retention(CLASS)
public @interface Dealloc {}
