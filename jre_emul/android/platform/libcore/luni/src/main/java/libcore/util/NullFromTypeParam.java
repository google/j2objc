/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package libcore.util;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
/**
 * Annotation for generic types in methods, denotes that a type nullability is
 * deliberately left floating; the nullability is the same as the
 * actual type parameter of the class.
 */
@Documented
@Retention(SOURCE)
@Target({TYPE_USE})
public @interface NullFromTypeParam {
   /**
    * Min Android API level (inclusive) to which this annotation is applied.
    */
   int from() default Integer.MIN_VALUE;
   /**
    * Max Android API level to which this annotation is applied.
    */
   int to() default Integer.MAX_VALUE;
}
