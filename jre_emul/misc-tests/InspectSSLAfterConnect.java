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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import javax.net.ssl.HttpsURLConnection;

/**
 * Test that we can inspect the SSL session after connect().
 *
 * Move to here from the test suite, due to security concerns.
 */
public class InspectSSLAfterConnect {
  private static HttpsURLConnection connection;
  private static InputStream inputStream;
  private static Certificate[] certs;

  public static void main(String[] args) {
    try {
      connect();
      printSSLCertificates();
      printPublicKeysHexEncoded();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void connect() throws MalformedURLException, IOException {
    connection = (HttpsURLConnection) new URL("https://www.google.com").openConnection();
    connection.connect();
    inputStream = connection.getInputStream();
  }

  public static void printSSLCertificates() throws Exception {
    certs = connection.getServerCertificates();
    for (Certificate cert : certs) {
      System.out.println(cert.toString());
    }
  }

  // Tip: One way to do cross-platform public key/certificate pinning
  // is to add your public key pin set via your build.gradle BuildConfig params
  // e.g. BuildConfigField "String[]", "HTTPS_PINSET", "new String[] { ... }",
  // then compare it in runtime with the ones fetched from the server.
  // As a bonus you could also compare the salted MessageDigest (e.g. SHA-256) results.
  public static void printPublicKeysHexEncoded() {
    StringBuilder sb = new StringBuilder();
    for (Certificate cert : certs) {
      for (byte b : cert.getPublicKey().getEncoded()) {
        sb.append(String.format("%02X ", b));
      }
      sb.append("\n");
    }
    System.out.println(sb.toString());
  }
}
