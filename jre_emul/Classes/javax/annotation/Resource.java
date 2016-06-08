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

package javax.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * javax.annotation.Resource annotation. No code was referenced,
 * created using just its public API.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {
    ElementType.TYPE,
    ElementType.METHOD,
    ElementType.FIELD })
public @interface Resource {

  public static enum AuthenticationType {
    APPLICATION, CONTAINER;
  }

  // Optional elements.
  AuthenticationType authenticationType() default AuthenticationType.CONTAINER;
  String description() default "";
  String mappedName() default "";
  String name() default "";
  boolean shareable() default true;
  Class type() default Object.class;
}
