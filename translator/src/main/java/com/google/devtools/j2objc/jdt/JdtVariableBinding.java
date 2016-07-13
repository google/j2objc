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

package com.google.devtools.j2objc.jdt;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Wrapper class around IVariableBinding.
 */
public class JdtVariableBinding extends JdtBinding implements IVariableBinding {
  private JdtVariableBinding declaration;
  private JdtTypeBinding type;
  private JdtTypeBinding declaringClass;
  private JdtMethodBinding declaringMethod;
  private boolean initialized = false;

  protected JdtVariableBinding(IVariableBinding binding) {
    super(binding);
  }

  private void maybeInitialize() {
    if (!initialized) {
      IVariableBinding varBinding = (IVariableBinding) binding;
      this.declaration = BindingConverter.wrapBinding(varBinding.getVariableDeclaration());
      this.type = BindingConverter.wrapBinding(varBinding.getType());
      this.declaringClass = BindingConverter.wrapBinding(varBinding.getDeclaringClass());
      this.declaringMethod = BindingConverter.wrapBinding(varBinding.getDeclaringMethod());
      initialized = true;
    }
  }

  public Object getConstantValue() {
    return ((IVariableBinding) binding).getConstantValue();
  }

  public ITypeBinding getDeclaringClass() {
    maybeInitialize();
    return declaringClass;
  }

  public IMethodBinding getDeclaringMethod() {
    maybeInitialize();
    return declaringMethod;
  }

  public ITypeBinding getType() {
    maybeInitialize();
    return type;
  }

  public IVariableBinding getVariableDeclaration() {
    maybeInitialize();
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
