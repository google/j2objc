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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Locale;
import libcore.net.UriCodec;
import libcore.net.url.UrlUtils;

/**
 * A Uniform Resource Identifier that identifies an abstract or physical
 * resource, as specified by <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC
 * 2396</a>.
 *
 * <h3>Parts of a URI</h3>
 * A URI is composed of many parts. This class can both parse URI strings into
 * parts and compose URI strings from parts. For example, consider the parts of
 * this URI:
 * {@code http://username:password@host:8080/directory/file?query#fragment}
 * <table>
 * <tr><th>Component                                            </th><th>Example value                                                      </th><th>Also known as</th></tr>
 * <tr><td>{@link #getScheme() Scheme}                          </td><td>{@code http}                                                       </td><td>protocol</td></tr>
 * <tr><td>{@link #getSchemeSpecificPart() Scheme-specific part}</td><td>{@code //username:password@host:8080/directory/file?query#fragment}</td><td></td></tr>
 * <tr><td>{@link #getAuthority() Authority}                    </td><td>{@code username:password@host:8080}                                </td><td></td></tr>
 * <tr><td>{@link #getUserInfo() User Info}                     </td><td>{@code username:password}                                          </td><td></td></tr>
 * <tr><td>{@link #getHost() Host}                              </td><td>{@code host}                                                       </td><td></td></tr>
 * <tr><td>{@link #getPort() Port}                              </td><td>{@code 8080}                                                       </td><td></td></tr>
 * <tr><td>{@link #getPath() Path}                              </td><td>{@code /directory/file}                                            </td><td></td></tr>
 * <tr><td>{@link #getQuery() Query}                            </td><td>{@code query}                                                      </td><td></td></tr>
 * <tr><td>{@link #getFragment() Fragment}                      </td><td>{@code fragment}                                                   </td><td>ref</td></tr>
 * </table>
 *
 * <h3>Absolute vs. Relative URIs</h3>
 * URIs are either {@link #isAbsolute() absolute or relative}.
 * <ul>
 *     <li><strong>Absolute:</strong> {@code http://android.com/robots.txt}
 *     <li><strong>Relative:</strong> {@code robots.txt}
 * </ul>
 *
 * <p>Absolute URIs always have a scheme. If its scheme is supported by {@link
 * URL}, you can use {@link #toURL} to convert an absolute URI to a URL.
 *
 * <p>Relative URIs do not have a scheme and cannot be converted to URLs. If you
 * have the absolute URI that a relative URI is relative to, you can use {@link
 * #resolve} to compute the referenced absolute URI. Symmetrically, you can use
 * {@link #relativize} to compute the relative URI from one URI to another.
 * <pre>   {@code
 *   URI absolute = new URI("http://android.com/");
 *   URI relative = new URI("robots.txt");
 *   URI resolved = new URI("http://android.com/robots.txt");
 *
 *   // print "http://android.com/robots.txt"
 *   System.out.println(absolute.resolve(relative));
 *
 *   // print "robots.txt"
 *   System.out.println(absolute.relativize(resolved));
 * }</pre>
 *
 * <h3>Opaque vs. Hierarchical URIs</h3>
 * Absolute URIs are either {@link #isOpaque() opaque or hierarchical}. Relative
 * URIs are always hierarchical.
 * <ul>
 *     <li><strong>Hierarchical:</strong> {@code http://android.com/robots.txt}
 *     <li><strong>Opaque:</strong> {@code mailto:robots@example.com}
 * </ul>
 *
 * <p>Opaque URIs have both a scheme and a scheme-specific part that does not
 * begin with the slash character: {@code /}. The contents of the
 * scheme-specific part of an opaque URI is not parsed so an opaque URI never
 * has an authority, user info, host, port, path or query. An opaque URIs may
 * have a fragment, however. A typical opaque URI is
 * {@code mailto:robots@example.com}.
 * <table>
 * <tr><th>Component           </th><th>Example value             </th></tr>
 * <tr><td>Scheme              </td><td>{@code mailto}            </td></tr>
 * <tr><td>Scheme-specific part</td><td>{@code robots@example.com}</td></tr>
 * <tr><td>Fragment            </td><td>                          </td></tr>
 * </table>
 * <p>Hierarchical URIs may have values for any URL component. They always
 * have a non-null path, though that path may be the empty string.
 *
 * <h3>Encoding and Decoding URI Components</h3>
 * Each component of a URI permits a limited set of legal characters. Other
 * characters must first be <i>encoded</i> before they can be embedded in a URI.
 * To recover the original characters from a URI, they may be <i>decoded</i>.
 * <strong>Contrary to what you might expect,</strong> this class uses the
 * term <i>raw</i> to refer to encoded strings. The non-<i>raw</i> accessors
 * return decoded strings. For example, consider how this URI is decoded:
 * {@code http://user:pa55w%3Frd@host:80/doc%7Csearch?q=green%20robots#over%206%22}
 * <table>
 * <tr><th>Component           </th><th>Legal Characters                                                    </th><th>Other Constraints                                  </th><th>Raw Value                                                      </th><th>Value</th></tr>
 * <tr><td>Scheme              </td><td>{@code 0-9}, {@code a-z}, {@code A-Z}, {@code +-.}                  </td><td>First character must be in {@code a-z}, {@code A-Z}</td><td>                                                               </td><td>{@code http}</td></tr>
 * <tr><td>Scheme-specific part</td><td>{@code 0-9}, {@code a-z}, {@code A-Z}, {@code _-!.~'()*,;:$&+=?/[]@}</td><td>Non-ASCII characters okay                          </td><td>{@code //user:pa55w%3Frd@host:80/doc%7Csearch?q=green%20robots}</td><td>{@code //user:pa55w?rd@host:80/doc|search?q=green robots}</td></tr>
 * <tr><td>Authority           </td><td>{@code 0-9}, {@code a-z}, {@code A-Z}, {@code _-!.~'()*,;:$&+=@[]}  </td><td>Non-ASCII characters okay                          </td><td>{@code user:pa55w%3Frd@host:80}                                </td><td>{@code user:pa55w?rd@host:80}</td></tr>
 * <tr><td>User Info           </td><td>{@code 0-9}, {@code a-z}, {@code A-Z}, {@code _-!.~'()*,;:$&+=}     </td><td>Non-ASCII characters okay                          </td><td>{@code user:pa55w%3Frd}                                        </td><td>{@code user:pa55w?rd}</td></tr>
 * <tr><td>Host                </td><td>{@code 0-9}, {@code a-z}, {@code A-Z}, {@code -.[]}                 </td><td>Domain name, IPv4 address or [IPv6 address]        </td><td>                                                               </td><td>host</td></tr>
 * <tr><td>Port                </td><td>{@code 0-9}                                                         </td><td>                                                   </td><td>                                                               </td><td>{@code 80}</td></tr>
 * <tr><td>Path                </td><td>{@code 0-9}, {@code a-z}, {@code A-Z}, {@code _-!.~'()*,;:$&+=/@}   </td><td>Non-ASCII characters okay                          </td><td>{@code /doc%7Csearch}                                          </td><td>{@code /doc|search}</td></tr>
 * <tr><td>Query               </td><td>{@code 0-9}, {@code a-z}, {@code A-Z}, {@code _-!.~'()*,;:$&+=?/[]@}</td><td>Non-ASCII characters okay                          </td><td>{@code q=green%20robots}                                       </td><td>{@code q=green robots}</td></tr>
 * <tr><td>Fragment            </td><td>{@code 0-9}, {@code a-z}, {@code A-Z}, {@code _-!.~'()*,;:$&+=?/[]@}</td><td>Non-ASCII characters okay                          </td><td>{@code over%206%22}                                            </td><td>{@code over 6"}</td></tr>
 * </table>
 * A URI's host, port and scheme are not eligible for encoding and must not
 * contain illegal characters.
 *
 * <p>To encode a URI, invoke any of the multiple-parameter constructors of this
 * class. These constructors accept your original strings and encode them into
 * their raw form.
 *
 * <p>To decode a URI, invoke the single-string constructor, and then use the
 * appropriate accessor methods to get the decoded components.
 *
 * <p>The {@link URL} class can be used to retrieve resources by their URI.
 */
public final class URI implements Comparable<URI>, Serializable {

    private static final long serialVersionUID = -6052424284110960213l;

    static final String UNRESERVED = "_-!.~\'()*";
    static final String PUNCTUATION = ",;:$&+=";

    static final UriCodec USER_INFO_ENCODER = new PartEncoder("");
    static final UriCodec PATH_ENCODER = new PartEncoder("/@");
    static final UriCodec AUTHORITY_ENCODER = new PartEncoder("@[]");

    /** for java.net.URL, which foolishly combines these two parts */
    static final UriCodec FILE_AND_QUERY_ENCODER = new PartEncoder("/@?");

    /** for query, fragment, and scheme-specific part */
    static final UriCodec ALL_LEGAL_ENCODER = new PartEncoder("?/[]@");

    /** Retains all ASCII chars including delimiters. */
    private static final UriCodec ASCII_ONLY = new UriCodec() {
        @Override protected boolean isRetained(char c) {
            return c <= 127;
        }
    };

    /**
     * Encodes the unescaped characters of {@code s} that are not permitted.
     * Permitted characters are:
     * <ul>
     *   <li>Unreserved characters in <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>.
     *   <li>{@code extraOkayChars},
     *   <li>non-ASCII, non-control, non-whitespace characters
     * </ul>
     */
    private static class PartEncoder extends UriCodec {
        private final String extraLegalCharacters;

        PartEncoder(String extraLegalCharacters) {
            this.extraLegalCharacters = extraLegalCharacters;
        }

        @Override protected boolean isRetained(char c) {
            return UNRESERVED.indexOf(c) != -1
                    || PUNCTUATION.indexOf(c) != -1
                    || extraLegalCharacters.indexOf(c) != -1
                    || (c > 127 && !Character.isSpaceChar(c) && !Character.isISOControl(c));
        }
    }

    private String string;
    private transient String scheme;
    private transient String schemeSpecificPart;
    private transient String authority;
    private transient String userInfo;
    private transient String host;
    private transient int port = -1;
    private transient String path;
    private transient String query;
    private transient String fragment;
    private transient boolean opaque;
    private transient boolean absolute;
    private transient boolean serverAuthority = false;

    private transient int hash = -1;

    private URI() {}

    /**
     * Creates a new URI instance by parsing {@code spec}.
     *
     * @param spec a URI whose illegal characters have all been encoded.
     */
    public URI(String spec) throws URISyntaxException {
        parseURI(spec, false);
    }

    /**
     * Creates a new URI instance of the given unencoded component parts.
     *
     * @param scheme the URI scheme, or null for a non-absolute URI.
     */
    public URI(String scheme, String schemeSpecificPart, String fragment)
            throws URISyntaxException {
        StringBuilder uri = new StringBuilder();
        if (scheme != null) {
            uri.append(scheme);
            uri.append(':');
        }
        if (schemeSpecificPart != null) {
            ALL_LEGAL_ENCODER.appendEncoded(uri, schemeSpecificPart);
        }
        if (fragment != null) {
            uri.append('#');
            ALL_LEGAL_ENCODER.appendEncoded(uri, fragment);
        }

        parseURI(uri.toString(), false);
    }

    /**
     * Creates a new URI instance of the given unencoded component parts.
     *
     * @param scheme the URI scheme, or null for a non-absolute URI.
     */
    public URI(String scheme, String userInfo, String host, int port, String path, String query,
            String fragment) throws URISyntaxException {
        if (scheme == null && userInfo == null && host == null && path == null
                && query == null && fragment == null) {
            this.path = "";
            return;
        }

        if (scheme != null && path != null && !path.isEmpty() && path.charAt(0) != '/') {
            throw new URISyntaxException(path, "Relative path");
        }

        StringBuilder uri = new StringBuilder();
        if (scheme != null) {
            uri.append(scheme);
            uri.append(':');
        }

        if (userInfo != null || host != null || port != -1) {
            uri.append("//");
        }

        if (userInfo != null) {
            USER_INFO_ENCODER.appendEncoded(uri, userInfo);
            uri.append('@');
        }

        if (host != null) {
            // check for IPv6 addresses that hasn't been enclosed in square brackets
            if (host.indexOf(':') != -1 && host.indexOf(']') == -1 && host.indexOf('[') == -1) {
                host = "[" + host + "]";
            }
            uri.append(host);
        }

        if (port != -1) {
            uri.append(':');
            uri.append(port);
        }

        if (path != null) {
            PATH_ENCODER.appendEncoded(uri, path);
        }

        if (query != null) {
            uri.append('?');
            ALL_LEGAL_ENCODER.appendEncoded(uri, query);
        }

        if (fragment != null) {
            uri.append('#');
            ALL_LEGAL_ENCODER.appendEncoded(uri, fragment);
        }

        parseURI(uri.toString(), true);
    }

    /**
     * Creates a new URI instance of the given unencoded component parts.
     *
     * @param scheme the URI scheme, or null for a non-absolute URI.
     */
    public URI(String scheme, String host, String path, String fragment) throws URISyntaxException {
        this(scheme, null, host, -1, path, null, fragment);
    }

    /**
     * Creates a new URI instance of the given unencoded component parts.
     *
     * @param scheme the URI scheme, or null for a non-absolute URI.
     */
    public URI(String scheme, String authority, String path, String query,
            String fragment) throws URISyntaxException {
        if (scheme != null && path != null && !path.isEmpty() && path.charAt(0) != '/') {
            throw new URISyntaxException(path, "Relative path");
        }

        StringBuilder uri = new StringBuilder();
        if (scheme != null) {
            uri.append(scheme);
            uri.append(':');
        }
        if (authority != null) {
            uri.append("//");
            AUTHORITY_ENCODER.appendEncoded(uri, authority);
        }

        if (path != null) {
            PATH_ENCODER.appendEncoded(uri, path);
        }
        if (query != null) {
            uri.append('?');
            ALL_LEGAL_ENCODER.appendEncoded(uri, query);
        }
        if (fragment != null) {
            uri.append('#');
            ALL_LEGAL_ENCODER.appendEncoded(uri, fragment);
        }

        parseURI(uri.toString(), false);
    }

    /**
     * Breaks uri into its component parts. This first splits URI into scheme,
     * scheme-specific part and fragment:
     *   [scheme:][scheme-specific part][#fragment]
     *
     * Then it breaks the scheme-specific part into authority, path and query:
     *   [//authority][path][?query]
     *
     * Finally it delegates to parseAuthority to break the authority into user
     * info, host and port:
     *   [user-info@][host][:port]
     */
    private void parseURI(String uri, boolean forceServer) throws URISyntaxException {
        string = uri;

        // "#fragment"
        int fragmentStart = UrlUtils.findFirstOf(uri, "#", 0, uri.length());
        if (fragmentStart < uri.length()) {
            fragment = ALL_LEGAL_ENCODER.validate(uri, fragmentStart + 1, uri.length(), "fragment");
        }

        // scheme:
        int start;
        int colon = UrlUtils.findFirstOf(uri, ":", 0, fragmentStart);
        if (colon < UrlUtils.findFirstOf(uri, "/?#", 0, fragmentStart)) {
            absolute = true;
            scheme = validateScheme(uri, colon);
            start = colon + 1;

            if (start == fragmentStart) {
                throw new URISyntaxException(uri, "Scheme-specific part expected", start);
            }

            // URIs with schemes followed by a non-/ char are opaque and need no further parsing.
            if (!uri.regionMatches(start, "/", 0, 1)) {
                opaque = true;
                schemeSpecificPart = ALL_LEGAL_ENCODER.validate(
                        uri, start, fragmentStart, "scheme specific part");
                return;
            }
        } else {
            absolute = false;
            start = 0;
        }

        opaque = false;
        schemeSpecificPart = uri.substring(start, fragmentStart);

        // "//authority"
        int fileStart;
        if (uri.regionMatches(start, "//", 0, 2)) {
            int authorityStart = start + 2;
            fileStart = UrlUtils.findFirstOf(uri, "/?", authorityStart, fragmentStart);
            if (authorityStart == uri.length()) {
                throw new URISyntaxException(uri, "Authority expected", uri.length());
            }
            if (authorityStart < fileStart) {
                authority = AUTHORITY_ENCODER.validate(uri, authorityStart, fileStart, "authority");
            }
        } else {
            fileStart = start;
        }

        // "path"
        int queryStart = UrlUtils.findFirstOf(uri, "?", fileStart, fragmentStart);
        path = PATH_ENCODER.validate(uri, fileStart, queryStart, "path");

        // "?query"
        if (queryStart < fragmentStart) {
            query = ALL_LEGAL_ENCODER.validate(uri, queryStart + 1, fragmentStart, "query");
        }

        parseAuthority(forceServer);
    }

    private String validateScheme(String uri, int end) throws URISyntaxException {
        if (end == 0) {
            throw new URISyntaxException(uri, "Scheme expected", 0);
        }

        for (int i = 0; i < end; i++) {
            if (!UrlUtils.isValidSchemeChar(i, uri.charAt(i))) {
                throw new URISyntaxException(uri, "Illegal character in scheme", 0);
            }
        }

        return uri.substring(0, end);
    }

    /**
     * Breaks this URI's authority into user info, host and port parts.
     *   [user-info@][host][:port]
     * If any part of this fails this method will give up and potentially leave
     * these fields with their default values.
     *
     * @param forceServer true to always throw if the authority cannot be
     *     parsed. If false, this method may still throw for some kinds of
     *     errors; this unpredictable behavior is consistent with the RI.
     */
    private void parseAuthority(boolean forceServer) throws URISyntaxException {
        if (authority == null) {
            return;
        }

        String tempUserInfo = null;
        String temp = authority;
        int index = temp.indexOf('@');
        int hostIndex = 0;
        if (index != -1) {
            // remove user info
            tempUserInfo = temp.substring(0, index);
            validateUserInfo(authority, tempUserInfo, 0);
            temp = temp.substring(index + 1); // host[:port] is left
            hostIndex = index + 1;
        }

        index = temp.lastIndexOf(':');
        int endIndex = temp.indexOf(']');

        String tempHost;
        int tempPort = -1;
        if (index != -1 && endIndex < index) {
            // determine port and host
            tempHost = temp.substring(0, index);

            if (index < (temp.length() - 1)) { // port part is not empty
                try {
                    tempPort = Integer.parseInt(temp.substring(index + 1));
                    if (tempPort < 0) {
                        if (forceServer) {
                            throw new URISyntaxException(authority,
                                    "Invalid port number", hostIndex + index + 1);
                        }
                        return;
                    }
                } catch (NumberFormatException e) {
                    if (forceServer) {
                        throw new URISyntaxException(authority,
                                "Invalid port number", hostIndex + index + 1);
                    }
                    return;
                }
            }
        } else {
            tempHost = temp;
        }

        if (tempHost.isEmpty()) {
            if (forceServer) {
                throw new URISyntaxException(authority, "Expected host", hostIndex);
            }
            return;
        }

        if (!isValidHost(forceServer, tempHost)) {
            return;
        }

        // this is a server based uri,
        // fill in the userInfo, host and port fields
        userInfo = tempUserInfo;
        host = tempHost;
        port = tempPort;
        serverAuthority = true;
    }

    private void validateUserInfo(String uri, String userInfo, int index)
            throws URISyntaxException {
        for (int i = 0; i < userInfo.length(); i++) {
            char ch = userInfo.charAt(i);
            if (ch == ']' || ch == '[') {
                throw new URISyntaxException(uri, "Illegal character in userInfo", index + i);
            }
        }
    }

    /**
     * Returns true if {@code host} is a well-formed host name or IP address.
     *
     * @param forceServer true to always throw if the host cannot be parsed. If
     *     false, this method may still throw for some kinds of errors; this
     *     unpredictable behavior is consistent with the RI.
     */
    private boolean isValidHost(boolean forceServer, String host) throws URISyntaxException {
        if (host.startsWith("[")) {
            // IPv6 address
            if (!host.endsWith("]")) {
                throw new URISyntaxException(host,
                        "Expected a closing square bracket for IPv6 address", 0);
            }
            if (InetAddress.isNumeric(host)) {
                // If it's numeric, the presence of square brackets guarantees
                // that it's a numeric IPv6 address.
                return true;
            }
            throw new URISyntaxException(host, "Malformed IPv6 address");
        }

        // '[' and ']' can only be the first char and last char
        // of the host name
        if (host.indexOf('[') != -1 || host.indexOf(']') != -1) {
            throw new URISyntaxException(host, "Illegal character in host name", 0);
        }

        int index = host.lastIndexOf('.');
        if (index < 0 || index == host.length() - 1
                || !Character.isDigit(host.charAt(index + 1))) {
            // domain name
            if (isValidDomainName(host)) {
                return true;
            }
            if (forceServer) {
                throw new URISyntaxException(host, "Illegal character in host name", 0);
            }
            return false;
        }

        // IPv4 address?
        try {
            InetAddress ia = InetAddress.parseNumericAddress(host);
            if (ia instanceof Inet4Address) {
                return true;
            }
        } catch (IllegalArgumentException ignored) {
        }

        if (forceServer) {
            throw new URISyntaxException(host, "Malformed IPv4 address", 0);
        }
        return false;
    }

    private boolean isValidDomainName(String host) {
        try {
            UriCodec.validateSimple(host, "-.");
        } catch (URISyntaxException e) {
            return false;
        }

        String lastLabel = null;
        for (String token : host.split("\\.")) {
            lastLabel = token;
            if (lastLabel.startsWith("-") || lastLabel.endsWith("-")) {
                return false;
            }
        }

        if (lastLabel == null) {
            return false;
        }

        if (!lastLabel.equals(host)) {
            char ch = lastLabel.charAt(0);
            if (ch >= '0' && ch <= '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares this URI with the given argument {@code uri}. This method will
     * return a negative value if this URI instance is less than the given
     * argument and a positive value if this URI instance is greater than the
     * given argument. The return value {@code 0} indicates that the two
     * instances represent the same URI. To define the order the single parts of
     * the URI are compared with each other. String components will be ordered
     * in the natural case-sensitive way. A hierarchical URI is less than an
     * opaque URI and if one part is {@code null} the URI with the undefined
     * part is less than the other one.
     *
     * @param uri
     *            the URI this instance has to compare with.
     * @return the value representing the order of the two instances.
     */
    public int compareTo(URI uri) {
        int ret;

        // compare schemes
        if (scheme == null && uri.scheme != null) {
            return -1;
        } else if (scheme != null && uri.scheme == null) {
            return 1;
        } else if (scheme != null && uri.scheme != null) {
            ret = scheme.compareToIgnoreCase(uri.scheme);
            if (ret != 0) {
                return ret;
            }
        }

        // compare opacities
        if (!opaque && uri.opaque) {
            return -1;
        } else if (opaque && !uri.opaque) {
            return 1;
        } else if (opaque && uri.opaque) {
            ret = schemeSpecificPart.compareTo(uri.schemeSpecificPart);
            if (ret != 0) {
                return ret;
            }
        } else {

            // otherwise both must be hierarchical

            // compare authorities
            if (authority != null && uri.authority == null) {
                return 1;
            } else if (authority == null && uri.authority != null) {
                return -1;
            } else if (authority != null && uri.authority != null) {
                if (host != null && uri.host != null) {
                    // both are server based, so compare userInfo, host, port
                    if (userInfo != null && uri.userInfo == null) {
                        return 1;
                    } else if (userInfo == null && uri.userInfo != null) {
                        return -1;
                    } else if (userInfo != null && uri.userInfo != null) {
                        ret = userInfo.compareTo(uri.userInfo);
                        if (ret != 0) {
                            return ret;
                        }
                    }

                    // userInfo's are the same, compare hostname
                    ret = host.compareToIgnoreCase(uri.host);
                    if (ret != 0) {
                        return ret;
                    }

                    // compare port
                    if (port != uri.port) {
                        return port - uri.port;
                    }
                } else { // one or both are registry based, compare the whole
                    // authority
                    ret = authority.compareTo(uri.authority);
                    if (ret != 0) {
                        return ret;
                    }
                }
            }

            // authorities are the same
            // compare paths
            ret = path.compareTo(uri.path);
            if (ret != 0) {
                return ret;
            }

            // compare queries

            if (query != null && uri.query == null) {
                return 1;
            } else if (query == null && uri.query != null) {
                return -1;
            } else if (query != null && uri.query != null) {
                ret = query.compareTo(uri.query);
                if (ret != 0) {
                    return ret;
                }
            }
        }

        // everything else is identical, so compare fragments
        if (fragment != null && uri.fragment == null) {
            return 1;
        } else if (fragment == null && uri.fragment != null) {
            return -1;
        } else if (fragment != null && uri.fragment != null) {
            ret = fragment.compareTo(uri.fragment);
            if (ret != 0) {
                return ret;
            }
        }

        // identical
        return 0;
    }

    /**
     * Returns the URI formed by parsing {@code uri}. This method behaves
     * identically to the string constructor but throws a different exception
     * on failure. The constructor fails with a checked {@link
     * URISyntaxException}; this method fails with an unchecked {@link
     * IllegalArgumentException}.
     */
    public static URI create(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private URI duplicate() {
        URI clone = new URI();
        clone.absolute = absolute;
        clone.authority = authority;
        clone.fragment = fragment;
        clone.host = host;
        clone.opaque = opaque;
        clone.path = path;
        clone.port = port;
        clone.query = query;
        clone.scheme = scheme;
        clone.schemeSpecificPart = schemeSpecificPart;
        clone.userInfo = userInfo;
        clone.serverAuthority = serverAuthority;
        return clone;
    }

    /*
     * Takes a string that may contain hex sequences like %F1 or %2b and
     * converts the hex values following the '%' to lowercase
     */
    private String convertHexToLowerCase(String s) {
        StringBuilder result = new StringBuilder("");
        if (s.indexOf('%') == -1) {
            return s;
        }

        int index, prevIndex = 0;
        while ((index = s.indexOf('%', prevIndex)) != -1) {
            result.append(s.substring(prevIndex, index + 1));
            result.append(s.substring(index + 1, index + 3).toLowerCase(Locale.US));
            index += 3;
            prevIndex = index;
        }
        return result.toString();
    }

    /**
     * Returns true if the given URI escaped strings {@code first} and {@code second} are
     * equal.
     *
     * TODO: This method assumes that both strings are escaped using the same escape rules
     * yet it still performs case insensitive comparison of the escaped sequences.
     * Why is this necessary ? We can just replace it with first.equals(second)
     * otherwise.
     */
    private boolean escapedEquals(String first, String second) {
        // This length test isn't a micro-optimization. We need it because we sometimes
        // calculate the number of characters to match based on the length of the second
        // string. If the second string is shorter than the first, we might attempt to match
        // 0 chars, and regionMatches is specified to return true in that case.
        if (first.length() != second.length()) {
            return false;
        }

        int prevIndex = 0;
        while (true) {
            int index = first.indexOf('%', prevIndex);
            int index1 = second.indexOf('%', prevIndex);
            if (index != index1) {
                return false;
            }

            // index == index1 from this point on.

            if (index == -1) {
                // No more escapes, match the remainder of the string
                // normally.
               return first.regionMatches(prevIndex, second, prevIndex,
                       second.length() - prevIndex);
            }

            if (!first.regionMatches(prevIndex, second, prevIndex, (index - prevIndex))) {
                return false;
            }

            if (!first.regionMatches(true /* ignore case */, index + 1, second, index + 1, 2)) {
                return false;
            }

            index += 3;
            prevIndex = index;
        }
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof URI)) {
            return false;
        }
        URI uri = (URI) o;

        if (uri.fragment == null && fragment != null || uri.fragment != null
                && fragment == null) {
            return false;
        } else if (uri.fragment != null && fragment != null) {
            if (!escapedEquals(uri.fragment, fragment)) {
                return false;
            }
        }

        if (uri.scheme == null && scheme != null || uri.scheme != null
                && scheme == null) {
            return false;
        } else if (uri.scheme != null && scheme != null) {
            if (!uri.scheme.equalsIgnoreCase(scheme)) {
                return false;
            }
        }

        if (uri.opaque && opaque) {
            return escapedEquals(uri.schemeSpecificPart,
                    schemeSpecificPart);
        } else if (!uri.opaque && !opaque) {
            if (!escapedEquals(path, uri.path)) {
                return false;
            }

            if (uri.query != null && query == null || uri.query == null
                    && query != null) {
                return false;
            } else if (uri.query != null && query != null) {
                if (!escapedEquals(uri.query, query)) {
                    return false;
                }
            }

            if (uri.authority != null && authority == null
                    || uri.authority == null && authority != null) {
                return false;
            } else if (uri.authority != null && authority != null) {
                if (uri.host != null && host == null || uri.host == null
                        && host != null) {
                    return false;
                } else if (uri.host == null && host == null) {
                    // both are registry based, so compare the whole authority
                    return escapedEquals(uri.authority, authority);
                } else { // uri.host != null && host != null, so server-based
                    if (!host.equalsIgnoreCase(uri.host)) {
                        return false;
                    }

                    if (port != uri.port) {
                        return false;
                    }

                    if (uri.userInfo != null && userInfo == null
                            || uri.userInfo == null && userInfo != null) {
                        return false;
                    } else if (uri.userInfo != null && userInfo != null) {
                        return escapedEquals(userInfo, uri.userInfo);
                    } else {
                        return true;
                    }
                }
            } else {
                // no authority
                return true;
            }

        } else {
            // one is opaque, the other hierarchical
            return false;
        }
    }

    /**
     * Returns the scheme of this URI, or null if this URI has no scheme. This
     * is also known as the protocol.
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Returns the decoded scheme-specific part of this URI, or null if this URI
     * has no scheme-specific part.
     */
    public String getSchemeSpecificPart() {
        return decode(schemeSpecificPart);
    }

    /**
     * Returns the encoded scheme-specific part of this URI, or null if this URI
     * has no scheme-specific part.
     */
    public String getRawSchemeSpecificPart() {
        return schemeSpecificPart;
    }

    /**
     * Returns the decoded authority part of this URI, or null if this URI has
     * no authority.
     */
    public String getAuthority() {
        return decode(authority);
    }

    /**
     * Returns the encoded authority of this URI, or null if this URI has no
     * authority.
     */
    public String getRawAuthority() {
        return authority;
    }

    /**
     * Returns the decoded user info of this URI, or null if this URI has no
     * user info.
     */
    public String getUserInfo() {
        return decode(userInfo);
    }

    /**
     * Returns the encoded user info of this URI, or null if this URI has no
     * user info.
     */
    public String getRawUserInfo() {
        return userInfo;
    }

    /**
     * Returns the host of this URI, or null if this URI has no host.
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the port number of this URI, or {@code -1} if this URI has no
     * explicit port.
     */
    public int getPort() {
        return port;
    }

    /** @hide */
    public int getEffectivePort() {
        return getEffectivePort(scheme, port);
    }

    /**
     * Returns the port to use for {@code scheme} connections will use when
     * {@link #getPort} returns {@code specifiedPort}.
     *
     * @hide
     */
    public static int getEffectivePort(String scheme, int specifiedPort) {
        if (specifiedPort != -1) {
            return specifiedPort;
        }

        if ("http".equalsIgnoreCase(scheme)) {
            return 80;
        } else if ("https".equalsIgnoreCase(scheme)) {
            return 443;
        } else {
            return -1;
        }
    }

    /**
     * Returns the decoded path of this URI, or null if this URI has no path.
     */
    public String getPath() {
        return decode(path);
    }

    /**
     * Returns the encoded path of this URI, or null if this URI has no path.
     */
    public String getRawPath() {
        return path;
    }

    /**
     * Returns the decoded query of this URI, or null if this URI has no query.
     */
    public String getQuery() {
        return decode(query);
    }

    /**
     * Returns the encoded query of this URI, or null if this URI has no query.
     */
    public String getRawQuery() {
        return query;
    }

    /**
     * Returns the decoded fragment of this URI, or null if this URI has no
     * fragment.
     */
    public String getFragment() {
        return decode(fragment);
    }

    /**
     * Gets the encoded fragment of this URI, or null if this URI has no
     * fragment.
     */
    public String getRawFragment() {
        return fragment;
    }

    @Override public int hashCode() {
        if (hash == -1) {
            hash = getHashString().hashCode();
        }
        return hash;
    }

    /**
     * Returns true if this URI is absolute, which means that a scheme is
     * defined.
     */
    public boolean isAbsolute() {
        // TODO: simplify to 'scheme != null' ?
        return absolute;
    }

    /**
     * Returns true if this URI is opaque. Opaque URIs are absolute and have a
     * scheme-specific part that does not start with a slash character. All
     * parts except scheme, scheme-specific and fragment are undefined.
     */
    public boolean isOpaque() {
        return opaque;
    }

    /**
     * Returns the normalized path.
     */
    private String normalize(String path, boolean discardRelativePrefix) {
        path = UrlUtils.canonicalizePath(path, discardRelativePrefix);

        /*
         * If the path contains a colon before the first colon, prepend
         * "./" to differentiate the path from a scheme prefix.
         */
        int colon = path.indexOf(':');
        if (colon != -1) {
            int slash = path.indexOf('/');
            if (slash == -1 || colon < slash) {
                path = "./" + path;
            }
        }

        return path;
    }

    /**
     * Normalizes the path part of this URI.
     *
     * @return an URI object which represents this instance with a normalized
     *         path.
     */
    public URI normalize() {
        if (opaque) {
            return this;
        }
        String normalizedPath = normalize(path, false);
        // if the path is already normalized, return this
        if (path.equals(normalizedPath)) {
            return this;
        }
        // get an exact copy of the URI re-calculate the scheme specific part
        // since the path of the normalized URI is different from this URI.
        URI result = duplicate();
        result.path = normalizedPath;
        result.setSchemeSpecificPart();
        return result;
    }

    /**
     * Tries to parse the authority component of this URI to divide it into the
     * host, port, and user-info. If this URI is already determined as a
     * ServerAuthority this instance will be returned without changes.
     *
     * @return this instance with the components of the parsed server authority.
     * @throws URISyntaxException
     *             if the authority part could not be parsed as a server-based
     *             authority.
     */
    public URI parseServerAuthority() throws URISyntaxException {
        if (!serverAuthority) {
            parseAuthority(true);
        }
        return this;
    }

    /**
     * Makes the given URI {@code relative} to a relative URI against the URI
     * represented by this instance.
     *
     * @param relative
     *            the URI which has to be relativized against this URI.
     * @return the relative URI.
     */
    public URI relativize(URI relative) {
        if (relative.opaque || opaque) {
            return relative;
        }

        if (scheme == null ? relative.scheme != null : !scheme
                .equals(relative.scheme)) {
            return relative;
        }

        if (authority == null ? relative.authority != null : !authority
                .equals(relative.authority)) {
            return relative;
        }

        // normalize both paths
        String thisPath = normalize(path, false);
        String relativePath = normalize(relative.path, false);

        /*
         * if the paths aren't equal, then we need to determine if this URI's
         * path is a parent path (begins with) the relative URI's path
         */
        if (!thisPath.equals(relativePath)) {
            // drop everything after the last slash in this path
            thisPath = thisPath.substring(0, thisPath.lastIndexOf('/') + 1);

            /*
             * if the relative URI's path doesn't start with this URI's path,
             * then just return the relative URI; the URIs have nothing in
             * common
             */
            if (!relativePath.startsWith(thisPath)) {
                return relative;
            }
        }

        URI result = new URI();
        result.fragment = relative.fragment;
        result.query = relative.query;
        // the result URI is the remainder of the relative URI's path
        result.path = relativePath.substring(thisPath.length());
        result.setSchemeSpecificPart();
        return result;
    }

    /**
     * Resolves the given URI {@code relative} against the URI represented by
     * this instance.
     *
     * @param relative
     *            the URI which has to be resolved against this URI.
     * @return the resolved URI.
     */
    public URI resolve(URI relative) {
        if (relative.absolute || opaque) {
            return relative;
        }

        if (relative.authority != null) {
            // If the relative URI has an authority, the result is the relative
            // with this URI's scheme.
            URI result = relative.duplicate();
            result.scheme = scheme;
            result.absolute = absolute;
            return result;
        }

        if (relative.path.isEmpty() && relative.scheme == null && relative.query == null) {
            // if the relative URI only consists of at most a fragment,
            URI result = duplicate();
            result.fragment = relative.fragment;
            return result;
        }

        URI result = duplicate();
        result.fragment = relative.fragment;
        result.query = relative.query;
        String resolvedPath;
        if (relative.path.startsWith("/")) {
            // The relative URI has an absolute path; use it.
            resolvedPath = relative.path;
        } else if (relative.path.isEmpty()) {
            // The relative URI has no path; use the base path.
            resolvedPath = path;
        } else {
            // The relative URI has a relative path; combine the paths.
            int endIndex = path.lastIndexOf('/') + 1;
            resolvedPath = path.substring(0, endIndex) + relative.path;
        }
        result.path = UrlUtils.authoritySafePath(result.authority, normalize(resolvedPath, true));
        result.setSchemeSpecificPart();
        return result;
    }

    /**
     * Helper method used to re-calculate the scheme specific part of the
     * resolved or normalized URIs
     */
    private void setSchemeSpecificPart() {
        // ssp = [//authority][path][?query]
        StringBuilder ssp = new StringBuilder();
        if (authority != null) {
            ssp.append("//" + authority);
        }
        if (path != null) {
            ssp.append(path);
        }
        if (query != null) {
            ssp.append("?" + query);
        }
        schemeSpecificPart = ssp.toString();
        // reset string, so that it can be re-calculated correctly when asked.
        string = null;
    }

    /**
     * Creates a new URI instance by parsing the given string {@code relative}
     * and resolves the created URI against the URI represented by this
     * instance.
     *
     * @param relative
     *            the given string to create the new URI instance which has to
     *            be resolved later on.
     * @return the created and resolved URI.
     */
    public URI resolve(String relative) {
        return resolve(create(relative));
    }

    private String decode(String s) {
        return s != null ? UriCodec.decode(s) : null;
    }

    /**
     * Returns the textual string representation of this URI instance using the
     * US-ASCII encoding.
     *
     * @return the US-ASCII string representation of this URI.
     */
    public String toASCIIString() {
        StringBuilder result = new StringBuilder();
        ASCII_ONLY.appendEncoded(result, toString());
        return result.toString();
    }

    /**
     * Returns the encoded URI.
     */
    @Override public String toString() {
        if (string != null) {
            return string;
        }

        StringBuilder result = new StringBuilder();
        if (scheme != null) {
            result.append(scheme);
            result.append(':');
        }
        if (opaque) {
            result.append(schemeSpecificPart);
        } else {
            if (authority != null) {
                result.append("//");
                result.append(authority);
            }

            if (path != null) {
                result.append(path);
            }

            if (query != null) {
                result.append('?');
                result.append(query);
            }
        }

        if (fragment != null) {
            result.append('#');
            result.append(fragment);
        }

        string = result.toString();
        return string;
    }

    /*
     * Form a string from the components of this URI, similarly to the
     * toString() method. But this method converts scheme and host to lowercase,
     * and converts escaped octets to lowercase.
     */
    private String getHashString() {
        StringBuilder result = new StringBuilder();
        if (scheme != null) {
            result.append(scheme.toLowerCase(Locale.US));
            result.append(':');
        }
        if (opaque) {
            result.append(schemeSpecificPart);
        } else {
            if (authority != null) {
                result.append("//");
                if (host == null) {
                    result.append(authority);
                } else {
                    if (userInfo != null) {
                        result.append(userInfo + "@");
                    }
                    result.append(host.toLowerCase(Locale.US));
                    if (port != -1) {
                        result.append(":" + port);
                    }
                }
            }

            if (path != null) {
                result.append(path);
            }

            if (query != null) {
                result.append('?');
                result.append(query);
            }
        }

        if (fragment != null) {
            result.append('#');
            result.append(fragment);
        }

        return convertHexToLowerCase(result.toString());
    }

    /**
     * Converts this URI instance to a URL.
     *
     * @return the created URL representing the same resource as this URI.
     * @throws MalformedURLException
     *             if an error occurs while creating the URL or no protocol
     *             handler could be found.
     */
    public URL toURL() throws MalformedURLException {
        if (!absolute) {
            throw new IllegalArgumentException("URI is not absolute: " + toString());
        }
        return new URL(toString());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        try {
            parseURI(string, false);
        } catch (URISyntaxException e) {
            throw new IOException(e.toString());
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException {
        // call toString() to ensure the value of string field is calculated
        toString();
        out.defaultWriteObject();
    }
}
