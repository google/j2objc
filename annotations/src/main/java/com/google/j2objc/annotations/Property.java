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
 * Adds property declarations to generated Objective-C for annotated fields.
 *
 * See <a
 * href="https://developer.apple.com/library/mac/documentation/Cocoa/Conceptual/ProgrammingWithObjectiveC/EncapsulatingData/EncapsulatingData.html"
 * >Apple's &#64;property documentation</a>.
 * <p>
 * Notes:
 * <ul>
 *   <li>Invalid attributes are reported as errors.</li>
 *   <li><b>readwrite</b>, <b>strong</b>, and <b>atomic</b> attributes are removed
 *     since they are defaults.</li>
 *   <li>Strings will include the <b>copy</b> attribute.</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 * class Foo {
 *   &#64;Property("copy, nonatomic") protected String bar;
 * }</pre>
 * generates:
 * <p>
 * <pre>
 * &#64;property (copy, nonatomic) NSString *bar;</pre>
 *
 * @author Harry Cheung
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface Property {
  String value() default "";
}