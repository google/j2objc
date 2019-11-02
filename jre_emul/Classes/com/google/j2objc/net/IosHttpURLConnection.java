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

package com.google.j2objc.net;

import com.google.j2objc.io.AsyncPipedNSInputStreamAdapter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/*-[
#include "NSDataInputStream.h"
#include "NSDataOutputStream.h"
#include "NSDictionaryMap.h"
#include "com/google/j2objc/net/NSErrorException.h"
#include "java/lang/Double.h"
#include "java/net/ConnectException.h"
#include "java/net/HttpCookie.h"
#include "java/net/MalformedURLException.h"
#include "java/net/UnknownHostException.h"
#include "java/net/SocketTimeoutException.h"
#include "java/util/logging/Level.h"
#include "java/util/logging/Logger.h"
]-*/

/**
 * HttpURLConnection implementation for iOS, using NSURLSession.
 *
 * @author Tom Ball
 */
public class IosHttpURLConnection extends HttpURLConnection {

  /** Represents the current NSURLSessionDataTask. Guarded by nativeDataTaskLock. */
  private Object nativeDataTask; // NSURLSessionDataTask.

  /** Used to guard the release of the data task. */
  private final Object nativeDataTaskLock = new Object();

  /**
   * If chunked transfer is not enabled, the OutputStream we offer to the writer is just an,
   * NSDataOutputStream that accumulates bytes until the stream is closed.
   */
  private OutputStream nativeDataOutputStream; // NSDataOutputStream.

  /**
   * If chunked transfer is enabled, we need to give the writer an OutputStream backed by a queue,
   * and then pipe that stream to nativePipedRequestStream.
   */
  private DataEnqueuedOutputStream requestStream;

  /** The NSInputStream from AsyncPipedNSInputStreamAdapter. */
  private Object nativePipedRequestStream; // NSInputStream.

  /** An InputStream for reading the response body. Guarded by responseBodyStreamLock. */
  private DataEnqueuedInputStream responseBodyStream;

  /** Used to guard responseBodyStream. */
  private final Object responseBodyStreamLock = new Object();

  /** An InputStream for reading the error response. */
  private InputStream errorDataStream;

  private List<HeaderEntry> headers = new ArrayList<HeaderEntry>();

  // Cache response failure, so multiple requests to a bad response throw the same exception.
  private IOException responseException;

  /** Guards both responseCode and responseException and is used to block getResponse(). */
  private final Object getResponseLock = new Object();

  // Delegate to handle native security data, to avoid a direct dependency on jre_security.
  private final SecurityDataHandler securityDataHandler;

  private static final int NATIVE_PIPED_STREAM_BUFFER_SIZE = 8192;
  private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
  private static final Map<Integer,String> RESPONSE_CODES = new HashMap<Integer,String>();

  // A case-insensitive comparator that supports a null key, so headers with
  // keys that only differ by case can be coalesced using a TreeMap.
  private static final Comparator<String> HEADER_KEY_COMPARATOR =
      new Comparator<String>() {
        @Override
        public int compare(String lhs, String rhs) {
          if (lhs == null || rhs == null) {
            if (rhs != null) {
              return -1;
            }
            if (lhs != null) {
              return 1;
            }
            return 0;
          }
          return String.CASE_INSENSITIVE_ORDER.compare(lhs, rhs);
        }
      };

  public IosHttpURLConnection(URL url) {
    this(url, null);
  }

  IosHttpURLConnection(URL url, SecurityDataHandler handler) {
    super(url);
    securityDataHandler = handler;
  }

  @Override
  public void disconnect() {
    connected = false;

    // Cancel the data task if it's still running.
    cancelDataTask();

    synchronized (responseBodyStreamLock) {
      if (responseBodyStream != null) {
        // Close the responseBodyStream from the offering side if it still exists.
        responseBodyStream.endOffering();
      }
      responseBodyStream = null;
    }

    nativeDataOutputStream = null;
    requestStream = null;
    nativePipedRequestStream = null;
    errorDataStream = null;

    // Unblock any remaining pending getResponse(), if for any reason cancelDataTask() fails.
    synchronized (getResponseLock) {
      responseException = null;
      getResponseLock.notifyAll();
    }
  }

  /**
   * Enable chunked streaming mode. The chunk length doesn't matter since NSURLSession does not give
   * us control over the chunk size.
   *
   * @param chunklen ignored.
   */
  @Override
  public void setChunkedStreamingMode(int chunklen) {
    super.setChunkedStreamingMode(chunklen);
  }

  /**
   * This method has no effect on iOS. NSURLSession only sends the Content-Length header field is
   * the HTTP request body is a data or an NSInputStream from a file (which we don't support), and
   * it always removes the Content-Length and sets Transfer-Encoding to chunked if a generic stream
   * is supplied as the request body.
   *
   * <p>The implication here is that you should never use this method. If you use this method to
   * attempt to send 5 MB of data to the OutputStream you obtain from {@link #getOutputStream()}, at
   * least 5 MB of data (if not more) will have to be allocated just to be able to send it with the
   * Content-Length field filled. Instead, call {@link #setChunkedStreamingMode(int)} and use the
   * chunked streaming mode, although this also means your server has to have chunked transfer
   * encoding support.
   *
   * @param contentLength ignored.
   */
  @Override
  public void setFixedLengthStreamingMode(long contentLength) {
    super.setFixedLengthStreamingMode(contentLength);
  }

  @Override
  public boolean usingProxy() {
    return false;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if (!doInput) {
      throw new ProtocolException("This protocol does not support input");
    }
    getResponse();
    if (responseCode >= HTTP_BAD_REQUEST) {
      throw new FileNotFoundException(url.toString());
    }
    return responseBodyStream;
  }

  @Override
  public void connect() throws IOException {
    if (responseCode != -1) {
      // Request already made.
      return;
    }

    if (responseException != null) {
      throw responseException;
    }

    if (connected) {
      return;
    }

    connected = true;

    int timeout = getReadTimeout();
    responseBodyStream = new DataEnqueuedInputStream(timeout > 0 ? timeout : -1);
    loadRequestCookies();
    makeRequest();
  }

  @Override
  public Map<String, List<String>> getHeaderFields() {
    try {
      getResponse();
      return getHeaderFieldsDoNotForceResponse();
    } catch (IOException e) {
      return Collections.emptyMap();
    }
  }

  private Map<String, List<String>> getHeaderFieldsDoNotForceResponse() {
    Map<String, List<String>> map = new TreeMap<String, List<String>>(HEADER_KEY_COMPARATOR);
    for (HeaderEntry entry : headers) {
      String k = entry.getKey();
      String v = entry.getValue();
      List<String> values = map.get(k);
      if (values == null) {
        values = new ArrayList<String>();
        map.put(k, values);
      }
      values.add(v);
    }
    return Collections.unmodifiableMap(map);
  }

  private List<HeaderEntry> getResponseHeaders() throws IOException {
    getResponse();
    return headers;
  }

  @Override
  public String getHeaderField(int pos) {
    try {
      List<HeaderEntry> headers = getResponseHeaders();
      return pos < headers.size() ? headers.get(pos).getValue() : null;
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Returns the value of the named header field.
   *
   * If called on a connection that sets the same header multiple times with
   * possibly different values, only the last value is returned.
   */
  @Override
  public String getHeaderField(String key) {
    try {
      getResponse();
      return getHeaderFieldDoNotForceResponse(key);
    } catch(IOException e) {
      return null;
    }
  }

  private String getHeaderFieldDoNotForceResponse(String key) {
    for (int i = headers.size() - 1; i >= 0; i--) {
      HeaderEntry entry = headers.get(i);
      if (key == null) {
        if (entry.getKey() == null) {
          return entry.getValue();
        }
        continue;
      }
      if (entry.getKey() != null && key.equalsIgnoreCase(entry.getKey())) {
        return entry.getValue();
      }
    }
    return null;
  }

  @Override
  public long getHeaderFieldDate(String field, long defaultValue) {
    String dateString = getHeaderField(field);
    try {
      SimpleDateFormat format = new SimpleDateFormat(HTTP_DATE_FORMAT);
      Date d = format.parse(dateString);
      return d.getTime();
    } catch (ParseException e) {
      return defaultValue;
    }
  }

  @Override
  public String getHeaderFieldKey(int posn) {
    try {
      List<HeaderEntry> headers = getResponseHeaders();
      return posn < headers.size() ? headers.get(posn).getKey() : null;
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public int getHeaderFieldInt(String field, int defaultValue) {
    String intString = getHeaderField(field);
    try {
      return Integer.parseInt(intString);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  @Override
  public long getHeaderFieldLong(String field, long defaultValue) {
    String longString = getHeaderField(field);
    return headerValueToLong(longString, defaultValue);
  }

  private long getHeaderFieldLongDoNotForceResponse(String field, long defaultValue) {
    String longString = getHeaderFieldDoNotForceResponse(field);
    return headerValueToLong(longString, defaultValue);
  }

  private long headerValueToLong(String value, long defaultValue) {
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  @Override
  public final Map<String, List<String>> getRequestProperties() {
    if (connected) {
      throw new IllegalStateException(
          "Cannot access request header fields after connection is set");
    }
    return getHeaderFieldsDoNotForceResponse();
  }

  @Override
  public void setRequestProperty(String field, String newValue) {
    if (connected) {
      throw new IllegalStateException("Cannot set request property after connection is made");
    }
    if (field == null) {
      throw new NullPointerException("field == null");
    }
    setHeader(field, newValue);
  }

  @Override
  public void addRequestProperty(String field, String newValue) {
    if (connected) {
      throw new IllegalStateException("Cannot add request property after connection is made");
    }
    if (field == null) {
      throw new NullPointerException("field == null");
    }
    addHeader(field, newValue);
  }

  @Override
  public String getRequestProperty(String field) {
    if (field == null) {
      return null;
    }
    return getHeaderFieldDoNotForceResponse(field);
  }

  @Override
  public InputStream getErrorStream() {
    return errorDataStream;
  }

  @Override
  public long getIfModifiedSince() {
    return getHeaderFieldLongDoNotForceResponse("If-Modified-Since", 0L);
  }

  @Override
  public void setIfModifiedSince(long newValue) {
    super.setIfModifiedSince(newValue);
    if (ifModifiedSince != 0) {
      SimpleDateFormat format = new SimpleDateFormat(HTTP_DATE_FORMAT);
      String dateString = format.format(new Date(ifModifiedSince));
      setHeader("If-Modified-Since", dateString);
    } else {
      removeHeader("If-Modified-Since");
    }
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    if (connected) {
      throw new IllegalStateException("Cannot get output stream after connection is made");
    }

    // If {@link java.net.HttpURLConnection#setChunkedStreamingMode(int)} is ever called to enable
    // chunked streaming mode, create a queue-backed OutputStream and pipe that to a native
    // NSInputStream, which we can then use as the HTTP request body stream.
    if (chunkLength > 0) {
      if (requestStream == null) {
        int timeout = getReadTimeout();
        requestStream = new DataEnqueuedOutputStream(timeout > 0 ? timeout : -1);
      }

      if (nativePipedRequestStream == null) {
        nativePipedRequestStream =
            AsyncPipedNSInputStreamAdapter.create(requestStream, NATIVE_PIPED_STREAM_BUFFER_SIZE);
      }
      connect();
      return requestStream;
    } else {
      if (nativeDataOutputStream == null) {
        nativeDataOutputStream = createNativeDataOutputStream();
      }
      return nativeDataOutputStream;
    }
  }

  private native OutputStream createNativeDataOutputStream() /*-[
    return (JavaIoOutputStream *) [NSDataOutputStream stream];
  ]-*/;

  private void getResponse() throws IOException {
    if (responseCode != -1) {
      // Request already made.
      return;
    }

    connect();
    synchronized (getResponseLock) {
      if (responseCode == -1 && responseException == null) {
        try {
          // There are three places where getResponseLock.notifyAll() is called: in disconnect(), in
          // the native -URLSession:dataTask:didReceiveResponse:, and in the native
          // -URLSession:task:didCompleteWithError:. The call in disconnect() is just a clean-up
          // step, where as -URLSession:dataTask:didReceiveResponse: is called only if the
          // connection is made and an HTTP response code is received. If a non-server error occurs
          // (such as lost connection), -URLSession:task:didCompleteWithError: will be called and an
          // exception will be set there. So the only condition we are waiting for here is if both
          // responseCode and responseExecption are not set.
          getResponseLock.wait();
        } catch (InterruptedException e) {
          // Ignored.
        }
      }
    }
    if (responseException != null) {
      throw responseException;
    }
    saveResponseCookies();
  }

  /**
   * Add any cookies for this URI to the request headers.
   */
  private void loadRequestCookies() throws IOException {
    CookieHandler cookieHandler = CookieHandler.getDefault();
    if (cookieHandler != null) {
      try {
        URI uri = getURL().toURI();
        Map<String, List<String>> cookieHeaders =
            cookieHandler.get(uri, getHeaderFieldsDoNotForceResponse());
        for (Map.Entry<String, List<String>> entry : cookieHeaders.entrySet()) {
          String key = entry.getKey();
          if (("Cookie".equalsIgnoreCase(key)
              || "Cookie2".equalsIgnoreCase(key))
              && !entry.getValue().isEmpty()) {
            List<String> cookies = entry.getValue();
            StringBuilder sb = new StringBuilder();
            for (int i = 0, size = cookies.size(); i < size; i++) {
              if (i > 0) {
                sb.append("; ");
              }
              sb.append(cookies.get(i));
            }
            setHeader(key, sb.toString());
          }
        }
      } catch (URISyntaxException e) {
        throw new IOException(e);
      }
    }
  }

  /**
   * Store any returned cookies.
   */
  private void saveResponseCookies() throws IOException {
    saveResponseCookies(getURL(), getHeaderFieldsDoNotForceResponse());
  }

  private static void saveResponseCookies(URL url, Map<String, List<String>> headerFields)
      throws IOException {
    CookieHandler cookieHandler = CookieHandler.getDefault();
    if (cookieHandler != null) {
      try {
        URI uri = url.toURI();
        cookieHandler.put(uri, headerFields);
      } catch (URISyntaxException e) {
        throw new IOException(e);
      }
    }
  }

  @Override
  public int getResponseCode() throws IOException {
    getResponse();
    return responseCode;
  }

  private native void makeRequest() throws IOException /*-[
    @autoreleasepool {

      NSMutableURLRequest *request =
          [NSMutableURLRequest requestWithURL:[NSURL URLWithString:[self->url_ toExternalForm]]];
      request.HTTPShouldHandleCookies = false;
      request.cachePolicy = self->useCaches_ ?
          NSURLRequestUseProtocolCachePolicy : NSURLRequestReloadIgnoringLocalCacheData;
      int readTimeout = [self getReadTimeout];
      request.timeoutInterval = readTimeout > 0 ? (readTimeout / 1000.0) : JavaLangDouble_MAX_VALUE;
      int n = [self->headers_ size];
      for (int i = 0; i < n; i++) {
        ComGoogleJ2objcNetIosHttpURLConnection_HeaderEntry *entry = [self->headers_ getWithInt:i];
        if (entry->key_) {
          [request setValue:[entry getValue] forHTTPHeaderField:entry->key_];
        }
      }

      if (self->doOutput_) {
        if ([self->method_ isEqualToString:@"GET"]) {
          self->method_ = @"POST";  // GET doesn't support output, so assume POST.
        } else if (![self->method_ isEqualToString:@"POST"] &&
                   ![self->method_ isEqualToString:@"PUT"] &&
                   ![self->method_ isEqualToString:@"PATCH"]) {
          NSString *errMsg =
              [NSString stringWithFormat:@"%@ does not support writing", self->method_];
          self->responseException_ = [[JavaNetProtocolException alloc] initWithNSString:errMsg];
          @throw self->responseException_;
        }

        if (self->nativeDataOutputStream_) {
          // Use the accumululated data as the request body.
          request.HTTPBody = [(NSDataOutputStream *) self->nativeDataOutputStream_ data];
        } else if (self->nativePipedRequestStream_) {
          // Use the piped NSInputStream as the request stream.
          request.HTTPBodyStream = self->nativePipedRequestStream_;
        }
      }
      request.HTTPMethod = self->method_;

      NSURLSessionConfiguration *sessionConfiguration =
          [NSURLSessionConfiguration defaultSessionConfiguration];
      NSURLSession *session =
          [NSURLSession sessionWithConfiguration:sessionConfiguration
                                        delegate:(id<NSURLSessionDataDelegate>)self
                                   delegateQueue:nil];
      NSURLSessionTask *task = [session dataTaskWithRequest:request];
      [task resume];
      JreStrongAssign(&self->nativeDataTask_, task);
      [session finishTasksAndInvalidate];
    }
  ]-*/;

  /*
   * NSURLSessionDataDelegate method: initial reply received.
   */
  /*-[
  - (void)URLSession:(NSURLSession *)session
            dataTask:(NSURLSessionDataTask *)dataTask
  didReceiveResponse:(NSURLResponse *)urlResponse
   completionHandler:(void (^)(NSURLSessionResponseDisposition disposition))completionHandler {
    if (urlResponse && ![urlResponse isKindOfClass:[NSHTTPURLResponse class]]) {
      @throw AUTORELEASE(([[JavaLangAssertionError alloc]
                           initWithId:[NSString stringWithFormat:@"Unknown class %@",
                               NSStringFromClass([urlResponse class])]]));
    }
    NSHTTPURLResponse *response = (NSHTTPURLResponse *) urlResponse;
    int responseCode = (int) response.statusCode;
    JavaNetHttpURLConnection_set_responseMessage_(self,
        ComGoogleJ2objcNetIosHttpURLConnection_getResponseStatusTextWithInt_(responseCode));

    // Clear request headers to make room for the response headers.
    [self->headers_ clear];

    // The HttpURLConnection headerFields map uses a null key for Status-Line.
    NSString *statusLine = [NSString stringWithFormat:@"HTTP/1.1 %d %@", responseCode,
        self->responseMessage_];
    ComGoogleJ2objcNetIosHttpURLConnection_addHeaderWithNSString_withNSString_(
        self, nil, statusLine);

    // Copy remaining response headers.
    [response.allHeaderFields enumerateKeysAndObjectsUsingBlock:
        ^(id key, id value, BOOL *stop) {
      ComGoogleJ2objcNetIosHttpURLConnection_addHeaderWithNSString_withNSString_(self, key, value);
    }];

    if (response.statusCode >= JavaNetHttpURLConnection_HTTP_BAD_REQUEST) {
      // Make errorDataStream an alias to responseBodyStream. Since getInputStream() throws an
      // exception when status code >= HTTP_BAD_REQUEST, it is guaranteed that responseBodyStream
      // can only mean error stream going forward.
      JreStrongAssign(&self->errorDataStream_, self->responseBodyStream_);
    }

    completionHandler(NSURLSessionResponseAllow);

    // Since the original request might have been redirected, we might need to
    // update the URL to the redirected URL.
    JavaNetURLConnection_set_url_(
        self, create_JavaNetURL_initWithNSString_(response.URL.absoluteString));

    // Unblock getResponse().
    @synchronized(getResponseLock_) {
      self->responseCode_ = responseCode;
      [self->getResponseLock_ java_notifyAll];
    }
  }
  ]-*/

  /*
   * NSURLSessionDataDelegate method: data received.
   */
  /*-[
  - (void)URLSession:(NSURLSession *)session
            dataTask:(NSURLSessionDataTask *)dataTask
      didReceiveData:(NSData *)data {
    @synchronized(responseBodyStreamLock_) {
      [self->responseBodyStream_ offerDataWithByteArray:[IOSByteArray arrayWithNSData:data]];
    }
  }
  ]-*/

  /*
   * NSURLSessionDataDelegate method: should we store the response in the cache.
   */
  /*-[
  - (void)URLSession:(NSURLSession *)session
            dataTask:(NSURLSessionDataTask *)dataTask
   willCacheResponse:(NSCachedURLResponse *)proposedResponse
   completionHandler:(void (^)(NSCachedURLResponse *cachedResponse))completionHandler {
    completionHandler( self->useCaches_ ? proposedResponse : nil );
  }
  ]-*/


  /*
   * NSURLSessionDelegate method: task completed.
   */
  /*-[
  - (void)URLSession:(NSURLSession *)session
                task:(NSURLSessionTask *)task
didCompleteWithError:(NSError *)error {
    JavaIoIOException *responseException = nil;
    if (error) {
      NSString *url = [self->url_ description];  // Use original URL in any error text.
      if ([[error domain] isEqualToString:@"NSURLErrorDomain"]) {
        switch ([error code]) {
          case NSURLErrorBadURL:
            responseException = create_JavaNetMalformedURLException_initWithNSString_(url);
            break;
          case NSURLErrorCannotConnectToHost:
            responseException =
                create_JavaNetConnectException_initWithNSString_([error description]);
            break;
          case NSURLErrorSecureConnectionFailed:
            responseException = RETAIN_(
                ComGoogleJ2objcNetIosHttpURLConnection_secureConnectionExceptionWithNSString_
                    ([error description]));
            break;
          case NSURLErrorCannotFindHost:
            responseException = create_JavaNetUnknownHostException_initWithNSString_(url);
            break;
          case NSURLErrorTimedOut:
            responseException = create_JavaNetSocketTimeoutException_initWithNSString_(url);
            break;
        }
      }
      if (!responseException) {
        responseException = create_JavaIoIOException_initWithNSString_([error description]);
      }
      ComGoogleJ2objcNetNSErrorException *cause =
          create_ComGoogleJ2objcNetNSErrorException_initWithId_(error);
      [responseException initCauseWithJavaLangThrowable:cause];
    }

    @synchronized(responseBodyStreamLock_) {
      if (!responseException) {
        // No error, close the responseBodyStream.
        [self->responseBodyStream_ endOffering];
      } else {
        // Close responseBodyStream with the exception so that subsequent calls to read() cause the
        // same exception to be thrown.
        [self->responseBodyStream_ endOfferingWithJavaIoIOException:responseException];
      }
    }

    // Set nativeDataTask to null.
    @synchronized(nativeDataTaskLock_) {
      JreStrongAssign(&self->nativeDataTask_, nil);
    }

    // Unblock getResponse() and set responseException. This call to notifyAll() is needed because
    // -URLSession:dataTask:didReceiveResponse: may not be called if a non-server error (such as
    // lost connection) occurs.
    @synchronized(getResponseLock_) {
      JreStrongAssign(&self->responseException_, responseException);
      [self->getResponseLock_ java_notifyAll];
    }
  }
  ]-*/

  /*
   * NSURLSessionDelegate method: session completed.
   */
  /*-[
  - (void)URLSession:(NSURLSession *)session didBecomeInvalidWithError:(NSError *)error {
    if (error) {
      // Cannot return error since this happened on another thread and the task
      // finished, so just log it.
      ComGoogleJ2objcNetNSErrorException *exception =
          create_ComGoogleJ2objcNetNSErrorException_initWithId_(error);
      JavaUtilLoggingLogger *logger = JavaUtilLoggingLogger_getLoggerWithNSString_(
          [[self java_getClass] getName]);
      [logger logWithJavaUtilLoggingLevel:JreLoadStatic(JavaUtilLoggingLevel, SEVERE)
                             withNSString:@"session invalidated with error"
                    withJavaLangThrowable:exception];
    }
  }
  ]-*/

  /**
   * Returns an SSLException if that class is linked into the application,
   * otherwise IOException.
   */
  static IOException secureConnectionException(String description) {
    try {
      Class<?> sslExceptionClass = Class.forName("javax.net.ssl.SSLException");
      Constructor<?> constructor = sslExceptionClass.getConstructor(String.class);
      return (IOException) constructor.newInstance(description);
    } catch (ClassNotFoundException e) {
      return new IOException(description);
    } catch (Exception e) {
      throw new AssertionError("unexpected exception", e);
    }
  }

  /*-[
  - (void)URLSession:(NSURLSession *)session
                task:(NSURLSessionTask *)task
willPerformHTTPRedirection:(NSHTTPURLResponse *)response
          newRequest:(NSURLRequest *)request
   completionHandler:(void (^)(NSURLRequest *))completionHandler {
    if (self->instanceFollowRedirects_
        && [response.URL.scheme isEqualToString:request.URL.scheme]) {
      // Workaround for iOS bug (https://forums.developer.apple.com/thread/43818).
      NSMutableURLRequest *nextRequest = [request mutableCopy];

      NSString *responseCookies = [response.allHeaderFields objectForKey:@"Set-Cookie"];
      if (responseCookies) {
        // Parse cookies and add each of them to iOS request.
        id<JavaUtilList> cookies = [JavaNetHttpCookie parseWithNSString:responseCookies];
        for (jint i = 0; i < [cookies size]; i++) {
          JavaNetHttpCookie *cookie = [cookies getWithInt:i];
          [nextRequest addValue:[cookie description] forHTTPHeaderField:@"Cookie"];
        }

        // Add cookies to Java cookie handler.
        id<JavaUtilMap> headerMap =
            ComGoogleJ2objcNetIosHttpURLConnection_makeSetCookieHeaderMapWithNSString_(
                responseCookies);
        JavaNetURL *redirectURL = [[JavaNetURL alloc] initWithNSString:[response.URL description]];
        ComGoogleJ2objcNetIosHttpURLConnection_saveResponseCookiesWithJavaNetURL_withJavaUtilMap_(
            redirectURL, headerMap);
        [redirectURL release];
      }
      completionHandler(nextRequest);
    } else {
      completionHandler(nil);
    }
  }
  ]-*/

  private static Map<String, List<String>> makeSetCookieHeaderMap(String cookieHeaderValue) {
    ArrayList<String> values = new ArrayList<>();
    values.add(cookieHeaderValue);
    HashMap<String, List<String>> map = new HashMap<>();
    map.put("Set-Cookie", values);
    return map;
  }

  private void addHeader(String k, String v) {
    if (k != null && (k.equalsIgnoreCase("Set-Cookie") || k.equalsIgnoreCase("Set-Cookie2"))) {
      CookieSplitter cs = new CookieSplitter(v);
      while (cs.hasNext()) {
        headers.add(new HeaderEntry(k, cs.next()));
      }
    } else {
      headers.add(new HeaderEntry(k, v));
    }
  }

  private void removeHeader(String k) {
    Iterator<HeaderEntry> iter = headers.iterator();
    while (iter.hasNext()) {
      HeaderEntry entry = iter.next();
      if (entry.key == k) {
        iter.remove();
        return;
      }
    }
  }

  private void setHeader(String k, String v) {
    for (HeaderEntry entry : headers) {
      if (entry.key == k || (entry.key != null && k != null && k.equalsIgnoreCase(entry.key))) {
        headers.remove(entry);
        break;
      }
    }
    headers.add(new HeaderEntry(k, v));
  }

  private static class HeaderEntry implements Map.Entry<String, String> {
    private final String key;
    private final String value;

    HeaderEntry(String k, String v) {
      this.key = k;
      this.value = v;
    }

    @Override
    public String getKey() {
      return key;
    }

    @Override
    public String getValue() {
      return value;
    }

    @Override
    public String setValue(String object) {
      throw new AssertionError("mutable method called on immutable class");
    }
  }

  private static String getResponseStatusText(int responseCode) {
    return RESPONSE_CODES.get(responseCode);
  }

  /** Cancels the native data task. */
  private native void cancelDataTask() /*-[
    @synchronized (self->nativeDataTaskLock_) {
      // Safe to do even if self->nativeDataTask_ is already nil.
      [(NSURLSessionDataTask *)self->nativeDataTask_ cancel];
      JreStrongAssign(&self->nativeDataTask_, nil);
    }
  ]-*/;

  /**
   * Splits a string with one or more cookies separated with a comma. Since
   * some attribute values can also have commas, like the expires date field,
   * this does a look ahead to detect where it's a separator or not.
   */
  private static class CookieSplitter {
    String s;
    char[] buf;
    int pos;

    CookieSplitter(String s) {
      this.s = s;
      buf = s.toCharArray();
      pos = 0;
    }

    boolean hasNext() {
      return pos < buf.length;
    }

    String next() {
      int start = pos;
      while (skipSpace()) {
        if (buf[pos] == ',') {
          int lastComma = pos++;
          skipSpace();
          int nextStart = pos;
          while (pos < buf.length && buf[pos] != '=' && buf[pos] != ';' && buf[pos] != ',') {
            pos++;
          }
          if (pos < buf.length && buf[pos] == '=') {
            // pos is inside the next cookie, so back up and return it.
            pos = nextStart;
            return s.substring(start, lastComma);
          }
          pos = lastComma;
        }
        pos++;
      }
      return s.substring(start);
    }

    // Skip whitespace, returning true if there are more chars to read.
    private boolean skipSpace() {
      while (pos < buf.length && Character.isWhitespace(buf[pos])) {
        pos++;
      }
      return pos < buf.length;
    }
  }

  static {
    RESPONSE_CODES.put(100, "Continue");
    RESPONSE_CODES.put(101, "Switching Protocols");
    RESPONSE_CODES.put(102, "Processing");
    RESPONSE_CODES.put(HTTP_OK, "OK");
    RESPONSE_CODES.put(HTTP_CREATED, "Created");
    RESPONSE_CODES.put(HTTP_ACCEPTED, "Accepted");
    RESPONSE_CODES.put(HTTP_NOT_AUTHORITATIVE, "Non Authoritative Information");
    RESPONSE_CODES.put(HTTP_NO_CONTENT, "No Content");
    RESPONSE_CODES.put(HTTP_RESET, "Reset Content");
    RESPONSE_CODES.put(HTTP_PARTIAL, "Partial Content");
    RESPONSE_CODES.put(207, "Multi-Status");
    RESPONSE_CODES.put(HTTP_MULT_CHOICE, "Multiple Choices");
    RESPONSE_CODES.put(HTTP_MOVED_PERM, "Moved Permanently");
    RESPONSE_CODES.put(HTTP_MOVED_TEMP, "Moved Temporarily");
    RESPONSE_CODES.put(HTTP_SEE_OTHER, "See Other");
    RESPONSE_CODES.put(HTTP_NOT_MODIFIED, "Not Modified");
    RESPONSE_CODES.put(HTTP_USE_PROXY, "Use Proxy");
    RESPONSE_CODES.put(307, "Temporary Redirect");
    RESPONSE_CODES.put(HTTP_BAD_REQUEST, "Bad Request");
    RESPONSE_CODES.put(HTTP_UNAUTHORIZED, "Unauthorized");
    RESPONSE_CODES.put(HTTP_PAYMENT_REQUIRED, "Payment Required");
    RESPONSE_CODES.put(HTTP_FORBIDDEN, "Forbidden");
    RESPONSE_CODES.put(HTTP_NOT_FOUND, "Not Found");
    RESPONSE_CODES.put(HTTP_BAD_METHOD, "Method Not Allowed");
    RESPONSE_CODES.put(HTTP_NOT_ACCEPTABLE, "Not Acceptable");
    RESPONSE_CODES.put(HTTP_PROXY_AUTH, "Proxy Authentication Required");
    RESPONSE_CODES.put(HTTP_CLIENT_TIMEOUT, "Request Timeout");
    RESPONSE_CODES.put(HTTP_CONFLICT, "Conflict");
    RESPONSE_CODES.put(HTTP_GONE, "Gone");
    RESPONSE_CODES.put(HTTP_LENGTH_REQUIRED, "Length Required");
    RESPONSE_CODES.put(HTTP_PRECON_FAILED, "Precondition Failed");
    RESPONSE_CODES.put(HTTP_ENTITY_TOO_LARGE, "Request Too Long");
    RESPONSE_CODES.put(HTTP_REQ_TOO_LONG, "Request-URI Too Long");
    RESPONSE_CODES.put(HTTP_UNSUPPORTED_TYPE, "Unsupported Media Type");
    RESPONSE_CODES.put(416, "Requested Range Not Satisfiable");
    RESPONSE_CODES.put(417, "Expectation Failed");
    RESPONSE_CODES.put(418, "Unprocessable Entity");
    RESPONSE_CODES.put(419, "Insufficient Space On Resource");
    RESPONSE_CODES.put(420, "Method Failure");
    RESPONSE_CODES.put(423, "Locked");
    RESPONSE_CODES.put(424, "Failed Dependency");
    RESPONSE_CODES.put(HTTP_INTERNAL_ERROR, "Internal Server Error");
    RESPONSE_CODES.put(HTTP_NOT_IMPLEMENTED, "Not Implemented");
    RESPONSE_CODES.put(HTTP_BAD_GATEWAY, "Bad Gateway");
    RESPONSE_CODES.put(HTTP_UNAVAILABLE, "Service Unavailable");
    RESPONSE_CODES.put(HTTP_GATEWAY_TIMEOUT, "Gateway Timeout");
    RESPONSE_CODES.put(HTTP_VERSION, "Http Version Not Supported");
    RESPONSE_CODES.put(507, "Insufficient Storage");
  }
}
