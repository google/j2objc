/*
 * Copyright 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.javax.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.KeyAgreementSpi;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

/**
 * Mock KeyAgreementSpi used by {@link KeyAgreementTest}.
 */
public class MockKeyAgreementSpi extends KeyAgreementSpi {
    public static class SpecificKeyTypes extends MockKeyAgreementSpi {
        @Override
        public void checkKeyType(Key key) throws InvalidKeyException {
            if (!(key instanceof MockKey)) {
                throw new InvalidKeyException("Must be MockKey!");
            }
        }
    }

    public static class SpecificKeyTypes2 extends MockKeyAgreementSpi {
        @Override
        public void checkKeyType(Key key) throws InvalidKeyException {
            System.err.println("Checking key of type " + key.getClass().getName());
            if (!(key instanceof MockKey2)) {
                throw new InvalidKeyException("Must be MockKey2!");
            }
        }
    }

    public static class AllKeyTypes extends MockKeyAgreementSpi {
    }

    public void checkKeyType(Key key) throws InvalidKeyException {
    }

    @Override
    protected Key engineDoPhase(Key key, boolean lastPhase) throws InvalidKeyException,
            IllegalStateException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected byte[] engineGenerateSecret() throws IllegalStateException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected int engineGenerateSecret(byte[] sharedSecret, int offset)
            throws IllegalStateException, ShortBufferException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected SecretKey engineGenerateSecret(String algorithm) throws IllegalStateException,
            NoSuchAlgorithmException, InvalidKeyException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected void engineInit(Key key, SecureRandom random) throws InvalidKeyException {
        checkKeyType(key);
    }

    @Override
    protected void engineInit(Key key, AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        checkKeyType(key);
    }
}
