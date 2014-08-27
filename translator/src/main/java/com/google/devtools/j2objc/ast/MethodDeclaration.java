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

import org.eclipse.jdt.core.dom.IMethodBinding;

import java.util.List;

/**
 * Node type for a method declaration.
 */
public class MethodDeclaration extends BodyDeclaration {

  private IMethodBinding methodBinding = null;
  private boolean isConstructor = false;
  private ChildLink<Type> returnType = ChildLink.create(Type.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  private ChildList<SingleVariableDeclaration> parameters =
      ChildList.create(SingleVariableDeclaration.class, this);
  private ChildLink<Block> body = ChildLink.create(Block.class, this);

  public MethodDeclaration(org.eclipse.jdt.core.dom.MethodDeclaration jdtNode) {
    super(jdtNode);
    methodBinding = jdtNode.resolveBinding();
    isConstructor = jdtNode.isConstructor();
    returnType.set((Type) TreeConverter.convert(jdtNode.getReturnType2()));
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
    for (Object param : jdtNode.parameters()) {
      parameters.add((SingleVariableDeclaration) TreeConverter.convert(param));
    }
    body.set((Block) TreeConverter.convert(jdtNode.getBody()));
  }

  public MethodDeclaration(MethodDeclaration other) {
    super(other);
    methodBinding = other.getMethodBinding();
    isConstructor = other.isConstructor();
    returnType.copyFrom(other.getReturnType());
    name.copyFrom(other.getName());
    parameters.copyFrom(other.getParameters());
    body.copyFrom(other.getBody());
  }

  public MethodDeclaration(IMethodBinding methodBinding) {
    super(methodBinding);
    this.methodBinding = methodBinding;
    isConstructor = methodBinding.isConstructor();
    returnType.set(Type.newType(methodBinding.getReturnType()));
    name.set(new SimpleName(methodBinding));
  }

  @Override
  public Kind getKind() {
    return Kind.METHOD_DECLARATION;
  }

  public IMethodBinding getMethodBinding() {
    return methodBinding;
  }

  public void setMethodBinding(IMethodBinding newMethodBinding) {
    methodBinding = newMethodBinding;
  }

  public boolean isConstructor() {
    return isConstructor;
  }

  public Type getReturnType() {
    return returnType.get();
  }

  public SimpleName getName() {
    return name.get();
  }

  public void setName(SimpleName newName) {
    name.set(newName);
  }

  public List<SingleVariableDeclaration> getParameters() {
    return parameters;
  }

  public Block getBody() {
    return body.get();
  }

  public void setBody(Block newBody) {
    body.set(newBody);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      javadoc.accept(visitor);
      annotations.accept(visitor);
      returnType.accept(visitor);
      name.accept(visitor);
      parameters.accept(visitor);
      body.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public MethodDeclaration copy() {
    return new MethodDeclaration(this);
  }
}
