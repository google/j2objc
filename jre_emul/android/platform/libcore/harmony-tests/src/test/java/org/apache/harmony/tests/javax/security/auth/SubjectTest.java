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

package org.apache.harmony.tests.javax.security.auth;

import junit.framework.TestCase;
import javax.security.auth.Subject;
import javax.security.auth.x500.X500Principal;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

import org.apache.harmony.testframework.serialization.SerializationTest;

/**
 * Tests for <code>Subject</code> class constructors and methods.
 *
 */
public class SubjectTest extends TestCase {

    /**
     * javax.security.auth.Subject#Subject()
     */
    public void test_Constructor_01() {
        try {
            Subject s = new Subject();
            assertNotNull("Null object returned", s);
            assertTrue("Set of principal is not empty", s.getPrincipals().isEmpty());
            assertTrue("Set of private credentials is not empty", s.getPrivateCredentials().isEmpty());
            assertTrue("Set of public credentials is not empty", s.getPublicCredentials().isEmpty());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    public void test_Constructor_failsWithNullArguments() {
        try {
            new Subject(false /* readOnly */,
                    null /* principals */,
                    new HashSet<Object>() /* pubCredentials */,
                    new HashSet<Object>() /* privCredentials */);
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            new Subject(false , new HashSet<Principal>(), null, new HashSet<Object>());
            fail();
        } catch (NullPointerException expected) {
        }

        try {
            new Subject(false , new HashSet<Principal>(), new HashSet<Object>(), null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    /**
     * javax.security.auth.Subject#doAs(Subject subject, PrivilegedAction action)
     */
    public void test_doAs_01() {
        Subject subj = new Subject();
        PrivilegedAction<Object> pa = new myPrivilegedAction();
        PrivilegedAction<Object> paNull = null;

        try {
            Object obj = Subject.doAs(null, pa);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            Object obj = Subject.doAs(subj, pa);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            Object obj = Subject.doAs(subj, paNull);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        }
    }

    /**
     * javax.security.auth.Subject#doAs(Subject subject, PrivilegedExceptionAction action)
     */
    public void test_doAs_02() {
        Subject subj = new Subject();
        PrivilegedExceptionAction<Object> pea = new myPrivilegedExceptionAction();
        PrivilegedExceptionAction<Object> peaNull = null;

        try {
            Object obj = Subject.doAs(null, pea);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            Object obj = Subject.doAs(subj, pea);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            Object obj = Subject.doAs(subj, peaNull);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of NullPointerException");
        }

        try {
            Subject.doAs(subj, new PrivilegedExceptionAction<Object>(){
                public Object run() throws PrivilegedActionException {
                    throw new PrivilegedActionException(null);
                }
            });
            fail("PrivilegedActionException wasn't thrown");
        } catch (PrivilegedActionException e) {
        }
    }

    /**
     * javax.security.auth.Subject#doAsPrivileged(Subject subject,
     *                                                   PrivilegedAction action,
     *                                                   AccessControlContext acc)
     */
    public void test_doAsPrivileged_01() {
        Subject subj = new Subject();
        PrivilegedAction<Object> pa = new myPrivilegedAction();
        PrivilegedAction<Object> paNull = null;
        AccessControlContext acc = AccessController.getContext();

        try {
            Object obj = Subject.doAsPrivileged(null, pa, acc);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            Object obj = Subject.doAsPrivileged(subj, pa, acc);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            Object obj = Subject.doAsPrivileged(subj, paNull, acc);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        }
    }

    /**
     * javax.security.auth.Subject#doAsPrivileged(Subject subject,
     *                                                   PrivilegedExceptionAction action,
     *                                                   AccessControlContext acc)
     */
    public void test_doAsPrivileged_02() {
        Subject subj = new Subject();
        PrivilegedExceptionAction<Object> pea = new myPrivilegedExceptionAction();
        PrivilegedExceptionAction<Object> peaNull = null;
        AccessControlContext acc = AccessController.getContext();

        try {
            Object obj = Subject.doAsPrivileged(null, pea, acc);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            Object obj = Subject.doAsPrivileged(subj, pea, acc);
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }

        try {
            Object obj = Subject.doAsPrivileged(subj, peaNull, acc);
            fail("NullPointerException wasn't thrown");
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail(e + " was thrown instead of NullPointerException");
        }

        try {
            Subject.doAsPrivileged(subj, new PrivilegedExceptionAction<Object>(){
                public Object run() throws PrivilegedActionException {
                    throw new PrivilegedActionException(null);
                }
            }, acc);
            fail("PrivilegedActionException wasn't thrown");
        } catch (PrivilegedActionException e) {
        }
    }

    /**
     * javax.security.auth.Subject#getSubject(AccessControlContext acc)
     */
    public void test_getSubject() {
        Subject subj = new Subject();
        AccessControlContext acc = new AccessControlContext(new ProtectionDomain[0]);

        try {
            assertNull(Subject.getSubject(acc));
        } catch (Exception e) {
            fail("Unexpected exception " + e);
        }
    }

    /**
     * javax.security.auth.Subject#toString()
     */
    public void test_toString() {
        Subject subj = new Subject();

        try {
            assertNotNull("Null returned", subj.toString());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    /**
     * javax.security.auth.Subject#hashCode()
     */
    public void test_hashCode() {
        Subject subj = new Subject();

        try {
            assertNotNull("Null returned", subj.hashCode());
        } catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    }

    public void testSerializationSelf() throws Exception {
        SerializationTest.verifySelf(getSerializationData());
    }

    public void testSerializationGolden() throws Exception {
        SerializationTest.verifyGolden(this, getSerializationData());
    }

    public void testSerialization_nullPrincipalsAllowed() throws Exception {
        Set<Principal> principalsSet = new HashSet<>();
        principalsSet.add(new X500Principal("CN=SomePrincipal"));
        principalsSet.add(null);
        principalsSet.add(new X500Principal("CN=SomeOtherPrincipal"));
        Subject subject = new Subject(
                false /* readOnly */, principalsSet, new HashSet<Object>(), new HashSet<Object>());
        SerializationTest.verifySelf(subject);
    }

    public void testSecureTest_removeAllNull_throwsException() throws Exception {
        Subject subject = new Subject(
                false, new HashSet<Principal>(), new HashSet<Object>(), new HashSet<Object>());
        try {
            subject.getPrincipals().removeAll(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    public void testSecureTest_retainAllNull_throwsException() throws Exception {
        Subject subject = new Subject(
                false, new HashSet<Principal>(), new HashSet<Object>(), new HashSet<Object>());
        try {
            subject.getPrincipals().retainAll(null);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    private Object[] getSerializationData() {
        Subject subject = new Subject();
        return new Object[] { subject, subject.getPrincipals(),
                subject.getPrivateCredentials(), subject.getPublicCredentials() };
    }
}


class myPrivilegedAction implements PrivilegedAction <Object> {
    myPrivilegedAction(){}
    public Object run() {
        return new Object();
    }
}

class myPrivilegedExceptionAction implements PrivilegedExceptionAction <Object> {
    myPrivilegedExceptionAction(){}
    public Object run() {
        return new Object();
    }
}
