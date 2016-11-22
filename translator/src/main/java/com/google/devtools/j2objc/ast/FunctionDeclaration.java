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

import java.util.List;
import javax.lang.model.type.TypeMirror;

/**
 * Node type for a function declaration.
 */
public class FunctionDeclaration extends BodyDeclaration {

  private String name = null;
  private boolean returnsRetained = false;
  private final ChildLink<Type> returnType = ChildLink.create(Type.class, this);
  private final ChildList<SingleVariableDeclaration> parameters =
      ChildList.create(SingleVariableDeclaration.class, this);
  private final ChildLink<Block> body = ChildLink.create(Block.class, this);
  private String jniSignature = null;

  public FunctionDeclaration(FunctionDeclaration other) {
    super(other);
    name = other.getName();
    returnsRetained = other.returnsRetained();
    returnType.copyFrom(other.getReturnType());
    parameters.copyFrom(other.getParameters());
    body.copyFrom(other.getBody());
    jniSignature = other.jniSignature;
  }

  public FunctionDeclaration(String name, TypeMirror returnType) {
    this.name = name;
    this.returnType.set(Type.newType(returnType));
  }

  @Override
  public Kind getKind() {
    return Kind.FUNCTION_DECLARATION;
  }

  public String getName() {
    return name;
  }

  public boolean returnsRetained() {
    return returnsRetained;
  }

  public void setReturnsRetained(boolean value) {
    returnsRetained = value;
  }

  public Type getReturnType() {
    return returnType.get();
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

  public void setBody(Block newBody) {
    body.set(newBody);
  }

  public String getJniSignature() {
    return jniSignature;
  }

  public void setJniSignature(String s) {
    this.jniSignature = s;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      javadoc.accept(visitor);
      annotations.accept(visitor);
      returnType.accept(visitor);
      parameters.accept(visitor);
      body.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public FunctionDeclaration copy() {
    return new FunctionDeclaration(this);
  }

  public FunctionDeclaration addParameter(SingleVariableDeclaration param) {
    parameters.add(param);
    return this;
  }
}
