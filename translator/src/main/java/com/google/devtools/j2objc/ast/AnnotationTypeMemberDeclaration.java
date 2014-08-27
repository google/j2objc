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

/**
 * Node for an annotation type member declaration.
 */
public class AnnotationTypeMemberDeclaration extends BodyDeclaration {

  private IMethodBinding methodBinding = null;
  private ChildLink<Type> type = ChildLink.create(Type.class, this);
  private ChildLink<SimpleName> name = ChildLink.create(SimpleName.class, this);
  private ChildLink<Expression> defaultValue = ChildLink.create(Expression.class, this);

  public AnnotationTypeMemberDeclaration(
      org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration jdtNode) {
    super(jdtNode);
    methodBinding = jdtNode.resolveBinding();
    type.set((Type) TreeConverter.convert(jdtNode.getType()));
    name.set((SimpleName) TreeConverter.convert(jdtNode.getName()));
    defaultValue.set((Expression) TreeConverter.convert(jdtNode.getDefault()));
  }

  public AnnotationTypeMemberDeclaration(AnnotationTypeMemberDeclaration other) {
    super(other);
    methodBinding = other.getMethodBinding();
    type.copyFrom(other.getType());
    name.copyFrom(other.getName());
    defaultValue.copyFrom(other.getDefault());
  }

  @Override
  public Kind getKind() {
    return Kind.ANNOTATION_TYPE_MEMBER_DECLARATION;
  }

  public IMethodBinding getMethodBinding() {
    return methodBinding;
  }

  public Type getType() {
    return type.get();
  }

  public SimpleName getName() {
    return name.get();
  }

  public Expression getDefault() {
    return defaultValue.get();
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      javadoc.accept(visitor);
      annotations.accept(visitor);
      type.accept(visitor);
      name.accept(visitor);
      defaultValue.accept(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public AnnotationTypeMemberDeclaration copy() {
    return new AnnotationTypeMemberDeclaration(this);
  }
}
