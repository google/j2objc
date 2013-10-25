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
 * An {@code IllegalFormatPrecisionException} will be thrown if the precision is
 * a negative other than -1 or in other cases where precision is not supported.
 *
 * @see java.lang.RuntimeException
 */

public class IllegalFormatPrecisionException extends IllegalFormatException {
    private static final long serialVersionUID = 18711008L;

    private final int p;

    /**
     * Constructs a new {@code IllegalFormatPrecisionException} with specified
     * precision.
     *
     * @param p
     *           the precision.
     */
    public IllegalFormatPrecisionException(int p) {
        this.p = p;
    }

    /**
     * Returns the precision associated with the exception.
     *
     * @return the precision.
     */
    public int getPrecision() {
        return p;
    }

    @Override
    public String getMessage() {
        return Integer.toString(p);
    }
}
