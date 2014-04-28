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

  private static final Map<Integer,String> responseCodes = new HashMap<Integer,String>();

  public IosHttpURLConnection(URL url) {
    super(url);
  }

  @Override
  public void disconnect() {
    connected = false;
    nativeRequestData = null;
    responseDataStream = null;
    errorDataStream = null;
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
    tryConnect();
    if (responseCode >= HTTP_BAD_REQUEST) {
      throw new FileNotFoundException(url.toString());
    }
    return responseDataStream;
  }

  @Override
  public void connect() throws IOException {
    tryConnect();
  }

  @Override
  public Map<String, List<String>> getHeaderFields() {
    try {
      tryConnect();
    } catch (IOException e) {
      return Collections.EMPTY_MAP;
    }
    return copyHeaders();
  }

  private Map<String, List<String>> copyHeaders() {
    Map<String, List<String>> map = new HashMap<String, List<String>>();
    for (HeaderEntry entry : headers) {
      map.put(entry.getKey(), entry.getValue());
    }
    return map;
  }

  @Override
  public String getHeaderField(int pos) {
    return headers.get(pos).getValueAsString();
  }

  @Override
  public String getHeaderField(String key) {
    for (HeaderEntry entry : headers) {
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
  }

  @Override
  public long getHeaderFieldDate(String field, long defaultValue) {
    String dateString = getHeaderField(field);
    try {
      SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
      Date d = format.parse(dateString);
      return d.getTime();
    } catch (ParseException e) {
      return defaultValue;
    }
  }

  @Override
  public String getHeaderFieldKey(int posn) {
    return headers.get(posn).getKey();
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
  public InputStream getErrorStream() {
    return errorDataStream;
  }

  @Override
  public long getIfModifiedSince() {
    return getHeaderFieldLong("If-Modified-Since", 0L);
  }

  @Override
  public native OutputStream getOutputStream() throws IOException /*-[
    if (responseCode_ == -1) {
      @throw AUTORELEASE([[JavaIoIOException alloc] initWithNSString:@"output not enabled"]);
    }
    if (!nativeRequestData_) {  // Don't reallocate if requested twice.
      ComGoogleJ2objcNetIosHttpURLConnection_set_nativeRequestData_(self, nativeRequestData_);
    }
    return nativeRequestData_;
  ]-*/;

  private native void tryConnect() throws IOException /*-[
    if (connected_) {
      return;
    }
    connected_ = YES;

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
        @throw AUTORELEASE([[JavaNetProtocolException alloc] initWithNSString:errMsg]);
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
            @throw AUTORELEASE([[JavaNetMalformedURLException alloc] initWithNSString:url]); break;
          case NSURLErrorCannotConnectToHost:
          case NSURLErrorNotConnectedToInternet:
          case NSURLErrorSecureConnectionFailed:
            @throw AUTORELEASE([[JavaNetConnectException alloc]
                                initWithNSString:[error description]]); break;
          case NSURLErrorCannotFindHost:
            @throw AUTORELEASE([[JavaNetUnknownHostException alloc] initWithNSString:url]); break;
        }
      }
      if (error || !response) {
        @throw AUTORELEASE([[JavaIoIOException alloc] initWithNSString:[error description]]);
      }
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
    return responseCodes.get(responseCode);
  }

  static {
    responseCodes.put(100, "Continue");
    responseCodes.put(101, "Switching Protocols");
    responseCodes.put(102, "Processing");
    responseCodes.put(HTTP_OK, "OK");
    responseCodes.put(HTTP_CREATED, "Created");
    responseCodes.put(HTTP_ACCEPTED, "Accepted");
    responseCodes.put(HTTP_NOT_AUTHORITATIVE, "Non Authoritative Information");
    responseCodes.put(HTTP_NO_CONTENT, "No Content");
    responseCodes.put(HTTP_RESET, "Reset Content");
    responseCodes.put(HTTP_PARTIAL, "Partial Content");
    responseCodes.put(207, "Multi-Status");
    responseCodes.put(HTTP_MULT_CHOICE, "Multiple Choices");
    responseCodes.put(HTTP_MOVED_PERM, "Moved Permanently");
    responseCodes.put(HTTP_MOVED_TEMP, "Moved Temporarily");
    responseCodes.put(HTTP_SEE_OTHER, "See Other");
    responseCodes.put(HTTP_NOT_MODIFIED, "Not Modified");
    responseCodes.put(HTTP_USE_PROXY, "Use Proxy");
    responseCodes.put(307, "Temporary Redirect");
    responseCodes.put(HTTP_BAD_REQUEST, "Bad Request");
    responseCodes.put(HTTP_UNAUTHORIZED, "Unauthorized");
    responseCodes.put(HTTP_PAYMENT_REQUIRED, "Payment Required");
    responseCodes.put(HTTP_FORBIDDEN, "Forbidden");
    responseCodes.put(HTTP_NOT_FOUND, "Not Found");
    responseCodes.put(HTTP_BAD_METHOD, "Method Not Allowed");
    responseCodes.put(HTTP_NOT_ACCEPTABLE, "Not Acceptable");
    responseCodes.put(HTTP_PROXY_AUTH, "Proxy Authentication Required");
    responseCodes.put(HTTP_CLIENT_TIMEOUT, "Request Timeout");
    responseCodes.put(HTTP_CONFLICT, "Conflict");
    responseCodes.put(HTTP_GONE, "Gone");
    responseCodes.put(HTTP_LENGTH_REQUIRED, "Length Required");
    responseCodes.put(HTTP_PRECON_FAILED, "Precondition Failed");
    responseCodes.put(HTTP_ENTITY_TOO_LARGE, "Request Too Long");
    responseCodes.put(HTTP_REQ_TOO_LONG, "Request-URI Too Long");
    responseCodes.put(HTTP_UNSUPPORTED_TYPE, "Unsupported Media Type");
    responseCodes.put(416, "Requested Range Not Satisfiable");
    responseCodes.put(417, "Expectation Failed");
    responseCodes.put(418, "Unprocessable Entity");
    responseCodes.put(419, "Insufficient Space On Resource");
    responseCodes.put(420, "Method Failure");
    responseCodes.put(423, "Locked");
    responseCodes.put(424, "Failed Dependency");
    responseCodes.put(HTTP_INTERNAL_ERROR, "Internal Server Error");
    responseCodes.put(HTTP_NOT_IMPLEMENTED, "Not Implemented");
    responseCodes.put(HTTP_BAD_GATEWAY, "Bad Gateway");
    responseCodes.put(HTTP_UNAVAILABLE, "Service Unavailable");
    responseCodes.put(HTTP_GATEWAY_TIMEOUT, "Gateway Timeout");
    responseCodes.put(HTTP_VERSION, "Http Version Not Supported");
    responseCodes.put(507, "Insufficient Storage");
  }
}
