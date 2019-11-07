/*
 * IMetadataResolver.java
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

/**
 * User: Mike Strobel
 * Date: 1/6/13
 * Time: 5:07 PM
 */
public interface IMetadataResolver {
    public final static IMetadataResolver EMPTY = new IMetadataResolver() {
        @Override
        public void pushFrame(final IResolverFrame frame) {
        }

        @Override
        public void popFrame() {
        }

        @Override
        public TypeReference lookupType(final String descriptor) {
            return null;
        }

        @Override
        public TypeDefinition resolve(final TypeReference type) {
            return type instanceof TypeDefinition ? (TypeDefinition) type
                                                  : null;
        }

        @Override
        public FieldDefinition resolve(final FieldReference field) {
            return field instanceof FieldDefinition ? (FieldDefinition) field
                                                    : null;
        }

        @Override
        public MethodDefinition resolve(final MethodReference method) {
            return method instanceof MethodDefinition ? (MethodDefinition) method
                                                      : null;
        }
    };

    public void pushFrame(final IResolverFrame frame);
    public void popFrame();

    public TypeReference lookupType(final String descriptor);

    public TypeDefinition resolve(final TypeReference type);
    public FieldDefinition resolve(final FieldReference field);
    public MethodDefinition resolve(final MethodReference method);
}
