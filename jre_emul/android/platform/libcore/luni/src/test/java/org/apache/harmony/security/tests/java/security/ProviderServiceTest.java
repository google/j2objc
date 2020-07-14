/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 * @author Boris V. Kuznetsov
 * @version $Revision$
 */

package org.apache.harmony.security.tests.java.security;

import java.lang.Override;
import java.security.InvalidParameterException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.harmony.security.tests.support.RandomImpl;

import junit.framework.TestCase;

/**
 * Tests for <code>Provider.Service</code> constructor and methods
 *
 */
public class ProviderServiceTest extends TestCase {

    public void testService() {
        Provider p = new MyProvider();
        try {
            new Provider.Service(null, "type", "algorithm", "className", null,
                    null);
            fail("provider is null: No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            new Provider.Service(p, null, "algorithm", "className", null, null);
            fail("type is null: No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            new Provider.Service(p, "type", null, "className", null, null);
            fail("algorithm is null: No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            new Provider.Service(p, "type", "algorithm", null, null, null);
            fail("className is null: No expected NullPointerException");
        } catch (NullPointerException e) {
        }

        Provider.Service s = new Provider.Service(p, "type", "algorithm",
                "className", null, null);

        if (!s.getType().equals("type")) {
            fail("getType() failed");
        }
        if (!s.getAlgorithm().equals("algorithm")) {
            fail("getAlgorithm() failed");
        }
        if (s.getProvider() != p) {
            fail("getProvider() failed");
        }
        if (!s.getClassName().equals("className")) {
            fail("getClassName() failed");
        }
        if (!s.supportsParameter(new Object())) {
            fail("supportsParameter() failed");
        }
    }

    public void testGetAttribute() {
        Provider p = new MyProvider();
        Provider.Service s = new Provider.Service(p, "type", "algorithm",
                "className", null, null);
        try {
            s.getAttribute(null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }

        if (s.getAttribute("aaa") != null) {
            fail("getAttribute(aaa) failed");
        }

        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("attribute", "value");
        hm.put("KeySize", "1024");
        hm.put("AAA", "BBB");

        s = new Provider.Service(p, "type", "algorithm", "className", null, hm);
        if (s.getAttribute("bbb") != null) {
            fail("getAttribute(bbb) failed");
        }
        if (!s.getAttribute("attribute").equals("value")) {
            fail("getAttribute(attribute) failed");
        }
        if (!s.getAttribute("KeySize").equals("1024")) {
            fail("getAttribute(KeySize) failed");
        }
    }

    public void testNewInstance() throws Exception {
        String type = "SecureRandom";
        String algorithm = "algorithm";
        MyProvider p = new MyProvider();
        Provider.Service s = new Provider.Service(p, type,
                algorithm,
                "org.apache.harmony.security.tests.support.RandomImpl",
                null, null);

        p.putService(s);

        // Check success and correct type.
        Object o = s.newInstance(null);
        assertTrue("new service instance is of incorrect class " + o.getClass().getName(),
                o instanceof RandomImpl);
    }

    public void testNewlyCreatedServiceDoesntGetsRegistered() throws Exception {
        String type = "SecureRandom";
        String algorithm = "algorithm";
        MyProvider p = new MyProvider();
        Provider.Service s = new Provider.Service(p, type,
                algorithm,
                "org.apache.harmony.security.tests.support.RandomImpl",
                null, null);
        assertSame(p, s.getProvider());

        assertNull(p.getService(type, algorithm));

        try {
            Object o = s.newInstance(null);
            fail("Expected " + NoSuchAlgorithmException.class.getName() + " as the service is not "
                    + "registered with the provider");
        } catch (NoSuchAlgorithmException e) {
            // Expected.
        }
    }


    public void testGetAlgorithm() {
        Provider p = new MyProvider();
        Provider.Service s1 = new Provider.Service(p, "type", "algorithm",
                "className", null, null);
        assertTrue(s1.getAlgorithm().equals("algorithm"));

        Provider.Service s2 = new Provider.Service(p, "SecureRandom",
                "algorithm", "tests.java.security.support.RandomImpl", null,
                null);
        assertTrue(s2.getAlgorithm().equals("algorithm"));
    }

    public void testGetClassName() {
        Provider p = new MyProvider();
        Provider.Service s1 = new Provider.Service(p, "type", "algorithm",
                "className", null, null);
        assertTrue(s1.getClassName().equals("className"));

        Provider.Service s2 = new Provider.Service(p, "SecureRandom",
                "algorithm", "tests.java.security.support.RandomImpl", null,
                null);
        assertTrue(s2.getClassName().equals("tests.java.security.support.RandomImpl"));
    }

    public void testGetProvider() {
        Provider p = new MyProvider();
        Provider.Service s1 = new Provider.Service(p, "type", "algorithm",
                "className", null, null);
        assertTrue(s1.getProvider() == p);

        Provider.Service s2 = new Provider.Service(p, "SecureRandom",
                "algorithm", "tests.java.security.support.RandomImpl", null,
                null);
        assertTrue(s2.getProvider() == p);
    }

    public void testGetType() {
        Provider p = new MyProvider();
        Provider.Service s1 = new Provider.Service(p, "type", "algorithm",
                "className", null, null);
        assertTrue(s1.getType().equals("type"));

        Provider.Service s2 = new Provider.Service(p, "SecureRandom",
                "algorithm", "tests.java.security.support.RandomImpl", null,
                null);
        assertTrue(s2.getType().equals("SecureRandom"));
    }

    public void testSupportsParameter() {
        Provider p = new MyProvider();
        Provider.Service s1 = new Provider.Service(p, "type", "algorithm",
                "className", null, null);
        assertTrue(s1.supportsParameter(null));
        assertTrue(s1.supportsParameter(new Object()));
    }

    public void testToString() {
        Provider p = new MyProvider();
        Provider.Service s1 = new Provider.Service(p, "type", "algorithm",
                "className", null, null);
        s1.toString();

        Provider.Service s2 = new Provider.Service(p, "SecureRandom",
                "algorithm", "tests.java.security.support.RandomImpl", null,
                null);
        s2.toString();
    }

    class MyProvider extends Provider {
        MyProvider() {
            super("MyProvider", 1.0, "Provider for testing");
            put("MessageDigest.SHA-1", "SomeClassName");
        }

        @Override
        public void putService(Service s) {
            super.putService(s);
        }
    }

    class MyService extends Provider.Service {

        public MyService(Provider provider, String type, String algorithm,
                String className, List<String> aliases,
                Map<String, String> attributes) {
            super(provider, type, algorithm, className, aliases, attributes);
            // TODO Auto-generated constructor stub
        }

        @Override
        public boolean supportsParameter(Object parameter) {
            if (parameter.getClass() == String.class) {
                return true;
            }
            return false;
        }
    }
}
