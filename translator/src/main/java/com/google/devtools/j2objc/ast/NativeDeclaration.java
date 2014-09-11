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

/**
 * A native declaration. Contains string contents without any structural context
 * other than whether the content is for the header or implementation.
 */
public class NativeDeclaration extends BodyDeclaration {

  private String headerCode = null;
  private String implementationCode = null;

  public NativeDeclaration(NativeDeclaration other) {
    headerCode = other.getHeaderCode();
    implementationCode = other.getImplementationCode();
  }

  /**
   * Creates a new NativeDeclaration. For proper spacing, the convention is
   * that the code snippets end with a newline.
   *
   * @param headerCode Code to be printed in the type interface.
   * @param implementationCode Code to be printed in the type implementation.
   */
  public NativeDeclaration(String headerCode, String implementationCode) {
    this.headerCode = headerCode;
    this.implementationCode = implementationCode;
  }

  @Override
  public Kind getKind() {
    return Kind.NATIVE_DECLARATION;
  }

  public String getHeaderCode() {
    return headerCode;
  }

  public String getImplementationCode() {
    return implementationCode;
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
}
