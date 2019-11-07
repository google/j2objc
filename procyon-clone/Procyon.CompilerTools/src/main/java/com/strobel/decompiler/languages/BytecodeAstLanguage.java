/*
 * BytecodeAstLanguage.java
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

package com.strobel.decompiler.languages;

import com.strobel.assembler.metadata.*;
import com.strobel.core.ArrayUtilities;
import com.strobel.core.ExceptionUtilities;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.DecompilerHelpers;
import com.strobel.decompiler.ITextOutput;
import com.strobel.decompiler.NameSyntax;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.decompiler.ast.AstBuilder;
import com.strobel.decompiler.ast.AstOptimizationStep;
import com.strobel.decompiler.ast.AstOptimizer;
import com.strobel.decompiler.ast.Block;
import com.strobel.decompiler.ast.Expression;
import com.strobel.decompiler.ast.Variable;

import javax.lang.model.element.Modifier;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BytecodeAstLanguage extends Language {
    private final String _name;
    private final boolean _inlineVariables;
    private final AstOptimizationStep _abortBeforeStep;

    public BytecodeAstLanguage() {
        this("Bytecode AST", true, AstOptimizationStep.None);
    }

    private BytecodeAstLanguage(final String name, final boolean inlineVariables, final AstOptimizationStep abortBeforeStep) {
        _name = name;
        _inlineVariables = inlineVariables;
        _abortBeforeStep = abortBeforeStep;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public String getFileExtension() {
        return ".jvm";
    }

    @Override
    public TypeDecompilationResults decompileType(final TypeDefinition type, final ITextOutput output, final DecompilationOptions options) {
        writeTypeHeader(type, output);

        output.writeLine(" {");
        output.indent();

        try {
            boolean first = true;

            for (final MethodDefinition method : type.getDeclaredMethods()) {
                if (!first) {
                    output.writeLine();
                }
                else {
                    first = false;
                }

                decompileMethod(method, output, options);
            }

            if (!options.getSettings().getExcludeNestedTypes()) {
                for (final TypeDefinition innerType : type.getDeclaredTypes()) {
                    output.writeLine();
                    decompileType(innerType, output, options);
                }
            }
        }
        finally {
            output.unindent();
            output.writeLine("}");
        }
        
        return new TypeDecompilationResults( null /*no line number mapping*/);        
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void decompileMethod(final MethodDefinition method, final ITextOutput output, final DecompilationOptions options) {
        VerifyArgument.notNull(method, "method");
        VerifyArgument.notNull(output, "output");
        VerifyArgument.notNull(options, "options");

        writeMethodHeader(method, output);

        final MethodBody body = method.getBody();

        if (body == null) {
            output.writeDelimiter(";");
            output.writeLine();
            return;
        }

        final DecompilerContext context = new DecompilerContext();

        context.setCurrentMethod(method);
        context.setCurrentType(method.getDeclaringType());

        final Block methodAst = new Block();

        output.writeLine(" {");
        output.indent();

        try {
            methodAst.getBody().addAll(AstBuilder.build(body, _inlineVariables, context));

            if (_abortBeforeStep != null) {
                AstOptimizer.optimize(context, methodAst, _abortBeforeStep);
            }

            final Set<Variable> allVariables = new LinkedHashSet<>();

            for (final Expression e : methodAst.getSelfAndChildrenRecursive(Expression.class)) {
                final Object operand = e.getOperand();

                if (operand instanceof Variable && !((Variable) operand).isParameter()) {
                    allVariables.add((Variable) operand);
                }
            }

            if (!allVariables.isEmpty()) {
                for (final Variable variable : allVariables) {
                    output.writeDefinition(variable.getName(), variable);

                    final TypeReference type = variable.getType();

                    if (type != null) {
                        output.writeDelimiter(" : ");
                        DecompilerHelpers.writeType(output, type, NameSyntax.SHORT_TYPE_NAME);
                    }

                    if (variable.isGenerated()) {
                        output.write(" [generated]");
                    }

                    output.writeLine();
                }

                output.writeLine();
            }

            methodAst.writeTo(output);
        }
        catch (final Throwable t) {
            writeError(output, t);
        }
        finally {
            output.unindent();
            output.writeLine("}");
        }
    }

    private static void writeError(final ITextOutput output, final Throwable t) {
        final List<String> lines = StringUtilities.split(
            ExceptionUtilities.getStackTraceString(t),
            true,
            '\r',
            '\n'
        );

        for (final String line : lines) {
            output.writeComment("// " + line.replace("\t", "    "));
            output.writeLine();
        }
    }

    private void writeTypeHeader(final TypeDefinition type, final ITextOutput output) {
        long flags = type.getFlags() & (Flags.ClassFlags | Flags.STATIC | Flags.FINAL);

        if (type.isInterface()) {
            flags &= ~Flags.ABSTRACT;
        }
        else if (type.isEnum()) {
            flags &= Flags.AccessFlags;
        }

        for (final Modifier modifier : Flags.asModifierSet(flags)) {
            output.writeKeyword(modifier.toString());
            output.write(' ');
        }

        if (type.isInterface()) {
            if (type.isAnnotation()) {
                output.writeKeyword("@interface");
            }
            else {
                output.writeKeyword("interface");
            }
        }
        else if (type.isEnum()) {
            output.writeKeyword("enum");
        }
        else {
            output.writeKeyword("class");
        }

        output.write(' ');

        DecompilerHelpers.writeType(output, type, NameSyntax.TYPE_NAME, true);
    }

    private void writeMethodHeader(final MethodDefinition method, final ITextOutput output) {
        if (method.isTypeInitializer()) {
            output.writeKeyword("static");
            return;
        }

        if (!method.getDeclaringType().isInterface()) {
            for (final Modifier modifier : Flags.asModifierSet(method.getFlags() & Flags.MethodFlags)) {
                output.writeKeyword(modifier.toString());
                output.write(' ');
            }
        }

        final List<GenericParameter> genericParameters = method.getGenericParameters();

        if (!genericParameters.isEmpty()) {
            output.writeDelimiter("<");

            for (int i = 0; i < genericParameters.size(); i++) {
                final GenericParameter gp = genericParameters.get(i);

                if (i != 0) {
                    output.writeDelimiter(", ");
                }

                DecompilerHelpers.writeType(output, gp, NameSyntax.TYPE_NAME);
            }

            output.writeDelimiter(">");
            output.write(' ');
        }

        if (!method.isTypeInitializer()) {
            DecompilerHelpers.writeType(output, method.getReturnType(), NameSyntax.TYPE_NAME);
            output.write(' ');

            if (method.isConstructor()) {
                output.writeReference(method.getDeclaringType().getName(), method.getDeclaringType());
            }
            else {
                output.writeReference(method.getName(), method);
            }

            output.writeDelimiter("(");

            final List<ParameterDefinition> parameters = method.getParameters();

            for (int i = 0; i < parameters.size(); i++) {
                final ParameterDefinition parameter = parameters.get(i);

                if (i != 0) {
                    output.writeDelimiter(", ");
                }

                DecompilerHelpers.writeType(output, parameter.getParameterType(), NameSyntax.TYPE_NAME);
                output.write(' ');
                output.writeReference(parameter.getName(), parameter);
            }

            output.writeDelimiter(")");
        }
    }

    @Override
    public String typeToString(final TypeReference type, final boolean includePackage) {
        final ITextOutput output = new PlainTextOutput();
        DecompilerHelpers.writeType(output, type, includePackage ? NameSyntax.TYPE_NAME : NameSyntax.SHORT_TYPE_NAME);
        return output.toString();
    }

    public static List<BytecodeAstLanguage> getDebugLanguages() {
        final AstOptimizationStep[] steps = AstOptimizationStep.values();
        final BytecodeAstLanguage[] languages = new BytecodeAstLanguage[steps.length];

        languages[0] = new BytecodeAstLanguage("Bytecode AST (Unoptimized)", false, steps[0]);

        String nextName = "Bytecode AST (Variable Splitting)";

        for (int i = 1; i < languages.length; i++) {
            languages[i] = new BytecodeAstLanguage(nextName, true, steps[i - 1]);
            nextName = "Bytecode AST (After " + steps[i - 1].name() + ")";
        }

        return ArrayUtilities.asUnmodifiableList(languages);
    }
}
