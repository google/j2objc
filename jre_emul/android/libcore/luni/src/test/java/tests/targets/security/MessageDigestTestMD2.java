/*
 * Copyright (C) 2009 The Android Open Source Project
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
package tests.targets.security;

import dalvik.annotation.AndroidOnly;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import junit.framework.TestCase;

public class MessageDigestTestMD2 extends TestCase {

    @AndroidOnly("Android doesn't include MD2 message digest algorithm")
    public void testMessageDigest1() throws Exception{
        try {
            MessageDigest digest = MessageDigest.getInstance("MD2");
            fail("MD2 MessageDigest algorithm must not be supported");
        } catch (NoSuchAlgorithmException e) {
            // expected
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(
                    "1.2.840.113549.2.2");
            fail("MD2 MessageDigest algorithm must not be supported");
        } catch (NoSuchAlgorithmException e) {
            // expected
        }
    }

    @AndroidOnly("Android allows usage of MD2 in third party providers")
    public void testMessageDigest2() throws Exception{

        Provider provider  = new MyProvider();
        Security.addProvider(provider);

        try {
            MessageDigest digest = MessageDigest.getInstance("MD2");

            digest = MessageDigest.getInstance("1.2.840.113549.2.2");
        } finally {
            Security.removeProvider(provider.getName());
        }
    }

    public final class MyProvider extends Provider {
        public MyProvider() {
            super("MessageDigestMD2Test", 1.00, "TestProvider");
            put("MessageDigest.MD2",
                    "tests.targets.security.MessageDigestTestMD2$MD2");
            put("Alg.Alias.MessageDigest.1.2.840.113549.2.2", "MD2");
        }
    }

    public static class MD2 extends MessageDigest {

        public MD2() {
            super("MD2");
        }

        protected MD2(String algorithm) {
            super(algorithm);
        }

        @Override
        protected byte[] engineDigest() {
            return null;
        }

        @Override
        protected void engineReset() {
        }

        @Override
        protected void engineUpdate(byte input) {
        }

        @Override
        protected void engineUpdate(byte[] input, int offset, int len) {
        }
    }
}
