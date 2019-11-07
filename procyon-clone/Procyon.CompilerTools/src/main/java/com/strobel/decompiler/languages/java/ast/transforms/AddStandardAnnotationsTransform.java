package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.assembler.metadata.*;
import com.strobel.core.Predicate;
import com.strobel.core.StringUtilities;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;

import java.util.List;

import static com.strobel.core.CollectionUtilities.any;

public final class AddStandardAnnotationsTransform extends ContextTrackingVisitor<Void> {
    private final static String OVERRIDE_ANNOTATION_NAME = "java/lang/Override";
    private final static String DEPRECATED_ANNOTATION_NAME = "java/lang/Deprecated";

    private final static Predicate<Annotation> IS_OVERRIDE_ANNOTATION = new Predicate<Annotation>() {
        @Override
        public boolean test(final Annotation a) {
            final TypeReference t = a.getType().getUserData(Keys.TYPE_REFERENCE);
            return t != null &&
                   StringUtilities.equals(t.getInternalName(), OVERRIDE_ANNOTATION_NAME);
        }
    };

    private final static Predicate<Annotation> IS_DEPRECATED_ANNOTATION = new Predicate<Annotation>() {
        @Deprecated
        public boolean test(final Annotation a) {
            final TypeReference t = a.getType().getUserData(Keys.TYPE_REFERENCE);
            return t != null &&
                   StringUtilities.equals(t.getInternalName(), DEPRECATED_ANNOTATION_NAME);
        }
    };

    private final AstBuilder _astBuilder;

    public AddStandardAnnotationsTransform(final DecompilerContext context) {
        super(context);
        _astBuilder = context.getUserData(Keys.AST_BUILDER);
    }

    @Override
    public Void visitMethodDeclaration(final MethodDeclaration node, final Void p) {
        tryAddOverrideAnnotation(node);
        tryAddDeprecatedAnnotationToMember(node);
        return super.visitMethodDeclaration(node, p);
    }

    @Override
    public Void visitConstructorDeclaration(final ConstructorDeclaration node, final Void p) {
        tryAddDeprecatedAnnotationToMember(node);
        return super.visitConstructorDeclaration(node, p);
    }

    @Override
    public Void visitFieldDeclaration(final FieldDeclaration node, final Void data) {
        tryAddDeprecatedAnnotationToMember(node);
        return super.visitFieldDeclaration(node, data);
    }

    @Override
    public Void visitEnumValueDeclaration(final EnumValueDeclaration node, final Void data) {
        tryAddDeprecatedAnnotationToMember(node);
        return super.visitEnumValueDeclaration(node, data);
    }

    @Override
    public Void visitTypeDeclaration(final TypeDeclaration typeDeclaration, final Void p) {
        tryAddDeprecatedAnnotationToType(typeDeclaration);
        return super.visitTypeDeclaration(typeDeclaration, p);
    }

    private void tryAddOverrideAnnotation(final MethodDeclaration node) {
        if (any(node.getAnnotations(), IS_OVERRIDE_ANNOTATION)) {
            return;
        }

        final MethodDefinition method = node.getUserData(Keys.METHOD_DEFINITION);

        if (method.isStatic() || method.isConstructor() || method.isTypeInitializer()) {
            return;
        }

        final TypeDefinition declaringType = method.getDeclaringType();

        if (declaringType.getCompilerMajorVersion() < CompilerTarget.JDK1_5.majorVersion) {
            return;
        }

        final TypeReference annotationType = new MetadataParser(declaringType).parseTypeDescriptor(OVERRIDE_ANNOTATION_NAME);

        final List<MethodReference> candidates = MetadataHelper.findMethods(
            declaringType,
            new Predicate<MethodReference>() {
                @Override
                public boolean test(final MethodReference reference) {
                    return StringUtilities.equals(reference.getName(), method.getName());
                }
            },
            false,
            true
        );

        for (final MethodReference candidate : candidates) {
            if (MetadataHelper.isOverride(method, candidate)) {
                final MethodDefinition resolvedCandidate = candidate.resolve();

                if (resolvedCandidate != null &&
                    resolvedCandidate.getDeclaringType().isInterface() &&
                    declaringType.getCompilerMajorVersion() < CompilerTarget.JDK1_6.majorVersion) {

                    continue;
                }

                final Annotation annotation = new Annotation();

                if (_astBuilder != null) {
                    annotation.setType(_astBuilder.convertType(annotationType));
                }
                else {
                    annotation.setType(new SimpleType(annotationType.getSimpleName()));
                }

                node.getAnnotations().add(annotation);
                break;
            }
        }
    }

    private void tryAddDeprecatedAnnotationToMember(final EntityDeclaration node) {
        if (any(node.getAnnotations(), IS_DEPRECATED_ANNOTATION)) {
            return;
        }

        IMemberDefinition member = node.getUserData(Keys.METHOD_DEFINITION);

        if (member == null) {
            member = node.getUserData(Keys.FIELD_DEFINITION);
        }

        if (member == null || (member.getFlags() & Flags.DEPRECATED) != Flags.DEPRECATED) {
            return;
        }

        final TypeReference declaringType = member.getDeclaringType();

        final TypeDefinition resolvedType = declaringType instanceof TypeDefinition
                                            ? (TypeDefinition) declaringType
                                            : declaringType.resolve();

        if (resolvedType == null ||
            resolvedType.getCompilerMajorVersion() < CompilerTarget.JDK1_5.majorVersion) {

            return;
        }

        addAnnotation(node, resolvedType.getResolver(), DEPRECATED_ANNOTATION_NAME);
    }

    private void tryAddDeprecatedAnnotationToType(final TypeDeclaration node) {
        if (any(node.getAnnotations(), IS_DEPRECATED_ANNOTATION)) {
            return;
        }

        final TypeDefinition type = node.getUserData(Keys.TYPE_DEFINITION);

        if (type == null || (type.getFlags() & Flags.DEPRECATED) != Flags.DEPRECATED) {
            return;
        }

        if (type.getCompilerMajorVersion() < CompilerTarget.JDK1_5.majorVersion) {
            return;
        }

        addAnnotation(node, type.getResolver(), DEPRECATED_ANNOTATION_NAME);
    }

    private void addAnnotation(
        final EntityDeclaration node,
        final IMetadataResolver resolver,
        final String annotationName) {

        if (resolver == null) {
            return;
        }

        final Annotation annotation = new Annotation();
        final TypeReference annotationType = new MetadataParser(resolver).parseTypeDescriptor(annotationName);

        if (_astBuilder != null) {
            annotation.setType(_astBuilder.convertType(annotationType));
        }
        else {
            annotation.setType(new SimpleType(annotationType.getSimpleName()));
        }

        node.getAnnotations().add(annotation);
    }
}