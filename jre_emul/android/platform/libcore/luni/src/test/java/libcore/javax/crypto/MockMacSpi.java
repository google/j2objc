/*
 * Copyright (C) 2015 The Android Open Source Project
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
 * limitations under the License
 */

package libcore.javax.crypto;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.MacSpi;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

/**
 * Mock CipherSpi used by {@link libcore.javax.crypto.CipherTest}.
 */
public class MockMacSpi extends MacSpi {
    public static class SpecificKeyTypes extends MockMacSpi {
        @Override
        public void checkKeyType(Key key) throws InvalidKeyException {
            if (!(key instanceof MockKey)) {
                throw new InvalidKeyException("Must be MockKey!");
            }
        }
    }

    public static class SpecificKeyTypes2 extends MockMacSpi {
        @Override
        public void checkKeyType(Key key) throws InvalidKeyException {
            System.err.println("Checking key of type " + key.getClass().getName());
            if (!(key instanceof MockKey2)) {
                throw new InvalidKeyException("Must be MockKey2!");
            }
        }
    }

    public static class AllKeyTypes extends MockMacSpi {
    }

    public void checkKeyType(Key key) throws InvalidKeyException {
    }

    @Override
    protected int engineGetMacLength() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected void engineInit(Key key, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected void engineUpdate(byte input) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected void engineUpdate(byte[] input, int inputOffset, int inputLen) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected byte[] engineDoFinal() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected void engineReset() {
        throw new UnsupportedOperationException("not implemented");
    }
}
