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

package java.nio.channels;

import java.io.IOException;

/**
 * A {@code FileLockInterruptionException} is thrown when a thread is
 * interrupted while waiting to acquire a file lock.
 * <p>
 * Note that the thread will also be in the 'interrupted' state.
 */
public class FileLockInterruptionException extends IOException {

    private static final long serialVersionUID = 7104080643653532383L;

    /**
     * Constructs a {@code FileLockInterruptionException}.
     */
    public FileLockInterruptionException() {
    }
}
