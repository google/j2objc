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

package javax.net.ssl;

import java.util.EventObject;

/**
 * The event sent to an {@code SSLSessionBindingListener} when the listener
 * object is bound ({@link SSLSession#putValue(String, Object)}) or unbound
 * ({@link SSLSession#removeValue(String)}) to an {@code SSLSession}.
 */
public class SSLSessionBindingEvent extends EventObject {

    private final String name;

    /**
     * Creates a new {@code SSLSessionBindingEvent} for the specified session
     * indicating a binding event for the specified name.
     *
     * @param session
     *            the session for which the event occurs.
     * @param name
     *            the name of the object being (un)bound.
     */
    public SSLSessionBindingEvent(SSLSession session, String name) {
        super(session);
        this.name = name;
    }

    /**
     * Returns the name of the binding being added or removed.
     *
     * @return the name of the binding.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the session to which the binding is added or from which it is
     * removed.
     *
     * @return the session to which the binding is added or from which it is
     *         removed.
     */
    public SSLSession getSession() {
        return (SSLSession) this.source;
    }

}
