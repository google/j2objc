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

import java.security.Principal;
import java.security.cert.Certificate;
import java.util.List;
import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * A secure cache response represents data which is originally retrieved over a
 * secure connection. Such a connection can be secured by using a cryptographic
 * protocol like TLS or SSL.
 *
 * @see ResponseCache
 */
public abstract class SecureCacheResponse extends CacheResponse {

    /**
     * Creates a new instance of this class.
     */
    public SecureCacheResponse() {
    }

    /**
     * Gets the cipher suite string on the connection which is originally used
     * to retrieve the network resource.
     *
     * @return the cipher suite string.
     */
    public abstract String getCipherSuite();

    /**
     * Gets the local certificate chain. When the original connection retrieved
     * the resource data, this certificate chain was sent to the server during
     * handshaking process. This method only takes effect when certificate-based
     * cipher suite is enabled.
     *
     * @return the certificate chain that was sent to the server. If no
     *         certificate chain was sent, the method returns {@code null}.
     */
    public abstract List<Certificate> getLocalCertificateChain();

    /**
     * Gets the cached server's certificate chain. As part of defining the
     * session, the certificate chain was established when the original
     * connection retrieved network resource. This method can only be invoked
     * when certificated-based cipher suite is enabled. Otherwise, it throws an
     * {@code SSLPeerUnverifiedException}.
     *
     * @return the server's certificate chain.
     * @throws SSLPeerUnverifiedException
     *             if the peer is unverified.
     */
    public abstract List<Certificate> getServerCertificateChain()
            throws SSLPeerUnverifiedException;

    /**
     * Gets the server's principle. When the original connection retrieved
     * network resource, the principle was established when defining the
     * session.
     *
     * @return a principal object representing the server's principal.
     * @throws SSLPeerUnverifiedException
     *             if the peer is unverified.
     */
    public abstract Principal getPeerPrincipal()
            throws SSLPeerUnverifiedException;

    /**
     * Gets the local principle that the original connection sent to the server.
     * When the original connection fetched the network resource, the principle
     * was sent to the server during handshaking process.
     *
     * @return the local principal object being sent to the server. Returns an
     *         {@code X500Principal} object for X509-based cipher suites. If no
     *         principal was sent, it returns {@code null}.
     */
    public abstract Principal getLocalPrincipal();
}
