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
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * A response cache entry. A {@code CacheResponse} object provides an {@code
 * InputStream} to access the response body and a {@code Map} for the response headers.
 * @see ResponseCache
 */
public abstract class CacheResponse {
    /**
     * Returns an {@code InputStream} to access the response body.
     *
     * @return an {@code InputStream} which can be used to fetch the response
     *         body.
     * @throws IOException
     *             if an I/O error is encountered while retrieving the response
     *             body.
     */
    public abstract InputStream getBody() throws IOException;

    /**
     * Returns an immutable {@code Map} which contains the response headers
     * information. Note that {@code URLConnection} may need the original headers to be
     * able to fully reconstruct the response. In particular, failure to provide
     * a mapping from null to the original HTTP status line will prevent an
     * {@code HttpURLConnection} from returning the correct response code.
     * See {@link URLConnection#getHeaderFields}.
     *
     * @return an immutable {@code Map} which contains the response headers.
     * @throws IOException
     *             if an I/O error is encountered while retrieving the response
     *             headers.
     */
    public abstract Map<String, List<String>> getHeaders() throws IOException;
}
