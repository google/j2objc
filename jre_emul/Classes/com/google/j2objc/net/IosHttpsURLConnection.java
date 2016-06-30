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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.URL;
import java.security.Permission;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * HttpsURLConnection implementation for iOS. Since NSURLSession supports
 * both http and https URLs, this class uses an HttpURLConnection delegate
 * whenever possible.
 */
public class IosHttpsURLConnection extends HttpsURLConnection implements SecurityDataHandler {
  private final IosHttpURLConnection delegate;
  private final List<Certificate> serverCertificates = new ArrayList<>();

  private static final Logger logger = Logger.getLogger(IosHttpsURLConnection.class.getName());

  public IosHttpsURLConnection(URL url) {
    super(url);
    delegate = new IosHttpURLConnection(url, this);
  }

  @Override
  public String getCipherSuite() {
    // TODO(tball): implement
    logger.severe("HttpsURLConnection.getCipherSuite() not implemented");
    return null;
  }

  @Override
  public Certificate[] getLocalCertificates() {
    // TODO(tball): implement
    logger.severe("HttpsURLConnection.getLocalCertificates() not implemented");
    return new Certificate[0];
  }

  @Override
  public void handleSecCertificateData(byte[] secCertData) throws Exception {
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    ByteArrayInputStream certificateInputStream = new ByteArrayInputStream(secCertData);
    Certificate certificate = certificateFactory.generateCertificate(certificateInputStream);
    serverCertificates.add(certificate);
  }

  @Override
  public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
    if (getURL().getHost().startsWith("http://")) {
      throw new SSLPeerUnverifiedException("The http protocol does not support certificates");
    }
    return serverCertificates.isEmpty() ? null
        : serverCertificates.toArray(new Certificate[serverCertificates.size()]);
  }

  // Delegate methods.
  @Override public void connect() throws IOException {
    connected = true;
    delegate.connect();
  }

  @Override public void disconnect() {
    delegate.disconnect();
  }

  @Override public InputStream getErrorStream() {
    return delegate.getErrorStream();
  }

  @Override public String getRequestMethod() {
    return delegate.getRequestMethod();
  }

  @Override public int getResponseCode() throws IOException {
    return delegate.getResponseCode();
  }

  @Override public String getResponseMessage() throws IOException {
    return delegate.getResponseMessage();
  }

  @Override public void setRequestMethod(String method) throws ProtocolException {
    delegate.setRequestMethod(method);
  }

  @Override public boolean usingProxy() {
    return delegate.usingProxy();
  }

  @Override public boolean getInstanceFollowRedirects() {
    return delegate.getInstanceFollowRedirects();
  }

  @Override public void setInstanceFollowRedirects(boolean followRedirects) {
    delegate.setInstanceFollowRedirects(followRedirects);
  }

  @Override public boolean getAllowUserInteraction() {
    return delegate.getAllowUserInteraction();
  }

  @Override public Object getContent() throws IOException {
    return delegate.getContent();
  }

  @SuppressWarnings("rawtypes") // Spec does not generify
  @Override public Object getContent(Class[] types) throws IOException {
    return delegate.getContent(types);
  }

  @Override public String getContentEncoding() {
    return delegate.getContentEncoding();
  }

  @Override public int getContentLength() {
    return delegate.getContentLength();
  }

  @Override public String getContentType() {
    return delegate.getContentType();
  }

  @Override public long getDate() {
    return delegate.getDate();
  }

  @Override public boolean getDefaultUseCaches() {
    return delegate.getDefaultUseCaches();
  }

  @Override public boolean getDoInput() {
    return delegate.getDoInput();
  }

  @Override public boolean getDoOutput() {
    return delegate.getDoOutput();
  }

  @Override public long getExpiration() {
    return delegate.getExpiration();
  }

  @Override public String getHeaderField(int pos) {
    return delegate.getHeaderField(pos);
  }

  @Override public Map<String, List<String>> getHeaderFields() {
    return delegate.getHeaderFields();
  }

  @Override public Map<String, List<String>> getRequestProperties() {
    return delegate.getRequestProperties();
  }

  @Override public void addRequestProperty(String field, String newValue) {
    delegate.addRequestProperty(field, newValue);
  }

  @Override public String getHeaderField(String key) {
    return delegate.getHeaderField(key);
  }

  @Override public long getHeaderFieldDate(String field, long defaultValue) {
    return delegate.getHeaderFieldDate(field, defaultValue);
  }

  @Override public int getHeaderFieldInt(String field, int defaultValue) {
    return delegate.getHeaderFieldInt(field, defaultValue);
  }

  @Override public String getHeaderFieldKey(int position) {
    return delegate.getHeaderFieldKey(position);
  }

  @Override public long getIfModifiedSince() {
    return delegate.getIfModifiedSince();
  }

  @Override public InputStream getInputStream() throws IOException {
    return delegate.getInputStream();
  }

  @Override public long getLastModified() {
    return delegate.getLastModified();
  }

  @Override public OutputStream getOutputStream() throws IOException {
    return delegate.getOutputStream();
  }

  @Override public Permission getPermission() throws IOException {
    return delegate.getPermission();
  }

  @Override public String getRequestProperty(String field) {
    return delegate.getRequestProperty(field);
  }

  @Override public URL getURL() {
    return delegate.getURL();
  }

  @Override public boolean getUseCaches() {
    return delegate.getUseCaches();
  }

  @Override public void setAllowUserInteraction(boolean newValue) {
    delegate.setAllowUserInteraction(newValue);
  }

  @Override public void setDefaultUseCaches(boolean newValue) {
    delegate.setDefaultUseCaches(newValue);
  }

  @Override public void setDoInput(boolean newValue) {
    delegate.setDoInput(newValue);
  }

  @Override public void setDoOutput(boolean newValue) {
    delegate.setDoOutput(newValue);
  }

  @Override public void setIfModifiedSince(long newValue) {
    delegate.setIfModifiedSince(newValue);
  }

  @Override public void setRequestProperty(String field, String newValue) {
    delegate.setRequestProperty(field, newValue);
  }

  @Override public void setUseCaches(boolean newValue) {
    delegate.setUseCaches(newValue);
  }

  @Override public void setConnectTimeout(int timeoutMillis) {
    delegate.setConnectTimeout(timeoutMillis);
  }

  @Override public int getConnectTimeout() {
    return delegate.getConnectTimeout();
  }

  @Override public void setReadTimeout(int timeoutMillis) {
    delegate.setReadTimeout(timeoutMillis);
  }

  @Override public int getReadTimeout() {
    return delegate.getReadTimeout();
  }

  @Override public String toString() {
    return delegate.toString();
  }

  @Override public void setFixedLengthStreamingMode(int contentLength) {
    delegate.setFixedLengthStreamingMode(contentLength);
  }

  @Override public void setChunkedStreamingMode(int chunkLength) {
    delegate.setChunkedStreamingMode(chunkLength);
  }
}
