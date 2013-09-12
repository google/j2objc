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
import com.google.common.collect.ListMultimap;
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
        if (node.isInterface()) {
          if (!getStaticFieldsNeedingAccessors(
              Arrays.asList(node.getFields()), /* isInterface */ true).isEmpty()) {
            types.add(node);
          }
        } else {
          types.add(node); // always print concrete types
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
        if (BindingUtil.isRuntimeAnnotation(Types.getTypeBinding(node))) {
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
        invokedConstructors.add(parameterKey(Types.getMethodBinding(node)));
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
            print(nativeCode + "\n\n");
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
        printNormalMethod(method);
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
    printMethodsAndOcni(node, methods, blockComments.get(node));

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
    printf("    %s *e = %s_values->buffer_[i];\n", typeName, typeName);
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
    String methodBody = generateMethodBody(m);
    if (methodBody == null) {
      return "";
    }
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
    String methodBody = generateMethodBody(method);
    if (methodBody == null) {
      return "";
    }
    return super.mappedMethodDeclaration(method, mappedMethod)
        + " " + reindent(methodBody) + "\n\n";
  }

  private String generateMethodBody(MethodDeclaration m) {
    if (Modifier.isNative(m.getModifiers())) {
      if (hasNativeCode(m, true)) {
        return extractNativeMethodBody(m);
      } else if (Options.generateNativeStubs()) {
        return generateNativeStub(m);
      } else {
        return null;
      }
    }
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
  protected String constructorDeclaration(MethodDeclaration m) {
    String methodBody;
    IMethodBinding binding = Types.getMethodBinding(m);
    boolean memDebug = Options.memoryDebug();
    List<Statement> statements = ASTUtil.getStatements(m.getBody());
    if (binding.getDeclaringClass().isEnum()) {
      return enumConstructorDeclaration(m, statements, binding);
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
    String invocation = generateStatement(statements.get(0), false) + ";\n";
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
        sb.append(generateStatement(statements.get(i), false));
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
      sb.append(generateStatement(statement, false));
    }
    sb.append("}\n}");
    print("+ (void)initialize " + reindent(sb.toString()) + "\n\n");
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
      printAnnotationValue(annotation.getAST(), value);
    }
  }

  private void printAnnotationValue(AST ast, Object value) {
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
      printf(" } count:%d type:[NSObject getClass]]", array.length);
    } else {
      assert false : "unknown annotation value type";
    }
  }
}
