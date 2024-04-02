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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MultimapBuilder;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;

/**
 * Base class for generating type declarations, either public or private.
 *
 * @author Tom Ball, Keith Stanger
 */
public class TypeDeclarationGenerator extends TypeGenerator {

  /** Represents Objective-C's audited nullability regions. */
  protected enum AuditedRegion {
    BEGIN("NS_ASSUME_NONNULL_BEGIN"),
    END("NS_ASSUME_NONNULL_END");

    private final String macro;

    private AuditedRegion(String macro) {
      this.macro = macro;
    }

    public String getMacro() {
      return macro;
    }
  }

  private static final String DEPRECATED_ATTRIBUTE = "__attribute__((deprecated))";

  private final GeneratedSourceMappings generatedSourceMappings;

  protected TypeDeclarationGenerator(SourceBuilder builder, AbstractTypeDeclaration node) {
    super(builder, node);
    generatedSourceMappings = new GeneratedSourceMappings();
  }

  protected TypeDeclarationGenerator(
      SourceBuilder builder,
      AbstractTypeDeclaration node,
      GeneratedSourceMappings generatedSourceMappings) {
    super(builder, node);
    this.generatedSourceMappings = generatedSourceMappings;
  }

  public static void generate(
      SourceBuilder builder,
      AbstractTypeDeclaration node,
      GeneratedSourceMappings generatedSourceMappings) {
    new TypeDeclarationGenerator(builder, node, generatedSourceMappings).generate();
  }

  public static void generate(SourceBuilder builder, AbstractTypeDeclaration node) {
    new TypeDeclarationGenerator(builder, node).generate();
  }

  protected boolean printPrivateDeclarations() {
    return false;
  }

  @Override
  protected boolean shouldPrintDeclaration(BodyDeclaration decl) {
    if (decl instanceof MethodDeclaration && !((MethodDeclaration) decl).hasDeclaration()) {
      return false;
    }
    return decl.hasPrivateDeclaration() == printPrivateDeclarations();
  }

  private void generate() {
    // If the type is private, then generate nothing in the header. The initial
    // declaration will go in the implementation file instead.
    if (!typeNode.hasPrivateDeclaration()) {
      generateInitialDeclaration();
    }
  }

  protected void generateInitialDeclaration() {
    if (typeNode.isDeadClass()) {
      printStaticFieldDeclarations();
      return;
    }
    printNativeEnum();

    printTypeDocumentation();
    printNonnullAuditedRegion(AuditedRegion.BEGIN);

    printInterfaceType();
    printImplementedProtocols();
    if (!typeElement.getKind().isInterface()) {
      printInstanceVariables();
    } else {
      newline();
    }
    if (!typeElement.getKind().isInterface()) {
      printProperties();
      printStaticAccessors();
    }
    printInnerDeclarations();
    println("\n@end");

    if (ElementUtil.isPackageInfo(typeElement)) {
      printOuterDeclarations();
      printNonnullAuditedRegion(AuditedRegion.END);
      return;
    }
    printCompanionClassDeclaration();
    printStaticInitFunction();
    printEnumConstants();
    printFieldSetters();
    printStaticFieldDeclarations();
    printOuterDeclarations();
    printTypeLiteralDeclaration();
    printBoxedOperators();

    printUnprefixedAlias();

    newline();
    printNonnullAuditedRegion(AuditedRegion.END);
  }

  private void printNativeEnum() {
    if (!(typeNode instanceof EnumDeclaration)) {
      return;
    }

    List<EnumConstantDeclaration> constants = ((EnumDeclaration) typeNode).getEnumConstants();
    String nativeName = NameTable.getNativeEnumName(typeName);
    String ordinalName = NameTable.getNativeOrdinalPreprocessorName(typeName);

    // C doesn't allow empty enum declarations.  Java does, so we skip the
    // C enum declaration and generate the type declaration.
    if (!constants.isEmpty()) {
      newline();
      printf("typedef NS_ENUM(jint, %s) {\n", nativeName);

      // Print C enum typedef.
      indent();
      int ordinal = 0;
      for (EnumConstantDeclaration constant : constants) {
        printIndent();
        String caseName = nameTable.getVariableBaseName(constant.getVariableElement());
        if (options.swiftEnums()) {
          printf(
              "%s_%s NS_SWIFT_NAME(%s) = %d,\n",
              nativeName, caseName, NameTable.getSwiftEnumName(caseName), ordinal++);
        } else {
          printf("%s_%s = %d,\n", nativeName, caseName, ordinal++);
        }
      }
      unindent();
      print("}");
      String swiftName = nameTable.getSwiftClassNameFromAnnotation(typeElement);
      if (swiftName != null) {
        printf(" NS_SWIFT_NAME(%s)", swiftName);
      }
      print(";\n");
      // Use different types for transpiled Java ordinals (which expects ordinals to be jint) and
      // native code using the enum (where stricter ordinal types help clang warnings).
      printf(
          "#if J2OBJC_IMPORTED_BY_JAVA_IMPLEMENTATION\n"
              + "#define %s jint\n"
              + "#else\n"
              + "#define %s %s\n"
              + "#endif\n\n",
          ordinalName, ordinalName, nativeName);
    } else {
      printf("#define %s jint\n", ordinalName);
    }
  }

  private void printTypeDocumentation() {
    newline();
    JavadocGenerator.printDocComment(getBuilder(), typeNode.getJavadoc());
  }

  private String getSuperTypeName() {
    TypeElement supertype = TranslationUtil.getSuperType(typeNode);
    if (supertype != null) {
      return nameTable.getFullName(supertype);
    }
    return "NSObject";
  }

  private List<String> getInterfaceNames() {
    if (ElementUtil.isAnnotationType(typeElement)) {
      return Lists.newArrayList("JavaLangAnnotationAnnotation");
    }
    List<String> names = Lists.newArrayList();
    for (TypeElement intrface : TranslationUtil.getInterfaceTypes(typeNode)) {
      names.add(nameTable.getFullName(intrface));
    }
    if (ElementUtil.getQualifiedName(typeElement).equals("java.lang.Enum")) {
      names.remove("NSCopying");
      names.add(0, "NSCopying");
    } else if (isInterfaceType()) {
      names.add("JavaObject");
    }
    return names;
  }

  protected void printInterfaceGenerics() {
    if (generateObjectiveCGenerics(typeElement.asType())) {
      List<String> genericNames = nameTable.getClassObjCGenericTypeNames(typeElement.asType());
      if (!genericNames.isEmpty()) {
        printf("<%s>", String.join(", ", genericNames));
      }
    }
  }

  private void printInterfaceType() {
    if (!typeNode.hasPrivateDeclaration()) {
      // Swift Names only need to be on public types because private declarations are private
      printSwiftName();
    }
    if (needsDeprecatedAttribute(typeNode.getAnnotations())) {
      println(DEPRECATED_ATTRIBUTE);
    }
    if (typeElement.getKind().isInterface()) {
      printf("@protocol %s", typeName);
    } else {
      printf("@interface %s", typeName);
      printInterfaceGenerics();
      printf(" : %s", getSuperTypeName());
    }
  }

  private void printImplementedProtocols() {
    List<String> interfaces = getInterfaceNames();
    if (!interfaces.isEmpty()) {
      print(" < ");
      boolean isFirst = true;
      for (String name : interfaces) {
        if (!isFirst) {
          print(", ");
        }
        isFirst = false;
        print(name);
      }
      print(" >");
    }
  }

  private void printSwiftName() {
    String swiftName = nameTable.getSwiftClassNameFromAnnotation(typeElement);

    if (swiftName != null) {
      if ((typeNode instanceof EnumDeclaration)) {
        swiftName = swiftName + "." + swiftName + "Class";
      }
      printf(" NS_SWIFT_NAME(%s)\n", swiftName);
    }
  }

  protected void printStaticInterfaceMethods() {
    for (BodyDeclaration declaration : getInnerDeclarations()) {
      if (declaration.getKind().equals(TreeNode.Kind.METHOD_DECLARATION)) {
        printMethodDeclaration((MethodDeclaration) declaration, true);
      }
    }
  }

  /**
   * Prints the list of static variable and/or enum constant accessor methods.
   */
  protected void printStaticAccessors() {
    if (options.staticAccessorMethods() && !options.classProperties()) {
      for (VariableDeclarationFragment fragment : getStaticFields()) {
        VariableElement var = fragment.getVariableElement();
        TypeMirror type = var.asType();
        String accessorName = nameTable.getStaticAccessorName(var);
        String objcType = paddedType(nameTable.getObjCTypeDeclaration(type), var).trim();
        TypeElement declaringClass = ElementUtil.getDeclaringClass(var);
        String baseName = nameTable.getVariableBaseName(var);
        ExecutableElement getter =
            ElementUtil.findGetterMethod(baseName, type, declaringClass, /* isStatic = */ true);
        if (getter == null) {
          printf("\n+ (%s)%s;\n", objcType, accessorName);
        }
        ExecutableElement setter =
            ElementUtil.findSetterMethod(baseName, type, declaringClass, /* isStatic = */ true);
        if (setter == null && !ElementUtil.isFinal(var)) {
          printf("\n+ (void)set%s:(%s)value;\n", NameTable.capitalize(accessorName), objcType);
        }
      }
      if (typeNode instanceof EnumDeclaration) {
        for (EnumConstantDeclaration constant : ((EnumDeclaration) typeNode).getEnumConstants()) {
          String accessorName = nameTable.getStaticAccessorName(constant.getVariableElement());
          String nullabilitySpecifier = "";
          TypeElement element = typeNode.getTypeElement();
          if (shouldAddNullableAnnotation(element)) {
            nullabilitySpecifier = " _Nullable";
          } else if (options.nullability() && !nullMarked) {
            // Only add a nonnull annotation if null marked is not enabled as it is implied
            // when annotated regions are in use. This code block preserves legacy behavior.
            nullabilitySpecifier = " _Nonnull";
          }
          printf("\n+ (%s *%s)%s;\n", typeName, nullabilitySpecifier, accessorName);
        }
      }
    }
  }

  /**
   * Prints the list of instance variables in a type.
   */
  protected void printInstanceVariables() {
    Iterable<VariableDeclarationFragment> fields = getInstanceFields();
    if (Iterables.isEmpty(fields)) {
      newline();
      return;
    }
    // Need direct access to fields possibly from inner classes that are
    // promoted to top level classes, so must make all visible fields public.
    println(" {");
    println(" @public");
    indent();
    FieldDeclaration lastDeclaration = null;
    boolean needsAsterisk = false;
    for (VariableDeclarationFragment fragment : fields) {
      VariableElement varElement = fragment.getVariableElement();
      FieldDeclaration declaration = (FieldDeclaration) fragment.getParent();
      if (declaration != lastDeclaration) {
        if (lastDeclaration != null) {
          println(";");
        }
        lastDeclaration = declaration;
        JavadocGenerator.printDocComment(getBuilder(), declaration.getJavadoc());
        printIndent();
        if (!ElementUtil.isVolatile(varElement)) {
          if (ElementUtil.isWeakReference(varElement)) {
            print("WEAK_ ");
          }
        }
        String objcType = getDeclarationType(varElement);
        needsAsterisk = objcType.endsWith("*");
        if (needsAsterisk) {
          // Strip pointer from type, as it will be added when appending fragment.
          // This is necessary to create "Foo *one, *two;" declarations.
          objcType = objcType.substring(0, objcType.length() - 2);
        }
        print(objcType);
        print(' ');
      } else {
        print(", ");
      }
      if (needsAsterisk) {
        print('*');
        if (shouldAddNullableAnnotation(varElement)) {
          print("_Nullable ");
        }
      }
      print(nameTable.getVariableShortName(varElement));
    }
    println(";");
    unindent();
    println("}");
  }

  protected void printProperties() {
    Iterable<VariableDeclarationFragment> fields = getAllFields();
    for (VariableDeclarationFragment fragment : fields) {
      PropertyGenerator.generate(fragment, options, nameTable, typeUtil, parametersNonnullByDefault)
          .ifPresent(this::println);
    }

    if (options.classProperties() && typeNode instanceof EnumDeclaration) {
      for (EnumConstantDeclaration constant : ((EnumDeclaration) typeNode).getEnumConstants()) {
        String accessorName = nameTable.getStaticAccessorName(constant.getVariableElement());
        print("\n@property (readonly, class");
        if (options.nullability()) {
          print(", nonnull");
        }
        String swiftName = accessorName;
        if (options.swiftEnums()) {
          swiftName = NameTable.getSwiftEnumName(accessorName);
        }
        printf(
            ") %s *%s NS_SWIFT_NAME(%s);",
            typeName, accessorName, swiftName);
      }
    }
  }

  protected void printCompanionClassDeclaration() {
    if (!typeElement.getKind().isInterface() || !needsCompanionClass()
        || printPrivateDeclarations() == needsPublicCompanionClass()) {
      return;
    }
    printf("\n@interface %s : NSObject", typeName);
    if (ElementUtil.isGeneratedAnnotation(typeElement)) {
      // Print annotation implementation interface.
      printf(" < %s >", typeName);
    }
    printInstanceVariables();
    printProperties();
    printStaticInterfaceMethods();
    printStaticAccessors();
    println("\n@end");
  }

  private void printStaticInitFunction() {
    if (hasInitializeMethod()) {
      printf("\nJ2OBJC_STATIC_INIT(%s)\n", typeName);
    } else {
      printf("\nJ2OBJC_EMPTY_STATIC_INIT(%s)\n", typeName);
    }
  }

  private void printEnumConstants() {
    if (typeNode instanceof EnumDeclaration) {
      newline();
      println("/*! INTERNAL ONLY - Use enum accessors declared below. */");

      // If the generated source is annotated with nonnull nullability regions,
      // ensure this type is explicitly marked as nonnull. Failing to do so leads
      // to the following error: `inferring '_Nonnull' for pointer type within array
      // is deprecated [-Werror,-Wnullability-inferred-on-nested-type]`.
      String arrayTypeNamePrefix = nullMarked ? "_Nonnull " : "";
      String arrayTypeName = arrayTypeNamePrefix + typeName;
      printf("FOUNDATION_EXPORT %s *%s_values_[];\n", typeName, arrayTypeName);
      for (EnumConstantDeclaration constant : ((EnumDeclaration) typeNode).getEnumConstants()) {
        String varName = nameTable.getVariableBaseName(constant.getVariableElement());
        newline();
        JavadocGenerator.printDocComment(getBuilder(), constant.getJavadoc());
        printf("inline %s *%s_get_%s(void);\n", typeName, typeName, varName);
        printf("J2OBJC_ENUM_CONSTANT(%s, %s)\n", typeName, varName);
      }
    }
  }

  private static final Predicate<VariableDeclarationFragment> NEEDS_SETTER =
      new Predicate<VariableDeclarationFragment>() {
    @Override
    public boolean apply(VariableDeclarationFragment fragment) {
      VariableElement var = fragment.getVariableElement();
      if (ElementUtil.isRetainedWithField(var)) {
        assert !ElementUtil.isPublic(var) : "@RetainedWith fields cannot be public.";
        return false;
      }
      return !var.asType().getKind().isPrimitive() && !ElementUtil.isSynthetic(var)
          && !ElementUtil.isWeakReference(var);
    }
  };

  protected void printFieldSetters() {
    Iterable<VariableDeclarationFragment> fields =
        Iterables.filter(getInstanceFields(), NEEDS_SETTER);
    if (Iterables.isEmpty(fields)) {
      return;
    }
    newline();
    for (VariableDeclarationFragment fragment : fields) {
      VariableElement var = fragment.getVariableElement();
      String typeStr = nameTable.getObjCTypeDeclaration(var.asType());
      if (typeStr.contains(",")) {
        typeStr = "J2OBJC_ARG(" + typeStr + ')';
      }
      String objcTypePadded = paddedType(typeStr, var).trim();
      String fieldName = nameTable.getVariableShortName(var);
      String isVolatile = ElementUtil.isVolatile(var) ? "_VOLATILE" : "";
      println(
          UnicodeUtils.format(
              "J2OBJC%s_FIELD_SETTER(%s, %s, %s)",
              isVolatile, typeName, fieldName, objcTypePadded));
    }
  }

  protected void printStaticFieldDeclarations() {
    for (VariableDeclarationFragment fragment : getStaticFields()) {
      if (typeNode.isDeadClass()) {
        printDeadClassConstant(fragment);
      } else {
        printStaticFieldFullDeclaration(fragment);
      }
    }
  }

  // Overridden in TypePrivateDeclarationGenerator
  protected void printStaticFieldDeclaration(
      VariableDeclarationFragment fragment, String baseDeclaration) {
    println("/*! INTERNAL ONLY - Use accessor function from above. */");
    println("FOUNDATION_EXPORT " + baseDeclaration + ";");
  }

  private void printStaticFieldFullDeclaration(VariableDeclarationFragment fragment) {
    VariableElement var = fragment.getVariableElement();
    boolean isVolatile = ElementUtil.isVolatile(var);
    String objcType = nameTable.getObjCTypeDeclaration(var.asType());
    String objcTypePadded = paddedType(objcType, var);
    String declType = paddedType(getDeclarationType(var), var);
    String name = nameTable.getVariableShortName(var);
    boolean isFinal = ElementUtil.isFinal(var);
    boolean isPrimitive = var.asType().getKind().isPrimitive();
    boolean isConstant = ElementUtil.isPrimitiveConstant(var);
    String qualifiers = isConstant ? "_CONSTANT"
        : (isPrimitive ? "_PRIMITIVE" : "_OBJ") + (isVolatile ? "_VOLATILE" : "")
        + (isFinal ? "_FINAL" : "");
    newline();
    FieldDeclaration decl = (FieldDeclaration) fragment.getParent();
    JavadocGenerator.printDocComment(getBuilder(), decl.getJavadoc());
    printf("inline %s%s_get_%s(void);\n", objcTypePadded, typeName, name);
    if (!isFinal) {
      printf("inline %s%s_set_%s(%svalue);\n", objcTypePadded, typeName, name, objcTypePadded);
      if (isPrimitive && !isVolatile) {
        printf("inline %s *%s_getRef_%s(void);\n", objcType, typeName, name);
      }
    }
    if (isConstant) {
      Object value = var.getConstantValue();
      assert value != null;
      printf("#define %s_%s %s\n", typeName, name, LiteralGenerator.generate(value));
    } else {
      printStaticFieldDeclaration(
          fragment, UnicodeUtils.format("%s%s_%s", declType, typeName, name));
    }
    printf(
        "J2OBJC_STATIC_FIELD%s(%s, %s, %s)\n", qualifiers, typeName, name, objcTypePadded.trim());
  }

  // Overridden in TypePrivateDeclarationGenerator
  protected void printDeadClassConstant(VariableDeclarationFragment fragment) {
    VariableElement var = fragment.getVariableElement();
    Object value = var.getConstantValue();
    assert value != null;
    String declType = paddedType(getDeclarationType(var), var);
    String name = nameTable.getVariableShortName(var);
    if (ElementUtil.isPrimitiveConstant(var)) {
      printf("#define %s_%s %s\n", typeName, name, LiteralGenerator.generate(value));
    } else {
      println("FOUNDATION_EXPORT "
          + UnicodeUtils.format("%s%s_%s", declType, typeName, name) + ";");
    }
  }

  private void printTypeLiteralDeclaration() {
    if (needsTypeLiteral()) {
      newline();
      printf("J2OBJC_TYPE_LITERAL_HEADER(%s)\n", typeName);
    }
  }

  private void printBoxedOperators() {
    PrimitiveType primitiveType = env.typeUtil().unboxedType(typeElement.asType());
    if (primitiveType == null) {
      return;
    }
    char binaryName = env.typeUtil().getSignatureName(primitiveType).charAt(0);
    String primitiveName = TypeUtil.getName(primitiveType);
    String capName = NameTable.capitalize(primitiveName);
    String primitiveTypeName = NameTable.getPrimitiveObjCType(primitiveType);
    String valueMethod = primitiveName + "Value";
    if (primitiveName.equals("long")) {
      valueMethod = "longLongValue";
    } else if (primitiveName.equals("byte")) {
      valueMethod = "charValue";
    }
    newline();
    printf("BOXED_INC_AND_DEC(%s, %s, %s)\n", capName, valueMethod, typeName);

    if ("DFIJ".indexOf(binaryName) >= 0) {
      printf("BOXED_COMPOUND_ASSIGN_ARITHMETIC(%s, %s, %s, %s)\n",
          capName, valueMethod, primitiveTypeName, typeName);
    }
    if ("IJ".indexOf(binaryName) >= 0) {
      printf("BOXED_COMPOUND_ASSIGN_MOD(%s, %s, %s, %s)\n",
          capName, valueMethod, primitiveTypeName, typeName);
    }
    if ("DF".indexOf(binaryName) >= 0) {
      printf("BOXED_COMPOUND_ASSIGN_FPMOD(%s, %s, %s, %s)\n",
          capName, valueMethod, primitiveTypeName, typeName);
    }
    if ("IJ".indexOf(binaryName) >= 0) {
      printf("BOXED_COMPOUND_ASSIGN_BITWISE(%s, %s, %s, %s)\n",
          capName, valueMethod, primitiveTypeName, typeName);
    }
    if ("I".indexOf(binaryName) >= 0) {
      printf("BOXED_SHIFT_ASSIGN_32(%s, %s, %s, %s)\n",
          capName, valueMethod, primitiveTypeName, typeName);
    }
    if ("J".indexOf(binaryName) >= 0) {
      printf("BOXED_SHIFT_ASSIGN_64(%s, %s, %s, %s)\n",
          capName, valueMethod, primitiveTypeName, typeName);
    }
  }

  private void printUnprefixedAlias() {
    if (ElementUtil.isTopLevel(typeElement)) {
      String unprefixedName =
          NameTable.camelCaseQualifiedName(ElementUtil.getQualifiedName(typeElement));
      if (!unprefixedName.equals(typeName)) {
        if (typeElement.getKind().isInterface()) {
          // Protocols can't be used in typedefs.
          printf("\n#define %s %s\n", unprefixedName, typeName);
        } else {
          printf("\n@compatibility_alias %s %s;\n", unprefixedName, typeName);
        }
      }
    }
  }

  private boolean canPrintPseudoProperty(MethodDeclaration m) {
    if (!m.isPseudoProperty()) {
      return false;
    }

    ExecutableElement methodElement = m.getExecutableElement();
    String methodName = nameTable.getMethodSelector(methodElement);
    String propertyName = NameTable.lowercaseFirst(methodName.replaceFirst("get", ""));

    TypeElement declaringClass = ElementUtil.getDeclaringClass(methodElement);
    if (NameTable.isReservedName(propertyName)) {
      return false;
    }
    // Check if there is a existing property with the same name
    if (ElementUtil.findField(declaringClass, propertyName) != null) {
      return false;
    }

    return true;
  }

  private void printPseudoProperty(MethodDeclaration m) {
    ExecutableElement methodElement = m.getExecutableElement();
    String methodName = nameTable.getMethodSelector(methodElement);
    String propertyName = NameTable.lowercaseFirst(methodName.replaceFirst("get", ""));

    TypeElement declaringClass = ElementUtil.getDeclaringClass(methodElement);
    if (NameTable.isReservedName(propertyName)) {
      return;
    }
    // Check if there is a existing property with the same name
    if (ElementUtil.findField(declaringClass, propertyName) != null) {
      return;
    }

    TypeMirror returnType = m.getReturnTypeMirror();
    ExecutableElement setter =
        ElementUtil.findSetterMethod(propertyName, returnType, declaringClass, false);

    newline();
    printf(
        "@property (%snonatomic, %s, %s%s) %s %s;",
        ElementUtil.isStatic(methodElement) ? "class, " : "",
        "getter=" + methodName,
        setter != null ? "setter=" + nameTable.getMethodSelector(setter) : "readonly",
        shouldAddNullableAnnotation(methodElement) ? ", nullable" : "",
        getReturnType(m, true), // Generics allowed in headers.
        propertyName);
    newline();
  }

  /**
   * Emit method declaration.
   *
   * @param m The method.
   * @param isCompanionClass If true, emit only if m is a static interface method.
   */
  private void printMethodDeclaration(MethodDeclaration m, boolean isCompanionClass) {
    ExecutableElement methodElement = m.getExecutableElement();
    TypeElement typeElement = ElementUtil.getDeclaringClass(methodElement);

    if (typeElement.getKind().isInterface()) {
      // isCompanion and isStatic must be both false (i.e. this prints a non-static method decl
      // in @protocol) or must both be true (i.e. this prints a static method decl in the
      // companion class' @interface).
      if (isCompanionClass != ElementUtil.isStatic(methodElement)) {
        return;
      }
    }

    newline();
    JavadocGenerator.printDocComment(getBuilder(), m.getJavadoc());

    if (canPrintPseudoProperty(m)) {
      printPseudoProperty(m);
      return;
    }

    // Method declarations allow generics.
    String methodSignature = getMethodSignature(m, true);

    // In order to properly map the method name from the entire signature, we must isolate it from
    // associated type and parameter declarations.  The method name is guaranteed to be between the
    // first closing parenthesis and first colon (for methods with arguments), or the entirety of
    // the declaration after the first closing parenthesis (for methods with no arguments).
    int identifierStartIndex = methodSignature.indexOf(')') + 1;
    int identifierEndIndex =
        methodSignature.contains(":") ? methodSignature.indexOf(':') : methodSignature.length();
    generatedSourceMappings.addMethodMapping(
        m /* methodDeclaration */,
        getBuilder().length() + identifierStartIndex /* targetBegin */,
        identifierEndIndex - identifierStartIndex /* length */);

    print(methodSignature);

    String methodName = nameTable.getMethodSelector(methodElement);
    if (!m.isConstructor() && NameTable.needsObjcMethodFamilyNoneAttribute(methodName)) {
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

    String swiftName = nameTable.getSwiftMethodNameFromAnnotation(methodElement);
    if (swiftName != null) {
      print(" NS_SWIFT_NAME(" + swiftName + ")");
    }

    if (needsDeprecatedAttribute(m.getAnnotations())) {
      print(" " + DEPRECATED_ATTRIBUTE);
    }
    if (m.isUnavailable()) {
      print(" NS_UNAVAILABLE");
    }
    println(";");
  }

  @Override
  protected void printMethodDeclaration(MethodDeclaration m) {
    printMethodDeclaration(m, false);
  }

  private boolean needsDeprecatedAttribute(List<Annotation> annotations) {
    return options.generateDeprecatedDeclarations() && hasDeprecated(annotations);
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

  @Override
  protected void printNativeDeclaration(NativeDeclaration declaration) {
    String code = declaration.getHeaderCode();
    if (code != null) {
      newline();
      print(declaration.getHeaderCode());
    }
  }

  @Override
  protected void printFunctionDeclaration(FunctionDeclaration function) {
    print("\nFOUNDATION_EXPORT " + getFunctionSignature(function, true));
    if (function.returnsRetained()) {
      print(" NS_RETURNS_RETAINED");
    }
    println(";");
  }

  protected void printNonnullAuditedRegion(AuditedRegion state) {
    if (nullMarked) {
      println(state.getMacro());
    }
  }

  /**
   * Defines the categories for grouping declarations in the header. The categories will be emitted
   * in the header in the same order that they are declared here.
   */
  private enum DeclarationCategory {
    PUBLIC("#pragma mark Public"),
    PROTECTED("#pragma mark Protected"),
    PACKAGE_PRIVATE("#pragma mark Package-Private"),
    PRIVATE("#pragma mark Private"),
    UNAVAILABLE("// Disallowed inherited constructors, do not use.");

    private final String header;

    DeclarationCategory(String header) {
      this.header = header;
    }

    private static DeclarationCategory categorize(BodyDeclaration decl) {
      if (decl instanceof MethodDeclaration && ((MethodDeclaration) decl).isUnavailable()) {
        return UNAVAILABLE;
      }
      int mods = decl.getModifiers();
      if ((mods & Modifier.PUBLIC) > 0) {
        return PUBLIC;
      } else if ((mods & Modifier.PROTECTED) > 0) {
        return PROTECTED;
      } else if ((mods & Modifier.PRIVATE) > 0) {
        return PRIVATE;
      }
      return PACKAGE_PRIVATE;
    }
  }

  /**
   * Print method declarations with #pragma mark lines documenting their scope.
   */
  @Override
  protected void printInnerDeclarations() {
    // Everything is public in interfaces.
    if (isInterfaceType() || typeNode.hasPrivateDeclaration()) {
      super.printInnerDeclarations();
      return;
    }

    ListMultimap<DeclarationCategory, BodyDeclaration> categorizedDecls =
        MultimapBuilder.hashKeys().arrayListValues().build();
    for (BodyDeclaration innerDecl : getInnerDeclarations()) {
      categorizedDecls.put(DeclarationCategory.categorize(innerDecl), innerDecl);
    }
    // Emit the categorized declarations using the declaration order of the category values.
    for (DeclarationCategory category : DeclarationCategory.values()) {
      List<BodyDeclaration> declarations = categorizedDecls.get(category);
      if (declarations.isEmpty()) {
        continue;
      }
      // Extract MethodDeclaration nodes so that they can be sorted.
      List<MethodDeclaration> methods = Lists.newArrayList();
      for (Iterator<BodyDeclaration> iter = declarations.iterator(); iter.hasNext(); ) {
        BodyDeclaration decl = iter.next();
        if (decl instanceof MethodDeclaration) {
          methods.add((MethodDeclaration) decl);
          iter.remove();
        }
      }
      Collections.sort(methods, METHOD_DECL_ORDER);
      newline();
      println(category.header);
      printDeclarations(methods);
      printDeclarations(declarations);
    }
  }

  /**
   * Returns an Objective-C nullability attribute string if there is a matching JSR305 annotation,
   * or an empty string.
   */
  @Override
  protected String nullability(Element element) {
    if (options.nullability()) {
      if (ElementUtil.hasNullableAnnotation(element)) {
        return " _Nullable";
      }
      if (ElementUtil.isNonnull(element, parametersNonnullByDefault)) {
        return " _Nonnull";
      }
    }
    return "";
  }

  /**
   * Method comparator, suitable for documentation and code-completion lists.
   *
   * Sort ordering: constructors first, then alphabetical by name. If they have the
   * same name, then compare the first parameter's simple type name, then the second, etc.
   */
  @VisibleForTesting
  static final Comparator<MethodDeclaration> METHOD_DECL_ORDER =
      new Comparator<MethodDeclaration>() {
    @Override
    public int compare(MethodDeclaration m1, MethodDeclaration m2) {
      if (m1.isConstructor() && !m2.isConstructor()) {
        return -1;
      }
      if (!m1.isConstructor() && m2.isConstructor()) {
        return 1;
      }
      String m1Name = ElementUtil.getName(m1.getExecutableElement());
      String m2Name = ElementUtil.getName(m2.getExecutableElement());
      if (!m1Name.equals(m2Name)) {
        return m1Name.compareToIgnoreCase(m2Name);
      }
      int nParams = m1.getParameters().size();
      int nOtherParams = m2.getParameters().size();
      int max = Math.min(nParams, nOtherParams);
      for (int i = 0; i < max; i++) {
        String paramType = TypeUtil.getName(m1.getParameter(i).getType().getTypeMirror());
        String otherParamType = TypeUtil.getName(m2.getParameter(i).getType().getTypeMirror());
        if (!paramType.equals(otherParamType)) {
          return paramType.compareToIgnoreCase(otherParamType);
        }
      }
      return nParams - nOtherParams;
    }
  };
}
