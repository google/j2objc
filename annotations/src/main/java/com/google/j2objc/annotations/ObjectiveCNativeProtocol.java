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

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation applicable to classes and interfaces that adds a Objective-C protocol and optional
 * associated header to the declaration. This allows Java classes to conform to Objective-C native
 * protocols.
 */
@Target({TYPE})
@Retention(RetentionPolicy.CLASS)
@Repeatable(ObjectiveCNativeProtocols.class)
public @interface ObjectiveCNativeProtocol {

  /** The name of the Objective-C protocol to conform to, for example "NSCoding". */
  String name() default "";

  /**
   * The header to include for the protocol definition. Leave blank for protocols declared by system
   * headers.
   */
  String header() default "";
}
