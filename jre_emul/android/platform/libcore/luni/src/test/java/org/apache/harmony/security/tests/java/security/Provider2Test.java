/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.security.tests.java.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;

public class Provider2Test extends junit.framework.TestCase {

    class TestProvider extends Provider {
        TestProvider(String name, double version, String info) {
            super(name, version, info);
        }
    }

    class MyEntry implements java.util.Map.Entry {
         public Object getKey() {
             return null;
         }

         public Object getValue() {
             return null;
         }

         public Object setValue(Object value) {
             return null;
         }
    }

    TestProvider provTest = new TestProvider("provTest", 1.2,
            "contains nothings, purely for testing the class");


    /**
     * java.security.Provider#entrySet()
     */
    public void test_entrySet() {
        // test method of java.security.provider.entrySet
        provTest.put("test.prop", "this is a test property");
        try {
            //make it compilable on 1.5
            provTest.entrySet().add(new MyEntry());
            fail("was able to modify the entrySet");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    /**
     * java.security.Provider#getInfo()
     */
    public void test_getInfo() {
        // test method of java.security.provider.getInfo
        assertEquals("the information of the provider is not stored properly",
                "contains nothings, purely for testing the class", provTest
                        .getInfo());
    }

    /**
     * java.security.Provider#getName()
     */
    public void test_getName() {
        // test method of java.security.provider.getName
        assertEquals("the name of the provider is not stored properly",
                "provTest", provTest.getName());
    }

    /**
     * java.security.Provider#getVersion()
     */
    public void test_getVersion() {
        // test method of java.security.provider.getVersion
        assertEquals("the version of the provider is not stored properly",
                1.2, provTest.getVersion(), 0);
    }

    /**
     * java.security.Provider#keySet()
     */
    public void test_keySet() {
        // test method of java.security.provider.keySet
        provTest.put("test.prop", "this is a test property");
        try {
            provTest.keySet().add("another property key");
            fail("was able to modify the keySet");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    /**
     * java.security.Provider#values()
     */
    public void test_values() {
        // test method of java.security.provider.values
        provTest.put("test.prop", "this is a test property");
        try {
            provTest.values().add("another property value");
            fail("was able to modify the values collection");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }


    /**
     * java.security.Provider#toString()
     */
    public void test_toString() {
        // Regression for HARMONY-3734
        assertEquals("provTest version 1.2", provTest.toString());
    }

    // Regression Test for Provider.Service.getAlias(), which is an package
    // private method but will lead to NPE thrown at
    // Secure.SecurityDorr.getAliases
    public void test_getAlias() throws Exception {
        MockProvider mockProvider = new MockProvider("MOCKNAME", 1.0,
                "MOCKINFO");
        Provider.Service service = new Provider.Service(mockProvider,
                "MOCKTYPE", "MOCKALGORITHM", "TESTCLASSNAME", null, null);
        mockProvider.putService(service);
        Security.addProvider(mockProvider);
        try {
            MessageDigest messageDigest = MessageDigest
                    .getInstance("NOTEXISTS");
        }

        catch (NoSuchAlgorithmException e) {
            // expected
        } finally {
            Security.removeProvider("MOCKNAME");
        }
    }

    public static class MockProvider extends Provider {

        private static final long serialVersionUID = 1L;

        public MockProvider(String name, double version, String info) {
            super(name, version, info);

        }

        public void putService(Provider.Service service) {
            super.putService(service);
        }

        public void removeService(Provider.Service service) {
            super.removeService(service);
        }
    }
}
