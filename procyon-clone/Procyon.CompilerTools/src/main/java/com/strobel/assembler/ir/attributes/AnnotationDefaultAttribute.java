/*
 * AnnotationDefaultAttribute.java
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

package com.strobel.assembler.ir.attributes;

import com.strobel.assembler.metadata.annotations.AnnotationElement;
import com.strobel.core.VerifyArgument;

/**
 * @author Mike Strobel
 */
public final class AnnotationDefaultAttribute extends SourceAttribute {
    private final AnnotationElement _defaultValue;

    public AnnotationDefaultAttribute(final int length, final AnnotationElement defaultValue) {
        super(AttributeNames.AnnotationDefault, length);
        _defaultValue = VerifyArgument.notNull(defaultValue, "defaultValue");
    }

    public AnnotationElement getDefaultValue() {
        return _defaultValue;
    }
}
