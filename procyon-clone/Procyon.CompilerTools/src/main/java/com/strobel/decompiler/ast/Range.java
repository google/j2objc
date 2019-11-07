/*
 * Range.java
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

import com.strobel.core.VerifyArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Range implements Comparable<Range> {
    private int _start;
    private int _end;

    public Range() {
    }

    public Range(final int start, final int end) {
        _start = start;
        _end = end;
    }

    public final int getStart() {
        return _start;
    }

    public final void setStart(final int start) {
        _start = start;
    }

    public final int getEnd() {
        return _end;
    }

    public final void setEnd(final int end) {
        _end = end;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof Range) {
            final Range range = (Range) o;

            return range._end == _end &&
                   range._start == _start;
        }

        return false;
    }

    public final boolean contains(final int location) {
        return location >= _start &&
               location <= _end;
    }
    public final boolean contains(final int start, final int end) {
        return start >= _start &&
               end <= _end;
    }

    public final boolean contains(final Range range) {
        return range != null &&
               range._start >= _start &&
               range._end <= _end;
    }

    public final boolean intersects(final Range range) {
        return range != null &&
               range._start <= _end &&
               range._end >= _start;
    }

    @Override
    public final int hashCode() {
        int result = _start;
        result = 31 * result + _end;
        return result;
    }

    @Override
    public final int compareTo(final Range o) {
        if (o == null) {
            return 1;
        }

        final int compareResult = Integer.compare(_start, o._start);

        return compareResult != 0 ? compareResult
                                  : Integer.compare(_end, o._end);
    }

    @Override
    public final String toString() {
        return String.format("Range(%d, %d)", _start, _end);
    }

    public static List<Range> orderAndJoint(final Iterable<Range> input) {
        VerifyArgument.notNull(input, "input");

        final ArrayList<Range> ranges = new ArrayList<>();

        for (final Range range : input) {
            if (range != null) {
                ranges.add(range);
            }
        }

        Collections.sort(ranges);

        for (int i = 0; i < ranges.size() - 1; ) {
            final Range current = ranges.get(i);
            final Range next = ranges.get(i + 1);

            //
            // Merge consecutive ranges if they intersect.
            //
            if (current.getStart() <= next.getStart() &&
                next.getStart() <= current.getEnd()) {

                current.setEnd(Math.max(current.getEnd(), next.getEnd()));
                ranges.remove(i + 1);
            }
            else {
                ++i;
            }
        }

        return ranges;
    }

    public static List<Range> invert(final Iterable<Range> input, final int codeSize) {
        VerifyArgument.notNull(input, "input");
        VerifyArgument.isPositive(codeSize, "codeSize");

        final List<Range> ordered = orderAndJoint(input);

        if (ordered.isEmpty()) {
            return Collections.singletonList(new Range(0, codeSize));
        }

        final List<Range> inverted = new ArrayList<>();

        //
        // Gap before the first element...
        //
        if (ordered.get(0).getStart() != 0) {
            inverted.add(new Range(0, ordered.get(0).getStart()));
        }

        //
        // Gap between elements...
        //
        for (int i = 0; i < ordered.size() - 1; i++) {
            inverted.add(
                new Range(
                    ordered.get(i).getEnd(),
                    ordered.get(i + 1).getStart()
                )
            );
        }

        assert ordered.get(ordered.size() - 1).getEnd() <= codeSize;

        //
        // Gap after the last element...
        //
        if (ordered.get(ordered.size() - 1).getEnd() != codeSize) {
            inverted.add(
                new Range(
                    ordered.get(ordered.size() - 1).getEnd(),
                    codeSize
                )
            );
        }

        return inverted;
    }
}
