/* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation type used to indicate that the compiler should not issue the
 * specified warnings for the marked program element. Warnings are not only
 * suppressed for the annotated element but also for all program elements
 * contained in that element.
 * <p>
 * It is recommended that programmers always use this annotation on the most
 * deeply nested element where it is actually needed.
 *
 * @since 1.5
 */
@Target( { ElementType.TYPE, ElementType.FIELD, ElementType.METHOD,
        ElementType.PARAMETER, ElementType.CONSTRUCTOR,
        ElementType.LOCAL_VARIABLE })
@Retention(RetentionPolicy.SOURCE)
public @interface SuppressWarnings {

    /**
     * The list of warnings a compiler should not issue.
     */
    public String[] value();
}
