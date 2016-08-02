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
import com.google.devtools.j2objc.jdt.JdtElements;
import com.google.devtools.j2objc.util.ElementUtil;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Element class for methods created during translation.
 *
 * @author Nathan Braswell
 */
public class GeneratedExecutableElement implements ExecutableElement {

  private final String name;
  private Set<Modifier> modifiers;
  private final List<VariableElement> parameters = Lists.newArrayList();
  private final TypeMirror returnType;
  private Element enclosingElement;
  private final boolean varargs;
  private final boolean isConstructor;

  public GeneratedExecutableElement(
      String name, Set<Modifier> modifiers, TypeMirror returnType,
      Element enclosingElement,
      boolean isConstructor, boolean varargs) {
    this.name = Preconditions.checkNotNull(name);
    this.modifiers = modifiers;
    this.returnType = returnType;
    this.enclosingElement = enclosingElement;
    this.isConstructor = isConstructor;
    this.varargs = varargs;
  }

  /**
   * Clone a method binding, so parameters can be added to it.
   */
  public GeneratedExecutableElement(ExecutableElement m) {
    this(m.getSimpleName().toString(), m.getModifiers(),
        m.getReturnType(),
        m.getEnclosingElement(),
        m.getKind() == ElementKind.CONSTRUCTOR, m.isVarArgs());
    parameters.addAll(m.getParameters());
  }

  public void addParametersPlaceholderFront(List<TypeMirror> types) {
    for (int i = types.size() - 1; i >= 0; i--) {
      addParameterPlaceholderFront(types.get(i));
    }
  }

  public void addParameterPlaceholderFront(TypeMirror type) {
    parameters.add(0, new GeneratedVariableElement("placeholder", type, true, false));
  }

  public void addParameterPlaceholderBack(TypeMirror t) {
    parameters.add(parameters.size(), new GeneratedVariableElement("placeholder", t, true, false));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof GeneratedExecutableElement)) {
      return false;
    }
    GeneratedExecutableElement other = (GeneratedExecutableElement) obj;
    return name.equals(other.name)
        && modifiers.equals(other.modifiers)
        && varargs == other.varargs
        // The returnType is null for constructors, so test equality first.
        && (returnType == null ? other.returnType == null : returnType.equals(other.returnType))
        && enclosingElement.equals(other.enclosingElement)
        && parameters.equals(other.parameters)
        && isConstructor == other.isConstructor;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    DebugASTPrinter.printModifiers(ElementUtil.fromModifierSet(modifiers), sb);
    sb.append(returnType != null ? returnType.toString() : "<no type>");
    sb.append(' ');
    sb.append((name != null) ? name : "<no name>");
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
  public ElementKind getKind() {
    return isConstructor ? ElementKind.CONSTRUCTOR : ElementKind.METHOD;
  }

  @Override
  public Set<Modifier> getModifiers() {
    return modifiers;
  }

  @Override
  public Element getEnclosingElement() {
    return enclosingElement;
  }

  @Override
  public List<? extends Element> getEnclosedElements() {
    throw new AssertionError("not implemented");
  }

  @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    throw new AssertionError("not implemented");
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    throw new AssertionError("not implemented");
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    throw new AssertionError("not implemented");
  }

  //TODO(user): Uncomment Overrides after Java 8 transition.
  //@Override
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    throw new AssertionError("not implemented");
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

  //@Override
  public TypeMirror getReceiverType() {
    throw new AssertionError("not implemented");
  }

  @Override
  public boolean isVarArgs() {
    return varargs;
  }

  //@Override
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

  @Override
  public Name getSimpleName() {
    return JdtElements.getInstance().getName(name);
  }
}
