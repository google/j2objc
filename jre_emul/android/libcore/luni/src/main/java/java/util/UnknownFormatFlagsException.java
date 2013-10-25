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
 * An {@code UnknownFormatFlagsException} will be thrown if there is
 * an unknown flag.
 *
 * @see java.lang.RuntimeException
 */
public class UnknownFormatFlagsException extends IllegalFormatException {

    private static final long serialVersionUID = 19370506L;

    private final String flags;

    /**
     * Constructs a new {@code UnknownFormatFlagsException} with the specified
     * flags.
     *
     * @param f
     *           the specified flags.
     */
    public UnknownFormatFlagsException(String f) {
        if (f == null) {
            throw new NullPointerException("f == null");
        }
        flags = f;
    }

    /**
     * Returns the flags associated with the exception.
     *
     * @return the flags associated with the exception.
     */
    public String getFlags() {
        return flags;
    }

    @Override
    public String getMessage() {
        return "Flags: " + flags;
    }
}
