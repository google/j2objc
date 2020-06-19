package org.apache.harmony.security.tests.support;

import org.apache.harmony.security.tests.support.cert.MyCertificate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.KeyStore.LoadStoreParameter;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.ProtectionParameter;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

public class TestKeyStoreSpi extends KeyStoreSpi {

    Map<String, Object> aliases = new HashMap<String, Object>();

    public static final Certificate CERT = new MyCertificate("certtype",
            new byte[] {});
    public static final Certificate[] CERTCHAIN = new Certificate[] {
            new MyCertificate("cert1", new byte[] {}),
            new MyCertificate("cert2", new byte[] {})};

    public static final Key KEY = new SecretKey() {

        public String getAlgorithm() {
            return "secret";
        }

        public byte[] getEncoded() {
            return new byte[] {42};
        }

        public String getFormat() {
            return "format";
        }

    };

    public static final Object DUMMY = new Object();

    public TestKeyStoreSpi() {
        aliases.put("certalias", CERT);
        aliases.put("chainalias", CERTCHAIN);
        aliases.put("keyalias", KEY);
        aliases.put("unknownalias", DUMMY);
    }

    @Override
    public Enumeration<String> engineAliases() {
        return Collections.enumeration(aliases.keySet());
    }

    @Override
    public boolean engineContainsAlias(String alias) {
        return aliases.containsKey(alias);
    }

    @Override
    public void engineDeleteEntry(String alias) throws KeyStoreException {
        throw new KeyStoreException("entry " + alias + " cannot be deleted");
    }

    @Override
    public Certificate engineGetCertificate(String alias) {
        return (Certificate) aliases.get(alias);
    }

    @Override
    public String engineGetCertificateAlias(Certificate cert) {
        if (cert == null) {
            throw new NullPointerException();
        }

        for (Map.Entry<String, Object> alias : aliases.entrySet()) {
            if (alias.getValue() == cert) {
                return alias.getKey();
            }
        }

        return null;
    }

    @Override
    public Certificate[] engineGetCertificateChain(String alias) {
        return (Certificate[]) aliases.get(alias);
    }

    @Override
    public Date engineGetCreationDate(String alias) {
        return new Date(42 * 1024 * 1024);
    }

    @Override
    public Key engineGetKey(String alias, char[] password)
            throws NoSuchAlgorithmException, UnrecoverableKeyException {
        if (engineContainsAlias(alias)) {
            if (!engineIsKeyEntry(alias)) {
                if (password == null) {
                    throw new NoSuchAlgorithmException("no such alg");
                } else {
                    throw new UnrecoverableKeyException();
                }

            }
            return (Key) aliases.get(alias);
        }

        throw new UnrecoverableKeyException();
    }

    @Override
    public boolean engineIsCertificateEntry(String alias) {
        try {
            Certificate c = (Certificate) aliases.get(alias);
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public boolean engineIsKeyEntry(String alias) {
        try {
            Key k = (Key) aliases.get(alias);
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public void engineLoad(InputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        if (stream != null) {
            if (stream.available() == 0)
            {
                throw new IOException();
            }
        }
        if (password == null) {
            throw new NoSuchAlgorithmException();
        } else if (password.length == 0) {
            throw new CertificateException();
        }
    }

    @Override
    public void engineLoad(LoadStoreParameter param) throws IOException,
            NoSuchAlgorithmException, CertificateException {
        if (param == null) {
            engineLoad(null, null);
            return;
        }

        ProtectionParameter pParam = param.getProtectionParameter();
        if (pParam == null) {
            throw new NoSuchAlgorithmException();
        }

        if (pParam instanceof PasswordProtection) {
            char[] password = ((PasswordProtection) pParam).getPassword();
            if (password == null) {
                throw new NoSuchAlgorithmException();
            } else {
                return;
            }
        }
        throw new CertificateException();
    }

    @Override
    public void engineSetCertificateEntry(String alias, Certificate cert)
            throws KeyStoreException {
        if (engineContainsAlias(alias)) {
            if (!engineIsCertificateEntry(alias)) {
                throw new KeyStoreException("alias is not a cert entry");
            }
        }
        aliases.put(alias, cert);
    }

    @Override
    public void engineSetKeyEntry(String alias, Key key, char[] password,
            Certificate[] chain) throws KeyStoreException {
        if (engineContainsAlias(alias)) {
            if (!engineIsKeyEntry(alias)) {
                throw new KeyStoreException("alias is not a key enrty");
            }
        }

        if (key instanceof PrivateKey)
        {
            if (chain == null || chain.length == 0) {
                throw new IllegalArgumentException();
            }
        }

        aliases.put(alias, key);
    }

    @Override
    public void engineSetKeyEntry(String alias, byte[] key, Certificate[] chain)
            throws KeyStoreException {
        throw new KeyStoreException("set entry failed");
    }

    @Override
    public int engineSize() {
        return aliases.size();
    }

    @Override
    public void engineStore(OutputStream stream, char[] password)
            throws IOException, NoSuchAlgorithmException, CertificateException {
        if (stream == null) {
            throw new IOException("store failed");
        }

        if (password == null) {
            throw new NoSuchAlgorithmException();
        } else if (password.length == 0) {
            throw new CertificateException();
        }
    }

    @Override
    public void engineStore(LoadStoreParameter param) throws IOException,
            NoSuchAlgorithmException, CertificateException {
        if (param == null) {
            throw new IOException();
        }

        ProtectionParameter pParam = param.getProtectionParameter();
        if (pParam instanceof PasswordProtection) {
            char[] password = ((PasswordProtection) pParam).getPassword();
            if (password == null) {
                throw new NoSuchAlgorithmException();
            } else if (password.length == 0) {
                throw new CertificateException();
            }
            return;
        }
        throw new UnsupportedOperationException();
    }

}
