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

import com.google.devtools.j2objc.ast.AnonymousClassDeclaration;
import com.google.devtools.j2objc.ast.LambdaExpression;
import com.google.devtools.j2objc.ast.TreeVisitor;
import com.google.devtools.j2objc.ast.TypeDeclaration;

import org.eclipse.jdt.core.dom.ITypeBinding;

import java.util.Stack;

/**
 * Sets the declaring class in LambdaTypeBinding.
 *
 * @author Nathan Braswell
 */
public class LambdaTypeBindingFixer extends TreeVisitor {

  private Stack<ITypeBinding> currentBinding;

  public LambdaTypeBindingFixer() {
    currentBinding = new Stack<ITypeBinding>();
  }

  @Override
  public boolean visit(TypeDeclaration node) {
    currentBinding.push(node.getTypeBinding());
    return true;
  }

  @Override
  public void endVisit(TypeDeclaration node) {
    currentBinding.pop();
  }

  @Override
  public boolean visit(AnonymousClassDeclaration node) {
    currentBinding.push(node.getTypeBinding());
    return true;
  }

  @Override
  public void endVisit(AnonymousClassDeclaration node) {
    currentBinding.pop();
  }

  @Override
  public void endVisit(LambdaExpression node) {
    if (currentBinding.size() == 0) {
      throw new AssertionError("Enclosing bindings for lambda is empty");
    }
    node.getLambdaTypeBinding().setDeclaringClass(currentBinding.lastElement());
  }
}
