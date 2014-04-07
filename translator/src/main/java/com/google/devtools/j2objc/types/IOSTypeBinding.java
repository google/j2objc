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

package com.google.devtools.j2objc.types;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * IOSTypeBinding: synthetic binding for an iOS type
 *
 * @author Tom Ball
 */
public class IOSTypeBinding extends GeneratedTypeBinding {

  private final ITypeBinding mappedType;
  // To be used if the header declaring this type does not match the type name.
  private String header = null;

  protected IOSTypeBinding(
      String name, ITypeBinding mappedType, ITypeBinding superClass, boolean isInterface) {
    super(name, null, superClass, isInterface, null);
    this.mappedType = mappedType;
  }

  public static IOSTypeBinding newClass(String name, ITypeBinding mappedType) {
    return new IOSTypeBinding(name, mappedType, null, false);
  }

  public static IOSTypeBinding newClass(
      String name, ITypeBinding mappedType, ITypeBinding superClass) {
    return new IOSTypeBinding(name, mappedType, superClass, false);
  }

  public static IOSTypeBinding newUnmappedClass(String name) {
    return new IOSTypeBinding(name, null, null, false);
  }

  public static IOSTypeBinding newInterface(String name, ITypeBinding mappedType) {
    return new IOSTypeBinding(name, mappedType, null, true);
  }

  public void setHeader(String header) {
    this.header = header;
  }

  public String getHeader() {
    return header;
  }

  @Override
  public boolean isEqualTo(IBinding binding) {
    if (binding == this) {
      return true;
    }
    if (binding instanceof IOSTypeBinding) {
      return name.equals(((IOSTypeBinding) binding).name);
    }
    return false;
  }

  @Override
  public boolean isAssignmentCompatible(ITypeBinding variableType) {
    return isEqualTo(variableType) ||
        (mappedType != null && mappedType.isAssignmentCompatible(variableType));
  }

  @Override
  public boolean isCastCompatible(ITypeBinding type) {
    return isEqualTo(type) || mappedType.isCastCompatible(type);
  }
}
