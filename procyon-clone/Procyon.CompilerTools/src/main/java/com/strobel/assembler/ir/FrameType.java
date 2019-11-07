/*
 * FrameType.java
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

/**
 * User: Mike Strobel
 * Date: 1/6/13
 * Time: 4:05 PM
 */
public enum FrameType {
    /**
     * Represents a compressed frame where locals are the same as the locals in the previous frame,
     * except that additional 1-3 locals are defined, and with an empty stack.
     */
    Append,

    /**
     * Represents a compressed frame where locals are the same as the locals in the previous frame,
     * except that the last 1-3 locals are absent and with an empty stack.
     */
    Chop,

    /**
     * Represents a compressed frame with complete frame data.
     */
    Full,

    /**
     * Represents an expanded frame.
     */
    New,

    /**
     * Represents a compressed frame with exactly the same locals as the previous frame and with
     * an empty stack.
     */
    Same,

    /**
     * Represents a compressed frame with exactly the same locals as the previous frame and with
     * a single value on the stack.
     */
    Same1
}
