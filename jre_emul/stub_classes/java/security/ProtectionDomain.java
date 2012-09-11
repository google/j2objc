/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.security;

/**
 * Stub implementation of ProtectionDomain.
 *
 * @see Object
 */
public class ProtectionDomain {
    
  public ProtectionDomain(CodeSource cs, PermissionCollection pc) {}

  public ProtectionDomain(CodeSource cs, PermissionCollection pc, ClassLoader cl, Principal[] p) {}

  public final CodeSource getCodeSource() {
    return null;
  }
  
  public final ClassLoader getClassLoader() {
    return ClassLoader.getSystemClassLoader();
  }
  
  public final Principal[] getPrincipals() {
    return null;
  }
  
  public final PermissionCollection getPermissions() {
    return null;
  }
  
  public boolean implies(Permission p) {
    return false;
  }
}
