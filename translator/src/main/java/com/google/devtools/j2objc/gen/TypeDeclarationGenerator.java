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
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.PropertyAnnotation;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import com.google.j2objc.annotations.Property;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Base class for generating type declarations, either public or private.
 *
 * @author Tom Ball, Keith Stanger
 */
public class TypeDeclarationGenerator extends TypeGenerator {

  private static final String DEPRECATED_ATTRIBUTE = "__attribute__((deprecated))";

  protected TypeDeclarationGenerator(SourceBuilder builder, AbstractTypeDeclaration node) {
    super(builder, node);
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
      pushNullabilityCompletenessPragma();
      generateInitialDeclaration();
      popNullabilityCompletenessPragma();
    }
  }

  protected void generateInitialDeclaration() {
    printNativeEnum();

    printTypeDocumentation();
    if (typeBinding.isInterface()) {
      printf("@protocol %s", typeName);
    } else {
      printf("@interface %s : %s", typeName, getSuperTypeName());
    }
    printImplementedProtocols();
    if (!typeBinding.isInterface()) {
      printInstanceVariables();
    } else {
      newline();
    }
    printProperties();
    if (!typeBinding.isInterface()) {
      printStaticAccessors();
    }
    printInnerDeclarations();
    printDisallowedConstructors();
    println("\n@end");

    // Package-info type, skip all outer declarations.
    if (BindingUtil.isSynthetic(typeBinding)) {
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
  }

  private void printNativeEnum() {
    if (!(typeNode instanceof EnumDeclaration)) {
      return;
    }

    List<EnumConstantDeclaration> constants = ((EnumDeclaration) typeNode).getEnumConstants();
    String nativeName = NameTable.getNativeEnumName(typeName);

    // C doesn't allow empty enum declarations.  Java does, so we skip the
    // C enum declaration and generate the type declaration.
    if (!constants.isEmpty()) {
      newline();
      printf("typedef NS_ENUM(NSUInteger, %s) {\n", nativeName);

      // Print C enum typedef.
      indent();
      int ordinal = 0;
      for (EnumConstantDeclaration constant : constants) {
        printIndent();
        printf("%s_%s = %d,\n",
            nativeName, nameTable.getVariableBaseName(constant.getVariableBinding()), ordinal++);
      }
      unindent();
      print("};\n");
    }
  }

  private void printTypeDocumentation() {
    newline();
    JavadocGenerator.printDocComment(getBuilder(), typeNode.getJavadoc());
    if (needsDeprecatedAttribute(typeNode.getAnnotations())) {
      println(DEPRECATED_ATTRIBUTE);
    }
  }

  private String getSuperTypeName() {
    ITypeBinding supertype = TranslationUtil.getSuperType(typeNode);
    if (supertype != null) {
      return nameTable.getFullName(supertype);
    }
    return "NSObject";
  }

  private List<String> getInterfaceNames() {
    if (typeBinding.isAnnotation()) {
      return Lists.newArrayList("JavaLangAnnotationAnnotation");
    }
    List<String> names = Lists.newArrayList();
    for (ITypeBinding intrface : TranslationUtil.getInterfaceTypes(typeNode)) {
      names.add(nameTable.getFullName(intrface));
    }
    if (typeBinding.isEnum()) {
      names.remove("NSCopying");
      names.add(0, "NSCopying");
    } else if (isInterfaceType()) {
      names.add("NSObject");
      names.add("JavaObject");
    }
    return names;
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
    if (Options.staticAccessorMethods()) {
      for (VariableDeclarationFragment fragment : getStaticFields()) {
        IVariableBinding var = fragment.getVariableBinding();
        String accessorName = nameTable.getStaticAccessorName(var);
        String objcType = nameTable.getObjCType(var.getType());
        printf("\n+ (%s)%s;\n", objcType, accessorName);
        if (!Modifier.isFinal(var.getModifiers())) {
          printf("\n+ (void)set%s:(%s)value;\n", NameTable.capitalize(accessorName), objcType);
        }
      }
      if (typeNode instanceof EnumDeclaration) {
        for (EnumConstantDeclaration constant : ((EnumDeclaration) typeNode).getEnumConstants()) {
          String accessorName = nameTable.getStaticAccessorName(constant.getVariableBinding());
          printf("\n+ (%s *)%s;\n", typeName, accessorName);
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
      IVariableBinding varBinding = fragment.getVariableBinding();
      FieldDeclaration declaration = (FieldDeclaration) fragment.getParent();
      if (declaration != lastDeclaration) {
        if (lastDeclaration != null) {
          println(";");
        }
        lastDeclaration = declaration;
        JavadocGenerator.printDocComment(getBuilder(), declaration.getJavadoc());
        printIndent();
        if (BindingUtil.isWeakReference(varBinding) && !BindingUtil.isVolatile(varBinding)) {
          // We must add this even without -use-arc because the header may be
          // included by a file compiled with ARC.
          print("__unsafe_unretained ");
        }
        String objcType = getDeclarationType(varBinding);
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
      }
      print(nameTable.getVariableShortName(varBinding));
    }
    println(";");
    unindent();
    println("}");
  }

  protected void printProperties() {
    Iterable<VariableDeclarationFragment> fields = getAllInstanceFields();
    for (VariableDeclarationFragment fragment : fields) {
      FieldDeclaration fieldDecl = (FieldDeclaration) fragment.getParent();
      IVariableBinding varBinding = fragment.getVariableBinding();
      if (!BindingUtil.isStatic(varBinding)) {
        PropertyAnnotation property = (PropertyAnnotation)
            TreeUtil.getAnnotation(Property.class, fieldDecl.getAnnotations());
        if (property != null) {
          print("@property ");
          ITypeBinding varType = varBinding.getType();
          String propertyName = nameTable.getVariableBaseName(varBinding);

          // Add default getter/setter here, as each fragment needs its own attributes
          // to support its unique accessors.
          Set<String> attributes = property.getPropertyAttributes();
          if (property.getGetter() == null) {
            IMethodBinding getter = BindingUtil.findGetterMethod(
                propertyName, varType, varBinding.getDeclaringClass());
            if (getter != null) {
              attributes.add("getter=" + NameTable.getMethodName(getter));
              if (!BindingUtil.isSynchronized(getter)) {
                attributes.add("nonatomic");
              }
            }
          }
          if (property.getSetter() == null) {
            IMethodBinding setter = BindingUtil.findSetterMethod(
                propertyName, varBinding.getDeclaringClass());
            if (setter != null) {
              attributes.add("setter=" + NameTable.getMethodName(setter));
              if (!BindingUtil.isSynchronized(setter)) {
                attributes.add("nonatomic");
              }
            }
          }

          if (!attributes.isEmpty()) {
            print('(');
            print(PropertyAnnotation.toAttributeString(attributes));
            print(") ");
          }

          String objcType = nameTable.getObjCType(varType);
          print(objcType);
          if (!objcType.endsWith("*")) {
            print(' ');
          }
          println(propertyName + ";");
        }
      }
    }
  }

  protected void printCompanionClassDeclaration() {
    if (!typeBinding.isInterface() || !needsCompanionClass()
        || printPrivateDeclarations() == needsPublicCompanionClass()) {
      return;
    }
    printf("\n@interface %s : NSObject", typeName);
    if (BindingUtil.isRuntimeAnnotation(typeBinding)
        || BindingUtil.hasDefaultMethodsInFamily(typeBinding)) {
      // Print annotation implementation interface.
      printf(" < %s >", typeName);
    }
    printInstanceVariables();
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
      printf("FOUNDATION_EXPORT %s *%s_values_[];\n", typeName, typeName);
      for (EnumConstantDeclaration constant : ((EnumDeclaration) typeNode).getEnumConstants()) {
        String varName = nameTable.getVariableBaseName(constant.getVariableBinding());
        newline();
        JavadocGenerator.printDocComment(getBuilder(), constant.getJavadoc());
        printf("inline %s *%s_get_%s();\n", typeName, typeName, varName);
        printf("J2OBJC_ENUM_CONSTANT(%s, %s)\n", typeName, varName);
      }
    }
  }

  private static final Predicate<VariableDeclarationFragment> NEEDS_SETTER =
      new Predicate<VariableDeclarationFragment>() {
    @Override
    public boolean apply(VariableDeclarationFragment fragment) {
      IVariableBinding var = fragment.getVariableBinding();
      return !var.getType().isPrimitive() && !BindingUtil.isSynthetic(var)
          && !BindingUtil.isWeakReference(var);
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
      IVariableBinding var = fragment.getVariableBinding();
      String typeStr = nameTable.getObjCType(var.getType());
      if (typeStr.contains(",")) {
        typeStr = "J2OBJC_ARG(" + typeStr + ')';
      }
      String fieldName = nameTable.getVariableShortName(var);
      String isVolatile = BindingUtil.isVolatile(var) ? "_VOLATILE" : "";
      println(UnicodeUtils.format("J2OBJC%s_FIELD_SETTER(%s, %s, %s)",
          isVolatile, typeName, fieldName, typeStr));
    }
  }

  protected void printStaticFieldDeclarations() {
    for (VariableDeclarationFragment fragment : getStaticFields()) {
      printStaticFieldFullDeclaration(fragment);
    }
  }

  // Overridden in TypePrivateDeclarationGenerator
  protected void printStaticFieldDeclaration(
      VariableDeclarationFragment fragment, String baseDeclaration) {
    println("/*! INTERNAL ONLY - Use accessor function from above. */");
    println("FOUNDATION_EXPORT " + baseDeclaration + ";");
  }

  private void printStaticFieldFullDeclaration(VariableDeclarationFragment fragment) {
    IVariableBinding var = fragment.getVariableBinding();
    boolean isVolatile = BindingUtil.isVolatile(var);
    String objcType = nameTable.getObjCType(var.getType());
    String objcTypePadded = objcType + (objcType.endsWith("*") ? "" : " ");
    String declType = getDeclarationType(var);
    declType += (declType.endsWith("*") ? "" : " ");
    String name = nameTable.getVariableShortName(var);
    boolean isFinal = Modifier.isFinal(var.getModifiers());
    boolean isPrimitive = var.getType().isPrimitive();
    boolean isConstant = BindingUtil.isPrimitiveConstant(var);
    String qualifiers = isConstant ? "_CONSTANT"
        : (isPrimitive ? "_PRIMITIVE" : "_OBJ") + (isVolatile ? "_VOLATILE" : "")
        + (isFinal ? "_FINAL" : "");
    newline();
    FieldDeclaration decl = (FieldDeclaration) fragment.getParent();
    JavadocGenerator.printDocComment(getBuilder(), decl.getJavadoc());
    printf("inline %s%s_get_%s();\n", objcTypePadded, typeName, name);
    if (!isFinal) {
      printf("inline %s%s_set_%s(%svalue);\n", objcTypePadded, typeName, name, objcTypePadded);
      if (isPrimitive && !isVolatile) {
        printf("inline %s *%s_getRef_%s();\n", objcType, typeName, name);
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
    printf("J2OBJC_STATIC_FIELD%s(%s, %s, %s)\n", qualifiers, typeName, name, objcType);
  }

  private void printTypeLiteralDeclaration() {
    newline();
    printf("J2OBJC_TYPE_LITERAL_HEADER(%s)\n", typeName);
  }

  private void printBoxedOperators() {
    ITypeBinding primitiveType = typeEnv.getPrimitiveType(typeBinding);
    if (primitiveType == null) {
      return;
    }
    char binaryName = primitiveType.getBinaryName().charAt(0);
    if ("ZV".indexOf(binaryName) >= 0) {
      return; // No special operators are needed for java.lang.Boolean or java.lang.Void.
    }
    String primitiveName = primitiveType.getName();
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
    String pkg = typeBinding.getPackage().getName();
    if (nameTable.hasPrefix(pkg) && typeBinding.isTopLevel()) {
      String unprefixedName = NameTable.camelCaseQualifiedName(typeBinding.getQualifiedName());
      if (!unprefixedName.equals(typeName)) {
        if (typeBinding.isInterface()) {
          // Protocols can't be used in typedefs.
          printf("\n#define %s %s\n", unprefixedName, typeName);
        } else {
          printf("\n@compatibility_alias %s %s;\n", unprefixedName, typeName);
        }
      }
    }
  }

  /**
   * Emit method declaration.
   *
   * @param m The method.
   * @param isCompanionClass If true, emit only if m is a static interface method.
   */
  private void printMethodDeclaration(MethodDeclaration m, boolean isCompanionClass) {
    IMethodBinding methodBinding = m.getMethodBinding();
    ITypeBinding typeBinding = methodBinding.getDeclaringClass();

    if (typeBinding.isInterface()) {
      // isCompanion and isStatic must be both false (i.e. this prints a non-static method decl
      // in @protocol) or must both be true (i.e. this prints a static method decl in the
      // companion class' @interface).
      if (isCompanionClass != BindingUtil.isStatic(methodBinding)) {
        return;
      }
    }

    newline();
    JavadocGenerator.printDocComment(getBuilder(), m.getJavadoc());
    print(getMethodSignature(m));
    String methodName = nameTable.getMethodSelector(methodBinding);
    if (!m.isConstructor() && !BindingUtil.isSynthetic(m.getModifiers())
        && NameTable.needsObjcMethodFamilyNoneAttribute(methodName)) {
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

  @Override
  protected void printMethodDeclaration(MethodDeclaration m) {
    printMethodDeclaration(m, false);
  }

  private boolean needsDeprecatedAttribute(List<Annotation> annotations) {
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
  protected void printInnerDeclarations() {
    // Everything is public in interfaces.
    if (isInterfaceType() || typeNode.hasPrivateDeclaration()) {
      super.printInnerDeclarations();
      return;
    }

    List<BodyDeclaration> innerDeclarations = Lists.newArrayList(getInnerDeclarations());
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

  /**
   * Declare any inherited constructors that aren't allowed to be accessed in Java
   * with a NS_UNAVAILABLE macro, so that clang will flag such access from native
   * code as an error.
   */
  private void printDisallowedConstructors() {
    if (!Options.disallowInheritedConstructors()) {
      return;
    }
    if (typeBinding.isAnnotation() || typeBinding.isAnonymous() || typeBinding.isArray()
        || typeBinding.isEnum() || typeBinding.isInterface() || typeBinding.isSynthetic()
        || BindingUtil.isAbstract(typeBinding)) {
      return;
    }
    Set<IMethodBinding> constructors = BindingUtil.getDeclaredConstructors(typeBinding);
    List<IMethodBinding> inheritedConstructors = new ArrayList<>();
    ITypeBinding superType = typeBinding.getSuperclass();
    while (superType != null) {
      // Add super constructors that have unique parameters.
      for (IMethodBinding superC : BindingUtil.getDeclaredConstructors(superType)) {
        if (!hasConstructor(constructors, superC)
            && !hasConstructor(inheritedConstructors, superC)) {
          inheritedConstructors.add(superC);
        }
      }
      superType = superType.getSuperclass();
    }
    if (!inheritedConstructors.isEmpty()) {
      newline();
      println("// Disallowed inherited constructors, do not use.");
      for (IMethodBinding constructor : inheritedConstructors) {
        print(getConstructorSignature(constructor));
        println(" NS_UNAVAILABLE;");
      }
    }
  }

  // Returns true if a constructor has an overriding constructor in a specified set.
  private static boolean hasConstructor(Collection<IMethodBinding> constructors,
      IMethodBinding constructor) {
    for (IMethodBinding c : constructors) {
      if (constructor.isSubsignature(c)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Create an Objective-C constructor signature from a method binding.
   * This is similar to TypeGenerator.getMethodSignature(MethodDeclaration),
   * but works with constructors not declared by the type being generated.
   */
  protected String getConstructorSignature(IMethodBinding m) {
    StringBuilder sb = new StringBuilder();
    sb.append("- (instancetype)");
    ITypeBinding[] params = m.getParameterTypes();
    String selector = nameTable.getMethodSelector(m);
    String[] selParts = selector.split(":");
    if (params.length == 0) {
      assert selParts.length == 1 && !selector.endsWith(":");
      sb.append(selParts[0]);
    } else {
      assert params.length == selParts.length;
      int baseLength = sb.length() + selParts[0].length();
      for (int i = 0; i < params.length; i++) {
        if (i != 0) {
          sb.append('\n');
          sb.append(pad(baseLength - selParts[i].length()));
        }
        String typeName = nameTable.getObjCType(params[i]);
        sb.append(UnicodeUtils.format("%s:(%s)arg%d", selParts[i], typeName, i));
      }
    }
    return sb.toString();
  }

  /**
   * Ignores nullability completeness warnings. If clang finds any nullability
   * annotations, it checks that all annotatable sites have annotations. Java
   * checker frameworks don't have that requirement.
   */
  protected void pushNullabilityCompletenessPragma() {
    if (hasNullabilityAnnotations) {
      newline();
      println("#if __has_feature(nullability)");
      println("#pragma clang diagnostic push");
      println("#pragma GCC diagnostic ignored \"-Wnullability-completeness\"");
      println("#endif");
    }
  }

  protected void popNullabilityCompletenessPragma() {
    if (hasNullabilityAnnotations) {
      newline();
      println("#if __has_feature(nullability)");
      println("#pragma clang diagnostic pop");
      println("#endif");
    }
  }
}
