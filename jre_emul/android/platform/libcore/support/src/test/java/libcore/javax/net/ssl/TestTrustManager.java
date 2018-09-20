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

import java.io.PrintStream;
import java.net.Socket;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import libcore.java.io.NullPrintStream;
import libcore.java.security.StandardNames;

/**
 * TestTrustManager is a simple proxy class that wraps an existing
 * X509ExtendedTrustManager to provide debug logging and recording of
 * values.
 */
public final class TestTrustManager extends X509ExtendedTrustManager {

    private static final boolean LOG = false;
    private static final PrintStream out = LOG ? System.out : new NullPrintStream();

    private final X509TrustManager trustManager;
    private final X509ExtendedTrustManager extendedTrustManager;

    public static TrustManager[] wrap(TrustManager[] trustManagers) {
        TrustManager[] result = trustManagers.clone();
        for (int i = 0; i < result.length; i++) {
            result[i] = wrap(result[i]);
        }
        return result;
    }

    public static TrustManager wrap(TrustManager trustManager) {
        if (trustManager instanceof X509ExtendedTrustManager) {
            return new TestTrustManager((X509ExtendedTrustManager) trustManager);
        } else if (trustManager instanceof X509TrustManager) {
            return new TestTrustManager((X509TrustManager) trustManager);
        }
        return trustManager;
    }

    public TestTrustManager(X509ExtendedTrustManager trustManager) {
        out.println("TestTrustManager.<init> extendedTrustManager=" + trustManager);
        this.extendedTrustManager = trustManager;
        this.trustManager = trustManager;
    }

    public TestTrustManager(X509TrustManager trustManager) {
        out.println("TestTrustManager.<init> trustManager=" + trustManager);
        this.extendedTrustManager = null;
        this.trustManager = trustManager;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        out.print("TestTrustManager.checkClientTrusted "
                  + "chain=" + chain.length + " "
                  + "authType=" + authType + " ");
        try {
            assertClientAuthType(authType);
            trustManager.checkClientTrusted(chain, authType);
            out.println("OK");
        } catch (CertificateException e) {
            e.printStackTrace(out);
            throw e;
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {
        if (extendedTrustManager == null) {
            out.print("(fallback to X509TrustManager) ");
            checkClientTrusted(chain, authType);
            return;
        }
        out.print("TestTrustManager.checkClientTrusted "
                + "chain=" + chain.length + " "
                + "authType=" + authType + " "
                + "socket=" + socket + " ");
        try {
            assertClientAuthType(authType);
            extendedTrustManager.checkClientTrusted(chain, authType, socket);
            out.println("OK");
        } catch (CertificateException e) {
            e.printStackTrace(out);
            throw e;
        }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
            throws CertificateException {
        if (extendedTrustManager == null) {
            out.print("(fallback to X509TrustManager) ");
            checkClientTrusted(chain, authType);
            return;
        }
        out.print("TestTrustManager.checkClientTrusted "
                + "chain=" + chain.length + " "
                + "authType=" + authType + " "
                + "engine=" + engine + " ");
        try {
            assertClientAuthType(authType);
            extendedTrustManager.checkClientTrusted(chain, authType, engine);
            out.println("OK");
        } catch (CertificateException e) {
            e.printStackTrace(out);
            throw e;
        }
    }

    private void assertClientAuthType(String authType) {
        if (!StandardNames.CLIENT_AUTH_TYPES.contains(authType)) {
            throw new AssertionError("Unexpected client auth type " + authType);
        }
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        out.print("TestTrustManager.checkServerTrusted "
                  + "chain=" + chain.length + " "
                  + "authType=" + authType + " ");
        try {
            assertServerAuthType(authType);
            trustManager.checkServerTrusted(chain, authType);
            out.println("OK");
        } catch (CertificateException e) {
            e.printStackTrace(out);
            throw e;
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
            throws CertificateException {
        if (extendedTrustManager == null) {
            out.print("(fallback to X509TrustManager) ");
            checkServerTrusted(chain, authType);
            return;
        }
        out.print("TestTrustManager.checkServerTrusted "
                + "chain=" + chain.length + " "
                + "authType=" + authType + " "
                + "socket=" + socket.toString() + " ");
        try {
            assertServerAuthType(authType);
            extendedTrustManager.checkServerTrusted(chain, authType, socket);
            out.println("OK");
        } catch (CertificateException e) {
            e.printStackTrace(out);
            throw e;
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
            throws CertificateException {
        if (extendedTrustManager == null) {
            out.print("(fallback to X509TrustManager) ");
            checkServerTrusted(chain, authType);
            return;
        }
        out.print("TestTrustManager.checkServerTrusted "
                + "chain=" + chain.length + " "
                + "authType=" + authType + " "
                + "engine=" + engine.toString() + " ");
        try {
            assertServerAuthType(authType);
            extendedTrustManager.checkServerTrusted(chain, authType, engine);
            out.println("OK");
        } catch (CertificateException e) {
            e.printStackTrace(out);
            throw e;
        }
    }

    private void assertServerAuthType(String authType) {
        if (!StandardNames.SERVER_AUTH_TYPES.contains(authType)) {
            throw new AssertionError("Unexpected server auth type " + authType);
        }
    }

    /**
     * Returns the list of certificate issuer authorities which are trusted for
     * authentication of peers.
     *
     * @return the list of certificate issuer authorities which are trusted for
     *         authentication of peers.
     */
    public X509Certificate[] getAcceptedIssuers() {
        X509Certificate[] result = trustManager.getAcceptedIssuers();
        out.print("TestTrustManager.getAcceptedIssuers result=" + result.length);
        return result;
    }
}

