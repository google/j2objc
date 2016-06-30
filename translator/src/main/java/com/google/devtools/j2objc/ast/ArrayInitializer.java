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

import com.google.devtools.j2objc.javac.BindingConverter;

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;

import javax.lang.model.type.TypeMirror;

/**
 * Array initializer node type.
 */
public class ArrayInitializer extends Expression {

  private ITypeBinding typeBinding = null;
  private TypeMirror typeMirror = null;

  private ChildList<Expression> expressions = ChildList.create(Expression.class, this);

  public ArrayInitializer(org.eclipse.jdt.core.dom.ArrayInitializer jdtNode) {
    super(jdtNode);
    typeBinding = BindingConverter.wrapBinding(jdtNode.resolveTypeBinding());
    typeMirror = BindingConverter.getType(typeBinding);
    for (Object expression : jdtNode.expressions()) {
      expressions.add((Expression) TreeConverter.convert(expression));
    }
  }

  public ArrayInitializer(ArrayInitializer other) {
    super(other);
    typeBinding = other.getTypeBinding();
    typeMirror = other.getTypeMirror();
    expressions.copyFrom(other.getExpressions());
  }

  public ArrayInitializer(ITypeBinding typeBinding) {
    this.typeBinding = typeBinding;
    typeMirror = BindingConverter.getType(typeBinding);
  }

  @Override
  public Kind getKind() {
    return Kind.ARRAY_INITIALIZER;
  }

  @Override
  public ITypeBinding getTypeBinding() {
    return typeBinding;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public List<Expression> getExpressions() {
    return expressions;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      for (Expression expression : expressions) {
        expression.accept(visitor);
      }
    }
    visitor.endVisit(this);
  }

  @Override
  public ArrayInitializer copy() {
    return new ArrayInitializer(this);
  }
}
