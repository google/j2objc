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

package javax.annotation.processing;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Generated annotation is used to mark source code that has been generated. It can also be used
 * to differentiate user written code from generated code in a single file.
 *
 * <h3>Examples:</h3>
 *
 * <pre>
 *   &#064;Generated("com.example.Generator")
 * </pre>
 *
 * <pre>
 *   &#064;Generated(value="com.example.Generator", date= "2017-07-04T12:08:56.235-0700")
 * </pre>
 *
 * <pre>
 *   &#064;Generated(value="com.example.Generator", date= "2017-07-04T12:08:56.235-0700",
 *      comments= "comment 1")
 * </pre>
 *
 * @since 9
 */
@Documented
@Retention(value = RetentionPolicy.SOURCE)
@Target(
    value = {
      ElementType.PACKAGE,
      ElementType.TYPE,
      ElementType.METHOD,
      ElementType.CONSTRUCTOR,
      ElementType.FIELD,
      ElementType.LOCAL_VARIABLE,
      ElementType.PARAMETER
    })
public @interface Generated {

  /**
   * The value element MUST have the name of the code generator. The name is the fully qualified
   * name of the code generator.
   *
   * @return The name of the code generator
   */
  String[] value();

  /**
   * Date when the source was generated. The date element must follow the ISO 8601 standard. For
   * example the date element would have the following value 2017-07-04T12:08:56.235-0700 which
   * represents 2017-07-04 12:08:56 local time in the U.S. Pacific Time time zone.
   *
   * @return The date the source was generated
   */
  String comments() default "";

  /**
   * A place holder for any comments that the code generator may want to include in the generated
   * code.
   *
   * @return Comments that the code generated included
   */
  String date() default "";
}
