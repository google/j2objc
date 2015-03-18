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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.Javadoc;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.HeaderImportCollector;
import com.google.devtools.j2objc.types.Import;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.*;

/**
 * Generates Objective-C header files from compilation units.
 *
 * @author Tom Ball
 */
public class ObjectiveCHeaderGenerator extends ObjectiveCSourceFileGenerator {

  /**
   * Generate an Objective-C header file for each type declared in the given {@link GenerationUnit}.
   */
  public static void generate(GenerationUnit unit) {
    new ObjectiveCHeaderGenerator(unit).generate();
  }

  protected ObjectiveCHeaderGenerator(GenerationUnit unit) {
    super(unit, false);
  }

  @Override
  protected String getSuffix() {
    return ".h";
  }

  public void generate() {
    println(J2ObjC.getFileHeader(getGenerationUnit().getSourceName()));

    generateFileHeader();

    Map<ITypeBinding, AbstractTypeDeclaration> declaredTypes =
        new HashMap<ITypeBinding, AbstractTypeDeclaration>();
    Map<String, ITypeBinding> declaredTypeNames = new HashMap<String, ITypeBinding>();
    Map<AbstractTypeDeclaration, CompilationUnit> decls =
        new LinkedHashMap<AbstractTypeDeclaration, CompilationUnit>();
    Set<PackageDeclaration> packagesToDoc = new LinkedHashSet<PackageDeclaration>();

    // First, gather everything we need to generate.
    // We do this first because we'll be reordering it later.
    for (CompilationUnit unit: getGenerationUnit().getCompilationUnits()) {
      unit.setGenerationContext();

      // It would be nice if we could put the PackageDeclarations and AbstractTypeDeclarations
      // in the same list of 'things to generate'.
      // TODO(mthvedt): Puzzle--figure out a way to do that in Java's type system
      // that is worth the effort.
      PackageDeclaration pkg = unit.getPackage();
      if (pkg.getJavadoc() != null && Options.docCommentsEnabled()) {
        packagesToDoc.add(pkg);
      }

      for (AbstractTypeDeclaration type : unit.getTypes()) {
        decls.put(type, unit);
        declaredTypes.put(type.getTypeBinding(), type);
        declaredTypeNames.put(NameTable.getFullName(type.getTypeBinding()), type.getTypeBinding());
      }
    }

    // We order the type declarations so that the inheritance tree appears in the correct order.
    // The ordering is minimal; a type is reordered only if a subtype is immediately following.
    ArrayList<ITypeBinding> orderedDeclarationBindings = new ArrayList<ITypeBinding>();
    for (Map.Entry<AbstractTypeDeclaration, CompilationUnit> e: decls.entrySet()) {
      e.getValue().setGenerationContext();
      orderSuperinterfaces(
          e.getKey().getTypeBinding(), orderedDeclarationBindings, declaredTypeNames);
    }

    Set<AbstractTypeDeclaration> seenDecls = new HashSet<AbstractTypeDeclaration>();
    for (ITypeBinding declBinding: orderedDeclarationBindings) {
      AbstractTypeDeclaration decl = declaredTypes.get(declBinding);
      CompilationUnit unit = decls.get(decl);
      if (!seenDecls.add(decl)) {
        continue;
      }

      unit.setGenerationContext();

      // Print package docs before the first type in the package. (See above comments and TODO.)
      if (Options.docCommentsEnabled() && packagesToDoc.contains(unit.getPackage())) {
        newline();
        printDocComment(unit.getPackage().getJavadoc());
        packagesToDoc.remove(unit.getPackage());
      }

      generateType(decl);
    }

    for (PackageDeclaration pkg : packagesToDoc) {
      newline();
      printDocComment(pkg.getJavadoc());
    }

    generateFileFooter();
    save(getOutputPath());
  }

  private void orderSuperinterfaces(ITypeBinding type, List<ITypeBinding> sortedDecls,
      Map<String, ITypeBinding> declaredTypeNames) {
    // In Objective-C, you can't declare a protocol or interface
    // forward of its implementing interfaces.
    if (!type.isAnnotation()) {
      // Annotations don't have overridable supertypes in generated Objective-C code
      ITypeBinding superBinding = type.getSuperclass();
      if (superBinding != null) {
        // The map lookup ensures we get the correct ITypeBinding corresponding to a given
        // CompilationUnit. The Eclipse parser may generate alternate
        // definitions of this ITypeBinding that aren't equal to the one we want.
        superBinding = declaredTypeNames.get(NameTable.getFullName(superBinding));
        if (superBinding != null) {
          orderSuperinterfaces(superBinding, sortedDecls, declaredTypeNames);
        }
      }

      for (ITypeBinding superinterface : type.getInterfaces()) {
        superinterface = declaredTypeNames.get(NameTable.getFullName(superinterface));
        if (superinterface != null) {
          orderSuperinterfaces(superinterface, sortedDecls, declaredTypeNames);
        }
      }
    }

    sortedDecls.add(type);
  }

  protected void generateType(AbstractTypeDeclaration node) {
    ITypeBinding binding = node.getTypeBinding();

    printConstantDefines(node);

    generateSpecificType(node);

    printFieldSetters(node);
    printStaticFieldDeclarations(node);
    printOuterDeclarations(node);
    printTypeLiteralDeclaration(node);
    printIncrementAndDecrementFunctions(binding);

    printUnprefixedAlias(binding);
  }

  private void generateSpecificType(AbstractTypeDeclaration node) {
    switch (node.getKind()) {
      case ANNOTATION_TYPE_DECLARATION:
        generateAnnotationType((AnnotationTypeDeclaration) node);
        break;
      case ENUM_DECLARATION:
        generateEnumType((EnumDeclaration) node);
        break;
      case TYPE_DECLARATION:
        if (((TypeDeclaration) node).isInterface()) {
          generateInterfaceType((TypeDeclaration) node);
        } else {
          generateClassType((TypeDeclaration) node);
        }
    }
  }

  private String getSuperTypeName(TypeDeclaration node) {
    Type superType = node.getSuperclassType();
    if (superType == null) {
      return "NSObject";
    }
    return NameTable.getFullName(superType.getTypeBinding());
  }

  private void printTypeDocumentation(AbstractTypeDeclaration node) {
    newline();
    printDocComment(node.getJavadoc());
    if (needsDeprecatedAttribute(node.getAnnotations())) {
      println(DEPRECATED_ATTRIBUTE);
    }
  }

  private void printImplementedProtocols(Iterable<String> interfaces) {
    if (!Iterables.isEmpty(interfaces)) {
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

  private List<String> getInterfaceNames(ITypeBinding type) {
    List<String> names = Lists.newArrayList();
    for (ITypeBinding intrface : type.getInterfaces()) {
      names.add(NameTable.getFullName(intrface));
    }
    return names;
  }

  private void generateClassType(TypeDeclaration node) {
    ITypeBinding binding = node.getTypeBinding();

    printTypeDocumentation(node);
    printf("@interface %s : %s", NameTable.getFullName(binding), getSuperTypeName(node));
    printImplementedProtocols(getInterfaceNames(binding));
    println(" {");
    printInstanceVariables(getFieldsToDeclare(node));
    println("}");
    printInnerDeclarations(node);
    println("\n@end");

    printStaticInitFunction(node);
  }

  private void generateInterfaceType(TypeDeclaration node) {
    ITypeBinding binding = node.getTypeBinding();
    String typeName = NameTable.getFullName(binding);

    printTypeDocumentation(node);
    printf("@protocol %s", typeName);
    List<String> interfaces = getInterfaceNames(binding);
    interfaces.add("NSObject");
    interfaces.add("JavaObject");
    printImplementedProtocols(interfaces);
    newline();
    printInnerDeclarations(node);
    println("\n@end");

    // Print @interface for static constants, if any.
    if (hasInitializeMethod(node)) {
      printf("\n@interface %s : NSObject\n", typeName);
      println("\n@end");
    }
    printStaticInitFunction(node);
  }

  private void generateEnumType(EnumDeclaration node) {
    ITypeBinding enumType = node.getTypeBinding();
    String typeName = NameTable.getFullName(enumType);
    List<EnumConstantDeclaration> constants = node.getEnumConstants();

    // Strip enum type suffix.
    String bareTypeName =
        typeName.endsWith("Enum") ? typeName.substring(0, typeName.length() - 4) : typeName;

    // C doesn't allow empty enum declarations.  Java does, so we skip the
    // C enum declaration and generate the type declaration.
    if (!constants.isEmpty()) {
      newline();
      printf("typedef NS_ENUM(NSUInteger, %s) {\n", bareTypeName);

      // Print C enum typedef.
      indent();
      int ordinal = 0;
      for (EnumConstantDeclaration constant : constants) {
        printIndent();
        printf("%s_%s = %d,\n", bareTypeName, constant.getName().getIdentifier(), ordinal++);
      }
      unindent();
      print("};\n");
    }

    // Print enum type.
    printTypeDocumentation(node);
    printf("@interface %s : JavaLangEnum", typeName);
    List<String> interfaces = getInterfaceNames(enumType);
    interfaces.remove("NSCopying");
    interfaces.add(0, "NSCopying");
    printImplementedProtocols(interfaces);
    println(" {");
    printInstanceVariables(getFieldsToDeclare(node));
    println("}");
    printInnerDeclarations(node);
    println("\n@end");
    printStaticInitFunction(node);

    printf("\nFOUNDATION_EXPORT %s *%s_values_[];\n", typeName, typeName);
    for (EnumConstantDeclaration constant : constants) {
      String varName = NameTable.getStaticVarName(constant.getVariableBinding());
      String valueName = constant.getName().getIdentifier();
      printf("\n#define %s_%s %s_values_[%s_%s]\n",
             typeName, varName, typeName, bareTypeName, valueName);
      printf("J2OBJC_ENUM_CONSTANT_GETTER(%s, %s)\n", typeName, varName);
    }
  }

  private void generateAnnotationType(AnnotationTypeDeclaration node) {
    ITypeBinding type = node.getTypeBinding();
    String typeName = NameTable.getFullName(type);
    List<AnnotationTypeMemberDeclaration> members = Lists.newArrayList(
        Iterables.filter(node.getBodyDeclarations(), AnnotationTypeMemberDeclaration.class));

    boolean isRuntime = BindingUtil.isRuntimeAnnotation(type);

    // Print annotation as protocol.
    printTypeDocumentation(node);
    printf("@protocol %s < JavaLangAnnotationAnnotation >\n", typeName);
    if (isRuntime) {
      printAnnotationProperties(members);
    }
    println("\n@end");

    if (isRuntime || hasInitializeMethod(node)) {
      // Print annotation implementation interface.
      printf("\n@interface %s : NSObject", typeName);
      if (isRuntime) {
        printf(" < %s >", typeName);
      }
      if (isRuntime && !members.isEmpty()) {
        println(" {\n @private");
        printAnnotationVariables(members);
        println("}");
        printAnnotationConstructor(type);
        printAnnotationAccessors(members);
      } else {
        newline();
      }
      println("\n@end");
    }
    printStaticInitFunction(node);
  }

  private void printTypeLiteralDeclaration(AbstractTypeDeclaration node) {
    newline();
    printf("J2OBJC_TYPE_LITERAL_HEADER(%s)\n", NameTable.getFullName(node.getTypeBinding()));
  }

  private static final Set<String> NEEDS_INC_AND_DEC = ImmutableSet.of(
      "int", "long", "double", "float", "short", "byte", "char");

  private void printIncrementAndDecrementFunctions(ITypeBinding type) {
    ITypeBinding primitiveType = Types.getPrimitiveType(type);
    if (primitiveType == null || !NEEDS_INC_AND_DEC.contains(primitiveType.getName())) {
      return;
    }
    String primitiveName = primitiveType.getName();
    String valueMethod = primitiveName + "Value";
    if (primitiveName.equals("long")) {
      valueMethod = "longLongValue";
    } else if (primitiveName.equals("byte")) {
      valueMethod = "charValue";
    }
    newline();
    printf("BOXED_INC_AND_DEC(%s, %s, %s)\n", NameTable.capitalize(primitiveName), valueMethod,
           NameTable.getFullName(type));
  }

  private void printUnprefixedAlias(ITypeBinding binding) {
    String typeName = NameTable.getFullName(binding);
    String pkg = binding.getPackage().getName();
    if (NameTable.hasPrefix(pkg) && binding.isTopLevel()) {
      String unprefixedName = NameTable.camelCaseQualifiedName(binding.getQualifiedName());
      if (binding.isEnum()) {
        unprefixedName += "Enum";
      }
      if (!unprefixedName.equals(typeName)) {
        if (binding.isInterface()) {
          // Protocols can't be used in typedefs.
          printf("\n#define %s %s\n", unprefixedName, typeName);
        } else {
          printf("\ntypedef %s %s;\n", typeName, unprefixedName);
        }
      }
    }
  }

  private void printStaticInitFunction(AbstractTypeDeclaration node) {
    ITypeBinding binding = node.getTypeBinding();
    String typeName = NameTable.getFullName(binding);
    if (hasInitializeMethod(node)) {
      printf("\nJ2OBJC_STATIC_INIT(%s)\n", typeName);
    } else {
      printf("\nJ2OBJC_EMPTY_STATIC_INIT(%s)\n", typeName);
    }
  }

  @Override
  protected void printFunctionDeclaration(FunctionDeclaration function) {
    print("\nFOUNDATION_EXPORT " + getFunctionSignature(function));
    if (function.returnsRetained()) {
      print(" NS_RETURNS_RETAINED");
    }
    println(";");
  }

  /**
   * Print method declarations with #pragma mark lines documenting their scope.
   */
  @Override
  protected void printInnerDeclarations(AbstractTypeDeclaration node) {
    // Everything is public in interfaces.
    if (isInterfaceType(node)) {
      super.printInnerDeclarations(node);
      return;
    }

    List<BodyDeclaration> innerDeclarations = Lists.newArrayList(getInnerDeclarations(node));
    printSortedDeclarations(innerDeclarations, "Public", java.lang.reflect.Modifier.PUBLIC);
    printSortedDeclarations(innerDeclarations, "Protected", java.lang.reflect.Modifier.PROTECTED);
    printSortedDeclarations(innerDeclarations, "Package-Private", 0);
    printSortedDeclarations(innerDeclarations, "Private", java.lang.reflect.Modifier.PRIVATE);
  }

  private void printSortedDeclarations(
      List<BodyDeclaration> allDeclarations, String title, int modifier) {
    List<BodyDeclaration> declarations = Lists.newArrayList();
    for (BodyDeclaration decl : allDeclarations) {
      int accessMask = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE;
      // The following test works with package-private access, which doesn't have its own flag.
      if ((decl.getModifiers() & accessMask) == modifier) {
        declarations.add(decl);
      }
    }
    if (declarations.isEmpty()) {
      return;
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
    printf("\n#pragma mark %s\n", title);
    TreeUtil.sortMethods(methods);
    printDeclarations(methods);
    printDeclarations(declarations);
  }

  protected void printForwardDeclarations(Set<Import> forwardDecls) {
    Set<String> forwardStmts = Sets.newTreeSet();
    for (Import imp : forwardDecls) {
      forwardStmts.add(createForwardDeclaration(imp.getTypeName(), imp.isInterface()));
    }
    if (!forwardStmts.isEmpty()) {
      for (String stmt : forwardStmts) {
        println(stmt);
      }
      newline();
    }
  }

  protected void generateFileHeader() {
    printf("#ifndef _%s_H_\n", getGenerationUnit().getName());
    printf("#define _%s_H_\n", getGenerationUnit().getName());
    pushIgnoreDeprecatedDeclarationsPragma();
    newline();

    HeaderImportCollector collector = new HeaderImportCollector();
    collector.collect(getGenerationUnit().getCompilationUnits());

    printForwardDeclarations(collector.getForwardDeclarations());

    // Print collected includes.
    Set<Import> superTypes = collector.getSuperTypes();
    Set<String> includeStmts = Sets.newTreeSet();
    includeStmts.add("#include \"J2ObjC_header.h\"");
    for (Import imp : superTypes) {
      includeStmts.add(String.format("#include \"%s.h\"", imp.getImportFileName()));
    }
    for (String stmt : includeStmts) {
      println(stmt);
    }
  }

  protected String createForwardDeclaration(String typeName, boolean isInterface) {
    return String.format("@%s %s;", isInterface ? "protocol" : "class", typeName);
  }

  protected void generateFileFooter() {
    newline();
    popIgnoreDeprecatedDeclarationsPragma();
    printf("#endif // _%s_H_\n", getGenerationUnit().getName());
  }

  private void printAnnotationVariables(List<AnnotationTypeMemberDeclaration> members) {
    indent();
    for (AnnotationTypeMemberDeclaration member : members) {
      printIndent();
      ITypeBinding type = member.getMethodBinding().getReturnType();
      print(NameTable.getObjCType(type));
      if (type.isPrimitive() || type.isInterface()) {
        print(' ');
      }
      print(NameTable.getAnnotationPropertyVariableName(member.getMethodBinding()));
      println(";");
    }
    unindent();
  }

  private void printAnnotationConstructor(ITypeBinding annotation) {
    newline();
    print(annotationConstructorDeclaration(annotation));
    println(";");
  }

  private void printAnnotationProperties(List<AnnotationTypeMemberDeclaration> members) {
    if (!members.isEmpty()) {
      newline();
    }
    for (AnnotationTypeMemberDeclaration member : members) {
      ITypeBinding type = member.getType().getTypeBinding();
      print("@property (readonly) ");
      String typeString = NameTable.getSpecificObjCType(type);
      String propertyName = NameTable.getAnnotationPropertyName(member.getMethodBinding());
      println(String.format("%s%s%s;", typeString, typeString.endsWith("*") ? "" : " ",
          propertyName));
      if (needsObjcMethodFamilyNoneAttribute(propertyName)) {
        println(String.format("- (%s)%s OBJC_METHOD_FAMILY_NONE;", typeString, propertyName));
      }
    }
  }

  private void printAnnotationAccessors(List<AnnotationTypeMemberDeclaration> members) {
    boolean printedNewline = false;
    for (AnnotationTypeMemberDeclaration member : members) {
      if (member.getDefault() != null) {
        if (!printedNewline) {
          newline();
          printedNewline = true;
        }
        ITypeBinding type = member.getType().getTypeBinding();
        String typeString = NameTable.getSpecificObjCType(type);
        String propertyName =
            NameTable.getAnnotationPropertyName(member.getMethodBinding());
        printf("+ (%s)%sDefault;\n", typeString, propertyName);
      }
    }
  }

  @Override
  protected void printStaticFieldDeclaration(
      VariableDeclarationFragment fragment, String baseDeclaration) {
    println("FOUNDATION_EXPORT " + baseDeclaration + ";");
  }
}
