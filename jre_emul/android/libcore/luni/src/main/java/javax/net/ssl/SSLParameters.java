/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.net.ssl;

/**
 * SSL handshake parameters that include protocols, cipher suites, and
 * client authentication requirements.
 * @since 1.6
 */
public class SSLParameters {

    private String[] cipherSuites;
    private String[] protocols;
    private boolean needClientAuth;
    private boolean wantClientAuth;

    /**
     * The default SSLParameters constructor. Cipher suites and
     * protocols are initialized to null and client authentication
     * options are initialized to false.
     */
    public SSLParameters() {}

    /**
     * A SSLParameters constructor that allows the values for the
     * initial cipher suites array to be provided. Other values
     * default as specified in {@link #SSLParameters()}.
     *
     * @param cipherSuites An array of cipherSuites that is cloned for
     * use within the SSLParameters, or null.
     */
    public SSLParameters(String[] cipherSuites) {
        setCipherSuites(cipherSuites);
    }

    /**
     * A SSLParameters constructor that allows the values for initial
     * cipher suites and protocols arrays to be provided. Other values
     * default as specified in {@link #SSLParameters()}.
     *
     * @param cipherSuites An array of cipher names that is cloned for
     * use within the SSLParameters, or null.
     * @param protocols An array of protocol names that is cloned for
     * use within the SSLParameters, or null.
     */
    public SSLParameters(String[] cipherSuites,
                         String[] protocols) {
        setCipherSuites(cipherSuites);
        setProtocols(protocols);
    }

    /**
     * Returns a copy of the cipher suites, or null if none have been
     * specified.
     */
    public String[] getCipherSuites() {
        if (cipherSuites == null) {
            return null;
        }
        return cipherSuites.clone();
    }

    /**
     * Sets the cipher suites to a copy of the input, or null
     */
    public void setCipherSuites(String[] cipherSuites) {
        this.cipherSuites = ((cipherSuites == null)
                             ? null
                             : cipherSuites.clone());
    }

    /**
     * Returns a copy of the protocols, or null if none have been
     * specified.
     */
    public String[] getProtocols() {
        if (protocols == null) {
            return null;
        }
        return protocols.clone();
    }

    /**
     * Sets the protocols to a copy of the input, or null
     */
    public void setProtocols(String[] protocols) {
        this.protocols = ((protocols == null)
                             ? null
                             : protocols.clone());
    }

    /**
     * Returns true if a server requires authentication from a client
     * during handshaking. If this returns true, {@link
     * #getWantClientAuth} will return false.
     */
    public boolean getNeedClientAuth () {
        return needClientAuth;
    }

    /**
     * Sets whether or not to a server needs client authentication.
     * After calling this, #getWantClientAuth() will return false.
     */
    public void setNeedClientAuth (boolean needClientAuth) {
        this.needClientAuth = needClientAuth;
        this.wantClientAuth = false;
    }

    /**
     * Returns true if a server optionally wants to authenticate a
     * client during handshaking. If this returns true, {@link
     * #getNeedClientAuth} will return false.
     */
    public boolean getWantClientAuth () {
        return wantClientAuth;
    }

    /**
     * Sets whether or not to a server wants client authentication.
     * After calling this, #getNeedClientAuth() will return false.
     */
    public void setWantClientAuth (boolean wantClientAuth) {
        this.wantClientAuth = wantClientAuth;
        this.needClientAuth = false;
    }
}
