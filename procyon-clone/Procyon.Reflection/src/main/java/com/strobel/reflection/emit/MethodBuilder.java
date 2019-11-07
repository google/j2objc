/*
 * MethodBuilder.java
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
import com.strobel.core.ArrayUtilities;
import com.strobel.core.ReadOnlyList;
import com.strobel.core.VerifyArgument;
import com.strobel.reflection.*;
import com.strobel.util.EmptyArrayCache;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author Mike Strobel
 */
@SuppressWarnings({"PackageVisibleField", "unchecked"})
public final class MethodBuilder extends MethodInfo {
    private final String _name;
    private final TypeBuilder<?> _declaringType;
    private final int _modifiers;

    private SignatureType _signatureType;
    private TypeList _thrownTypes;

    private boolean _isFinished;
    private ReadOnlyList<AnnotationBuilder<? extends Annotation>> _annotations;
    private byte[] _body;
    private int _numberOfExceptions;
    private __ExceptionInstance[] _exceptions;
    private Object _defaultValue;

    GenericParameterBuilder<?>[] genericParameterBuilders;
    ParameterBuilder[] parameterBuilders;
    CodeGenerator generator;
    MethodInfo generatedMethod;

    MethodBuilder(
        final String name,
        final int modifiers,
        final Type<?> returnType,
        final TypeList parameterTypes,
        final TypeList thrownTypes,
        final TypeBuilder<?> declaringType) {

        _name = VerifyArgument.notNullOrWhitespace(name, "name");
        _modifiers = modifiers;
        _signatureType = new SignatureType(
            returnType != null ? returnType : PrimitiveTypes.Void,
            parameterTypes != null ? parameterTypes : TypeList.empty()
        );
        _thrownTypes = thrownTypes != null ? thrownTypes : TypeList.empty();
        _declaringType = VerifyArgument.notNull(declaringType, "declaringType");
        _annotations = ReadOnlyList.emptyList();

        setSignature(returnType, parameterTypes);
    }

    final void verifyNotGeneric() {
        if (isGenericMethod() && !isGenericMethodDefinition()) {
            throw Error.methodIsGeneric();
        }
    }

    final void verifyNotAbstract() {
        if (isAbstract()) {
            throw Error.abstractMethodCannotHaveBody();
        }
    }

    public CodeGenerator getCodeGenerator() {
        verifyNotGeneric();
        verifyNotAbstract();

        if (generator == null) {
            generator = new CodeGenerator(this);
        }

        return generator;
    }

    public CodeGenerator getCodeGenerator(final int initialSize) {
        verifyNotGeneric();
        verifyNotAbstract();

        if (generator == null) {
            generator = new CodeGenerator(this, initialSize);
        }

        return generator;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public Type<?> getReturnType() {
        return _signatureType.getReturnType();
    }

    @Override
    public SignatureType getSignatureType() {
        return _signatureType;
    }

    @Override
    public Method getRawMethod() {
        _declaringType.verifyCreated();
        return generatedMethod.getRawMethod();
    }

    @Override
    public Object getDefaultValue() {
        return _defaultValue;
    }

    public void setDefaultValue(final Object value) {
        if (!_declaringType.isInterface() || !Types.Annotation.isAssignableFrom(_declaringType)) {
            throw Error.onlyAnnotationMethodsCanHaveDefaultValues();
        }
        _defaultValue = value;
    }

    @Override
    public TypeBuilder<?> getDeclaringType() {
        return _declaringType;
    }

    @Override
    public int getModifiers() {
        return _modifiers;
    }

    @Override
    public ParameterList getParameters() {
        return _declaringType.isCreated() ? generatedMethod.getParameters() : createParameters();
    }

    final ParameterList createParameters() {
        final List<ParameterBuilder> pb = this.getDefinedParameters();
        final ParameterInfo[] p = new ParameterInfo[pb.size()];

        for (int i = 0; i < p.length; i++) {
            final ParameterBuilder b = pb.get(i);
            p[i] = new ParameterInfo(b.getName(), b.getPosition(), b.getParameterType());
        }

        return new ParameterList(p);
    }

    @Override
    public TypeList getThrownTypes() {
        return _thrownTypes;
    }

    public TypeList getParameterTypes() {
        return _signatureType.getParameterTypes();
    }

    public List<ParameterBuilder> getDefinedParameters() {
        return ArrayUtilities.asUnmodifiableList(parameterBuilders);
    }

    public boolean isTypeCreated() {
        return _declaringType.isCreated();
    }

    public boolean isFinished() {
        return _isFinished;
    }

    public void setReturnType(final Type<?> type) {
        verifyCodeGeneratorNotCreated();
        setSignature(type, null);
    }

    final void verifyCodeGeneratorNotCreated() {
        if (generator != null) {
            throw Error.cannotModifyMethodAfterCallingGetGenerator();
        }
    }

    public void setSignature(final Type<?> returnType, final TypeList parameterTypes) {
        verifyNotGeneric();

        Type<?> newReturnType = _signatureType.getReturnType();
        TypeList newParameterTypes = _signatureType.getParameterTypes();

        if (returnType != null) {
            newReturnType = returnType;
        }

        if (parameterTypes != null) {
            newParameterTypes = parameterTypes;
            parameterBuilders = new ParameterBuilder[parameterTypes.size()];

            for (int i = 0, n = parameterTypes.size(); i < n; i++) {
                parameterBuilders[i] = new ParameterBuilder(
                    this,
                    i,
                    null,
                    parameterTypes.get(i)
                );
            }
        }

        if (!newReturnType.isEquivalentTo(_signatureType.getReturnType()) ||
            !newParameterTypes.isEquivalentTo(_signatureType.getParameterTypes())) {

            _signatureType = new SignatureType(newReturnType, newParameterTypes);
        }

        invalidateCaches();
    }

    public void setParameters(final TypeList types) {
        verifyCodeGeneratorNotCreated();
        setSignature(null, types);
    }

    public void setThrownTypes(final TypeList types) {
        verifyCodeGeneratorNotCreated();
        _thrownTypes = types != null ? types : TypeList.empty();
        invalidateCaches();
    }

    @Override
    public Type getReflectedType() {
        return _declaringType;
    }

    public void addCustomAnnotation(final AnnotationBuilder annotation) {
        VerifyArgument.notNull(annotation, "annotation");
        final AnnotationBuilder[] newAnnotations = new AnnotationBuilder[this._annotations.size() + 1];
        _annotations.toArray(newAnnotations);
        newAnnotations[this._annotations.size()] = annotation;
        _annotations = new ReadOnlyList<AnnotationBuilder<? extends Annotation>>(newAnnotations);
    }

    public ReadOnlyList<AnnotationBuilder<? extends Annotation>> getCustomAnnotations() {
        return _annotations;
    }

    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        _declaringType.verifyCreated();
        return generatedMethod.getAnnotation(annotationClass);
    }

    @NotNull
    @Override
    public Annotation[] getAnnotations() {
        _declaringType.verifyCreated();
        return generatedMethod.getAnnotations();
    }

    @NotNull
    @Override
    public Annotation[] getDeclaredAnnotations() {
        _declaringType.verifyCreated();
        return generatedMethod.getDeclaredAnnotations();
    }

    @Override
    public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
        _declaringType.verifyCreated();
        return generatedMethod.isAnnotationPresent(annotationClass);
    }

    @Override
    public boolean isGenericMethod() {
        return !ArrayUtilities.isNullOrEmpty(genericParameterBuilders);
    }

    @Override
    public boolean isGenericMethodDefinition() {
        return isGenericMethod();
    }

    @Override
    public TypeList getGenericMethodParameters() {
        if (ArrayUtilities.isNullOrEmpty(genericParameterBuilders)) {
            return TypeList.empty();
        }
        return Type.list(genericParameterBuilders);
    }

    @Override
    public MethodInfo getErasedMethodDefinition() {
        if (_declaringType.isCreated()) {
            return generatedMethod.getErasedMethodDefinition();
        }
        return super.getErasedMethodDefinition();
    }

    @Override
    public StringBuilder appendDescription(final StringBuilder sb) {
        StringBuilder s = new StringBuilder();

        for (final javax.lang.model.element.Modifier modifier : Flags.asModifierSet(getModifiers())) {
            s.append(modifier.toString());
            s.append(' ');
        }

        if (isGenericMethodDefinition()) {
            final GenericParameterBuilder<?>[] genericParameters = genericParameterBuilders;

            s.append('<');
            for (int i = 0, n = genericParameters.length; i < n; i++) {
                if (i != 0) {
                    s.append(", ");
                }
                s = genericParameters[i].appendSimpleDescription(s);
            }
            s.append('>');
            s.append(' ');
        }

        Type returnType = getReturnType();

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

        final ParameterList parameters = getParameters();

        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterInfo p = parameters.get(i);
            if (i != 0) {
                s.append(", ");
            }

            Type parameterType = p.getParameterType();

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

        final TypeList thrownTypes = getThrownTypes();

        if (!thrownTypes.isEmpty()) {
            s.append(" throws ");

            for (int i = 0, n = thrownTypes.size(); i < n; ++i) {
                final Type t = thrownTypes.get(i);
                if (i != 0) {
                    s.append(", ");
                }
                s = t.appendBriefDescription(s);
            }
        }

        return s;
    }

    @Override
    public StringBuilder appendSimpleDescription(final StringBuilder sb) {
        StringBuilder s = new StringBuilder();

        for (final javax.lang.model.element.Modifier modifier : Flags.asModifierSet(getModifiers())) {
            s.append(modifier.toString());
            s.append(' ');
        }

        if (isGenericMethodDefinition()) {
            final GenericParameterBuilder<?>[] genericParameters = genericParameterBuilders;

            s.append('<');
            for (int i = 0, n = genericParameters.length; i < n; i++) {
                if (i != 0) {
                    s.append(", ");
                }
                s = genericParameters[i].appendSimpleDescription(s);
            }
            s.append('>');
            s.append(' ');
        }

        s = getReturnType().appendSimpleDescription(s);
        s.append(' ');
        s.append(getName());
        s.append('(');

        final ParameterBuilder[] parameters = parameterBuilders;

        for (int i = 0, n = parameters.length; i < n; ++i) {
            final ParameterBuilder p = parameters[i];
            if (i != 0) {
                s.append(", ");
            }
            s = p.getParameterType().appendSimpleDescription(s);
        }

        s.append(')');

        final TypeList thrownTypes = getThrownTypes();

        if (!thrownTypes.isEmpty()) {
            s.append(" throws ");

            for (int i = 0, n = thrownTypes.size(); i < n; ++i) {
                final Type t = thrownTypes.get(i);
                if (i != 0) {
                    s.append(", ");
                }
                s = t.appendSimpleDescription(s);
            }
        }

        return s;
    }

    @Override
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        StringBuilder s = sb;
        s.append('(');

        final TypeList parameterTypes = getParameterTypes();

        for (int i = 0, n = parameterTypes.size(); i < n; ++i) {
            s = parameterTypes.get(i).appendErasedSignature(s);
        }

        s.append(')');
        s = getReturnType().appendErasedSignature(s);

        return s;
    }

    @Override
    public StringBuilder appendSignature(final StringBuilder sb) {
        StringBuilder s = sb;

        if (isGenericMethod()) {
            final GenericParameterBuilder<?>[] genericParameters = genericParameterBuilders;
            final int count = genericParameters.length;

            if (count > 0) {
                s.append('<');
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < count; ++i) {
                    final Type type = genericParameters[i];
                    s = type.appendGenericSignature(s);
                }
                s.append('>');
            }
        }

        s.append('(');

        final TypeList parameterTypes = getParameterTypes();

        for (int i = 0, n = parameterTypes.size(); i < n; ++i) {
            s = parameterTypes.get(i).appendSignature(s);
        }

        s.append(')');
        s = getReturnType().appendSignature(s);

        return s;
    }

    public GenericParameterBuilder<?>[] defineGenericParameters(final String... names) {
        VerifyArgument.notEmpty(names, "names");

        if (genericParameterBuilders != null) {
            throw Error.genericParametersAlreadySet();
        }

        final GenericParameterBuilder<?>[] parameters = new GenericParameterBuilder<?>[names.length];

        for (int i = 0, n = names.length; i < n; i++) {
            final String name = names[i];
            if (name == null) {
                throw new IllegalArgumentException("Names array contains one or more null elements.");
            }
            parameters[i] = new GenericParameterBuilder<>(new TypeBuilder(name, i, this));
        }

        genericParameterBuilders = parameters;

        return genericParameterBuilders;
    }

    public ParameterBuilder defineParameter(final int position, final String name) {
        verifyCodeGeneratorNotCreated();

        VerifyArgument.isNonNegative(position, "position");

        verifyNotGeneric();
        _declaringType.verifyNotCreated();

        final TypeList parameterTypes = _signatureType.getParameterTypes();

        if (parameterTypes == null || position >= parameterTypes.size()) {
            throw new IllegalArgumentException("Position is out of range.");
        }

        final ParameterBuilder parameterBuilder = parameterBuilders[position];

        parameterBuilder.setName(name);

        return parameterBuilder;
    }

    public void createMethodBody(final byte[] bytecode, final int size) {
        verifyNotGeneric();

        if (isFinished()) {
            throw Error.methodIsFinished();
        }

        _declaringType.verifyNotCreated();

        if (bytecode != null) {
            VerifyArgument.inRange(0, bytecode.length, size, "size");
        }

        if (bytecode == null) {
            _body = null;
            return;
        }

        _body = Arrays.copyOf(bytecode, size);
        _isFinished = true;
    }

    final byte[] getBody() {
        return _body;
    }

    final __ExceptionInstance[] getExceptionInstances() {
        return _exceptions;
    }

    final int getNumberOfExceptions() {
        return _numberOfExceptions;
    }

    final void createMethodBodyHelper(final CodeGenerator code) {
        VerifyArgument.notNull(code, "code");

        final __ExceptionInfo[] exceptions;
//        final Type<?>[] unhandledExceptions;
        int counter = 0;
        int[] filterAddresses;
        int[] catchAddresses;
        int[] catchEndAddresses;
        Type[] catchClass;
        int[] exceptionType;
        int numberOfCatches;
        int startAddress, endAddress;

        _declaringType.verifyNotCreated();

        if (_isFinished) {
            throw Error.methodIsFinished();
        }

        if (code.methodBuilder != this && code.methodBuilder != null) {
            throw Error.bytecodeGeneratorNotOwnedByMethodBuilder();
        }

        if (code.scopeTree._openScopeCount != 0) {
            throw Error.methodHasOpenLocalScope();
        }

        _body = code.bakeByteArray();

        exceptions = code.getExceptions();
/*
        unhandledExceptions = code.getUnhandledCheckedExceptions();

        for (final Type<?> unhandledExceptionType : unhandledExceptions) {
            if (!_thrownTypes.containsTypeAssignableFrom(unhandledExceptionType)) {
                throw Error.checkedExceptionUnhandled(unhandledExceptionType);
            }
        }
*/

        _numberOfExceptions = calculateNumberOfExceptions(exceptions);

        if (_numberOfExceptions > 0) {
            _exceptions = new __ExceptionInstance[_numberOfExceptions];

            for (final __ExceptionInfo exception : exceptions) {

                filterAddresses = exception.getFilterAddresses();
                catchAddresses = exception.getCatchAddresses();
                catchEndAddresses = exception.getCatchEndAddresses();
                catchClass = exception.getCatchClass();

                numberOfCatches = exception.getNumberOfCatches();
                startAddress = exception.getStartAddress();
                endAddress = exception.getEndAddress();
                exceptionType = exception.getExceptionTypes();

                for (int j = 0; j < numberOfCatches; j++) {
                    int tkExceptionClass = 0;
                    if (catchClass[j] != null) {
                        tkExceptionClass = _declaringType.getTypeToken(catchClass[j]);
                    }
                    switch (exceptionType[j]) {
                        case __ExceptionInfo.None:
                        case __ExceptionInfo.Filter:
                            _exceptions[counter++] = new __ExceptionInstance(
                                startAddress,
                                endAddress,
                                filterAddresses[j],
                                catchAddresses[j],
                                catchEndAddresses[j],
                                exceptionType[j],
                                tkExceptionClass
                            );
                            break;

                        case __ExceptionInfo.Finally:
                            _exceptions[counter++] = new __ExceptionInstance(
                                startAddress,
                                exception.getEndAddress(),
                                filterAddresses[j],
                                catchAddresses[j],
                                catchEndAddresses[j],
                                exceptionType[j],
                                tkExceptionClass
                            );
                            break;
                    }
                }
            }
        }
        else {
            _exceptions = EmptyArrayCache.fromElementType(__ExceptionInstance.class);
        }

        _isFinished = true;
    }

    private static int calculateNumberOfExceptions(final __ExceptionInfo[] exceptions) {
        int numberOfExceptions = 0;

        if (exceptions == null) {
            return 0;
        }

        for (final __ExceptionInfo exception : exceptions) {
            numberOfExceptions += exception.getNumberOfCatches();
        }

        return numberOfExceptions;
    }

    final void releaseBakedStructures() {
        _body = null;
    }
}

@SuppressWarnings("PackageVisibleField")
final class __ExceptionInstance {
    int exceptionClass;
    int startAddress;
    int endAddress;
    int filterAddress;
    int handleAddress;
    int handleEndAddress;
    int type;

    __ExceptionInstance(
        final int start,
        final int end,
        final int filterAddress,
        final int handle,
        final int handleEnd,
        final int type,
        final int exceptionClass) {

        this.startAddress = start;
        this.endAddress = end;
        this.filterAddress = filterAddress;
        this.handleAddress = handle;
        this.handleEndAddress = handleEnd;
        this.type = type;
        this.exceptionClass = exceptionClass;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && (obj instanceof __ExceptionInstance)) {
            final __ExceptionInstance that = (__ExceptionInstance)obj;
            return that.exceptionClass == exceptionClass &&
                   that.startAddress == startAddress &&
                   that.endAddress == endAddress &&
                   that.filterAddress == filterAddress &&
                   that.handleAddress == handleAddress &&
                   that.handleEndAddress == handleEndAddress;
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return exceptionClass ^ startAddress ^ endAddress ^ filterAddress ^ handleAddress ^ handleEndAddress ^ type;
    }
} 
