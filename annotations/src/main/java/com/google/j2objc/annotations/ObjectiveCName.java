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
 * Annotation that specifies what the Objective-C class, protocol, method,
 * constructor or package declaration should be when translated.
 *
 * <p>For packages add the annotation to the package declaration in a package-info.java file to
 * specify the desired package prefix. Alternatively, package prefixes may be specified using the
 * {@code --prefix} or {@code --prefixes} flags when invoking j2objc.
 *
 * <p>For classes specify the desired Objective-C class name for the translated type.
 *
 * <p>For methods specify the desired Objective-C selector for the translated method:
 * <pre>
 * &#64;ObjectiveCName("setDateWithYear:month:day:")
 * public void setDate(int year, int month, int day);</pre>
 *
 * @author Tom Ball
 */
@Documented
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PACKAGE })
@Retention(RetentionPolicy.CLASS)
public @interface ObjectiveCName {

  /**
   * The Objective-C name to use.
   */
  String value();
}
