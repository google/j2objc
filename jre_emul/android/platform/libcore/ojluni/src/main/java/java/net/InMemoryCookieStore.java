/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (c) 2005, 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.net;

//import dalvik.system.VMRuntime;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

// Android-changed: App compat changes and bug fixes
// b/26456024 Add targetSdkVersion based compatibility for domain matching
// b/33034917 Support clearing cookies by adding it with "max-age=0"
// b/25897688 InMemoryCookieStore ignores scheme (http/https) port and path of the cookie
// Remove cookieJar and domainIndex. Use urlIndex as single Cookie storage
// Fix InMemoryCookieStore#remove to verify cookie URI before removal
// Fix InMemoryCookieStore#removeAll to return false if it's empty.
/**
 * A simple in-memory java.net.CookieStore implementation
 *
 * @author Edward Wang
 * @since 1.6
 * @hide Visible for testing only.
 */
public class InMemoryCookieStore implements CookieStore {
    // the in-memory representation of cookies
    // BEGIN Android-removed: Remove cookieJar and domainIndex
    /*
    private List<HttpCookie> cookieJar = null;

    // the cookies are indexed by its domain and associated uri (if present)
    // CAUTION: when a cookie removed from main data structure (i.e. cookieJar),
    //          it won't be cleared in domainIndex & uriIndex. Double-check the
    //          presence of cookie when retrieve one form index store.
    private Map<String, List<HttpCookie>> domainIndex = null;
    */
    // END Android-removed: Remove cookieJar and domainIndex
    private Map<URI, List<HttpCookie>> uriIndex = null;

    // use ReentrantLock instead of syncronized for scalability
    private ReentrantLock lock = null;

    // BEGIN Android-changed: Add targetSdkVersion and remove cookieJar and domainIndex
//    private final boolean applyMCompatibility;

    /**
     * The default ctor
     */
    public InMemoryCookieStore() {
        // j2objc: use hard-coded Android version number for Android 10.
        this(/*VMRuntime.getRuntime().getTargetSdkVersion()*/ 29);
    }

    public InMemoryCookieStore(int targetSdkVersion) {
        uriIndex = new HashMap<>();
        lock = new ReentrantLock(false);
//        applyMCompatibility = (targetSdkVersion <= 23);
    }
    // END Android-changed: Add targetSdkVersion and remove cookieJar and domainIndex

    /**
     * Add one cookie into cookie store.
     */
    public void add(URI uri, HttpCookie cookie) {
        // pre-condition : argument can't be null
        if (cookie == null) {
            throw new NullPointerException("cookie is null");
        }

        lock.lock();
        try {
            // Android-changed: http://b/33034917, android supports clearing cookies
            // by adding the cookie with max-age: 0.
            //if (cookie.getMaxAge() != 0) {
            addIndex(uriIndex, getEffectiveURI(uri), cookie);
            //}
        } finally {
            lock.unlock();
        }
    }


    /**
     * Get all cookies, which:
     *  1) given uri domain-matches with, or, associated with
     *     given uri when added to the cookie store.
     *  3) not expired.
     * See RFC 2965 sec. 3.3.4 for more detail.
     */
    public List<HttpCookie> get(URI uri) {
        // argument can't be null
        if (uri == null) {
            throw new NullPointerException("uri is null");
        }

        List<HttpCookie> cookies = new ArrayList<HttpCookie>();
        // BEGIN Android-changed: b/25897688 InMemoryCookieStore ignores scheme (http/https)
        lock.lock();
        try {
            // check domainIndex first
            getInternal1(cookies, uriIndex, uri.getHost());
            // check uriIndex then
            getInternal2(cookies, uriIndex, getEffectiveURI(uri));
        } finally {
            lock.unlock();
        }
        // END Android-changed: b/25897688 InMemoryCookieStore ignores scheme (http/https)
        return cookies;
    }

    /**
     * Get all cookies in cookie store, except those have expired
     */
    public List<HttpCookie> getCookies() {
        // BEGIN Android-changed: Remove cookieJar and domainIndex
        List<HttpCookie> rt = new ArrayList<HttpCookie>();

        lock.lock();
        try {
            for (List<HttpCookie> list : uriIndex.values()) {
                Iterator<HttpCookie> it = list.iterator();
                while (it.hasNext()) {
                    HttpCookie cookie = it.next();
                    if (cookie.hasExpired()) {
                        it.remove();
                    } else if (!rt.contains(cookie)) {
                        rt.add(cookie);
                    }
                }
            }
        } finally {
            rt = Collections.unmodifiableList(rt);
            lock.unlock();
        }
        // END Android-changed: Remove cookieJar and domainIndex

        return rt;
    }

    /**
     * Get all URIs, which are associated with at least one cookie
     * of this cookie store.
     */
    public List<URI> getURIs() {
        // BEGIN Android-changed: App compat. Return URI with no cookies. http://b/65538736
        /*
        List<URI> uris = new ArrayList<URI>();

        lock.lock();
        try {
            Iterator<URI> it = uriIndex.keySet().iterator();
            while (it.hasNext()) {
                URI uri = it.next();
                List<HttpCookie> cookies = uriIndex.get(uri);
                if (cookies == null || cookies.size() == 0) {
                    // no cookies list or an empty list associated with
                    // this uri entry, delete it
                    it.remove();
                }
            }
        } finally {
            uris.addAll(uriIndex.keySet());
            lock.unlock();
        }

        return uris;
         */
        lock.lock();
        try {
            List<URI> result = new ArrayList<URI>(uriIndex.keySet());
            result.remove(null);
            return Collections.unmodifiableList(result);
        } finally {
            lock.unlock();
        }
        // END Android-changed: App compat. Return URI with no cookies. http://b/65538736
    }


    /**
     * Remove a cookie from store
     */
    public boolean remove(URI uri, HttpCookie ck) {
        // argument can't be null
        if (ck == null) {
            throw new NullPointerException("cookie is null");
        }

        // BEGIN Android-changed: Fix uri not being removed from uriIndex
        lock.lock();
        try {
            uri = getEffectiveURI(uri);
            if (uriIndex.get(uri) == null) {
                return false;
            } else {
                List<HttpCookie> cookies = uriIndex.get(uri);
                if (cookies != null) {
                    return cookies.remove(ck);
                } else {
                    return false;
                }
            }
        } finally {
            lock.unlock();
        }
        // END Android-changed: Fix uri not being removed from uriIndex
    }


    /**
     * Remove all cookies in this cookie store.
     */
    public boolean removeAll() {
        lock.lock();
        // BEGIN Android-changed: Let removeAll() return false when there are no cookies.
        boolean result = false;

        try {
            result = !uriIndex.isEmpty();
            uriIndex.clear();
        } finally {
            lock.unlock();
        }

        return result;
        // END Android-changed: Let removeAll() return false when there are no cookies.
    }


    /* ---------------- Private operations -------------- */


    /*
     * This is almost the same as HttpCookie.domainMatches except for
     * one difference: It won't reject cookies when the 'H' part of the
     * domain contains a dot ('.').
     * I.E.: RFC 2965 section 3.3.2 says that if host is x.y.domain.com
     * and the cookie domain is .domain.com, then it should be rejected.
     * However that's not how the real world works. Browsers don't reject and
     * some sites, like yahoo.com do actually expect these cookies to be
     * passed along.
     * And should be used for 'old' style cookies (aka Netscape type of cookies)
     */
    private boolean netscapeDomainMatches(String domain, String host)
    {
        if (domain == null || host == null) {
            return false;
        }

        // if there's no embedded dot in domain and domain is not .local
        boolean isLocalDomain = ".local".equalsIgnoreCase(domain);
        int embeddedDotInDomain = domain.indexOf('.');
        if (embeddedDotInDomain == 0) {
            embeddedDotInDomain = domain.indexOf('.', 1);
        }
        if (!isLocalDomain && (embeddedDotInDomain == -1 || embeddedDotInDomain == domain.length() - 1)) {
            return false;
        }

        // if the host name contains no dot and the domain name is .local
        int firstDotInHost = host.indexOf('.');
        if (firstDotInHost == -1 && isLocalDomain) {
            return true;
        }

        int domainLength = domain.length();
        int lengthDiff = host.length() - domainLength;
        if (lengthDiff == 0) {
            // if the host name and the domain name are just string-compare euqal
            return host.equalsIgnoreCase(domain);
        } else if (lengthDiff > 0) {
            // need to check H & D component
            String D = host.substring(lengthDiff);

//            // Android-changed: b/26456024 targetSdkVersion based compatibility for domain matching
//            // Android M and earlier: Cookies with domain "foo.com" would not match "bar.foo.com".
//            // The RFC dictates that the user agent must treat those domains as if they had a
//            // leading period and must therefore match "bar.foo.com".
//            if (applyMCompatibility && !domain.startsWith(".")) {
//                return false;
//            }

            return (D.equalsIgnoreCase(domain));
        } else if (lengthDiff == -1) {
            // if domain is actually .host
            return (domain.charAt(0) == '.' &&
                    host.equalsIgnoreCase(domain.substring(1)));
        }

        return false;
    }

    private void getInternal1(List<HttpCookie> cookies, Map<URI, List<HttpCookie>> cookieIndex,
            String host) {
        // BEGIN Android-changed: b/25897688 InMemoryCookieStore ignores scheme (http/https)
        // Use a separate list to handle cookies that need to be removed so
        // that there is no conflict with iterators.
        ArrayList<HttpCookie> toRemove = new ArrayList<HttpCookie>();
        for (Map.Entry<URI, List<HttpCookie>> entry : cookieIndex.entrySet()) {
            List<HttpCookie> lst = entry.getValue();
            for (HttpCookie c : lst) {
                String domain = c.getDomain();
                if ((c.getVersion() == 0 && netscapeDomainMatches(domain, host)) ||
                        (c.getVersion() == 1 && HttpCookie.domainMatches(domain, host))) {

                    // the cookie still in main cookie store
                    if (!c.hasExpired()) {
                        // don't add twice
                        if (!cookies.contains(c)) {
                            cookies.add(c);
                        }
                    } else {
                        toRemove.add(c);
                    }
                }
            }
            // Clear up the cookies that need to be removed
            for (HttpCookie c : toRemove) {
                lst.remove(c);

            }
            toRemove.clear();
        }
        // END Android-changed: b/25897688 InMemoryCookieStore ignores scheme (http/https)
    }

    // @param cookies           [OUT] contains the found cookies
    // @param cookieIndex       the index
    // @param comparator        the prediction to decide whether or not
    //                          a cookie in index should be returned
    private <T extends Comparable<T>>
        void getInternal2(List<HttpCookie> cookies, Map<T, List<HttpCookie>> cookieIndex,
                          T comparator)
    {
        // BEGIN Android-changed: b/25897688 InMemoryCookieStore ignores scheme (http/https)
        // Removed cookieJar
        for (T index : cookieIndex.keySet()) {
            if ((index == comparator) || (index != null && comparator.compareTo(index) == 0)) {
                List<HttpCookie> indexedCookies = cookieIndex.get(index);
                // check the list of cookies associated with this domain
                if (indexedCookies != null) {
                    Iterator<HttpCookie> it = indexedCookies.iterator();
                    while (it.hasNext()) {
                        HttpCookie ck = it.next();
                        // the cookie still in main cookie store
                        if (!ck.hasExpired()) {
                            // don't add twice
                            if (!cookies.contains(ck))
                                cookies.add(ck);
                        } else {
                            it.remove();
                        }
                    }
                } // end of indexedCookies != null
            } // end of comparator.compareTo(index) == 0
        } // end of cookieIndex iteration
        // END Android-changed: b/25897688 InMemoryCookieStore ignores scheme (http/https)
    }

    // add 'cookie' indexed by 'index' into 'indexStore'
    private <T> void addIndex(Map<T, List<HttpCookie>> indexStore,
                              T index,
                              HttpCookie cookie)
    {
        // Android-changed: "index" can be null. We only use the URI based
        // index on Android and we want to support null URIs. The underlying
        // store is a HashMap which will support null keys anyway.
        // if (index != null) {
        List<HttpCookie> cookies = indexStore.get(index);
        if (cookies != null) {
            // there may already have the same cookie, so remove it first
            cookies.remove(cookie);

            cookies.add(cookie);
        } else {
            cookies = new ArrayList<HttpCookie>();
            cookies.add(cookie);
            indexStore.put(index, cookies);
        }
    }


    //
    // for cookie purpose, the effective uri should only be http://host
    // the path will be taken into account when path-match algorithm applied
    //
    private URI getEffectiveURI(URI uri) {
        URI effectiveURI = null;
        // Android-added: Fix NullPointerException
        if (uri == null) {
            return null;
        }
        try {
            effectiveURI = new URI("http",
                                   uri.getHost(),
                                   null,  // path component
                                   null,  // query component
                                   null   // fragment component
                                  );
        } catch (URISyntaxException ignored) {
            effectiveURI = uri;
        }

        return effectiveURI;
    }
}
