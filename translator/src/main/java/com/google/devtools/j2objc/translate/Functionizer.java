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

package com.google.devtools.j2objc.translate;

import com.google.common.collect.Sets;
import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.ConstructorInvocation;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.ExpressionStatement;
import com.google.devtools.j2objc.ast.FieldAccess;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.MethodInvocation;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.NormalAnnotation;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.ReturnStatement;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleMemberAnnotation;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.SuperConstructorInvocation;
import com.google.devtools.j2objc.ast.SuperFieldAccess;
import com.google.devtools.j2objc.ast.SuperMethodInvocation;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.gen.SignatureGenerator;
import com.google.devtools.j2objc.types.FunctionBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.IOSMethodBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TranslationUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.VariableElement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;

/**
 * Converts methods that don't need dynamic dispatch to C functions. This optimization
 * initially just targets private methods, but will be expanded to include final methods
 * that don't override superclass methods.
 *
 * @author Tom Ball
 */
public class Functionizer extends TreeVisitor {

  private Set<IMethodBinding> functionizableMethods;

  @Override
  public boolean visit(CompilationUnit node) {
    functionizableMethods = determineFunctionizableMethods(node);
    return true;
  }

  /**
   * Determines the set of methods to functionize. In addition to a method being
   * final we must also find an invocation for that method. Static methods, though,
   * are always functionized since there are no dynamic dispatch issues.
   */
  private Set<IMethodBinding> determineFunctionizableMethods(final CompilationUnit unit) {
    final Set<IMethodBinding> functionizableDeclarations = Sets.newHashSet();
    final Set<IMethodBinding> invocations = Sets.newHashSet();
    unit.accept(new TreeVisitor() {
      @Override
      public void endVisit(MethodDeclaration node) {
        if (canFunctionize(node)) {
          functionizableDeclarations.add(node.getMethodBinding());
        }
      }

      @Override
      public void endVisit(MethodInvocation node) {
        invocations.add(node.getMethodBinding().getMethodDeclaration());
      }
    });
    return Sets.intersection(functionizableDeclarations, invocations);
  }

  @Override
  public boolean visit(AnnotationTypeDeclaration node) {
    return false;
  }

  @Override
  public boolean visit(NormalAnnotation node) {
    return false;
  }

  @Override
  public boolean visit(SingleMemberAnnotation node) {
    return false;
  }

  /**
   * Determines whether an instance method can be functionized.
   */
  private boolean canFunctionize(MethodDeclaration node) {
    IMethodBinding m = node.getMethodBinding();
    int modifiers = node.getModifiers();

    // Never functionize these types of methods.
    if (Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers)
        || BindingUtil.isSynthetic(modifiers) || m.isAnnotationMember()) {
      return false;
    }

    // Don't functionize equals/hash, since they are often called by collections.
    String name = m.getName();
    if ((name.equals("hashCode") && m.getParameterTypes().length == 0)
        || (name.equals("equals") && m.getParameterTypes().length == 1)) {
      return false;
    }

    if (!BindingUtil.isPrivate(m) && !BindingUtil.isFinal(m)) {
      return false;
    }

    return !hasSuperMethodInvocation(node);
  }

  private static boolean hasSuperMethodInvocation(MethodDeclaration node) {
    final boolean[] result = new boolean[1];
    result[0] = false;
    node.accept(new TreeVisitor() {
      @Override
      public void endVisit(SuperMethodInvocation node) {
        result[0] = true;
      }
    });
    return result[0];
  }

  private FunctionBinding newFunctionBinding(IMethodBinding method) {
    ITypeBinding declaringClass = method.getDeclaringClass();
    FunctionBinding binding = new FunctionBinding(
        nameTable.getFullFunctionName(method), method.getReturnType(), declaringClass);
    if (method.isConstructor() || !BindingUtil.isStatic(method)) {
      binding.addParameters(declaringClass);
    }
    binding.addParameters(method.getParameterTypes());
    return binding;
  }

  private FunctionBinding newAllocatingConstructorBinding(IMethodBinding method) {
    ITypeBinding declaringClass = method.getDeclaringClass();
    FunctionBinding binding = new FunctionBinding(
        nameTable.getReleasingConstructorName(method),
        nameTable.getAllocatingConstructorName(method), declaringClass, declaringClass);
    binding.addParameters(method.getParameterTypes());
    return binding;
  }

  @Override
  public void endVisit(MethodInvocation node) {
    IMethodBinding binding = node.getMethodBinding().getMethodDeclaration();
    if (!BindingUtil.isStatic(binding) && !functionizableMethods.contains(binding)) {
      return;
    }

    FunctionInvocation functionInvocation = new FunctionInvocation(
        newFunctionBinding(binding), node.getTypeBinding());
    List<Expression> args = functionInvocation.getArguments();
    TreeUtil.moveList(node.getArguments(), args);

    if (!BindingUtil.isStatic(binding)) {
      Expression expr = node.getExpression();
      if (expr == null) {
        ITypeBinding thisClass = TreeUtil.getOwningType(node).getTypeBinding();
        expr = new ThisExpression(thisClass);
      }
      args.add(0, TreeUtil.remove(expr));
    }

    node.replaceWith(functionInvocation);
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    IMethodBinding binding = node.getMethodBinding().getMethodDeclaration();
    // Yes, super method invocations can be static.
    if (!BindingUtil.isStatic(binding)) {
      return;
    }

    FunctionInvocation functionInvocation = new FunctionInvocation(
        newFunctionBinding(binding), node.getTypeBinding());
    TreeUtil.moveList(node.getArguments(), functionInvocation.getArguments());
    node.replaceWith(functionInvocation);
  }

  private void visitConstructorInvocation(
      Statement node, IMethodBinding binding, List<Expression> args) {
    FunctionInvocation invocation = new FunctionInvocation(
        newFunctionBinding(binding), binding.getReturnType());
    invocation.addArgument(new ThisExpression(binding.getDeclaringClass()));
    TreeUtil.moveList(args, invocation.getArguments());
    node.replaceWith(new ExpressionStatement(invocation));
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    visitConstructorInvocation(node, node.getMethodBinding(), node.getArguments());
  }

  @Override
  public void endVisit(ConstructorInvocation node) {
    visitConstructorInvocation(node, node.getMethodBinding(), node.getArguments());
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    IMethodBinding binding = node.getMethodBinding();
    ITypeBinding type = binding.getDeclaringClass();
    FunctionInvocation invocation =
        new FunctionInvocation(newAllocatingConstructorBinding(binding), type);
    invocation.setHasRetainedResult(node.hasRetainedResult() || Options.useARC());
    TreeUtil.moveList(node.getArguments(), invocation.getArguments());
    node.replaceWith(invocation);
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    IMethodBinding binding = node.getMethodBinding();
    // Don't functionize synthetic methods like dealloc or __annotations, since
    // they are added by the translator and need to remain in method form.
    if (BindingUtil.isSynthetic(binding)) {
      return;
    }
    boolean isInstanceMethod = !BindingUtil.isStatic(binding) && !binding.isConstructor();
    boolean isDefaultMethod = Modifier.isDefault(node.getModifiers());
    FunctionDeclaration function = null;
    List<BodyDeclaration> declarationList = TreeUtil.asDeclarationSublist(node);
    List<String> extraSelectors = nameTable.getExtraSelectors(binding);
    if (!isInstanceMethod || isDefaultMethod || Modifier.isNative(node.getModifiers())
        || functionizableMethods.contains(binding) || !extraSelectors.isEmpty()) {
      ITypeBinding declaringClass = binding.getDeclaringClass();
      boolean isEnumConstructor = binding.isConstructor() && declaringClass.isEnum();
      function = makeFunction(node);
      for (String selector : extraSelectors) {
        declarationList.add(makeExtraMethodDeclaration(node, selector));
      }
      declarationList.add(function);
      if (binding.isConstructor() && !BindingUtil.isAbstract(declaringClass)
          && !isEnumConstructor) {
        declarationList.add(makeAllocatingConstructor(node, false));
        declarationList.add(makeAllocatingConstructor(node, true));
      } else if (isEnumConstructor && Options.useARC()) {
        // Enums with ARC need the retaining constructor.
        declarationList.add(makeAllocatingConstructor(node, false));
      }
      // Instance methods must be kept in case they are invoked using "super".
      boolean keepMethod = isInstanceMethod
          // Public methods must be kept for the public API.
          || !(BindingUtil.isPrivateInnerType(declaringClass) || BindingUtil.isPrivate(binding))
          // Methods must be kept for reflection if enabled.
          || (TranslationUtil.needsReflection(declaringClass) && !isEnumConstructor);
      if (keepMethod) {
        setFunctionCaller(node, binding);
      } else {
        node.remove();
      }
      ErrorUtil.functionizedMethod();
    }
  }

  private MethodDeclaration makeExtraMethodDeclaration(
      MethodDeclaration original, String selector) {
    IMethodBinding originalBinding = original.getMethodBinding();
    IOSMethodBinding binding = IOSMethodBinding.newMappedMethod(selector, originalBinding);
    MethodDeclaration declaration = new MethodDeclaration(binding);
    TreeUtil.copyList(original.getParameters(), declaration.getParameters());
    setFunctionCaller(declaration, originalBinding);
    return declaration;
  }

  /**
   * Create an equivalent function declaration for a given method.
   */
  private FunctionDeclaration makeFunction(MethodDeclaration method) {
    IMethodBinding m = method.getMethodBinding();
    ITypeBinding declaringClass = m.getDeclaringClass();
    boolean isInstanceMethod = !BindingUtil.isStatic(m) && !m.isConstructor();

    FunctionDeclaration function = new FunctionDeclaration(
        nameTable.getFullFunctionName(m), m.getReturnType(), declaringClass);
    function.setJniSignature(SignatureGenerator.createJniFunctionSignature(m));
    function.setLineNumber(method.getName().getLineNumber());

    if (!BindingUtil.isStatic(m)) {
      GeneratedVariableBinding var = new GeneratedVariableBinding(NameTable.SELF_NAME, 0,
          declaringClass, false, true, declaringClass, null);
      function.addParameter(new SingleVariableDeclaration(var));
    }
    TreeUtil.copyList(method.getParameters(), function.getParameters());

    function.setModifiers(method.getModifiers() & Modifier.STATIC);
    if (BindingUtil.isPrivate(m) || (isInstanceMethod && !BindingUtil.isDefault(m))) {
      function.addModifiers(Modifier.PRIVATE);
    } else {
      function.addModifiers(Modifier.PUBLIC);
    }

    if (Modifier.isNative(method.getModifiers())) {
      function.addModifiers(Modifier.NATIVE);
      return function;
    }

    function.setBody(TreeUtil.remove(method.getBody()));

    if (BindingUtil.isStatic(m)) {
      // Add class initialization invocation, since this may be the first use of this class.
      String initName = UnicodeUtils.format("%s_initialize", nameTable.getFullName(declaringClass));
      ITypeBinding voidType = typeEnv.resolveJavaType("void");
      FunctionBinding initBinding = new FunctionBinding(initName, voidType, declaringClass);
      FunctionInvocation initCall = new FunctionInvocation(initBinding, voidType);
      function.getBody().addStatement(0, new ExpressionStatement(initCall));
    }

    if (!BindingUtil.isStatic(m)) {
      FunctionConverter.convert(function);
    }

    return function;
  }

  /**
   * Create a wrapper for a constructor that does the object allocation.
   */
  private FunctionDeclaration makeAllocatingConstructor(
      MethodDeclaration method, boolean releasing) {
    assert method.isConstructor();
    IMethodBinding binding = method.getMethodBinding();
    ITypeBinding declaringClass = binding.getDeclaringClass();

    String name = releasing ? nameTable.getReleasingConstructorName(binding)
        : nameTable.getAllocatingConstructorName(binding);
    FunctionDeclaration function = new FunctionDeclaration(name, declaringClass, declaringClass);
    function.setLineNumber(method.getName().getLineNumber());
    function.setModifiers(BindingUtil.isPrivate(binding) ? Modifier.PRIVATE : Modifier.PUBLIC);
    function.setReturnsRetained(!releasing);
    TreeUtil.copyList(method.getParameters(), function.getParameters());
    Block body = new Block();
    function.setBody(body);

    StringBuilder sb = new StringBuilder(releasing ? "J2OBJC_CREATE_IMPL(" : "J2OBJC_NEW_IMPL(");
    sb.append(nameTable.getFullName(declaringClass));
    sb.append(", ").append(nameTable.getFunctionName(binding));
    for (SingleVariableDeclaration param : function.getParameters()) {
      sb.append(", ").append(nameTable.getVariableQualifiedName(param.getVariableBinding()));
    }
    sb.append(")");
    body.addStatement(new NativeStatement(sb.toString()));

    return function;
  }

  /**
   *  Replace method block statements with single statement that invokes function.
   */
  private void setFunctionCaller(MethodDeclaration method, IMethodBinding methodBinding) {
    ITypeBinding returnType = methodBinding.getReturnType();
    ITypeBinding declaringClass = methodBinding.getDeclaringClass();
    Block body = new Block();
    method.setBody(body);
    method.removeModifiers(Modifier.NATIVE);
    List<Statement> stmts = body.getStatements();
    FunctionInvocation invocation = new FunctionInvocation(
        newFunctionBinding(methodBinding), returnType);
    List<Expression> args = invocation.getArguments();
    if (!BindingUtil.isStatic(methodBinding)) {
      args.add(new ThisExpression(methodBinding.getDeclaringClass()));
    }
    for (SingleVariableDeclaration param : method.getParameters()) {
      args.add(new SimpleName(param.getVariableBinding()));
    }
    if (BindingUtil.isVoid(returnType)) {
      stmts.add(new ExpressionStatement(invocation));
      if (methodBinding.isConstructor()) {
        stmts.add(new ReturnStatement(new ThisExpression(declaringClass)));
      }
    } else {
      stmts.add(new ReturnStatement(invocation));
    }
  }

  /**
   * Convert references to "this" in the function to a "self" parameter.
   */
  private static class FunctionConverter extends TreeVisitor {

    private final IVariableBinding selfParam;

    static void convert(FunctionDeclaration function) {
      IVariableBinding selfParam = function.getParameter(0).getVariableBinding();
      function.accept(new FunctionConverter(selfParam));
    }

    private FunctionConverter(IVariableBinding selfParam) {
      this.selfParam = selfParam;
    }

    @Override
    public boolean visit(FieldAccess node) {
      node.getExpression().accept(this);
      return false;
    }

    @Override
    public boolean visit(QualifiedName node) {
      node.getQualifier().accept(this);
      return false;
    }

    @Override
    public void endVisit(SimpleName node) {
      VariableElement var = TreeUtil.getVariableElement(node);
      if (var != null && ElementUtil.isField(var)) {
        // Convert name to self->name.
        node.replaceWith(new QualifiedName(var, new SimpleName(selfParam)));
      }
    }

    @Override
    public boolean visit(SuperFieldAccess node) {
      // Change super.field expression to self.field.
      SimpleName qualifier = new SimpleName(selfParam);
      FieldAccess newAccess = new FieldAccess(node.getVariableBinding(), qualifier);
      node.replaceWith(newAccess);
      return false;
    }

    @Override
    public void endVisit(ThisExpression node) {
      SimpleName self = new SimpleName(selfParam);
      node.replaceWith(self);
    }

    @Override
    public void endVisit(SuperMethodInvocation node) {
      // Super invocations won't work from a function. Setting the qualifier
      // will cause SuperMethodInvocationRewriter to rewrite this invocation.
      if (node.getQualifier() == null) {
        node.setQualifier(new SimpleName(selfParam));
      }
    }
  }
}
