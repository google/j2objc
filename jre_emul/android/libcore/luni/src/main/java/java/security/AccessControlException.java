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

/**
 * {@code AccessControlException} is thrown if the access control infrastructure
 * denies protected access due to missing permissions.
 */
public class AccessControlException extends SecurityException {

    private static final long serialVersionUID = 5138225684096988535L;

    private Permission perm; // Named as demanded by Serialized Form.

    /**
     * Constructs a new instance of {@code AccessControlException} with the
     * given message.
     *
     * @param message
     *            the detail message for this exception.
     */
    public AccessControlException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance of {@code AccessControlException} with the
     * given message and the requested {@code Permission} which was not granted.
     *
     * @param message
     *            the detail message for the exception.
     * @param perm
     *            the requested {@code Permission} which was not granted.
     */
    public AccessControlException(String message, Permission perm) {
        super(message);
        this.perm = perm;
    }

    /**
     * Returns the requested permission that caused this Exception or {@code
     * null} if there is no corresponding {@code Permission}.
     *
     * @return the requested permission that caused this Exception, maybe {@code null}.
     */
    public Permission getPermission() {
        return perm;
    }
}
