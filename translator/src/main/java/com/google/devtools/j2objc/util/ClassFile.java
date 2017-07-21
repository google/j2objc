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

import com.strobel.assembler.InputTypeLoader;
import com.strobel.assembler.metadata.FieldDefinition;
import com.strobel.assembler.metadata.MetadataSystem;
import com.strobel.assembler.metadata.MethodDefinition;
import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import java.io.IOException;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;

/**
 * JVM class file model, which uses a Procyon TypeDefinition as a delegate.
 */
public class ClassFile {
  private final TypeDefinition typeDef;
  private final TypeUtil typeUtil;

  public static ClassFile create(String path, TypeUtil typeUtil) throws IOException {
    final MetadataSystem metadataSystem = new MetadataSystem(new InputTypeLoader());
    TypeReference typeRef = metadataSystem.lookupType(path);
    TypeDefinition typeDef = metadataSystem.resolve(typeRef);
    return new ClassFile(typeDef, typeUtil);
  }

  private ClassFile(TypeDefinition typeDefinition, TypeUtil typeUtil) {
    this.typeDef = typeDefinition;
    this.typeUtil = typeUtil;
  }

  /**
   * Returns the simple name of the type defined by this class file.
   */
  public String getName() {
    return typeDef.getName();
  }

  /**
   * Returns the fully-qualified name of the type defined by this class file.
   */
  public String getFullName() {
    return typeDef.getFullName();
  }

  /**
   * Returns the Procyon field definition for a specified variable element,
   * or null if not found.
   */
  public FieldDefinition getFieldNode(VariableElement field) {
    String name = field.getSimpleName().toString();
    String descriptor = typeUtil.getFieldDescriptor(field.asType());
    for (FieldDefinition node : typeDef.getDeclaredFields()) {
      if (node.getName().equals(name) && node.getErasedSignature().equals(descriptor)) {
        return node;
      }
    }
    return null;
  }

  /**
   * Returns the Procyon method definition for a specified executable element,
   * or null if not found.
   */
  public MethodDefinition getMethodNode(ExecutableElement method) {
    String name = method.getSimpleName().toString();
    String descriptor = typeUtil.getMethodDescriptor((ExecutableType) method.asType());
    for (MethodDefinition node : typeDef.getDeclaredMethods()) {
      if (node.getName().equals(name) && node.getErasedSignature().equals(descriptor)) {
        return node;
      }
    }
    return null;
  }
}
