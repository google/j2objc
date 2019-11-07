/*
 * MethodDefinition.java
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
import com.strobel.assembler.ir.attributes.AttributeNames;
import com.strobel.assembler.ir.attributes.CodeAttribute;
import com.strobel.assembler.ir.attributes.ExceptionTableEntry;
import com.strobel.assembler.ir.attributes.SourceAttribute;
import com.strobel.assembler.metadata.annotations.CustomAnnotation;
import com.strobel.core.HashUtilities;
import com.strobel.core.StringUtilities;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

public class MethodDefinition extends MethodReference implements IMemberDefinition {
    private final GenericParameterCollection _genericParameters;
    private final ParameterDefinitionCollection _parameters;
    private final AnonymousLocalTypeCollection _declaredTypes;
    private final Collection<TypeReference> _thrownTypes;
    private final Collection<CustomAnnotation> _customAnnotations;
    private final Collection<SourceAttribute> _sourceAttributes;
    private final List<GenericParameter> _genericParametersView;
    private final List<TypeDefinition> _declaredTypesView;
    private final List<ParameterDefinition> _parametersView;
    private final List<TypeReference> _thrownTypesView;
    private final List<CustomAnnotation> _customAnnotationsView;
    private final List<SourceAttribute> _sourceAttributesView;

    private SoftReference<MethodBody> _body;
    private String _name;
    private String _fullName;
    private String _erasedSignature;
    private String _signature;
    private TypeReference _returnType;
    private TypeDefinition _declaringType;
    private long _flags;

    protected MethodDefinition() {
        _genericParameters = new GenericParameterCollection(this);
        _parameters = new ParameterDefinitionCollection(this);
        _declaredTypes = new AnonymousLocalTypeCollection(this);
        _thrownTypes = new Collection<>();
        _customAnnotations = new Collection<>();
        _sourceAttributes = new Collection<>();
        _genericParametersView = Collections.unmodifiableList(_genericParameters);
        _parametersView = Collections.unmodifiableList(_parameters);
        _declaredTypesView = Collections.unmodifiableList(_declaredTypes);
        _thrownTypesView = Collections.unmodifiableList(_thrownTypes);
        _customAnnotationsView = Collections.unmodifiableList(_customAnnotations);
        _sourceAttributesView = Collections.unmodifiableList(_sourceAttributes);
    }

    public final boolean hasBody() {
        final SoftReference<MethodBody> bodyCache = _body;
        return bodyCache != null && bodyCache.get() != null;
    }

    public final MethodBody getBody() {
        final MethodBody body;
        final SoftReference<MethodBody> cachedBody = _body;

        if (cachedBody == null || (body = _body.get()) == null) {
            return tryLoadBody();
        }

        return body;
    }

    public final boolean hasThis() {
        return !isStatic();
    }

    protected final void setBody(final MethodBody body) {
        _body = new SoftReference<>(body);
    }

    @Override
    public final boolean isDefinition() {
        return true;
    }

    public final boolean isAnonymousClassConstructor() {
        return Flags.testAny(_flags, Flags.ANONCONSTR);
    }

    public final List<TypeDefinition> getDeclaredTypes() {
        return _declaredTypesView;
    }

    protected final AnonymousLocalTypeCollection getDeclaredTypesInternal() {
        return _declaredTypes;
    }

    @Override
    public final List<GenericParameter> getGenericParameters() {
        return _genericParametersView;
    }

    @Override
    public final List<TypeReference> getThrownTypes() {
        return _thrownTypesView;
    }

    @Override
    public final TypeDefinition getDeclaringType() {
        return _declaringType;
    }

    @Override
    public final List<CustomAnnotation> getAnnotations() {
        return _customAnnotationsView;
    }

    public final List<SourceAttribute> getSourceAttributes() {
        return _sourceAttributesView;
    }

    @Override
    public final String getName() {
        return _name;
    }

    @Override
    public String getFullName() {
        if (_fullName == null) {
            _fullName = super.getFullName();
        }
        return _fullName;
    }

    @Override
    public String getSignature() {
        if (_signature == null) {
            _signature = super.getSignature();
        }
        return _signature;
    }

    @Override
    public String getErasedSignature() {
        if (_erasedSignature == null) {
            _erasedSignature = super.getErasedSignature();
        }
        return _erasedSignature;
    }

    @Override
    public final TypeReference getReturnType() {
        return _returnType;
    }

    @Override
    public final List<ParameterDefinition> getParameters() {
        return _parametersView;
    }

    protected final void setName(final String name) {
        _name = name;
    }

    protected final void setReturnType(final TypeReference returnType) {
        _returnType = returnType;
        invalidateSignature();
    }

    protected final void setDeclaringType(final TypeDefinition declaringType) {
        _declaringType = declaringType;
        _parameters.setDeclaringType(declaringType);
    }

    protected final void setFlags(final long flags) {
        _flags = flags;
    }

    protected final GenericParameterCollection getGenericParametersInternal() {
        return _genericParameters;
    }

    protected final ParameterDefinitionCollection getParametersInternal() {
        return _parameters;
    }

    protected final Collection<TypeReference> getThrownTypesInternal() {
        return _thrownTypes;
    }

    protected final Collection<CustomAnnotation> getAnnotationsInternal() {
        return _customAnnotations;
    }

    protected final Collection<SourceAttribute> getSourceAttributesInternal() {
        return _sourceAttributes;
    }

    @Override
    public int hashCode() {
        return HashUtilities.hashCode(getFullName());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof MethodDefinition) {
            final MethodDefinition other = (MethodDefinition) obj;

            return StringUtilities.equals(getName(), other.getName()) &&
                   StringUtilities.equals(getErasedSignature(), other.getErasedSignature()) &&
                   typeNamesMatch(getDeclaringType(), other.getDeclaringType());
        }

        return false;
    }

    @Override
    public void invalidateSignature() {
        _signature = null;
        _erasedSignature = null;
    }

    private boolean typeNamesMatch(final TypeReference t1, final TypeReference t2) {
        return t1 != null &&
               t2 != null &&
               StringUtilities.equals(t1.getFullName(), t2.getFullName());
    }

    // <editor-fold defaultstate="collapsed" desc="Method Attributes">

    public final boolean isAbstract() {
        return Flags.testAny(getFlags(), Flags.ABSTRACT);
    }

    public final boolean isDefault() {
        return Flags.testAny(getFlags(), Flags.DEFAULT);
    }

    public final boolean isBridgeMethod() {
        return Flags.testAny(getFlags(), Flags.ACC_BRIDGE | Flags.BRIDGE);
    }

    public final boolean isVarArgs() {
        return Flags.testAny(getFlags(), Flags.ACC_VARARGS | Flags.VARARGS);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Member Attributes">_

    @Override
    public final long getFlags() {
        return _flags;
    }

    @Override
    public final int getModifiers() {
        return Flags.toModifiers(getFlags());
    }

    @Override
    public final boolean isFinal() {
        return Flags.testAny(getFlags(), Flags.FINAL);
    }

    @Override
    public final boolean isNonPublic() {
        return !Flags.testAny(getFlags(), Flags.PUBLIC);
    }

    @Override
    public final boolean isPrivate() {
        return Flags.testAny(getFlags(), Flags.PRIVATE);
    }

    @Override
    public final boolean isProtected() {
        return Flags.testAny(getFlags(), Flags.PROTECTED);
    }

    @Override
    public final boolean isPublic() {
        return Flags.testAny(getFlags(), Flags.PUBLIC);
    }

    @Override
    public final boolean isStatic() {
        return Flags.testAny(getFlags(), Flags.STATIC);
    }

    @Override
    public final boolean isSynthetic() {
        return Flags.testAny(getFlags(), Flags.SYNTHETIC);
    }

    @Override
    public final boolean isDeprecated() {
        return Flags.testAny(getFlags(), Flags.DEPRECATED);
    }

    @Override
    public final boolean isPackagePrivate() {
        return !Flags.testAny(getFlags(), Flags.PUBLIC | Flags.PROTECTED | Flags.PRIVATE);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Name and Signature Formatting">

    /**
     * Human-readable brief description of a type or member, which does not include information super types, thrown exceptions, or modifiers other than
     * 'static'.
     */
    @Override
    public String getBriefDescription() {
        return appendBriefDescription(new StringBuilder()).toString();
    }

    /**
     * Human-readable full description of a type or member, which includes specification of super types (in brief format), thrown exceptions, and modifiers.
     */
    @Override
    public String getDescription() {
        return appendDescription(new StringBuilder()).toString();
    }

    /**
     * Human-readable erased description of a type or member.
     */
    @Override
    public String getErasedDescription() {
        return appendErasedDescription(new StringBuilder()).toString();
    }

    /**
     * Human-readable simple description of a type or member, which does not include information super type or fully-qualified type names.
     */
    @Override
    public String getSimpleDescription() {
        return appendSimpleDescription(new StringBuilder()).toString();
    }

    @Override
    protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
        if (fullName) {
            final TypeDefinition declaringType = getDeclaringType();

            if (declaringType != null) {
                return declaringType.appendName(sb, true, false).append('.').append(getName());
            }
        }

        return sb.append(_name);
    }

    @SuppressWarnings("ConstantConditions")
    public StringBuilder appendDescription(final StringBuilder sb) {
        StringBuilder s = sb;

        for (final javax.lang.model.element.Modifier modifier : Flags.asModifierSet(getModifiers() & ~Flags.ACC_VARARGS)) {
            s.append(modifier.toString());
            s.append(' ');
        }

        final List<? extends TypeReference> typeArguments;

        if (this instanceof IGenericInstance) {
            typeArguments = ((IGenericInstance) this).getTypeArguments();
        }
        else if (hasGenericParameters()) {
            typeArguments = getGenericParameters();
        }
        else {
            typeArguments = Collections.emptyList();
        }

        if (!typeArguments.isEmpty()) {
            final int count = typeArguments.size();

            s.append('<');

            for (int i = 0; i < count; i++) {
                if (i != 0) {
                    s.append(", ");
                }
                s = typeArguments.get(i).appendSimpleDescription(s);
            }

            s.append('>');
            s.append(' ');
        }

        TypeReference returnType = getReturnType();

        while (returnType.isWildcardType()) {
            returnType = returnType.getExtendsBound();
        }

        if (returnType.isGenericParameter()) {
            s.append(returnType.getName());
        }
        else {
            s = returnType.appendSimpleDescription(s);
        }

        s.append(' ');
        s.append(getName());
        s.append('(');

        final List<ParameterDefinition> parameters = getParameters();

        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterDefinition p = parameters.get(i);

            if (i != 0) {
                s.append(", ");
            }

            TypeReference parameterType = p.getParameterType();

            while (parameterType.isWildcardType()) {
                parameterType = parameterType.getExtendsBound();
            }

            if (parameterType.isGenericParameter()) {
                s.append(parameterType.getName());
            }
            else {
                s = parameterType.appendSimpleDescription(s);
            }

            s.append(" ").append(p.getName());
        }

        s.append(')');

        final List<TypeReference> thrownTypes = getThrownTypes();

        if (!thrownTypes.isEmpty()) {
            s.append(" throws ");

            for (int i = 0, n = thrownTypes.size(); i < n; ++i) {
                final TypeReference t = thrownTypes.get(i);
                if (i != 0) {
                    s.append(", ");
                }
                s = t.appendBriefDescription(s);
            }
        }

        return s;
    }

    public StringBuilder appendSimpleDescription(final StringBuilder sb) {
        StringBuilder s = sb;

        for (final javax.lang.model.element.Modifier modifier : Flags.asModifierSet(getModifiers() & ~Flags.ACC_VARARGS)) {
            s.append(modifier.toString());
            s.append(' ');
        }

        final List<? extends TypeReference> typeArguments;

        if (this instanceof IGenericInstance) {
            typeArguments = ((IGenericInstance) this).getTypeArguments();
        }
        else if (hasGenericParameters()) {
            typeArguments = getGenericParameters();
        }
        else {
            typeArguments = Collections.emptyList();
        }

        if (!typeArguments.isEmpty()) {
            s.append('<');
            for (int i = 0, n = typeArguments.size(); i < n; i++) {
                if (i != 0) {
                    s.append(", ");
                }

                final TypeReference typeArgument = typeArguments.get(i);

                if (typeArgument instanceof GenericParameter) {
                    s.append(typeArgument.getSimpleName());
                }
                else {
                    s = typeArgument.appendSimpleDescription(s);
                }
            }
            s.append('>');
            s.append(' ');
        }

        TypeReference returnType = getReturnType();

        while (returnType.isWildcardType()) {
            returnType = returnType.getExtendsBound();
        }

        if (returnType.isGenericParameter()) {
            s.append(returnType.getName());
        }
        else {
            s = returnType.appendSimpleDescription(s);
        }

        s.append(' ');
        s.append(getName());
        s.append('(');

        final List<ParameterDefinition> parameters = getParameters();

        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterDefinition p = parameters.get(i);

            if (i != 0) {
                s.append(", ");
            }

            TypeReference parameterType = p.getParameterType();

            while (parameterType.isWildcardType()) {
                parameterType = parameterType.getExtendsBound();
            }

            if (parameterType.isGenericParameter()) {
                s.append(parameterType.getName());
            }
            else {
                s = parameterType.appendSimpleDescription(s);
            }
        }

        s.append(')');

        final List<TypeReference> thrownTypes = getThrownTypes();

        if (!thrownTypes.isEmpty()) {
            s.append(" throws ");

            for (int i = 0, n = thrownTypes.size(); i < n; ++i) {
                final TypeReference t = thrownTypes.get(i);
                if (i != 0) {
                    s.append(", ");
                }
                s = t.appendSimpleDescription(s);
            }
        }

        return s;
    }

    public StringBuilder appendBriefDescription(final StringBuilder sb) {
        StringBuilder s = sb;

        TypeReference returnType = getReturnType();

        while (returnType.isWildcardType()) {
            returnType = returnType.getExtendsBound();
        }

        if (returnType.isGenericParameter()) {
            s.append(returnType.getName());
        }
        else {
            s = returnType.appendBriefDescription(s);
        }

        s.append(' ');
        s.append(getName());
        s.append('(');

        final List<ParameterDefinition> parameters = getParameters();

        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterDefinition p = parameters.get(i);

            if (i != 0) {
                s.append(", ");
            }

            TypeReference parameterType = p.getParameterType();

            while (parameterType.isWildcardType()) {
                parameterType = parameterType.getExtendsBound();
            }

            if (parameterType.isGenericParameter()) {
                s.append(parameterType.getName());
            }
            else {
                s = parameterType.appendBriefDescription(s);
            }
        }

        s.append(')');

        return s;
    }

    public StringBuilder appendErasedDescription(final StringBuilder sb) {
        if (hasGenericParameters() && !isGenericDefinition()) {
            final MethodDefinition definition = resolve();
            if (definition != null) {
                return definition.appendErasedDescription(sb);
            }
        }

        for (final javax.lang.model.element.Modifier modifier : Flags.asModifierSet(getModifiers() & ~Flags.ACC_VARARGS)) {
            sb.append(modifier.toString());
            sb.append(' ');
        }

        final List<ParameterDefinition> parameterTypes = getParameters();

        StringBuilder s = getReturnType().appendErasedDescription(sb);

        s.append(' ');
        s.append(getName());
        s.append('(');

        for (int i = 0, n = parameterTypes.size(); i < n; ++i) {
            if (i != 0) {
                s.append(", ");
            }
            s = parameterTypes.get(i).getParameterType().appendErasedDescription(s);
        }

        s.append(')');
        return s;
    }

    @Override
    public String toString() {
        return getSimpleDescription();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Deferred Method Body Loading">

    private MethodBody tryLoadBody() {
        if (Flags.testAny(_flags, Flags.LOAD_BODY_FAILED)) {
            return null;
        }

        final CodeAttribute codeAttribute = SourceAttribute.find(AttributeNames.Code, _sourceAttributes);

        if (codeAttribute == null) {
            return null;
        }

        final int codeAttributeIndex = _sourceAttributes.indexOf(codeAttribute);

        Buffer code = codeAttribute.getCode();
        ConstantPool constantPool = _declaringType.getConstantPool();

        if (code == null) {
            final ITypeLoader typeLoader = _declaringType.getTypeLoader();

            if (typeLoader == null) {
                _flags |= Flags.LOAD_BODY_FAILED;
                return null;
            }

            code = new Buffer();

            if (!typeLoader.tryLoadType(_declaringType.getInternalName(), code)) {
                _flags |= Flags.LOAD_BODY_FAILED;
                return null;
            }

            final List<ExceptionTableEntry> exceptionTableEntries = codeAttribute.getExceptionTableEntries();
            final List<SourceAttribute> codeAttributes = codeAttribute.getAttributes();

            final CodeAttribute newCode = new CodeAttribute(
                codeAttribute.getLength(),
                codeAttribute.getMaxStack(),
                codeAttribute.getMaxLocals(),
                codeAttribute.getCodeOffset(),
                codeAttribute.getCodeSize(),
                code,
                exceptionTableEntries.toArray(new ExceptionTableEntry[exceptionTableEntries.size()]),
                codeAttributes.toArray(new SourceAttribute[codeAttributes.size()])
            );

            if (constantPool == null) {
                final long magic = code.readInt() & 0xFFFFFFFFL;

                if (magic != ClassFileReader.MAGIC) {
                    throw new IllegalStateException(
                        format(
                            "Could not load method body for '%s:%s'; wrong magic number in class header: 0x%8X.",
                            getFullName(),
                            getSignature(),
                            magic
                        )
                    );
                }

                code.readUnsignedShort(); // minor version
                code.readUnsignedShort(); // major version

                constantPool = ConstantPool.read(code);
            }

            _sourceAttributes.set(codeAttributeIndex, newCode);
        }

        final MetadataParser parser = new MetadataParser(_declaringType);
        final IMetadataScope scope = new ClassFileReader.Scope(parser, _declaringType, constantPool);

        final MethodBody body = new MethodReader(this, scope).readBody();

        _body = new SoftReference<>(body);
        _sourceAttributes.set(codeAttributeIndex, codeAttribute);

        body.tryFreeze();

        return body;
    }

    // </editor-fold>
}
