/*
 * AttributeNames.java
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

import com.strobel.util.ContractUtils;

/**
 * @author Mike Strobel
 */
public final class AttributeNames {
    public static final String SourceFile = "SourceFile";
    public static final String ConstantValue = "ConstantValue";
    public static final String Code = "Code";
    public static final String Exceptions = "Exceptions";
    public static final String LineNumberTable = "LineNumberTable";
    public static final String LocalVariableTable = "LocalVariableTable";
    public static final String InnerClasses = "InnerClasses";
    public static final String Synthetic = "Synthetic";
    public static final String BootstrapMethods = "BootstrapMethods";
    public static final String Signature = "Signature";
    public static final String Deprecated = "Deprecated";
    public static final String EnclosingMethod = "EnclosingMethod";
    public static final String LocalVariableTypeTable = "LocalVariableTypeTable";
    public static final String RuntimeVisibleAnnotations = "RuntimeVisibleAnnotations";
    public static final String RuntimeInvisibleAnnotations = "RuntimeInvisibleAnnotations";
    public static final String RuntimeVisibleParameterAnnotations = "RuntimeVisibleParameterAnnotations";
    public static final String RuntimeInvisibleParameterAnnotations = "RuntimeInvisibleParameterAnnotations";
    public static final String AnnotationDefault = "AnnotationDefault";
    public static final String MethodParameters = "MethodParameters";

    private AttributeNames() {
        throw ContractUtils.unreachable();
    }
}
