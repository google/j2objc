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

package com.google.devtools.j2objc.util;

import com.google.devtools.j2objc.file.InputFile;
import com.strobel.assembler.InputTypeLoader;
import com.strobel.assembler.metadata.IMetadataResolver;
import com.strobel.assembler.metadata.ITypeLoader;
import com.strobel.assembler.metadata.JarTypeLoader;
import com.strobel.assembler.metadata.MetadataParser;
import com.strobel.assembler.metadata.MetadataSystem;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.languages.EntityType;
import com.strobel.decompiler.languages.Languages;
import com.strobel.decompiler.languages.java.ast.AstNodeCollection;
import com.strobel.decompiler.languages.java.ast.AstType;
import com.strobel.decompiler.languages.java.ast.CompilationUnit;
import com.strobel.decompiler.languages.java.ast.ConstructorDeclaration;
import com.strobel.decompiler.languages.java.ast.EntityDeclaration;
import com.strobel.decompiler.languages.java.ast.FieldDeclaration;
import com.strobel.decompiler.languages.java.ast.MethodDeclaration;
import com.strobel.decompiler.languages.java.ast.ParameterDeclaration;
import com.strobel.decompiler.languages.java.ast.TypeDeclaration;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * JVM class file model, which uses a Procyon TypeDefinition as a delegate.
 */
public class ClassFile {
  private final CompilationUnit unit;
  private final TypeDeclaration type;

  public static ClassFile create(InputFile file) throws IOException {
    ITypeLoader loader;
    String path = file.getAbsolutePath();
    if (path.endsWith(".jar")) {
      loader = new JarTypeLoader(new JarFile(path));
      path = file.getUnitName();
      if (!path.endsWith(".class")) {
        return null;
      }
      // Remove .class suffix, as JarTypeLoader adds it.
      path = path.substring(0, path.length() - 6);
    } else {
      loader = new InputTypeLoader();
    }
    MetadataSystem metadataSystem = new MetadataSystem(loader);
    CompilationUnit unit = decompileClassFile(path, metadataSystem);

    return new ClassFile(unit);
  }

  private static CompilationUnit decompileClassFile(String path, MetadataSystem metadataSystem) {
    TypeReference typeRef;
    /* Hack to get around classes whose descriptors clash with primitive types. */
    if (path.length() == 1) {
      MetadataParser parser = new MetadataParser(IMetadataResolver.EMPTY);
      typeRef = metadataSystem.resolve(parser.parseTypeDescriptor(path));
    } else {
      typeRef = metadataSystem.lookupType(path);
    }
    TypeDefinition typeDef = typeRef.resolve();

    /* TODO(tball): Support deobfuscation?
     * DeobfuscationUtilities.processType(typeDef);
     */

    DecompilationOptions options = new DecompilationOptions();
    DecompilerSettings settings = DecompilerSettings.javaDefaults();
    settings.setShowSyntheticMembers(true);
    options.setSettings(settings);
    options.setFullDecompilation(true);
    return Languages.java().decompileTypeToAst(typeDef, options);
  }

  private ClassFile(CompilationUnit unit) {
    this.unit = unit;
    assert unit.getTypes().size() == 1;
    this.type = unit.getTypes().firstOrNullObject();
  }

  /**
   * Returns the simple name of the type defined by this class file.
   */
  public String getName() {
    return type.getName();
  }

  /**
   * Returns the fully-qualified name of the type defined by this class file.
   */
  public String getFullName() {
    String pkgName = unit.getPackage().getName();
    String typeName = type.getName();
    return pkgName.isEmpty() ? typeName : pkgName + "." + typeName;
  }

  /**
   * Returns the Procyon field definition for a specified variable,
   * or null if not found.
   */
  public FieldDeclaration getFieldNode(String name, String signature) {
    for (EntityDeclaration node : type.getMembers()) {
      if (node.getEntityType() == EntityType.FIELD) {
        FieldDeclaration field = (FieldDeclaration) node;
        if (field.getName().equals(name)
            && signature(field.getReturnType()).equals(signature)) {
          return field;
        }
      }
    }
    return null;
  }

  /**
   * Returns the Procyon method definition for a specified method,
   * or null if not found.
   */
  public MethodDeclaration getMethod(String name, String signature) {
    for (EntityDeclaration node : type.getMembers()) {
      if (node.getEntityType() == EntityType.METHOD) {
        MethodDeclaration method = (MethodDeclaration) node;
        if (method.getName().equals(name) && signature.equals(signature(method))) {
          return method;
        }
      }
    }
    return null;
  }

  /**
   * Returns the Procyon method definition for a specified constructor,
   * or null if not found.
   */
  public ConstructorDeclaration getConstructor(String signature) {
    for (EntityDeclaration node : type.getMembers()) {
      if (node.getEntityType() == EntityType.CONSTRUCTOR) {
        ConstructorDeclaration cons = (ConstructorDeclaration) node;
        if (signature.equals(signature(cons))) {
          return cons;
        }
      }
    }
    return null;
  }

  public TypeDeclaration getType() {
    return type;
  }

  private String signature(MethodDeclaration method) {
    StringBuilder sb = new StringBuilder();
    sb.append(signature(method.getParameters()));
    sb.append(signature(method.getReturnType()));
    return sb.toString();
  }

  private String signature(ConstructorDeclaration cons) {
    StringBuilder sb = new StringBuilder();
    sb.append(signature(cons.getParameters()));
    sb.append('V');
    return sb.toString();
  }

  private String signature(AstNodeCollection<ParameterDeclaration> parameters) {
    return parameters.stream()
        .map(p -> signature(p.getType()))
        .collect(Collectors.joining("", "(", ")"));
  }

  private String signature(AstType type) {
    return type.toTypeReference().getErasedSignature();
  }
}
