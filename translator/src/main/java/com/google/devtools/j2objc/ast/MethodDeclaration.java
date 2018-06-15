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

import com.google.common.base.Preconditions;
import com.google.devtools.j2objc.util.ElementUtil;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Node type for a method declaration.
 */
public class MethodDeclaration extends BodyDeclaration {

  private ExecutableElement executableElement = null;
  private Name name = null;
  private boolean isConstructor = false;
  private boolean hasDeclaration = true;
  private boolean isUnavailable = false;
  private ChildList<SingleVariableDeclaration> parameters =
      ChildList.create(SingleVariableDeclaration.class, this);
  private ChildLink<Block> body = ChildLink.create(Block.class, this);

  public MethodDeclaration() {}

  public MethodDeclaration(MethodDeclaration other) {
    super(other);
    executableElement = other.getExecutableElement();
    name = other.getName();
    isConstructor = other.isConstructor();
    hasDeclaration = other.hasDeclaration();
    isUnavailable = other.isUnavailable();
    parameters.copyFrom(other.getParameters());
    body.copyFrom(other.getBody());
  }

  public MethodDeclaration(ExecutableElement method) {
    super(method);
    executableElement = method;
    isConstructor = ElementUtil.isConstructor(method);
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

  public Name getName() {
    return name != null
        ? name
        : (executableElement != null ? Name.newName(null, executableElement) : null);
  }

  public MethodDeclaration setName(Name newName) {
    name = newName;
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

  public TypeMirror getReturnTypeMirror() {
    return executableElement.getReturnType();
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

  @Override
  public void validateInner() {
    super.validateInner();
    Preconditions.checkNotNull(executableElement);
  }
}
