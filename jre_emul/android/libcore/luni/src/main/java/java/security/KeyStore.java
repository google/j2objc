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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import javax.security.auth.callback.CallbackHandler;
import libcore.io.IoUtils;
import org.apache.harmony.security.fortress.Engine;

/**
 * {@code KeyStore} is responsible for maintaining cryptographic keys and their
 * owners.
 * <p>
 * The type of the system key store can be changed by setting the {@code
 * 'keystore.type'} property in the file named {@code
 * JAVA_HOME/lib/security/java.security}.
 *
 * @see Certificate
 * @see PrivateKey
 */
public class KeyStore {

    // Store KeyStore SERVICE name
    private static final String SERVICE = "KeyStore";

    // Used to access common engine functionality
    private static final Engine ENGINE = new Engine(SERVICE);

    //  Store KeyStore property name
    private static final String PROPERTY_NAME = "keystore.type";

    //  Store default KeyStore type
    private static final String DEFAULT_KEYSTORE_TYPE = "jks";

    // Store KeyStore state (initialized or not)
    private boolean isInit;

    // Store used KeyStoreSpi
    private final KeyStoreSpi implSpi;

    // Store used provider
    private final Provider provider;

    // Store used type
    private final String type;

    /**
     * Constructs a new instance of {@code KeyStore} with the given arguments.
     *
     * @param keyStoreSpi
     *            the concrete key store.
     * @param provider
     *            the provider.
     * @param type
     *            the type of the {@code KeyStore} to be constructed.
     */
    protected KeyStore(KeyStoreSpi keyStoreSpi, Provider provider, String type) {
        this.type = type;
        this.provider = provider;
        this.implSpi = keyStoreSpi;
        isInit = false;
    }

    /**
     * Throws the standard "keystore not initialized" exception.
     */
    private static void throwNotInitialized() throws KeyStoreException {
        throw new KeyStoreException("KeyStore was not initialized");
    }

    /**
     * Returns a new instance of {@code KeyStore} with the specified type.
     *
     * @param type
     *            the type of the returned {@code KeyStore}.
     * @return a new instance of {@code KeyStore} with the specified type.
     * @throws KeyStoreException
     *             if an error occurred during the creation of the new {@code
     *             KeyStore}.
     * @throws NullPointerException if {@code type == null}
     * @see #getDefaultType
     */
    public static KeyStore getInstance(String type) throws KeyStoreException {
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        try {
            Engine.SpiAndProvider sap = ENGINE.getInstance(type, null);
            return new KeyStore((KeyStoreSpi) sap.spi, sap.provider, type);
        } catch (NoSuchAlgorithmException e) {
            throw new KeyStoreException(e);
        }
    }

    /**
     * Returns a new instance of {@code KeyStore} from the specified provider
     * with the given type.
     *
     * @param type
     *            the type of the returned {@code KeyStore}.
     * @param provider
     *            name of the provider of the {@code KeyStore}.
     * @return a new instance of {@code KeyStore} from the specified provider
     *         with the given type.
     * @throws KeyStoreException
     *             if an error occurred during the creation of the new {@code
     *             KeyStore}.
     * @throws NoSuchProviderException
     *             if the specified provider is not available.
     * @throws IllegalArgumentException if {@code provider == null || provider.isEmpty()}
     * @throws NullPointerException
     *             if {@code type} is {@code null} (instead of
     *             NoSuchAlgorithmException) as in 1.4 release
     * @see #getDefaultType
     */
    public static KeyStore getInstance(String type, String provider)
            throws KeyStoreException, NoSuchProviderException {
        if (provider == null || provider.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        try {
            return getInstance(type, impProvider);
        } catch (Exception e) {
            throw new KeyStoreException(e);
        }
    }

    /**
     * Returns a new instance of {@code KeyStore} from the specified provider
     * with the given type. The {@code provider} supplied does not have to be
     * registered.
     *
     * @param type
     *            the type of the returned {@code KeyStore}.
     * @param provider
     *            the provider of the {@code KeyStore}.
     * @return a new instance of {@code KeyStore} from the specified provider
     *         with the given type.
     * @throws KeyStoreException
     *             if an error occurred during the creation of the new {@code
     *             KeyStore}.
     * @throws IllegalArgumentException
     *             if {@code provider} is {@code null} or the empty string.
     * @throws NullPointerException if {@code type == null} (instead of
     *             NoSuchAlgorithmException) as in 1.4 release
     * @see #getDefaultType
     */
    public static KeyStore getInstance(String type, Provider provider) throws KeyStoreException {
        // check parameters
        if (provider == null) {
            throw new IllegalArgumentException("provider == null");
        }
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        // return KeyStore instance
        try {
            Object spi = ENGINE.getInstance(type, provider, null);
            return new KeyStore((KeyStoreSpi) spi, provider, type);
        } catch (Exception e) {
            // override exception
            throw new KeyStoreException(e);
        }
    }

    /**
     * Returns the default type for {@code KeyStore} instances.
     *
     * <p>The default is specified in the {@code 'keystore.type'} property in the
     * file named {@code java.security} properties file. If this property
     * is not set, {@code "jks"} will be used.
     *
     * @return the default type for {@code KeyStore} instances
     */
    public static final String getDefaultType() {
        String dt = Security.getProperty(PROPERTY_NAME);
        return (dt == null ? DEFAULT_KEYSTORE_TYPE : dt);
    }

    /**
     * Returns the provider associated with this {@code KeyStore}.
     *
     * @return the provider associated with this {@code KeyStore}.
     */
    public final Provider getProvider() {
        return provider;
    }

    /**
     * Returns the type of this {@code KeyStore}.
     *
     * @return the type of this {@code KeyStore}.
     */
    public final String getType() {
        return type;
    }

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
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized.
     * @throws NoSuchAlgorithmException
     *             if the algorithm for recovering the key is not available.
     * @throws UnrecoverableKeyException
     *             if the key can not be recovered.
     */
    public final Key getKey(String alias, char[] password)
            throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException {
        if (!isInit) {
            throwNotInitialized();
        }
        return implSpi.engineGetKey(alias, password);
    }

    /**
     * Returns the certificate chain for the entry with the given alias.
     *
     * @param alias
     *            the alias for the entry.
     * @return the certificate chain for the entry with the given alias, or
     *         {@code null} if the specified alias is not bound to an entry.
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized.
     */
    public final Certificate[] getCertificateChain(String alias) throws KeyStoreException {
        if (!isInit) {
            throwNotInitialized();
        }
        return implSpi.engineGetCertificateChain(alias);
    }

    /**
     * Returns the trusted certificate for the entry with the given alias.
     *
     * @param alias
     *            the alias for the entry.
     * @return the trusted certificate for the entry with the given alias, or
     *         {@code null} if the specified alias is not bound to an entry.
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized.
     */
    public final Certificate getCertificate(String alias) throws KeyStoreException {
        if (!isInit) {
            throwNotInitialized();
        }
        return implSpi.engineGetCertificate(alias);
    }

    /**
     * Returns the creation date of the entry with the given alias.
     *
     * @param alias
     *            the alias for the entry.
     * @return the creation date, or {@code null} if the specified alias is not
     *         bound to an entry.
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized.
     */
    public final Date getCreationDate(String alias) throws KeyStoreException {
        if (!isInit) {
            throwNotInitialized();
        }
        return implSpi.engineGetCreationDate(alias);
    }

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
     *             if this {@code KeyStore} is not initialized.
     * @throws IllegalArgumentException
     *             if {@code key} is a {@code PrivateKey} and {@code chain} does
     *             not contain any certificates.
     * @throws NullPointerException
     *             if {@code alias} is {@code null}.
     */
    public final void setKeyEntry(String alias, Key key, char[] password,
            Certificate[] chain) throws KeyStoreException {
        if (!isInit) {
            throwNotInitialized();
        }

        // Certificate chain is required for PrivateKey
        if (key != null && key instanceof PrivateKey && (chain == null || chain.length == 0)) {
            throw new IllegalArgumentException("Certificate chain is not defined for Private key");
        }
        implSpi.engineSetKeyEntry(alias, key, password, chain);
    }

    /**
     * Associates the given alias with a key and a certificate chain.
     * <p>
     * If the specified alias already exists, it will be reassigned.
     * <p>
     * If this {@code KeyStore} is of type {@code "jks"}, {@code key} must be
     * encoded conform to the PKS#8 standard as an
     * {@link javax.crypto.EncryptedPrivateKeyInfo}.
     *
     * @param alias
     *            the alias for the key.
     * @param key
     *            the key in an encoded format.
     * @param chain
     *            the certificate chain.
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized or if {@code key}
     *             is null.
     * @throws IllegalArgumentException
     *             if {@code key} is a {@code PrivateKey} and {@code chain}
     *             does.
     * @throws NullPointerException
     *             if {@code alias} is {@code null}.
     */
    public final void setKeyEntry(String alias, byte[] key, Certificate[] chain)
            throws KeyStoreException {
        if (!isInit) {
            throwNotInitialized();
        }
        implSpi.engineSetKeyEntry(alias, key, chain);
    }

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
     *             if this {@code KeyStore} is not initialized, or an existing
     *             alias is not associated to an entry containing a trusted
     *             certificate, or this method fails for any other reason.
     * @throws NullPointerException
     *             if {@code alias} is {@code null}.
     */
    public final void setCertificateEntry(String alias, Certificate cert) throws KeyStoreException {
        if (!isInit) {
            throwNotInitialized();
        }
        implSpi.engineSetCertificateEntry(alias, cert);
    }

    /**
     * Deletes the entry identified with the given alias from this {@code
     * KeyStore}.
     *
     * @param alias
     *            the alias for the entry.
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized, or if the entry
     *             can not be deleted.
     */
    public final void deleteEntry(String alias) throws KeyStoreException {
        if (!isInit) {
            throwNotInitialized();
        }
        implSpi.engineDeleteEntry(alias);
    }

    /**
     * Returns an {@code Enumeration} over all alias names stored in this
     * {@code KeyStore}.
     *
     * @return an {@code Enumeration} over all alias names stored in this
     *         {@code KeyStore}.
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized.
     */
    public final Enumeration<String> aliases() throws KeyStoreException {
        if (!isInit) {
            throwNotInitialized();
        }
        return implSpi.engineAliases();
    }

    /**
     * Indicates whether the given alias is present in this {@code KeyStore}.
     *
     * @param alias
     *            the alias of an entry.
     * @return {@code true} if the alias exists, {@code false} otherwise.
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized.
     */
    public final boolean containsAlias(String alias) throws KeyStoreException {
        if (!isInit) {
            throwNotInitialized();
        }
        return implSpi.engineContainsAlias(alias);
    }

    /**
     * Returns the number of entries stored in this {@code KeyStore}.
     *
     * @return the number of entries stored in this {@code KeyStore}.
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized.
     */
    public final int size() throws KeyStoreException {
        if (!isInit) {
            throwNotInitialized();
        }
        return implSpi.engineSize();
    }

    /**
     * Indicates whether the specified alias is associated with either a
     * {@link PrivateKeyEntry} or a {@link SecretKeyEntry}.
     *
     * @param alias
     *            the alias of an entry.
     * @return {@code true} if the given alias is associated with a key entry.
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized.
     */
    public final boolean isKeyEntry(String alias) throws KeyStoreException {
        if (!isInit) {
            throwNotInitialized();
        }
        return implSpi.engineIsKeyEntry(alias);
    }

    /**
     * Indicates whether the specified alias is associated with a
     * {@link TrustedCertificateEntry}.
     *
     * @param alias
     *            the alias of an entry.
     * @return {@code true} if the given alias is associated with a certificate
     *         entry.
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized.
     */
    public final boolean isCertificateEntry(String alias) throws KeyStoreException {
        if (!isInit) {
            throwNotInitialized();
        }
        return implSpi.engineIsCertificateEntry(alias);
    }

    /**
     * Returns the alias associated with the first entry whose certificate
     * matches the specified certificate.
     *
     * @param cert
     *            the certificate to find the associated entry's alias for.
     * @return the alias or {@code null} if no entry with the specified
     *         certificate can be found.
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized.
     */
    public final String getCertificateAlias(Certificate cert) throws KeyStoreException {
        if (!isInit) {
            throwNotInitialized();
        }
        return implSpi.engineGetCertificateAlias(cert);
    }

    /**
     * Writes this {@code KeyStore} to the specified {@code OutputStream}. The
     * data written to the {@code OutputStream} is protected by the specified
     * password.
     *
     * @param stream
     *            the {@code OutputStream} to write the store's data to.
     * @param password
     *            the password to protect the data.
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized.
     * @throws IOException
     *             if a problem occurred while writing to the stream.
     * @throws NoSuchAlgorithmException
     *             if the required algorithm is not available.
     * @throws CertificateException
     *             if an exception occurred while storing the certificates of
     *             this {@code KeyStore}.
     */
    public final void store(OutputStream stream, char[] password)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        if (!isInit) {
            throwNotInitialized();
        }

        //Just delegate stream and password to implSpi
        implSpi.engineStore(stream, password);
    }

    /**
     * Stores this {@code KeyStore} using the specified {@code
     * LoadStoreParameter}.
     *
     * @param param
     *            the {@code LoadStoreParameter} that specifies how to store
     *            this {@code KeyStore}, maybe {@code null}.
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized.
     * @throws IOException
     *             if a problem occurred while writing to the stream.
     * @throws NoSuchAlgorithmException
     *             if the required algorithm is not available.
     * @throws CertificateException
     *             if an exception occurred while storing the certificates of
     *             this {@code KeyStore}.
     * @throws IllegalArgumentException
     *             if the given {@link LoadStoreParameter} is not recognized.
     */
    public final void store(LoadStoreParameter param) throws KeyStoreException,
            IOException, NoSuchAlgorithmException, CertificateException {
        if (!isInit) {
            throwNotInitialized();
        }
        implSpi.engineStore(param);
    }

    /**
     * Initializes this {@code KeyStore} from the provided {@code InputStream}.
     * Pass {@code null} as the {@code stream} argument to initialize an empty
     * {@code KeyStore} or to initialize a {@code KeyStore} which does not rely
     * on an {@code InputStream}. This {@code KeyStore} utilizes the given
     * password to verify the stored data.
     *
     * @param stream
     *            the {@code InputStream} to load this {@code KeyStore}'s data
     *            from or {@code null}.
     * @param password
     *            the password to verify the stored data, maybe {@code null}.
     * @throws IOException
     *             if a problem occurred while reading from the stream.
     * @throws NoSuchAlgorithmException
     *             if the required algorithm is not available.
     * @throws CertificateException
     *             if an exception occurred while loading the certificates of
     *             this {@code KeyStore}.
     */
    public final void load(InputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        implSpi.engineLoad(stream, password);
        isInit = true;
    }

    /**
     * Loads this {@code KeyStore} using the specified {@code
     * LoadStoreParameter}.
     *
     * @param param
     *            the {@code LoadStoreParameter} that specifies how to load this
     *            {@code KeyStore}, maybe {@code null}.
     * @throws IOException
     *             if a problem occurred while reading from the stream.
     * @throws NoSuchAlgorithmException
     *             if the required algorithm is not available.
     * @throws CertificateException
     *             if an exception occurred while loading the certificates of
     *             this {@code KeyStore}.
     * @throws IllegalArgumentException
     *             if the given {@link LoadStoreParameter} is not recognized.
     */
    public final void load(LoadStoreParameter param) throws IOException,
            NoSuchAlgorithmException, CertificateException {
        implSpi.engineLoad(param);
        isInit = true;
    }

    /**
     * Returns the {@code Entry} with the given alias, using the specified
     * {@code ProtectionParameter}.
     *
     * @param alias
     *            the alias of the requested entry.
     * @param param
     *            the {@code ProtectionParameter} used to protect the requested
     *            entry, maybe {@code null}.
     * @return he {@code Entry} with the given alias, using the specified
     *         {@code ProtectionParameter}.
     * @throws NoSuchAlgorithmException
     *             if the required algorithm is not available.
     * @throws UnrecoverableEntryException
     *             if the entry can not be recovered.
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized.
     * @throws NullPointerException
     *             if {@code alias} is {@code null}.
     */
    public final Entry getEntry(String alias, ProtectionParameter param)
            throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException {
        if (alias == null) {
            throw new NullPointerException("alias == null");
        }
        if (!isInit) {
            throwNotInitialized();
        }
        return implSpi.engineGetEntry(alias, param);
    }

    /**
     * Stores the given {@code Entry} in this {@code KeyStore} and associates
     * the entry with the given {@code alias}. The entry is protected by the
     * specified {@code ProtectionParameter}.
     * <p>
     * If the specified alias already exists, it will be reassigned.
     *
     * @param alias
     *            the alias for the entry.
     * @param entry
     *            the entry to store.
     * @param param
     *            the {@code ProtectionParameter} to protect the entry.
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized.
     * @throws NullPointerException
     *             if {@code alias} is {@code null} or {@code entry} is {@code
     *             null}.
     */
    public final void setEntry(String alias, Entry entry,
            ProtectionParameter param) throws KeyStoreException {
        if (!isInit) {
            throwNotInitialized();
        }
        if (alias == null) {
            throw new NullPointerException("alias == null");
        }
        if (entry == null) {
            throw new NullPointerException("entry == null");
        }
        implSpi.engineSetEntry(alias, entry, param);
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
     * @throws KeyStoreException
     *             if this {@code KeyStore} is not initialized.
     */
    public final boolean entryInstanceOf(String alias,
            Class<? extends KeyStore.Entry> entryClass)
            throws KeyStoreException {
        if (alias == null) {
            throw new NullPointerException("alias == null");
        }
        if (entryClass == null) {
            throw new NullPointerException("entryClass == null");
        }

        if (!isInit) {
            throwNotInitialized();
        }
        return implSpi.engineEntryInstanceOf(alias, entryClass);
    }

    /**
     * {@code Builder} is used to construct new instances of {@code KeyStore}.
     */
    public abstract static class Builder {
        /**
         * Constructs a new instance of {@code Builder}.
         */
        protected Builder() {
        }

        /**
         * Returns the {@code KeyStore} created by this {@code Builder}.
         *
         * @return the {@code KeyStore} created by this {@code Builder}.
         * @throws KeyStoreException
         *             if an error occurred during construction.
         */
        public abstract KeyStore getKeyStore() throws KeyStoreException;

        /**
         * Returns the {@code ProtectionParameter} to be used when a {@code
         * Entry} with the specified alias is requested. Before this method is
         * invoked, {@link #getKeyStore()} must be called.
         *
         * @param alias
         *            the alias for the entry.
         * @return the {@code ProtectionParameter} to be used when a {@code
         *         Entry} with the specified alias is requested.
         * @throws KeyStoreException
         *             if an error occurred during the lookup for the protection
         *             parameter.
         * @throws IllegalStateException
         *             if {@link #getKeyStore()} is not called prior the
         *             invocation of this method.
         * @throws NullPointerException
         *             if {@code alias} is {@code null}.
         */
        public abstract ProtectionParameter getProtectionParameter(String alias)
                throws KeyStoreException;

        /**
         * Returns a new {@code Builder} that holds the given {@code KeyStore}
         * and the given {@code ProtectionParameter}.
         *
         * @param keyStore
         *            the {@code KeyStore} to be held.
         * @param protectionParameter
         *            the {@code ProtectionParameter} to be held.
         * @return a new instance of {@code Builder} that holds the specified
         *         {@code KeyStore} and the specified {@code
         *         ProtectionParameter}.
         * @throws NullPointerException
         *             if {@code keyStore} or {@code protectionParameter} is
         *             {@code null}.
         * @throws IllegalArgumentException
         *             if the given {@code KeyStore} is not initialized.
         */
        public static Builder newInstance(KeyStore keyStore,
                ProtectionParameter protectionParameter) {
            if (keyStore == null) {
                throw new NullPointerException("keyStore == null");
            }
            if (protectionParameter == null) {
                throw new NullPointerException("protectionParameter == null");
            }
            if (!keyStore.isInit) {
                throw new IllegalArgumentException("KeyStore was not initialized");
            }
            return new BuilderImpl(keyStore, protectionParameter, null, null, null);
        }

        /**
         * Returns a new {@code Builder} that creates a new {@code KeyStore}
         * based on the provided arguments.
         * <p>
         * If {@code provider} is {@code null}, all installed providers are
         * searched, otherwise the key store from the specified provider is
         * used.
         *
         * @param type
         *            the type of the {@code KeyStore} to be constructed.
         * @param provider
         *            the provider of the {@code KeyStore} to be constructed,
         *            maybe {@code null}.
         * @param file
         *            the {@code File} that contains the data for the {@code
         *            KeyStore}.
         * @param protectionParameter
         *            the {@code ProtectionParameter} used to protect the stored
         *            keys.
         * @return a new {@code Builder} that creates a new {@code KeyStore}
         *         based on the provided arguments.
         * @throws NullPointerException
         *             if {@code type, protectionParameter} or {@code file} is
         *             {@code null}.
         * @throws IllegalArgumentException
         *             {@code protectionParameter} not an instance of either
         *             {@code PasswordProtection} or {@code
         *             CallbackHandlerProtection}, {@code file} is not a file or
         *             does not exist at all.
         */
        public static Builder newInstance(String type, Provider provider,
                File file, ProtectionParameter protectionParameter) {
            // check null parameters
            if (type == null) {
                throw new NullPointerException("type == null");
            }
            if (protectionParameter == null) {
                throw new NullPointerException("protectionParameter == null");
            }
            if (file == null) {
                throw new NullPointerException("file == null");
            }
            // protection parameter should be PasswordProtection or
            // CallbackHandlerProtection
            if (!(protectionParameter instanceof PasswordProtection)
                    && !(protectionParameter instanceof CallbackHandlerProtection)) {
                throw new IllegalArgumentException("protectionParameter is neither "
                        + "PasswordProtection nor CallbackHandlerProtection instance");
            }
            // check file parameter
            if (!file.exists()) {
                throw new IllegalArgumentException("File does not exist: " + file.getName());
            }
            if (!file.isFile()) {
                throw new IllegalArgumentException("Not a regular file: " + file.getName());
            }
            // create new instance
            return new BuilderImpl(null, protectionParameter, file, type, provider);
        }

        /**
         * Returns a new {@code Builder} that creates a new {@code KeyStore}
         * based on the provided arguments.
         * <p>
         * If {@code provider} is {@code null}, all installed providers are
         * searched, otherwise the key store from the specified provider is
         * used.
         *
         * @param type
         *            the type of the {@code KeyStore} to be constructed.
         * @param provider
         *            the provider of the {@code KeyStore} to be constructed,
         *            maybe {@code null}.
         * @param protectionParameter
         *            the {@code ProtectionParameter} used to protect the stored
         *            keys.
         * @return a new {@code Builder} that creates a new {@code KeyStore}
         *         based on the provided arguments.
         * @throws NullPointerException
         *             if {@code type} or {@code protectionParameter} is {@code
         *             null}.
         * @throws IllegalArgumentException
         *             {@code protectionParameter} not an instance of either
         *             {@code PasswordProtection} or {@code
         *             CallbackHandlerProtection}, {@code file} is not a file or
         *             does not exist at all.
         */
        public static Builder newInstance(String type, Provider provider,
                ProtectionParameter protectionParameter) {
            if (type == null) {
                throw new NullPointerException("type == null");
            }
            if (protectionParameter == null) {
                throw new NullPointerException("protectionParameter == null");
            }
            return new BuilderImpl(null, protectionParameter, null, type, provider);
        }

        /*
         * This class is implementation of abstract class KeyStore.Builder
         *
         * @author Vera Petrashkova
         *
         */
        private static class BuilderImpl extends Builder {
            // Store used KeyStore
            private KeyStore keyStore;

            // Store used ProtectionParameter
            private ProtectionParameter protParameter;

            // Store used KeyStore type
            private final String typeForKeyStore;

            // Store used KeyStore provider
            private final Provider providerForKeyStore;

            // Store used file for KeyStore loading
            private final File fileForLoad;

            // Store getKeyStore method was invoked or not for KeyStoreBuilder
            private boolean isGetKeyStore = false;

            // Store last Exception in getKeyStore()
            private KeyStoreException lastException;

            /**
             * Constructor BuilderImpl initializes private fields: keyStore,
             * protParameter, typeForKeyStore providerForKeyStore fileForLoad,
             * isGetKeyStore
             */
            BuilderImpl(KeyStore ks, ProtectionParameter pp, File file,
                        String type, Provider provider) {
                keyStore = ks;
                protParameter = pp;
                fileForLoad = file;
                typeForKeyStore = type;
                providerForKeyStore = provider;
                isGetKeyStore = false;
                lastException = null;
            }

            /**
             * Implementation of abstract getKeyStore() method If
             * KeyStoreBuilder encapsulates KeyStore object then this object is
             * returned
             *
             * If KeyStoreBuilder encapsulates KeyStore type and provider then
             * KeyStore is created using these parameters. If KeyStoreBuilder
             * encapsulates file and ProtectionParameter then KeyStore data are
             * loaded from FileInputStream that is created on file. If file is
             * not defined then KeyStore object is initialized with null
             * InputStream and null password.
             *
             * Result KeyStore object is returned.
             */
            @Override
            public synchronized KeyStore getKeyStore() throws KeyStoreException {
                // If KeyStore was created but in final block some exception was
                // thrown
                // then it was stored in lastException variable and will be
                // thrown
                // all subsequent calls of this method.
                if (lastException != null) {
                    throw lastException;
                }
                if (keyStore != null) {
                    isGetKeyStore = true;
                    return keyStore;
                }

                try {
                    // get KeyStore instance using type or type and provider
                    final KeyStore ks = (providerForKeyStore == null ? KeyStore
                            .getInstance(typeForKeyStore) : KeyStore
                            .getInstance(typeForKeyStore, providerForKeyStore));
                    // protection parameter should be PasswordProtection
                    // or CallbackHandlerProtection
                    final char[] passwd;
                    if (protParameter instanceof PasswordProtection) {
                        passwd = ((PasswordProtection) protParameter)
                                .getPassword();
                    } else if (protParameter instanceof CallbackHandlerProtection) {
                        passwd = KeyStoreSpi
                                .getPasswordFromCallBack(protParameter);
                    } else {
                        throw new KeyStoreException("protectionParameter is neither "
                                + "PasswordProtection nor CallbackHandlerProtection instance");
                    }

                    // load KeyStore from file
                    if (fileForLoad != null) {
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(fileForLoad);
                            ks.load(fis, passwd);
                        } finally {
                            IoUtils.closeQuietly(fis);
                        }
                    } else {
                        ks.load(new TmpLSParameter(protParameter));
                    }

                    isGetKeyStore = true;
                    return ks;
                } catch (KeyStoreException e) {
                    // Store exception
                    throw lastException = e;
                } catch (Exception e) {
                    // Override exception
                    throw lastException = new KeyStoreException(e);
                }
            }

            /**
             * This is implementation of abstract method
             * getProtectionParameter(String alias)
             *
             * Return: ProtectionParameter to get Entry which was saved in
             * KeyStore with defined alias
             */
            @Override
            public synchronized ProtectionParameter getProtectionParameter(
                    String alias) throws KeyStoreException {
                if (alias == null) {
                    throw new NullPointerException("alias == null");
                }
                if (!isGetKeyStore) {
                    throw new IllegalStateException("getKeyStore() was not invoked");
                }
                return protParameter;
            }
        }

        /*
         * Implementation of LoadStoreParameter interface
         */
        private static class TmpLSParameter implements LoadStoreParameter {

            // Store used protection parameter
            private final ProtectionParameter protPar;

            /**
             * Creates TmpLoadStoreParameter object
             * @param protPar protection parameter
             */
            public TmpLSParameter(ProtectionParameter protPar) {
                this.protPar = protPar;
            }

            /**
             * This method returns protection parameter
             */
            public ProtectionParameter getProtectionParameter() {
                return protPar;
            }
        }
    }

    /**
     * {@code CallbackHandlerProtection} is a {@code ProtectionParameter} that
     * encapsulates a {@link CallbackHandler}.
     */
    public static class CallbackHandlerProtection implements
            ProtectionParameter {
        // Store CallbackHandler
        private final CallbackHandler callbackHandler;

        /**
         * Constructs a new instance of {@code CallbackHandlerProtection} with
         * the {@code CallbackHandler}.
         *
         * @param handler
         *            the {@code CallbackHandler}.
         * @throws NullPointerException
         *             if {@code handler} is {@code null}.
         */
        public CallbackHandlerProtection(CallbackHandler handler) {
            if (handler == null) {
                throw new NullPointerException("handler == null");
            }
            this.callbackHandler = handler;
        }

        /**
         * Returns the {@code CallbackHandler}.
         *
         * @return the {@code CallbackHandler}.
         */
        public CallbackHandler getCallbackHandler() {
            return callbackHandler;
        }
    }

    /**
     * {@code Entry} is the common marker interface for a {@code KeyStore}
     * entry.
     */
    public static interface Entry {
    }

    /**
     * {@code LoadStoreParameter} represents a parameter that specifies how a
     * {@code KeyStore} can be loaded and stored.
     *
     * @see KeyStore#load(LoadStoreParameter)
     * @see KeyStore#store(LoadStoreParameter)
     */
    public static interface LoadStoreParameter {
        /**
         * Returns the {@code ProtectionParameter} which is used to protect data
         * in the {@code KeyStore}.
         *
         * @return the {@code ProtectionParameter} which is used to protect data
         *         in the {@code KeyStore}, maybe {@code null}.
         */
        public ProtectionParameter getProtectionParameter();
    }

    /**
     * {@code PasswordProtection} is a {@code ProtectionParameter} that protects
     * a {@code KeyStore} using a password.
     */
    public static class PasswordProtection implements ProtectionParameter,
            Destroyable {

        // Store password
        private char[] password;

        private boolean isDestroyed = false;

        /**
         * Constructs a new instance of {@code PasswordProtection} with a
         * password. A copy of the password is stored in the new {@code
         * PasswordProtection} object.
         *
         * @param password
         *            the password, maybe {@code null}.
         */
        public PasswordProtection(char[] password) {
            if (password != null) {
                this.password = password.clone();
            }
        }

        /**
         * Returns the password.
         *
         * @return the password.
         * @throws IllegalStateException
         *             if the password has been destroyed.
         */
        public synchronized char[] getPassword() {
            if (isDestroyed) {
                throw new IllegalStateException("Password was destroyed");
            }
            return password;
        }

        /**
         * Destroys / invalidates the password.
         *
         * @throws DestroyFailedException
         *             if the password could not be invalidated.
         */
        public synchronized void destroy() throws DestroyFailedException {
            isDestroyed = true;
            if (password != null) {
                Arrays.fill(password, '\u0000');
                password = null;
            }
        }

        /**
         * Indicates whether the password is invalidated.
         *
         * @return {@code true} if the password is invalidated, {@code false}
         *         otherwise.
         */
        public synchronized boolean isDestroyed() {
            return isDestroyed;
        }
    }

    /**
     * {@code ProtectionParameter} is a marker interface for protection
     * parameters. A protection parameter is used to protect the content of a
     * {@code KeyStore}.
     */
    public static interface ProtectionParameter {
    }

    /**
     * {@code PrivateKeyEntry} represents a {@code KeyStore} entry that
     * holds a private key.
     */
    public static final class PrivateKeyEntry implements Entry {
        // Store Certificate chain
        private Certificate[] chain;

        // Store PrivateKey
        private PrivateKey privateKey;

        /**
         * Constructs a new instance of {@code PrivateKeyEntry} with the given
         * {@code PrivateKey} and the provided certificate chain.
         *
         * @param privateKey
         *            the private key.
         * @param chain
         *            the ordered certificate chain with the certificate
         *            corresponding to the private key at index 0.
         * @throws NullPointerException
         *             if {@code privateKey} or {@code chain} is {@code null}.
         * @throws IllegalArgumentException
         *             if {@code chain.length == 0}, the algorithm of the
         *             private key does not match the algorithm of the public
         *             key of the first certificate or the certificates are not
         *             all of the same type.
         */
        public PrivateKeyEntry(PrivateKey privateKey, Certificate[] chain) {
            if (privateKey == null) {
                throw new NullPointerException("privateKey == null");
            }
            if (chain == null) {
                throw new NullPointerException("chain == null");
            }

            if (chain.length == 0) {
                throw new IllegalArgumentException("chain.length == 0");
            }
            // Match algorithm of private key and algorithm of public key from
            // the end certificate
            String s = chain[0].getType();
            if (!(chain[0].getPublicKey().getAlgorithm()).equals(privateKey.getAlgorithm())) {
                throw new IllegalArgumentException("Algorithm of private key does not match "
                        + "algorithm of public key in end certificate of entry "
                        + "(with index number: 0)");
            }
            // Match certificate types
            for (int i = 1; i < chain.length; i++) {
                if (!s.equals(chain[i].getType())) {
                    throw new IllegalArgumentException("Certificates from the given chain have "
                                                       + "different types");
                }
            }
            // clone chain - this.chain = (Certificate[])chain.clone();
            boolean isAllX509Certificates = true;
            // assert chain length > 0
            for (Certificate cert: chain) {
                if (!(cert instanceof X509Certificate)) {
                    isAllX509Certificates = false;
                    break;
                }
            }

            if(isAllX509Certificates){
                this.chain = new X509Certificate[chain.length];
            } else {
                this.chain = new Certificate[chain.length];
            }
            System.arraycopy(chain, 0, this.chain, 0, chain.length);
            this.privateKey = privateKey;
        }

        /**
         * Returns the private key.
         *
         * @return the private key.
         */
        public PrivateKey getPrivateKey() {
            return privateKey;
        }

        /**
         * Returns the certificate chain.
         *
         * @return the certificate chain.
         */
        public Certificate[] getCertificateChain() {
            return chain.clone();
        }

        /**
         * Returns the certificate corresponding to the private key.
         *
         * @return the certificate corresponding to the private key.
         */
        public Certificate getCertificate() {
            return chain[0];
        }

        /**
         * Returns a string containing a concise, human-readable description of
         * this {@code PrivateKeyEntry}.
         *
         * @return a printable representation for this {@code PrivateKeyEntry}.
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(
                    "PrivateKeyEntry: number of elements in certificate chain is ");
            sb.append(Integer.toString(chain.length));
            sb.append("\n");
            for (int i = 0; i < chain.length; i++) {
                sb.append(chain[i].toString());
                sb.append("\n");
            }
            return sb.toString();
        }
    }

    /**
     * {@code SecretKeyEntry} represents a {@code KeyStore} entry that
     * holds a secret key.
     */
    public static final class SecretKeyEntry implements Entry {

        // Store SecretKey
        private final SecretKey secretKey;

        /**
         * Constructs a new instance of {@code SecretKeyEntry} with the given
         * {@code SecretKey}.
         *
         * @param secretKey
         *            the secret key.
         * @throws NullPointerException
         *             if {@code secretKey} is {@code null}.
         */
        public SecretKeyEntry(SecretKey secretKey) {
            if (secretKey == null) {
                throw new NullPointerException("secretKey == null");
            }
            this.secretKey = secretKey;
        }

        /**
         * Returns the secret key.
         *
         * @return the secret key.
         */
        public SecretKey getSecretKey() {
            return secretKey;
        }

        /**
         * Returns a string containing a concise, human-readable description of
         * this {@code SecretKeyEntry}.
         *
         * @return a printable representation for this {@code
         *         SecretKeyEntry}.
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("SecretKeyEntry: algorithm - ");
            sb.append(secretKey.getAlgorithm());
            return sb.toString();
        }
    }

    /**
     * {@code TrustedCertificateEntry} represents a {@code KeyStore} entry that
     * holds a trusted certificate.
     */
    public static final class TrustedCertificateEntry implements Entry {

        // Store trusted Certificate
        private final Certificate trustCertificate;

        /**
         * Constructs a new instance of {@code TrustedCertificateEntry} with the
         * given {@code Certificate}.
         *
         * @param trustCertificate
         *            the trusted certificate.
         * @throws NullPointerException
         *             if {@code trustCertificate} is {@code null}.
         */
        public TrustedCertificateEntry(Certificate trustCertificate) {
            if (trustCertificate == null) {
                throw new NullPointerException("trustCertificate == null");
            }
            this.trustCertificate = trustCertificate;
        }

        /**
         * Returns the trusted certificate.
         *
         * @return the trusted certificate.
         */
        public Certificate getTrustedCertificate() {
            return trustCertificate;
        }

        /**
         * Returns a string containing a concise, human-readable description of
         * this {@code TrustedCertificateEntry}.
         *
         * @return a printable representation for this {@code
         *         TrustedCertificateEntry}.
         */
        @Override
        public String toString() {
            return "Trusted certificate entry:\n" + trustCertificate;
        }
    }
}
