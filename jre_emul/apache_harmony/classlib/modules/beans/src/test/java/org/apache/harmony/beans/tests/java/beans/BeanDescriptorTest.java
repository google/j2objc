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

import org.apache.harmony.beans.tests.support.mock.MockJavaBean;

import java.beans.BeanDescriptor;

import junit.framework.TestCase;

/**
 * Unit test for BeanDescriptor.
 */
public class BeanDescriptorTest extends TestCase {

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Class under test for void BeanDescriptor(Class)
     */
    public void testBeanDescriptorClass() {
        String beanName = "BeanDescriptorTest.bean";
        MockJavaBean bean = new MockJavaBean(beanName);
        Class<? extends MockJavaBean> beanClass = bean.getClass();
        BeanDescriptor bd = new BeanDescriptor(beanClass);

        assertSame(beanClass, bd.getBeanClass());
        String displayName = beanClass.getName().substring(
                beanClass.getName().lastIndexOf('.') + 1);
        assertEquals(displayName, bd.getDisplayName());
        assertEquals(displayName, bd.getName());
        assertEquals(displayName, bd.getShortDescription());

        assertNotNull(bd.attributeNames());
        assertFalse(bd.isExpert());
        assertFalse(bd.isHidden());
        assertFalse(bd.isPreferred());
    }

    public void testBeanDescriptorClass_Null() {
        try {
            new BeanDescriptor(null);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }

    /*
     * Class under test for void BeanDescriptor(Class, Class)
     */
    public void testBeanDescriptorClassClass() {
        /*
         * String beanName = "BeanDescriptorTest.bean"; MockJavaBean bean = new
         * MockJavaBean(beanName); Class beanClass = bean.getClass(); Customizer
         * customizer = new MyCustomizer(); Class cusClass =
         * customizer.getClass(); BeanDescriptor bd = new
         * BeanDescriptor(beanClass, cusClass);
         *
         * assertSame(beanClass, bd.getBeanClass()); assertSame(cusClass,
         * bd.getCustomizerClass());
         *
         * String displayName = beanClass.getName().substring(
         * beanClass.getName().lastIndexOf('.') + 1); assertEquals(displayName,
         * bd.getDisplayName()); assertEquals(displayName, bd.getName());
         * assertEquals(displayName, bd.getShortDescription());
         *
         * assertNotNull(bd.attributeNames()); assertFalse(bd.isExpert());
         * assertFalse(bd.isHidden()); assertFalse(bd.isPreferred());
         */
    }

    public void testBeanDescriptorClassClass_BeanClassNull() {
        /*
         * Class beanClass = null; Customizer customizer = new MyCustomizer();
         * Class cusClass = customizer.getClass(); try { BeanDescriptor bd = new
         * BeanDescriptor(beanClass, cusClass); fail("Should throw
         * NullPointerException"); } catch (NullPointerException e) { }
         */
    }

    public void testBeanDescriptorClassClass_CustomizerClassNull() {
        String beanName = "BeanDescriptorTest.bean";
        MockJavaBean bean = new MockJavaBean(beanName);
        Class<? extends MockJavaBean> beanClass = bean.getClass();
        Class<?> cusClass = null;
        BeanDescriptor bd = new BeanDescriptor(beanClass, cusClass);

        assertSame(beanClass, bd.getBeanClass());
        assertNull(bd.getCustomizerClass());

        String displayName = beanClass.getName().substring(
                beanClass.getName().lastIndexOf('.') + 1);
        assertEquals(displayName, bd.getDisplayName());
        assertEquals(displayName, bd.getName());
        assertEquals(displayName, bd.getShortDescription());

        assertNotNull(bd.attributeNames());
        assertFalse(bd.isExpert());
        assertFalse(bd.isHidden());
        assertFalse(bd.isPreferred());
    }

    /**
     * @tests java.beans.BeanDescriptor#BeanDescriptor( java.lang.Class)
     */
    public void test_Ctor1_NullPointerException() {
        try {
            // Regression for HARMONY-225
            new BeanDescriptor(null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    /**
     * @tests java.beans.BeanDescriptor#BeanDescriptor( java.lang.Class,
     *        java.lang.Class)
     */
    public void test_Ctor2_NullPointerException() {
        try {
            // Regression for HARMONY-225
            new BeanDescriptor(null, String.class);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    /**
     * The test checks the method testNullaryConstructor()
     */
    public void testNullaryConstructor() {
        BeanDescriptor bd = new BeanDescriptor(String.class);
        assertEquals("String", bd.getName());
    }
}
