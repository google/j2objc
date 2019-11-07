/*
 * BuiltinTypes.java
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

package com.strobel.assembler.metadata;

import com.strobel.util.ContractUtils;

/**
 * @author Mike Strobel
 */
public final class BuiltinTypes {
    public final static TypeDefinition Boolean;
    public final static TypeDefinition Byte;
    public final static TypeDefinition Character;
    public final static TypeDefinition Short;
    public final static TypeDefinition Integer;
    public final static TypeDefinition Long;
    public final static TypeDefinition Float;
    public final static TypeDefinition Double;
    public final static TypeDefinition Void;
    public final static TypeDefinition Object;
    public final static TypeDefinition Bottom;
    public final static TypeDefinition Null;
    public final static TypeDefinition Class;

    static {
        Boolean = new PrimitiveType(JvmType.Boolean);
        Byte = new PrimitiveType(JvmType.Byte);
        Character = new PrimitiveType(JvmType.Character);
        Short = new PrimitiveType(JvmType.Short);
        Integer = new PrimitiveType(JvmType.Integer);
        Long = new PrimitiveType(JvmType.Long);
        Float = new PrimitiveType(JvmType.Float);
        Double = new PrimitiveType(JvmType.Double);
        Void = new PrimitiveType(JvmType.Void);
        Bottom = BottomType.INSTANCE;
        Null = NullType.INSTANCE;

        final Buffer buffer = new Buffer();
        final ITypeLoader typeLoader = new ClasspathTypeLoader();

        if (!typeLoader.tryLoadType("java/lang/Object", buffer)) {
            throw Error.couldNotLoadObjectType();
        }

        final MetadataSystem metadataSystem = MetadataSystem.instance();

        Bottom.setResolver(metadataSystem);
        Null.setResolver(metadataSystem);

        Object = ClassFileReader.readClass(metadataSystem, buffer);

        buffer.reset();

        if (!typeLoader.tryLoadType("java/lang/Class", buffer)) {
            throw Error.couldNotLoadClassType();
        }

        Class = ClassFileReader.readClass(metadataSystem, buffer);
    }

    public static TypeDefinition fromPrimitiveTypeCode(final int code) {
        switch (code) {
            case 4:
                return Boolean;
            case 8:
                return Byte;
            case 9:
                return Short;
            case 10:
                return Integer;
            case 11:
                return Long;
            case 5:
                return Character;
            case 6:
                return Float;
            case 7:
                return Double;
            default:
                throw ContractUtils.unreachable();
        }
    }
}
