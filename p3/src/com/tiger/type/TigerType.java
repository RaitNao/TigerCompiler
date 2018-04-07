package com.tiger.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TigerType {
    private List<TigerType> typeParams = new ArrayList<>();
    private TigerTypeType type;

    public TigerType(TigerTypeType type) {
        this.type = type;
    }

    public TigerType(TigerTypeType type, List<TigerType> typeParams) {
        this.type = type;
        this.typeParams.addAll(typeParams);
    }

    public TigerTypeType getType() {
        return type;
    }

    public List<TigerType> getTypeParams() {
        return new ArrayList<>(typeParams);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TigerType)) {
            return false;
        }
        TigerType that = (TigerType) obj;
        return this.type == that.type && this.typeParams.equals(that.typeParams);
    }
}
