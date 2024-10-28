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

import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.BooleanLiteral;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.InstanceofExpression;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.ParenthesizedExpression;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.RecordDeclaration;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SimpleType;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Synthesizes the components of a Java record, adding any default elements that a JVM would add if
 * they aren't explicitly written.
 *
 * @author Tom Ball
 */
public class RecordExpander extends UnitTreeVisitor {

  public RecordExpander(CompilationUnit unit) {
    super(unit);
  }

  @Override
  public void endVisit(RecordDeclaration node) {
    TypeElement record = node.getTypeElement();

    // Add missing field assignments if constructor wasn't explicitly declared.
    for (BodyDeclaration decl : node.getBodyDeclarations()) {
      if (decl.getKind() == TreeNode.Kind.METHOD_DECLARATION) {
        MethodDeclaration methodDecl = (MethodDeclaration) decl;
        if (ElementUtil.isConstructor(methodDecl.getExecutableElement())) {
          // Check if it's the default constructor for a record.
          List<? extends VariableElement> args = methodDecl.getExecutableElement().getParameters();
          if (args.size() == node.getRecordComponents().size()) {
            List<Statement> stmts = methodDecl.getBody().getStatements();
            if (stmts.size() == 1
                && stmts.get(0).getKind() == TreeNode.Kind.SUPER_CONSTRUCTOR_INVOCATION) {
              for (VariableElement arg : args) {
                VariableElement field =
                    ElementUtil.findField(record, arg.getSimpleName().toString());
                ExpressionStatement stmt =
                    new ExpressionStatement(
                        new Assignment(
                            new FieldAccess(field, new ThisExpression(record.asType())),
                            new SimpleName(arg)));
                stmts.add(stmt);
              }
            }
          }
        }
      }
    }

    // Add any accessor methods if not explicitly declared.
    for (RecordDeclaration.RecordComponent component : node.getRecordComponents()) {
      MethodDeclaration accessor = component.getAccessor();
      if (accessor == null) {
        // Method explicitly declared.
      } else {
        // Define method body for accessor.
        VariableElement field = ElementUtil.findField(record, component.toString());
        ReturnStatement returnStatement =
            new ReturnStatement().setExpression(new SimpleName(field));
        accessor.setBody(new Block().addStatement(returnStatement));
        accessor.setModifiers(Modifier.PUBLIC);
        node.addBodyDeclaration(accessor);
      }
    }

    maybeAddEquals(node);
    maybeAddHashCode(node);
    maybeAddToString(node);
    node.validate();
  }

  private void maybeAddEquals(RecordDeclaration node) {
    TypeElement record = node.getTypeElement();
    TypeMirror recordType = record.asType();
    ExecutableElement equalsElement = ElementUtil.findMethod(record, "equals", "java.lang.Object");
    if (needsMethodDeclaration(node, equalsElement)) {
      VariableElement arg = equalsElement.getParameters().get(0);
      MethodDeclaration equalsDecl =
          new MethodDeclaration(equalsElement)
              .addParameter(new SingleVariableDeclaration(arg))
              .setHasDeclaration(false);
      Block block = new Block();
      equalsDecl.setBody(block);

      // Add "if (!(o instanceof <record-type>) return false;"
      IfStatement ifStatement = new IfStatement();
      InstanceofExpression instanceofExpression =
          new InstanceofExpression()
              .setTypeMirror(typeUtil.getBoolean())
              .setLeftOperand(new SimpleName(arg))
              .setRightOperand(new SimpleType(recordType));
      ParenthesizedExpression parenthesizedExpression =
          new ParenthesizedExpression(instanceofExpression);
      PrefixExpression prefixExpression =
          new PrefixExpression(
              typeUtil.getBoolean(), PrefixExpression.Operator.NOT, parenthesizedExpression);
      ifStatement.setExpression(prefixExpression);
      ifStatement.setThenStatement(
          new ReturnStatement(new BooleanLiteral(false, typeUtil.getBoolean())));
      block.addStatement(ifStatement);

      // Add "<record-type> other = (<record-type>) o;"
      VariableElement otherVar =
          GeneratedVariableElement.newLocalVar("other", recordType, equalsElement);
      CastExpression castExpr = new CastExpression(recordType, new SimpleName(arg));
      VariableDeclarationFragment varDecl = new VariableDeclarationFragment(otherVar, castExpr);
      block.addStatement(new VariableDeclarationStatement(varDecl));

      // Add "return other.<n1> == <n1> [ && other.<n2> == <n2> ]*"
      InfixExpression infixExpr =
          new InfixExpression()
              .setTypeMirror(typeUtil.getBoolean())
              .setOperator(InfixExpression.Operator.CONDITIONAL_AND);

      for (RecordDeclaration.RecordComponent component : node.getRecordComponents()) {
        // other.<comp> == <comp>
        VariableElement field = ElementUtil.findField(record, component.toString());
        InfixExpression operand =
            new InfixExpression()
                .setTypeMirror(typeUtil.getBoolean())
                .setOperator(InfixExpression.Operator.EQUALS)
                .addOperand(new FieldAccess(field, field.asType(), new SimpleName(otherVar)))
                .addOperand(new SimpleName(field));
        infixExpr.addOperand(operand);
      }
      block.addStatement(new ReturnStatement(infixExpr));
      node.addBodyDeclaration(equalsDecl);
    }
  }

  private void maybeAddHashCode(RecordDeclaration node) {
    TypeElement record = node.getTypeElement();
    ExecutableElement hashCodeElement = ElementUtil.findMethod(record, "hashCode");
    if (needsMethodDeclaration(node, hashCodeElement)) {
      MethodDeclaration hashCodeDecl = new MethodDeclaration(hashCodeElement);
      hashCodeDecl.setHasDeclaration(false);
      Block block = new Block();
      hashCodeDecl.setBody(block);
      TypeElement objectsElement = elementUtil.getTypeElement("java.util.Objects");
      ExecutableElement method =
          ElementUtil.findMethod(objectsElement, "hash", "[java.lang.Object");
      if (method == null) {
        ErrorUtil.fatalError(
            new NoSuchMethodError("java.util.Objects.hash(Object...)"), node.toString());
      }
      MethodInvocation invocation =
          new MethodInvocation(new ExecutablePair(method), new SimpleName(objectsElement))
              .setVarargsType(typeUtil.getJavaObject().asType());
      for (RecordDeclaration.RecordComponent component : node.getRecordComponents()) {
        VariableElement field = ElementUtil.findField(record, component.toString());
        invocation.addArgument(new SimpleName(field));
      }
      block.addStatement(new ReturnStatement(invocation));
      node.addBodyDeclaration(hashCodeDecl);
    }
  }

  private void maybeAddToString(RecordDeclaration node) {
    TypeElement record = node.getTypeElement();
    ExecutableElement toStringElement = ElementUtil.findMethod(record, "toString");
    if (needsMethodDeclaration(node, toStringElement)) {
      MethodDeclaration toStringDecl = new MethodDeclaration(toStringElement);
      toStringDecl.setHasDeclaration(false);
      Block block = new Block();
      toStringDecl.setBody(block);

      // Add: return "<RecordClassName>[<name1>=<value1>, <name2>=<value2> ...]"
      InfixExpression infixExpr =
          new InfixExpression()
              .setTypeMirror(typeUtil.getJavaString().asType())
              .setOperator(InfixExpression.Operator.PLUS)
              .addOperand(new StringLiteral(record.getSimpleName().toString() + "[", typeUtil));
      Iterator<RecordDeclaration.RecordComponent> iter = node.getRecordComponents().iterator();
      String delimiter = "";
      while (iter.hasNext()) {
        RecordDeclaration.RecordComponent component = iter.next();
        infixExpr.addOperand(
            new StringLiteral(
                delimiter + component.getElement().getSimpleName().toString() + "=", typeUtil));
        VariableElement field = ElementUtil.findField(record, component.toString());
        infixExpr.addOperand(new SimpleName(field));
        delimiter = ", ";
      }
      infixExpr.addOperand(new StringLiteral("]", typeUtil));
      block.addStatement(new ReturnStatement(infixExpr));
      node.addBodyDeclaration(toStringDecl);
    }
  }

  // Check whether a method declaration needs to be created for a specified executable
  // element. Record class declarations only have method declarations if there's code
  // for those methods, while their associated element encloses all methods the record
  // defines, where the source file declares them. These include default accessors, as
  // well as equals(), hashCode() and toString().
  private boolean needsMethodDeclaration(RecordDeclaration node, ExecutableElement methodSymbol) {
    for (BodyDeclaration member : node.getBodyDeclarations()) {
      if (member.getKind() == TreeNode.Kind.METHOD_DECLARATION) {
        if (((MethodDeclaration) member).getExecutableElement() == methodSymbol) {
          return false;
        }
      }
    }
    return true;
  }
}
