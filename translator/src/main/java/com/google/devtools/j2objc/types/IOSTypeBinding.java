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

  private ITypeBinding mappedType;

  public IOSTypeBinding(String name, boolean isInterface) {
    super(name, null, null, isInterface, false);
  }

  public IOSTypeBinding(String name, ITypeBinding superClass) {
    super(name, null, superClass, false, false);
  }

  public IOSTypeBinding(String name, ITypeBinding superClass, boolean isArray) {
    super(name, null, superClass, false, isArray);
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

  public ITypeBinding getMappedType() {
    return mappedType;
  }

  public void setMappedType(ITypeBinding mappedType) {
    this.mappedType = mappedType;
  }
}
