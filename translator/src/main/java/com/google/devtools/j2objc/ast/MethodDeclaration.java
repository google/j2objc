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

import com.google.devtools.j2objc.util.ElementUtil;
import java.util.List;
import javax.lang.model.element.ExecutableElement;

/**
 * Node type for a method declaration.
 */
public class MethodDeclaration extends BodyDeclaration {

  private ExecutableElement executableElement = null;
  private boolean isConstructor = false;
  private boolean hasDeclaration = true;
  private boolean isUnavailable = false;
  private ChildLink<Type> returnType = ChildLink.create(Type.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  private ChildList<SingleVariableDeclaration> parameters =
      ChildList.create(SingleVariableDeclaration.class, this);
  private ChildLink<Block> body = ChildLink.create(Block.class, this);

  public MethodDeclaration() {}

  public MethodDeclaration(MethodDeclaration other) {
    super(other);
    executableElement = other.getExecutableElement();
    isConstructor = other.isConstructor();
    hasDeclaration = other.hasDeclaration();
    isUnavailable = other.isUnavailable();
    returnType.copyFrom(other.getReturnType());
    name.copyFrom(other.getName());
    parameters.copyFrom(other.getParameters());
    body.copyFrom(other.getBody());
  }

  public MethodDeclaration(ExecutableElement method) {
    super(method);
    executableElement = method;
    isConstructor = ElementUtil.isConstructor(method);
    returnType.set(Type.newType(method.getReturnType()));
    name.set(new SimpleName(method));
  }

  @Override
  public Kind getKind() {
    return Kind.METHOD_DECLARATION;
  }

  public ExecutableElement getExecutableElement() {
    return executableElement;
  }

  public MethodDeclaration setExecutableElement(ExecutableElement newElement) {
    executableElement = newElement;
    return this;
  }

  public boolean isConstructor() {
    return isConstructor;
  }

  public MethodDeclaration setIsConstructor(boolean value) {
    isConstructor = value;
    return this;
  }

  public boolean hasDeclaration() {
    return hasDeclaration;
  }

  public MethodDeclaration setHasDeclaration(boolean value) {
    hasDeclaration = value;
    return this;
  }

  public boolean isUnavailable() {
    return isUnavailable;
  }

  public MethodDeclaration setUnavailable(boolean value) {
    isUnavailable = value;
    return this;
  }

  public Type getReturnType() {
    return returnType.get();
  }

  public MethodDeclaration setReturnType(Type newType) {
    returnType.set(newType);
    return this;
  }

  public SimpleName getName() {
    return name.get();
  }

  public MethodDeclaration setName(SimpleName newName) {
    name.set(newName);
    return this;
  }

  public SingleVariableDeclaration getParameter(int index) {
    return parameters.get(index);
  }

  public List<SingleVariableDeclaration> getParameters() {
    return parameters;
  }

  public Block getBody() {
    return body.get();
  }

  public MethodDeclaration setBody(Block newBody) {
    body.set(newBody);
    return this;
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

  public MethodDeclaration addParameter(SingleVariableDeclaration param) {
    parameters.add(param);
    return this;
  }

  public MethodDeclaration addParameter(int index, SingleVariableDeclaration param) {
    parameters.add(index, param);
    return this;
  }
}
