/*
 * RemoveImplicitBoxingTransform.java
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

package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.assembler.metadata.ConversionType;
import com.strobel.assembler.metadata.MemberReference;
import com.strobel.assembler.metadata.MetadataHelper;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.semantics.ResolveResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RemoveImplicitBoxingTransform extends ContextTrackingVisitor<Void> {
    private final static Set<String> BOX_METHODS;
    private final static Set<String> UNBOX_METHODS;

    static {
        BOX_METHODS = new HashSet<>();
        UNBOX_METHODS = new HashSet<>();

        final String[] boxTypes = {
            "java/lang/Byte",
            "java/lang/Short",
            "java/lang/Integer",
            "java/lang/Long",
            "java/lang/Float",
            "java/lang/Double"
        };

        final String[] unboxMethods = {
            "byteValue:()B",
            "shortValue:()S",
            "intValue:()I",
            "longValue:()J",
            "floatValue:()F",
            "doubleValue:()D"
        };

        final String[] boxMethods = {
            "java/lang/Boolean.valueOf:(Z)Ljava/lang/Boolean;",
            "java/lang/Character.valueOf:(C)Ljava/lang/Character;",
            "java/lang/Byte.valueOf:(B)Ljava/lang/Byte;",
            "java/lang/Short.valueOf:(S)Ljava/lang/Short;",
            "java/lang/Integer.valueOf:(I)Ljava/lang/Integer;",
            "java/lang/Long.valueOf:(J)Ljava/lang/Long;",
            "java/lang/Float.valueOf:(F)Ljava/lang/Float;",
            "java/lang/Double.valueOf:(D)Ljava/lang/Double;"
        };

        Collections.addAll(BOX_METHODS, boxMethods);

/*
        for (final String unboxMethod : unboxMethods) {
            UNBOX_METHODS.add("java/lang/Number." + unboxMethod);
        }
*/

        for (final String boxType : boxTypes) {
            for (final String unboxMethod : unboxMethods) {
                UNBOX_METHODS.add(boxType + "." + unboxMethod);
            }
        }

        UNBOX_METHODS.add("java/lang/Character.charValue:()C");
        UNBOX_METHODS.add("java/lang/Boolean.booleanValue:()Z");
    }

    private final JavaResolver _resolver;

    public RemoveImplicitBoxingTransform(final DecompilerContext context) {
        super(context);
        _resolver = new JavaResolver(context);
    }

    @Override
    public Void visitInvocationExpression(final InvocationExpression node, final Void data) {
        super.visitInvocationExpression(node, data);

        if (node.getArguments().size() == 1 &&
            node.getTarget() instanceof MemberReferenceExpression) {

            removeBoxing(node);
        }
        else {
            removeUnboxing(node);
        }

        return null;
    }

    private boolean isValidPrimitiveParent(final InvocationExpression node, final AstNode parent) {
        if (parent == null || parent.isNull()) {
            return false;
        }

        if (parent instanceof BinaryOperatorExpression) {
            final BinaryOperatorExpression binary = (BinaryOperatorExpression) parent;

            //noinspection RedundantIfStatement
            if (binary.getLeft() instanceof NullReferenceExpression ||
                binary.getRight() instanceof NullReferenceExpression) {

                return false;
            }

            final ResolveResult leftResult = _resolver.apply(binary.getLeft());
            final ResolveResult rightResult = _resolver.apply(binary.getRight());

            return leftResult != null &&
                   rightResult != null &&
                   leftResult.getType() != null &&
                   rightResult.getType() != null &&
                   (node == binary.getLeft() ? rightResult.getType().isPrimitive()
                                             : leftResult.getType().isPrimitive());
        }

//        //
//        // IGNORE_TODO: Remove the `if` below once overload resolution is written and integrated.
//        //
//        if (node.getRole() == Roles.ARGUMENT) {
//            final MemberReference member = parent.getUserData(Keys.MEMBER_REFERENCE);
//
//            if (member instanceof MethodReference) {
//                final MethodReference method = (MethodReference) parent.getUserData(Keys.MEMBER_REFERENCE);
//
//                if (method == null || MetadataHelper.isOverloadCheckingRequired(method)) {
//                    return false;
//                }
//            }
//        }

        if (node.getRole() == Roles.TARGET_EXPRESSION &&
            parent instanceof MemberReferenceExpression &&
            isUnboxingExpression(parent.getParent())) {

            return true;
        }

        return !(
            node.getRole() == Roles.TARGET_EXPRESSION ||
            parent instanceof ClassOfExpression ||
            parent instanceof SynchronizedStatement ||
            parent instanceof ThrowStatement
        );
    }

    private boolean isUnboxingExpression(final AstNode node) {
        if (!(node instanceof InvocationExpression)) {
            return false;
        }

        final InvocationExpression e = (InvocationExpression) node;

        if (e.isNull()) {
            return false;
        }

        final Expression target = e.getTarget();

        if (!(target instanceof MemberReferenceExpression)) {
            return false;
        }

        final MemberReference reference = e.getUserData(Keys.MEMBER_REFERENCE);

        if (!(reference instanceof MethodReference)) {
            return false;
        }

        final String key = reference.getFullName() + ":" + reference.getSignature();

        return UNBOX_METHODS.contains(key);
    }

    private void removeUnboxing(final InvocationExpression e) {
        if (!isUnboxingExpression(e)) {
            return;
        }

//        final AstNode parent = e.getParent();
//
//        if (e.getRole() == Roles.ARGUMENT) {
//            removeUnboxingForArgument(e);
//            return;
//        }
//
//        if (parent instanceof CastExpression) {
//            removeUnboxingForCast(
//                e,
//                (MemberReferenceExpression) target,
//                (CastExpression) parent
//            );
//            return;
//        }
//
//        if (parent instanceof ConditionalExpression) {
//            removeUnboxingForCondition(
//                e,
//                (MemberReferenceExpression) target,
//                (ConditionalExpression) parent
//            );
//            return;
//        }

        performUnboxingRemoval(e, (MemberReferenceExpression) e.getTarget());
    }

//    private void removeUnboxingForCondition(
//        final InvocationExpression e,
//        final MemberReferenceExpression target,
//        final ConditionalExpression parent) {
//
//        final boolean leftSide = parent.getTrueExpression().isAncestorOf(e);
//        final Expression otherSide = leftSide ? parent.getFalseExpression() : parent.getTrueExpression();
//        final ResolveResult otherResult = _resolver.apply(otherSide);
//
//        if (otherResult == null || otherResult.getType() == null || !otherResult.getType().isPrimitive()) {
//            return;
//        }
//
//        performUnboxingRemoval(e, target);
//    }

    private boolean performUnboxingRemoval(final InvocationExpression e, final MemberReferenceExpression target) {
        final Expression boxedValue = target.getTarget();
        final MethodReference unboxMethod = (MethodReference) e.getUserData(Keys.MEMBER_REFERENCE);
        final AstBuilder astBuilder = context.getUserData(Keys.AST_BUILDER);

        //
        // If we have a situation where we're unboxing an boxed value, we do some additional
        // analysis to make sure we end up with a concise but *legal* type conversion.
        //
        // For example, given `f(double _d)`, `g(int _i)`, a Double `d` and an Integer `i`:
        //
        // `f(i.floatValue())` can be simplified to `f((double)i)` or even `f(i)`
        //
        // ...but...
        //
        // `g(d.intValue())` cannot be simplified to `g((int)d)`. A boxed type `S` can only
        // be cast to a primitive type `t` if there exists an implicit conversion from
        // the underlying primitive type `s` to `t`.
        //
        final TypeReference targetType = unboxMethod.getReturnType();
        final TypeReference sourceType = unboxMethod.getDeclaringType();

        switch (MetadataHelper.getNumericConversionType(targetType, sourceType)) {
            case IDENTITY:
            case IMPLICIT:
            case IMPLICIT_LOSSY: {
                boxedValue.remove();

                e.replaceWith(
                    new CastExpression(
                        astBuilder.convertType(unboxMethod.getReturnType()),
                        boxedValue
                    )
                );

                return true;
            }
        }

        return false;
    }

//    private void removeUnboxingForArgument(final InvocationExpression e) {
//        final AstNode parent = e.getParent();
//
//        final MemberReference unboxMethod = e.getUserData(Keys.MEMBER_REFERENCE);
//        final MemberReference outerBoxMethod = parent.getUserData(Keys.MEMBER_REFERENCE);
//
//        if (!(unboxMethod instanceof MethodReference && outerBoxMethod instanceof MethodReference)) {
//            return;
//        }
//
//        final String unboxMethodKey = unboxMethod.getFullName() + ":" + unboxMethod.getSignature();
//        final String boxMethodKey = outerBoxMethod.getFullName() + ":" + outerBoxMethod.getSignature();
//
//        if (!UNBOX_METHODS.contains(unboxMethodKey)) {
//            return;
//        }
//
//        final Expression boxedValue = ((MemberReferenceExpression) e.getTarget()).getTarget();
//
//        if (!BOX_METHODS.contains(boxMethodKey) ||
//            !(parent instanceof InvocationExpression &&
//              isValidPrimitiveParent((InvocationExpression) parent, parent.getParent()))) {
//
//            boxedValue.remove();
//            e.replaceWith(boxedValue);
//            return;
//        }
//
//        //
//        // If we have a situation where we're boxing an unboxed value, we do some additional
//        // analysis to make sure we end up with a concise but *legal* type conversion.
//        //
//        // For example, given `f(Double d)`, `g(Short s)`, and an Integer `i`:
//        //
//        // `f(Double.valueOf((double)i.intValue()))` can be simplified to `f((double)i)`
//        //
//        // ...but...
//        //
//        // `g(Short.valueOf((short)i.intValue()))` cannot be simplified to `g((short)i)`;
//        // it must be simplified to `g((short)i.intValue())`.  A boxed type `S` can only
//        // be cast to a primitive type `t` if there exists an implicit conversion from
//        // the underlying primitive type `s` to `t`.
//        //
//
//        final ResolveResult boxedValueResult = _resolver.apply(boxedValue);
//
//        if (boxedValueResult == null || boxedValueResult.getType() == null) {
//            return;
//        }
//
//        final TypeReference targetType = ((MethodReference) outerBoxMethod).getReturnType();
//        final TypeReference sourceType = boxedValueResult.getType();
//
//        switch (MetadataHelper.getNumericConversionType(targetType, sourceType)) {
//            case IDENTITY:
//            case IMPLICIT: {
//                //
//                // We don't actually want to remove round-trip boxing without some sort of cast,
//                // or we may end up with:
//                //
//                // Integer i = null; Integer j = Integer.valueOf(i.intValue());
//                //
//                // ...becoming:
//                //
//                // Integer i = null; Integer j = i;
//                //
///*
//                boxedValue.remove();
//                parent.replaceWith(boxedValue);
//                break;
//*/
//                return;
//            }
//
//            case EXPLICIT_TO_UNBOXED: {
//                final AstBuilder astBuilder = context.getUserData(Keys.AST_BUILDER);
//
//                if (astBuilder == null) {
//                    return;
//                }
//
//                boxedValue.remove();
//
//                final TypeReference castType = ((MethodReference) outerBoxMethod).getParameters().get(0).getParameterType();
//                final CastExpression cast = new CastExpression(astBuilder.convertType(castType), boxedValue);
//
//                parent.replaceWith(cast);
//
//                break;
//            }
//
//            default: {
//                return;
//            }
//        }
//    }
//
//    private void removeUnboxingForCast(
//        final InvocationExpression e,
//        final MemberReferenceExpression target,
//        final CastExpression parent) {
//
//        final TypeReference targetType = parent.getType().toTypeReference();
//
//        if (targetType == null || !targetType.isPrimitive()) {
//            return;
//        }
//
//        final Expression boxedValue = target.getTarget();
//        final ResolveResult boxedValueResult = _resolver.apply(boxedValue);
//
//        if (boxedValueResult == null || boxedValueResult.getType() == null) {
//            return;
//        }
//
//        final TypeReference sourceType = boxedValueResult.getType();
//        final ConversionType conversionType = MetadataHelper.getNumericConversionType(targetType, sourceType);
//
//        switch (conversionType) {
//            case IMPLICIT:
//            case EXPLICIT:
//            case EXPLICIT_TO_UNBOXED: {
//                boxedValue.remove();
//                e.replaceWith(boxedValue);
//                return;
//            }
//
//            default:
//                return;
//        }
//    }

    private void removeBoxing(final InvocationExpression node) {
        if (!isValidPrimitiveParent(node, node.getParent())) {
            return;
        }

        final MemberReference reference = node.getUserData(Keys.MEMBER_REFERENCE);

        if (!(reference instanceof MethodReference)) {
            return;
        }

        final String key = reference.getFullName() + ":" + reference.getSignature();

        if (!BOX_METHODS.contains(key)) {
            return;
        }

        final AstNodeCollection<Expression> arguments = node.getArguments();
        final Expression underlyingValue = arguments.firstOrNullObject();
        final ResolveResult valueResult = _resolver.apply(underlyingValue);

        if (valueResult == null || valueResult.getType() == null) {
            return;
        }

        final TypeReference sourceType = valueResult.getType();
        final TypeReference targetType = ((MethodReference) reference).getReturnType();
        final ConversionType conversionType = MetadataHelper.getNumericConversionType(targetType, sourceType);

        switch (conversionType) {
            case IMPLICIT:
            case IMPLICIT_LOSSY: /*{
                underlyingValue.remove();
                node.replaceWith(underlyingValue);
                break;
            }*/

            case EXPLICIT:
            case EXPLICIT_TO_UNBOXED: {
                final AstBuilder astBuilder = context.getUserData(Keys.AST_BUILDER);

                if (astBuilder == null) {
                    return;
                }

                final TypeReference castType;

                if (conversionType == ConversionType.EXPLICIT_TO_UNBOXED) {
                    castType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(targetType);
                }
                else {
                    castType = targetType;
                }

                underlyingValue.remove();
                node.replaceWith(new CastExpression(astBuilder.convertType(castType), underlyingValue));

                break;
            }
        }
    }
}

