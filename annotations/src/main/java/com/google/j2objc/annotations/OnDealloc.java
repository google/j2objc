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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotates a method which will be called from "- (void)dealloc" method in the transpiled ObjC
 * code.
 *
 * <p>Annotated method must be private, non-static, have no parameters, return void and there can be
 * at most one such method.
 *
 * <p>This annotation is designed to be used in combination with @Weak (which translates to
 * __unsafe_unretained). It can be used to nullify @Weak references or cleaning internal data
 * structures containing @Weak references, to avoid dangling pointers in transpiled ObjC code.
 *
 * @author Michał Pociecha-Łoś
 */
@Target(METHOD)
@Retention(CLASS)
public @interface OnDealloc {}
