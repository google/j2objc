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
import com.google.devtools.j2objc.types.Import;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
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

import java.util.Arrays;
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
  private final Set<String> invokedConstructors = Sets.newHashSet();

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
      pushIgnoreDeprecatedDeclarationsPragma();
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

        @Override
        public boolean visit(AnnotationTypeDeclaration node) {
          if (BindingUtil.isRuntimeAnnotation(Types.getTypeBinding(node))) {
            generate(node);
          }
          return true;
        }
      });
      popIgnoreDeprecatedDeclarationsPragma();
    } else {
      // Print a dummy C function so compiled object file is valid.
      List<AbstractTypeDeclaration> types = ASTUtil.getTypes(unit);
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
        if (node.isInterface()) {
          if (!getStaticFieldsNeedingAccessors(
              Arrays.asList(node.getFields()), /* isInterface */ true).isEmpty()) {
            result[0] = true;
          }
        } else {
          result[0] = true;  // always print concrete types
        }
        return false;
      }

      @Override
      public boolean visit(EnumDeclaration node) {
        result[0] = true; // always print enums
        return false;
      }

      @Override
      public boolean visit(AnnotationTypeDeclaration node) {
        result[0] = BindingUtil.isRuntimeAnnotation(Types.getTypeBinding(node));
        return false;
      }
    });
    return result[0];
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
    unit.accept(new ErrorReportingASTVisitor() {
      @Override
      public boolean visit(ConstructorInvocation node) {
        invokedConstructors.add(parameterKey(Types.getMethodBinding(node)));
        return false;
      }
    });
  }

  @Override
  public void generate(TypeDeclaration node) {
    syncLineNumbers(node.getName()); // avoid doc-comment

    String typeName = NameTable.getFullName(node);
    List<FieldDeclaration> fields = Lists.newArrayList(node.getFields());
    List<MethodDeclaration> methods = Lists.newArrayList(node.getMethods());
    fieldHiders = HiddenFieldDetector.getFieldNameConflicts(node);
    if (node.isInterface()) {
      printStaticInterface(typeName, fields, methods);
    } else {
      printf("@implementation %s\n\n", typeName);
      printStaticReferencesMethod(fields);
      printStaticVars(fields, /* isInterface */ false);
      printStaticFieldAccessors(fields, methods, /* isInterface */ false);
      printMethods(node);
      printObjCTypeMethod(node);
      if (!Options.stripReflection()) {
        printTypeAnnotationsMethod(node);
        printMethodAnnotationMethods(Lists.newArrayList(node.getMethods()));
        printFieldAnnotationMethods(Lists.newArrayList(node.getFields()));
      }

      println("@end");
    }
  }

  @Override
  protected void generate(AnnotationTypeDeclaration node) {
    syncLineNumbers(node.getName()); // avoid doc-comment

    String typeName = NameTable.getFullName(node);
    printf("@implementation %sImpl\n", typeName);
    List<AnnotationTypeMemberDeclaration> members = Lists.newArrayList();
    for (BodyDeclaration decl : ASTUtil.getBodyDeclarations(node)) {
      if (decl instanceof AnnotationTypeMemberDeclaration) {
        members.add((AnnotationTypeMemberDeclaration) decl);
      }
    }
    printAnnotationProperties(members);
    if (!members.isEmpty()) {
      printAnnotationConstructor(Types.getTypeBinding(node));
    }
    printAnnotationAccessors(members);
    println("- (IOSClass *)annotationType {");
    printf("  return [IOSClass classWithProtocol:@protocol(%s)];\n", typeName);
    println("}\n");
    printTypeAnnotationsMethod(node);
    println("@end\n");
  }

  private void printAnnotationConstructor(ITypeBinding annotation) {
    print(annotationConstructorDeclaration(annotation));
    println(" {");
    println("  if ((self = [super init])) {");
    for (IMethodBinding member : annotation.getDeclaredMethods()) {
      String name = member.getName();
      printf("    %s = ", name);
      ITypeBinding type = member.getReturnType();
      boolean needsRetain = !type.isPrimitive();
      if (needsRetain) {
        print("RETAIN(");
      }
      printf("%s_", name);
      if (needsRetain) {
        print(')');
      }
      println(";");
    }
    println("  }");
    println("  return self;");
    println("}\n");
  }

  private void printAnnotationAccessors(List<AnnotationTypeMemberDeclaration> members) {
    int nPrinted = 0;
    for (AnnotationTypeMemberDeclaration member : members) {
      Expression deflt = member.getDefault();
      if (deflt != null) {
        ITypeBinding type = Types.getTypeBinding(member.getType());
        String typeString = NameTable.getSpecificObjCType(type);
        String propertyName = NameTable.getName(member.getName());
        printf("+ (%s)%sDefault {\n", typeString, propertyName);
        printf("  return %s;\n", generateExpression(deflt));
        println("}\n");
        nPrinted++;
      }
    }
    if (nPrinted > 0) {
      newline();
    }
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
            NameTable.primitiveTypeToObjC(primitiveType.getName()));
      }
    }

    List<VariableDeclarationFragment> properties = getProperties(node.getFields());
    if (properties.size() > 0) {
      printStrongReferencesMethod(properties);
    }
  }

  private List<VariableDeclarationFragment> getProperties(FieldDeclaration[] fields) {
    List<VariableDeclarationFragment> properties = Lists.newArrayList();
    for (FieldDeclaration field : fields) {
      if (!Modifier.isStatic(field.getModifiers())) {
        properties.addAll(ASTUtil.getFragments(field));
      }
    }
    return properties;
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
          for (VariableDeclarationFragment var : ASTUtil.getFragments(f)) {
            IVariableBinding binding = Types.getVariableBinding(var);
            // All non-primitive static variables are strong references.
            if (!binding.getType().isPrimitive()) {
              String name = NameTable.getStaticVarQualifiedName(binding);
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

  private void printStaticInterface(
      String typeName, List<FieldDeclaration> fields, List<MethodDeclaration> methods) {
    List<IVariableBinding> staticFields =
        getStaticFieldsNeedingAccessors(fields, /* isInterface */ true);
    if (staticFields.isEmpty()) {
      return;
    }
    printf("\n@implementation %s\n\n", typeName);
    printStaticVars(fields, /* isInterface */ true);
    printStaticFieldAccessors(staticFields, methods);
    for (MethodDeclaration method : methods) {
      if (method.getBody() != null) {
        printMethod(method);
      }
    }
    println("@end");
  }

  @Override
  protected void generate(EnumDeclaration node) {
    List<EnumConstantDeclaration> constants = ASTUtil.getEnumConstants(node);
    List<MethodDeclaration> methods = Lists.newArrayList();
    List<FieldDeclaration> fields = Lists.newArrayList();
    MethodDeclaration initializeMethod = null;
    for (BodyDeclaration decl : ASTUtil.getBodyDeclarations(node)) {
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
    newline();
    for (EnumConstantDeclaration constant : constants) {
      IVariableBinding var = Types.getVariableBinding(constant.getName());
      printf("static %s *%s;\n", typeName, NameTable.getStaticVarQualifiedName(var));
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

    printStaticFieldAccessors(fields, methods, /* isInterface */ false);
    printMethods(methods);

    printf("+ (void)initialize {\n  if (self == [%s class]) {\n", typeName);
    for (int i = 0; i < constants.size(); i++) {
      EnumConstantDeclaration constant = constants.get(i);
      List<Expression> args = ASTUtil.getArguments(constant);
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
      for (Statement s : ASTUtil.getStatements(initializeMethod.getBody())) {
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

    printTypeAnnotationsMethod(node);
    println("@end");
  }

  @Override
  protected void printStaticFieldGetter(IVariableBinding var) {
    String name = BindingUtil.isPrimitiveConstant(var) ?
        NameTable.getPrimitiveConstantName(var) :
        NameTable.getStaticVarQualifiedName(var);
    printf("%s {\n  return %s;\n}\n\n", staticFieldGetterSignature(var), name);
  }

  @Override
  protected void printStaticFieldReferenceGetter(IVariableBinding var) {
    printf("%s {\n  return &%s;\n}\n\n", staticFieldReferenceGetterSignature(var),
           NameTable.getStaticVarQualifiedName(var));
  }

  @Override
  protected void printStaticFieldSetter(IVariableBinding var) {
    String fieldName = NameTable.getStaticVarQualifiedName(var);
    String paramName = NameTable.getName(var);
    String signature = staticFieldSetterSignature(var);
    if (Options.useReferenceCounting()) {
      printf("%s {\n  JreOperatorRetainedAssign(&%s, nil, %s);\n}\n\n",
          signature, fieldName, paramName);
    } else {
      printf("%s {\n  %s = %s;\n}\n\n", signature, fieldName, paramName);
    }
  }

  @Override
  protected String methodDeclaration(MethodDeclaration m) {
    int modifiers = m.getModifiers();
    if ((modifiers & Modifier.NATIVE) > 0) {
      if (hasNativeCode(m, true)) {
        return super.methodDeclaration(m) + " " + extractNativeMethodBody(m) + "\n\n";
      } else if (Options.generateNativeStubs()) {
        return super.methodDeclaration(m) + " " + generateNativeStub(m) + "\n\n";
      } else {
        return "";
      }
    }
    String methodBody = generateMethodBody(m);
    return super.methodDeclaration(m) + " " + reindent(methodBody) + "\n\n" +
        methodExceptionsFunction(m);
  }

  protected String methodExceptionsFunction(MethodDeclaration m) {
    if (m.thrownExceptions().isEmpty() || Options.stripReflection()) {
      return "";
    }
    IMethodBinding method = Types.getMethodBinding(m);
    StringBuilder sb = new StringBuilder();
    sb.append("+ (IOSObjectArray *)__exceptions_");
    sb.append(methodKey(method));
    sb.append(" {\n");
    ITypeBinding[] exceptionTypes = method.getExceptionTypes();
    sb.append("  return [IOSObjectArray arrayWithObjects:(id[]) { ");
    for (int i = 0; i < exceptionTypes.length; i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append('[');
      sb.append(NameTable.getFullName(exceptionTypes[i]));
      sb.append(" getClass]");
    }
    sb.append(" } count:");
    sb.append(exceptionTypes.length);
    sb.append(" type:[IOSClass getClass]];\n}\n\n");
    return sb.toString();
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
    IVariableBinding binding = Types.getVariableBinding(param);
    return binding != null && fieldHiders.contains(binding) ? name + "Arg" : name;
  }

  @Override
  protected String constructorDeclaration(MethodDeclaration m) {
    String methodBody;
    IMethodBinding binding = Types.getMethodBinding(m);
    boolean memDebug = Options.memoryDebug();
    List<Statement> statements = ASTUtil.getStatements(m.getBody());
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
    if (invokedConstructors.contains(parameterKey(binding))) {
      return super.constructorDeclaration(m, true) + " " + reindent(methodBody) + "\n\n"
          + super.constructorDeclaration(m, false) + " {\n  return "
          + generateStatement(createInnerConstructorInvocation(m), false) + ";\n}\n\n";
    } else {
      return super.constructorDeclaration(m, false) + " " + reindent(methodBody) + "\n\n" +
          methodExceptionsFunction(m);
    }
  }

  private Statement createInnerConstructorInvocation(MethodDeclaration m) {
    ConstructorInvocation invocation = m.getAST().newConstructorInvocation();
    Types.addBinding(invocation, Types.getBinding(m));
    for (SingleVariableDeclaration param : ASTUtil.getParameters(m)) {
      SimpleName paramName = param.getName();
      IVariableBinding paramBinding = Types.getVariableBinding(paramName);
      SimpleName argName = m.getAST().newSimpleName(paramName.getIdentifier());
      Types.addBinding(argName, paramBinding);
      ASTUtil.getArguments(invocation).add(argName);
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
    String impliedArgs = (args.isEmpty() ? "W" : " w") + "ithNSString:__name withInt:__ordinal";
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

    // Insert synthetic parameters.
    StringBuilder sb2 =
        new StringBuilder(generateStatement(createInnerConstructorInvocation(m), false));
    invocation = sb2.insert(sb2.length() - 1, " withNSString:__name withInt:__ordinal").toString();

    if (invokedConstructors.contains(parameterKey(binding))) {
      return super.constructorDeclaration(m, true) + " " + reindent(sb.toString()) + "\n\n"
          + super.constructorDeclaration(m, false) + " {\n  return "
          + invocation + ";\n}\n\n";
    } else {
      return super.constructorDeclaration(m, false) + " " + reindent(sb.toString()) + "\n\n";
    }
  }

  @Override
  protected void printStaticConstructorDeclaration(MethodDeclaration m) {
    String className = NameTable.getFullName(Types.getMethodBinding(m).getDeclaringClass());
    StringBuffer sb = new StringBuffer();
    sb.append("{\nif (self == [" + className + " class]) {\n");
    for (Statement statement : ASTUtil.getStatements(m.getBody())) {
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
    Set<Import> imports = collector.getImports();

    if (!imports.isEmpty()) {
      Set<String> includeStmts = Sets.newTreeSet();
      for (Import imp : imports) {
        includeStmts.add(String.format("#include \"%s.h\"", imp.getImportFileName()));
      }
      for (String stmt : includeStmts) {
        println(stmt);
      }

      // Print native includes.
      int endOfImportText = node.types().isEmpty() ? node.getLength()
          : ((ASTNode) node.types().get(0)).getStartPosition();
      for (Comment c : ASTUtil.getCommentList(node)) {
        int start = c.getStartPosition();
        if (start >= endOfImportText) {
          break;
        }
        if (c instanceof BlockComment) {
          String nativeImport = extractNativeCode(start, c.getLength(), true);
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
        for (VariableDeclarationFragment var : ASTUtil.getFragments(f)) {
          IVariableBinding binding = Types.getVariableBinding(var);
          if (!BindingUtil.isPrimitiveConstant(binding)) {
            String name = NameTable.getStaticVarQualifiedName(binding);
            String objcType = NameTable.getObjCType(binding.getType());
            Expression initializer = var.getInitializer();
            if (initializer != null) {
              printf("static %s %s = %s;\n", objcType, name, generateExpression(initializer));
            } else {
              printf("static %s %s;\n", objcType, name);
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

  private void printAnnotationProperties(List<AnnotationTypeMemberDeclaration> members) {
    int nPrinted = 0;
    for (AnnotationTypeMemberDeclaration member : members) {
      println(String.format("@synthesize %s;", NameTable.getName(member.getName())));
      nPrinted++;
    }
    if (nPrinted > 0) {
      newline();
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

  private void printTypeAnnotationsMethod(AbstractTypeDeclaration decl) {
    List<Annotation> runtimeAnnotations = ASTUtil.getRuntimeAnnotations(ASTUtil.getModifiers(decl));
    if (runtimeAnnotations.size() > 0) {
      println("+ (IOSObjectArray *)__annotations {");
      printAnnotationCreate(runtimeAnnotations);
    }
  }

  private void printMethodAnnotationMethods(List<MethodDeclaration> methods) {
    for (MethodDeclaration method : methods) {
      List<Annotation> runtimeAnnotations =
          ASTUtil.getRuntimeAnnotations(ASTUtil.getModifiers(method));
      if (runtimeAnnotations.size() > 0) {
        printf("+ (IOSObjectArray *)__annotations_%s {\n",
            methodKey(Types.getMethodBinding(method)));
        printAnnotationCreate(runtimeAnnotations);
      }
      printParameterAnnotationMethods(method);
    }
  }

  private void printParameterAnnotationMethods(MethodDeclaration method) {
    List<SingleVariableDeclaration> params = ASTUtil.getParameters(method);

    // Quick test to see if there are any parameter annotations.
    boolean hasAnnotations = false;
    for (SingleVariableDeclaration param : params) {
      List<Annotation> runtimeAnnotations =
          ASTUtil.getRuntimeAnnotations(ASTUtil.getModifiers(param));
      if (runtimeAnnotations.size() > 0) {
        hasAnnotations = true;
        break;
      }
    }

    if (hasAnnotations) {
      // Print array of arrays, with an element in the outer array for each parameter.
      printf("+ (IOSObjectArray *)__annotations_%s_params {\n",
          methodKey(Types.getMethodBinding(method)));
      print("  return [IOSObjectArray arrayWithObjects:(id[]) { ");
      for (int i = 0; i < params.size(); i++) {
        if (i > 0) {
          print(", ");
        }
        SingleVariableDeclaration param = params.get(i);
        List<Annotation> runtimeAnnotations =
            ASTUtil.getRuntimeAnnotations(ASTUtil.getModifiers(param));
        if (runtimeAnnotations.size() > 0) {
          print("[IOSObjectArray arrayWithObjects:(id[]) { ");
          printAnnotations(runtimeAnnotations);
          printf(" } count:%d type:[IOSClass classWithProtocol:" +
              "@protocol(JavaLangAnnotationAnnotation)]]", runtimeAnnotations.size());
        } else {
          print("[IOSObjectArray arrayWithLength:0 " +
              "type:[IOSClass classWithProtocol:@protocol(JavaLangAnnotationAnnotation)]]");
        }
      }
      printf(" } count:%d type:[IOSClass classWithProtocol:" +
          "@protocol(JavaLangAnnotationAnnotation)]];\n}\n", params.size());
    }
  }

  private void printFieldAnnotationMethods(List<FieldDeclaration> fields) {
    for (FieldDeclaration field : fields) {
      List<Annotation> runtimeAnnotations =
          ASTUtil.getRuntimeAnnotations(ASTUtil.getModifiers(field));
      if (runtimeAnnotations.size() > 0) {
        for (VariableDeclarationFragment var : ASTUtil.getFragments(field)) {
          printf("+ (IOSObjectArray *)__annotations_%s_ {\n", var.getName().getIdentifier());
          printAnnotationCreate(runtimeAnnotations);
        }
      }
    }
  }

  private void printAnnotationCreate(List<Annotation> runtimeAnnotations) {
    print("  return [IOSObjectArray arrayWithObjects:(id[]) { ");
    printAnnotations(runtimeAnnotations);
    printf(" } count:%d type:[IOSClass " +
        "classWithProtocol:@protocol(JavaLangAnnotationAnnotation)]];\n}\n\n",
        runtimeAnnotations.size());
  }

  private void printAnnotations(List<Annotation> runtimeAnnotations) {
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
      printf("[[%sImpl alloc] init",
          NameTable.getFullName(Types.getTypeBinding(annotation)));
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
    IAnnotationBinding binding = Types.getAnnotationBinding(annotation);
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
      printf("[%s %s]", NameTable.getFullName(declaringClass), var.getName());
    } else if (value instanceof ITypeBinding) {
      ITypeBinding type = (ITypeBinding) value;
      printf("[%s getClass]", NameTable.getFullName(type));
    } else if (value instanceof String) {
      printf("@\"%s\"", value);
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
      printf(" } count:%d type:[NSObject getClass]]", array.length);
    } else {
      assert false : "unknown annotation value type";
    }
  }

}
