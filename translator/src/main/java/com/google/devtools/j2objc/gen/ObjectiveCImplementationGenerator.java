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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.devtools.j2objc.J2ObjC;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.ImplementationImportCollector;
import com.google.devtools.j2objc.types.Import;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ErrorReportingASTVisitor;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.AST;
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
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates Objective-C implementation (.m) files from compilation units.
 *
 * @author Tom Ball
 */
public class ObjectiveCImplementationGenerator extends ObjectiveCSourceFileGenerator {
  private Set<IVariableBinding> fieldHiders;
  private final String suffix;
  private final Set<String> invokedConstructors = Sets.newHashSet();
  private final ListMultimap<AbstractTypeDeclaration, Comment> blockComments =
      ArrayListMultimap.create();

  /**
   * Generate an Objective-C implementation file for each type declared in a
   * specified compilation unit.
   */
  public static void generate(String fileName, CompilationUnit unit, String source) {
    ObjectiveCImplementationGenerator implementationGenerator =
        new ObjectiveCImplementationGenerator(fileName, unit, source);
    implementationGenerator.generate(unit);
  }

  private ObjectiveCImplementationGenerator(String fileName, CompilationUnit unit, String source) {
    super(fileName, source, unit, Options.emitLineDirectives());
    fieldHiders = HiddenFieldDetector.getFieldNameConflicts(unit);
    suffix = Options.getImplementationFileSuffix();
  }

  @Override
  protected String getSuffix() {
    return suffix;
  }

  public void generate(CompilationUnit unit) {
    println(J2ObjC.getFileHeader(getSourceFileName()));
    List<AbstractTypeDeclaration> typesToGenerate = collectTypes(unit);
    if (!typesToGenerate.isEmpty()) {
      findBlockComments(unit, typesToGenerate);
      findInvokedConstructors(unit);
      printStart(getSourceFileName());
      printImports(unit);
      pushIgnoreDeprecatedDeclarationsPragma();
      for (AbstractTypeDeclaration type : typesToGenerate) {
        generate(type);
      }
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

  private List<AbstractTypeDeclaration> collectTypes(CompilationUnit unit) {
    final List<AbstractTypeDeclaration> types = Lists.newArrayList();
    unit.accept(new ErrorReportingASTVisitor() {
      @Override
      public boolean visit(TypeDeclaration node) {
        if (!node.isInterface()
            || !Iterables.isEmpty(getStaticFieldsNeedingInitialization(node))
            || !Options.stripReflection()) {
          types.add(node);
        }
        return false;
      }

      @Override
      public boolean visit(EnumDeclaration node) {
        types.add(node); // always print enums
        return false;
      }

      @Override
      public boolean visit(AnnotationTypeDeclaration node) {
        if (BindingUtil.isRuntimeAnnotation(Types.getTypeBinding(node))
            || !Iterables.isEmpty(getStaticFieldsNeedingInitialization(node))) {
          types.add(node);
        }
        return false;
      }
    });
    return types;
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
        invokedConstructors.add(methodKey(Types.getMethodBinding(node)));
        return false;
      }
    });
  }

  /**
   * Finds all block comments and associates them with their containing type.
   */
  private void findBlockComments(CompilationUnit unit, List<AbstractTypeDeclaration> types) {
    List<Comment> comments = ASTUtil.getCommentList(unit);
    for (Comment comment : comments) {
      if (!comment.isBlockComment()) {
        continue;
      }
      int commentPos = comment.getStartPosition();
      AbstractTypeDeclaration containingType = null;
      int containingTypePos = -1;
      for (AbstractTypeDeclaration type : types) {
        int typePos = type.getStartPosition();
        if (typePos < 0) {
          continue;
        }
        int typeEnd = typePos + type.getLength();
        if (commentPos > typePos && commentPos < typeEnd && typePos > containingTypePos) {
          containingType = type;
          containingTypePos = typePos;
        }
      }
      if (containingType != null) {
        blockComments.put(containingType, comment);
      }
    }
  }

  @Override
  public void generate(TypeDeclaration node) {
    String typeName = NameTable.getFullName(node);
    List<MethodDeclaration> methods = Lists.newArrayList(node.getMethods());
    fieldHiders = HiddenFieldDetector.getFieldNameConflicts(node);
    if (node.isInterface()) {
      printStaticInterface(node, typeName, methods);
    } else {
      printInitFlagDefinition(node, methods);
      newline();
      syncLineNumbers(node.getName()); // avoid doc-comment
      printf("@implementation %s\n", typeName);
      printStaticReferencesMethod(node);
      printStaticVars(node);
      printMethods(node);
      if (!Options.stripReflection()) {
        printTypeAnnotationsMethod(node);
        printMethodAnnotationMethods(Lists.newArrayList(node.getMethods()));
        printFieldAnnotationMethods(node);
        printMetadata(node);
      }

      println("\n@end");
    }
  }

  @Override
  protected void generate(AnnotationTypeDeclaration node) {
    syncLineNumbers(node.getName()); // avoid doc-comment

    String typeName = NameTable.getFullName(node);
    List<MethodDeclaration> methods = ASTUtil.getMethodDeclarations(node);
    printInitFlagDefinition(node, methods);
    printf("\n@implementation %s\n", typeName);
    if (BindingUtil.isRuntimeAnnotation(Types.getTypeBinding(node))) {
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
    }
    printStaticVars(node);
    println("\n- (IOSClass *)annotationType {");
    printf("  return [IOSClass classWithProtocol:@protocol(%s)];\n", typeName);
    println("}");
    printMethods(methods);
    if (!Options.stripReflection()) {
      printTypeAnnotationsMethod(node);
      printMetadata(node);
    }
    println("\n@end");
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

  private static final String TYPE_REGEX = "\\([\\w\\s\\*<>\\[\\]]+\\)";
  private static final String PARAM_REGEX = "\\s*:\\s*" + TYPE_REGEX + "\\s*\\w+";
  private static final String ADDITIONAL_PARAM_REGEX = "\\s+(\\w+)" + PARAM_REGEX;
  private static final Pattern OBJC_METHOD_DECL_PATTERN = Pattern.compile(
      "^\\+|-\\s*" + TYPE_REGEX + "\\s*(\\w+)(" + PARAM_REGEX + "((?:" + ADDITIONAL_PARAM_REGEX
      + ")*))?\\s*\\{");
  private static final Pattern ADDITIONAL_PARAM_PATTERN = Pattern.compile(ADDITIONAL_PARAM_REGEX);

  private void findMethodSignatures(String code, Set<String> signatures) {
    Matcher matcher = OBJC_METHOD_DECL_PATTERN.matcher(code);
    while (matcher.find()) {
      StringBuilder signature = new StringBuilder();
      signature.append(matcher.group(1));
      if (matcher.group(2) != null) {
        signature.append(':');
        String additionalParams = matcher.group(3);
        if (additionalParams != null) {
          Matcher paramsMatcher = ADDITIONAL_PARAM_PATTERN.matcher(additionalParams);
          while (paramsMatcher.find()) {
            signature.append(paramsMatcher.group(1)).append(':');
          }
        }
      }
      signatures.add(signature.toString());
    }
  }

  private void printMethodsAndOcni(
      AbstractTypeDeclaration typeNode, Iterable<MethodDeclaration> methods,
      Iterable<Comment> comments) {
    Set<String> methodsPrinted = Sets.newHashSet();
    Iterator<MethodDeclaration> methodsIter = methods.iterator();
    Iterator<Comment> commentsIter = comments.iterator();
    MethodDeclaration nextMethod = methodsIter.hasNext() ? methodsIter.next() : null;
    Comment nextComment = commentsIter.hasNext() ? commentsIter.next() : null;
    int minPos = 0;
    while (nextMethod != null || nextComment != null) {
      int methodStartPos = nextMethod != null ? nextMethod.getStartPosition() : Integer.MAX_VALUE;
      if (methodStartPos < 0) {
        methodStartPos = minPos;
      }
      int commentStartPos =
          nextComment != null ? nextComment.getStartPosition() : Integer.MAX_VALUE;
      if (methodStartPos < commentStartPos) {
        assert nextMethod != null;
        printMethod(nextMethod);
        minPos = methodStartPos + nextMethod.getLength();
        nextMethod = methodsIter.hasNext() ? methodsIter.next() : null;
      } else {
        assert nextComment != null;
        if (commentStartPos > minPos) {
          String nativeCode = extractNativeCode(commentStartPos, nextComment.getLength());
          if (nativeCode != null) {
            nativeCode = reindent(nativeCode.trim());
            findMethodSignatures(nativeCode, methodsPrinted);
            print("\n" + nativeCode + "\n");
          }
        }
        nextComment = commentsIter.hasNext() ? commentsIter.next() : null;
      }
    }

    // If the type implements Iterable and there's no existing implementation
    // for NSFastEnumeration's protocol method, then add the default
    // implementation.
    if (BindingUtil.findInterface(Types.getTypeBinding(typeNode), "java.lang.Iterable") != null
        && !methodsPrinted.contains("countByEnumeratingWithState:objects:count:")) {
      print("- (NSUInteger)countByEnumeratingWithState:(NSFastEnumerationState *)state "
            + "objects:(__unsafe_unretained id *)stackbuf count:(NSUInteger)len {\n"
            + "  return JreDefaultFastEnumeration(self, state, stackbuf, len);\n}\n\n");
    }
  }

  private void printMethods(TypeDeclaration node) {
    printMethodsAndOcni(node, Arrays.asList(node.getMethods()), blockComments.get(node));

    // If node implements CharSequence, add forwarding method from the
    // sequenceDescription method to description (toString()).  See
    // JavaToIOSMethodTranslator.loadCharSequenceMethods() for details.
    ITypeBinding binding = Types.getTypeBinding(node);
    for (ITypeBinding interfaze : binding.getInterfaces()) {
      if (interfaze.getQualifiedName().equals("java.lang.CharSequence")) {
        println("- (NSString *)description {\n  return [self sequenceDescription];\n}\n");
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
    return !type.isPrimitive() && !BindingUtil.isWeakReference(varBinding);
  }

  // We generate the runtime debug method -memDebugStrongReferences.
  // This method will return an array of information about a strong reference,
  // including pointer to object and name.
  private void printStrongReferencesMethod(List<VariableDeclarationFragment> properties) {
    if (Options.memoryDebug()) {
      newline();
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

  // We generate the runtime debug method +memDebugStaticReferences.
  // This method will return an array of NSNumber containing pointers (casted into unsigned long)
  // to the objects referenced by a class variable with a strong reference.
  // It will be useful for debug purpose.
  //
  // Arrays returned by -memDebugStaticReferences and -memDebugStaticReferencesNames (see below)
  // must be the same size.
  //
  // In case of a Java enum, valuesVarNameis the name of the array of enum values.
  private void printStaticReferencesMethod(AbstractTypeDeclaration node) {
    if (Options.memoryDebug()) {
      newline();
      if (!Options.useReferenceCounting()) {
        println("+ (NSArray *)memDebugStaticReferences {");
        println("  return nil;");
        println("}");
        return;
      }
      println("+ (NSArray *)memDebugStaticReferences {");
      println("  NSMutableArray *result = [NSMutableArray array];");
      for (VariableDeclarationFragment var : ASTUtil.getAllFields(node)) {
        IVariableBinding binding = Types.getVariableBinding(var);
        if (BindingUtil.isStatic(binding)) {
          // All non-primitive static variables are strong references.
          if (!binding.getType().isPrimitive()) {
            String name = NameTable.getStaticVarQualifiedName(binding);
            println(String.format("  [result addObject:[JreMemDebugStrongReference " +
                "strongReferenceWithObject:%s name:@\"%s\"]];", name, name));
          }
        }
      }
      println("  return result;");
      println("}");
    }
  }

  private void printStaticInterface(AbstractTypeDeclaration node,
      String typeName, List<MethodDeclaration> methods) {
    boolean needsImplementation = !methods.isEmpty() || !Options.stripReflection();
    if (needsImplementation && !hasInitializeMethod(node, methods)) {
      printf("\n@interface %s : NSObject\n@end\n", typeName);
    }
    printInitFlagDefinition(node, methods);
    printStaticVars(node);
    if (!needsImplementation) {
      return;
    }
    printf("\n@implementation %s\n", typeName);
    for (MethodDeclaration method : methods) {
      if (method.getBody() != null) {
        printMethod(method);
      }
    }
    if (!Options.stripReflection()) {
      printMetadata(node);
    }
    println("\n@end");
  }

  @Override
  protected void generate(EnumDeclaration node) {
    List<EnumConstantDeclaration> constants = ASTUtil.getEnumConstants(node);
    List<MethodDeclaration> methods = Lists.newArrayList();
    MethodDeclaration initializeMethod = null;
    for (MethodDeclaration md : ASTUtil.getMethodDeclarations(node)) {
      if (isInitializeMethod(md)) {
        initializeMethod = md;
      } else {
        methods.add(md);
      }
    }
    syncLineNumbers(node.getName()); // avoid doc-comment

    String typeName = NameTable.getFullName(node);
    printInitFlagDefinition(node, methods);
    newline();
    printf("%s *%s_values[%s];\n", typeName, typeName, constants.size());

    newline();
    printf("@implementation %s\n", typeName);
    printStaticVars(node);
    printStaticReferencesMethod(node);

    // Enum constants needs to implement NSCopying.  Being singletons, they
    // can just return self, as long the retain count is incremented.
    String selfString = Options.useReferenceCounting() ? "[self retain]" : "self";
    printf("\n- (id)copyWithZone:(NSZone *)zone {\n  return %s;\n}\n", selfString);

    printMethodsAndOcni(node, methods, blockComments.get(node));

    printf("\n+ (void)initialize {\n  if (self == [%s class]) {\n", typeName);
    for (int i = 0; i < constants.size(); i++) {
      EnumConstantDeclaration constant = constants.get(i);
      List<Expression> args = ASTUtil.getArguments(constant);
      String name = NameTable.getName(constant.getName());
      String constantTypeName =
          NameTable.getFullName(Types.getMethodBinding(constant).getDeclaringClass());
      printf("    %s_%s = [[%s alloc] init", typeName, name, constantTypeName);

      if (args.isEmpty()) {
        print("With");
      } else {
        print(StatementGenerator.generateArguments(Types.getMethodBinding(constant),
            args, fieldHiders, getBuilder().getSourcePosition()));
        print(" with");
      }
      printf("NSString:@\"%s\" withInt:%d];\n", name, i);
    }
    if (initializeMethod != null) {
      for (Statement s : ASTUtil.getStatements(initializeMethod.getBody())) {
        printf("    %s", StatementGenerator.generate(s, fieldHiders, false,
            getBuilder().getSourcePosition()));
      }
    }
    printf("    %s_initialized = YES;\n", typeName);
    println("  }\n}\n");

    // Print generated values and valueOf methods.
    println("+ (IOSObjectArray *)values {");
    printf("  return [IOSObjectArray arrayWithObjects:%s_values count:%s type:" +
           "[IOSClass classWithClass:[%s class]]];\n", typeName, constants.size(), typeName);
    println("}\n");
    printf("+ (%s *)valueOfWithNSString:(NSString *)name {\n", typeName);
    printf("  for (int i = 0; i < %s; i++) {\n", constants.size());
    printf("    %s *e = %s_values[i];\n", typeName, typeName);
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
    println("}");

    if (!Options.stripReflection()) {
      printTypeAnnotationsMethod(node);
      printMetadata(node);
    }
    println("\n@end");
  }

  private void printInitFlagDefinition(
      AbstractTypeDeclaration node, List<MethodDeclaration> methods) {
    ITypeBinding binding = Types.getTypeBinding(node);
    String typeName = NameTable.getFullName(binding);
    if (hasInitializeMethod(node, methods)) {
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

  private String generateNativeStub(MethodDeclaration m) {
    IMethodBinding binding = Types.getMethodBinding(m);
    String methodName = NameTable.getName(binding);
    return String.format("{\n  @throw \"%s method not implemented\";\n}", methodName);
  }

  @Override
  protected String mappedMethodDeclaration(MethodDeclaration method, IOSMethod mappedMethod) {
    String methodBody = generateMethodBody(method);
    if (methodBody == null) {
      return "";
    }
    return "\n" + super.mappedMethodDeclaration(method, mappedMethod)
        + " " + reindent(methodBody) + "\n";
  }

  private String generateMethodBody(MethodDeclaration m) {
    String methodBody;
    if (Modifier.isNative(m.getModifiers())) {
      if (hasNativeCode(m, true)) {
        methodBody = extractNativeMethodBody(m);
      } else if (Options.generateNativeStubs()) {
        return generateNativeStub(m);
      } else {
        return null;
      }
    } else if (Modifier.isAbstract(m.getModifiers())) {
      // Generate a body which throws a NSInvalidArgumentException.
      String body =
          "{\n // can't call an abstract method\n " +
              "[self doesNotRecognizeSelector:_cmd];\n ";
      if (!Types.isVoidType(m.getReturnType2())) {
        body += "return 0;\n"; // Never executes, but avoids a gcc warning.
      }
      return body + "}";
    } else {
      // generate a normal method body
      methodBody = generateStatement(m.getBody(), false);
    }

    boolean isStatic = (m.getModifiers() & Modifier.STATIC) != 0;
    boolean isSynchronized = (m.getModifiers() & Modifier.SYNCHRONIZED) != 0;
    if (isStatic && isSynchronized) {
      methodBody = "{\n@synchronized([self class]) {\n" + methodBody + "}\n}\n";
    } else if (isSynchronized) {
      methodBody = "{\n@synchronized(self) {\n" + methodBody + "}\n}\n";
    }

    return methodBody;
  }

  @Override
  protected String getParameterName(SingleVariableDeclaration param) {
    String name = super.getParameterName(param);
    IVariableBinding binding = Types.getVariableBinding(param);
    return binding != null && fieldHiders.contains(binding) ? name + "Arg" : name;
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
    IMethodBinding binding = Types.getMethodBinding(m);
    boolean memDebug = Options.memoryDebug();
    List<Statement> statements = ASTUtil.getStatements(m.getBody());
    if (binding.getDeclaringClass().isEnum()) {
      printEnumConstructor(m, statements, binding);
      return;
    }
    StringBuffer sb = new StringBuffer("{\n");
    int constructorIdx = findConstructorInvocation(statements);
    int idx = 0;
    while (idx < constructorIdx) {
      sb.append(generateStatement(statements.get(idx++), false));
    }
    String superCall = idx == constructorIdx ?
        generateStatement(statements.get(idx++), false) : "[super init]";
    if (idx >= statements.size()) {
      sb.append("return ");
      if (memDebug) {
        sb.append("JreMemDebugAdd(");
      }
      sb.append(superCall).append(memDebug ? ");\n}" : ";\n}");
    } else {
      sb.append("if (self = ").append(superCall).append(") {\n");
      while (idx < statements.size()) {
        sb.append(generateStatement(statements.get(idx++), false));
      }
      if (memDebug) {
        sb.append("JreMemDebugAdd(self);\n");
      }
      sb.append("}\nreturn self;\n}");
    }
    methodBody = sb.toString();
    newline();
    syncLineNumbers(m.getName());  // avoid doc-comment
    if (invokedConstructors.contains(methodKey(binding))) {
      print(super.constructorDeclaration(m, true) + " " + reindent(methodBody) + "\n\n");
      print(super.constructorDeclaration(m, false) + " {\n" +
          "  return " + generateStatement(createInnerConstructorInvocation(m), false) + ";\n}\n");
    } else {
      print(super.constructorDeclaration(m, false) + " " + reindent(methodBody) + "\n");
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

  private void printEnumConstructor(
      MethodDeclaration m, List<Statement> statements, IMethodBinding binding) {
    assert !statements.isEmpty();

    // The InitializationNormalizer should have fixed this constructor so the
    // first statement is a constructor or super invocation.
    Statement s = statements.get(0);
    assert s instanceof ConstructorInvocation || s instanceof SuperConstructorInvocation;
    String invocation = generateStatement(statements.get(0), false);

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
        sb.append(generateStatement(statements.get(i), false));
      }
      if (memDebug) {
        sb.append("JreMemDebugAdd(self);\n");
      }
      sb.append("}\nreturn self;\n}");
    }

    newline();
    syncLineNumbers(m.getName());  // avoid doc-comment
    if (invokedConstructors.contains(methodKey(binding))) {
      print(super.constructorDeclaration(m, true) + " " + reindent(sb.toString()) + "\n\n");
      print(super.constructorDeclaration(m, false) + " {\n" +
          "  return " + generateStatement(createInnerConstructorInvocation(m), false) + ";\n}\n");
    } else {
      print(super.constructorDeclaration(m, false) + " " + reindent(sb.toString()) + "\n");
    }
  }

  @Override
  protected void printStaticConstructorDeclaration(MethodDeclaration m) {
    String className = NameTable.getFullName(Types.getMethodBinding(m).getDeclaringClass());
    StringBuffer sb = new StringBuffer();
    sb.append("{\nif (self == [" + className + " class]) {\n");
    for (Statement statement : ASTUtil.getStatements(m.getBody())) {
      sb.append(generateStatement(statement, false));
    }
    sb.append(className + "_initialized = YES;\n");
    sb.append("}\n}");
    print("\n+ (void)initialize " + reindent(sb.toString()) + "\n");
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
      ErrorUtil.warning(m, "no native code found");
      return "";
    }
    return '{' + nativeCode + '}';
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
    }
  }

  private void printStaticVars(AbstractTypeDeclaration node) {
    Iterable<VariableDeclarationFragment> fragments = getStaticFieldsNeedingInitialization(node);
    if (!Iterables.isEmpty(fragments)) {
      newline();
    }
    for (VariableDeclarationFragment var : fragments) {
      IVariableBinding binding = Types.getVariableBinding(var);
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

  private void printFieldAnnotationMethods(AbstractTypeDeclaration node) {
    for (FieldDeclaration field : ASTUtil.getFieldDeclarations(node)) {
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
      printf("[[%s alloc] init",
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
      printAnnotationValue(annotation.getAST(), value);
    }
  }

  private void printAnnotationValue(AST ast, Object value) {
    if (value == null) {
      print("nil");
    } else if (value instanceof IVariableBinding) {
      IVariableBinding var = (IVariableBinding) value;
      ITypeBinding declaringClass = var.getDeclaringClass();
      printf("%s_get_%s()", NameTable.getFullName(declaringClass), var.getName());
    } else if (value instanceof ITypeBinding) {
      ITypeBinding type = (ITypeBinding) value;
      printf("[[%s class] getClass]", NameTable.getFullName(type));
    } else if (value instanceof String) {
      StringLiteral node = ast.newStringLiteral();
      node.setLiteralValue((String) value);
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
        printAnnotationValue(ast, array[i]);
      }
      printf(" } count:%d type:[[NSObject class] getClass]]", array.length);
    } else {
      assert false : "unknown annotation value type";
    }
  }

  private void printMetadata(AbstractTypeDeclaration node) {
    print(new MetadataGenerator(node).getMetadataSource());
  }
}
