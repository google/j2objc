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
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for wrapping JDT IBindings, and (soon) generating
 * javax.lang.model.element and javax.lang.model.type wrappers of them.
 * This factory should only be called during AST conversion.
 */
public final class BindingConverter {
  private static Map<IBinding, JdtBinding> bindingCache = new IdentityHashMap<>();

  public static JdtAnnotationBinding wrapBinding(IAnnotationBinding binding) {
    if (binding == null) {
      return null;
    }
    if (binding instanceof JdtAnnotationBinding) {
      return (JdtAnnotationBinding) binding;
    }
    if (bindingCache.containsKey(binding)) {
      return (JdtAnnotationBinding) bindingCache.get(binding);
    }
    JdtAnnotationBinding result = new JdtAnnotationBinding(binding);
    bindingCache.put(binding, result);
    return result;
  }

  public static JdtAnnotationBinding[] wrapBindings(IAnnotationBinding[] bindings) {
    JdtAnnotationBinding[] wrappedBindings = new JdtAnnotationBinding[bindings.length];
    for (int i = 0; i < bindings.length; i++) {
      wrappedBindings[i] = BindingConverter.wrapBinding(bindings[i]);
    }
    return wrappedBindings;
  }

  public static JdtExtendedModifier wrapExtendedModifier(IExtendedModifier modifier) {
    if (modifier == null) {
      return null;
    }
    if (modifier instanceof JdtExtendedModifier) {
      return (JdtExtendedModifier) modifier;
    }
    return new JdtExtendedModifier(modifier);
  }

  public static JdtMemberValuePairBinding wrapBinding(IMemberValuePairBinding binding) {
    if (binding == null) {
      return null;
    }
    if (binding instanceof JdtMemberValuePairBinding) {
      return (JdtMemberValuePairBinding) binding;
    }
    if (bindingCache.containsKey(binding)) {
      return (JdtMemberValuePairBinding) bindingCache.get(binding);
    }
    JdtMemberValuePairBinding result = new JdtMemberValuePairBinding(binding);
    bindingCache.put(binding, result);
    return result;
  }

  public static JdtMemberValuePairBinding[] wrapBindings(IMemberValuePairBinding[] bindings) {
    JdtMemberValuePairBinding[] wrappedBindings = new JdtMemberValuePairBinding[bindings.length];
    for (int i = 0; i < bindings.length; i++) {
      wrappedBindings[i] = BindingConverter.wrapBinding(bindings[i]);
    }
    return wrappedBindings;
  }

  public static JdtMethodBinding wrapBinding(IMethodBinding binding) {
    if (binding == null) {
      return null;
    }
    if (binding instanceof JdtMethodBinding) {
      return (JdtMethodBinding) binding;
    }
    if (bindingCache.containsKey(binding)) {
      return (JdtMethodBinding) bindingCache.get(binding);
    }
    JdtMethodBinding result = new JdtMethodBinding(binding);
    bindingCache.put(binding, result);
    return result;
  }

  public static JdtMethodBinding[] wrapBindings(IMethodBinding[] bindings) {
    JdtMethodBinding[] wrappedBindings = new JdtMethodBinding[bindings.length];
    for (int i = 0; i < bindings.length; i++) {
      wrappedBindings[i] = BindingConverter.wrapBinding(bindings[i]);
    }
    return wrappedBindings;
  }

  public static JdtPackageBinding wrapBinding(IPackageBinding binding) {
    if (binding == null) {
      return null;
    }
    if (binding instanceof JdtPackageBinding) {
      return (JdtPackageBinding) binding;
    }
    if (bindingCache.containsKey(binding)) {
      return (JdtPackageBinding) bindingCache.get(binding);
    }
    JdtPackageBinding result = new JdtPackageBinding(binding);
    bindingCache.put(binding, result);
    return result;
  }

  public static JdtTypeBinding wrapBinding(ITypeBinding binding) {
    if (binding == null) {
      return null;
    }
    if (binding instanceof JdtTypeBinding) {
      return (JdtTypeBinding) binding;
    }
    if (bindingCache.containsKey(binding)) {
      return (JdtTypeBinding) bindingCache.get(binding);
    }
    JdtTypeBinding result = new JdtTypeBinding(binding);
    bindingCache.put(binding, result);
    return result;
  }

  public static JdtTypeBinding[] wrapBindings(ITypeBinding[] bindings) {
    JdtTypeBinding[] wrappedBindings = new JdtTypeBinding[bindings.length];
    for (int i = 0; i < bindings.length; i++) {
      wrappedBindings[i] = BindingConverter.wrapBinding(bindings[i]);
    }
    return wrappedBindings;
  }

  public static JdtVariableBinding wrapBinding(IVariableBinding binding) {
    if (binding == null) {
      return null;
    }
    if (binding instanceof JdtVariableBinding) {
      return (JdtVariableBinding) binding;
    }
    if (bindingCache.containsKey(binding)) {
      return (JdtVariableBinding) bindingCache.get(binding);
    }
    JdtVariableBinding result = new JdtVariableBinding(binding);
    bindingCache.put(binding, result);
    return result;
  }

  public static JdtVariableBinding[] wrapBindings(IVariableBinding[] bindings) {
    JdtVariableBinding[] wrappedBindings = new JdtVariableBinding[bindings.length];
    for (int i = 0; i < bindings.length; i++) {
      wrappedBindings[i] = BindingConverter.wrapBinding(bindings[i]);
    }
    return wrappedBindings;
  }

  public static List<JdtVariableBinding> wrapBindings(List<IVariableBinding> bindings) {
    List<JdtVariableBinding> wrappedBindings = new ArrayList<>();
    for (IVariableBinding binding : bindings) {
      wrappedBindings.add(BindingConverter.wrapBinding(binding));
    }
    return wrappedBindings;
  }

  public static JdtBinding wrapBinding(IBinding binding) {
    if (binding == null) {
      return null;
    }
    switch (binding.getKind()) {
      case IBinding.ANNOTATION: return wrapBinding((IAnnotationBinding) binding);
      case IBinding.MEMBER_VALUE_PAIR: return wrapBinding((IMemberValuePairBinding) binding);
      case IBinding.METHOD: return wrapBinding((IMethodBinding) binding);
      case IBinding.PACKAGE: return wrapBinding((IPackageBinding) binding);
      case IBinding.TYPE: return wrapBinding((ITypeBinding) binding);
      case IBinding.VARIABLE: return wrapBinding((IVariableBinding) binding);
      default:
        throw new AssertionError("unknown binding type: " + binding.getKind());
    }
  }

  public static void reset() {
    bindingCache.clear();
  }
}
