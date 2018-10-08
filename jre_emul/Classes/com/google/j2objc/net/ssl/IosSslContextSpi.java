/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.j2objc.net.ssl;

import java.security.KeyManagementException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/** Returns factories that use Apple's SecureTransport API. */
public class IosSslContextSpi extends SSLContextSpi {

  @Override
  protected void engineInit(KeyManager[] km, TrustManager[] tm, SecureRandom sr)
      throws KeyManagementException {
    throw new UnsupportedOperationException();
  }

  @Override
  protected SSLSocketFactory engineGetSocketFactory() {
    return new IosSslSocketFactory();
  }

  @Override
  protected SSLServerSocketFactory engineGetServerSocketFactory() {
    return null;
  }

  @Override
  protected SSLEngine engineCreateSSLEngine() {
    return null;
  }

  @Override
  protected SSLEngine engineCreateSSLEngine(String host, int port) {
    return null;
  }

  @Override
  protected SSLSessionContext engineGetServerSessionContext() {
    return null;
  }

  @Override
  protected SSLSessionContext engineGetClientSessionContext() {
    return null;
  }

  @Override
  protected SSLParameters engineGetDefaultSSLParameters() {
    return super.engineGetDefaultSSLParameters();
  }

  @Override
  protected SSLParameters engineGetSupportedSSLParameters() {
    return super.engineGetSupportedSSLParameters();
  }
}
