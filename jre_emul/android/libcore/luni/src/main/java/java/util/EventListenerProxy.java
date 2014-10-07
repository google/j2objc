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

package java.util;


/**
 * This abstract class provides a simple wrapper for objects of type {@code EventListener}.
 */
public abstract class EventListenerProxy implements EventListener {

    private final EventListener listener;

    /**
     * Creates a new {@code EventListener} proxy instance.
     *
     * @param listener
     *            the listener wrapped by this proxy.
     */
    public EventListenerProxy(EventListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the wrapped {@code EventListener}.
     *
     * @return the wrapped {@code EventListener}.
     */
    public EventListener getListener() {
        return listener;
    }
}
