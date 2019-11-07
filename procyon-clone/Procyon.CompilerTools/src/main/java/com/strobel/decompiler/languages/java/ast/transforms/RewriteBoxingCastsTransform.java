package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.assembler.metadata.MetadataHelper;
import com.strobel.assembler.metadata.MetadataParser;
import com.strobel.assembler.metadata.MethodReference;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.ExceptionUtilities;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.semantics.ResolveResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RewriteBoxingCastsTransform extends ContextTrackingVisitor<Void> {
    private static final Map<String, MethodReference> BOX_METHODS;

    static {
        Map<String, MethodReference> boxMethods;

        try {
            final String[] methods = {
                "java/lang/Boolean.valueOf:(Z)Ljava/lang/Boolean;",
                "java/lang/Character.valueOf:(C)Ljava/lang/Character;",
                "java/lang/Byte.valueOf:(B)Ljava/lang/Byte;",
                "java/lang/Short.valueOf:(S)Ljava/lang/Short;",
                "java/lang/Integer.valueOf:(I)Ljava/lang/Integer;",
                "java/lang/Long.valueOf:(J)Ljava/lang/Long;",
                "java/lang/Float.valueOf:(F)Ljava/lang/Float;",
                "java/lang/Double.valueOf:(D)Ljava/lang/Double;"
            };

            final MetadataParser parser = new MetadataParser();

            boxMethods = new HashMap<>();

            for (final String s : methods) {
                final TypeReference t = parser.parseTypeDescriptor(s.substring(0, s.indexOf('.')));
                final MethodReference m = parser.parseMethod(t, "valueOf", s.substring(s.indexOf(':') + 1));

                boxMethods.put(t.getInternalName(), m);
            }
        }
        catch (final Throwable ignored) {
            ExceptionUtilities.rethrowCritical(ignored);
            boxMethods = Collections.emptyMap();
        }

        BOX_METHODS = boxMethods;
    }

    private final JavaResolver _resolver;

    protected RewriteBoxingCastsTransform(final DecompilerContext context) {
        super(context);
        _resolver = new JavaResolver(context);
    }

    @Override
    public void run(final AstNode compilationUnit) {
        if (context.getCurrentType().getCompilerTarget().boxWithConstructors()) {
            return;
        }
        super.run(compilationUnit);
    }

    @Override
    public Void visitCastExpression(final CastExpression node, final Void data) {
        super.visitCastExpression(node, data);

        final AstType castType = node.getType();
        final TypeReference typeReference = castType.toTypeReference();

        if (typeReference != null && !typeReference.isPrimitive()) {
            final TypeReference unboxedType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(typeReference);

            if (unboxedType.isPrimitive()) {
                final Expression operand = node.getExpression();
                final ResolveResult rr = _resolver.apply(operand);

                if (rr != null &&
                    rr.getType() != null &&
                    MetadataHelper.getConversionType(unboxedType, rr.getType()).isImplicit()) {

                    final MethodReference boxMethod = BOX_METHODS.get(typeReference.getInternalName());

                    if (boxMethod != null) {
                        castType.remove();
                        operand.remove();

                        final InvocationExpression replacement = castType.invoke("valueOf", operand);

                        replacement.putUserData(Keys.MEMBER_REFERENCE, boxMethod);
                        replacement.getTarget().putUserData(Keys.MEMBER_REFERENCE, boxMethod);

                        node.replaceWith(replacement);
                    }
                }
            }
        }

        return null;
    }
}
