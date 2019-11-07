/*
 * Keys.java
 *
 * Copyright (c) 2013 Mike Strobel
 *
 * This source code is based on Mono.Cecil from Jb Evain, Copyright (c) Jb Evain;
 * and ILSpy/ICSharpCode from SharpDevelop, Copyright (c) AlphaSierraPapa.
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.decompiler.languages.java.ast;

import com.strobel.assembler.metadata.*;
import com.strobel.componentmodel.Key;
import com.strobel.core.ArrayUtilities;
import com.strobel.core.ExceptionUtilities;
import com.strobel.decompiler.ast.Variable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class Keys {
    public final static Key<Variable> VARIABLE = Key.create("Variable");
    public final static Key<VariableDefinition> VARIABLE_DEFINITION = Key.create("VariableDefinition");
    public final static Key<ParameterDefinition> PARAMETER_DEFINITION = Key.create("ParameterDefinition");
    public final static Key<MemberReference> MEMBER_REFERENCE = Key.create("MemberReference");
    public final static Key<PackageReference> PACKAGE_REFERENCE = Key.create("PackageReference");
    public final static Key<FieldDefinition> FIELD_DEFINITION = Key.create("FieldDefinition");
    public final static Key<MethodDefinition> METHOD_DEFINITION = Key.create("MethodDefinition");
    public final static Key<TypeDefinition> TYPE_DEFINITION = Key.create("TypeDefinition");
    public final static Key<TypeReference> TYPE_REFERENCE = Key.create("TypeReference");
    public final static Key<TypeReference> ANONYMOUS_BASE_TYPE_REFERENCE = Key.create("AnonymousBaseTypeReference");
    public final static Key<DynamicCallSite> DYNAMIC_CALL_SITE = Key.create("DynamicCallSite");
    public final static Key<AstBuilder> AST_BUILDER = Key.create("AstBuilder");
    public final static Key<Object> CONSTANT_VALUE = Key.create("ConstantValue");

    public final static List<Key<?>> ALL_KEYS;

    static {
        final ArrayList<Key<?>> keys = new ArrayList<>();

        try {
            for (final Field field : Keys.class.getDeclaredFields()) {
                if (field.getType() == Key.class) {
                    keys.add((Key<?>) field.get(null));
                }
            }

            ALL_KEYS = ArrayUtilities.<Key<?>>asUnmodifiableList(keys.toArray(new Key[keys.size()]));
        }
        catch (final Throwable t) {
            throw ExceptionUtilities.asRuntimeException(t);
        }
    }
}
