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

import java.security.InvalidKeyException;
import java.security.Provider;
import java.security.Security;

import javax.crypto.KeyAgreement;

import junit.framework.TestCase;

public class KeyAgreementTest extends TestCase {
    private static abstract class MockProvider extends Provider {
        public MockProvider(String name) {
            super(name, 1.0, "Mock provider used for testing");
            setup();
        }

        public abstract void setup();
    }

    public void testKeyAgreement_getInstance_SuppliedProviderNotRegistered_Success()
            throws Exception {
        Provider mockProvider = new MockProvider("MockProvider") {
            @Override
            public void setup() {
                put("KeyAgreement.FOO", MockKeyAgreementSpi.AllKeyTypes.class.getName());
            }
        };

        {
            KeyAgreement c = KeyAgreement.getInstance("FOO", mockProvider);
            c.init(new MockKey());
            assertEquals(mockProvider, c.getProvider());
        }
    }

    public void testKeyAgreement_getInstance_DoesNotSupportKeyClass_Success()
            throws Exception {
        Provider mockProvider = new MockProvider("MockProvider") {
            @Override
            public void setup() {
                put("KeyAgreement.FOO", MockKeyAgreementSpi.AllKeyTypes.class.getName());
                put("KeyAgreement.FOO SupportedKeyClasses", "none");
            }
        };

        Security.addProvider(mockProvider);
        try {
            KeyAgreement c = KeyAgreement.getInstance("FOO", mockProvider);
            c.init(new MockKey());
            assertEquals(mockProvider, c.getProvider());
        } finally {
            Security.removeProvider(mockProvider.getName());
        }
    }

    /**
     * Several exceptions can be thrown by init. Check that in this case we throw the right one,
     * as the error could fall under the umbrella of other exceptions.
     * http://b/18987633
     */
    public void testKeyAgreement_init_DoesNotSupportKeyClass_throwsInvalidKeyException()
            throws Exception {
        Provider mockProvider = new MockProvider("MockProvider") {
            @Override
            public void setup() {
                put("KeyAgreement.FOO", MockKeyAgreementSpi.AllKeyTypes.class.getName());
                put("KeyAgreement.FOO SupportedKeyClasses", "none");
            }
        };

        Security.addProvider(mockProvider);
        try {
            KeyAgreement c = KeyAgreement.getInstance("FOO");
            c.init(new MockKey());
            fail("Expected InvalidKeyException");
        } catch (InvalidKeyException expected) {
        } finally {
            Security.removeProvider(mockProvider.getName());
        }
    }
}
