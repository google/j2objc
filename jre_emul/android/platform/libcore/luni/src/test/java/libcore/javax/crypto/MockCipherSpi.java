/*
 * Copyright 2013 The Android Open Source Project
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

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherSpi;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

/**
 * Mock CipherSpi used by {@link CipherTest}.
 */
public class MockCipherSpi extends CipherSpi {
    public static class SpecificKeyTypes extends MockCipherSpi {
        @Override
        public void checkKeyType(Key key) throws InvalidKeyException {
            if (!(key instanceof MockKey)) {
                throw new InvalidKeyException("Must be MockKey!");
            }
        }
    }

    public static class SpecificKeyTypes2 extends MockCipherSpi {
        @Override
        public void checkKeyType(Key key) throws InvalidKeyException {
            System.err.println("Checking key of type " + key.getClass().getName());
            if (!(key instanceof MockKey2)) {
                throw new InvalidKeyException("Must be MockKey2!");
            }
        }
    }

    public static class SpecificAlgorithmParameterSpecTypes extends MockCipherSpi {
        @Override
        public void checkAlgorithmParameterSpec(AlgorithmParameterSpec aps)
                throws InvalidAlgorithmParameterException {
            if (!(aps instanceof MockAlgorithmParameterSpec)) {
                throw new InvalidAlgorithmParameterException("Must be "
                        + MockAlgorithmParameterSpec.class.getName());
            }
        }
    }

    public static class SpecificAlgorithmParameterSpecTypes2 extends MockCipherSpi {
        @Override
        public void checkAlgorithmParameterSpec(AlgorithmParameterSpec aps)
                throws InvalidAlgorithmParameterException {
            if (!(aps instanceof MockAlgorithmParameterSpec2)) {
                throw new InvalidAlgorithmParameterException("Must be "
                        + MockAlgorithmParameterSpec2.class.getName());
            }
        }
    }

    public static class SpecificAlgorithmParameterAesAlgorithm extends MockCipherSpi {
        @Override
        public void checkAlgorithmParameters(AlgorithmParameters ap)
                throws InvalidAlgorithmParameterException {
            if (!ap.getAlgorithm().equals("AES")) {
                throw new InvalidAlgorithmParameterException("Must be AES");
            }
        }
    }

    public static class SpecificAlgorithmParametersDesAlgorithm extends MockCipherSpi {
        @Override
        public void checkAlgorithmParameters(AlgorithmParameters ap)
                throws InvalidAlgorithmParameterException {
            if ((!ap.getAlgorithm().equals("DES"))) {
                throw new InvalidAlgorithmParameterException("Must be DES");
            }
        }
    }

    public static class AllKeyTypes extends MockCipherSpi {
    }

    public static class MustInitWithAlgorithmParameterSpec_RejectsAll extends MockCipherSpi {
        @Override
        protected void engineInit(int opmode, Key key, SecureRandom random)
                throws InvalidKeyException {
            throw new AssertionError("Must have AlgorithmParameterSpec");
        }

        @Override
        protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params,
                SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            throw new InvalidAlgorithmParameterException("expected rejection");
        }

        @Override
        protected void engineInit(int opmode, Key key, AlgorithmParameters params,
                SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            throw new AssertionError("Must have AlgorithmParameterSpec");
        }
    }

    public static class MustInitWithAlgorithmParameters_RejectsAll extends MockCipherSpi {
        @Override
        protected void engineInit(int opmode, Key key, SecureRandom random)
                throws InvalidKeyException {
            throw new AssertionError("Must have AlgorithmParameterSpec");
        }

        @Override
        protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params,
                SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            throw new AssertionError("Must have AlgorithmParameterSpec");
        }

        @Override
        protected void engineInit(int opmode, Key key, AlgorithmParameters params,
                SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            throw new InvalidAlgorithmParameterException("expected rejection");
        }
    }

    public static class MustInitWithAlgorithmParameters_ThrowsNull extends MockCipherSpi {
        @Override
        protected void engineInit(int opmode, Key key, SecureRandom random)
                throws InvalidKeyException {
            throw new NullPointerException("expected rejection");
        }

        @Override
        protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params,
                SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            throw new NullPointerException("expected rejection");
        }

        @Override
        protected void engineInit(int opmode, Key key, AlgorithmParameters params,
                SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            throw new NullPointerException("expected rejection");
        }
    }

    public static class MustInitForEncryptModeOrRejects extends MockCipherSpi {
        @Override
        protected void engineInit(int opmode, Key key, SecureRandom random)
                throws InvalidKeyException {
            if (opmode != Cipher.ENCRYPT_MODE) {
                throw new InvalidKeyException("expected rejection");
            }
        }

        @Override
        protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params,
                SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            if (opmode != Cipher.ENCRYPT_MODE) {
                throw new InvalidKeyException("expected rejection");
            }
        }

        @Override
        protected void engineInit(int opmode, Key key, AlgorithmParameters params,
                SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
            if (opmode != Cipher.ENCRYPT_MODE) {
                throw new InvalidKeyException("expected rejection");
            }
        }
    }

    public void checkKeyType(Key key) throws InvalidKeyException {
    }

    public void checkAlgorithmParameterSpec(AlgorithmParameterSpec aps)
            throws InvalidAlgorithmParameterException {
    }

    public void checkAlgorithmParameters(AlgorithmParameters ap)
            throws InvalidAlgorithmParameterException {
    }


    @Override
    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        if (!"FOO".equals(mode)) {
            throw new UnsupportedOperationException("not implemented");
        }
    }

    @Override
    protected void engineSetPadding(String padding) throws NoSuchPaddingException {
        if (!"FOO".equals(padding)) {
            throw new UnsupportedOperationException("not implemented");
        }
    }

    @Override
    protected int engineGetBlockSize() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected int engineGetOutputSize(int inputLen) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected byte[] engineGetIV() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected AlgorithmParameters engineGetParameters() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        checkKeyType(key);
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params,
            SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        checkKeyType(key);
        checkAlgorithmParameterSpec(params);
    }

    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        checkKeyType(key);
        checkAlgorithmParameters(params);
    }

    @Override
    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output,
            int outputOffset) throws ShortBufferException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen)
            throws IllegalBlockSizeException, BadPaddingException {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output,
            int outputOffset) throws ShortBufferException, IllegalBlockSizeException,
            BadPaddingException {
        throw new UnsupportedOperationException("not implemented");
    }

    public static class MockAlgorithmParameterSpec implements AlgorithmParameterSpec {
    }

    public static class MockAlgorithmParameterSpec2 implements AlgorithmParameterSpec {
    }
}
