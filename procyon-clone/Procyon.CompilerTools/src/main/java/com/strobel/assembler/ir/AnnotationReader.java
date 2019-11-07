/*
 * AnnotationReader.java
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

package com.strobel.assembler.ir;

import com.strobel.assembler.metadata.Buffer;
import com.strobel.assembler.metadata.IMetadataScope;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.assembler.metadata.annotations.*;
import com.strobel.core.ArrayUtilities;
import com.strobel.util.ContractUtils;

/**
 * @author Mike Strobel
 */
public final class AnnotationReader {
    public static CustomAnnotation read(final IMetadataScope scope, final Buffer input) {
        final int typeToken = input.readUnsignedShort();
        final int parameterCount = input.readUnsignedShort();
        
        final TypeReference annotationType = scope.lookupType(typeToken);
        final AnnotationParameter[] parameters = new AnnotationParameter[parameterCount];
        
        readParameters(parameters, scope, input, true);

        return new CustomAnnotation(annotationType, ArrayUtilities.asUnmodifiableList(parameters));
    }

    private static void readParameters(
        final AnnotationParameter[] parameters,
        final IMetadataScope scope,
        final Buffer input,
        final boolean namedParameter) {

        for (int i = 0; i < parameters.length; i++) {

            parameters[i] = new AnnotationParameter(
                namedParameter ? scope.<String>lookupConstant(input.readUnsignedShort())
                               : "value",
                readElement(scope, input)
            );
        }
    }

    public static AnnotationElement readElement(final IMetadataScope scope, final Buffer input) {
        final char tag = (char) input.readUnsignedByte();
        final AnnotationElementType elementType = AnnotationElementType.forTag(tag);

        switch (elementType) {
            case Constant: {
                Object constantValue = scope.lookupConstant(input.readUnsignedShort());

                switch (tag) {
                    case 'B':
                        constantValue = ((Number)constantValue).byteValue();
                        break;

                    case 'C':
                        constantValue = (char)((Number)constantValue).intValue();
                        break;

                    case 'S':
                        constantValue = ((Number)constantValue).shortValue();
                        break;

                    case 'Z':
                        constantValue = ((Number)constantValue).intValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
                        break;
                }

                return new ConstantAnnotationElement(constantValue);
            }

            case Enum: {
                final TypeReference enumType = scope.lookupType(input.readUnsignedShort());
                final String constantName = scope.lookupConstant(input.readUnsignedShort());
                return new EnumAnnotationElement(enumType, constantName);
            }

            case Array: {
                final AnnotationElement[] elements = new AnnotationElement[input.readUnsignedShort()];

                for (int i = 0; i < elements.length; i++) {
                    elements[i] = readElement(scope, input);
                }

                return new ArrayAnnotationElement(elements);
            }

            case Class: {
                final TypeReference type = scope.lookupType(input.readUnsignedShort());
                return new ClassAnnotationElement(type);
            }

            case Annotation: {
                final CustomAnnotation annotation = read(scope, input);
                return new AnnotationAnnotationElement(annotation);
            }
        }

        throw ContractUtils.unreachable();
    }
}
