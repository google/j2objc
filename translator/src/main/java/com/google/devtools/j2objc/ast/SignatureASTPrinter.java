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

import java.util.Comparator;
import java.util.List;

/**
 * An AST printer which is identical to DebugASTPrinter except MethodDeclaration bodies are not
 * printed.
 *
 * @author Manvith Narahari
 */
public class SignatureASTPrinter extends DebugASTPrinter {

  public static String toString(TreeNode node) {
    SignatureASTPrinter printer = new SignatureASTPrinter();
    node.accept(printer);
    return printer.sb.toString();
  }

  @Override
  protected void sort(List<BodyDeclaration> lst) {
    lst.sort(Comparator.comparing(BodyDeclaration::toString));
  }

  @Override
  protected void printMethodBody(MethodDeclaration node) {
    sb.println(';');
  }
}
