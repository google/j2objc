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

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation applicable to enums to control the name of the generated Objective-C native enum
 * (NS_ENUM). This annotation only affects the native enum, to control the name of the transpiled
 * enum class use the ObjectiveCName annotation.
 */
@Target({TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface ObjectiveCNativeEnumName {

  /** The name to apply to the native NSEnum. */
  String value() default "";
}
