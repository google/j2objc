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
/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.security;

/**
 * Legacy security code; do not use.
 */
public final class AccessController {
    private AccessController() { }

    /**
     * Calls {@code action.run()}.
     */
    public static <T> T doPrivileged(PrivilegedAction<T> action) {
        return action.run();
    }

    /**
     * Calls {@code action.run()}.
     */
    public static <T> T doPrivileged(PrivilegedAction<T> action, AccessControlContext context) {
        return action.run();
    }

    /**
     * Calls {@code action.run()}.
     */
    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
        try {
            return action.run();
        } catch (RuntimeException e) {
            throw e; // so we don't wrap RuntimeExceptions with PrivilegedActionException
        } catch (Exception e) {
            throw new PrivilegedActionException(e);
        }
    }

    /**
     * Calls {@code action.run()}.
     */
    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action, AccessControlContext context) throws PrivilegedActionException {
        return doPrivileged(action);
    }

    /**
     * Calls {@code action.run()}.
     */
    public static <T> T doPrivilegedWithCombiner(PrivilegedAction<T> action) {
        return action.run();
    }

    /**
     * Calls {@code action.run()}.
     */
    public static <T> T doPrivilegedWithCombiner(PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
        return doPrivileged(action);
    }

    public static void checkPermission(Permission permission) throws AccessControlException { }

    public static AccessControlContext getContext() { return new AccessControlContext(null); }
}
