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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*-[
#include "NSDataInputStream.h"
#include "NSDataOutputStream.h"
#include "NSDictionaryMap.h"
#include "java/lang/Double.h"
#include "java/net/ConnectException.h"
#include "java/net/MalformedURLException.h"
#include "java/net/UnknownHostException.h"
]-*/

/**
 * HttpURLConnection implementation for iOS, using NSURLConnection.
 *
 * @author Tom Ball
 */
public class IosHttpURLConnection extends HttpURLConnection {
  private OutputStream nativeRequestData;
  private InputStream responseDataStream;
  private InputStream errorDataStream;
  private List<HeaderEntry> headers = new ArrayList<HeaderEntry>();
  private int contentLength;

  // Cache response failure, so multiple requests to a bad response throw the same exception.
  private IOException responseException;

  private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
  private static final Map<Integer,String> RESPONSE_CODES = new HashMap<Integer,String>();

  public IosHttpURLConnection(URL url) {
    super(url);
  }

  @Override
  public void disconnect() {
    connected = false;
    nativeRequestData = null;
    responseDataStream = null;
    errorDataStream = null;
    responseException = null;
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
    return responseDataStream;
  }

  @Override
  public void connect() throws IOException {
    connected = true;
    // Native connection created when request is made to server.
  }

  @Override
  public Map<String, List<String>> getHeaderFields() {
    try {
      Map<String, List<String>> map = new HashMap<String, List<String>>();
      for (HeaderEntry entry : getHeaders()) {
        map.put(entry.getKey(), entry.getValue());
      }
      return map;
    } catch (IOException e) {
      return Collections.EMPTY_MAP;
    }
  }

  private List<IosHttpURLConnection.HeaderEntry> getHeaders() throws IOException {
    getResponse();
    return headers;
  }

  @Override
  public String getHeaderField(int pos) {
    try {
      return getHeaders().get(pos).getValueAsString();
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public String getHeaderField(String key) {
    try {
      for (HeaderEntry entry : getHeaders()) {
        if (key == null) {
          if (entry.getKey() == null) {
            return entry.getValueAsString();
          }
          continue;
        }
        if (key.equals(entry.getKey())) {
          return entry.getValueAsString();
        }
      }
      return null;
    } catch (IOException e) {
      return null;
    }
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
      return getHeaders().get(posn).getKey();
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
    try {
      return Long.parseLong(longString);
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
    return getHeaderFields();
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
    return getHeaderField(field);
  }

  @Override
  public InputStream getErrorStream() {
    return errorDataStream;
  }

  @Override
  public long getIfModifiedSince() {
    return getHeaderFieldLong("If-Modified-Since", 0L);
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
  public native OutputStream getOutputStream() throws IOException /*-[
    if (!nativeRequestData_) {  // Don't reallocate if requested twice.
      ComGoogleJ2objcNetIosHttpURLConnection_set_nativeRequestData_(
          self, [NSDataOutputStream stream]);
    }
    return nativeRequestData_;
  ]-*/;

  private void getResponse() throws IOException {
    if (responseCode != -1) {
      // Request already made.
      return;
    }
    makeSynchronousRequest();
  }

  private native void makeSynchronousRequest() throws IOException /*-[
    if (responseCode_ != -1) {
      // Request already made.
      return;
    }
    if (responseException_) {
      @throw responseException_;
    }

    NSMutableURLRequest *request =
        [NSMutableURLRequest requestWithURL:[NSURL URLWithString:[url_ toExternalForm]]];
    request.HTTPMethod = method_;
    request.cachePolicy = useCaches_ ?
        NSURLRequestUseProtocolCachePolicy : NSURLRequestReloadIgnoringLocalCacheData;
    request.timeoutInterval = readTimeout_ > 0 ? (readTimeout_ / 1000.0) : JavaLangDouble_MAX_VALUE;
    int n = [headers_ size];
    for (int i = 0; i < n; i++) {
      ComGoogleJ2objcNetIosHttpURLConnection_HeaderEntry *entry = [headers_ getWithInt:i];
      if (entry->key_) {
        [request setValue:[entry getValueAsString] forHTTPHeaderField:entry->key_];
      }
    }

    if (doOutput_) {
      if ([method_ isEqualToString:@"GET"]) {
        method_ = @"POST";  // GET doesn't support output, so assume POST.
      } else if (![method_ isEqualToString:@"POST"] && ![method_ isEqualToString:@"PUT"] &&
                 ![method_ isEqualToString:@"PATCH"]) {
        NSString *errMsg = [NSString stringWithFormat:@"%@ does not support writing", method_];
        responseException_ = [[JavaNetProtocolException alloc] initWithNSString:errMsg];
        @throw responseException_;
      }
      [request setValue:contentType_ forHTTPHeaderField:@"Content-Type"];
      if (nativeRequestData_) {
        request.HTTPBody = [(NSDataOutputStream *) nativeRequestData_ data];
      }
    }

    NSHTTPURLResponse *response = nil;
    NSError *error = nil;
    NSData *responseData = [NSURLConnection sendSynchronousRequest:request
                                                 returningResponse:&response
                                                             error:&error];
    responseCode_ = response ? [response statusCode] : [error code];
    responseMessage_ =
        [ComGoogleJ2objcNetIosHttpURLConnection getResponseStatusTextWithInt:responseCode_];
    contentLength_ = [responseData length];

    if (error || [response statusCode] >= JavaNetHttpURLConnection_HTTP_BAD_REQUEST) {
      if (responseData) {
        ComGoogleJ2objcNetIosHttpURLConnection_set_errorDataStream_(self,
            [[NSDataInputStream alloc] initWithData:responseData]);
      }
    } else {
      ComGoogleJ2objcNetIosHttpURLConnection_set_responseDataStream_(self,
          [[NSDataInputStream alloc] initWithData:responseData]);
    }

    NSString *url = [url_ description];  // Use original URL in any error text.
    if (error) {
      if ([[error domain] isEqualToString:@"NSURLErrorDomain"]) {
        switch ([error code]) {
          case NSURLErrorBadURL:
            responseException_ = [[JavaNetMalformedURLException alloc] initWithNSString:url]; break;
          case NSURLErrorCannotConnectToHost:
          case NSURLErrorNotConnectedToInternet:
          case NSURLErrorSecureConnectionFailed:
            responseException_ = [[JavaNetConnectException alloc]
                                  initWithNSString:[error description]]; break;
          case NSURLErrorCannotFindHost:
            responseException_ = [[JavaNetUnknownHostException alloc] initWithNSString:url]; break;
        }
      }
      if (!responseException_) {
        responseException_ = [[JavaIoIOException alloc] initWithNSString:[error description]];
      }
      @throw responseException_;
    }

    // The HttpURLConnection headerFields map uses a null key for Status-Line.
    NSString *statusLine =
        [NSString stringWithFormat:@"HTTP/1.1 %d %@", responseCode_, responseMessage_];
    [self addHeaderWithNSString:nil withNSString:statusLine];

    // Copy remaining response headers.
    [response.allHeaderFields enumerateKeysAndObjectsUsingBlock:^(id key, id value, BOOL *stop) {
      [self addHeaderWithNSString:key withNSString:value];
    }];
  ]-*/;

  private void addHeader(String k, String v) {
    headers.add(new HeaderEntry(k, v));
  }

  private List<String> getHeader(String k) {
    for (HeaderEntry entry : headers) {
      if (entry.key == k) {
        return entry.value;
      }
    }
    return null;
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
      if (entry.key == k) {
        for (String entryVal : entry.value) {
          if (entryVal.equals(v)) {
            return;  // already set.
          }
        }
        entry.value.add(v);
      }
    }
  }

  private static class HeaderEntry implements Map.Entry<String, List<String>> {
    private final String key;
    private final List<String> value;

    HeaderEntry(String k, String v) {
      this.key = k;
      this.value = Arrays.asList(v.split("[s,]+"));
    }

    @Override
    public String getKey() {
      return key;
    }

    @Override
    public List<String> getValue() {
      return value;
    }

    @Override
    public List<String> setValue(List<String> object) {
      throw new AssertionError("mutable method called on immutable class");
    }

    public String getValueAsString() {
      if (value.isEmpty()) {
        return "";
      }
      StringBuilder sb = new StringBuilder();
      Iterator<String> iter = value.iterator();
      sb.append(iter.next());
      while (iter.hasNext()) {
        sb.append(", ");
        sb.append(iter.next());
      }
      return sb.toString();
    }
  }

  private static String getResponseStatusText(int responseCode) {
    return RESPONSE_CODES.get(responseCode);
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
