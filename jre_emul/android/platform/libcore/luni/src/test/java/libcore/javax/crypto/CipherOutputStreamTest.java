/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.javax.crypto;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class CipherOutputStreamTest extends TestCase {

    // From b/36636576. CipherOutputStream had a bug where it would ignore exceptions
    // thrown during close().
    public void testDecryptCorruptGCM() throws Exception {
        for (Provider provider : Security.getProviders()) {
            Cipher cipher;
            try {
                cipher = Cipher.getInstance("AES/GCM/NoPadding", provider);
            } catch (NoSuchAlgorithmException e) {
                continue;
            }
            SecretKey key;
            if (provider.getName().equals("AndroidKeyStoreBCWorkaround")) {
                key = getAndroidKeyStoreSecretKey();
            } else {
                KeyGenerator keygen = KeyGenerator.getInstance("AES");
                keygen.init(256);
                key = keygen.generateKey();
            }
            GCMParameterSpec params = new GCMParameterSpec(128, new byte[12]);
            byte[] unencrypted = new byte[200];

            // Normal providers require specifying the IV, but KeyStore prohibits it, so
            // we have to special-case it
            if (provider.getName().equals("AndroidKeyStoreBCWorkaround")) {
                cipher.init(Cipher.ENCRYPT_MODE, key);
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, key, params);
            }
            byte[] encrypted = cipher.doFinal(unencrypted);

            // Corrupt the final byte, which will corrupt the authentication tag
            encrypted[encrypted.length - 1] ^= 1;

            cipher.init(Cipher.DECRYPT_MODE, key, params);
            CipherOutputStream cos = new CipherOutputStream(new ByteArrayOutputStream(), cipher);
            try {
                cos.write(encrypted);
                cos.close();
                fail("Writing a corrupted stream should throw an exception."
                        + "  Provider: " + provider);
            } catch (IOException expected) {
                assertTrue(expected.getCause() instanceof AEADBadTagException);
            }
        }

    }

    // The AndroidKeyStoreBCWorkaround provider can't use keys created by anything
    // but Android KeyStore, which requires using its own parameters class to create
    // keys.  Since we're in javax, we can't link against the frameworks classes, so
    // we have to use reflection to make a suitable key.  This will always be safe
    // because if we're making a key for AndroidKeyStoreBCWorkaround, the KeyStore
    // classes must be present.
    private static SecretKey getAndroidKeyStoreSecretKey() throws Exception {
        KeyGenerator keygen = KeyGenerator.getInstance("AES", "AndroidKeyStore");
        Class<?> keyParamsBuilderClass = keygen.getClass().getClassLoader().loadClass(
                "android.security.keystore.KeyGenParameterSpec$Builder");
        Object keyParamsBuilder = keyParamsBuilderClass.getConstructor(String.class, Integer.TYPE)
                // 3 is PURPOSE_ENCRYPT | PURPOSE_DECRYPT
                .newInstance("testDecryptCorruptGCM", 3);
        keyParamsBuilderClass.getMethod("setBlockModes", new Class[]{String[].class})
                .invoke(keyParamsBuilder, new Object[]{new String[]{"GCM"}});
        keyParamsBuilderClass.getMethod("setEncryptionPaddings", new Class[]{String[].class})
                .invoke(keyParamsBuilder, new Object[]{new String[]{"NoPadding"}});
        AlgorithmParameterSpec spec = (AlgorithmParameterSpec)
                keyParamsBuilderClass.getMethod("build", new Class[]{}).invoke(keyParamsBuilder);
        keygen.init(spec);
        return keygen.generateKey();
    }
}
