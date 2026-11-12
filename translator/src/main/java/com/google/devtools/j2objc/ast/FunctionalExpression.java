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

import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/** Common supertype for functional expressions (lambdas and method references). */
public abstract class FunctionalExpression extends Expression {

  private TypeMirror typeMirror = null;
  private TypeElement typeElement = null;
  private List<TypeMirror> targetTypes = new ArrayList<>();
  private ExecutablePair descriptor = ExecutablePair.NULL;
  protected ChildLink<Expression> lambdaOuterArg = ChildLink.create(Expression.class, this);
  protected ChildList<Expression> lambdaCaptureArgs = ChildList.create(Expression.class, this);

  public FunctionalExpression() {}

  public FunctionalExpression(FunctionalExpression other) {
    super(other);
    typeMirror = other.getTypeMirror();
    typeElement = other.getTypeElement();
    targetTypes.addAll(other.getTargetTypes());
    descriptor = other.getDescriptor();
    lambdaOuterArg.copyFrom(other.getLambdaOuterArg());
    lambdaCaptureArgs.copyFrom(other.getLambdaCaptureArgs());
  }

  @Override
  public TypeMirror getTypeMirror() {
    return typeMirror;
  }

  @CanIgnoreReturnValue
  public FunctionalExpression setTypeMirror(TypeMirror t) {
    typeMirror = t;
    return this;
  }

  public TypeElement getTypeElement() {
    return typeElement;
  }

  @CanIgnoreReturnValue
  public FunctionalExpression setTypeElement(TypeElement e) {
    typeElement = e;
    return this;
  }

  public List<TypeMirror> getTargetTypes() {
    return targetTypes;
  }

  @CanIgnoreReturnValue
  public FunctionalExpression addTargetType(TypeMirror t) {
    targetTypes.add(t);
    return this;
  }

  public ExecutablePair getDescriptor() {
    return descriptor;
  }

  @CanIgnoreReturnValue
  public FunctionalExpression setDescriptor(ExecutablePair e) {
    descriptor = e;
    return this;
  }

  public Expression getLambdaOuterArg() {
    return lambdaOuterArg.get();
  }

  @CanIgnoreReturnValue
  public FunctionalExpression setLambdaOuterArg(Expression newOuterArg) {
    lambdaOuterArg.set(newOuterArg);
    return this;
  }

  public List<Expression> getLambdaCaptureArgs() {
    return lambdaCaptureArgs;
  }

  @Override
  public abstract FunctionalExpression copy();
}
