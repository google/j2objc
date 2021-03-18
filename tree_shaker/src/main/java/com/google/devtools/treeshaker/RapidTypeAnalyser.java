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

import com.google.common.base.Splitter;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import java.util.Collection;
import java.util.List;

final class RapidTypeAnalyser {

  static CodeReferenceMap analyse(Collection<Type> types) {
    types.stream().filter(Type::isExported).forEach(RapidTypeAnalyser::markTypeLive);

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
            if (method.startsWith(UsedCodeMarker.PSEUDO_CONSTRUCTOR_PREFIX)) {
              // skip interface pseudo-constructors
              continue;
            }
            List<String> components =
                Splitter.onPattern(UsedCodeMarker.SIGNATURE_PREFIX).splitToList(method);
            // TODO(dpo): add better checking for name & signature components.
            if (components.isEmpty() || components.size() != 2) {
              continue;
            }
            String name = components.get(0);
            String sig = components.get(1);
            unusedBuilder.addMethod(member.getDeclaringType().getName(), name, sig);
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
      markMemberLive(
          member.getDeclaringType().getMemberByName(UsedCodeMarker.CLASS_INITIALIZER_NAME));
      markMemberLive(member);
    }
  }

  private static void markMemberLive(Member member) {
    if (member.isLive()) {
      return;
    }

    member.markLive();

    Type declaringType = member.getDeclaringType();
    if (member.isConstructor()) {
      markInstantiated(declaringType);
    }

    member.getReferencedMembers().forEach(RapidTypeAnalyser::onMemberReference);
    member.getReferencedTypes().forEach(RapidTypeAnalyser::markTypeLive);
  }

  private static void markInstantiated(Type type) {
    if (type.isInstantiated()) {
      return;
    }
    type.instantiate();
    type.getPotentiallyLiveMembers().forEach(RapidTypeAnalyser::markMemberLive);
    for (Type iface : type.getSuperInterfaces()) {
      iface.getMemberByName(UsedCodeMarker.CLASS_INITIALIZER_NAME).markLive();
      markInstantiated(iface);
    }
  }

  private static void traversePolymorphicReference(Type type, String memberName) {
    Member member = type.getMemberByName(memberName);
    if (member == null) {
      // No member found in this class. In this case we need to mark the supertype method as
      // potentially live since it might be an accidental override.
      markOverriddenMembersPotentiallyLive(type, memberName);
    } else if (member.isPolymorphic()) {
      if (member.isFullyTraversed()) {
        return;
      }
      member.markFullyTraversed();

      markMemberPotentiallyLive(member);
    }

    // Recursively unfold the overriding chain.
    type.getImmediateSubtypes()
        .forEach(subtype -> traversePolymorphicReference(subtype, memberName));
  }

  private static void markOverriddenMembersPotentiallyLive(Type type, String memberName) {
    while ((type = type.getSuperClass()) != null) {
      Member member = type.getMemberByName(memberName);
      if (member != null && member.isPolymorphic()) {
        markMemberPotentiallyLive(member);
        return;
      }
    }
  }

  private static void markMemberPotentiallyLive(Member member) {
    Type declaringType = member.getDeclaringType();
    if (declaringType.isInstantiated()) {
      markMemberLive(member);
    } else {
      // Type is not instantiated, defer making it live until the type is instantiated.
      declaringType.addPotentiallyLiveMember(member);
    }
  }

  private static void markTypeLive(Type type) {
    if (type.isLive()) {
      return;
    }

    type.markLive();

    // When a type is marked as live, we need to explicitly mark the super interfaces as live since
    // we need markImplementor call (which are not tracked in AST).
    type.getSuperInterfaces().forEach(RapidTypeAnalyser::markTypeLive);
  }

  private RapidTypeAnalyser() {}
}
