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

package com.google.devtools.j2objc.ast;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.type.TypeMirror;

/**
 * A native declaration. Contains string contents without any structural context
 * other than whether the content is for the header or implementation.
 */
public class NativeDeclaration extends BodyDeclaration {

  // Whether this declaration should be placed inside or outside the @interface
  // or @implementation block.
  private boolean isOuter = false;
  private String headerCode = null;
  private String implementationCode = null;
  private List<TypeMirror> implementationImportTypes = new ArrayList<>();

  public NativeDeclaration(NativeDeclaration other) {
    isOuter = other.isOuter();
    headerCode = other.getHeaderCode();
    implementationCode = other.getImplementationCode();
    implementationImportTypes.addAll(other.getImplementationImportTypes());
  }

  /**
   * Creates a new NativeDeclaration. For proper spacing, the convention is
   * that the code snippets end with a newline.
   *
   * @param isOuter Whether this code should be outside the ObjC class
   * definition.
   * @param headerCode Code to be printed in the type interface.
   * @param implementationCode Code to be printed in the type implementation.
   */
  public NativeDeclaration(boolean isOuter, String headerCode, String implementationCode) {
    this.isOuter = isOuter;
    this.headerCode = headerCode;
    this.implementationCode = implementationCode;
  }

  public static NativeDeclaration newInnerDeclaration(
      String headerCode, String implementationCode) {
    return new NativeDeclaration(false, headerCode, implementationCode);
  }

  public static NativeDeclaration newOuterDeclaration(
      String headerCode, String implementationCode) {
    return new NativeDeclaration(true, headerCode, implementationCode);
  }

  @Override
  public Kind getKind() {
    return Kind.NATIVE_DECLARATION;
  }

  public boolean isOuter() {
    return isOuter;
  }

  public String getHeaderCode() {
    return headerCode;
  }

  public String getImplementationCode() {
    return implementationCode;
  }

  public List<TypeMirror> getImplementationImportTypes() {
    return implementationImportTypes;
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    visitor.visit(this);
    visitor.endVisit(this);
  }

  @Override
  public NativeDeclaration copy() {
    return new NativeDeclaration(this);
  }

  public NativeDeclaration addImplementationImportType(TypeMirror type) {
    implementationImportTypes.add(type);
    return this;
  }
}
