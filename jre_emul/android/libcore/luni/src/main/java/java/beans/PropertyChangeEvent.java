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

import java.util.EventObject;

/**
 * An event that indicates that a constraint or a boundary of a property has
 * changed.
 */
public class PropertyChangeEvent extends EventObject {

    private static final long serialVersionUID = 7042693688939648123L;

    String propertyName;

    Object oldValue;

    Object newValue;

    Object propagationId;

    /**
     * The constructor used to create a new {@code PropertyChangeEvent}.
     *
     * @param source
     *            the changed bean.
     * @param propertyName
     *            the changed property, or <code>null</code> to indicate an
     *            unspecified set of the properties has changed.
     * @param oldValue
     *            the previous value of the property, or <code>null</code> if
     *            the <code>propertyName</code> is <code>null</code> or the
     *            previous value is unknown.
     * @param newValue
     *            the new value of the property, or <code>null</code> if the
     *            <code>propertyName</code> is <code>null</code> or the new
     *            value is unknown.
     */
    public PropertyChangeEvent(Object source, String propertyName,
            Object oldValue, Object newValue) {
        super(source);

        this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Returns the name of the property that has changed. If an unspecified set
     * of properties has changed it returns null.
     *
     * @return the name of the property that has changed, or null.
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Sets the propagationId object.
     *
     * @see #getPropagationId()
     */
    public void setPropagationId(Object propagationId) {
        this.propagationId = propagationId;
    }

    /**
     * Returns the propagationId object. This is reserved for future use. Beans
     * 1.0 demands that a listener receiving this property and then sending its
     * own PropertyChangeEvent sets the received propagationId on the new
     * PropertyChangeEvent's propagationId field.
     *
     * @return the propagationId object.
     */
    public Object getPropagationId() {
        return propagationId;
    }

    /**
     * Returns the old value that the property had. If the old value is unknown
     * this method returns null.
     *
     * @return the old property value or null.
     */
    public Object getOldValue() {
        return oldValue;
    }

    /**
     * Returns the new value that the property now has. If the new value is
     * unknown this method returns null.
     *
     * @return the old property value or null.
     */
    public Object getNewValue() {
        return newValue;
    }
}
