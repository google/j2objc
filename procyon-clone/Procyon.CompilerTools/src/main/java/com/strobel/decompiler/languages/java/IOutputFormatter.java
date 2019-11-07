/*
 * IOutputFormatter.java
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

package com.strobel.decompiler.languages.java;

import com.strobel.decompiler.languages.java.ast.AstNode;
import com.strobel.decompiler.languages.java.ast.CommentType;

public interface IOutputFormatter {
    void startNode(AstNode node);
    void endNode(AstNode node);

    void writeLabel(String label);
    void writeIdentifier(String identifier);
    void writeKeyword(String keyword);
    void writeOperator(String token);
    void writeDelimiter(String token);
    void writeToken(String token);
    void writeLiteral(String value);
    void writeTextLiteral(String value);

    void space();

    void openBrace(BraceStyle style);
    void closeBrace(BraceStyle style);

    void indent();
    void unindent();

    void newLine();

    void writeComment(CommentType commentType, String content);
    
    /**
     * instructs 'this' formatter to forget what it used to know about the sequence of line
     * number offsets in the source code
     */
    public void resetLineNumberOffsets( OffsetToLineNumberConverter offset2LineNumber);
    
}
