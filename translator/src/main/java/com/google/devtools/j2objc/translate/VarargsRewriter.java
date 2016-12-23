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

package com.google.devtools.j2objc.translate;

import com.google.common.collect.Lists;
import com.google.devtools.j2objc.ast.ArrayCreation;
import com.google.devtools.j2objc.ast.ArrayInitializer;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Rewrites the arguments of vararg method invocation nodes.
 * Ensures all ArrayInitializer nodes have ArrayCreation parents.
 *
 * @author Keith Stanger
 */
public class VarargsRewriter extends UnitTreeVisitor {

  public VarargsRewriter(CompilationUnit unit) {
    super(unit);
  }

  private void rewriteVarargs(
      ExecutableElement method, TypeMirror varargsType, List<Expression> args) {
    if (varargsType == null) {
      return;
    }
    varargsType = typeUtil.erasure(varargsType);
    int numRegularParams = method.getParameters().size() - 1;
    List<Expression> varargs = args.subList(numRegularParams, args.size());
    List<Expression> varargsCopy = Lists.newArrayList(varargs);
    varargs.clear();
    if (varargsCopy.isEmpty()) {
      args.add(new ArrayCreation(typeUtil.getArrayType(varargsType), typeUtil, 0));
    } else {
      ArrayInitializer newInit = new ArrayInitializer(typeUtil.getArrayType(varargsType));
      newInit.getExpressions().addAll(varargsCopy);
      args.add(new ArrayCreation(newInit));
    }
  }

  @Override
  public void endVisit(ArrayInitializer node) {
    if (!(node.getParent() instanceof ArrayCreation)) {
      ArrayCreation newArray = new ArrayCreation(node.getTypeMirror(), typeUtil);
      node.replaceWith(newArray);
      newArray.setInitializer(node);
    }
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    rewriteVarargs(node.getExecutableElement(), node.getVarargsType(), node.getArguments());
  }

  @Override
  public void endVisit(ConstructorInvocation node) {
    rewriteVarargs(node.getExecutableElement(), node.getVarargsType(), node.getArguments());
  }

  @Override
  public void endVisit(EnumConstantDeclaration node) {
    rewriteVarargs(node.getExecutableElement(), node.getVarargsType(), node.getArguments());
  }

  @Override
  public void endVisit(MethodInvocation node) {
    rewriteVarargs(node.getExecutableElement(), node.getVarargsType(), node.getArguments());
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    rewriteVarargs(node.getExecutableElement(), node.getVarargsType(), node.getArguments());
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    rewriteVarargs(node.getExecutableElement(), node.getVarargsType(), node.getArguments());
  }
}
