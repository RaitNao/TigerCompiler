package com.tiger.scanner;

public enum TokenType {
    ARRAY("array"), IF("if"), THEN("then"), ELSE("else"), WHILE("while"), FOR("for"), TO("to"), DO("do"), LET("let"),
    IN("in"), END("END"), OF("of"), BREAK("break"), NIL("nil"), FUNCTION("function"), VAR("var"), TYPE("type"),
    IMPORT("import"), PRIMITIVE("primitive"), COMMA(","), COLON(":"), SEMICOLON(";"), LPAREN("("), RPAREN(")"),
    LSQBR("["), RSQBR("]"), LCBR("{"), RCBR("}"), DOT("."), PLUS("+"), MINUS("-"), STAR("*"), FWSLASH("/"), EQUAL("="),
    NOTEQUAL("<>"), LESS("<"), LEQ("<="), MORE(">"), MEQ(">="), AND("&"), OR("|"), ASSIGN(":="),
    STRINGLIT, INTLIT, IDENTIFIER, COMMENT;

    private boolean keyword;
    private String keywordText = null;

    TokenType() {
        keyword = false;
    }

    TokenType(String keywordText) {
        keyword = true;
        this.keywordText = keywordText;
    }

    public boolean isKeyword() {
        return keyword;
    }

    public String getKeywordText() {
        return keywordText;
    }

}
