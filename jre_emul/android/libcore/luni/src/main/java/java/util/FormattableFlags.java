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
package java.util;

/**
 * FormattableFlags are used as a parameter to
 * {@link Formattable#formatTo(Formatter, int, int, int)} and change the output
 * format in {@code Formattable}s. The validation and interpretation of the
 * flags must be done by the implementations.
 *
 * @see Formattable
 */
public class FormattableFlags {

    private FormattableFlags(){
        //prevent this class from being instantiated
    }

    /**
     * Denotes the output is to be left-justified. In order to fill the minimum
     * width requirement, spaces('\u0020') will be appended at the end of the
     * specified output element. If no such flag is set, the output is
     * right-justified.
     *
     * The flag corresponds to '-' ('\u002d') in the format specifier.
     */
    public static final int LEFT_JUSTIFY = 1;

    /**
     * Denotes the output is to be converted to upper case in the way the locale
     * parameter of Formatter.formatTo() requires. The output has the same
     * effect as {@code String.toUpperCase(java.util.Locale)}.
     *
     * This flag corresponds to {@code '^' ('\u005e')} in the format specifier.
     */
    public static final int UPPERCASE = 2;

    /**
     * Denotes the output is to be formatted in an alternate form. The definition
     * of the alternate form is determined by the {@code Formattable}.
     *
     * This flag corresponds to {@code '#' ('\u0023')} in the format specifier.
     */
    public static final int ALTERNATE = 4;
}
