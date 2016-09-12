/*
 * Copyright (C) 2011 The Android Open Source Project
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

package java.lang;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.LOCAL_VARIABLE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.CLASS;
import java.lang.annotation.Target;

/**
 * Suppress FindBugs warnings on the annotated element. FindBugs will recognize
 * any annotation that has class retention and whose name ends with
 * "SuppressWarnings".
 *
 * @hide
 */
@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE})
@Retention(CLASS)
public @interface FindBugsSuppressWarnings {

    /**
     * The <a href="http://findbugs.sourceforge.net/bugDescriptions.html">FindBugs
     * Patterns</a> to suppress, such as {@code SE_TRANSIENT_FIELD_NOT_RESTORED}
     * or {@code Se}. Full, upper case names are preferred.
     */
    String[] value();
}
