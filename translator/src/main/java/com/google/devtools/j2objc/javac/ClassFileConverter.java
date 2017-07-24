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
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.types.GeneratedVariableElement;
import com.google.devtools.j2objc.util.ElementUtil;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.TranslationEnvironment;
import com.google.devtools.j2objc.util.TypeUtil;
import com.google.j2objc.annotations.Property;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;

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
public class ClassFileConverter extends ClassVisitor {
  private final JavacEnvironment parserEnv;
  private final TranslationEnvironment translationEnv;
  private final InputFile file;
  private final ClassFile classFile;
  private String typeName;

  public static CompilationUnit convertClassFile(
      Options options, JavacEnvironment env, InputFile file) {
    try {
      env.saveParameterNames();
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
    super(Opcodes.ASM5);
    this.parserEnv = parserEnv;
    this.translationEnv = translationEnv;
    this.file = file;
    this.classFile = ClassFile.create(file.getInputStream(), translationEnv.typeUtil());
    this.typeName = convertInternalTypeName(classFile.name);
  }

  private static String convertInternalTypeName(String internalName) {
    return org.objectweb.asm.Type.getObjectType(internalName).getClassName();
  }

  /**
   * Set classpath to the root path of the input file, to support typeElement lookup.
   */
  private void setClassPath() throws IOException {
    String fullPath = file.getAbsolutePath();
    String rootPath = fullPath.substring(0, fullPath.lastIndexOf(classFile.name + ".class"));
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
    compUnit.setPackage((PackageDeclaration) convert(pkgElement));
    compUnit.addType((AbstractTypeDeclaration) convert(typeElement));
    return compUnit;
  }

  @SuppressWarnings("fallthrough")
  private TreeNode convert(Element element) {
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
        node = convertMethodDeclaration((ExecutableElement) element);
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
        node = convertMethodDeclaration((ExecutableElement) element);
        break;
      default:
        throw new AssertionError("Unsupported element kind: " + element.getKind());
    }
    return node;
  }

  private Name convertName(Symbol symbol) {
    if (symbol.owner == null || symbol.owner.name.isEmpty()) {
      return new SimpleName(symbol);
    }
    return new QualifiedName(symbol, symbol.asType(), convertName(symbol.owner));
  }

  private TreeNode convertPackage(PackageElement element) {
    return new PackageDeclaration()
        .setPackageElement(element)
        .setName(convertName((PackageSymbol) element));
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
          : (BodyDeclaration) convert(elem);
      annotTypeDecl.addBodyDeclaration(bodyDecl);
    }
    removeInterfaceModifiers(annotTypeDecl);
    return annotTypeDecl;
  }

  private Expression convertAnnotationValue(TypeMirror type, AnnotationValue annot) {
    /* TODO(user): see if the method implements the AnnotationMirror branch correctly */
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
    /* TODO(user): inheritance; consider Elements.getAllAnnotationMirrors() */
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      annotations.add(convertAnnotation(annotationMirror));
    }
    return annotations;
  }

  private TreeNode convertTypeDeclaration(TypeElement element) {
    TypeDeclaration typeDecl = new TypeDeclaration(element);
    convertBodyDeclaration(typeDecl, element);
    for (Element elem : element.getEnclosedElements()) {
      typeDecl.addBodyDeclaration((BodyDeclaration) convert(elem));
    }
    if (typeDecl.isInterface()) {
      removeInterfaceModifiers(typeDecl);
    }
    return typeDecl;
  }

  private TreeNode convertMethodDeclaration(ExecutableElement element) {
    MethodDeclaration methodDecl = new MethodDeclaration(element);
    convertBodyDeclaration(methodDecl, element);
    List<SingleVariableDeclaration> parameters = methodDecl.getParameters();
    int nParams = element.getParameters().size();
    if (nParams > 0) {
      // If classfile was compiled with -parameters flag; use the MethodNode
      // to work around potential javac8 bug iterating over parameter names.
      MethodNode asmNode = classFile.getMethodNode(element);
      int nMethodNodes = asmNode.parameters != null ? asmNode.parameters.size() : 0;
      for (int i = 0; i < nParams; i++) {
        VariableElement param = element.getParameters().get(i);
        SingleVariableDeclaration varDecl = (SingleVariableDeclaration) convert(param);
        if (nMethodNodes == nParams) {
          ParameterNode paramNode = (ParameterNode) asmNode.parameters.get(i);
          // If element's name doesn't match the ParameterNode's name, use the latter.
          if (!paramNode.name.equals(param.getSimpleName().toString())) {
            param = GeneratedVariableElement.newParameter(paramNode.name, param.asType(),
                param.getEnclosingElement());
            varDecl.setVariableElement(param);
          }
        }
        parameters.add(varDecl);
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
    return methodDecl;
        /* TODO(user): method translation; finish when supported
         * .setBody((Block) convert(node.getBody())); */
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
    } else if (e.getKind() == ElementKind.METHOD) {
      ExecutableElement method = (ExecutableElement) e;
      TypeUtil typeUtil = translationEnv.typeUtil();
      String enumSig = typeUtil.getSignatureName(enumType);
      String valueOfDesc = "(Ljava/lang/String;)" + enumSig;
      String valuesDesc = "()[" + enumSig;
      String name = method.getSimpleName().toString();
      String methodDesc = typeUtil.getMethodDescriptor((ExecutableType) method.asType());
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
      TreeNode encElem = convert(elem);
      if (encElem.getKind() == TreeNode.Kind.ENUM_CONSTANT_DECLARATION) {
        enumDecl.addEnumConstant((EnumConstantDeclaration) encElem);
      } else if (!isEnumSynthetic(elem, element.asType())) {
        enumDecl.addBodyDeclaration((BodyDeclaration) encElem);
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

  // Extension of ClassNode that supports look up of ASM nodes using elements.
  static class ClassFile extends ClassNode {
    private final TypeUtil typeUtil;

    static ClassFile create(InputStream clazz, TypeUtil typeUtil) throws IOException {
      ClassReader classReader = new ClassReader(clazz);
      ClassFile cn = new ClassFile(typeUtil);
      classReader.accept(cn, ClassReader.EXPAND_FRAMES);
      return cn;
    }

    ClassFile(TypeUtil typeUtil) {
      super(Opcodes.ASM5);
      this.typeUtil = typeUtil;
    }

    @SuppressWarnings("unchecked")
    List<FieldNode> getFields() {
      return fields;
    }

    @SuppressWarnings("unchecked")
    List<MethodNode> getMethods() {
      return methods;
    }

    FieldNode getFieldNode(VariableElement field) {
      String name = field.getSimpleName().toString();
      String descriptor = typeUtil.getFieldDescriptor(field.asType());
      for (FieldNode node : getFields()) {
        if (node.name.equals(name) && node.desc.equals(descriptor)) {
          return node;
        }
      }
      throw new AssertionError("unable to find field node for " + field);
    }

    MethodNode getMethodNode(ExecutableElement method) {
      String name = method.getSimpleName().toString();
      String descriptor = typeUtil.getMethodDescriptor((ExecutableType) method.asType());
      for (MethodNode node : getMethods()) {
        if (node.name.equals(name) && node.desc.equals(descriptor)) {
          return node;
        }
      }
      throw new AssertionError("unable to find method node for " + method);
    }
  }
}
