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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.StringLiteral;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.ImplementationImportCollector;
import com.google.devtools.j2objc.types.Import;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TranslationUtil;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

import java.util.List;
import java.util.Set;

/**
 * Generates Objective-C implementation (.m) files from compilation units.
 *
 * @author Tom Ball
 */
public class ObjectiveCImplementationGenerator extends ObjectiveCSourceFileGenerator {

  private final String suffix;
  private final Set<String> invokedConstructors = Sets.newHashSet();

  /**
   * Generate an Objective-C implementation file for each type declared in a
   * specified compilation unit.
   */
  public static void generate(CompilationUnit unit) {
    new ObjectiveCImplementationGenerator(unit).generate();
  }

  private ObjectiveCImplementationGenerator(CompilationUnit unit) {
    super(unit, Options.emitLineDirectives());
    suffix = Options.getImplementationFileSuffix();
  }

  @Override
  protected String getSuffix() {
    return suffix;
  }

  public void generate() {
    CompilationUnit unit = getUnit();
    println(J2ObjC.getFileHeader(unit.getSourceFileFullPath()));
    List<AbstractTypeDeclaration> types = unit.getTypes();
    if (!types.isEmpty()) {
      findInvokedConstructors(unit);
      printStart(unit.getSourceFileFullPath());
      printImports(unit);
      printIgnoreIncompletePragmas(unit);
      pushIgnoreDeprecatedDeclarationsPragma();
      printFinalFunctionDecls(types);
      printClassExtensions(types);
      for (AbstractTypeDeclaration type : types) {
        generate(type);
        newline();
        ITypeBinding binding = type.getTypeBinding();
        printf("J2OBJC_%s_TYPE_LITERAL_SOURCE(%s)\n",
               binding.isInterface() ? "INTERFACE" : "CLASS", NameTable.getFullName(binding));
      }
      popIgnoreDeprecatedDeclarationsPragma();
    } else if (unit.getMainTypeName().endsWith(NameTable.PACKAGE_INFO_MAIN_TYPE)
        && unit.getPackage().getAnnotations().size() > 0) {
      generate(unit.getPackage());
    } else {
      // Print a dummy C function so compiled object file is valid.
      if (!types.isEmpty()) {
        printf("void %s_unused() {}\n", NameTable.getFullName(types.get(0).getTypeBinding()));
      }
    }
    save(unit);
  }

  private void printIgnoreIncompletePragmas(CompilationUnit unit) {
    if (unit.hasIncompleteProtocol() || unit.hasIncompleteImplementation()) {
      newline();
    }
    if (unit.hasIncompleteProtocol()) {
      println("#pragma clang diagnostic ignored \"-Wprotocol\"");
    }
    if (unit.hasIncompleteImplementation()) {
      println("#pragma clang diagnostic ignored \"-Wincomplete-implementation\"");
    }
  }

  private String parameterKey(IMethodBinding method) {
    StringBuilder sb = new StringBuilder();
    ITypeBinding[] parameterTypes = method.getParameterTypes();
    for (int i = 0; i < parameterTypes.length; i++) {
      if (i == 0) {
        sb.append(NameTable.capitalize(NameTable.parameterKeyword(parameterTypes[i])));
      } else {
        sb.append(NameTable.parameterKeyword(parameterTypes[i]));
      }
      sb.append('_');
    }
    return sb.toString();
  }

  private String methodKey(IMethodBinding method) {
    StringBuilder sb = new StringBuilder();
    if (method.isConstructor()) {
      sb.append(NameTable.getFullName(method.getDeclaringClass()));
    } else {
      sb.append(NameTable.getName(method));
    }
    sb.append(parameterKey(method));
    return sb.toString();
  }

  private void findInvokedConstructors(CompilationUnit unit) {
    unit.accept(new TreeVisitor() {
      @Override
      public boolean visit(ConstructorInvocation node) {
        invokedConstructors.add(methodKey(node.getMethodBinding()));
        return false;
      }
    });
  }

  @Override
  public void generate(TypeDeclaration node) {
    String typeName = NameTable.getFullName(node.getTypeBinding());
    if (node.isInterface()) {
      printStaticInterface(node, typeName);
    } else {
      printInitFlagDefinition(node);
      newline();
      syncLineNumbers(node.getName()); // avoid doc-comment
      printf("@implementation %s\n", typeName);
      printStaticVars(node);
      printMethods(node);
      if (TranslationUtil.needsReflection(node)) {
        printTypeAnnotationsMethod(node);
        printMethodAnnotationMethods(TreeUtil.getMethodDeclarations(node));
        printFieldAnnotationMethods(node);
        printMetadata(node);
      }

      println("\n@end");
      printFunctions(node.getBodyDeclarations());
    }
  }

  @Override
  protected void generate(AnnotationTypeDeclaration node) {
    boolean isRuntime = BindingUtil.isRuntimeAnnotation(node.getTypeBinding());
    boolean hasInitMethod = hasInitializeMethod(node);
    boolean needsReflection = TranslationUtil.needsReflection(node);
    String typeName = NameTable.getFullName(node.getTypeBinding());

    if (needsReflection && !isRuntime && !hasInitMethod) {
      printf("\n@interface %s : NSObject\n@end\n", typeName);
    }

    printInitFlagDefinition(node);
    printStaticVars(node);

    if (isRuntime || hasInitMethod || needsReflection) {
      syncLineNumbers(node.getName()); // avoid doc-comment
      printf("\n@implementation %s\n", typeName);

      if (isRuntime) {
        List<AnnotationTypeMemberDeclaration> members = Lists.newArrayList(
            Iterables.filter(node.getBodyDeclarations(), AnnotationTypeMemberDeclaration.class));
        printAnnotationProperties(members);
        if (!members.isEmpty()) {
          printAnnotationConstructor(node.getTypeBinding());
        }
        printAnnotationAccessors(members);
        println("\n- (IOSClass *)annotationType {");
        printf("  return %s_class_();\n", typeName);
        println("}");
      }
      printInitializeMethod(node);
      if (needsReflection) {
        printTypeAnnotationsMethod(node);
        printMetadata(node);
      }
      println("\n@end");
    }
  }

  private void printAnnotationConstructor(ITypeBinding annotation) {
    newline();
    print(annotationConstructorDeclaration(annotation));
    println(" {");
    println("  if ((self = [super init])) {");
    for (IMethodBinding member : annotation.getDeclaredMethods()) {
      String name = member.getName();
      printf("    %s = ", name);
      ITypeBinding type = member.getReturnType();
      boolean needsRetain = !type.isPrimitive();
      if (needsRetain) {
        print("RETAIN_(");
      }
      printf("%s_", name);
      if (needsRetain) {
        print(')');
      }
      println(";");
    }
    println("  }");
    println("  return self;");
    println("}");
  }

  private void printAnnotationAccessors(List<AnnotationTypeMemberDeclaration> members) {
    for (AnnotationTypeMemberDeclaration member : members) {
      Expression deflt = member.getDefault();
      if (deflt != null) {
        ITypeBinding type = member.getType().getTypeBinding();
        String typeString = NameTable.getSpecificObjCType(type);
        String propertyName = NameTable.getName(member.getName().getBinding());
        printf("\n+ (%s)%sDefault {\n", typeString, propertyName);
        printf("  return %s;\n", generateExpression(deflt));
        println("}");
      }
    }
  }

  private void generate(PackageDeclaration node) {
    List<Annotation> runtimeAnnotations = TreeUtil.getRuntimeAnnotationsList(node.getAnnotations());
    if (runtimeAnnotations.size() > 0 && TranslationUtil.needsReflection(node)) {
      printImports(getUnit());
      newline();
      String typeName = NameTable.getPrefix(node.getPackageBinding())
          + NameTable.PACKAGE_INFO_MAIN_TYPE;
      printf("@interface %s : NSObject\n", typeName);
      printf("@end\n\n");
      printf("@implementation %s\n", typeName);
      println("+ (IOSObjectArray *)__annotations {");
      printAnnotationCreate(runtimeAnnotations);
      println("\n@end");
    }
  }

  @Override
  protected void printNativeDeclaration(NativeDeclaration declaration) {
    newline();
    String code = declaration.getImplementationCode();
    if (code != null) {
      println(reindent(code));
    }
  }

  private void printMethods(TypeDeclaration node) {
    printDeclarations(node.getBodyDeclarations());
    printInitializeMethod(node);
  }

  private void printStaticInterface(AbstractTypeDeclaration node, String typeName) {
    boolean needsReflection = TranslationUtil.needsReflection(node);
    boolean needsImplementation = hasInitializeMethod(node) || needsReflection;
    if (needsImplementation && !hasInitializeMethod(node)) {
      printf("\n@interface %s : NSObject\n@end\n", typeName);
    }
    printInitFlagDefinition(node);
    printStaticVars(node);
    if (!needsImplementation) {
      return;
    }
    printf("\n@implementation %s\n", typeName);
    printInitializeMethod(node);
    if (needsReflection) {
      printMetadata(node);
    }
    println("\n@end");
    printFunctions(node.getBodyDeclarations());
  }

  @Override
  protected void generate(EnumDeclaration node) {
    List<EnumConstantDeclaration> constants = node.getEnumConstants();
    syncLineNumbers(node.getName()); // avoid doc-comment

    String typeName = NameTable.getFullName(node.getTypeBinding());
    printInitFlagDefinition(node);
    newline();
    printf("%s *%s_values_[%s];\n", typeName, typeName, constants.size());

    newline();
    printf("@implementation %s\n", typeName);
    printStaticVars(node);

    printDeclarations(node.getBodyDeclarations());
    printInitializeMethod(node);

    if (TranslationUtil.needsReflection(node)) {
      printTypeAnnotationsMethod(node);
      printMetadata(node);
    }
    println("\n@end");
    printFunctions(node.getBodyDeclarations());
  }

  private void printInitFlagDefinition(AbstractTypeDeclaration node) {
    ITypeBinding binding = node.getTypeBinding();
    String typeName = NameTable.getFullName(binding);
    if (hasInitializeMethod(node)) {
      printf("\nBOOL %s_initialized = NO;\n", typeName);
    }
  }

  @Override
  protected void printNormalMethod(MethodDeclaration m) {
    String methodBody = generateMethodBody(m);
    if (methodBody != null) {
      newline();
      syncLineNumbers(m.getName());  // avoid doc-comment
      print(super.methodDeclaration(m) + " " + reindent(methodBody) + "\n");
    }
  }

  @Override
  protected void printMappedMethodDeclaration(MethodDeclaration m, IOSMethod mappedMethod) {
    String methodBody = generateMethodBody(m);
    if (methodBody != null) {
      newline();
      println(super.mappedMethodDeclaration(m, mappedMethod) + " " + reindent(methodBody));
    }
  }

  private String generateMethodBody(MethodDeclaration m) {
    if (Modifier.isAbstract(m.getModifiers())) {
      return null;
    } else {
      // generate a normal method body
      return generateStatement(m.getBody(), /* isFunction */ false);
    }
  }

  private static int findConstructorInvocation(List<Statement> statements) {
    for (int i = 0; i < statements.size(); i++) {
      Statement stmt = statements.get(i);
      if (stmt instanceof ConstructorInvocation || stmt instanceof SuperConstructorInvocation) {
        return i;
      }
    }
    return -1;
  }

  @Override
  protected void printConstructor(MethodDeclaration m) {
    String methodBody;
    IMethodBinding binding = m.getMethodBinding();
    List<Statement> statements = m.getBody().getStatements();
    StringBuffer sb = new StringBuffer("{\n");
    int constructorIdx = findConstructorInvocation(statements);
    int idx = 0;
    while (idx < constructorIdx) {
      sb.append(generateStatement(statements.get(idx++), false));
    }
    String superCall =
        idx == constructorIdx ? generateStatement(statements.get(idx++), false) : "[super init]";
    if (idx >= statements.size()) {
      sb.append("return ");
      sb.append(superCall).append(";\n}");
    } else {
      sb.append("if (self = ").append(superCall).append(") {\n");
      while (idx < statements.size()) {
        sb.append(generateStatement(statements.get(idx++), false));
      }
      sb.append("}\nreturn self;\n}");
    }
    methodBody = sb.toString();
    newline();
    syncLineNumbers(m.getName());  // avoid doc-comment
    if (invokedConstructors.contains(methodKey(binding))) {
      print(super.constructorDeclaration(m, true) + " " + reindent(methodBody) + "\n\n");
      print(super.constructorDeclaration(m, false) + " {\n"
          + "  return " + generateStatement(createInnerConstructorInvocation(m), false) + ";\n}\n");
    } else {
      print(super.constructorDeclaration(m, false) + " " + reindent(methodBody) + "\n");
    }
  }

  private Statement createInnerConstructorInvocation(MethodDeclaration m) {
    ConstructorInvocation invocation = new ConstructorInvocation(m.getMethodBinding());
    for (SingleVariableDeclaration param : m.getParameters()) {
      invocation.getArguments().add(param.getName().copy());
    }
    return invocation;
  }

  private void printInitializeMethod(AbstractTypeDeclaration typeNode) {
    List<Statement> initStatements = typeNode.getClassInitStatements();
    if (initStatements.isEmpty()) {
      return;
    }
    String className = NameTable.getFullName(typeNode.getTypeBinding());
    StringBuffer sb = new StringBuffer();
    sb.append("{\nif (self == [" + className + " class]) {\n");
    for (Statement statement : initStatements) {
      sb.append(generateStatement(statement, false));
    }
    sb.append("J2OBJC_SET_INITIALIZED(" + className + ")\n");
    sb.append("}\n}");
    print("\n+ (void)initialize " + reindent(sb.toString()) + "\n");
  }

  private String generateStatement(Statement stmt, boolean asFunction) {
    return StatementGenerator.generate(stmt, asFunction, getBuilder().getCurrentLine());
  }

  private String generateExpression(Expression expr) {
    return StatementGenerator.generate(expr, false, getBuilder().getCurrentLine());
  }

  private void printImports(CompilationUnit node) {
    ImplementationImportCollector collector = new ImplementationImportCollector();
    collector.collect(node);
    Set<Import> imports = collector.getImports();

    Set<String> includeStmts = Sets.newTreeSet();
    includeStmts.add("#include \"J2ObjC_source.h\"");
    for (Import imp : imports) {
      includeStmts.add(String.format("#include \"%s.h\"", imp.getImportFileName()));
    }
    for (String stmt : includeStmts) {
      println(stmt);
    }

    for (NativeDeclaration decl : node.getNativeBlocks()) {
      printNativeDeclaration(decl);
    }
  }

  private void printStaticVars(AbstractTypeDeclaration node) {
    Iterable<VariableDeclarationFragment> fragments = getStaticFieldsNeedingInitialization(node);
    if (!Iterables.isEmpty(fragments)) {
      newline();
    }
    for (VariableDeclarationFragment var : fragments) {
      IVariableBinding binding = var.getVariableBinding();
      String name = NameTable.getStaticVarQualifiedName(binding);
      String objcType = NameTable.getObjCType(binding.getType());
      Expression initializer = var.getInitializer();
      if (initializer != null) {
        printf("%s %s = %s;\n", objcType, name, generateExpression(initializer));
      } else {
        printf("%s %s;\n", objcType, name);
      }
    }
  }

  private void printFinalFunctionDecls(List<AbstractTypeDeclaration> types) {
    boolean needsNewLine = true;
    for (AbstractTypeDeclaration type : types) {
      for (FunctionDeclaration function : TreeUtil.getFunctionDeclarations(type)) {
        int modifiers = function.getModifiers();
        if (!Modifier.isPrivate(modifiers)) {
          // Declaration is defined in header file.
          continue;
        }
        if (needsNewLine) {
          newline();
          needsNewLine = false;
        }
        // We expect native functions to be defined externally.
        if (!Modifier.isNative(modifiers)) {
          print("__attribute__((unused)) static ");
        }
        println(getFunctionSignature(function) + ";");
      }
    }
  }

  @Override
  protected void printFunction(FunctionDeclaration function) {
    if (Modifier.isNative(function.getModifiers())) {
      return;
    }
    String functionBody = generateStatement(function.getBody(), /* isFunction */ true);
    newline();
    println(getFunctionSignature(function) + " " + reindent(functionBody));
  }

  private void printAnnotationProperties(List<AnnotationTypeMemberDeclaration> members) {
    if (!members.isEmpty()) {
      newline();
    }
    for (AnnotationTypeMemberDeclaration member : members) {
      println(String.format("@synthesize %s;", NameTable.getName(member.getName().getBinding())));
    }
  }

  private void printTypeAnnotationsMethod(AbstractTypeDeclaration decl) {
    List<Annotation> runtimeAnnotations = TreeUtil.getRuntimeAnnotationsList(decl.getAnnotations());
    if (runtimeAnnotations.size() > 0) {
      println("\n+ (IOSObjectArray *)__annotations {");
      printAnnotationCreate(runtimeAnnotations);
    }
  }

  private void printMethodAnnotationMethods(Iterable<MethodDeclaration> methods) {
    for (MethodDeclaration method : methods) {
      List<Annotation> runtimeAnnotations =
          TreeUtil.getRuntimeAnnotationsList(method.getAnnotations());
      if (runtimeAnnotations.size() > 0) {
        printf("\n+ (IOSObjectArray *)__annotations_%s {\n", methodKey(method.getMethodBinding()));
        printAnnotationCreate(runtimeAnnotations);
      }
      printParameterAnnotationMethods(method);
    }
  }

  private void printParameterAnnotationMethods(MethodDeclaration method) {
    List<SingleVariableDeclaration> params = method.getParameters();

    // Quick test to see if there are any parameter annotations.
    boolean hasAnnotations = false;
    for (SingleVariableDeclaration param : params) {
      if (!Iterables.isEmpty(TreeUtil.getRuntimeAnnotations(param.getAnnotations()))) {
        hasAnnotations = true;
        break;
      }
    }

    if (hasAnnotations) {
      // Print array of arrays, with an element in the outer array for each parameter.
      printf("\n+ (IOSObjectArray *)__annotations_%s_params {\n",
          methodKey(method.getMethodBinding()));
      print("  return [IOSObjectArray arrayWithObjects:(id[]) { ");
      for (int i = 0; i < params.size(); i++) {
        if (i > 0) {
          print(", ");
        }
        SingleVariableDeclaration param = params.get(i);
        List<Annotation> runtimeAnnotations =
            TreeUtil.getRuntimeAnnotationsList(param.getAnnotations());
        if (runtimeAnnotations.size() > 0) {
          print("[IOSObjectArray arrayWithObjects:(id[]) { ");
          printAnnotations(runtimeAnnotations);
          printf(" } count:%d type:JavaLangAnnotationAnnotation_class_()]",
                 runtimeAnnotations.size());
        } else {
          print("[IOSObjectArray arrayWithLength:0 type:JavaLangAnnotationAnnotation_class_()]");
        }
      }
      printf(" } count:%d type:[IOSClass arrayClassWithComponentType:"
          + "JavaLangAnnotationAnnotation_class_()]];\n}\n", params.size());
    }
  }

  private void printFieldAnnotationMethods(AbstractTypeDeclaration node) {
    for (FieldDeclaration field : TreeUtil.getFieldDeclarations(node)) {
      List<Annotation> runtimeAnnotations =
          TreeUtil.getRuntimeAnnotationsList(field.getAnnotations());
      if (!runtimeAnnotations.isEmpty()) {
        for (VariableDeclarationFragment var : field.getFragments()) {
          printf("\n+ (IOSObjectArray *)__annotations_%s_ {\n", var.getName().getIdentifier());
          printAnnotationCreate(runtimeAnnotations);
        }
      }
    }
  }

  private void printAnnotationCreate(List<Annotation> runtimeAnnotations) {
    print("  return [IOSObjectArray arrayWithObjects:(id[]) { ");
    printAnnotations(runtimeAnnotations);
    printf(" } count:%d type:JavaLangAnnotationAnnotation_class_()];\n}\n",
           runtimeAnnotations.size());
  }

  private void printAnnotations(Iterable<Annotation> runtimeAnnotations) {
    boolean first = true;
    for (Annotation annotation : runtimeAnnotations) {
      if (first) {
        first = false;
      } else {
        print(", ");
      }
      if (Options.useReferenceCounting()) {
        print('[');
      }
      printf("[[%s alloc] init", NameTable.getFullName(
          annotation.getAnnotationBinding().getAnnotationType()));
      printAnnotationParameters(annotation);
      print(']');
      if (Options.useReferenceCounting()) {
        print(" autorelease]");
      }
    }
  }

  // Prints an annotation's values as a constructor argument list. If
  // the annotation type declares default values, then for any value that
  // isn't specified in the annotation will use the default.
  private void printAnnotationParameters(Annotation annotation) {
    IAnnotationBinding binding = annotation.getAnnotationBinding();
    IMemberValuePairBinding[] valueBindings = BindingUtil.getSortedMemberValuePairs(binding);
    for (int i = 0; i < valueBindings.length; i++) {
      if (i > 0) {
        print(' ');
      }
      IMemberValuePairBinding valueBinding = valueBindings[i];
      print(i == 0 ? "With" : "with");
      printf("%s:", NameTable.capitalize(valueBinding.getName()));
      Object value = valueBinding.getValue();
      printAnnotationValue(value);
    }
  }

  private void printAnnotationValue(Object value) {
    if (value == null) {
      print("nil");
    } else if (value instanceof IVariableBinding) {
      IVariableBinding var = (IVariableBinding) value;
      ITypeBinding declaringClass = var.getDeclaringClass();
      printf("%s_get_%s()", NameTable.getFullName(declaringClass), var.getName());
    } else if (value instanceof ITypeBinding) {
      printf("%s_class_()", NameTable.getFullName((ITypeBinding) value));
    } else if (value instanceof String) {
      StringLiteral node = new StringLiteral((String) value);
      print(StatementGenerator.generateStringLiteral(node));
    } else if (value instanceof Number || value instanceof Character || value instanceof Boolean) {
      print(value.toString());
    } else if (value.getClass().isArray()) {
      print("[IOSObjectArray arrayWithObjects:(id[]) { ");
      Object[] array = (Object[]) value;
      for (int i = 0; i < array.length; i++) {
        if (i > 0) {
          print(", ");
        }
        printAnnotationValue(array[i]);
      }
      printf(" } count:%d type:[[NSObject class] getClass]]", array.length);
    } else {
      assert false : "unknown annotation value type";
    }
  }

  private void printMetadata(AbstractTypeDeclaration node) {
    print(new MetadataGenerator(node).getMetadataSource());
  }

  private boolean hasPrivateFields(Iterable<FieldDeclaration> fields) {
    for (FieldDeclaration f : fields) {
      if (isPrivateOrSynthetic(f.getModifiers())) {
        return true;
      }
    }
    return false;
  }

  private List<MethodDeclaration> getPrivateMethodsToDeclare(AbstractTypeDeclaration typeNode) {
    List<MethodDeclaration> methods = Lists.newArrayList();
    for (MethodDeclaration method : TreeUtil.getMethodDeclarations(typeNode)) {
      int modifiers = method.getModifiers();
      // Synthetic methods don't need to be declared.
      if (Modifier.isPrivate(modifiers) && !BindingUtil.isSynthetic(modifiers)) {
        methods.add(method);
      }
    }
    return methods;
  }

  private void printClassExtensions(List<AbstractTypeDeclaration> types) {
    for (AbstractTypeDeclaration type : types) {
      if (type.getTypeBinding().isClass() || type.getTypeBinding().isEnum()) {
        printClassExtension(type);
      }
    }
  }

  private void printClassExtension(AbstractTypeDeclaration node) {
    if (Options.hidePrivateMembers()) {
      List<FieldDeclaration> fields = TreeUtil.getFieldDeclarationsList(node);
      boolean hasPrivateFields = hasPrivateFields(fields);
      List<MethodDeclaration> privateMethods = getPrivateMethodsToDeclare(node);
      if (!privateMethods.isEmpty() || hasPrivateFields) {
        String typeName = NameTable.getFullName(node.getTypeBinding());
        newline();
        printf("@interface %s ()", typeName);
        if (hasPrivateFields) {
          println(" {");
          printInstanceVariables(node, true);
          println("}");
        } else {
          newline();
        }
        for (MethodDeclaration m : privateMethods) {
          if (isPrivateOrSynthetic(m.getModifiers())) {
            IMethodBinding binding = m.getMethodBinding();
            IOSMethod iosMethod = IOSMethodBinding.getIOSMethod(binding);
            if (iosMethod != null) {
              println(super.mappedMethodDeclaration(m, iosMethod) + ";");
            } else if (m.isConstructor()) {
              println(constructorDeclaration(m) + ";");
            } else {
              printNormalMethodDeclaration(m);
            }
          }
        }
        println("@end");
        printFieldSetters(node, true);
      }
    }
  }
}
