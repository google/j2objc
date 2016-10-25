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
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.ast.DebugASTPrinter;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ElementUtil;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Element class for methods created during translation.
 *
 * @author Nathan Braswell
 */
public class GeneratedExecutableElement extends GeneratedElement implements ExecutableElement {

  private final String selector;
  private final List<VariableElement> parameters = Lists.newArrayList();
  private final TypeMirror returnType;
  private final boolean varargs;
  private final IMethodBinding binding = new Binding();

  public GeneratedExecutableElement(
      String name, String selector, ElementKind kind, TypeMirror returnType,
      Element enclosingElement, boolean varargs) {
    super(Preconditions.checkNotNull(name), checkElementKind(kind), enclosingElement, true);
    this.selector = selector;
    this.returnType = returnType;
    this.varargs = varargs;
  }

  private GeneratedExecutableElement(ExecutableElement m) {
    super(m.getSimpleName().toString(), m.getKind(),  m.getEnclosingElement(),
        ElementUtil.isSynthetic(m));
    this.selector = null;
    addModifiers(m.getModifiers());
    parameters.addAll(m.getParameters());
    this.returnType = m.getReturnType();
    this.varargs = m.isVarArgs();
  }

  public static GeneratedExecutableElement newMethodWithSelector(
      String selector, TypeMirror returnType, Element enclosingElement) {
    return new GeneratedExecutableElement(
        selector, selector, ElementKind.METHOD, returnType, enclosingElement, false);
  }

  /**
   * Clone a method element, so parameters can be added to it.
   */
  public static GeneratedExecutableElement asMutable(ExecutableElement method) {
    return method instanceof GeneratedExecutableElement
        ? (GeneratedExecutableElement) method
        : new GeneratedExecutableElement(method);
  }

  private static ElementKind checkElementKind(ElementKind kind) {
    Preconditions.checkArgument(kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR);
    return kind;
  }

  public String getSelector() {
    return selector;
  }

  public void addParameter(VariableElement param) {
    parameters.add(param);
  }

  public void addParametersPlaceholderFront(List<TypeMirror> types) {
    for (int i = types.size() - 1; i >= 0; i--) {
      addParameterPlaceholderFront(types.get(i));
    }
  }

  public void addParameterPlaceholderFront(TypeMirror type) {
    parameters.add(0, new GeneratedVariableElement(
        "placeholder", type, ElementKind.PARAMETER, null));
  }

  @Override
  public GeneratedExecutableElement addModifiers(Modifier... newModifiers) {
    return (GeneratedExecutableElement) super.addModifiers(newModifiers);
  }

  @Override
  public GeneratedExecutableElement addModifiers(Collection<? extends Modifier> newModifiers) {
    return (GeneratedExecutableElement) super.addModifiers(newModifiers);
  }

  @Override
  public GeneratedExecutableElement removeModifiers(Modifier... modifiersToRemove) {
    return (GeneratedExecutableElement) super.removeModifiers(modifiersToRemove);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    DebugASTPrinter.printModifiers(ElementUtil.fromModifierSet(getModifiers()), sb);
    sb.append(returnType != null ? returnType.toString() : "<no type>");
    sb.append(' ');
    sb.append(getName());
    sb.append('(');
    boolean notFirst = false;
    for (VariableElement p : getParameters()) {
      if (notFirst) {
        sb.append(", ");
      }
      sb.append(p.asType().toString());
      notFirst = true;
    }
    sb.append(')');
    return sb.toString();
  }

  @Override
  public TypeMirror asType() {
    throw new AssertionError("not implemented");
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitExecutable(this, p);
  }

  @Override
  public List<? extends TypeParameterElement> getTypeParameters() {
    throw new AssertionError("not implemented");
  }

  @Override
  public TypeMirror getReturnType() {
    return returnType;
  }

  @Override
  public List<? extends VariableElement> getParameters() {
    return parameters;
  }

  @Override
  public TypeMirror getReceiverType() {
    throw new AssertionError("not implemented");
  }

  @Override
  public boolean isVarArgs() {
    return varargs;
  }

  @Override
  public boolean isDefault() {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<? extends TypeMirror> getThrownTypes() {
    throw new AssertionError("not implemented");
  }

  @Override
  public AnnotationValue getDefaultValue() {
    throw new AssertionError("not implemented");
  }

  public IMethodBinding asMethodBinding() {
    return binding;
  }

  /**
   * An associated IMethodBinding implementation.
   */
  public class Binding implements IMethodBinding {

    public ExecutableElement asElement() {
      return GeneratedExecutableElement.this;
    }

    @Override
    public int getKind() {
      return IBinding.METHOD;
    }

    @Override
    public String getKey() {
      return getName();
    }

    @Override
    public boolean isEqualTo(IBinding binding) {
      return binding == this;
    }

    @Override
    public boolean isSynthetic() {
      return GeneratedExecutableElement.this.isSynthetic();
    }

    @Override
    public int getModifiers() {
      return ElementUtil.fromModifierSet(GeneratedExecutableElement.this.getModifiers())
          | (isSynthetic() ? BindingUtil.ACC_SYNTHETIC : 0);
    }

    @Override
    public ITypeBinding getDeclaredReceiverType() {
      return null;
    }

    @Override
    public ITypeBinding getDeclaringClass() {
      return BindingConverter.unwrapTypeElement(
          ElementUtil.getDeclaringClass(GeneratedExecutableElement.this));
    }

    @Override
    public Object getDefaultValue() {
      return null;
    }

    @Override
    public ITypeBinding[] getExceptionTypes() {
      return new ITypeBinding[0];
    }

    @Override
    public IMethodBinding getMethodDeclaration() {
      return this;
    }

    @Override
    public String getName() {
      return GeneratedExecutableElement.this.getSimpleName().toString();
    }

    @Override
    public IAnnotationBinding[] getParameterAnnotations(int paramIndex) {
      return new IAnnotationBinding[0];
    }

    @Override
    public ITypeBinding[] getParameterTypes() {
      ITypeBinding[] paramTypes = new ITypeBinding[parameters.size()];
      for (int i = 0; i < parameters.size(); i++) {
        paramTypes[i] = BindingConverter.unwrapTypeMirrorIntoTypeBinding(
            parameters.get(i).asType());
      }
      return paramTypes;
    }

    @Override
    public ITypeBinding getReturnType() {
      return BindingConverter.unwrapTypeMirrorIntoTypeBinding(returnType);
    }

    @Override
    public ITypeBinding[] getTypeArguments() {
      return new ITypeBinding[0];
    }

    @Override
    public ITypeBinding[] getTypeParameters() {
      return new ITypeBinding[0];
    }

    @Override
    public boolean isAnnotationMember() {
      return false;
    }

    @Override
    public boolean isConstructor() {
      return GeneratedExecutableElement.this.getKind() == ElementKind.CONSTRUCTOR;
    }

    @Override
    public boolean isDefaultConstructor() {
      return false;
    }

    @Override
    public boolean isGenericMethod() {
      return false;
    }

    @Override
    public boolean isParameterizedMethod() {
      return false;
    }

    @Override
    public boolean isRawMethod() {
      return false;
    }

    @Override
    public boolean isSubsignature(IMethodBinding otherMethod) {
      return false;
    }

    @Override
    public boolean isVarargs() {
      return GeneratedExecutableElement.this.isVarArgs();
    }

    @Override
    public boolean overrides(IMethodBinding method) {
      return false;
    }

    @Override
    public IJavaElement getJavaElement() {
      throw new AssertionError("not implemented");
    }

    @Override
    public boolean isRecovered() {
      return false;
    }

    @Override
    public boolean isDeprecated() {
      return false;
    }

    @Override
    public IAnnotationBinding[] getAnnotations() {
      return new IAnnotationBinding[0];
    }

    // Internal JDT has a different version than external.
    @SuppressWarnings("MissingOverride")
    public IBinding getDeclaringMember() {
      return null;
    }
  }
}
