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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Javadoc;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TagElement;
import com.google.devtools.j2objc.ast.TextElement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.types.IOSMethod;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.types.IOSParameter;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.NameTable;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

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
   * @param unit The AST of the source to generate
   * @param emitLineDirectives if true, generate CPP line directives
   */
  protected ObjectiveCSourceFileGenerator(CompilationUnit unit, boolean emitLineDirectives) {
    super(unit, emitLineDirectives);
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

  private static final Function<VariableDeclaration, IVariableBinding> GET_VARIABLE_BINDING_FUNC =
      new Function<VariableDeclaration, IVariableBinding>() {
    public IVariableBinding apply(VariableDeclaration node) {
      return node.getVariableBinding();
    }
  };

  private static final Predicate<VariableDeclaration> IS_STATIC_VARIABLE_PRED =
      new Predicate<VariableDeclaration>() {
    public boolean apply(VariableDeclaration node) {
      return BindingUtil.isStatic(node.getVariableBinding());
    }
  };

  private static final Predicate<VariableDeclarationFragment> NEEDS_INITIALIZATION_PRED =
      new Predicate<VariableDeclarationFragment>() {
    public boolean apply(VariableDeclarationFragment frag) {
      IVariableBinding binding = frag.getVariableBinding();
      return BindingUtil.isStatic(binding) && !BindingUtil.isPrimitiveConstant(binding);
    }
  };

  protected Iterable<IVariableBinding> getStaticFieldsNeedingAccessors(
      AbstractTypeDeclaration node) {
    return Iterables.transform(
        Iterables.filter(TreeUtil.getAllFields(node), IS_STATIC_VARIABLE_PRED),
        GET_VARIABLE_BINDING_FUNC);
  }

  /**
   * Excludes primitive constants which will not have variables declared for them.
   */
  protected Iterable<VariableDeclarationFragment> getStaticFieldsNeedingInitialization(
      AbstractTypeDeclaration node) {
    return Iterables.filter(TreeUtil.getAllFields(node), NEEDS_INITIALIZATION_PRED);
  }

  protected boolean isInitializeMethod(MethodDeclaration method) {
    IMethodBinding m = method.getMethodBinding();
    return BindingUtil.isStatic(m) && NameTable.CLINIT_NAME.equals(m.getName())
        && m.getParameterTypes().length == 0 && BindingUtil.isSynthetic(m);
  }

  protected boolean hasInitializeMethod(
      AbstractTypeDeclaration node, List<MethodDeclaration> methods) {
    if (node instanceof EnumDeclaration) {
      return true;
    }
    for (MethodDeclaration m : methods) {
      if (isInitializeMethod(m)) {
        return true;
      }
    }
    return false;
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
    IMethodBinding binding = m.getMethodBinding();
    IOSMethod iosMethod = IOSMethodBinding.getIOSMethod(binding);
    if (iosMethod != null) {
      printMappedMethodDeclaration(m, iosMethod);
    } else if (m.isConstructor()) {
      printConstructor(m);
    } else if (isInitializeMethod(m)) {
      printStaticConstructorDeclaration(m);
    } else {
      printNormalMethod(m);
    }
  }

  protected abstract void printNormalMethod(MethodDeclaration m);

  protected abstract void printConstructor(MethodDeclaration m);

  /**
   * Print an Objective-C constructor declaration string.
   */
  protected abstract void printStaticConstructorDeclaration(MethodDeclaration m);

  protected abstract void printMappedMethodDeclaration(MethodDeclaration m, IOSMethod mappedMethod);

  /**
   * Create an Objective-C method or constructor declaration string for an
   * inlined method.
   */
  protected String mappedMethodDeclaration(MethodDeclaration method, IOSMethod mappedMethod) {
    if (mappedMethod.isFunction()) {
      return functionDeclaration(method, mappedMethod);
    }
    StringBuffer sb = new StringBuffer();

    // Explicitly test hashCode() because of NSObject's hash return value.
    String baseDeclaration;
    if (mappedMethod.getName().equals("hash")) {
      baseDeclaration = "- (NSUInteger)hash";
    } else {
      baseDeclaration = String.format("%c (%s)%s",
          Modifier.isStatic(method.getModifiers()) ? '+' : '-',
          NameTable.getObjCType(method.getReturnType().getTypeBinding()),
          mappedMethod.getName());
    }

    sb.append(baseDeclaration);
    Iterator<IOSParameter> iosParameters = mappedMethod.getParameters().iterator();
    if (iosParameters.hasNext()) {
      List<SingleVariableDeclaration> parameters = method.getParameters();
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

  protected String functionDeclaration(MethodDeclaration method, IOSMethod mappedMethod) {
    StringBuffer sb = new StringBuffer();
    IMethodBinding m = method.getMethodBinding();
    sb.append(BindingUtil.isStatic(m) ? "" : "__attribute__ ((unused)) static ");
    Type returnType = method.getReturnType();
    sb.append(String.format("%s %s(",
        NameTable.getObjCType(returnType.getTypeBinding()),
        m.getName()));

    Iterator<SingleVariableDeclaration> parameters = method.getParameters().iterator();
    while (parameters.hasNext()) {
      IVariableBinding varType = parameters.next().getVariableBinding();
      sb.append(String.format("%s %s",
          NameTable.getObjCType(varType.getType()),
          NameTable.getName(varType)));
      if (parameters.hasNext()) {
        sb.append(", ");
      }
    }
    sb.append(')');
    return sb.toString();
  }

  /**
   * Create an Objective-C method declaration string.
   */
  protected String methodDeclaration(MethodDeclaration m) {
    assert !m.isConstructor();
    StringBuffer sb = new StringBuffer();
    boolean isStatic = Modifier.isStatic(m.getModifiers());
    IMethodBinding binding = m.getMethodBinding();
    String methodName = NameTable.getName(binding);
    String baseDeclaration = String.format("%c (%s)%s", isStatic ? '+' : '-',
        NameTable.getObjCType(binding.getReturnType()), methodName);
    sb.append(baseDeclaration);
    parametersDeclaration(binding, m.getParameters(), baseDeclaration, sb);
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
    IMethodBinding binding = m.getMethodBinding();
    String baseDeclaration = "- (instancetype)init";
    if (isInner) {
      baseDeclaration += NameTable.getFullName(binding.getDeclaringClass());
    }
    sb.append(baseDeclaration);
    parametersDeclaration(binding, m.getParameters(), baseDeclaration, sb);
    return sb.toString();
  }

  /**
   * Create an Objective-C constructor from a list of annotation member
   * declarations.
   */
  protected String annotationConstructorDeclaration(ITypeBinding annotation) {
    StringBuffer sb = new StringBuffer();
    sb.append("- (instancetype)init");
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
            NameTable.getSpecificObjCType(param.getVariableBinding().getType()), fieldName));
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
    String name = NameTable.getName(param.getName().getBinding());
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

  protected void printDocComment(Javadoc javadoc) {
    if (javadoc != null) {
      printIndent();
      println("/**");
      List<TagElement> tags = javadoc.getTags();
      for (TagElement tag : tags) {

        if (tag.getTagName() == null) {
          // Description section.
          String description = printTagFragments(tag.getFragments());

          // Extract first sentence from description.
          BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
          iterator.setText(description.toString());
          int start = iterator.first();
          int end = iterator.next();
          if (end != BreakIterator.DONE) {
            // Print brief tag first, since Quick Help shows it first. This makes the
            // generated source easier to review.
            printDocLine(String.format("@brief %s", description.substring(start, end).trim()));
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
    if (tagName.equals(TagElement.TAG_AUTHOR)
        || tagName.equals(TagElement.TAG_EXCEPTION)
        || tagName.equals(TagElement.TAG_PARAM)
        || tagName.equals(TagElement.TAG_RETURN)
        || tagName.equals(TagElement.TAG_SINCE)
        || tagName.equals(TagElement.TAG_THROWS)
        || tagName.equals(TagElement.TAG_VERSION)) {
      return String.format("%s %s", tagName, printTagFragments(tag.getFragments()));
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
      return String.format("<code>%s</code>", printTagFragments(tag.getFragments()));
    }

    // Remove tag, but return any text it has.
    return printTagFragments(tag.getFragments());
  }

  private String printTagFragments(List<TreeNode> fragments) {
    StringBuilder sb = new StringBuilder();
    for (TreeNode fragment : fragments) {
      sb.append(' ');
      if (fragment instanceof TextElement) {
        String text = escapeDocText(((TextElement) fragment).getText());
        sb.append(text.trim());
      } else if (fragment instanceof TagElement) {
        sb.append(printJavadocTag((TagElement) fragment));
      } else {
        sb.append(escapeDocText(fragment.toString()).trim());
      }
    }
    return sb.toString().trim();
  }

  private String escapeDocText(String text) {
    return text.replace("@", "@@").replace("/*", "/\\*");
  }

  @Override
  protected String getOutputFileName(CompilationUnit node) {
    String result = super.getOutputFileName(node);
    if (node.getMainTypeName().equals(NameTable.PACKAGE_INFO_MAIN_TYPE)) {
      return result.replace(NameTable.PACKAGE_INFO_MAIN_TYPE, NameTable.PACKAGE_INFO_FILE_NAME);
    }
    return result;
  }
}
