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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;

import junit.framework.TestCase;

/**
 * Test for PropertyChangeListenerProxy
 */
public class PropertyChangeListenerProxyTest extends TestCase {

    PropertyChangeListenerProxy proxy;

    PropertyChangeListener listener = new MockPropertyChangeListener();

    String name = "mock";

    static PropertyChangeEvent event = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        proxy = new PropertyChangeListenerProxy(name, listener);
    }

    public void testPropertyChangeListenerProxy() {
        proxy = new PropertyChangeListenerProxy(null, listener);
        assertSame(listener, proxy.getListener());
        assertNull(proxy.getPropertyName());
        PropertyChangeEvent newevent = new PropertyChangeEvent(new Object(),
                "name", new Object(), new Object());
        proxy.propertyChange(newevent);
        assertSame(newevent, event);
        proxy = new PropertyChangeListenerProxy(name, null);
        assertSame(name, proxy.getPropertyName());
        assertNull(proxy.getListener());
        try {
            proxy.propertyChange(new PropertyChangeEvent(new Object(), "name",
                    new Object(), new Object()));
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }

        proxy = new PropertyChangeListenerProxy(name, listener);
        assertSame(listener, proxy.getListener());
        assertSame(name, proxy.getPropertyName());
        newevent = new PropertyChangeEvent(new Object(), "name", new Object(),
                new Object());
        assertSame(name, proxy.getPropertyName());
        proxy.propertyChange(newevent);
        assertSame(newevent, event);
    }

    public void testPropertyChange() {
        proxy.propertyChange(null);
        assertNull(event);
    }

    /**
     * Regression for HARMONY-407
     */
    public void testPropertyChange_PropertyChangeEvent() {
        PropertyChangeListenerProxy proxy = new PropertyChangeListenerProxy(
                "harmony", null);
        try {
            proxy.propertyChange(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }

    public static class MockPropertyChangeListener implements
            PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent newevent) {
            event = newevent;
        }
    }

}
