/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.Javadoc;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Generates source files from AST types.  This class handles common actions
 * shared by the header and implementation generators.
 *
 * @author Tom Ball
 */
public abstract class ObjectiveCSourceFileGenerator extends AbstractSourceGenerator {

  private final GenerationUnit unit;

  /**
   * Create a new generator.
   *
   * @param unit The AST of the source to generate
   * @param emitLineDirectives if true, generate CPP line directives
   */
  protected ObjectiveCSourceFileGenerator(GenerationUnit unit, boolean emitLineDirectives) {
    super(new SourceBuilder(emitLineDirectives));
    this.unit = unit;
  }

  /**
   * Returns the suffix for files created by this generator.
   */
  protected abstract String getSuffix();

  protected String getOutputPath() {
    return getGenerationUnit().getOutputPath() + getSuffix();
  }

  protected GenerationUnit getGenerationUnit() {
    return unit;
  }

  protected CompilationUnit getUnit() {
    // TODO(mthvedt): Eliminate this method
    // when we support multiple compilation units per generation unit.
    return getGenerationUnit().getCompilationUnits().get(0);
  }

  protected void save(String path) {
    try {
      File outputDirectory = Options.getOutputDirectory();
      File outputFile = new File(outputDirectory, path);
      File dir = outputFile.getParentFile();
      if (dir != null && !dir.exists()) {
        if (!dir.mkdirs()) {
          ErrorUtil.warning("cannot create output directory: " + outputDirectory);
        }
      }
      String source = getBuilder().toString();

      // Make sure file ends with a new-line.
      if (!source.endsWith("\n")) {
        source += '\n';
      }

      Files.write(source, outputFile, Options.getCharset());
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
    } finally {
      reset();
    }
  }

  private static final Predicate<BodyDeclaration> IS_STATIC = new Predicate<BodyDeclaration>() {
    public boolean apply(BodyDeclaration decl) {
      return Modifier.isStatic(decl.getModifiers());
    }
  };

  private static final Predicate<BodyDeclaration> NOT_STATIC = Predicates.not(IS_STATIC);

  protected static final String DEPRECATED_ATTRIBUTE = "__attribute__((deprecated))";

  protected boolean isInterfaceType(AbstractTypeDeclaration node) {
    return node.getKind() == TreeNode.Kind.ANNOTATION_TYPE_DECLARATION
        || (node.getKind() == TreeNode.Kind.TYPE_DECLARATION
            && ((TypeDeclaration) node).isInterface());
  }

  protected Iterable<VariableDeclarationFragment> getStaticFieldsNeedingAccessors(
      AbstractTypeDeclaration node) {
    return TreeUtil.asFragments(Iterables.filter(getStaticFields(node), printDeclFilter));
  }

  protected Iterable<FieldDeclaration> getStaticFields(AbstractTypeDeclaration node) {
    Iterable<FieldDeclaration> fieldDecls = TreeUtil.getFieldDeclarations(node);
    // All variables declared in interface types are static.
    if (!isInterfaceType(node)) {
      fieldDecls = Iterables.filter(fieldDecls, IS_STATIC);
    }
    return fieldDecls;
  }

  protected boolean hasInitializeMethod(AbstractTypeDeclaration node) {
    return !node.getClassInitStatements().isEmpty();
  }

  protected abstract void printFunctionDeclaration(FunctionDeclaration declaration);

  protected boolean printPrivateDeclarations() {
    return false;
  }

  protected boolean shouldPrintDeclaration(BodyDeclaration decl) {
    int modifiers = decl.getModifiers();
    // Don't print declarations for any synthetic members.
    if (BindingUtil.isSynthetic(modifiers)) {
      return false;
    }
    boolean isPrivate = false;
    if (Options.hidePrivateMembers() || decl instanceof FunctionDeclaration) {
      isPrivate = Modifier.isPrivate(modifiers);
    }
    return isPrivate == printPrivateDeclarations();
  }

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

  protected void printInnerDeclarations(AbstractTypeDeclaration node) {
    printDeclarations(getInnerDeclarations(node));
  }

  protected void printOuterDeclarations(AbstractTypeDeclaration node) {
    printDeclarations(getOuterDeclarations(node));
  }

  private static final Predicate<BodyDeclaration> IS_OUTER_DECL = new Predicate<BodyDeclaration>() {
    public boolean apply(BodyDeclaration decl) {
      return decl instanceof FunctionDeclaration;
    }
  };

  private static final Predicate<BodyDeclaration> IS_INNER_DECL = new Predicate<BodyDeclaration>() {
    public boolean apply(BodyDeclaration decl) {
      switch (decl.getKind()) {
        case METHOD_DECLARATION:
        case NATIVE_DECLARATION:
          return true;
      }
      return false;
    }
  };

  private final Predicate<BodyDeclaration> printDeclFilter = new Predicate<BodyDeclaration>() {
    public boolean apply(BodyDeclaration decl) {
      return shouldPrintDeclaration(decl);
    }
  };

  protected Iterable<BodyDeclaration> getInnerDeclarations(AbstractTypeDeclaration node) {
    return Iterables.filter(Iterables.filter(
        node.getBodyDeclarations(), IS_INNER_DECL), printDeclFilter);
  }

  protected Iterable<BodyDeclaration> getOuterDeclarations(AbstractTypeDeclaration node) {
    return Iterables.filter(Iterables.filter(
        node.getBodyDeclarations(), IS_OUTER_DECL), printDeclFilter);
  }

  protected Iterable<BodyDeclaration> getInnerDefinitions(AbstractTypeDeclaration node) {
    return Iterables.filter(node.getBodyDeclarations(), IS_INNER_DECL);
  }

  protected Iterable<BodyDeclaration> getOuterDefinitions(AbstractTypeDeclaration node) {
    return Iterables.filter(node.getBodyDeclarations(), IS_OUTER_DECL);
  }

  protected Iterable<FieldDeclaration> getFieldsToDeclare(AbstractTypeDeclaration node) {
    if (isInterfaceType(node)) {
      return Collections.emptyList();
    }
    return Iterables.filter(Iterables.filter(
        TreeUtil.getFieldDeclarations(node), NOT_STATIC), printDeclFilter);
  }

  /**
   * Create an Objective-C method declaration string.
   */
  protected String methodDeclaration(MethodDeclaration m) {
    StringBuilder sb = new StringBuilder();
    IMethodBinding binding = m.getMethodBinding();
    char prefix = Modifier.isStatic(m.getModifiers()) ? '+' : '-';
    String returnType = NameTable.getObjCType(binding.getReturnType());
    String selector = NameTable.getMethodSelector(binding);
    if (m.isConstructor()) {
      returnType = "instancetype";
    } else if (selector.equals("hash")) {
      // Explicitly test hashCode() because of NSObject's hash return value.
      returnType = "NSUInteger";
    }
    sb.append(String.format("%c (%s)", prefix, returnType));

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
        IVariableBinding var = params.get(i).getVariableBinding();
        String typeName = NameTable.getSpecificObjCType(var.getType());
        sb.append(String.format("%s:(%s)%s", selParts[i], typeName, NameTable.getName(var)));
      }
    }

    return sb.toString();
  }

  /**
   * Create an Objective-C constructor from a list of annotation member
   * declarations.
   */
  protected String annotationConstructorDeclaration(ITypeBinding annotation) {
    StringBuffer sb = new StringBuffer();
    sb.append("- (instancetype)init");
    IMethodBinding[] members = BindingUtil.getSortedAnnotationMembers(annotation);
    for (int i = 0; i < members.length; i++) {
      if (i == 0) {
        sb.append("With");
      } else {
        sb.append(" with");
      }
      IMethodBinding member = members[i];
      String name = NameTable.getAnnotationPropertyName(member);
      sb.append(NameTable.capitalize(name));
      sb.append(":(");
      sb.append(NameTable.getSpecificObjCType(member.getReturnType()));
      sb.append(')');
      sb.append(name);
      sb.append("__");
    }
    return sb.toString();
  }

  /** Ignores deprecation warnings. Deprecation warnings should be visible for human authored code,
   *  not transpiled code. This method should be paired with popIgnoreDeprecatedDeclarationsPragma.
   */
  protected void pushIgnoreDeprecatedDeclarationsPragma() {
    if (Options.generateDeprecatedDeclarations()) {
      newline();
      println("#pragma clang diagnostic push");
      println("#pragma GCC diagnostic ignored \"-Wdeprecated-declarations\"");
    }
  }

  /** Restores deprecation warnings after a call to pushIgnoreDeprecatedDeclarationsPragma. */
  protected void popIgnoreDeprecatedDeclarationsPragma() {
    if (Options.generateDeprecatedDeclarations()) {
      println("\n#pragma clang diagnostic pop");
    }
  }

  protected void printDocComment(Javadoc javadoc) {
    JavadocGenerator.printDocComment(getBuilder(), javadoc);
  }

  /**
   * Prints the list of instance variables in a type.
   */
  protected void printInstanceVariables(Iterable<FieldDeclaration> fields) {
    indent();
    boolean first = true;
    for (FieldDeclaration field : fields) {
      List<VariableDeclarationFragment> vars = field.getFragments();
      assert !vars.isEmpty();
      IVariableBinding varBinding = vars.get(0).getVariableBinding();
      ITypeBinding varType = varBinding.getType();
      // Need direct access to fields possibly from inner classes that are
      // promoted to top level classes, so must make all visible fields public.
      if (first) {
        println(" @public");
        first = false;
      }
      printDocComment(field.getJavadoc());
      printIndent();
      if (BindingUtil.isWeakReference(varBinding)) {
        // We must add this even without -use-arc because the header may be
        // included by a file compiled with ARC.
        print("__weak ");
      }
      String objcType = NameTable.getSpecificObjCType(varType);
      boolean needsAsterisk = !varType.isPrimitive() && !objcType.matches("id|id<.*>|Class");
      if (needsAsterisk && objcType.endsWith(" *")) {
        // Strip pointer from type, as it will be added when appending fragment.
        // This is necessary to create "Foo *one, *two;" declarations.
        objcType = objcType.substring(0, objcType.length() - 2);
      }
      print(objcType);
      print(' ');
      for (Iterator<VariableDeclarationFragment> it = field.getFragments().iterator();
           it.hasNext(); ) {
        VariableDeclarationFragment f = it.next();
        if (needsAsterisk) {
          print('*');
        }
        String name = NameTable.getName(f.getName().getBinding());
        print(NameTable.javaFieldToObjC(name));
        if (it.hasNext()) {
          print(", ");
        }
      }
      println(";");
    }
    unindent();
  }

  protected boolean isPrivateOrSynthetic(int modifiers) {
    return Modifier.isPrivate(modifiers) || BindingUtil.isSynthetic(modifiers);
  }

  protected void printMethodDeclaration(MethodDeclaration m) {
    newline();
    printDocComment(m.getJavadoc());
    print(this.methodDeclaration(m));
    String methodName = NameTable.getMethodSelector(m.getMethodBinding());
    if (!m.isConstructor() && !BindingUtil.isSynthetic(m.getModifiers())
        && needsObjcMethodFamilyNoneAttribute(methodName)) {
      // Getting around a clang warning.
      // clang assumes that methods with names starting with new, alloc or copy
      // return objects of the same type as the receiving class, regardless of
      // the actual declared return type. This attribute tells clang to not do
      // that, please.
      // See http://clang.llvm.org/docs/AutomaticReferenceCounting.html
      // Sections 5.1 (Explicit method family control)
      // and 5.2.2 (Related result types)
      print(" OBJC_METHOD_FAMILY_NONE");
    }

    if (needsDeprecatedAttribute(m.getAnnotations())) {
      print(" " + DEPRECATED_ATTRIBUTE);
    }
    println(";");
  }

  protected boolean needsObjcMethodFamilyNoneAttribute(String name) {
    return name.startsWith("new") || name.startsWith("copy") || name.startsWith("alloc")
        || name.startsWith("init") || name.startsWith("mutableCopy");
  }

  protected boolean needsDeprecatedAttribute(List<Annotation> annotations) {
    return Options.generateDeprecatedDeclarations() && hasDeprecated(annotations);
  }

  private boolean hasDeprecated(List<Annotation> annotations) {
    for (Annotation annotation : annotations) {
      Name annotationTypeName = annotation.getTypeName();
      String expectedTypeName =
          annotationTypeName.isQualifiedName() ? "java.lang.Deprecated" : "Deprecated";
      if (expectedTypeName.equals(annotationTypeName.getFullyQualifiedName())) {
        return true;
      }
    }

    return false;
  }

  protected void printNativeDeclaration(NativeDeclaration declaration) {
    newline();
    String code = declaration.getHeaderCode();
    if (code != null) {
      print(declaration.getHeaderCode());
    }
  }

  protected void printFieldSetters(AbstractTypeDeclaration node) {
    ITypeBinding declaringType = node.getTypeBinding();
    boolean newlinePrinted = false;
    for (FieldDeclaration field : getFieldsToDeclare(node)) {
      ITypeBinding type = field.getType().getTypeBinding();
      if (type.isPrimitive()) {
        continue;
      }
      String typeStr = NameTable.getObjCType(type);
      String declaringClassName = NameTable.getFullName(declaringType);
      for (VariableDeclarationFragment var : field.getFragments()) {
        if (BindingUtil.isWeakReference(var.getVariableBinding())) {
          continue;
        }
        String fieldName = NameTable.javaFieldToObjC(NameTable.getName(var.getName().getBinding()));
        if (!newlinePrinted) {
          newlinePrinted = true;
          newline();
        }
        println(String.format("J2OBJC_FIELD_SETTER(%s, %s, %s)",
            declaringClassName, fieldName, typeStr));
      }
    }
  }

  protected String getFunctionSignature(FunctionDeclaration function) {
    StringBuilder sb = new StringBuilder();
    String returnType = NameTable.getObjCType(function.getReturnType().getTypeBinding());
    returnType += returnType.endsWith("*") ? "" : " ";
    sb.append(returnType).append(function.getName()).append('(');
    for (Iterator<SingleVariableDeclaration> iter = function.getParameters().iterator();
         iter.hasNext(); ) {
      IVariableBinding var = iter.next().getVariableBinding();
      String paramType = NameTable.getSpecificObjCType(var.getType());
      paramType += (paramType.endsWith("*") ? "" : " ");
      sb.append(paramType + NameTable.getName(var));
      if (iter.hasNext()) {
        sb.append(", ");
      }
    }
    sb.append(')');
    return sb.toString();
  }

  protected void printStaticFieldDeclarations(AbstractTypeDeclaration node) {
    for (VariableDeclarationFragment fragment : getStaticFieldsNeedingAccessors(node)) {
      printStaticFieldFullDeclaration(fragment);
    }
  }

  protected abstract void printStaticFieldDeclaration(
      VariableDeclarationFragment fragment, String baseDeclaration);

  private void printStaticFieldFullDeclaration(VariableDeclarationFragment fragment) {
    IVariableBinding var = fragment.getVariableBinding();
    String objcType = NameTable.getObjCType(var.getType());
    String typeWithSpace = objcType + (objcType.endsWith("*") ? "" : " ");
    String name = NameTable.getStaticVarName(var);
    String className = NameTable.getFullName(var.getDeclaringClass());
    boolean isFinal = Modifier.isFinal(var.getModifiers());
    boolean isPrimitive = var.getType().isPrimitive();
    newline();
    if (BindingUtil.isPrimitiveConstant(var)) {
      name = var.getName();
    } else {
      printStaticFieldDeclaration(
          fragment, String.format("%s%s_%s", typeWithSpace, className, name));
    }
    printf("J2OBJC_STATIC_FIELD_GETTER(%s, %s, %s)\n", className, name, objcType);
    if (!isFinal) {
      if (isPrimitive) {
        printf("J2OBJC_STATIC_FIELD_REF_GETTER(%s, %s, %s)\n", className, name, objcType);
      } else {
        printf("J2OBJC_STATIC_FIELD_SETTER(%s, %s, %s)\n", className, name, objcType);
      }
    }
  }

  protected void printConstantDefines(AbstractTypeDeclaration node) {
    boolean needsNewline = true;
    for (FieldDeclaration fieldDecl : getStaticFields(node)) {
      if (!shouldPrintDeclaration(fieldDecl)) {
        continue;
      }
      for (VariableDeclarationFragment fragment : fieldDecl.getFragments()) {
        IVariableBinding field = fragment.getVariableBinding();
        if (BindingUtil.isPrimitiveConstant(field)) {
          if (needsNewline) {
            needsNewline = false;
            newline();
          }
          printf("#define %s ", NameTable.getPrimitiveConstantName(field));
          Object value = field.getConstantValue();
          assert value != null;
          println(LiteralGenerator.generate(value));
        }
      }
    }
  }
}
