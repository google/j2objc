/*
 * Copyright (C) 2013 The Android Open Source Project
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

import junit.framework.Assert;
import libcore.java.security.StandardNames;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Assertions about the configuration of TLS/SSL primitives.
 */
public class SSLConfigurationAsserts extends Assert {

  /** Hidden constructor to prevent instantiation. */
  private SSLConfigurationAsserts() {}

  /**
   * Asserts that the provided {@link SSLContext} has the expected default configuration, and that
   * {@link SSLSocketFactory}, {@link SSLServerSocketFactory}, {@link SSLSocket},
   * {@link SSLServerSocket} and {@link SSLEngine} instances created from the context match the
   * configuration.
   */
  public static void assertSSLContextDefaultConfiguration(SSLContext sslContext)
      throws IOException {
    SSLParameters defaultParameters = sslContext.getDefaultSSLParameters();
    StandardNames.assertSSLContextEnabledProtocols(sslContext.getProtocol(),
        defaultParameters.getProtocols());
    StandardNames.assertDefaultCipherSuites(defaultParameters.getCipherSuites());
    assertFalse(defaultParameters.getWantClientAuth());
    assertFalse(defaultParameters.getNeedClientAuth());

    SSLParameters supportedParameters = sslContext.getSupportedSSLParameters();
    StandardNames.assertSupportedCipherSuites(supportedParameters.getCipherSuites());
    StandardNames.assertSupportedProtocols(supportedParameters.getProtocols());
    assertFalse(supportedParameters.getWantClientAuth());
    assertFalse(supportedParameters.getNeedClientAuth());

    assertContainsAll("Unsupported enabled cipher suites", supportedParameters.getCipherSuites(),
        defaultParameters.getCipherSuites());
    assertContainsAll("Unsupported enabled protocols", supportedParameters.getProtocols(),
        defaultParameters.getProtocols());

    assertSSLSocketFactoryConfigSameAsSSLContext(sslContext.getSocketFactory(), sslContext);
    assertSSLServerSocketFactoryConfigSameAsSSLContext(sslContext.getServerSocketFactory(),
        sslContext);

    SSLEngine sslEngine = sslContext.createSSLEngine();
    assertFalse(sslEngine.getUseClientMode());
    assertSSLEngineConfigSameAsSSLContext(sslEngine, sslContext);
  }

  /**
   * Asserts that the provided {@link SSLSocketFactory} has the expected default configuration and
   * that {@link SSLSocket} instances created by the factory match the configuration.
   */
  public static void assertSSLSocketFactoryDefaultConfiguration(
      SSLSocketFactory sslSocketFactory) throws Exception {
    assertSSLSocketFactoryConfigSameAsSSLContext(sslSocketFactory,
        SSLContext.getDefault());
  }

  /**
   * Asserts that {@link SSLSocketFactory}'s configuration matches {@code SSLContext}'s
   * configuration, and that {@link SSLSocket} instances obtained from the factory match this
   * configuration as well.
   */
  private static void assertSSLSocketFactoryConfigSameAsSSLContext(
      SSLSocketFactory sslSocketFactory, SSLContext sslContext) throws IOException {
    assertCipherSuitesEqual(sslContext.getDefaultSSLParameters().getCipherSuites(),
        sslSocketFactory.getDefaultCipherSuites());
    assertCipherSuitesEqual(sslContext.getSupportedSSLParameters().getCipherSuites(),
        sslSocketFactory.getSupportedCipherSuites());

    try (SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket()) {
      assertTrue(sslSocket.getUseClientMode());
      assertTrue(sslSocket.getEnableSessionCreation());
      assertSSLSocketConfigSameAsSSLContext(sslSocket, sslContext);
    }
  }

  /**
   * Asserts that the provided {@link SSLSocket} has the expected default configuration.
   */
  public static void assertSSLSocketDefaultConfiguration(SSLSocket sslSocket) throws Exception {
    assertTrue(sslSocket.getUseClientMode());
    assertTrue(sslSocket.getEnableSessionCreation());
    assertSSLSocketConfigSameAsSSLContext(sslSocket, SSLContext.getDefault());
  }

  /**
   * Asserts that {@link SSLSocket}'s configuration matches {@code SSLContext's} configuration.
   */
  private static void assertSSLSocketConfigSameAsSSLContext(SSLSocket sslSocket,
      SSLContext sslContext) {
    assertSSLParametersEqual(sslSocket.getSSLParameters(), sslContext.getDefaultSSLParameters());
    assertCipherSuitesEqual(sslSocket.getEnabledCipherSuites(),
        sslContext.getDefaultSSLParameters().getCipherSuites());
    assertProtocolsEqual(sslSocket.getEnabledProtocols(),
        sslContext.getDefaultSSLParameters().getProtocols());

    assertCipherSuitesEqual(sslSocket.getSupportedCipherSuites(),
        sslContext.getSupportedSSLParameters().getCipherSuites());
    assertProtocolsEqual(sslSocket.getSupportedProtocols(),
        sslContext.getSupportedSSLParameters().getProtocols());
  }

  /**
   * Asserts that the provided {@link SSLServerSocketFactory} has the expected default
   * configuration, and that {@link SSLServerSocket} instances created by the factory match the
   * configuration.
   */
  public static void assertSSLServerSocketFactoryDefaultConfiguration(
      SSLServerSocketFactory sslServerSocketFactory) throws Exception {
    assertSSLServerSocketFactoryConfigSameAsSSLContext(sslServerSocketFactory,
        SSLContext.getDefault());
  }

  /**
   * Asserts that {@link SSLServerSocketFactory}'s configuration matches {@code SSLContext}'s
   * configuration, and that {@link SSLServerSocket} instances obtained from the factory match this
   * configuration as well.
   */
  private static void assertSSLServerSocketFactoryConfigSameAsSSLContext(
      SSLServerSocketFactory sslServerSocketFactory, SSLContext sslContext)  throws IOException {
    assertCipherSuitesEqual(sslContext.getDefaultSSLParameters().getCipherSuites(),
        sslServerSocketFactory.getDefaultCipherSuites());
    assertCipherSuitesEqual(sslContext.getSupportedSSLParameters().getCipherSuites(),
        sslServerSocketFactory.getSupportedCipherSuites());
    try (SSLServerSocket sslServerSocket =
        (SSLServerSocket) sslServerSocketFactory.createServerSocket()) {
      assertFalse(sslServerSocket.getUseClientMode());
      assertTrue(sslServerSocket.getEnableSessionCreation());
      assertSSLServerSocketConfigSameAsSSLContext(sslServerSocket, sslContext);
    }
  }

  /**
   * Asserts that the provided {@link SSLServerSocket} has the expected default configuration.
   */
  public static void assertSSLServerSocketDefaultConfiguration(SSLServerSocket sslServerSocket)
      throws Exception {
    assertFalse(sslServerSocket.getUseClientMode());
    assertTrue(sslServerSocket.getEnableSessionCreation());
    assertSSLServerSocketConfigSameAsSSLContext(sslServerSocket, SSLContext.getDefault());
    // TODO: Check SSLParameters when supported by SSLServerSocket API
  }

  /**
   * Asserts that {@link SSLServerSocket}'s configuration matches {@code SSLContext's}
   * configuration.
   */
  private static void assertSSLServerSocketConfigSameAsSSLContext(SSLServerSocket sslServerSocket,
      SSLContext sslContext) {
    assertCipherSuitesEqual(sslServerSocket.getEnabledCipherSuites(),
        sslContext.getDefaultSSLParameters().getCipherSuites());
    assertProtocolsEqual(sslServerSocket.getEnabledProtocols(),
        sslContext.getDefaultSSLParameters().getProtocols());

    assertCipherSuitesEqual(sslServerSocket.getSupportedCipherSuites(),
        sslContext.getSupportedSSLParameters().getCipherSuites());
    assertProtocolsEqual(sslServerSocket.getSupportedProtocols(),
        sslContext.getSupportedSSLParameters().getProtocols());

    assertEquals(sslServerSocket.getNeedClientAuth(),
        sslContext.getDefaultSSLParameters().getNeedClientAuth());
    assertEquals(sslServerSocket.getWantClientAuth(),
        sslContext.getDefaultSSLParameters().getWantClientAuth());
  }

  /**
   * Asserts that the provided {@link SSLEngine} has the expected default configuration.
   */
  public static void assertSSLEngineDefaultConfiguration(SSLEngine sslEngine) throws Exception {
    assertFalse(sslEngine.getUseClientMode());
    assertTrue(sslEngine.getEnableSessionCreation());
    assertSSLEngineConfigSameAsSSLContext(sslEngine, SSLContext.getDefault());
  }

  /**
   * Asserts that {@link SSLEngine}'s configuration matches {@code SSLContext's} configuration.
   */
  private static void assertSSLEngineConfigSameAsSSLContext(SSLEngine sslEngine,
      SSLContext sslContext) {
    assertSSLParametersEqual(sslEngine.getSSLParameters(), sslContext.getDefaultSSLParameters());
    assertCipherSuitesEqual(sslEngine.getEnabledCipherSuites(),
        sslContext.getDefaultSSLParameters().getCipherSuites());
    assertProtocolsEqual(sslEngine.getEnabledProtocols(),
        sslContext.getDefaultSSLParameters().getProtocols());

    assertCipherSuitesEqual(sslEngine.getSupportedCipherSuites(),
        sslContext.getSupportedSSLParameters().getCipherSuites());
    assertProtocolsEqual(sslEngine.getSupportedProtocols(),
        sslContext.getSupportedSSLParameters().getProtocols());
  }

  private static void assertSSLParametersEqual(SSLParameters expected, SSLParameters actual) {
    assertCipherSuitesEqual(expected.getCipherSuites(), actual.getCipherSuites());
    assertProtocolsEqual(expected.getProtocols(), actual.getProtocols());
    assertEquals(expected.getNeedClientAuth(), actual.getNeedClientAuth());
    assertEquals(expected.getWantClientAuth(), actual.getWantClientAuth());
  }

  private static void assertCipherSuitesEqual(String[] expected, String[] actual) {
    assertEquals(Arrays.asList(expected), Arrays.asList(actual));
  }

  private static void assertProtocolsEqual(String[] expected, String[] actual) {
    // IMPLEMENTATION NOTE: The order of protocols versions does not matter. Similarly, it only
    // matters whether a protocol version is present or absent in the array. These arrays are
    // supposed to represent sets of protocol versions. Thus, we treat them as such.
    assertEquals(new HashSet<String>(Arrays.asList(expected)),
        new HashSet<String>(Arrays.asList(actual)));
  }

  /**
   * Asserts that the {@code container} contains all the {@code elements}.
   */
  private static void assertContainsAll(String message, String[] container, String[] elements) {
    Set<String> elementsNotInContainer = new HashSet<String>(Arrays.asList(elements));
    elementsNotInContainer.removeAll(Arrays.asList(container));
    assertEquals(message, Collections.EMPTY_SET, elementsNotInContainer);
  }
}
