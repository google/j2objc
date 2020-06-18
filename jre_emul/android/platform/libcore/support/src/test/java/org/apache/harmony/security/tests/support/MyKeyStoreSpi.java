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

package org.apache.harmony.security.tests.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Enumeration;

/**
 * Additional class for KeyStoreSpi and KeyStore verification
 *
 */

public class MyKeyStoreSpi extends KeyStoreSpi {

    @SuppressWarnings("unused")
    public Key engineGetKey(String alias, char[] password)
            throws NoSuchAlgorithmException, UnrecoverableKeyException {
        return null;
    }

    public Certificate[] engineGetCertificateChain(String alias) {
        return null;
    }

    public Certificate engineGetCertificate(String alias) {
        if (alias.equals("test_engineEntryInstanceOf_Alias1")) {
            return new MyCertificate("TestType");
        }
        return null;
    }

    public Date engineGetCreationDate(String alias) {
        return new Date(0);
    }

    public void engineSetKeyEntry(String alias, Key key, char[] password,
            Certificate[] chain) throws KeyStoreException {
        throw new KeyStoreException(
                "engineSetKeyEntry is not supported in myKeyStoreSpi");
    }

    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain)
            throws KeyStoreException {
        throw new KeyStoreException(
                "engineSetKeyEntry is not supported in myKeyStoreSpi");
    }

    public void engineSetCertificateEntry(String alias, Certificate cert)
            throws KeyStoreException {
        throw new KeyStoreException(
                "engineSetCertificateEntry is not supported in myKeyStoreSpi");
    }

    public void engineDeleteEntry(String alias) throws KeyStoreException {
        throw new KeyStoreException(
                "engineDeleteEntry is not supported in myKeyStoreSpi");
    }

    public Enumeration<String> engineAliases() {
        return null;
    }

    public boolean engineContainsAlias(String alias) {
        if (alias != null)
        {
            return alias.startsWith("test_engineEntry");
        }
        return false;
    }

    public int engineSize() {
        return 0;
    }

    public boolean engineIsKeyEntry(String alias) {
        if (alias.equals("test_engineEntryInstanceOf_Alias1")) {
            return true;
        } else {
            return false;
        }

    }

    public boolean engineIsCertificateEntry(String alias) {
        if (alias.equals("test_engineEntryInstanceOf_Alias2")) {
            return true;
        } else {
            return false;
        }
    }

    public String engineGetCertificateAlias(Certificate cert) {
        return "";
    }

    @SuppressWarnings("unused")
    public void engineStore(OutputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        if (!(stream instanceof ByteArrayOutputStream)) {
            throw new IOException("Incorrect stream");
        }
        if (((ByteArrayOutputStream) stream).size() == 0) {
            throw new IOException("Incorrect stream size ");

        }

    }

    @SuppressWarnings("unused")
    public void engineLoad(InputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException {
    }

    class MyCertificate extends Certificate {
        public MyCertificate(String type) {
            super(type);
        }

        public byte[] getEncoded() {
            return null;
        }

        public PublicKey getPublicKey() {
            return null;
        }

        public String toString() {
            return null;
        }

        public void verify(PublicKey key) {

        }

        public void verify(PublicKey key, String sigProvider) {

        }
    }
}