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
import com.google.common.collect.Maps;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.NormalAnnotation;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleMemberAnnotation;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperFieldAccess;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.SynchronizedStatement;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.types.GeneratedMethodBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Converts methods that don't need dynamic dispatch to C functions. This optimization
 * initially just targets private methods, but will be expanded to include final methods
 * that don't override superclass methods.
 *
 * @author Tom Ball
 */
public class Functionizer extends TreeVisitor {
  private Stack<MethodDeclaration> methodStack = new Stack<MethodDeclaration>();
  private Map<IMethodBinding, Integer> referenceCounts = Maps.newHashMap();

  // Map each functionalized method to its function.
  private Map<IMethodBinding, IMethodBinding> functionMap = Maps.newHashMap();

  @Override
  public boolean visit(CompilationUnit node) {
    determineReferenceCounts(node);
    functionize(node);
    return true;
  }

  /**
   * Determine how often each method in this node is invoked within this node.
   * This is used to determine whether or not to functionize the method, since
   * a private class can have public methods that aren't invoked if it implements
   * an interface (a private implementation of List, for example).
   */
  private void determineReferenceCounts(CompilationUnit node) {
    final List<IMethodBinding> invalidMethods = Lists.newArrayList();
    node.accept(new TreeVisitor() {
      @Override
      public void endVisit(MethodDeclaration node) {
        IMethodBinding m = node.getMethodBinding();
        if (!BindingUtil.isFunction(m) && !referenceCounts.containsKey(m)) {
          referenceCounts.put(m, 0);
        }
      }

      @Override
      public void endVisit(MethodInvocation node) {
        IMethodBinding m = getDeclaration(node.getMethodBinding());
        if (!BindingUtil.isFunction(m)) {
          Integer n = referenceCounts.get(m);
          if (n == null) {
            n = 0;
          }
          referenceCounts.put(m, n + 1);
        }
      }

      @Override
      public void endVisit(SuperMethodInvocation node) {
        MethodDeclaration method = TreeUtil.getOwningMethod(node);
        invalidMethods.add(getDeclaration(method.getMethodBinding()));
      }
    });
    for (IMethodBinding m : invalidMethods) {
      referenceCounts.remove(m);
    }
  }

  private void functionize(CompilationUnit unit) {
    for (AbstractTypeDeclaration decl : unit.getTypes()) {
      if (decl instanceof TypeDeclaration) {
        functionizeType((TypeDeclaration) decl);
      }
    }
  }

  private void functionizeType(TypeDeclaration node) {
    List<MethodDeclaration> functions = Lists.newArrayList();
    for (MethodDeclaration method : TreeUtil.getMethodDeclarations(node)) {
      IMethodBinding m = method.getMethodBinding();
      if (canFunctionize(m)) {
        MethodDeclaration function = makeFunction(method);
        setFunctionCaller(method, function);
        functions.add(function);
        functionMap.put(m, function.getMethodBinding());
        ErrorUtil.functionizedMethod();
      }
    }
    node.getBodyDeclarations().addAll(functions);

    for (BodyDeclaration decl : node.getBodyDeclarations()) {
      if (decl instanceof TypeDeclaration) {
        functionizeType((TypeDeclaration) decl);
      }
    }
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    return false;
  }

  @Override
  public boolean visit(EnumDeclaration node) {
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

  private boolean canFunctionize(IMethodBinding m) {
    m = getDeclaration(m);

    // Never functionize these types of methods.
    if (BindingUtil.isFunction(m) || BindingUtil.isAbstract(m) || BindingUtil.isSynthetic(m)
        || m.isAnnotationMember() || m.isConstructor() || BindingUtil.isDestructor(m)) {
      return false;
    }

    // Don't functionize equals/hash, since they are often called by collections.
    String name = m.getName();
    if ((name.equals("hashCode") && m.getParameterTypes().length == 0)
        || (name.equals("equals") && m.getParameterTypes().length == 1)) {
      return false;
    }

    if (BindingUtil.isPrivate(m) || BindingUtil.isPrivate(m.getDeclaringClass())) {
      // Only functionize if the method is invoked from within the outer class or any
      // of its member classes.
      Integer n = referenceCounts.get(m);
      return n == null ? false : n.intValue() > 0;
    }
    return false;
  }

  @Override
  public void endVisit(MethodInvocation node) {
    IMethodBinding binding = node.getMethodBinding();
    ITypeBinding declaringClass = binding.getDeclaringClass();
    if (declaringClass == null) {
      // Unrelated function, such as address_of: skip.
      return;
    }
    List<Expression> args = null;
    IMethodBinding functionBinding = functionMap.get(binding);
    if (functionBinding == null && BindingUtil.isFunction(binding)) {
      functionBinding = binding;
    }
    boolean isFunction = functionBinding != null;
    if (isFunction) {
      MethodInvocation functionInvocation = new MethodInvocation(functionBinding, null);
      args = functionInvocation.getArguments();
      TreeUtil.moveList(node.getArguments(), args);
      node.replaceWith(functionInvocation);
    } else {
      args = node.getArguments();
    }

    MethodDeclaration enclosingMethod = methodStack.peek();
    IMethodBinding enclosingBinding = enclosingMethod.getMethodBinding();
    Expression expr = node.getExpression();
    boolean isInstance = !BindingUtil.isStatic(binding);
    if (isFunction && isInstance) {
      boolean needsReceiver = expr == null || expr instanceof ThisExpression;
      if (BindingUtil.isFunction(enclosingBinding)
          && !BindingUtil.isStatic(enclosingBinding) && needsReceiver) {
        // Add self parameter.
        GeneratedVariableBinding selfParam = new GeneratedVariableBinding(NameTable.SELF_NAME,
            binding.getModifiers() | BindingUtil.ACC_SYNTHETIC, declaringClass, false, true,
            declaringClass, null);
        args.add(0, new SimpleName(selfParam));
      } else {
        boolean needsInstanceParam = isInstance && needsReceiver;
        if (sameClassMember(declaringClass, getEnclosingType(enclosingMethod))
            && needsInstanceParam) {
          // Add this parameter.
          args.add(0, new ThisExpression(declaringClass));
        } else if (expr != null) {
          // Move expression to first parameter. The expression has to be to an outer
          // class instance (i.e., this$0), since the method is private.
          Expression thisParam = expr;
          node.setExpression(null);
          args.add(0, thisParam);
        }
      }
    } else {
      if (!BindingUtil.isStatic(binding) && !BindingUtil.isStatic(enclosingBinding)
          && BindingUtil.isFunction(enclosingBinding)) {
        // Dynamic method invocation inside function.
        Expression receiver = expr;
        if ((receiver == null || receiver instanceof ThisExpression)) {
          // Change message receiver to self.
          IVariableBinding selfParam = enclosingMethod.getParameters().get(0).getVariableBinding();
          node.setExpression(new SimpleName(selfParam));
        }
      }
    }
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    methodStack.push(node);
    return true;
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    methodStack.pop();
  }

  /**
   * Create an equivalent function declaration for a given method. If it's an instance
   * method, a "self" parameter is added to the beginning of the parameter list (that
   * way, variable functions can be supported).
   */
  private MethodDeclaration makeFunction(MethodDeclaration method) {
    IMethodBinding m = method.getMethodBinding();
    ITypeBinding declaringClass = m.getDeclaringClass();
    ITypeBinding[] paramTypes = m.getParameterTypes();
    List<SingleVariableDeclaration> params = TreeUtil.copyList(method.getParameters());
    if (!BindingUtil.isStatic(m)) {
      List<ITypeBinding> list = Lists.newArrayList(paramTypes);
      list.add(0, m.getDeclaringClass());
      paramTypes = list.toArray(new ITypeBinding[list.size()]);
      GeneratedVariableBinding var = new GeneratedVariableBinding(NameTable.SELF_NAME, 0,
          declaringClass, false, true, declaringClass, null);
      params.add(0, new SingleVariableDeclaration(var));
    }
    String functionName = NameTable.makeFunctionName(declaringClass, m);
    IOSMethodBinding newBinding =
        IOSMethodBinding.newFunction(m, functionName, paramTypes);
    MethodDeclaration function = new MethodDeclaration(newBinding);
    function.getParameters().addAll(params);

    if (BindingUtil.isNative(m)) {
      // Add body to method, for forwarding function invocation.
      method.setBody(new Block());
      method.removeModifiers(Modifier.NATIVE);

      // Make method binding non-native, now that functionBinding copied its modifiers.
      GeneratedMethodBinding gennedBinding = new GeneratedMethodBinding(m);
      newBinding.setModifiers(m.getModifiers() & ~Modifier.NATIVE);
      method.setMethodBinding(gennedBinding);
      m = gennedBinding;

      // Set source positions, so function's native code can be extracted.
      function.setSourceRange(method.getStartPosition(), method.getLength());
    }

    if (BindingUtil.isSynchronized(m)) {
      SynchronizedStatement syncStmt = new SynchronizedStatement();
      if (BindingUtil.isStatic(m)) {
        syncStmt.setExpression(new TypeLiteral(declaringClass));
      } else {
        GeneratedVariableBinding selfParam = new GeneratedVariableBinding(NameTable.SELF_NAME, 0,
            declaringClass, false, true, declaringClass, null);
        SimpleName self = new SimpleName(selfParam);
        syncStmt.setExpression(self);
      }
      TreeUtil.copyList(method.getBody().getStatements(), syncStmt.getBody().getStatements());
      Block block = new Block();
      block.getStatements().add(syncStmt);
      function.setBody(block);
      function.removeModifiers(Modifier.SYNCHRONIZED);
    } else {
      function.setBody(method.getBody().copy());
    }

    if (BindingUtil.isStatic(m)) {
      // Add class initialization invocation, since this may be the first use of this class.
      String initName = String.format("%s_init", NameTable.getFullName(declaringClass));
      IOSMethodBinding initBinding =
          IOSMethodBinding.newFunction(initName, Types.resolveJavaType("void"), declaringClass);
      MethodInvocation initCall = new MethodInvocation(initBinding, null);
      function.getBody().getStatements().add(0, new ExpressionStatement(initCall));
    }
    FunctionConverter.convert(function, newBinding);
    return function;
  }

  /**
   *  Replace method block statements with single statement that invokes function.
   */
  private void setFunctionCaller(MethodDeclaration method, MethodDeclaration function) {
    IMethodBinding functionBinding = function.getMethodBinding();
    List<Statement> stmts = method.getBody().getStatements();
    stmts.clear();
    MethodInvocation invocation = new MethodInvocation(functionBinding, null);
    List<Expression> args = invocation.getArguments();
    for (SingleVariableDeclaration param : method.getParameters()) {
      args.add(new SimpleName(param.getVariableBinding()));
    }
    if (Types.isVoidType(functionBinding.getReturnType())) {
      stmts.add(new ExpressionStatement(invocation));
    } else {
      stmts.add(new ReturnStatement(invocation));
    }
  }

  /**
   *  Check if binding is to an instance method or field in the same class as the current method.
   */
  private static boolean sameClassMember(ITypeBinding declaringClass, ITypeBinding enclosingType) {
    if (declaringClass == null) {
      return false;
    }
    if (enclosingType.isEqualTo(declaringClass)) {
      return true;
    }
    return enclosingType.getTypeDeclaration().isEqualTo(declaringClass.getTypeDeclaration());
  }

  private static ITypeBinding getEnclosingType(MethodDeclaration md) {
    return TreeUtil.getOwningType(md).getTypeBinding().getErasure();
  }

  /**
   * Returns method declaration if declared in binding class, no superclass or
   * interface declarations returned.
   */
  private IMethodBinding getDeclaration(IMethodBinding m) {
    if (BindingUtil.isFunction(m)) {
      return m;
    }
    IMethodBinding decl = m.getMethodDeclaration();
    return m.getDeclaringClass().isEqualTo(decl.getDeclaringClass()) ? decl : m;
  }

  /**
   * Convert references to "this" in the function to a "self" parameter.
   */
  private static class FunctionConverter extends TreeVisitor {
    private final IMethodBinding binding;
    private final IVariableBinding selfParam;

    static void convert(MethodDeclaration function, IMethodBinding binding) {
      if (!BindingUtil.isStatic(binding)) {
        IVariableBinding selfParam = function.getParameters().get(0).getVariableBinding();
        function.accept(new FunctionConverter(binding, selfParam));
      }
    }

    private FunctionConverter(IMethodBinding binding, IVariableBinding selfParam) {
      this.binding = binding;
      this.selfParam = selfParam;
    }

    @Override
    public void endVisit(FieldAccess node) {
      Expression receiver = node.getExpression();
      SimpleName selfNode = new SimpleName(selfParam);
      if (receiver == null || receiver instanceof ThisExpression) {
        // Change field expression to this$.
        node.setExpression(selfNode);
      } else {
        IVariableBinding var = node.getVariableBinding();
        if (isField(receiver) && receiver instanceof SimpleName
            && !sameClassMember(var.getDeclaringClass(), binding.getDeclaringClass())) {
          // Change outer field expression to this$->expression.
          node.setExpression(new QualifiedName(((SimpleName) receiver).getBinding(), selfNode));
        }
      }
    }

    @Override
    public boolean visit(QualifiedName node) {
      Name qual = node.getQualifier();
      IBinding qualBinding = qual.getBinding();
      if (qualBinding instanceof IVariableBinding && !BindingUtil.isStatic(qualBinding)) {
        IVariableBinding var = (IVariableBinding) qualBinding;
        if (var.isField()) {
          if (!BindingUtil.isStatic(binding)) {
            FieldAccess qualFieldAccess = new FieldAccess(var, new SimpleName(selfParam));
            FieldAccess fieldAccess =
                new FieldAccess((IVariableBinding) node.getBinding(), qualFieldAccess);
            node.replaceWith(fieldAccess);
          }
        }
      }
      return true;
    }

    @Override
    public void endVisit(SimpleName node) {
      IBinding binding = node.getBinding();
      if (binding instanceof IVariableBinding && !BindingUtil.isStatic(binding)
          && !(node.getParent() instanceof FieldAccess)
          && !(node.getParent() instanceof SuperFieldAccess)
          && !(node.getParent() instanceof QualifiedName)) {
        IVariableBinding var = (IVariableBinding) binding;
        if (!var.isField() || BindingUtil.isConstant(var)) {
          return;
        }
        // Convert name to self->name.
        SimpleName qualifier = new SimpleName(selfParam);
        QualifiedName fqn = new QualifiedName(binding, qualifier);
        node.replaceWith(fqn);
      }
    }

    @Override
    public void endVisit(SuperFieldAccess node) {
      // Change super.field expression to self.field.
      SimpleName qualifier = new SimpleName(selfParam);
      FieldAccess newAccess = new FieldAccess(node.getVariableBinding(), qualifier);
      node.replaceWith(newAccess);
    }

    @Override
    public void endVisit(ThisExpression node) {
      ITypeBinding declaringClass = binding.getDeclaringClass();
      GeneratedVariableBinding selfParam = new GeneratedVariableBinding(NameTable.SELF_NAME, 0,
          declaringClass, false, true, declaringClass, null);
      SimpleName self = new SimpleName(selfParam);
      node.replaceWith(self);
    }

    private boolean isField(Expression receiver) {
      IVariableBinding variableBinding = TreeUtil.getVariableBinding(receiver);
      return variableBinding != null && variableBinding.isField();
    }
  }
}
