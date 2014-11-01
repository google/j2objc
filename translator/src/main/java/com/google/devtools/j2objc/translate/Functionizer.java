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
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NormalAnnotation;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleMemberAnnotation;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperFieldAccess;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;
import java.util.Set;

/**
 * Converts methods that don't need dynamic dispatch to C functions. This optimization
 * initially just targets private methods, but will be expanded to include final methods
 * that don't override superclass methods.
 *
 * @author Tom Ball
 */
public class Functionizer extends TreeVisitor {

  private Set<IMethodBinding> functionizableMethods;

  @Override
  public boolean visit(CompilationUnit node) {
    functionizableMethods = determineFunctionizableMethods(node);
    return true;
  }

  /**
   * Determines the set of methods to functionize. In addition to a method being
   * final we must also find an invocation for that method. Static methods, though,
   * are always functionized since there are no dynamic dispatch issues.
   */
  private Set<IMethodBinding> determineFunctionizableMethods(final CompilationUnit unit) {
    final Set<IMethodBinding> functionizableDeclarations = Sets.newHashSet();
    final Set<IMethodBinding> staticDeclarations = Sets.newHashSet();
    final Set<IMethodBinding> invocations = Sets.newHashSet();
    unit.accept(new TreeVisitor() {
      @Override
      public void endVisit(MethodDeclaration node) {
        IMethodBinding m = node.getMethodBinding();
        int mods = node.getModifiers();
        if (Modifier.isStatic(mods)) {
          staticDeclarations.add(m);
        } else if (canFunctionize(node)) {
          functionizableDeclarations.add(m);
        }
      }

      @Override
      public void endVisit(MethodInvocation node) {
        invocations.add(node.getMethodBinding().getMethodDeclaration());
      }
    });
    return Sets.union(staticDeclarations,
        Sets.intersection(functionizableDeclarations, invocations));
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    return false;
  }

  @Override
  public boolean visit(NormalAnnotation node) {
    return false;
  }

  @Override
  public boolean visit(SingleMemberAnnotation node) {
    return false;
  }

  private boolean canFunctionize(MethodDeclaration node) {
    IMethodBinding m = node.getMethodBinding();
    int modifiers = node.getModifiers();

    // Never functionize these types of methods.
    if (Modifier.isAbstract(modifiers) || BindingUtil.isSynthetic(modifiers)
        || m.isAnnotationMember() || m.isConstructor() || BindingUtil.isDestructor(m)
        || Modifier.isNative(modifiers)) {
      return false;
    }

    // Don't functionize equals/hash, since they are often called by collections.
    String name = m.getName();
    if ((name.equals("hashCode") && m.getParameterTypes().length == 0)
        || (name.equals("equals") && m.getParameterTypes().length == 1)) {
      return false;
    }

    if (!BindingUtil.isPrivate(m) && !BindingUtil.isFinal(m)) {
      return false;
    }

    return !hasSuperMethodInvocation(node);
  }

  private static boolean hasSuperMethodInvocation(MethodDeclaration node) {
    final boolean[] result = new boolean[1];
    result[0] = false;
    node.accept(new TreeVisitor() {
      @Override
      public void endVisit(SuperMethodInvocation node) {
        result[0] = true;
      }
    });
    return result[0];
  }

  @Override
  public void endVisit(MethodInvocation node) {
    IMethodBinding binding = node.getMethodBinding().getMethodDeclaration();
    if (!BindingUtil.isStatic(binding) && !functionizableMethods.contains(binding)) {
      return;
    }

    FunctionInvocation functionInvocation = new FunctionInvocation(
        NameTable.makeFunctionName(binding), node.getTypeBinding(), binding.getReturnType(),
        binding.getDeclaringClass());
    List<Expression> args = functionInvocation.getArguments();
    TreeUtil.moveList(node.getArguments(), args);

    if (!BindingUtil.isStatic(binding)) {
      Expression expr = node.getExpression();
      if (expr == null) {
        ITypeBinding thisClass = TreeUtil.getOwningType(node).getTypeBinding();
        expr = new ThisExpression(thisClass);
      }
      args.add(0, TreeUtil.remove(expr));
    }

    node.replaceWith(functionInvocation);
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    IMethodBinding binding = node.getMethodBinding();
    if (functionizableMethods.contains(binding)) {
      FunctionDeclaration function = BindingUtil.isStatic(binding)
          ? makeStaticFunction(node) : makeInstanceFunction(node);
      setFunctionCaller(node, function);
      List<BodyDeclaration> declarationList = TreeUtil.asDeclarationSublist(node);
      declarationList.add(function);
      ErrorUtil.functionizedMethod();
    }
  }

  /**
   * Create an equivalent function declaration for a given instance method. A
   * "self" parameter is added to the beginning of the parameter list (that way,
   * variable argument functions can be supported).
   */
  private FunctionDeclaration makeInstanceFunction(MethodDeclaration method) {
    IMethodBinding m = method.getMethodBinding();
    ITypeBinding declaringClass = m.getDeclaringClass();
    ITypeBinding[] paramTypes = m.getParameterTypes();
    List<SingleVariableDeclaration> params = TreeUtil.copyList(method.getParameters());

    List<ITypeBinding> list = Lists.newArrayList(paramTypes);
    list.add(0, m.getDeclaringClass());
    paramTypes = list.toArray(new ITypeBinding[list.size()]);
    GeneratedVariableBinding var = new GeneratedVariableBinding(NameTable.SELF_NAME, 0,
        declaringClass, false, true, declaringClass, null);
    params.add(0, new SingleVariableDeclaration(var));

    FunctionDeclaration function = new FunctionDeclaration(
        NameTable.makeFunctionName(m), m.getReturnType());
    function.getParameters().addAll(params);
    function.setModifiers(Modifier.PRIVATE);

    function.setBody(TreeUtil.remove(method.getBody()));

    FunctionConverter.convert(function);
    return function;
  }

  private FunctionDeclaration makeStaticFunction(MethodDeclaration method) {
    IMethodBinding m = method.getMethodBinding();
    ITypeBinding declaringClass = m.getDeclaringClass();
    ITypeBinding[] paramTypes = m.getParameterTypes();
    List<SingleVariableDeclaration> params = TreeUtil.copyList(method.getParameters());

    FunctionDeclaration function = new FunctionDeclaration(
        NameTable.makeFunctionName(m), m.getReturnType());
    function.getParameters().addAll(params);
    int access = BindingUtil.isPrivate(m) ? Modifier.PRIVATE : Modifier.PUBLIC;
    function.setModifiers(access);

    if (Modifier.isNative(method.getModifiers())) {
      function.addModifiers(Modifier.NATIVE);
      return function;
    }
    function.setBody(TreeUtil.remove(method.getBody()));

    // Add class initialization invocation, since this may be the first use of this class.
    String initName = String.format("%s_init", NameTable.getFullName(declaringClass));
    ITypeBinding voidType = Types.resolveJavaType("void");
    FunctionInvocation initCall =
        new FunctionInvocation(initName, voidType, voidType, declaringClass);
    function.getBody().getStatements().add(0, new ExpressionStatement(initCall));
    return function;
  }

  /**
   *  Replace method block statements with single statement that invokes function.
   */
  private void setFunctionCaller(MethodDeclaration method, FunctionDeclaration function) {
    IMethodBinding methodBinding = method.getMethodBinding();
    ITypeBinding returnType = function.getReturnType().getTypeBinding();
    Block body = new Block();
    method.setBody(body);
    method.removeModifiers(Modifier.NATIVE);
    List<Statement> stmts = body.getStatements();
    stmts.clear();
    FunctionInvocation invocation = new FunctionInvocation(
        function.getName(), returnType, returnType, methodBinding.getDeclaringClass());
    List<Expression> args = invocation.getArguments();
    if (!BindingUtil.isStatic(methodBinding)) {
      args.add(new ThisExpression(methodBinding.getDeclaringClass()));
    }
    for (SingleVariableDeclaration param : method.getParameters()) {
      args.add(new SimpleName(param.getVariableBinding()));
    }
    if (Types.isVoidType(returnType)) {
      stmts.add(new ExpressionStatement(invocation));
    } else {
      stmts.add(new ReturnStatement(invocation));
    }
  }

  /**
   * Convert references to "this" in the function to a "self" parameter.
   */
  private static class FunctionConverter extends TreeVisitor {

    private final IVariableBinding selfParam;

    static void convert(FunctionDeclaration function) {
      IVariableBinding selfParam = function.getParameters().get(0).getVariableBinding();
      function.accept(new FunctionConverter(selfParam));
    }

    private FunctionConverter(IVariableBinding selfParam) {
      this.selfParam = selfParam;
    }

    @Override
    public boolean visit(FieldAccess node) {
      node.getExpression().accept(this);
      return false;
    }

    @Override
    public boolean visit(QualifiedName node) {
      node.getQualifier().accept(this);
      return false;
    }

    @Override
    public void endVisit(SimpleName node) {
      IVariableBinding var = TreeUtil.getVariableBinding(node);
      if (var != null && var.isField()) {
        // Convert name to self->name.
        node.replaceWith(new QualifiedName(var, new SimpleName(selfParam)));
      }
    }

    @Override
    public boolean visit(SuperFieldAccess node) {
      // Change super.field expression to self.field.
      SimpleName qualifier = new SimpleName(selfParam);
      FieldAccess newAccess = new FieldAccess(node.getVariableBinding(), qualifier);
      node.replaceWith(newAccess);
      return false;
    }

    @Override
    public void endVisit(ThisExpression node) {
      SimpleName self = new SimpleName(selfParam);
      node.replaceWith(self);
    }
  }
}
