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

import javax.lang.model.element.VariableElement;

/**
 * A tree node used as the base class for the different kinds of patterns
 * (introduced in Java 16).
 */
public abstract class Pattern extends TreeNode {

  public Pattern() {}

  public Pattern(Pattern other) {}

  /**
   * A tree node for a binding pattern that matches a pattern with a variable
   * of any name and a type of the match candidate; an unnamed pattern. For
   * example the use of underscore _ below:
   * {@code
   * if (r instanceof R(_)) {}
   * }
   */
  public static class AnyPattern extends Pattern {

    public AnyPattern() {}

    public AnyPattern(AnyPattern other) {}

    @Override
    public Kind getKind() {
      return Kind.ANY_PATTERN;
    }

    @Override
    protected void acceptInner(TreeVisitor visitor) {
      var unused = visitor.visit(this);
      visitor.endVisit(this);
    }

    @Override
    public AnyPattern copy() {
      return new AnyPattern(this);
    }
  }

  /**
   * A binding pattern, which declares a named variable. For example, in
   * the statement {@code if (o instanceof String s) ...}, the "s" is the
   * variable declared by the binding pattern.
   */
  public static class BindingPattern extends Pattern {
    private final ChildLink<SingleVariableDeclaration> var =
        ChildLink.create(SingleVariableDeclaration.class, this);

    public BindingPattern(VariableElement element) {
      var.set(new SingleVariableDeclaration(element));
    }

    public BindingPattern(BindingPattern other) {
      super(other);
      var.copyFrom(other.getVariable());
    }

    @Override
    public Kind getKind() {
      return Kind.BINDING_PATTERN;
    }

    public SingleVariableDeclaration getVariable() {
      return var.get();
    }

    public BindingPattern setVariable(SingleVariableDeclaration e) {
      var.set(e);
      return this;
    }

    @Override
    protected void acceptInner(TreeVisitor visitor) {
      if (visitor.visit(this)) {
        var.accept(visitor);
      }
      visitor.endVisit(this);
    }

    @Override
    public BindingPattern copy() {
      return new BindingPattern(this);
    }
  }
}
