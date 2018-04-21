package com.tiger.scanner;

import com.tiger.syntax.TigerSymbol;

import static java.util.Objects.hash;

public class TigerToken implements TigerSymbol {
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

    @Override
    public String strValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TigerToken)) {
            return false;
        }

        TigerToken other = (TigerToken) obj;
        return other.value.equals(value) && other.type == type;
    }

    @Override
    public int hashCode() {
        return hash(value, type);
    }
}
