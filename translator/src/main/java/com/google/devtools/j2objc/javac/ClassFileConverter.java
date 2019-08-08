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

package com.google.devtools.j2objc.javac;

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.AbstractTypeDeclaration;
import com.google.devtools.j2objc.ast.Annotation;
import com.google.devtools.j2objc.ast.AnnotationTypeDeclaration;
import com.google.devtools.j2objc.ast.AnnotationTypeMemberDeclaration;
import com.google.devtools.j2objc.ast.Block;
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.EnumConstantDeclaration;
import com.google.devtools.j2objc.ast.EnumDeclaration;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MarkerAnnotation;
import com.google.devtools.j2objc.ast.MemberValuePair;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.NormalAnnotation;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.PropertyAnnotation;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleMemberAnnotation;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.Statement;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ClassFile;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.TranslationEnvironment;
import com.google.j2objc.annotations.Property;
import com.strobel.decompiler.languages.java.ast.EntityDeclaration;
import com.strobel.decompiler.languages.java.ast.ParameterDeclaration;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.StandardLocation;

/**
 * Converts a JVM classfile into a CompilationUnit. The resulting unit
 * is different from one created from the same source, because several
 * modifications made on source files during translation are already
 * present in a classfile. These include moving initializers into
 * constructors and the class initializer method, extracting inner
 * classes, and adding enum support fields and methods.
 *
 * @author Manvith Narahari
 * @author Tom Ball
 */
public class ClassFileConverter {
  private final JavacEnvironment parserEnv;
  private final TranslationEnvironment translationEnv;
  private final InputFile file;
  private final ClassFile classFile;
  private final String typeName;

  public static CompilationUnit convertClassFile(
      Options options, JavacEnvironment env, InputFile file) {
    try {
      ClassFileConverter converter = new ClassFileConverter(
          env, new TranslationEnvironment(options, env), file);
      converter.setClassPath();
      return converter.createUnit();
    } catch (IOException e) {
      ErrorUtil.error(e.getMessage());
      return null;
    }
  }

  private ClassFileConverter(JavacEnvironment parserEnv, TranslationEnvironment translationEnv,
      InputFile file) throws IOException {
    this.parserEnv = parserEnv;
    this.translationEnv = translationEnv;
    this.file = file;
    this.classFile = ClassFile.create(file);
    this.typeName = classFile.getFullName();
  }

  /**
   * Set classpath to the root path of the input file, to support typeElement lookup.
   */
  private void setClassPath() throws IOException {
    String fullPath = file.getAbsolutePath();
    String rootPath = fullPath.substring(0, fullPath.lastIndexOf(classFile.getRelativePath()));
    List<File> classPath = new ArrayList<>();
    classPath.add(new File(rootPath));
    parserEnv.fileManager().setLocation(StandardLocation.CLASS_PATH, classPath);
  }

  private CompilationUnit createUnit() {
    TypeElement typeElement = parserEnv.elementUtilities().getTypeElement(typeName);
    if (typeElement == null) {
      ErrorUtil.error("Invalid class file: " + file.getOriginalLocation());
    }
    PackageElement pkgElement = parserEnv.elementUtilities().getPackageOf(typeElement);
    if (pkgElement == null) {
      pkgElement = parserEnv.defaultPackage();
    }
    String mainTypeName = typeElement.getSimpleName().toString();
    CompilationUnit compUnit = new CompilationUnit(translationEnv, mainTypeName);
    compUnit.setPackage((PackageDeclaration) convert(pkgElement, compUnit));
    AbstractTypeDeclaration typeDecl = (AbstractTypeDeclaration) convert(typeElement, compUnit);
    convertClassInitializer(typeDecl);
    compUnit.addType(typeDecl);
    return compUnit;
  }

  /**
   * The clinit method isn't converted into a member element by javac,
   * so extract it separately from the classfile.
   */
  private void convertClassInitializer(AbstractTypeDeclaration typeDecl) {
    EntityDeclaration decl = classFile.getMethod("<clinit>", "()V");
    if (decl == null) {
      return;  // Class doesn't have a static initializer.
    }
    MethodTranslator translator = new MethodTranslator(
        parserEnv, translationEnv, null, typeDecl, null);
    Block block = (Block) decl.acceptVisitor(translator, null);
    for (Statement stmt : block.getStatements()) {
      typeDecl.addClassInitStatement(stmt.copy());
    }

 }

  @SuppressWarnings("fallthrough")
  private TreeNode convert(Element element, TreeNode parent) {
    TreeNode node;
    switch (element.getKind()) {
      case ANNOTATION_TYPE:
        node = convertAnnotationTypeDeclaration((TypeElement) element);
        break;
      case CLASS:
      case INTERFACE:
        node = convertTypeDeclaration((TypeElement) element);
        break;
      case CONSTRUCTOR:
      case METHOD:
        node = convertMethodDeclaration(
            (ExecutableElement) element, (AbstractTypeDeclaration) parent);
        break;
      case ENUM:
        node = convertEnumDeclaration((TypeElement) element);
        break;
      case ENUM_CONSTANT:
        node = convertEnumConstantDeclaration((VariableElement) element);
        break;
      case FIELD:
        node = convertFieldDeclaration((VariableElement) element);
        break;
      case PACKAGE:
        node = convertPackage((PackageElement) element);
        break;
      case PARAMETER:
        node = convertParameter((VariableElement) element);
        break;
      case STATIC_INIT:
        // Converted separately in convertClassInitializer().
        node = null;
        break;
      default:
        throw new AssertionError("Unsupported element kind: " + element.getKind());
    }
    return node;
  }

  static Name convertName(Element element) {
    Element parent = element.getEnclosingElement();
    if (parent == null || parent.getSimpleName().toString().isEmpty()) {
      return new SimpleName(element);
    }
    return new QualifiedName(element, element.asType(), convertName(parent));
  }

  private TreeNode convertPackage(PackageElement element) {
    return new PackageDeclaration()
        .setPackageElement(element)
        .setName(translationEnv.elementUtil().getPackageName(element));
  }

  private BodyDeclaration convertBodyDeclaration(BodyDeclaration newNode, Element element) {
    return newNode.setAnnotations(convertAnnotations(element));
  }

  private void removeInterfaceModifiers(AbstractTypeDeclaration typeDecl) {
    typeDecl.removeModifiers(Modifier.PUBLIC | Modifier.ABSTRACT);
    for (BodyDeclaration bodyDecl : typeDecl.getBodyDeclarations()) {
      bodyDecl.removeModifiers(Modifier.PUBLIC | Modifier.ABSTRACT);
    }
  }

  private AnnotationTypeMemberDeclaration convertAnnotationTypeMemberDeclaration(
      ExecutableElement element) {
    AnnotationValue annotValue = element.getDefaultValue();
    Expression expr = annotValue != null
        ? convertAnnotationValue(element.getReturnType(), annotValue) : null;
    AnnotationTypeMemberDeclaration memberDeclaration =
        new AnnotationTypeMemberDeclaration(element).setDefault(expr);
    convertBodyDeclaration(memberDeclaration, element);
    return memberDeclaration;
  }

  private TreeNode convertAnnotationTypeDeclaration(TypeElement element) {
    AnnotationTypeDeclaration annotTypeDecl = new AnnotationTypeDeclaration(element);
    convertBodyDeclaration(annotTypeDecl, element);
    for (Element elem : element.getEnclosedElements()) {
      BodyDeclaration bodyDecl = elem.getKind() == ElementKind.METHOD
          ? convertAnnotationTypeMemberDeclaration((ExecutableElement) elem)
          : (BodyDeclaration) convert(elem, annotTypeDecl);
      if (bodyDecl != null) {
        annotTypeDecl.addBodyDeclaration(bodyDecl);
      }
    }
    removeInterfaceModifiers(annotTypeDecl);
    return annotTypeDecl;
  }

  private Expression convertAnnotationValue(TypeMirror type, AnnotationValue annot) {
    /* TODO(manvithn): see if the method implements the AnnotationMirror branch correctly */
    return translationEnv.translationUtil().createAnnotationValue(type, annot);
  }

  private Annotation convertAnnotation(AnnotationMirror mirror) {
    Map<? extends ExecutableElement, ? extends AnnotationValue> args = mirror.getElementValues();
    String annotationName = mirror.getAnnotationType().toString();
    Annotation annotation;
    if (annotationName.equals(Property.class.getSimpleName())
        || annotationName.equals(Property.class.getName())) {
      PropertyAnnotation propAnnot = new PropertyAnnotation();
      for (String attr : ElementUtil.parsePropertyAttribute(mirror)) {
        propAnnot.addAttribute(attr);
      }
      annotation = propAnnot;
    } else if (args.isEmpty()) {
      annotation = new MarkerAnnotation();
    } else if (args.size() == 1) {
      Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry =
          args.entrySet().iterator().next();
      annotation = new SingleMemberAnnotation()
          .setValue(convertAnnotationValue(entry.getKey().getReturnType(), entry.getValue()));
    } else {
      NormalAnnotation normalAnnot = new NormalAnnotation();
      args.forEach((exec, annot) -> {
          MemberValuePair memberPair = new MemberValuePair()
              .setName(new SimpleName(exec))
              .setValue(convertAnnotationValue(exec.getReturnType(), annot));
          normalAnnot.addValue(memberPair);
      });
      annotation = normalAnnot;
    }
    return annotation.setAnnotationMirror(mirror)
        .setTypeName(new SimpleName(mirror.getAnnotationType().asElement()));
  }

  private List<Annotation> convertAnnotations(Element element) {
    List<Annotation> annotations = new ArrayList<>();
    /* TODO(manvithn): inheritance; consider Elements.getAllAnnotationMirrors() */
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      annotations.add(convertAnnotation(annotationMirror));
    }
    return annotations;
  }

  private TreeNode convertTypeDeclaration(TypeElement element) {
    TypeDeclaration typeDecl = new TypeDeclaration(element);
    convertBodyDeclaration(typeDecl, element);
    for (Element elem : element.getEnclosedElements()) {
      // Ignore inner types, as they are defined by other classfiles.
      if (!elem.getKind().isClass() && !elem.getKind().isInterface()) {
        BodyDeclaration decl = (BodyDeclaration) convert(elem, typeDecl);
        if (decl != null) {
          typeDecl.addBodyDeclaration(decl);
        }
      }
    }
    if (typeDecl.isInterface()) {
      removeInterfaceModifiers(typeDecl);
    }
    return typeDecl;
  }

  private String getMethodDescriptor(ExecutableElement exec) {
    return translationEnv.typeUtil().getMethodDescriptor((ExecutableType) exec.asType());
  }

  private TreeNode convertMethodDeclaration(ExecutableElement element,
      AbstractTypeDeclaration node) {
    MethodDeclaration methodDecl = new MethodDeclaration(element);
    convertBodyDeclaration(methodDecl, element);
    HashMap<String, VariableElement> localVariableTable = new HashMap<>();
    List<SingleVariableDeclaration> parameters = methodDecl.getParameters();
    String name = element.getSimpleName().toString();
    String descriptor = getMethodDescriptor(element);
    if (element.getParameters().size() > 0) {
      Iterator<ParameterDeclaration> paramsIterator = methodDecl.isConstructor()
          ? classFile.getConstructor(descriptor).getParameters().iterator()
          : classFile.getMethod(name, descriptor).getParameters().iterator();
      // If classfile was compiled with -parameters flag; use the MethodNode
      // to work around potential javac8 bug iterating over parameter names.
      for (VariableElement param : element.getParameters()) {
        SingleVariableDeclaration varDecl = (SingleVariableDeclaration) convert(param, methodDecl);
        String nameDef = paramsIterator.next().getName();
        // If element's name doesn't match the ParameterNode's name, use the latter.
        if (!nameDef.equals(param.getSimpleName().toString())) {
          param = GeneratedVariableElement.newParameter(nameDef, param.asType(),
              param.getEnclosingElement());
          varDecl.setVariableElement(param);
        }
        parameters.add(varDecl);
        localVariableTable.put(param.getSimpleName().toString(), param);
      }
    }
    if (element.isVarArgs()) {
      SingleVariableDeclaration lastParam = parameters.get(parameters.size() - 1);
      TypeMirror paramType = lastParam.getType().getTypeMirror();
      assert paramType.getKind() == TypeKind.ARRAY;
      TypeMirror varArgType = ((ArrayType) paramType).getComponentType();
      lastParam.setType(Type.newType(varArgType));
      lastParam.setIsVarargs(true);
    }
    if (!ElementUtil.isAbstract(element)) {
      EntityDeclaration decl = methodDecl.isConstructor()
          ? classFile.getConstructor(descriptor)
          : classFile.getMethod(name, descriptor);
      MethodTranslator translator = new MethodTranslator(
          parserEnv, translationEnv, element, node, localVariableTable);
      methodDecl.setBody((Block) decl.acceptVisitor(translator, null));
    }
    return methodDecl;
  }

  private TreeNode convertParameter(VariableElement element) {
    return new SingleVariableDeclaration(element).setAnnotations(convertAnnotations(element));
  }

  private TreeNode convertFieldDeclaration(VariableElement element) {
    Object constantValue = element.getConstantValue();
    Expression initializer = constantValue != null
        ? TreeUtil.newLiteral(constantValue, translationEnv.typeUtil()) : null;
    FieldDeclaration fieldDecl = new FieldDeclaration(element, initializer);
    convertBodyDeclaration(fieldDecl, element);
    return fieldDecl;
  }

  private boolean isEnumSynthetic(Element e, TypeMirror enumType) {
    if (e.getKind() == ElementKind.STATIC_INIT) {
      return true;
    }
    if (e.getKind() == ElementKind.METHOD) {
      ExecutableElement method = (ExecutableElement) e;
      String enumSig = translationEnv.typeUtil().getSignatureName(enumType);
      String valueOfDesc = "(Ljava/lang/String;)" + enumSig;
      String valuesDesc = "()[" + enumSig;
      String name = method.getSimpleName().toString();
      String methodDesc = getMethodDescriptor(method);
      boolean isValueOf = name.equals("valueOf") && methodDesc.equals(valueOfDesc);
      boolean isValues = name.equals("values") && methodDesc.equals(valuesDesc);
      return isValueOf || isValues;
    }
    return false;
  }

  private TreeNode convertEnumDeclaration(TypeElement element){
    EnumDeclaration enumDecl = new EnumDeclaration(element);
    convertBodyDeclaration(enumDecl, element);
    for (Element elem : element.getEnclosedElements()) {
      if (!isEnumSynthetic(elem, element.asType())) {
        TreeNode encElem = convert(elem, enumDecl);
        if (encElem.getKind() == TreeNode.Kind.ENUM_CONSTANT_DECLARATION) {
          enumDecl.addEnumConstant((EnumConstantDeclaration) encElem);
        } else {
          enumDecl.addBodyDeclaration((BodyDeclaration) encElem);
        }
      }
    }
    enumDecl.removeModifiers(Modifier.FINAL);
    return enumDecl;
  }

  private TreeNode convertEnumConstantDeclaration(VariableElement element) {
    EnumConstantDeclaration enumConstDecl = new EnumConstantDeclaration(element);
    convertBodyDeclaration(enumConstDecl, element);
    return enumConstDecl;
  }
}
