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

import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;

/**
 * Generates implementation code for an AbstractTypeDeclaration node.
 *
 * @author Tom Ball, Keith Stanger
 */
public class TypeImplementationGenerator extends TypeGenerator {

  private TypeImplementationGenerator(SourceBuilder builder, AbstractTypeDeclaration node) {
    super(builder, node);
  }

  public static void generate(SourceBuilder builder, AbstractTypeDeclaration node) {
    new TypeImplementationGenerator(builder, node).generate();
  }

  protected void generate() {
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

  private void printStaticVars() {
    boolean needsNewline = true;
    for (FieldDeclaration field : getStaticFields()) {
      if (field.hasPrivateDeclaration()) {
        // Static var is defined in declaration.
        continue;
      }
      for (VariableDeclarationFragment var : field.getFragments()) {
        IVariableBinding binding = var.getVariableBinding();
        Expression initializer = var.getInitializer();
        if (BindingUtil.isPrimitiveConstant(binding)) {
          continue;
        } else if (needsNewline) {
          needsNewline = false;
          newline();
        }
        String name = nameTable.getStaticVarQualifiedName(binding);
        String objcType = nameTable.getObjCType(binding.getType());
        objcType += objcType.endsWith("*") ? "" : " ";
        if (initializer != null) {
          printf("%s%s = %s;\n", objcType, name, generateExpression(initializer));
        } else {
          printf("%s%s;\n", objcType, name);
        }
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

  @Override
  protected void printMethodDeclaration(MethodDeclaration m) {
    if (typeBinding.isInterface() || Modifier.isAbstract(m.getModifiers())) {
      return;
    }
    newline();
    syncLineNumbers(m.getName());  // avoid doc-comment
    String methodBody = generateStatement(m.getBody(), /* isFunction */ false);
    print(getMethodSignature(m) + " " + reindent(methodBody) + "\n");
  }

  @Override
  protected void printFunctionDeclaration(FunctionDeclaration function) {
    if (Modifier.isNative(function.getModifiers())) {
      return;
    }
    newline();
    syncLineNumbers(function);  // avoid doc-comment
    String functionBody = generateStatement(function.getBody(), /* isFunction */ true);
    println(getFunctionSignature(function) + " " + reindent(functionBody));
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
