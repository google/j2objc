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
 * An {@code IllegalFormatFlagsException} will be thrown if the combination of
 * the format flags is illegal.
 *
 * @see java.lang.RuntimeException
 */
public class IllegalFormatFlagsException extends IllegalFormatException implements Serializable {
    private static final long serialVersionUID = 790824L;

    private final String flags;

    /**
     * Constructs a new {@code IllegalFormatFlagsException} with the specified
     * flags.
     *
     * @param flags
     *           the specified flags.
     */
    public IllegalFormatFlagsException(String flags) {
        if (flags == null) {
            throw new NullPointerException("flags == null");
        }
        this.flags = flags;
    }

    /**
     * Returns the flags that are illegal.
     *
     * @return the flags that are illegal.
     */
    public String getFlags() {
        return flags;
    }

    @Override
    public String getMessage() {
        return flags;
    }
}
