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

package org.apache.harmony.beans.tests.java.beans;

import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.apache.harmony.beans.tests.support.mock.MockJavaBean;

/**
 * Unit test for IndexedPropertyDescriptor.
 */
public class IndexedPropertyDescriptorTest extends TestCase {

    public void testEquals() throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertTrue(ipd.equals(ipd2));
        assertTrue(ipd.equals(ipd));
        assertTrue(ipd2.equals(ipd));
        assertFalse(ipd.equals(null));
    }

    /*
     * Read method
     */
    public void testEquals_ReadMethod() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("getPropertyFive",
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertFalse(ipd.equals(ipd2));
    }

    /*
     * read method null.
     */
    public void testEquals_ReadMethodNull() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = null;
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertFalse(ipd.equals(ipd2));
    }

    public void testEquals_WriteMethod() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("setPropertyFive",
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertFalse(ipd.equals(ipd2));
    }

    /*
     * write method null.
     */
    public void testEquals_WriteMethodNull() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = null;
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertFalse(ipd.equals(ipd2));
    }

    /*
     * Indexed read method.
     */
    public void testEquals_IndexedR() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("getPropertyFive",
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertFalse(ipd.equals(ipd2));
    }

    /*
     * Indexed read method null.
     */
    public void testEquals_IndexedRNull() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = null;
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertFalse(ipd.equals(ipd2));
    }

    /*
     * indexed write method.
     */
    public void testEquals_IndexedW() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("setPropertyFive",
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertFalse(ipd.equals(ipd2));
    }

    /*
     * Indexed write method null.
     */
    public void testEquals_IndexWNull() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = null;

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertFalse(ipd.equals(ipd2));
    }

    /*
     * Property Type.
     */
    public void testEquals_PropertyType() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                "PropertySix", beanClass);
        assertFalse(ipd.getPropertyType().equals(ipd2.getPropertyType()));
        assertFalse(ipd.equals(ipd2));
    }

    /*
     * Class under test for void IndexedPropertyDescriptor(String, Class)
     */
    public void testIndexedPropertyDescriptorStringClass()
            throws IntrospectionException, SecurityException,
            NoSuchMethodException {
        String propertyName = "propertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        String capitalName = propertyName.substring(0, 1).toUpperCase()
                + propertyName.substring(1);
        Method readMethod = beanClass.getMethod("get" + capitalName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + capitalName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + capitalName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + capitalName,
                new Class[] { Integer.TYPE, String.class });

        assertEquals(readMethod, ipd.getReadMethod());
        assertEquals(writeMethod, ipd.getWriteMethod());
        assertEquals(indexedReadMethod, ipd.getIndexedReadMethod());
        assertEquals(indexedWriteMethod, ipd.getIndexedWriteMethod());

        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());

        assertFalse(ipd.isBound());
        assertFalse(ipd.isConstrained());

        assertEquals(propertyName, ipd.getDisplayName());
        assertEquals(propertyName, ipd.getName());
        assertEquals(propertyName, ipd.getShortDescription());

        assertNotNull(ipd.attributeNames());

        assertFalse(ipd.isExpert());
        assertFalse(ipd.isHidden());
        assertFalse(ipd.isPreferred());

        // Regression for HARMONY-1236
        try {
            new IndexedPropertyDescriptor("0xDFRF", Float.TYPE);
            fail("IntrospectionException expected");
        } catch (IntrospectionException e) {
            // expected
        }
    }

    public void testIndexedPropertyDescriptorStringClass_PropertyNameNull()
            throws IntrospectionException {
        String propertyName = null;
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        try {
            new IndexedPropertyDescriptor(propertyName, beanClass);
            fail("Should throw IntrospectionException");
        } catch (IntrospectionException e) {
        }
    }

    public void testIndexedPropertyDescriptorStringClass_PropertyNameEmpty()
            throws IntrospectionException {
        String propertyName = "";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        try {
            new IndexedPropertyDescriptor(propertyName, beanClass);
            fail("Should throw IntrospectionException");
        } catch (IntrospectionException e) {
        }
    }

    public void testIndexedPropertyDescriptorStringClass_PropertyNameInvalid()
            throws IntrospectionException {
        String propertyName = "Not a property";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        try {
            new IndexedPropertyDescriptor(propertyName, beanClass);
            fail("Should throw IntrospectionException");
        } catch (IntrospectionException e) {
        }
    }

    public void testIndexedPropertyDescriptorStringClass_NotIndexedProperty()
            throws IntrospectionException {
        String propertyName = "propertyOne";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        try {
            new IndexedPropertyDescriptor(propertyName, beanClass);
            fail("Should throw IntrospectionException");
        } catch (IntrospectionException e) {
        }
    }

    public void testIndexedPropertyDescriptorStringClass_ClassNull()
            throws IntrospectionException {
        String propertyName = "propertyFour";
        Class<?> beanClass = null;
        try {
            new IndexedPropertyDescriptor(propertyName, beanClass);
            fail("Should throw IntrospectionException");
        } catch (IntrospectionException e) {
        }
    }

    /*
     * bean class does not implements java.io.Serializable
     */
    public void testIndexedPropertyDescriptorStringClass_NotBeanClass()
            throws IntrospectionException {
        String propertyName = "propertyOne";
        Class<NotJavaBean> beanClass = NotJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass);
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    private class MyClass {
        private int[] a;

        public void setA(int v, int i) {
            a[i] = v;
        }

        public void setA(int[] a) {
            this.a = a;
        }

        public int[] getA() {
            return a;
        }
    }

    /*
     * Class under test for void IndexedPropertyDescriptor(String, Class,
     * String, String, String, String)
     */
    public void testIndexedPropertyDescriptorStringClassStringStringStringString()
            throws IntrospectionException, SecurityException,
            NoSuchMethodException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"
                        + propertyName, "get" + propertyName, "set"
                        + propertyName);

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        assertEquals(readMethod, ipd.getReadMethod());
        assertEquals(writeMethod, ipd.getWriteMethod());
        assertEquals(indexedReadMethod, ipd.getIndexedReadMethod());
        assertEquals(indexedWriteMethod, ipd.getIndexedWriteMethod());

        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());

        assertFalse(ipd.isBound());
        assertFalse(ipd.isConstrained());

        assertEquals(propertyName, ipd.getDisplayName());
        assertEquals(propertyName, ipd.getName());
        assertEquals(propertyName, ipd.getShortDescription());

        assertNotNull(ipd.attributeNames());

        assertFalse(ipd.isExpert());
        assertFalse(ipd.isHidden());
        assertFalse(ipd.isPreferred());

        //empty method name
        new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"
                        + propertyName, "", "set"
                        + propertyName);

        try {
            new IndexedPropertyDescriptor("a", MyClass.class, "getA", "setA",
                    "", "setA");
            fail("Shoule throw exception");
        } catch (IntrospectionException e) {
        	// expected
        }

        try {
            new IndexedPropertyDescriptor(propertyName, beanClass, "",
                    "set" + propertyName, "get" + propertyName, "set"
                            + propertyName);
            fail("Shoule throw exception");
        } catch (IntrospectionException e) {
        	// expected
        }
        try {
            new IndexedPropertyDescriptor(propertyName, beanClass, "get"
                    + propertyName, "", "get" + propertyName, "set"
                    + propertyName);
            fail("Shoule throw exception");
        } catch (IntrospectionException e) {
        	// expected
        }
        try {
            new IndexedPropertyDescriptor(propertyName, beanClass, "get"
                    + propertyName, "set" + propertyName, "get" + propertyName,
                    "");
            fail("Shoule throw exception");
        } catch (IntrospectionException e) {
        	// expected
        }

        //null method name
        new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"
                        + propertyName, null, "set" + propertyName);
        new IndexedPropertyDescriptor(
                propertyName, beanClass, null, "set" + propertyName, "get"
                        + propertyName, "set" + propertyName);
        new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, null, "get"
                        + propertyName, "set" + propertyName);
        new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"
                        + propertyName, "get" + propertyName, null);
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_propNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        try {
            new IndexedPropertyDescriptor(null, beanClass,
                    "get" + propertyName, "set" + propertyName, "get"
                            + propertyName, "set" + propertyName);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {
        }
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_propEmpty() {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        try {
            new IndexedPropertyDescriptor("", beanClass, "get" + propertyName,
                    "set" + propertyName, "get" + propertyName, "set"
                            + propertyName);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {
        }
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_propInvalid()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        String invalidProp = "Not a prop";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                invalidProp, beanClass, "get" + propertyName, "set"
                        + propertyName, "get" + propertyName, "set"
                        + propertyName);
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());
        assertEquals(invalidProp, ipd.getName());
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_BeanClassNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class<?> beanClass = null;
        try {
            new IndexedPropertyDescriptor(propertyName, beanClass, "get"
                    + propertyName, "set" + propertyName, "get" + propertyName,
                    "set" + propertyName);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {
        }
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_ReadMethodNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, null, "set" + propertyName, "get"
                        + propertyName, "set" + propertyName);
        assertNull(ipd.getReadMethod());
        assertNotNull(ipd.getWriteMethod());
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_WriteMethodNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, null, "get"
                        + propertyName, "set" + propertyName);
        assertNotNull(ipd.getReadMethod());
        assertNull(ipd.getWriteMethod());
        assertEquals(String.class, ipd.getIndexedPropertyType());

        new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"+propertyName, "", "set" + propertyName);

        try{
            new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"+propertyName, "get" + propertyName, "");
        fail();
        }catch(Exception e){
        }
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_IndexedReadMethodNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"
                        + propertyName, null, "set" + propertyName);
        assertNull(ipd.getIndexedReadMethod());
        assertNotNull(ipd.getIndexedWriteMethod());
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_IndexedWriteMethodNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"
                        + propertyName, "get" + propertyName, null);
        assertNotNull(ipd.getIndexedReadMethod());
        assertNull(ipd.getIndexedWriteMethod());
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    /**
     * indexed read/write null
     *
     */
    public void testIndexedPropertyDescriptorStringClassStringStringStringString_RWNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, null, null, "get" + propertyName,
                "set" + propertyName);
        assertNull(ipd.getReadMethod());
        assertNull(ipd.getWriteMethod());
        assertEquals(String.class, ipd.getIndexedPropertyType());
        assertNull(ipd.getPropertyType());
    }

    /**
     * indexed read/write null
     *
     */
    public void testIndexedPropertyDescriptorStringClassStringStringStringString_IndexedRWNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        try {
            new IndexedPropertyDescriptor(propertyName, beanClass, "get"
                    + propertyName, "set" + propertyName, null, null);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {
        }
    }

    /**
     * index read /read null
     */
    public void testIndexedPropertyDescriptorStringClassStringStringStringString_RNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, null, "set" + propertyName, null,
                "set" + propertyName);
        assertEquals(String.class, ipd.getIndexedPropertyType());
        assertEquals(String[].class, ipd.getPropertyType());
        assertNotNull(ipd.getWriteMethod());
        assertNotNull(ipd.getIndexedWriteMethod());
    }

    /**
     * index write /write null
     */
    public void testIndexedPropertyDescriptorStringClassStringStringStringString_WNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, null, "get"
                        + propertyName, null);
        assertEquals(String.class, ipd.getIndexedPropertyType());
        assertEquals(String[].class, ipd.getPropertyType());
        assertNotNull(ipd.getReadMethod());
        assertNotNull(ipd.getIndexedReadMethod());
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_allNull()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, null, null, null, null);
        assertNull(ipd.getIndexedPropertyType());
        assertNull(ipd.getPropertyType());
        assertNull(ipd.getReadMethod());
        assertNull(ipd.getIndexedReadMethod());
    }

    /*
     * read/write incompatible
     *
     */
    public void testIndexedPropertyDescriptorStringClassStringStringStringString_RWIncompatible()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        String anotherProp = "PropertyFive";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"
                        + anotherProp, "get" + propertyName, "set"
                        + propertyName);
        assertEquals(String.class, ipd.getIndexedPropertyType());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals("set" + anotherProp, ipd.getWriteMethod().getName());
    }

    /**
     * IndexedRead/IndexedWrite incompatible
     *
     * @throws IntrospectionException
     *
     */
    public void testIndexedPropertyDescriptorStringClassStringStringStringString_IndexedRWIncompatible()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        String anotherProp = "PropertyFive";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"
                        + propertyName, "get" + propertyName, "set"
                        + anotherProp);
        assertEquals(String.class, ipd.getIndexedPropertyType());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals("set" + anotherProp, ipd.getIndexedWriteMethod().getName());
    }

    /*
     * ReadMethod/IndexedReadMethod incompatible
     *
     */
    public void testIndexedPropertyDescriptorStringClassStringStringStringString_RIndexedRcompatible()
            throws IntrospectionException {
        String propertyName = "PropertyFour";
        String anotherProp = "PropertyFive";
        Class<MockJavaBean> beanClass = MockJavaBean.class;
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, beanClass, "get" + propertyName, "set"
                        + propertyName, "get" + anotherProp, "set"
                        + anotherProp);
        assertEquals(String.class, ipd.getIndexedPropertyType());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals("set" + anotherProp, ipd.getIndexedWriteMethod().getName());
    }

    public void testIndexedPropertyDescriptorStringClassStringStringStringString_WrongArgumentNumber()
            throws IntrospectionException {
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor("a", DummyClass.class, null, "setAI",
                "getAI", "setAI");
        assertNotNull(ipd);
    }

    private class DummyClass {
        private int[] a;

        public void setAI(int v, int i) {
            a[i] = v;
        }

        public void setAI(int[] a) {
            this.a = a;
        }

        public int[] getA() {
            return a;
        }

        public int getAI(int i) {
            return a[i];
        }
    }

    /*
     * Class under test for void IndexedPropertyDescriptor(String, Method,
     * Method, Method, Method)
     */
    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        assertEquals(readMethod, ipd.getReadMethod());
        assertEquals(writeMethod, ipd.getWriteMethod());
        assertEquals(indexedReadMethod, ipd.getIndexedReadMethod());
        assertEquals(indexedWriteMethod, ipd.getIndexedWriteMethod());

        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());

        assertFalse(ipd.isBound());
        assertFalse(ipd.isConstrained());

        assertEquals(propertyName, ipd.getDisplayName());
        assertEquals(propertyName, ipd.getName());
        assertEquals(propertyName, ipd.getShortDescription());

        assertNotNull(ipd.attributeNames());

        assertFalse(ipd.isExpert());
        assertFalse(ipd.isHidden());
        assertFalse(ipd.isPreferred());
    }

    /*
     * propertyName=null
     */
    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_propNull()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        try {
            new IndexedPropertyDescriptor(null, readMethod, writeMethod,
                    indexedReadMethod, indexedWriteMethod);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {
        }
    }

    /*
     * propertyname="";
     */
    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_propEmpty()
            throws SecurityException, NoSuchMethodException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        try {
            new IndexedPropertyDescriptor("", readMethod, writeMethod,
                    indexedReadMethod, indexedWriteMethod);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {
        }
    }

    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_propInvalid()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        String invalidName = "An Invalid Property name";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                invalidName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertEquals(invalidName, ipd.getName());
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_ReadMethodNull()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, null, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertNull(ipd.getReadMethod());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_WriteMethodNull()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, null, indexedReadMethod,
                indexedWriteMethod);
        assertNull(ipd.getWriteMethod());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_IndexedReadMethodNull()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, null, indexedWriteMethod);
        assertNull(ipd.getIndexedReadMethod());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());
    }

    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_IndexedWriteMethodNull()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod, null);
        assertNull(ipd.getIndexedWriteMethod());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());

    }

    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_IndexedRWNull()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        try {
            new IndexedPropertyDescriptor(propertyName, readMethod,
                    writeMethod, null, null);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {
        }

    }

    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_RWNull()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, null, null, indexedReadMethod, indexedWriteMethod);

        assertNull(ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());

    }

    /*
     * read/write incompatible
     */
    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_RWIncompatible()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        String anotherProp = "PropertyFive";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + anotherProp,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertEquals(propertyName, ipd.getName());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());

    }

    /*
     * IndexedRead/IndexedWrite incompatible
     */
    public void testIndexedPropertyDescriptorStringMethodMethodMethodMethod_IndexedRWIncompatible()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        String anotherProp = "PropertyFive";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + anotherProp,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertEquals(propertyName, ipd.getName());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(String.class, ipd.getIndexedPropertyType());

        indexedReadMethod = beanClass.getMethod("get" + anotherProp,
                new Class[] { Integer.TYPE, Integer.TYPE });
        try {
            new IndexedPropertyDescriptor(
                    propertyName, readMethod, writeMethod, indexedReadMethod,
                    indexedWriteMethod);
            fail("should throw IntrosecptionException");
        } catch (IntrospectionException e) {
        	// expected
        }
    }

    public void testSetIndexedReadMethod() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, null, indexedWriteMethod);
        assertNull(ipd.getIndexedReadMethod());
        ipd.setIndexedReadMethod(indexedReadMethod);
        assertSame(indexedReadMethod, ipd.getIndexedReadMethod());
    }

    public void testSetIndexedReadMethod_invalid() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });

        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, null, null, indexedReadMethod, indexedWriteMethod);
        Method indexedReadMethod2 = beanClass.getMethod("getPropertySix",
                new Class[] { Integer.TYPE });
        try {
            ipd.setIndexedReadMethod(indexedReadMethod2);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {

        }
    }

    public void testSetIndexedReadMethod_null() throws SecurityException,
            NoSuchMethodException, IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertSame(indexedReadMethod, ipd.getIndexedReadMethod());
        ipd.setIndexedReadMethod(null);
        assertNull(ipd.getIndexedReadMethod());
    }

    /*
     * indexed read method without args
     */
    public void testSetIndexedReadMethod_RInvalidArgs()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertSame(indexedReadMethod, ipd.getIndexedReadMethod());
        try {
            ipd.setIndexedReadMethod(readMethod);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {
        }
    }

    /*
     * indexed read method with invalid arg type (!Integer.TYPE)
     */
    public void testSetIndexedReadMethod_RInvalidArgType()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertSame(indexedReadMethod, ipd.getIndexedReadMethod());
        try {
            ipd.setIndexedReadMethod(writeMethod);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {
        }
    }

    /*
     * indexed read method with void return.
     */
    public void testSetIndexedReadMethod_RInvalidReturn()
            throws SecurityException, NoSuchMethodException,
            IntrospectionException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertSame(indexedReadMethod, ipd.getIndexedReadMethod());
        Method voidMethod = beanClass.getMethod("getPropertyFourInvalid",
                new Class[] { Integer.TYPE });
        try {
            ipd.setIndexedReadMethod(voidMethod);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {
        }
    }

    public void testSetIndexedWriteMethod_null() throws IntrospectionException,
            NoSuchMethodException, NoSuchMethodException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);
        assertSame(indexedWriteMethod, ipd.getIndexedWriteMethod());
        ipd.setIndexedWriteMethod(null);
        assertNull(ipd.getIndexedWriteMethod());
    }

    public void testSetIndexedWriteMethod() throws IntrospectionException,
            NoSuchMethodException, NoSuchMethodException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod, null);
        assertNull(ipd.getIndexedWriteMethod());
        ipd.setIndexedWriteMethod(indexedWriteMethod);
        assertSame(indexedWriteMethod, ipd.getIndexedWriteMethod());
    }

    /*
     * bad arg count
     */
    public void testSetIndexedWriteMethod_noargs()
            throws IntrospectionException, NoSuchMethodException,
            NoSuchMethodException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod, null);
        assertNull(ipd.getIndexedWriteMethod());
        try {
            ipd.setIndexedWriteMethod(indexedReadMethod);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {
        }
    }

    /*
     * bad arg type
     */
    public void testSetIndexedWriteMethod_badargtype()
            throws IntrospectionException, NoSuchMethodException,
            NoSuchMethodException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod, null);
        assertNull(ipd.getIndexedWriteMethod());
        Method badArgType = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, Integer.TYPE });
        try {
            ipd.setIndexedWriteMethod(badArgType);
            fail("Should throw IntrospectionException");
        } catch (IntrospectionException e) {
        }
    }

    public void testSetIndexedWriteMethod_return()
            throws IntrospectionException, NoSuchMethodException,
            NoSuchMethodException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod, null);
        assertNull(ipd.getIndexedWriteMethod());
        Method badArgType = beanClass.getMethod("setPropertyFourInvalid",
                new Class[] { Integer.TYPE, String.class });
        ipd.setIndexedWriteMethod(badArgType);

        assertEquals(String.class, ipd.getIndexedPropertyType());
        assertEquals(String[].class, ipd.getPropertyType());
        assertEquals(Integer.TYPE, ipd.getIndexedWriteMethod().getReturnType());
    }

    public void testSetIndexedWriteMethod_InvalidIndexType()
            throws IntrospectionException, NoSuchMethodException,
            NoSuchMethodException {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod, null);
        assertNull(ipd.getIndexedWriteMethod());
        Method badArgType = beanClass.getMethod("setPropertyFourInvalid2",
                new Class[] { String.class, String.class });
        try {
            ipd.setIndexedWriteMethod(badArgType);
            fail("Should throw IntrospectionException");
        } catch (IntrospectionException e) {
        }

        ipd = new IndexedPropertyDescriptor("data", NormalBean.class);
        ipd.setIndexedReadMethod(null);
        try {
            ipd.setIndexedWriteMethod(NormalBean.class.getMethod("setData", Integer.TYPE, Integer.TYPE));
            fail("should throw IntrospectionException");
        } catch (IntrospectionException e) {
            // expected
        }
    }

    public void testSetIndexedMethodNullNull() throws Exception {
        try {
            IndexedPropertyDescriptor i = new IndexedPropertyDescriptor("a",
                    NormalBean.class, "getData", "setData", null,
                    "setData");
            i.setIndexedWriteMethod(null);
            fail("should throw IntrospectionException.");
        } catch (IntrospectionException e) {
            // expected
        }
        try {
            IndexedPropertyDescriptor i = new IndexedPropertyDescriptor("a",
                    NormalBean.class, "getData", "setData",
                    "getData", null);
            i.setIndexedReadMethod(null);
            fail("should throw IntrospectionException.");
        } catch (IntrospectionException e) {
            // expected
        }
    }


    public void testSetIndexedReadMethodFollowANullValue() throws Exception {
        try {
            IndexedPropertyDescriptor i = new IndexedPropertyDescriptor("a",
                    DummyBean.class, "readMethod", "writeMethod", null,
                    "indexedReadMethod");
            Method irm = DummyBean.class.getDeclaredMethod("indexedReadMethod",
                    Integer.TYPE);
            i.setIndexedReadMethod(irm);
            fail("should throw IntrospectionException.");
        } catch (IntrospectionException e) {
            // expected
        }
    }

    static class DummyBean {

        public int[] readMehtod() {
            return null;
        }

        public void writeMethod(int[] a) {
        }

        public double indexedReadMethod(int i) {
            return 0;
        }

        public void indexedWriteMethod(int i, int j) {
        }

    }

    class NotJavaBean {

        private String[] propertyOne;

        /**
         * @return Returns the propertyOne.
         */
        public String[] getPropertyOne() {
            return propertyOne;
        }

        /**
         * @param propertyOne
         *            The propertyOne to set.
         */
        public void setPropertyOne(String[] propertyOne) {
            this.propertyOne = propertyOne;
        }

        public String getPropertyOne(int i) {
            return getPropertyOne()[i];
        }

        public void setPropertyOne(int i, String value) {
            this.propertyOne[i] = value;
        }

    }

    //Regression Test
    class InCompatibleGetterSetterBean
    {
        private Object[] data = new Object[10];
        public void setData(Object[] data) {
            this.data = data;
        }
        public Object[] getDate() {
            return data;
        }
        public void setData(int index, Object o) {
            this.data[index] = o;
        }
    }

    public void testInCompatibleGetterSetterBean() {
        try {
            new IndexedPropertyDescriptor("data",
                    InCompatibleGetterSetterBean.class);
            fail("should throw IntrospectionException");
        } catch (IntrospectionException e) {
            // expected
        }
    }

    class NormalBean {
        private Object[] data = new Object[10];

        public Object[] getData() {
            return data;
        }

        public void setData(Object[] data) {
            this.data = data;
        }

        public void setData(int index, Object o) {
            data[index] = o;
        }

        public void setData(int index, int value) {
            // do nothing
        }

        public Object getData(int index) {
            return data[index];
        }
    }

    public void testEquals_superClass() throws Exception {
        PropertyDescriptor propertyDescriptor = new PropertyDescriptor("data",
                NormalBean.class);
        IndexedPropertyDescriptor indexedPropertyDescriptor = new IndexedPropertyDescriptor(
                "data", NormalBean.class);
        assertFalse(indexedPropertyDescriptor.equals(propertyDescriptor));
        assertTrue(propertyDescriptor.equals(indexedPropertyDescriptor));
    }

    public void testHashCode() throws Exception {
        String propertyName = "PropertyFour";
        Class<MockJavaBean> beanClass = MockJavaBean.class;

        Method readMethod = beanClass.getMethod("get" + propertyName,
                (Class[]) null);
        Method writeMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { String[].class });
        Method indexedReadMethod = beanClass.getMethod("get" + propertyName,
                new Class[] { Integer.TYPE });
        Method indexedWriteMethod = beanClass.getMethod("set" + propertyName,
                new Class[] { Integer.TYPE, String.class });

        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor(
                propertyName, readMethod, writeMethod, indexedReadMethod,
                indexedWriteMethod);

        IndexedPropertyDescriptor ipd2 = new IndexedPropertyDescriptor(
                propertyName, beanClass);

        assertEquals(ipd, ipd2);
        assertEquals(ipd.hashCode(), ipd2.hashCode());
    }

    public void testIncompatibleGetterAndIndexedGetterBean() {
        try {
            new IndexedPropertyDescriptor("data",
                    IncompatibleGetterAndIndexedGetterBean.class);
            fail("should throw IntrospectionException");

        } catch (IntrospectionException e) {
            //expected
        }
    }

    private class IncompatibleGetterAndIndexedGetterBean {
        private int[] data;

        public int getData() {
            return data[0];
        }

        public int getData(int index) {
            return data[index];
        }

        public void setData(int index, int data) {
            this.data[index] = data;
        }
    }
}
