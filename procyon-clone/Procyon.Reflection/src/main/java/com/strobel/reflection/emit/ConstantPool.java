/*
 * ConstantPool.java
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

import com.strobel.core.HashUtilities;
import com.strobel.core.StringUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.reflection.FieldInfo;
import com.strobel.reflection.MethodBase;
import com.strobel.reflection.MethodInfo;
import com.strobel.reflection.Type;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * @author strobelm
 */
@SuppressWarnings({"PublicField", "ProtectedField"})
final class ConstantPool {
    private final static Writer WRITER = new Writer();

    private final ArrayList<Entry> _pool = new ArrayList<>();
    private final HashMap<Key, Entry> _entryMap = new HashMap<>();
    private final Key _lookupKey = new Key();
    private final Key _newKey = new Key();

    private int _size;

    final HashSet<Type<?>> referencedInnerTypes = new LinkedHashSet<>();

    public void write(final CodeStream stream) {
        stream.putShort(_size + 1);

        for (final Entry entry : _pool) {
            if (entry != null) {
                entry.accept(WRITER, stream);
            }
        }
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

    public Utf8StringConstant getUtf8StringConstant(final String value) {
        _lookupKey.set(value);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            entry = new Utf8StringConstant(this, value);
        }
        _lookupKey.clear();
        return (Utf8StringConstant)entry;
    }

    public StringConstant getStringConstant(final String value) {
        final Utf8StringConstant utf8Constant = getUtf8StringConstant(value);
        _lookupKey.set(Tag.StringConstant, utf8Constant.index);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            entry = new StringConstant(this, utf8Constant.index);
        }
        _lookupKey.clear();
        return (StringConstant)entry;
    }

    public IntegerConstant getIntegerConstant(final int value) {
        _lookupKey.set(value);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            entry = new IntegerConstant(this, value);
        }
        _lookupKey.clear();
        return (IntegerConstant)entry;
    }

    public FloatConstant getFloatConstant(final float value) {
        _lookupKey.set(value);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            entry = new FloatConstant(this, value);
        }
        _lookupKey.clear();
        return (FloatConstant)entry;
    }

    public LongConstant getLongConstant(final long value) {
        _lookupKey.set(value);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            entry = new LongConstant(this, value);
        }
        _lookupKey.clear();
        return (LongConstant)entry;
    }

    public DoubleConstant getDoubleConstant(final double value) {
        _lookupKey.set(value);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            entry = new DoubleConstant(this, value);
        }
        _lookupKey.clear();
        return (DoubleConstant)entry;
    }

    public TypeInfo getTypeInfo(final Type<?> type) {
        final Utf8StringConstant name = getUtf8StringConstant(type.getInternalName());

        if (type.isNested()) {
            referencedInnerTypes.add(type);

            final Type declaringType = type.getDeclaringType();
            final MethodBase declaringMethod = type.getDeclaringMethod();
            final String shortName = type.getShortName();

            if (declaringType != null) {
                getTypeInfo(declaringType);
            }

            if (declaringMethod != null) {
                getMethodReference(declaringMethod);
            }

            if (!StringUtilities.isNullOrWhitespace(shortName)) {
                getUtf8StringConstant(shortName);
            }
        }

        _lookupKey.set(Tag.TypeInfo, name.index);

        Entry entry = _entryMap.get(_lookupKey);

        if (entry == null) {
            entry = new TypeInfo(this, name.index);
        }

        _lookupKey.clear();

        return (TypeInfo)entry;
    }

    public FieldReference getFieldReference(final FieldInfo field) {
        final TypeInfo typeInfo = getTypeInfo(field.getDeclaringType());
        final NameAndTypeDescriptor nameAndDescriptor = getNameAndTypeDescriptor(
            field.getName(),
            field.getErasedSignature()
        );
        _lookupKey.set(Tag.FieldReference, typeInfo.index, nameAndDescriptor.index);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            entry = new FieldReference(this, typeInfo.index, nameAndDescriptor.index);
        }
        _lookupKey.clear();
        return (FieldReference)entry;
    }

    public MethodReference getMethodReference(final MethodBase method) {
        final TypeInfo typeInfo = getTypeInfo(method.getDeclaringType());
        final NameAndTypeDescriptor nameAndDescriptor = getNameAndTypeDescriptor(
            method.getName(),
            method.getErasedSignature()
        );
        _lookupKey.set(Tag.MethodReference, typeInfo.index, nameAndDescriptor.index);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            entry = new MethodReference(this, typeInfo.index, nameAndDescriptor.index);
        }
        _lookupKey.clear();
        return (MethodReference)entry;
    }

    public InterfaceMethodReference getInterfaceMethodReference(final MethodInfo method) {
        final TypeInfo typeInfo = getTypeInfo(method.getDeclaringType());
        final NameAndTypeDescriptor nameAndDescriptor = getNameAndTypeDescriptor(
            method.getName(),
            method.getErasedSignature()
        );
        _lookupKey.set(Tag.InterfaceMethodReference, typeInfo.index, nameAndDescriptor.index);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            entry = new InterfaceMethodReference(this, typeInfo.index, nameAndDescriptor.index);
        }
        _lookupKey.clear();
        return (InterfaceMethodReference)entry;
    }

    NameAndTypeDescriptor getNameAndTypeDescriptor(final String name, final String typeDescriptor) {
        final Utf8StringConstant utf8Name = getUtf8StringConstant(name);
        final Utf8StringConstant utf8Descriptor = getUtf8StringConstant(typeDescriptor);
        _lookupKey.set(Tag.NameAndTypeDescriptor, utf8Name.index, utf8Descriptor.index);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            entry = new NameAndTypeDescriptor(this, utf8Name.index, utf8Descriptor.index);
        }
        _lookupKey.clear();
        return (NameAndTypeDescriptor)entry;
    }

    MethodHandle getMethodHandle(final ReferenceKind referenceKind, final int referenceIndex) {
        _lookupKey.set(Tag.MethodHandle, referenceIndex, referenceKind);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            entry = new MethodHandle(this, referenceKind, referenceIndex);
        }
        _lookupKey.clear();
        return (MethodHandle)entry;
    }

    MethodType getMethodType(final int descriptorIndex) {
        _lookupKey.set(Tag.MethodType, descriptorIndex);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            entry = new MethodType(this, descriptorIndex);
        }
        _lookupKey.clear();
        return (MethodType)entry;
    }

    InvokeDynamicInfo getInvokeDynamicInfo(
        final int bootstrapMethodAttributeIndex,
        final int nameAndTypeDescriptorIndex) {
        _lookupKey.set(Tag.InvokeDynamicInfo, bootstrapMethodAttributeIndex, nameAndTypeDescriptorIndex);
        Entry entry = _entryMap.get(_lookupKey);
        if (entry == null) {
            entry = new InvokeDynamicInfo(this, bootstrapMethodAttributeIndex, nameAndTypeDescriptorIndex);
        }
        _lookupKey.clear();
        return (InvokeDynamicInfo)entry;
    }

    public static abstract class Entry {
        protected final int index;
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

        public abstract Tag getTag();

        /**
         * The number of slots in the constant pool used by this entry.
         * 2 for DoubleConstant and LongConstant; 1 for everything else.
         */
        public int size() {
            return 1;
        }

        public abstract int byteLength();

        public abstract <R, D> R accept(Visitor<R, D> visitor, D data);
    }

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
    }

    public interface Visitor<R, P> {
        R visitTypeInfo(TypeInfo info, P p);
        R visitDoubleConstant(DoubleConstant info, P p);
        R visitFieldReference(FieldReference info, P p);
        R visitFloatConstant(FloatConstant info, P p);
        R visitIntegerConstant(IntegerConstant info, P p);
        R visitInterfaceMethodReference(InterfaceMethodReference info, P p);
        R visitInvokeDynamicInfo(InvokeDynamicInfo info, P p);
        R visitLongConstant(LongConstant info, P p);
        R visitNameAndTypeDescriptor(NameAndTypeDescriptor info, P p);
        R visitMethodReference(MethodReference info, P p);
        R visitMethodHandle(MethodHandle info, P p);
        R visitMethodType(MethodType info, P p);
        R visitStringConstant(StringConstant info, P p);
        R visitUtf8StringConstant(Utf8StringConstant info, P p);
    }

    private final static class Writer implements Visitor<Void, CodeStream> {

        @Override
        public Void visitTypeInfo(final TypeInfo info, final CodeStream codeStream) {
            codeStream.putByte(info.getTag().value);
            codeStream.putShort(info.nameIndex);
            return null;
        }

        @Override
        public Void visitDoubleConstant(final DoubleConstant info, final CodeStream codeStream) {
            codeStream.putByte(info.getTag().value);
            codeStream.putDouble(info.value);
            return null;
        }

        @Override
        public Void visitFieldReference(final FieldReference info, final CodeStream codeStream) {
            codeStream.putByte(info.getTag().value);
            codeStream.putShort(info.typeInfoIndex);
            codeStream.putShort(info.nameAndTypeDescriptorIndex);
            return null;
        }

        @Override
        public Void visitFloatConstant(final FloatConstant info, final CodeStream codeStream) {
            codeStream.putByte(info.getTag().value);
            codeStream.putFloat(info.value);
            return null;
        }

        @Override
        public Void visitIntegerConstant(final IntegerConstant info, final CodeStream codeStream) {
            codeStream.putByte(info.getTag().value);
            codeStream.putInt(info.value);
            return null;
        }

        @Override
        public Void visitInterfaceMethodReference(final InterfaceMethodReference info, final CodeStream codeStream) {
            codeStream.putByte(info.getTag().value);
            codeStream.putShort(info.typeInfoIndex);
            codeStream.putShort(info.nameAndTypeDescriptorIndex);
            return null;
        }

        @Override
        public Void visitInvokeDynamicInfo(final InvokeDynamicInfo info, final CodeStream codeStream) {
            codeStream.putByte(info.getTag().value);
            codeStream.putShort(info.bootstrapMethodAttributeIndex);
            codeStream.putShort(info.nameAndTypeDescriptorIndex);
            return null;
        }

        @Override
        public Void visitLongConstant(final LongConstant info, final CodeStream codeStream) {
            codeStream.putByte(info.getTag().value);
            codeStream.putLong(info.value);
            return null;
        }

        @Override
        public Void visitNameAndTypeDescriptor(final NameAndTypeDescriptor info, final CodeStream codeStream) {
            codeStream.putByte(info.getTag().value);
            codeStream.putShort(info.nameIndex);
            codeStream.putShort(info.typeDescriptorIndex);
            return null;
        }

        @Override
        public Void visitMethodReference(final MethodReference info, final CodeStream codeStream) {
            codeStream.putByte(info.getTag().value);
            codeStream.putShort(info.typeInfoIndex);
            codeStream.putShort(info.nameAndTypeDescriptorIndex);
            return null;
        }

        @Override
        public Void visitMethodHandle(final MethodHandle info, final CodeStream codeStream) {
            codeStream.putByte(info.getTag().value);
            codeStream.putShort(info.referenceKind.ordinal());
            codeStream.putShort(info.referenceIndex);
            return null;
        }

        @Override
        public Void visitMethodType(final MethodType info, final CodeStream codeStream) {
            codeStream.putByte(info.getTag().value);
            codeStream.putShort(info.descriptorIndex);
            return null;
        }

        @Override
        public Void visitStringConstant(final StringConstant info, final CodeStream codeStream) {
            codeStream.putByte(info.getTag().value);
            codeStream.putShort(info.stringIndex);
            return null;
        }

        @Override
        public Void visitUtf8StringConstant(final Utf8StringConstant info, final CodeStream codeStream) {
            codeStream.putByte(info.getTag().value);
            codeStream.putUtf8(info.value);
            return null;
        }
    }

    public static final class TypeInfo extends Entry {
        public final int nameIndex;

        public TypeInfo(final ConstantPool owner, final int nameIndex) {
            super(owner);
            this.nameIndex = nameIndex;
            owner._newKey.set(getTag(), nameIndex);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
        }

        public String getName() {
            return ((Utf8StringConstant)owner.get(nameIndex, Tag.Utf8StringConstant)).value;
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
        public <R, D> R accept(final Visitor<R, D> visitor, final D data) {
            return visitor.visitTypeInfo(this, data);
        }

        @Override
        public String toString() {
            return "TypeIndex[index: " + index + ", nameIndex: " + nameIndex + "]";
        }
    }

    public static final class MethodType extends Entry {
        public final int descriptorIndex;

        public MethodType(final ConstantPool owner, final int descriptorIndex) {
            super(owner);
            this.descriptorIndex = descriptorIndex;
            owner._newKey.set(getTag(), descriptorIndex);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
        }

        public String getType() {
            return ((Utf8StringConstant)owner.get(descriptorIndex, Tag.Utf8StringConstant)).value;
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
        public <R, D> R accept(final Visitor<R, D> visitor, final D data) {
            return visitor.visitMethodType(this, data);
        }

        @Override
        public String toString() {
            return "MethodType[index: " + index + ", descriptorIndex: " + descriptorIndex + "]";
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

        public TypeInfo getClassInfo() {
            return (TypeInfo)owner.get(typeInfoIndex, Tag.TypeInfo);
        }

        public String getClassName() {
            return getClassInfo().getName();
        }

        public NameAndTypeDescriptor getNameAndTypeInfo() {
            return (NameAndTypeDescriptor)owner.get(nameAndTypeDescriptorIndex, Tag.NameAndTypeDescriptor);
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

    public static final class FieldReference extends ReferenceEntry {
        public FieldReference(final ConstantPool owner, final int typeIndex, final int nameAndTypeDescriptorIndex) {
            super(owner, Tag.FieldReference, typeIndex, nameAndTypeDescriptorIndex);
        }

        @Override
        public <R, D> R accept(final Visitor<R, D> visitor, final D data) {
            return visitor.visitFieldReference(this, data);
        }
    }

    public static final class MethodReference extends ReferenceEntry {
        public MethodReference(final ConstantPool owner, final int typeIndex, final int nameAndTypeDescriptorIndex) {
            super(owner, Tag.MethodReference, typeIndex, nameAndTypeDescriptorIndex);
        }

        @Override
        public <R, D> R accept(final Visitor<R, D> visitor, final D data) {
            return visitor.visitMethodReference(this, data);
        }
    }

    public static final class InterfaceMethodReference extends ReferenceEntry {
        public InterfaceMethodReference(final ConstantPool owner, final int typeIndex, final int nameAndTypeDescriptorIndex) {
            super(owner, Tag.InterfaceMethodReference, typeIndex, nameAndTypeDescriptorIndex);
        }

        @Override
        public <R, D> R accept(final Visitor<R, D> visitor, final D data) {
            return visitor.visitInterfaceMethodReference(this, data);
        }
    }

    public static class MethodHandle extends Entry {
        public final ReferenceKind referenceKind;
        public final int referenceIndex;

        public MethodHandle(final ConstantPool owner, final ReferenceKind referenceKind, final int referenceIndex) {
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

            return (ReferenceEntry)owner.get(referenceIndex, expected);
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
        public <R, D> R accept(final Visitor<R, D> visitor, final D data) {
            return visitor.visitMethodHandle(this, data);
        }
    }

    public static class NameAndTypeDescriptor extends Entry {
        public final int nameIndex;
        public final int typeDescriptorIndex;

        public NameAndTypeDescriptor(final ConstantPool owner, final int nameIndex, final int typeDescriptorIndex) {
            super(owner);
            this.nameIndex = nameIndex;
            this.typeDescriptorIndex = typeDescriptorIndex;
            owner._newKey.set(getTag(), nameIndex, typeDescriptorIndex);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
        }

        public Tag getTag() {
            return Tag.NameAndTypeDescriptor;
        }

        public int byteLength() {
            return 5;
        }

        public String getName() {
            return ((Utf8StringConstant)owner.get(nameIndex, Tag.Utf8StringConstant)).value;
        }

        public String getType() {
            return ((Utf8StringConstant)owner.get(typeDescriptorIndex, Tag.Utf8StringConstant)).value;
        }

        public <R, D> R accept(final Visitor<R, D> visitor, final D data) {
            return visitor.visitNameAndTypeDescriptor(this, data);
        }

        @Override
        public String toString() {
            return "NameAndTypeDescriptor[index: " + index + ", descriptorIndex: " + nameIndex + ", typeDescriptorIndex: " + typeDescriptorIndex + "]";
        }
    }

    public static class InvokeDynamicInfo extends Entry {
        public final int bootstrapMethodAttributeIndex;
        public final int nameAndTypeDescriptorIndex;

        public InvokeDynamicInfo(
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

        public Tag getTag() {
            return Tag.InvokeDynamicInfo;
        }

        public int byteLength() {
            return 5;
        }

        public NameAndTypeDescriptor getNameAndTypeDescriptor() {
            return (NameAndTypeDescriptor)owner.get(nameAndTypeDescriptorIndex, Tag.NameAndTypeDescriptor);
        }

        public <R, D> R accept(final Visitor<R, D> visitor, final D data) {
            return visitor.visitInvokeDynamicInfo(this, data);
        }

        @Override
        public String toString() {
            return "InvokeDynamicInfo[bootstrapMethodAttributeIndex: " +
                   bootstrapMethodAttributeIndex +
                   ", nameAndTypeDescriptorIndex: " +
                   nameAndTypeDescriptorIndex +
                   "]";
        }
    }

    public static final class DoubleConstant extends Entry {
        public final double value;

        public DoubleConstant(final ConstantPool owner, final double value) {
            super(owner);
            this.value = value;
            owner._newKey.set(value);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
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
        public <R, D> R accept(final Visitor<R, D> visitor, final D data) {
            return visitor.visitDoubleConstant(this, data);
        }

        @Override
        public String toString() {
            return "DoubleConstant[index: " + index + ", value: " + value + "]";
        }
    }

    public static final class FloatConstant extends Entry {
        public final float value;

        public FloatConstant(final ConstantPool owner, final float value) {
            super(owner);
            this.value = value;
            owner._newKey.set(value);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
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
        public <R, D> R accept(final Visitor<R, D> visitor, final D data) {
            return visitor.visitFloatConstant(this, data);
        }

        @Override
        public String toString() {
            return "FloatConstant[index: " + index + ", value: " + value + "]";
        }
    }

    public static final class IntegerConstant extends Entry {
        public final int value;

        public IntegerConstant(final ConstantPool owner, final int value) {
            super(owner);
            this.value = value;
            owner._newKey.set(value);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
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
        public <R, D> R accept(final Visitor<R, D> visitor, final D data) {
            return visitor.visitIntegerConstant(this, data);
        }

        @Override
        public String toString() {
            return "IntegerConstant[index: " + index + ", value: " + value + "]";
        }
    }

    public static final class LongConstant extends Entry {
        public final long value;

        public LongConstant(final ConstantPool owner, final long value) {
            super(owner);
            this.value = value;
            owner._newKey.set(value);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
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
        public <R, D> R accept(final Visitor<R, D> visitor, final D data) {
            return visitor.visitLongConstant(this, data);
        }

        @Override
        public String toString() {
            return "LongConstant[index: " + index + ", value: " + value + "]";
        }
    }

    public static final class StringConstant extends Entry {
        public final int stringIndex;

        public StringConstant(final ConstantPool owner, final int stringIndex) {
            super(owner);
            this.stringIndex = stringIndex;
            owner._newKey.set(getTag(), stringIndex);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
        }

        public String getValue() {
            return ((Utf8StringConstant)owner.get(stringIndex)).value;
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
        public <R, D> R accept(final Visitor<R, D> visitor, final D data) {
            return visitor.visitStringConstant(this, data);
        }

        @Override
        public String toString() {
            return "StringConstant[index: " + index + ", stringIndex: " + stringIndex + "]";
        }
    }

    public static final class Utf8StringConstant extends Entry {
        public final String value;

        public Utf8StringConstant(final ConstantPool owner, final String value) {
            super(owner);
            this.value = value;
            owner._newKey.set(getTag(), value);
            owner._entryMap.put(owner._newKey.clone(), this);
            owner._newKey.clear();
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
        public <R, D> R accept(final Visitor<R, D> visitor, final D data) {
            return visitor.visitUtf8StringConstant(this, data);
        }

        @Override
        public String toString() {
            return "Utf8StringConstant[index: " + index + ", value: " + value + "]";
        }
    }

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
            _hashCode = 0x7FFFFFFF & (_tag.value + (int)longValue);
        }

        public void set(final float floatValue) {
            _tag = Tag.FloatConstant;
            _intValue = Float.floatToRawIntBits(floatValue);
            _hashCode = 0x7FFFFFFF & (_tag.value + _intValue);
        }

        public void set(final double doubleValue) {
            _tag = Tag.DoubleConstant;
            _longValue = Double.doubleToRawLongBits(doubleValue);
            _hashCode = 0x7FFFFFFF & (_tag.value + (int)_longValue);
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

            final Key key = (Key)obj;
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
}
