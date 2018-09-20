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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Properties;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import junit.framework.TestCase;
import libcore.java.security.StandardNames;

public class SSLSocketFactoryTest extends TestCase {
    private static final String SSL_PROPERTY = "ssl.SocketFactory.provider";

    public void test_SSLSocketFactory_getDefault() {
        SocketFactory sf = SSLSocketFactory.getDefault();
        assertNotNull(sf);
        assertTrue(SSLSocketFactory.class.isAssignableFrom(sf.getClass()));
    }

    public static class FakeSSLSocketProvider extends Provider {
        public FakeSSLSocketProvider() {
            super("FakeSSLSocketProvider", 1.0, "Testing provider");
            put("SSLContext.Default", FakeSSLContextSpi.class.getName());
        }
    }

    public static final class FakeSSLContextSpi extends SSLContextSpi {
        @Override
        protected void engineInit(KeyManager[] keyManagers, TrustManager[] trustManagers,
                SecureRandom secureRandom) throws KeyManagementException {
            throw new UnsupportedOperationException();
        }

        @Override
        protected SSLSocketFactory engineGetSocketFactory() {
            return new FakeSSLSocketFactory();
        }

        @Override
        protected SSLServerSocketFactory engineGetServerSocketFactory() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected SSLEngine engineCreateSSLEngine(String s, int i) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected SSLEngine engineCreateSSLEngine() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected SSLSessionContext engineGetServerSessionContext() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected SSLSessionContext engineGetClientSessionContext() {
            throw new UnsupportedOperationException();
        }
    }

    public static class FakeSSLSocketFactory extends SSLSocketFactory {
        public FakeSSLSocketFactory() {
        }

        @Override
        public String[] getDefaultCipherSuites() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
                int localPort) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Socket createSocket(InetAddress host, int port) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Socket createSocket(String host, int port) {
            throw new UnsupportedOperationException();
        }
    }

    public void test_SSLSocketFactory_getDefault_cacheInvalidate() throws Exception {
        String origProvider = resetSslProvider();
        try {
            SocketFactory sf1 = SSLSocketFactory.getDefault();
            assertNotNull(sf1);
            assertTrue(SSLSocketFactory.class.isAssignableFrom(sf1.getClass()));

            Provider fakeProvider = new FakeSSLSocketProvider();
            SocketFactory sf4 = null;
            SSLContext origContext = null;
            try {
                origContext = SSLContext.getDefault();
                Security.insertProviderAt(fakeProvider, 1);
                SSLContext.setDefault(SSLContext.getInstance("Default", fakeProvider));

                sf4 = SSLSocketFactory.getDefault();
                assertNotNull(sf4);
                assertTrue(SSLSocketFactory.class.isAssignableFrom(sf4.getClass()));

                assertFalse(sf1.getClass() + " should not be " + sf4.getClass(),
                        sf1.getClass().equals(sf4.getClass()));
            } finally {
                SSLContext.setDefault(origContext);
                Security.removeProvider(fakeProvider.getName());
            }

            SocketFactory sf3 = SSLSocketFactory.getDefault();
            assertNotNull(sf3);
            assertTrue(SSLSocketFactory.class.isAssignableFrom(sf3.getClass()));

            assertTrue(sf1.getClass() + " should be " + sf3.getClass(),
                    sf1.getClass().equals(sf3.getClass()));

            if (!StandardNames.IS_RI) {
                Security.setProperty(SSL_PROPERTY, FakeSSLSocketFactory.class.getName());
                SocketFactory sf2 = SSLSocketFactory.getDefault();
                assertNotNull(sf2);
                assertTrue(SSLSocketFactory.class.isAssignableFrom(sf2.getClass()));

                assertFalse(sf2.getClass().getName() + " should not be " + Security.getProperty(SSL_PROPERTY),
                        sf1.getClass().equals(sf2.getClass()));
                assertTrue(sf2.getClass().equals(sf4.getClass()));

                resetSslProvider();
            }
        } finally {
            Security.setProperty(SSL_PROPERTY, origProvider);
        }
    }

    private String resetSslProvider() {
        String origProvider = Security.getProperty(SSL_PROPERTY);

        try {
            Field field_secprops = Security.class.getDeclaredField("props");
            field_secprops.setAccessible(true);
            Properties secprops = (Properties) field_secprops.get(null);
            secprops.remove(SSL_PROPERTY);
            Security.increaseVersion();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not clear security provider", e);
        }

        assertNull(Security.getProperty(SSL_PROPERTY));
        return origProvider;
    }

    public void test_SSLSocketFactory_defaultConfiguration() throws Exception {
        SSLConfigurationAsserts.assertSSLSocketFactoryDefaultConfiguration(
                (SSLSocketFactory) SSLSocketFactory.getDefault());
    }

    public void test_SSLSocketFactory_getDefaultCipherSuitesReturnsCopies() {
        SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        assertNotSame(sf.getDefaultCipherSuites(), sf.getDefaultCipherSuites());
    }

    public void test_SSLSocketFactory_getSupportedCipherSuitesReturnsCopies() {
        SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        assertNotSame(sf.getSupportedCipherSuites(), sf.getSupportedCipherSuites());
    }

    public void test_SSLSocketFactory_createSocket() throws Exception {
        try {
            SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            Socket s = sf.createSocket(null, null, -1, false);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            Socket ssl = sf.createSocket(new Socket(), null, -1, false);
            fail();
        } catch (SocketException expected) {
        }

        ServerSocket ss = ServerSocketFactory.getDefault().createServerSocket(0);
        InetSocketAddress sa = (InetSocketAddress) ss.getLocalSocketAddress();
        InetAddress host = sa.getAddress();
        int port = sa.getPort();
        Socket s = new Socket(host, port);
        SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        Socket ssl = sf.createSocket(s, null, -1, false);
        assertNotNull(ssl);
        assertTrue(SSLSocket.class.isAssignableFrom(ssl.getClass()));
    }
}
