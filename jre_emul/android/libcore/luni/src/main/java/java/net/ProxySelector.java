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

/**
 * Selects the proxy server to use, if any, when connecting to a given URL.
 *
 * <h3>System Properties</h3>
 * <p>The default proxy selector is configured by system properties.
 *
 * <table border="1" cellpadding="3" cellspacing="0">
 * <tr class="TableHeadingColor"><th colspan="4">Hostname patterns</th></tr>
 * <tr><th>URL scheme</th><th>property name</th><th>description</th><th>default</th></tr>
 * <tr><td>ftp</td><td>ftp.nonProxyHosts</td><td>Hostname pattern for FTP servers to connect to
 *     directly (without a proxy).</td><td></td></tr>
 * <tr><td>http</td><td>http.nonProxyHosts</td><td>Hostname pattern for HTTP servers to connect to
 *     directly (without a proxy).</td><td></td></tr>
 * <tr><td>https</td><td>https.nonProxyHosts</td><td>Hostname pattern for HTTPS servers to connect
 *     to directly (without a proxy).</td><td></td></tr>
 * <tr><td colspan="4"><br></td></tr>
 *
 * <tr class="TableHeadingColor"><th colspan="4">{@linkplain Proxy.Type#HTTP HTTP Proxies}</th></tr>
 * <tr><th>URL scheme</th><th>property name</th><th>description</th><th>default</th></tr>
 * <tr><td rowspan="2">ftp</td><td>ftp.proxyHost</td><td>Hostname of the HTTP proxy server used for
 *     FTP requests.</td><td></td></tr>
 * <tr><td>ftp.proxyPort</td><td>Port number of the HTTP proxy server used for FTP
 *     requests.</td><td>80</td></tr>
 * <tr><td rowspan="2">http</td><td>http.proxyHost</td><td>Hostname of the HTTP proxy server used
 *     for HTTP requests.</td><td></td></tr>
 * <tr><td>http.proxyPort</td><td>Port number of the HTTP proxy server used for HTTP
 *     requests.</td><td>80</td></tr>
 * <tr><td rowspan="2">https</td><td>https.proxyHost</td><td>Hostname of the HTTP proxy server used
 *     for HTTPS requests.</td><td></td></tr>
 * <tr><td>https.proxyPort</td><td>Port number of the HTTP proxy server used for HTTPS
 *     requests.</td><td>443</td></tr>
 * <tr><td rowspan="2">ftp, http or https</td><td>proxyHost</td><td>Hostname of the HTTP proxy
 *     server used for FTP, HTTP and HTTPS requests.</td><td></td></tr>
 * <tr><td>proxyPort</td><td>Port number of the HTTP proxy server.</td><td>80 for FTP and HTTP
 *     <br>443 for HTTPS</td></tr>
 * <tr><td colspan="4"><br></td></tr>
 *
 * <tr class="TableHeadingColor"><th colspan="4">{@linkplain Proxy.Type#SOCKS SOCKS
 *     Proxies}</th></tr>
 * <tr><th>URL scheme</th><th>property name</th><th>description</th><th>default</th></tr>
 * <tr><td rowspan="2">ftp, http, https or socket</td><td>socksProxyHost</td><td>Hostname of the
 *     SOCKS proxy server used for FTP, HTTP, HTTPS and raw sockets.<br>Raw socket URLs are of the
 *     form <code>socket://<i>host</i>:<i>port</i></code></td><td></td></tr>
 * <tr><td>socksProxyPort</td><td>Port number of the SOCKS proxy server.</td><td>1080</td></tr>
 * </table>
 *
 * <p>Hostname patterns specify which hosts should be connected to directly,
 * ignoring any other proxy system properties. If the URL's host matches the
 * corresponding hostname pattern, {@link Proxy#NO_PROXY} is returned.
 *
 * <p>The format of a hostname pattern is a list of hostnames that are
 * separated by {@code |} and that use {@code *} as a wildcard. For example,
 * setting the {@code http.nonProxyHosts} property to {@code
 * *.android.com|*.kernel.org} will cause requests to {@code
 * http://developer.android.com} to be made without a proxy.
 *
 * <p>The default proxy selector always returns exactly one proxy. If no proxy
 * is applicable, {@link Proxy#NO_PROXY} is returned. If multiple proxies are
 * applicable, such as when both the {@code proxyHost} and {@code
 * socksProxyHost} system properties are set, the result is the property listed
 * earliest in the table above.
 *
 * <h3>Alternatives</h3>
 * <p>To request a URL without involving the system proxy selector, explicitly
 * specify a proxy or {@link Proxy#NO_PROXY} using {@link
 * URL#openConnection(Proxy)}.
 *
 * <p>Use {@link ProxySelector#setDefault(ProxySelector)} to install a custom
 * proxy selector.
 */
public abstract class ProxySelector {

    private static ProxySelector defaultSelector = new ProxySelectorImpl();

    /**
     * Returns the default proxy selector, or null if none exists.
     */
    public static ProxySelector getDefault() {
        return defaultSelector;
    }

    /**
     * Sets the default proxy selector. If {@code selector} is null, the current
     * proxy selector will be removed.
     */
    public static void setDefault(ProxySelector selector) {
        defaultSelector = selector;
    }

    /**
     * Returns the proxy servers to use on connections to {@code uri}. This list
     * will contain {@link Proxy#NO_PROXY} if no proxy server should be used.
     *
     * @throws IllegalArgumentException if {@code uri} is null.
     */
    public abstract List<Proxy> select(URI uri);

    /**
     * Notifies this {@code ProxySelector} that a connection to the proxy server
     * could not be established.
     *
     * @param uri the URI to which the connection could not be established.
     * @param address the address of the proxy.
     * @param failure the exception which was thrown during connection
     *     establishment.
     * @throws IllegalArgumentException if any argument is null.
     */
    public abstract void connectFailed(URI uri, SocketAddress address, IOException failure);
}
