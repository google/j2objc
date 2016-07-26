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

import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.types.NativeType;
import com.google.devtools.j2objc.types.NativeTypeBinding;
import com.google.devtools.j2objc.util.BindingUtil;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Factory for wrapping JDT IBindings, and (soon) generating
 * javax.lang.model.element and javax.lang.model.type wrappers of them.
 * This factory should only be called during AST conversion.
 */
public final class BindingConverter {
  private static Map<IBinding, JdtBinding> bindingCache = new IdentityHashMap<>();
  private static Map<JdtBinding, JdtElement> elementCache = new HashMap<>();
  private static Map<String, Name> nameCache = new HashMap<>();
  private static Map<JdtBinding, JdtTypeMirror> typeCache = new HashMap<>();

  public static final JdtTypeMirror NO_TYPE = new JdtNoType();
  public static final JdtTypeMirror NULL_TYPE = new JdtNullType();

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

  public static Name getName(String s) {
    if (s == null) {
      throw new IllegalArgumentException("null name");
    }
    Name result = nameCache.get(s);
    if (result == null) {
      result = new StringName(s);
      nameCache.put(s, result);
    }
    return result;
  }

  public static JdtTypeMirror getType(ITypeBinding binding) {
    if (binding == null) {
      return null;
    }
    JdtTypeMirror type = getTypeMirror(binding);
    if (type != null) {
      return type;
    }
    JdtTypeBinding jdtType = wrapBinding(binding);
    if (binding.isArray()) {
      type = new JdtArrayType(jdtType);
    } else if (BindingUtil.isIntersectionType(binding)) {
      type = new JdtIntersectionType(jdtType);
    } else if (binding.isPrimitive()) {
      if (jdtType instanceof NativeTypeBinding) {
        type = new JdtNativeType(jdtType);
      } else {
        type = new JdtPrimitiveType(jdtType);
      }
    } else if (binding.isTypeVariable()) {
      type = new JdtTypeVariable(jdtType);
    } else if (binding.isWildcardType()) {
      type = new JdtWildcardType(jdtType);
    } else {
      type = new JdtDeclaredType(jdtType);
    }
    typeCache.put(jdtType, type);
    return type;
  }

  public static JdtTypeMirror getType(IMethodBinding binding) {
    JdtTypeMirror type = getTypeMirror(binding);
    if (type != null) {
      return type;
    }
    JdtMethodBinding wrappedBinding = wrapBinding(binding);
    JdtExecutableType executableType = new JdtExecutableType(wrappedBinding);
    typeCache.put(wrappedBinding, executableType);
    return executableType;
  }

  private static JdtTypeMirror getTypeMirror(IBinding binding) {
    JdtBinding wrappedBinding = wrapBinding(binding);
    return typeCache.get(wrappedBinding);
  }

  public static Element getElement(IBinding binding) {
      return getElement(wrapBinding(binding));
  }

  public static VariableElement getVariableElement(IVariableBinding binding) {
    return (VariableElement) getElement(binding);
  }

  public static ExecutableElement getExecutableElement(IMethodBinding binding) {
    return (ExecutableElement) getElement(binding);
  }

  public static JdtElement getElement(JdtBinding binding) {
    if (binding == null) {
      return null;
    }
    JdtElement element = elementCache.get(binding);
    if (element != null) {
      return element;
    }
    if (binding instanceof JdtMethodBinding) {
      element = new JdtExecutableElement((JdtMethodBinding) binding);
    } else if (binding instanceof JdtPackageBinding) {
      element = new JdtPackageElement((JdtPackageBinding) binding);
    } else if (binding instanceof JdtTypeBinding) {
      JdtTypeBinding typeBinding = (JdtTypeBinding) binding;
      element = new JdtTypeElement(typeBinding);
    } else if (binding instanceof JdtVariableBinding) {
      element = new JdtVariableElement((JdtVariableBinding) binding);
    } else {
      throw new AssertionError("unknown element binding: " + binding.getClass().getSimpleName());
    }
    elementCache.put(binding, element);
    return element;
  }

  /**
   * JDT package bindings do not include annotations, so add them from the
   * package's AST node.
   */
  public static JdtPackageElement getPackageElement(
      org.eclipse.jdt.core.dom.PackageDeclaration pkg) {
    JdtPackageElement pkgElement = (JdtPackageElement) getElement(pkg.resolveBinding());
    for (Object modifier : pkg.annotations()) {
      IAnnotationBinding annotation =
          ((org.eclipse.jdt.core.dom.Annotation) modifier).resolveAnnotationBinding();
      pkgElement.addAnnotation(new JdtAnnotationMirror(annotation));
    }
    return pkgElement;
  }

  public static IBinding unwrapElement(Element element) {
    if (element instanceof GeneratedVariableElement) {
      return new GeneratedVariableBinding(element.toString(), 0, element.asType(),
          element.getKind() == ElementKind.FIELD, element.getKind() == ElementKind.PARAMETER,
          null, null);
    }
    return element != null ? ((JdtElement) element).binding : null;
  }

  public static IBinding unwrapTypeMirrorIntoBinding(TypeMirror t) {
    if (t == null) {
      return null;
    }
    if (t instanceof NativeType) {
      return new NativeTypeBinding(((NativeType) t).toString());
    }
    return ((JdtTypeMirror) t).binding;
  }

  public static ITypeBinding unwrapTypeMirrorIntoTypeBinding(TypeMirror t) {
    IBinding b = unwrapTypeMirrorIntoBinding(t);
    return b instanceof ITypeBinding ? (ITypeBinding) b : null;
  }

  public static void reset() {
    bindingCache.clear();
    elementCache.clear();
    nameCache.clear();
    typeCache.clear();
  }
}
