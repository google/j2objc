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
 * The base class for all {@code Certificate} related exceptions.
 * <p>
 * Note: This package is provided only for compatibility reasons. It contains a
 * simplified version of the java.security.cert package that was previously used
 * by JSSE (Java SSL package). All applications that do not have to be
 * compatible with older versions of JSSE (that is before Java SDK 1.5) should
 * only use java.security.cert.
 */
public class CertificateException extends Exception {

    /**
     * @serial
     */
    private static final long serialVersionUID = -5757213374030785290L;

    /**
     * Creates a new {@code CertificateException} with the specified message.
     *
     * @param msg
     *            the detail message for the exception.
     */
    public CertificateException(String msg) {
        super(msg);
    }

    /**
     * Creates a new {@code CertificateException}.
     */
    public CertificateException() {
    }
}
