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

import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
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
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.types.FunctionElement;
import com.google.devtools.j2objc.types.GeneratedExecutableElement;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.CaptureInfo;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.NameTable;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.devtools.j2objc.util.UnicodeUtils;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Converts methods that don't need dynamic dispatch to C functions. This optimization
 * initially just targets private methods, but will be expanded to include final methods
 * that don't override superclass methods.
 *
 * @author Tom Ball
 */
public class Functionizer extends UnitTreeVisitor {

  private final CaptureInfo captureInfo;
  private Set<ExecutableElement> functionizableMethods;

  public Functionizer(CompilationUnit unit) {
    super(unit);
    captureInfo = unit.getEnv().captureInfo();
  }

  @Override
  public boolean visit(CompilationUnit node) {
    FunctionizableFinder finder = new FunctionizableFinder();
    node.accept(finder);
    functionizableMethods = finder.getFunctionizableMethods();
    return true;
  }

  static class MethodInfo {

    private Boolean functionizable = null;
    private Set<ExecutableElement> superCalls = new HashSet<>();

    private boolean resolveFunctionizable(Map<ExecutableElement, MethodInfo> infoMap) {
      for (ExecutableElement superCall : superCalls) {
        MethodInfo superCallInfo = infoMap.get(superCall);
        if (superCallInfo == null || !superCallInfo.isFunctionizable(infoMap)) {
          return false;
        }
      }
      return true;
    }

    private boolean isFunctionizable(Map<ExecutableElement, MethodInfo> infoMap) {
      if (functionizable == null) {
        functionizable = resolveFunctionizable(infoMap);
      }
      return functionizable;
    }

    private void addSuperCall(ExecutableElement method) {
      superCalls.add(method);
    }
  }

  private static class FunctionizableFinder extends TreeVisitor {

    // Don't need a stack here because local types have already been extracted.
    private MethodInfo currentMethod = null;
    private Map<ExecutableElement, MethodInfo> infoMap = new HashMap<>();
    private Set<ExecutableElement> invocations = new HashSet<>();

    @Override
    public boolean visit(MethodDeclaration node) {
      if (isFunctionizingCandidate(node)) {
        infoMap.put(node.getExecutableElement(), currentMethod = new MethodInfo());
      }
      return true;
    }

    @Override
    public void endVisit(MethodDeclaration node) {
      currentMethod = null;
    }

    @Override
    public void endVisit(MethodInvocation node) {
      ExecutableElement method = node.getExecutableElement();
      // Regular invocations can only be functionized if the target is private or final, otherwise
      // the target method might be overridden by a subclass. Private methods are always
      // functionized so we only check for final methods here.
      if (ElementUtil.isFinal(method)) {
        invocations.add(method);
      }
    }

    @Override
    public void endVisit(SuperMethodInvocation node) {
      ExecutableElement method = node.getExecutableElement();
      // Super invocations can always be functionized because it is not a "virtual" invocation.
      invocations.add(method);

      if (currentMethod != null) {
        currentMethod.addSuperCall(method);
      }
    }

    private Set<ExecutableElement> getFunctionizableMethods() {
      Iterator<ExecutableElement> iter = invocations.iterator();
      while (iter.hasNext()) {
        ExecutableElement method = iter.next();
        MethodInfo info = infoMap.get(method);
        if (info == null || !info.isFunctionizable(infoMap)) {
          iter.remove();
        }
      }
      return invocations;
    }
  }

  private static boolean isFunctionizingCandidate(MethodDeclaration node) {
    ExecutableElement method = node.getExecutableElement();
    int modifiers = node.getModifiers();

    // Default methods, static methods and constructors are always functionized. We only care about
    // regular instance methods.
    if (!ElementUtil.isInstanceMethod(method) || ElementUtil.isDefault(method)
        || Modifier.isAbstract(modifiers) || !node.hasDeclaration()) {
      return false;
    }

    // Don't functionize equals/hash, since they are often called by collections.
    String name = ElementUtil.getName(method);
    if ((name.equals("hashCode") && method.getParameters().isEmpty())
        || (name.equals("equals") && method.getParameters().size() == 1)) {
      return false;
    }

    return true;
  }

  private FunctionElement newFunctionElement(ExecutableElement method) {
    TypeElement declaringClass = ElementUtil.getDeclaringClass(method);
    FunctionElement element = new FunctionElement(
        nameTable.getFullFunctionName(method), method.getReturnType(), declaringClass);
    if (ElementUtil.isConstructor(method) || !ElementUtil.isStatic(method)) {
      element.addParameters(declaringClass.asType());
    }
    transferParams(method, element, declaringClass);
    return element;
  }

  private FunctionElement newAllocatingConstructorElement(ExecutableElement method) {
    TypeElement declaringClass = ElementUtil.getDeclaringClass(method);
    FunctionElement element = new FunctionElement(
        nameTable.getReleasingConstructorName(method),
        nameTable.getAllocatingConstructorName(method), declaringClass.asType(), declaringClass);
    transferParams(method, element, declaringClass);
    return element;
  }

  private void transferParams(
      ExecutableElement method, FunctionElement function, TypeElement declaringClass) {
    if (ElementUtil.isConstructor(method)) {
      function.addParameters(ElementUtil.asTypes(
          captureInfo.getImplicitPrefixParams(declaringClass)));
    }
    function.addParameters(ElementUtil.asTypes(method.getParameters()));
    if (ElementUtil.isConstructor(method)) {
      function.addParameters(ElementUtil.asTypes(
          captureInfo.getImplicitPostfixParams(declaringClass)));
    }
  }

  private void functionizeInvocation(
      Expression node, ExecutableElement element, Expression receiver,
      List<Expression> methodArgs) {
    FunctionInvocation functionInvocation =
        new FunctionInvocation(newFunctionElement(element), node.getTypeMirror());
    List<Expression> funcArgs = functionInvocation.getArguments();
    TreeUtil.moveList(methodArgs, funcArgs);

    if (!ElementUtil.isStatic(element)) {
      if (receiver == null) {
        receiver = new ThisExpression(TreeUtil.getEnclosingTypeElement(node).asType());
      }
      funcArgs.add(0, TreeUtil.remove(receiver));
    }

    node.replaceWith(functionInvocation);
  }

  @Override
  public void endVisit(MethodInvocation node) {
    ExecutableElement method = node.getExecutableElement();
    if (ElementUtil.isStatic(method) || ElementUtil.isPrivate(method)
        || (functionizableMethods.contains(method) && ElementUtil.isFinal(method))) {
      functionizeInvocation(node, method, node.getExpression(), node.getArguments());
    }
  }

  @Override
  public void endVisit(SuperMethodInvocation node) {
    ExecutableElement method = node.getExecutableElement();
    if (ElementUtil.isStatic(method) || ElementUtil.isPrivate(method)
        || functionizableMethods.contains(method) || ElementUtil.isDefault(method)) {
      functionizeInvocation(node, method, node.getReceiver(), node.getArguments());
    }
  }

  @Override
  public void endVisit(SuperConstructorInvocation node) {
    ExecutableElement element = node.getExecutableElement();
    AbstractTypeDeclaration typeDecl = TreeUtil.getEnclosingType(node);
    TypeElement type = typeDecl.getTypeElement();
    FunctionElement funcElement = newFunctionElement(element);
    FunctionInvocation invocation = new FunctionInvocation(funcElement, typeUtil.getVoid());
    List<Expression> args = invocation.getArguments();
    args.add(new ThisExpression(ElementUtil.getDeclaringClass(element).asType()));
    if (typeDecl instanceof TypeDeclaration) {
      TypeDeclaration typeDeclaration = (TypeDeclaration) typeDecl;
      if (captureInfo.needsOuterParam(ElementUtil.getSuperclass(type))) {
        Expression outerArg = TreeUtil.remove(node.getExpression());
        args.add(outerArg != null ? outerArg : typeDeclaration.getSuperOuter().copy());
      }
      TreeUtil.moveList(typeDeclaration.getSuperCaptureArgs(), args);
    }
    TreeUtil.moveList(node.getArguments(), args);
    if (ElementUtil.isEnum(type)) {
      for (VariableElement param : captureInfo.getImplicitEnumParams()) {
        args.add(new SimpleName(param));
      }
    }
    node.replaceWith(new ExpressionStatement(invocation));
    assert funcElement.getParameterTypes().size() == args.size();
  }

  @Override
  public void endVisit(ConstructorInvocation node) {
    ExecutableElement element = node.getExecutableElement();
    TypeElement declaringClass = ElementUtil.getDeclaringClass(element);
    FunctionElement funcElement = newFunctionElement(element);
    FunctionInvocation invocation = new FunctionInvocation(funcElement, typeUtil.getVoid());
    List<Expression> args = invocation.getArguments();
    args.add(new ThisExpression(declaringClass.asType()));
    for (VariableElement captureParam : captureInfo.getImplicitPrefixParams(declaringClass)) {
      args.add(new SimpleName(captureParam));
    }
    TreeUtil.moveList(node.getArguments(), args);
    for (VariableElement captureParam : captureInfo.getImplicitPostfixParams(declaringClass)) {
      args.add(new SimpleName(captureParam));
    }
    node.replaceWith(new ExpressionStatement(invocation));
    assert funcElement.getParameterTypes().size() == args.size();
  }

  @Override
  public void endVisit(ClassInstanceCreation node) {
    ExecutableElement element = node.getExecutableElement();
    TypeElement type = ElementUtil.getDeclaringClass(element);
    FunctionElement funcElement = newAllocatingConstructorElement(element);
    FunctionInvocation invocation = new FunctionInvocation(funcElement, node.getTypeMirror());
    invocation.setHasRetainedResult(node.hasRetainedResult() || options.useARC());
    List<Expression> args = invocation.getArguments();
    Expression outerExpr = node.getExpression();
    if (outerExpr != null) {
      args.add(TreeUtil.remove(outerExpr));
    } else if (captureInfo.needsOuterParam(type)) {
      args.add(new ThisExpression(ElementUtil.getDeclaringClass(type).asType()));
    }
    Expression superOuterArg = node.getSuperOuterArg();
    if (superOuterArg != null) {
      args.add(TreeUtil.remove(superOuterArg));
    }
    TreeUtil.moveList(node.getCaptureArgs(), args);
    TreeUtil.moveList(node.getArguments(), args);
    node.replaceWith(invocation);
    assert funcElement.getParameterTypes().size() == args.size();
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    ExecutableElement element = node.getExecutableElement();
    // Don't functionize certain ObjC methods like dealloc or __annotations, since
    // they are added by the translator and need to remain in method form.
    if (!node.hasDeclaration()) {
      return;
    }
    boolean isConstructor = ElementUtil.isConstructor(element);
    boolean isInstanceMethod = !ElementUtil.isStatic(element) && !isConstructor;
    boolean isDefaultMethod = ElementUtil.isDefault(element);
    List<BodyDeclaration> declarationList = TreeUtil.asDeclarationSublist(node);
    if (!isInstanceMethod || isDefaultMethod || Modifier.isNative(node.getModifiers())
        || ElementUtil.isPrivate(element) || functionizableMethods.contains(element)) {
      TypeElement declaringClass = ElementUtil.getDeclaringClass(element);
      boolean isEnumConstructor = isConstructor && ElementUtil.isEnum(declaringClass);
      if (isConstructor) {
        addImplicitParameters(node, declaringClass);
      }
      FunctionDeclaration function = makeFunction(node);
      declarationList.add(function);
      if (isConstructor && !ElementUtil.isAbstract(declaringClass) && !isEnumConstructor) {
        declarationList.add(makeAllocatingConstructor(node, false));
        declarationList.add(makeAllocatingConstructor(node, true));
      } else if (isEnumConstructor && options.useARC()) {
        // Enums with ARC need the retaining constructor.
        declarationList.add(makeAllocatingConstructor(node, false));
      }
      if (isDefaultMethod) {
        // For default methods keep only the declaration. Implementing classes will add a shim.
        node.setBody(null);
        node.addModifiers(Modifier.ABSTRACT);
      } else if (isInstanceMethod) {
        // We can remove private instance methods if reflection is stripped.
        if (translationUtil.needsReflection(declaringClass) || !ElementUtil.isPrivate(element)) {
          setFunctionCaller(node, element);
        } else {
          node.remove();
        }
      } else if (isEnumConstructor) {
        // Enum constructors are never needed.
        node.remove();
      } else if (translationUtil.needsReflection(declaringClass)) {
        // Static methods and constructors, keep for reflection.
        setFunctionCaller(node, element);
        if (!options.emitWrapperMethods()) {
          // Take out of the header, only needed for reflection.
          node.setHasDeclaration(false);
        }
      } else {
        // Static methods and constructors, no reflection.
        if (options.emitWrapperMethods() && !ElementUtil.isPrivateInnerType(declaringClass)
            && !ElementUtil.isPrivate(element)) {
          setFunctionCaller(node, element);
        } else {
          node.remove();
        }
      }
    }
  }

  private void addImplicitParameters(MethodDeclaration node, TypeElement type) {
    List<SingleVariableDeclaration> methodParams = node.getParameters().subList(0, 0);
    for (VariableElement param : captureInfo.getImplicitPrefixParams(type)) {
      methodParams.add(new SingleVariableDeclaration(param));
    }
    methodParams = node.getParameters();
    for (VariableElement param : captureInfo.getImplicitPostfixParams(type)) {
      methodParams.add(new SingleVariableDeclaration(param));
    }
  }

  /**
   * Create an equivalent function declaration for a given method.
   */
  private FunctionDeclaration makeFunction(MethodDeclaration method) {
    ExecutableElement elem = method.getExecutableElement();
    TypeElement declaringClass = ElementUtil.getDeclaringClass(elem);
    boolean isInstanceMethod = !ElementUtil.isStatic(elem) && !ElementUtil.isConstructor(elem);

    FunctionDeclaration function =
        new FunctionDeclaration(nameTable.getFullFunctionName(elem), elem.getReturnType());
    function.setJniSignature(signatureGenerator.createJniFunctionSignature(elem));
    function.setLineNumber(method.getLineNumber());

    if (!ElementUtil.isStatic(elem)) {
      VariableElement var = GeneratedVariableElement.newParameter(
          NameTable.SELF_NAME, declaringClass.asType(), null);
      function.addParameter(new SingleVariableDeclaration(var));
    }
    TreeUtil.copyList(method.getParameters(), function.getParameters());

    function.setModifiers(method.getModifiers() & Modifier.STATIC);
    if (ElementUtil.isPrivate(elem) || (isInstanceMethod && !ElementUtil.isDefault(elem))) {
      function.addModifiers(Modifier.PRIVATE);
    } else {
      function.addModifiers(Modifier.PUBLIC);
    }

    if (Modifier.isNative(method.getModifiers())) {
      function.addModifiers(Modifier.NATIVE);
      return function;
    }

    function.setBody(TreeUtil.remove(method.getBody()));

    if (ElementUtil.isStatic(elem) || ElementUtil.isDefault(elem)) {
      // Add class initialization invocation, since this may be the first use of this class.
      String initName = UnicodeUtils.format("%s_initialize", nameTable.getFullName(declaringClass));
      TypeMirror voidType = typeUtil.getVoid();
      FunctionElement initElement = new FunctionElement(initName, voidType, declaringClass);
      FunctionInvocation initCall = new FunctionInvocation(initElement, voidType);
      function.getBody().addStatement(0, new ExpressionStatement(initCall));
    } else {
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
    ExecutableElement element = method.getExecutableElement();
    TypeElement declaringClass = ElementUtil.getDeclaringClass(element);

    String name = releasing ? nameTable.getReleasingConstructorName(element)
        : nameTable.getAllocatingConstructorName(element);
    FunctionDeclaration function = new FunctionDeclaration(name, declaringClass.asType());
    function.setLineNumber(method.getLineNumber());
    function.setModifiers(ElementUtil.isPrivate(element) ? Modifier.PRIVATE : Modifier.PUBLIC);
    function.setReturnsRetained(!releasing);
    TreeUtil.copyList(method.getParameters(), function.getParameters());
    Block body = new Block();
    function.setBody(body);

    StringBuilder sb = new StringBuilder(releasing ? "J2OBJC_CREATE_IMPL(" : "J2OBJC_NEW_IMPL(");
    sb.append(nameTable.getFullName(declaringClass));
    sb.append(", ").append(nameTable.getFunctionName(element));
    for (SingleVariableDeclaration param : function.getParameters()) {
      sb.append(", ").append(nameTable.getVariableQualifiedName(param.getVariableElement()));
    }
    sb.append(")");
    body.addStatement(new NativeStatement(sb.toString()));

    return function;
  }

  /**
   *  Replace method block statements with single statement that invokes function.
   */
  private void setFunctionCaller(MethodDeclaration method, ExecutableElement methodElement) {
    TypeMirror returnType = methodElement.getReturnType();
    TypeElement declaringClass = ElementUtil.getDeclaringClass(methodElement);
    Block body = new Block();
    method.setBody(body);
    method.removeModifiers(Modifier.NATIVE);
    List<Statement> stmts = body.getStatements();
    FunctionInvocation invocation =
        new FunctionInvocation(newFunctionElement(methodElement), returnType);
    List<Expression> args = invocation.getArguments();
    if (!ElementUtil.isStatic(methodElement)) {
      args.add(new ThisExpression(declaringClass.asType()));
    }
    for (SingleVariableDeclaration param : method.getParameters()) {
      args.add(new SimpleName(param.getVariableElement()));
    }
    if (TypeUtil.isVoid(returnType)) {
      stmts.add(new ExpressionStatement(invocation));
      if (ElementUtil.isConstructor(methodElement)) {
        stmts.add(new ReturnStatement(new ThisExpression(declaringClass.asType())));
      }
    } else {
      stmts.add(new ReturnStatement(invocation));
    }
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    if (!node.isInterface() && options.disallowInheritedConstructors()) {
      addDisallowedConstructors(node);
    }
  }

  /**
   * Declare any inherited constructors that aren't allowed to be accessed in Java
   * with a NS_UNAVAILABLE macro, so that clang will flag such access from native
   * code as an error.
   */
  private void addDisallowedConstructors(TypeDeclaration node) {
    TypeElement typeElement = node.getTypeElement();
    TypeElement superClass = ElementUtil.getSuperclass(typeElement);
    if (ElementUtil.isPrivateInnerType(typeElement) || ElementUtil.isAbstract(typeElement)
        || superClass == null
        // If we're not emitting constructors we don't need to disallow anything unless our
        // superclass is NSObject.
        || (!options.emitWrapperMethods()
            && typeUtil.getObjcClass(superClass) != TypeUtil.NS_OBJECT)) {
      return;
    }
    Map<String, ExecutableElement> inheritedConstructors = new HashMap<>();
    // Add super constructors that have unique parameter lists.
    for (ExecutableElement superC : ElementUtil.getConstructors(superClass)) {
      if (ElementUtil.isPrivate(superC)) {
        // Skip private super constructors since they're already unavailable.
        continue;
      }
      String selector = nameTable.getMethodSelector(superC);
      inheritedConstructors.put(selector, superC);
    }
    // Don't disallow this class' constructors if we're emitting wrapper methods.
    if (options.emitWrapperMethods()) {
      for (ExecutableElement constructor : ElementUtil.getConstructors(typeElement)) {
        inheritedConstructors.remove(nameTable.getMethodSelector(constructor));
      }
    }
    for (Map.Entry<String, ExecutableElement> entry : inheritedConstructors.entrySet()) {
      ExecutableElement oldConstructor = entry.getValue();
      GeneratedExecutableElement newConstructor =
          GeneratedExecutableElement.newConstructorWithSelector(
              entry.getKey(), typeElement, typeUtil);
      MethodDeclaration decl = new MethodDeclaration(newConstructor).setUnavailable(true);
      decl.addModifiers(Modifier.ABSTRACT);
      int count = 0;
      for (VariableElement param : oldConstructor.getParameters()) {
        VariableElement newParam = GeneratedVariableElement.newParameter(
            "arg" + count++, param.asType(), newConstructor);
        newConstructor.addParameter(newParam);
        decl.addParameter(new SingleVariableDeclaration(newParam));
      }
      addImplicitParameters(decl, ElementUtil.getDeclaringClass(oldConstructor));
      node.addBodyDeclaration(decl);
    }
  }

  /**
   * Convert references to "this" in the function to a "self" parameter.
   */
  private static class FunctionConverter extends TreeVisitor {

    private final VariableElement selfParam;

    static void convert(FunctionDeclaration function) {
      function.accept(new FunctionConverter(function.getParameter(0).getVariableElement()));
    }

    private FunctionConverter(VariableElement selfParam) {
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
      if (var != null && var.getKind().isField()) {
        // Convert name to self->name.
        node.replaceWith(new QualifiedName(var, node.getTypeMirror(), new SimpleName(selfParam)));
      }
    }

    @Override
    public boolean visit(SuperFieldAccess node) {
      // Change super.field expression to self.field.
      SimpleName qualifier = new SimpleName(selfParam);
      node.replaceWith(new FieldAccess(node.getVariableElement(), node.getTypeMirror(), qualifier));
      return false;
    }

    @Override
    public void endVisit(ThisExpression node) {
      SimpleName self = new SimpleName(selfParam);
      node.replaceWith(self);
    }

    @Override
    public void endVisit(SuperMethodInvocation node) {
      // Super invocations won't work from a function. Setting the receiver
      // will cause SuperMethodInvocationRewriter to rewrite this invocation.
      if (node.getReceiver() == null) {
        node.setReceiver(new SimpleName(selfParam));
      }
    }
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
}
