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

package com.google.devtools.j2objc.gen;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TranslationEnvironment;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import com.google.j2objc.annotations.GenerateObjectiveCGenerics;
import com.google.j2objc.annotations.ObjectiveCName;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * The base class for TypeDeclarationGenerator and TypeImplementationGenerator,
 * providing common routines.
 *
 * @author Tom Ball, Keith Stanger
 */
public abstract class TypeGenerator extends AbstractSourceGenerator {

  // Convenient fields for use by subclasses.
  protected final AbstractTypeDeclaration typeNode;
  protected final ElementUtil elementUtil;
  protected final TypeElement typeElement;
  protected final CompilationUnit compilationUnit;
  protected final TranslationEnvironment env;
  protected final TypeUtil typeUtil;
  protected final NameTable nameTable;
  protected final String typeName;
  protected final Options options;
  protected final boolean parametersNonnullByDefault;
  protected final boolean nullMarked;

  private final List<BodyDeclaration> declarations;

  protected TypeGenerator(SourceBuilder builder, AbstractTypeDeclaration node) {
    super(builder);
    typeNode = node;
    typeElement = node.getTypeElement();
    compilationUnit = TreeUtil.getCompilationUnit(node);
    env = compilationUnit.getEnv();
    elementUtil = env.elementUtil();
    typeUtil = env.typeUtil();
    nameTable = env.nameTable();
    typeName = nameTable.getFullName(typeElement);
    declarations = filterDeclarations(node.getBodyDeclarations());
    options = env.options();
    parametersNonnullByDefault = options.nullability()
        && env.elementUtil().areParametersNonnullByDefault(node.getTypeElement(), options);

    boolean isElementNullMarked = env.elementUtil().isNullMarked(node.getTypeElement(), options);
    // Note: Enums are implicitly marked as nonnull when the `nullMarked` experimental
    //       feature is enabled. This is true even if the `@NullMarked` annotation is
    //       not present on the package or type.
    boolean nullMarkedEnabled = options.nullMarked();
    boolean isEnumDeclaration = (typeNode instanceof EnumDeclaration);
    nullMarked = isElementNullMarked || (nullMarkedEnabled && isEnumDeclaration);
  }

  protected boolean shouldPrintDeclaration(BodyDeclaration decl) {
    return true;
  }

  private List<BodyDeclaration> filterDeclarations(Iterable<BodyDeclaration> declarations) {
    List<BodyDeclaration> filteredDecls = Lists.newArrayList();
    for (BodyDeclaration decl : declarations) {
      if (shouldPrintDeclaration(decl)) {
        filteredDecls.add(decl);
      }
    }
    return filteredDecls;
  }

  private static final Predicate<VariableDeclarationFragment> IS_STATIC_FIELD =
      new Predicate<VariableDeclarationFragment>() {
    @Override
    public boolean apply(VariableDeclarationFragment frag) {
      // isGlobalVar includes non-static but final primitives, which are treated
      // like static fields in J2ObjC.
      return ElementUtil.isGlobalVar(frag.getVariableElement());
    }
  };

  private static final Predicate<VariableDeclarationFragment> IS_INSTANCE_FIELD =
      new Predicate<VariableDeclarationFragment>() {
    @Override
    public boolean apply(VariableDeclarationFragment frag) {
      return ElementUtil.isInstanceVar(frag.getVariableElement());
    }
  };

  private static final Predicate<BodyDeclaration> IS_OUTER_DECL = new Predicate<BodyDeclaration>() {
    @Override
    public boolean apply(BodyDeclaration decl) {
      switch (decl.getKind()) {
        case FUNCTION_DECLARATION:
          return true;
        case NATIVE_DECLARATION:
          return ((NativeDeclaration) decl).isOuter();
        default:
          return false;
      }
    }
  };

  private static final Predicate<BodyDeclaration> IS_INNER_DECL = new Predicate<BodyDeclaration>() {
    @Override
    public boolean apply(BodyDeclaration decl) {
      switch (decl.getKind()) {
        case METHOD_DECLARATION:
          return true;
        case NATIVE_DECLARATION:
          return !((NativeDeclaration) decl).isOuter();
        default:
          return false;
      }
    }
  };

  // This predicate returns true if the declaration generates implementation
  // code inside a @implementation declaration.
  private static final Predicate<BodyDeclaration> HAS_INNER_IMPL =
      new Predicate<BodyDeclaration>() {
    @Override
    public boolean apply(BodyDeclaration decl) {
      return decl.getKind() == TreeNode.Kind.METHOD_DECLARATION
          && !Modifier.isAbstract(((MethodDeclaration) decl).getModifiers());
    }
  };

  protected abstract void printFunctionDeclaration(FunctionDeclaration decl);
  protected abstract void printMethodDeclaration(MethodDeclaration decl);
  protected abstract void printNativeDeclaration(NativeDeclaration decl);

  private void printDeclaration(BodyDeclaration declaration) {
    switch (declaration.getKind()) {
      case FUNCTION_DECLARATION:
        printFunctionDeclaration((FunctionDeclaration) declaration);
        return;
      case METHOD_DECLARATION:
        printMethodDeclaration((MethodDeclaration) declaration);
        return;
      case NATIVE_DECLARATION:
        printNativeDeclaration((NativeDeclaration) declaration);
        return;
      default:
        break;
    }
  }

  protected void printDeclarations(Iterable<? extends BodyDeclaration> declarations) {
    for (BodyDeclaration declaration : declarations) {
      printDeclaration(declaration);
    }
  }

  protected boolean isInterfaceType() {
    return typeElement.getKind().isInterface();
  }

  protected Iterable<VariableDeclarationFragment> getInstanceFields() {
    return getInstanceFields(declarations);
  }

  protected Iterable<VariableDeclarationFragment> getAllInstanceFields() {
    return getInstanceFields(typeNode.getBodyDeclarations());
  }

  private Iterable<VariableDeclarationFragment> getInstanceFields(List<BodyDeclaration> decls) {
    return Iterables.filter(
        TreeUtil.asFragments(Iterables.filter(decls, FieldDeclaration.class)),
        IS_INSTANCE_FIELD);
  }

  protected Iterable<VariableDeclarationFragment> getStaticFields() {
    return Iterables.filter(
        TreeUtil.asFragments(Iterables.filter(declarations, FieldDeclaration.class)),
        IS_STATIC_FIELD);
  }

  protected Iterable<VariableDeclarationFragment> getAllFields() {
    return TreeUtil.asFragments(
        Iterables.filter(typeNode.getBodyDeclarations(), FieldDeclaration.class));
  }

  protected Iterable<BodyDeclaration> getInnerDeclarations() {
    return Iterables.filter(declarations, IS_INNER_DECL);
  }

  protected Iterable<BodyDeclaration> getOuterDeclarations() {
    return Iterables.filter(declarations, IS_OUTER_DECL);
  }

  protected void printInnerDeclarations() {
    printDeclarations(getInnerDeclarations());
  }

  protected void printOuterDeclarations() {
    printDeclarations(getOuterDeclarations());
  }

  private boolean hasStaticAccessorMethods() {
    if (!options.staticAccessorMethods()) {
      return false;
    }
    for (VariableDeclarationFragment fragment : TreeUtil.getAllFields(typeNode)) {
      if (ElementUtil.isStatic(fragment.getVariableElement())
          && !((FieldDeclaration) fragment.getParent()).hasPrivateDeclaration()) {
        return true;
      }
    }
    return false;
  }

  private boolean hasStaticMethods() {
    return !Iterables.isEmpty(
        Iterables.filter(ElementUtil.getMethods(typeElement), ElementUtil::isStatic));
  }

  protected boolean needsPublicCompanionClass() {
    if (typeNode.hasPrivateDeclaration()) {
      return false;
    }
    return hasInitializeMethod()
        || hasStaticAccessorMethods()
        || ElementUtil.isGeneratedAnnotation(typeElement)
        || hasStaticMethods();
  }

  protected boolean needsCompanionClass() {
    return needsPublicCompanionClass()
        || !Iterables.isEmpty(Iterables.filter(typeNode.getBodyDeclarations(), HAS_INNER_IMPL));
  }

  protected boolean hasInitializeMethod() {
    return !typeNode.getClassInitStatements().isEmpty();
  }

  protected boolean needsTypeLiteral() {
    return !(ElementUtil.isPackageInfo(typeElement) || ElementUtil.isAnonymous(typeElement)
             || ElementUtil.isLambda(typeElement));
  }

  protected String getDeclarationType(VariableElement var) {
    TypeMirror type = var.asType();
    if (ElementUtil.isVolatile(var)) {
      return "volatile_" + NameTable.getPrimitiveObjCType(type);
    } else {
      return nameTable.getObjCType(type);
    }
  }

  protected boolean hasGenerateObjectiveCGenerics(TypeMirror type) {
    return TypeUtil.asTypeElement(type) != null
        && ElementUtil.hasAnnotation(
            TypeUtil.asTypeElement(type), GenerateObjectiveCGenerics.class);
  }

  protected String getReturnType(MethodDeclaration m, boolean asObjCGenericDecl) {
    ExecutableElement element = m.getExecutableElement();
    return nameTable.getObjCTypeDeclaration(
        element.getReturnType(),
        hasGenerateObjectiveCGenerics(element.getReturnType()) || asObjCGenericDecl);
  }

  /** Create an Objective-C method signature string. */
  protected String getMethodSignature(MethodDeclaration m, boolean asObjCGenericDecl) {
    StringBuilder sb = new StringBuilder();
    ExecutableElement element = m.getExecutableElement();
    char prefix = Modifier.isStatic(m.getModifiers()) ? '+' : '-';
    String returnType = getReturnType(m, asObjCGenericDecl);
    String selector = nameTable.getMethodSelector(element);

    // Verify the same number of parameters are defined by the method and the annotation.
    long colonCount = selector.chars().filter(ch -> ch == ':').count();
    if (element.getParameters().size() != colonCount) {
      AnnotationMirror annotation = ElementUtil.getAnnotation(element, ObjectiveCName.class);
      if (annotation != null) {
        String declaredSelector = (String) ElementUtil.getAnnotationValue(annotation, "value");
        String methodName = ElementUtil.getName(element);
        ErrorUtil.error(m,
            "Invalid selector: @ObjectiveCName(\""
                + declaredSelector
                + ")\" has a different number of parameters than method declaration.");
        return methodName;
      }
    }

    if (m.isConstructor()) {
      returnType = "instancetype";
    } else if (selector.equals("hash")) {
      // Explicitly test hashCode() because of NSObject's hash return value.
      returnType = "NSUInteger";
    }
    sb.append(UnicodeUtils.format("%c (%s%s)", prefix, returnType, nullability(element)));

    List<SingleVariableDeclaration> params = m.getParameters();
    String[] selParts = selector.split(":");

    if (params.isEmpty()) {
      assert selParts.length == 1 && !selector.endsWith(":");
      sb.append(selParts[0]);
    } else {
      assert params.size() == selParts.length;
      int baseLength = sb.length() + selParts[0].length();
      for (int i = 0; i < params.size(); i++) {
        if (i != 0) {
          sb.append('\n');
          sb.append(pad(baseLength - selParts[i].length()));
        }
        VariableElement var = params.get(i).getVariableElement();
        String typeName =
            nameTable.getObjCTypeDeclaration(
                var.asType(), hasGenerateObjectiveCGenerics(var.asType()) || asObjCGenericDecl);
        sb.append(
            UnicodeUtils.format(
                "%s:(%s%s)%s",
                selParts[i], typeName, nullability(var), nameTable.getVariableShortName(var)));
      }
    }

    return sb.toString();
  }

  /** Returns an Objective-C nullability attribute string if needed. */
  protected abstract String nullability(Element element);

  protected String getFunctionSignature(FunctionDeclaration function, boolean isPrototype) {
    StringBuilder sb = new StringBuilder();
    TypeMirror returnTypeMirror = function.getReturnType().getTypeMirror();
    String returnType =
        paddedType(nameTable.getObjCType(returnTypeMirror), function.getExecutableElement());
    sb.append(returnType).append(function.getName()).append('(');
    if (isPrototype && function.getParameters().isEmpty()) {
      sb.append("void");
    } else {
      for (Iterator<SingleVariableDeclaration> iter = function.getParameters().iterator();
           iter.hasNext(); ) {
        VariableElement var = iter.next().getVariableElement();
        String paramType = paddedType(nameTable.getObjCType(var.asType()), var);
        sb.append(paramType + nameTable.getVariableShortName(var));
        if (iter.hasNext()) {
          sb.append(", ");
        }
      }
    }
    sb.append(')');
    return sb.toString();
  }

  /**
   * Returns a String representation of a type with trailing padding and nullable annotations, if
   * applicable.
   *
   * <p><b>Note:</b> If {@code type} is not a pointer, a new String is returned with a trailing
   * space. If {@code type} represents a pointer, {@code element} represents a nullable type, and
   * NullMarked is enabled, an Objective-C nullability specifier is appended with a trailing space.
   * Otherwise, trailing padding is not added to the returned String.
   *
   * @param type the string representation of the data type.
   * @param element represents a program element such as a package, class, or method.
   */
  protected String paddedType(String type, Element element) {
    String suffix = " ";
    if (type.endsWith("*")) {
      suffix = shouldAddNullableAnnotation(element) ? "_Nullable " : "";
    }
    return type + suffix;
  }

  // TODO: b/287612419 - Update call-sites to pass through a String representation of
  //                     the related type to ensure false is returned in those cases.
  /**
   * Returns a boolean indicating if {@code element} should have a nullable annotation applied to it
   * if it is a pointer type.
   *
   * <p><b>Warning:</b> The method should only be called when {@code element} is known to be a
   * pointer type.
   *
   * @param element represents a program element such as a package, class, or method.
   */
  protected boolean shouldAddNullableAnnotation(Element element) {
    boolean hasNullableAnnotation = element != null && ElementUtil.hasNullableAnnotation(element);
    return hasNullableAnnotation && nullMarked;
  }

  protected String generateExpression(Expression expr) {
    return StatementGenerator.generate(expr, getBuilder().getCurrentLine());
  }
}
