package com.tiger.scanner;

public class TigerToken {
    private TokenType type;
    private String value;

    public TigerToken(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public TigerToken(TokenType type) {
        this.type = type;
        this.value = type.getKeywordText();
    }

    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        switch (type) {
            case IDENTIFIER:
                return value + ":id";
            case INTLIT:
                return value + ":intlit";
            case FLOATLIT:
                return value + ":floatlit";
            default:
                return value;
        }
    }
}
