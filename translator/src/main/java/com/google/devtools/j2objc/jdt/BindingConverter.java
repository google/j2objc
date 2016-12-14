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

import com.google.devtools.j2objc.types.AbstractTypeMirror;
import com.google.devtools.j2objc.types.GeneratedElement;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedPackageElement;
import com.google.devtools.j2objc.types.GeneratedTypeElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.BindingUtil;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.PackageDeclaration;

/**
 * Factory for wrapping JDT IBindings, and generating javax.lang.model.element and
 * javax.lang.model.type wrappers of them.
 */
public final class BindingConverter {

  private static Map<IBinding, JdtElement> elementCache = new HashMap<>();
  private static Map<GeneratedElement, IBinding> generatedBindingCache = new HashMap<>();
  private static Map<String, Name> nameCache = new HashMap<>();
  private static Map<IBinding, JdtTypeMirror> typeCache = new HashMap<>();

  public static final JdtNoType NO_TYPE = new JdtNoType(null);
  public static final JdtTypeMirror NULL_TYPE = new JdtNullType();

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

  public static TypeMirror getType(ITypeBinding binding) {
    if (binding == null) {
      return null;
    }
    JdtTypeMirror type = typeCache.get(binding);
    if (type != null) {
      return type;
    }
    if (binding.isArray()) {
      type = new JdtArrayType(binding);
    } else if (BindingUtil.isIntersectionType(binding)) {
      type = JdtIntersectionType.fromJdtIntersection(binding);
    } else if (binding.isPrimitive()) {
      if (binding.getBinaryName().charAt(0) == 'V') {
        type = new JdtNoType(binding);
      } else {
        type = new JdtPrimitiveType(binding);
      }
    } else if (binding.isTypeVariable()) {
      type = JdtTypeVariable.fromTypeVariable(binding);
    } else if (binding.isWildcardType()) {
      type = new JdtWildcardType(binding);
    } else if (binding.isCapture()) {
      type = JdtTypeVariable.fromCapture(binding);
    } else if (binding.isNullType()) {
      return NULL_TYPE;
    } else {
      type = new JdtDeclaredType(binding);
    }
    typeCache.put(binding, type);
    return type;
  }

  public static JdtExecutableType getType(IMethodBinding binding) {
    JdtTypeMirror type = typeCache.get(binding);
    if (type != null) {
      return (JdtExecutableType) type;
    }
    JdtExecutableType executableType = new JdtExecutableType(binding);
    typeCache.put(binding, executableType);
    return executableType;
  }

  public static VariableElement getVariableElement(IVariableBinding binding) {
    return (VariableElement) getElement(binding);
  }

  public static ExecutableElement getExecutableElement(IMethodBinding binding) {
    return (ExecutableElement) getElement(binding);
  }

  public static TypeElement getTypeElement(ITypeBinding binding) {
    return (TypeElement) getElement(binding);
  }

  public static Element getElement(IBinding binding) {
    if (binding == null) {
      return null;
    }
    JdtElement element = elementCache.get(binding);
    if (element != null) {
      return element;
    }
    if (binding instanceof GeneratedMethodBinding) {
      return ((GeneratedMethodBinding) binding).asElement();
    } else if (binding instanceof GeneratedVariableBinding) {
      return ((GeneratedVariableBinding) binding).asElement();
    } else if (binding instanceof IMethodBinding) {
      element = new JdtExecutableElement((IMethodBinding) binding);
    } else if (binding instanceof IPackageBinding) {
      element = new JdtPackageElement((IPackageBinding) binding);
    } else if (binding instanceof ITypeBinding) {
      ITypeBinding typeBinding = (ITypeBinding) binding;
      element = typeBinding.isTypeVariable() || typeBinding.isCapture()
          ? new JdtTypeParameterElement(typeBinding) : new JdtTypeElement(typeBinding);
    } else if (binding instanceof IVariableBinding) {
      element = JdtVariableElement.create((IVariableBinding) binding);
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
  public static JdtPackageElement getPackageElement(PackageDeclaration pkg) {
    IPackageBinding binding = pkg.resolveBinding();
    JdtPackageElement pkgElement = (JdtPackageElement) getElement(binding);
    if (pkgElement.getAnnotationMirrors().isEmpty() && pkg.annotations().size() > 0) {
      for (Object modifier : pkg.annotations()) {
        IAnnotationBinding annotation =
            ((org.eclipse.jdt.core.dom.Annotation) modifier).resolveAnnotationBinding();
        pkgElement.addAnnotation(new JdtAnnotationMirror(annotation));
      }
    }
    return pkgElement;
  }

  public static JdtPackageElement getPackageElement(IPackageBinding binding) {
    return (JdtPackageElement) getElement(binding);
  }

  public static IBinding unwrapElement(Element element) {
    if (element instanceof GeneratedElement) {
      return unwrapGeneratedElement((GeneratedElement) element);
    }
    return element != null ? ((JdtElement) element).binding : null;
  }

  private static IBinding unwrapGeneratedElement(GeneratedElement element) {
    IBinding binding = generatedBindingCache.get(element);
    if (binding != null) {
      return binding;
    }
    if (element instanceof GeneratedVariableElement) {
      binding = new GeneratedVariableBinding((GeneratedVariableElement) element);
      generatedBindingCache.put(element, binding);
      return binding;
    }
    if (element instanceof GeneratedExecutableElement) {
      binding = new GeneratedMethodBinding((GeneratedExecutableElement) element);
      generatedBindingCache.put(element, binding);
      return binding;
    }
    if (element instanceof GeneratedTypeElement) {
      throw new AssertionError("not supported");
    }
    if (element instanceof GeneratedPackageElement) {
      binding = new GeneratedPackageBinding(((GeneratedPackageElement) element).getName());
      generatedBindingCache.put(element,  binding);
      return binding;
    }
    throw new AssertionError("unknown generated element kind");
  }

  public static ITypeBinding unwrapTypeElement(TypeElement t) {
    return (ITypeBinding) unwrapElement(t);
  }

  public static IVariableBinding unwrapVariableElement(VariableElement v) {
    return (IVariableBinding) unwrapElement(v);
  }

  public static IMethodBinding unwrapExecutableElement(ExecutableElement e) {
    return (IMethodBinding) unwrapElement(e);
  }

  public static IBinding unwrapTypeMirrorIntoBinding(TypeMirror t) {
    if (t == null) {
      return null;
    } else if (t instanceof AbstractTypeMirror) {
      throw new AssertionError("not supported");
    } else if (t instanceof GeneratedExecutableElement.Mirror) {
      return new GeneratedMethodBinding(
          ((GeneratedExecutableElement.Mirror) t).asExecutableElement());
    }
    return ((JdtTypeMirror) t).binding;
  }

  public static ITypeBinding unwrapTypeMirrorIntoTypeBinding(TypeMirror t) {
    IBinding b = unwrapTypeMirrorIntoBinding(t);
    return b instanceof ITypeBinding ? (ITypeBinding) b : null;
  }

  public static IAnnotationBinding unwrapAnnotationMirror(AnnotationMirror a) {
    return ((JdtAnnotationMirror) a).binding;
  }

  public static void reset() {
    generatedBindingCache.clear();
    elementCache.clear();
    nameCache.clear();
    typeCache.clear();
  }
}
