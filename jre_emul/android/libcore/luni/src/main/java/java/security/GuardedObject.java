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

package java.security;

import java.io.IOException;
import java.io.Serializable;

/**
 * {@code GuardedObject} controls access to an object, by checking all requests
 * for the object with a {@code Guard}.
 */
public class GuardedObject implements Serializable {

    private static final long serialVersionUID = -5240450096227834308L;

    private final Object object;

    private final Guard guard;

    /**
     * Constructs a new instance of {@code GuardedObject} which protects access
     * to the specified {@code Object} using the specified {@code Guard}.
     *
     * @param object
     *            the {@code Object} to protect.
     * @param guard
     *            the {@code Guard} which protects the specified {@code Object},
     *            maybe {@code null}.
     */
    public GuardedObject(Object object, Guard guard) {
        this.object = object;
        this.guard = guard;
    }

    /**
     * Returns the guarded {@code Object} if the associated {@code Guard}
     * permits access. If access is not granted, then a {@code
     * SecurityException} is thrown.
     *
     * @return the guarded object.
     * @exception SecurityException
     *                if access is not granted to the guarded object.
     */
    public Object getObject() throws SecurityException {
        if (guard != null) {
            guard.checkGuard(object);
        }
        return object;
    }

    /**
     * Checks the guard (if there is one) before performing a default
     * serialization.
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        if (guard != null) {
            guard.checkGuard(object);
        }
        out.defaultWriteObject();
    }
}
