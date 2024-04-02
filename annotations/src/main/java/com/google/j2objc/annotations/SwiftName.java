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
 * Annotation that specifies what the Swift class, protocol, method, constructor or package
 * declaration should be when translated.
 *
 * <p>When the flag is applied to the package or type level it will automatically apply names to
 * methods.
 *
 * <p>For packages add the annotation to the package declaration in a package-info.java file to
 * apply Swift annotations throughout the package.
 *
 * <p>For classes specify the desired Swift class name for the translated type or nothing to use the
 * java name.
 *
 * <p>For methods specify the desired Swift selector for the translated method. The Swift method
 * signature is derived based on some patterns, and it is not converted automatically when it is not
 * recognized.
 *
 * <pre>
 * &#64;SwiftName("setDate(year:month:day:)")
 * public void setDate(int year, int month, int day);</pre>
 *
 * @author Justin Anderson
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PACKAGE})
@Retention(RetentionPolicy.CLASS)
public @interface SwiftName {

  /**
   * The Swift name to use for this element.
   *
   * @return the Swift name.
   */
  String value() default "";
}
