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
import java.util.LinkedHashMap;
import java.util.List;

final class Type {
  private String name;
  private Type superClass;
  private final List<Type> superInterfaces = new ArrayList<>();
  private final List<Type> immediateSubtypes = new ArrayList<>();
  private final LinkedHashMap<String, Member> membersByName = new LinkedHashMap<>();
  private String implSourceFile;
  private String headerSourceFile;
  private boolean live;
  private boolean instantiated;
  private boolean isJsTypeInterface;
  private final List<Member> potentiallyLiveMembers = new ArrayList<>();

  static Type buildFrom(TypeInfo typeInfo, String name) {
    Type type = new Type();
    type.name = name;
    type.headerSourceFile = typeInfo.getHeaderSourceFilePath();
    type.implSourceFile = typeInfo.getImplSourceFilePath();
    type.isJsTypeInterface = typeInfo.getJstypeInterface();
    typeInfo
        .getMemberList()
        .forEach(memberInfo -> type.addMember(Member.buildFrom(memberInfo, type)));

    return type;
  }

  private Type() {}

  String getHeaderSourceFile() {
    return headerSourceFile;
  }

  String getImplSourceFile() {
    return implSourceFile;
  }

  Collection<Member> getMembers() {
    return membersByName.values();
  }

  Member getMemberByName(String name) {
    return membersByName.get(name);
  }

  void addMember(Member member) {
    Member previous = membersByName.put(member.getName(), member);
    checkState(previous == null);
  }

  String getName() {
    return name;
  }

  Type getSuperClass() {
    return superClass;
  }

  void setSuperClass(Type superClass) {
    this.superClass = superClass;
  }

  List<Type> getSuperInterfaces() {
    return superInterfaces;
  }

  void addSuperInterface(Type superInterface) {
    this.superInterfaces.add(superInterface);
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

  /** Returns the list of members that need to mark as live when the type becomes live. */
  List<Member> getPotentiallyLiveMembers() {
    return potentiallyLiveMembers;
  }

  void addPotentiallyLiveMember(Member member) {
    checkState(!isInstantiated());
    potentiallyLiveMembers.add(member);
  }

  public void addImmediateSubtype(Type type) {
    immediateSubtypes.add(type);
  }

  public List<Type> getImmediateSubtypes() {
    return immediateSubtypes;
  }

  boolean isJsTypeInterface() {
    return isJsTypeInterface;
  }
}
