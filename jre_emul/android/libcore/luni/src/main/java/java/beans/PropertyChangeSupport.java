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

package java.beans;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import libcore.util.Objects;

/**
 * Manages a list of listeners to be notified when a property changes. Listeners
 * subscribe to be notified of all property changes, or of changes to a single
 * named property.
 *
 * <p>This class is thread safe. No locking is necessary when subscribing or
 * unsubscribing listeners, or when publishing events. Callers should be careful
 * when publishing events because listeners may not be thread safe.
 */
public class PropertyChangeSupport implements Serializable {

    private static final long serialVersionUID = 6401253773779951803l;
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("source", Object.class),
        new ObjectStreamField("children", Object.class),
        new ObjectStreamField("propertyChangeSupportSerializedDataVersion", int.class),
    };

    private transient Object sourceBean;

    /**
     * All listeners, including PropertyChangeListenerProxy listeners that are
     * only be notified when the assigned property is changed. This list may be
     * modified concurrently!
     */
    private transient List<PropertyChangeListener> listeners
            = new CopyOnWriteArrayList<PropertyChangeListener>();

    /**
     * Creates a new instance that uses the source bean as source for any event.
     *
     * @param sourceBean
     *            the bean used as source for all events.
     */
    public PropertyChangeSupport(Object sourceBean) {
        if (sourceBean == null) {
            throw new NullPointerException("sourceBean == null");
        }
        this.sourceBean = sourceBean;
    }

    /**
     * Fires a {@link PropertyChangeEvent} with the given name, old value and
     * new value. As source the bean used to initialize this instance is used.
     * If the old value and the new value are not null and equal the event will
     * not be fired.
     *
     * @param propertyName
     *            the name of the property
     * @param oldValue
     *            the old value of the property
     * @param newValue
     *            the new value of the property
     */
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        firePropertyChange(new PropertyChangeEvent(sourceBean, propertyName, oldValue, newValue));
    }

    /**
     * Fires an {@link IndexedPropertyChangeEvent} with the given name, old
     * value, new value and index. As source the bean used to initialize this
     * instance is used. If the old value and the new value are not null and
     * equal the event will not be fired.
     *
     * @param propertyName
     *            the name of the property
     * @param index
     *            the index
     * @param oldValue
     *            the old value of the property
     * @param newValue
     *            the new value of the property
     */
    public void fireIndexedPropertyChange(String propertyName, int index,
            Object oldValue, Object newValue) {
        firePropertyChange(new IndexedPropertyChangeEvent(sourceBean,
                propertyName, oldValue, newValue, index));
    }

    /**
     * Unsubscribes {@code listener} from change notifications for the property
     * named {@code propertyName}. If multiple subscriptions exist for {@code
     * listener}, it will receive one fewer notifications when the property
     * changes. If the property name or listener is null or not subscribed, this
     * method silently does nothing.
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        for (PropertyChangeListener p : listeners) {
            if (equals(propertyName, listener, p)) {
                listeners.remove(p);
                return;
            }
        }
    }

    /**
     * Returns true if two chains of PropertyChangeListenerProxies have the same
     * names in the same order and bottom out in the same event listener. This
     * method's signature is asymmetric to avoid allocating a proxy: if
     * non-null, {@code aName} represents the first property name and {@code a}
     * is its listener.
     */
    private boolean equals(String aName, EventListener a, EventListener b) {
        /*
         * Each iteration of the loop attempts to match a pair of property names
         * from a and b. If they don't match, the chains must not be equal!
         */
        while (b instanceof PropertyChangeListenerProxy) {
            PropertyChangeListenerProxy bProxy = (PropertyChangeListenerProxy) b; // unwrap b
            String bName = bProxy.getPropertyName();
            b = bProxy.getListener();
            if (aName == null) {
                if (!(a instanceof PropertyChangeListenerProxy)) {
                    return false;
                }
                PropertyChangeListenerProxy aProxy = (PropertyChangeListenerProxy) a; // unwrap a
                aName = aProxy.getPropertyName();
                a = aProxy.getListener();
            }
            if (!Objects.equal(aName, bName)) {
                return false; // not equal; a and b subscribe to different properties
            }
            aName = null;
        }
        return aName == null && Objects.equal(a, b);
    }

    /**
     * Subscribes {@code listener} to change notifications for the property
     * named {@code propertyName}. If the listener is already subscribed, it
     * will receive an additional notification when the property changes. If the
     * property name or listener is null, this method silently does nothing.
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        if (listener != null && propertyName != null) {
            listeners.add(new PropertyChangeListenerProxy(propertyName, listener));
        }
    }

    /**
     * Returns the subscribers to be notified when {@code propertyName} changes.
     * This includes both listeners subscribed to all property changes and
     * listeners subscribed to the named property only.
     */
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        List<PropertyChangeListener> result = new ArrayList<PropertyChangeListener>();
        for (PropertyChangeListener p : listeners) {
            if (p instanceof PropertyChangeListenerProxy && Objects.equal(
                    propertyName, ((PropertyChangeListenerProxy) p).getPropertyName())) {
                result.add(p);
            }
        }
        return result.toArray(new PropertyChangeListener[result.size()]);
    }

    /**
     * Fires a property change of a boolean property with the given name. If the
     * old value and the new value are not null and equal the event will not be
     * fired.
     *
     * @param propertyName
     *            the property name
     * @param oldValue
     *            the old value
     * @param newValue
     *            the new value
     */
    public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
        firePropertyChange(propertyName, Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
    }

    /**
     * Fires a property change of a boolean property with the given name. If the
     * old value and the new value are not null and equal the event will not be
     * fired.
     *
     * @param propertyName
     *            the property name
     * @param index
     *            the index of the changed property
     * @param oldValue
     *            the old value
     * @param newValue
     *            the new value
     */
    public void fireIndexedPropertyChange(String propertyName, int index,
            boolean oldValue, boolean newValue) {
        if (oldValue != newValue) {
            fireIndexedPropertyChange(propertyName, index,
                    Boolean.valueOf(oldValue), Boolean.valueOf(newValue));
        }
    }

    /**
     * Fires a property change of an integer property with the given name. If
     * the old value and the new value are not null and equal the event will not
     * be fired.
     *
     * @param propertyName
     *            the property name
     * @param oldValue
     *            the old value
     * @param newValue
     *            the new value
     */
    public void firePropertyChange(String propertyName, int oldValue, int newValue) {
        firePropertyChange(propertyName, Integer.valueOf(oldValue), Integer.valueOf(newValue));
    }

    /**
     * Fires a property change of an integer property with the given name. If
     * the old value and the new value are not null and equal the event will not
     * be fired.
     *
     * @param propertyName
     *            the property name
     * @param index
     *            the index of the changed property
     * @param oldValue
     *            the old value
     * @param newValue
     *            the new value
     */
    public void fireIndexedPropertyChange(String propertyName, int index,
            int oldValue, int newValue) {
        if (oldValue != newValue) {
            fireIndexedPropertyChange(propertyName, index,
                    Integer.valueOf(oldValue), Integer.valueOf(newValue));
        }
    }

    /**
     * Returns true if there are listeners registered to the property with the
     * given name.
     *
     * @param propertyName
     *            the name of the property
     * @return true if there are listeners registered to that property, false
     *         otherwise.
     */
    public boolean hasListeners(String propertyName) {
        for (PropertyChangeListener p : listeners) {
            if (!(p instanceof PropertyChangeListenerProxy) || Objects.equal(
                    propertyName, ((PropertyChangeListenerProxy) p).getPropertyName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Unsubscribes {@code listener} from change notifications for all
     * properties. If the listener has multiple subscriptions, it will receive
     * one fewer notification when properties change. If the property name or
     * listener is null or not subscribed, this method silently does nothing.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        for (PropertyChangeListener p : listeners) {
            if (equals(null, listener, p)) {
                listeners.remove(p);
                return;
            }
        }
    }

    /**
     * Subscribes {@code listener} to change notifications for all properties.
     * If the listener is already subscribed, it will receive an additional
     * notification. If the listener is null, this method silently does nothing.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Returns all subscribers. This includes both listeners subscribed to all
     * property changes and listeners subscribed to a single property.
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return listeners.toArray(new PropertyChangeListener[0]); // 0 to avoid synchronization
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        /*
         * The serialized form of this class uses PropertyChangeSupport to group
         * PropertyChangeListeners subscribed to the same property name.
         */
        Map<String, PropertyChangeSupport> map = new Hashtable<String, PropertyChangeSupport>();
        for (PropertyChangeListener p : listeners) {
            if (p instanceof PropertyChangeListenerProxy && !(p instanceof Serializable)) {
                PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) p;
                PropertyChangeListener listener = (PropertyChangeListener) proxy.getListener();
                if (listener instanceof Serializable) {
                    PropertyChangeSupport list = map.get(proxy.getPropertyName());
                    if (list == null) {
                        list = new PropertyChangeSupport(sourceBean);
                        map.put(proxy.getPropertyName(), list);
                    }
                    list.listeners.add(listener);
                }
            }
        }

        ObjectOutputStream.PutField putFields = out.putFields();
        putFields.put("source", sourceBean);
        putFields.put("children", map);
        out.writeFields();

        for (PropertyChangeListener p : listeners) {
            if (p instanceof Serializable) {
                out.writeObject(p);
            }
        }
        out.writeObject(null);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField readFields = in.readFields();
        sourceBean = readFields.get("source", null);
        listeners = new CopyOnWriteArrayList<PropertyChangeListener>();

        Map<String, PropertyChangeSupport> children
                = (Map<String, PropertyChangeSupport>) readFields.get("children", null);
        if (children != null) {
            for (Map.Entry<String, PropertyChangeSupport> entry : children.entrySet()) {
                for (PropertyChangeListener p : entry.getValue().listeners) {
                    listeners.add(new PropertyChangeListenerProxy(entry.getKey(), p));
                }
            }
        }

        PropertyChangeListener listener;
        while ((listener = (PropertyChangeListener) in.readObject()) != null) {
            listeners.add(listener);
        }
    }

    /**
     * Publishes a property change event to all listeners of that property. If
     * the event's old and new values are equal (but non-null), no event will be
     * published.
     */
    public void firePropertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        if (newValue != null && oldValue != null && newValue.equals(oldValue)) {
            return;
        }

        notifyEachListener:
        for (PropertyChangeListener p : listeners) {
            // unwrap listener proxies until we get a mismatched name or the real listener
            while (p instanceof PropertyChangeListenerProxy) {
                PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy) p;
                if (!Objects.equal(proxy.getPropertyName(), propertyName)) {
                    continue notifyEachListener;
                }
                p = (PropertyChangeListener) proxy.getListener();
            }
            p.propertyChange(event);
        }
    }
}
