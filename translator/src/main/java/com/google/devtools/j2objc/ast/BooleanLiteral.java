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
import com.google.devtools.j2objc.types.Types;

import org.eclipse.jdt.core.dom.ITypeBinding;

import javax.lang.model.type.TypeMirror;

/**
 * Either "true" or "false".
 */
public class BooleanLiteral extends Expression {

  private boolean booleanValue = false;
  private final TypeMirror typeMirror;

  public BooleanLiteral(org.eclipse.jdt.core.dom.BooleanLiteral jdtNode) {
    super(jdtNode);
    booleanValue = jdtNode.booleanValue();
    ITypeBinding typeBinding = BindingConverter.wrapBinding(jdtNode.resolveTypeBinding());
    typeMirror = BindingConverter.getType(typeBinding);
  }

  public BooleanLiteral(BooleanLiteral other) {
    super(other);
    booleanValue = other.booleanValue();
    typeMirror = other.getTypeMirror();
  }

  public BooleanLiteral(boolean booleanValue, Types typeEnv) {
    this.booleanValue = booleanValue;
    typeMirror = BindingConverter.getType(typeEnv.resolveJavaType("boolean"));
  }

  @Override
  public Kind getKind() {
    return Kind.BOOLEAN_LITERAL;
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }
  public boolean booleanValue() {
    return booleanValue;
  }

  public void setBooleanValue(boolean newBooleanValue) {
    booleanValue = newBooleanValue;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }

  @Override
  public BooleanLiteral copy() {
    return new BooleanLiteral(this);
  }
}
