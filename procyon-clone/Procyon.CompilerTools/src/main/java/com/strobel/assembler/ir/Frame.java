/*
 * Frame.java
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

import com.strobel.assembler.metadata.MetadataHelper;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.ArrayUtilities;
import com.strobel.core.CollectionUtilities;
import com.strobel.core.Comparer;
import com.strobel.core.HashUtilities;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilerHelpers;
import com.strobel.decompiler.PlainTextOutput;
import com.strobel.util.ContractUtils;
import com.strobel.util.EmptyArrayCache;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * User: Mike Strobel Date: 1/6/13 Time: 4:09 PM
 */
public final class Frame {
    public final static FrameValue[] EMPTY_VALUES = EmptyArrayCache.fromElementType(FrameValue.class);

    public final static Frame NEW_EMPTY = new Frame(
        FrameType.New,
        EmptyArrayCache.fromElementType(FrameValue.class),
        EmptyArrayCache.fromElementType(FrameValue.class)
    );

    public final static Frame SAME = new Frame(
        FrameType.Same,
        EmptyArrayCache.fromElementType(FrameValue.class),
        EmptyArrayCache.fromElementType(FrameValue.class)
    );

    private final FrameType _frameType;
    private final List<FrameValue> _localValues;
    private final List<FrameValue> _stackValues;

    public Frame(final FrameType frameType, final FrameValue[] localValues, final FrameValue[] stackValues) {
        _frameType = VerifyArgument.notNull(frameType, "frameType");
        _localValues = ArrayUtilities.asUnmodifiableList(VerifyArgument.notNull(localValues, "localValues").clone());
        _stackValues = ArrayUtilities.asUnmodifiableList(VerifyArgument.notNull(stackValues, "stackValues").clone());
    }

    private Frame(final FrameType frameType, final List<FrameValue> localValues, final List<FrameValue> stackValues) {
        _frameType = frameType;
        _localValues = localValues;
        _stackValues = stackValues;
    }

    public final FrameType getFrameType() {
        return _frameType;
    }

    public final List<FrameValue> getLocalValues() {
        return _localValues;
    }

    public final List<FrameValue> getStackValues() {
        return _stackValues;
    }

    public final Frame withEmptyStack() {
        if (_frameType != FrameType.New) {
            throw new IllegalStateException("Can only call withEmptyStack() on New frames.");
        }

        return new Frame(_frameType, _localValues, Collections.<FrameValue>emptyList());
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Frame)) {
            return false;
        }

        final Frame frame = (Frame) o;

        return frame._frameType == _frameType &&
               CollectionUtilities.sequenceDeepEquals(frame._localValues, _localValues) &&
               CollectionUtilities.sequenceDeepEquals(frame._stackValues, _stackValues);
    }

    @Override
    public final int hashCode() {
        int result = _frameType.hashCode();

        for (int i = 0; i < _localValues.size(); i++) {
            result = HashUtilities.combineHashCodes(result, _localValues.get(i));
        }

        for (int i = 0; i < _stackValues.size(); i++) {
            result = HashUtilities.combineHashCodes(result, _stackValues.get(i));
        }

        return result;
    }

    @Override
    public final String toString() {
        final PlainTextOutput writer = new PlainTextOutput();
        DecompilerHelpers.writeFrame(writer, this);
        return writer.toString();
    }

    @SuppressWarnings("ConstantConditions")
    public static Frame computeDelta(final Frame previous, final Frame current) {
        VerifyArgument.notNull(previous, "previous");
        VerifyArgument.notNull(current, "current");

        final List<FrameValue> previousLocals = previous._localValues;
        final List<FrameValue> currentLocals = current._localValues;
        final List<FrameValue> currentStack = current._stackValues;

        final int previousLocalCount = previousLocals.size();
        final int currentLocalCount = currentLocals.size();
        final int currentStackSize = currentStack.size();

        int localCount = previousLocalCount;
        FrameType type = FrameType.Full;

        if (currentStackSize == 0) {
            switch (currentLocalCount - previousLocalCount) {
                case -3:
                case -2:
                case -1:
                    type = FrameType.Chop;
                    localCount = currentLocalCount;
                    break;

                case 0:
                    type = FrameType.Same;
                    break;

                case 1:
                case 2:
                case 3:
                    type = FrameType.Append;
                    break;
            }
        }
        else if (currentLocalCount == localCount && currentStackSize == 1) {
            type = FrameType.Same1;
        }

        if (type != FrameType.Full) {
            for (int i = 0; i < localCount; i++) {
                if (!currentLocals.get(i).equals(previousLocals.get(i))) {
                    type = FrameType.Full;
                    break;
                }
            }
        }

        switch (type) {
            case Append: {
                return new Frame(
                    type,
                    currentLocals.subList(previousLocalCount, currentLocalCount)
                                 .toArray(new FrameValue[currentLocalCount - previousLocalCount]),
                    EmptyArrayCache.fromElementType(FrameValue.class)
                );
            }

            case Chop: {
                return new Frame(
                    type,
                    previousLocals.subList(currentLocalCount, previousLocalCount)
                                  .toArray(new FrameValue[previousLocalCount - currentLocalCount]),
                    EmptyArrayCache.fromElementType(FrameValue.class)
                );
            }

            case Full: {
                return new Frame(
                    type,
                    currentLocals,
                    currentStack
                );
            }

            case Same: {
                return SAME;
            }

            case Same1: {
                return new Frame(
                    type,
                    EmptyArrayCache.fromElementType(FrameValue.class),
                    new FrameValue[] { currentStack.get(currentStackSize - 1) }
                );
            }

            default: {
                throw ContractUtils.unreachable();
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static Frame merge(final Frame input, final Frame output, final Frame next, final Map<Instruction, TypeReference> initializations) {
        VerifyArgument.notNull(input, "input");
        VerifyArgument.notNull(output, "output");
        VerifyArgument.notNull(next, "next");

        final List<FrameValue> inputLocals = input._localValues;
        final List<FrameValue> outputLocals = output._localValues;

        final int inputLocalCount = inputLocals.size();
        final int outputLocalCount = outputLocals.size();
        final int nextLocalCount = next._localValues.size();
        final int tempLocalCount = Math.max(nextLocalCount, inputLocalCount);

        final FrameValue[] nextLocals = next._localValues.toArray(new FrameValue[tempLocalCount]);

        FrameValue t;
        boolean changed = false;

        for (int i = 0; i < inputLocalCount; i++) {
            if (i < outputLocalCount) {
                t = outputLocals.get(i);
            }
            else {
                t = inputLocals.get(i);
            }

            if (initializations != null) {
                t = initialize(initializations, t);
            }

            changed |= merge(t, nextLocals, i);
        }

        final List<FrameValue> inputStack = input._stackValues;
        final List<FrameValue> outputStack = output._stackValues;

        final int inputStackSize = inputStack.size();
        final int outputStackSize = outputStack.size();
        final int nextStackSize = next._stackValues.size();

        final FrameValue[] nextStack = next._stackValues.toArray(new FrameValue[nextStackSize]);

        for (int i = 0, max = Math.min(nextStackSize, inputStackSize); i < max; i++) {
            t = inputStack.get(i);

            if (initializations != null) {
                t = initialize(initializations, t);
            }

            changed |= merge(t, nextStack, i);
        }

        for (int i = inputStackSize, max = Math.min(nextStackSize, outputStackSize); i < max; i++) {
            t = outputStack.get(i);

            if (initializations != null) {
                t = initialize(initializations, t);
            }

            changed |= merge(t, nextStack, i);
        }

        if (!changed) {
            return next;
        }

        int newLocalCount = nextLocalCount;
/*
        int newStackSize;

        //noinspection StatementWithEmptyBody
        for (newLocalCount = nextLocalCount;
             newLocalCount < inputLocalCount && nextLocals[newLocalCount] != null;
             newLocalCount++) {
        }
*/

/*
        //noinspection StatementWithEmptyBody
        for (newStackSize = nextStackSize;
             newStackSize < inputStackSize && nextStack[newStackSize] != null;
             newStackSize++) {
        }
*/

        return new Frame(
            FrameType.New,
            nextLocals.length == newLocalCount ? nextLocals
                                               : Arrays.copyOf(nextLocals, newLocalCount),
            nextStack
        );
    }

    private static FrameValue initialize(final Map<Instruction, TypeReference> initializations, FrameValue t) {
        if (t == null) {
            return t;
        }

        final Object parameter = t.getParameter();

        if (parameter instanceof Instruction) {
            final TypeReference initializedType = initializations.get(parameter);

            if (initializedType != null) {
                t = FrameValue.makeReference(initializedType);
            }
        }

        return t;
    }

    private static boolean merge(final FrameValue t, final FrameValue[] values, final int index) {
        final FrameValue u = values[index];

        if (Comparer.equals(t, u)) {
            return false;
        }

        if (t == FrameValue.EMPTY) {
            return false;
        }

        if (t == FrameValue.NULL) {
            if (u == FrameValue.NULL) {
                return false;
            }
        }

        if (u == FrameValue.EMPTY) {
            values[index] = t;
            return true;
        }

        final FrameValueType tType = t.getType();
        final FrameValueType uType = u.getType();

        final FrameValue v;

        if (uType == FrameValueType.Reference) {
            if (t == FrameValue.NULL) {
                return false;
            }

            if (tType == FrameValueType.Reference) {
                v = FrameValue.makeReference(
                    MetadataHelper.findCommonSuperType(
                        (TypeReference) t.getParameter(),
                        (TypeReference) u.getParameter()
                    )
                );
            }
            else {
                v = FrameValue.TOP;
            }
        }
        else if (u == FrameValue.NULL && tType == FrameValueType.Reference) {
            v = t;
        }
        else {
            v = FrameValue.TOP;
        }

        if (!u.equals(v)) {
            values[index] = v;
            return true;
        }

        return false;
    }
}
