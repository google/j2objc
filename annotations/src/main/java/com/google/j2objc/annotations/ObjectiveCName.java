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
 * Annotation that specifies what the Objective-C class, protocol, method,
 * constructor or package declaration should be when translated.
 *
 * Though this interface is marked with {@link RetentionPolicy#RUNTIME},
 * it will not be emitted in transpiled output from the J2ObjC transpiler.
 * This is the only annotation ignored in this way.
 *
 * @author Tom Ball
 */
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PACKAGE })
@Retention(RetentionPolicy.CLASS)
public @interface ObjectiveCName {

  /**
   * The Objective-C name to use.
   */
  String value();
}
