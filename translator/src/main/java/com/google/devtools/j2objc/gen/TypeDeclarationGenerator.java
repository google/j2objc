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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

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
    // Don't print declarations for any synthetic members.
    if (BindingUtil.isSynthetic(decl.getModifiers())) {
      return false;
    }
    return hasPrivateDeclaration(decl) == printPrivateDeclarations();
  }

  private void generate() {
    printConstantDefines();
    printNativeEnum();

    // TODO(kstanger): Refactor this large if-statement.
    if (!isInterfaceType()) {
      ITypeBinding binding = node.getTypeBinding();

      printTypeDocumentation();
      printf("@interface %s : %s", NameTable.getFullName(binding), getSuperTypeName());
      printImplementedProtocols();
      println(" {");
      printInstanceVariables();
      println("}");
      printInnerDeclarations();
      println("\n@end");
    } else if (node instanceof AnnotationTypeDeclaration) {
      ITypeBinding type = node.getTypeBinding();
      String typeName = NameTable.getFullName(type);
      List<AnnotationTypeMemberDeclaration> members = Lists.newArrayList(
          Iterables.filter(node.getBodyDeclarations(), AnnotationTypeMemberDeclaration.class));

      boolean isRuntime = BindingUtil.isRuntimeAnnotation(type);

      // Print annotation as protocol.
      printTypeDocumentation();
      printf("@protocol %s < JavaLangAnnotationAnnotation >\n", typeName);
      if (isRuntime) {
        printAnnotationProperties(members);
      }
      println("\n@end");

      if (isRuntime || hasInitializeMethod()) {
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
    } else {
      ITypeBinding binding = node.getTypeBinding();
      String typeName = NameTable.getFullName(binding);

      printTypeDocumentation();
      printf("@protocol %s", typeName);
      printImplementedProtocols();
      newline();
      printInnerDeclarations();
      println("\n@end");

      // Print @interface for static constants, if any.
      if (hasInitializeMethod()) {
        printf("\n@interface %s : NSObject\n", typeName);
        println("\n@end");
      }
    }

    printStaticInitFunction();
    printEnumConstants();
    printFieldSetters();
    printStaticFieldDeclarations();
    printOuterDeclarations();
    printTypeLiteralDeclaration();
    printIncrementAndDecrementFunctions();

    printUnprefixedAlias();
  }

  protected void printConstantDefines() {
    boolean needsNewline = true;
    for (FieldDeclaration fieldDecl : getStaticFields()) {
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

  private void printNativeEnum() {
    if (!(node instanceof EnumDeclaration)) {
      return;
    }

    ITypeBinding enumType = node.getTypeBinding();
    String typeName = NameTable.getFullName(enumType);
    List<EnumConstantDeclaration> constants = ((EnumDeclaration) node).getEnumConstants();

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
  }

  private void printTypeDocumentation() {
    newline();
    JavadocGenerator.printDocComment(getBuilder(), node.getJavadoc());
    if (needsDeprecatedAttribute(node.getAnnotations())) {
      println(DEPRECATED_ATTRIBUTE);
    }
  }

  private String getSuperTypeName() {
    ITypeBinding superclass = node.getTypeBinding().getSuperclass();
    if (superclass == null) {
      return null;
    }
    return NameTable.getFullName(superclass);
  }

  private List<String> getInterfaceNames() {
    List<String> names = Lists.newArrayList();
    for (ITypeBinding intrface : node.getTypeBinding().getInterfaces()) {
      names.add(NameTable.getFullName(intrface));
    }
    if (node.getTypeBinding().isEnum()) {
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

  /**
   * Prints the list of instance variables in a type.
   */
  protected void printInstanceVariables() {
    indent();
    boolean first = true;
    for (FieldDeclaration field : getInstanceFields()) {
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
      JavadocGenerator.printDocComment(getBuilder(), field.getJavadoc());
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

  private void printStaticInitFunction() {
    ITypeBinding binding = node.getTypeBinding();
    String typeName = NameTable.getFullName(binding);
    if (hasInitializeMethod()) {
      printf("\nJ2OBJC_STATIC_INIT(%s)\n", typeName);
    } else {
      printf("\nJ2OBJC_EMPTY_STATIC_INIT(%s)\n", typeName);
    }
  }

  private void printEnumConstants() {
    if (node instanceof EnumDeclaration) {
      String typeName = NameTable.getFullName(node.getTypeBinding());
      // Strip enum type suffix.
      String bareTypeName =
          typeName.endsWith("Enum") ? typeName.substring(0, typeName.length() - 4) : typeName;
      printf("\nFOUNDATION_EXPORT %s *%s_values_[];\n", typeName, typeName);
      for (EnumConstantDeclaration constant : ((EnumDeclaration) node).getEnumConstants()) {
        String varName = NameTable.getStaticVarName(constant.getVariableBinding());
        String valueName = constant.getName().getIdentifier();
        printf("\n#define %s_%s %s_values_[%s_%s]\n",
            typeName, varName, typeName, bareTypeName, valueName);
        printf("J2OBJC_ENUM_CONSTANT_GETTER(%s, %s)\n", typeName, varName);
      }
    }
  }

  protected void printFieldSetters() {
    ITypeBinding declaringType = node.getTypeBinding();
    boolean newlinePrinted = false;
    for (FieldDeclaration field : getInstanceFields()) {
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

  protected void printStaticFieldDeclarations() {
    for (VariableDeclarationFragment fragment : TreeUtil.asFragments(getStaticFields())) {
      printStaticFieldFullDeclaration(fragment);
    }
  }

  protected void printStaticFieldDeclaration(
      VariableDeclarationFragment fragment, String baseDeclaration) {
    println("FOUNDATION_EXPORT " + baseDeclaration + ";");
  }

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

  private void printTypeLiteralDeclaration() {
    newline();
    printf("J2OBJC_TYPE_LITERAL_HEADER(%s)\n", NameTable.getFullName(node.getTypeBinding()));
  }

  private static final Set<String> NEEDS_INC_AND_DEC = ImmutableSet.of(
      "int", "long", "double", "float", "short", "byte", "char");

  private void printIncrementAndDecrementFunctions() {
    ITypeBinding type = node.getTypeBinding();
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

  private void printUnprefixedAlias() {
    ITypeBinding binding = node.getTypeBinding();
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
    print(getAnnotationConstructorSignature(annotation));
    println(";");
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
  protected void printMethodDeclaration(MethodDeclaration m) {
    newline();
    JavadocGenerator.printDocComment(getBuilder(), m.getJavadoc());
    print(getMethodSignature(m));
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

  private boolean needsObjcMethodFamilyNoneAttribute(String name) {
    return name.startsWith("new") || name.startsWith("copy") || name.startsWith("alloc")
        || name.startsWith("init") || name.startsWith("mutableCopy");
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
    newline();
    String code = declaration.getHeaderCode();
    if (code != null) {
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
    if (isInterfaceType()) {
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
}
