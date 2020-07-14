/*
 * Copyright (C) 2011 The Android Open Source Project
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

import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.Provider;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import junit.framework.TestCase;
import libcore.java.security.StandardNames;

public final class CipherTest extends TestCase {

    private static abstract class MockProvider extends Provider {
        public MockProvider(String name) {
            super(name, 1.0, "Mock provider used for testing");
            setup();
        }

        public abstract void setup();
    }

    public void testCipher_getInstance_SuppliedProviderNotRegistered_Success() throws Exception {
        Provider mockProvider = new MockProvider("MockProvider") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.AllKeyTypes.class.getName());
            }
        };

        {
            Cipher c = Cipher.getInstance("FOO", mockProvider);
            c.init(Cipher.ENCRYPT_MODE, new MockKey());
            assertEquals(mockProvider, c.getProvider());
        }
    }

    public void testCipher_getInstance_DoesNotSupportKeyClass_Success() throws Exception {
        Provider mockProvider = new MockProvider("MockProvider") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.AllKeyTypes.class.getName());
                put("Cipher.FOO SupportedKeyClasses", "None");
            }
        };

        Security.addProvider(mockProvider);
        try {
            Cipher c = Cipher.getInstance("FOO", mockProvider);
            c.init(Cipher.ENCRYPT_MODE, new MockKey());
            assertEquals(mockProvider, c.getProvider());
        } finally {
            Security.removeProvider(mockProvider.getName());
        }
    }

    public void testCipher_getInstance_SuppliedProviderNotRegistered_MultipartTransform_Success()
            throws Exception {
        Provider mockProvider = new MockProvider("MockProvider") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.AllKeyTypes.class.getName());
            }
        };

        {
            Cipher c = Cipher.getInstance("FOO/FOO/FOO", mockProvider);
            c.init(Cipher.ENCRYPT_MODE, new MockKey());
            assertEquals(mockProvider, c.getProvider());
        }
    }

    public void testCipher_getInstance_OnlyUsesSpecifiedProvider_SameNameAndClass_Success()
            throws Exception {
        Provider mockProvider = new MockProvider("MockProvider") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.AllKeyTypes.class.getName());
            }
        };

        Security.addProvider(mockProvider);
        try {
            {
                Provider mockProvider2 = new MockProvider("MockProvider") {
                    public void setup() {
                        put("Cipher.FOO", MockCipherSpi.AllKeyTypes.class.getName());
                    }
                };
                Cipher c = Cipher.getInstance("FOO", mockProvider2);
                assertEquals(mockProvider2, c.getProvider());
            }
        } finally {
            Security.removeProvider(mockProvider.getName());
        }
    }

    /* J2ObjC removed: broken test
    public void testCipher_getInstance_DelayedInitialization_KeyType() throws Exception {
        Provider mockProviderSpecific = new MockProvider("MockProviderSpecific") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.SpecificKeyTypes.class.getName());
                put("Cipher.FOO SupportedKeyClasses", MockKey.class.getName());
            }
        };
        Provider mockProviderSpecific2 = new MockProvider("MockProviderSpecific2") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.SpecificKeyTypes2.class.getName());
                put("Cipher.FOO SupportedKeyClasses", MockKey2.class.getName());
            }
        };
        Provider mockProviderAll = new MockProvider("MockProviderAll") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.AllKeyTypes.class.getName());
            }
        };

        Security.addProvider(mockProviderSpecific);
        Security.addProvider(mockProviderSpecific2);
        Security.addProvider(mockProviderAll);

        try {
            {
                System.out.println(Arrays.deepToString(Security.getProviders("Cipher.FOO")));
                Cipher c = Cipher.getInstance("FOO");
                c.init(Cipher.ENCRYPT_MODE, new MockKey());
                assertEquals(mockProviderSpecific, c.getProvider());

                try {
                    c.init(Cipher.ENCRYPT_MODE, new MockKey2());
                    assertEquals(mockProviderSpecific2, c.getProvider());
                    if (StandardNames.IS_RI) {
                        fail("RI was broken before; fix tests now that it works!");
                    }
                } catch (InvalidKeyException e) {
                    if (!StandardNames.IS_RI) {
                        fail("Non-RI should select the right provider");
                    }
                }
            }

            {
                Cipher c = Cipher.getInstance("FOO");
                c.init(Cipher.ENCRYPT_MODE, new Key() {
                    @Override
                    public String getAlgorithm() {
                        throw new UnsupportedOperationException("not implemented");
                    }

                    @Override
                    public String getFormat() {
                        throw new UnsupportedOperationException("not implemented");
                    }

                    @Override
                    public byte[] getEncoded() {
                        throw new UnsupportedOperationException("not implemented");
                    }
                });
                assertEquals(mockProviderAll, c.getProvider());
            }

            {
                Cipher c = Cipher.getInstance("FOO");
                assertEquals(mockProviderSpecific, c.getProvider());
            }
        } finally {
            Security.removeProvider(mockProviderSpecific.getName());
            Security.removeProvider(mockProviderSpecific2.getName());
            Security.removeProvider(mockProviderAll.getName());
        }
    }
     */

    public void testCipher_getInstance_CorrectPriority_AlgorithmOnlyFirst() throws Exception {
        Provider mockProviderOnlyAlgorithm = new MockProvider("MockProviderOnlyAlgorithm") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.AllKeyTypes.class.getName());
            }
        };
        Provider mockProviderFullTransformSpecified = new MockProvider("MockProviderFull") {
            public void setup() {
                put("Cipher.FOO/FOO/FOO", MockCipherSpi.AllKeyTypes.class.getName());
            }
        };

        Security.addProvider(mockProviderOnlyAlgorithm);
        Security.addProvider(mockProviderFullTransformSpecified);
        try {
            Cipher c = Cipher.getInstance("FOO/FOO/FOO");
            assertEquals(mockProviderOnlyAlgorithm, c.getProvider());
        } finally {
            Security.removeProvider(mockProviderOnlyAlgorithm.getName());
            Security.removeProvider(mockProviderFullTransformSpecified.getName());
        }
    }

    public void testCipher_getInstance_CorrectPriority_FullTransformFirst() throws Exception {
        Provider mockProviderOnlyAlgorithm = new MockProvider("MockProviderOnlyAlgorithm") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.AllKeyTypes.class.getName());
            }
        };
        Provider mockProviderFullTransformSpecified = new MockProvider("MockProviderFull") {
            public void setup() {
                put("Cipher.FOO/FOO/FOO", MockCipherSpi.AllKeyTypes.class.getName());
            }
        };

        Security.addProvider(mockProviderFullTransformSpecified);
        Security.addProvider(mockProviderOnlyAlgorithm);
        try {
            Cipher c = Cipher.getInstance("FOO/FOO/FOO");
            assertEquals(mockProviderFullTransformSpecified, c.getProvider());
        } finally {
            Security.removeProvider(mockProviderOnlyAlgorithm.getName());
            Security.removeProvider(mockProviderFullTransformSpecified.getName());
        }
    }

    public void testCipher_getInstance_CorrectPriority_AliasedAlgorithmFirst() throws Exception {
        Provider mockProviderAliasedAlgorithm = new MockProvider("MockProviderAliasedAlgorithm") {
            public void setup() {
                put("Cipher.BAR", MockCipherSpi.AllKeyTypes.class.getName());
                put("Alg.Alias.Cipher.FOO", "BAR");
            }
        };
        Provider mockProviderAlgorithmOnly = new MockProvider("MockProviderAlgorithmOnly") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.AllKeyTypes.class.getName());
            }
        };

        Security.addProvider(mockProviderAliasedAlgorithm);
        Security.addProvider(mockProviderAlgorithmOnly);
        try {
            Cipher c = Cipher.getInstance("FOO/FOO/FOO");
            assertEquals(mockProviderAliasedAlgorithm, c.getProvider());
        } finally {
            Security.removeProvider(mockProviderAliasedAlgorithm.getName());
            Security.removeProvider(mockProviderAlgorithmOnly.getName());
        }
    }

    public void testCipher_getInstance_WrongType_Failure() throws Exception {
        Provider mockProviderInvalid = new MockProvider("MockProviderInvalid") {
            public void setup() {
                put("Cipher.FOO", Object.class.getName());
            }
        };

        Security.addProvider(mockProviderInvalid);
        try {
            Cipher c = Cipher.getInstance("FOO");
            c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[16], "FOO"));
            fail("Should not find any matching providers; found: " + c);
        } catch (ClassCastException expected) {
        } finally {
            Security.removeProvider(mockProviderInvalid.getName());
        }
    }

    public void testCipher_init_CallsInitWithParams_AlgorithmParameterSpec() throws Exception {
        Provider mockProviderRejects = new MockProvider("MockProviderRejects") {
            public void setup() {
                put("Cipher.FOO",
                        MockCipherSpi.MustInitWithAlgorithmParameterSpec_RejectsAll.class.getName());
                put("Cipher.FOO SupportedKeyClasses", MockKey.class.getName());
            }
        };
        Provider mockProviderAccepts = new MockProvider("MockProviderAccepts") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.AllKeyTypes.class.getName());
                put("Cipher.FOO SupportedKeyClasses", MockKey.class.getName());
            }
        };

        Security.addProvider(mockProviderRejects);
        Security.addProvider(mockProviderAccepts);
        try {
            Cipher c = Cipher.getInstance("FOO");
            c.init(Cipher.ENCRYPT_MODE, new MockKey(), new IvParameterSpec(new byte[12]));
            assertEquals(mockProviderAccepts, c.getProvider());
        } finally {
            Security.removeProvider(mockProviderRejects.getName());
            Security.removeProvider(mockProviderAccepts.getName());
        }
    }

    /* J2ObjC removed: AES AlgorithmParameters not supported
    public void testCipher_init_CallsInitWithParams_AlgorithmParameters() throws Exception {
        Provider mockProviderRejects = new MockProvider("MockProviderRejects") {
            public void setup() {
                put("Cipher.FOO",
                        MockCipherSpi.MustInitWithAlgorithmParameters_RejectsAll.class.getName());
                put("Cipher.FOO SupportedKeyClasses", MockKey.class.getName());
            }
        };
        Provider mockProviderAccepts = new MockProvider("MockProviderAccepts") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.AllKeyTypes.class.getName());
                put("Cipher.FOO SupportedKeyClasses", MockKey.class.getName());
            }
        };

        Security.addProvider(mockProviderRejects);
        Security.addProvider(mockProviderAccepts);
        try {
            Cipher c = Cipher.getInstance("FOO");
            c.init(Cipher.ENCRYPT_MODE, new MockKey(), AlgorithmParameters.getInstance("AES"));
            assertEquals(mockProviderAccepts, c.getProvider());
        } finally {
            Security.removeProvider(mockProviderRejects.getName());
            Security.removeProvider(mockProviderAccepts.getName());
        }
    }
     */

    /* J2ObjC removed: AES AlgorithmParameters not supported
    public void testCipher_init_CallsInitIgnoresRuntimeException() throws Exception {
        Provider mockProviderRejects = new MockProvider("MockProviderRejects") {
            public void setup() {
                put("Cipher.FOO",
                        MockCipherSpi.MustInitWithAlgorithmParameters_ThrowsNull.class.getName());
                put("Cipher.FOO SupportedKeyClasses", MockKey.class.getName());
            }
        };
        Provider mockProviderAccepts = new MockProvider("MockProviderAccepts") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.AllKeyTypes.class.getName());
                put("Cipher.FOO SupportedKeyClasses", MockKey.class.getName());
            }
        };

        Security.addProvider(mockProviderRejects);
        Security.addProvider(mockProviderAccepts);
        try {
            Cipher c = Cipher.getInstance("FOO");
            c.init(Cipher.ENCRYPT_MODE, new MockKey(), AlgorithmParameters.getInstance("AES"));
            assertEquals(mockProviderAccepts, c.getProvider());
        } finally {
            Security.removeProvider(mockProviderRejects.getName());
            Security.removeProvider(mockProviderAccepts.getName());
        }
    }
     */

    /* J2ObjC removed: AES AlgorithmParameters not supported
    public void testCipher_init_CallsInitWithMode() throws Exception {
        Provider mockProviderOnlyEncrypt = new MockProvider("MockProviderOnlyEncrypt") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.MustInitForEncryptModeOrRejects.class.getName());
                put("Cipher.FOO SupportedKeyClasses", MockKey.class.getName());
            }
        };
        Provider mockProviderAcceptsAll = new MockProvider("MockProviderAcceptsAll") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.AllKeyTypes.class.getName());
                put("Cipher.FOO SupportedKeyClasses", MockKey.class.getName());
            }
        };

        Security.addProvider(mockProviderOnlyEncrypt);
        Security.addProvider(mockProviderAcceptsAll);
        try {
            {
                Cipher c = Cipher.getInstance("FOO");
                c.init(Cipher.DECRYPT_MODE, new MockKey(), AlgorithmParameters.getInstance("AES"));
                assertEquals(mockProviderAcceptsAll, c.getProvider());
            }

            {
                Cipher c = Cipher.getInstance("FOO");
                c.init(Cipher.ENCRYPT_MODE, new MockKey(), AlgorithmParameters.getInstance("AES"));
                assertEquals(mockProviderOnlyEncrypt, c.getProvider());
            }
        } finally {
            Security.removeProvider(mockProviderOnlyEncrypt.getName());
            Security.removeProvider(mockProviderAcceptsAll.getName());
        }
    }
     */

    /**
     * Several exceptions can be thrown by init. Check that in this case we throw the right one,
     * as the error could fall under the umbrella of other exceptions.
     * http://b/18987633
     */
    public void testCipher_init_DoesNotSupportKeyClass_throwsInvalidKeyException()
            throws Exception {
        Provider mockProvider = new MockProvider("MockProvider") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.AllKeyTypes.class.getName());
                put("Cipher.FOO SupportedKeyClasses", "none");
            }
        };

        Security.addProvider(mockProvider);
        try {
            Cipher c = Cipher.getInstance("FOO");
            c.init(Cipher.DECRYPT_MODE, new MockKey());
            fail("Expected InvalidKeyException");
        } catch (InvalidKeyException expected) {
        } finally {
            Security.removeProvider(mockProvider.getName());
        }
    }

    /**
     * If a provider rejects a key for "Cipher/Mode/Padding"", there might be another that
     * accepts the key for "Cipher". Don't throw InvalidKeyException when trying the first one.
     * http://b/22208820
     */
    public void testCipher_init_tryAllCombinationsBeforeThrowingInvalidKey()
            throws Exception {
        Provider mockProvider = new MockProvider("MockProvider") {
            public void setup() {
                put("Cipher.FOO/FOO/FOO", MockCipherSpi.AllKeyTypes.class.getName());
                put("Cipher.FOO/FOO/FOO SupportedKeyClasses", "none");
            }
        };

        Provider mockProvider2 = new MockProvider("MockProvider2") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.AllKeyTypes.class.getName());
            }
        };

        Security.addProvider(mockProvider);

        try {
            try {
                // The provider installed doesn't accept the key.
                Cipher c = Cipher.getInstance("FOO/FOO/FOO");
                c.init(Cipher.DECRYPT_MODE, new MockKey());
                fail("Expected InvalidKeyException");
            } catch (InvalidKeyException expected) {
            }

            Security.addProvider(mockProvider2);

            try {
                // The new provider accepts "FOO" with this key. Use it despite the other provider
                // accepts "FOO/FOO/FOO" but doesn't accept the key.
                Cipher c = Cipher.getInstance("FOO/FOO/FOO");
                c.init(Cipher.DECRYPT_MODE, new MockKey());
                assertEquals("MockProvider2", c.getProvider().getName());
            } finally {
                Security.removeProvider(mockProvider2.getName());
            }
        } finally {
            Security.removeProvider(mockProvider.getName());
        }
    }

    /**
     * http://b/29038928
     * If in a second call to init the current spi doesn't support the new specified key, look for
     * another suitable spi.
     */
    public void test_init_onKeyTypeChange_reInitCipher() throws Exception {
        Provider mockProvider = new MockProvider("MockProvider") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.SpecificKeyTypes.class.getName());
            }
        };
        Provider mockProvider2 = new MockProvider("MockProvider2") {
            public void setup() {
                put("Cipher.FOO", MockCipherSpi.SpecificKeyTypes2.class.getName());
            }
        };
        try {
            Security.addProvider(mockProvider);
            Security.addProvider(mockProvider2);
            Cipher cipher = Cipher.getInstance("FOO");
            cipher.init(Cipher.ENCRYPT_MODE, new MockKey());
            assertEquals("MockProvider", cipher.getProvider().getName());
            // Using a different key...
            cipher.init(Cipher.ENCRYPT_MODE, new MockKey2());
            // ...results in a different provider.
            assertEquals("MockProvider2", cipher.getProvider().getName());
        } finally {
            Security.removeProvider(mockProvider.getName());
            Security.removeProvider(mockProvider2.getName());
        }
    }

    /**
     * http://b/29038928
     * If in a second call to init the current spi doesn't support the new specified
     * {@link AlgorithmParameterSpec}, look for another suitable spi.
     */
    public void test_init_onAlgorithmParameterTypeChange_reInitCipher() throws Exception {
        Provider mockProvider = new MockProvider("MockProvider") {
            public void setup() {
                put("Cipher.FOO",
                        MockCipherSpi.SpecificAlgorithmParameterSpecTypes.class.getName());
            }
        };
        Provider mockProvider2 = new MockProvider("MockProvider2") {
            public void setup() {
                put("Cipher.FOO",
                        MockCipherSpi.SpecificAlgorithmParameterSpecTypes2.class.getName());
            }
        };
        try {
            Security.addProvider(mockProvider);
            Security.addProvider(mockProvider2);
            Cipher cipher = Cipher.getInstance("FOO");
            cipher.init(Cipher.ENCRYPT_MODE,
                    new MockKey(),
                    new MockCipherSpi.MockAlgorithmParameterSpec());
            assertEquals("MockProvider", cipher.getProvider().getName());
            // Using a different AlgorithmParameterSpec...
            cipher.init(Cipher.ENCRYPT_MODE,
                    new MockKey(),
                    new MockCipherSpi.MockAlgorithmParameterSpec2());
            // ...results in a different provider.
            assertEquals("MockProvider2", cipher.getProvider().getName());
        } finally {
            Security.removeProvider(mockProvider.getName());
            Security.removeProvider(mockProvider2.getName());
        }
    }

    /**
     * http://b/29038928
     * If in a second call to init the current spi doesn't support the new specified
     * {@link AlgorithmParameters}, look for another suitable spi.
     */
    /* J2ObjC removed: AES AlgorithmParameters not supported
    public void test_init_onAlgorithmParametersChange_reInitCipher() throws Exception {
        Provider mockProvider = new MockProvider("MockProvider") {
            public void setup() {
                put("Cipher.FOO",
                        MockCipherSpi.SpecificAlgorithmParameterAesAlgorithm.class.getName());
            }
        };
        Provider mockProvider2 = new MockProvider("MockProvider2") {
            public void setup() {
                put("Cipher.FOO",
                        MockCipherSpi.SpecificAlgorithmParametersDesAlgorithm.class.getName());
            }
        };
        try {
            Security.addProvider(mockProvider);
            Security.addProvider(mockProvider2);
            Cipher cipher = Cipher.getInstance("FOO");
            cipher.init(Cipher.ENCRYPT_MODE,
                    new MockKey(),
                    AlgorithmParameters.getInstance("AES"));
            assertEquals("MockProvider", cipher.getProvider().getName());
            // Using a different AlgorithmParameters...
            cipher.init(Cipher.ENCRYPT_MODE,
                    new MockKey(),
                    AlgorithmParameters.getInstance("DES"));
            // ...results in a different provider.
            assertEquals("MockProvider2", cipher.getProvider().getName());
        } finally {
            Security.removeProvider(mockProvider.getName());
            Security.removeProvider(mockProvider2.getName());
        }
    }
     */
}
