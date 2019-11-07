/*
 * TypeBuilder.java
 *
 * Copyright (c) 2012 Mike Strobel
 *
 * This source code is subject to terms and conditions of the Apache License, Version 2.0.
 * A copy of the license can be found in the License.html file at the root of this distribution.
 * By using this source code in any fashion, you are agreeing to be bound by the terms of the
 * Apache License, Version 2.0.
 *
 * You must not remove this notice, or any other, from this software.
 */

package com.strobel.reflection.emit;

import com.strobel.annotations.NotNull;
import com.strobel.compilerservices.CallerResolver;
import com.strobel.compilerservices.RuntimeHelpers;
import com.strobel.core.ArrayUtilities;
import com.strobel.core.ExceptionUtilities;
import com.strobel.core.Pair;
import com.strobel.core.ReadOnlyList;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.io.PathHelper;
import com.strobel.reflection.*;
import com.strobel.util.EmptyArrayCache;
import com.strobel.util.TypeUtils;
import sun.misc.Unsafe;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.*;

/**
 * @author strobelm
 */
@SuppressWarnings({ "unchecked", "PackageVisibleField" })
public final class TypeBuilder<T> extends Type<T> {
    private final static String DumpGeneratedClassesProperty = "com.strobel.reflection.emit.TypeBuilder.DumpGeneratedClasses";
    private final static String GeneratedClassOutputPathProperty = "com.strobel.reflection.emit.TypeBuilder.GeneratedClassOutputPath";
    private final static String VerifyGeneratedClassesProperty = "com.strobel.reflection.emit.TypeBuilder.VerifyGeneratedClasses";

    final ConstantPool constantPool;
    final ArrayList<ConstructorBuilder> constructorBuilders;
    final ArrayList<MethodBuilder> methodBuilders;
    final ArrayList<FieldBuilder> fieldBuilders;
    final ArrayList<GenericParameterBuilder<?>> genericParameterBuilders;
    final ArrayList<MethodOverride> methodOverrides;

    private String _name;
    private String _fullName;
    private String _internalName;
    private Package _package;
    private Type<? super T> _baseType;
    private ConstructorList _constructors;
    private MethodList _methods;
    private FieldList _fields;
    private TypeList _interfaces;
    private TypeBuilder _declaringType;
    private MethodBuilder _declaringMethod;
    private int _modifiers;
    private boolean _hasBeenCreated;
    private Class<T> _generatedClass;
    private Type<T> _generatedType;
    private Type<?> _extendsBound;

    private int _genericParameterPosition;
    private boolean _isGenericParameter;
    private boolean _isGenericTypeDefinition;
    private TypeBindings _typeBindings;
    private ReadOnlyList<AnnotationBuilder<? extends Annotation>> _annotations;
    private Map<Class<? extends Annotation>, AnnotationBuilder<? extends Annotation>> _annotationMap;
    private final ProtectionDomain _protectionDomain;

    // <editor-fold defaultstate="collapsed" desc="Constructors and Initializers">

    public TypeBuilder(final String name, final int modifiers) {
        this(name, modifiers, Types.Object, TypeList.empty());
    }

    public TypeBuilder(
        final String name,
        final int modifiers,
        final Type baseType,
        final TypeList interfaces) {

        this();

        initialize(
            name,
            modifiers,
            baseType,
            interfaces,
            null
        );
    }

    TypeBuilder() {
        this.constantPool = new ConstantPool();
        this.constructorBuilders = new ArrayList<>();
        this.methodBuilders = new ArrayList<>();
        this.fieldBuilders = new ArrayList<>();
        this.genericParameterBuilders = new ArrayList<>();
        this.methodOverrides = new ArrayList<>();

        _constructors = ConstructorList.empty();
        _methods = MethodList.empty();
        _fields = FieldList.empty();
        _typeBindings = TypeBindings.empty();
        _annotations = ReadOnlyList.emptyList();
        _protectionDomain = CallerResolver.getCallerClass(1).getProtectionDomain();
    }

    TypeBuilder(final String name, final int genericParameterPosition, final TypeBuilder declaringType) {
        this();

        _declaringType = VerifyArgument.notNull(declaringType, "declaringType");

        initializeAsGenericParameter(
            VerifyArgument.notNull(name, "name"),
            VerifyArgument.isNonNegative(genericParameterPosition, "genericParameterPosition")
        );
    }

    TypeBuilder(final String name, final int genericParameterPosition, final MethodBuilder declaringMethod) {
        this();

        _declaringMethod = VerifyArgument.notNull(declaringMethod, "declaringMethod");
        _declaringType = _declaringMethod.getDeclaringType();

        initializeAsGenericParameter(
            VerifyArgument.notNull(name, "name"),
            VerifyArgument.isNonNegative(genericParameterPosition, "genericParameterPosition")
        );
    }

    TypeBuilder(final String name, final int modifiers, final Type baseType, final TypeBuilder declaringType) {
        this();

        initialize(
            name,
            modifiers,
            baseType,
            TypeList.empty(),
            declaringType
        );
    }

    TypeBuilder(
        final String name,
        final int modifiers,
        final Type<? super T> baseType,
        final TypeList interfaces,
        final TypeBuilder declaringType) {

        this();

        initialize(
            name,
            modifiers,
            baseType,
            interfaces,
            declaringType
        );
    }

    private void initializeAsGenericParameter(final String name, final int position) {
        _name = name;
        _fullName = name;
        _internalName = name.replace('.', '/');
        _genericParameterPosition = position;
        _isGenericParameter = true;
        _isGenericTypeDefinition = false;
        _interfaces = TypeList.empty();
    }

    private void initialize(
        final String fullName,
        final int modifiers,
        final Type baseType,
        final TypeList interfaces,
        final TypeBuilder declaringType) {

        VerifyArgument.notNullOrWhitespace(fullName, "fullName");

        if (fullName.length() > 1023) {
            throw Error.typeNameTooLong();
        }

        _fullName = fullName;
        _internalName = fullName.replace('.', '/');
        _isGenericTypeDefinition = false;
        _isGenericParameter = false;
        _hasBeenCreated = false;
        _declaringType = declaringType;

        final int lastDotIndex = fullName.lastIndexOf('.');

        if (lastDotIndex == -1 || lastDotIndex == 0) {
            _package = Package.getPackage(StringUtilities.EMPTY);
            _name = _fullName;
        }
        else {
            _package = Package.getPackage(fullName.substring(0, lastDotIndex));
            _name = fullName.substring(lastDotIndex + 1);
        }

        if (Modifier.isInterface(modifiers)) {
            _modifiers = modifiers & (Modifier.interfaceModifiers() | Modifier.INTERFACE) | Modifier.ABSTRACT;
        }
        else {
            _modifiers = modifiers & Modifier.classModifiers();
        }

        setBaseType(baseType);
        setInterfaces(interfaces);
    }

    public final void setInterfaces(final TypeList interfaces) {
        verifyNotCreated();

        if (interfaces == null) {
            _interfaces = TypeList.empty();
        }
        else {
            for (final Type<?> ifType : interfaces) {
                if (!ifType.isInterface()) {
                    throw Error.typeMustBeInterface(ifType);
                }
                if (this.isEquivalentTo(ifType)) {
                    throw Error.typeCannotHaveItselfAsInterface();
                }
            }
            _interfaces = interfaces;
        }

        updateExtendsBound();
        invalidateCaches();
    }

    public final void setBaseType(final Type baseType) {
        verifyNotGeneric();
        verifyNotCreated();

        if (baseType != null) {
            if (baseType.isInterface()) {
                throw Error.baseTypeCannotBeInterface();
            }
            if (baseType.isGenericParameter() && !this.isGenericParameter()) {
                throw Error.baseTypeCannotBeGenericParameter();
            }
            if (this.isEquivalentTo(baseType)) {
                throw Error.typeCannotHaveItselfAsBaseType();
            }
            _baseType = baseType;
            updateExtendsBound();
            return;
        }

        if (Modifier.isInterface(_modifiers)) {
            _baseType = null;
        }
        else {
            _baseType = Types.Object;
        }

        updateExtendsBound();
        invalidateCaches();
    }

    private void updateExtendsBound() {
        if (!isGenericParameter()) {
            return;
        }

        _extendsBound = _baseType == Types.Object ? null : _baseType;

        if (_interfaces.isEmpty()) {
            return;
        }

        if (_interfaces.size() == 1 && _extendsBound == null) {
            _extendsBound = _interfaces.get(0);
            return;
        }

        _extendsBound = Type.makeCompoundType(_baseType, _interfaces);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Assertions">

    final void verifyNotCreated() {
        if (isCreated()) {
            throw Error.typeHasBeenCreated();
        }
    }

    final void verifyCreated() {
        if (!isCreated()) {
            throw Error.typeHasNotBeenCreated();
        }
    }

    final void verifyNotGeneric() {
        //noinspection ConstantConditions
        if (isGenericType() && !isGenericTypeDefinition()) {
            throw new IllegalStateException();
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Type Information Overrides">

    @Override
    public Package getPackage() {
        return _package;
    }

    @Override
    public Type<?> getReflectedType() {
        return _declaringType;
    }

    @Override
    public MethodBase getDeclaringMethod() {
        return _declaringMethod;
    }

    @Override
    protected String getClassSimpleName() {
        return _name;
    }

    @Override
    protected String getClassFullName() {
        return _fullName;
    }

    @Override
    public String getShortName() {
        return _name;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String getFullName() {
        return _fullName;
    }

    @Override
    public String getInternalName() {
        return _internalName;
    }

    @Override
    public StringBuilder appendErasedDescription(final StringBuilder sb) {
        if (isGenericParameter()) {
            return getExtendsBound().appendErasedDescription(sb);
        }
        return super.appendErasedDescription(sb);
    }

    @Override
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        if (isGenericParameter()) {
            return getExtendsBound().appendErasedSignature(sb);
        }
        return super.appendErasedSignature(sb);
    }

    @Override
    public StringBuilder appendSignature(final StringBuilder sb) {
        return super.appendSignature(sb);
    }

    @Override
    public Type<? super T> getBaseType() {
        return _baseType;
    }

    @Override
    public TypeList getExplicitInterfaces() {
        return _interfaces;
    }

    @Override
    public Type<?> getDeclaringType() {
        return _declaringType;
    }

    @Override
    public int getModifiers() {
        return _modifiers;
    }

    @Override
    public boolean isEquivalentTo(final Type<?> other) {
        if (other == this) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other instanceof GenericParameterBuilder<?>) {
            return ((GenericParameterBuilder) other).typeBuilder == this;
        }

        final Type<?> runtimeType = other instanceof TypeBuilder<?>
                                    ? ((TypeBuilder) other)._generatedType
                                    : other;

        return _generatedType != null &&
               runtimeType != null &&
               _generatedType.isEquivalentTo(runtimeType);
    }

    @Override
    public boolean isInstance(final Object o) {
        return _hasBeenCreated &&
               _generatedClass.isInstance(o);
    }

    @Override
    public boolean isGenericParameter() {
        return _isGenericParameter;
    }

    @Override
    public boolean isGenericType() {
        return _isGenericTypeDefinition;
    }

    @Override
    public boolean isGenericTypeDefinition() {
        return _isGenericTypeDefinition;
    }

    @Override
    public int getGenericParameterPosition() {
        if (isGenericParameter()) {
            return _genericParameterPosition;
        }
        return super.getGenericParameterPosition();
    }

    @Override
    public Type<?> getGenericTypeDefinition() {
        if (isGenericTypeDefinition()) {
            return this;
        }
        throw Error.notGenericType(this);
    }

    @Override
    protected TypeBindings getTypeBindings() {
        return _typeBindings;
    }

    @Override
    public Type<?> getExtendsBound() {
        if (_isGenericParameter) {
            return _extendsBound != null ? _extendsBound : Types.Object;
        }
        return super.getExtendsBound();
    }

    @Override
    public boolean isAssignableFrom(final Type type) {
        if (_hasBeenCreated) {
            return _generatedType.isAssignableFrom(type);
        }
        return super.isAssignableFrom(type);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Member Information Overrides">

    @Override
    public ConstructorInfo getConstructor(
        final Set<BindingFlags> bindingFlags,
        final CallingConvention callingConvention,
        final Type... parameterTypes) {

        verifyCreated();
        return _generatedType.getConstructor(bindingFlags, callingConvention, parameterTypes);
    }

    @Override
    public ConstructorList getConstructors(final Set<BindingFlags> bindingFlags) {
        verifyCreated();
        return _generatedType.getConstructors(bindingFlags);
    }

    @Override
    public MemberList getMembers(final Set<BindingFlags> bindingFlags, final Set<MemberType> memberTypes) {
        verifyCreated();
        return _generatedType.getMembers(bindingFlags, memberTypes);
    }

    @Override
    public MemberList getMember(final String name, final Set<BindingFlags> bindingFlags, final Set<MemberType> memberTypes) {
        verifyCreated();
        return _generatedType.getMember(name, bindingFlags, memberTypes);
    }

    @Override
    public MethodInfo getMethod(
        final String name,
        final Set<BindingFlags> bindingFlags,
        final CallingConvention callingConvention,
        final Type... parameterTypes) {

        verifyCreated();
        return _generatedType.getMethod(name, bindingFlags, callingConvention, parameterTypes);
    }

    @Override
    public MethodList getMethods(final Set<BindingFlags> bindingFlags, final CallingConvention callingConvention) {
        verifyCreated();
        return _generatedType.getMethods(bindingFlags, callingConvention);
    }

    @Override
    public Type<?> getNestedType(final String fullName, final Set<BindingFlags> bindingFlags) {
        verifyCreated();
        return _generatedType.getNestedType(fullName, bindingFlags);
    }

    @Override
    public TypeList getNestedTypes(final Set<BindingFlags> bindingFlags) {
        verifyCreated();
        return _generatedType.getNestedTypes(bindingFlags);
    }

    @Override
    public FieldList getFields(final Set<BindingFlags> bindingFlags) {
        verifyCreated();
        return _generatedType.getFields(bindingFlags);
    }

    @Override
    public FieldInfo getField(final String name, final Set<BindingFlags> bindingFlags) {
        verifyCreated();
        return _generatedType.getField(name, bindingFlags);
    }

    @Override
    public Class<T> getErasedClass() {
        verifyCreated();
        return _generatedClass;
    }

    @Override
    public <P, R> R accept(final TypeVisitor<P, R> visitor, final P parameter) {
        if (isGenericParameter()) {
            return visitor.visitTypeParameter(this, parameter);
        }
        return visitor.visitClassType(this, parameter);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Annotations">

    public void addCustomAnnotation(final AnnotationBuilder<? extends Annotation> annotation) {
        VerifyArgument.notNull(annotation, "annotation");

        final Class<? extends Annotation> annotationClass = annotation.getAnnotationType().getErasedClass();

        if (_annotationMap != null) {
            if (_annotationMap.containsKey(annotationClass)) {
                throw new IllegalArgumentException(
                    "@" + annotation.getAnnotationType().getShortName() + " is already defined."
                );
            }
        }
        else {
            _annotationMap = new HashMap<>();
        }

        final AnnotationBuilder[] newAnnotations = new AnnotationBuilder[this._annotations.size() + 1];
        _annotations.toArray(newAnnotations);
        newAnnotations[this._annotations.size()] = annotation;
        _annotations = new ReadOnlyList<AnnotationBuilder<? extends Annotation>>(newAnnotations);
        _annotationMap.put(annotationClass, annotation);
    }

    public ReadOnlyList<AnnotationBuilder<? extends Annotation>> getCustomAnnotations() {
        return _annotations;
    }

    @Override
    public <A extends Annotation> A getAnnotation(final Class<A> annotationClass) {
        if (isCreated()) {
            return _generatedType.getAnnotation(annotationClass);
        }
        else {
            final AnnotationBuilder<A> annotation = (AnnotationBuilder<A>) _annotationMap.get(annotationClass);

            if (annotation != null) {
                return annotation.getAnnotation();
            }

            return null;
        }
    }

    @NotNull
    @Override
    public Annotation[] getAnnotations() {
        if (isCreated()) {
            return _generatedType.getAnnotations();
        }
        else {
            final Type<? super T> baseType = getBaseType();

            final Annotation[] declaredAnnotations = bakeAnnotations();
            final Annotation[] ancestorAnnotations;

            if (baseType != null) {
                ancestorAnnotations = baseType.getAnnotations();
            }
            else {
                ancestorAnnotations = EmptyArrayCache.fromElementType(Annotation.class);
            }

            if (ancestorAnnotations.length == 0) {
                return declaredAnnotations;
            }

            final Set<Class<? extends Annotation>> annotationTypes = new HashSet<>();
            final List<Annotation> annotations = new ArrayList<>();

            for (final Annotation annotation : declaredAnnotations) {
                annotationTypes.add(annotation.annotationType());
            }

            for (final Annotation annotation : ancestorAnnotations) {
                if (annotationTypes.add(annotation.annotationType())) {
                    annotations.add(annotation);
                }
            }

            Collections.addAll(annotations, declaredAnnotations);

            return annotations.toArray(new Annotation[annotations.size()]);
        }
    }

    private Annotation[] bakeAnnotations() {
        final List<AnnotationBuilder<? extends Annotation>> annotationList = _annotations;

        if (annotationList.isEmpty()) {
            return EmptyArrayCache.fromElementType(Annotation.class);
        }

        final Annotation[] annotations = new Annotation[annotationList.size()];

        for (int i = 0; i < annotations.length; i++) {
            annotations[i] = annotationList.get(i).getAnnotation();
        }

        return annotations;
    }

    @NotNull
    @Override
    public Annotation[] getDeclaredAnnotations() {
        verifyCreated();
        return _generatedType.getDeclaredAnnotations();
    }

    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
        verifyCreated();
        return _generatedType.isAnnotationPresent(annotationClass);
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Type Manipulation">

    @Override
    protected Type<?> makeGenericTypeCore(final TypeList typeArguments) {
        if (!isGenericTypeDefinition()) {
            throw Error.notGenericTypeDefinition(this);
        }
        return TypeBuilderInstantiation.makeGenericType(this, typeArguments);
    }

    public boolean isCreated() {
        return _hasBeenCreated;
    }

    public synchronized Type<T> createType() {
        try {
            return createTypeNoLock(null);
        }
        catch (final IOException e) {
            throw ExceptionUtilities.asRuntimeException(e);
        }
    }

    public synchronized Type<T> createType(final File outputFile) {
        try (final FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            return createTypeNoLock(outputStream);
        }
        catch (final IOException e) {
            throw ExceptionUtilities.asRuntimeException(e);
        }
    }

    public synchronized Type<T> createType(final OutputStream outputStream) {
        try {
            return createTypeNoLock(outputStream);
        }
        catch (final IOException e) {
            throw ExceptionUtilities.asRuntimeException(e);
        }
    }

    public ConstructorBuilder defineConstructor(
        final int modifiers,
        final TypeList parameterTypes) {

        return defineConstructor(modifiers, parameterTypes, TypeList.empty());
    }

    public ConstructorBuilder defineConstructor(
        final int modifiers,
        final TypeList parameterTypes,
        final TypeList thrownTypes) {

        verifyNotGeneric();
        verifyNotCreated();

        final ConstructorBuilder constructor = new ConstructorBuilder(
            modifiers & Modifier.constructorModifiers(),
            parameterTypes,
            thrownTypes,
            this
        );

        constructorBuilders.add(constructor);
        _constructors = new ConstructorList(ArrayUtilities.append(_constructors.toArray(), constructor));

        return constructor;
    }

    public ConstructorBuilder defineDefaultConstructor() {
        return defineDefaultConstructor(isAbstract() ? Modifier.PROTECTED : Modifier.PUBLIC);
    }

    public ConstructorBuilder defineDefaultConstructor(final int modifiers) {
        verifyNotGeneric();
        verifyNotCreated();

        if (isInterface()) {
            throw Error.interfacesCannotDefineConstructors();
        }

        final ConstructorInfo baseConstructor = _baseType.getConstructor(BindingFlags.AllExact);

        if (baseConstructor == null || baseConstructor.isPrivate()) {
            throw Error.baseTypeHasNoDefaultConstructor(_baseType);
        }

        final ConstructorBuilder constructor = new ConstructorBuilder(
            modifiers & Modifier.constructorModifiers(),
            baseConstructor.getParameters().getParameterTypes(),
            TypeList.empty(),
            this
        );

        final CodeGenerator code = constructor.getCodeGenerator();

        code.emitThis();

        for (final ParameterInfo parameter : baseConstructor.getParameters()) {
            code.emitLoadArgument(parameter.getPosition());
        }

        code.call(baseConstructor);
        code.emitReturn();

        constructor.returnCodeGenerator = false;
        constructorBuilders.add(constructor);
        _constructors = new ConstructorList(ArrayUtilities.append(_constructors.toArray(), constructor));

        return constructor;
    }

    final void addMethodToList(final MethodBuilder methodBuilder) {
        methodBuilders.add(methodBuilder);
    }

    public void defineMethodOverride(final MethodInfo override, final MethodInfo baseMethod) {
        VerifyArgument.notNull(override, "override");
        VerifyArgument.notNull(baseMethod, "baseMethod");

        if (override.getDeclaringType() != this) {
            throw Error.methodBuilderBelongsToAnotherType();
        }

        if (override.isStatic() || baseMethod.isStatic()) {
            throw Error.staticInstanceMethodMismatch();
        }

        if (baseMethod.isFinal()) {
            throw Error.cannotOverrideFinalMethod();
        }

        if (!StringUtilities.equals(override.getName(), baseMethod.getName())) {
            throw Error.methodNameMismatch();
        }

        final int baseParameterCount;

        if (baseMethod instanceof MethodBuilder) {
            baseParameterCount = ((MethodBuilder) baseMethod).parameterBuilders.length;
        }
        else {
            baseParameterCount = baseMethod.getParameters().size();
        }

        final MethodBuilder overrideBuilder = (MethodBuilder) override;

        if (overrideBuilder.parameterBuilders.length != baseParameterCount) {
            throw Error.parameterCountMismatch();
        }

        if ((overrideBuilder.getReturnType() == PrimitiveTypes.Void) !=
            (baseMethod.getReturnType() == PrimitiveTypes.Void)) {

            throw Error.incompatibleReturnTypes();
        }

        verifyNotGeneric();
        verifyNotCreated();

        final Type<?> baseDeclaringType = baseMethod.getDeclaringType().isGenericType()
                                          ? baseMethod.getDeclaringType().getGenericTypeDefinition()
                                          : baseMethod.getDeclaringType().getErasedType();

        final MemberList<? extends MemberInfo> m = baseDeclaringType
            .findMembers(
                MemberType.methodsOnly(),
                BindingFlags.AllDeclared,
                RawMethodMatcher,
                baseMethod.getRawMethod()
            );

        assert m != null && m.size() == 1;

        final MethodInfo base = (MethodInfo) m.get(0);

        methodOverrides.add(new MethodOverride(overrideBuilder, base));
    }

    private static final MemberFilter RawMethodMatcher = new MemberFilter() {
        @Override
        public boolean apply(final MemberInfo m, final Object filterCriteria) {
            return ((MethodInfo) m).getRawMethod() == filterCriteria;
        }
    };

    public MethodBuilder defineMethod(
        final String name,
        final int modifiers) {

        return defineMethod(name, modifiers, PrimitiveTypes.Void, TypeList.empty(), TypeList.empty());
    }

    public MethodBuilder defineMethod(
        final String name,
        final int modifiers,
        final Type<?> returnType) {

        return defineMethod(name, modifiers, returnType, TypeList.empty(), TypeList.empty());
    }

    public MethodBuilder defineMethod(
        final String name,
        final int modifiers,
        final Type<?> returnType,
        final TypeList parameterTypes) {

        return defineMethod(name, modifiers, returnType, parameterTypes, TypeList.empty());
    }

    public MethodBuilder defineMethod(
        final String name,
        final int modifiers,
        final Type<?> returnType,
        final TypeList parameterTypes,
        final TypeList thrownTypes) {

        return defineMethodCore(
            name,
            modifiers & Modifier.methodModifiers(),
            returnType,
            parameterTypes,
            thrownTypes
        );
    }

    private MethodBuilder defineMethodCore(
        final String name,
        final int modifiers,
        final Type<?> returnType,
        final TypeList parameterTypes,
        final TypeList thrownTypes) {

        VerifyArgument.notNullOrWhitespace(name, "name");

        verifyNotGeneric();
        verifyNotCreated();

        final MethodBuilder method = new MethodBuilder(
            name,
            modifiers,
            returnType,
            parameterTypes,
            thrownTypes,
            this
        );

        methodBuilders.add(method);
        _methods = new MethodList(ArrayUtilities.append(_methods.toArray(), method));

        return method;
    }

    public MethodBuilder defineTypeInitializer() {
        return defineMethod(
            "<clinit>",
            Modifier.STATIC,
            PrimitiveTypes.Void
        );
    }

    public FieldBuilder defineConstant(
        final String name,
        final Type<?> type,
        final int modifiers,
        final Object constantValue) {

        VerifyArgument.notNullOrWhitespace(name, "name");
        VerifyArgument.notNull(constantValue, "constantValue");

        verifyNotGeneric();
        verifyNotCreated();

        return defineFieldCore(name, type, modifiers, constantValue);
    }

    public FieldBuilder defineField(
        final String name,
        final Type<?> type,
        final int modifiers) {

        return defineFieldCore(name, type, modifiers, null);
    }

    private FieldBuilder defineFieldCore(
        final String name,
        final Type<?> type,
        final int modifiers,
        final Object constantValue) {

        VerifyArgument.notNullOrWhitespace(name, "name");

        verifyNotGeneric();
        verifyNotCreated();

        if (constantValue != null &&
            !TypeUtils.isAutoUnboxed(Type.of(constantValue.getClass()))) {

            throw Error.valueMustBeConstant();
        }

        final FieldBuilder field = new FieldBuilder(
            this,
            name,
            type,
            modifiers & Modifier.fieldModifiers(),
            constantValue
        );

        fieldBuilders.add(field);
        _fields = new FieldList(ArrayUtilities.append(_fields.toArray(), field));

        return field;
    }

    public GenericParameterBuilder<?>[] defineGenericParameters(final String... names) {
        if (!_typeBindings.isEmpty()) {
            throw Error.defineGenericParametersAlreadyCalled();
        }

        VerifyArgument.notEmpty(names, "names");

        final String[] defensiveCopy = Arrays.copyOf(names, names.length);

        VerifyArgument.noNullElements(defensiveCopy, "names");

        _isGenericTypeDefinition = true;

        final GenericParameterBuilder<?>[] genericParameters = new GenericParameterBuilder<?>[names.length];

        for (int i = 0, n = names.length; i < n; i++) {
            genericParameters[i] = new GenericParameterBuilder<>(
                new TypeBuilder(names[i], i, this)
            );
        }

        Collections.addAll(genericParameterBuilders, genericParameters);

        _typeBindings = TypeBindings.createUnbound(Type.list(genericParameters));

        invalidateCaches();

        return genericParameters;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Constant Pool">

    short getTypeToken(final Type<?> type) {
        VerifyArgument.notNull(type, "type");
        return (short) (constantPool.getTypeInfo(erase(type)).index & 0xFFFF);
    }

    short getMethodToken(final MethodBase method) {
        VerifyArgument.notNull(method, "method");
        if (method.getDeclaringType().isInterface()) {
            return (short) (constantPool.getInterfaceMethodReference((MethodInfo) method).index & 0xFFFF);
        }
        return (short) (constantPool.getMethodReference(method).index & 0xFFFF);
    }

    short getFieldToken(final FieldInfo field) {
        VerifyArgument.notNull(field, "field");
        return (short) (constantPool.getFieldReference(field).index & 0xFFFF);
    }

    short getConstantToken(final int value) {
        return (short) (constantPool.getIntegerConstant(value).index & 0xFFFF);
    }

    short getConstantToken(final long value) {
        return (short) (constantPool.getLongConstant(value).index & 0xFFFF);
    }

    short getConstantToken(final float value) {
        return (short) (constantPool.getFloatConstant(value).index & 0xFFFF);
    }

    short getConstantToken(final double value) {
        return (short) (constantPool.getDoubleConstant(value).index & 0xFFFF);
    }

    short getStringToken(final String value) {
        return (short) (constantPool.getStringConstant(value).index & 0xFFFF);
    }

    short getUtf8StringToken(final String value) {
        return (short) (constantPool.getUtf8StringConstant(value).index & 0xFFFF);
    }

    private static Type<?> erase(final Type<?> t) {
        final Type<?> def = t.isGenericType() ? t.getGenericTypeDefinition() : t;
        return def.getErasedType();
    }

/*
    private static MethodBase erase(final MethodBase m) {
        if (m instanceof MethodInfo) {
            return ((MethodInfo) m).getErasedMethodDefinition();
        }

        if (!m.getDeclaringType().isGenericType()) {
            return m;
        }

        final Type<?> erasedType = erase(m.getDeclaringType());
        final Object rawMethod = ((ConstructorInfo) m).getRawConstructor();

        final MemberList<?> members =
            erasedType.findMembers(
                MemberType.constructorsOnly(),
                BindingFlags.AllDeclared,
                Type.FilterRawMember,
                rawMethod
            );

        if (!members.isEmpty()) {
            return (MethodBase) members.get(0);
        }

        throw ContractUtils.unreachable();
    }
*/

/*
    private static FieldInfo erase(final FieldInfo f) {
        if (!f.getDeclaringType().isGenericType()) {
            return f;
        }

        final Type<?> erasedType = erase(f.getDeclaringType());
        final Object rawField = f.getRawField();

        final MemberList<?> members =
            erasedType.findMembers(
                MemberType.fieldsOnly(),
                BindingFlags.AllDeclared,
                Type.FilterRawMember,
                rawField
            );

        if (!members.isEmpty()) {
            return (FieldInfo) members.get(0);
        }

        throw ContractUtils.unreachable();
    }
*/

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Type Generation Methods">

    @SuppressWarnings("ConstantConditions")
    private Type<T> createTypeNoLock(final OutputStream writeTo) throws IOException {
        if (isCreated()) {
            return _generatedType;
        }

        verifyNotGeneric();
        verifyNotCreated();

        if (StringUtilities.isTrue(System.getProperty(VerifyGeneratedClassesProperty, "false"))) {
            Verifier.verify(this);
        }

        if (isGenericParameter()) {
            _hasBeenCreated = true;
            for (final AnnotationBuilder annotation : _annotations) {
                annotation.bake();
            }
            return this;
        }
        else if (!genericParameterBuilders.isEmpty()) {
            for (int i = 0, n = genericParameterBuilders.size(); i < n; i++) {
                genericParameterBuilders.get(i).typeBuilder.createType();
            }
        }

        if (_constructors.size() == 0 && !isInterface()) {
            defineDefaultConstructor();
        }

        createBridgeMethods();

        byte[] body;

        final int methodCount = methodBuilders.size();

        for (int i = 0; i < methodCount; i++) {
            final MethodBuilder method = methodBuilders.get(i);

            if (method.isAbstract() && !this.isAbstract()) {
                throw Error.abstractMethodDeclaredOnNonAbstractType();
            }

            body = method.getBody();

            if (method.isAbstract()) {
                if (body != null) {
                    throw Error.abstractMethodCannotHaveBody();
                }
            }
            else {
                if (method.generator != null) {
                    method.createMethodBodyHelper(method.getCodeGenerator());
                    body = method.getBody();
                }

                if (body == null || body.length == 0) {
                    throw Error.methodHasEmptyBody(method);
                }
            }
        }

        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024)) {
            new ClassWriter(this).writeClass(outputStream);

            final String fullName = getClassFullName();
            final byte[] classBytes = outputStream.toByteArray();

            if (writeTo != null) {
                writeTo.write(classBytes);
            }
            else {
                try (final OutputStream tempStream = getDefaultOutputStream()) {
                    if (tempStream != null) {
                        tempStream.write(classBytes);
                    }
                }
            }

            _hasBeenCreated = true;

            _generatedClass = (Class<T>) getUnsafeInstance().defineClass(
                fullName,
                classBytes,
                0,
                classBytes.length,
                Thread.currentThread().getContextClassLoader(),
                _protectionDomain
            );

            RuntimeHelpers.ensureClassInitialized(_generatedClass);

            _generatedType = Type.of(_generatedClass);
        }
        catch (final Throwable t) {
            throw Error.classGenerationFailed(this, t);
        }
        finally {
            if (_generatedType != null) {
                updateMembersWithGeneratedReferences();
            }

            for (int i = 0; i < methodCount; i++) {
                methodBuilders.get(i).releaseBakedStructures();
            }
        }

        return _generatedType;
    }

    private void createBridgeMethods() {
        for (final MethodOverride methodOverride : methodOverrides) {
            if (isBridgeMethodNeeded(methodOverride)) {
                createBridgeMethod(methodOverride);
            }
        }
    }

    private void createBridgeMethod(final MethodOverride methodOverride) {
        final MethodInfo baseMethod = methodOverride.baseMethod;
        final MethodBuilder override = methodOverride.override;

        final TypeList parameterTypes = baseMethod.getParameters()
                                                  .getParameterTypes()
                                                  .getErasedTypes();

        final Type<?> returnType = baseMethod.getReturnType() == PrimitiveTypes.Void
                                   ? baseMethod.getReturnType()
                                   : baseMethod.getReturnType().getErasedType();

        final TypeList thrownTypes = baseMethod.getThrownTypes().getErasedTypes();

        final MethodBuilder bridge = defineMethodCore(
            override.getName(),
            (baseMethod.getModifiers() & ~Modifier.ABSTRACT) | Flags.ACC_BRIDGE | Flags.ACC_SYNTHETIC,
            returnType,
            parameterTypes,
            thrownTypes
        );

        final CodeGenerator code = bridge.getCodeGenerator();

        code.emitThis();

        for (int i = 0, parameterTypesSize = parameterTypes.size(); i < parameterTypesSize; i++) {
            final Type<?> s = parameterTypes.get(i);
            final Type<?> t = override.parameterBuilders[i].getParameterType().getErasedType();

            code.emitLoadArgument(i);
            code.emitConversion(s, t);
        }

        code.call(override);

        if (returnType != PrimitiveTypes.Void) {
            code.emitConversion(override.getReturnType().getErasedType(), returnType);
        }

        code.emitReturn(returnType);
    }

    private boolean isBridgeMethodNeeded(final MethodOverride methodOverride) {
        return isBridgeMethodNeeded(methodOverride.baseMethod, methodOverride.override);
    }

    static boolean isBridgeMethodNeeded(final MethodInfo baseMethod, final MethodInfo override) {
        final MethodInfo erasedBase = baseMethod.getErasedMethodDefinition();
        final MethodInfo erasedOverride = override.getErasedMethodDefinition();

        final Type<?> baseReturnType = erasedBase.getReturnType().getErasedType();
        final Type<?> overrideReturnType = erasedOverride.getReturnType().getErasedType();

        if ((baseReturnType == PrimitiveTypes.Void) !=
            (overrideReturnType == PrimitiveTypes.Void)) {

            throw Error.incompatibleReturnTypes();
        }

        if (!TypeUtils.areEquivalent(overrideReturnType, baseReturnType)) {
            return true;
        }

        final TypeList parameterTypes = (erasedOverride instanceof MethodBuilder)
                                        ? ((MethodBuilder) erasedOverride).getParameterTypes().getErasedTypes()
                                        : erasedOverride.getParameters().getParameterTypes().getErasedTypes();

        final TypeList baseParameters = (erasedBase instanceof MethodBuilder)
                                        ? ((MethodBuilder) erasedBase).getParameterTypes().getErasedTypes()
                                        : erasedBase.getParameters().getParameterTypes().getErasedTypes();

        if (baseParameters.size() != parameterTypes.size()) {
            throw Error.parameterCountMismatch();
        }

        for (int i = 0, n = parameterTypes.size(); i < n; i++) {
            final Type<?> t1 = parameterTypes.get(i);
            final Type<?> t2 = baseParameters.get(i);

            if (!t1.isEquivalentTo(t2)) {
                return true;
            }
        }

        return false;
    }

    private OutputStream getDefaultOutputStream() {
        if (!StringUtilities.isTrue(System.getProperty(DumpGeneratedClassesProperty, "false"))) {
            return null;
        }

        final String outputPathSetting = System.getProperty(GeneratedClassOutputPathProperty);

        final String outputDirectory = outputPathSetting != null
                                       ? PathHelper.getFullPath(outputPathSetting)
                                       : PathHelper.getTempPath();

        final String outputFile = PathHelper.combine(
            outputDirectory,
            getInternalName().replace('/', PathHelper.DirectorySeparator) + ".class"
        );

        final File temp = new File(outputFile);
        final File parentDirectory = temp.getParentFile();

        if (!parentDirectory.exists() && !parentDirectory.mkdirs()) {
            return null;
        }

        try {
            return new FileOutputStream(temp);
        }
        catch (final IOException e) {
            return null;
        }
    }

    private void updateMembersWithGeneratedReferences() {
        final FieldList generatedFields = _generatedType.getFields(BindingFlags.AllDeclared);
        final MethodList generatedMethods = _generatedType.getMethods(BindingFlags.AllDeclared);
        final ConstructorList generatedConstructors = _generatedType.getConstructors(BindingFlags.AllDeclared);
        final TypeList generatedTypeParameters = _isGenericTypeDefinition ? _generatedType.getGenericTypeParameters() : TypeList.empty();

        for (int i = 0, n = genericParameterBuilders.size(); i < n; i++) {
            genericParameterBuilders.get(i).typeBuilder._generatedType = generatedTypeParameters.get(i);
        }

        for (int i = 0, n = fieldBuilders.size(); i < n; i++) {
            fieldBuilders.get(i).generatedField = generatedFields.get(i);
        }

        final HashMap<TypeList, ConstructorInfo> constructorLookup = new HashMap<>();
        final HashMap<Pair<String, String>, MethodInfo> methodLookup = new HashMap<>();

        for (final ConstructorInfo constructor : generatedConstructors) {
            constructorLookup.put(
                constructor.getParameters().getParameterTypes(),
                constructor
            );

            constructor.getRawConstructor().setAccessible(true);
        }

        for (final MethodInfo method : generatedMethods) {
            if ((method.getModifiers() & Flags.ACC_BRIDGE) == 0) {
                methodLookup.put(
                    Pair.create(
                        method.getName(),
                        method.getErasedSignature()
                    ),
                    method
                );
            }
        }

        for (final ConstructorBuilder constructorBuilder : constructorBuilders) {
            constructorBuilder.generatedConstructor = constructorLookup.get(
                constructorBuilder.getParameterTypes()
            );
        }

        for (final MethodBuilder methodBuilder : methodBuilders) {
            if ((methodBuilder.getModifiers() & Flags.ACC_BRIDGE) != 0) {
                continue;
            }

            final MethodInfo generatedMethod = methodLookup.get(
                Pair.create(
                    methodBuilder.getName(),
                    methodBuilder.getErasedSignature()
                )
            );

            if (generatedMethod != null && methodBuilder.isGenericMethodDefinition()) {
                final TypeList generatedMethodTypeParameters = generatedMethod.getGenericMethodParameters();

                for (int i = 0, n = methodBuilder.genericParameterBuilders.length; i < n; i++) {
                    methodBuilder.genericParameterBuilders[i].typeBuilder._generatedType = generatedMethodTypeParameters.get(i);
                }
            }

            methodBuilder.generatedMethod = generatedMethod;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Unsafe Access">

    private static Unsafe _unsafe;

    private static Unsafe getUnsafeInstance() {

        if (_unsafe != null) {
            return _unsafe;
        }

        try {
            _unsafe = Unsafe.getUnsafe();
        }
        catch (final Throwable ignored) {
        }

        try {
            final Field instanceField = Unsafe.class.getDeclaredField("theUnsafe");
            instanceField.setAccessible(true);
            _unsafe = (Unsafe) instanceField.get(Unsafe.class);
        }
        catch (final Throwable t) {
            throw Error.couldNotLoadUnsafeClassInstance();
        }

        return _unsafe;
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="MethodOverride Class">

    private final static class MethodOverride {
        final MethodBuilder override;
        final MethodInfo baseMethod;

        private MethodOverride(final MethodBuilder override, final MethodInfo baseMethod) {
            this.baseMethod = baseMethod;
            this.override = override;
        }
    }

    // </editor-fold>
}
