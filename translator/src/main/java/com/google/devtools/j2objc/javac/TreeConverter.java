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

import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.ArrayAccess;
import com.google.devtools.j2objc.ast.ArrayType;
import com.google.devtools.j2objc.ast.AssertStatement;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BlockComment;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.BreakStatement;
import com.google.devtools.j2objc.ast.CatchClause;
import com.google.devtools.j2objc.ast.Comment;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConditionalExpression;
import com.google.devtools.j2objc.ast.ContinueStatement;
import com.google.devtools.j2objc.ast.DoStatement;
import com.google.devtools.j2objc.ast.EnhancedForStatement;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.Javadoc;
import com.google.devtools.j2objc.ast.LineComment;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.SourcePosition;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SwitchCase;
import com.google.devtools.j2objc.ast.SwitchStatement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.file.JarredInputFile;
import com.google.devtools.j2objc.file.RegularInputFile;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.ParserEnvironment;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.tree.DocCommentTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Position;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

/**
 * Converts a Java AST from the JDT data structure to our J2ObjC data structure.
 */
public class TreeConverter {
  private JCTree.JCCompilationUnit unit;

  public static CompilationUnit convertCompilationUnit(
      ParserEnvironment env, JCTree.JCCompilationUnit javacUnit) {
    TreeConverter converter = new TreeConverter(javacUnit);
    String sourceFilePath = javacUnit.getSourceFile().toUri().getPath();
    InputFile sourceFile = convertFileObject(javacUnit.getSourceFile());
    String source = getSource(sourceFile);
    String mainTypeName = FileUtil.getMainTypeName(sourceFile);
    CompilationUnit unit = new CompilationUnit(env, sourceFilePath, mainTypeName, source);
    unit.setPackage(new PackageDeclaration().setPackageElement(javacUnit.packge));
    for (JCTree type : javacUnit.getTypeDecls()) {
      unit.addType((AbstractTypeDeclaration) converter.convert(type));
    }

    // TODO(tball): add unparented comments, once comment scanner is implemented.
    return unit;
  }

  private static InputFile convertFileObject(JavaFileObject fileObject) {
    assert fileObject.getKind() == JavaFileObject.Kind.SOURCE;
    URI uri = fileObject.toUri();
    String scheme = uri.getScheme();
    if (scheme.equals("file")) {
      return new RegularInputFile(uri.getPath());
    } else if (scheme.equals("jar")) {
      String path = uri.getPath();
      if (path.startsWith("file:")) {
        path = path.substring(5);
      }
      int bang = path.indexOf('!');
      String jarPath = path.substring(0, bang);
      String internalPath = path.substring(bang + 1);
      return new JarredInputFile(jarPath, internalPath);
    }
    return null;
  }

  private static String getSource(InputFile file) {
    try (Reader reader = file.openReader(); StringWriter writer = new StringWriter()) {
      char[] buf = new char[4096];
      int n;
      while ((n = reader.read(buf)) > 0) {
        writer.write(buf, 0, n);
      }
      return writer.toString();
    } catch (IOException e) {
      throw new AssertionError("failed re-reading source file: " + file.getPath(), e);
    }
  }

  private TreeConverter(JCTree.JCCompilationUnit javacUnit) {
    unit = javacUnit;
  }

  private TreeNode convert(Object obj) {
    if (obj == null) {
      return null;
    }
    JCTree node = (JCTree) obj;
    TreeNode newNode = convertInner(node)
        .setPosition(getPosition(node));
    newNode.validate();
    return newNode;
  }

  private SourcePosition getPosition(JCTree node) {
    int startPosition = TreeInfo.getStartPos(node);
    int endPosition = TreeInfo.getEndPos(node, unit.endPositions);
    int length = startPosition == Position.NOPOS || endPosition == Position.NOPOS
        ? 0 : endPosition - startPosition;
    if (unit.getLineMap() != null) {
      int line = unit.getLineMap().getLineNumber(startPosition);
      return new SourcePosition(startPosition, length, line);
    } else {
      return new SourcePosition(startPosition, length);
    }
  }

  @SuppressWarnings("fallthrough")
  private TreeNode convertInner(JCTree javacNode) {
    switch (javacNode.getKind()) {
      case ANNOTATION_TYPE:
        return convertAnnotationTypeDeclaration((JCTree.JCClassDecl) javacNode);
      case ARRAY_ACCESS:
        return convertArrayAccess((JCTree.JCArrayAccess) javacNode);
      case ARRAY_TYPE:
        return convertArrayType((JCTree.JCArrayTypeTree) javacNode);
      case ASSERT:
        return convertAssert((JCTree.JCAssert) javacNode);
      case ASSIGNMENT:
        return convertAssignment((JCTree.JCAssign) javacNode);
      case BLOCK:
        return convertBlock((JCTree.JCBlock) javacNode);
      case BREAK:
        return convertBreakStatement((JCTree.JCBreak) javacNode);
      case CASE:
        return convertCase((JCTree.JCCase) javacNode);
      case CATCH:
        return convertCatch((JCTree.JCCatch) javacNode);
      case CLASS:
        return convertTypeDeclaration((JCTree.JCClassDecl) javacNode);
      case COMPILATION_UNIT:
        throw new AssertionError(
            "CompilationUnit must be converted using convertCompilationUnit()");
      case CONDITIONAL_EXPRESSION:
        return convertConditionalExpression((JCTree.JCConditional) javacNode);
      case CONTINUE:
        return convertContinueStatement((JCTree.JCContinue) javacNode);
      case DO_WHILE_LOOP:
        return convertDoStatement((JCTree.JCDoWhileLoop) javacNode);
      case ENHANCED_FOR_LOOP:
        return convertEnhancedForStatement((JCTree.JCEnhancedForLoop) javacNode);
      case EXPRESSION_STATEMENT:
        return convertExpressionStatement((JCTree.JCExpressionStatement) javacNode);
      case METHOD:
        return convertMethodDeclaration((JCTree.JCMethodDecl) javacNode);
      case SWITCH:
        return convertSwitch((JCTree.JCSwitch) javacNode);

      case AND_ASSIGNMENT:
      case DIVIDE_ASSIGNMENT:
      case LEFT_SHIFT_ASSIGNMENT:
      case MINUS_ASSIGNMENT:
      case MULTIPLY_ASSIGNMENT:
      case OR_ASSIGNMENT:
      case PLUS_ASSIGNMENT:
      case REMAINDER_ASSIGNMENT:
      case RIGHT_SHIFT_ASSIGNMENT:
      case UNSIGNED_RIGHT_SHIFT_ASSIGNMENT:
      case XOR_ASSIGNMENT:
        return convertAssignOp((JCTree.JCAssignOp) javacNode);

      default:
        throw new AssertionError("Unknown node type: " + javacNode.getClass().getName());
    }
  }

  private TreeNode convertAbstractTypeDeclaration(
      JCTree.JCClassDecl node, AbstractTypeDeclaration newNode) {
    convertBodyDeclaration(node, newNode);
    List<BodyDeclaration> bodyDeclarations = new ArrayList<>();
    for (Object bodyDecl : node.getMembers()) {
      bodyDeclarations.add((BodyDeclaration) convert(bodyDecl));
    }
    return newNode
        .setName(convertName(node.sym))
        .setTypeElement(node.sym)
        .setBodyDeclarations(bodyDeclarations);
  }

  private SimpleName convertName(Element element) {
    return new SimpleName(element);
  }

  private TreeNode convertAnnotationTypeDeclaration(JCTree.JCClassDecl node) {
    return convertAbstractTypeDeclaration(node, new AnnotationTypeDeclaration());
  }

  private TreeNode convertConditionalExpression(JCTree.JCConditional node) {
    return new ConditionalExpression()
        .setTypeMirror(node.type)
        .setExpression((Expression) convert(node.getCondition()))
        .setThenExpression((Expression) convert(node.getTrueExpression()))
        .setElseExpression((Expression) convert(node.getFalseExpression()));
  }

  private TreeNode convertContinueStatement(JCTree.JCContinue node) {
    return new ContinueStatement()
        .setLabel((SimpleName) convert(node.getLabel()));
  }

  private TreeNode convertDoStatement(JCTree.JCDoWhileLoop node) {
    return new DoStatement()
        .setExpression((Expression) convert(node.getCondition()))
        .setBody((Statement) convert(node.getStatement()));
  }

  private TreeNode convertArrayAccess(JCTree.JCArrayAccess node) {
    return new ArrayAccess()
        .setArray((Expression) convert(node.getExpression()))
        .setIndex((Expression) convert(node.getIndex()));
  }

  private TreeNode convertArrayType(JCTree.JCArrayTypeTree node) {
    ArrayType newNode = new ArrayType();
    convertType(node, newNode);
    Type componentType = (Type) Type.newType(node.getType().type);
    return newNode.setComponentType(componentType);
  }

  private TreeNode convertAssert(JCTree.JCAssert node) {
    return new AssertStatement()
        .setExpression((Expression) convert(node.getCondition()))
        .setMessage((Expression) convert(node.getDetail()));
  }

  private TreeNode convertAssignment(JCTree.JCAssign node) {
    Assignment newNode = new Assignment();
    convertExpression(node, newNode);
    return newNode
        .setOperator(Assignment.Operator.ASSIGN)
        .setLeftHandSide((Expression) convert(node.getVariable()))
        .setRightHandSide((Expression) convert(node.getExpression()));
  }

  private TreeNode convertAssignOp(JCTree.JCAssignOp node) {
    Assignment newNode = new Assignment();
    convertExpression(node, newNode);
    String operatorName = node.getOperator().getSimpleName().toString();
    return newNode
        .setOperator(Assignment.Operator.fromJdtOperatorName(operatorName))
        .setLeftHandSide((Expression) convert(node.getVariable()))
        .setRightHandSide((Expression) convert(node.getExpression()));
  }

  private TreeNode convertBlock(JCTree.JCBlock node) {
    Block newNode = new Block();
    for (StatementTree stmt : node.getStatements()) {
      newNode.addStatement((Statement) convert(stmt));
    }
    return newNode;
  }

  private TreeNode convertBodyDeclaration(JCTree.JCClassDecl node, BodyDeclaration newNode) {
    List<Annotation> annotations = new ArrayList<>();
    for (AnnotationTree annotation : node.getModifiers().getAnnotations()) {
      annotations.add((Annotation) convert(annotation));
    }
    int flags = convertFlags(node.getModifiers().getFlags());
    return newNode
        .setModifiers(flags)
        .setAnnotations(annotations)
        .setJavadoc((Javadoc) getAssociatedJavaDoc(node));
  }

  private TreeNode convertBreakStatement(JCTree.JCBreak node) {
    BreakStatement newNode = new BreakStatement();
    newNode.setLabel((SimpleName) convert(node.getLabel()));
    return newNode;
  }

  private TreeNode convertCase(JCTree.JCCase node) {
    // Case statements are converted in convertSwitch().
    return new SwitchCase()
        .setExpression((Expression) convert(node.getExpression()));
  }

  private TreeNode convertCatch(JCTree.JCCatch node) {
    return new CatchClause()
        .setException((SingleVariableDeclaration) convert(node.getParameter()))
        .setBody((Block) convert(node.getBlock()));
  }

  private TreeNode convertEnhancedForStatement(JCTree.JCEnhancedForLoop node) {
    return new EnhancedForStatement()
        .setParameter((SingleVariableDeclaration) convert(node.getVariable()))
        .setExpression((Expression) convert(node.getExpression()))
        .setBody((Statement) convert(node.getStatement()));
  }

  private TreeNode convertExpression(
      JCTree.JCExpression node, Expression newNode) {
    return newNode
        .setConstantValue(node.type.constValue());
  }

  private TreeNode convertExpressionStatement(JCTree.JCExpressionStatement node) {
    return new ExpressionStatement()
        .setExpression((Expression) convert(node.getExpression()));
  }

  private TreeNode convertMethodDeclaration(JCTree.JCMethodDecl node) {
    MethodDeclaration newNode = new MethodDeclaration();
    List<Annotation> annotations = new ArrayList<>();
    for (AnnotationTree annotation : node.getModifiers().getAnnotations()) {
      annotations.add((Annotation) convert(annotation));
    }
    int flags = convertFlags(node.getModifiers().getFlags());
    return newNode
        .setModifiers(flags)
        .setAnnotations(annotations)
        .setJavadoc((Javadoc) convert(getAssociatedJavaDoc(node)));
  }

  private TreeNode convertSwitch(JCTree.JCSwitch node) {
    SwitchStatement newNode = new SwitchStatement()
        .setExpression((Expression) convert(node.getExpression()));
    for (JCTree.JCCase switchCase : node.getCases()) {
      newNode.addStatement((SwitchCase) convert(switchCase));
      for (JCTree.JCStatement s : switchCase.getStatements()) {
        newNode.addStatement((Statement) convert(s));
      }
    }
    return newNode;
  }

  private TreeNode convertType(JCTree.JCExpression node, Type newNode) {
    return newNode
        .setTypeMirror(node.type);
  }

  private TreeNode convertTypeDeclaration(JCTree.JCClassDecl node) {
    TypeDeclaration newNode =
        (TypeDeclaration) convertAbstractTypeDeclaration(node, new TypeDeclaration());

    newNode.setSuperclassType((Type) convert(node.getExtendsClause()));
    newNode.setInterface(
        node.getKind() == Kind.INTERFACE || node.getKind() == Kind.ANNOTATION_TYPE);
    for (Object superInterface : node.getImplementsClause()) {
      newNode.addSuperInterfaceType((Type) convert(superInterface));
    }
    return newNode;
  }

  private TreeNode getAssociatedJavaDoc(JCTree node) {
    Comment comment = convertAssociatedComment(node);
    return comment.isDocComment() ? comment : null;
  }

  private Comment convertAssociatedComment(JCTree node) {
    DocCommentTable docComments = unit.docComments;
    if (docComments == null || !docComments.hasComment(node)) {
      return null;
    }
    com.sun.tools.javac.parser.Tokens.Comment javacComment = docComments.getComment(node);
    Comment comment;
    switch (javacComment.getStyle()) {
      case BLOCK:
        comment = new BlockComment();
        break;
      case JAVADOC:
        comment = new Javadoc();
        break;
      case LINE:
        comment = new LineComment();
        break;
      default:
        throw new AssertionError("unknown comment type");
    }
    int startPos = javacComment.getSourcePos(0);
    int endPos = startPos + javacComment.getText().length();
    comment.setSourceRange(startPos, endPos);
    return comment;
  }

  /**
   * Returns the non-annotation modifiers as an or'd int.
   */
  private int convertFlags(Set<Modifier> modifiers) {
    int flags = 0;
    for (Modifier m : modifiers) {
      switch (m) {
        case PUBLIC:
          flags |= java.lang.reflect.Modifier.PUBLIC;
          break;
        case PROTECTED:
          flags |= java.lang.reflect.Modifier.PROTECTED;
          break;
        case PRIVATE:
          flags |= java.lang.reflect.Modifier.PRIVATE;
          break;
        case ABSTRACT:
          flags |= java.lang.reflect.Modifier.ABSTRACT;
          break;
        case STATIC:
          flags |= java.lang.reflect.Modifier.STATIC;
          break;
        case FINAL:
          flags |= java.lang.reflect.Modifier.FINAL;
          break;
        case TRANSIENT:
          flags |= java.lang.reflect.Modifier.TRANSIENT;
          break;
        case VOLATILE:
          flags |= java.lang.reflect.Modifier.VOLATILE;
          break;
        case SYNCHRONIZED:
          flags |= java.lang.reflect.Modifier.SYNCHRONIZED;
          break;
        case NATIVE:
          flags |= java.lang.reflect.Modifier.NATIVE;
          break;
        default:
          // Ignore, no java.lang.reflect equivalent.
      }
    }
    return flags;
  }
}
