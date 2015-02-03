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

/**
 * CookiePolicy has three pre-defined policy. They are ACCEPT_ALL, ACCEPT_NONE
 * and ACCEPT_ORIGINAL_SERVER respectively. They are used to decide which
 * cookies should be accepted and which should not be.
 *
 * See <a href="http://www.ietf.org/rfc/rfc2965.txt">RFC 2965</a> sections 3.3 and 7 for more detail.
 *
 * @since 1.6
 */
public interface CookiePolicy {

    /**
     * A pre-defined policy, accepts all cookies.
     */
    static final CookiePolicy ACCEPT_ALL = new CookiePolicy() {
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            return true;
        }
    };

    /**
     * A pre-defined policy, accepts no cookies at all.
     */
    static final CookiePolicy ACCEPT_NONE = new CookiePolicy() {
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            return false;
        }
    };

    /**
     * A pre-defined policy, only accepts cookies from original server.
     */
    static final CookiePolicy ACCEPT_ORIGINAL_SERVER = new CookiePolicy() {
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            return HttpCookie.domainMatches(cookie.getDomain(), uri.getHost());
        }
    };

    /**
     * This method is used to determine whether or not the specified cookie
     * should be accepted.
     *
     * @param uri
     *            the URI to used to determine acceptability
     * @param cookie
     *            the HttpCookie to be determined
     * @return true if this cookie should be accepted; false otherwise
     */
    boolean shouldAccept(URI uri, HttpCookie cookie);
}
