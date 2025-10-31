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
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Simple AST printer, suitable for node toString() results. This printer is based on
 * org.eclipse.jdt.internal.core.dom.NaiveASTFlattener.
 *
 * @author Tom Ball
 */
@SuppressWarnings("UngroupedOverloads")
public class DebugASTPrinter extends TreeVisitor {
  protected SourceBuilder sb = new SourceBuilder(false);
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
    sb.print(node.getTypeMirror().toString());
    sb.print(' ');
    sb.print(ElementUtil.getName(node.getExecutableElement()));
    sb.print("()");
    if (node.getDefault() != null) {
      sb.print(" default ");
      node.getDefault().accept(this);
    }
    sb.println(';');
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
    Type componentType = node.getType().getComponentType();
    int emptyDims = 1;
    while (componentType.getKind() == TreeNode.Kind.ARRAY_TYPE) {
      componentType = ((ArrayType) componentType).getComponentType();
      emptyDims++;
    }
    emptyDims -= node.getDimensions().size();
    sb.print("new ");
    componentType.accept(this);
    for (Expression dim : node.getDimensions()) {
      sb.print('[');
      dim.accept(this);
      sb.print(']');
    }
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
    printTypeParameters(node.getExecutableElement().getTypeParameters());
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
    printTypeParameters(node.getExecutableElement().getTypeParameters());
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
  public boolean visit(DoStatement node) {
    sb.printIndent();
    sb.print("do ");
    node.getBody().accept(this);
    sb.printIndent();
    sb.print("while (");
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
    sb.print(ElementUtil.getName(node.getVariableElement()));
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
    printStaticBlock(node);
    sb.println('}');
    return false;
  }

  @Override
  public boolean visit(ExpressionMethodReference node) {
    node.getExpression().accept(this);
    sb.print("::");
    sb.print(ElementUtil.getName(node.getExecutableElement()));
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
    sb.print(node.getTypeMirror().toString());
    sb.print(' ');
    node.getFragment().accept(this);
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
    Pattern pattern = node.getPattern();
    if (pattern != null && pattern.getKind() == TreeNode.Kind.BINDING_PATTERN) {
      sb.print(" ");
      var unused = visit(((Pattern.BindingPattern) pattern).getVariable());
    }
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
      VariableElement variableElement = x.getVariableElement();
      if (delimiterFlag) {
        sb.print(", ");
      } else {
        delimiterFlag = true;
      }
      sb.print(variableElement.asType().toString());
      sb.print(" ");
      sb.print(variableElement.getSimpleName().toString());
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

  protected void printMethodBody(MethodDeclaration node) {
    if (node.getBody() == null) {
      sb.println(';');
    } else {
      node.getBody().accept(this);
    }
  }

  @Override
  public boolean visit(MethodDeclaration node) {
    sb.printIndent();
    printAnnotations(node.getAnnotations());
    printModifiers(node.getModifiers());
    ExecutableElement meth = node.getExecutableElement();
    printTypeParameters(meth.getTypeParameters());
    if (!node.isConstructor()) {
      sb.print(node.getReturnTypeMirror().toString());
      sb.print(' ');
    }
    sb.print(ElementUtil.getName(meth));
    sb.print("(");
    for (Iterator<SingleVariableDeclaration> it = node.getParameters().iterator(); it.hasNext(); ) {
      it.next().accept(this);
      if (it.hasNext()) {
        sb.print(',');
      }
    }
    sb.print(")");
    List<? extends TypeMirror> exceptions = meth.getThrownTypes();
    if (exceptions.size() > 0) {
      sb.print(" throws ");
      for (int i = 0; i < exceptions.size(); ) {
        sb.print(exceptions.get(i).toString());
        if (++i < exceptions.size()){
          sb.print(',');
        }
      }
      sb.print(' ');
    }
    printMethodBody(node);
    return false;
  }

  @Override
  public boolean visit(MethodInvocation node) {
    if (node.getExpression() != null) {
      node.getExpression().accept(this);
      sb.print(".");
    }
    printTypeParameters(node.getExecutableElement().getTypeParameters());
    sb.print(ElementUtil.getName(node.getExecutableElement()));
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
    sb.print(node.getCode());
    return false;
  }

  @Override
  public boolean visit(NativeStatement node) {
    sb.printIndent();
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
    String text = node.getToken();
    sb.print(text != null ? text : node.getValue().toString());
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
    sb.print(node.getTypeMirror().toString());
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
    sb.print(node.getTypeMirror().toString());
    return false;
  }

  @Override
  public boolean visit(RecordDeclaration node) {
    sb.printIndent();
    printAnnotations(node.getAnnotations());
    printModifiers(node.getModifiers());
    sb.print("record ");
    node.getName().accept(this);
    sb.print(' ');
    sb.println('{');
    sb.indent();
    List<BodyDeclaration> bodyDeclarations = new ArrayList<>(node.getBodyDeclarations());
    sort(bodyDeclarations);
    for (BodyDeclaration bodyDecl : bodyDeclarations) {
      bodyDecl.accept(this);
    }
    printStaticBlock(node);
    sb.unindent();
    sb.printIndent();
    sb.println('}');
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
    sb.print(node.getTypeMirror().toString());
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
    printModifiers(ElementUtil.fromModifierSet(node.getVariableElement().getModifiers()));
    node.getType().accept(this);
    if (node.isVarargs()) {
      sb.print("...");
    }
    sb.print(' ');
    sb.print(ElementUtil.getName(node.getVariableElement()));
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
    printTypeParameters(node.getExecutableElement().getTypeParameters());
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
    sb.print(ElementUtil.getName(node.getVariableElement()));
    return false;
  }

  @Override
  public boolean visit(SuperMethodInvocation node) {
    if (node.getQualifier() != null) {
      node.getQualifier().accept(this);
      sb.print(".");
    }
    sb.print("super.");
    printTypeParameters(node.getExecutableElement().getTypeParameters());
    sb.print(ElementUtil.getName(node.getExecutableElement()));
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
    sb.print(ElementUtil.getName(node.getExecutableElement()));
    return false;
  }

  @Override
  public boolean visit(SwitchCase node) {
    sb.printIndent();
    if (node.isDefault()) {
      sb.print("default: ");
    } else {
      sb.print("case ");
      for (Iterator<Expression> it = node.getExpressions().iterator(); it.hasNext(); ) {
        it.next().accept(this);
        if (it.hasNext()) {
          sb.print(", ");
        }
      }
      if (node.getPattern() != null) {
        if (node.getPattern() instanceof Pattern.BindingPattern) {
          SingleVariableDeclaration varDecl =
              ((Pattern.BindingPattern) node.getPattern()).getVariable();
          VariableElement variableElement = varDecl.getVariableElement();
          sb.print(variableElement.asType().toString());
          sb.print(" ");
          sb.print(variableElement.getSimpleName().toString());
        }
      }
      if (node.getGuard() != null) {
        sb.print(" when ");
        node.getGuard().accept(this);
      }
      sb.print(": ");
    }
    TreeNode body = node.getBody();
    if (body == null) {
      sb.newline();
    } else {
      sb.print(body.toString());
    }
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
    for (Iterator<Statement> it = node.getStatements().iterator(); it.hasNext(); ) {
      it.next().accept(this);
    }
    sb.unindent();
    sb.printIndent();
    sb.println("};");
    return false;
  }

  @Override
  public boolean visit(SwitchExpression node) {
    sb.print("switch (");
    node.getExpression().accept(this);
    sb.print(") ");
    sb.println("{");
    sb.indent();
    for (Statement element : node.getStatements()) {
      element.accept(this);
    }
    sb.unindent();
    sb.printIndent();
    sb.print("}");
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
    List<TreeNode> resources = node.getResources();
    if (!resources.isEmpty()) {
      sb.print('(');
      for (Iterator<TreeNode> it = resources.iterator(); it.hasNext(); ) {
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

  protected void sort(List<BodyDeclaration> lst) {}

  @Override
  public boolean visit(TypeDeclaration node) {
    if (node.getJavadoc() != null) {
      node.getJavadoc().accept(this);
    }
    printAnnotations(node.getAnnotations());
    printModifiers(node.getModifiers());
    sb.print(node.isInterface() ? "interface " : "class ");
    if (node.getName() != null) {
      node.getName().accept(this);
      printTypeParameters(node.getTypeElement().getTypeParameters());
      sb.print(' ');
      TypeMirror superclassTypeMirror = node.getSuperclassTypeMirror();
      if (!(TypeUtil.isNone(superclassTypeMirror) || TypeUtil.isJavaObject(superclassTypeMirror))) {
        sb.print("extends ");
        sb.print(superclassTypeMirror.toString());
        sb.print(' ');
      }
      List<? extends TypeMirror> superInterfaceTypeMirrors = node.getSuperInterfaceTypeMirrors();
      if (!superInterfaceTypeMirrors.isEmpty()) {
        sb.print(node.isInterface() ? "extends " : "implements "); // $NON-NLS-2$
        for (Iterator<? extends TypeMirror> it = node.getSuperInterfaceTypeMirrors().iterator();
            it.hasNext(); ) {
          sb.print(it.next().toString());
          if (it.hasNext()) {
            sb.print(", ");
          }
        }
        sb.print(' ');
      }
      sb.println('{');
      sb.indent();
      List<BodyDeclaration> bodyDeclarations = new ArrayList<>(node.getBodyDeclarations());
      sort(bodyDeclarations);
      for (BodyDeclaration bodyDecl : bodyDeclarations) {
        bodyDecl.accept(this);
      }
      printStaticBlock(node);
      sb.unindent();
      sb.printIndent();
      sb.println('}');
    } else {
      sb.println("<uninitialized> {}");
    }
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
    sb.print(ElementUtil.getName(node.getExecutableElement()));
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
    sb.print(ElementUtil.getName(node.getVariableElement()));
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
    printModifiers(node.getModifiers());
    sb.print(node.getTypeMirror().toString());
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

  @Override
  public boolean visit(YieldStatement node) {
    sb.printIndent();
    sb.print("yield ");
    node.getExpression().accept(this);
    sb.println(";");
    return false;
  }

  protected void printAnnotations(List<Annotation> annotations) {
    Iterator<Annotation> iterator = annotations.iterator();
    while (iterator.hasNext()) {
      iterator.next().accept(this);
      sb.print(' ');
    }
  }

  public static void printModifiers(int modifiers, StringBuilder builder) {
    DebugASTPrinter temp = new DebugASTPrinter();
    temp.printModifiers(modifiers);
    builder.append(temp.sb.toString());
  }

  protected void printModifiers(int modifiers) {
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
    if (Modifier.isStrict(modifiers)) {
      sb.print("strictfp ");
    }
    if (Modifier.isTransient(modifiers)) {
      sb.print("transient ");
    }
    if ((modifiers & ElementUtil.ACC_SYNTHETIC) > 0) {
      sb.print("synthetic ");
    }
  }

  protected void printTypeParameter(TypeParameterElement element) {
    sb.print(element.getSimpleName().toString());
    Iterator<? extends TypeMirror> boundsList = element.getBounds().iterator();
    TypeMirror bound = boundsList.next();
    if (!TypeUtil.isJavaObject(bound) || boundsList.hasNext()) {
      sb.print(" extends ");
      sb.print(bound.toString());
    }
    while (boundsList.hasNext()) {
      sb.print(" & ");
      bound = boundsList.next();
      sb.print(bound.toString());
    }
  }

  protected void printTypeParameters(List<? extends TypeParameterElement> typeParams) {
    Iterator<? extends TypeParameterElement> it = typeParams.iterator();
    if (it.hasNext()) {
      sb.print('<');
      printTypeParameter(it.next());
      while (it.hasNext()) {
        sb.print(',');
        printTypeParameter(it.next());
      }
      sb.print('>');
    }
  }

  protected void printStaticBlock(AbstractTypeDeclaration node) {
    if (!node.getClassInitStatements().isEmpty()) {
      sb.printIndent();
      sb.println("static {");
      sb.indent();
      for (Statement stmt : node.getClassInitStatements()) {
        stmt.accept(this);
      }
      sb.unindent();
      sb.printIndent();
      sb.println('}');
    }
  }
}
