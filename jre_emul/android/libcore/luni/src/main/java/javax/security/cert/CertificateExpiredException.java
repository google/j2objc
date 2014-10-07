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

package javax.security.cert;

/**
 * The exception that is thrown when a {@code Certificate} has expired.
 * <p>
 * Note: This package is provided only for compatibility reasons. It contains a
 * simplified version of the java.security.cert package that was previously used
 * by JSSE (Java SSL package). All applications that do not have to be
 * compatible with older versions of JSSE (that is before Java SDK 1.5) should
 * only use java.security.cert.
 */
public class CertificateExpiredException extends CertificateException {

    /**
     * @serial
     */
    private static final long serialVersionUID = 5091601212177261883L;

    /**
     * Creates a new {@code CertificateExpiredException} with the specified
     * message.
     *
     * @param msg
     *            the detail message for this exception
     */
    public CertificateExpiredException(String msg) {
        super(msg);
    }

    /**
     * Creates a new {@code CertificateExpiredException}.
     */
    public CertificateExpiredException() {
    }
}
