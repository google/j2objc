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
 * This class provides a way to manage cookies with a HTTP protocol handler.
 */
public abstract class CookieHandler {

    private static CookieHandler systemWideCookieHandler;

    /**
     * Returns the system-wide cookie handler or {@code null} if not set.
     */
    public static CookieHandler getDefault() {
        return systemWideCookieHandler;
    }

    /**
     * Sets the system-wide cookie handler.
     */
    public static void setDefault(CookieHandler cHandler) {
        systemWideCookieHandler = cHandler;
    }

    /**
     * Gets all cookies for a specific URI from the cookie cache.
     *
     * @param uri
     *            a URI to search for applicable cookies.
     * @param requestHeaders
     *            a list of request headers.
     * @return an unchangeable map of all appropriate cookies.
     * @throws IOException
     *             if an error occurs during the I/O operation.
     */
    public abstract Map<String, List<String>> get(URI uri,
            Map<String, List<String>> requestHeaders) throws IOException;

    /**
     * Sets all cookies of a specific URI in the {@code responseHeaders} into
     * the cookie cache.
     *
     * @param uri
     *            the origin URI of the cookies.
     * @param responseHeaders
     *            a list of request headers.
     * @throws IOException
     *             if an error occurs during the I/O operation.
     */
    public abstract void put(URI uri, Map<String, List<String>> responseHeaders)
            throws IOException;
}
