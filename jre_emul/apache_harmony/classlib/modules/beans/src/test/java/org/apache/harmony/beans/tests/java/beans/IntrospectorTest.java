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

import org.apache.harmony.beans.tests.support.MisprintBean;
import org.apache.harmony.beans.tests.support.OtherBean;
import org.apache.harmony.beans.tests.support.SampleBean;
import org.apache.harmony.beans.tests.support.mock.FakeFox;
import org.apache.harmony.beans.tests.support.mock.FakeFox01;
import org.apache.harmony.beans.tests.support.mock.FakeFox011;
import org.apache.harmony.beans.tests.support.mock.FakeFox01BeanInfo;
import org.apache.harmony.beans.tests.support.mock.FakeFox02;
import org.apache.harmony.beans.tests.support.mock.FakeFox031;
import org.apache.harmony.beans.tests.support.mock.FakeFox041;
import org.apache.harmony.beans.tests.support.mock.FakeFox0411;
import org.apache.harmony.beans.tests.support.mock.MockButton;
import org.apache.harmony.beans.tests.support.mock.MockFoo;
import org.apache.harmony.beans.tests.support.mock.MockFooButton;
import org.apache.harmony.beans.tests.support.mock.MockFooLabel;
import org.apache.harmony.beans.tests.support.mock.MockFooStop;
import org.apache.harmony.beans.tests.support.mock.MockFooSub;
import org.apache.harmony.beans.tests.support.mock.MockFooSubSub;
import org.apache.harmony.beans.tests.support.mock.MockJavaBean;
import org.apache.harmony.beans.tests.support.mock.MockNullSubClass;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PropertyPermission;
import java.util.TooManyListenersException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for Introspector.
 */
public class IntrospectorTest extends TestCase {

    private String[] defaultPackage;

    public IntrospectorTest(String str) {
        super(str);
    }

    public IntrospectorTest() {}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        defaultPackage = Introspector.getBeanInfoSearchPath();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Introspector.flushCaches();
        Introspector.setBeanInfoSearchPath(defaultPackage);
    }

    /**
     * The test checks the getBeanDescriptor method
     */
    public void testBeanDescriptor() throws Exception {
        String[] oldBeanInfoSearchPath = Introspector.getBeanInfoSearchPath();
        try {
            Introspector
                    .setBeanInfoSearchPath(new String[] { "java.beans.infos" });
            BeanInfo info = Introspector.getBeanInfo(SampleBean.class);
            assertNotNull(info);
            BeanDescriptor descriptor = info.getBeanDescriptor();
            assertNotNull(descriptor);
            assertEquals(SampleBean.class, descriptor.getBeanClass());
        } finally {
            Introspector.setBeanInfoSearchPath(oldBeanInfoSearchPath);
        }
    }

    public void testBeanDescriptor_Same() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MockJavaBean.class);
        assertSame(beanInfo.getBeanDescriptor(), beanInfo.getBeanDescriptor());
    }

    /**
     * The test checks the getEventSetDescriptors method
     *
     * @throws IntrospectionException
     */
    public void testUnicastEventSetDescriptor() throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(SampleBean.class);
        assertNotNull(info);
        EventSetDescriptor[] descriptors = info.getEventSetDescriptors();
        assertNotNull(descriptors);
        for (EventSetDescriptor descriptor : descriptors) {
            Method m = descriptor.getAddListenerMethod();
            if (m != null) {
                Class<?>[] exceptionTypes = m.getExceptionTypes();
                boolean found = false;

                for (Class<?> et : exceptionTypes) {
                    if (et
                            .equals(TooManyListenersException.class)) {
                        assertTrue(descriptor.isUnicast());
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    assertFalse(descriptor.isUnicast());
                }
            }
        }
    }

    /**
     * The test checks the getEventSetDescriptors method
     *
     * @throws IntrospectionException
     */
    public void testEventSetDescriptorWithoutAddListenerMethod()
            throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(OtherBean.class);
        EventSetDescriptor[] descriptors;

        assertNotNull(info);
        descriptors = info.getEventSetDescriptors();
        assertNotNull(descriptors);
        assertEquals(1, descriptors.length);
        assertTrue(contains("sample", descriptors));
    }

    /**
     * The test checks the getEventSetDescriptors method
     *
     * @throws IntrospectionException
     */
    public void testIllegalEventSetDescriptor() throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(MisprintBean.class);
        assertNotNull(info);
        EventSetDescriptor[] descriptors = info.getEventSetDescriptors();
        assertNotNull(descriptors);
        assertEquals(0, descriptors.length);
    }

    /**
     * The test checks the getPropertyDescriptors method
     *
     * @throws IntrospectionException
     */
    public void testPropertyDescriptorWithSetMethod()
            throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(OtherBean.class);
        assertNotNull(info);
        PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
        assertNotNull(descriptors);
        assertEquals(2, descriptors.length);
        assertEquals("class", descriptors[0].getName());
        assertEquals("number", descriptors[1].getName());
    }

    public void testGetBeanInfo_NPE() throws IntrospectionException {
        // Regression for HARMONY-257
        try {
            Introspector.getBeanInfo((java.lang.Class<?>) null);
            fail("getBeanInfo should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            Introspector.getBeanInfo((java.lang.Class<?>) null,
                    (java.lang.Class<?>) null);
            fail("getBeanInfo should throw NullPointerException");
        } catch (NullPointerException e) {
        }

        try {
            Introspector.getBeanInfo((java.lang.Class<?>) null, 0);
            fail("getBeanInfo should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }

    /*
     * Common
     */
    public void testDecapitalize() {
        assertEquals("fooBah", Introspector.decapitalize("FooBah"));
        assertEquals("fooBah", Introspector.decapitalize("fooBah"));
        assertEquals("x", Introspector.decapitalize("X"));
        assertNull(Introspector.decapitalize(null));
        assertEquals("", Introspector.decapitalize(""));
        assertEquals("a1", Introspector.decapitalize("A1"));
    }

    public void testFlushCaches() throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(MockJavaBean.class);
        BeanDescriptor beanDesc = new BeanDescriptor(MockJavaBean.class);
        assertEquals(beanDesc.getName(), info.getBeanDescriptor().getName());
        assertEquals(beanDesc.isExpert(), info.getBeanDescriptor().isExpert());

        Introspector.flushCaches();
        BeanInfo cacheInfo = Introspector.getBeanInfo(MockJavaBean.class);
        assertNotSame(info, cacheInfo);
        beanDesc = new BeanDescriptor(MockJavaBean.class);
        assertEquals(beanDesc.getName(), info.getBeanDescriptor().getName());
        assertEquals(beanDesc.isExpert(), info.getBeanDescriptor().isExpert());
    }

    public void testFlushFromCaches() throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(MockFooSubSub.class);
        BeanInfo info2 = Introspector.getBeanInfo(MockFooSubSub.class);
        assertSame(info, info2);
        Introspector.flushFromCaches(MockFooSubSub.class);
        BeanInfo info3 = Introspector.getBeanInfo(MockFooSubSub.class);
        assertNotSame(info, info3);
    }

    public void testFlushFromCaches_Null() throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(MockJavaBean.class);
        BeanDescriptor beanDesc = new BeanDescriptor(MockJavaBean.class);
        assertEquals(beanDesc.getName(), info.getBeanDescriptor().getName());
        assertEquals(beanDesc.isExpert(), info.getBeanDescriptor().isExpert());
        try {
            Introspector.flushFromCaches(null);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }

    /*
     * Class under test for BeanInfo getBeanInfo(Class) No XXXXBeanInfo + test
     * cache info
     */
    public void testGetBeanInfoClass_no_BeanInfo()
            throws IntrospectionException {
        Class<FakeFox> beanClass = FakeFox.class;
        BeanInfo info = Introspector.getBeanInfo(beanClass);
        assertNull(info.getAdditionalBeanInfo());
        BeanDescriptor beanDesc = info.getBeanDescriptor();
        assertEquals("FakeFox", beanDesc.getName());
        assertEquals(0, info.getEventSetDescriptors().length);
        assertEquals(-1, info.getDefaultEventIndex());
        assertEquals(-1, info.getDefaultPropertyIndex());

        MethodDescriptor[] methodDesc = info.getMethodDescriptors();
        Method[] methods = beanClass.getMethods();
        assertEquals(methods.length, methodDesc.length);
        ArrayList<Method> methodList = new ArrayList<Method>();

        for (Method element : methods) {
            methodList.add(element);
        }

        for (MethodDescriptor element : methodDesc) {
            assertTrue(methodList.contains(element.getMethod()));
        }

        PropertyDescriptor[] propertyDesc = info.getPropertyDescriptors();
        assertEquals(1, propertyDesc.length);
        for (PropertyDescriptor element : propertyDesc) {
            if (element.getName().equals("class")) {
                assertNull(element.getWriteMethod());
                assertNotNull(element.getReadMethod());
            }
        }

        BeanInfo cacheInfo = Introspector.getBeanInfo(FakeFox.class);
        assertSame(info, cacheInfo);
    }

    /*
     * There is a BeanInfo class + test cache info
     */
    public void testGetBeanInfoClass_HaveBeanInfo()
            throws IntrospectionException {
        Class<FakeFox01> beanClass = FakeFox01.class;
        BeanInfo info = Introspector.getBeanInfo(beanClass);
        // printInfo(info);

        BeanInfo beanInfo = new FakeFox01BeanInfo();

        assertBeanInfoEquals(beanInfo, info);
        assertEquals(-1, info.getDefaultEventIndex());
        assertEquals(0, info.getDefaultPropertyIndex());

        BeanInfo cacheInfo = Introspector.getBeanInfo(beanClass);
        assertSame(info, cacheInfo);
    }

    public void testGetBeanInfoClass_ClassNull() throws IntrospectionException {
        try {
            Introspector.getBeanInfo(null);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }

    /*
     * Class under test for BeanInfo getBeanInfo(Class, Class)
     */
    public void testGetBeanInfoClassClass_Property()
            throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(MockFoo.class,
                MockFooStop.class);
        PropertyDescriptor[] pds = info.getPropertyDescriptors();

        assertEquals(2, pds.length);
        assertTrue(contains("name", String.class, pds));
        assertTrue(contains("complexLabel", MockFooLabel.class, pds));
    }

    public void testGetBeanInfoClassClass_Method()
            throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(MockFoo.class,
                MockFooStop.class);
        MethodDescriptor[] mds = info.getMethodDescriptors();

        assertEquals(4, mds.length);
        assertTrue(contains("getName", mds));
        assertTrue(contains("setName", mds));
        assertTrue(contains("getComplexLabel", mds));
        assertTrue(contains("setComplexLabel", mds));
        try {
            Introspector.getBeanInfo(MockFoo.class, Serializable.class);
            fail("Shoule throw exception, stopclass must be superclass of given bean");
        } catch (IntrospectionException e) {
        }
    }

    /*
     * BeanClass provide bean info about itself
     */
    public static class MockBeanInfo4BeanClassSelf implements BeanInfo {

        public void setValue(String v) throws Exception {
        }

        public int getValue() {
            return 0;
        }

        public BeanDescriptor getBeanDescriptor() {
            return null;
        }

        public EventSetDescriptor[] getEventSetDescriptors() {
            return new EventSetDescriptor[0];
        }

        public int getDefaultEventIndex() {
            return -1;
        }

        public int getDefaultPropertyIndex() {
            return -1;
        }

        public PropertyDescriptor[] getPropertyDescriptors() {
            return new PropertyDescriptor[0];
        }

        public MethodDescriptor[] getMethodDescriptors() {
            return new MethodDescriptor[0];
        }

        public BeanInfo[] getAdditionalBeanInfo() {
            return null;
        }

//        public Image getIcon(int iconKind) {
//            return null;
//        }
    }

    public void test_BeanInfo_Self() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MockBeanInfo4BeanClassSelf.class);
        assertEquals(0, info.getMethodDescriptors().length);
        assertEquals(0, info.getPropertyDescriptors().length);
        assertEquals(0, info.getEventSetDescriptors().length);
    }

    /*
     * Introspect static methods
     */
    public void testGetBeanInfo_StaticMethods() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(StaticClazz.class);
        PropertyDescriptor[] propertyDescriptors = beanInfo
                .getPropertyDescriptors();
        assertEquals(1, propertyDescriptors.length);
        assertTrue(contains("class", Class.class, propertyDescriptors));
        MethodDescriptor[] methodDescriptors = beanInfo.getMethodDescriptors();
        assertTrue(contains("getStaticMethod", methodDescriptors));
        assertTrue(contains("setStaticMethod", methodDescriptors));

        beanInfo = Introspector.getBeanInfo(StaticClazzWithProperty.class);
        propertyDescriptors = beanInfo.getPropertyDescriptors();
        assertEquals(1, propertyDescriptors.length);
        methodDescriptors = beanInfo.getMethodDescriptors();
        assertTrue(contains("getStaticName", methodDescriptors));
        assertTrue(contains("setStaticName", methodDescriptors));
    }

    public void testMockIncompatibleGetterAndIndexedGetterBean() throws Exception {
        Class<?> beanClass = MockIncompatibleGetterAndIndexedGetterBean.class;
        BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
        PropertyDescriptor pd = null;
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            pd = pds[i];
            if (pd.getName().equals("data")) {
                break;
            }
        }
        assertNotNull(pd);
        assertTrue(pd instanceof IndexedPropertyDescriptor);
        IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
        assertNull(ipd.getReadMethod());
        assertNull(ipd.getWriteMethod());
        Method indexedReadMethod = beanClass.getMethod("getData",
                new Class[] { int.class });
        Method indexedWriteMethod = beanClass.getMethod("setData", new Class[] {
                int.class, int.class });
        assertEquals(indexedReadMethod, ipd.getIndexedReadMethod());
        assertEquals(indexedWriteMethod, ipd.getIndexedWriteMethod());
    }

    public void testMockIncompatibleSetterAndIndexedSetterBean() throws Exception {
        Class<?> beanClass = MockIncompatibleSetterAndIndexedSetterBean.class;
        BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
        PropertyDescriptor pd = null;
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            pd = pds[i];
            if (pd.getName().equals("data")) {
                break;
            }
        }
        assertNotNull(pd);
        assertTrue(pd instanceof IndexedPropertyDescriptor);
        IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
        assertNull(ipd.getReadMethod());
        assertNull(ipd.getWriteMethod());
        Method indexedReadMethod = beanClass.getMethod("getData",
                new Class[] { int.class });
        Method indexedWriteMethod = beanClass.getMethod("setData", new Class[] {
                int.class, int.class });
        assertEquals(indexedReadMethod, ipd.getIndexedReadMethod());
        assertEquals(indexedWriteMethod, ipd.getIndexedWriteMethod());
    }

    public void testMockIncompatibleAllSetterAndGetterBean() throws Exception {
        Class<?> beanClass = MockIncompatibleAllSetterAndGetterBean.class;
        BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
        PropertyDescriptor pd = null;
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            pd = pds[i];
            if (pd.getName().equals("data")) {
                break;
            }
        }
        assertNotNull(pd);
        assertTrue(pd instanceof IndexedPropertyDescriptor);
        IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
        assertNull(ipd.getReadMethod());
        assertNull(ipd.getWriteMethod());
        Method indexedReadMethod = beanClass.getMethod("getData",
                new Class[] { int.class });
        Method indexedWriteMethod = beanClass.getMethod("setData", new Class[] {
                int.class, int.class });
        assertEquals(indexedReadMethod, ipd.getIndexedReadMethod());
        assertEquals(indexedWriteMethod, ipd.getIndexedWriteMethod());
    }

    public class MockIncompatibleGetterAndIndexedGetterBean {
        private int[] datas;

        public int getData() {
            return datas[0];
        }

        public int getData(int index) {
            return datas[index];
        }

        public void setData(int index, int data) {
            this.datas[index] = data;
        }
    }

    public class MockIncompatibleSetterAndIndexedSetterBean {

        private int[] datas;

        public int getData(int index){
            return datas[index];
        }

        public void setData(int index, int data) {
            this.datas[index] = data;
        }

        public void setData(int data){
            this.datas[0] = data;
        }

    }

    public class MockIncompatibleAllSetterAndGetterBean {

        private int[] datas;

        public int getData(){
            return datas[0];
        }

        public int getData(int index){
            return datas[index];
        }

        public void setData(int index, int data) {
            this.datas[index] = data;
        }

        public void setData(int data){
            this.datas[0] = data;
        }

        public void setData(){
            this.datas[0] = 0;
        }

    }

    public static class StaticClazz {

        /*
         * public static get method
         */
        public static String getStaticMethod() {
            return "static class";
        }

        /*
         * public static set method
         */
        public static void setStaticMethod(String content) {
            // do nothing
        }

    }

    public static class StaticClazzWithProperty {

        private static String staticName = "Static Clazz";

        /*
         * public static get method
         */
        public static String getStaticName() {
            return staticName;
        }

        /*
         * public static set method
         */
        public static void setStaticName(String name) {
            staticName = name;
        }
    }

    public void testGetBeanInfoClassClass_StopNull()
            throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(MockFoo.class);// , null);
        PropertyDescriptor[] pds = info.getPropertyDescriptors();
        boolean name = false;
        boolean label = false;
        for (PropertyDescriptor element : pds) {
            if (element.getName().equals("name")) {
                name = true;
            }
            if (element.getName().equals("label")) {
                label = true;
            }
        }

        assertTrue(name);
        assertTrue(label);
    }

    public void testGetBeanInfoClassClass_ClassNull()
            throws IntrospectionException {
        try {
            Introspector.getBeanInfo(null, MockFooStop.class);
            fail("Should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }

    /*
     * StopClass is not a supper class of the bean.
     */
    public void testGetBeanInfoClassClass_ClassInvalid()
            throws IntrospectionException {
        try {
            Introspector.getBeanInfo(MockButton.class, MockFooStop.class);
            fail("Should throw IntrospectionException.");
        } catch (IntrospectionException e) {
        }
    }

    /*
     * FLAG=IGNORE_ALL_BEANINFO;
     */
    public void testGetBeanInfoClassint_IGNORE_ALL_Property()
            throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(MockFooSub.class,
                Introspector.IGNORE_ALL_BEANINFO);
        PropertyDescriptor[] pds = info.getPropertyDescriptors();
        int text = 0;
        for (PropertyDescriptor element : pds) {
            String name = element.getName();
            if (name.startsWith("text")) {
                text++;
                assertEquals("text", name);
            }
        }
        assertEquals(1, text);
    }

    /*
     * FLAG=IGNORE_ALL_BEANINFO;
     */
    public void testGetBeanInfoClassint_IGNORE_ALL_Method()
            throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(MockFooSub.class,
                Introspector.IGNORE_ALL_BEANINFO);
        MethodDescriptor[] mds = info.getMethodDescriptors();
        int getMethod = 0;
        int setMethod = 0;
        for (MethodDescriptor element : mds) {
            String name = element.getName();
            if (name.startsWith("getText")) {
                getMethod++;
                assertEquals("getText", name);
            }
            if (name.startsWith("setText")) {
                setMethod++;
                assertEquals("setText", name);
            }
        }

        assertEquals(1, getMethod);
        assertEquals(1, setMethod);
    }

    /*
     * FLAG=IGNORE_ALL_BEANINFO;
     */
    public void testGetBeanInfoClassint_IGNORE_ALL_Event()
            throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(MockFooSub.class,
                Introspector.IGNORE_ALL_BEANINFO);
        EventSetDescriptor[] esds = info.getEventSetDescriptors();

        assertEquals(1, esds.length);
        assertTrue(contains("mockPropertyChange", esds));
    }

    /*
     * FLAG invalid;
     */
    public void testGetBeanInfoClassint_FLAG_Invalid()
            throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(MockFooSub.class, -1);
        PropertyDescriptor[] pds = info.getPropertyDescriptors();

        Introspector.getBeanInfo(MockFooSub.class,
                Introspector.IGNORE_ALL_BEANINFO);
        PropertyDescriptor[] pds2 = info.getPropertyDescriptors();
        assertEquals(pds.length, pds2.length);
        for (int i = 0; i < pds.length; i++) {
            assertEquals(pds[i], pds2[i]);
        }
    }

    public void testGetBeanInfoSearchPath() {
        String[] path = Introspector.getBeanInfoSearchPath();
        assertEquals(1, path.length);
        assertTrue(path[0].endsWith("beans.infos"));
    }

    public void testGetBeanInfoSearchPath_Default()
            throws IntrospectionException, ClassNotFoundException {
        BeanInfo info = Introspector.getBeanInfo(MockFooButton.class);
        PropertyDescriptor[] pds = info.getPropertyDescriptors();
        BeanDescriptor beanDesc;

        assertEquals(2, pds.length);
        assertEquals("class", pds[0].getName());

        beanDesc = info.getBeanDescriptor();
        assertEquals("MockFooButton", beanDesc.getName());
    }

    /*
     * Test Introspection with BeanInfo No immediate BeanInfo Have super
     * BeanInfo
     */
    public void testBeanInfo_1() throws IntrospectionException {
        Class<FakeFox011> beanClass = FakeFox011.class;
        BeanInfo info = Introspector.getBeanInfo(beanClass);
        assertNull(info.getAdditionalBeanInfo());
        BeanDescriptor beanDesc = info.getBeanDescriptor();
        assertEquals("FakeFox011", beanDesc.getName());
        assertEquals(0, info.getEventSetDescriptors().length);
        assertEquals(-1, info.getDefaultEventIndex());
        assertEquals(0, info.getDefaultPropertyIndex());

        MethodDescriptor[] methodDesc = info.getMethodDescriptors();

        assertEquals(4, methodDesc.length);

        PropertyDescriptor[] propertyDesc = info.getPropertyDescriptors();
        assertEquals(2, propertyDesc.length);
        for (PropertyDescriptor element : propertyDesc) {
            if (element.getName().equals("class")) {
                assertNull(element.getWriteMethod());
                assertNotNull(element.getReadMethod());
            }
        }
    }

    public void testBeanInfo_2() throws IntrospectionException {
        Class<FakeFox02> beanClass = FakeFox02.class;
        BeanInfo info = Introspector.getBeanInfo(beanClass);
        assertNull(info.getAdditionalBeanInfo());
        BeanDescriptor beanDesc = info.getBeanDescriptor();
        assertEquals("FakeFox02", beanDesc.getName());
        assertEquals(0, info.getEventSetDescriptors().length);
        assertEquals(-1, info.getDefaultEventIndex());
        assertEquals(-1, info.getDefaultPropertyIndex());

        PropertyDescriptor[] propertyDesc = info.getPropertyDescriptors();
        for (PropertyDescriptor element : propertyDesc) {
            if (element.getName().equals("fox02")) {
                assertEquals("fox02.beaninfo", element.getDisplayName());
            }
        }
    }

    public void testPropertySort() throws IntrospectionException {
        Class<FakeFox70> beanClass = FakeFox70.class;
        BeanInfo info = Introspector.getBeanInfo(beanClass);
        PropertyDescriptor[] descs = info.getPropertyDescriptors();
        String[] names = { "a", "aaa", "bb", "bbb", "bc", "class", "ddd", "ff", };
        for (int i = 0; i < descs.length; i++) {
            assertEquals(names[i], descs[i].getName());
        }
    }

    public void testIntrospectProperties() throws IntrospectionException {
        Class<FakeFox80> beanClass = FakeFox80.class;
        BeanInfo info = Introspector.getBeanInfo(beanClass);
        assertEquals(2, info.getPropertyDescriptors().length);
    }

    public void testIntrospectProperties2() throws IntrospectionException {
        Class<FakeFox90> beanClass = FakeFox90.class;
        BeanInfo info = Introspector.getBeanInfo(beanClass);
        // printInfo(info);
        PropertyDescriptor[] pds = info.getPropertyDescriptors();
        assertEquals(2, pds.length);
        assertNull(pds[1].getReadMethod());
    }

    /*
     * If Bean1 has wrong getter method: public int getProp6(boolean i), then
     * Introspector.getBeanInfo(Bean1) throws java.beans.IntrospectionException.
     */
    public void testIntrospectorGetBeanInfo() throws IntrospectionException {
        Class<FakeFoxInfo> clazz = FakeFoxInfo.class;
        BeanInfo info = Introspector.getBeanInfo(clazz);
        // printInfo(info);
        PropertyDescriptor[] pds = info.getPropertyDescriptors();
        assertEquals("prop6", pds[1].getName());
        assertNull(pds[1].getReadMethod());
        assertNotNull(pds[1].getWriteMethod());
    }

    public void testGetBeanInfoExplicitNull() throws Exception {
        Introspector.flushCaches();
        BeanInfo subinfo = Introspector.getBeanInfo(MockNullSubClass.class);
        assertNotNull(subinfo.getPropertyDescriptors());
        assertNotNull(subinfo.getEventSetDescriptors());
        assertNotNull(subinfo.getMethodDescriptors());
        assertEquals(-1, subinfo.getDefaultEventIndex());
        assertEquals(-1, subinfo.getDefaultPropertyIndex());
    }

    static class FakeFoxInfo {

        public int getProp6(boolean i) {
            return 0;
        }

        public void setProp6(boolean i) {

        }
    }

    /*
     * setBeanInfoSearchPath method of Introspector doesn't invoke
     * checkPropertiesAccess method of SecurityManager class
     */
    public void testSetBeanInfoSearchPath2() {
        try {
            // test here
            {
                String[] newPath = new String[] { "a", "b" };
                Introspector.setBeanInfoSearchPath(newPath);
                String[] path = Introspector.getBeanInfoSearchPath();
                assertTrue(Arrays.equals(newPath, path));
                assertNotSame(newPath, path);
                path[0] = "c";
                newPath[0] = "d";
                String[] path2 = Introspector.getBeanInfoSearchPath();
                assertEquals("d", path2[0]);
            }
            {
                String[] newPath = new String[] {};
                Introspector.setBeanInfoSearchPath(newPath);
                String[] path = Introspector.getBeanInfoSearchPath();
                assertNotSame(newPath, path);
                assertTrue(Arrays.equals(newPath, path));
            }
            {
                String[] newPath = null;
                Introspector.setBeanInfoSearchPath(newPath);
                try {
                    Introspector.getBeanInfoSearchPath();
                    fail("Should throw NullPointerException.");
                } catch (NullPointerException e) {
                }
            }
        } catch (SecurityException e) {
        }
    }

    /*
     * @test setBeanInfoSearchPath
     *
     * Change the sequence of the paths in Introspector.searchpaths, check
     * whether the BeanInfo is consistent with the bean class
     */
    public void testSetBeanInfoSearchPath_SameClassesInDifferentPackage()
            throws IntrospectionException {
        // set the search path in the correct sequence
        Introspector
                .setBeanInfoSearchPath(new String[] {
                        "org.apache.harmony.beans.tests.support.mock.homonymy.mocksubject1.info",
                        "org.apache.harmony.beans.tests.support.mock.homonymy.mocksubject2.info", });

        BeanInfo beanInfo = Introspector
                .getBeanInfo(org.apache.harmony.beans.tests.support.mock.homonymy.mocksubject1.MockHomonymySubject.class);
        BeanDescriptor beanDesc = beanInfo.getBeanDescriptor();

        assertEquals(beanDesc.getName(), "mocksubject1");
        assertEquals(
                beanDesc.getBeanClass(),
                org.apache.harmony.beans.tests.support.mock.homonymy.mocksubject1.MockHomonymySubject.class);

        // set the search path in the reverse sequence
        Introspector
                .setBeanInfoSearchPath(new String[] {
                        "org.apache.harmony.beans.tests.support.mock.homonymy.mocksubject2.info",
                        "org.apache.harmony.beans.tests.support.mock.homonymy.mocksubject1.info", });

        beanInfo = Introspector
                .getBeanInfo(org.apache.harmony.beans.tests.support.mock.homonymy.mocksubject1.MockHomonymySubject.class);
        beanDesc = beanInfo.getBeanDescriptor();

        assertEquals(beanDesc.getName(), "mocksubject1");
        assertEquals(
                beanDesc.getBeanClass(),
                org.apache.harmony.beans.tests.support.mock.homonymy.mocksubject1.MockHomonymySubject.class);

    }

    static class MockSecurity2 extends SecurityManager {

        @Override
        public void checkPermission(Permission p) {
            if (p instanceof PropertyPermission) {
                throw new SecurityException("Expected exception.");
            }
        }
    }

    /*
     * If Bean3 has empty BeanInfo, then
     * Introspector.getBeanInfo(Bean3.class).getBeanDescriptor() returns null.
     */
    public void testNullBeanDescriptor() throws IntrospectionException {
        Class<Bean3> clazz = Bean3.class;
        BeanInfo info = Introspector.getBeanInfo(clazz);
        // printInfo(info);
        assertNotNull(info.getBeanDescriptor());
    }

    public static class Bean3 {

        private String prop1;

        public String getProp1() {
            return prop1;
        }

        public void setProp1(String prop1) {
            this.prop1 = prop1;
        }
    }

    public static class Bean3BeanInfo extends SimpleBeanInfo {
    }

    public void testGetPropertyDescriptors_H1838()
            throws IntrospectionException {
        // Regression for HARMONY-1838
        PropertyDescriptor[] propertyDescriptors = Introspector.getBeanInfo(
                Bean.class).getPropertyDescriptors();

        assertEquals("class", propertyDescriptors[0].getName());
        assertEquals("prop1", propertyDescriptors[1].getName());
        assertEquals("prop2", propertyDescriptors[2].getName());
        assertEquals(3, propertyDescriptors.length);
    }

    public static class Bean {
        public String getProp1(int i) {
            return null;
        }

        public void setProp2(int i, String str) {
        }
    }

    /*
     *
     */
    public void testGetPropertyDescriptors() throws IntrospectionException {
        Class<Bean2> clazz = Bean2.class;
        BeanInfo info = Introspector.getBeanInfo(clazz);
        PropertyDescriptor[] pds = info.getPropertyDescriptors();

        assertEquals(2, pds.length);
        assertEquals("property1", pds[0].getName());
        assertEquals("property8", pds[1].getName());
    }

    public void testHarmony4861() throws IntrospectionException {
		final PropertyDescriptor[] propertyDescriptors = Introspector
				.getBeanInfo(TestBean.class).getPropertyDescriptors();

		for (PropertyDescriptor d : propertyDescriptors) {
			if (d.getName().equals("prop1")) { //$NON-NLS-1$
				assertEquals("isProp1", d.getReadMethod().getName()); //$NON-NLS-1$
				return;
			}
		}
	}

	public static class TestBean {
		boolean prop1;

		public void setProp1(boolean prop1) {
			this.prop1 = prop1;
		}

		public boolean isProp1() {
			return prop1;
		}

		public boolean getProp1() {
			return prop1;
		}
	}

    public static TestSuite suite() {
//        TestSuite suite = new TestSuite();
        TestSuite suite = new TestSuite(IntrospectorTest.class);

//        suite.addTest(new IntrospectorTest("testIntrospection_7"));
        return suite;
    }

    public static class Bean1 {

        private int i;

        public int ggetI() {
            return i;
        }

        public void ssetI(int i) {
            this.i = i;
        }
    }

    public static class Bean1BeanInfo extends SimpleBeanInfo {

        @Override
        public PropertyDescriptor[] getPropertyDescriptors() {
            try {
                PropertyDescriptor _property1 = new PropertyDescriptor(
                        "property1", Bean1.class, "ggetI", "ssetI");
                PropertyDescriptor[] pds = new PropertyDescriptor[] { _property1 };
                return pds;
            } catch (IntrospectionException exception) {
                return null;
            }
        }
    }

    public static class Bean2 extends Bean1 {

        private int property8;

        public int getProperty8() {
            return property8;
        }

        public void setProperty8(int property8) {
            this.property8 = property8;
        }
    }

    private static void assertBeanInfoEquals(BeanInfo beanInfo, BeanInfo info) {
        // compare BeanDescriptor
        assertEquals(beanInfo.getBeanDescriptor().getDisplayName(), info
                .getBeanDescriptor().getDisplayName());

        // compare MethodDescriptor
        MethodDescriptor[] methodDesc1 = beanInfo.getMethodDescriptors();
        MethodDescriptor[] methodDesc2 = info.getMethodDescriptors();
        assertEquals(methodDesc1.length, methodDesc2.length);

        for (int i = 0; i < methodDesc1.length; i++) {
            assertEquals(methodDesc1[i].getMethod(), methodDesc2[i].getMethod());
            assertEquals(methodDesc1[i].getDisplayName(), methodDesc2[i]
                    .getDisplayName());
        }

        // compare PropertyDescriptor
        PropertyDescriptor[] propertyDesc1 = beanInfo.getPropertyDescriptors();
        PropertyDescriptor[] propertyDesc2 = info.getPropertyDescriptors();
        assertEquals(propertyDesc1.length, propertyDesc2.length);

        for (int i = 0; i < propertyDesc1.length; i++) {
            assertEquals(propertyDesc1[i], propertyDesc2[i]);
        }

        // compare EventSetDescriptor
        EventSetDescriptor[] eventDesc1 = beanInfo.getEventSetDescriptors();
        EventSetDescriptor[] eventDesc2 = beanInfo.getEventSetDescriptors();
        if (eventDesc1 == null) {
            assertNull(eventDesc2);
        }
        if (eventDesc2 == null) {
            assertNull(eventDesc1);
        }
        if ((eventDesc1 != null) && (eventDesc1 != null)) {
            assertEquals(eventDesc1.length, eventDesc2.length);
            for (int i = 0; i < eventDesc1.length; i++) {
                assertEquals(eventDesc1[i].getAddListenerMethod(),
                        eventDesc2[i].getAddListenerMethod());
                assertEquals(eventDesc1[i].getRemoveListenerMethod(),
                        eventDesc2[i].getRemoveListenerMethod());
                assertEquals(eventDesc1[i].getGetListenerMethod(),
                        eventDesc2[i].getGetListenerMethod());
                assertEquals(eventDesc1[i].getListenerMethods().length,
                        eventDesc2[i].getListenerMethods().length);
            }
        }

    }

    private static boolean contains(String propName, Class<?> propClass,
            PropertyDescriptor[] pds)
    {
        for (PropertyDescriptor pd : pds) {
            if (propName.equals(pd.getName()) &&
                    propClass.equals(pd.getPropertyType())) {
                return true;
            }
        }

        return false;
    }

    private static boolean contains(String methodName,
            MethodDescriptor[] mds)
    {
        for (MethodDescriptor md : mds) {
            if (methodName.equals(md.getName())) {
                return true;
            }
        }

        return false;
    }

    private static boolean contains(String eventSetName,
            EventSetDescriptor[] esds)
    {
        for (EventSetDescriptor esd : esds) {
            if (eventSetName.equals(esd.getName())) {
                return true;
            }
        }

        return false;
    }

    /*
     * The following classes are used to test introspect Event
     */

    static class FakeFox70 {

        int ddd;

        int bbb;

        int bc;

        Integer ff;

        String a;

        String bb;

        String aaa;

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getAaa() {
            return aaa;
        }

        public void setAaa(String aaa) {
            this.aaa = aaa;
        }

        public String getBb() {
            return bb;
        }

        public void setBb(String bb) {
            this.bb = bb;
        }

        public int getBbb() {
            return bbb;
        }

        public void setBbb(int bbb) {
            this.bbb = bbb;
        }

        public int getBc() {
            return bc;
        }

        public void setBc(int bc) {
            this.bc = bc;
        }

        public int getDdd() {
            return ddd;
        }

        public void setDdd(int ddd) {
            this.ddd = ddd;
        }

        public Integer getFf() {
            return ff;
        }

        public void setFf(Integer ff) {
            this.ff = ff;
        }
    }

    static class FakeFox80 {

        public String get() {
            return null;
        }

        public String get123() {
            return null;
        }
    }

    static class FakeFox90 {

        public String getFox(String value) {
            return null;
        }

        public void setFox(String value) {

        }
    }

    public void testProperty() throws IntrospectionException {
        Class<MockSubClassForPorpertiesStandard> beanClass = MockSubClassForPorpertiesStandard.class;
        BeanInfo info = Introspector.getBeanInfo(beanClass);
        assertEquals(-1, info.getDefaultEventIndex());
        assertEquals(-1, info.getDefaultPropertyIndex());
        PropertyDescriptor[] pds = info.getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            assertFalse(pd.isBound());
            assertFalse(pd.isConstrained());
            assertFalse(pd.isExpert());
            assertFalse(pd.isHidden());
            assertFalse(pd.isPreferred());
        }
        assertEquals(2, info.getPropertyDescriptors().length);

        BeanInfo dummyInfo = Introspector.getBeanInfo(FakeFox041.class);
        PropertyDescriptor[] p = dummyInfo.getPropertyDescriptors();
        assertFalse(p[0].isBound());
        assertFalse(p[0].isConstrained());
        assertFalse(p[1].isBound());
        assertFalse(p[1].isConstrained());
        assertTrue(p[2].isBound());
        assertTrue(p[2].isConstrained());

        dummyInfo = Introspector.getBeanInfo(FakeFox0411.class);
        p = dummyInfo.getPropertyDescriptors();
        assertFalse(p[0].isBound());
        assertFalse(p[0].isConstrained());
        assertFalse(p[1].isBound());
        assertFalse(p[1].isConstrained());
        assertTrue(p[2].isBound());
        assertFalse(p[2].isConstrained());
        assertTrue(p[3].isBound());
        assertTrue(p[3].isConstrained());

        dummyInfo = Introspector.getBeanInfo(FakeFox0411.class, FakeFox041.class);
        p = dummyInfo.getPropertyDescriptors();
        assertFalse(p[0].isBound());
        assertFalse(p[0].isConstrained());
    }

    public void testDefaultEvent() throws IntrospectionException {
        Class<?> beanClass = MockClassForDefaultEvent.class;
        BeanInfo info = Introspector.getBeanInfo(beanClass);
        assertEquals(-1, info.getDefaultEventIndex());
        assertEquals(-1, info.getDefaultPropertyIndex());
        EventSetDescriptor[] events = info.getEventSetDescriptors();
        for (EventSetDescriptor event : events) {
            assertFalse(event.isUnicast());
            assertTrue(event.isInDefaultEventSet());
            assertFalse(event.isExpert());
            assertFalse(event.isHidden());
            assertFalse(event.isPreferred());
        }
    }

    public void testDefaultIndex() throws IntrospectionException {
        Introspector
                .setBeanInfoSearchPath(new String[] { "org.apache.harmony.beans.tests.support" });

        BeanInfo dummyInfo = Introspector.getBeanInfo(FakeFox031.class);
        assertEquals(-1, dummyInfo.getDefaultPropertyIndex());
        assertEquals(-1, dummyInfo.getDefaultEventIndex());
    }

    static class MockBaseClassForPorpertiesStandard {
        int a = 0;

        int b = 1;
    }

    static class MockSubClassForPorpertiesStandard extends
            MockBaseClassForPorpertiesStandard {
        int a = 2;

        int b = 3;

        public int getName() {
            return a;
        }

        public void setName(int i) {
            a = i;
        }
    }

    static class MockClassForDefaultEvent {
        public void addPropertyChangeListener(PropertyChangeListener a) {
        }

        public void removePropertyChangeListener(PropertyChangeListener a) {
        }
    }
    static class MockBaseClassForPorperties {
        int a = 0;

        int b = 1;
    }

    static class MockSubClassForPorperties extends MockBaseClassForPorperties {
        int a = 2;

        int b = 3;

        int c = 3;

        public int getName() {
            return a;
        }

        public void setName(int i) {
            a = i;
        }
    }

//    public void testGetIcon() throws IntrospectionException {
//        Class<MockSubClassForPorperties> beanClass = MockSubClassForPorperties.class;
//        BeanInfo info = Introspector.getBeanInfo(beanClass);
//        assertNotNull(info.getIcon(BeanInfo.ICON_COLOR_16x16));
//    }

    public static class MockBaseClassForPorpertiesBeanInfo extends
            SimpleBeanInfo {

        @Override
        public MethodDescriptor[] getMethodDescriptors() {
            MethodDescriptor md = null;
            try {
                Class<MockSubClassForPorperties> clz = MockSubClassForPorperties.class;
                Method m = clz.getMethod("getName", new Class[] {});
                md = new MethodDescriptor(m);
            } catch (Exception e) {

            }
            return new MethodDescriptor[] { md };
        }

        @Override
        public PropertyDescriptor[] getPropertyDescriptors() {
            PropertyDescriptor[] pds = new PropertyDescriptor[2];
            Class<MockSubClassForPorperties> clazz = MockSubClassForPorperties.class;
            try {
                Method getter = clazz.getMethod("getName");
                Method setter = clazz.getMethod("setName", Integer.TYPE);
                pds[0] = new PropertyDescriptor("a", getter, setter);
                pds[0].setConstrained(true);
                pds[0].setBound(true);
                pds[0].setExpert(true);
                pds[0].setHidden(true);
                pds[1] = new PropertyDescriptor("b", getter, setter);
            } catch (IntrospectionException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

            return pds;
        }

//        public Image getIcon(int iconKind) {
//            return null;
//        }
    }

    public static class MockSubClassForPorpertiesBeanInfo extends
            SimpleBeanInfo {

        @Override
        public MethodDescriptor[] getMethodDescriptors() {
            MethodDescriptor md = null;
            try {
                Class<MockSubClassForPorperties> clz = MockSubClassForPorperties.class;
                Method m = clz.getMethod("getName", new Class[] {});
                md = new MethodDescriptor(m);
            } catch (Exception e) {

            }
            return new MethodDescriptor[] { md };
        }

        @Override
        public PropertyDescriptor[] getPropertyDescriptors() {
            PropertyDescriptor[] pds = new PropertyDescriptor[2];
            Class<MockSubClassForPorperties> clazz = MockSubClassForPorperties.class;
            try {
                Method getter = clazz.getMethod("getName");
                Method setter = clazz.getMethod("setName", Integer.TYPE);
                pds[0] = new PropertyDescriptor("a", getter, setter);
                pds[1] = new PropertyDescriptor("b", getter, setter);
            } catch (IntrospectionException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return pds;
        }

//        public Image getIcon(int iconKind) {
//            return new BufferedImage(16, 16, 1);
//        }

    }

    /*
     * Regression test for HARMONY-4892
     */
    public static class MyBean {

        public static String invisble;

        public static String getInvisible() {
            return invisble;
        }

        public String visible;

        public String getVisible() {
            return visible;
        }

        public void setVisible(String a) {
            this.visible = a;
        }
    }

    public void testPropertyDescriptors() throws IntrospectionException {
        BeanInfo info = Introspector.getBeanInfo(MyBean.class);
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            assertFalse(pd.getName().equals("invisible"));
        }
    }

    /*
     * Introspector Mixed Testing Begin
     */
    private String propertyName = "list";

    public class MixedSimpleClass1 {

        public Object isList(int index) {
            return null;
        }

        public Object isList() {
            return null;
        }
    }

    public void test_MixedSimpleClass1() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass1.class);
        Method getter = MixedSimpleClass1.class.getDeclaredMethod("isList");

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
            }
        }
    }

    public class MixedSimpleClass2 {

        public Object isList(int index) {
            return null;
        }

        public Object getList() {
            return null;
        }

    }

    public void test_MixedSimpleClass2() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass2.class);
        Method getter = MixedSimpleClass2.class.getDeclaredMethod("getList");

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
            }
        }
    }

    public class MixedSimpleClass3 {

        public Object getList(int index) {
            return null;
        }

        public Object isList() {
            return null;
        }

    }

    public void test_MixedSimpleClass3() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass3.class);
        Method getter = MixedSimpleClass3.class.getDeclaredMethod("getList",
                int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass4 {

        public Object getList(int index) {
            return null;
        }

        public Object getList() {
            return null;
        }

    }

    public void test_MixedSimpleClass4() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass4.class);
        Method getter = MixedSimpleClass4.class.getDeclaredMethod("getList",
                int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass5 {
        public Object getList(int index) {
            return null;
        }

        public Object getList() {
            return null;
        }

        public void setList(Object obj) {

        }
    }

    public void test_MixedSimpleClass5() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass5.class);
        Method getter = MixedSimpleClass5.class.getDeclaredMethod("getList");
        Method setter = MixedSimpleClass5.class.getDeclaredMethod("setList",
                Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedSimpleClass6 {
        public Object getList(int index) {
            return null;
        }

        public Object isList() {
            return null;
        }

        public void setList(Object obj) {

        }
    }

    public void test_MixedSimpleClass6() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass6.class);
        Method getter = MixedSimpleClass6.class.getDeclaredMethod("getList",
                int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass7 {
        public Object isList(int index) {
            return null;
        }

        public Object getList() {
            return null;
        }

        public void setList(Object obj) {

        }
    }

    public void test_MixedSimpleClass7() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass7.class);
        Method getter = MixedSimpleClass7.class.getDeclaredMethod("getList");
        Method setter = MixedSimpleClass7.class.getDeclaredMethod("setList",
                Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedSimpleClass8 {
        public Object isList(int index) {
            return null;
        }

        public Object isList() {
            return null;
        }

        public void setList(Object obj) {

        }
    }

    public void test_MixedSimpleClass8() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass8.class);
        Method setter = MixedSimpleClass8.class.getDeclaredMethod("setList",
                Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedSimpleClass9 {
        public Object isList(int index) {
            return null;
        }

        public Object isList() {
            return null;
        }

        public void setList(int index, Object obj) {

        }
    }

    public void test_MixedSimpleClass9() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass9.class);
        Method setter = MixedSimpleClass9.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass10 {
        public Object isList(int index) {
            return null;
        }

        public Object getList() {
            return null;
        }

        public void setList(int index, Object obj) {

        }
    }

    public void test_MixedSimpleClass10() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass10.class);
        Method setter = MixedSimpleClass10.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass11 {
        public Object getList(int index) {
            return null;
        }

        public Object isList() {
            return null;
        }

        public void setList(int index, Object obj) {

        }
    }

    public void test_MixedSimpleClass11() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass11.class);
        Method getter = MixedSimpleClass11.class.getDeclaredMethod("getList",
                int.class);
        Method setter = MixedSimpleClass11.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass12 {
        public Object getList(int index) {
            return null;
        }

        public Object getList() {
            return null;
        }

        public void setList(int index, Object obj) {

        }
    }

    public void test_MixedSimpleClass12() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass12.class);
        Method getter = MixedSimpleClass12.class.getDeclaredMethod("getList",
                int.class);
        Method setter = MixedSimpleClass12.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass13 {
        public Object getList(int index) {
            return null;
        }

        public Object getList() {
            return null;
        }

        public void setList(int index, Object obj) {

        }

        public void setList(Object obj) {

        }
    }

    public void test_MixedSimpleClass13() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass13.class);
        Method getter = MixedSimpleClass13.class.getDeclaredMethod("getList",
                int.class);
        Method setter = MixedSimpleClass13.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass14 {
        public Object getList(int index) {
            return null;
        }

        public Object isList() {
            return null;
        }

        public void setList(int index, Object obj) {

        }

        public void setList(Object obj) {

        }
    }

    public void test_MixedSimpleClass14() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass14.class);
        Method getter = MixedSimpleClass14.class.getDeclaredMethod("getList",
                int.class);
        Method setter = MixedSimpleClass14.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass15 {
        public Object isList(int index) {
            return null;
        }

        public Object getList() {
            return null;
        }

        public void setList(int index, Object obj) {

        }

        public void setList(Object obj) {

        }
    }

    public void test_MixedSimpleClass15() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass15.class);
        Method getter = MixedSimpleClass15.class.getDeclaredMethod("getList");
        Method setter = MixedSimpleClass15.class.getDeclaredMethod("setList",
                Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedSimpleClass16 {
        public Object isList(int index) {
            return null;
        }

        public Object isList() {
            return null;
        }

        public void setList(int index, Object obj) {

        }

        public void setList(Object obj) {

        }
    }

    public void test_MixedSimpleClass16() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass16.class);
        Method setter = MixedSimpleClass16.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass17 {
        public Object getList() {
            return null;
        }

        public void setList(int index, Object obj) {

        }

        public void setList(Object obj) {

        }
    }

    public void test_MixedSimpleClass17() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass17.class);
        Method getter = MixedSimpleClass17.class.getDeclaredMethod("getList");
        Method setter = MixedSimpleClass17.class.getDeclaredMethod("setList",
                Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedSimpleClass18 {
        public Object isList() {
            return null;
        }

        public void setList(int index, Object obj) {

        }

        public void setList(Object obj) {

        }
    }

    public void test_MixedSimpleClass18() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass18.class);
        Method setter = MixedSimpleClass18.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass19 {
        public Object getList(int index) {
            return null;
        }

        public void setList(Object obj) {

        }

        public void setList(int index, Object obj) {

        }
    }

    public void test_MixedSimpleClass19() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass19.class);
        Method getter = MixedSimpleClass19.class.getDeclaredMethod("getList",
                int.class);
        Method setter = MixedSimpleClass19.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass20 {
        public Object isList(int index) {
            return null;
        }

        public void setList(Object obj) {

        }

        public void setList(int index, Object obj) {

        }
    }

    public void test_MixedSimpleClass20() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass20.class);
        Method setter = MixedSimpleClass20.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass21 {
        public Object getList(int index) {
            return null;
        }

        public void setList(Object obj) {
        }
    }

    public void test_MixedSimpleClass21() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass21.class);
        Method getter = MixedSimpleClass21.class.getDeclaredMethod("getList",
                int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass22 {
        public Object isList(int index) {
            return null;
        }

        public void setList(Object obj) {
        }
    }

    public void test_MixedSimpleClass22() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass22.class);
        Method setter = MixedSimpleClass22.class.getDeclaredMethod("setList",
                Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedSimpleClass23 {
        public Object getList() {
            return null;
        }

        public void setList(int index, Object obj) {
        }
    }

    public void test_MixedSimpleClass23() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass23.class);
        Method setter = MixedSimpleClass23.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass24 {
        public Object isList() {
            return null;
        }

        public void setList(int index, Object obj) {
        }
    }

    public void test_MixedSimpleClass24() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass24.class);
        Method setter = MixedSimpleClass24.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass25 {
        public void setList(Object obj) {

        }

        public void setList(int index, Object obj) {

        }
    }

    public void test_MixedSimpleClass25() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass25.class);
        Method setter = MixedSimpleClass25.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass26 {

        public Object[] getList() {
            return null;
        }

        public Object getList(int i) {
            return null;
        }
    }

    public void test_MixedSimpleClass26() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass26.class);
        Method normalGetter = MixedSimpleClass26.class
                .getDeclaredMethod("getList");
        Method indexedGetter = MixedSimpleClass26.class.getDeclaredMethod(
                "getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertEquals(normalGetter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(indexedGetter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());

            }
        }
    }

    public class MixedSimpleClass27 {

        public Object[] isList() {
            return null;
        }

        public Object getList(int i) {
            return null;
        }
    }

    public void test_MixedSimpleClass27() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass27.class);
        Method indexedGetter = MixedSimpleClass27.class.getDeclaredMethod(
                "getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(indexedGetter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());

            }
        }
    }

    public class MixedSimpleClass28 {

        public Object[] getList() {
            return null;
        }

        public Object isList(int i) {
            return null;
        }
    }

    public void test_MixedSimpleClass28() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass28.class);
        Method getter = MixedSimpleClass28.class.getDeclaredMethod("getList");

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
            }
        }
    }

    public class MixedSimpleClass29 {

        public Object[] isList() {
            return null;
        }

        public Object isList(int i) {
            return null;
        }
    }

    public void test_MixedSimpleClass29() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass29.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            assertFalse(propertyName.equals(pd.getName()));
        }
    }

    public class MixedSimpleClass30 {

        public Object getList() {
            return null;
        }

        public Object[] getList(int i) {
            return null;
        }
    }

    public void test_MixedSimpleClass30() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass30.class);
        Method indexedGetter = MixedSimpleClass30.class.getDeclaredMethod(
                "getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(indexedGetter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());

            }
        }
    }

    public class MixedSimpleClass31 {

        public Object isList() {
            return null;
        }

        public Object[] getList(int i) {
            return null;
        }
    }

    public void test_MixedSimpleClass31() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass31.class);
        Method indexedGetter = MixedSimpleClass31.class.getDeclaredMethod(
                "getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(indexedGetter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());

            }
        }
    }

    public class MixedSimpleClass32 {

        public Object getList() {
            return null;
        }

        public Object[] isList(int i) {
            return null;
        }
    }

    public void test_MixedSimpleClass32() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass32.class);
        Method getter = MixedSimpleClass32.class.getDeclaredMethod("getList");

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
            }
        }
    }

    public class MixedSimpleClass33 {

        public Object isList() {
            return null;
        }

        public Object[] isList(int i) {
            return null;
        }
    }

    public void test_MixedSimpleClass33() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass33.class);
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            assertFalse(propertyName.equals(pd.getName()));
        }
    }

    public class MixedSimpleClass34 {
        public Object[] getList() {
            return null;
        }

        public Object[] getList(int index) {
            return null;
        }
    }

    public void test_MixedSimpleClass34() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass34.class);
        Method indexedGetter = MixedSimpleClass34.class.getDeclaredMethod(
                "getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(indexedGetter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());

            }
        }
    }

    public class MixedSimpleClass35 {
        public Object[] isList() {
            return null;
        }

        public Object[] getList(int index) {
            return null;
        }
    }

    public void test_MixedSimpleClass35() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass35.class);
        Method indexedGetter = MixedSimpleClass35.class.getDeclaredMethod(
                "getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(indexedGetter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());

            }
        }
    }

    public class MixedSimpleClass36 {
        public Object[] getList() {
            return null;
        }

        public Object[] isList(int index) {
            return null;
        }
    }

    public void test_MixedSimpleClass36() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass36.class);
        Method normalGetter = MixedSimpleClass36.class
                .getDeclaredMethod("getList");

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(normalGetter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
            }
        }
    }

    public class MixedSimpleClass37 {
        public Object[] isList() {
            return null;
        }

        public Object[] isList(int index) {
            return null;
        }
    }

    public void test_MixedSimpleClass37() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass37.class);
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            assertFalse(propertyName.equals(pd.getName()));
        }
    }

    public class MixedSimpleClass38 {
        public Object[][] getList() {
            return null;
        }

        public Object[] getList(int index) {
            return null;
        }
    }

    public void test_MixedSimpleClass38() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass38.class);
        Method normalGetter = MixedSimpleClass38.class
                .getDeclaredMethod("getList");
        Method indexedGetter = MixedSimpleClass38.class.getDeclaredMethod(
                "getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertEquals(normalGetter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(indexedGetter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());

            }
        }
    }

    public class MixedSimpleClass39 {
        public boolean isList(int index) {
            return false;
        }
    }

    public void test_MixedSimpleClass39() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass39.class);
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            assertFalse(propertyName.equals(pd.getName()));
        }
    }

    public class MixedSimpleClass40 {
        public Object isList() {
            return null;
        }
    }

    public void test_MixedSimpleClass40() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass40.class);
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            assertFalse(propertyName.equals(pd.getName()));
        }
    }

    public class MixedSimpleClass41 {
        public Object getList() {
            return null;
        }

        public void setList(Object obj) {
        }
    }

    public void test_MixedSimpleClass41() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass41.class);
        Method getter = MixedSimpleClass41.class.getDeclaredMethod("getList");
        Method setter = MixedSimpleClass41.class.getDeclaredMethod("setList",
                Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedSimpleClass42 {
        public Object isList() {
            return null;
        }

        public void setList(Object obj) {
        }
    }

    public void test_MixedSimpleClass42() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass42.class);
        Method setter = MixedSimpleClass42.class.getDeclaredMethod("setList",
                Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedSimpleClass43 {
        public Object getList() {
            return null;
        }
    }

    public void test_MixedSimpleClass43() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass43.class);
        Method getter = MixedSimpleClass43.class.getDeclaredMethod("getList");
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
            }
        }
    }

    public class MixedSimpleClass44 {
        public void setList(Object obj) {

        }
    }

    public void test_MixedSimpleClass44() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedSimpleClass44.class);
        Method setter = MixedSimpleClass44.class.getDeclaredMethod("setList",
                Object.class);
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedSimpleClass45 {
        public boolean isList(int index) {
            return true;
        }
    }

    public void test_MixedSimpleClass45() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass45.class);
        assertEquals(1, beanInfo.getPropertyDescriptors().length);
    }

    public class MixedSimpleClass46 {
        public boolean getList() {
            return true;
        }

        public boolean isList(int index) {
            return true;
        }
    }

    public void test_MixedSimpleClass46() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass46.class);
        Method getter = MixedSimpleClass46.class.getMethod("getList",
                new Class<?>[] {});
        assertEquals(2, beanInfo.getPropertyDescriptors().length);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertFalse(pd instanceof IndexedPropertyDescriptor);
            }
        }
    }

    public class MixedSimpleClass47 {
        public boolean isList() {
            return true;
        }

        public boolean isList(int index) {
            return true;
        }
    }

    public void test_MixedSimpleClass47() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass47.class);
        Method getter = MixedSimpleClass47.class.getMethod("isList",
                new Class<?>[] {});
        assertEquals(2, beanInfo.getPropertyDescriptors().length);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertFalse(pd instanceof IndexedPropertyDescriptor);
            }
        }
    }

    public class MixedSimpleClass48 {
        public boolean getList(int index) {
            return true;
        }

        public boolean isList(int index) {
            return true;
        }
    }

    public void test_MixedSimpleClass48() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass48.class);
        Method getter = MixedSimpleClass48.class.getMethod("getList",
                new Class<?>[] { int.class });
        assertEquals(2, beanInfo.getPropertyDescriptors().length);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter,
                        ((IndexedPropertyDescriptor) pd).getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass49 {
        public boolean isList(int index) {
            return true;
        }

        public void setList(boolean bool) {
        }
    }

    public void test_MixedSimpleClass49() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass49.class);
        Method setter = MixedSimpleClass49.class.getMethod("setList",
                new Class<?>[] { boolean.class });
        assertEquals(2, beanInfo.getPropertyDescriptors().length);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertNull(pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
                assertFalse(pd instanceof IndexedPropertyDescriptor);
            }
        }
    }

    public class MixedSimpleClass50 {
        public boolean isList(int index) {
            return true;
        }

        public void setList(int index, boolean bool) {
        }
    }

    public void test_MixedSimpleClass50() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass50.class);
        Method setter = MixedSimpleClass50.class.getMethod("setList",
                new Class<?>[] { int.class, boolean.class });
        assertEquals(2, beanInfo.getPropertyDescriptors().length);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter,
                        ((IndexedPropertyDescriptor) pd)
                                .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass51 {
        public boolean getList() {
            return true;
        }

        public boolean isList(int index) {
            return true;
        }

        public void setList(boolean bool) {
        }
    }

    public void test_MixedSimpleClass51() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass51.class);
        Method getter = MixedSimpleClass51.class.getMethod("getList",
                new Class<?>[] {});
        Method setter = MixedSimpleClass51.class.getMethod("setList",
                new Class<?>[] { boolean.class });
        assertEquals(2, beanInfo.getPropertyDescriptors().length);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
                assertFalse(pd instanceof IndexedPropertyDescriptor);
            }
        }
    }

    public class MixedSimpleClass52 {
        public boolean getList() {
            return true;
        }

        public boolean isList(int index) {
            return true;
        }

        public void setList(int index, boolean bool) {
        }
    }

    public void test_MixedSimpleClass52() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass52.class);
        Method setter = MixedSimpleClass52.class.getMethod("setList",
                new Class<?>[] { int.class, boolean.class });
        assertEquals(2, beanInfo.getPropertyDescriptors().length);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter,
                        ((IndexedPropertyDescriptor) pd)
                                .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass53 {
        public boolean isList() {
            return true;
        }

        public boolean isList(int index) {
            return true;
        }

        public void setList(boolean bool) {
        }
    }

    public void test_MixedSimpleClass53() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass53.class);
        Method getter = MixedSimpleClass53.class.getMethod("isList",
                new Class<?>[] {});
        Method setter = MixedSimpleClass53.class.getMethod("setList",
                new Class<?>[] { boolean.class });
        assertEquals(2, beanInfo.getPropertyDescriptors().length);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
                assertFalse(pd instanceof IndexedPropertyDescriptor);
            }
        }
    }

    public class MixedSimpleClass54 {
        public boolean isList() {
            return true;
        }

        public boolean isList(int index) {
            return true;
        }

        public void setList(int index, boolean bool) {
        }
    }

    public void test_MixedSimpleClass54() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass54.class);
        Method setter = MixedSimpleClass54.class.getMethod("setList",
                new Class<?>[] { int.class, boolean.class });
        assertEquals(2, beanInfo.getPropertyDescriptors().length);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter,
                        ((IndexedPropertyDescriptor) pd)
                                .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass55 {
        public boolean getList(int index) {
            return true;
        }

        public boolean isList(int index) {
            return true;
        }

        public void setList(boolean bool) {
        }
    }

    public void test_MixedSimpleClass55() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass55.class);
        Method getter = MixedSimpleClass55.class.getMethod("getList",
                new Class<?>[] { int.class });
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter,
                        ((IndexedPropertyDescriptor) pd).getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass56 {
        public boolean getList(int index) {
            return true;
        }

        public boolean isList(int index) {
            return true;
        }

        public void setList(int index, boolean bool) {
        }
    }

    public void test_MixedSimpleClass56() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass56.class);
        Method getter = MixedSimpleClass56.class.getMethod("getList",
                new Class<?>[] { int.class });
        Method setter = MixedSimpleClass56.class.getMethod("setList",
                new Class<?>[] { int.class, boolean.class });
        assertEquals(2, beanInfo.getPropertyDescriptors().length);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter,
                        ((IndexedPropertyDescriptor) pd).getIndexedReadMethod());
                assertEquals(setter,
                        ((IndexedPropertyDescriptor) pd)
                                .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass57 {
        public boolean isList(int index) {
            return true;
        }

        public void setList(boolean bool) {

        }

        public void setList(int index, boolean bool) {
        }
    }

    public void test_MixedSimpleClass57() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass57.class);
        Method setter = MixedSimpleClass57.class.getMethod("setList",
                new Class<?>[] { int.class, boolean.class });
        assertEquals(2, beanInfo.getPropertyDescriptors().length);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter,
                        ((IndexedPropertyDescriptor) pd)
                                .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass58 {
        public boolean getList() {
            return true;
        }

        public boolean isList(int index) {
            return true;
        }

        public void setList(boolean bool) {
        }

        public void setList(int index, boolean bool) {

        }
    }

    public void test_MixedSimpleClass58() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass58.class);
        Method getter = MixedSimpleClass58.class.getMethod("getList",
                new Class<?>[] {});
        Method setter = MixedSimpleClass58.class.getMethod("setList",
                new Class<?>[] { boolean.class });
        assertEquals(2, beanInfo.getPropertyDescriptors().length);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
                assertFalse(pd instanceof IndexedPropertyDescriptor);
            }
        }
    }

    public class MixedSimpleClass59 {
        public boolean isList() {
            return true;
        }

        public boolean isList(int index) {
            return true;
        }

        public void setList(boolean bool) {
        }

        public void setList(int index, boolean bool) {

        }
    }

    public void test_MixedSimpleClass59() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass59.class);
        Method getter = MixedSimpleClass59.class.getMethod("isList",
                new Class<?>[] {});
        Method setter = MixedSimpleClass59.class.getMethod("setList",
                new Class<?>[] { boolean.class });
        assertEquals(2, beanInfo.getPropertyDescriptors().length);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
                assertFalse(pd instanceof IndexedPropertyDescriptor);
            }
        }
    }

    public class MixedSimpleClass60 {
        public boolean getList(int index) {
            return true;
        }

        public boolean isList(int index) {
            return true;
        }

        public void setList(boolean bool) {
        }

        public void setList(int index, boolean bool) {

        }
    }

    public void test_MixedSimpleClass60() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass60.class);
        Method getter = MixedSimpleClass60.class.getMethod("getList",
                new Class<?>[] { int.class });
        Method setter = MixedSimpleClass60.class.getMethod("setList",
                new Class<?>[] { int.class, boolean.class });
        assertEquals(2, beanInfo.getPropertyDescriptors().length);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter,
                        ((IndexedPropertyDescriptor) pd).getIndexedReadMethod());
                assertEquals(setter,
                        ((IndexedPropertyDescriptor) pd)
                                .getIndexedWriteMethod());
            }
        }
    }

    public class MixedSimpleClass61 {
        public boolean getList() {
            return true;
        }

        public boolean getList(int index) {
            return true;
        }

        public boolean isList() {
            return true;
        }

        public boolean isList(int index) {
            return true;
        }

        public void setList(boolean bool) {
        }

        public void setList(int index, boolean bool) {

        }
    }

    public void test_MixedSimpleClass61() throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(MixedSimpleClass61.class);
        Method getter = MixedSimpleClass61.class.getMethod("getList",
                new Class<?>[] { int.class });
        Method setter = MixedSimpleClass61.class.getMethod("setList",
                new Class<?>[] { int.class, boolean.class });
        assertEquals(2, beanInfo.getPropertyDescriptors().length);
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter,
                        ((IndexedPropertyDescriptor) pd).getIndexedReadMethod());
                assertEquals(setter,
                        ((IndexedPropertyDescriptor) pd)
                                .getIndexedWriteMethod());
            }
        }
    }

    public class MixedExtendClass1 extends MixedSimpleClass4 {
        public void setList(Object a) {

        }
    }

    public void test_MixedExtendClass1() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedExtendClass1.class);
        Method getter = MixedSimpleClass4.class.getDeclaredMethod("getList");
        Method setter = MixedExtendClass1.class.getDeclaredMethod("setList",
                Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
                break;
            }
        }
    }

    public class MixedExtendClass2 extends MixedSimpleClass4 {
        public void setList(int index, Object a) {

        }
    }

    public void test_MixedExtendClass2() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedExtendClass2.class);
        Method getter = MixedSimpleClass4.class.getDeclaredMethod("getList",
                int.class);
        Method setter = MixedExtendClass2.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
                break;
            }
        }
    }

    public class MixedExtendClass3 extends MixedSimpleClass4 {
        public void setList(Object a) {

        }

        public void setList(int index, Object a) {

        }
    }

    public void test_MixedExtendClass3() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedExtendClass3.class);
        Method getter = MixedSimpleClass4.class.getDeclaredMethod("getList",
                int.class);
        Method setter = MixedExtendClass3.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
                break;
            }
        }
    }

    public class MixedExtendClass4 extends MixedSimpleClass4 {
        public Object getList() {
            return null;
        }
    }

    public void test_MixedExtendClass4() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedExtendClass4.class);
        Method getter = MixedExtendClass4.class.getDeclaredMethod("getList");

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                break;
            }
        }
    }

    public class MixedExtendClass5 extends MixedSimpleClass4 {
        public Object getList(int index) {
            return null;
        }
    }

    public void test_MixedExtendClass5() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedExtendClass5.class);
        Method getter = MixedExtendClass5.class.getDeclaredMethod("getList",
                int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
                break;
            }
        }
    }

    public class MixedExtendClass6 extends MixedSimpleClass25 {
        public Object getList() {
            return null;
        }
    }

    public void test_MixedExtendClass6() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedExtendClass6.class);
        Method getter = MixedExtendClass6.class.getDeclaredMethod("getList");
        Method setter = MixedSimpleClass25.class.getDeclaredMethod("setList",
                Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
                break;
            }
        }
    }

    public class MixedExtendClass7 extends MixedSimpleClass25 {
        public Object getList(int index) {
            return null;
        }
    }

    public void test_MixedExtendClass7() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedExtendClass7.class);
        Method getter = MixedExtendClass7.class.getDeclaredMethod("getList",
                int.class);
        Method setter = MixedSimpleClass25.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
                break;
            }
        }
    }

    public class MixedExtendClass8 extends MixedSimpleClass25 {
        public Object getList() {
            return null;
        }

        public Object getList(int index) {
            return null;
        }
    }

    public void test_MixedExtendClass8() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedExtendClass8.class);
        Method getter = MixedExtendClass8.class.getDeclaredMethod("getList",
                int.class);
        Method setter = MixedSimpleClass25.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
                break;
            }
        }
    }

    public class MixedExtendClass9 extends MixedSimpleClass25 {
        public void setList(Object obj) {
        }
    }

    public void test_MixedExtendClass9() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedExtendClass9.class);
        Method setter = MixedExtendClass9.class.getDeclaredMethod("setList",
                Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
                break;
            }
        }
    }

    public class MixedExtendClass10 extends MixedSimpleClass25 {
        public void setList(int index, Object obj) {
        }
    }

    public void test_MixedExtendClass10() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedExtendClass10.class);
        Method setter = MixedExtendClass10.class.getDeclaredMethod("setList",
                int.class, Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
                break;
            }
        }
    }

    public class MixedExtendClass11 extends MixedSimpleClass41 {
        public void setList(String obj) {
        }
    }

    public void test_MixedExtendClass11() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedExtendClass11.class);
        Method getter = MixedSimpleClass41.class.getDeclaredMethod("getList");
        Method setter = MixedSimpleClass41.class.getDeclaredMethod("setList",
                Object.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedExtendClass13 extends MixedSimpleClass42 {
        public void setList(String obj) {
        }
    }

    public void test_MixedExtendClass13() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedExtendClass13.class);
        Method setter = MixedExtendClass13.class.getDeclaredMethod("setList",
                String.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedExtendClass15 extends MixedSimpleClass44 {
        public void setList(String obj) {

        }
    }

    public void test_MixedExtendClass15() throws Exception {
        BeanInfo info = Introspector.getBeanInfo(MixedExtendClass15.class);
        Method setter = MixedExtendClass15.class.getDeclaredMethod("setList",
                String.class);
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass1 {

        public boolean isList(int index) {
            return false;
        }

        public boolean isList() {
            return false;
        }
    }

    public void test_MixedBooleanSimpleClass1() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass1.class);
        Method getter = MixedBooleanSimpleClass1.class
                .getDeclaredMethod("isList");

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass2 {

        public boolean isList(int index) {
            return false;
        }

        public boolean getList() {
            return false;
        }

    }

    public void test_MixedBooleanSimpleClass2() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass2.class);
        Method getter = MixedBooleanSimpleClass2.class
                .getDeclaredMethod("getList");

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass3 {

        public boolean getList(int index) {
            return false;
        }

        public boolean isList() {
            return false;
        }

    }

    public void test_MixedBooleanSimpleClass3() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass3.class);
        Method getter = MixedBooleanSimpleClass3.class.getDeclaredMethod(
                "getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass4 {
        public boolean getList(int index) {
            return false;
        }

        public boolean getList() {
            return false;
        }
    }

    public void test_MixedBooleanSimpleClass4() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass4.class);
        Method getter = MixedBooleanSimpleClass4.class.getDeclaredMethod(
                "getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass5 {

        public boolean isList(int index) {
            return false;
        }

        public boolean isList() {
            return false;
        }

        public void setList(boolean b) {

        }

    }

    public void test_MixedBooleanSimpleClass5() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass5.class);
        Method getter = MixedBooleanSimpleClass5.class
                .getDeclaredMethod("isList");
        Method setter = MixedBooleanSimpleClass5.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass6 {

        public boolean isList(int index) {
            return false;
        }

        public boolean getList() {
            return false;
        }

        public void setList(boolean b) {

        }

    }

    public void test_MixedBooleanSimpleClass6() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass6.class);
        Method getter = MixedBooleanSimpleClass6.class
                .getDeclaredMethod("getList");
        Method setter = MixedBooleanSimpleClass6.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass7 {

        public boolean getList(int index) {
            return false;
        }

        public boolean isList() {
            return false;
        }

        public void setList(boolean b) {

        }

    }

    public void test_MixedBooleanSimpleClass7() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass7.class);
        Method getter = MixedBooleanSimpleClass7.class
                .getDeclaredMethod("isList");
        Method setter = MixedBooleanSimpleClass7.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass8 {

        public boolean getList(int index) {
            return false;
        }

        public boolean getList() {
            return false;
        }

        public void setList(boolean b) {

        }

    }

    public void test_MixedBooleanSimpleClass8() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass8.class);
        Method getter = MixedBooleanSimpleClass8.class
                .getDeclaredMethod("getList");
        Method setter = MixedBooleanSimpleClass8.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass9 {

        public boolean getList(int index) {
            return false;
        }

        public boolean getList() {
            return false;
        }

        public void setList(int index, boolean b) {

        }

    }

    public void test_MixedBooleanSimpleClass9() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass9.class);
        Method getter = MixedBooleanSimpleClass9.class.getDeclaredMethod(
                "getList", int.class);
        Method setter = MixedBooleanSimpleClass9.class.getDeclaredMethod(
                "setList", int.class, boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass10 {

        public boolean getList(int index) {
            return false;
        }

        public boolean isList() {
            return false;
        }

        public void setList(int index, boolean b) {

        }

    }

    public void test_MixedBooleanSimpleClass10() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass10.class);
        Method getter = MixedBooleanSimpleClass10.class.getDeclaredMethod(
                "getList", int.class);
        Method setter = MixedBooleanSimpleClass10.class.getDeclaredMethod(
                "setList", int.class, boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass11 {

        public boolean isList(int index) {
            return false;
        }

        public boolean getList() {
            return false;
        }

        public void setList(int index, boolean b) {

        }

    }

    public void test_MixedBooleanSimpleClass11() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass11.class);
        Method setter = MixedBooleanSimpleClass11.class.getDeclaredMethod(
                "setList", int.class, boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass12 {

        public boolean isList(int index) {
            return false;
        }

        public boolean isList() {
            return false;
        }

        public void setList(int index, boolean b) {

        }

    }

    public void test_MixedBooleanSimpleClass12() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass12.class);
        Method setter = MixedBooleanSimpleClass12.class.getDeclaredMethod(
                "setList", int.class, boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass13 {

        public boolean isList(int index) {
            return false;
        }

        public boolean isList() {
            return false;
        }

        public void setList(boolean b) {

        }

        public void setList(int index, boolean b) {

        }

    }

    public void test_MixedBooleanSimpleClass13() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass13.class);
        Method getter = MixedBooleanSimpleClass13.class
                .getDeclaredMethod("isList");
        Method setter = MixedBooleanSimpleClass13.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass14 {

        public boolean isList(int index) {
            return false;
        }

        public boolean getList() {
            return false;
        }

        public void setList(boolean b) {

        }

        public void setList(int index, boolean b) {

        }
    }

    public void test_MixedBooleanSimpleClass14() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass14.class);
        Method getter = MixedBooleanSimpleClass14.class
                .getDeclaredMethod("getList");
        Method setter = MixedBooleanSimpleClass14.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass15 {

        public boolean getList(int index) {
            return false;
        }

        public boolean isList() {
            return false;
        }

        public void setList(boolean b) {

        }

        public void setList(int index, boolean b) {

        }
    }

    public void test_MixedBooleanSimpleClass15() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass15.class);
        Method getter = MixedBooleanSimpleClass15.class.getDeclaredMethod(
                "getList", int.class);
        Method setter = MixedBooleanSimpleClass15.class.getDeclaredMethod(
                "setList", int.class, boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass16 {

        public boolean getList(int index) {
            return false;
        }

        public boolean getList() {
            return false;
        }

        public void setList(boolean b) {

        }

        public void setList(int index, boolean b) {

        }
    }

    public void test_MixedBooleanSimpleClass16() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass16.class);
        Method getter = MixedBooleanSimpleClass16.class.getDeclaredMethod(
                "getList", int.class);
        Method setter = MixedBooleanSimpleClass16.class.getDeclaredMethod(
                "setList", int.class, boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass17 {
        public boolean getList() {
            return false;
        }

        public void setList(int index, boolean obj) {

        }

        public void setList(boolean obj) {

        }
    }

    public void test_MixedBooleanSimpleClass17() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass17.class);
        Method getter = MixedBooleanSimpleClass17.class
                .getDeclaredMethod("getList");
        Method setter = MixedBooleanSimpleClass17.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass18 {
        public boolean isList() {
            return false;
        }

        public void setList(int index, boolean obj) {

        }

        public void setList(boolean obj) {

        }
    }

    public void test_MixedBooleanSimpleClass18() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass18.class);
        Method getter = MixedBooleanSimpleClass18.class
                .getDeclaredMethod("isList");
        Method setter = MixedBooleanSimpleClass18.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass19 {

        public boolean getList(int index) {
            return false;
        }

        public void setList(boolean obj) {

        }

        public void setList(int index, boolean obj) {

        }
    }

    public void test_MixedBooleanSimpleClass19() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass19.class);
        Method getter = MixedBooleanSimpleClass19.class.getDeclaredMethod(
                "getList", int.class);
        Method setter = MixedBooleanSimpleClass19.class.getDeclaredMethod(
                "setList", int.class, boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass20 {
        public boolean isList(int index) {
            return false;
        }

        public void setList(boolean obj) {

        }

        public void setList(int index, boolean obj) {

        }
    }

    public void test_MixedBooleanSimpleClass20() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass20.class);
        Method setter = MixedBooleanSimpleClass20.class.getDeclaredMethod(
                "setList", int.class, boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass21 {
        public boolean getList(int index) {
            return false;
        }

        public void setList(boolean obj) {
        }
    }

    public void test_MixedBooleanSimpleClass21() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass21.class);
        Method getter = MixedBooleanSimpleClass21.class.getDeclaredMethod(
                "getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(getter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass22 {
        public boolean isList(int index) {
            return false;
        }

        public void setList(boolean obj) {
        }
    }

    public void test_MixedBooleanSimpleClass22() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass22.class);
        Method setter = MixedBooleanSimpleClass22.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass23 {
        public boolean getList() {
            return false;
        }

        public void setList(int index, boolean obj) {
        }
    }

    public void test_MixedBooleanSimpleClass23() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass23.class);
        Method setter = MixedBooleanSimpleClass23.class.getDeclaredMethod(
                "setList", int.class, boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass24 {
        public boolean isList() {
            return false;
        }

        public void setList(int index, boolean obj) {
        }
    }

    public void test_MixedBooleanSimpleClass24() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass24.class);
        Method setter = MixedBooleanSimpleClass24.class.getDeclaredMethod(
                "setList", int.class, boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass25 {
        public void setList(boolean obj) {

        }

        public void setList(int index, boolean obj) {

        }
    }

    public void test_MixedBooleanSimpleClass25() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass25.class);
        Method setter = MixedBooleanSimpleClass25.class.getDeclaredMethod(
                "setList", int.class, boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass26 {

        public boolean[] getList() {
            return null;
        }

        public boolean getList(int i) {
            return false;
        }
    }

    public void test_MixedBooleanSimpleClass26() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass26.class);
        Method normalGetter = MixedBooleanSimpleClass26.class
                .getDeclaredMethod("getList");
        Method indexedGetter = MixedBooleanSimpleClass26.class
                .getDeclaredMethod("getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertEquals(normalGetter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(indexedGetter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());

            }
        }
    }

    public class MixedBooleanSimpleClass27 {

        public boolean[] isList() {
            return null;
        }

        public boolean getList(int i) {
            return false;
        }
    }

    public void test_MixedBooleanSimpleClass27() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass27.class);
        Method indexedGetter = MixedBooleanSimpleClass27.class
                .getDeclaredMethod("getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(indexedGetter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());

            }
        }
    }

    public class MixedBooleanSimpleClass28 {

        public boolean[] getList() {
            return null;
        }

        public boolean isList(int i) {
            return false;
        }
    }

    public void test_MixedBooleanSimpleClass28() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass28.class);
        Method getter = MixedBooleanSimpleClass28.class
                .getDeclaredMethod("getList");

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass29 {

        public boolean[] isList() {
            return null;
        }

        public boolean isList(int i) {
            return false;
        }
    }

    public void test_MixedBooleanSimpleClass29() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass29.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            assertFalse(propertyName.equals(pd.getName()));
        }
    }

    public class MixedBooleanSimpleClass30 {

        public boolean getList() {
            return false;
        }

        public boolean[] getList(int i) {
            return null;
        }
    }

    public void test_MixedBooleanSimpleClass30() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass30.class);
        Method indexedGetter = MixedBooleanSimpleClass30.class
                .getDeclaredMethod("getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(indexedGetter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());

            }
        }
    }

    public class MixedBooleanSimpleClass31 {

        public boolean isList() {
            return false;
        }

        public boolean[] getList(int i) {
            return null;
        }
    }

    public void test_MixedBooleanSimpleClass31() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass31.class);
        Method indexedGetter = MixedBooleanSimpleClass31.class
                .getDeclaredMethod("getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(indexedGetter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());

            }
        }
    }

    public class MixedBooleanSimpleClass32 {

        public boolean getList() {
            return false;
        }

        public boolean[] isList(int i) {
            return null;
        }
    }

    public void test_MixedBooleanSimpleClass32() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass32.class);
        Method getter = MixedBooleanSimpleClass32.class
                .getDeclaredMethod("getList");

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass33 {

        public boolean isList() {
            return false;
        }

        public boolean[] isList(int i) {
            return null;
        }
    }

    public void test_MixedBooleanSimpleClass33() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass33.class);
        Method getter = MixedBooleanSimpleClass33.class
                .getDeclaredMethod("isList");

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass34 {
        public boolean[] getList() {
            return null;
        }

        public boolean[] getList(int index) {
            return null;
        }
    }

    public void test_MixedBooleanSimpleClass34() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass34.class);
        Method indexedGetter = MixedBooleanSimpleClass34.class
                .getDeclaredMethod("getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(indexedGetter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());

            }
        }
    }

    public class MixedBooleanSimpleClass35 {
        public boolean[] isList() {
            return null;
        }

        public boolean[] getList(int index) {
            return null;
        }
    }

    public void test_MixedBooleanSimpleClass35() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass35.class);
        Method indexedGetter = MixedBooleanSimpleClass35.class
                .getDeclaredMethod("getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(indexedGetter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());

            }
        }
    }

    public class MixedBooleanSimpleClass36 {
        public boolean[] getList() {
            return null;
        }

        public boolean[] isList(int index) {
            return null;
        }
    }

    public void test_MixedBooleanSimpleClass36() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass36.class);
        Method normalGetter = MixedBooleanSimpleClass36.class
                .getDeclaredMethod("getList");

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(normalGetter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass37 {
        public boolean[] isList() {
            return null;
        }

        public boolean[] isList(int index) {
            return null;
        }
    }

    public void test_MixedBooleanSimpleClass37() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass37.class);
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            assertFalse(propertyName.equals(pd.getName()));
        }
    }

    public class MixedBooleanSimpleClass38 {
        public boolean[][] getList() {
            return null;
        }

        public boolean[] getList(int index) {
            return null;
        }
    }

    public void test_MixedBooleanSimpleClass38() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass38.class);
        Method normalGetter = MixedBooleanSimpleClass38.class
                .getDeclaredMethod("getList");
        Method indexedGetter = MixedBooleanSimpleClass38.class
                .getDeclaredMethod("getList", int.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertEquals(normalGetter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertEquals(indexedGetter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());

            }
        }
    }

    public class MixedBooleanSimpleClass39 {
        public void setList(boolean a) {

        }
    }

    public void test_MixedBooleanSimpleClass39() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass39.class);
        Method setter = MixedBooleanSimpleClass39.class.getDeclaredMethod(
                "setList", boolean.class);
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
                break;
            }
        }
    }

    public class MixedBooleanSimpleClass40 {
        public void setList(int index, boolean a) {

        }
    }

    public void test_MixedBooleanSimpleClass40() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass40.class);
        Method setter = MixedBooleanSimpleClass40.class.getDeclaredMethod(
                "setList", int.class, boolean.class);
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
                break;
            }
        }
    }

    public class MixedBooleanSimpleClass41 {
        public boolean getList() {
            return false;
        }

        public void setList(boolean bool) {
        }
    }

    public void test_MixedBooleanSimpleClass41() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass41.class);
        Method getter = MixedBooleanSimpleClass41.class
                .getDeclaredMethod("getList");
        Method setter = MixedBooleanSimpleClass41.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass42 {
        public boolean isList() {
            return false;
        }

        public void setList(boolean bool) {
        }
    }

    public void test_MixedBooleanSimpleClass42() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass42.class);
        Method getter = MixedBooleanSimpleClass42.class
                .getDeclaredMethod("isList");
        Method setter = MixedBooleanSimpleClass42.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanSimpleClass43 {
        public boolean isList() {
            return false;
        }
    }

    public void test_MixedBooleanSimpleClass43() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanSimpleClass43.class);
        Method getter = MixedBooleanSimpleClass43.class
                .getDeclaredMethod("isList");
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanExtendClass1 extends MixedBooleanSimpleClass1 {
        public void setList(boolean a) {

        }
    }

    public void test_MixedBooleanExtendClass1() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanExtendClass1.class);
        Method getter = MixedBooleanSimpleClass1.class
                .getDeclaredMethod("isList");
        Method setter = MixedBooleanExtendClass1.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
                break;
            }
        }
    }

    public class MixedBooleanExtendClass2 extends MixedBooleanSimpleClass1 {
        public void setList(int index, boolean a) {

        }
    }

    public void test_MixedBooleanExtendClass2() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanExtendClass2.class);
        Method setter = MixedBooleanExtendClass2.class.getDeclaredMethod(
                "setList", int.class, boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
                break;
            }
        }
    }

    public class MixedBooleanExtendClass3 extends MixedBooleanSimpleClass1 {
        public void setList(boolean a) {

        }

        public void setList(int index, boolean a) {

        }
    }

    public void test_MixedBooleanExtendClass3() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanExtendClass3.class);
        Method getter = MixedBooleanSimpleClass1.class
                .getDeclaredMethod("isList");
        Method setter = MixedBooleanExtendClass3.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
                break;
            }
        }
    }

    public class MixedBooleanExtendClass4 extends MixedBooleanSimpleClass1 {
        public boolean isList() {
            return false;
        }
    }

    public void test_MixedBooleanExtendClass4() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanExtendClass4.class);
        Method getter = MixedBooleanSimpleClass1.class
                .getDeclaredMethod("isList");

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                break;
            }
        }
    }

    public class MixedBooleanExtendClass5 extends MixedBooleanSimpleClass1 {
        public boolean isList(int index) {
            return false;
        }
    }

    public void test_MixedBooleanExtendClass5() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanExtendClass5.class);
        Method getter = MixedBooleanSimpleClass1.class
                .getDeclaredMethod("isList");

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                break;
            }
        }
    }

    public class MixedBooleanExtendClass6 extends MixedBooleanSimpleClass25 {
        public boolean isList() {
            return false;
        }
    }

    public void test_MixedBooleanExtendClass6() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanExtendClass6.class);
        Method getter = MixedBooleanExtendClass6.class
                .getDeclaredMethod("isList");
        Method setter = MixedBooleanSimpleClass25.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
                break;
            }
        }
    }

    public class MixedBooleanExtendClass7 extends MixedBooleanSimpleClass25 {
        public boolean isList(int index) {
            return false;
        }
    }

    public void test_MixedBooleanExtendClass7() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanExtendClass7.class);
        Method setter = MixedBooleanSimpleClass25.class.getDeclaredMethod(
                "setList", int.class, boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
                break;
            }
        }
    }

    public class MixedBooleanExtendClass8 extends MixedBooleanSimpleClass25 {
        public boolean isList() {
            return false;
        }

        public boolean isList(int index) {
            return false;
        }
    }

    public void test_MixedBooleanExtendClass8() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanExtendClass8.class);
        Method getter = MixedBooleanExtendClass8.class
                .getDeclaredMethod("isList");
        Method setter = MixedBooleanSimpleClass25.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
                break;
            }
        }
    }

    public class MixedBooleanExtendClass9 extends MixedBooleanSimpleClass25 {
        public void setList(boolean obj) {
        }
    }

    public void test_MixedBooleanExtendClass9() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanExtendClass9.class);
        Method setter = MixedBooleanExtendClass9.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
                break;
            }
        }
    }

    public class MixedBooleanExtendClass10 extends MixedBooleanSimpleClass25 {
        public void setList(int index, boolean obj) {
        }
    }

    public void test_MixedBooleanExtendClass10() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanExtendClass10.class);
        Method setter = MixedBooleanExtendClass10.class.getDeclaredMethod(
                "setList", int.class, boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertTrue(pd instanceof IndexedPropertyDescriptor);
                assertNull(pd.getReadMethod());
                assertNull(pd.getWriteMethod());
                assertNull(((IndexedPropertyDescriptor) pd)
                        .getIndexedReadMethod());
                assertEquals(setter, ((IndexedPropertyDescriptor) pd)
                        .getIndexedWriteMethod());
                break;
            }
        }
    }

    public class MixedBooleanExtendClass11 extends MixedBooleanSimpleClass41 {
        public void setList(Object obj) {

        }
    }

    public void test_MixedBooleanExtendClass11() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanExtendClass11.class);
        Method getter = MixedBooleanSimpleClass41.class
                .getDeclaredMethod("getList");
        Method setter = MixedBooleanSimpleClass41.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanExtendClass12 extends MixedBooleanSimpleClass41 {
        public void setList(Boolean obj) {

        }
    }

    public void test_MixedBooleanExtendClass12() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanExtendClass12.class);
        Method getter = MixedBooleanSimpleClass41.class
                .getDeclaredMethod("getList");
        Method setter = MixedBooleanSimpleClass41.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanExtendClass13 extends MixedBooleanSimpleClass42 {
        public void setList(Object obj) {

        }
    }

    public void test_MixedBooleanExtendClass13() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanExtendClass13.class);
        Method getter = MixedBooleanSimpleClass42.class
                .getDeclaredMethod("isList");
        Method setter = MixedBooleanSimpleClass42.class.getDeclaredMethod(
                "setList", boolean.class);

        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertEquals(setter, pd.getWriteMethod());
            }
        }
    }

    public class MixedBooleanExtendClass14 extends MixedBooleanSimpleClass43 {
        public boolean isList() {
            return false;
        }
    }

    public void test_MixedBooleanExtendClass14() throws Exception {
        BeanInfo info = Introspector
                .getBeanInfo(MixedBooleanExtendClass14.class);
        Method getter = MixedBooleanSimpleClass43.class
                .getDeclaredMethod("isList");
        for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
            if (propertyName.equals(pd.getName())) {
                assertFalse(pd instanceof IndexedPropertyDescriptor);
                assertEquals(getter, pd.getReadMethod());
                assertNull(pd.getWriteMethod());
            }
        }
    }
    /*
     * Introspector Mixed Testing End
     */

}
