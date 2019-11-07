/*
 * Copyright (c) 2003, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.strobel.assembler.metadata.signatures;

import com.strobel.assembler.metadata.GenericParameter;
import com.strobel.assembler.metadata.IClassSignature;
import com.strobel.assembler.metadata.IMethodSignature;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.assembler.metadata.WildcardType;

import java.util.List;

public interface MetadataFactory {
    GenericParameter makeTypeVariable(String name, FieldTypeSignature[] bounds);

    TypeReference makeParameterizedType(TypeReference declaration, TypeReference owner, TypeReference... typeArgs);

    GenericParameter findTypeVariable(String name);

    WildcardType makeWildcard(FieldTypeSignature superBound, FieldTypeSignature extendsBounds);

    TypeReference makeNamedType(String name);
    TypeReference makeArrayType(TypeReference componentType);
    TypeReference makeByte();
    TypeReference makeBoolean();
    TypeReference makeShort();
    TypeReference makeChar();
    TypeReference makeInt();
    TypeReference makeLong();
    TypeReference makeFloat();
    TypeReference makeDouble();
    TypeReference makeVoid();

    IMethodSignature makeMethodSignature(
        final TypeReference returnType,
        final List<TypeReference> parameterTypes,
        final List<GenericParameter> genericParameters,
        final List<TypeReference> thrownTypes);

    IClassSignature makeClassSignature(
        final TypeReference baseType,
        final List<TypeReference> interfaceTypes,
        final List<GenericParameter> genericParameters);
}
