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

import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.FunctionInvocation;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.NativeStatement;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.ThisExpression;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.VariableDeclaration;
import com.google.devtools.j2objc.types.FunctionBinding;
import com.google.devtools.j2objc.types.GeneratedVariableBinding;
import com.google.devtools.j2objc.types.LambdaTypeBinding;
import com.google.devtools.j2objc.types.NativeTypeBinding;
import com.google.devtools.j2objc.util.BindingUtil;
import com.google.devtools.j2objc.util.ElementUtil;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.VariableElement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Rewrites Lambda nodes into Lambda_get calls.
 * Creates the Lambda implementation function, the capture struct, the Lambda_get function,
 * a dealloc function if the lambda is capturing, and replaces the lambda expression with a call
 * to the Lambda_get function.
 *
 * @author Nathan Braswell
 */
public class LambdaRewriter extends TreeVisitor {

  private static final NativeTypeBinding SELType = new NativeTypeBinding("SEL");
  private static final NativeTypeBinding LambdaBase = new NativeTypeBinding("LambdaBase *");
  private final OuterReferenceResolver outerResolver;

  public LambdaRewriter(OuterReferenceResolver outerResolver) {
    this.outerResolver = outerResolver;
  }

  public static boolean isDefault(int modifiers) {
    return ((modifiers & org.eclipse.jdt.core.dom.Modifier.DEFAULT) != 0);
  }

  @Override
  public void endVisit(LambdaExpression node) {
      LambdaGenerator gen = new LambdaGenerator(node);
      gen.setupMethodStrings();
      gen.createFunctionGet();
      gen.createFunctionImpl();
      gen.createFunctionGetInvocation();
      if (node.isCapturing()) {
        gen.createFunctionDealloc();
        gen.addInternalCapturing();
      } else {
        gen.addInternalNonCapturing();
      }
      node.replaceWith(gen.getFunctionGetInvocation());
  }

  class LambdaGenerator {
    LambdaExpression node = null;
    TypeDeclaration enclosingType = null;
    ITypeBinding enclosingTypeBinding;
    String lambdaName;
    String lambdaGetName;
    String lambdaImplName;
    String lambdaDeallocName;
    String functionalTypeString;
    ITypeBinding lambdaType;
    IMethodBinding functionalInterface;
    int numProtocols = 0;
    String protocols = "(Protocol *[]){ ";
    int numMethods = 0;
    String valuesSetup = "";
    String selectors = "(SEL []){ ";
    String imps = "(IMP []){ ";
    String signatures = "(const char *[]){ ";

    FunctionDeclaration funcImpl = null;
    FunctionDeclaration funcGet = null;
    FunctionInvocation funcGetInvocation = null;
    FunctionDeclaration funcDealloc = null;

    public LambdaGenerator(LambdaExpression node) {
      this.node = node;
      enclosingType =
          TreeUtil.getNearestAncestorWithType(TypeDeclaration.class, node);
      enclosingTypeBinding = enclosingType.getTypeBinding();
      lambdaName = node.getUniqueName();
      lambdaGetName = lambdaName + "_get";
      lambdaImplName = lambdaName + "_impl";
      lambdaDeallocName = lambdaName + "_dealloc";
      lambdaType = node.getTypeBinding();
      functionalInterface = lambdaType.getFunctionalInterfaceMethod();
    }

    public void setupMethodStrings() {
      // Here we build up array literals for protocols, selectors, IMPs, and signatures,
      // one for each method on our lambda object (the functional method and all
      // the default methods). These will be passed to the correct Create* function that will
      // make a class/object with these methods.
      Set<ITypeBinding> allTypes = BindingUtil.getOrderedInheritedTypesInclusive(lambdaType);
      if (BindingUtil.isIntersectionType(lambdaType)) {
        allTypes.remove(lambdaType);
      }
      for (ITypeBinding i : allTypes) {
        if (numProtocols > 0) {
          protocols += ", ";
        }
        numProtocols++;
        String classNameWithId = nameTable.getObjCType(i);
        String className = classNameWithId.substring(3, classNameWithId.length() - 1);
        String protocol = "@protocol(" + className + ")";
        protocols += protocol;
        for (IMethodBinding m : i.getDeclaredMethods()) {
          boolean willGrab = isDefault(m.getModifiers()) || Modifier.isStatic(m.getModifiers());
          boolean isFunctional = functionalInterface.isSubsignature(m);
          boolean willGenerate = willGrab || isFunctional;
          if (willGenerate) {
            if (numMethods > 0) {
              selectors += ", ";
              imps += ", ";
              signatures += ", ";
            }
            numMethods++;
          }
          if (willGrab) {
            String methodSelector = "@selector(" + nameTable.getMethodSelector(m) + ")";
            selectors += methodSelector;
            valuesSetup += "Method method" + numMethods
                + " = class_getInstanceMethod([" + className + " class], "
                + methodSelector + ");\n";
            imps += "method_getImplementation(method" + numMethods + ")";
            signatures += "method_getTypeEncoding(method" + numMethods + ")";
          } else if (isFunctional) {
            String selector = "@selector(" + nameTable.getMethodSelector(functionalInterface) + ")";
            selectors += selector;
            imps += "(IMP)&" + lambdaImplName;
            valuesSetup += "struct objc_method_description desc" + numMethods
                + " = protocol_getMethodDescription(" + protocol + ", " + selector
                + ", YES, YES);\n";
            signatures += "desc" + numMethods + ".types";
          }
        }
      }

      // If this is a capturing lambda, we add a dealloc method that will handle releasing
      // all the objects that we capture.
      if (node.isCapturing()) {
        selectors += ", @selector(dealloc)}";
        imps += ", (IMP)&" + lambdaDeallocName + "}";
        signatures += ", \"v@:\"}";
        numMethods++;
      } else {
        selectors += "}";
        imps += "}";
        signatures += "}";
      }
      protocols += "}";
      functionalTypeString = nameTable.getObjCType(lambdaType);
    }

    private void createFunctionGet() {
      funcGet = new FunctionDeclaration(lambdaGetName, lambdaType, enclosingTypeBinding);
      funcGet.addModifiers(Modifier.PRIVATE);
      funcGet.setBody(new Block());
      funcGet.getBody().getStatements().add(new NativeStatement("static dispatch_once_t token;"));
      enclosingType.getBodyDeclarations().add(0, funcGet);
    }

    private void createFunctionGetInvocation() {
      funcGetInvocation = new FunctionInvocation(new FunctionBinding(
          lambdaGetName, lambdaType, enclosingTypeBinding), lambdaType);
    }

    private void createFunctionImpl() {
      ITypeBinding returnType = functionalInterface.getReturnType();
      funcImpl =
          new FunctionDeclaration(lambdaImplName, returnType, enclosingTypeBinding);
      funcImpl.addModifiers(Modifier.PRIVATE);
      funcImpl
          .getParameters()
          .add(
              new SingleVariableDeclaration(
                  new GeneratedVariableBinding("self", 0, LambdaBase, false, true, null, null)));
      funcImpl
          .getParameters()
          .add(
              new SingleVariableDeclaration(
                  new GeneratedVariableBinding("_cmd", 0, SELType, false, true, null, null)));

      for (VariableDeclaration d : node.getParameters()) {
        funcImpl.getParameters().add(new SingleVariableDeclaration(d.getVariableBinding()));
      }

      funcImpl.setBody((Block) TreeUtil.remove(node.getBody()));
      enclosingType.getBodyDeclarations().add(0, funcImpl);
    }

    private void createFunctionDealloc() {
      funcDealloc = new FunctionDeclaration(lambdaDeallocName,
          typeEnv.resolveJavaType("void"), enclosingTypeBinding);
      funcDealloc.addModifiers(Modifier.PRIVATE);
      funcDealloc.setBody(new Block());
      funcDealloc
          .getParameters()
          .add(
              new SingleVariableDeclaration(
                  new GeneratedVariableBinding("self", 0, LambdaBase, false, true, null, null)));
      funcDealloc
          .getParameters()
          .add(
              new SingleVariableDeclaration(
                  new GeneratedVariableBinding("_cmd", 0, SELType, false, true, null, null)));

      enclosingType.getBodyDeclarations().add(0, funcDealloc);
    }

    private void addInternalNonCapturing() {
      List<Statement> statements = funcGet.getBody().getStatements();
      // Use dispatch_once to create a cached instance of the non-capturing lambda.
      statements.add(0, new NativeStatement("static " + functionalTypeString + " instance;"));
      statements.add(
          new NativeStatement(
              "dispatch_once(&token, ^{\n" + valuesSetup + "\n"
                  + "instance = CreateNonCapturing(\n"
                  + "\"" + lambdaName + "\", " + numProtocols + ", " + protocols + ",\n"
                  + numMethods + "," + selectors + ", " + imps + ", " + signatures + ");\n"
              + "});"));
      statements.add(new NativeStatement("return instance;"));
    }

    private void addInternalCapturing() {
      List<Statement> funcImplStatements = funcImpl.getBody().getStatements();
      List<Statement> statements = funcGet.getBody().getStatements();
      List<Statement> funcDeallocStatements = funcDealloc.getBody().getStatements();

      // Use dispatch_once to create a cached class for the capturing lambda.
      statements.add(new NativeStatement("static Class cls;"));
      statements.add(
          new NativeStatement(
              "dispatch_once(&token, ^{\n"
                  + valuesSetup + "\n"
                  + "cls = CreatePossiblyCapturingClass(\n"
                  + "\"" + lambdaName + "\", " + numProtocols + ", " + protocols + ",\n"
                  + numMethods + ",\n" + selectors + ",\n" + imps + ",\n" + signatures + ");\n"
              + "});"));

      // Allocate the lambda object with extra space for the capture struct, and grab this
      // internal capture struct for the _impl, _get, and dealloc functions.
      String funcWithoutID = functionalTypeString.substring(3, functionalTypeString.length() - 1);
      String lambdaCaptureStructName = lambdaName + "_captures";
      statements.add(
          new NativeStatement(
              "LambdaBase<" + funcWithoutID + "> *result = NSAllocateObject(cls, sizeof("
              + lambdaCaptureStructName + "), nil);"));
      statements.add(
          new NativeStatement(
              lambdaCaptureStructName + " *captures = (" + lambdaCaptureStructName
              + " *) &result->captures_;"));
      funcImplStatements.add(
          0,
          new NativeStatement(
              lambdaCaptureStructName + " *captures = (" + lambdaCaptureStructName
              + " *) &self_->captures_;"));
      funcDeallocStatements.add(
          0,
          new NativeStatement(
              lambdaCaptureStructName + " *captures = (" + lambdaCaptureStructName
              + " *) &self_->captures_;"));

      // Add an outerField to the capture struct (and init of the capture struct) if we have one.
      String structContents = "";
      LambdaTypeBinding uniqueLambdaType = node.getLambdaTypeBinding();
      IVariableBinding outerField = outerResolver.getOuterField(uniqueLambdaType);
      if (outerField != null) {
        structContents +=
            nameTable.getObjCType(outerField.getType()) + " " + outerField.getName() + ";\n";
        statements.add(
            new NativeStatement(
                "captures->" + outerField.getName()
                + " = [" + outerField.getName() + "_ retain];"));
        funcDeallocStatements.add(
            new NativeStatement(
                "[captures->" + outerField.getName() + " release];"));

        // Note that the init of the captures struct is 0 in funcImplStatements.
        funcImplStatements.add(
            1,
            new NativeStatement(
                nameTable.getObjCType(outerField.getType())
                + " " + outerField.getName() + "_ = captures->" + outerField.getName() + ";"));
        // TODO(user): This self local should be unnecessary, but SuperMethodInvocationRewriter
        // adds a ThisExpression node after the OuterReferenceResolver has already run, so it
        // doesn't get the right path using the lambda and instead just becomes self.
        funcImplStatements.add(
            2,
            new NativeStatement(
                nameTable.getObjCType(outerField.getType())
                + " self = captures->" + outerField.getName() + ";"));

        funcGet.getParameters().add(new SingleVariableDeclaration(outerField));

        List<VariableElement> pathToOuter = outerResolver.getPath(node);
        if (pathToOuter != null) {
          funcGetInvocation.getArguments().add(Name.newName(pathToOuter));
        } else {
          funcGetInvocation.getArguments().add(new ThisExpression(outerField.getType()));
        }
      }

      // Add the captured fields to the capture struct and its packing/unpacking.
      List<List<VariableElement>> captureArgPaths = outerResolver.getCaptureArgPaths(node);

      for (int i = 0; i < captureArgPaths.size(); i++) {
        List<VariableElement> varPath = captureArgPaths.get(i);
        VariableElement var = varPath.get(varPath.size() - 1);
        String varName = getName(varPath);
        if (ElementUtil.isField(var)) {
          varName += "_";
        }

        // For each captured field, we add it to the capture struct,
        // to the get function's parameters, to the get function's invocation,
        // the capture = var to the get function, and the var = capture to the
        // function implementation. If it's not a primitve, we use JreStrongAssign for
        // capture = var and add a RELEASE_ call to the _dealloc function.
        structContents += nameTable.getObjCType(var.asType()) + " " + varName + ";\n";
        if (!var.asType().getKind().isPrimitive() && !ElementUtil.isWeakReference(var)) {
          statements.add(
              new NativeStatement(
                  "JreStrongAssign(&captures->" + varName + ", " + varName + ");"));
          funcDeallocStatements.add(
              1,
              new NativeStatement(
                  "RELEASE_(captures->" + varName + ");"));
        } else {
          statements.add(
              new NativeStatement(
                  "captures->" + varName + " = " + varName + ";"));
        }
        funcImplStatements.add(
            1,
            new NativeStatement(
                nameTable.getObjCType(var.asType()) + " " + varName
                + " = captures->" + varName + ";"));
        funcGet.getParameters().add(new SingleVariableDeclaration(var));
        funcGetInvocation.getArguments().add(Name.newName(varPath));
      }

      statements.add(new NativeStatement("return [result autorelease];"));

      // Make the closure struct.
      NativeDeclaration lambdaCaptureStruct =
          NativeDeclaration.newOuterDeclaration("",
              "typedef struct " + lambdaCaptureStructName + " {\n"
                  + structContents
              + "} " + lambdaCaptureStructName + ";\n");
      enclosingType.getBodyDeclarations().add(0, lambdaCaptureStruct);
    }

    FunctionInvocation getFunctionGetInvocation() {
      return funcGetInvocation;
    }

    private String getName(List<VariableElement> varPath) {
      Name n = Name.newName(varPath);
      if (n instanceof SimpleName) {
        return ((SimpleName) n).getIdentifier();
      }
      return ((QualifiedName) n).getName().getIdentifier();
    }
  }
}
