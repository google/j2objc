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

import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Converts a Java AST from the JDT data structure to our J2ObjC data structure.
 */
public class TreeConverter {

  public static CompilationUnit convertCompilationUnit(
      org.eclipse.jdt.core.dom.CompilationUnit jdtUnit, String sourceFilePath, String mainTypeName,
      String source, NameTable.Factory nameTableFactory) {
    return new CompilationUnit(jdtUnit, sourceFilePath, mainTypeName, source, nameTableFactory);
  }

  public static Statement convertStatement(org.eclipse.jdt.core.dom.Statement jdtStatement) {
    return (Statement) convert(jdtStatement);
  }

  public static TreeNode convert(Object obj) {
    if (obj == null) {
      return null;
    }
    ASTNode jdtNode = (ASTNode) obj;
    TreeNode node = convertInner(jdtNode);
    node.validate();
    return node;
  }

  public static TreeNode convertInner(ASTNode jdtNode) {
    switch (jdtNode.getNodeType()) {
      case ASTNode.ANNOTATION_TYPE_DECLARATION:
        return new AnnotationTypeDeclaration(
            (org.eclipse.jdt.core.dom.AnnotationTypeDeclaration) jdtNode);
      case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
        return new AnnotationTypeMemberDeclaration(
            (org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration) jdtNode);
      case ASTNode.ANONYMOUS_CLASS_DECLARATION:
        return new AnonymousClassDeclaration(
            (org.eclipse.jdt.core.dom.AnonymousClassDeclaration) jdtNode);
      case ASTNode.ARRAY_ACCESS:
        return new ArrayAccess((org.eclipse.jdt.core.dom.ArrayAccess) jdtNode);
      case ASTNode.ARRAY_CREATION:
        return new ArrayCreation((org.eclipse.jdt.core.dom.ArrayCreation) jdtNode);
      case ASTNode.ARRAY_INITIALIZER:
        return new ArrayInitializer((org.eclipse.jdt.core.dom.ArrayInitializer) jdtNode);
      case ASTNode.ARRAY_TYPE:
        return new ArrayType((org.eclipse.jdt.core.dom.ArrayType) jdtNode);
      case ASTNode.ASSERT_STATEMENT:
        return new AssertStatement((org.eclipse.jdt.core.dom.AssertStatement) jdtNode);
      case ASTNode.ASSIGNMENT:
        return new Assignment((org.eclipse.jdt.core.dom.Assignment) jdtNode);
      case ASTNode.BLOCK:
        return new Block((org.eclipse.jdt.core.dom.Block) jdtNode);
      case ASTNode.BLOCK_COMMENT:
        return new BlockComment((org.eclipse.jdt.core.dom.BlockComment) jdtNode);
      case ASTNode.BOOLEAN_LITERAL:
        return new BooleanLiteral((org.eclipse.jdt.core.dom.BooleanLiteral) jdtNode);
      case ASTNode.BREAK_STATEMENT:
        return new BreakStatement((org.eclipse.jdt.core.dom.BreakStatement) jdtNode);
      case ASTNode.CAST_EXPRESSION:
        return new CastExpression((org.eclipse.jdt.core.dom.CastExpression) jdtNode);
      case ASTNode.CATCH_CLAUSE:
        return new CatchClause((org.eclipse.jdt.core.dom.CatchClause) jdtNode);
      case ASTNode.CHARACTER_LITERAL:
        return new CharacterLiteral((org.eclipse.jdt.core.dom.CharacterLiteral) jdtNode);
      case ASTNode.CLASS_INSTANCE_CREATION:
        return new ClassInstanceCreation((org.eclipse.jdt.core.dom.ClassInstanceCreation) jdtNode);
      case ASTNode.CONDITIONAL_EXPRESSION:
        return new ConditionalExpression((org.eclipse.jdt.core.dom.ConditionalExpression) jdtNode);
      case ASTNode.CONSTRUCTOR_INVOCATION:
        return new ConstructorInvocation((org.eclipse.jdt.core.dom.ConstructorInvocation) jdtNode);
      case ASTNode.CONTINUE_STATEMENT:
        return new ContinueStatement((org.eclipse.jdt.core.dom.ContinueStatement) jdtNode);
      case ASTNode.DO_STATEMENT:
        return new DoStatement((org.eclipse.jdt.core.dom.DoStatement) jdtNode);
      case ASTNode.EMPTY_STATEMENT:
        return new EmptyStatement((org.eclipse.jdt.core.dom.EmptyStatement) jdtNode);
      case ASTNode.ENHANCED_FOR_STATEMENT:
        return new EnhancedForStatement((org.eclipse.jdt.core.dom.EnhancedForStatement) jdtNode);
      case ASTNode.ENUM_CONSTANT_DECLARATION:
        return new EnumConstantDeclaration(
            (org.eclipse.jdt.core.dom.EnumConstantDeclaration) jdtNode);
      case ASTNode.ENUM_DECLARATION:
        return new EnumDeclaration((org.eclipse.jdt.core.dom.EnumDeclaration) jdtNode);
      case ASTNode.EXPRESSION_STATEMENT:
        return new ExpressionStatement((org.eclipse.jdt.core.dom.ExpressionStatement) jdtNode);
      case ASTNode.FIELD_ACCESS:
        return new FieldAccess((org.eclipse.jdt.core.dom.FieldAccess) jdtNode);
      case ASTNode.FIELD_DECLARATION:
        return new FieldDeclaration((org.eclipse.jdt.core.dom.FieldDeclaration) jdtNode);
      case ASTNode.FOR_STATEMENT:
        return new ForStatement((org.eclipse.jdt.core.dom.ForStatement) jdtNode);
      case ASTNode.IF_STATEMENT:
        return new IfStatement((org.eclipse.jdt.core.dom.IfStatement) jdtNode);
      case ASTNode.INFIX_EXPRESSION:
        return new InfixExpression((org.eclipse.jdt.core.dom.InfixExpression) jdtNode);
      case ASTNode.INITIALIZER:
        return new Initializer((org.eclipse.jdt.core.dom.Initializer) jdtNode);
      case ASTNode.INSTANCEOF_EXPRESSION:
        return new InstanceofExpression((org.eclipse.jdt.core.dom.InstanceofExpression) jdtNode);
      case ASTNode.JAVADOC:
        return new Javadoc((org.eclipse.jdt.core.dom.Javadoc) jdtNode);
      case ASTNode.LABELED_STATEMENT:
        return new LabeledStatement((org.eclipse.jdt.core.dom.LabeledStatement) jdtNode);
      case ASTNode.LINE_COMMENT:
        return new LineComment((org.eclipse.jdt.core.dom.LineComment) jdtNode);
      case ASTNode.MARKER_ANNOTATION:
        return new MarkerAnnotation((org.eclipse.jdt.core.dom.MarkerAnnotation) jdtNode);
      case ASTNode.MEMBER_VALUE_PAIR:
        return new MemberValuePair((org.eclipse.jdt.core.dom.MemberValuePair) jdtNode);
      case ASTNode.METHOD_DECLARATION:
        return new MethodDeclaration((org.eclipse.jdt.core.dom.MethodDeclaration) jdtNode);
      case ASTNode.METHOD_INVOCATION:
        return new MethodInvocation((org.eclipse.jdt.core.dom.MethodInvocation) jdtNode);
      case ASTNode.NORMAL_ANNOTATION:
        return new NormalAnnotation((org.eclipse.jdt.core.dom.NormalAnnotation) jdtNode);
      case ASTNode.NULL_LITERAL:
        return new NullLiteral((org.eclipse.jdt.core.dom.NullLiteral) jdtNode);
      case ASTNode.NUMBER_LITERAL:
        return new NumberLiteral((org.eclipse.jdt.core.dom.NumberLiteral) jdtNode);
      case ASTNode.PACKAGE_DECLARATION:
        return new PackageDeclaration((org.eclipse.jdt.core.dom.PackageDeclaration) jdtNode);
      case ASTNode.PARAMETERIZED_TYPE:
        return new ParameterizedType((org.eclipse.jdt.core.dom.ParameterizedType) jdtNode);
      case ASTNode.PARENTHESIZED_EXPRESSION:
        return new ParenthesizedExpression(
            (org.eclipse.jdt.core.dom.ParenthesizedExpression) jdtNode);
      case ASTNode.POSTFIX_EXPRESSION:
        return new PostfixExpression((org.eclipse.jdt.core.dom.PostfixExpression) jdtNode);
      case ASTNode.PREFIX_EXPRESSION:
        return new PrefixExpression((org.eclipse.jdt.core.dom.PrefixExpression) jdtNode);
      case ASTNode.PRIMITIVE_TYPE:
        return new PrimitiveType((org.eclipse.jdt.core.dom.PrimitiveType) jdtNode);
      case ASTNode.QUALIFIED_NAME:
        return new QualifiedName((org.eclipse.jdt.core.dom.QualifiedName) jdtNode);
      case ASTNode.QUALIFIED_TYPE:
        return new QualifiedType((org.eclipse.jdt.core.dom.QualifiedType) jdtNode);
      case ASTNode.RETURN_STATEMENT:
        return new ReturnStatement((org.eclipse.jdt.core.dom.ReturnStatement) jdtNode);
      case ASTNode.SIMPLE_NAME:
        return new SimpleName((org.eclipse.jdt.core.dom.SimpleName) jdtNode);
      case ASTNode.SIMPLE_TYPE:
        return new SimpleType((org.eclipse.jdt.core.dom.SimpleType) jdtNode);
      case ASTNode.SINGLE_MEMBER_ANNOTATION:
        return new SingleMemberAnnotation(
            (org.eclipse.jdt.core.dom.SingleMemberAnnotation) jdtNode);
      case ASTNode.SINGLE_VARIABLE_DECLARATION:
        return new SingleVariableDeclaration(
            (org.eclipse.jdt.core.dom.SingleVariableDeclaration) jdtNode);
      case ASTNode.STRING_LITERAL:
        return new StringLiteral((org.eclipse.jdt.core.dom.StringLiteral) jdtNode);
      case ASTNode.SUPER_CONSTRUCTOR_INVOCATION:
        return new SuperConstructorInvocation(
            (org.eclipse.jdt.core.dom.SuperConstructorInvocation) jdtNode);
      case ASTNode.SUPER_FIELD_ACCESS:
        return new SuperFieldAccess((org.eclipse.jdt.core.dom.SuperFieldAccess) jdtNode);
      case ASTNode.SUPER_METHOD_INVOCATION:
        return new SuperMethodInvocation((org.eclipse.jdt.core.dom.SuperMethodInvocation) jdtNode);
      case ASTNode.SWITCH_CASE:
        return new SwitchCase((org.eclipse.jdt.core.dom.SwitchCase) jdtNode);
      case ASTNode.SWITCH_STATEMENT:
        return new SwitchStatement((org.eclipse.jdt.core.dom.SwitchStatement) jdtNode);
      case ASTNode.SYNCHRONIZED_STATEMENT:
        return new SynchronizedStatement((org.eclipse.jdt.core.dom.SynchronizedStatement) jdtNode);
      case ASTNode.TAG_ELEMENT:
        return new TagElement((org.eclipse.jdt.core.dom.TagElement) jdtNode);
      case ASTNode.TEXT_ELEMENT:
        return new TextElement((org.eclipse.jdt.core.dom.TextElement) jdtNode);
      case ASTNode.THIS_EXPRESSION:
        return new ThisExpression((org.eclipse.jdt.core.dom.ThisExpression) jdtNode);
      case ASTNode.THROW_STATEMENT:
        return new ThrowStatement((org.eclipse.jdt.core.dom.ThrowStatement) jdtNode);
      case ASTNode.TRY_STATEMENT:
        return new TryStatement((org.eclipse.jdt.core.dom.TryStatement) jdtNode);
      case ASTNode.TYPE_DECLARATION:
        return new TypeDeclaration((org.eclipse.jdt.core.dom.TypeDeclaration) jdtNode);
      case ASTNode.TYPE_DECLARATION_STATEMENT:
        return new TypeDeclarationStatement(
            (org.eclipse.jdt.core.dom.TypeDeclarationStatement) jdtNode);
      case ASTNode.TYPE_LITERAL:
        return new TypeLiteral((org.eclipse.jdt.core.dom.TypeLiteral) jdtNode);
      case ASTNode.UNION_TYPE:
        return new UnionType((org.eclipse.jdt.core.dom.UnionType) jdtNode);
      case ASTNode.VARIABLE_DECLARATION_EXPRESSION:
        return new VariableDeclarationExpression(
            (org.eclipse.jdt.core.dom.VariableDeclarationExpression) jdtNode);
      case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
        return new VariableDeclarationFragment(
            (org.eclipse.jdt.core.dom.VariableDeclarationFragment) jdtNode);
      case ASTNode.VARIABLE_DECLARATION_STATEMENT:
        return new VariableDeclarationStatement(
            (org.eclipse.jdt.core.dom.VariableDeclarationStatement) jdtNode);
      case ASTNode.WHILE_STATEMENT:
        return new WhileStatement((org.eclipse.jdt.core.dom.WhileStatement) jdtNode);
      // These nodes only appear in comments and J2ObjC doens't need any
      // information from their subtree so we just convert them to TextElement.
      case ASTNode.MEMBER_REF:
      case ASTNode.METHOD_REF:
      case ASTNode.METHOD_REF_PARAMETER:
        return new TextElement(jdtNode);
      case ASTNode.COMPILATION_UNIT:
        throw new AssertionError(
            "CompilationUnit must be converted using convertCompilationUnit()");
      default:
        throw new AssertionError("Unknown node type: " + jdtNode.getClass().getName());
    }
  }
}
