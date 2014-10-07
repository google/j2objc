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
 * An {@code IllegalFormatWidthException} will be thrown if the width is a
 * negative value other than -1 or in other cases where a width is not
 * supported.
 *
 * @see java.lang.RuntimeException
 */
public class IllegalFormatWidthException extends IllegalFormatException {

    private static final long serialVersionUID = 16660902L;

    private final int w;

    /**
     * Constructs a new {@code IllegalFormatWidthException} with specified
     * width.
     *
     * @param w
     *           the width.
     */
    public IllegalFormatWidthException(int w) {
        this.w = w;
    }

    /**
     * Returns the width associated with the exception.
     *
     * @return the width.
     */
    public int getWidth() {
        return w;
    }

    @Override
    public String getMessage() {
        return Integer.toString(w);
    }
}
