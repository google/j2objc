/*
 * Copyright (C) 2011 Google Inc.
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

package com.google.mockwebserver;

import static com.google.mockwebserver.SocketPolicy.DISCONNECT_AT_START;
import static com.google.mockwebserver.SocketPolicy.FAIL_HANDSHAKE;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * A scriptable web server. Callers supply canned responses and the server
 * replays them upon request in sequence.
 */
public final class MockWebServer {
    private static final X509TrustManager UNTRUSTED_TRUST_MANAGER = new X509TrustManager() {
        @Override public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            throw new CertificateException();
        }

        @Override public void checkServerTrusted(X509Certificate[] chain, String authType) {
            throw new AssertionError();
        }

        @Override public X509Certificate[] getAcceptedIssuers() {
            throw new AssertionError();
        }
    };

    private static final Logger logger = Logger.getLogger(MockWebServer.class.getName());

    private final BlockingQueue<RecordedRequest> requestQueue
            = new LinkedBlockingQueue<RecordedRequest>();

    /** All map values are Boolean.TRUE. (Collections.newSetFromMap isn't available in Froyo) */
    private final Map<Socket, Boolean> openClientSockets = new ConcurrentHashMap<Socket, Boolean>();
    private final AtomicInteger requestCount = new AtomicInteger();
    private int bodyLimit = Integer.MAX_VALUE;
    private ServerSocket serverSocket;
    private SSLSocketFactory sslSocketFactory;
    private ExecutorService acceptExecutor;
    private ExecutorService requestExecutor;
    private boolean tunnelProxy;
    private Dispatcher dispatcher = new QueueDispatcher();

    private int port = -1;
    private int workerThreads = Integer.MAX_VALUE;

    public int getPort() {
        if (port == -1) {
            throw new IllegalStateException("Cannot retrieve port before calling play()");
        }
        return port;
    }

    public String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new AssertionError(e);
        }
    }

    public Proxy toProxyAddress() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(getHostName(), getPort()));
    }

    /**
     * Returns a URL for connecting to this server.
     *
     * @param path the request path, such as "/".
     */
    public URL getUrl(String path) {
        try {
            return sslSocketFactory != null
                    ? new URL("https://" + getHostName() + ":" + getPort() + path)
                    : new URL("http://" + getHostName() + ":" + getPort() + path);
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns a cookie domain for this server. This returns the server's
     * non-loopback host name if it is known. Otherwise this returns ".local"
     * for this server's loopback name.
     */
    public String getCookieDomain() {
        String hostName = getHostName();
        return hostName.contains(".") ? hostName : ".local";
    }

    public void setWorkerThreads(int threads) {
        this.workerThreads = threads;
    }

    /**
     * Sets the number of bytes of the POST body to keep in memory to the given
     * limit.
     */
    public void setBodyLimit(int maxBodyLength) {
        this.bodyLimit = maxBodyLength;
    }

    /**
     * Serve requests with HTTPS rather than otherwise.
     *
     * @param tunnelProxy whether to expect the HTTP CONNECT method before
     *     negotiating TLS.
     */
    public void useHttps(SSLSocketFactory sslSocketFactory, boolean tunnelProxy) {
        this.sslSocketFactory = sslSocketFactory;
        this.tunnelProxy = tunnelProxy;
    }

    /**
     * Awaits the next HTTP request, removes it, and returns it. Callers should
     * use this to verify the request sent was as intended.
     */
    public RecordedRequest takeRequest() throws InterruptedException {
        return requestQueue.take();
    }

    /**
     * Returns the number of HTTP requests received thus far by this server.
     * This may exceed the number of HTTP connections when connection reuse is
     * in practice.
     */
    public int getRequestCount() {
        return requestCount.get();
    }

    /**
     * Scripts {@code response} to be returned to a request made in sequence.
     * The first request is served by the first enqueued response; the second
     * request by the second enqueued response; and so on.
     *
     * @throws ClassCastException if the default dispatcher has been replaced
     *     with {@link #setDispatcher(Dispatcher)}.
     */
    public void enqueue(MockResponse response) {
        ((QueueDispatcher) dispatcher).enqueueResponse(response.clone());
    }

    /**
     * Equivalent to {@code play(0)}.
     */
    public void play() throws IOException {
        play(0);
    }

    /**
     * Starts the server, serves all enqueued requests, and shuts the server
     * down.
     *
     * @param port the port to listen to, or 0 for any available port.
     *     Automated tests should always use port 0 to avoid flakiness when a
     *     specific port is unavailable.
     */
    public void play(int port) throws IOException {
        if (acceptExecutor != null) {
            throw new IllegalStateException("play() already called");
        }
        // The acceptExecutor handles the Socket.accept() and hands each request off to the
        // requestExecutor. It also handles shutdown.
        acceptExecutor = Executors.newSingleThreadExecutor();
        // The requestExecutor has a fixed number of worker threads. In order to get strict
        // guarantees that requests are handled in the order in which they are accepted
        // workerThreads should be set to 1.
        requestExecutor = Executors.newFixedThreadPool(workerThreads);
        serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);

        this.port = serverSocket.getLocalPort();
        acceptExecutor.execute(namedRunnable("MockWebServer-accept-" + port, new Runnable() {
            public void run() {
                try {
                    acceptConnections();
                } catch (Throwable e) {
                    logger.log(Level.WARNING, "MockWebServer connection failed", e);
                }

                /*
                 * This gnarly block of code will release all sockets and
                 * all thread, even if any close fails.
                 */
                try {
                    serverSocket.close();
                } catch (Throwable e) {
                    logger.log(Level.WARNING, "MockWebServer server socket close failed", e);
                }
                for (Iterator<Socket> s = openClientSockets.keySet().iterator(); s.hasNext(); ) {
                    try {
                        s.next().close();
                        s.remove();
                    } catch (Throwable e) {
                        logger.log(Level.WARNING, "MockWebServer socket close failed", e);
                    }
                }
                try {
                    acceptExecutor.shutdown();
                } catch (Throwable e) {
                    logger.log(Level.WARNING, "MockWebServer acceptExecutor shutdown failed", e);
                }
                try {
                    requestExecutor.shutdown();
                } catch (Throwable e) {
                    logger.log(Level.WARNING, "MockWebServer requestExecutor shutdown failed", e);
                }
            }

            private void acceptConnections() throws Exception {
                while (true) {
                    Socket socket;
                    try {
                        socket = serverSocket.accept();
                    } catch (SocketException e) {
                        return;
                    }
                    SocketPolicy socketPolicy = dispatcher.peek().getSocketPolicy();
                    if (socketPolicy == DISCONNECT_AT_START) {
                        dispatchBookkeepingRequest(0, socket);
                        socket.close();
                    } else {
                        openClientSockets.put(socket, true);
                        serveConnection(socket);
                    }
                }
            }
        }));
    }

    public void shutdown() throws IOException {
        if (serverSocket != null) {
            serverSocket.close(); // should cause acceptConnections() to break out
        }
    }

    private void serveConnection(final Socket raw) {
        String name = "MockWebServer-" + raw.getRemoteSocketAddress();
        requestExecutor.execute(namedRunnable(name, new Runnable() {
            int sequenceNumber = 0;

            public void run() {
                try {
                    processConnection();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "MockWebServer connection failed", e);
                }
            }

            public void processConnection() throws Exception {
                Socket socket;
                if (sslSocketFactory != null) {
                    if (tunnelProxy) {
                        createTunnel();
                    }
                    SocketPolicy socketPolicy = dispatcher.peek().getSocketPolicy();
                    if (socketPolicy == FAIL_HANDSHAKE) {
                        dispatchBookkeepingRequest(sequenceNumber, raw);
                        processHandshakeFailure(raw);
                        return;
                    }
                    socket = sslSocketFactory.createSocket(
                            raw, raw.getInetAddress().getHostAddress(), raw.getPort(), true);
                    SSLSocket sslSocket = (SSLSocket) socket;
                    sslSocket.setUseClientMode(false);
                    openClientSockets.put(socket, true);

                    sslSocket.startHandshake();

                    openClientSockets.remove(raw);
                } else {
                    socket = raw;
                }

                InputStream in = new BufferedInputStream(socket.getInputStream());
                OutputStream out = new BufferedOutputStream(socket.getOutputStream());

                while (processOneRequest(socket, in, out)) {
                }

                if (sequenceNumber == 0) {
                    logger.warning("MockWebServer connection didn't make a request");
                }

                in.close();
                out.close();
                socket.close();
                openClientSockets.remove(socket);
            }

            /**
             * Respond to CONNECT requests until a SWITCH_TO_SSL_AT_END response
             * is dispatched.
             */
            private void createTunnel() throws IOException, InterruptedException {
                while (true) {
                    SocketPolicy socketPolicy = dispatcher.peek().getSocketPolicy();
                    if (!processOneRequest(raw, raw.getInputStream(), raw.getOutputStream())) {
                        throw new IllegalStateException("Tunnel without any CONNECT!");
                    }
                    if (socketPolicy == SocketPolicy.UPGRADE_TO_SSL_AT_END) return;
                }
            }

            /**
             * Reads a request and writes its response. Returns true if a request
             * was processed.
             */
            private boolean processOneRequest(Socket socket, InputStream in, OutputStream out)
                    throws IOException, InterruptedException {
                RecordedRequest request = readRequest(socket, in, out, sequenceNumber);
                if (request == null) {
                    return false;
                }
                requestCount.incrementAndGet();
                requestQueue.add(request);
                MockResponse response = dispatcher.dispatch(request);
                if (response.getSocketPolicy() == SocketPolicy.DISCONNECT_AFTER_READING_REQUEST) {
                  logger.info("Received request: " + request + " and disconnected without responding");
                  return false;
                }
                writeResponse(out, response);

                // For socket policies that poison the socket after the response is written:
                // The client has received the response and will no longer be blocked after
                // writeResponse() has returned. A client can then re-use the connection before
                // the socket is poisoned (i.e. keep-alive / connection pooling). The second
                // request/response may fail at the beginning, middle, end, or even succeed
                // depending on scheduling. Delays can be required in tests to improve the chances
                // of sockets being in a known state when subsequent requests are made.
                //
                // For SHUTDOWN_OUTPUT_AT_END the client may detect a problem with its input socket
                // after the request has been made but before the server has chosen a response.
                // For clients that perform retries, this can cause the client to issue a retry
                // request. The retry handler may call dispatcher.dispatch(request) before the
                // initial, failed request handler does and cause non-obvious response ordering.
                // Setting workerThreads = 1 ensures that the dispatcher is called for requests in
                // the order they are received.

                if (response.getSocketPolicy() == SocketPolicy.DISCONNECT_AT_END) {
                    in.close();
                    out.close();
                } else if (response.getSocketPolicy() == SocketPolicy.SHUTDOWN_INPUT_AT_END) {
                    socket.shutdownInput();
                } else if (response.getSocketPolicy() == SocketPolicy.SHUTDOWN_OUTPUT_AT_END) {
                    socket.shutdownOutput();
                }
                logger.info("Received request: " + request + " and responded: " + response);
                sequenceNumber++;
                return true;
            }
        }));
    }

    private void processHandshakeFailure(Socket raw) throws Exception {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[] { UNTRUSTED_TRUST_MANAGER }, new SecureRandom());
        SSLSocketFactory sslSocketFactory = context.getSocketFactory();
        SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(
                raw, raw.getInetAddress().getHostAddress(), raw.getPort(), true);
        try {
            socket.startHandshake(); // we're testing a handshake failure
            throw new AssertionError();
        } catch (IOException expected) {
        }
        socket.close();
    }

    private void dispatchBookkeepingRequest(int sequenceNumber, Socket socket) throws InterruptedException {
        requestCount.incrementAndGet();
        RecordedRequest request = new RecordedRequest(null, null, null, -1, null, sequenceNumber,
                socket);
        dispatcher.dispatch(request);
    }

    /** @param sequenceNumber the index of this request on this connection. */
    private RecordedRequest readRequest(Socket socket, InputStream in, OutputStream out,
            int sequenceNumber) throws IOException {
        String request;
        try {
            request = readAsciiUntilCrlf(in);
        } catch (IOException streamIsClosed) {
            return null; // no request because we closed the stream
        }
        if (request.length() == 0) {
            return null; // no request because the stream is exhausted
        }

        List<String> headers = new ArrayList<String>();
        long contentLength = -1;
        boolean chunked = false;
        boolean expectContinue = false;
        String header;
        while ((header = readAsciiUntilCrlf(in)).length() != 0) {
            headers.add(header);
            String lowercaseHeader = header.toLowerCase(Locale.US);
            if (contentLength == -1 && lowercaseHeader.startsWith("content-length:")) {
                contentLength = Long.parseLong(header.substring(15).trim());
            }
            if (lowercaseHeader.startsWith("transfer-encoding:")
                    && lowercaseHeader.substring(18).trim().equals("chunked")) {
                chunked = true;
            }
            if (lowercaseHeader.startsWith("expect:")
                    && lowercaseHeader.substring(7).trim().equals("100-continue")) {
                expectContinue = true;
            }
        }

        if (expectContinue) {
            out.write(("HTTP/1.1 100 Continue\r\n").getBytes(StandardCharsets.US_ASCII));
            out.write(("Content-Length: 0\r\n").getBytes(StandardCharsets.US_ASCII));
            out.write(("\r\n").getBytes(StandardCharsets.US_ASCII));
            out.flush();
        }

        boolean hasBody = false;
        TruncatingOutputStream requestBody = new TruncatingOutputStream();
        List<Integer> chunkSizes = new ArrayList<Integer>();
        MockResponse throttlePolicy = dispatcher.peek();
        if (contentLength != -1) {
            hasBody = true;
            throttledTransfer(throttlePolicy, in, requestBody, contentLength);
        } else if (chunked) {
            hasBody = true;
            while (true) {
                int chunkSize = Integer.parseInt(readAsciiUntilCrlf(in).trim(), 16);
                if (chunkSize == 0) {
                    readEmptyLine(in);
                    break;
                }
                chunkSizes.add(chunkSize);
                throttledTransfer(throttlePolicy, in, requestBody, chunkSize);
                readEmptyLine(in);
            }
        }

        if (request.startsWith("OPTIONS ")
                || request.startsWith("GET ")
                || request.startsWith("HEAD ")
                || request.startsWith("TRACE ")
                || request.startsWith("CONNECT ")) {
            if (hasBody) {
                throw new IllegalArgumentException("Request must not have a body: " + request);
            }
        } else if (!request.startsWith("POST ")
                && !request.startsWith("PUT ")
                && !request.startsWith("PATCH ")
                && !request.startsWith("DELETE ")) { // Permitted as spec is ambiguous.
            throw new UnsupportedOperationException("Unexpected method: " + request);
        }

        return new RecordedRequest(request, headers, chunkSizes, requestBody.numBytesReceived,
                requestBody.toByteArray(), sequenceNumber, socket);
    }

    private void writeResponse(OutputStream out, MockResponse response) throws IOException {
        out.write((response.getStatus() + "\r\n").getBytes(StandardCharsets.US_ASCII));
        List<String> headers = response.getHeaders();
        for (int i = 0, size = headers.size(); i < size; i++) {
            String header = headers.get(i);
            out.write((header + "\r\n").getBytes(StandardCharsets.US_ASCII));
        }
        out.write(("\r\n").getBytes(StandardCharsets.US_ASCII));
        out.flush();

        InputStream in = response.getBodyStream();
        if (in == null) return;
        throttledTransfer(response, in, out, Long.MAX_VALUE);
    }

    /**
     * Transfer bytes from {@code in} to {@code out} until either {@code length}
     * bytes have been transferred or {@code in} is exhausted. The transfer is
     * throttled according to {@code throttlePolicy}.
     */
    private void throttledTransfer(MockResponse throttlePolicy, InputStream in, OutputStream out,
            long limit) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesPerPeriod = throttlePolicy.getThrottleBytesPerPeriod();
        long delayMs = throttlePolicy.getThrottleUnit().toMillis(throttlePolicy.getThrottlePeriod());

        while (true) {
            for (int b = 0; b < bytesPerPeriod; ) {
                int toRead = (int) Math.min(Math.min(buffer.length, limit), bytesPerPeriod - b);
                int read = in.read(buffer, 0, toRead);
                if (read == -1) return;

                out.write(buffer, 0, read);
                out.flush();
                b += read;
                limit -= read;

                if (limit == 0) return;
            }

            try {
                if (delayMs != 0) Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                throw new AssertionError();
            }
        }
    }

    /**
     * Returns the text from {@code in} until the next "\r\n", or null if
     * {@code in} is exhausted.
     */
    private String readAsciiUntilCrlf(InputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();
        while (true) {
            int c = in.read();
            if (c == '\n' && builder.length() > 0 && builder.charAt(builder.length() - 1) == '\r') {
                builder.deleteCharAt(builder.length() - 1);
                return builder.toString();
            } else if (c == -1) {
                return builder.toString();
            } else {
                builder.append((char) c);
            }
        }
    }

    private void readEmptyLine(InputStream in) throws IOException {
        String line = readAsciiUntilCrlf(in);
        if (line.length() != 0) {
            throw new IllegalStateException("Expected empty but was: " + line);
        }
    }

    /**
     * Sets the dispatcher used to match incoming requests to mock responses.
     * The default dispatcher simply serves a fixed sequence of responses from
     * a {@link #enqueue(MockResponse) queue}; custom dispatchers can vary the
     * response based on timing or the content of the request.
     */
    public void setDispatcher(Dispatcher dispatcher) {
        if (dispatcher == null) {
            throw new NullPointerException();
        }
        this.dispatcher = dispatcher;
    }

    /**
     * An output stream that drops data after bodyLimit bytes.
     */
    private class TruncatingOutputStream extends ByteArrayOutputStream {
        private int numBytesReceived = 0;
        @Override public void write(byte[] buffer, int offset, int len) {
            numBytesReceived += len;
            super.write(buffer, offset, Math.min(len, bodyLimit - count));
        }
        @Override public void write(int oneByte) {
            numBytesReceived++;
            if (count < bodyLimit) {
                super.write(oneByte);
            }
        }
    }

    private static Runnable namedRunnable(final String name, final Runnable runnable) {
        return new Runnable() {
            public void run() {
                String originalName = Thread.currentThread().getName();
                Thread.currentThread().setName(name);
                try {
                    runnable.run();
                } finally {
                    Thread.currentThread().setName(originalName);
                }
            }
        };
    }
}
