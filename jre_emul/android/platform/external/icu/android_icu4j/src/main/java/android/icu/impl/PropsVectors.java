/* GENERATED SOURCE. DO NOT MODIFY. */
// © 2016 and later: Unicode, Inc. and others.
// License & terms of use: http://www.unicode.org/copyright.html#License
/*
 ******************************************************************************
 * Copyright (C) 1996-2011, International Business Machines Corporation and   *
 * others. All Rights Reserved.                                               *
 ******************************************************************************
 */

/**
 * Store bits (Unicode character properties) in bit set vectors.
 *
 * This is a port of the C++ class UPropsVectors from ICU4C
 *
 * @author Shaopeng Jia
 * @hide draft / provisional / internal are hidden on Android
 */

package android.icu.impl;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Unicode Properties Vectors associated with code point ranges.
 *
 * Rows of primitive integers in a contiguous array store the range limits and
 * the properties vectors.
 *
 * In each row, row[0] contains the start code point and row[1] contains the
 * limit code point, which is the start of the next range.
 *
 * Initially, there is only one range [0..0x110000] with values 0.
 *
 * It would be possible to store only one range boundary per row, but
 * self-contained rows allow to later sort them by contents.
 * @hide Only a subset of ICU is exposed in Android
 */
public class PropsVectors {
    private int v[];
    private int columns; // number of columns, plus two for start
    // and limit values
    private int maxRows;
    private int rows;
    private int prevRow; // search optimization: remember last row seen
    private boolean isCompacted;

    // internal function to compare elements in v and target. Return true iff
    // elements in v starting from index1 to index1 + length - 1
    // are exactly the same as elements in target
    // starting from index2 to index2 + length - 1
    private boolean areElementsSame(int index1, int[] target, int index2,
            int length) {
        for (int i = 0; i < length; ++i) {
            if (v[index1 + i] != target[index2 + i]) {
                return false;
            }
        }
        return true;
    }

    // internal function which given rangeStart, returns
    // index where v[index]<=rangeStart<v[index+1].
    // The returned index is a multiple of columns, and therefore
    // points to the start of a row.
    private int findRow(int rangeStart) {
        int index = 0;

        // check the vicinity of the last-seen row (start
        // searching with an unrolled loop)

        index = prevRow * columns;
        if (rangeStart >= v[index]) {
            if (rangeStart < v[index + 1]) {
                // same row as last seen
                return index;
            } else {
                index += columns;
                if (rangeStart < v[index + 1]) {
                    ++prevRow;
                    return index;
                } else {
                    index += columns;
                    if (rangeStart < v[index + 1]) {
                        prevRow += 2;
                        return index;
                    } else if ((rangeStart - v[index + 1]) < 10) {
                        // we are close, continue looping
                        prevRow += 2;
                        do {
                            ++prevRow;
                            index += columns;
                        } while (rangeStart >= v[index + 1]);
                        return index;
                    }
                }
            }
        } else if (rangeStart < v[1]) {
            // the very first row
            prevRow = 0;
            return 0;
        }

        // do a binary search for the start of the range
        int start = 0;
        int mid = 0;
        int limit = rows;
        while (start < limit - 1) {
            mid = (start + limit) / 2;
            index = columns * mid;
            if (rangeStart < v[index]) {
                limit = mid;
            } else if (rangeStart < v[index + 1]) {
                prevRow = mid;
                return index;
            } else {
                start = mid;
            }
        }

        // must be found because all ranges together always cover
        // all of Unicode
        prevRow = start;
        index = start * columns;
        return index;
    }

    /*
     * Special pseudo code points for storing the initialValue and the
     * errorValue which are used to initialize a Trie or similar.
     */
    public final static int FIRST_SPECIAL_CP = 0x110000;
    public final static int INITIAL_VALUE_CP = 0x110000;
    public final static int ERROR_VALUE_CP = 0x110001;
    public final static int MAX_CP = 0x110001;

    public final static int INITIAL_ROWS = 1 << 12;
    public final static int MEDIUM_ROWS = 1 << 16;
    public final static int MAX_ROWS = MAX_CP + 1;

    /*
     * Constructor.
     * @param numOfColumns Number of value integers (32-bit int) per row.
     */
    public PropsVectors(int numOfColumns) {
        if (numOfColumns < 1) {
            throw new IllegalArgumentException("numOfColumns need to be no "
                    + "less than 1; but it is " + numOfColumns);
        }
        columns = numOfColumns + 2; // count range start and limit columns
        v = new int[INITIAL_ROWS * columns];
        maxRows = INITIAL_ROWS;
        rows = 2 + (MAX_CP - FIRST_SPECIAL_CP);
        prevRow = 0;
        isCompacted = false;
        v[0] = 0;
        v[1] = 0x110000;
        int index = columns;
        for (int cp = FIRST_SPECIAL_CP; cp <= MAX_CP; ++cp) {
            v[index] = cp;
            v[index + 1] = cp + 1;
            index += columns;
        }
    }

    /*
     * In rows for code points [start..end], select the column, reset the mask
     * bits and set the value bits (ANDed with the mask).
     *
     * @throws IllegalArgumentException
     *
     * @throws IllegalStateException
     *
     * @throws IndexOutOfBoundsException
     */
    public void setValue(int start, int end, int column, int value, int mask) {
        if (start < 0 || start > end || end > MAX_CP || column < 0
                || column >= (columns - 2)) {
            throw new IllegalArgumentException();
        }
        if (isCompacted) {
            throw new IllegalStateException("Shouldn't be called after"
                    + "compact()!");
        }

        int firstRow, lastRow;
        int limit = end + 1;
        boolean splitFirstRow, splitLastRow;
        // skip range start and limit columns
        column += 2;
        value &= mask;

        // find the rows whose ranges overlap with the input range
        // find the first and last row, always successful
        firstRow = findRow(start);
        lastRow = findRow(end);

        /*
         * Rows need to be split if they partially overlap with the input range
         * (only possible for the first and last rows) and if their value
         * differs from the input value.
         */
        splitFirstRow = (start != v[firstRow] && value != (v[firstRow + column] & mask));
        splitLastRow = (limit != v[lastRow + 1] && value != (v[lastRow + column] & mask));

        // split first/last rows if necessary
        if (splitFirstRow || splitLastRow) {
            int rowsToExpand = 0;
            if (splitFirstRow) {
                ++rowsToExpand;
            }
            if (splitLastRow) {
                ++rowsToExpand;
            }
            int newMaxRows = 0;
            if ((rows + rowsToExpand) > maxRows) {
                if (maxRows < MEDIUM_ROWS) {
                    newMaxRows = MEDIUM_ROWS;
                } else if (maxRows < MAX_ROWS) {
                    newMaxRows = MAX_ROWS;
                } else {
                    throw new IndexOutOfBoundsException(
                            "MAX_ROWS exceeded! Increase it to a higher value" +
                            "in the implementation");
                }
                int[] temp = new int[newMaxRows * columns];
                System.arraycopy(v, 0, temp, 0, rows * columns);
                v = temp;
                maxRows = newMaxRows;
            }

            // count the number of row cells to move after the last row,
            // and move them
            int count = (rows * columns) - (lastRow + columns);
            if (count > 0) {
                System.arraycopy(v, lastRow + columns, v, lastRow
                        + (1 + rowsToExpand) * columns, count);
            }
            rows += rowsToExpand;

            // split the first row, and move the firstRow pointer
            // to the second part
            if (splitFirstRow) {
                // copy all affected rows up one and move the lastRow pointer
                count = lastRow - firstRow + columns;
                System.arraycopy(v, firstRow, v, firstRow + columns, count);
                lastRow += columns;

                // split the range and move the firstRow pointer
                v[firstRow + 1] = v[firstRow + columns] = start;
                firstRow += columns;
            }

            // split the last row
            if (splitLastRow) {
                // copy the last row data
                System.arraycopy(v, lastRow, v, lastRow + columns, columns);

                // split the range and move the firstRow pointer
                v[lastRow + 1] = v[lastRow + columns] = limit;
            }
        }

        // set the "row last seen" to the last row for the range
        prevRow = lastRow / columns;

        // set the input value in all remaining rows
        firstRow += column;
        lastRow += column;
        mask = ~mask;
        for (;;) {
            v[firstRow] = (v[firstRow] & mask) | value;
            if (firstRow == lastRow) {
                break;
            }
            firstRow += columns;
        }
    }

    /*
     * Always returns 0 if called after compact().
     */
    public int getValue(int c, int column) {
        if (isCompacted || c < 0 || c > MAX_CP || column < 0
                || column >= (columns - 2)) {
            return 0;
        }
        int index = findRow(c);
        return v[index + 2 + column];
    }

    /*
     * Returns an array which contains value elements
     * in row rowIndex.
     *
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    public int[] getRow(int rowIndex) {
        if (isCompacted) {
            throw new IllegalStateException(
                    "Illegal Invocation of the method after compact()");
        }
        if (rowIndex < 0 || rowIndex > rows) {
            throw new IllegalArgumentException("rowIndex out of bound!");
        }
        int[] rowToReturn = new int[columns - 2];
        System.arraycopy(v, rowIndex * columns + 2, rowToReturn, 0,
                         columns - 2);
        return rowToReturn;
    }

    /*
     * Returns an int which is the start codepoint
     * in row rowIndex.
     *
     * @throws IllegalStateException
     *
     * @throws IllegalArgumentException
     */
    public int getRowStart(int rowIndex) {
        if (isCompacted) {
            throw new IllegalStateException(
                    "Illegal Invocation of the method after compact()");
        }
        if (rowIndex < 0 || rowIndex > rows) {
            throw new IllegalArgumentException("rowIndex out of bound!");
        }
        return v[rowIndex * columns];
    }

    /*
     * Returns an int which is the limit codepoint
     * minus 1 in row rowIndex.
     *
     * @throws IllegalStateException
     *
     * @throws IllegalArgumentException
     */
    public int getRowEnd(int rowIndex) {
        if (isCompacted) {
            throw new IllegalStateException(
                    "Illegal Invocation of the method after compact()");
        }
        if (rowIndex < 0 || rowIndex > rows) {
            throw new IllegalArgumentException("rowIndex out of bound!");
        }
        return v[rowIndex * columns + 1] - 1;
    }

    /*
     * Compact the vectors:
     * - modify the memory
     * - keep only unique vectors
     * - store them contiguously from the beginning of the memory
     * - for each (non-unique) row, call the respective function in
     *   CompactHandler
     *
     * The handler's rowIndex is the index of the row in the compacted
     * memory block. Therefore, it starts at 0 increases in increments of the
     * columns value.
     *
     * In a first phase, only special values are delivered (each exactly once).
     * Then CompactHandler::startRealValues() is called
     * where rowIndex is the length of the compacted array.
     * Then, in the second phase, the CompactHandler::setRowIndexForRange() is
     * called for each row of real values.
     */
    public void compact(CompactHandler compactor) {
        if (isCompacted) {
            return;
        }

        // Set the flag now: Sorting and compacting destroys the builder
        // data structure.
        isCompacted = true;
        int valueColumns = columns - 2; // not counting start & limit

        // sort the properties vectors to find unique vector values
        Integer[] indexArray = new Integer[rows];
        for (int i = 0; i < rows; ++i) {
            indexArray[i] = Integer.valueOf(columns * i);
        }

        Arrays.sort(indexArray, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                int indexOfRow1 = o1.intValue();
                int indexOfRow2 = o2.intValue();
                int count = columns; // includes start/limit columns

                // start comparing after start/limit
                // but wrap around to them
                int index = 2;
                do {
                    if (v[indexOfRow1 + index] != v[indexOfRow2 + index]) {
                        return v[indexOfRow1 + index] < v[indexOfRow2 + index] ? -1
                                : 1;
                    }
                    if (++index == columns) {
                        index = 0;
                    }
                } while (--count > 0);

                return 0;
            }
        });

        /*
         * Find and set the special values. This has to do almost the same work
         * as the compaction below, to find the indexes where the special-value
         * rows will move.
         */
        int count = -valueColumns;
        for (int i = 0; i < rows; ++i) {
            int start = v[indexArray[i].intValue()];

            // count a new values vector if it is different
            // from the current one
            if (count < 0 || !areElementsSame(indexArray[i].intValue() + 2, v,
                    indexArray[i-1].intValue() + 2, valueColumns)) {
                count += valueColumns;
            }

            if (start == INITIAL_VALUE_CP) {
                compactor.setRowIndexForInitialValue(count);
            } else if (start == ERROR_VALUE_CP) {
                compactor.setRowIndexForErrorValue(count);
            }
        }

        // count is at the beginning of the last vector,
        // add valueColumns to include that last vector
        count += valueColumns;

        // Call the handler once more to signal the start of
        // delivering real values.
        compactor.startRealValues(count);

        /*
         * Move vector contents up to a contiguous array with only unique
         * vector values, and call the handler function for each vector.
         *
         * This destroys the Properties Vector structure and replaces it
         * with an array of just vector values.
         */
        int[] temp = new int[count];
        count = -valueColumns;
        for (int i = 0; i < rows; ++i) {
            int start = v[indexArray[i].intValue()];
            int limit = v[indexArray[i].intValue() + 1];

            // count a new values vector if it is different
            // from the current one
            if (count < 0 || !areElementsSame(indexArray[i].intValue() + 2,
                    temp, count, valueColumns)) {
                count += valueColumns;
                System.arraycopy(v, indexArray[i].intValue() + 2, temp, count,
                        valueColumns);
            }

            if (start < FIRST_SPECIAL_CP) {
                compactor.setRowIndexForRange(start, limit - 1, count);
            }
        }
        v = temp;

        // count is at the beginning of the last vector,
        // add one to include that last vector
        rows = count / valueColumns + 1;
    }

    /*
     * Get the vectors array after calling compact().
     *
     * @throws IllegalStateException
     */
    public int[] getCompactedArray() {
        if (!isCompacted) {
            throw new IllegalStateException(
                    "Illegal Invocation of the method before compact()");
        }
        return v;
    }

    /*
     * Get the number of rows for the compacted array.
     *
     * @throws IllegalStateException
     */
    public int getCompactedRows() {
        if (!isCompacted) {
            throw new IllegalStateException(
                    "Illegal Invocation of the method before compact()");
        }
        return rows;
    }

    /*
     * Get the number of columns for the compacted array.
     *
     * @throws IllegalStateException
     */
    public int getCompactedColumns() {
        if (!isCompacted) {
            throw new IllegalStateException(
                    "Illegal Invocation of the method before compact()");
        }
        return columns - 2;
    }

    /*
     * Call compact(), create a IntTrie with indexes into the compacted
     * vectors array.
     */
    public IntTrie compactToTrieWithRowIndexes() {
        PVecToTrieCompactHandler compactor = new PVecToTrieCompactHandler();
        compact(compactor);
        return compactor.builder.serialize(new DefaultGetFoldedValue(
                compactor.builder), new DefaultGetFoldingOffset());
    }

    // inner class implementation of Trie.DataManipulate
    private static class DefaultGetFoldingOffset implements Trie.DataManipulate {
        @Override
        public int getFoldingOffset(int value) {
            return value;
        }
    }

    // inner class implementation of TrieBuilder.DataManipulate
    private static class DefaultGetFoldedValue implements
            TrieBuilder.DataManipulate {
        private IntTrieBuilder builder;

        public DefaultGetFoldedValue(IntTrieBuilder inBuilder) {
            builder = inBuilder;
        }

        @Override
        public int getFoldedValue(int start, int offset) {
            int initialValue = builder.m_initialValue_;
            int limit = start + 0x400;
            while (start < limit) {
                boolean[] inBlockZero = new boolean[1];
                int value = builder.getValue(start, inBlockZero);
                if (inBlockZero[0]) {
                    start += TrieBuilder.DATA_BLOCK_LENGTH;
                } else if (value != initialValue) {
                    return offset;
                } else {
                    ++start;
                }
            }
            return 0;
        }
    }

    public static interface CompactHandler {
        public void setRowIndexForRange(int start, int end, int rowIndex);
        public void setRowIndexForInitialValue(int rowIndex);
        public void setRowIndexForErrorValue(int rowIndex);
        public void startRealValues(int rowIndex);
    }
}