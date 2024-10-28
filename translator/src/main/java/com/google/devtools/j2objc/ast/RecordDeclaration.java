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
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Node type for a record declaration.
 */
public class RecordDeclaration extends AbstractTypeDeclaration {

  private List<RecordComponent> recordComponents = new ArrayList<>();

  public RecordDeclaration() {}

  public RecordDeclaration(RecordDeclaration other) {
    super(other);
  }

  public RecordDeclaration(TypeElement typeElement) {
    super(typeElement);
  }

  @Override
  public Kind getKind() {
    return Kind.RECORD_DECLARATION;
  }

  @Override
  public RecordDeclaration copy() {
    return new RecordDeclaration(this);
  }

  public List<RecordComponent> getRecordComponents() {
    return recordComponents;
  }

  public void addRecordComponent(VariableElement component, MethodDeclaration accessor) {
    recordComponents.add(new RecordComponent(component, accessor));
  }

  @Override
  protected void acceptInner(TreeVisitor visitor) {
    if (visitor.visit(this)) {
      javadoc.accept(visitor);
      annotations.accept(visitor);
      name.accept(visitor);
      bodyDeclarations.accept(visitor);
      classInitStatements.accept(visitor);
    }
    visitor.endVisit(this);
  }

  /** A record component is an extended VariableElement which adds an accessor method. */
  public static class RecordComponent {
    private final VariableElement var;
    private final MethodDeclaration accessor;

    public RecordComponent(VariableElement component, MethodDeclaration accessor) {
      this.var = component;
      this.accessor = accessor;
    }

    public VariableElement getElement() {
      return var;
    }

    public MethodDeclaration getAccessor() {
      return accessor;
    }

    @Override
    public String toString() {
      return var.getSimpleName().toString();
    }
  }
}
