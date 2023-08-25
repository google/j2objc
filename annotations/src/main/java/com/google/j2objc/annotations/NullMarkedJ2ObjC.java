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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation which allows @NullMarked Java source code to generate Objective-C code with
 * nullability support using audited nullability regions:
 * `NS_ASSUME_NONNULL_BEGIN`/`NS_ASSUME_NONNULL_END`. Java objects annotated as @Nullable will be
 * annotated as nullable in generated Objective-C code.
 *
 * <p>This annotation must be present in package files to enable nullness mapping from
 * Java @NullMarked to Objective-C nullability.
 *
 * <p>Note: @NullMarked is supported in J2ObjC for the following `ElementType`s: `TYPE`, `METHOD`,
 * `CONSTRUCTOR`, and `PACKAGE`. `MODULE` is not supported in J2ObjC even though it's a supported
 * target in @NullMarked.
 *
 * <p>This annotation can only be specified on packages.
 */
@Documented
@Target({ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NullMarkedJ2ObjC {}
