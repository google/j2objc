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

package org.apache.harmony.tests.javax.security.auth.x500;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import junit.framework.TestCase;
import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.security.tests.support.cert.TestUtils;
import tests.support.resource.Support_Resources;


/**
 * Tests for <code>X500Principal</code> class constructors and methods.
 *
 */
public class X500PrincipalTest extends TestCase {

    /**
     * javax.security.auth.x500.X500Principal#X500Principal(String name)
     */
    public void test_X500Principal_01() {
        String name = "CN=Duke,OU=JavaSoft,O=Sun Microsystems,C=US";

        try {
            X500Principal xpr = new X500Principal(name);
            assertNotNull("Null object returned", xpr);
            String resName = xpr.getName();
            assertEquals(name, resName);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            X500Principal xpr = new X500Principal((String)null);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of NullPointerException");
        }

        try {
            X500Principal xpr = new X500Principal("X500PrincipalName");
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
    }

    /**
     * javax.security.auth.x500.X500Principal#X500Principal(InputStream is)
     */
    public void test_X500Principal_02() {
        String name = "CN=Duke,OU=JavaSoft,O=Sun Microsystems,C=US";
        byte[] ba = getByteArray(TestUtils.getX509Certificate_v1());
        ByteArrayInputStream is = new ByteArrayInputStream(ba);
        InputStream isNull = null;

        try {
            X500Principal xpr = new X500Principal(is);
            assertNotNull("Null object returned", xpr);
            byte[] resArray = xpr.getEncoded();
            assertEquals(ba.length, resArray.length);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            X500Principal xpr = new X500Principal(isNull);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of NullPointerException");
        }

        is = new ByteArrayInputStream(name.getBytes());
        try {
            X500Principal xpr = new X500Principal(is);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
    }

    /**
     * javax.security.auth.x500.X500Principal#X500Principal(byte[] name)
     */
    public void test_X500Principal_03() {
        String name = "CN=Duke,OU=JavaSoft,O=Sun Microsystems,C=US";
        byte[] ba = getByteArray(TestUtils.getX509Certificate_v1());
        byte[] baNull = null;

        try {
            X500Principal xpr = new X500Principal(ba);
            assertNotNull("Null object returned", xpr);
            byte[] resArray = xpr.getEncoded();
            assertEquals(ba.length, resArray.length);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            X500Principal xpr = new X500Principal(baNull);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }

        ba = name.getBytes();
        try {
            X500Principal xpr = new X500Principal(ba);
            fail("IllegalArgumentException wasn't thrown");
        } catch (IllegalArgumentException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of IllegalArgumentException");
        }
    }

    /**
     * javax.security.auth.x500.X500Principal#getName()
     */
    public void test_getName() {
        String name = "CN=Duke,OU=JavaSoft,O=Sun Microsystems,C=US";
        X500Principal xpr = new X500Principal(name);
        try {
            String resName = xpr.getName();
            assertEquals(name, resName);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    /**
     * javax.security.auth.x500.X500Principal#getName(String format)
     */
    public void test_getName_Format() {
        String name = "CN=Duke,OU=JavaSoft,O=Sun Microsystems,C=US";
        String expectedName = "cn=duke,ou=javasoft,o=sun microsystems,c=us";
        X500Principal xpr = new X500Principal(name);
        try {
            String resName = xpr.getName(X500Principal.CANONICAL);
            assertEquals(expectedName, resName);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        expectedName = "CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US";
        try {
            String resName = xpr.getName(X500Principal.RFC1779);
            assertEquals(expectedName, resName);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            String resName = xpr.getName(X500Principal.RFC2253);
            assertEquals(name, resName);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            String resName = xpr.getName(null);
            fail("IllegalArgumentException  wasn't thrown");
        } catch (IllegalArgumentException  iae) {
        }
        try {
            String resName = xpr.getName("RFC2254");
            fail("IllegalArgumentException  wasn't thrown");
        } catch (IllegalArgumentException  iae) {
        }
    }

    /**
     * javax.security.auth.x500.X500Principal#hashCode()
     */
    public void test_hashCode() {
        String name = "CN=Duke,OU=JavaSoft,O=Sun Microsystems,C=US";
        X500Principal xpr = new X500Principal(name);
        try {
            int res = xpr.hashCode();
            assertNotNull(res);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    /**
     * javax.security.auth.x500.X500Principal#toString()
     */
    public void test_toString() {
        String name = "CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US";
        X500Principal xpr = new X500Principal(name);
        try {
            String res = xpr.toString();
            assertNotNull(res);
            assertEquals(name, res);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    /**
     * javax.security.auth.x500.X500Principal#getEncoded()
     */
    public void test_getEncoded() {
        byte[] ba = getByteArray(TestUtils.getX509Certificate_v1());
        X500Principal xpr = new X500Principal(ba);
        try {
            byte[] res = xpr.getEncoded();
            assertNotNull(res);
            assertEquals(ba.length, res.length);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    /**
     * javax.security.auth.x500.X500Principal#equals(Object o)
     */
    public void test_equals() {
        String name1 = "CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US";
        String name2 = "cn=duke,ou=javasoft,o=sun microsystems,c=us";
        String name3 = "CN=Alex Astapchuk, OU=SSG, O=Intel ZAO, C=RU";
        X500Principal xpr1 = new X500Principal(name1);
        X500Principal xpr2 = new X500Principal(name2);
        X500Principal xpr3 = new X500Principal(name3);
        try {
            assertTrue("False returned", xpr1.equals(xpr2));
            assertFalse("True returned", xpr1.equals(xpr3));
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    private byte[] getByteArray(byte[] array) {
        byte[] x = null;
        try {
            ByteArrayInputStream is = new ByteArrayInputStream(array);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate)cf.generateCertificate(is);
            X500Principal xx = cert.getIssuerX500Principal();
            x = xx.getEncoded();
        } catch (Exception e) {
            return null;
        }
        return x;
    }

        /**
     * @tests javax.security.auth.x500.X500Principal#X500Principal(java.lang.String)
     */
    public void test_ConstructorLjava_lang_String() {
        X500Principal principal = new X500Principal(
                "CN=Hermione Granger, O=Apache Software Foundation, OU=Harmony, L=Hogwarts, ST=Hants, C=GB");
        String name = principal.getName();
        String expectedOuput = "CN=Hermione Granger,O=Apache Software Foundation,OU=Harmony,L=Hogwarts,ST=Hants,C=GB";
        assertEquals("Output order precedence problem", expectedOuput, name);
    }

    /**
     * @tests javax.security.auth.x500.X500Principal#X500Principal(java.lang.String, java.util.Map)
     */
    public void test_ConstructorLjava_lang_String_java_util_Map() {
        Map<String, String> keyword = new HashMap<String, String>();
        keyword.put("CN", "2.19");
        keyword.put("OU", "1.2.5.19");
        keyword.put("O", "1.2.5");
        X500Principal X500p = new X500Principal("CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US ,CN=DD", keyword);
        String name = X500p.getName();
        String expectedOut = "2.19=#130444756b65,1.2.5.19=#13084a617661536f6674,1.2.5=#131053756e204d6963726f73797374656d73,C=US,2.19=#13024444";
        assertEquals("Output order precedence problem", expectedOut, name);
    }

    /**
     * @tests javax.security.auth.x500.X500Principal#getName(java.lang.String)
     */
    public void test_getNameLjava_lang_String() {
        X500Principal principal = new X500Principal(
                "CN=Dumbledore, OU=Administration, O=Hogwarts School, C=GB");
        String canonical = principal.getName(X500Principal.CANONICAL);
        String expected = "cn=dumbledore,ou=administration,o=hogwarts school,c=gb";
        assertEquals("CANONICAL output differs from expected result", expected,
                canonical);
    }

    /**
     * @tests javax.security.auth.x500.X500Principal#getName(java.lang.String, java.util.Map)
     */
    public void test_getNameLjava_lang_String_java_util_Map() {
        Map<String, String> keyword = new HashMap<String, String>();
        keyword.put("CN", "2.19");
        keyword.put("OU", "1.2.5.19");
        keyword.put("O", "1.2.5");
        X500Principal X500p = new X500Principal("CN=Duke, OU=JavaSoft, O=Sun Microsystems, C=US ,CN=DD", keyword);
        keyword = new HashMap<String, String>();
        keyword.put("2.19", "mystring");
        String rfc1779Name = X500p.getName("RFC1779", keyword);
        String rfc2253Name = X500p.getName("RFC2253", keyword);
        String expected1779Out = "mystring=Duke, OID.1.2.5.19=JavaSoft, OID.1.2.5=Sun Microsystems, C=US, mystring=DD";
        String expected2253Out = "mystring=Duke,1.2.5.19=#13084a617661536f6674,1.2.5=#131053756e204d6963726f73797374656d73,C=US,mystring=DD";
        assertEquals("Output order precedence problem", expected1779Out, rfc1779Name);
        assertEquals("Output order precedence problem", expected2253Out, rfc2253Name);
        try {
            X500p.getName("CANONICAL", keyword);
            fail("Should throw IllegalArgumentException exception here");
        } catch (IllegalArgumentException e) {
            //expected IllegalArgumentException here
        }
    }

      private boolean testing = false;

    public void testStreamPosition() throws Exception {
        //this encoding is read from the file
        /*byte [] mess = {0x30, 0x30,
         0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41,
         0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41,
         1, 2, 3//extra bytes
         };
         */

        InputStream is = Support_Resources
                .getResourceStream("X500PrincipalTest.0.dat");
        X500Principal principal = new X500Principal(is);
        String s = principal.toString();
        assertEquals("CN=A, CN=B, CN=A, CN=B", s);
        byte[] restBytes = new byte[] { 0, 0, 0 };
        is.read(restBytes);
        assertEquals(restBytes[0], 1);
        assertEquals(restBytes[1], 2);
        assertEquals(restBytes[2], 3);
        is.close();
    }

    public void testStreamPosition_0() throws Exception {
        //this encoding is read from the file
        /*byte [] mess = {0x30, 0x30,
         0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41,
         0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41,
         };
         */

        InputStream is = Support_Resources
                .getResourceStream("X500PrincipalTest.1.dat");
        X500Principal principal = new X500Principal(is);
        String s = principal.toString();
        assertEquals("CN=A, CN=B, CN=A, CN=B", s);
        assertEquals(0, is.available());
        is.close();
    }

    public void testStreamPosition_1() throws Exception {
        byte[] mess = { 0x30, (byte) 0x81, (byte) 0x9A, 0x31, 0x0A, 0x30, 0x08,
                0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x5A, 0x31, 0x0A,
                0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x0B, 0x13, 0x01, 0x45,
                0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x06, 0x13,
                0x01, 0x44, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04,
                0x07, 0x13, 0x01, 0x43, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x0A, 0x13, 0x01, 0x42, 0x31, 0x15, 0x30, 0x08,
                0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41, 0x30, 0x09,
                0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x02, 0x43, 0x41, 0x31,
                0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x0B, 0x13, 0x01,
                0x45, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x06,
                0x13, 0x01, 0x44, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x07, 0x13, 0x01, 0x43, 0x31, 0x0A, 0x30, 0x08, 0x06,
                0x03, 0x55, 0x04, 0x0A, 0x13, 0x01, 0x42, 0x31, 0x15, 0x30,
                0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41, 0x30,
                0x09, 0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x02, 0x43, 0x41, 2,
                3, 4 };

        ByteArrayInputStream is = new ByteArrayInputStream(mess);
        X500Principal principal = new X500Principal(is);

        String s = principal.getName(X500Principal.RFC1779);
        assertEquals(
                "CN=A + ST=CA, O=B, L=C, C=D, OU=E, CN=A + ST=CA, O=B, L=C, C=D, OU=E, CN=Z",
                s);
        assertEquals(3, is.available());
        assertEquals(2, is.read());
        assertEquals(3, is.read());
        assertEquals(4, is.read());
    }

    public void testStreamPosition_2() throws Exception {
        byte[] mess = { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x13, 0x01, 0x41, 2 };
        ByteArrayInputStream is = new ByteArrayInputStream(mess);
        X500Principal principal = new X500Principal(is);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=A", s);
        assertEquals(1, is.available());
        assertEquals(2, is.read());
    }

    public void testEncodingFromFile() throws Exception {
        //this encoding is read from the file
        /*byte [] mess = {0x30, 0x30,
         0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41,
         0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41
         };
         */
        InputStream is = Support_Resources
                .getResourceStream("X500PrincipalTest.1.dat");
        X500Principal principal = new X500Principal(is);
        String s = principal.toString();
        assertEquals("CN=A, CN=B, CN=A, CN=B", s);
        is.close();
    }

    public void testEncodingFromEncoding() {
        byte[] arr1 = new X500Principal("O=Org.").getEncoded();
        byte[] arr2 = new X500Principal(new X500Principal("O=Org.")
                .getEncoded()).getEncoded();
        assertTrue(Arrays.equals(arr1, arr2));
    }

    /**
     * tests if the encoding is backed
     */
    public void testSafeEncoding() {
        byte[] mess = { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x13, 0x01, 0x41 };
        X500Principal principal = new X500Principal(mess);
        mess[mess.length - 1] = (byte) 0xFF;
        byte[] enc = principal.getEncoded();
        assertEquals(enc[mess.length - 1], 0x41);
    }

    /**
     * Inits X500Principal with byte array
     * gets toString
     * checks the result
     */
    public void testToString() throws Exception {
        byte[] mess = { 0x30, 0x18, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08, 0x06,
                0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41 };
        X500Principal principal = new X500Principal(mess);
        String s = principal.toString();
        assertNotNull(s);
    }

    /**
     * Inits X500Principal with byte array
     * gets hashCode
     * compares with expected value
     */
    public void testHashCode() throws Exception {
        byte[] mess = { 0x30, 0x18, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08, 0x06,
                0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41 };
        X500Principal principal = new X500Principal(mess);
        int hash = principal.hashCode();
        assertEquals(principal.getName(X500Principal.CANONICAL).hashCode(),
                hash);
    }

    /**
     * Inits X500Principal with byte array
     * Inits other X500Principal with equivalent string
     * checks if <code>equals</code> returns true for first against second one
     */
    public void testEquals() throws Exception {
        byte[] mess = { 0x30, 0x18, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08, 0x06,
                0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41 };
        X500Principal principal = new X500Principal(mess);
        X500Principal principal2 = new X500Principal("CN=A, CN=B");
        assertTrue(principal.equals(principal2));
    }

    /**
     * @tests javax.security.auth.x500.X500Principal#equals(Object)
     */
    public void test_equalsLjava_lang_Object() {
        X500Principal xp1 = new X500Principal(
                "C=US, ST=California, L=San Diego, O=Apache, OU=Project Harmony, CN=Test cert");
        assertEquals(
                "C=US,ST=California,L=San Diego,O=Apache,OU=Project Harmony,CN=Test cert",
                xp1.getName());
    }

    /**
     * Inits X500Principal with byte array, where Oid does fall into any keyword, but not given as a keyword
     * Value is given as hex value
     * (extra spaces are given)
     * gets Name in RFC1779 format
     * compares with expected value of name
     */
    public void testKWAsOid_RFC1779() throws Exception {
        String dn = "CN=A, OID.2.5.4.3  =    #130142";
        X500Principal principal = new X500Principal(dn);

        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=A, CN=B", s);
    }

    /**
     * Inits X500Principal with byte array, where Oid does fall into any keyword, but not given as a keyword
     * Value is given as hex value
     * (extra spaces are given)
     * gets Name in RFC2253 format
     * compares with expected value of name
     */
    public void testKWAsOid_RFC2253() throws Exception {
        String dn = "CN=A, OID.2.5.4.3 =  #130142";
        X500Principal principal = new X500Principal(dn);

        String s = principal.getName(X500Principal.RFC2253);
        assertEquals("CN=A,CN=B", s);
    }

    /**
     * Inits X500Principal with byte array, where Oid does fall into any keyword, but not given as a keyword
     * Value is given as hex value
     * (extra spaces are given)
     * gets Name in CANONICAL format
     * compares with expected value of name
     */
    public void testKWAsOid_CANONICAL() throws Exception {
        String dn = "CN=A, OID.2.5.4.3 =  #130142";
        X500Principal principal = new X500Principal(dn);

        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=a,cn=b", s);
    }

    /**
     * Inits X500Principal with byte array, where Oid does not fall into any keyword
     * gets Name in RFC1779 format
     * compares with expected value of name
     */
    public void testOid_RFC1779() throws Exception {
        byte[] mess = { 0x30, 0x18, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08, 0x06,
                0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41 };
        mess[8] = 0x60;
        X500Principal principal = new X500Principal(mess);

        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=A, OID.2.16.4.3=B", s);
    }

    /**
     * Inits X500Principal with byte array, where Oid does not fall into any keyword
     * gets Name in RFC2253 format
     * compares with expected value of name
     */
    public void testOid_RFC2253() throws Exception {
        byte[] mess = { 0x30, 0x18, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x13, 0x01, 0x4F, 0x31, 0x0A, 0x30, 0x08, 0x06,
                0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41 };
        mess[8] = 0x60;
        X500Principal principal = new X500Principal(mess);

        String s = principal.getName(X500Principal.RFC2253);
        assertEquals("CN=A,2.16.4.3=#13014f", s);
    }

    /**
     * Inits X500Principal with byte array, where Oid does not fall into any keyword
     * gets Name in CANONICAL format
     * compares with expected value of name
     */
    public void testOid_CANONICAL() throws Exception {
        byte[] mess = { 0x30, 0x18, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x13, 0x01, 0x4F, 0x31, 0x0A, 0x30, 0x08, 0x06,
                0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41 };
        mess[8] = 0x60;
        X500Principal principal = new X500Principal(mess);

        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=a,2.16.4.3=#13014f", s);
    }

    /**
     * Inits X500Principal with a string
     * gets encoded form
     * compares with expected byte array
     */
    public void testNameGetEncoding() throws Exception {
        byte[] mess = { 0x30, (byte) 0x81, (byte) 0x9A, 0x31, 0x0A, 0x30, 0x08,
                0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x5A, 0x31, 0x0A,
                0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x0B, 0x13, 0x01, 0x45,
                0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x06, 0x13,
                0x01, 0x44, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04,
                0x07, 0x13, 0x01, 0x43, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x0A, 0x13, 0x01, 0x42, 0x31, 0x15, 0x30, 0x08,
                0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41, 0x30, 0x09,
                0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x02, 0x43, 0x41, 0x31,
                0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x0B, 0x13, 0x01,
                0x45, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x06,
                0x13, 0x01, 0x44, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x07, 0x13, 0x01, 0x43, 0x31, 0x0A, 0x30, 0x08, 0x06,
                0x03, 0x55, 0x04, 0x0A, 0x13, 0x01, 0x42, 0x31, 0x15, 0x30,
                0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41, 0x30,
                0x09, 0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x02, 0x43, 0x41 };
        String dn = "CN=A+ST=CA,O=B,L=C,C=D,OU=E,CN=A+ST=CA,O=B,L=C,C=D,OU=E,CN=Z";
        X500Principal principal = new X500Principal(dn);
        byte[] s = principal.getEncoded();

        assertTrue(Arrays.equals(mess, s));
    }

    /**
     * Inits X500Principal with a string
     * gets encoded form
     * compares with expected byte array
     */
    public void testNameGetEncoding_01() throws Exception {
        byte[] mess = { 0x30, 0x18, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08, 0x06,
                0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41 };
        String dn = "CN=A,CN=B";
        X500Principal principal = new X500Principal(dn);
        byte[] s = principal.getEncoded();

        assertTrue(Arrays.equals(mess, s));
    }

    /**
     * Inits X500Principal with byte array
     * gets Name in RFC1779 format
     * compares with expected value of name
     */
    public void testGetName_RFC1779() throws Exception {
        byte[] mess = { 0x30, (byte) 0x81, (byte) 0x9A, 0x31, 0x0A, 0x30, 0x08,
                0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x5A, 0x31, 0x0A,
                0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x0B, 0x13, 0x01, 0x45,
                0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x06, 0x13,
                0x01, 0x44, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04,
                0x07, 0x13, 0x01, 0x43, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x0A, 0x13, 0x01, 0x42, 0x31, 0x15, 0x30, 0x08,
                0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41, 0x30, 0x09,
                0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x02, 0x43, 0x41, 0x31,
                0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x0B, 0x13, 0x01,
                0x45, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x06,
                0x13, 0x01, 0x44, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x07, 0x13, 0x01, 0x43, 0x31, 0x0A, 0x30, 0x08, 0x06,
                0x03, 0x55, 0x04, 0x0A, 0x13, 0x01, 0x42, 0x31, 0x15, 0x30,
                0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41, 0x30,
                0x09, 0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x02, 0x43, 0x41 };
        X500Principal principal = new X500Principal(mess);

        String s = principal.getName(X500Principal.RFC1779);
        assertEquals(
                "CN=A + ST=CA, O=B, L=C, C=D, OU=E, CN=A + ST=CA, O=B, L=C, C=D, OU=E, CN=Z",
                s);

    }

    /**
     * Inits X500Principal with byte array
     * gets Name in RFC2253 format
     * compares with expected value of name
     */
    public void testGetName_RFC2253() throws Exception {
        byte[] mess = { 0x30, (byte) 0x81, (byte) 0x9A, 0x31, 0x0A, 0x30, 0x08,
                0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x5A, 0x31, 0x0A,
                0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x0B, 0x13, 0x01, 0x45,
                0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x06, 0x13,
                0x01, 0x44, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04,
                0x07, 0x13, 0x01, 0x43, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x0A, 0x13, 0x01, 0x42, 0x31, 0x15, 0x30, 0x08,
                0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41, 0x30, 0x09,
                0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x02, 0x43, 0x41, 0x31,
                0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x0B, 0x13, 0x01,
                0x45, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x06,
                0x13, 0x01, 0x44, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x07, 0x13, 0x01, 0x43, 0x31, 0x0A, 0x30, 0x08, 0x06,
                0x03, 0x55, 0x04, 0x0A, 0x13, 0x01, 0x42, 0x31, 0x15, 0x30,
                0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41, 0x30,
                0x09, 0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x02, 0x43, 0x41 };
        X500Principal principal = new X500Principal(mess);

        String s = principal.getName(X500Principal.RFC2253);
        assertEquals(
                "CN=A+ST=CA,O=B,L=C,C=D,OU=E,CN=A+ST=CA,O=B,L=C,C=D,OU=E,CN=Z",
                s);
    }

    /**
     * Inits X500Principal with byte array
     * gets Name in CANONICAL format
     * compares with expected value of name
     */
    public void testGetName_CANONICAL() throws Exception {
        byte[] mess = { 0x30, (byte) 0x81, (byte) 0x9A, 0x31, 0x0A, 0x30, 0x08,
                0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x5A, 0x31, 0x0A,
                0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x0B, 0x13, 0x01, 0x45,
                0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x06, 0x13,
                0x01, 0x44, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04,
                0x07, 0x13, 0x01, 0x43, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x0A, 0x13, 0x01, 0x42, 0x31, 0x15, 0x30, 0x08,
                0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41, 0x30, 0x09,
                0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x02, 0x43, 0x41, 0x31,
                0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x0B, 0x13, 0x01,
                0x45, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x06,
                0x13, 0x01, 0x44, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x07, 0x13, 0x01, 0x43, 0x31, 0x0A, 0x30, 0x08, 0x06,
                0x03, 0x55, 0x04, 0x0A, 0x13, 0x01, 0x42, 0x31, 0x15, 0x30,
                0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41, 0x30,
                0x09, 0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x02, 0x43, 0x41 };
        X500Principal principal = new X500Principal(mess);

        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals(
                "CN=A+ST=CA,O=B,L=C,C=D,OU=E,CN=A+ST=CA,O=B,L=C,C=D,OU=E,CN=Z"
                        .toLowerCase(Locale.US), s);
    }

    /**
     * Inits X500Principal with byte array
     * gets Name in RFC1779 format
     * compares with expected value of name
     */
    public void testStreamGetName_RFC1779() throws Exception {
        byte[] mess = { 0x30, (byte) 0x81, (byte) 0x9A, 0x31, 0x0A, 0x30, 0x08,
                0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x5A, 0x31, 0x0A,
                0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x0B, 0x13, 0x01, 0x45,
                0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x06, 0x13,
                0x01, 0x44, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04,
                0x07, 0x13, 0x01, 0x43, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x0A, 0x13, 0x01, 0x42, 0x31, 0x15, 0x30, 0x08,
                0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41, 0x30, 0x09,
                0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x02, 0x43, 0x41, 0x31,
                0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x0B, 0x13, 0x01,
                0x45, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x06,
                0x13, 0x01, 0x44, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x07, 0x13, 0x01, 0x43, 0x31, 0x0A, 0x30, 0x08, 0x06,
                0x03, 0x55, 0x04, 0x0A, 0x13, 0x01, 0x42, 0x31, 0x15, 0x30,
                0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41, 0x30,
                0x09, 0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x02, 0x43, 0x41 };
        ByteArrayInputStream is = new ByteArrayInputStream(mess);
        X500Principal principal = new X500Principal(is);

        String s = principal.getName(X500Principal.RFC1779);
        assertEquals(
                "CN=A + ST=CA, O=B, L=C, C=D, OU=E, CN=A + ST=CA, O=B, L=C, C=D, OU=E, CN=Z",
                s);
    }

    /**
     * Inits X500Principal with byte array
     * gets Name in RFC2253 format
     * compares with expected value of name
     */
    public void testStreamGetName_RFC2253() throws Exception {
        byte[] mess = { 0x30, (byte) 0x81, (byte) 0x9A, 0x31, 0x0A, 0x30, 0x08,
                0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x5A, 0x31, 0x0A,
                0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x0B, 0x13, 0x01, 0x45,
                0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x06, 0x13,
                0x01, 0x44, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04,
                0x07, 0x13, 0x01, 0x43, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x0A, 0x13, 0x01, 0x42, 0x31, 0x15, 0x30, 0x08,
                0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41, 0x30, 0x09,
                0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x02, 0x43, 0x41, 0x31,
                0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x0B, 0x13, 0x01,
                0x45, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x06,
                0x13, 0x01, 0x44, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x07, 0x13, 0x01, 0x43, 0x31, 0x0A, 0x30, 0x08, 0x06,
                0x03, 0x55, 0x04, 0x0A, 0x13, 0x01, 0x42, 0x31, 0x15, 0x30,
                0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41, 0x30,
                0x09, 0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x02, 0x43, 0x41 };
        ByteArrayInputStream is = new ByteArrayInputStream(mess);
        X500Principal principal = new X500Principal(is);

        String s = principal.getName(X500Principal.RFC2253);
        assertEquals(
                "CN=A+ST=CA,O=B,L=C,C=D,OU=E,CN=A+ST=CA,O=B,L=C,C=D,OU=E,CN=Z",
                s);
    }

    /**
     * Inits X500Principal with byte array
     * gets Name in CANONICAL format
     * compares with expected value of name
     */
    public void testStreamGetName_CANONICAL() throws Exception {
        byte[] mess = { 0x30, (byte) 0x81, (byte) 0x9A, 0x31, 0x0A, 0x30, 0x08,
                0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x5A, 0x31, 0x0A,
                0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x0B, 0x13, 0x01, 0x45,
                0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x06, 0x13,
                0x01, 0x44, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04,
                0x07, 0x13, 0x01, 0x43, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x0A, 0x13, 0x01, 0x42, 0x31, 0x15, 0x30, 0x08,
                0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41, 0x30, 0x09,
                0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x02, 0x43, 0x41, 0x31,
                0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x0B, 0x13, 0x01,
                0x45, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x06,
                0x13, 0x01, 0x44, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x07, 0x13, 0x01, 0x43, 0x31, 0x0A, 0x30, 0x08, 0x06,
                0x03, 0x55, 0x04, 0x0A, 0x13, 0x01, 0x42, 0x31, 0x15, 0x30,
                0x08, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41, 0x30,
                0x09, 0x06, 0x03, 0x55, 0x04, 0x08, 0x13, 0x02, 0x43, 0x41 };
        ByteArrayInputStream is = new ByteArrayInputStream(mess);
        X500Principal principal = new X500Principal(is);

        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals(
                "CN=A+ST=CA,O=B,L=C,C=D,OU=E,CN=A+ST=CA,O=B,L=C,C=D,OU=E,CN=Z"
                        .toLowerCase(Locale.US), s);
    }

    /**
     * Inits X500Principal with a string, where OID does not fall into any keyword
     * gets encoded form
     * inits new X500Principal with the encoding
     * gets string in RFC1779 format
     * compares with expected value
     */
    public void testGetName_EncodingWithWrongOidButGoodName_SeveralRDNs_RFC1779()
            throws Exception {
        String dn = "OID.2.16.4.3=B; CN=A";
        X500Principal principal = new X500Principal(dn);
        byte[] enc = principal.getEncoded();
        X500Principal principal2 = new X500Principal(enc);
        String s = principal2.getName(X500Principal.RFC1779);
        assertEquals("OID.2.16.4.3=B, CN=A", s);

    }

    /**
     * Inits X500Principal with a string, where OID does not fall into any keyword
     * gets encoded form
     * inits new X500Principal with the encoding
     * gets string in RFC2253 format
     * compares with expected value
     */
    public void testGetName_EncodingWithWrongOidButGoodName_SeveralRDNs_RFC2253()
            throws Exception {
        String dn = "OID.2.16.4.3=B; CN=A";
        X500Principal principal = new X500Principal(dn);
        byte[] enc = principal.getEncoded();
        X500Principal principal2 = new X500Principal(enc);
        String s = principal2.getName(X500Principal.RFC2253);
        assertEquals("2.16.4.3=#130142,CN=A", s);

    }

    /**
     * Inits X500Principal with a string, where OID does not fall into any keyword
     * gets encoded form
     * inits new X500Principal with the encoding
     * gets string in CANONICAL format
     * compares with expected value
     */
    public void testGetName_EncodingWithWrongOidButGoodName_SeveralRDNs_CANONICAL()
            throws Exception {
        String dn = "OID.2.16.4.3=B; CN=A";
        X500Principal principal = new X500Principal(dn);
        byte[] enc = principal.getEncoded();
        X500Principal principal2 = new X500Principal(enc);
        String s = principal2.getName(X500Principal.CANONICAL);
        assertEquals("2.16.4.3=#130142,cn=a", s);

    }

    /**
     * Inits X500Principal with a string, where OID does not fall into any keyword
     * gets string in RFC1779 format
     * compares with expected value
     */
    public void testGetName_wrongOidButGoodName_RFC1779() throws Exception {
        String dn = "OID.2.16.4.3=B + CN=A";
        X500Principal principal = new X500Principal(dn);

        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("OID.2.16.4.3=B + CN=A", s);
    }

    /**
     * Inits X500Principal with a string, where OID does not fall into any keyword
     * gets string in RFC2253 format
     * compares with expected value
     */
    public void testGetName_wrongOidButGoodName_RFC2253() throws Exception {
        String dn = "OID.2.16.4.3=B + CN=A";
        X500Principal principal = new X500Principal(dn);

        String s = principal.getName(X500Principal.RFC2253);
        assertEquals("2.16.4.3=#130142+CN=A", s);
    }

    /**
     * Inits X500Principal with a string, there are multiple AVAs
     * gets string in CANONICAL format
     * compares with expected value paying attention on sorting order of AVAs
     */
    public void testGetName_CANONICAL_SortOrder() throws Exception {
        String dn = "ST=C + CN=A; OU=B + CN=D";
        X500Principal principal = new X500Principal(dn);

        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=a+st=c,cn=d+ou=b", s);

    }

    /**
     * Inits X500Principal with a string, there are multiple AVAs and Oid which does not fall into any keyword
     * gets string in CANONICAL format
     * compares with expected value paying attention on sorting order of AVAs
     */
    public void testGetName_CANONICAL_SortOrder_01() throws Exception {
        String dn = "OID.2.16.4.3=B + CN=A";
        X500Principal principal = new X500Principal(dn);

        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=a+2.16.4.3=#130142", s);

    }

    /**
     * Inits X500Principal with a string, there are multiple AVAs and Oid which does not fall into any keyword, and value given in hex format
     * gets string in CANONICAL format
     * compares with expected value paying attention on sorting order of AVAs
     */
    public void testGetName_CANONICAL_SortOrder_02() throws Exception {
        String dn = "OID.2.16.4.3=#13024220+ CN=A";
        X500Principal principal = new X500Principal(dn);

        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=a+2.16.4.3=#13024220", s);

    }

    /**
     * Inits X500Principal with a string, there are multiple AVAs and 2 Oids which do not fall into any keyword
     * gets string in CANONICAL format
     * compares with expected value paying attention on sorting order of AVAs
     */
    public void testGetName_CANONICAL_SortOrder_03() throws Exception {
        String dn = "OID.2.16.4.9=A + OID.2.16.4.3=B";
        X500Principal principal = new X500Principal(dn);

        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("2.16.4.3=#130142+2.16.4.9=#130141", s);

    }

    /**
     * Inits X500Principal with a string, there are multiple AVAs and 2 Oids which do not fall into any keyword
     * gets string in CANONICAL format
     * compares with expected value paying attention on sorting order of AVAs
     */
    public void testGetName_CANONICAL_SortOrder_04() throws Exception {
        String dn = "OID.2.2.2.2=A + OID.1.1.1.1=B";
        X500Principal principal = new X500Principal(dn);

        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("1.1.1.1=#130142+2.2.2.2=#130141", s);

    }

    /**
     * Inits X500Principal with a string, there are multiple AVAs and 2 Oids which do not fall into any keyword
     * gets string in CANONICAL format
     * compares with expected value paying attention on sorting order of AVAs
     */
    public void testGetName_CANONICAL_SortOrder_05() throws Exception {
        String dn = "OID.2.16.4.9=A + OID.2.16.4=B";
        X500Principal principal = new X500Principal(dn);

        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("2.16.4=#130142+2.16.4.9=#130141", s);

    }

    /**
     * Inits X500Principal with a string, there are multiple AVAs and 2 Oids which do not fall into any keyword
     * gets string in CANONICAL format
     * compares with expected value paying attention on sorting order of AVAs
     */
    public void testGetName_CANONICAL_SortOrder_06() throws Exception {
        String dn = "OID.1.1.2=A + OID.1.2=B";
        X500Principal principal = new X500Principal(dn);

        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("1.1.2=#130141+1.2=#130142", s);

    }

    /**
     * Inits X500Principal with a string, there are multiple AVAs and 2 Oids which do not fall into any keyword
     * gets string in CANONICAL format
     * compares with expected value paying attention on sorting order of AVAs
     */
    public void testGetName_CANONICAL_SortOrder_07() throws Exception {
        String dn = "OID.1.1.1=A + OID.1.1=B";
        X500Principal principal = new X500Principal(dn);

        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("1.1=#130142+1.1.1=#130141", s);

    }

    /**
     * FIXME test is failed - implement unicode normalization
     *
     * @throws Exception
     */
    public void testGetNameUnicodeNormalized() throws Exception {
        String unicodeStr = "CN= \u0401\u0410";
        X500Principal principal = new X500Principal(unicodeStr);
        principal.getName(X500Principal.CANONICAL);
    }

    /**
     * Inits X500Principal with empty string
     * gets encoding
     * compares with expected encoding
     */
    public void testEmptyInputName() {
        String dn = "CN=\"\"";
        byte[] mess = { 0x30, 0x0B, 0x31, 0x09, 0x30, 0x07, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x13, 0x00 };
        X500Principal principal = new X500Principal(dn);
        assertTrue(Arrays.equals(mess, principal.getEncoded()));
    }

    /**
     * Inits X500Principal with string as single escaped space
     * gets encoding
     * compares with expected encoding
     */
    public void testNameSingleEscapedSpace() {
        String dn = "CN=\\ ";
        byte[] mess = { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x13, 0x01, 0x20 };
        X500Principal principal = new X500Principal(dn);
        assertTrue(Arrays.equals(mess, principal.getEncoded()));
    }

    /**
     * Inits X500Principal with string with spaces
     * gets Name in RFC2253 format
     * compares with expected value of name
     */
    public void testNameOnlySpaces_RFC1779() {
        String dn = "CN=\"  \"";
        X500Principal principal = new X500Principal(dn);
        assertEquals("CN=\"  \"", principal.getName(X500Principal.RFC1779));
    }

    /**
     * Inits X500Principal with string with spaces
     * gets Name in RFC2253 format
     * compares with expected value of name
     */
    public void testNameOnlySpaces_RFC2253() {
        String dn = "CN=\"  \"";
        X500Principal principal = new X500Principal(dn);
        assertEquals("CN=\\ \\ ", principal.getName(X500Principal.RFC2253));
    }

    /**
     * Inits X500Principal with string with only spaces,
     * gets Name in CANONICAL format:leading and trailing white space
     * chars are removed even string doesn't have other chars (bug???)
     */
    public void testNameOnlySpaces_CANONICAL() {
        String dn = "CN=\"  \"";
        X500Principal principal = new X500Principal(dn);
        assertEquals("cn=", principal.getName(X500Principal.CANONICAL));
    }

    ///*** Negative Tests ***///

    /**
     * Inits X500Principal with string, where DN name is improper "CNN"
     * checks if proper exception is thrown
     */
    public void testIllegalInputName() {
        try {
            String dn = "CNN=A";
            new X500Principal(dn);
            fail("No IllegalArgumentException on improper input name \"CNN\"");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with string, where there is leading ';'
     * checks if proper exception is thrown
     */
    public void testIllegalInputName_01() {
        try {
            String dn = ";CN=A";
            new X500Principal(dn);
            fail("No IllegalArgumentException on leading ';' in input name");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with string, where there is leading '='
     * checks if proper exception is thrown
     */
    public void testIllegalInputName_02() {
        try {
            String dn = "=CN=A";
            new X500Principal(dn);
            fail("No IllegalArgumentException on leading '=' in input name");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with string, where there is no value
     * checks if proper exception is thrown
     */
    public void testEmptyInputName_0() {
        String dn = "CN=";
        byte[] mess = { 0x30, 0x0B, 0x31, 0x09, 0x30, 0x07, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x13, 0x00 };
        X500Principal principal = new X500Principal(dn);
        assertTrue(Arrays.equals(mess, principal.getEncoded()));
    }

    public void testEmptyInputName_1() {
        String dn = "CN=\"\", C=\"\"";
        X500Principal principal = new X500Principal(dn);
        dn = "CN=, C=";
        X500Principal principal2 = new X500Principal(dn);
        assertTrue(Arrays.equals(principal.getEncoded(), principal2
                .getEncoded()));

    }

    public void testEmptyInputName_2() {
        String dn = "CN=\"\" + OU=A, C=\"\"";
        X500Principal principal = new X500Principal(dn);
        dn = "CN=+OU=A, C=";
        X500Principal principal2 = new X500Principal(dn);
        assertTrue(Arrays.equals(principal.getEncoded(), principal2
                .getEncoded()));

    }

    public void testIllegalInputName_15() {
        try {
            String dn = "CN=,C";
            new X500Principal(dn);
            fail("No IllegalArgumentException on improper attribute value");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testIllegalInputName_16() {
        try {
            String dn = "CN=,C=+";
            new X500Principal(dn);
            fail("No IllegalArgumentException on improper attribute value");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with string, where value is given in wrong hex format
     * checks if proper exception is thrown
     */
    public void testIllegalInputName_04() {
        try {
            String dn = "CN=#XYZ";
            new X500Principal(dn);
            fail("No IllegalArgumentException on improper hex value");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with string, where value is given with special chars
     * checks if proper exception is thrown
     */
    public void testIllegalInputName_05() {
        try {
            String dn = "CN=X+YZ";
            new X500Principal(dn);
            fail("No IllegalArgumentException on improper attribute value");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with string, where value is given with special chars
     * Compatibility issue: according RFC 2253 such string is invalid
     * but we accept it, not string char is escaped
     */
    public void testIllegalInputName_06() {
        String dn = "CN=X=YZ";
        X500Principal p = new X500Principal(dn);
        assertEquals("CN=X\\=YZ", p.getName(X500Principal.RFC2253));
    }

    /**
     * Inits X500Principal with string, where value is given with not string chars
     * Compatibility issue: according RFC 2253 such string is invalid
     * but we accept it, not string char is escaped
     */
    public void testIllegalInputName_07() {
        String dn = "CN=X\"YZ";
        X500Principal p = new X500Principal(dn);
        assertEquals("CN=X\\\"YZ", p.getName(X500Principal.RFC2253));
    }

    /**
     * Inits X500Principal with string, where value is given with special chars
     * Compatibility issue: according RFC 2253 such string is invalid
     * but we accept it, special char is escaped
     */
    public void testIllegalInputName_08() {
        String dn = "CN=X<YZ";
        X500Principal p = new X500Principal(dn);
        assertEquals("CN=X\\<YZ", p.getName(X500Principal.RFC2253));
    }

    /**
     * Inits X500Principal with string, where value is given with special chars
     * checks if proper exception is thrown
     */
    public void testIllegalInputName_09() {
        try {
            String dn = "CN=#";
            new X500Principal(dn);
            fail("No IllegalArgumentException on improper attribute hex value");
        } catch (IllegalArgumentException e) {
            //ignore
        }

    }

    /**
     * Inits X500Principal with string, where value is given with special chars
     * checks if proper exception is thrown
     */
    public void testIllegalInputName_10() {
        try {
            String dn = "CN=#13";
            new X500Principal(dn);
            fail("No IllegalArgumentException on improper attribute hex value");
        } catch (IllegalArgumentException e) {
            //ignore
        }

    }

    /**
     * Inits X500Principal with string, where value is given with special chars
     * checks if proper exception is thrown
     */
    public void testIllegalInputName_11() {
        try {
            String dn = "CN=#1301";
            new X500Principal(dn);
            fail("No IllegalArgumentException on improper attribute hex value");
        } catch (IllegalArgumentException e) {
            //ignore
        }

    }

    /**
     * Inits X500Principal with string, where value is given with special chars
     * checks if proper exception is thrown
     */
    public void testIllegalInputName_12() {
        try {
            String dn = "CN=#13010101";
            new X500Principal(dn);
            fail("No IllegalArgumentException on improper attribute hex value");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with string, where value is given with special chars
     * checks if proper exception is thrown
     */
    public void testIllegalInputName_13() {
        try {
            String dn = "CN=# 0";
            new X500Principal(dn);
            fail("No IllegalArgumentException on improper attribute hex value");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with string, where value is given in hex format, but improper tag
     * checks if it is ignored
     */
    public void testSemiIllegalInputName_14() {
        String dn = "CN=#7E0142";
        new X500Principal(dn);
    }

    /**
     * Change rev/d1c04dac850d upstream addresses the case of the string CN=prefix\<>suffix.
     *
     * Before said change, the string can be used to construct an X500Principal, although according
     * to RFC2253 is not possible. Also, characters after '<' are ignored. We have tests documenting
     * that we allow such strings, like testIllegalInputName_07, so we modified the change as to
     * allow the string. We check that the characters after '<' are not ignored.
     *
     * Note: the string CN=prefix\<>suffix in the test is escaped as CN=prefix\\<>suffix
     */
    /*
    public void testSemiIllegalInputName_15() {
        String dn = "CN=prefix\\<>suffix";

        X500Principal principal = new X500Principal(dn);
        assertEquals("CN=\"prefix<>suffix\"", principal.getName(X500Principal.RFC1779));
        assertEquals("CN=prefix\\<\\>suffix", principal.getName(X500Principal.RFC2253));
        assertEquals("cn=prefix\\<\\>suffix", principal.getName(X500Principal.CANONICAL));
    }
     */

    public void testInitClause() {
        try {
            byte[] mess = { 0x30, 0x18, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                    0x55, 0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08,
                    0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41 };
            mess[3] = 0x12;//length field
            new X500Principal(mess);

            fail("No IllegalArgumentException on input array with improper length field");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with byte array = null
     * checks if proper exception is thrown
     */
    public void testIllegalInputArray_0() {
        try {
            byte[] mess = null;
            new X500Principal(mess);
            fail("No IllegalArgumentException on input array with improper length field");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with byte array with wrong length field
     * checks if proper exception is thrown
     */
    public void testIllegalInputArray() {
        try {
            byte[] mess = { 0x30, 0x18, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                    0x55, 0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08,
                    0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41 };
            mess[3] = 0x12;//length field
            new X500Principal(mess);

            fail("No IllegalArgumentException on input array with improper length field");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with input stream with wrong length field
     * checks if proper exception is thrown
     */
    public void testIllegalInputArray_is() {
        try {
            byte[] mess = { 0x30, 0x18, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                    0x55, 0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08,
                    0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41 };
            mess[3] = 0x12;//length field
            ByteArrayInputStream is = new ByteArrayInputStream(mess);
            new X500Principal(is);

            fail("No IllegalArgumentException on input array with improper length field");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with byte array with wrong inner Sequence tag field
     * checks if proper exception is thrown
     */
    public void testIllegalInputArray_01() {
        try {
            byte[] mess = { 0x30, 0x18, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                    0x55, 0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08,
                    0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41 };
            mess[4] = 0x12;//inner Sequence tag field
            new X500Principal(mess);

            fail("No IllegalArgumentException on input array with improper inner Sequence tag field");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with byte array with wrong last byte of OID
     * checks if proper exception is thrown
     */
    public void testIllegalInputArray_02() {
        try {
            byte[] mess = { 0x30, 0x18, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                    0x55, 0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08,
                    0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41 };
            mess[10] = (byte) 0xFE;//last byte of OID
            new X500Principal(mess);

            fail("No IllegalArgumentException on input array with improper last byte of OID");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with byte array with wrong length of OID
     * checks if proper exception is thrown
     */
    public void testIllegalInputArray_03() {
        try {
            byte[] mess = { 0x30, 0x18, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                    0x55, 0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08,
                    0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41 };
            mess[7] = 2;//length of OID
            new X500Principal(mess);

            fail("No IllegalArgumentException on input array with improper length of OID");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with byte array with wrong tag of value
     * checks if it is ignored
     */
    public void testSemiIllegalInputArray_04() {
        byte[] mess = { 0x30, 0x18, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08, 0x06,
                0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41 };
        mess[11] = (byte) 0x0F;//tag of value
        new X500Principal(mess);
    }

    /**
     * Inits X500Principal with byte array with wrong length of value
     * checks if proper exception is thrown
     */
    public void testIllegalInputArray_05() {
        try {
            byte[] mess = { 0x30, 0x18, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                    0x55, 0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08,
                    0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41 };
            mess[12] = 2;//length of value
            new X500Principal(mess);

            fail("No IllegalArgumentException on input array with improper length of value");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with input stream with wrong length of value
     * checks if proper exception is thrown
     */
    public void testIllegalInputArray_05_is() {
        try {
            byte[] mess = { 0x30, 0x18, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                    0x55, 0x04, 0x03, 0x13, 0x01, 0x42, 0x31, 0x0A, 0x30, 0x08,
                    0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x01, 0x41 };
            mess[12] = 2;//length of value
            ByteArrayInputStream is = new ByteArrayInputStream(mess);
            new X500Principal(is);

            fail("No IllegalArgumentException on input array with improper length of value");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with string
     * Calls getName with improper parameter as format
     * checks if proper exception is thrown
     */
    public void testIllegalFormat() {
        try {
            String dn = "CN=A";
            X500Principal principal = new X500Principal(dn);
            principal.getName("WRONG FORMAT");
            fail("No IllegalArgumentException on improper parameter as format");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with a string, there are multiple AVAs and Oid which does not fall into any keyword
     * Gets encoding
     * Inits other X500Principal with the encoding
     * gets string in RFC1779 format
     * compares with expected value paying attention on sorting order of AVAs
     */
    public void testGetName_EncodingWithWrongOidButGoodName_MultAVA_RFC1779()
            throws Exception {
        String dn = "OID.2.16.4.3=B + CN=A";
        X500Principal principal = new X500Principal(dn);
        byte[] enc = principal.getEncoded();
        X500Principal principal2 = new X500Principal(enc);
        String s = principal2.getName(X500Principal.RFC1779);
        assertTrue("OID.2.16.4.3=B + CN=A".equals(s) ||
            "CN=A + OID.2.16.4.3=B".equals(s));

    }

    /**
     * Inits X500Principal with a string, there are multiple AVAs and Oid which does not fall into any keyword
     * Gets encoding
     * Inits other X500Principal with the encoding
     * gets string in RFC2253 format
     * compares with expected value paying attention on sorting order of AVAs
     */
    public void testGetName_EncodingWithWrongOidButGoodName_MultAVA_RFC2253()
            throws Exception {
        String dn = "OID.2.16.4.3=B + CN=A";
        X500Principal principal = new X500Principal(dn);
        byte[] enc = principal.getEncoded();
        X500Principal principal2 = new X500Principal(enc);
        String s = principal2.getName(X500Principal.RFC2253);
        assertTrue("2.16.4.3=#130142+CN=A".equals(s) ||
            "CN=A+2.16.4.3=#130142".equals(s));

    }

    /**
     * Inits X500Principal with a string, there are multiple AVAs and Oid which does not fall into any keyword
     * Gets encoding
     * Inits other X500Principal with the encoding
     * gets string in CANONICAL format
     * compares with expected value paying attention on sorting order of AVAs
     */
    public void testGetName_EncodingWithWrongOidButGoodName_MultAVA_CANONICAL()
            throws Exception {
        String dn = "OID.2.16.4.3=B + CN=A";
        X500Principal principal = new X500Principal(dn);
        byte[] enc = principal.getEncoded();
        X500Principal principal2 = new X500Principal(enc);
        String s = principal2.getName(X500Principal.CANONICAL);
        assertEquals("cn=a+2.16.4.3=#130142", s);

    }

    /**
     * Inits X500Principal with byte array, where there are leading and tailing spaces
     * gets Name in RFC1779 format
     * compares with expected value of name
     */
    public void testNameSpaceFromEncoding_RFC1779() throws Exception {
        byte[] mess = { 0x30, 0x0E, 0x31, 0x0C, 0x30, 0x0A, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x13, 0x03, 0x20, 0x41, 0x20, };
        X500Principal principal = new X500Principal(mess);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=\" A \"", s);

    }

    /**
     * Inits X500Principal with byte array, where there are leading and tailing spaces
     * gets Name in RFC2253 format
     * compares with expected value of name
     */
    public void testNameSpaceFromEncoding_RFC2253() throws Exception {
        byte[] mess = { 0x30, 0x0E, 0x31, 0x0C, 0x30, 0x0A, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x13, 0x03, 0x20, 0x41, 0x20, };
        X500Principal principal = new X500Principal(mess);
        String s = principal.getName(X500Principal.RFC2253);
        assertEquals("CN=\\ A\\ ", s);

    }

    /**
     * Inits X500Principal with byte array, where there are leading and tailing spaces
     * gets Name in CANONICAL format
     * compares with expected value of name
     */
    public void testNameSpaceFromEncoding_CANONICAL() throws Exception {
        byte[] mess = { 0x30, 0x0E, 0x31, 0x0C, 0x30, 0x0A, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x13, 0x03, 0x20, 0x41, 0x20, };
        X500Principal principal = new X500Principal(mess);
        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=a", s);

    }

    /**
     * Inits X500Principal with byte array, where there are special characters
     * gets Name in RFC1779 format
     * compares with expected value of name, checks if the string is in quotes
     */
    public void testNameSpecialCharsFromEncoding_RFC1779() throws Exception {
        byte[] mess = { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x0C, 0x02, 0x3B, 0x2C };
        X500Principal principal = new X500Principal(mess);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=\";,\"", s);

    }

    /**
     * Inits X500Principal with byte array, where there are special characters
     * gets Name in RFC1779 format
     * compares with expected value of name, checks if the characters are escaped
     */
    public void testNameSpecialCharsFromEncoding_RFC2253() throws Exception {
        byte[] mess = { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x0C, 0x02, 0x3B, 0x2C };
        X500Principal principal = new X500Principal(mess);
        String s = principal.getName(X500Principal.RFC2253);
        assertEquals("CN=\\;\\,", s);

    }

    /**
     * Inits X500Principal with byte array, where there are special characters
     * gets Name in CANONICAL format
     * compares with expected value of name, checks if the characters are escaped
     */
    public void testNameSpecialCharsFromEncoding_CANONICAL() throws Exception {
        byte[] mess = { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03, 0x55,
                0x04, 0x03, 0x0C, 0x02, 0x3B, 0x2C };
        X500Principal principal = new X500Principal(mess);
        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=\\;\\,", s);

    }

    /**
     * Inits X500Principal with the string with special characters - \"B
     * gets Name in RFC1779 format
     * compares with expected value of name - "\B"
     */
    public void testNameSpecialChars_RFC1779() throws Exception {
        String dn = "CN=A,CN=\\\"B";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=A, CN=\"\\\"B\"", s);

    }

    /**
     * Inits X500Principal with the string with special characters - \"B
     * gets Name in RFC2253 format
     * compares with expected value of name - "\B"
     */
    public void testNameSpecialChars_RFC2253() throws Exception {
        String dn = "CN=A,CN=\\\"B";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC2253);
        assertEquals("CN=A,CN=\\\"B", s);

    }

    /**
     * Inits X500Principal with the string with special characters - \"B
     * gets Name in CANONICAL format
     * compares with expected value of name - "\b"
     */
    public void testNameSpecialChars_CANONICAL() throws Exception {
        String dn = "CN=A,CN=\\\"B";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=a,cn=\\\"b", s);

    }

    /**
     * Inits X500Principal with the string with special characters - \\nB
     * gets Name in RFC1779 format
     * compares with expected value of name - "\nB"
     */
    public void testNameSpecialChars_RFC1779_01() throws Exception {
        String dn = "CN=\\\nB";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=\"\nB\"", s);
    }

    /**
     * Inits X500Principal with the string with special characters - \\nB
     * gets Name in RFC2253 format
     * compares with expected value of name - \nB
     */
    public void testNameSpecialChars_RFC2253_01() throws Exception {
        X500Principal p = new X500Principal("CN=\\\nB");
        assertEquals("CN=\nB", p.getName(X500Principal.RFC2253));
    }

    /**
     * Inits X500Principal with the string with special characters - \\nB
     * gets Name in CANONICAL format
     * compares with expected value of name - \\nb
     */
    public void testNameSpecialChars_CANONICAL_01() throws Exception {
        //FIXME testNameSpecialChars_RFC2253_01
        //        String dn = "CN=\\\nB";
        //        X500Principal principal = new X500Principal(dn);
        //        String s = principal.getName(X500Principal.CANONICAL);
        //        assertEquals("cn=b", s);

    }

    /**
     * Inits X500Principal with the string with special characters - \\B
     * gets Name in RFC1779 format
     * compares with expected value of name - "\B"
     */
    public void testNameSpecialChars_RFC1779_02() throws Exception {
        String dn = "CN=\\\\B";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=\"\\\\B\"", s);

    }

    /**
     * Inits X500Principal with the string with special characters - \\B
     * gets Name in RFC2253 format
     * compares with expected value of name - \\B
     */
    public void testNameSpecialChars_RFC2253_02() throws Exception {
        String dn = "CN=\\\\B";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC2253);
        assertEquals("CN=\\\\B", s);

    }

    /**
     * Inits X500Principal with the string with special characters - \\B
     * gets Name in CANONICAL format
     * compares with expected value of name - \\b
     */
    public void testNameSpecialChars_CANONICAL_02() throws Exception {
        String dn = "CN=\\\\B";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=\\\\b", s);

    }

    /**
     * Inits X500Principal with the string with special characters - ABC"DEF"
     * gets encoding
     * compares with expected encoding
     */
    public void testNameWithQuotation() throws Exception {
        String dn = "CN=\"ABCDEF\"";

        X500Principal principal = new X500Principal(dn);
        byte[] enc = principal.getEncoded();
        assertTrue(Arrays.equals(new byte[] { 0x30, 0x11, 0x31, 0x0F, 0x30,
                0x0D, 0x06, 0x03, 0x55, 0x04, 0x03, 0x13, 0x06, 0x41, 0x42,
                0x43, 0x44, 0x45, 0x46 }, enc));

    }

    /**
     * Inits X500Principal with the string with special characters - "ABCDEF
     * checks if the proper exception is thrown
     */
    public void testNameWithQuotation_01() throws Exception {
        String dn = "CN=\"ABCDEF";
        try {
            new X500Principal(dn);
            fail("No IllegalArgumentException on string with no closing quotations");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with the string with special characters - ABC"D#EF"
     * gets encoding
     * compares with expected encoding
     */
    public void testNameWithQuotation_02() throws Exception {
        String dn = "CN=\"ABCD#EF\"";
        X500Principal principal = new X500Principal(dn);
        byte[] enc = principal.getEncoded();
        assertTrue(Arrays.equals(new byte[] { 0x30, 0x12, 0x31, 0x10, 0x30,
                0x0E, 0x06, 0x03, 0x55, 0x04, 0x03, 0x0C, 0x07, 0x41, 0x42,
                0x43, 0x44, 0x23, 0x45, 0x46 }, enc));
    }

    /**
     * Inits X500Principal with the string with special characters - ABC"DEF"
     * Compatibility issue: according RFC 2253 such string is invalid
     * but we accept it, not string char is escaped
     */
    public void testNameWithQuotation_03() throws Exception {
        String dn = "CN=ABC\"DEF\"";
        X500Principal principal = new X500Principal(dn);
        assertEquals("CN=ABC\\\"DEF\\\"", principal
                .getName(X500Principal.RFC2253));
    }

    /**
     * Inits X500Principal with the string with special characters - ABC"DEF"
     * gets Name in RFC1779 format
     * compares with expected value of name - "ABCDEF"
     */
    public void testNameSpecialChars_RFC1779_03() throws Exception {
        String dn = "CN=\"ABCDEF\"";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=ABCDEF", s);

    }

    /**
     * Inits X500Principal with the string with special characters - ABC"DEF"
     * gets Name in RFC2253 format
     * compares with expected value of name - ABC"DEF"
     */
    public void testNameSpecialChars_RFC2253_03() throws Exception {
        String dn = "CN=\"ABCDEF\"";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC2253);
        assertEquals("CN=ABCDEF", s);

    }

    /**
     * Inits X500Principal with the string with special characters - ABC"DEF"
     * gets Name in CANONICAL format
     * compares with expected value of name - abc"def"
     */
    public void testNameSpecialChars_CANONICAL_03() throws Exception {
        String dn = "CN=\"ABCDEF\"";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=abcdef", s);

    }

    /**
     * Inits X500Principal with the string with special characters - ABC"D#EF"
     * gets Name in RFC1779 format
     * compares with expected value of name - "ABCD#EF"
     */
    public void testNameSpecialChars_RFC1779_04() throws Exception {
        String dn = "CN=\"ABCD#EF\"";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=\"ABCD#EF\"", s);

    }

    /**
     * Inits X500Principal with the string with special characters - ABC"D#EF"
     * gets Name in RFC1779 format
     * compares with expected value of name - ABCD\#EF
     */
    public void testNameSpecialChars_RFC2253_04() throws Exception {
        String dn = "CN=\"ABCD#EF\"";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC2253);
        assertEquals("CN=ABCD\\#EF", s);

    }

    /**
     * Inits X500Principal with the string with special characters - ABC"D#EF"
     * gets Name in RFC1779 format
     * compares with expected value of name - abc"d#ef"
     */
    public void testNameSpecialChars_CANONICAL_04() throws Exception {
        String dn = "CN=\"ABCD#EF\"";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=abcd#ef", s);

    }

    /**
     * Inits X500Principal with the string with special characters - X#YZ
     * gets Name in RFC1779 format
     * compares with expected value of name - "X#YZ"
     */
    public void testNameSpecialChars_RFC1779_05() {
        String dn = "CN=X#YZ";
        X500Principal principal = new X500Principal(dn);

        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=\"X#YZ\"", s);

    }

    /**
     * Inits X500Principal with the string with special characters - X#YZ
     * gets Name in RFC2253 format
     * compares with expected value of name - X\#YZ
     */
    public void testNameSpecialChars_RFC2253_05() {
        String dn = "CN=X#YZ";
        X500Principal principal = new X500Principal(dn);

        String s = principal.getName(X500Principal.RFC2253);

        assertEquals("CN=X\\#YZ", s);

    }

    /**
     * Inits X500Principal with the string with special characters - X#YZ
     * gets Name in CANONICAL format
     * compares with expected value of name - x#yz
     */
    public void testNameSpecialChars_CANONICAL_05() {
        String dn = "CN=X#YZ";
        X500Principal principal = new X500Principal(dn);

        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=x#yz", s);

    }

    /**
     * Inits X500Principal with the string with special characters - CN=\#XYZ
     * gets Name in RFC1779 format
     * compares with expected value of name - CN="#XYZ"
     */
    public void testNameSpecialChars_RFC1779_6() throws Exception {
        String dn = "CN=\\#XYZ";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=\"#XYZ\"", s);

    }

    /**
     * Inits X500Principal with the string with special characters - CN=\#XYZ
     * gets Name in RFC2253 format
     * compares with expected value of name - CN=\#XYZ
     */
    public void testNameSpecialChars_RFC2253_6() throws Exception {
        String dn = "CN=\\#XYZ";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC2253);
        assertEquals("CN=\\#XYZ", s);
    }

    /**
     * Inits X500Principal with the string with special characters - CN=\#XYZ
     * gets Name in CANONICAL format
     * compares with expected value of name - cn=\#xyz
     */
    public void testNameSpecialChars_CANONICAL_6() throws Exception {
        String dn = "CN=\\#XYZ";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=\\#xyz", s);
    }

    /**
     * Inits X500Principal with the string with special characters - B\'space'
     * gets Name in RFC1779 format
     * compares with expected value of name - "B "
     */
    public void testNameSpaces_RFC1779() throws Exception {
        String dn = "CN=A,CN=B\\ ";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=A, CN=\"B \"", s);

    }

    /**
     * Inits X500Principal with the string with special characters - B\'space'
     * gets Name in RFC2253 format
     * compares with expected value of name - B\'space'
     */
    public void testNameSpaces_RFC2253() throws Exception {
        String dn = "CN=A,CN=B\\ ";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC2253);
        assertEquals("CN=A,CN=B\\ ", s);

    }

    /**
     * Inits X500Principal with the string with special characters - B\'space'
     * gets Name in CANONICAL format
     * compares with expected value of name - B\
     */
    public void testNameSpaces_CANONICAL() throws Exception {
        String dn = "CN=A,CN=B\\ ";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=a,cn=b", s);

    }

    /**
     * Inits X500Principal with the string with special characters - "B'space''space''space'A"
     * gets Name in RFC1779 format
     * compares with expected value of name - "B   A"
     */
    public void testNameSpaces_RFC1779_01() throws Exception {
        String dn = "CN=\"B   A\"";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=\"B   A\"", s);

    }

    /**
     * Inits X500Principal with the string with special characters - "B'space''space''space'A"
     * gets Name in 2253 format
     * compares with expected value of name - B'space''space''space'A
     */
    public void testNameSpaces_RFC2253_01() throws Exception {
        String dn = "CN=\"B   A\"";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC2253);
        assertEquals("CN=B   A", s);

    }

    /**
     * Inits X500Principal with the string with special characters - "B'space''space''space'A"
     * gets Name in CANONICAL format
     * compares with expected value of name - b'space'a
     */
    public void testNameSpaces_CANONICAL_01() throws Exception {
        String dn = "CN=\"B   A\"";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=b a", s);

    }

    /**
     * Inits X500Principal with the string with special characters - \\'space''space'B
     * gets Name in RFC1779 format
     * compares with expected value of name - "  B"
     */
    public void testNameSpaces_RFC1779_02() throws Exception {
        String dn = "CN=\\  B";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=\"  B\"", s);

    }

    /**
     * Inits X500Principal with the string with special characters - \\'space''space'B
     * gets Name in RFC1779 format
     * compares with expected value of name - \'space''space'B
     */
    public void testNameSpaces_RFC2253_02() throws Exception {
        String dn = "CN=\\  B";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC2253);
        assertEquals("CN=\\ \\ B", s);

    }

    /**
     * Inits X500Principal with the string with special characters - \\'space''space'B
     * gets Name in CANONICAL format
     * compares with expected value of name - \'space''space'b
     */
    public void testNameSpaces_CANONICAL_02() throws Exception {
        String dn = "CN=\\  B";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=b", s);

    }

    /**
     * Inits X500Principal with the string with special characters - ""B
     * checks if the proper exception is thrown
     */
    public void testNameQu() throws Exception {
        String dn = "CN=\"\"B";
        try {
            new X500Principal(dn);
            fail("No IllegalArgumentException on improper string");
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Inits X500Principal with the string with special characters - "A\"B"
     * gets Name in RFC1779 format
     * compares with expected value of name - "A\"B"
     */
    public void testNameQu_RFC1779_2() throws Exception {
        String dn = "CN=\"A\\\"B\"";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=\"A\\\"B\"", s);
    }

    /**
     * Inits X500Principal with the string with special characters - "A\"B"
     * gets Name in RFC2253 format
     * compares with expected value of name - A\"B
     */
    public void testNameQu_RFC2253_2() throws Exception {
        String dn = "CN=\"A\\\"B\"";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC2253);
        assertEquals("CN=A\\\"B", s);
    }

    /**
     * Inits X500Principal with the string with special characters - "A\"B"
     * gets Name in CANONICAL format
     * compares with expected value of name - a\"b
     */
    public void testNameQu_CANONICAL_2() throws Exception {
        String dn = "CN=\"A\\\"B\"";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=a\\\"b", s);

    }

    /**
     * Inits X500Principal with the string with special characters - "A\""
     * gets Name in RFC1779 format
     * compares with expected value of name - "A\""
     */
    public void testNameQu_RFC1779_3() throws Exception {
        String dn = "CN=\"A\\\"\"";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=\"A\\\"\"", s);
    }

    /**
     * Inits X500Principal with the string with special characters - "A\""
     * gets Name in RFC2253 format
     * compares with expected value of name - A\"
     */
    public void testNameQu_RFC2253_3() throws Exception {
        String dn = "CN=\"A\\\"\"";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC2253);
        assertEquals("CN=A\\\"", s);
    }

    /**
     * Inits X500Principal with the string with special characters - "A\""
     * gets Name in CANONICAL format
     * compares with expected value of name - A\"
     */
    public void testNameQu_CANONICAL_3() throws Exception {
        String dn = "CN=\"A\\\"\"";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=a\\\"", s);

    }

    /**
     * Inits X500Principal with the string with special characters - "A\", C=B"
     * gets Name in RFC1779 format
     * compares with expected value of name - "A\", C=B"
     */
    public void testNameQu_4() throws Exception {
        String dn = "CN=\"A\\\", C=B\"";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=\"A\\\", C=B\"", s);

    }

    /**
     * Inits X500Principal with the string with special characters - CN="A\\", C=B
     * gets Name in RFC1779 format
     * compares with expected value of name - CN="A\\", C=B
     */
    public void testNameQu_5() throws Exception {
        String dn = "CN=\"A\\\\\", C=B";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=\"A\\\\\", C=B", s);

    }

    /**
     * Inits X500Principal with the string with special characters - CN=A\nB
     * gets Name in RFC1779 format
     * compares with expected value of name - CN="A\nB"
     */
    public void testNameCR_RFC1779() throws Exception {
        String dn = "CN=A\nB";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=\"A\nB\"", s);
    }


    public void testNamePlus_RFC1779() throws Exception {
        String dn = "CN=A\\+B";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC1779);
        assertEquals("CN=\"A+B\"", s);
    }

    /**
     * Inits X500Principal with the string with special characters - CN=A\nB
     * gets Name in RFC2253 format
     * compares with expected value of name - CN=A\nB
     */
    public void testNameCR_RFC2253() throws Exception {
        String dn = "CN=A\nB";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.RFC2253);
        assertEquals("CN=A\nB", s);
    }

    /**
     * Inits X500Principal with the string with special characters - CN=A\nB
     * gets Name in CANONICAL format
     * compares with expected value of name - cn=a\nb
     */
    public void testNameCR_CANONICAL() throws Exception {
        String dn = "CN=A\nB";
        X500Principal principal = new X500Principal(dn);
        String s = principal.getName(X500Principal.CANONICAL);
        assertEquals("cn=a\nb", s);
    }

    public static final String[] RFC2253_SPECIAL = new String[] { ",", "=",
            "+", "<", ">", "#", ";" };

    /*
    public void testValidDN() throws Exception {

        TestList list = new TestList();

        list.add("", "", "", "", new byte[] { 0x30, 0x00 }); // empty RDN sequence

        // sequence of RDN: RDN *("," RDN)
        list.add("CN=A,C=B", "CN=A,C=B", "CN=A, C=B", "cn=a,c=b");
        list.add("C=B,CN=A", "C=B,CN=A", "C=B, CN=A", "c=b,cn=a");
        list.add("CN=A,CN=A", "CN=A,CN=A", "CN=A, CN=A", "cn=a,cn=a"); // duplicate RDNs

        // sequence of RDN: RFC 1779 compatibility
        list.add("CN=A , C=B", "CN=A,C=B", "CN=A, C=B");
        list.add("CN=A  ,  C=B", "CN=A,C=B", "CN=A, C=B");
        list.add("CN=A;C=B", "CN=A,C=B", "CN=A, C=B");
        list.add("CN=A ; C=B", "CN=A,C=B", "CN=A, C=B");
        //FIXME list.add("CN=A\r,\rC=B", "CN=A,C=B"); // <CR> & comma => comma
        list.add("  CN=A,C=B  ", "CN=A,C=B", "CN=A, C=B"); // spaces at beg&end
        list.add("  CN=A,C=\"B\"  ", "CN=A,C=B", "CN=A, C=B"); // spaces at beg&end

        // set of ATAV: ATAV *("+" ATAV)
        list.add("CN=A+ST=CA", "CN=A+ST=CA", "CN=A + ST=CA", "cn=a+st=ca");
        list.add("CN=A+CN=A", "CN=A+CN=A", "CN=A + CN=A", "cn=a+cn=a"); // duplicate AT
        list
                .add("2.5.4.3=A+2.5.4.3=A", "CN=A+CN=A", "CN=A + CN=A",
                        "cn=a+cn=a"); // duplicate AT

        // set of ATAV: RFC 1779 compatibility
        list.add("CN=A + ST=CA", "CN=A+ST=CA", "CN=A + ST=CA");
        list.add("CN=A  +  ST=CA", "CN=A+ST=CA", "CN=A + ST=CA");
        //FIXME list.add("CN=A\r+\rST=CA", "CN=A+ST=CA"); // <CR> & '+' => '+'

        // ATAV = AttributeType "=" AttributeValue
        list.add("CN=A", "CN=A", "CN=A");
        list.add("cn=A", "CN=A", "CN=A"); // AT case insensitive
        list.add("cN=A", "CN=A", "CN=A"); // AT case insensitive
        list.add("cn=a", "CN=a", "CN=a"); // AT case insensitive

        // ATAV : RFC 1779 compatibility
        list.add("CN = A", "CN=A", "CN=A");
        list.add("CN  =  A", "CN=A", "CN=A");
        // FIXME list.add("CN\r=\rA", "CN=A"); // <CR> & '=' => '='

        // AttributeType = <name string> | <OID>
        // testing OID case :  OID => <name string>
        // tested all OIDs from RFC 2253 (2.3) and RFC 1779 (Table 1)

        // different variants of 2.5.4.3 (CN) OID
        list.add("OID.2.5.4.3=A", "CN=A", "CN=A");
        list.add("oid.2.5.4.3=A", "CN=A", "CN=A");
        list.add("2.5.4.3=A", "CN=A", "CN=A");
        list.add("02.5.4.3=A", "CN=A", "CN=A"); // first: 02 => 2
        list.add("2.5.4.0003=A", "CN=A", "CN=A"); // last: 0003 => 3

        // the rest of OIDs
        list.add("2.5.4.7=A", "L=A", "L=A", "l=a");
        list.add("2.5.4.8=A", "ST=A", "ST=A", "st=a");
        list.add("2.5.4.10=A", "O=A", "O=A", "o=a");
        list.add("2.5.4.11=A", "OU=A", "OU=A", "ou=a");
        list.add("2.5.4.6=A", "C=A", "C=A", "c=a");
        list.add("2.5.4.9=A", "STREET=A", "STREET=A", "street=a");
        list.add("0.9.2342.19200300.100.1.25=A", "DC=A",
                "OID.0.9.2342.19200300.100.1.25=A", "dc=#160141");
        list.add("0.9.2342.19200300.100.1.1=A", "UID=A",
                "OID.0.9.2342.19200300.100.1.1=A", "uid=a");

        // attribute types from RFC 2459 (see Appendix A)
        // keywords are from the API spec
        list.add("T=A", "2.5.4.12=#130141", "OID.2.5.4.12=A",
                "2.5.4.12=#130141");
        list.add("DNQ=A", "2.5.4.46=#130141", "OID.2.5.4.46=A",
                "2.5.4.46=#130141");
        list.add("DNQUALIFIER=A", "2.5.4.46=#130141", "OID.2.5.4.46=A",
                "2.5.4.46=#130141");
        list.add("SURNAME=A", "2.5.4.4=#130141", "OID.2.5.4.4=A",
                "2.5.4.4=#130141");
        list.add("GIVENNAME=A", "2.5.4.42=#130141", "OID.2.5.4.42=A",
                "2.5.4.42=#130141");
        list.add("INITIALS=A", "2.5.4.43=#130141", "OID.2.5.4.43=A",
                "2.5.4.43=#130141");
        list.add("GENERATION=A", "2.5.4.44=#130141", "OID.2.5.4.44=A",
                "2.5.4.44=#130141");
        list.add("EMAILADDRESS=A", "1.2.840.113549.1.9.1=#160141",
                "OID.1.2.840.113549.1.9.1=A", "1.2.840.113549.1.9.1=#160141",
                null, (byte) 0x05); //FIXME bug???
        list.add("SERIALNUMBER=A", "2.5.4.5=#130141", "OID.2.5.4.5=A",
                "2.5.4.5=#130141");

        // AttributeValue => BER encoding (if OID in dotted-decimal form)
        // see RFC 2253 (2.4)
        list.add("OID.2.5.4.12=A", "2.5.4.12=#130141", "OID.2.5.4.12=A");
        list.add("oid.2.5.4.12=A", "2.5.4.12=#130141", "OID.2.5.4.12=A");
        list.add("2.5.4.12=A", "2.5.4.12=#130141", "OID.2.5.4.12=A");
        list.add("1.1=A", "1.1=#130141", "OID.1.1=A");

        //
        // AttributeValue first alternative : *( stringchar / pair )
        // testing pair characters.
        //
        // Note: for RFC1779 quoted string is returned (unspecified)
        //
        list.add("CN=", "CN=", "CN="); // zero string chars
        list.add("CN= ", "CN=", "CN="); // zero string chars
        list.add("CN=A+ST=", "CN=A+ST=", "CN=A + ST="); // zero string chars
        list.add("CN=+ST=A", "CN=+ST=A", "CN= + ST=A"); // empty value for 1 RDN
        list.add("CN=A+ST= ", "CN=A+ST=", "CN=A + ST="); // empty value for 1 RDN
        list.add("CN=+ST=", "CN=+ST=", "CN= + ST="); // empty value for both RDNs
        list.add("CN=,ST=B", "CN=,ST=B", "CN=, ST=B"); // empty value for 1 RDN
        list.add("CN=,ST=", "CN=,ST=", "CN=, ST="); // empty value for both RDNs
        list.add("CN=;ST=B", "CN=,ST=B", "CN=, ST=B"); // empty value for 1 RDN
        list.add("CN=;ST=", "CN=,ST=", "CN=, ST="); // empty value for both RDNs
        for (String element : RFC2253_SPECIAL) {
            // \special
            list.add("CN=\\" + element,
                    "CN=\\" + element, "CN=\"" + element
                    + "\"");

            // A + \special + B
            list.add("CN=A\\" + element + "B", "CN=A\\"
                    + element + "B", "CN=\"A" + element
                    + "B\"");
        }

        // pair = \"
        list.add("CN=\\\"", "CN=\\\"", "CN=\"\\\"\"", null, (byte) 0x02);
        list.add("CN=\\\"A", "CN=\\\"A", "CN=\"\\\"A\"", null, (byte) 0x02);
        list.add("CN=\\\",C=\\\"", "CN=\\\",C=\\\"", "CN=\"\\\"\", C=\"\\\"\"",
                null, (byte) 0x02); // 2 RDN
        list.add("CN=A\\\"B", "CN=A\\\"B", "CN=\"A\\\"B\"", null, (byte) 0x02); // A\"B
        list.add("CN=A ST=B", "CN=A ST\\=B", "CN=\"A ST=B\""); // no RDN separator

        // pair = \space
        list.add("CN=\\ ", "CN=\\ ", "CN=\" \"", "cn=");

        // pair = \hexpair
        list.add("CN=\\41", "CN=A", "CN=A"); // 0x41=='A'
        list.add("CN=\\41\\2C", "CN=A\\,", "CN=\"A,\""); // 0x41=='A', 0x2C=','
        list.add("CN=\\41\\2c", "CN=A\\,", "CN=\"A,\""); // 0x41=='A', 0x2c=','
        list.add("CN=\\D0\\AF", "CN=" + ((char) 1071), "CN=" + ((char) 1071),
                new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03,
                        0x55, 0x04, 0x03,
                        // UTF8 String
                        0x0C, 0x02, (byte) 0xD0, (byte) 0xAF }); // 0xD0AF == the last letter(capital) of Russian alphabet
        list.add("CN=\\D0\\AFA\\41", "CN=" + ((char) 1071) + "AA", "CN="
                + ((char) 1071) + "AA", new byte[] { 0x30, 0x0F, 0x31, 0x0D,
                0x30, 0x0B, 0x06, 0x03, 0x55, 0x04, 0x03,
                // UTF8 String
                0x0C, 0x04, (byte) 0xD0, (byte) 0xAF, 0x41, 0x41 }); // 0xD0AF == the last letter(capital) of Russian alphabet
        // UTF-8(0xE090AF) is non-shortest form of UTF-8(0xD0AF)
        //FIXME list.add("CN=\\E0\\90\\AF", "CN=" + ((char) 1071), "CN="
        //        + ((char) 1071), new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30,
        //        0x09, 0x06, 0x03, 0x55, 0x04, 0x03,
        //        // UTF8 String
        //        0x0C, 0x02, (byte) 0xD0, (byte) 0xAF });
        // UTF-8(0xF08090AF) is non-shortest form of UTF-8(0xD0AF)
        //FIXME list.add("CN=\\F0\\80\\90\\AF", "CN=" + ((char) 1071), "CN="
        //        + ((char) 1071), new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30,
        //        0x09, 0x06, 0x03, 0x55, 0x04, 0x03,
        //        // UTF8 String
        //        0x0C, 0x02, (byte) 0xD0, (byte) 0xAF });
        //FIXME        list.add("CN=\\D0", "CN=" + ((char) 65533), "CN=" + ((char) 65533),
        //                new byte[] { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
        //                        0x55, 0x04, 0x03,
        //                        // UTF8 String
        //                        0x0C, 0x01, 0x3F }); // 0xD0 is not correct UTF8 char => '?'
        list.add("CN=\\41+ST=A", "CN=A+ST=A", "CN=A + ST=A"); // 0x41=='A'
        list.add("CN=\\41\\2C+ST=A", "CN=A\\,+ST=A", "CN=\"A,\" + ST=A"); // 0x41=='A', 0x2C=','
        list.add("CN=\\41\\2c+ST=A", "CN=A\\,+ST=A", "CN=\"A,\" + ST=A"); // 0x41=='A', 0x2c=','

        // stringchar '=' or not leading '#'
        //FIXME RFC 2253 grammar violation: '=' and '#' is a special char
        list.add("CN==", "CN=\\=", "CN=\"=\"");
        list.add("CN=A=", "CN=A\\=", "CN=\"A=\"");
        list.add("CN=A#", "CN=A\\#", "CN=\"A#\"");

        // not leading or trailing spaces
        list.add("CN=A B", "CN=A B", "CN=A B", "cn=a b");
        list.add("CN=A\\ B", "CN=A B", "CN=A B", "cn=a b");
        list.add("CN=A \\,B", "CN=A \\,B", "CN=\"A ,B\"", "cn=a \\,b");

        //not alphabet chars
        list.add("CN=$", "CN=$", "CN=$", new byte[] { 0x30, 0x0C, 0x31, 0x0A,
                0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03,
                //UTF-8 String: "$"
                0x0C, 0x01, 0x24 });
        list.add("CN=(", "CN=(", "CN=(", new byte[] { 0x30, 0x0C, 0x31, 0x0A,
                0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03,
                //PrintableString: "("
                0x13, 0x01, 0x28 });

        //
        //
        // AttributeValue second alternative : "#" hexstring
        //
        //
        list.add("CN=#130141", "CN=A", "CN=A", "cn=a"); // ASN1 Printable hex string = 'A'
        list.add("CN=#140141", "CN=A", "CN=A", "cn=a", new byte[] { 0x30,
                0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03,
                0x14, 0x01, 0x41 }); // ASN1 Teletex hex string = 'A'

        list.add("CN=#010100", "CN=#010100", "CN=#010100", "cn=#010100"); // ASN1 Boolean = FALSE
        list.add("CN=#0101fF", "CN=#0101ff", "CN=#0101FF", "cn=#0101ff"); // ASN1 Boolean = TRUE
        //FIXME list.add("CN=#3000", "CN=#3000", "CN=#3000"); // ASN1 Sequence
        //FIXME list.add("CN=#0500", "CN=A", "CN=A"); // ASN1 Null
        list.add("CN= #0101fF", "CN=#0101ff", "CN=#0101FF", // space at beginning
                new byte[] { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                        0x55, 0x04, 0x03, 0x01, 0x01, (byte) 0xFF } // ASN.1 Boolean = TRUE
        );
        list.add("CN= #0101fF+ST=A", "CN=#0101ff+ST=A", "CN=#0101FF + ST=A",
                "cn=#0101ff+st=a"); //space
        list.add("CN=  \n  #0101fF+ST=A", "CN=#0101ff+ST=A", "CN=#0101FF + ST=A",
                "cn=#0101ff+st=a"); // multiple spaces
        list.add("CN= #0101fF ", "CN=#0101ff", "CN=#0101FF", // space at the end
                new byte[] { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                        0x55, 0x04, 0x03, 0x01, 0x01, (byte) 0xFF } // ASN.1 Boolean = TRUE
                , (byte) 0x00);
        list.add("CN= #0101fF  \n  ", "CN=#0101ff", "CN=#0101FF", // multiple spaces at the end
                new byte[] { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                        0x55, 0x04, 0x03, 0x01, 0x01, (byte) 0xFF } // ASN.1 Boolean = TRUE
                , (byte) 0x00);

        //FIXME unspecified output for RFC1779
        //FIXME list.add("CN=#1C0141", "CN=A", "CN=A"); // ASN1 Universal hex string = 'A'
        //FIXME list.add("CN=#1E0141", "CN=A", "CN=A"); // ASN1 Bmp hex string = 'A'

        //
        // AttributeValue third alternative : " *( quotechar / pair ) "
        // quotechar = <any character except '\' or '"' >
        //
        // Note:
        // RFC2253: passed quoted AV string is unquoted, special chars are escaped
        // RFC1779: escaped quoted chars are unescaped
        //
        list.add("CN=\"\"", "CN=", "CN="); // empty quoted string
        list.add("CN=\"A\"", "CN=A", "CN=A"); // "A"
        for (String element : RFC2253_SPECIAL) {
            // "special" => \special
            list.add("CN=\"" + element + "\"", "CN=\\"
                    + element, "CN=\"" + element + "\"");

            // "A + special + B" => A + \special + B
            list.add("CN=\"A" + element + "B\"", "CN=A\\"
                    + element + "B", "CN=\"A" + element
                    + "B\"");
        }
        for (String element : RFC2253_SPECIAL) {
            // "\special" => \special
            list.add("CN=\"\\" + element + "\"", "CN=\\"
                    + element, "CN=\"" + element + "\"");

            // "A + \special + B" => A + \special + B
            list.add("CN=\"A\\" + element + "B\"", "CN=A\\"
                    + element + "B", "CN=\"A" + element
                    + "B\"");
        }
        list.add("CN=\"\\\"\"", "CN=\\\"", "CN=\"\\\"\"", null, (byte) 0x02); // "\""
        list.add("CN=\"A\\\"B\"", "CN=A\\\"B", "CN=\"A\\\"B\"", null,
                (byte) 0x02); // "A\"B"

        // pair = \hexpair (test cases are the same as for the first alternative)
        list.add("CN=\"\\41\"", "CN=A", "CN=A"); // 0x41=='A'
        list.add("CN=\"\\41\\2C\"", "CN=A\\,", "CN=\"A,\""); // 0x41=='A', 0x2C=','
        list.add("CN=\"\\41\\2c\"", "CN=A\\,", "CN=\"A,\""); // 0x41=='A', 0x2c=','
        list.add("CN=\"\\D0\\AF\"", "CN=" + ((char) 1071), "CN="
                + ((char) 1071), new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30,
                0x09, 0x06, 0x03, 0x55, 0x04, 0x03,
                // UTF8 String
                0x0C, 0x02, (byte) 0xD0, (byte) 0xAF }); // 0xD0AF == the last letter(capital) of Russian alphabet
        list.add("CN=\"\\D0\\AFA\\41\"", "CN=" + ((char) 1071) + "AA", "CN="
                + ((char) 1071) + "AA", new byte[] { 0x30, 0x0F, 0x31, 0x0D,
                0x30, 0x0B, 0x06, 0x03, 0x55, 0x04, 0x03,
                // UTF8 String
                0x0C, 0x04, (byte) 0xD0, (byte) 0xAF, 0x41, 0x41 }); // 0xD0AF == the last letter(capital) of Russian alphabet
        list.add("CN=\"\\E0\\90\\AF\"", "CN=\ufffd\ufffd\ufffd", "CN=\ufffd\ufffd\ufffd",
                new byte[] { 0x30, 0x14, 0x31, 0x12, 0x30, 0x10, 0x06, 0x03, 0x55, 0x04, 0x03,
                // UTF8 String
                0x0C, 0x09, (byte) 0xEF, (byte) 0xBF, (byte) 0xBD, (byte) 0xEF, (byte) 0xBF,
                (byte) 0xBD, (byte) 0xEF, (byte) 0xBF, (byte) 0xBD });
        // UTF8(0xE090AF) is not correct because it's a overlong form of UTF8(0xD0AF).
        list.add("CN=\"\\F0\\80\\90\\AF\"", "CN=\ufffd\ufffd\ufffd\ufffd",
                "CN=\ufffd\ufffd\ufffd\ufffd",
                new byte[] { 0x30, 0x17, 0x31, 0x15, 0x30, 0x13, 0x06, 0x03, 0x55, 0x04, 0x03,
                // UTF8 String
                0x0C, 0x0C, (byte) 0xEF, (byte) 0xBF, (byte) 0xBD, (byte) 0xEF, (byte) 0xBF,
                (byte) 0xBD, (byte) 0xEF, (byte) 0xBF, (byte) 0xBD, (byte) 0xEF, (byte) 0xBF,
                (byte) 0xBD });
        // UTF8(0xF08090AF) is not correct because it's a overlong form of UTF8(0xD0AF).

        list.add("CN=\"\\41\"+ST=A", "CN=A+ST=A", "CN=A + ST=A"); // 0x41=='A'
        list.add("CN=\"\\41\\2C\"+ST=A", "CN=A\\,+ST=A", "CN=\"A,\" + ST=A"); // 0x41=='A', 0x2C=','
        list.add("CN=\"\\41\\2c\"+ST=A", "CN=A\\,+ST=A", "CN=\"A,\" + ST=A"); // 0x41=='A', 0x2c=','

        // AttributeValue third alternative : RFC 1779 compatibility
        //FIXME list.add("CN=\"\r\"", "CN=\"\r\""); // "<CR>"
        //FIXME list.add("CN=\"\\\r\"", "CN=\"\\\r\""); // "\<CR>"

        // AttributeValue : RFC 1779 compatibility
        list.add("CN=  A  ", "CN=A", "CN=A", "cn=a"); // leading & trailing spaces
        list.add("CN=\\  A  ", "CN=\\ \\ A", "CN=\"  A\"", "cn=a", null,
                (byte) 0x01); // escaped leading space
        list.add("CN=  A \\ ", "CN=A\\ \\ ", "CN=\"A  \"", "cn=a", null,
                (byte) 0x01); // escaped trailing space

        list.add("CN=  \"A\"  ", "CN=A", "CN=A", "cn=a"); // leading & trailing spaces

        StringBuffer errorMsg = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {

            Object[] obj = list.get(i);

            String dn = (String) obj[0];
            String rfc2253 = (String) obj[1];
            String rfc1779 = (String) obj[2];
            String canonical = (String) obj[3];
            byte[] encoded = (byte[]) obj[4];
            byte mask = ((byte[]) obj[5])[0];

            try {
                X500Principal p = new X500Principal(dn);
                if (!rfc2253.equals(p.getName(X500Principal.RFC2253))) {
                    if (!testing || ((mask & 0x01) == 0)) {

                        errorMsg.append("\nRFC2253: " + i);
                        errorMsg.append(" \tparm: '" + dn + "'");
                        errorMsg.append("\t\texpected: '" + rfc2253 + "'");
                        errorMsg.append("\treturned: '"
                                + p.getName(X500Principal.RFC2253) + "'");
                    }
                }

                if (!rfc1779.equals(p.getName(X500Principal.RFC1779))) {
                    if (!testing || ((mask & 0x02) == 0)) {

                        errorMsg.append("\nRFC1779: " + i);
                        errorMsg.append(" \tparm: '" + dn + "'");
                        errorMsg.append("\t\texpected: '" + rfc1779 + "'");
                        errorMsg.append("\treturned: '"
                                + p.getName(X500Principal.RFC1779) + "'");
                    }
                }

                if (canonical != null) {
                    if (!canonical.equals(p.getName(X500Principal.CANONICAL))) {
                        if (!testing || ((mask & 0x04) == 0)) {

                            errorMsg.append("\nCANONICAL: " + i);
                            errorMsg.append("\tparm: '" + dn + "'");
                            errorMsg.append("\t\texpected: '" + canonical + "'");
                            errorMsg.append("\treturned: '"
                                    + p.getName(X500Principal.CANONICAL) + "'");
                        }
                    }
                }

                if (encoded != null) {
                    if (!Arrays.equals(encoded, p.getEncoded())) {
                        if (!testing || ((mask & 0x08) == 0)) {

                            errorMsg.append("\nUnexpected encoding for: " + i
                                    + ", dn= '" + dn + "'");

                            System.out.println("\nI " + i);
                            byte[] enc = p.getEncoded();
                            for (byte element : enc) {
                                System.out.print(", 0x"
                                        + Integer.toHexString(element));
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                errorMsg.append("\nIllegalArgumentException: " + i);
                errorMsg.append("\tparm: '" + dn + "'");
            } catch (Exception e) {
                errorMsg.append("\nException: " + i);
                errorMsg.append("\tparm: '" + dn + "'");
                errorMsg.append("\texcep: " + e.getClass().getName());
            }
        }

        if (errorMsg.length() != 0) {
            fail(errorMsg.toString());
        }

    }
     */

    public void testInvalidDN() {
        String[] illegalDN = new String[] {
                // RDN
                //FIXME " ", // space only
                "CN", // attribute type only
                "CN=A;", // RFC 1779: BNF allows this, but ...
                "CN=A,", // RFC 1779: BNF allows this, but ...
                ",CN=A", // no AttributeType for first RDN
                "CN=,A", // no AttributeType for second RDN
                "CN=A+", // no AttributeTypeAndValue for second RDN
                "CN=#130141 ST=B", // no RDN separator

                // AttributeType = <name string> | <OID>
                "AAA=A", // no such <name string>
                "1..1=A", // wrong OID
                ".1.1=A", // wrong OID
                "11=A", // wrong OID
                "1=A", // wrong OID
                "AID.1.1=A", // wrong OID
                "1.50=A", // wrong OID
                "5.1.0=A", // wrong OID
                "2.-5.4.3=A", // wrong OID
                "2.5.-4.3=A", // wrong OID
                "2.5.4-.3=A", // wrong OID
                //FIXME "2.5.4.-3=A", // wrong OID

                // AttributeValue first alternative : *( stringchar / pair )
                "CN=,", // stringchar = ','
                //FIXME "CN==",
                "CN=+", // stringchar = '+'
                //FIXME "CN=<", // stringchar = '<'
                //FIXME "CN=>", // stringchar = '>'
                "CN=#", // stringchar = '#'
                //FIXME "CN=Z#", // stringchar = '#'
                "CN=;", // stringchar = ';'
                "CN=\"", // stringchar = "
                //FIXME "CN=A\"B", // stringchar = "
                "CN=\\", // stringchar = \
                "CN=A\\", // stringchar = \
                "CN=A\\B", // stringchar = \
                "CN=\\z", // invalid pair = \z
                "CN=\\4", // invalid pair = \4
                "CN=\\4Z", // invalid pair = \4Z
                "CN=\\4\\2c", // invalid pair = \4\2c

                // AttributeValue second alternative : "#" hexstring
                "CN=#", // no hex string
                "CN=#2", // no hex pair
                "CN=#22", // hexpair is not BER encoding
                "CN=#0001", // invalid BER encoding (missed content)
                "CN=#000201", // invalid BER encoding (wrong length)
                "CN=#0002010101", // invalid BER encoding (wrong length)
                "CN=#00FF", // invalid BER encoding (wrong length)
                "CN=#ZZ", // not hex pair

                // FIXME boolean with indefinite length
                //"CN=#0100010000", // invalid BER encoding (wrong length)

                // AttributeValue third alternative : " *( quotechar / pair ) "
                "CN=\"A\" B", // TODO comment me
                "CN=\"A\\", // TODO comment me
                "CN=\"\\4\"", // invalid pair = \4
                "CN=\"\\4Z\"", // invalid pair = \4Z
                "CN=\"\\4\\2c\"", // invalid pair = \4\2c
        };

        StringBuffer errorMsg = new StringBuffer();
        for (String element : illegalDN) {

            try {
                new X500Principal(element);
                errorMsg.append("No IllegalArgumentException: '" + element
                        + "'\n");
            } catch (IllegalArgumentException e) {
            }
        }

        if (errorMsg.length() != 0) {
            fail(errorMsg.toString());
        }
    }

    public void testValidEncoding() {
        TestList list = new TestList();

        //
        // Empty
        //
        list.add(new byte[] { 0x30, 0x00 }, "", "", "");
        list.add(new byte[] { 0x30, 0x02, 0x31, 0x00 }, "", "", ""); //??? invalid size constraints

        //
        // Known OID + string with different tags(all string)
        //
        list.add(new byte[] { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // PrintableString
                0x13, 0x01, 0x5A }, "CN=Z", "CN=Z", "cn=z");
        list.add(new byte[] { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // TeletexString
                0x14, 0x01, 0x5A }, "CN=Z", "CN=Z", "cn=z");
        //FIXME:compatibility        list.add(new byte[] { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
        //                0x55, 0x04, 0x03,
        //                // UniversalString
        //                0x1C, 0x01, 0x5A }, "CN=Z", "CN=Z", "cn=z");
        list.add(new byte[] { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String
                0x0C, 0x01, 0x5A }, "CN=Z", "CN=Z", "cn=z");
        //FIXME:compatibility        list.add(new byte[] { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
        //                0x55, 0x04, 0x03,
        //                // BMPString
        //                0x1E, 0x01, 0x5A }, "CN=Z", "CN=Z", "cn=z");

        //
        // Unknown OID + string with different tags(all string)
        //
        list.add(new byte[] { 0x30, 0x0A, 0x31, 0x08, 0x30, 0x06, 0x06, 0x01,
                0x00,
                // PrintableString
                0x13, 0x01, 0x5A }, "0.0=#13015a", "OID.0.0=Z", "0.0=#13015a");
        list.add(new byte[] { 0x30, 0x0A, 0x31, 0x08, 0x30, 0x06, 0x06, 0x01,
                0x00,
                // TeletexString
                0x14, 0x01, 0x5A }, "0.0=#14015a", "OID.0.0=Z", "0.0=#14015a");
        //FIXME:compatibility        list.add(new byte[] { 0x30, 0x0A, 0x31, 0x08, 0x30, 0x06, 0x06, 0x01,
        //                0x00,
        //                // UniversalString
        //                0x1C, 0x01, 0x5A }, "0.0=#1c015a", "OID.0.0=Z", "cn=z");
        list.add(new byte[] { 0x30, 0x0A, 0x31, 0x08, 0x30, 0x06, 0x06, 0x01,
                0x00,
                // UTF8String
                0x0C, 0x01, 0x5A }, "0.0=#0c015a", "OID.0.0=Z", "0.0=#0c015a");
        //FIXME:compatibility        list.add(new byte[] { 0x30, 0x0A, 0x31, 0x08, 0x30, 0x06, 0x06, 0x01,
        //                0x00,
        //                // BMPString
        //                0x1E, 0x01, 0x5A }, "0.0=#1e015a", "OID.0.0=Z", "cn=z");

        //
        // Known OID + not a string value
        //
        list.add(new byte[] { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // Boolean
                0x01, 0x01, (byte) 0xFF }, "CN=#0101ff", "CN=#0101FF",
                "cn=#0101ff");
        list.add(new byte[] { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // Integer
                0x02, 0x01, 0x0F }, "CN=#02010f", "CN=#02010F", "cn=#02010f");
        list.add(new byte[] { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // BitString
                0x03, 0x01, 0x00 }, "CN=#030100", "CN=#030100", "cn=#030100");
        list.add(new byte[] { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // SEQUENCE
                0x30, 0x01, 0x0A }, "CN=#30010a", "CN=#30010A", "cn=#30010a");

        //
        // unknown OID + not a string value
        //
        list.add(new byte[] { 0x30, 0x0A, 0x31, 0x08, 0x30, 0x06, 0x06, 0x01,
                0x00,
                // Boolean
                0x01, 0x01, (byte) 0xFF }, "0.0=#0101ff", "OID.0.0=#0101FF",
                "0.0=#0101ff");
        list.add(new byte[] { 0x30, 0x0A, 0x31, 0x08, 0x30, 0x06, 0x06, 0x01,
                0x00,
                // Integer
                0x02, 0x01, 0x0F }, "0.0=#02010f", "OID.0.0=#02010F",
                "0.0=#02010f");
        list.add(new byte[] { 0x30, 0x0A, 0x31, 0x08, 0x30, 0x06, 0x06, 0x01,
                0x00,
                // BitString
                0x03, 0x01, 0x00 }, "0.0=#030100", "OID.0.0=#030100",
                "0.0=#030100");
        list.add(new byte[] { 0x30, 0x0A, 0x31, 0x08, 0x30, 0x06, 0x06, 0x01,
                0x00,
                // SEQUENCE
                0x30, 0x01, 0x0A }, "0.0=#30010a", "OID.0.0=#30010A",
                "0.0=#30010a");

        //
        // Known OID + UTF-8 string with chars to be escaped
        //

        // spaces
        list.add(new byte[] { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: a single space char
                0x0C, 0x01, 0x20 }, "CN=\\ ", "CN=\" \"", "cn=");
        list.add(new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: a space char at the beginning
                0x0C, 0x02, 0x20, 0x5A }, "CN=\\ Z", "CN=\" Z\"", "cn=z");
        list.add(new byte[] { 0x30, 0x0E, 0x31, 0x0C, 0x30, 0x0A, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: two space chars at the beginning
                0x0C, 0x03, 0x20, 0x20, 0x5A }, "CN=\\ \\ Z", "CN=\"  Z\"",
                "cn=z", (byte) 0x01);
        list.add(new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: a space char at the end
                0x0C, 0x02, 0x5A, 0x20 }, "CN=Z\\ ", "CN=\"Z \"", "cn=z");
        list.add(new byte[] { 0x30, 0x0E, 0x31, 0x0C, 0x30, 0x0A, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: two space chars at the end
                0x0C, 0x03, 0x5A, 0x20, 0x20 }, "CN=Z\\ \\ ", "CN=\"Z  \"",
                "cn=z", (byte) 0x01);

        // special chars
        list.add(new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: a '#' char at the beginning
                0x0C, 0x02, 0x23, 0x5A }, "CN=\\#Z", "CN=\"#Z\"", "cn=\\#z");
        list.add(new byte[] { 0x30, 0x0E, 0x31, 0x0C, 0x30, 0x0A, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: two '#' chars
                0x0C, 0x03, 0x23, 0x5A, 0x23 }, "CN=\\#Z\\#", "CN=\"#Z#\"",
                "cn=\\#z#");
        list.add(new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: ','
                0x0C, 0x02, 0x5A, 0x2C }, "CN=Z\\,", "CN=\"Z,\"", "cn=z\\,");
        list.add(new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: '+'
                0x0C, 0x02, 0x5A, 0x2B }, "CN=Z\\+", "CN=\"Z+\"", "cn=z\\+");
        list.add(new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: '"'
                0x0C, 0x02, 0x5A, 0x22 }, "CN=Z\\\"", "CN=\"Z\\\"\"",
                "cn=z\\\"", (byte) 0x02);
        list.add(new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: '\'
                0x0C, 0x02, 0x5A, 0x5C }, "CN=Z\\\\", "CN=\"Z\\\\\"",
                "cn=z\\\\", (byte) 0x02);
        list.add(new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: '<'
                0x0C, 0x02, 0x5A, 0x3C }, "CN=Z\\<", "CN=\"Z<\"", "cn=z\\<");
        list.add(new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: '>'
                0x0C, 0x02, 0x5A, 0x3E }, "CN=Z\\>", "CN=\"Z>\"", "cn=z\\>");
        list.add(new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: ';'
                0x0C, 0x02, 0x5A, 0x3B }, "CN=Z\\;", "CN=\"Z;\"", "cn=z\\;");
        list.add(new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: '='
                0x0C, 0x02, 0x5A, 0x3D }, "CN=Z\\=", "CN=\"Z=\"", "cn=z=");
        //FIXME        list.add(new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03,
        //                0x55, 0x04, 0x03,
        //                // UTF8String: ';'
        //                0x0C, 0x02, 0x5A, 0x0D }, "CN=Z\\\r", "CN=\"Z\r\"", "cn=z");

        // combinations
        list.add(new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: '\ '
                0x0C, 0x02, 0x5C, 0x20 }, "CN=\\\\\\ ", "CN=\"\\\\ \"",
                "cn=\\\\", (byte) 0x02);
        list.add(new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: ' \'
                0x0C, 0x02, 0x20, 0x5C }, "CN=\\ \\\\", "CN=\" \\\\\"",
                "cn=\\\\", (byte) 0x02);
        list.add(new byte[] { 0x30, 0x0E, 0x31, 0x0C, 0x30, 0x0A, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: ' \ '
                0x0C, 0x03, 0x20, 0x5C, 0x20 }, "CN=\\ \\\\\\ ",
                "CN=\" \\\\ \"", "cn=\\\\", (byte) 0x02);
        list.add(new byte[] { 0x30, 0x0E, 0x31, 0x0C, 0x30, 0x0A, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: 'Z Z' no escaping
                0x0C, 0x03, 0x5A, 0x20, 0x5A }, "CN=Z Z", "CN=Z Z", "cn=z z");
        list.add(new byte[] { 0x30, 0x0F, 0x31, 0x0D, 0x30, 0x0B, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: 'Z  Z' no escaping
                0x0C, 0x04, 0x5A, 0x20, 0x20, 0x5A }, "CN=Z  Z", "CN=\"Z  Z\"",
                "cn=z z", (byte) 0x02);
        list.add(new byte[] { 0x30, 0x0F, 0x31, 0x0D, 0x30, 0x0B, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: ' #Z ' no escaping
                0x0C, 0x04, 0x20, 0x23, 0x5A, 0x20 }, "CN=\\ \\#Z\\ ",
                "CN=\" #Z \"", "cn=#z");

        //
        // Special cases
        //
        //        list.add(new byte[] {
        //        // Name
        //                0x30, 0x13, 0x31, 0x11, 0x30, 0x0F,
        //                // OID
        //                0x06, 0x0A, 0x09, (byte) 0x92, 0x26, (byte) 0x89, (byte) 0x93,
        //                (byte) 0xF2, 0x2C, 0x64, 0x01, 0x01,
        //                // ANY
        //                0x13, 0x01, 0x41 }, "UID=A", "OID.0.9.2342.19200300.100.1.1=A",
        //                "uid=a");
        //
        //        list.add(new byte[] { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
        //                0x55, 0x04, 0x03, 0x1E, 0x01, 0x5A }, "CN=Z", "CN=Z",
        //                "cn=#1e015a");

        //
        // Multi-valued DN
        //
        list.add(new byte[] { 0x30, 0x14, 0x31, 0x12,
                // 1
                0x30, 0x08, 0x06, 0x03, 0x55, 0x04, 0x03,
                // UTF8String: 'Z'
                0x0C, 0x01, 0x5A,
                //2
                0x30, 0x06, 0x06, 0x01, 0x01,
                // UTF8String: 'A'
                0x0C, 0x01, 0x41 }, "CN=Z+0.1=#0c0141", "CN=Z + OID.0.1=A",
                "cn=z+0.1=#0c0141");

        //
        //
        //
        list.add(new byte[] { 0x30, 0x0D, 0x31, 0x0B, 0x30, 0x09, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // UTF8String: the last letter(capital) of Russian alphabet
                0x0C, 0x02, (byte) 0xD0, (byte) 0xAF }, "CN=" + ((char) 1071),
                "CN=" + ((char) 1071), "cn=" + ((char) 1103));
        // FIXME list.add(new byte[] { 0x30, 0x0E, 0x31, 0x0C, 0x30, 0x0A, 0x06, 0x03,
        //        0x55, 0x04, 0x03,
        //        // UTF8String: the last letter(capital) of Russian alphabet
        //        0x0C, 0x03, (byte) 0xE0, (byte) 0x90, (byte) 0xAF }, "CN="
        //        + ((char) 1071), "CN=" + ((char) 1071), "cn=" + ((char) 1103));
        // FIXME list.add(
        //        new byte[] { 0x30, 0x0F, 0x31, 0x0D, 0x30, 0x0B, 0x06, 0x03,
        //                0x55, 0x04, 0x03,
        //                // UTF8String: the last letter(capital) of Russian alphabet
        //                0x0C, 0x04, (byte) 0xF0, (byte) 0x80, (byte) 0x90,
        //                (byte) 0xAF }, "CN=" + ((char) 1071), "CN="
        //                + ((char) 1071), "cn=" + ((char) 1103));
        list.add(new byte[] { 0x30, 0x0C, 0x31, 0x0A, 0x30, 0x08, 0x06, 0x03,
                0x55, 0x04, 0x03,
                // PrintableString: char '$' is not in table 8 (X.680)
                0x13, 0x01, 0x24 }, "CN=$", "CN=$", "cn=$");

        StringBuffer errorMsg = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {

            Object[] values = list.get(i);
            byte[] encoded = (byte[]) values[0];
            String rfc2253 = (String) values[1];
            String rfc1179 = (String) values[2];
            String canonical = (String) values[3];
            byte mask = ((byte[]) values[4])[0];

            X500Principal p;
            try {
                p = new X500Principal(encoded);

                if (!rfc2253.equals(p.getName(X500Principal.RFC2253))) {
                    if (!testing || ((mask & 0x01) == 0)) {
                        errorMsg.append("RFC2253: " + i);
                        errorMsg.append("\t\texpected: '" + rfc2253 + "'");
                        errorMsg.append("\treturned: '"
                                + p.getName(X500Principal.RFC2253) + "'\n");
                    }
                }

                if (!rfc1179.equals(p.getName(X500Principal.RFC1779))) {
                    if (!testing || ((mask & 0x02) == 0)) {
                        errorMsg.append("RFC1779: " + i);
                        errorMsg.append("\t\texpected: '" + rfc1179 + "'");
                        errorMsg.append("\treturned: '"
                                + p.getName(X500Principal.RFC1779) + "'\n");
                    }
                }

                if (!canonical.equals(p.getName(X500Principal.CANONICAL))) {
                    if (!testing || ((mask & 0x04) == 0)) {
                        errorMsg.append("CANONICAL: " + i);
                        errorMsg.append("\t\texpected: " + canonical + "'");
                        errorMsg.append("\treturned: '"
                                + p.getName(X500Principal.CANONICAL) + "'\n");
                    }
                }

            } catch (IllegalArgumentException e) {
                errorMsg.append("\nIllegalArgumentException: " + i + ", for "
                        + rfc2253);
                continue;
            } catch (Exception e) {
                errorMsg.append("Exception: " + i + ", for " + rfc2253);
                errorMsg.append("\texcep: " + e.getClass().getName() + "\n");
                continue;
            }

        }

        if (errorMsg.length() != 0) {
            fail(errorMsg.toString());
        }
    }

    @SuppressWarnings("serial")
    public static class TestList extends ArrayList<Object[]> {
        //
        // TODO comment me
        //
        public void add(String param, String rfc2253, String rfc1779) {
            add(param, rfc2253, rfc1779, (byte[]) null);
        }

        public void add(String param, String rfc2253, String rfc1779,
                String canonical) {
            add(param, rfc2253, rfc1779, canonical, null);
        }

        public void add(String param, String rfc2253, String rfc1779,
                byte[] encoded) {
            add(new Object[] { param, rfc2253, rfc1779, null, encoded,
                    emptyMask });
        }

        public void add(String param, String rfc2253, String rfc1779,
                byte[] encoded, byte mask) {
            add(new Object[] { param, rfc2253, rfc1779, null, encoded,
                    new byte[] { mask } });
        }

        public void add(String param, String rfc2253, String rfc1779,
                String canonical, byte[] encoded) {
            add(new Object[] { param, rfc2253, rfc1779, canonical, encoded,
                    emptyMask });
        }

        public void add(String param, String rfc2253, String rfc1779,
                String canonical, byte[] encoded, byte mask) {
            add(new Object[] { param, rfc2253, rfc1779, canonical, encoded,
                    new byte[] { mask } });
        }

        //
        // TODO comment me
        //

        private static final byte[] emptyMask = new byte[] { 0x00 };

        public void add(byte[] encoding, String rfc2253, String rfc1779,
                String canonical) {
            add(new Object[] { encoding, rfc2253, rfc1779, canonical, emptyMask });
        }

        public void add(byte[] encoding, String rfc2253, String rfc1779,
                String canonical, byte mask) {
            add(new Object[] { encoding, rfc2253, rfc1779, canonical,
                    new byte[] { mask } });
        }
    }


    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(getSerializationData());
    }

    public void testSerializationGolden() throws Exception {
        SerializationTest.verifyGolden(this, getSerializationData());
    }

    private Object[] getSerializationData() {
        return new Object[] { new X500Principal("CN=A"),
                new X500Principal("CN=A, C=B"),
                new X500Principal("CN=A, CN=B + C=C") };
    }
}
