package org.apache.harmony.security.tests.java.security;

import junit.framework.TestCase;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyFactorySpi;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class KeyFactoryTest extends TestCase {

    Provider provider;
    boolean exceptionThrown;

    Provider existingProvider;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        exceptionThrown = false;

        Provider[] providers = Security.getProviders();
        if (providers.length == 0) {
            fail("no providers found");
        }

        existingProvider = providers[0];

        provider = new TestKeyFactoryProvider();
        Security.addProvider(provider);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Security.removeProvider(provider.getName());
    }

    @SuppressWarnings("unchecked")
    public void testGetInstanceString() {
        try {
            KeyFactory factory = KeyFactory.getInstance(TEST_KEYFACTORY_NAME);
            assertNotNull(factory);
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        }

        String[] parameters = {
                "UnknownKeyFactory",
                null
        };

        Class[] exceptions = {
                NoSuchAlgorithmException.class,
                NullPointerException.class
        };

        for (int i = 0; i < parameters.length; i++) {
            String algorithm = parameters[i];
            exceptionThrown = false;
            String message = "getInstance(" + (algorithm == null ? "null" : "\"" + algorithm + "\"") + ")";
            try {
                KeyFactory.getInstance(algorithm);
            } catch (Exception e) {
                checkException(message, e, exceptions[i]);
            } finally {
                checkException(message, null, exceptions[i]);
            }
        }

    }

    @SuppressWarnings("unchecked")
    public void testGetInstanceStringString() {
        try {
            KeyFactory factory = KeyFactory.getInstance(TEST_KEYFACTORY_NAME, TEST_PROVIDER_NAME);
            assertNotNull(factory);
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        } catch (NoSuchProviderException e) {
            fail("unexpected exception: " + e);
        }

        String[][] combinations = {
                { "UnknownKeyFactory", TEST_PROVIDER_NAME},
                { TEST_KEYFACTORY_NAME, "UnknownProvider"},
                { TEST_KEYFACTORY_NAME, existingProvider.getName() },
                { null, TEST_PROVIDER_NAME },
                { TEST_KEYFACTORY_NAME, null },
                { null, null}
        };

        Class[] exceptions = {
                NoSuchAlgorithmException.class,
                NoSuchProviderException.class,
                NoSuchAlgorithmException.class,
                NullPointerException.class,
                IllegalArgumentException.class,
                IllegalArgumentException.class
        };

        for (int i = 0; i < combinations.length; i++) {
            String[] combination = combinations[i];
            String message = "getInstance(\"" + combination[0] + "\", \"" + combination[1] + "\")";
            exceptionThrown = false;
            try {
                KeyFactory.getInstance(combination[0], combination[1]);
            } catch (Exception e) {
                checkException(message, e, exceptions[i]);
            } finally {
                checkException(message, null, exceptions[i]);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void testGetInstanceStringProvider() {
        try {
            KeyFactory factory = KeyFactory.getInstance(TEST_KEYFACTORY_NAME, provider);
            assertNotNull(factory);
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        }

        String[] algorithms = {
                "UnknownKeyFactory",
                null,
                TEST_KEYFACTORY_NAME,
                TEST_KEYFACTORY_NAME
        };

        Provider[] providers = {
                provider,
                provider,
                existingProvider,
                null
        };

        Class[] exceptions = {
                NoSuchAlgorithmException.class,
                NullPointerException.class,
                NoSuchAlgorithmException.class,
                IllegalArgumentException.class
        };

        for (int i = 0; i < algorithms.length; i++) {
            String algorithm = algorithms[i];
            Provider provider = providers[i];
            String message = "getInstance(" +
                (algorithm == null ? "null" : "\"" + algorithm + "\"") +
                ", " +
                (provider == null ? "null" : "provider");
            exceptionThrown = false;
            try {
                KeyFactory.getInstance(algorithm, provider);
            } catch (Exception e) {
                checkException(message, e, exceptions[i]);
            } finally {
                checkException(message, null, exceptions[i]);
            }

        }
    }

    @SuppressWarnings("unchecked")
    public void testGetKeySpec() {
        KeyFactory factory = null;
        try {
            factory = KeyFactory.getInstance(TEST_KEYFACTORY_NAME);
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        }

        assertNotNull(factory);

        {
            Key[] keys = {
                    new TestPrivateKey(),
                    new TestPublicKey(),
                    new TestPrivateKey(new byte[] { 42, 41, 40 }),
                    new TestPublicKey(new byte[] { 40, 41, 42 })
            };

            Class[] keySpecs = {
                    TestPrivateKeySpec.class,
                    TestPublicKeySpec.class,
                    TestPrivateKeySpec.class,
                    TestPublicKeySpec.class,
            };

            for (int i = 0; i < keys.length; i++) {
                Key key = keys[i];
                Class keySpec = keySpecs[i];
                String message = "getKeySpec(" + key.toString() + ", " + keySpec.toString() + ")";
                try {
                    KeySpec spec = factory.getKeySpec(key, keySpec);
                    assertNotNull(spec);
                    assertTrue(spec.getClass() == keySpec);
                } catch (InvalidKeySpecException e) {
                    fail("unexpected exception: " + e);
                }
            }
        }

        {
            Key[] keys = {
                    new AnotherKey(),
                    null,
                    new TestPrivateKey(),
                    null,
            };

            Class[] keySpecs = {
                    KeySpec.class,
                    TestPrivateKeySpec.class,
                    null,
                    null,
            };

            Class[] exceptions = {
                    InvalidKeySpecException.class,
                    NullPointerException.class,
                    InvalidKeySpecException.class,
                    NullPointerException.class
            };

            for (int i = 0; i < keys.length; i++) {
                Key key = keys[i];
                Class keySpec = keySpecs[i];
                exceptionThrown = false;
                String message = "getKeySpec(" +
                    (key == null ? "null" : key.toString()) +
                    ", " +
                    (keySpec == null ? "null" : keySpec.toString()) + ")";
                try {
                    factory.getKeySpec(key, keySpec);
                } catch (Exception e) {
                    checkException(message, e, exceptions[i]);
                } finally {
                    checkException(message, null, exceptions[i]);
                }

            }
        }
    }

    @SuppressWarnings("unchecked")
    public void testTranslateKey() {
        KeyFactory factory = null;
        try {
            factory = KeyFactory.getInstance(TEST_KEYFACTORY_NAME);
        } catch (NoSuchAlgorithmException e) {
            fail("unexpected exception: " + e);
        }

        assertNotNull(factory);

        {
            Key[] keys = {
                    new TestPrivateKey(),
                    new TestPublicKey()
            };

            Class[] translated = {
                    TestPublicKey.class,
                    TestPrivateKey.class
            };

            for (int i = 0; i < keys.length; i++) {
                Key key = keys[i];
                Class translate = translated[i];
                try {
                    Key translateKey = factory.translateKey(key);
                    assertNotNull(translateKey);
                    assertEquals(translate, translateKey.getClass());
                } catch (InvalidKeyException e) {
                    fail("unexpected exception: " + e);
                }
            }
        }

        {
            Key[] keys = {
                    new AnotherKey(),
                    null
            };

            Class[] exceptions = {
                    InvalidKeyException.class,
                    NullPointerException.class
            };

            for (int i = 0; i < keys.length; i++) {
                Key key = keys[i];
                String message = "translateKey(" +
                    (key == null ? "null" : key.toString()) + ")";
                exceptionThrown = false;
                try {
                    factory.translateKey(key);
                } catch (Exception e) {
                    checkException(message, e, exceptions[i]);
                } finally {
                    checkException(message, null, exceptions[i]);
                }
            }
        }
    }

    private static final String TEST_PROVIDER_NAME = "TestKeyFactoryProvider";
    private static final String TEST_KEYFACTORY_NAME = "TestKeyFactory";

    static class TestKeyFactoryProvider extends Provider {

        protected TestKeyFactoryProvider() {
            super(TEST_PROVIDER_NAME, 1.1, "Test KeyFactory Provider");
            put("KeyFactory." + TEST_KEYFACTORY_NAME, TestKeyFactorySpi.class.getName());
        }
    }

    public static class TestKeyFactorySpi extends KeyFactorySpi {

        @Override
        protected PrivateKey engineGeneratePrivate(KeySpec keySpec)
                throws InvalidKeySpecException {
            if (TestPrivateKeySpec.class == keySpec.getClass()) {
                return new TestPrivateKey(((TestPrivateKeySpec)keySpec).encoded);
            }

            throw new InvalidKeySpecException();
        }

        @Override
        protected PublicKey engineGeneratePublic(KeySpec keySpec)
                throws InvalidKeySpecException {
            if (TestPublicKeySpec.class == keySpec.getClass()) {
                return new TestPublicKey(((TestPublicKeySpec)keySpec).encoded);
            }
            throw new InvalidKeySpecException();
        }

        @Override
        protected <T extends KeySpec> T engineGetKeySpec(Key key,
                Class<T> keySpec) throws InvalidKeySpecException {

            if (key == null) {
                throw new NullPointerException();
            }

            Constructor<T> constructor = null;
            if (TestPrivateKeySpec.class == keySpec) {
                try {
                    constructor = keySpec.getConstructor(TestPrivateKey.class);
                } catch (SecurityException e) {
                    throw new InvalidKeySpecException(e);
                } catch (NoSuchMethodException e) {
                    throw new InvalidKeySpecException(e);
                }
            } else if (TestPublicKeySpec.class == keySpec) {
                try {
                    constructor = keySpec.getConstructor(TestPublicKey.class);
                } catch (SecurityException e) {
                    throw new InvalidKeySpecException(e);
                } catch (NoSuchMethodException e) {
                    throw new InvalidKeySpecException(e);
                }
            }

            if (constructor == null) {
                throw new InvalidKeySpecException();
            }

            try {
                return constructor.newInstance(key);
            } catch (IllegalArgumentException e) {
                throw new InvalidKeySpecException(e);
            } catch (InstantiationException e) {
                throw new InvalidKeySpecException(e);
            } catch (IllegalAccessException e) {
                throw new InvalidKeySpecException(e);
            } catch (InvocationTargetException e) {
                throw new InvalidKeySpecException(e);
            }
        }

        @Override
        protected Key engineTranslateKey(Key key) throws InvalidKeyException {
            if (TestPrivateKey.class == key.getClass()) {
                return new TestPublicKey();
            } else if (TestPublicKey.class == key.getClass()) {
                return new TestPrivateKey();
            }
            throw new InvalidKeyException();
        }

    }

    static class TestPrivateKeySpec implements KeySpec {
        @SuppressWarnings("unused")
        private final byte[] encoded;

        public TestPrivateKeySpec(TestPrivateKey key) {
            this.encoded = key.getEncoded();
        }
    }

    static class TestPublicKeySpec implements KeySpec {
        @SuppressWarnings("unused")
        private final byte[] encoded;

        public TestPublicKeySpec(TestPublicKey key) {
            this.encoded = key.getEncoded();
        }
    }

    static class TestPrivateKey implements PrivateKey {

        private final byte[] encoded;

        public TestPrivateKey() {
            encoded = new byte[] {3, 4, 5};
        }

        public TestPrivateKey(byte[] encoded) {
            this.encoded = encoded;
        }

        public String getAlgorithm() {
            return "TestPrivateKey";
        }

        public byte[] getEncoded() {
            return encoded;
        }

        public String getFormat() {
            return "TestFormat";
        }
    }

    static class TestPublicKey implements PublicKey {

        private final byte[] encoded;

        public TestPublicKey() {
            encoded = new byte[] {3, 4, 5};
        }

        public TestPublicKey(byte[] encoded) {
            this.encoded = encoded;
        }

        public String getAlgorithm() {
            return "TestPublicKey";
        }

        public byte[] getEncoded() {
            return encoded;
        }

        public String getFormat() {
            return "TestFormat";
        }
    }

    static class AnotherKey implements Key {

        public String getAlgorithm() {
            return "AnotherKey";
        }

        public byte[] getEncoded() {
            return null;
        }

        public String getFormat() {
            return "AnotherFormat";
        }

    }

    private void checkException(String message, Exception thrown, Class<? extends Exception> expected) {
        if (thrown == null) {
            if (!exceptionThrown) {
                fail(message + ", expected " + expected.getName());
            }
        } else if (expected == thrown.getClass()) {
            exceptionThrown = true;
            // ok
        } else {
            exceptionThrown = true;
            fail(message + ", unexpected exception: " + thrown + ", expected: " + expected.getName());
        }
    }

}
