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

package com.google.devtools.j2objc.javac;

import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ParserEnvironment;
import com.google.devtools.j2objc.util.TranslationEnvironment;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.strobel.decompiler.languages.java.ast.AstNode;
import com.strobel.decompiler.languages.java.ast.BlockStatement;
import com.strobel.decompiler.languages.java.ast.IAstVisitor;
import com.strobel.decompiler.patterns.Pattern;
import java.util.function.Consumer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

/**
 * Procyon AST visitor that converts method/constructor bodies.
 *
 * @author Manvith Narahari
 */
class MethodTranslator implements IAstVisitor<Void, TreeNode> {
  private final ParserEnvironment parserEnv;
  private final TranslationEnvironment translationEnv;
  private final ExecutableElement element;
  private final TypeDeclaration typeDecl;

  public MethodTranslator(ParserEnvironment parserEnv, TranslationEnvironment translationEnv,
                          ExecutableElement element, TypeDeclaration typeDecl) {
    this.parserEnv = parserEnv;
    this.translationEnv = translationEnv;
    this.element = element;
    this.typeDecl = typeDecl;
  }

  protected void visitChildren(final AstNode node, Consumer<TreeNode> builder) {
    for (AstNode child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      builder.accept(child.acceptVisitor(this, null));
    }
  }

  /*
   * TODO(user): this is a hack so that generated nodes get carried up to the top level
   * instead of throwing a null pointer exception; another way is to replace these method calls
   * with return null and extract mappings to BlockStatement instead of MethodDeclaration but we'll
   * still have to deal with null pointers for other nodes.
   */
  protected TreeNode visitChildren(final AstNode node) {
    for (AstNode child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
      TreeNode tn = child.acceptVisitor(this, null);
      if (tn != null) {
        return tn;
      }
    }
    return null;
  }

  @Override
  public TreeNode visitComment(com.strobel.decompiler.languages.java.ast.Comment node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitPatternPlaceholder(AstNode node, Pattern pattern, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitInvocationExpression(
      com.strobel.decompiler.languages.java.ast.InvocationExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitTypeReference(
      com.strobel.decompiler.languages.java.ast.TypeReferenceExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitJavaTokenNode(
      com.strobel.decompiler.languages.java.ast.JavaTokenNode node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitMemberReferenceExpression(
      com.strobel.decompiler.languages.java.ast.MemberReferenceExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitIdentifier(
      com.strobel.decompiler.languages.java.ast.Identifier node, Void data) {
    SimpleName name = new SimpleName()
        .setIdentifier(node.getName())
        .setTypeMirror(null);
    visitChildren(node, (TreeNode expr) -> {});
    return name;
  }

  @Override
  public TreeNode visitNullReferenceExpression(
      com.strobel.decompiler.languages.java.ast.NullReferenceExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitThisReferenceExpression(
      com.strobel.decompiler.languages.java.ast.ThisReferenceExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitSuperReferenceExpression(
      com.strobel.decompiler.languages.java.ast.SuperReferenceExpression node, Void data) {
    TypeMirror objType = translationEnv.typeUtil().getJavaObject().asType();
    TypeMirror nodeType = typeDecl.getTypeElement().asType();
    assert !parserEnv.typeUtilities().isSameType(objType, nodeType);
    TypeElement superClass = TranslationUtil.getSuperType(typeDecl);
    SuperConstructorInvocation superCall = null;
    for (ExecutableElement exec : ElementUtil.getConstructors(superClass)) {
      if (exec.getParameters().size() == 0) {
        ExecutableType execType = translationEnv.typeUtil()
            .asMemberOf((DeclaredType) superClass.asType(), exec);
        superCall = new SuperConstructorInvocation()
            .setExecutablePair(new ExecutablePair(exec, execType));
        break;
      }
    }
    visitChildren(node, (TreeNode expr) -> {});
    return superCall;
  }

  @Override
  public TreeNode visitClassOfExpression(
      com.strobel.decompiler.languages.java.ast.ClassOfExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitBlockStatement(
      com.strobel.decompiler.languages.java.ast.BlockStatement node, Void data) {
    Block block = new Block();
    visitChildren(node, (TreeNode stmt) -> block.addStatement((Statement) stmt));
    return block;
  }

  @Override
  public TreeNode visitExpressionStatement(
      com.strobel.decompiler.languages.java.ast.ExpressionStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitBreakStatement(
      com.strobel.decompiler.languages.java.ast.BreakStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitContinueStatement(
      com.strobel.decompiler.languages.java.ast.ContinueStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitDoWhileStatement(
      com.strobel.decompiler.languages.java.ast.DoWhileStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitEmptyStatement(
      com.strobel.decompiler.languages.java.ast.EmptyStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitIfElseStatement(
      com.strobel.decompiler.languages.java.ast.IfElseStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitLabelStatement(
      com.strobel.decompiler.languages.java.ast.LabelStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitLabeledStatement(
      com.strobel.decompiler.languages.java.ast.LabeledStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitReturnStatement(
      com.strobel.decompiler.languages.java.ast.ReturnStatement node, Void data) {
    ReturnStatement returnStatement = new ReturnStatement();
    visitChildren(node, (TreeNode expr) -> returnStatement.setExpression((Expression) expr));
    return returnStatement;
  }

  @Override
  public TreeNode visitSwitchStatement(
      com.strobel.decompiler.languages.java.ast.SwitchStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitSwitchSection(
      com.strobel.decompiler.languages.java.ast.SwitchSection node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitCaseLabel(
      com.strobel.decompiler.languages.java.ast.CaseLabel node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitThrowStatement(
      com.strobel.decompiler.languages.java.ast.ThrowStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitCatchClause(
      com.strobel.decompiler.languages.java.ast.CatchClause node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitAnnotation(
      com.strobel.decompiler.languages.java.ast.Annotation node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitNewLine(
      com.strobel.decompiler.languages.java.ast.NewLineNode node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitVariableDeclaration(
      com.strobel.decompiler.languages.java.ast.VariableDeclarationStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitVariableInitializer(
      com.strobel.decompiler.languages.java.ast.VariableInitializer node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitText(com.strobel.decompiler.languages.java.ast.TextNode node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitImportDeclaration(
      com.strobel.decompiler.languages.java.ast.ImportDeclaration node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitSimpleType(
      com.strobel.decompiler.languages.java.ast.SimpleType node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitMethodDeclaration(
      com.strobel.decompiler.languages.java.ast.MethodDeclaration node, Void data) {
    return visitBlockStatement((BlockStatement) node.getLastChild(), data);
  }

  @Override
  public TreeNode visitInitializerBlock(
      com.strobel.decompiler.languages.java.ast.InstanceInitializer node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitConstructorDeclaration(
      com.strobel.decompiler.languages.java.ast.ConstructorDeclaration node, Void data) {
    return visitBlockStatement((BlockStatement) node.getLastChild(), data);
  }

  @Override
  public TreeNode visitTypeParameterDeclaration(
      com.strobel.decompiler.languages.java.ast.TypeParameterDeclaration node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitParameterDeclaration(
      com.strobel.decompiler.languages.java.ast.ParameterDeclaration node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitFieldDeclaration(
      com.strobel.decompiler.languages.java.ast.FieldDeclaration node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitTypeDeclaration(
      com.strobel.decompiler.languages.java.ast.TypeDeclaration node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitCompilationUnit(
      com.strobel.decompiler.languages.java.ast.CompilationUnit node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitPackageDeclaration(
      com.strobel.decompiler.languages.java.ast.PackageDeclaration node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitArraySpecifier(
      com.strobel.decompiler.languages.java.ast.ArraySpecifier node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitComposedType(
      com.strobel.decompiler.languages.java.ast.ComposedType node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitWhileStatement(
      com.strobel.decompiler.languages.java.ast.WhileStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitPrimitiveExpression(
      com.strobel.decompiler.languages.java.ast.PrimitiveExpression node, Void data) {
    Expression expr = TreeUtil.newLiteral(node.getValue(), translationEnv.typeUtil());
    visitChildren(node, (obj) -> {});
    return expr;
  }

  @Override
  public TreeNode visitCastExpression(
      com.strobel.decompiler.languages.java.ast.CastExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitBinaryOperatorExpression(
      com.strobel.decompiler.languages.java.ast.BinaryOperatorExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitInstanceOfExpression(
      com.strobel.decompiler.languages.java.ast.InstanceOfExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitIndexerExpression(
      com.strobel.decompiler.languages.java.ast.IndexerExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitIdentifierExpression(
      com.strobel.decompiler.languages.java.ast.IdentifierExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitUnaryOperatorExpression(
      com.strobel.decompiler.languages.java.ast.UnaryOperatorExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitConditionalExpression(
      com.strobel.decompiler.languages.java.ast.ConditionalExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitArrayInitializerExpression(
      com.strobel.decompiler.languages.java.ast.ArrayInitializerExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitObjectCreationExpression(
      com.strobel.decompiler.languages.java.ast.ObjectCreationExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitArrayCreationExpression(
      com.strobel.decompiler.languages.java.ast.ArrayCreationExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitAssignmentExpression(
      com.strobel.decompiler.languages.java.ast.AssignmentExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitForStatement(
      com.strobel.decompiler.languages.java.ast.ForStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitForEachStatement(
      com.strobel.decompiler.languages.java.ast.ForEachStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitTryCatchStatement(
      com.strobel.decompiler.languages.java.ast.TryCatchStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitGotoStatement(
      com.strobel.decompiler.languages.java.ast.GotoStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitParenthesizedExpression(
      com.strobel.decompiler.languages.java.ast.ParenthesizedExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitSynchronizedStatement(
      com.strobel.decompiler.languages.java.ast.SynchronizedStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitAnonymousObjectCreationExpression(
      com.strobel.decompiler.languages.java.ast.AnonymousObjectCreationExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitWildcardType(
      com.strobel.decompiler.languages.java.ast.WildcardType node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitMethodGroupExpression(
      com.strobel.decompiler.languages.java.ast.MethodGroupExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitEnumValueDeclaration(
      com.strobel.decompiler.languages.java.ast.EnumValueDeclaration node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitAssertStatement(
      com.strobel.decompiler.languages.java.ast.AssertStatement node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitLambdaExpression(
      com.strobel.decompiler.languages.java.ast.LambdaExpression node, Void data) {
    return visitChildren(node);
  }

  @Override
  public TreeNode visitLocalTypeDeclarationStatement(
      com.strobel.decompiler.languages.java.ast.LocalTypeDeclarationStatement node, Void data) {
    return visitChildren(node);
  }
}
