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

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.J2ObjC.Language;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.ImplementationImportCollector;
import com.google.devtools.j2objc.types.ImportCollector;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.List;
import java.util.Set;

/**
 * Generates Objective-C implementation (.m) files from compilation units.
 *
 * @author Tom Ball
 */
public class ObjectiveCImplementationGenerator extends ObjectiveCSourceFileGenerator {
  private Set<IVariableBinding> fieldHiders;
  private final String suffix;
  private Set<String> invokedConstructors = Sets.newHashSet();

  /**
   * Generate an Objective-C implementation file for each type declared in a
   * specified compilation unit.
   */
  public static void generate(String fileName, Language language, CompilationUnit unit,
      String source) {
    ObjectiveCImplementationGenerator implementationGenerator =
        new ObjectiveCImplementationGenerator(fileName, language, unit, source);
    implementationGenerator.generate(unit);
  }

  private ObjectiveCImplementationGenerator(String fileName, Language language,
      CompilationUnit unit, String source) {
    super(fileName, source, unit, Options.emitLineDirectives());
    fieldHiders = HiddenFieldDetector.getFieldNameConflicts(unit);
    suffix = language.getSuffix();
  }

  @Override
  protected String getSuffix() {
    return suffix;
  }

  public void generate(CompilationUnit unit) {
    println(J2ObjC.getFileHeader(getSourceFileName()));
    if (needsPrinting(unit)) {
      findInvokedConstructors(unit);
      printStart(getSourceFileName());
      printImports(unit);
      unit.accept(new ErrorReportingASTVisitor() {
        @Override
        public boolean visit(TypeDeclaration node) {
          generate(node);
          return true;
        }

        @Override
        public boolean visit(EnumDeclaration node) {
          generate(node);
          return true;
        }
      });
    } else {
      // Print a dummy C function so compiled object file is valid.
      @SuppressWarnings("unchecked")
      List<AbstractTypeDeclaration> types = unit.types(); // safe by definition
      if (!types.isEmpty()) {
        printf("void %s_unused() {}\n", NameTable.getFullName(types.get(0)));
      }
    }
    save(unit);
  }

  private boolean needsPrinting(CompilationUnit unit) {
    final boolean[] result = { false };
    unit.accept(new ErrorReportingASTVisitor() {
      @Override
      public boolean visit(TypeDeclaration node) {
        if (!node.isInterface()) {
          result[0] = true;  // always print concrete types
          return false;
        }
        return true;
      }

      @Override
      public boolean visit(EnumDeclaration node) {
        result[0] = true; // always print enums
        return false;
      }

      @Override
      public boolean visit(AnnotationTypeDeclaration node) {
        return false;  // never print annotations
      }

      @Override
      public void endVisit(MethodDeclaration node) {
        // Only print protocols if they have static constants.
        if (isInterfaceConstantAccessor(Types.getMethodBinding(node))) {
          result[0] = true;
        }
      }
    });
    return result[0];
  }

  private String constructorKey(IMethodBinding constructor) {
    StringBuilder sb = new StringBuilder();
    for (ITypeBinding type : constructor.getParameterTypes()) {
      sb.append(NameTable.javaRefToObjC(type) + ":");
    }
    return sb.toString();
  }

  private void findInvokedConstructors(CompilationUnit unit) {
    unit.accept(new ErrorReportingASTVisitor() {
      @Override
      public boolean visit(ConstructorInvocation node) {
        invokedConstructors.add(constructorKey(Types.getMethodBinding(node)));
        return false;
      }
    });
  }

  @Override
  public void generate(TypeDeclaration node) {
    List<IMethodBinding> testMethods = null;
    if (Types.isJUnitTest(Types.getTypeBinding(node))) {
       testMethods = findTestMethods(node);
    }
    syncLineNumbers(node.getName()); // avoid doc-comment

    fieldHiders = HiddenFieldDetector.getFieldNameConflicts(node);
    if (node.isInterface()) {
      printStaticInterface(node);
    } else {
      String typeName = NameTable.getFullName(node);
      printf("@implementation %s\n\n", typeName);
      List<FieldDeclaration> fields = Lists.newArrayList(node.getFields());
      printStaticReferencesMethod(fields);
      printStaticVars(Lists.newArrayList(node.getFields()), /* isInterface */ false);
      printProperties(node.getFields());
      printMethods(node);
      printObjCTypeMethod(node);

      println("@end");

      // Generate main method, if declared.
      MethodDeclaration main = null;
      for (MethodDeclaration m : node.getMethods()) {
        if (isMainMethod(m)) {
          main = m;
          break;
        }
      }
      if (main != null || (testMethods != null && Options.generateTestMain())) {
        newline();
        printMainMethod(main, typeName, testMethods);
      }
    }
  }

  private List<IMethodBinding> findTestMethods(TypeDeclaration node) {
    ITypeBinding type = Types.getTypeBinding(node);
    List<IMethodBinding> tests = Lists.newArrayList();
    while (type != null) {
      for (IMethodBinding md : type.getDeclaredMethods()) {
        int modifiers = md.getModifiers();
        if (Modifier.isPublic(modifiers)) {
          if (md.getName().startsWith("test") && md.getParameterTypes().length == 0) {
            tests.add(md);
          }
        }
      }
      type = type.getSuperclass();
    }
    return tests.isEmpty() ? null : tests;
  }

  @Override
  protected void generate(AnnotationTypeDeclaration node) {
    // No implementation for annotations.
  }

  private void printMethods(TypeDeclaration node) {
    printMethods(Lists.newArrayList(node.getMethods()));

    // If node implements CharSequence, add forwarding method from the
    // sequenceDescription method to description (toString()).  See
    // JavaToIOSMethodTranslator.loadCharSequenceMethods() for details.
    ITypeBinding binding = Types.getTypeBinding(node);
    for (ITypeBinding interfaze : binding.getInterfaces()) {
      if (interfaze.getQualifiedName().equals("java.lang.CharSequence")) {
        println("- (NSString *)description {\n  return [self sequenceDescription];\n}\n");
      }
    }

    // If node defines a primitive number wrapper, add a getValue() method.
    // This is required by iOS 5.0 to support cloning these types.
    if (Types.isJavaNumberType(binding)) {
      ITypeBinding primitiveType = Types.getPrimitiveType(binding);
      if (primitiveType != null) {
        // All java.lang primitive type wrappers have a "value" field.
        printf("- (void)getValue:(void *)buffer {\n  *((%s *) buffer) = value_;\n}\n\n",
            NameTable.getFullName(primitiveType));
      }
    }

    List<VariableDeclarationFragment> properties = getProperties(node.getFields());
    if (properties.size() > 0) {
      printCopyAllPropertiesMethod(NameTable.getFullName(node), properties);
      printStrongReferencesMethod(properties);
    }
  }

  private List<VariableDeclarationFragment> getProperties(FieldDeclaration[] fields) {
    List<VariableDeclarationFragment> properties = Lists.newArrayList();
    for (FieldDeclaration field : fields) {
      if (!Modifier.isStatic(field.getModifiers())) {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fragments = field.fragments(); // safe by definition
        properties.addAll(fragments);
      }
    }
    return properties;
  }

  private void printCopyAllPropertiesMethod(
      String typeName, List<VariableDeclarationFragment> properties) {
    println("- (void)copyAllPropertiesTo:(id)copy {");
    println("  [super copyAllPropertiesTo:copy];");
    println(String.format("  %s *typedCopy = (%s *) copy;", typeName, typeName));
    for (VariableDeclarationFragment property : properties) {
      String propName = NameTable.getName(property.getName());
      String objCFieldName = NameTable.javaFieldToObjC(propName);
      println(String.format("  typedCopy.%s = %s;", propName, objCFieldName));
    }
    println("}\n");
  }

  // Returns whether the property is a strong reference.
  private boolean isStrongReferenceProperty(VariableDeclarationFragment property) {
    IVariableBinding varBinding = Types.getVariableBinding(property);
    ITypeBinding type = Types.getTypeBinding(property);
    return !type.isPrimitive() && !Types.hasWeakAnnotation(varBinding.getDeclaringClass()) &&
        !Types.isWeakReference(varBinding);
  }

  // We generate the runtime debug method -memDebugStrongReferences.
  // This method will return an array of information about a strong reference,
  // including pointer to object and name.
  private void printStrongReferencesMethod(List<VariableDeclarationFragment> properties) {
    if (Options.memoryDebug()) {
      if (!Options.useReferenceCounting()) {
        println("- (NSArray *)memDebugStrongReferences {");
        println("  return nil;");
        println("}");
        return;
      }
      println("- (NSArray *)memDebugStrongReferences {");
      println("  NSMutableArray *result =");
      println("      [[[super memDebugStrongReferences] mutableCopy] autorelease];");
      for (VariableDeclarationFragment property : properties) {
        String propName = NameTable.getName(property.getName());
        String objCFieldName = NameTable.javaFieldToObjC(propName);
        if (isStrongReferenceProperty(property)) {
          println(String.format("  [result addObject:[JreMemDebugStrongReference " +
              "strongReferenceWithObject:%s name:@\"%s\"]];", objCFieldName, propName));
        }
      }
      println("  return result;");
      println("}\n");
    }
  }

  // Returns whether the static property a strong reference.
  private boolean isStrongStaticProperty(VariableDeclarationFragment var) {
    IVariableBinding binding = Types.getVariableBinding(var);
    if (!Types.isPrimitiveConstant(binding)) {
      if (!Types.isWeakReference(binding)) {
        ITypeBinding type = Types.getTypeBinding(var);
        if (!type.isPrimitive()) {
          return true;
        }
      }
    }
    return false;
  }

  private void printStaticReferencesMethod(List<FieldDeclaration> fields) {
    printStaticReferencesMethod(fields, null);
  }

  // We generate the runtime debug method +memDebugStaticReferences.
  // This method will return an array of NSNumber containing pointers (casted into unsigned long)
  // to the objects referenced by a class variable with a strong reference.
  // It will be useful for debug purpose.
  //
  // Arrays returned by -memDebugStaticReferences and -memDebugStaticReferencesNames (see below)
  // must be the same size.
  //
  // In case of a Java enum, valuesVarNameis the name of the array of enum values.
  private void printStaticReferencesMethod(List<FieldDeclaration> fields, String valuesVarName) {
    if (Options.memoryDebug()) {
      if (!Options.useReferenceCounting()) {
        println("+ (NSArray *)memDebugStaticReferences {");
        println("  return nil;");
        println("}");
        return;
      }
      println("+ (NSArray *)memDebugStaticReferences {");
      println("  NSMutableArray *result = [NSMutableArray array];");
      for (FieldDeclaration f : fields) {
        if (Modifier.isStatic(f.getModifiers())) {
          @SuppressWarnings("unchecked")
          List<VariableDeclarationFragment> fragments = f.fragments(); // safe by specification
          for (VariableDeclarationFragment var : fragments) {
            if (isStrongStaticProperty(var)) {
              IVariableBinding binding = Types.getVariableBinding(var);
              String name = NameTable.getName(binding);
              println(String.format("  [result addObject:[JreMemDebugStrongReference " +
                  "strongReferenceWithObject:%s name:@\"%s\"]];", name, name));
            }
          }
        }
      }
      if (valuesVarName != null) {
        println(String.format("  [result addObject:[JreMemDebugStrongReference " +
            "strongReferenceWithObject:%s name:@\"enumValues\"]];", valuesVarName));
      }
      println("  return result;");
      println("}\n");
    }
  }

  private void printStaticInterface(TypeDeclaration node) {
    // Print implementation for static constants, if any.
    boolean needsPrinting = false;
    List<MethodDeclaration> methods = Lists.newArrayList(node.getMethods());
    for (MethodDeclaration m : methods) {
      if (isInterfaceConstantAccessor(Types.getMethodBinding(m))) {
        needsPrinting = true;
        break;
      }
    }

    if (needsPrinting) {
      printf("\n@implementation %s\n\n", NameTable.getFullName(node));
      printStaticVars(Lists.newArrayList(node.getFields()), /* isInterface */ true);
      for (MethodDeclaration m : methods) {
        IMethodBinding binding = Types.getMethodBinding(m);
        if (binding.isSynthetic() || isInterfaceConstantAccessor(binding)) {
          printMethod(m);
        }
      }
      println("@end");
    }
  }

  @Override
  protected void generate(EnumDeclaration node) {
    @SuppressWarnings("unchecked")
    List<EnumConstantDeclaration> constants = node.enumConstants(); // safe by definition
    List<MethodDeclaration> methods = Lists.newArrayList();
    List<FieldDeclaration> fields = Lists.newArrayList();
    MethodDeclaration initializeMethod = null;
    @SuppressWarnings("unchecked")
    List<BodyDeclaration> declarations = node.bodyDeclarations(); // safe by definition
    for (BodyDeclaration decl : declarations) {
      if (decl instanceof FieldDeclaration) {
        fields.add((FieldDeclaration) decl);
      } else if (decl instanceof MethodDeclaration) {
        MethodDeclaration md = (MethodDeclaration) decl;
        if (md.getName().getIdentifier().equals("initialize")) {
          initializeMethod = md;
        } else {
          methods.add(md);
        }
      }
    }
    syncLineNumbers(node.getName()); // avoid doc-comment

    String typeName = NameTable.getFullName(node);
    for (EnumConstantDeclaration constant : constants) {
      printf("static %s *%s_%s;\n", typeName, typeName, NameTable.getName(constant.getName()));
    }
    printf("IOSObjectArray *%s_values;\n", typeName);
    newline();

    printf("@implementation %s\n\n", typeName);
    printStaticVars(fields, /* isInterface */ false);
    printStaticReferencesMethod(fields, typeName + "_values");

    for (EnumConstantDeclaration constant : constants) {
      String name = NameTable.getName(constant.getName());
      printf("+ (%s *)%s {\n", typeName, name);
      printf("  return %s_%s;\n", typeName, name);
      println("}");
    }
    newline();

    // Enum constants needs to implement NSCopying.  Being singletons, they
    // can just return self, as long the retain count is incremented.
    String selfString = Options.useReferenceCounting() ? "[self retain]" : "self";
    printf("- (id)copyWithZone:(NSZone *)zone {\n  return %s;\n}\n\n", selfString);

    printProperties(fields.toArray(new FieldDeclaration[0]));
    printMethods(methods);

    printf("+ (void)initialize {\n  if (self == [%s class]) {\n", typeName);
    for (int i = 0; i < constants.size(); i++) {
      EnumConstantDeclaration constant = constants.get(i);
      @SuppressWarnings("unchecked")
      List<Expression> args = constant.arguments(); // safe by definition
      String name = NameTable.getName(constant.getName());
      String constantTypeName =
          NameTable.getFullName(Types.getMethodBinding(constant).getDeclaringClass());
      printf("    %s_%s = [[%s alloc] init", typeName, name, constantTypeName);
      boolean isSimpleEnum = constantTypeName.equals(typeName);

      // Common-case: no extra fields and no constant anonymous classes.
      if (args.isEmpty() && isSimpleEnum) {
        printf("WithNSString:@\"%s\" withInt:%d];\n", name, i);
      } else {
        String argString = StatementGenerator.generateArguments(Types.getMethodBinding(constant),
            args, fieldHiders, getBuilder().getSourcePosition());
        print(argString);
        if (args.isEmpty()) {
          print("With");
        } else {
          print(" with");
        }
        printf("NSString:@\"%s_%s\" withInt:%d];\n", typeName.replace("Enum", ""), name, i);
      }
    }
    printf("    %s_values = [[IOSObjectArray alloc] initWithObjects:(id[]){ ", typeName);
    for (EnumConstantDeclaration constant : constants) {
      printf("%s_%s, ", typeName, NameTable.getName(constant.getName()));
    }
    printf("nil } count:%d type:[IOSClass classWithClass:[%s class]]];\n",
        constants.size(), typeName);
    if (initializeMethod != null) {
      @SuppressWarnings("unchecked")
      List<Statement> stmts = initializeMethod.getBody().statements(); // safe by definition
      for (Statement s : stmts) {
        printf("    %s", StatementGenerator.generate(s, fieldHiders, false,
            getBuilder().getSourcePosition()));
      }
    }
    println("  }\n}\n");

    // Print generated values and valueOf methods.
    println("+ (IOSObjectArray *)values {");
    printf("  return [IOSObjectArray arrayWithArray:%s_values];\n", typeName);
    println("}\n");
    printf("+ (%s *)valueOfWithNSString:(NSString *)name {\n", typeName);
    printf("  for (int i = 0; i < [%s_values count]; i++) {\n", typeName);
    printf("    %s *e = [%s_values objectAtIndex:i];\n", typeName, typeName);
    printf("    if ([name isEqual:[e name]]) {\n");
    printf("      return e;\n");
    printf("    }\n");
    printf("  }\n");
    if (Options.useReferenceCounting()) {
      printf("  @throw [[[JavaLangIllegalArgumentException alloc] initWithNSString:name]"
           + " autorelease];\n");
    } else {
      printf("  @throw [[JavaLangIllegalArgumentException alloc] initWithNSString:name];\n");
    }
    printf("  return nil;\n");
    println("}\n");

    println("@end");
  }

  @Override
  protected String methodDeclaration(MethodDeclaration m) {
    int modifiers = m.getModifiers();
    if ((modifiers & Modifier.NATIVE) > 0) {
      if (hasNativeCode(m)) {
        return super.methodDeclaration(m) + " " + extractNativeMethodBody(m) + "\n\n";
      } else if (Options.generateNativeStubs()) {
        return super.methodDeclaration(m) + " " + generateNativeStub(m) + "\n\n";
      } else {
        return "";
      }
    }
    String methodBody = generateMethodBody(m);
    return super.methodDeclaration(m) + " " + reindent(methodBody) + "\n\n";
  }

  private String generateNativeStub(MethodDeclaration m) {
    IMethodBinding binding = Types.getMethodBinding(m);
    String methodName = NameTable.getName(binding);
    return String.format("{\n  @throw \"%s method not implemented\";\n}", methodName);
  }

  @Override
  protected String mappedMethodDeclaration(MethodDeclaration method, IOSMethod mappedMethod) {
    String methodBody;
    if ((method.getModifiers() & Modifier.NATIVE) > 0) {
      if (hasNativeCode(method)) {
        methodBody = extractNativeMethodBody(method);
      } else {
        return "";
      }
    } else {
      methodBody = generateMethodBody(method);
    }
    return super.mappedMethodDeclaration(method, mappedMethod)
        + " " + reindent(methodBody) + "\n\n";
  }

  private String generateMethodBody(MethodDeclaration m) {
    if (Modifier.isAbstract(m.getModifiers())) {
      // Generate a body which throws a NSInvalidArgumentException.
      String body =
          "{\n // can't call an abstract method\n " +
              "[self doesNotRecognizeSelector:_cmd];\n ";
      if (!Types.isVoidType(m.getReturnType2())) {
        body += "return 0;\n"; // Never executes, but avoids a gcc warning.
      }
      return body + "}";
    }
    // generate a normal method body
    String methodBody = generateStatement(m.getBody(), false);

    boolean isStatic = (m.getModifiers() & Modifier.STATIC) != 0;
    boolean isSynchronized = (m.getModifiers() & Modifier.SYNCHRONIZED) != 0;
    if (isStatic && isSynchronized) {
      methodBody = reindent("{\n@synchronized([self class]) {\n" + methodBody + "}\n}\n");
    } else if (isSynchronized) {
      methodBody = reindent("{\n@synchronized(self) {\n" + methodBody + "}\n}\n");
    }

    return methodBody;
  }

  @Override
  protected String getParameterName(SingleVariableDeclaration param) {
    String name = super.getParameterName(param);
    IVariableBinding binding = param.resolveBinding();
    return binding != null && fieldHiders.contains(binding) ? name + "Arg" : name;
  }

  @Override
  protected String constructorDeclaration(MethodDeclaration m) {
    String methodBody;
    IMethodBinding binding = Types.getMethodBinding(m);
    boolean memDebug = Options.memoryDebug();
    @SuppressWarnings("unchecked")
    List<Statement> statements = m.getBody().statements();
    if (binding.getDeclaringClass().isEnum()) {
      return enumConstructorDeclaration(m, statements, binding);
    } else if (statements.isEmpty()) {
      methodBody = memDebug ?
          "{\nreturn (self = JreMemDebugAdd([super init]));\n}" :
          "{\nreturn (self = [super init]);\n}";
    } else if (statements.size() == 1 &&
        (statements.get(0) instanceof ConstructorInvocation ||
         statements.get(0) instanceof SuperConstructorInvocation)) {
      if (memDebug) {
        methodBody = "{\nreturn JreMemDebugAdd(" +
            generateStatement(statements.get(0), false, true) + ");\n}";
      } else {
        methodBody = "{\nreturn " + generateStatement(statements.get(0), false, true) + ";\n}";
      }
    } else {
      StringBuffer sb = new StringBuffer();
      Statement first = statements.get(0);
      boolean firstPrinted = false;
      sb.append("{\nif ((self = ");
      if (first instanceof ConstructorInvocation ||
          first instanceof SuperConstructorInvocation) {
        sb.append(generateStatement(first, false, true));
        firstPrinted = true;
      } else {
        sb.append("[super init]");
      }
      sb.append(")) {\n");
      for (int i = firstPrinted ? 1 : 0; i < statements.size(); i++) {
        sb.append(generateStatement(statements.get(i), false, true));
      }
      if (memDebug) {
        sb.append("JreMemDebugAdd(self);\n");
      }
      sb.append("}\nreturn self;\n}");
      methodBody = sb.toString();
    }
    if (invokedConstructors.contains(constructorKey(binding))) {
      return super.constructorDeclaration(m, true) + " " + reindent(methodBody) + "\n\n"
          + super.constructorDeclaration(m, false) + " {\n  return "
          + generateStatement(createInnerConstructorInvocation(m), false) + ";\n}\n\n";
    } else {
      return super.constructorDeclaration(m, false) + " " + reindent(methodBody) + "\n\n";
    }
  }

  private Statement createInnerConstructorInvocation(MethodDeclaration m) {
    ConstructorInvocation invocation = m.getAST().newConstructorInvocation();
    Types.addBinding(invocation, Types.getBinding(m));
    @SuppressWarnings("unchecked")
    List<Expression> arguments = invocation.arguments();
    @SuppressWarnings("unchecked")
    List<SingleVariableDeclaration> params = m.parameters();
    for (SingleVariableDeclaration param : params) {
      SimpleName paramName = param.getName();
      IVariableBinding paramBinding = Types.getVariableBinding(paramName);
      SimpleName argName = m.getAST().newSimpleName(paramName.getIdentifier());
      Types.addBinding(argName, paramBinding);
      arguments.add(argName);
    }
    return invocation;
  }

  private String enumConstructorDeclaration(MethodDeclaration m, List<Statement> statements,
      IMethodBinding binding) {
    assert !statements.isEmpty();

    // Append enum generated parameters to invocation.  The
    // InitializationNormalizer should have fixed this constructor so the
    // first statement is a constructor or super invocation.
    Statement s = statements.get(0);
    assert s instanceof ConstructorInvocation || s instanceof SuperConstructorInvocation;
    String invocation = generateStatement(statements.get(0), false, true) + ";\n";
    List<?> args = s instanceof ConstructorInvocation
        ? ((ConstructorInvocation) s).arguments() : ((SuperConstructorInvocation) s).arguments();
    String impliedArgs = (args.isEmpty() ? "W" : " w") + "ithNSString:name withInt:ordinal";
    int index = invocation.lastIndexOf(']');
    invocation = invocation.substring(0, index) + impliedArgs + ']';

    StringBuffer sb = new StringBuffer();
    boolean memDebug = Options.memoryDebug();
    if (statements.size() == 1) {
      sb.append("{\nreturn ");
      if  (memDebug) {
        sb.append("JreMemDebugAdd(" + invocation + ")");
      } else {
        sb.append(invocation);
      }
      sb.append(";\n}");
    } else {
      sb.append("{\nif ((self = ");
      sb.append(invocation);
      sb.append(")) {\n");
      for (int i = 1; i < statements.size(); i++) {
        sb.append(generateStatement(statements.get(i), false, true));
      }
      if (memDebug) {
        sb.append("JreMemDebugAdd(self);\n");
      }
      sb.append("}\nreturn self;\n}");
    }
    String result = super.constructorDeclaration(m) + " " + reindent(sb.toString()) + "\n\n";
    return result;
  }

  @Override
  protected void printStaticConstructorDeclaration(MethodDeclaration m) {
    String className =
        NameTable.javaTypeToObjC(Types.getMethodBinding(m).getDeclaringClass(), false);
    StringBuffer sb = new StringBuffer();
    sb.append("{\nif (self == [" + className + " class]) {\n");
    @SuppressWarnings("unchecked")
    List<Statement> statements = m.getBody().statements();
    for (Statement statement : statements) {
      sb.append(generateStatement(statement, false, true));
    }
    sb.append("}\n}");
    print("+ (void)initialize " + reindent(sb.toString()) + "\n\n");
  }

  private String generateStatement(Statement stmt, boolean asFunction, boolean inConstructor) {
    return StatementGenerator.generate(stmt, fieldHiders, asFunction,
        getBuilder().getSourcePosition());
  }

  private String generateStatement(Statement stmt, boolean asFunction) {
    return StatementGenerator.generate(stmt, fieldHiders, asFunction,
        getBuilder().getSourcePosition());
  }

  private String generateExpression(Expression expr) {
    return StatementGenerator.generate(expr, fieldHiders, false,
        getBuilder().getSourcePosition());
  }

  private void printMainMethod(MethodDeclaration m, String typeName,
      List<IMethodBinding> testMethods) {
    if (m != null) {  // True for unit tests.
      Types.addFunction(Types.getMethodBinding(m));
    }
    println("int main( int argc, const char *argv[] ) {");
    if (m != null && (m.getModifiers() & Modifier.NATIVE) > 0 && hasNativeCode(m)) {
      println(extractNativeMethodBody(m));
      return;
    }
    indent();
    printIndent();
    println("int exitCode = 0;");
    if (Options.memoryDebug()) {
      printIndent();
      println("JreMemDebugEnabled = TRUE;");
    }
    if (!Options.useGC()) {
      printIndent();
      println("@autoreleasepool {");
      indent();
    }

    if (m != null) {
      @SuppressWarnings("unchecked")
      List<SingleVariableDeclaration> params = m.parameters();
      assert params.size() == 1;  // Previously checked in isMainMethod().
      printIndent();
      printf("IOSObjectArray *%s = JreEmulationMainArguments(argc, argv);\n\n",
          params.get(0).getName().getIdentifier());
      printMethodBody(m, true);
    }
    if (testMethods != null) {
      printIndent();
      printf("exitCode = [JUnitRunner runTests:[%s class]", typeName);
      for (IMethodBinding test : testMethods) {
        printf(", @\"%s\"", test.getName());
      }
      println(", nil];");
    }
    if (!Options.useGC()) {
      unindent();
      printIndent();
      println("}");
    }
    if (Options.memoryDebug()) {
      printIndent();
      println("JreMemDebugGenerateAllocationsReport();");
    }
    printIndent();
    println("return exitCode;");
    unindent();
    println("}");
  }

  private void printMethodBody(MethodDeclaration m, boolean isFunction) throws AssertionError {
    for (Object stmt : m.getBody().statements()) {
      if (stmt instanceof Statement) {
        String objcStmt = reindent(generateStatement((Statement) stmt, isFunction));
        println(objcStmt);
      } else {
        throw new AssertionError("unexpected AST type: " + stmt.getClass());
      }
    }
  }

  private String extractNativeMethodBody(MethodDeclaration m) {
    assert (m.getModifiers() & Modifier.NATIVE) > 0;
    String nativeCode = extractNativeCode(m.getStartPosition(), m.getLength());
    if (nativeCode == null) {
      J2ObjC.warning(m, "no native code found");
      return "";
    }
    indent();
    String code = reindent('{' + nativeCode + '}');
    unindent();
    return code;
  }

  private void printImports(CompilationUnit node) {
    ImplementationImportCollector collector = new ImplementationImportCollector();
    collector.collect(node, getSourceFileName());
    Set<ImportCollector.Import> imports = collector.getImports();

    if (!imports.isEmpty()) {
      Set<String> importStmts = Sets.newTreeSet();
      for (ImportCollector.Import imp : imports) {
        importStmts.add(String.format("#import \"%s.h\"", imp.getImportFileName()));
      }
      for (String stmt : importStmts) {
        println(stmt);
      }

      // Print native imports.
      int endOfImportText = node.types().isEmpty() ? node.getLength()
          : ((ASTNode) node.types().get(0)).getStartPosition();
      @SuppressWarnings("unchecked")
      List<Comment> comments = node.getCommentList(); // safe by definition
      for (Comment c : comments) {
        int start = c.getStartPosition();
        if (start >= endOfImportText) {
          break;
        }
        if (c instanceof BlockComment) {
          String nativeImport = extractNativeCode(start, c.getLength());
          if (nativeImport != null) {  // if it has a JSNI section
            println(nativeImport.trim());
          }
        }
      }

      newline();
    }
  }

  private void printStaticVars(List<FieldDeclaration> fields, boolean isInterface) {
    boolean hadStaticVar = false;
    for (FieldDeclaration f : fields) {
      if (Modifier.isStatic(f.getModifiers()) || isInterface) {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fragments = f.fragments(); // safe by specification
        for (VariableDeclarationFragment var : fragments) {
          IVariableBinding binding = Types.getVariableBinding(var);
          if (!Types.isPrimitiveConstant(binding)) {
            String name = NameTable.getName(binding);
            Expression initializer = var.getInitializer();
            if (initializer != null) {
              printConstant(name, initializer);
            } else {
              printf("static %s %s;\n", NameTable.javaRefToObjC(f.getType()), name);
            }
            hadStaticVar = true;
          }
        }
      }
    }
    if (hadStaticVar) {
      newline();
    }
  }

  private void printProperties(FieldDeclaration[] fields) {
    int nPrinted = 0;
    for (FieldDeclaration field : fields) {
      if ((field.getModifiers() & Modifier.STATIC) == 0) {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> vars = field.fragments(); // safe by definition
        for (VariableDeclarationFragment var : vars) {
          if (var.getName().getIdentifier().startsWith("this$") && superDefinesVariable(var)) {
            // Don't print, as it shadows an inner field in a super class.
            continue;
          }

          String name = NameTable.getName(var.getName());
          ITypeBinding type = Types.getTypeBinding(field.getType());
          String typeString = NameTable.javaRefToObjC(type);
          String objCFieldName = NameTable.javaFieldToObjC(name);

          // Don't emit the getter when there is already a method with the
          // same name.
          boolean hasGetter = false;
          boolean hasSetter = false;
          ITypeBinding declaringClass = Types.getTypeBinding(field.getParent());
          if (declaringClass != null) {
            IMethodBinding[] methods = declaringClass.getDeclaredMethods();
            for (IMethodBinding method : methods) {
              if (method.getName().equals(name) && method.getParameterTypes().length == 0) {
                hasGetter = true;
                break;
              }
            }
          }

          if (!hasGetter && Options.useReferenceCounting() && !type.isPrimitive()) {
            // Generates a getter that will make sure the returned object is still valid
            // if it's used after it's unreferenced by the instance.
            printf(String.format("- (%s)%s {\n  return [[%s retain] autorelease];\n}\n",
                typeString, name, objCFieldName));
            hasGetter = true;
          }
          IVariableBinding binding = Types.getVariableBinding(var);
          if (Options.useReferenceCounting() && !type.isPrimitive() &&
              !Types.isWeakReference(binding)) {
            // Setter can always be generated and won't collide with a transpiled method name.
            String setterName = "set" + NameTable.capitalize(name);
            printf(String.format("- (void)%s:(%s)%s {\n  JreOperatorRetainedAssign(&%s, %s);\n}\n",
                setterName, typeString, name, objCFieldName, name));
          }
          if (!hasGetter || !hasSetter) {
            printf(String.format("@synthesize %s = %s;\n", name, objCFieldName));
          }
          nPrinted++;
        }
      }
    }
    if (nPrinted > 0) {
      newline();
    }
  }

  private void printConstant(String name, Expression initializer) {
    Object constant = initializer.resolveConstantExpressionValue();
    String text = generateExpression(initializer);
    // non-constant initializers were already moved to static blocks
    assert constant != null;
    print("static ");
    if (constant instanceof String) {
      printf("NSString * %s = %s;\n", name, text);
    } else if (constant instanceof Boolean) {
      printf("BOOL %s = %s;\n;", name, ((Boolean) constant).booleanValue() ? "YES" : "NO");
    } else if (constant instanceof Character) {
      printf("unichar %s = %s;\n", name, text);
    } else {
      assert constant instanceof Number;
      Number number = (Number) constant;
      if (constant instanceof Byte) {
        printf("char %s = %d;\n", name, number.byteValue());
      } else if (constant instanceof Double) {
        printf("double %s = %s;\n", name, text);
      } else if (constant instanceof Float) {
        printf("float %s = %s;\n", name, text);
      } else if (constant instanceof Integer) {
        printf("int %s = %s;\n", name, text);
      } else if (constant instanceof Long) {
        printf("long long %s = %s;\n", name, text);
      } else {
        printf("short %s = %d;\n", name, number.shortValue());
      }
    }
  }

   /**
    * If type extends java.lang.Number, add a required implementation of
    * NSValue.objCType().  This can't be implemented as a native method
    * because its return type is const char *.  Since this method overrides
    * the default implementation, the signatures need to match exactly.
    */
  private void printObjCTypeMethod(TypeDeclaration node) {
    ITypeBinding type = Types.getTypeBinding(node);
    if (Types.isJavaNumberType(type)) {
      char objCType;
      String s = type.getName();
      // Strings as case values would be nice here.
      if (s.equals("Byte")) {
        objCType = 'c';
      } else if (s.equals("Double")) {
        objCType = 'd';
      } else if (s.equals("Float")) {
        objCType = 'f';
      } else if (s.equals("Integer")) {
        objCType = 'i';
      } else if (s.equals("Long")) {
        objCType = 'q';
      } else if (s.equals("Short")) {
        objCType = 's';
      } else {
        return;  // Other numeric types will be returned as objects.
      }
      println("- (const char *)objCType {");
      printf("  return \"%c\";\n", objCType);
      println("}\n");
    }
  }
}
