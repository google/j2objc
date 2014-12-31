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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For Java member variables, annotation adds property declarations to generated Objective-C.
 * Be careful as this could change the functionality of the translated code. It's up to you to
 * maintain similar access controls on members.
 *
 * See <a href="https://developer.apple.com/library/mac/documentation/Cocoa/Conceptual/ProgrammingWithObjectiveC/EncapsulatingData/EncapsulatingData.html">Apple's @property documention</a>.
 *
 * Note:
 *  - Invalid attributes will cause an AssertionError.
 *  - <b>readwrite</b>, <b>strong</b>, and <b>atomic</b> attributes will be removed as they are defaults.
 *  - Strings will add the <b>copy</b> attribute.
 *
 * Example:
 *
 * class Foo {
 *   @Property("copy, nonatomic") protected String bar;
 * }
 *
 * generates:
 *
 * @property (copy, nonatomic) NSString *bar;
 *
 * @author Harry Cheung
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Property {
  String value() default "";
}
