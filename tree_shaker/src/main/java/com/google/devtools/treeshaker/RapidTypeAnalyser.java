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
import com.google.common.flogger.GoogleLogger;
import com.google.devtools.j2objc.util.CodeReferenceMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

final class RapidTypeAnalyser {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  static CodeReferenceMap analyse(Collection<Type> types) {
    types.stream().filter(Type::isExported).forEach(RapidTypeAnalyser::markTypeLive);

    // Go over the entry points to start the traversal.
    types.stream()
        .flatMap(t -> t.getMembers().stream())
        .filter(Member::isExported)
        .forEach(m -> onMemberReference(m));

    CodeReferenceMap.Builder unusedBuilder = CodeReferenceMap.builder();
    List<String> uninstantiated = new ArrayList<>();
    for (Type type : types) {
      if (type.isLive()) {
        for (Member member : type.getMembers()) {
          if (!member.isLive()) {
            String method = member.getName();
            if (method.startsWith(PSEUDO_CONSTRUCTOR_PREFIX)) {
              // skip interface pseudo-constructors
              continue;
            }
            if (!type.isInstantiated() && method.equals(INITIALIZER_NAME)) {
              // skip unused initializers for uninstantiated types
              // TODO(dpo): investigate how best to remove initializers for uninstantiated classes.
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
        if (!type.isInstantiated()) {
          for (Member member : type.getPotentiallyLiveMembers()) {
            if (member.getName().equals(INITIALIZER_NAME)) {
              continue;
            }
            uninstantiated.add(type.getName() + "::" + member.getName());
          }
        }
      } else {
        unusedBuilder.addClass(type.getName());
      }
    }
    if (!uninstantiated.isEmpty()) {
      logger.atFine().log("Uninstantiated Members: %s", String.join(", ", uninstantiated));
    }
    return unusedBuilder.build();
  }

  private static void onMemberReference(Member member) {
    if (member.isPolymorphic()) {
      traversePolymorphicReference(member.getDeclaringType(), member.getSignature());
    } else {
      markTypeLive(member.getDeclaringType());
    }
    markMemberLive(member);
  }

  private static void markMemberLive(Member member) {
    if (member.isLive()) {
      return;
    }

    member.markLive();
    if (member.isConstructor()) {
      markInstantiated(member.getDeclaringType());
    }
    member.getReferencedMembers().forEach(RapidTypeAnalyser::onMemberReference);
    member.getReferencedTypes().forEach(RapidTypeAnalyser::markTypeLive);
  }

  private static void markInstantiated(Type type) {
    if (type.isInstantiated()) {
      return;
    }

    type.instantiate();
    markMemberLive(type.getMemberByName(INITIALIZER_NAME));
    type.getPotentiallyLiveMembers().forEach(RapidTypeAnalyser::markMemberLive);
    for (Type iface : type.getSuperInterfaces()) {
      markInstantiated(iface);
    }
  }

  private static void traversePolymorphicReference(Type type, String memberSignature) {
    Member member = type.getMemberBySignature(memberSignature);
    if (member == null) {
      // If no member found, check for a member which original member matches the signature.
      Optional<Member> matchingMember =
          type.getMembers().stream()
              .filter(
                  candidate ->
                      candidate.getOriginalMember() != null
                          && memberSignature.equals(candidate.getOriginalMember().getSignature()))
              .findFirst();
      if (matchingMember.isPresent()) {
        member = matchingMember.get();
      }
    }
    if (member == null) {
      // No member found in this class. In this case we need to mark the supertype method as
      // potentially live since it might be an accidental override.
      markOverriddenMembersPotentiallyLive(type, memberSignature);
    } else if (member.isPolymorphic()) {
      if (member.isFullyTraversed()) {
        return;
      }
      member.markFullyTraversed();
      markMemberPotentiallyLive(member);
    }
    // Recursively unfold the overriding chain.
    type.getImmediateSubtypes()
        .forEach(subtype -> traversePolymorphicReference(subtype, memberSignature));
  }

  private static void markOverriddenMembersPotentiallyLive(Type type, String memberSignature) {
    while ((type = type.getSuperClass()) != null) {
      Member member = type.getMemberBySignature(memberSignature);
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
    markMemberLive(type.getMemberByName(CLASS_INITIALIZER_NAME));

    // TODO(tball): remove when dead fields are reported (b/225384453).
    markMemberLive(type.getMemberByName(INITIALIZER_NAME));

    type.getMembers()
        .forEach(
            member -> {
              // Mark members where the original method is from an external type.
              // Mark members that have the UsedByNative annotation if the type is used.
              if (member.getOriginalMember() == null || member.hasUsedByNativeAnnotation()) {
                markMemberLive(member);
              }
            });

    if (type.getSuperClass() != null) {
      markTypeLive(type.getSuperClass());
    }
    // When a type is marked as live, we need to explicitly mark the super interfaces as live since
    // we need markImplementor call (which are not tracked in AST).
    type.getSuperInterfaces().forEach(RapidTypeAnalyser::markTypeLive);
  }

  private RapidTypeAnalyser() {}
}
