/*
 * ClassFileReader.java
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

import com.strobel.assembler.Collection;
import com.strobel.assembler.ir.ConstantPool;
import com.strobel.assembler.ir.MetadataReader;
import com.strobel.assembler.ir.attributes.*;
import com.strobel.assembler.metadata.annotations.CustomAnnotation;
import com.strobel.core.ArrayUtilities;
import com.strobel.core.Comparer;
import com.strobel.core.ExceptionUtilities;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.util.EmptyArrayCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Mike Strobel
 */
@SuppressWarnings({ "ConstantConditions", "PointlessBitwiseExpression", "UnnecessaryContinue" })
public final class ClassFileReader extends MetadataReader {
    public final static int OPTION_PROCESS_ANNOTATIONS = 1 << 0;
    public final static int OPTION_PROCESS_CODE = 1 << 1;

    public final static int OPTIONS_DEFAULT = OPTION_PROCESS_ANNOTATIONS;

    final static long MAGIC = 0xCAFEBABEL;

    private final int _options;
    private final IMetadataResolver _resolver;
    private final Buffer _buffer;
    private final ConstantPool _constantPool;
    private final ConstantPool.TypeInfoEntry _baseClassEntry;
    private final ConstantPool.TypeInfoEntry[] _interfaceEntries;
    private final List<FieldInfo> _fields;
    private final List<MethodInfo> _methods;
    private final List<SourceAttribute> _attributes;
    private final String _internalName;

    private final TypeDefinition _typeDefinition;
    private final MetadataParser _parser;
    private final ResolverFrame _resolverFrame;
    private final Scope _scope;

    private ClassFileReader(
        final int options,
        final IMetadataResolver resolver,
        final int majorVersion,
        final int minorVersion,
        final Buffer buffer,
        final ConstantPool constantPool,
        final int accessFlags,
        final ConstantPool.TypeInfoEntry thisClassEntry,
        final ConstantPool.TypeInfoEntry baseClassEntry,
        final ConstantPool.TypeInfoEntry[] interfaceEntries) {

        super();

        _options = options;
        _resolver = resolver;
        _resolverFrame = new ResolverFrame();
        _internalName = thisClassEntry.getName();
        _buffer = buffer;
        _constantPool = constantPool;
        _baseClassEntry = baseClassEntry;
        _interfaceEntries = VerifyArgument.notNull(interfaceEntries, "interfaceEntries");
        _fields = new ArrayList<>();
        _methods = new ArrayList<>();

        _typeDefinition = new TypeDefinition();
        _typeDefinition.setResolver(_resolver);
        _typeDefinition.setFlags(accessFlags);
        _typeDefinition.setCompilerVersion(majorVersion, minorVersion);

        final int delimiter = _internalName.lastIndexOf('/');

        if (delimiter < 0) {
            _typeDefinition.setPackageName(StringUtilities.EMPTY);
            _typeDefinition.setName(_internalName);
        }
        else {
            _typeDefinition.setPackageName(_internalName.substring(0, delimiter).replace('/', '.'));
            _typeDefinition.setName(_internalName.substring(delimiter + 1));
        }

        _attributes = _typeDefinition.getSourceAttributesInternal();

        final int delimiterIndex = _internalName.lastIndexOf('/');

        if (delimiterIndex < 0) {
            _typeDefinition.setName(_internalName);
        }
        else {
            _typeDefinition.setPackageName(_internalName.substring(0, delimiterIndex).replace('/', '.'));
            _typeDefinition.setName(_internalName.substring(delimiterIndex + 1));
        }

        _resolverFrame.addType(_typeDefinition);
        _parser = new MetadataParser(_typeDefinition);
        _scope = new Scope(_parser, _typeDefinition, constantPool);

        _constantPool.freezeIfUnfrozen();
        _typeDefinition.setConstantPool(_constantPool);
    }

    protected boolean shouldProcessAnnotations() {
        return (_options & OPTION_PROCESS_ANNOTATIONS) == OPTION_PROCESS_ANNOTATIONS;
    }

    protected boolean shouldProcessCode() {
        return (_options & OPTION_PROCESS_CODE) == OPTION_PROCESS_CODE;
    }

    @Override
    protected IMetadataScope getScope() {
        return _scope;
    }

    @Override
    public MetadataParser getParser() {
        return _parser;
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
    @Override
    protected SourceAttribute readAttributeCore(final String name, final Buffer buffer, final int originalOffset, final int length) {
        VerifyArgument.notNull(name, "name");
        VerifyArgument.notNull(buffer, "buffer");
        VerifyArgument.isNonNegative(length, "length");

        switch (name) {
            case AttributeNames.Code: {
                final int maxStack = buffer.readUnsignedShort();
                final int maxLocals = buffer.readUnsignedShort();
                final int codeLength = buffer.readInt();
                final int codeOffset = buffer.position();
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
                        catchType = _scope.lookupType(catchTypeToken);
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

                if (shouldProcessCode()) {
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
                else {
                    return new CodeAttribute(
                        length,
                        originalOffset + codeOffset,
                        codeLength,
                        maxStack,
                        maxLocals,
                        exceptionTable,
                        attributes
                    );
                }
            }

            case AttributeNames.InnerClasses: {
                final InnerClassEntry[] entries = new InnerClassEntry[buffer.readUnsignedShort()];

                for (int i = 0; i < entries.length; i++) {
                    final int innerClassIndex = buffer.readUnsignedShort();
                    final int outerClassIndex = buffer.readUnsignedShort();
                    final int shortNameIndex = buffer.readUnsignedShort();
                    final int accessFlags = buffer.readUnsignedShort();

                    final ConstantPool.TypeInfoEntry innerClass = _constantPool.getEntry(innerClassIndex);
                    final ConstantPool.TypeInfoEntry outerClass;

                    if (outerClassIndex != 0) {
                        outerClass = _constantPool.getEntry(outerClassIndex);
                    }
                    else {
                        outerClass = null;
                    }

                    entries[i] = new InnerClassEntry(
                        innerClass.getName(),
                        outerClass != null ? outerClass.getName() : null,
                        shortNameIndex != 0 ? _constantPool.<String>lookupConstant(shortNameIndex) : null,
                        accessFlags
                    );
                }

                return new InnerClassesAttribute(length, ArrayUtilities.asUnmodifiableList(entries));
            }
        }

        return super.readAttributeCore(name, buffer, originalOffset, length);
    }

    @SuppressWarnings("ConstantConditions")
    private void readAttributesPhaseOne(final Buffer buffer, final SourceAttribute[] attributes) {
        for (int i = 0; i < attributes.length; i++) {
            final int nameIndex = buffer.readUnsignedShort();
            final int length = buffer.readInt();
            final IMetadataScope scope = getScope();
            final String name = scope.lookupConstant(nameIndex);

            switch (name) {
                case AttributeNames.SourceFile: {
                    final int token = buffer.readUnsignedShort();
                    final String sourceFile = scope.lookupConstant(token);
                    attributes[i] = new SourceFileAttribute(sourceFile);
                    continue;
                }

                case AttributeNames.ConstantValue: {
                    final int token = buffer.readUnsignedShort();
                    final Object constantValue = scope.lookupConstant(token);
                    attributes[i] = new ConstantValueAttribute(constantValue);
                    continue;
                }

                case AttributeNames.LineNumberTable: {
                    final int entryCount = buffer.readUnsignedShort();
                    final LineNumberTableEntry[] entries = new LineNumberTableEntry[entryCount];

                    for (int j = 0; j < entries.length; j++) {
                        entries[j] = new LineNumberTableEntry(
                            buffer.readUnsignedShort(),
                            buffer.readUnsignedShort()
                        );
                    }

                    attributes[i] = new LineNumberTableAttribute(entries);
                    continue;
                }

                case AttributeNames.Signature: {
                    final int token = buffer.readUnsignedShort();
                    final String signature = scope.lookupConstant(token);
                    attributes[i] = new SignatureAttribute(signature);
                    continue;
                }

                case AttributeNames.MethodParameters: {
                    attributes[i] = readAttributeCore(name, buffer, buffer.position(), length);
                    continue;
                }

                case AttributeNames.InnerClasses: {
                    attributes[i] = readAttributeCore(name, buffer, buffer.position(), length);
                    continue;
                }

                default: {
                    final int offset = buffer.position();
                    final byte[] blob = new byte[length];
                    buffer.read(blob, 0, blob.length);
                    attributes[i] = new BlobAttribute(name, blob, offset);
                    continue;
                }
            }
        }
    }

    public static TypeDefinition readClass(final IMetadataResolver resolver, final Buffer b) {
        return readClass(OPTIONS_DEFAULT, resolver, b);
    }

    public static TypeDefinition readClass(final int options, final IMetadataResolver resolver, final Buffer b) {
        final long magic = b.readInt() & 0xFFFFFFFFL;

        if (magic != MAGIC) {
            throw new IllegalStateException("Wrong magic number: " + magic);
        }

        final int minorVersion = b.readUnsignedShort();
        final int majorVersion = b.readUnsignedShort();

        final ConstantPool constantPool = ConstantPool.read(b);

        final int accessFlags = b.readUnsignedShort();

        final ConstantPool.TypeInfoEntry thisClass = (ConstantPool.TypeInfoEntry) constantPool.get(b.readUnsignedShort(), ConstantPool.Tag.TypeInfo);
        final ConstantPool.TypeInfoEntry baseClass;

        final int baseClassToken = b.readUnsignedShort();

        if (baseClassToken == 0) {
            baseClass = null;
        }
        else {
            baseClass = constantPool.getEntry(baseClassToken);
        }

        final ConstantPool.TypeInfoEntry interfaces[] = new ConstantPool.TypeInfoEntry[b.readUnsignedShort()];

        for (int i = 0; i < interfaces.length; i++) {
            interfaces[i] = (ConstantPool.TypeInfoEntry) constantPool.get(b.readUnsignedShort(), ConstantPool.Tag.TypeInfo);
        }

        return new ClassFileReader(
            options,
            resolver,
            majorVersion,
            minorVersion,
            b,
            constantPool,
            accessFlags,
            thisClass,
            baseClass,
            interfaces
        ).readClass();
    }

    // <editor-fold defaultstate="collapsed" desc="ClassReader Implementation">

    final TypeDefinition readClass() {
        _parser.pushGenericContext(_typeDefinition);

        try {
            _resolver.pushFrame(_resolverFrame);

            try {
                populateMemberInfo();

                SourceAttribute enclosingMethod = SourceAttribute.find(AttributeNames.EnclosingMethod, _attributes);

                final MethodReference declaringMethod;

                //noinspection UnusedDeclaration
                try /*(final AutoCloseable ignored = _parser.suppressTypeResolution())*/ {
                    if (enclosingMethod instanceof BlobAttribute) {
                        enclosingMethod = inflateAttribute(enclosingMethod);
                    }

                    if (enclosingMethod instanceof EnclosingMethodAttribute) {
                        MethodReference method = ((EnclosingMethodAttribute) enclosingMethod).getEnclosingMethod();

                        if (method != null) {
                            final MethodDefinition resolvedMethod = method.resolve();

                            if (resolvedMethod != null) {
                                method = resolvedMethod;

                                final AnonymousLocalTypeCollection enclosedTypes = resolvedMethod.getDeclaredTypesInternal();

                                if (!enclosedTypes.contains(_typeDefinition)) {
                                    enclosedTypes.add(_typeDefinition);
                                }
                            }

                            _typeDefinition.setDeclaringMethod(method);
                        }

                        declaringMethod = method;
                    }
                    else {
                        declaringMethod = null;
                    }
                }
                catch (final Exception e) {
                    throw ExceptionUtilities.asRuntimeException(e);
                }

                if (declaringMethod != null) {
                    _parser.popGenericContext();
                    _parser.pushGenericContext(declaringMethod);
                    _parser.pushGenericContext(_typeDefinition);
                }

                try {
                    populateDeclaringType();
                    populateBaseTypes();
                    visitAttributes();
                    visitFields();
                    defineMethods();
                    populateNamedInnerTypes();
                    populateAnonymousInnerTypes();
                    checkEnclosingMethodAttributes();
                }
                finally {
                    if (declaringMethod != null) {
                        _parser.popGenericContext();
                    }
                }
            }
            finally {
                _resolver.popFrame();
            }

            return _typeDefinition;
        }
        finally {
            _parser.popGenericContext();
        }
    }

    private void checkEnclosingMethodAttributes() {
        final InnerClassesAttribute innerClasses = SourceAttribute.find(AttributeNames.InnerClasses, _attributes);

        if (innerClasses == null) {
            return;
        }

        for (final InnerClassEntry entry : innerClasses.getEntries()) {
            final String outerClassName = entry.getOuterClassName();
            final String innerClassName = entry.getInnerClassName();

            if (outerClassName != null) {
                continue;
            }

            if (!StringUtilities.startsWith(innerClassName, _internalName + "$")) {
                continue;
            }

            final TypeReference innerType = _parser.parseTypeDescriptor(innerClassName);
            final TypeDefinition resolvedInnerType = innerType.resolve();

            if (resolvedInnerType != null && resolvedInnerType.getDeclaringMethod() == null) {
                final EnclosingMethodAttribute enclosingMethodAttribute;

                final SourceAttribute rawEnclosingMethodAttribute = SourceAttribute.find(
                    AttributeNames.EnclosingMethod,
                    resolvedInnerType.getSourceAttributes()
                );

                if (rawEnclosingMethodAttribute instanceof EnclosingMethodAttribute) {
                    enclosingMethodAttribute = (EnclosingMethodAttribute) rawEnclosingMethodAttribute;
                }
                else {
                    enclosingMethodAttribute = null;
                }

                MethodReference method;

                if (enclosingMethodAttribute != null &&
                    (method = enclosingMethodAttribute.getEnclosingMethod()) != null) {

                    final MethodDefinition resolvedMethod = method.resolve();

                    if (resolvedMethod != null) {
                        method = resolvedMethod;

                        final AnonymousLocalTypeCollection enclosedTypes = resolvedMethod.getDeclaredTypesInternal();

                        if (!enclosedTypes.contains(_typeDefinition)) {
                            enclosedTypes.add(_typeDefinition);
                        }
                    }

                    resolvedInnerType.setDeclaringMethod(method);
                }
            }
        }
    }

    private void populateMemberInfo() {
        final int fieldCount = _buffer.readUnsignedShort();

        for (int i = 0; i < fieldCount; i++) {
            final int accessFlags = _buffer.readUnsignedShort();

            final String name = _constantPool.lookupUtf8Constant(_buffer.readUnsignedShort());
            final String descriptor = _constantPool.lookupUtf8Constant(_buffer.readUnsignedShort());

            final SourceAttribute[] attributes;
            final int attributeCount = _buffer.readUnsignedShort();

            if (attributeCount > 0) {
                attributes = new SourceAttribute[attributeCount];
                readAttributesPhaseOne(_buffer, attributes);
            }
            else {
                attributes = EmptyArrayCache.fromElementType(SourceAttribute.class);
            }

            final FieldInfo field = new FieldInfo(accessFlags, name, descriptor, attributes);

            _fields.add(field);
        }

        final int methodCount = _buffer.readUnsignedShort();

        for (int i = 0; i < methodCount; i++) {
            final int accessFlags = _buffer.readUnsignedShort();

            final String name = _constantPool.lookupUtf8Constant(_buffer.readUnsignedShort());
            final String descriptor = _constantPool.lookupUtf8Constant(_buffer.readUnsignedShort());

            final SourceAttribute[] attributes;
            final int attributeCount = _buffer.readUnsignedShort();

            if (attributeCount > 0) {
                attributes = new SourceAttribute[attributeCount];
                readAttributesPhaseOne(_buffer, attributes);
            }
            else {
                attributes = EmptyArrayCache.fromElementType(SourceAttribute.class);
            }

            final MethodInfo method = new MethodInfo(accessFlags, name, descriptor, attributes);

            _methods.add(method);
        }

        final int typeAttributeCount = _buffer.readUnsignedShort();

        if (typeAttributeCount > 0) {
            final SourceAttribute[] typeAttributes = new SourceAttribute[typeAttributeCount];

            readAttributesPhaseOne(_buffer, typeAttributes);

            Collections.addAll(_attributes, typeAttributes);
        }
    }

    private void populateDeclaringType() {
        final InnerClassesAttribute innerClasses = SourceAttribute.find(AttributeNames.InnerClasses, _attributes);

        if (innerClasses == null) {
            return;
        }

        for (final InnerClassEntry entry : innerClasses.getEntries()) {
            final String innerClassName = entry.getInnerClassName();
            final String shortName = entry.getShortName();

            String outerClassName = entry.getOuterClassName();

            if (Comparer.equals(innerClassName, _internalName)) {
                final TypeReference outerType;
                final TypeDefinition resolvedOuterType;

                if (outerClassName == null) {
                    final int delimiterIndex = innerClassName.lastIndexOf('$');

                    if (delimiterIndex >= 0) {
                        outerClassName = innerClassName.substring(0, delimiterIndex);
                    }
                    else {
                        continue;
                    }
                }

                if (StringUtilities.isNullOrEmpty(shortName)) {
                    _typeDefinition.setFlags(_typeDefinition.getFlags() | Flags.ANONYMOUS);
                }
                else {
                    _typeDefinition.setSimpleName(shortName);
                }

                _typeDefinition.setFlags(
                    (_typeDefinition.getFlags() & ~Flags.AccessFlags) | entry.getAccessFlags()
                );

                outerType = _parser.parseTypeDescriptor(outerClassName);
                resolvedOuterType = outerType.resolve();

                if (resolvedOuterType != null) {
                    if (_typeDefinition.getDeclaringType() == null) {
                        _typeDefinition.setDeclaringType(resolvedOuterType);

                        final Collection<TypeDefinition> declaredTypes = resolvedOuterType.getDeclaredTypesInternal();

                        if (!declaredTypes.contains(_typeDefinition)) {
                            declaredTypes.add(_typeDefinition);
                        }
                    }
                }
                else if (_typeDefinition.getDeclaringType() == null) {
                    _typeDefinition.setDeclaringType(outerType);
                }

                return;
            }
        }
    }

    private void populateBaseTypes() {
        final SignatureAttribute signature = SourceAttribute.find(AttributeNames.Signature, _attributes);
        final String[] interfaceNames = new String[_interfaceEntries.length];

        for (int i = 0; i < _interfaceEntries.length; i++) {
            interfaceNames[i] = _interfaceEntries[i].getName();
        }

        final TypeReference baseType;
        final Collection<TypeReference> explicitInterfaces = _typeDefinition.getExplicitInterfacesInternal();
        final String genericSignature = signature != null ? signature.getSignature() : null;

        if (StringUtilities.isNullOrEmpty(genericSignature)) {
            baseType = _baseClassEntry != null ? _parser.parseTypeDescriptor(_baseClassEntry.getName()) : null;

            for (final String interfaceName : interfaceNames) {
                explicitInterfaces.add(_parser.parseTypeDescriptor(interfaceName));
            }
        }
        else {
            final IClassSignature classSignature = _parser.parseClassSignature(genericSignature);

            baseType = classSignature.getBaseType();
            explicitInterfaces.addAll(classSignature.getExplicitInterfaces());
            _typeDefinition.getGenericParametersInternal().addAll(classSignature.getGenericParameters());
        }

        _typeDefinition.setBaseType(baseType);
    }

    private void populateNamedInnerTypes() {
        final InnerClassesAttribute innerClasses = SourceAttribute.find(AttributeNames.InnerClasses, _attributes);

        if (innerClasses == null) {
            return;
        }

        final Collection<TypeDefinition> declaredTypes = _typeDefinition.getDeclaredTypesInternal();

        for (final InnerClassEntry entry : innerClasses.getEntries()) {
            final String outerClassName = entry.getOuterClassName();

            if (outerClassName == null) {
                continue;
            }

            final String innerClassName = entry.getInnerClassName();

            if (Comparer.equals(_internalName, innerClassName)) {
                continue;
            }

            final TypeReference innerType = _parser.parseTypeDescriptor(innerClassName);
            final TypeDefinition resolvedInnerType = innerType.resolve();

            if (resolvedInnerType != null &&
                Comparer.equals(_internalName, outerClassName) &&
                !declaredTypes.contains(resolvedInnerType)) {

                declaredTypes.add(resolvedInnerType);
                resolvedInnerType.setFlags(resolvedInnerType.getFlags() | entry.getAccessFlags());
            }
        }
    }

    private void populateAnonymousInnerTypes() {
        final InnerClassesAttribute innerClasses = SourceAttribute.find(AttributeNames.InnerClasses, _attributes);

        if (innerClasses == null) {
            return;
        }

        final Collection<TypeDefinition> declaredTypes = _typeDefinition.getDeclaredTypesInternal();

        for (final InnerClassEntry entry : innerClasses.getEntries()) {
            final String simpleName = entry.getShortName();

            if (!StringUtilities.isNullOrEmpty(simpleName)) {
                continue;
            }

            final String outerClassName = entry.getOuterClassName();
            final String innerClassName = entry.getInnerClassName();

            if (outerClassName == null || Comparer.equals(innerClassName, _internalName)) {
                continue;
            }

            final TypeReference innerType = _parser.parseTypeDescriptor(innerClassName);
            final TypeDefinition resolvedInnerType = innerType.resolve();

            if (resolvedInnerType instanceof TypeDefinition &&
                Comparer.equals(_internalName, outerClassName) &&
                !declaredTypes.contains(resolvedInnerType)) {

                declaredTypes.add(resolvedInnerType);
            }
        }

        final TypeReference self = _parser.getResolver().lookupType(_internalName);

        if (self != null && self.isNested()) {
            return;
        }

        for (final InnerClassEntry entry : innerClasses.getEntries()) {
            final String outerClassName = entry.getOuterClassName();

            if (outerClassName != null) {
                continue;
            }

            final String innerClassName = entry.getInnerClassName();

            if (Comparer.equals(innerClassName, _internalName)) {
                continue;
            }

            final TypeReference innerType = _parser.parseTypeDescriptor(innerClassName);
            final TypeDefinition resolvedInnerType = innerType.resolve();

            if (resolvedInnerType != null &&
                Comparer.equals(_internalName, outerClassName) &&
                !declaredTypes.contains(resolvedInnerType)) {

                declaredTypes.add(resolvedInnerType);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void visitFields() {
        final Collection<FieldDefinition> declaredFields = _typeDefinition.getDeclaredFieldsInternal();

        for (final FieldInfo field : _fields) {
            final TypeReference fieldType;
            final SignatureAttribute signature = SourceAttribute.find(AttributeNames.Signature, field.attributes);

            fieldType = tryParseTypeSignature(
                signature != null ? signature.getSignature() : null,
                field.descriptor
            );

            final FieldDefinition fieldDefinition = new FieldDefinition(fieldType);

            fieldDefinition.setDeclaringType(_typeDefinition);
            fieldDefinition.setFlags(Flags.fromStandardFlags(field.accessFlags, Flags.Kind.Field));
            fieldDefinition.setName(field.name);

            declaredFields.add(fieldDefinition);

            inflateAttributes(field.attributes);

            final ConstantValueAttribute constantValueAttribute = SourceAttribute.find(AttributeNames.ConstantValue, field.attributes);

            if (constantValueAttribute != null) {
                final Object constantValue = constantValueAttribute.getValue();

                if (constantValue instanceof Number) {
                    final Number number = (Number) constantValue;
                    final JvmType jvmType = fieldDefinition.getFieldType().getSimpleType();

                    switch (jvmType) {
                        case Boolean:
                            fieldDefinition.setConstantValue(number.longValue() != 0L);
                            break;
                        case Byte:
                            fieldDefinition.setConstantValue(number.byteValue());
                            break;
                        case Character:
                            fieldDefinition.setConstantValue((char) number.longValue());
                            break;
                        case Short:
                            fieldDefinition.setConstantValue(number.shortValue());
                            break;
                        case Integer:
                            fieldDefinition.setConstantValue(number.intValue());
                            break;
                        case Long:
                            fieldDefinition.setConstantValue(number.longValue());
                            break;
                        case Float:
                            fieldDefinition.setConstantValue(number.floatValue());
                            break;
                        case Double:
                            fieldDefinition.setConstantValue(number.doubleValue());
                            break;
                        default:
                            fieldDefinition.setConstantValue(constantValue);
                            break;
                    }
                }
                else {
                    fieldDefinition.setConstantValue(constantValue);
                }
            }

            if (SourceAttribute.find(AttributeNames.Synthetic, field.attributes) != null) {
                fieldDefinition.setFlags(fieldDefinition.getFlags() | Flags.SYNTHETIC);
            }

            if (SourceAttribute.find(AttributeNames.Deprecated, field.attributes) != null) {
                fieldDefinition.setFlags(fieldDefinition.getFlags() | Flags.DEPRECATED);
            }

            for (final SourceAttribute attribute : field.attributes) {
                fieldDefinition.getSourceAttributesInternal().add(attribute);
            }

            if (shouldProcessAnnotations()) {
                final Collection<CustomAnnotation> annotations = fieldDefinition.getAnnotationsInternal();

                final AnnotationsAttribute visibleAnnotations = SourceAttribute.find(
                    AttributeNames.RuntimeVisibleAnnotations,
                    field.attributes
                );

                final AnnotationsAttribute invisibleAnnotations = SourceAttribute.find(
                    AttributeNames.RuntimeInvisibleAnnotations,
                    field.attributes
                );

                if (visibleAnnotations != null) {
                    Collections.addAll(annotations, visibleAnnotations.getAnnotations());
                }

                if (invisibleAnnotations != null) {
                    Collections.addAll(annotations, invisibleAnnotations.getAnnotations());
                }
            }
        }
    }

    private TypeReference tryParseTypeSignature(final String signature, final String fallback) {
        try {
            if (signature != null) {
                return _parser.parseTypeSignature(signature);
            }
        }
        catch (final Throwable ignored) {
        }

        return _parser.parseTypeSignature(fallback);
    }

    @SuppressWarnings("ConstantConditions")
    private void defineMethods() {
        try (final AutoCloseable ignored = _parser.suppressTypeResolution()) {
            for (final MethodInfo method : _methods) {
                final IMethodSignature methodSignature;
                final IMethodSignature methodDescriptor = _parser.parseMethodSignature(method.descriptor);
                final MethodDefinition methodDefinition = new MethodDefinition();

                methodDefinition.setName(method.name);
                methodDefinition.setFlags(Flags.fromStandardFlags(method.accessFlags, Flags.Kind.Method));
                methodDefinition.setDeclaringType(_typeDefinition);

                if (_typeDefinition.isInterface() && !Flags.testAny(method.accessFlags, Flags.ABSTRACT)) {
                    methodDefinition.setFlags(methodDefinition.getFlags() | Flags.DEFAULT);
                }

                _typeDefinition.getDeclaredMethodsInternal().add(methodDefinition);
                _parser.pushGenericContext(methodDefinition);

                try {
                    final SignatureAttribute signature = SourceAttribute.find(AttributeNames.Signature, method.attributes);

                    methodSignature = tryParseMethodSignature(
                        signature != null ? signature.getSignature() : null,
                        methodDescriptor
                    );

                    final List<ParameterDefinition> signatureParameters = methodSignature.getParameters();
                    final List<ParameterDefinition> descriptorParameters = methodDescriptor.getParameters();
                    final ParameterDefinitionCollection parameters = methodDefinition.getParametersInternal();

                    methodDefinition.setReturnType(methodSignature.getReturnType());
                    parameters.addAll(signatureParameters);
                    methodDefinition.getGenericParametersInternal().addAll(methodSignature.getGenericParameters());
                    methodDefinition.getThrownTypesInternal().addAll(methodSignature.getThrownTypes());

                    final int missingParameters = descriptorParameters.size() - signatureParameters.size();

                    for (int i = 0; i < missingParameters; i++) {
                        final ParameterDefinition parameter = descriptorParameters.get(i);
                        parameter.setFlags(parameter.getFlags() | Flags.SYNTHETIC);
                        parameters.add(i, parameter);
                    }

                    int slot = 0;

                    if (!Flags.testAny(methodDefinition.getFlags(), Flags.STATIC)) {
                        ++slot;
                    }

                    final MethodParametersAttribute methodParameters = SourceAttribute.find(AttributeNames.MethodParameters, method.attributes);
                    final List<MethodParameterEntry> parameterEntries = methodParameters != null ? methodParameters.getEntries() : null;
                    final List<ParameterDefinition> parametersList = methodDefinition.getParameters();

                    for (int i = 0; i < parametersList.size(); i++) {
                        final ParameterDefinition parameter = parametersList.get(i);

                        parameter.setSlot(slot);
                        slot += parameter.getSize();

                        if (parameterEntries != null && i < parameterEntries.size()) {
                            final MethodParameterEntry entry = parameterEntries.get(i);
                            final String parameterName = entry.getName();

                            if (!StringUtilities.isNullOrWhitespace(parameterName)) {
                                parameter.setName(parameterName);
                            }

                            parameter.setFlags(entry.getFlags());
                        }
                    }

//                    if (methodDefinition.isSynthetic() &&
//                        methodDefinition.isPackagePrivate() &&
//                        parameters.size() > 0) {
//
//                        final ParameterDefinition parameter = last(parameters);
//                        final TypeReference parameterType = parameter.getParameterType();
//                        final TypeDefinition resolvedParameterType = parameterType.resolve();
//
//                        if (resolvedParameterType != null &&
//                            (resolvedParameterType.isAnonymous() ||
//                             resolvedParameterType.isSynthetic())) {
//
//                            parameter.setFlags(parameter.getFlags() | Flags.SYNTHETIC);
//                        }
//                    }

                    inflateAttributes(method.attributes);

                    Collections.addAll(methodDefinition.getSourceAttributesInternal(), method.attributes);

                    method.codeAttribute = SourceAttribute.find(AttributeNames.Code, method.attributes);

                    if (method.codeAttribute != null) {
                        methodDefinition.getSourceAttributesInternal().addAll(((CodeAttribute) method.codeAttribute).getAttributes());
                    }

                    final ExceptionsAttribute exceptions = SourceAttribute.find(AttributeNames.Exceptions, method.attributes);

                    if (exceptions != null) {
                        final Collection<TypeReference> thrownTypes = methodDefinition.getThrownTypesInternal();

                        for (final TypeReference thrownType : exceptions.getExceptionTypes()) {
                            if (!thrownTypes.contains(thrownType)) {
                                thrownTypes.add(thrownType);
                            }
                        }
                    }

                    if ("<init>".equals(method.name)) {
                        if (Flags.testAny(_typeDefinition.getFlags(), Flags.ANONYMOUS)) {
                            methodDefinition.setFlags(methodDefinition.getFlags() | Flags.ANONCONSTR | Flags.SYNTHETIC);
                        }

                        if (Flags.testAny(method.accessFlags, Flags.STRICTFP)) {
                            _typeDefinition.setFlags(_typeDefinition.getFlags() | Flags.STRICTFP);
                        }
                    }

                    readMethodBody(method, methodDefinition);

                    if (SourceAttribute.find(AttributeNames.Synthetic, method.attributes) != null) {
                        methodDefinition.setFlags(methodDefinition.getFlags() | Flags.SYNTHETIC);
                    }

                    if (SourceAttribute.find(AttributeNames.Deprecated, method.attributes) != null) {
                        methodDefinition.setFlags(methodDefinition.getFlags() | Flags.DEPRECATED);
                    }

                    if (shouldProcessAnnotations()) {
                        final AnnotationsAttribute visibleAnnotations = SourceAttribute.find(
                            AttributeNames.RuntimeVisibleAnnotations,
                            method.attributes
                        );

                        final AnnotationsAttribute invisibleAnnotations = SourceAttribute.find(
                            AttributeNames.RuntimeInvisibleAnnotations,
                            method.attributes
                        );

                        final Collection<CustomAnnotation> annotations = methodDefinition.getAnnotationsInternal();

                        if (visibleAnnotations != null) {
                            Collections.addAll(annotations, visibleAnnotations.getAnnotations());
                        }

                        if (invisibleAnnotations != null) {
                            Collections.addAll(annotations, invisibleAnnotations.getAnnotations());
                        }

                        final ParameterAnnotationsAttribute visibleParameterAnnotations = SourceAttribute.find(
                            AttributeNames.RuntimeVisibleParameterAnnotations,
                            method.attributes
                        );

                        final ParameterAnnotationsAttribute invisibleParameterAnnotations = SourceAttribute.find(
                            AttributeNames.RuntimeInvisibleParameterAnnotations,
                            method.attributes
                        );

                        if (visibleParameterAnnotations != null) {
                            for (int i = 0; i < visibleParameterAnnotations.getAnnotations().length && i < parameters.size(); i++) {
                                Collections.addAll(
                                    parameters.get(i).getAnnotationsInternal(),
                                    visibleParameterAnnotations.getAnnotations()[i]
                                );
                            }
                        }

                        if (invisibleParameterAnnotations != null) {
                            for (int i = 0; i < invisibleParameterAnnotations.getAnnotations().length && i < parameters.size(); i++) {
                                Collections.addAll(
                                    parameters.get(i).getAnnotationsInternal(),
                                    invisibleParameterAnnotations.getAnnotations()[i]
                                );
                            }
                        }
                    }
                }
                finally {
                    _parser.popGenericContext();
                }
            }
        }
        catch (final Exception e) {
            throw ExceptionUtilities.asRuntimeException(e);
        }
    }

    private IMethodSignature tryParseMethodSignature(final String signature, final IMethodSignature fallback) {
        try {
            if (signature != null) {
                return _parser.parseMethodSignature(signature);
            }
        }
        catch (final Throwable ignored) {
        }

        return fallback;
    }

    private void readMethodBody(final MethodInfo methodInfo, final MethodDefinition methodDefinition) {
        if (methodInfo.codeAttribute instanceof CodeAttribute) {
            if (Flags.testAny(_options, OPTION_PROCESS_CODE)) {
                final MethodReader reader = new MethodReader(methodDefinition, _scope);
                final MethodBody body = reader.readBody();

                methodDefinition.setBody(body);
                body.freeze();
            }
            else {
                final CodeAttribute codeAttribute = (CodeAttribute) methodInfo.codeAttribute;

                final LocalVariableTableAttribute localVariables = SourceAttribute.find(
                    AttributeNames.LocalVariableTable,
                    codeAttribute.getAttributes()
                );

                if (localVariables == null) {
                    return;
                }

                final List<ParameterDefinition> parameters = methodDefinition.getParameters();

                for (final LocalVariableTableEntry entry : localVariables.getEntries()) {
                    ParameterDefinition parameter = null;

                    for (int j = 0; j < parameters.size(); j++) {
                        if (parameters.get(j).getSlot() == entry.getIndex()) {
                            parameter = parameters.get(j);
                            break;
                        }
                    }

                    if (parameter != null && !parameter.hasName()) {
                        parameter.setName(entry.getName());
                    }
                }
            }
        }
    }

    private void visitAttributes() {
        inflateAttributes(_attributes);

        if (shouldProcessAnnotations()) {
            final AnnotationsAttribute visibleAnnotations = SourceAttribute.find(
                AttributeNames.RuntimeVisibleAnnotations,
                _attributes
            );

            final AnnotationsAttribute invisibleAnnotations = SourceAttribute.find(
                AttributeNames.RuntimeInvisibleAnnotations,
                _attributes
            );

            final Collection<CustomAnnotation> annotations = _typeDefinition.getAnnotationsInternal();

            if (visibleAnnotations != null) {
                Collections.addAll(annotations, visibleAnnotations.getAnnotations());
            }

            if (invisibleAnnotations != null) {
                Collections.addAll(annotations, invisibleAnnotations.getAnnotations());
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="FieldInfo Class">

    final class FieldInfo {
        final int accessFlags;
        final String name;
        final String descriptor;
        final SourceAttribute[] attributes;

        FieldInfo(final int accessFlags, final String name, final String descriptor, final SourceAttribute[] attributes) {
            this.accessFlags = accessFlags;
            this.name = name;
            this.descriptor = descriptor;
            this.attributes = attributes;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="MethodInfo Class">

    final class MethodInfo {
        final int accessFlags;
        final String name;
        final String descriptor;
        final SourceAttribute[] attributes;

        SourceAttribute codeAttribute;

        MethodInfo(final int accessFlags, final String name, final String descriptor, final SourceAttribute[] attributes) {
            this.accessFlags = accessFlags;
            this.name = name;
            this.descriptor = descriptor;
            this.attributes = attributes;
            this.codeAttribute = SourceAttribute.find(AttributeNames.Code, attributes);
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Metadata Scope">

    private final static MethodHandleType[] METHOD_HANDLE_TYPES = MethodHandleType.values();

    static class Scope implements IMetadataScope {
        private final MetadataParser _parser;
        private final TypeDefinition _typeDefinition;
        private final ConstantPool _constantPool;

        Scope(final MetadataParser parser, final TypeDefinition typeDefinition, final ConstantPool constantPool) {
            _parser = parser;
            _typeDefinition = typeDefinition;
            _constantPool = constantPool;
        }

        @Override
        public TypeReference lookupType(final int token) {
            final ConstantPool.Entry entry = _constantPool.get(token);

            if (entry instanceof ConstantPool.TypeInfoEntry) {
                final ConstantPool.TypeInfoEntry typeInfo = (ConstantPool.TypeInfoEntry) entry;

                return _parser.parseTypeDescriptor(typeInfo.getName());
            }

            final String typeName = _constantPool.lookupConstant(token);

            return _parser.parseTypeSignature(typeName);
        }

        @Override
        public FieldReference lookupField(final int token) {
            final ConstantPool.FieldReferenceEntry entry = _constantPool.getEntry(token);
            return lookupField(entry.typeInfoIndex, entry.nameAndTypeDescriptorIndex);
        }

        @Override
        public MethodReference lookupMethod(final int token) {
            final ConstantPool.Entry entry = _constantPool.getEntry(token);
            final ConstantPool.ReferenceEntry reference;

            if (entry instanceof ConstantPool.MethodHandleEntry) {
                final ConstantPool.MethodHandleEntry methodHandle = (ConstantPool.MethodHandleEntry) entry;
                reference = _constantPool.getEntry(methodHandle.referenceIndex);
            }
            else {
                reference = (ConstantPool.ReferenceEntry) entry;
            }

            return lookupMethod(reference.typeInfoIndex, reference.nameAndTypeDescriptorIndex);
        }

        @Override
        public MethodHandle lookupMethodHandle(final int token) {
            final ConstantPool.MethodHandleEntry entry = _constantPool.getEntry(token);
            final ConstantPool.ReferenceEntry reference = _constantPool.getEntry(entry.referenceIndex);

            return new MethodHandle(
                lookupMethod(reference.typeInfoIndex, reference.nameAndTypeDescriptorIndex),
                METHOD_HANDLE_TYPES[entry.referenceKind.ordinal()]
            );
        }

        @Override
        public IMethodSignature lookupMethodType(final int token) {
            final ConstantPool.MethodTypeEntry entry = _constantPool.getEntry(token);
            return _parser.parseMethodSignature(entry.getType());
        }

        @Override
        public DynamicCallSite lookupDynamicCallSite(final int token) {
            final ConstantPool.InvokeDynamicInfoEntry entry = _constantPool.getEntry(token);
            final BootstrapMethodsAttribute attribute = SourceAttribute.find(AttributeNames.BootstrapMethods, _typeDefinition.getSourceAttributes());

            final BootstrapMethodsTableEntry bootstrapMethod = attribute.getBootstrapMethods()
                                                                        .get(entry.bootstrapMethodAttributeIndex);

            final ConstantPool.NameAndTypeDescriptorEntry nameAndType = _constantPool.getEntry(entry.nameAndTypeDescriptorIndex);

            return new DynamicCallSite(
                entry.bootstrapMethodAttributeIndex,
                bootstrapMethod.getMethodHandle(),
                bootstrapMethod.getArguments(),
                nameAndType.getName(),
                _parser.parseMethodSignature(nameAndType.getType())
            );
        }

        @Override
        public FieldReference lookupField(final int typeToken, final int nameAndTypeToken) {
            final ConstantPool.NameAndTypeDescriptorEntry nameAndDescriptor = _constantPool.getEntry(nameAndTypeToken);

            return _parser.parseField(
                lookupType(typeToken),
                nameAndDescriptor.getName(),
                nameAndDescriptor.getType()
            );
        }

        @Override
        public MethodReference lookupMethod(final int typeToken, final int nameAndTypeToken) {
            final ConstantPool.NameAndTypeDescriptorEntry nameAndDescriptor = _constantPool.getEntry(nameAndTypeToken);

            return _parser.parseMethod(
                lookupType(typeToken),
                nameAndDescriptor.getName(),
                nameAndDescriptor.getType()
            );
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T lookupConstant(final int token) {
            final ConstantPool.Entry entry = _constantPool.get(token);

            if (entry.getTag() == ConstantPool.Tag.TypeInfo) {
                return (T) lookupType(token);
            }

            return _constantPool.lookupConstant(token);
        }

        @Override
        public Object lookup(final int token) {
            final ConstantPool.Entry entry = _constantPool.get(token);

            if (entry == null) {
                return null;
            }

            switch (entry.getTag()) {
                case Utf8StringConstant:
                case IntegerConstant:
                case FloatConstant:
                case LongConstant:
                case DoubleConstant:
                case StringConstant:
                    return lookupConstant(token);

                case TypeInfo:
                    return lookupType(token);

                case FieldReference:
                    return lookupField(token);

                case MethodReference:
                    return lookupMethod(token);

                case InterfaceMethodReference:
                    return lookupMethod(token);

                case MethodHandle:
                    return lookupMethodHandle(token);

                case MethodType:
                    return lookupMethodType(token);

                case InvokeDynamicInfo:
                    return lookupDynamicCallSite(token);

                default:
                    return null;
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ResolverFrame Class">

    private final class ResolverFrame implements IResolverFrame {
        final HashMap<String, TypeReference> types = new HashMap<>();
        final HashMap<String, GenericParameter> typeVariables = new HashMap<>();

        public void addType(final TypeReference type) {
            VerifyArgument.notNull(type, "type");
            types.put(type.getInternalName(), type);
        }

        public void addTypeVariable(final GenericParameter type) {
            VerifyArgument.notNull(type, "type");
            typeVariables.put(type.getName(), type);
        }

        public void removeType(final TypeReference type) {
            VerifyArgument.notNull(type, "type");
            types.remove(type.getInternalName());
        }

        public void removeTypeVariable(final GenericParameter type) {
            VerifyArgument.notNull(type, "type");
            typeVariables.remove(type.getName());
        }

        @Override
        public TypeReference findType(final String descriptor) {
            final TypeReference type = types.get(descriptor);

            if (type != null) {
                return type;
            }

            return null;
        }

        @Override
        public GenericParameter findTypeVariable(final String name) {
            final GenericParameter typeVariable = typeVariables.get(name);

            if (typeVariable != null) {
                return typeVariable;
            }

            for (final String typeName : types.keySet()) {
                final TypeReference t = types.get(typeName);

                if (t.containsGenericParameters()) {
                    for (final GenericParameter p : t.getGenericParameters()) {
                        if (StringUtilities.equals(p.getName(), name)) {
                            return p;
                        }
                    }
                }
            }

            return null;
        }
    }

    // </editor-fold>
}
