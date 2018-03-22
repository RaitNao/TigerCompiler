package com.tiger.parser;

import com.tiger.syntax.TigerSymbol;

import java.util.Iterator;

public class TigerProduction {
    private TigerSymbol[] symbols;
    private TigerNT NT;

    public TigerProduction(TigerNT NT, TigerSymbol... symbols) {
        this.NT = NT;
        this.symbols = symbols;
    }

    public TigerSymbol[] getSymbols() {
        return symbols.clone();
    }

    public TigerNT getNT() {
        return NT;
    }
}
