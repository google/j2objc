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
 * Classes that handle custom formatting for the 's' specifier of {@code Formatter}
 * should implement the {@code Formattable} interface. It gives basic control over
 * formatting objects.
 *
 * @see Formatter
 */

public interface Formattable {

    /**
     * Formats the object using the specified {@code Formatter}.
     *
     * @param formatter
     *            the {@code Formatter} to use.
     * @param flags
     *            the flags applied to the output format, which is a bitmask
     *            that is any combination of {@code FormattableFlags.LEFT_JUSTIFY},
     *            {@code FormattableFlags.UPPERCASE}, and {@code FormattableFlags.ALTERNATE}. If
     *            no such flag is set, the output is formatted by the default
     *            formatting of the implementation.
     * @param width
     *            the minimum number of characters that should be written to the
     *            output. If the length of the converted value is less than {@code width}
     *            Additional space characters (' ') are added to the output if the
     *            as needed to make up the difference. These spaces are added at the
     *            beginning by default unless the flag
     *            FormattableFlags.LEFT_JUSTIFY is set, which denotes that
     *            padding should be added at the end. If width is -1, then
     *            minimum length is not enforced.
     * @param precision
     *            the maximum number of characters that can be written to the
     *            output. The length of the output is trimmed down to this size
     *            before the width padding is applied. If the precision
     *            is -1, then maximum length is not enforced.
     * @throws IllegalFormatException
     *             if any of the parameters is not supported.
     */
    void formatTo(Formatter formatter, int flags, int width, int precision)
            throws IllegalFormatException;
}
