package com.tiger.parser;

import com.tiger.syntax.TigerSymbol;
import static java.util.Objects.hash;

public class TigerNT implements TigerSymbol {
    private String value;
    private NTType type;

    public TigerNT(String value, NTType type) {
        this.value = value;
        this.type = type;
    }

    public TigerNT(String value) {
        this(value, NTType.NORMAL);
    }

    public NTType getType() {
        return type;
    }

    @Override
    public String strValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TigerNT)) {
            return false;
        }

        TigerNT other = (TigerNT) obj;
        return other.value == value && other.type == type;
    }

    @Override
    public int hashCode() {
        return hash(value, type);
    }
}
