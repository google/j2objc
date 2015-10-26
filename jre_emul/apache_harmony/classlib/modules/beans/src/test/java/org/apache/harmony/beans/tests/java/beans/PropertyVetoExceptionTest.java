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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.Serializable;

import junit.framework.TestCase;

import org.apache.harmony.beans.tests.support.mock.MockJavaBean;
import org.apache.harmony.testframework.serialization.SerializationTest;
import org.apache.harmony.testframework.serialization.SerializationTest.SerializableAssert;

/**
 * Unit test for class PropertyVetoException
 */
public class PropertyVetoExceptionTest extends TestCase {

    private PropertyChangeEvent event;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockJavaBean myBean = new MockJavaBean("Bean_PropertyVetoExceptionTest");
        event = new PropertyChangeEvent(myBean, "propertyOne", "value_old",
                "value_one");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPropertyVetoException() {
        String message = "testPropertyVetoException";
        PropertyVetoException e = new PropertyVetoException(message, event);
        assertSame(message, e.getMessage());
        assertSame(event, e.getPropertyChangeEvent());
    }

    public void testPropertyVetoException_MessageNull() {
        String message = null;
        PropertyVetoException e = new PropertyVetoException(message, event);
        assertNull(e.getMessage());
        assertSame(event, e.getPropertyChangeEvent());
    }

    public void testPropertyVetoException_EventNull() {
        String message = "testPropertyVetoException";
        PropertyVetoException e = new PropertyVetoException(message, null);
        assertSame(message, e.getMessage());
        assertNull(e.getPropertyChangeEvent());
    }

    // comparator for PropertyVetoException objects
    private static final SerializableAssert comparator = new SerializableAssert() {
        public void assertDeserialized(Serializable initial,
                Serializable deserialized) {

            SerializationTest.THROWABLE_COMPARATOR.assertDeserialized(initial,
                    deserialized);

            PropertyVetoException initEx = (PropertyVetoException) initial;
            PropertyVetoException desrEx = (PropertyVetoException) deserialized;

            assertNull(desrEx.getPropertyChangeEvent().getSource());

            // compare event objects
            PropertyChangeEventTest.comparator.assertDeserialized(initEx
                    .getPropertyChangeEvent(), desrEx.getPropertyChangeEvent());
        }
    };
}
