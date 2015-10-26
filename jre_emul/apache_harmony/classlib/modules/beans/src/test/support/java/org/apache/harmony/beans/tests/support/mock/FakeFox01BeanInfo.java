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

package org.apache.harmony.beans.tests.support.mock;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.lang.reflect.Method;

/**
 * The series of FakeFox classes are used to test
 * Introspector.getBeanInfo(Class, int)
 */
public class FakeFox01BeanInfo extends SimpleBeanInfo {

    Class<FakeFox01> clazz = FakeFox01.class;

    String suffix = "." + clazz.getName();

    @Override
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor beanDesc = new BeanDescriptor(clazz);
        beanDesc.setDisplayName(beanDesc.getDisplayName() + suffix);
        return beanDesc;
    }

    @Override
    public MethodDescriptor[] getMethodDescriptors() {
        Method get = null;
        Method set = null;
        try {
            get = clazz.getMethod("getFox01", (Class[]) null);
            set = clazz.getMethod("setFox01", new Class[] { String.class });
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        MethodDescriptor getDesc = new MethodDescriptor(get);
        getDesc.setDisplayName(getDesc.getDisplayName() + suffix);
        MethodDescriptor setDesc = new MethodDescriptor(set);
        setDesc.setDisplayName(setDesc.getDisplayName() + suffix);
        return new MethodDescriptor[] { setDesc, getDesc };
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor propertyDesc = null;
        try {
            propertyDesc = new PropertyDescriptor("fox01", clazz);
            propertyDesc.setDisplayName(propertyDesc.getDisplayName() + suffix);
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
        return new PropertyDescriptor[] { propertyDesc };
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.BeanInfo#getDefaultEventIndex()
     */
    @Override
    public int getDefaultEventIndex() {
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.BeanInfo#getDefaultPropertyIndex()
     */
    @Override
    public int getDefaultPropertyIndex() {
        // TODO Auto-generated method stub
        return 0;
    }
}
