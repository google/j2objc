package com.strobel.decompiler.languages;

import com.strobel.annotations.NotNull;
import com.strobel.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * <code>TypeCompilationResults</code> holds the result of decompiling a single type.
 */
public class TypeDecompilationResults {

    /**
     * the mapping from original line numbers to post-compiled line numbers
     */
    private final List<LineNumberPosition> _lineNumberPositions;

    /**
     * Constructs decompilation results.
     *
     * @param lineNumberPositions
     *     the mapping of original to decompiled line numbers, or <code>null</code> if the
     *     decompilation target language does not support line numbers.
     */
    public TypeDecompilationResults(@Nullable final List<LineNumberPosition> lineNumberPositions) {
        _lineNumberPositions = lineNumberPositions;
    }

    /**
     * Returns the line number positions resulting from a decompilation.
     *
     * @return an unmodifiable list containing the line number positions resulting
     *         from a decompilation.  May be empty if the decompilation target language
     *         does not support line numbers.
     */
    @NotNull
    public List<LineNumberPosition> getLineNumberPositions() {
        if (_lineNumberPositions == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(_lineNumberPositions);
    }
}
