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
package com.google.devtools.j2objc.ast;

import com.google.devtools.j2objc.jdt.BindingConverter;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import org.eclipse.jdt.core.dom.IMethodBinding;

/**
 * Abstract base class of all AST node types that represent a method reference expression (added in
 * JLS8, section 15.13).
 *
 * <pre>
 * MethodReference:
 *    CreationReference
 *    ExpressionMethodReference
 *    SuperMethodReference
 *    TypeMethodReference
 * </pre>
 */
public abstract class MethodReference extends FunctionalExpression {

  protected ExecutableElement methodElement;
  protected ChildList<Type> typeArguments = ChildList.create(Type.class, this);

  public MethodReference() {}

  public MethodReference(MethodReference other) {
    super(other);
    methodElement = other.getExecutableElement();
    typeArguments.copyFrom(other.getTypeArguments());
  }

  public IMethodBinding getMethodBinding() {
    return (IMethodBinding) BindingConverter.unwrapElement(methodElement);
  }

  public ExecutableElement getExecutableElement() {
    return methodElement;
  }

  public MethodReference setExecutableElement(ExecutableElement newElement) {
    methodElement = newElement;
    return this;
  }

  public List<Type> getTypeArguments() {
    return typeArguments;
  }

  public MethodReference addTypeArgument(Type typeArg) {
    typeArguments.add(typeArg);
    return this;
  }

  @Override
  public abstract MethodReference copy();
}
