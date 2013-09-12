/*
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

import com.google.common.base.Preconditions;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * ITypeBinding implementation for a C-style pointer type.
 *
 * @author Keith Stanger
 */
public class PointerTypeBinding extends AbstractTypeBinding {

  private final ITypeBinding pointeeType;

  public PointerTypeBinding(ITypeBinding pointeeType) {
    this.pointeeType = Preconditions.checkNotNull(pointeeType);
  }

  public ITypeBinding getPointeeType() {
    return pointeeType;
  }

  @Override
  public boolean isInterface() {
    return false;
  }

  @Override
  public boolean isClass() {
    return false;
  }

  @Override
  public boolean isArray() {
    return false;
  }

  @Override
  public String getKey() {
    return pointeeType.getKey() + "*";
  }

  @Override
  public String getName() {
    return pointeeType.getName() + "_p";
  }

  @Override
  public String getQualifiedName() {
    return pointeeType.getQualifiedName() + "*";
  }

  @Override
  public boolean isEqualTo(IBinding binding) {
    if (binding == this) {
      return true;
    }
    if (!(binding instanceof PointerTypeBinding)) {
      return false;
    }
    return pointeeType.isEqualTo(((PointerTypeBinding) binding).pointeeType);
  }

  @Override
  public boolean isAssignmentCompatible(ITypeBinding variableType) {
    if (!(variableType instanceof PointerTypeBinding)) {
      return false;
    }
    return pointeeType.isAssignmentCompatible(((PointerTypeBinding) variableType).pointeeType);
  }
}
