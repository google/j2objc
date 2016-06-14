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

package com.google.devtools.j2objc.javac;

import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Wrapper class around IVariableBinding.
 */
public class JdtVariableBinding extends JdtBinding implements IVariableBinding {
  private final JdtVariableBinding declaration;
  private final JdtTypeBinding type;
  private final JdtTypeBinding declaringClass;
  private final JdtMethodBinding declaringMethod;

  JdtVariableBinding(IVariableBinding binding) {
    super(binding);
    IVariableBinding decl = binding.getVariableDeclaration();
    this.declaration = decl != binding ? BindingConverter.wrapBinding(decl) : this;
    this.type = BindingConverter.wrapBinding(binding.getType());
    this.declaringClass = BindingConverter.wrapBinding(binding.getDeclaringClass());
    this.declaringMethod = BindingConverter.wrapBinding(binding.getDeclaringMethod());
  }

  public Object getConstantValue() {
    return ((IVariableBinding) binding).getConstantValue();
  }

  public JdtTypeBinding getDeclaringClass() {
    return declaringClass;
  }

  public JdtMethodBinding getDeclaringMethod() {
    return declaringMethod;
  }

  public JdtTypeBinding getType() {
    return type;
  }

  public JdtVariableBinding getVariableDeclaration() {
    return declaration;
  }

  public int getVariableId() {
    return ((IVariableBinding) binding).getVariableId();
  }

  public boolean isEffectivelyFinal() {
    return ((IVariableBinding) binding).isEffectivelyFinal();
  }

  public boolean isEnumConstant() {
    return ((IVariableBinding) binding).isEnumConstant();
  }

  public boolean isField() {
    return ((IVariableBinding) binding).isField();
  }

  public boolean isParameter() {
    return ((IVariableBinding) binding).isParameter();
  }
}
