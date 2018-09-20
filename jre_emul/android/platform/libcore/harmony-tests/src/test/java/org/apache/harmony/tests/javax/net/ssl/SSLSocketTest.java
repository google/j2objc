/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.harmony.tests.javax.net.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.security.cert.X509Certificate;
import junit.framework.TestCase;
import libcore.io.Base64;
import libcore.java.security.StandardNames;
import org.apache.harmony.tests.javax.net.ssl.HandshakeCompletedEventTest.TestTrustManager;

public class SSLSocketTest extends TestCase {

    public class HandshakeCL implements HandshakeCompletedListener {
        public void handshakeCompleted(HandshakeCompletedEvent event) {
        }
    }

    /**
     * javax.net.ssl.SSLSocket#SSLSocket()
     */
    public void testConstructor() throws Exception {
        SSLSocket ssl = getSSLSocket();
        assertNotNull(ssl);
        ssl.close();
    }

    /**
     * javax.net.ssl.SSLSocket#SSLSocket(InetAddress address, int port)
     */
    public void testConstructor_InetAddressI() throws Exception {
        int sport = startServer("Cons InetAddress,I");
        int[] invalidPort = {-1, Integer.MIN_VALUE, 65536, Integer.MAX_VALUE};

        SSLSocket ssl = getSSLSocket(InetAddress.getLocalHost(), sport);
        assertNotNull(ssl);
        assertEquals(sport, ssl.getPort());
        ssl.close();

        try {
            getSSLSocket(InetAddress.getLocalHost(), sport + 1);
            fail();
        } catch (IOException expected) {
        }

        for (int i = 0; i < invalidPort.length; i++) {
            try {
                getSSLSocket(InetAddress.getLocalHost(), invalidPort[i]);
                fail();
            } catch (IllegalArgumentException expected) {
            }
        }
    }

    /**
     * javax.net.ssl.SSLSocket#SSLSocket(InetAddress address, int port,
     *                                          InetAddress clientAddress, int clientPort)
     */
    public void testConstructor_InetAddressIInetAddressI() throws Exception {
        int sport = startServer("Cons InetAddress,I,InetAddress,I");

        SSLSocket ssl = getSSLSocket(InetAddress.getLocalHost(), sport,
                                     InetAddress.getLocalHost(), 0);
        assertNotNull(ssl);
        assertEquals(sport, ssl.getPort());
        ssl.close();

        try {
            getSSLSocket(InetAddress.getLocalHost(), 8081, InetAddress.getLocalHost(), 8082);
            fail();
        } catch (IOException expected) {
        }

        try {
            getSSLSocket(InetAddress.getLocalHost(), -1, InetAddress.getLocalHost(), sport + 1);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        try {
            getSSLSocket(InetAddress.getLocalHost(), sport, InetAddress.getLocalHost(), -1);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            getSSLSocket(InetAddress.getLocalHost(), Integer.MIN_VALUE,
                         InetAddress.getLocalHost(), sport + 1);
            fail();
        } catch (IOException expectedOnRI) {
            assertTrue(StandardNames.IS_RI);
        } catch (IllegalArgumentException expectedOnAndroid) {
            assertFalse(StandardNames.IS_RI);
        }
        try {
            getSSLSocket(InetAddress.getLocalHost(), sport,
                         InetAddress.getLocalHost(), Integer.MAX_VALUE);
            fail();
        } catch (IllegalArgumentException expectedOnAndroid) {
            assertFalse(StandardNames.IS_RI);
        }
    }

    /**
     * javax.net.ssl.SSLSocket#SSLSocket(String host, int port)
     */
    public void testConstructor_StringI() throws Exception {
        int sport = startServer("Cons String,I");
        int[] invalidPort = {-1, Integer.MIN_VALUE, 65536, Integer.MAX_VALUE};

        SSLSocket ssl = getSSLSocket(InetAddress.getLocalHost().getHostName(), sport);
        assertNotNull(ssl);
        assertEquals(sport, ssl.getPort());
        ssl.close();

        try {
            getSSLSocket("localhost", 8082);
            fail();
        } catch (IOException expected) {
        }

        for (int i = 0; i < invalidPort.length; i++) {
            try {
                getSSLSocket(InetAddress.getLocalHost().getHostName(), invalidPort[i]);
                fail();
            } catch (IllegalArgumentException expected) {
            }
        }

        try {
            getSSLSocket("1.2.3.4hello", sport);
            fail();
        } catch (UnknownHostException expected) {
        }
    }

    /**
     * javax.net.ssl.SSLSocket#SSLSocket(String host, int port, InetAddress clientAddress,
     *           int clientPort)
     */
    public void testConstructor_StringIInetAddressI() throws Exception {
        int sport = startServer("Cons String,I,InetAddress,I");
        int[] invalidPort = {-1, Integer.MIN_VALUE, 65536, Integer.MAX_VALUE};

        SSLSocket ssl = getSSLSocket(InetAddress.getLocalHost().getHostName(), sport,
                                     InetAddress.getLocalHost(), 0);
        assertNotNull(ssl);
        assertEquals(sport, ssl.getPort());

        try {
            getSSLSocket(InetAddress.getLocalHost().getHostName(), 8081, InetAddress.getLocalHost(), 8082);
            fail();
        } catch (IOException expected) {
        }

        for (int i = 0; i < invalidPort.length; i++) {
            try {
                getSSLSocket(InetAddress.getLocalHost().getHostName(), invalidPort[i],
                             InetAddress.getLocalHost(), 0);
                fail();
            } catch (IllegalArgumentException expected) {
            }
            try {
                getSSLSocket(InetAddress.getLocalHost().getHostName(), sport,
                             InetAddress.getLocalHost(), invalidPort[i]);
                fail();
            } catch (IllegalArgumentException expected) {
            }
        }

        try {
            getSSLSocket("1.2.3.4hello", sport, InetAddress.getLocalHost(), 0);
            fail();
        } catch (UnknownHostException expected) {
        }
    }

    public void test_creationStressTest() throws Exception {
        // Test the default codepath, which uses /dev/urandom.
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, null, null);
        for (int i = 0; i < 2048; ++i) {
            sslContext.getSocketFactory().createSocket().close();
        }

        // Test the other codepath, which copies a seed from a byte[].
        sslContext.init(null, null, new SecureRandom());
        for (int i = 0; i < 2048; ++i) {
            sslContext.getSocketFactory().createSocket().close();
        }
    }

    /**
     * javax.net.ssl.SSLSocket#addHandshakeCompletedListener(HandshakeCompletedListener listener)
     */
    // AndroidOnly("RI doesn't throw the specified IAE")
    public void test_addHandshakeCompletedListener() throws IOException {
        SSLSocket ssl = getSSLSocket();
        HandshakeCompletedListener ls = new HandshakeCL();
        try {
            ssl.addHandshakeCompletedListener(null);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        ssl.addHandshakeCompletedListener(ls);
        ssl.close();
    }

    /**
     * javax.net.ssl.SSLSocket#removeHandshakeCompletedListener(HandshakeCompletedListener listener)
     */
    public void test_removeHandshakeCompletedListener() throws IOException {
        SSLSocket ssl = getSSLSocket();
        HandshakeCompletedListener ls = new HandshakeCL();
        try {
            ssl.removeHandshakeCompletedListener(null);
            fail();
        } catch (IllegalArgumentException expected) {
        }

        try {
            ssl.removeHandshakeCompletedListener(ls);
        } catch (IllegalArgumentException expected) {
        }

        ssl.addHandshakeCompletedListener(ls);
        ssl.removeHandshakeCompletedListener(ls);
        ssl.close();
    }

    /**
     * javax.net.ssl.SSLSocket#setEnableSessionCreation(boolean flag)
     * javax.net.ssl.SSLSocket#getEnableSessionCreation()
     */
    public void test_EnableSessionCreation() throws IOException {
        SSLSocket ssl = getSSLSocket();
        assertTrue(ssl.getEnableSessionCreation());
        ssl.setEnableSessionCreation(false);
        assertFalse(ssl.getEnableSessionCreation());
        ssl.setEnableSessionCreation(true);
        assertTrue(ssl.getEnableSessionCreation());
        ssl.close();
    }

    /**
     * javax.net.ssl.SSLSocket#setNeedClientAuth(boolean need)
     * javax.net.ssl.SSLSocket#getNeedClientAuthCreation()
     */
    public void test_NeedClientAuth() throws UnknownHostException, IOException {
        SSLSocket ssl = getSSLSocket();
        ssl.setNeedClientAuth(true);
        assertTrue(ssl.getNeedClientAuth());
        ssl.setNeedClientAuth(false);
        assertFalse(ssl.getNeedClientAuth());
        ssl.close();
    }

    /**
     * javax.net.ssl.SSLSocket#setWantClientAuth(boolean want)
     * javax.net.ssl.SSLSocket#getWantClientAuthCreation()
     */
    public void test_WantClientAuth() throws UnknownHostException, IOException {
        SSLSocket ssl = getSSLSocket();
        ssl.setWantClientAuth(true);
        assertTrue(ssl.getWantClientAuth());
        ssl.setWantClientAuth(false);
        assertFalse(ssl.getWantClientAuth());
        ssl.close();
    }

    /**
     * javax.net.ssl.SSLSocket#getSupportedProtocols()
     */
    public void test_getSupportedProtocols() throws IOException {
        SSLSocket ssl = getSSLSocket();
        String[] res = ssl.getSupportedProtocols();
        assertTrue("No supported protocols found", res.length > 0);
        ssl.close();
    }

    /**
     * javax.net.ssl.SSLSocket#getEnabledProtocols()
     * javax.net.ssl.SSLSocket#setEnabledProtocols(String[] protocols)
     */
    public void test_EnabledProtocols() throws IOException {
        SSLSocket ssl = getSSLSocket();
        try {
            ssl.setEnabledProtocols(null);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        ssl.setEnabledProtocols(new String[] {});
        try {
            ssl.setEnabledProtocols(new String[] {"blubb"});
            fail();
        } catch (IllegalArgumentException expected) {
        }
        ssl.setEnabledProtocols(ssl.getEnabledProtocols());
        String[] res = ssl.getEnabledProtocols();
        assertEquals("no enabled protocols set",
                     ssl.getEnabledProtocols().length, res.length);
        ssl.close();
    }

    /**
     * javax.net.ssl.SSLSocket#getSession()
     */
    public void test_getSession() throws IOException {
        SSLSocket ssl = getSSLSocket();
        assertNotNull(ssl.getSession());
        ssl.close();
    }

    /**
     * javax.net.ssl.SSLSocket#getSupportedCipherSuites()
     */
    public void test_getSupportedCipherSuites() throws IOException {
        SSLSocket ssl = getSSLSocket();
        String[] res = ssl.getSupportedCipherSuites();
        assertTrue("no supported cipher suites", res.length > 0);
        ssl.close();
    }

    /**
     * javax.net.ssl.SSLSocket#getEnabledCipherSuites()
     * javax.net.ssl.SSLSocket#setEnabledCipherSuites(String[] suites)
     */
    public void test_EnabledCipherSuites() throws IOException {
        SSLSocket ssl = getSSLSocket();
        try {
            ssl.setEnabledCipherSuites(null);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        ssl.setEnabledCipherSuites(new String[] {});
        try {
            ssl.setEnabledCipherSuites(new String[] {"blubb"});
            fail();
        } catch (IllegalArgumentException expected) {
        }
        ssl.setEnabledCipherSuites(ssl.getSupportedCipherSuites());
        String[] res = ssl.getEnabledCipherSuites();
        assertNotNull("NULL result", res);
        assertEquals("not all supported cipher suites were enabled",
                     Arrays.asList(ssl.getSupportedCipherSuites()),
                     Arrays.asList(res));
        ssl.close();
    }

    /**
     * javax.net.ssl.SSLSocket#getUseClientMode()
     * javax.net.ssl.SSLSocket#setUseClientMode(boolean mode)
     */
    public void test_UseClientMode() throws IOException {
        SSLSocket ssl = getSSLSocket();
        assertTrue(ssl.getUseClientMode());
        ssl.setUseClientMode(false);
        assertFalse(ssl.getUseClientMode());
        ssl.close();

        ssl = getSSLSocket("localhost", startServer("UseClientMode"));
        try {
            ssl.startHandshake();
        } catch (IOException ioe) {
            //fail(ioe + " was thrown for method startHandshake()");
        }
        try {
            ssl.setUseClientMode(false);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        ssl.close();
    }

    /**
     * javax.net.ssl.SSLSocket#startHandshake()
     */
    public void test_startHandshake() throws IOException {
        SSLSocket ssl = getSSLSocket();
        try {
            ssl.startHandshake();
            fail();
        } catch (IOException expected) {
        }
        ssl.close();
    }

    private boolean useBKS = !StandardNames.IS_RI;

    private String PASSWORD = "android";

    private boolean serverReady = false;

    /**
     * Defines the keystore contents for the server, BKS version. Holds just a
     * single self-generated key. The subject name is "Test Server".
     */
    private static final String SERVER_KEYS_BKS = ""
            + "AAAAAQAAABQDkebzoP1XwqyWKRCJEpn/t8dqIQAABDkEAAVteWtleQAAARpYl20nAAAAAQAFWC41"
            + "MDkAAAJNMIICSTCCAbKgAwIBAgIESEfU1jANBgkqhkiG9w0BAQUFADBpMQswCQYDVQQGEwJVUzET"
            + "MBEGA1UECBMKQ2FsaWZvcm5pYTEMMAoGA1UEBxMDTVRWMQ8wDQYDVQQKEwZHb29nbGUxEDAOBgNV"
            + "BAsTB0FuZHJvaWQxFDASBgNVBAMTC1Rlc3QgU2VydmVyMB4XDTA4MDYwNTExNTgxNFoXDTA4MDkw"
            + "MzExNTgxNFowaTELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExDDAKBgNVBAcTA01U"
            + "VjEPMA0GA1UEChMGR29vZ2xlMRAwDgYDVQQLEwdBbmRyb2lkMRQwEgYDVQQDEwtUZXN0IFNlcnZl"
            + "cjCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA0LIdKaIr9/vsTq8BZlA3R+NFWRaH4lGsTAQy"
            + "DPMF9ZqEDOaL6DJuu0colSBBBQ85hQTPa9m9nyJoN3pEi1hgamqOvQIWcXBk+SOpUGRZZFXwniJV"
            + "zDKU5nE9MYgn2B9AoiH3CSuMz6HRqgVaqtppIe1jhukMc/kHVJvlKRNy9XMCAwEAATANBgkqhkiG"
            + "9w0BAQUFAAOBgQC7yBmJ9O/eWDGtSH9BH0R3dh2NdST3W9hNZ8hIa8U8klhNHbUCSSktZmZkvbPU"
            + "hse5LI3dh6RyNDuqDrbYwcqzKbFJaq/jX9kCoeb3vgbQElMRX8D2ID1vRjxwlALFISrtaN4VpWzV"
            + "yeoHPW4xldeZmoVtjn8zXNzQhLuBqX2MmAAAAqwAAAAUvkUScfw9yCSmALruURNmtBai7kQAAAZx"
            + "4Jmijxs/l8EBaleaUru6EOPioWkUAEVWCxjM/TxbGHOi2VMsQWqRr/DZ3wsDmtQgw3QTrUK666sR"
            + "MBnbqdnyCyvM1J2V1xxLXPUeRBmR2CXorYGF9Dye7NkgVdfA+9g9L/0Au6Ugn+2Cj5leoIgkgApN"
            + "vuEcZegFlNOUPVEs3SlBgUF1BY6OBM0UBHTPwGGxFBBcetcuMRbUnu65vyDG0pslT59qpaR0TMVs"
            + "P+tcheEzhyjbfM32/vwhnL9dBEgM8qMt0sqF6itNOQU/F4WGkK2Cm2v4CYEyKYw325fEhzTXosck"
            + "MhbqmcyLab8EPceWF3dweoUT76+jEZx8lV2dapR+CmczQI43tV9btsd1xiBbBHAKvymm9Ep9bPzM"
            + "J0MQi+OtURL9Lxke/70/MRueqbPeUlOaGvANTmXQD2OnW7PISwJ9lpeLfTG0LcqkoqkbtLKQLYHI"
            + "rQfV5j0j+wmvmpMxzjN3uvNajLa4zQ8l0Eok9SFaRr2RL0gN8Q2JegfOL4pUiHPsh64WWya2NB7f"
            + "V+1s65eA5ospXYsShRjo046QhGTmymwXXzdzuxu8IlnTEont6P4+J+GsWk6cldGbl20hctuUKzyx"
            + "OptjEPOKejV60iDCYGmHbCWAzQ8h5MILV82IclzNViZmzAapeeCnexhpXhWTs+xDEYSKEiG/camt"
            + "bhmZc3BcyVJrW23PktSfpBQ6D8ZxoMfF0L7V2GQMaUg+3r7ucrx82kpqotjv0xHghNIm95aBr1Qw"
            + "1gaEjsC/0wGmmBDg1dTDH+F1p9TInzr3EFuYD0YiQ7YlAHq3cPuyGoLXJ5dXYuSBfhDXJSeddUkl"
            + "k1ufZyOOcskeInQge7jzaRfmKg3U94r+spMEvb0AzDQVOKvjjo1ivxMSgFRZaDb/4qw=";

    /**
     * Defines the keystore contents for the server, JKS version. Holds just a
     * single self-generated key. The subject name is "Test Server".
     */
    private static final String SERVER_KEYS_JKS = ""
            + "/u3+7QAAAAIAAAABAAAAAQAFbXlrZXkAAAEaWFfBeAAAArowggK2MA4GCisGAQQBKgIRAQEFAASC"
            + "AqI2kp5XjnF8YZkhcF92YsJNQkvsmH7zqMM87j23zSoV4DwyE3XeC/gZWq1ToScIhoqZkzlbWcu4"
            + "T/Zfc/DrfGk/rKbBL1uWKGZ8fMtlZk8KoAhxZk1JSyJvdkyKxqmzUbxk1OFMlN2VJNu97FPVH+du"
            + "dvjTvmpdoM81INWBW/1fZJeQeDvn4mMbbe0IxgpiLnI9WSevlaDP/sm1X3iO9yEyzHLL+M5Erspo"
            + "Cwa558fOu5DdsICMXhvDQxjWFKFhPHnKtGe+VvwkG9/bAaDgx3kfhk0w5zvdnkKb+8Ed9ylNRzdk"
            + "ocAa/mxlMTOsTvDKXjjsBupNPIIj7OP4GNnZaxkJjSs98pEO67op1GX2qhy6FSOPNuq8k/65HzUc"
            + "PYn6voEeh6vm02U/sjEnzRevQ2+2wXoAdp0EwtQ/DlMe+NvcwPGWKuMgX4A4L93DZGb04N2VmAU3"
            + "YLOtZwTO0LbuWrcCM/q99G/7LcczkxIVrO2I/rh8RXVczlf9QzcrFObFv4ATuspWJ8xG7DhsMbnk"
            + "rT94Pq6TogYeoz8o8ZMykesAqN6mt/9+ToIemmXv+e+KU1hI5oLwWMnUG6dXM6hIvrULY6o+QCPH"
            + "172YQJMa+68HAeS+itBTAF4Clm/bLn6reHCGGU6vNdwU0lYldpiOj9cB3t+u2UuLo6tiFWjLf5Zs"
            + "EQJETd4g/EK9nHxJn0GAKrWnTw7pEHQJ08elzUuy04C/jEEG+4QXU1InzS4o/kR0Sqz2WTGDoSoq"
            + "ewuPRU5bzQs/b9daq3mXrnPtRBL6HfSDAdpTK76iHqLCGdqx3avHjVSBm4zFvEuYBCev+3iKOBmg"
            + "yh7eQRTjz4UOWfy85omMBr7lK8PtfVBDzOXpasxS0uBgdUyBDX4tO6k9jZ8a1kmQRQAAAAEABVgu"
            + "NTA5AAACSDCCAkQwggGtAgRIR8SKMA0GCSqGSIb3DQEBBAUAMGkxCzAJBgNVBAYTAlVTMRMwEQYD"
            + "VQQIEwpDYWxpZm9ybmlhMQwwCgYDVQQHEwNNVFYxDzANBgNVBAoTBkdvb2dsZTEQMA4GA1UECxMH"
            + "QW5kcm9pZDEUMBIGA1UEAxMLVGVzdCBTZXJ2ZXIwHhcNMDgwNjA1MTA0ODQyWhcNMDgwOTAzMTA0"
            + "ODQyWjBpMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEMMAoGA1UEBxMDTVRWMQ8w"
            + "DQYDVQQKEwZHb29nbGUxEDAOBgNVBAsTB0FuZHJvaWQxFDASBgNVBAMTC1Rlc3QgU2VydmVyMIGf"
            + "MA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCwoC6chqCI84rj1PrXuJgbiit4EV909zR6N0jNlYfg"
            + "itwB39bP39wH03rFm8T59b3mbSptnGmCIpLZn25KPPFsYD3JJ+wFlmiUdEP9H05flfwtFQJnw9uT"
            + "3rRIdYVMPcQ3RoZzwAMliGr882I2thIDbA6xjGU/1nRIdvk0LtxH3QIDAQABMA0GCSqGSIb3DQEB"
            + "BAUAA4GBAJn+6YgUlY18Ie+0+Vt8oEi81DNi/bfPrAUAh63fhhBikx/3R9dl3wh09Z6p7cIdNxjW"
            + "n2ll+cRW9eqF7z75F0Omm0C7/KAEPjukVbszmzeU5VqzkpSt0j84YWi+TfcHRrfvhLbrlmGITVpY"
            + "ol5pHLDyqGmDs53pgwipWqsn/nEXEBgj3EoqPeqHbDf7YaP8h/5BSt0=";

    protected int startServer(String name) {
        String keys = useBKS ? SERVER_KEYS_BKS : SERVER_KEYS_JKS;
        TestServer server = new TestServer(true, keys);
        Thread serverThread = new Thread(server);
        serverThread.start();
        try {
            while (!serverReady) {
                Exception e = server.getException();
                if (e != null) {
                    throw new AssertionError(e);
                }
                Thread.currentThread().sleep(50);
            }
            // give the server 100 millis to accept
            Thread.currentThread().sleep(100);
        } catch (InterruptedException ignore) {
        }
        return server.sport;
    }

    /**
     * Implements a test SSL socket server. It wait for a connection on a given
     * port, requests client authentication (if specified), and read 256 bytes
     * from the socket.
     */
    class TestServer implements Runnable {

        public static final int CLIENT_AUTH_NONE = 0;

        public static final int CLIENT_AUTH_WANTED = 1;

        public static final int CLIENT_AUTH_NEEDED = 2;

        private TestTrustManager trustManager;

        private Exception exception;

        String keys;

        private boolean provideKeys;

        int sport;

        public TestServer(boolean provideKeys, String keys) {
            this.keys = keys;
            this.provideKeys = provideKeys;

            trustManager = new TestTrustManager();
        }

        public void run() {
            try {
                KeyManager[] keyManagers = provideKeys ? getKeyManagers(keys) : null;
                TrustManager[] trustManagers = new TrustManager[] { trustManager };

                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(keyManagers, trustManagers, null);

                SSLServerSocket serverSocket = (SSLServerSocket)
                        sslContext.getServerSocketFactory().createServerSocket();
                try {
                    serverSocket.bind(new InetSocketAddress(0));
                    sport = serverSocket.getLocalPort();
                    serverReady = true;

                    SSLSocket clientSocket = (SSLSocket)serverSocket.accept();

                    try {
                        InputStream stream = clientSocket.getInputStream();
                        try {
                            for (int i = 0; i < 256; i++) {
                                int j = stream.read();
                                if (i != j) {
                                    throw new RuntimeException("Error reading socket, expected " + i
                                                               + ", got " + j);
                                }
                            }
                        } finally {
                            stream.close();
                        }
                    } finally {
                        clientSocket.close();
                    }
                } finally {
                    serverSocket.close();
                }
            } catch (Exception ex) {
                exception = ex;
            }
        }

        public Exception getException() {
            return exception;
        }

        public X509Certificate[] getChain() {
            return trustManager.getChain();
        }

    }

    /**
     * Loads a keystore from a base64-encoded String. Returns the KeyManager[]
     * for the result.
     */
    private KeyManager[] getKeyManagers(String keys) throws Exception {
        byte[] bytes = Base64.decode(keys.getBytes());
        InputStream inputStream = new ByteArrayInputStream(bytes);

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(inputStream, PASSWORD.toCharArray());
        inputStream.close();

        String algorithm = KeyManagerFactory.getDefaultAlgorithm();
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(algorithm);
        keyManagerFactory.init(keyStore, PASSWORD.toCharArray());

        return keyManagerFactory.getKeyManagers();
    }

    private SSLSocket getSSLSocket() throws IOException {
        return (SSLSocket) SSLSocketFactory.getDefault().createSocket();
    }

    private SSLSocket getSSLSocket(InetAddress host, int port) throws IOException {
        return (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
    }

    private SSLSocket getSSLSocket(String host, int port) throws UnknownHostException, IOException {
        return (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);
    }

    private SSLSocket getSSLSocket(InetAddress host, int port, InetAddress localHost, int localPort)
            throws IOException {
        return (SSLSocket) SSLSocketFactory.getDefault().createSocket(host,
                                                                      port,
                                                                      localHost,
                                                                      localPort);
    }

    private SSLSocket getSSLSocket(String host, int port, InetAddress localHost, int localPort)
            throws UnknownHostException, IOException {
        return (SSLSocket) SSLSocketFactory.getDefault().createSocket(host,
                                                                      port,
                                                                      localHost,
                                                                      localPort);
    }
}
