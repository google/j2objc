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

import com.google.devtools.j2objc.types.Types;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import java.util.List;

/**
 * Node for an enum constant.
 */
public class EnumConstantDeclaration extends BodyDeclaration {

  private IVariableBinding variableBinding = null;
  private IMethodBinding methodBinding = null;
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  private ChildList<Expression> arguments = ChildList.create(Expression.class, this);

  public EnumConstantDeclaration(org.eclipse.jdt.core.dom.EnumConstantDeclaration jdtNode) {
    super(jdtNode);
    variableBinding = Types.getVariableBinding(jdtNode.getName());
    methodBinding = Types.getMethodBinding(jdtNode);
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
    for (Object argument : jdtNode.arguments()) {
      arguments.add((Expression) TreeConverter.convert(argument));
    }
  }

  public EnumConstantDeclaration(EnumConstantDeclaration other) {
    super(other);
    variableBinding = other.getVariableBinding();
    methodBinding = other.getMethodBinding();
    name.copyFrom(other.getName());
    arguments.copyFrom(other.getArguments());
  }

  @Override
  public Kind getKind() {
    return Kind.ENUM_CONSTANT_DECLARATION;
  }

  public IVariableBinding getVariableBinding() {
    return variableBinding;
  }

  public IMethodBinding getMethodBinding() {
    return methodBinding;
  }

  public SimpleName getName() {
    return name.get();
  }

  public List<Expression> getArguments() {
    return arguments;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      javadoc.accept(visitor);
      annotations.accept(visitor);
      name.accept(visitor);
      arguments.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public EnumConstantDeclaration copy() {
    return new EnumConstantDeclaration(this);
  }
}
