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

import com.google.devtools.j2objc.util.TypeUtil;
import javax.lang.model.type.TypeMirror;

/**
 * Node type for string literals.
 */
public class ExcludedClassAccess extends Expression {

  private TypeMirror typeMirror;

  public ExcludedClassAccess() {}

  public ExcludedClassAccess(ExcludedClassAccess other) {
    super(other);
    this.typeMirror = other.getTypeMirror();
  }

  public ExcludedClassAccess(String literalValue, TypeMirror type) {
    this.constantValue = literalValue;
    this.typeMirror = type;
  }

  public ExcludedClassAccess(String literalValue, TypeUtil typeUtil) {
    this(literalValue, typeUtil.getJavaString().asType());
  }

  @Override
  public Kind getKind() {
    return Kind.LINE_COMMENT;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  public ExcludedClassAccess setTypeMirror(TypeMirror newType) {
    typeMirror = newType;
    return this;
  }

  @Override
  public ExcludedClassAccess setConstantValue(Object value) {
    assert value == null || value instanceof String;
    return (ExcludedClassAccess) super.setConstantValue(value);
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }

@Override
public Expression copy() {
	// TODO Auto-generated method stub
	return null;
}

}
