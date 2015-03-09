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

import java.util.EventListener;

/**
 * The interface to be implemented by any object that requires notification when
 * data objects are bound to (or unbound from) an {@code SSLSession}.
 */
public interface SSLSessionBindingListener extends EventListener {

    /**
     * Notifies this listener when a value is bound to an {@code SSLSession}.
     *
     * @param event
     *            the event data.
     */
    public void valueBound(SSLSessionBindingEvent event);

    /**
     * Notifies this listener when a value is unbound from an {@code SSLSession}.
     *
     * @param event
     *            the event data.
     */
    public void valueUnbound(SSLSessionBindingEvent event);

}
