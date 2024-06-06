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
 * Annotation applicable to methods that generates a peer "adapter" method in the transpiled class
 * or interface. Adapter methods can provide one or more "adaptations" to the generated method,
 * which change the types and behavior of the annotated ("adapted") method.
 *
 * <p>Adapter methods are intended to reduce friction between native and J2ObjC interfaces, and
 * improve type information and safety. For example, using the EXCEPTIONS_AS_ERRORS adaptation
 * allows Java methods that throw exceptions to be safely called from Objective-C and Swift without
 * needing to catch exceptions in Objective-C (exceptions cannot be caught in Swift).
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface ObjectiveCAdapterMethod {
  /** Specific adaptations applied by the annotation. */
  public enum Adaptation {
    /**
     * Catch Java exceptions inside the adapter method and convert those exceptions to simple
     * NSErrors (see JreExceptionAdapters). The supplied selector must have an "...error:" or
     * "...WithError:" final argument following Objective-C naming conventions. If the original
     * method returns void, the adapter method will return a BOOL success value.
     */
    EXCEPTIONS_AS_ERRORS,

    /**
     * Java returned booleans are converted to Objective C BOOL types (including handling
     * OBJC_BOOL_IS_BOOL differences).
     */
    RETURN_NATIVE_BOOLS,

    /**
     * Java boolean arguments are converted to Objective C BOOL types (including handling
     * OBJC_BOOL_IS_BOOL differences).
     */
    ACCEPT_NATIVE_BOOLS,

    /**
     * Java enumeration return values are converted to the NS_ENUM equivalent. This occurs at the
     * outer-level only, Java enums within other types (container types, for example) are not
     * supported.
     */
    RETURN_NATIVE_ENUMS,

    /**
     * Java enumeration argument values are converted from their NS_ENUM equivalent. This occurs at
     * the outer-level only, Java enums within other types (container types, for example) are not
     * supported.
     */
    ACCEPT_NATIVE_ENUMS,

    /**
     * Java return values annotated with ObjectiveCAdapterProtocol are replaced with the protocol
     * specified by ObjectiveCAdapterProtocol. This substitution occurs even within type parameters
     * of the return value.
     */
    RETURN_ADAPTER_PROTOCOLS,

    /**
     * Java return values of java.util.List are returned as NSArrays. The resulting array follows
     * Objective C expectations, the array is immutable, but the elements of the array may be
     * mutable (not a deep copy). Array content type information (generics) is maintained.
     */
    RETURN_LISTS_AS_NATIVE_ARRAYS,
  }

  /**
   * The Objective-C selector to use for the adapter method. Has the same naming requirements as the
   * ObjectiveCName annotation for methods. This field is required.
   */
  String selector() default "";

  /** List of adaptations the adapter method will use. */
  Adaptation[] adaptations() default {};
}
