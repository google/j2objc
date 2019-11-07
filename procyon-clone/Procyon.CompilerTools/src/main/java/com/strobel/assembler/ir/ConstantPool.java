/*
 * ConstantPool.java
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

package com.strobel.assembler.ir;

import com.strobel.assembler.metadata.Buffer;
import com.strobel.assembler.metadata.FieldReference;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.Freezable;
import com.strobel.core.HashUtilities;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

@SuppressWarnings({ "PublicField", "ProtectedField" })
public final class ConstantPool extends Freezable implements Iterable<ConstantPool.Entry> {
    private final ArrayList<Entry> _pool = new ArrayList<>();
    private final HashMap<Key, Entry> _entryMap = new HashMap<>();
    private final Key _lookupKey = new Key();
    private final Key _newKey = new Key();

    private int _size;

    @Override
    public Iterator<Entry> iterator() {
        return _pool.iterator();
    }

    public void accept(final Visitor visitor) {
        VerifyArgument.notNull(visitor, "visitor");

        for (final Entry entry : _pool) {
            if (entry != null) {
                visitor.visit(entry);
            }
        }
    }

    public void write(final Buffer stream) {
        stream.writeShort(_size + 1);
        accept(new Writer(stream));
    }

    @SuppressWarnings("unchecked")
    public <T extends Entry> T getEntry(final int index) {
        VerifyArgument.inRange(0, _size + 1, index, "index");

        final Entry info = _pool.get(index - 1);

        if (info == null) {
            throw new IndexOutOfBoundsException();
        }

        return (T) info;
    }

    public Entry get(final int index) {
        VerifyArgument.inRange(0, _size + 1, index, "index");

        final Entry info = _pool.get(index - 1);

        if (info == null) {
            throw new IndexOutOfBoundsException();
        }

        return info;
    }

    public Entry get(final int index, final Tag expectedType) {
        VerifyArgument.inRange(0, _size + 1, index, "index");

        final Entry entry = get(index);
        final Tag actualType = entry.getTag();

        if (actualType != expectedType) {
            throw new IllegalStateException(
                String.format(
                    "Expected type '%s' but found type '%s'.",
                    expectedType,
                    actualType
                )
            );
        }

        return entry;
    }

    public String lookupStringConstant(final int index) {
        final StringConstantEntry entry = (StringConstantEntry) get(index, Tag.StringConstant);
        return entry.getValue();
    }

    public String lookupUtf8Constant(final int index) {
        final Utf8StringConstantEntry entry = (Utf8StringConstantEntry) get(index, Tag.Utf8StringConstant);
        return entry.value;
    }

    @SuppressWarnings("unchecked")
    public <T> T lookupConstant(final int index) {
        final ConstantEntry entry = (ConstantEntry) get(index);
        return (T) entry.getConstantValue();
    }

    public int lookupIntegerConstant(final int index) {
        final IntegerConstantEntry entry = (IntegerConstantEntry) get(index, Tag.IntegerConstant);
        return entry.value;
    }

    public long lookupLongConstant(final int index) {
        final LongConstantEntry entry = (LongConstantEntry) get(index, Tag.LongConstant);
        return entry.value;
    }

    public float lookupFloatConstant(final int index) {
        final FloatConstantEntry entry = (FloatConstantEntry) get(index, Tag.FloatConstant);
        return entry.value;
    }

    public double lookupDoubleConstant(final int index) {
        final DoubleConstantEntry entry = (DoubleConstantEntry) get(index, Tag.DoubleConstant);
        return entry.value;
    }

    public Utf8StringConstantEntry getUtf8StringConstant(final String value) {
        _lookupKey.set(value);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            if (isFrozen()) {
                return null;
            }
            entry = new Utf8StringConstantEntry(this, value);
        }
        _lookupKey.clear();
        return (Utf8StringConstantEntry) entry;
    }

    public StringConstantEntry getStringConstant(final String value) {
        final Utf8StringConstantEntry utf8Constant = getUtf8StringConstant(value);
        _lookupKey.set(Tag.StringConstant, utf8Constant.index);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            if (isFrozen()) {
                return null;
            }
            entry = new StringConstantEntry(this, utf8Constant.index);
        }
        _lookupKey.clear();
        return (StringConstantEntry) entry;
    }

    public IntegerConstantEntry getIntegerConstant(final int value) {
        _lookupKey.set(value);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            if (isFrozen()) {
                return null;
            }
            entry = new IntegerConstantEntry(this, value);
        }
        _lookupKey.clear();
        return (IntegerConstantEntry) entry;
    }

    public FloatConstantEntry getFloatConstant(final float value) {
        _lookupKey.set(value);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            if (isFrozen()) {
                return null;
            }
            entry = new FloatConstantEntry(this, value);
        }
        _lookupKey.clear();
        return (FloatConstantEntry) entry;
    }

    public LongConstantEntry getLongConstant(final long value) {
        _lookupKey.set(value);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            if (isFrozen()) {
                return null;
            }
            entry = new LongConstantEntry(this, value);
        }
        _lookupKey.clear();
        return (LongConstantEntry) entry;
    }

    public DoubleConstantEntry getDoubleConstant(final double value) {
        _lookupKey.set(value);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            if (isFrozen()) {
                return null;
            }
            entry = new DoubleConstantEntry(this, value);
        }
        _lookupKey.clear();
        return (DoubleConstantEntry) entry;
    }

    public TypeInfoEntry getTypeInfo(final TypeReference type) {
        final Utf8StringConstantEntry name = getUtf8StringConstant(type.getInternalName());
        _lookupKey.set(Tag.TypeInfo, name.index);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            if (isFrozen()) {
                return null;
            }
            entry = new TypeInfoEntry(this, name.index);
        }
        _lookupKey.clear();
        return (TypeInfoEntry) entry;
    }

    public FieldReferenceEntry getFieldReference(final FieldReference field) {
        final TypeInfoEntry typeInfo = getTypeInfo(field.getDeclaringType());
        final NameAndTypeDescriptorEntry nameAndDescriptor = getNameAndTypeDescriptor(
            field.getName(),
            field.getErasedSignature()
        );
        _lookupKey.set(Tag.FieldReference, typeInfo.index, nameAndDescriptor.index);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            if (isFrozen()) {
                return null;
            }
            entry = new FieldReferenceEntry(this, typeInfo.index, nameAndDescriptor.index);
        }
        _lookupKey.clear();
        return (FieldReferenceEntry) entry;
    }

    public MethodReferenceEntry getMethodReference(final MethodReference method) {
        final TypeInfoEntry typeInfo = getTypeInfo(method.getDeclaringType());
        final NameAndTypeDescriptorEntry nameAndDescriptor = getNameAndTypeDescriptor(
            method.getName(),
            method.getErasedSignature()
        );
        _lookupKey.set(Tag.MethodReference, typeInfo.index, nameAndDescriptor.index);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            if (isFrozen()) {
                return null;
            }
            entry = new MethodReferenceEntry(this, typeInfo.index, nameAndDescriptor.index);
        }
        _lookupKey.clear();
        return (MethodReferenceEntry) entry;
    }

    public InterfaceMethodReferenceEntry getInterfaceMethodReference(final MethodReference method) {
        final TypeInfoEntry typeInfo = getTypeInfo(method.getDeclaringType());
        final NameAndTypeDescriptorEntry nameAndDescriptor = getNameAndTypeDescriptor(
            method.getName(),
            method.getErasedSignature()
        );
        _lookupKey.set(Tag.InterfaceMethodReference, typeInfo.index, nameAndDescriptor.index);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            if (isFrozen()) {
                return null;
            }
            entry = new InterfaceMethodReferenceEntry(this, typeInfo.index, nameAndDescriptor.index);
        }
        _lookupKey.clear();
        return (InterfaceMethodReferenceEntry) entry;
    }

    NameAndTypeDescriptorEntry getNameAndTypeDescriptor(final String name, final String typeDescriptor) {
        final Utf8StringConstantEntry utf8Name = getUtf8StringConstant(name);
        final Utf8StringConstantEntry utf8Descriptor = getUtf8StringConstant(typeDescriptor);
        _lookupKey.set(Tag.NameAndTypeDescriptor, utf8Name.index, utf8Descriptor.index);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            if (isFrozen()) {
                return null;
            }
            entry = new NameAndTypeDescriptorEntry(this, utf8Name.index, utf8Descriptor.index);
        }
        _lookupKey.clear();
        return (NameAndTypeDescriptorEntry) entry;
    }

    MethodHandleEntry getMethodHandle(final ReferenceKind referenceKind, final int referenceIndex) {
        _lookupKey.set(Tag.MethodHandle, referenceIndex, referenceKind);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            if (isFrozen()) {
                return null;
            }
            entry = new MethodHandleEntry(this, referenceKind, referenceIndex);
        }
        _lookupKey.clear();
        return (MethodHandleEntry) entry;
    }

    MethodTypeEntry getMethodType(final int descriptorIndex) {
        _lookupKey.set(Tag.MethodType, descriptorIndex);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            if (isFrozen()) {
                return null;
            }
            entry = new MethodTypeEntry(this, descriptorIndex);
        }
        _lookupKey.clear();
        return (MethodTypeEntry) entry;
    }

    InvokeDynamicInfoEntry getInvokeDynamicInfo(
        final int bootstrapMethodAttributeIndex,
        final int nameAndTypeDescriptorIndex) {
        _lookupKey.set(Tag.InvokeDynamicInfo, bootstrapMethodAttributeIndex, nameAndTypeDescriptorIndex);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            if (isFrozen()) {
                return null;
            }
            entry = new InvokeDynamicInfoEntry(this, bootstrapMethodAttributeIndex, nameAndTypeDescriptorIndex);
        }
        _lookupKey.clear();
        return (InvokeDynamicInfoEntry) entry;
    }

    public static ConstantPool read(final Buffer b) {
        boolean skipOne = false;

        final ConstantPool pool = new ConstantPool();
        final int size = b.readUnsignedShort();
        final Key key = new Key();

        for (int i = 1; i < size; i++) {
            if (skipOne) {
                skipOne = false;
                continue;
            }

            key.clear();

            final Tag tag = Tag.fromValue(b.readUnsignedByte());

            switch (tag) {
                case Utf8StringConstant:
                    new Utf8StringConstantEntry(pool, b.readUtf8());
                    break;
                case IntegerConstant:
                    new IntegerConstantEntry(pool, b.readInt());
                    break;
                case FloatConstant:
                    new FloatConstantEntry(pool, b.readFloat());
                    break;
                case LongConstant:
                    new LongConstantEntry(pool, b.readLong());
                    skipOne = true;
                    break;
                case DoubleConstant:
                    new DoubleConstantEntry(pool, b.readDouble());
                    skipOne = true;
                    break;
                case TypeInfo:
                    new TypeInfoEntry(pool, b.readUnsignedShort());
                    break;
                case StringConstant:
                    new StringConstantEntry(pool, b.readUnsignedShort());
                    break;
                case FieldReference:
                    new FieldReferenceEntry(pool, b.readUnsignedShort(), b.readUnsignedShort());
                    break;
                case MethodReference:
                    new MethodReferenceEntry(pool, b.readUnsignedShort(), b.readUnsignedShort());
                    break;
                case InterfaceMethodReference:
                    new InterfaceMethodReferenceEntry(pool, b.readUnsignedShort(), b.readUnsignedShort());
                    break;
                case NameAndTypeDescriptor:
                    new NameAndTypeDescriptorEntry(pool, b.readUnsignedShort(), b.readUnsignedShort());
                    break;
                case MethodHandle:
                    new MethodHandleEntry(pool, ReferenceKind.fromTag(b.readUnsignedByte()), b.readUnsignedShort());
                    break;
                case MethodType:
                    new MethodTypeEntry(pool, b.readUnsignedShort());
                    break;
                case InvokeDynamicInfo:
                    new InvokeDynamicInfoEntry(pool, b.readUnsignedShort(), b.readUnsignedShort());
                    break;
            }
        }

        return pool;
    }

    // <editor-fold defaultstate="collapsed" desc="Entry Base Class">

    public static abstract class Entry {
        public final int index;

        protected final ConstantPool owner;

        Entry(final ConstantPool owner) {
            this.owner = owner;
            this.index = owner._size + 1;
            owner._pool.add(this);
            owner._size += size();
            for (int i = 1; i < size(); i++) {
                owner._pool.add(null);
            }
        }

        abstract void fixupKey(final Key key);

        public abstract Tag getTag();

        /**
         * The number of slots in the constant pool used by this entry. 2 for DoubleConstantEntry and LongConstantEntry; 1 for everything else.
         */
        public int size() {
            return 1;
        }

        public abstract int byteLength();

        public abstract void accept(final Visitor visitor);
    }

    public static abstract class ConstantEntry extends Entry {
        ConstantEntry(final ConstantPool owner) {
            super(owner);
        }

        public abstract Object getConstantValue();
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ReferenceKind Enum">

    public static enum ReferenceKind {
        GetField(1, "getfield"),
        GetStatic(2, "getstatic"),
        PutField(3, "putfield"),
        PutStatic(4, "putstatic"),
        InvokeVirtual(5, "invokevirtual"),
        InvokeStatic(6, "invokestatic"),
        InvokeSpecial(7, "invokespecial"),
        NewInvokeSpecial(8, "newinvokespecial"),
        InvokeInterface(9, "invokeinterface");

        public final int tag;
        public final String name;

        ReferenceKind(final int tag, final String name) {
            this.tag = tag;
            this.name = name;
        }

        static ReferenceKind fromTag(final int tag) {
            switch (tag) {
                case 1:
                    return GetField;
                case 2:
                    return GetStatic;
                case 3:
                    return PutField;
                case 4:
                    return PutStatic;
                case 5:
                    return InvokeVirtual;
                case 6:
                    return InvokeStatic;
                case 7:
                    return InvokeSpecial;
                case 8:
                    return NewInvokeSpecial;
                case 9:
                    return InvokeInterface;
                default:
                    return null;
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Tag Enum">

    public static enum Tag {
        Utf8StringConstant(1),
        IntegerConstant(3),
        FloatConstant(4),
        LongConstant(5),
        DoubleConstant(6),
        TypeInfo(7),
        StringConstant(8),
        FieldReference(9),
        MethodReference(10),
        InterfaceMethodReference(11),
        NameAndTypeDescriptor(12),
        MethodHandle(15),
        MethodType(16),
        InvokeDynamicInfo(18);

        public final int value;

        Tag(final int value) {
            this.value = value;
        }

        public static Tag fromValue(final int value) {
            VerifyArgument.inRange(Tag.Utf8StringConstant.value, Tag.InvokeDynamicInfo.value, value, "value");
            return lookup[value];
        }

        private final static Tag[] lookup;

        static {
            final Tag[] values = Tag.values();

            lookup = new Tag[Tag.InvokeDynamicInfo.value + 1];

            for (final Tag tag : values) {
                lookup[tag.value] = tag;
            }
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Visitor Interface">

    public interface Visitor {
        void visit(Entry entry);
        void visitTypeInfo(TypeInfoEntry info);
        void visitDoubleConstant(DoubleConstantEntry info);
        void visitFieldReference(FieldReferenceEntry info);
        void visitFloatConstant(FloatConstantEntry info);
        void visitIntegerConstant(IntegerConstantEntry info);
        void visitInterfaceMethodReference(InterfaceMethodReferenceEntry info);
        void visitInvokeDynamicInfo(InvokeDynamicInfoEntry info);
        void visitLongConstant(LongConstantEntry info);
        void visitNameAndTypeDescriptor(NameAndTypeDescriptorEntry info);
        void visitMethodReference(MethodReferenceEntry info);
        void visitMethodHandle(MethodHandleEntry info);
        void visitMethodType(MethodTypeEntry info);
        void visitStringConstant(StringConstantEntry info);
        void visitUtf8StringConstant(Utf8StringConstantEntry info);
        void visitEnd();

        // <editor-fold defaultstate="collapsed" desc="Empty Visitor (No-Op)">

        public static final Visitor EMPTY = new Visitor() {
            @Override
            public void visit(final Entry entry) {
            }

            @Override
            public void visitTypeInfo(final TypeInfoEntry info) {
            }

            @Override
            public void visitDoubleConstant(final DoubleConstantEntry info) {
            }

            @Override
            public void visitFieldReference(final FieldReferenceEntry info) {
            }

            @Override
            public void visitFloatConstant(final FloatConstantEntry info) {
            }

            @Override
            public void visitIntegerConstant(final IntegerConstantEntry info) {
            }

            @Override
            public void visitInterfaceMethodReference(final InterfaceMethodReferenceEntry info) {
            }

            @Override
            public void visitInvokeDynamicInfo(final InvokeDynamicInfoEntry info) {
            }

            @Override
            public void visitLongConstant(final LongConstantEntry info) {
            }

            @Override
            public void visitNameAndTypeDescriptor(final NameAndTypeDescriptorEntry info) {
            }

            @Override
            public void visitMethodReference(final MethodReferenceEntry info) {
            }

            @Override
            public void visitMethodHandle(final MethodHandleEntry info) {
            }

            @Override
            public void visitMethodType(final MethodTypeEntry info) {
            }

            @Override
            public void visitStringConstant(final StringConstantEntry info) {
            }

            @Override
            public void visitUtf8StringConstant(final Utf8StringConstantEntry info) {
            }

            @Override
            public void visitEnd() {
            }
        };

        // </editor-fold>
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Writer Class">

    private final static class Writer implements Visitor {
        private final Buffer codeStream;

        private Writer(final Buffer codeStream) {
            this.codeStream = VerifyArgument.notNull(codeStream, "codeStream");
        }

        @Override
        public void visit(final Entry entry) {
            entry.accept(this);
        }

        @Override
        public void visitTypeInfo(final TypeInfoEntry info) {
            codeStream.writeByte(info.getTag().value);
            codeStream.writeShort(info.nameIndex);
        }

        @Override
        public void visitDoubleConstant(final DoubleConstantEntry info) {
            codeStream.writeByte(info.getTag().value);
            codeStream.writeDouble(info.value);
        }

        @Override
        public void visitFieldReference(final FieldReferenceEntry info) {
            codeStream.writeByte(info.getTag().value);
            codeStream.writeShort(info.typeInfoIndex);
            codeStream.writeShort(info.nameAndTypeDescriptorIndex);
        }

        @Override
        public void visitFloatConstant(final FloatConstantEntry info) {
            codeStream.writeByte(info.getTag().value);
            codeStream.writeFloat(info.value);
        }

        @Override
        public void visitIntegerConstant(final IntegerConstantEntry info) {
            codeStream.writeByte(info.getTag().value);
            codeStream.writeInt(info.value);
        }

        @Override
        public void visitInterfaceMethodReference(final InterfaceMethodReferenceEntry info) {
            codeStream.writeByte(info.getTag().value);
            codeStream.writeShort(info.typeInfoIndex);
            codeStream.writeShort(info.nameAndTypeDescriptorIndex);
        }

        @Override
        public void visitInvokeDynamicInfo(final InvokeDynamicInfoEntry info) {
            codeStream.writeByte(info.getTag().value);
            codeStream.writeShort(info.bootstrapMethodAttributeIndex);
            codeStream.writeShort(info.nameAndTypeDescriptorIndex);
        }

        @Override
        public void visitLongConstant(final LongConstantEntry info) {
            codeStream.writeByte(info.getTag().value);
            codeStream.writeLong(info.value);
        }

        @Override
        public void visitNameAndTypeDescriptor(final NameAndTypeDescriptorEntry info) {
            codeStream.writeByte(info.getTag().value);
            codeStream.writeShort(info.nameIndex);
            codeStream.writeShort(info.typeDescriptorIndex);
        }

        @Override
        public void visitMethodReference(final MethodReferenceEntry info) {
            codeStream.writeByte(info.getTag().value);
            codeStream.writeShort(info.typeInfoIndex);
            codeStream.writeShort(info.nameAndTypeDescriptorIndex);
        }

        @Override
        public void visitMethodHandle(final MethodHandleEntry info) {
            codeStream.writeByte(info.getTag().value);
            codeStream.writeShort(info.referenceKind.ordinal());
            codeStream.writeShort(info.referenceIndex);
        }

        @Override
        public void visitMethodType(final MethodTypeEntry info) {
            codeStream.writeByte(info.getTag().value);
            codeStream.writeShort(info.descriptorIndex);
        }

        @Override
        public void visitStringConstant(final StringConstantEntry info) {
            codeStream.writeByte(info.getTag().value);
            codeStream.writeShort(info.stringIndex);
        }

        @Override
        public void visitUtf8StringConstant(final Utf8StringConstantEntry info) {
            codeStream.writeByte(info.getTag().value);
            codeStream.writeUtf8(info.value);
        }

        @Override
        public void visitEnd() {
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Entry Implementations">

    public static final class TypeInfoEntry extends Entry {
        public final int nameIndex;

        public TypeInfoEntry(final ConstantPool owner, final int nameIndex) {
            super(owner);
            this.nameIndex = nameIndex;
            owner._newKey.set(getTag(), nameIndex);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
        }

        public String getName() {
            return ((Utf8StringConstantEntry) owner.get(nameIndex, Tag.Utf8StringConstant)).value;
        }

        @Override
        void fixupKey(final Key key) {
            key.set(Tag.TypeInfo, nameIndex);
        }

        @Override
        public Tag getTag() {
            return Tag.TypeInfo;
        }

        @Override
        public int byteLength() {
            return 3;
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visitTypeInfo(this);
        }

        @Override
        public String toString() {
            return "TypeIndex[index: " + index + ", nameIndex: " + nameIndex + "]";
        }
    }

    public static final class MethodTypeEntry extends Entry {
        public final int descriptorIndex;

        public MethodTypeEntry(final ConstantPool owner, final int descriptorIndex) {
            super(owner);
            this.descriptorIndex = descriptorIndex;
            owner._newKey.set(getTag(), descriptorIndex);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
        }

        public String getType() {
            return ((Utf8StringConstantEntry) owner.get(descriptorIndex, Tag.Utf8StringConstant)).value;
        }

        @Override
        void fixupKey(final Key key) {
            key.set(Tag.MethodType, descriptorIndex);
        }

        @Override
        public Tag getTag() {
            return Tag.MethodType;
        }

        @Override
        public int byteLength() {
            return 3;
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visitMethodType(this);
        }

        @Override
        public String toString() {
            return "MethodTypeEntry[index: " + index + ", descriptorIndex: " + descriptorIndex + "]";
        }
    }

    public static abstract class ReferenceEntry extends Entry {
        public final Tag tag;
        public final int typeInfoIndex;
        public final int nameAndTypeDescriptorIndex;

        protected ReferenceEntry(final ConstantPool cp, final Tag tag, final int typeInfoIndex, final int nameAndTypeDescriptorIndex) {
            super(cp);
            this.tag = tag;
            this.typeInfoIndex = typeInfoIndex;
            this.nameAndTypeDescriptorIndex = nameAndTypeDescriptorIndex;
            owner._newKey.set(tag, typeInfoIndex, nameAndTypeDescriptorIndex);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
        }

        public Tag getTag() {
            return tag;
        }

        public int byteLength() {
            return 5;
        }

        public TypeInfoEntry getClassInfo() {
            return (TypeInfoEntry) owner.get(typeInfoIndex, Tag.TypeInfo);
        }

        public String getClassName() {
            return getClassInfo().getName();
        }

        public NameAndTypeDescriptorEntry getNameAndTypeInfo() {
            return (NameAndTypeDescriptorEntry) owner.get(nameAndTypeDescriptorIndex, Tag.NameAndTypeDescriptor);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[index: " +
                   index +
                   ", typeInfoIndex: " +
                   this.typeInfoIndex +
                   ", nameAndTypeDescriptorIndex: " +
                   this.nameAndTypeDescriptorIndex +
                   "]";
        }
    }

    public static final class FieldReferenceEntry extends ReferenceEntry {
        public FieldReferenceEntry(final ConstantPool owner, final int typeIndex, final int nameAndTypeDescriptorIndex) {
            super(owner, Tag.FieldReference, typeIndex, nameAndTypeDescriptorIndex);
        }

        @Override
        void fixupKey(final Key key) {
            key.set(Tag.FieldReference, typeInfoIndex, nameAndTypeDescriptorIndex);
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visitFieldReference(this);
        }
    }

    public static final class MethodReferenceEntry extends ReferenceEntry {
        public MethodReferenceEntry(final ConstantPool owner, final int typeIndex, final int nameAndTypeDescriptorIndex) {
            super(owner, Tag.MethodReference, typeIndex, nameAndTypeDescriptorIndex);
        }

        @Override
        void fixupKey(final Key key) {
            key.set(Tag.MethodReference, typeInfoIndex, nameAndTypeDescriptorIndex);
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visitMethodReference(this);
        }
    }

    public static final class InterfaceMethodReferenceEntry extends ReferenceEntry {
        public InterfaceMethodReferenceEntry(final ConstantPool owner, final int typeIndex, final int nameAndTypeDescriptorIndex) {
            super(owner, Tag.InterfaceMethodReference, typeIndex, nameAndTypeDescriptorIndex);
        }

        @Override
        void fixupKey(final Key key) {
            key.set(Tag.InterfaceMethodReference, typeInfoIndex, nameAndTypeDescriptorIndex);
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visitInterfaceMethodReference(this);
        }
    }

    public static class MethodHandleEntry extends Entry {
        public final ReferenceKind referenceKind;
        public final int referenceIndex;

        public MethodHandleEntry(final ConstantPool owner, final ReferenceKind referenceKind, final int referenceIndex) {
            super(owner);
            this.referenceKind = referenceKind;
            this.referenceIndex = referenceIndex;
            owner._newKey.set(getTag(), referenceIndex, referenceKind);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
        }

        public ReferenceEntry getReference() {
            final Tag actual = owner.get(referenceIndex).getTag();

            Tag expected = Tag.MethodReference;

            // allow these tag types also:
            switch (actual) {
                case FieldReference:
                case InterfaceMethodReference:
                    expected = actual;
            }

            return (ReferenceEntry) owner.get(referenceIndex, expected);
        }

        @Override
        void fixupKey(final Key key) {
            key.set(Tag.MethodHandle, referenceIndex, referenceKind);
        }

        @Override
        public Tag getTag() {
            return Tag.MethodHandle;
        }

        @Override
        public int byteLength() {
            return 4;
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visitMethodHandle(this);
        }
    }

    public static class NameAndTypeDescriptorEntry extends Entry {
        public final int nameIndex;
        public final int typeDescriptorIndex;

        public NameAndTypeDescriptorEntry(final ConstantPool owner, final int nameIndex, final int typeDescriptorIndex) {
            super(owner);
            this.nameIndex = nameIndex;
            this.typeDescriptorIndex = typeDescriptorIndex;
            owner._newKey.set(getTag(), nameIndex, typeDescriptorIndex);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
        }

        @Override
        void fixupKey(final Key key) {
            key.set(Tag.NameAndTypeDescriptor, nameIndex, typeDescriptorIndex);
        }

        public Tag getTag() {
            return Tag.NameAndTypeDescriptor;
        }

        public int byteLength() {
            return 5;
        }

        public String getName() {
            return ((Utf8StringConstantEntry) owner.get(nameIndex, Tag.Utf8StringConstant)).value;
        }

        public String getType() {
            return ((Utf8StringConstantEntry) owner.get(typeDescriptorIndex, Tag.Utf8StringConstant)).value;
        }

        public void accept(final Visitor visitor) {
            visitor.visitNameAndTypeDescriptor(this);
        }

        @Override
        public String toString() {
            return "NameAndTypeDescriptorEntry[index: " + index + ", descriptorIndex: " + nameIndex + ", typeDescriptorIndex: " + typeDescriptorIndex + "]";
        }
    }

    public static class InvokeDynamicInfoEntry extends Entry {

        public final int bootstrapMethodAttributeIndex;
        public final int nameAndTypeDescriptorIndex;

        public InvokeDynamicInfoEntry(
            final ConstantPool owner,
            final int bootstrapMethodAttributeIndex,
            final int nameAndTypeDescriptorIndex) {

            super(owner);
            this.bootstrapMethodAttributeIndex = bootstrapMethodAttributeIndex;
            this.nameAndTypeDescriptorIndex = nameAndTypeDescriptorIndex;
            owner._newKey.set(getTag(), bootstrapMethodAttributeIndex, nameAndTypeDescriptorIndex);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
        }

        @Override
        void fixupKey(final Key key) {
            key.set(Tag.InvokeDynamicInfo, bootstrapMethodAttributeIndex, nameAndTypeDescriptorIndex);
        }

        public Tag getTag() {
            return Tag.InvokeDynamicInfo;
        }

        public int byteLength() {
            return 5;
        }

        public NameAndTypeDescriptorEntry getNameAndTypeDescriptor() {
            return (NameAndTypeDescriptorEntry) owner.get(nameAndTypeDescriptorIndex, Tag.NameAndTypeDescriptor);
        }

        public void accept(final Visitor visitor) {
            visitor.visitInvokeDynamicInfo(this);
        }

        @Override
        public String toString() {
            return "InvokeDynamicInfoEntry[bootstrapMethodAttributeIndex: " +
                   bootstrapMethodAttributeIndex +
                   ", nameAndTypeDescriptorIndex: " +
                   nameAndTypeDescriptorIndex +
                   "]";
        }
    }

    public static final class DoubleConstantEntry extends ConstantEntry {
        public final double value;

        public DoubleConstantEntry(final ConstantPool owner, final double value) {
            super(owner);
            this.value = value;
            owner._newKey.set(value);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
        }

        @Override
        void fixupKey(final Key key) {
            key.set(value);
        }

        @Override
        public Tag getTag() {
            return Tag.DoubleConstant;
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public int byteLength() {
            return 9;
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visitDoubleConstant(this);
        }

        @Override
        public String toString() {
            return "DoubleConstantEntry[index: " + index + ", value: " + value + "]";
        }

        @Override
        public Object getConstantValue() {
            return value;
        }
    }

    public static final class FloatConstantEntry extends ConstantEntry {
        public final float value;

        public FloatConstantEntry(final ConstantPool owner, final float value) {
            super(owner);
            this.value = value;
            owner._newKey.set(value);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
        }

        @Override
        void fixupKey(final Key key) {
            key.set(value);
        }

        @Override
        public Tag getTag() {
            return Tag.FloatConstant;
        }

        @Override
        public int byteLength() {
            return 5;
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visitFloatConstant(this);
        }

        @Override
        public String toString() {
            return "FloatConstantEntry[index: " + index + ", value: " + value + "]";
        }

        @Override
        public Object getConstantValue() {
            return value;
        }
    }

    public static final class IntegerConstantEntry extends ConstantEntry {
        public final int value;

        public IntegerConstantEntry(final ConstantPool owner, final int value) {
            super(owner);
            this.value = value;
            owner._newKey.set(value);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
        }

        @Override
        void fixupKey(final Key key) {
            key.set(value);
        }

        @Override
        public Tag getTag() {
            return Tag.IntegerConstant;
        }

        @Override
        public int byteLength() {
            return 5;
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visitIntegerConstant(this);
        }

        @Override
        public String toString() {
            return "IntegerConstantEntry[index: " + index + ", value: " + value + "]";
        }

        @Override
        public Object getConstantValue() {
            return value;
        }
    }

    public static final class LongConstantEntry extends ConstantEntry {
        public final long value;

        public LongConstantEntry(final ConstantPool owner, final long value) {
            super(owner);
            this.value = value;
            owner._newKey.set(value);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
        }

        @Override
        void fixupKey(final Key key) {
            key.set(value);
        }

        @Override
        public Tag getTag() {
            return Tag.LongConstant;
        }

        @Override
        public int byteLength() {
            return 9;
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visitLongConstant(this);
        }

        @Override
        public String toString() {
            return "LongConstantEntry[index: " + index + ", value: " + value + "]";
        }

        @Override
        public Object getConstantValue() {
            return value;
        }
    }

    public static final class StringConstantEntry extends ConstantEntry {
        public final int stringIndex;

        public StringConstantEntry(final ConstantPool owner, final int stringIndex) {
            super(owner);
            this.stringIndex = stringIndex;
            owner._newKey.set(getTag(), stringIndex);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
        }

        public String getValue() {
            return ((Utf8StringConstantEntry) owner.get(stringIndex)).value;
        }

        @Override
        void fixupKey(final Key key) {
            key.set(Tag.StringConstant, stringIndex);
        }

        @Override
        public Tag getTag() {
            return Tag.StringConstant;
        }

        @Override
        public int byteLength() {
            return 3;
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visitStringConstant(this);
        }

        @Override
        public String toString() {
            return "StringConstantEntry[index: " + index + ", stringIndex: " + stringIndex + "]";
        }

        @Override
        public Object getConstantValue() {
            return getValue();
        }
    }

    public static final class Utf8StringConstantEntry extends ConstantEntry {
        public final String value;

        public Utf8StringConstantEntry(final ConstantPool owner, final String value) {
            super(owner);
            this.value = value;
            owner._newKey.set(getTag(), value);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
        }

        @Override
        void fixupKey(final Key key) {
            key.set(value);
        }

        @Override
        public Tag getTag() {
            return Tag.Utf8StringConstant;
        }

        @Override
        public int byteLength() {
            class SizeOutputStream extends OutputStream {
                @Override
                public void write(final int b) {
                    size++;
                }

                private int size;
            }

            final SizeOutputStream sizeOut = new SizeOutputStream();
            final DataOutputStream out = new DataOutputStream(sizeOut);

            try {
                out.writeUTF(value);
            }
            catch (IOException ignore) {
            }

            return 1 + sizeOut.size;
        }

        @Override
        public void accept(final Visitor visitor) {
            visitor.visitUtf8StringConstant(this);
        }

        @Override
        public String toString() {
            return "Utf8StringConstantEntry[index: " + index + ", value: " + value + "]";
        }

        @Override
        public Object getConstantValue() {
            return value;
        }
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Key Class">

    private static final class Key {
        private Tag _tag;
        private int _intValue;
        private long _longValue;
        private String _stringValue1;
        private String _stringValue2;
        private int _refIndex1 = -1;
        private int _refIndex2 = -1;
        private int _hashCode;

        public void clear() {
            _tag = null;
            _intValue = 0;
            _longValue = 0L;
            _stringValue1 = null;
            _stringValue2 = null;
            _refIndex1 = -1;
            _refIndex2 = -1;
        }

        public void set(final int intValue) {
            _tag = Tag.IntegerConstant;
            _intValue = intValue;
            _hashCode = 0x7FFFFFFF & (_tag.value + _intValue);
        }

        public void set(final long longValue) {
            _tag = Tag.LongConstant;
            _longValue = longValue;
            _hashCode = 0x7FFFFFFF & (_tag.value + (int) longValue);
        }

        public void set(final float floatValue) {
            _tag = Tag.FloatConstant;
            _intValue = Float.floatToIntBits(floatValue);
            _hashCode = 0x7FFFFFFF & (_tag.value + _intValue);
        }

        public void set(final double doubleValue) {
            _tag = Tag.DoubleConstant;
            _longValue = Double.doubleToLongBits(doubleValue);
            _hashCode = 0x7FFFFFFF & (_tag.value + (int) _longValue);
        }

        public void set(final String utf8Value) {
            _tag = Tag.Utf8StringConstant;
            _stringValue1 = utf8Value;
            _hashCode = HashUtilities.combineHashCodes(_tag, utf8Value);
        }

        public void set(
            final Tag tag,
            final int refIndex1,
            final ReferenceKind refKind) {

            _tag = tag;
            _refIndex1 = refIndex1;
            _refIndex2 = refKind.tag;
            _hashCode = HashUtilities.combineHashCodes(tag, refIndex1);
        }

        public void set(
            final Tag tag,
            final int refIndex1) {

            _tag = tag;
            _refIndex1 = refIndex1;
            _hashCode = HashUtilities.combineHashCodes(tag, refIndex1);
        }

        public void set(
            final Tag tag,
            final int refIndex1,
            final int refIndex2) {

            _tag = tag;
            _refIndex1 = refIndex1;
            _refIndex2 = refIndex2;
            _hashCode = HashUtilities.combineHashCodes(tag, refIndex1, refIndex2);
        }

        public void set(
            final Tag tag,
            final String stringValue1) {

            _tag = tag;
            _stringValue1 = stringValue1;
            _hashCode = HashUtilities.combineHashCodes(tag, stringValue1);
        }

        public void set(
            final Tag tag,
            final String stringValue1,
            final String stringValue2) {

            _tag = tag;
            _stringValue1 = stringValue1;
            _stringValue2 = stringValue2;
            _hashCode = HashUtilities.combineHashCodes(tag, stringValue1, stringValue2);
        }

        @Override
        @SuppressWarnings("CloneDoesntCallSuperClone")
        protected Key clone() {
            final Key key = new Key();
            key._tag = _tag;
            key._hashCode = _hashCode;
            key._intValue = _intValue;
            key._longValue = _longValue;
            key._stringValue1 = _stringValue1;
            key._stringValue2 = _stringValue2;
            key._refIndex1 = _refIndex1;
            key._refIndex2 = _refIndex2;
            return key;
        }

        @Override
        public int hashCode() {
            return _hashCode;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }

            final Key key = (Key) obj;
            if (key._tag != _tag) {
                return false;
            }

            switch (_tag) {
                case Utf8StringConstant:
                    return StringUtilities.equals(key._stringValue1, _stringValue1);

                case IntegerConstant:
                case FloatConstant:
                    return key._intValue == _intValue;

                case LongConstant:
                case DoubleConstant:
                    return key._longValue == _longValue;

                case TypeInfo:
                case StringConstant:
                case MethodType:
                    return key._refIndex1 == _refIndex1;

                case NameAndTypeDescriptor:
                case FieldReference:
                case MethodReference:
                case InterfaceMethodReference:
                case MethodHandle:
                case InvokeDynamicInfo:
                    return key._refIndex1 == _refIndex1 &&
                           key._refIndex2 == _refIndex2;
            }

            return false;
        }
    }

    // </editor-fold>
}
