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
 * Adds property declarations to generated Objective-C for annotated fields.
 *
 * <p>Can be used on a class, causing all methods without parameters to be converted to properties.
 *
 * <p>See <a
 * href="https://developer.apple.com/library/mac/documentation/Cocoa/Conceptual/ProgrammingWithObjectiveC/EncapsulatingData/EncapsulatingData.html"
 * >Apple's &#64;property documentation</a>.
 *
 * <p>Notes:
 *
 * <ul>
 *   <li>Invalid attributes are reported as errors.
 *   <li><b>readwrite</b>, <b>strong</b> (when using ARC), and <b>atomic</b> attributes are removed
 *       since they are defaults.
 *   <li>Strings will include the <b>copy</b> attribute.
 * </ul>
 *
 * Example:
 *
 * <pre>
 * class Foo {
 *   &#64;Property("copy, nonatomic") protected String bar;
 * }</pre>
 *
 * generates:
 *
 * <pre>
 * &#64;property (copy, nonatomic) NSString *bar;
 * </pre>
 *
 * Class Example:
 *
 * <pre>
 * &#64;Property
 * class Foo {
 *   public String getBar();
 *   public String foo();
 * }</pre>
 *
 * generates:
 *
 * <pre>
 * &#64;property (copy, nonatomic, getter=getBar, readonly) NSString *bar;
 * &#64;property (copy, nonatomic, getter=foo, readonly) NSString *foo;
 * </pre>
 *
 * @author Harry Cheung
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface Property {

  /**
   * Prevents a method from being converted to a property when its class is annotated with
   * `@Property`.
   */
  @Target({ElementType.METHOD})
  public @interface Suppress {

    String reason() default "";
  }

  String value() default "";
}
