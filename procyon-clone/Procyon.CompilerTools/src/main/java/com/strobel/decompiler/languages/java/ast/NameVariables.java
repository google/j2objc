/*
 * NameVariables.java
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

package com.strobel.decompiler.languages.java.ast;

import com.strobel.assembler.metadata.*;
import com.strobel.core.IntegerBox;
import com.strobel.core.StringUtilities;
import com.strobel.core.StrongBox;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.ast.AstCode;
import com.strobel.decompiler.ast.Block;
import com.strobel.decompiler.ast.Expression;
import com.strobel.decompiler.ast.Loop;
import com.strobel.decompiler.ast.PatternMatching;
import com.strobel.decompiler.ast.Variable;
import com.strobel.decompiler.languages.java.JavaOutputVisitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.strobel.core.CollectionUtilities.getOrDefault;

public class NameVariables {
    private final static char MAX_LOOP_VARIABLE_NAME = 'm';
    private final static String[] METHOD_PREFIXES = { "get", "is", "are", "to", "as", "create", "make", "new", "read", "parse", "extract", "find" };
    private final static String[] METHOD_SUFFIXES = { "At", "For", "From", "Of" };
    private final static Map<String, String> BUILT_IN_TYPE_NAMES;
    private final static Map<String, String> METHOD_NAME_MAPPINGS;

    static {
        final Map<String, String> builtInTypeNames = new LinkedHashMap<>();
        final Map<String, String> methodNameMappings = new LinkedHashMap<>();

        builtInTypeNames.put(BuiltinTypes.Boolean.getInternalName(), "b");
        builtInTypeNames.put("java/lang/Boolean", "b");
        builtInTypeNames.put(BuiltinTypes.Byte.getInternalName(), "b");
        builtInTypeNames.put("java/lang/Byte", "b");
        builtInTypeNames.put(BuiltinTypes.Short.getInternalName(), "n");
        builtInTypeNames.put("java/lang/Short", "n");
        builtInTypeNames.put(BuiltinTypes.Integer.getInternalName(), "n");
        builtInTypeNames.put("java/lang/Integer", "n");
        builtInTypeNames.put(BuiltinTypes.Long.getInternalName(), "n");
        builtInTypeNames.put("java/lang/Long", "n");
        builtInTypeNames.put(BuiltinTypes.Float.getInternalName(), "n");
        builtInTypeNames.put("java/lang/Float", "n");
        builtInTypeNames.put(BuiltinTypes.Double.getInternalName(), "n");
        builtInTypeNames.put("java/lang/Double", "n");
        builtInTypeNames.put(BuiltinTypes.Character.getInternalName(), "c");
        builtInTypeNames.put("java/lang/Number", "n");
        builtInTypeNames.put("java/io/Serializable", "s");
        builtInTypeNames.put("java/lang/Character", "c");
        builtInTypeNames.put("java/lang/Object", "o");
        builtInTypeNames.put("java/lang/String", "s");
        builtInTypeNames.put("java/lang/StringBuilder", "sb");
        builtInTypeNames.put("java/lang/StringBuffer", "sb");
        builtInTypeNames.put("java/lang/Class", "clazz");

        BUILT_IN_TYPE_NAMES = Collections.unmodifiableMap(builtInTypeNames);

        methodNameMappings.put("get", "value");

        METHOD_NAME_MAPPINGS = methodNameMappings;
    }

    private final ArrayList<String> _fieldNamesInCurrentType;
    private final Map<String, Integer> _typeNames = new HashMap<>();

    public NameVariables(final DecompilerContext context) {
        _fieldNamesInCurrentType = new ArrayList<>();

        for (final FieldDefinition field : context.getCurrentType().getDeclaredFields()) {
            _fieldNamesInCurrentType.add(field.getName());
        }
    }

    public final void addExistingName(final String name) {
        if (StringUtilities.isNullOrEmpty(name)) {
            return;
        }

        final IntegerBox number = new IntegerBox();
        final String nameWithoutDigits = splitName(name, number);
        final Integer existingNumber = _typeNames.get(nameWithoutDigits);

        if (existingNumber != null) {
            _typeNames.put(nameWithoutDigits, Math.max(number.value, existingNumber));
        }
        else {
            _typeNames.put(nameWithoutDigits, number.value);
        }
    }

    final String splitName(final String name, final IntegerBox number) {
        int position = name.length();

        while (position > 0 && name.charAt(position - 1) >= '0' && name.charAt(position - 1) <= '9') {
            position--;
        }

        if (position < name.length()) {
            number.value = Integer.parseInt(name.substring(position));
            return name.substring(0, position);
        }

        number.value = 1;
        return name;
    }

    public static void assignNamesToVariables(
        final DecompilerContext context,
        final Iterable<Variable> parameters,
        final Iterable<Variable> variables,
        final Block methodBody) {

        final NameVariables nv = new NameVariables(context);

        for (final String name : context.getReservedVariableNames()) {
            nv.addExistingName(name);
        }

        for (final Variable p : parameters) {
            nv.addExistingName(p.getName());
        }

        if (context.getCurrentMethod().isTypeInitializer()) {
            //
            // We cannot assign final static variables with a type qualifier, so make sure we
            // don't have variable/field name collisions in type initializers which must assign
            // those fields.
            //
            for (final FieldDefinition f : context.getCurrentType().getDeclaredFields()) {
                if (f.isStatic() && f.isFinal() && !f.hasConstantValue()) {
                    nv.addExistingName(f.getName());
                }
            }
        }

        for (final Variable v : variables) {
            if (v.isGenerated()) {
                nv.addExistingName(v.getName());
            }
            else {
                final VariableDefinition originalVariable = v.getOriginalVariable();

                if (originalVariable != null) {
/*
                    if (originalVariable.isFromMetadata() && originalVariable.hasName()) {
                        v.setName(originalVariable.getName());
                        continue;
                    }
*/

                    final String varName = originalVariable.getName();

                    if (StringUtilities.isNullOrEmpty(varName) || varName.startsWith("V_") || !isValidName(varName)) {
                        v.setName(null);
                    }
                    else {
                        v.setName(nv.getAlternativeName(varName));
                    }
                }
                else {
                    v.setName(null);
                }
            }
        }

        for (final Variable p : parameters) {
            if (!p.getOriginalParameter().hasName()) {
                p.setName(nv.generateNameForVariable(p, methodBody));
            }
        }

        for (final Variable varDef : variables) {
            final boolean generateName = StringUtilities.isNullOrEmpty(varDef.getName()) ||
                                         varDef.isGenerated() ||
                                         !varDef.isParameter() && !varDef.getOriginalVariable().isFromMetadata();

            if (generateName) {
                varDef.setName(nv.generateNameForVariable(varDef, methodBody));
            }
        }
    }

    static boolean isValidName(final String name) {
        if (StringUtilities.isNullOrEmpty(name)) {
            return false;
        }

        if (!Character.isJavaIdentifierPart(name.charAt(0))) {
            return false;
        }

        for (int i = 1; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public String getAlternativeName(final String oldVariableName) {
        final IntegerBox number = new IntegerBox();
        final String nameWithoutDigits = splitName(oldVariableName, number);

        if (!_typeNames.containsKey(nameWithoutDigits) && !JavaOutputVisitor.isKeyword(oldVariableName)) {
            _typeNames.put(nameWithoutDigits, Math.max(number.value, 1));
            return oldVariableName;
        }

        if (oldVariableName.length() == 1 &&
            oldVariableName.charAt(0) >= 'i' &&
            oldVariableName.charAt(0) <= MAX_LOOP_VARIABLE_NAME) {

            for (char c = 'i'; c <= MAX_LOOP_VARIABLE_NAME; c++) {
                final String cs = String.valueOf(c);

                if (!_typeNames.containsKey(cs)) {
                    _typeNames.put(cs, 1);
                    return cs;
                }
            }
        }

        if (!_typeNames.containsKey(nameWithoutDigits)) {
            _typeNames.put(nameWithoutDigits, number.value - 1);
        }

        final int count = _typeNames.get(nameWithoutDigits) + 1;

        _typeNames.put(nameWithoutDigits, count);

        if (count != 1 || JavaOutputVisitor.isKeyword(nameWithoutDigits)) {
            return nameWithoutDigits + count;
        }
        else {
            return nameWithoutDigits;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private String generateNameForVariable(final Variable variable, final Block methodBody) {
        String proposedName = null;

        if (variable.getType().getSimpleType() == JvmType.Integer) {
            boolean isLoopCounter = false;

        loopSearch:
            for (final Loop loop : methodBody.getSelfAndChildrenRecursive(Loop.class)) {
                Expression e = loop.getCondition();

                while (e != null && e.getCode() == AstCode.LogicalNot) {
                    e = e.getArguments().get(0);
                }

                if (e != null) {
                    switch (e.getCode()) {
                        case CmpEq:
                        case CmpNe:
                        case CmpLe:
                        case CmpGt:
                        case CmpGe:
                        case CmpLt: {
                            final StrongBox<Variable> loadVariable = new StrongBox<>();
                            if (PatternMatching.matchGetOperand(e.getArguments().get(0), AstCode.Load, loadVariable) &&
                                loadVariable.get() == variable) {

                                isLoopCounter = true;
                                break loopSearch;
                            }
                            break;
                        }
                    }
                }
            }

            if (isLoopCounter) {
                for (char c = 'i'; c < MAX_LOOP_VARIABLE_NAME; c++) {
                    final String name = String.valueOf(c);

                    if (!_typeNames.containsKey(name)) {
                        proposedName = name;
                        break;
                    }
                }
            }
        }

        if (StringUtilities.isNullOrEmpty(proposedName)) {
            String proposedNameForStore = null;

            for (final Expression e : methodBody.getSelfAndChildrenRecursive(Expression.class)) {
                if (e.getCode() == AstCode.Store && e.getOperand() == variable) {
                    final String name = getNameFromExpression(e.getArguments().get(0));

                    if (name != null/* && !_fieldNamesInCurrentType.contains(name)*/) {
                        if (proposedNameForStore != null) {
                            proposedNameForStore = null;
                            break;
                        }

                        proposedNameForStore = name;
                    }
                }
            }

            if (proposedNameForStore != null) {
                proposedName = proposedNameForStore;
            }
        }

        if (StringUtilities.isNullOrEmpty(proposedName)) {
            String proposedNameForLoad = null;

            for (final Expression e : methodBody.getSelfAndChildrenRecursive(Expression.class)) {
                final List<Expression> arguments = e.getArguments();

                for (int i = 0; i < arguments.size(); i++) {
                    final Expression a = arguments.get(i);
                    if (a.getCode() == AstCode.Load && a.getOperand() == variable) {
                        final String name = getNameForArgument(e, i);

                        if (name != null/* && !_fieldNamesInCurrentType.contains(name)*/) {
                            if (proposedNameForLoad != null) {
                                proposedNameForLoad = null;
                                break;
                            }

                            proposedNameForLoad = name;
                        }
                    }
                }
            }

            if (proposedNameForLoad != null) {
                proposedName = proposedNameForLoad;
            }
        }

        if (StringUtilities.isNullOrEmpty(proposedName)) {
            proposedName = getNameForType(variable.getType());
        }

        return this.getAlternativeName(proposedName);
/*
        while (true) {
            proposedName = this.getAlternativeName(proposedName);

            if (!_fieldNamesInCurrentType.contains(proposedName)) {
                return proposedName;
            }
        }
*/
    }

    private static String cleanUpVariableName(final String s) {
        if (s == null) {
            return null;
        }

        String name = s;

        if (name.length() > 2 && name.startsWith("m_")) {
            name = name.substring(2);
        }
        else if (name.length() > 1 && name.startsWith("_")) {
            name = name.substring(1);
        }

        final int length = name.length();

        if (length == 0) {
            return "obj";
        }

        int lowerEnd;

        for (lowerEnd = 1;
             lowerEnd < length && Character.isUpperCase(name.charAt(lowerEnd));
             lowerEnd++) {

            if (lowerEnd < length - 1) {
                final char nextChar = name.charAt(lowerEnd + 1);

                if (Character.isLowerCase(nextChar)) {
                    break;
                }

                if (!Character.isAlphabetic(nextChar)) {
                    lowerEnd++;
                    break;
                }
            }
        }

        name = name.substring(0, lowerEnd).toLowerCase() + name.substring(lowerEnd);

        if (JavaOutputVisitor.isKeyword(name)) {
            return name + "1";
        }

        return name;
    }

    private static String getNameFromExpression(final Expression e) {
        switch (e.getCode()) {
            case ArrayLength: {
                return cleanUpVariableName("length");
            }

            case GetField:
            case GetStatic: {
                return cleanUpVariableName(((FieldReference) e.getOperand()).getName());
            }

            case InvokeVirtual:
            case InvokeSpecial:
            case InvokeStatic:
            case InvokeInterface: {
                final MethodReference method = (MethodReference) e.getOperand();

                if (method != null) {
                    final String methodName = method.getName();

                    String name = methodName;

                    final String mappedMethodName = METHOD_NAME_MAPPINGS.get(methodName);

                    if (mappedMethodName != null) {
                        return cleanUpVariableName(mappedMethodName);
                    }

                    for (final String prefix : METHOD_PREFIXES) {
                        if (methodName.length() > prefix.length() &&
                            methodName.startsWith(prefix) &&
                            Character.isUpperCase(methodName.charAt(prefix.length()))) {

                            name = methodName.substring(prefix.length());
                            break;
                        }
                    }

                    for (final String suffix : METHOD_SUFFIXES) {
                        if (name.length() > suffix.length() &&
                            name.endsWith(suffix) &&
                            Character.isLowerCase(name.charAt(name.length() - suffix.length() - 1))) {

                            name = name.substring(0, name.length() - suffix.length());
                            break;
                        }
                    }

                    return cleanUpVariableName(name);
                }

                break;
            }
        }

        return null;
    }

    private static String getNameForArgument(final Expression parent, final int i) {
        switch (parent.getCode()) {
            case PutField:
            case PutStatic: {
                if (i == parent.getArguments().size() - 1) {
                    return cleanUpVariableName(((FieldReference) parent.getOperand()).getName());
                }
                break;
            }

            case InvokeVirtual:
            case InvokeSpecial:
            case InvokeStatic:
            case InvokeInterface:
            case InitObject: {
                final MethodReference method = (MethodReference) parent.getOperand();

                if (method != null) {
                    final String methodName = method.getName();
                    final List<ParameterDefinition> parameters = method.getParameters();

                    if (parameters.size() == 1 && i == parent.getArguments().size() - 1) {
                        if (methodName.length() > 3 &&
                            StringUtilities.startsWith(methodName, "set") &&
                            Character.isUpperCase(methodName.charAt(3))) {

                            return cleanUpVariableName(methodName.substring(3));
                        }
                    }

                    final MethodDefinition definition = method.resolve();

                    if (definition != null) {
                        final ParameterDefinition p = getOrDefault(
                            definition.getParameters(),
                            parent.getCode() != AstCode.InitObject && !definition.isStatic() ? i - 1 : i
                        );

                        if (p != null && p.hasName() && !StringUtilities.isNullOrEmpty(p.getName())) {
                            return cleanUpVariableName(p.getName());
                        }
                    }
                }

                break;
            }
        }

        return null;
    }

    private String getNameForType(final TypeReference type) {

        TypeReference nameSource = type;

        String name;

        if (nameSource.isArray()) {
            name = "array";
        }
        else if (StringUtilities.equals(nameSource.getInternalName(), "java/lang/Throwable")) {
            name = "t";
        }
        else if (StringUtilities.endsWith(nameSource.getName(), "Exception")) {
            name = "ex";
        }
        else if (StringUtilities.endsWith(nameSource.getName(), "List")) {
            name = "list";
        }
        else if (StringUtilities.endsWith(nameSource.getName(), "Set")) {
            name = "set";
        }
        else if (StringUtilities.endsWith(nameSource.getName(), "Collection")) {
            name = "collection";
        }
        else {
            name = BUILT_IN_TYPE_NAMES.get(nameSource.getInternalName());

            if (name != null) {
                return name;
            }

            nameSource = MetadataHelper.getDeclaredType(nameSource);

            if (!nameSource.isDefinition()) {
                final TypeDefinition resolvedType = nameSource.resolve();

                if (resolvedType != null) {
                    nameSource = resolvedType;
                }
            }

            name = nameSource.getSimpleName();

            //
            // Remove leading 'I' for interfaces.
            //
            if (name.length() > 2 &&
                (name.charAt(0) == 'I' || name.charAt(0) == 'J') &&
                Character.isUpperCase(name.charAt(1)) &&
                Character.isLowerCase(name.charAt(2))) {

                name = name.substring(1);
            }

            name = cleanUpVariableName(name);
        }

        return name;
    }
}
