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
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.TreeVisitor;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.List;

/**
 * Rewrites the arguments of vararg method invocation nodes.
 * Ensures all ArrayInitializer nodes have ArrayCreation parents.
 *
 * @author Keith Stanger
 */
public class VarargsRewriter extends TreeVisitor {

  private void rewriteVarargs(IMethodBinding method, List<Expression> args) {
    if (!method.isVarargs()) {
      return;
    }
    ITypeBinding[] paramTypes = method.getParameterTypes();
    ITypeBinding lastParam = paramTypes[paramTypes.length - 1];
    assert lastParam.isArray();
    int varargsSize = args.size() - paramTypes.length + 1;
    if (varargsSize == 1) {
      Expression lastArg = args.get(args.size() - 1);
      ITypeBinding lastArgType = lastArg.getTypeBinding();
      if (lastArgType.isAssignmentCompatible(lastParam)) {
        // Last argument is already an array.
        return;
      }
      // Special case: check for a clone method invocation, since clone()'s return
      // type is declared as Object but it always returns the caller's type.
      if (lastArg instanceof MethodInvocation) {
        MethodInvocation invocation = (MethodInvocation) lastArg;
        if (invocation.getMethodBinding().getName().equals("clone")
            && invocation.getArguments().isEmpty()
            && invocation.getExpression().getTypeBinding().isAssignmentCompatible(lastParam)) {
          return;
        }
      }
    }

    List<Expression> varargs = args.subList(paramTypes.length - 1, args.size());
    List<Expression> varargsCopy = Lists.newArrayList(varargs);
    varargs.clear();
    if (varargsCopy.isEmpty()) {
      args.add(new ArrayCreation(lastParam.getErasure(), typeEnv, 0));
    } else {
      ArrayInitializer newInit = new ArrayInitializer(lastParam.getErasure());
      newInit.getExpressions().addAll(varargsCopy);
      args.add(new ArrayCreation(newInit));
    }
  }

  @Override
  public void endVisit(ArrayInitializer node) {
    if (!(node.getParent() instanceof ArrayCreation)) {
      ArrayCreation newArray = new ArrayCreation(node.getTypeBinding(), typeEnv);
      node.replaceWith(newArray);
      newArray.setInitializer(node);
    }
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    rewriteVarargs(node.getMethodBinding(), node.getArguments());
  }

  @Override
  public void endVisit(ConstructorInvocation node) {
    rewriteVarargs(node.getMethodBinding(), node.getArguments());
  }

  @Override
  public void endVisit(EnumConstantDeclaration node) {
    rewriteVarargs(node.getMethodBinding(), node.getArguments());
  }

  @Override
  public void endVisit(MethodInvocation node) {
    rewriteVarargs(node.getMethodBinding(), node.getArguments());
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    rewriteVarargs(node.getMethodBinding(), node.getArguments());
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    rewriteVarargs(node.getMethodBinding(), node.getArguments());
  }
}
