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
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Array initializer node type.
 */
public class ArrayInitializer extends Expression {

  private TypeMirror typeMirror = null;

  private ChildList<Expression> expressions = ChildList.create(Expression.class, this);

  public ArrayInitializer() {}

  public ArrayInitializer(ArrayInitializer other) {
    super(other);
    typeMirror = other.getTypeMirror();
    expressions.copyFrom(other.getExpressions());
  }

  public ArrayInitializer(ITypeBinding typeBinding) {
    typeMirror = BindingConverter.getType(typeBinding);
  }

  @Override
  public Kind getKind() {
    return Kind.ARRAY_INITIALIZER;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public ArrayInitializer setTypeMirror(TypeMirror newTypeMirror) {
    typeMirror = newTypeMirror;
    return this;
  }

  public List<Expression> getExpressions() {
    return expressions;
  }

  public ArrayInitializer setExpressions(List<Expression> newExpressions) {
    expressions.replaceAll(newExpressions);
    return this;
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

  public ArrayInitializer addExpression(Expression e) {
    expressions.add(e);
    return this;
  }
}
