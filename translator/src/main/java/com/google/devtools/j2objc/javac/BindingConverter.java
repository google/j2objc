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

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Factory for wrapping JDT IBindings, and (soon) generating
 * javax.lang.model.element and javax.lang.model.type wrappers of them.
 * This factory should only be called during AST conversion.
 */
public final class BindingConverter {

  public static JdtAnnotationBinding wrapBinding(IAnnotationBinding binding) {
    return binding != null ? new JdtAnnotationBinding(binding) : null;
  }

  public static JdtAnnotationBinding[] wrapBindings(IAnnotationBinding[] bindings) {
    JdtAnnotationBinding[] wrappedBindings = new JdtAnnotationBinding[bindings.length];
    for (int i = 0; i < bindings.length; i++) {
      wrappedBindings[i] = BindingConverter.wrapBinding(bindings[i]);
    }
    return wrappedBindings;
  }

  public static JdtExtendedModifier wrapExtendedModifier(IExtendedModifier modifier) {
    return modifier != null ? new JdtExtendedModifier(modifier) : null;
  }

  public static JdtMemberValuePairBinding wrapBinding(IMemberValuePairBinding binding) {
    return binding != null ? new JdtMemberValuePairBinding(binding) : null;
  }

  public static JdtMemberValuePairBinding[] wrapBindings(IMemberValuePairBinding[] bindings) {
    JdtMemberValuePairBinding[] wrappedBindings = new JdtMemberValuePairBinding[bindings.length];
    for (int i = 0; i < bindings.length; i++) {
      wrappedBindings[i] = BindingConverter.wrapBinding(bindings[i]);
    }
    return wrappedBindings;
  }

  public static JdtMethodBinding wrapBinding(IMethodBinding binding) {
    return binding != null ? new JdtMethodBinding(binding) : null;
  }

  public static JdtMethodBinding[] wrapBindings(IMethodBinding[] bindings) {
    JdtMethodBinding[] wrappedBindings = new JdtMethodBinding[bindings.length];
    for (int i = 0; i < bindings.length; i++) {
      wrappedBindings[i] = BindingConverter.wrapBinding(bindings[i]);
    }
    return wrappedBindings;
  }

  public static JdtPackageBinding wrapBinding(IPackageBinding binding) {
    return binding != null ? new JdtPackageBinding(binding) : null;
  }

  public static JdtTypeBinding wrapBinding(ITypeBinding binding) {
    return binding != null ? new JdtTypeBinding(binding) : null;
  }

  public static JdtTypeBinding[] wrapBindings(ITypeBinding[] bindings) {
    JdtTypeBinding[] wrappedBindings = new JdtTypeBinding[bindings.length];
    for (int i = 0; i < bindings.length; i++) {
      wrappedBindings[i] = BindingConverter.wrapBinding(bindings[i]);
    }
    return wrappedBindings;
  }

  public static JdtVariableBinding wrapBinding(IVariableBinding binding) {
    return binding != null ? new JdtVariableBinding(binding) : null;
  }

  public static JdtVariableBinding[] wrapBindings(IVariableBinding[] bindings) {
    JdtVariableBinding[] wrappedBindings = new JdtVariableBinding[bindings.length];
    for (int i = 0; i < bindings.length; i++) {
      wrappedBindings[i] = BindingConverter.wrapBinding(bindings[i]);
    }
    return wrappedBindings;
  }
}
