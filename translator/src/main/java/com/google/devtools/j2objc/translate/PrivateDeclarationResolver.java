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

package com.google.devtools.j2objc.translate;

import com.google.devtools.j2objc.Options;
import com.google.devtools.j2objc.ast.FieldDeclaration;
import com.google.devtools.j2objc.ast.FunctionDeclaration;
import com.google.devtools.j2objc.ast.MethodDeclaration;
import com.google.devtools.j2objc.ast.NativeDeclaration;
import com.google.devtools.j2objc.ast.TreeVisitor;

import org.eclipse.jdt.core.dom.Modifier;

/**
 * Determines which declarations should be moved out of the public header file.
 *
 * @author Keith Stanger
 */
public class PrivateDeclarationResolver extends TreeVisitor {

  @Override
  public void endVisit(FieldDeclaration node) {
    node.setHasPrivateDeclaration(
        Options.hidePrivateMembers() && Modifier.isPrivate(node.getModifiers()));
  }

  @Override
  public void endVisit(FunctionDeclaration node) {
    node.setHasPrivateDeclaration(Modifier.isPrivate(node.getModifiers()));
  }

  @Override
  public void endVisit(MethodDeclaration node) {
    node.setHasPrivateDeclaration(
        Options.hidePrivateMembers() && Modifier.isPrivate(node.getModifiers()));
  }

  @Override
  public void endVisit(NativeDeclaration node) {
    node.setHasPrivateDeclaration(
        Options.hidePrivateMembers() && Modifier.isPrivate(node.getModifiers()));
  }
}
