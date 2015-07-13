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

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Binding class for lambda types. Used in place of the AST resolved type binding, which is a raw
 * functional interface type that is not unique.
 *
 * @author Seth Kirby
 */
public class LambdaTypeBinding extends AbstractTypeBinding {
  protected final String name;
  private final IPackageBinding packageBinding;
  private final IMethodBinding functionalInterfaceMethod;

  public LambdaTypeBinding(String name, IPackageBinding packageBinding,
      IMethodBinding functionalInterfaceMethod) {
    this.name = name;
    this.packageBinding = packageBinding;
    this.functionalInterfaceMethod = functionalInterfaceMethod;
  }

  @Override
  public String getKey() {
    return name;
  }

  @Override
  public boolean isEqualTo(IBinding binding) {
    if (binding == this) {
      return true;
    }
    if (binding instanceof LambdaTypeBinding) {
      return getQualifiedName().equals(((LambdaTypeBinding) binding).getQualifiedName());
    }
    return false;
  }

  @Override
  public String getBinaryName() {
    return getQualifiedName();
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getQualifiedName() {
    if (packageBinding != null) {
      return packageBinding.getName() + "." + name;
    }
    return name;
  }

  @Override
  public IPackageBinding getPackage() {
    return packageBinding;
  }

  @Override
  public String toString() {
    return name;
  }

  public IMethodBinding getFunctionalInterfaceMethod() {
    return functionalInterfaceMethod;
  }

  @Override
  public boolean isAssignmentCompatible(ITypeBinding variableType) {
    return false;
  }
}
