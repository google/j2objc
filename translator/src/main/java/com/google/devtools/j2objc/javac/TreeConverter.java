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

import com.google.common.collect.Lists;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.ArrayAccess;
import com.google.devtools.j2objc.ast.ArrayCreation;
import com.google.devtools.j2objc.ast.ArrayInitializer;
import com.google.devtools.j2objc.ast.ArrayType;
import com.google.devtools.j2objc.ast.AssertStatement;
import com.google.devtools.j2objc.ast.Assignment;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BlockComment;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.BooleanLiteral;
import com.google.devtools.j2objc.ast.BreakStatement;
import com.google.devtools.j2objc.ast.CastExpression;
import com.google.devtools.j2objc.ast.CatchClause;
import com.google.devtools.j2objc.ast.CharacterLiteral;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.Comment;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConditionalExpression;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.ContinueStatement;
import com.google.devtools.j2objc.ast.CreationReference;
import com.google.devtools.j2objc.ast.DoStatement;
import com.google.devtools.j2objc.ast.EmptyStatement;
import com.google.devtools.j2objc.ast.EnhancedForStatement;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionMethodReference;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.ForStatement;
import com.google.devtools.j2objc.ast.FunctionalExpression;
import com.google.devtools.j2objc.ast.IfStatement;
import com.google.devtools.j2objc.ast.InfixExpression;
import com.google.devtools.j2objc.ast.Initializer;
import com.google.devtools.j2objc.ast.InstanceofExpression;
import com.google.devtools.j2objc.ast.Javadoc;
import com.google.devtools.j2objc.ast.LabeledStatement;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.LineComment;
import com.google.devtools.j2objc.ast.MarkerAnnotation;
import com.google.devtools.j2objc.ast.MemberValuePair;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.MethodReference;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.NormalAnnotation;
import com.google.devtools.j2objc.ast.NullLiteral;
import com.google.devtools.j2objc.ast.NumberLiteral;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.ParameterizedType;
import com.google.devtools.j2objc.ast.ParenthesizedExpression;
import com.google.devtools.j2objc.ast.PostfixExpression;
import com.google.devtools.j2objc.ast.PrefixExpression;
import com.google.devtools.j2objc.ast.PrimitiveType;
import com.google.devtools.j2objc.ast.PropertyAnnotation;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SimpleType;
import com.google.devtools.j2objc.ast.SingleMemberAnnotation;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.SourcePosition;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperFieldAccess;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.SuperMethodReference;
import com.google.devtools.j2objc.ast.SwitchCase;
import com.google.devtools.j2objc.ast.SwitchStatement;
import com.google.devtools.j2objc.ast.SynchronizedStatement;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.ThrowStatement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TryStatement;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.TypeDeclarationStatement;
import com.google.devtools.j2objc.ast.TypeLiteral;
import com.google.devtools.j2objc.ast.TypeMethodReference;
import com.google.devtools.j2objc.ast.UnionType;
import com.google.devtools.j2objc.ast.VariableDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationExpression;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.ast.VariableDeclarationStatement;
import com.google.devtools.j2objc.ast.WhileStatement;
import com.google.devtools.j2objc.translate.OcniExtractor;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.types.GeneratedPackageElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.TranslationEnvironment;
import com.google.j2objc.annotations.Property;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.DocCommentTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFunctionalExpression;
import com.sun.tools.javac.tree.JCTree.JCMemberReference;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.Tag;
import com.sun.tools.javac.util.Position;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

/** Converts a Java AST from the Javac data structure to our J2ObjC data structure. */
public class TreeConverter {
  private final CompilationUnitTree unit;
  private final JavacEnvironment env;
  private CompilationUnit newUnit;
  private final Trees trees;
  private final SourcePositions sourcePositions;

  public static CompilationUnit convertCompilationUnit(
      Options options, JavacEnvironment env, CompilationUnitTree javacUnit) {
    String sourceFilePath = getPath(javacUnit.getSourceFile());
    try {
      TreeConverter converter = new TreeConverter(javacUnit, env);
      JavaFileObject sourceFile = javacUnit.getSourceFile();
      String source = sourceFile.getCharContent(false).toString();
      String mainTypeName = FileUtil.getMainTypeName(sourceFile);
      TranslationEnvironment translationEnv = new TranslationEnvironment(options, env);
      converter.newUnit = new CompilationUnit(translationEnv, sourceFilePath, mainTypeName, source);
      TreePath path = new TreePath(javacUnit);
      converter.newUnit.setPackage(converter.convertPackage(path));
      for (Tree type : javacUnit.getTypeDecls()) {
        if (type.getKind() == Kind.IMPORT) {
          continue;
        }
        TreeNode newNode = converter.convert(type, path);
        if (newNode.getKind() != TreeNode.Kind.EMPTY_STATEMENT) {
          converter.newUnit.addType((AbstractTypeDeclaration) newNode);
        }
      }
      addOcniComments(converter.newUnit, options.jsniWarnings());

      // Enable this to debug tree conversion issues, otherwise let
      // TranslationProcessor.applyMutations() handle verification.
      // converter.newUnit.validate();

      return converter.newUnit;
    } catch (Throwable e) {
      ErrorUtil.fatalError(e, sourceFilePath);
      return null;
    }
  }

  private TreeConverter(CompilationUnitTree javacUnit, JavacEnvironment javacEnv) {
    unit = javacUnit;
    env = javacEnv;
    trees = javacEnv.treeUtilities();
    sourcePositions = trees.getSourcePositions();
  }

  private TreeNode convert(Tree node, TreePath parent) {
    if (node == null) {
      return null;
    }
    TreeNode newNode = convertInner(node, parent).setPosition(getPosition(node));
    if (newNode instanceof Expression) {
      copyConstantValue(node, (Expression) newNode);
    }
    return newNode;
  }

  private SourcePosition getPosition(Tree node) {
    int startPosition = (int) sourcePositions.getStartPosition(unit, node);
    int endPosition = (int) sourcePositions.getEndPosition(unit, node);
    int length =
        startPosition == Position.NOPOS || endPosition == Position.NOPOS
            ? 0
            : endPosition - startPosition;
    return getSourcePosition(startPosition, length);
  }

  @SuppressWarnings("fallthrough")
  private TreeNode convertInner(Tree javacNode, TreePath parent) {
    switch (javacNode.getKind()) {
      case ANNOTATION:
      case TYPE_ANNOTATION:
        return convertAnnotation((AnnotationTree) javacNode, parent);
      case ANNOTATION_TYPE:
        return convertAnnotationTypeDeclaration((ClassTree) javacNode, parent);
      case ARRAY_ACCESS:
        return convertArrayAccess((ArrayAccessTree) javacNode, parent);
      case ARRAY_TYPE:
        return convertArrayType((ArrayTypeTree) javacNode, parent);
      case ASSERT:
        return convertAssert((AssertTree) javacNode, parent);
      case ASSIGNMENT:
        return convertAssignment((AssignmentTree) javacNode, parent);
      case BLOCK:
        return convertBlock((BlockTree) javacNode, parent);
      case BREAK:
        return convertBreakStatement((BreakTree) javacNode);
      case CASE:
        return convertCase((CaseTree) javacNode, parent);
      case CATCH:
        return convertCatch((CatchTree) javacNode, parent);
      case CLASS:
        return convertClassDeclaration((ClassTree) javacNode, parent);
      case COMPILATION_UNIT:
        throw new AssertionError(
            "CompilationUnit must be converted using convertCompilationUnit()");
      case CONDITIONAL_EXPRESSION:
        return convertConditionalExpression((ConditionalExpressionTree) javacNode, parent);
      case CONTINUE:
        return convertContinueStatement((ContinueTree) javacNode);
      case DO_WHILE_LOOP:
        return convertDoStatement((DoWhileLoopTree) javacNode, parent);
      case EMPTY_STATEMENT:
        return new EmptyStatement();
      case ENHANCED_FOR_LOOP:
        return convertEnhancedForStatement((EnhancedForLoopTree) javacNode, parent);
      case ENUM:
        return convertEnum((ClassTree) javacNode, parent);
      case EXPRESSION_STATEMENT:
        return convertExpressionStatement((ExpressionStatementTree) javacNode, parent);
      case FOR_LOOP:
        return convertForLoop((ForLoopTree) javacNode, parent);
      case IDENTIFIER:
        return convertIdent((IdentifierTree) javacNode, parent);
      case INSTANCE_OF:
        return convertInstanceOf((InstanceOfTree) javacNode, parent);
      case INTERFACE:
        return convertClassDeclaration((ClassTree) javacNode, parent);
      case IF:
        return convertIf((IfTree) javacNode, parent);
      case LABELED_STATEMENT:
        return convertLabeledStatement((LabeledStatementTree) javacNode, parent);
      case LAMBDA_EXPRESSION:
        return convertLambda((LambdaExpressionTree) javacNode, parent);
      case MEMBER_REFERENCE:
        return convertMemberReference((MemberReferenceTree) javacNode, parent);
      case MEMBER_SELECT:
        return convertFieldAccess((MemberSelectTree) javacNode, parent);
      case METHOD:
        return convertMethodDeclaration((MethodTree) javacNode, parent);
      case METHOD_INVOCATION:
        return convertMethodInvocation((MethodInvocationTree) javacNode, parent);
      case NEW_ARRAY:
        return convertNewArray((NewArrayTree) javacNode, parent);
      case NEW_CLASS:
        return convertNewClass((NewClassTree) javacNode, parent);
      case PARAMETERIZED_TYPE:
        return convertTypeApply((ParameterizedTypeTree) javacNode, parent);
      case PARENTHESIZED:
        return convertParens((ParenthesizedTree) javacNode, parent);
      case PRIMITIVE_TYPE:
        return convertPrimitiveType((PrimitiveTypeTree) javacNode, parent);
      case RETURN:
        return convertReturn((ReturnTree) javacNode, parent);
      case SWITCH:
        return convertSwitch((SwitchTree) javacNode, parent);
      case THROW:
        return convertThrow((ThrowTree) javacNode, parent);
      case TRY:
        return convertTry((TryTree) javacNode, parent);
      case TYPE_CAST:
        return convertTypeCast((TypeCastTree) javacNode, parent);
      case VARIABLE:
        return convertVariableDeclaration((VariableTree) javacNode, parent);
      case WHILE_LOOP:
        return convertWhileLoop((WhileLoopTree) javacNode, parent);

      case BOOLEAN_LITERAL:
        return convertBooleanLiteral((LiteralTree) javacNode, parent);
      case CHAR_LITERAL:
        return convertCharLiteral((LiteralTree) javacNode, parent);
      case DOUBLE_LITERAL:
      case FLOAT_LITERAL:
      case INT_LITERAL:
      case LONG_LITERAL:
        return convertNumberLiteral((LiteralTree) javacNode, parent);
      case STRING_LITERAL:
        return convertStringLiteral((LiteralTree) javacNode, parent);
      case SYNCHRONIZED:
        return convertSynchronized((SynchronizedTree) javacNode, parent);
      case NULL_LITERAL:
        return new NullLiteral(getTypeMirror(getTreePath(parent, javacNode)));

      case AND:
      case CONDITIONAL_AND:
      case CONDITIONAL_OR:
      case DIVIDE:
      case EQUAL_TO:
      case GREATER_THAN:
      case GREATER_THAN_EQUAL:
      case LEFT_SHIFT:
      case LESS_THAN:
      case LESS_THAN_EQUAL:
      case MINUS:
      case MULTIPLY:
      case NOT_EQUAL_TO:
      case OR:
      case PLUS:
      case REMAINDER:
      case RIGHT_SHIFT:
      case UNSIGNED_RIGHT_SHIFT:
      case XOR:
        return convertBinary((BinaryTree) javacNode, parent);

      case BITWISE_COMPLEMENT:
      case LOGICAL_COMPLEMENT:
      case PREFIX_DECREMENT:
      case PREFIX_INCREMENT:
      case UNARY_MINUS:
      case UNARY_PLUS:
        return convertPrefixExpr((UnaryTree) javacNode, parent);

      case POSTFIX_DECREMENT:
      case POSTFIX_INCREMENT:
        return convertPostExpr((UnaryTree) javacNode, parent);

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
        return convertAssignOp((CompoundAssignmentTree) javacNode, parent);

      case OTHER:
        {
          if (((JCTree) javacNode).hasTag(Tag.NULLCHK)) {
            // Skip javac's nullchk operators, since j2objc provides its own.
            // TODO(tball): convert to nil_chk() functions in this class, to
            // always check references that javac flagged?
            return convert(((UnaryTree) javacNode).getExpression(), getTreePath(parent, javacNode));
          }
          throw new AssertionError("Unknown OTHER node, tag: " + ((JCTree) javacNode).getTag());
        }

      default:
        throw new AssertionError("Unknown node type: " + javacNode.getKind());
    }
  }

  private TreeNode convertAnnotation(AnnotationTree node, TreePath parent) {
    List<? extends ExpressionTree> args = node.getArguments();
    String annotationName = node.getAnnotationType().toString();
    AnnotationMirror annotationMirror = ((JCAnnotation) node).attribute;
    boolean isPropertyAnnotation =
        annotationName.equals(Property.class.getSimpleName())
            || annotationName.equals(Property.class.getName());
    Annotation newNode;
    TreePath path = getTreePath(parent, node);
    if (isPropertyAnnotation) {
      newNode = new PropertyAnnotation().setAnnotationMirror(annotationMirror);
      if (!args.isEmpty()) {
        for (String attr : ElementUtil.parsePropertyAttribute(annotationMirror)) {
          ((PropertyAnnotation) newNode).addAttribute(attr);
        }
      }
    } else if (args.isEmpty()) {
      newNode = new MarkerAnnotation().setAnnotationMirror(annotationMirror);
    } else if (args.size() == 1) {
      AssignmentTree assign = (AssignmentTree) args.get(0);
      ExpressionTree expression = assign.getExpression();
      newNode =
          new SingleMemberAnnotation()
              .setValue((Expression) convert(expression, getTreePath(path, assign)));
    } else {
      NormalAnnotation normalAnn = new NormalAnnotation();
      for (ExpressionTree obj : node.getArguments()) {
        AssignmentTree assign = (AssignmentTree) obj;
        ExpressionTree variable = assign.getVariable();
        ExpressionTree expression = assign.getExpression();
        TreePath assignPath = getTreePath(path, assign);
        TreePath variablePath = getTreePath(assignPath, variable);
        SimpleName name =
            convertSimpleName(
                getElement(variablePath), getTypeMirror(variablePath), getPosition(variable));
        MemberValuePair memberPair =
            new MemberValuePair()
                .setName(name)
                .setValue((Expression) convert(expression, assignPath));
        normalAnn.addValue(memberPair);
      }
      newNode = normalAnn;
    }
    return newNode
        .setAnnotationMirror(annotationMirror)
        .setTypeName((Name) convert(node.getAnnotationType(), path));
  }

  private List<Annotation> convertAnnotations(ModifiersTree modifiers, TreePath parent) {
    List<Annotation> annotations = new ArrayList<>();
    TreePath path = getTreePath(parent, modifiers);
    for (AnnotationTree annotation : modifiers.getAnnotations()) {
      annotations.add((Annotation) convert(annotation, path));
    }
    return annotations;
  }

  private TreeNode convertAnnotationTypeDeclaration(ClassTree node, TreePath parent) {
    AnnotationTypeDeclaration newNode = new AnnotationTypeDeclaration();
    TreePath path = getTreePath(parent, node);
    Element element = getElement(path);
    convertBodyDeclaration(node, path, node.getModifiers(), newNode);
    for (Tree bodyDecl : node.getMembers()) {
      if (bodyDecl.getKind() == Kind.METHOD) {
        MethodTree methodTree = (MethodTree) bodyDecl;
        TreePath methodPath = getTreePath(path, methodTree);
        ExecutableElement methodElement = (ExecutableElement) getElement(methodPath);
        Tree defaultValue = methodTree.getDefaultValue();
        ModifiersTree modifiers = methodTree.getModifiers();
        AnnotationTypeMemberDeclaration newMember =
            new AnnotationTypeMemberDeclaration()
                .setDefault((Expression) convert(defaultValue, methodPath))
                .setExecutableElement(methodElement);
        newMember
            .setModifiers((int) ((JCModifiers) modifiers).flags)
            .setAnnotations(convertAnnotations(modifiers, getTreePath(methodPath, modifiers)))
            .setJavadoc((Javadoc) getAssociatedJavaDoc(methodTree, methodPath));
        newNode.addBodyDeclaration(newMember);
      } else {
        newNode.addBodyDeclaration((BodyDeclaration) convert(bodyDecl, path));
      }
    }
    return newNode
        .setName(convertSimpleName(element, getTypeMirror(path), getNamePosition(node)))
        .setTypeElement((TypeElement) element);
  }

  private TreeNode convertArrayAccess(ArrayAccessTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    return new ArrayAccess()
        .setArray((Expression) convert(node.getExpression(), path))
        .setIndex((Expression) convert(node.getIndex(), path));
  }

  private TreeNode convertArrayType(ArrayTypeTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    ArrayType newNode = new ArrayType();
    Type componentType = Type.newType(getTypeMirror(getTreePath(path, node.getType())));
    return newNode.setComponentType(componentType).setTypeMirror(getTypeMirror(path));
  }

  private TreeNode convertAssert(AssertTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    return new AssertStatement()
        .setExpression((Expression) convert(node.getCondition(), path))
        .setMessage((Expression) convert(node.getDetail(), path));
  }

  private TreeNode convertAssignment(AssignmentTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    Assignment newNode = new Assignment();
    return newNode
        .setOperator(Assignment.Operator.ASSIGN)
        .setLeftHandSide((Expression) convert(node.getVariable(), path))
        .setRightHandSide((Expression) convert(node.getExpression(), path));
  }

  private TreeNode convertAssignOp(CompoundAssignmentTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    Assignment newNode = new Assignment();
    return newNode
        .setOperator(Assignment.Operator.from(node.getKind()))
        .setLeftHandSide((Expression) convert(node.getVariable(), path))
        .setRightHandSide((Expression) convert(node.getExpression(), path));
  }

  private TreeNode convertBinary(BinaryTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    InfixExpression newNode = new InfixExpression();
    newNode
        .setTypeMirror(getTypeMirror(path))
        .setOperator(InfixExpression.Operator.from(node.getKind()));

    // Flatten this tree to avoid stack overflow with very deep trees. This
    // code traverses the subtree non-recursively and merges all children
    // that have the same operator into this node.
    List<StackState> stack = Lists.newArrayList();
    stack.add(new StackState(node));
    while (!stack.isEmpty()) {
      StackState currentState = stack.get(stack.size() - 1);
      ExpressionTree child = currentState.nextChild();
      if (child == null) {
        stack.remove(stack.size() - 1);
        continue;
      }
      if (child instanceof BinaryTree) {
        BinaryTree infixChild = (BinaryTree) child;
        if (infixChild.getKind() == node.getKind()) {
          stack.add(new StackState(infixChild));
          continue;
        }
      }
      newNode.addOperand((Expression) convert(child, path));
    }
    return newNode;
  }

  private TreeNode convertBlock(BlockTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    Block newNode = new Block();
    for (StatementTree stmt : node.getStatements()) {
      TreeNode tree = convert(stmt, path);
      if (tree instanceof AbstractTypeDeclaration) {
        tree = new TypeDeclarationStatement().setDeclaration((AbstractTypeDeclaration) tree);
      }
      newNode.addStatement((Statement) tree);
    }
    return newNode;
  }

  private TreeNode convertBodyDeclaration(
      Tree node,
      TreePath parent,
      ModifiersTree modifiers,
      BodyDeclaration newNode) {
    TreePath path = getTreePath(parent, node);
    return newNode
        .setModifiers((int) ((JCModifiers) modifiers).flags)
        .setAnnotations(convertAnnotations(modifiers, path))
        .setJavadoc((Javadoc) getAssociatedJavaDoc(node, path));
  }

  private TreeNode convertBooleanLiteral(LiteralTree node, TreePath parent) {
    return new BooleanLiteral((Boolean) node.getValue(), getTypeMirror(getTreePath(parent, node)));
  }

  private TreeNode convertBreakStatement(BreakTree node) {
    BreakStatement newNode = new BreakStatement();
    Object label = node.getLabel();
    if (label != null) {
      newNode.setLabel(
          (SimpleName) new SimpleName(label.toString()).setPosition(getPosition(node)));
    }
    return newNode;
  }

  private TreeNode convertCase(CaseTree node, TreePath parent) {
    // Case statements are converted in convertSwitch().
    SwitchCase newNode = new SwitchCase();
    ExpressionTree expressionTree = node.getExpression();
    if (expressionTree != null) {
      newNode.setExpression((Expression) convert(expressionTree, getTreePath(parent, node)));
    } else {
      newNode.setIsDefault(true);
    }
    return newNode;
  }

  private TreeNode convertCatch(CatchTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    return new CatchClause()
        .setException((SingleVariableDeclaration) convert(node.getParameter(), path))
        .setBody((Block) convert(node.getBlock(), path));
  }

  private TreeNode convertCharLiteral(LiteralTree node, TreePath parent) {
    return new CharacterLiteral(
        (Character) node.getValue(), getTypeMirror(getTreePath(parent, node)));
  }

  private TreeNode convertClassDeclaration(ClassTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    TypeElement element = (TypeElement) getElement(path);
    // javac defines all type declarations with JCClassDecl, so differentiate here
    // to support our different declaration nodes.
    if (element.getKind() == ElementKind.ANNOTATION_TYPE) {
      throw new AssertionError("Annotation type declaration tree conversion not implemented");
    }

    TypeDeclaration newNode = convertClassDeclarationHelper(node, parent);

    newNode.setInterface(
        node.getKind() == Kind.INTERFACE || node.getKind() == Kind.ANNOTATION_TYPE);
    if (ElementUtil.isAnonymous(element)) {
      newUnit.getEnv().elementUtil().mapElementType(element, getTypeMirror(path));
    }
    return newNode;
  }

  private TypeDeclaration convertClassDeclarationHelper(ClassTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    TypeDeclaration newNode = new TypeDeclaration();
    TypeElement element = (TypeElement) getElement(path);
    convertBodyDeclaration(node, parent, node.getModifiers(), newNode);
    List<BodyDeclaration> bodyDeclarations = newNode.getBodyDeclarations();
    for (Tree bodyDecl : node.getMembers()) {
      Object member = convert(bodyDecl, path);
      if (member instanceof BodyDeclaration) { // Not true for enum constants.
        bodyDeclarations.add((BodyDeclaration) member);
      } else if (member instanceof Block) {
        BlockTree javacBlock = (BlockTree) bodyDecl;
        Block block = (Block) member;
        bodyDeclarations.add(new Initializer(block, javacBlock.isStatic()));
      }
    }
    newNode.setName(convertSimpleName(element, getTypeMirror(path), getNamePosition(node)));
    newNode.setTypeElement(element);
    return newNode;
  }

  private TreeNode convertConditionalExpression(ConditionalExpressionTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    return new ConditionalExpression()
        .setTypeMirror(getTypeMirror(path))
        .setExpression((Expression) convert(node.getCondition(), path))
        .setThenExpression((Expression) convert(node.getTrueExpression(), path))
        .setElseExpression((Expression) convert(node.getFalseExpression(), path));
  }

  private TreeNode convertContinueStatement(ContinueTree node) {
    ContinueStatement newNode = new ContinueStatement();
    Object label = node.getLabel();
    if (label != null) {
      newNode.setLabel(
          (SimpleName) new SimpleName(label.toString()).setPosition(getPosition(node)));
    }
    return newNode;
  }

  private TreeNode convertDoStatement(DoWhileLoopTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    return new DoStatement()
        .setExpression(convertWithoutParens(node.getCondition(), path))
        .setBody((Statement) convert(node.getStatement(), path));
  }

  private TreeNode convertEnhancedForStatement(EnhancedForLoopTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    return new EnhancedForStatement()
        .setParameter(
            (SingleVariableDeclaration)
                convertSingleVariable(node.getVariable(), path).setPosition(getPosition(node)))
        .setExpression((Expression) convert(node.getExpression(), path))
        .setBody((Statement) convert(node.getStatement(), path));
  }

  private TreeNode convertEnum(ClassTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    TypeElement element = (TypeElement) getElement(path);
    if (ElementUtil.isAnonymous(element)) {
      return convertClassDeclaration(node, parent).setPosition(getPosition(node));
    }
    EnumDeclaration newNode = new EnumDeclaration();
    convertBodyDeclaration(node, parent, node.getModifiers(), newNode);
    newNode
        .setName(convertSimpleName(element, getTypeMirror(path), getNamePosition(node)))
        .setTypeElement(element);
    for (Tree bodyDecl : node.getMembers()) {
      if (bodyDecl.getKind() == Kind.VARIABLE) {
        TreeNode var = convertVariableDeclaration((VariableTree) bodyDecl, path);
        if (var.getKind() == TreeNode.Kind.ENUM_CONSTANT_DECLARATION) {
          newNode.addEnumConstant((EnumConstantDeclaration) var);
        } else {
          newNode.addBodyDeclaration((BodyDeclaration) var);
        }
      } else if (bodyDecl.getKind() == Kind.BLOCK) {
        BlockTree javacBlock = (BlockTree) bodyDecl;
        Block block = (Block) convert(javacBlock, path);
        newNode.addBodyDeclaration(new Initializer(block, javacBlock.isStatic()));
      } else {
        newNode.addBodyDeclaration((BodyDeclaration) convert(bodyDecl, path));
      }
    }
    return newNode;
  }

  private TreeNode copyConstantValue(Tree node, Expression newNode) {
    Object value = ((JCTree) node).type.constValue();
    if (value instanceof Integer) {
      switch (((JCTree) node).type.baseType().getKind()) {
        case BOOLEAN:
          // Convert boolean values of 1/0 as true/false.
          value = ((Integer) value).intValue() == 1;
          break;
        case CHAR:
          value = (char) ((Integer) value).intValue();
          break;
        default: // value doesn't need to be changed.
      }
    }
    return value != null ? newNode.setConstantValue(value) : newNode;
  }

  private TreeNode convertExpressionStatement(ExpressionStatementTree node, TreePath parent) {
    TreeNode expr = convert(node.getExpression(), getTreePath(parent, node));
    if (expr instanceof Statement) {
      return expr;
    }
    return new ExpressionStatement().setExpression((Expression) expr);
  }

  private TreeNode convertFieldAccess(MemberSelectTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    String fieldName = node.getIdentifier().toString();
    SourcePosition pos = getPosition(node);
    ExpressionTree selected = node.getExpression();
    TreePath selectedPath = getTreePath(path, selected);
    Element element = getElement(path);
    TypeMirror typeMirror = getTypeMirror(path);
    if (fieldName.equals("this")) {
      return new ThisExpression()
          .setQualifier((Name) convert(selected, path))
          .setTypeMirror(typeMirror);
    }
    if ("super".equals(getMemberName(selected))) {
      SuperFieldAccess newNode =
          new SuperFieldAccess()
              .setVariableElement((VariableElement) element)
              .setTypeMirror(typeMirror);
      if (selected.getKind() == Kind.MEMBER_SELECT) {
        newNode.setQualifier(
            (Name) convert(((MemberSelectTree) selected).getExpression(), selectedPath));
      }
      return newNode;
    }
    if (node.getIdentifier().toString().equals("class")) {
      Type type = convertType(getTypeMirror(selectedPath), pos, false);
      type.setPosition(getPosition(node));
      return new TypeLiteral(typeMirror).setType(type);
    }
    if (selected.getKind() == Kind.IDENTIFIER
        && (!element.getKind().isField() || ElementUtil.isConstant((VariableElement) element))) {
      if (selected.toString().equals("this")) {
        // Just return the constant.
        return new SimpleName(element);
      }
      return new QualifiedName()
          .setName(convertSimpleName(element, typeMirror, pos))
          .setQualifier(
              convertSimpleName(getElement(selectedPath), getTypeMirror(selectedPath), pos))
          .setElement(element);
    }
    if (selected.getKind() == Kind.MEMBER_SELECT) {
      TreeNode newSelected = convertFieldAccess((MemberSelectTree) selected, path).setPosition(pos);
      if (newSelected.getKind() == TreeNode.Kind.QUALIFIED_NAME) {
        return new QualifiedName()
            .setName(convertSimpleName(element, typeMirror, pos))
            .setQualifier((QualifiedName) newSelected)
            .setElement(element);
      }
    }
    if (ElementUtil.isConstant((VariableElement) element)
        && ElementUtil.isStatic(element)
        && !(selected.getKind() == Kind.METHOD_INVOCATION)
        && !(selected.getKind() == Kind.MEMBER_SELECT)
        && !(selected.getKind() == Kind.PARENTHESIZED)) {
      return new QualifiedName()
          .setName(convertSimpleName(element, typeMirror, pos))
          .setQualifier((Name) convert(selected, path))
          .setElement(element);
    }
    return new FieldAccess()
        .setVariableElement((VariableElement) element)
        .setExpression((Expression) convert(selected, path))
        .setName(convertSimpleName(element, typeMirror, pos).setTypeMirror(typeMirror));
  }

  private TreeNode convertForLoop(ForLoopTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    ForStatement newNode =
        new ForStatement()
            .setExpression((Expression) convert(node.getCondition(), path))
            .setBody((Statement) convert(node.getStatement(), path));
    VariableDeclarationExpression lastVar = null;
    for (StatementTree initializer : node.getInitializer()) {
      if (initializer.getKind() == Kind.VARIABLE) {
        VariableTree var = (VariableTree) initializer;
        VariableDeclarationExpression newVar = convertVariableExpression(var, path);
        if (lastVar == null) {
          newNode.addInitializer(newVar);
          lastVar = newVar;
        } else {
          lastVar.addFragment(TreeUtil.remove(newVar.getFragment(0)));
        }
      } else {
        assert initializer.getKind() == Kind.EXPRESSION_STATEMENT;
        TreePath initializerPath = getTreePath(path, initializer);
        TreeNode expr =
            convert(((ExpressionStatementTree) initializer).getExpression(), initializerPath);
        newNode.addInitializer((Expression) expr);
      }
    }
    for (ExpressionStatementTree updater : node.getUpdate()) {
      newNode.addUpdater((Expression) convert(updater.getExpression(), getTreePath(path, updater)));
    }
    return newNode;
  }

  private TreeNode convertFunctionalExpression(
      JCFunctionalExpression node, TreePath parent, FunctionalExpression newNode) {
    List<? extends TypeMirror> targets = getTargets(node, parent);
    for (TypeMirror type : targets) {
      newNode.addTargetType(type);
    }
    Types types =
        Types.instance(((com.sun.tools.javac.api.BasicJavacTask) env.task()).getContext());
    return newNode
        .setTypeMirror(targets.iterator().next())
        .setDescriptor(
            new ExecutablePair(
                (ExecutableElement) types
                    .findDescriptorSymbol(((com.sun.tools.javac.code.Type) targets.get(0)).tsym),
                (ExecutableType) node.getDescriptorType(types)));
  }

  private List<? extends TypeMirror> getTargets(JCFunctionalExpression node, TreePath parent) {
    try {
      @SuppressWarnings("unchecked")
      com.sun.tools.javac.util.List<com.sun.tools.javac.code.Type> result =
          (com.sun.tools.javac.util.List<com.sun.tools.javac.code.Type>)
              JCFunctionalExpression.class.getField("targets").get(node);
      return result;
    } catch (ReflectiveOperationException e) {
      // continue below
    }
    // In earlier versions, the TypeMirror just contained the first type of an intersection type.
    // That's why, the field "targets" is used above. This issue is fixed in JDK 11.
    TypeMirror t = getTypeMirror(getTreePath(parent, node));
    return newUnit.getEnv().typeUtil().getUpperBounds(t);
  }

  private TreeNode convertIdent(IdentifierTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    Element element = getElement(path);
    TypeMirror typeMirror = getTypeMirror(path);
    String text = element.toString();
    if (text.equals("this")) {
      return new ThisExpression().setTypeMirror(typeMirror);
    }
    return new SimpleName(element, typeMirror);
  }

  private TreeNode convertIf(IfTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    return new IfStatement()
        .setExpression(convertWithoutParens(node.getCondition(), path))
        .setThenStatement((Statement) convert(node.getThenStatement(), path))
        .setElseStatement((Statement) convert(node.getElseStatement(), path));
  }

  private TreeNode convertInstanceOf(InstanceOfTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    TypeMirror clazz = getTypeMirror(getTreePath(path, node.getType()));
    return new InstanceofExpression()
        .setLeftOperand((Expression) convert(node.getExpression(), path))
        .setRightOperand(Type.newType(clazz))
        .setTypeMirror(getTypeMirror(path));
  }

  private TreeNode convertLabeledStatement(LabeledStatementTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    return new LabeledStatement()
        .setLabel(
            (SimpleName) new SimpleName(node.getLabel().toString()).setPosition(getPosition(node)))
        .setBody((Statement) convert(node.getStatement(), path));
  }

  private TreeNode convertLambda(LambdaExpressionTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    LambdaExpression newNode = new LambdaExpression();
    convertFunctionalExpression((JCFunctionalExpression) node, parent, newNode);
    for (VariableTree param : node.getParameters()) {
      newNode.addParameter((VariableDeclaration) convert(param, path));
    }
    return newNode.setBody(convert(node.getBody(), path));
  }

  private TreeNode convertMethodReference(
      MemberReferenceTree node, TreePath parent, MethodReference newNode) {
    TreePath path = getTreePath(parent, node);
    convertFunctionalExpression((JCMemberReference) node, parent, newNode);
    if (node.getTypeArguments() != null) {
      for (ExpressionTree typeArg : node.getTypeArguments()) {
        newNode.addTypeArgument(Type.newType(((JCExpression) typeArg).type));
      }
    }
    return newNode
        .setExecutableElement((ExecutableElement) getElement(path))
        .setVarargsType(((JCMemberReference) node).varargsElement);
  }

  private TreeNode convertMemberReference(MemberReferenceTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    Element element = getElement(path);
    SourcePosition pos = getPosition(node);
    if (ElementUtil.isConstructor(element)) {
      CreationReference newNode = new CreationReference();
      convertMethodReference(node, parent, newNode);
      TypeMirror qualifierType = getTypeMirror(getTreePath(path, node.getQualifierExpression()));
      return newNode.setType(Type.newType(qualifierType));
    }
    if (((JCMemberReference) node).hasKind(JCMemberReference.ReferenceKind.SUPER)) {
      SuperMethodReference newNode = new SuperMethodReference();
      convertMethodReference(node, parent, newNode);
      if (node.getQualifierExpression().getKind() == Kind.IDENTIFIER) {
        // super::foo
        return newNode;
      } else {
        // Qualifier expression is <name>."super", so it's always a JCFieldAccess.
        MemberSelectTree expr = (MemberSelectTree) node.getQualifierExpression();
        TreePath exprPath = getTreePath(path, expr);
        return newNode.setQualifier(
            convertSimpleName(
                getElement(getTreePath(exprPath, expr.getExpression())),
                getTypeMirror(exprPath),
                getPosition(expr.getExpression())));
      }
    }
    if (((JCMemberReference) node).hasKind(JCMemberReference.ReferenceKind.UNBOUND)
        || ((JCMemberReference) node).hasKind(JCMemberReference.ReferenceKind.STATIC)) {
      TypeMethodReference newNode = new TypeMethodReference();
      convertMethodReference(node, parent, newNode);
      return newNode.setType(convertType(getTypeMirror(path), pos, false));
    }

    ExpressionMethodReference newNode = new ExpressionMethodReference();
    convertMethodReference(node, parent, newNode);
    return newNode.setExpression((Expression) convert(node.getQualifierExpression(), path));
  }

  private TreeNode convertMethodDeclaration(MethodTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    ExecutableElement element = (ExecutableElement) getElement(path);
    MethodDeclaration newNode = new MethodDeclaration();

    // JCMethodDecl's preferred diagnostic position is the beginning of the method name.
    int methodStartPosition = ((JCMethodDecl) node).pos().getPreferredPosition();

    int length =
        ElementUtil.isConstructor(element)
            ? element.toString().indexOf('(')
            : node.getName().length();

    Name name = Name.newName(null /* qualifier */, element);
    name.setPosition(new SourcePosition(methodStartPosition, length));

    convertBodyDeclaration(node, parent, node.getModifiers(), newNode);
    for (VariableTree param : node.getParameters()) {
      newNode.addParameter((SingleVariableDeclaration) convert(param, path));
    }
    return newNode
        .setIsConstructor(ElementUtil.isConstructor(element))
        .setExecutableElement(element)
        .setBody((Block) convert(node.getBody(), path))
        .setName(name);
  }

  private static String getMemberName(ExpressionTree node) {
    switch (node.getKind()) {
      case IDENTIFIER:
        return node.toString();
      case MEMBER_SELECT:
        return ((MemberSelectTree) node).getIdentifier().toString();
      default:
        return null;
    }
  }

  private TreeNode convertMethodInvocation(MethodInvocationTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    ExpressionTree method = node.getMethodSelect();
    TreePath methodPath = getTreePath(path, method);
    String methodName = getMemberName(method);
    ExecutableType type = (ExecutableType) getTypeMirror(methodPath);
    ExecutableElement element = (ExecutableElement) getElement(methodPath);
    ExpressionTree target =
        method.getKind() == Kind.MEMBER_SELECT ? ((MemberSelectTree) method).getExpression() : null;

    if ("this".equals(methodName)) {
      ConstructorInvocation newNode =
          new ConstructorInvocation()
              .setExecutablePair(new ExecutablePair(element))
              .setVarargsType(((JCMethodInvocation) node).varargsElement);
      for (ExpressionTree arg : node.getArguments()) {
        newNode.addArgument((Expression) convert(arg, path));
      }
      return newNode;
    }

    if ("super".equals(methodName)) {
      SuperConstructorInvocation newNode =
          new SuperConstructorInvocation()
              .setExecutablePair(new ExecutablePair(element))
              .setVarargsType(((JCMethodInvocation) node).varargsElement);
      if (target != null) {
        newNode.setExpression((Expression) convert(target, methodPath));
      }
      for (ExpressionTree arg : node.getArguments()) {
        newNode.addArgument((Expression) convert(arg, path));
      }
      return newNode;
    }

    if (target != null && "super".equals(getMemberName(target))) {
      SuperMethodInvocation newNode =
          new SuperMethodInvocation()
              .setExecutablePair(new ExecutablePair(element, type))
              .setVarargsType(((JCMethodInvocation) node).varargsElement);
      if (target.getKind() == Kind.MEMBER_SELECT) {
        // foo.bar.MyClass.super.print(...):
        //   target: foo.bar.MyClass.super
        //   target.selected: foo.bar.MyClass
        TreePath targetPath = getTreePath(getTreePath(path, method), target);
        newNode.setQualifier(
            (Name) convert(((MemberSelectTree) target).getExpression(), targetPath));
      }
      for (ExpressionTree arg : node.getArguments()) {
        newNode.addArgument((Expression) convert(arg, path));
      }
      return newNode;
    }

    MethodInvocation newNode = new MethodInvocation();
    if (target != null) {
      newNode.setExpression((Expression) convert(target, methodPath));
    }
    for (ExpressionTree arg : node.getArguments()) {
      newNode.addArgument((Expression) convert(arg, path));
    }
    return newNode
        .setTypeMirror(getTypeMirror(path))
        .setExecutablePair(new ExecutablePair(element, type))
        .setVarargsType(((JCMethodInvocation) node).varargsElement);
  }

  private SimpleName convertSimpleName(Element element, TypeMirror type, SourcePosition pos) {
    return (SimpleName) new SimpleName(element, type).setPosition(pos);
  }

  private TreeNode convertNewArray(NewArrayTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    ArrayCreation newNode = new ArrayCreation();
    List<Expression> dimensions = new ArrayList<>();
    for (ExpressionTree dimension : node.getDimensions()) {
      dimensions.add((Expression) convert(dimension, path));
    }
    javax.lang.model.type.ArrayType type = (javax.lang.model.type.ArrayType) getTypeMirror(path);
    if (node.getInitializers() != null) {
      ArrayInitializer initializers = new ArrayInitializer(type);
      for (ExpressionTree initializer : node.getInitializers()) {
        initializers.addExpression((Expression) convert(initializer, path));
      }
      newNode.setInitializer(initializers);
    }
    return newNode
        .setType((ArrayType) new ArrayType(type).setPosition(getPosition(node)))
        .setDimensions(dimensions);
  }

  private TreeNode convertNewClass(NewClassTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    ClassInstanceCreation newNode = new ClassInstanceCreation();
    Expression enclosingExpression = (Expression) convert(node.getEnclosingExpression(), path);
    ExecutableElement executable = (ExecutableElement) getElement(path);
    TypeMirror vargarsType = ((JCNewClass) node).varargsElement;
    // Case where the first parameter of the constructor of an inner class is the outer class (e.g.
    // new Outer().new Inner(...). Move the enclosing expression (e.g. new Outer()) as the first
    // argument. A varargs parameter could unintentionally trigger this condition because it could
    // map to zero arguments.
    if (executable.getParameters().size() - node.getArguments().size() == 1
        && vargarsType == null) {
      newNode.addArgument(enclosingExpression);
      enclosingExpression = null;
    }
    for (ExpressionTree arg : node.getArguments()) {
      newNode.addArgument((Expression) convert(arg, path));
    }
    return newNode
        .setExecutablePair(new ExecutablePair(executable))
        .setVarargsType(vargarsType)
        .setExpression(enclosingExpression)
        .setType(convertType(getTypeMirror(getTreePath(path, node.getIdentifier()))))
        .setAnonymousClassDeclaration((TypeDeclaration) convert(node.getClassBody(), path));
  }

  private TreeNode convertNumberLiteral(LiteralTree node, TreePath parent) {
    return new NumberLiteral((Number) node.getValue(), getTypeMirror(getTreePath(parent, node)))
        .setToken(getTreeSource(node));
  }

  private PackageDeclaration convertPackage(TreePath parent) {
    ExpressionTree pkgName = unit.getPackageName();
    PackageElement pkg =
        pkgName != null
            ? env.elementUtilities().getPackageElement(pkgName.toString())
            : env.defaultPackage();
    PackageDeclaration newNode = null;
    if (pkg == null) {
      // Synthetic package, create from name.
      pkg = new GeneratedPackageElement(pkgName != null ? pkgName.toString() : "");
      newNode = new PackageDeclaration().setPackageElement(pkg);
      newNode.setName(new SimpleName(pkg, null));
    } else {
      Tree node = trees.getTree(pkg);
      newNode = new PackageDeclaration().setPackageElement(pkg);
      for (AnnotationTree pkgAnnotation : unit.getPackageAnnotations()) {
        newNode.addAnnotation((Annotation) convert(pkgAnnotation, parent));
      }
      if (unit.getSourceFile().toUri().getPath().endsWith("package-info.java")) {
        if (node == null) {
          // Java 8 javac bug, fixed in Java 9. Doc-comments in package-info.java
          // sources are keyed to their compilation unit, not their package node.
          node = unit;
        }
        newNode.setJavadoc((Javadoc) getAssociatedJavaDoc(node, getTreePath(parent, node)));
      }
      newNode.setName(newUnit.getEnv().elementUtil().getPackageName(pkg));
    }
    newNode.setPosition(SourcePosition.NO_POSITION);
    return newNode;
  }

  private TreeNode convertPrefixExpr(UnaryTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    return new PrefixExpression()
        .setTypeMirror(getTypeMirror(path))
        .setOperator(PrefixExpression.Operator.from(node.getKind()))
        .setOperand((Expression) convert(node.getExpression(), path));
  }

  private TreeNode convertParens(ParenthesizedTree node, TreePath parent) {
    return new ParenthesizedExpression()
        .setExpression((Expression) convert(node.getExpression(), getTreePath(parent, node)));
  }

  private TreeNode convertPostExpr(UnaryTree node, TreePath parent) {
    return new PostfixExpression()
        .setOperator(PostfixExpression.Operator.from(node.getKind()))
        .setOperand((Expression) convert(node.getExpression(), getTreePath(parent, node)));
  }

  private TreeNode convertPrimitiveType(PrimitiveTypeTree node, TreePath parent) {
    return new PrimitiveType(getTypeMirror(getTreePath(parent, node)));
  }

  private TreeNode convertReturn(ReturnTree node, TreePath parent) {
    return new ReturnStatement(
        (Expression) convert(node.getExpression(), getTreePath(parent, node)));
  }

  private TreeNode convertStringLiteral(LiteralTree node, TreePath parent) {
    return new StringLiteral((String) node.getValue(), getTypeMirror(getTreePath(parent, node)));
  }

  private TreeNode convertSwitch(SwitchTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    SwitchStatement newNode =
        new SwitchStatement().setExpression(convertWithoutParens(node.getExpression(), path));
    for (CaseTree switchCase : node.getCases()) {
      newNode.addStatement((SwitchCase) convert(switchCase, path));
      TreePath switchCasePath = getTreePath(path, switchCase);
      for (StatementTree s : switchCase.getStatements()) {
        newNode.addStatement((Statement) convert(s, switchCasePath));
      }
    }
    return newNode;
  }

  private TreeNode convertSynchronized(SynchronizedTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    Expression expr = convertWithoutParens(node.getExpression(), path);
    expr.setPosition(getPosition(node));
    return new SynchronizedStatement()
        .setExpression(expr)
        .setBody((Block) convert(node.getBlock(), path));
  }

  private TreeNode convertThrow(ThrowTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    return new ThrowStatement().setExpression((Expression) convert(node.getExpression(), path));
  }

  private TreeNode convertTry(TryTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    TryStatement newNode = new TryStatement();
    for (Tree obj : node.getResources()) {
      if (obj.getKind() == Kind.VARIABLE) {
        newNode.addResource(convertVariableExpression((VariableTree) obj, path));
      } else {
        newNode.addResource(convertInner(obj, path));
      }
    }
    for (CatchTree obj : node.getCatches()) {
      newNode.addCatchClause((CatchClause) convert(obj, path));
    }
    return newNode
        .setBody((Block) convert(node.getBlock(), path))
        .setFinally((Block) convert(node.getFinallyBlock(), path));
  }

  private Type convertType(TypeMirror typeMirror) {
    com.sun.tools.javac.code.Type type = (com.sun.tools.javac.code.Type) typeMirror;
    if (type.getKind() == TypeKind.EXECUTABLE) {
      Type returnType = Type.newType(type.getReturnType());
      if (type.hasTag(TypeTag.FORALL)) {
        return new ParameterizedType().setType(returnType).setTypeMirror(type.getReturnType());
      } else {
        return returnType;
      }
    }
    if (type.getKind() == TypeKind.DECLARED) {
      List<? extends TypeMirror> typeArgs = ((DeclaredType) type).getTypeArguments();
      if (!typeArgs.isEmpty()) {
        return new ParameterizedType().setType(Type.newType(typeMirror)).setTypeMirror(typeMirror);
      }
    }
    return Type.newType(type);
  }

  private Type convertType(TypeMirror varType, SourcePosition pos, boolean isVarargs) {
    Type newType;
    if (isVarargs) {
      newType = Type.newType(((javax.lang.model.type.ArrayType) varType).getComponentType());
    } else {
      if (varType.getKind() == TypeKind.DECLARED
          && !((DeclaredType) varType).getTypeArguments().isEmpty()) {
        newType =
            new ParameterizedType()
                .setType((SimpleType) new SimpleType(varType).setPosition(pos))
                .setTypeMirror(varType);
      } else if (varType.getKind() == TypeKind.UNION) {
        newType = new UnionType();
        newType.setTypeMirror(varType);
        for (TypeMirror t : ((javax.lang.model.type.UnionType) varType).getAlternatives()) {
          Type alternative = convertType(t, pos, false);
          alternative.setPosition(pos);
          ((UnionType) newType).addType(alternative);
        }
      } else {
        newType = Type.newType(varType);
      }
    }
    return (Type) newType.setPosition(pos);
  }

  private TreeNode convertTypeApply(ParameterizedTypeTree node, TreePath parent) {
    TypeMirror typeMirror = getTypeMirror(getTreePath(parent, node));
    return new ParameterizedType().setType(Type.newType(typeMirror)).setTypeMirror(typeMirror);
  }

  private TreeNode convertTypeCast(TypeCastTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    return new CastExpression(
        getTypeMirror(path), (Expression) convert(node.getExpression(), path));
  }

  private TreeNode convertVariableDeclaration(VariableTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    VariableElement element = (VariableElement) getElement(path);
    if (element.getKind() == ElementKind.FIELD) {
      FieldDeclaration newNode =
          new FieldDeclaration(element, (Expression) convert(node.getInitializer(), path));
      convertBodyDeclaration(node, parent, node.getModifiers(), newNode);
      return newNode;
    }
    if (element.getKind() == ElementKind.LOCAL_VARIABLE) {
      return new VariableDeclarationStatement(
          element, (Expression) convert(node.getInitializer(), path));
    }
    if (element.getKind() == ElementKind.ENUM_CONSTANT) {
      EnumConstantDeclaration newNode = new EnumConstantDeclaration().setVariableElement(element);
      convertBodyDeclaration(node, parent, node.getModifiers(), newNode);
      ClassInstanceCreation init = (ClassInstanceCreation) convert(node.getInitializer(), path);
      TreeUtil.moveList(init.getArguments(), newNode.getArguments());
      if (init.getAnonymousClassDeclaration() != null) {
        newNode.setAnonymousClassDeclaration(TreeUtil.remove(init.getAnonymousClassDeclaration()));
      }
      return newNode
          .setExecutablePair(init.getExecutablePair())
          .setVarargsType(init.getVarargsType());
    }
    return convertSingleVariable(node, parent);
  }

  private TreeNode convertSingleVariable(VariableTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    VariableElement element = (VariableElement) getElement(path);
    SourcePosition pos = getPosition(node);
    boolean isVarargs = (((VarSymbol) element).flags() & Flags.VARARGS) > 0;
    Type newType = convertType(getTypeMirror(path), pos, isVarargs);
    return new SingleVariableDeclaration()
        .setType(newType)
        .setIsVarargs(isVarargs)
        .setAnnotations(convertAnnotations(node.getModifiers(), path))
        .setVariableElement(element)
        .setInitializer((Expression) convert(node.getInitializer(), path));
  }

  private VariableDeclarationExpression convertVariableExpression(
      VariableTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    VariableElement element = (VariableElement) getElement(path);
    boolean isVarargs = (((JCVariableDecl) node).sym.flags() & Flags.VARARGS) > 0;
    Type newType = convertType(getTypeMirror(path), getPosition(node), isVarargs);
    VariableDeclarationFragment fragment = new VariableDeclarationFragment();
    fragment
        .setVariableElement(element)
        .setInitializer((Expression) convert(node.getInitializer(), path));
    return new VariableDeclarationExpression().setType(newType).addFragment(fragment);
  }

  private TreeNode convertWhileLoop(WhileLoopTree node, TreePath parent) {
    TreePath path = getTreePath(parent, node);
    return new WhileStatement()
        .setExpression(convertWithoutParens(node.getCondition(), path))
        .setBody((Statement) convert(node.getStatement(), path));
  }

  private TreeNode getAssociatedJavaDoc(Tree node, TreePath path) {
    Comment comment = convertAssociatedComment(node, path);
    return comment != null && comment.isDocComment() ? comment : null;
  }

  private Comment convertAssociatedComment(Tree node, TreePath path) {
    boolean docCommentsEnabled = newUnit.getEnv().options().docCommentsEnabled();
    DocCommentTable docComments = ((JCCompilationUnit) unit).docComments;
    if (!docCommentsEnabled || docComments == null || !docComments.hasComment((JCTree) node)) {
      return null;
    }
    com.sun.tools.javac.parser.Tokens.Comment javacComment = docComments.getComment((JCTree) node);
    Comment comment;
    switch (javacComment.getStyle()) {
      case BLOCK:
        comment = new BlockComment();
        break;
      case JAVADOC:
        comment = convertJavadocComment(path);
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
    comment.setLineNumber((int) unit.getLineMap().getLineNumber(startPos));
    return comment;
  }

  private Javadoc convertJavadocComment(TreePath path) {
    return JavadocConverter.convertJavadoc(
        path, newUnit.getSource(), env, newUnit.getEnv().options().reportJavadocWarnings());
  }

  private static void addOcniComments(CompilationUnit unit, boolean jsniWarnings) {
    for (OcniExtractor.OcniType kind : OcniExtractor.OcniType.values()) {
      addNativeComments(unit, kind.delimiter(), "]-*/");
    }
    if (jsniWarnings) {
      addNativeComments(unit, "/*-{", "}-*/");
    }
  }

  private static void addNativeComments(CompilationUnit unit, String delim, String endDelim) {
    // Can't use a regex because it will greedily include everything between
    // the first and last closing pattern, resulting in a single comment node.
    String source = unit.getSource();
    int startPos = 0;
    int endPos = 0;
    while ((startPos = source.indexOf(delim, endPos)) > -1) {
      endPos = source.indexOf(endDelim, startPos);
      if (endPos > startPos) {
        endPos += 4; // Include closing delimiter.
        BlockComment ocniComment = new BlockComment();
        ocniComment.setSourceRange(startPos, endPos - startPos);
        unit.getCommentList().add(ocniComment);
      } else {
        int lineNum = unit.getLineNumber(startPos);
        ErrorUtil.error("Error finding OCNI closing delimiter for OCNI comment at line " + lineNum);
        break;
      }
    }
  }

  private static String getPath(JavaFileObject file) {
    String uri = file.toUri().toString();
    if (uri.startsWith("mem:/")) {
      // MemoryFileObject needs a custom file system for URI to return the
      // correct path, so the URI string is split instead.
      return uri.substring(5);
    }
    return file.toUri().getPath();
  }

  private String getTreeSource(Tree node) {
    try {
      CharSequence source = unit.getSourceFile().getCharContent(true);
      return source
          .subSequence(
              (int) sourcePositions.getStartPosition(unit, node),
              (int) sourcePositions.getEndPosition(unit, node))
          .toString();
    } catch (IOException e) {
      return node.toString();
    }
  }

  // javac uses a ParenthesizedExpression for the if, do, and while statements, while JDT doesn't.
  private Expression convertWithoutParens(ExpressionTree condition, TreePath parent) {
    Expression result = (Expression) convert(condition, parent);
    if (result.getKind() == TreeNode.Kind.PARENTHESIZED_EXPRESSION) {
      result = TreeUtil.remove(((ParenthesizedExpression) result).getExpression());
    }
    return result;
  }

  private SourcePosition getSourcePosition(int start, int end) {
    if (unit.getLineMap() != null) {
      int line = (int) unit.getLineMap().getLineNumber(start);
      return new SourcePosition(start, end, line);
    } else {
      return new SourcePosition(start, end);
    }
  }

  // Return best guess for the position of a declaration node's name.
  private SourcePosition getNamePosition(Tree node) {
    int start = (int) sourcePositions.getStartPosition(unit, node);
    if (start == -1) {
      return SourcePosition.NO_POSITION;
    }
    String src = newUnit.getSource();
    Kind kind = node.getKind();
    if (kind == Kind.ANNOTATION_TYPE
        || kind == Kind.CLASS
        || kind == Kind.ENUM
        || kind == Kind.INTERFACE) {
      // Skip the class/enum/interface token.
      while (src.charAt(start++) != ' ') {}
    } else if (kind != Kind.METHOD && kind != Kind.VARIABLE) {
      return getPosition(node);
    }
    if (!Character.isJavaIdentifierStart(src.charAt(start))) {
      return getPosition(node);
    }
    int endPos = start + 1;
    while (Character.isJavaIdentifierPart(src.charAt(endPos))) {
      endPos++;
    }
    return getSourcePosition(start, endPos);
  }

  private Element getElement(TreePath path) {
    return trees.getElement(path);
  }

  private TypeMirror getTypeMirror(TreePath path) {
    return trees.getTypeMirror(path);
  }

  private static TreePath getTreePath(TreePath path, Tree tree) {
    return new TreePath(path, tree);
  }

  // Helper class for convertBinary().
  private static class StackState {
    private final BinaryTree expression;
    private int nextChild = -2;

    private StackState(BinaryTree expr) {
      expression = expr;
    }

    private ExpressionTree nextChild() {
      int childIdx = nextChild++;
      if (childIdx == -2) {
        return expression.getLeftOperand();
      } else if (childIdx == -1) {
        return expression.getRightOperand();
      } else {
        return null;
      }
    }
  }
}
