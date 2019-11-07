/*
 * CoreMetadataFactory.java
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

import com.strobel.assembler.ir.attributes.AttributeNames;
import com.strobel.assembler.ir.attributes.InnerClassEntry;
import com.strobel.assembler.ir.attributes.InnerClassesAttribute;
import com.strobel.assembler.ir.attributes.SourceAttribute;
import com.strobel.assembler.metadata.signatures.BottomSignature;
import com.strobel.assembler.metadata.signatures.FieldTypeSignature;
import com.strobel.assembler.metadata.signatures.MetadataFactory;
import com.strobel.assembler.metadata.signatures.Reifier;
import com.strobel.assembler.metadata.signatures.SimpleClassTypeSignature;
import com.strobel.core.ArrayUtilities;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class CoreMetadataFactory implements MetadataFactory {
    private final TypeDefinition _owner;
    private final IMetadataResolver _resolver;
    private final IGenericContext _scope;
    private final Stack<GenericParameter> _tempScope;

    private CoreMetadataFactory(final TypeDefinition owner, final IMetadataResolver resolver, final IGenericContext scope) {
        _owner = owner;
        _resolver = resolver;
        _scope = scope;
        _tempScope = new Stack<>();
    }

    public static CoreMetadataFactory make(final TypeDefinition owner, final IGenericContext scope) {
        return new CoreMetadataFactory(VerifyArgument.notNull(owner, "owner"), owner.getResolver(), scope);
    }

    public static CoreMetadataFactory make(final IMetadataResolver resolver, final IGenericContext scope) {
        return new CoreMetadataFactory(null, resolver, scope);
    }

    private IGenericContext getScope() {
        return _scope;
    }

    public GenericParameter makeTypeVariable(final String name, final FieldTypeSignature[] bounds) {
        final GenericParameter genericParameter = new GenericParameter(name);

        genericParameter.setResolver(_resolver);

        if (ArrayUtilities.isNullOrEmpty(bounds)) {
            return genericParameter;
        }

        _tempScope.push(genericParameter);

        try {
            final TypeReference extendsBound = makeTypeBound(bounds);
            genericParameter.setExtendsBound(extendsBound);
            return genericParameter;
        }
        finally {
            _tempScope.pop();
        }
    }

    public WildcardType makeWildcard(
        final FieldTypeSignature superBound,
        final FieldTypeSignature extendsBound) {

        if (superBound == null || superBound == BottomSignature.make()) {
            if (extendsBound == null ||
                (extendsBound instanceof SimpleClassTypeSignature &&
                 StringUtilities.equals("java.lang.Object", ((SimpleClassTypeSignature) extendsBound).getName()))) {

                return WildcardType.unbounded();
            }

            return WildcardType.makeExtends(makeTypeBound(extendsBound));
        }
        else {
            return WildcardType.makeSuper(makeTypeBound(superBound));
        }
    }

    protected TypeReference makeTypeBound(final FieldTypeSignature... bounds) {
        final TypeReference baseType;
        final List<TypeReference> interfaceTypes;

        Reifier reifier = null;

        if (ArrayUtilities.isNullOrEmpty(bounds)) {
            return null;
        }

        if (bounds[0] != BottomSignature.make()) {
            reifier = Reifier.make(this);
            bounds[0].accept(reifier);
            baseType = reifier.getResult();
            assert baseType != null;
        }
        else {
            baseType = null;
        }

        if (bounds.length == 1) {
            return baseType;
        }

        if (reifier == null) {
            reifier = Reifier.make(this);
        }

        if (bounds.length == 2 && baseType == null) {
            bounds[1].accept(reifier);
            final TypeReference singleInterface = reifier.getResult();
            assert singleInterface != null;
            return singleInterface;
        }

        final TypeReference[] it = new TypeReference[bounds.length - 1];

        for (int i = 0; i < it.length; i++) {
            bounds[i + 1].accept(reifier);
            it[i] = reifier.getResult();
            assert it[i] != null;
        }

        interfaceTypes = ArrayUtilities.asUnmodifiableList(it);

        return new CompoundTypeReference(baseType, interfaceTypes);
    }

    public TypeReference makeParameterizedType(
        final TypeReference declaration,
        final TypeReference owner,
        final TypeReference... typeArguments) {

        if (typeArguments.length == 0) {
            return declaration;
        }

        return declaration.makeGenericType(typeArguments);
    }

    public GenericParameter findTypeVariable(final String name) {
        for (int i = _tempScope.size() - 1; i >= 0; i--) {
            final GenericParameter genericParameter = _tempScope.get(i);

            if (genericParameter != null && StringUtilities.equals(genericParameter.getName(), name)) {
                return genericParameter;
            }
        }

        final IGenericContext scope = getScope();

        if (scope != null) {
            return scope.findTypeVariable(name);
        }

        return null;
    }

    private InnerClassEntry findInnerClassEntry(final String name) {
        if (_owner == null) {
            return null;
        }

        final String internalName = name.replace('.', '/');
        final SourceAttribute attribute = SourceAttribute.find(AttributeNames.InnerClasses, _owner.getSourceAttributes());

        if (attribute instanceof InnerClassesAttribute) {
            final List<InnerClassEntry> entries = ((InnerClassesAttribute) attribute).getEntries();

            for (final InnerClassEntry entry : entries) {
                if (StringUtilities.equals(entry.getInnerClassName(), internalName)) {
                    return entry;
                }
            }
        }

        return null;
    }

    public TypeReference makeNamedType(final String name) {
        final int length = name.length();

        final InnerClassEntry entry = findInnerClassEntry(name);

        if (entry != null) {
            final String innerClassName = entry.getInnerClassName();
            final int packageEnd = innerClassName.lastIndexOf('/');
            final String shortName = StringUtilities.isNullOrEmpty(entry.getShortName()) ? null : entry.getShortName();
            final TypeReference declaringType;

            if (!StringUtilities.isNullOrEmpty(entry.getOuterClassName())) {
                declaringType = makeNamedType(entry.getOuterClassName().replace('/', '.'));
            }
            else {
                int lastDollarIndex = name.lastIndexOf('$');

                while (lastDollarIndex >= 1 &&
                       lastDollarIndex < length &&
                       name.charAt(lastDollarIndex - 1) == '$') {

                    if (lastDollarIndex > 1) {
                        lastDollarIndex = name.lastIndexOf(lastDollarIndex, lastDollarIndex - 2);
                    }
                    else {
                        lastDollarIndex = -1;
                    }
                }

                if (lastDollarIndex == length - 1) {
                    lastDollarIndex = -1;
                }

                if (lastDollarIndex < 0) {
                    if (_owner != null && _owner.getCompilerTarget().compareTo(CompilerTarget.JDK1_4) < 0) {
                        declaringType = _owner;
                    }
                    else {
                        declaringType = null;
                    }
                }
                else {
                    declaringType = makeNamedType(name.substring(0, lastDollarIndex).replace('/', '.'));
                }
            }

            if (declaringType != null) {
                return new UnresolvedType(
                    declaringType,
                    packageEnd < 0 ? innerClassName : innerClassName.substring(packageEnd + 1),
                    shortName
                );
            }
        }

        final int packageEnd = name.lastIndexOf('.');

        if (packageEnd < 0) {
            return new UnresolvedType(StringUtilities.EMPTY, name, null);
        }

        return new UnresolvedType(
            packageEnd < 0 ? StringUtilities.EMPTY : name.substring(0, packageEnd),
            packageEnd < 0 ? name : name.substring(packageEnd + 1),
            null
        );
    }

    public TypeReference makeArrayType(final TypeReference componentType) {
        return componentType.makeArrayType();
    }

    public TypeReference makeByte() {
        return BuiltinTypes.Byte;
    }

    public TypeReference makeBoolean() {
        return BuiltinTypes.Boolean;
    }

    public TypeReference makeShort() {
        return BuiltinTypes.Short;
    }

    public TypeReference makeChar() {
        return BuiltinTypes.Character;
    }

    public TypeReference makeInt() {
        return BuiltinTypes.Integer;
    }

    public TypeReference makeLong() {
        return BuiltinTypes.Long;
    }

    public TypeReference makeFloat() {
        return BuiltinTypes.Float;
    }

    public TypeReference makeDouble() {
        return BuiltinTypes.Double;
    }

    public TypeReference makeVoid() {
        return BuiltinTypes.Void;
    }

    @Override
    public IMethodSignature makeMethodSignature(
        final TypeReference returnType,
        final List<TypeReference> parameterTypes,
        final List<GenericParameter> genericParameters,
        final List<TypeReference> thrownTypes) {

        return new MethodSignature(
            parameterTypes,
            returnType,
            genericParameters,
            thrownTypes
        );
    }

    @Override
    public IClassSignature makeClassSignature(
        final TypeReference baseType,
        final List<TypeReference> interfaceTypes,
        final List<GenericParameter> genericParameters) {

        return new ClassSignature(baseType, interfaceTypes, genericParameters);
    }

    private final static class ClassSignature implements IClassSignature {
        private final TypeReference _baseType;
        private final List<TypeReference> _interfaceTypes;
        private final List<GenericParameter> _genericParameters;

        private ClassSignature(
            final TypeReference baseType,
            final List<TypeReference> interfaceTypes,
            final List<GenericParameter> genericParameters) {

            _baseType = VerifyArgument.notNull(baseType, "baseType");
            _interfaceTypes = VerifyArgument.noNullElements(interfaceTypes, "interfaceTypes");
            _genericParameters = VerifyArgument.noNullElements(genericParameters, "genericParameters");
        }

        @Override
        public TypeReference getBaseType() {
            return _baseType;
        }

        @Override
        public List<TypeReference> getExplicitInterfaces() {
            return _interfaceTypes;
        }

        @Override
        public boolean hasGenericParameters() {
            return !_genericParameters.isEmpty();
        }

        @Override
        public boolean isGenericDefinition() {
            return false;
        }

        @Override
        public List<GenericParameter> getGenericParameters() {
            return _genericParameters;
        }
    }

    private final static class MethodSignature implements IMethodSignature {
        private final List<ParameterDefinition> _parameters;
        private final TypeReference _returnType;
        private final List<GenericParameter> _genericParameters;
        private final List<TypeReference> _thrownTypes;

        private String _signature;
        private String _erasedSignature;

        MethodSignature(
            final List<TypeReference> parameterTypes,
            final TypeReference returnType,
            final List<GenericParameter> genericParameters,
            final List<TypeReference> thrownTypes) {

            VerifyArgument.notNull(parameterTypes, "parameterTypes");
            VerifyArgument.notNull(returnType, "returnType");
            VerifyArgument.notNull(genericParameters, "genericParameters");
            VerifyArgument.notNull(thrownTypes, "thrownTypes");

            final ParameterDefinition[] parameters = new ParameterDefinition[parameterTypes.size()];

            for (int i = 0, slot = 0, n = parameters.length; i < n; i++, slot++) {
                final TypeReference parameterType = parameterTypes.get(i);

                parameters[i] = new ParameterDefinition(slot, parameterType);

                if (parameterType.getSimpleType().isDoubleWord()) {
                    slot++;
                }
            }

            _parameters = ArrayUtilities.asUnmodifiableList(parameters);
            _returnType = returnType;
            _genericParameters = genericParameters;
            _thrownTypes = thrownTypes;
        }

        @Override
        public boolean hasParameters() {
            return !_parameters.isEmpty();
        }

        @Override
        public List<ParameterDefinition> getParameters() {
            return _parameters;
        }

        @Override
        public TypeReference getReturnType() {
            return _returnType;
        }

        @Override
        public List<TypeReference> getThrownTypes() {
            return _thrownTypes;
        }

        @Override
        public String getSignature() {
            if (_signature == null) {
                _signature = MethodReference.appendSignature(this, new StringBuilder()).toString();
            }
            return _signature;
        }

        @Override
        public String getErasedSignature() {
            if (_erasedSignature == null) {
                _erasedSignature = MethodReference.appendErasedSignature(this, new StringBuilder()).toString();
            }
            return _erasedSignature;
        }

        @Override
        public void invalidateSignature() {
            _signature = null;
            _erasedSignature = null;
        }

        @Override
        public boolean hasGenericParameters() {
            return !_genericParameters.isEmpty();
        }

        @Override
        public boolean isGenericDefinition() {
            return !_genericParameters.isEmpty();
        }

        @Override
        public List<GenericParameter> getGenericParameters() {
            return _genericParameters;
        }

        @Override
        public GenericParameter findTypeVariable(final String name) {
            for (final GenericParameter genericParameter : getGenericParameters()) {
                if (StringUtilities.equals(genericParameter.getName(), name)) {
                    return genericParameter;
                }
            }

            return null;
        }
    }

    private final class UnresolvedType extends TypeReference {
        private final String _name;
        private final String _shortName;
        private final String _packageName;
        private final GenericParameterCollection _genericParameters;

        private String _fullName;
        private String _internalName;
        private String _signature;
        private String _erasedSignature;

        UnresolvedType(final TypeReference declaringType, final String name, final String shortName) {
            _name = VerifyArgument.notNull(name, "name");
            _shortName = shortName;
            setDeclaringType(VerifyArgument.notNull(declaringType, "declaringType"));
            _packageName = declaringType.getPackageName();
            _genericParameters = new GenericParameterCollection(this);
            _genericParameters.freeze();
        }

        UnresolvedType(final String packageName, final String name, final String shortName) {
            _packageName = VerifyArgument.notNull(packageName, "packageName");
            _name = VerifyArgument.notNull(name, "name");
            _shortName = shortName;
            _genericParameters = new GenericParameterCollection(this);
            _genericParameters.freeze();
        }

        UnresolvedType(final TypeReference declaringType, final String name, final String shortName, final List<GenericParameter> genericParameters) {
            _name = VerifyArgument.notNull(name, "name");
            _shortName = shortName;
            setDeclaringType(VerifyArgument.notNull(declaringType, "declaringType"));
            _packageName = declaringType.getPackageName();

            _genericParameters = new GenericParameterCollection(this);

            for (final GenericParameter genericParameter : genericParameters) {
                _genericParameters.add(genericParameter);
            }

            _genericParameters.freeze();
        }

        UnresolvedType(final String packageName, final String name, final String shortName, final List<GenericParameter> genericParameters) {
            _packageName = VerifyArgument.notNull(packageName, "packageName");
            _name = VerifyArgument.notNull(name, "name");
            _shortName = shortName;

            _genericParameters = new GenericParameterCollection(this);

            for (final GenericParameter genericParameter : genericParameters) {
                _genericParameters.add(genericParameter);
            }
        }

        @Override
        public String getName() {
            return _name;
        }

        @Override
        public String getPackageName() {
            return _packageName;
        }

        public String getFullName() {
            if (_fullName == null) {
                _fullName = super.getFullName();
            }
            return _fullName;
        }

        @Override
        public String getErasedSignature() {
            if (_erasedSignature == null) {
                _erasedSignature = super.getErasedSignature();
            }
            return _erasedSignature;
        }

        @Override
        public String getSignature() {
            if (_signature == null) {
                _signature = super.getSignature();
            }
            return _signature;
        }

        public String getInternalName() {
            if (_internalName == null) {
                _internalName = super.getInternalName();
            }
            return _internalName;
        }

        @Override
        public <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
            return visitor.visitClassType(this, parameter);
        }

        @Override
        public String getSimpleName() {
            return _shortName != null ? _shortName : _name;
        }

        @Override
        public boolean isGenericDefinition() {
            return hasGenericParameters();
        }

        @Override
        public List<GenericParameter> getGenericParameters() {
            return _genericParameters;
        }

        @Override
        public TypeReference makeGenericType(final TypeReference... typeArguments) {
            VerifyArgument.noNullElementsAndNotEmpty(typeArguments, "typeArguments");

            final TypeReference[] adjustedTypeArguments = Arrays.copyOf(typeArguments, typeArguments.length);

            if (checkRecursive(this, ArrayUtilities.asUnmodifiableList(adjustedTypeArguments))) {
                for (int i = 0; i < adjustedTypeArguments.length; i++) {
                    final TypeReference t = adjustedTypeArguments[i];
                    adjustedTypeArguments[i] = t.isGenericType() ? t.getRawType() : t;
                }
            }

            return new UnresolvedGenericType(
                this,
                ArrayUtilities.asUnmodifiableList(adjustedTypeArguments)
            );
        }

        @Override
        public TypeDefinition resolve() {
            return _resolver.resolve(this);
        }

        @Override
        public FieldDefinition resolve(final FieldReference field) {
            return _resolver.resolve(field);
        }

        @Override
        public MethodDefinition resolve(final MethodReference method) {
            return _resolver.resolve(method);
        }

        @Override
        public TypeDefinition resolve(final TypeReference type) {
            return _resolver.resolve(type);
        }
    }

    private final class UnresolvedGenericType extends TypeReference implements IGenericInstance {
        private final TypeReference _genericDefinition;
        private final List<TypeReference> _typeParameters;

        private String _signature;

        UnresolvedGenericType(final TypeReference genericDefinition, final List<TypeReference> typeParameters) {
            _genericDefinition = genericDefinition;
            _typeParameters = typeParameters;
        }

        @Override
        public TypeReference getElementType() {
            return null;
        }

        @Override
        public <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
            return visitor.visitParameterizedType(this, parameter);
        }

        @Override
        public String getName() {
            return _genericDefinition.getName();
        }

        @Override
        public String getPackageName() {
            return _genericDefinition.getPackageName();
        }

        @Override
        public TypeReference getDeclaringType() {
            return _genericDefinition.getDeclaringType();
        }

        @Override
        public String getSimpleName() {
            return _genericDefinition.getSimpleName();
        }

        @Override
        public String getFullName() {
            return _genericDefinition.getFullName();
        }

        @Override
        public String getInternalName() {
            return _genericDefinition.getInternalName();
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
            return _genericDefinition.getErasedSignature();
        }

        @Override
        public boolean isGenericDefinition() {
            return false;
        }

        @Override
        public boolean isGenericType() {
            return true;
        }

        @Override
        public List<GenericParameter> getGenericParameters() {
            if (!_genericDefinition.isGenericDefinition()) {
                final TypeDefinition resolvedDefinition = _genericDefinition.resolve();

                if (resolvedDefinition != null) {
                    return resolvedDefinition.getGenericParameters();
                }
            }

            return _genericDefinition.getGenericParameters();
        }

        @Override
        public boolean hasTypeArguments() {
            return true;
        }

        @Override
        public List<TypeReference> getTypeArguments() {
            return _typeParameters;
        }

        @Override
        public IGenericParameterProvider getGenericDefinition() {
            return _genericDefinition;
        }

        @Override
        public TypeReference getUnderlyingType() {
            return _genericDefinition;
        }

        @Override
        public TypeDefinition resolve() {
            return _resolver.resolve(this);
        }

        @Override
        public FieldDefinition resolve(final FieldReference field) {
            return _resolver.resolve(field);
        }

        @Override
        public MethodDefinition resolve(final MethodReference method) {
            return _resolver.resolve(method);
        }

        @Override
        public TypeDefinition resolve(final TypeReference type) {
            return _resolver.resolve(type);
        }
    }
}

