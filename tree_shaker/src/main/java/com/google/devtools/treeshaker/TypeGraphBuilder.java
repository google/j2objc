/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.devtools.treeshaker;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Give information about inheritance relationships between types. */
class TypeGraphBuilder {

  static Collection<Type> build(List<LibraryInfo> libraryInfos) {
    Map<String, Type> typesByName = new LinkedHashMap<>();

    // Create all types and members.
    for (LibraryInfo libraryInfo : libraryInfos) {
      for (TypeInfo typeInfo : libraryInfo.getTypeList()) {
        Type type = Type.buildFrom(typeInfo, libraryInfo.getTypeMap(typeInfo.getTypeId()));
        typesByName.put(type.getName(), type);
      }
    }

    // Build cross-references between types and members
    for (LibraryInfo libraryInfo : libraryInfos) {
      buildCrossReferences(typesByName, libraryInfo);
    }

    return typesByName.values();
  }

  private static void buildCrossReferences(Map<String, Type> typesByName, LibraryInfo libraryInfo) {
    for (TypeInfo typeInfo : libraryInfo.getTypeList()) {
      Type type = typesByName.get(libraryInfo.getTypeMap(typeInfo.getTypeId()));

      int extendsId = typeInfo.getExtendsType();
      // TODO(dpo): re-abstract
      //if (extendsId != LibraryInfoBuilder.NULL_TYPE) {
      if (extendsId != 0) {
        Type superClass = typesByName.get(libraryInfo.getTypeMap(extendsId));
        superClass.addImmediateSubtype(type);
        type.setSuperClass(superClass);
      }

      for (int implementsId : typeInfo.getImplementsTypeList()) {
        Type superInterface = typesByName.get(libraryInfo.getTypeMap(implementsId));
        superInterface.addImmediateSubtype(type);
        type.addSuperInterface(superInterface);
      }

      for (MemberInfo memberInfo : typeInfo.getMemberList()) {
        Member member = type.getMemberByName(memberInfo.getName());

        for (int referencedId : memberInfo.getReferencedTypesList()) {
          Type referencedType = typesByName.get(libraryInfo.getTypeMap(referencedId));
          member.addReferencedType(checkNotNull(referencedType));
        }

        for (MethodInvocation methodInvocation : memberInfo.getInvokedMethodsList()) {
          Type enclosingType =
              typesByName.get(libraryInfo.getTypeMap(methodInvocation.getEnclosingType()));
          Member referencedMember = enclosingType.getMemberByName(methodInvocation.getMethod());
          member.addReferencedMember(checkNotNull(referencedMember));
        }
      }
    }
  }

  private TypeGraphBuilder() {}
}
