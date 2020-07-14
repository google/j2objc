/*
 * Copyright 2014 The Android Open Source Project
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

package org.apache.harmony.crypto.tests.javax.crypto;

import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.MacSpi;
import libcore.javax.crypto.MockKey;
import libcore.javax.crypto.MockKey2;

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
        throw new UnsupportedOperationException();
    }

    @Override
    protected void engineInit(Key key, AlgorithmParameterSpec params) throws InvalidKeyException,
            InvalidParameterException {
        checkKeyType(key);
    }

    @Override
    protected void engineUpdate(byte input) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void engineUpdate(byte[] input, int offset, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected byte[] engineDoFinal() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void engineReset() {
        throw new UnsupportedOperationException();
    }
}
