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
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.TranslationEnvironment;
import com.google.j2objc.annotations.Property;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.DocCommentTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.Tag;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Position;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

/**
 * Converts a Java AST from the JDT data structure to our J2ObjC data structure.
 */
public class TreeConverter {
  private final JCTree.JCCompilationUnit unit;
  private final JavacEnvironment env;
  private final Types types;
  private CompilationUnit newUnit;

  public static CompilationUnit convertCompilationUnit(
      Options options, JavacEnvironment env, JCTree.JCCompilationUnit javacUnit) {
    String sourceFilePath = getPath(javacUnit.getSourceFile());
    try {
      TreeConverter converter = new TreeConverter(javacUnit, env);
      JavaFileObject sourceFile = javacUnit.getSourceFile();
      String source = sourceFile.getCharContent(false).toString();
      String mainTypeName = FileUtil.getMainTypeName(sourceFile);
      converter.newUnit = new CompilationUnit(new TranslationEnvironment(options, env),
          sourceFilePath, mainTypeName, source);
      PackageElement pkg = javacUnit.packge != null ? javacUnit.packge : env.defaultPackage();
      converter.newUnit.setPackage(
          converter.convertPackage(pkg, Trees.instance(env.task())));
      for (JCTree type : javacUnit.getTypeDecls()) {
        TreeNode newNode = converter.convert(type);
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

  private TreeConverter(JCTree.JCCompilationUnit javacUnit, JavacEnvironment javacEnv) {
    unit = javacUnit;
    env = javacEnv;
    types = Types.instance(javacEnv.getContext());
  }

  private TreeNode convert(Object obj) {
    if (obj == null) {
      return null;
    }
    JCTree node = (JCTree) obj;
    TreeNode newNode = convertInner(node)
        .setPosition(getPosition(node));
    if (newNode instanceof Expression) {
      copyConstantValue(node, (Expression) newNode);
    }
    return newNode;
  }

  private SourcePosition getPosition(JCTree node) {
    int startPosition = TreeInfo.getStartPos(node);
    int endPosition = TreeInfo.getEndPos(node, unit.endPositions);
    int length = startPosition == Position.NOPOS || endPosition == Position.NOPOS
        ? 0 : endPosition - startPosition;
    return getSourcePosition(startPosition, length);
  }

  @SuppressWarnings("fallthrough")
  private TreeNode convertInner(JCTree javacNode) {
    switch (javacNode.getKind()) {
      case ANNOTATION:
      case TYPE_ANNOTATION:
        return convertAnnotation((JCTree.JCAnnotation) javacNode);
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
        return convertClassDeclaration((JCTree.JCClassDecl) javacNode);
      case COMPILATION_UNIT:
        throw new AssertionError(
            "CompilationUnit must be converted using convertCompilationUnit()");
      case CONDITIONAL_EXPRESSION:
        return convertConditionalExpression((JCTree.JCConditional) javacNode);
      case CONTINUE:
        return convertContinueStatement((JCTree.JCContinue) javacNode);
      case DO_WHILE_LOOP:
        return convertDoStatement((JCTree.JCDoWhileLoop) javacNode);
      case EMPTY_STATEMENT:
        return new EmptyStatement();
      case ENHANCED_FOR_LOOP:
        return convertEnhancedForStatement((JCTree.JCEnhancedForLoop) javacNode);
      case ENUM:
        return convertEnum((JCTree.JCClassDecl) javacNode);
      case EXPRESSION_STATEMENT:
        return convertExpressionStatement((JCTree.JCExpressionStatement) javacNode);
      case FOR_LOOP:
        return convertForLoop((JCTree.JCForLoop) javacNode);
      case IDENTIFIER:
        return convertIdent((JCTree.JCIdent) javacNode);
      case INSTANCE_OF:
        return convertInstanceOf((JCTree.JCInstanceOf) javacNode);
      case INTERFACE:
        return convertClassDeclaration((JCTree.JCClassDecl) javacNode);
      case IF:
        return convertIf((JCTree.JCIf) javacNode);
      case LABELED_STATEMENT:
        return convertLabeledStatement((JCTree.JCLabeledStatement) javacNode);
      case LAMBDA_EXPRESSION:
        return convertLambda((JCTree.JCLambda) javacNode);
      case MEMBER_REFERENCE:
        return convertMemberReference((JCTree.JCMemberReference) javacNode);
      case MEMBER_SELECT:
        return convertFieldAccess((JCTree.JCFieldAccess) javacNode);
      case METHOD:
        return convertMethodDeclaration((JCTree.JCMethodDecl) javacNode);
      case METHOD_INVOCATION:
        return convertMethodInvocation((JCTree.JCMethodInvocation) javacNode);
      case NEW_ARRAY:
        return convertNewArray((JCTree.JCNewArray) javacNode);
      case NEW_CLASS:
        return convertNewClass((JCTree.JCNewClass) javacNode);
      case PARAMETERIZED_TYPE:
        return convertTypeApply((JCTree.JCTypeApply) javacNode);
      case PARENTHESIZED:
        return convertParens((JCTree.JCParens) javacNode);
      case PRIMITIVE_TYPE:
        return convertPrimitiveType((JCTree.JCPrimitiveTypeTree) javacNode);
      case RETURN:
        return convertReturn((JCTree.JCReturn) javacNode);
      case SWITCH:
        return convertSwitch((JCTree.JCSwitch) javacNode);
      case THROW:
        return convertThrow((JCTree.JCThrow) javacNode);
      case TRY:
        return convertTry((JCTree.JCTry) javacNode);
      case TYPE_CAST:
        return convertTypeCast((JCTree.JCTypeCast) javacNode);
      case VARIABLE:
        return convertVariableDeclaration((JCTree.JCVariableDecl) javacNode);
      case WHILE_LOOP:
        return convertWhileLoop((JCTree.JCWhileLoop) javacNode);

      case BOOLEAN_LITERAL:
        return convertBooleanLiteral((JCTree.JCLiteral) javacNode);
      case CHAR_LITERAL:
        return convertCharLiteral((JCTree.JCLiteral) javacNode);
      case DOUBLE_LITERAL:
      case FLOAT_LITERAL:
      case INT_LITERAL:
      case LONG_LITERAL:
        return convertNumberLiteral((JCTree.JCLiteral) javacNode);
      case STRING_LITERAL:
        return convertStringLiteral((JCTree.JCLiteral) javacNode);
      case SYNCHRONIZED:
        return convertSynchronized((JCTree.JCSynchronized) javacNode);
      case NULL_LITERAL:
        return new NullLiteral(((JCTree.JCLiteral) javacNode).type);

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
        return convertBinary((JCTree.JCBinary) javacNode);

      case BITWISE_COMPLEMENT:
      case LOGICAL_COMPLEMENT:
      case PREFIX_DECREMENT:
      case PREFIX_INCREMENT:
      case UNARY_MINUS:
      case UNARY_PLUS:
        return convertPrefixExpr((JCTree.JCUnary) javacNode);

      case POSTFIX_DECREMENT:
      case POSTFIX_INCREMENT:
        return convertPostExpr((JCTree.JCUnary) javacNode);

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

      case OTHER: {
        if (javacNode.hasTag(Tag.NULLCHK)) {
          // Skip javac's nullchk operators, since j2objc provides its own.
          // TODO(tball): convert to nil_chk() functions in this class, to
          // always check references that javac flagged?
          return convert(((JCTree.JCUnary) javacNode).arg);
        }
        throw new AssertionError("Unknown OTHER node, tag: " + javacNode.getTag());
      }

      default:
        throw new AssertionError("Unknown node type: " + javacNode.getKind());
    }
  }

  private TreeNode convertAbstractTypeDeclaration(
      JCTree.JCClassDecl node, AbstractTypeDeclaration newNode) {
    convertBodyDeclaration(node, node.getModifiers(), newNode, node.sym);
    List<BodyDeclaration> bodyDeclarations = newNode.getBodyDeclarations();
    for (JCTree bodyDecl : node.getMembers()) {
      Object member = convert(bodyDecl);
      if (member instanceof BodyDeclaration) {  // Not true for enum constants.
        bodyDeclarations.add((BodyDeclaration) member);
      } else if (member instanceof Block) {
        JCTree.JCBlock javacBlock = (JCTree.JCBlock) bodyDecl;
        Block block = (Block) member;
        bodyDeclarations.add(new Initializer(block, javacBlock.isStatic()));
      }
    }
    return newNode
        .setName(convertSimpleName(node.sym, node.sym.asType(), getNamePosition(node)))
        .setTypeElement(node.sym);
  }

  private TreeNode convertAnnotation(JCTree.JCAnnotation node) {
    List<JCTree.JCExpression> args = node.getArguments();
    String annotationName = node.getAnnotationType().toString();
    boolean isPropertyAnnotation = annotationName.equals(Property.class.getSimpleName())
        || annotationName.equals(Property.class.getName());
    Annotation newNode;
    if (isPropertyAnnotation) {
      newNode = new PropertyAnnotation()
          .setAnnotationMirror(node.attribute);
      if (!args.isEmpty()) {
        for (String attr : ElementUtil.parsePropertyAttribute(node.attribute)) {
          ((PropertyAnnotation) newNode).addAttribute(attr);
        }
      }
    } else if (args.isEmpty()) {
      newNode = new MarkerAnnotation()
          .setAnnotationMirror(node.attribute);
    } else if (args.size() == 1) {
      JCTree.JCAssign assign = (JCTree.JCAssign) args.get(0);
      newNode = new SingleMemberAnnotation()
          .setValue((Expression) convert(assign.rhs));
    } else {
      NormalAnnotation normalAnn = new NormalAnnotation();
      for (JCTree.JCExpression obj : node.getArguments()) {
        JCTree.JCAssign assign = (JCTree.JCAssign) obj;
        Symbol sym = ((JCTree.JCIdent) assign.lhs).sym;
        MemberValuePair memberPair = new MemberValuePair()
            .setName(convertSimpleName(sym, sym.asType(), getPosition(assign.lhs)))
            .setValue((Expression) convert(assign.rhs));
        normalAnn.addValue(memberPair);
      }
      newNode = normalAnn;
    }
    return newNode
        .setAnnotationMirror(node.attribute)
        .setTypeName((Name) convert(node.getAnnotationType()));
  }

  private List<Annotation> convertAnnotations(JCTree.JCModifiers modifiers) {
    List<Annotation> annotations = new ArrayList<>();
    for (AnnotationTree annotation : modifiers.getAnnotations()) {
      annotations.add((Annotation) convert(annotation));
    }
    return annotations;
  }

  private TreeNode convertAnnotationTypeDeclaration(JCTree.JCClassDecl node) {
    AnnotationTypeDeclaration newNode = new AnnotationTypeDeclaration();
    convertBodyDeclaration(node, node.getModifiers(), newNode, node.sym);
    for (JCTree bodyDecl : node.getMembers()) {
      if (bodyDecl.getKind() == Kind.METHOD) {
        JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) bodyDecl;
        AnnotationTypeMemberDeclaration newMember = new AnnotationTypeMemberDeclaration()
            .setDefault((Expression) convert(methodDecl.defaultValue))
            .setExecutableElement(methodDecl.sym);
        newMember
            .setModifiers((int) methodDecl.getModifiers().flags)
            .setAnnotations(convertAnnotations(methodDecl.mods))
            .setJavadoc((Javadoc) getAssociatedJavaDoc(methodDecl, methodDecl.sym));
        newNode.addBodyDeclaration(newMember);
      } else {
        newNode.addBodyDeclaration((BodyDeclaration) convert(bodyDecl));
      }
    }
    return newNode
        .setName(convertSimpleName(node.sym, node.type, getNamePosition(node)))
        .setTypeElement(node.sym);
  }

  private TreeNode convertArrayAccess(JCTree.JCArrayAccess node) {
    return new ArrayAccess()
        .setArray((Expression) convert(node.getExpression()))
        .setIndex((Expression) convert(node.getIndex()));
  }

  private TreeNode convertArrayType(JCTree.JCArrayTypeTree node) {
    ArrayType newNode = new ArrayType();
    Type componentType = (Type) Type.newType(node.getType().type);
    return newNode
        .setComponentType(componentType)
        .setTypeMirror(node.type);
  }

  private TreeNode convertAssert(JCTree.JCAssert node) {
    return new AssertStatement()
        .setExpression((Expression) convert(node.getCondition()))
        .setMessage((Expression) convert(node.getDetail()));
  }

  private TreeNode convertAssignment(JCTree.JCAssign node) {
    Assignment newNode = new Assignment();
    return newNode
        .setOperator(Assignment.Operator.ASSIGN)
        .setLeftHandSide((Expression) convert(node.getVariable()))
        .setRightHandSide((Expression) convert(node.getExpression()));
  }

  private TreeNode convertAssignOp(JCTree.JCAssignOp node) {
    Assignment newNode = new Assignment();
    String operatorName = node.getOperator().getSimpleName().toString() + "=";
    return newNode
        .setOperator(Assignment.Operator.fromOperatorName(operatorName))
        .setLeftHandSide((Expression) convert(node.getVariable()))
        .setRightHandSide((Expression) convert(node.getExpression()));
  }

  private TreeNode convertBinary(JCTree.JCBinary node) {
    InfixExpression newNode = new InfixExpression();
    newNode
        .setTypeMirror(node.type)
        .setOperator(InfixExpression.Operator.parse(node.getOperator().name.toString()));

    // Flatten this tree to avoid stack overflow with very deep trees. This
    // code traverses the subtree non-recursively and merges all children
    // that have the same operator into this node.
    List<StackState> stack = Lists.newArrayList();
    stack.add(new StackState(node));
    while (!stack.isEmpty()) {
      StackState currentState = stack.get(stack.size() - 1);
      JCTree.JCExpression child = currentState.nextChild();
      if (child == null) {
        stack.remove(stack.size() - 1);
        continue;
      }
      if (child instanceof JCTree.JCBinary) {
        JCTree.JCBinary infixChild = (JCTree.JCBinary) child;
        if (infixChild.getKind() == node.getKind()) {
          stack.add(new StackState(infixChild));
          continue;
        }
      }
      newNode.addOperand((Expression) convert(child));
    }
    return newNode;
  }

  private TreeNode convertBlock(JCTree.JCBlock node) {
    Block newNode = new Block();
    for (StatementTree stmt : node.getStatements()) {
      TreeNode tree = convert(stmt);
      if (tree instanceof AbstractTypeDeclaration) {
        tree = new TypeDeclarationStatement().setDeclaration((AbstractTypeDeclaration) tree);
      }
      newNode.addStatement((Statement) tree);
    }
    return newNode;
  }

  private TreeNode convertBodyDeclaration(JCTree node, JCTree.JCModifiers modifiers,
      BodyDeclaration newNode, Element element) {
    return newNode
        .setModifiers((int) modifiers.flags)
        .setAnnotations(convertAnnotations(modifiers))
        .setJavadoc((Javadoc) getAssociatedJavaDoc(node, element));
  }

  private TreeNode convertBooleanLiteral(JCTree.JCLiteral node) {
    return new BooleanLiteral((Boolean) node.getValue(), node.type);
  }

  private TreeNode convertBreakStatement(JCTree.JCBreak node) {
    BreakStatement newNode = new BreakStatement();
    Object label = node.getLabel();
    if (label != null) {
      newNode.setLabel((SimpleName)
          new SimpleName(label.toString()).setPosition(getPosition(node)));
    }
    return newNode;
  }

  private TreeNode convertCase(JCTree.JCCase node) {
    // Case statements are converted in convertSwitch().
    SwitchCase newNode = new SwitchCase();
    if (node.pat != null) {
      newNode.setExpression((Expression) convert(node.getExpression()));
    } else {
      newNode.setIsDefault(true);
    }
    return newNode;
  }

  private TreeNode convertCatch(JCTree.JCCatch node) {
    return new CatchClause()
        .setException((SingleVariableDeclaration) convert(node.getParameter()))
        .setBody((Block) convert(node.getBlock()));
  }

  private TreeNode convertCharLiteral(JCTree.JCLiteral node) {
    return new CharacterLiteral((Character) node.getValue(), node.type);
  }

  private TreeNode convertClassDeclaration(JCTree.JCClassDecl node) {
    // javac defines all type declarations with JCClassDecl, so differentiate here
    // to support our different declaration nodes.
    if (node.sym.getKind() == ElementKind.ANNOTATION_TYPE) {
      throw new AssertionError("Annotation type declaration tree conversion not implemented");
    }

    TypeDeclaration newNode =
        (TypeDeclaration) convertAbstractTypeDeclaration(node, new TypeDeclaration());

    newNode.setInterface(
        node.getKind() == Kind.INTERFACE || node.getKind() == Kind.ANNOTATION_TYPE);
    if (node.sym.isAnonymous()) {
      newUnit.getEnv().elementUtil().mapElementType(node.sym, node.type);
    }
    return newNode;
  }

  private TreeNode convertConditionalExpression(JCTree.JCConditional node) {
    return new ConditionalExpression()
        .setTypeMirror(node.type)
        .setExpression((Expression) convert(node.getCondition()))
        .setThenExpression((Expression) convert(node.getTrueExpression()))
        .setElseExpression((Expression) convert(node.getFalseExpression()));
  }

  private TreeNode convertContinueStatement(JCTree.JCContinue node) {
    ContinueStatement newNode = new ContinueStatement();
    Object label = node.getLabel();
    if (label != null) {
        newNode.setLabel((SimpleName)
            new SimpleName(label.toString()).setPosition(getPosition(node)));
    }
    return newNode;
  }

  private TreeNode convertDoStatement(JCTree.JCDoWhileLoop node) {
    return new DoStatement()
        .setExpression(convertWithoutParens(node.getCondition()))
        .setBody((Statement) convert(node.getStatement()));
  }

  private TreeNode convertEnhancedForStatement(JCTree.JCEnhancedForLoop node) {
    return new EnhancedForStatement()
        .setParameter((SingleVariableDeclaration) convertSingleVariable(node.getVariable())
            .setPosition(getPosition(node)))
        .setExpression((Expression) convert(node.getExpression()))
        .setBody((Statement) convert(node.getStatement()));
  }

  private TreeNode convertEnum(JCTree.JCClassDecl node) {
    if (node.sym.isAnonymous()) {
      return convertClassDeclaration(node).setPosition(getPosition(node));
    }
    EnumDeclaration newNode = new EnumDeclaration();
    convertBodyDeclaration(node, node.getModifiers(), newNode, node.sym);
    newNode
        .setName(convertSimpleName(node.sym, node.type, getNamePosition(node)))
        .setTypeElement(node.sym);
    for (JCTree bodyDecl : node.getMembers()) {
      if (bodyDecl.getKind() == Kind.VARIABLE) {
        TreeNode var = convertVariableDeclaration((JCTree.JCVariableDecl) bodyDecl);
        if (var.getKind() == TreeNode.Kind.ENUM_CONSTANT_DECLARATION) {
          newNode.addEnumConstant((EnumConstantDeclaration) var);
        } else {
          newNode.addBodyDeclaration((BodyDeclaration) var);
        }
      } else if (bodyDecl.getKind() == Kind.BLOCK) {
        JCTree.JCBlock javacBlock = (JCTree.JCBlock) bodyDecl;
        Block block = (Block) convert(javacBlock);
        newNode.addBodyDeclaration(new Initializer(block, javacBlock.isStatic()));
      } else {
        newNode.addBodyDeclaration((BodyDeclaration) convert(bodyDecl));
      }
    }
    return newNode;
  }

  private TreeNode copyConstantValue(JCTree node, Expression newNode) {
    Object value = node.type.constValue();
    if (value instanceof Integer) {
      switch (node.type.baseType().getKind()) {
        case BOOLEAN:
          // Convert boolean values of 1/0 as true/false.
          value = ((Integer) value).intValue() == 1;
          break;
        case CHAR:
          value = (char) ((Integer) value).intValue();
          break;
        default:  // value doesn't need to be changed.
      }
    }
    return value != null ? newNode.setConstantValue(value) : newNode;
  }

  private TreeNode convertExpressionStatement(JCTree.JCExpressionStatement node) {
    TreeNode expr = convert(node.getExpression());
    if (expr instanceof Statement) {
      return expr;
    }
    return new ExpressionStatement().setExpression((Expression) expr);
  }

  private TreeNode convertFieldAccess(JCTree.JCFieldAccess node) {
    String fieldName = node.name.toString();
    SourcePosition pos = getPosition(node);
    JCTree.JCExpression selected = node.getExpression();
    if (fieldName.equals("this")) {
      return new ThisExpression()
          .setQualifier((Name) convert(selected))
          .setTypeMirror(node.sym.asType());
    }
    if ("super".equals(getMemberName(selected))) {
      SuperFieldAccess newNode = new SuperFieldAccess()
          .setVariableElement((VariableElement) node.sym)
          .setTypeMirror(node.type);
      if (selected.getKind() == Kind.MEMBER_SELECT) {
        newNode.setQualifier((Name) convert(((JCTree.JCFieldAccess) selected).getExpression()));
      }
      return newNode;
    }
    if (node.getIdentifier().toString().equals("class")) {
      return new TypeLiteral(node.type)
          .setType((Type) convertType(selected.type, pos, false).setPosition(getPosition(node)));
    }
    if (selected.getKind() == Kind.IDENTIFIER && (!node.sym.getKind().isField()
        || ElementUtil.isConstant((VariableElement) node.sym))) {
      if (selected.toString().equals("this")) {
        // Just return the constant.
        return new SimpleName(node.sym);
      }
      JCIdent ident = (JCTree.JCIdent) selected;
      return new QualifiedName()
          .setName(convertSimpleName(node.sym, node.type, pos))
          .setQualifier(convertSimpleName(ident.sym, ident.type, pos))
          .setElement(node.sym);
    }
    if (selected.getKind() == Kind.MEMBER_SELECT) {
      TreeNode newSelected = convertFieldAccess((JCTree.JCFieldAccess) selected).setPosition(pos);
      if (newSelected.getKind() == TreeNode.Kind.QUALIFIED_NAME) {
        return new QualifiedName()
            .setName(convertSimpleName(node.sym, node.type, pos))
            .setQualifier((QualifiedName) newSelected)
            .setElement(node.sym);
      }
    }
    if (ElementUtil.isConstant((VariableElement) node.sym) && ElementUtil.isStatic(node.sym)
        && !(selected.getKind() == Kind.METHOD_INVOCATION)
        && !(selected.getKind() == Kind.MEMBER_SELECT)) {
      return new QualifiedName()
          .setName(convertSimpleName(node.sym, node.type, pos))
          .setQualifier((Name) convert(selected))
          .setElement(node.sym);
    }
    return new FieldAccess()
        .setVariableElement((VariableElement) node.sym)
        .setExpression((Expression) convert(selected))
        .setName(convertSimpleName(node.sym, node.type, pos).setTypeMirror(node.type));
  }

  private TreeNode convertForLoop(JCTree.JCForLoop node) {
    ForStatement newNode = new ForStatement()
        .setExpression((Expression) convert(node.getCondition()))
        .setBody((Statement) convert(node.getStatement()));
    VariableDeclarationExpression lastVar = null;
    for (JCTree.JCStatement initializer : node.getInitializer()) {
      if (initializer.getKind() == Kind.VARIABLE) {
        JCTree.JCVariableDecl var = (JCTree.JCVariableDecl) initializer;
        VariableDeclarationExpression newVar = convertVariableExpression(var);
        if (lastVar == null) {
          newNode.addInitializer(newVar);
          lastVar = newVar;
        } else {
          lastVar.addFragment(TreeUtil.remove(newVar.getFragment(0)));
        }
      } else {
        assert initializer.getKind() == Kind.EXPRESSION_STATEMENT;
        newNode.addInitializer((Expression)
          convert(((JCTree.JCExpressionStatement) initializer).getExpression()));
      }
    }
    for (JCTree.JCExpressionStatement updater : node.getUpdate()) {
      newNode.addUpdater((Expression) convert(updater.getExpression()));
    }
    return newNode;
  }

  private TreeNode convertFunctionalExpression(
      JCTree.JCFunctionalExpression node, FunctionalExpression newNode) {
    for (TypeMirror type : getTargets(node)) {
      newNode.addTargetType(type);
    }
    return newNode
        .setTypeMirror(getTargets(node).iterator().next())
        .setDescriptor(
            new ExecutablePair(
                (ExecutableElement) types.findDescriptorSymbol(getTargets(node).head.tsym),
                (ExecutableType) node.getDescriptorType(types)));
  }

  private static com.sun.tools.javac.util.List<com.sun.tools.javac.code.Type> getTargets(
      JCTree.JCFunctionalExpression node) {
    try {
      return (com.sun.tools.javac.util.List<com.sun.tools.javac.code.Type>)
          JCTree.JCFunctionalExpression.class.getField("targets").get(node);
    } catch (ReflectiveOperationException e) {
      // continue below
    }
    try {
      return com.sun.tools.javac.util.List.of(
          (com.sun.tools.javac.code.Type)
              JCTree.JCFunctionalExpression.class.getField("target").get(node));
    } catch (ReflectiveOperationException e) {
      throw new LinkageError(e.getMessage(), e);
    }
  }

  private TreeNode convertIdent(JCTree.JCIdent node) {
    String text = node.sym.toString();
    if (text.equals("this")) {
      return new ThisExpression().setTypeMirror(node.type);
    }
    return new SimpleName(node.sym.baseSymbol(), node.type);
  }

  private TreeNode convertIf(JCTree.JCIf node) {
    return new IfStatement()
        .setExpression(convertWithoutParens(node.getCondition()))
        .setThenStatement((Statement) convert(node.getThenStatement()))
        .setElseStatement((Statement) convert(node.getElseStatement()));
  }

  private TreeNode convertInstanceOf(JCTree.JCInstanceOf node) {
    TypeMirror clazz = nameType(node.getType());
    return new InstanceofExpression()
        .setLeftOperand((Expression) convert(node.getExpression()))
        .setRightOperand(Type.newType(clazz))
        .setTypeMirror(node.type);
  }

  private TreeNode convertLabeledStatement(JCTree.JCLabeledStatement node) {
    return new LabeledStatement()
        .setLabel((SimpleName) new SimpleName(node.label.toString()).setPosition(getPosition(node)))
        .setBody((Statement) convert(node.body));
  }

  private TreeNode convertLambda(JCTree.JCLambda node) {
    LambdaExpression newNode = new LambdaExpression();
    convertFunctionalExpression(node, newNode);
    for (JCVariableDecl param : node.params) {
      newNode.addParameter((VariableDeclaration) convert(param));
    }
    return newNode.setBody(convert(node.getBody()));
  }

  private TreeNode convertMethodReference(JCTree.JCMemberReference node, MethodReference newNode) {
    convertFunctionalExpression(node, newNode);
    if (node.getTypeArguments() != null) {
      for (JCTree.JCExpression typeArg : node.getTypeArguments()) {
        newNode.addTypeArgument(Type.newType(typeArg.type));
      }
    }
    return newNode
        .setExecutableElement((ExecutableElement) node.sym)
        .setVarargsType(node.varargsElement);
  }

  private TreeNode convertMemberReference(JCTree.JCMemberReference node) {
    Element element = node.sym;
    SourcePosition pos = getPosition(node);
    if (ElementUtil.isConstructor(element)) {
      CreationReference newNode = new CreationReference();
      convertMethodReference(node, newNode);
      return newNode
          .setType(Type.newType(nameType(node.expr)));
    }
    if (node.hasKind(JCTree.JCMemberReference.ReferenceKind.SUPER)) {
      SuperMethodReference newNode = new SuperMethodReference();
      convertMethodReference(node, newNode);
      if (node.getQualifierExpression().getKind() == Kind.IDENTIFIER) {
        // super::foo
        return newNode;
      } else {
        // Qualifier expression is <name>."super", so it's always a JCFieldAccess.
        JCTree.JCFieldAccess expr = (JCTree.JCFieldAccess) node.getQualifierExpression();
        return newNode
            .setQualifier(
                convertSimpleName(nameSymbol(expr.selected), expr.type,
                    getPosition(expr.selected)));
      }
    }
    if (node.hasKind(JCTree.JCMemberReference.ReferenceKind.UNBOUND)
        || node.hasKind(JCTree.JCMemberReference.ReferenceKind.STATIC)) {
      TypeMethodReference newNode = new TypeMethodReference();
      convertMethodReference(node, newNode);
      return newNode
          .setType(convertType(node.type, pos, false));
    }

    ExpressionMethodReference newNode = new ExpressionMethodReference();
    convertMethodReference(node, newNode);
    return newNode.setExpression((Expression) convert(node.getQualifierExpression()));
  }

  private TreeNode convertMethodDeclaration(JCTree.JCMethodDecl node) {
    MethodDeclaration newNode = new MethodDeclaration();

    // JCMethodDecl's preferred diagnostic position is the beginning of the method name.
    int methodStartPosition = node.pos().getPreferredPosition();

    int length = ElementUtil.isConstructor(node.sym)
        ? node.sym.toString().indexOf('(')
        : node.name.length();

    Name name = Name.newName(null /* qualifier */, node.sym);
    name.setPosition(new SourcePosition(methodStartPosition, length));

    convertBodyDeclaration(node, node.getModifiers(), newNode, node.sym);
    for (JCTree.JCVariableDecl param : node.getParameters()) {
      newNode.addParameter((SingleVariableDeclaration) convert(param));
    }
    return newNode
        .setIsConstructor(ElementUtil.isConstructor(node.sym))
        .setExecutableElement(node.sym)
        .setBody((Block) convert(node.getBody()))
        .setName(name);
  }

  private static String getMemberName(JCTree.JCExpression node) {
    switch (node.getKind()) {
      case IDENTIFIER: return node.toString();
      case MEMBER_SELECT: return ((JCTree.JCFieldAccess) node).name.toString();
      default: return null;
    }
  }

  private static Symbol getMemberSymbol(JCTree.JCExpression node) {
    switch (node.getKind()) {
      case IDENTIFIER: return ((JCTree.JCIdent) node).sym.baseSymbol();
      case MEMBER_SELECT: return ((JCTree.JCFieldAccess) node).sym;
      default: throw new AssertionError("Unexpected tree kind: " + node.getKind());
    }
  }

  private TreeNode convertMethodInvocation(JCTree.JCMethodInvocation node) {
    JCTree.JCExpression method = node.getMethodSelect();
    String methodName = getMemberName(method);
    ExecutableType type = (ExecutableType) method.type;
    Symbol.MethodSymbol sym = (Symbol.MethodSymbol) getMemberSymbol(method);
    JCTree.JCExpression target = method.getKind() == Kind.MEMBER_SELECT
        ? ((JCTree.JCFieldAccess) method).selected : null;

    if ("this".equals(methodName)) {
      ConstructorInvocation newNode = new ConstructorInvocation()
          .setExecutablePair(new ExecutablePair(sym))
          .setVarargsType(node.varargsElement);
      for (JCTree.JCExpression arg : node.getArguments()) {
        newNode.addArgument((Expression) convert(arg));
      }
      return newNode;
    }

    if ("super".equals(methodName)) {
      SuperConstructorInvocation newNode = new SuperConstructorInvocation()
          .setExecutablePair(new ExecutablePair(sym))
          .setVarargsType(node.varargsElement);
      if (target != null) {
        newNode.setExpression((Expression) convert(target));
      }
      for (JCTree.JCExpression arg : node.getArguments()) {
        newNode.addArgument((Expression) convert(arg));
      }
      return newNode;
    }

    if (target != null && "super".equals(getMemberName(target))) {
      SuperMethodInvocation newNode = new SuperMethodInvocation()
          .setExecutablePair(new ExecutablePair(sym, type))
          .setVarargsType(node.varargsElement);
      if (target.getKind() == Kind.MEMBER_SELECT) {
        // foo.bar.MyClass.super.print(...):
        //   target: foo.bar.MyClass.super
        //   target.selected: foo.bar.MyClass
        newNode.setQualifier((Name) convert(((JCTree.JCFieldAccess) target).selected));
      }
      for (JCTree.JCExpression arg : node.getArguments()) {
        newNode.addArgument((Expression) convert(arg));
      }
      return newNode;
    }

    MethodInvocation newNode = new MethodInvocation();
    if (target != null) {
      newNode.setExpression((Expression) convert(target));
    }
    for (JCTree.JCExpression arg : node.getArguments()) {
      newNode.addArgument((Expression) convert(arg));
    }
    return newNode
        .setTypeMirror(node.type)
        .setExecutablePair(new ExecutablePair(sym, type))
        .setVarargsType(node.varargsElement);
  }

  private SimpleName convertSimpleName(Element element, TypeMirror type, SourcePosition pos) {
    return (SimpleName) new SimpleName(element, type).setPosition(pos);
  }

  private Name convertName(Symbol symbol, SourcePosition pos) {
    if (symbol.owner == null || symbol.owner.name.isEmpty()) {
      return new SimpleName(symbol);
    }
    return new QualifiedName(symbol, symbol.asType(), convertName(symbol.owner, pos));
  }

  private TreeNode convertNewArray(JCTree.JCNewArray node) {
    ArrayCreation newNode = new ArrayCreation();
    List<Expression> dimensions = new ArrayList<>();
    for (JCTree.JCExpression dimension : node.getDimensions()) {
      dimensions.add((Expression) convert(dimension));
    }
    javax.lang.model.type.ArrayType type = (javax.lang.model.type.ArrayType) node.type;
    if (node.getInitializers() != null) {
      ArrayInitializer initializers = new ArrayInitializer(type);
      for (JCTree.JCExpression initializer : node.getInitializers()) {
        initializers.addExpression((Expression) convert(initializer));
      }
      newNode.setInitializer(initializers);
    }
    return newNode
        .setType((ArrayType) new ArrayType(type).setPosition(getPosition(node)))
        .setDimensions(dimensions);
  }

  private TreeNode convertNewClass(JCTree.JCNewClass node) {
    ClassInstanceCreation newNode = new ClassInstanceCreation();
    for (JCTree.JCExpression arg : node.getArguments()) {
      newNode.addArgument((Expression) convert(arg));
    }
    return newNode
        .setExecutablePair(new ExecutablePair((ExecutableElement) node.constructor))
        .setVarargsType(node.varargsElement)
        .setExpression((Expression) convert(node.getEnclosingExpression()))
        .setType(convertType(node.clazz.type))
        .setAnonymousClassDeclaration((TypeDeclaration) convert(node.def));
  }

  private TreeNode convertNumberLiteral(JCTree.JCLiteral node) {
    return new NumberLiteral((Number) node.getValue(), node.type)
        .setToken(getTreeSource(node));
  }

  private PackageDeclaration convertPackage(PackageElement pkg, Trees trees) {
    JCTree node = (JCTree) trees.getTree(pkg);
    PackageDeclaration newNode = new PackageDeclaration()
        .setPackageElement(pkg);
    for (JCTree.JCAnnotation pkgAnnotation : unit.getPackageAnnotations()) {
      newNode.addAnnotation((Annotation) convert(pkgAnnotation));
    }
    if (unit.sourcefile.toUri().getPath().endsWith("package-info.java")) {
      if (node == null) {
        // Java 8 javac bug, fixed in Java 9. Doc-comments in package-info.java
        // sources are keyed to their compilation unit, not their package node.
        node = unit;
      }
      newNode.setJavadoc((Javadoc) getAssociatedJavaDoc(node, pkg));
    }
    return (PackageDeclaration) newNode.setName(convertName((PackageSymbol) pkg, getPosition(node)))
        .setPosition(SourcePosition.NO_POSITION);
  }

  private TreeNode convertPrefixExpr(JCTree.JCUnary node) {
    return new PrefixExpression()
        .setTypeMirror(node.type)
        .setOperator(PrefixExpression.Operator.parse(node.getOperator().name.toString()))
        .setOperand((Expression) convert(node.getExpression()));
  }

  private TreeNode convertParens(JCTree.JCParens node) {
    return new ParenthesizedExpression()
        .setExpression((Expression) convert(node.getExpression()));
  }

  private TreeNode convertPostExpr(JCTree.JCUnary node) {
    return new PostfixExpression()
        .setOperator(PostfixExpression.Operator.parse(node.getOperator().name.toString()))
        .setOperand((Expression) convert(node.getExpression()));
  }

  private TreeNode convertPrimitiveType(JCTree.JCPrimitiveTypeTree node) {
    return new PrimitiveType(node.type);
  }

  private TreeNode convertReturn(JCTree.JCReturn node) {
    return new ReturnStatement((Expression) convert(node.getExpression()));
  }

  private TreeNode convertStringLiteral(JCTree.JCLiteral node) {
    return new StringLiteral((String) node.getValue(), node.type);
  }

  private TreeNode convertSwitch(JCTree.JCSwitch node) {
    SwitchStatement newNode = new SwitchStatement()
        .setExpression(convertWithoutParens(node.getExpression()));
    for (JCTree.JCCase switchCase : node.getCases()) {
      newNode.addStatement((SwitchCase) convert(switchCase));
      for (JCTree.JCStatement s : switchCase.getStatements()) {
        newNode.addStatement((Statement) convert(s));
      }
    }
    return newNode;
  }

  private TreeNode convertSynchronized(JCTree.JCSynchronized node) {
    return new SynchronizedStatement()
        .setExpression((Expression) convertWithoutParens(node.getExpression())
            .setPosition(getPosition(node)))
        .setBody((Block) convert(node.getBlock()));
  }

  private TreeNode convertThrow(JCTree.JCThrow node) {
    return new ThrowStatement()
        .setExpression((Expression) convert(node.getExpression()));
  }

  private TreeNode convertTry(JCTree.JCTry node) {
    TryStatement newNode = new TryStatement();
    for (Object obj : node.getResources()) {
      newNode.addResource(convertVariableExpression((JCTree.JCVariableDecl) obj));
    }
    for (Object obj : node.getCatches()) {
      newNode.addCatchClause((CatchClause) convert(obj));
    }
    return newNode
        .setBody((Block) convert(node.getBlock()))
        .setFinally((Block) convert(node.getFinallyBlock()));
  }

  private Type convertType(TypeMirror typeMirror) {
    com.sun.tools.javac.code.Type type = (com.sun.tools.javac.code.Type) typeMirror;
    if (type.getKind() == TypeKind.EXECUTABLE) {
      Type returnType = Type.newType(type.getReturnType());
      if (type.hasTag(TypeTag.FORALL)) {
        return new ParameterizedType()
            .setType(returnType)
            .setTypeMirror(type.getReturnType());
      } else {
        return returnType;
      }
    }
    if (type.getKind() == TypeKind.DECLARED) {
      List<? extends TypeMirror> typeArgs = ((DeclaredType) type).getTypeArguments();
      if (!typeArgs.isEmpty()) {
        return new ParameterizedType()
            .setType(Type.newType(typeMirror))
            .setTypeMirror(typeMirror);
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
        newType = new ParameterizedType()
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

  private TypeMirror nameType(JCTree node) {
    if (node.getKind() == Kind.PARAMETERIZED_TYPE) {
      return ((JCTree.JCTypeApply) node).clazz.type;
    }
    if (node.getKind() == Kind.ARRAY_TYPE) {
      return ((JCTree.JCArrayTypeTree) node).type;
    }
    return nameSymbol(node).asType();
  }

  private Symbol nameSymbol(JCTree node) {
    return node.getKind() == Kind.MEMBER_SELECT
        ? ((JCTree.JCFieldAccess) node).sym
        : ((JCTree.JCIdent) node).sym;
  }

  private TreeNode convertTypeApply(JCTree.JCTypeApply node) {
    return new ParameterizedType()
        .setType(Type.newType(node.type))
        .setTypeMirror(node.type);
  }

  private TreeNode convertTypeCast(JCTree.JCTypeCast node) {
    return new CastExpression(node.type, (Expression) convert(node.getExpression()));
  }

  private TreeNode convertVariableDeclaration(JCTree.JCVariableDecl node) {
    VarSymbol var = node.sym;
    if (var.getKind() == ElementKind.FIELD) {
      FieldDeclaration newNode = new FieldDeclaration(var,
          (Expression) convert(node.getInitializer()));
      convertBodyDeclaration(node, node.getModifiers(), newNode, var);
      return newNode;
    }
    if (var.getKind() == ElementKind.LOCAL_VARIABLE) {
      return new VariableDeclarationStatement(var, (Expression) convert(node.getInitializer()));
    }
    if (var.getKind() == ElementKind.ENUM_CONSTANT) {
      EnumConstantDeclaration newNode = new EnumConstantDeclaration()
          .setVariableElement(var);
      convertBodyDeclaration(node, node.getModifiers(), newNode, var);
      ClassInstanceCreation init = (ClassInstanceCreation) convert(node.getInitializer());
      TreeUtil.moveList(init.getArguments(), newNode.getArguments());
      if (init.getAnonymousClassDeclaration() != null) {
        newNode.setAnonymousClassDeclaration(TreeUtil.remove(init.getAnonymousClassDeclaration()));
      }
      return newNode
          .setExecutablePair(init.getExecutablePair())
          .setVarargsType(init.getVarargsType());
    }
    return convertSingleVariable(node);
  }

  private TreeNode convertSingleVariable(JCTree.JCVariableDecl node) {
    VarSymbol var = node.sym;
    SourcePosition pos = getPosition(node);
    boolean isVarargs = (node.sym.flags() & Flags.VARARGS) > 0;
    Type newType = convertType(var.asType(), pos, isVarargs);
    return new SingleVariableDeclaration()
        .setType(newType)
        .setIsVarargs(isVarargs)
        .setAnnotations(convertAnnotations(node.getModifiers()))
        .setVariableElement(var)
        .setInitializer((Expression) convert(node.getInitializer()));
  }

  private VariableDeclarationExpression convertVariableExpression(JCTree.JCVariableDecl node) {
    VarSymbol var = node.sym;
    boolean isVarargs = (node.sym.flags() & Flags.VARARGS) > 0;
    Type newType = convertType(var.asType(), getPosition(node), isVarargs);
    VariableDeclarationFragment fragment = new VariableDeclarationFragment();
    fragment
        .setVariableElement(var)
        .setInitializer((Expression) convert(node.getInitializer()));
    return new VariableDeclarationExpression()
        .setType(newType)
        .addFragment(fragment);
  }

  private TreeNode convertWhileLoop(JCTree.JCWhileLoop node) {
    return new WhileStatement()
        .setExpression(convertWithoutParens(node.getCondition()))
        .setBody((Statement) convert(node.getStatement()));
  }

  private TreeNode getAssociatedJavaDoc(JCTree node, Element element) {
    Comment comment = convertAssociatedComment(node, element);
    return comment != null && comment.isDocComment() ? comment : null;
  }

  private Comment convertAssociatedComment(JCTree node, Element element) {
    boolean docCommentsEnabled = newUnit.getEnv().options().docCommentsEnabled();
    DocCommentTable docComments = unit.docComments;
    if (!docCommentsEnabled || docComments == null || !docComments.hasComment(node)) {
      return null;
    }
    com.sun.tools.javac.parser.Tokens.Comment javacComment = docComments.getComment(node);
    Comment comment;
    switch (javacComment.getStyle()) {
      case BLOCK:
        comment = new BlockComment();
        break;
      case JAVADOC:
        comment = docCommentsEnabled
            ? convertJavadocComment(element) : new Javadoc();
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
    comment.setLineNumber(unit.getLineMap().getLineNumber(startPos));
    return comment;
  }

  private Javadoc convertJavadocComment(Element element) {
    return JavadocConverter.convertJavadoc(element, newUnit.getSource(), env,
        newUnit.getEnv().options().reportJavadocWarnings());
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
        endPos += 4;  // Include closing delimiter.
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

  private String getTreeSource(JCTree node) {
    try {
      CharSequence source = unit.getSourceFile().getCharContent(true);
      return source.subSequence(node.getStartPosition(), node.getEndPosition(unit.endPositions))
          .toString();
    } catch (IOException e) {
      return node.toString();
    }
  }

  // javac uses a JCParens for the if, do, and while statements, while JDT doesn't.
  private Expression convertWithoutParens(JCTree.JCExpression condition) {
    Expression result = (Expression) convert(condition);
    if (result.getKind() == TreeNode.Kind.PARENTHESIZED_EXPRESSION) {
      result = TreeUtil.remove(((ParenthesizedExpression) result).getExpression());
    }
    return result;
  }

  private SourcePosition getSourcePosition(int start, int end) {
    if (unit.getLineMap() != null) {
      int line = unit.getLineMap().getLineNumber(start);
      return new SourcePosition(start, end, line);
    } else {
      return new SourcePosition(start, end);
    }
  }

  // Return best guess for the position of a declaration node's name.
  private SourcePosition getNamePosition(JCTree node) {
    if (node.pos == -1) {
      return SourcePosition.NO_POSITION;
    }
    String src = newUnit.getSource();
    int start = node.pos;
    Kind kind = node.getKind();
    if (kind == Kind.ANNOTATION_TYPE || kind == Kind.CLASS
        || kind == Kind.ENUM || kind == Kind.INTERFACE) {
      // Skip the class/enum/interface token.
      while (src.charAt(start++) != ' ') {
      }
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

  // Helper class for convertBinary().
  private static class StackState {
    private final JCTree.JCBinary expression;
    private int nextChild = -2;

    private StackState(JCTree.JCBinary expr) {
      expression = expr;
    }

    private JCTree.JCExpression nextChild() {
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
