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

import com.google.devtools.j2objc.jdt.BindingConverter;
import com.google.devtools.j2objc.util.ParserEnvironment;
import com.google.devtools.j2objc.util.TypeUtil;
import javax.lang.model.element.TypeElement;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Types is a singleton service class for type-related operations.
 *
 * @author Tom Ball
 */
public class Types {

  private final ParserEnvironment env;

  // Commonly used java types.
  private final ITypeBinding javaObjectType;

  // Non-standard naming pattern is used, since in this case it's more readable.
  private final ITypeBinding NSObject;

  // Special IOS types.
  private final ITypeBinding idType;

  public Types(ParserEnvironment env) {
    this.env = env;

    // Find core java types.
    javaObjectType = resolveWellKnownType("java.lang.Object");

    // Create core IOS types.
    NSObject = BindingConverter.unwrapTypeElement(TypeUtil.NS_OBJECT);
    idType = BindingConverter.unwrapTypeMirrorIntoTypeBinding(TypeUtil.ID_TYPE);
  }

  private ITypeBinding resolveWellKnownType(String name) {
    return BindingConverter.unwrapTypeElement((TypeElement) env.resolve(name));
  }

  public boolean isIdType(ITypeBinding type) {
    return type == idType || type == NSObject || type == javaObjectType
        || (type instanceof NativeType.Binding && type.getName().equals("id"));
  }
}
