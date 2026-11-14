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
import com.google.devtools.j2objc.ast.ClassInstanceCreation;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.RecordDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.ast.UnitTreeVisitor;
import com.google.devtools.j2objc.translate.KmpMethodHierarchy.TypeMethods;
import com.google.devtools.j2objc.translate.KmpTypeConverter.MappingStrategy;
import com.google.devtools.j2objc.types.ExecutablePair;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.j2objc.annotations.ObjectiveCKmpMethod;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

/**
 * Implements the @ObjectiveCKmpMethod translator.
 *
 * <p>This translator detects methods annotated with {@link ObjectiveCKmpMethod} and generates an
 * "adapter" method in Objective-C. This adapter method uses native Objective-C types (like NSArray,
 * NSDictionary, NSSet) instead of their Java counterparts (List, Map, Set), which is useful for
 * better interoperability in Kotlin Multiplatform (KMP) environments.
 *
 * <p>The generated adapter method calls the original Java method after converting the native
 * Objective-C types back to Java types using a provided adapter class.
 *
 * <h2>How the Adapter Works</h2>
 *
 * <p>The adapter class (specified in the annotation) should contain methods that convert between
 * Java collection types and native Objective-C collection types. The translator looks for methods
 * in the adapter class based on name prefixes and type matching.
 *
 * <h3>Method Selection Rules</h3>
 *
 * <p>The translator selects adapter methods based on the direction of conversion:
 *
 * <ul>
 *   <li><b>Native to Java (Method Parameters)</b>: The translator looks for methods whose name
 *       starts with "to" and whose <b>return type</b> matches the target Java type. It assumes the
 *       single parameter of this method is the native Objective-C type.
 *   <li><b>Java to Native (Method Return Value)</b>: The translator looks for methods whose name
 *       starts with "from" and whose <b>parameter type</b> matches the source Java type. It assumes
 *       the return type of this method is the native Objective-C type.
 * </ul>
 *
 * <h3>Type Matching (Selection)</h3>
 *
 * <p>The translator finds all applicable methods in the adapter class that start with the required
 * prefix ("to" or "from"). It then scores how well their signatures match the target type.
 *
 * <p>The translator prioritizes matching candidate methods by:
 *
 * <ol>
 *   <li><b>Exact Match</b> over <b>Wildcard Match</b>.
 *   <li>Deeper type arguments (higher nesting level).
 *   <li>Lower score for matching type arguments (fewer wildcards used).
 * </ol>
 *
 * <p>Wildcard matches are disallowed for mapped types (like {@code List}, {@code Map}, {@code Set}
 * , {@code Boolean}) to ensure strict typing at the conversion boundaries.
 */
public final class ObjectiveCKmpMethodTranslator extends UnitTreeVisitor {

  private final KmpMethodHierarchy methodHierarchy;
  private final KmpAdapterEmitter adapterEmitter;

  public ObjectiveCKmpMethodTranslator(CompilationUnit unit) {
    super(unit);
    KmpTypeConverter typeConverter = new KmpTypeConverter(typeUtil, nameTable, options);
    this.methodHierarchy = new KmpMethodHierarchy(typeUtil, elementUtil);
    this.adapterEmitter = new KmpAdapterEmitter(typeUtil, nameTable, options, typeConverter);
  }

  // Re-run lambda lowering passes because ObjectiveCKmpMethodTranslator synthesizes new
  // LambdaExpression AST arguments for composable collection converters after LambdaRewriter
  // originally executed in the translator pipeline.
  @Override
  public void run() {
    Set<AbstractTypeDeclaration> existingTypes = new HashSet<>(unit.getTypes());
    // Rewrite call sites before emitting adapter methods/dispatch helpers so generated invocations
    // inside synthetic adapter and _dispatch bodies are never rewritten into self-recursion.
    new KmpMethodInvocationRewriter(unit, methodHierarchy, adapterEmitter).run();
    super.run();
    new LambdaTypeElementAdder(unit, "$KmpLambda$").run();
    new LambdaRewriter(unit).run();
    new InnerClassExtractor(unit).run();

    Functionizer functionizer = new Functionizer(unit);
    var unused = functionizer.visit(unit);
    InitializationNormalizer initNormalizer = new InitializationNormalizer(unit);
    OperatorRewriter operatorRewriter = new OperatorRewriter(unit);

    // Pass 1: Run InitializationNormalizer, Functionizer, and OperatorRewriter exclusively over
    // newly introduced lambda type declarations created during this translation pass.
    for (AbstractTypeDeclaration type : unit.getTypes()) {
      if (!existingTypes.contains(type)) {
        type.accept(initNormalizer);
        type.accept(functionizer);
        for (Statement stmt : type.getClassInitStatements()) {
          stmt.accept(operatorRewriter);
        }
      }
    }

    // Pass 2: Run Functionizer specifically on ClassInstanceCreation nodes across the unit.
    // This updates newly generated lambda instantiations (e.g., inside generated adapter methods).
    unit.accept(
        new TreeVisitor() {
          @Override
          public void endVisit(ClassInstanceCreation node) {
            functionizer.endVisit(node);
          }
        });

    new StaticVarRewriter(unit).run();
    new PrivateDeclarationResolver(unit).run();
  }

  private void processType(AbstractTypeDeclaration typeNode) {
    TypeElement typeElem = typeNode.getTypeElement();
    TypeMethods resolved = methodHierarchy.getTypeMethods(typeElem);
    boolean isInterface = typeElem.getKind().isInterface();

    for (Map.Entry<String, AnnotationMirror> entry : resolved.annotatedSignatures().entrySet()) {
      String signature = entry.getKey();
      AnnotationMirror annotation = entry.getValue();
      ExecutablePair method = resolved.allMethods().get(signature);

      boolean shouldGenerate;
      MappingStrategy strategy = MappingStrategy.NATIVE_TO_JAVA;

      if (isInterface) {
        strategy = MappingStrategy.JAVA_TO_NATIVE;
        MethodDeclaration unadaptedDecl =
            KmpAdapterEmitter.findMethodDeclaration(typeNode, method.element());
        if (unadaptedDecl != null) {
          unadaptedDecl.setOptional(true);
        }
        // Only generate the adapted protocol method and _dispatch helper when the method is
        // declared and directly annotated on this interface (skipping inherited methods and
        // unannotated subinterface overrides).
        boolean isDeclaredOnCurrentInterface =
            ElementUtil.getDeclaringClass(method.element()).equals(typeElem);
        boolean hasDirectAnnotation =
            ElementUtil.getAnnotation(method.element(), ObjectiveCKmpMethod.class) != null;
        shouldGenerate = isDeclaredOnCurrentInterface && hasDirectAnnotation;
      } else {
        TypeMethods superHierarchy = methodHierarchy.getSupertypeHierarchy(typeElem, signature);
        if (ElementUtil.isAbstract(method.element())) {
          strategy = MappingStrategy.JAVA_TO_NATIVE;
          // Generate if declared on this class, or if inherited by an abstract class from an
          // interface rather than an abstract superclass that already generated them.
          shouldGenerate =
              ElementUtil.getDeclaringClass(method.element()).equals(typeElem)
                  || (ElementUtil.isAbstract(typeElem)
                      && (superHierarchy == null
                          || superHierarchy.typeElement().getKind().isInterface()));
        } else {
          shouldGenerate =
              superHierarchy == null
                  || ElementUtil.isAbstract(superHierarchy.allMethods().get(signature).element())
                  || ElementUtil.isDefault(superHierarchy.allMethods().get(signature).element());
        }
      }

      if (shouldGenerate) {
        adapterEmitter.emit(typeNode, method, annotation, isInterface, strategy);
      }
    }
  }

  @Override
  public void endVisit(EnumDeclaration node) {
    processType(node);
  }

  @Override
  public void endVisit(RecordDeclaration node) {
    processType(node);
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    TypeElement typeElem = node.getTypeElement();
    if (typeElem != null && typeElem.getSimpleName().contentEquals("package-info")) {
      return;
    }
    processType(node);
  }
}
