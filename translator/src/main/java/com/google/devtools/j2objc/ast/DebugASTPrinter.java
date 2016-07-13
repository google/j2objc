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

import com.google.devtools.j2objc.gen.JavadocGenerator;
import com.google.devtools.j2objc.gen.SourceBuilder;
import com.google.devtools.j2objc.jdt.TypeUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.Iterator;
import java.util.List;

/**
 * Simple AST printer, suitable for node toString() results. This printer is based on
 * org.eclipse.jdt.internal.core.dom.NaiveASTFlattener.
 *
 * @author Tom Ball
 */
public class DebugASTPrinter extends TreeVisitor {
  private SourceBuilder sb = new SourceBuilder(false);
  private boolean inIfStatement = false;

  public static String toString(TreeNode node) {
// Uncomment to debug print failures.
//    try {
      DebugASTPrinter printer = new DebugASTPrinter();
      node.accept(printer);
      return printer.sb.toString();
//    } catch (Throwable t) {
//      System.err.println("toString(" + node.getClass().getSimpleName() + ") failure");
//      t.printStackTrace();
//      throw t;
//    }
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    sb.printIndent();
    printAnnotations(node.getAnnotations());
    printModifiers(node.getModifiers());
    sb.print("@interface ");
    node.getName().accept(this);
    sb.println(" {");
    sb.indent();
    for (BodyDeclaration decl : node.getBodyDeclarations()) {
      decl.accept(this);
    }
    sb.unindent();
    sb.println("}");
    return false;
  }

  @Override
  public boolean visit(AnnotationTypeMemberDeclaration node) {
    sb.printIndent();
    printAnnotations(node.getAnnotations());
    printModifiers(node.getModifiers());
    node.getType().accept(this);
    sb.print(' ');
    node.getName().accept(this);
    sb.print("()");
    if (node.getDefault() != null) {
      sb.print(" default ");
      node.getDefault().accept(this);
    }
    sb.println(';');
    return false;
  }

  @Override
  public boolean visit(AnonymousClassDeclaration node) {
    sb.print(" {");
    sb.indent();
    for (BodyDeclaration decl : node.getBodyDeclarations()) {
      decl.accept(this);
    }
    sb.unindent();
    sb.println("}");
    return false;
  }

  @Override
  public boolean visit(ArrayAccess node) {
    node.getArray().accept(this);
    sb.print('[');
    node.getIndex().accept(this);
    sb.print(']');
    return false;
  }

  @Override
  public boolean visit(ArrayCreation node) {
    sb.print("new ");
    node.getType().accept(this);
    for (Expression dim : node.getDimensions()) {
      sb.print('[');
      dim.accept(this);
      sb.print(']');
    }
    int emptyDims = TypeUtil.getDimensions((javax.lang.model.type.ArrayType) node.getTypeMirror())
        - node.getDimensions().size();
    for (int i = 0; i < emptyDims; i++) {
      sb.print("[]");
    }
    if (node.getInitializer() != null) {
      node.getInitializer().accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(ArrayInitializer node) {
    sb.print('{');
    Iterator<Expression> iter = node.getExpressions().iterator();
    while (iter.hasNext()) {
      Expression expr = iter.next();
      expr.accept(this);
      if (iter.hasNext()) {
        sb.print(',');
      }
    }
    sb.print('}');
    return false;
  }

  @Override
  public boolean visit(ArrayType node) {
    node.getComponentType().accept(this);
    sb.print("[]");
    return false;
  }

  @Override
  public boolean visit(AssertStatement node) {
    sb.printIndent();
    sb.print("assert ");
    node.getExpression().accept(this);
    if (node.getMessage() != null) {
      sb.print(" : ");
      node.getMessage().accept(this);
    }
    sb.println(';');
    return false;
  }

  @Override
  public boolean visit(Assignment node) {
    node.getLeftHandSide().accept(this);
    sb.print(node.getOperator().toString());
    node.getRightHandSide().accept(this);
    return false;
  }

  @Override
  public boolean visit(Block node) {
    sb.println('{');
    sb.indent();
    for (Statement stmt : node.getStatements()) {
      stmt.accept(this);
    }
    sb.unindent();
    sb.printIndent();
    sb.println('}');
    return false;
  }

  @Override
  public boolean visit(BooleanLiteral node) {
    sb.print(node.booleanValue() ? "true" : "false");
    return false;
  }

  @Override
  public boolean visit(BreakStatement node) {
    sb.printIndent();
    sb.print("break");
    if (node.getLabel() != null) {
      sb.print(' ');
      node.getLabel().accept(this);
    }
    sb.println(';');
    return false;
  }

  @Override
  public boolean visit(CastExpression node) {
    sb.print('(');
    node.getType().accept(this);
    sb.print(')');
    node.getExpression().accept(this);
    return false;
  }

  @Override
  public boolean visit(CatchClause node) {
    sb.print("catch (");
    node.getException().accept(this);
    sb.print(") ");
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(CharacterLiteral node) {
    sb.print(UnicodeUtils.escapeCharLiteral(node.charValue()));
    return false;
  }

  @Override
  public boolean visit(ClassInstanceCreation node) {
    if (node.getExpression() != null) {
      node.getExpression().accept(this);
      sb.print('.');
    }
    sb.print("new ");
    printTypeParameters(node.getMethodBinding().getTypeParameters());
    node.getType().accept(this);
    sb.print("(");
    for (Iterator<Expression> it = node.getArguments().iterator(); it.hasNext(); ) {
      Expression e = it.next();
      e.accept(this);
      if (it.hasNext()) {
        sb.print(',');
      }
    }
    sb.print(')');
    if (node.getAnonymousClassDeclaration() != null) {
      node.getAnonymousClassDeclaration().accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(CommaExpression node) {
    sb.print('(');
    for (Iterator<Expression> it = node.getExpressions().iterator(); it.hasNext(); ) {
      Expression e = it.next();
      e.accept(this);
      if (it.hasNext()) {
        sb.print(", ");
      }
    }
    sb.print(')');
    return false;
  }

  @Override
  public boolean visit(CompilationUnit node) {
    if (!node.getPackage().isDefaultPackage()) {
      node.getPackage().accept(this);
    }
    for (Iterator<AbstractTypeDeclaration> it = node.getTypes().iterator(); it.hasNext(); ) {
      it.next().accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(ConditionalExpression node) {
    node.getExpression().accept(this);
    sb.print(" ? ");
    node.getThenExpression().accept(this);
    sb.print(" : ");
    node.getElseExpression().accept(this);
    return false;
  }

  @Override
  public boolean visit(ConstructorInvocation node) {
    sb.printIndent();
    printTypeParameters(node.getMethodBinding().getTypeParameters());
    sb.print("this(");
    for (Iterator<Expression> it = node.getArguments().iterator(); it.hasNext(); ) {
      it.next().accept(this);
      if (it.hasNext()) {
        sb.print(',');
      }
    }
    sb.println(");");
    return false;
  }

  @Override
  public boolean visit(ContinueStatement node) {
    sb.printIndent();
    sb.print("continue");
    if (node.getLabel() != null) {
      sb.print(' ');
      node.getLabel().accept(this);
    }
    sb.println(';');
    return false;
  }

  @Override
  public boolean visit(CreationReference node) {
    node.getType().accept(this);
    sb.print("::new");
    return false;
  }

  @Override
  public boolean visit(CStringLiteral node) {
    sb.print(node.getLiteralValue());
    return false;
  }

  @Override
  public boolean visit(Dimension node) {
    if (!node.annotations().isEmpty()) {
      sb.append(' ');
    }
    for (Annotation x : node.annotations()) {
      x.accept(this);
      sb.append(' ');
    }
    sb.append("[]");
    return false;
  }

  @Override
  public boolean visit(DoStatement node) {
    sb.printIndent();
    sb.print("do ");
    node.getBody().accept(this);
    sb.print(" while (");
    node.getExpression().accept(this);
    sb.println(");");
    return false;
  }

  @Override
  public boolean visit(EmptyStatement node) {
    sb.printIndent();
    sb.println(';');
    return false;
  }

  @Override
  public boolean visit(EnhancedForStatement node) {
    sb.printIndent();
    sb.print("for (");
    node.getParameter().accept(this);
    sb.print(" : ");
    node.getExpression().accept(this);
    sb.print(") ");
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(EnumConstantDeclaration node) {
    sb.printIndent();
    printAnnotations(node.getAnnotations());
    printModifiers(node.getModifiers());
    node.getName().accept(this);
    if (!node.getArguments().isEmpty()) {
      sb.print('(');
      for (Iterator<Expression> it = node.getArguments().iterator(); it.hasNext(); ) {
        Expression e = (Expression) it.next();
        e.accept(this);
        if (it.hasNext()) {
          sb.print(',');
        }
      }
      sb.print(')');
    }
    if (node.getAnonymousClassDeclaration() != null) {
      node.getAnonymousClassDeclaration().accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(EnumDeclaration node) {
    sb.printIndent();
    printAnnotations(node.getAnnotations());
    printModifiers(node.getModifiers());
    sb.print("enum ");
    node.getName().accept(this);
    sb.print(' ');
    sb.print('{');
    for (Iterator<EnumConstantDeclaration> it = node.getEnumConstants().iterator();
        it.hasNext(); ) {
      EnumConstantDeclaration d = (EnumConstantDeclaration) it.next();
      d.accept(this);
      if (it.hasNext()) {
        sb.print(", ");
      }
    }
    if (!node.getBodyDeclarations().isEmpty()) {
      sb.print("; ");
      for (Iterator<BodyDeclaration> it = node.getBodyDeclarations().iterator(); it.hasNext(); ) {
        it.next().accept(this);
      }
    }
    sb.println('}');
    return false;
  }

  @Override
  public boolean visit(ExpressionMethodReference node) {
    node.getExpression().accept(this);
    sb.print("::");
    node.getName().accept(this);
    return false;
  }

  @Override
  public boolean visit(ExpressionStatement node) {
    sb.printIndent();
    node.getExpression().accept(this);
    sb.println(';');
    return false;
  }

  @Override
  public boolean visit(FieldAccess node) {
    node.getExpression().accept(this);
    sb.print('.');
    node.getName().accept(this);
    return false;
  }

  @Override
  public boolean visit(FieldDeclaration node) {
    if (node.getJavadoc() != null) {
      node.getJavadoc().accept(this);
    }
    sb.printIndent();
    printAnnotations(node.getAnnotations());
    printModifiers(node.getModifiers());
    node.getType().accept(this);
    sb.print(' ');
    for (Iterator<VariableDeclarationFragment> it = node.getFragments().iterator();
        it.hasNext(); ) {
      it.next().accept(this);
      if (it.hasNext()) {
        sb.print(", ");
      }
    }
    sb.println(';');
    return false;
  }

  @Override
  public boolean visit(ForStatement node) {
    sb.printIndent();
    sb.print("for (");
    for (Iterator<Expression> it = node.getInitializers().iterator(); it.hasNext(); ) {
      it.next().accept(this);
      if (it.hasNext()) {
        sb.print(", ");
      }
    }
    sb.print("; ");
    if (node.getExpression() != null) {
      node.getExpression().accept(this);
    }
    sb.print("; ");
    for (Iterator<Expression> it = node.getUpdaters().iterator(); it.hasNext(); ) {
      it.next().accept(this);
      if (it.hasNext()) {
        sb.print(", ");
      }
    }
    sb.print(") ");
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(FunctionDeclaration node) {
    sb.printIndent();
    printAnnotations(node.getAnnotations());
    printModifiers(node.getModifiers());
    node.getReturnType().accept(this);
    sb.print(' ');
    sb.print(node.getName());
    sb.print('(');
    for (Iterator<SingleVariableDeclaration> it = node.getParameters().iterator(); it.hasNext(); ) {
      it.next().accept(this);
      if (it.hasNext()) {
        sb.print(',');
      }
    }
    sb.print(')');
    if (node.getBody() == null) {
      sb.print(';');
    } else {
      node.getBody().accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(FunctionInvocation node) {
    sb.append(node.getName());
    sb.append('(');
    for (Iterator<Expression> iter = node.getArguments().iterator(); iter.hasNext(); ) {
      iter.next().accept(this);
      if (iter.hasNext()) {
        sb.append(", ");
      }
    }
    sb.append(')');
    return false;
  }

  @Override
  public boolean visit(IfStatement node) {
    if (!inIfStatement) {
      sb.printIndent();
    }
    boolean wasInStatement = inIfStatement;
    inIfStatement = true;
    sb.print("if (");
    node.getExpression().accept(this);
    sb.print(") ");
    node.getThenStatement().accept(this);
    if (node.getElseStatement() != null) {
      sb.printIndent();
      sb.print("else ");
      node.getElseStatement().accept(this);
    }
    inIfStatement = wasInStatement;
    return false;
  }

  @Override
  public boolean visit(InfixExpression node) {
    boolean isFirst = true;
    String op = ' ' + node.getOperator().toString() + ' ';
    for (Expression operand : node.getOperands()) {
      if (!isFirst) {
        sb.print(op);
      }
      isFirst = false;
      operand.accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(Initializer node) {
    printAnnotations(node.getAnnotations());
    printModifiers(node.getModifiers());
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(InstanceofExpression node) {
    node.getLeftOperand().accept(this);
    sb.print(" instanceof ");
    node.getRightOperand().accept(this);
    return false;
  }

  @Override
  public boolean visit(IntersectionType node) {
    sb.print('(');
    boolean delimiterFlag = false;
    for (Type t : node.types()) {
      if (delimiterFlag) {
        sb.print(" & ");
      } else {
        delimiterFlag = true;
      }
      t.accept(this);
    }
    sb.print(')');
    return false;
  }

  @Override
  public boolean visit(Javadoc node) {
    sb.println(JavadocGenerator.toString(node));
    return false;
  }

  @Override
  public boolean visit(LabeledStatement node) {
    sb.printIndent();
    node.getLabel().accept(this);
    sb.print(": ");
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(LambdaExpression node) {
    sb.print("(");
    boolean delimiterFlag = false;
    for (VariableDeclaration x : node.getParameters()) {
      IVariableBinding variableBinding = x.getVariableBinding();
      if (delimiterFlag) {
        sb.print(", ");
      } else {
        delimiterFlag = true;
      }
      sb.print(variableBinding.getType().getName());
      sb.print(" ");
      sb.print(variableBinding.getName());
    }
    sb.print(") -> ");
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(MarkerAnnotation node) {
    sb.print('@');
    node.getTypeName().accept(this);
    return false;
  }

  @Override
  public boolean visit(MemberValuePair node) {
    node.getName().accept(this);
    sb.print('=');
    node.getValue().accept(this);
    return false;
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    sb.printIndent();
    printAnnotations(node.getAnnotations());
    printModifiers(node.getModifiers());
    IMethodBinding meth = node.getMethodBinding();
    printTypeParameters(meth.getTypeParameters());
    if (!node.isConstructor()) {
      if (node.getReturnType() != null) {
        node.getReturnType().accept(this);
      }
      sb.print(' ');
    }
    node.getName().accept(this);
    sb.print("(");
    for (Iterator<SingleVariableDeclaration> it = node.getParameters().iterator(); it.hasNext(); ) {
      it.next().accept(this);
      if (it.hasNext()) {
        sb.print(',');
      }
    }
    sb.print(")");
    ITypeBinding[] exceptions = meth.getExceptionTypes();
    if (exceptions.length > 0) {
      sb.print(" throws ");
      for (int i = 0; i < exceptions.length; ) {
        sb.print(exceptions[i].getName());
        if (++i < exceptions.length){
          sb.print(',');
        }
      }
      sb.print(' ');
    }
    if (node.getBody() == null) {
      sb.println(';');
    } else {
      node.getBody().accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    if (node.getExpression() != null) {
      node.getExpression().accept(this);
      sb.print(".");
    }
    printTypeParameters(node.getMethodBinding().getTypeParameters());
    node.getName().accept(this);
    sb.print('(');
    for (Iterator<Expression> it = node.getArguments().iterator(); it.hasNext(); ) {
      it.next().accept(this);
      if (it.hasNext()) {
        sb.print(',');
      }
    }
    sb.print(')');
    return false;
  }

  @Override
  public boolean visit(NameQualifiedType node) {
    node.getQualifier().accept(this);
    sb.print('.');
    for (Annotation x : node.annotations()) {
      x.accept(this);
      sb.print(' ');
    }
    node.getName().accept(this);
    return false;
  }

  @Override
  public boolean visit(NativeDeclaration node) {
    if (node.getImplementationCode() != null) {
      sb.println(node.getImplementationCode());
    } else if (node.getHeaderCode() != null) {
      sb.println(node.getHeaderCode());
    }
    return false;
  }

  @Override
  public boolean visit(NativeExpression node) {
    sb.println(node.getCode());
    return false;
  }

  @Override
  public boolean visit(NativeStatement node) {
    sb.println(node.getCode());
    return false;
  }

  @Override
  public boolean visit(NormalAnnotation node) {
    sb.print("@");
    node.getTypeName().accept(this);
    sb.print("(");
    for (Iterator<MemberValuePair> it = node.getValues().iterator(); it.hasNext(); ) {
      MemberValuePair p = (MemberValuePair) it.next();
      p.accept(this);
      if (it.hasNext()) {
        sb.print(',');
      }
    }
    sb.print(")");
    return false;
  }

  @Override
  public boolean visit(NullLiteral node) {
    sb.print("null");
    return false;
  }

  @Override
  public boolean visit(NumberLiteral node) {
    sb.print(node.getToken());
    return false;
  }

  @Override
  public boolean visit(PackageDeclaration node) {
    printAnnotations(node.getAnnotations());
    sb.print("package ");
    node.getName().accept(this);
    sb.println(';');
    return false;
  }

  @Override
  public boolean visit(ParameterizedType node) {
    node.getType().accept(this);
    printTypeParameters(node.getTypeBinding().getTypeParameters());
    return false;
  }

  @Override
  public boolean visit(ParenthesizedExpression node) {
    sb.print('(');
    node.getExpression().accept(this);
    sb.print(')');
    return false;
  }

  @Override
  public boolean visit(PostfixExpression node) {
    node.getOperand().accept(this);
    sb.print(node.getOperator().toString());
    return false;
  }

  @Override
  public boolean visit(PrefixExpression node) {
    sb.print(node.getOperator().toString());
    node.getOperand().accept(this);
    return false;
  }

  @Override
  public boolean visit(PrimitiveType node) {
    sb.print(node.getTypeBinding().getName());
    return false;
  }

  @Override
  public boolean visit(PropertyAnnotation node) {
    String attributeString = PropertyAnnotation.toAttributeString(node.getPropertyAttributes());
    sb.print("@Property(\"" + attributeString + "\")");
    return false;
  }

  @Override
  public boolean visit(QualifiedName node) {
    node.getQualifier().accept(this);
    sb.print(".");
    node.getName().accept(this);
    return false;
  }

  @Override
  public boolean visit(QualifiedType node) {
    sb.print(node.getTypeBinding().getQualifiedName());
    return false;
  }

  @Override
 public boolean visit(ReturnStatement node) {
    sb.printIndent();
    sb.print("return");
    if (node.getExpression() != null) {
      sb.print(' ');
      node.getExpression().accept(this);
    }
    sb.println(';');
    return false;
  }

  @Override
  public boolean visit(SimpleName node) {
    sb.print(node.getIdentifier());
    return false;
  }

  @Override
  public boolean visit(SimpleType node) {
    sb.print(node.getTypeBinding().getName());
    return false;
  }

  @Override
  public boolean visit(SingleMemberAnnotation node) {
    sb.print("@");
    node.getTypeName().accept(this);
    sb.print("(");
    node.getValue().accept(this);
    sb.print(")");
    return false;
  }

  @Override
  public boolean visit(SingleVariableDeclaration node) {
    sb.printIndent();
    printModifiers(node.getVariableBinding().getModifiers());
    node.getType().accept(this);
    if (node.isVarargs()) {
      sb.print("...");
    }
    sb.print(' ');
    node.getName().accept(this);
    for (int i = 0; i < node.getExtraDimensions(); i++) {
      sb.print("[]");
    }
    if (node.getInitializer() != null) {
      sb.print("=");
      node.getInitializer().accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(StringLiteral node) {
    sb.printf("\"%s\"", UnicodeUtils.escapeStringLiteral(node.getLiteralValue()));
    return false;
  }

  @Override
  public boolean visit(SuperConstructorInvocation node) {
    sb.printIndent();
    if (node.getExpression() != null) {
      node.getExpression().accept(this);
      sb.print(".");
    }
    printTypeParameters(node.getMethodBinding().getTypeParameters());
    sb.print("super(");
    for (Iterator<Expression> it = node.getArguments().iterator(); it.hasNext(); ) {
      it.next().accept(this);
      if (it.hasNext()) {
        sb.print(',');
      }
    }
    sb.println(");");
    return false;
  }

  @Override
  public boolean visit(SuperFieldAccess node) {
    if (node.getQualifier() != null) {
      node.getQualifier().accept(this);
      sb.print(".");
    }
    sb.print("super.");
    node.getName().accept(this);
    return false;
  }

  @Override
  public boolean visit(SuperMethodInvocation node) {
    if (node.getQualifier() != null) {
      node.getQualifier().accept(this);
      sb.print(".");
    }
    sb.print("super.");
    printTypeParameters(node.getMethodBinding().getTypeParameters());
    node.getName().accept(this);
    sb.print("(");
    for (Iterator<Expression> it = node.getArguments().iterator(); it.hasNext(); ) {
      it.next().accept(this);
      if (it.hasNext()) {
        sb.print(',');
      }
    }
    sb.print(")");
    return false;
  }

  @Override
  public boolean visit(SuperMethodReference node) {
    if (node.getQualifier() != null) {
      node.getQualifier().accept(this);
      sb.print(".");
    }
    sb.print("super::");
    sb.print(node.getName().toString());
    return false;
  }

  @Override
  public boolean visit(SwitchCase node) {
    sb.unindent();
    sb.printIndent();
    if (node.isDefault()) {
      sb.println("default :");
    } else {
      sb.print("case ");
      node.getExpression().accept(this);
      sb.println(":");
    }
    sb.indent();
    return false;
  }

  @Override
  public boolean visit(SwitchStatement node) {
    sb.printIndent();
    sb.print("switch (");
    node.getExpression().accept(this);
    sb.print(") ");
    sb.println("{");
    sb.indent();
    sb.indent();
    for (Iterator<Statement> it = node.getStatements().iterator(); it.hasNext(); ) {
      it.next().accept(this);
    }
    sb.unindent();
    sb.unindent();
    sb.printIndent();
    sb.println("}");
    return false;
  }

  @Override
  public boolean visit(SynchronizedStatement node) {
    sb.print("synchronized (");
    node.getExpression().accept(this);
    sb.print(") ");
    node.getBody().accept(this);
    return false;
  }

  @Override
  public boolean visit(TagElement node) {
    sb.print(JavadocGenerator.toString(node));
    return false;
  }

  @Override
  public boolean visit(ThisExpression node) {
    if (node.getQualifier() != null) {
      node.getQualifier().accept(this);
      sb.print(".");
    }
    sb.print("this");
    return false;
  }

  @Override
  public boolean visit(ThrowStatement node) {
    sb.printIndent();
    sb.print("throw ");
    node.getExpression().accept(this);
    sb.println(';');
    return false;
  }

  @Override
  public boolean visit(TryStatement node) {
    sb.printIndent();
    sb.print("try ");
    List<VariableDeclarationExpression> resources = node.getResources();
    if (!resources.isEmpty()) {
      sb.print('(');
      for (Iterator<VariableDeclarationExpression> it = resources.iterator(); it.hasNext(); ) {
        it.next().accept(this);
        if (it.hasNext()) {
          sb.print(';');
        }
      }
      sb.print(')');
    }
    node.getBody().accept(this);
    sb.print(' ');
    for (Iterator<CatchClause> it = node.getCatchClauses().iterator(); it.hasNext(); ) {
      it.next().accept(this);
    }
    if (node.getFinally() != null) {
      sb.print(" finally ");
      node.getFinally().accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    if (node.getJavadoc() != null) {
      node.getJavadoc().accept(this);
    }
    printAnnotations(node.getAnnotations());
    printModifiers(node.getModifiers());
    sb.print(node.isInterface() ? "interface " : "class ");
    node.getName().accept(this);
    printTypeParameters(node.getTypeBinding().getTypeParameters());
    sb.print(' ');
    if (node.getSuperclassType() != null) {
      sb.print("extends ");
      node.getSuperclassType().accept(this);
      sb.print(' ');
    }
    if (!node.getSuperInterfaceTypes().isEmpty()) {
      sb.print(node.isInterface() ? "extends " : "implements "); //$NON-NLS-2$
      for (Iterator<Type> it = node.getSuperInterfaceTypes().iterator(); it.hasNext(); ) {
        it.next().accept(this);
        if (it.hasNext()) {
          sb.print(", ");
        }
      }
      sb.print(' ');
    }
    sb.println('{');
    sb.indent();
    for (Iterator<BodyDeclaration> it = node.getBodyDeclarations().iterator(); it.hasNext(); ) {
      it.next().accept(this);
    }
    sb.unindent();
    sb.printIndent();
    sb.println('}');
    return false;
  }

  @Override
  public boolean visit(TypeLiteral node) {
    node.getType().accept(this);
    sb.print(".class");
    return false;
  }

  @Override
  public boolean visit(TypeMethodReference node) {
    node.getType().accept(this);
    sb.print("::");
    if (!node.getTypeArguments().isEmpty()) {
      sb.print('<');
      boolean delimiterFlag = false;
      for (Type t : node.getTypeArguments()) {
        if (delimiterFlag) {
          sb.print(", ");
        } else {
          delimiterFlag = true;
        }
        t.accept(this);
      }
      sb.print('>');
    }
    node.getName().accept(this);
    return false;
  }

  @Override
  public boolean visit(UnionType node) {
    for (Iterator<Type> it = node.getTypes().iterator(); it.hasNext(); ) {
      it.next().accept(this);
      if (it.hasNext()) {
        sb.print('|');
      }
    }
    return false;
  }

  @Override
  public boolean visit(VariableDeclarationExpression node) {
    printModifiers(TypeUtil.getModifiers(node.getTypeMirror()));
    node.getType().accept(this);
    sb.print(' ');
    for (Iterator<VariableDeclarationFragment> it = node.getFragments().iterator();
         it.hasNext(); ) {
      it.next().accept(this);
      if (it.hasNext()) {
        sb.print(", ");
      }
    }
    return false;
  }

  @Override
  public boolean visit(VariableDeclarationFragment node) {
    node.getName().accept(this);
    for (int i = 0; i < node.getExtraDimensions(); i++) {
      sb.print("[]");
    }
    if (node.getInitializer() != null) {
      sb.print("=");
      node.getInitializer().accept(this);
    }
    return false;
  }

  @Override
  public boolean visit(VariableDeclarationStatement node) {
    sb.printIndent();
    printModifiers(node.getType().getTypeBinding().getModifiers());
    node.getType().accept(this);
    sb.print(' ');
    for (Iterator<VariableDeclarationFragment> it = node.getFragments().iterator();
         it.hasNext(); ) {
      it.next().accept(this);
      if (it.hasNext()) {
        sb.print(", ");
      }
    }
    sb.println(';');
    return false;
  }

  @Override
  public boolean visit(WhileStatement node) {
    sb.printIndent();
    sb.print("while (");
    node.getExpression().accept(this);
    sb.print(") ");
    node.getBody().accept(this);
    return false;
  }

  private void printAnnotations(List<Annotation> annotations) {
    Iterator<Annotation> iterator = annotations.iterator();
    while (iterator.hasNext()) {
      iterator.next().accept(this);
      sb.print(' ');
    }
  }

  private void printModifiers(int modifiers) {
    if (Modifier.isPublic(modifiers)) {
      sb.print("public ");
    }
    if (Modifier.isProtected(modifiers)) {
      sb.print("protected ");
    }
    if (Modifier.isPrivate(modifiers)) {
      sb.print("private ");
    }
    if (Modifier.isStatic(modifiers)) {
      sb.print("static ");
    }
    if (Modifier.isAbstract(modifiers)) {
      sb.print("abstract ");
    }
    if (Modifier.isFinal(modifiers)) {
      sb.print("final ");
    }
    if (Modifier.isSynchronized(modifiers)) {
      sb.print("synchronized ");
    }
    if (Modifier.isVolatile(modifiers)) {
      sb.print("volatile ");
    }
    if (Modifier.isNative(modifiers)) {
      sb.print("native ");
    }
    if (Modifier.isStrictfp(modifiers)) {
      sb.print("strictfp ");
    }
    if (Modifier.isTransient(modifiers)) {
      sb.print("transient ");
    }
    if ((modifiers & BindingUtil.ACC_SYNTHETIC) > 0) {
      sb.print("synthetic ");
    }
  }

  private void printTypeParameters(ITypeBinding[] typeParams) {
    if (typeParams.length > 0) {
      sb.print('<');
      for (int i = 0; i < typeParams.length; ) {
        sb.print(typeParams[i].getName());
        if (++i < typeParams.length){
          sb.print(',');
        }
      }
      sb.print('>');
    }
  }
}
