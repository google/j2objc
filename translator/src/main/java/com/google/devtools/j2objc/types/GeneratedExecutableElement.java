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
import com.google.devtools.j2objc.util.ElementUtil;
import java.util.List;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Element class for methods created during translation.
 *
 * @author Nathan Braswell
 */
public class GeneratedExecutableElement extends GeneratedElement implements ExecutableElement {

  private final List<VariableElement> parameters = Lists.newArrayList();
  private final TypeMirror returnType;
  private final boolean varargs;

  public GeneratedExecutableElement(
      String name, ElementKind kind, TypeMirror returnType, Element enclosingElement,
      boolean varargs) {
    super(Preconditions.checkNotNull(name), checkElementKind(kind), enclosingElement);
    this.returnType = returnType;
    this.varargs = varargs;
  }

  /**
   * Clone a method binding, so parameters can be added to it.
   */
  public GeneratedExecutableElement(ExecutableElement m) {
    this(m.getSimpleName().toString(), m.getKind(), m.getReturnType(),  m.getEnclosingElement(),
         m.isVarArgs());
    addModifiers(m.getModifiers());
    parameters.addAll(m.getParameters());
  }

  private static ElementKind checkElementKind(ElementKind kind) {
    Preconditions.checkArgument(kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR);
    return kind;
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
}
