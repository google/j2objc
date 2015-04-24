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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclaration;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.util.BindingUtil;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;

/**
 * Writes the __javaClone method in order to support correct Java clone()
 * behavior.
 *
 * @author Keith Stanger
 */
public class JavaCloneWriter extends TreeVisitor {

  private static final String JAVA_CLONE_METHOD = "__javaClone";

  private static final Function<VariableDeclaration, IVariableBinding> GET_VARIABLE_BINDING_FUNC =
      new Function<VariableDeclaration, IVariableBinding>() {
    public IVariableBinding apply(VariableDeclaration node) {
      return node.getVariableBinding();
    }
  };

  private static final Predicate<IVariableBinding> IS_WEAK_PRED =
      new Predicate<IVariableBinding>() {
    public boolean apply(IVariableBinding binding) {
      return !BindingUtil.isStatic(binding) && BindingUtil.isWeakReference(binding);
    }
  };

  @Override
  public void endVisit(TypeDeclaration node) {
    ITypeBinding type = node.getTypeBinding();
    List<IVariableBinding> fields = getWeakFields(node);
    if (fields.isEmpty()) {
      return;
    }

    ITypeBinding voidType = typeEnv.resolveJavaType("void");
    int modifiers = Modifier.PUBLIC | BindingUtil.ACC_SYNTHETIC;
    IOSMethodBinding methodBinding = IOSMethodBinding.newMethod(
        JAVA_CLONE_METHOD, modifiers, voidType, type);

    MethodDeclaration declaration = new MethodDeclaration(methodBinding);
    node.getBodyDeclarations().add(declaration);

    Block body = new Block();
    declaration.setBody(body);
    List<Statement> statements = body.getStatements();

    ITypeBinding nsObjectType = typeEnv.resolveIOSType("NSObject");
    IOSMethodBinding cloneMethod = IOSMethodBinding.newMethod(
        JAVA_CLONE_METHOD, Modifier.PUBLIC, voidType, nsObjectType);
    SuperMethodInvocation superCall = new SuperMethodInvocation(cloneMethod);
    statements.add(new ExpressionStatement(superCall));

    for (IVariableBinding field : fields) {
      if (Options.useARC()) {
        FunctionInvocation invocation = new FunctionInvocation(
            "JreRelease", voidType, voidType, voidType);
        invocation.getArguments().add(new SimpleName(field));
        statements.add(new ExpressionStatement(invocation));
      } else {
        statements.add(new ExpressionStatement(
            new MethodInvocation(typeEnv.getReleaseMethod(), new SimpleName(field))));
      }
    }
  }

  private static List<IVariableBinding> getWeakFields(TypeDeclaration node) {
    return Lists.newArrayList(Iterables.filter(
        Iterables.transform(TreeUtil.getAllFields(node), GET_VARIABLE_BINDING_FUNC), IS_WEAK_PRED));
  }
}
