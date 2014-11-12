/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package libcore.java.util.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import junit.framework.TestCase;
import libcore.util.SerializationTester;

public final class PropertyChangeSupportTest extends TestCase {

    public void testOldAndNewAreBothNull() {
        Object bean = new Object();
        PropertyChangeSupport support = new PropertyChangeSupport(bean);
        EventLog listener = new EventLog();
        support.addPropertyChangeListener(listener);

        PropertyChangeEvent nullToNull = new PropertyChangeEvent(bean, "a", null, null);
        support.firePropertyChange(nullToNull);
        assertEquals(Arrays.<PropertyChangeEvent>asList(nullToNull), listener.log);
    }

    public void testOldAndNewAreTheSame() {
        Object bean = new Object();
        PropertyChangeSupport support = new PropertyChangeSupport(bean);
        EventLog listener = new EventLog();
        support.addPropertyChangeListener(listener);

        PropertyChangeEvent xToX = new PropertyChangeEvent(bean, "a", "x", new String("x"));
        support.firePropertyChange(xToX);
        assertEquals(Arrays.<PropertyChangeEvent>asList(), listener.log);
    }

    public void testEventsFilteredByProxies() {
        Object bean = new Object();
        PropertyChangeEvent eventA = new PropertyChangeEvent(bean, "a", false, true);
        PropertyChangeEvent eventB = new PropertyChangeEvent(bean, "b", false, true);
        PropertyChangeEvent eventC = new PropertyChangeEvent(bean, "c", false, true);

        PropertyChangeSupport support = new PropertyChangeSupport(bean);

        EventLog all = new EventLog();
        support.addPropertyChangeListener(all);

        EventLog proxiedA = new EventLog();
        support.addPropertyChangeListener(new PropertyChangeListenerProxy("a", proxiedA));

        EventLog addA = new EventLog();
        support.addPropertyChangeListener("a", addA);

        EventLog addAProxiedB = new EventLog();
        support.addPropertyChangeListener("a", new PropertyChangeListenerProxy("b", addAProxiedB));

        EventLog proxiedAB = new EventLog();
        support.addPropertyChangeListener(new PropertyChangeListenerProxy(
                "a", new PropertyChangeListenerProxy("b", proxiedAB)));

        EventLog proxiedAA = new EventLog();
        support.addPropertyChangeListener(new PropertyChangeListenerProxy("a",
                new PropertyChangeListenerProxy("a", proxiedAA)));

        EventLog proxiedAAC = new EventLog();
        support.addPropertyChangeListener(new PropertyChangeListenerProxy("a",
                new PropertyChangeListenerProxy("a",
                new PropertyChangeListenerProxy("c", proxiedAAC))));

        support.firePropertyChange(eventA);
        support.firePropertyChange(eventB);
        support.firePropertyChange(eventC);

        assertEquals(Arrays.asList(eventA, eventB, eventC), all.log);
        assertEquals(Arrays.asList(eventA), proxiedA.log);
        assertEquals(Arrays.asList(eventA), addA.log);
        assertEquals(Arrays.<PropertyChangeEvent>asList(), addAProxiedB.log);
        assertEquals(Arrays.<PropertyChangeEvent>asList(), proxiedAB.log);
        assertEquals(Arrays.<PropertyChangeEvent>asList(eventA), proxiedAA.log);
        assertEquals(Arrays.<PropertyChangeEvent>asList(), proxiedAAC.log);
    }

    /**
     * Test that we need to do our own equals() work to manually unwrap an
     * arbitrary number of proxies.
     */
    public void testRemoveWithProxies() {
        Object bean = new Object();
        PropertyChangeSupport support = new PropertyChangeSupport(bean);

        EventLog all = new EventLog();
        support.addPropertyChangeListener(all);
        assertEquals(1, support.getPropertyChangeListeners().length);

        EventLog proxiedA = new EventLog();
        support.addPropertyChangeListener(new PropertyChangeListenerProxy("a", proxiedA));
        assertEquals(2, support.getPropertyChangeListeners().length);

        EventLog addA = new EventLog();
        support.addPropertyChangeListener("a", addA);
        assertEquals(3, support.getPropertyChangeListeners().length);

        EventLog addAProxiedB = new EventLog();
        support.addPropertyChangeListener("a", new PropertyChangeListenerProxy("b", addAProxiedB));
        assertEquals(4, support.getPropertyChangeListeners().length);

        EventLog proxiedAB = new EventLog();
        PropertyChangeListenerProxy proxyAB = new PropertyChangeListenerProxy(
                "a", new PropertyChangeListenerProxy("b", proxiedAB));
        support.addPropertyChangeListener(proxyAB);
        assertEquals(5, support.getPropertyChangeListeners().length);

        EventLog proxiedAAC = new EventLog();
        support.addPropertyChangeListener(new PropertyChangeListenerProxy("a",
                new PropertyChangeListenerProxy("a",
                        new PropertyChangeListenerProxy("c", proxiedAAC))));
        assertEquals(6, support.getPropertyChangeListeners().length);

        support.removePropertyChangeListener(all);
        assertEquals(5, support.getPropertyChangeListeners().length);
        support.removePropertyChangeListener("a", proxiedA);
        assertEquals(4, support.getPropertyChangeListeners().length);
        support.removePropertyChangeListener(new PropertyChangeListenerProxy("a", addA));
        assertEquals(3, support.getPropertyChangeListeners().length);
        support.removePropertyChangeListener(
                "a", new PropertyChangeListenerProxy("b", addAProxiedB));
        assertEquals(2, support.getPropertyChangeListeners().length);
        support.removePropertyChangeListener(proxyAB);
        assertEquals(1, support.getPropertyChangeListeners().length);

        support.removePropertyChangeListener(proxiedAAC);
        support.removePropertyChangeListener(new PropertyChangeListenerProxy("a", proxiedAAC));
        support.removePropertyChangeListener("a", new PropertyChangeListenerProxy("c", proxiedAAC));
        support.removePropertyChangeListener("a", new PropertyChangeListenerProxy("c",
                new PropertyChangeListenerProxy("a", proxiedAAC)));
        assertEquals(1, support.getPropertyChangeListeners().length);

        support.removePropertyChangeListener("a", new PropertyChangeListenerProxy("a",
                new PropertyChangeListenerProxy("c", proxiedAAC)));
        assertEquals(0, support.getPropertyChangeListeners().length);
    }

    public void testAddingOneListenerTwice() {
        Object bean = new Object();
        PropertyChangeSupport support = new PropertyChangeSupport(bean);
        EventLog log = new EventLog();
        support.addPropertyChangeListener("a", log);
        support.addPropertyChangeListener(log);
        support.addPropertyChangeListener(log);
        support.addPropertyChangeListener("a", log);

        PropertyChangeEvent eventA = new PropertyChangeEvent(bean, "a", false, true);
        PropertyChangeEvent eventB = new PropertyChangeEvent(bean, "b", false, true);

        support.firePropertyChange(eventA);
        support.firePropertyChange(eventB);

        assertEquals(Arrays.asList(eventA, eventA, eventA, eventA, eventB, eventB), log.log);
    }

    public void testAddingAListenerActuallyAddsAProxy() {
        Object bean = new Object();
        PropertyChangeSupport support = new PropertyChangeSupport(bean);
        PropertyChangeListener listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {}
        };
        support.addPropertyChangeListener("a", listener);

        PropertyChangeListenerProxy p1
                = (PropertyChangeListenerProxy) support.getPropertyChangeListeners()[0];
        assertEquals(PropertyChangeListenerProxy.class, p1.getClass());
        assertTrue(p1 != listener); // weird but consistent with the RI
        assertEquals("a", p1.getPropertyName());
        assertEquals(listener, p1.getListener());
    }

    public void testAddingAProxy() {
        Object bean = new Object();
        PropertyChangeSupport support = new PropertyChangeSupport(bean);
        PropertyChangeListener listener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {}
        };
        PropertyChangeListenerProxy proxy = new PropertyChangeListenerProxy("a", listener);
        support.addPropertyChangeListener("b", proxy);

        // this proxy sets us up to receive 'b' events
        PropertyChangeListenerProxy p1
                = (PropertyChangeListenerProxy) support.getPropertyChangeListeners()[0];
        assertEquals(PropertyChangeListenerProxy.class, p1.getClass());
        assertEquals("b", p1.getPropertyName());

        // this proxy sets us up to receive 'a' events
        PropertyChangeListenerProxy p2 = (PropertyChangeListenerProxy) p1.getListener();
        assertEquals(PropertyChangeListenerProxy.class, p2.getClass());
        assertEquals("a", p2.getPropertyName());
        assertEquals(listener, p2.getListener());
    }

    public void testSerialize() {
        String s = "aced0005737200206a6176612e6265616e732e50726f70657274794368616e67"
                + "65537570706f727458d5d264574860bb03000349002a70726f706572747943686"
                + "16e6765537570706f727453657269616c697a65644461746156657273696f6e4c"
                + "00086368696c6472656e7400154c6a6176612f7574696c2f486173687461626c6"
                + "53b4c0006736f757263657400124c6a6176612f6c616e672f4f626a6563743b78"
                + "7000000002737200136a6176612e7574696c2e486173687461626c6513bb0f252"
                + "14ae4b803000246000a6c6f6164466163746f724900097468726573686f6c6478"
                + "703f4000000000000877080000000b00000001740001617371007e00000000000"
                + "2707400046265616e7372003a6c6962636f72652e6a6176612e7574696c2e6265"
                + "616e732e50726f70657274794368616e6765537570706f7274546573742445766"
                + "56e744c6f67b92667637d0b6f450200024c00036c6f677400104c6a6176612f75"
                + "74696c2f4c6973743b4c00046e616d657400124c6a6176612f6c616e672f53747"
                + "2696e673b7870737200136a6176612e7574696c2e41727261794c6973747881d2"
                + "1d99c7619d03000149000473697a6578700000000077040000000a7874000b6c6"
                + "97374656e6572546f4171007e000c70787871007e00087371007e00097371007e"
                + "000d0000000077040000000a7874000d6c697374656e6572546f416c6c7078";

        Object bean = "bean";
        PropertyChangeSupport support = new PropertyChangeSupport(bean);
        EventLog listenerToAll = new EventLog();
        listenerToAll.name = "listenerToAll";
        EventLog listenerToA = new EventLog();
        listenerToA.name = "listenerToA";
        support.addPropertyChangeListener(listenerToAll);
        support.addPropertyChangeListener("a", listenerToA);
        support.addPropertyChangeListener("a", listenerToA);

        new SerializationTester<PropertyChangeSupport>(support, s) {
            @Override protected boolean equals(PropertyChangeSupport a, PropertyChangeSupport b) {
                return describe(a.getPropertyChangeListeners())
                        .equals(describe(b.getPropertyChangeListeners()));
            }
            @Override protected void verify(PropertyChangeSupport deserialized) {
                assertEquals("[a to listenerToA, a to listenerToA, listenerToAll]",
                        describe(deserialized.getPropertyChangeListeners()));
            }
        }.test();
    }

    private String describe(PropertyChangeListener[] listeners) {
        List<String> result = new ArrayList<String>();
        for (PropertyChangeListener listener : listeners) {
            result.add(describe(listener));
        }
        Collections.sort(result);
        return result.toString();
    }

    private String describe(EventListener listener) {
        if (listener instanceof PropertyChangeListenerProxy) {
            PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) listener;
            return proxy.getPropertyName() + " to " + describe(proxy.getListener());
        } else {
            return listener.toString();
        }
    }

    static class EventLog implements PropertyChangeListener, Serializable {
        String name = "EventLog";
        List<PropertyChangeEvent> log = new ArrayList<PropertyChangeEvent>();
        public void propertyChange(PropertyChangeEvent event) {
            log.add(event);
        }
        @Override public String toString() {
            return name;
        }
    }
}
