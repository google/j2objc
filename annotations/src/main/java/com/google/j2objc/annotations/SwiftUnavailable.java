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
 * Marks a declaration as not suitable for Swift importing. This
 * adds an <code>NS_SWIFT_UNAVAILABLE</code> annotation, as described:
 * https://developer.apple.com/documentation/swift/objective-c_and_c_code_customization/making_objective-c_apis_unavailable_in_swift
 */
@Documented
@Target({
    ElementType.CONSTRUCTOR,
    ElementType.FIELD,
    ElementType.METHOD,
    ElementType.TYPE
})
@Retention(RetentionPolicy.SOURCE)
public @interface SwiftUnavailable {

  String value() default "unavailable";

}
