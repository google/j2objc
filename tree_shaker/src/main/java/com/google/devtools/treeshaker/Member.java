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

import java.util.ArrayList;
import java.util.List;

final class Member {
  static Member buildFrom(MemberInfo memberInfo, Type declaringType) {
    return new Member(
        memberInfo.getName(),
        declaringType,
        memberInfo.getStatic(),
        memberInfo.getConstructor(),
        memberInfo.getExported(),
        memberInfo.getAnnotations());
  }

  private final String name;
  private final Type declaringType;
  private final boolean isStatic;
  private final boolean isConstructor;
  private final boolean isExported;
  private final String signature;

  private boolean fullyTraversed;
  private boolean live;
  private final List<Type> referencedTypes = new ArrayList<>();
  private final List<Member> referencedMembers = new ArrayList<>();
  private Member originalMember;
  private final Annotations annotations;

  private Member(String name, Type declaringType, boolean isStatic, boolean isConstructor,
      boolean isExported, Annotations annotations) {
    this.name = name;
    this.declaringType = declaringType;
    this.isStatic = isStatic;
    this.isConstructor = isConstructor;
    this.isExported = isExported;
    this.signature = name.substring(0, name.lastIndexOf(')') + 1);
    this.annotations = annotations;
  }

  Type getDeclaringType() {
    return declaringType;
  }

  boolean isExported() {
    return isExported;
  }

  String getName() {
    return name;
  }

  String getSignature() {
    return signature;
  }

  public boolean isConstructor() {
    return isConstructor;
  }

  public boolean isPolymorphic() {
    return !isStatic && !isConstructor();
  }

  boolean isLive() {
    return live;
  }

  void markLive() {
    this.live = true;
  }

  boolean isFullyTraversed() {
    return fullyTraversed;
  }

  void markFullyTraversed() {
    this.fullyTraversed = true;
  }

  List<Type> getReferencedTypes() {
    return referencedTypes;
  }

  void addReferencedType(Type referencedType) {
    this.referencedTypes.add(referencedType);
  }

  List<Member> getReferencedMembers() {
    return referencedMembers;
  }

  void addReferencedMember(Member referencedMember) {
    referencedMembers.add(referencedMember);
  }

  Member getOriginalMember() {
    return originalMember;
  }

  void setOriginalMember(Member originalMember) {
    this.originalMember = originalMember;
  }

  boolean hasUsedByNativeAnnotation() {
    return annotations.getUsedByNative();
  }
}
