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
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import java.util.List;
import javax.lang.model.type.ArrayType;
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

  private void rewriteVarargs(ExecutablePair method, List<Expression> args) {
    if (!method.element().isVarArgs()) {
      return;
    }
    List<? extends TypeMirror> paramTypes = method.type().getParameterTypes();
    TypeMirror varargType = paramTypes.get(paramTypes.size() - 1);
    assert TypeUtil.isArray(varargType);
    int varargsSize = args.size() - paramTypes.size() + 1;
    if (varargsSize == 1) {
      Expression lastArg = args.get(args.size() - 1);
      TypeMirror lastArgType = lastArg.getTypeMirror();
      if (typeUtil.isAssignable(lastArgType, varargType)) {
        // Last argument is already an array.
        return;
      }
      // Special case: check for a clone method invocation, since clone()'s return
      // type is declared as Object but it always returns the caller's type.
      if (lastArg instanceof MethodInvocation) {
        MethodInvocation invocation = (MethodInvocation) lastArg;
        if (ElementUtil.getName(invocation.getExecutableElement()).equals("clone")
            && invocation.getArguments().isEmpty()
            && typeUtil.isAssignable(invocation.getExpression().getTypeMirror(), varargType)) {
          return;
        }
      }
    }

    List<Expression> varargs = args.subList(paramTypes.size() - 1, args.size());
    List<Expression> varargsCopy = Lists.newArrayList(varargs);
    varargs.clear();
    if (varargsCopy.isEmpty()) {
      args.add(new ArrayCreation((ArrayType) typeUtil.erasure(varargType), typeEnv, 0));
    } else {
      ArrayInitializer newInit = new ArrayInitializer((ArrayType) typeUtil.erasure(varargType));
      newInit.getExpressions().addAll(varargsCopy);
      args.add(new ArrayCreation(newInit));
    }
  }

  @Override
  public void endVisit(ArrayInitializer node) {
    if (!(node.getParent() instanceof ArrayCreation)) {
      ArrayCreation newArray = new ArrayCreation(node.getTypeMirror(), typeEnv);
      node.replaceWith(newArray);
      newArray.setInitializer(node);
    }
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    rewriteVarargs(node.getExecutablePair(), node.getArguments());
  }

  @Override
  public void endVisit(ConstructorInvocation node) {
    rewriteVarargs(node.getExecutablePair(), node.getArguments());
  }

  @Override
  public void endVisit(EnumConstantDeclaration node) {
    rewriteVarargs(node.getExecutablePair(), node.getArguments());
  }

  @Override
  public void endVisit(MethodInvocation node) {
    rewriteVarargs(node.getExecutablePair(), node.getArguments());
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    rewriteVarargs(node.getExecutablePair(), node.getArguments());
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    rewriteVarargs(node.getExecutablePair(), node.getArguments());
  }
}
