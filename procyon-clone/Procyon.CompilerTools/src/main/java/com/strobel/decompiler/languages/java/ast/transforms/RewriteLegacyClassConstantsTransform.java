package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.annotations.Nullable;
import com.strobel.assembler.metadata.*;
import com.strobel.core.VerifyArgument;
import com.strobel.decompiler.DecompilerContext;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.patterns.*;

import javax.lang.model.element.Modifier;

import static com.strobel.core.CollectionUtilities.firstOrDefault;

public class RewriteLegacyClassConstantsTransform implements IAstTransform {
    private final static int CANDIDATE_MAX_CODE_SIZE = 32;
    private final DecompilerContext _context;

    public RewriteLegacyClassConstantsTransform(final DecompilerContext context) {
        _context = context;
    }

    @Override
    public void run(final AstNode compilationUnit) {
        final TypeDefinition currentType = _context.getCurrentType();

        if (currentType.getCompilerTarget().hasClassLiterals()) {
            return;
        }

        final MethodDefinition classMethod = tryLocateClassMethod(currentType, compilationUnit);

        if (classMethod != null &&
            classMethod.getDeclaringType() != null) {

            new Rewriter(_context, classMethod).run(compilationUnit);
        }
    }

    private MethodDefinition tryLocateClassMethod(final TypeDefinition currentType, final AstNode compilationUnit) {
        final ClassMethodLocator locator = new ClassMethodLocator(_context);

        locator.run(compilationUnit);

        if (locator.classMethod != null) {
            return locator.classMethod;
        }

        if (currentType.isNested()) {
            return tryLocateClassMethodOutOfScope(currentType);
        }

        return null;
    }

    @Nullable
    private MethodDefinition tryLocateClassMethodOutOfScope(final TypeDefinition currentType) {
        final TypeDefinition enclosingType = MetadataHelper.getOutermostEnclosingType(currentType);

        if (enclosingType == null) {
            return null;
        }

        final AstBuilder builder = _context.getUserData(Keys.AST_BUILDER);

        for (final MethodDefinition m : enclosingType.getDeclaredMethods()) {
            if (ClassMethodLocator.isClassMethodCandidate(m)) {
                final MethodBody body = m.getBody();

                if (body != null &&
                    body.getCodeSize() <= CANDIDATE_MAX_CODE_SIZE) {

                    final MethodDeclaration method = builder.createMethod(m);

                    if (ClassMethodLocator.PATTERN.matches(method)) {
                        return m;
                    }
                }
            }
        }

        return null;
    }

    // <editor-fold defaultstate="collapsed" desc="ClassMethodLocator Class">

    private final static class ClassMethodLocator extends ContextTrackingVisitor<Void> {
        final static MethodDeclaration PATTERN = createPattern();

        private TypeDeclaration _currentType;

        MethodDefinition classMethod;

        protected ClassMethodLocator(final DecompilerContext context) {
            super(context);
        }

        @Override
        protected boolean shouldContinue() {
            return classMethod == null;
        }

        @Override
        public Void visitTypeDeclaration(final TypeDeclaration typeDeclaration, final Void p) {
            if (_currentType != null) {
                return null;
            }

            _currentType = typeDeclaration;

            try {
                return super.visitTypeDeclaration(typeDeclaration, p);
            }
            finally {
                _currentType = null;
            }
        }

        @Override
        public Void visitConstructorDeclaration(final ConstructorDeclaration node, final Void p) {
            return null;
        }

        @Override
        public Void visitFieldDeclaration(final FieldDeclaration node, final Void data) {
            return null;
        }

        @Override
        protected Void visitChildren(final AstNode node, final Void data) {
            AstNode next;

            for (AstNode child = node.getFirstChild(); child != null; child = next) {
                //
                // Store next to allow the loop to continue if the visitor removes/replaces child.
                //
                next = child.getNextSibling();
                child.acceptVisitor(this, data);
            }

            return null;
        }

        @Override
        public Void visitMethodDeclaration(final MethodDeclaration node, final Void p) {
            final MethodDefinition m = node.getUserData(Keys.METHOD_DEFINITION);

            if (isClassMethodCandidate(m) && PATTERN.matches(node)) {
                this.classMethod = m;
            }

            return null;
        }

        static boolean isClassMethodCandidate(final MethodDefinition m) {
            return m != null &&
                   m.isSynthetic() &&
                   m.isStatic() &&
                   m.isPackagePrivate() &&
                   m.getParameters().size() == 1 &&
                   CommonTypeReferences.Class.isEquivalentTo(m.getReturnType()) &&
                   CommonTypeReferences.String.isEquivalentTo(m.getParameters().get(0).getParameterType());
        }

        // <editor-fold defaultstate="collapsed" desc="Pattern Construction">

        private static MethodDeclaration createPattern() {
            //
            // static /* synthetic */ Class class$(String s) {
            //     ClassNotFoundException ex;
            //     try {
            //         return Class.forName(s);
            //     }
            //     catch (ClassNotFoundException ex) {
            //         /* 1.4 */ throw new NoClassDefFoundError().initCause((Throwable)ex);
            //         /* 1.2 */ throw new NoClassDefFoundError(ex.getMessage());
            //     }
            // }
            //

            final MethodDeclaration method = new MethodDeclaration();
            final MetadataParser parser = new MetadataParser();

            final TypeReference classNotFoundException = parser.parseTypeDescriptor("java/lang/ClassNotFoundException");
            final TypeReference noClassDefFoundError = parser.parseTypeDescriptor("java/lang/NoClassDefFoundError");

            final AstType classType = new AstTypeMatch(CommonTypeReferences.Class).toType();
            final AstType throwable = new AstTypeMatch(CommonTypeReferences.Throwable).toType();

            method.setName(Pattern.ANY_STRING);
            method.getModifiers().add(new JavaModifierToken(Modifier.STATIC));
            method.setReturnType(classType);

            method.getParameters().add(
                new ParameterDeclaration(
                    Pattern.ANY_STRING,
                    new AstTypeMatch(CommonTypeReferences.String).toType()
                )
            );

            final BlockStatement tryBlock = new BlockStatement(
                classType.clone()
                         .makeReference()
                         .invoke("forName", new ParameterReferenceNode(0).toExpression())
                         .makeReturn()
            );

            final BlockStatement catchBlock = new Choice(
                // Java 1.4 Pattern: throw new NoClassDefFoundError().initCause((Throwable)ex);
                new BlockStatement(
                    new AstTypeMatch(noClassDefFoundError)
                        .toType()
                        .makeNew()
                        .invoke("initCause", new IdentifierExpressionBackReference("catch").toExpression().cast(throwable))
                        .makeThrow()
                ),
                // Java 1.2 Pattern: throw new NoClassDefFoundError(ex.getMessage());
                new BlockStatement(
                    new AstTypeMatch(noClassDefFoundError)
                        .toType()
                        .makeNew(new IdentifierExpressionBackReference("catch").toExpression().invoke("getMessage"))
                        .makeThrow()
                )
            ).toBlockStatement();

            final CatchClause catchClause = new CatchClause();

            catchClause.setVariableName(Pattern.ANY_STRING);
            catchClause.getExceptionTypes().add(new AstTypeMatch(classNotFoundException).toType());
            catchClause.setBody(catchBlock);

            final TryCatchStatement tryCatch = new TryCatchStatement();

            tryCatch.setTryBlock(tryBlock);
            tryCatch.getCatchClauses().add(new NamedNode("catch", catchClause).toCatchClause());

            method.setBody(
                new BlockStatement(
                    // As yet un-removed variable declaration: ClassNotFoundException ex;
                    new VariableDeclarationStatement(
                        new AstTypeMatch(classNotFoundException).toType(),
                        Pattern.ANY_STRING
                    ).makeOptional().toStatement(),
                    tryCatch
                )
            );

            return method;
        }

        // </editor-fold>
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Rewriter Class">

    private final static class Rewriter extends ContextTrackingVisitor<Void> {
        private final static ConditionalExpression PATTERN = createPattern();

        private final MethodDefinition _method;
        private final MetadataParser _parser;

        protected Rewriter(final DecompilerContext context, final MethodDefinition classMethod) {
            super(context);

            _method = VerifyArgument.notNull(classMethod, "classMethod");
            _parser = new MetadataParser(classMethod.getDeclaringType());
        }

        @Override
        public Void visitConditionalExpression(final ConditionalExpression node, final Void data) {
            super.visitConditionalExpression(node, data);

            final Match m = PATTERN.match(node);

            if (m.success()) {
                final InvocationExpression call = firstOrDefault(m.<InvocationExpression>get("methodCall"));
                final MemberReference method = call != null ? call.getUserData(Keys.MEMBER_REFERENCE) : null;

                if (method == null || !method.isEquivalentTo(_method)) {
                    return null;
                }

                final PrimitiveExpression className = firstOrDefault(m.<PrimitiveExpression>get("class"));

                if (className != null &&
                    className.getValue() instanceof String) {

                    final AstBuilder builder = context.getUserData(Keys.AST_BUILDER);
                    final String dottedName = (String) className.getValue();
                    final TypeReference classType = _parser.parseTypeDescriptor(dottedName.replace('.', '/'));

                    final ClassOfExpression replacement = new ClassOfExpression(
                        call.getOffset(),
                        builder.convertType(classType)
                    );

                    node.replaceWith(replacement);
                }
            }

            return null;
        }

        // <editor-fold defaultstate="collapsed" desc="Pattern Construction">

        private static ConditionalExpression createPattern() {
            final Expression target = new TypeReferenceExpression(new AnyNode().toType()).makeOptional().toExpression();
            final MemberReferenceExpression access = new MemberReferenceExpression(target, Pattern.ANY_STRING);

            @SuppressWarnings("UnnecessaryLocalVariable")
            final ConditionalExpression pattern = new ConditionalExpression(
                new BinaryOperatorExpression(
                    access.withName("fieldAccess").toExpression(),
                    BinaryOperatorType.EQUALITY,
                    new NullReferenceExpression()
                ),
                new AssignmentExpression(
                    new BackReference("fieldAccess").toExpression(),
                    AssignmentOperatorType.ASSIGN,
                    target.clone()
                          .invoke(Pattern.ANY_STRING, new TypedLiteralNode("class", String.class).toExpression())
                          .withName("methodCall")
                          .toExpression()
                ),
                new BackReference("fieldAccess").toExpression()
            );

            return pattern;
        }

        // </editor-fold>
    }

    // </editor-fold>
}
