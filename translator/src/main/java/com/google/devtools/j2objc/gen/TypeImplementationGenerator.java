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
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

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

    if (needsImplementation()) {
      newline();
      syncLineNumbers(typeNode.getName()); // avoid doc-comment
      printf("@implementation %s\n", typeName);
      printInnerDeclarations();
      printAnnotationImplementation();
      printInitializeMethod();
      printReflectionMethods();
      println("\n@end");
    }

    printOuterDeclarations();
    printTypeLiteralImplementation();
  }

  private void printInitFlagDefinition() {
    if (hasInitializeMethod()) {
      printf("\nJ2OBJC_INITIALIZED_DEFN(%s)\n", typeName);
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
      String name = nameTable.getStaticVarQualifiedName(varBinding);
      String objcType = nameTable.getObjCType(varBinding.getType());
      objcType += objcType.endsWith("*") ? "" : " ";
      if (initializer != null) {
        printf("%s%s = %s;\n", objcType, name, generateExpression(initializer));
      } else {
        printf("%s%s;\n", objcType, name);
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
    return method.isConstructor() && extendsNumber(method.getDeclaringClass())
        && NSNUMBER_DESIGNATED_INITIALIZERS.contains(nameTable.getMethodSelector(method));
  }

  @Override
  protected void printMethodDeclaration(MethodDeclaration m) {
    if (typeBinding.isInterface() || Modifier.isAbstract(m.getModifiers())) {
      return;
    }
    newline();
    boolean isDesignatedInitializer = isDesignatedInitializer(m.getMethodBinding());
    if (isDesignatedInitializer) {
      println("#pragma clang diagnostic push");
      println("#pragma clang diagnostic ignored \"-Wobjc-designated-initializers\"");
    }
    syncLineNumbers(m.getName());  // avoid doc-comment
    String methodBody = generateStatement(m.getBody(), /* isFunction */ false);
    print(getMethodSignature(m) + " " + reindent(methodBody) + "\n");
    if (isDesignatedInitializer) {
      println("#pragma clang diagnostic pop");
    }
  }

  @Override
  protected void printFunctionDeclaration(FunctionDeclaration function) {
    newline();
    syncLineNumbers(function);  // avoid doc-comment
    if (Modifier.isNative(function.getModifiers())) {
      printJniFunctionAndWrapper(function);
    } else {
      String functionBody = generateStatement(function.getBody(), /* isFunction */ true);
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
      sb.append(paramType + ' ' + nameTable.getVariableName(var));
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
        printf("return (%s) ", nameTable.getSpecificObjCType(returnType));
      }
    }
    print(function.getJniSignature());
    print("(&J2ObjC_JNIEnv");
    if (Modifier.isStatic(function.getModifiers())) {
      printf(", %s_class_()", nameTable.getFullName(function.getDeclaringClass()));
    }
    for (SingleVariableDeclaration param : function.getParameters()) {
      printf(", %s", nameTable.getVariableName(param.getVariableBinding()));
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
      sb.append(generateStatement(statement, false));
    }
    sb.append("J2OBJC_SET_INITIALIZED(" + typeName + ")\n");
    sb.append("}\n}");
    print("\n+ (void)initialize " + reindent(sb.toString()) + "\n");
  }

  private void printReflectionMethods() {
    if (typeNeedsReflection) {
      RuntimeAnnotationGenerator.printTypeAnnotationMethods(getBuilder(), typeNode);
      printMetadata();
    }
  }

  private void printAnnotationImplementation() {
    if (BindingUtil.isRuntimeAnnotation(typeBinding)) {
      List<AnnotationTypeMemberDeclaration> members = TreeUtil.getAnnotationMembers(typeNode);
      printAnnotationProperties(members);
      if (!members.isEmpty()) {
        printAnnotationConstructor(typeBinding);
      }
      printAnnotationAccessors(members);
      println("\n- (IOSClass *)annotationType {");
      printf("  return %s_class_();\n", typeName);
      println("}");
      println("\n- (NSString *)description {");
      printf("  return @\"@%s()\";\n", typeBinding.getBinaryName());
      println("}");
    }
  }

  private void printAnnotationConstructor(ITypeBinding annotation) {
    newline();
    print(getAnnotationConstructorSignature(annotation));
    println(" {");
    println("  if ((self = [super init])) {");
    for (IMethodBinding member : annotation.getDeclaredMethods()) {
      String name = NameTable.getAnnotationPropertyVariableName(member);
      printf("    self->%s = ", name);
      ITypeBinding type = member.getReturnType();
      boolean needsRetain = !type.isPrimitive();
      if (needsRetain) {
        print("RETAIN_(");
      }
      printf("%s__", NameTable.getAnnotationPropertyName(member));
      if (needsRetain) {
        print(')');
      }
      println(";");
    }
    println("  }");
    println("  return self;");
    println("}");
  }

  private void printAnnotationProperties(List<AnnotationTypeMemberDeclaration> members) {
    if (!members.isEmpty()) {
      newline();
    }
    for (AnnotationTypeMemberDeclaration member : members) {
      IMethodBinding memberBinding = member.getMethodBinding();
      println(String.format("@synthesize %s = %s;",
          NameTable.getAnnotationPropertyName(memberBinding),
          NameTable.getAnnotationPropertyVariableName(memberBinding)));
    }
  }

  private void printAnnotationAccessors(List<AnnotationTypeMemberDeclaration> members) {
    for (AnnotationTypeMemberDeclaration member : members) {
      Expression deflt = member.getDefault();
      if (deflt != null) {
        ITypeBinding type = member.getType().getTypeBinding();
        String typeString = nameTable.getSpecificObjCType(type);
        String propertyName = NameTable.getAnnotationPropertyName(member.getMethodBinding());
        printf("\n+ (%s)%sDefault {\n", typeString, propertyName);
        printf("  return %s;\n", generateExpression(deflt));
        println("}");
      }
    }
  }

  protected void printMetadata() {
    print(new MetadataGenerator(typeNode).getMetadataSource());
  }

  protected String generateStatement(Statement stmt, boolean asFunction) {
    return StatementGenerator.generate(stmt, asFunction, getBuilder().getCurrentLine());
  }
}
