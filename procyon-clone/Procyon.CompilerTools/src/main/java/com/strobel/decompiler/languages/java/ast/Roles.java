/*
 * Roles.java
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

package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.Role;

public final class Roles {
    public final static Role<AstNode> Root = AstNode.ROOT_ROLE;

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Common Roles                                                                                                       //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public final static Role<AstType> TYPE = new Role<>("Type", AstType.class, AstType.NULL);
    public final static Role<AstType> BASE_TYPE = new Role<>("BaseType", AstType.class, AstType.NULL);
    public final static Role<AstType> IMPLEMENTED_INTERFACE = new Role<>("ImplementedInterface", AstType.class, AstType.NULL);
    public final static Role<AstType> TYPE_ARGUMENT = new Role<>("TypeArgument", AstType.class, AstType.NULL);
    public final static Role<AstType> EXTENDS_BOUND = new Role<>("ExtendsBound", AstType.class, AstType.NULL);
    public final static Role<AstType> SUPER_BOUND = new Role<>("SuperBound", AstType.class, AstType.NULL);
    public final static Role<TypeParameterDeclaration> TYPE_PARAMETER = new Role<>("TypeParameter", TypeParameterDeclaration.class);
    public final static Role<Expression> ARGUMENT = new Role<>("Argument", Expression.class, Expression.NULL);
    public final static Role<ParameterDeclaration> PARAMETER = new Role<>("Parameter", ParameterDeclaration.class);
    public final static Role<Expression> EXPRESSION = new Role<>("Expression", Expression.class, Expression.NULL);
    public final static Role<Expression> TARGET_EXPRESSION = new Role<>("Target", Expression.class, Expression.NULL);
    public final static Role<Expression> CONDITION = new Role<>("Condition", Expression.class, Expression.NULL);
    public final static Role<Comment> COMMENT = new Role<>("Comment", Comment.class);
    public final static Role<Identifier> LABEL = new Role<>("Label", Identifier.class, Identifier.NULL);
    public final static Role<Identifier> IDENTIFIER = new Role<>("Identifier", Identifier.class, Identifier.NULL);
    public final static Role<Statement> EMBEDDED_STATEMENT = new Role<>("EmbeddedStatement", Statement.class, Statement.NULL);
    public final static Role<BlockStatement> BODY = new Role<>("Body", BlockStatement.class, BlockStatement.NULL);
    public final static Role<Annotation> ANNOTATION = new Role<>("Annotation", Annotation.class);
    public final static Role<VariableInitializer> VARIABLE = new Role<>("Variable", VariableInitializer.class, VariableInitializer.NULL);
    public final static Role<EntityDeclaration> TYPE_MEMBER = new Role<>("TypeMember", EntityDeclaration.class);
    public final static Role<TypeDeclaration> TOP_LEVEL_TYPE_ROLE = new Role<>("TopLevelType", TypeDeclaration.class, TypeDeclaration.NULL);
    public final static Role<TypeDeclaration> LOCAL_TYPE_DECLARATION = new Role<>("LocalTypeDeclaration", TypeDeclaration.class, TypeDeclaration.NULL);
    public final static Role<AstType> THROWN_TYPE = new Role<>("ThrownType", AstType.class, AstType.NULL);
    public final static Role<PackageDeclaration> PACKAGE = new Role<>("Package", PackageDeclaration.class, PackageDeclaration.NULL);
    public final static Role<NewLineNode> NEW_LINE = new Role<>("NewLine", NewLineNode.class);
    public final static Role<TextNode> TEXT = new Role<>("Text", TextNode.class);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COMMON TOKENS                                                                                                      //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public final static TokenRole LEFT_PARENTHESIS = new TokenRole("(", TokenRole.FLAG_DELIMITER);
    public final static TokenRole RIGHT_PARENTHESIS = new TokenRole(")", TokenRole.FLAG_DELIMITER);
    public final static TokenRole LEFT_BRACKET = new TokenRole("[", TokenRole.FLAG_DELIMITER);
    public final static TokenRole RIGHT_BRACKET = new TokenRole("]", TokenRole.FLAG_DELIMITER);
    public final static TokenRole LEFT_BRACE = new TokenRole("{", TokenRole.FLAG_DELIMITER);
    public final static TokenRole RIGHT_BRACE = new TokenRole("}", TokenRole.FLAG_DELIMITER);
    public final static TokenRole LEFT_CHEVRON = new TokenRole("<", TokenRole.FLAG_DELIMITER);
    public final static TokenRole RIGHT_CHEVRON = new TokenRole(">", TokenRole.FLAG_DELIMITER);
    public final static TokenRole COMMA = new TokenRole(",", TokenRole.FLAG_DELIMITER);
    public final static TokenRole DOT = new TokenRole(".", TokenRole.FLAG_DELIMITER);
    public final static TokenRole SEMICOLON = new TokenRole(";", TokenRole.FLAG_DELIMITER);
    public final static TokenRole COLON = new TokenRole(":", TokenRole.FLAG_DELIMITER);
    public final static TokenRole DOUBLE_COLON = new TokenRole("::", TokenRole.FLAG_DELIMITER);
    public final static TokenRole ASSIGN = new TokenRole("=", TokenRole.FLAG_OPERATOR);
    public final static TokenRole PIPE = new TokenRole("|", TokenRole.FLAG_OPERATOR);
    public final static TokenRole VARARGS = new TokenRole("...", TokenRole.FLAG_DELIMITER);

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // KEYWORD TOKENS                                                                                                     //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public final static TokenRole DEFAULT_KEYWORD = new TokenRole("default", TokenRole.FLAG_KEYWORD);
    public final static TokenRole PACKAGE_KEYWORD = new TokenRole("package", TokenRole.FLAG_KEYWORD);
    public final static TokenRole ENUM_KEYWORD = new TokenRole("enum", TokenRole.FLAG_KEYWORD);
    public final static TokenRole INTERFACE_KEYWORD = new TokenRole("interface", TokenRole.FLAG_KEYWORD);
    public final static TokenRole CLASS_KEYWORD = new TokenRole("class", TokenRole.FLAG_KEYWORD);
    public final static TokenRole ANNOTATION_KEYWORD = new TokenRole("@interface", TokenRole.FLAG_KEYWORD);
    public final static TokenRole EXTENDS_KEYWORD = new TokenRole("extends", TokenRole.FLAG_KEYWORD);
    public final static TokenRole IMPLEMENTS_KEYWORD = new TokenRole("implements", TokenRole.FLAG_KEYWORD);
}
