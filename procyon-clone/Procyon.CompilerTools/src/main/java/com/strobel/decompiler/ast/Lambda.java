/*
 * Lambda.java
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

package com.strobel.decompiler.ast;

import com.strobel.assembler.Collection;
import com.strobel.assembler.metadata.DynamicCallSite;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilerHelpers;
import com.strobel.decompiler.ITextOutput;
import com.strobel.decompiler.NameSyntax;

import java.util.Collections;
import java.util.List;

public class Lambda extends Node {
    private final Collection<Variable> _parameters = new Collection<>();

    private DynamicCallSite _callSite;
    private MethodReference _method;
    private TypeReference _functionType;
    private Block _body;

    private TypeReference _expectedReturnType;
    private TypeReference _inferredReturnType;

    public Lambda() {
    }

    public Lambda(final Block body) {
        _body = body;
    }

    public Lambda(final Block body, final TypeReference functionType) {
        _body = body;
        _functionType = functionType;
    }

    public final List<Variable> getParameters() {
        return _parameters;
    }

    public final DynamicCallSite getCallSite() {
        return _callSite;
    }

    public final void setCallSite(final DynamicCallSite callSite) {
        _callSite = callSite;
    }

    public final Block getBody() {
        return _body;
    }

    public final void setBody(final Block body) {
        _body = body;
    }

    public final TypeReference getFunctionType() {
        return _functionType;
    }

    public final void setFunctionType(final TypeReference functionType) {
        _functionType = functionType;
    }

    public final MethodReference getMethod() {
        return _method;
    }

    public final void setMethod(final MethodReference method) {
        _method = method;
    }

    public final TypeReference getExpectedReturnType() {
        return _expectedReturnType;
    }

    public final void setExpectedReturnType(final TypeReference expectedReturnType) {
        _expectedReturnType = expectedReturnType;
    }

    public final TypeReference getInferredReturnType() {
        return _inferredReturnType;
    }

    public final void setInferredReturnType(final TypeReference inferredReturnType) {
        _inferredReturnType = inferredReturnType;
    }

    @Override
    public List<Node> getChildren() {
        return _body != null ? Collections.<Node>singletonList(_body)
                             : Collections.<Node>emptyList();
    }

    @Override
    public final void writeTo(final ITextOutput output) {
        output.write("(");

        boolean comma = false;

        for (final Variable parameter : _parameters) {
            if (comma) {
                output.write(", ");
            }
            DecompilerHelpers.writeOperand(output, parameter);
            if (parameter.getType() != null) {
                output.writeDelimiter(":");
                DecompilerHelpers.writeType(output, parameter.getType(), NameSyntax.SHORT_TYPE_NAME);
            }
            comma = true;
        }

        output.write(") -> ");

        if (_body != null) {
            final List<Node> body = _body.getBody();

            if (body.size() == 1 && body.get(0) instanceof Expression) {
                body.get(0).writeTo(output);
            }
            else {
                output.writeLine("{");
                output.indent();
                _body.writeTo(output);
                output.unindent();
                output.write("}");
            }
        }
        else {
            output.write("{}");
        }
    }
}
