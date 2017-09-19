/*
 * Copyright (C) 2009 The Android Open Source Project
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

package libcore.java.net;

import com.google.mockwebserver.Dispatcher;
import com.google.mockwebserver.MockResponse;
import com.google.mockwebserver.MockWebServer;
import com.google.mockwebserver.RecordedRequest;
import com.google.mockwebserver.SocketPolicy;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.HttpRetryException;
import java.net.HttpURLConnection;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.ResponseCache;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.net.SocketFactory;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import libcore.java.util.AbstractResourceLeakageDetectorTestCase;
import tests.net.DelegatingSocketFactory;

import static com.google.mockwebserver.SocketPolicy.DISCONNECT_AT_END;
import static com.google.mockwebserver.SocketPolicy.DISCONNECT_AT_START;
import static com.google.mockwebserver.SocketPolicy.FAIL_HANDSHAKE;
import static com.google.mockwebserver.SocketPolicy.SHUTDOWN_INPUT_AT_END;
import static com.google.mockwebserver.SocketPolicy.SHUTDOWN_OUTPUT_AT_END;

public final class URLConnectionTest extends AbstractResourceLeakageDetectorTestCase {

    private MockWebServer server;
    private String hostName;
    private Object savedUrlCache;

    @Override protected void setUp() throws Exception {
        super.setUp();
        savedUrlCache = setNewUrlCache();
        server = new MockWebServer();
        hostName = server.getHostName();
    }

    @Override protected void tearDown() throws Exception {
        ResponseCache.setDefault(null);
        Authenticator.setDefault(null);
        System.clearProperty("proxyHost");
        System.clearProperty("proxyPort");
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
        System.clearProperty("https.proxyHost");
        System.clearProperty("https.proxyPort");
        server.shutdown();
        restoreUrlCache(savedUrlCache);
        server = null;
        super.tearDown();
    }

    // Make sure each test runs with a clean cache. Otherwise, a test may get a cache hit from the
    // previous test.
    private static native Object setNewUrlCache() /*-[
      NSURLCache *oldCache = [NSURLCache sharedURLCache];
      NSURLCache *newCache =
          [[NSURLCache alloc] initWithMemoryCapacity:0 diskCapacity:0 diskPath:nil];
      [NSURLCache setSharedURLCache:newCache];
      [newCache release];
      return oldCache;
    ]-*/;

    private static native void restoreUrlCache(Object savedCache) /*-[
      [NSURLCache setSharedURLCache:savedCache];
    ]-*/;

//  JVM failure.
//    public void testRequestHeaderValidation() throws Exception {
//        // Android became more strict after M about which characters were allowed in request header
//        // names and values: previously almost anything was allowed if it didn't contain \0.
//
//        assertForbiddenRequestHeaderName(null);
//        assertForbiddenRequestHeaderName("");
//        assertForbiddenRequestHeaderName("\n");
//        assertForbiddenRequestHeaderName("a\nb");
//        assertForbiddenRequestHeaderName("\u0000");
//        assertForbiddenRequestHeaderName("\r");
//        assertForbiddenRequestHeaderName("\t");
//        assertForbiddenRequestHeaderName("\u001f");
//        assertForbiddenRequestHeaderName("\u007f");
//        assertForbiddenRequestHeaderName("\u0080");
//        assertForbiddenRequestHeaderName("\ud83c\udf69");
//
//        assertEquals(null, setAndReturnRequestHeaderValue(null));
//        assertEquals("", setAndReturnRequestHeaderValue(""));
//        assertForbiddenRequestHeaderValue("\u0000");
//
//        // Workaround for http://b/26422335 , http://b/26889631 , http://b/27606665 :
//        // allow (but strip) trailing \n, \r and \r\n
//        // assertForbiddenRequestHeaderValue("\r");
//        // End of workaround
//        assertForbiddenRequestHeaderValue("\t");
//        assertForbiddenRequestHeaderValue("\u001f");
//        assertForbiddenRequestHeaderValue("\u007f");
//
//        // For http://b/28867041 the allowed character range was changed.
//        // assertForbiddenRequestHeaderValue("\u0080");
//        // assertForbiddenRequestHeaderValue("\ud83c\udf69");
//        assertEquals("\u0080", setAndReturnRequestHeaderValue("\u0080"));
//        assertEquals("\ud83c\udf69", setAndReturnRequestHeaderValue("\ud83c\udf69"));
//
//        // Workaround for http://b/26422335 , http://b/26889631 , http://b/27606665 :
//        // allow (but strip) trailing \n, \r and \r\n
//        assertEquals("", setAndReturnRequestHeaderValue("\n"));
//        assertEquals("a", setAndReturnRequestHeaderValue("a\n"));
//        assertEquals("", setAndReturnRequestHeaderValue("\r"));
//        assertEquals("a", setAndReturnRequestHeaderValue("a\r"));
//        assertEquals("", setAndReturnRequestHeaderValue("\r\n"));
//        assertEquals("a", setAndReturnRequestHeaderValue("a\r\n"));
//        assertForbiddenRequestHeaderValue("a\nb");
//        assertForbiddenRequestHeaderValue("\nb");
//        assertForbiddenRequestHeaderValue("a\rb");
//        assertForbiddenRequestHeaderValue("\rb");
//        assertForbiddenRequestHeaderValue("a\r\nb");
//        assertForbiddenRequestHeaderValue("\r\nb");
//        // End of workaround
//    }

    private static void assertForbiddenRequestHeaderName(String name) throws Exception {
        URL url = new URL("http://www.google.com/");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.addRequestProperty(name, "value");
            fail("Expected exception");
        } catch (IllegalArgumentException expected) {
        } catch (NullPointerException expectedIfNull) {
            assertTrue(name == null);
        }
    }

    private static void assertForbiddenRequestHeaderValue(String value) throws Exception {
        URL url = new URL("http://www.google.com/");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.addRequestProperty("key", value);
            fail("Expected exception");
        } catch (IllegalArgumentException expected) {
        }
    }

    private static String setAndReturnRequestHeaderValue(String value) throws Exception {
        URL url = new URL("http://www.google.com/");
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.addRequestProperty("key", value);
        return urlConnection.getRequestProperty("key");
    }

// TODO(tball): b/28067294
//    public void testRequestHeaders() throws IOException, InterruptedException {
//        server.enqueue(new MockResponse());
//        server.play();
//
//        HttpURLConnection urlConnection = (HttpURLConnection) server.getUrl("/").openConnection();
//        urlConnection.addRequestProperty("D", "e");
//        urlConnection.addRequestProperty("D", "f");
//        assertEquals("f", urlConnection.getRequestProperty("D"));
//        assertEquals("f", urlConnection.getRequestProperty("d"));
//        Map<String, List<String>> requestHeaders = urlConnection.getRequestProperties();
//        assertEquals(newSet("e", "f"), new HashSet<String>(requestHeaders.get("D")));
//        assertEquals(newSet("e", "f"), new HashSet<String>(requestHeaders.get("d")));
//        try {
//            requestHeaders.put("G", Arrays.asList("h"));
//            fail("Modified an unmodifiable view.");
//        } catch (UnsupportedOperationException expected) {
//        }
//        try {
//            requestHeaders.get("D").add("i");
//            fail("Modified an unmodifiable view.");
//        } catch (UnsupportedOperationException expected) {
//        }
//        try {
//            urlConnection.setRequestProperty(null, "j");
//            fail();
//        } catch (NullPointerException expected) {
//        }
//        try {
//            urlConnection.addRequestProperty(null, "k");
//            fail();
//        } catch (NullPointerException expected) {
//        }
//        urlConnection.setRequestProperty("NullValue", null); // should fail silently!
//        assertNull(urlConnection.getRequestProperty("NullValue"));
//        urlConnection.addRequestProperty("AnotherNullValue", null);  // should fail silently!
//        assertNull(urlConnection.getRequestProperty("AnotherNullValue"));
//
//        urlConnection.getResponseCode();
//        RecordedRequest request = server.takeRequest();
//        assertContains(request.getHeaders(), "D: e");
//        assertContains(request.getHeaders(), "D: f");
//        assertContainsNoneMatching(request.getHeaders(), "NullValue.*");
//        assertContainsNoneMatching(request.getHeaders(), "AnotherNullValue.*");
//        assertContainsNoneMatching(request.getHeaders(), "G:.*");
//        assertContainsNoneMatching(request.getHeaders(), "null:.*");
//
//        try {
//            urlConnection.addRequestProperty("N", "o");
//            fail("Set header after connect");
//        } catch (IllegalStateException expected) {
//        }
//        try {
//            urlConnection.setRequestProperty("P", "q");
//            fail("Set header after connect");
//        } catch (IllegalStateException expected) {
//        }
//        try {
//            urlConnection.getRequestProperties();
//            fail();
//        } catch (IllegalStateException expected) {
//        }
//    }

    public void testGetRequestPropertyReturnsLastValue() throws Exception {
        server.play();
        HttpURLConnection urlConnection = (HttpURLConnection) server.getUrl("/").openConnection();
        urlConnection.addRequestProperty("A", "value1");
        urlConnection.addRequestProperty("A", "value2");
        assertEquals("value2", urlConnection.getRequestProperty("A"));
    }

//  JVM failure.
//    public void testResponseHeaders() throws IOException, InterruptedException {
//        server.enqueue(new MockResponse()
//                .setStatus("HTTP/1.0 200 Fantastic")
//                .addHeader("A: c")
//                .addHeader("B: d")
//                .addHeader("A: e")
//                .setChunkedBody("ABCDE\nFGHIJ\nKLMNO\nPQR", 8));
//        server.play();
//
//        HttpURLConnection urlConnection = (HttpURLConnection) server.getUrl("/").openConnection();
//        assertEquals(200, urlConnection.getResponseCode());
//        assertEquals("Fantastic", urlConnection.getResponseMessage());
//        assertEquals("HTTP/1.0 200 Fantastic", urlConnection.getHeaderField(null));
//        Map<String, List<String>> responseHeaders = urlConnection.getHeaderFields();
//        assertEquals(Arrays.asList("HTTP/1.0 200 Fantastic"), responseHeaders.get(null));
//        assertEquals(newSet("c", "e"), new HashSet<String>(responseHeaders.get("A")));
//        assertEquals(newSet("c", "e"), new HashSet<String>(responseHeaders.get("a")));
//        try {
//            responseHeaders.put("N", Arrays.asList("o"));
//            fail("Modified an unmodifiable view.");
//        } catch (UnsupportedOperationException expected) {
//        }
//        try {
//            responseHeaders.get("A").add("f");
//            fail("Modified an unmodifiable view.");
//        } catch (UnsupportedOperationException expected) {
//        }
//        assertEquals("A", urlConnection.getHeaderFieldKey(0));
//        assertEquals("c", urlConnection.getHeaderField(0));
//        assertEquals("B", urlConnection.getHeaderFieldKey(1));
//        assertEquals("d", urlConnection.getHeaderField(1));
//        assertEquals("A", urlConnection.getHeaderFieldKey(2));
//        assertEquals("e", urlConnection.getHeaderField(2));
//    }

    public void testGetErrorStreamOnSuccessfulRequest() throws Exception {
        server.enqueue(new MockResponse().setBody("A"));
        server.play();
        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        assertEquals(200, connection.getResponseCode());
        assertNull(connection.getErrorStream());
    }

    public void testGetErrorStreamOnUnsuccessfulRequest() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(404).setBody("A"));
        server.play();
        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        assertEquals(404, connection.getResponseCode());
        assertEquals("A", readAscii(connection.getErrorStream(), Integer.MAX_VALUE));
    }

    // Check that if we don't read to the end of a response, the next request on the
    // recycled connection doesn't get the unread tail of the first request's response.
    // http://code.google.com/p/android/issues/detail?id=2939
    public void test_2939() throws Exception {
        MockResponse response = new MockResponse().setChunkedBody("ABCDE\nFGHIJ\nKLMNO\nPQR", 8);

        server.enqueue(response);
        server.enqueue(response);
        server.play();

        assertContent("ABCDE", server.getUrl("/").openConnection(), 5);
        assertContent("ABCDE", server.getUrl("/").openConnection(), 5);
    }

    // Check that we recognize a few basic mime types by extension.
    // http://code.google.com/p/android/issues/detail?id=10100
    public void test_10100() throws Exception {
        assertEquals("image/jpeg", URLConnection.guessContentTypeFromName("someFile.jpg"));
        assertEquals("application/pdf", URLConnection.guessContentTypeFromName("stuff.pdf"));
    }

// TODO(tball): b/28067294
//    public void testConnectionsArePooled() throws Exception {
//        MockResponse response = new MockResponse().setBody("ABCDEFGHIJKLMNOPQR");
//
//        server.enqueue(response);
//        server.enqueue(response);
//        server.enqueue(response);
//        server.play();
//
//        assertContent("ABCDEFGHIJKLMNOPQR", server.getUrl("/foo").openConnection());
//        assertEquals(0, server.takeRequest().getSequenceNumber());
//        assertContent("ABCDEFGHIJKLMNOPQR", server.getUrl("/bar?baz=quux").openConnection());
//        assertEquals(1, server.takeRequest().getSequenceNumber());
//        assertContent("ABCDEFGHIJKLMNOPQR", server.getUrl("/z").openConnection());
//        assertEquals(2, server.takeRequest().getSequenceNumber());
//    }

// TODO(tball): b/28067294
//    public void testChunkedConnectionsArePooled() throws Exception {
//        MockResponse response = new MockResponse().setChunkedBody("ABCDEFGHIJKLMNOPQR", 5);
//
//        server.enqueue(response);
//        server.enqueue(response);
//        server.enqueue(response);
//        server.play();
//
//        assertContent("ABCDEFGHIJKLMNOPQR", server.getUrl("/foo").openConnection());
//        assertEquals(0, server.takeRequest().getSequenceNumber());
//        assertContent("ABCDEFGHIJKLMNOPQR", server.getUrl("/bar?baz=quux").openConnection());
//        assertEquals(1, server.takeRequest().getSequenceNumber());
//        assertContent("ABCDEFGHIJKLMNOPQR", server.getUrl("/z").openConnection());
//        assertEquals(2, server.takeRequest().getSequenceNumber());
//    }

    /**
     * Test that connections are added to the pool as soon as the response has
     * been consumed.
     */
// TODO(tball): b/28067294
//    public void testConnectionsArePooledWithoutExplicitDisconnect() throws Exception {
//        server.enqueue(new MockResponse().setBody("ABC"));
//        server.enqueue(new MockResponse().setBody("DEF"));
//        server.play();
//
//        URLConnection connection1 = server.getUrl("/").openConnection();
//        assertEquals("ABC", readAscii(connection1.getInputStream(), Integer.MAX_VALUE));
//        assertEquals(0, server.takeRequest().getSequenceNumber());
//        URLConnection connection2 = server.getUrl("/").openConnection();
//        assertEquals("DEF", readAscii(connection2.getInputStream(), Integer.MAX_VALUE));
//        assertEquals(1, server.takeRequest().getSequenceNumber());
//    }

    public void testServerClosesSocket() throws Exception {
        testServerClosesSocket(DISCONNECT_AT_END);
    }

    public void testServerShutdownInput() throws Exception {
        testServerClosesSocket(SHUTDOWN_INPUT_AT_END);
    }

    private void testServerClosesSocket(SocketPolicy socketPolicy) throws Exception {
        server.enqueue(new MockResponse()
                .setBody("This connection won't pool properly")
                .setSocketPolicy(socketPolicy));
        server.enqueue(new MockResponse().setBody("This comes after a busted connection"));
        server.play();

        assertContent("This connection won't pool properly", server.getUrl("/a").openConnection());
        assertEquals(0, server.takeRequest().getSequenceNumber());
        assertContent("This comes after a busted connection", server.getUrl("/b").openConnection());
        // sequence number 0 means the HTTP socket connection was not reused
        assertEquals(0, server.takeRequest().getSequenceNumber());
    }

// TODO(tball): b/28067294
//    public void testServerShutdownOutput() throws Exception {
//        // This test causes MockWebServer to log a "connection failed" stack trace
//
//        // Setting the server workerThreads to 1 ensures the responses are generated in the order
//        // the requests are accepted by the server. Without this the second and third requests made
//        // by the client (the request for "/b" and the retry for "/b" when the bad socket is
//        // detected) can be handled by the server out of order leading to test failure.
//        server.setWorkerThreads(1);
//        server.enqueue(new MockResponse()
//                .setBody("Output shutdown after this response")
//                .setSocketPolicy(SHUTDOWN_OUTPUT_AT_END));
//        server.enqueue(new MockResponse().setBody("This response will fail to write"));
//        server.enqueue(new MockResponse().setBody("This comes after a busted connection"));
//        server.play();
//
//        assertContent("Output shutdown after this response", server.getUrl("/a").openConnection());
//        assertEquals(0, server.takeRequest().getSequenceNumber());
//        assertContent("This comes after a busted connection", server.getUrl("/b").openConnection());
//        assertEquals(1, server.takeRequest().getSequenceNumber());
//        assertEquals(0, server.takeRequest().getSequenceNumber());
//    }

    enum WriteKind { BYTE_BY_BYTE, SMALL_BUFFERS, LARGE_BUFFERS }

    // j2objc: this test case is disabled because CFReadStream (which is the NSInputStream we give
    // to the NSMutableURLRequest when we make a streamed POST request) logs a message
    // "Stream [address] is sending an event before being opened", even though it does not affect
    // test correctness. This only happens when small chunks are written to the piped
    // (NS)OutputStream before the connection is fully established, and the message is never seen
    // in the next two tests. Since the log message only adds noise, we disable this test.
//    public void test_chunkedUpload_byteByByte() throws Exception {
//        doUpload(TransferKind.CHUNKED, WriteKind.BYTE_BY_BYTE);
//    }

    public void test_chunkedUpload_smallBuffers() throws Exception {
        doUpload(TransferKind.CHUNKED, WriteKind.SMALL_BUFFERS);
    }

    public void test_chunkedUpload_largeBuffers() throws Exception {
        doUpload(TransferKind.CHUNKED, WriteKind.LARGE_BUFFERS);
    }

    public void test_fixedLengthUpload_byteByByte() throws Exception {
        doUpload(TransferKind.FIXED_LENGTH, WriteKind.BYTE_BY_BYTE);
    }

    public void test_fixedLengthUpload_smallBuffers() throws Exception {
        doUpload(TransferKind.FIXED_LENGTH, WriteKind.SMALL_BUFFERS);
    }

    public void test_fixedLengthUpload_largeBuffers() throws Exception {
        doUpload(TransferKind.FIXED_LENGTH, WriteKind.LARGE_BUFFERS);
    }

    private void doUpload(TransferKind uploadKind, WriteKind writeKind) throws Exception {
        int n = 512*1024;
        server.setBodyLimit(0);
        server.enqueue(new MockResponse());
        server.play();

        HttpURLConnection conn = (HttpURLConnection) server.getUrl("/").openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        // j2objc: URLConnection does not time out by default (i.e. getReadTimeout() returns 0).
        // When we create the native NSMutableURLRequest, we set its timeoutInterval to
        // JavaLangDouble_MAX_VALUE to make it never time out. This is problematic if the request
        // uses chunked transfer encoding, as it causes the NSURLSessionDataTask to send malformed
        // chunks and never terminate the request. A definite timeout is therefore needed here.
        conn.setReadTimeout(1000);

        if (uploadKind == TransferKind.CHUNKED) {
            conn.setChunkedStreamingMode(-1);
        } else {
            conn.setFixedLengthStreamingMode(n);
        }
        OutputStream out = conn.getOutputStream();
        if (writeKind == WriteKind.BYTE_BY_BYTE) {
            for (int i = 0; i < n; ++i) {
                out.write('x');
            }
        } else {
            byte[] buf = new byte[writeKind == WriteKind.SMALL_BUFFERS ? 256 : 64*1024];
            Arrays.fill(buf, (byte) 'x');
            for (int i = 0; i < n; i += buf.length) {
                out.write(buf, 0, Math.min(buf.length, n - i));
            }
        }
        out.close();
        assertEquals(200, conn.getResponseCode());
        RecordedRequest request = server.takeRequest();
        assertEquals(n, request.getBodySize());
        if (uploadKind == TransferKind.CHUNKED) {
            assertTrue(request.getChunkSizes().size() > 0);
        } else {
            assertTrue(request.getChunkSizes().isEmpty());
        }
    }

//  JVM failure.
//    public void testGetResponseCodeNoResponseBody() throws Exception {
//        server.enqueue(new MockResponse()
//                .addHeader("abc: def"));
//        server.play();
//
//        URL url = server.getUrl("/");
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setDoInput(false);
//        assertEquals("def", conn.getHeaderField("abc"));
//        assertEquals(200, conn.getResponseCode());
//        try {
//            conn.getInputStream();
//            fail();
//        } catch (ProtocolException expected) {
//        }
//    }

// TODO(user): b/65289980.
//    public void testConnectViaHttps() throws IOException, InterruptedException {
//        TestSSLContext testSSLContext = TestSSLContext.create();
//
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), false);
//        server.enqueue(new MockResponse().setBody("this response comes via HTTPS"));
//        server.play();
//
//        HttpsURLConnection connection = (HttpsURLConnection) server.getUrl("/foo").openConnection();
//        connection.setSSLSocketFactory(testSSLContext.clientContext.getSocketFactory());
//
//        assertContent("this response comes via HTTPS", connection);
//
//        RecordedRequest request = server.takeRequest();
//        assertEquals("GET /foo HTTP/1.1", request.getRequestLine());
//        assertEquals("TLSv1.2", request.getSslProtocol());
//    }
//
//    public void testConnectViaHttpsReusingConnections() throws IOException, InterruptedException {
//        TestSSLContext testSSLContext = TestSSLContext.create();
//        SSLSocketFactory clientSocketFactory = testSSLContext.clientContext.getSocketFactory();
//
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), false);
//        server.enqueue(new MockResponse().setBody("this response comes via HTTPS"));
//        server.enqueue(new MockResponse().setBody("another response via HTTPS"));
//        server.play();
//
//        HttpsURLConnection connection = (HttpsURLConnection) server.getUrl("/").openConnection();
//        connection.setSSLSocketFactory(clientSocketFactory);
//        assertContent("this response comes via HTTPS", connection);
//
//        connection = (HttpsURLConnection) server.getUrl("/").openConnection();
//        connection.setSSLSocketFactory(clientSocketFactory);
//        assertContent("another response via HTTPS", connection);
//
//        assertEquals(0, server.takeRequest().getSequenceNumber());
//        assertEquals(1, server.takeRequest().getSequenceNumber());
//    }
//
//    public void testConnectViaHttpsReusingConnectionsDifferentFactories()
//            throws IOException, InterruptedException {
//        TestSSLContext testSSLContext = TestSSLContext.create();
//
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), false);
//        server.enqueue(new MockResponse().setBody("this response comes via HTTPS"));
//        server.enqueue(new MockResponse().setBody("another response via HTTPS"));
//        server.play();
//
//        // install a custom SSL socket factory so the server can be authorized
//        HttpsURLConnection connection = (HttpsURLConnection) server.getUrl("/").openConnection();
//        connection.setSSLSocketFactory(testSSLContext.clientContext.getSocketFactory());
//        assertContent("this response comes via HTTPS", connection);
//
//        connection = (HttpsURLConnection) server.getUrl("/").openConnection();
//        try {
//            readAscii(connection.getInputStream(), Integer.MAX_VALUE);
//            fail("without an SSL socket factory, the connection should fail");
//        } catch (SSLException expected) {
//        }
//    }
//
//    //
//    // Verify that we don't retry connections on certificate verification errors.
//    //
//    // http://code.google.com/p/android/issues/detail?id=13178
//    public void testConnectViaHttpsToUntrustedServer() throws IOException, InterruptedException {
//        TestSSLContext testSSLContext = TestSSLContext.create(TestKeyStore.getClientCA2(),
//                                                              TestKeyStore.getServer());
//
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), false);
//        server.enqueue(new MockResponse()); // unused
//        server.play();
//
//        HttpsURLConnection connection = (HttpsURLConnection) server.getUrl("/foo").openConnection();
//        connection.setSSLSocketFactory(testSSLContext.clientContext.getSocketFactory());
//        try {
//            connection.getInputStream();
//            fail();
//        } catch (SSLHandshakeException expected) {
//            assertTrue(expected.getCause() instanceof CertificateException);
//        }
//        assertEquals(0, server.getRequestCount());
//    }

// TODO(tball): b/28067294
//    public void testConnectViaProxyUsingProxyArg() throws Exception {
//        testConnectViaProxy(ProxyConfig.CREATE_ARG, "http://android.com/foo", "android.com");
//    }

// TODO(tball): b/28067294
//    public void testConnectViaProxyUsingProxySystemProperty() throws Exception {
//        testConnectViaProxy(
//                ProxyConfig.PROXY_SYSTEM_PROPERTY, "http://android.com/foo", "android.com");
//    }

//    public void testConnectViaProxyUsingHttpProxySystemProperty() throws Exception {
//        testConnectViaProxy(
//                ProxyConfig.HTTP_PROXY_SYSTEM_PROPERTY, "http://android.com/foo", "android.com");
//    }

    // Regression test for http://b/29983827 : ensure that a trailing "/" is not added to the
    // HTTP request line when using a proxy.
//    public void testConnectViaProxy_emptyPath() throws Exception {
//        testConnectViaProxy(
//                ProxyConfig.HTTP_PROXY_SYSTEM_PROPERTY, "http://android.com", "android.com");
//    }

//    public void testConnectViaProxy_rootPath() throws Exception {
//        testConnectViaProxy(
//                ProxyConfig.HTTP_PROXY_SYSTEM_PROPERTY, "http://android.com/", "android.com");
//    }

//    public void testConnectViaProxy_pathWithoutTrailingSlash() throws Exception {
//        testConnectViaProxy(
//                ProxyConfig.HTTP_PROXY_SYSTEM_PROPERTY, "http://android.com/foo", "android.com");
//    }

//    public void testConnectViaProxy_pathWithTrailingSlash() throws Exception {
//        testConnectViaProxy(
//                ProxyConfig.HTTP_PROXY_SYSTEM_PROPERTY, "http://android.com/foo/", "android.com");
//    }
//
//    public void testConnectViaProxy_complexUrlWithNoPath() throws Exception {
//        testConnectViaProxy(ProxyConfig.HTTP_PROXY_SYSTEM_PROPERTY,
//                "http://android.com:8080?height=100&width=42", "android.com:8080");
//    }

    private void testConnectViaProxy(ProxyConfig proxyConfig, String urlString, String expectedHost)
            throws Exception {
        MockResponse mockResponse = new MockResponse().setBody("this response comes via a proxy");
        server.enqueue(mockResponse);
        server.play();

        URL url = new URL(urlString);
        HttpURLConnection connection = proxyConfig.connect(server, url);
        assertContent("this response comes via a proxy", connection);

        RecordedRequest request = server.takeRequest();
        assertEquals("GET " + urlString + " HTTP/1.1", request.getRequestLine());
        assertContains(request.getHeaders(), "Host: " + expectedHost);
    }

    public void testContentDisagreesWithContentLengthHeader() throws IOException {
        server.enqueue(new MockResponse()
                .setBody("abc\r\nYOU SHOULD NOT SEE THIS")
                .clearHeaders()
                .addHeader("Content-Length: 3"));
        server.play();

        assertContent("abc", server.getUrl("/").openConnection());
    }

    public void testContentDisagreesWithChunkedHeader() throws IOException {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setChunkedBody("abc", 3);
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        bytesOut.write(mockResponse.getBody());
        bytesOut.write("\r\nYOU SHOULD NOT SEE THIS".getBytes());
        mockResponse.setBody(bytesOut.toByteArray());
        mockResponse.clearHeaders();
        mockResponse.addHeader("Transfer-encoding: chunked");

        server.enqueue(mockResponse);
        server.play();

        assertContent("abc", server.getUrl("/").openConnection());
    }

// TODO(user): b/65289980.
//    public void testConnectViaHttpProxyToHttpsUsingProxyArgWithNoProxy() throws Exception {
//        testConnectViaDirectProxyToHttps(ProxyConfig.NO_PROXY);
//    }
//
//    public void testConnectViaHttpProxyToHttpsUsingHttpProxySystemProperty() throws Exception {
//        // https should not use http proxy
//        testConnectViaDirectProxyToHttps(ProxyConfig.HTTP_PROXY_SYSTEM_PROPERTY);
//    }
//
//    private void testConnectViaDirectProxyToHttps(ProxyConfig proxyConfig) throws Exception {
//        TestSSLContext testSSLContext = TestSSLContext.create();
//
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), false);
//        server.enqueue(new MockResponse().setBody("this response comes via HTTPS"));
//        server.play();
//
//        URL url = server.getUrl("/foo");
//        HttpsURLConnection connection = (HttpsURLConnection) proxyConfig.connect(server, url);
//        connection.setSSLSocketFactory(testSSLContext.clientContext.getSocketFactory());
//
//        assertContent("this response comes via HTTPS", connection);
//
//        RecordedRequest request = server.takeRequest();
//        assertEquals("GET /foo HTTP/1.1", request.getRequestLine());
//    }

//    public void testConnectViaHttpProxyToHttpsUsingProxyArg() throws Exception {
//        testConnectViaHttpProxyToHttps(ProxyConfig.CREATE_ARG);
//    }
//
//    /**
//     * We weren't honoring all of the appropriate proxy system properties when
//     * connecting via HTTPS. http://b/3097518
//     */
//    public void testConnectViaHttpProxyToHttpsUsingProxySystemProperty() throws Exception {
//        testConnectViaHttpProxyToHttps(ProxyConfig.PROXY_SYSTEM_PROPERTY);
//    }
//
//    public void testConnectViaHttpProxyToHttpsUsingHttpsProxySystemProperty() throws Exception {
//        testConnectViaHttpProxyToHttps(ProxyConfig.HTTPS_PROXY_SYSTEM_PROPERTY);
//    }

//    /**
//     * We were verifying the wrong hostname when connecting to an HTTPS site
//     * through a proxy. http://b/3097277
//     */
//    private void testConnectViaHttpProxyToHttps(ProxyConfig proxyConfig) throws Exception {
//        TestSSLContext testSSLContext = TestSSLContext.create();
//        RecordingHostnameVerifier hostnameVerifier = new RecordingHostnameVerifier();
//
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), true);
//        server.enqueue(new MockResponse()
//                .setSocketPolicy(SocketPolicy.UPGRADE_TO_SSL_AT_END)
//                .clearHeaders());
//        server.enqueue(new MockResponse().setBody("this response comes via a secure proxy"));
//        server.play();
//
//        URL url = new URL("https://android.com/foo");
//        HttpsURLConnection connection = (HttpsURLConnection) proxyConfig.connect(server, url);
//        connection.setSSLSocketFactory(testSSLContext.clientContext.getSocketFactory());
//        connection.setHostnameVerifier(hostnameVerifier);
//
//        assertContent("this response comes via a secure proxy", connection);
//
//        RecordedRequest connect = server.takeRequest();
//        assertEquals("Connect line failure on proxy",
//                "CONNECT android.com:443 HTTP/1.1", connect.getRequestLine());
//        assertContains(connect.getHeaders(), "Host: android.com");
//
//        RecordedRequest get = server.takeRequest();
//        assertEquals("GET /foo HTTP/1.1", get.getRequestLine());
//        assertContains(get.getHeaders(), "Host: android.com");
//        assertEquals(Arrays.asList("verify android.com"), hostnameVerifier.calls);
//    }


// TODO(user): b/65289980.
//    /**
//     * Tolerate bad https proxy response when using HttpResponseCache. http://b/6754912
//     */
//    public void testConnectViaHttpProxyToHttpsUsingBadProxyAndHttpResponseCache() throws Exception {
//        TestSSLContext testSSLContext = TestSSLContext.create();
//
//        initResponseCache();
//
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), true);
//
//        // The inclusion of a body in the response to the CONNECT is key to reproducing b/6754912.
//        MockResponse badProxyResponse = new MockResponse()
//                .setSocketPolicy(SocketPolicy.UPGRADE_TO_SSL_AT_END)
//                .clearHeaders()
//                .setBody("bogus proxy connect response content");
//
//        server.enqueue(badProxyResponse);
//        server.enqueue(new MockResponse().setBody("response"));
//
//        server.play();
//
//        URL url = new URL("https://android.com/foo");
//        ProxyConfig proxyConfig = ProxyConfig.PROXY_SYSTEM_PROPERTY;
//        HttpsURLConnection connection = (HttpsURLConnection) proxyConfig.connect(server, url);
//        connection.setSSLSocketFactory(testSSLContext.clientContext.getSocketFactory());
//        connection.setHostnameVerifier(new RecordingHostnameVerifier());
//        assertContent("response", connection);
//
//        RecordedRequest connect = server.takeRequest();
//        assertEquals("CONNECT android.com:443 HTTP/1.1", connect.getRequestLine());
//        assertContains(connect.getHeaders(), "Host: android.com");
//    }
//
//    private void initResponseCache() throws IOException {
//        String tmp = System.getProperty("java.io.tmpdir");
//        File cacheDir = new File(tmp, "HttpCache-" + UUID.randomUUID());
//        cache = AndroidShimResponseCache.create(cacheDir, Integer.MAX_VALUE);
//        ResponseCache.setDefault(cache);
//    }

    /**
     * Test Etag headers are returned correctly when a client-side cache is not installed.
     * https://code.google.com/p/android/issues/detail?id=108949
     */
// TODO(tball): b/28067294
//    public void testEtagHeaders_uncached() throws Exception {
//        final String etagValue1 = "686897696a7c876b7e";
//        final String body1 = "Response with etag 1";
//        final String etagValue2 = "686897696a7c876b7f";
//        final String body2 = "Response with etag 2";
//
//        server.enqueue(
//            new MockResponse()
//                .setBody(body1)
//                .setHeader("Content-Type", "text/plain")
//                .setHeader("Etag", etagValue1));
//        server.enqueue(
//            new MockResponse()
//                .setBody(body2)
//                .setHeader("Content-Type", "text/plain")
//                .setHeader("Etag", etagValue2));
//        server.play();
//
//        URL url = server.getUrl("/");
//        HttpURLConnection connection1 = (HttpURLConnection) url.openConnection();
//        assertEquals(etagValue1, connection1.getHeaderField("Etag"));
//        assertContent(body1, connection1);
//        connection1.disconnect();
//
//        // Discard the server-side record of the request made.
//        server.takeRequest();
//
//        HttpURLConnection connection2 = (HttpURLConnection) url.openConnection();
//        assertEquals(etagValue2, connection2.getHeaderField("Etag"));
//        assertContent(body2, connection2);
//        connection2.disconnect();
//
//        // Check the client did not cache.
//        RecordedRequest request = server.takeRequest();
//        assertNull(request.getHeader("If-None-Match"));
//    }

    /**
     * Test Etag headers are returned correctly when a client-side cache is installed and the server
     * data is unchanged.
     * https://code.google.com/p/android/issues/detail?id=108949
     */
// TODO(user): b/65289980.
//    public void testEtagHeaders_cachedWithServerHit() throws Exception {
//        final String etagValue = "686897696a7c876b7e";
//        final String body = "Response with etag";
//
//        server.enqueue(
//            new MockResponse()
//                .setBody(body)
//                .setResponseCode(HttpURLConnection.HTTP_OK)
//                .setHeader("Content-Type", "text/plain")
//                .setHeader("Etag", etagValue));
//
//        server.enqueue(
//            new MockResponse()
//                .setResponseCode(HttpURLConnection.HTTP_NOT_MODIFIED));
//        server.play();
//
//        initResponseCache();
//
//        URL url = server.getUrl("/");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        assertEquals(etagValue, connection.getHeaderField("Etag"));
//        assertContent(body, connection);
//        connection.disconnect();
//
//        // Discard the server-side record of the request made.
//        server.takeRequest();
//
//        // Confirm the cached body is returned along with the original etag header.
//        HttpURLConnection cachedConnection = (HttpURLConnection) url.openConnection();
//        assertEquals(etagValue, cachedConnection.getHeaderField("Etag"));
//        assertContent(body, cachedConnection);
//        cachedConnection.disconnect();
//
//        // Check the client formatted the request correctly.
//        RecordedRequest request = server.takeRequest();
//        assertEquals(etagValue, request.getHeader("If-None-Match"));
//    }

    /*
     * Test Etag headers are returned correctly when a client-side cache is installed and the server
     * data has changed.
     * https://code.google.com/p/android/issues/detail?id=108949
     */
//    public void testEtagHeaders_cachedWithServerMiss() throws Exception {
//        final String etagValue1 = "686897696a7c876b7e";
//        final String body1 = "Response with etag 1";
//        final String etagValue2 = "686897696a7c876b7f";
//        final String body2 = "Response with etag 2";
//
//        server.enqueue(
//            new MockResponse()
//                .setBody(body1)
//                .setResponseCode(HttpURLConnection.HTTP_OK)
//                .setHeader("Content-Type", "text/plain")
//                .setHeader("Etag", etagValue1));
//
//        server.enqueue(
//            new MockResponse()
//                .setBody(body2)
//                .setResponseCode(HttpURLConnection.HTTP_OK)
//                .setHeader("Content-Type", "text/plain")
//                .setHeader("Etag", etagValue2));
//
//        server.play();
//
//        initResponseCache();
//
//        URL url = server.getUrl("/");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        assertEquals(etagValue1, connection.getHeaderField("Etag"));
//        assertContent(body1, connection);
//        connection.disconnect();
//
//        // Discard the server-side record of the request made.
//        server.takeRequest();
//
//        // Confirm the new body is returned along with the new etag header.
//        HttpURLConnection cachedConnection = (HttpURLConnection) url.openConnection();
//        assertEquals(etagValue2, cachedConnection.getHeaderField("Etag"));
//        assertContent(body2, cachedConnection);
//        cachedConnection.disconnect();
//
//        // Check the client formatted the request correctly.
//        RecordedRequest request = server.takeRequest();
//        assertEquals(etagValue1, request.getHeader("If-None-Match"));
//    }

//    /**
//     * Test which headers are sent unencrypted to the HTTP proxy.
//     */
//    public void testProxyConnectIncludesProxyHeadersOnly()
//            throws IOException, InterruptedException {
//        RecordingHostnameVerifier hostnameVerifier = new RecordingHostnameVerifier();
//        TestSSLContext testSSLContext = TestSSLContext.create();
//
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), true);
//        server.enqueue(new MockResponse()
//                .setSocketPolicy(SocketPolicy.UPGRADE_TO_SSL_AT_END)
//                .clearHeaders());
//        server.enqueue(new MockResponse().setBody("encrypted response from the origin server"));
//        server.play();
//
//        URL url = new URL("https://android.com/foo");
//        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(
//                server.toProxyAddress());
//        connection.addRequestProperty("Private", "Secret");
//        connection.addRequestProperty("Proxy-Authorization", "bar");
//        connection.addRequestProperty("User-Agent", "baz");
//        connection.setSSLSocketFactory(testSSLContext.clientContext.getSocketFactory());
//        connection.setHostnameVerifier(hostnameVerifier);
//        assertContent("encrypted response from the origin server", connection);
//
//        RecordedRequest connect = server.takeRequest();
//        assertContainsNoneMatching(connect.getHeaders(), "Private.*");
//        assertContains(connect.getHeaders(), "Proxy-Authorization: bar");
//        assertContains(connect.getHeaders(), "User-Agent: baz");
//        assertContains(connect.getHeaders(), "Host: android.com");
//        assertContains(connect.getHeaders(), "Proxy-Connection: Keep-Alive");
//
//        RecordedRequest get = server.takeRequest();
//        assertContains(get.getHeaders(), "Private: Secret");
//        assertEquals(Arrays.asList("verify android.com"), hostnameVerifier.calls);
//    }
//
//    public void testProxyAuthenticateOnConnect() throws Exception {
//        Authenticator.setDefault(new SimpleAuthenticator());
//        TestSSLContext testSSLContext = TestSSLContext.create();
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), true);
//        server.enqueue(new MockResponse()
//                .setResponseCode(407)
//                .addHeader("Proxy-Authenticate: Basic realm=\"localhost\""));
//        server.enqueue(new MockResponse()
//                .setSocketPolicy(SocketPolicy.UPGRADE_TO_SSL_AT_END)
//                .clearHeaders());
//        server.enqueue(new MockResponse().setBody("A"));
//        server.play();
//
//        URL url = new URL("https://android.com/foo");
//        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(
//                server.toProxyAddress());
//        connection.setSSLSocketFactory(testSSLContext.clientContext.getSocketFactory());
//        connection.setHostnameVerifier(new RecordingHostnameVerifier());
//        assertContent("A", connection);
//
//        RecordedRequest connect1 = server.takeRequest();
//        assertEquals("CONNECT android.com:443 HTTP/1.1", connect1.getRequestLine());
//        assertContainsNoneMatching(connect1.getHeaders(), "Proxy\\-Authorization.*");
//
//        RecordedRequest connect2 = server.takeRequest();
//        assertEquals("CONNECT android.com:443 HTTP/1.1", connect2.getRequestLine());
//        assertContains(connect2.getHeaders(), "Proxy-Authorization: Basic "
//                + SimpleAuthenticator.BASE_64_CREDENTIALS);
//
//        RecordedRequest get = server.takeRequest();
//        assertEquals("GET /foo HTTP/1.1", get.getRequestLine());
//        assertContainsNoneMatching(get.getHeaders(), "Proxy\\-Authorization.*");
//    }
//
//    // Don't disconnect after building a tunnel with CONNECT
//    // http://code.google.com/p/android/issues/detail?id=37221
//    public void testProxyWithConnectionClose() throws IOException {
//        TestSSLContext testSSLContext = TestSSLContext.create();
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), true);
//        server.enqueue(new MockResponse()
//                .setSocketPolicy(SocketPolicy.UPGRADE_TO_SSL_AT_END)
//                .clearHeaders());
//        server.enqueue(new MockResponse().setBody("this response comes via a proxy"));
//        server.play();
//
//        URL url = new URL("https://android.com/foo");
//        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection(
//                server.toProxyAddress());
//        connection.setRequestProperty("Connection", "close");
//        connection.setSSLSocketFactory(testSSLContext.clientContext.getSocketFactory());
//        connection.setHostnameVerifier(new RecordingHostnameVerifier());
//
//        assertContent("this response comes via a proxy", connection);
//    }

// TODO(tball): b/28067294
//    public void testDisconnectedConnection() throws IOException {
//        server.enqueue(new MockResponse()
//                .throttleBody(2, 100, TimeUnit.MILLISECONDS)
//                .setBody("ABCD"));
//        server.play();
//
//        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
//        InputStream in = connection.getInputStream();
//        assertEquals('A', (char) in.read());
//        connection.disconnect();
//        try {
//            // Reading 'B' may succeed if it's buffered.
//            in.read();
//            // But 'C' shouldn't be buffered (the response is throttled) and this should fail.
//            in.read();
//            fail("Expected a connection closed exception");
//        } catch (IOException expected) {
//        }
//    }

    public void testDisconnectBeforeConnect() throws IOException {
        server.enqueue(new MockResponse().setBody("A"));
        server.play();

        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        connection.disconnect();

        assertContent("A", connection);
        assertEquals(200, connection.getResponseCode());
    }

    public void testDisconnectAfterOnlyResponseCodeCausesNoCloseGuardWarning() throws IOException {
        server.enqueue(new MockResponse()
                .setBody(gzip("ABCABCABC".getBytes("UTF-8")))
                .addHeader("Content-Encoding: gzip"));
        server.play();

        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        try {
            assertEquals(200, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }

    public void testDefaultRequestProperty() throws Exception {
        URLConnection.setDefaultRequestProperty("X-testSetDefaultRequestProperty", "A");
        assertNull(URLConnection.getDefaultRequestProperty("X-setDefaultRequestProperty"));
    }

    /**
     * Reads {@code count} characters from the stream. If the stream is
     * exhausted before {@code count} characters can be read, the remaining
     * characters are returned and the stream is closed.
     */
    private String readAscii(InputStream in, int count) throws IOException {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            int value = in.read();
            if (value == -1) {
                in.close();
                break;
            }
            result.append((char) value);
        }
        return result.toString();
    }

    public void testMarkAndResetWithContentLengthHeader() throws IOException {
        testMarkAndReset(TransferKind.FIXED_LENGTH);
    }

    public void testMarkAndResetWithChunkedEncoding() throws IOException {
        testMarkAndReset(TransferKind.CHUNKED);
    }

//  JVM failure.
//    public void testMarkAndResetWithNoLengthHeaders() throws IOException {
//        testMarkAndReset(TransferKind.END_OF_STREAM);
//    }

    private void testMarkAndReset(TransferKind transferKind) throws IOException {
        MockResponse response = new MockResponse();
        transferKind.setBody(response, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", 1024);
        server.enqueue(response);
        server.enqueue(response);
        server.play();

        InputStream in = server.getUrl("/").openConnection().getInputStream();
        assertFalse("This implementation claims to support mark().", in.markSupported());
        in.mark(5);
        assertEquals("ABCDE", readAscii(in, 5));
        try {
            in.reset();
            fail();
        } catch (IOException expected) {
        }
        assertEquals("FGHIJKLMNOPQRSTUVWXYZ", readAscii(in, Integer.MAX_VALUE));
        assertContent("ABCDEFGHIJKLMNOPQRSTUVWXYZ", server.getUrl("/").openConnection());
    }

    /**
     * We've had a bug where we forget the HTTP response when we see response
     * code 401. This causes a new HTTP request to be issued for every call into
     * the URLConnection.
     */
// TODO(tball): b/28067294
//    public void testUnauthorizedResponseHandling() throws IOException {
//        MockResponse response = new MockResponse()
//                .addHeader("WWW-Authenticate: Basic realm=\"protected area\"")
//                .setResponseCode(401) // UNAUTHORIZED
//                .setBody("Unauthorized");
//        server.enqueue(response);
//        server.enqueue(response);
//        server.enqueue(response);
//        server.play();
//
//        URL url = server.getUrl("/");
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//        assertEquals(401, conn.getResponseCode());
//        assertEquals(401, conn.getResponseCode());
//        assertEquals(401, conn.getResponseCode());
//        assertEquals(1, server.getRequestCount());
//    }

    public void testNonHexChunkSize() throws IOException {
        server.enqueue(new MockResponse()
                .setBody("5\r\nABCDE\r\nG\r\nFGHIJKLMNOPQRSTU\r\n0\r\n\r\n")
                .clearHeaders()
                .addHeader("Transfer-encoding: chunked"));
        server.play();

        URLConnection connection = server.getUrl("/").openConnection();

        // j2objc: URLConnection does not time out by default (i.e. getReadTimeout() returns 0).
        // When we create the native NSMutableURLRequest, we set its timeoutInterval to
        // JavaLangDouble_MAX_VALUE to make it never time out. This is problematic if the response
        // uses chunked transfer encoding, as the NSURLSessionDataTask is unable to make progress
        // and the read will never finish. A definite timeout is therefore needed here.
        connection.setReadTimeout(1000);

        try {
            readAscii(connection.getInputStream(), Integer.MAX_VALUE);
            fail();
        } catch (IOException e) {
        }
    }

    public void testMissingChunkBody() throws IOException {
        server.enqueue(new MockResponse()
                .setBody("5")
                .clearHeaders()
                .addHeader("Transfer-encoding: chunked")
                .setSocketPolicy(DISCONNECT_AT_END));
        server.play();

        URLConnection connection = server.getUrl("/").openConnection();

        // j2objc: See testNonHexChunkSize() above for why the timeout is needed.
        connection.setReadTimeout(1000);

        try {
            readAscii(connection.getInputStream(), Integer.MAX_VALUE);
            fail();
        } catch (IOException e) {
        }
    }

// JVM failure.
    /**
     * This test checks whether connections are gzipped by default. This
     * behavior in not required by the API, so a failure of this test does not
     * imply a bug in the implementation.
     */
//    public void testGzipEncodingEnabledByDefault() throws IOException, InterruptedException {
//        server.enqueue(new MockResponse()
//                .setBody(gzip("ABCABCABC".getBytes("UTF-8")))
//                .addHeader("Content-Encoding: gzip"));
//        server.play();
//
//        URLConnection connection = server.getUrl("/").openConnection();
//        assertEquals("ABCABCABC", readAscii(connection.getInputStream(), Integer.MAX_VALUE));
//        assertNull(connection.getContentEncoding());
//        assertEquals(-1, connection.getContentLength());
//
//        RecordedRequest request = server.takeRequest();
//        assertContains(request.getHeaders(), "Accept-Encoding: gzip");
//    }

// TODO(tball): b/28067294
//    public void testClientConfiguredGzipContentEncoding() throws Exception {
//        byte[] bodyBytes = gzip("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes("UTF-8"));
//        server.enqueue(new MockResponse()
//                .setBody(bodyBytes)
//                .addHeader("Content-Encoding: gzip")
//                .addHeader("Content-Length: " + bodyBytes.length));
//        server.play();
//
//        URLConnection connection = server.getUrl("/").openConnection();
//        connection.addRequestProperty("Accept-Encoding", "gzip");
//        InputStream gunzippedIn = new GZIPInputStream(connection.getInputStream());
//        assertEquals("ABCDEFGHIJKLMNOPQRSTUVWXYZ", readAscii(gunzippedIn, Integer.MAX_VALUE));
//        assertEquals(bodyBytes.length, connection.getContentLength());
//
//        RecordedRequest request = server.takeRequest();
//        assertContains(request.getHeaders(), "Accept-Encoding: gzip");
//        assertEquals("gzip", connection.getContentEncoding());
//    }

// TODO(tball): b/28067294
//    public void testGzipAndConnectionReuseWithFixedLength() throws Exception {
//        testClientConfiguredGzipContentEncodingAndConnectionReuse(TransferKind.FIXED_LENGTH);
//    }

// TODO(tball): b/28067294
//    public void testGzipAndConnectionReuseWithChunkedEncoding() throws Exception {
//        testClientConfiguredGzipContentEncodingAndConnectionReuse(TransferKind.CHUNKED);
//    }

    public void testClientConfiguredCustomContentEncoding() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("ABCDE")
                .addHeader("Content-Encoding: custom"));
        server.play();

        URLConnection connection = server.getUrl("/").openConnection();
        connection.addRequestProperty("Accept-Encoding", "custom");
        assertEquals("ABCDE", readAscii(connection.getInputStream(), Integer.MAX_VALUE));

        RecordedRequest request = server.takeRequest();
        assertContains(request.getHeaders(), "Accept-Encoding: custom");
    }

    /**
     * Test a bug where gzip input streams weren't exhausting the input stream,
     * which corrupted the request that followed.
     * http://code.google.com/p/android/issues/detail?id=7059
     */
    private void testClientConfiguredGzipContentEncodingAndConnectionReuse(
            TransferKind transferKind) throws Exception {
        MockResponse responseOne = new MockResponse();
        responseOne.addHeader("Content-Encoding: gzip");
        transferKind.setBody(responseOne, gzip("one (gzipped)".getBytes("UTF-8")), 5);
        server.enqueue(responseOne);
        MockResponse responseTwo = new MockResponse();
        transferKind.setBody(responseTwo, "two (identity)", 5);
        server.enqueue(responseTwo);
        server.play();

        URLConnection connection = server.getUrl("/").openConnection();
        connection.addRequestProperty("Accept-Encoding", "gzip");
        InputStream gunzippedIn = new GZIPInputStream(connection.getInputStream());
        assertEquals("one (gzipped)", readAscii(gunzippedIn, Integer.MAX_VALUE));
        assertEquals(0, server.takeRequest().getSequenceNumber());

        connection = server.getUrl("/").openConnection();
        assertEquals("two (identity)", readAscii(connection.getInputStream(), Integer.MAX_VALUE));
        assertEquals(1, server.takeRequest().getSequenceNumber());
    }

//  JVM failure.
    /**
     * Test that HEAD requests don't have a body regardless of the response
     * headers. http://code.google.com/p/android/issues/detail?id=24672
     */
//    public void testHeadAndContentLength() throws Exception {
//        server.enqueue(new MockResponse()
//                .clearHeaders()
//                .addHeader("Content-Length: 100"));
//        server.enqueue(new MockResponse().setBody("A"));
//        server.play();
//
//        HttpURLConnection connection1 = (HttpURLConnection) server.getUrl("/").openConnection();
//        connection1.setRequestMethod("HEAD");
//        assertEquals("100", connection1.getHeaderField("Content-Length"));
//        assertContent("", connection1);
//
//        HttpURLConnection connection2 = (HttpURLConnection) server.getUrl("/").openConnection();
//        assertEquals("A", readAscii(connection2.getInputStream(), Integer.MAX_VALUE));
//
//        assertEquals(0, server.takeRequest().getSequenceNumber());
//        assertEquals(1, server.takeRequest().getSequenceNumber());
//    }

// TODO(tball): b/28067294
    /**
     * Test that request body chunking works. This test has been relaxed from treating
     * the {@link java.net.HttpURLConnection#setChunkedStreamingMode(int)}
     * chunk length as being fixed because OkHttp no longer guarantees
     * the fixed chunk size. Instead, we check that chunking takes place
     * and we force the chunk size with flushes.
     */
//    public void testSetChunkedStreamingMode() throws IOException, InterruptedException {
//        server.enqueue(new MockResponse());
//        server.play();
//
//        HttpURLConnection urlConnection = (HttpURLConnection) server.getUrl("/").openConnection();
//        // Later releases of Android ignore the value for chunkLength if it is > 0 and default to
//        // a fixed chunkLength. During the change-over period while the chunkLength indicates the
//        // chunk buffer size (inc. header) the chunkLength has to be >= 8. This enables the flush()
//        // to dictate the size of the chunks.
//        urlConnection.setChunkedStreamingMode(50 /* chunkLength */);
//        urlConnection.setDoOutput(true);
//        OutputStream outputStream = urlConnection.getOutputStream();
//        String outputString = "ABCDEFGH";
//        byte[] outputBytes = outputString.getBytes("US-ASCII");
//        int targetChunkSize = 3;
//        for (int i = 0; i < outputBytes.length; i += targetChunkSize) {
//            int count = i + targetChunkSize < outputBytes.length ? 3 : outputBytes.length - i;
//            outputStream.write(outputBytes, i, count);
//            outputStream.flush();
//        }
//        assertEquals(200, urlConnection.getResponseCode());
//
//        RecordedRequest request = server.takeRequest();
//        assertEquals(outputString, new String(request.getBody(), "US-ASCII"));
//        assertEquals(Arrays.asList(3, 3, 2), request.getChunkSizes());
//    }

// TODO(tball): b/28067294
//    public void testAuthenticateWithFixedLengthStreaming() throws Exception {
//        testAuthenticateWithStreamingPost(StreamingMode.FIXED_LENGTH);
//    }

// TODO(tball): b/28067294
//    public void testAuthenticateWithChunkedStreaming() throws Exception {
//        testAuthenticateWithStreamingPost(StreamingMode.CHUNKED);
//    }

    private void testAuthenticateWithStreamingPost(StreamingMode streamingMode) throws Exception {
        MockResponse pleaseAuthenticate = new MockResponse()
                .setResponseCode(401)
                .addHeader("WWW-Authenticate: Basic realm=\"protected area\"")
                .setBody("Please authenticate.");
        server.enqueue(pleaseAuthenticate);
        server.play();

        Authenticator.setDefault(new SimpleAuthenticator());
        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        connection.setDoOutput(true);
        byte[] requestBody = { 'A', 'B', 'C', 'D' };
        if (streamingMode == StreamingMode.FIXED_LENGTH) {
            connection.setFixedLengthStreamingMode(requestBody.length);
        } else if (streamingMode == StreamingMode.CHUNKED) {
            connection.setChunkedStreamingMode(0);
        }
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(requestBody);
        outputStream.close();
        try {
            connection.getInputStream();
            fail();
        } catch (HttpRetryException expected) {
        }

        // no authorization header for the request...
        RecordedRequest request = server.takeRequest();
        assertContainsNoneMatching(request.getHeaders(), "Authorization: Basic .*");
        assertEquals(Arrays.toString(requestBody), Arrays.toString(request.getBody()));
    }

    public void testSetValidRequestMethod() throws Exception {
        server.play();
        assertValidRequestMethod("GET");
        assertValidRequestMethod("DELETE");
        assertValidRequestMethod("HEAD");
        assertValidRequestMethod("OPTIONS");
        assertValidRequestMethod("POST");
        assertValidRequestMethod("PUT");
        assertValidRequestMethod("TRACE");
    }

    private void assertValidRequestMethod(String requestMethod) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        connection.setRequestMethod(requestMethod);
        assertEquals(requestMethod, connection.getRequestMethod());
    }

    public void testSetInvalidRequestMethodLowercase() throws Exception {
        server.play();
        assertInvalidRequestMethod("get");
    }

    public void testSetInvalidRequestMethodConnect() throws Exception {
        server.play();
        assertInvalidRequestMethod("CONNECT");
    }

    private void assertInvalidRequestMethod(String requestMethod) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        try {
            connection.setRequestMethod(requestMethod);
            fail();
        } catch (ProtocolException expected) {
        }
    }

    public void testCannotSetNegativeFixedLengthStreamingMode() throws Exception {
        server.play();
        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        try {
            connection.setFixedLengthStreamingMode(-2);
            fail();
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testCanSetNegativeChunkedStreamingMode() throws Exception {
        server.play();
        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        connection.setChunkedStreamingMode(-2);
    }

    public void testCannotSetFixedLengthStreamingModeAfterConnect() throws Exception {
        server.enqueue(new MockResponse().setBody("A"));
        server.play();
        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        assertEquals("A", readAscii(connection.getInputStream(), Integer.MAX_VALUE));
        try {
            connection.setFixedLengthStreamingMode(1);
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    public void testCannotSetChunkedStreamingModeAfterConnect() throws Exception {
        server.enqueue(new MockResponse().setBody("A"));
        server.play();
        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        assertEquals("A", readAscii(connection.getInputStream(), Integer.MAX_VALUE));
        try {
            connection.setChunkedStreamingMode(1);
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    public void testCannotSetFixedLengthStreamingModeAfterChunkedStreamingMode() throws Exception {
        server.play();
        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        connection.setChunkedStreamingMode(1);
        try {
            connection.setFixedLengthStreamingMode(1);
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    public void testCannotSetChunkedStreamingModeAfterFixedLengthStreamingMode() throws Exception {
        server.play();
        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        connection.setFixedLengthStreamingMode(1);
        try {
            connection.setChunkedStreamingMode(1);
            fail();
        } catch (IllegalStateException expected) {
        }
    }

//    public void testSecureFixedLengthStreaming() throws Exception {
//        testSecureStreamingPost(StreamingMode.FIXED_LENGTH);
//    }
//
//    public void testSecureChunkedStreaming() throws Exception {
//        testSecureStreamingPost(StreamingMode.CHUNKED);
//    }

//    /**
//     * Users have reported problems using HTTPS with streaming request bodies.
//     * http://code.google.com/p/android/issues/detail?id=12860
//     */
//    private void testSecureStreamingPost(StreamingMode streamingMode) throws Exception {
//        TestSSLContext testSSLContext = TestSSLContext.create();
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), false);
//        server.enqueue(new MockResponse().setBody("Success!"));
//        server.play();
//
//        HttpsURLConnection connection = (HttpsURLConnection) server.getUrl("/").openConnection();
//        connection.setSSLSocketFactory(testSSLContext.clientContext.getSocketFactory());
//        connection.setDoOutput(true);
//        byte[] requestBody = { 'A', 'B', 'C', 'D' };
//        if (streamingMode == StreamingMode.FIXED_LENGTH) {
//            connection.setFixedLengthStreamingMode(requestBody.length);
//        } else if (streamingMode == StreamingMode.CHUNKED) {
//            connection.setChunkedStreamingMode(0);
//        }
//        OutputStream outputStream = connection.getOutputStream();
//        outputStream.write(requestBody);
//        outputStream.close();
//        assertEquals("Success!", readAscii(connection.getInputStream(), Integer.MAX_VALUE));
//
//        RecordedRequest request = server.takeRequest();
//        assertEquals("POST / HTTP/1.1", request.getRequestLine());
//        if (streamingMode == StreamingMode.FIXED_LENGTH) {
//            assertEquals(Collections.<Integer>emptyList(), request.getChunkSizes());
//        } else if (streamingMode == StreamingMode.CHUNKED) {
//            assertEquals(Arrays.asList(4), request.getChunkSizes());
//        }
//        assertEquals(Arrays.toString(requestBody), Arrays.toString(request.getBody()));
//    }

    enum StreamingMode {
        FIXED_LENGTH, CHUNKED
    }

// TODO(tball): b/28067294
//    public void testAuthenticateWithPost() throws Exception {
//        MockResponse pleaseAuthenticate = new MockResponse()
//                .setResponseCode(401)
//                .addHeader("WWW-Authenticate: Basic realm=\"protected area\"")
//                .setBody("Please authenticate.");
//        // fail auth three times...
//        server.enqueue(pleaseAuthenticate);
//        server.enqueue(pleaseAuthenticate);
//        server.enqueue(pleaseAuthenticate);
//        // ...then succeed the fourth time
//        server.enqueue(new MockResponse().setBody("Successful auth!"));
//        server.play();
//
//        Authenticator.setDefault(new SimpleAuthenticator());
//        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
//        connection.setDoOutput(true);
//        byte[] requestBody = { 'A', 'B', 'C', 'D' };
//        OutputStream outputStream = connection.getOutputStream();
//        outputStream.write(requestBody);
//        outputStream.close();
//        assertEquals("Successful auth!", readAscii(connection.getInputStream(), Integer.MAX_VALUE));
//
//        // no authorization header for the first request...
//        RecordedRequest request = server.takeRequest();
//        assertContainsNoneMatching(request.getHeaders(), "Authorization: .*");
//
//        // ...but the three requests that follow include an authorization header
//        for (int i = 0; i < 3; i++) {
//            request = server.takeRequest();
//            assertEquals("POST / HTTP/1.1", request.getRequestLine());
//            assertContains(request.getHeaders(), "Authorization: Basic "
//                    + SimpleAuthenticator.BASE_64_CREDENTIALS);
//            assertEquals(Arrays.toString(requestBody), Arrays.toString(request.getBody()));
//        }
//    }

//  JVM failure.
//    public void testAuthenticateWithGet() throws Exception {
//        MockResponse pleaseAuthenticate = new MockResponse()
//                .setResponseCode(401)
//                .addHeader("WWW-Authenticate: Basic realm=\"protected area\"")
//                .setBody("Please authenticate.");
//        // fail auth three times...
//        server.enqueue(pleaseAuthenticate);
//        server.enqueue(pleaseAuthenticate);
//        server.enqueue(pleaseAuthenticate);
//        // ...then succeed the fourth time
//        server.enqueue(new MockResponse().setBody("Successful auth!"));
//        server.play();
//
//        SimpleAuthenticator authenticator = new SimpleAuthenticator();
//        Authenticator.setDefault(authenticator);
//        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
//        assertEquals("Successful auth!", readAscii(connection.getInputStream(), Integer.MAX_VALUE));
//        assertEquals(Authenticator.RequestorType.SERVER, authenticator.requestorType);
//        assertEquals(server.getPort(), authenticator.requestingPort);
//        assertEquals(InetAddress.getByName(server.getHostName()), authenticator.requestingSite);
//        assertEquals("protected area", authenticator.requestingPrompt);
//        assertEquals("http", authenticator.requestingProtocol);
//        assertEquals("Basic", authenticator.requestingScheme);
//
//        // no authorization header for the first request...
//        RecordedRequest request = server.takeRequest();
//        assertContainsNoneMatching(request.getHeaders(), "Authorization: .*");
//
//        // ...but the three requests that follow requests include an authorization header
//        for (int i = 0; i < 3; i++) {
//            request = server.takeRequest();
//            assertEquals("GET / HTTP/1.1", request.getRequestLine());
//            assertContains(request.getHeaders(), "Authorization: Basic "
//                    + SimpleAuthenticator.BASE_64_CREDENTIALS);
//        }
//    }

// TODO(tball): b/28067294
    // bug 11473660
//    public void testAuthenticateWithLowerCaseHeadersAndScheme() throws Exception {
//        MockResponse pleaseAuthenticate = new MockResponse()
//                .setResponseCode(401)
//                .addHeader("www-authenticate: basic realm=\"protected area\"")
//                .setBody("Please authenticate.");
//        // fail auth three times...
//        server.enqueue(pleaseAuthenticate);
//        server.enqueue(pleaseAuthenticate);
//        server.enqueue(pleaseAuthenticate);
//        // ...then succeed the fourth time
//        server.enqueue(new MockResponse().setBody("Successful auth!"));
//        server.play();
//
//        SimpleAuthenticator authenticator = new SimpleAuthenticator();
//        Authenticator.setDefault(authenticator);
//        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
//        assertEquals("Successful auth!", readAscii(connection.getInputStream(), Integer.MAX_VALUE));
//        assertEquals(Authenticator.RequestorType.SERVER, authenticator.requestorType);
//        assertEquals(server.getPort(), authenticator.requestingPort);
//        assertEquals(InetAddress.getByName(server.getHostName()), authenticator.requestingSite);
//        assertEquals("protected area", authenticator.requestingPrompt);
//        assertEquals("http", authenticator.requestingProtocol);
//        assertEquals("basic", authenticator.requestingScheme);
//    }

//  JVM failure.
    // http://code.google.com/p/android/issues/detail?id=19081
//    public void testAuthenticateWithCommaSeparatedAuthenticationMethods() throws Exception {
//        server.enqueue(new MockResponse()
//                .setResponseCode(401)
//                .addHeader("WWW-Authenticate: Scheme1 realm=\"a\", Basic realm=\"b\", "
//                        + "Scheme3 realm=\"c\"")
//                .setBody("Please authenticate."));
//        server.enqueue(new MockResponse().setBody("Successful auth!"));
//        server.play();
//
//        SimpleAuthenticator authenticator = new SimpleAuthenticator();
//        authenticator.expectedPrompt = "b";
//        Authenticator.setDefault(authenticator);
//        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
//        assertEquals("Successful auth!", readAscii(connection.getInputStream(), Integer.MAX_VALUE));
//
//        assertContainsNoneMatching(server.takeRequest().getHeaders(), "Authorization: .*");
//        assertContains(server.takeRequest().getHeaders(),
//                "Authorization: Basic " + SimpleAuthenticator.BASE_64_CREDENTIALS);
//        assertEquals("Basic", authenticator.requestingScheme);
//    }

//  JVM failure.
//    public void testAuthenticateWithMultipleAuthenticationHeaders() throws Exception {
//        server.enqueue(new MockResponse()
//                .setResponseCode(401)
//                .addHeader("WWW-Authenticate: Scheme1 realm=\"a\"")
//                .addHeader("WWW-Authenticate: Basic realm=\"b\"")
//                .addHeader("WWW-Authenticate: Scheme3 realm=\"c\"")
//                .setBody("Please authenticate."));
//        server.enqueue(new MockResponse().setBody("Successful auth!"));
//        server.play();
//
//        SimpleAuthenticator authenticator = new SimpleAuthenticator();
//        authenticator.expectedPrompt = "b";
//        Authenticator.setDefault(authenticator);
//        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
//        assertEquals("Successful auth!", readAscii(connection.getInputStream(), Integer.MAX_VALUE));
//
//        assertContainsNoneMatching(server.takeRequest().getHeaders(), "Authorization: .*");
//        assertContains(server.takeRequest().getHeaders(),
//                "Authorization: Basic " + SimpleAuthenticator.BASE_64_CREDENTIALS);
//        assertEquals("Basic", authenticator.requestingScheme);
//    }

//  JVM failure.
//    public void testRedirectedWithChunkedEncoding() throws Exception {
//        testRedirected(TransferKind.CHUNKED, true);
//    }

//  JVM failure.
//    public void testRedirectedWithContentLengthHeader() throws Exception {
//        testRedirected(TransferKind.FIXED_LENGTH, true);
//    }

    public void testRedirectedWithNoLengthHeaders() throws Exception {
        testRedirected(TransferKind.END_OF_STREAM, false);
    }

    private void testRedirected(TransferKind transferKind, boolean reuse) throws Exception {
        MockResponse response = new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
                .addHeader("Location: /foo");
        transferKind.setBody(response, "This page has moved!", 10);
        server.enqueue(response);
        server.enqueue(new MockResponse().setBody("This is the new location!"));
        server.play();

        URLConnection connection = server.getUrl("/").openConnection();
        assertEquals("This is the new location!",
                readAscii(connection.getInputStream(), Integer.MAX_VALUE));

        RecordedRequest first = server.takeRequest();
        assertEquals("GET / HTTP/1.1", first.getRequestLine());
        RecordedRequest retry = server.takeRequest();
        assertEquals("GET /foo HTTP/1.1", retry.getRequestLine());
        if (reuse) {
            assertEquals("Expected connection reuse", 1, retry.getSequenceNumber());
        }
    }

//    public void testRedirectedOnHttps() throws IOException, InterruptedException {
//        TestSSLContext testSSLContext = TestSSLContext.create();
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), false);
//        server.enqueue(new MockResponse()
//                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
//                .addHeader("Location: /foo")
//                .setBody("This page has moved!"));
//        server.enqueue(new MockResponse().setBody("This is the new location!"));
//        server.play();
//
//        HttpsURLConnection connection = (HttpsURLConnection) server.getUrl("/").openConnection();
//        connection.setSSLSocketFactory(testSSLContext.clientContext.getSocketFactory());
//        assertEquals("This is the new location!",
//                readAscii(connection.getInputStream(), Integer.MAX_VALUE));
//
//        RecordedRequest first = server.takeRequest();
//        assertEquals("GET / HTTP/1.1", first.getRequestLine());
//        RecordedRequest retry = server.takeRequest();
//        assertEquals("GET /foo HTTP/1.1", retry.getRequestLine());
//        assertEquals("Expected connection reuse", 1, retry.getSequenceNumber());
//    }
//
//    public void testNotRedirectedFromHttpsToHttp() throws IOException, InterruptedException {
//        TestSSLContext testSSLContext = TestSSLContext.create();
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), false);
//        server.enqueue(new MockResponse()
//                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
//                .addHeader("Location: http://anyhost/foo")
//                .setBody("This page has moved!"));
//        server.play();
//
//        HttpsURLConnection connection = (HttpsURLConnection) server.getUrl("/").openConnection();
//        connection.setSSLSocketFactory(testSSLContext.clientContext.getSocketFactory());
//        assertEquals("This page has moved!",
//                readAscii(connection.getInputStream(), Integer.MAX_VALUE));
//    }

    public void testNotRedirectedFromHttpToHttps() throws IOException, InterruptedException {
        server.enqueue(new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
                .addHeader("Location: https://anyhost/foo")
                .setBody("This page has moved!"));
        server.play();

        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        assertEquals("This page has moved!",
                readAscii(connection.getInputStream(), Integer.MAX_VALUE));
    }

    public void testRedirectToAnotherOriginServer() throws Exception {
        MockWebServer server2 = new MockWebServer();
        server2.enqueue(new MockResponse().setBody("This is the 2nd server!"));
        server2.play();

        server.enqueue(new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
                .addHeader("Location: " + server2.getUrl("/").toString())
                .setBody("This page has moved!"));
        server.enqueue(new MockResponse().setBody("This is the first server again!"));
        server.play();

        URLConnection connection = server.getUrl("/").openConnection();
        assertEquals("This is the 2nd server!",
                readAscii(connection.getInputStream(), Integer.MAX_VALUE));
        assertEquals(server2.getUrl("/"), connection.getURL());

        // make sure the first server was careful to recycle the connection
        assertEquals("This is the first server again!",
                readAscii(server.getUrl("/").openStream(), Integer.MAX_VALUE));

        RecordedRequest first = server.takeRequest();
        assertContains(first.getHeaders(), "Host: " + hostName + ":" + server.getPort());
        RecordedRequest second = server2.takeRequest();
        assertContains(second.getHeaders(), "Host: " + hostName + ":" + server2.getPort());
        RecordedRequest third = server.takeRequest();
        // assertEquals("Expected connection reuse", 1, third.getSequenceNumber()); // JVM failure

        server2.shutdown();
    }

// TODO(tball): b/28067294
    // http://b/27590872 - assert we do not throw a runtime exception if a server responds with
    // a location that cannot be represented directly by URI.
//    public void testRedirectWithInvalidRedirectUrl() throws Exception {
//        // The first server hosts a redirect to a second. We need two so that the ProxySelector
//        // installed is used for the redirect. Otherwise the second request will be handled via the
//        // existing keep-alive connection.
//        server.play();
//
//        MockWebServer server2 = new MockWebServer();
//        server2.play();
//
//        String targetPath = "/target";
//        // The "%0" in the suffix is invalid without a second digit.
//        String invalidSuffix = "?foo=%0&bar=%00";
//
//        String redirectPath = server2.getUrl(targetPath).toString();
//        String invalidRedirectUri = redirectPath + invalidSuffix;
//
//        // Redirect to the invalid URI.
//        server.enqueue(new MockResponse()
//                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
//                .addHeader("Location: " + invalidRedirectUri));
//
//        server2.enqueue(new MockResponse().setBody("Target"));
//
//        // Assert the target URI is actually invalid.
//        try {
//            new URI(invalidRedirectUri);
//            fail("Target URL is expected to be invalid");
//        } catch (URISyntaxException expected) {}
//
//        // The ProxySelector requires a URI object, which forces the HttpURLConnectionImpl to create
//        // a URI object containing a string based on the redirect address, regardless of what it is
//        // using internally to hold the target address.
//        ProxySelector originalSelector = ProxySelector.getDefault();
//        final List<URI> proxySelectorUris = new ArrayList<>();
//        ProxySelector.setDefault(new ProxySelector() {
//            @Override
//            public List<Proxy> select(URI uri) {
//                if (uri.getScheme().equals("http")) {
//                    // Ignore socks proxy lookups.
//                    proxySelectorUris.add(uri);
//                }
//                return Collections.singletonList(Proxy.NO_PROXY);
//            }
//
//            @Override
//            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
//                // no-op
//            }
//        });
//
//        try {
//            HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
//            assertEquals("Target", readAscii(connection.getInputStream(), Integer.MAX_VALUE));
//
//            // Inspect the redirect request to see what request was actually made.
//            RecordedRequest actualRequest = server2.takeRequest();
//            assertEquals(targetPath + invalidSuffix, actualRequest.getPath());
//
//            // The first URI will be the initial request. We want to inspect the redirect.
//            URI uri = proxySelectorUris.get(1);
//            // The HttpURLConnectionImpl converts %0 -> %250. i.e. it escapes the %.
//            assertEquals(redirectPath + "?foo=%250&bar=%00", uri.toString());
//        } finally {
//            ProxySelector.setDefault(originalSelector);
//            server2.shutdown();
//        }
//    }

    public void testInstanceFollowsRedirects() throws Exception {
        testInstanceFollowsRedirects("http://www.google.com/");
        testInstanceFollowsRedirects("https://www.google.com/");
    }

    private void testInstanceFollowsRedirects(String spec) throws Exception {
        URL url = new URL(spec);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setInstanceFollowRedirects(true);
        assertTrue(urlConnection.getInstanceFollowRedirects());
        urlConnection.setInstanceFollowRedirects(false);
        assertFalse(urlConnection.getInstanceFollowRedirects());
    }

    public void testFollowRedirects() throws Exception {
        testFollowRedirects("http://www.google.com/");
        testFollowRedirects("https://www.google.com/");
    }

    private void testFollowRedirects(String spec) throws Exception {
        URL url = new URL(spec);
        boolean originalValue = HttpURLConnection.getFollowRedirects();
        try {
            HttpURLConnection.setFollowRedirects(false);
            assertFalse(HttpURLConnection.getFollowRedirects());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            assertFalse(connection.getInstanceFollowRedirects());

            HttpURLConnection.setFollowRedirects(true);
            assertTrue(HttpURLConnection.getFollowRedirects());

            HttpURLConnection connection2 = (HttpURLConnection) url.openConnection();
            assertTrue(connection2.getInstanceFollowRedirects());
        } finally {
            HttpURLConnection.setFollowRedirects(originalValue);
        }
    }

// TODO(tball): b/28067294
//    public void testResponse300MultipleChoiceWithPost() throws Exception {
//        // Chrome doesn't follow the redirect, but Firefox and the RI both do
//        testResponseRedirectedWithPost(HttpURLConnection.HTTP_MULT_CHOICE);
//    }

    public void testResponse301MovedPermanentlyWithPost() throws Exception {
        testResponseRedirectedWithPost(HttpURLConnection.HTTP_MOVED_PERM);
    }

    public void testResponse302MovedTemporarilyWithPost() throws Exception {
        testResponseRedirectedWithPost(HttpURLConnection.HTTP_MOVED_TEMP);
    }

    public void testResponse303SeeOtherWithPost() throws Exception {
        testResponseRedirectedWithPost(HttpURLConnection.HTTP_SEE_OTHER);
    }

    private void testResponseRedirectedWithPost(int redirectCode) throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(redirectCode)
                .addHeader("Location: /page2")
                .setBody("This page has moved!"));
        server.enqueue(new MockResponse().setBody("Page 2"));
        server.play();

        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/page1").openConnection();
        connection.setDoOutput(true);
        byte[] requestBody = { 'A', 'B', 'C', 'D' };
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(requestBody);
        outputStream.close();
        assertEquals("Page 2", readAscii(connection.getInputStream(), Integer.MAX_VALUE));
        assertTrue(connection.getDoOutput());

        RecordedRequest page1 = server.takeRequest();
        assertEquals("POST /page1 HTTP/1.1", page1.getRequestLine());
        assertEquals(Arrays.toString(requestBody), Arrays.toString(page1.getBody()));

        RecordedRequest page2 = server.takeRequest();
        assertEquals("GET /page2 HTTP/1.1", page2.getRequestLine());
    }

//  JVM failure.
//    public void testResponse305UseProxy() throws Exception {
//        server.play();
//        server.enqueue(new MockResponse()
//                .setResponseCode(HttpURLConnection.HTTP_USE_PROXY)
//                .addHeader("Location: " + server.getUrl("/"))
//                .setBody("This page has moved!"));
//        server.enqueue(new MockResponse().setBody("Proxy Response"));
//
//        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/foo").openConnection();
//        // Fails on the RI, which gets "Proxy Response"
//        assertEquals("This page has moved!",
//                readAscii(connection.getInputStream(), Integer.MAX_VALUE));
//
//        RecordedRequest page1 = server.takeRequest();
//        assertEquals("GET /foo HTTP/1.1", page1.getRequestLine());
//        assertEquals(1, server.getRequestCount());
//    }

//    public void testHttpsWithCustomTrustManager() throws Exception {
//        RecordingHostnameVerifier hostnameVerifier = new RecordingHostnameVerifier();
//        RecordingTrustManager trustManager = new RecordingTrustManager();
//        SSLContext sc = SSLContext.getInstance("TLS");
//        sc.init(null, new TrustManager[] { trustManager }, new java.security.SecureRandom());
//
//        HostnameVerifier defaultHostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
//        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
//        SSLSocketFactory defaultSSLSocketFactory = HttpsURLConnection.getDefaultSSLSocketFactory();
//        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//        try {
//            TestSSLContext testSSLContext = TestSSLContext.create();
//            server.useHttps(testSSLContext.serverContext.getSocketFactory(), false);
//            server.enqueue(new MockResponse().setBody("ABC"));
//            server.enqueue(new MockResponse().setBody("DEF"));
//            server.enqueue(new MockResponse().setBody("GHI"));
//            server.play();
//
//            URL url = server.getUrl("/");
//            assertEquals("ABC", readAscii(url.openStream(), Integer.MAX_VALUE));
//            assertEquals("DEF", readAscii(url.openStream(), Integer.MAX_VALUE));
//            assertEquals("GHI", readAscii(url.openStream(), Integer.MAX_VALUE));
//
//            assertEquals(Arrays.asList("verify " + hostName), hostnameVerifier.calls);
//            assertEquals(Arrays.asList("checkServerTrusted ["
//                    + "CN=" + hostName + " 3, "
//                    + "CN=Test Intermediate Certificate Authority 2, "
//                    + "CN=Test Root Certificate Authority 1"
//                    + "] ECDHE_RSA"),
//                    trustManager.calls);
//        } finally {
//            HttpsURLConnection.setDefaultHostnameVerifier(defaultHostnameVerifier);
//            HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSocketFactory);
//        }
//    }

    /**
     * Test that the timeout period is honored. The connect timeout is applied to each socket
     * connection attempt. If a hostname resolves to multiple IPs HttpURLConnection will wait the
     * full timeout for each.
    public void testConnectTimeouts() throws IOException {
        // During CTS tests we are limited in what host names we can depend on and unfortunately
        // DNS lookups are not pluggable through standard APIs. During manual testing you should be
        // able to change this to any name that can be resolved to multiple IPs and it should still
        // work.
        String hostName = "localhost";
        int expectedConnectionAttempts = InetAddress.getAllByName(hostName).length;
        int perSocketTimeout = 1000;
        final int[] socketCreationCount = new int[1];
        final int[] socketConnectTimeouts = new int[expectedConnectionAttempts];

        // It is difficult to force socket timeouts reliably so we replace the default SocketFactory
        // and have it create Sockets that always time out when connect(SocketAddress, int) is
        // called.
        SocketFactory originalDefault = SocketFactory.getDefault();
        try {
            // Override the default SocketFactory so we can intercept socket creation.
            SocketFactory.setDefault(new DelegatingSocketFactory(originalDefault) {
                @Override
                protected Socket configureSocket(Socket socket) throws IOException {
                    final int attemptNumber = socketCreationCount[0]++;
                    Answer socketConnectAnswer = new Answer() {
                        @Override public Object answer(InvocationOnMock invocation)
                                throws Throwable {
                            int timeoutArg = (int) invocation.getArguments()[1];
                            socketConnectTimeouts[attemptNumber] = timeoutArg;
                            throw new SocketTimeoutException(
                                "Simulated timeout after " + timeoutArg);
                        }
                    };

                    Socket socketSpy = spy(socket);
                    // Create a partial mock that wraps the actual socket and intercepts the
                    // connect(SocketAddress, int) method.
                    doAnswer(socketConnectAnswer)
                        .when(socketSpy).connect(any(SocketAddress.class), anyInt());
                    return socketSpy;
                }
            });

            URLConnection urlConnection = new URL("http://" + hostName + "/").openConnection();
            urlConnection.setConnectTimeout(perSocketTimeout);
            urlConnection.getInputStream();
            fail();
        } catch (SocketTimeoutException e) {
            assertEquals(expectedConnectionAttempts, socketCreationCount[0]);
            for (int i = 0; i < expectedConnectionAttempts; i++) {
                assertEquals(perSocketTimeout, socketConnectTimeouts[i]);
            }
        } finally {
            SocketFactory.setDefault(originalDefault);
        }
    }
     */

//    public void testReadTimeouts() throws IOException {
//        /*
//         * This relies on the fact that MockWebServer doesn't close the
//         * connection after a response has been sent. This causes the client to
//         * try to read more bytes than are sent, which results in a timeout.
//         */
//        MockResponse timeout = new MockResponse()
//                .setBody("ABC")
//                .clearHeaders()
//                .addHeader("Content-Length: 4");
//        server.enqueue(timeout);
//        server.enqueue(new MockResponse().setBody("unused")); // to keep the server alive
//        server.play();
//
//        URLConnection urlConnection = server.getUrl("/").openConnection();
//        urlConnection.setReadTimeout(1000);
//        InputStream in = urlConnection.getInputStream();
//        assertEquals('A', in.read());
//        assertEquals('B', in.read());
//        assertEquals('C', in.read());
//        try {
//            in.read(); // if Content-Length was accurate, this would return -1 immediately
//            fail();
//        } catch (SocketTimeoutException expected) {
//        }
//    }

    public void testSetChunkedEncodingAsRequestProperty() throws IOException, InterruptedException {
        server.enqueue(new MockResponse());
        server.play();

        HttpURLConnection urlConnection = (HttpURLConnection) server.getUrl("/").openConnection();
        urlConnection.setRequestProperty("Transfer-encoding", "chunked");
        urlConnection.setDoOutput(true);
        urlConnection.getOutputStream().write("ABC".getBytes("UTF-8"));
        assertEquals(200, urlConnection.getResponseCode());

        RecordedRequest request = server.takeRequest();
        assertEquals("ABC", new String(request.getBody(), "UTF-8"));
    }

//  JVM failure.
//    public void testConnectionCloseInRequest() throws IOException, InterruptedException {
//        server.enqueue(new MockResponse()); // server doesn't honor the connection: close header!
//        server.enqueue(new MockResponse());
//        server.play();
//
//        HttpURLConnection a = (HttpURLConnection) server.getUrl("/").openConnection();
//        a.setRequestProperty("Connection", "close");
//        assertEquals(200, a.getResponseCode());
//
//        HttpURLConnection b = (HttpURLConnection) server.getUrl("/").openConnection();
//        assertEquals(200, b.getResponseCode());
//
//        assertEquals(0, server.takeRequest().getSequenceNumber());
//        assertEquals("When connection: close is used, each request should get its own connection",
//                0, server.takeRequest().getSequenceNumber());
//    }

    public void testConnectionCloseInResponse() throws IOException, InterruptedException {
        server.enqueue(new MockResponse().addHeader("Connection: close"));
        server.enqueue(new MockResponse());
        server.play();

        HttpURLConnection a = (HttpURLConnection) server.getUrl("/").openConnection();
        assertEquals(200, a.getResponseCode());

        HttpURLConnection b = (HttpURLConnection) server.getUrl("/").openConnection();
        assertEquals(200, b.getResponseCode());

        assertEquals(0, server.takeRequest().getSequenceNumber());
        assertEquals("When connection: close is used, each request should get its own connection",
                0, server.takeRequest().getSequenceNumber());
    }

    public void testConnectionCloseWithRedirect() throws IOException, InterruptedException {
        MockResponse response = new MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP)
                .addHeader("Location: /foo")
                .addHeader("Connection: close");
        server.enqueue(response);
        server.enqueue(new MockResponse().setBody("This is the new location!"));
        server.play();

        URLConnection connection = server.getUrl("/").openConnection();
        assertEquals("This is the new location!",
                readAscii(connection.getInputStream(), Integer.MAX_VALUE));

        assertEquals(0, server.takeRequest().getSequenceNumber());
        assertEquals("When connection: close is used, each request should get its own connection",
                0, server.takeRequest().getSequenceNumber());
    }

// JVM failures.
//    public void testResponseCodeDisagreesWithHeaders() throws IOException, InterruptedException {
//        server.enqueue(new MockResponse()
//                .setResponseCode(HttpURLConnection.HTTP_NO_CONTENT)
//                .setBody("This body is not allowed!"));
//        server.play();
//
//        URLConnection connection = server.getUrl("/").openConnection();
//        assertEquals("This body is not allowed!",
//                readAscii(connection.getInputStream(), Integer.MAX_VALUE));
//    }

    public void testSingleByteReadIsSigned() throws IOException {
        server.enqueue(new MockResponse().setBody(new byte[] { -2, -1 }));
        server.play();

        URLConnection connection = server.getUrl("/").openConnection();
        InputStream in = connection.getInputStream();
        assertEquals(254, in.read());
        assertEquals(255, in.read());
        assertEquals(-1, in.read());
    }

// TODO(tball): b/28067294
//    public void testFlushAfterStreamTransmittedWithChunkedEncoding() throws IOException {
//        testFlushAfterStreamTransmitted(TransferKind.CHUNKED);
//    }

// TODO(tball): b/28067294
//    public void testFlushAfterStreamTransmittedWithFixedLength() throws IOException {
//        testFlushAfterStreamTransmitted(TransferKind.FIXED_LENGTH);
//    }

// TODO(tball): b/28067294
//    public void testFlushAfterStreamTransmittedWithNoLengthHeaders() throws IOException {
//        testFlushAfterStreamTransmitted(TransferKind.END_OF_STREAM);
//    }

    /**
     * We explicitly permit apps to close the upload stream even after it has
     * been transmitted.  We also permit flush so that buffered streams can
     * do a no-op flush when they are closed. http://b/3038470
     */
    private void testFlushAfterStreamTransmitted(TransferKind transferKind) throws IOException {
        server.enqueue(new MockResponse().setBody("abc"));
        server.play();

        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        connection.setDoOutput(true);
        byte[] upload = "def".getBytes("UTF-8");

        if (transferKind == TransferKind.CHUNKED) {
            connection.setChunkedStreamingMode(0);
        } else if (transferKind == TransferKind.FIXED_LENGTH) {
            connection.setFixedLengthStreamingMode(upload.length);
        }

        OutputStream out = connection.getOutputStream();
        out.write(upload);
        assertEquals("abc", readAscii(connection.getInputStream(), Integer.MAX_VALUE));

        out.flush(); // dubious but permitted
        try {
            out.write("ghi".getBytes("UTF-8"));
            fail();
        } catch (IOException expected) {
        }
    }

//    public void testGetHeadersThrows() throws IOException {
//        server.enqueue(new MockResponse().setSocketPolicy(DISCONNECT_AT_START));
//        server.play();
//
//        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
//        try {
//            connection.getInputStream();
//            fail();
//        } catch (IOException expected) {
//        }
//
//        try {
//            connection.getInputStream();
//            fail();
//        } catch (IOException expected) {
//        }
//    }

    public void testReadTimeoutsOnRecycledConnections() throws Exception {
        server.enqueue(new MockResponse().setBody("ABC"));
        server.play();

        // The request should work once and then fail
        URLConnection connection = server.getUrl("").openConnection();
        // Read timeout of a day, sure to cause the test to timeout and fail.
        connection.setReadTimeout(24 * 3600 * 1000);
        InputStream input = connection.getInputStream();
        assertEquals("ABC", readAscii(input, Integer.MAX_VALUE));
        input.close();
        try {
            connection = server.getUrl("").openConnection();
            // Set the read timeout back to 100ms, this request will time out
            // because we've only enqueued one response.
            connection.setReadTimeout(100);
            connection.getInputStream();
            fail();
        } catch (IOException expected) {
        }
    }

//  JVM failure.
    /**
     * This test goes through the exhaustive set of interesting ASCII characters
     * because most of those characters are interesting in some way according to
     * RFC 2396 and RFC 2732. http://b/1158780
     * After M, Android's HttpURLConnection started canonicalizing hostnames to lower case, IDN
     * encoding and being more strict about invalid characters.
     */
//    public void testUrlCharacterMapping() throws Exception {
//        server.setDispatcher(new Dispatcher() {
//            @Override public MockResponse dispatch(RecordedRequest request)
//                throws InterruptedException {
//                return new MockResponse();
//            }
//        });
//        server.play();
//
//        // alphanum
//        testUrlToUriMapping("abzABZ09", "abzabz09", "abzABZ09", "abzABZ09", "abzABZ09");
//        testUrlToRequestMapping("abzABZ09", "abzABZ09", "abzABZ09");
//
//        // control characters
//
//        // On JB-MR2 and below, we would allow a host containing \u0000
//        // and then generate a request with a Host header that violated RFC2616.
//        // We now reject such hosts.
//        //
//        // The ideal behaviour here is to be "lenient" about the host and rewrite
//        // it, but attempting to do so introduces a new range of incompatible
//        // behaviours.
//        testUrlToUriMapping("\u0000", null, "%00", "%00", "%00"); // RI fails this
//        testUrlToRequestMapping("\u0000", "%00", "%00");
//
//        testUrlToUriMapping("\u0001", null, "%01", "%01", "%01");
//        testUrlToRequestMapping("\u0001", "%01", "%01");
//
//        testUrlToUriMapping("\u001f", null, "%1F", "%1F", "%1F");
//        testUrlToRequestMapping("\u001f", "%1F", "%1F");
//
//        // ascii characters
//        testUrlToUriMapping("%20", null, "%20", "%20", "%20");
//        testUrlToRequestMapping("%20", "%20", "%20");
//        testUrlToUriMapping(" ", null, "%20", "%20", "%20");
//        testUrlToRequestMapping(" ", "%20", "%20");
//        testUrlToUriMapping("!", "!", "!", "!", "!");
//        testUrlToRequestMapping("!", "!", "!");
//        testUrlToUriMapping("\"", null, "%22", "%22", "%22");
//        testUrlToRequestMapping("\"", "%22", "%22");
//        testUrlToUriMapping("#", null, null, null, "%23");
//        testUrlToRequestMapping("#", null, null);
//        testUrlToUriMapping("$", "$", "$", "$", "$");
//        testUrlToRequestMapping("$", "$", "$");
//        testUrlToUriMapping("&", "&", "&", "&", "&");
//        testUrlToRequestMapping("&", "&", "&");
//        testUrlToUriMapping("'", "'", "'", "%27", "'");
//        testUrlToRequestMapping("'", "'", "%27");
//        testUrlToUriMapping("(", "(", "(", "(", "(");
//        testUrlToRequestMapping("(", "(", "(");
//        testUrlToUriMapping(")", ")", ")", ")", ")");
//        testUrlToRequestMapping(")", ")", ")");
//        testUrlToUriMapping("*", "*", "*", "*", "*");
//        testUrlToRequestMapping("*", "*", "*");
//        testUrlToUriMapping("+", "+", "+", "+", "+");
//        testUrlToRequestMapping("+", "+", "+");
//        testUrlToUriMapping(",", ",", ",", ",", ",");
//        testUrlToRequestMapping(",", ",", ",");
//        testUrlToUriMapping("-", "-", "-", "-", "-");
//        testUrlToRequestMapping("-", "-", "-");
//        testUrlToUriMapping(".", null, ".", ".", ".");
//        testUrlToRequestMapping(".", ".", ".");
//        testUrlToUriMapping(".foo", ".foo", ".foo", ".foo", ".foo");
//        testUrlToRequestMapping(".foo", ".foo", ".foo");
//        testUrlToUriMapping("/", null, "/", "/", "/");
//        testUrlToRequestMapping("/", "/", "/");
//        testUrlToUriMapping(":", null, ":", ":", ":");
//        testUrlToRequestMapping(":", ":", ":");
//        testUrlToUriMapping(";", ";", ";", ";", ";");
//        testUrlToRequestMapping(";", ";", ";");
//        testUrlToUriMapping("<", null, "%3C", "%3C", "%3C");
//        testUrlToRequestMapping("<", "%3C", "%3C");
//        testUrlToUriMapping("=", "=", "=", "=", "=");
//        testUrlToRequestMapping("=", "=", "=");
//        testUrlToUriMapping(">", null, "%3E", "%3E", "%3E");
//        testUrlToRequestMapping(">", "%3E", "%3E");
//        testUrlToUriMapping("?", null, null, "?", "?");
//        testUrlToRequestMapping("?", null, "?");
//        testUrlToUriMapping("@", null, "@", "@", "@");
//        testUrlToRequestMapping("@", "@", "@");
//        testUrlToUriMapping("[", null, null, null, "%5B");
//        testUrlToRequestMapping("[", null, null);
//        testUrlToUriMapping("\\", null, null, null, "%5C");
//        testUrlToRequestMapping("\\", null, null);
//        testUrlToUriMapping("]", null, null, null, "%5D");
//        testUrlToRequestMapping("]", null, null);
//        testUrlToUriMapping("^", null, "%5E", null, "%5E");
//        testUrlToRequestMapping("^", "%5E", null);
//        testUrlToUriMapping("_", "_", "_", "_", "_");
//        testUrlToRequestMapping("_", "_", "_");
//        testUrlToUriMapping("`", null, "%60", null, "%60");
//        testUrlToRequestMapping("`", "%60", null);
//        testUrlToUriMapping("{", null, "%7B", null, "%7B");
//        testUrlToRequestMapping("{", "%7B", null);
//        testUrlToUriMapping("|", null, "%7C", null, "%7C");
//        testUrlToRequestMapping("|", "%7C", null);
//        testUrlToUriMapping("}", null, "%7D", null, "%7D");
//        testUrlToRequestMapping("}", "%7D", null);
//        testUrlToUriMapping("~", "~", "~", "~", "~");
//        testUrlToRequestMapping("~", "~", "~");
//        testUrlToUriMapping("\u007f", null, "%7F", "%7F", "%7F");
//        testUrlToRequestMapping("\u007f", "%7F", "%7F");
//
//        // beyond ASCII
//
//        // 0x80 is the code point for the Euro sign in CP1252 (but not 8859-15 or Unicode).
//        // Unicode code point 0x80 is a control character and maps to {0xC2, 0x80} in UTF-8.
//        // 0x80 is outside of the ASCII range and is not supported by IDN in hostnames.
//        testUrlToUriMapping("\u0080", null, "%C2%80", "%C2%80", "%C2%80");
//        testUrlToRequestMapping("\u0080", "%C2%80", "%C2%80");
//
//        // More complicated transformations for the authorities below.
//
//        // 0x20AC is the code point for the Euro sign in Unicode.
//        // Unicode code point 0x20AC maps to {0xE2, 0x82, 0xAC} in UTF-8
//        // 0x20AC is not supported by all registrars but there are some legacy domains that
//        // use it and Android currently supports IDN conversion for it.
//        testUrlToUriMapping("\u20ac", null /* skip */, "%E2%82%AC", "%E2%82%AC", "%E2%82%AC");
//        testUrlToUriMappingAuthority("http://host\u20ac.tld/", "http://xn--host-yv7a.tld/");
//        testUrlToRequestMapping("\u20ac",  "%E2%82%AC", "%E2%82%AC");
//
//        // UTF-16 {0xD842, 0xDF9F} -> Unicode 0x20B9F (a Kanji character)
//        // Unicode code point 0x20B9F maps to {0xF0, 0xA0, 0xAE, 0x9F} in UTF-8
//        // IDN can deal with this code point.
//        testUrlToUriMapping("\ud842\udf9f", null /* skip */, "%F0%A0%AE%9F", "%F0%A0%AE%9F",
//            "%F0%A0%AE%9F");
//        testUrlToUriMappingAuthority("http://host\uD842\uDF9F.tld/", "http://xn--host-ov06c.tld/");
//        testUrlToRequestMapping("\ud842\udf9f",  "%F0%A0%AE%9F", "%F0%A0%AE%9F");
//    }
//
    private void testUrlToUriMappingAuthority(String urlString, String expectedUriString)
        throws Exception {
        URI authorityUri = backdoorUrlToUri(new URL(urlString));
        assertEquals(expectedUriString, authorityUri.toString());
    }

    /**
     * Exercises HttpURLConnection to convert URL to a URI. Unlike URL#toURI,
     * HttpURLConnection recovers from URLs with unescaped but unsupported URI
     * characters like '{' and '|' by escaping these characters.
     */
    private URI backdoorUrlToUri(URL url) throws Exception {
        final AtomicReference<URI> uriReference = new AtomicReference<URI>();

        ResponseCache.setDefault(new ResponseCache() {
            @Override public CacheRequest put(URI uri, URLConnection connection)
                    throws IOException {
                return null;
            }
            @Override public CacheResponse get(URI uri, String requestMethod,
                    Map<String, List<String>> requestHeaders) throws IOException {
                uriReference.set(uri);
                throw new UnsupportedOperationException();
            }
        });

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.getResponseCode();
        } catch (Exception expected) {
        }

        return uriReference.get();
    }

    /*
     * Test the request that would be made by making an actual request a MockWebServer and capturing
     * the request made.
     *
     * Any "as" values that are null are not tested.
     */
    private void testUrlToRequestMapping(
            String string, String asFile, String asQuery) throws Exception {
        if (asFile != null) {
            URL fileUrl = server.getUrl("/file" + string + "/#discarded");
            HttpURLConnection urlConnection = (HttpURLConnection) fileUrl.openConnection();
            // Bypass the cache.
            urlConnection.setUseCaches(false);

            assertEquals(200, urlConnection.getResponseCode());
            assertEquals("/file" + asFile + "/", server.takeRequest().getPath());
        }
        if (asQuery != null) {
            URL queryUrl = server.getUrl("/file?q" + string + "=x#discarded");
            HttpURLConnection urlConnection = (HttpURLConnection) queryUrl.openConnection();
            // Bypass the cache.
            urlConnection.setUseCaches(false);

            assertEquals(200, urlConnection.getResponseCode());
            assertEquals("/file?q" + asQuery + "=x", server.takeRequest().getPath());
        }
    }

    /*
     * Test the request that would be made by looking at the URI presented to the cache. This
     * includes the likely host name that would be used if a request were made. The cache throws an
     * exception so no request is actually made.
     *
     * Any "as" values that are null are not tested.
     */
    private void testUrlToUriMapping(String string, String asAuthority, String asFile,
            String asQuery, String asFragment) throws Exception {
        if (asAuthority != null) {
            URI authorityUri = backdoorUrlToUri(new URL("http://host" + string + ".tld/"));
            assertEquals("http://host" + asAuthority + ".tld/", authorityUri.toString());
        }
        if (asFile != null) {
            URI fileUri = backdoorUrlToUri(new URL("http://host.tld/file" + string + "/"));
            assertEquals("http://host.tld/file" + asFile + "/", fileUri.toString());
        }
        if (asQuery != null) {
            URI queryUri = backdoorUrlToUri(new URL("http://host.tld/file?q" + string + "=x"));
            assertEquals("http://host.tld/file?q" + asQuery + "=x", queryUri.toString());
        }
        assertEquals("http://host.tld/file#" + asFragment + "-x",
            backdoorUrlToUri(new URL("http://host.tld/file#" + asFragment + "-x")).toString());
    }

// TODO(tball): b/28067294
//    public void testHostWithNul() throws Exception {
//        URL url = new URL("http://host\u0000/");
//        try {
//            url.openStream();
//            fail();
//        } catch (UnknownHostException expected) {}
//    }

    /**
     * Don't explode if the cache returns a null body. http://b/3373699
     */
    public void testResponseCacheReturnsNullOutputStream() throws Exception {
        final AtomicBoolean aborted = new AtomicBoolean();
        ResponseCache.setDefault(new ResponseCache() {
            @Override public CacheResponse get(URI uri, String requestMethod,
                    Map<String, List<String>> requestHeaders) throws IOException {
                return null;
            }
            @Override public CacheRequest put(URI uri, URLConnection connection) throws IOException {
                return new CacheRequest() {
                    @Override public void abort() {
                        aborted.set(true);
                    }
                    @Override public OutputStream getBody() throws IOException {
                        return null;
                    }
                };
            }
        });

        server.enqueue(new MockResponse().setBody("abcdef"));
        server.play();

        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        InputStream in = connection.getInputStream();
        assertEquals("abc", readAscii(in, 3));
        in.close();
        assertFalse(aborted.get()); // The best behavior is ambiguous, but RI 6 doesn't abort here
    }


    /**
     * http://code.google.com/p/android/issues/detail?id=14562
     */
    public void testReadAfterLastByte() throws Exception {
        server.enqueue(new MockResponse()
                .setBody("ABC")
                .clearHeaders()
                .addHeader("Connection: close")
                .setSocketPolicy(SocketPolicy.DISCONNECT_AT_END));
        server.play();

        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        InputStream in = connection.getInputStream();
        assertEquals("ABC", readAscii(in, 3));
        assertEquals(-1, in.read());
        assertEquals(-1, in.read()); // throws IOException in Gingerbread
    }

//  JVM failure.
//    public void testGetContent() throws Exception {
//        server.enqueue(new MockResponse().setBody("A"));
//        server.play();
//        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
//        InputStream in = (InputStream) connection.getContent();
//        assertEquals("A", readAscii(in, Integer.MAX_VALUE));
//    }

//  JVM failure.
//    public void testGetContentOfType() throws Exception {
//        server.enqueue(new MockResponse().setBody("A"));
//        server.play();
//        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
//        try {
//            connection.getContent(null);
//            fail();
//        } catch (NullPointerException expected) {
//        }
//        try {
//            connection.getContent(new Class[] { null });
//            fail();
//        } catch (NullPointerException expected) {
//        }
//        assertNull(connection.getContent(new Class[] { getClass() }));
//        connection.disconnect();
//    }

// TODO(tball): b/28067294
//    public void testGetOutputStreamOnGetFails() throws Exception {
//        server.enqueue(new MockResponse());
//        server.play();
//        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
//        try {
//            connection.getOutputStream();
//            fail();
//        } catch (ProtocolException expected) {
//        }
//    }

// TODO(tball): b/28067294
//    public void testGetOutputAfterGetInputStreamFails() throws Exception {
//        server.enqueue(new MockResponse());
//        server.play();
//        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
//        connection.setDoOutput(true);
//        try {
//            connection.getInputStream();
//            connection.getOutputStream();
//            fail();
//        } catch (ProtocolException expected) {
//        }
//    }

    public void testSetDoOutputOrDoInputAfterConnectFails() throws Exception {
        server.enqueue(new MockResponse());
        server.play();
        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        connection.connect();
        try {
            connection.setDoOutput(true);
            fail();
        } catch (IllegalStateException expected) {
        }
        try {
            connection.setDoInput(true);
            fail();
        } catch (IllegalStateException expected) {
        }
        connection.disconnect();
    }

    public void testLastModified() throws Exception {
        server.enqueue(new MockResponse()
                .addHeader("Last-Modified", "Wed, 27 Nov 2013 11:26:00 GMT")
                .setBody("Hello"));
        server.play();

        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        connection.connect();

        assertEquals(1385551560000L, connection.getLastModified());
        assertEquals(1385551560000L, connection.getHeaderFieldDate("Last-Modified", -1));
    }

    public void testClientSendsContentLength() throws Exception {
        server.enqueue(new MockResponse().setBody("A"));
        server.play();
        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        connection.setDoOutput(true);
        OutputStream out = connection.getOutputStream();
        out.write(new byte[] { 'A', 'B', 'C' });
        out.close();
        assertEquals("A", readAscii(connection.getInputStream(), Integer.MAX_VALUE));
        RecordedRequest request = server.takeRequest();
        assertContains(request.getHeaders(), "Content-Length: 3");
    }

    public void testGetContentLengthConnects() throws Exception {
        server.enqueue(new MockResponse().setBody("ABC"));
        server.play();
        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        assertEquals(3, connection.getContentLength());
        connection.disconnect();
    }

    public void testGetContentTypeConnects() throws Exception {
        server.enqueue(new MockResponse()
                .addHeader("Content-Type: text/plain")
                .setBody("ABC"));
        server.play();
        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        assertEquals("text/plain", connection.getContentType());
        connection.disconnect();
    }

    public void testGetContentEncodingConnects() throws Exception {
        server.enqueue(new MockResponse()
                .addHeader("Content-Encoding: identity")
                .setBody("ABC"));
        server.play();
        HttpURLConnection connection = (HttpURLConnection) server.getUrl("/").openConnection();
        assertEquals("identity", connection.getContentEncoding());
        connection.disconnect();
    }

    // http://b/4361656
    public void testUrlContainsQueryButNoPath() throws Exception {
        server.enqueue(new MockResponse().setBody("A"));
        server.play();
        URL url = new URL("http", server.getHostName(), server.getPort(), "?query");
        assertEquals("A", readAscii(url.openConnection().getInputStream(), Integer.MAX_VALUE));
        RecordedRequest request = server.takeRequest();
        assertEquals("GET /?query HTTP/1.1", request.getRequestLine());
    }

    // http://code.google.com/p/android/issues/detail?id=20442
    public void testInputStreamAvailableWithChunkedEncoding() throws Exception {
        testInputStreamAvailable(TransferKind.CHUNKED);
    }

    public void testInputStreamAvailableWithContentLengthHeader() throws Exception {
        testInputStreamAvailable(TransferKind.FIXED_LENGTH);
    }

    public void testInputStreamAvailableWithNoLengthHeaders() throws Exception {
        testInputStreamAvailable(TransferKind.END_OF_STREAM);
    }

    private void testInputStreamAvailable(TransferKind transferKind) throws IOException {
        String body = "ABCDEFGH";
        MockResponse response = new MockResponse();
        transferKind.setBody(response, body, 4);
        server.enqueue(response);
        server.play();
        URLConnection connection = server.getUrl("/").openConnection();
        InputStream in = connection.getInputStream();
        for (int i = 0; i < body.length(); i++) {
            assertTrue(in.available() >= 0);
            assertEquals(body.charAt(i), in.read());
        }
        assertEquals(0, in.available());
        assertEquals(-1, in.read());
    }

// TODO(tball): b/28067294
    // http://code.google.com/p/android/issues/detail?id=28095
//    public void testInvalidIpv4Address() throws Exception {
//        try {
//            URI uri = new URI("http://1111.111.111.111/index.html");
//            uri.toURL().openConnection().connect();
//            fail();
//        } catch (UnknownHostException expected) {
//        }
//    }

// TODO(tball): b/28067294
    // http://code.google.com/p/android/issues/detail?id=16895
//    public void testUrlWithSpaceInHost() throws Exception {
//        URLConnection urlConnection = new URL("http://and roid.com/").openConnection();
//        try {
//            urlConnection.getInputStream();
//            fail();
//        } catch (UnknownHostException expected) {
//        }
//    }

// TODO(tball): b/28067294
    // http://code.google.com/p/android/issues/detail?id=16895
//    public void testUrlWithSpaceInHostViaHttpProxy() throws Exception {
//        server.enqueue(new MockResponse());
//        server.play();
//        URLConnection urlConnection = new URL("http://and roid.com/")
//                .openConnection(server.toProxyAddress());
//
//        try {
//            // This test is to check that a NullPointerException is not thrown.
//            urlConnection.getInputStream();
//            fail(); // the RI makes a bogus proxy request for "GET http://and roid.com/ HTTP/1.1"
//        } catch (UnknownHostException expected) {
//        }
//    }

//    public void testSslFallback_allSupportedProtocols() throws Exception {
//        TestSSLContext testSSLContext = TestSSLContext.create();
//
//        String[] allSupportedProtocols = { "TLSv1.2", "TLSv1.1", "TLSv1", "SSLv3" };
//        SSLSocketFactory serverSocketFactory =
//                new LimitedProtocolsSocketFactory(
//                        testSSLContext.serverContext.getSocketFactory(),
//                        allSupportedProtocols);
//        server.useHttps(serverSocketFactory, false);
//        server.enqueue(new MockResponse().setSocketPolicy(FAIL_HANDSHAKE));
//        server.enqueue(new MockResponse().setSocketPolicy(FAIL_HANDSHAKE));
//        server.enqueue(new MockResponse().setSocketPolicy(FAIL_HANDSHAKE));
//        server.enqueue(new MockResponse().setBody("This required fallbacks"));
//        server.play();
//
//        HttpsURLConnection connection = (HttpsURLConnection) server.getUrl("/").openConnection();
//        // Keeps track of the client sockets created so that we can interrogate them.
//        final boolean disableFallbackScsv = true;
//        FallbackTestClientSocketFactory clientSocketFactory = new FallbackTestClientSocketFactory(
//                new LimitedProtocolsSocketFactory(
//                        testSSLContext.clientContext.getSocketFactory(), allSupportedProtocols),
//                disableFallbackScsv);
//        connection.setSSLSocketFactory(clientSocketFactory);
//        assertEquals("This required fallbacks",
//                readAscii(connection.getInputStream(), Integer.MAX_VALUE));
//
//        // Confirm the server accepted a single connection.
//        RecordedRequest retry = server.takeRequest();
//        assertEquals(0, retry.getSequenceNumber());
//        assertEquals("SSLv3", retry.getSslProtocol());
//
//        // Confirm the client fallback looks ok.
//        List<SSLSocket> createdSockets = clientSocketFactory.getCreatedSockets();
//        assertEquals(4, createdSockets.size());
//        TlsFallbackDisabledScsvSSLSocket clientSocket1 =
//                (TlsFallbackDisabledScsvSSLSocket) createdSockets.get(0);
//        assertSslSocket(clientSocket1,
//                false /* expectedWasFallbackScsvSet */, "TLSv1.2", "TLSv1.1", "TLSv1", "SSLv3");
//
//        TlsFallbackDisabledScsvSSLSocket clientSocket2 =
//                (TlsFallbackDisabledScsvSSLSocket) createdSockets.get(1);
//        assertSslSocket(clientSocket2,
//                true /* expectedWasFallbackScsvSet */, "TLSv1.1", "TLSv1", "SSLv3");
//
//        TlsFallbackDisabledScsvSSLSocket clientSocket3 =
//                (TlsFallbackDisabledScsvSSLSocket) createdSockets.get(2);
//        assertSslSocket(clientSocket3, true /* expectedWasFallbackScsvSet */, "TLSv1", "SSLv3");
//
//        TlsFallbackDisabledScsvSSLSocket clientSocket4 =
//                (TlsFallbackDisabledScsvSSLSocket) createdSockets.get(3);
//        assertSslSocket(clientSocket4, true /* expectedWasFallbackScsvSet */, "SSLv3");
//    }
//
//    public void testSslFallback_defaultProtocols() throws Exception {
//        TestSSLContext testSSLContext = TestSSLContext.create();
//
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), false);
//        server.enqueue(new MockResponse().setSocketPolicy(FAIL_HANDSHAKE));
//        server.enqueue(new MockResponse().setSocketPolicy(FAIL_HANDSHAKE));
//        server.enqueue(new MockResponse().setBody("This required fallbacks"));
//        server.play();
//
//        HttpsURLConnection connection = (HttpsURLConnection) server.getUrl("/").openConnection();
//        // Keeps track of the client sockets created so that we can interrogate them.
//        final boolean disableFallbackScsv = true;
//        FallbackTestClientSocketFactory clientSocketFactory = new FallbackTestClientSocketFactory(
//                testSSLContext.clientContext.getSocketFactory(),
//                disableFallbackScsv);
//        connection.setSSLSocketFactory(clientSocketFactory);
//        assertEquals("This required fallbacks",
//                readAscii(connection.getInputStream(), Integer.MAX_VALUE));
//
//        // Confirm the server accepted a single connection.
//        RecordedRequest retry = server.takeRequest();
//        assertEquals(0, retry.getSequenceNumber());
//        assertEquals("TLSv1", retry.getSslProtocol());
//
//        // Confirm the client fallback looks ok.
//        List<SSLSocket> createdSockets = clientSocketFactory.getCreatedSockets();
//        assertEquals(3, createdSockets.size());
//        TlsFallbackDisabledScsvSSLSocket clientSocket1 =
//                (TlsFallbackDisabledScsvSSLSocket) createdSockets.get(0);
//        assertSslSocket(clientSocket1,
//                false /* expectedWasFallbackScsvSet */, "TLSv1.2", "TLSv1.1", "TLSv1");
//
//        TlsFallbackDisabledScsvSSLSocket clientSocket2 =
//                (TlsFallbackDisabledScsvSSLSocket) createdSockets.get(1);
//        assertSslSocket(clientSocket2, true /* expectedWasFallbackScsvSet */, "TLSv1.1", "TLSv1");
//
//        TlsFallbackDisabledScsvSSLSocket clientSocket3 =
//                (TlsFallbackDisabledScsvSSLSocket) createdSockets.get(2);
//        assertSslSocket(clientSocket3, true /* expectedWasFallbackScsvSet */, "TLSv1");
//    }

//    private static void assertSslSocket(TlsFallbackDisabledScsvSSLSocket socket,
//            boolean expectedWasFallbackScsvSet, String... expectedEnabledProtocols) {
//        Set<String> enabledProtocols =
//                new HashSet<String>(Arrays.asList(socket.getEnabledProtocols()));
//        Set<String> expectedProtocolsSet = new HashSet<String>(Arrays.asList(expectedEnabledProtocols));
//        assertEquals(expectedProtocolsSet, enabledProtocols);
//        assertEquals(expectedWasFallbackScsvSet, socket.wasTlsFallbackScsvSet());
//    }
//
//    public void testInspectSslBeforeConnect() throws Exception {
//        TestSSLContext testSSLContext = TestSSLContext.create();
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), false);
//        server.enqueue(new MockResponse());
//        server.play();
//
//        HttpsURLConnection connection = (HttpsURLConnection) server.getUrl("/").openConnection();
//        connection.setSSLSocketFactory(testSSLContext.clientContext.getSocketFactory());
//        assertNotNull(connection.getHostnameVerifier());
//        try {
//            connection.getLocalCertificates();
//            fail();
//        } catch (IllegalStateException expected) {
//        }
//        try {
//            connection.getServerCertificates();
//            fail();
//        } catch (IllegalStateException expected) {
//        }
//        try {
//            connection.getCipherSuite();
//            fail();
//        } catch (IllegalStateException expected) {
//        }
//        try {
//            connection.getPeerPrincipal();
//            fail();
//        } catch (IllegalStateException expected) {
//        }
//    }
//
//    /**
//     * Test that we can inspect the SSL session after connect().
//     * http://code.google.com/p/android/issues/detail?id=24431
//     */
//    public void testInspectSslAfterConnect() throws Exception {
//        TestSSLContext testSSLContext = TestSSLContext.create();
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), false);
//        server.enqueue(new MockResponse());
//        server.play();
//
//        HttpsURLConnection connection = (HttpsURLConnection) server.getUrl("/").openConnection();
//        connection.setSSLSocketFactory(testSSLContext.clientContext.getSocketFactory());
//        connection.connect();
//        try {
//            assertNotNull(connection.getHostnameVerifier());
//            assertNull(connection.getLocalCertificates());
//            assertNotNull(connection.getServerCertificates());
//            assertNotNull(connection.getCipherSuite());
//            assertNotNull(connection.getPeerPrincipal());
//        } finally {
//            connection.disconnect();
//        }
//    }
//
//    // http://b/26769689
//    public void testSSLSocketFactoryWithIpv6LiteralHostname() throws Exception {
//        TestSSLContext testSSLContext = TestSSLContext.create();
//        server.useHttps(testSSLContext.serverContext.getSocketFactory(), false);
//        server.enqueue(new MockResponse());
//        server.play();
//
//        final AtomicReference<String> hostNameUsed = new AtomicReference<>(null);
//
//        SSLSocketFactory factory = new DelegatingSSLSocketFactory(
//                testSSLContext.clientContext.getSocketFactory()) {
//            @Override
//            public SSLSocket createSocket(Socket s, String host, int port, boolean autoClose)
//                    throws IOException {
//                hostNameUsed.set(host);
//                return (SSLSocket) delegate.createSocket(s, host, port, autoClose);
//            }
//        };
//
//        HttpsURLConnection urlConnection = (HttpsURLConnection)
//                new URL("https://[" + Inet6Address.getLoopbackAddress().getHostAddress() + "]:"
//                        + server.getPort() + "/").openConnection();
//        urlConnection.setSSLSocketFactory(factory);
//        try {
//            urlConnection.connect();
//            fail();
//        } catch (IOException expected) {
//            // We expect the connection to fail with a cert validation exception because we're
//            // using a literal address.
//        } finally {
//            urlConnection.disconnect();
//        }
//
//        // Note that the square brackets around the literal address were crucial. Whatsapp
//        // wouldn't function properly without them.
//        assertEquals("[" + Inet6Address.getLoopbackAddress().getHostAddress() + "]",
//                hostNameUsed.get());
//    }

    /**
     * Returns a gzipped copy of {@code bytes}.
     */
    public byte[] gzip(byte[] bytes) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        OutputStream gzippedOut = new GZIPOutputStream(bytesOut);
        gzippedOut.write(bytes);
        gzippedOut.close();
        return bytesOut.toByteArray();
    }

    /**
     * Reads at most {@code limit} characters from {@code in} and asserts that
     * content equals {@code expected}.
     */
    private void assertContent(String expected, URLConnection connection, int limit)
            throws IOException {
        connection.connect();
        assertEquals(expected, readAscii(connection.getInputStream(), limit));
        ((HttpURLConnection) connection).disconnect();
    }

    private void assertContent(String expected, URLConnection connection) throws IOException {
        assertContent(expected, connection, Integer.MAX_VALUE);
    }

    private void assertContains(List<String> list, String value) {
        assertTrue(list.toString(), list.contains(value));
    }

    private void assertContainsNoneMatching(List<String> list, String pattern) {
        for (String header : list) {
            if (header.matches(pattern)) {
                fail("Header " + header + " matches " + pattern);
            }
        }
    }

    private Set<String> newSet(String... elements) {
        return new HashSet<String>(Arrays.asList(elements));
    }

    enum TransferKind {
        CHUNKED() {
            @Override void setBody(MockResponse response, byte[] content, int chunkSize)
                    throws IOException {
                response.setChunkedBody(content, chunkSize);
            }
        },
        FIXED_LENGTH() {
            @Override void setBody(MockResponse response, byte[] content, int chunkSize) {
                response.setBody(content);
            }
        },
        END_OF_STREAM() {
            @Override void setBody(MockResponse response, byte[] content, int chunkSize) {
                response.setBody(content);
                response.setSocketPolicy(DISCONNECT_AT_END);
                for (Iterator<String> h = response.getHeaders().iterator(); h.hasNext(); ) {
                    if (h.next().startsWith("Content-Length:")) {
                        h.remove();
                        break;
                    }
                }
            }
        };

        abstract void setBody(MockResponse response, byte[] content, int chunkSize)
                throws IOException;

        void setBody(MockResponse response, String content, int chunkSize) throws IOException {
            setBody(response, content.getBytes("UTF-8"), chunkSize);
        }
    }

    enum ProxyConfig {
        NO_PROXY() {
            @Override public HttpURLConnection connect(MockWebServer server, URL url)
                    throws IOException {
                return (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
            }
        },

        CREATE_ARG() {
            @Override public HttpURLConnection connect(MockWebServer server, URL url)
                    throws IOException {
                return (HttpURLConnection) url.openConnection(server.toProxyAddress());
            }
        },

        PROXY_SYSTEM_PROPERTY() {
            @Override public HttpURLConnection connect(MockWebServer server, URL url)
                    throws IOException {
                System.setProperty("proxyHost", "localhost");
                System.setProperty("proxyPort", Integer.toString(server.getPort()));
                return (HttpURLConnection) url.openConnection();
            }
        },

        HTTP_PROXY_SYSTEM_PROPERTY() {
            @Override public HttpURLConnection connect(MockWebServer server, URL url)
                    throws IOException {
                System.setProperty("http.proxyHost", "localhost");
                System.setProperty("http.proxyPort", Integer.toString(server.getPort()));
                return (HttpURLConnection) url.openConnection();
            }
        },

        HTTPS_PROXY_SYSTEM_PROPERTY() {
            @Override public HttpURLConnection connect(MockWebServer server, URL url)
                    throws IOException {
                System.setProperty("https.proxyHost", "localhost");
                System.setProperty("https.proxyPort", Integer.toString(server.getPort()));
                return (HttpURLConnection) url.openConnection();
            }
        };

        public abstract HttpURLConnection connect(MockWebServer server, URL url) throws IOException;
    }

    private static class RecordingTrustManager implements X509TrustManager {
        private final List<String> calls = new ArrayList<String>();

        public X509Certificate[] getAcceptedIssuers() {
            calls.add("getAcceptedIssuers");
            return new X509Certificate[] {};
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            calls.add("checkClientTrusted " + certificatesToString(chain) + " " + authType);
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            calls.add("checkServerTrusted " + certificatesToString(chain) + " " + authType);
        }

        private String certificatesToString(X509Certificate[] certificates) {
            List<String> result = new ArrayList<String>();
            for (X509Certificate certificate : certificates) {
                result.add(certificate.getSubjectDN() + " " + certificate.getSerialNumber());
            }
            return result.toString();
        }
    }

    private static class RecordingHostnameVerifier implements HostnameVerifier {
        private final List<String> calls = new ArrayList<String>();

        public boolean verify(String hostname, SSLSession session) {
            calls.add("verify " + hostname);
            return true;
        }
    }

    private static class SimpleAuthenticator extends Authenticator {
        /** base64("username:password") */
        private static final String BASE_64_CREDENTIALS = "dXNlcm5hbWU6cGFzc3dvcmQ=";

        private String expectedPrompt;
        private RequestorType requestorType;
        private int requestingPort;
        private InetAddress requestingSite;
        private String requestingPrompt;
        private String requestingProtocol;
        private String requestingScheme;

        protected PasswordAuthentication getPasswordAuthentication() {
            requestorType = getRequestorType();
            requestingPort = getRequestingPort();
            requestingSite = getRequestingSite();
            requestingPrompt = getRequestingPrompt();
            requestingProtocol = getRequestingProtocol();
            requestingScheme = getRequestingScheme();
            return (expectedPrompt == null || expectedPrompt.equals(requestingPrompt))
                    ? new PasswordAuthentication("username", "password".toCharArray())
                    : null;
        }
    }

    /**
     * An SSLSocketFactory that delegates all calls.
     */
    private static class DelegatingSSLSocketFactory extends SSLSocketFactory {

        protected final SSLSocketFactory delegate;

        public DelegatingSSLSocketFactory(SSLSocketFactory delegate) {
            this.delegate = delegate;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override
        public SSLSocket createSocket(Socket s, String host, int port, boolean autoClose)
                throws IOException {
            return (SSLSocket) delegate.createSocket(s, host, port, autoClose);
        }

        @Override
        public SSLSocket createSocket() throws IOException {
            return (SSLSocket) delegate.createSocket();
        }

        @Override
        public SSLSocket createSocket(String host, int port)
                throws IOException, UnknownHostException {
            return (SSLSocket) delegate.createSocket(host, port);
        }

        @Override
        public SSLSocket createSocket(String host, int port, InetAddress localHost,
                int localPort) throws IOException, UnknownHostException {
            return (SSLSocket) delegate.createSocket(host, port, localHost, localPort);
        }

        @Override
        public SSLSocket createSocket(InetAddress host, int port) throws IOException {
            return (SSLSocket) delegate.createSocket(host, port);
        }

        @Override
        public SSLSocket createSocket(InetAddress address, int port,
                InetAddress localAddress, int localPort) throws IOException {
            return (SSLSocket) delegate.createSocket(address, port, localAddress, localPort);
        }

    }

    /**
     * An SSLSocketFactory that delegates calls but limits the enabled protocols for any created
     * sockets.
     */
    private static class LimitedProtocolsSocketFactory extends DelegatingSSLSocketFactory {

        private final String[] protocols;

        private LimitedProtocolsSocketFactory(SSLSocketFactory delegate, String... protocols) {
            super(delegate);
            this.protocols = protocols;
        }

        @Override
        public SSLSocket createSocket(Socket s, String host, int port, boolean autoClose)
                throws IOException {
            SSLSocket socket = (SSLSocket) delegate.createSocket(s, host, port, autoClose);
            socket.setEnabledProtocols(protocols);
            return socket;
        }

        @Override
        public SSLSocket createSocket() throws IOException {
            SSLSocket socket = (SSLSocket) delegate.createSocket();
            socket.setEnabledProtocols(protocols);
            return socket;
        }

        @Override
        public SSLSocket createSocket(String host, int port)
                throws IOException, UnknownHostException {
            SSLSocket socket = (SSLSocket) delegate.createSocket(host, port);
            socket.setEnabledProtocols(protocols);
            return socket;
        }

        @Override
        public SSLSocket createSocket(String host, int port, InetAddress localHost,
                int localPort) throws IOException, UnknownHostException {
            SSLSocket socket = (SSLSocket) delegate.createSocket(host, port, localHost, localPort);
            socket.setEnabledProtocols(protocols);
            return socket;
        }

        @Override
        public SSLSocket createSocket(InetAddress host, int port) throws IOException {
            SSLSocket socket = (SSLSocket) delegate.createSocket(host, port);
            socket.setEnabledProtocols(protocols);
            return socket;
        }

        @Override
        public SSLSocket createSocket(InetAddress address, int port,
                InetAddress localAddress, int localPort) throws IOException {
            SSLSocket socket =
                    (SSLSocket) delegate.createSocket(address, port, localAddress, localPort);
            socket.setEnabledProtocols(protocols);
            return socket;
        }
    }

    /**
     * An {@link javax.net.ssl.SSLSocket} that delegates all calls.
     */
    private static abstract class DelegatingSSLSocket extends SSLSocket {
        protected final SSLSocket delegate;

        public DelegatingSSLSocket(SSLSocket delegate) {
            this.delegate = delegate;
        }

        @Override public void shutdownInput() throws IOException {
            delegate.shutdownInput();
        }

        @Override public void shutdownOutput() throws IOException {
            delegate.shutdownOutput();
        }

        @Override public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override public String[] getEnabledCipherSuites() {
            return delegate.getEnabledCipherSuites();
        }

        @Override public void setEnabledCipherSuites(String[] suites) {
            delegate.setEnabledCipherSuites(suites);
        }

        @Override public String[] getSupportedProtocols() {
            return delegate.getSupportedProtocols();
        }

        @Override public String[] getEnabledProtocols() {
            return delegate.getEnabledProtocols();
        }

        @Override public void setEnabledProtocols(String[] protocols) {
            delegate.setEnabledProtocols(protocols);
        }

        @Override public SSLSession getSession() {
            return delegate.getSession();
        }

        @Override public void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
            delegate.addHandshakeCompletedListener(listener);
        }

        @Override public void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
            delegate.removeHandshakeCompletedListener(listener);
        }

        @Override public void startHandshake() throws IOException {
            delegate.startHandshake();
        }

        @Override public void setUseClientMode(boolean mode) {
            delegate.setUseClientMode(mode);
        }

        @Override public boolean getUseClientMode() {
            return delegate.getUseClientMode();
        }

        @Override public void setNeedClientAuth(boolean need) {
            delegate.setNeedClientAuth(need);
        }

        @Override public void setWantClientAuth(boolean want) {
            delegate.setWantClientAuth(want);
        }

        @Override public boolean getNeedClientAuth() {
            return delegate.getNeedClientAuth();
        }

        @Override public boolean getWantClientAuth() {
            return delegate.getWantClientAuth();
        }

        @Override public void setEnableSessionCreation(boolean flag) {
            delegate.setEnableSessionCreation(flag);
        }

        @Override public boolean getEnableSessionCreation() {
            return delegate.getEnableSessionCreation();
        }

        @Override public SSLParameters getSSLParameters() {
            return delegate.getSSLParameters();
        }

        @Override public void setSSLParameters(SSLParameters p) {
            delegate.setSSLParameters(p);
        }

        @Override public void close() throws IOException {
            delegate.close();
        }

        @Override public InetAddress getInetAddress() {
            return delegate.getInetAddress();
        }

        @Override public InputStream getInputStream() throws IOException {
            return delegate.getInputStream();
        }

        @Override public boolean getKeepAlive() throws SocketException {
            return delegate.getKeepAlive();
        }

        @Override public InetAddress getLocalAddress() {
            return delegate.getLocalAddress();
        }

        @Override public int getLocalPort() {
            return delegate.getLocalPort();
        }

        @Override public OutputStream getOutputStream() throws IOException {
            return delegate.getOutputStream();
        }

        @Override public int getPort() {
            return delegate.getPort();
        }

        @Override public int getSoLinger() throws SocketException {
            return delegate.getSoLinger();
        }

        @Override public int getReceiveBufferSize() throws SocketException {
            return delegate.getReceiveBufferSize();
        }

        @Override public int getSendBufferSize() throws SocketException {
            return delegate.getSendBufferSize();
        }

        @Override public int getSoTimeout() throws SocketException {
            return delegate.getSoTimeout();
        }

        @Override public boolean getTcpNoDelay() throws SocketException {
            return delegate.getTcpNoDelay();
        }

        @Override public void setKeepAlive(boolean keepAlive) throws SocketException {
            delegate.setKeepAlive(keepAlive);
        }

        @Override public void setSendBufferSize(int size) throws SocketException {
            delegate.setSendBufferSize(size);
        }

        @Override public void setReceiveBufferSize(int size) throws SocketException {
            delegate.setReceiveBufferSize(size);
        }

        @Override public void setSoLinger(boolean on, int timeout) throws SocketException {
            delegate.setSoLinger(on, timeout);
        }

        @Override public void setSoTimeout(int timeout) throws SocketException {
            delegate.setSoTimeout(timeout);
        }

        @Override public void setTcpNoDelay(boolean on) throws SocketException {
            delegate.setTcpNoDelay(on);
        }

        @Override public String toString() {
            return delegate.toString();
        }

        @Override public SocketAddress getLocalSocketAddress() {
            return delegate.getLocalSocketAddress();
        }

        @Override public SocketAddress getRemoteSocketAddress() {
            return delegate.getRemoteSocketAddress();
        }

        @Override public boolean isBound() {
            return delegate.isBound();
        }

        @Override public boolean isConnected() {
            return delegate.isConnected();
        }

        @Override public boolean isClosed() {
            return delegate.isClosed();
        }

        @Override public void bind(SocketAddress localAddr) throws IOException {
            delegate.bind(localAddr);
        }

        @Override public void connect(SocketAddress remoteAddr) throws IOException {
            delegate.connect(remoteAddr);
        }

        @Override public void connect(SocketAddress remoteAddr, int timeout) throws IOException {
            delegate.connect(remoteAddr, timeout);
        }

        @Override public boolean isInputShutdown() {
            return delegate.isInputShutdown();
        }

        @Override public boolean isOutputShutdown() {
            return delegate.isOutputShutdown();
        }

        @Override public void setReuseAddress(boolean reuse) throws SocketException {
            delegate.setReuseAddress(reuse);
        }

        @Override public boolean getReuseAddress() throws SocketException {
            return delegate.getReuseAddress();
        }

        @Override public void setOOBInline(boolean oobinline) throws SocketException {
            delegate.setOOBInline(oobinline);
        }

        @Override public boolean getOOBInline() throws SocketException {
            return delegate.getOOBInline();
        }

        @Override public void setTrafficClass(int value) throws SocketException {
            delegate.setTrafficClass(value);
        }

        @Override public int getTrafficClass() throws SocketException {
            return delegate.getTrafficClass();
        }

        @Override public void sendUrgentData(int value) throws IOException {
            delegate.sendUrgentData(value);
        }

        @Override public SocketChannel getChannel() {
            return delegate.getChannel();
        }

        @Override public void setPerformancePreferences(int connectionTime, int latency,
                int bandwidth) {
            delegate.setPerformancePreferences(connectionTime, latency, bandwidth);
        }
    }

    /**
     * An SSLSocketFactory that delegates calls. It keeps a record of any sockets created.
     * If {@link #disableTlsFallbackScsv} is set to {@code true} then sockets created by the
     * delegate are wrapped with ones that will not accept the {@link #TLS_FALLBACK_SCSV} cipher,
     * thus bypassing server-side fallback checks on platforms that support it. Unfortunately this
     * wrapping will disable any reflection-based calls to SSLSocket from Platform.
     */
    private static class FallbackTestClientSocketFactory extends DelegatingSSLSocketFactory {
        /**
         * The cipher suite used during TLS connection fallback to indicate a fallback.
         * See https://tools.ietf.org/html/draft-ietf-tls-downgrade-scsv-00
         */
        public static final String TLS_FALLBACK_SCSV = "TLS_FALLBACK_SCSV";

        private final boolean disableTlsFallbackScsv;
        private final List<SSLSocket> createdSockets = new ArrayList<SSLSocket>();

        public FallbackTestClientSocketFactory(SSLSocketFactory delegate,
                boolean disableTlsFallbackScsv) {
            super(delegate);
            this.disableTlsFallbackScsv = disableTlsFallbackScsv;
        }

        @Override public SSLSocket createSocket(Socket s, String host, int port, boolean autoClose)
                throws IOException {
            SSLSocket socket = super.createSocket(s, host, port, autoClose);
            if (disableTlsFallbackScsv) {
                socket = new TlsFallbackDisabledScsvSSLSocket(socket);
            }
            createdSockets.add(socket);
            return socket;
        }

        @Override public SSLSocket createSocket() throws IOException {
            SSLSocket socket = super.createSocket();
            if (disableTlsFallbackScsv) {
                socket = new TlsFallbackDisabledScsvSSLSocket(socket);
            }
            createdSockets.add(socket);
            return socket;
        }

        @Override public SSLSocket createSocket(String host,int port) throws IOException {
            SSLSocket socket = super.createSocket(host, port);
            if (disableTlsFallbackScsv) {
                socket = new TlsFallbackDisabledScsvSSLSocket(socket);
            }
            createdSockets.add(socket);
            return socket;
        }

        @Override public SSLSocket createSocket(String host,int port, InetAddress localHost,
                int localPort) throws IOException {
            SSLSocket socket = super.createSocket(host, port, localHost, localPort);
            if (disableTlsFallbackScsv) {
                socket = new TlsFallbackDisabledScsvSSLSocket(socket);
            }
            createdSockets.add(socket);
            return socket;
        }

        @Override public SSLSocket createSocket(InetAddress host,int port) throws IOException {
            SSLSocket socket = super.createSocket(host, port);
            if (disableTlsFallbackScsv) {
                socket = new TlsFallbackDisabledScsvSSLSocket(socket);
            }
            createdSockets.add(socket);
            return socket;
        }

        @Override public SSLSocket createSocket(InetAddress address,int port,
                InetAddress localAddress, int localPort) throws IOException {
            SSLSocket socket = super.createSocket(address, port, localAddress, localPort);
            if (disableTlsFallbackScsv) {
                socket = new TlsFallbackDisabledScsvSSLSocket(socket);
            }
            createdSockets.add(socket);
            return socket;
        }

        public List<SSLSocket> getCreatedSockets() {
            return createdSockets;
        }
    }

    private static class TlsFallbackDisabledScsvSSLSocket extends DelegatingSSLSocket {

        private boolean tlsFallbackScsvSet;

        public TlsFallbackDisabledScsvSSLSocket(SSLSocket socket) {
            super(socket);
        }

        @Override public void setEnabledCipherSuites(String[] suites) {
            List<String> enabledCipherSuites = new ArrayList<String>(suites.length);
            for (String suite : suites) {
                if (suite.equals(FallbackTestClientSocketFactory.TLS_FALLBACK_SCSV)) {
                    // Record that an attempt was made to set TLS_FALLBACK_SCSV, but don't actually
                    // set it.
                    tlsFallbackScsvSet = true;
                } else {
                    enabledCipherSuites.add(suite);
                }
            }
            delegate.setEnabledCipherSuites(
                    enabledCipherSuites.toArray(new String[enabledCipherSuites.size()]));
        }

        public boolean wasTlsFallbackScsvSet() {
            return tlsFallbackScsvSet;
        }
    }
}
