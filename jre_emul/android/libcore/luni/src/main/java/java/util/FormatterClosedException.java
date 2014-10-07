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
 * A {@code FormatterClosedException} will be thrown if the formatter has been
 * closed.
 *
 * @see java.lang.RuntimeException
 */
public class FormatterClosedException extends IllegalStateException implements
        Serializable {
    private static final long serialVersionUID = 18111216L;

    /**
     * Constructs a new {@code FormatterClosedException} with the stack trace
     * filled in.
     */
    public FormatterClosedException() {
    }
}
