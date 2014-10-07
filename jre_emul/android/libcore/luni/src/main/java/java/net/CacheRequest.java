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

package java.net;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@code CacheRequest} is a kind of channel for storing resource data in the
 * {@code ResponseCache}. A protocol handler calls the {@code OutputStream}
 * which is provided by the {@code CacheRequest} object, to store the resource
 * data into the cache. It also allows the user to interrupt and abort the
 * current store operation by calling the method {@code abort}. If an {@code
 * IOException} occurs while reading the response or writing data to the cache,
 * the current cache store operation is abandoned.
 *
 * @see ResponseCache
 */
public abstract class CacheRequest {

    /**
     * This implementation does nothing.
     */
    public CacheRequest() {
    }

    /**
     * Aborts the current cache operation. If an {@code IOException} occurs
     * while reading the response or writing resource data to the cache, the
     * current cache store operation is aborted.
     */
    public abstract void abort();

    /**
     * Returns an {@code OutputStream} which is used to write the response body.
     *
     * @return an {@code OutputStream} which is used to write the response body.
     * @throws IOException
     *             if an I/O error is encountered during writing response body
     *             operation.
     */
    public abstract OutputStream getBody() throws IOException;
}
