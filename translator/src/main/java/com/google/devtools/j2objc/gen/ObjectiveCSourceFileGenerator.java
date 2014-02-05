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
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.IOSParameter;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.ASTUtil;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.text.BreakIterator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Generates source files from AST types.  This class handles common actions
 * shared by the header and implementation generators.
 *
 * @author Tom Ball
 */
public abstract class ObjectiveCSourceFileGenerator extends SourceFileGenerator {

  /**
   * Create a new generator.
   *
   * @param sourceFileName the name of the source file being translated
   * @param outputDirectory the top-level directory for output file(s)
   */
  protected ObjectiveCSourceFileGenerator(String sourceFileName, String source,
      CompilationUnit unit, boolean emitLineDirectives) {
    super(sourceFileName, source, unit, emitLineDirectives);
  }

  /**
   * Generate an output source file from the specified type declaration.
   */
  public void generate(AbstractTypeDeclaration node) {
    if (node instanceof TypeDeclaration) {
      generate((TypeDeclaration) node);
    } else if (node instanceof EnumDeclaration) {
      generate((EnumDeclaration) node);
    } else if (node instanceof AnnotationTypeDeclaration) {
      generate((AnnotationTypeDeclaration) node);
    }
  }

  protected abstract void generate(TypeDeclaration node);

  protected abstract void generate(EnumDeclaration node);

  protected abstract void generate(AnnotationTypeDeclaration node);

  public void save(CompilationUnit node) {
    save(getOutputFileName(node));
  }

  protected void printStaticFieldAccessors(
      List<FieldDeclaration> fields, List<MethodDeclaration> methods, boolean isInterface) {
    printStaticFieldAccessors(getStaticFieldsNeedingAccessors(fields, isInterface), methods);
  }

  protected void printStaticFieldAccessors(
      List<IVariableBinding> bindings, List<MethodDeclaration> methods) {
    for (IVariableBinding binding : bindings) {
      if (needsGetter(binding, methods)) {
        printStaticFieldGetter(binding);
      }
      if (!Modifier.isFinal(binding.getModifiers())) {
        if (binding.getType().isPrimitive()) {
          printStaticFieldReferenceGetter(binding);
        } else {
          printStaticFieldSetter(binding);
        }
      }
    }
  }

  protected List<IVariableBinding> getStaticFieldsNeedingAccessors(
      List<FieldDeclaration> fields, boolean isInterface) {
    List<IVariableBinding> bindings = Lists.newArrayList();
    for (FieldDeclaration f : fields) {
      if (Modifier.isStatic(f.getModifiers()) || isInterface) {
        for (VariableDeclarationFragment var : ASTUtil.getFragments(f)) {
          IVariableBinding binding = Types.getVariableBinding(var);
          bindings.add(binding);
        }
      }
    }
    return bindings;
  }

  /**
   * Returns true if a getter method is needed for a specified field.  The
   * heuristic used is to find a method that has the same name, returns the
   * same type, and has no parameters.  Obviously, lousy code can fail this
   * test, but it should work in practice with existing Java code standards.
   */
  private boolean needsGetter(IVariableBinding var, List<MethodDeclaration> methods) {
    String accessorName = NameTable.getStaticAccessorName(var.getName());
    ITypeBinding type = var.getType();
    for (MethodDeclaration methodDecl : methods) {
      IMethodBinding method = Types.getMethodBinding(methodDecl);
      if (method.getName().equals(accessorName) && method.getReturnType().isEqualTo(type)
          && method.getParameterTypes().length == 0) {
        return false;
      }
    }
    return true;
  }

  protected void printStaticFieldGetter(IVariableBinding var) {
    printf(staticFieldGetterSignature(var));
  }

  protected String staticFieldGetterSignature(IVariableBinding var) {
    String objcType = NameTable.getObjCType(var.getType());
    String accessorName = NameTable.getStaticAccessorName(var.getName());
    return String.format("+ (%s)%s", objcType, accessorName);
  }

  protected void printStaticFieldReferenceGetter(IVariableBinding var) {
    printf(staticFieldReferenceGetterSignature(var));
  }

  protected String staticFieldReferenceGetterSignature(IVariableBinding var) {
    String objcType = NameTable.getObjCType(var.getType());
    String accessorName = NameTable.getStaticAccessorName(var.getName());
    return String.format("+ (%s *)%sRef", objcType, accessorName);
  }

  protected void printStaticFieldSetter(IVariableBinding var) {
    printf(staticFieldSetterSignature(var));
  }

  protected String staticFieldSetterSignature(IVariableBinding var) {
    String objcType = NameTable.getObjCType(var.getType());
    String paramName = NameTable.getName(var);
    return String.format("+ (void)set%s:(%s)%s", NameTable.capitalize(var.getName()), objcType,
                         paramName);
  }

  /**
   * Print a list of methods.
   */
  protected void printMethods(List<MethodDeclaration> methods) {
    for (MethodDeclaration m : methods) {
      printMethod(m);
    }
  }

  protected void printMethod(MethodDeclaration m) {
    syncLineNumbers(m.getName());  // avoid doc-comment
    IMethodBinding binding = Types.getMethodBinding(m);
    IOSMethod iosMethod = IOSMethodBinding.getIOSMethod(binding);
    if (iosMethod != null) {
      print(mappedMethodDeclaration(m, iosMethod));
    } else if (m.isConstructor()) {
      print(constructorDeclaration(m));
    } else if (Modifier.isStatic(m.getModifiers()) &&
        NameTable.CLINIT_NAME.equals(m.getName().getIdentifier())) {
      printStaticConstructorDeclaration(m);
    } else {
      printNormalMethod(m);
    }
  }

  protected void printNormalMethod(MethodDeclaration m) {
    print(methodDeclaration(m));
  }

  /**
   * Create an Objective-C method or constructor declaration string for an
   * inlined method.
   */
  protected String mappedMethodDeclaration(MethodDeclaration method, IOSMethod mappedMethod) {
    StringBuffer sb = new StringBuffer();

    // Explicitly test hashCode() because of NSObject's hash return value.
    String baseDeclaration;
    if (mappedMethod.getName().equals("hash")) {
      baseDeclaration = "- (NSUInteger)hash";
    } else {
      baseDeclaration = String.format("%c (%s)%s",
          Modifier.isStatic(method.getModifiers()) ? '+' : '-',
          NameTable.getObjCType(Types.getTypeBinding(method.getReturnType2())),
          mappedMethod.getName());
    }

    sb.append(baseDeclaration);
    Iterator<IOSParameter> iosParameters = mappedMethod.getParameters().iterator();
    if (iosParameters.hasNext()) {
      List<SingleVariableDeclaration> parameters = ASTUtil.getParameters(method);
      IOSParameter first = iosParameters.next();
      SingleVariableDeclaration var = parameters.get(first.getIndex());
      addTypeAndName(first, var, sb);
      while (iosParameters.hasNext()) {
        sb.append(mappedMethod.isVarArgs() ? ", " : " ");
        IOSParameter next = iosParameters.next();
        sb.append(next.getParameterName());
        var = parameters.get(next.getIndex());
        addTypeAndName(next, var, sb);
      }
    }
    return sb.toString();
  }

  private void addTypeAndName(IOSParameter iosParameter, SingleVariableDeclaration var,
      StringBuffer sb) {
    sb.append(":(");
    sb.append(iosParameter.getType());
    sb.append(')');
    sb.append(var.getName().getIdentifier());
  }

  /**
   * Create an Objective-C method declaration string.
   */
  protected String methodDeclaration(MethodDeclaration m) {
    assert !m.isConstructor();
    StringBuffer sb = new StringBuffer();
    boolean isStatic = Modifier.isStatic(m.getModifiers());
    IMethodBinding binding = Types.getMethodBinding(m);
    String methodName = NameTable.getName(binding);
    String baseDeclaration = String.format("%c (%s)%s", isStatic ? '+' : '-',
        NameTable.getObjCType(binding.getReturnType()), methodName);
    sb.append(baseDeclaration);
    parametersDeclaration(binding, ASTUtil.getParameters(m), baseDeclaration, sb);
    return sb.toString();
  }

  /**
   * Create an Objective-C constructor declaration string.
   */
  protected String constructorDeclaration(MethodDeclaration m) {
    return constructorDeclaration(m, /* isInner */ false);
  }

  protected String constructorDeclaration(MethodDeclaration m, boolean isInner) {
    assert m.isConstructor();
    StringBuffer sb = new StringBuffer();
    IMethodBinding binding = Types.getMethodBinding(m);
    String baseDeclaration = "- (id)init";
    if (isInner) {
      baseDeclaration += NameTable.getFullName(binding.getDeclaringClass());
    }
    sb.append(baseDeclaration);
    parametersDeclaration(binding, ASTUtil.getParameters(m), baseDeclaration, sb);
    return sb.toString();
  }

  /**
   * Create an Objective-C constructor from a list of annotation member
   * declarations.
   */
  protected String annotationConstructorDeclaration(ITypeBinding annotation) {
    StringBuffer sb = new StringBuffer();
    sb.append("- (id)init");
    IMethodBinding[] members = BindingUtil.getSortedAnnotationMembers(annotation);
    for (int i = 0; i < members.length; i++) {
      if (i == 0) {
        sb.append("With");
      } else {
        sb.append(" with");
      }
      IMethodBinding member = members[i];
      sb.append(NameTable.capitalize(member.getName()));
      sb.append(":(");
      sb.append(NameTable.getSpecificObjCType(member.getReturnType()));
      sb.append(')');
      sb.append(member.getName());
      sb.append('_');
    }
    return sb.toString();
  }

  /**
   * Print an Objective-C constructor declaration string.
   */
  protected abstract void printStaticConstructorDeclaration(MethodDeclaration m);

  private void parametersDeclaration(IMethodBinding method, List<SingleVariableDeclaration> params,
      String baseDeclaration, StringBuffer sb) throws AssertionError {
    method = BindingUtil.getOriginalMethodBinding(method);
    if (!params.isEmpty()) {
      ITypeBinding[] parameterTypes = method.getParameterTypes();
      boolean first = true;
      int nParams = params.size();
      for (int i = 0; i < nParams; i++) {
        SingleVariableDeclaration param = params.get(i);
        String fieldName = getParameterName(param);
        if (fieldName.equals(Types.EMPTY_PARAMETER_NAME)) {
          fieldName = "";
        }
        ITypeBinding typeBinding = parameterTypes[i];
        String keyword = NameTable.parameterKeyword(typeBinding);
        if (first) {
          sb.append(NameTable.capitalize(keyword));
          baseDeclaration += keyword;
          first = false;
        } else {
          sb.append(pad(baseDeclaration.length() - keyword.length()));
          sb.append(keyword);
        }
        sb.append(String.format(":(%s)%s",
            NameTable.getSpecificObjCType(Types.getTypeBinding(param)), fieldName));
        if (i + 1 < nParams) {
          sb.append('\n');
        }
      }
    }
    if (method.isConstructor() && method.getDeclaringClass().isEnum()) {
      // If enum constant type, append name and ordinal.
      if (params.isEmpty()) {
        sb.append("WithNSString:(NSString *)__name withInt:(int)__ordinal");
      } else {
        sb.append('\n');
        String keyword = "withNSString";
        sb.append(pad(baseDeclaration.length() - keyword.length()));
        sb.append(keyword);
        sb.append(":(NSString *)__name\n");
        keyword = "withInt";
        sb.append(pad(baseDeclaration.length() - keyword.length()));
        sb.append(keyword);
        sb.append(":(int)__ordinal");
      }
    }
  }

  protected String getParameterName(SingleVariableDeclaration param) {
    String name = NameTable.getName(param.getName());
    if (NameTable.isReservedName(name)) {
      name += "Arg";
    }
    return name;
  }

  /** Ignores deprecation warnings. Deprecation warnings should be visible for human authored code,
   *  not transpiled code. This method should be paired with popIgnoreDeprecatedDeclarationsPragma.
   */
  protected void pushIgnoreDeprecatedDeclarationsPragma() {
    if (Options.generateDeprecatedDeclarations()) {
      printf("#pragma clang diagnostic push\n");
      printf("#pragma GCC diagnostic ignored \"-Wdeprecated-declarations\"\n");
    }
  }

  /** Restores deprecation warnings after a call to pushIgnoreDeprecatedDeclarationsPragma. */
  protected void popIgnoreDeprecatedDeclarationsPragma() {
    if (Options.generateDeprecatedDeclarations()) {
      printf("#pragma clang diagnostic pop\n");
    }
  }

  @SuppressWarnings("unchecked")
  protected void printDocComment(Javadoc javadoc) {
    if (javadoc != null) {
      newline();
      printIndent();
      println("/**");
      List<TagElement> tags = javadoc.tags();
      for (TagElement tag : tags) {

        if (tag.getTagName() == null) {
          // Description section.
          StringBuilder sb = new StringBuilder();

          // Each fragment is a source line, stripped of leading asterisk and trimmed.
          List<?> fragments = tag.fragments();
          for (Object fragment : fragments) {
            if (fragment instanceof TextElement) {
              if (sb.length() > 0) {
                sb.append(' ');
              }
              sb.append((TextElement) fragment);
            } else {
              sb.append(printJavadocTag((TagElement) fragment));
            }
          }

          // Extract first sentence from description.
          String description = sb.toString();
          sb = new StringBuilder();
          BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
          iterator.setText(description.toString());
          int start = iterator.first();
          int end = iterator.next();
          if (end != BreakIterator.DONE) {
            // Print brief tag first, since Quick Help shows it first. This makes the
            // generated source easier to review.
            printDocLine(String.format("@brief %s", description.substring(start, end)));
            String remainder = description.substring(end).trim();
            if (!remainder.isEmpty()) {
              printDocLine(remainder);
            }
          } else {
            printDocLine(description.trim());
          }
        } else {
          String doc = printJavadocTag(tag);
          if (!doc.isEmpty()) {
            printDocLine(doc);
          }
        }
      }
      printIndent();
      println(" */");
    }
  }

  private void printDocLine(String line) {
    printIndent();
    print(' ');
    println(line);
  }

  private String printJavadocTag(TagElement tag) {
    String tagName = tag.getTagName();
    // Xcode 5 compatible tags.
    if (tagName.equals(TagElement.TAG_AUTHOR) ||
        tagName.equals(TagElement.TAG_EXCEPTION) ||
        tagName.equals(TagElement.TAG_PARAM) ||
        tagName.equals(TagElement.TAG_RETURN) ||
        tagName.equals(TagElement.TAG_SINCE) ||
        tagName.equals(TagElement.TAG_THROWS) ||
        tagName.equals(TagElement.TAG_VERSION)) {
      return String.format("%s %s", tagName, printTagFragments(tag.fragments()));
    }

    if (tagName.equals(TagElement.TAG_DEPRECATED)) {
      // Deprecated annotation translated instead.
      return "";
    }

    if (tagName.equals(TagElement.TAG_SEE)) {
      // TODO(tball): implement @see when Xcode quick help links are documented.
      return "";
    }

    if (tagName.equals(TagElement.TAG_CODE)) {
      return String.format("<code>%s</code>", printTagFragments(tag.fragments()));
    }

    // Remove tag, but return any text it has.
    return printTagFragments(tag.fragments());
  }

  private String printTagFragments(List<?> fragments) {
    StringBuilder sb = new StringBuilder();
    for (Object fragment : fragments) {
      if (fragment instanceof TextElement) {
        if (sb.length() > 0) {
          sb.append(' ');
        }
        String text = escapeDocText(((TextElement) fragment).getText());
        sb.append(text);
      } else if (fragment instanceof TagElement) {
        sb.append(printJavadocTag((TagElement) fragment));
      } else {
        sb.append(escapeDocText(fragment.toString()));
      }
    }
    return sb.toString().trim();
  }

  private String escapeDocText(String text) {
    return text.replace("@", "@@").replace("/*", "/\\*");
  }
}
