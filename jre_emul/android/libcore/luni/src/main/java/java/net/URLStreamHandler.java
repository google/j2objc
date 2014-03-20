/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package java.net;

import java.io.IOException;
import libcore.net.url.UrlUtils;
import libcore.util.Objects;

/**
 * The abstract class {@code URLStreamHandler} is the base for all classes which
 * can handle the communication with a URL object over a particular protocol
 * type.
 */
public abstract class URLStreamHandler {
    /**
     * Establishes a new connection to the resource specified by the URL {@code
     * u}. Since different protocols also have unique ways of connecting, it
     * must be overwritten by the subclass.
     *
     * @param u
     *            the URL to the resource where a connection has to be opened.
     * @return the opened URLConnection to the specified resource.
     * @throws IOException
     *             if an I/O error occurs during opening the connection.
     */
    protected abstract URLConnection openConnection(URL u) throws IOException;

    /**
     * Establishes a new connection to the resource specified by the URL {@code
     * u} using the given {@code proxy}. Since different protocols also have
     * unique ways of connecting, it must be overwritten by the subclass.
     *
     * @param u
     *            the URL to the resource where a connection has to be opened.
     * @param proxy
     *            the proxy that is used to make the connection.
     * @return the opened URLConnection to the specified resource.
     * @throws IOException
     *             if an I/O error occurs during opening the connection.
     * @throws IllegalArgumentException
     *             if any argument is {@code null} or the type of proxy is
     *             wrong.
     * @throws UnsupportedOperationException
     *             if the protocol handler doesn't support this method.
     */
    protected URLConnection openConnection(URL u, Proxy proxy) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Parses the clear text URL in {@code str} into a URL object. URL strings
     * generally have the following format:
     * <p>
     * http://www.company.com/java/file1.java#reference
     * <p>
     * The string is parsed in HTTP format. If the protocol has a different URL
     * format this method must be overridden.
     *
     * @param url
     *            the URL to fill in the parsed clear text URL parts.
     * @param spec
     *            the URL string that is to be parsed.
     * @param start
     *            the string position from where to begin parsing.
     * @param end
     *            the string position to stop parsing.
     * @see #toExternalForm
     * @see URL
     */
    protected void parseURL(URL url, String spec, int start, int end) {
        if (this != url.streamHandler) {
            throw new SecurityException("Only a URL's stream handler is permitted to mutate it");
        }
        if (end < start) {
            throw new StringIndexOutOfBoundsException(spec, start, end - start);
        }

        int fileStart;
        String authority;
        String userInfo;
        String host;
        int port = -1;
        String path;
        String query;
        String ref;
        if (spec.regionMatches(start, "//", 0, 2)) {
            // Parse the authority from the spec.
            int authorityStart = start + 2;
            fileStart = UrlUtils.findFirstOf(spec, "/?#", authorityStart, end);
            authority = spec.substring(authorityStart, fileStart);
            int userInfoEnd = UrlUtils.findFirstOf(spec, "@", authorityStart, fileStart);
            int hostStart;
            if (userInfoEnd != fileStart) {
                userInfo = spec.substring(authorityStart, userInfoEnd);
                hostStart = userInfoEnd + 1;
            } else {
                userInfo = null;
                hostStart = authorityStart;
            }

            /*
             * Extract the host and port. The host may be an IPv6 address with
             * colons like "[::1]", in which case we look for the port delimiter
             * colon after the ']' character.
             */
            int colonSearchFrom = hostStart;
            int ipv6End = UrlUtils.findFirstOf(spec, "]", hostStart, fileStart);
            if (ipv6End != fileStart) {
                if (UrlUtils.findFirstOf(spec, ":", hostStart, ipv6End) == ipv6End) {
                    throw new IllegalArgumentException("Expected an IPv6 address: "
                            + spec.substring(hostStart, ipv6End + 1));
                }
                colonSearchFrom = ipv6End;
            }
            int hostEnd = UrlUtils.findFirstOf(spec, ":", colonSearchFrom, fileStart);
            host = spec.substring(hostStart, hostEnd);
            int portStart = hostEnd + 1;
            if (portStart < fileStart) {
                port = Integer.parseInt(spec.substring(portStart, fileStart));
                if (port < 0) {
                    throw new IllegalArgumentException("port < 0: " + port);
                }
            }
            path = null;
            query = null;
            ref = null;
        } else {
            // Get the authority from the context URL.
            fileStart = start;
            authority = url.getAuthority();
            userInfo = url.getUserInfo();
            host = url.getHost();
            if (host == null) {
                host = "";
            }
            port = url.getPort();
            path = url.getPath();
            query = url.getQuery();
            ref = url.getRef();
        }

        /*
         * Extract the path, query and fragment. Each part has its own leading
         * delimiter character. The query can contain slashes and the fragment
         * can contain slashes and question marks.
         *    / path ? query # fragment
         */
        int pos = fileStart;
        while (pos < end) {
            int nextPos;
            switch (spec.charAt(pos)) {
            case '#':
                nextPos = end;
                ref = spec.substring(pos + 1, nextPos);
                break;
            case '?':
                nextPos = UrlUtils.findFirstOf(spec, "#", pos, end);
                query = spec.substring(pos + 1, nextPos);
                ref = null;
                break;
            default:
                nextPos = UrlUtils.findFirstOf(spec, "?#", pos, end);
                path = relativePath(path, spec.substring(pos, nextPos));
                query = null;
                ref = null;
                break;
            }
            pos = nextPos;
        }

        if (path == null) {
            path = "";
        }

        path = UrlUtils.authoritySafePath(authority, path);

        setURL(url, url.getProtocol(), host, port, authority, userInfo, path, query, ref);
    }

    /**
     * Returns a new path by resolving {@code path} relative to {@code base}.
     */
    private static String relativePath(String base, String path) {
        if (path.startsWith("/")) {
            return UrlUtils.canonicalizePath(path, true);
        } else if (base != null) {
            String combined = base.substring(0, base.lastIndexOf('/') + 1) + path;
            return UrlUtils.canonicalizePath(combined, true);
        } else {
            return path;
        }
    }

    /**
     * Sets the fields of the URL {@code u} to the values of the supplied
     * arguments.
     *
     * @param u
     *            the non-null URL object to be set.
     * @param protocol
     *            the protocol.
     * @param host
     *            the host name.
     * @param port
     *            the port number.
     * @param file
     *            the file component.
     * @param ref
     *            the reference.
     * @deprecated Use setURL(URL, String String, int, String, String, String,
     *             String, String) instead.
     */
    @Deprecated
    protected void setURL(URL u, String protocol, String host, int port,
            String file, String ref) {
        if (this != u.streamHandler) {
            throw new SecurityException();
        }
        u.set(protocol, host, port, file, ref);
    }

    /**
     * Sets the fields of the URL {@code u} to the values of the supplied
     * arguments.
     */
    protected void setURL(URL u, String protocol, String host, int port,
            String authority, String userInfo, String path, String query,
            String ref) {
        if (this != u.streamHandler) {
            throw new SecurityException();
        }
        u.set(protocol, host, port, authority, userInfo, path, query, ref);
    }

    /**
     * Returns the clear text representation of a given URL using HTTP format.
     *
     * @param url
     *            the URL object to be converted.
     * @return the clear text representation of the specified URL.
     * @see #parseURL
     * @see URL#toExternalForm()
     */
    protected String toExternalForm(URL url) {
        return toExternalForm(url, false);
    }

    String toExternalForm(URL url, boolean escapeIllegalCharacters) {
        StringBuilder result = new StringBuilder();
        result.append(url.getProtocol());
        result.append(':');

        String authority = url.getAuthority();
        if (authority != null) {
            result.append("//");
            if (escapeIllegalCharacters) {
                URI.AUTHORITY_ENCODER.appendPartiallyEncoded(result, authority);
            } else {
                result.append(authority);
            }
        }

        String fileAndQuery = url.getFile();
        if (fileAndQuery != null) {
            if (escapeIllegalCharacters) {
                URI.FILE_AND_QUERY_ENCODER.appendPartiallyEncoded(result, fileAndQuery);
            } else {
                result.append(fileAndQuery);
            }
        }

        String ref = url.getRef();
        if (ref != null) {
            result.append('#');
            if (escapeIllegalCharacters) {
                URI.ALL_LEGAL_ENCODER.appendPartiallyEncoded(result, ref);
            } else {
                result.append(ref);
            }
        }

        return result.toString();
    }

    /**
     * Returns true if {@code a} and {@code b} have the same protocol, host,
     * port, file, and reference.
     */
    protected boolean equals(URL a, URL b) {
        return sameFile(a, b)
                && Objects.equal(a.getRef(), b.getRef())
                && Objects.equal(a.getQuery(), b.getQuery());
    }

    /**
     * Returns the default port of the protocol used by the handled URL. The
     * default implementation always returns {@code -1}.
     */
    protected int getDefaultPort() {
        return -1;
    }

    /**
     * Returns the host address of {@code url}.
     */
    protected InetAddress getHostAddress(URL url) {
        try {
            String host = url.getHost();
            if (host == null || host.length() == 0) {
                return null;
            }
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * Returns the hash code of {@code url}.
     */
    protected int hashCode(URL url) {
        return toExternalForm(url).hashCode();
    }

    /**
     * Returns true if the hosts of {@code a} and {@code b} are equal.
     */
    protected boolean hostsEqual(URL a, URL b) {
        // URLs with the same case-insensitive host name have equal hosts
        String aHost = a.getHost();
        String bHost = b.getHost();
        return (aHost == bHost) || aHost != null && aHost.equalsIgnoreCase(bHost);
    }

    /**
     * Returns true if {@code a} and {@code b} have the same protocol, host,
     * port and file.
     */
    protected boolean sameFile(URL a, URL b) {
        return Objects.equal(a.getProtocol(), b.getProtocol())
                && hostsEqual(a, b)
                && a.getEffectivePort() == b.getEffectivePort()
                && Objects.equal(a.getFile(), b.getFile());
    }
}
