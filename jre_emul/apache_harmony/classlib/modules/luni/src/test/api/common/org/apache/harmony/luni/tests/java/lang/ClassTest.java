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

package org.apache.harmony.luni.tests.java.lang;

import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ClassTest extends junit.framework.TestCase {

    static class StaticMember$Class {
        class Member2$A {
        }
    }

    class Member$Class {
        class Member3$B {
        }
    }

    public static class TestClass {
        @SuppressWarnings("unused")
        private int privField = 1;

        public int pubField = 2;

        private Object cValue = null;

        public Object ack = new Object();

        @SuppressWarnings("unused")
        private int privMethod() {
            return 1;
        }

        public int pubMethod() {
            return 2;
        }

        public Object cValue() {
            return cValue;
        }

        public TestClass() {
        }

        @SuppressWarnings("unused")
        private TestClass(Object o) {
        }
    }

    public static class SubTestClass extends TestClass {
    }

    /**
     * @tests java.lang.Class#forName(java.lang.String)
     */
    /* TODO(user): enable if Class.forName is mapped.
    public void test_forNameLjava_lang_String() throws Exception {
        assertSame("Class for name failed for java.lang.Object",
                   Object.class, Class.forName("java.lang.Object"));
        assertSame("Class for name failed for [[Ljava.lang.Object;",
                   Object[][].class, Class.forName("[[Ljava.lang.Object;"));

        assertSame("Class for name failed for [I",
                   int[].class, Class.forName("[I"));

        try {
            Class.forName("int");
            fail();
        } catch (ClassNotFoundException e) {
        }

        try {
            Class.forName("byte");
            fail();
        } catch (ClassNotFoundException e) {
        }
        try {
            Class.forName("char");
            fail();
        } catch (ClassNotFoundException e) {
        }

        try {
            Class.forName("void");
            fail();
        } catch (ClassNotFoundException e) {
        }

        try {
            Class.forName("short");
            fail();
        } catch (ClassNotFoundException e) {
        }
        try {
            Class.forName("long");
            fail();
        } catch (ClassNotFoundException e) {
        }

        try {
            Class.forName("boolean");
            fail();
        } catch (ClassNotFoundException e) {
        }
        try {
            Class.forName("float");
            fail();
        } catch (ClassNotFoundException e) {
        }
        try {
            Class.forName("double");
            fail();
        } catch (ClassNotFoundException e) {
        }

        //regression test for JIRA 2162
        try {
            Class.forName("%");
            fail("should throw ClassNotFoundException.");
        } catch (ClassNotFoundException e) {
        }

        //Regression Test for HARMONY-3332
        String securityProviderClassName;
        int count = 1;
        while ((securityProviderClassName = Security
                .getProperty("security.provider." + count++)) != null) {
            Class.forName(securityProviderClassName);
        }
    }
    */

    /**
     * @tests java.lang.Class#getClasses()
     */
    /* TODO(user): enable if Class.getClasses is mapped.
    public void test_getClasses() {
        assertEquals("Incorrect class array returned",
                     2, ClassTest.class.getClasses().length);
    }
    */

    /**
     * @tests java.lang.Class#getClasses()
     */
    /* TODO(user): enable if Class.getClasses is mapped.
    public void test_getClasses_subtest0() {
        final Permission privCheckPermission = new BasicPermission("Privilege check") {
            private static final long serialVersionUID = 1L;
        };

        class MyCombiner implements DomainCombiner {
            boolean combine;

            public ProtectionDomain[] combine(ProtectionDomain[] executionDomains,
                    ProtectionDomain[] parentDomains) {
                combine = true;
                return new ProtectionDomain[0];
            }

            private boolean recurring = false;

            public boolean isPriviledged() {
                if (recurring) {
                    return true;
                }
                try {
                    recurring = true;
                    combine = false;
                    try {
                        AccessController.checkPermission(privCheckPermission);
                    } catch (SecurityException e) {}
                    return !combine;
                } finally {
                    recurring = false;
                }
            }
        }

        final MyCombiner combiner = new MyCombiner();
        class SecurityManagerCheck extends SecurityManager {
            String reason;

            Class<?> checkClass;

            int checkType;

            int checkPermission;

            int checkMemberAccess;

            int checkPackageAccess;

            public void setExpected(String reason, Class<?> cls, int type) {
                this.reason = reason;
                checkClass = cls;
                checkType = type;
                checkPermission = 0;
                checkMemberAccess = 0;
                checkPackageAccess = 0;
            }

            @Override
            public void checkPermission(Permission perm) {
                if (combiner.isPriviledged())
                    return;
                checkPermission++;
            }

            @Override
            public void checkMemberAccess(Class<?> cls, int type) {
                if (combiner.isPriviledged())
                    return;
                checkMemberAccess++;
                assertEquals(reason + " unexpected class", checkClass, cls);
                assertEquals(reason + "unexpected type", checkType, type);
            }

            @Override
            public void checkPackageAccess(String packageName) {
                if (combiner.isPriviledged())
                    return;
                checkPackageAccess++;
                String name = checkClass.getName();
                int index = name.lastIndexOf('.');
                String checkPackage = name.substring(0, index);
                assertEquals(reason + " unexpected package",
                             checkPackage,  packageName);
            }

            public void assertProperCalls() {
                assertEquals(reason + " unexpected checkPermission count",
                             0, checkPermission);
                assertEquals(reason + " unexpected checkMemberAccess count",
                             1, checkMemberAccess);
                assertEquals(reason + " unexpected checkPackageAccess count",
                             1, checkPackageAccess);
            }
        }

        AccessControlContext acc = new AccessControlContext(new ProtectionDomain[0]);
        AccessControlContext acc2 = new AccessControlContext(acc, combiner);

        PrivilegedAction<?> action = new PrivilegedAction<Object>() {
            public Object run() {
                File resources = Support_Resources.createTempFolder();
                try {
                    Support_Resources.copyFile(resources, null, "hyts_security.jar");
                    File file = new File(resources.toString() + "/hyts_security.jar");
                    URL url = new URL("file:" + file.getPath());
                    ClassLoader loader = new URLClassLoader(new URL[] { url }, null);
                    Class<?> cls = Class.forName("packB.SecurityTestSub", false, loader);
                    SecurityManagerCheck sm = new SecurityManagerCheck();
                    System.setSecurityManager(sm);
                    try {
                        sm.setExpected("getClasses", cls, Member.PUBLIC);
                        cls.getClasses();
                        sm.assertProperCalls();

                        sm.setExpected("getDeclaredClasses", cls, Member.DECLARED);
                        cls.getDeclaredClasses();
                        sm.assertProperCalls();

                        sm.setExpected("getConstructor", cls, Member.PUBLIC);
                        cls.getConstructor(new Class[0]);
                        sm.assertProperCalls();

                        sm.setExpected("getConstructors", cls, Member.PUBLIC);
                        cls.getConstructors();
                        sm.assertProperCalls();

                        sm.setExpected("getDeclaredConstructor", cls, Member.DECLARED);
                        cls.getDeclaredConstructor(new Class[0]);
                        sm.assertProperCalls();

                        sm.setExpected("getDeclaredConstructors", cls, Member.DECLARED);
                        cls.getDeclaredConstructors();
                        sm.assertProperCalls();

                        sm.setExpected("getField", cls, Member.PUBLIC);
                        cls.getField("publicField");
                        sm.assertProperCalls();

                        sm.setExpected("getFields", cls, Member.PUBLIC);
                        cls.getFields();
                        sm.assertProperCalls();

                        sm.setExpected("getDeclaredField", cls, Member.DECLARED);
                        cls.getDeclaredField("publicField");
                        sm.assertProperCalls();

                        sm.setExpected("getDeclaredFields", cls, Member.DECLARED);
                        cls.getDeclaredFields();
                        sm.assertProperCalls();

                        sm.setExpected("getDeclaredMethod", cls, Member.DECLARED);
                        cls.getDeclaredMethod("publicMethod", new Class[0]);
                        sm.assertProperCalls();

                        sm.setExpected("getDeclaredMethods", cls, Member.DECLARED);
                        cls.getDeclaredMethods();
                        sm.assertProperCalls();

                        sm.setExpected("getMethod", cls, Member.PUBLIC);
                        cls.getMethod("publicMethod", new Class[0]);
                        sm.assertProperCalls();

                        sm.setExpected("getMethods", cls, Member.PUBLIC);
                        cls.getMethods();
                        sm.assertProperCalls();

                        sm.setExpected("newInstance", cls, Member.PUBLIC);
                        cls.newInstance();
                        sm.assertProperCalls();
                    } finally {
                        System.setSecurityManager(null);
                    }
                } catch (Exception e) {
                    if (e instanceof RuntimeException)
                        throw (RuntimeException) e;
                    fail("unexpected exception: " + e);
                }
                return null;
            }
        };
        AccessController.doPrivileged(action, acc2);
    }
    */

    /**
     * @tests java.lang.Class#getComponentType()
     */
    /* TODO(user): enable if Class.getComponentType is mapped.
    public void test_getComponentType() {
        assertSame("int array does not have int component type", int.class, int[].class
                .getComponentType());
        assertSame("Object array does not have Object component type", Object.class,
                Object[].class.getComponentType());
        assertNull("Object has non-null component type", Object.class.getComponentType());
    }
    */

    /**
     * @tests java.lang.Class#getConstructor(java.lang.Class[])
     */
    public void test_getConstructor$Ljava_lang_Class()
        throws NoSuchMethodException {
        TestClass.class.getConstructor(new Class[0]);
        try {
            TestClass.class.getConstructor(Object.class);
            fail("Found private constructor");
        } catch (NoSuchMethodException e) {
            // Correct - constructor with obj is private
        }
    }

    /**
     * @tests java.lang.Class#getConstructors()
     */
    /* TODO(user): enable if Class.getConstructors() is mapped.
    public void test_getConstructors() throws Exception {
        Constructor[] c = TestClass.class.getConstructors();
        assertEquals("Incorrect number of constructors returned", 1, c.length);
    }
    */

    /**
     * @tests java.lang.Class#getDeclaredClasses()
     */
    /* TODO(user): enable if Class.getDeclaredClasses is mapped.
    public void test_getDeclaredClasses() {
        assertEquals("Incorrect class array returned", 2, ClassTest.class.getClasses().length);
    }
    */

    /**
     * @tests java.lang.Class#getDeclaredConstructor(java.lang.Class[])
     */
    /* TODO(user): enable if Class.getDeclaredConstructor is mapped.
    public void test_getDeclaredConstructor$Ljava_lang_Class() throws Exception {
        Constructor<TestClass> c = TestClass.class.getDeclaredConstructor(new Class[0]);
        assertNull("Incorrect constructor returned", c.newInstance().cValue());
        c = TestClass.class.getDeclaredConstructor(Object.class);
    }
    */

    /**
     * @tests java.lang.Class#getDeclaredConstructors()
     */
    /* TODO(user): enable if Class.getDeclaredConstructors is mapped.
    public void test_getDeclaredConstructors() throws Exception {
        Constructor[] c = TestClass.class.getDeclaredConstructors();
        assertEquals("Incorrect number of constructors returned", 2, c.length);
    }
    */

    /**
     * @tests java.lang.Class#getDeclaredField(java.lang.String)
     */
    /* TODO(user): enable if Class.getDeclaredField is mapped.
    public void test_getDeclaredFieldLjava_lang_String() throws Exception {
        Field f = TestClass.class.getDeclaredField("pubField");
        assertEquals("Returned incorrect field", 2, f.getInt(new TestClass()));
    }
    */

    /**
     * @tests java.lang.Class#getDeclaredFields()
     */
    /* TODO(user): enable if Class.getDeclaredFields is mapped.
    public void test_getDeclaredFields() throws Exception {
        Field[] f = TestClass.class.getDeclaredFields();
        assertEquals("Returned incorrect number of fields", 4, f.length);
        f = SubTestClass.class.getDeclaredFields();
        // Declared fields do not include inherited
        assertEquals("Returned incorrect number of fields", 0, f.length);
    }
    */

    /**
     * @tests java.lang.Class#getDeclaredMethod(java.lang.String,
     *        java.lang.Class[])
     */
    /* TODO(user): enable if Class.getDeclaredMethod is mapped.
    public void test_getDeclaredMethodLjava_lang_String$Ljava_lang_Class() throws Exception {
        Method m = TestClass.class.getDeclaredMethod("pubMethod", new Class[0]);
        assertEquals("Returned incorrect method", 2, ((Integer) (m.invoke(new TestClass())))
                .intValue());
        m = TestClass.class.getDeclaredMethod("privMethod", new Class[0]);
    }
    */

    /**
     * @tests java.lang.Class#getDeclaredMethods()
     */
    public void test_getDeclaredMethods() throws Exception {
        Method[] m = TestClass.class.getDeclaredMethods();
        assertEquals("Returned incorrect number of methods", 3, m.length);
        m = SubTestClass.class.getDeclaredMethods();
        assertEquals("Returned incorrect number of methods", 0, m.length);
    }

    /**
     * @tests java.lang.Class#getDeclaringClass()
     */
    /* TODO(user): enable if Class.getDeclaringClass is mapped.
    public void test_getDeclaringClass() {
        assertEquals(ClassTest.class, TestClass.class.getDeclaringClass());
    }
    */

    /**
     * @tests java.lang.Class#getField(java.lang.String)
     */
    /* TODO(user): enable if Class.getField is mapped.
    public void test_getFieldLjava_lang_String() throws Exception {
        Field f = TestClass.class.getField("pubField");
        assertEquals("Returned incorrect field", 2, f.getInt(new TestClass()));
        try {
            f = TestClass.class.getField("privField");
            fail("Private field access failed to throw exception");
        } catch (NoSuchFieldException e) {
            // Correct
        }
    }
    */

    /**
     * @tests java.lang.Class#getFields()
     */
    /* TODO(user): enable if Class.getFields is mapped.
    public void test_getFields() throws Exception {
        Field[] f = TestClass.class.getFields();
        assertEquals("Incorrect number of fields", 2, f.length);
        f = SubTestClass.class.getFields();
        // Check inheritance of pub fields
        assertEquals("Incorrect number of fields", 2, f.length);
    }
    */

    /**
     * @tests java.lang.Class#getInterfaces()
     */
    /* TODO(user): enable if Class.getInterfaces is mapped.
    public void test_getInterfaces() {
        Class[] interfaces;
        List<?> interfaceList;
        interfaces = Object.class.getInterfaces();
        assertEquals("Incorrect interface list for Object", 0, interfaces.length);
        interfaceList = Arrays.asList(Vector.class.getInterfaces());
        assertTrue("Incorrect interface list for Vector", interfaceList
                .contains(Cloneable.class)
                && interfaceList.contains(Serializable.class)
                && interfaceList.contains(List.class));
    }
    */

    /**
     * @tests java.lang.Class#getMethod(java.lang.String, java.lang.Class[])
     */
    public void test_getMethodLjava_lang_String$Ljava_lang_Class() throws Exception {
        Method m = TestClass.class.getMethod("pubMethod", new Class[0]);
        assertEquals("Returned incorrect method", 2, ((Integer) (m.invoke(new TestClass())))
                .intValue());
        try {
            m = TestClass.class.getMethod("privMethod", new Class[0]);
            fail("Failed to throw exception accessing private method");
        } catch (NoSuchMethodException e) {
            // Correct
            return;
        }
    }

    /**
     * @tests java.lang.Class#getMethods()
     */
    /* TODO(user): enable if Class.getMethods is mapped.
    public void test_getMethods() throws Exception {
        Method[] m = TestClass.class.getMethods();
        assertEquals("Returned incorrect number of methods",
                     2 + Object.class.getMethods().length, m.length);
        m = SubTestClass.class.getMethods();
        assertEquals("Returned incorrect number of sub-class methods",
                     2 + Object.class.getMethods().length, m.length);
        // Number of inherited methods
    }
    */

    private static final class PrivateClass {
    }
    /**
     * @tests java.lang.Class#getModifiers()
     */
    public void test_getModifiers() {
        int dcm = PrivateClass.class.getModifiers();
        assertFalse("default class is public", Modifier.isPublic(dcm));
        assertFalse("default class is protected", Modifier.isProtected(dcm));
        assertTrue("default class is not private", Modifier.isPrivate(dcm));

        int ocm = Object.class.getModifiers();
        assertTrue("public class is not public", Modifier.isPublic(ocm));
        assertFalse("public class is protected", Modifier.isProtected(ocm));
        assertFalse("public class is private", Modifier.isPrivate(ocm));
    }

    /**
     * @tests java.lang.Class#getResource(java.lang.String)
     */
    /* TODO(user): enable if Class.getResource is mapped.
    public void test_getResourceLjava_lang_String() {
        final String name = "/org/apache/harmony/luni/tests/test_resource.txt";
        URL res = getClass().getResource(name);
        assertNotNull(res);
    }
    */

    /**
     * @tests java.lang.Class#getResourceAsStream(java.lang.String)
     */
    /* TODO(user): enable if Class.getResourceAsStream is mapped.
    public void test_getResourceAsStreamLjava_lang_String() throws Exception {
        final String name = "/org/apache/harmony/luni/tests/test_resource.txt";
        assertNotNull("the file " + name + " can not be found in this directory", getClass()
                .getResourceAsStream(name));

        final String nameBadURI = "org/apache/harmony/luni/tests/test_resource.txt";
        assertNull("the file " + nameBadURI + " should not be found in this directory",
                getClass().getResourceAsStream(nameBadURI));

        InputStream str = Object.class.getResourceAsStream("Class.class");
        assertNotNull("java.lang.Object couldn't find its class with getResource...", str);

        assertTrue("Cannot read single byte", str.read() != -1);
        assertEquals("Cannot read multiple bytes", 5, str.read(new byte[5]));
        str.close();

        InputStream str2 = getClass().getResourceAsStream("ClassTest.class");
        assertNotNull("Can't find resource", str2);
        assertTrue("Cannot read single byte", str2.read() != -1);
        assertEquals("Cannot read multiple bytes", 5, str2.read(new byte[5]));
        str2.close();
    }
    */

    /**
     * @tests java.lang.Class#getSuperclass()
     */
    public void test_getSuperclass() {
        assertNull("Object has a superclass???", Object.class.getSuperclass());
        assertSame("Normal class has bogus superclass", Writer.class,
                PrintWriter.class.getSuperclass());
        assertSame("Array class has bogus superclass", Object.class, PrintWriter[].class
                .getSuperclass());
        assertNull("Base class has a superclass", int.class.getSuperclass());
        assertNull("Interface class has a superclass", Cloneable.class.getSuperclass());
    }

    /**
     * @tests java.lang.Class#isArray()
     */
    /* TODO(user): enable if Class.isArray is mapped.
    public void test_isArray() throws ClassNotFoundException {
        assertTrue("Non-array type claims to be.", !int.class.isArray());
        Class<?> clazz = null;
        clazz = Class.forName("[I");
        assertTrue("int Array type claims not to be.", clazz.isArray());

        clazz = Class.forName("[Ljava.lang.Object;");
        assertTrue("Object Array type claims not to be.", clazz.isArray());

        clazz = Class.forName("java.lang.Object");
        assertTrue("Non-array Object type claims to be.", !clazz.isArray());
    }

    /**
     * @tests java.lang.Class#isAssignableFrom(java.lang.Class)
     */
    public void test_isAssignableFromLjava_lang_Class() {
        Class<?> clazz1 = null;
        Class<?> clazz2 = null;

        clazz1 = Object.class;
        clazz2 = Class.class;
        assertTrue("returned false for superclass", clazz1.isAssignableFrom(clazz2));

        clazz1 = TestClass.class;
        assertTrue("returned false for same class", clazz1.isAssignableFrom(clazz1));

        clazz1 = Runnable.class;
        clazz2 = Thread.class;
        assertTrue("returned false for implemented interface", clazz1.isAssignableFrom(clazz2));
    }

    /**
     * @tests java.lang.Class#isInterface()
     */
    /* TODO(user): enable if Class.isInterface is mapped.
    public void test_isInterface() throws ClassNotFoundException {
        assertTrue("Prim type claims to be interface.", !int.class.isInterface());
        Class<?> clazz = null;
        clazz = Class.forName("[I");
        assertTrue("Prim Array type claims to be interface.", !clazz.isInterface());

        clazz = Class.forName("java.lang.Runnable");
        assertTrue("Interface type claims not to be interface.", clazz.isInterface());
        clazz = Class.forName("java.lang.Object");
        assertTrue("Object type claims to be interface.", !clazz.isInterface());

        clazz = Class.forName("[Ljava.lang.Object;");
        assertTrue("Array type claims to be interface.", !clazz.isInterface());
    }
    */

    /**
     * @tests java.lang.Class#isPrimitive()
     */
    /* TODO(user): enable if Class.isPrimitive is mapped.
    public void test_isPrimitive() {
        assertFalse("Interface type claims to be primitive.", Runnable.class.isPrimitive());
        assertFalse("Object type claims to be primitive.", Object.class.isPrimitive());
        assertFalse("Prim Array type claims to be primitive.", int[].class.isPrimitive());
        assertFalse("Array type claims to be primitive.", Object[].class.isPrimitive());
        assertTrue("Prim type claims not to be primitive.", int.class.isPrimitive());
        assertFalse("Object type claims to be primitive.", Object.class.isPrimitive());
    }
    */

    /**
     * @tests java.lang.Class#newInstance()
     */
    public void test_newInstance() throws Exception {
        Class<?> clazz = null;
        clazz = Object.class;
        assertNotNull("new object instance was null", clazz.newInstance());

        clazz = Throwable.class;
        assertSame("new Throwable instance was not a throwable",
                   clazz, clazz.newInstance().getClass());

        clazz = Integer.class;
        try {
            clazz.newInstance();
            fail("Exception for instantiating a newInstance with no default constructor is not thrown");
        } catch (InstantiationException e) {
            // expected
        }
    }

    /**
     * @tests java.lang.Class#toString()
     */
    /* TODO(user): enable if Class.toString is mapped.
    public void test_toString() throws ClassNotFoundException {
        assertEquals("Class toString printed wrong value",
                     "int", int.class.toString());
        Class<?> clazz = null;
        clazz = Class.forName("[I");
        assertEquals("Class toString printed wrong value",
                     "class [I", clazz.toString());

        clazz = Class.forName("java.lang.Object");
        assertEquals("Class toString printed wrong value",
                     "class java.lang.Object", clazz.toString());

        clazz = Class.forName("[Ljava.lang.Object;");
        assertEquals("Class toString printed wrong value",
                     "class [Ljava.lang.Object;", clazz.toString());
    }
    */
}
