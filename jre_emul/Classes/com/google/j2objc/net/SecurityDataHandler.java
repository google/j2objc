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

/**
 * Defines methods that accept data from the iOS Security Framework.
 * A handler interface is used so that IosHttpURLConnection does not
 * have any dependencies on the jre_security library.
 */
interface SecurityDataHandler {

  /**
   * Handles a "raw" certificate. Raw certificates are byte arrays returned
   * by Security Framework's SecCertificateCopyData(). This method actually
   * throws a CertificateException, but that is also defined in jre_security.
   */
  void handleSecCertificateData(byte[] secCertData) throws Exception;

}
