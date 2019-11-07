/*
 * MetadataReader.java
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

import com.strobel.assembler.ir.attributes.*;
import com.strobel.assembler.metadata.*;
import com.strobel.assembler.metadata.annotations.AnnotationElement;
import com.strobel.assembler.metadata.annotations.CustomAnnotation;
import com.strobel.core.ArrayUtilities;
import com.strobel.core.VerifyArgument;

import java.util.List;

/**
 * @author Mike Strobel
 */
public abstract class MetadataReader {
    protected MetadataReader() {
    }

    protected abstract IMetadataScope getScope();
    protected abstract MetadataParser getParser();

    public void readAttributes(final Buffer input, final SourceAttribute[] attributes) {
        for (int i = 0; i < attributes.length; i++) {
            attributes[i] = readAttribute(input);
        }
    }

    public SourceAttribute readAttribute(final Buffer buffer) {
        final int nameIndex = buffer.readUnsignedShort();
        final int length = buffer.readInt();
        final IMetadataScope scope = getScope();
        final String name = scope.lookupConstant(nameIndex);

        return readAttributeCore(name, buffer, -1, length);
    }

    /**
     * Reads a {@link SourceAttribute} from the specified buffer.
     *
     * @param name
     *     The name of the attribute to decode.
     * @param buffer
     *     A buffer containing the attribute blob.
     * @param originalOffset
     *     The offset of position 0 in the buffer relative to the start of the original class file.
     *     This is needed during lazy inflation of {@link CodeAttribute} (and possibly others). In
     *     the case of {@link CodeAttribute}, it is helpful to know exactly where each method's body
     *     begins so we can load it on demand at some point in the future.
     * @param length
     *     The length of the attribute.  Implementations should not rely on {@link Buffer#size()
     *     buffer.size()}.
     */
    protected SourceAttribute readAttributeCore(final String name, final Buffer buffer, final int originalOffset, final int length) {
        final IMetadataScope scope = getScope();

        if (length == 0) {
            return SourceAttribute.create(name);
        }

        switch (name) {
            case AttributeNames.SourceFile: {
                final int token = buffer.readUnsignedShort();
                final String sourceFile = scope.lookupConstant(token);
                return new SourceFileAttribute(sourceFile);
            }

            case AttributeNames.ConstantValue: {
                final int token = buffer.readUnsignedShort();
                final Object constantValue = scope.lookupConstant(token);
                return new ConstantValueAttribute(constantValue);
            }

            case AttributeNames.Code: {
                final int maxStack = buffer.readUnsignedShort();
                final int maxLocals = buffer.readUnsignedShort();
                final int codeLength = buffer.readInt();
                final int relativeOffset = buffer.position();
                final int codeOffset = (originalOffset >= 0) ? (originalOffset - 2 + relativeOffset) : relativeOffset;
                final byte[] code = new byte[codeLength];

                buffer.read(code, 0, codeLength);

                final int exceptionTableLength = buffer.readUnsignedShort();
                final ExceptionTableEntry[] exceptionTable = new ExceptionTableEntry[exceptionTableLength];


                for (int k = 0; k < exceptionTableLength; k++) {
                    final int startOffset = buffer.readUnsignedShort();
                    final int endOffset = buffer.readUnsignedShort();
                    final int handlerOffset = buffer.readUnsignedShort();
                    final int catchTypeToken = buffer.readUnsignedShort();
                    final TypeReference catchType;

                    if (catchTypeToken == 0) {
                        catchType = null;
                    }
                    else {
                        catchType = scope.lookupType(catchTypeToken);
                    }

                    exceptionTable[k] = new ExceptionTableEntry(
                        startOffset,
                        endOffset,
                        handlerOffset,
                        catchType
                    );
                }

                final int attributeCount = buffer.readUnsignedShort();
                final SourceAttribute[] attributes = new SourceAttribute[attributeCount];

                readAttributes(buffer, attributes);

                return new CodeAttribute(
                    length,
                    maxStack,
                    maxLocals,
                    codeOffset,
                    codeLength,
                    buffer,
                    exceptionTable,
                    attributes
                );
            }

            case AttributeNames.Exceptions: {
                final int exceptionCount = buffer.readUnsignedShort();
                final TypeReference[] exceptionTypes = new TypeReference[exceptionCount];

                for (int i = 0; i < exceptionTypes.length; i++) {
                    exceptionTypes[i] = scope.lookupType(buffer.readUnsignedShort());
                }

                return new ExceptionsAttribute(exceptionTypes);
            }

            case AttributeNames.LineNumberTable: {
                final int entryCount = buffer.readUnsignedShort();
                final LineNumberTableEntry[] entries = new LineNumberTableEntry[entryCount];

                for (int i = 0; i < entries.length; i++) {
                    entries[i] = new LineNumberTableEntry(
                        buffer.readUnsignedShort(),
                        buffer.readUnsignedShort()
                    );
                }

                return new LineNumberTableAttribute(entries);
            }

            case AttributeNames.LocalVariableTable:
            case AttributeNames.LocalVariableTypeTable: {
                final int entryCount = buffer.readUnsignedShort();
                final LocalVariableTableEntry[] entries = new LocalVariableTableEntry[entryCount];

                for (int i = 0; i < entries.length; i++) {
                    final int scopeOffset = buffer.readUnsignedShort();
                    final int scopeLength = buffer.readUnsignedShort();
                    final int nameToken = buffer.readUnsignedShort();
                    final int typeToken = buffer.readUnsignedShort();
                    final int variableIndex = buffer.readUnsignedShort();
                    final String variableName = scope.lookupConstant(nameToken);
                    final String descriptor = scope.lookupConstant(typeToken);

                    TypeReference parsedType;

                    try {
                        parsedType = getParser().parseTypeSignature(descriptor);
                    }
                    catch (final java.lang.Error | Exception ignored) {
                        parsedType = null;
                    }

                    entries[i] = new LocalVariableTableEntry(
                        variableIndex,
                        variableName,
                        parsedType != null ? parsedType : BuiltinTypes.Object,
                        descriptor,
                        scopeOffset,
                        scopeLength,
                        parsedType == null
                    );
                }

                return new LocalVariableTableAttribute(name, entries);
            }

            case AttributeNames.EnclosingMethod: {
                final int typeToken = buffer.readUnsignedShort();
                final int methodToken = buffer.readUnsignedShort();

                return new EnclosingMethodAttribute(
                    scope.lookupType(typeToken),
                    methodToken > 0 ? scope.lookupMethod(typeToken, methodToken)
                                    : null
                );
            }

            case AttributeNames.RuntimeVisibleAnnotations:
            case AttributeNames.RuntimeInvisibleAnnotations: {
                final CustomAnnotation[] annotations = new CustomAnnotation[buffer.readUnsignedShort()];

                for (int i = 0; i < annotations.length; i++) {
                    annotations[i] = AnnotationReader.read(scope, buffer);
                }

                return new AnnotationsAttribute(name, length, annotations);
            }

            case AttributeNames.RuntimeVisibleParameterAnnotations:
            case AttributeNames.RuntimeInvisibleParameterAnnotations: {
                final CustomAnnotation[][] annotations = new CustomAnnotation[buffer.readUnsignedByte()][];

                for (int i = 0; i < annotations.length; i++) {
                    final CustomAnnotation[] parameterAnnotations = new CustomAnnotation[buffer.readUnsignedShort()];

                    for (int j = 0; j < parameterAnnotations.length; j++) {
                        parameterAnnotations[j] = AnnotationReader.read(scope, buffer);
                    }

                    annotations[i] = parameterAnnotations;
                }

                return new ParameterAnnotationsAttribute(name, length, annotations);
            }

            case AttributeNames.AnnotationDefault: {
                final AnnotationElement defaultValue = AnnotationReader.readElement(scope, buffer);
                return new AnnotationDefaultAttribute(length, defaultValue);
            }

            case AttributeNames.Signature: {
                final int token = buffer.readUnsignedShort();
                final String signature = scope.lookupConstant(token);
                return new SignatureAttribute(signature);
            }

            case AttributeNames.BootstrapMethods: {
                final BootstrapMethodsTableEntry[] methods = new BootstrapMethodsTableEntry[buffer.readUnsignedShort()];

                for (int i = 0; i < methods.length; i++) {
                    final MethodHandle bootstrapMethodHandle = scope.lookupMethodHandle(buffer.readUnsignedShort());
                    final MethodReference bootstrapMethod = bootstrapMethodHandle.getMethod();
                    final Object[] arguments = new Object[buffer.readUnsignedShort()];
                    final List<ParameterDefinition> parameters = bootstrapMethod.getParameters();

                    final int methodParameters = parameters.size();
                    
                    if (methodParameters != arguments.length + 3) {
                        final MethodDefinition resolved = bootstrapMethod.resolve();

                        final int varArgsAdjustment;
                        
                        if(resolved == null || !resolved.isVarArgs()) {
                            varArgsAdjustment = 0;
                        }
                        else {
                            varArgsAdjustment = 1;
                        }

                        final int varArgsAdjustedMethodParameters = methodParameters - varArgsAdjustment;

                        if (varArgsAdjustedMethodParameters > arguments.length + 3) {
                       		throw Error.invalidBootstrapMethodEntry(bootstrapMethod, varArgsAdjustedMethodParameters, arguments.length);
                        }
                    }

                    for (int j = 0; j < arguments.length; j++) {
                        final TypeReference parameterType;
                        final int token = buffer.readUnsignedShort();
                        final int parameterIndex = j + 3;

                        if (parameterIndex < methodParameters) {
                            parameterType = parameters.get(parameterIndex).getParameterType();
                        }
                        else {
                            parameterType = BuiltinTypes.Object;
                        }

                        switch (parameterType.getInternalName()) {
                            case "java/lang/invoke/MethodHandle":
                                arguments[j] = scope.lookupMethodHandle(token);
                                continue;

                            case "java/lang/invoke/MethodType":
                                arguments[j] = scope.lookupMethodType(token);
                                continue;

                            default:
                                arguments[j] = scope.lookup(token);
                                continue;
                        }
                    }

                    methods[i] = new BootstrapMethodsTableEntry(bootstrapMethodHandle, arguments);
                }

                return new BootstrapMethodsAttribute(methods);
            }

            case AttributeNames.MethodParameters: {
                final int methodParameterCount = buffer.readUnsignedByte();
                final int computedCount = (length - 1) / 4;
                final MethodParameterEntry[] entries = new MethodParameterEntry[methodParameterCount];

                for (int i = 0; i < entries.length; i++) {
                    final int nameIndex;
                    final int flags;

                    if (i < computedCount) {
                        nameIndex = buffer.readUnsignedShort();
                        flags = buffer.readUnsignedShort();
                    }
                    else {
                        nameIndex = 0;
                        flags = 0;
                    }

                    entries[i] = new MethodParameterEntry(
                        nameIndex != 0 ? getScope().<String>lookupConstant(nameIndex) : null,
                        flags
                    );
                }

                return new MethodParametersAttribute(ArrayUtilities.asUnmodifiableList(entries));
            }

            default: {
                final byte[] blob = new byte[length];
                final int offset = buffer.position();
                buffer.read(blob, 0, blob.length);
                return new BlobAttribute(name, blob, offset);
            }
        }
    }

    protected void inflateAttributes(final SourceAttribute[] attributes) {
        VerifyArgument.noNullElements(attributes, "attributes");

        if (attributes.length == 0) {
            return;
        }

        Buffer buffer = null;

        for (int i = 0; i < attributes.length; i++) {
            final SourceAttribute attribute = attributes[i];

            if (attribute instanceof BlobAttribute) {
                if (buffer == null) {
                    buffer = new Buffer(attribute.getLength());
                }

                attributes[i] = inflateAttribute(buffer, attribute);
            }
        }
    }

    protected final SourceAttribute inflateAttribute(final SourceAttribute attribute) {
        return inflateAttribute(new Buffer(0), attribute);
    }

    protected final SourceAttribute inflateAttribute(final Buffer buffer, final SourceAttribute attribute) {
        if (attribute instanceof BlobAttribute) {
            buffer.reset(attribute.getLength());

            final BlobAttribute blobAttribute = (BlobAttribute) attribute;

            System.arraycopy(
                blobAttribute.getData(),
                0,
                buffer.array(),
                0,
                attribute.getLength()
            );

            return readAttributeCore(
                attribute.getName(),
                buffer,
                blobAttribute.getDataOffset(),
                attribute.getLength()
            );
        }

        return attribute;
    }

    protected void inflateAttributes(final List<SourceAttribute> attributes) {
        VerifyArgument.noNullElements(attributes, "attributes");

        if (attributes.isEmpty()) {
            return;
        }

        Buffer buffer = null;

        for (int i = 0; i < attributes.size(); i++) {
            final SourceAttribute attribute = attributes.get(i);

            if (attribute instanceof BlobAttribute) {
                if (buffer == null) {
                    buffer = new Buffer(attribute.getLength());
                }
                else if (buffer.size() < attribute.getLength()) {
                    buffer.reset(attribute.getLength());
                }
                else {
                    buffer.position(0);
                }

                final BlobAttribute blobAttribute = (BlobAttribute) attribute;

                System.arraycopy(
                    blobAttribute.getData(),
                    0,
                    buffer.array(),
                    0,
                    attribute.getLength()
                );

                attributes.set(
                    i,
                    readAttributeCore(
                        attribute.getName(),
                        buffer,
                        blobAttribute.getDataOffset(),
                        attribute.getLength()
                    )
                );
            }
        }
    }
}
