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

import static com.google.devtools.treeshaker.UsedCodeMarker.CLASS_INITIALIZER_NAME;
import static com.google.devtools.treeshaker.UsedCodeMarker.INITIALIZER_NAME;
import static com.google.devtools.treeshaker.UsedCodeMarker.PSEUDO_CONSTRUCTOR_PREFIX;
import static com.google.devtools.treeshaker.UsedCodeMarker.SIGNATURE_PREFIX;

import com.google.common.base.Splitter;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import java.util.Collection;
import java.util.List;

final class ClassHierarchyAnalyzer {
  static CodeReferenceMap analyze(Collection<Type> types) {
    types.stream().filter(Type::isExported).forEach(ClassHierarchyAnalyzer::markTypeLive);

    // Go over the entry points to start the traversal.
    types.stream()
        .flatMap(t -> t.getMembers().stream())
        .filter(Member::isExported)
        .forEach(m -> onMemberReference(m));

    CodeReferenceMap.Builder unusedBuilder = CodeReferenceMap.builder();
    for (Type type : types) {
      if (type.isLive()) {
        for (Member member : type.getMembers()) {
          if (!member.isLive()) {
            String method = member.getName();
            if (method.startsWith(PSEUDO_CONSTRUCTOR_PREFIX)) {
              // skip interface pseudo-constructors
              continue;
            }
            List<String> components =
                Splitter.onPattern(SIGNATURE_PREFIX).splitToList(method);
            // TODO(dpo): add better checking for name & signature components.
            if (components.isEmpty() || components.size() != 2) {
              continue;
            }
            String name = components.get(0);
            String sig = components.get(1);
            unusedBuilder.addMethod(type.getName(), name, sig);
          }
        }
      } else {
        unusedBuilder.addClass(type.getName());
      }
    }
    return unusedBuilder.build();
  }

  private static void onMemberReference(Member member) {
    if (member.isPolymorphic()) {
      traversePolymorphicReference(member.getDeclaringType(), member.getName());
    } else {
      markTypeLive(member.getDeclaringType());
      markMemberLive(member);
    }
  }

  private static void markMemberLive(Member member) {
    if (member.isLive()) {
      return;
    }

    member.markLive();
    member.getReferencedMembers().forEach(ClassHierarchyAnalyzer::onMemberReference);
    member.getReferencedTypes().forEach(ClassHierarchyAnalyzer::markTypeLive);
  }

  private static void traversePolymorphicReference(Type type, String memberName) {
    Member member = type.getMemberByName(memberName);
    if (member == null) {
      // No member found in this class so we need to mark the supertype method as
      // live since it might be an implicit override.
      markOverriddenMemberLive(type, memberName);
    } else if (member.isPolymorphic()) {
      markMemberLive(member);
    }
    // Recursively unfold the overriding chain.
    type.getImmediateSubtypes()
        .forEach(subtype -> traversePolymorphicReference(subtype, memberName));
  }

  private static void markOverriddenMemberLive(Type type, String memberName) {
    while ((type = type.getSuperClass()) != null) {
      Member member = type.getMemberByName(memberName);
      if (member != null && member.isPolymorphic()) {
        markMemberLive(member);
        return;
      }
    }
  }

  private static void markTypeLive(Type type) {
    if (type.isLive()) {
      return;
    }

    type.markLive();
    markMemberLive(type.getMemberByName(CLASS_INITIALIZER_NAME));
    markMemberLive(type.getMemberByName(INITIALIZER_NAME));
    if (type.getSuperClass() != null) {
      markTypeLive(type.getSuperClass());
    }
    type.getSuperInterfaces().forEach(ClassHierarchyAnalyzer::markTypeLive);
  }

  private ClassHierarchyAnalyzer() {}
}
