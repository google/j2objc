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

package com.google.devtools.j2objc.util;

import com.google.common.base.Preconditions;
import com.google.devtools.j2objc.types.Types;
import javax.lang.model.element.Element;

/**
 * The environment used by the parser to generate compilation units.
 */
public abstract class ParserEnvironment {
  private final NameTable.Factory nameTableFactory;
  private NameTable nameTable;
  private Types types;

  protected ParserEnvironment(NameTable.Factory nameTableFactory) {
    Preconditions.checkNotNull(nameTableFactory);
    this.nameTableFactory = nameTableFactory;
  }

  public synchronized NameTable nameTable() {
    if (nameTable == null) {
      nameTable = nameTableFactory.newNameTable(this);
    }
    return nameTable;
  }

  public synchronized Types types() {
    if (types == null) {
      types = new Types(this);
    }
    return types;
  }

  /**
   * Returns the element associated with a fully-qualified name.
   * Null is returned if there is no associated element for the
   * specified name.
   */
  public abstract Element resolve(String name);

  // TODO(tball): return an instance that merges j.l.m.u.Elements and ElementUtil.
  public abstract javax.lang.model.util.Elements elementUtilities();

  // TODO(tball): return an instance that merges j.l.m.u.Types and BindingUtil.
  public abstract javax.lang.model.util.Types typeUtilities();
}
