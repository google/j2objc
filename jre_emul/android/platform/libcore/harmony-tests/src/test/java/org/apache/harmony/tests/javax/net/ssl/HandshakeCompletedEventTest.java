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

package org.apache.harmony.tests.javax.net.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;
import junit.framework.TestCase;
import libcore.io.Base64;
import org.apache.harmony.xnet.tests.support.mySSLSession;

/**
 * Tests for <code>HandshakeCompletedEvent</code> class constructors and methods.
 *
 */
public class HandshakeCompletedEventTest extends TestCase {

    private String certificate = "-----BEGIN CERTIFICATE-----\n"
        + "MIICZTCCAdICBQL3AAC2MA0GCSqGSIb3DQEBAgUAMF8xCzAJBgNVBAYTAlVTMSAw\n"
        + "HgYDVQQKExdSU0EgRGF0YSBTZWN1cml0eSwgSW5jLjEuMCwGA1UECxMlU2VjdXJl\n"
        + "IFNlcnZlciBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTAeFw05NzAyMjAwMDAwMDBa\n"
        + "Fw05ODAyMjAyMzU5NTlaMIGWMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZv\n"
        + "cm5pYTESMBAGA1UEBxMJUGFsbyBBbHRvMR8wHQYDVQQKExZTdW4gTWljcm9zeXN0\n"
        + "ZW1zLCBJbmMuMSEwHwYDVQQLExhUZXN0IGFuZCBFdmFsdWF0aW9uIE9ubHkxGjAY\n"
        + "BgNVBAMTEWFyZ29uLmVuZy5zdW4uY29tMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCB\n"
        + "iQKBgQCofmdY+PiUWN01FOzEewf+GaG+lFf132UpzATmYJkA4AEA/juW7jSi+LJk\n"
        + "wJKi5GO4RyZoyimAL/5yIWDV6l1KlvxyKslr0REhMBaD/3Z3EsLTTEf5gVrQS6sT\n"
        + "WMoSZAyzB39kFfsB6oUXNtV8+UKKxSxKbxvhQn267PeCz5VX2QIDAQABMA0GCSqG\n"
        + "SIb3DQEBAgUAA34AXl3at6luiV/7I9MN5CXYoPJYI8Bcdc1hBagJvTMcmlqL2uOZ\n"
        + "H9T5hNMEL9Tk6aI7yZPXcw/xI2K6pOR/FrMp0UwJmdxX7ljV6ZtUZf7pY492UqwC\n"
        + "1777XQ9UEZyrKJvF5ntleeO0ayBqLGVKCWzWZX9YsXCpv47FNLZbupE=\n"
        + "-----END CERTIFICATE-----\n";


    /**
     * @throws IOException
     * javax.net.ssl.HandshakeCompletedEvent#HandshakeCompletedEvent(SSLSocket sock, SSLSession s)
     */
    public final void test_Constructor() throws Exception {
        mySSLSession session = new mySSLSession();
        SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        try {
            new HandshakeCompletedEvent(null, null);
            fail("Any exception wasn't thrown for null parameters");
        } catch (Exception expected) {
        }
    }

    /**
     * @throws IOException
     * javax.net.ssl.HandshakeCompletedEvent#getCipherSuite()
     */
    public final void test_getCipherSuite() throws Exception {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        assertEquals("SuiteName", event.getCipherSuite());
    }

    /**
     * @throws IOException
     * javax.net.ssl.HandshakeCompletedEvent#getLocalCertificates()
     */
    public final void test_getLocalCertificates() throws Exception {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        assertNull(event.getLocalCertificates());
    }

    /**
     * @throws IOException
     * javax.net.ssl.HandshakeCompletedEvent#getLocalPrincipal()
     */
    public final void test_getLocalPrincipal() throws Exception {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        assertNull(event.getLocalPrincipal());
    }

    /**
     * @throws IOException
     * javax.net.ssl.HandshakeCompletedEvent#getPeerCertificateChain()
     */
    public final void test_getPeerCertificateChain() throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(certificate.getBytes());
        mySSLSession session = new mySSLSession((X509Certificate[]) null);
        SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        try {
            event.getPeerCertificateChain();
            fail("SSLPeerUnverifiedException wasn't thrown");
        } catch (SSLPeerUnverifiedException expected) {
        }

        X509Certificate xc = X509Certificate.getInstance(bis);
        X509Certificate[] xcs = {xc};
        session = new mySSLSession(xcs);
        event = new HandshakeCompletedEvent(socket, session);

        X509Certificate[] res = event.getPeerCertificateChain();
        assertEquals(1, res.length);
    }

    /**
     * @throws IOException
     * javax.net.ssl.HandshakeCompletedEvent#getPeerCertificates()
     */
    public final void test_getPeerCertificates() throws IOException {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        try {
            event.getPeerCertificates();
            fail("SSLPeerUnverifiedException wasn't thrown");
        } catch (SSLPeerUnverifiedException expected) {
        }

        session = new mySSLSession((X509Certificate[]) null);
        event = new HandshakeCompletedEvent(socket, session);
        Certificate[] res = event.getPeerCertificates();
        assertEquals(3, res.length);
    }

    /**
     * @throws IOException
     * javax.net.ssl.HandshakeCompletedEvent#getPeerPrincipal()
     */
    public final void test_getPeerPrincipal() throws IOException {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        assertNull(event.getPeerPrincipal());
    }

    /**
     * @throws IOException
     * javax.net.ssl.HandshakeCompletedEvent#getSession()
     */
    public final void test_getSession() throws IOException {
        mySSLSession session = new mySSLSession("localhost", 1080, null);
        SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, session);
        SSLSession ss = event.getSession();
        assertNotNull(ss);
        assertEquals(session, ss);
    }

    /**
     * @throws IOException
     * javax.net.ssl.HandshakeCompletedEvent#getSocket()
     */
    public final void test_getSocket() throws IOException {
        SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
        HandshakeCompletedEvent event = new HandshakeCompletedEvent(socket, null);
        SSLSocket ss = event.getSocket();
        assertNotNull(ss);
        assertEquals(socket, ss);
    }


    // Regression test for CompletedHandshakeEvent not firing with a custom
    // TrustManager


    private SSLSocket socket;
    private SSLServerSocket serverSocket;
    private MyHandshakeListener listener;
    private String host = "localhost";

    private String PASSWORD = "android";

    /**
     * Defines the keystore contents for the server, BKS version. Holds just a
     * single self-generated key. The subject name is "Test Server".
     */
    private static final String SERVER_KEYS_BKS =
            "AAAAAQAAABQDkebzoP1XwqyWKRCJEpn/t8dqIQAABDkEAAVteWtleQAAARpYl20nAAAAAQAFWC41"
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
    private static final String SERVER_KEYS_JKS =
            "/u3+7QAAAAIAAAABAAAAAQAFbXlrZXkAAAEaWFfBeAAAArowggK2MA4GCisGAQQBKgIRAQEFAASC"
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

    /**
     * Defines the keystore contents for the client, JKS version. Holds just a
     * single self-generated key. The subject name is "Test Client".
     */
    private static final String CLIENT_KEYS_JKS =
            "/u3+7QAAAAIAAAABAAAAAQAFbXlrZXkAAAEaWFhyMAAAArkwggK1MA4GCisGAQQBKgIRAQEFAASC"
            + "AqGVSfXolBStZy4nnRNn4fAr+S7kfU2BS23wwW8uB2Ru3GvtLzlK9q08Gvq/LNqBafjyFTVL5FV5"
            + "SED/8YomO5a98GpskSeRvytCiTBLJdgGhws5TOGekgIAcBROPGIyOtJPQ0HfOQs+BqgzGDHzHQhw"
            + "u/8Tm6yQwiP+W/1I9B1QnaEztZA3mhTyMMJsmsFTYroGgAog885D5Cmzd8sYGfxec3R6I+xcmBAY"
            + "eibR5kGpWwt1R+qMvRrtBqh5r6WSKhCBNax+SJVbtUNRiKyjKccdJg6fGqIWWeivwYTy0OhjA6b4"
            + "NiZ/ZZs5pxFGWUj/Rlp0RYy8fCF6aw5/5s4Bf4MI6dPSqMG8Hf7sJR91GbcELyzPdM0h5lNavgit"
            + "QPEzKeuDrGxhY1frJThBsNsS0gxeu+OgfJPEb/H4lpYX5IvuIGbWKcxoO9zq4/fimIZkdA8A+3eY"
            + "mfDaowvy65NBVQPJSxaOyFhLHfeLqOeCsVENAea02vA7andZHTZehvcrqyKtm+z8ncHGRC2H9H8O"
            + "jKwKHfxxrYY/jMAKLl00+PBb3kspO+BHI2EcQnQuMw/zr83OR9Meq4TJ0TMuNkApZELAeFckIBbS"
            + "rBr8NNjAIfjuCTuKHhsTFWiHfk9ZIzigxXagfeDRiyVc6khOuF/bGorj23N2o7Rf3uLoU6PyXWi4"
            + "uhctR1aL6NzxDoK2PbYCeA9hxbDv8emaVPIzlVwpPK3Ruvv9mkjcOhZ74J8bPK2fQmbplbOljcZi"
            + "tZijOfzcO/11JrwhuJZRA6wanTqHoujgChV9EukVrmbWGGAcewFnAsSbFXIik7/+QznXaDIt5NgL"
            + "H/Bcz4Z/fdV7Ae1eUaxKXdPbI//4J+8liVT/d8awjW2tldIaDlmGMR3aoc830+3mAAAAAQAFWC41"
            + "MDkAAAJIMIICRDCCAa0CBEhHxLgwDQYJKoZIhvcNAQEEBQAwaTELMAkGA1UEBhMCVVMxEzARBgNV"
            + "BAgTCkNhbGlmb3JuaWExDDAKBgNVBAcTA01UVjEPMA0GA1UEChMGR29vZ2xlMRAwDgYDVQQLEwdB"
            + "bmRyb2lkMRQwEgYDVQQDEwtUZXN0IENsaWVudDAeFw0wODA2MDUxMDQ5MjhaFw0wODA5MDMxMDQ5"
            + "MjhaMGkxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMQwwCgYDVQQHEwNNVFYxDzAN"
            + "BgNVBAoTBkdvb2dsZTEQMA4GA1UECxMHQW5kcm9pZDEUMBIGA1UEAxMLVGVzdCBDbGllbnQwgZ8w"
            + "DQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAIK3Q+KiFbmCGg422TAo4gggdhMH6FJhiuz8DxRyeMKR"
            + "UAfP4MK0wtc8N42waZ6OKvxpBFUy0BRfBsX0GD4Ku99yu9/tavSigTraeJtwV3WWRRjIqk7L3wX5"
            + "cmgS2KSD43Y0rNUKrko26lnt9N4qiYRBSj+tcAN3Lx9+ptqk1LApAgMBAAEwDQYJKoZIhvcNAQEE"
            + "BQADgYEANb7Q1GVSuy1RPJ0FmiXoMYCCtvlRLkmJphwxovK0cAQK12Vll+yAzBhHiQHy/RA11mng"
            + "wYudC7u3P8X/tBT8GR1Yk7QW3KgFyPafp3lQBBCraSsfrjKj+dCLig1uBLUr4f68W8VFWZWWTHqp"
            + "NMGpCX6qmjbkJQLVK/Yfo1ePaUexPSOX0G9m8+DoV3iyNw6at01NRw==";

    /**
     * Defines the keystore contents for the client, BKS version. Holds just a
     * single self-generated key. The subject name is "Test Client".
     */
    private static final String CLIENT_KEYS_BKS =
            "AAAAAQAAABT4Rka6fxbFps98Y5k2VilmbibNkQAABfQEAAVteWtleQAAARpYl+POAAAAAQAFWC41"
            + "MDkAAAJNMIICSTCCAbKgAwIBAgIESEfU9TANBgkqhkiG9w0BAQUFADBpMQswCQYDVQQGEwJVUzET"
            + "MBEGA1UECBMKQ2FsaWZvcm5pYTEMMAoGA1UEBxMDTVRWMQ8wDQYDVQQKEwZHb29nbGUxEDAOBgNV"
            + "BAsTB0FuZHJvaWQxFDASBgNVBAMTC1Rlc3QgQ2xpZW50MB4XDTA4MDYwNTExNTg0NVoXDTA4MDkw"
            + "MzExNTg0NVowaTELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExDDAKBgNVBAcTA01U"
            + "VjEPMA0GA1UEChMGR29vZ2xlMRAwDgYDVQQLEwdBbmRyb2lkMRQwEgYDVQQDEwtUZXN0IENsaWVu"
            + "dDCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEApUvmWsQDHPpbDKK13Yez2/q54tTOmRml/qva"
            + "2K6dZjkjSTW0iRuk7ztaVEvdJpfVIDv1oBsCI51ttyLHROy1epjF+GoL74mJb7fkcd0VOoSOTjtD"
            + "+3GgZkHPAm5YmUYxiJXqxKKJJqMCTIW46eJaA2nAep9QIwZ14/NFAs4ObV8CAwEAATANBgkqhkiG"
            + "9w0BAQUFAAOBgQCJrCr3hZQFDlLIfsSKI1/w+BLvyf4fubOid0pBxfklR8KBNPTiqjSmu7pd/C/F"
            + "1FR8CdZUDoPflZHCOU+fj5r5KUC1HyigY/tEUvlforBpfB0uCF+tXW4DbUfOWhfMtLV4nCOJOOZg"
            + "awfZLJWBJouLKOp427vDftxTSB+Ks8YjlgAAAqwAAAAU+NH6TtrzjyDdCXm5B6Vo7xX5G4YAAAZx"
            + "EAUkcZtmykn7YdaYxC1jRFJ+GEJpC8nZVg83QClVuCSIS8a5f8Hl44Bk4oepOZsPzhtz3RdVzDVi"
            + "RFfoyZFsrk9F5bDTVJ6sQbb/1nfJkLhZFXokka0vND5AXMSoD5Bj1Fqem3cK7fSUyqKvFoRKC3XD"
            + "FQvhqoam29F1rbl8FaYdPvhhZo8TfZQYUyUKwW+RbR44M5iHPx+ykieMe/C/4bcM3z8cwIbYI1aO"
            + "gjQKS2MK9bs17xaDzeAh4sBKrskFGrDe+2dgvrSKdoakJhLTNTBSG6m+rzqMSCeQpafLKMSjTSSz"
            + "+KoQ9bLyax8cbvViGGju0SlVhquloZmKOfHr8TukIoV64h3uCGFOVFtQjCYDOq6NbfRvMh14UVF5"
            + "zgDIGczoD9dMoULWxBmniGSntoNgZM+QP6Id7DBasZGKfrHIAw3lHBqcvB5smemSu7F4itRoa3D8"
            + "N7hhUEKAc+xA+8NKmXfiCBoHfPHTwDvt4IR7gWjeP3Xv5vitcKQ/MAfO5RwfzkYCXQ3FfjfzmsE1"
            + "1IfLRDiBj+lhQSulhRVStKI88Che3M4JUNGKllrc0nt1pWa1vgzmUhhC4LSdm6trTHgyJnB6OcS9"
            + "t2furYjK88j1AuB4921oxMxRm8c4Crq8Pyuf+n3YKi8Pl2BzBtw++0gj0ODlgwut8SrVj66/nvIB"
            + "jN3kLVahR8nZrEFF6vTTmyXi761pzq9yOVqI57wJGx8o3Ygox1p+pWUPl1hQR7rrhUbgK/Q5wno9"
            + "uJk07h3IZnNxE+/IKgeMTP/H4+jmyT4mhsexJ2BFHeiKF1KT/FMcJdSi+ZK5yoNVcYuY8aZbx0Ef"
            + "lHorCXAmLFB0W6Cz4KPP01nD9YBB4olxiK1t7m0AU9zscdivNiuUaB5OIEr+JuZ6dNw=";


    /**
     * Implements the actual test case. Launches a server and a client, requires
     * client authentication and checks the certificates afterwards (not in the
     * usual sense, we just make sure that we got the expected certificates,
     * because our self-signed test certificates are not valid.)
     */

    public void testClientAuth() throws Exception {

        boolean useBKS = true;

        listener = new MyHandshakeListener();
        String serverKeys = (useBKS ? SERVER_KEYS_BKS : SERVER_KEYS_JKS);
        String clientKeys = (useBKS ? CLIENT_KEYS_BKS : CLIENT_KEYS_JKS);
        TestServer server = new TestServer(true,
                                           TestServer.CLIENT_AUTH_WANTED, serverKeys);
        TestClient client = new TestClient(true, clientKeys);

        Thread serverThread = new Thread(server);
        Thread clientThread = new Thread(client);

        serverThread.start();
        Thread.currentThread().sleep(3000);
        clientThread.start();

        serverThread.join();
        clientThread.join();

        // The server must have completed without an exception.
        Exception e = server.getException();
        if (e != null) {
            e.printStackTrace();
        }

        // The client must have completed without an exception.
        e = client.getException();
        if (e != null) {
            e.printStackTrace();
        }

        assertNull(e);

        assertTrue(listener.completeDone);
    }

    /**
     * Implements a test SSL socket server. It wait for a connection on a given
     * port, requests client authentication (if specified), reads 256 bytes
     * from the socket, and writes 256 bytes to the socket.
     */
    class TestServer implements Runnable {

        public static final int CLIENT_AUTH_NONE = 0;

        public static final int CLIENT_AUTH_WANTED = 1;

        public static final int CLIENT_AUTH_NEEDED = 2;

        private TestTrustManager trustManager;

        private Exception exception;

        String keys;

        private int clientAuth;

        private boolean provideKeys;

        public TestServer(boolean provideKeys, int clientAuth, String keys) throws Exception {
            this.keys = keys;
            this.clientAuth = clientAuth;
            this.provideKeys = provideKeys;

            trustManager = new TestTrustManager();

            KeyManager[] keyManagers = provideKeys ? getKeyManagers(keys) : null;
            TrustManager[] trustManagers = new TrustManager[] { trustManager };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);

            serverSocket = (SSLServerSocket) sslContext.getServerSocketFactory().createServerSocket();

            if (clientAuth == CLIENT_AUTH_WANTED) {
                serverSocket.setWantClientAuth(true);
            } else if (clientAuth == CLIENT_AUTH_NEEDED) {
                serverSocket.setNeedClientAuth(true);
            } else {
                serverSocket.setWantClientAuth(false);
            }

            serverSocket.bind(new InetSocketAddress(0));
        }

        public void run() {
            try {
                SSLSocket clientSocket = (SSLSocket)serverSocket.accept();

                InputStream istream = clientSocket.getInputStream();

                for (int i = 0; i < 256; i++) {
                    int j = istream.read();
                    assertEquals(i, j);
                }

                istream.close();

                OutputStream ostream = clientSocket.getOutputStream();

                for (int i = 0; i < 256; i++) {
                    ostream.write(i);
                }

                ostream.flush();
                ostream.close();

                clientSocket.close();
                serverSocket.close();

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
     * Implements a test SSL socket client. It open a connection to localhost on
     * a given port, writes 256 bytes to the socket, and reads 256 bytes from the
     * socket.
     */
    class TestClient implements Runnable {

        private TestTrustManager trustManager;

        private Exception exception;

        private String keys;

        private boolean provideKeys;

        public TestClient(boolean provideKeys, String keys) {
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

                SSLSocket socket = (SSLSocket)sslContext.getSocketFactory().createSocket();

                socket.connect(serverSocket.getLocalSocketAddress());
                socket.addHandshakeCompletedListener(listener);
                socket.startHandshake();

                OutputStream ostream = socket.getOutputStream();

                for (int i = 0; i < 256; i++) {
                    ostream.write(i);
                }

                ostream.flush();
                ostream.close();

                InputStream istream = socket.getInputStream();

                for (int i = 0; i < 256; i++) {
                    int j = istream.read();
                    assertEquals(i, j);
                }

                istream.close();

                socket.close();

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


    /**
     * Implements basically a dummy TrustManager. It stores the certificate
     * chain it sees, so it can later be queried.
     */
    public static class TestTrustManager implements X509TrustManager {

        private X509Certificate[] chain;

        private String authType;

        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            this.chain = chain;
            this.authType = authType;
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            this.chain = chain;
            this.authType = authType;
        }

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[0];
        }

        public X509Certificate[] getChain() {
            return chain;
        }

        public String getAuthType() {
            return authType;
        }

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] chain, String authType)
                throws CertificateException {

        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] chain, String authType)
                throws CertificateException {

        }

    }

    class MyHandshakeListener implements HandshakeCompletedListener {

        public boolean completeDone;

        MyHandshakeListener() {
            completeDone = false;
        }

        public void handshakeCompleted(HandshakeCompletedEvent event) {
            if (event != null) completeDone = true;
        }
    }
}
