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
 * Legacy security code; do not use.
 */
public class ProtectionDomain {
    public ProtectionDomain(CodeSource cs, PermissionCollection permissions) { }

    public ProtectionDomain(CodeSource cs, PermissionCollection permissions, ClassLoader cl, Principal[] principals) { }

    public final ClassLoader getClassLoader() { return null; }

    public final CodeSource getCodeSource() { return null; }

    public final PermissionCollection getPermissions() { return null; }

    public final Principal[] getPrincipals() { return null; }

    public boolean implies(Permission permission) { return true; }
}
