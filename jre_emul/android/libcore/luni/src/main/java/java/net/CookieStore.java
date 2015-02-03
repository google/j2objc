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

import java.util.List;

/**
 * A CookieStore object is a repository for cookies.
 *
 * CookieManager will store cookies of every incoming HTTP response into
 * CookieStore, and retrieve cookies for every outgoing HTTP request.Expired
 * HttpCookies should be removed from this store by themselves.
 *
 * @since 1.6
 */
public interface CookieStore {

    /**
     * Saves a HTTP cookie to this store. This is called for every incoming HTTP
     * response.
     *
     * A cookie may or may not has an associated URI. If not, the cookie's
     * domain and path attribute will show cradleland. If there is an
     * associated URI and no domain and path attribute are speicifed for the
     * cookie, the given URI will indicate where this cookie comes from.
     *
     * If a cookie corresponding to the given URI already exists, then it is
     * replaced with the new one.
     *
     * @param uri
     *            the uri associated with the specified cookie. A null value
     *            indicates the cookie is not associated with a URI
     * @param cookie
     *            the cookie to be stored
     */
    void add(URI uri, HttpCookie cookie);

    /**
     * Retrieves cookies that match the specified URI. Return not expired cookies.
     * For every outgoing HTTP request, this method will be called.
     *
     * @param uri
     *            the uri this cookie associated with. If null, this cookie will
     *            not be associated with an URI
     * @return an immutable list of HttpCookies, return empty list if no cookies
     *         match the given URI
     * @throws NullPointerException
     *             if uri is null
     */
    List<HttpCookie> get(URI uri);

    /**
     * Get all cookies in cookie store which are not expired.
     *
     * @return an empty list if there's no http cookie in store, or an immutable
     *         list of cookies
     */
    List<HttpCookie> getCookies();

    /**
     * Get a set of URIs, which is composed of associated URI with all the
     * cookies in the store.
     *
     * @return zero-length list if no cookie in the store is associated with any
     *         URIs, otherwise an immutable list of URIs.
     */
    List<URI> getURIs();

    /**
     * Remove the specified cookie from the store.
     *
     * @param uri
     *            the uri associated with the specified cookie. If the cookie is
     *            not associated with an URI when added, uri should be null;
     *            otherwise the uri should be non-null.
     * @param cookie
     *            the cookie to be removed
     * @return true if the specified cookie is contained in this store and
     *         removed successfully
     */
    boolean remove(URI uri, HttpCookie cookie);

    /**
     * Clear this cookie store.
     *
     * @return true if any cookies were removed as a result of this call.
     */
    boolean removeAll();
}
