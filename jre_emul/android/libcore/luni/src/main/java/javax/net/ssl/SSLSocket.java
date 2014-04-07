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

package javax.net.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * The extension of {@code Socket} providing secure protocols like SSL (Secure
 * Sockets Layer) or TLS (Transport Layer Security).
 *
 * <h3>Default configuration</h3>
 * <p>{@code SSLSocket} instances obtained from default {@link SSLSocketFactory},
 * {@link SSLServerSocketFactory}, and {@link SSLContext} are configured as follows:
 *
 * <h4>Protocols</h4>
 *
 * <p>Client socket:
 * <table>
 *     <thead>
 *         <tr>
 *             <th>Protocol</th>
 *             <th>Supported (API Levels)</th>
 *             <th>Enabled by default (API Levels)</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td>SSLv3</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>TLSv1</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>TLSv1.1</td>
 *             <td>16+</td>
 *             <td>20+</td>
 *         </tr>
 *         <tr>
 *             <td>TLSv1.2</td>
 *             <td>16+</td>
 *             <td>20+</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * <p>Server socket:
 * <table>
 *     <thead>
 *         <tr>
 *             <th>Protocol</th>
 *             <th>Supported (API Levels)</th>
 *             <th>Enabled by default (API Levels)</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td>SSLv3</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>TLSv1</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>TLSv1.1</td>
 *             <td>16+</td>
 *             <td>16+</td>
 *         </tr>
 *         <tr>
 *             <td>TLSv1.2</td>
 *             <td>16+</td>
 *             <td>16+</td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * <h4>Cipher suites</h4>
 *
 * <p>Methods that operate with cipher suite names (for example,
 * {@link #getSupportedCipherSuites() getSupportedCipherSuites},
 * {@link #setEnabledCipherSuites(String[]) setEnabledCipherSuites}) have used
 * standard names for cipher suites since API Level 9, as listed in the table
 * below. Prior to API Level 9, non-standard (OpenSSL) names had been used (see
 * the table following this table).
 * <table>
 *     <thead>
 *         <tr>
 *             <th>Cipher suite</th>
 *             <th>Supported (API Levels)</th>
 *             <th>Enabled by default (API Levels)</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td>SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DHE_DSS_WITH_DES_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DHE_RSA_WITH_DES_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DH_anon_EXPORT_WITH_RC4_40_MD5</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DH_anon_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DH_anon_WITH_DES_CBC_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_DH_anon_WITH_RC4_128_MD5</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_EXPORT_WITH_RC4_40_MD5</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_DES_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_NULL_MD5</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_NULL_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_RC4_128_MD5</td>
 *             <td>9+</td>
 *             <td>9-19</td>
 *         </tr>
 *         <tr>
 *             <td>SSL_RSA_WITH_RC4_128_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_DSS_WITH_AES_128_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_DSS_WITH_AES_128_CBC_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_DSS_WITH_AES_128_GCM_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_DSS_WITH_AES_256_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_DSS_WITH_AES_256_CBC_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_DSS_WITH_AES_256_GCM_SHA384</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_RSA_WITH_AES_128_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_RSA_WITH_AES_128_CBC_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_RSA_WITH_AES_128_GCM_SHA256</td>
 *             <td>20+</td>
 *             <td>20+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_RSA_WITH_AES_256_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_RSA_WITH_AES_256_CBC_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DHE_RSA_WITH_AES_256_GCM_SHA384</td>
 *             <td>20+</td>
 *             <td>20+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_anon_WITH_AES_128_CBC_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_anon_WITH_AES_128_CBC_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_anon_WITH_AES_128_GCM_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_anon_WITH_AES_256_CBC_SHA</td>
 *             <td>9+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_anon_WITH_AES_256_CBC_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_DH_anon_WITH_AES_256_GCM_SHA384</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11-19</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256</td>
 *             <td>20+</td>
 *             <td>20+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384</td>
 *             <td>20+</td>
 *             <td>20+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_ECDSA_WITH_NULL_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_ECDSA_WITH_RC4_128_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11-19</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256</td>
 *             <td>20+</td>
 *             <td>20+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384</td>
 *             <td>20+</td>
 *             <td>20+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_RSA_WITH_NULL_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDHE_RSA_WITH_RC4_128_SHA</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11-19</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11-19</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11-19</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_ECDSA_WITH_NULL_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_ECDSA_WITH_RC4_128_SHA</td>
 *             <td>11+</td>
 *             <td>11-19</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11-19</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_RSA_WITH_AES_128_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11-19</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_RSA_WITH_AES_256_CBC_SHA</td>
 *             <td>11+</td>
 *             <td>11-19</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_RSA_WITH_NULL_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_RSA_WITH_RC4_128_SHA</td>
 *             <td>11+</td>
 *             <td>11-19</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_anon_WITH_AES_128_CBC_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_anon_WITH_AES_256_CBC_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_anon_WITH_NULL_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_EMPTY_RENEGOTIATION_INFO_SCSV</td>
 *             <td>11+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_ECDH_anon_WITH_RC4_128_SHA</td>
 *             <td>11+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_RSA_WITH_AES_128_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>9+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_RSA_WITH_AES_128_CBC_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_RSA_WITH_AES_128_GCM_SHA256</td>
 *             <td>20+</td>
 *             <td>20+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_RSA_WITH_AES_256_CBC_SHA</td>
 *             <td>9+</td>
 *             <td>11+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_RSA_WITH_AES_256_CBC_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *         <tr>
 *             <td>TLS_RSA_WITH_AES_256_GCM_SHA384</td>
 *             <td>20+</td>
 *             <td>20+</td>
 *         </tr>
 *         <tr>
 *             <td>TLS_RSA_WITH_NULL_SHA256</td>
 *             <td>20+</td>
 *             <td></td>
 *         </tr>
 *     </tbody>
 * </table>
 *
 * <p>API Levels 1 to 8 use OpenSSL names for cipher suites. The table below
 * lists these OpenSSL names and their corresponding standard names used in API
 * Levels 9 and newer.
 * <table>
 *     <thead>
 *         <tr>
 *             <th>OpenSSL cipher suite</th>
 *             <th>Standard cipher suite</th>
 *             <th>Supported (API Levels)</th>
 *             <th>Enabled by default (API Levels)</th>
 *         </tr>
 *     </thead>
 *
 *     <tbody>
 *         <tr>
 *             <td>AES128-SHA</td>
 *             <td>TLS_RSA_WITH_AES_128_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>AES256-SHA</td>
 *             <td>TLS_RSA_WITH_AES_256_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-8, 11+</td>
 *         </tr>
 *         <tr>
 *             <td>DES-CBC-MD5</td>
 *             <td>SSL_CK_DES_64_CBC_WITH_MD5</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>DES-CBC-SHA</td>
 *             <td>SSL_RSA_WITH_DES_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>DES-CBC3-MD5</td>
 *             <td>SSL_CK_DES_192_EDE3_CBC_WITH_MD5</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>DES-CBC3-SHA</td>
 *             <td>SSL_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>DHE-DSS-AES128-SHA</td>
 *             <td>TLS_DHE_DSS_WITH_AES_128_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>DHE-DSS-AES256-SHA</td>
 *             <td>TLS_DHE_DSS_WITH_AES_256_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-8, 11+</td>
 *         </tr>
 *         <tr>
 *             <td>DHE-RSA-AES128-SHA</td>
 *             <td>TLS_DHE_RSA_WITH_AES_128_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *         <tr>
 *             <td>DHE-RSA-AES256-SHA</td>
 *             <td>TLS_DHE_RSA_WITH_AES_256_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-8, 11+</td>
 *         </tr>
 *         <tr>
 *             <td>EDH-DSS-DES-CBC-SHA</td>
 *             <td>SSL_DHE_DSS_WITH_DES_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>EDH-DSS-DES-CBC3-SHA</td>
 *             <td>SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>EDH-RSA-DES-CBC-SHA</td>
 *             <td>SSL_DHE_RSA_WITH_DES_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>EDH-RSA-DES-CBC3-SHA</td>
 *             <td>SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>EXP-DES-CBC-SHA</td>
 *             <td>SSL_RSA_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>EXP-EDH-DSS-DES-CBC-SHA</td>
 *             <td>SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>EXP-EDH-RSA-DES-CBC-SHA</td>
 *             <td>SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>EXP-RC2-CBC-MD5</td>
 *             <td>SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>EXP-RC4-MD5</td>
 *             <td>SSL_RSA_EXPORT_WITH_RC4_40_MD5</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>RC2-CBC-MD5</td>
 *             <td>SSL_CK_RC2_128_CBC_WITH_MD5</td>
 *             <td>1-8</td>
 *             <td>1-8</td>
 *         </tr>
 *         <tr>
 *             <td>RC4-MD5</td>
 *             <td>SSL_RSA_WITH_RC4_128_MD5</td>
 *             <td>1+</td>
 *             <td>1-19</td>
 *         </tr>
 *         <tr>
 *             <td>RC4-SHA</td>
 *             <td>SSL_RSA_WITH_RC4_128_SHA</td>
 *             <td>1+</td>
 *             <td>1+</td>
 *         </tr>
 *     </tbody>
 * </table>
 */
public abstract class SSLSocket extends Socket {

    /**
     * Only to be used by subclasses.
     * <p>
     * Creates a TCP socket.
     */
    protected SSLSocket() {
    }

    /**
     * Only to be used by subclasses.
     * <p>
     * Creates a TCP socket connection to the specified host at the specified
     * port.
     *
     * @param host
     *            the host name to connect to.
     * @param port
     *            the port number to connect to.
     * @throws IOException
     *             if creating the socket fails.
     * @throws UnknownHostException
     *             if the specified host is not known.
     */
    protected SSLSocket(String host, int port) throws IOException, UnknownHostException {
        super(host, port);
    }

    /**
     * Only to be used by subclasses.
     * <p>
     * Creates a TCP socket connection to the specified address at the specified
     * port.
     *
     * @param address
     *            the address to connect to.
     * @param port
     *            the port number to connect to.
     * @throws IOException
     *             if creating the socket fails.
     */
    protected SSLSocket(InetAddress address, int port) throws IOException {
        super(address, port);
    }

    /**
     * Only to be used by subclasses.
     * <p>
     * Creates a TCP socket connection to the specified host at the specified
     * port with the client side bound to the specified address and port.
     *
     * @param host
     *            the host name to connect to.
     * @param port
     *            the port number to connect to.
     * @param clientAddress
     *            the client address to bind to
     * @param clientPort
     *            the client port number to bind to.
     * @throws IOException
     *             if creating the socket fails.
     * @throws UnknownHostException
     *             if the specified host is not known.
     */
    protected SSLSocket(String host, int port, InetAddress clientAddress, int clientPort)
            throws IOException, UnknownHostException {
        super(host, port, clientAddress, clientPort);
    }

    /**
     * Only to be used by subclasses.
     * <p>
     * Creates a TCP socket connection to the specified address at the specified
     * port with the client side bound to the specified address and port.
     *
     * @param address
     *            the address to connect to.
     * @param port
     *            the port number to connect to.
     * @param clientAddress
     *            the client address to bind to.
     * @param clientPort
     *            the client port number to bind to.
     * @throws IOException
     *             if creating the socket fails.
     */
    protected SSLSocket(InetAddress address, int port, InetAddress clientAddress, int clientPort)
            throws IOException {
        super(address, port, clientAddress, clientPort);
    }

    /**
     * Unsupported for SSL because reading from an SSL socket may require
     * writing to the network.
     */
    @Override public void shutdownInput() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Unsupported for SSL because writing to an SSL socket may require reading
     * from the network.
     */
    @Override public void shutdownOutput() throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the names of the supported cipher suites.
     */
    public abstract String[] getSupportedCipherSuites();

    /**
     * Returns the names of the enabled cipher suites.
     */
    public abstract String[] getEnabledCipherSuites();

    /**
     * Sets the names of the cipher suites to be enabled.
     * Only cipher suites returned by {@link #getSupportedCipherSuites()} are
     * allowed.
     *
     * @param suites
     *            the names of the to be enabled cipher suites.
     * @throws IllegalArgumentException
     *             if one of the cipher suite names is not supported.
     */
    public abstract void setEnabledCipherSuites(String[] suites);

    /**
     * Returns the names of the supported protocols.
     */
    public abstract String[] getSupportedProtocols();

    /**
     * Returns the names of the enabled protocols.
     */
    public abstract String[] getEnabledProtocols();

    /**
     * Sets the names of the protocols to be enabled. Only
     * protocols returned by {@link #getSupportedProtocols()} are allowed.
     *
     * @param protocols
     *            the names of the to be enabled protocols.
     * @throws IllegalArgumentException
     *             if one of the protocols is not supported.
     */
    public abstract void setEnabledProtocols(String[] protocols);

    /**
     * Returns the {@code SSLSession} for this connection. If necessary, a
     * handshake will be initiated, in which case this method will block until the handshake
     * has been established. If the handshake fails, an invalid session object
     * will be returned.
     *
     * @return the session object.
     */
    public abstract SSLSession getSession();

    /**
     * Registers the specified listener to receive notification on completion of a
     * handshake on this connection.
     *
     * @param listener
     *            the listener to register.
     * @throws IllegalArgumentException
     *             if {@code listener} is {@code null}.
     */
    public abstract void addHandshakeCompletedListener(HandshakeCompletedListener listener);

    /**
     * Removes the specified handshake completion listener.
     *
     * @param listener
     *            the listener to remove.
     * @throws IllegalArgumentException
     *             if the specified listener is not registered or {@code null}.
     */
    public abstract void removeHandshakeCompletedListener(HandshakeCompletedListener listener);

    /**
     * Starts a new SSL handshake on this connection.
     *
     * @throws IOException
     *             if an error occurs.
     */
    public abstract void startHandshake() throws IOException;

    /**
     * Sets whether this connection should act in client mode when handshaking.
     *
     * @param mode
     *            {@code true} if this connection should act in client mode,
     *            {@code false} if not.
     */
    public abstract void setUseClientMode(boolean mode);

    /**
     * Returns true if this connection will act in client mode when handshaking.
     */
    public abstract boolean getUseClientMode();

    /**
     * Sets whether the server should require client authentication. This
     * does not apply to sockets in {@link #getUseClientMode() client mode}.
     * Client authentication is one of the following:
     * <ul>
     * <li>authentication required</li>
     * <li>authentication requested</li>
     * <li>no authentication needed</li>
     * </ul>
     * This method overrides the setting of {@link #setWantClientAuth(boolean)}.
     */
    public abstract void setNeedClientAuth(boolean need);

    /**
     * Sets whether the server should request client authentication. Unlike
     * {@link #setNeedClientAuth} this won't stop the negotiation if the client
     * doesn't authenticate. This does not apply to sockets in {@link
     * #getUseClientMode() client mode}.The client authentication is one of:
     * <ul>
     * <li>authentication required</li>
     * <li>authentication requested</li>
     * <li>no authentication needed</li>
     * </ul>
     * This method overrides the setting of {@link #setNeedClientAuth(boolean)}.
     */
    public abstract void setWantClientAuth(boolean want);

    /**
     * Returns true if the server socket should require client authentication.
     * This does not apply to sockets in {@link #getUseClientMode() client
     * mode}.
     */
    public abstract boolean getNeedClientAuth();

    /**
     * Returns true if the server should request client authentication. This
     * does not apply to sockets in {@link #getUseClientMode() client mode}.
     */
    public abstract boolean getWantClientAuth();

    /**
     * Sets whether new SSL sessions may be created by this socket or if
     * existing sessions must be reused. If {@code flag} is false and there are
     * no sessions to resume, handshaking will fail.
     *
     * @param flag {@code true} if new sessions may be created.
     */
    public abstract void setEnableSessionCreation(boolean flag);

    /**
     * Returns whether new SSL sessions may be created by this socket or if
     * existing sessions must be reused.
     *
     * @return {@code true} if new sessions may be created, otherwise
     *         {@code false}.
     */
    public abstract boolean getEnableSessionCreation();

    /**
     * Returns a new SSLParameters based on this SSLSocket's current
     * cipher suites, protocols, and client authentication settings.
     *
     * @since 1.6
     */
    public SSLParameters getSSLParameters() {
        SSLParameters p = new SSLParameters();
        p.setCipherSuites(getEnabledCipherSuites());
        p.setProtocols(getEnabledProtocols());
        p.setNeedClientAuth(getNeedClientAuth());
        p.setWantClientAuth(getWantClientAuth());
        return p;
    }

    /**
     * Sets various SSL handshake parameters based on the SSLParameter
     * argument. Specifically, sets the SSLSocket's enabled cipher
     * suites if the parameter's cipher suites are non-null. Similarly
     * sets the enabled protocols. If the parameters specify the want
     * or need for client authentication, those requirements are set
     * on the SSLSocket, otherwise both are set to false.
     * @since 1.6
     */
    public void setSSLParameters(SSLParameters p) {
        String[] cipherSuites = p.getCipherSuites();
        if (cipherSuites != null) {
            setEnabledCipherSuites(cipherSuites);
        }
        String[] protocols = p.getProtocols();
        if (protocols != null) {
            setEnabledProtocols(protocols);
        }
        if (p.getNeedClientAuth()) {
            setNeedClientAuth(true);
        } else if (p.getWantClientAuth()) {
            setWantClientAuth(true);
        } else {
            setWantClientAuth(false);
        }
    }
}
