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
import javax.security.cert.X509Certificate;

/**
 * The interface representing an SSL session.
 */
public interface SSLSession {

    /**
     * Returns the maximum size that an application buffer can be for this
     * session.
     *
     * @return the maximum application buffer size.
     */
    public int getApplicationBufferSize();

    /**
     * Returns the name of the cipher suite used in this session.
     *
     * @return the name of the cipher suite used in this session.
     */
    public String getCipherSuite();

    /**
     * Returns the time this session was created, in milliseconds since midnight
     * January 1st 1970 UTC.
     *
     * @return the time the session was created.
     */
    public long getCreationTime();

    /**
     * Returns this sessions identifier.
     *
     * @return this sessions identifier.
     */
    public byte[] getId();

    /**
     * Returns the time this session was last accessed, in milliseconds since
     * midnight January 1st 1970 UTC.
     *
     * @return the time this session was last accessed.
     */
    public long getLastAccessedTime();

    /**
     * Returns the list of certificates that were used to identify the local
     * side to the peer during the handshake.
     *
     * @return the list of certificates, ordered from local certificate to
     *         CA's certificates.
     */
    public Certificate[] getLocalCertificates();

    /**
     * Returns the principal used to identify the local side to the peer during
     * the handshake.
     *
     * @return the principal used to identify the local side.
     */
    public Principal getLocalPrincipal();

    /**
     * Returns the maximum size that a network buffer can be for this session.
     *
     * @return the maximum network buffer size.
     */
    public int getPacketBufferSize();

    /**
     * Returns the list of certificates the peer used to identify itself during
     * the handshake.
     * <p>
     * Note: this method exists for compatility reasons, use
     * {@link #getPeerCertificates()} instead.
     *
     * @return the list of certificates, ordered from the identity certificate to
     *         the CA's certificates
     * @throws SSLPeerUnverifiedException
     *             if the identity of the peer is not verified.
     */
    public X509Certificate[] getPeerCertificateChain() throws SSLPeerUnverifiedException;

    /**
     * Returns the list of certificates the peer used to identify itself during
     * the handshake.
     *
     * @return the list of certificates, ordered from the identity certificate to
     *         the CA's certificates.
     * @throws SSLPeerUnverifiedException
     *             if the identity of the peer is not verified.
     */
    public Certificate[] getPeerCertificates() throws SSLPeerUnverifiedException;

    /**
     * Returns the host name of the peer of this session. The host name is not
     * authenticated.
     *
     * @return the host name of the peer of this session, or {@code null} if no
     *         host name is available.
     */
    public String getPeerHost();

    /**
     * Returns the port number of the peer of this session. The port number is
     * not authenticated.
     *
     * @return the port number of the peer, of {@code -1} is no port number is
     *         available.
     */
    public int getPeerPort();

    /**
     * Returns the principal identifying the peer during the handshake.
     *
     * @return the principal identifying the peer.
     * @throws SSLPeerUnverifiedException
     *             if the identity of the peer has not been verified.
     */
    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException;

    /**
     * Returns the protocol name that is used for all connections in this
     * session.
     *
     * @return the protocol name that is used for all connections in this
     *         session.
     */
    public String getProtocol();

    /**
     * Returns the context of this session, or null if no context is available.
     */
    public SSLSessionContext getSessionContext();

    /**
     * Returns the object bound to the specified name in this session's
     * application layer data.
     *
     * @param name
     *            the name of the bound value.
     * @return the value bound to the specified name, or {@code null} if the
     *         specified name does not exist or is not accessible in the current
     *         access control context.
     * @throws IllegalArgumentException
     *             if {@code name} is {@code null}.
     */
    public Object getValue(String name);

    /**
     * Returns the list of the object names bound to this session's application
     * layer data..
     * <p>
     * Depending on the current access control context, the list of object names
     * may be different.
     *
     * @return the list of the object names bound to this session's application
     *         layer data.
     */
    public String[] getValueNames();

    /**
     * Invalidates this session.
     * <p>
     * No new connections can be created, but any existing connection remains
     * valid until it is closed.
     */
    public void invalidate();

    /**
     * Returns whether this session is valid.
     *
     * @return {@code true} if this session is valid, otherwise {@code false}.
     */
    public boolean isValid();

    /**
     * Binds the specified object under the specified name in this session's
     * application layer data.
     * <p>
     * For bindings (new or existing) implementing the
     * {@code SSLSessionBindingListener} interface the object will be notified.
     *
     * @param name
     *            the name to bind the object to.
     * @param value
     *            the object to bind.
     * @throws IllegalArgumentException
     *             if either {@code name} or {@code value} is {@code null}.
     */
    public void putValue(String name, Object value);

    /**
     * Removes the binding for the specified name in this session's application
     * layer data. If the existing binding implements the
     * {@code SSLSessionBindingListener} interface the object will be notified.
     *
     * @param name
     *            the binding to remove.
     * @throws IllegalArgumentException
     *             if {@code name} is {@code null}.
     */
    public void removeValue(String name);
}
