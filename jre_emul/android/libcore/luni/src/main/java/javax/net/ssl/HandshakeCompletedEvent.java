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

package javax.net.ssl;

import java.security.Principal;
import java.security.cert.Certificate;
import java.util.EventObject;
import javax.security.cert.X509Certificate;

/**
 * The event object encapsulating the information about a completed SSL
 * handshake on a SSL connection.
 */
public class HandshakeCompletedEvent extends EventObject {

    private transient SSLSession session;

    /**
     * Creates a new {@code HandshakeCompletedEvent} with the specified SSL
     * socket and SSL session.
     *
     * @param sock
     *            the SSL socket.
     * @param s
     *            the SSL session.
     */
    public HandshakeCompletedEvent(SSLSocket sock, SSLSession s) {
        super(sock);
        session = s;
    }

    /**
     * Returns the SSL session associated with this event.
     *
     * @return the SSL session associated with this event.
     */
    public SSLSession getSession() {
        return session;
    }

    /**
     * Returns the name of the cipher suite negotiated during this handshake.
     *
     * @return the name of the cipher suite negotiated during this handshake.
     */
    public String getCipherSuite() {
        return session.getCipherSuite();
    }

    /**
     * Returns the list of local certificates used during the handshake. These
     * certificates were sent to the peer.
     *
     * @return Returns the list of certificates used during the handshake with
     *         the local identity certificate followed by CAs, or {@code null}
     *         if no certificates were used during the handshake.
     */
    public Certificate[] getLocalCertificates() {
        return session.getLocalCertificates();
    }

    /**
     * Return the list of certificates identifying the peer during the
     * handshake.
     *
     * @return the list of certificates identifying the peer with the peer's
     *         identity certificate followed by CAs.
     * @throws SSLPeerUnverifiedException
     *             if the identity of the peer has not been verified.
     */
    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException {
        return session.getPeerCertificates();
    }

    /**
     * Returns the list of certificates identifying the peer. The peer's
     * identity certificate is followed by the validated certificate authority
     * certificates.
     * <p>
     * <b>Replaced by:</b> {@link #getPeerCertificates()}
     *
     * @return the list of certificates identifying the peer
     * @throws SSLPeerUnverifiedException
     *             if the identity of the peer has not been verified.
     */
    public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException {
        return session.getPeerCertificateChain();
    }

    /**
     * Returns the {@code Principal} identifying the peer.
     *
     * @return the {@code Principal} identifying the peer.
     * @throws SSLPeerUnverifiedException
     *             if the identity of the peer has not been verified.
     */
    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        return session.getPeerPrincipal();
    }

    /**
     * Returns the {@code Principal} used to identify during the handshake.
     *
     * @return the {@code Principal} used to identify during the handshake.
     */
    public Principal getLocalPrincipal() {
        return session.getLocalPrincipal();
    }

    /**
     * Returns the SSL socket that produced this event.
     *
     * @return the SSL socket that produced this event.
     */
    public SSLSocket getSocket() {
        return (SSLSocket) this.source;
    }

}
