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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.j2objc.annotations.Property;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Generates implementation code for an AbstractTypeDeclaration node.
 *
 * @author Tom Ball, Keith Stanger
 */
public class TypeImplementationGenerator extends TypeGenerator {

  private static final Set<String> NSNUMBER_DESIGNATED_INITIALIZERS = ImmutableSet.of(
      "initWithBool:",
      "initWithChar:",
      "initWithDouble:",
      "initWithFloat:",
      "initWithInt:",
      "initWithInteger:",
      "initWithLong:",
      "initWithLongLong:",
      "initWithShort:",
      "initWithUnsignedChar:",
      "initWithUnsignedInt:",
      "initWithUnsignedInteger:",
      "initWithUnsignedLong:",
      "initWithUnsignedLongLong:",
      "initWithUnsignedShort:");

  private TypeImplementationGenerator(SourceBuilder builder, AbstractTypeDeclaration node) {
    super(builder, node);
  }

  public static void generate(SourceBuilder builder, AbstractTypeDeclaration node) {
    new TypeImplementationGenerator(builder, node).generate();
  }

  protected void generate() {
    syncFilename(compilationUnit.getSourceFilePath());

    printInitFlagDefinition();
    printStaticVars();
    printEnumValuesArray();

    if (!typeBinding.isInterface() || needsCompanionClass()) {
      newline();
      syncLineNumbers(typeNode.getName()); // avoid doc-comment
      printf("@implementation %s\n", typeName);
      printProperties();
      printStaticAccessors();
      printInnerDeclarations();
      printInitializeMethod();
      printReflectionMethods();
      println("\n@end");
    }

    // Package-info type, skip all outer declarations.
    if (BindingUtil.isSynthetic(typeBinding)) {
      return;
    }
    printOuterDeclarations();
    printTypeLiteralImplementation();
  }

  private void printInitFlagDefinition() {
    if (hasInitializeMethod()) {
      printf("\nJ2OBJC_INITIALIZED_DEFN(%s)\n", typeName);
    }
  }

  private static final Predicate<VariableDeclarationFragment> PROPERTIES =
      new Predicate<VariableDeclarationFragment>() {
    @Override
    public boolean apply(VariableDeclarationFragment fragment) {
      IVariableBinding varBinding = fragment.getVariableBinding();
      return BindingUtil.hasAnnotation(varBinding, Property.class)
          && !BindingUtil.isStatic(varBinding);
    }
  };

  private void printProperties() {
    Iterable<VariableDeclarationFragment> fields =
        Iterables.filter(getInstanceFields(), PROPERTIES);
    if (Iterables.isEmpty(fields)) {
      return;
    }
    newline();
    for (VariableDeclarationFragment fragment : fields) {
      IVariableBinding varBinding = fragment.getVariableBinding();
      String propertyName = nameTable.getVariableBaseName(varBinding);
      String varName = nameTable.getVariableShortName(varBinding);
      println("@synthesize " + propertyName + " = " + varName + ";");
    }
  }

  private static final Predicate<VariableDeclarationFragment> NEEDS_DEFINITION =
      new Predicate<VariableDeclarationFragment>() {
    public boolean apply(VariableDeclarationFragment fragment) {
      return !BindingUtil.isPrimitiveConstant(fragment.getVariableBinding())
          // Private static vars are defined in the private declaration.
          && !((FieldDeclaration) fragment.getParent()).hasPrivateDeclaration();
    }
  };

  private void printStaticVars() {
    Iterable<VariableDeclarationFragment> fields =
        Iterables.filter(getStaticFields(), NEEDS_DEFINITION);
    if (Iterables.isEmpty(fields)) {
      return;
    }
    newline();
    for (VariableDeclarationFragment fragment : fields) {
      IVariableBinding varBinding = fragment.getVariableBinding();
      Expression initializer = fragment.getInitializer();
      String name = nameTable.getVariableQualifiedName(varBinding);
      String objcType = getDeclarationType(varBinding);
      objcType += objcType.endsWith("*") ? "" : " ";
      if (initializer != null) {
        String cast = !varBinding.getType().isPrimitive() && BindingUtil.isVolatile(varBinding)
            ? "(void *)" : "";
        printf("%s%s = %s%s;\n", objcType, name, cast, generateExpression(initializer));
      } else {
        printf("%s%s;\n", objcType, name);
      }
    }
  }

  /**
   * Prints the list of static variable and/or enum constant accessor methods.
   */
  protected void printStaticAccessors() {
    if (!Options.staticAccessorMethods()) {
      return;
    }
    for (VariableDeclarationFragment fragment : getStaticFields()) {
      if (!((FieldDeclaration) fragment.getParent()).hasPrivateDeclaration()) {
        IVariableBinding varBinding = fragment.getVariableBinding();
        ITypeBinding type = varBinding.getType();
        boolean isVolatile = BindingUtil.isVolatile(varBinding);
        boolean isPrimitive = type.isPrimitive();
        String accessorName = nameTable.getStaticAccessorName(varBinding);
        String varName = nameTable.getVariableQualifiedName(varBinding);
        String objcType = nameTable.getObjCType(type);
        String typeSuffix = isPrimitive ? NameTable.capitalize(type.getName()) : "Id";
        if (isVolatile) {
          printf("\n+ (%s)%s {\n  return JreLoadVolatile%s(&%s);\n}\n",
                 objcType, accessorName, typeSuffix, varName);
        } else {
          printf("\n+ (%s)%s {\n  return %s;\n}\n", objcType, accessorName, varName);
        }
        int modifiers = varBinding.getModifiers();
        if (!Modifier.isFinal(modifiers)) {
          String setterFunc = isVolatile
              ? (isPrimitive ? "JreAssignVolatile" + typeSuffix : "JreVolatileStrongAssign")
              : (isPrimitive | Options.useARC() ? null : "JreStrongAssign");
          if (setterFunc == null) {
            printf("\n+ (void)set%s:(%s)value {\n  %s = value;\n}\n",
                NameTable.capitalize(accessorName), objcType, varName);
          } else {
            printf("\n+ (void)set%s:(%s)value {\n  %s(&%s, value);\n}\n",
                NameTable.capitalize(accessorName), objcType, setterFunc, varName);
          }
        }
      }
    }
    if (typeNode instanceof EnumDeclaration) {
      for (EnumConstantDeclaration constant : ((EnumDeclaration) typeNode).getEnumConstants()) {
        IVariableBinding varBinding = constant.getVariableBinding();
        printf("\n+ (%s *)%s {\n  return %s;\n}\n",
            typeName, nameTable.getStaticAccessorName(varBinding),
            nameTable.getVariableQualifiedName(varBinding));
      }
    }
  }

  private void printEnumValuesArray() {
    if (typeNode instanceof EnumDeclaration) {
      List<EnumConstantDeclaration> constants = ((EnumDeclaration) typeNode).getEnumConstants();
      newline();
      printf("%s *%s_values_[%s];\n", typeName, typeName, constants.size());
    }
  }

  private void printTypeLiteralImplementation() {
    newline();
    printf("J2OBJC_%s_TYPE_LITERAL_SOURCE(%s)\n",
        isInterfaceType() ? "INTERFACE" : "CLASS", typeName);
  }

  private boolean extendsNumber(ITypeBinding type) {
    ITypeBinding numberType = typeEnv.resolveJavaType("java.lang.Number");
    while (type != null) {
      if (type == numberType) {
        return true;
      }
      type = type.getSuperclass();
    }
    return false;
  }

  private boolean isDesignatedInitializer(IMethodBinding method) {
    if (!method.isConstructor()) {
      return false;
    }
    String selector = nameTable.getMethodSelector(method);
    return selector.equals("init")
        || (extendsNumber(method.getDeclaringClass())
            && NSNUMBER_DESIGNATED_INITIALIZERS.contains(selector));
  }

  @Override
  protected void printMethodDeclaration(MethodDeclaration m) {
    if (Modifier.isAbstract(m.getModifiers())) {
      return;
    }

    newline();
    boolean isDesignatedInitializer = isDesignatedInitializer(m.getMethodBinding());
    if (isDesignatedInitializer) {
      println("J2OBJC_IGNORE_DESIGNATED_BEGIN");
    }
    syncLineNumbers(m.getName());  // avoid doc-comment
    String methodBody = generateStatement(m.getBody());
    print(getMethodSignature(m) + " " + reindent(methodBody) + "\n");
    if (isDesignatedInitializer) {
      println("J2OBJC_IGNORE_DESIGNATED_END");
    }
  }

  @Override
  protected void printFunctionDeclaration(FunctionDeclaration function) {
    newline();
    syncLineNumbers(function);  // avoid doc-comment
    if (Modifier.isNative(function.getModifiers())) {
      printJniFunctionAndWrapper(function);
    } else {
      String functionBody = generateStatement(function.getBody());
      println(getFunctionSignature(function) + " " + reindent(functionBody));
    }
  }

  private String getJniFunctionSignature(FunctionDeclaration function) {
    StringBuilder sb = new StringBuilder();
    sb.append(nameTable.getJniType(function.getReturnType().getTypeBinding()));
    sb.append(' ');
    sb.append(function.getJniSignature()).append('(');
    sb.append("JNIEnv *_env_");
    if (Modifier.isStatic(function.getModifiers())) {
      sb.append(", jclass _cls_");
    }
    if (!function.getParameters().isEmpty()) {
      sb.append(", ");
    }
    for (Iterator<SingleVariableDeclaration> iter = function.getParameters().iterator();
         iter.hasNext(); ) {
      IVariableBinding var = iter.next().getVariableBinding();
      String paramType = nameTable.getJniType(var.getType());
      sb.append(paramType + ' ' + nameTable.getVariableBaseName(var));
      if (iter.hasNext()) {
        sb.append(", ");
      }
    }
    sb.append(')');
    return sb.toString();
  }

  private void printJniFunctionAndWrapper(FunctionDeclaration function) {
    // Declare the matching JNI function.
    print("JNIEXPORT ");
    print(getJniFunctionSignature(function));
    println(";\n");

    // Generate a wrapper function that calls the matching JNI function.
    print(getFunctionSignature(function));
    println(" {");
    print("  ");
    ITypeBinding returnType = function.getReturnType().getTypeBinding();
    if (!BindingUtil.isVoid(returnType)) {
      if (returnType.isPrimitive()) {
        print("return ");
      } else {
        printf("return (%s) ", nameTable.getObjCType(returnType));
      }
    }
    print(function.getJniSignature());
    print("(&J2ObjC_JNIEnv");
    if (Modifier.isStatic(function.getModifiers())) {
      printf(", %s_class_()", nameTable.getFullName(function.getDeclaringClass()));
    }
    for (SingleVariableDeclaration param : function.getParameters()) {
      printf(", %s", nameTable.getVariableBaseName(param.getVariableBinding()));
    }
    println(");");
    println("}");
  }

  @Override
  protected void printNativeDeclaration(NativeDeclaration declaration) {
    newline();
    String code = declaration.getImplementationCode();
    if (code != null) {
      println(reindent(code));
    }
  }

  private void printInitializeMethod() {
    List<Statement> initStatements = typeNode.getClassInitStatements();
    if (initStatements.isEmpty()) {
      return;
    }
    StringBuffer sb = new StringBuffer();
    sb.append("{\nif (self == [" + typeName + " class]) {\n");
    for (Statement statement : initStatements) {
      sb.append(generateStatement(statement));
    }
    sb.append("J2OBJC_SET_INITIALIZED(" + typeName + ")\n");
    sb.append("}\n}");
    print("\n+ (void)initialize " + reindent(sb.toString()) + "\n");
  }

  private void printReflectionMethods() {
    if (typeNeedsReflection) {
      printMetadata();
    }
  }

  protected void printMetadata() {
    print(new MetadataGenerator(typeNode).getMetadataSource());
  }

  protected String generateStatement(Statement stmt) {
    return StatementGenerator.generate(stmt, getBuilder().getCurrentLine());
  }
}
