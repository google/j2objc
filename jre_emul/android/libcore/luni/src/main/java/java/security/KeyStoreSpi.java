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

package java.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.Enumeration;
import javax.crypto.SecretKey;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.PasswordCallback;

/**
 * {@code KeyStoreSpi} is the Service Provider Interface (SPI) definition for
 * {@link KeyStore}.
 *
 * @see KeyStore
 */
public abstract class KeyStoreSpi {

    /**
     * Returns the key with the given alias, using the password to recover the
     * key from the store.
     *
     * @param alias
     *            the alias for the entry.
     * @param password
     *            the password used to recover the key.
     * @return the key with the specified alias, or {@code null} if the
     *         specified alias is not bound to an entry.
     * @throws NoSuchAlgorithmException
     *             if the algorithm for recovering the key is not available.
     * @throws UnrecoverableKeyException
     *             if the key can not be recovered.
     */
    public abstract Key engineGetKey(String alias, char[] password)
            throws NoSuchAlgorithmException, UnrecoverableKeyException;

    /**
     * Returns the certificate chain for the entry with the given alias.
     *
     * @param alias
     *            the alias for the entry
     * @return the certificate chain for the entry with the given alias, or
     *         {@code null} if the specified alias is not bound to an entry.
     */
    public abstract Certificate[] engineGetCertificateChain(String alias);

    /**
     * Returns the trusted certificate for the entry with the given alias.
     *
     * @param alias
     *            the alias for the entry.
     * @return the trusted certificate for the entry with the given alias, or
     *         {@code null} if the specified alias is not bound to an entry.
     */
    public abstract Certificate engineGetCertificate(String alias);

    /**
     * Returns the creation date of the entry with the given alias.
     *
     * @param alias
     *            the alias for the entry.
     * @return the creation date, or {@code null} if the specified alias is not
     *         bound to an entry.
     */
    public abstract Date engineGetCreationDate(String alias);

    /**
     * Associates the given alias with the key, password and certificate chain.
     * <p>
     * If the specified alias already exists, it will be reassigned.
     *
     * @param alias
     *            the alias for the key.
     * @param key
     *            the key.
     * @param password
     *            the password.
     * @param chain
     *            the certificate chain.
     * @throws KeyStoreException
     *             if the specified key can not be protected, or if this
     *             operation fails for another reason.
     * @throws IllegalArgumentException
     *             if {@code key} is a {@code PrivateKey} and {@code chain} does
     *             not contain any certificates.
     */
    public abstract void engineSetKeyEntry(String alias, Key key,
            char[] password, Certificate[] chain) throws KeyStoreException;

    /**
     * Associates the given alias with a key and a certificate chain.
     * <p>
     * If the specified alias already exists, it will be reassigned.
     *
     * @param alias
     *            the alias for the key.
     * @param key
     *            the key in an encoded format.
     * @param chain
     *            the certificate chain.
     * @throws KeyStoreException
     *             if this operation fails.
     * @throws IllegalArgumentException
     *             if {@code key} is a {@code PrivateKey} and {@code chain}
     *             does.
     */
    public abstract void engineSetKeyEntry(String alias, byte[] key,
            Certificate[] chain) throws KeyStoreException;

    /**
     * Associates the given alias with a certificate.
     * <p>
     * If the specified alias already exists, it will be reassigned.
     *
     * @param alias
     *            the alias for the certificate.
     * @param cert
     *            the certificate.
     * @throws KeyStoreException
     *             if an existing alias is not associated to an entry containing
     *             a trusted certificate, or this method fails for any other
     *             reason.
     */
    public abstract void engineSetCertificateEntry(String alias,
            Certificate cert) throws KeyStoreException;

    /**
     * Deletes the entry identified with the given alias from this {@code
     * KeyStoreSpi}.
     *
     * @param alias
     *            the alias for the entry.
     * @throws KeyStoreException
     *             if the entry can not be deleted.
     */
    public abstract void engineDeleteEntry(String alias)
            throws KeyStoreException;

    /**
     * Returns an {@code Enumeration} over all alias names stored in this
     * {@code KeyStoreSpi}.
     *
     * @return an {@code Enumeration} over all alias names stored in this
     *         {@code KeyStoreSpi}.
     */
    public abstract Enumeration<String> engineAliases();

    /**
     * Indicates whether the given alias is present in this {@code KeyStoreSpi}.
     *
     * @param alias
     *            the alias of an entry.
     * @return {@code true} if the alias exists, {@code false} otherwise.
     */
    public abstract boolean engineContainsAlias(String alias);

    /**
     * Returns the number of entries stored in this {@code KeyStoreSpi}.
     *
     * @return the number of entries stored in this {@code KeyStoreSpi}.
     */
    public abstract int engineSize();

    /**
     * Indicates whether the specified alias is associated with either a
     * {@link KeyStore.PrivateKeyEntry} or a {@link KeyStore.SecretKeyEntry}.
     *
     * @param alias
     *            the alias of an entry.
     * @return {@code true} if the given alias is associated with a key entry.
     */
    public abstract boolean engineIsKeyEntry(String alias);

    /**
     * Indicates whether the specified alias is associated with a
     * {@link KeyStore.TrustedCertificateEntry}.
     *
     * @param alias
     *            the alias of an entry.
     * @return {@code true} if the given alias is associated with a certificate
     *         entry.
     */
    public abstract boolean engineIsCertificateEntry(String alias);

    /**
     * Returns the alias associated with the first entry whose certificate
     * matches the specified certificate.
     *
     * @param cert
     *            the certificate to find the associated entry's alias for.
     * @return the alias or {@code null} if no entry with the specified
     *         certificate can be found.
     */
    public abstract String engineGetCertificateAlias(Certificate cert);

    /**
     * Writes this {@code KeyStoreSpi} to the specified {@code OutputStream}.
     * The data written to the {@code OutputStream} is protected by the
     * specified password.
     *
     * @param stream
     *            the {@code OutputStream} to write the store's data to.
     * @param password
     *            the password to protect the data.
     * @throws IOException
     *             if a problem occurred while writing to the stream.
     * @throws NoSuchAlgorithmException
     *             if the required algorithm is not available.
     * @throws CertificateException
     *             if the an exception occurred while storing the certificates
     *             of this code {@code KeyStoreSpi}.
     */
    public abstract void engineStore(OutputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException;

    /**
     * Stores this {@code KeyStoreSpi} using the specified {@code
     * LoadStoreParameter}.
     *
     * @param param
     *            the {@code LoadStoreParameter} that specifies how to store
     *            this {@code KeyStoreSpi}, maybe {@code null}.
     * @throws IOException
     *             if a problem occurred while writing to the stream.
     * @throws NoSuchAlgorithmException
     *             if the required algorithm is not available.
     * @throws CertificateException
     *             if the an exception occurred while storing the certificates
     *             of this code {@code KeyStoreSpi}.
     * @throws IllegalArgumentException
     *             if the given {@link KeyStore.LoadStoreParameter} is not
     *             recognized.
     */
    public void engineStore(KeyStore.LoadStoreParameter param)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        throw new UnsupportedOperationException();
    }

    /**
     * Loads this {@code KeyStoreSpi} from the given {@code InputStream}.
     * Utilizes the given password to verify the stored data.
     *
     * @param stream
     *            the {@code InputStream} to load this {@code KeyStoreSpi}'s
     *            data from.
     * @param password
     *            the password to verify the stored data, maybe {@code null}.
     * @throws IOException
     *             if a problem occurred while reading from the stream.
     * @throws NoSuchAlgorithmException
     *             if the required algorithm is not available.
     * @throws CertificateException
     *             if the an exception occurred while loading the certificates
     *             of this code {@code KeyStoreSpi}.
     */
    public abstract void engineLoad(InputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException;

    /**
     * Loads this {@code KeyStoreSpi} using the specified {@code
     * LoadStoreParameter}.
     *
     * @param param
     *            the {@code LoadStoreParameter} that specifies how to load this
     *            {@code KeyStoreSpi}, maybe {@code null}.
     * @throws IOException
     *             if a problem occurred while reading from the stream.
     * @throws NoSuchAlgorithmException
     *             if the required algorithm is not available.
     * @throws CertificateException
     *             if the an exception occurred while loading the certificates
     *             of this code {@code KeyStoreSpi}.
     * @throws IllegalArgumentException
     *             if the given {@link KeyStore.LoadStoreParameter} is not
     *             recognized.
     */
    public void engineLoad(KeyStore.LoadStoreParameter param)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        if (param == null) {
            engineLoad(null, null);
            return;
        }
        char[] pwd;
        KeyStore.ProtectionParameter pp = param.getProtectionParameter();
        if (pp instanceof KeyStore.PasswordProtection) {
            try {
                pwd = ((KeyStore.PasswordProtection) pp).getPassword();
                engineLoad(null, pwd);
                return;
            } catch (IllegalStateException e) {
                throw new IllegalArgumentException(e);
            }
        }
        if (pp instanceof KeyStore.CallbackHandlerProtection) {
            try {
                pwd = getPasswordFromCallBack(pp);
                engineLoad(null, pwd);
                return;
            } catch (UnrecoverableEntryException e) {
                throw new IllegalArgumentException(e);
            }
        }
        throw new UnsupportedOperationException("protectionParameter is neither PasswordProtection "
                                                + "nor CallbackHandlerProtection instance");
    }

    /**
     * Returns the {@code Entry} with the given alias, using the specified
     * {@code ProtectionParameter}.
     *
     * @param alias
     *            the alias of the requested entry.
     * @param protParam
     *            the {@code ProtectionParameter}, used to protect the requested
     *            entry, maybe {@code null}.
     * @return he {@code Entry} with the given alias, using the specified
     *         {@code ProtectionParameter}.
     * @throws NoSuchAlgorithmException
     *             if the required algorithm is not available.
     * @throws UnrecoverableEntryException
     *             if the entry can not be recovered.
     * @throws KeyStoreException
     *             if this operation fails
     */
    public KeyStore.Entry engineGetEntry(String alias,
            KeyStore.ProtectionParameter protParam) throws KeyStoreException,
            NoSuchAlgorithmException, UnrecoverableEntryException {
        if (!engineContainsAlias(alias)) {
            return null;
        }
        if (engineIsCertificateEntry(alias)) {
            return new KeyStore.TrustedCertificateEntry(
                    engineGetCertificate(alias));
        }
        char[] passW = null;
        if (protParam != null) {
            if (protParam instanceof KeyStore.PasswordProtection) {
                try {
                    passW = ((KeyStore.PasswordProtection) protParam)
                            .getPassword();
                } catch (IllegalStateException ee) {
                    throw new KeyStoreException("Password was destroyed", ee);
                }
            } else if (protParam instanceof KeyStore.CallbackHandlerProtection) {
                passW = getPasswordFromCallBack(protParam);
            } else {
                throw new UnrecoverableEntryException("ProtectionParameter object is not "
                                                      + "PasswordProtection: " + protParam);
            }
        }
        if (engineIsKeyEntry(alias)) {
            Key key = engineGetKey(alias, passW);
            if (key instanceof PrivateKey) {
                return new KeyStore.PrivateKeyEntry((PrivateKey) key,
                                                    engineGetCertificateChain(alias));
            }
            if (key instanceof SecretKey) {
                return new KeyStore.SecretKeyEntry((SecretKey) key);
            }
        }
        throw new NoSuchAlgorithmException("Unknown KeyStore.Entry object");
    }

    /**
     * Stores the given {@code Entry} in this {@code KeyStoreSpi} and associates
     * the entry with the given {@code alias}. The entry is protected by the
     * specified {@code ProtectionParameter}.
     * <p>
     * If the specified alias already exists, it will be reassigned.
     *
     * @param alias
     *            the alias for the entry.
     * @param entry
     *            the entry to store.
     * @param protParam
     *            the {@code ProtectionParameter} to protect the entry.
     * @throws KeyStoreException
     *             if this operation fails.
     */
    public void engineSetEntry(String alias, KeyStore.Entry entry,
            KeyStore.ProtectionParameter protParam) throws KeyStoreException {
        if (entry == null) {
            throw new KeyStoreException("entry == null");
        }

        if (engineContainsAlias(alias)) {
            engineDeleteEntry(alias);
        }

        if (entry instanceof KeyStore.TrustedCertificateEntry) {
            KeyStore.TrustedCertificateEntry trE = (KeyStore.TrustedCertificateEntry) entry;
            engineSetCertificateEntry(alias, trE.getTrustedCertificate());
            return;
        }

        char[] passW = null;
        if (protParam != null) {
            if (protParam instanceof KeyStore.PasswordProtection) {
                try {
                    passW = ((KeyStore.PasswordProtection) protParam).getPassword();
                } catch (IllegalStateException ee) {
                    throw new KeyStoreException("Password was destroyed", ee);
                }
            } else if (protParam instanceof KeyStore.CallbackHandlerProtection) {
                try {
                    passW = getPasswordFromCallBack(protParam);
                } catch (Exception e) {
                    throw new KeyStoreException(e);
                }
            } else {
                throw new KeyStoreException("protParam should be PasswordProtection or "
                                            + "CallbackHandlerProtection");
            }
        }

        if (entry instanceof KeyStore.PrivateKeyEntry) {
            KeyStore.PrivateKeyEntry prE = (KeyStore.PrivateKeyEntry) entry;
            engineSetKeyEntry(alias, prE.getPrivateKey(), passW, prE
                    .getCertificateChain());
            return;
        }

        if (entry instanceof KeyStore.SecretKeyEntry) {
            KeyStore.SecretKeyEntry skE = (KeyStore.SecretKeyEntry) entry;
            engineSetKeyEntry(alias, skE.getSecretKey(), passW, null);
            //            engineSetKeyEntry(alias, skE.getSecretKey().getEncoded(), null);
            return;
        }

        throw new KeyStoreException("Entry object is neither PrivateKeyObject nor SecretKeyEntry "
                                    + "nor TrustedCertificateEntry: " + entry);
    }

    /**
     * Indicates whether the entry for the given alias is assignable to the
     * provided {@code Class}.
     *
     * @param alias
     *            the alias for the entry.
     * @param entryClass
     *            the type of the entry.
     * @return {@code true} if the {@code Entry} for the alias is assignable to
     *         the specified {@code entryClass}.
     */
    public boolean engineEntryInstanceOf(String alias,
            Class<? extends KeyStore.Entry> entryClass) {
        if (!engineContainsAlias(alias)) {
            return false;
        }

        try {
            if (engineIsCertificateEntry(alias)) {
                return entryClass
                        .isAssignableFrom(Class
                                .forName("java.security.KeyStore$TrustedCertificateEntry"));
            }

            if (engineIsKeyEntry(alias)) {
                if (entryClass.isAssignableFrom(Class
                        .forName("java.security.KeyStore$PrivateKeyEntry"))) {
                    return engineGetCertificate(alias) != null;
                }

                if (entryClass.isAssignableFrom(Class
                        .forName("java.security.KeyStore$SecretKeyEntry"))) {
                    return engineGetCertificate(alias) == null;
                }
            }
        } catch (ClassNotFoundException ignore) {}

        return false;
    }

    /*
     * This method returns password which is encapsulated in
     * CallbackHandlerProtection object If there is no implementation of
     * CallbackHandler then this method returns null
     */
    static char[] getPasswordFromCallBack(KeyStore.ProtectionParameter protParam)
            throws UnrecoverableEntryException {

        if (protParam == null) {
            return null;
        }

        if (!(protParam instanceof KeyStore.CallbackHandlerProtection)) {
            throw new UnrecoverableEntryException("Incorrect ProtectionParameter");
        }

        String clName = Security.getProperty("auth.login.defaultCallbackHandler");
        if (clName == null) {
            throw new UnrecoverableEntryException("Default CallbackHandler was not defined");

        }

        try {
            Class<?> cl = Class.forName(clName);
            CallbackHandler cbHand = (CallbackHandler) cl.newInstance();
            PasswordCallback[] pwCb = { new PasswordCallback("password: ", true) };
            cbHand.handle(pwCb);
            return pwCb[0].getPassword();
        } catch (Exception e) {
            throw new UnrecoverableEntryException(e.toString());
        }
    }
}
