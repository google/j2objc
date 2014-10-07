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

/**
 * Legacy security code; do not use.
 */
public abstract class Policy {
    /**
     * Legacy security code; do not use.
     */
    public static interface Parameters { }

    public Policy() { }

    public static Policy getInstance(String type, Policy.Parameters params) throws NoSuchAlgorithmException { return null; }

    public static Policy getInstance(String type, Policy.Parameters params, String provider) throws NoSuchProviderException, NoSuchAlgorithmException { return null; }

    public static Policy getInstance(String type, Policy.Parameters params, Provider provider) throws NoSuchAlgorithmException { return null; }

    public Policy.Parameters getParameters() { return null; }

    public Provider getProvider() { return null; }

    public String getType() { return null; }

    public static final PermissionCollection UNSUPPORTED_EMPTY_COLLECTION = new AllPermissionCollection();

    public PermissionCollection getPermissions(CodeSource cs) { return null; }

    public void refresh() { }

    public PermissionCollection getPermissions(ProtectionDomain domain) { return null; }

    public boolean implies(ProtectionDomain domain, Permission permission) { return true; }

    public static Policy getPolicy() { return null; }

    public static void setPolicy(Policy policy) { }
}
