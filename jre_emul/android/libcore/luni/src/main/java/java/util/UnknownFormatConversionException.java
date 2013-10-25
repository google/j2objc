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
 * An {@code UnknownFormatConversionException} will be thrown if the format
 * conversion is unknown.
 *
 * @see java.lang.RuntimeException
 */
public class UnknownFormatConversionException extends IllegalFormatException {
    private static final long serialVersionUID = 19060418L;

    private final String s;

    /**
     * Constructs an {@code UnknownFormatConversionException} with the unknown
     * format conversion.
     *
     * @param s
     *           the unknown format conversion.
     */
    public UnknownFormatConversionException(String s) {
        if (s == null) {
            throw new NullPointerException("s == null");
        }
        this.s = s;
    }

    /**
     * Returns the conversion associated with the exception.
     *
     * @return the conversion associated with the exception.
     */
    public String getConversion() {
        return s;
    }

    @Override
    public String getMessage() {
        return "Conversion: " + s;
    }
}
