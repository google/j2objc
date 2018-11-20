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

package libcore.javax.net.ssl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import junit.framework.Assert;
import libcore.java.security.StandardNames;
/* J2ObjC: not implemented
import libcore.java.security.TestKeyStore;
*/

/**
 * TestSSLContext is a convenience class for other tests that
 * want a canned SSLContext and related state for testing so they
 * don't have to duplicate the logic.
 */
public final class TestSSLContext extends Assert {

    /*
     * The RI and Android have very different default SSLSession cache behaviors.
     * The RI keeps an unlimited number of SSLSesions around for 1 day.
     * Android keeps 10 SSLSessions forever.
     */
    private static final boolean IS_RI = StandardNames.IS_RI;
    public static final int EXPECTED_DEFAULT_CLIENT_SSL_SESSION_CACHE_SIZE = (IS_RI) ? 0 : 10;
    public static final int EXPECTED_DEFAULT_SERVER_SSL_SESSION_CACHE_SIZE = (IS_RI) ? 0 : 100;
    public static final int EXPECTED_DEFAULT_SSL_SESSION_CACHE_TIMEOUT =
            (IS_RI) ? 24 * 3600 : 8 * 3600;

    /**
     * The Android SSLSocket and SSLServerSocket implementations are
     * based on a version of OpenSSL which includes support for RFC
     * 4507 session tickets. When using session tickets, the server
     * does not need to keep a cache mapping session IDs to SSL
     * sessions for reuse. Instead, the client presents the server
     * with a session ticket it received from the server earlier,
     * which is an SSL session encrypted by the server's secret
     * key. Since in this case the server does not need to keep a
     * cache, some tests may find different results depending on
     * whether or not the session tickets are in use. These tests can
     * use this function to determine if loopback SSL connections are
     * expected to use session tickets and conditionalize their
     * results appropriately.
     */
    public static boolean sslServerSocketSupportsSessionTickets () {
        // Disabled session tickets for better compatability b/2682876
        // return !IS_RI;
        return false;
    }

    public final KeyStore clientKeyStore;
    public final char[] clientStorePassword;
    public final KeyStore serverKeyStore;
    public final char[] serverStorePassword;
    public final KeyManager[] clientKeyManagers;
    public final KeyManager[] serverKeyManagers;
    public final X509ExtendedTrustManager clientTrustManager;
    public final X509ExtendedTrustManager serverTrustManager;
    public final SSLContext clientContext;
    public final SSLContext serverContext;
    public final SSLServerSocket serverSocket;
    public final InetAddress host;
    public final int port;

    /**
     * Used for replacing the hostname in an InetSocketAddress object during
     * serialization.
     */
    private static class HostnameRewritingObjectOutputStream extends ObjectOutputStream {
        private final String hostname;

        public HostnameRewritingObjectOutputStream(OutputStream out, String hostname)
                throws IOException {
            super(out);
            this.hostname = hostname;
        }

        @Override
        public PutField putFields() throws IOException {
            return new PutFieldProxy(super.putFields(), hostname);
        }

        private static class PutFieldProxy extends ObjectOutputStream.PutField {
            private final PutField delegate;
            private final String hostname;

            public PutFieldProxy(ObjectOutputStream.PutField delegate, String hostname) {
                this.delegate = delegate;
                this.hostname = hostname;
            }

            @Override
            public void put(String name, boolean val) {
                delegate.put(name, val);
            }

            @Override
            public void put(String name, byte val) {
                delegate.put(name, val);
            }

            @Override
            public void put(String name, char val) {
                delegate.put(name, val);
            }

            @Override
            public void put(String name, short val) {
                delegate.put(name, val);
            }

            @Override
            public void put(String name, int val) {
                delegate.put(name, val);
            }

            @Override
            public void put(String name, long val) {
                delegate.put(name, val);
            }

            @Override
            public void put(String name, float val) {
                delegate.put(name, val);
            }

            @Override
            public void put(String name, double val) {
                delegate.put(name, val);
            }

            @Override
            public void put(String name, Object val) {
                if ("hostname".equals(name)) {
                    delegate.put(name, hostname);
                } else {
                    delegate.put(name, val);
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public void write(ObjectOutput out) throws IOException {
                delegate.write(out);
            }
        }
    }

    /**
     * Creates an InetSocketAddress where the hostname points to an arbitrary
     * hostname, but the address points to the loopback address. Useful for
     * testing SNI where both "localhost" and IP addresses are not allowed.
     */
    public InetSocketAddress getLoopbackAsHostname(String hostname, int port) throws IOException, ClassNotFoundException {
        InetSocketAddress addr = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HostnameRewritingObjectOutputStream oos = new HostnameRewritingObjectOutputStream(baos, hostname);
        oos.writeObject(addr);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        return (InetSocketAddress) ois.readObject();
    }

    private TestSSLContext(KeyStore clientKeyStore,
                           char[] clientStorePassword,
                           KeyStore serverKeyStore,
                           char[] serverStorePassword,
                           KeyManager[] clientKeyManagers,
                           KeyManager[] serverKeyManagers,
                           X509ExtendedTrustManager clientTrustManager,
                           X509ExtendedTrustManager serverTrustManager,
                           SSLContext clientContext,
                           SSLContext serverContext,
                           SSLServerSocket serverSocket,
                           InetAddress host,
                           int port) {
        this.clientKeyStore = clientKeyStore;
        this.clientStorePassword = clientStorePassword;
        this.serverKeyStore = serverKeyStore;
        this.serverStorePassword = serverStorePassword;
        this.clientKeyManagers = clientKeyManagers;
        this.serverKeyManagers = serverKeyManagers;
        this.clientTrustManager = clientTrustManager;
        this.serverTrustManager = serverTrustManager;
        this.clientContext = clientContext;
        this.serverContext = serverContext;
        this.serverSocket = serverSocket;
        this.host = host;
        this.port = port;
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Usual TestSSLContext creation method, creates underlying
     * SSLContext with certificate and key as well as SSLServerSocket
     * listening provided host and port.
     */
    public static TestSSLContext create() {
        /* J2ObjC: not implemented
        return create(TestKeyStore.getClient(),
                      TestKeyStore.getServer());
        */
        return null;
    }

    /**
     * TestSSLContext creation method that allows separate creation of server key store
     */
    /* J2ObjC: not implemented
    public static TestSSLContext create(TestKeyStore client, TestKeyStore server) {
        return createWithAdditionalKeyManagers(client, server, null, null);
    }
    */

    /**
     * TestSSLContext creation method that allows separate creation of server key store and
     * the use of additional {@code KeyManager} instances
     */
    /* J2ObjC: not implemented
    public static TestSSLContext createWithAdditionalKeyManagers(
            TestKeyStore client, TestKeyStore server,
            KeyManager[] additionalClientKeyManagers, KeyManager[] additionalServerKeyManagers) {
        String protocol = "TLSv1.2";
        KeyManager[] clientKeyManagers = concat(client.keyManagers, additionalClientKeyManagers);
        KeyManager[] serverKeyManagers = concat(server.keyManagers, additionalServerKeyManagers);
        SSLContext clientContext =
                createSSLContext(protocol, clientKeyManagers, client.trustManagers);
        SSLContext serverContext =
                createSSLContext(protocol, serverKeyManagers, server.trustManagers);
        return create(client.keyStore, client.storePassword,
                      server.keyStore, server.storePassword,
                      clientKeyManagers,
                      serverKeyManagers,
                      client.trustManagers[0],
                      server.trustManagers[0],
                      clientContext,
                      serverContext);
    }
    */

    /**
     * TestSSLContext creation method that allows separate creation of client and server key store
     */
    public static TestSSLContext create(KeyStore clientKeyStore, char[] clientStorePassword,
                                        KeyStore serverKeyStore, char[] serverStorePassword,
                                        KeyManager[] clientKeyManagers,
                                        KeyManager[] serverKeyManagers,
                                        TrustManager clientTrustManagers,
                                        TrustManager serverTrustManagers,
                                        SSLContext clientContext,
                                        SSLContext serverContext) {
        try {
            SSLServerSocket serverSocket = (SSLServerSocket)
                serverContext.getServerSocketFactory().createServerSocket(0);
            InetAddress host = InetAddress.getLocalHost();
            int port = serverSocket.getLocalPort();

            return new TestSSLContext(clientKeyStore, clientStorePassword,
                                      serverKeyStore, serverStorePassword,
                                      clientKeyManagers,
                                      serverKeyManagers,
                                      (X509ExtendedTrustManager) clientTrustManagers,
                                      (X509ExtendedTrustManager) serverTrustManagers,
                                      clientContext, serverContext,
                                      serverSocket, host, port);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a SSLContext with a KeyManager using the private key and
     * certificate chain from the given KeyStore and a TrustManager
     * using the certificates authorities from the same KeyStore.
     */
    public static final SSLContext createSSLContext(final String protocol,
                                                    final KeyManager[] keyManagers,
                                                    final TrustManager[] trustManagers)
    {
        try {
            SSLContext context = SSLContext.getInstance(protocol);
            context.init(keyManagers, trustManagers, new SecureRandom());
            return context;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void assertCertificateInKeyStore(Principal principal,
                                                   KeyStore keyStore) throws Exception {
        String subjectName = principal.getName();
        boolean found = false;
        for (String alias: Collections.list(keyStore.aliases())) {
            if (!keyStore.isCertificateEntry(alias)) {
                continue;
            }
            X509Certificate keyStoreCertificate = (X509Certificate) keyStore.getCertificate(alias);
            if (subjectName.equals(keyStoreCertificate.getSubjectDN().getName())) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    public static void assertCertificateInKeyStore(Certificate certificate,
                                                   KeyStore keyStore) throws Exception {
        boolean found = false;
        for (String alias: Collections.list(keyStore.aliases())) {
            if (!keyStore.isCertificateEntry(alias)) {
                continue;
            }
            Certificate keyStoreCertificate = keyStore.getCertificate(alias);
            if (certificate.equals(keyStoreCertificate)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    public static void assertServerCertificateChain(X509TrustManager trustManager,
                                                    Certificate[] serverChain)
            throws CertificateException {
        X509Certificate[] chain = (X509Certificate[]) serverChain;
        trustManager.checkServerTrusted(chain, chain[0].getPublicKey().getAlgorithm());
    }

    public static void assertClientCertificateChain(X509TrustManager trustManager,
                                                    Certificate[] clientChain)
            throws CertificateException {
        X509Certificate[] chain = (X509Certificate[]) clientChain;
        trustManager.checkClientTrusted(chain, chain[0].getPublicKey().getAlgorithm());
    }

    /**
     * Returns an SSLSocketFactory that calls setWantClientAuth and
     * setNeedClientAuth as specified on all returned sockets.
     */
    public static SSLSocketFactory clientAuth(final SSLSocketFactory sf,
                                              final boolean want,
                                              final boolean need) {
        return new SSLSocketFactory() {
            private SSLSocket set(Socket socket) {
                SSLSocket s = (SSLSocket) socket;
                s.setWantClientAuth(want);
                s.setNeedClientAuth(need);
                return s;
            }
            public Socket createSocket(String host, int port)
                    throws IOException, UnknownHostException {
                return set(sf.createSocket(host, port));
            }
            public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
                    throws IOException, UnknownHostException {
                return set(sf.createSocket(host, port, localHost, localPort));
            }
            public Socket createSocket(InetAddress host, int port) throws IOException {
                return set(sf.createSocket(host, port));
            }
            public Socket createSocket(InetAddress address, int port,
                                       InetAddress localAddress, int localPort) throws IOException {
                return set(sf.createSocket(address, port));
            }

            public String[] getDefaultCipherSuites() {
                return sf.getDefaultCipherSuites();
            }
            public String[] getSupportedCipherSuites() {
                return sf.getSupportedCipherSuites();
            }

            public Socket createSocket(Socket s, String host, int port, boolean autoClose)
                    throws IOException {
                return set(sf.createSocket(s, host, port, autoClose));
            }
        };
    }

    private static KeyManager[] concat(KeyManager[] a, KeyManager[] b) {
        if ((a == null) || (a.length == 0)) {
            return b;
        }
        if ((b == null) || (b.length == 0)) {
            return a;
        }
        KeyManager[] result = new KeyManager[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
