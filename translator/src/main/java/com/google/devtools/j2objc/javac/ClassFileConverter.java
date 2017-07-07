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
import com.google.devtools.j2objc.ast.BodyDeclaration;
import com.google.devtools.j2objc.ast.CompilationUnit;
import com.google.devtools.j2objc.ast.Expression;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.Name;
import com.google.devtools.j2objc.ast.PackageDeclaration;
import com.google.devtools.j2objc.ast.QualifiedName;
import com.google.devtools.j2objc.ast.SimpleName;
import com.google.devtools.j2objc.ast.SingleVariableDeclaration;
import com.google.devtools.j2objc.ast.SourcePosition;
import com.google.devtools.j2objc.ast.TreeNode;
import com.google.devtools.j2objc.ast.TreeUtil;
import com.google.devtools.j2objc.ast.Type;
import com.google.devtools.j2objc.ast.TypeDeclaration;
import com.google.devtools.j2objc.file.InputFile;
import com.google.devtools.j2objc.util.ErrorUtil;
import com.google.devtools.j2objc.util.TranslationEnvironment;
import com.google.devtools.j2objc.util.TypeUtil;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
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

/**
 * Converts a JVM classfile into a CompilationUnit.
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
//      case ANNOTATION_TYPE:
//        break;
      case CLASS:
      case INTERFACE:
        node = convertTypeDeclaration((TypeElement) element);
        break;
      case CONSTRUCTOR:
      case METHOD:
        node = convertMethodDeclaration((ExecutableElement) element);
        break;
//      case ENUM:
//        break;
//      case ENUM_CONSTANT:
//        break;
//      case EXCEPTION_PARAMETER:
//        break;
      case FIELD:
        node = convertFieldDeclaration((VariableElement) element);
        break;
//      case INSTANCE_INIT:
//        break;
//      case LOCAL_VARIABLE:
//        break;
//      case OTHER:
//        break;
      case PACKAGE:
        node = convertPackage((PackageElement) element);
        break;
      case PARAMETER:
        node = convertParameter((VariableElement) element);
        break;
//      case RESOURCE_VARIABLE:
//        break;
//      case STATIC_INIT:
//        break;
//      case TYPE_PARAMETER:
//        break;
      default:
        throw new AssertionError("Unknown element kind: " + element.getKind());
    }
    return node.setPosition(SourcePosition.NO_POSITION);
  }

  /* TODO(user): source-level debugging; see TreeConverter.convertName() */
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

  private TreeNode convertBodyDeclaration(BodyDeclaration newNode) {
    return newNode;
        /* TODO(user): annotations, source-level debugging; finish when supported
         * .setAnnotations(convertAnnotations(modifiers))
         * .setJavadoc((Javadoc) getAssociatedJavaDoc(node, element)); */
  }

  private TreeNode convertTypeDeclaration(TypeElement element) {
    TypeDeclaration typeDecl = new TypeDeclaration(element);
    convertBodyDeclaration(typeDecl);
    /* TODO(user): inheritance; may need to also use Elements.getAllMembers(TypeElement type) or
     * ElementUtil.getMethods(), etc. */
    for (Element elem : element.getEnclosedElements()) {
      typeDecl.addBodyDeclaration((BodyDeclaration) convert(elem));
    }
    if (typeDecl.isInterface()) {
      typeDecl.removeModifiers(Modifier.PUBLIC | Modifier.ABSTRACT);
      for (BodyDeclaration bodyDecl : typeDecl.getBodyDeclarations()) {
        bodyDecl.removeModifiers(Modifier.PUBLIC | Modifier.ABSTRACT);
      }
    }
    return typeDecl;
  }

  private TreeNode convertMethodDeclaration(ExecutableElement element) {
    MethodDeclaration methodDecl = new MethodDeclaration(element);
    List<SingleVariableDeclaration> parameters = methodDecl.getParameters();
    for (VariableElement param : element.getParameters()) {
      parameters.add((SingleVariableDeclaration) convert(param));
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
    return new SingleVariableDeclaration(element);
        /* TODO(user): annotations; finish when supported
         * .setAnnotations(convertAnnotations(node.getModifiers())); */
  }

  /* TODO(user): fields are linked to the static initializer and constructors;
   * consider storing static final compile-time constants;
   * static final primitive types or Strings */
  private TreeNode convertFieldDeclaration(VariableElement element) {
    Object constantValue = element.getConstantValue();
    Expression initializer = constantValue != null
        ? TreeUtil.newLiteral(constantValue, translationEnv.typeUtil()) : null;
    FieldDeclaration fieldDecl = new FieldDeclaration(element, initializer);
    convertBodyDeclaration(fieldDecl);
    return fieldDecl;
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
