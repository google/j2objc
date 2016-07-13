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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.VariableDeclarationFragment;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.jdt.JdtElements;
import com.google.devtools.j2objc.types.Types;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.FileUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.UnicodeUtils;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.lang.model.element.PackageElement;

/**
 * The base class for TypeDeclarationGenerator and TypeImplementationGenerator,
 * providing common routines.
 *
 * @author Tom Ball, Keith Stanger
 */
public abstract class TypeGenerator extends AbstractSourceGenerator {

  // Convenient fields for use by subclasses.
  protected final AbstractTypeDeclaration typeNode;
  protected final ITypeBinding typeBinding;
  protected final CompilationUnit compilationUnit;
  protected final Types typeEnv;
  protected final NameTable nameTable;
  protected final String typeName;
  protected final boolean hasNullabilityAnnotations;

  private final List<BodyDeclaration> declarations;
  private final boolean parametersNonnullByDefault;

  protected TypeGenerator(SourceBuilder builder, AbstractTypeDeclaration node) {
    super(builder);
    typeNode = node;
    typeBinding = node.getTypeBinding();
    compilationUnit = TreeUtil.getCompilationUnit(node);
    typeEnv = compilationUnit.getTypeEnv();
    nameTable = compilationUnit.getNameTable();
    typeName = nameTable.getFullName(typeBinding);
    declarations = filterDeclarations(node.getBodyDeclarations());
    parametersNonnullByDefault = Options.nullability()
        && areParametersNonnullByDefault();
    hasNullabilityAnnotations = Options.nullability()
        && (parametersNonnullByDefault || hasNullabilityAnnotations());
  }

  protected boolean shouldPrintDeclaration(BodyDeclaration decl) {
    return true;
  }

  private List<BodyDeclaration> filterDeclarations(Iterable<BodyDeclaration> declarations) {
    List<BodyDeclaration> filteredDecls = Lists.newArrayList();
    for (BodyDeclaration decl : declarations) {
      if (shouldPrintDeclaration(decl)) {
        filteredDecls.add(decl);
      }
    }
    return filteredDecls;
  }

  private static final Predicate<VariableDeclarationFragment> IS_STATIC_FIELD =
      new Predicate<VariableDeclarationFragment>() {
    @Override
    public boolean apply(VariableDeclarationFragment frag) {
      // isGlobalVar includes non-static but final primitives, which are treated
      // like static fields in J2ObjC.
      return BindingUtil.isGlobalVar(frag.getVariableBinding());
    }
  };

  private static final Predicate<VariableDeclarationFragment> IS_INSTANCE_FIELD =
      new Predicate<VariableDeclarationFragment>() {
    @Override
    public boolean apply(VariableDeclarationFragment frag) {
      return BindingUtil.isInstanceVar(frag.getVariableBinding());
    }
  };

  private static final Predicate<BodyDeclaration> IS_OUTER_DECL = new Predicate<BodyDeclaration>() {
    @Override
    public boolean apply(BodyDeclaration decl) {
      switch (decl.getKind()) {
        case FUNCTION_DECLARATION:
          return true;
        case NATIVE_DECLARATION:
          return ((NativeDeclaration) decl).isOuter();
        default:
          return false;
      }
    }
  };

  private static final Predicate<BodyDeclaration> IS_INNER_DECL = new Predicate<BodyDeclaration>() {
    @Override
    public boolean apply(BodyDeclaration decl) {
      switch (decl.getKind()) {
        case METHOD_DECLARATION:
          return true;
        case NATIVE_DECLARATION:
          return !((NativeDeclaration) decl).isOuter();
        default:
          return false;
      }
    }
  };

  // This predicate returns true if the declaration generates implementation
  // code inside a @implementation declaration.
  private static final Predicate<BodyDeclaration> HAS_INNER_IMPL =
      new Predicate<BodyDeclaration>() {
    @Override
    public boolean apply(BodyDeclaration decl) {
      return decl.getKind() == TreeNode.Kind.METHOD_DECLARATION
          && !Modifier.isAbstract(((MethodDeclaration) decl).getModifiers());
    }
  };

  protected abstract void printFunctionDeclaration(FunctionDeclaration decl);
  protected abstract void printMethodDeclaration(MethodDeclaration decl);
  protected abstract void printNativeDeclaration(NativeDeclaration decl);

  private void printDeclaration(BodyDeclaration declaration) {
    switch (declaration.getKind()) {
      case FUNCTION_DECLARATION:
        printFunctionDeclaration((FunctionDeclaration) declaration);
        return;
      case METHOD_DECLARATION:
        printMethodDeclaration((MethodDeclaration) declaration);
        return;
      case NATIVE_DECLARATION:
        printNativeDeclaration((NativeDeclaration) declaration);
        return;
      default:
        break;
    }
  }

  protected void printDeclarations(Iterable<? extends BodyDeclaration> declarations) {
    for (BodyDeclaration declaration : declarations) {
      printDeclaration(declaration);
    }
  }

  protected boolean isInterfaceType() {
    return typeBinding.isInterface();
  }

  protected Iterable<VariableDeclarationFragment> getInstanceFields() {
    return getInstanceFields(declarations);
  }

  protected Iterable<VariableDeclarationFragment> getAllInstanceFields() {
    return getInstanceFields(typeNode.getBodyDeclarations());
  }

  private Iterable<VariableDeclarationFragment> getInstanceFields(List<BodyDeclaration> decls) {
    return Iterables.filter(
        TreeUtil.asFragments(Iterables.filter(decls, FieldDeclaration.class)),
        IS_INSTANCE_FIELD);
  }

  protected Iterable<VariableDeclarationFragment> getStaticFields() {
    return Iterables.filter(
        TreeUtil.asFragments(Iterables.filter(declarations, FieldDeclaration.class)),
        IS_STATIC_FIELD);
  }

  protected Iterable<BodyDeclaration> getInnerDeclarations() {
    return Iterables.filter(declarations, IS_INNER_DECL);
  }

  protected Iterable<BodyDeclaration> getOuterDeclarations() {
    return Iterables.filter(declarations, IS_OUTER_DECL);
  }

  protected void printInnerDeclarations() {
    printDeclarations(getInnerDeclarations());
  }

  protected void printOuterDeclarations() {
    printDeclarations(getOuterDeclarations());
  }

  private boolean hasStaticAccessorMethods() {
    if (!Options.staticAccessorMethods()) {
      return false;
    }
    for (VariableDeclarationFragment fragment : TreeUtil.getAllFields(typeNode)) {
      if (BindingUtil.isStatic(fragment.getVariableBinding())
          && !((FieldDeclaration) fragment.getParent()).hasPrivateDeclaration()) {
        return true;
      }
    }
    return false;
  }

  protected boolean needsPublicCompanionClass() {
    if (typeNode.hasPrivateDeclaration()) {
      return false;
    }
    return hasInitializeMethod()
        || hasStaticAccessorMethods()
        || BindingUtil.isRuntimeAnnotation(typeBinding)
        || BindingUtil.hasDefaultMethodsInFamily(typeBinding)
        || BindingUtil.hasStaticInterfaceMethods(typeBinding);
  }

  protected boolean needsCompanionClass() {
    return needsPublicCompanionClass()
        || !Iterables.isEmpty(Iterables.filter(typeNode.getBodyDeclarations(), HAS_INNER_IMPL));
  }

  protected boolean hasInitializeMethod() {
    return !typeNode.getClassInitStatements().isEmpty();
  }

  protected String getDeclarationType(IVariableBinding var) {
    ITypeBinding type = var.getType();
    if (BindingUtil.isVolatile(var)) {
      return "volatile_" + NameTable.getPrimitiveObjCType(type);
    } else {
      return nameTable.getObjCType(type);
    }
  }

  /**
   * Create an Objective-C method signature string.
   */
  protected String getMethodSignature(MethodDeclaration m) {
    StringBuilder sb = new StringBuilder();
    IMethodBinding binding = m.getMethodBinding();
    char prefix = Modifier.isStatic(m.getModifiers()) ? '+' : '-';
    String returnType = nameTable.getObjCType(binding.getReturnType());
    String selector = nameTable.getMethodSelector(binding);
    if (m.isConstructor()) {
      returnType = "instancetype";
    } else if (selector.equals("hash")) {
      // Explicitly test hashCode() because of NSObject's hash return value.
      returnType = "NSUInteger";
    }
    sb.append(UnicodeUtils.format("%c (%s%s)", prefix, returnType, nullability(binding, false)));

    List<SingleVariableDeclaration> params = m.getParameters();
    String[] selParts = selector.split(":");

    if (params.isEmpty()) {
      assert selParts.length == 1 && !selector.endsWith(":");
      sb.append(selParts[0]);
    } else {
      assert params.size() == selParts.length;
      int baseLength = sb.length() + selParts[0].length();
      for (int i = 0; i < params.size(); i++) {
        if (i != 0) {
          sb.append('\n');
          sb.append(pad(baseLength - selParts[i].length()));
        }
        IVariableBinding var = params.get(i).getVariableBinding();
        String typeName = nameTable.getObjCType(var.getType());
        sb.append(UnicodeUtils.format("%s:(%s%s)%s", selParts[i], typeName, nullability(var, true),
            nameTable.getVariableShortName(var)));
      }
    }

    return sb.toString();
  }

  /**
   * Returns an Objective-C nullability attribute string if there is a matching
   * JSR305 annotation, or an empty string.
   */
  private String nullability(IBinding binding, boolean isParameter) {
    if (Options.nullability()) {
      if (BindingUtil.hasNullableAnnotation(binding)) {
        return " __nullable";
      }
      if (BindingUtil.hasNonnullAnnotation(binding)) {
        return " __nonnull";
      }
      if (isParameter && !((IVariableBinding) binding).getType().isPrimitive()
          && (parametersNonnullByDefault || BindingUtil.hasNonnullAnnotation(binding))) {
        return " __nonnull";
      }
    }
    return "";
  }

  private boolean areParametersNonnullByDefault() {
    if (BindingUtil.hasAnnotation(typeBinding, ParametersAreNonnullByDefault.class)) {
      return true;
    }
    try {
      PackageElement pkg =
          JdtElements.getInstance().getPackageOf(BindingConverter.getElement(typeBinding));
      String pkgName = pkg.getQualifiedName().toString();
      // See if a package-info source file has a ParametersAreNonnullByDefault annotation.
      InputFile file = FileUtil.findOnSourcePath(pkgName + ".package-info");
      if (file != null) {
        String pkgInfo = FileUtil.readFile(file);
        if (pkgInfo.indexOf("@ParametersAreNonnullByDefault") >= 0) {
          return true;
        }
        if (pkgInfo.indexOf("@javax.annotation.ParametersAreNonnullByDefault") >= 0) {
          return true;
        }
      }

      // See if the package-info class file has it.
      final boolean[] result = new boolean[1];
      file = FileUtil.findOnClassPath(pkgName + ".package-info");
      if (file != null) {
        ClassReader classReader = new ClassReader(file.getInputStream());
        classReader.accept(new ClassVisitor(Opcodes.ASM5) {
          @Override
          public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if (desc.equals("Ljavax/annotation/ParametersAreNonnullByDefault;")) {
              result[0] = true;
            }
            return null;
          }
        }, 0);
        return result[0];
      }
    } catch (IOException e) {
      // fall-through
    }
    return false;
  }

  private boolean hasNullabilityAnnotations() {
    final boolean[] hasAnnotation = new boolean[1];
    typeNode.accept(new TreeVisitor() {
      @Override
      public void endVisit(MethodDeclaration node) {
        IMethodBinding method = node.getMethodBinding();
        if (BindingUtil.hasNullableAnnotation(method)
            || BindingUtil.hasNonnullAnnotation(method)) {
          hasAnnotation[0] = true;
        } else {
          for (SingleVariableDeclaration param : node.getParameters()) {
            IVariableBinding paramBinding = param.getVariableBinding();
            if (BindingUtil.hasNullableAnnotation(paramBinding)
                || BindingUtil.hasNonnullAnnotation(paramBinding)) {
              hasAnnotation[0] = true;
              break;
            }
          }
        }
      }
    });
    return hasAnnotation[0];
  }

  protected String getFunctionSignature(FunctionDeclaration function) {
    StringBuilder sb = new StringBuilder();
    String returnType = nameTable.getObjCType(function.getReturnType().getTypeBinding());
    returnType += returnType.endsWith("*") ? "" : " ";
    sb.append(returnType).append(function.getName()).append('(');
    for (Iterator<SingleVariableDeclaration> iter = function.getParameters().iterator();
         iter.hasNext(); ) {
      IVariableBinding var = iter.next().getVariableBinding();
      String paramType = nameTable.getObjCType(var.getType());
      paramType += (paramType.endsWith("*") ? "" : " ");
      sb.append(paramType + nameTable.getVariableShortName(var));
      if (iter.hasNext()) {
        sb.append(", ");
      }
    }
    sb.append(')');
    return sb.toString();
  }

  protected String generateExpression(Expression expr) {
    return StatementGenerator.generate(expr, getBuilder().getCurrentLine());
  }
}
