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
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.Iterator;
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
public class Functionizer extends ErrorReportingASTVisitor {
  private Stack<MethodDeclaration> methodStack = new Stack<MethodDeclaration>();

  // Map each functionalized method to its function.
  private Map<IMethodBinding, IMethodBinding> functionMap = Maps.newHashMap();

  private static final int SYNTHETIC = 0x1000;

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    return false;
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    // TODO(tball): add enum support.
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

  @Override
  public boolean visit(TypeDeclaration node) {
    List<MethodDeclaration> functions = Lists.newArrayList();
    for (MethodDeclaration method : ASTUtil.getMethodDeclarations(node)) {
      IMethodBinding m = Types.getMethodBinding(method);
      if (canFunctionize(m)) {
        MethodDeclaration function = makeFunction(method, m);
        setFunctionCaller(method, function);
        functions.add(function);
        functionMap.put(m, Types.getMethodBinding(function));
        ErrorUtil.functionizedMethod();
      }
    }
    ASTUtil.getBodyDeclarations(node).addAll(functions);
    return true;
  }

  private boolean canFunctionize(IMethodBinding m) {
    return BindingUtil.isPrivate(m) && !BindingUtil.isFunction(m) && !BindingUtil.isAbstract(m) &&
        !BindingUtil.isNative(m) && !m.isConstructor() && !m.isAnnotationMember();
  }

  @Override
  public void endVisit(MethodInvocation node) {
    IMethodBinding binding = Types.getMethodBinding(node);
    ITypeBinding declaringClass = binding.getDeclaringClass();
    if (declaringClass == null) {
      // Unrelated function, such as address_of: skip.
      return;
    }
    AST ast = node.getAST();
    List<Expression> args = null;
    IMethodBinding functionBinding = functionMap.get(binding);
    if (functionBinding == null && BindingUtil.isFunction(binding)) {
      functionBinding = binding;
    }
    boolean isFunction = functionBinding != null;
    if (isFunction) {
      MethodInvocation functionInvocation =
          ASTFactory.newMethodInvocation(ast, functionBinding, null);
      args = ASTUtil.getArguments(functionInvocation);
      args.addAll(NodeCopier.copySubtrees(ast, ASTUtil.getArguments(node)));
      ASTUtil.setProperty(node, functionInvocation);
    } else {
      args = ASTUtil.getArguments(node);
    }

    MethodDeclaration enclosingMethod = methodStack.peek();
    IMethodBinding enclosingBinding = Types.getMethodBinding(enclosingMethod);
    Expression expr = node.getExpression();
    boolean isInstance = !BindingUtil.isStatic(binding);
    if (isFunction && isInstance) {
      boolean needsReceiver = expr == null || expr instanceof ThisExpression;
      if (BindingUtil.isFunction(enclosingBinding) &&
          !BindingUtil.isStatic(enclosingBinding) && needsReceiver) {
        // Add self parameter.
        GeneratedVariableBinding selfParam = new GeneratedVariableBinding("self",
            binding.getModifiers() | SYNTHETIC, declaringClass, false, true, declaringClass, null);
        args.add(0, ASTFactory.newSimpleName(node.getAST(), selfParam));
      } else {
        boolean needsInstanceParam = isInstance && needsReceiver;
        if (sameClassMember(binding, declaringClass, getEnclosingType(enclosingMethod)) &&
            needsInstanceParam) {
          // Add this parameter.
          ThisExpression thisExpr = ASTFactory.newThisExpression(node.getAST(), declaringClass);
          args.add(0, thisExpr);
        } else if (expr != null) {
          // Move expression to first parameter. The expression has to be to an outer
          // class instance (i.e., this$0), since the method is private.
          Expression thisParam = expr;
          node.setExpression(null);
          args.add(0, thisParam);
        }
      }
    } else {
      if (!BindingUtil.isStatic(binding) && !BindingUtil.isStatic(enclosingBinding) &&
          BindingUtil.isFunction(enclosingBinding)) {
        // Dynamic method invocation inside function.
        Expression receiver = expr;
        if ((receiver == null || receiver instanceof ThisExpression)) {
          // Change message receiver to self.
          IVariableBinding selfParam =
              Types.getVariableBinding(ASTUtil.getParameters(enclosingMethod).get(0));
          node.setExpression(ASTFactory.newSimpleName(node.getAST(), selfParam));
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

  @Override
  public void endVisit(SuperMethodInvocation node) {
    IMethodBinding binding = Types.getMethodBinding(node);
    ITypeBinding declaringClass = binding.getDeclaringClass();
    if (declaringClass == null) {
      // Unrelated function, such as address_of: skip.
      return;
    }
    AST ast = node.getAST();
    List<Expression> args = null;
    IMethodBinding functionBinding = functionMap.get(binding);
    if (functionBinding == null && BindingUtil.isFunction(binding)) {
      functionBinding = binding;
    }
    boolean isFunction = functionBinding != null;
    if (isFunction) {
      MethodInvocation functionInvocation =
          ASTFactory.newMethodInvocation(ast, functionBinding, null);
      args = ASTUtil.getArguments(functionInvocation);
      args.addAll(NodeCopier.copySubtrees(ast, ASTUtil.getArguments(node)));
      ASTUtil.setProperty(node, functionInvocation);
    } else {
      args = ASTUtil.getArguments(node);
    }

    MethodDeclaration enclosingMethod = methodStack.peek();
    IMethodBinding enclosingBinding = Types.getMethodBinding(enclosingMethod);
    boolean isInstance = !BindingUtil.isStatic(binding);
    if (isFunction && isInstance) {
      if (BindingUtil.isFunction(enclosingBinding) && !BindingUtil.isStatic(enclosingBinding)) {
        // Add self parameter.
        IVariableBinding selfParam =
            Types.getVariableBinding(ASTUtil.getParameters(enclosingMethod).get(0));
        args.add(0, ASTFactory.newSimpleName(ast, selfParam));
      } else {
        if (sameClassMember(binding, declaringClass, getEnclosingType(enclosingMethod)) &&
            isInstance) {
          // Add this parameter.
          ThisExpression thisExpr = ASTFactory.newThisExpression(ast, declaringClass);
          args.add(0, thisExpr);
        }
      }
    } else {
      if (!BindingUtil.isStatic(binding) && !BindingUtil.isStatic(enclosingBinding) &&
          BindingUtil.isFunction(enclosingBinding)) {
        // Change message receiver to self->method(args).
        IVariableBinding selfParam =
            Types.getVariableBinding(ASTUtil.getParameters(enclosingMethod).get(0));
        MethodInvocation invocation = ASTFactory.newMethodInvocation(ast, binding,
            ASTFactory.newSimpleName(ast, selfParam));
        ASTUtil.getArguments(invocation).addAll(
            NodeCopier.copySubtrees(ast, ASTUtil.getArguments(node)));
        ASTUtil.setProperty(node, invocation);
      }
    }
  }

  /**
   * Create an equivalent function declaration for a given method. If it's an instance
   * method, a "self" parameter is added to the beginning of the parameter list (that
   * way, variable functions can be supported).
   */
  private MethodDeclaration makeFunction(MethodDeclaration method, IMethodBinding m) {
    final AST ast = method.getAST();
    ITypeBinding declaringClass = m.getDeclaringClass();
    ITypeBinding[] paramTypes = m.getParameterTypes();
    List<SingleVariableDeclaration> params =
        NodeCopier.copySubtrees(ast, ASTUtil.getParameters(method));
    if (!BindingUtil.isStatic(m)) {
      List<ITypeBinding> list = Lists.newArrayList(paramTypes);
      list.add(0, m.getDeclaringClass());
      paramTypes = list.toArray(new ITypeBinding[list.size()]);
      GeneratedVariableBinding var = new GeneratedVariableBinding("self", 0,
          declaringClass, false, true, declaringClass, null);
      SingleVariableDeclaration param =
          ASTFactory.newSingleVariableDeclaration(ast, var);
      params.add(0, param);
    }
    String functionName = NameTable.makeFunctionName(declaringClass, m);
    IOSMethodBinding newBinding =
        IOSMethodBinding.newFunction(m, functionName, paramTypes);
    MethodDeclaration function = ASTFactory.newMethodDeclaration(ast, newBinding);
    ASTUtil.getParameters(function).addAll(params);
    if (BindingUtil.isSynchronized(m)) {
      SynchronizedStatement syncStmt = ast.newSynchronizedStatement();
      if (BindingUtil.isStatic(m)) {
        syncStmt.setExpression(ASTFactory.newTypeLiteral(ast, declaringClass));
      } else {
        GeneratedVariableBinding selfParam = new GeneratedVariableBinding("self", 0,
            declaringClass, false, true, declaringClass, null);
        SimpleName self = ASTFactory.newSimpleName(ast, selfParam);
        syncStmt.setExpression(self);
      }
      ASTUtil.getStatements(syncStmt.getBody()).addAll((
          NodeCopier.copySubtrees(ast, ASTUtil.getStatements(method.getBody()))));
      Block block = ast.newBlock();
      ASTUtil.getStatements(block).add(syncStmt);
      function.setBody(block);
      Iterator<IExtendedModifier> modifiers = ASTUtil.getModifiers(function).iterator();
      while (modifiers.hasNext()) {
        IExtendedModifier mod = modifiers.next();
        if (mod instanceof Modifier && ((Modifier) mod).isSynchronized()) {
          modifiers.remove();
          break;
        }
      }
    } else {
      function.setBody(NodeCopier.copySubtree(ast, method.getBody()));
    }
    if (BindingUtil.isStatic(m)) {
      // Add class initialization invocation, since this may be the first use of this class.
      String initName = String.format("%s_init", NameTable.getFullName(declaringClass));
      IOSMethodBinding initBinding =
          IOSMethodBinding.newFunction(initName, ast.resolveWellKnownType("void"), declaringClass);
      MethodInvocation initCall = ASTFactory.newMethodInvocation(ast, initBinding, null);
      ASTUtil.getStatements(function.getBody()).add(0, ast.newExpressionStatement(initCall));
    }
    FunctionConverter.convert(function, newBinding);
    return function;
  }

  /**
   *  Replace method block statements with single statement that invokes function.
   */
  public void setFunctionCaller(MethodDeclaration method, MethodDeclaration function) {
    AST ast = method.getAST();
    IMethodBinding functionBinding = Types.getMethodBinding(function);
    List<Statement> stmts = ASTUtil.getStatements(method.getBody());
    stmts.clear();
    MethodInvocation invocation = ASTFactory.newMethodInvocation(ast, functionBinding, null);
    List<Expression> args = ASTUtil.getArguments(invocation);
    for (SingleVariableDeclaration param : ASTUtil.getParameters(method)) {
      args.add(ASTFactory.newSimpleName(ast, Types.getVariableBinding(param)));
    }
    if (Types.isVoidType(functionBinding.getReturnType())) {
      stmts.add(ast.newExpressionStatement(invocation));
    } else {
      ReturnStatement stmt = ast.newReturnStatement();
      stmt.setExpression(invocation);
      stmts.add(stmt);
    }
  }

  /**
   *  Check if binding is to an instance method or field in the same class as the current method.
   */
  private static boolean sameClassMember(IBinding binding, ITypeBinding declaringClass,
      ITypeBinding enclosingType) {
    if (declaringClass == null) {
      return false;
    }
    if (enclosingType.isEqualTo(declaringClass)) {
      return true;
    }
    return enclosingType.getTypeDeclaration().isEqualTo(declaringClass.getTypeDeclaration());
  }

  private static ITypeBinding getEnclosingType(MethodDeclaration md) {
    ASTNode enclosingType = ASTUtil.getOwningType(md);
    return Types.getTypeBinding(enclosingType).getErasure();
  }

  /**
   * Convert references to "this" in the function to a "self" parameter.
   */
  private static class FunctionConverter extends ASTVisitor {
    private final IMethodBinding binding;
    private final IVariableBinding selfParam;

    static void convert(MethodDeclaration function, IMethodBinding binding) {
      if (!BindingUtil.isStatic(binding)) {
        IVariableBinding selfParam =
            Types.getVariableBinding(ASTUtil.getParameters(function).get(0));
        function.accept(new FunctionConverter(binding, selfParam));
      }
    }

    private FunctionConverter(IMethodBinding binding, IVariableBinding selfParam) {
      this.binding = binding;
      this.selfParam = selfParam;
    }

    @Override
    public void endVisit(FieldAccess node) {
      AST ast = node.getAST();
      Expression receiver = node.getExpression();
      SimpleName selfNode = ASTFactory.newSimpleName(ast, selfParam);
      if (receiver == null || receiver instanceof ThisExpression) {
        // Change field expression to this$.
        node.setExpression(selfNode);
      } else {
        IVariableBinding var = Types.getVariableBinding(node);
        if (isField(receiver) && receiver instanceof SimpleName &&
            !sameClassMember(var, var.getDeclaringClass(), binding.getDeclaringClass())) {
          // Change outer field expression to this$->expression.
          node.setExpression(ASTFactory.newQualifiedName(ast, selfNode,
              NodeCopier.copySubtree(ast, (SimpleName) receiver)));
        }
      }
    }

    @Override
    public boolean visit(QualifiedName node) {
      Name qual = node.getQualifier();
      IBinding qualBinding = Types.getBinding(qual);
      if (qualBinding instanceof IVariableBinding && !BindingUtil.isStatic(qualBinding)) {
        IVariableBinding var = (IVariableBinding) qualBinding;
        if (var.isField()) {
          AST ast = node.getAST();
          if (!BindingUtil.isStatic(binding)) {
            FieldAccess qualFieldAccess = ASTFactory.newFieldAccess(ast, var,
                ASTFactory.newSimpleName(node.getAST(), selfParam));
            FieldAccess fieldAccess = ASTFactory.newFieldAccess(ast, Types.getVariableBinding(node),
                qualFieldAccess);
            ASTUtil.setProperty(node, fieldAccess);
          }
        }
      }
    return true;
    }

    @Override
    public void endVisit(SimpleName node) {
      IBinding binding = Types.getBinding(node);
      if (binding instanceof IVariableBinding && !BindingUtil.isStatic(binding) &&
          !(node.getParent() instanceof FieldAccess) &&
          !(node.getParent() instanceof SuperFieldAccess) &&
          !(node.getParent() instanceof QualifiedName)) {
        IVariableBinding var = (IVariableBinding) binding;
        if (!var.isField() || BindingUtil.isConstant(var)) {
          return;
        }
        // Convert name to self->name.
        SimpleName qualifier = ASTFactory.newSimpleName(node.getAST(), selfParam);
        QualifiedName fqn = ASTFactory.newQualifiedName(node.getAST(), qualifier,
            NodeCopier.copySubtree(node.getAST(), node));
        ASTUtil.setProperty(node, fqn);
      }
    }

    @Override
    public void endVisit(SuperFieldAccess node) {
      // Change super.field expression to self.field.
      SimpleName qualifier = ASTFactory.newSimpleName(node.getAST(), selfParam);
      FieldAccess newAccess =
          ASTFactory.newFieldAccess(node.getAST(), Types.getVariableBinding(node), qualifier);
      ASTUtil.setProperty(node, newAccess);
    }

    @Override
    public void endVisit(ThisExpression node) {
      ITypeBinding declaringClass = binding.getDeclaringClass();
      GeneratedVariableBinding selfParam = new GeneratedVariableBinding("self", 0,
          declaringClass, false, true, declaringClass, null);
      SimpleName self = ASTFactory.newSimpleName(node.getAST(), selfParam);
      ASTUtil.setProperty(node, self);
    }

    private boolean isField(Expression receiver) {
      IBinding b = Types.getBinding(receiver);
      if (b instanceof IVariableBinding) {
        return ((IVariableBinding) b).isField();
      }
      return false;
    }
  }
}
