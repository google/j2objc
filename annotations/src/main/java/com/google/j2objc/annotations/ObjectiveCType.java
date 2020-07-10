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

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that specifies what the field or method return type should be when translated.
 *
 * &#64;ObjectiveCName("void *")
 * public Object lock;</pre>
 */
@Target({FIELD, LOCAL_VARIABLE, PARAMETER, METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface ObjectiveCType {

  /**
   * The Objective-C type to use for this element.
   *
   * @return the Objective-C type.
   */
  String value();
}
