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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * A default {@code PermissionCollection} implementation that uses a hashtable.
 * Each hashtable entry stores a Permission object as both the key and the
 * value.
 * <p>
 * This {@code PermissionCollection} is intended for storing &quot;neutral&quot;
 * permissions which do not require special collection.
 */

final class PermissionsHash extends PermissionCollection {

    private static final long serialVersionUID = -8491988220802933440L;

    private final Hashtable perms = new Hashtable();

    /**
     * Adds the argument to the collection.
     *
     * @param permission
     *            the permission to add to the collection.
     */
    public void add(Permission permission) {
        perms.put(permission, permission);
    }

    /**
     * Returns an enumeration of the permissions in the receiver.
     *
     * @return Enumeration the permissions in the receiver.
     */
    public Enumeration elements() {
        return perms.elements();
    }

    /**
     * Indicates whether the argument permission is implied by the permissions
     * contained in the receiver.
     *
     * @return boolean <code>true</code> if the argument permission is implied
     *         by the permissions in the receiver, and <code>false</code> if
     *         it is not.
     * @param permission
     *            java.security.Permission the permission to check
     */
    public boolean implies(Permission permission) {
        for (Enumeration elements = elements(); elements.hasMoreElements();) {
            if (((Permission)elements.nextElement()).implies(permission)) {
                return true;
            }
        }
        return false;
    }
}
