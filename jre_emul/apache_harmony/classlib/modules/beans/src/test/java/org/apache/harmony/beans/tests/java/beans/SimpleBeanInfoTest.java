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

import java.beans.SimpleBeanInfo;

import junit.framework.TestCase;


/**
 * Unit test for SimpleBeanInfo
 */
public class SimpleBeanInfoTest extends TestCase {

    public void testGetAdditionalBeanInfo() {
        SimpleBeanInfo info = new SimpleBeanInfo();

        assertNull(info.getAdditionalBeanInfo());
    }

    public void testGetBeanDescriptor() {
        SimpleBeanInfo info = new SimpleBeanInfo();

        assertNull(info.getBeanDescriptor());
    }

    public void testGetDefaultEventIndex() {
        SimpleBeanInfo info = new SimpleBeanInfo();

        assertEquals(-1, info.getDefaultEventIndex());
    }

    public void testGetDefaultPropertyIndex() {
        SimpleBeanInfo info = new SimpleBeanInfo();

        assertEquals(-1, info.getDefaultPropertyIndex());
    }

    public void testGetEventSetDescriptors() {
        SimpleBeanInfo info = new SimpleBeanInfo();

        assertNull(info.getEventSetDescriptors());
    }

//    public void testGetIcon() {
//        SimpleBeanInfo info = new SimpleBeanInfo();
//
//        assertNull(info.getIcon(BeanInfo.ICON_COLOR_16x16));
//    }

    public void testGetMethodDescriptors() {
        SimpleBeanInfo info = new SimpleBeanInfo();

        assertNull(info.getMethodDescriptors());
    }

    public void testGetPropertyDescriptors() {
        SimpleBeanInfo info = new SimpleBeanInfo();

        assertNull(info.getPropertyDescriptors());
    }

//    public void testLoadImage() {
//        SimpleBeanInfo info = new SimpleBeanInfo();
//        Image image;
//
//        image = info.loadImage("/gif/harmony-logo.gif");
//        assertNotNull(image);
//
//        image = info.loadImage("/gif/test.gif");
//        assertNotNull(image);
//
//        // regression for HARMONY-2241
//        info = new SimpleBeanInfo() {};
//        image = info.loadImage("testB.jpg");
//        assertNotNull(image);
//
//        info = new SimpleBeanInfo();
//        image = info.loadImage(null);
//        assertNull(image);
//    }

}
