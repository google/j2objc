/*
 * MetadataSystem.java
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

import com.strobel.compilerservices.RuntimeHelpers;
import com.strobel.core.Fences;
import com.strobel.core.VerifyArgument;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Mike Strobel
 */
public class MetadataSystem extends MetadataResolver {
    private static MetadataSystem _instance;

    private final ConcurrentHashMap<String, TypeDefinition> _types;
    private final ITypeLoader _typeLoader;

    private boolean _isEagerMethodLoadingEnabled;

    public static MetadataSystem instance() {
        if (_instance == null) {
            synchronized (MetadataSystem.class) {
                if (_instance == null) {
                    _instance = Fences.orderWrites(new MetadataSystem());
                }
            }
        }
        return _instance;
    }

    public MetadataSystem() {
        this(new ClasspathTypeLoader());
    }

    //
    // Temporarily removing this constructor to get a Java 9 compatibility fix out quickly.
    // Hopefully nobody is using it.  Will restore once ClasspathTypeLoader can be fleshed
    // out to support arbitrary paths.
    //
//    public MetadataSystem(final String classPath) {
//        this(new ClasspathTypeLoader(VerifyArgument.notNull(classPath, "classPath")));
//    }

    public MetadataSystem(final ITypeLoader typeLoader) {
        _typeLoader = VerifyArgument.notNull(typeLoader, "typeLoader");
        _types = new ConcurrentHashMap<>();
    }

    public final boolean isEagerMethodLoadingEnabled() {
        return _isEagerMethodLoadingEnabled;
    }

    public final void setEagerMethodLoadingEnabled(final boolean value) {
        _isEagerMethodLoadingEnabled = value;
    }

    public void addTypeDefinition(final TypeDefinition type) {
        VerifyArgument.notNull(type, "type");
        _types.putIfAbsent(type.getInternalName(), type);
    }

    @Override
    protected TypeDefinition resolveCore(final TypeReference type) {
        VerifyArgument.notNull(type, "type");
        return resolveType(type.getInternalName(), false);
    }

    @Override
    protected TypeReference lookupTypeCore(final String descriptor) {
        return resolveType(descriptor, true);
    }

    protected TypeDefinition resolveType(final String descriptor, final boolean mightBePrimitive) {
        VerifyArgument.notNull(descriptor, "descriptor");

        if (mightBePrimitive) {
            if (descriptor.length() == 1) {
                final int primitiveHash = descriptor.charAt(0) - 'B';

                if (primitiveHash >= 0 && primitiveHash < PRIMITIVE_TYPES_BY_DESCRIPTOR.length) {
                    final TypeDefinition primitiveType = PRIMITIVE_TYPES_BY_DESCRIPTOR[primitiveHash];

                    if (primitiveType != null) {
                        return primitiveType;
                    }
                }
            }
            else {
                final int primitiveHash = hashPrimitiveName(descriptor);

                if (primitiveHash >= 0 && primitiveHash < PRIMITIVE_TYPES_BY_NAME.length) {
                    final TypeDefinition primitiveType = PRIMITIVE_TYPES_BY_NAME[primitiveHash];

                    if (primitiveType != null && descriptor.equals(primitiveType.getName())) {
                        return primitiveType;
                    }
                }
            }
        }

        TypeDefinition cachedDefinition = _types.get(descriptor);

        if (cachedDefinition != null) {
            return cachedDefinition;
        }

        final Buffer buffer = new Buffer(0);

        if (!_typeLoader.tryLoadType(descriptor, buffer)) {
            return null;
        }

        final TypeDefinition typeDefinition = ClassFileReader.readClass(
            _isEagerMethodLoadingEnabled ? ClassFileReader.OPTIONS_DEFAULT | ClassFileReader.OPTION_PROCESS_CODE
                                         : ClassFileReader.OPTIONS_DEFAULT,
            this,
            buffer
        );

        cachedDefinition = _types.putIfAbsent(descriptor, typeDefinition);
        typeDefinition.setTypeLoader(_typeLoader);

        if (cachedDefinition != null) {
            return cachedDefinition;
        }

        return typeDefinition;
    }

    // <editor-fold defaultstate="collapsed" desc="Primitive Lookup">

    private final static TypeDefinition[] PRIMITIVE_TYPES_BY_NAME = new TypeDefinition['Z' - 'B' + 1];
    private final static TypeDefinition[] PRIMITIVE_TYPES_BY_DESCRIPTOR = new TypeDefinition[16];

    static {
        RuntimeHelpers.ensureClassInitialized(BuiltinTypes.class);

        final TypeDefinition[] allPrimitives = {
            BuiltinTypes.Boolean,
            BuiltinTypes.Byte,
            BuiltinTypes.Character,
            BuiltinTypes.Short,
            BuiltinTypes.Integer,
            BuiltinTypes.Long,
            BuiltinTypes.Float,
            BuiltinTypes.Double,
            BuiltinTypes.Void
        };

        for (final TypeDefinition t : allPrimitives) {
            PRIMITIVE_TYPES_BY_DESCRIPTOR[hashPrimitiveName(t.getName())] = t;
            PRIMITIVE_TYPES_BY_NAME[t.getInternalName().charAt(0) - 'B'] = t;
        }
    }

    private static int hashPrimitiveName(final String name) {
        if (name.length() < 3) {
            return 0;
        }
        return (name.charAt(0) + name.charAt(2)) % 16;
    }

    // </editor-fold>
}
