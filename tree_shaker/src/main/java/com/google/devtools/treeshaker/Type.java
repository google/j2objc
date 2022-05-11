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

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class Type {
  static Type buildFrom(TypeInfo typeInfo, String name) {
    return new Type(name, typeInfo.getExported(), typeInfo.getMemberList());
  }

  private final String name;
  private final boolean isExported;
  private final Map<String, Member> membersByName = new LinkedHashMap<>();
  private final Map<String, Member> membersBySignature = new LinkedHashMap<>();

  private Type superClass;
  private final List<Type> superInterfaces = new ArrayList<>();
  private final List<Type> immediateSubtypes = new ArrayList<>();
  private boolean live;
  private boolean instantiated;
  private final Set<Member> potentiallyLiveMembers = new HashSet<>();

  private Type(String name, boolean isExported, Collection<MemberInfo> members) {
    this.name = name;
    this.isExported = isExported;
    members.forEach(memberInfo -> {
      Member member = Member.buildFrom(memberInfo, this);
      Member previous = membersByName.put(member.getName(), member);
      Member previousBySignature = membersBySignature.put(member.getSignature(), member);
      checkState(previous == null);
      checkState(previousBySignature == null);
    });
  }

  String getName() {
    return name;
  }

  boolean isExported() {
    return isExported;
  }

  Member getMemberByName(String name) {
    return membersByName.get(name);
  }

  Member getMemberBySignature(String signature) {
    return membersBySignature.get(signature);
  }

  Collection<Member> getMembers() {
    return membersByName.values();
  }

  Type getSuperClass() {
    return superClass;
  }

  void setSuperClass(Type superClass) {
    this.superClass = superClass;
  }

  Collection<Type> getSuperInterfaces() {
    return superInterfaces;
  }

  void addSuperInterface(Type superInterface) {
    this.superInterfaces.add(superInterface);
  }

  Collection<Type> getImmediateSubtypes() {
    return immediateSubtypes;
  }

  void addImmediateSubtype(Type type) {
    immediateSubtypes.add(type);
  }

  void markLive() {
    this.live = true;
  }

  boolean isLive() {
    return live;
  }

  boolean isInstantiated() {
    return instantiated;
  }

  void instantiate() {
    this.instantiated = true;
  }

  // Returns the list of members that need to mark as live when the type becomes live.
  Collection<Member> getPotentiallyLiveMembers() {
    return potentiallyLiveMembers;
  }

  void addPotentiallyLiveMember(Member member) {
    checkState(!isInstantiated());
    potentiallyLiveMembers.add(member);
  }
}
