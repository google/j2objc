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

import java.io.Serializable;

/**
 * A {@code FormatFlagsConversionMismatchException} will be thrown if a
 * conversion and the flags are incompatible.
 *
 * @see java.lang.RuntimeException
 */
public class FormatFlagsConversionMismatchException extends
        IllegalFormatException implements Serializable {

    private static final long serialVersionUID = 19120414L;

    private final String f;

    private final char c;

    /**
     * Constructs a new {@code FormatFlagsConversionMismatchException} with the
     * flags and conversion specified.
     *
     * @param f
     *           the flags.
     * @param c
     *           the conversion.
     */
    public FormatFlagsConversionMismatchException(String f, char c) {
        if (f == null) {
            throw new NullPointerException("f == null");
        }
        this.f = f;
        this.c = c;
    }

    /**
     * Returns the incompatible format flag.
     *
     * @return the incompatible format flag.
     */
    public String getFlags() {
        return f;
    }

    /**
     * Returns the incompatible conversion.
     *
     * @return the incompatible conversion.
     */
    public char getConversion() {
        return c;
    }

    @Override
    public String getMessage() {
        return "%" + c + " does not support '" + f + "'";
    }
}
