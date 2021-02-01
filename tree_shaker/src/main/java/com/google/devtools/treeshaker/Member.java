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
    Member member = new Member();
    member.memberInfo = memberInfo;
    member.declaringType = declaringType;
    member.isStatic = memberInfo.getStatic();
    member.isConstructor = memberInfo.getConstructor();
    return member;
  }

  private MemberInfo memberInfo;
  private Type declaringType;
  private boolean isStatic;
  private boolean isConstructor;

  private boolean fullyTraversed;
  private boolean live;
  private final List<Type> referencedTypes = new ArrayList<>();
  private final List<Member> referencedMembers = new ArrayList<>();

  private Member() {}

  Type getDeclaringType() {
    return declaringType;
  }

  boolean isJsAccessible() {
    return memberInfo.getJsAccessible();
  }

  String getName() {
    return memberInfo.getName();
  }

  boolean hasPosition() {
    return memberInfo.hasPosition();
  }

  SourcePosition getPosition() {
    return memberInfo.getPosition();
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
}
