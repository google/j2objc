/*
 * MetadataParser.java
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

import com.strobel.assembler.metadata.signatures.*;
import com.strobel.compilerservices.RuntimeHelpers;
import com.strobel.core.ArrayUtilities;
import com.strobel.core.SafeCloseable;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.util.EmptyArrayCache;

import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mike Strobel
 */
public final class MetadataParser {
    private final static ThreadLocal<MetadataParser> THREAD_UNBOUND_PARSERS = new ThreadLocal<MetadataParser>() {
        @Override
        protected MetadataParser initialValue() {
            return new MetadataParser(IMetadataResolver.EMPTY);
        }
    };

    public static MetadataParser unbound() {
        return THREAD_UNBOUND_PARSERS.get();
    }

    private final IMetadataResolver _resolver;
    private final SignatureParser _signatureParser;
    private final Stack<IGenericContext> _genericContexts;
    private final CoreMetadataFactory _factory;
    private final AtomicInteger _suppressResolveDepth;

    public MetadataParser() {
        this(MetadataSystem.instance());
    }

    public MetadataParser(final IMetadataResolver resolver) {
        _resolver = VerifyArgument.notNull(resolver, "resolver");
        _signatureParser = SignatureParser.make();
        _genericContexts = new Stack<>();
        _factory = CoreMetadataFactory.make(resolver, new StackBasedGenericContext());
        _suppressResolveDepth = new AtomicInteger();
    }

    public MetadataParser(final TypeDefinition owner) {
        VerifyArgument.notNull(owner, "owner");

        _resolver = owner.getResolver() != null ? owner.getResolver() : MetadataSystem.instance();
        _signatureParser = SignatureParser.make();
        _genericContexts = new Stack<>();
        _factory = CoreMetadataFactory.make(owner, new StackBasedGenericContext());
        _suppressResolveDepth = new AtomicInteger();
    }

    public final SafeCloseable suppressTypeResolution() {
        _suppressResolveDepth.incrementAndGet();

        return new SafeCloseable() {
            @Override
            public void close() {
                _suppressResolveDepth.decrementAndGet();
            }
        };
    }

    private final class StackBasedGenericContext implements IGenericContext {
        @Override
        public GenericParameter findTypeVariable(final String name) {
            for (int i = _genericContexts.size() - 1; i >= 0; i--) {
                final IGenericContext context = _genericContexts.get(i);
                final GenericParameter typeVariable = context.findTypeVariable(name);

                if (typeVariable != null) {
                    return typeVariable;
                }
            }

            if (_resolver instanceof IGenericContext) {
                return ((IGenericContext) _resolver).findTypeVariable(name);
            }

            return null;
        }
    }

    public final IMetadataResolver getResolver() {
        return _resolver;
    }

    public void pushGenericContext(final IGenericContext context) {
        _genericContexts.push(VerifyArgument.notNull(context, "context"));
    }

    public void popGenericContext() {
        _genericContexts.pop();
    }

    public TypeReference parseTypeDescriptor(final String descriptor) {
        VerifyArgument.notNull(descriptor, "descriptor");

        if (descriptor.startsWith("[")) {
            return parseTypeSignature(descriptor);
        }

        return parseTypeSignature("L" + descriptor + ";");
    }

    public TypeReference parseTypeSignature(final String signature) {
        VerifyArgument.notNull(signature, "signature");

        final TypeSignature typeSignature = _signatureParser.parseTypeSignature(signature);
        final Reifier reifier = Reifier.make(_factory);

        typeSignature.accept(reifier);

        return reifier.getResult();
    }

    public FieldReference parseField(final TypeReference declaringType, final String name, final String signature) {
        VerifyArgument.notNull(declaringType, "declaringType");
        VerifyArgument.notNull(name, "name");
        VerifyArgument.notNull(signature, "signature");

        pushGenericContext(declaringType);

        try {
            return new UnresolvedField(
                declaringType,
                name,
                parseTypeSignature(signature)
            );
        }
        finally {
            popGenericContext();
        }
    }

    public MethodReference parseMethod(final TypeReference declaringType, final String name, final String descriptor) {
        VerifyArgument.notNull(declaringType, "declaringType");
        VerifyArgument.notNull(name, "name");
        VerifyArgument.notNull(descriptor, "descriptor");

        pushGenericContext(declaringType);

        try {
            final IMethodSignature signature = parseMethodSignature(descriptor);
            return lookupMethod(declaringType, name, signature);
        }
        finally {
            popGenericContext();
        }
    }

    public TypeReference lookupType(final String packageName, final String typeName) {
//        final TypeReference reference = new UnresolvedType(packageName, typeName);

        final String dottedName;

        if (StringUtilities.isNullOrEmpty(packageName)) {
            dottedName = typeName;
        }
        else {
            dottedName = packageName + "." + typeName;
        }

        final TypeReference reference = _factory.makeNamedType(dottedName);

        if (_suppressResolveDepth.get() > 0) {
            return reference;
        }

//        final TypeReference resolved = _resolver.resolve(reference);
//
//        return resolved != null ? resolved : reference;
        return reference;
    }

/*
    public TypeReference lookupType(final TypeReference declaringType, final String typeName) {
        final TypeReference reference = new UnresolvedType(declaringType, typeName);

        if (_suppressResolveDepth.get() > 0) {
            return reference;
        }

//        final TypeReference resolved = _resolver.resolve(reference);
//
//        return resolved != null ? resolved : reference;
        return reference;
    }
*/

    protected TypeReference lookupTypeVariable(final String name) {
        for (int i = 0, n = _genericContexts.size(); i < n; i++) {
            final IGenericContext context = _genericContexts.get(i);
            final TypeReference typeVariable = context.findTypeVariable(name);

            if (typeVariable != null) {
                return typeVariable;
            }
        }

        if (_resolver instanceof IGenericContext) {
            return ((IGenericContext) _resolver).findTypeVariable(name);
        }

        return null;
    }

    @SuppressWarnings("ConstantConditions")
    public IMethodSignature parseMethodSignature(final String signature) {
        VerifyArgument.notNull(signature, "signature");

        final MethodTypeSignature methodTypeSignature = _signatureParser.parseMethodSignature(signature);
        final Reifier reifier = Reifier.make(_factory);

        final TypeReference returnType;
        final List<TypeReference> parameterTypes;
        final List<GenericParameter> genericParameters;
        final List<TypeReference> thrownTypes;

        final ReturnType returnTypeSignature = methodTypeSignature.getReturnType();
        final TypeSignature[] parameterTypeSignatures = methodTypeSignature.getParameterTypes();
        final FormalTypeParameter[] genericParameterSignatures = methodTypeSignature.getFormalTypeParameters();
        final FieldTypeSignature[] thrownTypeSignatures = methodTypeSignature.getExceptionTypes();

        boolean needPopGenericContext = false;

        try {
            if (ArrayUtilities.isNullOrEmpty(genericParameterSignatures)) {
                genericParameters = Collections.emptyList();
            }
            else {
                final GenericParameter[] gp = new GenericParameter[genericParameterSignatures.length];

                pushGenericContext(
                    new IGenericContext() {
                        @Override
                        public GenericParameter findTypeVariable(final String name) {
                            for (final GenericParameter g : gp) {
                                if (g == null) {
                                    break;
                                }

                                if (StringUtilities.equals(g.getName(), name)) {
                                    return g;
                                }
                            }
                            return null;
                        }
                    }
                );

                needPopGenericContext = true;

                //
                // Reify generic parameters in two passes so that if a parameter has bounds depending
                // on a successor parameter, the successor can be resolved.
                //

                for (int i = 0; i < gp.length; i++) {
                    gp[i] = _factory.makeTypeVariable(
                        genericParameterSignatures[i].getName(),
                        EmptyArrayCache.fromElementType(FieldTypeSignature.class)
                    );
                }

                genericParameters = ArrayUtilities.asUnmodifiableList(gp);

                for (int i = 0; i < gp.length; i++) {
                    final FieldTypeSignature[] bounds = genericParameterSignatures[i].getBounds();

                    if (!ArrayUtilities.isNullOrEmpty(bounds)) {
                        gp[i].setExtendsBound(_factory.makeTypeBound(bounds));
                    }
                }
            }

            returnTypeSignature.accept(reifier);
            returnType = reifier.getResult();

            if (ArrayUtilities.isNullOrEmpty(parameterTypeSignatures)) {
                parameterTypes = Collections.emptyList();
            }
            else {
                final TypeReference[] pt = new TypeReference[parameterTypeSignatures.length];

                for (int i = 0; i < pt.length; i++) {
                    parameterTypeSignatures[i].accept(reifier);
                    pt[i] = reifier.getResult();
                }

                parameterTypes = ArrayUtilities.asUnmodifiableList(pt);
            }

            if (ArrayUtilities.isNullOrEmpty(thrownTypeSignatures)) {
                thrownTypes = Collections.emptyList();
            }
            else {
                final TypeReference[] tt = new TypeReference[thrownTypeSignatures.length];

                for (int i = 0; i < tt.length; i++) {
                    thrownTypeSignatures[i].accept(reifier);
                    tt[i] = reifier.getResult();
                }

                thrownTypes = ArrayUtilities.asUnmodifiableList(tt);
            }

            return _factory.makeMethodSignature(returnType, parameterTypes, genericParameters, thrownTypes);
        }
        finally {
            if (needPopGenericContext) {
                popGenericContext();
            }
        }
    }

    public IClassSignature parseClassSignature(final String signature) {
        VerifyArgument.notNull(signature, "signature");

        final ClassSignature classSignature = _signatureParser.parseClassSignature(signature);
        final Reifier reifier = Reifier.make(_factory);

        final TypeReference baseType;
        final List<TypeReference> interfaceTypes;
        final List<GenericParameter> genericParameters;

        final ClassTypeSignature baseTypeSignature = classSignature.getSuperType();
        final ClassTypeSignature[] interfaceTypeSignatures = classSignature.getInterfaces();
        final FormalTypeParameter[] genericParameterSignatures = classSignature.getFormalTypeParameters();

        boolean needPopGenericContext = false;

        try {
            if (ArrayUtilities.isNullOrEmpty(genericParameterSignatures)) {
                genericParameters = Collections.emptyList();
            }
            else {
                final GenericParameter[] gp = new GenericParameter[genericParameterSignatures.length];

                pushGenericContext(
                    new IGenericContext() {
                        @Override
                        public GenericParameter findTypeVariable(final String name) {
                            for (final GenericParameter g : gp) {
                                if (g == null) {
                                    break;
                                }

                                if (StringUtilities.equals(g.getName(), name)) {
                                    return g;
                                }
                            }
                            return null;
                        }
                    }
                );

                needPopGenericContext = true;

                //
                // Reify generic parameters in two passes so that if a parameter has bounds depending
                // on a successor parameter, the successor can be resolved.
                //

                for (int i = 0; i < gp.length; i++) {
                    gp[i] = _factory.makeTypeVariable(
                        genericParameterSignatures[i].getName(),
                        EmptyArrayCache.fromElementType(FieldTypeSignature.class)
                    );
                }

                genericParameters = ArrayUtilities.asUnmodifiableList(gp);

                for (int i = 0; i < gp.length; i++) {
                    final FieldTypeSignature[] bounds = genericParameterSignatures[i].getBounds();

                    if (!ArrayUtilities.isNullOrEmpty(bounds)) {
                        gp[i].setExtendsBound(_factory.makeTypeBound(bounds));
                    }
                }
            }

            baseTypeSignature.accept(reifier);
            baseType = reifier.getResult();

            if (ArrayUtilities.isNullOrEmpty(interfaceTypeSignatures)) {
                interfaceTypes = Collections.emptyList();
            }
            else {
                final TypeReference[] it = new TypeReference[interfaceTypeSignatures.length];

                for (int i = 0; i < it.length; i++) {
                    interfaceTypeSignatures[i].accept(reifier);
                    it[i] = reifier.getResult();
                }

                interfaceTypes = ArrayUtilities.asUnmodifiableList(it);
            }

            return _factory.makeClassSignature(baseType, interfaceTypes, genericParameters);
        }
        finally {
            if (needPopGenericContext) {
                popGenericContext();
            }
        }
    }

    protected MethodReference lookupMethod(final TypeReference declaringType, final String name, final IMethodSignature signature) {
        final MethodReference reference = new UnresolvedMethod(
            declaringType,
            name,
            signature
        );

        if (_suppressResolveDepth.get() > 0) {
            return reference;
        }

//        final MethodReference resolved = _resolver.resolve(reference);
//
//        return resolved != null ? resolved : reference;
        return reference;
    }

    // <editor-fold defaultstate="collapsed" desc="Primitive Lookup">

    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private final static TypeReference[] PRIMITIVE_TYPES = new TypeReference[16];

    static {
        RuntimeHelpers.ensureClassInitialized(MetadataSystem.class);

        final TypeReference[] allPrimitives = {
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

        for (final TypeReference t : allPrimitives) {
            PRIMITIVE_TYPES[hashPrimitiveName(t.getName())] = t;
        }
    }

    private static int hashPrimitiveName(final String name) {
        if (name.length() < 3) {
            return 0;
        }
        return (name.charAt(0) + name.charAt(2)) % 16;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="UnresolvedMethod Class">

    private final class UnresolvedMethod extends MethodReference {
        private final TypeReference _declaringType;
        private final String _name;
        private final IMethodSignature _signature;
        private final List<GenericParameter> _genericParameters;

        UnresolvedMethod(final TypeReference declaringType, final String name, final IMethodSignature signature) {
            _declaringType = VerifyArgument.notNull(declaringType, "declaringType");
            _name = VerifyArgument.notNull(name, "name");
            _signature = VerifyArgument.notNull(signature, "signature");

            if (_signature.hasGenericParameters()) {
                final GenericParameterCollection genericParameters = new GenericParameterCollection(this);

                for (final GenericParameter genericParameter : _signature.getGenericParameters()) {
                    genericParameters.add(genericParameter);
                }

                genericParameters.freeze(false);

                _genericParameters = genericParameters;
            }
            else {
                _genericParameters = Collections.emptyList();
            }
        }

        @Override
        public String getName() {
            return _name;
        }

        @Override
        public TypeReference getReturnType() {
            return _signature.getReturnType();
        }

        @Override
        public List<ParameterDefinition> getParameters() {
            return _signature.getParameters();
        }

        @Override
        public TypeReference getDeclaringType() {
            return _declaringType;
        }

        @Override
        public List<GenericParameter> getGenericParameters() {
            return _genericParameters;
        }

        @Override
        public List<TypeReference> getThrownTypes() {
            return _signature.getThrownTypes();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="UnresolvedField Class">

    private final class UnresolvedField extends FieldReference {
        private final TypeReference _declaringType;
        private final String _name;
        private final TypeReference _fieldType;

        UnresolvedField(final TypeReference declaringType, final String name, final TypeReference fieldType) {
            _declaringType = VerifyArgument.notNull(declaringType, "declaringType");
            _name = VerifyArgument.notNull(name, "name");
            _fieldType = VerifyArgument.notNull(fieldType, "fieldType");
        }

        @Override
        public String getName() {
            return _name;
        }

        @Override
        public TypeReference getDeclaringType() {
            return _declaringType;
        }

        @Override
        public TypeReference getFieldType() {
            return _fieldType;
        }

        @Override
        protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
            if (fullName) {
                final TypeReference declaringType = getDeclaringType();

                if (declaringType != null) {
                    return declaringType.appendName(sb, true, false).append('.').append(_name);
                }
            }

            return sb.append(_name);
        }
    }

/*
    private final class UnresolvedGenericType extends TypeReference implements IGenericInstance {
        private final TypeReference _genericDefinition;
        private final List<TypeReference> _typeParameters;

        UnresolvedGenericType(final TypeReference genericDefinition, final List<TypeReference> typeParameters) {
            _genericDefinition = genericDefinition;
            _typeParameters = typeParameters;
        }

        @Override
        public TypeReference getElementType() {
            return null;
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
        public boolean isGenericDefinition() {
            return false;
        }

        @Override
        public List<GenericParameter> getGenericParameters() {
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
*/

/*
    private final class UnresolvedType extends TypeReference {
        private final String _name;
        private final String _packageName;
        private final TypeReference _declaringType;
        private final GenericParameterCollection _genericParameters;
        private String _fullName;
        private String _internalName;

        UnresolvedType(final TypeReference declaringType, final String name) {
            _name = VerifyArgument.notNull(name, "name");
            _packageName = StringUtilities.EMPTY;
            _declaringType = VerifyArgument.notNull(declaringType, "declaringType");
            _genericParameters = new GenericParameterCollection(this);
        }

        UnresolvedType(final String packageName, final String name) {
            _packageName = VerifyArgument.notNull(packageName, "packageName");
            _name = VerifyArgument.notNull(name, "name");
            _declaringType = null;
            _genericParameters = new GenericParameterCollection(this);
        }

        UnresolvedType(final TypeReference declaringType, final String name, final List<GenericParameter> genericParameters) {
            _name = VerifyArgument.notNull(name, "name");
            _packageName = StringUtilities.EMPTY;
            _declaringType = VerifyArgument.notNull(declaringType, "declaringType");

            _genericParameters = new GenericParameterCollection(this);

            for (final GenericParameter genericParameter : genericParameters) {
                _genericParameters.add(genericParameter);
            }
        }

        UnresolvedType(final String packageName, final String name, final List<GenericParameter> genericParameters) {
            _packageName = VerifyArgument.notNull(packageName, "packageName");
            _name = VerifyArgument.notNull(name, "name");
            _declaringType = null;

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
                final StringBuilder name = new StringBuilder();
                appendName(name, true, true);
                _fullName = name.toString();
            }
            return _fullName;
        }

        public String getInternalName() {
            if (_internalName == null) {
                final StringBuilder name = new StringBuilder();
                appendName(name, true, false);
                _internalName = name.toString();
            }
            return _internalName;
        }

        @Override
        public boolean isAnonymous() {
            return _declaringType != null && _shortName == null;
        }

        @Override
        public TypeReference getDeclaringType() {
            return _declaringType;
        }

        @Override
        public String getSimpleName() {
            return _name;
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
        public TypeReference makeGenericType(final List<TypeReference> typeArguments) {
            VerifyArgument.notEmpty(typeArguments, "typeArguments");
            VerifyArgument.noNullElements(typeArguments, "typeArguments");

//            final TypeDefinition resolved = this.resolve();

            return new UnresolvedGenericType(
//                resolved != null ? resolved : this,
                this,
                ArrayUtilities.asUnmodifiableList(typeArguments.toArray(new TypeReference[typeArguments.size()]))
            );
        }

        @Override
        public TypeReference makeGenericType(final TypeReference... typeArguments) {
            VerifyArgument.notEmpty(typeArguments, "typeArguments");
            VerifyArgument.noNullElements(typeArguments, "typeArguments");

//            final TypeDefinition resolved = this.resolve();

            return new UnresolvedGenericType(
//                resolved != null ? resolved : this,
                this,
                ArrayUtilities.asUnmodifiableList(typeArguments.clone())
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
*/

    // </editor-fold>
}
