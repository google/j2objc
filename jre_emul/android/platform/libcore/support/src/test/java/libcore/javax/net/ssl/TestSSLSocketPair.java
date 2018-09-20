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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSocket;

/**
 * TestSSLSocketPair is a convenience class for other tests that want
 * a pair of connected and handshaked client and server SSLSockets for
 * testing.
 */
public final class TestSSLSocketPair {
    public final TestSSLContext c;
    public final SSLSocket server;
    public final SSLSocket client;

    private TestSSLSocketPair (TestSSLContext c,
                               SSLSocket server,
                               SSLSocket client) {
        this.c = c;
        this.server = server;
        this.client = client;
    }

    public void close() {
        c.close();
        try {
            server.close();
            client.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * based on test_SSLSocket_startHandshake
     */
    public static TestSSLSocketPair create () {
        TestSSLContext c = TestSSLContext.create();
        SSLSocket[] sockets = connect(c, null, null);
        return new TestSSLSocketPair(c, sockets[0], sockets[1]);
    }

    /**
     * Create a new connected server/client socket pair within a
     * existing SSLContext. Optionally specify clientCipherSuites to
     * allow forcing new SSLSession to test SSLSessionContext
     * caching. Optionally specify serverCipherSuites for testing
     * cipher suite negotiation.
     */
    public static SSLSocket[] connect (final TestSSLContext context,
                                       final String[] clientCipherSuites,
                                       final String[] serverCipherSuites) {
        try {
            final SSLSocket client = (SSLSocket)
                context.clientContext.getSocketFactory().createSocket(context.host, context.port);
            final SSLSocket server = (SSLSocket) context.serverSocket.accept();

            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future s = executor.submit(new Callable<Void>() {
                    public Void call() throws Exception {
                        if (serverCipherSuites != null) {
                            server.setEnabledCipherSuites(serverCipherSuites);
                        }
                        server.startHandshake();
                        return null;
                    }
                });
            Future c = executor.submit(new Callable<Void>() {
                    public Void call() throws Exception {
                        if (clientCipherSuites != null) {
                            client.setEnabledCipherSuites(clientCipherSuites);
                        }
                        client.startHandshake();
                        return null;
                    }
                });
            executor.shutdown();

            // catch client and server exceptions separately so we can
            // potentially log both.
            Exception serverException;
            try {
                s.get(30, TimeUnit.SECONDS);
                serverException = null;
            } catch (Exception e) {
                serverException = e;
                e.printStackTrace();
            }
            Exception clientException;
            try {
                c.get(30, TimeUnit.SECONDS);
                clientException = null;
            } catch (Exception e) {
                clientException = e;
                e.printStackTrace();
            }
            if (serverException != null) {
                throw serverException;
            }
            if (clientException != null) {
                throw clientException;
            }
            return new SSLSocket[] { server, client };
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

