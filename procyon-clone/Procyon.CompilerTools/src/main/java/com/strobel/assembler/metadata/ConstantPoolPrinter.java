/*
 * ConstantPoolPrinter.java
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

import com.strobel.assembler.ir.ConstantPool;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilerHelpers;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.ITextOutput;

import static java.lang.String.format;

public class ConstantPoolPrinter implements ConstantPool.Visitor {
    private final static int MAX_TAG_LENGTH;

    static {
        int maxTagLength = 0;

        for (final ConstantPool.Tag tag : ConstantPool.Tag.values()) {
            final int length = tag.name().length();

            if (length > maxTagLength) {
                maxTagLength = length;
            }
        }

        MAX_TAG_LENGTH = maxTagLength;
    }

    private final ITextOutput _output;
    private final DecompilerSettings _settings;
    private boolean _isHeaderPrinted;

    public ConstantPoolPrinter(final ITextOutput output) {
        this(output, DecompilerSettings.javaDefaults());
    }

    public ConstantPoolPrinter(final ITextOutput output, final DecompilerSettings settings) {
        _output = VerifyArgument.notNull(output, "output");
        _settings = VerifyArgument.notNull(settings, "settings");
    }

    protected void printTag(final ConstantPool.Tag tag) {
        _output.writeAttribute(format("%1$-" + MAX_TAG_LENGTH + "s  ", tag));
    }

    @Override
    public void visit(final ConstantPool.Entry entry) {
        VerifyArgument.notNull(entry, "entry");

        if (!_isHeaderPrinted) {
            _output.writeAttribute("Constant Pool");
            _output.write(':');
            _output.writeLine();
            _isHeaderPrinted = true;
        }

        _output.indent();
        _output.writeLiteral(format("%1$5d", entry.index));
        _output.write(": ");

        printTag(entry.getTag());
        entry.accept(this);

        _output.writeLine();
        _output.unindent();
    }

    @Override
    public void visitTypeInfo(final ConstantPool.TypeInfoEntry info) {
        _output.writeDelimiter("#");
        _output.writeLiteral(format("%1$-14d", info.nameIndex));
        _output.writeComment(format("//  %1$s", StringUtilities.escape(info.getName(), false, _settings.isUnicodeOutputEnabled())));
    }

    @Override
    public void visitDoubleConstant(final ConstantPool.DoubleConstantEntry info) {
        DecompilerHelpers.writePrimitiveValue(_output, info.getConstantValue());
    }

    @Override
    public void visitFieldReference(final ConstantPool.FieldReferenceEntry info) {
        final ConstantPool.NameAndTypeDescriptorEntry nameAndTypeInfo = info.getNameAndTypeInfo();
        final int startColumn = _output.getColumn();

        _output.writeDelimiter("#");
        _output.writeLiteral(info.typeInfoIndex);
        _output.writeDelimiter(".");
        _output.writeDelimiter("#");
        _output.writeLiteral(info.nameAndTypeDescriptorIndex);

        final int endColumn = _output.getColumn();
        final int padding = (14 - (endColumn - startColumn));
        final String paddingText = padding > 0 ? StringUtilities.repeat(' ', padding) : "";

        _output.writeComment(
            format(
                paddingText + " //  %1$s.%2$s:%3$s",
                StringUtilities.escape(info.getClassName(), false, _settings.isUnicodeOutputEnabled()),
                StringUtilities.escape(nameAndTypeInfo.getName(), false, _settings.isUnicodeOutputEnabled()),
                StringUtilities.escape(nameAndTypeInfo.getType(), false, _settings.isUnicodeOutputEnabled())
            )
        );
    }

    @Override
    public void visitFloatConstant(final ConstantPool.FloatConstantEntry info) {
        DecompilerHelpers.writePrimitiveValue(_output, info.getConstantValue());
    }

    @Override
    public void visitIntegerConstant(final ConstantPool.IntegerConstantEntry info) {
        DecompilerHelpers.writePrimitiveValue(_output, info.getConstantValue());
    }

    @Override
    public void visitInterfaceMethodReference(final ConstantPool.InterfaceMethodReferenceEntry info) {
        final ConstantPool.NameAndTypeDescriptorEntry nameAndTypeInfo = info.getNameAndTypeInfo();
        final int startColumn = _output.getColumn();

        _output.writeDelimiter("#");
        _output.writeLiteral(info.typeInfoIndex);
        _output.writeDelimiter(".");
        _output.writeDelimiter("#");
        _output.writeLiteral(info.nameAndTypeDescriptorIndex);

        final int endColumn = _output.getColumn();
        final int padding = (14 - (endColumn - startColumn));
        final String paddingText = padding > 0 ? StringUtilities.repeat(' ', padding) : "";

        _output.writeComment(
            format(
                paddingText + " //  %1$s.%2$s:%3$s",
                StringUtilities.escape(info.getClassName(), false, _settings.isUnicodeOutputEnabled()),
                StringUtilities.escape(nameAndTypeInfo.getName(), false, _settings.isUnicodeOutputEnabled()),
                StringUtilities.escape(nameAndTypeInfo.getType(), false, _settings.isUnicodeOutputEnabled())
            )
        );
    }

    @Override
    public void visitInvokeDynamicInfo(final ConstantPool.InvokeDynamicInfoEntry info) {
        final ConstantPool.NameAndTypeDescriptorEntry nameAndTypeInfo = info.getNameAndTypeDescriptor();
        final int startColumn = _output.getColumn();

        _output.writeLiteral(info.bootstrapMethodAttributeIndex);
        _output.writeDelimiter(", ");
        _output.writeDelimiter("#");
        _output.writeLiteral(nameAndTypeInfo.nameIndex);
        _output.writeDelimiter(".");
        _output.writeDelimiter("#");
        _output.writeLiteral(nameAndTypeInfo.typeDescriptorIndex);

        final int endColumn = _output.getColumn();
        final int padding = (14 - (endColumn - startColumn));
        final String paddingText = padding > 0 ? StringUtilities.repeat(' ', padding) : "";

        _output.writeComment(
            format(
                paddingText + " //  %1$s:%2$s",
                StringUtilities.escape(nameAndTypeInfo.getName(), false, _settings.isUnicodeOutputEnabled()),
                StringUtilities.escape(nameAndTypeInfo.getType(), false, _settings.isUnicodeOutputEnabled())
            )
        );
    }

    @Override
    public void visitLongConstant(final ConstantPool.LongConstantEntry info) {
        DecompilerHelpers.writePrimitiveValue(_output, info.getConstantValue());
    }

    @Override
    public void visitNameAndTypeDescriptor(final ConstantPool.NameAndTypeDescriptorEntry info) {
        final int startColumn = _output.getColumn();

        _output.writeDelimiter("#");
        _output.writeLiteral(info.nameIndex);
        _output.writeDelimiter(".");
        _output.writeDelimiter("#");
        _output.writeLiteral(info.typeDescriptorIndex);

        final int endColumn = _output.getColumn();
        final int padding = (14 - (endColumn - startColumn));
        final String paddingText = padding > 0 ? StringUtilities.repeat(' ', padding) : "";

        _output.writeComment(
            format(
                paddingText + " //  %1$s:%2$s",
                StringUtilities.escape(info.getName(), false, _settings.isUnicodeOutputEnabled()),
                StringUtilities.escape(info.getType(), false, _settings.isUnicodeOutputEnabled())
            )
        );
    }

    @Override
    public void visitMethodReference(final ConstantPool.MethodReferenceEntry info) {
        final ConstantPool.NameAndTypeDescriptorEntry nameAndTypeInfo = info.getNameAndTypeInfo();
        final int startColumn = _output.getColumn();

        _output.writeDelimiter("#");
        _output.writeLiteral(info.typeInfoIndex);
        _output.writeDelimiter(".");
        _output.writeDelimiter("#");
        _output.writeLiteral(info.nameAndTypeDescriptorIndex);

        final int endColumn = _output.getColumn();
        final int padding = (14 - (endColumn - startColumn));
        final String paddingText = padding > 0 ? StringUtilities.repeat(' ', padding) : "";

        _output.writeComment(
            format(
                paddingText + " //  %1$s.%2$s:%3$s",
                StringUtilities.escape(info.getClassName(), false, _settings.isUnicodeOutputEnabled()),
                StringUtilities.escape(nameAndTypeInfo.getName(), false, _settings.isUnicodeOutputEnabled()),
                StringUtilities.escape(nameAndTypeInfo.getType(), false, _settings.isUnicodeOutputEnabled())
            )
        );
    }

    @Override
    public void visitMethodHandle(final ConstantPool.MethodHandleEntry info) {
        final ConstantPool.ReferenceEntry reference = info.getReference();
        final ConstantPool.NameAndTypeDescriptorEntry nameAndTypeInfo = reference.getNameAndTypeInfo();
        final int startColumn = _output.getColumn();

        _output.writeLiteral(info.referenceKind);
        _output.write(' ');
        _output.writeDelimiter("#");
        _output.writeLiteral(reference.typeInfoIndex);
        _output.writeDelimiter(".");
        _output.writeDelimiter("#");
        _output.writeLiteral(reference.nameAndTypeDescriptorIndex);

        final int endColumn = _output.getColumn();
        final int padding = (28 - (endColumn - startColumn));
        final String paddingText = padding > 0 ? StringUtilities.repeat(' ', padding) : "";

        _output.writeComment(
            format(
                paddingText + " //  %1$s.%2$s:%3$s",
                StringUtilities.escape(reference.getClassName(), false, _settings.isUnicodeOutputEnabled()),
                StringUtilities.escape(nameAndTypeInfo.getName(), false, _settings.isUnicodeOutputEnabled()),
                StringUtilities.escape(nameAndTypeInfo.getType(), false, _settings.isUnicodeOutputEnabled())
            )
        );
    }

    @Override
    public void visitMethodType(final ConstantPool.MethodTypeEntry info) {
        IMethodSignature signature;

        final String text = info.getType();

        try {
            signature = MetadataParser.unbound().parseMethodSignature(text);
        }
        catch (final Throwable ignored) {
            signature = null;
        }

        _output.writeReference(text, signature);
    }

    @Override
    public void visitStringConstant(final ConstantPool.StringConstantEntry info) {
        _output.writeDelimiter("#");
        _output.writeLiteral(format("%1$-14d", info.stringIndex));
        _output.writeComment(format("//  %1$s", StringUtilities.escape(info.getValue(), true, _settings.isUnicodeOutputEnabled())));
    }

    @Override
    public void visitUtf8StringConstant(final ConstantPool.Utf8StringConstantEntry info) {
        DecompilerHelpers.writePrimitiveValue(_output, info.getConstantValue());
    }

    @Override
    public void visitEnd() {
    }
}
