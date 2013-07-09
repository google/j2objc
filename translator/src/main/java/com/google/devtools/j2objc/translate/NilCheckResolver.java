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

import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.NodeCopier;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;

/**
 * Adds nil_chk calls where required to maintain compatibility Java's
 * NullPointerException being thrown when null is dereferenced.
 *
 * @author Keith Stanger
 */
public class NilCheckResolver extends ErrorReportingASTVisitor {

  private static final IOSMethodBinding NIL_CHK_DECL = IOSMethodBinding.newFunction(
      "nil_chk", Types.resolveIOSType("id"), Types.resolveIOSType("id"));

  private static boolean needsNilCheck(Expression e) {
    IVariableBinding sym = Types.getVariableBinding(e);
    if (sym != null) {
      // Outer class references should always be non-nil.
      return !sym.getName().startsWith("this$") && !sym.getName().equals("outer$")
          && !hasNilCheckParent(e, sym);
    }
    if (e instanceof MethodInvocation) {
      IMethodBinding method = Types.getMethodBinding(e);
      // Check for some common cases where the result is known not to be null.
      return !method.getName().equals("getClass")
          && !(Types.isBoxedPrimitive(method.getDeclaringClass())
               && method.getName().equals("valueOf"));
    }
    if (e instanceof ArrayAccess) {
      return true;
    }
    return false;
  }

  private static boolean hasNilCheckParent(Expression e, IVariableBinding sym) {
    ASTNode parent = e.getParent();
    while (parent != null) {
      if (parent instanceof IfStatement) {
        Expression condition = ((IfStatement) parent).getExpression();
        if (condition instanceof InfixExpression) {
          InfixExpression infix = (InfixExpression) condition;
          IBinding lhs = Types.getBinding(infix.getLeftOperand());
          if (lhs != null && infix.getRightOperand() instanceof NullLiteral) {
            return sym.isEqualTo(lhs);
          }
          IBinding rhs = Types.getBinding(infix.getRightOperand());
          if (rhs != null && infix.getLeftOperand() instanceof NullLiteral) {
            return sym.isEqualTo(rhs);
          }
        }
      }
      parent = parent.getParent();
      if (parent instanceof MethodDeclaration) {
        break;
      }
    }
    return false;
  }

  private static void addNilCheck(Expression node) {
    if (!needsNilCheck(node)) {
      return;
    }
    IOSMethodBinding nilChkBinding = IOSMethodBinding.newTypedInvocation(
        NIL_CHK_DECL, Types.getTypeBinding(node));
    MethodInvocation nilChkInvocation = ASTFactory.newMethodInvocation(
        node.getAST(), nilChkBinding, null);
    ASTUtil.setProperty(node, nilChkInvocation);
    ASTUtil.getArguments(nilChkInvocation).add(node);
  }

  @Override
  public void endVisit(ArrayAccess node) {
    addNilCheck(node.getArray());
  }

  @Override
  public void endVisit(FieldAccess node) {
    addNilCheck(node.getExpression());
  }

  @Override
  public boolean visit(QualifiedName node) {
    if (!needsNilCheck(node.getQualifier())) {
      return true;
    }

    // Instance references to static fields don't need to be nil-checked.
    // This is true in Java (surprisingly), where instance.FIELD returns
    // FIELD even when instance is null.
    IBinding binding = Types.getBinding(node);
    if (binding instanceof IVariableBinding &&
        BindingUtil.isStatic((IVariableBinding) binding)) {
      IBinding qualifierBinding = Types.getBinding(node.getQualifier());
      if (qualifierBinding instanceof IVariableBinding &&
          !BindingUtil.isStatic((IVariableBinding) qualifierBinding))
      return true;
    }

    // We can't substitute the qualifier with a nil_chk because it must have a
    // Name type, so we have to convert to a FieldAccess node.
    FieldAccess newNode = convertToFieldAccess(node);
    newNode.accept(this);
    return false;
  }

  private static FieldAccess convertToFieldAccess(QualifiedName node) {
    AST ast = node.getAST();
    ASTNode parent = node.getParent();
    if (parent instanceof QualifiedName) {
      FieldAccess newParent = convertToFieldAccess((QualifiedName) parent);
      Expression expr = newParent.getExpression();
      assert expr instanceof QualifiedName;
      node = (QualifiedName) expr;
    }
    FieldAccess newNode = ASTFactory.newFieldAccess(
        ast, Types.getVariableBinding(node), NodeCopier.copySubtree(ast, node.getQualifier()));
    ASTUtil.setProperty(node, newNode);
    return newNode;
  }

  @Override
  public void endVisit(MethodInvocation node) {
    IMethodBinding binding = Types.getMethodBinding(node);
    if (BindingUtil.isStatic(binding)) {
      return;
    }
    Expression receiver = node.getExpression();
    if (receiver != null) {
      addNilCheck(receiver);
    }
  }
}
