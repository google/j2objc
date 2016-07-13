/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
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

package com.google.devtools.j2objc.types;

import com.google.devtools.j2objc.jdt.JdtPackageBinding;
import org.eclipse.jdt.core.dom.IBinding;

/**
 * Binding class for types created during translation.
 *
 * @author Keith Stanger
 */
public class GeneratedPackageBinding extends JdtPackageBinding {

  private final String name;

  public GeneratedPackageBinding(String name) {
    super(null);
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String[] getNameComponents() {
    return name.split("\\.");
  }

  @Override
  public boolean isUnnamed() {
    return false;
  }

  @Override
  public int getKind() {
    return IBinding.PACKAGE;
  }

  @Override
  public boolean isEqualTo(IBinding binding) {
    if (binding == this) {
      return true;
    }
    if (binding instanceof GeneratedPackageBinding) {
      return name.equals(((GeneratedPackageBinding) binding).name);
    }
    return false;
  }

  @Override
  public String getKey() {
    return name;
  }
}
