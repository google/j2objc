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

/**
* @author Vera Y. Petrashkova
* @version $Revision$
*/

package org.apache.harmony.security.tests.support.cert;

import java.io.DataInputStream;
import java.io.InputStream;
import java.security.cert.CRL;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactorySpi;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Additional class for verification CertificateFactorySpi and
 * CertificateFactory classes
 *
 */

public class MyCertificateFactorySpi extends CertificateFactorySpi {
    // Variants of execution:
    // mode: false - list of encodings is empty
    // mode: true - list of encodings consists of 2 elements
    //               some exceptions are thrown when
    private static boolean mode;

    private Set<String> list;

    public MyCertificateFactorySpi() {
        super();
        mode = true;
        list = new HashSet<String>();
        list.add("aa");
        list.add("bb");
    }

    public static void putMode(boolean newMode) {
        mode = newMode;
    }

    public Certificate engineGenerateCertificate(InputStream inStream)
            throws CertificateException {
        if (!(inStream instanceof DataInputStream)) {
            throw new CertificateException("Incorrect inputstream");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Collection engineGenerateCertificates(InputStream inStream)
            throws CertificateException {
        if (!(inStream instanceof DataInputStream)) {
            throw new CertificateException("Incorrect inputstream");
        }
        return null;
    }

    public CRL engineGenerateCRL(InputStream inStream) throws CRLException {
        if (!(inStream instanceof DataInputStream)) {
            throw new CRLException("Incorrect inputstream");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Collection engineGenerateCRLs(InputStream inStream)
            throws CRLException {
        if (!(inStream instanceof DataInputStream)) {
            throw new CRLException("Incorrect inputstream");
        }
        return null;
    }

    public CertPath engineGenerateCertPath(InputStream inStream)
            throws CertificateException {
        if (!(inStream instanceof DataInputStream)) {
            throw new CertificateException("Incorrect inputstream");
        }
        Iterator<String> it = engineGetCertPathEncodings();
        if (!it.hasNext()) {
            throw new CertificateException("There are no CertPath encodings");
        }
        return engineGenerateCertPath(inStream, it.next());
    }

    public CertPath engineGenerateCertPath(InputStream inStream, String encoding)
            throws CertificateException {
        if (!(inStream instanceof DataInputStream)) {
            throw new CertificateException("Incorrect inputstream");
        }
        if (encoding.length() == 0) {
            if (mode) {
                throw new IllegalArgumentException("Encoding is empty");
            }
        }
        return null;
    }

    public CertPath engineGenerateCertPath(List<? extends Certificate> certificates) {
        if (certificates == null) {
            if (mode) {
                throw new NullPointerException("certificates is null");
            }
        }
        return null;
    }

    public Iterator<String> engineGetCertPathEncodings() {
        if (!mode) {
            list.clear();
        }
        return list.iterator();
    }
}