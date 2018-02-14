package com.tiger.scanner;

public enum TokenType {
    COMMENT, ARRAY("array"), BEGIN("begin"), BOOLEAN("boolean"), IF("if"), THEN("then"), ELSE("else"), WHILE("while"), FOR("for"), TO("to"), DO("do"), LET("let"),
    IN("in"), INT("int"), END("end"), ENDDO("enddo"), ENDIF("endif"), FALSE("false"), TRUE("true"), FLOAT("float"), OF("of"), BREAK("break"), NIL("nil"),
    FUNC("func"), VAR("var"), TYPE("type"), UNIT("unit"), IMPORT("import"), PRIMITIVE("primitive"), RETURN("return"),
    COMMA(","), COLON(":"), SEMICOLON(";"), LPAREN("("), RPAREN(")"), LSQBR("["), RSQBR("]"), LCBR("{"), RCBR("}"), DOT("."), PLUS("+"), MINUS("-"),
    STAR("*"), FWSLASH("/"), EQUAL("="), NOTEQUAL("<>"), LESS("<"), LEQ("<="), MORE(">"), MEQ(">="), AND("&"), OR("|"), ASSIGN(":="),
    INTLIT, FLOATLIT, IDENTIFIER;

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
