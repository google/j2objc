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
import java.util.List;
import java.util.Map;

/**
 * Caches {@code URLConnection} responses.
 * <p>The system's default cache can be set using {@link #setDefault}.
 * If {@link URLConnection#getUseCaches} returns true, {@code URLConnection} will use the
 * default response cache, if one has been set.
 * <p>Although {@code URLConnection} will always call {@link #put}, the specific
 * {@code ResponseCache} implementation gets to decide what will actually be cached,
 * and for how long.
 */
public abstract class ResponseCache {
    private static ResponseCache defaultResponseCache = null;

    /**
     * Returns the system's default response cache, or null.
     */
    public static ResponseCache getDefault() {
        return defaultResponseCache;
    }

    /**
     * Sets the system's default response cache. Use null to remove the response cache.
     */
    public static void setDefault(ResponseCache responseCache) {
        defaultResponseCache = responseCache;
    }

    /**
     * Returns the cached response corresponding to the given request.
     *
     * @param uri
     *            the request URI.
     * @param requestMethod
     *            the request method.
     * @param requestHeaders
     *            a map of request headers.
     * @return the {@code CacheResponse} object if the request is available in the cache
     *         or {@code null} otherwise.
     * @throws IOException
     *             if an I/O error occurs while getting the cached data.
     * @throws IllegalArgumentException
     *             if any one of the parameters is set to {@code null}.
     */
    public abstract CacheResponse get(URI uri, String requestMethod,
            Map<String, List<String>> requestHeaders) throws IOException;

    /**
     * Allows the protocol handler to cache data after retrieving resources. The
     * {@code ResponseCache} decides whether the resource data should be cached
     * or not. If so, this method returns a {@code CacheRequest} to write the
     * resource data to. Otherwise, this method returns {@code null}.
     *
     * @param uri
     *            the reference to the requested resource.
     * @param connection
     *            the connection to fetch the response.
     * @return a CacheRequest object with a WriteableByteChannel if the resource
     *         has to be cached, {@code null} otherwise.
     * @throws IOException
     *             if an I/O error occurs while adding the resource.
     * @throws IllegalArgumentException
     *             if any one of the parameters is set to {@code null}.
     */
    public abstract CacheRequest put(URI uri, URLConnection connection) throws IOException;
}
